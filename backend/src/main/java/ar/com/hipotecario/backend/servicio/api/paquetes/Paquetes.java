package ar.com.hipotecario.backend.servicio.api.paquetes;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.paquetes.Costos.Costo;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Paquete;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.TarjetasEsales.TarjetaEsales;

public class Paquetes extends ApiObjetos<Paquete> {

	/* ========== CONSTANTES ========== */
	public static String PAQ_LETRA_BUHO_PACK_WHITE = "I";
	public static String PAQ_LETRA_GOLD = "P";
	public static String PAQ_LETRA_PLAT = "L";
	public static String PAQ_LETRA_BLACK = "S";

	public static String SUB_CARACT_BUHO_PUNTOS = "02";
	public static String SUB_CARACT_AEROLINEAS = "05";

	public static String FACTOR_LIMITE_BLACK = "1.5";
	public static String FACTOR_LIMITE_PLAT = "1.5";
	public static String FACTOR_LIMITE_GOLD = "1.0";
	public static String FACTOR_LIMITE_WHITE = "1.0";

	/* ========== ATRIBUTOS ========== */
	public static class Paquete extends ApiObjeto {
		public Integer id;
		public String codigo;
		public String descripcion;
		public Object tipo;
		public Estado estado;
		public String ciclo;
		public String resumen;
		public String pagoTarjeta;
		public String empleado;
		public Cliente cliente;
		public String programaRecompensa;
		public String programaRecompensaCanal;
		public List<Producto> productos;

		/* ========== METODOS ========== */
		public Producto tarjetaCredito() {
			for (Producto producto : productos) {
				if (producto.categoria.contains("TARJETA DE CREDITO") && !producto.descripcion.contains(" NACIONAL")) {
					return producto;
				}
			}
			return null;
		}

		public String getInicialTarjeta() {
			Producto tarjetaCredito = tarjetaCredito();
			if (tarjetaCredito != null) {
				return tarjetaCredito.descripcion.replaceAll("VISA ", "").substring(0, 1);
			}

			return null;
		}

		public BigDecimal calcularLimiteCuotas(String letraTC, BigDecimal limite) {
			BigDecimal factor = new BigDecimal(1);
			if (PAQ_LETRA_BLACK.equals(letraTC)) {
				factor = new BigDecimal(FACTOR_LIMITE_BLACK);
			} else if (PAQ_LETRA_PLAT.equals(letraTC)) {
				factor = new BigDecimal(FACTOR_LIMITE_PLAT);
			} else if (PAQ_LETRA_GOLD.equals(letraTC)) {
				factor = new BigDecimal(FACTOR_LIMITE_GOLD);
			} else if (PAQ_LETRA_BUHO_PACK_WHITE.equals(letraTC)) {
				factor = new BigDecimal(FACTOR_LIMITE_WHITE);
			}

			return limite.multiply(factor);
		}

		public SubProducto buscar(String caracteristica) {
			SubProducto dato = null;
			for (SubProducto sub : subProductosDisponibles()) {
				if (sub.caracteristica.equals(caracteristica)) {
					dato = sub;
					break;
				}
			}
			return dato;
		}

		public static SubProducto subProductoAerolineas() {
			SubProducto aero = new SubProducto();
			aero.id = "1";
			aero.nombre = "AEROLINEAS PLUS";
			aero.caracteristica = SUB_CARACT_AEROLINEAS;
			return aero;
		}

		public static SubProducto subProductoBuho() {
			SubProducto buho = new SubProducto();
			buho.id = "2";
			buho.nombre = "BUHO PUNTOS";
			buho.caracteristica = SUB_CARACT_BUHO_PUNTOS;
			return buho;
		}

		public List<SubProducto> subProductosDisponibles() {
			List<SubProducto> subProductos = new ArrayList<>();
			SubProducto buho = subProductoBuho();
			SubProducto aero = subProductoAerolineas();

			if (programaRecompensa.contains("1")) {
				subProductos.add(aero);
			}

			if (programaRecompensa.contains("2")) {
				subProductos.add(buho);
			}

			return subProductos;
		}

		/* ========== SERVICIOS ========== */
		public TarjetaCredito tarjetaCreditoFull(Contexto contexto, String letraTC, BigDecimal limite, String codigoPaquete) {
			Producto tc = tarjetaCredito();
			if (tc == null)
				return null;

			String inicialTarjeta = getInicialTarjeta();
			TarjetaEsales tarjetaEsales = SqlEsales.tarjetaPorInicial(contexto, inicialTarjeta).tryGet();

			if (tarjetaEsales != null && tarjetaEsales.estaActiva()) {
				TarjetaCredito tcResponse = new TarjetaCredito();
				tcResponse.nombre = "Visa " + tarjetaEsales.nombre;
				tcResponse.descripcion = tarjetaEsales.descripcion;
				tcResponse.letraTC = letraTC;
				tcResponse.legales = tarjetaEsales.legales;
				tcResponse.detalle = tarjetaEsales.detalle;
				tcResponse.limiteCompra = limite;
				tcResponse.limiteCompraCuotas = calcularLimiteCuotas(letraTC, limite);
				tcResponse.limiteCompraTotal = limite;

				Costos costos = ApiPaquetes.costos(contexto, "0").tryGet();
				if (costos == null) {
					tcResponse.costo = new BigDecimal(0);

				} else {

					Costo costo = costos.buscar(codigoPaquete);
					if (costo != null) {
						tcResponse.costo = costo.sinIVA;
					}
				}

				return tcResponse;
			}

			return null;
		}
	}

	/* ========== CLASES ========== */
	public class Cliente extends ApiObjeto {
		public String rol;
	}

	public class Estado extends ApiObjeto {
		public String codigo;
		public String descripcion;
	}

	public static class SubProducto extends ApiObjeto {
		public String id;
		public String nombre;
		public String caracteristica;
	}

	public static class Moneda extends ApiObjeto {
		public static String DOLARES = "2";
		public static String PESOS = "80";

		public String id;
		public String descripcion;
	}

	public static class Producto extends ApiObjeto {
		public static String CAJA_AHORRO = "AHO";
		public static String TARJETA_DE_DEBITO = "ATM";
		public static String TARJETA_DE_CREDITO = "SMA";

		public Integer id;
		public String categoria;
		public String codigo;
		public String descripcion;
		public String tipo;
		public Moneda moneda;
		public Boolean cuentaCobro;
		public String condicionante;
		public String categoriaDefault;
		public String opcional;

		/* ========== METODOS ========== */
		public String moneda() {
			if (moneda != null)
				return moneda.descripcion;
			return null;
		}

		public Boolean esPesos() {
			if (empty(moneda))
				return false;
			return moneda.id.equals(Moneda.PESOS);
		}

		public Boolean esDolar() {
			if (empty(moneda))
				return false;
			return moneda.id.equals(Moneda.DOLARES);
		}

		public Boolean esTarjetaCredito() {
			if (empty(tipo))
				return false;
			Boolean esDelTipo = tipo.equals(TARJETA_DE_CREDITO);
			esDelTipo &= !empty(categoriaDefault);
			return esDelTipo;
		}

		public Boolean esTarjetaDebito() {
			if (empty(tipo))
				return false;
			Boolean esDelTipo = tipo.equals(TARJETA_DE_DEBITO);
			return esDelTipo;
		}

		public Boolean esCajaAhorro() {
			if (empty(tipo))
				return false;
			Boolean esDelTipo = tipo.equals(CAJA_AHORRO);
			esDelTipo &= esPesos();
			return esDelTipo;
		}

		public Boolean esCajaAhorroDolar() {
			if (empty(tipo))
				return false;
			Boolean esDelTipo = tipo.equals(CAJA_AHORRO);
			esDelTipo &= esDolar();
			return esDelTipo;
		}
	}

	public static class TarjetaCredito extends ApiObjeto {
		public String nombre;
		public String descripcion;
		public String legales;
		public String detalle;
		public String letraTC;
		public BigDecimal limiteCompra;
		public BigDecimal limiteCompraCuotas;
		public BigDecimal limiteCompraTotal;
		public BigDecimal costo;
	}

	/* ========== SERVICIOS ========== */
	// API-PaquetesCore_PaquetesGRD_ws-parametria-paquete
	public static Paquetes get(Contexto contexto, String letraTarjeta, String numeroPaquete, Boolean empleado) {
		ApiRequest request = new ApiRequest("Paquetes", "paquetes", "GET", "/v1/infoParametrias/paquetes", contexto);
		if (!letraTarjeta.isEmpty())
			request.query("letraTarjeta", letraTarjeta);
		if (!numeroPaquete.equals("-1"))
			request.query("numeroPaquete", numeroPaquete);
		request.query("empleado", empleado);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(Paquetes.class);
	}

	// Obtener paquete por numeroPaquete
	public static Paquete getPorNumero(Contexto contexto, String numeroPaquete, Boolean empleado) {
		Paquetes paquetes = get(contexto, "", numeroPaquete, empleado);
		if (paquetes != null && !paquetes.isEmpty()) {
			return paquetes.get(0);
		}
		return null;
	}

	// Obtener paquete por letraPaquete
	public static Paquetes getPorLetra(Contexto contexto, String letraTarjeta, Boolean empleado) {
		return get(contexto, letraTarjeta, "-1", empleado);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("BB", "homologacion");
		Boolean porLetra = false;

		if (porLetra) {
			Paquetes datos = getPorLetra(contexto, "S", false);
			imprimirResultado(contexto, datos);
			return;
		}

		Paquete datos = getPorNumero(contexto, "69", false);
		imprimirResultado(contexto, datos);

		System.out.println("Inicial tarjeta: " + datos.getInicialTarjeta());
	}
}