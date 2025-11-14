package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB.CuentaOB;

public class CuentasOB extends ApiObjetos<CuentaOB> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaOB extends ApiObjeto {
		public String numeroProducto;
		public String tipoProducto;
		public String descMoneda;
		public String moneda;
		public String descEstado;
		public String estado;
		public String cbu;
		public String descTipoTitularidad;
		public String tipoTitularidad;
		public String sucursal;
		public String disponible;
		public String estadoCuenta;
		public String acuerdo;
	}

    // API-Cuentas_ConsultaCuentaIdcliente
    public static CuentasOB get(Contexto contexto, String idCliente) {
        ApiRequest request = new ApiRequest("Cuentas", "cuentas", "GET", "/v1/cuentas", contexto);
        request.query("idcliente", idCliente);
        request.query("consultaalias", true);

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 204), request, response);
        CuentasOB datos = response.crear(CuentasOB.class);
        return datos.filter(x -> "VIGENTE".equals(x.descEstado), CuentasOB.class);
    }
	public static ApiResponse relacionClienteProducto(Contexto contexto, Objeto relacion) {
		ApiRequest request = new ApiRequest("relaciones", "productos", "POST", "/v1/relaciones", contexto);
		request.body(relacion);
		return request.ejecutar();
	}

    /* ========== TEST ========== */
    public static void main(String[] args) {
        Contexto contexto = contexto("HB", "desarrollo");
        CuentasOB datos = get(contexto, "5175946");
        imprimirResultado(contexto, datos);
    }

}