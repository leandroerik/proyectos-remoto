package ar.com.hipotecario.mobile.api;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.RespuestaMB;
import ar.com.hipotecario.mobile.conector.ApiMB;
import ar.com.hipotecario.mobile.conector.ApiRequestMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.lib.Formateador;
import ar.com.hipotecario.mobile.lib.Objeto;
import ar.com.hipotecario.mobile.lib.Pdf;
import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.PlazoFijoLogro;
import ar.com.hipotecario.mobile.negocio.TarjetaCredito;
import ar.com.hipotecario.mobile.servicio.CuentasService;
import ar.com.hipotecario.mobile.servicio.ProductosService;
import ar.com.hipotecario.mobile.servicio.RestArchivo;
import ar.com.hipotecario.mobile.servicio.RestCatalogo;
import ar.com.hipotecario.mobile.servicio.RestDebin;
import ar.com.hipotecario.mobile.servicio.RestPersona;
import ar.com.hipotecario.mobile.servicio.TransferenciaService;

public class MBArchivo {

	public static byte[] comprobante(ContextoMB contexto) {
		String id = contexto.parametros.string("id");
		String template = id.split("_")[0];
		Map<String, String> parametros = contexto.sesion().comprobante(id);
		contexto.setHeader("Content-Type", "application/pdf; name=Comprobante BH.pdf");

		if(template.equals("debinRecurrente")){
			return comprobanteDebinRecurrente(contexto, id);
		}

		if(template.equals("debinProgramado") || template.equals("debinProgramadoBaja")){
			return comprobanteDebinProgramado(contexto, id);
		}

		if (template.equals("debin")) {
			String idDebin = id.split("_")[1];
			ApiResponseMB detalle = RestDebin.detalleDebin(contexto, idDebin);

			if (detalle.hayError()) {
				throw new RuntimeException();
			}

			parametros = new HashMap<>();
			parametros.put("ID", idDebin);
			parametros.put("VENDEDOR_CBU", detalle.string("vendedor.cliente.cuenta.cbu"));
			parametros.put("VENDEDOR_ALIAS", detalle.string("vendedor.cliente.cuenta.alias"));
			parametros.put("VENDEDOR_CUIT", detalle.string("vendedor.cliente.idTributario"));
			parametros.put("VENDEDOR_NOMBRE", detalle.string("vendedor.cliente.nombreCompleto").trim());
			parametros.put("COMPRADOR_CBU", detalle.string("comprador.cliente.cuenta.cbu"));
			parametros.put("COMPRADOR_ALIAS", detalle.string("comprador.cliente.cuenta.alias"));
			parametros.put("COMPRADOR_CUIT", detalle.string("comprador.cliente.idTributario"));
			parametros.put("COMPRADOR_NOMBRE", detalle.string("comprador.cliente.nombreCompleto").trim());
			parametros.put("IMPORTE", (detalle.string("detalle.moneda.id").equals("2") ? "USD" : "$") + " " + Formateador.importe(detalle.bigDecimal("detalle.importe")));
			parametros.put("FECHA_HORA", detalle.date("fechaNegocio", "yyyy-MM-dd'T'HH:mm:ss", "dd/MM/yyyy"));
			parametros.put("CONCEPTO", TransferenciaService.conceptos().get(detalle.string("detalle.concepto")));
			parametros.put("DESCRIPCION", detalle.string("detalle.descripcion"));
			parametros.put("ESTADO", detalle.string("estado.codigo"));

		}

		return Pdf.generar(template, parametros);
	}

	public static RespuestaMB terminosCondiciones(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();

		ApiResponseMB response = RestArchivo.digitalizacionGetDocumentos(contexto);
		if (response.hayError()) {
			return RespuestaMB.error();
		}

		Objeto documentos = new Objeto();

		// Quitar documentos duplicados en base a la fecha
//		for (Objeto documento : response.objetos()) {
//			String nroTramiteWKF = documento.string("nroTramiteWKF");
//			Date fechaCreacion = documento.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss.sss");
//			if (fechaCreacion != null && !nroTramiteWKF.isEmpty()) {
//				Boolean existeMasNuevo = false;
//				for (Objeto documentoB : response.objetos()) {
//					if (nroTramiteWKF.equals(documentoB.string("nroTramiteWKF"))) {
//						existeMasNuevo |= fechaCreacion.getTime() < documentoB.date("fechaCreacion", "yyyy-MM-dd'T'HH:mm:ss.sss").getTime();
//					}
//				}
//				if (existeMasNuevo) {
//					continue;
//				}
//			}
//			documentos.add(documento);
//		}

		// Quitar documentos duplicados en base al orden
		Set<String> numerosTramiteWorkflow = new HashSet<>();
		List<Objeto> datos = response.objetos();
		Collections.reverse(datos);
		for (Objeto documento : datos) {
			String nroTramiteWKF = documento.string("nroTramiteWKF");
			String clave = nroTramiteWKF + documento.string("descripcionClase");
			if (!numerosTramiteWorkflow.contains(clave)) {
				documentos.add(documento);
			}
			numerosTramiteWorkflow.add(clave);
		}

		for (Objeto documento : documentos.ordenar("tituloDocumento").objetos()) {
			// Caja Ahorro
			if (documento.string("id").startsWith("FormOrigHBCA") || documento.string("descripcionClase").equals("FormOrigHBCA")) {
				Objeto item = new Objeto();
				item.set("id", documento.string("id"));
				item.set("tipo", "Caja Ahorro");
				item.set("descripcion", documento.string("tituloDocumento"));
				respuesta.add("documentos", item);
			}

			// Prestamos Personales
			if (documento.string("id").startsWith("FormOrigHBPP") || documento.string("descripcionClase").equals("FormOrigHBPP")) {
				Objeto item = new Objeto();
				item.set("id", documento.string("id"));
				item.set("tipo", "Préstamo Personal");
				item.set("descripcion", documento.string("tituloDocumento"));
				respuesta.add("documentos", item);
			}

			// Paquetes
			if (documento.string("id").startsWith("FormOrigPaquete") || documento.string("descripcionClase").equals("FormOrigPaquete")) {
				Objeto item = new Objeto();
				item.set("id", documento.string("id"));
				item.set("tipo", "Paquete");
				item.set("descripcion", documento.string("tituloDocumento"));
				respuesta.add("documentos", item);
			}
		}

		// Cuenta Comitente
		if (!contexto.cuentasComitentes().isEmpty()) {
			Objeto item = new Objeto();
			item.set("id", "CuentaComitente");
			item.set("tipo", "Cuenta Comitente");
			item.set("descripcion", "Cuenta Comitente");
			respuesta.add("documentos", item);
		}

		// Cuenta Cuotapartista (TODO: REFACTOR)
		ApiResponseMB responseFiltrada = ProductosService.productos(contexto);
		if (responseFiltrada.hayError()) {
			return RespuestaMB.error();
		}
		for (Objeto objeto : responseFiltrada.objetos("productos")) {
			if (objeto.string("tipo").equals("RJA")) {
				Objeto itemF2704 = new Objeto();
				itemF2704.set("id", "CuentaCuotapartistaF2704");
				itemF2704.set("tipo", "Cuenta Cuotapartista F2704");
				itemF2704.set("descripcion", "Formulario F2704 - Constancia Entrega Reglamento FCI Toronto - Banco Valores S.A.");
				respuesta.add("documentos", itemF2704);

				Objeto itemF2705 = new Objeto();
				itemF2705.set("id", "CuentaCuotapartistaF2705");
				itemF2705.set("tipo", "Cuenta Cuotapartista F2705");
				itemF2705.set("descripcion", "Formulario F2705 - Constancia Entrega Reglamento FCI Toronto - Deutsche Bank S.A.");
				respuesta.add("documentos", itemF2705);
			}
		}

		// Plazo Fijo Logros
		for (PlazoFijoLogro plazoFijoLogro : contexto.plazosFijosLogros()) {
			try {
				String fecha = "2018-09-06";
				fecha = plazoFijoLogro.fechaAlta().after(new SimpleDateFormat("yyyy-MM-dd").parse("2018-12-28")) ? "2018-12-28" : fecha;
				fecha = plazoFijoLogro.fechaAlta().after(new SimpleDateFormat("yyyy-MM-dd").parse("2019-03-08")) ? "2019-03-08" : fecha;

				Objeto item = new Objeto();
				item.set("id", "plazo-fijo-logro-" + fecha);
				item.set("tipo", "Plazo Fijo Logro: " + plazoFijoLogro.nombre());
				item.set("descripcion", "Plazo Fijo Logro");
				respuesta.add("documentos", item);
			} catch (ParseException e) {
				throw new RuntimeException(e);
			}
		}

		return respuesta;
	}

	public static byte[] archivoDigitalizado(ContextoMB contexto) {
		String id = contexto.parametros.string("id");

		if (Objeto.anyEmpty(id)) {
			return null;
		}

		if (id.startsWith("plazo-fijo-logro-")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/terminosycondiciones/" + id + ".pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
			return archivo;
		}

		if (id.equals("CuentaCuotapartistaF2704")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/terminosycondiciones/F2704_Constancia_reglamento_FCI_Banco_de_Valores.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
			return archivo;
		}

		if (id.equals("CuentaCuotapartistaF2705")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/terminosycondiciones/F2705_Constancia_reglamento_FCI_Deutsche_Bank.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
			return archivo;
		}

		if (id.equals("CuentaComitente")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/terminosycondiciones/CuentaComitente.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=" + id + ".pdf");
			return archivo;
		}

		if (id.startsWith("inversor-comisiones")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/cuentainversor/F2784_Comisiones_cargos_y_tasas_cta_comitente_042024_v1.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=F2784_Comisiones_y_tasas_para_cuenta_comitente.pdf");
			return archivo;
		}

		if (id.startsWith("inversor-solicitud-apertura")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/cuentainversor/F2786_Solicitud_Apertura_Cuenta_Comitente.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=F2786_Solicitud_Apertura_Cuenta_Comitente.pdf");
			return archivo;
		}

		if (id.startsWith("inversor-constancia-recibo")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/cuentainversor/F3004_Constancia_recibo_de_entrega_Reglamento_de_Gestion_BACS_02_2022.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=F3004_Constancia_recibo_de_entrega_Reglamento_de_Gestion_BACS_02_2022.pdf");
			return archivo;
		}

		if (id.startsWith("cuenta-especial")) {
			ByteArrayOutputStream buffer = null;
			try {
				InputStream is = ConfigMB.class.getResourceAsStream("/cuentaespecial/F2001_Caja_de_ah_Homebanking_clausulas_Com_A_7199_082024_vf.pdf");
				buffer = new ByteArrayOutputStream();
				int nRead;
				byte[] data = new byte[1024];
				while ((nRead = is.read(data, 0, data.length)) != -1) {
					buffer.write(data, 0, nRead);
				}
				buffer.flush();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}

			byte[] archivo = buffer.toByteArray();
			contexto.setHeader("Content-Type", "application/pdf; name=F3004_Constancia_recibo_de_entrega_Reglamento_de_Gestion_BACS_02_2022.pdf");
			return archivo;
		}

		ApiResponseMB response = RestArchivo.digitalizacionGetArchivo(contexto, id);
		String base64 = response.string("bytesDocumento");
		byte[] archivo = Base64.getDecoder().decode(base64);
		try {
			archivo = Base64.getDecoder().decode(new String(archivo));
		} catch (Exception e) {
		}
		contexto.setHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=" + id + "." + response.string("propiedades.ExtArchivo", "pdf"));
		return archivo;
	}

	public static RespuestaMB simularLibreDeuda(ContextoMB contexto) {
		String idProducto = contexto.parametros.string("idProducto");

		if (Objeto.anyEmpty(idProducto)) {
			return RespuestaMB.parametrosIncorrectos();
		}

		Cuenta cuenta = contexto.cuenta(idProducto);
		if (cuenta != null && cuenta.esCuentaCorriente()) {
			if (cuenta.saldo().longValue() < 0L) {
				return RespuestaMB.estado("DEUDA");
			}

			Objeto parametros = new Objeto();
			parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
			parametros.add(new Objeto().set("key", "NUMERO_CUENTA").set("value", cuenta.numero()));
			parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
			parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

			ApiRequestMB request = ApiMB.request("LibreDeudaCuentaCorriente", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
			request.path("idComprobante", ConfigMB.string("id_comprobante_cc"));
			request.body("parametros", parametros);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), cuenta.numero());
			if (response.hayError()) {
				return RespuestaMB.error();
			}
			return RespuestaMB.exito();
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idProducto);
		if (tarjetaCredito != null) {
			ApiRequestMB requestMora = ApiMB.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
			requestMora.query("idClienteCobis", contexto.idCobis());
			ApiResponseMB responseMora = ApiMB.response(requestMora, contexto.idCobis());
			if (!responseMora.hayError()) {
				for (Objeto item : responseMora.objetos()) {
					if (item.string("pro_cod").equals("203")) {
						return RespuestaMB.estado("DEUDA");
					}
				}
			} else {
				return RespuestaMB.error();
			}

			Objeto parametros = new Objeto();
			parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
			parametros.add(new Objeto().set("key", "NUMERO_TARJETA").set("value", tarjetaCredito.numero()));
			parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
			parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

			ApiRequestMB request = ApiMB.request("LibreDeudaTarjetaCredito", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
			request.path("idComprobante", ConfigMB.string("id_comprobante_tc"));
			request.body("parametros", parametros);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), tarjetaCredito.numero());
			if (response.hayError()) {
				return RespuestaMB.error();
			}
			return RespuestaMB.exito();
		}

		return RespuestaMB.estado("PRODUCTO_NO_ENCONTRADO");
	}

	public static byte[] libreDeuda(ContextoMB contexto) {
		String idProducto = contexto.parametros.string("idProducto");

		if (Objeto.anyEmpty(idProducto)) {
			return null;
		}

		Cuenta cuenta = contexto.cuenta(idProducto);
		if (cuenta != null && cuenta.esCuentaCorriente()) {
			if (cuenta.saldo().longValue() < 0L) {
				return null;
			}

			Objeto parametros = new Objeto();
			parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
			parametros.add(new Objeto().set("key", "NUMERO_CUENTA").set("value", cuenta.numero()));
			parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
			parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

			ApiRequestMB request = ApiMB.request("LibreDeudaCuentaCorriente", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
			request.path("idComprobante", ConfigMB.string("id_comprobante_cc"));
			request.body("parametros", parametros);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), cuenta.numero());
			String base64 = response.string("comprobante");
			byte[] archivo = Base64.getDecoder().decode(base64);
			try {
				archivo = Base64.getDecoder().decode(new String(archivo));
			} catch (Exception e) {
			}
			contexto.setHeader("Content-Type", "application/pdf; name=LibreDeuda-" + cuenta.numero() + ".pdf");
			return archivo;
		}

		TarjetaCredito tarjetaCredito = contexto.tarjetaCredito(idProducto);
		if (tarjetaCredito != null) {
			ApiRequestMB requestMora = ApiMB.request("Moras", "moras", "GET", "/v1/productosEnMora", contexto);
			requestMora.query("idClienteCobis", contexto.idCobis());
			ApiResponseMB responseMora = ApiMB.response(requestMora, contexto.idCobis());
			if (!responseMora.hayError()) {
				for (Objeto item : responseMora.objetos()) {
					if (item.string("pro_cod").equals("203")) {
						return null;
					}
				}
			}

			Objeto parametros = new Objeto();
			parametros.add(new Objeto().set("key", "NOMBRE_PERSONA").set("value", contexto.persona().nombreCompleto()));
			parametros.add(new Objeto().set("key", "NUMERO_TARJETA").set("value", tarjetaCredito.numero()));
			parametros.add(new Objeto().set("key", "FECHA").set("value", new SimpleDateFormat("dd/MM/yyyy").format(new Date())));
			parametros.add(new Objeto().set("key", "DNI_PERSONA").set("value", contexto.persona().numeroDocumento()));

			ApiRequestMB request = ApiMB.request("LibreDeudaTarjetaCredito", "comprobantes", "POST", "/v1/comprobante/{idComprobante}", contexto);
			request.path("idComprobante", ConfigMB.string("id_comprobante_tc"));
			request.body("parametros", parametros);

			ApiResponseMB response = ApiMB.response(request, contexto.idCobis(), tarjetaCredito.numero());
			String base64 = response.string("comprobante");
			byte[] archivo = Base64.getDecoder().decode(base64);
			try {
				archivo = Base64.getDecoder().decode(new String(archivo));
			} catch (Exception e) {
			}
			contexto.setHeader("Content-Type", "application/pdf; name=LibreDeuda-" + tarjetaCredito.numero() + ".pdf");
			return archivo;
		}

		return null;
	}

	public static RespuestaMB descargaAdjunto(ContextoMB contexto) {
		RespuestaMB respuesta = new RespuestaMB();
		try {
			String idDoc = contexto.parametros.string("idDoc");
			if (idDoc.isEmpty()) {
				return RespuestaMB.parametrosIncorrectos();
			}

			idDoc = "{" + idDoc + "}";

			ApiResponseMB response = RestArchivo.digitalizacionGetArchivo(contexto, idDoc);
			respuesta.add("claseDocumental", response.get("claseDocumental"));
			respuesta.add("propiedades", response.get("propiedades"));
			respuesta.add("bytesDocumento", response.get("bytesDocumento"));
			return respuesta;
		} catch (Exception e) {
			return RespuestaMB.error();
		}
	}

	public static byte[] comprobanteDebinRecurrente(ContextoMB contexto, String id) {
		String template = id.split("_")[0];
		Map<String, String> parametros = contexto.sesion().comprobante(id);
		contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");

		if (template.equals("debinRecurrente")) {
			String idDebin = id.split("_")[1];

			//TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
			Date fechaDesde = new Date("01/01/1900");
			Date currentDate = new Date(System.currentTimeMillis());

			Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaRecurrencias(contexto, fechaDesde, currentDate));

			Objeto objRecurrencia = new Objeto();
			for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
				if(item.string("id").equals(idDebin)){
					objRecurrencia = item;
				}
			}
			//TODO: FIN BUSQUEDA

			String documento = objRecurrencia.string("vendedor.cuit").substring(2,10).replaceFirst("^0+", "");
			String nombreVendedor = "";
			String aliasComprador = "";
			String nombreComprador = "";

			Futuro<ApiResponseMB> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contexto, documento));
			Objeto finalObjRecurrencia = objRecurrencia;
			Futuro<ApiResponseMB> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contexto, finalObjRecurrencia.string("comprador.cbu")));

			if(!responsePersona.get().hayError()){
				aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
				nombreComprador = responseCoelsa.get().string("nombreTitular");
			}

			if(!responsePersona.get().hayError()){
				for (Objeto itemPersona : responsePersona.get().objetos()) {
					if (itemPersona.string("cuil").equals(objRecurrencia.string("vendedor.cuit")))
						nombreVendedor = itemPersona.string("apellidoYNombre");
				}
			}

			if (responseLista.get().hayError()) {
				throw new RuntimeException();
			}

			parametros = new HashMap<>();
			parametros.put("ID", idDebin);
			parametros.put("VENDEDOR_CUIT", objRecurrencia.string("vendedor.cuit"));
			parametros.put("VENDEDOR_NOMBRE", nombreVendedor);
			parametros.put("COMPRADOR_CBU", objRecurrencia.string("comprador.cbu"));
			parametros.put("COMPRADOR_ALIAS", aliasComprador);
			parametros.put("COMPRADOR_CUIT", objRecurrencia.string("comprador.cuit"));
			parametros.put("COMPRADOR_NOMBRE", RestCatalogo.banco(objRecurrencia.string("comprador.cbu").substring(0,3)).toUpperCase());
		}

		return Pdf.generar(template, parametros);
	}

	public static byte[] comprobanteDebinProgramado(ContextoMB contexto, String id) {
		String template = id.split("_")[0];
		Map<String, String> parametros = contexto.sesion().comprobante(id);
		contexto.setHeader("Content-Type", "application/pdf; name=comprobante.pdf");

		//if (template.equals("debinProgramado")) {
			String idDebin = id.split("_")[1];

			//TODO: ACA busqueda de la recurrecia a aceptar/rechazar.
			Date fechaDesde = new Date("01/01/1900");
			Date currentDate = new Date(System.currentTimeMillis());

			Futuro<ApiResponseMB> responseLista = new Futuro<>(() -> RestDebin.consultaDebinesProgramados(contexto, fechaDesde, currentDate));
			//Futuro<ApiResponseMB> detalle = new Futuro<>(() -> RestDebin.detalleDebin(contexto, idDebin));

			Objeto objDebinProgramado = new Objeto();
			for (Objeto item : responseLista.get().objetos("result.recurrencia")) {
				if(item.string("id").equals(idDebin)){
					objDebinProgramado = item;
				}
			}
			//TODO: FIN BUSQUEDA

			String documento = objDebinProgramado.string("vendedor.cuit").substring(2,10).replaceFirst("^0+", "");
			String nombreVendedor = ""; //detalle.get().string("vendedor.cliente.nombreCompleto").trim()
			String aliasComprador = "";
			String nombreComprador = "";

			Futuro<ApiResponseMB> responsePersona = new Futuro<>(() -> RestPersona.consultarPersonaCuitPadron(contexto, documento));
			Objeto finalObjRecurrencia = objDebinProgramado;
			Futuro<ApiResponseMB> responseCoelsa = new Futuro<>(() -> CuentasService.cuentaCoelsa(contexto, finalObjRecurrencia.string("comprador.cbu")));

			if(!responsePersona.get().hayError()){
				aliasComprador = responseCoelsa.get().string("aliasValorOriginal");
				nombreComprador = responseCoelsa.get().string("nombreTitular");
			}

			if(!responsePersona.get().hayError()){
				for (Objeto itemPersona : responsePersona.get().objetos()) {
					if (itemPersona.string("cuil").equals(objDebinProgramado.string("vendedor.cuit")))
						nombreVendedor = itemPersona.string("apellidoYNombre");
				}
			}

			nombreVendedor = RestDebin.nombreVendedor(contexto, objDebinProgramado.string("vendedor.cuit"));

			if (responseLista.get().hayError()) {
				throw new RuntimeException();
			}

			String simboloMoneda = objDebinProgramado.string("debin.moneda").equals("840") ? "USD" : "$";
			String importeConMoneda = simboloMoneda.concat(" ").concat(Formateador.importe(objDebinProgramado.bigDecimal("debin.importe")));
			parametros = new HashMap<>();
			parametros.put("ID", idDebin);
			parametros.put("VENDEDOR_CUIT", objDebinProgramado.string("vendedor.cuit"));
			parametros.put("VENDEDOR_NOMBRE", nombreVendedor.toUpperCase());
			parametros.put("MONTO_CUOTA", importeConMoneda);
			parametros.put("CUOTAS", objDebinProgramado.string("debin.limite_cuotas"));
			parametros.put("COMPRADOR_CBU", objDebinProgramado.string("comprador.cbu"));
			parametros.put("COMPRADOR_ALIAS", aliasComprador);
			parametros.put("COMPRADOR_CUIT", objDebinProgramado.string("comprador.cuit"));
			parametros.put("COMPRADOR_NOMBRE", objDebinProgramado.string("comprador.nombre").toUpperCase());
		//}

		return Pdf.generar(template, parametros);
	}
}
