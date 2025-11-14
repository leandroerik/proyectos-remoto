package ar.com.hipotecario.backend.servicio.api.empresas;

import java.util.ArrayList;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class CompletaFirmaOB extends ApiObjeto {

	public String codigo;

	public String descripcion;

	public ArrayList<GrupoOB> grupos;

	public ArrayList<String> esquema;

	/* ========== SERVICIOS ========== */
	// API-CompletaFirmaOB

	public static CompletaFirmaOB get(Contexto contexto, String cedruc, String cuenta, String monto, String firmante, String firmasRegistradas, String funcionalidadOB, String moneda) {
		ApiRequest request = new ApiRequest("CompletaFirmaOB", "empresas", "GET", "/v1/esquemas/{cedruc}/completafirma", contexto);
		request.path("cedruc", cedruc);
		request.query("cuenta", cuenta);
		request.query("monto", monto);
		request.query("firmante", firmante);
		if(moneda!=null){
			request.query("moneda", moneda);
		}

		if (firmasRegistradas != null) {
			request.query("firmasRegistradas", firmasRegistradas);
		}
		request.query("funcionalidadOB", funcionalidadOB);

		ApiResponse response = request.ejecutar();

		ApiException.throwIf("FIRMANTE_NO_EXISTE", response.contains("El CUIT/CUIL DEL FIRMANTE 1 NO EXISTE"), request, response);
		ApiException.throwIf("FIRMAS_REGISTRADAS_NO_EXISTE", response.contains("El CUIT/CUIL DEL FIRMANTE"), request, response);
		ApiException.throwIf("FIRMANTE/S NO HABILITADO/S EN DICTAMEN", response.contains("FIRMANTE/S NO HABILITADO EN DICTAMEN"), request, response);
		ApiException.throwIf("TRANSACCION_NO_PERMITIDA", response.contains("TRANSACCION NO PERMITIDA"), request, response);
		ApiException.throwIf("NO_HAY_COMBINACIONES_DISPONIBLES", response.codigoHttp.equals(204), request, response);
		ApiException.throwIf("FUNCIONALIDAD_OB_NO_HABILITADA", response.codigoHttp.equals(325), request, response);
		ApiException.throwIf("PENDIENTE_BANCO", response.codigoHttp.equals(350), request, response);
		ApiException.throwIf("EXCEDE_EL_MONTO", response.codigoHttp.equals(375), request, response);
		ApiException.throwIf("LA_CUENTA_DEL_CLIENTE_NO_EXISTE", response.codigoHttp.equals(400), request, response);
		ApiException.throwIf("SIN_FIRMA", response.codigoHttp.equals(500), request, response);
		ApiException.throwIf("EXCEDE_EL_MONTO",response.contains("EXCEDE EL MONTO"), request, response);
		ApiException.throwIf(!response.http(200), request, response);

		if (response.objetos().size() > 0)
			return response.crear(CompletaFirmaOB.class, response.objetos().get(0));
		else
			return response.crear(CompletaFirmaOB.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {

		String test = "totalmenteFirmada";
		Contexto contexto = contexto("OB", "desarrollo");
		CompletaFirmaOB datos = null;

		switch (test) {
		case "totalmenteFirmada":
			datos = get(contexto, "30509300700", "300000000367653", "50000", "20203839880", "20045339492", "4", null);
			break;
		case "parcialmenteFirmada":
			datos = get(contexto, "30509300700", "300000000367653", "50000", "20203839880", "", "4",null);
			break;
		case "noHabilitadoEnDictamen":
			datos = get(contexto, "30506733932", "300000000404592", "15000000", "20107222155", "", "4", null);
			break;
		case "errorFuncional":
			datos = get(contexto, "30685231677", "300000000565583", "2000000", "23127464359", "", "4", null);
			break;
		}
		imprimirResultado(contexto, datos);
	}

}
