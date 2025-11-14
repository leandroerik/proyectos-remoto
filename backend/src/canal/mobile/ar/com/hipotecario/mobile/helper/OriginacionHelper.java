package ar.com.hipotecario.mobile.helper;

import java.math.BigDecimal;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.negocio.Constantes;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Solicitud;
import ar.com.hipotecario.mobile.negocio.Solicitud.SolicitudProducto;
import ar.com.hipotecario.mobile.negocio.SolicitudPrestamo;
import ar.com.hipotecario.mobile.negocio.TarjetaDebito;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestOmnicanalidad;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.RestVenta;

public class OriginacionHelper {

	public static Boolean validaDatosPrincipales(Objeto cliente) {
		Boolean irSucursal = false;
		Boolean simularTodoTrue = false;

		/* DATOS PRINCIPALES - DESDE */
		if (cliente.string("nombres").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		if (cliente.string("apellidos").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		if (cliente.string("numeroDocumento").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		if (cliente.string("idTipoDocumento").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		if (cliente.string("cuit").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		if (cliente.string("idSexo").isEmpty() || simularTodoTrue) {
			irSucursal = true;
		}
		return irSucursal;
	}

	public static Objeto validaDomicilioLegal(ContextoMB contexto) {
		Objeto domicilio = RestPersona.domicilioLegal(contexto, contexto.persona().cuit());
		if (domicilio == null) {
			domicilio = new Objeto();
		}
		Objeto domicilioLegal = new Objeto();
		domicilioLegal.set("faltan_datos", false);
		if ("".equals(domicilio.string("calle")) || "".equals(domicilio.string("numero")) || "".equals(domicilio.string("idCodigoPostal")) || "".equals(domicilio.string("idCiudad"))) {
			domicilioLegal.set("faltan_datos", true);
		}

		domicilioLegal.set("calle", domicilio.string("calle"));
		domicilioLegal.set("altura", domicilio.string("numero"));
		domicilioLegal.set("piso", domicilio.string("piso"));
		domicilioLegal.set("departamento", domicilio.string("departamento"));
		domicilioLegal.set("codigoPostal", domicilio.string("idCodigoPostal"));
		domicilioLegal.set("idPais", domicilio.integer("idPais"));
		domicilioLegal.set("idProvincia", domicilio.integer("idProvincia"));
		domicilioLegal.set("idLocalidad", domicilio.integer("idCiudad", 146));
		domicilioLegal.set("provincia", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));
		domicilioLegal.set("localidad", RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia", 1), domicilio.integer("idCiudad", 146)));
		domicilioLegal.set("entreCalle1", domicilio.string("calleEntre1"));
		domicilioLegal.set("entreCalle2", domicilio.string("calleEntre2"));
		return domicilioLegal;
	}

	public static Objeto validaDomicilioPostal(ContextoMB contexto, Boolean esMonoProductoTC, Boolean poseeCuentasUnipersonales) {
		Objeto domicilio = RestPersona.domicilioPostal(contexto, contexto.persona().cuit());
		if (domicilio == null) {
			domicilio = new Objeto();
		}
		Objeto domicilioPostal = new Objeto();
		domicilioPostal.set("faltan_datos", false);
		if ("".equals(domicilio.string("calle")) || "".equals(domicilio.string("numero")) || "".equals(domicilio.string("idCodigoPostal")) || "".equals(domicilio.string("idCiudad"))) {
			if (esMonoProductoTC || !poseeCuentasUnipersonales) {
				if (!"".equals(domicilio.string("numero"))) {
					domicilio.set("piso", "".equals(domicilio.string("piso")) ? "-" : domicilio.string("piso"));
					domicilio.set("departamento", "".equals(domicilio.string("departamento")) ? "-" : domicilio.string("departamento"));
					if (!"".equals(domicilio.string("calle")) && !"".equals(domicilio.string("numero")) && !"".equals(domicilio.string("idCodigoPostal")) && !"".equals(domicilio.string("idCiudad"))) {
						// si esto da error lo dejo pasar, le va a dar amarillo después (sólo por el
						// piso y el depto)
						RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio, "DP");
					} else {
						domicilioPostal.set("faltan_datos", true);
					}
				}
			} else {
				domicilioPostal.set("faltan_datos", true);
			}
		}

		domicilioPostal.set("calle", domicilio.string("calle"));
		domicilioPostal.set("altura", domicilio.string("numero"));
		domicilioPostal.set("piso", domicilio.string("piso"));
		domicilioPostal.set("departamento", domicilio.string("departamento"));
		domicilioPostal.set("codigoPostal", domicilio.string("idCodigoPostal"));
		domicilioPostal.set("idPais", domicilio.integer("idPais"));
		domicilioPostal.set("idProvincia", domicilio.integer("idProvincia"));
		domicilioPostal.set("idLocalidad", domicilio.integer("idCiudad", 146));
		domicilioPostal.set("provincia", RestCatalogo.nombreProvincia(contexto, domicilio.integer("idProvincia", 1)));
		domicilioPostal.set("localidad", RestCatalogo.nombreLocalidad(contexto, domicilio.integer("idProvincia", 1), domicilio.integer("idCiudad", 146)));
		domicilioPostal.set("entreCalle1", domicilio.string("calleEntre1"));
		domicilioPostal.set("entreCalle2", domicilio.string("calleEntre2"));
		return domicilioPostal;
	}

	public static void validaProfesion(ContextoMB contexto) {
		ApiResponseMB responseActividades = RestPersona.consultarActividades(contexto);
		if (!responseActividades.hayError()) {
			String idProfesion = "";
			String idRamo = "";
			String idCargo = "";
			BigDecimal ingresoNeto = null;
			Integer idActividad = null;

			boolean cargarActividad = true;
			boolean tienePrincipal = false;
			for (Objeto item : responseActividades.objetos()) {
				if (item.bool("esPrincipal")) {
					tienePrincipal = true;
					break;
				}
			}

			// La realidad es que tengo que recorrer todas las direcciones y por más que no
			// sea la principal
			// cambiarle la profesión en caso de venir 40001
			for (Objeto item : responseActividades.objetos()) {
				if ("".equals(item.string("fechaEgresoActividad"))) {
					// Si es una profesión actual limpio estas variables para fijarme si hay una que
					// le falte.
					idProfesion = "";
					idRamo = "";
					idCargo = "";
					ingresoNeto = null;
					idActividad = item.integer("id");
					String idSituacionLaboral = item.string("idSituacionLaboral");
					idProfesion = item.string("idProfesion");
					idRamo = item.string("idRamo");
					idCargo = item.string("idCargo");
					ingresoNeto = item.bigDecimal("ingresoNeto");

					if ("40001".equals(idProfesion) || "".equals(idProfesion)) {
						idProfesion = "11900";
						ApiResponseMB responseActividad = RestPersona.actualizarActividad(contexto, idActividad, idSituacionLaboral, idProfesion, idRamo, idCargo, ingresoNeto, null);
						if (!responseActividad.hayError()) {
							idProfesion = "11900";
							item.set("idProfesion", "11900");
						}
					}
				}
			}

			idProfesion = "";
			idRamo = "";
			idCargo = "";
			ingresoNeto = null;
			idActividad = null;

			if (tienePrincipal) {
				for (Objeto item : responseActividades.objetos()) {
					if ("".equals(item.string("fechaEgresoActividad"))) {
						// Si es una profesión actual limpio estas variables para fijarme si hay una que
						// le falte.
						idProfesion = "";
						idRamo = "";
						idCargo = "";
						ingresoNeto = null;
						idActividad = item.integer("id");
						idProfesion = item.string("idProfesion");
						idRamo = item.string("idRamo");
						idCargo = item.string("idCargo");
						ingresoNeto = item.bigDecimal("ingresoNeto");

						if ("40001".equals(idProfesion)) {
							idProfesion = "";
						}
						if (!"".equals(idProfesion) && !"".equals(idRamo) && ingresoNeto != null) {
							cargarActividad = false;
							// si una profesión tiene los tres datos, no hace falta actualizar ni dar de
							// alta ninguna profesión,
							// por lo tanto hago un break
							break;
						}
					}
				}
			}

			if (cargarActividad) {
				BigDecimal minimoSueldo = ConfigMB.bigDecimal("minimo_sueldo");
				if (minimoSueldo == null) {
					minimoSueldo = new BigDecimal(69500);
				}
				if ("".equals(idProfesion))
					idProfesion = "11900";
				if ("".equals(idRamo))
					idRamo = "040307";
				if ("".equals(idCargo))
					idCargo = "1501";
				if (ingresoNeto == null)
					ingresoNeto = minimoSueldo;
				if (tienePrincipal)
					RestPersona.actualizarActividad(contexto, idActividad, "1", idProfesion, idRamo, idCargo, ingresoNeto, null);
				else
					RestPersona.actualizarActividad(contexto, idActividad, "1", idProfesion, idRamo, idCargo, ingresoNeto, true);
			}
		}
	}

	public static Objeto seteaDatosPoliticos(Objeto datosPoliticos, Objeto cliente) {
		/* DATOS POLITICOS - DESDE */
		Objeto pregunta1 = new Objeto();
		pregunta1.set("value", cliente.bool("esSO"));
		pregunta1.set("desc", Constantes.TEXTO_SUJETO_OBLIGADO);
		pregunta1.set("textoAyuda", Constantes.TEXTO_AYUDA_SUJETO_OBLIGADO);

		Objeto pregunta2 = new Objeto();
		pregunta2.set("value", cliente.bool("esPEP"));
		pregunta2.set("desc", Constantes.TEXTO_EXPUESTO_POLITICAMENTE);
		pregunta2.set("textoAyuda", Constantes.TEXTO_AYUDA_EXP_POLITICAMENTE);

		Objeto pregunta3 = new Objeto();
		pregunta3.set("value", cliente.bool("indicioFatca") || !"L".equals(cliente.string("idResidencia")));
		pregunta3.set("desc", Constantes.TEXTO_EEUU_O_RESIDENCIA_FISCAL_OTRO_PAIS);
		pregunta3.set("textoAyuda", Constantes.TEXTO_AYUDA_RESID_FISCAL_OTRO_PAIS);

		datosPoliticos.add(pregunta1);
		datosPoliticos.add(pregunta2);
		datosPoliticos.add(pregunta3);

		/* DATOS POLITICOS - HASTA */
		return datosPoliticos;
	}

	public static Boolean validezDatosObligatorios(ContextoMB contexto) {
		Boolean esSujetoObligado = contexto.parametros.bool("sujetoObligado", null);
		Boolean esExpuestoPoliticamente = contexto.parametros.bool("expuestoPoliticamente", null);
		Boolean estadounidenseOResidenciaFiscalOtroPais = contexto.parametros.bool("estadounidenseOresidenciaFiscalOtroPais", null);

		return esSujetoObligado != null && esExpuestoPoliticamente != null && estadounidenseOResidenciaFiscalOtroPais != null && (esSujetoObligado || esExpuestoPoliticamente || estadounidenseOResidenciaFiscalOtroPais);
	}

	public static Boolean validaActualizaDatosEmail(ContextoMB contexto, String modificacion, String email) {
		String mailAnterior = contexto.persona().email();
		if (email != null && !"".equals(email)) {
			String modificacionMailAux = RestPersona.compararMailActualizado(contexto, email);
			ApiResponseMB responseMail = RestPersona.actualizarEmail(contexto, contexto.persona().cuit(), email);
			if (responseMail.hayError()) {
				return false;
			}
			contexto.insertarLogCambioMail(contexto, mailAnterior, email);
			modificacion += modificacionMailAux;
		}
		return true;
	}

	public static Boolean validaActualizaDatosTelefono(ContextoMB contexto, String modificacion, String celularCodigoArea, String celularNumero, String celularCaracteristica) {
		String mailAnterior = contexto.persona().email();
		String celularAnterior = contexto.persona().celular();

		if (celularCodigoArea != null && !"".equals(celularCodigoArea) && celularNumero != null && !"".equals(celularNumero)) {
			String modificacionCelularAux = RestPersona.compararCelularActualizado(contexto, celularCodigoArea, celularCaracteristica, celularNumero);
			ApiResponseMB responseCelular = RestPersona.actualizarCelular(contexto, contexto.persona().cuit(), celularCodigoArea, celularCaracteristica, celularNumero);
			if (responseCelular.hayError()) {
				RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, mailAnterior, celularAnterior);
				return false;
			}
			contexto.insertarLogCambioCelular(contexto, celularAnterior, celularCodigoArea + celularCaracteristica + celularNumero);

			if (!"".equals(modificacionCelularAux)) {
				modificacion = ("".equals(modificacion) ? modificacionCelularAux : modificacion + ", " + modificacionCelularAux);
			}
		}
		return true;
	}

	public static String validaActualizaDatosDomicilio(ContextoMB contexto) {
		String datoModificado = "";
		String calleParticular = contexto.parametros.string("domicilioPostal.calle");
		String alturaParticular = contexto.parametros.string("domicilioPostal.altura");
		String pisoParticular = contexto.parametros.string("domicilioPostal.piso");
		String departamentoParticular = contexto.parametros.string("domicilioPostal.departamento");
		String idProvinciaParticular = contexto.parametros.string("domicilioPostal.idProvincia");
		String idLocalidadParticular = contexto.parametros.string("domicilioPostal.idLocalidad");
		String codigoPostalParticular = contexto.parametros.string("domicilioPostal.codigoPostal");
		String entreCalle1Particular = contexto.parametros.string("domicilioPostal.entreCalle1");
		String entreCalle2Particular = contexto.parametros.string("domicilioPostal.entreCalle2");

		if ((calleParticular != null && !"".equals(calleParticular)) || (alturaParticular != null && !"".equals(alturaParticular)) || (idLocalidadParticular != null && !"".equals(idLocalidadParticular)) || (codigoPostalParticular != null && !"".equals(codigoPostalParticular))) {
			if (pisoParticular == null || "".equals(pisoParticular))
				pisoParticular = "-";
			if (departamentoParticular == null || "".equals(departamentoParticular))
				departamentoParticular = "-";
			if (entreCalle1Particular == null || "".equals(entreCalle1Particular))
				entreCalle1Particular = "-";
			if (entreCalle2Particular == null || "".equals(entreCalle2Particular))
				entreCalle2Particular = "-";

			Objeto domicilio = new Objeto();
			domicilio.set("calle", calleParticular);
			domicilio.set("numero", alturaParticular);
			domicilio.set("piso", pisoParticular);
			domicilio.set("departamento", departamentoParticular);
			domicilio.set("idProvincia", idProvinciaParticular);
			domicilio.set("idCiudad", idLocalidadParticular);
			domicilio.set("idCodigoPostal", codigoPostalParticular);
			domicilio.set("calleEntre1", entreCalle1Particular);
			domicilio.set("calleEntre2", entreCalle2Particular);

			String modificacionDireccionAux = RestPersona.compararDomicilioActualizado(contexto, calleParticular, alturaParticular, pisoParticular, departamentoParticular, idProvinciaParticular, idLocalidadParticular, codigoPostalParticular);

			String salida = OriginacionHelper.actualizarDomicilio(contexto, domicilio, datoModificado, null, null, "DP");
			if (!"".equals(salida)) {
				return salida;
			}

			if (!"".equals(modificacionDireccionAux)) {
				datoModificado = ("".equals(datoModificado) ? modificacionDireccionAux : datoModificado + ", " + modificacionDireccionAux);
			}
		}
		return datoModificado;
	}

	public static String validaActualizaDomicilioEntrega(ContextoMB contexto, String datoModificado) {

		String calleUnicaEntrega = contexto.parametros.string("domicilioLegal.calle");
		String alturaUnicaEntrega = contexto.parametros.string("domicilioLegal.altura");
		String pisoUnicaEntrega = contexto.parametros.string("domicilioLegal.piso");
		String departamentoUnicaEntrega = contexto.parametros.string("domicilioLegal.departamento");
		String idProvinciaUnicaEntrega = contexto.parametros.string("domicilioLegal.idProvincia");
		String idLocalidadUnicaEntrega = contexto.parametros.string("domicilioLegal.idLocalidad");
		String codigoPostalUnicaEntrega = contexto.parametros.string("domicilioLegal.codigoPostal");
		String entreCalle1UnicaEntrega = contexto.parametros.string("domicilioLegal.entreCalle1");
		String entreCalle2UnicaEntrega = contexto.parametros.string("domicilioLegal.entreCalle2");
		String mailAnterior = contexto.persona().email();
		String celularAnterior = contexto.persona().celular();

		if ((calleUnicaEntrega != null && !"".equals(calleUnicaEntrega)) || (alturaUnicaEntrega != null && !"".equals(alturaUnicaEntrega)) || (pisoUnicaEntrega != null && !"".equals(pisoUnicaEntrega)) || (departamentoUnicaEntrega != null && !"".equals(departamentoUnicaEntrega)) || (idLocalidadUnicaEntrega != null && !"".equals(idLocalidadUnicaEntrega)) || (codigoPostalUnicaEntrega != null && !"".equals(codigoPostalUnicaEntrega))) {
			if (pisoUnicaEntrega == null || "".equals(pisoUnicaEntrega))
				pisoUnicaEntrega = "-";
			if (departamentoUnicaEntrega == null || "".equals(departamentoUnicaEntrega))
				departamentoUnicaEntrega = "-";
			if (entreCalle1UnicaEntrega == null || "".equals(entreCalle1UnicaEntrega))
				entreCalle1UnicaEntrega = "-";
			if (entreCalle2UnicaEntrega == null || "".equals(entreCalle2UnicaEntrega))
				entreCalle2UnicaEntrega = "-";

			Objeto domicilio = new Objeto();
			domicilio.set("calle", calleUnicaEntrega);
			domicilio.set("numero", alturaUnicaEntrega);
			domicilio.set("piso", pisoUnicaEntrega);
			domicilio.set("departamento", departamentoUnicaEntrega);
			domicilio.set("idCiudad", idLocalidadUnicaEntrega);
			domicilio.set("idCodigoPostal", codigoPostalUnicaEntrega);
			domicilio.set("idProvincia", idProvinciaUnicaEntrega);
			domicilio.set("calleEntre1", entreCalle1UnicaEntrega);
			domicilio.set("calleEntre2", entreCalle2UnicaEntrega);
			String salida = OriginacionHelper.actualizarDomicilio(contexto, domicilio, datoModificado, mailAnterior, celularAnterior, "LE");
			if (!"".equals(salida)) {
				return salida;
			}
		}
		return "";
	}

	public static String validaSeguroDesempleoSegunEdad(ContextoMB contexto, String idSolicitud, String idPrestamo) {

		if (!contexto.esJubilado() && contexto.persona().edad() < 65) {
			ApiResponseMB responseSeguroDesempleo = RestVenta.agregarSeguroDesempleo(contexto, idSolicitud, idPrestamo);
			if (responseSeguroDesempleo.hayError() || !responseSeguroDesempleo.objetos("Errores").isEmpty()) {
				return "false";
			}
			return "true";
		}
		return "";
	}

	public static RespuestaMB validaPersonaEdad(ContextoMB contexto) {
		Integer plazo = contexto.parametros.integer("plazo");
		if (contexto.persona().edad() == null)
			return RespuestaMB.estado("ERROR_SIN_EDAD");

		if (contexto.persona().edad() > 76)
			return RespuestaMB.estado("MAYOR_DE_EDAD_76");

		if (contexto.persona().edad() > 74 && plazo > 24)
			return RespuestaMB.estado("MAYOR_DE_EDAD_74");

		return new RespuestaMB();
	}

	public static String validaCuentaExistente(ContextoMB contexto, String idCuenta) {
		if (idCuenta == null) { // Me fijo si tiene cuenta.
			if (contexto.poseeCuentasUnipersonales()) {
				for (Cuenta itemCuenta : contexto.cuentas()) {
					if (itemCuenta.unipersonal() && itemCuenta.idMoneda().equals("80") && !"I".equals(itemCuenta.idEstado())) {
						idCuenta = itemCuenta.id();
						Cuenta cuenta = contexto.cuenta(idCuenta);
						if (cuenta == null) {
							return null;
						}

					}
				}
			}
			return "";
		}
		return idCuenta;

	}

	public static String validaCuentaYTarjetas(Cuenta cuenta, Solicitud solicitud, ContextoMB contexto, String idSolicitud, String idPrestamo, Objeto solicitudDatos) {

		boolean tieneSeguroSolicitado = false;
		String idSeguroSolicitado = "";
		String idProductoCajaAhorro = "";
		String idProductoTarjetaDebito = "";
		TarjetaDebito tarjetaDebito = contexto.tarjetaDebitoPorDefecto();

		boolean tieneCajaAhorroSolicitada = false;
		boolean tieneTarjetaDebitoSolicitada = false;

		// for (Objeto datoSolicitud : response.objetos("Datos")) {
		if (solicitud.IdSolicitud.equals(idSolicitud)) {
			for (SolicitudProducto productoSolicitud : solicitud.Productos) {
				if ("2".equals(productoSolicitud.tipoProducto)) {
					if (!idPrestamo.equals(productoSolicitud.Id)) {
						// el idPrestamo que pasó por parámetro es diferente al encontrado en la
						// solicitud
						return "ERROR";
					}
				}
				if ("8".equals(productoSolicitud.tipoProducto)) {
					tieneCajaAhorroSolicitada = true;
					idProductoCajaAhorro = productoSolicitud.Id;

					// tengo que fijarme si la caja de ahorro no viene con datos nulos
					// si viene con datos nulos la elimino.
					String recurso = RestOmnicanalidad.recursos().get("8");
					ApiResponseMB responseProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProductoCajaAhorro);
					if (responseProducto.hayError()) {
						return "ERROR";
					}
					Objeto datosProductoCajaAhorro = responseProducto.objetos("Datos").get(0);
					if ("".equals(datosProductoCajaAhorro.string("ProductoBancario")) || "0".equals(datosProductoCajaAhorro.string("Oficial"))) {
						ApiResponseMB responseBajaCA = RestVenta.eliminarCajaAhorroPesos(contexto, idSolicitud, idProductoCajaAhorro);
						if (!responseBajaCA.hayError() && responseBajaCA.objetos("Errores").isEmpty()) {
							tieneCajaAhorroSolicitada = false;
							idProductoCajaAhorro = "";
						} else { // hubo un error al tratar de eliminarla, tengo que responder error
							return "ERROR_BAJA_CA_SIN_DATOS";
						}
					}
				}
				if ("11".equals(productoSolicitud.tipoProducto)) {
					tieneTarjetaDebitoSolicitada = true;
					idProductoTarjetaDebito = productoSolicitud.Id;
					// tengo que fijarme si la caja de ahorro no viene con datos nulos
					// si viene con datos nulos la elimino.
					String recurso = RestOmnicanalidad.recursos().get("11");
					ApiResponseMB responseProducto = RestOmnicanalidad.buscarProducto(contexto, idSolicitud, recurso, idProductoTarjetaDebito);
					if (responseProducto.hayError()) {
						return "ERROR";
					}
					Objeto datosProductoTarjetaDebito = responseProducto.objetos("Datos").get(0);
					if ("".equals(datosProductoTarjetaDebito.string("Tipo")) || "0".equals(datosProductoTarjetaDebito.string("Oficial"))) {
						ApiResponseMB responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
						if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
							tieneTarjetaDebitoSolicitada = false;
							idProductoTarjetaDebito = "";
						} else { // hubo un error al tratar de eliminarla, tengo que responder error
							return "ERROR_BAJA_TD_SIN_DATOS";
						}
					}

				}
				if ("31".equals(productoSolicitud.tipoProducto)) {
					tieneSeguroSolicitado = true;
					idSeguroSolicitado = productoSolicitud.Id;
				}

			}
			// }

			if (!tieneCajaAhorroSolicitada && cuenta == null) {
				ApiResponseMB responseCajaAhorro = RestVenta.generarCajaAhorroPesos(contexto, idSolicitud);
				if (responseCajaAhorro.hayError() || responseCajaAhorro.objetos("Errores").size() > 0) {
					return "ERROR_SOLICITUD_CAJA_AHORRO";
				}
				tieneCajaAhorroSolicitada = true;
				// Sólo pido la tarjeta de débito si no tiene cuenta
				if (!tieneTarjetaDebitoSolicitada && tarjetaDebito == null) {
					ApiResponseMB responseTarjetaDebito = RestVenta.generarTarjetaDebito(contexto, idSolicitud, "0");
					if (responseTarjetaDebito.hayError() || responseTarjetaDebito.objetos("Errores").size() > 0) {
						return "ERROR_SOLICITUD_TARJETA_DEBITO";
					}
					tieneTarjetaDebitoSolicitada = true;
				}
			}

			if (contexto.tarjetaDebitoPorDefecto() == null && cuenta == null && !tieneTarjetaDebitoSolicitada && tieneCajaAhorroSolicitada) {
				ApiResponseMB responseTarjetaDebito = RestVenta.generarTarjetaDebito(contexto, idSolicitud, "0");
				if (responseTarjetaDebito.hayError() || responseTarjetaDebito.objetos("Errores").size() > 0) {
					return "ERROR_SOLICITUD_TARJETA_DEBITO";
				}
				tieneTarjetaDebitoSolicitada = true;
			}

			if (tieneCajaAhorroSolicitada && cuenta != null) {
				ApiResponseMB responseBajaCA = RestVenta.eliminarCajaAhorroPesos(contexto, idSolicitud, idProductoCajaAhorro);
				if (!responseBajaCA.hayError() && responseBajaCA.objetos("Errores").isEmpty()) {
					tieneCajaAhorroSolicitada = false;
				}
				// Y le elimino también la tarjeta asociada:
				ApiResponseMB responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
				if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
					tieneTarjetaDebitoSolicitada = false;
					idProductoTarjetaDebito = "";
				}
			}

			if (contexto.tarjetaDebitoPorDefecto() != null && tieneTarjetaDebitoSolicitada) {
				ApiResponseMB responseBajaTD = RestVenta.eliminarTarjetaDebito(contexto, idSolicitud, idProductoTarjetaDebito);
				if (!responseBajaTD.hayError() && responseBajaTD.objetos("Errores").isEmpty()) {
					tieneTarjetaDebitoSolicitada = false;
					idProductoTarjetaDebito = "";
				}
			}

		}

		solicitudDatos.set("tieneSeguroSolicitado", tieneSeguroSolicitado);
		solicitudDatos.set("idSeguroSolicitado", idSeguroSolicitado);
		solicitudDatos.set("idProductoCajaAhorro", idProductoCajaAhorro);
		solicitudDatos.set("idProductoTarjetaDebito", idProductoTarjetaDebito);
		return "";
	}

	public static String actualizaSeguroDesempleo(ContextoMB contexto, String idSolicitud, String idPrestamo, Objeto solicitudDatos, Boolean quiereSeguroDesempleo) {
		if (solicitudDatos.bool("tieneSeguroSolicitado") && !quiereSeguroDesempleo) {
			// la solicitud tiene seguro de desempleo, pero como el usuario no lo quiere lo
			// tengo que eliminar de la solicitud
			ApiResponseMB eliminacionSeguroDesempleo = RestVenta.eliminarSeguroDesempleo(contexto, idSolicitud, solicitudDatos.string("idSeguroSolicitado"));
			if (eliminacionSeguroDesempleo.hayError()) {
				return "ERROR_DELETE_SEGURO_DESEMPLEO";
			}
			solicitudDatos.set("tieneSeguroSolicitado", false);
		}

		RestOmnicanalidad.actualizarCanalSolicitud(contexto, idSolicitud);

		if (!solicitudDatos.bool("tieneSeguroSolicitado") && quiereSeguroDesempleo) {
			// la solicitud NO tiene seguro de desempleo, pero como el usuario lo quiere lo
			// tengo que agregar a la solicitud
			ApiResponseMB responseSeguroDesempleo = RestVenta.agregarSeguroDesempleo(contexto, idSolicitud, idPrestamo);
			if (responseSeguroDesempleo.hayError() || !responseSeguroDesempleo.objetos("Errores").isEmpty()) {
				return "ERROR_POST_SEGURO_DESEMPLEO";
			}
			solicitudDatos.set("tieneSeguroSolicitado", true);
			solicitudDatos.set("idSeguroSolicitado", responseSeguroDesempleo.objetos("Datos").get(0).string("Id"));
		}
		return "";
	}

	public static RespuestaMB validaMontoModifSolicitud(ContextoMB contexto, Solicitud solicitud, Cuenta cuenta) {
		// Si la solicitud quedó con un monto diferente al que ingreso el usuario, es
		// porque no me aceptó el crédito que él pidió, sino uno de menor monto.
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		Integer plazo = contexto.parametros.integer("plazo");
		Integer diaVencimiento = contexto.parametros.integer("diaVencimiento");
		SolicitudPrestamo prestamoGet = solicitud.consultarPrestamoPersonal(contexto, solicitud.idPrestamo());

		if (prestamoGet != null) {
			if (prestamoGet.MontoAprobado.compareTo(monto) < 0) {
				RespuestaMB RespuestaErrorMonto = new RespuestaMB();
				RespuestaErrorMonto.setEstado("MONTO_APROBADO_MENOR_AL_PEDIDO");
				RespuestaErrorMonto.set("montoAprobado", prestamoGet.MontoAprobado);
				RespuestaErrorMonto.set("montoAprobadoFormateado", Formateador.importe(prestamoGet.MontoAprobado));
				Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "validaMontoModifSolicitud", null, RespuestaErrorMonto.toJson());
				return RespuestaErrorMonto;
			}
		} else {
			Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "validaMontoModifSolicitud", null, "ERROR_CONSULTANDO_PRESTAMO");
			return RespuestaMB.estado("ERROR_CONSULTANDO_PRESTAMO");
		}

		// Ahora, cuando pase la evaluación. La vuelvo a mandar con 03.
		// No me queda alternativa que hacerlo así, ya que hay campos que sino no
		// impactan. Por ejemplo el dia de vencimiento
		// y quiero que eso se guarde para que la próxima que lo busque que lo tome.
		ApiResponseMB prestamoModificar = RestVenta.modificarSolicitudPrestamoPersonal(contexto, solicitud.IdSolicitud, solicitud.idPrestamo(), monto, plazo, diaVencimiento, cuenta == null ? "0" : cuenta.numero(), "03");

		if (prestamoModificar.hayError() || prestamoModificar.objetos("Errores") != null && prestamoModificar.objetos("Errores").size() > 0) {
			Solicitud.logOriginacion(contexto, solicitud.IdSolicitud, "validaMontoModifSolicitud", prestamoModificar, "");
			return RespuestaMB.error();
		}

		return new RespuestaMB();
	}

	public static String actualizarDomicilio(ContextoMB contexto, Objeto domicilio, String modificacion, String emailAnterior, String celularAnterior, String tipo) {

		ApiResponseMB response = RestPersona.actualizarDomicilio(contexto, contexto.persona().cuit(), domicilio, tipo);
		if (response.hayError()) {
			RestPersona.enviarMailActualizacionDatosPersonales(contexto, modificacion, emailAnterior, celularAnterior);
			String error = "ERROR";
			error = response.string("mensajeAlUsuario").contains("NO EXISTE CODIGO POSTAL") ? "ERROR_NO_EXISTE_CODIGO_POSTAL" : error;
			error = response.string("mensajeAlUsuario").contains("DEBEN SER NUMERICOS") ? "ERROR_PARAMETROS_INCORRECTOS" : error;
			error = response.string("mensajeAlUsuario").contains("EXISTEN MODIFICACIONES PENDIENTES CON SMART OPEN") ? "CAMBIO_PENDIENTE" : error;

			if ("LE".equals(tipo)) {
				error = response.string("mensajeAlUsuario").contains("NO EXISTE CODIGO POSTAL") ? "ERROR_NO_EXISTE_CODIGO_POSTAL_DOMICILIO_UNICA_ENTREGA" : error;
				error = response.string("mensajeAlUsuario").contains("DEBEN SER NUMERICOS") ? "ERROR_PARAMETROS_INCORRECTOS_DOMICILIO_UNICA_ENTREGA" : error;

			}
			return error;
		}
		return "";
	}

}
