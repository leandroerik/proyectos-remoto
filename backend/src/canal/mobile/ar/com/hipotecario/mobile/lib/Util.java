package ar.com.hipotecario.mobile.lib;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.SocketTimeoutException;
import java.security.SecureRandom;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.negocio.CuentaTercero;
import ar.com.hipotecario.mobile.servicio.CuentasService;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.SqlNotificaciones;
import org.apache.commons.lang3.StringUtils;

public abstract class Util {

    public static final String[] NEMONICO_ONBOARDINGS = {"ONBOARDING", "ONBOARDING_MORA"};
    public static SecureRandom secureRandom = new SecureRandom();
    public static SecureRandom secureRandomHmacSHA1 = secureRandomHmacSHA1();
    private static String BH_CODIGO = "044";

    public static String getBhCodigo() {
        return BH_CODIGO;
    }

    public static Integer random(Integer minimo, Integer maximo) {
        return secureRandom.nextInt(maximo - minimo) + minimo;
    }

    public static Integer randomHmacSHA1(Integer minimo, Integer maximo) {
        return secureRandomHmacSHA1.nextInt(maximo - minimo) + minimo;
    }

    public static SecureRandom secureRandomHmacSHA1() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA1");
            SecretKey key = keyGen.generateKey();
            byte[] hmacsha1 = key.getEncoded();
            return new SecureRandom(hmacsha1);
        } catch (Exception e) {
            return new SecureRandom();
        }
    }

    public static String idProceso() {
        Integer random = secureRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
        return random.toString();
    }

    public static String enmascarar(String servicio, String clave, String valor) {
        valor = "ValidarUsuarioIDG".equals(servicio) && "clave".equals(clave) ? "***" : valor;
        valor = "ValidarClaveIDG".equals(servicio) && "clave".equals(clave) ? "***" : valor;
        if (clave != null) {
            if (clave.contains("clave") || clave.contains("pass") || clave.contains("pin") || clave.contains("token")) {
                valor = "***";
            }
        }
        return valor;
    }

    public static String enmascarar(String servicio, Objeto datos) {
        Objeto clon = null;

        if ("ValidarTCO".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            for (Objeto objeto : clon.objetos("respuestas")) {
                objeto.set("respuesta", "***");
            }
        }

        if ("CrearUsuario".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("CrearClave".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("CrearUsuarioIDG".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("CrearClaveIDG".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("CambiarUsuario".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("CambiarClave".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.objeto("parametros").set("clave", "***");
        }

        if ("LinkPostVerificacion".equals(servicio)) {
            clon = Objeto.fromJson(datos.toJson());
            clon.set("pin", "***");
        }

        for (String clave : datos.claves()) {
            if (clave.contains("clave") || clave.contains("pass") || clave.contains("pin") || clave.contains("token")) {
                clon = Objeto.fromJson(datos.toJson());
                clon.set(clave, "***");
            }
        }

        return clon != null ? clon.toJson() : datos.toJson();
    }

    public static void pruebaTimeOut(ContextoMB contexto) throws SocketTimeoutException {
        if ((ConfigMB.esDesarrollo() || ConfigMB.esHomologacion()) && MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prueba_error_302", "prendido_prueba_error_302_cobis")) {
            throw new java.net.SocketTimeoutException();
        }
    }

    public static String getNumeroCaso(ApiResponseMB responseReclamo) {
        String numeroCaso = "";
        try {
            Objeto reclamo = (Objeto) responseReclamo.get("Datos");
            numeroCaso = reclamo.objetos().get(0).string("NumeracionCRM");
        } catch (Exception e) {
            return "";
        }
        return numeroCaso;
    }

    public static BigDecimal porcentaje(BigDecimal num, BigDecimal total) {
        BigDecimal cero = new BigDecimal(0);
        if (total.compareTo(cero) <= 0) {
            return cero;
        }
        return num.multiply(new BigDecimal(100)).divide(total, RoundingMode.CEILING);
    }

    public static Boolean isDiaHabil(ContextoMB contexto, String momento) {
        try {

            ApiResponseMB responseCatalogo = RestCatalogo.calendarioFecha(contexto, momento);

            if (responseCatalogo.hayError()) {
                return false;
            }

            String nombreDia = responseCatalogo.objetos().get(0).string("nombreDia");
            if (nombreDia.equalsIgnoreCase("Sabado") || nombreDia.equalsIgnoreCase("Domingo")) {
                return false;
            }

            if (responseCatalogo.objetos().get(0).string("esDiaHabil").equalsIgnoreCase("1")) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static Boolean isDiaHabil(ContextoMB contexto) {
        try {
            ApiResponseMB responseCatalogo = RestCatalogo.calendarioFechaActual(contexto);
            if (responseCatalogo.hayError()) {
                return false;
            }

            String nombreDia = responseCatalogo.objetos().get(0).string("nombreDia");
            if (nombreDia.equalsIgnoreCase("Sabado") || nombreDia.equalsIgnoreCase("Domingo")) {
                return false;
            }

            if (responseCatalogo.objetos().get(0).string("esDiaHabil").equalsIgnoreCase("1")) {
                return true;
            }

        } catch (Exception e) {
            return false;
        }

        return false;
    }

    public static BigDecimal dividir(BigDecimal dividendo, BigDecimal divisor) {
        try {
            return dividendo.divide(divisor);
        } catch (java.lang.ArithmeticException ex) {
            try {
                return dividendo.divide(divisor, 6, RoundingMode.DOWN);
            } catch (Exception e) {
                return null;
            }
        }
    }

    public static RespuestaMB contador(ContextoMB contexto) {
        try {
            String nemonico = contexto.parametros.string("nemonico");

            if (onboardingDeclarado(contexto, nemonico)) {
                return RespuestaMB.exito();
            }

            SqlRequestMB sqlRequest = SqlMB.request("InsertContador", "homebanking");
            sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[contador] WITH (ROWLOCK) (idCobis, tipo, momento, canal) VALUES (?, ?, GETDATE(), 'MB')";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(nemonico);
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
            return RespuestaMB.error();
        }
        return RespuestaMB.exito();
    }

    public static SqlResponseMB getContador(ContextoMB contexto, String nemonico, Date dia) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ?";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(nemonico);
            if (dia != null) {
                LocalDate localDate = LocalDate.ofInstant(dia.toInstant(), ZoneId.systemDefault());
                sqlRequest.sql += " AND DATEPART(dd, momento) = ?";
                sqlRequest.sql += " AND DATEPART(mm, momento) = ?";
                sqlRequest.sql += " AND DATEPART(yy, momento) = ?";

                sqlRequest.add(localDate.getDayOfMonth());
                sqlRequest.add(localDate.getMonthValue());
                sqlRequest.add(localDate.getYear());
            }

            SqlResponseMB response = SqlMB.response(sqlRequest);
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean fueraHorarioFraudes(ContextoMB contexto) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss");
        Boolean isFueraHorario = false;
        try {
            String listado = ConfigMB.string("listadoHorarioFraude", "14/11/2020 00:00:01-14/11/2020 00:00:02_15/12/2020 23:59:00-15/12/2020 23:59:02");
            Set<String> diasHorasFueraHorario = Objeto.setOf(listado.split("_"));

            String[] dh = null;
            for (String diaHora : diasHorasFueraHorario) {

                dh = diaHora.split("-");

                Date fechaActual = new Date();
                Date dateInicio = sdf.parse(dh[0]);
                Date dateFin = sdf.parse(dh[1]);

                if (dateInicio.before(fechaActual) && dateFin.after(fechaActual)) {
                    isFueraHorario = true;
                    break;
                }
                dh = null;
            }

        } catch (Exception e) {
            //
        }
        return isFueraHorario;
    }

    public static boolean esFechaActualSuperiorVencimiento(Date fechaPosterior, Date fechaActual) {
        return fechaActual.compareTo(fechaPosterior) == 1;
    }

    public static Boolean isfueraHorario(Integer horaInicio, Integer horaFin) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Boolean fueraHorario = false;
        try {
            Calendar c1 = Calendar.getInstance();
            String dateS = sdf.format(new Date());
            Date date = sdf.parse(dateS);
            c1.setTime(date);
            fueraHorario = c1.get(Calendar.HOUR_OF_DAY) < horaInicio || c1.get(Calendar.HOUR_OF_DAY) >= horaFin;
        } catch (Exception e) {
            // TODO: handle exception
        }
        return fueraHorario;
    }

    public static Boolean isfueraHorarioV2(String horaInicio, String horaFin) {

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        Date hoy = new Date();
        String hora = sdf.format(hoy);
        Boolean fueraHorario = false;
        try {
            if (hora.compareTo(horaInicio) < 0 || hora.compareTo(horaFin) > 0) {
                fueraHorario = true;
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return fueraHorario;
    }

    public static void insertarLogMotor(ContextoMB contexto, String numeroSolicitud, String resolucion, String explicacion, String mensajeCliente, String mensajeDesarrollador) {
        try {
            String sql = "";
            sql += " INSERT INTO [Homebanking].[dbo].[log_api_ventas] (momento,idCobis,numeroDocumento,numeroSolicitud,servicio,resolucionMotor,explicacionMotor,mensajeCliente,mensajeDesarrollador,canal)";
            sql += " VALUES (GETDATE(), ?, ?, ?, ?, ?, ?, ?, ?, ?)";

            SqlRequestMB sqlRequest = SqlMB.request("InsertLogApiVentas", "homebanking");
            sqlRequest.sql = sql;
            sqlRequest.parametros.add(Texto.substring(contexto.idCobis(), 250));
            sqlRequest.parametros.add(Texto.substring(contexto.persona().numeroDocumento(), 250));
            sqlRequest.parametros.add(Texto.substring(numeroSolicitud, 250));
            sqlRequest.parametros.add(Texto.substring("motor", 250));
            sqlRequest.parametros.add(Texto.substring(resolucion, 990));
            sqlRequest.parametros.add(Texto.substring(explicacion, 990));
            sqlRequest.parametros.add(Texto.substring(mensajeCliente, 990));
            sqlRequest.parametros.add(Texto.substring(mensajeDesarrollador, 990));
            sqlRequest.parametros.add("MB");
            SqlMB.response(sqlRequest);
        } catch (Exception e) {
        }
    }

    public static Objeto getOrUpdateNotificacionEstadoProducto(ContextoMB contexto, Objeto solicitud) {
        Objeto respuesta = new Objeto();
        SqlResponseMB selectResponse = SqlNotificaciones.getNotificacionProducto(contexto, solicitud.string("producto"));
        if (selectResponse.registros.size() > 0) {
            if (!solicitud.string("estado").equalsIgnoreCase(selectResponse.registros.get(0).string("estado")) || !solicitud.string("idSolicitud").equalsIgnoreCase(selectResponse.registros.get(0).string("solicitud"))) {
                SqlNotificaciones.updateNotificacionProducto(contexto, solicitud.string("idSolicitud"), false, false, solicitud.string("estado"), solicitud.string("producto"));
            } else {
                return respuesta.set("leido", selectResponse.registros.get(0).integer("leido") == 1).set("borrar", selectResponse.registros.get(0).integer("borrado") == 1);
            }
        } else {
            SqlNotificaciones.insertNotificacionSolicitud(contexto, solicitud.string("idSolicitud"), false, false, solicitud.string("estado"), solicitud.string("producto"));
        }
        respuesta.set("leido", false).set("borrar", false);
        return respuesta;
    }

    public static String bucketMora(Integer diasDeMora, String producto) {

        if (diasDeMora >= 1 && diasDeMora <= 30) {
            return "B1";
        }

        if (diasDeMora >= 31 && diasDeMora <= 60) {
            return "B2";
        }

        if (producto.equals("HIPOTECARIO")) {
            if (diasDeMora >= 61 && diasDeMora <= 180) {
                return "B3";
            }
        } else {
            if (diasDeMora >= 61 && diasDeMora <= 90) {
                return "B3";
            } else {
                if (!ConfigMB.esProduccion() && diasDeMora > 90) {
                    return "B2";
                }
            }
        }
        return "";
    }

    public static String calcularFechaNdiasHabiles(ContextoMB contexto, String fechaOrigen, Integer cantidadDias) {
        return calcularFechaNdiasHabiles(contexto, fechaOrigen, "yyyy-MM-dd", cantidadDias);
    }

    public static String calcularFechaNdiasHabiles(ContextoMB contexto, String fechaOrigen, String formatoFecha, Integer cantidadDias) {
        Integer diasPlus = 0;
        SimpleDateFormat sdf = new SimpleDateFormat(formatoFecha);
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(fechaOrigen));
            Integer count = 0;
            String momentoPlusDias = "";
            Integer countCorte = cantidadDias + 5;

            while (diasPlus < cantidadDias) {

                if (count >= countCorte) {
                    return momentoPlusDias;
                }

                count++;
                cal.setTime(sdf.parse(fechaOrigen));
                cal.add(Calendar.DAY_OF_MONTH, count);
                momentoPlusDias = sdf.format(cal.getTime());

                if (Util.isDiaHabil(contexto, momentoPlusDias)) {
                    diasPlus++;
                }

            }
            return momentoPlusDias;
        } catch (Exception e) {
            return "";
        }
    }

    public static SqlResponseMB getContador(ContextoMB contexto, String nemonico) {
        try {
            SqlRequestMB sqlRequest = SqlMB.request("ConsultaContador", "homebanking");
            sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[contador] WITH (NOLOCK) WHERE idCobis = ? AND tipo = ?";
            sqlRequest.add(contexto.idCobis());
            sqlRequest.add(nemonico);
            SqlResponseMB response = SqlMB.response(sqlRequest);
            return response;
        } catch (Exception e) {
            return null;
        }
    }

    public static Boolean tieneMuestreoNemonico(ContextoMB contexto, String nemonico) {
        try {
            SqlResponseMB response = getContador(contexto, nemonico);
            if (response == null || response.hayError) {
                return false;
            }
            return response.registros.size() >= 1 ? true : false;
        } catch (Exception e) {
            return false;
        }
    }

    public static RespuestaMB CatalogoRelaciones(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        respuesta.add(new Objeto().set("id", "1").set("descripcion", "Padre"));
        respuesta.add(new Objeto().set("id", "4").set("descripcion", "Madre"));
        respuesta.add(new Objeto().set("id", "15").set("descripcion", "Hijo/a"));
        respuesta.add(new Objeto().set("id", "2").set("descripcion", "Cónyuge"));
        respuesta.add(new Objeto().set("id", "18").set("descripcion", "Otro"));
        return respuesta;
    }

    public static RespuestaMB CatalogoRelacionesV2(ContextoMB contexto) {
        RespuestaMB respuesta = new RespuestaMB();
        List<Objeto> relaciones = new ArrayList<>();
        relaciones.add(new Objeto().set("id", "1").set("descripcion", "Padre"));
        relaciones.add(new Objeto().set("id", "4").set("descripcion", "Madre"));
        relaciones.add(new Objeto().set("id", "15").set("descripcion", "Hijo/a"));
        relaciones.add(new Objeto().set("id", "2").set("descripcion", "Cónyuge"));
        relaciones.add(new Objeto().set("id", "18").set("descripcion", "Otro"));
        respuesta.set("relaciones", relaciones);
        return respuesta;
    }

    public static Date dateStringToDate(Date fecha, String formato) throws ParseException {
        String fechaActualString = new SimpleDateFormat(formato).format(fecha);
        return new SimpleDateFormat(formato).parse(fechaActualString);
    }

    private static Boolean onboardingDeclarado(ContextoMB contexto, String nemonico) {
        try {
            if (Arrays.asList(NEMONICO_ONBOARDINGS).contains(nemonico)) {
                return tieneMuestreoNemonico(contexto, nemonico);
            }
        } catch (Exception e) {
        }
        return false;
    }

    public static String calcularFechaNdiasHabilesPasado(ContextoMB contexto, String fechaOrigen, Integer cantidadDias) {
        if (cantidadDias >= 0) {
            return "";
        }

        Integer diasMinus = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(fechaOrigen));
            Integer count = 0;
            String momentoMinusDias = "";
            Integer countCorte = cantidadDias + 5;

            while (cantidadDias < diasMinus) {

                if (countCorte >= count) {
                    return momentoMinusDias;
                }

                count++;
                cal.setTime(sdf.parse(fechaOrigen));
                cal.add(Calendar.DAY_OF_MONTH, -count);
                momentoMinusDias = sdf.format(cal.getTime());

                if (Util.isDiaHabil(contexto, momentoMinusDias)) {
                    diasMinus--;
                }

            }
            return momentoMinusDias;
        } catch (Exception e) {
            return "";
        }
    }

    public static void ordenarObjetos(List<Objeto> items, String campo) {
        Collections.sort(items, new Comparator<Objeto>() {
            public int compare(Objeto o1, Objeto o2) {
                String a = o1.string(campo).split(" -")[0];
                String b = o2.string(campo).split(" -")[0];
                return a.compareTo(b);
            }
        });
    }

    public static List<Objeto> getOrUpdateNotificacionSolicitudes(ContextoMB contexto, List<Objeto> solicitudes, String cuotapartista) {
        List<Objeto> notificaciones = new ArrayList<>();
        if (solicitudes.size() > 0) {
            //Se marca como borrado las notificaciones que no están dentro del dia actual y día habil anterior.
            SqlResponseMB selectResponseD = SqlNotificaciones.getNotificacionProductoNoBorrado(contexto, "INV");
            List<Objeto> desfasados = selectResponseD.registros.stream().filter(x -> !(solicitudes.stream().anyMatch(s -> x.string("solicitud").equals(s.string("llave"))))).toList();
            desfasados.forEach(x -> SqlNotificaciones.updateNotificacionSolicitudLeidoBorrado(contexto, x.string("solicitud"), Boolean.TRUE, Boolean.TRUE));

            solicitudes.forEach(s -> {
                SqlResponseMB getResponse = SqlNotificaciones.getNotificacionSolicitud(contexto, s.string("llave"));
                if (getResponse.registros.size() == 0) {
                    SqlNotificaciones.insertNotificacionSolicitud(contexto, s.string("llave"), false, false, s.string("tipoSolicitud").toUpperCase(), "INV");
                }
            });
            SqlResponseMB selectResponse = SqlNotificaciones.getNotificacionProducto(contexto, "INV");
            notificaciones = selectResponse.registros.stream().filter(x -> x.string("solicitud").split("_")[1].equals(cuotapartista)).collect(Collectors.toList());
            notificaciones.forEach(x -> {
                Optional<Objeto> soli = solicitudes.stream()
                        .filter(a -> x.string("solicitud").equals(a.string("llave")))
                        .findFirst();

                x.set("importe", soli.isPresent() ? soli.get().bigDecimal("importe", String.valueOf(BigDecimal.ZERO)) : BigDecimal.ZERO);
                x.set("estadoSolicitud", soli.isPresent() ? soli.get().string("estadoSolicitud").equalsIgnoreCase("Agendada") : Boolean.FALSE);
            });
        }
        return notificaciones;
    }

    public static RespuestaMB isFueraHorarioProcesoBatch(ContextoMB contexto) {
        return fueraHorarioProcesosBatch() ? RespuestaMB.estado("FUERA_HORARIO") : RespuestaMB.exito();
    }

    private static Boolean fueraHorarioProcesosBatch() {
        try {
            return isfueraHorario(ConfigMB.integer("procesos_horaInicio", 7), ConfigMB.integer("procesos_horaFin", 22));
        } catch (Exception e) {
            return false;
        }
    }

    public static RespuestaMB isFueraHorarioDiaProcesoBatch(ContextoMB contexto) {
        return isDiaHabil(contexto) && fueraHorarioProcesosBatch() ? RespuestaMB.estado("FUERA_HORARIO") : RespuestaMB.exito();
    }

    private static CuentaTercero obtenerCuentaTercero(ContextoMB contexto, String id) {
        return new CuentaTercero(contexto, id, true);
    }

    public static String obtenerNombreBanco(ContextoMB contexto, String cbu, String numeroProducto) {
        if (CuentasService.esCuentaBH(cbu) || CuentasService.esCuentaBH(numeroProducto))
            return RestCatalogo.banco(BH_CODIGO);
        if (CuentasService.esCbu(cbu) || CuentasService.esCbu(numeroProducto))
            return RestCatalogo.banco(cbu.substring(0, 3));
        if (CuentasService.esCvu(cbu) || CuentasService.esCvu(numeroProducto)) {
            String nombre = RestCatalogo.bancoCVU(cbu.substring(0, 8));
            if (StringUtils.isNotBlank(nombre))
                return nombre;
        }

        if (StringUtils.isNotBlank(cbu)) {
            CuentaTercero cuentaTercero = obtenerCuentaTercero(contexto, cbu);
            if (!cuentaTercero.cuentaEncontrada)
                cuentaTercero = obtenerCuentaTercero(contexto, numeroProducto);
            return cuentaTercero.banco();
        }

        if (CuentasService.esCuentaBH(numeroProducto) || CuentasService.esCuentaCorrienteBH(numeroProducto))
            return RestCatalogo.banco(BH_CODIGO);
        return obtenerCuentaTercero(contexto, numeroProducto).banco();
    }

    public static boolean migracionCompleta() {
        return MBAplicacion.funcionalidadPrendida("migracion_transmit_completa");
    }

    public static boolean migracionPrendida() {
        return MBAplicacion.funcionalidadPrendida("prendido_migracion_transmit");
    }

    public static String obtenerDescripcionMonedaTransmit(String idMoneda) {
        return idMoneda.equals("80") ? "ARS" : "USD";
    }

    public static LocalDate stringToLocalDate(String fecha, String formato) {
        Date date;
        try {
            date = new SimpleDateFormat(formato).parse(fecha);
        } catch (ParseException e) {
            date = new Date();
        }
        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public static long diferenciaDias(Date fecha) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        String hoyString = sdf.format(new Date());
        String fechaString = sdf.format(fecha);
        Date hoy = sdf.parse(hoyString);
        Date fechaVencimiento = sdf.parse(fechaString);
        long diffEnMillisec = Math.abs(fechaVencimiento.getTime() - hoy.getTime());
        long diff = TimeUnit.DAYS.convert(diffEnMillisec, TimeUnit.MILLISECONDS);
        return diff;
    }
}
