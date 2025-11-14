package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.Cuotapartistas.Cuotapartista;

public class Cuotapartistas extends ApiObjetos<Cuotapartista> {

	/* ========== ATRIBUTOS ========== */
	public static class Cuotapartista extends ApiObjeto {
		public boolean EstaAnulado;
		public String IDCuotapartista;
		public CuentasBancarias CuentasBancarias;
	}

	public static class CuentasBancarias extends ApiObjeto {
		public List<CuotapartistaCuentaBancariaModel> CuotapartistaCuentaBancariaModel;
	}

	public static class CuotapartistaCuentaBancariaModel extends ApiObjeto {
		public String CBU;
		public String Descripcion;
		public String IDCuentaBancaria;
		public String NumeroCuenta;
		public Moneda Moneda;
	}

	public static class Moneda extends ApiObjeto {
		public Integer COD;
		public String Description;
		public String ID;
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_SelecCuotapartistaByDoc
	public static Cuotapartistas post(Contexto contexto, String idTpDocIdentidad, String numDocIdentidad, String email, String idUsuario, Boolean mostrarCuentasAnuladas, String nombre) {
		ApiRequest request = new ApiRequest("CuentasCuotapartistas", "inversiones", "POST", "/v1/cuotapartista", contexto);
		request.body("pCuotapartista.email", email);
		request.body("pCuotapartista.idTpDocIdentidad", idTpDocIdentidad);
		request.body("pCuotapartista.idUsuario", idUsuario);
		request.body("pCuotapartista.mostrarCuentasAnuladas", mostrarCuentasAnuladas);
		request.body("pCuotapartista.numDocIdentidad", numDocIdentidad);
		request.body("nombre", nombre);
		request.cache = false;
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Cuotapartistas.class, response.objeto("CuotapartistaModel").objetos());

	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Cuotapartistas datos = post(contexto, "59", "30715363638", "", "", false, "");
		imprimirResultado(contexto, datos);
	}

}
