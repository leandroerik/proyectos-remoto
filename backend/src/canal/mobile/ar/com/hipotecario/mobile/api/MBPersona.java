package ar.com.hipotecario.mobile.api;

import static ar.com.hipotecario.mobile.lib.Objeto.empty;
import static ar.com.hipotecario.mobile.servicio.SqlRenaper.consultaRenaperOnboarding;
import static ar.com.hipotecario.mobile.servicio.SqlRenaper.consultaTablaInternaRenaper;
import static ar.com.hipotecario.mobile.servicio.SqlRenaper.consultaTablaInternaRenaperValidado;
import static ar.com.hipotecario.mobile.servicio.SqlRenaper.ultimoIdDispositivoPendiente;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.xml.bind.DatatypeConverter;

import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.libreriariesgofraudes.domain.enums.BankProcessChangeDataType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.mb.ChangeDataMBBMBankProcess;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.servicio.*;
import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.helper.OriginacionHelper;
import ar.com.hipotecario.mobile.lib.Fecha;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.negocio.EnumEstadoUsuarioRenaper;
import ar.com.hipotecario.mobile.negocio.EnumOrigenValidacionRenaper;
import ar.com.hipotecario.mobile.negocio.SituacionLaboral;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestContexto;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.SqlEsales;
import ar.com.hipotecario.mobile.servicio.SqlMobile;
import ar.com.hipotecario.mobile.servicio.SqlRenaper;
import ar.com.hipotecario.mobile.servicio.SqlScanDni;
import ar.com.hipotecario.mobile.servicio.SqlSoftTokenService;

public class MBPersona {

    /*
     * Errores Scan Dni
     */

    private static final String ORIGEN_INVALIDO = "ORIGEN_INVALIDO";
    private static final String SIN_CUIT = "SIN_CUIT";
    private static final String SIN_PSEUDO_SESION = "SIN_PSEUDO_SESION";
    private static final String ERROR_TIENE_IR_SUCURSAL = "ERROR_TIENE_IR_SUCURSAL";
    private static final String ERROR_OBTENER_USUARIO = "ERROR_OBTENER_USUARIO";
    private static final String ERROR_INSERTAR_USUARIO_TOKEN = "ERROR_INSERTAR_USUARIO_TOKEN";
    private static final String VALIDAR_DNI_SUCURSAL = "VALIDAR_DNI_SUCURSAL";
    private static final String MAXIMOS_REINTENTOS = "MAXIMOS_REINTENTOS";
    private static final String NO_COINCIDE_DNI = "NO_COINCIDE_DNI";
    private static final String ERROR_RENAPER = "ERROR_RENAPER";
    private static final String PERSONA_NO_ENCONTRADA = "PERSONA_NO_ENCONTRADA";
    private static final String ERROR_OBTENER_RENAPER_ESTADO = "ERROR_OBTENER_RENAPER_ESTADO";
    private static final String ERROR_GUARDAR_USUARIO_RENAPER = "ERROR_GUARDAR_USUARIO_RENAPER";
    private static final String ERROR_REINTENTAR = "ERROR_REINTENTAR";
    private static final String ERROR_FORMATO = "ERROR_FORMATO";
    private static final String CUIL_NO_EXSITE = "CUIL_NO_EXSITE";
    private static final String ERROR_VALIDADO = "ERROR_VALIDADO";
//	private static final String DNI_ULTIMO_EJEMPLAR = "DNI_NO_ES_ULTIMO_EJEJMPLAR";
//	private static final String MENOR_EDAD = "MENOR_EDAD";

    /*
     * Constantes respuestas
     */

    private static final String CONSOLIDADA = "CONSOLIDADA";
    private static final String SCAN_DNI = "SCAN_DNI";
    private static final String MENSAJE = "mensaje";

    /*
     * Constantes Scan Dni
     */

    private static final String LABEL_PUSH = "SCAN_DNI";
    private static final String TITLE_PUSH = "Nuevo Ingreso";
    private static final String MENSAJE_PUSH = "✋🏻⚠️ Hemos detectado un ingreso en la App BH ⚠️✋🏻 Si no fuiste vos, entrá a Home Banking y hace click en “Mi cuenta está en riesgo”";
    //	private static final String URL_PUSH = "https://hb.hipotecario.com.ar/hb/#/linearoja/bloquearCuenta";
    private static final String TYPE_PUSH = "Login";
    private static final String NOTIFICATION_TYPE_PUSH = "Login";
    private static final String FORMATO_FECHA_RENAPER = "yyyy-MM-dd HH:mm:ss";
    private static final String FORMATO_FECHA_SCANNER = "dd/MM/yyyy";
    private static final String REGEX_FECHA_SCANNER = "^(0[1-9]|[12][0-9]|3[01])[- /.](0[1-9]|1[012])[- /.](19|20)\\d\\d$";
    private static final String FORMATO_FECHA_CONVERT = "yyyy-MM-dd";
    private static final String REGEX_NUMBER = "^[0-9]*$";
    private static final String NUMBER_REPLACE = "[^0-9.]";

    public static RespuestaMB persona(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        if (Objeto.anyEmpty(documento)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        Objeto datos = new Objeto();
        Date fechaHoraUltimaConexion = contexto.sesion().fechaHoraUltimaConexion();

        List<Objeto> personas = RestPersona.personas(contexto, documento, idTipoDocumento, idSexo);
        if (personas == null) {
            return RespuestaMB.error();
        } else if (personas.isEmpty()) {
            return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
        } else if (personas.size() > 1) {
            return RespuestaMB.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        Objeto persona = personas.get(0);

        datos.set("nombre", Texto.primerasMayuscula(persona.string("nombre").split(" ")[0]));
        datos.set("nombres", Texto.primerasMayuscula(persona.string("nombre")));
        datos.set("apellido", Texto.primerasMayuscula(persona.string("apellido")).split(" ")[0]);
        datos.set("apellidos", Texto.primerasMayuscula(persona.string("apellido")));
        datos.set("cuit", persona.string("numeroIdentificacionTributaria"));
        datos.set("personUuid", DatatypeConverter.printBase64Binary((contexto.idCobis() != null ? contexto.idCobis() : persona.string("idCliente")).getBytes()));
        Integer edad; // emm- calculo la edad de la persona
        try {
            Calendar fechaNacimiento = Calendar.getInstance();
            fechaNacimiento.setTime(persona.date("fechaNacimiento", "yyyy-MM-dd"));
            edad = calcularEdad(LocalDate.of(fechaNacimiento.get(Calendar.YEAR),
                    fechaNacimiento.get(Calendar.MONTH) + 1, fechaNacimiento.get(Calendar.DATE)));
        } catch (Exception e) { // si da error prefiero que siga, y que no de error
            edad = null;
        }
        if (edad != null) { // si me vino la edad la informo
            datos.set("edad", edad);
            datos.set("menor", edad < 18);
        } else { // sino viene la edad, informo que es menor por las dudas.
            datos.set("edad", 15);
            datos.set("menor", true);
        }

        datos.set("esEmpleado", Objeto.setOf("EM", "FU").contains(persona.string("tipoPersona")));

        try {
            datos.set("ultimaConexionFormateada",
                    new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fechaHoraUltimaConexion));
        } catch (Exception e) {
            datos.set("ultimaConexionFormateada", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
        }
        datos.set("primerIngreso", fechaHoraUltimaConexion == null);

        return RespuestaMB.exito().set("persona", datos).set("idCobis", persona.string("idCliente"));
    }

    public static int calcularEdad(LocalDate birthDate) {
        LocalDate now = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, now).getYears();
        } else {
            return 0;
        }
    }

    public static RespuestaMB cliente(ContextoMB contexto) {
        Boolean buscarEmail = contexto.parametros.bool("buscarEmail");
        Boolean buscarCelular = contexto.parametros.bool("buscarCelular");
        Boolean buscarDomicilio = contexto.parametros.bool("buscarDomicilio");
        Boolean buscarDatosNacimiento = contexto.parametros.bool("buscarDatosNacimiento");
        boolean prendidoDataValid = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid");

        // LLAMADAS EN PARALELO
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(() -> contexto.sesion().fechaHoraUltimaConexion());
        Futuro<ApiResponseMB> futuroClientes = new Futuro<>(() -> RestPersona.clientes(contexto));
        Futuro<Map<Integer, String>> futuroMapaPaises = (buscarDatosNacimiento) ? new Futuro<>(() -> RestCatalogo.mapaPaises(contexto)) : new Futuro<>(() -> null);
        Futuro<String> futuroEmail = (buscarEmail) ? new Futuro<>(() -> RestPersona.direccionEmail(contexto, contexto.persona().cuit())) : new Futuro<>(() -> null);
        Futuro<Objeto> futuroCelular = (buscarCelular) ? new Futuro<>(() -> RestPersona.celular(contexto, contexto.persona().cuit())) : new Futuro<>(() -> null);
        Futuro<Objeto> futuroDomicilio = (buscarDomicilio) ? new Futuro<>(() -> RestPersona.domicilioPostal(contexto, contexto.persona().cuit())) : new Futuro<>(() -> null);
        Futuro<RespuestaMB> futuroDataValid = (prendidoDataValid) ? new Futuro<>(() -> MBPersona.dataValid(contexto)) : new Futuro<>(() -> null);
        Futuro<RespuestaMB> futuroRedesSociales = (prendidoDataValid) ? new Futuro<>(() -> RestPersona.getRedesSociales(contexto)) : new Futuro<>(() -> null);
        // FIN LLAMADAS EN PARALELO

        Objeto datos = new Objeto();
        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.get();

        ApiResponseMB clientes = futuroClientes.get();
        if (clientes.hayError()) {
            return RespuestaMB.error();
        }

        Objeto cliente = clientes.objetos().get(0);
        datos.set("nombre", Texto.primerasMayuscula(cliente.string("nombres").split(" ")[0]));
        datos.set("nombres", Texto.primerasMayuscula(cliente.string("nombres")));
        datos.set("apellido", Texto.primerasMayuscula(cliente.string("apellidos")).split(" ")[0]);
        datos.set("apellidos", Texto.primerasMayuscula(cliente.string("apellidos")));
        datos.set("cuit", cliente.string("cuit"));
        datos.set("customerId", cliente.string("cuit"));

        datos.set("ultimaConexionFormateada",
                fechaHoraUltimaConexion != null
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fechaHoraUltimaConexion)
                        : "");
        datos.set("primerIngreso", fechaHoraUltimaConexion == null);
        datos.set("edad", cliente.integer("edad"));
        datos.set("menor", cliente.integer("edad") < 18);
        datos.set("actividadAfip", cliente.string("actividadAFIP"));
        datos.set("esEmpleado", Objeto.setOf("EM", "FU").contains(cliente.string("idTipoCliente")));
        datos.set("idSexo", cliente.string("idSexo"));
        datos.set("sexo", cliente.string("idSexo").equals("M") ? "Male" : "Female");
        datos.set("personUuid", DatatypeConverter.printBase64Binary(contexto.idCobis().getBytes()));

        datos.set("idEstadoCivil", cliente.string("idEstadoCivil"));
        switch (cliente.string("idEstadoCivil")) {
            case "S":
                datos.set("estadoCivil", "Single");
                break;
            case "C":
                datos.set("estadoCivil", "Married");
                break;
            case "V":
                datos.set("estadoCivil", "Widower");
                break;
            case "D":
                datos.set("estadoCivil", "Divorced");
                break;
            default:
                datos.set("estadoCivil", "Others");
                break;
        }

        // datos.set("jubilado", "11".equals(cliente.string("idSituacionLaboral")));

        if (buscarDatosNacimiento) {
            datos.set("idPaisNacimiento", cliente.integer("idPaisNacimiento"));
            datos.set("paisNacimiento", futuroMapaPaises.get().get(cliente.integer("idPaisNacimiento")));
            datos.set("idNacionalidad", cliente.integer("idNacionalidad"));
            datos.set("nacionalidad", futuroMapaPaises.get().get(cliente.integer("idNacionalidad")));
            datos.set("ciudadNacimiento", cliente.string("ciudadNacimiento"));
            datos.set("fechaNacimiento", cliente.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
        }

        // TODO: paralelizar

        if (buscarEmail) {
            datos.set("email", futuroEmail.get());
            if (prendidoDataValid) {
                datos.set("checkDirLegal", RestPersona.direccionEmailLegal(contexto, contexto.persona().cuit()));
            }
        }

        if (buscarCelular) {
            Objeto celular = futuroCelular.get();
            if (celular != null) {
                Objeto objeto = new Objeto();
                objeto.set("codigoArea", celular.string("codigoArea"));
                objeto.set("caracteristica", celular.string("caracteristica"));
                objeto.set("numero", celular.string("numero"));
                datos.set("celular", objeto);
            }
        }

        if (buscarDomicilio) {
            Objeto domicilio = futuroDomicilio.get();
            if (domicilio == null) {
                domicilio = new Objeto();
            }
            Objeto objeto = new Objeto();
            objeto.set("calle", domicilio.string("calle"));
            objeto.set("altura", domicilio.string("numero"));
            objeto.set("piso", domicilio.string("piso"));
            objeto.set("departamento", domicilio.string("departamento"));
            objeto.set("provincia", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));
            objeto.set("localidad", RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia", 1),
                    domicilio.integer("idCiudad", 146)));
            objeto.set("codigoPostal", domicilio.string("idCodigoPostal"));
            objeto.set("entreCalle1", domicilio.string("calleEntre1"));
            objeto.set("entreCalle2", domicilio.string("calleEntre2"));
            objeto.set("idProvincia", domicilio.integer("idProvincia", 1));
            objeto.set("idLocalidad", domicilio.integer("idCiudad", 146));
            datos.set("domicilio", objeto);
        }

        if (prendidoDataValid) {
            Objeto dataValidObj = new Objeto();
            Objeto redesSociales = new Objeto();
            try {
                RespuestaMB dataValid = futuroDataValid.get();
                dataValidObj.set("mail", dataValid.get("EMAIL") == null ? "OK" : dataValid.get("EMAIL"));
                dataValidObj.set("telefono", dataValid.get("TEL") == null ? "OK" : dataValid.get("TEL"));
                dataValidObj.set("domicilio", dataValid.get("DOM") == null ? "OK" : dataValid.get("DOM"));
            } catch (Exception e) {
                dataValidObj.set("mail", "OK");
                dataValidObj.set("telefono", "OK");
                dataValidObj.set("domicilio", "OK");
            }
            datos.set("dataValid", dataValidObj);
            try {
                RespuestaMB redes = futuroRedesSociales.get();
                redesSociales.set("twitter", redes.get("twitter"));
                redesSociales.set("instagram", redes.get("instagram"));
            } catch (Exception e) {
                redesSociales.set("twitter", "");
                redesSociales.set("instagram", "");
            }
            datos.set("redesSociales", redesSociales);
        }
        return RespuestaMB.exito("persona", datos);
    }

    public static RespuestaMB telefonoPersonal(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Objeto celular = RestPersona.celular(contexto, contexto.persona().cuit());
        if (celular != null) {
            respuesta.set("celular", celular.string("codigoArea", "") + "-" + celular.string("caracteristica", "") + "-"
                    + celular.string("numero", ""));
        }
        return respuesta;
    }

    public static RespuestaMB actualizarDatosPersonales(ContextoMB contexto) {
        Boolean actualizarEmail = contexto.parametros.bool("actualizarEmail", false);
        Boolean actualizarCelular = contexto.parametros.bool("actualizarCelular", false);
        Boolean actualizarDomicilio = contexto.parametros.bool("actualizarDomicilio", false);
        String emailAnterior = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
        String email = contexto.parametros.string("email", emailAnterior);
        String celularCodigoArea = contexto.parametros.string("celular.codigoArea");
        String celularCaracteristica = contexto.parametros.string("celular.caracteristica");
        String celularNumero = contexto.parametros.string("celular.numero");
        Boolean habilitacionDatosPersonales = contexto.parametros.bool("habilitacionDatosPersonales", false);
        Boolean esOriginacion = contexto.parametros.bool("esOriginacion", false);
        String celularAnterior = RestPersona.numeroCelular(contexto, contexto.persona().cuit()); // el de celular por
        // defecto
        String modificacion = "";

        Boolean validarDatos = contexto.parametros.bool("validarDatos", false);
        Boolean prendidoDataValid = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid");
        Boolean prendidoDataValidOtp = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp");
        if (prendidoDataValidOtp) {
            prendidoDataValid = false;
        }

        Boolean esCaAltaOnline = contexto.parametros.bool("esCaAltaOnline", false);

        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return RespuestaMB.parametrosIncorrectos();

        if (contexto.idCobis() == null) {
            return RespuestaMB.estado("SIN_PSEUDO_SESION");
        }

        if (contexto.esProspecto()) {
            return RespuestaMB.estado("PROSPECTO");
        }

        if (validarDatos) {
            actualizarEmail = false;
            actualizarCelular = false;
            actualizarDomicilio = false;

            if (prendidoDataValidOtp) {
                Objeto resOtpDatavalid = dataValidOtp(contexto);
                if ("VENCIDO".equals(resOtpDatavalid.string("estado"))) {

                    if (resOtpDatavalid.bool("emailVencido")
                            && !contexto.sesion().tieneOtpDatavalidValido("email", emailAnterior)) {
                        return RespuestaMB.estado("REQUIERE_OTP");
                    }

                    if (resOtpDatavalid.bool("telefonoVencido")
                            && !contexto.sesion().tieneOtpDatavalidValido("sms", celularAnterior)) {
                        return RespuestaMB.estado("REQUIERE_OTP");
                    }
                }
            }
        }

        String funcionalidad = "";

        if (!validarDatos) {
            // TODO: corresponsalia/jubilados
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_corresponsalia")) {

                RespuestaMB respuesta = MBCorresponsalia.esJubilado(contexto);
                Boolean esJubilado = (Boolean) respuesta.get("esJubilado");
                if (!esJubilado) {
                    if (esOriginacion) {
                        // no tiene que entrar por originacion y modificar mail y celu. Solo modifica
                        // direccion y celular hay un servicio que actualiza mail y celular hecho para
                        // originacion que se usa en otro lado
                        RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "originacion", JourneyTransmitEnum.MB_INICIO_SESION);
                        if (actualizarEmail || actualizarCelular || respuestaValidaTransaccion.hayError())
                            return respuestaValidaTransaccion;
                    }
                }
            } else {

                if (esOriginacion) {
                    // no tiene que entrar por originacion y modificar mail y celu. Solo modifica
                    // direccion y celular hay un servicio que actualiza mail y celular hecho para
                    // originacion que se usa en otro lado
                    RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "originacion", JourneyTransmitEnum.MB_INICIO_SESION);
                    if (actualizarEmail || actualizarCelular || respuestaValidaTransaccion.hayError())
                        return respuestaValidaTransaccion;
                }

            }

            if (!esOriginacion) {
                RespuestaMB respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, (esCaAltaOnline ? "alta-ca-td-online" : ((habilitacionDatosPersonales) ? "validar-otp" : "datos-personales")), JourneyTransmitEnum.MB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;
            }

            if (!esMigrado && prendidoDataValidOtp && actualizarEmail
                    && !contexto.sesion().tieneOtpDatavalidValido("email", email)) {
                return RespuestaMB.estado("REQUIERE_OTP");
            }

            if (!esMigrado && prendidoDataValidOtp && actualizarCelular) {
                String telefono = celularCodigoArea + celularCaracteristica + celularNumero;
                telefono = telefono.startsWith("0") ? telefono.substring(1) : telefono;
                if (!contexto.sesion().tieneOtpDatavalidValido("sms", telefono)) {
                    return RespuestaMB.estado("REQUIERE_OTP");
                }
            }

            RespuestaMB respuestaTransmit = recomendacionTransmit(contexto, funcionalidad);
            if (respuestaTransmit.hayError())
                return respuestaTransmit;
        }

        modificacion = validarActualizarEmail(contexto, actualizarEmail, email, modificacion, emailAnterior, validarDatos,
                prendidoDataValid);
        modificacion = !modificacion.contains("ERROR")
                ? validarActualizarCelular(contexto, actualizarCelular, modificacion, emailAnterior, celularAnterior,
                validarDatos, prendidoDataValid)
                : "ERROR";
        modificacion = !modificacion.contains("ERROR")
                ? validaActualizarDomicilio(contexto, actualizarDomicilio, modificacion, emailAnterior, celularAnterior,
                validarDatos, prendidoDataValid)
                : "ERROR";
        modificacion = !modificacion.contains("ERROR") ? validardatosPersonales(contexto, habilitacionDatosPersonales,
                modificacion, emailAnterior, celularAnterior) : "ERROR";
        MBMonitoring monitoringApi = new MBMonitoring();

        if (modificacion.contains("ERROR")) {
            return RespuestaMB.estado(modificacion, contexto.csmIdAuth);
        }

        if (!esMigrado && prendidoDataValid) {
            if (validarDatos) {
                Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
                RestPersona.postDataValid(contexto, 0, secMail, 0);

                Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
                Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
                RestPersona.postDataValid(contexto, secTelDom, 0, secTel);

                Integer secDir = RestPersona.domicilioPostal(contexto, contexto.persona().cuit()).integer("idCore");
                RestPersona.postDataValid(contexto, secDir, 0, 0);

                try {
                    contexto.parametros.set("nemonico", "ACTUALIZA_DATAVALID");
                    Util.contador(contexto);
                } catch (Exception e) {
                }
            } else {
                try {
                    if (contexto.sesion().getDataValid()) {
                        contexto.parametros.set("nemonico", "MODIFICA_DATAVALID");
                        Util.contador(contexto);
                        contexto.sesion().setDataValid(false);
                    }
                } catch (Exception e) {
                }
            }
        }

        boolean actualizaOtp = false;
        if (validarDatos || actualizarEmail) {
            Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
            RestPersona.postDataValidOtp(contexto, 0, secMail, 0);
            actualizaOtp = true;
        }

        if (validarDatos || actualizarCelular) {
            Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
            Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
            RestPersona.postDataValidOtp(contexto, secTelDom, 0, secTel);
            actualizaOtp = true;
        }

        if (actualizaOtp) {
            contexto.sesion().limpiarOtpDatavalid();
            try {
                contexto.parametros.set("nemonico", "ACTUALIZA_DATAVALID_OTP");
                Util.contador(contexto);
            } catch (Exception e) {
            }
        }

        contexto.limpiarSegundoFactor();
        contexto.sesion().setValidaRiesgoNet(false);
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
            String codigoTransaccion = "YA0000";
            codigoTransaccion = (actualizarCelular) ? "YA0000"
                    : (actualizarEmail) ? "Y90000" : (actualizarDomicilio) ? "ZP0000" : codigoTransaccion;
            String celu = (actualizarCelular) ? (celularCodigoArea + celularCaracteristica + celularNumero) : null;
            String emailNuevo = (actualizarEmail) ? email : null;
            monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccion, emailNuevo, celu,
                    (actualizarCelular) ? celularAnterior : null);
        }

        RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, emailAnterior, celularAnterior);

        if (prendidoDataValid) {
            try {
                contexto.sesion().delCache("Email");
                contexto.sesion().delCache("Telefono");
                contexto.sesion().delCache("Domicilio");

                cliente(contexto);
            } catch (Exception e) {
                //
            }
        }

        return RespuestaMB.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static RespuestaMB paises(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Map<Integer, String> paises = RestCatalogo.mapaPaises(contexto);
        for (Integer id : paises.keySet()) {
            respuesta.add("paises", new Objeto().set("id", id).set("descripcion", paises.get(id)));
        }
        return respuesta;
    }

    public static RespuestaMB provincias(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        Map<Integer, String> provincias = RestCatalogo.mapaProvincias(contexto);
        for (Integer id : provincias.keySet()) {
            respuesta.add("provincias", new Objeto().set("id", id).set("descripcion", provincias.get(id)));
        }
        return respuesta;
    }

    public static RespuestaMB localidades(ContextoMB contexto) {
        Integer idProvincia = contexto.parametros.integer("idProvincia");

        if (Objeto.anyEmpty(idProvincia)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        RespuestaMB respuesta = new RespuestaMB();
        Map<Integer, String> localidades = RestCatalogo.mapaLocalidades(contexto, idProvincia);
        for (Integer id : localidades.keySet()) {
            respuesta.add("localidades", new Objeto().set("id", id).set("descripcion", localidades.get(id)));
        }
        return respuesta;
    }

    public static RespuestaMB funcionalidadesSegundoFactor(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        List<String> funcionalidades = RestPersona.funcionalidadesSegundoFactor();
        if (funcionalidades == null) {
            return RespuestaMB.error();
        }
        for (String funcionalidad : funcionalidades) {
            respuesta.add("funcionalidades", funcionalidad);
        }
        return respuesta;
    }

    public static RespuestaMB validadoresSegundoFactor(ContextoMB contexto) {
        return validadoresSegundoFactor(contexto, null);
    }

    public static RespuestaMB validadoresSegundoFactor(ContextoMB contexto, String cbuDestinoParametro) {
        String funcionalidad = contexto.parametros.string("funcionalidad");
        String cbu = contexto.parametros.string("cbu", cbuDestinoParametro);
        // cbu solo se manda en transferencias, tener en cuenta que si se manda
        // va a hacer la validación para ocultar mail por cbu

        // Sólo se usan en el caso de registrar dispositivo
        String idDispositivo = contexto.parametros.string("idDispositivo", null);

        if (Objeto.anyEmpty(funcionalidad)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        boolean forzarOcultamientoEmail = false;
        try {
            if (cbu != null) {
                if (cbu.startsWith("044")) {
                    CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu);
                    cbu = cuentaTercero.cuentaLink.string("cuenta");
                }
                Boolean ocultarPorCVU = cbu.startsWith("000") && contexto.primeraTransferencia(cbu);

                Boolean isAgendada = contexto.agendada(cbu);
                Boolean ocultarPorCBU = !cbu.startsWith("000") && (Objects.isNull(isAgendada) || !isAgendada);
                forzarOcultamientoEmail = ocultarPorCVU || ocultarPorCBU;
            }
        } catch (Exception e) {
        }

        boolean poseeCuentas = false;
        boolean poseeCuentasUnipersonales = false;
        boolean esMonoProducto = false;
        Futuro<Boolean> futuroEsClienteExterior = new Futuro<>(() -> esClienteExterior(contexto));
        Futuro<Boolean> futuroTieneTodasTarjetasVirtuales = new Futuro<>(() -> tieneTodasTarjetasVirtuales(contexto));

        try {
            if (!contexto.idCobis().isEmpty() && contexto.tarjetasDebito().isEmpty()) {
                poseeCuentas = !contexto.cuentas().isEmpty();
                poseeCuentasUnipersonales = contexto.poseeCuentasUnipersonales();
                esMonoProducto = contexto.esMonoProductoTC();
            }
        } catch (Exception e) {
        }

        boolean otpSegundoFactor = RestPersona.otpSegundoFactor(contexto.idCobis());
        boolean buhoFacilHabilitado = !MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                "prendido_dobleFactor_buhofacil") ? false : otpBuhoFacil(contexto);
        boolean smsHabilitado = otpSegundoFactor;
        boolean emailHabilitado = !forzarOcultamientoEmail && otpSegundoFactor;
        boolean linkHabilitado = contexto.tarjetaDebitoPorDefecto() != null;
        SqlResponseMB respuestaSqlSoftToken = new SqlSoftTokenService().consultarAltaSoftToken(contexto.idCobis(),
                "ACTIVO");
        boolean softTokenHabilitado = !respuestaSqlSoftToken.hayError && !respuestaSqlSoftToken.registros.isEmpty();

        Futuro<RespuestaMB> futuroUsuarioBloqueado = new Futuro<>(() -> new MBSoftToken().consultaUsuarioBloqueado(contexto));

        RespuestaMB respuesta = new RespuestaMB();

        if ("adhesion-canal".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("adhesion-pago".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_adhesion_pago")
                    && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);

        }

        if ("originacion-datos-personales".equals(funcionalidad)) {

            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms",
                    smsHabilitado || (poseeCuentas && !poseeCuentasUnipersonales));
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_originacion"))
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
        }


        if ("biometria".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);

            if (softTokenHabilitado
                    && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_biometria")
                    && tieneTarjetasDebitoHabilitadas(contexto))
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
        }

        if ("biometria-revocar".equals(funcionalidad)) {
            if (softTokenHabilitado
                    && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_biometria")
                    && tieneTarjetasDebitoHabilitadas(contexto))
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            else
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);

        }

        if ("pago-qr".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            respuesta.add("validadores", "biometria").add("validadoresUsuario", "biometria", true);
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_pago_qr")
                    && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
        }

        if ("rescate-fondos".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "rescate-fondos"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_rescate_fondos")
                        && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
        }

        if ("aumento-limite-transferencia".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                    "prendido_soft_token_aumento_limite_transferencia") && tieneTarjetasDebitoHabilitadas(contexto)
                    && softTokenHabilitado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);

        }

        if ("cambio-clave-canal".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("datos-personales".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "datos-personales"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_datos_personales")
                        && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
        }

        if ("debin".equals(funcionalidad) || "orden-extraccion".equals(funcionalidad)) {

            if (TransmitMB.isChallengeOtp(contexto, "debin")) {
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            } else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_debin")
                        && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
        }

        if ("adelanto".equals(funcionalidad)) {

            if (TransmitMB.isChallengeOtp(contexto, "adelanto"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_adelanto")
                        && tieneTarjetasDebitoHabilitadas(contexto) && contexto.cuentaUnipersonalCAPesos() != null
                        && softTokenHabilitado)
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            }
        }

        // echeq
        if ("echeq".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);

            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_echeq")
                    && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
        }

        if ("prestamo-personal".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "prestamo-personal"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_prestamo_personal")
                        && tieneTarjetasDebitoHabilitadas(contexto) && contexto.cuentaUnipersonalCAPesos() != null
                        && softTokenHabilitado) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
                }
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            }

        }

        if ("prestamo-personal".equals(funcionalidad)) {
            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_prestamo_personal")
                    && tieneTarjetasDebitoHabilitadas(contexto) && contexto.cuentaUnipersonalCAPesos() != null
                    && softTokenHabilitado) {
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
        }

        if ("operar-preguntas-seguridad".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("operar-tco".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "preguntas-personales").add("validadores", "preguntas-personales",
                    RestPersona.preguntasPersonalesCargadas(contexto));
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("originacion".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "originacion"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms",
                        smsHabilitado || esMonoProducto || (poseeCuentas && !poseeCuentasUnipersonales));
        }

        if ("transferencia".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "transferencia"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "buhoFacil").add("validadoresUsuario", "buhoFacil", buhoFacilHabilitado);
                respuesta.add("validadores", "biometria").add("validadoresUsuario", "biometria", true);
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                try {
                    if (RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", contexto.idCobis()))
                        respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
                } catch (Exception e) {
                }
            }
        }

        if ("validar-otp".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "validar-otp"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
        }

        if ("registrar-dispositivo".equals(funcionalidad)) {
            if (Objeto.anyEmpty(idDispositivo))
                return RespuestaMB.parametrosIncorrectos();

            if (futuroEsClienteExterior.tryGet())
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                String numeroCelular = "";
                Objeto celular = RestPersona.celular(contexto, contexto.persona().cuit());
                if (celular != null)
                    numeroCelular = celular.string("codigoArea").concat(celular.string("caracteristica"))
                            .concat(celular.string("numero"));
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", StringUtils.isNotBlank(numeroCelular));

                boolean modificoDatos = modificoDatos(contexto);
                if (modificoDatos && !validacionesRegistrarSms(contexto, esMonoProducto, modificoDatos))
                    respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
                else
                    respuesta.add("validadores", "email").add("validadoresUsuario", "email",
                            StringUtils.isNotBlank(RestPersona.direccionEmail(contexto, contexto.persona().cuit())));
            }
        }

        if ("alta-soft-token".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email",
                    StringUtils.isNotBlank(RestPersona.direccionEmail(contexto, contexto.persona().cuit())));
        }

        if ("alta-ca-td-online".equals(funcionalidad)) {
            if (TransmitMB.isChallengeOtp(contexto, "alta-ca-td-online"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
        }

        if ("venta-dolares".equals(funcionalidad) && TransmitMB.isChallengeOtp(contexto, "venta-dolares"))
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);

        if ("venta-acciones-bonos".equals(funcionalidad) && TransmitMB.isChallengeOtp(contexto, "venta-acciones-bonos"))
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);

        if ("migracion-transmit".equals(funcionalidad)) {
            if (esClienteExterior(contexto))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
                if (!esMonoProducto) {
                    if (futuroTieneTodasTarjetasVirtuales.tryGet())
                        respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
                    else if (softTokenHabilitado && !usuarioBloqueadoSoftToken(futuroUsuarioBloqueado.tryGet()))
                        respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
                }
            }
        }

        HashMap<String, Boolean> habilitadores = new HashMap<>();
        habilitadores.put("otpSegundoFactor", otpSegundoFactor);
        habilitadores.put("smsHabilitado", smsHabilitado);
        habilitadores.put("emailHabilitado", emailHabilitado);
        habilitadores.put("linkHabilitado", linkHabilitado);

        return respuesta;
    }

    public static Boolean otpBuhoFacil(ContextoMB contexto) {
        if ((contexto.sesion().cache("intentosBuhoFacil") == null
                || Integer.parseInt(contexto.sesion().cache("intentosBuhoFacil")) < 2)
                && contexto.sesion().cache("dispositivo") != null) {
            return MBBiometria.verificaAccesos(contexto).string("buhoFacilActivo").equalsIgnoreCase("1");
        }
        return false;
    }

    public static RespuestaMB marcasCliente(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("esMonoProductoTC", contexto.esMonoProductoTC());
        return respuesta;
    }

    // todo-emm: esto no se usa, debería eliminarse, directamente se hace el insert
// en datos personales
// ya que si lo hacemos en dos llamados separados, tenemos que pedir dos veces
// por separado la tco.
// Algo que no se hace desde el front.
    public static RespuestaMB permitirSegundoFactorOtp(ContextoMB contexto) {
        Boolean acepto = contexto.parametros.bool("acepto");

        if (Objeto.anyEmpty(acepto)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        if (acepto) {
            if (!contexto.validaSegundoFactor("datos-personales")) {
                return RespuestaMB.estado("REQUIERE_SEGUNDO_FACTOR");
            }
        }

        SqlResponseMB sqlResponse = RestPersona.permitirSegundoFactorOtp(contexto.idCobis(), acepto);
        if (sqlResponse.hayError) {
            return RespuestaMB.error();
        }
        contexto.limpiarSegundoFactor();
        return RespuestaMB.exito();
    }

    public static RespuestaMB buscarPersona(ContextoMB contexto) {
        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");
        String numeroDocumento = contexto.parametros.string("numeroDocumento");

        if (Objeto.anyEmpty(numeroDocumento)) {
            return RespuestaMB.parametrosIncorrectos();
        }

        ApiRequestMB request = ApiMB.request("ConsultaPersonas", "personas", "GET", "/cuils", contexto);
        request.query("dni", numeroDocumento);

        ApiResponseMB response = ApiMB.response(request, numeroDocumento);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        List<Objeto> lista = new ArrayList<>();
        for (Objeto item : response.objetos()) {
            if (!nombre.isEmpty() && !item.string("apellidoYNombre").toUpperCase().contains(nombre.toUpperCase())) {
                continue;
            }
            if (!apellido.isEmpty() && !item.string("apellidoYNombre").toUpperCase().contains(apellido.toUpperCase())) {
                continue;
            }
            lista.add(item);
        }

        if (lista.isEmpty()) {
            return RespuestaMB.estado("PERSONA_NO_ENCONTRADA");
        }
        if (lista.size() > 1) {
            return RespuestaMB.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }

        String nombreCompleto = lista.get(0).string("apellidoYNombre");
        String cuil = lista.get(0).string("cuil");

        ApiRequestMB requestPersona = ApiMB.request("ConsultaPersonasPorCuit", "personas", "GET", "/personas/{cuit}",
                contexto);
        requestPersona.path("cuit", cuil);

        ApiResponseMB responsePersona = ApiMB.response(requestPersona, cuil);

        Objeto datosPersonales = new Objeto();
        datosPersonales.set("nombreCompleto", nombreCompleto);
        datosPersonales.set("cuil", cuil);
        datosPersonales.set("fechaNacimiento", responsePersona.date("fechaNacimiento", "yyyy-MM-dd", "dd/MM/yyyy"));
        datosPersonales.set("edad", responsePersona.integer("edad"));

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("datosPersonales", datosPersonales);
        return respuesta;
    }

    public static RespuestaMB domicilioTarjetaCredito(ContextoMB contexto) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return RespuestaMB.estado("SIN_TARJETA_CREDITO_TITULAR");
        }

        ApiRequestMB request = ApiMB.request("DireccionTarjetaCredito", "productos", "GET",
                "/v1/productos/{id}/domicilios", contexto);
        request.path("id", tarjetaCredito.numero());
        request.query("idCliente", contexto.idCobis());
        request.query("tipoProducto", "ATCDOMIC");

        ApiResponseMB response = ApiMB.response(request, "80", contexto.idCobis(), tarjetaCredito.numero());
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        String ciudad = "";
        try {
            ApiRequestMB requestDomicilio = ApiMB.request("Domicilio", "personas", "GET", "/personas/{id}/domicilios",
                    contexto);
            requestDomicilio.header("x-usuario", ConfigMB.string("configuracion_usuario"));
            requestDomicilio.path("id", contexto.persona().cuit());
            requestDomicilio.permitirSinLogin = true;
            requestDomicilio.cacheSesion = true;
            ApiResponseMB responseDomicilio = ApiMB.response(requestDomicilio, contexto.idCobis());

            if (!responseDomicilio.hayError()) {
                for (Objeto item : responseDomicilio.objetos()) {
                    String codigoPostalTarjeta = response.objetos().get(0).string("codigoPostal");
                    String codigoPostalPersona = item.string("idCodigoPostal");
                    if (codigoPostalTarjeta.equals(codigoPostalPersona)) {
                        ciudad = item.string("ciudad");
                    }
                }
            }
        } catch (Exception e) {
        }

        Objeto domicilio = new Objeto();
        domicilio.set("calle", response.objetos().get(0).string("direccion"));
        domicilio.set("altura", response.objetos().get(0).string("numero"));
        domicilio.set("piso", response.objetos().get(0).string("piso"));
        domicilio.set("departamento", response.objetos().get(0).string("departamento"));
        domicilio.set("codigoPostal", response.objetos().get(0).string("codigoPostal"));
        domicilio.set("ciudad", ciudad);

        return RespuestaMB.exito("domicilio", domicilio);
    }

    public static RespuestaMB consultaValidacionCensoNacional(ContextoMB contexto) {
        if (!"11".equals(contexto.persona().tipoTributario())) {
            return RespuestaMB.exito().set("valida", true);
        }

        ApiResponseMB response = RestPersona.consultaCensoNacional(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }

        if (!response.bool("censado")) {
            return RespuestaMB.estado("SIN_CENSAR");
        }

        RespuestaMB respuesta = new RespuestaMB();
        respuesta.set("valida", response.bool("censado"));
        respuesta.set("censado", response.bool("censado"));
        respuesta.set("errorReferenciaCore", !response.string("errorWoci").equals(""));

        return respuesta;

    }

    public static RespuestaMB accountOfficer(ContextoMB contexto) {
        ApiResponseMB response = RestPersona.segmentacion(contexto);
        if (response.hayError()) {
            return RespuestaMB.error();
        }
        RespuestaMB respuesta = new RespuestaMB();
        Objeto accountOfficer = new Objeto();
        if (Objects.nonNull(response) && Objects.nonNull(response.objetos()) && !response.objetos().isEmpty()) {
            if (Objects.nonNull(response.objetos().get(0))) {
                Objeto datos = response.objetos().get(0);
                if (Objects.nonNull(datos) && Objects.nonNull(datos.string("idSegmentoRenta"))
                        && "1".equals(datos.string("idSegmentoRenta"))
                        && Objects.nonNull(datos.string("idOficialGestionPaquete"))
                        && !datos.string("idOficialGestionPaquete").isEmpty()) {
                    accountOfficer.set("showEmail", true);
                    accountOfficer.set("oficialAsignadoMail", datos.string("idOficialGestionPaquete") + "@bh.com.ar");

                } else {
                    accountOfficer.set("showEmail", false);
                    accountOfficer.set("oficialAsignadoMail", ConfigMB.string("account_officer_defaultMail"));
                }
                accountOfficer.set("imageURL",
                        ConfigMB.string("account_officer_url_img") + respuesta.string("idOficialGestionPaquete"));
                accountOfficer.set("id", datos.string("Id"));
                accountOfficer.set("OficialAsignado", datos.string("OficialAsignado"));
            }
            respuesta.set("estado", 0);
        }
        respuesta.set("accountOfficer", accountOfficer);
        return respuesta;
    }

    public static RespuestaMB usageData(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        try {
            if (contexto.persona().nombreCompleto().isEmpty()) {
                return RespuestaMB.parametrosIncorrectos();
            }
            Objeto usuario = new Objeto();
            Date fecha = new Date();
            usuario.set("fechaCreacion", fecha);
            usuario.set("fechaUltLogin", fecha);
            Objeto data = contexto.parametros.objetos().get(0);
            if (Objects.nonNull(data)) {
                if (data.get("type") != null) {
                    String email = contexto.persona().email();
                    String NombreUsuario = contexto.persona().nombreCompleto();
                    usuario.set("token", data.get("token"));
                    usuario.set("email", email);
                    usuario.set("nombreUsuario", NombreUsuario);
                    usuario.set("idCobis", contexto.idCobis());
                    usuario.set("dispositivo", (String) data.get("device.model"));
                    usuario.set("versionOS", (String) data.get("versionOS"));
                    usuario.set("versionApp", (String) data.get("versionApp"));
                    usuario.set("ubiLatitud", (String) data.get("latitud"));
                    usuario.set("ubiLongitud", (String) data.get("longitud"));
                } else {
                    usuario.set("dispositivo", (String) data.get("device.model"));
                    usuario.set("ubiLatitud", (String) data.get("latitud"));
                    usuario.set("ubiLongitud", (String) data.get("longitud"));
                    usuario.set("idCobis", contexto.idCobis());
                }
                if (!SqlMobile.persistirUsuario(usuario)) {
                    return RespuestaMB.estado("ERROR_AL_GRABAR_USUARIO");
                } else {
                    respuesta.set("estado", 0);
                }
            } else {
                return RespuestaMB.parametrosIncorrectos();
            }

        } catch (Exception exception) {
            return RespuestaMB.error();
        }
        return respuesta;
    }

    private static String validardatosPersonales(ContextoMB contexto, Boolean habilitacionDatosPersonales,
                                                 String modificacion, String mailAnterior, String celularAnterior) {
        if (habilitacionDatosPersonales) { // emm-->si viene en falso NO TIENE QUE LLAMAR AL PERMITIR ya que en ese caso
            // no
            // se modifica el valor en la tabla
            // sólo lo vamos a modificar si viene true
            Integer valorOtpSegundoFactor = RestPersona.sugerirOtpSegundoFactor(contexto.idCobis());
            boolean enviarMailOtp = false;
            if (valorOtpSegundoFactor != null && valorOtpSegundoFactor != 1) {
                enviarMailOtp = true;
            }
            SqlResponseMB sqlResponse = RestPersona.permitirSegundoFactorOtp(contexto.idCobis(), true);
            if (sqlResponse.hayError) {
                RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior,
                        celularAnterior);
                return "ERROR_HABILITACION_OTP";
            }
            if (enviarMailOtp) {
                RestPersona.enviarMailActivacionOtp(contexto);
            }
        }
        return modificacion;
    }

    private static String validarActualizarEmail(ContextoMB contexto, Boolean actualizarEmail, String email,
                                                 String modificacion, String emailAnterior, Boolean validarDatos, Boolean prendidoDataValid) {
        if (actualizarEmail && !validarDatos) {
            String modificacionMailAux = RestPersona.compararMailActualizado(contexto, email);

            ApiResponseMB response = RestPersona.actualizarEmail(contexto, contexto.persona().cuit(), email);
            if (response.hayError()) {
                return "ERROR";
            }

            if (prendidoDataValid) {
                Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
                RestPersona.postDataValid(contexto, 0, secMail, 0);
            }

            if (!"".equals(modificacionMailAux)) {
                contexto.insertarLogCambioMail(contexto, emailAnterior, email);
            }

            return modificacion += modificacionMailAux;
        }

        return modificacion;
    }

    private static String validarActualizarCelular(ContextoMB contexto, Boolean actualizarCelular, String modificacion,
                                                   String emailAnterior, String celularAnterior, Boolean validarDatos, Boolean prendidoDataValid) {
        String celularCodigoArea = contexto.parametros.string("celular.codigoArea");
        String celularCaracteristica = contexto.parametros.string("celular.caracteristica");
        String celularNumero = contexto.parametros.string("celular.numero");
        if (actualizarCelular && !validarDatos) {
            String modificacionCelularAux = RestPersona.compararCelularActualizado(contexto, celularCodigoArea,
                    celularCaracteristica, celularNumero);

            ApiResponseMB response = RestPersona.actualizarCelular(contexto, contexto.persona().cuit(),
                    celularCodigoArea, celularCaracteristica, celularNumero);
            if (response.hayError()) {
                RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, emailAnterior,
                        celularAnterior);
                return "ERROR";
            }

            if (prendidoDataValid) {
                Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
                Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
                RestPersona.postDataValid(contexto, secTelDom, 0, secTel);
            }

            if (!"".equals(modificacionCelularAux)) {
                contexto.insertarLogCambioCelular(contexto, celularAnterior,
                        celularCodigoArea + celularCaracteristica + celularNumero);
                modificacion = ("".equals(modificacion) ? modificacionCelularAux
                        : modificacion + ", " + modificacionCelularAux);
            }
            return modificacion;
        }

        return modificacion;
    }

    private static String validaActualizarDomicilio(ContextoMB contexto, Boolean actualizarDomicilio,
                                                    String modificacion, String emailAnterior, String celularAnterior, Boolean validarDatos,
                                                    Boolean prendidoDataValid) {
        String calle = contexto.parametros.string("domicilio.calle");
        String altura = contexto.parametros.string("domicilio.altura");
        String piso = contexto.parametros.string("domicilio.piso");
        String departamento = contexto.parametros.string("domicilio.departamento");
        String idProvincia = contexto.parametros.string("domicilio.idProvincia");
        String idLocalidad = contexto.parametros.string("domicilio.idLocalidad");
        String codigoPostal = contexto.parametros.string("domicilio.codigoPostal");
        String entreCalle1 = contexto.parametros.string("domicilio.entreCalle1");
        String entreCalle2 = contexto.parametros.string("domicilio.entreCalle2");

        if (actualizarDomicilio) {
            Objeto domicilio = new Objeto();
            domicilio.set("calle", calle);
            domicilio.set("numero", altura);
            domicilio.set("piso", piso);
            domicilio.set("departamento", departamento);
            domicilio.set("idProvincia", idProvincia);
            domicilio.set("idCiudad", idLocalidad);
            domicilio.set("idCodigoPostal", codigoPostal);
            domicilio.set("calleEntre1", entreCalle1);
            domicilio.set("calleEntre2", entreCalle2);

            Objeto domicilioAnterior = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());

            String modificacionDireccionAux = RestPersona.compararDomicilioActualizado(contexto, calle, altura, piso,
                    departamento, idProvincia, idLocalidad, codigoPostal);
            String salida = OriginacionHelper.actualizarDomicilio(contexto, domicilio, modificacion, emailAnterior,
                    celularAnterior, "DP");
            if (!"".equals(salida)) {
                if (salida.equalsIgnoreCase("CAMBIO_PENDIENTE")) {
                    try {
                        Objeto domicilioAnt = new Objeto();
                        domicilioAnt.set("calle", domicilioAnterior.string("calle"));
                        domicilioAnt.set("numero", domicilioAnterior.string("numero"));
                        domicilioAnt.set("piso", domicilioAnterior.string("piso"));
                        domicilioAnt.set("departamento", domicilioAnterior.string("departamento"));
                        domicilioAnt.set("idProvincia", domicilioAnterior.string("idProvincia"));
                        domicilioAnt.set("idCiudad", domicilioAnterior.string("idCiudad"));
                        domicilioAnt.set("idCodigoPostal", domicilioAnterior.string("idCodigoPostal"));
                        domicilioAnt.set("calleEntre1", domicilioAnterior.string("calleEntre1"));
                        domicilioAnt.set("calleEntre2", domicilioAnterior.string("calleEntre2"));

                        RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilioAnt, "DP");
                    } catch (Exception e) {
                    }
                }

                return salida;
            }

            if (prendidoDataValid) {
                Integer secDir = RestPersona.domicilioPostal(contexto, contexto.persona().cuit()).integer("idCore");
                RestPersona.postDataValid(contexto, secDir, 0, 0);
            }

            if (!"".equals(modificacionDireccionAux)) {
                modificacion = ("".equals(modificacion) ? modificacionDireccionAux
                        : modificacion + ", " + modificacionDireccionAux);
            }
            return modificacion;
        }

        return modificacion;
    }

    public static RespuestaMB dataValid(ContextoMB contexto) {
        try {

            if (contexto == null || contexto.idCobis() == null) {
                return RespuestaMB.estado("SIN_SESION");
            }

            RespuestaMB respuesta = new RespuestaMB();

            if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid")) {
                contexto.sesion().setDataValid(false);
                Calendar fechaActual = Calendar.getInstance();
                fechaActual.setTime(new Date());
                Date dFechaActual = fechaActual.getTime();
                ApiResponseMB dataValid = RestPersona.getDataValid(contexto);

                for (Objeto entidad : dataValid.objetos()) {
                    if (!entidad.bool("validado") || entidad.get("fhVto") == null
                            || dFechaActual.compareTo(entidad.date("fhVto", "yyyy-MM-dd")) >= 0) {
                        respuesta.set((String) entidad.get("entidad"), "VENCIDO");
                        respuesta.setEstado("VENCIDO");
                    }
                }
                try {
                    if (respuesta.string("estado").equals("VENCIDO")) {
                        contexto.parametros.set("nemonico", "ALERTA_DATAVALID");
                        Util.contador(contexto);
                        contexto.sesion().setDataValid(true);
                    }
                } catch (Exception e) {
                    //
                }
            }
            return respuesta;
        } catch (Exception e) {
            return RespuestaMB.estado("ERROR_DATA_VALID");
        }
    }

    private static boolean tieneTarjetasDebitoHabilitadas(ContextoMB contexto) {

        contexto.parametros.set("traerTarjetasVirtuales", true);

        RespuestaMB respuestaTarjetas = MBTarjetas.tarjetaDebitoHabilitadaRedLink(contexto);
        return !respuestaTarjetas.hayError() && respuestaTarjetas.get("tarjetasDebito") != null;
    }

    public static RespuestaMB situacionLaboral(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        SituacionLaboral actividadLaboralActual = SituacionLaboral.situacionLaboralPrincipal(contexto);

        respuesta.set("ingresos", new Objeto().set("ingresoMinimo", ConfigMB.bigDecimal("monto_minimo_carga_ingresos"))
                .set("ingresoMinimoFormateado", Formateador.importe(ConfigMB.bigDecimal("monto_minimo_carga_ingresos")))
                .set("ingresoMaximo", ConfigMB.bigDecimal("monto_maximo_carga_ingresos")).set("ingresoMaximoFormateado",
                        Formateador.importe(ConfigMB.bigDecimal("monto_maximo_carga_ingresos"))));

        Objeto listaActividadesLaborales = MBCatalogo.situacionesLaborales(contexto);

        for (Objeto actividad : listaActividadesLaborales.objetos("situaciones")) {
            Objeto objeto = actividad;
            if (actividad.string("id").equals(actividadLaboralActual.idSituacionLaboral)) {
                objeto.set("actividadPrincipal", true);
            }
            respuesta.add("situaciones", objeto);
        }

        return respuesta;
    }

    public static Boolean tieneOpcionAdelanto(ContextoMB contexto) {

        if (ConfigMB.bool("prendido_adelanto_bh") && !ContextoMB.cambioDetectadoParaNormativoPP(contexto, false)
                && !contexto.tienePPProcrearDesembolsado()) {
            Boolean acreditacionesActivas = MBProducto.acreditacionesHaberes(contexto);
            Boolean preAprobadoAdelanto = MBPrestamo.ofertaPreAprobada(contexto).existe("adelantoBH");

            // Adelanto BH 1 a 1.
            if (!contexto.persona().esEmpleado() && contexto.esPlanSueldo() && acreditacionesActivas) {
                return true;
            }

            // Adelanto BH para preaprobados con planSueldo
            if (preAprobadoAdelanto && contexto.esPlanSueldo()) {
                return true;
            }

            // Adelanto BH para preaprobados tipo jubilados
            if (ConfigMB.bool("prendido_adelanto_jubilados", false) && preAprobadoAdelanto && contexto.esJubilado()
                    && contexto.tieneCuentaCategoriaB()) { // si es jubilado,
                // pero no tiene
                // cuenta con
                // categoria b, no
                // puede tomar el
                // preaprobado
                return true;
            }
        }

        return false;
    }

    public static Boolean validarTarjetaVirtual(ContextoMB contexto) {
        for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
            if (tarjetaDebito.virtual())
                return true;
        }
        return false;
    }

    public static RespuestaMB validarIdDispositivo(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();

        try {
            Boolean habilitadoCobis = false;
            if (ConfigMB.string("mb_prendido_scandni_TDV_cobis") != null) {
                Set<String> cobisHabilitado = Objeto.setOf(ConfigMB.string("mb_prendido_scandni_TDV_cobis").split("_"));
                habilitadoCobis = cobisHabilitado.contains(contexto.idCobis()) ? true : false;
            }
            if ((!ConfigMB.bool("mb_prendido_scandni_TDV", true)) || (!ConfigMB.esProduccion() && habilitadoCobis)) {
                return RespuestaMB.estado("CONSOLIDADA");
            }

            // se valida iddispositivo que no venga vacio
            String idDispositivo = contexto.parametros.string("idDispositivo", "");

            // validacion si tiene tarjeta virtual
            if (validarTarjetaVirtual(contexto)) {
                String cuitPersona = contexto.persona().cuit();

                Boolean primeraVez = false;
                // consulta si existen registros de onboarding en la tabla interna(esto se
                // ejecuta una sola vez por cliente)
                if (SqlRenaper.consultaTablaInterna(cuitPersona).registros.isEmpty()) {

                    // obtiene los registros de onboarding
                    SqlResponseMB renaperResponse = consultaRenaperOnboarding(cuitPersona);

                    if (!renaperResponse.registros.isEmpty()) {
                        // insercion del primer registro de onboarding.
                        SqlRenaper.insertTablaInternaPrimerRegistro(renaperResponse);

                        // insercion de registros en tabla interna con el nuevo dispositivo
                        SqlRenaper.InsertTablaInterna(renaperResponse, "INICIADO", idDispositivo);
                        primeraVez = true;
                        if (idDispositivo.isEmpty()) {
                            // queda a decision del PO posible resolucion seria scan dni.
                            return respuesta.setEstado("ID_DISPOSITIVO_VACIO");
                        }

                    } else {
                        return respuesta.setEstado("CONSOLIDADA");
                    }
                }
                // llama al ultimo id de dispositivo registrado usado por el cliente
                SqlResponseMB consultaTablaInterna = SqlRenaper.consultaTablaInterna(idDispositivo, cuitPersona);
                // Obtengo el primer registro que se obtuvo de renaper.
                SqlResponseMB consultTablaInternaDispositivoRenaper = SqlRenaper.consultaTablaInterna(cuitPersona);

                if (!consultaTablaInterna.registros.isEmpty() && !consultaTablaInterna.hayError) {
                    String estado = consultaTablaInterna.registros.get(0).string("estado");
                    String idDispositivoSql = consultaTablaInterna.registros.get(0).string("id_dispositivo");
                    String idDispositivoRenaper = "";
                    if (primeraVez) {
                        idDispositivoRenaper = consultTablaInternaDispositivoRenaper.registros.get(0)
                                .string("id_dispositivo");
                    } else {
                        idDispositivoRenaper = idDispositivo;
                    }

                    if (estado.equalsIgnoreCase("VALIDAR_DNI_SUCURSAL") && idDispositivoSql.equals(idDispositivo)) {
                        return RespuestaMB.estado("VALIDAR_DNI_SUCURSAL");
                    }

                    if (estado.equalsIgnoreCase("PENDIENTE")) {
                        if (idDispositivoSql.equals(idDispositivoRenaper)) {
                            return RespuestaMB.estado("SCAN_DNI");
                        } else {
                            SqlRenaper.insertTablaInterna(consultaTablaInterna, idDispositivo);
                            SqlRenaper.insertTablaInternaPendiente(consultaTablaInterna, idDispositivo);

                            return RespuestaMB.estado("SCAN_DNI");
                        }
                    } else {
                        if (estado.equalsIgnoreCase("INICIADO") && idDispositivoSql.equals(idDispositivoRenaper)) {
                            // que estado va aca despues de devolver consolidada cuando dispositivo es ok
                            String validacion = "VALIDADO";
                            Integer id = consultaTablaInternaRenaper(cuitPersona).registros.get(0).integer("id");
                            SqlRenaper.updateTablaInterna(id, validacion);
                            SqlRenaper.updateTablaInternaIdDispositivo(id, idDispositivoSql);
                            return respuesta.setEstado("CONSOLIDADA");
                        } else {

                            if ((estado.equalsIgnoreCase("VALIDADO") || estado.equalsIgnoreCase("FINALIZADO"))
                                    && idDispositivoSql.equals(idDispositivo)) {
                                return respuesta.setEstado("CONSOLIDADA");
                            } else {
                                SqlRenaper.InsertarTablaInternaRenaper(consultaTablaInterna, "PENDIENTE",
                                        idDispositivo);
                                return respuesta.setEstado("SCAN_DNI");
                            }
                        }
                    }

                }

                SqlResponseMB consultaTablaInternaDatosOkRenaper = SqlRenaper
                        .consultaTablaInternaDatosExistentes(idDispositivo, cuitPersona);
                SqlResponseMB renaperResponse = consultaRenaperOnboarding(cuitPersona);
                if (consultaTablaInternaDatosOkRenaper.registros.isEmpty()
                        || consultaTablaInternaDatosOkRenaper.hayError) {

                    SqlRenaper.InsertTablaInterna(renaperResponse, "INICIADO", idDispositivo);
                    SqlRenaper.insertTablaInternaPendiente(renaperResponse, idDispositivo);
                    return respuesta.setEstado("SCAN_DNI");
                }

                if (!renaperResponse.hayError && !renaperResponse.registros.isEmpty()) {
                    // compruebo los datos por si el cliente no valido nunca
                    String estado = consultaTablaInternaDatosOkRenaper.registros.get(0).string("estado");

                    String idDispositivoSql = consultaTablaInternaDatosOkRenaper.registros.get(0)
                            .string("id_dispositivo");
                    String idDispositivoRenaper = consultTablaInternaDispositivoRenaper.registros.get(0)
                            .string("id_dispositivo");

                    // CUANDO EL CLIENTE INGRESA CON EL MISMO DISPOSITIVO Y TIENE EL ESTADO VALIDAR
                    // DNI SUCURSAL
                    // DEBE IR A VALIDAR A LA SUCURSAL.
                    if (estado.equalsIgnoreCase("VALIDAR_DNI_SUCURSAL") && idDispositivoSql.equals(idDispositivo)) {
                        return RespuestaMB.estado("VALIDAR_DNI_SUCURSAL");
                    }

                    // SI EL CLIENTE ENTRA CON OTRO DISPOSITIVO AL ANTERIOR
                    // SE ESTADO PENDIENTE Y RETORNA SCAN DNI.
                    if (!idDispositivoSql.equals(idDispositivo)) {
                        SqlRenaper.InsertarTablaInterna(renaperResponse, "INICIADO", idDispositivo);
                        SqlRenaper.InsertarTablaInterna(renaperResponse, "PENDIENTE", idDispositivo);
                        return respuesta.setEstado("SCAN_DNI");
                    }
                    if (estado.equalsIgnoreCase("INICIADO") && idDispositivoSql.equals(idDispositivoRenaper)) {
                        String validacion = "VALIDADO";
                        Integer id = consultaTablaInternaRenaper(cuitPersona).registros.get(0).integer("id");
                        SqlRenaper.updateTablaInterna(id, validacion);
                        SqlRenaper.updateTablaInternaIdDispositivo(id, idDispositivoSql);
                        return respuesta.setEstado("CONSOLIDADA");
                    }

                    if (idDispositivo.equals(idDispositivoSql)) {
                        String validacion = "Finalizado";
                        Integer id = consultaTablaInternaRenaper(cuitPersona).registros.get(0).integer("id");
                        SqlRenaper.updateTablaInterna(id, validacion);
                        return respuesta.setEstado("CONSOLIDADA");
                    } else {
                        // comprueba si el dispositivo esta vacio cuando la tabla tiene datos.
                        if (idDispositivo.isEmpty()) {
                            // queda a decision del PO posible resolucion seria scan dni.
                            // se realiza control y se inserta registro cuando el id esta vacio.
                            SqlRenaper.InsertarTablaInterna(renaperResponse, "PENDIENTE", idDispositivo);
                            return respuesta.setEstado("ID_DISPOSITIVO_VACIO");
                        } else {
                            String validacion = "PENDIENTE";
                            Integer id = consultaTablaInternaRenaper(cuitPersona).registros.get(0).integer("id");
                            SqlRenaper.updateTablaInterna(id, validacion);
                            return respuesta.setEstado("SCAN_DNI");
                        }
                    }
                }
            } else {
                return respuesta.setEstado("CONSOLIDADA");
            }
        } catch (Exception e) {
            return respuesta.setEstado("ERROR_NO_TIPIFICADO");
        }
        return respuesta.setEstado("ERROR_NO_TIPIFICADO");

    }

    public static RespuestaMB validarDatosRenaper(ContextoMB contexto) {
        // definicion de parametros
        String scanner = contexto.parametros.string("scanner");
        RespuestaMB respuesta = new RespuestaMB();

        Boolean habilitadoCobis = false;
        if (ConfigMB.string("mb_prendido_scandni_TDV_cobis") != null) {
            Set<String> cobisHabilitado = Objeto.setOf(ConfigMB.string("mb_prendido_scandni_TDV_cobis").split("_"));
            habilitadoCobis = cobisHabilitado.contains(contexto.idCobis()) ? true : false;
        }
        if ((!ConfigMB.bool("mb_prendido_scandni_TDV")) || (!ConfigMB.esProduccion() && habilitadoCobis)) {
            return RespuestaMB.estado("CONSOLIDADA");
        }

        try {
            if (contexto.sesion().getCuentaIntentosScanDni() == null) {
                contexto.sesion().setCuentaIntentosScanDni(0);
            }
            if (contexto.sesion().getCuentaIntentosPersonaNoEncontrada() == null) {
                contexto.sesion().setCuentaIntentosPersonaNoEncontrada(0);
            }
            if (contexto.sesion().getCuentaIntentosScanDni() >= 3) {
                return respuesta.setEstado("MAXIMOS_REINTENTOS");
            }
            String cuitPersona = contexto.persona().cuit();
            // ApiResponseMB responseRenaperViviendas = llamadaRenaperViviendas(scanner,
            // contexto);
            // 400 =detalle mensaje La solicitud contiene sintaxis errónea.
            // 404 = Recurso no encontrado

            ApiResponseMB responseRenaperViviendas = validarRenaper(contexto, obtenerNumeroTramiteScanner(scanner),
                    obtenerDniScanner(scanner), obtenerSexoScanner(scanner));
            if (responseRenaperViviendas.codigo == 400) {
                return respuesta.setEstado("PERSONA_NO_ENCONTRADA");
            }
            if (responseRenaperViviendas.codigo == 404 || responseRenaperViviendas.codigo == 500) {
                return respuesta.setEstado("ERROR_REINTENTAR");
            }

            if (responseRenaperViviendas == null || responseRenaperViviendas.hayError()) {
                contexto.sesion().setCuentaIntentosScanDni(contexto.sesion().getCuentaIntentosScanDni() + 1);
                respuesta.setEstado("ERROR_REINTENTAR");
//				Integer count = contexto.sesion().getCuentaIntentosScanDni();

                return respuesta;
            }
            SqlResponseMB responseRenaperOnboarding = consultaTablaInternaRenaperValidado(cuitPersona);

            SqlResponseMB ultimodispositivoPendiente = ultimoIdDispositivoPendiente(cuitPersona);
            // consulto cuando no existen registros previos y si no estoy validado.

            if (responseRenaperOnboarding.registros.isEmpty()) {
                return respuesta.setEstado("PERSONA_NO_ENCONTRADA");
            }
            String idDispositivoPendiente = ultimodispositivoPendiente.registros.get(0).string("id_dispositivo");
//			String idDispositivoSql = responseRenaperOnboarding.registros.get(0).string("id_dispositivo");
            String fecha_emision = responseRenaperOnboarding.registros.get(0).string("fecha_emision");
            String vencimiento = responseRenaperOnboarding.registros.get(0).string("vencimiento");

            if (fecha_emision.equals(vencimiento)) {
                return respuesta.setEstado("DATOS_INCORRECTOS");
            } else {
                // insercion de registros en tabla interna del llamado a renaper viviendas.
                if (!SqlRenaper.validaDiferenciaDatosCuil(responseRenaperOnboarding, responseRenaperViviendas)) {
                    if (!SqlRenaper.validaDiferenciaDatos(responseRenaperOnboarding, responseRenaperViviendas)) {

//
                        SqlRenaper.InsertTablaInterna(responseRenaperViviendas, "FINALIZADO", idDispositivoPendiente);
                        return respuesta.setEstado("CONSOLIDADA");
                    } else {

                        SqlRenaper.InsertTablaInterna(responseRenaperViviendas, "VALIDAR_DNI_SUCURSAL",
                                idDispositivoPendiente);
                        return RespuestaMB.estado("VALIDAR_DNI_SUCURSAL");
                    }
                } else {
                    contexto.sesion().setCuentaIntentosPersonaNoEncontrada(
                            contexto.sesion().getCuentaIntentosPersonaNoEncontrada() + 1);

                    if (contexto.sesion().getCuentaIntentosPersonaNoEncontrada() >= 3
                            && contexto.sesion().getCuentaIntentosPersonaNoEncontrada() <= 4) {
                        SqlRenaper.InsertarTablaInterna(ultimodispositivoPendiente, "VALIDAR_DNI_SUCURSAL",
                                idDispositivoPendiente);
                        return respuesta.setEstado("VALIDAR_DNI_SUCURSAL");
                    }

                    return respuesta.setEstado("DOCUMENTO_DIFERENTE");

                }

            }
        } catch (Exception e) {
            return respuesta.setEstado("ERROR_NO_TIPIFICADO");
        }
    }

//	public static ApiResponseMB llamadaRenaperViviendas(String scanner, ContextoMB contexto) {
//		String numeroTramite = "";
//		String numeroDocumento = "";
//		String sexo = "";
//		ApiRequestMB request = new ApiRequestMB();
//
//		try {
//			if (!empty(scanner)) {
//				try (Scanner scanner1 = new Scanner(scanner).useDelimiter("@")) {
//					numeroTramite = scanner1.next();
//					String nombre = scanner1.next();
//					String apellido = scanner1.next();
//					sexo = scanner1.next();
//					numeroDocumento = scanner1.next();
//					String ejemplar = scanner1.next();
//
//					request = ApiMB.request("Validaciones", "viviendas", "GET", "/v1/validaciones", contexto);
//					request.query("idtramite", numeroTramite);
//					request.query("dni", numeroDocumento);
//					request.query("sexo", sexo);
//					return ApiMB.response(request);
//				}
//			}
//
//		} catch (Exception e) {
//			//
//		}
//
//		return null;
//	}

    public static RespuestaMB validarDatosCRM(ContextoMB contexto) {
        String cuil = contexto.parametros.string("cuilPersona");
        Boolean estado = contexto.parametros.bool("estado");
        String validacion = "VALIDADO";
        if (!estado)
            validacion = "NO_VALIDADO";

        if (Objeto.anyEmpty(cuil))
            return RespuestaMB.error();

        List<String> listaIdCobis = RestPersona.listaIdCobis(contexto,
                (cuil.substring(0, cuil.length() - 1)).substring(2).replaceFirst("^0+(?!$)", ""), null, null);
        if (listaIdCobis == null || (listaIdCobis != null && listaIdCobis.isEmpty())
                || (listaIdCobis != null && listaIdCobis.size() > 1))
            return RespuestaMB.error();

        contexto.sesion().setIdCobis(listaIdCobis.get(0));

        if (checkOnboardingTdv(contexto, cuil)) {
            SqlResponseMB sqlResponse = consultaTablaInternaRenaper(cuil);
            if (sqlResponse.hayError || (!sqlResponse.hayError && sqlResponse.registros.isEmpty()))
                return RespuestaMB.estado("ERROR_NO_ENCONTRADO");
            return SqlRenaper.updateTablaInterna(sqlResponse.registros.get(0).integer("id"), validacion)
                    ? RespuestaMB.exito()
                    : RespuestaMB.error();
        } else {
            SqlResponseMB sqlResponse = SqlScanDni.obtenerUltimoUsuarioRenaper(cuil);
            if (sqlResponse.hayError || (!sqlResponse.hayError && sqlResponse.registros.isEmpty()))
                return RespuestaMB.estado("ERROR_NO_ENCONTRADO");
            return SqlScanDni.actualizarEstadoUsuarioValidadoDesdeSucursal(cuil) ? RespuestaMB.exito()
                    : RespuestaMB.error();
        }
    }

    protected static String buscarSexoByScanner(String scanner) {

        String[] parts = scanner.split("@");
        for (int i = 0; i < parts.length; i++) {
            String dato = empty(parts[i]) ? null : parts[i].trim();
            if (esSexo(dato)) {
                return dato;
            }
        }
        return null;
    }

    protected static String buscarNroTramitebyScanner(String scanner) {

        String[] parts = scanner.split("@");
        for (int i = 0; i < parts.length; i++) {
            String dato = empty(parts[i]) ? null : parts[i].trim();
            if (esNroTramite(dato)) {
                return dato;
            }
        }
        return null;
    }

    protected static String buscarDniByScanner(String scanner) {
        String[] parts = scanner.split("@");
        for (int i = 0; i < parts.length; i++) {
            String dato = empty(parts[i]) ? null : parts[i].trim();
            if (esDni(dato)) {
                return dato;
            }
        }
        return null;
    }

    public static Boolean esNroTramite(String numeroTramite) {
        return !empty(numeroTramite) && numeroTramite.length() == 11 && numeroTramite.substring(0, 2).equals("00");
    }

    public static Boolean esSexo(String sexo) {
        return !empty(sexo) && sexo.length() == 1;
    }

    public static Boolean esDni(String dni) {
        return !empty(dni) && (dni.length() == 7 || dni.length() == 8) && dni.matches(".*[0-9].*");
    }

    private static boolean validarMaximoReintentos(ContextoMB contexto) {
        if (Objeto.empty(contexto.sesion().getCuentaIntentosScanDni()))
            contexto.sesion().setCuentaIntentosScanDni(0);
        if (Objeto.empty(contexto.sesion().getCuentaIntentosPersonaNoEncontrada()))
            contexto.sesion().setCuentaIntentosPersonaNoEncontrada(0);
        return contexto.sesion().getCuentaIntentosScanDni() < 3;
    }

    /**
     * Api Genérica de Scan DNI para validar cambios de dispositivo
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB scanDniDispositivo(ContextoMB contexto) {
        try {
            String idDispositivo = contexto.parametros.string("idDispositivo", "");
            String origenValidacion = contexto.parametros.string("origenValidacion", null);
            String token = contexto.parametros.string("token", null);

            if (Objeto.anyEmpty(origenValidacion))
                return RespuestaMB.parametrosIncorrectos();

            if (!EnumUtils.isValidEnum(EnumOrigenValidacionRenaper.class, origenValidacion))
                return RespuestaMB.estado(ORIGEN_INVALIDO);

            switch (EnumOrigenValidacionRenaper.valueOf(origenValidacion)) {
                case LOGIN, CORRESPONSALIA:
                    return validardIdDispositivoLogin(contexto, token, idDispositivo);
                default:
                    return RespuestaMB.error();
            }

        } catch (Exception e) {
            return RespuestaMB.estado("ERROR_NO_TIPIFICADO");
        }
    }

    /**
     * Api Genérica de Scan DNI para validar datos contra renaper
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB scanDniRenaper(ContextoMB contexto) {

        String scanner = contexto.parametros.string("scanner");
        String origenValidacion = contexto.parametros.string("origenValidacion", null);
        String token = contexto.parametros.string("token", null);
        String idDispositivo = contexto.parametros.string("idDispositivo", null);

        if (Objeto.anyEmpty(scanner, origenValidacion))
            return RespuestaMB.parametrosIncorrectos();

        if (!EnumUtils.isValidEnum(EnumOrigenValidacionRenaper.class, origenValidacion))
            return RespuestaMB.estado(ORIGEN_INVALIDO);

        switch (EnumOrigenValidacionRenaper.valueOf(origenValidacion)) {
            case LOGIN, CORRESPONSALIA:
                return validarRenaperLogin(contexto, scanner, token, idDispositivo);
            default:
                return RespuestaMB.error();
        }
    }

    /**
     * Se Comunica con api viviendas para validar documento contra Renaper
     *
     * @param contexto
     * @param numeroTramite
     * @param numeroDocumento
     * @param sexo
     * @return cuit.
     */
    private static ApiResponseMB validarRenaper(ContextoMB contexto, String numeroTramite, String numeroDocumento,
                                                String sexo) {
        ApiRequestMB request = ApiMB.request("ValidacionRenaper", "viviendas", "GET", "/v1/validaciones", contexto);
        request.query("idtramite", numeroTramite);
        request.query("dni", numeroDocumento);
        request.query("sexo", sexo);
        request.permitirSinLogin = true;
        return ApiMB.response(request);
    }

    /**
     * Obtiene cuit de persona
     *
     * @param contexto
     * @return cuit.
     */
    private static String obtenerCuit(ContextoMB contexto) {
        return contexto.persona().cuit();
    }

    /**
     * Chequea que el usuario exista para Mobile
     *
     * @param contexto
     * @return existe.
     */
    private static boolean existeUsuario(ContextoMB contexto) {
        return SqlScanDni.usuarioExiste(contexto.idCobis());
    }

    /**
     * Chequea que el usuario tenga que ir a validar DNI en Sucursal
     *
     * @param cuit
     * @return existe.
     */
    private static SqlResponseMB validaTieneIrSucursal(String cuit) {
        return SqlScanDni.obtenerUltimoUsuarioRenaper(cuit);
    }

    /**
     * Valida Si el usuario tiene que ir a la sucursal
     *
     * @param cuit
     * @return respuesta.
     */
    private static RespuestaMB usuarioTieneQueIrSucursal(String cuit) {
        SqlResponseMB sqlResponse = validaTieneIrSucursal(cuit);
        if (sqlResponse.hayError)
            return RespuestaMB.estado(ERROR_TIENE_IR_SUCURSAL);

        if (!sqlResponse.registros.isEmpty() && sqlResponse.registros.get(0).string("enumerador")
                .equals(EnumEstadoUsuarioRenaper.VALIDARSUCURSAL.name()))
            return RespuestaMB.estado(VALIDAR_DNI_SUCURSAL);

        return null;
    }

    /**
     * Chequea que el token corresponda al usuario
     *
     * @param contexto
     * @param token
     * @return coincide.
     */
    private static boolean coincideTokenUsuario(ContextoMB contexto, String token) {
        return SqlScanDni.coincideToken(contexto.idCobis(), token);
    }

    /**
     * Chequea que fue validado en sucursal
     *
     * @param contexto
     * @return coincide.
     */
    private static boolean validadoEnSucursal(ContextoMB contexto) {
        SqlResponseMB sqlResponse = SqlScanDni.obtenerUltimoUsuarioRenaper(obtenerCuit(contexto));
        if (sqlResponse.hayError || (!sqlResponse.hayError && sqlResponse.registros.isEmpty()))
            return false;
        return sqlResponse.registros.get(0).bool("validado_sucursal");
    }

    /**
     * Actualizar que fue validado en sucursal en el caso que corresponda
     *
     * @param contexto
     * @return coincide.
     */
    private static void quitarValidadoEnSucursal(ContextoMB contexto) {
        if (validadoEnSucursal(contexto))
            SqlScanDni.modificarValidadoUltimoUsuarioRenaper(obtenerCuit(contexto));
    }

    /**
     * Guarda Historial del Usuario
     *
     * @param contexto
     * @return respuesta.
     */
    private static RespuestaMB guardarHistorialUsuario(ContextoMB contexto, String token, String idDispositivo,
                                                       String numeroTelefono) {
        SqlResponseMB sqlResponse = SqlScanDni.obtenerUsuario(contexto.idCobis());
        if (sqlResponse.hayError || (!sqlResponse.hayError && sqlResponse.registros.isEmpty()))
            return RespuestaMB.estado(ERROR_OBTENER_USUARIO);

        if (!SqlScanDni.guardarUsuarioToken(contexto.idCobis(), sqlResponse.registros.get(0).string("token"), token,
                idDispositivo, numeroTelefono))
            return RespuestaMB.estado(ERROR_INSERTAR_USUARIO_TOKEN);
        return null;
    }

    /**
     * Envío de Notificacion
     *
     * @param contexto
     * @param mensaje
     * @param url
     * @return apiResponse.
     */
    private static ApiResponseMB enviarNotificacion(ContextoMB contexto, String mensaje, String url,
                                                    String notificationType, String label, String title, String type) {
        ApiRequestMB request = ApiMB.request("SegmentacionPush", "segmentacion_mobile", "POST", "/v1/send", contexto);
        request.body("cobisList", Collections.singletonList(contexto.idCobis()));
        request.body("label", label);
        request.body("message", mensaje);
        request.body("title", title);
        request.body("type", type);
        request.body("notificationType", notificationType);
        request.body("validLabel", "true");
        request.body("webUrl", url);

        return ApiMB.response(request);
    }

    /**
     * Verificar el cuil del scanner
     *
     * @param cuil
     * @return valido.
     */
    public static Boolean esCuil(String cuil) {
        return !empty(cuil) && cuil.matches(REGEX_NUMBER) && cuil.length() > 1 && !cuil.equals("0");
    }

    /**
     * Verificar el ejemplar del scanner
     *
     * @param ejemplar
     * @return valido.
     */
    public static Boolean esEjemplar(String ejemplar) {
        return !empty(ejemplar) && (ejemplar.length() == 1);
    }

    /**
     * Verificar la fecha de emisión del scanner
     * }
     *
     * @param fechaEmision}
     * @return valido.
     */
    public static Boolean esFechaEmision(String fechaEmision) {
        return !empty(fechaEmision) && fechaEmision.matches(REGEX_FECHA_SCANNER);
    }

    /**
     * Verificao si el scaneo es de DNI viejo o nuevo
     *
     * @param scanner
     * @return valido.
     */
    public static Boolean esDniViejo(String scanner) {
        return scanner.split("@").length > 9;
    }

    /**
     * Obtengo el dni del scanner
     *
     * @param scanner
     * @return dni.
     */
    private static String obtenerDniScanner(String scanner) {
        String dni = scanner.split("@").length != 0
                ? esDniViejo(scanner) ? scanner.split("@")[1].trim() : scanner.split("@")[4]
                : "";

        if (!dni.matches(REGEX_NUMBER))
            dni = dni.replaceAll(NUMBER_REPLACE, "");

        return StringUtils.isNotBlank(dni) && esDni(dni) ? dni : "";
    }

    /**
     * Obtengo el número de trámite del scanner
     *
     * @param scanner
     * @return dni.
     */
    private static String obtenerNumeroTramiteScanner(String scanner) {
        String dni = scanner.split("@").length != 0
                ? esDniViejo(scanner) ? scanner.split("@")[10] : scanner.split("@")[0]
                : "";
        return StringUtils.isNotBlank(dni) && esNroTramite(dni) ? dni : "";
    }

    /**
     * Obtengo el sexo del scanner
     *
     * @param scanner
     * @return dni.
     */
    private static String obtenerSexoScanner(String scanner) {
        String dni = scanner.split("@").length != 0
                ? esDniViejo(scanner) ? scanner.split("@")[8] : scanner.split("@")[3]
                : "";
        return StringUtils.isNotBlank(dni) && esSexo(dni) ? dni : "";
    }

    /**
     * Obtengo el ejemplar del scanner
     *
     * @param scanner
     * @return ejemplar.
     */
    private static String obtenerEjemplarScanner(String scanner) {
        String ejemplar = scanner.split("@").length != 0
                ? esDniViejo(scanner) ? scanner.split("@")[2] : scanner.split("@")[5]
                : "";
        return StringUtils.isNotBlank(ejemplar) && esEjemplar(ejemplar) ? ejemplar : "";
    }


    /**
     * Obtengo la fecha de emisión del scanner
     *
     * @param scanner
     * @return fecha de emisión.
     */
    private static String obtenerFechaEmisionScanner(String scanner) {
        String fecha = scanner.split("@").length != 0
                ? esDniViejo(scanner) ? scanner.split("@")[9] : scanner.split("@")[7]
                : "";
        return StringUtils.isNotBlank(fecha) && esFechaEmision(fecha) ? fecha : "";
    }

    /**
     * Compara Dni de scanner con dni persona
     *
     * @param scanner
     * @return coinciden.
     */
    private static boolean coincidenDni(ContextoMB contexto, String scanner) {
        return obtenerDniScanner(scanner).equals(contexto.persona().numeroDocumento());
    }

    /**
     * Suma Cantidad de Intentos de Scan Dni
     *
     * @param contexto
     */
    private static void sumarIntentoScanDni(ContextoMB contexto) {
        contexto.sesion().setCuentaIntentosScanDni(contexto.sesion().getCuentaIntentosScanDni() + 1);
    }

    /**
     * Suma Cantidad de Intentos Persona No Encontrada
     *
     * @param contexto
     */
    private static void sumarIntentoPersonaNoEncontrada(ContextoMB contexto) {
        contexto.sesion()
                .setCuentaIntentosPersonaNoEncontrada(contexto.sesion().getCuentaIntentosPersonaNoEncontrada() + 1);
    }

    /**
     * Valido que el documento scaneado sea igual al que tenemos
     *
     * @param contexto
     * @param scanner
     * @return respuesta.
     */
    private static RespuestaMB validarDocumentoScaneadoConPersona(ContextoMB contexto, String scanner) {
        if (coincidenDni(contexto, scanner))
            return null;
        sumarIntentoScanDni(contexto);
        if (!validarMaximoReintentos(contexto))
            return RespuestaMB.estado(MAXIMOS_REINTENTOS);
        return RespuestaMB.estado(NO_COINCIDE_DNI);
    }

    protected static boolean esMenorEdad(ContextoMB contexto) {
        return contexto.persona().esMenor();
    }

    protected static boolean esEjemplar(String ejemplar, String scanner) {
        return ejemplar.equals(obtenerEjemplarScanner(scanner));
    }

    /**
     * Validar la cantidad de intentos Persona no encontrada
     *
     * @param contexto
     * @return respuesta.
     */
    private static boolean validarIntentosPersonaNoEncontradaMenorTres(ContextoMB contexto) {
        return contexto.sesion().getCuentaIntentosPersonaNoEncontrada() < 3;
    }

    /**
     * Obtener Usuario Estado Renaper por Enumerador
     *
     * @param estadoEnum
     * @return respuesta
     */
    private static SqlResponseMB obtenerUsuarioEstadoRenaper(EnumEstadoUsuarioRenaper estadoEnum) {
        return SqlScanDni.obtenerUsuarioRenaperEstadoPorEnumerador(estadoEnum.name());
    }

    /**
     * Obtener Usuario Estado Renaper por Enumerador
     *
     * @param scanner
     * @return respuesta
     */
    private static boolean validarFormatoScan(String scanner) {
        return scanner.split("@").length >= 8;
    }

    /**
     * Inserta Usuario Renaper
     *
     * @return respuesta
     */
    private static RespuestaMB insertarUsuarioRenaper(String idTramite, String ejemplar, String vencimiento,
                                                      String fechaEmision, String apellido, String nombre, String fechaNacimiento, String cuil, String calle,
                                                      String numero, String piso, String departamento, String codigoPostal, String barrio, String monoblock,
                                                      String ciudad, String municipio, String provincia, String pais, String idDispositivo,
                                                      EnumEstadoUsuarioRenaper estadoEnum) {
        SqlResponseMB sqlResponse = obtenerUsuarioEstadoRenaper(estadoEnum);
        if (sqlResponse.hayError || sqlResponse.registros.isEmpty())
            return RespuestaMB.estado(ERROR_OBTENER_RENAPER_ESTADO);

        if (!SqlScanDni.guardarUsuarioRenaper(idTramite, ejemplar, vencimiento, fechaEmision, apellido, nombre,
                fechaNacimiento, cuil, calle, numero, piso, departamento, codigoPostal, barrio, monoblock, ciudad,
                municipio, provincia, pais, idDispositivo, sqlResponse.registros.get(0).integer("id", null)))
            return RespuestaMB.estado(ERROR_GUARDAR_USUARIO_RENAPER);

        return null;
    }

    /**
     * Obtengo cuil de persona a partir de su dni
     *
     * @param contexto
     * @param dni
     * @return cuil
     */
    private static String obtenerCuilDeDocumento(ContextoMB contexto, String dni) {
        ApiRequestMB request = ApiMB.request("ObtenerCuilPersonaPorDocumento", "personas", "GET", "/nrodoc", contexto);
        request.query("nrodoc", dni);
        request.permitirSinLogin = true;
        ApiResponseMB response = ApiMB.response(request);
        if (response.hayError())
            return "";

        List<Objeto> lista = new ArrayList<>();
        for (Objeto item : response.objetos()) {
            if (ConfigMB.esProduccion()) {
                if (!contexto.persona().nombre().isEmpty()
                        && item.string("apellidoYNombre").toUpperCase()
                        .contains(contexto.persona().nombre().toUpperCase())
                        && !contexto.persona().apellido().isEmpty() && item.string("apellidoYNombre").toUpperCase()
                        .contains(contexto.persona().apellido().toUpperCase()))
                    lista.add(item);
            } else {
                // MOCK DE CASOS PARA PODER PROBAR EN HOMO
                if (contexto.idCobis().equals("27304")) {
                    if (!contexto.persona().nombre().isEmpty()
                            && !item.string("apellidoYNombre").toUpperCase()
                            .contains(contexto.persona().nombre().toUpperCase())
                            && !contexto.persona().apellido().isEmpty() && !item.string("apellidoYNombre").toUpperCase()
                            .contains(contexto.persona().apellido().toUpperCase()))
                        lista.add(item);
                } else {
                    if (!contexto.persona().nombre().isEmpty()
                            && item.string("apellidoYNombre").toUpperCase()
                            .contains(contexto.persona().nombre().toUpperCase())
                            && !contexto.persona().apellido().isEmpty() && item.string("apellidoYNombre").toUpperCase()
                            .contains(contexto.persona().apellido().toUpperCase()))
                        lista.add(item);
                }
            }
        }

        if (lista.isEmpty() || (!lista.isEmpty() && lista.size() > 1))
            return "";

        return lista.get(0).string("cuil");
    }

    /**
     * Valido los datos de api renaper con los de persona
     *
     * @param contexto
     * @param cuil
     * @param fechaNacimiento
     * @return valido.
     */
    private static boolean validarDatosRenaperConPersona(ContextoMB contexto, String cuil, String fechaNacimiento) {
        return cuil.equals(obtenerCuit(contexto))
                && Fecha.stringToDate(contexto.persona().fechaNacimiento(FORMATO_FECHA_CONVERT), FORMATO_FECHA_CONVERT)
                .equals(Fecha.stringToDate(fechaNacimiento, FORMATO_FECHA_RENAPER));
    }

    /**
     * Valido los datos de api renaper
     *
     * @param codigo
     * @return valido.
     */
    private static boolean validarCodigoApi(String codigo) {
        return codigo.equals("0") || codigo.equals("99");
    }

    /**
     * Valido los datos de api renaper
     *
     * @param fechaVencimiento
     * @param codigoFallecido
     * @param fechaEmision
     * @param ejemplar
     * @param scanner
     * @return valido.
     */
    private static boolean validarDatosRenaperConScanner(String fechaVencimiento, String codigoFallecido,
                                                         String fechaEmision, String ejemplar, String scanner) {
        Date hoy = new Date();
        return (hoy.before(Fecha.stringToDate(fechaVencimiento, FORMATO_FECHA_RENAPER))
                || hoy.equals(Fecha.stringToDate(fechaVencimiento, FORMATO_FECHA_RENAPER)))
                && codigoFallecido.equals("0") && ejemplar.equals(obtenerEjemplarScanner(scanner))
                && Fecha.stringToDate(fechaEmision, FORMATO_FECHA_RENAPER)
                .equals(Fecha.stringToDate(obtenerFechaEmisionScanner(scanner), FORMATO_FECHA_SCANNER));

    }

    /**
     * Valido que el documento scaneado contra renaper
     *
     * @param scanner
     * @return respuesta.
     */
    private static RespuestaMB validarDocumentoScaneadoConRenaper(ContextoMB contexto, String scanner,
                                                                  String idDispositivo) {
        RespuestaMB respuesta = null;
        String cuil = "";
        ApiResponseMB apiResponse = validarRenaper(contexto, obtenerNumeroTramiteScanner(scanner),
                obtenerDniScanner(scanner), obtenerSexoScanner(scanner));

        if (apiResponse.hayError()) {
            if (apiResponse.codigo == 400 || apiResponse.codigo == 404) {
                System.out.println(contexto.idCobis() + "| corresponsalia | + " + "PERSONA_NO_ENCONTRADA");
                return RespuestaMB.estado(PERSONA_NO_ENCONTRADA);
            }
            sumarIntentoScanDni(contexto);
            if (apiResponse.codigo == 500) {
                System.out.println(contexto.idCobis() + "| corresponsalia | + " + "ERROR_REINTENTAR");
                return RespuestaMB.estado(ERROR_REINTENTAR);
            }
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "ERROR_RENAPER");
            return RespuestaMB.estado(ERROR_RENAPER);
        }

        cuil = !esCuil(apiResponse.string("cuil")) ? obtenerCuilDeDocumento(contexto, obtenerDniScanner(scanner))
                : apiResponse.string("cuil");

        /*
         * if(esMenorEdad(contexto)) { sumarIntentoPersonaNoEncontrada(contexto); return
         * RespuestaMB.estado(MENOR_EDAD); }
         *
         * if(!esEjemplar(apiResponse.string("ejemplar"),scanner)){
         * sumarIntentoPersonaNoEncontrada(contexto); return
         * RespuestaMB.estado(DNI_ULTIMO_EJEMPLAR); }
         */

        Boolean condicion1 = !validarCodigoApi(apiResponse.string("codigo"));
        Boolean condicion2 = !validarDatosRenaperConScanner(apiResponse.string("vencimiento"),
                apiResponse.string("codigo_fallecido"), apiResponse.string("emision"), apiResponse.string("ejemplar"),
                scanner);
        Boolean condicion3 = !validarDatosRenaperConPersona(contexto, cuil, apiResponse.string("fecha_nacimiento"));

        if (condicion1 || condicion2 || condicion3) {
            sumarIntentoPersonaNoEncontrada(contexto);
            if (!validarIntentosPersonaNoEncontradaMenorTres(contexto)) {
                respuesta = insertarUsuarioRenaper(apiResponse.string("id_tramite_principal", null),
                        apiResponse.string("ejemplar", null).toUpperCase(), apiResponse.string("vencimiento", null),
                        apiResponse.string("emision", null), apiResponse.string("apellido", null).toUpperCase(),
                        apiResponse.string("nombres", null).toUpperCase(), apiResponse.string("fecha_nacimiento", null),
                        cuil, apiResponse.string("calle", null).toUpperCase(), apiResponse.string("numero", null),
                        apiResponse.string("piso", null), apiResponse.string("departamento", null),
                        apiResponse.string("codigo_postal", null), apiResponse.string("barrio", null).toUpperCase(),
                        apiResponse.string("monoblock", null), apiResponse.string("ciudad", null),
                        apiResponse.string("municipio", null).toUpperCase(),
                        apiResponse.string("provincia", null).toUpperCase(),
                        apiResponse.string("pais", null).toUpperCase(), idDispositivo,
                        EnumEstadoUsuarioRenaper.VALIDARSUCURSAL);
                if (respuesta != null)
                    return respuesta;
                System.out.println(contexto.idCobis() + "| corresponsalia | + " + "VALIDAR_DNI_SUCURSAL");
                return RespuestaMB.estado(VALIDAR_DNI_SUCURSAL);
            }
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "error3");
            return RespuestaMB.estado(PERSONA_NO_ENCONTRADA);
        }

        insertarUsuarioRenaper(apiResponse.string("id_tramite_principal", null),
                apiResponse.string("ejemplar", null).toUpperCase(), apiResponse.string("vencimiento", null),
                apiResponse.string("emision", null), apiResponse.string("apellido", null).toUpperCase(),
                apiResponse.string("nombres", null).toUpperCase(), apiResponse.string("fecha_nacimiento", null), cuil,
                apiResponse.string("calle", null).toUpperCase(), apiResponse.string("numero", null),
                apiResponse.string("piso", null), apiResponse.string("departamento", null),
                apiResponse.string("codigo_postal", null), apiResponse.string("barrio", null).toUpperCase(),
                apiResponse.string("monoblock", null), apiResponse.string("ciudad", null),
                apiResponse.string("municipio", null).toUpperCase(),
                apiResponse.string("provincia", null).toUpperCase(), apiResponse.string("pais", null).toUpperCase(),
                idDispositivo, EnumEstadoUsuarioRenaper.VALIDADO);

        registrarScan(contexto, "0");
        return RespuestaMB.exito();
    }

    /**
     * Registra en el visualizador los casos OK de Scan
     *
     * @param contexto
     * @param respuesta
     * @return respuesta.
     */
    private static String registrarScan(ContextoMB contexto, String respuesta) {
        try {
            Map<String, Object> headers = new LinkedHashMap<>();
            headers.putAll(contexto.headers());

            Objeto extra = new Objeto();
            extra.set("respuesta", respuesta);
            extra.set("ip", contexto.ip());

            ApiRequestMB apiRequest = ApiMB.request("auditor", "auditor", "POST", "/v1/reportes", contexto);
            apiRequest.permitirSinLogin = true;
            apiRequest.habilitarLog = false;
            apiRequest.body.set("canal", "MB");
            apiRequest.body.set("subCanal", "BACUNI");
            apiRequest.body.set("usuario", contexto.sesion().idCobis());
            apiRequest.body.set("idProceso", Util.idProceso());
            apiRequest.body.set("sesion", contexto.sesion().id());
            apiRequest.body.set("servicio",
                    "API-MB" + contexto.request.getRequestURI().replace("/mb/api/", "/").replace('/', '_'));
            apiRequest.body.set("resultado", "200");
            apiRequest.body.set("duracion", 1L);
            Objeto mensajes = apiRequest.body.set("mensajes");
            mensajes.set("entrada", Objeto.fromMap(headers).toJson());
            mensajes.set("salida", extra.toJson());
            ApiMB.response(apiRequest);
        } catch (Exception e) {
        }
        return "";
    }

    /**
     * Realiza las validaciones correspondientes al Scan DNi para saber si quien se
     * loguea es la misma persona la que instaló por primera vez, reinstaló o cambió
     * de dispositivo.
     *
     * @param contexto
     * @param token
     * @param idDispositivo
     * @return respuesta.
     */
    private static RespuestaMB validardIdDispositivoLogin(ContextoMB contexto, String token, String idDispositivo) {
        RespuestaMB respuesta = null;

        if (contexto.idCobis() == null)
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        if (Objeto.empty(token, idDispositivo))
            return RespuestaMB.parametrosIncorrectos();

        if (existeUsuario(contexto)) {
            if (Objeto.empty(obtenerCuit(contexto)))
                return RespuestaMB.estado(SIN_CUIT);

            if (coincideTokenUsuario(contexto, token) || validadoEnSucursal(contexto)) {
                quitarValidadoEnSucursal(contexto);
                registrarScan(contexto, CONSOLIDADA);
                return RespuestaMB.exito(MENSAJE, CONSOLIDADA);
            }

            respuesta = usuarioTieneQueIrSucursal(obtenerCuit(contexto));
            if (respuesta != null)
                return respuesta;

            respuesta = guardarHistorialUsuario(contexto, token, idDispositivo, "");
            if (respuesta != null)
                return respuesta;

            enviarNotificacion(contexto, MENSAJE_PUSH, "", NOTIFICATION_TYPE_PUSH, LABEL_PUSH, TITLE_PUSH, TYPE_PUSH);
        }

        registrarScan(contexto, SCAN_DNI);
        return RespuestaMB.exito(MENSAJE, SCAN_DNI);
    }

    /**
     * Realiza el Scan Dni
     *
     * @param contexto Valida si el usuario tiene que ir a sucursal o no
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB validarEstadoUsuario(ContextoMB contexto) {
        if (contexto.idCobis() == null)
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        RespuestaMB respuesta = usuarioTieneQueIrSucursal(obtenerCuit(contexto));
        if (respuesta != null)
            return respuesta;

        return RespuestaMB.exito();
    }

    /**
     * Realiza el Scan Dni
     *
     * @param contexto
     * @param scanner
     * @param token
     * @param idDispositivo
     * @return respuesta.
     */
    private static RespuestaMB validarRenaperLogin(ContextoMB contexto, String scanner, String token,
                                                   String idDispositivo) {
        RespuestaMB respuesta = null;

        if (contexto.idCobis() == null) {
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "SIN_PSEUDO_SESION");
            return RespuestaMB.sinPseudoSesion();
        }

        if (!validarMaximoReintentos(contexto)) {
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "MAXIMOS_REINTENTOS");
            return RespuestaMB.estado(MAXIMOS_REINTENTOS);
        }

        if (!validarFormatoScan(scanner)) {
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "ERROR_FORMATO");
            return RespuestaMB.estado(ERROR_FORMATO);
        }

        respuesta = validarDocumentoScaneadoConPersona(contexto, scanner);
        if (respuesta != null) {
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "error1");
            return respuesta;
        }

        if (!validarIntentosPersonaNoEncontradaMenorTres(contexto)) {
            System.out.println(contexto.idCobis() + "| corresponsalia | + " + "error2");
            return RespuestaMB.estado(VALIDAR_DNI_SUCURSAL);
        }

        return validarDocumentoScaneadoConRenaper(contexto, scanner, idDispositivo);

    }


    /**
     * Actualiza el estado del usuario
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB actualizarEstadoUsuario(ContextoMB contexto) {
        String cuil = contexto.parametros.string("cuil");

        if (Objeto.anyEmpty(cuil))
            return RespuestaMB.parametrosIncorrectos();

        SqlResponseMB sqlResponse = SqlScanDni.obtenerUltimoUsuarioRenaper(cuil);
        if (sqlResponse.hayError || (!sqlResponse.hayError && sqlResponse.registros.isEmpty()))
            return RespuestaMB.estado(CUIL_NO_EXSITE);

        return SqlScanDni.actualizarEstadoUsuarioValidado(cuil) ? RespuestaMB.exito()
                : RespuestaMB.estado(ERROR_VALIDADO);
    }

    /**
     * Verifica si es usuario es Onboarding y TDV
     *
     * @param cuil
     * @return valido.
     */
    private static boolean checkOnboardingTdv(ContextoMB contexto, String cuil) {
        SqlResponseMB sqlResponse = consultaRenaperOnboarding(cuil);
        if (sqlResponse.hayError)
            return false;
        return !sqlResponse.registros.isEmpty() && validarTarjetaVirtual(contexto);
    }

    /**
     * valida si es usuario es Onboarding y TDV
     *
     * @param contexto
     * @return respuesta.
     */
    public static RespuestaMB validarEsOnboardingTdv(ContextoMB contexto) {

        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.estado(SIN_PSEUDO_SESION);

        if (Objeto.empty(obtenerCuit(contexto)))
            return RespuestaMB.estado(SIN_CUIT);

        return RespuestaMB.exito("esOnboardingTdv", checkOnboardingTdv(contexto, obtenerCuit(contexto)));
    }

    public static String obtenerCuitDeDocumento(ContextoMB contexto) {
        String documento = contexto.parametros.string("documento");
        String nombres = contexto.parametros.string("nombres");
        String apellidos = contexto.parametros.string("apellidos");

        ApiResponseMB response = RestPersona.consultarPersonaCuitPadron(contexto, documento);

        if (response == null || response.hayError()) {
            return "";
        }

        List<Objeto> lista = new ArrayList<>();
        for (Objeto item : response.objetos()) {
            if (item.string("apellidoYNombre").toUpperCase().contains(nombres.toUpperCase())
                    && item.string("apellidoYNombre").toUpperCase().contains(apellidos.toUpperCase()))
                lista.add(item);
        }

        if (lista.isEmpty() || (!lista.isEmpty() && lista.size() > 1))
            return "";

        return lista.get(0).string("cuil");
    }

    /**
     * Valida que el usuario sólo tenga tarjetas virtuales
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean tieneTodasTarjetasVirtuales(ContextoMB contexto) {
        return contexto.tarjetasDebito().stream().allMatch(td -> td.virtual());
    }

    /**
     * Valida si el usuario ingresa por primera vez
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean esPrimeraVez(ContextoMB contexto) {
        return !existeUsuario(contexto);
    }

    /**
     * Valida si el usuario modifico datos
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean modificoDatos(ContextoMB contexto) {
        RespuestaMB respuesta = RestContexto.isRiskForChangeInformation48Horas(contexto);
        if (!respuesta.hayError())
            return !((boolean) respuesta.get("enableb_operator"));
        return false;
    }

    /**
     * Valida si el usuario reinstaló dispositivo
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean reinstaloDispositivoActivoSoftToken48horas(ContextoMB contexto) {
        boolean cambioDispositivo = false;

        RespuestaMB respuesta = MBRegistroDispositivo.esUltimoRegistrado(contexto);
        if (!respuesta.hayError())
            cambioDispositivo = !((boolean) respuesta.get("esUltimoRegistrado"));

        return new MBSoftToken().activoSoftTokenEnLasUltimas48HorasHabiles(contexto) || cambioDispositivo;
    }

    /**
     * Valida si el usuario hizo onboarding
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean esOnboarding(ContextoMB contexto) {
        if (ConfigMB.bool("validar_onboarding_registro_dispositivo", false)) {
            SqlResponseMB sqlResponse = SqlEsales.esOnboarding(obtenerCuit(contexto));
            if (sqlResponse.hayError)
                return false;
            return !sqlResponse.registros.isEmpty();
        }
        return false;
    }

    /**
     * Valida si los casos que si o si llevan sms son correctos
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean validacionesRegistrarSms(ContextoMB contexto, boolean esMonoProducto, boolean modificoDatos) {
        return esOnboarding(contexto) || tieneTodasTarjetasVirtuales(contexto) || esMonoProducto || esPrimeraVez(contexto)
                || (reinstaloDispositivoActivoSoftToken48horas(contexto) && !modificoDatos);
    }

    public static RespuestaMB tieneBloqueoOperaciones(ContextoMB ctx) {
        if (!MBAplicacion.funcionalidadPrendida(ctx.idCobis(), "prendido_bloqueo_base_negativa")) {
            return RespuestaMB.exito();
        }

        if (RestPersona.tieneBaseNegativa(ctx)) {
            return RespuestaMB.estado(
                    ctx.tieneContador("GUARDO_DOC_INGRESO") ? "PENDIENTE_PROBACION" : "REQUIERE_DOCUMENTACION"
            );
        }

        return RespuestaMB.exito();
    }

    /**
     * Valida contra Transmit
     *
     * @param contexto
     * @param funcionalidad
     * @return respuesta.
     */
    private static RespuestaMB recomendacionTransmit(ContextoMB contexto, String funcionalidad) {
        if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_datos_personales",
                "prendido_modo_transaccional_datos_personales_cobis") && !TransmitMB.isChallengeOtp(contexto, funcionalidad)) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return RespuestaMB.parametrosIncorrectos();

                ChangeDataMBBMBankProcess changeDataMBBMBankProcess = new ChangeDataMBBMBankProcess(contexto.idCobis(), sessionToken, BankProcessChangeDataType.ACCOUNT_DETAILS_CHANGE);

                return TransmitMB.recomendacionTransmit(contexto, changeDataMBBMBankProcess, funcionalidad);
            } catch (Exception e) {
                return RespuestaMB.exito();
            }
        }
        return RespuestaMB.exito();
    }

    public static boolean esClienteExterior(ContextoMB contexto) {
        return RestPersona.existeMuestreo("habilitar.mail.login", "true", contexto.idCobis()) ||
                RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", contexto.idCobis());
    }

    public static RespuestaMB obtenerDatosClientesTransmit(ContextoMB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return RespuestaMB.sinPseudoSesion();

        Boolean esClienteExterior;
        Boolean tieneTodasTarjetasVirtuales = false;
        Boolean esMonoproducto = false;

        Futuro<Boolean> futuroClienteExterior = new Futuro<>(() -> esClienteExterior(contexto));
        Futuro<Boolean> futuroTarjetasVirtuales = new Futuro<>(() -> tieneTodasTarjetasVirtuales(contexto));
        Futuro<Boolean> futuroMonoproducto = new Futuro<>(contexto::esMonoProductoTC);

        esClienteExterior = futuroClienteExterior.tryGet();
        if (!esClienteExterior) {
            tieneTodasTarjetasVirtuales = futuroTarjetasVirtuales.tryGet();
            if (!tieneTodasTarjetasVirtuales)
                esMonoproducto = futuroMonoproducto.tryGet();
        }

        return RespuestaMB.exito("tipoCliente",
                Transmit.generarTipoClienteEncriptado(
                        ConfigMB.string("clave_secreta_transmit", ""),
                        esClienteExterior,
                        tieneTodasTarjetasVirtuales,
                        esMonoproducto));
    }

    public static RespuestaMB dataValidOtp(ContextoMB contexto) {
        try {
            if (contexto == null || contexto.idCobis() == null) {
                return RespuestaMB.estado("SIN_SESION");
            }

            if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp")) {
                return RespuestaMB.exito();
            }

            Calendar fechaActual = Calendar.getInstance();
            fechaActual.setTime(new Date());
            Date dFechaActual = fechaActual.getTime();

            boolean emailVencido = false;
            boolean telefonoVencido = false;

            ApiResponseMB dataValid = RestPersona.getDataValid(contexto);
            for (Objeto item : dataValid.objetos()) {

                boolean estaVencido = item.get("fhVto") == null
                        || dFechaActual.compareTo(item.date("fhVto", "yyyy-MM-dd")) >= 0;

                if ("EMAIL".equals(item.string("entidad"))
                        && (estaVencido || !"S".equals(item.string("optMail")))) {
                    emailVencido = true;
                }

                if ("TEL".equals(item.string("entidad"))
                        && (estaVencido || !"S".equals(item.string("otpTelefono")))) {
                    telefonoVencido = true;
                }
            }

            if (!emailVencido && !telefonoVencido) {
                return RespuestaMB.exito();
            }

            String fechaDesde = Fecha.restarDias(new Date(), 30L, "yyyy-MM-dd");
            List<Objeto> eventos = contexto.tieneContador("ALERTA_DATAVALID_OTP", fechaDesde);
            for (Objeto evento : eventos) {
                Date fechaEvento = evento.date("momento");
                if (Fecha.esFechaActual(fechaEvento)) {
                    return RespuestaMB.exito();
                }
            }

            contexto.insertarContador("ALERTA_DATAVALID_OTP");
            return RespuestaMB.estado("VENCIDO")
                    .set("emailVencido", emailVencido)
                    .set("telefonoVencido", telefonoVencido)
                    .set("bloquearModal", eventos.size() >= 3);
        } catch (Exception e) {
            return RespuestaMB.estado("ERROR_DATA_VALID");
        }
    }

    private static boolean usuarioBloqueadoSoftToken(RespuestaMB respuestaMB) {
        if (respuestaMB == null || respuestaMB.hayError())
            return false;
        return respuestaMB.bool("usuarioBloqueado", false);
    }

    public static RespuestaMB mostrarModalTcv(ContextoMB contexto) {
        boolean mostrarModalTcv = false;
        if (!contexto.tieneContador("GUARDO_ADICIONALES_TCV")) {
            mostrarModalTcv = tieneAltaTcv(contexto) && contexto.logsLogin().size() >= 3;
        }

        return RespuestaMB.exito("mostrarModalTcv", mostrarModalTcv);
    }

    static boolean tieneAltaTcv(ContextoMB contexto) {
        SqlRequestMB request = SqlMB.request("GetSesionTcv", "esales");
        String sql = "SELECT TOP 1 cuil ";
        sql += "FROM [esales].[dbo].[Sesion] t1 WITH (NOLOCK) ";
        sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] t2 WITH (NOLOCK) ";
        sql += "ON t1.id = t2.sesion_id ";
        sql += "WHERE t1.fecha_ultima_modificacion > ? ";
        sql += "AND t2.sucursal_onboarding LIKE '%FLUJO_TCV%' ";
        sql += "AND t1.estado = 'FINALIZAR_OK' ";
        sql += "AND t1.cuil = ? ";

        request.sql = sql;
        request.add(Fecha.restarDias(new Date(), 20L, "yyyy-MM-dd").toString());
        request.add(contexto.persona().cuit());
        SqlResponseMB response = SqlMB.response(request);
        return response.registros.size() > 0;
    }

    public static RespuestaMB guardarAdicionalesTcv(ContextoMB contexto) {
        String idEstadoCivil = contexto.parametros.string("idEstadoCivil");
        Objeto conyuge = contexto.parametros.objeto("conyuge");

        if ("C".equals(idEstadoCivil)
                && contexto.persona().cuit().contains(conyuge.string("numeroDocumento"))) {
            return RespuestaMB.estado("CONYUGE_INVALIDO");
        }

        if ("S".equals(contexto.persona().idEstadoCivil()) && "S".equals(idEstadoCivil)) {
            contexto.insertarContador("GUARDO_ADICIONALES_TCV");
            return RespuestaMB.exito();
        }

        guardarAdicionalPersona(contexto, idEstadoCivil, conyuge);

        if (!"C".equals(contexto.persona().idEstadoCivil()) && "C".equals(idEstadoCivil)) {
            RespuestaMB resGuardarConyuge = guardarConyuge(contexto, conyuge);
            if (!"0".equals(resGuardarConyuge.string("estado"))) {
                return resGuardarConyuge;
            }
        }

        contexto.insertarContador("GUARDO_ADICIONALES_TCV");
        return RespuestaMB.exito();
    }

    private static RespuestaMB guardarConyuge(ContextoMB contexto, Objeto conyuge) {
        String numeroDocumento = conyuge.string("numeroDocumento");
        if (numeroDocumento.length() == 7) {
            numeroDocumento = "0" + numeroDocumento;
        }

        String cuilConyuge = RestPersona.buscarCuil(contexto, numeroDocumento, conyuge.string("genero"));
        if (cuilConyuge == null) {
            return RespuestaMB.estado("CUIL_NO_ENCONTRADO");
        }

        if (cuilConyuge.equals(RestPersona.cuitConyuge(contexto))) {
            return RespuestaMB.exito();
        }

        ApiResponseMB personaConyuge = RestPersona.consultarPersonaEspecifica(contexto, cuilConyuge);
        if (personaConyuge.hayError()) {
            RestPersona.crearPersona(contexto, cuilConyuge);
        }

        Objeto persona = new Objeto();
        persona.set("nombreConyuge", contexto.persona().nombre());
        persona.set("apellidoConyuge", contexto.persona().apellido());
        persona.set("idEstadoCivil", "C");
        persona.set("idSubtipoEstadoCivil", "Y");
        persona.set("nombres", conyuge.string("nombre"));
        persona.set("apellidos", conyuge.string("apellido"));
        persona.set("numeroDocumento", conyuge.string("numeroDocumento"));
        persona.set("idSexo", conyuge.string("genero"));

        String dia = conyuge.string("fechaNacimiento").split("/")[0];
        String mes = conyuge.string("fechaNacimiento").split("/")[1];
        String anio = conyuge.string("fechaNacimiento").split("/")[2];
        persona.set("fechaNacimiento", anio + "-" + mes + "-" + dia + "T00:00:00");

        persona.set("idNacionalidad", conyuge.string("idNacionalidad"));
        persona.set("idPaisNacimiento", conyuge.string("idNacionalidad"));
        persona.set("idPaisResidencia", conyuge.string("idPaisResidencia"));
        persona.set("idVersionDocumento", "A");
        persona.set("idTipoIDTributario", "08");
        persona.set("idIva", "CONF");
        persona.set("idGanancias", "NORE");
        persona.set("idTipoDocumento", "01");
        if (!"80".equals(conyuge.string("idNacionalidad"))
                || !"80".equals(conyuge.string("idPaisResidencia"))) {
            persona.set("idTipoDocumento", "134");
        }

        RestPersona.actualizarPersona(contexto, persona, cuilConyuge);

        String idRelacion = null;
        ApiRequestMB request = ApiMB.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponseMB resRelaciones = ApiMB.response(request);
        if (!resRelaciones.hayError()) {
            for (Objeto item : resRelaciones.objetos()) {
                if ("2".equals(item.string("idTipoRelacion"))) {
                    idRelacion = item.string("id");
                }
            }
        }

        if (idRelacion == null) {
            RestPersona.generarRelacionPersona(contexto, "2", cuilConyuge, contexto.idCobis());
        } else {
            RestPersona.actualizarRelacionPersona(contexto, idRelacion, "2", cuilConyuge, contexto.idCobis(), null, null, Fecha.fechaActual() + "T00:00:00");
        }

        return RespuestaMB.exito();
    }

    private static void guardarAdicionalPersona(ContextoMB contexto, String idEstadoCivil, Objeto conyuge) {
        Objeto persona = new Objeto();
        persona.set("idEstadoCivil", idEstadoCivil);
        if ("C".equals(idEstadoCivil)) {
            persona.set("cantidadNupcias", "P");
            persona.set("idSubtipoEstadoCivil", "Y");
            persona.set("nombreConyuge", conyuge.string("nombre"));
            persona.set("apellidoConyuge", conyuge.string("apellido"));
        }

        RestPersona.actualizarPersona(contexto, persona, contexto.persona().cuit());
    }

}