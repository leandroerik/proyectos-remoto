package ar.com.hipotecario.canal.homebanking.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.transmit.JourneyTransmitEnum;
import ar.com.hipotecario.backend.util.Errores;
import ar.com.hipotecario.backend.util.Transmit;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.*;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.*;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestContexto;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import ar.com.hipotecario.canal.homebanking.servicio.RestVivienda;
import ar.com.hipotecario.canal.homebanking.servicio.SqlSoftTokenService;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.canal.libreriariesgofraudes.domain.enums.BankProcessChangeDataType;
import ar.com.hipotecario.canal.libreriariesgofraudes.infrastructure.gateway.model.bm.hb.ChangeDataHBBMBankProcess;
import org.apache.commons.lang3.StringUtils;

public class HBPersona {

    public static Respuesta persona(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        if (Objeto.anyEmpty(documento)) {
            return Respuesta.parametrosIncorrectos();
        }

        Objeto datos = new Objeto();
        Date fechaHoraUltimaConexion = contexto.sesion.fechaHoraUltimaConexion;

        List<Objeto> personas = RestPersona.personas(contexto, documento, idTipoDocumento, idSexo);
        if (personas == null) {
            return Respuesta.error();
        } else if (personas.isEmpty()) {
            return Respuesta.estado("PERSONA_NO_ENCONTRADA");
        } else if (personas.size() > 1) {
            return Respuesta.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        Objeto persona = personas.get(0);

        datos.set("nombre", Texto.primerasMayuscula(persona.string("nombre").split(" ")[0]));
        datos.set("nombres", Texto.primerasMayuscula(persona.string("nombre")));
        datos.set("apellido", Texto.primerasMayuscula(persona.string("apellido")).split(" ")[0]);
        datos.set("apellidos", Texto.primerasMayuscula(persona.string("apellido")));
        datos.set("cuit", persona.string("numeroIdentificacionTributaria"));
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
        datos.set("primerIngreso", false);

        return Respuesta.exito("persona", datos);
    }

    public static int calcularEdad(LocalDate birthDate) {
        LocalDate now = LocalDate.now();
        if (birthDate != null) {
            return Period.between(birthDate, now).getYears();
        } else {
            return 0;
        }
    }

    public static Respuesta cliente(ContextoHB contexto) {
        Boolean buscarEmail = contexto.parametros.bool("buscarEmail");
        Boolean buscarCelular = contexto.parametros.bool("buscarCelular");
        Boolean buscarDomicilio = contexto.parametros.bool("buscarDomicilio");
        Boolean buscarDatosNacimiento = contexto.parametros.bool("buscarDatosNacimiento");
        Boolean buscarEsProspecto = contexto.parametros.bool("buscarEsProspecto");

        Boolean prendidoDataValid = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid");

        Objeto datos = new Objeto();
        Date fechaHoraUltimaConexion = contexto.sesion.fechaHoraUltimaConexion;

        ApiResponse clientes = RestPersona.clientes(contexto);
        if (clientes.hayError()) {
            return Respuesta.error();
        }

        Objeto cliente = clientes.objetos().get(0);
        datos.set("nombre", Texto.primerasMayuscula(cliente.string("nombres").split(" ")[0]));
        datos.set("nombres", Texto.primerasMayuscula(cliente.string("nombres")));
        datos.set("apellido", Texto.primerasMayuscula(cliente.string("apellidos")).split(" ")[0]);
        datos.set("apellidos", Texto.primerasMayuscula(cliente.string("apellidos")));
        datos.set("cuit", cliente.string("cuit"));
        datos.set("ultimaConexionFormateada",
                fechaHoraUltimaConexion != null
                        ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(fechaHoraUltimaConexion)
                        : "");
        datos.set("primerIngreso", false);
        datos.set("edad", cliente.integer("edad"));
        datos.set("menor", cliente.integer("edad") < 18);
        datos.set("esEmpleado", Objeto.setOf("EM", "FU").contains(cliente.string("idTipoCliente")));
        datos.set("idSexo", cliente.string("idSexo"));

        Futuro<Integer> futuroIdPaisNacimiento = buscarDatosNacimiento
                ? new Futuro<>(() -> cliente.integer("idPaisNacimiento"))
                : null;
        Futuro<String> futuroPaisNacimiento = buscarDatosNacimiento
                ? new Futuro<>(() -> RestCatalogo.mapaPaises(contexto).get(cliente.integer("idPaisNacimiento")))
                : null;
        Futuro<String> futuroEmail = buscarEmail
                ? new Futuro<>(() -> RestPersona.direccionEmail(contexto, contexto.persona().cuit()))
                : null;
        Futuro<Boolean> futuroDirLegal = buscarEmail && prendidoDataValid
                ? new Futuro<>(() -> RestPersona.direccionEmailLegal(contexto, contexto.persona().cuit()))
                : null;
        Futuro<Objeto> futuroCelular = buscarCelular
                ? new Futuro<>(() -> RestPersona.celular(contexto, contexto.persona().cuit()))
                : null;
        Futuro<Objeto> futuroDomicilio = buscarDomicilio
                ? new Futuro<>(() -> RestPersona.domicilioPostal(contexto, contexto.persona().cuit()))
                : null;
        Futuro<String> futuroProvincia = buscarDomicilio
                ? new Futuro<>(
                () -> RestCatalogo.nombreProvincia(contexto, futuroDomicilio.get().integer("idProvincia", 1)))
                : null;
        Futuro<String> futuroLocalidad = buscarDomicilio ? new Futuro<>(() -> RestCatalogo.nombreLocalidad(contexto,
                futuroDomicilio.get().integer("idProvincia", 1), futuroDomicilio.get().integer("idCiudad", 146)))
                : null;
        Futuro<Respuesta> futuroDataValid = prendidoDataValid ? new Futuro<>(() -> HBPersona.dataValid(contexto))
                : null;
        Futuro<Respuesta> futuroRedes = prendidoDataValid ? new Futuro<>(() -> RestPersona.getRedesSociales(contexto))
                : null;

        if (buscarDatosNacimiento) {
            datos.set("idPaisNacimiento", futuroIdPaisNacimiento.get());
            datos.set("paisNacimiento", futuroPaisNacimiento.get());
            datos.set("idNacionalidad", cliente.integer("idNacionalidad"));
            datos.set("nacionalidad", RestCatalogo.mapaPaises(contexto).get(cliente.integer("idNacionalidad")));
            datos.set("ciudadNacimiento", cliente.string("ciudadNacimiento"));
        }

        datos.set("fechaNacimiento", cliente.date("fechaNacimiento", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));

        // TODO: paralelizar

        if (buscarEsProspecto) {
            datos.set("esProspecto", contexto.esProspecto());
        }

        if (buscarEmail) {
            datos.set("email", futuroEmail.get());
            if (prendidoDataValid) {
                datos.set("checkDirLegal", futuroDirLegal.get());
            }

        }

        if (buscarCelular) {
            Objeto celular = futuroCelular.get();
            if (celular != null) {
                Objeto objeto = new Objeto();
                objeto.set("codigoArea", celular.string("codigoArea"));
                objeto.set("caracteristica", celular.string("caracteristica"));
                objeto.set("numero", celular.string("numero"));
                objeto.set("secTel", celular.string("id"));

                contexto.parametros.set("parametro", "whatsapp");
                Respuesta respuestaNotifWhatsapp = HBNotificaciones.consultarNotificacionPorParametro(contexto);
                if (respuestaNotifWhatsapp.bool("existe")) {
                    objeto.set("notificaWhatsapp", respuestaNotifWhatsapp.bool("habilitado"));
                } else {
                    objeto.set("notificaWhatsapp", false);
                }
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
            objeto.set("provincia", futuroProvincia.get());
            objeto.set("localidad", futuroLocalidad.get());
            objeto.set("codigoPostal", domicilio.string("idCodigoPostal"));
            objeto.set("entreCalle1", domicilio.string("calleEntre1"));
            objeto.set("entreCalle2", domicilio.string("calleEntre2"));
            objeto.set("idProvincia", domicilio.integer("idProvincia", 1));
            objeto.set("idLocalidad", domicilio.integer("idCiudad", 146));
            objeto.set("secDir", domicilio.integer("id"));
            datos.set("domicilio", objeto);
        }

        if (prendidoDataValid) {
            Objeto dataValidObj = new Objeto();
            Respuesta dataValid = futuroDataValid.get();

            dataValidObj.set("mail", dataValid.get("EMAIL") == null ? "OK" : dataValid.get("EMAIL"));
            dataValidObj.set("telefono", dataValid.get("TEL") == null ? "OK" : dataValid.get("TEL"));
            dataValidObj.set("domicilio", dataValid.get("DOM") == null ? "OK" : dataValid.get("DOM"));
            datos.set("dataValid", dataValidObj);

            Objeto redesSociales = new Objeto();
            Respuesta redes = futuroRedes.get();
            redesSociales.set("twitter", redes.get("twitter"));
            redesSociales.set("instagram", redes.get("instagram"));
            datos.set("redesSociales", redesSociales);
        }

        return Respuesta.exito("persona", datos);
    }

    public static Respuesta telefonoPersonal(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        Objeto celular = RestPersona.celular(contexto, contexto.persona().cuit());
        if (celular != null) {
            respuesta.set("celular", celular.string("codigoArea", "") + "-" + celular.string("caracteristica", "") + "-"
                    + celular.string("numero", ""));
        }
        return respuesta;
    }

    public static Boolean enviarMailActivacionOtp(ContextoHB contexto) {
        try {
            Objeto parametros = new Objeto();
            parametros.set("Subject", "Se realizó la activación de OTP");
            parametros.set("NOMBRE", contexto.persona().nombre());
            parametros.set("APELLIDO", contexto.persona().apellido());
            Date hoy = new Date();
            parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
            parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
            parametros.set("CANAL", "Home Banking");
            parametros.set("TITULAR_CANAL", contexto.persona().apellido());

            RestNotificaciones.envioMail(contexto, ConfigHB.string("doppler_activacion_otp"), parametros);
        } catch (Exception e) {
        }
        return true;
    }

    public static Respuesta actualizarDatosPersonales(ContextoHB contexto) {
        Boolean actualizarEmail = contexto.parametros.bool("actualizarEmail", false);
        Boolean actualizarCelular = contexto.parametros.bool("actualizarCelular", false);
        Boolean actualizarDomicilio = contexto.parametros.bool("actualizarDomicilio", false);
        Boolean actualizarRedesSociales = contexto.parametros.bool("actualizarRedesSociales", false);
        String mailAnterior = RestPersona.direccionEmail(contexto, contexto.persona().cuit());
        String email = contexto.parametros.string("email", mailAnterior);
        String celularCodigoArea = contexto.parametros.string("celular.codigoArea");
        String celularCaracteristica = contexto.parametros.string("celular.caracteristica");
        String celularNumero = contexto.parametros.string("celular.numero");
        String calle = contexto.parametros.string("domicilio.calle");
        String altura = contexto.parametros.string("domicilio.altura");
        String piso = contexto.parametros.string("domicilio.piso");
        String departamento = contexto.parametros.string("domicilio.departamento");
        String idProvincia = contexto.parametros.string("domicilio.idProvincia");
        String idLocalidad = contexto.parametros.string("domicilio.idLocalidad");
        String codigoPostal = contexto.parametros.string("domicilio.codigoPostal");
        String entreCalle1 = contexto.parametros.string("domicilio.entreCalle1");
        String entreCalle2 = contexto.parametros.string("domicilio.entreCalle2");
        Boolean habilitacionDatosPersonales = contexto.parametros.bool("habilitacionDatosPersonales", false);
        Boolean esOriginacion = contexto.parametros.bool("esOriginacion", false);
        Boolean checkDirLegal = contexto.parametros.bool("checkDirLegal");
        String celularAnterior = RestPersona.numeroCelular(contexto, contexto.persona().cuit());
        Boolean validarDatos = contexto.parametros.bool("validarDatos", false);
        Boolean prendidoDataValid = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid");
        boolean prendidoDataValidOtp = HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp");
        if (prendidoDataValidOtp) {
            prendidoDataValid = false;
        }
        Boolean esCaAltaOnline = contexto.parametros.bool("esCaAltaOnline", false);
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        boolean esMigrado = contexto.esMigrado(contexto);

        if (esMigrado && Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        String modificacion = "";

        if (contexto.esProspecto()) {
            return Respuesta.estado("PROSPECTO");
        }

        boolean validarDatavalidOtpEmail = false;
        boolean validarDatavalidOtpTelefono = false;

        if (validarDatos) {
            actualizarEmail = false;
            actualizarCelular = false;
            actualizarDomicilio = false;
            validarDatavalidOtpEmail = true;
            validarDatavalidOtpTelefono = true;

            if (prendidoDataValidOtp) {
                Objeto resOtpDatavalid = dataValidOtp(contexto);
                if ("VENCIDO".equals(resOtpDatavalid.string("estado"))) {

                    if (resOtpDatavalid.bool("emailVencido")
                            && !contexto.sesion.tieneOtpDatavalidValido("email", mailAnterior)) {
                        return Respuesta.estado("REQUIERE_OTP");
                    }

                    if (resOtpDatavalid.bool("telefonoVencido")
                            && !contexto.sesion.tieneOtpDatavalidValido("sms", celularAnterior)) {
                        return Respuesta.estado("REQUIERE_OTP");
                    }
                }
            }
        } else {
            if (esOriginacion && !esMigrado) {
                Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, "originacion", JourneyTransmitEnum.HB_INICIO_SESION);
                if (actualizarEmail || actualizarCelular || respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;
            } else {
                Respuesta respuestaValidaTransaccion = contexto.validarTransaccion(contexto, esMigrado, esCaAltaOnline ? "alta-ca-td-online" : ((habilitacionDatosPersonales) ? "validar-otp" : "datos-personales"), JourneyTransmitEnum.HB_INICIO_SESION);
                if (respuestaValidaTransaccion.hayError())
                    return respuestaValidaTransaccion;
                String funcionalidad = esCaAltaOnline ? "alta-ca-td-online" : ((habilitacionDatosPersonales) ? "validar-otp" : "datos-personales");

                Respuesta respuestaTransmit = recomendacionTransmit(contexto, funcionalidad);
                if (respuestaTransmit.hayError())
                    return respuestaTransmit;
            }

            if (!esMigrado && prendidoDataValidOtp && actualizarEmail
                    && !contexto.sesion.tieneOtpDatavalidValido("email", email)) {
                return Respuesta.estado("REQUIERE_OTP");
            }

            if (!esMigrado && prendidoDataValidOtp && actualizarCelular) {
                String telefono = celularCodigoArea + celularCaracteristica + celularNumero;
                telefono = telefono.startsWith("0") ? telefono.substring(1) : telefono;
                if (!contexto.sesion.tieneOtpDatavalidValido("sms", telefono)) {
                    return Respuesta.estado("REQUIERE_OTP");
                }
            }
        }

        if (actualizarEmail) {
            String modificacionMailAux = RestPersona.compararMailActualizado(contexto, email);
            ApiResponse response = RestPersona.actualizarEmail(contexto, contexto.persona().cuit(), email, checkDirLegal);
            if (response.hayError()) {
                return Respuesta.error(contexto.csmIdAuth);
            }

            Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
            if (!esMigrado && prendidoDataValid) {
                new Futuro<>(() -> RestPersona.postDataValid(contexto, 0, secMail, 0));
            }

            if (!esMigrado && prendidoDataValidOtp) {
                new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, 0, secMail, 0));
                validarDatavalidOtpEmail = true;
            }

            try {
                new Futuro<>(() -> HBNotificaciones.modificarConfiguracionAlertas(contexto));
            } catch (Exception e) {
            }

            if (!modificacionMailAux.isEmpty()) {
                new Futuro<>(() -> contexto.insertarLogCambioMail(contexto, mailAnterior, email));
            }
            modificacion += modificacionMailAux;
        }

        if (actualizarCelular) {
            String modificacionCelularAux = RestPersona.compararCelularActualizado(contexto, celularCodigoArea,
                    celularCaracteristica, celularNumero);

            ApiResponse response = RestPersona.actualizarCelular(contexto, contexto.persona().cuit(), celularCodigoArea,
                    celularCaracteristica, celularNumero);
            if (response.hayError()) {
                String finalModificacion = modificacion;
                new Futuro<>(() -> RestPersona.enviarMailActualizacionDatosPersonales(contexto, finalModificacion,
                        mailAnterior, celularAnterior));
                return Respuesta.error(contexto.csmIdAuth);
            }

            Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
            Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
            if (!esMigrado && prendidoDataValid) {
                new Futuro<>(() -> RestPersona.postDataValid(contexto, secTelDom, 0, secTel));
            }

            if (!esMigrado && prendidoDataValidOtp) {
                new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, secTelDom, 0, secTel));
                validarDatavalidOtpTelefono = true;
            }

            if (!modificacionCelularAux.isEmpty()) {
                new Futuro<>(() -> contexto.insertarLogCambioCelular(contexto, celularAnterior,
                        celularCodigoArea + celularCaracteristica + celularNumero));
                modificacion = (modificacion.isEmpty() ? modificacionCelularAux
                        : modificacion + ", " + modificacionCelularAux);
            }
        }

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

            String modificacionDireccionAux = RestPersona.compararDomicilioActualizado(contexto, calle, altura, piso,
                    departamento, idProvincia, idLocalidad, codigoPostal);

            Objeto domicilioAnterior = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());

            ApiResponse response = RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio,
                    "DP");
            if (response.hayError()) {
                String finalModificacion = modificacion;
                new Futuro<>(() -> RestPersona.enviarMailActualizacionDatosPersonales(contexto, finalModificacion,
                        mailAnterior, celularAnterior));
                String error = "ERROR";
                error = response.string("mensajeAlUsuario").contains("NO EXISTE CODIGO POSTAL")
                        ? "NO_EXISTE_CODIGO_POSTAL"
                        : error;
                error = response.string("mensajeAlUsuario").contains("DEBEN SER NUMERICOS") ? "PARAMETROS_INCORRECTOS"
                        : error;
                error = response.string("mensajeAlUsuario").contains("EXISTEN MODIFICACIONES PENDIENTES CON SMART OPEN")
                        ? "CAMBIO_PENDIENTE"
                        : error;

                if (response.string("mensajeAlUsuario").contains("EXISTEN MODIFICACIONES PENDIENTES CON SMART OPEN")) {
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

                        new Futuro<>(() -> RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(),
                                domicilioAnt, "DP"));
                    } catch (Exception e) {
                    }
                }

                return Respuesta.estado(error, contexto.csmIdAuth);
            }

            if (!esMigrado && prendidoDataValid) {
                Integer secDir = RestPersona.domicilioPostal(contexto, contexto.persona().cuit()).integer("idCore");
                new Futuro<>(() -> RestPersona.postDataValid(contexto, secDir, 0, 0));
            }

            if (!modificacionDireccionAux.isEmpty()) {
                modificacion = (modificacion.isEmpty() ? modificacionDireccionAux
                        : modificacion + ", " + modificacionDireccionAux);
            }
        }

        if (habilitacionDatosPersonales) { // emm-->si viene en falso NO TIENE QUE LLAMAR AL PERMITIR ya que en ese caso
            Integer valorOtpSegundoFactor = RestPersona.sugerirOtpSegundoFactor(contexto.idCobis());
            boolean enviarMailOtp = valorOtpSegundoFactor != null && valorOtpSegundoFactor != 1;
            SqlResponse sqlResponse = RestPersona.permitirSegundoFactorOtp(contexto.idCobis(), true);
            if (sqlResponse.hayError) {
                String finalModificacion = modificacion;
                new Futuro<>(() -> RestPersona.enviarMailActualizacionDatosPersonales(contexto, finalModificacion,
                        mailAnterior, celularAnterior));
                return Respuesta.estado("ERROR_HABILITACION_OTP", contexto.csmIdAuth);
            }
            if (enviarMailOtp) {
                new Futuro<>(() -> enviarMailActivacionOtp(contexto));
            }
        }

        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_monitoreo_transaccional")) {
            HBMonitoring monitoringApi = new HBMonitoring();
            String codigoTransaccion = "YA0000";
            codigoTransaccion = (actualizarCelular) ? "YA0000"
                    : (actualizarEmail) ? "Y90000" : (actualizarDomicilio) ? "ZP0000" : codigoTransaccion;
            String celu = (actualizarCelular) ? (celularCodigoArea + celularCaracteristica + celularNumero) : null;
            String emailNuevo = (actualizarEmail) ? email : null;
            String codigoTransaccionFinal = codigoTransaccion;
            Boolean actualizarCelularFinal = actualizarCelular;
            new Futuro<>(() -> monitoringApi.sendMonitoringNoMoney(contexto, null, codigoTransaccionFinal, emailNuevo,
                    celu, (actualizarCelularFinal) ? celularAnterior : null));
        }

        if (actualizarRedesSociales && prendidoDataValid) {
            new Futuro<>(() -> RestPersona.insertRedesSociales(contexto));
        }

        if (validarDatos && prendidoDataValid) {
            Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
            new Futuro<>(() -> RestPersona.postDataValid(contexto, 0, secMail, 0));

            Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
            Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
            new Futuro<>(() -> RestPersona.postDataValid(contexto, secTelDom, 0, secTel));

            Integer secDir = RestPersona.domicilioPostal(contexto, contexto.persona().cuit()).integer("idCore");
            new Futuro<>(() -> RestPersona.postDataValid(contexto, secDir, 0, 0));

            try {
                contexto.parametros.set("nemonico", "ACTUALIZA_DATAVALID");
                new Futuro<>(() -> Util.contador(contexto));
            } catch (Exception e) {
            }
        } else if (validarDatos && prendidoDataValidOtp) {
            Integer secMail = RestPersona.idEmail(contexto, contexto.persona().cuit());
            new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, 0, secMail, 0));

            Integer secTel = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idCore");
            Integer secTelDom = RestPersona.celular(contexto, contexto.persona().cuit()).integer("idDireccion");
            new Futuro<>(() -> RestPersona.postDataValidOtp(contexto, secTelDom, 0, secTel));
        } else {
            try {
                if (contexto.sesion.dataValid) {
                    contexto.parametros.set("nemonico", "MODIFICA_DATAVALID");
                    new Futuro<>(() -> Util.contador(contexto));
                    contexto.sesion.dataValid = false;
                }
            } catch (Exception e) {
            }
        }

        contexto.limpiarSegundoFactor();
        contexto.sesion.validaRiesgoNet = (false);
        String finalModificacion = modificacion;
        new Futuro<>(() -> RestPersona.enviarMailActualizacionDatosPersonales(contexto, finalModificacion, mailAnterior,
                celularAnterior));

        if (prendidoDataValid) {
            try {
                contexto.sesion.cache.remove("Email");
                contexto.sesion.cache.remove("Telefono");
                contexto.sesion.cache.remove("Domicilio");
                new Futuro<>(() -> cliente(contexto));
            } catch (Exception e) {
            }
        }

        if (validarDatavalidOtpEmail || validarDatavalidOtpTelefono) {
            contexto.sesion.limpiarOtpDatavalid();
            try {
                contexto.parametros.set("nemonico", "ACTUALIZA_DATAVALID_OTP");
                Util.contador(contexto);
            } catch (Exception e) {
            }
        }

        return Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth);
    }

    public static Respuesta paises(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        Map<Integer, String> paises = RestCatalogo.mapaPaises(contexto);
        for (Integer id : paises.keySet()) {
            respuesta.add("paises", new Objeto().set("id", id).set("descripcion", paises.get(id)));
        }
        return respuesta;
    }

    public static Respuesta provincias(ContextoHB contexto) {
        contexto.sesion.setChallengeOtp(false);

        Respuesta respuesta = new Respuesta();
        ApiResponse datos = RestCatalogo.provincias(contexto);
        if (datos.hayError()) {
            return Respuesta.error();
        }
        for (Objeto dato : datos.objetos()) {
            if (dato.string("id").equals("1")) {
                respuesta.add("provincias",
                        new Objeto().set("id", dato.integer("id")).set("descripcion", dato.string("descripcion")));
            }
        }
        for (Objeto dato : datos.objetos()) {
            if (!dato.string("id").equals("1") && !dato.string("descripcion").equals("NO DEFINIDA")) {
                respuesta.add("provincias",
                        new Objeto().set("id", dato.integer("id")).set("descripcion", dato.string("descripcion")));
            }
        }
        return respuesta;
    }

    public static Respuesta localidades(ContextoHB contexto) {
        Integer idProvincia = contexto.parametros.integer("idProvincia");

        if (Objeto.anyEmpty(idProvincia)) {
            return Respuesta.parametrosIncorrectos();
        }

        Respuesta respuesta = new Respuesta();
        Map<Integer, String> localidades = RestCatalogo.mapaLocalidades(contexto, idProvincia);
        for (Integer id : localidades.keySet()) {
            respuesta.add("localidades", new Objeto().set("id", id).set("descripcion", localidades.get(id)));
        }
        return respuesta;
    }

    public static Respuesta validadoresSegundoFactor(ContextoHB contexto, String cbuDestinoParametro) {
        String funcionalidad = contexto.parametros.string("funcionalidad");
        // este parametro del contexto solo se manda en transferencias,
        // tener en cuenta que cuando esta funcion se la llama internamente desde el
        // proyecto
        // como no llega del request el cbu, este se va a mandar desde la función que lo
        // esté llamando
        String cbu = contexto.parametros.string("cbu", cbuDestinoParametro);
        contexto.sesion.cbuDestinoValidacionSegundoFactor = (cbu);

        boolean forzarOcultamientoEmail = false;
        try {
            if (cbu != null) {
                String cuenta = cbu;
                if (cbu.startsWith("044")) {
                    CuentaTercero cuentaTercero = new CuentaTercero(contexto, cbu);
                    cbu = cuentaTercero.cuentaLink.string("cuenta");
                }
                Boolean ocultarPorCVU = cbu.startsWith("000") && RestContexto.primeraTransferencia(contexto, cbu);
                Boolean ocultarPorCBU = !cbu.startsWith("000") && !RestContexto.agendada(contexto, cbu, cuenta);
                forzarOcultamientoEmail = ocultarPorCVU || ocultarPorCBU;
            }
            if (cbu == null && "transferencia".equals(funcionalidad)) {
                forzarOcultamientoEmail = true;
            }
        } catch (Exception e) {
            forzarOcultamientoEmail = "transferencia".equals(funcionalidad);
        }

        boolean poseeCuentas = false;
        boolean poseeCuentasUnipersonales = false;
        boolean esMonoProducto = false;

        try {
            if (!contexto.idCobis().isEmpty() && contexto.tarjetasDebito().isEmpty()) {
                poseeCuentas = !contexto.cuentas().isEmpty();
                poseeCuentasUnipersonales = contexto.poseeCuentasUnipersonales();
                esMonoProducto = contexto.esMonoProductoTC();
            }
        } catch (Exception e) {
        }

        boolean smsHabilitado = RestPersona.otpSegundoFactor(contexto.idCobis());
        boolean emailHabilitado = !forzarOcultamientoEmail && RestPersona.otpSegundoFactor(contexto.idCobis());
        boolean linkHabilitado = contexto.tarjetaDebitoPorDefecto() != null;
        boolean enableToGetRedLinkPassword = contexto.isEnableToGetRedLinkPassword();

        SqlResponse respuestaSqlSoftToken = new SqlSoftTokenService().consultarAltaSoftToken(contexto.idCobis(),
                "ACTIVO");
        boolean softTokenHabilitado = !respuestaSqlSoftToken.hayError && !respuestaSqlSoftToken.registros.isEmpty();
        SqlResponse respuestaSqlBloqueadoSoftToken = new SqlSoftTokenService()
                .consultarBloqueoUsoSoftTokenPorUsuario(contexto.idCobis(), true);
        boolean softTokenBloqueado = !respuestaSqlBloqueadoSoftToken.hayError
                && !respuestaSqlBloqueadoSoftToken.registros.isEmpty();

        Respuesta respuesta = new Respuesta();

        if ("adhesion-canal".equals(funcionalidad)) {
            if (contexto.esProspecto() && !contexto.esProcrear(contexto) && !contexto.esTasaCero(contexto)) {
                return respuesta;
            }
            System.out.println("el resultado del calculo clave red-link fue (" + enableToGetRedLinkPassword + ")");
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", enableToGetRedLinkPassword);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
        }

        if ("adhesion-pago".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                    "prendido_soft_token_alta_pago_impuesto_servicio") && tieneTarjetasDebitoHabilitadas(contexto)
                    && softTokenHabilitado) {
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
        }

        if ("billetera-virtual".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("biometria".equals(funcionalidad)) {
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", emailHabilitado);
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", enableToGetRedLinkPassword);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("biometria-revocar".equals(funcionalidad)) {
            if (softTokenHabilitado && !softTokenBloqueado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            if (enableToGetRedLinkPassword)
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", true);
        }

        if ("cambio-clave-canal".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", !enableToGetRedLinkPassword);
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", enableToGetRedLinkPassword);
            if (RestPersona.existeMuestreo("habilitar.mail.login", "true", contexto.idCobis())) {
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            }
            if (softTokenHabilitado && !softTokenBloqueado)
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
        }

        if ("datos-personales".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "datos-personales")) {
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            } else {
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_datos_personales")
                        && tieneTarjetasDebitoHabilitadas(contexto)) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                }
            }
        }

        if ("debin".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "debin")) {
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            } else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_debin")
                        && tieneTarjetasDebitoHabilitadas(contexto)) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                }
            }
        }

        if ("prestamo-personal".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "prestamo-personal")) {
                contexto.sesion.validaSegundoFactorOtp = true;
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            } else {
                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_prestamo_personal")
                        && tieneTarjetasDebitoHabilitadas(contexto) && contexto.cuentaUnipersonalCAPesos() != null
                        && softTokenHabilitado) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
                }
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true); // pidieron forzar sms.
            }
        }

        if ("adelanto".equals(funcionalidad)) {
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_adelanto")
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
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", enableToGetRedLinkPassword);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("originacion".equals(funcionalidad)) {
            respuesta.add("validadores", "riesgo-net").add("validadoresUsuario", "riesgo-net", true);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms",
                    smsHabilitado || esMonoProducto || (poseeCuentas && !poseeCuentasUnipersonales));
        }

        if ("rescate-fondos".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "rescate-fondos"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_rescate_fondos")
                        && tieneTarjetasDebitoHabilitadas(contexto) && softTokenHabilitado) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
                }
            }
        }

        if ("aumento-limite-transferencia".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                    "prendido_soft_token_aumento_limite_transferencia") && tieneTarjetasDebitoHabilitadas(contexto)) {
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
            }
        }

        if ("cambio-usuario-logeado".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "cambio-usuario-logeado"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_cambio_usuario_logeado")
                        && tieneTarjetasDebitoHabilitadas(contexto)) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                }
            }
        }

        if ("cambio-clave-logeado".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "cambio-clave-logeado"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
                respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);

                if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_cambio_clave_logeado")
                        && tieneTarjetasDebitoHabilitadas(contexto)) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                }
            }
        }

        if ("originacion-datos-personales".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms",
                    smsHabilitado || esMonoProducto || (poseeCuentas && !poseeCuentasUnipersonales));

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(),
                    "prendido_soft_token_originacion_datos_personales") && softTokenHabilitado) {
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);
            }
        }

        if ("transferencia".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "transferencia"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

                if (!ConfigHB.bool("forzar_otp", false)) {
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
                }

                if (RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", contexto.idCobis())) {
                    respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
                }
            }
        }

        if ("validar-otp".equals(funcionalidad)) {
            respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link", linkHabilitado);
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("orden-extraccion".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);

            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_soft_token_orden_extraccion")
                    && tieneTarjetasDebitoHabilitadas(contexto)) {
                respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", softTokenHabilitado);
            }
        }

        if ("alta-td-virtual".equals(funcionalidad)) {
            respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", smsHabilitado);
        }

        if ("recupero-usuario-clave".equals(funcionalidad)) {
            if (contexto.esProspecto() && !contexto.esProcrear(contexto) && !contexto.esTasaCero(contexto))
                return respuesta;

            if (TransmitHB.isChallengeOtp(contexto, "recupero-usuario-clave"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                boolean esClienteExterior = esClienteExterior(contexto);
                boolean tieneTodasTarjetasVirtuales = tieneTodasTarjetasVirtuales(contexto);

                if (esClienteExterior || tieneTodasTarjetasVirtuales)
                    respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);

                if (softTokenHabilitado && !softTokenBloqueado)
                    respuesta.add("validadores", "soft-token").add("validadoresUsuario", "soft-token", true);

                if (!esClienteExterior && "true".equals(ConfigHB.string("prendido_recupero_sms")))
                    respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);

                if (!esClienteExterior && !tieneTodasTarjetasVirtuales)
                    respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link",
                            enableToGetRedLinkPassword);
            }
        }

        if ("alta-ca-td-online".equals(funcionalidad)) {
            if (TransmitHB.isChallengeOtp(contexto, "alta-ca-td-online"))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
            }
        }

        if ("linea-roja".equals(funcionalidad)) {
            if (esClienteExterior(contexto))
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
            else {
                respuesta.add("validadores", "sms").add("validadoresUsuario", "sms", true);
                respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);
                if (!tieneTodasTarjetasVirtuales(contexto))
                    respuesta.add("validadores", "red-link").add("validadoresUsuario", "red-link",
                            enableToGetRedLinkPassword);
            }
        }

        if ("venta-dolares".equals(funcionalidad) && TransmitHB.isChallengeOtp(contexto, "venta-dolares"))
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);

        if ("venta-acciones-bonos".equals(funcionalidad) && TransmitHB.isChallengeOtp(contexto, "venta-acciones-bonos"))
            respuesta.add("validadores", "email").add("validadoresUsuario", "email", true);

        return respuesta;
    }

    public static Respuesta marcasCliente(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        respuesta.set("esMonoProductoTC", contexto.esMonoProductoTC());
        return respuesta;
    }

    public static Respuesta permitirSegundoFactorOtp(ContextoHB contexto) {
        Boolean acepto = contexto.parametros.bool("acepto");

        if (Objeto.anyEmpty(acepto)) {
            return Respuesta.parametrosIncorrectos();
        }

        if (acepto) {
            if (!contexto.validaSegundoFactor("datos-personales")) {
                return Respuesta.estado("REQUIERE_SEGUNDO_FACTOR");
            }
        }

        SqlResponse sqlResponse = RestPersona.permitirSegundoFactorOtp(contexto.idCobis(), acepto);
        if (sqlResponse.hayError) {
            return Respuesta.error();
        }
        contexto.limpiarSegundoFactor();
        return Respuesta.exito();
    }

    public static Respuesta buscarPersona(ContextoHB contexto) {
        String nombre = contexto.parametros.string("nombre");
        String apellido = contexto.parametros.string("apellido");
        String numeroDocumento = contexto.parametros.string("numeroDocumento");

        if (Objeto.anyEmpty(numeroDocumento)) {
            return Respuesta.parametrosIncorrectos();
        }

        ApiRequest request = Api.request("ConsultaPersonas", "personas", "GET", "/cuils", contexto);
        request.query("dni", numeroDocumento);

        ApiResponse response = Api.response(request, numeroDocumento);
        if (response.hayError()) {
            return Respuesta.error();
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
            return Respuesta.estado("PERSONA_NO_ENCONTRADA");
        }
        if (lista.size() > 1) {
            return Respuesta.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }

        String nombreCompleto = lista.get(0).string("apellidoYNombre");
        String cuil = lista.get(0).string("cuil");

        ApiRequest requestPersona = Api.request("ConsultaPersonasPorCuit", "personas", "GET", "/personas/{cuit}",
                contexto);
        requestPersona.path("cuit", cuil);

        ApiResponse responsePersona = Api.response(requestPersona, cuil);

        Objeto datosPersonales = new Objeto();
        datosPersonales.set("nombreCompleto", nombreCompleto);
        datosPersonales.set("cuil", cuil);
        datosPersonales.set("fechaNacimiento", responsePersona.date("fechaNacimiento", "yyyy-MM-dd", "dd/MM/yyyy"));
        datosPersonales.set("edad", responsePersona.integer("edad"));

        Respuesta respuesta = new Respuesta();
        respuesta.set("datosPersonales", datosPersonales);
        return respuesta;
    }

    public static Respuesta domicilioTarjetaCredito(ContextoHB contexto) {
        TarjetaCredito tarjetaCredito = contexto.tarjetaCreditoTitular();
        if (tarjetaCredito == null) {
            return Respuesta.estado("SIN_TARJETA_CREDITO_TITULAR");
        }

        ApiRequest request = Api.request("DireccionTarjetaCredito", "productos", "GET", "/v1/productos/{id}/domicilios",
                contexto);
        request.path("id", tarjetaCredito.numero());
        request.query("idCliente", contexto.idCobis());
        request.query("tipoProducto", "ATCDOMIC");

        ApiResponse response = Api.response(request, "80", contexto.idCobis(), tarjetaCredito.numero());
        if (response.hayError()) {
            return Respuesta.error();
        }

        String ciudad = "";
        try {
            ApiRequest requestDomicilio = Api.request("Domicilio", "personas", "GET", "/personas/{id}/domicilios",
                    contexto);
            requestDomicilio.header("x-usuario", ConfigHB.string("configuracion_usuario"));
            requestDomicilio.path("id", contexto.persona().cuit());
            requestDomicilio.permitirSinLogin = true;
            requestDomicilio.cacheSesion = true;
            ApiResponse responseDomicilio = Api.response(requestDomicilio, contexto.idCobis());

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

        return Respuesta.exito("domicilio", domicilio);
    }

    public static Respuesta dataValid(ContextoHB contexto) {
        try {
            Respuesta respuesta = new Respuesta();
            if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid")) {
                Calendar fechaActual = Calendar.getInstance();
                fechaActual.setTime(new Date());
                Date dFechaActual = fechaActual.getTime();
                ApiResponse dataValid = RestPersona.getDataValid(contexto);

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
                        contexto.sesion.dataValid = true;
                    }
                } catch (Exception e) {
                    //
                }
            }
            return respuesta;
        } catch (Exception e) {
            return Respuesta.estado("ERROR_DATA_VALID");
        }

    }

    private static boolean tieneTarjetasDebitoHabilitadas(ContextoHB contexto) {
        contexto.parametros.set("traerTarjetasVirtuales", true);
        Respuesta respuestaTarjetas = HBTarjetas.tarjetaDebitoHabilitadaRedLink(contexto);
        return !respuestaTarjetas.hayError() && respuestaTarjetas.get("tarjetasDebito") != null;
    }

    public static Respuesta situacionLaboral(ContextoHB contexto) {
        Respuesta respuesta = new Respuesta();
        SituacionLaboral actividadLaboralActual = SituacionLaboral.situacionLaboralPrincipal(contexto);

        respuesta.set("ingresos", new Objeto().set("ingresoMinimo", ConfigHB.bigDecimal("monto_minimo_carga_ingresos"))
                .set("ingresoMinimoFormateado", Formateador.importe(ConfigHB.bigDecimal("monto_minimo_carga_ingresos")))
                .set("ingresoMaximo", ConfigHB.bigDecimal("monto_maximo_carga_ingresos")).set("ingresoMaximoFormateado",
                        Formateador.importe(ConfigHB.bigDecimal("monto_maximo_carga_ingresos"))));

        Objeto listaActividadesLaborales = HBCatalogo.situacionesLaborales(contexto);

        for (Objeto actividad : listaActividadesLaborales.objetos("situaciones")) {
            if (actividad.string("id").equals(actividadLaboralActual.idSituacionLaboral)) {
                actividad.set("actividadPrincipal", true);
            }
            respuesta.add("situaciones", actividad);
        }

        return respuesta;
    }

    public static Boolean tieneOpcionAdelanto(ContextoHB contexto) {

        if (ConfigHB.bool("prendido_adelanto_bh") && !RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false)
                && !contexto.tienePPProcrearDesembolsado()) {
            Boolean acreditacionesActivas = HBProducto.acreditacionesHaberes(contexto);
            Boolean preAprobadoAdelanto = HBConsolidado.ofertaPreAprobada(contexto).existe("adelantoBH");

            // Adelanto BH 1 a 1.
            if (!contexto.persona().esEmpleado() && contexto.esPlanSueldo() && acreditacionesActivas) {
                return true;
            }

            // Adelanto BH para preaprobados con planSueldo
            if (preAprobadoAdelanto && contexto.esPlanSueldo()) {
                return true;
            }

            // Adelanto BH para preaprobados tipo jubilados
            // si es jubilado, pero no tiene cuenta con categoria b, no
            // puede tomar el
            // preaprobado
            return ConfigHB.bool("prendido_adelanto_jubilados") && preAprobadoAdelanto && contexto.esJubilado()
                    && contexto.tieneCuentaCategoriaB();
        }

        return false;
    }

    public static String obtenerCuitDeDocumento(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String nombres = contexto.parametros.string("nombres");
        String apellidos = contexto.parametros.string("apellidos");

        ApiResponse response = RestPersona.consultarPersonaCuitPadron(contexto, documento);

        if (response == null || response.hayError()) {
            return "";
        }

        List<Objeto> lista = new ArrayList<>();
        for (Objeto item : response.objetos()) {
            if (item.string("apellidoYNombre").toUpperCase().contains(nombres.toUpperCase())
                    && item.string("apellidoYNombre").toUpperCase().contains(apellidos.toUpperCase()))
                lista.add(item);
        }

        if (lista.size() != 1)
            return "";

        return lista.get(0).string("cuil");
    }

    public static Respuesta buscarPersonaRenaper(ContextoHB contexto) {

        String idTramite = contexto.parametros.string("idTramite");
        String dni = contexto.parametros.string("documento");
        String sexo = contexto.parametros.string("idSexo");

        ApiResponse response = RestVivienda.validarRenaper(contexto, idTramite, dni, sexo);

        Respuesta respuesta = new Respuesta();

        respuesta.set("idVersionDocumento", response.string("ejemplar").toUpperCase());
        respuesta.set("fechaNacimiento", response.date("fecha_nacimiento", "yyyy-MM-dd", "dd-MM-yyyy"));

        ApiResponse datosPais = RestCatalogo.paises(contexto);

        if (datosPais.hayError()) {
            return Respuesta.error();
        }

        datosPais.ordenar("descripcion");
        for (Objeto dato : datosPais.objetos()) {
            if (dato.string("descripcion").equals(response.get("pais"))) {
                respuesta.set("idPaisResidencia", dato.string("id"));
            }
        }

        return respuesta;
    }

    /**
     * Valida que el usuario sólo tenga tarjetas virtuales
     *
     * @param contexto
     * @return booleano.
     */
    private static boolean tieneTodasTarjetasVirtuales(ContextoHB contexto) {
        List<TarjetaDebito> tarjetas = contexto.tarjetasDebito();
        return !tarjetas.isEmpty() && tarjetas.stream().allMatch(TarjetaDebito::virtual);
    }

    private static boolean esClienteExterior(ContextoHB contexto) {
        return RestPersona.existeMuestreo("habilitar.mail.login", "true", contexto.idCobis()) || RestPersona.existeMuestreo("habilitar.mail.transferencias", "true", contexto.idCobis());
    }

    private static boolean esClienteExteriorOTdv(ContextoHB contexto) {
        return tieneTodasTarjetasVirtuales(contexto) || esClienteExterior(contexto);
    }

    public static Respuesta esClienteExteriorOrTdv(ContextoHB contexto) {
        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();
        return new Respuesta().set("esClienteExteriorOrTdv", esClienteExteriorOTdv(contexto));
    }

    public static Object personaTercero(ContextoHB ctx) {
        String cuil = ctx.parametros.string("cuil");

        ApiResponse respuestaPersona = RestPersona.consultarPersonaEspecifica(ctx, cuil);
        if (respuestaPersona.hayError()) {
            return Respuesta.error();
        }

        String tipoDocumento = "11".equals(respuestaPersona.string("idTipoIDTributario").trim()) ? "CUIT" : "CUIL";

        return Respuesta.exito().set("nombreCompleto", Texto.primerasMayuscula(respuestaPersona.string("nombres") + " " + respuestaPersona.string("apellidos"))).set("tipoDocumento", tipoDocumento);
    }

    public static Respuesta tieneBloqueoOperaciones(ContextoHB ctx) {
        boolean pedirDocExtraordinaria = !ctx.tieneContador("GUARDO_DOC_EXTRAORDINARIA");

        try {
            if (!HBAplicacion.funcionalidadPrendida(ctx.idCobis(), "prendido_bloqueo_base_negativa")) {
                return Respuesta.exito()
                        .set("segurosHabilitado", true)
                        .set("pedirDocExtraordinaria", pedirDocExtraordinaria);
            }

            ApiRequest requestBaseNegativa = Api.request("BasesNegativas", "personas", "GET", "/basesNegativas/personas", ctx);
            requestBaseNegativa.query("idSolicitante", ctx.idCobis());
            requestBaseNegativa.query("tipoDocumento", ctx.persona().idTipoDocumentoString());
            requestBaseNegativa.query("nroDocumento", ctx.persona().numeroDocumento());
            requestBaseNegativa.query("sexo", ctx.persona().idSexo());
            requestBaseNegativa.query("idTributario", ctx.persona().tipoTributario());
            requestBaseNegativa.query("nroTributario", ctx.persona().cuit());
            requestBaseNegativa.cacheSesion = true;

            boolean UPCLDNOCI = false;
            boolean UPCLDLeGA = false;

            ApiResponse responseBaseNegativa = Api.response(requestBaseNegativa, ctx.idCobis());
            if (!responseBaseNegativa.hayError()) {
                for (Objeto item : responseBaseNegativa.objetos()) {
                    if (item.string("referencia").equals("UPCLDNOCI")) {
                        UPCLDNOCI = true;
                    }
                    if (item.string("referencia").equals("UPCLDLEGA")) {
                        UPCLDLeGA = true;
                    }
                }
            }

            if (!UPCLDNOCI && !UPCLDLeGA) {
                return Respuesta.exito()
                        .set("segurosHabilitado", true)
                        .set("pedirDocExtraordinaria", pedirDocExtraordinaria);
            }

            boolean tieneDocIngreso = ctx.tieneContador("GUARDO_DOC_INGRESO");
            if (tieneDocIngreso) {
                return Respuesta.estado("PENDIENTE_APROBACION")
                        .set("segurosHabilitado", !UPCLDNOCI)
                        .set("pedirDocExtraordinaria", pedirDocExtraordinaria);
            }

            return Respuesta.estado("REQUIERE_DOCUMENTACION")
                    .set("segurosHabilitado", !UPCLDNOCI)
                    .set("pedirDocExtraordinaria", pedirDocExtraordinaria);
        } catch (Exception e) {
            return Respuesta.exito()
                    .set("segurosHabilitado", true)
                    .set("pedirDocExtraordinaria", pedirDocExtraordinaria);
        }
    }

    public static Respuesta obtenerDatosClientesTransmit(ContextoHB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

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

        return Respuesta.exito("tipoCliente",
                Transmit.generarTipoClienteEncriptado(
                        ConfigHB.string("clave_secreta_transmit", ""),
                        esClienteExterior,
                        tieneTodasTarjetasVirtuales,
                        esMonoproducto));
    }

    public static Respuesta dataValidOtp(ContextoHB contexto) {
        try {
            if (contexto == null || contexto.idCobis() == null) {
                return Respuesta.estado("SIN_SESION");
            }

            if (!HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_data_valid_otp")) {
                return Respuesta.exito();
            }

            Calendar fechaActual = Calendar.getInstance();
            fechaActual.setTime(new Date());
            Date dFechaActual = fechaActual.getTime();

            boolean emailVencido = false;
            boolean telefonoVencido = false;

            ApiResponse dataValid = RestPersona.getDataValid(contexto);
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
                return Respuesta.exito();
            }

            String fechaDesde = Fecha.restarDias(new Date(), 30L, "yyyy-MM-dd");
            List<Objeto> eventos = contexto.tieneContador("ALERTA_DATAVALID_OTP", fechaDesde);
            for (Objeto evento : eventos) {
                Date fechaEvento = evento.date("momento");
                if (Fecha.esFechaActual(fechaEvento)) {
                    return Respuesta.exito();
                }
            }

            contexto.insertarContador("ALERTA_DATAVALID_OTP");

            return Respuesta.estado("VENCIDO")
                    .set("emailVencido", emailVencido)
                    .set("telefonoVencido", telefonoVencido)
                    .set("bloquearModal", eventos.size() >= 3);
        } catch (Exception e) {
            return Respuesta.estado("ERROR_DATA_VALID");
        }
    }

    /**
     * Valida contra Transmit
     *
     * @param contexto
     * @param funcionalidad
     * @return respuesta.
     */
    private static Respuesta recomendacionTransmit(ContextoHB contexto, String funcionalidad) {
        if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_modo_transaccional_datos_personales",
                "prendido_modo_transaccional_datos_personales_cobis") && !TransmitHB.isChallengeOtp(contexto, funcionalidad)) {
            try {
                String sessionToken = contexto.parametros.string(Transmit.getSessionToken(), null);
                if (Objeto.empty(sessionToken))
                    return Respuesta.parametrosIncorrectos();

                ChangeDataHBBMBankProcess changeDataHBBMBankProcess = new ChangeDataHBBMBankProcess(contexto.idCobis(), sessionToken, BankProcessChangeDataType.ACCOUNT_DETAILS_CHANGE);

                return TransmitHB.recomendacionTransmit(contexto, changeDataHBBMBankProcess, funcionalidad);
            } catch (Exception e) {
                return Respuesta.exito();
            }
        }
        return Respuesta.exito();
    }

    public static Respuesta mostrarModalTcv(ContextoHB contexto) {
        boolean mostrarModalTcv = false;
        if (!contexto.tieneContador("GUARDO_ADICIONALES_TCV")) {
            mostrarModalTcv = tieneAltaTcv(contexto) && contexto.logsLogin().size() >= 3;
        }

        return Respuesta.exito("mostrarModalTcv", mostrarModalTcv);
    }

    private static boolean tieneAltaTcv(ContextoHB contexto) {
        SqlRequest request = Sql.request("GetSesionTcv", "esales");
        String sql = "SELECT TOP 1 cuil ";
        sql += "FROM [esales].[dbo].[Sesion] t1 WITH (NOLOCK) ";
        sql += "INNER JOIN [esales].[dbo].[Sesion_Esales_BB2] t2 WITH (NOLOCK) ";
        sql += "ON t1.id = t2.sesion_id ";
        sql += "WHERE t1.fecha_ultima_modificacion > ? ";

        if (contexto.esProduccion()) {
            sql += "AND t2.sucursal_onboarding LIKE '%FLUJO_TCV%' ";
        }

        sql += "AND t1.estado = 'FINALIZAR_OK' ";
        sql += "AND t1.cuil = ? ";

        request.sql = sql;
        request.add(ar.com.hipotecario.backend.base.Fecha.ahora().restarDias(30).toString());
        request.add(contexto.persona().cuit());
        SqlResponse response = Sql.response(request);
        return response.registros.size() > 0;
    }

    public static Respuesta guardarAdicionalesTcv(ContextoHB contexto) {
        String idEstadoCivil = contexto.parametros.string("idEstadoCivil");
        Objeto conyuge = contexto.parametros.objeto("conyuge");

        if ("C".equals(idEstadoCivil)
                && contexto.persona().cuit().contains(conyuge.string("numeroDocumento"))) {
            return Respuesta.estado("CONYUGE_INVALIDO");
        }

        if ("S".equals(contexto.persona().idEstadoCivil()) && "S".equals(idEstadoCivil)) {
            contexto.insertarContador("GUARDO_ADICIONALES_TCV");
            return Respuesta.exito();
        }

        guardarAdicionalPersona(contexto, idEstadoCivil, conyuge);

        if (!"C".equals(contexto.persona().idEstadoCivil()) && "C".equals(idEstadoCivil)) {
            Respuesta resGuardarConyuge = guardarConyuge(contexto, conyuge);
            if (!"0".equals(resGuardarConyuge.string("estado"))) {
                return resGuardarConyuge;
            }
        }

        contexto.insertarContador("GUARDO_ADICIONALES_TCV");
        return Respuesta.exito();
    }

    private static Respuesta guardarConyuge(ContextoHB contexto, Objeto conyuge) {
        String numeroDocumento = conyuge.string("numeroDocumento");
        if (numeroDocumento.length() == 7) {
            numeroDocumento = "0" + numeroDocumento;
        }

        String cuilConyuge = RestPersona.buscarCuil(contexto, numeroDocumento, conyuge.string("genero"));
        if (cuilConyuge == null) {
            return Respuesta.estado("CUIL_NO_ENCONTRADO");
        }

        if (cuilConyuge.equals(RestPersona.cuitConyuge(contexto))) {
            return Respuesta.exito();
        }

        ApiResponse personaConyuge = RestPersona.consultarPersonaEspecifica(contexto, cuilConyuge);
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
        ApiRequest request = Api.request("PersonasRelacionadas", "personas", "GET", "/personas/{cuit}/relaciones", contexto);
        request.path("cuit", contexto.persona().cuit());
        ApiResponse resRelaciones = Api.response(request);
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
            RestPersona.actualizarRelacionPersona(contexto, idRelacion, "2", cuilConyuge, contexto.idCobis(), null, null, ar.com.hipotecario.backend.base.Fecha.hoy().toString() + "T00:00:00");
        }

        return Respuesta.exito();
    }

    private static void guardarAdicionalPersona(ContextoHB contexto, String idEstadoCivil, Objeto conyuge) {
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

    public static Respuesta obtenerDatosNuevoClientesTransmit(ContextoHB contexto) {
        if (Objeto.empty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

        Futuro<Persona> futuroPersona = new Futuro<>(contexto::persona);

        Boolean esClienteExterior;
        Boolean tieneTodasTarjetasVirtuales = false;
        Boolean esMonoproducto = false;

        Futuro<Boolean> futuroClienteExterior = new Futuro<>(() -> esClienteExterior(contexto));
        Futuro<Boolean> futuroTarjetasVirtuales = new Futuro<>(() -> tieneTodasTarjetasVirtuales(contexto));
        Futuro<Boolean> futuroMonoproducto = new Futuro<>(contexto::esMonoProductoTC);

        Persona persona = futuroPersona.tryGet();
        if (Objeto.empty(persona))
            return Respuesta.estado(Errores.ERROR_PERSONA_NO_ENCONTRADA);

        Futuro<Objeto> futuroTelefono = new Futuro<>(() -> RestPersona.celular(contexto, persona.cuit()));

        esClienteExterior = futuroClienteExterior.tryGet();
        if (!esClienteExterior) {
            tieneTodasTarjetasVirtuales = futuroTarjetasVirtuales.tryGet();
            if (!tieneTodasTarjetasVirtuales)
                esMonoproducto = futuroMonoproducto.tryGet();
        }

        Objeto telefono = futuroTelefono.tryGet();

        if (Objeto.empty(telefono))
            return Respuesta.estado(Errores.ERROR_TELEFONO_NO_ENCONTRADA);

        return Respuesta.exito("datos",
                Transmit.generarDatosClienteEncriptado(
                        ConfigHB.string("clave_secreta_transmit", ""),
                        String.format("%s%s", persona.idSexo(), persona.numeroDocumento()),
                        obtenerTelefono(telefono),
                        Fecha.dateToStringFormat(persona.fechaNacimiento(), "dd/MM/yyyy"),
                        persona.email(),
                        esClienteExterior,
                        tieneTodasTarjetasVirtuales,
                        esMonoproducto));
    }

    private static String obtenerTelefono(Objeto telefono) {
        return String.format("+%s9%s%s%s",
                telefono.string("codigoPais").replaceFirst("^0+(?!$)", ""),
                telefono.string("codigoArea").replaceFirst("^0+(?!$)", ""),
                telefono.string("caracteristica"),
                telefono.string("numero"));
    }
    public static Respuesta alertaSos(ContextoHB ctx) {
        if(!HBAplicacion.funcionalidadPrendida(ctx.idCobis(), "prendido_alerta_sos")){
            return Respuesta.exito();
        }

        if(ctx.persona().esPersonaJuridica() || ctx.persona().esEmpleado()){
            return Respuesta.exito();
        }

        List<Objeto> alertas = obtenerAlertaSos(ctx);
        if(alertas.isEmpty()){
            return Respuesta.exito();
        }

        Date fechaInicio = null;
        for(Objeto alerta : alertas){
            if(fechaInicio == null || alerta.date("FECHA_ALTA_ALERTA").before(fechaInicio)){
                fechaInicio = alerta.date("FECHA_ALTA_ALERTA");
            }
        }

        if(fechaInicio == null){
            return Respuesta.error();
        }

        boolean tieneDocIngresos = !ctx.tieneContador("GUARDO_DOC_INGRESO", new SimpleDateFormat("yyyy-MM-dd").format(fechaInicio)).isEmpty();
        boolean tieneDocExtraordinaria = !ctx.tieneContador("GUARDO_DOC_EXTRAORDINARIA", new SimpleDateFormat("yyyy-MM-dd").format(fechaInicio)).isEmpty();
        if(tieneDocIngresos && tieneDocExtraordinaria){
            return Respuesta.exito();
        }

        try{
            ApiResponse resDocumentos = RestArchivo.digitalizacionGetDocumentos(ctx);
            if (!resDocumentos.hayError()) {

                for (Objeto doc : resDocumentos.objetos()) {
                    if (tieneDocIngresos && tieneDocExtraordinaria) break;

                    Date fechaDoc = doc.date("fechaModificacion", "yyyy-MM-dd'T'HH:mm:ss");
                    if (doc.string("descripcionClase").equals("IngresosH")) {
                        if (fechaDoc.after(fechaInicio)) {
                            tieneDocIngresos = true;
                        }
                    } else if (doc.string("descripcionClase").equals("DocumentacionRespaldatoriaPrevencionH")) {
                        if (fechaDoc.after(fechaInicio)) {
                            tieneDocExtraordinaria = true;
                        }
                    }
                }
            }
        } catch (Exception e) {}

        if(tieneDocIngresos && tieneDocExtraordinaria){
            return Respuesta.exito();
        }

        Respuesta respuesta = Respuesta.estado("PENDIENTE_DOC");
        if(!tieneDocIngresos){
            Objeto dato = new Objeto();
            dato.set("clase", "IngresosH");
            dato.set("descripcion", "Actualizá la documentación de ingresos");
            respuesta.add("faltantes", dato);
        }

        if(!tieneDocExtraordinaria){
            Objeto dato = new Objeto();
            dato.set("clase", "DocumentacionRespaldatoriaPrevencionH");
            dato.set("descripcion", "Completá la documentación respaldatoria");
            respuesta.add("faltantes", dato);
        }

        return respuesta;
    }

    private static List<Objeto> obtenerAlertaSos(ContextoHB ctx) {
        SqlRequest sqlRequest = Sql.request("bandejaAlertaSos", "hbs");
        sqlRequest.sql += "SELECT CODIGO_ALERTA, SUCURSAL, FECHA_ALTA_ALERTA, FECHA_VTO_ALERTA FROM [hbs].[dbo].[bandeja_alertas_generadas_SOS] WITH (NOLOCK) WHERE CUIL = ? AND FECHA_VTO_ALERTA > GETDATE() ";
        sqlRequest.parametros.add(ctx.persona().cuit());

        SqlResponse response = Sql.response(sqlRequest);
        return response.hayError ? new ArrayList<>() : response.registros;
    }

}
