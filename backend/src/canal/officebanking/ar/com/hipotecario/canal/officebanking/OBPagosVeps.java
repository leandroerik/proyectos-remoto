package ar.com.hipotecario.canal.officebanking;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.ApiLinkPagosVep;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.Contribuyentes;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.TokenAfip;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.VepsPendientes;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.veps.CuentaConVepDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.veps.TributoVepDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoVepsOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagosVepOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;

public class OBPagosVeps extends ModuloOB {

	public static Object cargarPagoVep(ContextoOB contexto) {
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
		ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
		ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
		ServicioHistorialPagoVepsOB servicioHistorialPagoVeps = new ServicioHistorialPagoVepsOB(contexto);
		try {
			// Obtengo la sesion
			SesionOB sesion = contexto.sesion();
			List<Object> datosVeps = new ArrayList<>();
			Gson gson = new Gson();
			int pagina = 1;

			Type listType = new TypeToken<CuentaConVepDTO>() {
			}.getType();

			// Convierte el JSON en una lista de objetos CuentaConVepDTO

			CuentaConVepDTO cuentaConVepList = gson.fromJson(contexto.request.body(), listType);

			String numeroCuenta = cuentaConVepList.getNumeroCuenta();

			// Obtengo el numero de tarjeta virtual

			List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
            if (empty(tarjetasVirtuales)) {
                return respuesta("TARJETA_VIRTUAL_INVALIDA");
            }
            String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

			// Obtengo la cuenta
			Objeto cuenta = OBCuentas.cuenta(contexto, numeroCuenta);
			if (empty(cuenta) || cuenta == null) {
				return respuesta("CUENTA_INVALIDA");
			}

			List<VepsPendientes.Vep> vepFiltrado = new ArrayList<>();
			VepsPendientes vepPendientes = new VepsPendientes();

			for (TributoVepDTO ve : cuentaConVepList.getListaVep()) {

				if (cuentaConVepList.getTipoConsultaLink().equals("3")) {
					String vep = ve.getNumeroVep();

					if (ve.getNumeroVep().length() != 12) {
						vep = StringUtils.leftPad(ve.getNumeroVep(), 12, "0");
					}

					vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(), ve.getIdTributarioContribuyente(), String.valueOf(sesion.empresaOB.cuit), ve.getIdTributarioOriginante(), null, tarjetaVirtual, vep, String.valueOf(pagina), cuentaConVepList.getTipoConsultaLink()).get();

				} else if (cuentaConVepList.getTipoConsultaLink().equals("1") || cuentaConVepList.getTipoConsultaLink().equals("2")) {

					vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(), ve.getIdTributarioContribuyente(), String.valueOf(sesion.empresaOB.cuit), ve.getIdTributarioOriginante(), null, tarjetaVirtual, ve.getNumeroVep(), String.valueOf(pagina), cuentaConVepList.getTipoConsultaLink()).get();
				}

				try {

					while (vepPendientes.veps.stream().noneMatch(v -> v.informacionVep.nroVEP.equals(ve.getNumeroVep()))) {
						pagina++;

						if (cuentaConVepList.getTipoConsultaLink().equals("3")) {
							String vep = ve.getNumeroVep();

							if (ve.getNumeroVep().length() != 12) {
								vep = StringUtils.leftPad(ve.getNumeroVep(), 12, "0");
							}

							vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(), ve.getIdTributarioContribuyente(), String.valueOf(sesion.empresaOB.cuit), ve.getIdTributarioOriginante(), null, tarjetaVirtual, vep, String.valueOf(pagina), cuentaConVepList.getTipoConsultaLink()).get();

						} else if (cuentaConVepList.getTipoConsultaLink().equals("1") || cuentaConVepList.getTipoConsultaLink().equals("2")) {

							vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, contexto.sesion().empresaOB.cuit.toString(), ve.getIdTributarioContribuyente(), String.valueOf(sesion.empresaOB.cuit), ve.getIdTributarioOriginante(), null, tarjetaVirtual, ve.getNumeroVep(), String.valueOf(pagina), cuentaConVepList.getTipoConsultaLink()).get();
						}
					}

					vepFiltrado.add(vepPendientes.veps.stream().filter(v -> v.informacionVep.nroVEP.equals(ve.getNumeroVep())).filter(v -> v.token != null).findFirst().get());

				} catch (Exception e) {
					/* datosVeps.add */
					return (respuesta("ERROR", "descripcion", "NO SE ENCONTRO EL VEP " + ve.getNumeroVep()));
				}
			}

			for (VepsPendientes.Vep vepFilter : vepFiltrado) {

				TributoVepDTO input = cuentaConVepList.getListaVep().stream().filter(v -> v.getNumeroVep().equals(vepFilter.nroVep)).findFirst().get();

				PagosVepOB pago = servicioPagoVeps.crear(contexto, vepFilter.informacionVep.usuario.idTributario, sesion.empresaOB.cuit.toString(), vepFilter.informacionVep.contribuyente.idTributario, vepFilter.informacionVep.importe, vepFilter.token, numeroCuenta, cuenta.get("tipoProducto").toString(), tarjetaVirtual, vepFilter.nroVep, sesion.empresaOB, vepFilter.informacionVep.pagoDesc, vepFilter.informacionVep.fechaExpiracion, cuentaConVepList.getTipoConsultaLink(), input.getIdTributarioOriginante()).get();

				BandejaOB bandeja = servicioBandeja.find(pago.id).get();
				EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
				AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
				EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
				servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
				servicioHistorialPagoVeps.crear(pago, accionCrear, empresaUsuario);
				contexto.parametros.set("idBandeja", pago.id);
				datosVeps.add(detalleSolicitud(contexto));
			}

			return respuesta("datos", datosVeps);

		} catch (Exception e) {
			contexto.response.status(400); // Bad Request en caso de error
			return "Error al procesar la solicitud: " + e.getMessage();
		}
	}

	public static Object detalleSolicitud(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		Integer idPago = contexto.parametros.integer("idBandeja");

		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);

		PagosVepOB pago = servicioPagoVeps.find(idPago).get();

		if (pago == null || !pago.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
			return respuesta("DATOS_INVALIDOS");
		}
		Objeto pagoDetalle = new Objeto();
		pagoDetalle.set("idOperacion", pago.id);
		pagoDetalle.set("numeroVep", pago.numeroVep);
		pagoDetalle.set("descripcion", pago.descripcion);
		pagoDetalle.set("fechaCreacion", pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
		pagoDetalle.set("importe", pago.monto);
		pagoDetalle.set("cuentaOrigen", pago.cuentaOrigen);
		pagoDetalle.set("tipo", pago.tipoProductoFirma.descripcion);
		pagoDetalle.set("monedaSimbolo", pago.moneda.simbolo);
		pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
		pagoDetalle.set("fechaVencimiento", pago.fechaVencimiento.toString());

		if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
			pagoDetalle.set("estado", pago.estado.descripcion);
		} else
			pagoDetalle.set("estado", pago.estadoBandeja.descripcion);

		return respuesta("datos", pagoDetalle);
	}

	public static Object detalleComprobante(ContextoOB contexto) {
		Integer idBandeja = contexto.parametros.integer("idBandeja");
		Objeto datos = new Objeto();
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);

		PagosVepOB pago = servicioPagoVeps.find(idBandeja).get();
		if (pago == null) {
			return respuesta("No existe comprobante con ese ID de operacion");
		}

		datos.set("idOperacion", idBandeja);
		datos.set("numeroVep", pago.numeroVep);
		datos.set("descripcion", pago.descripcion);
		datos.set("fechaCreacion", pago.fechaCreacion.toLocalDate().toString());
		datos.set("monto", pago.monto);
		datos.set("cuenta", pago.cuentaOrigen);
		Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
		datos.set("cbu", cuenta.get("cbu"));

		datos.set("tipo", pago.tipoProductoFirma.descripcion);
		datos.set("moneda", pago.moneda.simbolo);
		datos.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
		datos.set("vencimiento", pago.fechaVencimiento.toString());
		datos.set("cuitContribuyente", pago.idTributarioContribuyente);

		BandejaOB bandeja = servicioBandeja.find(idBandeja).get();
		datos.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

		return datos;
	}

	public static Object tokenAFIP(ContextoOB contexto) {
		String idTributarioCliente = contexto.parametros.string("cuil");

		SesionOB sesion = contexto.sesion();
		String idTributarioEmpresa = sesion.empresaOB.cuit.toString();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		TokenAfip ecs = ApiLinkPagosVep.tokenAfip(contexto, idTributarioCliente, idTributarioEmpresa, tarjetaVirtual).get();

		Objeto datos = new Objeto();
		datos.set("token", ecs.token);
		datos.set("firma", ecs.firma);
		datos.set("urlAfip", ecs.urlAfip);
		datos.set("accion", ecs.accion);

		return respuesta("datos", datos);
	}

	public static Object altaContribuyente(ContextoOB contexto) {
		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente");
		String referencia = contexto.parametros.string("referencia");

		SesionOB sesion = contexto.sesion();
		String idTributarioCliente = sesion.usuarioOB.cuil.toString();
		String idTributarioEmpresa = sesion.empresaOB.cuit.toString();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		return respuesta(ApiLinkPagosVep.agregarContribuyente(contexto, idTributarioCliente, idTributarioEmpresa, tarjetaVirtual, idTributarioContribuyente, referencia).get().toString());
	}

	public static Object consultaContribuyente(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();

		String idTributarioCliente = sesion.empresaOB.cuit.toString();
		String idTributarioEmpresa = sesion.empresaOB.cuit.toString();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		Contribuyentes contribuyentes = ApiLinkPagosVep.contribuyentes(contexto, idTributarioCliente, idTributarioEmpresa, null, tarjetaVirtual, null).get();

		return respuesta("datos", contribuyentes.contribuyentes);
	}

	public static Object bajaContribuyente(ContextoOB contexto) {

		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente");

		SesionOB sesion = contexto.sesion();
		String idTributarioCliente = sesion.usuarioOB.cuil.toString();
		String idTributarioEmpresa = sesion.empresaOB.cuit.toString();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		return respuesta(ApiLinkPagosVep.eliminarContribuyente(contexto, idTributarioCliente, idTributarioEmpresa, tarjetaVirtual, idTributarioContribuyente).get().toString());
	}

	public static Object detallePago(ContextoOB contexto) {
		Integer idBandeja = contexto.parametros.integer("idBandeja");

		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		Objeto datos = new Objeto();

		PagosVepOB vep = servicioPagoVeps.find(idBandeja).get();

		datos.set("importe", vep.monto);
		datos.set("fechaPago", LocalDate.now() + " " + LocalTime.now().withSecond(0).withNano(0));
		datos.set("idOperacion", vep.id);
		datos.set("descripcion", vep.descripcion);
		datos.set("idTributarioContribuyente", vep.idTributarioContribuyente);
		datos.set("fechaCreacion", vep.fechaCreacion.toLocalDate().toString() + " " + vep.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
		datos.set("numeroVep", vep.numeroVep);
		datos.set("cuenta", vep.cuentaOrigen);

		Objeto estado = new Objeto();
		estado.set("id", vep.estadoBandeja.id);
		estado.set("descripcionCorta", vep.estadoBandeja.descripcion);
		datos.set("estado", estado);

		datos.set("estadoOperacion", vep.estado.descripcion);

		Objeto cuenta = OBCuentas.cuenta(contexto, vep.cuentaOrigen);
		datos.set("saldoCuenta", cuenta.get("saldoGirar"));
				
		BandejaOB bandeja = servicioBandeja.find(idBandeja).get();
		datos.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));	

		return respuesta("datos", datos);
	}

	public static Object pagarVepPropio(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPagoOB = new ServicioEstadoPagoOB(contexto);
		EstadoPagoOB estadoEnBandeja = servicioEstadoPagoOB.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		List<Object> datos = new ArrayList<>();

		try {
			VepsPendientes vepsPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), null, tarjetaVirtual, null, null, "1").get();

			datos.addAll(vepsPendientes.veps.stream().filter(vep -> vep.token != null).map(vep -> {
				List<PagosVepOB> data = servicioPagoVeps.buscarPorEmpresaYFiltros(sesion.empresaOB, null, null, estadoEnBandeja, vep.informacionVep.nroVEP).get();
				Objeto vepPendiente = new Objeto();
				vepPendiente.set("datosVep", vep);
				if (data.isEmpty()) {
					vepPendiente.set("idEstadoPago", EnumEstadoPagosDeServicioYVepsOB.DISPONIBLE.getCodigo());
					vepPendiente.set("descriptionEstadoPago", EnumEstadoPagosDeServicioYVepsOB.DISPONIBLE.toString());

				} else {
					vepPendiente.set("idEstadoPago", data.get(0).estado.id);
					vepPendiente.set("descriptionEstadoPago", data.get(0).estado.descripcion);
				}

				return vepPendiente;
			}).toList());

			for (int pagina = 2; pagina < vepsPendientes.paginaSiguiente + 1; pagina++) {
				vepsPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), null, tarjetaVirtual, null, String.valueOf(pagina), "1").get();

				datos.addAll(vepsPendientes.veps.stream().filter(vep -> vep.token != null).map(vep -> {

					List<PagosVepOB> data = servicioPagoVeps.buscarPorEmpresaYFiltros(sesion.empresaOB, null, null, estadoEnBandeja, vep.informacionVep.nroVEP).get();

					Objeto vepPendiente = new Objeto();
					vepPendiente.set("datosVep", vep);

					if (data.isEmpty()) {
						vepPendiente.set("idEstadoPago", 0);
						vepPendiente.set("descriptionEstadoPago", "");

					} else {
						vepPendiente.set("idEstadoPago", data.get(0).estado.id);
						vepPendiente.set("descriptionEstadoPago", data.get(0).estado.descripcion);
					}

					return vepPendiente;
				}).toList());

			}
		} catch (ApiException e) {
			if (e.response.body.contains("su respuesta no tiene ningún contenido")) {
				return respuesta("datos", "NO HAY VEPS PENDIENTES");
			}

		}

		return respuesta("datos", datos);
	}

	public static Object pagarTercero(ContextoOB contexto) {
		String idTributarioOriginante = contexto.parametros.string("idTributarioOriginante");
		SesionOB sesion = contexto.sesion();
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPagoOB = new ServicioEstadoPagoOB(contexto);
		EstadoPagoOB estadoEnBandeja = servicioEstadoPagoOB.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		List<Objeto> datos = new ArrayList<>();

		try {
			VepsPendientes vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), null, idTributarioOriginante, null, tarjetaVirtual, null, null, "2").get();

			datos.addAll(vepPendientes.veps.stream().filter(vep -> vep.token != null).map(vep -> {
				List<PagosVepOB> data = servicioPagoVeps.buscarPorEmpresaYFiltros(sesion.empresaOB, null, null, estadoEnBandeja, vep.informacionVep.nroVEP).get();

				Objeto vepPendiente = new Objeto();
				vepPendiente.set("datosVep", vep);

				if (data.isEmpty()) {
					vepPendiente.set("idEstadoPago", EnumEstadoPagosDeServicioYVepsOB.DISPONIBLE.getCodigo());
					vepPendiente.set("descriptionEstadoPago", EnumEstadoPagosDeServicioYVepsOB.DISPONIBLE.toString());

				} else {
					vepPendiente.set("idEstadoPago", data.get(0).estado.id);
					vepPendiente.set("descriptionEstadoPago", data.get(0).estado.descripcion);
				}

				return vepPendiente;
			}).toList());

			for (int pagina = 2; pagina < vepPendientes.paginaSiguiente + 1; pagina++) {
				vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, sesion.empresaOB.cuit.toString(), sesion.empresaOB.cuit.toString(), null, idTributarioOriginante, null, tarjetaVirtual, null, String.valueOf(pagina), "2").get();

				datos.addAll(vepPendientes.veps.stream().filter(vep -> vep.token != null).map(vep -> {
					List<PagosVepOB> data = servicioPagoVeps.buscarPorEmpresaYFiltros(sesion.empresaOB, null, null, estadoEnBandeja, vep.informacionVep.nroVEP).get();

					Objeto vepPendiente = new Objeto();
					vepPendiente.set("datosVep", vep);

					if (data.isEmpty()) {
						vepPendiente.set("idEstadoPago", 0);
						vepPendiente.set("descriptionEstadoPago", "");

					} else {
						vepPendiente.set("idEstadoPago", data.get(0).estado.id);
						vepPendiente.set("descriptionEstadoPago", data.get(0).estado.descripcion);
					}

					return vepPendiente;
				}).toList());

			}
		} catch (ApiException e) {
			if (e.response.body.contains("su respuesta no tiene ningún contenido")) {
				return respuesta("datos", "NO HAY VEPS PENDIENTES");
			}

		}

		return respuesta("datos", datos);
	}

	public static Object pagarOtroContribuyente(ContextoOB contexto) {
		String numeroVep = contexto.parametros.string("numeroVep");
		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente");
		String idTributarioOriginante = contexto.parametros.string("idTributarioOriginante");

		SesionOB sesion = contexto.sesion();
		int pagina = 1;

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
        if (empty(tarjetasVirtuales)) {
            return respuesta("TARJETA_VIRTUAL_INVALIDA");
        }
        String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		String vepDe12 = numeroVep;

		if (numeroVep.length() != 12) {
			vepDe12 = StringUtils.leftPad(numeroVep, 12, "0");
		}

		VepsPendientes vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, String.valueOf(contexto.sesion().empresaOB.cuit), idTributarioContribuyente, null, idTributarioOriginante, "10", tarjetaVirtual, vepDe12, "1", "3").get();

		Objeto datos = new Objeto();

		while (vepPendientes.veps.stream().noneMatch(v -> v.informacionVep.nroVEP.equals(numeroVep))) {
			pagina++;
			vepPendientes = ApiLinkPagosVep.vepPendientesNuevo(contexto, null, null, String.valueOf(contexto.sesion().empresaOB.cuit), idTributarioContribuyente, String.valueOf(sesion.empresaOB.cuit), idTributarioOriginante, "10", tarjetaVirtual, vepDe12, String.valueOf(pagina), "3").get();
		}

		try {
			vepPendientes.veps.stream().filter(vep -> vep.token != null).filter(vep -> vep.informacionVep.nroVEP.equals(numeroVep)).map(vep -> {
				Objeto vepPendiente = new Objeto();
				vepPendiente.set("numeroVep", vep.informacionVep.nroVEP);
				vepPendiente.set("importe", vep.informacionVep.importe);
				vepPendiente.set("detalle", vep.informacionVep.orgRecaudDesc);
				vepPendiente.set("cuit", vep.informacionVep.contribuyente.idTributario);
				vepPendiente.set("fechaOriginacion", vep.informacionVep.fechaHoraCreacion);
				vepPendiente.set("fechaVencimiento", vep.informacionVep.fechaExpiracion);
				vepPendiente.set("idTributarioContribuyente", vep.informacionVep.contribuyente.idTributario);
				vepPendiente.set("idTributarioOriginante", vep.informacionVep.usuario.idTributario);

				datos.add(vepPendiente);
				return vepPendiente;
			}).findFirst().get();
		} catch (Exception e) {
			return respuesta("NO SE ENCONTRO EL VEP: " + numeroVep);
		}

		return respuesta("datosVep", vepPendientes.veps.stream().filter(vep -> vep.informacionVep.nroVEP.equals(numeroVep)).findFirst().get());
	}

	public static Object validarPagoEnBandeja(ContextoOB contexto) {
		Objeto objetoNumerosVep = contexto.parametros.objeto("numerosVep");
		List<Object> numerosVep = objetoNumerosVep.toList();
		ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPagoOB = new ServicioEstadoPagoOB(contexto);
		EstadoPagoOB estadoEnBandeja = servicioEstadoPagoOB.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		List<Object> estadosVeps = new ArrayList<>();

		for (Object vep : numerosVep) {
			Objeto datos = new Objeto();
			datos.set("numeroVep", vep);

			if (!servicioPagoVeps.buscarPorEmpresaYFiltros(contexto.sesion().empresaOB, null, null, estadoEnBandeja, vep.toString()).get().isEmpty()) {
				datos.set("estado", "EXISTE_SOLICITUD_EN_BANDEJA");
			} else
				datos.set("estado", "DISPONIBLE");
			estadosVeps.add(datos);
		}

		return respuesta("datos", estadosVeps);

	}

}
