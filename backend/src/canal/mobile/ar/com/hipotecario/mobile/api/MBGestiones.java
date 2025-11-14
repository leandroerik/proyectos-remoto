package ar.com.hipotecario.mobile.api;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Texto;
import ar.com.hipotecario.mobile.lib.Util;
import ar.com.hipotecario.mobile.negocio.*;
import ar.com.hipotecario.mobile.servicio.RestMora;
import ar.com.hipotecario.mobile.servicio.RestPostventa;

public class MBGestiones {

	public static RespuestaMB gestionesV2(ContextoMB contexto) {
		if(MBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_gestiones_v2")){
			return getGestionesV2(contexto);
		}

		return gestiones(contexto);
	}


	@SuppressWarnings("unchecked")
	public static RespuestaMB gestiones(ContextoMB contexto) {
		Futuro<RespuestaMB> responseSucursalAmarilloF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaAmarillo(contexto)); //Sucursal Virtual
		Futuro<RespuestaMB> responseSucursalVerdeF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaVerde(contexto)); //Sucursal Virtual
		Futuro<RespuestaMB> respuestaNotifINVF = new Futuro<>(() -> MBInversion.obtenerSolicitudes(contexto)); //// Solicitudes inversiones
		Futuro<RespuestaMB> otrasSolicitudesF = new Futuro<>(() -> MBGestiones.otrasSolicitudes(contexto)); //// crm - postventa?
		Futuro<RespuestaMB> respuestaNotifALF = new Futuro<>(() -> MBAumentoLimiteTC.notificacionesAumetoLimite(contexto)); //// Aumento Limite TC


		RespuestaMB respuesta = new RespuestaMB();

		Integer cantidadGestionesCerradas = 0;
		Integer cantidadGestionesEnCurso = 0;
		try {
			MBProcesos.desistirSolicitudes(contexto);
		} catch (Exception e) {
		}

		try{
			RespuestaMB otrasSolcitudes = otrasSolicitudesF.get(); //// Otras solicitudes - las primeras.
			if (!otrasSolcitudes.hayError()) {
				for (Object notificacionEnCurso : otrasSolcitudes.objeto("notificaciones").toList()) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Object gestionEnCurso : otrasSolcitudes.objeto("gestionesEnCurso").toList()) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Object gestionCerrada : otrasSolcitudes.objeto("gestionesCerradas").toList()) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		}catch (Exception e){
		}

		try {
			RespuestaMB responseSucursal = responseSucursalVerdeF.tryGet(); //Sucursal Virtual
			if (!responseSucursal.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursal.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursal.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Objeto gestionCerrada : responseSucursal.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		} catch (Exception e) {
		}

		try {
			RespuestaMB responseSucursal = responseSucursalAmarilloF.tryGet(); //Sucursal Virtual
			if (!responseSucursal.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursal.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursal.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Objeto gestionCerrada : responseSucursal.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		} catch (Exception e) {
		}

		try {

			if (respuesta.get("gestionesEnCurso") == null) {
//				respuesta.add("gestionesEnCurso", new HashMap<String, Object>());
				respuesta.setNull("gestionesEnCurso");
			}

			if (respuesta.get("gestionesCerradas") == null) {
//				respuesta.add("gestionesCerradas", new HashMap<String, Object>());
				respuesta.setNull("gestionesCerradas");
			}

			respuesta.set("cantidadGestionesEnCurso", cantidadGestionesEnCurso);
			respuesta.set("cantidadGestionesCerradas", cantidadGestionesCerradas);

			try {
				// AUMENTO LIMITE TC
				RespuestaMB respuestaNotifAL = respuestaNotifALF.get(); //// Aumento Limite TC
				if (!respuestaNotifAL.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}
			try {
				// SOLICITUDES INVERSIONES
				RespuestaMB respuestaNotifINV = respuestaNotifINVF.get(); //// Solicitudes inversiones
				if (!respuestaNotifINV.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifINV.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}
			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	private static RespuestaMB otrasSolicitudes(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();

		ApiResponseMB response = RestPostventa.obtenerCasosGestion(contexto);
		Objeto datos = (Objeto) response.get("Datos");
		for (Objeto dato : datos.objetos()) {
			try {
				// Desc dato.set("orden", Long.MAX_VALUE - dato.date("FechaUltimaModificacion",
				// "yyyy-MM-dd").getTime());
				dato.set("orden", dato.date("FechaUltimaModificacion", "yyyy-MM-dd").getTime());
			} catch (Exception e) {
			}
		}
		datos.ordenar("orden", "NumeroCaso");
		for (Objeto caso : datos.objetos()) {
			Map<String, Object> gestion = new HashMap<String, Object>();
			Map<String, Object> notificacion = new HashMap<String, Object>();
			SqlResponseMB sqlResponse = getDetalleTipi(Texto.textToHtml(caso.string("Titulo")));

			if (sqlResponse == null || sqlResponse.hayError) {
				continue;
			}

			if (sqlResponse.registros.get(0).string("producto_final").equalsIgnoreCase("NO_MOSTRAR")) {
				continue;
			}

			String tipiFinal = Texto.htmlToText(sqlResponse.registros.get(0).string("tipificacion_final"));
			String productoFinal = Texto.htmlToText(sqlResponse.registros.get(0).string("producto_final"));

			gestion.put("producto", productoFinal);
			gestion.put("motivo", tipiFinal);
			String fechaUltimaModificacion = "";
			try {
				if (caso.get("FechaUltimaModificacion") != null) {
					DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
					OffsetDateTime dateTime = OffsetDateTime.parse((String) caso.get("FechaUltimaModificacion"));
					fechaUltimaModificacion = dateTime.format(formatter);
				}
			} catch (Exception e) {
				fechaUltimaModificacion = "-";
			}
			gestion.put("fecha", fechaUltimaModificacion);
			String numeroCaso = caso.get("NumeroCaso") == null ? "" : (String) caso.get("NumeroCaso");
			gestion.put("numeroGestion", numeroCaso);
			notificacion.put("type", "CRM");
			notificacion.put("numeroCaso", numeroCaso);
			notificacion.put("dateDesc", fechaUltimaModificacion);
			notificacion.put("read", caso.get("NotificacionLeida"));

			if ("Resuelto".equalsIgnoreCase(caso.string("Estado"))) {
				String notaResolucion = caso.get("NotasResolucion") == null ? "" : (String) caso.get("NotasResolucion");
				String estadoGestion = caso.string("Resolucion").equalsIgnoreCase("NoFavorable") ? "NO FAVORABLE" : "FAVORABLE";
				gestion.put("estadoGestion", estadoGestion);
				gestion.put("notasResolucion", notaResolucion);

				Objeto detalle = new Objeto();
				detalle.set("descripcion", "<p><font color= #FC7B41>" + caso.string("Nombres", contexto.persona().nombre()) + ",</font></p> Ya nos ocupamos de tu solicitud número: <font color= #FC7B41>" + numeroCaso + "</font> Te informamos que la misma se encuentra finalizada habiendo llegado a la siguiente resolución: <br><font color= #FC7B41>" + notaResolucion + "</font><br> En caso de haber recibido un archivo adjunto vas a poder descargarlo desde tu mail personal.");
				detalle.set("numeroCaso", numeroCaso);
				detalle.set("title", tipiFinal);
				detalle.set("dateDesc", fechaUltimaModificacion);
				detalle.set("nombre", caso.string("Nombres", contexto.persona().nombre()));
				detalle.set("notasResolucion", notaResolucion);
				detalle.set("parte1", "Ya nos ocupamos de tu solicitud número:");
				detalle.set("parte2", "Te informamos que la misma se encuentra finalizada habiendo llegado a la siguiente resolución:");
				detalle.set("parte3", "En caso de haber recibido un archivo adjunto vas a poder descargarlo desde tu mail personal.");

				try {
					if (caso.get("Adjuntos") == null || "{}".equals(caso.get("Adjuntos"))) {
						gestion.put("attach", null);
					} else {
						Objeto adj = (Objeto) caso.get("Adjuntos");
						List<Objeto> attach = new ArrayList<Objeto>();
						for (Entry<String, Object> entry : adj.toMap().entrySet()) {
							Objeto adjunto = new Objeto();
							adjunto.set("name", entry.getKey()).set("fileId", entry.getValue());
							attach.add(adjunto);
						}
						gestion.put("attach", attach);
					}
				} catch (Exception e) {
					gestion.put("attach", null);
				}

				gestion.put("detalle", detalle);
				respuesta.add("gestionesCerradas", gestion);
				notificacion.put("estado", "CERRADA");
				notificacion.put("title", "Tu gestión \"" + tipiFinal + "\" fue cerrada de forma " + estadoGestion.toLowerCase());
				notificacion.put("parte1", "Revisá el detalle de tu gestión");
				respuesta.add("notificaciones", notificacion);
//				cantidadGestionesCerradas++;
			} else {
				respuesta.add("gestionesEnCurso", gestion);
				notificacion.put("estado", "ACTIVA");
				notificacion.put("title", "Tu gestión \"" + tipiFinal + "\" está activa");
				notificacion.put("parte1", "Estamos analizando tu caso");
				respuesta.add("notificaciones", notificacion);
//				cantidadGestionesEnCurso++;
			}
		}
		return respuesta;
	}

	public static String getProducto(String tipificacion) {
		try {
			SqlResponseMB response = null;
			SqlRequestMB sqlRequest = SqlMB.request("SelectProductoTipificacion", "homebanking");
			sqlRequest.sql = "select top 1 producto from [Homebanking].[dbo].[parametria_tipificaciones] where tipificacion = ?";
			sqlRequest.parametros.add(tipificacion);
			response = SqlMB.response(sqlRequest);
			if (response.registros.size() > 0) {
				return response.registros.get(0).string("producto");
			}
		} catch (Exception e) {
		}
		return " ";
	}

	public static SqlResponseMB getDetalleTipi(String tipificacion) {
		try {
			SqlResponseMB response = null;
			SqlRequestMB sqlRequest = SqlMB.request("SelectProductoTipificacion", "homebanking");
			sqlRequest.sql = "select top 1 * from [Homebanking].[dbo].[parametria_tipificaciones] where tipificacion = ?";
			sqlRequest.parametros.add(tipificacion);
			response = SqlMB.response(sqlRequest);
			if (response.registros.size() > 0) {
				return response;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static RespuestaMB solicitarLibreDeuda(ContextoMB ctx) {
		List<String> codigosMora = new ArrayList<>();
		boolean ventaCartera = false;

		for (Objeto item : RestMora.getMoraCliente(ctx).objetos()) {
			if(item.integer("diasmora") == 0){
				continue;
			}

			String productoStr = item.string("producto").trim();
			String[] productoArr = productoStr.split("-");
			String nroProducto = productoArr[productoArr.length - 1];
			if(!codigosMora.contains(nroProducto)){
				codigosMora.add(nroProducto);
			}

			if(productoArr[0].equals("TC")){
				String nroTc = nroProducto.substring(1);
				if(!codigosMora.contains(nroTc)){
					codigosMora.add(nroTc);
				}
			}
			if(item.string("gestionng").toLowerCase().contains("fideicomiso")){
				ventaCartera = true;
			}
		}

		for (Objeto item : RestMora.getMoraCasos(ctx).objetos()) {
			if(item.integer("totalSaldoAjustado") == 0){
				continue;
			}

			String productoStr = item.string("producto").trim();
			String[] productoArr = productoStr.split(" ");
			String nroProducto = productoArr[productoArr.length - 1];
			if(!codigosMora.contains(nroProducto)){
				codigosMora.add(nroProducto);
			}

			if(productoArr[0].equals("TC")){
				String nroTc = nroProducto.substring(1);
				if(!codigosMora.contains(nroTc)){
					codigosMora.add(nroTc);
				}
			}
		}

		boolean tieneProductosVigentes = false;
		boolean tieneProductoVigenteMora = false;
		boolean tieneProductosNoVigentes = false;
		boolean tieneProductoNoVigenteMora = false;
		List<Objeto> moras = new ArrayList<>();
		for(TarjetaCredito tc: ctx.tarjetasCredito()){
			boolean tieneMora = codigosMora.contains(tc.cuenta());
			if("20".equals(tc.idEstado())){
				tieneProductosVigentes = true;
				if(tieneMora){
					tieneProductoVigenteMora = true;
				}
			}
			else{
				tieneProductosNoVigentes = true;
				if(tieneMora){
					tieneProductoNoVigenteMora = true;
				}
			}

			if(tieneMora || "25".equals(tc.idEstado())){
				Objeto obj = new Objeto();
				obj.set("descripcion", "Tarjeta Crédito");
				obj.set("numeroEnmascarado", tc.numeroEnmascarado());
				moras.add(obj);
			}
		}

		for (Cuenta ca : ctx.cuentas()){
			if("CTE".equals(ca.idTipo())){
				boolean tieneMora = codigosMora.contains(ca.numero());
				if("A".equals(ca.idEstado())){
					tieneProductosVigentes = true;
					if(tieneMora){
						tieneProductoVigenteMora = true;
					}
				}
				else {
					tieneProductosNoVigentes = true;
					if(tieneMora){
						tieneProductoNoVigenteMora = true;
					}
				}

				if(tieneMora){
					Objeto obj = new Objeto();
					obj.set("descripcion", "Cuenta Corriente");
					obj.set("numeroEnmascarado", ca.numeroEnmascarado());
					moras.add(obj);
				}
			}
		}

		for(Prestamo p : ctx.prestamos()){
			if("HIPOTECARIO".equals(p.categoria()) || "PERSONAL".equals(p.categoria())){
				boolean tieneMora = codigosMora.contains(p.numero());
				if("V".equals(p.idEstado())){
					tieneProductosVigentes = true;
					if(tieneMora){
						tieneProductoVigenteMora = true;
					}
				}
				else {
					tieneProductosNoVigentes = true;
					if(tieneMora){
						tieneProductoNoVigenteMora = true;
					}
				}

				if(tieneMora){
					Objeto obj = new Objeto();
					obj.set("descripcion", "HIPOTECARIO".equals(p.categoria()) ? "Prestamo Hipotecario" : "Prestamo Personal");
					obj.set("numeroEnmascarado", p.numeroEnmascarado());
					moras.add(obj);
				}
			}
		}

		for (CajaSeguridad ca : ctx.cajasSeguridad()){
			boolean tieneMora = codigosMora.contains(ca.numero());
			if("V".equals(ca.idEstado())){
				tieneProductosVigentes = true;
				if(tieneMora){
					tieneProductoVigenteMora = true;
				}
			}
			else {
				tieneProductosNoVigentes = true;
				tieneProductoNoVigenteMora = tieneMora;
			}

			if(tieneMora){
				Objeto obj = new Objeto();
				obj.set("descripcion", "Caja de Seguridad");
				obj.set("numeroEnmascarado", ca.numeroEnmascarado());
				moras.add(obj);
			}
		}

		if(!moras.isEmpty()){
			return RespuestaMB.estado("ERROR_MORA").set("moras", moras);
		}

		ApiResponseMB res = RestPostventa.solicitarLibreDeuda(ctx, ventaCartera, tieneProductosVigentes, tieneProductoVigenteMora, tieneProductosNoVigentes, tieneProductoNoVigenteMora);
		if (res == null || res.hayError()) {
			return RespuestaMB.error();
		}

		String numeroCaso = Util.getNumeroCaso(res);
		if (numeroCaso.isEmpty()) {
			return RespuestaMB.error();
		}

		RestPostventa.eliminarCacheGestiones(ctx);
		return RespuestaMB.exito("numeroCaso", numeroCaso);
	}

	public static RespuestaMB getGestionesV2(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		Integer cantidadGestionesCerradas = 0;
		Integer cantidadGestionesEnCurso = 0;

		Futuro<RespuestaMB> responseSucursalAmarilloF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaAmarillo(contexto)); //Sucursal Virtual
		Futuro<RespuestaMB> responseSucursalVerdeF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaVerde(contexto)); //Sucursal Virtual

		try {
			RespuestaMB responseSucursal = responseSucursalAmarilloF.tryGet(); //Sucursal Virtual
			if (!responseSucursal.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursal.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursal.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Objeto gestionCerrada : responseSucursal.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		}catch (Exception e){
		}

		try {
			RespuestaMB responseSucursal = responseSucursalVerdeF.tryGet(); //Sucursal Virtual
			if (!responseSucursal.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursal.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursal.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Objeto gestionCerrada : responseSucursal.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		} catch (Exception e) {
		}

		if(cantidadGestionesEnCurso > 0){
			if (respuesta.get("gestionesEnCurso") == null) {
				respuesta.setNull("gestionesEnCurso");
			}

			if (respuesta.get("gestionesCerradas") == null) {
				respuesta.setNull("gestionesCerradas");
			}

			respuesta.set("cantidadGestionesEnCurso", cantidadGestionesEnCurso);
			respuesta.set("cantidadGestionesCerradas", cantidadGestionesCerradas);
			return respuesta;
		}

		Futuro<RespuestaMB> respuestaNotifINVF = new Futuro<>(() -> MBInversion.obtenerSolicitudes(contexto)); //// Solicitudes inversiones
		Futuro<RespuestaMB> otrasSolicitudesF = new Futuro<>(() -> MBGestiones.otrasSolicitudes(contexto)); //// crm - postventa?
		Futuro<RespuestaMB> respuestaNotifALF = new Futuro<>(() -> MBAumentoLimiteTC.notificacionesAumetoLimite(contexto)); //// Aumento Limite TC

		try {
			MBProcesos.desistirSolicitudes(contexto);
		} catch (Exception e) {
		}

		try{
			RespuestaMB otrasSolcitudes = otrasSolicitudesF.get(); //// Otras solicitudes - las primeras.
			if (!otrasSolcitudes.hayError()) {
				for (Object notificacionEnCurso : otrasSolcitudes.objeto("notificaciones").toList()) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Object gestionEnCurso : otrasSolcitudes.objeto("gestionesEnCurso").toList()) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
					cantidadGestionesEnCurso++;
				}
				for (Object gestionCerrada : otrasSolcitudes.objeto("gestionesCerradas").toList()) {
					respuesta.add("gestionesCerradas", gestionCerrada);
					cantidadGestionesCerradas++;
				}
			}
		} catch (Exception e) {
		}

		try {

			if (respuesta.get("gestionesEnCurso") == null) {
//				respuesta.add("gestionesEnCurso", new HashMap<String, Object>());
				respuesta.setNull("gestionesEnCurso");
			}

			if (respuesta.get("gestionesCerradas") == null) {
//				respuesta.add("gestionesCerradas", new HashMap<String, Object>());
				respuesta.setNull("gestionesCerradas");
			}

			respuesta.set("cantidadGestionesEnCurso", cantidadGestionesEnCurso);
			respuesta.set("cantidadGestionesCerradas", cantidadGestionesCerradas);

			try {
				// AUMENTO LIMITE TC
				RespuestaMB respuestaNotifAL = respuestaNotifALF.get(); //// Aumento Limite TC
				if (!respuestaNotifAL.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}
			try {
				// SOLICITUDES INVERSIONES
				RespuestaMB respuestaNotifINV = respuestaNotifINVF.get(); //// Solicitudes inversiones
				if (!respuestaNotifINV.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifINV.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}
			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static Object gestionesSucursalVirtual(ContextoMB ctx) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<RespuestaMB> responseSucursalAmarilloF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaAmarillo(ctx)); //Sucursal Virtual
		Futuro<RespuestaMB> responseSucursalVerdeF = new Futuro<>(() -> MBSucursalVirtual.getSolicitudesCampanitaVerde(ctx)); //Sucursal Virtual

		try {
			RespuestaMB responseSucursalAmarillo = responseSucursalAmarilloF.tryGet(RespuestaMB.error()); //Sucursal Virtual - AA
			if (!responseSucursalAmarillo.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursalAmarillo.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursalAmarillo.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
				}
				for (Objeto gestionCerrada : responseSucursalAmarillo.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
				}
			}
		} catch (Exception e) {}

		try {
			RespuestaMB responseSucursalVerde = responseSucursalVerdeF.tryGet(RespuestaMB.error()); //Sucursal Virtual - AV
			if (!responseSucursalVerde.hayError()) {
				for (Objeto notificacionEnCurso : responseSucursalVerde.objetos("notificaciones")) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Objeto gestionEnCurso : responseSucursalVerde.objetos("gestionesEnCurso")) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
				}
				for (Objeto gestionCerrada : responseSucursalVerde.objetos("gestionesCerradas")) {
					respuesta.add("gestionesCerradas", gestionCerrada);
				}
			}
		} catch (Exception e) {}

		return respuesta;
	}

	public static Object gestionesInversion(ContextoMB ctx) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<RespuestaMB> respuestaNotifINVF = new Futuro<>(() -> MBInversion.obtenerSolicitudes(ctx)); //// Solicitudes inversiones

		try {
			RespuestaMB respuestaNotifINV = respuestaNotifINVF.get(); //// Solicitudes inversiones
			if (!respuestaNotifINV.hayError()) {
				for (Objeto notif : (ArrayList<Objeto>) respuestaNotifINV.get("notificacionesAL")) {
					respuesta.add("notificaciones", notif);
				}
			}
		} catch (Exception e) {}

		return respuesta;
	}

	public static Object gestionesPostventa(ContextoMB ctx) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<RespuestaMB> otrasSolicitudesF = new Futuro<>(() -> MBGestiones.otrasSolicitudes(ctx)); //// crm - postventa?

		try{
			RespuestaMB otrasSolcitudes = otrasSolicitudesF.get(); //// Otras solicitudes - CRM?
			if (!otrasSolcitudes.hayError()) {
				for (Object notificacionEnCurso : otrasSolcitudes.objeto("notificaciones").toList()) {
					respuesta.add("notificaciones", notificacionEnCurso);
				}
				for (Object gestionEnCurso : otrasSolcitudes.objeto("gestionesEnCurso").toList()) {
					respuesta.add("gestionesEnCurso", gestionEnCurso);
				}
				for (Object gestionCerrada : otrasSolcitudes.objeto("gestionesCerradas").toList()) {
					respuesta.add("gestionesCerradas", gestionCerrada);
				}
			}
		}catch (Exception e){}

		return respuesta;
	}

	public static Object gestionesAumentoLimite(ContextoMB ctx) {
		RespuestaMB respuesta = new RespuestaMB();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		try {
			MBProcesos.desistirSolicitudes(ctx);
		} catch (Exception e) {}

		Futuro<RespuestaMB> respuestaNotifALF = new Futuro<>(() -> MBAumentoLimiteTC.notificacionesAumetoLimite(ctx)); //// Aumento Limite TC

		try {
			RespuestaMB respuestaNotifAL = respuestaNotifALF.get(); //// Aumento Limite TC
			if (!respuestaNotifAL.hayError()) {
				for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
					respuesta.add("notificaciones", notif);
				}
			}
		} catch (Exception e) {}

		return respuesta;
	}

}
