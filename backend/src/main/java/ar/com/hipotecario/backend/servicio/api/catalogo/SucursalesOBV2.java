package ar.com.hipotecario.backend.servicio.api.catalogo;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;

public class SucursalesOBV2 extends ApiObjeto {

        public int codfilial;
        public String descfilial;
        public String oficial;
        public int codSucursal;
        public String desSucursal;
        public String domicilio;
        public String idCiudad;
        public String ciudad;
        public String nombreSucursal;
        public String codigoPostal;
        public String codigoCategoria;
        public String desTipoSucursal;
        public String descsucadmin;
        public int codProvincia;
        public String nomProvincia;


    public static SucursalesOBV2 post(ContextoOB contexto, String codSucursal){
        ApiRequest request = new ApiRequest("API-Catalogo_ConsultarSucursalesV2","catalogo","GET","/v2/sucursales/{codSucursal}",contexto);
        request.path("codSucursal",codSucursal);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200),request,response);
        return response.crear(SucursalesOBV2.class);
    }

}
