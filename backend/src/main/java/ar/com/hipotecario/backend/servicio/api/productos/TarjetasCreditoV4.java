package ar.com.hipotecario.backend.servicio.api.productos;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;

import ar.com.hipotecario.backend.base.Texto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.productos.TarjetasCreditoV4.TarjetaCreditoV4;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.Tarjetas;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.TarjetasCredito;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.lib.Encriptador;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// SQL14: SELECT TOP 100 * FROM [smartopen].[dbo].[VisaTarje]
public class TarjetasCreditoV4 extends ApiObjetos<TarjetaCreditoV4> {

	/* ========== ATRIBUTOS ========== */
	public static class TarjetaCreditoV4 extends ApiObjeto {
		public Boolean esPagableUS;
		public String altaPuntoVenta;
		public Long idPaquete;
		public Boolean muestraPaquete;
		public Integer sucursal;
		public String descSucursal;
		public String estado;
		public String descEstado;
		public String fechaAlta;
		public String tipoTitularidad;
		public String descTipoTitularidad;
		public String numero;
		public String cuenta;
		public String tipoTarjeta;
		public String descTipoTarjeta;
		public String fechaVencActual;
		public String cierreActual;
		public double debitosEnCursoPesos;
		public double debitosEnCursoDolares;
		public String formaPago;
		public String denominacionTarjeta;
		public String modeloLiquidacion;
		public String descModeloLiquidacion;
		public String fechaProximoCierre;

		public Boolean esTitular() {
			return "T".equals(tipoTitularidad) || "P".equals(tipoTitularidad);
		}

		public String descripcion() {
			return "Tarjeta de Crédito";
		}

		public String idEncriptado() {
			return "true".equals(ConfigHB.string("prendido_encriptacion_tc")) ?
					Encriptador.encriptarPBEBH(numero)
					: numero;
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

		public String debitosPesosFormateado() {
			return Formateador.importe(BigDecimal.valueOf(debitosEnCursoPesos));
		}

		public String debitosDolaresFormateado() {
			return Formateador.importe(BigDecimal.valueOf(debitosEnCursoDolares));
		}

		public String stopDebit(ContextoHB contexto, String numero) {
			ar.com.hipotecario.canal.homebanking.conector.ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero);
			return response.objetos().get(0).string("stopDebit");
		}

		public boolean adheridoResumenElectronico(ContextoHB contexto, String numero) {
			ar.com.hipotecario.canal.homebanking.conector.ApiResponse response = TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, numero);
			return response.objetos().get(0).bool("adheridoResumenElectronico");
		}
	}

	public static List<TarjetaCreditoV4> tarjetasCreditoTitularConAdicionalesTercero(Contexto contexto) {
		TarjetaCreditoV4 tarjetaCreditoTitular = tarjetaCreditoTitular(contexto);
		List<TarjetaCreditoV4> lista = new ArrayList<>();
		for (TarjetaCreditoV4 tarjetaCredito : tarjetasCredito(contexto)) {
			if (tarjetaCredito.esTitular()) {
				lista.add(tarjetaCredito);
			}
		}
		for (TarjetaCreditoV4 tarjetaCredito : tarjetasCredito(contexto)) {
			if (!tarjetaCredito.esTitular() && (tarjetaCreditoTitular == null || !tarjetaCreditoTitular.cuenta.equals(tarjetaCredito.cuenta))) {
				lista.add(tarjetaCredito);
			}
		}
		return lista;
	}

	public static TarjetaCreditoV4 tarjetaCreditoTitular(Contexto contexto) {
		TarjetaCreditoV4 tarjetaCreditoTitular = null;
		for (TarjetaCreditoV4 tarjetaCredito : tarjetasCredito(contexto)) {
			if (tarjetaCredito.esTitular()) {
				tarjetaCreditoTitular = tarjetaCredito;
			}
		}
		return tarjetaCreditoTitular;
	}

	public static List<TarjetaCreditoV4> tarjetasCredito(Contexto contexto) {
		List<TarjetaCreditoV4> lista = new ArrayList<>();
		PosicionConsolidadaV4 productos = PosicionConsolidadaV4.get(contexto, contexto.sesion().idCobis);
		for (TarjetaCreditoV4 item : productos.tarjetasCredito) {
			lista.add(item);
		}
		List<TarjetaCreditoV4> tarjetasCredito = new ArrayList<>();
		for (TarjetaCreditoV4 tarjetaCredito : lista) {
			if (tarjetaCredito.esTitular()) {
				tarjetasCredito.add(tarjetaCredito);
			}
		}
		for (TarjetaCreditoV4 tarjetaCredito : lista) {
			if (!tarjetaCredito.esTitular()) {
				tarjetasCredito.add(tarjetaCredito);
			}
		}
		return tarjetasCredito;
	}

	public static List<TarjetaCreditoV4> getTarjetas(ContextoOB contexto, String idCobis, Boolean adicionales) {
		ApiRequest request = new ApiRequest("ListadoTarjetas", "tarjetascredito", "GET", "/v2/tarjetascredito", contexto);

		request.query("adicionales", adicionales);
		request.query("cancelados", false);
		request.query("idcliente", idCobis);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);

		if(response.http(204)) {
			throw new RuntimeException("SIN_CUENTAS");
		}

		try {
			ObjectMapper objectMapper = new ObjectMapper();
			List<TarjetaCreditoV4> tarjetas = objectMapper.readValue(response.body, new TypeReference<List<TarjetaCreditoV4>>() {});
			if(!tarjetas.isEmpty()) {
				TarjetasCredito.obtenerDatosClienteSO(contexto, tarjetas.get(0).cuenta);
			}
			return tarjetas;
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Error deserializando la respuesta a una lista de Tarjetas", e);
		}
	}
}