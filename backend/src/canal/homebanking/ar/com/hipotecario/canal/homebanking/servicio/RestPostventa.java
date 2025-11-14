package ar.com.hipotecario.canal.homebanking.servicio;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.*;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.api.HBCatalogo;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.lib.Formateador;

public class RestPostventa {

	private static String CODIDO_TIPIFICACION_RESCATE_SUCURSAL = "12";
	private static String CODIDO_TIPIFICACION_REPO_TD = "7";
	private static String CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC = "137";
	private static String CODIDO_TIPIFICACION_PROMO_NO_IMPACTADA_TD = "121"; // Reclamo: 121+R, Consulta: 121+C
	private static String CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD = "108"; // Reclamo: 108+R, Consulta: 108+C
	private static String CODIDO_TIPIFICACION_CERTIFICADO_CUENTA = "309P";

	public static ApiResponse bajaTarjetaHML_AUTOGESTIVO(ContextoHB contexto, String tipificacion, TarjetaCredito tc, String observaciones) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", tipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", tipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());
			request.body("observaciones", observaciones);


			Objeto numero = new Objeto();
			numero.set("nombreAtributo", "NumerodeTarjeta");
			numero.set("Valor", tc.numero());
			atrList.add(numero);

			Objeto cuenta = new Objeto();
			cuenta.set("nombreAtributo", "NumeroCuenta");
			cuenta.set("Valor", tc.cuenta());
			atrList.add(cuenta);

			request.body("Atributos", atrList);

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}
	public static ApiResponse bajaTarjetaHML(ContextoHB contexto, String tipificacion, TarjetaCredito tc, String observaciones) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", tipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", tipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());
			request.body("observaciones", observaciones);

			Objeto numero = new Objeto();
			numero.set("nombreAtributo", "numerotc");
			numero.set("Valor", tc.numero());
			atrList.add(numero);

			Objeto cuenta = new Objeto();
			cuenta.set("nombreAtributo", "numcuentavisa");
			cuenta.set("Valor", tc.cuenta());
			atrList.add(cuenta);

			request.body("Atributos", atrList);

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaTarjetaCredito(ContextoHB contexto, String codigotipificacion, TarjetaCredito tc, String observaciones) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			ApiResponse detalleTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tc.numero());

			request.query("codigotipificacion", codigotipificacion); // BAJATC_PEDIDO
			request.body("Canal", "HB");
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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaTarjetaCreditoAdicional(ContextoHB contexto, String codigotipificacion, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion); // BAJA_TC_ADICIONAL_PEDIDO
			request.body("Canal", "HB");
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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaDirectaTarjetaCredito(ContextoHB contexto, String codigotipificacion, TarjetaCredito tc, String observaciones) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion); // BAJATCAUTOGESTIVO
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());
			request.body("observaciones", observaciones);

			Objeto numero = new Objeto();
			numero.set("nombreAtributo", "numerotc");
			numero.set("Valor", tc.numero());
			atrList.add(numero);

			Objeto numCuentaVisa = new Objeto();
			numCuentaVisa.set("nombreAtributo", "numcuentavisa");
			numCuentaVisa.set("Valor", tc.cuenta());
			atrList.add(numCuentaVisa);

			request.body("Atributos", atrList);

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaPaquete(ContextoHB contexto, String codigotipificacion, String idPaquete) {
		try {

			// me aseguro de estar utilizando el paquete que corresponde a numeroPaquete
			// recibido
			Objeto paq = new Objeto();
			ApiResponse prods = ProductosService.productos(contexto, false);
			List<Objeto> paquetes = prods.objetos("productos");
			for (Objeto paquete : paquetes) {
				if (paquete.get("numero").equals(idPaquete)) {
					paq = paquete;
					break;
				}
			}

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
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
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse crearCasoLineaRoja(ContextoHB contexto) {
		try {

			String pTelefono = contexto.parametros.string("telefono");
			String pDomicilio = contexto.parametros.string("domicilio");
			String pMail = contexto.parametros.string("mail");
			String pDetalleUltMov = contexto.parametros.string("detalleUltMov");
			String pDetalleMovDesc = contexto.parametros.string("detalleMovDesc");
			String pDetalle = contexto.parametros.string("detalle");

			String codigotipificacion = "LINEA_ROJA_CANALES";
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto telefono = new Objeto();
			telefono.set("nombreAtributo", "telefono");
			telefono.set("Valor", pTelefono);
			atrList.add(telefono);

			Objeto domicilio = new Objeto();
			domicilio.set("nombreAtributo", "Domicilio");
			domicilio.set("Valor", pDomicilio);
			atrList.add(domicilio);

			Objeto mail = new Objeto();
			mail.set("nombreAtributo", "mail");
			mail.set("Valor", pMail);
			atrList.add(mail);

			Objeto detalleUltMov = new Objeto();
			detalleUltMov.set("nombreAtributo", "detalleUltMov");
			detalleUltMov.set("Valor", pDetalleUltMov);
			atrList.add(detalleUltMov);

			Objeto detalleMovDesc = new Objeto();
			detalleMovDesc.set("nombreAtributo", "detalleMovDesc");
			detalleMovDesc.set("Valor", pDetalleMovDesc);
			atrList.add(detalleMovDesc);

			Objeto detalle = new Objeto();
			detalle.set("nombreAtributo", "detalle");
			detalle.set("Valor", pDetalle);
			atrList.add(detalle);

			request.body("Atributos", atrList);
			request.permitirSinLogin = true;
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaDebitoAutomatico(ContextoHB contexto, String codigotipificacion, String cuenta, String codigoAdhesion, String cuitEmpresa) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
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
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse cambioFormaPagoPrestamo(ContextoHB contexto, String codigotipificacion, Prestamo prestamo, Objeto formaPago, Cuenta cuentaPago) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
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
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse obtenerCaso(ContextoHB contexto, String cuil) {
		ApiRequest request = Api.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);
		request.query("numerocuil", contexto.persona().cuit());
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), cuil);
	}

	public static ApiResponse cambioFormaPagoTC(ContextoHB contexto, String tipificacion, String formaPago, TarjetaCredito tarjetaCredito, Cuenta cuenta) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);
			request.query("codigotipificacion", tipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", tipificacion);
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
			} else {
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
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	private static Objeto detalleFormaPago(ContextoHB contexto, String idformaPago) {
		Objeto formaPago = new Objeto();
		try {
			Respuesta respuesta = HBCatalogo.formaPagoTC(contexto);

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

	public static ApiResponse crearCasoReimpresionTC(ContextoHB contexto, String codigotipificacion, String tarjeta, String idPieza) {
		try {
			TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(tarjeta);
			List<Objeto> atrList = new ArrayList<>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTarjeta = new Objeto();
			numeroTarjeta.set("nombreAtributo", "numerodetarjeta");
			Objeto valorNumTarjeta = new Objeto();
			valorNumTarjeta.set("Titulo", tarjetaCredito.numero() + " - " + tarjetaCredito.tipo());
			valorNumTarjeta.set("Valor", tarjetaCredito.numero());
			valorNumTarjeta.setNull("Padre");
			valorNumTarjeta.set("PorDefecto", false);
			valorNumTarjeta.set("Valor2", "");
			valorNumTarjeta.set("Valor3", "");
			valorNumTarjeta.set("Valor4", "");
			valorNumTarjeta.setNull("Valor5");
			valorNumTarjeta.set("Valor6", "\"" + idPieza + "\"");
			valorNumTarjeta.set("Valor7", "");
			valorNumTarjeta.setNull("Valor8");
			valorNumTarjeta.setNull("Valor9");
			valorNumTarjeta.setNull("Valor10");
			valorNumTarjeta.setNull("Valor11");
			valorNumTarjeta.setNull("Valor12");
			valorNumTarjeta.setNull("Valor13");
			valorNumTarjeta.setNull("Valor14");
			valorNumTarjeta.setNull("Valor15");

			numeroTarjeta.set("Valor", valorNumTarjeta);
			atrList.add(numeroTarjeta);

			Objeto motivos = new Objeto();
			motivos.set("nombreAtributo", "motivos");
			motivos.set("Valor", "3");
			atrList.add(motivos);

			Objeto domicilio = new Objeto();
			domicilio.set("nombreAtributo", "domicilio");
			domicilio.set("Valor", "DP");
			atrList.add(domicilio);

			Objeto tarjetaEstado = new Objeto();
			tarjetaEstado.set("nombreAtributo", "tarjetaEstado");
			tarjetaEstado.set("Valor", "20");
			atrList.add(tarjetaEstado);

			Objeto cuentaEstado = new Objeto();
			cuentaEstado.set("nombreAtributo", "cuentaEstado");
			cuentaEstado.set("Valor", "10");
			atrList.add(cuentaEstado);

			Objeto cuenta = new Objeto();
			cuenta.set("nombreAtributo", "cuenta");
			cuenta.set("Valor", tarjetaCredito.cuenta());
			atrList.add(cuenta);

			Objeto vigenciaHasta = new Objeto();
			vigenciaHasta.set("nombreAtributo", "vigenciaHasta");
			vigenciaHasta.set("Valor", tarjetaCredito.fechaVencimiento("yyyy-MM-dd"));
			atrList.add(vigenciaHasta);

			Objeto estadoPieza = new Objeto();
			estadoPieza.set("nombreAtributo", "estadoPieza");
			estadoPieza.set("Valor", "");
			atrList.add(estadoPieza);

			Objeto codigodistribucion = new Objeto();
			codigodistribucion.set("nombreAtributo", "codigodistribucion");
			codigodistribucion.set("Valor", "-10");
			atrList.add(codigodistribucion);

			request.body("Atributos", atrList);
			request.permitirSinLogin = true;
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse crearCasoRescateTcEnSucursal(ContextoHB contexto, TarjetaCredito tarjetaCredito, String idSucursal, String idPieza) {
		try {
			List<Objeto> atrList = new ArrayList<>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", CODIDO_TIPIFICACION_RESCATE_SUCURSAL);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", CODIDO_TIPIFICACION_RESCATE_SUCURSAL);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTarjetaAtributo = new Objeto();
			numeroTarjetaAtributo.set("nombreAtributo", "numerotarjeta");
			Objeto valorNumTarjeta = new Objeto();
			valorNumTarjeta.set("Titulo", tarjetaCredito.numero() + " - " + tarjetaCredito.tipo());
			valorNumTarjeta.set("Valor", tarjetaCredito.numero());
			valorNumTarjeta.setNull("Padre");
			valorNumTarjeta.set("PorDefecto", false);
			valorNumTarjeta.set("Valor2", idPieza);
			valorNumTarjeta.set("Valor3", tarjetaCredito.idEstado());
			valorNumTarjeta.set("Valor4", "");
			valorNumTarjeta.set("Valor5", "");
			valorNumTarjeta.set("Valor6", "");
			valorNumTarjeta.set("Valor7", "");
			valorNumTarjeta.setNull("Valor8");
			valorNumTarjeta.setNull("Valor9");
			valorNumTarjeta.setNull("Valor10");
			valorNumTarjeta.setNull("Valor11");
			valorNumTarjeta.setNull("Valor12");
			valorNumTarjeta.setNull("Valor13");
			valorNumTarjeta.setNull("Valor14");
			valorNumTarjeta.setNull("Valor15");

			numeroTarjetaAtributo.set("Valor", valorNumTarjeta);
			atrList.add(numeroTarjetaAtributo);

			Objeto sucursalDestinoAtributo = new Objeto();
			sucursalDestinoAtributo.set("nombreAtributo", "sucursaldestino");
			Objeto sucursalDestinoValor = new Objeto();
			sucursalDestinoValor.set("Titulo", "");
			sucursalDestinoValor.set("Valor", idSucursal);
			sucursalDestinoValor.setNull("Padre");
			sucursalDestinoValor.set("PorDefecto", false);
			sucursalDestinoValor.setNull("Valor2");
			sucursalDestinoValor.setNull("Valor3");
			sucursalDestinoValor.setNull("Valor4");
			sucursalDestinoValor.setNull("Valor5");
			sucursalDestinoValor.setNull("Valor6");
			sucursalDestinoValor.setNull("Valor7");
			sucursalDestinoValor.setNull("Valor8");
			sucursalDestinoValor.setNull("Valor9");
			sucursalDestinoValor.setNull("Valor10");
			sucursalDestinoValor.setNull("Valor11");
			sucursalDestinoValor.setNull("Valor12");
			sucursalDestinoValor.setNull("Valor13");
			sucursalDestinoValor.setNull("Valor14");
			sucursalDestinoValor.setNull("Valor15");
			sucursalDestinoAtributo.set("Valor", sucursalDestinoValor);
			atrList.add(sucursalDestinoAtributo);

			request.body("Atributos", atrList);
			request.permitirSinLogin = true;
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse terminosYCondiciones(ContextoHB contexto, String codigotipificacion, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			String descFormaPago = (String) detalleFormaPago(contexto, tc.idFormaPago()).objetos().get(0).get("pagoFormaDescrip");

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto tarjeta = new Objeto();
			tarjeta.set("nombreAtributo", "numerotc");

			Objeto valor = new Objeto();
			valor.set("Titulo", tc.tipo() + " - " + tc.numero()); // "Visa Gold - 4304970016221797",
			valor.set("Valor", tc.numero()); // 4304970016221797,
			valor.setNull("Padre");
			valor.set("PorDefecto", false);
			valor.set("Valor2", tc.idTipo()); // "P",
			valor.set("Valor3", tc.tipo()); // "Visa Gold",
			valor.set("Valor4", tc.idTitularidad()); // "T",
			valor.set("Valor5", tc.idFormaPago()); // 2,
			valor.set("Valor6", descFormaPago); // "DEB PAG MIN CTA. CTE.",
			valor.set("Valor7", tc.habilitada()); // true,
			valor.set("Valor8", "\"" + tc.cuenta() + "\""); // "\"0940604142\"",
			valor.set("Valor9", tc.modeloLiquidacion()); // 501,
			valor.set("Valor10", tc.modelo()); // "Modelo 501 - GOLD",
			valor.setNull("Valor11");
			valor.setNull("Valor12");
			valor.setNull("Valor13");
			valor.setNull("Valor14");
			valor.setNull("Valor15");

			tarjeta.set("Valor", valor);
			atrList.add(tarjeta);

			request.body("Atributos", atrList);

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse bajaCajaAhorro(ContextoHB contexto, String codigotipificacion, Cuenta cuenta) {
		try {

			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
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
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static Boolean tieneSolicitudEnCurso(ContextoHB contexto, String tipificacion, Objeto obj, Boolean excepcionAlFallar) {
		try {
			ApiRequest request = Api.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			// Tomo la fecha actual y desde ahi calculo 6 meses para atras
			Date currentDate = new Date(System.currentTimeMillis());
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -180);
			Date startDate = c.getTime();

			request.query("numerocuil", contexto.persona().cuit());
			request.query("fechadesde", dateFormat.format(startDate));
			request.query("fechahasta", dateFormat.format(currentDate));

			ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());
			if (excepcionAlFallar) {
				if (response.hayError()) {
					throw new RuntimeException();
				}
			}

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
					case "BAJATC_PEDIDO":
						if (caso.objetos("Atributos").get(0).objetos("numerotc").get(0).get("Valor").equals(obj.get("tcNumero"))) {
							return true;
						}
						break;
					case "BAJATCAUTOGESTIVO":
						if (caso.string("Atributos.numerotc").equals(obj.get("tcNumero"))) {
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
					case "9017P":
						if (caso.objetos("Atributos").get(0).string("NumerodeTarjeta").equals(obj.get("tcNumero"))) {
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
					default: // "TERMINOS_CONDICIONES_PEDIDO", "SAV-TC": //Cambio forma de pago TC
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
	
	public static Boolean tieneSolicitudEnCursoBajaCa(ContextoHB contexto, String tipificacion, Objeto obj, Boolean excepcionAlFallar) {
		try {
			ApiRequest request = Api.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);

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

			ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());
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

	// para las notificaciones con adjuntos (la campanita)
	public static ApiResponse obtenerCasos(ContextoHB contexto) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ApiRequest request = Api.request("V1ObtenerCasosGet", "postventa", "GET", "/ObtenerCasos", contexto);
		Integer diasNotififacion = ConfigHB.integer("cantidadDiasNotificacion", 45);

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
		return Api.response(request, "Resuelto");
	}

	// para actualizar las notificaciones con adjuntos (la campanita)
	public static ApiResponse actualizarNotificaciones(ContextoHB contexto) {
		String numeroCaso = contexto.parametros.string("numeroCaso");
		String notificacionBorrada = contexto.parametros.string("notificacionBorrada", "false");
		String notificacionLeida = contexto.parametros.string("notificacionLeida", "false");

		ApiRequest request = Api.request("V1CasoNotificacion", "postventa", "PATCH", "/casoNotificacion", contexto);

		request.query("numerocaso", numeroCaso);
		request.query("notificacionBorrada", notificacionBorrada);
		request.query("notificacionLeida", notificacionLeida);

		return Api.response(request);
	}

	public static ApiResponse reposicionTD(ContextoHB contexto, String idTarjetaDebito, String motivo, String estadoPieza, String codigoDistribucion) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", CODIDO_TIPIFICACION_REPO_TD);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", CODIDO_TIPIFICACION_REPO_TD);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroTD = new Objeto();
			numeroTD.set("nombreAtributo", "NumeroDeTD");

			Objeto valor = new Objeto();
			valor.set("Titulo", idTarjetaDebito);
			valor.set("Valor", idTarjetaDebito);
			valor.setNull("Padre");
			valor.set("PorDefecto", false);
			valor.setNull("Valor2");
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

			numeroTD.set("Valor", valor);
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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse cambioCarteraTC(ContextoHB contexto, String idCarteraNueva, TarjetaCredito tc) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiResponse detalleTarjeta = TarjetaCreditoService.consultaTarjetaCredito(contexto, tc.numero());
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static Respuesta tieneMaximoCambiosCartera(ContextoHB contexto) {
		try {
			ApiRequest request = Api.request("ObtenerCasos", "postventa", "GET", "/ObtenerCasos", contexto);

			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			// Tomo la fecha actual y desde ahi calculo 6 meses para atras
			int cantidadDiasCambioCartera = ConfigHB.integer("cantidadDiasCambioCartera");
			Date currentDate = new Date(System.currentTimeMillis());
			Calendar c = Calendar.getInstance();
			c.setTime(currentDate);
			c.add(Calendar.DATE, -365);
			Date startDate = c.getTime();

			request.query("numerocuil", contexto.persona().cuit());
			request.query("fechadesde", dateFormat.format(startDate));
			request.query("fechahasta", dateFormat.format(currentDate));

			ApiResponse response = Api.response(request, contexto.idCobis(), contexto.persona().cuit());

			String tipificacion = CODIDO_TIPIFICACION_CAMBIO_CARTERA_TC;
			// TODO: VE (cantidadMaximaAnualCambioCartera) por si quieren cambiar el valor a futuro
			Integer cantidadMaxAnual = ConfigHB.integer("cantidadMaximaAnualCambioCartera");

			Integer cantidadCambiosCartera = 0;

			for (Objeto caso : response.objetos("Datos")) {
				if (tipificacion.equals(caso.get("CodigoTipi"))) {
					cantidadCambiosCartera++;

					if (cantidadCambiosCartera >= cantidadMaxAnual){
						return Respuesta.estado("CAMBIOS_MAXIMO");
					}else{
						Date fechaAlta = !caso.string("FechaAlta").isEmpty() ? dateFormat.parse(caso.string("FechaAlta")) : currentDate;
						long diferenciaDias = Util.diferenciaDias(fechaAlta);

						if(diferenciaDias <= cantidadDiasCambioCartera){
							return Respuesta.estado("SOLICITUD_EN_CURSO");
						}
					}
				}
			}

		} catch (Exception e) {
			return Respuesta.error();
		}
		return Respuesta.exito();
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

	public static ApiResponse obtenerCasosGestion(ContextoHB contexto) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		ApiRequest request = Api.request("gestionGet", "postventa", "GET", "/ObtenerCasos", contexto);
		Integer diasNotififacion = ConfigHB.integer("cantidadDiasNotificacion", 45);

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
		return Api.response(request, "curso");
	}

	public static void eliminarCacheGestiones(ContextoHB contexto) {
		try {
			Api.eliminarCache(contexto, "gestionGet", "curso");
		} catch (Exception e) {
		}
	}


	public static ApiResponse promoNoImpactadaTD(ContextoHB contexto, TarjetaDebito td, Cuenta cuenta, Objeto movimiento) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			String codigoTipificacion = "";

			if (ConfigHB.esDesarrollo()) {
				codigoTipificacion = "ReclamoPromocionesTd";
			} else {
				codigoTipificacion = CODIDO_TIPIFICACION_PROMO_NO_IMPACTADA_TD + "R";
			}
			request.query("codigotipificacion", codigoTipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigoTipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());
			request.body("Observaciones", movimiento.string("comentario"));

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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse desconocimientoConsumo(ContextoHB contexto, TarjetaDebito td, Cuenta cuenta, String fecha2, List<Objeto> movimientos2, String comentario) {

		BigDecimal montoTotal = new BigDecimal("0");

		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD + "R");
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", CODIDO_TIPIFICACION_DESCONICIMIENTO_CONSUMO_TD + "R");
			request.body("CodigoTributarioCliente", contexto.persona().cuit());
			request.body("Observaciones", comentario);

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
				valorMovimiento.set("Valor2", movimiento.bigDecimal("importe").abs()); // "Valor2":5060
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
				valorMovimiento.set("Valor16", movimiento.bigDecimal("importe").abs()); // "Valor16":5060

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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse crearNota(ContextoHB contexto, String numeroCaso, Objeto comprobante) {
		try {
			ApiRequest request = Api.request("CrearNota", "postventa", "POST", "/crearNota", contexto);

			String titulo = comprobante.string("titulo");
			String descripcion = comprobante.string("descripcion");
			String nombreArchivo = comprobante.string("nombreArchivo");
			String archivo = comprobante.string("archivo");

			request.body("NumeracionCRM", numeroCaso);
			request.body("Titulo", titulo);
			request.body("Descripcion", descripcion);
			request.body("NombreDelDocumento", nombreArchivo);
			request.body("Documento", archivo);

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	//BUHO PUNTOS CASO CRM Y ESTADO BDD
 /* 	
	public static void casoCRMPrisma(ContextoHB contexto, Objeto datos) {
		var apiRequest = Api.request("AltaYCierreReclamoCRM", "prisma", "POST", "/v2/reclamosAltaYCierre", contexto);

        var casoRequest = new HashMap<String, Object>();
        var cerrarCasosRequest = new HashMap<String, Object>();
  
        //CasoRequest
        casoRequest.put("canal", "HB");
        casoRequest.put("codigoTipificacion", "9013P");
        casoRequest.put("codigoTributarioCliente", contexto.persona().cuit());
        
        var listaAtributos = new ArrayList<Map<String, Object>>();
        
        listaAtributos.add(setMapAtributoList("Fechadecanje", datos.string("fecha_de_canje", new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()))));
        listaAtributos.add(setMapAtributoList("Rubrodelacompra", datos.string("nombre", "Comercio no especificado")));
        listaAtributos.add(setMapAtributoList("OfertadePuntos", datos.string("puntosacanjear", "")));
        listaAtributos.add(setMapAtributoList("EquivalenciaenPesos", datos.string("monto_del_premio", "")));
        listaAtributos.add(setMapAtributoList("codigopremio", datos.string("transaccion_id", "")));
        casoRequest.put("atributos", listaAtributos);
        

        //CerrarCasoRequest
        cerrarCasosRequest.put("Resolucion", "1");
        cerrarCasosRequest.put("NotasResolucion", "Tu canje de Puntos por Pesos se procesó con éxito");
        cerrarCasosRequest.put("Email", "true");

        //CasoCompletoRequest
        apiRequest.body("casoRequest", casoRequest);
        apiRequest.body("cerrarCasosRequest", cerrarCasosRequest);
        
       	Api.response(apiRequest);
	}*/
	
	public static void casoCRMPrisma(ContextoHB contexto, Objeto datos) {
		try {
			var resp = crearCaso(contexto, datos);
			if (resp.codigo == 200) {
				Objeto respCRM = (Objeto)resp.objeto("Datos").lista.get(0);
				cerrarCaso(contexto, respCRM.string("NumeracionCRM"));
			}

		} catch (Exception e) {
		}
	}
	
	private static ApiResponse crearCaso(ContextoHB contexto, Objeto datos) throws Exception  {
		var atrList = new ArrayList<Objeto>();
		var request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

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
		
		return Api.response(request);
	}

	private static ApiResponse cerrarCaso(ContextoHB contexto, String numero_caso_crm) throws Exception {
		var requestClose = Api.request("CerrarCaso", "postventa", "POST", "/CerrarCaso", contexto);

		requestClose.body("NumeroCaso", numero_caso_crm);	    
		requestClose.body("Resolucion", "1");
		requestClose.body("NotasResolucion", "Tu canje de Puntos por Pesos se procesó con éxito");
		requestClose.body("Email", "true");

		return Api.response(requestClose);
	}

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

	public static ApiResponse obtenerOfertasRetencion(ContextoHB contexto) {
		try {
			ApiRequest request = Api.request("ObtenerOfertasRetencion", "postventa", "GET", "/obtenerOfertasRetencion", contexto);
			request.query("idCobis", contexto.idCobis());
			request.cacheSesion = true;
			return Api.response(request);
		}catch (Exception e){
			return null;
		}
	}

	public static ApiResponse solicitarLibreDeuda(ContextoHB contexto, boolean tieneMora, boolean tieneProductosVigentes, boolean tieneProductoVigenteMora, boolean tieneProductosNoVigentes, boolean tieneProductoNoVigenteMora) {
		try {
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCaso", "postventa", "POST", "/crearCaso", contexto);

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

			ApiResponse resVentaCarteraPre2010 = RestMora.tieneVentaCarteraPre2010(contexto);
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

			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

	public static ApiResponse certificadoCuenta(ContextoHB contexto, Cuenta cuenta) {
		try {
			String codigotipificacion = CODIDO_TIPIFICACION_CERTIFICADO_CUENTA;
			List<Objeto> atrList = new ArrayList<Objeto>();
			ApiRequest request = Api.request("CrearCasoCertificadoCuenta", "postventa", "POST", "/crearCaso", contexto);

			request.query("codigotipificacion", codigotipificacion);
			request.body("Canal", "HB");
			request.body("CodigoTipificacion", codigotipificacion);
			request.body("CodigoTributarioCliente", contexto.persona().cuit());

			Objeto numeroproducto = new Objeto();
			numeroproducto.set("nombreAtributo", "NúmerodeCajadeAhorro");

			Objeto valorNumProducto = new Objeto();
			valorNumProducto.set("Titulo", cuenta.numero() + " - " + cuenta.idTipo()); // "Titulo": "406300017897700 - AHO",
			valorNumProducto.set("Valor", cuenta.numero() + " - " + cuenta.idTipo()); // "Valor": "406300017897700 - AHO",
			valorNumProducto.setNull("Padre");
			valorNumProducto.set("PorDefecto", false);
			valorNumProducto.setNull("Valor2");
			valorNumProducto.setNull("Valor3");
			valorNumProducto.setNull("Valor4");
			valorNumProducto.set("Valor5", cuenta.cbu()); // "Valor5": "0440000411000000044776",
			valorNumProducto.setNull("Valor6");
			valorNumProducto.setNull("Valor7");
			valorNumProducto.setNull("Valor8");
			valorNumProducto.setNull("Valor9");
			valorNumProducto.setNull("Valor10");
			valorNumProducto.setNull("Valor11");
			valorNumProducto.setNull("Valor12");
			valorNumProducto.setNull("Valor13");
			valorNumProducto.setNull("Valor14");
			valorNumProducto.setNull("Valor15");
			valorNumProducto.setNull("Valor16");
			valorNumProducto.setNull("Valor17");
			valorNumProducto.setNull("Valor18");
			valorNumProducto.setNull("Valor19");
			valorNumProducto.setNull("Valor20");
			valorNumProducto.setNull("Valor21");
			valorNumProducto.setNull("Valor22");

			numeroproducto.set("Valor", valorNumProducto);
			atrList.add(numeroproducto);
			request.body("Atributos", atrList);
			return Api.response(request);
		} catch (Exception e) {
			return null;
		}
	}

}
