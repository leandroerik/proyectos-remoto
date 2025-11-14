package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.assembler;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper.*;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.port.*;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.dto.TASPosicionConsolidadaV4DTO;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.*;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TASPosicionConsolidadaV4Assembler {
    private static TASCuentasV4Port portCta;
    private static TASCajasSeguridadV4Port portCaja;
    private static TASInversionesV4Port portInv;
    private static TASPrestamosV4Port portPrestamo;
    private static TASTarjetasDebitoV4Port portTD;
    private static TASPlazosFijosV4Port portPF;
    private static TASPaquetesV4Port portPaq;
    public static void init(TASCuentasV4Port injectedPortCta, TASCajasSeguridadV4Port injectedPortCaja, TASInversionesV4Port injectedPortInv
            ,TASPrestamosV4Port injectedPortPrestamo, TASTarjetasDebitoV4Port injectedPortTD, TASPlazosFijosV4Port injectedPortPF, TASPaquetesV4Port injectedPortPaq) {
        portCta = injectedPortCta;
        portCaja = injectedPortCaja;
        portInv = injectedPortInv;
        portPrestamo = injectedPortPrestamo;
        portTD = injectedPortTD;
        portPF = injectedPortPF;
        portPaq = injectedPortPaq;
    }

    public static TASPosicionConsolidadaV4DTO crearModeloProductos(ContextoTAS contexto, Objeto pcons) {
        List<TASProductosGenericosV4> listResponsePCons = new ArrayList<TASProductosGenericosV4>();
        for (Objeto producto : pcons.objetos()) {
            listResponsePCons.add(producto.toClass(TASProductosGenericosV4.class));
        }
        TASPosicionConsolidadaV4DTO modelo = new TASPosicionConsolidadaV4DTO();
        //Cuentas
        modelo.setCuentas(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("CUENTA").contains(p.getCodigoProducto()),
                        TASCuentasV4Mapper::mapear)));

        //Cajas Seguridad
        modelo.setCajasSeguridad(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("CAJA_SEGURIDAD").contains(p.getCodigoProducto()),
                        TASCajaSeguridadV4Mapper::mapear)));

        //Inversiones
        modelo.setInversiones(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("INVERSION").contains(p.getCodigoProducto()),
                        TASInversionesV4Mapper::mapear)));

        // Plazos Fijos
        modelo.setPlazosFijos(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("PLAZO_FIJO").contains(p.getCodigoProducto()),
                        TASPlazosFijosV4Mapper::mapear)));

        // Préstamos
        modelo.setPrestamos(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("PRESTAMO").contains(p.getCodigoProducto()),
                        TASPrestamosV4Mapper::mapear)));

        // Tarjetas de Débito
        modelo.setTarjetasDebito(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> codigosPorTipo.get("TARJETA_DEBITO").contains(p.getCodigoProducto()),
                        TASTarjetasDebitoV4Mapper::mapear)));

        // Productos genéricos (excluidos por código)
        modelo.setProductos(TASPosicionConsolidadaV4Utils.validarLista(
                filtrarYMapear(listResponsePCons,
                        p -> p.getCodigoProducto() != null &&
                                !Arrays.asList("3", "4", "202", "16", "82", "14", "7").contains(p.getCodigoProducto()),
                        TASProductosV4Mapper::mapear)));
        //mock prueba no viene producto
        /*modelo.setCuentas(null);
        modelo.setCajasSeguridad(null);
        modelo.setInversiones(null);
        modelo.setPlazosFijos(null);
        modelo.setPrestamos(null);
        modelo.setTarjetasDebito(null);*/
        return modelo;
    }
    private static final Map<String, List<String>> codigosPorTipo = Map.of(
            "CUENTA", List.of("3", "4"),
            "CAJA_SEGURIDAD", List.of("82"),
            "INVERSION", List.of("202"),
            "PLAZO_FIJO", List.of("14"),
            "PRESTAMO", List.of("7"),
            "TARJETA_DEBITO", List.of("16"),
            "TARJETA_CREDITO", List.of("28")
    );

    private static boolean contiene(String campo, String valor) {
        return campo != null && campo.toUpperCase().contains(valor.toUpperCase());
    }
    private static <T> List<T> filtrarYMapear(List<TASProductosGenericosV4> origen,
                                              Predicate<TASProductosGenericosV4> filtro,
                                              Function<TASProductosGenericosV4, T> mapper) {
        return origen.stream()
                .filter(filtro)
                .map(mapper)
                .collect(Collectors.toList());
    }

    public static Objeto recuperarDatos(ContextoTAS contexto, String idCliente,TASPosicionConsolidadaV4DTO lista){
            //verificacion de datos y llamado al servicio
            Futuro<List <TASCuentasPConsV4>> futuroCtas = new Futuro<>(()-> getDatosCuentas(contexto, idCliente, lista.getCuentas()));
            Futuro<List <TASCajasSegPConsV4>> futuroCajasSeg = new Futuro<>(()-> getDatosCajasSeguridad(contexto, idCliente, lista.getCajasSeguridad()));
            Futuro<List <TASInversionesPConsV4>> futuroInversiones = new Futuro<>(()-> getDatosinversiones(contexto, idCliente, lista.getInversiones()));
            Futuro<List <TASPlazosFijosPConsV4>> futuroPF = new Futuro<>(() -> getDatosPF(contexto, idCliente, lista.getPlazosFijos()));
            Futuro<List <TASPrestamosPConsV4>> futuroPrestamo = new Futuro<>(()-> getDatosPrestamos(contexto, idCliente, lista.getPrestamos()));
            Futuro<List <TASTarjetasDebitoPConsV4>> futuroTD = new Futuro<>(()-> getDatosTD(contexto, idCliente, lista.getTarjetasDebito()));
            Futuro<List <TASProductosPConsV4>> futuroProd = new Futuro<>(()-> getDatosProductos(contexto, idCliente, lista.getProductos()));

            //seteo los resultados de cada producto
            TASPosicionConsolidadaV4DTO response = new TASPosicionConsolidadaV4DTO();
            //test servicio y mapeo
            //response.setCuentas(getDatosCuentas(contexto,idCliente,lista.getCuentas()));
            response.setCuentas(futuroCtas.get());

            //test servicio y mapeo
            //response.setCajasSeguridad(getDatosCajasSeguridad(contexto, idCliente, lista.getCajasSeguridad()));
            response.setCajasSeguridad(futuroCajasSeg.get());

            //test servicio y mapeo
            //response.setInversiones(getDatosinversiones(contexto, idCliente, lista.getInversiones()));
            response.setInversiones(futuroInversiones.get());

            //test servicio y mapeo
            //response.setPlazosFijos(getDatosPF(contexto, idCliente, lista.getPlazosFijos()));
            response.setPlazosFijos(futuroPF.get());

            //test servicio y mapeo
            //response.setPrestamos(getDatosPrestamos(contexto, idCliente, lista.getPrestamos()));
            response.setPrestamos(futuroPrestamo.get());

            //test servicio y mapeo
            //response.setTarjetasDebito(getDatosTD(contexto, idCliente, lista.getTarjetasDebito()));
            response.setTarjetasDebito(futuroTD.get());

            //test servicio y mapeo
            //response.setProductos(getDatosProductos(contexto, idCliente, lista.getProductos()));
            response.setProductos(futuroProd.get());

//            //agrego TC
            response.setTarjetasCredito(lista.getTarjetasCredito());

            return verificarErroresApi(response);
    }

    private static List<TASCuentasPConsV4> getDatosCuentas(ContextoTAS contexto, String idCliente, List<TASCuentasPConsV4> cuentasGenericas) {
        try {
            Objeto ctasCliente = portCta.getDatosCuentas(contexto, idCliente, false, false, false, false,"vigente");

            if (ctasCliente.integer("codigoHTTP") == 200) {
                boolean sinDatosBase = cuentasGenericas == null || cuentasGenericas.isEmpty();
                if (sinDatosBase) {
                    // Mapeo completo si no vino nada en v4
                    return TASCuentasV4Mapper.mapeoCompleto(ctasCliente);
                }
            }
            // Si vino algo en v4, enriquecemos parcialmente
            List<TASCuentasPConsV4> cuentasEnriquecidas = new ArrayList<>();
            Map<String, Objeto> cuentasMap = ctasCliente.objetos().stream()
                    .collect(Collectors.toMap(obj -> obj.string("numeroProducto"), Function.identity()));

            if (cuentasGenericas != null) {
                for (TASCuentasPConsV4 cta : cuentasGenericas) {
                    if (faltanDatosCta(cta)) {
                        Objeto datos = cuentasMap.get(cta.getNumeroProducto());
                        if (datos != null) {
                            cuentasEnriquecidas.add(TASCuentasV4Mapper.enriquecerCtas(cta, datos));
                        } else {
                            cuentasEnriquecidas.add(cta);
                        }
                    } else {
                        cuentasEnriquecidas.add(cta);
                    }
                }
            }

            return cuentasEnriquecidas;
        } catch (Exception e) {
            if (cuentasGenericas != null) {
                for (TASCuentasPConsV4 cta : cuentasGenericas) {
                    cta.setErrorApi(true);
                    cta.setErrorDesc(e);
                }
            }
            return cuentasGenericas;
        }
    }
    private static boolean faltanDatosCta(TASCuentasPConsV4 cta){
        if(cta.getNumeroProducto() == null && cta.getIdProducto() == null){
            return false;
        }else{
            return cta.getIdPaquete() == null && cta.getSucursal() == null;
        }
    }
    private static List<TASCajasSegPConsV4> getDatosCajasSeguridad(ContextoTAS contexto, String idCliente, List<TASCajasSegPConsV4> cajasGenericas) {
        try {
            Objeto cajasCliente = portCaja.getDatosCajas(contexto, idCliente, false);

            if (cajasCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) cajasCliente;
                boolean sinDatosBase = cajasGenericas == null || cajasGenericas.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASCajaSeguridadV4Mapper.mapeoCompleto(cajasCliente);
                }
            }
            List<TASCajasSegPConsV4> cajasEnriquecidas = new ArrayList<>();
            Map<String, Objeto> cajasMap = cajasCliente.objetos().stream()
                    .collect(Collectors.toMap(obj -> obj.string("numeroProducto"), Function.identity()));

            if (cajasGenericas != null) {
                for (TASCajasSegPConsV4 caja : cajasGenericas) {
                    if (faltanDatosCaja(caja)) {
                        Objeto datos = cajasMap.get(caja.getNumeroProducto());
                        if (datos != null) {
                            cajasEnriquecidas.add(TASCajaSeguridadV4Mapper.enriquecerCajas(caja, datos));
                        } else {
                            cajasEnriquecidas.add(caja);
                        }
                    } else {
                        cajasEnriquecidas.add(caja);
                    }
                }
            }
            return cajasEnriquecidas;
        } catch (Exception e) {
            if (cajasGenericas != null) {
                for (TASCajasSegPConsV4 cajaError : cajasGenericas) {
                    cajaError.setErrorApi(true);
                    cajaError.setErrorDesc(e);
                }
            }
            return cajasGenericas;
        }
    } private static boolean faltanDatosCaja(TASCajasSegPConsV4 caja){
        if(caja.getNumeroProducto() == null && caja.getIdProducto() == null){
            return false;
        }else{
            return caja.getSucursal() == null && caja.getEstadoCajaSeguridad() == null;
        }
    }
    private static List<TASInversionesPConsV4> getDatosinversiones(ContextoTAS contexto, String idCliente, List<TASInversionesPConsV4> inversionesGenericas) {
        try {
            Objeto invCliente = portInv.getDatosInversiones(contexto, idCliente, "vigente", false);

            if (invCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) invCliente;
                boolean sinDatosBase = inversionesGenericas == null || inversionesGenericas.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASInversionesV4Mapper.mapeoCompleto(invCliente);
                }
            }
            List<TASInversionesPConsV4> inversionesEnriquecidas = new ArrayList<>();
            Map<String, Objeto> invMap = invCliente.objetos().stream()
                    .collect(Collectors.toMap(obj -> obj.string("numeroProducto"), Function.identity()));

            if (inversionesGenericas != null) {
                for (TASInversionesPConsV4 inversion : inversionesGenericas) {
                    if (faltanDatosInversiones(inversion)) {
                        Objeto datos = invMap.get(inversion.getNumeroProducto());
                        if (datos != null) {
                            inversionesEnriquecidas.add(TASInversionesV4Mapper.enriquecerInversiones(inversion, datos));
                        } else {
                            inversionesEnriquecidas.add(inversion);
                        }
                    } else {
                        inversionesEnriquecidas.add(inversion);
                    }
                }
            }
            return inversionesEnriquecidas;
        } catch (Exception e) {
            if (inversionesGenericas != null) {
                for (TASInversionesPConsV4 invError : inversionesGenericas) {
                    invError.setErrorApi(true);
                    invError.setErrorDesc(e);
                }
            }
            return inversionesGenericas;
        }
    }private static boolean faltanDatosInversiones(TASInversionesPConsV4 inv){
        if(inv.getNumeroProducto() == null && inv.getIdProducto() == null){
            return false;
        }else{
            return inv.getSucursal() == null && inv.getDescEstado() == null;
        }
    }

    private static List<TASPrestamosPConsV4> getDatosPrestamos(ContextoTAS contexto, String idCliente, List<TASPrestamosPConsV4> prestamosGenericos) {
        try {
            Objeto prestamoCliente = portPrestamo.getDatosPrestamo(contexto, idCliente, "vigente", false);

            if (prestamoCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) prestamoCliente;
                boolean sinDatosBase = prestamosGenericos == null || prestamosGenericos.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASPrestamosV4Mapper.mapeoCompleto(prestamoCliente);
                }
            }
            List<TASPrestamosPConsV4> prestamosEnriquecidos = new ArrayList<>();
            Map<String, Objeto> prestamoMap = prestamoCliente.objetos().stream()
                    .collect(Collectors.toMap(obj -> obj.string("numeroProducto"), Function.identity()));

            if (prestamosGenericos != null) {
                for (TASPrestamosPConsV4 prestamo : prestamosGenericos) {
                    if (faltanDatosPrestamos(prestamo)) {
                        Objeto datos = prestamoMap.get(prestamo.getNumeroProducto());
                        if (datos != null) {
                            prestamosEnriquecidos.add(TASPrestamosV4Mapper.enriquecerPrestamo(prestamo, datos));
                        } else {
                            prestamosEnriquecidos.add(prestamo);
                        }
                    } else {
                        prestamosEnriquecidos.add(prestamo);
                    }
                }
            }
            return prestamosEnriquecidos;
        } catch (Exception e) {
            if (prestamosGenericos != null) {
                for (TASPrestamosPConsV4 prestamoError : prestamosGenericos) {
                    prestamoError.setErrorApi(true);
                    prestamoError.setErrorDesc(e);
                }
            }
            return prestamosGenericos;
        }
    }private static boolean faltanDatosPrestamos(TASPrestamosPConsV4 prestamo){
        if(prestamo.getNumeroProducto() == null && prestamo.getIdProducto() == null){
            return false;
        }else{
            return prestamo.getSucursal() == null && prestamo.getDescEstado() == null;
        }
    }

    private static List<TASTarjetasDebitoPConsV4> getDatosTD(ContextoTAS contexto, String idCliente, List<TASTarjetasDebitoPConsV4> tdGenericas) {
        try {
            Objeto tdCliente = portTD.getDatosTD(contexto, idCliente, "vigente");

            if (tdCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) tdCliente;
                boolean sinDatosBase = tdGenericas == null || tdGenericas.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASTarjetasDebitoV4Mapper.mapeoCompleto(tdCliente);
                }
            }
            List<TASTarjetasDebitoPConsV4> tdEnriquecidas = new ArrayList<>();
            Map<String, Objeto> tdMap = tdCliente.objetos().stream()
                    .collect(Collectors.toMap(obj -> obj.string("numeroProducto"), Function.identity()));

            if (tdGenericas != null) {
                for (TASTarjetasDebitoPConsV4 td : tdGenericas) {
                    if (faltanDatosTD(td)) {
                        Objeto datos = tdMap.get(td.getNumeroProducto());
                        if (datos != null) {
                            tdEnriquecidas.add(TASTarjetasDebitoV4Mapper.enriquecerTD(td, datos));
                        } else {
                            tdEnriquecidas.add(td);
                        }
                    } else {
                        tdEnriquecidas.add(td);
                    }
                }
            }
            return tdEnriquecidas;
        } catch (Exception e) {
            if (tdGenericas != null) {
                for (TASTarjetasDebitoPConsV4 tdError : tdGenericas) {
                    tdError.setErrorApi(true);
                    tdError.setErrorDesc(e);
                }
            }
            return tdGenericas;
        }
    } private static boolean faltanDatosTD(TASTarjetasDebitoPConsV4 td){
        if(td.getNumeroProducto() == null && td.getIdProducto() == null){
            return false;
        }else{
            return td.getSucursal() == null && td.getDescEstado() == null;
        }
    }

    private static List<TASPlazosFijosPConsV4> getDatosPF(ContextoTAS contexto, String idCliente, List<TASPlazosFijosPConsV4> pfgenericos) {
        try {
            Objeto pfCliente = portPF.getDatosPF(contexto, idCliente, "vigente", false);

            if (pfCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) pfCliente;
                boolean sinDatosBase = pfgenericos == null || pfgenericos.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASPlazosFijosV4Mapper.mapeoCompleto(pfCliente);
                }
            }
            return pfgenericos;
        } catch (Exception e) {
            if (pfgenericos != null) {
                for (TASPlazosFijosPConsV4 pfError : pfgenericos) {
                    pfError.setErrorApi(true);
                    pfError.setErrorDesc(e);
                }
            }
            return pfgenericos;
        }
    }
    private static List<TASProductosPConsV4> getDatosProductos(ContextoTAS contexto, String idCliente, List<TASProductosPConsV4> prodGenericos) {
        try {
            Objeto paqCliente = portPaq.getDatosPaquetes(contexto, idCliente, "N");
            if (paqCliente instanceof ApiResponse) {
                ApiResponse response = (ApiResponse) paqCliente;
                boolean sinDatosBase = prodGenericos == null || prodGenericos.isEmpty();

                if (response.codigoHttp == 200 && sinDatosBase) {
                    return TASProductosV4Mapper.mapeoCompleto(paqCliente);
                }
            }
            return prodGenericos;
        } catch (Exception e) {
            if (prodGenericos != null) {
                for (TASProductosPConsV4 prodError : prodGenericos) {
                    prodError.setErrorApi(true);
                    prodError.setErrorDesc(e);
                }
            }
            return prodGenericos;
        }
    }
    public static Objeto verificarErroresApi(TASPosicionConsolidadaV4DTO productos){
        //todo este metodo tiene q devolver la respuesta final...
        //todo: recorrer cada producto, verificar q no tengan error de servicio, pasarlo a objejo, armar response
        Objeto response = new Objeto();
        Objeto errores = new Objeto();
        boolean errorGlobal = false;
        boolean errorApi = false;
        if(productos.getCuentas() != null && !productos.getCuentas().isEmpty()){
           Objeto ctas = new Objeto();
            for(TASCuentasPConsV4 cuenta : productos.getCuentas()){
                errorApi = cuenta.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && cuenta.getErrorDesc() instanceof ApiException){
                        ApiException apiException= (ApiException) cuenta.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                ctas.add(TASCuentasV4Mapper.cuentaToObjeto(cuenta));
            }
            if(!errorGlobal) {
                response.set("cuentas", ctas);
            }
        }

        if(productos.getCajasSeguridad() != null && !productos.getCajasSeguridad().isEmpty()){
            Objeto cajas = new Objeto();
            for(TASCajasSegPConsV4 caja : productos.getCajasSeguridad()){
                errorApi = caja.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && caja.getErrorDesc() instanceof ApiException){
                        ApiException apiException= (ApiException) caja.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                cajas.add(TASCajaSeguridadV4Mapper.cajaToObjeto(caja));
            }
            if(!errorGlobal){
                response.set("cajasSeguridad", cajas);
            }
        }
        if(productos.getInversiones() != null && !productos.getInversiones().isEmpty()){
            Objeto inversiones = new Objeto();
            for(TASInversionesPConsV4 inversion : productos.getInversiones()){
                errorApi = inversion.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && inversion.getErrorDesc() instanceof ApiException){
                        ApiException apiException = (ApiException) inversion.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                inversiones.add(TASInversionesV4Mapper.inversionToObjeto(inversion));
            }
            if(!errorGlobal){
                response.set("inversiones", inversiones);
            }
        }
        if(productos.getPlazosFijos() != null && !productos.getPlazosFijos().isEmpty()){
            Objeto plazosFijos = new Objeto();
            for(TASPlazosFijosPConsV4 pf : productos.getPlazosFijos()){
                errorApi = pf.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && pf.getErrorDesc() instanceof ApiException){
                        ApiException apiException = (ApiException) pf.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                plazosFijos.add(TASPlazosFijosV4Mapper.pfToObjeto(pf));
            }
            if(!errorGlobal){
                response.set("plazosFijos", plazosFijos);
            }
        }
        if(productos.getPrestamos() != null && !productos.getPrestamos().isEmpty()){
            Objeto prestamos = new Objeto();
            for(TASPrestamosPConsV4 prestamo : productos.getPrestamos()){
                errorApi = prestamo.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && prestamo.getErrorDesc() instanceof ApiException){
                        ApiException apiException = (ApiException) prestamo.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                prestamos.add(TASPrestamosV4Mapper.prestamoToObjeto(prestamo));
            }
            if(!errorGlobal){
                response.set("prestamos", prestamos);
            }
        }
        if(productos.getTarjetasDebito() != null && !productos.getTarjetasDebito().isEmpty()){
            Objeto tarjetas = new Objeto();
            for(TASTarjetasDebitoPConsV4 td : productos.getTarjetasDebito()){
                errorApi = td.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && td.getErrorDesc() instanceof ApiException){
                        ApiException apiException = (ApiException) td.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                tarjetas.add(TASTarjetasDebitoV4Mapper.tdToObjeto(td));
            }
            if(!errorGlobal){
                response.set("tarjetasDebito", tarjetas);
            }
        }
        if(productos.getTarjetasCredito() != null && !productos.getTarjetasCredito().isEmpty()){
            Objeto tarjetas = new Objeto();
            for(TASTarjetasCreditoPConsV4 tc : productos.getTarjetasCredito()){
                tarjetas.add(TASTarjetasCreditoV4Mapper.tcToObjeto(tc));
            }
            response.set("tarjetasCredito", tarjetas);
        }
        if(productos.getProductos() != null && !productos.getProductos().isEmpty()){
            Objeto paquetes = new Objeto();
            for(TASProductosPConsV4 paq : productos.getProductos()){
                errorApi = paq.isErrorApi();
                if(errorApi){
                    if(!errorGlobal && paq.getErrorDesc() instanceof ApiException){
                        ApiException apiException = (ApiException) paq.getErrorDesc();
                        Objeto errorDesc = TASPosicionConsolidadaV4Utils.mapearError(apiException);
                        errores.add(errorDesc);
                        errorGlobal = true;
                    }
                    continue;
                }
                paquetes.add(TASProductosV4Mapper.paqueteToObjeto(paq));
            }
            if(!errorGlobal){
                response.set("productos", paquetes);
            }
        }
        if(errores != null && !errores.isEmpty()){
            response.set("errores", errores);
        }

        return response;
    }


}
