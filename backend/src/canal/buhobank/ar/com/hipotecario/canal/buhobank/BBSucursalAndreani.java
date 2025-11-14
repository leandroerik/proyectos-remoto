package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.base.HttpRequest;
import ar.com.hipotecario.backend.base.HttpResponse;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BBSucursalAndreani extends ApiObjeto {

    private static Objeto getSucursales(ContextoBB contexto) {
        try {
            String urlBase = contexto.config.string("buhobank_apiandreani_url");
            HttpRequest request = new HttpRequest("GET", urlBase);
            HttpResponse response = request.run();

            if (response.code != 200) {
                throw new RuntimeException("Error en la respuesta de la API: Código " + response.code);
            }

            return response.jsonBody();

        } catch (Exception e) {
            LogBB.evento(contexto, GeneralBB.ERROR_GET_INFORMACION_SUCURSALES, "Error al consultar sucursales: " + e.getMessage());
            return null;
        }
    }


    public static Objeto obtenerSucursales(ContextoBB contexto) {

        String claveCache = GeneralBB.SUCURSALES_ANDREANI;
        try {
            String sucursalesCacheadas = contexto.get(claveCache);

            if (!empty(sucursalesCacheadas)) {
                return Objeto.fromJson(sucursalesCacheadas);
            }

            Objeto responseJson = getSucursales(contexto);

            List<Object> sucursales = responseJson.toList();

            List<Object> sucursalesFiltradas = sucursales.stream()
                    .filter(sucursal -> sucursal instanceof Map)
                    .map(Map.class::cast)
                    .filter(mapa -> {
                        String canal = (String) mapa.get("canal");
                        List<String>codigosPostalesAtendidos = (List<String>) mapa.get("codigosPostalesAtendidos");
                        Map<String, Object> datosAdi = (Map<String, Object>) mapa.get("datosAdicionales");
                        boolean seHaceAtencionAlCliente = (boolean) datosAdi.get("seHaceAtencionAlCliente");
                        String tipo = (String) datosAdi.get("tipo");

                        return "B2C".equals(canal) && seHaceAtencionAlCliente && "SUCURSAL".equals(tipo) && codigosPostalesAtendidos != null;
                    })
                    .map(mapa -> {
                        Objeto datoSuc = new Objeto();
                        datoSuc.set("id", mapa.get("id"));
                        datoSuc.set("direccion", mapa.get("direccion"));
                        datoSuc.set("horarioDeAtencion", mapa.get("horarioDeAtencion"));
                        datoSuc.set("telefonos", mapa.get("telefonos"));
                        datoSuc.set("descripcion", mapa.get("descripcion"));
                        return datoSuc;
                    })
                    .collect(Collectors.toList());

            Objeto sucursalesRespuesta = Objeto.fromList(sucursalesFiltradas);

            contexto.set(claveCache, sucursalesRespuesta.toString(), GeneralBB.BB_REDIS_EXPIRACION);

            return sucursalesRespuesta;

        } catch (Exception e) {
            LogBB.evento(contexto, GeneralBB.ERROR_GET_INFORMACION_SUCURSALES, "Error al consultar sucursales: " + e.getMessage());
            return null;
        }
    }
}
