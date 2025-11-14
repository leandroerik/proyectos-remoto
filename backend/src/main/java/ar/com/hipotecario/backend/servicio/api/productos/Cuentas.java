package ar.com.hipotecario.backend.servicio.api.productos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.cuentas.CajasAhorrosV1.CajaAhorroV1;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;

// COBIS: SELECT TOP 100 * FROM [cob_ahorros].[dbo].[ah_cuenta]
// COBIS: SELECT TOP 100 * FROM [cob_cuentas].[dbo].[cc_ctacte]
public class Cuentas extends ApiObjetos<Cuenta> {

	/* ========== ATRIBUTOS ========== */
	public static class Cuenta extends ApiObjeto {
		public Boolean muestraPaquete;
		public String tipoProducto;
		public String numeroProducto;
		public String idProducto;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public String idDomicilio;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public Boolean adicionales;
		public String moneda;
		public String descMoneda;
		public String estadoCuenta;
		public BigDecimal disponible;
		public BigDecimal acuerdo;
		public String idPaquete;
		public String categoria;

		public String descripcion() {
			String descripcion = "";
			descripcion = "AHO".equals(tipoProducto) ? "Caja de Ahorro" : descripcion;
			descripcion = "CTE".equals(tipoProducto) ? "Cuenta Corriente" : descripcion;
			return descripcion;
		}

		public String descripcionCorta() {
			String descripcion = "";
			descripcion = "AHO".equals(tipoProducto) ? "CA" : descripcion;
			descripcion = "CTE".equals(tipoProducto) ? "CC" : descripcion;
			return descripcion;
		}

		public Boolean esCajaAhorro() {
			return "AHO".equals(tipoProducto);
		}

		public Boolean esTitular() {
			return  "T".equals(tipoTitularidad);
		}

		public Boolean esVigente() {
			return  "V".equals(estado);
		}

		public Boolean esCuentaCorriente() {
			return "CTE".equals(tipoProducto);
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
			return Modulo.simboloMoneda(moneda);
		}

		public String titularidad() {
			String titularidad = null;
			titularidad = "T".equals(tipoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(tipoTitularidad) ? "Adicional" : titularidad;
			titularidad = "C".equals(tipoTitularidad) ? "Compañia" : titularidad;
			titularidad = "P".equals(tipoTitularidad) ? "Persona" : titularidad;
			titularidad = "D".equals(tipoTitularidad) ? "Deudor Principal Prestamo" : titularidad;
			titularidad = "M".equals(tipoTitularidad) ? "Menor Autorizado" : titularidad;
			return titularidad != null ? titularidad : Texto.primeraMayuscula(descTipoTitularidad);
		}

		public String moneda() {
			return Modulo.moneda(moneda);
		}

		public Boolean esPesos() {
			return "80".equals(moneda);
		}

		public Boolean esDolares() {
			return "2".equals(moneda);
		}

		public String descripcionEstado() {
			String estado = "";
			estado = "A".equals(estadoCuenta) ? "Activa" : estado;
			estado = "C".equals(estadoCuenta) ? "Cancelada" : estado;
			estado = "I".equals(estadoCuenta) ? "Inmovilizada" : estado;
			estado = "M".equals(estadoCuenta) ? "Mantenimiento de Firmas" : estado;
			estado = "R".equals(estadoCuenta) ? "Cerrada" : estado;
			estado = "V".equals(estadoCuenta) ? "Vigente" : estado;
			return estado;
		}

		public String saldoFormateado() {
			return Modulo.importe(disponible);
		}

		public String acuerdoFormateado() {
			return Modulo.importe(acuerdo);
		}

		public Boolean mostrar() {
			Boolean mostrar = true;
			mostrar &= !(tipoTitularidad.equals("F"));
			mostrar &= !(categoria.equals("RFB"));
			mostrar &= !(categoria.equals("FCL"));
			mostrar &= !(categoria.equals("R"));
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

	public static List<Cuenta> cuentas(Contexto contexto) {
		List<Cuenta> cuentas = new ArrayList<>();
		PosicionConsolidada productos = ApiProductos.posicionConsolidada(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (Cuenta item : productos.cuentas) {
			if (item.mostrar()) {
				cuentas.add(item);
			}
		}
		return cuentas;
	}

	public Cuenta cuentaPesos() {
		for (Cuenta cuenta : this) {
			if (cuenta.esCajaAhorro() && cuenta.esPesos() && "T".equals(cuenta.tipoTitularidad)) {
				return cuenta;
			}
		}
		
		return null;
	}

	public Cuenta cuentaDolares() {
		for (Cuenta cuenta : this) {
			if (cuenta.esCajaAhorro() && cuenta.esDolares() && cuenta.tipoTitularidad.equals("T")) {
				return cuenta;
			}
		}
		
		return null;
	}
}
