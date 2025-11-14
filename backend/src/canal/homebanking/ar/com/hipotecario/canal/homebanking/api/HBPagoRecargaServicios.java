package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.TarjetaDebito;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;

public class HBPagoRecargaServicios {

    /**
     * Constantes Api Link
     */

    private static final String LINK = "link";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String PUT = "PUT";
    private static final String DELETE = "DELETE";
    private static final String CODIGO_RESULTADO = "codigoResultado";
    private static final String DESCRIPCION_RESULTADO = "descripcionResultado";
    private static final String VALOR_CODIGO_RESULTADO_OK = "00";
    private static final String VALOR_CODIGO_RESULTADO_ADHSION_EXISTENTE = "154";
    private static final String VALOR_DESCRIPCION_RESULTADO_OK = "OK";
    private static final String CAJA_AHORRO = "11";
    private static final String CUENTA_CORRIENTE = "01";
    private static final String FORMATO_FECHA_BUSQUEDA = "yyyyMMdd";
    private static final String LINEAS_POR_PAGINA = "10";
    private static final String NUMERO_PAGINA = "1";
    private static final String FECHA_DESDE_DEFAULT = "20000101";

    private static final String URL_RUBROS = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/rubros";
    private static final String URL_EMPRESAS = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/rubros/{codigoRubro}/empresas";
    private static final String URL_ADHESION = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/adhesiones";
    private static final String URL_DELETE_ADHESION = "/v1/servicios/recargas//{numeroTarjeta}/{numeroUsuario}/adhesiones/{codigoEmpresa}/usuarios/{idServicio}";
    private static final String URL_ADHESIONES = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/adhesiones/{codigoEmpresa}/usuarios/{idUsuario}";
    private static final String URL_COMPRAS = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/compras";
    private static final String URL_RECARGA_SERVICIO = "/v1/servicios/recargas/{numeroTarjeta}/{numeroUsuario}/recargas";

    /**
     * Constantes Errores
     */
    private static final String MENSAJE_USUARIO = "mensajeAlUsuario";
    private static final String MENSAJE_FALTA_HEADER = "Falta header x-";
    private static final String MENSAJE_TARJETA_FORMATO = "El numeroTarjeta sólo puede contener caracteres numéricos";
    private static final String MENSAJE_CODIGO_RUBRO_FORMATO = "El codigoRubro sólo puede contener caracteres alfabéticos";
    private static final String MENSAJE_IMPORTE_OBLIGATORIO = "Es obligatorio indicar el CodigoImporteCVAL ó el Importe Libre";
    private static final String MENSAJE_FECHA_DESDE_FORMATO = "La Fecha Desde debe tener el formato yyyymmdd";
    private static final String MENSAJE_FECHA_HASTA_FORMATO = "La Fecha Hasta debe tener el formato yyyymmdd";
    private static final String MENSAJE_FECHA_HASTA_MENOR = "La Fecha Hasta no puede ser menor a la Fecha Desde";
    private static final String MENSAJE_CODIGO_RUBRO_OBLIGATORIO = "El Codigo Rubro es obligatorio si ingresa Codigo Empresa";

    private static final String ERROR_FALTA_HEADER = "ERROR_FALTA_HEADER";
    private static final String ERROR_TARJETA_FORMATO = "ERROR_TARJETA_FORMATO";
    private static final String ERROR_CODIGO_RUBRO_FORMATO = "ERROR_CODIGO_RUBRO_FORMATO";
    private static final String ERROR_IMPORTE_OBLIGATORIO = "ERROR_IMPORTE_OBLIGATORIO";
    private static final String ERROR_FECHA_DESDE_FORMATO = "ERROR_FECHA_DESDE_FORMATO";
    private static final String ERROR_FECHA_HASTA_FORMATO = "ERROR_FECHA_HASTA_FORMATO";
    private static final String ERROR_FECHA_HASTA_MENOR = "ERROR_FECHA_HASTA_MENOR";
    private static final String ERROR_CODIGO_RUBRO_OBLIGATORIO = "ERROR_CODIGO_RUBRO_OBLIGATORIO";
    private static final String API_LINK_400 = "API_LINK_400";
    private static final String SIN_PSEUDO_SESION = "SIN_PSEUDO_SESION";
    private static final String SIN_TARJETA_DEBITO = "SIN_TARJETA_DEBITO";
    private static final String ERROR_RUBROS = "ERROR_RUBROS";
    private static final String ERROR_EMPRESAS = "ERROR_EMPRESAS";
    private static final String ERROR_ADHESIONES = "ERROR_ADHESIONES";
    private static final String ERROR_HISTORIAL = "ERROR_HISTORIAL";
    private static final String ERROR_ALTA_ADHESIONES = "ERROR_ALTA_ADHESIONES";
    private static final String ERROR_ACTUALIZACION_ADHESIONES = "ERROR_ACTUALIZACION_ADHESIONES";
    private static final String ERROR_ELIMINA_ADHESIONES = "ERROR_ELIMINA_ADHESIONES";
    private static final String API_LINK = "API_LINK";
    private static final String TARJETA_DEBITO_INVALIDA = "TARJETA_DEBITO_INVALIDA";
    private static final String PARAMETROS = "PARAMETROS";
    private static final String RUBRO_INVALIDO = "RUBRO_INVALIDO";
    private static final String BANCO_INVALIDO = "BANCO_INVALIDO";
    private static final String ADHESION_EXISTENTE = "ADHESION_EXISTENTE";
    private static final String ADHESION_INEXISTENTE = "ADHESION_INEXISTENTE";
    private static final String EMPRESA_INVALIDA = "EMPRESA_INVALIDA";
    private static final String ERROR_RECARGA = "ERROR_RECARGA";
    private static final String CUENTA_INEXISTENTE = "CUENTA_INEXISTENTE";
    private static final String ERROR_TARJETA_DEBITO = "ERROR_TARJETA_DEBITO";
    private static final String ERROR_CUENTA = "ERROR_CUENTA";
    private static final String ERROR_TRANSACCION = "ERROR_TRANSACCION";
    private static final String USUARIO_BLOQUEADO = "USUARIO_BLOQUEADO";
    private static final String IMPORTE_INVALIDO = "IMPORTE_INVALIDO";
    private static final String ERROR_SUBE = "ERROR_SUBE";
    private static final String ERROR_TARJETA = "ERROR_TARJETA";
    private static final String SALDO_INSUFICIENTE = "SALDO_INSUFICIENTE";

    /**
     * Obtiene todos los rubros para Recargas.
     *
     * @param contexto
     * @return listado de rubros.
     */
    public static Respuesta obtenerRubrosRecarga(ContextoHB contexto) {
        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiResponse response = obtenerRubros(contexto);
        if (response.hayError())
            return castearErrorApiLink(response, ERROR_RUBROS);

        return esResultadoOk(response) ? Respuesta.exito("rubros", response.objeto("rubros").objeto("rubro").ordenar("descripcion")) : castearRespuestaError(response);

    }

    /**
     * Obtiene todos las Empresas correspondientes a un Rubro.
     *
     * @param contexto
     * @return listado de empresas.
     */
    public static Respuesta obtenerEmpresasPorRubroRecarga(ContextoHB contexto) {
        String codigoRubro = contexto.parametros.string("codigoRubro");

        if (Objeto.anyEmpty(codigoRubro))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiResponse apiResponse = obtenerEmpresas(contexto);

        if (apiResponse.hayError())
            return castearErrorApiLink(apiResponse, ERROR_EMPRESAS);

        if (esResultadoOk(apiResponse)) {
            List<Objeto> empresas = apiResponse.objeto("empresas").objetos("empresa");

            if (codigoRubro.equals(ConfigHB.string("hb_recarga_rubro_celulares")) && empresas.stream().filter(e -> e.string("codigo").equals(ConfigHB.string("hb_recarga_codigo_personal"))).count() != 0)
                empresas.stream().filter(e -> e.string("codigo").equals(ConfigHB.string("hb_recarga_codigo_personal"))).collect(Collectors.toList()).get(0).set("descripcion", ConfigHB.string("hb_recarga_descripcion_personal"));

            if (codigoRubro.equals(ConfigHB.string("hb_recarga_rubro_billeteras")))
                empresas = empresas.stream().filter(e -> Arrays.asList(ConfigHB.string("hb_recarga_codigos_billeteras").split(",")).contains(e.string("codigo"))).collect(Collectors.toList());

            Collections.sort(empresas, new Comparator<Objeto>() {
                @Override
                public int compare(Objeto e1, Objeto e2) {
                    return e1.string("descripcion").compareTo(e2.string("descripcion"));
                }
            });

            if (empresas.stream().filter(e -> e.string("descripcion").toUpperCase().equals("SUBE")).count() != 0)
                empresas = rearrange(empresas, empresas.stream().filter(e -> e.string("descripcion").toUpperCase().equals("SUBE")).collect(Collectors.toList()).get(0));

            return Respuesta.exito("empresas", empresas);
        }

        return castearRespuestaError(apiResponse);
    }

    /**
     * Da de alta la Adhesión de un Usuario a un Servicio de Recarga.
     *
     * @param contexto
     * @return Respuesta.
     */
    public static Respuesta altaAdhesion(ContextoHB contexto) {

        String codigoEmpresa = contexto.parametros.string("codigoEmpresa");
        String descripcion = contexto.parametros.string("descripcion");
        String numeroServicio = contexto.parametros.string("numeroServicio");

        if (Objeto.anyEmpty(codigoEmpresa, descripcion, numeroServicio))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        TarjetaDebito tarjetaDebito = obtenerTarjetaDebitoPorDefecto(contexto);

        /*
         * Primero debo dar de alta la adhesión y luego actualizar la descripcion se
         * hace en dos pasos porque está hecho así en LINK
         */
        ApiRequest request = Api.request("LinkAltaAdhesiones", LINK, POST, URL_ADHESION, contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.path("numeroUsuario", contexto.idCobis());

        request.body("idServicio", numeroServicio);
        request.body("codigoEmpresa", codigoEmpresa);
        request.body("descripcion", descripcion);

        ApiResponse response = Api.response(request);
        if (response.hayError())
            return castearErrorApiLink(response, ERROR_ALTA_ADHESIONES);

        /*
         * Verifico errores propios de LINK y paso el caso excepcional de adhesion
         * existente
         */
        if (!esResultadoOk(response) && !esResultadoAdhesionExistente(response))
            return castearRespuestaError(response);

        request = Api.request("LinkActualizacionAdhesiones", LINK, PUT, URL_ADHESION, contexto);
        request.path("numeroTarjeta", tarjetaDebito.numero());
        request.path("numeroUsuario", contexto.idCobis());

        request.body("idServicio", numeroServicio);
        request.body("codigoEmpresa", codigoEmpresa);
        request.body("descripcion", descripcion);

        response = Api.response(request);
        if (response.hayError())
            return castearErrorApiLink(response, ERROR_ACTUALIZACION_ADHESIONES);

        /*
         * Verifico errores propios de LINK
         */
        if (!esResultadoOk(response))
            return castearRespuestaError(response);

        return Respuesta.exito();
    }

    /**
     * Obtiene las Adhesiones de un Usuario a un Servicio de Recarga.
     *
     * @param contexto
     * @return adhesiones.
     */
    public static Respuesta obtenerAdhesionesPorEmpresa(ContextoHB contexto) {
        String codigoEmpresa = contexto.parametros.string("codigoEmpresa");

        if (Objeto.anyEmpty(codigoEmpresa))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiResponse apiResponse = obtenerAdhesiones(contexto);

        if (apiResponse.hayError())
            return castearErrorApiLink(apiResponse, ERROR_ADHESIONES);

        return esResultadoOk(apiResponse) ? Respuesta.exito("adhesiones", apiResponse.objeto("usuarios").objeto("usuario").ordenar("descripcion")) : castearRespuestaError(apiResponse);
    }

    /**
     * Obtiene las Adhesiones de un Usuario a Todas Las Empresas de todo los rubros.
     *
     * @param contexto
     * @return adhesiones.
     */
    public static Respuesta obtenerAdhesionesTodosRubros(ContextoHB contexto) {
        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiResponse apiResponse = obtenerRubros(contexto);
        if (apiResponse.hayError())
            return castearErrorApiLink(apiResponse, ERROR_RUBROS);

        if (!esResultadoOk(apiResponse))
            return castearRespuestaError(apiResponse);

        // LLAMADAS EN PARALELO
        Map<String, Futuro<ApiResponse>> mapaEmpresas = new HashMap<>();
        for (Objeto rubro : apiResponse.objeto("rubros").objeto("rubro").ordenar("descripcion").objetos()) {
            ContextoHB nuevoContexto = contexto.clonar();
            nuevoContexto.parametros.set("codigoRubro", rubro.get("codigo"));
            Futuro<ApiResponse> futuroEmpresas = new Futuro<>(() -> obtenerEmpresas(nuevoContexto));
            mapaEmpresas.put(rubro.string("codigo"), futuroEmpresas);
        }

        Map<String, Futuro<ApiResponse>> mapaAdhesiones = new HashMap<>();
        for (Objeto rubro : apiResponse.objeto("rubros").objeto("rubro").ordenar("descripcion").objetos()) {
            for (Objeto empresa : mapaEmpresas.get(rubro.string("codigo")).get().objeto("empresas").objeto("empresa").ordenar("descripcion").objetos()) {
                ContextoHB nuevoContexto = contexto.clonar();
                nuevoContexto.parametros.set("codigoRubro", rubro.get("codigo"));
                nuevoContexto.parametros.set("codigoEmpresa", empresa.get("codigo"));
                Futuro<ApiResponse> futuroAdhesiones = new Futuro<>(() -> obtenerAdhesiones(nuevoContexto));
                mapaAdhesiones.put(rubro.string("codigo") + empresa.string("codigo"), futuroAdhesiones);
            }
        }
        // FIN LLAMADAS EN PARALELO

        List<Objeto> adhesiones = new ArrayList<>();
        for (Objeto rubro : apiResponse.objeto("rubros").objeto("rubro").ordenar("descripcion").objetos()) {
            contexto.parametros.set("codigoRubro", rubro.get("codigo"));
            ApiResponse apiResponseEmpresas = mapaEmpresas.get(rubro.string("codigo")).get();
            if (!apiResponseEmpresas.hayError()) {
                for (Objeto empresa : apiResponseEmpresas.objeto("empresas").objeto("empresa").ordenar("descripcion").objetos()) {
                    contexto.parametros.set("codigoEmpresa", empresa.get("codigo"));
                    ApiResponse apiResponseAdhesiones = mapaAdhesiones.get(rubro.string("codigo") + empresa.string("codigo")).get();
                    if (!apiResponseAdhesiones.hayError()) {
                        apiResponseAdhesiones.objeto("usuarios").objeto("usuario").objetos().stream().forEach(u -> {
                            Objeto adhesion = new Objeto();
                            adhesion.set("codigoRubro", rubro.get("codigo"));
                            adhesion.set("codigoEmpresa", empresa.get("codigo"));
                            adhesion.set("descripcionEmpresa", rubro.get("codigo").equals(ConfigHB.string("hb_recarga_rubro_celulares")) && empresa.get("codigo").equals(ConfigHB.string("hb_recarga_codigo_personal")) ? ConfigHB.string("hb_recarga_descripcion_personal") : empresa.get("descripcion"));
                            adhesion.set("codigoAdhesion", u.get("codigo"));
                            adhesion.set("descripcionAdhesion", u.get("descripcion"));
                            adhesiones.add(adhesion);
                        });
                    }
                }
            }
        }

        Collections.sort(adhesiones, new Comparator<Objeto>() {
            @Override
            public int compare(Objeto o1, Objeto o2) {
                return o1.string("descripcionAdhesion").compareTo(o2.string("descripcionAdhesion"));
            }
        });

        return Respuesta.exito("adhesiones", adhesiones);
    }

    /**
     * Elimina la Adhesión de un Usuario a un Servicio de Recarga.
     *
     * @param contexto
     * @return Respuesta.
     */
    public static Respuesta eliminaAdhesion(ContextoHB contexto) {

        String codigoEmpresa = contexto.parametros.string("codigoEmpresa");
        String numeroServicio = contexto.parametros.string("numeroServicio");

        if (Objeto.anyEmpty(codigoEmpresa, numeroServicio))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiRequest request = Api.request("LinkEliminarAdhesion", LINK, DELETE, URL_DELETE_ADHESION, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());
        request.path("idServicio", numeroServicio);
        request.path("codigoEmpresa", codigoEmpresa);

        ApiResponse response = Api.response(request);
        if (response.hayError())
            return castearErrorApiLink(response, ERROR_ELIMINA_ADHESIONES);

        return esResultadoOk(response) ? Respuesta.exito() : castearRespuestaError(response);
    }

    /**
     * Historial de recargas con filtros.
     *
     * @param contexto
     * @return lista de recargas.
     */
    public static Respuesta historialRecargas(ContextoHB contexto) {

        String codigoRubro = contexto.parametros.string("codigoRubro");
        String codigoEmpresa = contexto.parametros.string("codigoEmpresa", null);
        // los formatos de las fechas tienen que ser yyyymmdd
        // si los parámetros fecha son nulos los seteo al ser obligatorios
        String fechaDesde = contexto.parametros.string("fechaDesde", FECHA_DESDE_DEFAULT);
        String fechaHasta = contexto.parametros.string("fechaHasta", LocalDate.now().format(DateTimeFormatter.ofPattern(FORMATO_FECHA_BUSQUEDA)));
        // si los parámetros de numero y lineas son nulos los seteo por ser obligatorios
        String numeroPagina = contexto.parametros.string("numeroPagina", NUMERO_PAGINA);
        String lineasPorPagina = contexto.parametros.string("lineasPorPagina", LINEAS_POR_PAGINA);
        BigDecimal importe = contexto.parametros.bigDecimal("importe");
        String datoFiliatorio = contexto.parametros.string("datoFiliatorio", null);

        if (Objeto.anyEmpty(codigoRubro))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        ApiRequest request = Api.request("LinkHistorialRecargas", LINK, POST, URL_COMPRAS, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());

        request.body("codigoRubro", codigoRubro);
        request.body("codigoEmpresa", codigoEmpresa);
        request.body("fechaDesde", fechaDesde);
        request.body("fechaHasta", fechaHasta);
        request.body("numeroPagina", numeroPagina);
        request.body("lineasPorPagina", lineasPorPagina);
        request.body("datoFiliatorio", datoFiliatorio);
        request.body("importe", importe != null ? Formateador.importeTlf(importe, 12) : null);

        ApiResponse response = Api.response(request);

        if (codigoRubro.equals(ConfigHB.string("hb_recarga_rubro_celulares")) && response.objeto("recargas").objetos("recarga").stream().filter(e -> e.string("empresa").equals(ConfigHB.string("hb_recarga_descripcion_personal"))).count() != 0) {
            response.objeto("recargas").objetos("recarga").stream().forEach(e -> {
                if (e.string("empresa").equals(ConfigHB.string("hb_recarga_descripcion_personal")))
                    e.set("empresa", ConfigHB.string("hb_recarga_descripcion_personal"));
            });
        }

        if (response.hayError())
            return castearErrorApiLink(response, ERROR_HISTORIAL);

        return esResultadoOk(response) ? Respuesta.exito("page", response) : castearRespuestaError(response);
    }

    /**
     * Recarga de Servicio contra Link.
     *
     * @param contexto
     * @return Respuesta.
     */
    public static Respuesta recargaServicio(ContextoHB contexto) {

        String numeroCuenta = contexto.parametros.string("numeroCuenta");
        String codigoEmpresa = contexto.parametros.string("codigoEmpresa");
        String codigoImporteCVAL = contexto.parametros.string("codigoImporteCVAL", null);
        BigDecimal importeLibre = contexto.parametros.bigDecimal("importeLibre");
        String numeroServicio = contexto.parametros.string("numeroServicio");

        if (Objeto.anyEmpty(codigoEmpresa, numeroCuenta, numeroServicio))
            return Respuesta.parametrosIncorrectos();

        if (Objeto.empty(codigoImporteCVAL) && Objeto.empty(importeLibre))
            return Respuesta.parametrosIncorrectos();

        Respuesta respuesta = validarContexto(contexto);
        if (respuesta != null)
            return respuesta;

        Cuenta cuenta = contexto.cuenta(numeroCuenta);
        if (cuenta == null)
            return Respuesta.estado(CUENTA_INEXISTENTE);

        Respuesta respuestaPausada = HBTarjetas.verificarTarjetaDebitoPausadaEnCuenta(cuenta, contexto);
        if (respuestaPausada != null)
            return respuestaPausada;

        if (!Objeto.anyEmpty(importeLibre) && cuenta.saldo().compareTo(importeLibre) == -1)
            return Respuesta.estado(SALDO_INSUFICIENTE);

        TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociadaHabilitadaLink(cuenta);

        if (Objeto.anyEmpty(tarjetaDebito))
            return Respuesta.estado(SIN_TARJETA_DEBITO);

        ApiRequest request = Api.request("LinkRecargaServicio", LINK, POST, URL_RECARGA_SERVICIO, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());

        request.body("idServicio", numeroServicio);
        request.body("codigoEmpresa", codigoEmpresa);
        request.body("codigoImporteCVAL", codigoImporteCVAL);
        request.body("importeLibre", importeLibre != null ? Formateador.importeTlf(importeLibre, 12) : null);
        request.body("tipoCuenta", cuenta.esCajaAhorro() ? CAJA_AHORRO : CUENTA_CORRIENTE);
        request.body("cuentaPBF", cuenta.numero());
        request.body("tarjetaDebito", tarjetaDebito.numero());

        ApiResponse response = Api.response(request);
        if (response.hayError())
            return castearErrorApiLink(response, ERROR_RECARGA);

        if (!esResultadoOk(response))
            return castearRespuestaError(response);

        ProductosService.eliminarCacheProductos(contexto);

        return Respuesta.exito("recarga", response);
    }

    /**
     * Valida los datos necesarios para poder comunicarse con api-link
     *
     * @param contexto
     * @return respuesta de validaciones.
     */
    private static Respuesta validarContexto(ContextoHB contexto) {
        if (contexto.idCobis() == null)
            return Respuesta.estado(SIN_PSEUDO_SESION);

        if (obtenerTarjetaDebitoPorDefecto(contexto) == null)
            return Respuesta.estado(SIN_TARJETA_DEBITO);

        return null;
    }

    /**
     * Obtengo la tarjeta de débito por defecto del cliente
     *
     * @param contexto
     * @return Tarjeta de Debito.
     */
    private static TarjetaDebito obtenerTarjetaDebitoPorDefecto(ContextoHB contexto) {
        return contexto.tarjetaDebitoPorDefecto(contexto.tarjetasDebito());
    }

    /**
     * Valida si el resultado del response es OK
     *
     * @param response
     * @return resultado OK o no.
     */
    private static boolean esResultadoOk(ApiResponse response) {
        if (Objeto.anyEmpty(response.string(CODIGO_RESULTADO), response.string(DESCRIPCION_RESULTADO)))
            return false;
        return response.string(CODIGO_RESULTADO).equals(VALOR_CODIGO_RESULTADO_OK) && response.string(DESCRIPCION_RESULTADO).equals(VALOR_DESCRIPCION_RESULTADO_OK);
    }

    /**
     * Valida si el resultado del response es adhesion existente
     *
     * @param response
     * @return resultado OK o no.
     */
    private static boolean esResultadoAdhesionExistente(ApiResponse response) {
        return response.string(CODIGO_RESULTADO).equals(VALOR_CODIGO_RESULTADO_ADHSION_EXISTENTE);
    }

    /**
     * Obtener los rubros
     *
     * @param response
     * @return respuesta
     */
    private static ApiResponse obtenerRubros(ContextoHB contexto) {
        ApiRequest request = Api.request("LinkObtenerRubros", LINK, GET, URL_RUBROS, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());

        return Api.response(request);
    }

    /**
     * Obtener las empresas
     *
     * @param response
     * @return respuesta
     */
    private static ApiResponse obtenerEmpresas(ContextoHB contexto) {
        String codigoRubro = contexto.parametros.string("codigoRubro");

        ApiRequest request = Api.request("LinkObtenerEmpresas", LINK, GET, URL_EMPRESAS, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());
        request.path("codigoRubro", codigoRubro);

        return Api.response(request);
    }

    /**
     * Obtener Adhesiones por Empresa
     *
     * @param response
     * @return respuesta
     */
    private static ApiResponse obtenerAdhesiones(ContextoHB contexto) {
        String codigoEmpresa = contexto.parametros.string("codigoEmpresa");

        ApiRequest request = Api.request("LinkObtenerAdhesiones", LINK, GET, URL_ADHESIONES, contexto);
        request.path("numeroTarjeta", obtenerTarjetaDebitoPorDefecto(contexto).numero());
        request.path("numeroUsuario", contexto.idCobis());
        request.path("codigoEmpresa", codigoEmpresa);

        return Api.response(request);
    }

    /**
     * Castea los mensajes de error 400 de las apis de recargas
     *
     * @param response
     * @return respuesta con error correspondiente.
     */
    private static Respuesta castearRespuestaError400(ApiResponse response) {
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_FALTA_HEADER))
            return Respuesta.estado(ERROR_FALTA_HEADER);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_CODIGO_RUBRO_FORMATO))
            return Respuesta.estado(ERROR_CODIGO_RUBRO_FORMATO);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_CODIGO_RUBRO_OBLIGATORIO))
            return Respuesta.estado(ERROR_CODIGO_RUBRO_OBLIGATORIO);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_FECHA_DESDE_FORMATO))
            return Respuesta.estado(ERROR_FECHA_DESDE_FORMATO);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_FECHA_HASTA_MENOR))
            return Respuesta.estado(ERROR_FECHA_HASTA_MENOR);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_FECHA_HASTA_FORMATO))
            return Respuesta.estado(ERROR_FECHA_HASTA_FORMATO);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_IMPORTE_OBLIGATORIO))
            return Respuesta.estado(ERROR_IMPORTE_OBLIGATORIO);
        if (response.string(MENSAJE_USUARIO).contains(MENSAJE_TARJETA_FORMATO))
            return Respuesta.estado(ERROR_TARJETA_FORMATO);
        return Respuesta.estado(API_LINK_400);
    }

    /**
     * Castea los mensajes de error de las apis de recargas
     *
     * @param response
     * @return Respuesta.
     */
    private static Respuesta castearRespuestaError(ApiResponse response) {
        String estado = "";
        switch (response.string(CODIGO_RESULTADO)) {
            case "50":
                estado = ERROR_TARJETA_DEBITO;
                break;
            case "51":
                estado = ERROR_TARJETA_DEBITO;
                break;
            case "52":
                estado = ERROR_TARJETA_DEBITO;
                break;
            case "55":
                estado = ERROR_TARJETA_DEBITO;
                break;
            case "56":
                estado = ERROR_CUENTA;
                break;
            case "57":
                estado = ERROR_TRANSACCION;
                break;
            case "58":
                estado = ERROR_CUENTA;
                break;
            case "59":
                estado = ERROR_CUENTA;
                break;
            case "60":
                estado = ERROR_CUENTA;
                break;
            case "61":
                estado = ERROR_CUENTA;
                break;
            case "62":
                estado = USUARIO_BLOQUEADO;
                break;
            case "63":
                estado = ERROR_CUENTA;
                break;
            case "64":
                estado = IMPORTE_INVALIDO;
                break;
            case "66":
                estado = ERROR_CUENTA;
                break;
            case "67":
                estado = IMPORTE_INVALIDO;
                break;
            case "68":
                estado = API_LINK;
                break;
            case "70":
                estado = API_LINK;
                break;
            case "71":
                estado = API_LINK;
                break;
            case "72":
                estado = API_LINK;
                break;
            case "114":
                estado = IMPORTE_INVALIDO;
                break;
            case "100":
                estado = PARAMETROS;
                break;
            case "104":
                estado = BANCO_INVALIDO;
                break;
            case "105":
                estado = TARJETA_DEBITO_INVALIDA;
                break;
            case "107":
                estado = RUBRO_INVALIDO;
                break;
            case "109":
                estado = EMPRESA_INVALIDA;
                break;
            case "111":
                estado = ADHESION_INEXISTENTE;
                break;
            case "154":
                estado = ADHESION_EXISTENTE;
                break;
            case "B7":
                estado = ERROR_SUBE;
                break;
            case "C4":
                estado = ERROR_SUBE;
                break;
            case "C5":
                estado = ERROR_SUBE;
                break;
            case "C6":
                estado = ERROR_SUBE;
                break;
            case "ZR":
                estado = IMPORTE_INVALIDO;
                break;
            case "ZS":
                estado = ERROR_TRANSACCION;
                break;
            case "CE":
                estado = ERROR_TARJETA;
                break;
            case "CF":
                estado = ERROR_TARJETA;
                break;
            case "CG":
                estado = ERROR_TARJETA;
                break;
            case "CO":
                estado = ERROR_TARJETA;
                break;
            case "CP":
                estado = ERROR_TARJETA;
                break;
            case "CY":
                estado = ERROR_TARJETA;
                break;
            case "CZ":
                estado = ERROR_TARJETA;
                break;
            case "D0":
                estado = ERROR_TARJETA;
                break;
            case "D8":
                estado = ERROR_TARJETA;
                break;
            case "D9":
                estado = ERROR_TARJETA;
                break;
            case "DA":
                estado = ERROR_TARJETA;
                break;
            case "DI":
                estado = ERROR_TARJETA;
                break;
            case "DJ":
                estado = ERROR_TARJETA;
                break;
            case "DH":
                estado = IMPORTE_INVALIDO;
                break;
            default:
                estado = API_LINK;
                break;
        }
        return Respuesta.estado(estado);
    }

    /**
     * Retorna respuesta correspondiente con error
     *
     * @param response
     * @param errorDefault
     * @return Respuesta.
     */
    private static Respuesta castearErrorApiLink(ApiResponse response, String errorDefault) {
        return response.codigo == 400 ? castearRespuestaError400(response) : Respuesta.estado(errorDefault);
    }

    /**
     * Envía el elemento a la primera posición de la lista
     *
     * @param items
     * @param input
     * @return lista con el elemnto al principio.
     */
    private static <T> List<T> rearrange(List<T> items, T input) {
        int index = items.indexOf(input);
        List<T> copy;
        if (index >= 0) {
            copy = new ArrayList<T>(items.size());
            copy.add(items.get(index));
            copy.addAll(items.subList(0, index));
            copy.addAll(items.subList(index + 1, items.size()));
        } else {
            return items;
        }
        return copy;
    }

}
