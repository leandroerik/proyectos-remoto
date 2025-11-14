package ar.com.hipotecario.backend.servicio.api.productos;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasCredito.TarjetaCredito;

// SQL14: SELECT TOP 100 * FROM [smartopen].[dbo].[VisaTarje]
public class TarjetasCredito extends ApiObjetos<TarjetaCredito> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCredito extends ApiObjeto {
		public Boolean muestraPaquete;
		public String sucursal;
		public String descSucursal;
		public String descEstado;
		public String estado;
		public Fecha fechaAlta = Fecha.nunca();
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String tipoTarjeta;
		public String descTipoTarjeta;
		public String numero;
		public String cuenta;
		public Fecha fechaVencActual;
		public BigDecimal debitosEnCursoPesos;
		public BigDecimal debitosEnCursoDolares;
		public Fecha cierreActual;
		public String formaPago;
		public String denominacionTarjeta;
		public String idPaquete;

		public Boolean esTitular() {
			return "T".equals(tipoTitularidad) || "P".equals(tipoTitularidad);
		}

		public String descripcion() {
			return "Tarjeta de Crédito";
		}

		public String tipo() {
			String tipo = null;
			tipo = "I".equals(tipoTarjeta) ? "Visa Internacional" : tipo;
			tipo = "N".equals(tipoTarjeta) ? "Visa Nacional" : tipo;
			tipo = "M".equals(tipoTarjeta) ? "Mastercard" : tipo;
			tipo = "B".equals(tipoTarjeta) ? "Visa Business" : tipo;
			tipo = "O".equals(tipoTarjeta) ? "Visa Corporate" : tipo;
			tipo = "P".equals(tipoTarjeta) ? "Visa Gold" : tipo;
			tipo = "R".equals(tipoTarjeta) ? "Visa Purchasing" : tipo;
			tipo = "L".equals(tipoTarjeta) ? "Platinum" : tipo;
			tipo = "S".equals(tipoTarjeta) ? "Signature" : tipo;
			return tipo != null ? tipo : Texto.primeraMayuscula(descTipoTitularidad);
		}

		public String ultimos4digitos() {
			return Modulo.ultimos4digitos(numero);
		}

		public String numeroEnmascarado() {
			String numeroFormateado = "";
			String numeroAux = Modulo.ultimos4digitos(numero);
			if (numeroAux != null && numeroAux.length() == 4) {
				numeroFormateado += "XXX-";
				numeroFormateado += numeroAux;
			}
			return numeroFormateado;
		}

		public String estado() {
			String estadoAux = "";
			estadoAux = "20".equals(estado) ? "Tarjeta Normal" : estadoAux;
			estadoAux = "22".equals(estado) ? "Tarjeta con No Renovar" : estadoAux;
			estadoAux = "23".equals(estado) ? "Tarjeta Internacional por Viaje" : estadoAux;
			estadoAux = "24".equals(estado) ? "Tarjeta con Orden de Baja" : estadoAux;
			estadoAux = "25".equals(estado) ? "Tarjeta con Problemas" : estadoAux;
			estadoAux = "29".equals(estado) ? "Tarjeta Dada de Baja" : estadoAux;
			return estadoAux;
		}

		public String titularidad() {
			String titularidad = "";
			titularidad = "P".equals(tipoTitularidad) ? "Principal" : titularidad;
			titularidad = "T".equals(tipoTitularidad) ? "Titular" : titularidad;
			titularidad = "A".equals(tipoTitularidad) ? "Adicional" : titularidad;
			return titularidad;
		}

		public String debitosPesosFormateado() {
			return Modulo.importe(debitosEnCursoPesos);
		}

		public String debitosDolaresFormateado() {
			return Modulo.importe(debitosEnCursoDolares);
		}

		public String formaPago() {
			String formaPagoAux = "";
			Map<String, String> mapa = formasPago();
			for (String clave : mapa.keySet()) {
				if (clave.equals(formaPago)) {
					formaPagoAux = mapa.get(clave);
				}
			}
			return formaPagoAux;
		}

		public static Map<String, String> formasPago() {
			Map<String, String> mapa = new LinkedHashMap<>();
			mapa.put("01", "Efectivo");
			mapa.put("02", "Débito Automático Pago Mínimo");
			mapa.put("03", "Débito Automático Pago Total");
			mapa.put("04", "Débito Automático Pago Mínimo");
			mapa.put("05", "Débito Automático Pago Total");
			mapa.put("07", "Débito Saldo Pesos en CA");
			mapa.put("15", "Débito Saldo Actual en Cuenta");
			mapa.put("45", "Débito Pago Mínimo en Cuenta");
			mapa.put("46", "Débito Saldo Actual en Cuenta");
			mapa.put("72", "Débito Importe Acordado en CC");
			mapa.put("92", "Débito Importe Acordado en CA");
			return mapa;
		}

		public Boolean esHML() {
			return numero.startsWith("400103") || numero.startsWith("400104");
		}
	}

	public static List<TarjetaCredito> tarjetasCreditoTitularConAdicionalesTercero(Contexto contexto) {
		TarjetaCredito tarjetaCreditoTitular = tarjetaCreditoTitular(contexto);
		List<TarjetaCredito> lista = new ArrayList<>();
		for (TarjetaCredito tarjetaCredito : tarjetasCredito(contexto)) {
			if (tarjetaCredito.esTitular()) {
				lista.add(tarjetaCredito);
			}
		}
		for (TarjetaCredito tarjetaCredito : tarjetasCredito(contexto)) {
			if (!tarjetaCredito.esTitular() && (tarjetaCreditoTitular == null || !tarjetaCreditoTitular.cuenta.equals(tarjetaCredito.cuenta))) {
				lista.add(tarjetaCredito);
			}
		}
		return lista;
	}

	public static TarjetaCredito tarjetaCreditoTitular(Contexto contexto) {
		TarjetaCredito tarjetaCreditoTitular = null;
		for (TarjetaCredito tarjetaCredito : tarjetasCredito(contexto)) {
			if (tarjetaCredito.esTitular()) {
				tarjetaCreditoTitular = tarjetaCredito;
			}
		}
		return tarjetaCreditoTitular;
	}

	public static List<TarjetaCredito> tarjetasCredito(Contexto contexto) {
		List<TarjetaCredito> lista = new ArrayList<>();
		PosicionConsolidada productos = PosicionConsolidada.get(contexto, contexto.sesion().idCobis);
		for (TarjetaCredito item : productos.tarjetasCredito) {
			lista.add(item);
		}
		List<TarjetaCredito> tarjetasCredito = new ArrayList<>();
		for (TarjetaCredito tarjetaCredito : lista) {
			if (tarjetaCredito.esTitular()) {
				tarjetasCredito.add(tarjetaCredito);
			}
		}
		for (TarjetaCredito tarjetaCredito : lista) {
			if (!tarjetaCredito.esTitular()) {
				tarjetasCredito.add(tarjetaCredito);
			}
		}
		return tarjetasCredito;
	}
}
