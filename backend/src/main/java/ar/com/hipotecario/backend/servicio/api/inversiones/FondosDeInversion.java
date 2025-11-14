package ar.com.hipotecario.backend.servicio.api.inversiones;

import java.math.BigDecimal;
import java.math.BigInteger;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.inversiones.FondosDeInversion.FondoInversion;

public class FondosDeInversion extends ApiObjetos<FondoInversion> {

	public static class FondoInversion extends ApiObjeto {
		public Integer FondoID;
		public Integer FondoNumero;
		public String FondoNombreAbr;
		public String FondoNombre;
		public String TipoVCPID;
		public String TipoVCPDescripcion;
		public String TipoVCPAbreviatura;
		public Integer MonedaID;
		public String MonedaSimbolo;
		public String MonedaDescripcion;
		public String HoraInicio;
		public String HoraCierre;
		public Integer PlazoLiquidacionFondo;
		public BigDecimal PrecisionFondo;
		public Integer AplicaPersonaFisica;
		public String CondicionIngresoEgresoID;
		public String CondicionIngresoEgresoDesc;
		public Long NumeroCuentaFondo;
		public BigInteger CBUFondo;
		public String DebitaInmediato;
		public String TpRiesgoDescripcion;
		public Integer TpRiesgoNivelRiesgo;
		public Integer TpRiesgoCodInterfaz;
		public Integer EstaInhibido;

		public String getFondoNombre() {
			int indice = FondoNombre.indexOf("-");
			if (indice > 0) {
				return FondoNombre.substring(indice + 2);
			}
			return FondoNombre;
		}

		public Integer getTpRiesgoNivelRiesgo() {
			return TpRiesgoNivelRiesgo == null ? 1000 : TpRiesgoNivelRiesgo;
		}

		public Integer getIndex() {
			try {
				return Integer.parseInt(FondoNombre.substring(0, FondoNombre.indexOf("-")).trim());
			} catch (Exception e) {
				return Integer.MAX_VALUE;
			}

		}
	}

	/* ========== SERVICIOS ========== */
	// API-Inversiones_GetFondosParaOperarESCO
	public static FondosDeInversion post(Contexto contexto, Integer idCuotapartista, String tipoSolicitud) {
		ApiRequest request = new ApiRequest("FondosDeInversion", "inversiones", "POST", "/v1/fondos", contexto);
		request.body("pFondosParaOperar.idcuotapartista", idCuotapartista);
		request.body("pFondosParaOperar.tipoSolicitud", tipoSolicitud);
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(FondosDeInversion.class, response.objeto("Table").objetos());
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "desarrollo");
		FondosDeInversion datos = post(contexto, 8106, "SU");
		imprimirResultado(contexto, datos);
	}

}