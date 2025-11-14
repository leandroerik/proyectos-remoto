package ar.com.hipotecario.mobile.servicio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.api.MBCatalogo;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Prestamo;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;

public class RestPostventa {
	private static String CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC = "137";
	private static String CODIDO_TIPIFICACION_PROMO_NO_IMPACTADA_TD = "121"; // Reclamo: 121+R, Consulta: 121+C
	private static String CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD = "108"; // Reclamo: 108+R, Consulta: 108+C
	private final static String SP_EXEC_INSERTAR_REGISTRO_CRM_PWP = "[Mobile].[dbo].[sp_InsertarRegistroCRMdePWP]";
	private final static String SP_EXEC_CONSULTA_CASHBACK_CANJEADO = "[Mobile].[dbo].[sp_ConsultarCashback]";

	public static ApiResponseMB bajaTarjetaCreditoAdicional(ContextoMB contexto, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", "BAJA_TC_ADICIONAL_PEDIDO");
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", "BAJA_TC_ADICIONAL_PEDIDO");
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTC = new Objeto();
			numeroTC.set("nombreAtributo", "numerotc");
			numeroTC.set("Valor", tc.numero());
			atrList.add(numeroTC);

			Objeto nombreTitular = new Objeto();
			nombreTitular.set("nombreAtributo", "nombreTitular");
			nombreTitular.set("Valor", tc.denominacionTarjeta().trim());
			atrList.add(nombreTitular);

			Objeto numeroCuenta = new Objeto();
			numeroCuenta.set("nombreAtributo", "numdecuenta");
			numeroCuenta.set("Valor", tc.cuenta());
			atrList.add(numeroCuenta);

			Objeto estado = new Objeto();
			estado.set("nombreAtributo", "estadotc");
			estado.set("Valor", tc.idEstado());
			atrList.add(estado);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB obtenerCasos(ContextoMB contexto) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ApiRequestMB request = ApiMB.request("V1ObtenerCasosGet", "postventa", "GET", "/ObtenerCasos", contexto);
		Integer diasNotififacion = ConfigMB.integer("cantidadDiasNotificacion", 45);
		Date currentDate = new Date(System.currentTimeMillis());
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, -diasNotififacion);
		Date startDate = c.getTime();

		request.query("numerocuil", contexto.persona().cuit());
		request.query("vigencia", "Resuelto");
		request.query("fechadesde", dateFormat.format(startDate));
		request.query("fechahasta", dateFormat.format(currentDate));
		request.query("filtrarnotifborrada", "true");
		request.query("adjuntos", "true");

		request.cacheSesion = true;
		return ApiMB.response(request, "Resuelto");
	}

	public static ApiResponseMB bajaTarjetaCreditoAdicional(ContextoMB contexto, String codigotipificacion, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion); // BAJA_TC_ADICIONAL_PEDIDO
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTC = new Objeto();
			numeroTC.set("nombreAtributo", "numerotc");
			numeroTC.set("Valor", tc.numero());
			atrList.add(numeroTC);

			Objeto nombreTitular = new Objeto();
			nombreTitular.set("nombreAtributo", "nombreTitular");
			nombreTitular.set("Valor", tc.denominacionTarjeta().trim());
			atrList.add(nombreTitular);

			Objeto numeroCuenta = new Objeto();
			numeroCuenta.set("nombreAtributo", "numdecuenta");
			numeroCuenta.set("Valor", tc.cuenta());
			atrList.add(numeroCuenta);

			Objeto estado = new Objeto();
			estado.set("nombreAtributo", "estadotc");
			estado.set("Valor", tc.idEstado());
			atrList.add(estado);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB bajaPaquete(ContextoMB contexto, String codigotipificacion, String idPaquete) {
		try {

			// me aseguro de estar utilizando el paquete que corresponde a numeroPaquete
			// recibido
			Objeto paq = new Objeto();
			ApiResponseMB prods = ProductosService.productos(contexto, false);
			List<Objeto> paquetes = prods.objetos("productos");
			for (Objeto paquete : paquetes) {
				if (paquete.get("numero").equals(idPaquete)) {
					paq = paquete;
					break;
				}
			}

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroPaquete = new Objeto();
			numeroPaquete.set("nombreAtributo", "numeroPaquete");
			numeroPaquete.set("Valor", paq.get("descPaquete") + " Nro: " + paq.get("numero"));
			atrList.add(numeroPaquete);

			Objeto nombre = new Objeto();
			nombre.set("nombreAtributo", "nombre");
			nombre.set("Valor", paq.get("numero"));
			atrList.add(nombre);

			Objeto estado = new Objeto();
			estado.set("nombreAtributo", "estado");
			estado.set("Valor", paq.get("estado"));
			atrList.add(estado);

			Objeto titularidad = new Objeto();
			titularidad.set("nombreAtributo", "titularidad");
			titularidad.set("Valor", paq.get("tipoTitularidad"));
			atrList.add(titularidad);

			request.body("Atributos", atrList);
			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB bajaDebitoAutomatico(ContextoMB contexto, String codigotipificacion, String cuenta, String codigoAdhesion, String cuitEmpresa) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto codAdhesion = new Objeto();
			codAdhesion.set("nombreAtributo", "CodigodeAdhesion");
			codAdhesion.set("Valor", codigoAdhesion);
			atrList.add(codAdhesion);

			Objeto cuit = new Objeto();
			cuit.set("nombreAtributo", "CUIT");
			cuit.set("Valor", cuitEmpresa);
			atrList.add(cuit);

			if ("BAJA_ADHESION_CA_PEDIDO".equals(codigotipificacion)) {
				Objeto cajaAhorro = new Objeto();
				cajaAhorro.set("nombreAtributo", "NumerodeCajadeAhorro");
				cajaAhorro.set("Valor", cuenta);
				atrList.add(cajaAhorro);
			} else { // BAJA_ADHESION_CC_PEDIDO
				Objeto cuentaCorriente = new Objeto();
				cuentaCorriente.set("nombreAtributo", "NumerodeCuentaCorriente");
				cuentaCorriente.set("Valor", cuenta);
				atrList.add(cuentaCorriente);
			}

			request.body("Atributos", atrList);
			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB reposicionTD(ContextoMB contexto, String idTarjetaDebito, String motivo, String estadoPieza, String codigoDistribucion) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", "7");
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", "7");
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTD = new Objeto();
			numeroTD.set("nombreAtributo", "NumeroDeTD");
			numeroTD.set("Valor", idTarjetaDebito);
			atrList.add(numeroTD);

			Objeto motivoRepo = new Objeto();
			motivoRepo.set("nombreAtributo", "MotivosPedidosDeTD");
			motivoRepo.set("Valor", motivo);
			atrList.add(motivoRepo);

			Objeto domicilioDeEntregaTD = new Objeto();
			domicilioDeEntregaTD.set("nombreAtributo", "DomicilioDeEntregaTD");
			domicilioDeEntregaTD.set("Valor", "DP");
			atrList.add(domicilioDeEntregaTD);

			Objeto estadoPiezaRepo = new Objeto();
			estadoPiezaRepo.set("nombreAtributo", "estadoPieza");
			estadoPiezaRepo.set("Valor", estadoPieza.isEmpty() ? "404" : estadoPieza);
			atrList.add(estadoPiezaRepo);

			Objeto codigoDistribucionRepo = new Objeto();
			codigoDistribucionRepo.set("nombreAtributo", "codigodistribucion");
			codigoDistribucionRepo.set("Valor", codigoDistribucion.isEmpty() ? "404" : codigoDistribucion);
			atrList.add(codigoDistribucionRepo);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB cambioFormaPagoPrestamo(ContextoMB contexto, String codigotipificacion, Prestamo prestamo, Objeto formaPago, Cuenta cuentaPago) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			// DetallePrestamo
			Objeto numeroprod = new Objeto();
			numeroprod.set("nombreAtributo", "numeroproducto");

			Objeto valorNumProd = new Objeto();
			valorNumProd.set("Titulo", prestamo.categoria() + " - " + prestamo.numero());
			valorNumProd.set("Valor", "\"" + prestamo.numero() + "\"");
			valorNumProd.setNull("Padre");
			valorNumProd.set("PorDefecto", false);
			valorNumProd.set("Valor2", prestamo.idTitularidad());
			valorNumProd.set("Valor3", prestamo.idEstado());
			valorNumProd.set("Valor4", prestamo.categoria());
			valorNumProd.set("Valor5", prestamo.idFormaPago());
			valorNumProd.set("Valor6", prestamo.descripcionFormaPago());
			valorNumProd.set("Valor7", Integer.parseInt(prestamo.idMoneda()));
			valorNumProd.set("Valor8", prestamo.codigo());
			valorNumProd.set("Valor9", prestamo.estado());
			valorNumProd.setNull("Valor10");
			valorNumProd.setNull("Valor11");
			valorNumProd.setNull("Valor12");
			valorNumProd.setNull("Valor13");
			valorNumProd.setNull("Valor14");
			valorNumProd.setNull("Valor15");

			numeroprod.set("Valor", valorNumProd);
			atrList.add(numeroprod);

			// FormaPagoActual
			Objeto formaPagoActual = new Objeto();
			formaPagoActual.set("nombreAtributo", "formapagoactual");

			Objeto valorFormaPagoActual = new Objeto();
			valorFormaPagoActual.set("Titulo", prestamo.descripcionFormaPago());
			valorFormaPagoActual.set("Valor", prestamo.idFormaPago());
			valorFormaPagoActual.setNull("Padre");
			valorFormaPagoActual.set("PorDefecto", false);
			valorFormaPagoActual.setNull("Valor2");
			valorFormaPagoActual.setNull("Valor3");
			valorFormaPagoActual.setNull("Valor4");
			valorFormaPagoActual.setNull("Valor5");
			valorFormaPagoActual.setNull("Valor6");
			valorFormaPagoActual.setNull("Valor7");
			valorFormaPagoActual.setNull("Valor8");
			valorFormaPagoActual.setNull("Valor9");
			valorFormaPagoActual.setNull("Valor10");
			valorFormaPagoActual.setNull("Valor11");
			valorFormaPagoActual.setNull("Valor12");
			valorFormaPagoActual.setNull("Valor13");
			valorFormaPagoActual.setNull("Valor14");
			valorFormaPagoActual.setNull("Valor15");

			formaPagoActual.set("Valor", valorFormaPagoActual);
			atrList.add(formaPagoActual);

			// nuevaFormaPago
			Objeto nuevaFormaPago = new Objeto();
			nuevaFormaPago.set("nombreAtributo", "nuevaformapago");

			Objeto valorNuevaFormaPago = new Objeto();

			valorNuevaFormaPago.set("Titulo", formaPago.objetos().get(0).get("descripcion"));
			valorNuevaFormaPago.set("Valor", formaPago.objetos().get(0).get("formaCobro"));
			valorNuevaFormaPago.setNull("Padre");
			valorNuevaFormaPago.set("PorDefecto", false);
			valorNuevaFormaPago.set("Valor2", formaPago.objetos().get(0).integer("producto"));
			valorNuevaFormaPago.setNull("Valor3");
			valorNuevaFormaPago.setNull("Valor4");
			valorNuevaFormaPago.setNull("Valor5");
			valorNuevaFormaPago.setNull("Valor6");
			valorNuevaFormaPago.setNull("Valor7");
			valorNuevaFormaPago.setNull("Valor8");
			valorNuevaFormaPago.setNull("Valor9");
			valorNuevaFormaPago.setNull("Valor10");
			valorNuevaFormaPago.setNull("Valor11");
			valorNuevaFormaPago.setNull("Valor12");
			valorNuevaFormaPago.setNull("Valor13");
			valorNuevaFormaPago.setNull("Valor14");
			valorNuevaFormaPago.setNull("Valor15");

			nuevaFormaPago.set("Valor", valorNuevaFormaPago);
			atrList.add(nuevaFormaPago);

			String fc = (String) formaPago.objetos().get(0).get("formaCobro");
			if (!"EFMN".equals(fc) && !"EFMNC".equals(fc)) {
				// Cuenta
				Objeto cuenta = new Objeto();
				cuenta.set("nombreAtributo", "cuenta");

				Objeto valorCuenta = new Objeto();
				valorCuenta.set("Titulo", Cuenta.tipo(cuentaPago.numero()) + " - " + cuentaPago.numero());
				valorCuenta.set("Valor", cuentaPago.numero());
				valorCuenta.setNull("Padre");
				valorCuenta.set("PorDefecto", false);
				valorCuenta.set("Valor2", Cuenta.tipo(cuentaPago.numero()));
				valorCuenta.set("Valor3", cuentaPago.idTitularidad());
				valorCuenta.set("Valor4", cuentaPago.id());
				valorCuenta.set("Valor5", cuentaPago.descripcionEstado());
				valorCuenta.set("Valor6", cuentaPago.idEstado());
				valorCuenta.set("Valor7", cuentaPago.desripcionUsoFirma());
				valorCuenta.set("Valor8", cuentaPago.usoFirma());

				if (cuentaPago.oficina() == null) {
					if (cuentaPago.sucursal() == null) {
						valorCuenta.setNull("Valor9");
					} else {
						valorCuenta.set("Valor9", cuentaPago.sucursal());
					}
				} else {
					valorCuenta.set("Valor9", cuentaPago.oficina());
				}

				valorCuenta.set("Valor10", Integer.parseInt(cuentaPago.idMoneda()));
				valorCuenta.setNull("Valor11");
				valorCuenta.setNull("Valor12");
				valorCuenta.setNull("Valor13");
				valorCuenta.setNull("Valor14");
				valorCuenta.setNull("Valor15");
				cuenta.set("Valor", valorCuenta);
				atrList.add(cuenta);
			}

			// cuentaActual
			Objeto cuentaActual = new Objeto();
			cuentaActual.set("nombreAtributo", "nrocuentaactual");
			cuentaActual.set("Valor", prestamo.cuentaPago() != null ? prestamo.cuentaPago().numero() : "-");
			atrList.add(cuentaActual);

			request.body("Atributos", atrList);
			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB cambioFormaPagoTC(ContextoMB contexto, String codigotipificacion, String formaPago, TarjetaCredito tarjetaCredito, Cuenta cuenta) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);
			// String codigotipificacion = "SAV-TC";
			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			// numeroproducto
			String descFormaPago = (String) detalleFormaPago(contexto, tarjetaCredito.idFormaPago()).objetos().get(0).get("pagoFormaDescrip");
			Objeto numeroprod = new Objeto();
			numeroprod.set("nombreAtributo", "numeroproducto");

			Objeto valorNumProd = new Objeto();
			valorNumProd.set("Titulo", tarjetaCredito.tipo() + " - " + tarjetaCredito.numero());
			valorNumProd.set("Valor", tarjetaCredito.numero());
			valorNumProd.setNull("Padre");
			valorNumProd.set("PorDefecto", false);
			valorNumProd.set("Valor2", tarjetaCredito.idTipo());
			valorNumProd.set("Valor3", tarjetaCredito.tipo());
			valorNumProd.set("Valor4", tarjetaCredito.idTitularidad());
			valorNumProd.set("Valor5", tarjetaCredito.idFormaPago());
			valorNumProd.set("Valor6", descFormaPago);
			valorNumProd.set("Valor7", tarjetaCredito.habilitada());
			valorNumProd.set("Valor8", "\"" + tarjetaCredito.cuenta() + "\"");
			valorNumProd.set("Valor9", tarjetaCredito.modeloLiquidacion());
			valorNumProd.set("Valor10", tarjetaCredito.modelo());
			valorNumProd.setNull("Valor11");
			valorNumProd.setNull("Valor12");
			valorNumProd.setNull("Valor13");
			valorNumProd.setNull("Valor14");
			valorNumProd.setNull("Valor15");

			numeroprod.set("Valor", valorNumProd);
			atrList.add(numeroprod);

			// FormaPagoActual
			Objeto formaPagoActual = new Objeto();
			formaPagoActual.set("nombreAtributo", "formapagoactual");

			Objeto valorFormaPagoActual = new Objeto();
			valorFormaPagoActual.set("Titulo", descFormaPago);
			valorFormaPagoActual.set("Valor", tarjetaCredito.idFormaPago());
			valorFormaPagoActual.setNull("Padre");
			valorFormaPagoActual.set("PorDefecto", false);
			valorFormaPagoActual.setNull("Valor2");
			valorFormaPagoActual.setNull("Valor3");
			valorFormaPagoActual.setNull("Valor4");
			valorFormaPagoActual.setNull("Valor5");
			valorFormaPagoActual.setNull("Valor6");
			valorFormaPagoActual.setNull("Valor7");
			valorFormaPagoActual.setNull("Valor8");
			valorFormaPagoActual.setNull("Valor9");
			valorFormaPagoActual.setNull("Valor10");
			valorFormaPagoActual.setNull("Valor11");
			valorFormaPagoActual.setNull("Valor12");
			valorFormaPagoActual.setNull("Valor13");
			valorFormaPagoActual.setNull("Valor14");
			valorFormaPagoActual.setNull("Valor15");

			formaPagoActual.set("Valor", valorFormaPagoActual);
			atrList.add(formaPagoActual);

			// nuevaFormaPago
			Objeto formaPagoObj = detalleFormaPago(contexto, formaPago);
			String pagoFormaCodi = formaPagoObj.objetos().get(0).get("pagoFormaCodi").toString().trim();
			String cuentaTipo = formaPagoObj.objetos().get(0).get("cuentaTipo") == null ? "" : formaPagoObj.objetos().get(0).get("cuentaTipo").toString();

			Objeto nuevaFormaPago = new Objeto();
			nuevaFormaPago.set("nombreAtributo", "nuevaformapago");

			Objeto valorNuevaFormaPago = new Objeto();

			valorNuevaFormaPago.set("Titulo", formaPagoObj.objetos().get(0).get("pagoFormaDescrip"));
			valorNuevaFormaPago.set("Valor", pagoFormaCodi);
			valorNuevaFormaPago.setNull("Padre");
			valorNuevaFormaPago.set("PorDefecto", false);
			if (cuentaTipo.isEmpty()) {
				valorNuevaFormaPago.setNull("Valor2");
			} else {
				valorNuevaFormaPago.set("Valor2", formaPagoObj.objetos().get(0).integer("cuentaTipo"));
			}
			valorNuevaFormaPago.setNull("Valor3");
			valorNuevaFormaPago.setNull("Valor4");
			valorNuevaFormaPago.setNull("Valor5");
			valorNuevaFormaPago.setNull("Valor6");
			valorNuevaFormaPago.setNull("Valor7");
			valorNuevaFormaPago.setNull("Valor8");
			valorNuevaFormaPago.setNull("Valor9");
			valorNuevaFormaPago.setNull("Valor10");
			valorNuevaFormaPago.setNull("Valor11");
			valorNuevaFormaPago.setNull("Valor12");
			valorNuevaFormaPago.setNull("Valor13");
			valorNuevaFormaPago.setNull("Valor14");
			valorNuevaFormaPago.setNull("Valor15");

			nuevaFormaPago.set("Valor", valorNuevaFormaPago);
			atrList.add(nuevaFormaPago);

			// Cuenta
			Objeto cuentaObj = new Objeto();
			cuentaObj.set("nombreAtributo", "cuenta");

			Objeto valorCuenta = new Objeto();

			if (cuentaTipo.isEmpty()) {
				valorCuenta.set("Titulo", "N/A");
				valorCuenta.setNull("Valor");
				valorCuenta.setNull("Padre");
				valorCuenta.set("PorDefecto", false);
				valorCuenta.setNull("Valor2");
				valorCuenta.setNull("Valor3");
				valorCuenta.setNull("Valor4");
				valorCuenta.setNull("Valor5");
				valorCuenta.setNull("Valor6");
				valorCuenta.setNull("Valor7");
				valorCuenta.setNull("Valor8");
				valorCuenta.setNull("Valor9");
				valorCuenta.setNull("Valor10");
			} else { // TODO valor2 de nuevaFormaPago is 2 o 3
				valorCuenta.set("Titulo", cuenta.idTipo() + " - " + cuenta.numero());
				valorCuenta.set("Valor", cuenta.numero());
				valorCuenta.setNull("Padre");
				valorCuenta.set("PorDefecto", false);
				valorCuenta.set("Valor2", cuenta.idTipo());
				valorCuenta.set("Valor3", cuenta.idTitularidad());
				valorCuenta.set("Valor4", cuenta.id());
				valorCuenta.set("Valor5", cuenta.descripcionEstado());
				valorCuenta.set("Valor6", cuenta.idEstado());
				valorCuenta.set("Valor7", cuenta.desripcionUsoFirma());
				valorCuenta.set("Valor8", cuenta.usoFirma());
				if (cuenta.oficina() == null) {
					if (cuenta.sucursal() == null) {
						valorCuenta.setNull("Valor9");
					} else {
						valorCuenta.set("Valor9", cuenta.sucursal());
					}
				} else {
					valorCuenta.set("Valor9", cuenta.oficina());
				}

				valorCuenta.set("Valor10", cuenta.idMoneda());
			}

			valorCuenta.setNull("Valor11");
			valorCuenta.setNull("Valor12");
			valorCuenta.setNull("Valor13");
			valorCuenta.setNull("Valor14");
			valorCuenta.setNull("Valor15");
			cuentaObj.set("Valor", valorCuenta);
			atrList.add(cuentaObj);

			// modeloliquidacion
			Objeto modeloliquidacion = new Objeto();
			modeloliquidacion.set("nombreAtributo", "modeloliquidacion");
			modeloliquidacion.set("Valor", tarjetaCredito.modelo());
			atrList.add(modeloliquidacion);

			request.body("Atributos", atrList);
			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	private static Objeto detalleFormaPago(ContextoMB contexto, String idformaPago) {
		Objeto formaPago = new Objeto();
		try {
			RespuestaMB respuesta = MBCatalogo.formaPagoTC(contexto);

			for (Objeto obj : respuesta.objetos("formasPago")) {
				if (idformaPago.equals(obj.get("pagoFormaCodi").toString().trim())) {
					formaPago = obj;
					break;
				}
			}
		} catch (Exception e) {
			return null;
		}
		return formaPago;

	}

	public static ApiResponseMB bajaTarjetaCredito(ContextoMB contexto, String codigotipificacion, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			ApiResponseMB detalleTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tc.numero());

			request.query("codigotipificacion", codigotipificacion); // BAJATC_PEDIDO
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numero = new Objeto();
			numero.set("nombreAtributo", "numerotc");
			numero.set("Valor", tc.numero());
			atrList.add(numero);

			Objeto esTitular = new Objeto();
			esTitular.set("nombreAtributo", "estitular");
			esTitular.set("Valor", tc.esTitular());
			atrList.add(esTitular);

			Objeto estado = new Objeto();
			estado.set("nombreAtributo", "estado");
			estado.set("Valor", detalleTarjeta.objetos().get(0).string("cuentaEstado"));
			atrList.add(estado);

			Objeto esPaquetizada = new Objeto();
			esPaquetizada.set("nombreAtributo", "espaquetizada");
			esPaquetizada.set("Valor", false);
			atrList.add(esPaquetizada);

			Objeto estadoPlastico = new Objeto();
			estadoPlastico.set("nombreAtributo", "estadoplastico");
			estadoPlastico.set("Valor", tc.idEstado());
			atrList.add(estadoPlastico);

			Objeto numCuentaVisa = new Objeto();
			numCuentaVisa.set("nombreAtributo", "numcuentavisa");
			numCuentaVisa.set("Valor", tc.cuenta());
			atrList.add(numCuentaVisa);

			Objeto gaf = new Objeto();
			gaf.set("nombreAtributo", "gaf");
			gaf.set("Valor", detalleTarjeta.objetos().get(0).string("descGrupoAfinidad"));
			atrList.add(gaf);

			Objeto tipoTarjeta = new Objeto();
			tipoTarjeta.set("nombreAtributo", "tipotarjeta");
			tipoTarjeta.set("Valor", tc.idTipo());
			atrList.add(tipoTarjeta);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static Boolean tieneSolicitudEnCurso(ContextoMB contexto, String tipificacion, Objeto obj, Boolean excepcionAlFallar) {
		try {
			ApiRequestMB request = ApiMB.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Integer diasNotififacion = ConfigMB.integer("cantidadDiasNotificacion", 45);

			Date currentDate = new Date(System.currentTimeMillis());
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -diasNotififacion);
			Date startDate = c.getTime();

			request.query("numerocuil", contexto.persona().cuit());
			request.query("fechadesde", dateFormat.format(startDate));
			request.query("fechahasta", dateFormat.format(currentDate));

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
			if (excepcionAlFallar) {
				if (response.hayError()) {
					throw new RuntimeException();
				}
			}

			if (tipificacion.equals("BAJATC")) {

				// posibles tipificaciones de bajas TC
				List<String> tipis = new ArrayList<String>();
				tipis.add("BAJATC_PEDIDO");
				tipis.add("BAJA_TC_ADICIONAL_PEDIDO");
				tipis.add("BAJA_PAQUETES");
				tipis.add("BAJATCHML_PEDIDO");

				for (Objeto caso : response.objetos("Datos")) {
					if (tipis.contains(caso.get("CodigoTipi")) && !"Resuelto".equalsIgnoreCase((String) caso.get("Estado"))) {
						switch ((String) caso.get("CodigoTipi")) {
						case "BAJATC_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("numerotc").get(0).get("Valor").equals(obj.get("tcNumero"))) {
								return true;
							}
							break;
						case "BAJA_TC_ADICIONAL_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("numerotc").get(0).get("Valor").equals(obj.get("tcNumero"))) {
								return true;
							}
							break;
						case "BAJA_PAQUETES":
							if (caso.objetos("Atributos").get(0).get("nombre").equals(obj.get("idPaquete"))) {
								return true;
							}
							break;
						case "BAJATCHML_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("numerotc").get(0).get("Valor").equals(obj.get("tcNumero"))) {
								return true;
							}
							break;
						default:
							break;
						}
					}
				}
			} else {

				for (Objeto caso : response.objetos("Datos")) {
					if (tipificacion.equals(caso.get("CodigoTipi")) && !"Resuelto".equalsIgnoreCase((String) caso.get("Estado"))) {
						switch (tipificacion) {
						case "BAJA_ADHESION_CA_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("NumerodeCajadeAhorro").get(0).get("Valor").equals(obj.get("numeroCuenta")) && caso.objetos("Atributos").get(0).get("CodigodeAdhesion").equals(obj.get("codigoAdhesion"))) {
								return true;
							}
							break;
						case "BAJA_ADHESION_CC_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("NumerodeCuentaCorriente").get(0).get("Valor").equals(obj.get("numeroCuenta")) && caso.objetos("Atributos").get(0).get("CodigodeAdhesion").equals(obj.get("codigoAdhesion"))) {
								return true;
							}
							break;
						case "BAJA_PAQUETES":
							if (caso.objetos("Atributos").get(0).get("nombre").equals(obj.get("idPaquete"))) {
								return true;
							}
							break;
						case "FP-PH":
							if (caso.objetos("Atributos").get(0).objetos("numeroproducto").get(0).get("Valor").equals("\"" + obj.get("prestamoNumero") + "\"")) {
								return true;
							}
							break;
						case "FP-PP":
							if (caso.objetos("Atributos").get(0).objetos("numeroproducto").get(0).get("Valor").equals("\"" + obj.get("prestamoNumero") + "\"")) {
								return true;
							}
							break;
						case "BAJACA_PEDIDO":
							if (caso.objetos("Atributos").get(0).objetos("numeroproducto").get(0).get("Valor3").equals(obj.get("numero"))) {
								return true;
							}
							break;
						case "7":
							if (caso.objetos("Atributos").get(0).objetos("NumeroDeTD").get(0).get("Titulo").equals(obj.get("idTarjetaDebito"))) {
								return true;
							} else if (caso.objetos("Atributos").get(0).objetos("NumeroDeTD").get(0).get("Valor").equals(obj.get("idTarjetaDebito"))) {
								return true;
							}
							break;
						case "137":
							if (caso.objetos("Atributos").get(0).objetos("numerodetc").get(0).get("Valor").equals(obj.get("tcNumero"))) {
								return true;
							}
							break;
						default: // "TERMINOS_CONDICIONES_PEDIDO"; "SAV-TC" (Cambio forma de pago TC)
							return true;
						}
					}
				}
			}
		} catch (Exception e) {
			if (excepcionAlFallar) {
				throw new RuntimeException(e);
			}
			return false;
		}
		return false;
	}
	
	public static Boolean tieneSolicitudEnCursoBajaCa(ContextoMB contexto, String tipificacion, Objeto obj, Boolean excepcionAlFallar) {
		try {
			ApiRequestMB request = ApiMB.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new Date(System.currentTimeMillis());
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -10);
			Date startDate = c.getTime();

			c.setTime(currentDate);
			c.add(Calendar.DATE, 1);
			Date endDate = c.getTime();

			request.query("numerocuil", contexto.persona().cuit());
			request.query("fechadesde", dateFormat.format(startDate));
			request.query("fechahasta", dateFormat.format(endDate));

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());
			if (excepcionAlFallar) {
				if (response.hayError()) {
					throw new RuntimeException();
				}
			}

			for (Objeto caso : response.objetos("Datos")) {
				if (!tipificacion.equals(caso.get("CodigoTipi"))){
					continue;
				}
				
				if (!"Resuelto".equalsIgnoreCase((String) caso.get("Estado")) || ("Resuelto".equalsIgnoreCase((String) caso.get("Estado")) && "Favorable".equalsIgnoreCase((String) caso.get("Resolucion")))) {
					switch (tipificacion) {
					case "BAJACA_PEDIDO":
						if (caso.objetos("Atributos").get(0).objetos("numeroproducto").get(0).get("Valor3").equals(obj.get("numero"))) {
							return true;
						}
						break;
					default:
						return true;
					}
				}
			}
		} catch (Exception e) {
			if (excepcionAlFallar) {
				throw new RuntimeException(e);
			}
			return false;
		}
		return false;
	}
	
	// TODO baja CA sin paquete
	public static ApiResponseMB bajaCajaAhorro(ContextoMB contexto, String codigotipificacion, Cuenta cuenta) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "MB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			String saldoEnCuenta = cuenta.simboloMoneda() + " " + cuenta.saldo();

			Objeto ca = new Objeto();
			ca.set("nombreAtributo", "numeroproducto");

			Objeto valor = new Objeto();
			valor.set("Titulo", cuenta.numero() + " - " + cuenta.idTipo()); // "404500000211739 - AHO",
			valor.set("Valor", cuenta.numero() + " - " + cuenta.idTipo()); // ""404500000211739 - AHO"",
			valor.setNull("Padre");
			valor.set("PorDefecto", false);
			valor.set("Valor2", cuenta.fechaAlta("yyyy-MM-dd")); // "2004-07-30"
			valor.set("Valor3", cuenta.numero()); // 404500000211739
			valor.set("Valor4", cuenta.usoFirma()); // "I",
			valor.set("Valor5", cuenta.idTitularidad()); // "T",
			valor.setNull("Valor6");
			valor.set("Valor7", cuenta.categoria()); // "D",
			valor.set("Valor8", saldoEnCuenta); // "$ 547.63",
			valor.setNull("Valor9");
			valor.setNull("Valor10");
			valor.setNull("Valor11");
			valor.setNull("Valor12");
			valor.setNull("Valor13");
			valor.setNull("Valor14");
			valor.setNull("Valor15");

			ca.set("Valor", valor);
			atrList.add(ca);

			Objeto saldo = new Objeto();
			saldo.set("nombreAtributo", "saldo");
			saldo.set("Valor", saldoEnCuenta);
			atrList.add(saldo);

			request.body("Atributos", atrList);
			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	// para actualizar las notificaciones con adjuntos (la campanita)
	public static ApiResponseMB actualizarNotificaciones(ContextoMB contexto) {
		String numeroCaso = contexto.parametros.string("numeroCaso");
		String notificacionBorrada = contexto.parametros.string("notificacionBorrada", "false");
		String notificacionLeida = contexto.parametros.string("notificacionLeida", "false");

		ApiRequestMB request = ApiMB.request("V1CasoNotificacion", "postventa", "PATCH", "/casoNotificacion", contexto);

		request.query("numerocaso", numeroCaso);
		request.query("notificacionBorrada", notificacionBorrada);
		request.query("notificacionLeida", notificacionLeida);

		return ApiMB.response(request);
	}

	public static RespuestaMB tieneMaximoCambiosCartera(ContextoMB contexto) {
		try {
			ApiRequestMB request = ApiMB.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			Date currentDate = new Date(System.currentTimeMillis());
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -365);
			Date startDate = c.getTime();

			request.query("numerocuil", contexto.persona().cuit());
			request.query("fechadesde", dateFormat.format(startDate));
			request.query("fechahasta", dateFormat.format(currentDate));

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), contexto.persona().cuit());

			String tipificacion = CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC;
			Integer cantidadMaxAnual = 2; // TODO: dejar por VE por si quieren cambiar el valor a futuro

			Integer cantidadCambiosCartera = 0;

			for (Objeto caso : response.objetos("Datos")) {
				if (tipificacion.equals(caso.get("CodigoTipi"))) {
					cantidadCambiosCartera++;

					if (cantidadCambiosCartera >= cantidadMaxAnual) {
						return RespuestaMB.estado("CAMBIOS_MAXIMO");
					}
				}
			}
		} catch (Exception e) {
			return RespuestaMB.error(); // TODO: validar en caso de error si dejo o no pedir cambio
		}
		return RespuestaMB.exito();
	}

	public static ApiResponseMB cambioCarteraTC(ContextoMB contexto, String idCarteraNueva, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiResponseMB detalleTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tc.numero());
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numerodetc = new Objeto();
			numerodetc.set("nombreAtributo", "numerodetc");

			Objeto valor = new Objeto();
			valor.set("Titulo", tc.numero() + " - " + tc.descTipoTarjeta());
			valor.set("Valor", tc.numero());
			valor.setNull("Padre");
			valor.set("PorDefecto", false);
			valor.set("Valor2", tc.idTitularidad());
			valor.set("Valor3", tc.modelo());
			valor.set("Valor4", tc.modeloLiquidacion());
			valor.setNull("Valor5");
			valor.set("Valor6", tc.cuenta());
			valor.set("Valor7", tc.denominacionTarjeta());
			valor.setNull("Valor8");
			valor.setNull("Valor9");
			valor.setNull("Valor10");
			valor.setNull("Valor11");
			valor.setNull("Valor12");
			valor.setNull("Valor13");
			valor.setNull("Valor14");
			valor.setNull("Valor15");

			numerodetc.set("Valor", valor);
			atrList.add(numerodetc);

			Objeto gafActual = new Objeto();
			gafActual.set("nombreAtributo", "gafActual");
			gafActual.set("Valor", tc.modelo());
			atrList.add(gafActual);

			Objeto carteraActual = new Objeto();
			carteraActual.set("nombreAtributo", "carteraActual");
			carteraActual.set("Valor", tc.grupoCarteraTc());
			atrList.add(carteraActual);

			Objeto carteraNueva = new Objeto();
			carteraNueva.set("nombreAtributo", "carteraNueva");

			Objeto valorCarteraNueva = new Objeto();
			valorCarteraNueva.set("Titulo", descripcionCarteras(idCarteraNueva));
			valorCarteraNueva.set("Valor", "\"" + idCarteraNueva + "\"");
			valorCarteraNueva.setNull("Padre");
			valorCarteraNueva.set("PorDefecto", false);
			valorCarteraNueva.setNull("Valor2");
			valorCarteraNueva.setNull("Valor3");
			valorCarteraNueva.setNull("Valor4");
			valorCarteraNueva.setNull("Valor5");
			valorCarteraNueva.setNull("Valor6");
			valorCarteraNueva.setNull("Valor7");
			valorCarteraNueva.setNull("Valor8");
			valorCarteraNueva.setNull("Valor9");
			valorCarteraNueva.setNull("Valor10");
			valorCarteraNueva.setNull("Valor11");
			valorCarteraNueva.setNull("Valor12");
			valorCarteraNueva.setNull("Valor13");
			valorCarteraNueva.setNull("Valor14");
			valorCarteraNueva.setNull("Valor15");

			carteraNueva.set("Valor", valorCarteraNueva);
			atrList.add(carteraNueva);

			Objeto codigoGAF = new Objeto();
			codigoGAF.set("nombreAtributo", "codigoGAF");
			codigoGAF.set("Valor", tc.modeloLiquidacion());
			atrList.add(codigoGAF);

			Objeto cuentaEstado = new Objeto();
			cuentaEstado.set("nombreAtributo", "cuentaEstado");
			cuentaEstado.set("Valor", detalleTarjeta.objetos().get(0).string("cuentaEstado"));
			atrList.add(cuentaEstado);

			Objeto tarjetaEstado = new Objeto();
			tarjetaEstado.set("nombreAtributo", "tarjetaEstado");
			tarjetaEstado.set("Valor", tc.idEstado());
			atrList.add(tarjetaEstado);

			Objeto cuenta = new Objeto();
			cuenta.set("nombreAtributo", "cuenta");
			cuenta.set("Valor", tc.cuenta());
			atrList.add(cuenta);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	private static String descripcionCarteras(String valor) {
		String titulo = "";
		switch (valor) {
		case "1":
			titulo = "1 - VTO Aprox: 03 al 09";
			break;
		case "2":
			titulo = "2 - VTO Aprox: 01 al 05";
			break;
		case "3":
			titulo = "3 - VTO Aprox: 20 al 26";
			break;
		case "4":
			titulo = "4 - VTO Aprox: 13 al 19";
			break;
		default:
			break;
		}
		return titulo;
	}

	public static ApiResponseMB obtenerCasosGestion(ContextoMB contexto) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ApiRequestMB request = ApiMB.request("gestionGet", "postventa", "GET", "/ObtenerCasos", contexto);
		Integer diasNotififacion = ConfigMB.integer("cantidadDiasNotificacion", 45);

		Date currentDate = new Date(System.currentTimeMillis());
		Calendar c = Calendar.getInstance();
		c.setTime(currentDate);
		c.add(Calendar.DATE, -diasNotififacion);
		Date startDate = c.getTime();

		request.query("numerocuil", contexto.persona().cuit());
		request.query("fechadesde", dateFormat.format(startDate));
		request.query("fechahasta", dateFormat.format(currentDate));
		request.query("filtrarnotifborrada", "true");
		request.query("adjuntos", "true");

		request.cacheSesion = true;
		return ApiMB.response(request, "curso");
	}

	public static void eliminarCacheGestiones(ContextoMB contexto) {
		try {
			ApiMB.eliminarCache(contexto, "gestionGet", "curso");
		} catch (Exception e) {
		}
	}

	public static ApiResponseMB promoNoImpactadaTD(ContextoMB contexto, TarjetaDebito td, Cuenta cuenta, Objeto movimiento) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			String codigoTipificacion = "";

			if (ConfigMB.esDesarrollo()) {
				codigoTipificacion = "ReclamoPromocionesTd";
			} else {
				codigoTipificacion = CODIDO_TIPIFICACION_PROMO_NO_IMPACTADA_TD + "R";
			}

			request.query("codigotipificacion", codigoTipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigoTipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			// falta definir, no esta impactado
			// request.body("observaciones", movimiento.string("comentario"));

			Objeto numerodeTarjetadeDebito = new Objeto();
			numerodeTarjetadeDebito.set("nombreAtributo", "NumerodeTarjetadeDebito");

			Objeto valor = new Objeto();
			valor.set("Titulo", td.numero() + " - " + td.getTipo()); // "Titulo": "4998590288913904 - ATM",
			valor.set("Valor", td.numero() + " - " + td.getTipo()); // "Valor": "4998590288913904 - ATM",
			valor.setNull("Padre");
			valor.set("PorDefecto", false);
			valor.set("Valor2", td.numero()); // "Valor2": 4998590288913904,
			valor.setNull("Valor3");
			valor.setNull("Valor4");
			valor.setNull("Valor5");
			valor.setNull("Valor6");
			valor.setNull("Valor7");
			valor.setNull("Valor8");
			valor.setNull("Valor9");
			valor.setNull("Valor10");
			valor.setNull("Valor11");
			valor.setNull("Valor12");
			valor.setNull("Valor13");
			valor.setNull("Valor14");
			valor.setNull("Valor15");

			numerodeTarjetadeDebito.set("Valor", valor);
			atrList.add(numerodeTarjetadeDebito);

			Objeto numeroproducto = new Objeto();
			numeroproducto.set("nombreAtributo", "numeroproducto");

			Objeto valorNumProducto = new Objeto();
			valorNumProducto.set("Titulo", cuenta.numero() + " - " + cuenta.idTipo()); // "Titulo": "406300017897700 - AHO",
			valorNumProducto.set("Valor", cuenta.numero() + " - " + cuenta.idTipo()); // "Valor": "406300017897700 - AHO",
			valorNumProducto.setNull("Padre");
			valorNumProducto.set("PorDefecto", false);
			valorNumProducto.set("Valor2", cuenta.idTitularidad()); // "Valor2": "T",
			valorNumProducto.set("Valor3", cuenta.numero()); // "Valor3": 406300017897700,
			valorNumProducto.set("Valor4", cuenta.idTipo()); // "Valor4": "AHO",
			valorNumProducto.set("Valor5", cuenta.id()); // "Valor5": 61435123,
			valorNumProducto.set("Valor6", cuenta.descEstado()); // "Valor6": "VIGENTE",
			valorNumProducto.set("Valor7", cuenta.estado()); // "Valor7": "V",
			valorNumProducto.setNull("Valor8");
			valorNumProducto.setNull("Valor9");
			valorNumProducto.setNull("Valor10");
			valorNumProducto.setNull("Valor11");
			valorNumProducto.setNull("Valor12");
			valorNumProducto.setNull("Valor13");
			valorNumProducto.setNull("Valor14");
			valorNumProducto.setNull("Valor15");

			numeroproducto.set("Valor", valorNumProducto);
			atrList.add(numeroproducto);

			Objeto movimientos = new Objeto();
			movimientos.set("nombreAtributo", "movimientos");

			Objeto valorMovimientos = new Objeto();
			valorMovimientos.set("Titulo", movimiento.string("nombreComercio")); // "Titulo": "AUTOSERVICIO CARL - GARIN",
			valorMovimientos.set("Valor", movimiento.string("fecha")); // "Valor": "2022-08-01",
			valorMovimientos.setNull("Padre");
			valorMovimientos.set("PorDefecto", false);
			valorMovimientos.set("Valor2", Formateador.importe(movimiento.bigDecimal("monto"))); // "Valor2": -4671,
			valorMovimientos.set("Valor3", true); // "Valor3": true,
			valorMovimientos.set("Valor4", 4202); // "Valor4": 4202,
			valorMovimientos.set("Valor5", movimiento.string("moneda")); // "Valor5": "Pesos",
			valorMovimientos.setNull("Valor6");
			valorMovimientos.setNull("Valor7");
			valorMovimientos.setNull("Valor8");
			valorMovimientos.setNull("Valor9");
			valorMovimientos.setNull("Valor10");
			valorMovimientos.setNull("Valor11");
			valorMovimientos.setNull("Valor12");
			valorMovimientos.setNull("Valor13");
			valorMovimientos.setNull("Valor14");
			valorMovimientos.setNull("Valor15");
			valorMovimientos.set("Valor16", Formateador.importe(movimiento.bigDecimal("monto"))); // "Valor16": -4671

			movimientos.set("Valor", valorMovimientos);
			atrList.add(movimientos);

			Objeto rubrodeComercio = new Objeto();
			rubrodeComercio.set("nombreAtributo", "RubrodeComercio");
			rubrodeComercio.set("Valor", movimiento.string("rubro")); // "8"
			atrList.add(rubrodeComercio);

			Objeto campania = new Objeto();
			campania.set("nombreAtributo", "Campaña");
			campania.set("Valor", "N/A"); // "vea"
			atrList.add(campania);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponseMB desconocimientoConsumo(ContextoMB contexto, TarjetaDebito td, Cuenta cuenta, String fecha2, List<Objeto> movimientos2) {

		BigDecimal montoTotal = new BigDecimal("0");

		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD + "R");
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD + "R");
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroProducto = new Objeto();
			numeroProducto.set("nombreAtributo", "numeroproducto");

			Objeto valorNumProd = new Objeto();
			valorNumProd.set("Titulo", td.numero() + " - " + td.getTipo()); // "4998590354264901 - ATM"
			valorNumProd.set("Valor", td.numero() + " - " + td.getTipo()); // "4998590354264901 - ATM"
			valorNumProd.setNull("Padre");
			valorNumProd.set("PorDefecto", false);
			valorNumProd.set("Valor2", td.numero()); // 4998590354264901
			valorNumProd.setNull("Valor3");
			valorNumProd.setNull("Valor4");
			valorNumProd.setNull("Valor5");
			valorNumProd.setNull("Valor6");
			valorNumProd.setNull("Valor7");
			valorNumProd.setNull("Valor8");
			valorNumProd.setNull("Valor9");
			valorNumProd.setNull("Valor10");
			valorNumProd.setNull("Valor11");
			valorNumProd.setNull("Valor12");
			valorNumProd.setNull("Valor13");
			valorNumProd.setNull("Valor14");
			valorNumProd.setNull("Valor15");

			numeroProducto.set("Valor", valorNumProd);

			atrList.add(numeroProducto);

			// CUENTA
			Objeto numeroCuenta = new Objeto();
			numeroCuenta.set("nombreAtributo", "NumerodeCuenta");

			Objeto valorNumCuenta = new Objeto();
			valorNumCuenta.set("Titulo", cuenta.numero() + " - " + cuenta.idTipo()); // "Titulo":"403800022352544 - AHO",
			valorNumCuenta.set("Valor", cuenta.numero() + " - " + cuenta.idTipo()); // "Valor":"403800022352544 - AHO",
			valorNumCuenta.setNull("Padre");
			valorNumCuenta.set("PorDefecto", false);
			valorNumCuenta.set("Valor2", cuenta.fechaAlta("yyyy-MM-dd")); // "Valor2":"2017-07-31",
			valorNumCuenta.set("Valor3", cuenta.numero()); // "Valor3":403800022352544,
			valorNumCuenta.setNull("Valor4");
			valorNumCuenta.setNull("Valor5");
			valorNumCuenta.setNull("Valor6");
			valorNumCuenta.setNull("Valor7");
			valorNumCuenta.setNull("Valor8");
			valorNumCuenta.setNull("Valor9");
			valorNumCuenta.setNull("Valor10");
			valorNumCuenta.setNull("Valor11");
			valorNumCuenta.setNull("Valor12");
			valorNumCuenta.setNull("Valor13");
			valorNumCuenta.setNull("Valor14");
			valorNumCuenta.setNull("Valor15");

			numeroCuenta.set("Valor", valorNumCuenta);

			atrList.add(numeroCuenta);

			// MOVIMIENTOS
			Objeto movimientos = new Objeto();
			Objeto movimientosAtr = new Objeto();
			movimientosAtr.set("nombreAtributo", "movimientos");

			// For para agregar los movimientos

			for (Objeto movimiento : movimientos2) {

				Objeto valorMovimiento = new Objeto();
				valorMovimiento.set("Titulo", movimiento.string("descripcion")); // * "Titulo":"MERCADOPAGO*URIEL - CAP.FEDERA"
				valorMovimiento.set("Valor", movimiento.string("fechaMovimiento")); // * "Valor":"07/11/2023" castear fecha "Valor": "2022-08-01"
				valorMovimiento.setNull("Padre");
				valorMovimiento.set("PorDefecto", false);
				valorMovimiento.set("Valor2", Formateador.importe(movimiento.bigDecimal("importe"))); // * "Valor2":-5060
				valorMovimiento.set("Valor3", true); // "Valor3":true,
				valorMovimiento.set("Valor4", 4202); // "Valor4":4202
				valorMovimiento.set("Valor5", movimiento.string("simboloMoneda")); // * "Valor5":Pesos
				valorMovimiento.set("Valor6", movimiento.string("categoria") + "-" + movimiento.string("subCategoria")); // "Valor6": "COMPRAS-TARJETA DE DEBITO"
				valorMovimiento.setNull("Valor7");
				valorMovimiento.setNull("Valor8");
				valorMovimiento.setNull("Valor9");
				valorMovimiento.setNull("Valor10");
				valorMovimiento.setNull("Valor11");
				valorMovimiento.setNull("Valor12");
				valorMovimiento.setNull("Valor13");
				valorMovimiento.setNull("Valor14");
				valorMovimiento.setNull("Valor15");
				valorMovimiento.set("Valor16", Formateador.importe(movimiento.bigDecimal("importe"))); // "Valor16": -5060

				montoTotal = montoTotal.add(movimiento.bigDecimal("importe"));

				movimientos.add(valorMovimiento);

			}

			Objeto fecha = new Objeto(); // "Fecha":"2023-11-04T00:00:00",
			fecha.set("nombreAtributo", "Fecha");
			fecha.set("Valor", fecha2 + "T00:00:00");
			atrList.add(fecha);

			Objeto monto = new Objeto(); // "Monto":"5060",
			monto.set("nombreAtributo", "Monto");
			monto.set("Valor", Formateador.importe(montoTotal.abs()));
			atrList.add(monto);

			movimientosAtr.set("Valor", movimientos);
			atrList.add(movimientosAtr);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}
	
	//BUHO PUNTOS CASO CRM Y ESTADO BDD
	
//	public static void casoCRMPrisma(ContextoMB contexto, Objeto datos) {
//		var apiRequest = ApiMB.request("AltaYCierreReclamoCRM", "prisma", "POST", "/v2/reclamosAltaYCierre", contexto);
//
//        var casoRequest = new HashMap<String, Object>();
//        var cerrarCasosRequest = new HashMap<String, Object>();
//  
//        //CasoRequest
//        casoRequest.put("canal", "MB");
//        casoRequest.put("codigoTipificacion", "9013P");
//        casoRequest.put("codigoTributarioCliente", contexto.persona().cuit());
//        
//        var listaAtributos = new ArrayList<Map<String, Object>>();
//        
//        listaAtributos.add(setMapAtributoList("Fechadecanje", datos.string("fecha_de_canje", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))));
//        listaAtributos.add(setMapAtributoList("Rubrodelacompra", datos.string("nombre", "Comercio no especificado")));
//        listaAtributos.add(setMapAtributoList("OfertadePuntos", datos.string("puntosacanjear", "")));
//        listaAtributos.add(setMapAtributoList("EquivalenciaenPesos", datos.string("monto_del_premio", "")));
//        listaAtributos.add(setMapAtributoList("codigopremio", datos.string("transaccion_id", "")));
//        casoRequest.put("atributos", listaAtributos);
//        
//
//        //CerrarCasoRequest
//        cerrarCasosRequest.put("Resolucion", "1");
//        cerrarCasosRequest.put("NotasResolucion", "Tu canje de Puntos por Pesos se procesó con éxito");
//        cerrarCasosRequest.put("Email", "true");
//
//        //CasoCompletoRequest
//        apiRequest.body("casoRequest", casoRequest);
//        apiRequest.body("cerrarCasosRequest", cerrarCasosRequest);
//        
//        new Futuro<>(() -> {
//           	ApiMB.response(apiRequest);
//           	return true;
//        });
//	}
	
	
	public static void casoCRMPrisma(ContextoMB contexto, Objeto datos) {
		try {
			var resp = crearCaso(contexto, datos);
			

			if (resp.codigo == 200) {
				try {
					var cerrar = cerrarCaso(contexto, resp.string("NumeroCaso"));
					if(cerrar.codigo == 200)
						insertarRegistroCRMdePWP("C", resp.string("NumeroCaso"), contexto.persona().cuit(), "MB", datos.integer("transaccion_id"));
				}
				catch(Exception e) {
					insertarRegistroCRMdePWP("A", resp.string("NumeroCaso"), contexto.persona().cuit(), "MB", datos.integer("transaccion_id"));
					createErrorResponse(e);
				}
			}
			else
				insertarRegistroCRMdePWP("S", null, contexto.persona().cuit(), "MB", datos.integer("transaccion_id"));

		} catch (Exception e) {
			insertarRegistroCRMdePWP("S", null, contexto.persona().cuit(), "MB", datos.integer("transaccion_id"));
			createErrorResponse(e);
		}
	}	

	private static void cerrarCasoCanjeBuhoPuntos(ContextoMB contexto) {
		try {
			var caso = obtenerCasoAbiertos(contexto);
			var obj = (Objeto) caso.get("Datos");
			boolean existe = obj.objetos().stream().anyMatch(ob -> ob.get("CodigoTipi").equals("9013P"));
			if (existe) {
				List<Objeto> listaBuhoPuntos = obj.objetos().stream().filter(ob -> ob.get("CodigoTipi").equals("9013P"))
						.toList();
				listaBuhoPuntos.forEach(l -> {
					var numeroCaso = l.get("NumeroCaso").toString();
					try {
						cerrarCaso(contexto, numeroCaso);
					} catch (Exception e) {
						createErrorResponse(e);
					}
				});
			}
		} catch (Exception e) {
			createErrorResponse(e);
		}
	}
	
	private static ApiResponseMB obtenerCasoAbiertos(ContextoMB contexto) throws Exception {
		var request = ApiMB.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);
		request.query("vigencia", "Activo");
		request.query("numerocuil", contexto.persona().cuit());
		return ApiMB.response(request);
	}

	
	private static ApiResponseMB crearCaso(ContextoMB contexto, Objeto datos) throws Exception  {
		var atrList = new ArrayList<Objeto>();
		var request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

		request.query("codigotipificacion", "9013P");
		request.body("Canal", "HB");
		request.body("CodigoTipificacion", "9013P");
		request.body("CodigoTributarioCliente", contexto.persona().cuit());

		atrList.add(createAtributo("Fechadecanje", datos.string("fecha_de_canje", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))));
		atrList.add(createAtributo("Rubrodelacompra", datos.string("nombre", "Comercio no especificado")));
		atrList.add(createAtributo("OfertadePuntos", datos.string("puntosacanjear", "")));
		atrList.add(createAtributo("EquivalenciaenPesos", datos.string("monto_del_premio", "")));
		atrList.add(createAtributo("codigopremio", datos.string("transaccion_id", "")));
		
		request.body("Atributos", atrList);
		
		return ApiMB.response(request);
	}

	private static ApiResponseMB cerrarCaso(ContextoMB contexto, String numero_caso_crm) throws Exception {
		var requestClose = ApiMB.request("CerrarCaso", "postventa", "POST", "/CerrarCaso", contexto);

		requestClose.body("NumeroCaso", numero_caso_crm);	    
		requestClose.body("Resolucion", "1");
		requestClose.body("NotasResolucion", "Tu canje de Puntos por Pesos se procesó con éxito");
		requestClose.body("Email", "true");

		return ApiMB.response(requestClose);
	}

	private static ApiResponseMB createErrorResponse(Exception e) {
		var errorResponse = new ApiResponseMB();
		errorResponse.codigo = 500;
		errorResponse.json = "Ocurrió un error: " + e.getMessage();
		return errorResponse;
	}
	
	//FIN BUHO PUNTOS CASO CRM Y ESTADO BDD

	private static Objeto createAtributo(String nombreAtributo, String valor) {
		var atributo = new Objeto();
		atributo.set("nombreAtributo", nombreAtributo);
		atributo.set("Valor", valor);
		return atributo;
	}
	
	public static HashMap<String,Object> setMapAtributoList(String nombreValor, Object valor){
		HashMap<String, Object> attrList = new HashMap<String, Object>();
		attrList.put("nombreAtributo", nombreValor);
		attrList.put("Valor", valor);
		
		return attrList;
	}

	private static SqlResponseMB insertarRegistroCRMdePWP(String estado, String numeroCaso, String cuit, String canal, Integer idOffer) {
		SqlRequestMB sqlRequest = SqlMB.request("InsertarRegistroCRM", "mobile");
		sqlRequest.configurarStoredProcedure(SP_EXEC_INSERTAR_REGISTRO_CRM_PWP, estado, numeroCaso, cuit, canal, canal, idOffer);
		
		return SqlMB.response(sqlRequest);
	}
	
	public static ApiResponseMB solicitarLibreDeuda(ContextoMB contexto, boolean tieneMora, boolean tieneProductosVigentes, boolean tieneProductoVigenteMora, boolean tieneProductosNoVigentes, boolean tieneProductoNoVigenteMora) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequestMB request = ApiMB.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			String tipificacion = "LIBREDEUDA";
			request.query("codigotipificacion", tipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", tipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto tipoDeEnvio = new Objeto();
			tipoDeEnvio.set("nombreAtributo", "TipoDeEnvio");
			tipoDeEnvio.set("Valor", "0");
			atrList.add(tipoDeEnvio);

			Objeto productosVigentes = new Objeto();
			productosVigentes.set("nombreAtributo", "productosVigentes");
			productosVigentes.set("Valor", tieneProductosVigentes);
			atrList.add(productosVigentes);

			Objeto productosNoVigentes = new Objeto();
			productosNoVigentes.set("nombreAtributo", "productosNoVigentes");
			productosNoVigentes.set("Valor", tieneProductosNoVigentes);
			atrList.add(productosNoVigentes);

			Objeto poseeMora = new Objeto();
			poseeMora.set("nombreAtributo", "poseeMora");
			poseeMora.set("Valor", tieneMora);
			atrList.add(poseeMora);

			Objeto productoVigenteMora = new Objeto();
			productoVigenteMora.set("nombreAtributo", "productoVigenteMora");
			productoVigenteMora.set("Valor", tieneProductoVigenteMora);
			atrList.add(productoVigenteMora);

			Objeto productoNoVigenteMora = new Objeto();
			productoNoVigenteMora.set("nombreAtributo", "productoNoVigenteMora");
			productoNoVigenteMora.set("Valor", tieneProductoNoVigenteMora);
			atrList.add(productoNoVigenteMora);

			Objeto tieneVentaCartera = new Objeto();
			tieneVentaCartera.set("nombreAtributo", "tieneVentaCartera");
			tieneVentaCartera.set("Valor", tieneMora);
			atrList.add(tieneVentaCartera);

			ApiResponseMB resVentaCarteraPre2010 = RestMora.tieneVentaCarteraPre2010(contexto);
			Objeto tieneVentaCarteraPre2010 = new Objeto();
			tieneVentaCarteraPre2010.set("nombreAtributo", "tieneVentaCarteraPre2010");
			tieneVentaCarteraPre2010.set("Valor", resVentaCarteraPre2010.get("Valor"));
			atrList.add(tieneVentaCarteraPre2010);

			Objeto segBloqueadaLegales = new Objeto();
			segBloqueadaLegales.set("nombreAtributo", "segBloqueadaLegales");
			segBloqueadaLegales.set("Valor", false);
			atrList.add(segBloqueadaLegales);

			Objeto modoTest = new Objeto();
			modoTest.set("nombreAtributo", "modoTest");
			modoTest.set("Valor", false);
			atrList.add(modoTest);

			Objeto derivar = new Objeto();
			derivar.set("nombreAtributo", "derivar");
			derivar.set("Valor", false);
			atrList.add(derivar);

			request.body("Atributos", atrList);

			return ApiMB.response(request);
		} catch (Exception e) {
			return null;
		}
	}
}
