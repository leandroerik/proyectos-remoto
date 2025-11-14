package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.productos.PrestamosV4;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.*;
import ar.com.hipotecario.canal.homebanking.servicio.CuentasService;
import ar.com.hipotecario.canal.homebanking.servicio.InversionesService;
import ar.com.hipotecario.canal.homebanking.servicio.PagoServicioService;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCajaSeguridad;
import ar.com.hipotecario.canal.homebanking.servicio.RestDebin;
import ar.com.hipotecario.canal.homebanking.servicio.RestMora;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.TarjetaCreditoService;

public class HBProducto {

	public static final String CODIGO_PRODUCTO_PRESTAMO = "7";
	public static final String[] TIPOS_MORA_TEMPRANA = { "ONE", "MT" };
	public static final String CUOTA_VENCIDA = "Vencida";
	public static final String FORMA_PAGO_EFECTIVO = "Efectivo";
	public static final String[] BUCKETS = { "B2", "B3" };
	
	public static Long logTiempo(Long tiempo, String marca) {
		Boolean activado = false;
		if (activado) {
			System.out.println("[+] " + marca + ": " + (System.currentTimeMillis() - tiempo) + " ms");
		}
		return System.currentTimeMillis();
	}

	public static Respuesta productos(ContextoHB contexto) {
		Objeto productos = new Objeto();

		long tiempo = System.currentTimeMillis();

		if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
			return contexto.productosV4(contexto);
		}

		ApiResponse response = ProductosService.productos(contexto);
		if (response.hayError()) {
			return Respuesta.error();
		}

		tiempo = logTiempo(tiempo, "A");

		List<TarjetaCredito> tarjetasCredito = contexto.tarjetasCreditoTitularConAdicionalesTercero();

		// LLAMADAS EN PARALELO
		Futuro<List<ProductoMora>> futuroProductosEnMora = new Futuro<>(() -> getProductosEnMora(contexto));
		Map<String, Futuro<ApiResponse>> futuroDetalleTC = new HashMap<String, Futuro<ApiResponse>>();
		for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
			Futuro<ApiResponse> futuro = new Futuro<>(
					() -> TarjetaCreditoService.consultaTarjetaCreditoCache(contexto, tarjetaCredito.numero()));
			futuroDetalleTC.put(tarjetaCredito.numero(), futuro);
		}

		Map<String, Futuro<Objeto>> futuroResumenTC = new HashMap<String, Futuro<Objeto>>();

		for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
			Futuro<Objeto> futuro = new Futuro<>(
					() -> HBTarjetas.fechasCierreVtoTarjetaCredito(contexto, tarjetaCredito, null));
			futuroResumenTC.put(tarjetaCredito.numero(), futuro);
		}

		futuroProductosEnMora.tryGet();
		for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
			futuroDetalleTC.get(tarjetaCredito.numero()).tryGet();
			futuroResumenTC.get(tarjetaCredito.numero()).tryGet();
		}

		// FIN LLAMADAS EN PARALELO

		tiempo = logTiempo(tiempo, "B");

		BigDecimal cuentasSaldoPesos = new BigDecimal(0.0);
		BigDecimal cuentasSaldoDolares = new BigDecimal(0.0);
		Objeto productosPaquete = new Objeto();
		Boolean mostrarArrepentimiento = false;
		int cantidadDiasArrepentimiento = 10;
		Objeto adelantoExistente = new Objeto();

		tiempo = logTiempo(tiempo, "C");

		for (Cuenta cuenta : contexto.cuentas()) {
			String estadoTd = "";

			if (ConfigHB.bool("prendido_link_td_habilitada",false)) {
				try {
					estadoTd = contexto.tarjetaDebitoAsociada(cuenta).estadoLink();
				}catch (Exception ignored){	}
			}

			Objeto item = new Objeto();
			//item.set("id", cuenta.id());
			item.set("id", cuenta.idEncriptado());
			item.set("tdInactiva", estadoTd.equalsIgnoreCase("INACTIVA"));
			item.set("descripcion", cuenta.producto());
			item.set("numero", cuenta.numero());
			item.set("numeroFormateado", cuenta.numeroFormateado());
			item.set("numeroEnmascarado", cuenta.numeroEnmascarado());
			item.set("descripcionCorta", cuenta.descripcionCorta());
			item.set("titularidad", cuenta.titularidad());
			item.set("moneda", cuenta.moneda());
			item.set("simboloMoneda", cuenta.simboloMoneda());
			item.set("estado", cuenta.descripcionEstado());
			item.set("saldo", cuenta.saldo());
			item.set("saldoFormateado", cuenta.saldoFormateado());
			item.set("acuerdo", cuenta.acuerdo());
			item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
			item.set("disponible",
					cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
			item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
			item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
			if (cuenta.fechaAltaDate() != null && "AHO".equals(cuenta.idTipo())) {
				item.set("sePuedeArrepentir",
						Fecha.cantidadDias(cuenta.fechaAltaDate(), new Date()) <= cantidadDiasArrepentimiento);
				mostrarArrepentimiento = mostrarArrepentimiento
						|| Fecha.cantidadDias(cuenta.fechaAltaDate(), new Date()) <= cantidadDiasArrepentimiento;
			}
			if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
				item.set("adelantoDisponible", cuenta.adelantoDisponible());
				item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
				item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
				item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
				item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
				item.set("adelantoInteresesDevengadosFormateado",
						Formateador.importe(cuenta.adelantoInteresesDevengados()));
				adelantoExistente = item;
			} else {
				productos.add("cuentas", item);
			}

			cuentasSaldoPesos = "$".equals(cuenta.simboloMoneda()) ? cuentasSaldoPesos.add(cuenta.saldo())
					: cuentasSaldoPesos;
			cuentasSaldoDolares = "USD".equals(cuenta.simboloMoneda()) ? cuentasSaldoDolares.add(cuenta.saldo())
					: cuentasSaldoDolares;

			if (cuenta.idPaquete() != null && !"".equals(cuenta.idPaquete())) {
				if (!"80".equals(cuenta.idMoneda()) || cuenta.esCuentaCorriente()) {
					Objeto productoPaquete = new Objeto();
					//productoPaquete.set("id", cuenta.id());
					productoPaquete.set("id", cuenta.idEncriptado());
					productoPaquete.set("titulo", cuenta.producto());
					productoPaquete.set("descripcion", cuenta.simboloMoneda() + " " + cuenta.numeroEnmascarado());
					productosPaquete.add(productoPaquete);
				}
			}
		}
		productos.set("cuentas", reorganizaCuentas(productos.objetos("cuentas"), adelantoExistente));

		tiempo = logTiempo(tiempo, "D");

		for (TarjetaDebito tarjetaDebito : contexto.tarjetasDebito()) {
			if (tarjetaDebito.activacionTemprana()) {
				continue;
			}

			Objeto item = new Objeto();
			//item.set("id", tarjetaDebito.id());
			item.set("id", tarjetaDebito.idEncriptado());
			item.set("descripcion", tarjetaDebito.producto());
			item.set("ultimos4digitos", tarjetaDebito.ultimos4digitos());
			item.set("titularidad", tarjetaDebito.titularidad());
			item.set("virtual", tarjetaDebito.virtual());
			if (!ConfigHB.esOpenShift()) {
				item.set("numero", tarjetaDebito.numero());
			}
			productos.add("tarjetasDebito", item);

			if (tarjetaDebito.idPaquete() != null && !"".equals(tarjetaDebito.idPaquete())) {
				Objeto productoPaquete = new Objeto();
				//productoPaquete.set("id", tarjetaDebito.id());
				productoPaquete.set("id", tarjetaDebito.idEncriptado());
				productoPaquete.set("titulo", "Tarjeta de Débito");
				productoPaquete.set("descripcion", "VISA XXXX-" + tarjetaDebito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}
		}

		tiempo = logTiempo(tiempo, "E");

		for (TarjetaCredito tarjetaCredito : tarjetasCredito) {
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
			item.set("cuenta", tarjetaCredito.cuenta());

			try {
				futuroDetalleTC.get(tarjetaCredito.numero());
				item.set("stopDebit", tarjetaCredito.stopDebit());
				item.set("adheridoResumenElectronico", tarjetaCredito.adheridoResumenElectronico());
			} catch (Exception e) {
				item.set("stopDebit", "NO");
				item.set("adheridoResumenElectronico", true);
			}

			String vencimiento;
			String cierre;

			if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendidoLogicaFechaTC",
					"prendidoLogicaFechaTC_cobis")) { // validacion logica fechas
				Objeto fechaTC = futuroResumenTC.get(tarjetaCredito.numero()).get();
				vencimiento = fechaTC.string("vencimiento");
				cierre = fechaTC.string("cierre");
			} else {
				cierre = tarjetaCredito.fechaCierre("dd/MM/yyyy");
				vencimiento = tarjetaCredito.fechaVencimiento("dd/MM/yyyy");
			}
			item.set("fechaCierre", cierre);
			item.set("fechaVencimiento", vencimiento);
			item.set("fechaProximoCierre", tarjetaCredito.fechaProximoCierre("dd/MM/yyyy"));
			item.set("fechaAlta", tarjetaCredito.fechaAlta("dd/MM/yyyy"));
			item.set("formaPago", tarjetaCredito.formaPago());
			item.set("esHml", tarjetaCredito.esHML());
			if (!ConfigHB.esOpenShift()) {
				item.set("numero", tarjetaCredito.idEncriptado());
				item.set("cuenta", tarjetaCredito.cuenta());
			}
			if (tarjetaCredito.fechaAltaDate() != null) {
				item.set("sePuedeArrepentir",
						Fecha.cantidadDias(tarjetaCredito.fechaAltaDate(), new Date()) <= cantidadDiasArrepentimiento);
				mostrarArrepentimiento = mostrarArrepentimiento || Fecha.cantidadDias(tarjetaCredito.fechaAltaDate(),
						new Date()) <= cantidadDiasArrepentimiento;
			}
			item.set("altaPuntoVenta", tarjetaCredito.altaPuntoVenta());

			productos.add("tarjetasCredito", item);

			if (tarjetaCredito.idPaquete() != null && !"".equals(tarjetaCredito.idPaquete())
					&& tarjetaCredito.esTitular()) {
				Objeto productoPaquete = new Objeto();
				productoPaquete.set("id", tarjetaCredito.idEncriptado());
				productoPaquete.set("titulo", "Tarjeta de Crédito");
				productoPaquete.set("descripcion",
						tarjetaCredito.tipo() + " " + "XXXX-" + tarjetaCredito.ultimos4digitos());
				productosPaquete.add(productoPaquete);
			}
		}

		tiempo = logTiempo(tiempo, "F");

		Objeto objetosPlazosFijos = new Objeto();
		for (PlazoFijo plazoFijo : contexto.plazosFijos()) {
			if(plazoFijo.esCedip()) {
				continue;
			}
			
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
			objetosPlazosFijos.add(item);
		}
		productos.set("plazosFijos", objetosPlazosFijos.ordenar("orden"));

		tiempo = logTiempo(tiempo, "G");

		List<ProductoMora> productosEnMora = futuroProductosEnMora.get();
		List<ProductoMoraDetalles> productosEnMoraDetalles = new ArrayList<>();
		productosEnMora.forEach(productoMora -> {
			ProductoMoraDetalles item = getProductosEnMoraDetalles(contexto, productoMora.ctaId());
			if (Objects.nonNull(item)) {
				productosEnMoraDetalles.add(item);
			}
		});

		tiempo = logTiempo(tiempo, "H");

		Objeto consolidadoOnboardings = new Objeto();
		try {
			consolidadoOnboardings.set("onboardingMoraMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING_MORA"));
			consolidadoOnboardings.set("onboardingPromesaMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING"));
			consolidadoOnboardings.set("onboardingFciMostrado", Util.tieneMuestreoNemonico(contexto, "ONBOARDING_FCI"));
		} catch (Exception e) {
			consolidadoOnboardings.set("onboardingMoraMostrado", true);
			consolidadoOnboardings.set("onboardingPromesaMostrado", true);
			consolidadoOnboardings.set("onboardingFciMostrado", true);
		}

		productos.set("consolidadoOnboarding", consolidadoOnboardings);

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
				item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
				item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
				item.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
				item.set("ultimos4digitosCuenta", prestamo.ultimos4digitos());
				item.set("fechaAlta", prestamo.fechaAlta("dd/MM/yy"));
				item.set("fechaProximoVencimiento", prestamo.fechaProximoVencimiento("dd/MM/yy"));
				item.set("montoAprobado", prestamo.montoAprobado());
				item.set("montoAprobadoFormateado", prestamo.montoAprobadoFormateado());

				item.set("idEstado", prestamo.consolidada().string("estado"));
				item.set("estado", prestamo.consolidada().string("descEstado"));
				item.set("subtipo", prestamo.consolidada().string("codigoProducto"));
				item.set("fechaProximoVencimiento",
						prestamo.consolidada().date("fechaProxVencimiento", "yyyy-MM-dd", "dd/MM/yyyy"));
				item.set("esProcrear", prestamo.consolidada().string("esProCrear").equals("S"));
				item.set("montoCuotaActual", prestamo.consolidada().bigDecimal("montoCuotaActual"));
				item.set("cantidadCuotas", prestamo.consolidada().integer("plazoOriginal"));
				item.set("numeroProducto", prestamo.consolidada().string("numeroProducto"));

				if (prestamo.cuotaActual() != null && prestamo.cuotaActual() >= 0) {
					item.set("cuotaActual", prestamo.cuotaActual());
				} else {
					item.set("cuotaActual", "-");
				}

				if (prestamo.consolidada().date("fechaAlta", "yyyy-MM-dd") != null) {
					item.set("sePuedeArrepentir",
							Fecha.cantidadDias(prestamo.consolidada().date("fechaAlta", "yyyy-MM-dd"),
									new Date()) <= cantidadDiasArrepentimiento);
					mostrarArrepentimiento = mostrarArrepentimiento
							|| Fecha.cantidadDias(prestamo.consolidada().date("fechaAlta", "yyyy-MM-dd"),
									new Date()) <= cantidadDiasArrepentimiento;
				}

				verificarEstadoMoraTemprana(contexto, productosEnMora, productosEnMoraDetalles, prestamo, item);
				if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
					try {
						HBPrestamo.obtenerProximaCuota(contexto, prestamo, item);
					} catch (Exception e) {
					}

				}

				try {
					Integer cantidadCuotasPagadas = prestamo.cuotas().stream().filter(cuota -> HBPrestamo.ESTADO_CUOTA_CANCELADO.equals(cuota.estado())).collect(Collectors.toList()).size();
					item.set("cantidadCuotasPagadas", !prestamo.enConstruccion() ? cantidadCuotasPagadas : 0);
				} catch (Exception e) {
					item.set("cantidadCuotasPagadas", 0);
				}

				item.set("cantidadCuotasPendientes", !prestamo.enConstruccion() ? prestamo.cuotasPendientes() : prestamo.cantidadCuotas());
				item.set("cantidadCuotasVencidas", prestamo.cuotasVencidas());
				item.set("codigo", prestamo.codigo());
				item.set("enConstruccion", prestamo.enConstruccion());
				item.set("habilitaMenuCambioFP", prestamo.habilitadoCambioFormaPago());
				item.set("habilitaMenuPago", prestamo.habilitadoMenuPago());
				item.set("idMoneda", prestamo.idMoneda());
				item.set("idTipoProducto", prestamo.idTipo());
				item.set("montoAdeudado", prestamo.codigo().equals("PPADELANTO") ? prestamo.montoUltimaCuotaFormateado() : prestamo.montoAdeudadoFormateado());
				item.set("mostrarLinkDecreto767", false);
				item.set("ocultarMontoAdeudado", HBPrestamo.esProcrear(contexto, prestamo.codigo()));
				item.set("pagable", prestamo.codigo().equals("PPADELANTO"));
				item.set("porcentajeFechaProximoVencimiento", Fecha.porcentajeTranscurrido(31L, prestamo.fechaProximoVencimiento()));
				item.set("saldoActual", prestamo.montoUltimaCuotaFormateado());
				item.set("tipoFormaPagoActual", prestamo.debitoAutomatico() || prestamo.idFormaPago().equals("DTCMN") ? "AUTOMATIC_DEBIT" : "CASH");
				item.set("urlDecreto767", ConfigHB.string("url_decreto_767", ""));

				productos.add("prestamos", item);
			}
		}

		tiempo = logTiempo(tiempo, "I");

		for (CuentaComitente cuentaComitente : contexto.cuentasComitentes()) {
			Objeto item = new Objeto();
			item.set("id", cuentaComitente.id());
			item.set("descripcion", cuentaComitente.producto());
			item.set("numero", cuentaComitente.numero());
			item.set("titularidad", cuentaComitente.titularidad());
			if (cuentaComitente.fechaAlta() != null) {
				item.set("sePuedeArrepentir",
						Fecha.cantidadDias(cuentaComitente.fechaAlta(), new Date()) <= cantidadDiasArrepentimiento);
				mostrarArrepentimiento = mostrarArrepentimiento
						|| Fecha.cantidadDias(cuentaComitente.fechaAlta(), new Date()) <= cantidadDiasArrepentimiento;
			}
			productos.add("cuentasComitentes", item);
		}

		tiempo = logTiempo(tiempo, "J");

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

		tiempo = logTiempo(tiempo, "K");

		for (Objeto objeto : response.objetos("productos")) {
			if ("PAQ".equals(objeto.string("tipo"))) {
				Objeto item = new Objeto();
				String codigoPaquete = objeto.string("codigoPaquete");
				item.set("id", codigoPaquete);
				item.set("descPaquete", Paquete.mapaDescripciones().get(objeto.string("codigoPaquete")));
				item.set("productos", productosPaquete);

				boolean mostrarLeyenda = true;

				if (contexto.persona().esEmpleado() || "34".equals(codigoPaquete) || "35".equals(codigoPaquete)
						|| "36".equals(codigoPaquete) || "37".equals(codigoPaquete) || "38".equals(codigoPaquete)) {
					mostrarLeyenda = false;
				}
				item.set("mostrarLeyenda", mostrarLeyenda);
				item.set("fechaAlta", objeto.date("FechaAlta", "yyyy-MM-dd", "dd/MM/yyyy"));
				if (objeto.date("fechaAlta", "yyyy-MM-dd") != null) {
					item.set("sePuedeArrepentir", Fecha.cantidadDias(objeto.date("fechaAlta", "yyyy-MM-dd"),
							new Date()) <= cantidadDiasArrepentimiento);
					mostrarArrepentimiento = mostrarArrepentimiento
							|| Fecha.cantidadDias(objeto.date("fechaAlta", "yyyy-MM-dd"),
									new Date()) <= cantidadDiasArrepentimiento;
				}
				productos.add("paquetes", item);
			}
			if (objeto.string("tipo").equals("RJA")) {
				Objeto item = new Objeto();
				if (objeto.date("fechaAlta", "yyyy-MM-dd") != null) {
					item.set("sePuedeArrepentir", Fecha.cantidadDias(objeto.date("fechaAlta", "yyyy-MM-dd"),
							new Date()) <= cantidadDiasArrepentimiento);
					mostrarArrepentimiento = mostrarArrepentimiento
							|| Fecha.cantidadDias(objeto.date("fechaAlta", "yyyy-MM-dd"),
									new Date()) <= cantidadDiasArrepentimiento;
				}
			}
		}

		tiempo = logTiempo(tiempo, "L");

		Objeto consolidadoSaldos = new Objeto();
		consolidadoSaldos.set("cuentasSaldoPesos", cuentasSaldoPesos);
		consolidadoSaldos.set("cuentasSaldoDolares", cuentasSaldoDolares);
		productos.set("consolidadoSaldos", consolidadoSaldos);
		productos.set("mostrarArrepentimiento", mostrarArrepentimiento);

		Respuesta respuesta = Respuesta.exito("productos", productos);
		if (ProductosService.productos(contexto).objetos("errores").size() > 0) {
			respuesta.setEstadoExistenErrores();
		}

		tiempo = logTiempo(tiempo, "M");

		return respuesta;
	}

	public static Respuesta limpiarCache(ContextoHB contexto) {
		PagoServicioService.eliminarCachePendientes(contexto);
		ProductosService.eliminarCacheProductos(contexto);
		return Respuesta.exito();
	}

	// TODO Deprecar filtrarPorCuentasPesos & filtrarPorCuentasDolares
	public static Respuesta cuentas(ContextoHB contexto) {
		String filtrarPorIdMoneda = contexto.parametros.string("filtrarPorIdMoneda", null);
		Boolean filtrarPorCuentasPesos = contexto.parametros.bool("filtrarPorCuentasPesos", false);
		Boolean filtrarPorCuentasDolares = contexto.parametros.bool("filtrarPorCuentasDolares", false);
		Boolean filtrarPorCuentasUnipersonales = contexto.parametros.bool("filtrarPorCuentasUnipersonales", false);
		Boolean separarPorMoneda = contexto.parametros.bool("separarPorMoneda", false);
		Boolean filtrarInactivas = contexto.parametros.bool("filtrarInactivas", false);
		Boolean filtrarCategoriaSS = contexto.parametros.bool("filtrarCategoriaSS",false);
		Boolean filtrarCuentasPlanSueldo = contexto.parametros.bool("filtrarCuentasPlanSueldo", false);
		Boolean buscarEstadoVendedorCoelsa = contexto.parametros.bool("buscarEstadoVendedorCoelsa", false);
		Boolean buscarTDAsociada = contexto.parametros.bool("buscarTDAsociada", false);
		ApiResponse response = new ApiResponse();
		if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
			for (Objeto item : ContextoHB.consolidada.objeto("productos").objeto("cuentas").objetos()) {
				contexto.cuentas().add(new Cuenta(contexto, item));
			}
		} else {
			response = ProductosService.productos(contexto);
		}

		if (response.hayError()) {
			return Respuesta.error();
		}

		Objeto cuentas = new Objeto();
		Objeto adelantoExistente = new Objeto();
		if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
			for (Cuenta cuenta : contexto.cuentas()) {
				//item.set("id", cuenta.string("id", ""));
				Objeto item = new Objeto();
				item.set("id", cuenta.idEncriptado());
				item.set("descripcion", cuenta.string("descripcion", ""));
				item.set("descripcionCorta", cuenta.string("descripcionCorta", ""));
				item.set("numeroFormateado", cuenta.string("numeroFormateado", ""));
				item.set("numeroEnmascarado", cuenta.string("numeroEnmascarado", ""));
				//item.set("ultimos4digitos", cuenta.ultimos4digitos());
				item.set("titularidad", cuenta.string("titularidad", ""));
				//item.set("idMoneda", cuenta.string("moneda", ""));
				item.set("moneda", cuenta.string("moneda", ""));
				item.set("simboloMoneda", cuenta.string("simboloMoneda", ""));
				item.set("estado", cuenta.string("estado", ""));
				item.set("saldo", cuenta.string("saldo", ""));
				item.set("saldoFormateado", cuenta.string("saldoFormateado", ""));
				//item.set("acuerdo", cuenta.acuerdo());
				//item.set("acuerdoFormateado", cuenta.acuerdoFormateado());
				item.set("cbu", cuenta.cbu());
				item.set("alias", cuenta.alias());
				item.set("esCvu", cuenta.esCvu());
				//item.set("disponible", cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
				item.set("disponibleFormateado", cuenta.string("disponibleFormateado", ""));
				//item.set("esMAU", cuenta.categoria().equals("MAU"));
				item.set("fechaAlta", cuenta.string("fechaAlta", ""));
//				if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
//					item.set("adelantoDisponible", cuenta.adelantoDisponible());
//					item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
//					item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
//					item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
//					item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
//					item.set("adelantoInteresesDevengadosFormateado",
//							Formateador.importe(cuenta.adelantoInteresesDevengados()));
//					adelantoExistente = item;
//				}
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

				if (filtrarInactivas && "I".equals(cuenta.string("estado", ""))) {
					continue;
				}

				if (filtrarCuentasPlanSueldo) {
					ApiResponse responseCuenta = CuentasService.cuentaBH(contexto, cuenta.string("numero", ""));
//					if (!Objeto.setOf("EM", "K", "EV").contains(responseCuenta.string("categoria"))) {
//						continue;
//					}
				}

				if (filtrarPorIdMoneda != null && filtrarPorIdMoneda.equals(cuenta.string("moneda", ""))) {
					if (!separarPorMoneda) {
						cuentas.add(item);
					} else {
						cuentas.add(cuenta.moneda().toLowerCase(), item);
					}
					continue;
				}

				if (filtrarPorIdMoneda == null) {
					Boolean pasaFiltro = !filtrarPorCuentasPesos && !filtrarPorCuentasDolares;
					pasaFiltro |= filtrarPorCuentasPesos && "80".equals(cuenta.string("moneda", ""));
					pasaFiltro |= filtrarPorCuentasDolares && "2".equals(cuenta.string("moneda", ""));

//					if (filtrarPorCuentasUnipersonales && !cuenta.unipersonal()) {
//						pasaFiltro = false;
//					}

//					if (filtrarCategoriaSS && "SS".equalsIgnoreCase(cuenta.categoria())) {
//						pasaFiltro = false;
//					}

					if (pasaFiltro) {
						if (!separarPorMoneda) {
							cuentas.add(item);
						} else {
							cuentas.add(cuenta.string("moneda", "").toLowerCase().replace("ó", "o"), item); // TODO arreglar
						}
					}
				}

				if (buscarTDAsociada) {
					TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoAsociadaHabilitadaLink(cuenta);
					item.set("tarjetaAsociadaVirtual", tarjetaDebito.virtual());
				}
			}
		} else {
			for (Cuenta cuenta : contexto.cuentas()) {
				//item.set("id", cuenta.id());
				Objeto item = new Objeto();
				item.set("id", cuenta.idEncriptado());
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
				item.set("cbu", cuenta.cbu());
				item.set("alias", cuenta.alias());
				item.set("esCvu", cuenta.esCvu());
				item.set("disponible",
						cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
				item.set("disponibleFormateado", Formateador.importe(item.bigDecimal("disponible")));
				item.set("esMAU", cuenta.categoria().equals("MAU"));
				item.set("fechaAlta", cuenta.fechaAlta("dd/MM/yyyy"));
				if ("ADE".equalsIgnoreCase(cuenta.categoria())) {
					item.set("adelantoDisponible", cuenta.adelantoDisponible());
					item.set("adelantoDisponibleFormateado", Formateador.importe(cuenta.adelantoDisponible()));
					item.set("adelantoUtilizado", cuenta.adelantoUtilizado());
					item.set("adelantoUtilizadoFormateado", Formateador.importe(cuenta.adelantoUtilizado()));
					item.set("adelantoInteresesDevengados", cuenta.adelantoInteresesDevengados());
					item.set("adelantoInteresesDevengadosFormateado",
							Formateador.importe(cuenta.adelantoInteresesDevengados()));
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
					ApiResponse responseCuenta = CuentasService.cuentaBH(contexto, cuenta.numero());
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

					if (filtrarCategoriaSS && "SS".equalsIgnoreCase(cuenta.categoria())) {
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
		}

		Objeto xcuentas = reorganizaCuentas(cuentas.objetos(), adelantoExistente);
		return Respuesta.exito("cuentas", xcuentas.lista == null ? null : xcuentas);
	}

	public static Respuesta cuentasComitentes(ContextoHB contexto) {
		String idCuentaComitente = contexto.parametros.string("idCuentaComitente", null);
		Boolean buscarCuentasAsociadas = contexto.parametros.bool("buscarCuentasAsociadas", false);
		Boolean buscarTenenciaValuada = contexto.parametros.bool("buscarTenenciaValuada", false);

		CuentaComitente cuentaComitenteRequest = contexto.cuentaComitente(idCuentaComitente);

		Boolean flag = false;
		Respuesta respuesta = new Respuesta();
		Objeto cuentasComitentes = new Objeto();
		
		Map<CuentaComitente, ApiResponse> mapaResponse = HBInversion.cuentasComitentesEspecies(contexto,
				respuesta);
		
		for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
			if (cuentaComitenteRequest != null && !itemCuentaComitente.id().equals(idCuentaComitente)) {
				continue;
			}

			ApiResponse responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto,
					itemCuentaComitente.numero());
			if (responseCuentasComitentes.hayError()) {
				return Respuesta.error();
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
				//item.set("id", cuenta.id());
				item.set("id", cuenta.idEncriptado());
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
				item.set("disponible",
						cuenta.saldo().add(cuenta.acuerdo() != null ? cuenta.acuerdo() : new BigDecimal("0")));
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
					ApiResponse response = mapaResponse.get(cuenta.get(0));
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
			flag = true;
		}

		respuesta.set("cuentasComitentes", flag != false ? cuentasComitentes : null);

		return respuesta;
	}

	public static Respuesta cuentasCuotapartistas(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();

		ApiResponse response = new ApiResponse();
		if(ConfigHB.bool("hb_posicion_consolidada_V4")) {
			for (Objeto item : ContextoHB.consolidada.objeto("productos").objeto("cuentasCuotapartistas").objetos()) {
				item.set("id", item.string("numero"));
				item.set("titularidad", Texto.primeraMayuscula(item.string("descTipoTitularidad").toLowerCase()));
				respuesta.add("cuentasCuotapartistas", item);
			}
		} else {
			response = ProductosService.productos(contexto);
			for (Objeto objeto : response.objetos("productos")) {
				if (objeto.string("tipo").equals("RJA")) {
					Objeto item = new Objeto();
					item.set("id", objeto.string("numero"));
					item.set("titularidad", Texto.primeraMayuscula(objeto.string("descTipoTitularidad").toLowerCase()));
					respuesta.add("cuentasCuotapartistas", item);
				}
			}
		}

		if (response.hayError()) {
			return Respuesta.error();
		}

		respuesta.set("poseeCuentaCuotapartista", respuesta.get("cuentasCuotapartistas") != null);
		return respuesta.ordenar("estado", "poseeCuentaCuotapartista");
	}

	public static Respuesta cajasSeguridad(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
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

			ApiResponse response = RestCajaSeguridad.detalle(contexto, cajaSeguridad.numero());
			if (!response.hayError()) {
				for (Objeto datos : response.objetos()) {
					Cuenta cuenta = contexto.cuenta(datos.string("cuenta"));
					item.set("cuenta",
							cuenta != null ? cuenta.descripcionCorta() + " XXXX - " + cuenta.ultimos4digitos() : "");
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

	public static Respuesta bajaCajaAhorro(ContextoHB contexto) {
		String idCuenta = contexto.parametros.string("idCuenta");

		if (Objeto.anyEmpty(idCuenta)) {
			return Respuesta.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idCuenta);
		if (cuenta == null) {
			return Respuesta.estado("ERROR");
		}
		if (!cuenta.idTipo().equals("AHO")) {
			return Respuesta.estado("ERROR");
		}

		Respuesta respuesta = new Respuesta();
		// CHEQUEO SI TIENE UNA SOLICITUD DE CAJA DE AHORRO EN CURSO
		if (!"".equals(cuenta.idPaquete())) {
			Objeto tcObj = new Objeto();
			tcObj.set("idPaquete", cuenta.idPaquete());

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "BAJA_PAQUETES", tcObj, true)) {
				return Respuesta.estado("SOLICITUD_EN_CURSO");
			}
		} else {
			// TODO chequear baja CA
			Objeto tcObj = new Objeto();
			tcObj.set("numero", cuenta.numero());

			if (RestPostventa.tieneSolicitudEnCursoBajaCa(contexto, "BAJACA_PEDIDO", tcObj, true)) {
				return Respuesta.estado("SOLICITUD_EN_CURSO");
			}
		}

		// CHEQUEO SI ESTA ASOCIADA A UNA CUENTA COMITENTE
		for (CuentaComitente itemCuentaComitente : contexto.cuentasComitentes()) {
			ApiResponse responseCuentasComitentes = InversionesService.inversionesGetCuentasPorComitente(contexto,
					itemCuentaComitente.numero());
			if (responseCuentasComitentes.hayError()) {
				return Respuesta.estado("ERROR_CONSULTANDO_COMITENTES");
			}
			for (Objeto cuentaItem : responseCuentasComitentes.objetos()) {
				if (cuentaItem.string("NUMERO").trim().equals(cuenta.numero())
						|| cuentaItem.string("NUMERO").trim().equals(cuenta.id())) {
					return Respuesta.estado("CUENTA_COMITENTE_ASOCIADA");
				}
			}
		}

		// CHEQUEO SI LA CAJA DE AHORRO TIENE MAS DE UN TITULAR
		if (!cuenta.unipersonal()) {
			return Respuesta.estado("COTITULAR");
		}
		// if (!"U".equalsIgnoreCase(cuenta.usoFirma())) {
		// return Respuesta.estado("COTITULAR");
		// }

		// CHEQUEO SI TIENE BLOQUEOS
		String numeroCorto = cuenta.numeroCorto();
		if ("".equals(numeroCorto)) {
			return Respuesta.error();
		}
		ApiResponse responseBloqueos = CuentasService.cajaAhorroBloqueos(contexto, numeroCorto);
		if (responseBloqueos.hayError()) {
			return Respuesta.estado("CONSULTA_BLOQUEOS");
		}
		if (!responseBloqueos.objetos().isEmpty()) {
			return Respuesta.estado("TIENE_BLOQUEOS");
		}

		ApiResponse responseReclamo;

		if ("80".equals(cuenta.idMoneda()) && !"".equals(cuenta.idPaquete())) {

			responseReclamo = RestPostventa.bajaPaquete(contexto, "BAJA_PAQUETES", cuenta.idPaquete());

			if (responseReclamo == null || responseReclamo.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

		} else if ("80".equals(cuenta.idMoneda()) && "".equals(cuenta.idPaquete())) {

			responseReclamo = RestPostventa.bajaCajaAhorro(contexto, "BAJACA_PEDIDO", cuenta);

			if (responseReclamo == null || responseReclamo.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}
			
			if (HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
				Objeto parametros = new Objeto();
				parametros.set("IDCOBIS", contexto.idCobis());
				parametros.set("NOMBRE", contexto.persona().nombre());
				parametros.set("APELLIDO", contexto.persona().apellido());
				parametros.set("CANAL", "Home Banking");
				parametros.set("NUMERO_CUENTA", cuenta.numero());
				parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(new Date()));
				new Futuro<>(() -> HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_baja_caja_ahorro_ok"), parametros));
			}

		} else {
			return Respuesta.error();
		}

		SqlResponse sqlResponse = insertarReclamo(contexto, "BAJA_RECL", cuenta.numero(), cuenta.idTipo(),
				cuenta.sucursal(), "");
		if (sqlResponse.hayError) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}

		return respuesta;
	}
	
	public static Respuesta bajaTarjetaCredito(ContextoHB contexto) {
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_baja_tc_con_oferta_crm")) {
			return bajaTarjetaCreditoV2(contexto);
		}
		String idTarjeta = contexto.parametros.string("idTarjeta");

		if (Objeto.anyEmpty(idTarjeta)) {
			return Respuesta.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
		if (tarjetaCredito == null) {
			return Respuesta.estado("ERROR");
		}

		if (tarjetaCredito.esHML() && tarjetaCredito.esTitular()) {
			return bajaTarjetaHML(contexto);
			// return mensajeBajaTCHML(contexto, tarjetaCredito);
		}

		Respuesta respuesta = new Respuesta();
		try {

			/*
			 * if( tieneSolicitudBajaCurso(contexto, tarjetaCredito.numero()) ){
			 * respuesta.setEstado("ES_TC");
			 * respuesta.set("mensaje",Config.string("baja_tc_mensaje_solicitud_en_curso"));
			 * respuesta.set("habilitaBaja", false); return respuesta; }
			 */

			if (tarjetaCredito.esTitular()) {

				if (HBPrestamo.tienePrestamoTasaCero(contexto)) {
					respuesta.setEstado("ES_TC");
					respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_call_center"));
					respuesta.set("habilitaBaja", false);
					return respuesta;
				}

				String estado = tarjetaCredito.idEstado();
				Respuesta respPaquetes = HBPaquetes.consolidadaPaquetes(contexto);
				Objeto objPaquete = respPaquetes.objeto("datosPrincipales");
				Boolean tienePaquete = !"".equals(objPaquete.string("numeroPaquete"));
				// Boolean tienePaquete = !"".equals(tarjetaCredito.idPaquete());
				if (estado.equals("20")) {
					Respuesta caso = generarCasoBajaTC(contexto, tarjetaCredito,
							tienePaquete ? "BAJA_PAQUETES" : "BAJATC_PEDIDO");
					if (caso.hayError()) {
						return caso;
					}

					respuesta.setEstado("ES_TC");
					respuesta.set("mensaje",
							messageBajaTc(contexto.persona().nombreCompleto(), caso.string("numeroCaso"), false));
					respuesta.set("habilitaBaja", false);
					respuesta.set("numeroCaso", "CAS-" + caso.string("numeroCaso"));
					return respuesta;
				}

				if (estado.equals("25")) {

					Respuesta caso = generarCasoBajaTC(contexto, tarjetaCredito, "BAJATC_PEDIDO");
					if (caso.hayError()) {
						return caso;
					}

					respuesta.setEstado("ES_TC");
					if (tienePaquete) {
						respuesta.set("mensaje",
								messageBajaTc(contexto.persona().nombreCompleto(), caso.string("numeroCaso"), false));
					} else {
						respuesta.set("mensaje",
								messageBajaTc(contexto.persona().nombreCompleto(), caso.string("numeroCaso"), true)
										+ ConfigHB.string("baja_tc_mensaje_cuotas"));
					}
					respuesta.set("habilitaBaja", false);
					respuesta.set("numeroCaso", "CAS-" + caso.string("numeroCaso"));
					return respuesta;
				}

				// Otros estados o casos no contemplados
				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_call_center"));
				respuesta.set("habilitaBaja", false);
				return respuesta;

			} else { // adicional

				Respuesta caso = generarCasoBajaTC(contexto, tarjetaCredito, "BAJA_TC_ADICIONAL_PEDIDO");
				if (caso.hayError()) {
					return caso;
				}
				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje",
						messageBajaTc(contexto.persona().nombreCompleto(), caso.string("numeroCaso"), true));
				respuesta.set("habilitaBaja", false);
				respuesta.set("numeroCaso", "CAS-" + caso.string("numeroCaso"));
				return respuesta;
			}
		} catch (Exception e) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}
	}

	public static Respuesta bajaTarjetaCreditoV2(ContextoHB contexto) {

		String idTarjeta = contexto.parametros.string("idTarjeta");
		String tipificacion = contexto.parametros.string("tipificacion");
		String motivoBaja = contexto.parametros.string("motivo","");

		Boolean altaPaqueteRetencion = contexto.parametros.bool("altaPaqueteRetencion", false);
		String altaPaqueteRetencionPackOferta = contexto.parametros.string("altaPaqueteRetencionPackOferta", "");
		String observaciones = "";

		if (Objeto.anyEmpty(idTarjeta)) {
			return Respuesta.parametrosIncorrectos();
		}

		Futuro<Respuesta> respuestaFuturoCRM = new Futuro<>(() -> campaniaCRM(contexto));
		Futuro<TarjetaCredito> tarjetaCreditoFuturo = new Futuro<>(() -> contexto.tarjetaCredito(idTarjeta));

		if (tarjetaCreditoFuturo.get() == null) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}

		String oferta = "";
		Objeto ofertas = !respuestaFuturoCRM.get().objetos("ofertas").isEmpty() ? respuestaFuturoCRM.get().objetos("ofertas").get(0) : null;
		if (ofertas != null){
			oferta = " Primera Oferta: "+ ofertas.string("OfertaRetencion1");
			oferta = oferta.concat(" ("+ofertas.string("OfertaRetencion1Descripcion")+")");
			if(!ofertas.string("OfertaRetencion2").isEmpty()){
				oferta = oferta.concat(", Segunda Oferta: "+ ofertas.string("OfertaRetencion2"));
				oferta = oferta.concat(" ("+ ofertas.string("OfertaRetencion2Descripcion")+")");
			}
		}

		if(altaPaqueteRetencion){
			observaciones = altaPaqueteRetencionPackOferta;
		}else{
			observaciones = !motivoBaja.isEmpty() ? ("Motivo: "+motivoBaja) : "";
			observaciones = observaciones.concat(oferta.isEmpty() ? "" : oferta);
		}

		String estadoTarjeta = tarjetaCreditoFuturo.get().idEstado();
		String idPaquete =  tarjetaCreditoFuturo.get().idPaquete();
		Boolean tienePaquete = !"".equals(idPaquete);
		Boolean esHML = tarjetaCreditoFuturo.get().esHML();

		Boolean esTitular = tarjetaCreditoFuturo.get().esTitular();

		try {
			ApiResponse responseReclamo = null;

			if(tienePaquete && estadoTarjeta.equals("20") && "BAJA_PAQUETES".equals(tipificacion)){
				responseReclamo = RestPostventa.bajaPaquete(contexto, tipificacion, idPaquete);
			}

			if(!esTitular){
				tipificacion = "BAJA_TC_ADICIONAL_PEDIDO";
				responseReclamo = RestPostventa.bajaTarjetaCreditoAdicional(contexto, tipificacion, tarjetaCreditoFuturo.get());
			}

			if(!tienePaquete && estadoTarjeta.equals("20")){
				if(esTitular){
					switch (tipificacion) {
						case "BAJATC_PEDIDO":
							responseReclamo = RestPostventa.bajaTarjetaCredito(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);
							break;
						case "BAJATCAUTOGESTIVO":
							responseReclamo = RestPostventa.bajaDirectaTarjetaCredito(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);
							break;
						case "BAJATCHML_PEDIDO":
							responseReclamo = RestPostventa.bajaTarjetaHML(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);
							break;
						case "9017P": //TODO: BAJA TC HML - CANAL AUTOGESTIVO
							responseReclamo = RestPostventa.bajaTarjetaHML_AUTOGESTIVO(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);
							break;
						default:
							return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
					}
				}
			}

			if(estadoTarjeta.equals("25")){
				if(esHML) responseReclamo = RestPostventa.bajaTarjetaHML_AUTOGESTIVO(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);

				if(!esHML) responseReclamo = RestPostventa.bajaDirectaTarjetaCredito(contexto, tipificacion, tarjetaCreditoFuturo.get(), observaciones);
			}

			if (responseReclamo == null || responseReclamo.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamo);
			if (numeroCaso.isEmpty()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			SqlResponse sqlResponse = HBProducto.insertarReclamo(contexto, "BAJA_RECL",
					tienePaquete ? idPaquete : tarjetaCreditoFuturo.get().numero(), tienePaquete ? "PAQ":"ATC",
					tarjetaCreditoFuturo.get().sucursal(), "");

			if (sqlResponse.hayError) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			return Respuesta.exito().set("numeroCaso", numeroCaso);

		} catch (Exception e) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}

	}

	//TODO: Metodo que se usa para enviar tipi de baja TC con Observacion: Retencion por alta de paquete desde el Flujo de Baja TC
	public static Object altaPaqueteTarjetaCredito(ContextoHB contexto) {

		String idTarjeta = contexto.parametros.string("idTarjeta");
		String tipificacion = "";
		Boolean altaPaqueteRetencionPackOferta = contexto.parametros.bool(("altaPaqueteRetencionPackOferta"), false);

		if (Objeto.anyEmpty(idTarjeta)) {
			return Respuesta.parametrosIncorrectos();
		}

		Futuro<TarjetaCredito> tarjetaCreditoFuturo = new Futuro<>(() -> contexto.tarjetaCredito(idTarjeta));
		tipificacion = tarjetaCreditoFuturo.get().esHML() ? "BAJATCHML_PEDIDO" : "BAJATC_PEDIDO";

		contexto.parametros.set("idTarjeta", idTarjeta);
		contexto.parametros.set("tipificacion", tipificacion);
		contexto.parametros.set("altaPaqueteRetencion", true);
		contexto.parametros.set("altaPaqueteRetencionPackOferta", altaPaqueteRetencionPackOferta ? "RETENIDO SOLO ALTA PACK":"RETENIDO PACK + OFERTA 1");

		return bajaTarjetaCreditoV2(contexto);
	}

	public static Respuesta bajaPaquete(ContextoHB contexto) {
		String numeroPaquete = contexto.parametros.string("numeroPaquete");

		if (Objeto.anyEmpty(numeroPaquete)) {
			return Respuesta.parametrosIncorrectos();
		}

		/*
		 * if (tieneSolicitudEnCurso(contexto, "BAJA_RECL", "PAQ", numeroPaquete)) {
		 * return Respuesta.estado("SOLICITUD_EN_CURSO"); }
		 */
		Respuesta respuesta = new Respuesta();

		try {
			String tipificacion = "BAJA_PAQUETES";

			Objeto tcObj = new Objeto();
			tcObj.set("idPaquete", numeroPaquete);

			if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, tcObj, true)) {
				return Respuesta.estado("SOLICITUD_EN_CURSO");
			}

			ApiResponse responseReclamo = RestPostventa.bajaPaquete(contexto, tipificacion, numeroPaquete);

			if (responseReclamo == null || responseReclamo.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamo);
			if (numeroCaso.isEmpty()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			SqlResponse sqlResponse = insertarReclamo(contexto, "BAJA_RECL", numeroPaquete, "PAQ", "", "");
			if (sqlResponse.hayError) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta.set("numeroCaso", numeroCaso);
		} catch (Exception e) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}
	}

	// TODO: 29674
	// no se detecto llamado desde el front lo cambio para que quede listo por si se
	// empieza a utilizar
	public static Respuesta solicitarDocumentacionDigitalTyc(ContextoHB contexto) {
		// if(HBAplicacion.funcionalidadPrendida(contexto.idCobis(),
		// "prendido_tyc_crm")) {

		String idTarjeta = contexto.parametros.string("idTarjeta");
		Respuesta respuesta = new Respuesta();

		if (Objeto.anyEmpty(idTarjeta)) {
			return Respuesta.parametrosIncorrectos();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);
		if (tarjetaCredito == null) {
			return Respuesta.estado("ERROR");
		}
		String tipificacion = "TERMINOS_CONDICIONES_PEDIDO";

		// ver de agregar validacion de idTarjeta
		if (RestPostventa.tieneSolicitudEnCurso(contexto, tipificacion, null, true)) {
			return Respuesta.estado("SOLICITUD_EN_CURSO");
		}

		ApiResponse responseReclamo = RestPostventa.terminosYCondiciones(contexto, tipificacion, tarjetaCredito);
		if (responseReclamo == null || responseReclamo.hayError()) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}
		return respuesta;
		// }else {
		// String idDocumento = contexto.parametros.string("idDocumento");
		//
		// try {
		// idDocumento = URLDecoder.decode(idDocumento, "UTF-8");
		// } catch (UnsupportedEncodingException e) {
		// }
		//
		// idDocumento = idDocumento.replace("{", "");
		// idDocumento = idDocumento.replace("}", "");
		//
		// if (Objeto.anyEmpty(idDocumento)) {
		// return Respuesta.parametrosIncorrectos();
		// }
		//
		// if (tieneSolicitudEnCurso(contexto, "SOLDOCDIG", "_", idDocumento)) {
		// return Respuesta.estado("SOLICITUD_EN_CURSO");
		// }
		// Respuesta respuesta = new Respuesta();
		//
		// // si la tarjeta está paquetizada tengo que dar de alta el reclamo para que
		// la
		// // de de baja del paquete
		// String idTemaReclamo = "";
		// String idGrupo = "";
		// String idCanal = "";
		// if ("".equals(Config.string("reclamo_solicitud_baja_paquete"))) {
		// return Respuesta.estado("FALTA_CONFIGURAR_RECLAMO_PAQUETE_BAJA");
		// } else {
		// idTemaReclamo =
		// Config.string("reclamo_solicitud_documentacion_digital_tyc").split("_")[0];
		// idGrupo =
		// Config.string("reclamo_solicitud_documentacion_digital_tyc").split("_")[1];
		// idCanal =
		// Config.string("reclamo_solicitud_documentacion_digital_tyc").split("_")[2];
		// }
		//
		// // Genero un reclamo en PRISMA
		// ApiResponse responseReclamoPaquete = RestPrisma.generarReclamo(contexto,
		// idCanal, idTemaReclamo, "CLI", idDocumento, idDocumento, false, idGrupo, "",
		// "Solicitud de Documentación Digital", "C", "CONSULTA");
		// if (responseReclamoPaquete.hayError()) {
		// return Respuesta.estado("ERROR_GENERANDO_RECLAMO_PRISMA");
		// }
		//
		// SqlResponse sqlResponse = insertarReclamo(contexto, "SOLDOCDIG", idDocumento,
		// "_", "", "");
		// if (sqlResponse.hayError) {
		// return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		// }
		//
		// return respuesta;
		// }

	}

	public static SqlResponse insertarReclamo(ContextoHB contexto, String tipoOperacion, String numeroProducto,
			String tipoProducto, String sucursal, String ttcc) {
		// --OBJETO DE SOLICITUD DE BAJA DE BBDD
		SqlResponse sqlResponse = null;
		SqlRequest sqlRequest = Sql.request("InsertReclamo", "homebanking");
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
		sqlRequest.add("HB");

		sqlResponse = Sql.response(sqlRequest);
		return sqlResponse;
	}

	public static Respuesta mensajeBajaTCHML(ContextoHB contexto, TarjetaCredito tarjetaCredito) {
		Respuesta respuesta = new Respuesta();
		String esHml = "ES_HML";// Config.esProduccion() ? "esHML" : "ES_HML";
		try {
			/* ========== PRESTAMO TASA CERO ========== */
			Boolean tienePrestamo = HBPrestamo.tienePrestamoTasaCero(contexto);

			if (tienePrestamo) {
				respuesta.setEstado(esHml);
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_call_center"));
				respuesta.set("habilitaBaja", false);
				return respuesta;
			}

			String estado = tarjetaCredito.idEstado();

			/* ========== SOLICITUD BAJA ========== */
			Boolean solicitudPendiente = false;

			Objeto tcObj = new Objeto();
			tcObj.set("tcNumero", tarjetaCredito.numero());

			if (RestPostventa.tieneSolicitudEnCurso(contexto, "BAJATCHML_PEDIDO", tcObj, true)) {
				solicitudPendiente = true;
			}

			if ((estado.equals("20") || estado.equals("25")) && solicitudPendiente) {
				respuesta.setEstado(esHml);
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_solicitud_en_curso"));
				respuesta.set("habilitaBaja", false);
				return respuesta;
			}

			/* ========== CUOTAS PENDIENTES ========== */
			Boolean tieneCuotas = false;
			Respuesta respuestaCuotas = tieneCuentasPendientes(contexto, tarjetaCredito.cuenta(),
					tarjetaCredito.numero());
			if (respuestaCuotas.get("estado").equals("0")) {
				tieneCuotas = (Boolean) respuestaCuotas.get("tieneCuotas");
			} else {
				return respuestaCuotas;
			}

			if (estado.equals("20") && !tieneCuotas) {
				respuesta.setEstado(esHml);
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_sin_cuotas"));
				respuesta.set("habilitaBaja", true);
				return respuesta;
			}

			if (estado.equals("20") && tieneCuotas || estado.equals("25")) {
				respuesta.setEstado(esHml);
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_cuotas"));
				respuesta.set("habilitaBaja", true);
				return respuesta;
			}

			/* ========== SI ES HML Y NO ENTRO EN NINGUN CASO ========== */
			respuesta.setEstado(esHml);
			respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_call_center"));
			respuesta.set("habilitaBaja", false);
			return respuesta;

		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static Respuesta bajaTarjetaHML(ContextoHB contexto) {
		try {
			Respuesta respuesta = new Respuesta();
			String idTarjeta = contexto.parametros.string("idTarjeta");

			if (Objeto.anyEmpty(idTarjeta)) {
				return Respuesta.parametrosIncorrectos();
			}

			TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idTarjeta);

			if (!tarjetaCredito.esTitular()) {
				return Respuesta.error();
			}

			ApiResponse responseReclamoTarjeta = RestPostventa.bajaTarjetaHML(contexto, "BAJATCHML_PEDIDO",
					tarjetaCredito, "");

			if (responseReclamoTarjeta == null || responseReclamoTarjeta.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamoTarjeta);

			SqlResponse sqlResponse = insertarReclamo(contexto, "BAJA_RECL", tarjetaCredito.numero(), "ATC",
					tarjetaCredito.sucursal(), "");
			if (sqlResponse.hayError) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta.exito().set("numeroCaso", numeroCaso);

		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	private static Respuesta tieneCuentasPendientes(ContextoHB contexto, String cuenta, String numero) {
		try {
			ApiResponse response = TarjetaCreditoService.cuotasPendientes(contexto, cuenta, numero);
			Respuesta respuesta = new Respuesta();
			boolean error404cuotasPendientes = false;
			respuesta.set("tieneCuotas", false);
			if (response.hayError()) {
				if (response.string("codigo").equals("404") || response.string("codigo").equals("112107")) {
					error404cuotasPendientes = true;
				} else {
					return Respuesta.estado("ERROR_CONSULTA_CUOTAS_PENDIENTES");
				}
			}
			if (!error404cuotasPendientes) {
				if (respuesta.set("cuotasPendientes") != null
						&& !response.objetos("cuotasPendientes.tarjeta").isEmpty()) {
					respuesta.set("tieneCuotas", true);
				}
			}
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
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

	private static Respuesta generarCasoBajaTC(ContextoHB contexto, TarjetaCredito tarjetaCredito, String tipi) {
		Respuesta respuesta = new Respuesta();
		try {
			ApiResponse responseReclamo = null;

			// agegar validacion de datos segun tipi
			Respuesta respPaquetes = HBPaquetes.consolidadaPaquetes(contexto);
			Objeto objPaquete = respPaquetes.objeto("datosPrincipales");
			Objeto tcObj = new Objeto();
			tcObj.set("tcNumero", tarjetaCredito.numero());
			tcObj.set("idPaquete", objPaquete.string("numeroPaquete"));
			// tcObj.set("idPaquete", objPaquete.string("numeroPaquete"));

			if (RestPostventa.tieneSolicitudEnCurso(contexto, tipi, tcObj, true)) {
				respuesta.setEstado("ES_TC");
				respuesta.set("mensaje", ConfigHB.string("baja_tc_mensaje_solicitud_en_curso"));
				respuesta.set("habilitaBaja", false);
				return respuesta;
			}

			switch (tipi) {
				case "BAJATC_PEDIDO":
					responseReclamo = RestPostventa.bajaTarjetaCredito(contexto, tipi, tarjetaCredito, "");
					break;
				case "BAJA_TC_ADICIONAL_PEDIDO":
					responseReclamo = RestPostventa.bajaTarjetaCreditoAdicional(contexto, tipi, tarjetaCredito);
					break;
				case "BAJA_PAQUETES":
					responseReclamo = RestPostventa.bajaPaquete(contexto, tipi, tcObj.string("idPaquete"));
					break;
				default:
					return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			if (responseReclamo == null || responseReclamo.hayError()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			String numeroCaso = Util.getNumeroCaso(responseReclamo);

			if (numeroCaso.isEmpty()) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			try {
				respuesta.set("numeroCaso", numeroCaso.replace("CAS-", ""));
			} catch (Exception e) {
				respuesta.set("numeroCaso", numeroCaso);
			}

			SqlResponse sqlResponse = insertarReclamo(contexto, "BAJA_RECL", tarjetaCredito.numero(), "ATC",
					tarjetaCredito.sucursal(), "");
			if (sqlResponse.hayError) {
				return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
			}

			return respuesta;

		} catch (Exception e) {
			return Respuesta.estado("ERROR_GENERANDO_RECLAMO");
		}
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
		return (adelantoExistente.existe("id")
				&& ("Adelanto BH".equalsIgnoreCase(adelantoExistente.string("descripcion"))
						|| "ADE".equalsIgnoreCase(adelantoExistente.string("descripcionCorta")))
				&& !agregada
				&& ("Caja de Ahorro".equalsIgnoreCase(cuenta.string("descripcion"))
						|| "AHO".equalsIgnoreCase(cuenta.string("descripcionCorta"))
								&& cuenta.string("adelantoCuentaAsociada")
										.contentEquals(adelantoExistente.string("numeroProducto"))));
	}

	public static Boolean acreditacionesHaberes(ContextoHB contexto) {
		ApiResponse acreditaciones = ProductosService.getAcreditaciones(contexto);

		if (acreditaciones.objetos().size() > 0) {
			Integer mesActual = LocalDateTime.now().getDayOfMonth() > 10 ? LocalDateTime.now().getMonthValue()
					: LocalDateTime.now().getDayOfMonth() - 1;
			String ultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual);
			String penultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual - 1);
			String antPenultimaFecha = String.valueOf(LocalDateTime.now().getYear()) + String.valueOf(mesActual - 2);

			Objeto haberes = acreditaciones.objetos().get(0);
			if (haberes.get("acreditacionesPeriodo1") != null && haberes.get("acreditacionesPeriodo2") != null
					&& haberes.get("acreditacionesPeriodo3") != null
					&& haberes.get("acreditacionesPeriodo1") != ultimaFecha
					&& haberes.get("acreditacionesPeriodo2") != penultimaFecha
					&& haberes.get("acreditacionesPeriodo3") != antPenultimaFecha
					&& haberes.get("acreditacionesMonto1") != null && haberes.get("acreditacionesMonto2") != null
					&& haberes.get("acreditacionesMonto3") != null) {
				return true;
			}
		}
		return false;
	}

	public static String obtenerSegmentoComercial(ContextoHB contexto) {
		ApiResponse acreditaciones = ProductosService.getAcreditaciones(contexto);

		if (!acreditaciones.hayError() && acreditaciones.objetos().size() > 0) {
			Objeto haberes = acreditaciones.objetos().get(0);
			if (haberes.get("segmentoComercial") != null) {
				return haberes.get("segmentoComercial").toString();
			}
		}
		return "";
	}

	public static boolean esPlanSueldoInactivo(ContextoHB contexto) {
		if ("PLAN SUELDO INACTIVO".equals(obtenerSegmentoComercial(contexto))) {
			return true;
		}
		return false;
	}

	public static boolean esTarjetaRetension(ContextoHB contexto) {
		ApiResponse responseOfertaRetencion = RestPostventa.obtenerOfertasRetencion(contexto);
		if(responseOfertaRetencion.hayError()){
			if("400".equals(responseOfertaRetencion.objetos("Errores").get(0).string("Codigo"))){
				return false;
			}
			return new Respuesta().hayError();
		}
		if(responseOfertaRetencion.objetos("Datos").size() > 0) {
			return true;
		}
		return false;
	}

	public static Respuesta campaniaCRM(ContextoHB contexto) {

		ApiResponse response = RestPostventa.obtenerOfertasRetencion(contexto);
		Respuesta respuesta = new Respuesta();
		Objeto ofertas = new Objeto();

		if (response == null){
			return Respuesta.estado("ERROR");
		}

		try{
			if (response.hayError()) {
				if (response.objetos("Errores") != null && response.objetos("Errores").size() > 0) {
					if (response.objetos("Errores").get(0).string("Codigo").equals("400")) {
						return respuesta.set("tieneCampania", false);
					}
				}
				return Respuesta.estado("ERROR");
			}

			if (response.objetos("Datos") != null && response.objetos("Datos").size() > 0) {
				Objeto oferta = response.objetos("Datos").get(0);
				ofertas.set("OfertaRetencion1", oferta.string("OfertaRetencion1"));
				ofertas.set("OfertaRetencion1Descripcion", oferta.string("OfertaRetencion1Descripcion"));
				ofertas.set("OfertaRetencion2", oferta.string("OfertaRetencion2"));
				ofertas.set("OfertaRetencion2Descripcion", oferta.string("OfertaRetencion2Descripcion"));

			}

			return respuesta.set("tieneCampania", true).set("ofertas", ofertas);

		}catch (Exception e){
			return null;
		}
	}

	public static List<ProductoMora> getProductosEnMora(ContextoHB contexto) {
		List<ProductoMora> productosEnMora = new ArrayList<>();
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
			ApiResponse responseProductosMora = RestMora.getProductosEnMora(contexto);
			if (responseProductosMora.codigo != 204 && !responseProductosMora.hayError()) {
				for (Objeto item : responseProductosMora.objetos()) {
					ProductoMora productoEnMora = new ProductoMora(item);
					productosEnMora.add(productoEnMora);
				}
			}
		}

		return productosEnMora;
	}

	public static ProductoMoraDetalles getProductosEnMoraDetalles(ContextoHB contexto, String ctaId) {
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_prestamos_mora")) {
			ApiResponse responseProductosMoraDetalles = RestMora.getProductosEnMoraDetalles(contexto, ctaId);
			if (responseProductosMoraDetalles.codigo != 204 && !responseProductosMoraDetalles.hayError()
					&& responseProductosMoraDetalles.objetos().size() > 0) {
				return new ProductoMoraDetalles(responseProductosMoraDetalles.objetos().get(0));
			}
		}
		return null;
	}

	public static void verificarEstadoMoraTemprana(ContextoHB contexto, List<ProductoMora> productosEnMora,
			List<ProductoMoraDetalles> productoMoraDetalles, Prestamo prestamo, Objeto item) {
		Boolean onboardingMostrado = false;
		if (productosEnMora.size() > 0 && productosEnMora.stream()
				.anyMatch(prod -> prod.numeroProducto().equals(prestamo.numero())
						&& Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora())
						&& CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim()))) {
			ProductoMora productoMora = productosEnMora.stream()
					.filter(prod -> prod.numeroProducto().equals(prestamo.numero())).findFirst().get();
			item.set("estadoMora", "EN_MORAT");

			if (productoMoraDetalles.size() > 0) {
				ProductoMoraDetalles detalle = productoMoraDetalles.stream()
						.filter(prod -> prod.ctaId().equals(productoMora.ctaId())).findFirst().get();
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
				verificarEstadoMoraTempranaBucket(contexto, detalle, prestamo, item);
			}
		} else if (!FORMA_PAGO_EFECTIVO.equals(prestamo.formaPago())
				&& prestamo.cuotas().stream().anyMatch(cuota -> CUOTA_VENCIDA.equals(cuota.estado()))) {
			item.set("estadoMora", "EN_MORAT");
		}

		Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
		item.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

		try {
			if (!muestreoOnboarding && onboardingMostrado) {
				contexto.parametros.set("nemonico", "ONBOARDING");
				Util.contador(contexto);
			}
		} catch (Exception e) {
		}
	}

	public static void verificarEstadoMoraTempranaBucket(ContextoHB contexto, ProductoMoraDetalles detalle,
			Prestamo prestamo, Objeto item) {
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_promesa_pago")) {
			if (Arrays.asList(BUCKETS).contains(Util.bucketMora(detalle.diasEnMora(), prestamo.categoria()))) {
				item.set("estadoMora", "EN_MORAT" + "_" + Util.bucketMora(detalle.diasEnMora(), prestamo.categoria()));
			}
		}
	}

	public static Object estadoBajaPaquete(ContextoHB ctx) {

		String idPaquete = ctx.parametros.string("idPaquete");

		if (Objeto.anyEmpty(idPaquete)) {
			return Respuesta.parametrosIncorrectos();
		}

		if (RestPostventa.tieneSolicitudEnCurso(ctx, "BAJA_PAQUETES", new Objeto().set("idPaquete", idPaquete), true)) {
			return Respuesta.estado("ESPERAR_ASESOR");
		}

		return Respuesta.exito();
	}

	public static SqlResponse AltaCuentaEspecial(ContextoHB contexto) {
		SqlResponse sqlResponse = null;
		SqlRequest sqlRequest = Sql.request("InsertReclamo", "homebanking");
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
		sqlRequest.add("HB");

		sqlResponse = Sql.response(sqlRequest);
		return sqlResponse;
	}

	public static void verificarEstadoMoraTempranaV4(ContextoHB contexto, List<ProductoMora> productosEnMora,
													 List<ProductoMoraDetalles> productoMoraDetalles, PrestamosV4.PrestamoV4 prestamo, Objeto item) {
		Boolean onboardingMostrado = false;
		if (productosEnMora.size() > 0 && productosEnMora.stream()
				.anyMatch(prod -> prod.numeroProducto().equals(prestamo.numeroProducto)
						&& Arrays.asList(TIPOS_MORA_TEMPRANA).contains(prod.tipoMora())
						&& CODIGO_PRODUCTO_PRESTAMO.equals(prod.prodCod().trim()))) {
			ProductoMora productoMora = productosEnMora.stream()
					.filter(prod -> prod.numeroProducto().equals(prestamo.numeroProducto)).findFirst().get();
			item.set("estadoMora", "EN_MORAT");

			if (productoMoraDetalles.size() > 0) {
				ProductoMoraDetalles detalle = productoMoraDetalles.stream()
						.filter(prod -> prod.ctaId().equals(productoMora.ctaId())).findFirst().get();
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
				verificarEstadoMoraTempranaBucketV4(contexto, detalle, prestamo, item);
			}
		} else if (!FORMA_PAGO_EFECTIVO.equals(prestamo.formasPago(prestamo.formaPago))
				&& prestamo.cuotas(contexto, prestamo).stream().anyMatch(cuota -> CUOTA_VENCIDA.equals(cuota.estado()))) {
			item.set("estadoMora", "EN_MORAT");
		}

		Boolean muestreoOnboarding = Util.tieneMuestreoNemonico(contexto, "ONBOARDING");
		item.set("onboardingMostrado", muestreoOnboarding || onboardingMostrado);

		try {
			if (!muestreoOnboarding && onboardingMostrado) {
				contexto.parametros.set("nemonico", "ONBOARDING");
				Util.contador(contexto);
			}
		} catch (Exception e) {
		}
	}

	public static void verificarEstadoMoraTempranaBucketV4(ContextoHB contexto, ProductoMoraDetalles detalle,
														   PrestamosV4.PrestamoV4 prestamo, Objeto item) {
		if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_promesa_pago")) {
			if (Arrays.asList(BUCKETS).contains(Util.bucketMora(detalle.diasEnMora(), prestamo.categoria))) {
				item.set("estadoMora", "EN_MORAT" + "_" + Util.bucketMora(detalle.diasEnMora(), prestamo.categoria));
			}
		}
	}

}
