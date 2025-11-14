package ar.com.hipotecario.canal.homebanking.servicio;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.api.HBBuhoPuntos;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Pdf;

public class BuhopuntosProductoService {
    private static Objeto getRequest(ContextoHB contexto, Integer idCanje) {

        Objeto request = new Objeto();
        Objeto cliente = new Objeto();
        Objeto datosEnvio = new Objeto();
        Objeto solicitud = new Objeto();
        Objeto canjes = new Objeto();
        Objeto canjesItems = new Objeto();
        Objeto metodoPago = new Objeto();
        cliente.set("datosEnvio", datosEnvio);
        solicitud.set("canjes", canjes);
        canjes.add(canjesItems);
        canjesItems.set("metodoPago", metodoPago);
        request.set("cliente", cliente);
        request.set("solicitud", solicitud);
        canjes.add("metodoPago", canjesItems);

        cliente.set("tipoDocumento", 0);
        cliente.set("fechaApertura", getFecha());
        cliente.set("codigoCliente", contexto.idCobis());
        cliente.set("apellido", contexto.persona().apellido());
        cliente.set("numeroDocumento", 0);
        cliente.set("nombre", contexto.persona().nombre());

        datosEnvio.set("piso", "1");
        datosEnvio.set("numero", "123");
        datosEnvio.set("codigoPostal", "1234");
        datosEnvio.set("calle", "Test");
        datosEnvio.set("codigoProvincia", "2");
        datosEnvio.set("pais", "1");
        datosEnvio.set("entreCalles", "Test");
        datosEnvio.set("ciudad", "Test");
        datosEnvio.set("observaciones", ConfigHB.string("cashback_observaciones"));
        datosEnvio.set("departamento", "1");
        datosEnvio.set("localidad", "Test");
        datosEnvio.set("telefono", "111111111");
        datosEnvio.set("email", contexto.persona().email());

        String descripcion = "Credito de ";
        descripcion.concat(contexto.parametros.string("premio")).concat(" en ");
        descripcion.concat(contexto.parametros.string("descripcion"));

        canjesItems.set("descripcion", descripcion);
        canjesItems.set("id", idCanje);
        canjesItems.set("puntos", contexto.parametros.integer("premio"));
        canjesItems.set("nombre", contexto.parametros.string("descripcion").toUpperCase());
        canjesItems.set("codigoPremio", contexto.parametros.string("id"));
        canjesItems.set("cuotas", "N");

        solicitud.set("idEstado", "NEW");
        solicitud.set("idTema", 1255);
        solicitud.set("idProducto", "ATC");
        solicitud.set("codigoProducto", 0000000000);
        solicitud.set("numeroProducto", 0000000000);
        solicitud.set("idGrupo", 1);

        return request;
    }

    private static String getFecha() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String fechaFormateada = ahora.format(formatter);
        return fechaFormateada;
    }

    public static boolean tieneCAPermitida(ContextoHB contexto) {
        boolean permitido = false;
        permitido = contexto.cajaAhorroTitularPesos().descripcionEstado().equals("Activa")
                | contexto.cajaAhorroTitularPesos().descripcionEstado().equals("Vigente");
        return permitido;
    }

    public static boolean tieneTcPermitida(ContextoHB contexto) {
        boolean permitido = false;
        permitido = contexto.tarjetaCreditoTitular() != null
                ? contexto.tarjetaCreditoTitular().esPrefijoVisa().equals("2") ? true : false
                : false;

        return permitido;
    }

    public static Objeto consultaCashBack(ContextoHB contexto) {
        Objeto cashback = new Objeto();
        for (int i = 1; i <= 6; i++) {
            String codigo = String.format("202300%02d", i);
            String[] premio = ConfigHB.string("cashback_premio_" + codigo).split("_");
            var item = new Objeto();
            item.set("id", premio[0]);
            item.set("descripcion", premio[1]);
            item.set("puntos", premio[2]);
            item.set("tipo_cashback", premio[3]);
            item.set("cashback", premio[4]);
            cashback.add(item);
        }
        return cashback;
    }

    public static Integer canjearCashback(ContextoHB contexto) {
        Integer idCanje = SqlBuhopuntosProductoService.consultarCashbackIdCanje(contexto);
        // Para ambiente bajo se trabaja con idOperacion = idCanje, pero en prod se
        // espera a la respuesta de api-rewards
        Integer idOperacion = idCanje;

        if (ConfigHB.esProduccion()) {
            var apiRequest = Api.request("CanjeItemsPyP", "rewards", "POST", "/v1/canjeItems", contexto);
            apiRequest.body(getRequest(contexto, idCanje));
            apiRequest.path("id", contexto.idCobis());
            var api = Api.response(apiRequest);
            if (api.hayError())
                return -1;
            try {
                for (Objeto canje : api.objetos("canjes")) {
                    idOperacion = canje.integer("idOperacion");
                }
            } catch (Exception e) {

            }
        }

        SqlBuhopuntosProductoService.insertarCashbackCanjeado(contexto, idOperacion.toString());
        contexto.parametros.set("puntosacanjear", contexto.parametros.integer("premio"));
        HBBuhoPuntos.updatePuntosMock(contexto);

        return idOperacion;

    }

    private static Integer aceptarPropuestaV2(ContextoHB contexto) {
        Integer respuesta = 0;
        String token = contexto.parametros.string("x-scope", null);
        int reintentos = 0;
        final int MAX_REINTENTOS = 1;

        while (reintentos <= MAX_REINTENTOS) {
            var apiRequest = Api.request("aceptarPropuesta", "productos", "POST", "/v1/propuesta/{id}/aceptar",
                    contexto);
            apiRequest.path("id", idPath(token));
            apiRequest.header("x-scope", token);
            var api = Api.response(apiRequest);

            if (!api.hayError())
                break;

            Boolean ofertaDisponible;
            try {
                ofertaDisponible = ofertaDisponible(contexto, token);
            } catch (RuntimeException e) {
                respuesta = 403;
                return respuesta;
            }
            if (!ofertaDisponible)
                break;

            if (reintentos == MAX_REINTENTOS) {
                respuesta = -1;
                return respuesta;
            }

            reintentos++;
        }
        SqlBuhopuntosProductoService.canjearPropuestas(contexto);

        new Futuro<>(() -> {
            Objeto objeto = SqlBuhopuntosProductoService.consultarPropuestas(contexto).get(0);
            RestPostventa.casoCRMPrisma(contexto, setPropuesta(objeto));
            return true;
        });

        return respuesta;
    }

    public static Integer aceptarPropuesta(ContextoHB contexto) {
        Integer respuesta = 0;
        if (ConfigHB.esProduccion())
            return aceptarPropuestaV2(contexto);

        String token = contexto.parametros.string("x-scope", null);

        String tokenMock = "porfavornomeretensequeessucioperoesporelbiendelapruebaunitariajajaja";

        if (!token.equals(tokenMock)) {
            respuesta = 403;
            return respuesta;
        }

        SqlBuhopuntosProductoService.canjearPropuestas(contexto);

        List<Objeto> rest = SqlBuhopuntosProductoService.consultarPropuestas(contexto);

        Objeto propuesta = setPropuesta(rest.get(0));
        contexto.parametros.set("puntosacanjear", propuesta.integer("puntosacanjear"));
        HBBuhoPuntos.updatePuntosMock(contexto);

        new Futuro<>(() -> {
            RestPostventa.casoCRMPrisma(contexto, propuesta);
            return true;
        });

        return respuesta;
    }

    public static Objeto getPropuestas(ContextoHB contexto) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime ahoraDateTime = LocalDateTime.now();
        LocalDate ahora = ahoraDateTime.toLocalDate();

        List<Objeto> canjes = SqlBuhopuntosProductoService.consultarPropuestas(contexto);

        Respuesta buhoPuntos = HBBuhoPuntos.consolidada(contexto);

        Objeto propuestas = new Objeto();

        for (Objeto item : canjes) {

            Objeto propuesta = setPropuesta(item);
            String fechaStr = propuesta.string("fecha_de_expiracion");

            LocalDateTime fechaVencimiento;
            try {
                fechaVencimiento = LocalDateTime.parse(fechaStr, formatter);
            } catch (DateTimeParseException e) {
                continue;
            }

            boolean noHaVencido = ahora.isBefore(fechaVencimiento.toLocalDate()) ||
                    (ahora.isEqual(fechaVencimiento.toLocalDate()) && ahoraDateTime.isBefore(fechaVencimiento));

            if (noHaVencido) {
                long diasFaltantes = ChronoUnit.DAYS.between(ahora, fechaVencimiento.toLocalDate());
                propuesta.set("dias_vencimiento", diasFaltantes);
                if (propuestaCanjeable(propuesta, buhoPuntos.integer("puntos")))
                    propuestas.add(propuesta);
            }
        }

        return propuestas;
    }

    private static boolean propuestaCanjeable(Objeto propuesta, Integer puntos) {
        return propuesta.integer("puntosacanjear") <= puntos;
    }

    public static Objeto setPropuesta(Objeto item) {
        Objeto propuesta = new Objeto();
        propuesta.set("id", item.integer("id"));

        propuesta.set("estado", item.string("estado"));
        propuesta.set("fecha_de_compra", item.string("fecha_de_compra"));
        propuesta.set("fecha_de_expiracion", item.string("fecha_de_expiracion"));
        propuesta.set("token", item.string("token"));
        propuesta.set("transaccion", item.string("transaccion"));
        propuesta.set("transaccion_id", item.integer("id").toString());
        propuesta.set("id_cobis", item.string("id_cobis"));
        propuesta.set("fecha_de_canje", item.string("fecha_de_canje"));
        propuesta.set("monto_del_premio", item.integer("monto_del_premio"));
        propuesta.set("nombre_comercio", item.string("nombre_comercio"));
        propuesta.set("puntosacanjear", item.string("puntosacanjear"));
        propuesta.set("rubro", item.string("rubro"));
        propuesta.set("nombre", buildRubroNombreComercio(propuesta));

        return propuesta;
    }

    public static Objeto setPropuestaHistorial(Objeto item) {
        Objeto propuesta = new Objeto();

        if (item.existe("id_cashback")) {
            propuesta.set("id", item.string("id_operacion"));
            String[] premio = ConfigHB.string("cashback_premio_" + item.integer("id_cashback")).split("_");
            propuesta.set("fecha_de_canje", item.string("fecha"));
            propuesta.set("monto_del_premio", premio[2]);
            propuesta.set("nombre_comercio", "Cashback");
            propuesta.set("puntosacanjear", premio[4]);
            propuesta.set("rubro", "Cashback");
            propuesta.set("nombre", "Cashback");
            propuesta.set("es_cashback", true);
        } else {
            propuesta.set("id", item.integer("id"));
            propuesta.set("fecha_de_canje", item.string("fecha_de_canje"));
            propuesta.set("monto_del_premio", item.integer("monto_del_premio"));
            propuesta.set("nombre_comercio", item.string("nombre_comercio"));
            propuesta.set("puntosacanjear", item.string("puntosacanjear"));
            propuesta.set("rubro", item.string("rubro"));
            propuesta.set("nombre", buildRubroNombreComercio(propuesta));
            propuesta.set("es_cashback", false);
        }

        return propuesta;
    }

    private static String buildRubroNombreComercio(Objeto propuesta) {
        if (!propuesta.string("rubro").isBlank() && !propuesta.string("nombre_comercio").isBlank()) {
            return propuesta.string("rubro") + " (" + propuesta.string("nombre_comercio") + ")";
        } else if (!propuesta.string("rubro").isBlank()) {
            return propuesta.string("rubro");
        } else {
            return propuesta.string("nombre_comercio");
        }
    }

    private static List<Objeto> consultarHistorial(ContextoHB contexto) {
        Futuro<List<Objeto>> requestPwp = new Futuro<List<Objeto>>(
                () -> SqlBuhopuntosProductoService.consultarPropuestasCanjeadas(contexto));
        Futuro<List<Objeto>> requestCashback = new Futuro<List<Objeto>>(
                () -> SqlBuhopuntosProductoService.consultarCashbackCanjeado(contexto));

        List<Objeto> registros;
        requestPwp.get();
        requestCashback.get();

        registros = requestPwp.get();
        registros.addAll(requestCashback.get());

        return registros;
    }

    public static List<Objeto> consultarHistorialPropuestas(ContextoHB contexto) {

        List<Objeto> historial = consultarHistorial(contexto);

        Objeto propuestas = new Objeto();

        for (Objeto item : historial) {

            Objeto propuesta = setPropuestaHistorial(item);

            propuestas.add(propuesta);

        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        List<Objeto> sortedObjetos = propuestas.objetos().stream()
                .sorted(Comparator.comparing(
                        obj -> LocalDateTime.parse((CharSequence) ((Objeto) obj).get("fecha_de_canje"), formatter),
                        Comparator.reverseOrder()))
                .collect(Collectors.toList());

        return (propuestas.lista != null && propuestas.lista.size() > 0 ? sortedObjetos : null);
    }

    /*
     * private static ApiResponse getPropuestaMobi(ContextoHB contexto, String
     * token) {
     * ApiRequest apiRequest = Api.request("obtenerPropuesta", "productos", "GET",
     * "/v1/propuesta/{id}", contexto);
     * 
     * apiRequest.path("id", idPath(token));
     * apiRequest.header("x-scope", token);
     * 
     * return Api.response(apiRequest);
     * 
     * }
     */

    private static Boolean ofertaDisponible(ContextoHB contexto, String token) {
        ApiRequest apiRequest = Api.request("obtenerPropuesta", "productos", "GET", "/v1/propuesta/{id}", contexto);

        apiRequest.path("id", idPath(token));
        apiRequest.header("x-scope", token);

        ApiResponse response = Api.response(apiRequest);
        if (response.hayError())
            throw new RuntimeException("Error al consultar la propuesta en productos");
        boolean ofertaDisponible = response.objeto("estado").bool("disponible");

        return ofertaDisponible;

    }

    public static byte[] comprobanteCashback(ContextoHB contexto) {

        List<Objeto> registros = SqlBuhopuntosProductoService.consultarCashbackCanjeado(contexto);

        Objeto cashback = registros.get(0);

        String[] premio = ConfigHB.string("cashback_premio_" + cashback.string("id_cashback")).split("_");

        Map<String, String> parametros = new HashMap<>();
        String template = "";

        if (premio[3].equals("TC")) {
            parametros.put("NOMBRE_APELLIDO", contexto.persona().nombreCompleto());
            parametros.put("FECHA_HORA", cashback.string("fecha"));
            parametros.put("MONTO_PREMIO", "$" + premio[4]);
            parametros.put("PUNTOS_CANJEADOS", premio[2]);
            parametros.put("PROPUESTA_ID", contexto.parametros.string("id_operacion"));
            parametros.put("NUMERO_TC", contexto.tarjetaCreditoTitular().numero());
            template = "bp_cashback_TC";
        } else {
            parametros.put("PROPUESTA_ID", contexto.parametros.string("id_operacion"));
            parametros.put("FECHA_HORA", cashback.string("fecha"));
            parametros.put("MONTO_PREMIO", "$" + premio[4]);
            parametros.put("NOMBRE_COMERCIO", premio[1]);
            parametros.put("PUNTOS_CANJEADOS", premio[2]);
            parametros.put("CONCEPTO", "Buho Puntos por Pesos");
            template = "bp_cashback_TD";
        }

        contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");

        return Pdf.generar(template, parametros);
    }

    public static byte[] comprobantePropuesta(ContextoHB contexto) {

        String id = contexto.parametros.string("idPropuesta", "");
        String fecha = consultaFechaCanje(contexto);
        String puntosCanjeados = contexto.parametros.string("puntosCanjeados", "");
        String nombreComercio = contexto.parametros.string("nombreComercio", "Rubro");
        String montoPremio = contexto.parametros.string("montoPremio", "");
        contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");
        String template = "buho_puntos_PWP";

        Map<String, String> parametros = new HashMap<>();
        parametros.put("PROPUESTA_ID", id);
        parametros.put("FECHA_HORA", fecha);
        parametros.put("MONTO_PREMIO", "$" + montoPremio);
        parametros.put("NOMBRE_COMERCIO", nombreComercio);
        parametros.put("PUNTOS_CANJEADOS", puntosCanjeados);
        parametros.put("CONCEPTO", "Buho Puntos por Pesos");

        return Pdf.generar(template, parametros);
    }

    private static String consultaFechaCanje(ContextoHB contexto) {

        contexto.parametros.set("id", contexto.parametros.integer("idPropuesta", null));

        List<Objeto> registros = SqlBuhopuntosProductoService.consultarPropuestas(contexto);
        Objeto propuesta = setPropuesta(registros.get(0));

        return propuesta.string("fecha_de_canje");
    }

    private static String idPath(String token) {
        String[] chunks = token.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payload = new String(decoder.decode(chunks[1]));
        chunks = payload.split(":");
        String id = chunks[2].substring(0, chunks[2].indexOf(","));
        return id;
    }
}
