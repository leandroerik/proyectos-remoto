package ar.com.hipotecario.canal.homebanking.api;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.lib.Texto;
import ar.com.hipotecario.canal.homebanking.lib.Util;
import ar.com.hipotecario.canal.homebanking.negocio.*;
import ar.com.hipotecario.canal.homebanking.servicio.RestMora;
import ar.com.hipotecario.canal.homebanking.servicio.RestPostventa;
import ar.com.hipotecario.canal.homebanking.servicio.RestPrestamo;
import ar.com.hipotecario.canal.homebanking.servicio.RestVivienda;

public class HBGestiones {

	private static ExecutorService pool = Executors.newCachedThreadPool();

	public static Respuesta gestionesAsync(ContextoHB contexto) {
		if (!ConfigHB.esProduccion() && !ConfigHB.esOpenShift()) {
			return gestiones(contexto);
		}

		Respuesta respuesta = new Respuesta();
		try {
			Future<Respuesta> future = pool.submit(() -> gestiones(contexto));
			respuesta = future.get(ConfigHB.integer("gestiones", 20), TimeUnit.SECONDS);
		} catch (Exception e) {
		}
		return respuesta;
	}

	@SuppressWarnings("unchecked")
	public static Respuesta gestiones(ContextoHB contexto) {
		Futuro<Respuesta> responseSucursalAmarilloF = new Futuro<>(() -> HBSucursalVirtual.getSolicitudesCampanitaAmarillo(contexto)); //Sucursal Virtual
		Futuro<Respuesta> responseSucursalVerdeF = new Futuro<>(() -> HBSucursalVirtual.getSolicitudesCampanitaVerde(contexto)); //Sucursal Virtual
		Futuro<Respuesta> respuestaNotifINVF = new Futuro<>(() -> HBInversion.obtenerSolicitudes(contexto)); //// Solicitudes inversiones
		Futuro<Respuesta> otrasSolicitudesF = new Futuro<>(() -> HBGestiones.otrasSolicitudes(contexto)); //// crm - postventa?
		Futuro<Respuesta> respuestaNotifALF = new Futuro<>(() -> HBAumentoLimiteTC.notificacionesAumetoLimite(contexto)); //// Aumento Limite TC

		Integer cantidadGestionesCerradas = 0;
		Integer cantidadGestionesEnCurso = 0;

		try {
			HBProcesos.desistirSolicitudes(contexto);
		} catch (Exception e) {
		}

		Respuesta respuesta = new Respuesta();
		try {
			try{
				Respuesta otrasSolcitudes = otrasSolicitudesF.get(); //// Otras solicitudes - CRM?
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
                Respuesta responseSucursalAmarillo = responseSucursalAmarilloF.tryGet(Respuesta.error()); //Sucursal Virtual - AA
                if (!responseSucursalAmarillo.hayError()) {
                    for (Objeto notificacionEnCurso : responseSucursalAmarillo.objetos("notificaciones")) {
                        respuesta.add("notificaciones", notificacionEnCurso);
                    }
                    for (Objeto gestionEnCurso : responseSucursalAmarillo.objetos("gestionesEnCurso")) {
                        respuesta.add("gestionesEnCurso", gestionEnCurso);
                        cantidadGestionesEnCurso++;
                    }
                    for (Objeto gestionCerrada : responseSucursalAmarillo.objetos("gestionesCerradas")) {
                        respuesta.add("gestionesCerradas", gestionCerrada);
                        cantidadGestionesCerradas++;
                    }
                }
            } catch (Exception e) {
            }

			try {
				Respuesta responseSucursalVerde = responseSucursalVerdeF.tryGet(Respuesta.error()); //Sucursal Virtual
				if (!responseSucursalVerde.hayError()) {
					for (Objeto notificacionEnCurso : responseSucursalVerde.objetos("notificaciones")) {
						respuesta.add("notificaciones", notificacionEnCurso);
					}
					for (Objeto gestionEnCurso : responseSucursalVerde.objetos("gestionesEnCurso")) {
						respuesta.add("gestionesEnCurso", gestionEnCurso);
						cantidadGestionesEnCurso++;
					}
					for (Objeto gestionCerrada : responseSucursalVerde.objetos("gestionesCerradas")) {
						respuesta.add("gestionesCerradas", gestionCerrada);
						cantidadGestionesCerradas++;
					}
				}
			} catch (Exception e) {
			}

			if (respuesta.get("gestionesEnCurso") == null) {
				respuesta.add("gestionesEnCurso", new HashMap<String, Object>());
			}

			if (respuesta.get("gestionesCerradas") == null) {
				respuesta.add("gestionesCerradas", new HashMap<String, Object>());
			}

			respuesta.set("cantidadGestionesEnCurso", cantidadGestionesEnCurso);
			respuesta.set("cantidadGestionesCerradas", cantidadGestionesCerradas);

			try {
				// AUMENTO LIMITE TC
				Respuesta respuestaNotifAL = respuestaNotifALF.get(); //// Aumento Limite TC
				if (!respuestaNotifAL.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}
			try {
				// SOLICITUDES INVERSIONES
				Respuesta respuestaNotifINV = respuestaNotifINVF.get(); //// Solicitudes inversiones
				if (!respuestaNotifINV.hayError()) {
					for (Objeto notif : (ArrayList<Objeto>) respuestaNotifINV.get("notificacionesAL")) {
						respuesta.add("notificaciones", notif);
					}
				}
			} catch (Exception e) {
			}

			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	private static Respuesta otrasSolicitudes(ContextoHB contexto) {
		ApiResponse response = RestPostventa.obtenerCasosGestion(contexto);
		Objeto datos = (Objeto) response.get("Datos");

		Respuesta respuesta = new Respuesta();

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
			SqlResponse sqlResponse = getDetalleTipi(Texto.textToHtml(caso.string("Titulo")));

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

				gestion.put("detalle", detalle.toMap());
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

	public static SqlResponse getDetalleTipi(String tipificacion) {
		try {
			SqlResponse response = null;
			SqlRequest sqlRequest = Sql.request("SelectProductoTipificacion", "homebanking");
			sqlRequest.sql = "select top 1 * from [Homebanking].[dbo].[parametria_tipificaciones] where tipificacion = ?";
			sqlRequest.parametros.add(tipificacion);
			response = Sql.response(sqlRequest);
			if (response.registros.size() > 0) {
				return response;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public static Respuesta solicitarLibreDeuda(ContextoHB ctx) {
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

		for (Objeto item :  RestMora.getMoraCasos(ctx).objetos()) {

			if(item.bigDecimal("totalSaldoAjustado").compareTo(new BigDecimal("0.0")) == 0){
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
				if(tieneMora){
					tieneProductoNoVigenteMora = true;
				}
			}

			if(tieneMora){
				Objeto obj = new Objeto();
				obj.set("descripcion", "Caja de Seguridad");
				obj.set("numeroEnmascarado", ca.numeroEnmascarado());
				moras.add(obj);
			}
		}

		if(!moras.isEmpty()){
			return Respuesta.estado("ERROR_MORA").set("moras", moras);
		}

		ApiResponse res = RestPostventa.solicitarLibreDeuda(ctx, ventaCartera, tieneProductosVigentes, tieneProductoVigenteMora, tieneProductosNoVigentes, tieneProductoNoVigenteMora);
		if (res == null || res.hayError()) {
			return Respuesta.error();
		}

		String numeroCaso = Util.getNumeroCaso(res);
		if (numeroCaso.isEmpty()) {
			return Respuesta.error();
		}

		RestPostventa.eliminarCacheGestiones(ctx);
		return Respuesta.exito("numeroCaso", numeroCaso);
    }

	public static Respuesta solicitarLiberacionHipoteca(ContextoHB ctx) {
		Boolean cancelacionTotal = ctx.parametros.bool("cancelacionTotal");
		String nroPrestamo = ctx.parametros.string("nroPrestamo");

		String garantia = ctx.parametros.string("garantia");
		String codCiudadGarantia = ctx.parametros.string("codCiudadGarantia");
		String ciudadGarantia = ctx.parametros.string("ciudadGarantia");
		String nroPrestamoMig = ctx.parametros.string("nroPrestamoMig");

		if(cancelacionTotal)
		{
			String cuit = ctx.persona().cuit();
			try {
				if (!ConfigHB.esProduccion() && !ctx.requestHeader("cuit").isEmpty())
					cuit = ctx.requestHeader("cuit");
			} catch (Exception e) {
			}

			if (Objeto.anyEmpty(cuit))
				return Respuesta.parametrosIncorrectos();

			ApiResponse responseHipotecas = RestPrestamo.hipotecas(ctx, cuit);
			if (responseHipotecas == null || (responseHipotecas != null && responseHipotecas.hayError()))
				return Respuesta.estado("ERROR_API");

			// Obtener la lista de hipotecas desde la respuesta
			List<Object> objHipotecas = responseHipotecas.lista;
			if (objHipotecas != null && !objHipotecas.isEmpty()) {
				Objeto prestamoFiltrado = objHipotecas.stream()
						.filter(o -> o instanceof Objeto).map(o -> (Objeto) o).filter(p -> {
							Object nro = p.get("nroPrestamo");
							return nro != null && nroPrestamo.equals(String.valueOf(nro));
						}).findFirst().orElse(null);

				if (prestamoFiltrado != null){
					garantia = String.valueOf(prestamoFiltrado.get("garantia"));
					codCiudadGarantia = String.valueOf(prestamoFiltrado.get("codigoCiudad"));
					ciudadGarantia = String.valueOf(prestamoFiltrado.get("ciudad"));
					nroPrestamoMig = String.valueOf(prestamoFiltrado.get("nroPrestamoMigrado"));
				}
				else
					return Respuesta.estado("ERROR_API");
			}
			else
				return Respuesta.estado("ERROR_API");
		}

		ApiResponse res = RestVivienda.liberacionHipotecaAutomatico(ctx, nroPrestamo, garantia, codCiudadGarantia, ciudadGarantia, nroPrestamoMig);
		if (res == null || (res != null && res.hayError() ))
			return Respuesta.estado("ERROR_API");

		String numeroCaso = "";
		try {
			numeroCaso = (String) res.get("numeracionCRM");
			//Objeto reclamo = (Objeto) res.get("Datos");
			//numeroCaso = reclamo.objetos().get(0).string("NumeracionCRM");
		} catch (Exception e) {
			return Respuesta.error();
		}

		if (numeroCaso.isEmpty())
			return Respuesta.error();

		RestPostventa.eliminarCacheGestiones(ctx);
		return Respuesta.exito("numeroCaso", numeroCaso);
	}

	public static Object gestionesSucursalVirtual(ContextoHB ctx) {
		Respuesta respuesta = new Respuesta();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<Respuesta> responseSucursalAmarilloF = new Futuro<>(() -> HBSucursalVirtual.getSolicitudesCampanitaAmarillo(ctx)); //Sucursal Virtual
		Futuro<Respuesta> responseSucursalVerdeF = new Futuro<>(() -> HBSucursalVirtual.getSolicitudesCampanitaVerde(ctx)); //Sucursal Virtual

		try {
			Respuesta responseSucursalAmarillo = responseSucursalAmarilloF.tryGet(Respuesta.error()); //Sucursal Virtual - AA
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
			Respuesta responseSucursalVerde = responseSucursalVerdeF.tryGet(Respuesta.error()); //Sucursal Virtual - AV
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

	public static Object gestionesInversion(ContextoHB ctx) {
		Respuesta respuesta = new Respuesta();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<Respuesta> respuestaNotifINVF = new Futuro<>(() -> HBInversion.obtenerSolicitudes(ctx)); //// Solicitudes inversiones

		try {
			Respuesta respuestaNotifINV = respuestaNotifINVF.get(); //// Solicitudes inversiones
			if (!respuestaNotifINV.hayError()) {
				for (Objeto notif : (ArrayList<Objeto>) respuestaNotifINV.get("notificacionesAL")) {
					respuesta.add("notificaciones", notif);
				}
			}
		} catch (Exception e) {}

		return respuesta;
	}

	public static Object gestionesPostventa(ContextoHB ctx) {
		Respuesta respuesta = new Respuesta();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<Respuesta> otrasSolicitudesF = new Futuro<>(() -> HBGestiones.otrasSolicitudes(ctx)); //// crm - postventa?

		try{
			Respuesta otrasSolcitudes = otrasSolicitudesF.get(); //// Otras solicitudes - CRM?
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

	public static Object gestionesAumentoLimite(ContextoHB ctx) {
		Respuesta respuesta = new Respuesta();
		respuesta.set("notificaciones", new ArrayList<>());
		respuesta.set("gestionesEnCurso", new ArrayList<>());
		respuesta.set("gestionesCerradas", new ArrayList<>());

		Futuro<Respuesta> respuestaNotifALF = new Futuro<>(() -> HBAumentoLimiteTC.notificacionesAumetoLimite(ctx)); //// Aumento Limite TC

		try {
			Respuesta respuestaNotifAL = respuestaNotifALF.get(); //// Aumento Limite TC
			if (!respuestaNotifAL.hayError()) {
				for (Objeto notif : (ArrayList<Objeto>) respuestaNotifAL.get("notificacionesAL")) {
					respuesta.add("notificaciones", notif);
				}
			}
		} catch (Exception e) {}

		return respuesta;
	}
}
