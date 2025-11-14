package ar.com.hipotecario.canal.officebanking;

import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import javax.print.DocFlavor;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Resource;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCambioComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCondicionComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumTipoPersonaComexOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoOrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;
import com.azure.core.credential.TokenCredential;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import fr.opensagres.poi.xwpf.converter.pdf.PdfConverter;
import fr.opensagres.poi.xwpf.converter.pdf.PdfOptions;
import org.apache.commons.lang3.arch.Processor;
import org.apache.pdfbox.pdmodel.interactive.action.PDActionGoTo;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;


public class OBComex extends ModuloOB{
	private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");	
	private static final Character identificacion_rectificacion = 'B';
	
	static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);

	public static Object cuentas(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		ServicioOPComexOB servicio = new ServicioOPComexOB(contexto);
		List<Objeto> cuentasFiltrados = new ArrayList<Objeto>();
				
		try {
			List<OrdenPagoComexOB> lstOrdenes = servicio.listarCuentas(sesion.empresaOB).tryGet();
			if(lstOrdenes.size() == 0) {
				return respuesta("SIN_CUENTAS");
			}
				
			Set<String> filtrarCuentas = new HashSet<>();

			cuentasFiltrados = lstOrdenes.stream()
					.filter(p -> filtrarCuentas.add(p.cuentaOrigen))
					.map(p -> new Objeto().set("cuenta", p.cuentaOrigen))
					.collect(Collectors.toList());
		}catch(Exception ex) {
			LogOB.evento(contexto, "CuentasOrdenesDePago", "ERROR: " + ex.getMessage());
			return respuesta("ERROR");
		}
		
		return respuesta("datos", cuentasFiltrados);
	}
	
	public static Object categorias(ContextoOB contexto) {
		ServicioCategoriaComexOB servicio = new ServicioCategoriaComexOB(contexto);
		List<CategoriaComexOB> lstCategorias = servicio.findAll().tryGet();
		lstCategorias = lstCategorias.stream().filter(categoria-> categoria.estado).toList();
		
		if(empty(lstCategorias)) {
			return respuesta("ERROR");
		}
		
		Objeto respuesta = respuesta("0");
		for (CategoriaComexOB c : lstCategorias) {
			Objeto categoria = respuesta.add("categorias");
			categoria.set("id", c.id);
			categoria.set("descripcion", StringUtil.reemplazarCaracteresCodificacion(c.descripcion));
			categoria.set("texto", StringUtil.reemplazarCaracteresCodificacion(c.texto.descripcion));
		}

		return respuesta;
	}
	
	public static Object conceptos(ContextoOB contexto) {
		Integer idCategoria = contexto.parametros.integer("idCategoria");
		Objeto respuesta = respuesta("0");
	
		ServicioConceptoComexOB servicio = new ServicioConceptoComexOB(contexto);
		ServicioCategoriaComexOB servicio_categoria = new ServicioCategoriaComexOB(contexto);
		
		CategoriaComexOB categoria = servicio_categoria.find(idCategoria).tryGet();
		List<ConceptoComexOB> lstConceptos = servicio.findByCategoria(categoria).tryGet();
		lstConceptos = lstConceptos.stream().filter(concepto->concepto.estado).toList();
				
		if(empty(lstConceptos)) {
			return respuesta("ERROR");
		}
				
		for (ConceptoComexOB c : lstConceptos) {
			Objeto concepto = respuesta.add("conceptos");
			concepto.set("id", c.id);
			concepto.set("codigo", c.codigo);
			concepto.set("descripcion", StringUtil.reemplazarCaracteresCodificacion(c.descripcion));
						
			if(empty(c.moneda)) {
				concepto.set("moneda", null);	
			}else {
				Objeto mon = new Objeto();
				mon.set("codigo", c.moneda.codigoCobis);
				mon.set("simbolo", c.moneda.simbolo);
				mon.set("descripcion", c.moneda.descripcion);
				concepto.set("moneda", mon);				
			}
			concepto.set("items", empty(c.item) ? null : StringUtil.reemplazarCaracteresCodificacion(c.item.descripcion));
		}
	
		return respuesta;		
	}

	protected static Object historialOrdenesDePago(ContextoOB contexto) {
		Boolean previsualizacion = contexto.parametros.bool("previsualizacion", false);
		String cuenta = contexto.parametros.string("cuenta", null);
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
		
		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
		Objeto respuesta = new Objeto();
		
		try {
			List<OrdenPagoComexOB> ordenes = servicioOPComexOB.filtrarOrdenesPagosHistorial(contexto.sesion().empresaOB, cuenta, fechaDesde, fechaHasta, previsualizacion).get();
						
			for(OrdenPagoComexOB o : ordenes) {
				Objeto datos = new Objeto();

				Objeto estado = new Objeto();
				datos.set("idBandeja", o.id);
				datos.set("fechaCreacion", o.fechaCreacion.toLocalDate().toString());
				boolean esBandeja = o.estadoBandeja.id.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo())||o.estadoBandeja.id.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo());
				estado.set("descripcion",esBandeja?o.estadoBandeja.descripcion:o.estado.descripcion);
				estado.set("id",esBandeja?o.estadoBandeja.id:o.estado.id);
				datos.set("estado",estado);


				datos.set("razonSocial", o.razonSocial);
				datos.set("numeroTRR", o.numeroTRR);
				datos.set("concepto_codigo", o.concepto.codigo);
				datos.set("rectificacion", o.rectificacion != null ? o.rectificacion.toString() : null);
				Objeto mon = new Objeto();
				mon.set("id", o.moneda.id);
				mon.set("descripcion", o.moneda.descripcion);
				datos.set("moneda", mon);
				datos.set("monto", o.monto);
				datos.set("cuenta", o.cuentaOrigen);
			
				respuesta.add(datos);
			}
		}catch(Exception ex) {
			LogOB.evento(contexto, "HistorialOrdenesDePago", "ERROR: " + ex.getMessage());
			return respuesta("ERROR");
		}
		
		return respuesta("datos", respuesta);
	}
	
	protected static Object ValidarRelacionPorcentual(ContextoOB contexto) {

        BigDecimal montoA = contexto.parametros.bigDecimal("montoA");
        BigDecimal montoB = contexto.parametros.bigDecimal("montoB");
        BigDecimal constante = BigDecimal.valueOf(0.2);
        
		Objeto respuesta = new Objeto();
		 
		BigDecimal total = montoA.add(montoB);
		BigDecimal maximoFraccionario = total.multiply(constante);
		
		Objeto datos = new Objeto();
		datos.set("valido", montoB.compareTo(maximoFraccionario)<=0);
		datos.set("total", total);
		datos.set("maximoFraccionario", maximoFraccionario);
						
		respuesta.add(datos);
				
		return respuesta("datos", respuesta);
	}
	
	public static Object detalle(ContextoOB contexto) {
		Integer idOrdenPago = contexto.parametros.integer("idOrdenPago");

		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
		ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
		OrdenPagoComexOB ordenPago = servicioOPComexOB.find(idOrdenPago).get();

		if(empty(ordenPago)) {
			return respuesta("DATOS_INVALIDOS");
		}

		Objeto orden = new Objeto();

		try {
			orden.set("id", ordenPago.id);
			orden.set("fechaCreacion", ordenPago.fechaCreacion.toString());
			orden.set("nroTRR", ordenPago.numeroTRR);
			orden.set("identificacion_rectificacion", !ordenPago.rectificacion.equals('R') && !ordenPago.rectificacion.equals('C'));
			orden.set("rectificacion", ordenPago.rectificacion.toString());
			Objeto mon = new Objeto();
			mon.set("id", ordenPago.simboloMonedaExt.id);
			mon.set("descripcion", ordenPago.moneda.descripcion);
			orden.set("moneda", mon);

			orden.set("montoLiquidar", ordenPago.monto);
			orden.set("categoria", ordenPago.categoria.descripcion);
			orden.set("idCategoria", ordenPago.categoria.id);
			orden.set("codigoConcepto", ordenPago.concepto.codigo);
			orden.set("descripcionConcepto", ordenPago.concepto.descripcion);
			orden.set("idConcepto", ordenPago.concepto.id);
			orden.set("itemsConcepto", ordenPago.concepto.item.descripcion);
			orden.set("originante", ordenPago.razonSocial);
			orden.set("cuenta", ordenPago.cuentaOrigen);
			orden.set("persona",empty(ordenPago.persona)?null:ordenPago.persona.toString().replaceAll("_"," "));
			orden.set("relacion",ordenPago.relacion);
			orden.set("creadoPor", ordenPago.usuario.nombre + " " + ordenPago.usuario.apellido);
			boolean esBandeja = ordenPago.estadoBandeja.id.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo())||ordenPago.estadoBandeja.id.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo());
			Objeto estado = new Objeto();
			estado.set("descripcion",esBandeja?ordenPago.estadoBandeja.descripcion:ordenPago.estado.descripcion);
			estado.set("id",esBandeja?ordenPago.estadoBandeja.id:ordenPago.estado.id);
			orden.set("estado",estado);
			orden.set("cuitcuil",ordenPago.cuitCuil);
			orden.set("cambio",ordenPago.cambio.toString().replaceAll("_"," "));
			orden.set("condicion",ordenPago.condicion.toString().replaceAll("_"," "));
			
			List<ArchivosComexOB> archivos = obtenerArchivosBD(ordenPago.id);
			Objeto lstArchivos = new Objeto();
			for(ArchivosComexOB archivo : archivos) {
				Objeto ar = new Objeto();
				ar.set("nombreArchivo", archivo.nombreArchivo);
				ar.set("url", archivo.url);
				ar.set("idArchivo",archivo.id);
				lstArchivos.add(ar);
			}
			orden.set("archivos", lstArchivos);

			BandejaOB bandeja = servicioBandeja.find(ordenPago.id).get();
			orden.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
		}catch(Exception ex) {
			LogOB.evento(contexto, "DetalleOrdenesDePago", "ERROR: " + ex.getMessage());
			return respuesta("ERROR");
		}

		return respuesta("datos", orden);
	}

	public static Objeto eliminarArchivo(ContextoOB contexto){
		int idArchivo = contexto.parametros.integer("idArchivo");

		ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
		ArchivosComexOB archivo = servicioArchivosComexOB.find(idArchivo).get();

		String connectionString = contexto.config.string("ob_azure_blob_st_url");
		String containerName = contexto.config.string("ob_azure_blob_st_container");
		String rutaComexMasterFiles = contexto.config.string("cx_ruta_master_files");
		AzureBlobStorageManager az = new AzureBlobStorageManager(contexto,connectionString,containerName);
		try{
			az.deleteBlob(contexto,rutaComexMasterFiles + archivo.url);
		}catch (Exception e){
			return respuesta("ERROR","Error al eliminar el BLOB");
		}

		servicioArchivosComexOB.delete(archivo);
		return respuesta("0");

	}

	public static Objeto eliminarArchivo(int idArchivo){
		ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
		ArchivosComexOB archivo = servicioArchivosComexOB.find(idArchivo).get();

		String connectionString = contexto.config.string("ob_azure_blob_st_url");
		String containerName = contexto.config.string("ob_azure_blob_st_container");
		String rutaComexMasterFiles = contexto.config.string("cx_ruta_master_files");
		AzureBlobStorageManager az = new AzureBlobStorageManager(contexto,connectionString,containerName);
		try{
			az.deleteBlob(contexto,rutaComexMasterFiles + archivo.url);
		}catch (Exception e){
			return respuesta("ERROR","Error al eliminar el BLOB");
		}

		servicioArchivosComexOB.delete(archivo);
		return respuesta("0");

	}
	
	protected static Object CargarOrdenDePago(ContextoOB contexto) {

		   String cuentaOrigen = contexto.parametros.string("cuentaOrigen");
		   BigDecimal monto = contexto.parametros.bigDecimal("monto");
		   BigDecimal montoMonedaExt = contexto.parametros.bigDecimal("montoMonedaExt");
	       int categoria = contexto.parametros.integer("categoria");
	       int concepto = contexto.parametros.integer("concepto");
	       String razonSocial = contexto.parametros.string("razonSocial");
	       String numeroTRR = contexto.parametros.string("numeroTRR");
	       String nroCuentaCreditoPesos = contexto.parametros.string("nroCuentaCreditoPesos");
	       String nroCuentaCredMonedaExt = contexto.parametros.string("nroCuentaCredMonedaExt");
	       int idMonedaExtranjera = contexto.parametros.integer("idMonedaExtranjera");
		   String cuitcuil = contexto.parametros.string("cuitcuil");
		   String condicion = contexto.parametros.string("condicion");
		   String cambio = contexto.parametros.string("cambio");
		   boolean relacion = contexto.parametros.bool("relacion");
		   String codigoPersona = contexto.parametros.string("persona");


	        ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
			ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
	        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
	        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
	        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
	        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
	        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
	        ServicioCategoriaComexOB servicioCategoriaComexOB = new ServicioCategoriaComexOB(contexto);
	        ServicioConceptoComexOB servicioConceptoComexOB = new ServicioConceptoComexOB(contexto);
	        ServicioEstadoComexOB servicioEstadoComexOB = new ServicioEstadoComexOB(contexto);
		    ServicioHistorialOPComex servicioHistorial = new ServicioHistorialOPComex(contexto);
	        SesionOB sesion = contexto.sesion();
			String pathComexBandeja="";
			String pathComexMasterFiles="";
			String pathRelativo="";
		    OrdenPagoComexOB ultimoIdCarga;
		    String nuevoId="001";
			String connectionString = contexto.config.string("ob_azure_blob_st_url");
			String containerName = contexto.config.string("ob_azure_blob_st_container");

			EnumCondicionComexOB enumCondicion = EnumCondicionComexOB.getByCodigo(condicion);
			EnumCambioComexOB enumCambio = EnumCambioComexOB.getByCodigo(cambio);
			EnumTipoPersonaComexOB enumPersona = EnumTipoPersonaComexOB.getByCodigo(codigoPersona);

	        // Validar Cuenta (se deja comentado hasta integrar)

			Objeto cuentaCreditoPesos = OBCuentas.cuenta(contexto, nroCuentaCreditoPesos);
		    Objeto cuentaCreditoExt = OBCuentas.cuenta(contexto, nroCuentaCredMonedaExt);

	        /*if (empty(cuentaCreditoPesos) || cuentaCreditoPesos == null) {
	            return respuesta("CUENTA_PESOS_CREDITO_INVALIDA");
	        }
			if (empty(cuentaCreditoExt) || cuentaCreditoExt == null) {
				return respuesta("CUENTA_PESOS_CREDITO_INVALIDA");
			}*/


	        TipoProductoFirmaOB producto = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.TRANSFERENCIAS.getCodigo()).get();

	        /*Creo la OP en la tabla OPComex en BD*/

	        CategoriaComexOB categoriaComex = servicioCategoriaComexOB.find(categoria).tryGet();
	        ConceptoComexOB conceptoComex = servicioConceptoComexOB.findById(concepto).tryGet();
	        MonedaOB peso = servicioMoneda.find(80).get(); 
	        MonedaOB dolar = servicioMoneda.find(idMonedaExtranjera).tryGet(); 
	        EstadoOPComexOB estado = servicioEstadoComexOB.find(1).tryGet();

			ultimoIdCarga=servicioOPComexOB.buscarPorTRR(numeroTRR).tryGet();
			if(ultimoIdCarga!=null && ultimoIdCarga.url!=null){
				int numeroStr =  (Integer.parseInt(
						ultimoIdCarga.url.substring
								(ultimoIdCarga.url.length() - 4, ultimoIdCarga.url.length() - 1))+1);
				nuevoId = String.format("%03d", numeroStr);
			}

		pathRelativo =
				sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/"
		;
		pathComexBandeja = contexto.config.string
				("cx_ruta_en_bandeja")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";

		pathComexMasterFiles = contexto.config.string
				("cx_ruta_master_files")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";
		//Servicio de Azure y subida de archivo
		try {
			Collection<Part> fileParts = contexto.request.raw().getParts();
			AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
			int i = 0;
			for (Part filePart : fileParts) {
				if (filePart.getName().contains("archivo")) {
					String fileName = filePart.getSubmittedFileName();
					int dotIndex = fileName.lastIndexOf('.');
					if (fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName()).toList().contains(fileName)){
						String name = fileName.substring(0, dotIndex);
						String extension = fileName.substring(dotIndex);
						i++;
						fileName = name + " ("+i+")"+extension;
					}
					if (fileName.matches(".*[!@#$%^].*")) {
						if(fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(fileName.replaceAll("[!@#$%^]", ""))){
							String name = fileName.substring(0, dotIndex);
							String extension = fileName.substring(dotIndex);
							fileName = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
							i++;
						}else{
							fileName = fileName.replaceAll("[!@#$%^]","");
						}
					}



					String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");
					String[] extensionesValidas = {"pdf", "jpg", "png", "docx", "xls", "xlsx"};
					if (!Arrays.asList(extensionesValidas).contains(tipoExtension[1].toLowerCase())) {
						return respuesta("EXTENSION_ARCHIVO_INVALIDA");
					}

					InputStream inputStream = filePart.getInputStream();
					byte[] archivoBytes = inputStream.readAllBytes();
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexBandeja, fileName, "1");
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexMasterFiles, fileName, "1");
				}};


		} catch (Exception e) {
			LogOB.evento(contexto,"cargarArchivoCX", "Error al subir archivo");
			return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta bandeja.");
		}
		
		// se carga la op en la BD
		OrdenPagoComexOB op = servicioOPComexOB.crear(
				contexto,
				cuentaOrigen,
				monto,
				categoriaComex,
				conceptoComex,
				razonSocial,
				numeroTRR,
				'n',
				false,
				nroCuentaCreditoPesos,
				nroCuentaCredMonedaExt,
				pathRelativo,
				dolar,
				montoMonedaExt,
				dolar,
				estado,
				LocalDateTime.now(),
				LocalDateTime.now(),
				enumCambio,
				enumCondicion,
				cuitcuil,
				enumPersona,
				relacion
		).get();

		if(op==null){
			LogOB.evento(contexto,"cargarOPCX", "Error al persistir la op");
			try{
				AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
				az.deleteBlob(contexto, pathComexBandeja);
			}catch (Exception e){
				LogOB.evento(contexto,"eliminarCarpetaCX", "Error al borrar la carpeta");
			}
			return respuesta("ERROR", "descripcion", "Error al persistir la OP");
		}

		try {
			int i = 0;
			Collection<Part> fileParts = contexto.request.raw().getParts();
			for (Part filePart : fileParts) {
				if (filePart.getName().contains("archivo")) {
					String fileName = filePart.getSubmittedFileName();
					int dotIndex = fileName.lastIndexOf('.');
					if (fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName()).toList().contains(fileName)){
						i++;
						String name = fileName.substring(0, dotIndex);
						String extension = fileName.substring(dotIndex);
						fileName = name + " ("+i+")"+extension;
					}
					if (fileName.matches(".*[!@#$%^].*")) {
						if(fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(fileName.replaceAll("[!@#$%^]", ""))){
							String name = fileName.substring(0, dotIndex);
							String extension = fileName.substring(dotIndex);
							fileName = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
							i++;
						}else{
							fileName = fileName.replaceAll("[!@#$%^]","");
						}
					}
					String pathArchivos =
							sesion.empresaOB.emp_codigo.toString()
							+"-"
							+obtenerFecha()
							+"-"
							+numeroTRR.toString()
							+"-"
							+nuevoId
							+"/"
							+fileName;
					;

					servicioArchivosComexOB.crear(contexto, op, LocalDateTime.now(), fileName, pathArchivos ).get();
				}
			}
		} catch (Exception e) {
			LogOB.evento(contexto,"registrarArchivoCX", "Error al registrar archivo");
			return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta bandeja.");
		}

		BandejaOB bandeja = servicioBandeja.find(op.id).get();
		EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
		EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
		servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
		servicioHistorial.crear(op, accionCrear, empresaUsuario, estado, estado);
		contexto.parametros.set("idOrdenPago", op.id);

			return respuesta("detalle", detalle(contexto));
	}

	public static Objeto editarOrdenPago(ContextoOB contexto){
		int id = contexto.parametros.integer("idOP");
		int categoria = contexto.parametros.integer("categoria");
		int concepto = contexto.parametros.integer("concepto");
		String razonSocial = contexto.parametros.string("razonSocial");
		String numeroTRR = contexto.parametros.string("numeroTRR");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		BigDecimal montoMonedaExt = contexto.parametros.bigDecimal("montoMonedaExt");
		String connectionString = contexto.config.string("ob_azure_blob_st_url");
		String containerName = contexto.config.string("ob_azure_blob_st_container");
		boolean relacion = contexto.parametros.bool("relacion");
		String codigoPersona = contexto.parametros.string("persona");
		Objeto objetoIds = contexto.parametros.objeto("idArchivos",null);
		String condicion = contexto.parametros.string("condicion");
		String cambio = contexto.parametros.string("cambio");
		String nroCuentaCreditoPesos = contexto.parametros.string("nroCuentaCreditoPesos");
		String nroCuentaCredMonedaExt = contexto.parametros.string("nroCuentaCredMonedaExt");
		int idMonedaExtranjera = contexto.parametros.integer("idMonedaExtranjera");
		String cuitcuil = contexto.parametros.string("cuitcuil", null);
		SesionOB sesion = contexto.sesion();

		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
		ServicioCategoriaComexOB servicioCategoriaComexOB = new ServicioCategoriaComexOB(contexto);
		ServicioConceptoComexOB servicioConceptoComexOB = new ServicioConceptoComexOB(contexto);
		ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
		EnumCondicionComexOB enumCondicion = EnumCondicionComexOB.getByCodigo(condicion);
		EnumCambioComexOB enumCambio = EnumCambioComexOB.getByCodigo(cambio);
		CategoriaComexOB categoriaComex = servicioCategoriaComexOB.find(categoria).tryGet();
		ConceptoComexOB conceptoComex = servicioConceptoComexOB.findById(concepto).tryGet();
		EnumTipoPersonaComexOB enumPersona = EnumTipoPersonaComexOB.getByCodigo(codigoPersona);
		OrdenPagoComexOB ordenPago = servicioOPComexOB.find(id).get();
		MonedaOB dolar = servicioMoneda.find(idMonedaExtranjera).tryGet();

		boolean cambiaTRR = !ordenPago.numeroTRR.equals(numeroTRR);
		if (objetoIds!=null){
			List<Object> idArchivos = objetoIds.toList();
			idArchivos.forEach(idArchivo->{
				int idArch = Integer.valueOf(idArchivo.toString());
				eliminarArchivo(idArch);
			});
		}



		String nuevoId = "001";
		if (cambiaTRR){
			OrdenPagoComexOB ultimoIdCarga=servicioOPComexOB.buscarPorTRR(numeroTRR).tryGet();
			if(ultimoIdCarga!=null && ultimoIdCarga.url!=null){
				int numeroStr =  (Integer.parseInt(
						ultimoIdCarga.url.substring
								(ultimoIdCarga.url.length() - 4, ultimoIdCarga.url.length() - 1))+1);
				nuevoId = String.format("%03d", numeroStr);
			}
			String nuevoPath = sesion.empresaOB.emp_codigo.toString()
					+"-"
					+obtenerFecha()
					+"-"
					+numeroTRR.toString()
					+"-"
					+nuevoId
					+"/";
			List<ArchivosComexOB> archivos = obtenerArchivosBD(id);

			ordenPago.url = nuevoPath;
			for (ArchivosComexOB archivo : archivos) {

				String nuevaURL = archivo.url.replaceFirst("^[^/]*/", nuevoPath);
				AzureBlobStorageManager.renameBlob(contexto,contexto.config.string("ob_azure_blob_st_container"),contexto.config.string("cx_ruta_en_bandeja")+archivo.url,contexto.config.string("cx_ruta_en_bandeja")+ nuevaURL);
				AzureBlobStorageManager.renameBlob(contexto,contexto.config.string("ob_azure_blob_st_container"),contexto.config.string("cx_ruta_master_files")+archivo.url,contexto.config.string("cx_ruta_master_files")+ nuevaURL);
				archivo.url = nuevaURL;
				servicioArchivosComexOB.update(archivo);
			}
		}

		String pathComexBandeja = contexto.config.string
				("cx_ruta_en_bandeja")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";

		String pathComexMasterFiles = contexto.config.string
				("cx_ruta_master_files")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";


		try {
			Collection<Part> fileParts = contexto.request.raw().getParts();
			AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
			int i = 0;
			for (Part filePart : fileParts) {
				if (filePart.getName().contains("archivo")) {
					String fileName = filePart.getSubmittedFileName();
					int dotIndex = fileName.lastIndexOf('.');
					if (fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName()).toList().contains(fileName)){
						String name = fileName.substring(0, dotIndex);
						String extension = fileName.substring(dotIndex);
						i++;
						fileName = name + " ("+i+")"+extension;
					}
					if (fileName.matches(".*[!@#$%^].*")) {
						if(fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(fileName.replaceAll("[!@#$%^]", ""))){
							String name = fileName.substring(0, dotIndex);
							String extension = fileName.substring(dotIndex);
							fileName = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
							i++;
						}else{
							fileName = fileName.replaceAll("[!@#$%^]","");
						}
					}

					String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");
					String[] extensionesValidas = {"pdf", "jpg", "png", "docx", "xls", "xlsx"};
					if (!Arrays.asList(extensionesValidas).contains(tipoExtension[1].toLowerCase())) {
						return respuesta("EXTENSION_ARCHIVO_INVALIDA");
					}

					String pathRelativo = sesion.empresaOB.emp_codigo.toString()
							+"-"
							+obtenerFecha()
							+"-"
							+numeroTRR.toString()
							+"-"
							+nuevoId
							+"/"
							+fileName;

					InputStream inputStream = filePart.getInputStream();
					byte[] archivoBytes = inputStream.readAllBytes();
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexBandeja, fileName, "1");
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexMasterFiles, fileName, "1");
					servicioArchivosComexOB.crear(contexto, ordenPago, LocalDateTime.now(), fileName, pathRelativo).get();
				}
			}

		} catch (Exception e) {
			LogOB.evento(contexto,"cargarArchivoCX", "Error al subir archivo");
			return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta bandeja.");
		}

		ordenPago.categoria = categoriaComex;
		ordenPago.concepto = conceptoComex;
		ordenPago.razonSocial = razonSocial;
		ordenPago.numeroTRR = numeroTRR;
		ordenPago.monto = monto;
		ordenPago.montoMonedaExt = montoMonedaExt;
		ordenPago.relacion = relacion;
		ordenPago.persona = enumPersona;
		ordenPago.cambio = enumCambio;
		ordenPago.condicion = enumCondicion;
		ordenPago.nroCuentaCreditoPesos = nroCuentaCreditoPesos;
		ordenPago.nroCuentaCredMonedaExt = nroCuentaCredMonedaExt;
		ordenPago.moneda = dolar;
		ordenPago.cuitCuil= cuitcuil;
		servicioOPComexOB.update(ordenPago);


		return respuesta("0");
	}

	public static Objeto rectificarOrdenPago(ContextoOB contexto){
		int id = contexto.parametros.integer("idOP");
		int categoria = contexto.parametros.integer("categoria");
		int concepto = contexto.parametros.integer("concepto");
		String razonSocial = contexto.parametros.string("razonSocial");
		String numeroTRR = contexto.parametros.string("numeroTRR");
		BigDecimal monto = contexto.parametros.bigDecimal("monto");
		BigDecimal montoMonedaExt = contexto.parametros.bigDecimal("montoMonedaExt");
		String connectionString = contexto.config.string("ob_azure_blob_st_url");
		String containerName = contexto.config.string("ob_azure_blob_st_container");
		String nroCuentaCreditoPesos = contexto.parametros.string("nroCuentaCreditoPesos");
		String nroCuentaCredMonedaExt = contexto.parametros.string("nroCuentaCredMonedaExt");
		int idMonedaExtranjera = contexto.parametros.integer("idMonedaExtranjera");
		boolean relacion = contexto.parametros.bool("relacion");
		Objeto objetoIds = contexto.parametros.objeto("idArchivos",null);
		String codigoPersona = contexto.parametros.string("persona");
		String condicion = contexto.parametros.string("condicion");
		String cambio = contexto.parametros.string("cambio");
		SesionOB sesion = contexto.sesion();
		String nuevoId="001";
		OrdenPagoComexOB ultimoIdCarga;

		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
		ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
		ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
		ServicioCategoriaComexOB servicioCategoriaComexOB = new ServicioCategoriaComexOB(contexto);
		ServicioConceptoComexOB servicioConceptoComexOB = new ServicioConceptoComexOB(contexto);
		ServicioEstadoComexOB servicioEstadoComexOB = new ServicioEstadoComexOB(contexto);
		ServicioHistorialOPComex servicioHistorial = new ServicioHistorialOPComex(contexto);
		MonedaOB dolar = servicioMoneda.find(idMonedaExtranjera).tryGet();

		EnumCondicionComexOB enumCondicion = EnumCondicionComexOB.getByCodigo(condicion);
		EnumCambioComexOB enumCambio = EnumCambioComexOB.getByCodigo(cambio);
		CategoriaComexOB categoriaComex = servicioCategoriaComexOB.find(categoria).tryGet();
		ConceptoComexOB conceptoComex = servicioConceptoComexOB.findById(concepto).tryGet();
		EnumTipoPersonaComexOB enumPersona = EnumTipoPersonaComexOB.getByCodigo(codigoPersona);
		List<Integer> idArchivosNoCopiar = new ArrayList<>();
		if (objetoIds!=null){
			 idArchivosNoCopiar = objetoIds.toList().stream().map(objeto->Integer.valueOf(objeto.toString())).toList();

		}

		ultimoIdCarga=servicioOPComexOB.buscarPorTRR(numeroTRR).tryGet();
		OrdenPagoComexOB ordenARectificar = servicioOPComexOB.find(id).get();
		if(ultimoIdCarga!=null && ultimoIdCarga.url!=null){
			int numeroStr =  (Integer.parseInt(
					ultimoIdCarga.url.substring
							(ultimoIdCarga.url.length() - 4, ultimoIdCarga.url.length() - 1))+1);
			nuevoId = String.format("%03d", numeroStr);
		}

		ultimoIdCarga=servicioOPComexOB.find(id).tryGet();
		if(ultimoIdCarga!=null && ultimoIdCarga.url!=null){
			int numeroStr =  (Integer.parseInt(
					ultimoIdCarga.url.substring
							(ultimoIdCarga.url.length() - 4, ultimoIdCarga.url.length() - 1))+1);
			nuevoId = String.format("%03d", numeroStr);
		}
		String pathComexBandeja = contexto.config.string
				("cx_ruta_en_bandeja")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";

		String pathComexMasterFiles = contexto.config.string
				("cx_ruta_master_files")
				+sesion.empresaOB.emp_codigo.toString()
				+"-"
				+obtenerFecha()
				+"-"
				+numeroTRR.toString()
				+"-"
				+nuevoId
				+"/";

		Collection<Part> fileParts = null;
		try {
			  fileParts= contexto.request.raw().getParts();
			AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
			int i = 0;
			for (Part filePart : fileParts) {
				if (filePart.getName().contains("archivo")) {
					String fileName = filePart.getSubmittedFileName();
					int dotIndex = fileName.lastIndexOf('.');
					if (fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName()).toList().contains(fileName)){
						String name = fileName.substring(0, dotIndex);
						String extension = fileName.substring(dotIndex);
						i++;
						fileName = name + " ("+i+")"+extension;
					}
					if (fileName.matches(".*[!@#$%^].*")) {
						if(fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(fileName.replaceAll("[!@#$%^]", ""))){
							String name = fileName.substring(0, dotIndex);
							String extension = fileName.substring(dotIndex);
							fileName = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
							i++;
						}else{
							fileName = fileName.replaceAll("[!@#$%^]","");
						}
					}

					String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");
					if (!tipoExtension[1].equalsIgnoreCase("pdf")
							&& !tipoExtension[1].equalsIgnoreCase("jpg")
							&& !tipoExtension[1].equalsIgnoreCase("xls")
							&& !tipoExtension[1].equalsIgnoreCase("xlsx")
							&& !tipoExtension[1].equalsIgnoreCase("doc")
							&& !tipoExtension[1].equalsIgnoreCase("docx")) {
						return respuesta("EXTENSION_ARCHIVO_INVALIDA");
					}

					InputStream inputStream = filePart.getInputStream();
					byte[] archivoBytes = inputStream.readAllBytes();
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexBandeja, fileName, "1");
					az.uploadArchivoToAzure(contexto, archivoBytes, pathComexMasterFiles, fileName, "1");
				}
			}

		} catch (Exception e) {
			LogOB.evento(contexto,"cargarArchivoCX", "Error al subir archivo");
			return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta bandeja.");
		}
		String pathArchivos =
				sesion.empresaOB.emp_codigo.toString()
						+"-"
						+obtenerFecha()
						+"-"
						+numeroTRR
						+"-"
						+nuevoId
						+"/";
		EstadoOPComexOB estadoInicial = ultimoIdCarga.estado;
		ultimoIdCarga.categoria = categoriaComex;
		ultimoIdCarga.concepto = conceptoComex;
		ultimoIdCarga.razonSocial = razonSocial;
		ultimoIdCarga.numeroTRR = numeroTRR;
		ultimoIdCarga.monto = monto;
		ultimoIdCarga.montoMonedaExt = montoMonedaExt;
		ultimoIdCarga.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		ultimoIdCarga.estado = servicioEstadoComexOB.find(EnumEstadoOrdenPagoComexOB.EN_BANDEJA.getCodigo()).get();
		ultimoIdCarga.relacion = relacion;
		ultimoIdCarga.persona = enumPersona;
		ultimoIdCarga.cambio = enumCambio;
		ultimoIdCarga.condicion = enumCondicion;
		ultimoIdCarga.nroCuentaCreditoPesos = nroCuentaCreditoPesos;
		ultimoIdCarga.nroCuentaCredMonedaExt = nroCuentaCredMonedaExt;
		ultimoIdCarga.moneda = dolar;

		try{
			ultimoIdCarga.rectificacion = definirRectificacion(ultimoIdCarga.rectificacion);
		}catch (Exception e){
			return respuesta("ERROR","DATOS",e.getMessage());
		}

		ultimoIdCarga = servicioOPComexOB.crear(contexto,ultimoIdCarga.cuentaOrigen,ultimoIdCarga.monto,ultimoIdCarga.categoria,ultimoIdCarga.concepto,ultimoIdCarga.razonSocial,ultimoIdCarga.numeroTRR,ultimoIdCarga.rectificacion,ultimoIdCarga.bienesYservicio,ultimoIdCarga.nroCuentaCreditoPesos,ultimoIdCarga.nroCuentaCredMonedaExt,pathArchivos,ultimoIdCarga.moneda,ultimoIdCarga.montoMonedaExt,ultimoIdCarga.simboloMonedaExt,ultimoIdCarga.estado,ultimoIdCarga.fechaCreacion,ultimoIdCarga.fechaModificacion,ultimoIdCarga.cambio,ultimoIdCarga.condicion,ultimoIdCarga.cuitCuil,ultimoIdCarga.persona,ultimoIdCarga.relacion).get();

		ordenARectificar.rectificacion = 'R';
		servicioOPComexOB.update(ordenARectificar);
		if(ultimoIdCarga==null){
			LogOB.evento(contexto,"cargarOPCX", "Error al persistir la op");
			try{
				AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
				az.deleteBlob(contexto, pathComexBandeja);
			}catch (Exception e){
				LogOB.evento(contexto,"eliminarCarpetaCX", "Error al borrar la carpeta");
			}
			return respuesta("ERROR", "descripcion", "Error al persistir la OP");
		}


		List<ArchivosComexOB> archivosPrevios = servicioArchivosComexOB.findByOP(ordenARectificar).get();
		AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
		int i =0;
		for (ArchivosComexOB archivo : archivosPrevios) {
			if (!idArchivosNoCopiar.contains(archivo.id)){
				String nombreArchivo = archivo.nombreArchivo;
				int dotIndex = nombreArchivo.lastIndexOf('.');
				if(fileParts.stream().filter(part->part.getName().contains("archivo")).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(nombreArchivo.replaceAll("[!@#$%^]", ""))){
					String name = nombreArchivo.substring(0, dotIndex);
					String extension = nombreArchivo.substring(dotIndex);
					nombreArchivo = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
					i++;
				}
				az.copyBlob(contexto,contexto.config.string
						("cx_ruta_master_files")+archivo.url,contexto.config.string
						("cx_ruta_master_files")+pathArchivos+nombreArchivo);
				az.copyBlob(contexto,contexto.config.string
						("cx_ruta_master_files")+archivo.url,contexto.config.string
						("cx_ruta_en_bandeja")+pathArchivos+nombreArchivo);
				servicioArchivosComexOB.crear(contexto,ultimoIdCarga,archivo.fechaCreacion,nombreArchivo,pathArchivos+nombreArchivo);
			}

		}


		try {


			for (Part filePart : fileParts) {
				if (filePart.getName().contains("archivo")) {
					String fileName = filePart.getSubmittedFileName();
					int dotIndex = fileName.lastIndexOf('.');
					if (fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName()).toList().contains(fileName)){
						String name = fileName.substring(0, dotIndex);
						String extension = fileName.substring(dotIndex);
						i++;
						fileName = name + " ("+i+")"+extension;
					}
					if (fileName.matches(".*[!@#$%^].*")) {
						if(fileParts.stream().filter(part->part.getName().contains("archivo")).filter(part->part!=filePart).map(part->part.getSubmittedFileName().replaceAll("[!@#$%^]", "")).toList().contains(fileName.replaceAll("[!@#$%^]", ""))){
							String name = fileName.substring(0, dotIndex);
							String extension = fileName.substring(dotIndex);
							fileName = name.replaceAll("[!@#$%^]","") + " ("+i+")"+extension;
							i++;
						}else{
							fileName = fileName.replaceAll("[!@#$%^]","");
						}
					}

					servicioArchivosComexOB.crear(contexto, ultimoIdCarga, LocalDateTime.now(), fileName, pathArchivos+fileName).get();
				}
			}
		} catch (Exception e) {
			LogOB.evento(contexto,"registrarArchivoCX", "Error al registrar archivo");
			return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta bandeja.");
		}

		EstadoOPComexOB estado = servicioEstadoComexOB.find(1).tryGet();

		new ServicioHistorialRectificacionComexOB(contexto).crear(Long.valueOf(id),Long.valueOf(ultimoIdCarga.id),ultimoIdCarga.rectificacion);

		Objeto respuesta = new Objeto();
		respuesta.set("id",ultimoIdCarga.id);
		contexto.parametros.set("idOrdenPago", ultimoIdCarga.id);
		return respuesta("detalle",detalle(contexto));
	}

	private static Character definirRectificacion(Character rectificacion) throws Exception {
		switch (rectificacion){
			case 'n': return 'a';
			case 'a': return 'b';
			case 'b': return 'c';
			default: throw new Exception("Ya alcanzo el maximo de rectificaciones");
		}
	}

	public static void calcularIdentificacionRectificacion(OrdenPagoComexOB ordenPago) {
		try {
			ServicioOPComexOB servicioOrdenPago = new ServicioOPComexOB(contexto);
			Character nuevaIdentificacion = ordenPago.rectificacion == null ? 'A' : (char) (ordenPago.rectificacion + 1);
			ordenPago.rectificacion = nuevaIdentificacion; 
			servicioOrdenPago.update(ordenPago);
			cambioIdentificacionRectificacion(contexto, ordenPago, nuevaIdentificacion);	
		}catch(Exception ex) {
			LogOB.evento(contexto, "calcularIdentificacionRectificacion", "ERROR: " + ex.getMessage());
		}
	}
	
	public static void cambioIdentificacionRectificacion(ContextoOB contexto, OrdenPagoComexOB ordenPago, Character nuevaIdentificacion) {
		SesionOB sesion = contexto.sesion();
		
		try {
			ServicioHistorialOPComex servicioHistorialOPComex = new ServicioHistorialOPComex(contexto);
			ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
			EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);
			EstadoOPComexOB estadoInicialOPComex = ordenPago.estado;
			EstadoOPComexOB estadoOPComex = ordenPago.estado;
			AccionesOB accionFirmar;
			
			if(ordenPago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) { 
				accionFirmar = servicioAcciones.find(EnumAccionesOB.FIRMAR.getCodigo()).get();	
			}else {
				accionFirmar = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
			}
			
			servicioHistorialOPComex.cambiaEstado(ordenPago, accionFirmar, empresaUsuario, estadoInicialOPComex, estadoOPComex, nuevaIdentificacion).get();	
		}catch(Exception ex) {
			LogOB.evento(contexto, "cambioIdentificacionRectificacion", "ERROR: " + ex.getMessage());
		}		
	}

	public static Object cargarPdfDetalle(ContextoOB contexto, int idOrdenPago, String connectionString, String containerName, String pathDestino) {
		try {
			contexto.parametros.set("idOrdenPago", idOrdenPago);
			Object resultado = detalle(contexto);
			LocalDateTime fechaCreacionOP = LocalDateTime.now();
			if (resultado == null || !(resultado instanceof Objeto)) {
				LogOB.evento(contexto, "cargarPdfDetalle", "ERROR: No se pudo obtener los datos de la orden de pago.");
				return null;
			}
			Objeto orden = (Objeto) resultado;
			StringBuilder texto = new StringBuilder();
			texto.append("DETALLE DE OP:  ").append("\n").append("\n").append("\n");
			texto.append("ID: ").append(orden.get("datos.id")).append("\n");
			texto.append("CUIT Empresa: ").append(contexto.sesion().empresaOB.cuit.toString()).append("\n");
			texto.append("IdCobis: ").append(contexto.sesion().empresaOB.idCobis).append("\n");
			texto.append("Fecha de Creación: ").append(orden.get("datos.fechaCreacion")).append("\n");
			texto.append("Nro TRR: ").append(orden.get("datos.nroTRR")).append("\n");
			texto.append("Rectificación: ").append(orden.get("datos.rectificacion")).append("\n");
			texto.append("Monto a Liquidar: ").append(orden.get("datos.montoLiquidar")).append("\n");
			texto.append("Moneda: ").append(orden.get("datos.moneda.descripcion")).append("\n");
			texto.append("Categoría: ").append(orden.get("datos.categoria")).append("\n");
			texto.append("Código de Concepto: ").append(orden.get("datos.codigoConcepto")).append("\n");
			texto.append("CUIT/CUIL: ").append(orden.get("datos.cuitcuil")).append("\n");
			texto.append("Condicion: ").append(orden.get("datos.condicion")).append("\n");
			texto.append("CierreDeCambio: ").append(orden.get("datos.cambio")).append("\n");
			texto.append("Originante: ").append(orden.get("datos.originante")).append("\n");
			texto.append("Firmado el: ").append(fechaCreacionOP.toString()).append("\n");
			texto.append("Cuenta: ").append(orden.get("datos.cuenta")).append("\n");
			texto.append("Creado Por: ").append(orden.get("datos.creadoPor")).append("\n");
			texto.append("Firmas: ");

			List<Object> firmas = ((Objeto) orden.get("datos.firmas.datos")).toList();
			firmas.forEach(firma->{
				texto.append(((Map)firma).get("nombreFirmante")).append("\n");
			});


			String rutaPlantilla = "officebanking/comex-detalle-op.docx";

			try (InputStream fis = Resource.stream(rutaPlantilla) ;
				 XWPFDocument documento = new XWPFDocument(fis)) {
				XWPFParagraph paragraph = documento.createParagraph();
				XWPFRun run = paragraph.createRun();
				run.setText(texto.toString());

				PdfOptions options = PdfOptions.create();
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				PdfConverter.getInstance().convert(documento, out, options);
				try {
					byte[] pdfBytes = out.toByteArray();
					AzureBlobStorageManager.subirArchivoContenedor(contexto, pathDestino, orden, pdfBytes);

				} catch (Exception e) {

					System.err.println("ERROR: " + e.getMessage());
				}
			}

		} catch (Exception ex) {
			LogOB.evento(contexto, "cargarPdfDetalle", "ERROR: " + ex.getMessage());
			System.err.println("ERROR: " + ex.getMessage());
		}
		return respuesta("EXITO");
	}



	public static List<ArchivosComexOB> obtenerArchivosBD(int idOp) {
		ServicioArchivosComexOB servicioArchivosComexOB = new ServicioArchivosComexOB(contexto);
		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);
		OrdenPagoComexOB op = servicioOPComexOB.find(idOp).tryGet();

		List<ArchivosComexOB> archivos = servicioArchivosComexOB.findByOP(op).get();
		return archivos;
	}

	public static Object obtenerFecha(){
		return LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
	}

	public static Object  validarTTRYConcepto (ContextoOB contexto){
		String numeroTRR = contexto.parametros.string("numeroTRR");
		String codigoConcepto = contexto.parametros.string("codigoConcepto");
		ServicioOPComexOB servicioOPComexOB = new ServicioOPComexOB(contexto);

		OrdenPagoComexOB buscarOrdenPago = servicioOPComexOB.buscarPorTRR(numeroTRR).tryGet();
		Objeto response = new Objeto();

		if(buscarOrdenPago != null && !buscarOrdenPago.estado.id.toString().equals('3') && buscarOrdenPago.concepto.codigo.toString().equals(codigoConcepto)){
			response.set("idBandeja", buscarOrdenPago.id);
		}

		return respuesta("datos", response);
	}

	public static Object descargarArchivo(ContextoOB contexto){
		String url = contexto.parametros.string("url");

		String connectionString = contexto.config.string("ob_azure_blob_st_url");
		String containerName = contexto.config.string("ob_azure_blob_st_container");
		AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
		Blob archivo;
		try {
			BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.COMERCIO_EXTERIOR,url);
			archivo = new SerialBlob(blobClient.downloadContent().toBytes());
		} catch (Exception e) {
			return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese nombre.");
		}
		Objeto respuesta = new Objeto();
		try {
			return archivo.getBinaryStream().readAllBytes();
		}catch (Exception e){
			e.toString();
		}


		return respuesta("0","DATOS",respuesta);
	}
}
