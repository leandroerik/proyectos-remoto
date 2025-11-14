package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
// API-Productos_PosicionConsolidadaV4
public class PosicionConsolidadV4 extends ApiObjeto {
	/* ========== ATRIBUTOS ========== */

	public String codigoPaquete;
	public String codigoProducto;
	public String codigoTitularidad;
	public String descripcionPaquete;
	public String descTitularidad;
	public String estado;
	public String fechaAlta;
	public boolean muestraPaquete;
	public String numeroProducto;
	public String tipo;
	public String cuentaAsociada;
	public int codMoneda;
	public String descProducto;
	public int detProducto;
	public String pfFechaVencimiento;
	public double importe;




	public static List<PosicionConsolidadV4> getV4(Contexto contexto, String idCobis) {
		return getV4(contexto, idCobis, true, true, true, "vigente", null);
	}
	public static List<PosicionConsolidadV4> getV4(Contexto contexto, String idCobis, Boolean adicionales, Boolean firmaconjunta, Boolean firmantes, String tipoestado, String cuit)  {
		ApiRequest request = new ApiRequest("PosicionConsolidada", "productos", "GET", "/v4/posicionconsolidada", contexto);
		request.query("idcliente", idCobis);
		request.query("adicionales", adicionales);
		request.query("firmaconjunta", firmaconjunta);
		request.query("firmantes", firmantes);
		request.query("tipoestado", tipoestado); // todos | vigente | cancelado
		request.query("cuit", cuit);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		ObjectMapper mapper = new ObjectMapper();
		List<PosicionConsolidadV4> productos=null;

		try {
			productos	= mapper.readValue(
					response.body,
					mapper.getTypeFactory().constructCollectionType(List.class, PosicionConsolidadV4.class)
			);

		}catch (Exception e){
			System.out.println(e.getMessage());
		}
		return productos;
	}
}