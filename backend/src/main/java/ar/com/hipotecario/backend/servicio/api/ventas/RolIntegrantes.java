package ar.com.hipotecario.backend.servicio.api.ventas;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;

public class RolIntegrantes extends ApiObjetos<RolIntegrante> {

	/* ========== CLASES ========== */
	public static class RolIntegrante extends ApiObjeto {
		public String Id;
		public Integer Secuencia;
		public String IdCobis;
		public String NumeroDocumentoTributario;
		public Boolean Aceptado;
		public Object Rol;
		public String TipoDocumento;
		public String NumeroDocumento;
		public String Nombres;
		public String Apellido;
		public Object NumeroTarjetaDebito;
		public BigDecimal IngresoNeto;
		public Boolean DDJJCompleta;
		public Object Tipo;
		public Object Grupo;
		public Object TarjetaCreditoAdicionales;
		public Fecha FechaNacimiento;
		public Object DdjjSalud;
		public Object idDDJJJ;
	}
}
