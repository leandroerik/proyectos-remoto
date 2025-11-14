package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.servicio.api.link.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ReferenciaPagoServicioOB;
import org.apache.commons.lang3.RandomStringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.empresas.SubConveniosOB;
import ar.com.hipotecario.backend.servicio.api.link.LinkAdhesiones.RespuestaOk;
import ar.com.hipotecario.backend.servicio.api.link.LinkAdhesiones.ServicioAdhesion;
import ar.com.hipotecario.backend.servicio.api.link.PagosOB.PagoOB;
import ar.com.hipotecario.backend.servicio.api.link.Pagos.Vencimiento;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoPagosDeServicioYVepsOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios.ConceptoDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios.EnteDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios.PagoUnicoDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios.RubroDTO;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pagoDeServicios.VencimientoDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.EstadoPagoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoDeServicios.PagoDeServiciosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagosVep.PagosVepOB;

public class OBPagos extends ModuloOB {
	public static Object rubros(ContextoOB contexto) {

		SesionOB sesion = contexto.sesion();
		
		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		if (empty(sesion.empresaOB)) {
			return respuesta("EMPRESA_INVALIDA");
		}

		Rubros rubros = ApiLink.rubros(contexto, tarjetaVirtual).tryGet();
		if (empty(rubros) || empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		Objeto lstRubros = new Objeto();
		rubros.forEach(rubro -> lstRubros.add(new Objeto().set("codigo", rubro.codigo).set("descripcion", rubro.descripcion)));

		Objeto datos = new Objeto();
		datos.set("rubros", lstRubros.objetos());

		return respuesta("datos", datos);
	}

	public static Object entes(ContextoOB contexto) {
		String codigoRubro = contexto.parametros.string("rubro");

		SesionOB sesion = contexto.sesion();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}
		String tarjetaVirtual = tarjetasVirtuales.get(0).nroTarjeta;

		Entes entes = ApiLink.entes(contexto, tarjetaVirtual, codigoRubro).tryGet();
		if (empty(entes) || entes.size() < 1) {
			return respuesta("RUBRO_INVALIDO");
		}

		Objeto lstEntes = new Objeto();
		entes.forEach(e -> lstEntes.add(new Objeto().set("codigo", e.codigo).set("descripcion", e.descripcion).set("isBaseDeuda", e.isBaseDeuda)));

		Objeto datos = new Objeto();
		datos.set("idRubro", entes.get(0).rubro.codigo);
		datos.set("descripcionRubro", entes.get(0).rubro.descripcion);
		datos.set("entes", lstEntes.objetos());

		return respuesta("datos", datos);
	}

	public static Object ente(ContextoOB contexto) {
		String codigoRubro = contexto.parametros.string("rubro");
		String codigoEnte = contexto.parametros.string("ente");

		Objeto ente = validarEnteYRubro(contexto, codigoEnte);
		if (ente.get("estado") != null) {
			if (ente.get("estado").equals("RUBRO_INVALIDO"))
				return respuesta("RUBRO_INVALIDO");
			if (ente.get("estado").equals("ENTE_INVALIDO"))
				return respuesta("ENTE_INVALIDO");
		}

		Objeto datos = new Objeto();
		datos.set("rubro", codigoRubro);
		datos.set("ente", ente);

		return respuesta("datos", datos);
	}

	public static String ente(ContextoOB contexto, String codigoEnte) {
		Objeto ente = validarEnteYRubro(contexto, codigoEnte);
		if (ente.get("estado") != null) {
			return "Sin información";
		}

		return ente.get("descripcion").toString();
	}

	public static Object cargarPago(ContextoOB contexto) {
		String ente = contexto.parametros.string("ente");
		String rubro = contexto.parametros.string("rubro");
		String numeroCuenta = contexto.parametros.string("numeroCuenta");
		BigDecimal importe = contexto.parametros.bigDecimal("importe");
		String codigoLink = contexto.parametros.string("codigoLink");
		Boolean adherirServicio = contexto.parametros.bool("adherirServicio", false);
		String identificadorPago = contexto.parametros.string("identificadorPago");
		String conceptoIngresado = contexto.parametros.string("concepto");
		String descripcion = contexto.parametros.string("descripcion", null);
		String idDeuda = contexto.parametros.string("idDeuda", null);

		SesionOB sesion = contexto.sesion();
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
		ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
		ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
		ServicioHistorialPagoDeServiciosOB servicioHistorialPagoDeServicios = new ServicioHistorialPagoDeServiciosOB(contexto);
		EstadoPagoOB estadoEnBandeja = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		Objeto datos = new Objeto();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		if (importe.signum() != 1) {
			return respuesta("MONTO_INVALIDO");
		}

		Objeto ent = (Objeto) OBPagos.ente(contexto);
		if (!ent.get("estado").equals("0")) {
			if (ent.get("estado").equals("RUBRO_INVALIDO"))
				return respuesta("RUBRO_INVALIDO");
			if (ent.get("estado").equals("ENTE_INVALIDO"))
				return respuesta("ENTE_INVALIDO");
		}

		Objeto cuenta = OBCuentas.cuenta(contexto, numeroCuenta);
		if (empty(cuenta) || cuenta == null) {
			return respuesta("CUENTA_DEBITO_INVALIDA");
		}

		try {
			boolean estabaAdherido = true;

			Objeto tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, tarjetasVirtuales, ente);
			
			if (empty(tvAdh.get("datos.adhesion"))) {
				crearAdhesion(contexto);
				tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, tarjetasVirtuales, ente);
				estabaAdherido = false;
			}

			LinkAdhesiones.ServicioAdhesion adhesion;

			try {
				adhesion = (ServicioAdhesion) tvAdh.get("datos.adhesion");
			} catch (NoSuchElementException ex) {
				return respuesta("ERROR AL OBTENER INFORMACION DEL PAGO");
			}
			String codigoAdhesion = adhesion.codigoAdhesion;
			contexto.parametros.set("codigoAdhesion", codigoAdhesion);

			if (!estabaAdherido) {
				eliminarAdhesion(contexto);
			}

		} catch (Exception e) {
			return respuesta("ERROR", "descripcion", "Error al validar el código link");
		}

		String cuentaOrigen = cuenta.get("numeroProducto").toString();
		String enteDescripcion = ente(contexto, ente);

		Objeto obtenerDatosPago = (Objeto) obtenerDatosPago(contexto);
		Objeto informacionPago = (Objeto) obtenerDatosPago.get("datosPago");

		if (informacionPago == null) {
			return respuesta("ERROR AL OBTENER INFORMACION DEL PAGO");
		}
		int longitudReferencia;
		String referencia = null;
		String usuarioLP = informacionPago.get("ususarioLP").toString();
		LocalDate vencimientoAlPago = null;
		String idBase = null;

		Objeto vencimiento = (Objeto) informacionPago.get("vencimiento");
		if (vencimiento != null && !vencimiento.isEmpty()) {
			if (idDeuda != null) {

				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
				vencimientoAlPago = LocalDate.parse(vencimiento.get("fecha").toString(), formatter);
				idBase = vencimiento.get("id").toString();

				Objeto concepto = (Objeto) vencimiento.get("concepto");

				if (concepto.get("isIngresoReferencia").toString().equals("true")) {
					if (concepto.get("isLongitudReferencia").toString().equals("true")) {
						//Seteo de referencia
						longitudReferencia = (Integer) concepto.get("longitudMaximaTextoReferencia") - (Integer) concepto.get("longitudMinimaTextoReferencia");
						referencia = RandomStringUtils.random(longitudReferencia);
					}
				}
			}
			if (idDeuda == null) {
				DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyMMdd");
				Object vencimiento0 = vencimiento.toList().get(0);

				Gson gson = new Gson();
				String json = gson.toJson(vencimiento0);
				Map<String, Object> vencimientoMap = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
				}.getType());

				String fecha = (String) vencimientoMap.get("fecha");
				vencimientoAlPago = LocalDate.parse(fecha, formatter);
				if(ente!= null && ente.equals("FYI") && vencimientoMap.get("idDeudaPago")!=null){
					idBase = vencimientoMap.get("idDeudaPago").toString();
				}else{
					idBase = vencimientoMap.get("id").toString();
				}

				// importe = (BigDecimal) vencimientoMap.get("importe");

				Object concepto = vencimientoMap.get("concepto");
				Gson gsonConcepto = new Gson();
				String jsonConcepto = gsonConcepto.toJson(concepto);
				Map<String, Object> conceptoMap = gsonConcepto.fromJson(jsonConcepto, new TypeToken<Map<String, Object>>() {
				}.getType());

				if (conceptoMap.get("isIngresoReferencia").toString().equals("true")) {
					if (conceptoMap.get("isLongitudReferencia").toString().equals("true")) {
						// seteo de referencia

						Double longMax = (Double) conceptoMap.get("longitudMaximaTextoReferencia");
						double lmax = longMax.doubleValue();

						Double longMin = (Double) conceptoMap.get("longitudMinimaTextoReferencia");
						double lmin = longMin.doubleValue();

						longitudReferencia = (int) lmax - (int) lmin;
						referencia = RandomStringUtils.random(longitudReferencia);
					}
				}
			}

		} else if (informacionPago.get("conceptos") != null || !informacionPago.get("conceptos").toString().isEmpty()) {
			Objeto conceptos = (Objeto) informacionPago.get("conceptos");
			List<Object> listaConceptos = conceptos.toList();

			for (Object concepto : listaConceptos) {
				Gson gson = new Gson();
				String json = gson.toJson(concepto);
				Map<String, Double> mapLong = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
				}.getType());
				Map<String, Object> map = gson.fromJson(json, new TypeToken<Map<String, Object>>() {
				}.getType());

				if (map.get("codigo").equals(conceptoIngresado) && map.get("isIngresoReferencia").equals(true)) {
					double longMinRef = mapLong.get("longitudMinimaTextoReferencia");
					Integer longMin = (int) longMinRef;
					referencia = RandomStringUtils.randomNumeric(longMin);
				}
			}
		}

		PagoDeServiciosOB pago = servicioPagoDeServicios.crear(contexto, ente, enteDescripcion, rubro, codigoLink, conceptoIngresado, cuentaOrigen, importe, referencia, identificadorPago, usuarioLP, vencimientoAlPago, sesion.empresaOB, idBase, descripcion).get();

		BandejaOB bandeja = servicioBandeja.find(pago.id).get();
		EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

		servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
		servicioHistorialPagoDeServicios.crear(pago, accionCrear, empresaUsuario);

		if (adherirServicio) {
			contexto.parametros.set("rubro", rubro);
			contexto.parametros.set("ente", ente);
			contexto.parametros.set("codigoLink", codigoLink);

			datos.set("crearAdhesion", crearAdhesion(contexto));
		}

		contexto.parametros.set("idSolicitudPago", pago.id);
		datos.set("detalle", detalleSolicitud(contexto));

		return respuesta("datos", datos);
	}

	public static Object validarPagoEnBandeja(ContextoOB contexto) {
		String ente = contexto.parametros.string("ente");
		String codigoLink = contexto.parametros.string("codigoLink");

		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
		EstadoPagoOB estadoEnBandeja = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo()).get();
		SesionOB sesion = contexto.sesion();

		if (!servicioPagoDeServicios.buscarPorCpeEstadoDelPagoYEmpresa(ente, codigoLink, estadoEnBandeja.id, sesion.empresaOB).get().isEmpty()) {
			return respuesta("EXISTE_SOLICITUD_EN_BANDEJA");
		}

		return respuesta("0");
	}

	public static Object obtenerDatosPago(ContextoOB contexto) {
		String codigoLink = contexto.parametros.string("codigoLink");
		contexto.parametros.string("rubro");
		contexto.parametros.string("ente");
		String idDeuda = contexto.parametros.string("idDeuda", null);
		int iteracion;
		int iteracionMaxima=12;
        String enteLink=contexto.parametros.string("ente");
		String ARBAWEB="FYI";
		List<TarjetaVirtualOB> tarjetasVirtuales = contexto.sesion().tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		Objeto datosRespuesta = new Objeto();
		boolean estabaAdherido = true;
				
		Objeto tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, tarjetasVirtuales, enteLink);
				
		if (empty(tvAdh.get("datos.adhesion"))) {
			crearAdhesion(contexto);

			tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, contexto.sesion().tarjetasVirtuales, enteLink);
			iteracion=0;
			while(tvAdh.get("datos.adhesion")==null && iteracion<iteracionMaxima){
				try {
					Thread.sleep(500);
					tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, contexto.sesion().tarjetasVirtuales, enteLink);
				}catch (Exception ignored){}
				iteracion++;
			}

			estabaAdherido = false;
		}

		LinkAdhesiones.ServicioAdhesion adhesion;
		
		try {			
			adhesion = (ServicioAdhesion) tvAdh.get("datos.adhesion");
		} catch (NoSuchElementException ex) {
			return respuesta("ERROR AL OBTENER INFORMACION DEL PAGO");
		}
		String codigoAdhesion = adhesion.codigoAdhesion;
		contexto.parametros.set("codigoAdhesion", codigoAdhesion);

		PagosOB pagosPendientes = ApiLink.pagosPendientesOB(contexto, tvAdh.get("datos.tarjeta").toString()).tryGet();

		boolean pedienteNoEncontrado= pagosPendientes.stream().noneMatch(p -> p.codigoAdhesion.equals(codigoAdhesion));

		iteracion=0;
		while(pedienteNoEncontrado && iteracion<iteracionMaxima){
			try {
				Thread.sleep(500);
				pagosPendientes = ApiLink.pagosPendientesOB(contexto, tvAdh.get("datos.tarjeta").toString()).tryGet();
				pedienteNoEncontrado= pagosPendientes.stream().noneMatch(p -> p.codigoAdhesion.equals(codigoAdhesion));
			}catch (Exception ignored){}
			iteracion++;
		}

		if (pedienteNoEncontrado) {
			return respuesta("PAGO_ADHERIDO_YA_PAGADO");
		}

		PagoUnicoDTO pendienteUnico = new PagoUnicoDTO();

		PagoOB pendiente = pagosPendientes.stream().filter(p -> p.codigoAdhesion.equals(codigoAdhesion)).findFirst().get();
		
		pendienteUnico.setCodigoAdhesion(pendiente.codigoAdhesion);
		pendienteUnico.setCodigoLink(pendiente.ususarioLP);

		RubroDTO rubroDTO = new RubroDTO(pendiente.ente.rubro.codigo, pendiente.ente.rubro.descripcion);
		EnteDTO enteDTO = new EnteDTO(pendiente.ente.codigo, pendiente.ente.descripcion, rubroDTO);
		pendienteUnico.setEnte(enteDTO);

		if (idDeuda != null) {
			Objeto datos = new Objeto();

			PagosOB.Vencimiento vencimiento = null;
			if(pendiente.ente.codigo!=null && pendiente.ente.codigo.equalsIgnoreCase(ARBAWEB)){
				vencimiento = pendiente.vencimiento.stream().filter(v -> v.idDeudaPago.equals(idDeuda)).findFirst().get();
			}else{
				vencimiento = pendiente.vencimiento.stream().filter(v -> v.id.equals(idDeuda)).findFirst().get();
			}

			ConceptoDTO conceptoDTO = new ConceptoDTO(vencimiento.concepto.codigo, vencimiento.concepto.descripcion, vencimiento.concepto.isIngresoReferencia, vencimiento.concepto.isLongitudReferencia, vencimiento.concepto.longitudMinimaTextoReferencia, vencimiento.concepto.longitudMaximaTextoReferencia, vencimiento.concepto.ingresoImportes);
			VencimientoDTO vencimientoDTO = new VencimientoDTO(((pendiente.ente.codigo!=null && !pendiente.ente.codigo.equalsIgnoreCase(ARBAWEB)) ? vencimiento.id : vencimiento.idDeudaPago), vencimiento.fecha, vencimiento.importe, conceptoDTO);
			pendienteUnico.setVencimiento(vencimientoDTO);

			datos.set("codigoAdhesion", pendienteUnico.getCodigoAdhesion());
			datos.set("ususarioLP", pendienteUnico.getCodigoLink());

			Objeto ente = new Objeto();
			ente.set("codigo", pendienteUnico.getEnte().getCodigo());
			ente.set("descripcion", pendienteUnico.getEnte().getDescripcion());

			Objeto rubro = new Objeto();
			rubro.set("codigo", pendienteUnico.getEnte().getRubro().getCodigo());
			rubro.set("descripcion", pendienteUnico.getEnte().getRubro().getDescripcion());
			ente.set("rubro", rubro);

			datos.set("ente", ente);

			Objeto vencimientoUnico = new Objeto();
			vencimientoUnico.set("id", pendienteUnico.getVencimiento().getId());
			vencimientoUnico.set("fecha", pendienteUnico.getVencimiento().getFecha());
			vencimientoUnico.set("importe", pendienteUnico.getVencimiento().getImporte());

			Objeto concepto = new Objeto();
			concepto.set("codigo", pendienteUnico.getVencimiento().getConcepto().getCodigo());
			concepto.set("descripcion", pendienteUnico.getVencimiento().getConcepto().getDescripcion().trim());
			concepto.set("isIngresoReferencia", pendienteUnico.getVencimiento().getConcepto().getIngresoReferencia());
			concepto.set("isLongitudReferencia", pendienteUnico.getVencimiento().getConcepto().getLongitudReferencia());
			concepto.set("longitudMinimaTextoReferencia", pendienteUnico.getVencimiento().getConcepto().getLongitudMinimaTextoReferencia());
			concepto.set("longitudMaximaTextoReferencia", pendienteUnico.getVencimiento().getConcepto().getLongitudMaximaTextoReferencia());
			vencimientoUnico.set("concepto", concepto);

			datos.set("vencimiento", vencimientoUnico);

			datosRespuesta.set("datosPago", datos);

		} else {
			Objeto pendienteRespuesta = pendiente.objeto();
			datosRespuesta.set("datosPago", pendienteRespuesta);
		}
		datosRespuesta.set("codigoLink", codigoLink);

		if (!estabaAdherido) {
			eliminarAdhesion(contexto);
		}

		return datosRespuesta;
	}

	public static Object conceptos(ContextoOB contexto) {
		String codigoLink = contexto.parametros.string("codigoLink");
		contexto.parametros.string("rubro");
		contexto.parametros.string("ente");
        String enteLink=contexto.parametros.string("ente");
		List<TarjetaVirtualOB> tarjetasVirtuales = contexto.sesion().tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		Objeto respuesta = new Objeto();
		boolean estabaAdherido = true;

		Objeto tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, tarjetasVirtuales, enteLink);
		
		if (empty(tvAdh.get("datos.adhesion"))) {
			crearAdhesion(contexto);
			tvAdh = (Objeto) obtenerTarjetaAdhesion(contexto, codigoLink, contexto.sesion().tarjetasVirtuales, enteLink);
			estabaAdherido = false;
		}
		
		LinkAdhesiones.ServicioAdhesion adhesion = (ServicioAdhesion) tvAdh.get("datos.adhesion");
		String codigoAdhesion = adhesion.codigoAdhesion;
		contexto.parametros.set("codigoAdhesion", codigoAdhesion);

		PagosOB pagosPendientes = ApiLink.pagosPendientesOB(contexto, (String) tvAdh.get("datos.tarjeta")).get();

		PagoOB pendiente = pagosPendientes.stream().filter(p -> p.codigoAdhesion.equals(codigoAdhesion)).findFirst().get();
		if (pendiente.vencimiento.isEmpty()) {
			for (PagosOB.Conceptos concepto : pendiente.conceptos) {

				Objeto datos = new Objeto();
				datos.set("codigo", concepto.codigo);
				datos.set("descripcion", concepto.descripcion);
				respuesta.add(datos);
			}
		} else {
			Objeto datos = new Objeto();
			datos.set("codigo", pendiente.vencimiento.get(0).concepto.codigo);
			datos.set("descripcion", pendiente.vencimiento.get(0).concepto.descripcion);
			respuesta.add(datos);
		}

		if (!estabaAdherido) {
			eliminarAdhesion(contexto);
		}

		return respuesta;
	}

	public static Object crearAdhesion(ContextoOB contexto) {
		String codigoRubro = contexto.parametros.string("rubro");
		String ente = contexto.parametros.string("ente");
		String codigoLink = contexto.parametros.string("codigoLink");
		String referencia = contexto.parametros.string("descripcion",null);

		List<TarjetaVirtualOB> tarjetasVirtuales = contexto.sesion().tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		contexto.parametros.set("rubro", codigoRubro);
		contexto.parametros.set("ente", ente);

		Objeto ent = (Objeto) OBPagos.ente(contexto);
		if (!ent.get("estado").equals("0")) {
			if (ent.get("estado").equals("RUBRO_INVALIDO"))
				return respuesta("RUBRO_INVALIDO");
			if (ent.get("estado").equals("ENTE_INVALIDO"))
				return respuesta("ENTE_INVALIDO");
		}

		RespuestaOk respuestaOk;
		String tarjetaVirtual = OBTarjetaVirtual.obtenerTarjetaVirtual(contexto, tarjetasVirtuales);

		Boolean esBaseDeuda = ent.get("datos.ente.isBaseDeuda").toString().equals("true");
		try {
			respuestaOk = ApiLink.crearAdhesiones(contexto, tarjetaVirtual, ente, esBaseDeuda, codigoLink).get();
		} catch (Exception e) {
			return respuesta("ERROR_AL_CREAR_ADHESION");
		}

		if (referencia!=null) new ServicioReferenciaPagoServiciosOB(contexto).crear(contexto.sesion().empresaOB, ente,codigoLink,referencia);
		return respuesta("respuesta", respuestaOk);
	}

	public static Object eliminarAdhesion(ContextoOB contexto) {
		String codigoAdhesion = contexto.parametros.string("codigoAdhesion");
		String codigoLink = contexto.parametros.string("codigoLink");

		SesionOB sesion = contexto.sesion();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}
		
		RespuestaOk respuesta = null;
		
		Objeto tvAdh = (Objeto) OBPagos.obtenerTarjetaAdhesion(contexto, codigoLink, tarjetasVirtuales, "");
		if (!empty(tvAdh.get("datos.adhesion"))) {
			respuesta = ApiLink.eliminarAdhesiones(contexto, tvAdh.get("datos.tarjeta").toString(), codigoAdhesion).get();
		}
				
		if (respuesta != null && !respuesta.ok) {
			return respuesta("ERROR_AL_ELIMINAR_ADHESION");
		}
		return respuesta("0");
	}

	public static Object adhesiones(ContextoOB contexto) {
		String ente = contexto.parametros.string("ente", null);
		SesionOB sesion = contexto.sesion();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		Objeto servicios = new Objeto();
		List<ReferenciaPagoServicioOB> referencias = new ServicioReferenciaPagoServiciosOB(contexto).buscarPorEmpresa(contexto.sesion().empresaOB).get();
		for(TarjetaVirtualOB tv : tarjetasVirtuales) {
			LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, tv.nroTarjeta).tryGet();
			
			if(!empty(ente)) {
				adhesiones.forEach(p -> {
					Optional<ReferenciaPagoServicioOB> referencia = referencias.stream()
							.filter(ref -> ref.codigoLink.equals(p.codigoPagoElectronico) && ref.ente.equals(p.ente.codigo))
							.findAny();
				    if (p.ente.codigo.equals(ente)) {
				        servicios.add(new Objeto()
				                .set("codigoAdhesion", p.codigoAdhesion)
				                .set("codigoLink", p.codigoPagoElectronico)
				                .set("rubro", p.ente.rubro.codigo)
				                .set("enteId", p.ente.codigo)
				                .set("ente", p.ente.descripcion)
								.set("referencia",referencia.isPresent()?referencia.get().referencia:"")
								.set("codigoAdhesion",p.codigoAdhesion)
				        );
				    }
				});
			}else {
				adhesiones.forEach(p -> {
					Optional<ReferenciaPagoServicioOB> referencia = referencias.stream()
							.filter(ref -> ref.codigoLink.equals(p.codigoPagoElectronico) && ref.ente.equals(p.ente.codigo))
							.findAny();
						servicios
						.add(new Objeto()
								.set("codigoAdhesion", p.codigoAdhesion)
								.set("codigoLink", p.codigoPagoElectronico)
								.set("rubro", p.ente.rubro.codigo)
								.set("enteId", p.ente.codigo)
								.set("ente", p.ente.descripcion)
								.set("referencia",referencia.isPresent()?referencia.get().referencia:"")
								.set("codigoAdhesion",p.codigoAdhesion));
			});
			}			
		}
				
		if (empty(servicios)) {
			return respuesta("SIN_ADHESIONES");
		}

		return respuesta("datos", servicios);
	}

	public static Object editarReferencia(ContextoOB contexto){
		String ente = contexto.parametros.string("ente");
		String codigoLink = contexto.parametros.string("codigoLink");
		String nuevaReferencia = contexto.parametros.string("referencia");

		ServicioReferenciaPagoServiciosOB servicioReferenciaPagoServiciosOB = new ServicioReferenciaPagoServiciosOB(contexto);
		ReferenciaPagoServicioOB referencia = servicioReferenciaPagoServiciosOB.buscar(contexto.sesion().empresaOB,ente,codigoLink).get();
		if (empty(referencia)){
			servicioReferenciaPagoServiciosOB.crear(contexto.sesion().empresaOB, ente,codigoLink,nuevaReferencia);
		}else{
			referencia.referencia = nuevaReferencia;
			servicioReferenciaPagoServiciosOB.actualizar(referencia);
		}


		return respuesta("0");
	}

	public static Object entesAdheridos(ContextoOB contexto) {
		String codigoLink = contexto.parametros.string("codigoLink", null);
		String ente = contexto.parametros.string("ente", null);
		SesionOB sesion = contexto.sesion();

		List<TarjetaVirtualOB> tarjetasVirtuales = sesion.tarjetasVirtuales;
		if (empty(tarjetasVirtuales)) {
			return respuesta("TARJETA_VIRTUAL_INVALIDA");
		}

		Objeto servicios = new Objeto();
		Set<String> codigosExistentes = new HashSet<>();
		
		for(TarjetaVirtualOB tv : tarjetasVirtuales) {
			LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, tv.nroTarjeta).tryGet();
			if(empty(codigoLink)){
				adhesiones.forEach(p -> {
					if (codigosExistentes.add(p.ente.codigo)) {
						servicios.add(new Objeto()
								.set("ente", p.ente.codigo)
								.set("descripcionEnte", p.ente.descripcion));
					}
				});
			}else {				
				for (LinkAdhesiones.ServicioAdhesion p : adhesiones) {
				    if (p.codigoPagoElectronico.equals(codigoLink) && p.ente.codigo!=null && p.ente.codigo.equals(ente)) {
				        return respuesta("CODIGO_ADHERIDO");
				    }
				}
			}			
		}
				
		if (empty(servicios)) {
			return respuesta("SIN_ADHESIONES");
		}

		return respuesta("datos", servicios);
	}

	
	public static Object pagosPendientes(ContextoOB contexto) {
		String codigoRubro = contexto.parametros.string("rubro", null);
		String ente = contexto.parametros.string("ente", null);
		String codigoLink = contexto.parametros.string("codigoLink", null);

		Objeto respuesta = new Objeto();
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioTarjetaVirtualOB servicioTarjetaVirtualOB = new ServicioTarjetaVirtualOB(contexto);
		SesionOB sesion = contexto.sesion();
		List<TarjetaVirtualOB> tarjetasVirtuales = null;
		Set<String> codigosEspeciales = Set.of("FYI");
		if (empty(sesion.tarjetasVirtuales)) {
			tarjetasVirtuales =
					contexto.sesion().empresaOB.emp_codigo.toString().equals("2101")
							?
							servicioTarjetaVirtualOB.buscarPorEmpresaAndId(sesion.empresaOB, 7786).get()
							:
							servicioTarjetaVirtualOB.buscarPorEmpresa(sesion.empresaOB).get();
			sesion.tarjetasVirtuales = tarjetasVirtuales;
			sesion.save();
		} else {
			tarjetasVirtuales = sesion.tarjetasVirtuales;
		}

		for (TarjetaVirtualOB tv : tarjetasVirtuales) {
			PagosOB pagosPendientes = ApiLink.pagosPendientesOB(contexto, tv.nroTarjeta).tryGet();
			int mismoEnteCodigo = 0;
			if(pagosPendientes!=null){
			for (PagoOB pendiente : pagosPendientes) {

				List<PagoDeServiciosOB> pagoOb = servicioPagoDeServicios.buscarPorEmpresaYEstadoCodigoLink(sesion.empresaOB, EnumEstadoPagosDeServicioYVepsOB.EN_BANDEJA.getCodigo(), pendiente.ususarioLP).get();

				if ((codigoRubro == null || pendiente.ente.rubro.codigo.equals(codigoRubro)) && (ente == null || pendiente.ente.codigo.equals(ente)) && (codigoLink == null || pendiente.ususarioLP.equals(codigoLink))) {

					Objeto datosPagoPendiente = new Objeto();

					datosPagoPendiente.set("codigoLink", pendiente.ususarioLP);
					datosPagoPendiente.set("codigoEnte", pendiente.ente.codigo);
					datosPagoPendiente.set("descripcionEnte", pendiente.ente.descripcion);
					datosPagoPendiente.set("codigoRubro", pendiente.ente.rubro.codigo);

					if (pendiente.vencimiento.isEmpty()) {
						datosPagoPendiente.set("vencimiento", null);
						datosPagoPendiente.set("importe", null);

						if ((long) pendiente.conceptos.size() > 1) {
							for (PagosOB.Conceptos concepto : pendiente.conceptos) {

								Objeto datosPagoPendientePorConcepto = new Objeto();
								datosPagoPendientePorConcepto.set("codigoLink", pendiente.ususarioLP);
								datosPagoPendientePorConcepto.set("vencimiento", datosPagoPendiente.get("vencimiento"));
								datosPagoPendientePorConcepto.set("importe", datosPagoPendiente.get("importe"));
								datosPagoPendientePorConcepto.set("codigoEnte", pendiente.ente.codigo);
								datosPagoPendientePorConcepto.set("descripcionEnte", pendiente.ente.descripcion);
								datosPagoPendientePorConcepto.set("codigoRubro", pendiente.ente.rubro.codigo);
								datosPagoPendientePorConcepto.set("conceptoCodigo", concepto.codigo);
								datosPagoPendientePorConcepto.set("conceptoDescripcion", concepto.descripcion.trim());

								if (!pagoOb.isEmpty()) {
									if (mismoEnteCodigo < pagoOb.size()) {
										datosPagoPendientePorConcepto.set("idEstadoPago", pagoOb.get(0).estado.id);
										datosPagoPendientePorConcepto.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);
										respuesta.add(datosPagoPendientePorConcepto);
										mismoEnteCodigo++;
									} else {
										datosPagoPendientePorConcepto.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);

										for (PagosOB.Vencimiento v : pendiente.vencimiento) {
											if (pagoOb.get(0).idDeuda != null && !pagoOb.get(0).idDeuda.isEmpty()) {
												if (pagoOb.get(0).monto.compareTo(v.importe) != 0 && !pagoOb.get(0).idDeuda.equals(v.idDeuda)) {
													datosPagoPendientePorConcepto.set("pagoEnBandeja", true);
												}
											} else {
												if (pagoOb.get(0).monto.compareTo(v.importe) != 0) {
													datosPagoPendientePorConcepto.set("pagoEnBandeja", true);
												}
											}
										}

										respuesta.add(datosPagoPendientePorConcepto);
									}
								} else {
									respuesta.add(datosPagoPendientePorConcepto);
								}
							}
						} else if (pendiente.conceptos.size() == 1) {
							if (!pagoOb.isEmpty()) {
								if (mismoEnteCodigo < pagoOb.size()) {
									datosPagoPendiente.set("idEstadoPago", pagoOb.get(0).estado.id);
									datosPagoPendiente.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);
									respuesta.add(datosPagoPendiente);
									mismoEnteCodigo++;
								} else {
									datosPagoPendiente.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);

									for (PagosOB.Vencimiento v : pendiente.vencimiento) {
										if (pagoOb.get(0).idDeuda != null && !pagoOb.get(0).idDeuda.isEmpty()) {
											if (pagoOb.get(0).monto.compareTo(v.importe) != 0 && !pagoOb.get(0).idDeuda.equals(v.idDeuda)) {
												datosPagoPendiente.set("pagoEnBandeja", true);
											}
										} else {
											if (pagoOb.get(0).monto.compareTo(v.importe) != 0) {
												datosPagoPendiente.set("pagoEnBandeja", true);
											}
										}
									}

									respuesta.add(datosPagoPendiente);
								}
							} else {
								respuesta.add(datosPagoPendiente);
							}
						}
					} else {
						for (PagosOB.Vencimiento vencimiento : pendiente.vencimiento) {
							Objeto datosPagoPendientePorVencimiento = new Objeto();
							datosPagoPendientePorVencimiento.set("idDeuda", codigosEspeciales.contains(pendiente.ente.codigo)
									? vencimiento.idDeudaPago
									: vencimiento.id);
							datosPagoPendientePorVencimiento.set("codigoLink", pendiente.ususarioLP);
							datosPagoPendientePorVencimiento.set("vencimiento", vencimiento.fecha);
							datosPagoPendientePorVencimiento.set("importe", vencimiento.importe);
							datosPagoPendientePorVencimiento.set("codigoEnte", pendiente.ente.codigo);
							datosPagoPendientePorVencimiento.set("descripcionEnte", pendiente.ente.descripcion);
							datosPagoPendientePorVencimiento.set("codigoRubro", pendiente.ente.rubro.codigo);
							datosPagoPendientePorVencimiento.set("conceptoCodigo", vencimiento.concepto.codigo);
							datosPagoPendientePorVencimiento.set("conceptoDescripcion", vencimiento.concepto.descripcion.trim());

							if (!pagoOb.isEmpty()) {
								if (mismoEnteCodigo < pagoOb.size()) {
									datosPagoPendientePorVencimiento.set("idEstadoPago", pagoOb.get(0).estado.id);
									datosPagoPendientePorVencimiento.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);
									respuesta.add(datosPagoPendientePorVencimiento);
									mismoEnteCodigo++;
								} else {
									datosPagoPendientePorVencimiento.set("descriptionEstadoPago", pagoOb.get(0).estado.descripcion);

									if (pagoOb.get(0).idDeuda != null && !pagoOb.get(0).idDeuda.isEmpty()) {
										if (pagoOb.get(0).monto.compareTo(vencimiento.importe) != 0 && !pagoOb.get(0).idDeuda.equals(vencimiento.idDeuda)) {
											datosPagoPendientePorVencimiento.set("pagoEnBandeja", true);
										}
									} else {
										if (pagoOb.get(0).monto.compareTo(vencimiento.importe) != 0) {
											datosPagoPendientePorVencimiento.set("pagoEnBandeja", true);
										}
									}

									respuesta.add(datosPagoPendientePorVencimiento);
								}
							} else {
								respuesta.add(datosPagoPendientePorVencimiento);
							}
						}
					}
				}
			}
		}
	}
		DateTimeFormatter formatoOriginal = DateTimeFormatter.ofPattern("yyMMdd");
		DateTimeFormatter formatoNuevo = DateTimeFormatter.ofPattern("dd/MM/yyyy");

		for (Objeto rta : respuesta.ordenar("vencimiento").objetos()) {
			if (rta.get("vencimiento") != null) {
				String fechaOriginal = rta.get("vencimiento").toString();
				LocalDate fechaNueva = LocalDate.parse(fechaOriginal, formatoOriginal);
				String vencimiento = fechaNueva.format(formatoNuevo);
				rta.set("vencimiento", vencimiento);
			}
		}

		return respuesta("datos", respuesta);
	}
	
	public static Object elegirEnteComprobantes(ContextoOB contexto) {
		String ente = contexto.parametros.string("ente");

		contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
		contexto.parametros.string("codigoLink", null);
		contexto.parametros.string("numeroVep", null);

		return pagos(contexto, ente);
	}
	
	
	protected static Object HistorialPagosServicios(ContextoOB contexto) {
		Objeto respuesta = new Objeto();
		SesionOB sesion = contexto.sesion();
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
		ServicioPagosVepOB servicioPagosVep = new ServicioPagosVepOB(contexto);
		Boolean previsualizacion = contexto.parametros.bool("previsualizacion", false);
		Boolean buscarVeps = contexto.parametros.bool("buscarVeps", false);
		Fecha fechaDesde= contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
		String tipoDePago=contexto.parametros.string("tipoDePago", null);
		String ente= contexto.parametros.string("ente", null);
		
		EstadoPagoOB estadoPagado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.PAGADO.getCodigo()).get();
	
		int i=0;
		
		if(!buscarVeps) {
			List<PagoDeServiciosOB> pagos = servicioPagoDeServicios.buscarPorEmpresaYEnte(sesion.empresaOB, ente, fechaDesde, fechaHasta).get();
			for (PagoDeServiciosOB pago : pagos) {
				Objeto datos = new Objeto();
				datos.set("cantidad", i++);
				datos.set("ente", pago.descripcionEnte);
				datos.set("enteId", pago.ente);
				datos.set("importe", pago.monto);
				datos.set("fechaPago", pago.fechaPago.toLocalDate().toString());
				datos.set("codigoLink", pago.codigoLink);
				datos.set("rubro", pago.rubro);
				datos.set("idBandeja", pago.id);

				if (pago.estadoBandeja.id.equals(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())) {
					datos.set("estado", pago.estado.descripcion);
				} else
					datos.set("estado", pago.estadoBandeja.descripcion);
				datos.set("descripcion", pago.descripcion);
				
				respuesta.add(datos);
				
				 if (previsualizacion) {
			            if (respuesta.toList().size() == 5) {
			                return respuesta("datos", respuesta);
			            }
			        }
			}
		}else {
			List<PagosVepOB> pagosVep = servicioPagosVep.buscarPorEmpresaYEnte(sesion.empresaOB, fechaDesde, fechaHasta, ente).get();
			for (PagosVepOB vep : pagosVep) {
				Objeto datos = new Objeto();
				datos.set("cantidad", i++);
				datos.set("ente", vep.descripcion);	
				datos.set("enteId", vep.numeroVep);
				datos.set("importe", vep.monto);
				datos.set("fechaPago", vep.fechaPago!=null ? vep.fechaPago.toLocalDate().toString():vep.fechaCreacion.toLocalDate().toString());
				datos.set("codigoLink", vep.numeroVep);
				datos.set("rubro", "Impuestos AFIP VEP");
				datos.set("idBandeja", vep.id);
				if (vep.estadoBandeja.id.equals(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())) {
					datos.set("estado", vep.estado.descripcion);
				} else
					datos.set("estado", vep.estadoBandeja.descripcion);
				datos.set("descripcion", vep.descripcion);
				respuesta.add(datos);
				
				if (previsualizacion) {
		            if (respuesta.toList().size() == 5) {
		                return respuesta("datos", respuesta);
		            }
		        }
			}
		}
		
		return respuesta("datos", respuesta);
	}
	

	protected static Object pagos(ContextoOB contexto, String ente) {
		Objeto respuesta = new Objeto();
		SesionOB sesion = contexto.sesion();
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioEstadoPagoOB servicioEstadoPago = new ServicioEstadoPagoOB(contexto);
		ServicioPagosVepOB servicioPagosVep = new ServicioPagosVepOB(contexto);

		EstadoPagoOB estadoPagado = servicioEstadoPago.find(EnumEstadoPagosDeServicioYVepsOB.PAGADO.getCodigo()).get();

		Fecha fechaHasta;
		Fecha fechaDesde;
		String codigoLink;
		String numeroVep;

		if (contexto.parametros.get("fechaDesde") == null) {
			fechaDesde = null;
		} else {
			fechaDesde = new Fecha(contexto.parametros.get("fechaDesde").toString(), "yyyy-MM-dd");
		}

		if (contexto.parametros.get("fechaHasta") == null) {
			fechaHasta = null;
		} else {
			fechaHasta = new Fecha(contexto.parametros.get("fechaHasta").toString(), "yyyy-MM-dd");
		}

		if (contexto.parametros.get("codigoLink") == null) {
			codigoLink = null;
		} else {
			codigoLink = contexto.parametros.get("codigoLink").toString();
		}

		if (contexto.parametros.get("numeroVep") == null) {
			numeroVep = null;
		} else {
			numeroVep = contexto.parametros.get("numeroVep").toString();
		}

		List<PagoDeServiciosOB> pagos = servicioPagoDeServicios.buscarPorEmpresaYFiltros(sesion.empresaOB, ente, fechaDesde, fechaHasta, estadoPagado, codigoLink).get();

		for (PagoDeServiciosOB pago : pagos) {
			Objeto datos = new Objeto();
			datos.set("ente", pago.descripcionEnte);
			datos.set("enteId", pago.ente);
			datos.set("importe", pago.monto);
			datos.set("fechaPago", pago.fechaPago.toLocalDate().toString());
			datos.set("codigoLink", pago.codigoLink);
			datos.set("rubro", pago.rubro);
			datos.set("idBandeja", pago.id);

			if (pago.estadoBandeja.id.equals(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())) {
				datos.set("estado", EnumEstadoPagosDeServicioYVepsOB.PAGADO.name());
			} else
				datos.set("estadoBandeja", pago.estadoBandeja.descripcion);
			datos.set("descripcion", pago.descripcion);
			respuesta.add(datos);
		}

		if (ente.equals("0")) {
			List<PagosVepOB> pagosVep = servicioPagosVep.buscarPorEmpresaYFiltros(sesion.empresaOB, fechaDesde, fechaHasta, estadoPagado, numeroVep).get();

			for (PagosVepOB vep : pagosVep) {
				Objeto datos = new Objeto();
				datos.set("descripcion", vep.descripcion);

				if (vep.estadoBandeja.id.equals(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())) {
					datos.set("estado", EnumEstadoPagosDeServicioYVepsOB.PAGADO.name());
				} else
					datos.set("estadoBandeja", vep.estadoBandeja.descripcion);

				datos.set("fechaPago", vep.fechaPago.toLocalDate().toString());
				datos.set("numeroVep", vep.numeroVep);
				datos.set("importe", vep.monto);
				datos.set("idBandeja", vep.id);
				datos.set("ente", 0);
				respuesta.add(datos);
			}
		}

		return respuesta("datos", respuesta);
	}

	public static Object detalleComprobante(ContextoOB contexto) {
		Integer idBandeja = contexto.parametros.integer("idBandeja");

		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		Objeto datos = new Objeto();

		PagoDeServiciosOB pago = servicioPagoDeServicios.find(idBandeja).get();

		if (pago == null) {
			return respuesta("No existe comprobante con ese ID de operacion");
		}

		datos.set("codigoLink", pago.codigoLink);
		datos.set("ente", pago.descripcionEnte);
		datos.set("enteId", pago.ente);
		datos.set("importe", pago.monto);
		datos.set("fechaPago", pago.fechaPago.toLocalDate().toString() + " " + pago.fechaPago.toLocalTime().withSecond(0).withNano(0).toString());

		if (pago.vencimiento != null) {
			datos.set("vencimiento", pago.vencimiento.toString());
		} else
			datos.set("vencimiento", null);

		datos.set("idOperacion", idBandeja);
		datos.set("cuenta", pago.cuentaOrigen);

		Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
		datos.set("cbu", cuenta.get("cbu"));

		datos.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
		datos.set("fechaCreacion", pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());

		BandejaOB bandeja = servicioBandeja.find(idBandeja).get();

		datos.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
		datos.set("descripcion", pago.descripcion);
		
		return datos;
	}
	
	
	public static Object detalleComprobanteServiciosVeps(ContextoOB contexto) {
		Integer idBandeja = contexto.parametros.integer("idBandeja");
		Boolean buscarVeps = contexto.parametros.bool("buscarVeps", false);
		
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		Objeto datos = new Objeto();
		
		if(!buscarVeps) {
		
			ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
			PagoDeServiciosOB pago = servicioPagoDeServicios.find(idBandeja).get();
			
			if (pago == null) {
				return respuesta("No existe comprobante con ese ID de operacion");
			}
			datos.set("codigoLink", pago.codigoLink);
			datos.set("ente", pago.descripcionEnte);
			datos.set("enteId", pago.ente);
			datos.set("importe", pago.monto);
			datos.set("fechaPago",
					pago.fechaPago!=null 
					? pago.fechaPago.toLocalDate().toString() + " " + pago.fechaPago.toLocalTime().withSecond(0).withNano(0).toString()
					: pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
			if (pago.vencimiento != null) {
				datos.set("vencimiento", pago.vencimiento.toString());
			} else
				datos.set("vencimiento", null);
			datos.set("idOperacion", idBandeja);
			datos.set("cuenta", pago.cuentaOrigen);
			Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
			datos.set("cbu", cuenta.get("cbu"));
			datos.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
			datos.set("fechaCreacion", pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
			BandejaOB bandeja = servicioBandeja.find(idBandeja).get();
			datos.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
			datos.set("descripcion", pago.descripcion);
			
		}else {
			
			ServicioPagosVepOB servicioPagoVeps = new ServicioPagosVepOB(contexto);
			PagosVepOB pago = servicioPagoVeps.find(idBandeja).get();
			
			if (pago == null) {
				return respuesta("No existe comprobante con ese ID de operacion");
			}
			datos.set("codigoLink", pago.numeroVep);
			datos.set("ente", pago.descripcion);
			datos.set("enteId", pago.descripcion);
			datos.set("importe", pago.monto);
			datos.set("fechaPago",
					pago.fechaPago!=null 
					? pago.fechaPago.toLocalDate().toString() + " " + pago.fechaPago.toLocalTime().withSecond(0).withNano(0).toString()
					: pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
			datos.set("vencimiento", null);
			datos.set("idOperacion", idBandeja);
			datos.set("cuenta", pago.cuentaOrigen);
			Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
			datos.set("cbu", cuenta.get("cbu"));
			datos.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
			datos.set("fechaCreacion", pago.fechaCreacion.toLocalDate().toString() + " " + pago.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
			BandejaOB bandeja = servicioBandeja.find(idBandeja).get();
			datos.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
			datos.set("descripcion", pago.descripcion);
			datos.set("cuitContribuyente", pago.idTributarioContribuyente);
		}
				
		return datos;
	}

	public static Object detalleSolicitud(ContextoOB contexto) {
		Integer idPago = contexto.parametros.integer("idSolicitudPago");

		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		SesionOB sesion = contexto.sesion();

		PagoDeServiciosOB pago = servicioPagoDeServicios.find(idPago).get();
		if (pago == null || !pago.empresa.idCobis.equals(sesion.empresaOB.idCobis)) {
			return respuesta("DATOS_INVALIDOS");
		}

		Objeto pagoDetalle = new Objeto();
		pagoDetalle.set("id", pago.id);
		pagoDetalle.set("codigoLink", pago.codigoLink);
		pagoDetalle.set("nombreEnte", pago.descripcionEnte);
		pagoDetalle.set("monto", pago.monto);
		pagoDetalle.set("cuenta", pago.cuentaOrigen);
		pagoDetalle.set("tipo", pago.tipoProductoFirma.descripcion);
		pagoDetalle.set("moneda", pago.moneda.simbolo);
		pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
		pagoDetalle.set("descripcion", pago.descripcion);
		pagoDetalle.set("estadoOperacion", pago.estado.descripcion);

		Objeto estado = new Objeto();
		estado.set("id", pago.estadoBandeja.id);
		estado.set("descripcionCorta", pago.estadoBandeja.descripcion);
		pagoDetalle.set("estado", estado);

		Objeto cuenta = OBCuentas.cuenta(contexto, pago.cuentaOrigen);
		pagoDetalle.set("saldoCuenta", cuenta.get("saldoGirar"));
		
		BandejaOB bandeja = servicioBandeja.find(pago.id).get();
		pagoDetalle.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));

		return respuesta("datos", pagoDetalle);
	}

	@SuppressWarnings("unchecked")
	private static Objeto validarEnteYRubro(ContextoOB contexto, String codigoEnte) {
		Objeto entes = (Objeto) OBPagos.entes(contexto);
		if (!entes.get("estado").equals("0")) {
			return entes;
		}
		List<Objeto> lstEntes = (List<Objeto>) entes.get("datos.entes");
		Optional<Objeto> ente = lstEntes.stream().filter(e -> e.get("codigo").equals(codigoEnte)).findFirst();
		if (!ente.isPresent()) {
			return respuesta("ENTE_INVALIDO");
		}
		return ente.get();
	}

	protected static Boolean validarSaldoYCuenta(ContextoOB contexto, BigDecimal importePago, String cuentaOrigen) {
		Objeto cuenta = OBCuentas.cuenta(contexto, cuentaOrigen);
		BigDecimal saldoCuenta = (BigDecimal) cuenta.get("saldoGirar");
		Boolean tieneSaldo = false;

		if ((saldoCuenta.compareTo(importePago)) >= 0) {
			tieneSaldo = true;
		}

		return tieneSaldo;
	}

	public static Object obtenerTarjetaAdhesion(ContextoOB contexto, String codigoLink, List<TarjetaVirtualOB> tarjetasVirtuales, String ente) {
		Objeto datos = new Objeto();
		String codigoLinkSinEnte;

		for(TarjetaVirtualOB t : tarjetasVirtuales) {
			LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, t.nroTarjeta).get();
			if (!adhesiones.stream().noneMatch(a -> a.codigoPagoElectronico.equals(codigoLink) && a.ente.codigo!=null && a.ente.codigo.equals(ente) )) {
				datos.set("tarjeta", t.nroTarjeta);
				datos.set("adhesion", adhesiones.stream().filter(a -> a.codigoPagoElectronico.equals(codigoLink) && a.ente.codigo.equals(ente)).findFirst().get());
			}
		}

		if(datos.isEmpty() || datos==null) {
			for (TarjetaVirtualOB t : tarjetasVirtuales) {
				LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, t.nroTarjeta).get();
				if (!adhesiones.stream().noneMatch(a -> a.codigoPagoElectronico.equals(codigoLink))) {
					datos.set("tarjeta", t.nroTarjeta);
					datos.set("adhesion", adhesiones.stream().filter(a -> a.codigoPagoElectronico.equals(codigoLink)).findFirst().get());
				}
			}
		}

		if(datos.isEmpty() || datos==null){
			codigoLinkSinEnte = codigoLink.startsWith(ente)
					? codigoLink.substring(ente.length())
					: codigoLink;
			for(TarjetaVirtualOB t : tarjetasVirtuales) {
				LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, t.nroTarjeta).get();
				if (!adhesiones.stream()
						.noneMatch(a -> a.codigoPagoElectronico.equals(codigoLinkSinEnte) && a.ente.codigo.equals(ente))
				) {
					datos.set("tarjeta", t.nroTarjeta);
					datos.set("adhesion", adhesiones.stream().filter(a -> a.codigoPagoElectronico.equals(codigoLinkSinEnte)).findFirst().get());
				}
			}
		}
		
		return respuesta("datos", datos);
	}
	protected static Objeto filtroEnte(ContextoOB contexto) {
		Boolean filtroVeps = contexto.parametros.bool("filtroVeps", false);
		ServicioPagoDeServiciosOB servicioPagoDeServicios = new ServicioPagoDeServiciosOB(contexto);
		ServicioPagosVepOB servicioPagosVep = new ServicioPagosVepOB(contexto);
		SesionOB sesion = contexto.sesion();
		Objeto datos = new Objeto();
		Fecha fechaDesde= new Fecha("2023-12-26", "yyyy-MM-dd");
		Fecha fechaHasta =new Fecha("2023-12-27", "yyyy-MM-dd");

		if(!filtroVeps) {
			List<PagoDeServiciosOB> pagos = servicioPagoDeServicios.buscarPorEmpresaYEnte(sesion.empresaOB, null, null, null).get();
			Set<String> entesUnicos = new HashSet<>();

			List<Objeto> entesFiltrados = pagos.stream()
					.filter(p -> entesUnicos.add(p.ente + "|" + p.descripcionEnte))
					.map(p -> new Objeto().set("ente", p.ente).set("descripcionEnte", p.descripcionEnte))
					.collect(Collectors.toList());

			datos.set("entesFiltrados", entesFiltrados);
		}else {
			List<PagosVepOB> pagosVeps = servicioPagosVep.buscarPorEmpresaYEnte(sesion.empresaOB, null, null, null).get();
			Set<String> entesUnicos = new HashSet<>();

			List<Objeto> entesFiltrados = pagosVeps.stream()
					.filter(p -> entesUnicos.add(p.descripcion + "|" + p.descripcion))
					.map(p -> new Objeto().set("ente", p.descripcion).set("descripcionEnte", p.descripcion))
					.collect(Collectors.toList());

			datos.set("entesFiltrados", entesFiltrados);
		}

		return datos;
	}


}
	
