package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales.Sucursal;

public class SucursalesOB extends ApiObjetos<SucursalesOB.SucursalOB> {

    /* ========== ATRIBUTOS ========== */
    public static class SucursalOB extends ApiObjeto {
        public String CodSucursal;
        public String DesSucursal;
        public String Domicilio;
        public String HorarioAtencion;
        public String desTipoSucursal;
        public String NomProvincia;
        public Integer codTipoSucursal;
        public Integer codProvincia;
        public BigDecimal Latitud;
        public BigDecimal Longitud;
        public Boolean AudioNoVidentes;
        public String CodigoPostal;
    }

    public static class SucursalGeo extends ApiObjeto {
        public String id;
        public String desSucursal;
        public String domicilio;
        public String provincia;
        public BigDecimal distancia;
    }

    /* ========== SERVICIOS ========== */
    static SucursalesOB get(Contexto contexto) {
        return get(contexto, null, null, null);
    }

    // API-Catalogo_ConsultaSucursales
    static SucursalesOB get(Contexto contexto, String provincia, String sucursal, String tipoSucursal) {
        ApiRequest request = new ApiRequest("sucursales", "catalogo", "GET", "/v1/sucursales", contexto);
        request.query("codProvincia", provincia);
        request.query("codSucursal", sucursal);
        request.query("codTipoSucursal", tipoSucursal);
        request.cache = true;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados"), request, response);
        return response.crear(SucursalesOB.class);
    }



}
