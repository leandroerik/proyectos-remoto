package ar.com.hipotecario.canal.homebanking.api;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

import ar.com.hipotecario.backend.Eventos;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.response.GeneroResponse;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Emails;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.RequestMigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.seguridad.MigracionUsuario.ResponseMigracionUsuario;
import ar.com.hipotecario.backend.servicio.api.transmit.*;
import ar.com.hipotecario.backend.servicio.sql.SqlHomeBanking;
import ar.com.hipotecario.backend.servicio.sql.SqlMobile;
import ar.com.hipotecario.backend.servicio.sql.homebanking.AccesoBiometriaHB.AccesoBiometria;
import ar.com.hipotecario.backend.servicio.sql.mobile.RegistroDispositivoMobile.RegistroDispositivo;
import ar.com.hipotecario.backend.servicio.sql.mobile.SoftTokenMobile.SoftToken;
import ar.com.hipotecario.backend.util.Errores;
import ar.com.hipotecario.backend.util.MapperUtil;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.endpoints.EHBLogin;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.CuentaComitente;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.servicio.*;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.gabrielsuarez.glib.G;
import ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersonas;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaRequest;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.UsuarioLibreriaResponse;
import ar.com.hipotecario.backend.servicio.api.transmit.LibreriaFraudes.DatosAdicionales;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;

public class HBLogin {

    private static final Logger log = LoggerFactory.getLogger(HBLogin.class);

    public static Respuesta login(ContextoHB contexto) {
        if (Util.migracionCompleta())
            return loginTransmit(contexto);
        return loginIsva(contexto);
    }

    private static Respuesta loginIsva(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String usuario = contexto.parametros.string("usuario");
        String clave = contexto.parametros.string("clave");
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        Boolean buscarProductos = contexto.parametros.bool("buscarProductos", false);
        Boolean validarDobleSesion = contexto.parametros.bool("validarDobleSesion", true);
        Boolean buscarConfiguracion = contexto.parametros.bool("buscarConfiguracion", true);

        if (!fingerprint.contains("-"))
            fingerprint = UUID.randomUUID().toString();

        eliminarSesion(contexto, "", false);

        if (Objeto.anyEmpty(documento))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaCobis = obtenerIdCobis(contexto, documento, idTipoDocumento, idSexo);
        if (respuestaCobis.hayError())
            return respuestaCobis;

        String idCobis = respuestaCobis.string("cobis");

        setearCobisSesion(contexto, idCobis);
        Eventos.limpiar(idCobis);
        contexto.sesion.save();

        if (Objeto.anyEmpty(usuario, clave))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuestaChequearMigracion = chequearMigracion(contexto, documento, idSexo, fingerprint);
        if (respuestaChequearMigracion != null)
            return respuestaChequearMigracion;

        Futuro<ApiResponse> futuroPerfilInversor = new Futuro<>(() -> RestPersona.perfilInversor(contexto));
        List<Futuro<ApiResponse>> futurosIntegrantes = new ArrayList<>();
        for (CuentaComitente item : contexto.cuentasComitentes()) {
            Futuro<ApiResponse> futuroIntegrante = new Futuro<>(() -> ProductosService.integrantesProducto(contexto, item.numero()));
            futurosIntegrantes.add(futuroIntegrante);
        }

        // Desarrollo para cambio de usuario forzoso para algunos Cobis seleccionados
        Boolean esUsuarioMarcado = EHBLogin.esUsuarioMarcado(idCobis);
        if (esUsuarioMarcado) {
            Objeto consultaForzar = EHBLogin.datosCambioUsuario(idCobis);
            if (consultaForzar != null && !consultaForzar.bool("realizado")) {
                Boolean consultaST = consultaForzar.bool("token");
                if (consultaST != null && consultaST) {
                    return Respuesta.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", true);
                } else {
                    return Respuesta.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", false);
                }
            }
        }
        // LLAMADAS A SERVICIOS EN PARALELO
        final String fingerprintFinal = fingerprint;
        boolean isOn = HBAplicacion.funcionalidadPrendida(idCobis, "prendido_monitoreo_transaccional");
        Futuro<Respuesta> futuroNull = new Futuro<>(() -> null);

        String idCobisFinal = idCobis;
        Futuro<Boolean> futuroExisteSesion = new Futuro<>(
                () -> SqlHomebanking.existeSesion(idCobisFinal, fingerprintFinal));
        Futuro<Boolean> futuroBloqueadoPorFraude = new Futuro<>(() -> SqlHomebanking.bloqueadoPorFraude(idCobisFinal));
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = new Futuro<>(
                () -> isOn && SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobisFinal));
        Futuro<ApiResponse> futuroValidarUsuario = new Futuro<>(
                () -> RestSeguridad.validarUsuario(contexto, usuario, fingerprintFinal));
        Futuro<ApiResponse> futuroValidarClave = new Futuro<>(
                () -> RestSeguridad.validarClavev2(contexto, clave, fingerprintFinal));
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(
                () -> SqlHomebanking.fechaHoraUltimaConextion(idCobisFinal));
        Futuro<Respuesta> futuroConfiguracionUsuario = futuroNull;
        Futuro<Respuesta> futuroPersona = futuroNull;
        Futuro<Respuesta> futuroProductos = futuroNull;
        if (buscarConfiguracion) {
            futuroConfiguracionUsuario = new Futuro<>(() -> HBAplicacion.configuracionUsuario(contexto, idCobisFinal));
        }
        if (buscarPersona) {
            futuroPersona = new Futuro<>(() -> HBPersona.persona(contexto));
        }
        if (buscarProductos) {
            futuroProductos = new Futuro<>(() -> HBProducto.productos(contexto));
        }

        // Verifico sesion duplicada
        if (validarDobleSesion) {
            if (ConfigHB.bool("validar_sesion_duplicada", false)) {
                if (futuroExisteSesion.get()) {
                    contexto.eliminarSesion(false, idCobis);
                    return Respuesta.estado("EXISTE_SESION");
                }
            }
        }

        // Verifico bloqueo por fraude
        if (futuroBloqueadoPorFraude.get()) {
            contexto.eliminarSesion(false, idCobis);
            return Respuesta.estado("USUARIO_BLOQUEADO_POR_FRAUDE");
        }

        if (futuroBloqueadoPorTransaccionFraudeLink.get()) {
            contexto.eliminarSesion(false, idCobis);
            return Respuesta.estado("USUARIO_BLOQUEADO_POR_FRAUDE_LINK");
        }
        // Validar Usuario
        ApiResponse validarUsuario = futuroValidarUsuario.get();
        if (validarUsuario.hayError()) {
            String error = "ERROR";
            error = validarUsuario.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO"
                    : error;
            error = validarUsuario.string("detalle").contains("Clave incorrecta") ? "USUARIO_INVALIDO" : error;
            error = validarUsuario.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarUsuario.string("detalle").contains("Maximum authentication attempts exceeded")
                    ? "USUARIO_BLOQUEADO"
                    : error;
            error = validarUsuario.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

            error = validarUsuario.string("mensajeAlUsuario").contains("The password authentication failed")
                    ? "USUARIO_INVALIDO"
                    : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO"
                    : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded")
                    ? "USUARIO_BLOQUEADO"
                    : error;
            error = validarUsuario.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA"
                    : error;

            if (validarUsuario.string("mensajeAlDesarrollador").contains("Clave incorrecta")) {
                String mensajeCanal = validarUsuario.string("mensajeAlDesarrollador")
                        .replace("Clave incorrecta. Canal ", "");
                String canal = mensajeCanal.split("-")[0].trim();
                String fechaCambio = mensajeCanal.split("-")[1].trim();
                return Respuesta.estado(error)
                        .set("mensaje",
                                ConfigHB.string("mensaje_password_invalida", "La password ingresada es incorrecta."))
                        .set("canal", canal).set("fechaCambio", fechaCambio);
            }
            return Respuesta.estado(error).set("mensaje",
                    ConfigHB.string("mensaje_usuario_invalido", "El usuario ingresado es incorrecto."));
        }

        ApiResponse validarClave = futuroValidarClave.get();
        if (validarClave.hayError()) {
            String error = "ERROR";
            error = validarClave.string("detalle").contains("The password authentication failed") ? "USUARIO_INVALIDO"
                    : error;
            error = validarClave.string("detalle").contains("Clave incorrecta") ? "USUARIO_INVALIDO" : error;
            error = validarClave.string("detalle").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("detalle").contains("Maximum authentication attempts exceeded")
                    ? "USUARIO_BLOQUEADO"
                    : error;
            error = validarClave.string("detalle").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

            error = validarClave.string("mensajeAlUsuario").contains("The password authentication failed")
                    ? "USUARIO_INVALIDO"
                    : error;
            error = validarClave.string("mensajeAlUsuario").contains("is now locked out") ? "USUARIO_BLOQUEADO" : error;
            error = validarClave.string("mensajeAlUsuario").contains("Maximum authentication attempts exceeded")
                    ? "USUARIO_BLOQUEADO"
                    : error;
            error = validarClave.string("mensajeAlUsuario").contains("password has expired") ? "CLAVE_EXPIRADA" : error;

            if (validarClave.string("mensajeAlDesarrollador").contains("Clave incorrecta")) {
                String mensajeCanal = validarClave.string("mensajeAlDesarrollador").replace("Clave incorrecta. Canal ",
                        "");
                String canal = mensajeCanal.split("-")[0].trim();
                String fechaCambio = mensajeCanal.split("-")[1].trim();
                return Respuesta.estado(error)
                        .set("mensaje",
                                ConfigHB.string("mensaje_password_invalida", "La password ingresada es incorrecta."))
                        .set("canal", canal).set("fechaCambio", fechaCambio);
            }

            return Respuesta.estado(error).set("mensaje",
                    ConfigHB.string("mensaje_password_invalida", "La password ingresada es incorrecta."));
        }

        // Fecha hora ultima conexion
        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.get();
        contexto.sesion.fechaHoraUltimaConexion = (fechaHoraUltimaConexion);
        new Futuro<Boolean>(() -> SqlHomebanking.registrarFechaHoraUltimaConextion(idCobisFinal,
                ConfigHB.string("modo_ultima_conexion", "rowlock")));

        // Registrar Sesion BD
        if (ConfigHB.bool("validar_sesion_duplicada", false)) {
            new Futuro<Boolean>(
                    () -> SqlHomebanking.registrarSesion(idCobisFinal, fingerprintFinal, contexto.idSesion()));
        }

        // Respuesta
        Respuesta respuesta = new Respuesta();
        respuesta.set("fingerprint", fingerprint);

        if (buscarConfiguracion) {
            respuesta.unir(futuroConfiguracionUsuario.get());
        }
        if (buscarPersona) {
            respuesta.unir(futuroPersona.get());
        }
        if (buscarProductos) {
            futuroProductos.get();
        }

        try {
            new Futuro<>(() -> mailIngresoUsuario(contexto, respuesta.string("persona.cuit"),
                    respuesta.string("persona.apellido"), respuesta.string("persona.nombre")));
        } catch (Exception e) {
        }

        contexto.sesion.usuarioLogueado = (true);
        contexto.sesion.ip = (contexto.ip());
        respuesta.set("hash", contexto.hashSesion());
        respuesta.set("personUuid", DatatypeConverter.printBase64Binary(idCobis.getBytes()));
        new Futuro<>(() -> contexto.insertarLogLogin(contexto, fingerprintFinal));

        futuroPerfilInversor.tryGet();
        for (Futuro<ApiResponse> futuroIntegrante : futurosIntegrantes) {
            futuroIntegrante.tryGet();
        }

        return respuesta;
    }

    public static Boolean mailIngresoUsuario(ContextoHB contexto, String cuit, String apellido, String nombre)
            throws Exception {
        try {
            boolean isSalesforce = HBSalesforce.prendidoSalesforce(contexto.idCobis());

            if (cuit == null || cuit.isEmpty()) {
                ApiResponse persona = RestPersona.clientes(contexto);
                cuit = persona.objetos().get(0).string("cuit");
                apellido = persona.objetos().get(0).string("apellidos");
                nombre = persona.objetos().get(0).string("nombres");
            }


            boolean enviarMail = RestNotificaciones.consultaConfiguracionAlertasEspecifica(contexto, "A_ACC");
            if (!isSalesforce && !enviarMail)
                return false;

            String emailDestino = RestPersona.direccionEmail(contexto, cuit);
            if (emailDestino == null || "".equals(emailDestino))
                return false;

            if (emailDestino != null && !emailDestino.isEmpty()) {
                ApiRequest requestMail = Api.request("LoginCorreoElectronico", "notificaciones", "POST",
                        "/v1/correoelectronico", contexto);
                requestMail.body("de", "aviso@mail-hipotecario.com.ar");
                requestMail.body("para", emailDestino);
                requestMail.body("plantilla", ConfigHB.string("doppler_acceso_usuario"));
                requestMail.permitirSinLogin = true;
                Objeto parametros = requestMail.body("parametros");
                parametros.set("Subject", "Ingreso a BH");
                parametros.set("NOMBRE", nombre);
                parametros.set("APELLIDO", apellido);
                Date hoy = new Date();
                parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
                parametros.set("HORA", new SimpleDateFormat("hh:mm a").format(hoy));
                parametros.set("CANAL", "Home Banking");
                parametros.set("TITULAR_CANAL", apellido);

                if (isSalesforce) {
                    parametros.set("ENVIO_MAIL", enviarMail);
                    parametros.set("idcobis", contexto.idCobis());
                    String salesforce_login = ConfigHB.string("salesforce_login");
                    new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, salesforce_login, parametros));
                } else {
                    new Futuro<>(() -> Api.response(requestMail, new Date().getTime()));
                }
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static Respuesta pseudoLogin(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String idTipoDocumento = contexto.parametros.string("idTipoDocumento", null);
        String idSexo = contexto.parametros.string("idSexo", null);

        contexto.eliminarSesion(false, "");

        if (Objeto.anyEmpty(documento)) {
            return Respuesta.parametrosIncorrectos();
        }

        // idCobis
        List<String> listaIdCobis = RestPersona.listaIdCobis(contexto, documento, idTipoDocumento, idSexo);
        if (listaIdCobis == null) {
            return Respuesta.estado("USUARIO_NO_ENCONTRADO");
        } else if (listaIdCobis.isEmpty()) {
            return Respuesta.estado("USUARIO_INVALIDO");
        } else if (listaIdCobis.size() > 1) {
            return Respuesta.estado("MULTIPLES_PERSONAS_ENCONTRADAS");
        }
        String idCobis = listaIdCobis.get(0);

        // verifico que el usuario no haya sido bloqueado por link
        if (HBAplicacion.funcionalidadPrendida(idCobis, "prendido_monitoreo_transaccional")
                && SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobis)) {
            return Respuesta.estado("USUARIO_BLOQUEADO_POR_FRAUDE_LINK");
        }
        contexto.sesion.idCobis = (idCobis);
        contexto.sesion.save();
        new Futuro<>(() -> registrarHeaders(contexto, "pseudoLogin"));



        Respuesta respuesta = new Respuesta();
        respuesta.set("claveGenerada", true);
        respuesta.set("tieneClaveUsuario", true);
        respuesta.set("tieneClaveNumerica", true);
        respuesta.set("esProspecto", false);
        respuesta.set("prendidoRecuperoClave", true);
        respuesta.set("prendidoDuracionOtp", true);
        respuesta.set("prendidoCuentaEnRiesgo", true);
        // respuesta.set("monoproducto", !contexto.esMonoProductoTC());
        respuesta.set("tieneTD", contexto.tieneTD());

        respuesta.set("prendidoAltaUsuarioTransmit", HBAplicacion.funcionalidadPrendida("alta_usuario_transmit"));

        return respuesta;
    }

    public static Respuesta falsoLogin(ContextoHB contexto) {
        String usuario = contexto.parametros.string("usuario");
        Boolean buscarPersona = contexto.parametros.bool("buscarPersona", false);
        Boolean buscarProductos = contexto.parametros.bool("buscarProductos", true);
        Boolean buscarAcumar = contexto.parametros.bool("buscarAcumar", false);
        Boolean buscarConfiguracion = contexto.parametros.bool("buscarConfiguracion", true);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        if (!fingerprint.contains("-")) {
            fingerprint = UUID.randomUUID().toString();
        }

        if (Objeto.anyEmpty(usuario)) {
            return Respuesta.parametrosIncorrectos();
        }

        // SERVICIOS

        // Cliente

        // Productos

        // ConsultaContador

        // InsertLogLogin

        // SelectSugerirOtpSegundoFactor

        // SelectUltimaConexion

        // SelectUsuario

        // UpdateUltimaConexion

        // ESPERAR


        // LOGICA DE NEGOCIO
        Boolean esIdCobis = !usuario.matches(".*[a-zA-Z]+.*");
        String idCobis = esIdCobis ? usuario.trim() : SqlClientesOperadores.idCobis(usuario);
        if (idCobis == null) {
            return Respuesta.estado("CLAVES_NO_GENERADAS_SIN_HOMEBANKING");
        }
        contexto.sesion.idCobis = (idCobis);
        contexto.sesion.idCobisReal = (idCobis);
        Eventos.limpiar(idCobis);
        contexto.sesion.save();

        // cache para sesion
        Futuro<ApiResponse> futuroPerfilInversor = new Futuro<>(() -> RestPersona.perfilInversor(contexto));
        List<Futuro<ApiResponse>> futurosIntegrantes = new ArrayList<>();
        for (CuentaComitente item : contexto.cuentasComitentes()) {
            Futuro<ApiResponse> futuroIntegrante = new Futuro<>(() -> ProductosService.integrantesProducto(contexto, item.numero()));
            futurosIntegrantes.add(futuroIntegrante);
        }

        // Desarrollo para cambio de usuario forzoso para algunos Cobis seleccionados
        Boolean esUsuarioMarcado = EHBLogin.esUsuarioMarcado(idCobis);
        if (esUsuarioMarcado) {
            Objeto consultaForzar = EHBLogin.datosCambioUsuario(idCobis);
            if (consultaForzar != null && !consultaForzar.bool("realizado")) {
                Boolean consultaST = consultaForzar.bool("token");
                if (consultaST != null && consultaST) {
                    return Respuesta.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", true);
                } else {
                    return Respuesta.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", false);
                }
            }
        }
        Date fechaHoraUltimaConexion = SqlHomebanking.fechaHoraUltimaConextion(idCobis);
        contexto.sesion.fechaHoraUltimaConexion = (fechaHoraUltimaConexion);
        SqlHomebanking.registrarFechaHoraUltimaConextion(idCobis, ConfigHB.string("modo_ultima_conexion", "rowlock"));

        Respuesta respuesta = new Respuesta();
        respuesta.set("fingerprint", fingerprint);
        if (buscarPersona) {
            respuesta.unir(HBPersona.cliente(contexto));
        }
        if (buscarAcumar) {
            Objeto acumar = new Objeto();
            acumar.set("esAcumar", false);
            if (RestAcumar.esAcumar(idCobis)) {
                String estado = RestAcumar.estado(idCobis);
                acumar.set("esAcumar", true);
                acumar.set("permitirAlta", estado.equals("SV"));
                acumar.set("permitirResubirDocumentacion", estado.equals("RD") || estado.equals("R2"));
                acumar.set("estado", estado);
            }
            respuesta.set("acumar", acumar);
        }
        if (buscarProductos) {
            HBProducto.productos(contexto);
        }
        if (buscarConfiguracion) {
            respuesta.unir(HBAplicacion.configuracionUsuario(contexto, idCobis));
        }


        contexto.sesion.usuarioLogueado = (true);
        contexto.sesion.ip = (contexto.ip());
        if (respuesta.string("estado").equals("ERROR")) {
            return Respuesta.error();
        }
        contexto.insertarLogLogin(contexto, fingerprint);
        respuesta.set("hash", contexto.hashSesion());
        respuesta.set("personUuid", DatatypeConverter.printBase64Binary(idCobis.getBytes()));

        futuroPerfilInversor.tryGet();
        for (Futuro<ApiResponse> futuroIntegrante : futurosIntegrantes) {
            futuroIntegrante.tryGet();
        }

        return respuesta;
    }

    public static Respuesta logout(ContextoHB contexto) {
        contexto.eliminarSesion(true, contexto.idCobis());
        return Respuesta.exito();
    }

    public static Respuesta preguntasRiesgoNet(ContextoHB contexto) {
        contexto.sesion.respuestasRiesgoNet = new HashMap<>();

        Boolean bloqueadoRiesgoNet = contexto.bloqueadoRiesgoNet();
        if (bloqueadoRiesgoNet) {
            return Respuesta.estado("BLOQUEADO_RIESGONET");
        }

        Objeto domicilioPostal = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
        if (domicilioPostal == null) {
            domicilioPostal = new Objeto();
        }

        ApiRequest request = Api.request("PreguntasRiesgoNet", "personas", "GET", "/rnvconsulta", contexto);

        if (ConfigHB.esHomologacion()) {
            request.query("dni", "85777996");
            request.query("genero", "M");
            request.query("cuit", "20857779968");
            request.query("apellido", "DATOS DE ");
            request.query("nombre", "PRUEBA");
        } else {
            request.query("dni", contexto.persona().numeroDocumento());
            request.query("genero", contexto.persona().idSexo());
            request.query("cuit", contexto.persona().cuit());
            request.query("apellido", contexto.persona().apellido());
            request.query("nombre", contexto.persona().nombres());
        }

        request.query("fechaNacimiento",
                new SimpleDateFormat("dd/MM/yyyy").format(contexto.persona().fechaNacimiento()));
        request.query("provinciaP", domicilioPostal.string("provincia", "-"));
        request.query("localidadP", domicilioPostal.string("ciudad", "-"));
        request.query("calleP", domicilioPostal.string("calle", "-"));
        request.query("alturaP", domicilioPostal.string("numero", "0"));

        request.permitirSinLogin = true;

        ApiResponse response = Api.response(request, contexto.persona().numeroDocumento());
        if (response.hayError()) {
            return Respuesta.error();
        }

        Integer numeroPregunta = 0;
        Integer numeroOpcion = 0;
        Respuesta respuesta = new Respuesta();
        for (Objeto item0 : response.objetos()) {
            for (Objeto item1 : item0.objetos("oPreguntas")) {
                for (Objeto item2 : item1.objetos("preguntas")) {
                    for (Objeto item3 : item2.objetos("Pregunta")) {
                        String correcta = item3.objetos("ok").get(0).string("codigo");
                        Objeto pregunta = new Objeto();
                        pregunta.set("id", (++numeroPregunta).toString());
                        pregunta.set("enunciado", item3.string("enunciado"));
                        for (Objeto item4 : item3.objetos("opciones")) {
                            for (Objeto item5 : item4.objetos("opcion")) {
                                Integer id = ++numeroOpcion;
                                Boolean esCorrecta = correcta.equalsIgnoreCase(item5.string("codigo"));
                                Objeto opcion = new Objeto();
                                opcion.set("id", id.toString());
                                opcion.set("enunciado", item5.string("opcion1"));
                                pregunta.add("opciones", opcion);
                                if (esCorrecta) {
                                    contexto.sesion.respuestasRiesgoNet.put(numeroPregunta, id);
                                }
                            }
                        }
                        if (!ConfigHB.esProduccion()) {
                            respuesta.set("respuestas", contexto.sesion.respuestasRiesgoNet);
                        }
                        respuesta.add("preguntas", pregunta);
                        numeroOpcion = 0;
                    }
                }
            }
        }

        if (numeroPregunta == 0) {
            return Respuesta.estado("RECHAZADO_RIESGONET");
        }
        contexto.sesion.save();
        contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "P");
        contexto.insertLogPreguntasRiesgoNet(contexto, "PreguntasRiesgoNet", request.idProceso());
        return respuesta;
    }

    public static Respuesta responderPreguntasRiesgoNet(ContextoHB contexto) {
        Objeto respuestas = contexto.parametros;

        Boolean bloqueadoRiesgoNet = contexto.bloqueadoRiesgoNet();
        if (bloqueadoRiesgoNet) {
            return Respuesta.estado("BLOQUEADO_RIESGONET");
        }

        Integer cantidadRespuestasCorrectas = 0;
        for (Integer idPregunta : contexto.sesion.respuestasRiesgoNet.keySet()) {
            for (Objeto item : respuestas.objetos()) {
                if (idPregunta.equals(item.integer("idPregunta"))) {
                    Integer idRespuesta = contexto.sesion.respuestasRiesgoNet.get(idPregunta);
                    cantidadRespuestasCorrectas += idRespuesta.equals(item.integer("idRespuesta")) ? 1 : 0;
                    break;
                }
            }
        }

        Boolean validaRiesgoNet = false;
        Integer cantidadRespuestasIncorrectas = 5 - cantidadRespuestasCorrectas;

        if (cantidadRespuestasCorrectas < ConfigHB.integer("cantidad_respuestas_correctas_riesgonet", 4)) {
            SqlRequest sqlRequest = Sql.request("InsertRiesgoNet", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[riesgo_net] (idCobis, momento, respuestasIncorrectas) VALUES (?, GETDATE(), ?)";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(cantidadRespuestasIncorrectas);
            Sql.response(sqlRequest);

            bloqueadoRiesgoNet = contexto.bloqueadoRiesgoNet();
            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "R");

            if (bloqueadoRiesgoNet) {
                return Respuesta.estado("BLOQUEADO_RIESGONET");
            }
        } else {
            validaRiesgoNet = true;
            contexto.sesion.validaRiesgoNet = (true);
            contexto.sesion.save();
            Integer cantidadDiasBloqueo = ConfigHB.integer("cantidad_dias_bloqueo_riesgonet", 1);
            String inicio = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(
                    new java.sql.Date(new java.util.Date().getTime() - cantidadDiasBloqueo * 24 * 60 * 60 * 1000L));
            String fin = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new java.sql.Date(new java.util.Date().getTime()));
            SqlRequest sqlRequest = Sql.request("DeleteRiesgoNet", "homebanking");
            sqlRequest.sql = "DELETE [Homebanking].[dbo].[riesgo_net] WHERE idCobis = ? AND momento > ? AND momento < ?";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(inicio);
            sqlRequest.add(fin);
            Sql.response(sqlRequest);
            contexto.insertarLogEnvioOtp(contexto, null, null, true, null, "A");
        }

        return validaRiesgoNet ? Respuesta.exito() : Respuesta.estado("RESPUESTAS_INCORRECTAS");
    }

    public static Respuesta usuarioSugerido(ContextoHB contexto) {
        Objeto sugerencia = new Objeto();

        Integer random1 = (int) (Math.random() * (9999 - 1000)) + 1000;
        String usuario = "";
        Persona persona = contexto.persona();
        usuario += persona.apellido().substring(0, 1).toUpperCase();
        usuario += Texto.substring(persona.nombre(), 9).toLowerCase();
        usuario += random1.toString();
        sugerencia.set("usuario", usuario);

        Integer random2 = (int) (Math.random() * (9999 - 1000)) + 1000;
        sugerencia.set("clave", random2.toString());
        return Respuesta.exito("sugerencia", sugerencia);
    }

    public static String registrarHeaders(ContextoHB contexto, String origen) {
        if (!ConfigHB.bool("activar_log_header", false)) {
            return "";
        }
        try {
            Objeto extra = new Objeto();
            extra.set("origen", origen);
            extra.set("ip", contexto.ip());

            ApiRequest apiRequest = Api.request("auditor", "auditor", "POST", "/v1/reportes", contexto);
            apiRequest.permitirSinLogin = true;
            apiRequest.habilitarLog = false;
            apiRequest.body.set("canal", "HB");
            apiRequest.body.set("subCanal", "BACUNI");
            apiRequest.body.set("usuario", contexto.sesion.idCobis);
            apiRequest.body.set("idProceso", Util.idProceso());
            apiRequest.body.set("sesion", String.valueOf(contexto.idSesion().hashCode()).replace("-", ""));
            apiRequest.body.set("servicio", "API-HB_headers");
            apiRequest.body.set("resultado", "200");
            apiRequest.body.set("duracion", 1L);
            Objeto mensajes = apiRequest.body.set("mensajes");
            mensajes.set("entrada", new Objeto().set("headers", contexto.request.headers()));
            mensajes.set("salida", extra.toJson());
            Api.response(apiRequest);

            // Verifico checksum
            try {
                String idCobis = contexto.sesion.idCobis;
                String checksum = contexto.parametros.string("checksum");
                Boolean verificarChecksum = contexto.parametros.bool("verificarChecksum", true);

                Boolean muestreo = Set.of(ConfigHB.string("login_verificar_checksum_cobis", "").split("_"))
                        .contains(idCobis);
                if (verificarChecksum && (muestreo || ConfigHB.bool("login_verificar_checksum", false))) {
                    DateTimeFormatter formato = DateTimeFormatter.ofPattern("yyyy-MM-dd");

                    LocalDate fecha = LocalDate.now();
                    String ayer = formato.format(fecha.plusDays(-1));
                    String hoy = formato.format(fecha.plusDays(0));
                    String mañana = formato.format(fecha.plusDays(1));

                    String hashAyer = G.md5("checksum$" + ayer).substring(2, 10);
                    String hashHoy = G.md5("checksum$" + hoy).substring(2, 10);
                    String hashMañana = G.md5("checksum$" + mañana).substring(2, 10);

                    if (!hashHoy.equalsIgnoreCase(checksum) && !hashAyer.equalsIgnoreCase(checksum)
                            && !hashMañana.equalsIgnoreCase(checksum)) {
                        contexto.parametros.set("userAgent", contexto.request.userAgent());
                        return "STRIKE";
                    }
                }
            } catch (Exception e) {
            }

        } catch (Exception e) {
        }
        return "";
    }

    public static Respuesta validarUsuario(ContextoHB contexto) {
        String usuario = contexto.parametros.string("usuario", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.estado("SIN_PSEUDO_SESION");

        if (Objeto.anyEmpty(usuario))
            return Respuesta.parametrosIncorrectos();

        if (RestSeguridad.validarUsuario(contexto, usuario, UUID.randomUUID().toString()).hayError())
            return Respuesta.error();

        return Respuesta.exito();
    }

    public static Respuesta validarClave(ContextoHB contexto) {
        String clave = contexto.parametros.string("clave", null);

        if (Objeto.anyEmpty(contexto.idCobis()))
            return Respuesta.estado("SIN_PSEUDO_SESION");

        if (Objeto.anyEmpty(clave))
            return Respuesta.parametrosIncorrectos();

        if (RestSeguridad.validarClavev2(contexto, clave, UUID.randomUUID().toString()).hayError())
            return Respuesta.error();

        return Respuesta.exito();
    }

    /* INICIO TRANSMIT */

    public static Respuesta obtenerGenero(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");

        if (Objeto.empty(documento))
            return Respuesta.parametrosIncorrectos();

        if (!Util.migracionPrendida())
            return Respuesta.estado(Errores.FUNCIONALIDAD_APAGADA);

        DatosBasicosPersonas datosBasicosPersonas = ApiPersonas.datosBasicos(contexto, documento).tryGet();

        if (datosBasicosPersonas == null || datosBasicosPersonas.isEmpty())
            return Respuesta.estado(Errores.NO_ENCUENTRA_PERSONAS);

        if (!contexto.esMigrado(contexto, documento))
            return Respuesta.estado(datosBasicosPersonas.size() == 1
                    ? Errores.ERROR_USUARIO_NO_MIGRADO
                    : Errores.ERROR_USUARIO_NO_MIGRADO_MULTIPLES);

        return Respuesta.exito("generos", datosBasicosPersonas.stream().map(p -> new GeneroResponse(p.sexo)).toList());
    }

    public static Respuesta loginTransmit(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.empty(documento, csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        return validacionesLogin(contexto);
    }

    public static Respuesta migrarTransmit(ContextoHB contexto, String idSexo, String fingerprint, String ultimaFechaModificacionUsuario, String ultimaFechaModificacionClave) {
        if (!validarUsuarioParaMigracion(contexto, contexto.parametros.string("usuario"), fingerprint) ||
                !validarClaveParaMigracion(contexto, contexto.parametros.string("clave"), fingerprint))
            return null;

        DatosBasicosPersonas datosBasicos = ApiPersonas.datosBasicos(contexto, contexto.parametros.string("documento")).tryGet();
        if (datosBasicos == null || datosBasicos.isEmpty())
            return null;

        if (datosBasicos.size() > 1 && datosBasicos.stream().anyMatch(db -> db.sexo.equals(idSexo)))
            return null;

        ar.com.hipotecario.backend.servicio.api.personas.Persona.DatosBasicosPersona persona = datosBasicos.size() == 1
                ? datosBasicos.get(0)
                : datosBasicos.stream().filter(db -> db.sexo.equals(idSexo)).findFirst().orElse(null);

        if (persona == null)
            return null;

        Futuro<ar.com.hipotecario.backend.servicio.api.personas.Emails> futuroEmails = ApiPersonas.emails(contexto, persona.numeroIdentificacionTributaria);
        Futuro<ar.com.hipotecario.backend.servicio.api.personas.Telefonos> futuroTelefonos = ApiPersonas.telefonos(contexto, persona.numeroIdentificacionTributaria);

        ar.com.hipotecario.backend.servicio.api.personas.Emails emails = futuroEmails.tryGet();
        if (emails == null)
            return null;
        ar.com.hipotecario.backend.servicio.api.personas.Telefonos telefonos = futuroTelefonos.tryGet();
        if (telefonos == null)
            return null;

        UsuarioLibreriaRequest usuarioLibreriaRequest = new UsuarioLibreriaRequest(
                obtenerEmailPersona(emails),
                contexto.parametros.string("clave"),
                contexto.parametros.string("usuario"),
                contexto.parametros.string("documento"),
                obtenerTelefonoPersona(telefonos),
                contexto.canal(),
                contexto.subCanal(),
                persona.numeroIdentificacionTributaria,
                contexto.ip(),
                JourneyTransmitEnum.HB_ENROLAR_USUARIO,
                contexto.sesion().idSesion,
                contexto.idCobis(),
                "",
                "",
                obtenerFechaModificacionUsuario(ultimaFechaModificacionUsuario),
                obtenerFechaModificacionClave(ultimaFechaModificacionClave),
                obtenerFechaModificacionTelefonoPersona(telefonos),
                obtenerFechaModificacionEmailPersonal(emails),
                true
        );

        UsuarioLibreriaResponse usuarioLibreriaResponse = ApiTransmit.migrarUsuarioHB(contexto, usuarioLibreriaRequest).tryGet();

        if (usuarioLibreriaResponse == null || usuarioLibreriaResponse.esError())
            return null;

        DatosAdicionales datosAdicionalesCsm = MapperUtil.mapToObject(usuarioLibreriaResponse.addicionalData, DatosAdicionales.class);
        if (datosAdicionalesCsm == null || !StringUtils.isNotBlank(datosAdicionalesCsm.csmIdAuth))
            return null;

        if (!actualizarEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(contexto.parametros.string("documento"))))
            return null;

        Futuro<Boolean> futuroModificarRegistroDispositivo = new Futuro<>(() -> modificarRegistroDispositivo(contexto));
        Futuro<Boolean> futuroModificarAltaSoftToken = new Futuro<>(() -> modificarAltaSoftToken(contexto));
        Futuro<Boolean> futuroAccesoBiometria = new Futuro<>(() -> modificarAccesoBiometria(contexto));

        futuroModificarRegistroDispositivo.tryGet();
        futuroModificarAltaSoftToken.tryGet();
        futuroAccesoBiometria.tryGet();

        contexto.csmIdAuth = datosAdicionalesCsm.csmIdAuth;
        contexto.parametros.set("migrado", true);

        return validacionesLogin(contexto);
    }

    private static Respuesta validacionesLogin(ContextoHB contexto) {
        String documento = contexto.parametros.string("documento");
        String idSexo = contexto.parametros.string("idSexo", null);
        String fingerprint = contexto.parametros.string("fingerprint", UUID.randomUUID().toString());

        Integer numeroDocumento = ar.com.hipotecario.backend.base.Util.documento(documento);
        if (numeroDocumento == 0)
            return Respuesta.parametrosIncorrectos();

        eliminarSesion(contexto, "", false);

        Respuesta respuestaIdCobis = obtenerIdCobis(contexto, documento, null, idSexo);
        if (respuestaIdCobis.hayError())
            return respuestaIdCobis;

        String idCobis = respuestaIdCobis.string("cobis");

        setearCobisSesion(contexto, idCobis);
        Eventos.limpiar(idCobis);
        contexto.sesion.save();

        Futuro<ApiResponse> futuroPerfilInversor = obtenerPerfilInversor(contexto);
        List<Futuro<ApiResponse>> futurosIntegrantes = obtenerIntegrantes(contexto);

        boolean prendidoMonitoreoTransaccional = prendidoMonitoreoTransaccional(idCobis);
        boolean validaSesionDuplicada = validaSesionDuplicada();

        Futuro<Boolean> futuroExisteSesion = validaSesionDuplicada ? new Futuro<>(
                () -> SqlHomebanking.existeSesion(idCobis, fingerprint)) : null;
        Futuro<Boolean> futuroBloqueadoPorTransaccionFraudeLink = prendidoMonitoreoTransaccional ? new Futuro<>(
                () -> SqlHomebanking.bloqueadoPorTransaccionFraudeLink(idCobis)) : null;
        Futuro<Date> futuroFechaHoraUltimaConexion = new Futuro<>(
                () -> SqlHomebanking.fechaHoraUltimaConextion(idCobis));
        Futuro<Respuesta> futuroConfiguracionUsuario = new Futuro<>(() -> HBAplicacion.configuracionUsuario(contexto, idCobis));
        Futuro<Respuesta> futuroPersona = new Futuro<>(() -> HBPersona.persona(contexto));
        Futuro<Respuesta> futuroProductos = new Futuro<>(() -> HBProducto.productos(contexto));

        if (validaSesionDuplicada) {
            Boolean existe = futuroExisteSesion.tryGet();
            if (existe != null && existe) {
                eliminarSesion(contexto, idCobis, false);
                return Respuesta.estado(Errores.EXISTE_SESION);
            }
        }

        if (prendidoMonitoreoTransaccional) {
            Boolean bloqueadoFraudeLink = futuroBloqueadoPorTransaccionFraudeLink.tryGet();
            if (bloqueadoFraudeLink != null && bloqueadoFraudeLink) {
                eliminarSesion(contexto, idCobis, false);
                return Respuesta.estado(Errores.USUARIO_BLOQUEADO_POR_FRAUDE_LINK);
            }
        }

        if (!TransmitHB.validarCsmTransaccion(contexto, JourneyTransmitEnum.HB_INICIO_SESION, idCobis))
            return Respuesta.estado(Errores.ERROR_LOGIN_LIBRERIA);

        Date fechaHoraUltimaConexion = futuroFechaHoraUltimaConexion.tryGet();
        if (fechaHoraUltimaConexion != null)
            registrarFechaHoraUltimaConexion(contexto, fechaHoraUltimaConexion);

        if (validaSesionDuplicada)
            registrarSesion(contexto, fingerprint);

        Objeto persona = futuroPersona.tryGet();
        if (!Objeto.empty(persona))
            enviarMailIngreso(contexto, persona.string("cuit"), persona.string("apellido"), persona.string("nombre"));

        insertarLog(contexto, fingerprint);

        setearSesionLogueado(contexto);

        futuroProductos.tryGet();
        futuroPerfilInversor.tryGet();
        for (Futuro<ApiResponse> futuroIntegrante : futurosIntegrantes)
            futuroIntegrante.tryGet();

        return Respuesta.exito()
                .set("fingerprint", fingerprint)
                .set("hash", contexto.hashSesion())
                .set("personUuid", convertirHashCobis(idCobis))
                .set("csmIdAuth", contexto.csmIdAuth)
                .unir(futuroConfiguracionUsuario.tryGet())
                .unir(persona);
    }

    private static String convertirHashCobis(String idCobis) {
        try {
            return DatatypeConverter.printBase64Binary(idCobis.getBytes());
        } catch (Exception ex) {
            return "";
        }
    }

    private static void loguearMensajeError(String accion, String funcionalidad, String idCobis) {
        log.info("Error al intentar ".concat(accion).concat("el ").concat(funcionalidad).concat(" de cliente ").concat(idCobis).concat("al migrar a Transmit"));
    }

    private static Boolean modificarAccesoBiometria(ContextoHB contexto) {
        AccesoBiometria accesoBiometria = SqlHomeBanking.obtenerAccesosBiometria(contexto, contexto.idCobis(), "").tryGet();
        if (accesoBiometria == null) {
            loguearMensajeError("obtener", "Acceso Biometria", contexto.idCobis());
            return false;
        }

        if (SqlHomeBanking.borrarAccesosBiometria(contexto, contexto.idCobis()).tryGet())
            return true;

        loguearMensajeError("modificar", "Acceso Biometria", contexto.idCobis());
        return true;
    }

    private static Boolean modificarAltaSoftToken(ContextoHB contexto) {
        SoftToken softToken = SqlMobile.obtenerUltimaAltaSoftTokenActiva(contexto, contexto.idCobis()).tryGet();
        if (softToken == null) {
            loguearMensajeError("obtener", "Alta de Soft Token", contexto.idCobis());
            return false;
        }

        if (SqlMobile.deshabilitarSoftToken(contexto, String.valueOf(softToken.Id)).tryGet())
            return true;

        loguearMensajeError("modificar", "Alta de Soft Token", contexto.idCobis());
        return true;
    }

    private static Boolean modificarRegistroDispositivo(ContextoHB contexto) {
        RegistroDispositivo registroDispositivo = SqlMobile.obtenerUltimoRegistroPorCobis(contexto, contexto.idCobis()).tryGet();
        if (registroDispositivo == null) {
            loguearMensajeError("obtener", "Registro de Dispositivo", contexto.idCobis());
            return false;
        }

        if (SqlMobile.deshabilitarRegistroDispositivo(contexto, registroDispositivo.IdDispositivo.concat(String.valueOf(new Random().nextInt())), String.valueOf(registroDispositivo.Id)).tryGet())
            return true;

        loguearMensajeError("modificar", "Registro de Dispositivo", contexto.idCobis());
        return false;
    }

    private static Emails.Email obtenerEmail(ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        return emails.personal() != null ? emails.personal() : (emails.laboral() != null ? emails.laboral() : null);
    }

    private static String obtenerEmailPersona(ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        Emails.Email email = obtenerEmail(emails);
        return email != null ? email.direccion() : "";
    }

    private static Date obtenerFechaModificacionEmailPersonal(ar.com.hipotecario.backend.servicio.api.personas.Emails emails) {
        Emails.Email email = obtenerEmail(emails);
        return email != null ? email.fechaModificacion.fechaDate() : null;
    }

    private static Telefonos.Telefono obtenerTelefono(ar.com.hipotecario.backend.servicio.api.personas.Telefonos telefonos) {
        return telefonos.celular();
    }

    private static String obtenerTelefonoPersona(ar.com.hipotecario.backend.servicio.api.personas.Telefonos telefonos) {
        ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono telefono = obtenerTelefono(telefonos);
        return telefono != null ? telefono.numeroMigracion() : "";
    }

    private static Date obtenerFechaModificacionTelefonoPersona(ar.com.hipotecario.backend.servicio.api.personas.Telefonos telefonos) {
        ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono telefono = obtenerTelefono(telefonos);
        return telefono != null ? telefono.fechaModificacion.fechaDate() : null;
    }

    private static Date convertirDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    private static Date obtenerFechaModificacionClave(String ultimaFechaModificacionClave) {
        return StringUtils.isNotBlank(ultimaFechaModificacionClave)
                ? convertirDate(LocalDate.parse(ultimaFechaModificacionClave, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                : new Date();
    }

    private static Date obtenerFechaModificacionUsuario(String ultimaFechaModificacionUsuario) {
        return StringUtils.isNotBlank(ultimaFechaModificacionUsuario)
                ? convertirDate(LocalDate.parse(ultimaFechaModificacionUsuario, DateTimeFormatter.ofPattern("yyyy-MM-dd")))
                : new Date();
    }

    private static Respuesta chequearMigracion(ContextoHB contexto, String documento, String idSexo, String fingerprint) {
        ResponseMigracionUsuario estadoMigradoResponse = obtenerEstadoMigracion(contexto, ar.com.hipotecario.backend.base.Util.documento(documento));
        if (estadoMigradoResponse != null && EstadoMigradoEnum.codigo(estadoMigradoResponse.migrado).esMigrado())
            return loginTransmit(contexto);
        return null;
    }

    private static void setearCobisSesion(ContextoHB contexto, String idCobis) {
        contexto.sesion.idCobis = (idCobis);
        contexto.sesion.idCobisReal = (idCobis);
    }

    private static List<String> obtenerCobis(ContextoHB contexto, String documento, String idTipoDocumento, String idSexo) {
        String idCobis = EHBLogin.get(contexto, documento);
        if (Objeto.empty(idCobis))
            return RestPersona.listaIdCobis(contexto, documento, idTipoDocumento, idSexo);
        return List.of(idCobis);
    }

    private static Respuesta obtenerIdCobis(ContextoHB contexto, String documento, String idTipoDocumento, String idSexo) {
        List<String> listaIdCobis = obtenerCobis(contexto, documento, idTipoDocumento, idSexo);
        if (listaIdCobis == null) {
            if (contexto.sesion.clienteInexistente)
                return Respuesta.estado(Errores.PERSONA_NO_ENCONTRADA).set("mensaje",
                        ConfigHB.string("mensaje_dni_no_encontrado",
                                "Los datos ingresados no son válidos. Corroborá que ingresaste bien el DNI."));
            return Respuesta.estado(contexto.sesion.cobisCaido ? Errores.COBIS_CAIDO : Errores.ERROR_GENERICO);
        } else if (listaIdCobis.isEmpty())
            return Respuesta.estado(Errores.PERSONA_NO_ENCONTRADA).set("mensaje",
                    ConfigHB.string("mensaje_dni_no_encontrado",
                            "Los datos ingresados no son válidos. Corroborá que ingresaste bien el DNI."));
        else if (listaIdCobis.size() > 1)
            return Respuesta.estado(Errores.MULTIPLES_PERSONAS_ENCONTRADAS);

        return Respuesta.exito().set("cobis", listaIdCobis.get(0));
    }

    private static Futuro<ApiResponse> obtenerPerfilInversor(ContextoHB contexto) {
        return new Futuro<>(() -> RestPersona.perfilInversor(contexto));
    }

    private static List<Futuro<ApiResponse>> obtenerIntegrantes(ContextoHB contexto) {
        return contexto.cuentasComitentes().stream()
                .map(item -> new Futuro<>(() ->
                        ProductosService.integrantesProducto(contexto, item.numero())
                )).toList();
    }

    private static Respuesta usuariosMarcados(String idCobis) {
        if (EHBLogin.esUsuarioMarcado(idCobis)) {
            Objeto consultaForzar = EHBLogin.datosCambioUsuario(idCobis);
            if (consultaForzar != null && !consultaForzar.bool("realizado"))
                return Respuesta.estado("FORZAR_CAMBIO_USUARIO").set("conSoftToken", consultaForzar.bool("token", false));
        }
        return Respuesta.exito();
    }

    private static void eliminarSesion(ContextoHB contexto, String idCobis, boolean eliminarBaseDeDatos) {
        contexto.eliminarSesion(eliminarBaseDeDatos, idCobis);
    }

    private static boolean prendidoMonitoreoTransaccional(String idCobis) {
        return HBAplicacion.funcionalidadPrendida(idCobis, "prendido_monitoreo_transaccional");
    }

    private static boolean validaSesionDuplicada() {
        return HBAplicacion.funcionalidadPrendida("validar_sesion_duplicada");
    }

    private static ResponseMigracionUsuario obtenerEstadoMigracion(ContextoHB contexto, int numeroDocumento) {
        ResponseMigracionUsuario response = gestionarMigracion(contexto, new RequestMigracionUsuario(numeroDocumento, 0, OpcionMigradoEnum.CONSULTAR, contexto.idCobis(), "", ""));
        if (Objeto.empty(response) || (!Objeto.empty(response) && response.codRet == -1))
            return null;
        return response;
    }

    private static boolean actualizarEstadoMigracion(ContextoHB contexto, int numeroDocumento) {
        ResponseMigracionUsuario response = gestionarMigracion(contexto, new RequestMigracionUsuario(numeroDocumento, Integer.parseInt(EstadoMigradoEnum.MIGRADO.getCodigo()), OpcionMigradoEnum.ACTUALIZAR, contexto.idCobis(), "", ""));
        return !Objeto.empty(response) && (Objeto.empty(response) || response.codRet != -1);
    }

    private static ResponseMigracionUsuario gestionarMigracion(ContextoHB contexto, RequestMigracionUsuario request) {
        return ApiSeguridad.gestionarMigracion(contexto, request).tryGet();
    }

    private static void registrarFechaHoraUltimaConexion(ContextoHB contexto, Date fechaHoraUltimaConexion) {
        contexto.sesion.fechaHoraUltimaConexion = fechaHoraUltimaConexion;
        new Futuro<>(() -> SqlHomebanking.registrarFechaHoraUltimaConextion(contexto.idCobis(),
                ConfigHB.string("modo_ultima_conexion", "rowlock")));
    }

    private static void registrarSesion(ContextoHB contexto, String fingerprint) {
        new Futuro<>(() -> SqlHomebanking.registrarSesion(contexto.idCobis(), fingerprint, contexto.idSesion()));
    }

    private static void setearSesionLogueado(ContextoHB contexto) {
        contexto.sesion.usuarioLogueado = true;
        contexto.sesion.ip = contexto.ip();
    }

    private static void insertarLog(ContextoHB contexto, String fingerprint) {
        new Futuro<>(() -> contexto.insertarLogLogin(contexto, fingerprint));
    }

    private static void enviarMailIngreso(ContextoHB contexto, String apellido, String nombre, String cuit) {
        new Futuro<>(() -> mailIngresoUsuario(contexto, cuit, apellido, nombre));
    }

    private static boolean validarUsuarioParaMigracion(ContextoHB contexto, String usuario, String fingerprint) {
        ApiResponse validarUsuario = new Futuro<>(
                () -> RestSeguridad.validarUsuario(contexto, usuario, fingerprint)).tryGet();
        if (validarUsuario == null)
            return false;
        return !validarUsuario.hayError();
    }

    private static boolean validarClaveParaMigracion(ContextoHB contexto, String clave, String fingerprint) {
        ApiResponse validarClave = new Futuro<>(
                () -> RestSeguridad.validarClavev2(contexto, clave, fingerprint)).tryGet();
        if (validarClave == null)
            return false;
        return !validarClave.hayError();
    }


    public static Respuesta renovacionTransmit(ContextoHB contexto) {
        String csmId = contexto.parametros.string("csmId", "");
        String checksum = contexto.parametros.string("checksum", "");

        if (Objeto.anyEmpty(csmId, checksum))
            return Respuesta.parametrosIncorrectos();

        if (Objeto.empty(contexto.idCobis()))
            return Respuesta.sinPseudoSesion();

        if (!contexto.esMigrado(contexto))
            return Respuesta.estado(Errores.ERROR_USUARIO_NO_MIGRADO);

        return TransmitHB.validarCsmTransaccion(contexto, JourneyTransmitEnum.HB_INICIO_SESION, "")
                ? Respuesta.exito().set("csmIdAuth", contexto.csmIdAuth)
                : Respuesta.estado(Errores.ERROR_LOGIN_LIBRERIA);
    }

    /* FIN TRANSMIT */

}
