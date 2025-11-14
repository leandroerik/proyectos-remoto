package ar.com.hipotecario.backend.servicio.api.ventas;

import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.ventas.IntegrantesScoring.IntegranteScoring;

public class IntegrantesScoring extends ApiObjetos<IntegranteScoring> {

	/* ========== CLASES ========== */
	public static class IntegranteScoring extends ApiObjeto {
		public Object Documentaciones;
		public Integer Secuencia;
		public String NumeroTributario;
		public Boolean Preaprobado;
		public Boolean IndOk;
		public Object RiesgoNetHTML;
		public Boolean SolicitarIngresosPreaprobado;
		public Object DdjjAceptada;
		public String Explicacion;
		public Object VerazHTML;
		public Object NosisHTML;
		public List<Object> IngresosInferidos;
		public Object Advertencias;
		public String Id;
	}
}
