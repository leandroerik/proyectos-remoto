package ar.com.hipotecario.canal.homebanking.api;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;
import ar.com.hipotecario.canal.homebanking.lib.Momento;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Cobertura;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Enlatado;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Producto;
import ar.com.hipotecario.canal.homebanking.negocio.seguro.Response;
import ar.com.hipotecario.canal.homebanking.servicio.RestSeguro;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class HBSeguro {

	private static String enlatadoVidaConyuge = ConfigHB.string("enlatados_habilitados_vida_conyuge");
	private static String enlatadoVidaSinConyuge = ConfigHB.string("enlatados_habilitados_vida_sin_conyuge");
	private static String enlatadoBMClienteBolso = ConfigHB.string("enlatados_habilitados_bienes_moviles_cliente_bolso");
	private static String enlatadoBMClienteElectro = ConfigHB.string("enlatados_habilitados_bienes_moviles_cliente_electro");
	private static String enlatadoBMClienteFull = ConfigHB.string("enlatados_habilitados_bienes_moviles_cliente_full");
	private static String enlatadoBMEmpleadoBolso = ConfigHB.string("enlatados_habilitados_bienes_moviles_empleado_bolso");
	private static String enlatadoBMEmpleadoElectro = ConfigHB.string("enlatados_habilitados_bienes_moviles_empleado_electro");
	private static String enlatadoBMEmpleadoFull = ConfigHB.string("enlatados_habilitados_bienes_moviles_empleado_full");
	private static String enlatadoCPCliente = ConfigHB.string("enlatados_habilitados_compra_protegida_cliente");
	private static String enlatadoCPEmpleado = ConfigHB.string("enlatados_habilitados_compra_protegida_empleado");
	private static String enlatadoSalud = ConfigHB.string("enlatados_habilitados_salud");
	private static String enlatadoSaludSenior = ConfigHB.string("enlatados_habilitados_salud_senior");
	private static String enlatadoAPCliente = ConfigHB.string("enlatados_habilitados_ap_cliente");
	private static String enlatadoAPEmpleado = ConfigHB.string("enlatados_habilitados_ap_empleado");
	private static String enlatadoAPMayores = ConfigHB.string("enlatados_habilitados_ap_mayores");
	private static String enlatadoATMCliente = ConfigHB.string("enlatados_habilitados_atm_cliente");
	private static String enlatadoATMEmpleado = ConfigHB.string("enlatados_habilitados_atm_empleado");
	private static String enlatadoMovilidadEmpleado = ConfigHB.string("enlatados_habilitados_movilidad_empleado");
	private static String enlatadoMovilidadCliente = ConfigHB.string("enlatados_habilitados_movilidad_cliente");
	private static String enlatadoMascotasEmpleado = ConfigHB.string("enlatados_habilitados_mascotas_empleado");
	private static String enlatadoMascotasdCliente = ConfigHB.string("enlatados_habilitados_mascotas_cliente");

	private static final String FORMATO_PRECIO = ConfigHB.string("formato_precio");

	public static Respuesta modalSeguro(ContextoHB contexto) {
		Boolean modalSegurosHabilitado = ConfigHB.bool("modal_seguros", false);
		String fechaLimite = ConfigHB.string("fecha_limite_modal_seguros", "01/01/2022");
		Boolean mostrar = Momento.ahora().esAnterior(new Momento(fechaLimite, "dd/MM/yyyy"));
		Respuesta respuesta = new Respuesta();
		respuesta.set("mostrar", mostrar && modalSegurosHabilitado);
		return respuesta;
	}

	public static Respuesta obtenerToken(ContextoHB contexto) {
		Objeto response = RestSeguro.token(contexto);
		if (response == null)
			return Respuesta.error();
		Objeto datos = new Objeto();
		String json = response.string("result");
		if (json != null && !json.isEmpty()) {
			Objeto idSession = Objeto.fromJson(json);
			datos.set("id", idSession.string("sessionId"));

		}

		return Respuesta.exito("sesion", datos);
	}

	public static Respuesta obtenerProductos(ContextoHB contexto) {
		String cuit = contexto.parametros.string("cuit");
		List<Objeto> productos = RestSeguro.productos(contexto, cuit);
		if (productos == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = productos.get(0);
		String json = producto.string("result.getInfoPorductosVigente.body.listInfoProductosVigentes.infoProductoVigente");
		if (json != null && !json.isEmpty()) {
			Objeto arrayDatosAdicionales = Objeto.fromJson(json);
			for (Objeto datosAdicionales : arrayDatosAdicionales.objetos()) {
				Objeto objeto = new Objeto();
				objeto.set("producto", datosAdicionales.string("producto"));
				objeto.set("productoDesc", datosAdicionales.string("productoDesc"));
				objeto.set("ramo", datosAdicionales.integer("ramo"));
				objeto.set("mtPrima", datosAdicionales.bigDecimal("mtPrima"));
				objeto.set("nroCuenta", datosAdicionales.string("nroCuenta"));
				objeto.set("nuPoliza", datosAdicionales.integer("nuPoliza"));
				objeto.set("medioPago", datosAdicionales.string("medioPago"));
				objeto.set("deOrigen", datosAdicionales.string("deOrigen"));
				objeto.set("feDesde", datosAdicionales.string("feDesde"));
				objeto.set("feHasta", datosAdicionales.string("feHasta"));
				objeto.set("certificado", datosAdicionales.string("certificado"));
				objeto.set("grupoCumulo", datosAdicionales.string("grupoCumulo"));
				objeto.set("sponsor", datosAdicionales.string("sponsor"));
				datos.add(objeto);
			}

		}
		return Respuesta.exito("productos", datos);

	}

	public static Respuesta obtenerRamoProducto(ContextoHB contexto) {
		String cuit = contexto.parametros.string("cuit");
		List<Objeto> response = RestSeguro.ramoProductos(contexto, cuit);
		if (response == null)
			return Respuesta.error();
		Objeto datos = new Objeto();
		Objeto productos = response.get(0);
		String json = productos.string("result");
		if (json != null && !json.isEmpty()) {
			Objeto respuesta = Objeto.fromJson(json);
			datos.add(respuesta);

		}

		return Respuesta.exito("respuesta", datos);
	}

	public static Respuesta obtenerOfertas(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		String ambiente = contexto.parametros.string("ambiente");
		Boolean esEmpleado = contexto.parametros.bool("esEmpleado");
		String ramoProducto = contexto.parametros.string("ramoProducto");


		List<String> idsEnlatados = new ArrayList<String>(Arrays.asList(getIdsEnlatadosPorAmbiente(ambiente, esEmpleado).split("_")));

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {

				if (oferta.string("idProductoBase").contains(ramoProducto)) {

					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {
						if (esEmpleado && item.string("idEnlatado").contains("EMP")
								&& item.string("idEnlatado").contains(ramoProducto)
								&&
								idsEnlatados.contains(item.string("codigoEnlatado"))) {
							agregarPremioFormateado(datos, item);
						} else if (!esEmpleado && item.string("idEnlatado").contains("BH")
								&& item.string("idEnlatado").contains(ramoProducto)
								&& idsEnlatados.contains(item.string("codigoEnlatado"))) {
							agregarPremioFormateado(datos, item);

						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasVida(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		Boolean esConyuge = contexto.parametros.bool("esConyuge");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<String> idsEnlatadosVidaConyuge = new ArrayList<String>(Arrays.asList(enlatadoVidaConyuge.split("_")));
		List<String> idsEnlatadosVidaSinConyuge = new ArrayList<String>(Arrays.asList(enlatadoVidaSinConyuge.split("_")));

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {

						if (esConyuge) {
							if (item.string("idEnlatado").contains("19-612") && idsEnlatadosVidaConyuge.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}

						} else {
							if (item.string("idEnlatado").contains("19-611") && idsEnlatadosVidaSinConyuge.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}
						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasSalud(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		List<String> idEnlatadosSalud = new ArrayList<String>(Arrays.asList(enlatadoSalud.split("_")));

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {

						if (item.string("idEnlatado").contains("19-609") && idEnlatadosSalud.contains(item.string("codigoEnlatado"))) {
							// TODO VER CON LIBER CUANDO VUELA
							String jsonCobertura = item.string("coberturas");
							Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
							Objeto datosCoberturas = new Objeto();
							for (Objeto itemCobertura : arrayCoberturas.objetos()) {
								if (itemCobertura.string("codigoCobertura").equals("006")) {
									BigDecimal montoCobertura = itemCobertura.bigDecimal("montoCobertura");
									BigDecimal rentaDiaria = montoCobertura.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
									Integer rentaDiariaInt = rentaDiaria.intValue();
									//itemCobertura.set("montoCobertura", rentaDiariaInt);
									itemCobertura.set("rentaDiaria", rentaDiariaInt);
									datosCoberturas.add(itemCobertura);
								} else {
									datosCoberturas.add(itemCobertura);
								}
							}

							item.set("coberturas", datosCoberturas);
							agregarPremioFormateado(datos, item);
						}
					}
				}

			}

		}

		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasSaludSenior(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}

				String jsonEnlatado = oferta.string("enlatados");

				if (json != null && !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {
						List<String> idEnlatadosSaludSenior = new ArrayList<String>(Arrays.asList(enlatadoSaludSenior.split("_")));

						if (item.string("idEnlatado").contains("20-006") && idEnlatadosSaludSenior.contains(item.string("codigoEnlatado"))) {
							String jsonCobertura = item.string("coberturas");
							Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
							Objeto datosCoberturas = new Objeto();

							for (Objeto itemCobertura : arrayCoberturas.objetos()) {

								String montoCobertura = itemCobertura.string("montoCobertura");
								if (montoCobertura != null && !montoCobertura.isEmpty()) {
									BigDecimal montoCoberturaBD = new BigDecimal(montoCobertura);
									BigDecimal montoCoberturaBDRounding = montoCoberturaBD.setScale(0, RoundingMode.DOWN);
									String montoCoberturaFormateado = formatearBigDecimal(montoCoberturaBDRounding, FORMATO_PRECIO);
									BigDecimal rentaDiaria = montoCoberturaBD.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
									Integer rentaDiariaInt = rentaDiaria.intValue();
									itemCobertura.set("rentaDiaria", rentaDiariaInt);
									if (!montoCoberturaFormateado.contains(","))
										itemCobertura.set("montoCoberturaFormateado", montoCoberturaFormateado + ",00");
									else
										itemCobertura.set("montoCoberturaFormateado", montoCoberturaFormateado);
								}
								datosCoberturas.add(itemCobertura);

							}

							item.set("coberturas", datosCoberturas);
							agregarPremioFormateado(datos, item);
						}
					}
				}
			}
		}

		return Respuesta.exito("ofertas", datos);
		//return Respuesta.exito("ofertas", "hola llego bien");
	}


	public static Respuesta obtenerOfertasAP(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		Boolean esEmpleado = contexto.parametros.bool("esEmpleado");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {

						if (esEmpleado) {
							List<String> idEnlatadosAPEmpleado = new ArrayList<String>(Arrays.asList(enlatadoAPEmpleado.split("_")));
							if (item.string("idEnlatado").contains("18-202") && idEnlatadosAPEmpleado.contains(item.string("codigoEnlatado"))) {

								String jsonCobertura = item.string("coberturas");
								Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
								Objeto datosCoberturas = new Objeto();
								for (Objeto itemCobertura : arrayCoberturas.objetos()) {
									if (itemCobertura.string("codigoCobertura").equals("003")) {
										BigDecimal montoCobertura = itemCobertura.bigDecimal("montoCobertura");
										BigDecimal rentaDiaria = montoCobertura.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
										Integer rentaDiariaInt = rentaDiaria.intValue();
										//itemCobertura.set("montoCobertura", rentaDiariaInt);
										itemCobertura.set("rentaDiaria", rentaDiariaInt);
										datosCoberturas.add(itemCobertura);
									} else {
										datosCoberturas.add(itemCobertura);
									}
								}

								item.set("coberturas", datosCoberturas);
								agregarPremioFormateado(datos, item);
							}

						} else {
							List<String> idEnlatadosAPCliente = new ArrayList<String>(Arrays.asList(enlatadoAPCliente.split("_")));
							if (item.string("idEnlatado").contains("18-201") && idEnlatadosAPCliente.contains(item.string("codigoEnlatado"))) {

								String jsonCobertura = item.string("coberturas");
								Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
								Objeto datosCoberturas = new Objeto();
								for (Objeto itemCobertura : arrayCoberturas.objetos()) {
									if (itemCobertura.string("codigoCobertura").equals("003")) {
										BigDecimal montoCobertura = itemCobertura.bigDecimal("montoCobertura");
										BigDecimal rentaDiaria = montoCobertura.divide(new BigDecimal("365"), 2, RoundingMode.HALF_UP);
										Integer rentaDiariaInt = rentaDiaria.intValue();
										//itemCobertura.set("montoCobertura", rentaDiariaInt);
										itemCobertura.set("rentaDiaria", rentaDiariaInt);
										datosCoberturas.add(itemCobertura);
									} else {
										datosCoberturas.add(itemCobertura);
									}
								}

								item.set("coberturas", datosCoberturas);
								agregarPremioFormateado(datos, item);
							}
						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasAPMayores(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {
						List<String> idEnlatadosAPCliente = new ArrayList<String>(Arrays.asList(enlatadoAPMayores.split("_")));
						if (item.string("idEnlatado").contains("18-217") && idEnlatadosAPCliente.contains(item.string("codigoEnlatado"))) {

							String jsonCobertura = item.string("coberturas");
							Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
							Objeto datosCoberturas = new Objeto();
							for (Objeto itemCobertura : arrayCoberturas.objetos()) {

								String montoCobertura = itemCobertura.string("montoCobertura");
								if (montoCobertura != null && !montoCobertura.isEmpty()){
									BigDecimal montoCoberturaBD = new BigDecimal(montoCobertura);
									BigDecimal montoCoberturaBDRounding = montoCoberturaBD.setScale(0, RoundingMode.DOWN);
									String montoCoberturaFormateado = formatearBigDecimal(montoCoberturaBDRounding, FORMATO_PRECIO);

									if (!montoCoberturaFormateado.contains(","))
										itemCobertura.set("montoCoberturaFormateado", montoCoberturaFormateado + ",00");
									 else
										itemCobertura.set("montoCoberturaFormateado", montoCoberturaFormateado);
								}

								datosCoberturas.add(itemCobertura);
							}

							item.set("coberturas", datosCoberturas);
							agregarPremioFormateado(datos, item);
						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasBM(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		Boolean esEmpleado = contexto.parametros.bool("esEmpleado");
		String ramoProducto = contexto.parametros.string("ramoProducto");
		String tipo = contexto.parametros.string("tipo");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {

			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {
						if (item.string("idEnlatado").contains(ramoProducto)) {

							switch (tipo) {
							case "bolso":
								if (esEmpleado) {
									List<String> idsEnlatadoBMEmpleadosBolso = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoBolso.split("_")));
									if (item.string("idEnlatado").contains("9-222") && idsEnlatadoBMEmpleadosBolso.contains(item.string("codigoEnlatado"))) {
										agregarPremioFormateado(datos, item);
									}
								} else {
									List<String> idsEnlatadoBMClientesBolso = new ArrayList<String>(Arrays.asList(enlatadoBMClienteBolso.split("_")));
									if (item.string("idEnlatado").contains("9-224") && idsEnlatadoBMClientesBolso.contains(item.string("codigoEnlatado"))) {

										// TODO VER CON LIBER CUANDO VUELA
										String jsonCobertura = item.string("coberturas");
										Objeto arrayCoberturas = Objeto.fromJson(jsonCobertura);
										Objeto datosCoberturas = new Objeto();
										for (Objeto itemCobertura : arrayCoberturas.objetos()) {
											if (itemCobertura.string("codigoCobertura").equals("010") || itemCobertura.string("codigoCobertura").equals("009")) {
												datosCoberturas.add(itemCobertura);
											}
										}

										item.set("coberturas", datosCoberturas);
										agregarPremioFormateado(datos, item);
									}
								}
								break;
							case "electro":
								if (esEmpleado) {
									List<String> idsEnlatadoBMEmpleadosElectro = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoElectro.split("_")));
									if (item.string("idEnlatado").contains("9-222") && idsEnlatadoBMEmpleadosElectro.contains(item.string("codigoEnlatado"))) {
										agregarPremioFormateado(datos, item);
									}

								} else {
									List<String> idsEnlatadoBMClientesElectro = new ArrayList<String>(Arrays.asList(enlatadoBMClienteElectro.split("_")));
									if (item.string("idEnlatado").contains("9-224") && idsEnlatadoBMClientesElectro.contains(item.string("codigoEnlatado"))) {
										agregarPremioFormateado(datos, item);
									}
								}

								break;
							case "full":
								if (esEmpleado) {
									List<String> idsEnlatadoBMEmpleadosFull = new ArrayList<String>(Arrays.asList(enlatadoBMEmpleadoFull.split("_")));
									if (item.string("idEnlatado").contains("9-222") && idsEnlatadoBMEmpleadosFull.contains(item.string("codigoEnlatado"))) {
										agregarPremioFormateado(datos, item);
									}
								} else {
									List<String> idsEnlatadoBMClientesFull = new ArrayList<String>(Arrays.asList(enlatadoBMClienteFull.split("_")));
									if (item.string("idEnlatado").contains("9-224") && idsEnlatadoBMClientesFull.contains(item.string("codigoEnlatado"))) {
										agregarPremioFormateado(datos, item);
									}

								}
								break;
							default:
								datos.add("", item);

							}

						}

					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasCompraProtegida(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		Boolean esEmpleado = contexto.parametros.bool("esEmpleado");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<String> idsEnlatadoCPClientes = new ArrayList<String>(Arrays.asList(enlatadoCPCliente.split("_")));
		List<String> idsEnlatadoCPEmpleados = new ArrayList<String>(Arrays.asList(enlatadoCPEmpleado.split("_")));

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {

						if (esEmpleado) {
							if (item.string("idEnlatado").contains("17-202") && idsEnlatadoCPEmpleados.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}

						} else if (!esEmpleado) {
							if (item.string("idEnlatado").contains("17-201") && idsEnlatadoCPClientes.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}
						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasATM(ContextoHB contexto) {
		String sessionId = contexto.parametros.string("sessionId");
		Boolean esEmpleado = contexto.parametros.bool("esEmpleado");
		String ramoProducto = contexto.parametros.string("ramoProducto");

		List<Objeto> ofertas = RestSeguro.ofertas(contexto, sessionId);
		if (ofertas == null)
			return Respuesta.error();

		Objeto datos = new Objeto();
		Objeto producto = ofertas.get(0);
		String json = producto.string("result.productos");
		if (json != null && !json.isEmpty()) {
			Objeto arrayOfertas = Objeto.fromJson(json);
			for (Objeto oferta : arrayOfertas.objetos()) {
				if (oferta.string("idProductoBase").contains(ramoProducto)) {
					datos.add("descripcionRamo", oferta.string("descripcionRamo"));
					datos.add("descripcionProducto", oferta.string("descripcionProducto"));
				}
				String jsonEnlatado = oferta.string("enlatados");

				if (json != null & !json.isEmpty()) {
					Objeto arrayEnlatados = Objeto.fromJson(jsonEnlatado);
					for (Objeto item : arrayEnlatados.objetos()) {

						if (esEmpleado) {
							List<String> idEnlatadosATMEmpleado = new ArrayList<String>(Arrays.asList(enlatadoATMEmpleado.split("_")));
							if (item.string("idEnlatado").contains("9-202") && idEnlatadosATMEmpleado.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}

						} else {
							List<String> idEnlatadosATMCliente = new ArrayList<String>(Arrays.asList(enlatadoATMCliente.split("_")));
							if (item.string("idEnlatado").contains("9-201") && idEnlatadosATMCliente.contains(item.string("codigoEnlatado"))) {
								agregarPremioFormateado(datos, item);
							}
						}
					}

				}

			}
		}
		return Respuesta.exito("ofertas", datos);
	}

	public static Respuesta obtenerOfertasMovilidad( ContextoHB contextoHB ) {
		List<String> codigosEnlatadosHabilitadosEmpleado = new ArrayList<>(Arrays.asList( enlatadoMovilidadEmpleado.split("_") ));
		List<String> codigosEnlatadosHabilitadosCliente = new ArrayList<>(Arrays.asList( enlatadoMovilidadCliente.split("_") ));

		Boolean esEmpleado = contextoHB.parametros.bool( "esEmpleado" );
		String ramoProducto = contextoHB.parametros.string("ramoProducto");
		String sessionId = contextoHB.parametros.string("sessionId");

		List<Objeto> response = RestSeguro.ofertas( contextoHB, sessionId );
		if ( response == null ) { return Respuesta.error(); }

		Objeto ofertasCompleto = response.get(0);
		Objeto ofertaFinal = new Objeto();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Response ofertas = mapper.readValue( ofertasCompleto.toString(), Response.class );
			List<Producto> productos = ofertas.getResult().getProductos();

			if ( !productos.isEmpty() ) {
				for ( Producto producto : productos ) {
					if( producto.getIdProductoBase().equals( ramoProducto ) ) {
						for ( Enlatado enlatado : producto.getEnlatados() ) {
							BigDecimal premio = enlatado.getPremio();
							String premioFormateado = formatearBigDecimal(premio, FORMATO_PRECIO);
							if (!premioFormateado.contains(",")) {
								enlatado.setPremioFormateado(premioFormateado + ",00");
							} else enlatado.setPremioFormateado(premioFormateado);
							if(esEmpleado){
								if( codigosEnlatadosHabilitadosEmpleado.contains( enlatado.getCodigoEnlatado() ) ) {
									for (Cobertura cobertura : enlatado.getCoberturas()){
										BigDecimal montoCobertura = cobertura.getMontoCobertura();
										String montoCoberturaFormateado = formatearBigDecimal(montoCobertura, FORMATO_PRECIO);
										if (!montoCoberturaFormateado.contains(",")) {
											cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado + ",00");
										} else cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado);
									}
									ofertaFinal.add("enlatados", enlatado);
								}
							}
							else {
								if( codigosEnlatadosHabilitadosCliente.contains( enlatado.getCodigoEnlatado() ) ) {
									for (Cobertura cobertura : enlatado.getCoberturas()){
										BigDecimal montoCobertura = cobertura.getMontoCobertura();
										String montoCoberturaFormateado = formatearBigDecimal(montoCobertura, FORMATO_PRECIO);
										cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado);
										if (!montoCoberturaFormateado.contains(",")) {
											cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado + ",00");
										}
									}
									ofertaFinal.add("enlatados", enlatado);
								}
							}
						}
					}
				}
			}

		} catch ( JsonProcessingException e ) {
			return Respuesta.error();
		}

		return Respuesta.exito( "ofertas", ofertaFinal );
	}

	public static Respuesta obtenerOfertasMascotas( ContextoHB contextoHB ) {
		if("true".equals(ConfigHB.string("apagar_ofertas_mascotas")))
			return Respuesta.exito( "ofertas", "Esta apagado el servicio desde la variable de entorno en backend(apagar_ofertas_mascotas)" );
		List<String> codigosEnlatadosHabilitadosEmpleado = new ArrayList<>(Arrays.asList( enlatadoMascotasEmpleado.split("_") ));
		List<String> codigosEnlatadosHabilitadosCliente = new ArrayList<>(Arrays.asList( enlatadoMascotasdCliente.split("_") ));

		Boolean esEmpleado = contextoHB.parametros.bool( "esEmpleado" );
		String ramoProducto = contextoHB.parametros.string("ramoProducto");

		String sessionId = contextoHB.parametros.string("sessionId");

		List<Objeto> response = RestSeguro.ofertas( contextoHB, sessionId );
		if ( response == null ) { return Respuesta.error(); }

		Objeto ofertasCompleto = response.get(0);
		Objeto ofertaFinal = new Objeto();
		ObjectMapper mapper = new ObjectMapper();

		try {
			Response ofertas = mapper.readValue( ofertasCompleto.toString(), Response.class );
			List<Producto> productos = ofertas.getResult().getProductos();

			if ( !productos.isEmpty() ) {
				for ( Producto producto : productos ) {
					if( producto.getIdProductoBase().equals( ramoProducto ) ) {
						for ( Enlatado enlatado : producto.getEnlatados() ) {
							BigDecimal premio = enlatado.getPremio();
							String premioFormateado = formatearBigDecimal(premio, FORMATO_PRECIO);
							if (!premioFormateado.contains(",")) {
								enlatado.setPremioFormateado(premioFormateado + ",00");
							} else enlatado.setPremioFormateado(premioFormateado);
							if(esEmpleado){
								if( codigosEnlatadosHabilitadosEmpleado.contains( enlatado.getCodigoEnlatado() ) ) {
									for (Cobertura cobertura : enlatado.getCoberturas()){
										BigDecimal montoCobertura = cobertura.getMontoCobertura();
										String montoCoberturaFormateado = formatearBigDecimal(montoCobertura, FORMATO_PRECIO);
										if (!montoCoberturaFormateado.contains(",")) {
											cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado + ",00");
										} else cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado);
									}
									ofertaFinal.add("enlatados", enlatado);
								}
							}
							else {
								if( codigosEnlatadosHabilitadosCliente.contains( enlatado.getCodigoEnlatado() ) ) {
									for (Cobertura cobertura : enlatado.getCoberturas()){
										BigDecimal montoCobertura = cobertura.getMontoCobertura();
										String montoCoberturaFormateado = formatearBigDecimal(montoCobertura, FORMATO_PRECIO);
										cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado);
										if (!montoCoberturaFormateado.contains(",")) {
											cobertura.setMontoCoberturaFormateado(montoCoberturaFormateado + ",00");
										}
									}
									ofertaFinal.add("enlatados", enlatado);
								}
							}
						}
					}
				}
			}

		} catch ( JsonProcessingException e ) {
			return Respuesta.error();
		}

		return Respuesta.exito( "ofertas", ofertaFinal );
	}

	public static Respuesta leads(ContextoHB contexto) {

		String cuit = contexto.parametros.string("cuit");
		String sessionId = contexto.parametros.string("sessionId");

		Objeto response = RestSeguro.leads(contexto, cuit, sessionId);
		if (response == null)
			return Respuesta.error();
		Objeto datos = new Objeto();
		String json = response.string("result");
		if (json != null && !json.isEmpty()) {
			Objeto respuesta = Objeto.fromJson(json);
			datos.add(respuesta);

		}

		return Respuesta.exito("respuesta", datos);

	}

	public static Respuesta actualizarLeads(ContextoHB contexto) {

		String cuit = contexto.parametros.string("cuit");
		String sessionId = contexto.parametros.string("sessionId");
		String leadId = contexto.parametros.string("leadId");

		Objeto response = RestSeguro.actualizarLeads(contexto, cuit, sessionId, leadId);
		if (response == null)
			return Respuesta.error();
		Objeto datos = new Objeto();
		String json = response.string("result");
		if (json != null && !json.isEmpty()) {
			Objeto respuesta = Objeto.fromJson(json);
			datos.add(respuesta);

		}
		new Futuro<>(() -> {
			if(HBSalesforce.prendidoSalesforce(contexto.idCobis()) && contexto.parametros.existe("Ambiente"))
				contexto.sesion.cache.put("salesforce_seguro_ambiente", contexto.parametros.string("Ambiente"));
			return true;
		});
		

		return Respuesta.exito("respuesta", datos);

	}

	public static Respuesta insertarEmisionOnline(ContextoHB contexto) {
		Objeto response = RestSeguro.insertEmisionOnline(contexto);
		if (response == null)
			return Respuesta.error();
		Objeto datos = new Objeto();
		String json = response.string("result");
		if (json != null && !json.isEmpty()) {
			Objeto respuesta = Objeto.fromJson(json);
			datos.add(respuesta);
		}

		return Respuesta.exito("respuesta", datos);

	}

 	/** Versión 2 del método para insertar una emisión en línea.
 		*
 		* Esta versión está diseñada para manejar el error 504 ("Read timed out")
 		* que puede ocurrir durante la operación de inserción. Esto permite al front-end identificar
 		* y manejar de manera específica el caso de tiempo de espera agotado.
 		*
 		* Comportamiento:
 		* - Si la respuesta contiene un resultado válido y la transacción es exitosa,
 		* 	el método devuelve una respuesta de exito 200 OK con un estado interno "0".
 		* - Si la respuesta es válida pero contiene un mensaje de error en la transacción,
 		* 	el método devuelve una resúesta de exito 200 OK con un estado interno "1".
 		* - Si la respuesta indica un error 504 ("Read timed out"), el método devuelve una respuesta 200 OK
 		*  con un código de error 504 y un estado interno "2"
 		* - Si la respuesta es nula o no se reconoce, se devuelve un error genérico.
 	*/
	public static Respuesta insertarEmisionOnlineV2( ContextoHB contexto ) {
		Objeto datos = new Objeto();
		Objeto response;

		response = RestSeguro.insertEmisionOnlineV2( contexto );
		if (response == null)
			return Respuesta.error();

		String result = response.string("result");
		if( result != null && !result.isEmpty() ) {
			new Futuro<>(() -> {
				if (HBSalesforce.prendidoSalesforceAmbienteBajoConFF(contexto)) {
					
					Objeto parametros = new Objeto();
					parametros.set("IDCOBIS", contexto.idCobis());
					parametros.set("NOMBRE", contexto.persona().nombre());
					parametros.set("APELLIDO", contexto.persona().apellido());
					parametros.set("CANAL", "Home Banking");

					Objeto seguro =  new Objeto();				
					seguro.mapa = Map.ofEntries(
						    Map.entry("2-202", "HOGAR (EMPLEADO)"),
						    Map.entry("2-201", "HOGAR"),
						    Map.entry("9-222", "BIENES MÓVILES (EMPLEADO)"),
						    Map.entry("9-224", "BIENES MÓVILES"),
						    Map.entry("9-202", "ROBO EN CAJERO (EMPLEADO)"),
						    Map.entry("9-201", "ROBO EN CAJERO"),
						    Map.entry("9-402", "MOVILIDAD (EMPLEADO)"),
						    Map.entry("9-401", "MOVILIDAD"),
						    Map.entry("17-202", "COMPRA PROTEGIDA (EMPLEADO)"),
						    Map.entry("17-201", "COMPRA PROTEGIDA"),
						    Map.entry("18-202", "ACCIDENTE PERSONAL (EMPLEADO)"),
						    Map.entry("18-201", "ACCIDENTE PERSONAL"),
						    Map.entry("18-217", "ACCIDENTE PERSONAL SENIOR"),
						    Map.entry("19-612", "VIDA (EMPLEADO)"),
						    Map.entry("19-611", "VIDA"),
						    Map.entry("19-609", "SALUD"),
						    Map.entry("21-003", "MASCOTA (EMPLEADO)"),
						    Map.entry("21-002", "MASCOTA")
						);
					
					String ramo = contexto.parametros.string("ramo");
					String producto = contexto.parametros.string("producto");
					System.out.println(contexto.parametros);
					String ramoProducto = ramo.concat("-").concat(producto);
					String tipoBien = "";
					String nombreBien = "";
					parametros.set("NOMBRE_SEGURO", seguro.mapa.get(ramoProducto));
					if(ramo.equals("2")) {
						String domicilio = contexto.parametros.string("emiDeCalleEmi") + 
								" " + contexto.parametros.string("emiDeCalleEmi") + ",";
						if(contexto.parametros.string("emiDePisoEmi") != null)
							domicilio += " " + contexto.parametros.string("emiDePisoEmi");
						if(contexto.parametros.string("emiDeDtoEmi") != null)
							domicilio += " " + contexto.parametros.string("emiDeDtoEmi");
						
						domicilio += " " + contexto.parametros.string("emiDeDesLocalidadEmi");
						parametros.set("DOMICILIO_DECLARADO", domicilio);
						tipoBien = contexto.parametros.string("tipoVivienda");
						
						nombreBien = contexto.parametros.string("tipoVivienda").equals("1") ? 
								"Barrio Privado" : contexto.parametros.string("tipoVivienda").equals("2") ? "Casa" : "Departamento";
					}
					//Necesita desarrollo front
					if(contexto.sesion.cache.containsKey("salesforce_seguro_ambiente")) {
						parametros.set("CANTIDAD_AMBIENTES", contexto.sesion.cache.get("salesforce_seguro_ambiente"));
						contexto.sesion.cache.remove("salesforce_seguro_ambiente");
					}
					
					if(ramo.equals("21")) {
						String tipoMascota = contexto.parametros.string("tipoMascota").equals("1") ? "Perro" : "Gato";
						tipoBien = contexto.parametros.string("tipoMascota");
						parametros.set("TIPO_MASCOTA",  tipoMascota);
						parametros.set("RAZA",  contexto.parametros.string("raza", ""));
						parametros.set("NOMBRE_MASCOTA",  contexto.parametros.string("nombre", ""));
					}
					
					if(ramo.equals("9")) {
						if(producto.equals("401") || producto.equals("402")) {
							nombreBien = contexto.parametros.string("tipoMovilidad").equals("1") ? "Bicicleta" : "Monopatín";
							tipoBien = contexto.parametros.string("tipoMovilidad");
						}
						
						parametros.set("MARCA",  contexto.parametros.string("marca", ""));
						parametros.set("MODELO", contexto.parametros.string("modelo", ""));
					}
						
					parametros.set("NOMBRE_BIEN", nombreBien);
					parametros.set("TIPO_BIEN_ASEGURADO", tipoBien);
					parametros.set("PLAN_ID", contexto.parametros.string("emiPlanEmi", ""));
					parametros.set("PLAN_MONTO", contexto.parametros.string("premioOrigen", ""));

					Date hoy = new Date();
					parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
					parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
					HBSalesforce.registrarEventoSalesforce(contexto, ConfigHB.string("salesforce_alta_seguros"), parametros);
				}
				return true;
			});

			datos.add( Objeto.fromJson(result) );
			return Respuesta.exito("respuesta", datos);
		}

		result = response.string("codigo");
		if(  result != null && result.equals("504") ) {
			String jsonString = "{ \"codigo\": \"" + result + "\" }";
			datos.add( Objeto.fromJson(jsonString) );
			return Respuesta.exito("respuesta", datos).setEstado("2");
		}

		return Respuesta.error();
	}

	private static void agregarPremioFormateado(Objeto datos, Objeto item) {
		BigDecimal premio = item.bigDecimal("premio");
		if (premio != null) {
			item.set("premio", premio.setScale(2, RoundingMode.HALF_UP));
			item.set("premioFormateado", Formateador.importe(premio.setScale(2, RoundingMode.HALF_UP)));
		}
		datos.add("enlatados", item);
	}

	private static String getIdsEnlatadosPorAmbiente(String ambiente, Boolean esEmpleado) {
		String tipo = esEmpleado ? "empleado" : "cliente";
		return ConfigHB.string("enlatados_habilitados_hogar_ambiente_" + ambiente + "_" + tipo);
	}

	/**
	 * Formatea un BigDecimal usando el formato especificado.
	 * @param monto El BigDecimal a formatear.
	 * @param formato El formato
	 * @return El BigDecimal formateado como una cadena.
	 */
	private static String formatearBigDecimal(BigDecimal monto, String formato) {
		DecimalFormat df = new DecimalFormat(formato);
		return df.format(monto);
	}
}
