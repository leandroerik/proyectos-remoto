package ar.com.hipotecario.canal.officebanking;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.servicio.api.delivery.ApiDelivery;
import ar.com.hipotecario.canal.officebanking.util.Texto;



public class OBDelivery extends Modulo{
	
//	private static String flagTrackeo = "prendido_trackeo_tarjetas_api";
//	private static String flagTrackeoCodificacion = "prendido_trackeo_tarjetas_api_codificacion";

	private static final String filtro_titular = "TITULAR";
	private static final String VALUE_JSON_PRODUCT_HIDDEN_NUMBER = "XXXX-";
	
	public static Objeto agregarTrackeoTarjetaDebito(ContextoOB contexto) {
		SesionOB sesionOB =  contexto.sesion();
		String dni = sesionOB.usuarioOB.numeroDocumento.toString();
		return agregarTrackeoTarjetaDebitoDni(contexto, dni);
	}
	
	public static Objeto agregarTrackeoTarjetaDebitoDni(ContextoOB contexto, String dni) {
		Boolean soloTitular	= (Boolean) contexto.parametros.get("soloTitular");

		try {
			Objeto respuesta = new Objeto();
			//if (HBAplicacion.funcionalidadPrendida(sesionOB.idCobis, flagTrackeo)) {
				
				ApiResponse response = ApiDelivery.deliveryClientes(contexto, dni,"ob");
				if (response.hayError()) {
					respuesta = obtenerRespuestaError(response);
				} else {
					List<Objeto> elementos = response.objetos();
					//List<Map<String, Object>> multiResult = new ArrayList<>();
					Map<String, Object> mapTarjetas = new HashMap<String, Object>();
					for (Objeto tdist : elementos) {
						// filtra y deja solo tarjetas categoria titular
						String catx = tdist.string("Categoria").toUpperCase().trim();
						if (soloTitular != null && soloTitular && !filtro_titular.equals(catx)) {
							continue;
						}
						Map<String, Object> oneresult = new HashMap<String, Object>();
						// PRODUCTO
						String productType = "UNKNOW";
						String productTypeText = "";
						if (tdist.string("TipoProducto") != null && tdist.string("TipoProducto").equals("Tarjeta Debito")) {
							productType = "DEBITCARD";
							productTypeText = obtenerTexto(contexto,"delivery_tipoTD_txt");
						} else if (tdist.string("TipoProducto") != null && tdist.string("TipoProducto").equals("Tarjeta Visa")) {
							productType = "CREDITCARD";
							productTypeText = obtenerTexto(contexto,"delivery_tipoTC_txt");
						}
						oneresult.put("productType", productType);
						oneresult.put("productTypeText", productTypeText);
						
						// NOMBRE EMBOZADO TARJETA
						oneresult.put("nombreEmbozadoTarjeta", tdist.string("NombreEmbozadoTarjeta"));

						// NOMBRE TITULAR
						oneresult.put("nombreTitular", tdist.string("NombreTitular"));
						
						// NUMERO TARJETA
						String tNumber = (tdist.string("IdentificadorPieza") == null) ? "" : getHiddenNumber(tdist.string("IdentificadorPieza"));
						oneresult.put("productNumber", tNumber);

						// NUMERO TARJETA
						String pNumber = (tdist.string("IdentificadorPieza") == null) ? "" : tdist.string("IdentificadorPieza");
						oneresult.put("pieceNumber", pNumber);

						// SOLICITUD
						String requestText = "";
						if (tdist.string("CodigoNovedad") != null && tdist.string("CodigoNovedad").equals("ALTA")) {
							requestText = obtenerTexto(contexto,"delivery_request_alta");
						} else if (tdist.string("CodigoNovedad") != null && (tdist.string("CodigoNovedad").equals("REPO") || tdist.string("CodigoNovedad").equals("REIM") || tdist.string("CodigoNovedad").equals("REEM") || tdist.string("CodigoNovedad").equals("ALRP") || tdist.string("CodigoNovedad").equals("ALRE"))) {
							requestText = obtenerTexto(contexto,"delivery_request_repo");
						} else if (tdist.string("CodigoNovedad") != null && tdist.string("CodigoNovedad").equals("RENO")) {
							requestText = obtenerTexto(contexto,"delivery_request_reno");
						} else if (tdist.string("CodigoNovedad") != null && (tdist.string("CodigoNovedad").equals("UPGR") || tdist.string("CodigoNovedad").equals("DWGR"))) {
							requestText = obtenerTexto(contexto,"delivery_request_cambio");
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
						if (getPendingIds(contexto).contains(Integer.toString(IdEstado))) {

							if (distributionType.equals("CLIENTE")) {
								detailText = obtenerTexto(contexto,"delivery_request_encamino");
								trackType = "CLIENTPENDING";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_deliveryInformation");
								modifyAddress = obtenerTexto(contexto,"delivery_request_modifyaddress");
								modifyAddressTel = obtenerTexto(contexto,"delivery_request_modifyaddresstel");

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto(contexto,"delivery_request_encamino");
								trackType = "BANKPENDING";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_deliveryInformation");
								modifyAddress = obtenerTexto(contexto,"delivery_request_modifyaddress");
								modifyAddressTel = obtenerTexto(contexto,"delivery_request_modifyaddresstel");

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto(contexto,"delivery_request_encamino");
								trackType = "BRANCHPENDING";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_deliveryInformation_branch");
								modifyAddress = obtenerTexto(contexto,"delivery_request_modifyaddress");
								modifyAddressTel = obtenerTexto(contexto,"delivery_request_modifyaddresstel");

							}

							// CORREO PASARA A VISITARTE
						} else if (getDepartedIds(contexto).contains(IdEstado.toString())) {
							if (distributionType.equals("CLIENTE")) {
								detailText = obtenerTexto(contexto,"delivery_request_deliveryInformation");
								trackType = "CLIENTDEPARTED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_deliveryInformation");

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto(contexto,"delivery_request_visita");
								trackType = "BANKDEPARTED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto(contexto,"delivery_request_visita");
								trackType = "BRANCHDEPARTED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							}
							// CORREO DEMORADO
						} else if (getDelayedIds(contexto).contains(IdEstado.toString())) {
							if (distributionType.equals("CLIENTE")) {
								trackType = "CLIENTDELAYED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_deliveryInformation");

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto(contexto,"delivery_request_visita");
								trackType = "BANKDELAYED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto(contexto,"delivery_request_visita");
								trackType = "BRANCHDELAYED";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							}
							// LISTO PARA RETIRAR
						} else if (getLocationIds(contexto).contains(IdEstado.toString())) {

							if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto(contexto,"delivery_request_retirar");
								trackType = "BANKLOCATION";
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto(contexto, "delivery_request_retirar");
								trackType = "BRANCHLOCATION";
								// Chequear con Producto, si por ahora dejamos que el horario venga en el titulo
								// y mas adelante agregr dato
								deliveryInformation = obtenerTexto(contexto,"delivery_request_visitadni");

							}

							// ENTREGADA
						} else if (getDeliveredIds(contexto).contains(IdEstado.toString())) {

							if (distributionType.equals("CLIENTE")) {
								trackType = "CLIENTDELIVERED";

							} else if (distributionType.equals("BANCO")) {
								detailText = obtenerTexto(contexto, "delivery_request_entregado");
								trackType = "BANKDELIVERED";

							} else if (distributionType.equals("ANDREANI")) {
								detailText = obtenerTexto(contexto,"delivery_request_entregado");
								trackType = "BRANCHDELIVERED";

							}

							// NO ENTREGADO
						} else if (getRefusedIds(contexto).contains(IdEstado.toString())) {
							detailText = obtenerTexto(contexto,"delivery_request_rechazado");
							trackType = "REFUSED";// Ocultar el modulo de informacion de entrega
							oneresult.put("refusedTitle", obtenerTexto(contexto,"delivery_request_refusedtitle"));
							oneresult.put("refusedText", obtenerTexto(contexto,"delivery_request_refusedtext"));
							oneresult.put("refusedTel", obtenerTexto(contexto,"delivery_request_refusedtel"));
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
							 * obtenerTexto(contexto,"delivery_request_unknowtitle", sesionOB.idCobis));
							 * oneresult.put("unknowText", obtenerTexto(contexto,"delivery_request_unknowtext",
							 * sesionOB.idCobis)); oneresult.put("unknowTel",
							 * obtenerTexto(contexto,"delivery_request_unknowtel", sesionOB.idCobis));
							 * 
							 * }
							 */
						}
						if (trackType.equals("UNKNOW")) {
							oneresult.put("unknowTitle", statusText);
							oneresult.put("unknowText", obtenerTexto(contexto,"delivery_request_unknowtext"));
							oneresult.put("unknowTel", obtenerTexto(contexto,"delivery_request_unknowtel"));
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
						mapTarjetas.put(pNumber.substring(pNumber.length()-4, pNumber.length()), oneresult);
					}
					respuesta.set("productsDelivery", mapTarjetas);
				}
			//} else {
			//	respuesta.set("error", "Funcionalidad no habilitada");
			//}
			return respuesta;
		} catch (Exception e) {
			return null;
		}
	}

	private static Objeto obtenerRespuestaError(ApiResponse response) {
		Objeto respuesta = new Objeto();
		if (!response.codigo("306")) {
			for (String clave : response.keys()) {
				respuesta.set(clave, response.get(clave));
			}
		} else {
			return respuesta("ERROR");
		}
		return respuesta;
	}

	private static List<String> getPendingIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_pending_id").split("_"));
	}

	private static List<String> getDepartedIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_departed_id").split("_"));
	}

	private static List<String> getDelayedIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_delayed_id").split("_"));
	}

	private static List<String> getLocationIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_location_id").split("_"));
	}

	private static List<String> getDeliveredIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_delivered_id").split("_"));
	}

	private static List<String> getRefusedIds(ContextoOB contexto) {
		return Arrays.asList(contexto.config.string("ob_delivery_refused_id").split("_"));
	}

//	private static String obtenerTexto(String clave) {
//		return obtenerTexto(clave, "0");
//	}

	private static String obtenerTexto(ContextoOB contexto,String clave) {
		String config = contexto.config.string("ob_".concat(clave));
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