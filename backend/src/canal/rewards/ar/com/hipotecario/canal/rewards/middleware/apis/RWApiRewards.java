package ar.com.hipotecario.canal.rewards.middleware.apis;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioAdherido;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioAdquirido;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioNovedad;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWRechazo;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.*;
import ar.com.hipotecario.canal.rewards.modulos.campanias.model.*;
import ar.com.hipotecario.canal.rewards.modulos.lotes.models.SolicitudLotes;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.GestionarRechazoRequestDTO;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.SolicitudRechazos;

public class RWApiRewards {
    public static List<RWBeneficioAdquirido> getBeneficionsRewardsByIdCobis(ContextoRewards contexto,
            String id) {
        ApiRequest request = new ApiRequest("Rewards", "rewards", "GET", "/v1/beneficios", contexto);
        request.query("id", id);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        List<RWBeneficioAdquirido> arrBeneficios = new ArrayList<>();
        for (Objeto obj : response.objetos()) {
            RWBeneficioAdquirido ca = new RWBeneficioAdquirido();
            ca.setCuenta(obj.string("cuenta") != null ? obj.string("cuenta") : "");
            ca.setDescTipoEstado(obj.string("descTipoEstado") != null ? obj.string("descTipoEstado") : "");
            ca.setFechaAlta(obj.string("fechaAlta") != null ? obj.string("fechaAlta") : "");
            ca.setFechaBaja(obj.string("fechaBaja") != null ? obj.string("fechaBaja") : "");
            ca.setNumeroSocio(obj.string("numeroSocio") != null ? obj.string("numeroSocio") : "");
            ca.setPrograma(obj.string("programa") != null ? obj.string("programa") : "");
            ca.setTipoTarjeta(obj.string("tipoTarjeta") != null ? obj.string("tipoTarjeta") : "");
            arrBeneficios.add(ca);
        }
        return arrBeneficios;
    }

    public static List<RWBeneficioAdherido> getBeneficionsRewardsMensualByIdCobis(ContextoRewards context,
            String idCobis,
            String fechaDesde, String fechaHasta, Boolean ultimoAnio, int pagina) {
        ApiRequest request = new ApiRequest("Rewards", "rewards", "GET", "/v1/clientes/{id}/adheridos", context);
        request.path("id", idCobis);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.query("ultimoanio", ultimoAnio);
        request.query("siguiente", pagina);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        List<RWBeneficioAdherido> arrBeneficios = new ArrayList<>();
        for (Objeto obj : response.objetos()) {
            RWBeneficioAdherido ca = new RWBeneficioAdherido(obj);
            arrBeneficios.add(ca);
        }
        return arrBeneficios;
    }

    public static List<RWBeneficioNovedad> getBeneficiosHistoricoNovedades(ContextoRewards context, String idCobis,
            String fechaDesde, String fechaHasta) {
        ApiRequest request = new ApiRequest("Rewards", "rewards", "GET", "/v1/clientes/{id}/beneficios", context);
        request.path("id", idCobis);
        request.query("fechadesde", fechaDesde);
        request.query("fechahasta", fechaHasta);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        List<RWBeneficioNovedad> arrBeneficios = new ArrayList<>();
        for (Objeto obj : response.objetos()) {
            RWBeneficioNovedad ca = new RWBeneficioNovedad(obj);
            arrBeneficios.add(ca);
        }
        return arrBeneficios;
    }

    public static ApiResponse postAjustePuntosAprobar(ContextoRewards contexto, AprovacionAjuste aprobacion) {

        ApiRequest request = new ApiRequest("API-Rewards_ModificacionAjustesRewards", "rewards", "POST",
                "/v1/ajustes/aprobaciones",
                contexto);
        request.cache = false;

        request.body(aprobacion.objeto());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }

    public static ApiResponse postAjustePuntosSolicitud(ContextoRewards contexto,
            SolicitudAjuste solicitud) {

        ApiRequest request = new ApiRequest("API-Rewards_AltaAjuste", "rewards", "POST", "/v1/ajustes/{id}/puntos",
                contexto);
        request.path("id", solicitud.getIdcliente());
        request.cache = false;

        request.body(solicitud.objeto());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;

    }

    public static List<Ajuste> getConsultaAjustes(ContextoRewards contexto,
            QueryConsultaAjustes query) {

        String url = "/v1/ajustes?estado={estado}&fechadesde={fechadesde}&fechahasta={fechahasta}&secuencial=0";

        if (query.getUsuario() != null && !query.getUsuario().isEmpty()) {
            url += "&usrin={usrin}";
        }

        ApiRequest request = new ApiRequest("Rewards", "rewards", "GET",
                url, contexto);
        request.path("estado", query.getEstado());
        request.path("fechadesde", query.getFechaDesde());
        request.path("fechahasta", query.getFechaHasta());

        if (query.getUsuario() != null && !query.getUsuario().isEmpty()) {
            request.path("usrin", query.getUsuario());
        }

        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        List<Ajuste> arrAjustes = new ArrayList<>();
        for (Objeto obj : response.objetos()) {
            Ajuste aj = new Ajuste();

            aj.setCliente(obj.integer("cliente") != null ? obj.integer("cliente") : null);
            aj.setCodigoMovimiento(obj.string("codigoMovimiento") != null ? obj.string("codigoMovimiento") : "");
            aj.setFechaIngreso(obj.string("fechaIngreso") != null ? obj.string("fechaIngreso") : "");
            aj.setHora(obj.string("hora") != null ? obj.string("hora") : "");
            aj.setLote(obj.integer("lote") != null ? obj.integer("lote") : null);
            aj.setNombre(obj.string("nombre") != null ? obj.string("nombre") : "");
            aj.setObservacion(obj.string("observacion") != null ? obj.string("observacion") : "");
            aj.setPuntos(obj.integer("puntos") != null ? obj.integer("puntos") : null);
            aj.setSecuencial(obj.string("secuencial") != null ? obj.string("secuencial") : "");
            aj.setSigno(obj.string("signo") != null ? obj.string("signo") : "");
            aj.setUsuarioIngreso(obj.string("usuarioIngreso") != null ? obj.string("usuarioIngreso") : "");

            arrAjustes.add(aj);
        }
        return arrAjustes;
    }

    public static List<Campania> getConsultaCampanias(ContextoRewards contexto, QueryConsultaCampanias query) {
        List<Campania> arrCampanias = new ArrayList<>();
        String secuencialActual = query.getSecuencial().isEmpty()?"0":query.getSecuencial();

        //La API actual de campanias solo retorna 20 registros dependiendo el valor de 'secuencial'
        //Queremos todos los registros, entonces vamos ciclando y concatenando hasta que no queden mas
        while (true) {
            String url = "/v1/campanias/totales?estado={estado}&fechaDesde={fechadesde}&fechaHasta={fechahasta}&secuencial={secuencial}";

            ApiRequest request = new ApiRequest("Rewards", "rewards", "GET",
                    url, contexto);
            request.path("estado", query.getEstado());
            request.path("fechadesde", query.getFechaDesde());
            request.path("fechahasta", query.getFechaHasta());
            request.path("secuencial", secuencialActual);
            request.cache = false;

            ApiResponse response = request.ejecutar();

            //Controlar si ya no quedan mas registros en la secuencia
            if (response.http(400) && response.string("codigo").equals("121001")){
                break;
            }

            ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
            ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

            for (Objeto obj : response.objetos()) {
                Campania campania = new Campania();

                campania.setFechaProceso(obj.string("fechaProceso") != null ? obj.string("fechaProceso") : "");
                campania.setFechaEnvio(obj.string("fechaEnvio") != null ? obj.string("fechaEnvio") : "");
                campania.setTotalRegistros(obj.string("totalRegistros") != null ? obj.string("totalRegistros") : "");
                campania.setTotalRegistrosProcesados(obj.string("totalRegistrosProcesados") != null ? obj.string("totalRegistrosProcesados") : "");
                campania.setTotalRegistrosRechazados(obj.string("totalRegistrosRechazados") != null ? obj.string("totalRegistrosRechazados") : "");
                campania.setTotalPuntos(obj.string("totalPuntos") != null ? obj.string("totalPuntos") : "");
                campania.setTotalPuntosConfirmados(obj.string("totalPuntosConfirmados") != null ? obj.string("totalPuntosConfirmados") : "");
                campania.setTotalPuntosRechazados(obj.string("totalPuntosRechazados") != null ? obj.string("totalPuntosRechazados") : "");
                campania.setUsuarioAprobacion(obj.string("usuarioAprobacion") != null ? obj.string("usuarioAprobacion") : "");
                campania.setFecha(obj.string("fecha") != null ? obj.string("fecha") : "");
                campania.setSecuencial(obj.string("secuencial") != null ? obj.string("secuencial") : "");

                arrCampanias.add(campania);
            }

            //Si el parametro secuencial que me envió el cliente no está vacío, significa que quiere
            //solo una pagina de 20 registros, si envió vacío, entonces son todos los registros
            if (!query.getSecuencial().isEmpty()){
                break;
            }

            // Actualizar el secuencial para la próxima consulta
            Campania ultima = arrCampanias.get(arrCampanias.size() - 1);
            secuencialActual = ultima.getSecuencial();
        }

        return arrCampanias;
    }

    public static List<CampaniaDetalle> getConsultaCampaniaDetalle(ContextoRewards contexto, QueryConsultaCampaniaDetalle query) {
        List<CampaniaDetalle> detalles = new ArrayList<>();
        String secuencialActual = query.getSecuencial().isEmpty()?"0":query.getSecuencial();

        //La API actual de campania detalle solo retorna 20 registros dependiendo el valor de 'secuencial'
        //Queremos todos los registros, entonces vamos ciclando y concatenando hasta que no queden mas
        while (true) {
            String url = "/v1/campanias/detalles?codigo={codigo}&secuencial={secuencial}";

            ApiRequest request = new ApiRequest("Rewards", "rewards", "GET",
                    url, contexto);
            request.path("codigo", query.getCodigo());
            request.path("secuencial", secuencialActual);
            request.cache = false;

            ApiResponse response = request.ejecutar();

            //Controlar si ya no quedan mas registros en la secuencia
            if (response.http(400) && response.string("codigo").equals("121001")){
                break;
            }

            ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("no tiene beneficios"), request, response);
            ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

            for (Objeto obj : response.objetos()) {
                CampaniaDetalle detalle = new CampaniaDetalle();

                detalle.setFechaIn(obj.string("fechaIn") != null ? obj.string("fechaIn") : "");
                detalle.setCliente(obj.string("cliente") != null ? obj.string("cliente") : "");
                detalle.setNombre(obj.string("nombre") != null ? obj.string("nombre") : "");
                detalle.setSigno(obj.string("signo") != null ? obj.string("signo") : "");
                detalle.setPuntos(obj.string("puntos") != null ? obj.string("puntos") : "");
                detalle.setCodMov(obj.string("codMov") != null ? obj.string("codMov") : "");
                detalle.setUsuario(obj.string("usuario") != null ? obj.string("usuario") : "");
                detalle.setHora(obj.string("hora") != null ? obj.string("hora") : "");
                detalle.setObserv(obj.string("observ") != null ? obj.string("observ") : "");
                detalle.setCodigoRegistroTotales(obj.string("codigoRegistroTotales") != null ? obj.string("codigoRegistroTotales") : "");
                detalle.setSecuencial(obj.string("secuencial") != null ? obj.string("secuencial") : "");

                detalles.add(detalle);
            }

            //Si el parametro secuencial que me envió el cliente no está vacío, significa que quiere
            //solo una pagina de 20 registros, si envió vacío, entonces son todos los registros
            if (!query.getSecuencial().isEmpty()){
                break;
            }

            // Actualizar el secuencial para la próxima consulta
            CampaniaDetalle ultima = detalles.get(detalles.size() - 1);
            secuencialActual = ultima.getSecuencial();
        }

        return detalles;
    }

    public static ApiResponse patchActualizarEstadoCampania(ContextoRewards contexto, ActualizarCampania actualizarCampania) {
        ApiRequest request = new ApiRequest("Rewards", "rewards", "PATCH",
                "/v1/campanias",
                contexto);
        request.cache = false;

        request.body(actualizarCampania.objeto());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;

    }
    public static ApiResponse postLotesTotales(ContextoRewards contexto,
                                                        SolicitudLotes solicitud) {

        ApiRequest request = new ApiRequest("API-Rewards_ConsultaLotesTotales", "rewards", "POST", "/v1/lotes/totales",
                contexto);
        request.cache = false;

        request.body(solicitud.objeto());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }

    public static List<RWRechazo> getRechazos(ContextoRewards contexto, String idCobis,
                                               SolicitudRechazos reqRechazos) {

        String url = "/v1/clientes/{id}/rechazos";


        ApiRequest request = new ApiRequest("Rewards", "rewards", "GET",
                url, contexto);

        request.path("id",  idCobis);
        request.cache = false;

        Object obj = reqRechazos;
        Class<?> clazz = reqRechazos.getClass();

        for (Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true); // acceder a campos privados

            try {
                Object value = field.get(obj);
                String nombreCampo = field.getName();

                if (value != null && !(value instanceof String && ((String) value).isEmpty())) {
                    //agrego como query param solo los campos que sean distinto a "" o null.
                    request.query(nombreCampo, value.toString());
                }

            } catch (IllegalAccessException e) {
                // Podés registrar o lanzar una RuntimeException si querés cortar el flujo
                throw new RuntimeException("Error al acceder al campo " + field.getName(), e);
            }
        }

        ApiResponse response = request.ejecutar();

        ApiException.throwIf("CLIENTE_NO_EXISTE", response.contains("No tiene rechazos"), request, response);
        ApiException.throwIf(!response.http(200) || response.objetos().isEmpty(), request, response);

        List<RWRechazo> arrRechazos = new ArrayList<>();
        for (Objeto objeto : response.objetos()) {
            RWRechazo rechazo = new RWRechazo(objeto);

            if(!reqRechazos.getIdRechazo().isEmpty()){
                if(reqRechazos.getIdRechazo().equals(rechazo.getCodigoRechazo())){
                    arrRechazos.add(rechazo);
                }
            }
            else{
                arrRechazos.add(rechazo);
            }
        }

        return arrRechazos;
    }
    public static ApiResponse postRechazos(ContextoRewards contexto, GestionarRechazoRequestDTO gestionRechazoDto) {

        ApiRequest request = new ApiRequest("API-Rewards_GestionRechazos", "rewards", "POST",
                "/v1/clientes/rechazos",
                contexto);
        request.cache = false;

        request.body(gestionRechazoDto.objeto());

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        return response;
    }
}