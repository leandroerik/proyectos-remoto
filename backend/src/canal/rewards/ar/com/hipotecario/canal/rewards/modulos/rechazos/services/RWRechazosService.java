package ar.com.hipotecario.canal.rewards.modulos.rechazos.services;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.RechazosAr;
import ar.com.hipotecario.backend.servicio.api.catalogo.RechazosEd;
import ar.com.hipotecario.backend.servicio.api.catalogo.DocumentosRewards;
import ar.com.hipotecario.backend.servicio.api.catalogo.CatalogoActividades; // o Actividades si el nombre de la clase es solo Actividades
import ar.com.hipotecario.backend.servicio.api.catalogo.EstadosCiviles;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sexos;
import ar.com.hipotecario.canal.rewards.core.LogRW;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.core.RespuestaRW;
import ar.com.hipotecario.canal.rewards.middleware.apis.RWApiRewards;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWBeneficioNovedad;
import ar.com.hipotecario.canal.rewards.middleware.models.negocio.RWRechazo;
import ar.com.hipotecario.canal.rewards.modulos.ajustar_puntos.model.ResponseAprobarAjuste;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.GestionarRechazoRequestDTO;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.GestionarRechazoResponse;
import ar.com.hipotecario.canal.rewards.modulos.rechazos.model.SolicitudRechazos;

import java.util.ArrayList;
import java.util.List;

public class RWRechazosService {

    public static Objeto getRechazosEd(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            // Llamar a ApiCatalogo.rechazosEd que devuelve un Futuro<RechazosEd>
            Futuro<RechazosEd> futuroRechazosEd = ApiCatalogo.rechazosEd(contexto);
            // Obtener el resultado del Futuro. Esto puede bloquear hasta que el resultado
            // esté
            // disponible.
            // Si la operación en ApiCatalogo.rechazosEd ya es síncrona dentro del
            // Util.futuro,
            // .get() devolverá el resultado inmediatamente o lanzará la excepción
            // encapsulada.
            RechazosEd data = futuroRechazosEd.get(); // Esto puede lanzar Exception

            response.set("status", 0); // 0 para éxito
            response.set("data", data);
        } catch (ApiException e) {
            // Loguear el error si es necesario: LogRW.error(contexto,
            // "RWRechazosService.getRechazosEd - ApiException", e);
            response.set("status", 1); // 1 para error
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getRechazosEd - ApiException", e, "ERROR_API_RECHAZOS_ED"));
        } catch (Exception e) { // Captura más genérica por el .get() del Futuro
            // Loguear el error si es necesario: LogRW.error(contexto,
            // "RWRechazosService.getRechazosEd - Exception", e);
            response.set("status", 1); // 1 para error
            response.set("error",
                    RespuestaRW.error(contexto, "RWRechazosService.getRechazosEd - Exception", e,
                            "ERROR_INESPERADO_RECHAZOS_ED"));
        }
        return response;
    }

    public static Objeto getRechazosAr(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            // Llamar a ApiCatalogo.rechazosAr que devuelve un Futuro<RechazosAr>
            Futuro<RechazosAr> futuroRechazosAr = ApiCatalogo.rechazosAr(contexto);
            // Obtener el resultado del Futuro.
            RechazosAr data = futuroRechazosAr.get(); // Esto puede lanzar Exception

            response.set("status", 0); // 0 para éxito
            response.set("data", data);
        } catch (ApiException e) {
            // Loguear el error si es necesario: LogRW.error(contexto,
            // "RWRechazosService.getRechazosAr - ApiException", e);
            response.set("status", 1); // 1 para error
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getRechazosAr - ApiException", e, "ERROR_API_RECHAZOS_AR"));
        } catch (Exception e) { // Captura más genérica por el .get() del Futuro
            // Loguear el error si es necesario: LogRW.error(contexto,
            // "RWRechazosService.getRechazosAr - Exception", e);
            response.set("status", 1); // 1 para error
            response.set("error",
                    RespuestaRW.error(contexto, "RWRechazosService.getRechazosAr - Exception", e,
                            "ERROR_INESPERADO_RECHAZOS_AR"));
        }
        return response;
    }

    public static Objeto getDocumentosRewards(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            Futuro<DocumentosRewards> futuroDocumentosRewards = ApiCatalogo.documentosRewards(contexto);
            DocumentosRewards data = futuroDocumentosRewards.get();
            response.set("status", 0);
            response.set("data", data);
        } catch (ApiException e) {
            LogRW.error(contexto, e, "RWRechazosService.getDocumentosRewards - ApiException");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getDocumentosRewards - ApiException", e, "ERROR_API_DOCUMENTOS_REWARDS"));
        } catch (Exception e) {
            LogRW.error(contexto, e, "RWRechazosService.getDocumentosRewards - Exception");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getDocumentosRewards - Exception", e, "ERROR_INESPERADO_DOCUMENTOS_REWARDS"));
        }
        return response;
    }

    public static Objeto getActividades(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            Futuro<CatalogoActividades> futuroActividades = ApiCatalogo.actividades(contexto);
            CatalogoActividades data = futuroActividades.get();
            response.set("status", 0);
            response.set("data", data);
        } catch (ApiException e) {
            LogRW.error(contexto, e, "RWRechazosService.getActividades - ApiException");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getActividades - ApiException", e, "ERROR_API_ACTIVIDADES"));
        } catch (Exception e) {
            LogRW.error(contexto, e, "RWRechazosService.getActividades - Exception");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getActividades - Exception", e, "ERROR_INESPERADO_ACTIVIDADES"));
        }
        return response;
    }

    public static Objeto getEstadosCiviles(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            Futuro<EstadosCiviles> futuroEstadosCiviles = ApiCatalogo.estadosCiviles(contexto);
            EstadosCiviles data = futuroEstadosCiviles.get();
            response.set("status", 0);
            response.set("data", data);
        } catch (ApiException e) {
            LogRW.error(contexto, e, "RWRechazosService.getEstadosCiviles - ApiException");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getEstadosCiviles - ApiException", e, "ERROR_API_ESTADOS_CIVILES"));
        } catch (Exception e) {
            LogRW.error(contexto, e, "RWRechazosService.getEstadosCiviles - Exception");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getEstadosCiviles - Exception", e, "ERROR_INESPERADO_ESTADOS_CIVILES"));
        }
        return response;
    }

    public static Objeto getSexos(ContextoRewards contexto) {
        Objeto response = new Objeto();
        try {
            Futuro<Sexos> futuroSexos = ApiCatalogo.sexos(contexto);
            Sexos data = futuroSexos.get();
            response.set("status", 0);
            response.set("data", data);
        } catch (ApiException e) {
            LogRW.error(contexto, e, "RWRechazosService.getSexos - ApiException");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getSexos - ApiException", e, "ERROR_API_SEXOS"));
        } catch (Exception e) {
            LogRW.error(contexto, e, "RWRechazosService.getSexos - Exception");
            response.set("status", 1);
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.getSexos - Exception", e, "ERROR_INESPERADO_SEXOS"));
        }
        return response;
    }

    public static Objeto getRechazosClientes(ContextoRewards contexto, String idCobis, SolicitudRechazos reqRechazos ){
        Objeto response = new Objeto();

        try {
            List<RWRechazo> resultados = new ArrayList<>();

            resultados = RWApiRewards.getRechazos(contexto, idCobis , reqRechazos);

            response.set("status", 0);
            response.set("data", resultados);

        } catch (ApiException e) {
            if (e.response.codigoHttp.equals(204)) {
                response.set("error", RespuestaRW.error(contexto, "SIN_DATOS"));
            } else {
                response.set("error", RespuestaRW.error(contexto,
                        "RERechazosService.getRechazosClientes - ApiException", e, ""));
            }

        } catch (Exception e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RERechazosService.getRechazosClientes - Exception", e, ""));
        }

        return response;
    }

    static public Objeto postRechazos(ContextoRewards contexto, List<RWRechazo> rechazosModificaciones) {
        Objeto response = new Objeto();

        try {
            List<GestionarRechazoResponse> lstGestionRechazo = new ArrayList();
            //No pude probar en desa ni en homo el parseo del body.
            //List<String> lstBody = new ArrayList<>();


            for (RWRechazo rechazo : rechazosModificaciones) {
                GestionarRechazoRequestDTO gestionRech = new GestionarRechazoRequestDTO(rechazo.toStringGestionRechazo());
                ApiResponse resultados = RWApiRewards.postRechazos(contexto, gestionRech);

                GestionarRechazoResponse respuestaRechazo = GestionarRechazoResponse.fromJson(resultados.body);
                lstGestionRechazo.add(respuestaRechazo);
                //lstBody.add(resultados.body);
            }

            response.set("status",200);
            response.set("data", lstGestionRechazo);
            return response;
        } catch (ApiException e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.postRechazos - ApiException", e, ""));
            return response;

        } catch (Exception e) {
            response.set("error", RespuestaRW.error(contexto,
                    "RWRechazosService.postRechazos - Exception", e, ""));
            return response;
        }

    }
}
