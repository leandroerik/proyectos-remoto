package ar.com.hipotecario.backend.servicio.api.seguridad;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.seguridad.DesafiosTCO.DesafioTCO;

public class DesafiosTCO extends ApiObjetos<DesafioTCO> {

	/* ========== ATRIBUTOS ========== */
	public static class DesafioTCO extends ApiObjeto {
		public Integer orden;
		public Integer fila;
		public Integer columna;

		public String coordenada() {
			if (this.fila != null && this.columna != null) {
				String fila = String.valueOf(this.fila + 1);
				String[] columnas = { "A", "B", "C", "D", "E", "F", "G", "H", "I" };
				String columna = columnas[this.columna];
				return columna + fila;
			}
			return "";
		}

		public String toString() {
			return coordenada();
		}
	}

	/* ========== SERVICIOS ========== */
	// API-Seguridad_ConsultaCoordenadasIDG
	public static DesafiosTCO get(Contexto contexto, String idCliente, String grupo, Integer cantidad) {
		ApiRequest request = new ApiRequest("DesafiosTCO", "seguridad", "GET", "/v1/tarjetascoordenadas/desafio", contexto);
		request.query("cantidad", cantidad);
		request.query("grupo", grupo);
		request.query("idcliente", idCliente);

		ApiResponse response = request.ejecutar();
		ApiException.throwIf("TARJETA_INACTIVA", response.contains("NO_ACTIVE_CARD_AFTER_ACTIVATION"), request, response);
		ApiException.throwIf("USUARIO_SIN_TCO", response.contains("USER_DOES_NOT_EXIST"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("como resultado del intento fallido se bloquea"), request, response);
		ApiException.throwIf("USUARIO_BLOQUEADO", response.contains("siempre que el canal de autenticación esté Bloqueado."), request, response);

		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(DesafiosTCO.class, response);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		Contexto contexto = contexto("OB", "desarrollo");
		DesafiosTCO datos = get(contexto, "4594725", "ClientesBH", 2);
		System.out.println(datos.get(0) + " " + datos.get(1) + "\n");
		imprimirResultado(contexto, datos);
	}
}
