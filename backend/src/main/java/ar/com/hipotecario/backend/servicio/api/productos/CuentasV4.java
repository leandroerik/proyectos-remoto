package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasV4.CuentaV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CuentasV4 extends ApiObjetos<CuentaV4> {

	/* ========== ATRIBUTOS ========== */
	public static class CuentaV4 extends ApiObjeto {
		public String codigoPaquete;
		public String codigoProducto;
		public String codigoTitularidad;
		public String descripcionPaquete;
		public String descTitularidad;
		public String estado;
		public String fechaAlta;
		public Boolean muestraPaquete;
		public String numeroProducto;
		public String tipo;
		public String cuentaAsociada;
		public Long codMoneda;
		public String descProducto;
		public Long detProducto;
		public BigDecimal importe;
		public Fecha pfFechaVencimiento;

		public String descripcion() {
			String descripcion = "";
			descripcion = "AHO".equals(tipo) ? "Caja de Ahorro" : descripcion;
			descripcion = "CTE".equals(tipo) ? "Cuenta Corriente" : descripcion;
			return descripcion;
		}

		public String descripcionCorta() {
			String descripcion = "";
			descripcion = "AHO".equals(tipo) ? "CA" : descripcion;
			descripcion = "CTE".equals(tipo) ? "CC" : descripcion;
			return descripcion;
		}

		public Boolean esCajaAhorro() {
			return "AHO".equals(tipo);
		}

		public Boolean esCuentaCorriente() {
			return "CTE".equals(tipo);
		}

		public String numeroFormateado() {
			String numeroFormateado = "";
			String numero = numeroProducto;
			if (numero != null && numero.length() == 15) {
				numeroFormateado += numero.substring(0, 1) + "-";
				numeroFormateado += numero.substring(1, 4) + "-";
				numeroFormateado += numero.substring(4, 14) + "-";
				numeroFormateado += numero.substring(14, 15);
			}
			return numeroFormateado;
		}

		public String numeroEnmascarado() {
			String numeroFormateado = "";
			String numero = numeroProducto;
			if (numero != null && numero.length() == 15) {
				numeroFormateado += "XXX-";
				numeroFormateado += numero.substring(11, 15);
			} else if (numero != null && numero.length() > 4) {
				numeroFormateado += "XXX-";
				numeroFormateado += numero.substring(numero.length() - 4);
			}
			return numeroFormateado;
		}

		public String simboloMoneda() {
			return Modulo.simboloMoneda(String.valueOf(codMoneda));
		}

		public String titularidad() {
			String titularidad = null;
			titularidad = "T".equals(codigoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(codigoTitularidad) ? "Adicional" : titularidad;
			titularidad = "C".equals(codigoTitularidad) ? "Compañia" : titularidad;
			titularidad = "P".equals(codigoTitularidad) ? "Persona" : titularidad;
			titularidad = "D".equals(codigoTitularidad) ? "Deudor Principal Prestamo" : titularidad;
			titularidad = "M".equals(codigoTitularidad) ? "Menor Autorizado" : titularidad;
			return titularidad != null ? titularidad : Texto.primeraMayuscula(descTitularidad);
		}

		public String moneda() {
			return Modulo.moneda(String.valueOf(codMoneda));
		}

		public Boolean esPesos() {
			return "80".equals(codMoneda);
		}

		public Boolean esDolares() {
			return "2".equals(codMoneda);
		}

		public String descripcionEstado() {
			String estadofinalizado = "";
			estadofinalizado = "A".equals(estado) ? "Activa" : estadofinalizado;
			estadofinalizado = "C".equals(estado) ? "Cancelada" : estadofinalizado;
			estadofinalizado = "I".equals(estado) ? "Inmovilizada" : estadofinalizado;
			estadofinalizado = "M".equals(estado) ? "Mantenimiento de Firmas" : estadofinalizado;
			estadofinalizado = "R".equals(estado) ? "Cerrada" : estadofinalizado;
			estadofinalizado = "V".equals(estado) ? "Vigente" : estadofinalizado;
			return estadofinalizado;
		}

		public String saldoFormateado() {
			return Modulo.importe(importe);
		}

		public Boolean mostrar() {
			boolean mostrar = true;
			mostrar = !(codigoTitularidad.equals("F"));
			return mostrar;
		}

		public String cbu(Contexto contexto) {
			CajaAhorroV1 cajaAhorro = ApiCuentas.cajaAhorroV1(contexto, numeroProducto, Fecha.ahora().restarDias(20)).tryGet();
			return cajaAhorro != null ? cajaAhorro.cbu : "";
		}

		public String alias(Contexto contexto, String cbu) {
			CuentaCoelsa cuentaCoelsa = ApiCuentas.cuentaCoelsa(contexto, cbu).tryGet();
			return cuentaCoelsa != null ? cuentaCoelsa.nuevoAlias : "";
		}
	}

	public static List<CuentaV4> cuentas(Contexto contexto) {
		List<CuentaV4> cuentas = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (CuentaV4 item : productos.cuentas) {
			if (item.mostrar()) {
				cuentas.add(item);
			}
		}
		return cuentas;
	}

	public CuentaV4 cuentaPesos() {
		for (CuentaV4 cuenta : this) {
			if (cuenta.esCajaAhorro() && cuenta.esPesos() && "T".equals(cuenta.codigoTitularidad)) {
				return cuenta;
			}
		}

		return null;
	}

	public CuentaV4 cuentaDolares() {
		for (CuentaV4 cuenta : this) {
			if (cuenta.esCajaAhorro() && cuenta.esDolares() && cuenta.codigoTitularidad.equals("T")) {
				return cuenta;
			}
		}

		return null;
	}
}