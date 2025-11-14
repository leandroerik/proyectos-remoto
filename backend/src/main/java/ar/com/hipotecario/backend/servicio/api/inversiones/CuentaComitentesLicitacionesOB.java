package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.util.List;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;



public class CuentaComitentesLicitacionesOB extends ApiObjetos<ar.com.hipotecario.backend.servicio.api.inversiones.CuentaComitentesLicitaciones.CuentaComitenteLicitacion> {

    public List<CuentaComitenteLicitacion> posicionesNegociablesOrdenadas;
    public Object paginacion;

    /* ========== ATRIBUTOS ========== */
    public static class CuentaComitenteLicitacion extends ApiObjeto {
        public Integer ordenSecuencial;
        public String codigo;
        public String cuentaCustodia;
        public String descripcionCuenta;
        public String descripcionTenencia;
        public BigDecimal saldoNominal;
        public BigDecimal ventasPactadas;
        public BigDecimal saldoDisponible;
        public String clasificacion;
        public String idTipoRelacion;
        public String situacion;
        public String usoDeCuenta;
        public String tipoRelacion;
        public String fechaPrecioConsulta;
        public String precioConsulta;
        public String variacion;

    }

	/* ========== SERVICIOS ========== */
	// API-Inversiones_ConsultaPosicionNegociable
	public static CuentaComitentesLicitacionesOB get(Contexto contexto, String id, String cuentaComitente, Fecha fecha, String cantregistros, String secuencial) {
	    ApiRequest request = new ApiRequest("CuentasComitenteLicitaciones", "inversiones", "GET", "/v1/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
	    request.path("id", id);
	    request.path("cuentaComitente", cuentaComitente);
	    request.query("cantregistros", cantregistros);
	    request.query("fecha", fecha.string("yyyy-MM-dd"));
	    request.query("secuencial", secuencial);
	    request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
	    CuentaComitentesLicitacionesOB r = response.crear(CuentaComitentesLicitacionesOB.class);
	    return r;
	}

    public static CuentaComitentesLicitacionesOB getV2(Contexto contexto, String id, String cuentaComitente, Fecha fecha, String cantregistros, String secuencial) {
        ApiRequest request = new ApiRequest("CuentasComitenteLicitaciones", "inversiones", "GET", "/v2/cuentascomitentes/{id}/licitaciones/{cuentaComitente}", contexto);
        request.path("id", id);
        request.path("cuentaComitente", cuentaComitente);
        request.query("cantregistros", cantregistros);
        request.query("fecha", fecha.string("yyyy-MM-dd"));
        request.query("secuencial", secuencial);
        request.cache = false;

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        CuentaComitentesLicitacionesOB r = response.crear(CuentaComitentesLicitacionesOB.class);
        return r;
    }

	/* ========== TEST ========== */
	/*public static void main(String[] args) {
		Fecha fecha = new Fecha("2020-01-01", "yyyy-MM-dd");
		Contexto contexto = contexto("HB", "homologacion");
		CuentaComitentesLicitacionesOB datos = get(contexto, "554", "2-000108703", fecha, "100", "1");
		imprimirResultado(contexto, datos);
	}*/

}
