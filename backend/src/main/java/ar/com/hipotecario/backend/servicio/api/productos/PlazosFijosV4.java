package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.PlazosFijosV4.PlazoFijoV4;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class PlazosFijosV4 extends ApiObjetos<PlazoFijoV4> {

	/* ========== ATRIBUTOS ========== */
	public static class PlazoFijoV4 extends ApiObjeto {
		public String codigoProducto;
		public String codigoTitularidad;
		public String descripcionPaquete;
		public String descTitularidad;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public Boolean muestraPaquete;
		public String numeroProducto;
		public String tipo;
		public String cuentaAsociada;
		public String codMoneda;
		public String descProducto;
		public BigDecimal detProducto;
		public BigDecimal importe;
		public Fecha pfFechaVencimiento;
		public String estadoPF;

		public String producto() {
			return "Plazo Fijo";
		}

		public String descripcion() {
			return tipo(descProducto);
		}

		public String titularidad() {
			String titularidad = null;
			titularidad = "T".equals(codigoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(codigoTitularidad) ? "Cotitular" : titularidad;
			return titularidad != null ? titularidad : Texto.primeraMayuscula(descTitularidad);
		}

		public String descripcionMoneda() {
			return Modulo.moneda(codMoneda);
		}

		public String moneda() {
			return Modulo.simboloMoneda(codMoneda);
		}

		public String importeInicialFormateado() {
			return Modulo.importe(importe);
		}

		public String estado() {
			return estado(estadoPF);
		}

		public Boolean mostrar() {
			if ("VEN".equals(estadoPF) || "CAN".equals(estadoPF) || "ANU".contains(estadoPF)) {
				if (pfFechaVencimiento.esFuturo()) {
					return true;
				}
			}
			return false;
		}

		public static String estado(String idEstado) {
			String estado = "";
			estado = "V".equals(idEstado) ? "Vigente" : estado;
			estado = "ACT".equals(idEstado) ? "Activado" : estado;
			estado = "ING".equals(idEstado) ? "Ingresado" : estado;
			estado = "REN".equals(idEstado) ? "Renovado" : estado;
			estado = "VEN".equals(idEstado) ? "Vencido" : estado;
			estado = "CAN".equals(idEstado) ? "Cancelado" : estado;
			estado = "ANU".equals(idEstado) ? "Anulado" : estado;
			return estado;
		}

		public static String tipo(String idTipo) {
			String tipo = "";
			tipo = "0001".equals(idTipo) ? "Transferible" : tipo;
			tipo = "0002".equals(idTipo) ? "Transferible con Intereses Periódicos" : tipo;
			tipo = "0003".equals(idTipo) ? "Intransferible" : tipo;
			tipo = "0004".equals(idTipo) ? "Intransferible con Intereses Periódicos" : tipo;
			tipo = "0005".equals(idTipo) ? "Intransferible con Seguro" : tipo;
			tipo = "0006".equals(idTipo) ? "Transferible Ajustable por CER" : tipo;
			tipo = "0007".equals(idTipo) ? "Intransferible Ajustable por CER" : tipo;
			tipo = "0008".equals(idTipo) ? "Transferible Precancelable" : tipo;
			tipo = "0010".equals(idTipo) ? "Intransferible Empleado" : tipo;
			tipo = "0011".equals(idTipo) ? "Tradicional" : tipo;
			tipo = "0012".equals(idTipo) ? "Intransferible con Intereses Periódicos" : tipo;
			tipo = "0013".equals(idTipo) ? "Empleado Intransferible" : tipo;
			tipo = "0017".equals(idTipo) ? "Intransferible Ajustable por UVA" : tipo;
			tipo = "0018".equals(idTipo) ? "Ajustable por UVA" : tipo;
			tipo = "0020".equals(idTipo) ? "Procrear joven ajustable por UVA" : tipo;
			tipo = "0021".equals(idTipo) ? "Empleado Ajustable por UVA" : tipo;
			tipo = "0025".equals(idTipo) ? "Logros $" : tipo;
			tipo = "0026".equals(idTipo) ? "Logros USD" : tipo;
			tipo = "0027".equals(idTipo) ? "Logros UVA" : tipo;
			tipo = "0028".equals(idTipo) ? "LOGROS U$D CARTERA GRAL SUC" : tipo;
			tipo = "0029".equals(idTipo) ? "LOGROS $ EMPLEADOS SUCURSAL" : tipo;
			tipo = "0030".equals(idTipo) ? "LOGROS U$D EMPLEADOS SUCURSAL" : tipo;
			tipo = "0031".equals(idTipo) ? "LOGROS UVA EMP SUCURSAL" : tipo;
			tipo = "0032".equals(idTipo) ? "LOGROS UVA CARTERA GRAL SUC" : tipo;
			tipo = "0033".equals(idTipo) ? "Logros $" : tipo;
			tipo = "0034".equals(idTipo) ? "Logros USD" : tipo;
			tipo = "0035".equals(idTipo) ? "Logros UVA" : tipo;
			tipo = "0040".equals(idTipo) ? "Plazo Fijo WEB" : tipo;
			tipo = "0041".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo; // Sucursal
			tipo = "0042".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo;
			tipo = "0043".equals(idTipo) ? "Empleado Ajustable por UVA Precancelable $" : tipo;
			tipo = "0044".equals(idTipo) ? "Ajustable por UVA Precancelable $" : tipo; // WEB
			return tipo;
		}

		public Boolean esCedip() {
			return "1000".equals(tipo);
		}
	}

	public static List<PlazoFijoV4> plazosFijos(Contexto contexto) {
		List<PlazoFijoV4> plazosFijos = new ArrayList<>();
		PosicionConsolidadaV4 productos = ApiProductos.posicionConsolidadaV4(contexto, contexto.sesion().idCobis, true, false, false).get();
		for (PlazoFijoV4 item : productos.plazosFijos) {
			if (item.mostrar()) {
				plazosFijos.add(item);
			}
		}
		return plazosFijos;
	}
}