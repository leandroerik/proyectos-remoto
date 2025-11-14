package ar.com.hipotecario.mobile.api;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.*;
import ar.com.hipotecario.mobile.lib.*;
import ar.com.hipotecario.mobile.negocio.*;
import ar.com.hipotecario.mobile.servicio.*;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public class MBProducto {

	/* Mensajes para mostrar en la UI */
	private final static Map<String, String> mensajesValidacionBajas = new HashMap<String, String>();
	public static final String CODIGO_PRODUCTO_PRESTAMO = "7";
	public static final String[] TIPOS_MORA_TEMPRANA = { "ONE", "MT" };
	public static final String CUOTA_VENCIDA = "Vencida";
	public static final String FORMA_PAGO_EFECTIVO = "Efectivo";
	public static final String[] BUCKETS = { "B2", "B3" };
	private static final String SP_EXEC_CANJEAR_PROPUESTA = "[Mobile].[dbo].[sp_CanjearPropuesta]";
	private static final String SP_EXEC_CONSULTA_PROPUESTA = "[Mobile].[dbo].[sp_ConsultarPropuestas]";
	private final static String SP_EXEC_CANJEAR_CASHBACK = "[Mobile].[dbo].[sp_CanjearCashback]";
	private final static String SP_EXEC_CONSULTA_CASHBACK_CANJEADO = "[Mobile].[dbo].[sp_ConsultarCashback]";
	private final static String SP_EXEC_OBTENER_CASHBACK_IDCANJE = "[Mobile].[dbo].[sp_ObtenerSiguienteIdCanje]";


	static {
		mensajesValidacionBajas.put("SALDO_DISTINTO_DE_CERO", "El saldo de tu cuenta debe estar en 0");
		mensajesValidacionBajas.put("POSEE_DEBITOS_ADHERIDOS", "No debes tener débitos automáticos adheridos");
		mensajesValidacionBajas.put("COTITULAR", "La cuenta no debe estar compartida");
		mensajesValidacionBajas.put("CUENTA_COMITENTE_ASOCIADA", "La cuenta no debe estar asociada a una cuenta comitente o cuotaparte");
		mensajesValidacionBajas.put("TIENE_BLOQUEOS", "La cuenta no debe tener bloqueos");
	}

	public static RespuestaMB productos(ContextoMB contexto) {
		Boolean primeraLlamada = contexto.parametros.bool("primeraLlamada", false);
		return productos(contexto, primeraLlamada);
	}

	public static RespuestaMB productos(ContextoMB contexto, boolean primeraLlamada) {
		if (primeraLlamada) {
			return productosPrimeraLlamada(contexto);
		} else {
			return productosLegacy(contexto);
		}
	}

	public static RespuestaMB productosLegacy(ContextoMB contexto) {
		Boolean buscarFCI = contexto.parametros.bool("buscarFCI", false);
		Objeto productos = new Objeto();

		ApiResponseMB response = ProductosService.productos(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		// LLAMADAS EN PARALELO
		List<Futuro<String>> listaFuturoCuentas = new ArrayList<>();
		for (Cuenta cuenta : contexto.cuentas()) {
			Futuro<String> futuroCBU = new Futuro<>(() -> cuenta.cbu());
			listaFuturoCuentas.add(futuroCBU);
		}

		List<Futuro<Objeto>> listaFuturoTarjetas1 = new ArrayList<>();
		List<Futuro<Boolean>> listaFuturoTarjetas2 = new ArrayList<>();
		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC", "prendidoLogicaFechaTC_cobis")) {
				Futuro<Objeto> futuroFechaTC = new Futuro<>(() -> MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null));
				listaFuturoTarjetas1.add(futuroFechaTC);
			}
			Futuro<Boolean> futuroAdheridoResumenElectronico = new Futuro<>(() -> tarjetaCredito.adheridoResumenElectronico());
			listaFuturoTarjetas2.add(futuroAdheridoResumenElectronico);
		}
		Futuro<ApiResponseMB> futuroCuotapartistaResponse = buscarFCI ? new Futuro<>(() -> RestInversiones.cuotapartista(contexto, null, MBInversion.tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento())) : new Futuro<>(() -> null);
		Futuro<List<ProductoMora>> futuroProductosEnMora = new Futuro<>(() -> getProductosEnMora(contexto));
		
		
		for (Futuro<String> futuroCBU : listaFuturoCuentas) {
			futuroCBU.get();
		}
		for (Futuro<Objeto> futuroFechaTC : listaFuturoTarjetas1) {
			futuroFechaTC.get();
		}
		for (Futuro<Boolean> futuroAdheridoResumenElectronico : listaFuturoTarjetas2) {
			futuroAdheridoResumenElectronico.get();
		}

		Futuro<Boolean> cambioDetectadoNormativo = new Futuro<>(() -> RestContexto.cambioDetectadoParaNormativoPPV2(contexto, false));
		Futuro<Boolean> futuroEnMora = new Futuro<>(() -> RestContexto.enMora(contexto));

		// FIN LLAMADAS EN PARALELO

		Objeto productosPaquete = new Objeto();
		Objeto adelantoExistente = new Objeto();
		for (Cuenta cuenta : contexto.cuentas()) {
			String estadoTd = "";

			if (ConfigMB.bool("prendido_link_td_habilitada",false)) {
				try {
					estadoTd = contexto.tarjetaDebitoAsociada(cuenta).estadoLink();
				}catch (Exception ignored){	}
			}

			Objeto item = new Objeto();
			item.set("id", cuenta.id());
			item.set("tdInactiva", estadoTd.equalsIgnoreCase("INACTIVA"));
			item.set("descripcion", cuenta.producto());
			item.set("tipoLargo", cuenta.tipoLargo());
			item.set("tipoCorto", cuenta.tipoCorto());
			item.set("numero", cuenta.numero());
			item.set("numeroFormateado", cuenta.numeroFormateado());
			item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
			item.set("titularidad", cuenta.titularidad());
			item.set("moneda", cuenta.moneda());
			item.set("simboloMoneda", cuenta.simboloMoneda());
			item.set("estado", cuenta.descripcionEstado());
			item.set("saldo", cuenta.saldo());
			item.set("saldoFormateado", cuenta.saldoFormateado());
			item.set("acuerdo", cuenta.acuerdo());
			item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
			item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
			item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
			item.set("esMAU", cuenta.categoria().equals("MAU"));
			item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
			item.set("cbu", cuenta.cbu());
			item.set("permiteCuotificacion", !contexto.persona().esEmpleado() && !cambioDetectadoNormativo.get() && !futuroEnMora.get());
			item.set("categoria", cuenta.categoria());
			if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
				item.set("adelantoDisponible", cuenta.adelantoDisponible());
				item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
				item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
				item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
				item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
				item.set("adelantoInteresesDevengadosFormateado", Formateador.importe(cuenta.adelantoInteresesDevengados()));
				adelantoExistente = item;
			} else {
				productos.add("cuentas", item);
			}

			if (cuenta.idPaquete() != null && !"".equals(cuenta.idPaquete())) {
				if (!"80".equals(cuenta.idMoneda()) || cuenta.esCuentaCorriente()) {
					Objeto productoPaquete = new Objeto();
					productoPaquete.set("id", cuenta.id());
					productoPaquete.set("titulo", cuenta.producto());
					productoPaquete.set("descripcion", cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());
					productosPaquete.add(productoPaquete);
				}
			}
		}
		productos.set("cuentas", ConfigMB.bool("prendido_adelanto_bh") ? reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente) : productos.objetos("cuentas"));

		Objeto titular = new Objeto();
		titular.set("nombre", contexto.persona().nombre());
		titular.set("apellido", contexto.persona().apellido());
		
		productos.set("titular", titular);
		
		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			Objeto item = new Objeto();
			item.set("id", tarjetaDebito.id());
			item.set("descripcion", tarjetaDebito.producto());
			item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
			item.set("titularidad", tarjetaDebito.titularidad());
			item.set("virtual", tarjetaDebito.virtual());
			if (!ConfigMB.esOpenShift()) {
				item.set("numero", tarjetaDebito.numero());
			}
			productos.add("tarjetasDebito", item);

			if (tarjetaDebito.idPaquete() != null && !"".equals(tarjetaDebito.idPaquete())) {
				Objeto productoPaquete = new Objeto();
				productoPaquete.set("id", tarjetaDebito.id());
				productoPaquete.set("titulo", "Tarjeta de Débito");
				productoPaquete.set("descripcion", "VISA XXXX-" + tarjetaDebito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}
		}

		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
			Objeto item = new Objeto();
			item.set("id", tarjetaCredito.idEncriptado());
			item.set("descripcion", tarjetaCredito.producto());
			item.set("tipo", tarjetaCredito.tipo());
			item.set("idTipo", tarjetaCredito.idTipo());
			item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
			item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
			item.set("estado", tarjetaCredito.estado());
			item.set("titularidad", tarjetaCredito.titularidad());
			item.set("debitosPesos", tarjetaCredito.debitosPesos());
			item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
			item.set("debitosDolares", tarjetaCredito.debitosDolares());
			item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());

			String vencimiento;
			String cierre;
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC", "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
				Objeto fechaTC = MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null);
				vencimiento = fechaTC.string("vencimiento");
				cierre = fechaTC.string("cierre");
			} else {
				cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
				vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
			}

			try {
				item.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico());
			} catch (Exception e) {
				item.set("adheridoResumenElectronico", true);
			}

			item.set("fechaCierre", cierre);
			item.set("fechaProximoCierre", tarjetaCredito.fechaProximoCierre("dd/MM/yyyy"));
			item.set("fechaVencimiento", vencimiento);
			item.set("formaPago", tarjetaCredito.formaPago().equalsIgnoreCase("EFECTIVO") ? "Manual" : tarjetaCredito.formaPago());
			item.set("esHml", tarjetaCredito.esHML());
			item.set("numero", tarjetaCredito.idEncriptado());
			item.set("cuenta", tarjetaCredito.cuenta());
			item.set("altaPuntoVenta", tarjetaCredito.altaPuntoVenta());
			
			productos.add("tarjetasCredito", item);

			if (tarjetaCredito.idPaquete() != null && !"".equals(tarjetaCredito.idPaquete()) && tarjetaCredito.esTitular()) {
				Objeto productoPaquete = new Objeto();
				productoPaquete.set("id", tarjetaCredito.idEncriptado());
				productoPaquete.set("titulo", "Tarjeta de Crédito");
				productoPaquete.set("descripcion", tarjetaCredito.tipo() + " " + "XXXX-" + tarjetaCredito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}

		}

		Objeto objetosPlazosFijos = new Objeto();
		Objeto totalSaldosPlazosFijos = new Objeto().set("totalPesos", BigDecimal.ZERO).set("totalDolares", BigDecimal.ZERO);
		totalSaldosPlazosFijos.set("totalPesosLogros", BigDecimal.ZERO).set("totalDolaresLogros", BigDecimal.ZERO);

		for (PlazoFijo plazoFijo : contexto.plazosFijos()) {
			Objeto item = new Objeto();
			item.set("id", plazoFijo.id());
			item.set("descripcion", plazoFijo.producto());
			item.set("tipo", plazoFijo.descripcion());
			item.set("numero", plazoFijo.numero());
			item.set("titularidad", plazoFijo.titularidad());
			item.set("moneda", plazoFijo.descripcionMoneda());
			item.set("simboloMoneda", plazoFijo.moneda());
			item.set("importeInicial", plazoFijo.importeInicial());
			item.set("importeInicialFormateado", plazoFijo.importeInicialFormateado());
			item.set("fechaAlta", plazoFijo.fechaAlta("dd/MM/yy"));
			item.set("fechaVencimiento", plazoFijo.fechaVencimiento("dd/MM/yy"));
			item.set("estado", plazoFijo.estado());
			item.set("orden", plazoFijo.fechaVencimiento().getTime());
			if (plazoFijo.validarPFLogros()) {
				totalSaldosXPlazoFijo(totalSaldosPlazosFijos, plazoFijo, "Logros");
				item.set("plazoFijoLogros", true);
			} else {
				totalSaldosXPlazoFijo(totalSaldosPlazosFijos, plazoFijo, "");
			}

			objetosPlazosFijos.add(item);
		}
		productos.set("plazosFijos", objetosPlazosFijos.ordenar("orden"));
		productos.set("totalSaldosPlazosFijos", totalSaldosPlazosFijos);
		List<ProductoMora> productosEnMora = futuroProductosEnMora.get();
		List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
		productosEnMora.forEach(productoMora -> {
			ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
			if (Objects.nonNull(item)) {
				productosEnMoraDetalles.add(item);
			}
		});

		for (Prestamo prestamo : contexto.prestamos()) {
			if (!"C".equals(prestamo.idEstado())) {
				Objeto item = new Objeto();
				item.set("id", prestamo.id());
				item.set("descripcion", prestamo.producto());
				item.set("tipo", prestamo.tipo());
				item.set("numero", prestamo.numero());
				item.set("titularidad", prestamo.titularidad());
				item.set("moneda", prestamo.descripcionMoneda());
				item.set("simboloMoneda", prestamo.simboloMoneda());
				item.set("formaPago", prestamo.formaPago());
				item.set("fechaAlta", prestamo.fechaAlta("dd/MM/yy"));
				item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yy"));
				item.set("montoAprobado", prestamo.montoAprobado());
				item.set("montoAprobadoFormateado", prestamo.montoAprobadoFormateado());
				Boolean checkCuotasMora = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_checkMoraTempranaCuotas");
				verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item, checkCuotasMora);
				productos.add("prestamos", item);
			}
		}

		for (CuentaComitente cuentaComitente : contexto.cuentasComitentes()) {
			Objeto item = new Objeto();
			item.set("id", cuentaComitente.id());
			item.set("descripcion", cuentaComitente.producto());
			item.set("numero", cuentaComitente.numero());
			item.set("titularidad", cuentaComitente.titularidad());
			productos.add("cuentasComitentes", item);
		}

		for (CajaSeguridad cajaSeguridad : contexto.cajasSeguridad()) {
			Objeto item = new Objeto();
			item.set("id", cajaSeguridad.id());
			item.set("descripcion", cajaSeguridad.producto());
			item.set("numero", cajaSeguridad.numero());
			item.set("titularidad", cajaSeguridad.titularidad());
			item.set("estado", cajaSeguridad.estado());
			item.set("fechaVencimiento", cajaSeguridad.fechaVencimiento("dd/MM/yyyy"));
			item.set("sucursal", cajaSeguridad.sucursal());
			productos.add("cajasSeguridad", item);
		}

		if (buscarFCI) {
			ApiResponseMB cuotapartistaResponse = futuroCuotapartistaResponse.get();
			if (!cuotapartistaResponse.hayError()) {
				for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
					Objeto item = new Objeto();
					item.set("numeroCuotapartista", cuotapartista.string("NumeroCuotapartista"));
					item.set("estaAnulado", cuotapartista.bool("EstaAnulado"));
					productos.add("fci", item);
				}
			}
		}

		for (Objeto objeto : response.objetos("productos")) {
			if ("PAQ".equals(objeto.string("tipo"))) {
				Objeto item = new Objeto();
				String codigoPaquete = objeto.string("codigoPaquete");
				item.set("id", codigoPaquete);
				item.set("descPaquete", Paquete.mapaDescripciones().get(objeto.string("codigoPaquete")));
				item.set("tipoTitularidad", objeto.string("tipoTitularidad"));
				item.set("productos", productosPaquete);

				boolean mostrarLeyenda = true;

				if (contexto.persona().esEmpleado() || "34".equals(codigoPaquete) || "35".equals(codigoPaquete) || "36".equals(codigoPaquete) || "37".equals(codigoPaquete) || "38".equals(codigoPaquete)) {
					mostrarLeyenda = false;
				}
				item.set("mostrarLeyenda", mostrarLeyenda);

				productos.add("paquetes", item);
			}
		}

		Objeto consolidadoOnboardings = new Objeto();
		try {
			consolidadoOnboardings.set("onboardingMoraMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING_MORA"));
			consolidadoOnboardings.set("onboardingPromesaMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING"));
		} catch (Exception e) {
			consolidadoOnboardings.set("onboardingMoraMostrado", true);
			consolidadoOnboardings.set("onboardingPromesaMostrado", true);
		}
		productos.set("consolidadoOnboarding", consolidadoOnboardings);

		RespuestaMB respuesta = RespuestaMB.exito("productos", productos);
		if (ProductosService.productos(contexto).objetos("errores").size() > 0) {
			respuesta.setEstadoExistenErrores();
		}
		return respuesta;
	}

	protected static Date dateStringToDate(Date fecha, String formato) throws ParseException {
		String fechaActualString = new SimpleDateFormat(formato).format(fecha);
		return new SimpleDateFormat(formato).parse(fechaActualString);
	}

	protected static boolean esFechaActualSuperiorVencimiento(Date fechaPosteriorCierre, Date fechaActual) {
		return fechaActual.compareTo(fechaPosteriorCierre) == 1;
	}

	/*
	 * Esta versión incluye algunos datos que no estaban presentes en la anterior
	 * llamada, se agrega una llamada adicional para mantener retrocompatibilidad
	 */
	public static RespuestaMB productosPrimeraLlamada(ContextoMB contexto) {
		Boolean buscarFCI = contexto.parametros.bool("buscarFCI", false);
		Objeto productos = new Objeto();

		ApiResponseMB response = ProductosService.productos(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		// LLAMADAS EN PARALELO
		List<Futuro<String>> listaFuturoCuentas = new ArrayList<>();
		for (Cuenta cuenta : contexto.cuentas()) {
			Futuro<String> futuroCBU = new Futuro<>(() -> cuenta.cbu());
			listaFuturoCuentas.add(futuroCBU);
		}

		List<Futuro<Objeto>> listaFuturoTarjetas1 = new ArrayList<>();
		List<Futuro<Boolean>> listaFuturoTarjetas2 = new ArrayList<>();
		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC", "prendidoLogicaFechaTC_cobis")) {
				Futuro<Objeto> futuroFechaTC = new Futuro<>(() -> MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null));
				listaFuturoTarjetas1.add(futuroFechaTC);
			}
			Futuro<Boolean> futuroAdheridoResumenElectronico = new Futuro<>(() -> tarjetaCredito.adheridoResumenElectronico());
			listaFuturoTarjetas2.add(futuroAdheridoResumenElectronico);
		}
		Futuro<ApiResponseMB> futuroCuotapartistaResponse = buscarFCI ? new Futuro<>(() -> RestInversiones.cuotapartista(contexto, null, MBInversion.tipoDocEsco(contexto.persona()), null, false, contexto.persona().esPersonaJuridica() ? contexto.persona().cuit() : contexto.persona().numeroDocumento())) : new Futuro<>(() -> null);

		for (Futuro<String> futuroCBU : listaFuturoCuentas) {
			futuroCBU.get();
		}
		for (Futuro<Objeto> futuroFechaTC : listaFuturoTarjetas1) {
			futuroFechaTC.get();
		}
		for (Futuro<Boolean> futuroAdheridoResumenElectronico : listaFuturoTarjetas2) {
			futuroAdheridoResumenElectronico.get();
		}

		// FIN LLAMADAS EN PARALELO

		Objeto productosPaquete = new Objeto();
		Objeto adelantoExistente = new Objeto();
		for (Cuenta cuenta : contexto.cuentas()) {
			String estadoTd = "";
			if (ConfigMB.bool("prendido_link_td_habilitada", false)) {
				try {
					estadoTd = contexto.tarjetaDebitoAsociada(cuenta).estadoLink();
				}catch (Exception ignored){	}
			}

			Objeto item = new Objeto();
			item.set("id", cuenta.id());
			item.set("tdInactiva", estadoTd.equalsIgnoreCase("INACTIVA"));
			item.set("descripcion", cuenta.producto());
			item.set("tipoLargo", cuenta.tipoLargo());
			item.set("tipoCorto", cuenta.tipoCorto());
			item.set("numero", cuenta.numero());
			item.set("numeroFormateado", cuenta.numeroFormateado());
			item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
			item.set("titularidad", cuenta.titularidad());
			item.set("moneda", cuenta.moneda());
			item.set("idMoneda", cuenta.idMoneda());
			item.set("simboloMoneda", cuenta.simboloMoneda());
			item.set("estado", cuenta.descripcionEstado());
			item.set("saldo", cuenta.saldo());
			item.set("saldoFormateado", cuenta.saldoFormateado());
			item.set("acuerdo", cuenta.acuerdo());
			item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
			item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
			item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
			item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
			item.set("cbu", cuenta.cbu());
			item.set("categoria", cuenta.categoria());
			if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
				item.set("adelantoDisponible", cuenta.adelantoDisponible());
				item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
				item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
				item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
				item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
				item.set("adelantoInteresesDevengadosFormateado", Formateador.importe(cuenta.adelantoInteresesDevengados()));
				adelantoExistente = item;
			} else {
				productos.add("cuentas", item);
			}

			if (cuenta.idPaquete() != null && !"".equals(cuenta.idPaquete())) {
				if (!"80".equals(cuenta.idMoneda()) || cuenta.esCuentaCorriente()) {
					Objeto productoPaquete = new Objeto();
					productoPaquete.set("id", cuenta.id());
					productoPaquete.set("titulo", cuenta.producto());
					productoPaquete.set("descripcion", cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());
					productosPaquete.add(productoPaquete);
				}
			}

		}
		productos.set("cuentas", ConfigMB.bool("prendido_adelanto_bh") ? reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente) : productos.objetos("cuentas"));

		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			Objeto item = new Objeto();
			item.set("id", tarjetaDebito.id());
			item.set("descripcion", tarjetaDebito.producto());
			item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
			item.set("titularidad", tarjetaDebito.titularidad());
			if (!ConfigMB.esOpenShift()) {
				item.set("numero", tarjetaDebito.numero());
			}
			productos.add("tarjetasDebito", item);

			if (tarjetaDebito.idPaquete() != null && !"".equals(tarjetaDebito.idPaquete())) {
				Objeto productoPaquete = new Objeto();
				productoPaquete.set("id", tarjetaDebito.id());
				productoPaquete.set("titulo", "Tarjeta de Débito");
				productoPaquete.set("descripcion", "VISA XXXX-" + tarjetaDebito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}
		}

		for (TarjetaCredito tarjetaCredito : contexto.tarjetasCreditoTitularConAdicionalesTercero()) {
			Objeto item = new Objeto();
			item.set("id", tarjetaCredito.idEncriptado());
			item.set("descripcion", tarjetaCredito.producto());
			item.set("tipo", tarjetaCredito.tipo());
			item.set("idTipo", tarjetaCredito.idTipo());
			item.set("ultimos4digitos", tarjetaCredito.ultimos4digitos());
			item.set("numeroEnmascarado", tarjetaCredito.numeroEnmascarado());
			item.set("estado", tarjetaCredito.estado());
			item.set("titularidad", tarjetaCredito.titularidad());
			item.set("debitosPesos", tarjetaCredito.debitosPesos());
			item.set("debitosPesosFormateado", tarjetaCredito.debitosPesosFormateado());
			item.set("debitosDolares", tarjetaCredito.debitosDolares());
			item.set("debitosDolaresFormateado", tarjetaCredito.debitosDolaresFormateado());

			String vencimiento;
			String cierre;

			if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC", "prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
				Objeto fechaTC = MBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null);
				vencimiento = fechaTC.string("vencimiento");
				cierre = fechaTC.string("cierre");
			} else {
				cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
				vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
			}

			try {
				item.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico());
			} catch (Exception e) {
				item.set("adheridoResumenElectronico", true);
			}

			item.set("fechaCierre", cierre);
			item.set("fechaProximoCierre", tarjetaCredito.fechaProximoCierre("dd/MM/yyyy"));
			item.set("fechaVencimiento", vencimiento);
			item.set("formaPago", tarjetaCredito.formaPago().equalsIgnoreCase("EFECTIVO") ? "Manual" : tarjetaCredito.formaPago());
			item.set("esHml", tarjetaCredito.esHML());
			item.set("numero", tarjetaCredito.idEncriptado());
			item.set("cuenta", tarjetaCredito.cuenta());
			item.set("altaPuntoVenta", tarjetaCredito.altaPuntoVenta());

			productos.add("tarjetasCredito", item);

			if (tarjetaCredito.idPaquete() != null && !"".equals(tarjetaCredito.idPaquete()) && tarjetaCredito.esTitular()) {
				Objeto productoPaquete = new Objeto();
				productoPaquete.set("id", tarjetaCredito.idEncriptado());
				productoPaquete.set("titulo", "Tarjeta de Crédito");
				productoPaquete.set("descripcion", tarjetaCredito.tipo() + " " + "XXXX-" + tarjetaCredito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}

		}

		Objeto objetosPlazosFijos = new Objeto();
		Objeto totalSaldosPlazosFijos = new Objeto().set("totalPesos", BigDecimal.ZERO).set("totalDolares", BigDecimal.ZERO);
		totalSaldosPlazosFijos.set("totalPesosLogros", BigDecimal.ZERO).set("totalDolaresLogros", BigDecimal.ZERO);

		for (PlazoFijo plazoFijo : contexto.plazosFijos()) {
			Objeto item = new Objeto();
			item.set("id", plazoFijo.id());
			item.set("descripcion", plazoFijo.producto());
			item.set("tipo", plazoFijo.descripcion());
			item.set("numero", plazoFijo.numero());
			item.set("titularidad", plazoFijo.titularidad());
			item.set("moneda", plazoFijo.descripcionMoneda());
			item.set("simboloMoneda", plazoFijo.moneda());
			item.set("importeInicial", plazoFijo.importeInicial());
			item.set("importeInicialFormateado", plazoFijo.importeInicialFormateado());
			item.set("fechaAlta", plazoFijo.fechaAlta("dd/MM/yy"));
			item.set("fechaVencimiento", plazoFijo.fechaVencimiento("dd/MM/yy"));
			item.set("estado", plazoFijo.estado());
			item.set("orden", plazoFijo.fechaVencimiento().getTime());
			if (plazoFijo.validarPFLogros()) {
				totalSaldosXPlazoFijo(totalSaldosPlazosFijos, plazoFijo, "Logros");
				item.set("plazoFijoLogros", true);
			} else {
				totalSaldosXPlazoFijo(totalSaldosPlazosFijos, plazoFijo, "");
			}

			objetosPlazosFijos.add(item);
		}
		productos.set("plazosFijos", objetosPlazosFijos.ordenar("orden"));
		productos.set("totalSaldosPlazosFijos", totalSaldosPlazosFijos);

		for (Prestamo prestamo : contexto.prestamos()) {
			if (!"C".equals(prestamo.idEstado())) {
				Objeto item = new Objeto();
				item.set("id", prestamo.id());
				item.set("descripcion", prestamo.producto());
				item.set("tipo", prestamo.tipo());
				item.set("numero", prestamo.numero());
				item.set("titularidad", prestamo.titularidad());
				item.set("moneda", prestamo.descripcionMoneda());
				item.set("simboloMoneda", prestamo.simboloMoneda());
				item.set("formaPago", prestamo.formaPago());
				item.set("fechaAlta", prestamo.fechaAlta("dd/MM/yy"));
				item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yy"));
				item.set("montoAprobado", prestamo.montoAprobado());
				item.set("montoAprobadoFormateado", prestamo.montoAprobadoFormateado());
				productos.add("prestamos", item);
			}
		}

		for (CuentaComitente cuentaComitente : contexto.cuentasComitentes()) {
			Objeto item = new Objeto();
			item.set("id", cuentaComitente.id());
			item.set("descripcion", cuentaComitente.producto());
			item.set("numero", cuentaComitente.numero());
			item.set("titularidad", cuentaComitente.titularidad());
			productos.add("cuentasComitentes", item);
		}

		if (buscarFCI) {
			ApiResponseMB cuotapartistaResponse = futuroCuotapartistaResponse.get();
			if (!cuotapartistaResponse.hayError()) {
				for (Objeto cuotapartista : cuotapartistaResponse.objetos("CuotapartistaModel")) {
					Objeto item = new Objeto();
					item.set("numeroCuotapartista", cuotapartista.string("NumeroCuotapartista"));
					item.set("estaAnulado", cuotapartista.bool("EstaAnulado"));
					productos.add("fci", item);
				}
			}
		}

		for (CajaSeguridad cajaSeguridad : contexto.cajasSeguridad()) {
			Objeto item = new Objeto();
			item.set("id", cajaSeguridad.id());
			item.set("descripcion", cajaSeguridad.producto());
			item.set("numero", cajaSeguridad.numero());
			item.set("titularidad", cajaSeguridad.titularidad());
			item.set("estado", cajaSeguridad.estado());
			item.set("fechaVencimiento", cajaSeguridad.fechaVencimiento("dd/MM/yyyy"));
			item.set("sucursal", cajaSeguridad.sucursal());
			productos.add("cajasSeguridad", item);
		}

		for (Objeto objeto : response.objetos("productos")) {
			if ("PAQ".equals(objeto.string("tipo"))) {
				Objeto item = new Objeto();
				String codigoPaquete = objeto.string("codigoPaquete");
				item.set("id", codigoPaquete);
				item.set("descPaquete", Paquete.mapaDescripciones().get(objeto.string("codigoPaquete")));
				item.set("tipoTitularidad", objeto.string("tipoTitularidad"));
				item.set("productos", productosPaquete);

				boolean mostrarLeyenda = true;

				if (contexto.persona().esEmpleado() || "34".equals(codigoPaquete) || "35".equals(codigoPaquete) || "36".equals(codigoPaquete) || "37".equals(codigoPaquete) || "38".equals(codigoPaquete)) {
					mostrarLeyenda = false;
				}
				item.set("mostrarLeyenda", mostrarLeyenda);

				productos.add("paquetes", item);
			}
		}

		Objeto consolidadoOnboardings = new Objeto();
		try {
			consolidadoOnboardings.set("onboardingMoraMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING_MORA"));
			consolidadoOnboardings.set("onboardingPromesaMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING"));
		} catch (Exception e) {
			consolidadoOnboardings.set("onboardingMoraMostrado", true);
			consolidadoOnboardings.set("onboardingPromesaMostrado", true);
		}
		productos.set("consolidadoOnboarding", consolidadoOnboardings);

		RespuestaMB respuesta = RespuestaMB.exito("productos", productos);
		if (ProductosService.productos(contexto).objetos("errores").size() > 0) {
			respuesta.setEstadoExistenErrores();
		}

		return respuesta;
	}

	private static void totalSaldosXPlazoFijo(Objeto totalSaldos, PlazoFijo plazoFijo, String tipo) {
		BigDecimal total = BigDecimal.ZERO;
		if ("$".equals(plazoFijo.monedaActual())) {
			total = totalSaldos.bigDecimal("totalPesos" + tipo).add(plazoFijo.importeInicial());
			totalSaldos.set("totalPesos" + tipo, total);
		}
		if ("U$S".equals(plazoFijo.monedaActual())) {
			total = totalSaldos.bigDecimal("totalDolares" + tipo).add(plazoFijo.importeInicial());
			totalSaldos.set("totalDolares" + tipo, total);
		}
	}

	public static RespuestaMB limpiarCache(ContextoMB contexto) {
		PagoServicioService.eliminarCachePendientes(contexto);
		ProductosService.eliminarCacheProductos(contexto);
		return RespuestaMB.exito();
	}

	// TODO Deprecar filtrarPorCuentasPesos & filtrarPorCuentasDolares
	public static RespuestaMB cuentas(ContextoMB contexto) {
		String filtrarPorIdMoneda = contexto.parametros.string("filtrarPorIdMoneda", null);
		Boolean filtrarPorCuentasPesos = contexto.parametros.bool("filtrarPorCuentasPesos", false);
		Boolean filtrarPorCA = contexto.parametros.bool("filtrarPorCA", false);
		Boolean filtrarPorCuentasDolares = contexto.parametros.bool("filtrarPorCuentasDolares", false);
		Boolean filtrarPorCuentasUnipersonales = contexto.parametros.bool("filtrarPorCuentasUnipersonales", false);
		Boolean separarPorMoneda = contexto.parametros.bool("separarPorMoneda", false);
		Boolean filtrarInactivas = contexto.parametros.bool("filtrarInactivas", false); // emm-agrego este filtro para que filtre las inactivas
		Boolean filtrarCuentasPlanSueldo = contexto.parametros.bool("filtrarCuentasPlanSueldo", false);
		Boolean buscarEstadoVendedorCoelsa = contexto.parametros.bool("buscarEstadoVendedorCoelsa", false);
		Boolean buscarTDAsociada = contexto.parametros.bool("buscarTDAsociada", false);
		Boolean filtrarPorCERA = contexto.parametros.bool("filtrarPorCERA", false);

		ApiResponseMB response = ProductosService.productos(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		Objeto cuentas = new Objeto();
		List<Cuenta> todas = contexto.cuentas();

		if (filtrarPorCA) {
			todas.removeIf(cuenta -> (!"CA".equalsIgnoreCase(cuenta.descripcionCorta())));
		}
		
		if(filtrarPorCERA) {
			todas.removeIf(cuenta -> ("CRA".equalsIgnoreCase(cuenta.categoria())));
		}

		Objeto adelantoExistente = new Objeto();
		for (Cuenta cuenta : todas) {
			Objeto item = new Objeto();
			item.set("id", cuenta.id());
			item.set("descripcion", cuenta.producto());
			item.set("descripcionCorta", cuenta.descripcionCorta());
			item.set("numeroFormateado", cuenta.numeroFormateado());
			item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
			item.set("ultimos4digitos", cuenta.ultimos4digitos());
			item.set("titularidad", cuenta.titularidad());
			item.set("idMoneda", cuenta.idMoneda());
			item.set("moneda", cuenta.moneda());
			item.set("simboloMoneda", cuenta.simboloMoneda());
			item.set("estado", cuenta.descripcionEstado());
			item.set("saldo", cuenta.saldo());
			item.set("saldoFormateado", cuenta.saldoFormateado());
			item.set("acuerdo", cuenta.acuerdo());
			item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
			item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
			item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
			item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
			item.set("cbu", cuenta.cbu());
			if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
				item.set("adelantoDisponible", cuenta.adelantoDisponible());
				item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
				item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
				item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
				item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
				item.set("adelantoInteresesDevengadosFormateado", Formateador.importe(cuenta.adelantoInteresesDevengados()));
				adelantoExistente = item;
			}

			if (buscarEstadoVendedorCoelsa) {
				try {
					Boolean activa = RestDebin.cuentaActivaVendedor(contexto, cuenta);
					item.set("estadoVendedorCoelsa", activa ? "ACTIVA" : "INACTIVA");
				} catch (Exception e) {
					item.set("estadoVendedorCoelsa", "DESCONOCIDO");
				}
			}

			if (item.equals(adelantoExistente)) {
				continue;
			}

			if (filtrarInactivas && "I".equals(cuenta.idEstado())) {
				continue;
			}

			if (filtrarCuentasPlanSueldo) {
				ApiResponseMB responseCuenta = CuentasService.cuentaBH(contexto, cuenta.numero());
				if (!Objeto.setOf("EM", "K", "EV").contains(responseCuenta.string("categoria"))) {
					continue;
				}
			}

			if (filtrarPorIdMoneda != null && filtrarPorIdMoneda.equals(cuenta.idMoneda())) {
				if (!separarPorMoneda) {
					cuentas.add(item);
				} else {
					cuentas.add(cuenta.moneda().toLowerCase(), item);
				}
				continue;
			}

			if (filtrarPorIdMoneda == null) {
				Boolean pasaFiltro = !filtrarPorCuentasPesos && !filtrarPorCuentasDolares;
				pasaFiltro |= filtrarPorCuentasPesos && "80".equals(cuenta.idMoneda());
				pasaFiltro |= filtrarPorCuentasDolares && "2".equals(cuenta.idMoneda());
				if (filtrarPorCuentasUnipersonales && !cuenta.unipersonal()) {
					pasaFiltro = false;
				}
				if (pasaFiltro) {
					if (!separarPorMoneda) {
						cuentas.add(item);
					} else {
						cuentas.add(cuenta.moneda().toLowerCase().replace("ó", "o"), item); // TODO arreglar
					}
				}
			}

			if (buscarTDAsociada) {
				TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociadaHabilitadaLink(cuenta);
				item.set("tarjetaAsociadaVirtual", tarjetaDebito.virtual());
			}
		}

		return RespuestaMB.exito("cuentas", ConfigMB.bool("prendido_adelanto_bh") ? reorganizaCuentas(cuentas.objetos(), adelantoExistente) : cuentas);
	}

	public static RespuestaMB cuentasComitentes(ContextoMB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente", null);
		Boolean buscarCuentasAsociadas = contexto.parametros.bool("buscarCuentasAsociadas", false);
		Boolean buscarTenenciaValuada = contexto.parametros.bool("buscarTenenciaValuada", false);

		CuentaComitente cuentaComitenteRequest = contexto.cuentaComitente(idCuentaComitente);

		RespuestaMB respuesta = new RespuestaMB();
		Map<CuentaComitente, ApiResponseMB> mapaResponse = MBInversion.cuentasComitentesEspecies(contexto, respuesta);

		Objeto cuentasComitentes = new Objeto();
		for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
			if (cuentaComitenteRequest != null && !itemCuentaComitente.id().equals(idCuentaComitente)) {
				continue;
			}

			ApiResponseMB responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto, itemCuentaComitente.numero());
			if (responseCuentasComitentes.hayError()) {
				return RespuestaMB.error();
			}
			Objeto cuentaComitente = new Objeto();
			Objeto cuentas = new Objeto();
			for (Objeto cuentaItem : responseCuentasComitentes.objetos()) {
				Objeto item = new Objeto();
				String numeroCuenta = cuentaItem.string("NUMERO").trim();
				Cuenta cuenta = contexto.cuenta(numeroCuenta);
				if (cuenta == null) {
					continue;
				}
				item.set("id", cuenta.id());
				item.set("descripcion", cuenta.producto());
				item.set("descripcionCorta", cuenta.descripcionCorta());
				item.set("numeroFormateado", cuenta.numeroFormateado());
				item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
				item.set("ultimos4digitos", cuenta.ultimos4digitos());
				item.set("titularidad", cuenta.titularidad());
				item.set("idMoneda", cuenta.idMoneda());
				item.set("moneda", cuenta.moneda());
				item.set("simboloMoneda", cuenta.simboloMoneda());
				item.set("estado", cuenta.descripcionEstado());
				item.set("saldo", cuenta.saldo());
				item.set("saldoFormateado", cuenta.saldoFormateado());
				item.set("acuerdo", cuenta.acuerdo());
				item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
				item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
				item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
				if (buscarCuentasAsociadas) {
					cuentas.add(item);
				}
			}

			BigDecimal sumaTitulosValoresPesos = new BigDecimal("0");
			if (buscarTenenciaValuada) {
				List<CuentaComitente> cuenta= mapaResponse.keySet()
						.stream()
						.filter(c -> c.numero().equals(itemCuentaComitente.numero()))
						.collect(Collectors.toList());
				if (cuenta.isEmpty() || cuenta.size() > 1) {
					respuesta.setEstadoExistenErrores();
				} else {
					ApiResponseMB response = mapaResponse.get(cuenta.get(0));
					for (Objeto item : response.objetos()) {
						if (!"".equals(item.string("codigoEspecie"))) {
							sumaTitulosValoresPesos = sumaTitulosValoresPesos.add(item.bigDecimal("valorizacion"));
						}
					}
				}
			}

			cuentaComitente.set("id", itemCuentaComitente.id());
			cuentaComitente.set("numero", itemCuentaComitente.numero());
			cuentaComitente.set("titularidad", itemCuentaComitente.titularidad());
			if (buscarTenenciaValuada) {
				cuentaComitente.set("tenenciaPesos", sumaTitulosValoresPesos);
				cuentaComitente.set("tenenciaPesosFormateado", Formateador.importe(sumaTitulosValoresPesos));
			}
			cuentaComitente.set("cuentas", cuentas);
			cuentasComitentes.add(cuentaComitente);
		}

		respuesta.set("cuentasComitentes", cuentasComitentes);

		return respuesta;
	}

	public static RespuestaMB cuentasCuotapartistas(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();

		ApiResponseMB response = ProductosService.productos(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		for (Objeto objeto : response.objetos("productos")) {
			if (objeto.string("tipo").equals("RJA")) {
				Objeto item = new Objeto();
				item.set("id", objeto.string("numero"));
				item.set("titularidad", Texto.primeraMayuscula(objeto.string("descTipoTitularidad").toLowerCase()));
				respuesta.add("cuentasCuotapartistas", item);
			}
		}

		respuesta.set("poseeCuentaCuotapartista", respuesta.get("cuentasCuotapartistas") != null);
		return respuesta.ordenar("estado", "poseeCuentaCuotapartista");
	}

	public static RespuestaMB cajasSeguridad(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		for (CajaSeguridad cajaSeguridad : contexto.cajasSeguridad()) {
			Objeto item = new Objeto();
			item.set("id", cajaSeguridad.id());
			item.set("descripcion", cajaSeguridad.producto());
			// item.set("numero", cajaSeguridad.numero());
			item.set("titularidad", cajaSeguridad.titularidad());
			item.set("estado", cajaSeguridad.estado());
			item.set("fechaAlta", cajaSeguridad.fechaAlta("dd/MM/yyyy"));
			// item.set("fechaVencimiento", cajaSeguridad.fechaVencimiento("dd/MM/yyyy"));
			item.set("sucursal", cajaSeguridad.sucursal());

			ApiResponseMB response = RestCajaSeguridad.detalle(contexto, cajaSeguridad.numero());
			if (!response.hayError()) {
				for (Objeto datos : response.objetos()) {
					Cuenta cuenta = contexto.cuenta(datos.string("cuenta"));
					item.set("cuenta", cuenta != null ? cuenta.descripcionCorta() + " XXXX - " + cuenta.ultimos4digitos() : "");
					item.set("numero", datos.string("numeroProducto"));
					item.set("renovacionAutomatica", datos.bool("renueva"));
					item.set("modelo", Texto.primeraMayuscula(datos.string("descripcionProducto").toLowerCase()));
					item.set("fechaVencimiento", datos.date("fechaVencimiento", "yyyy-MM-dd", "dd/MM/yyyy"));

				}
			}

			respuesta.add("cajasSeguridad", item);
		}
		return respuesta;
	}

	public static RespuestaMB bajaCajaAhorro(ContextoMB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("ERROR");
		}
		if (!cuenta.idTipo().equals("AHO")) {
			return RespuestaMB.estado("ERROR");
		}

		RespuestaMB respuesta = new RespuestaMB();
		// CHEQUEO SI TIENE UNA SOLICITUD DE CAJA DE AHORRO EN CURSO
		if (!"".equals(cuenta.idPaquete())) {
			Objeto tcObj = new Objeto();
			tcObj.set("idPaquete", cuenta.idPaquete());

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "BAJA_PAQUETES", tcObj, true)) {
				return RespuestaMB.estado("SOLICITUD_EN_CURSO").set("message", ConfigMB.string("message_en_curso"));
			}
		} else {
			// TODO chequear baja CA
			Objeto tcObj = new Objeto();
			tcObj.set("numero", cuenta.numero());

			if (RestPostventa.tieneSolicitudEnCursoBajaCa(contexto, "BAJACA_PEDIDO", tcObj, true)) {
				return RespuestaMB.estado("SOLICITUD_EN_CURSO").set("message", ConfigMB.string("message_en_curso"));
			}
		}

		// CHEQUEO SI ESTA ASOCIADA A UNA CUENTA COMITENTE
		for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
			ApiResponseMB responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto, itemCuentaComitente.numero());
			if (responseCuentasComitentes.hayError()) {
				return RespuestaMB.estado("ERROR_CONSULTANDO_COMITENTES");
			}
			for (Objeto cuentaItem : responseCuentasComitentes.objetos()) {
				if (cuentaItem.string("NUMERO").trim().equals(cuenta.numero()) || cuentaItem.string("NUMERO").trim().equals(cuenta.id())) {
					return RespuestaMB.estado("CUENTA_COMITENTE_ASOCIADA");
				}
			}
		}

		// CHEQUEO SI LA CAJA DE AHORRO TIENE MAS DE UN TITULAR
		if (!cuenta.unipersonal()) {
			return RespuestaMB.estado("COTITULAR");
		}

		// CHEQUEO SI TIENE BLOQUEOS
		String numeroCorto = cuenta.numeroCorto();
		if ("".equals(numeroCorto)) {
			return RespuestaMB.error();
		}
		ApiResponseMB responseBloqueos = CuentasService.cajaAhorroBloqueos(contexto, numeroCorto);
		if (responseBloqueos.hayError()) {
			return RespuestaMB.estado("CONSULTA_BLOQUEOS");
		}
		if (!responseBloqueos.objetos().isEmpty()) {
			return RespuestaMB.estado("TIENE_BLOQUEOS");
		}

		ApiResponseMB responseReclamo;

		if ("80".equals(cuenta.idMoneda()) && !"".equals(cuenta.idPaquete())) {

			responseReclamo = RestPostventa.bajaPaquete(contexto, "BAJA_PAQUETES", cuenta.idPaquete());

			if (responseReclamo == null || responseReclamo.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

		} else if ("80".equals(cuenta.idMoneda()) && "".equals(cuenta.idPaquete())) {

			responseReclamo = RestPostventa.bajaCajaAhorro(contexto, "BAJACA_PEDIDO", cuenta);

			if (responseReclamo == null || responseReclamo.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}
			
			if (MBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("CANAL", ConfigMB.string("salesforce_nombre_canal"));
				parametros.set("NUMERO_CUENTA", cuenta.numero());
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				new Futuro<>(() -> MBSalesforce.registrarEventoSalesforce(contexto, ConfigMB.string("salesforce_baja_caja_ahorro_ok"), parametros));
			}

		} else {
			return RespuestaMB.error();
		}

		SqlResponseMB sqlResponse = insertarReclamo(contexto, "BAJA_RECL", cuenta.numero(), cuenta.idTipo(), cuenta.sucursal(), "");
		if (sqlResponse.hayError) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}

		if (!respuesta.hayError()) {
			respuesta.set("message", ConfigMB.string("message_baja_caja_ahorro"));
		}

		return respuesta;
	}

	public static RespuestaMB bajaCuentaCorriente(ContextoMB contexto) {
		String numeroCuenta = contexto.parametros.string("numeroCuenta");

		if (Objeto.anyEmpty(numeroCuenta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(numeroCuenta);
		if (cuenta == null) {
			return RespuestaMB.estado("ERROR");
		}
		if (!cuenta.idTipo().equals("CTE")) {
			return RespuestaMB.estado("ERROR");
		}

		RespuestaMB respuesta = new RespuestaMB();
		// CHEQUEO SI TIENE UNA SOLICITUD DE CUENTA CORRIENTE EN CURSO
		if (tieneSolicitudEnCurso(contexto, "BAJA", "CTE", cuenta.numero())) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}

		if (tieneSolicitudEnCurso(contexto, "BAJA_RECL", "CTE", cuenta.numero())) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}

		// CHEQUEO SI TIENE CHEQUERA
		ApiResponseMB responseChequeras = CuentasService.chequeras(contexto, cuenta.numero());
		if (responseChequeras.hayError()) {
			return RespuestaMB.estado("ERROR_CONSULTA_CHEQUERAS");
		}
		if (!responseChequeras.objetos().isEmpty()) {
			return RespuestaMB.estado("POSEE_CHEQUERA");
		}

		// CHEQUEO SI ESTA ASOCIADA A UNA CUENTA COMITENTE
		for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
			ApiResponseMB responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto, itemCuentaComitente.numero());
			if (responseCuentasComitentes.hayError()) {
				return RespuestaMB.estado("ERROR_CONSULTANDO_COMITENTES");
			}
			for (Objeto cuentaItem : responseCuentasComitentes.objetos()) {
				if (cuentaItem.string("NUMERO").trim().equals(cuenta.numero()) || cuentaItem.string("NUMERO").trim().equals(cuenta.id())) {
					return RespuestaMB.estado("CUENTA_COMITENTE_ASOCIADA");
				}
			}
		}

		// CHEQUEO SI TIENE DEBITO ADHERIDO
		ApiResponseMB responseDebitosAdheridos = CuentasService.debitosAdheridosCuentaCorriente(contexto, cuenta.numero());
		if (responseDebitosAdheridos.hayError()) {
			return RespuestaMB.estado("ERROR_CONSULTA_DEBITOS_ADHERIDOS");
		}
		if (!responseDebitosAdheridos.objetos().isEmpty()) {
			return RespuestaMB.estado("POSEE_DEBITOS_ADHERIDOS");
		}

		// CHEQUEO SI TIENE SALDO DISTINTO DE CERO
		if (cuenta.saldo().compareTo(new BigDecimal(0)) != 0) {
			return RespuestaMB.estado("SALDO_DISTINTO_DE_CERO");
		}

		// CHEQUEO SI LA CAJA DE AHORRO TIENE MAS DE UN TITULAR
		if (!cuenta.unipersonal()) {
			return RespuestaMB.estado("COTITULAR");
		}

		// CHEQUEO SI ESTA PAQUETIZADO
		if (!"".equals(cuenta.idPaquete())) {
			return RespuestaMB.estado("PAQUETIZADO");
		}

		// CHEQUEO SI TIENE BLOQUEOS
		ApiResponseMB responseBloqueos = CuentasService.cuentaCorrienteBloqueos(contexto, cuenta.numero());
		if (responseBloqueos.hayError()) {
			return RespuestaMB.estado("CONSULTA_BLOQUEOS");
		}
		if (!responseBloqueos.objetos().isEmpty()) {
			return RespuestaMB.estado("TIENE_BLOQUEOS");
		}

		return respuesta;
	}

	public static RespuestaMB propuestasDisponibilidad(ContextoMB contexto) {
		if (!MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_funciones_pwp", "prendido_funciones_pwp_cobis")) {
			return RespuestaMB.exito("disponible", false);
		}
		return RespuestaMB.exito("disponible", true);
	}

	public static RespuestaMB mostrarOnboarding(ContextoMB contexto) {
		if(!Objects.isNull(contexto.parametros.get("show_onboarding"))) {
			return setOnboardingFalse(contexto);
		}
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("SelectOnboarding", "mobile");
		String select = "SELECT show_onboarding ";
		
		sqlRequest.sql = select;
		sqlRequest.sql += "FROM Mobile.dbo.usuarios ";
		sqlRequest.sql += "WHERE id_cobis = ? ";
		
		sqlRequest.add(Integer.valueOf(contexto.idCobis()));
		
		sqlResponse = SqlMB.response(sqlRequest);
		
		Objeto onboarding = sqlResponse.registros.get(0);
		
		return RespuestaMB.exito("show_onboarding", onboarding.get("show_onboarding"));

	}
	
	private static RespuestaMB setOnboardingFalse(ContextoMB contexto) {
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("UpdateOnboarding", "mobile");
		
		String update = "UPDATE Mobile.dbo.usuarios SET show_onboarding = ? WHERE id_cobis = ? ";
		
		sqlRequest.sql = update;
		sqlRequest.add(false);
		sqlRequest.add(Integer.valueOf(contexto.idCobis()));

		sqlResponse = SqlMB.response(sqlRequest);
		
		if(sqlResponse.hayError)
			return RespuestaMB.error();
		return RespuestaMB.exito();
	}
	
	public static RespuestaMB validarCashBack(ContextoMB contexto) {
		
		Boolean prendidoCashback = MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_cashback", "prendido_cashback_cobis");

		return RespuestaMB.exito("prendidoCashBack", prendidoCashback);
	}
	
	
	private static Objeto getRequest(ContextoMB contexto, Integer idCanje) {
		
		String[] premio = ConfigMB.string("cashback_premio_" + contexto.parametros.string("id")).split("_");
		var itemPremio = new Objeto();
		itemPremio.set("descripcion", premio[1]);
		itemPremio.set("puntos", premio[2]);
		itemPremio.set("tipo_cashback", premio[3]);
		itemPremio.set("cashback", premio[4]);
		
		Objeto request = new Objeto();
		Objeto cliente = new Objeto();
		Objeto datosEnvio = new Objeto();
		Objeto solicitud = new Objeto();
		Objeto canjes = new Objeto();
		Objeto canjesItems = new Objeto();
		Objeto metodoPago = new Objeto();
		cliente.set("datosEnvio", datosEnvio);
		solicitud.set("canjes", canjes);
		canjes.add(canjesItems);
		canjesItems.set("metodoPago", metodoPago);
		request.set("cliente", cliente);
		request.set("solicitud", solicitud);
		canjes.add("metodoPago", canjesItems);

		
		cliente.set("tipoDocumento", 0);
		cliente.set("fechaApertura", getFecha());
		cliente.set("codigoCliente", contexto.idCobis());
		cliente.set("apellido", contexto.persona().apellido());
		cliente.set("numeroDocumento", 0);
		cliente.set("nombre", contexto.persona().nombre());
		
		datosEnvio.set("piso", "1");
		datosEnvio.set("numero", "123");
		datosEnvio.set("codigoPostal", "1234");
		datosEnvio.set("calle", "Test");
		datosEnvio.set("codigoProvincia", "2");
		datosEnvio.set("pais", "1");
		datosEnvio.set("entreCalles", "Test");
		datosEnvio.set("ciudad", "Test");
		datosEnvio.set("observaciones", ConfigMB.string("cashback_observaciones"));
		datosEnvio.set("departamento", "1");
		datosEnvio.set("localidad", "Test");
		datosEnvio.set("telefono", "111111111");
		datosEnvio.set("email", contexto.persona().email());
		
		String descripcion = "Credito de " +
			itemPremio.string("puntos") + " en " +
			itemPremio.string("descripcion");
		
		canjesItems.set("descripcion", descripcion);
		canjesItems.set("id", idCanje);
		canjesItems.set("puntos", itemPremio.integer("puntos"));
		canjesItems.set("nombre", itemPremio.string("descripcion").toUpperCase());
		canjesItems.set("codigoPremio", contexto.parametros.string("id"));
		canjesItems.set("cuotas", "N");
		
		solicitud.set("idEstado", "NEW");
		solicitud.set("idTema", 1255);
		solicitud.set("idProducto", "ATC");
		solicitud.set("codigoProducto", 0000000000);
		solicitud.set("numeroProducto", 0000000000);
		solicitud.set("idGrupo", 1);
		
		
		return request;
	}
	
	private static String getFecha() {
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
        String fechaFormateada = ahora.format(formatter);
		return fechaFormateada;
	}
	
	public static RespuestaMB consultaCashBack(ContextoMB contexto) {
		Objeto cashback = new Objeto();
		for(int i = 1; i <= 6; i++ ) {
			String codigo = String.format("202300%02d", i);
			String[] premio = ConfigMB.string("cashback_premio_" + codigo).split("_");
			var item = new Objeto();
			item.set("id", premio[0]);
			item.set("descripcion", premio[1]);
			item.set("puntos", premio[2]);
			item.set("tipo_cashback", premio[3]);
			item.set("cashback", premio[4]);
			cashback.add(item);
		}
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("tc_permitida", tieneTcPermitida(contexto));
		respuesta.set("ca_permitida", tieneCAPermitida(contexto));
		respuesta.set("cashback", cashback);
		respuesta.set("estado", "0");
    	return respuesta;
	}
	
	private static boolean tieneCAPermitida(ContextoMB contexto) {
		boolean permitido = false;
		permitido = contexto.cajaAhorroTitularPesos().descripcionEstado().equals("Activa") | contexto.cajaAhorroTitularPesos().descripcionEstado().equals("Vigente");
		return permitido;
	}
	
	private static boolean tieneTcPermitida(ContextoMB contexto) {
		boolean permitido = false;
		permitido = contexto.tarjetaCreditoTitular() != null ? contexto.tarjetaCreditoTitular().esPrefijoVisa().equals("2") ? true : false : false;
		
		return permitido;
	}
	

	public static RespuestaMB canjearCashback(ContextoMB contexto) {
		SqlResponseMB sql = consultarCashbackIdCanje(contexto);
		Integer idCanje = sql.registros.get(0).integer("id_canje");
		//Para ambiente bajo se trabaja con idOperacion = idCanje, pero en prod se espera a la respuesta de api-rewards
		Integer idOperacion = idCanje;
		
		if(ConfigMB.esProduccion()) {
			var apiRequest = ApiMB.request("CanjeItemsPyP", "rewards", "POST", "/v1/canjeItems", contexto);
			apiRequest.body(getRequest(contexto, idCanje));
			apiRequest.path("id", contexto.idCobis());
			var api = ApiMB.response(apiRequest);
			if(api.hayError())
				return RespuestaMB.error();
			try {
				for(Objeto canje : api.objetos("canjes")) {
					idOperacion = canje.integer("idOperacion");
				}
			}
			catch(Exception e) {
				
			}
		}
		RespuestaMB respuesta = new RespuestaMB();

		respuesta.set("id_operacion", idOperacion);
		insertarCashbackCanjeado(contexto, idOperacion.toString());
		return respuesta;

	}
	
	private static RespuestaMB aceptarPropuestaV2(ContextoMB contexto) {
		String token = contexto.parametros.string("x-scope", null);
		int reintentos = 0;
		final int MAX_REINTENTOS = 1;

		while (reintentos <= MAX_REINTENTOS) {
			var apiRequest = ApiMB.request("aceptarPropuesta", "productos", "POST", "/v1/propuesta/{id}/aceptar", contexto);
			apiRequest.path("id", idPath(token));
			apiRequest.header("x-scope", token);
			var api = ApiMB.response(apiRequest);

			if (!api.hayError()) break;

			var ofertaMobi = getPropuestaMobi(contexto, token);
			if (ofertaMobi.hayError())
				return RespuestaMB.error();

			boolean ofertaDisponible = ofertaMobi.objeto("estado").bool("disponible");
			if (!ofertaDisponible) break;
			
			if(reintentos == MAX_REINTENTOS)
				return RespuestaMB.error();

			reintentos++;
			
		}

		canjearPropuestas(contexto);

		new Futuro<>(() -> {
			RestPostventa.casoCRMPrisma(contexto, setPropuesta(consultarPropuestas(contexto).registros.get(0)));
			return true;
		});

		return RespuestaMB.exito();
	}
	
	public static RespuestaMB aceptarPropuesta(ContextoMB contexto) {

		if(ConfigMB.esProduccion())
			return aceptarPropuestaV2(contexto);
		
		RespuestaMB respuesta = new RespuestaMB();
		String token = contexto.parametros.string("x-scope", null);

		String tokenMock = "porfavornomeretensequeessucioperoesporelbiendelapruebaunitariajajaja";

		if (!token.equals(tokenMock)) {
			return respuesta.setEstado("403");
		}

		canjearPropuestas(contexto);

		Objeto propuesta = setPropuesta(consultarPropuestas(contexto).registros.get(0));
		contexto.parametros.set("puntosacanjear", propuesta.integer("puntosacanjear"));
		MBBuhoPuntos.updatePuntosMock(contexto);

		new Futuro<>(() -> {
			RestPostventa.casoCRMPrisma(contexto, propuesta);
			return true;
		});

		return RespuestaMB.exito();

	}
	
	private static String idPath(String token) {
		String[] chunks = token.split("\\.");
		Base64.Decoder decoder = Base64.getUrlDecoder();
		String payload = new String(decoder.decode(chunks[1]));
		chunks = payload.split(":");
		String id = chunks[2].substring(0, chunks[2].indexOf(","));
		return id;
	}

	public static RespuestaMB getPropuestas(ContextoMB contexto) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
		LocalDateTime ahoraDateTime = LocalDateTime.now();
		LocalDate ahora = ahoraDateTime.toLocalDate();
		
		SqlResponseMB sqlResponse = consultarPropuestas(contexto);

		RespuestaMB buhoPuntos = MBBuhoPuntos.consolidada(contexto);

		Objeto propuestas = new Objeto();

		for (Objeto item : sqlResponse.registros) {
		
		    Objeto propuesta = setPropuesta(item);
		    String fechaStr = propuesta.string("fecha_de_expiracion");
		
		    LocalDateTime fechaVencimiento;
		    try {
		        fechaVencimiento = LocalDateTime.parse(fechaStr, formatter);
		    } catch (DateTimeParseException e) {
		        continue;
		    }
		
		    boolean noHaVencido = ahora.isBefore(fechaVencimiento.toLocalDate()) ||
		                          (ahora.isEqual(fechaVencimiento.toLocalDate()) && ahoraDateTime.isBefore(fechaVencimiento));
		
		    if (noHaVencido) {
		            long diasFaltantes = ChronoUnit.DAYS.between(ahora, fechaVencimiento.toLocalDate());
		            propuesta.set("dias_vencimiento", diasFaltantes);
		            if(propuestaCanjeable(propuesta, buhoPuntos.integer("puntos")))
		            	propuestas.add(propuesta);
		    }
		}
		
		if (propuestas.objetos().isEmpty()) {
			Objeto listaVacia = new Objeto();
			return RespuestaMB.exito("propuestas", listaVacia.objetos());
		}
		return RespuestaMB.exito("propuestas", propuestas);
	}
	
	private static SqlResponseMB insertarCashbackCanjeado(ContextoMB contexto, String idOperacion) {
		SqlRequestMB sqlRequest = SqlMB.request("CanjearCashback", "mobile");
		sqlRequest.configurarStoredProcedure(SP_EXEC_CANJEAR_CASHBACK,
				contexto.parametros.integer("id", null),
				new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()),
				contexto.idCobis(),
				idOperacion
				);
		return SqlMB.response(sqlRequest);
	}
	
	private static SqlResponseMB consultarCashbackCanjeado(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarCashback", "mobile");
		if(contexto.parametros.existe("id_operacion"))
			sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_CASHBACK_CANJEADO, null, contexto.parametros.string("id_operacion"));
		else
			sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_CASHBACK_CANJEADO, contexto.idCobis());
		
		return SqlMB.response(sqlRequest);
	}
	
	private static SqlResponseMB consultarCashbackIdCanje(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("ConsultarCashbackIdCanje", "mobile");

		sqlRequest.configurarStoredProcedure(SP_EXEC_OBTENER_CASHBACK_IDCANJE);
		
		return SqlMB.response(sqlRequest);
	}
	
	public static SqlResponseMB canjearPropuestas(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("CanjearPropuestas", "mobile");
		sqlRequest.configurarStoredProcedure(SP_EXEC_CANJEAR_PROPUESTA, contexto.parametros.integer("id", null), new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()));
		return SqlMB.response(sqlRequest);
	}

	public static SqlResponseMB consultarPropuestas(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectPropuestas", "mobile");
		if (contexto.parametros.integer("id", null) != null) {
			sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, null, contexto.parametros.integer("id", null), null);
		} else {
			sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, contexto.idCobis(), null, 'V');
		}
		
		return SqlMB.response(sqlRequest);
	}
	
	private static SqlResponseMB consultarPropuestasCanjeadas(ContextoMB contexto) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectPropuestas", "mobile");
		sqlRequest.configurarStoredProcedure(SP_EXEC_CONSULTA_PROPUESTA, contexto.idCobis(), null, 'C');
		
		return SqlMB.response(sqlRequest);
	}
	
	public static ApiResponseMB getPropuestaMobi(ContextoMB contexto, String token) {
		ApiRequestMB apiRequest = ApiMB.request("obtenerPropuesta", "productos", "GET", "/v1/propuesta/{id}", contexto);

		apiRequest.path("id", idPath(token));
		apiRequest.header("x-scope", token);

		return ApiMB.response(apiRequest);
	}

	public static boolean propuestaCanjeable(Objeto propuesta, Integer puntos) {
		return propuesta.integer("puntosacanjear") <= puntos;
	}

	public static Objeto setPropuestaHistorial(Objeto item) {
		Objeto propuesta = new Objeto();
		
		if(item.existe("id_cashback")) {
			propuesta.set("id", item.string("id_operacion"));
			String[] premio = ConfigMB.string("cashback_premio_" + item.integer("id_cashback")).split("_");
			propuesta.set("fecha_de_canje", item.string("fecha"));
			propuesta.set("monto_del_premio", premio[2]);
			propuesta.set("nombre_comercio", "Cashback");
			propuesta.set("puntosacanjear", premio[4]);
			propuesta.set("rubro", "Cashback");
			propuesta.set("nombre",  "Cashback");
			propuesta.set("es_cashback", true);
		}
		else {
			propuesta.set("id", item.integer("id"));
			propuesta.set("fecha_de_canje", item.string("fecha_de_canje"));
			propuesta.set("monto_del_premio", item.integer("monto_del_premio"));
			propuesta.set("nombre_comercio", item.string("nombre_comercio"));
			propuesta.set("puntosacanjear", item.string("puntosacanjear"));
			propuesta.set("rubro", item.string("rubro"));
			propuesta.set("nombre",  buildRubroNombreComercio(propuesta));
			propuesta.set("es_cashback", false);
		}

		return propuesta;
	}
	
	
	public static Objeto setPropuesta(Objeto item) {
		Objeto propuesta = new Objeto();
		propuesta.set("id", item.integer("id"));
		propuesta.set("estado", item.string("estado"));
		propuesta.set("fecha_de_compra", item.string("fecha_de_compra"));
		propuesta.set("fecha_de_expiracion", item.string("fecha_de_expiracion"));
		propuesta.set("fecha_de_canje", item.string("fecha_de_canje"));
		propuesta.set("id_cobis", item.string("id_cobis"));
		propuesta.set("monto_del_premio", item.integer("monto_del_premio"));
		propuesta.set("nombre_comercio", item.string("nombre_comercio"));
		propuesta.set("puntosacanjear", item.string("puntosacanjear"));
		propuesta.set("token", item.string("token"));
		propuesta.set("transaccion", item.string("transaccion"));
		propuesta.set("rubro", item.string("rubro"));
		propuesta.set("transaccion_id", item.integer("id").toString());
		propuesta.set("nombre", buildRubroNombreComercio(propuesta));

		return propuesta;
	}

	public static String buildRubroNombreComercio(Objeto propuesta) {
		if (!propuesta.string("rubro").isBlank() && !propuesta.string("nombre_comercio").isBlank()) {
			return propuesta.string("rubro") + " (" + propuesta.string("nombre_comercio") + ")";
		} else if (!propuesta.string("rubro").isBlank()) {
			return propuesta.string("rubro");
		} else {
			return propuesta.string("nombre_comercio");
		}
	}
	
	private static List<Objeto> consultarHistorial(ContextoMB contexto) {
		Futuro<SqlResponseMB> requestPwp = new Futuro<SqlResponseMB>(() -> consultarPropuestasCanjeadas(contexto));
		Futuro<SqlResponseMB> requestCashback = new Futuro<SqlResponseMB>(() -> consultarCashbackCanjeado(contexto));
		
		List<Objeto> registros;
		requestPwp.get();
		requestCashback.get();
		
		registros = requestPwp.get().registros;
		registros.addAll(requestCashback.get().registros);
		
		return registros;
	}
	
	public static RespuestaMB consultarHistorialPropuestas(ContextoMB contexto) {

		Objeto propuestas = new Objeto();
		
		List<Objeto> registros = consultarHistorial(contexto);
		
		for (Objeto item : registros) {

			Objeto propuesta = setPropuestaHistorial(item);


			propuestas.add(propuesta);

		}
		
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

		List<Objeto> sortedObjetos = propuestas.objetos().stream()
	            .sorted(Comparator.comparing(obj -> LocalDateTime.parse((CharSequence) ((Objeto)obj).get("fecha_de_canje"), formatter), Comparator.reverseOrder()))
	            .collect(Collectors.toList());

	
		Objeto listaVacia = new Objeto();
		return RespuestaMB.exito("propuestas", propuestas.toList() != null && propuestas.toList().size() > 0 ? sortedObjetos : listaVacia);
		
	}
	

	public static String consultaFechaCanje(ContextoMB contexto) {
		contexto.parametros.set("id", contexto.parametros.integer("idPropuesta", null));

		SqlResponseMB sql = consultarPropuestas(contexto);
		Objeto propuesta = setPropuesta(sql.registros.get(0));
		
		return propuesta.string("fecha_de_canje");
		
	}
	
	public static byte[] comprobantePropuesta(ContextoMB contexto) {
		String id = contexto.parametros.string("idPropuesta", "");
		String fecha = consultaFechaCanje(contexto);
		String puntosCanjeados = contexto.parametros.string("puntosCanjeados", "");
		String nombreComercio = contexto.parametros.string("nombreComercio", "Comercio no especificado");
		String montoPremio = contexto.parametros.string("montoPremio", "");

		String template = "buho_puntos_PWP";

		Map<String, String> parametros = new HashMap<>();
		parametros.put("PROPUESTA_ID", id);
		parametros.put("FECHA_HORA", fecha);
		parametros.put("MONTO_PREMIO", "$" + montoPremio);
		parametros.put("NOMBRE_COMERCIO", nombreComercio);
		parametros.put("PUNTOS_CANJEADOS", puntosCanjeados);
		parametros.put("CONCEPTO", "Buho Puntos por Pesos");

		return Pdf.generar(template, parametros);
	}

	public static byte[] comprobanteCashback(ContextoMB contexto) {

		SqlResponseMB resp = consultarCashbackCanjeado(contexto);
		
		Objeto cashback = resp.registros.get(0);
		
		String[] premio = ConfigMB.string("cashback_premio_" + cashback.string("id_cashback")).split("_");
		
		Map<String, String> parametros = new HashMap<>();
		String template = "";

		if(premio[3].equals("TC")) {
			parametros.put("NOMBRE_APELLIDO", contexto.persona().nombreCompleto());
			parametros.put("FECHA_HORA", cashback.string("fecha"));
			parametros.put("MONTO_PREMIO", "$" + premio[4]);
			parametros.put("PUNTOS_CANJEADOS", premio[2]);
			parametros.put("PROPUESTA_ID", contexto.parametros.string("id_operacion"));
			parametros.put("NUMERO_TC", contexto.tarjetaCreditoTitular().numero());
			template = "bp_cashback_TC";
		}
		else {
			parametros.put("PROPUESTA_ID", contexto.parametros.string("id_operacion"));
			parametros.put("FECHA_HORA", cashback.string("fecha"));
			parametros.put("MONTO_PREMIO", "$" + premio[4]);
			parametros.put("NOMBRE_COMERCIO", premio[1]);
			parametros.put("PUNTOS_CANJEADOS", premio[2]);
			parametros.put("CONCEPTO", "Buho Puntos por Pesos");
			template = "bp_cashback_TD";
		}

		contexto.responseHeader("Content-Type", "application/pdf; name=comprobante.pdf");

		return Pdf.generar(template, parametros);
	}
	
	public static RespuestaMB bajaPaquete(ContextoMB contexto) {
		String numeroPaquete = contexto.parametros.string("numeroPaquete");

		if (Objeto.anyEmpty(numeroPaquete)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		/*
		 * if (tieneSolicitudEnCurso(contexto, "BAJA_RECL", "PAQ", numeroPaquete)) {
		 * return Respuesta.estado("SOLICITUD_EN_CURSO"); }
		 */

		if (RestPostventa.tieneSolicitudEnCurso(contexto, "BAJA_PAQUETES", new Objeto().set("idPaquete", numeroPaquete), true)) {
			return RespuestaMB.estado("SOLICITUD_EN_CURSO");
		}

		RespuestaMB respuesta = new RespuestaMB();

		try {
			ApiResponseMB responseReclamo = RestPostventa.bajaPaquete(contexto, "BAJA_PAQUETES", numeroPaquete);

			if (responseReclamo == null || responseReclamo.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			SqlResponseMB sqlResponse = insertarReclamo(contexto, "BAJA_RECL", numeroPaquete, "PAQ", "", "");
			if (sqlResponse.hayError) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}

	}

	public static SqlResponseMB insertarReclamo(ContextoMB contexto, String tipoOperacion, String numeroProducto, String tipoProducto, String sucursal, String ttcc) {
		// --OBJETO DE SOLICITUD DE BAJA DE BBDD
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("InsertReclamo", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[solicitudProducto] ";
		sqlRequest.sql += "(";
		sqlRequest.sql += "sp_idCobis,";
		sqlRequest.sql += "sp_tipoOperacion,";
		sqlRequest.sql += "sp_fechaSolicitud,";
		sqlRequest.sql += "sp_tipoProducto,";
		sqlRequest.sql += "sp_numeroProducto,";
		sqlRequest.sql += "sp_sucursal,";
		sqlRequest.sql += "sp_apellido,";
		sqlRequest.sql += "sp_nombre,";
		sqlRequest.sql += "sp_sexo,";
		sqlRequest.sql += "sp_tipoDocumento,";
		sqlRequest.sql += "sp_numeroDocumento,";
		sqlRequest.sql += "sp_ttcc,";
		sqlRequest.sql += "sp_canal";
		sqlRequest.sql += ")";
		sqlRequest.sql += "values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(tipoOperacion);
		sqlRequest.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		sqlRequest.add(tipoProducto);
		sqlRequest.add(numeroProducto);
		sqlRequest.add(sucursal);
		sqlRequest.add(contexto.persona().apellido());
		sqlRequest.add(contexto.persona().nombre());
		sqlRequest.add(contexto.persona().idSexo());
		sqlRequest.add(contexto.persona().tipoDocumento());
		sqlRequest.add(contexto.persona().numeroDocumento());
		sqlRequest.add(ttcc);
		sqlRequest.add("MB");

		sqlResponse = SqlMB.response(sqlRequest);
		return sqlResponse;
	}

	public static boolean tieneSolicitudEnCurso(ContextoMB contexto, String tipoOperacion, String tipoProducto, String numeroProducto) {
		SqlResponseMB sqlResponse = consultarReclamo(contexto, tipoOperacion, tipoProducto, numeroProducto);
		if (sqlResponse.hayError) {
			throw new RuntimeException();
		}
		Calendar fechaVto = Calendar.getInstance();
		fechaVto.add(Calendar.DAY_OF_MONTH, -2); // la máxima cantidad de días para que una solicitud sea vigente es de 2. Esta
													// lógica se sacó de HB viejo.

		for (Objeto registro : sqlResponse.registros) {
			if (registro.date("sp_fechaSolicitud").compareTo(fechaVto.getTime()) > 0) {
				return true;
			}
		}
		return false;
	}

	public static SqlResponseMB consultarReclamo(ContextoMB contexto, String tipoOperacion, String tipoProducto, String numeroProducto) {

		SqlResponseMB sqlResponse = null;
		SqlRequestMB sqlRequest = SqlMB.request("SelectReclamo", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[solicitudProducto] ";
		sqlRequest.sql += "WHERE sp_idCobis = ? ";
		sqlRequest.sql += "AND sp_tipoOperacion = ? ";
		sqlRequest.sql += "AND sp_tipoProducto = ? ";
		sqlRequest.sql += "AND sp_numeroProducto = ? ";

		sqlRequest.add(contexto.idCobis());
		sqlRequest.add(tipoOperacion);
		sqlRequest.add(tipoProducto);
		sqlRequest.add(numeroProducto);

		sqlResponse = SqlMB.response(sqlRequest);

		return sqlResponse;
	}

	public static String obtenerSegmentoComercial(ContextoMB contexto) {
		ApiResponseMB acreditaciones = ProductosService.getAcreditaciones(contexto);

		if (!acreditaciones.hayError() && acreditaciones.objetos().size() > 0) {
			Objeto haberes = acreditaciones.objetos().get(0);
			if (haberes.get("segmentoComercial") != null) {
				return haberes.get("segmentoComercial").toString();
			}
		}
		return "";
	}

	public static boolean esPlanSueldoInactivo(ContextoMB contexto) {
		if ("PLAN SUELDO INACTIVO".equals(obtenerSegmentoComercial(contexto))) {
			return true;
		}
		return false;
	}

	public static RespuestaMB botonArrepentimiento(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("tieneboton", false);
		respuesta.set("tieneBotonBajaProductos", ConfigMB.bool("tieneBotonBajaProductos"));
		// Obtengo los productos del cliente
		ApiResponseMB productsResponse = ProductosService.productos(contexto);
		List<Objeto> productosList = new ArrayList<Objeto>();
		Objeto cuentas = (Objeto) productsResponse.get("cuentas");
		if (cuentas != null) {
			productosList.addAll(((Objeto) cuentas).objetos());
		}
		Objeto productos = (Objeto) productsResponse.get("productos");
		if (productos != null) {
			productosList.addAll((productos).objetos());
		}
		Objeto prestamos = (Objeto) productsResponse.get("prestamos");
		if (prestamos != null) {
			productosList.addAll((prestamos).objetos());
		}
		Objeto inversiones = (Objeto) productsResponse.get("inversiones");
		if (inversiones != null) {
			productosList.addAll((inversiones).objetos());
		}
		// List<Objeto> productos = ((Objeto)
		// productsResponse.get("productos")).objetos();
		// Calculo la fecha actual -10 dias para obtener la fecha limite
		Calendar cal = Calendar.getInstance();
		cal.add(Calendar.DATE, -10);
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		String limitDate = dateFormat.format(cal.getTime());
		// Recorro los productos del cliente y comparo la fecha de alta de cada uno con
		// la fecha limite
		for (Objeto producto : productosList) {
			String fechaAlta = (String) producto.get("fechaAlta");
			try {
				Date firstDate = dateFormat.parse(limitDate);
				Date secondDate = dateFormat.parse(fechaAlta);
				if (firstDate.before(secondDate)) {
					respuesta.set("tieneboton", true);
					break;
				}
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}

		respuesta.set("url", "https://www.hipotecario.com.ar/arrepentimiento/");
		respuesta.set("textoboton", "Si en los últimos 10 dias pediste algún producto nuevo (una cuenta, tarjeta, paquete o préstamo) podes arrepentirte y darlo de baja de manera sencilla.");
		return respuesta;
	}

	public static RespuestaMB bajaTarjetaCreditoAdicional(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		String idTarjeta = contexto.parametros.string("idTarjeta");

		if (Objeto.anyEmpty(idTarjeta)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);

		try {

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "BAJATC", new Objeto().set("tcNumero", tarjetaCredito.numero()), true)) {
				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje", ConfigMB.string("baja_tc_mensaje_solicitud_en_curso"));
				respuesta.set("habilitaBaja", false);
				return respuesta;
			}

			String estado = tarjetaCredito.idEstado();
			if (!tarjetaCredito.esTitular() && (estado.equals("25") || estado.equals("20"))) {

				RespuestaMB caso = generarCasoBajaTC(contexto, tarjetaCredito);
				if (caso.hayError()) {
					return caso;
				}

				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje", messageBajaTc(contexto.persona().nombreCompleto(), caso.string("numeroCaso"), true));
				return respuesta;

			} else {
				// Otros estados o casos no contemplados
				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje", ConfigMB.string("baja_tc_mensaje_call_center"));
				return respuesta;
			}
		} catch (Exception e) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}
	}

	private static String messageBajaTc(String apeNom, String numeroCaso, Boolean isShortMessage) {
		String msgBaja = "Estimado/a {nombre},\n¡Ya recibimos tu contacto! Tu solicitud se registr&oacute; bajo el n&uacute;mero {num}.\n";

		if (!isShortMessage) {
			msgBaja += "Nos vamos a comunicar con vos a la brevedad para dar curso a la gesti&oacute;n que solicitaste.\n¡Gracias!";
		}

		msgBaja = msgBaja.replace("{nombre}", apeNom).replace("{num}", numeroCaso);
		return msgBaja;
	}

	private static RespuestaMB generarCasoBajaTC(ContextoMB contexto, TarjetaCredito tarjetaCredito) {
		RespuestaMB respuesta = new RespuestaMB();
		ApiResponseMB responseReclamo = null;
		try {
			responseReclamo = RestPostventa.bajaTarjetaCreditoAdicional(contexto, tarjetaCredito);

			if (responseReclamo == null || responseReclamo.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamo);

			if (numeroCaso.isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			respuesta.set("numeroCaso", numeroCaso);

			SqlResponseMB sqlResponse = insertarReclamo(contexto, "BAJA_RECL", tarjetaCredito.numero(), "ATC", tarjetaCredito.sucursal(), "");
			if (sqlResponse.hayError) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}
	}

	protected static Boolean tieneSolicitudBajaCurso(ContextoMB contexto, String numero) {
		if (MBProducto.tieneSolicitudEnCurso(contexto, "BAJA", "ATC", numero)) {
			return true;
		}
		if (MBProducto.tieneSolicitudEnCurso(contexto, "BAJA_RECL", "ATC", numero)) {
			return true;
		}
		return false;
	}

	protected static RespuestaMB tieneCuentasPendientes(ContextoMB contexto, String cuenta, String numero) {
		try {
			ApiResponseMB response = TarjetaCreditoService.cuotasPendientes(contexto, cuenta, numero);
			RespuestaMB respuesta = new RespuestaMB();
			boolean error404cuotasPendientes = false;
			respuesta.set("tieneCuotas", false);
			if (response.hayError()) {
				if (response.string("codigo").equals("404") || response.string("codigo").equals("112107")) {
					error404cuotasPendientes = true;
				} else {
					return RespuestaMB.estado("ERROR_CONSULTA_CUOTAS_PENDIENTES");
				}
			}
			if (!error404cuotasPendientes) {
				if (respuesta.set("cuotasPendientes") != null && !response.objetos("cuotasPendientes.tarjeta").isEmpty()) {
					respuesta.set("tieneCuotas", true);
				}
			}
			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static RespuestaMB generarCasoBajaTC(ContextoMB contexto, TarjetaCredito tarjetaCredito, String tipi) {
		RespuestaMB respuesta = new RespuestaMB();
		try {
			ApiResponseMB responseReclamo;

			switch (tipi) {
			case "BAJATC_PEDIDO":
				responseReclamo = RestPostventa.bajaTarjetaCredito(contexto, tipi, tarjetaCredito);
				break;
			case "BAJA_TC_ADICIONAL_PEDIDO":
				responseReclamo = RestPostventa.bajaTarjetaCreditoAdicional(contexto, tipi, tarjetaCredito);
				break;
			case "BAJA_PAQUETES":
				responseReclamo = RestPostventa.bajaPaquete(contexto, tipi, tarjetaCredito.idPaquete());
				break;
			default:
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			if (responseReclamo == null || responseReclamo.hayError()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamo);

			if (numeroCaso.isEmpty()) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			try {
				respuesta.set("numeroCaso", numeroCaso.replace("CAS-", ""));
			} catch (Exception e) {
				respuesta.set("numeroCaso", numeroCaso);
			}

			SqlResponseMB sqlResponse = insertarReclamo(contexto, "BAJA_RECL", tarjetaCredito.numero(), "ATC", tarjetaCredito.sucursal(), "");
			if (sqlResponse.hayError) {
				return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta;

		} catch (Exception e) {
			return RespuestaMB.estado("ERROR_GENERANDO_RECLAMO");
		}
	}

	public static boolean tieneSolicitudesEnCurso(String cobisId, String[] tipoOperacion, String tipoProducto, String numeroProducto, String fechaMinimaSolicitud) {
		SqlResponseMB sqlResponse = consultarSolicitudProducto(cobisId, tipoOperacion, tipoProducto, numeroProducto, fechaMinimaSolicitud);
		boolean tieneSolicitudesEnCurso = false;
		if (sqlResponse.hayError) {
			throw new RuntimeException();
		}
		if (!sqlResponse.registros.isEmpty()) {
			tieneSolicitudesEnCurso = true;
		}
		return tieneSolicitudesEnCurso;
	}

	public static SqlResponseMB consultarSolicitudProducto(String cobisId, String[] tipoOperacion, String tipoProducto, String numeroProducto, String fechaMinimaSolicitud) {
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("SelectSolicitudProducto", "homebanking");
		sqlRequest.sql = "SELECT * FROM [Homebanking].[dbo].[solicitudProducto] ";
		sqlRequest.sql += "WHERE sp_idCobis = ? ";
		sqlRequest.sql += "AND sp_tipoOperacion in( " + convertArrayToStringSeparatedByCommas(tipoOperacion) + ") ";
		sqlRequest.sql += "AND sp_tipoProducto = ? ";
		sqlRequest.sql += "AND sp_numeroProducto = ? ";
		sqlRequest.sql += "AND sp_fechaSolicitud >= ? ";

		sqlRequest.add(cobisId);
		sqlRequest.add(tipoProducto);
		sqlRequest.add(numeroProducto);
		sqlRequest.add(fechaMinimaSolicitud);

		sqlResponse = SqlMB.response(sqlRequest);
		return sqlResponse;
	}

	private static String convertArrayToStringSeparatedByCommas(String[] name) {
		StringBuilder sb = new StringBuilder();
		for (String st : name) {
			sb.append('\'').append(st).append('\'').append(',');
		}
		if (name.length != 0)
			sb.deleteCharAt(sb.length() - 1);
		return sb.toString();
	}

	protected static Objeto validacion(String codigo, boolean ok) {
		Objeto ret = new Objeto();
		ret.set("condicion", codigo);
		ret.set("desc", mensajesValidacionBajas.containsKey(codigo) ? mensajesValidacionBajas.get(codigo) : "");
		ret.set("ok", ok);
		return ret;
	}

	public static Objeto reorganizaCuentas(List<Objeto> cuentas, Objeto adelantoExistente) {
		Objeto cuentasReorganizadas = new Objeto();
		Boolean agregada = false;
		for (Objeto cuenta : cuentas) {
			if (seIntegra(adelantoExistente, cuenta, agregada)) {
				cuenta.set("adelanto", adelantoExistente);
				agregada = true;
			}
			cuentasReorganizadas.add(cuenta);
		}
		return cuentasReorganizadas;
	}

	private static boolean seIntegra(Objeto adelantoExistente, Objeto cuenta, Boolean agregada) {
		return (adelantoExistente.existe("id") && ("Adelanto BH".equalsIgnoreCase(adelantoExistente.string("descripcion")) || "ADE".equalsIgnoreCase(adelantoExistente.string("descripcionCorta"))) && !agregada && ("Caja de Ahorro".equalsIgnoreCase(cuenta.string("descripcion")) || "AHO".equalsIgnoreCase(cuenta.string("descripcionCorta")) && cuenta.string("adelantoCuentaAsociada").contentEquals(adelantoExistente.string("numeroProducto"))));
	}

	public static Boolean acreditacionesHaberes(ContextoMB contexto) {
		ApiResponseMB acreditaciones = ProductosService.getAcreditaciones(contexto);

		if (acreditaciones.objetos().size() > 0) {
			Integer mesActual = LocalDateTime.now().getDayOfMonth() > 10 ? LocalDateTime.now().getMonthValue() : LocalDateTime.now().getDayOfMonth() - 1;
			String ultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual);
			String penultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual - 1);
			String antPenultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual - 2);

			Objeto haberes = acreditaciones.objetos().get(0);
			if (haberes.get("acreditacionesPeriodo1") != null && haberes.get("acreditacionesPeriodo2") != null && haberes.get("acreditacionesPeriodo3") != null && haberes.get("acreditacionesPeriodo1") != ultimaFecha && haberes.get("acreditacionesPeriodo2") != penultimaFecha && haberes.get("acreditacionesPeriodo3") != antPenultimaFecha && haberes.get("acreditacionesMonto1") != null && haberes.get("acreditacionesMonto2") != null && haberes.get("acreditacionesMonto3") != null) {
				return true;
			}
		}
		return false;
	}

	public static List<ProductoMora> getProductosEnMora(ContextoMB contexto) {
		List<ProductoMora> productosEnMora = new ArrayList<>();
//		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
		ApiResponseMB responseProductosMora = RestMora.getProductosEnMora(contexto);
		if (responseProductosMora.codigo != 204 && !responseProductosMora.hayError()) {
			for (Objeto item : responseProductosMora.objetos()) {
				ApiResponseMB responseDetalle = RestMora.getProductosEnMoraDetalles(contexto, item.string("cta_id"));
				item.set("diasMora", responseDetalle.objetos().get(0).integer("DiasMora"));
				ProductoMora productoEnMora = new ProductoMora(item);
				productosEnMora.add(productoEnMora);
			}
		}
//		}

		return productosEnMora;
	}

	public static void verificarEstadoMoraTemprana(ContextoMB contexto, List<ProductoMora> productosEnMora, List<ProductoMoraDetalles> productoMoraDetalles, Prestamo prestamo, Objeto item, Boolean checkCuotas) {
		Boolean onboardingMostrado = true;
		Boolean enMora = false;
		if (productosEnMora.size() > 0 && productosEnMora.stream().anyMatch(prod -> prod.numeroProducto().equals(prestamo.numero()) && Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora()) && CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim()))) {
			ProductoMora productoMora = productosEnMora.stream().filter(prod -> prod.numeroProducto().equals(prestamo.numero())).findFirst().get();
			item.set("estadoMora", "EN_MORAT");
			onboardingMostrado = false;
			enMora = true;
			if (productoMoraDetalles.size() > 0) {
				ProductoMoraDetalles detalle = productoMoraDetalles.stream().filter(prod -> prod.ctaId().equals(productoMora.ctaId())).findFirst().get();
				item.set("inicioMora", detalle.inicioMora());
				item.set("deudaVencida", detalle.deudaVencida());
				item.set("diasEnMora", detalle.diasEnMora());
				item.set("ctaId", detalle.ctaId());
				item.set("promesaVigente", detalle.promesaVigente());
				item.set("AvisoPago", detalle.yaPague());
				item.set("deudaAVencer", detalle.deudaAVencer());
				item.set("montoMinPromesa", detalle.montoMinPromesa());
				item.set("montoMaxPromesa", detalle.deudaVencida());
				onboardingMostrado = !detalle.promesaVigente().isEmpty() || !detalle.yaPague().isEmpty();
			}
		} else if (checkCuotas){
			if (!FORMA_PAGO_EFECTIVO.equals(prestamo.formaPago()) && prestamo.cuotas().stream().anyMatch(cuota -> CUOTA_VENCIDA.equals(cuota.estado()))) {
				item.set("estadoMora", "EN_MORAT");
			}
		}
		verificarEstadoMoraTempranaBucket(contexto, productosEnMora, prestamo, item);
		Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
		item.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

		try {
			if (!muestreoOnboarding && onboardingMostrado && enMora) {
				contexto.parametros.set("nemonico", "ONBOARDING");
				Util.contador(contexto);
			}
		} catch (Exception e) {
		}

	}

	public static void verificarEstadoMoraTempranaBucket(ContextoMB contexto, List<ProductoMora> productosEnMora, Prestamo prestamo, Objeto item) {

		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "mb_prendido_promesa_pago")) {
			for (ProductoMora prod : productosEnMora) {
				if (prod.numeroProducto().equals(prestamo.numero()) && Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora()) && CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim())) {
					if (Arrays.asList(BUCKETS).contains(Util.bucketMora(prod.diasMora(), prestamo.categoria()))) {
						item.set("estadoMora", "EN_MORAT" + "_" + Util.bucketMora(prod.diasMora(), prestamo.categoria()));
					}
				}
			}
		}
	}

	public static ProductoMoraDetalles getProductosEnMoraDetalles(ContextoMB contexto, String ctaId) {
//		if (MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
		ApiResponseMB responseProductosMoraDetalles = RestMora.getProductosEnMoraDetallesNoCache(contexto, ctaId);
		if (responseProductosMoraDetalles.codigo != 204 && !responseProductosMoraDetalles.hayError() && responseProductosMoraDetalles.objetos().size() > 0) {
			return new ProductoMoraDetalles(responseProductosMoraDetalles.objetos().get(0));
		}
//		}
		return null;
	}

	public static SqlResponseMB AltaCuentaEspecial(ContextoMB contexto) {
		SqlResponseMB sqlResponse;
		SqlRequestMB sqlRequest = SqlMB.request("InsertReclamo", "homebanking");
		sqlRequest.sql = "INSERT INTO [Homebanking].[dbo].[solicitudProducto] ";
		sqlRequest.sql += "(";
		sqlRequest.sql += "sp_idCobis,";
		sqlRequest.sql += "sp_tipoOperacion,";
		sqlRequest.sql += "sp_fechaSolicitud,";
		sqlRequest.sql += "sp_tipoProducto,";
		sqlRequest.sql += "sp_apellido,";
		sqlRequest.sql += "sp_nombre,";
		sqlRequest.sql += "sp_sexo,";
		sqlRequest.sql += "sp_tipoDocumento,";
		sqlRequest.sql += "sp_numeroDocumento,";
		sqlRequest.sql += "sp_canal";
		sqlRequest.sql += ")";
		sqlRequest.sql += "values(?,?,?,?,?,?,?,?,?,?)";

		sqlRequest.add(contexto.idCobis());
		sqlRequest.add("ALTA");
		sqlRequest.add(new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
		sqlRequest.add("CRA");
		sqlRequest.add(contexto.persona().apellido().toUpperCase());
		sqlRequest.add(contexto.persona().nombre().toUpperCase());
		sqlRequest.add(contexto.persona().idSexo());
		sqlRequest.add(contexto.persona().idTipoDocumento());
		sqlRequest.add(contexto.persona().numeroDocumento());
		sqlRequest.add("MB");

		sqlResponse = SqlMB.response(sqlRequest);
		return sqlResponse;
	}


}
