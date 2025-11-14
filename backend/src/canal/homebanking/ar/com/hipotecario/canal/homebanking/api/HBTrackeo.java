package ar.com.hipotecario.canal.homebanking.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.servicio.RestDelivery;

public class HBTrackeo {

	private static String flagTrackeo = "prendido_trackeo_tarjetas_api";
	private static String flagTrackeoCodificacion = "prendido_trackeo_tarjetas_api_codificacion";

	private static final String filtro_titular = "TITULAR";
	private static final String VALUE_JSON_PRODUCT_HIDDEN_NUMBER = "XXXX-";

	public static Respuesta agregarTrackeoTarjetaDebito(ContextoHB contexto) {
		try {
			Respuesta respuesta = new Respuesta();
			if (HBAplicacion.funcionalidadPrendida(contexto.idCobis(), flagTrackeo)) {
				String dni = contexto.persona().numeroDocumento();
				ApiResponse response = RestDelivery.deliveryClientes(contexto, dni);
				if (response.hayError()) {
					respuesta = obtenerRespuestaError(response);
				} else {
					List<Objeto> elementos = response.objetos();
					List<Map<String, Object>> multiResult = new ArrayList<>();
					for (Objeto tdist : elementos) {
						// filtra y deja solo tarjetas categoria titular
						String catx = tdist.string("Categoria").toUpperCase().trim();
						if (!filtro_titular.equals(catx)) {
							continue;
						}
						Map<String, Object> oneresult = new HashMap<String, Object>();

						// PRODUCTO
						String productType = "UNKNOW";
						String productTypeText = "";
						if (tdist.string("TipoProducto") != null && tdist.string("TipoProducto").equals("Tarjeta Debito")) {
							productType = "DEBITCARD";
							productTypeText = obtenerTexto("delivery_tipoTD_txt", contexto.idCobis());
						} else if (tdist.string("TipoProducto") != null && tdist.string("TipoProducto").equals("Tarjeta Visa")) {
							productType = "CREDITCARD";
							productTypeText = obtenerTexto("delivery_tipoTC_txt", contexto.idCobis());
						}
						oneresult.put("productType", productType);
						oneresult.put("productTypeText", productTypeText);

						// NUMERO TARJETA
						String tNumber = (tdist.string("IdentificadorPieza") == null) ? "" : getHiddenNumber(tdist.string("IdentificadorPieza"));
						oneresult.put("productNumber", tNumber);

						// NUMERO TARJETA
						String pNumber = (tdist.string("IdentificadorPieza") == null) ? "" : tdist.string("IdentificadorPieza");
						oneresult.put("pieceNumber", pNumber);

						// SOLICITUD
						String requestText = "";
						if (tdist.string("CodigoNovedad") != null && tdist.string("CodigoNovedad").equals("ALTA")) {
							requestText = obtenerTexto("delivery_request_alta", contexto.idCobis());
						} else if (tdist.string("CodigoNovedad") != null && (tdist.string("CodigoNovedad").equals("REPO") || tdist.string("CodigoNovedad").equals("REIM") || tdist.string("CodigoNovedad").equals("REEM") || tdist.string("CodigoNovedad").equals("ALRP") || tdist.string("CodigoNovedad").equals("ALRE"))) {
							requestText = obtenerTexto("delivery_request_repo", contexto.idCobis());
						} else if (tdist.string("CodigoNovedad") != null && tdist.string("CodigoNovedad").equals("RENO")) {
							requestText = obtenerTexto("delivery_request_reno", contexto.idCobis());
						} else if (tdist.string("CodigoNovedad") != null && (tdist.string("CodigoNovedad").equals("UPGR") || tdist.string("CodigoNovedad").equals("DWGR"))) {
							requestText = obtenerTexto("delivery_request_cambio", contexto.idCobis());
						}
						oneresult.put("requestText", requestText);

						Integer IdEstado = tdist.integer("IdEstado", -99);
						String statusText = tdist.string("EstadoTraduccion", "");
						String detailText = "";
						String distributionType = tdist.string("EleccionDistribucion", "");
						String trackType = "UNKNOW";
						String deliveryInformation = "";
						String address = tdist.string("DomicilioCore", "");
						String modifyAddress = "";
						String modifyAddressTel = "";
						// EN PREPARACION
						if (getPendingIds().contains(Integer.toString(IdEstado))) {

							if (distributionType.equals("CLIENTE")) {
								detailText = obtenerTexto("delivery_request_encamino", contexto.idCobis());
								trackType = "CLIENTPENDING";
								deliveryInformation = obtenerTexto("delivery_request_deliveryInformation", contexto.idCobis());
								modifyAddress = obtenerTexto("delivery_request_modifyaddress", contexto.idCobis());
								modifyAddressTel = obtenerTexto("delivery_request_modifyaddresstel", contexto.idCobis());

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto("delivery_request_encamino", contexto.idCobis());
								trackType = "BANKPENDING";
								deliveryInformation = obtenerTexto("delivery_request_deliveryInformation", contexto.idCobis());
								modifyAddress = obtenerTexto("delivery_request_modifyaddress", contexto.idCobis());
								modifyAddressTel = obtenerTexto("delivery_request_modifyaddresstel", contexto.idCobis());

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto("delivery_request_encamino", contexto.idCobis());
								trackType = "BRANCHPENDING";
								deliveryInformation = obtenerTexto("delivery_request_deliveryInformation_branch", contexto.idCobis());
								modifyAddress = obtenerTexto("delivery_request_modifyaddress", contexto.idCobis());
								modifyAddressTel = obtenerTexto("delivery_request_modifyaddresstel", contexto.idCobis());

							}

							// CORREO PASARA A VISITARTE
						} else if (getDepartedIds().contains(IdEstado.toString())) {
							if (distributionType.equals("CLIENTE")) {
								detailText = obtenerTexto("delivery_request_deliveryInformation", contexto.idCobis());
								trackType = "CLIENTDEPARTED";
								deliveryInformation = obtenerTexto("delivery_request_deliveryInformation", contexto.idCobis());

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto("delivery_request_visita", contexto.idCobis());
								trackType = "BANKDEPARTED";
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto("delivery_request_visita", contexto.idCobis());
								trackType = "BRANCHDEPARTED";
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							}
							// CORREO DEMORADO
						} else if (getDelayedIds().contains(IdEstado.toString())) {
							if (distributionType.equals("CLIENTE")) {
								trackType = "CLIENTDELAYED";
								deliveryInformation = obtenerTexto("delivery_request_deliveryInformation", contexto.idCobis());

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto("delivery_request_visita", contexto.idCobis());
								trackType = "BANKDELAYED";
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto("delivery_request_visita", contexto.idCobis());
								trackType = "BRANCHDELAYED";
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							}
							// LISTO PARA RETIRAR
						} else if (getLocationIds().contains(IdEstado.toString())) {

							if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto("delivery_request_retirar", contexto.idCobis());
								trackType = "BANKLOCATION";
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto("delivery_request_retirar", contexto.idCobis());
								trackType = "BRANCHLOCATION";
								// Chequear con Producto, si por ahora dejamos que el horario venga en el titulo
								// y mas adelante agregr dato
								deliveryInformation = obtenerTexto("delivery_request_visitadni", contexto.idCobis());

							}

							// ENTREGADA
						} else if (getDeliveredIds().contains(IdEstado.toString())) {

							if (distributionType.equals("CLIENTE")) {
								trackType = "CLIENTDELIVERED";

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto("delivery_request_entregado", contexto.idCobis());
								trackType = "BANKDELIVERED";

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto("delivery_request_entregado", contexto.idCobis());
								trackType = "BRANCHDELIVERED";

							}

							// NO ENTREGADO
						} else if (getRefusedIds().contains(IdEstado.toString())) {
							detailText = obtenerTexto("delivery_request_rechazado", contexto.idCobis());
							trackType = "REFUSED";// Ocultar el modulo de informacion de entrega
							oneresult.put("refusedTitle", obtenerTexto("delivery_request_refusedtitle", contexto.idCobis()));
							oneresult.put("refusedText", obtenerTexto("delivery_request_refusedtext", contexto.idCobis()));
							oneresult.put("refusedTel", obtenerTexto("delivery_request_refusedtel", contexto.idCobis()));
							oneresult.put("address", address);

						} else {
							/*
							 * if (distributionType.equals("CLIENTE")) { trackType = "CLIENTZERO";
							 * 
							 * } else if (distributionType.equals("BANCO")) { trackType = "BANKZERO";
							 * 
							 * } else if (distributionType.equals("ANDREANI")) { trackType = "BRANCHZERO";
							 * 
							 * } else { oneresult.put("unknowTitle",
							 * obtenerTexto("delivery_request_unknowtitle", contexto.idCobis()));
							 * oneresult.put("unknowText", obtenerTexto("delivery_request_unknowtext",
							 * contexto.idCobis())); oneresult.put("unknowTel",
							 * obtenerTexto("delivery_request_unknowtel", contexto.idCobis()));
							 * 
							 * }
							 */
						}
						if (trackType.equals("UNKNOW")) {
							oneresult.put("unknowTitle", statusText);
							oneresult.put("unknowText", obtenerTexto("delivery_request_unknowtext", contexto.idCobis()));
							oneresult.put("unknowTel", obtenerTexto("delivery_request_unknowtel", contexto.idCobis()));
						}
						oneresult.put("statusText", statusText);
						oneresult.put("detailText", detailText);
						oneresult.put("distributionType", distributionType);
						oneresult.put("trackType", trackType);
						oneresult.put("deliveryInformation", deliveryInformation);
						oneresult.put("modifyAddress", modifyAddress);
						oneresult.put("modifyAddressTel", modifyAddressTel);
						// DIRECCION
						oneresult.put("address", address);
						multiResult.add(oneresult);
					}
					respuesta.set("productsDelivery", multiResult);
				}
			} else {
				respuesta.set("error", "Funcionalidad no habilitada");
			}
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	private static Respuesta obtenerRespuestaError(ApiResponse response) {
		Respuesta respuesta = new Respuesta();
		if (response.codigo != 306) {
			for (String clave : response.claves()) {
				respuesta.set(clave, response.get(clave));
			}
		} else {
			return Respuesta.estado(respuesta.string("error"));
		}
		return respuesta;
	}

	private static List<String> getPendingIds() {
		return Arrays.asList(ConfigHB.string("delivery_pending_id").split("_"));
	}

	private static List<String> getDepartedIds() {
		return Arrays.asList(ConfigHB.string("delivery_departed_id").split("_"));
	}

	private static List<String> getDelayedIds() {
		return Arrays.asList(ConfigHB.string("delivery_delayed_id").split("_"));
	}

	private static List<String> getLocationIds() {
		return Arrays.asList(ConfigHB.string("delivery_location_id").split("_"));
	}

	private static List<String> getDeliveredIds() {
		return Arrays.asList(ConfigHB.string("delivery_delivered_id").split("_"));
	}

	private static List<String> getRefusedIds() {
		return Arrays.asList(ConfigHB.string("delivery_refused_id").split("_"));
	}

//	private static String obtenerTexto(String clave) {
//		return obtenerTexto(clave, "0");
//	}

	private static String obtenerTexto(String clave, String idCobis) {
		String config = ConfigHB.string(clave);

		if (HBAplicacion.funcionalidadPrendida(idCobis, flagTrackeoCodificacion) && config != null)
			config = Texto.htmlToText(config);
		return config;
	}

	public static String getHiddenNumber(String number) {
		String hiddenNumber = "-";
		if (number != null && !number.equals("")) {
			number = number.trim();
			hiddenNumber = VALUE_JSON_PRODUCT_HIDDEN_NUMBER + number.substring(number.length() - 4);
		}
		return hiddenNumber;
	}
}
