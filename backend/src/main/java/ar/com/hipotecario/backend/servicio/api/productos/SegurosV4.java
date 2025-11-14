package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.SegurosV4.SeguroV4;
import ar.com.hipotecario.backend.servicio.api.seguro.ApiSeguro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

public class SegurosV4 extends ApiObjetos<SeguroV4> {

	/* ========== ATRIBUTOS ========== */
	public static class SeguroV4 extends ApiObjeto {
		public String producto;
		public String ramo;
		public String numeroPoliza;
		public String fechaDesde;
		public String fechaHasta;
		public String montoPrima;
		public String medioPago;
		public String origen;
		public String numeroCuenta;
		public String fechaVenta;
	}

	public static List<SeguroV4> getSeguros(ContextoOB contexto, String cuit) {
		ApiRequest request = new ApiRequest("ListadoSeguros", ApiSeguro.API, "GET", "/v1/{cuit}/productos", contexto);

		request.path("cuit", cuit);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		if(response.http(204)) {
			throw new RuntimeException("SIN_SEGUROS");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			List<SeguroV4> listSeguro = objectMapper.readValue(response.body, new TypeReference<List<SeguroV4>>() {});
			return listSeguro;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error deserializando la respuesta a una lista de Tarjetas", e);
		}
	}
}