package ar.com.hipotecario.canal.officebanking;

import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.blobToString;
import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.getPage;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.ServletException;
import javax.servlet.http.Part;


import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.servicio.api.empresas.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import org.apache.commons.lang3.CharSet;

import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoComprobantesDTO;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.*;

import org.apache.commons.lang3.EnumUtils;
import org.jetbrains.annotations.NotNull;
import org.mozilla.universalchardet.UniversalDetector;

import com.azure.storage.blob.BlobClient;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.dto.ErrorArchivoOB;
import ar.com.hipotecario.canal.officebanking.dto.ErrorGenericoOB;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoPapDTO;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.ArchivoBeneficiarioDTO;
import ar.com.hipotecario.canal.officebanking.dto.pagosMasivos.OrdenDePagoDTO;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumBancosOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMonedasOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ServicioOrdenPagoFechaEjecucionOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pap.ArchivoPAPItem;
import ar.com.hipotecario.canal.officebanking.jpa.dto.pap.ArchivoPBItem;
import ar.com.hipotecario.canal.officebanking.jpa.ob.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBancoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioControlDualAutorizanteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioControlDualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioFirmasProductosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioHistorialPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoAProveedoresConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.*;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BancoOB;
import com.azure.storage.blob.BlobClient;
import com.opencsv.CSVWriter;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import org.apache.commons.lang3.EnumUtils;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.jetbrains.annotations.NotNull;

import javax.servlet.ServletException;
import javax.servlet.http.Part;
import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static ar.com.hipotecario.canal.officebanking.OBManejoArchivos.*;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.*;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualAutorizanteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresConfigOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BancoOB;


public class OBPagoProveedores extends ModuloOB {


    public static Object consultaCovenios(ContextoOB contexto) {
        List<Objeto> convenios = new ArrayList<>();
        SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
        if (respuestaConvenio.subconvenios!=null){
            List<SubConveniosOB.SubConvenio> listaSubconvenios = respuestaConvenio.subconvenios.subconvenio;
            Objeto datos = (Objeto) OBCuentas.cuentas(contexto);
            Objeto cuentas = (Objeto) datos.get("datos.cuentas");
            convenios.addAll(listaSubconvenios.stream().map(conv -> {
                        Objeto data = new Objeto();
                        String numeroCuenta = conv.strCtaFormatoPagos.substring(4, 22).replace("-", "");
                        Optional<Objeto> cuenta = cuentas.objetos().stream().filter(c -> c.get("numeroProducto").toString().equals(numeroCuenta)).findFirst();
                            data.set("idConvenio", conv.nroConv);
                            data.set("subConv", conv.nroSubConv);
                            data.set("numeroProducto", numeroCuenta);
                            data.set("saldoGirar", cuenta.isPresent()?cuenta.get().get("saldoGirar"):"");
                            data.set("moneda", cuenta.isPresent()?cuenta.get().get("moneda"):0);
                            data.set("nroAdh", conv.nroAdh);
                        return data;
                    })
                    .toList());
        }
        if (contexto.sesion().empresaOB.cuit.toString().equals("30624957942")&&contexto.sesion().usuarioOB.numeroDocumento.toString().equals("40854690")){
            convenios = convenios.stream().filter(convenio->convenio.get("idConvenio").toString().equals("1102")).toList();
        }
        return respuesta("datos", convenios);
    }


    public static String getEncodingTest1(ContextoOB contexto) {
    	Part filePart;
        InputStream inputStream;
        byte[] archivoBytes = null ;
		
			try {
				filePart = contexto.request.raw().getPart("archivo");
				inputStream = filePart.getInputStream();
				archivoBytes = inputStream.readAllBytes();
			} catch (IOException | ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		
        // Detectar BOM para UTF-8
        if (archivoBytes.length >= 3 &&
            archivoBytes[0] == (byte) 0xEF &&
            archivoBytes[1] == (byte) 0xBB &&
            archivoBytes[2] == (byte) 0xBF) {
            System.out.println(StandardCharsets.UTF_8.name());

            return StandardCharsets.UTF_8.name();
        }
        // Asumir ANSI (Cp1252) si no es UTF-8
        System.out.println(Charset.forName("Cp1252"));
        return Charset.forName("Cp1252").toString();
    }
    

    private static String getEncoding(byte[] archivoBytes) {
        byte[] buf = new byte[4096];
        
        try (InputStream inputStream = new BufferedInputStream(new ByteArrayInputStream(archivoBytes))) {
            
            // Inicializar el UniversalDetector
            UniversalDetector detector = new UniversalDetector(null);

            // Leer datos del InputStream y pasarlos al detector
            int nread;
            while ((nread = inputStream.read(buf)) > 0 && !detector.isDone()) {
                detector.handleData(buf, 0, nread);
            }

            // Señalar el fin de los datos al detector
            detector.dataEnd();

            // Obtener la codificación detectada
            String encoding = detector.getDetectedCharset();
            if (encoding == null ) {
            	encoding = StandardCharsets.UTF_8.name();
            }
            // Mostrar la codificación detectada
            System.out.println("Codificación detectada: " + encoding);
            
            return encoding;
        } catch (Exception e) {
            e.printStackTrace();
           return StandardCharsets.UTF_8.name();
        }
    }

    public static String getEncodingTest(ContextoOB contexto) throws Exception {
        byte[] buf = new byte[4096];
        Part filePart = contexto.request.raw().getPart("archivo");
        InputStream inputStream = filePart.getInputStream();

        // Inicializar el UniversalDetector
        UniversalDetector detector = new UniversalDetector(null);

        // Leer datos del InputStream y pasarlos al detector
        int nread;
        while ((nread = inputStream.read(buf)) > 0 && !detector.isDone()) {
            detector.handleData(buf, 0, nread);
        }

        // Señalar el fin de los datos al detector
        detector.dataEnd();

        // Obtener la codificación detectada
        String encoding = detector.getDetectedCharset();
        inputStream.close();
        
        if (encoding == null ) {
        	encoding = StandardCharsets.UTF_8.name();
        }
        
        // Mostrar la codificación detectada
        System.out.println("Codificación detectada: " + encoding);
        return encoding;
	}


    private static String getRegistroArch(byte[] archivoBytes, String encoding) throws IOException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(archivoBytes), encoding))) {
            return reader.readLine();
        }
    }

    private static void processBody(String body) {
        // Implementar la validación del body
        if (body == null || body.isEmpty()) {
            throw new IllegalArgumentException("Body is missing or empty");
        }
        // Validaciones específicas del body
        // Ejemplo: validación de contenido
    }
    
    
    
    
    public static Object cargarPap(ContextoOB contexto) throws SQLException, IOException, ServletException {
    	//parametros
        Integer convenio = contexto.parametros.integer("convenio");
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        String nroAdherente = contexto.parametros.string("adherente");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        String transferencias = contexto.parametros.string("transferencia");
        String cheques = contexto.parametros.string("cheque");
        // parametros archivo cliente
        Part filePart = contexto.request.raw().getPart("archivo");

        //inicializa
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioHistorialPagoAProveedoresOB servicioHistorial = new ServicioHistorialPagoAProveedoresOB(contexto);
        ServicioFirmasProductosOB firmas = new ServicioFirmasProductosOB(contexto);
        ServicioOrdenPagoFechaEjecucionOB servicioOPFechaEjecucion = new ServicioOrdenPagoFechaEjecucionOB(contexto);
        ServicioControlDualOB servicioControlDualOB = new ServicioControlDualOB(contexto);
        
        SesionOB sesion = contexto.sesion();
      
        
        //Cargar y leer archivo
        String nombreArchivo = filePart.getSubmittedFileName();
        InputStream inputStream = filePart.getInputStream();
       
        byte[] archivoBytes = inputStream.readAllBytes();
        String contenido = new String(archivoBytes, Charset.forName("Cp1252"));

        archivoBytes = contenido.getBytes(Charset.forName("Cp1252"));
        Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(new ByteArrayInputStream(archivoBytes));

        ControlDualOB controlDual = servicioControlDualOB.findByEmpresa(sesion.empresaOB).tryGet();

 		// valido extension archivo
        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");
        if (!tipoExtension[1].equalsIgnoreCase("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }
        // Obtengo el contenido del archivo.
        String contenidoArchivo = blobToString(archivo);
        String cabecera = contenidoArchivo.split("\n")[0];
        String convenioCabecera = cabecera.substring(40, 47);


        if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
            return respuesta("CONVENIO_INVALIDO");
        }




        contenido = new String(archivoBytes, Charset.forName("Cp1252"));
        System.out.println("contenido = " + contenido);
        System.out.println("contenido.length() = " + contenido.length());
        contenido = contenido.replace("ñ", "n")
                .replace("Ñ", "N")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ü", "u")
                .replace("Ü", "U")
                .replace("ö", "o")
                .replace("ò","o")
                .replace("Ò","O")
                .replace("à", "a")
                .replace("À", "A")
                .replace("è", "e")
                .replace("È", "E")
                .replace("ì", "i")
                .replace("Ì", "I")
                .replace("ù", "u")
                .replace("Ù", "U")
                .replace("'", " ")
                .replace("’", " ")
                .replace("´"," ")
                .replace("º"," ")
                .replace("Ö", "O");
        contenido = contenido.replace("°"," ").replace("?"," ");
        String[][] replacements = {
                {"Ã±", "ñ"},
                {"Ã‘", "Ñ"},
                {"Ã¡", "á"},
                {"Ã©", "é"},
                {"Ã­", "í"},
                {"Ã³", "ó"},
                {"Ãº", "ú"},
                {"ÃÁ", "A"},
                {"ÃÉ", "E"},
                {"ÃÍ", "I"},
                {"ÃÓ", "O"},
                {"ÃÚ", "U"},
                {"Ã¼", "u"},
                {"Ã¶", "o"},
                {"Ã–", "O"},
                {"Ã?", "I"},
                {"Â°", " "},
                {"Â", ""},
                {"\\u00A0", " "},
                {"Ã²", "o"},
                {"Ã’", "O"},
                {"Ã ", "A"},
                {"Ã¨", "e"},
                {"Ã‰", "E"},
                {"Ã¬", "i"},
                {"ÃÌ", "I"},
                {"Ã¹", "u"},
                {"ÃÙ", "U"},
                {"A‰", "E"},
                {"â€™", " "},
                {"Â´"," "},
                {"¥", "N"},
                {"Ã", "A"}
        };

        String[] lineas = contenido.split("\n");
        List<String> updatedLines = new ArrayList<>();

        for (int i = 0; i < lineas.length; i++) {
            String linea = lineas[i];
            boolean reemplazoHecho = false;
            int lastReplacementPos = -1;

            for (String[] pair : replacements) {
                String target = pair[0];
                String replacement = pair[1];
                Pattern pattern = Pattern.compile(Pattern.quote(target));
                Matcher matcher = pattern.matcher(linea);
                StringBuffer sb = new StringBuffer();

                while (matcher.find()) {
                    reemplazoHecho = true;
                    lastReplacementPos = matcher.start() + replacement.length();
                    matcher.appendReplacement(sb, replacement);
                }
                matcher.appendTail(sb);
                linea = sb.toString();
            }

            if (linea.length() < 1601 && reemplazoHecho && lastReplacementPos != -1) {
                if (lastReplacementPos < linea.length()) {
                    linea = linea.substring(0, lastReplacementPos) + " " + linea.substring(lastReplacementPos);
                } else {
                    linea += " ";
                }
            }

            updatedLines.add(linea);
            System.out.println("Línea " + (i + 1) + " longitud: " + linea.length());
        }

        contenido = String.join("\n", updatedLines);
        contenido = contenido.replace("\u00A0", " ");




        System.out.println("contenido = " + contenido);
        System.out.println("contenido.length() = " + contenido.length());
        archivoBytes = contenido.getBytes(Charset.forName("Cp1252"));

        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }

        TipoProductoFirmaOB producto = servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.PAGO_PROVEEDORES.getCodigo()).get();
        ArchivoPAPItem detalleItemTotal = ArchivoPAPItem.readPAPItemTotalesFromFile(archivoBytes, getEncoding(archivoBytes));

        Integer cantidadRegistros = detalleItemTotal.getTotalRegistros();
        BigDecimal importe = BigDecimal.valueOf(detalleItemTotal.getImporte() / 100.00);
        
        PagoAProveedoresOB pago = servicioPagoAProveedoresOB.crear(contexto, cuenta,
                importe, nombreArchivo, archivo, cantidadRegistros, convenio, producto, subconvenio, nroAdherente, new BigDecimal(transferencias), new BigDecimal(cheques), controlDual != null && controlDual.control_dual).get();
        String pathMaster = contexto.config.string("pap_ruta_master_files");

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        
        try {
            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, reemplazarTXT(pago.nombreArchivo), String.valueOf(pago.id));
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        
        String pathDestinoBandeja = contexto.config.string("pap_ruta_en_bandeja");
        try {
            az.copyBlob(contexto, pathMaster + reemplazarTXT(pago.nombreArchivo), pathDestinoBandeja + reemplazarTXT(pago.nombreArchivo));
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
        }

        BandejaOB bandeja = servicioBandeja.find(pago.id).get();

        if (new BigDecimal(transferencias).compareTo(BigDecimal.ZERO) > 0) {
            firmas.crear(bandeja.id, 4, new BigDecimal(transferencias));
        }
        if (new BigDecimal(cheques).compareTo(BigDecimal.ZERO) > 0) {
            firmas.crear(bandeja.id, 12, new BigDecimal(cheques));
        }
            
        EstadoBandejaOB estadoInicialBandeja = null;
        if(controlDual != null && controlDual.control_dual) {
        	estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_AUTORIZACION.getCodigo()).get();
        }else {
        	estadoInicialBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        }        
        
        AccionesOB accionCrear = servicioAcciones.find(EnumAccionesOB.CREAR.getCodigo()).get();
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, sesion.empresaOB, sesion.usuarioOB);

        servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionCrear, estadoInicialBandeja, estadoInicialBandeja);
        servicioHistorial.crear(pago, accionCrear, empresaUsuario);

        contexto.parametros.set("idSolicitudPago", pago.id);

        List<ArchivoPapDTO.Body> listDetalleItem = ArchivoPapDTO.getBody(archivoBytes, getEncoding(archivoBytes));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        LocalDate dateAux = LocalDate.parse(listDetalleItem.get(0).fechaEjecucion, formatter);
        for (ArchivoPapDTO.Body item : listDetalleItem) {
            LocalDate dateAux1 = LocalDate.parse(item.fechaEjecucion, formatter);
            if (dateAux1.isAfter(dateAux)) {
                dateAux = dateAux1;
            }
        }

        servicioOPFechaEjecucion.crear(pago.nombreArchivo, dateAux, pago.id).get();

        Objeto pagoDetalle = new Objeto();
        pagoDetalle.set("id", pago.id);
        pagoDetalle.set("monto", pago.monto);
        pagoDetalle.set("cantidadRegistros", pago.cantidadRegistros);
        pagoDetalle.set("cuenta", pago.cuentaOrigen);
        pagoDetalle.set("tipo", pago.tipoProducto);
        pagoDetalle.set("moneda", pago.moneda.simbolo);
        pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);

        if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo())
            pagoDetalle.set("estado", pago.estado.descripcion);
        else
            pagoDetalle.set("estado", pago.estadoBandeja.descripcion);

        pagoDetalle.set("estadoOperacion", pago.estado.descripcion);

        return respuesta("detalle", pagoDetalle);
    }

    public static Object precargaPapPB(ContextoOB contexto) {
    	Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        int subconvenio = contexto.parametros.integer("subconvenio");
        Objeto dato = new Objeto();
        Objeto respuesta = new Objeto();
        List<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();
        ServicioPagoBeneficiariosOB servicioPagoBeneficiariosOB = new ServicioPagoBeneficiariosOB(contexto);

        try {
            Part filePart = contexto.request.raw().getPart("archivo");
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);

            int registrosPorPagina = 60;
            InputStream inputStream = filePart.getInputStream();
            Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(inputStream);
            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equals("txt")) {
                return errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
            }

            Objeto respuestaValidarNombre = OBValidacionesPB.validarNombreArchivo(filePart.getSubmittedFileName());
            if (!respuestaValidarNombre.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarNombre);
            }
            String contenidoArchivo = blobToString(archivo);
            String cabecera = null;
            try{
                 cabecera = contenidoArchivo.substring(0, 1600);
            }catch (Exception e){
                int firstIndex = contenidoArchivo.indexOf("PB");
                int secondIndex = contenidoArchivo.indexOf("PB", firstIndex + 2);
                cabecera =  contenidoArchivo.substring(0, secondIndex);
            }

            String convenioCabecera = cabecera.substring(40, 47);
            List<PagoBeneficiariosOB> pagoBeneficiarios = servicioPagoBeneficiariosOB.buscarPorArchivo(filePart.getSubmittedFileName()).get();
            // validar convenio que viene en el combo contra el del archivo
            if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
                return new ErrorArchivoOB().setErroresArchivo("Convenio inválido.", "El convenio elegido debe coincidir con el del archivo.", 0, "Convenio");
            }

            if (pagoBeneficiarios.size() != 0) {
                return errores.setErroresArchivo("Nombre archivo invalido invalido", "Ya se ha ingresado un archivo con este nombre", null, null);
            }

            Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
            if (empty(cuentaDebito) || cuentaDebito == null) {
                return new ErrorGenericoOB().setErrores("Cuenta inválida.", "La cuenta seleccionada no existe.");
            }

            SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
            SubConveniosOB.SubConvenio subConvenioOBSeleccionado = respuestaConvenio.subconvenios.subconvenio.stream().filter(subConvenio -> subConvenio.nroSubConv == subconvenio && subConvenio.nroConv==convenio).findFirst().orElse(null);
            if (Optional.ofNullable(subConvenioOBSeleccionado).isEmpty()) {
                return new ErrorArchivoOB().setErroresArchivo("Subconvenio inválido.", "El subconvenio elegido debe coincidir con el del archivo.", 0, "Subconvenio");
            }

            ArchivoBeneficiarioDTO.Header nuevaCabecera = ArchivoBeneficiarioDTO.getHeader(contenidoArchivo.trim());
            Objeto respuestaValidarHeader = OBValidacionesPB.validarHeader(nuevaCabecera, subConvenioOBSeleccionado);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }
            List<ArchivoBeneficiarioDTO.Body> listDetalleItem = null;
            try {
                listDetalleItem = ArchivoBeneficiarioDTO.getBody(archivo);
            } catch (Exception e) {
                return new ErrorArchivoOB().setErroresArchivo("Cuerpo del archivo invalido", e.getMessage(), null, null);
            }

            List<ArchivoBeneficiarioDTO.Body> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
            try {
                for (ArchivoBeneficiarioDTO.Body item : listaPaginada) {
                        Objeto detail = new Objeto();
                        detail.set("identificacion", item.registroId);
                        detail.set("nombreBeneficiario", item.nombreBeneficiario);
                        detail.set("cuit", item.cuit);
                        detail.set("email", item.email);
                        detail.set("cbu", item.cbu);
                        respuesta.add("registros", detail);
                }

                Integer dataCantReg = ArchivoPBItem.readPAPItemTotalesFromFile(archivo).getTotalRegistros();

                dato.set("cantidadRegistros", listDetalleItem.size());

                dato.set("nombreArchivo", filePart.getSubmittedFileName());
                dato.set("numeroPagina", numeroPagina);
                dato.set("paginasTotales",  listDetalleItem.size() / registrosPorPagina);
                respuesta.add("informacionArchivo", dato);

                ServicioBeneficiariosConfigOB servicioBeneficiariosConfigOB = new ServicioBeneficiariosConfigOB(contexto);
                columnas.addAll(servicioBeneficiariosConfigOB.findAll().get().stream().parallel()
                        .filter(columna -> columna.visible.equals(true))
                        .sorted(Comparator.comparing(PagoBeneficiariosConfigOB::getPosicion))
                        .map(columna -> columna.nombreColumna)
                        .map(nombre -> nombre.replace("Ã³", "ó"))
                        .toList());
                respuesta.add("columnas", columnas);

            } catch (Exception e) {
                throw new Exception(e);
            }
            return respuesta("datos", respuesta);
        } catch (Exception e) {
            return contexto.response(200, respuesta("ERROR", "datos", e.getMessage()));
        }

    }
    
    public static Object cargarPapPB(ContextoOB contexto) throws SQLException, IOException, ServletException {
        Integer convenio = contexto.parametros.integer("convenio");
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioPagoBeneficiariosOB serviciopagoBeneficiarioOB = new ServicioPagoBeneficiariosOB(contexto);

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        String nombreArchivo = filePart.getSubmittedFileName();

        InputStream inputStream = filePart.getInputStream();
        byte[] archivoBytes = inputStream.readAllBytes();
        String contenido = new String(archivoBytes, Charset.forName("Cp1252"));
        contenido = contenido.replace("ñ", "n")
                .replace("Ñ", "N")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ü", "u")
                .replace("Ü", "U")
                .replace("ö", "o")
                .replace("Ö", "O");

        contenido = contenido.replace("Ã±", "ñ")
                .replace("Ã‘", "Ñ")
                .replace("Ã¡", "á")
                .replace("Ã©", "é")
                .replace("Ã­", "í")
                .replace("Ã³", "ó")
                .replace("Ãº", "ú")
                .replace("ÃÁ", "Á")
                .replace("ÃÉ", "É")
                .replace("ÃÍ", "Í")
                .replace("ÃÓ", "Ó")
                .replace("ÃÚ", "Ú")
                .replace("Ã¼","u")
                .replace("Ã¶", "o")
                .replace("Ã–", "O")
                .replace("Ã?", "Í")
                .replace("Â°", " ")
                .replace("Â", "")
                .replace("\u00A0", " ")
                .replace("Ã", "A");

        if (contenido.contains("Â°")) {
            String[] contenidoEnLineas = contenido.split("\n");
            for (int i = 0; i < contenidoEnLineas.length; i++) {
                if (contenidoEnLineas[i].contains("Â°")) {
                    //String lineaModificada = contenidoEnLineas[i].replace("Â°", " ").replace("Ã?", "Í");
                    //  contenidoEnLineas[i] = lineaModificada;
                }
            }
            contenido = String.join("\n", contenidoEnLineas);
        }
        contenido = contenido.replaceAll("°"," ");
        archivoBytes = contenido.getBytes(Charset.forName("Cp1252"));
        Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(new ByteArrayInputStream(archivoBytes));

        // validar archivo
        String[] tipoExtension = filePart.getSubmittedFileName().split("\\.");

        if (!tipoExtension[1].equals("txt")) {
            return respuesta("EXTENSION_ARCHIVO_INVALIDA");
        }

        // Obtengo el contenido del archivo.
        String contenidoArchivo = blobToString(archivo);
        String cabecera = contenidoArchivo.split("\n")[0];
        String convenioCabecera = cabecera.substring(40, 47);

        // validar convenio que viene en el combo contra el del archivo
        if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
            return respuesta("CONVENIO_INVALIDO");
        }

        Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
        if (empty(cuentaDebito) || cuentaDebito == null) {
            return respuesta("CUENTA_DEBITO_INVALIDA");
        }

        ArchivoPBItem detalleItemTotal = ArchivoPBItem.readPAPItemTotalesFromFile(archivo);

        // Toma del archivo cantidad registros
        Integer cantidadRegistros = detalleItemTotal.getTotalRegistros();

        // Agregar Importe
        PagoBeneficiariosOB pago = serviciopagoBeneficiarioOB.crear(contexto, cuenta,
                nombreArchivo, archivo, cantidadRegistros, convenio, subconvenio).get();
        String pathMaster = contexto.config.string("pap_ruta_master_files");

        System.out.println("filePart.getSubmittedFileName() = " + filePart.getSubmittedFileName());
        try {
            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, pago.nombreArchivo, String.valueOf(pago.id));
            ApiEmpresas.validaIntegridadArchivo(contexto, filePart.getSubmittedFileName(), convenio.toString(), subconvenio.toString());

        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        String pathDestinoAprocesar = contexto.config.string("pap_ruta_procesar");
        try {
            az.copyBlob(contexto, pathMaster + pago.nombreArchivo, pathDestinoAprocesar + pago.nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta en bandeja");
        }

        contexto.parametros.set("idSolicitudPago", pago.id);

        Objeto pagoDetalle = new Objeto();
        pagoDetalle.set("id", pago.id);
        pagoDetalle.set("cantidadRegistros", pago.cantidadRegistros);
        pagoDetalle.set("cuenta", pago.cuentaOrigen);
        pagoDetalle.set("tipo", pago.tipoProducto);
        pagoDetalle.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);

        return respuesta("detalle", pagoDetalle);
    }

    public static Object cargarComprobantes(ContextoOB contexto) throws ServletException, IOException {
        Integer convenio = contexto.parametros.integer("convenio");
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        Integer filas = contexto.parametros.integer("filas");
        Part filePart = contexto.request.raw().getPart("archivo");

        ServicioComprobantesPAPOB servicioComprobantesPAPOB = new ServicioComprobantesPAPOB(contexto);

        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        String nombreArchivo = filePart.getSubmittedFileName();
        InputStream inputStream = filePart.getInputStream();
        byte[] archivoBytes = inputStream.readAllBytes();
        String contenido = new String(archivoBytes, Charset.forName("Cp1252"));
        contenido = contenido.replace("ñ", "n")
                .replace("Ñ", "N")
                .replace("á", "a")
                .replace("é", "e")
                .replace("í", "i")
                .replace("ó", "o")
                .replace("ú", "u")
                .replace("Á", "A")
                .replace("É", "E")
                .replace("Í", "I")
                .replace("Ó", "O")
                .replace("Ú", "U")
                .replace("ü", "u")
                .replace("Ü", "U")
                .replace("ö", "o")
                .replace("Ö", "O");

        contenido = contenido.replace("Ã±", "ñ")
                .replace("Ã‘", "Ñ")
                .replace("Ã¡", "á")
                .replace("Ã©", "é")
                .replace("Ã­", "í")
                .replace("Ã³", "ó")
                .replace("Ãº", "ú")
                .replace("ÃÁ", "Á")
                .replace("ÃÉ", "É")
                .replace("ÃÍ", "Í")
                .replace("ÃÓ", "Ó")
                .replace("ÃÚ", "Ú")
                .replace("Ã¼","u")
                .replace("Ã¶", "o")
                .replace("Ã–", "O")
                .replace("Ã?", "Í")
                .replace("Â°", " ")
                .replace("Â", "")
                .replace("\u00A0", " ")
                .replace("Ã", "A");

        contenido = contenido.replaceAll("°"," ");
        archivoBytes = contenido.getBytes(Charset.forName("Cp1252"));

        ComprobantePAPOB comprobante = servicioComprobantesPAPOB.crear(contexto.sesion().empresaOB,contexto.sesion().usuarioOB,
                nombreArchivo, convenio, subconvenio,filas,"ENVIADA").get();
        String pathMaster = contexto.config.string("pap_ruta_master_files");

        try {
            az.uploadArchivoToAzure(contexto, archivoBytes, pathMaster, comprobante.nombreArchivo, String.valueOf(comprobante.id));
            ApiEmpresas.validaIntegridadArchivo(contexto,nombreArchivo,convenio.toString(),subconvenio.toString());
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al cargar el archivo a carpeta master.");
        }
        String pathDestinoAprocesar = contexto.config.string("pap_ruta_procesar");
        try {
            az.copyBlob(contexto, pathMaster + comprobante.nombreArchivo, pathDestinoAprocesar + comprobante.nombreArchivo);
        } catch (Exception e) {
            e.printStackTrace();
            return respuesta("ERROR", "descripcion", "Error al copiar el archivo a carpeta a procesar");
        }

        Objeto detalle = new Objeto();
        detalle.set("id", comprobante.id);

        return respuesta("0","datos", detalle);
    }

    public static Object descargaTemplate(ContextoOB contexto) {
        Integer producto = contexto.parametros.integer("producto");
        byte[] file;

        EnumTipoProductoOB productos = EnumTipoProductoOB.getByCodigo(producto);

        if (productos == null) {
            return respuesta("ERROR", "descripcion", "producto es inválido.");
        }

        try {
            file = OBManejoArchivos.descargarTemplate(contexto, producto);
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se pudo descargar el template");
        }
        return file;
    }

    public static Object precargaComprobanteRetenciones(ContextoOB contexto) {
        Integer convenio = contexto.parametros.integer("convenio");
        int subconvenio = contexto.parametros.integer("subconvenio");
        Objeto dato = new Objeto();
        Objeto respuesta = new Objeto();
        ArrayList<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();

        try {
            Part filePart = contexto.request.raw().getPart("archivo");
            byte[] archivoBytes;
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);
            int registrosPorPagina = 60;

            Objeto respuestaValidarNombre = OBValidacionesComprobantesPAP.validarNombreArchivo(filePart.getSubmittedFileName());
            if (!respuestaValidarNombre.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarNombre);
            }

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equalsIgnoreCase("txt")) {
                return errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
            }
            InputStream inputStream = filePart.getInputStream();
            archivoBytes = inputStream.readAllBytes();
            String cabecera = getRegistroArch(archivoBytes, getEncoding(archivoBytes));
            String convenioCabecera = cabecera.substring(40, 47);
            if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
                return new ErrorArchivoOB().setErroresArchivo("Convenio inválido.", "El convenio elegido debe coincidir con el del archivo.", 0, "Convenio");
            }
            SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
            SubConveniosOB.SubConvenio subConvenioOBSeleccionado = respuestaConvenio.subconvenios.subconvenio.stream().filter(subConvenio -> subConvenio.nroSubConv == subconvenio && subConvenio.nroConv == convenio).findFirst().orElse(null);
            if (Optional.ofNullable(subConvenioOBSeleccionado).isEmpty()) {
                return new ErrorArchivoOB().setErroresArchivo("Subconvenio inválido.", "El subconvenio elegido debe coincidir con el del archivo.", 0, "Subconvenio");
            }
            ArchivoComprobantesDTO.Header nuevaCabecera = ArchivoComprobantesDTO.getHeader(cabecera.trim());
            Objeto respuestaValidarHeader = OBValidacionesComprobantesPAP.validarHeader(nuevaCabecera, subConvenioOBSeleccionado);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }
            List<ArchivoComprobantesDTO.HeaderPage> listHeadersPagina = null;
            try {

                listHeadersPagina = ArchivoComprobantesDTO.getHeaderPages(new String(archivoBytes, Charset.forName("Cp1252")));

            } catch (Exception e) {
                return new ErrorArchivoOB().setErroresArchivo("Cuerpo del archivo invalido", e.getMessage(), null, null);
            }


            int numLinea = 1;
            for (ArchivoComprobantesDTO.HeaderPage item : listHeadersPagina) {
                Objeto resp = OBValidacionesComprobantesPAP.validarHeaderPagina(item, numLinea);
                if (!resp.get("estado").equals("0")) {
                    return contexto.response(200, resp);
                }
                numLinea++;
            }
            List<ArchivoComprobantesDTO.HeaderPage> listaPaginada = getPage(listHeadersPagina, numeroPagina, registrosPorPagina);
            for (ArchivoComprobantesDTO.HeaderPage item : listaPaginada) {
                Objeto detail = new Objeto();
                detail.set("referencia", item.referenciaPago.trim());
                detail.set("retencion", EnumRetencionTipo.fromCodigo(Integer.valueOf(item.tipoCertificado)).getNombre());
                respuesta.add("registros", detail);
            }

            dato.set("cantidadRegistros", listHeadersPagina.size());
            dato.set("nombreArchivo", filePart.getSubmittedFileName());
            dato.set("numeroPagina", numeroPagina);
            dato.set("paginasTotales", (int) Math.ceil(Integer.parseInt(dato.get("cantidadRegistros").toString()) / registrosPorPagina));
            respuesta.add("informacionArchivo", dato);
            columnas.add("Número de orden de pago");
            columnas.add("Tipo de retención");
            respuesta.add("columnas", columnas);


            return respuesta("datos", respuesta);
        } catch (Exception e) {
            return contexto.response(200, respuesta("ERROR", "datos", e.getMessage()));
        }
    }


    
    public static Object precargaPap(ContextoOB contexto) {
    	Integer convenio = contexto.parametros.integer("convenio");
        String cuenta = contexto.parametros.string("cuentaOrigen");
        int subconvenio = contexto.parametros.integer("subconvenio");
        Objeto dato = new Objeto();
        Objeto respuesta = new Objeto();
        List<String> columnas = new ArrayList<>();
        ErrorArchivoOB errores = new ErrorArchivoOB();
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioControlDualAutorizanteOB servicioControlDualAutorizanteOB = new ServicioControlDualAutorizanteOB(contexto);
        
        try {           

            Part filePart = contexto.request.raw().getPart("archivo");
            byte[] archivoBytes;
            
            
            Integer numeroPagina = contexto.parametros.integer("numeroPagina", 1);
            int registrosPorPagina = 60;
            
            // validar nombre y extension de archivo
            Objeto respuestaValidarNombre = OBValidacionesPAP.validarNombreArchivo(filePart.getSubmittedFileName());
            if (!respuestaValidarNombre.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarNombre);
            }

            String[] tipoExtensionTXT = filePart.getSubmittedFileName().split("\\.");
            if (!tipoExtensionTXT[1].equalsIgnoreCase("txt")) {                return errores.setErroresArchivo(
                        "El archivo tiene un formato inválido",
                        "Sólo aceptamos archivos con la extensión .txt.\r\n"
                                + "Por favor generá el archivo correcto desde el template y subilo nuevamente.", null, null);
            }

            InputStream inputStream = filePart.getInputStream();
            archivoBytes = inputStream.readAllBytes();
            
            //Blob archivo = OBManejoArchivos.inputStreamToBlobANSI(inputStream);
//            String contenidoArchivo = blobToString(archivo);
             
            		
            // Procesar y validar el header
            String cabecera = null;
//            try{
//               
                // Leer con el encoding detectado
            	cabecera = getRegistroArch(archivoBytes, getEncoding(archivoBytes));
//            	cabecera = contenidoArchivo.substring(0, 1600);
//            	
//            }catch (Exception e){
//                int firstIndex = contenidoArchivo.indexOf("PO");
//                int secondIndex = contenidoArchivo.indexOf("PO", firstIndex + 2);
//                cabecera =  contenidoArchivo.substring(0, secondIndex);
//            	
//            }

            String convenioCabecera = cabecera.substring(40, 47);
            List<PagoAProveedoresOB> pago = servicioPagoAProveedoresOB.buscarPorArchivo(filePart.getSubmittedFileName()).get();
            // validar convenio que viene en el combo contra el del archivo
            if (convenio == null || convenio != Integer.parseInt(convenioCabecera)) {
                return new ErrorArchivoOB().setErroresArchivo("Convenio inválido.", "El convenio elegido debe coincidir con el del archivo.", 0, "Convenio");
            }
            if (pago.size() != 0) {
                return errores.setErroresArchivo("Nombre archivo invalido invalido", "Ya se ha ingresado un archivo con este nombre", null, null);
            }

            Objeto cuentaDebito = OBCuentas.cuenta(contexto, cuenta);
            if (empty(cuentaDebito) || cuentaDebito == null) {
                return new ErrorGenericoOB().setErrores("Cuenta inválida.", "La cuenta seleccionada no existe.");
            }

            SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
            SubConveniosOB.SubConvenio subConvenioOBSeleccionado = respuestaConvenio.subconvenios.subconvenio.stream().filter(subConvenio -> subConvenio.nroSubConv == subconvenio && subConvenio.nroConv==convenio).findFirst().orElse(null);
            if (Optional.ofNullable(subConvenioOBSeleccionado).isEmpty()) {
                return new ErrorArchivoOB().setErroresArchivo("Subconvenio inválido.", "El subconvenio elegido debe coincidir con el del archivo.", 0, "Subconvenio");
            }

//            ArchivoPapDTO.Header nuevaCabecera = ArchivoPapDTO.getHeader(contenidoArchivo.trim());
            ArchivoPapDTO.Header nuevaCabecera = ArchivoPapDTO.getHeader(cabecera.trim());
            Objeto respuestaValidarHeader = OBValidacionesPAP.validarHeader(nuevaCabecera, subConvenioOBSeleccionado);
            if (!respuestaValidarHeader.get("estado").equals("0")) {
                return contexto.response(200, respuestaValidarHeader);
            }
            LocalDate fechaCreacion = (LocalDate) respuestaValidarHeader.get("fecha");
            List<ArchivoPapDTO.Body> listDetalleItem = null;
            try {
            	
            	listDetalleItem = ArchivoPapDTO.getBody(archivoBytes, getEncoding(archivoBytes));

            } catch (Exception e) {
                return new ErrorArchivoOB().setErroresArchivo("Cuerpo del archivo invalido", e.getMessage(), null, null);
            }

            int numLinea = 1;
            for (ArchivoPapDTO.Body item : listDetalleItem) {
                Objeto resp = OBValidacionesPAP.validarDetalle(item, contexto, subConvenioOBSeleccionado, numLinea, fechaCreacion);
                if (!resp.get("estado").equals("0")) {
                    return contexto.response(200, resp);
                }
                numLinea++;
            }
            //ArchivoPapDTO.trailer = ArchivoPapDTO.getTrailer();

            List<ArchivoPapDTO.Body> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
            BigDecimal totalImporte = BigDecimal.valueOf(0);
            boolean esTransferencia = false;
            try {
                Double cheques = 0.00;
                Double transferencia = 0.00;
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
                for (ArchivoPapDTO.Body item : listaPaginada) {
                    if (!item.registroId.equals("FT")) {
                        Objeto detail = new Objeto();
                        detail.set("fechaEjecucion", LocalDate.parse(item.fechaEjecucion, formatter).toString().replace('-', '/'));
                        double importe = Double.parseDouble(eliminarCerosIzquierda(item.importe)) / 100.00;
                        totalImporte = totalImporte.add(BigDecimal.valueOf(importe));
                        detail.set("importe", importe);
                        detail.set("identificacion", item.referencia);
                        detail.set("razonSocial", item.nombreBeneficiario);
                        detail.set("cuit", item.documento);

                        for (EnumMediosPagoPAPOB mediosPago : EnumMediosPagoPAPOB.values()) {
                            if (mediosPago.getCodigo().equals(item.medioPago)) {
                                detail.set("medioPago", mediosPago.name());
                                break;
                            }
                        }

                        detail.set("email", item.email);
                        detail.set("cbu", item.cbuCredito);
                        detail.set("curazarCheque", item.cruzarCheque);
                        detail.set("clausula", item.clausula);

                        if (item.fechaVencimiento.isBlank()) {
                            detail.set("fechaVencimiento", item.fechaVencimiento);
                        } else {
                            detail.set("fechaVencimiento", LocalDate.parse(item.fechaVencimiento, formatter).toString().replace('-', '/'));
                        }
                        detail.set("tipoTransaccion", mapearTipoOperatoria(item.tipoOperatoria));
                        detail.set("nroCheque", item.nroCheque);

                        if (item.medioPago.equals("009")) {
                            transferencia += Double.parseDouble(item.importe);
                            esTransferencia = true;
                        } else {
                            cheques += Double.parseDouble(item.importe);
                        }
                        respuesta.add("registros", detail);
                    }
                }

                ArchivoPapDTO.Trailer data = ArchivoPapDTO.getTrailer();
                Integer dataCantReg = ArchivoPAPItem.readPAPItemTotalesFromFile(archivoBytes, getEncoding(archivoBytes)).getTotalRegistros();
//                Objeto respuestaValidarTrailer = OBValidacionesPAP.validarFooter(ArchivoPapDTO.trailer, totalImporte, dataCantReg);
//                if (!respuestaValidarTrailer.get("estado").equals("0")) {
//                    return contexto.response(200, respuestaValidarTrailer);
//                }

//                if (BigDecimal.valueOf(Long.valueOf(data.importeTotal)/100).compareTo(totalImporte)!=0){
//                    return errores.setErroresArchivo("Importe incorrecto","La suma de los importes de los detalles tiene que coincidir con el del trailer.",0,null);
//                }
                dato.set("cantidadRegistros", dataCantReg);
                dato.set("importeTotal", Double.parseDouble(data.importeTotal) / 100.00);

                dato.set("nombreArchivo", filePart.getSubmittedFileName());
                dato.set("numeroPagina", numeroPagina);
                dato.set("paginasTotales", (int) Math.ceil(Integer.parseInt(dato.get("cantidadRegistros").toString()) / registrosPorPagina));
                dato.set("cheques", cheques / 100.00);
                dato.set("transferencias", transferencia / 100.00);
                
                List<ControlDualAutorizanteOB> autorizantes = servicioControlDualAutorizanteOB.findAutorizantesActivos(contexto.sesion().empresaOB).get();
                dato.set("autorizante", autorizantes.isEmpty() ? false : true);
                respuesta.add("informacionArchivo", dato);

                //Armo listado de nombres de columnas visibles
                ServicioPagoAProveedoresConfigOB servicioPagoAProveedoresConfigOB = new ServicioPagoAProveedoresConfigOB(contexto);
                columnas.addAll(servicioPagoAProveedoresConfigOB.findAll().get().stream().parallel()
                        .filter(columna -> columna.visible.equals(true))
                        .sorted(Comparator.comparing(PagoAProveedoresConfigOB::getPosicion))
                        .map(columna -> columna.nombreColumna).toList());
                respuesta.add("columnas", columnas);


            } catch (Exception e) {
                throw new Exception(e);
            }
            return respuesta("datos", respuesta);
        } catch (Exception e) {
            return contexto.response(200, respuesta("ERROR", "datos", e.getMessage()));
        }
    }

    public static Object consultaEstados(ContextoOB contexto) {
        ServicioEstadosPagoAProveedoresOB servicioEstadoPagosProveedores = new ServicioEstadosPagoAProveedoresOB(contexto);
        List<EstadosPagosAProveedoresOB> estadosProveedores = servicioEstadoPagosProveedores.findAll().get();

        Objeto datos = new Objeto();
        for (EstadosPagosAProveedoresOB estado : estadosProveedores) {

            if (estado.id != EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()) {
                Objeto est = new Objeto();
                est.set("id", estado.id);
                est.set("descripcion", estado.descripcion);
                datos.add(est);
            } else {
                Objeto pendiente = new Objeto();
                pendiente.set("id", 2);
                pendiente.set("descripcion", "PENDIENTE FIRMA");
                datos.add(pendiente);

                Objeto parcial = new Objeto();
                parcial.set("id", 4);
                parcial.set("descripcion", "PARCIALMENTE FIRMADA");
                datos.add(parcial);
            }
        }
        return respuesta("datos", datos);
    }

    public static Object listarArchivos(ContextoOB contexto) {
        int nroAdh = contexto.parametros.integer("nroAdh");
        int nroConv = contexto.parametros.integer("nroConv");
        int nroSubConv = contexto.parametros.integer("nroSubConv");
        LotesPAP listLotes = ApiEmpresas.archivosRecibidosOP(contexto, nroAdh, nroConv, nroSubConv).tryGet();
        return respuesta("datos", listLotes);
    }
    public static Object listarArchivosEnviados(ContextoOB contexto) {
        int nroAdh = contexto.parametros.integer("nroAdh");
        int nroConv = contexto.parametros.integer("nroConv");
        int nroSubConv = contexto.parametros.integer("nroSubConv");
        String fechaD = contexto.parametros.string("fechaD");
        String fechaH = contexto.parametros.string("fechaH");
        String tipoArch = contexto.parametros.string("tipoArch");
        ArchivosEnviados archivos = ApiEmpresas.archivosEnviadosPagoProveedores(contexto,nroAdh,nroConv,nroSubConv,fechaD,fechaH,tipoArch).tryGet();
        return respuesta("datos", archivos);
    }
    public static Object listarArchivosComprobantes(ContextoOB contexto) {
        int nroAdh = contexto.parametros.integer("nroAdh");
        int nroConv = contexto.parametros.integer("nroConv");
        int nroSubConv = contexto.parametros.integer("nroSubConv");
        String fechaD = contexto.parametros.string("fechaD");
        String fechaH = contexto.parametros.string("fechaH");
        ServicioComprobantesPAPOB servicioComprobantesPAPOB = new ServicioComprobantesPAPOB(contexto);
        LocalDateTime fechaDesde = LocalDate.parse(fechaD).atStartOfDay();
        LocalDateTime fechaHasta =  LocalDate.parse(fechaH).atStartOfDay();
        List<ComprobantePAPOB> archivos =  servicioComprobantesPAPOB.listar(contexto,nroAdh,nroConv,nroSubConv,fechaDesde,fechaHasta).tryGet();
        Objeto datos=new Objeto();
        for(ComprobantePAPOB c : archivos){
            Objeto data=new Objeto();
            data.set("id",c.id);
            data.set("convenio",c.convenio);
            data.set("fechaCreacion",c.fechaCreacion.toString().split("T")[0]);
            data.set("subconvenio",c.subconvenio);
            data.set("nombreArchivo",c.nombreArchivo);
            data.set("estado",c.estado);
            data.set("cantidad_filas",c.cantidad_filas);
            data.set("creadoPor",c.usuario.nombre + " "+ c.usuario.apellido);
            datos.add("archivos",data);
        }

        return respuesta("datos",datos);
    }

    public static Object verTablaComprobantes(ContextoOB contexto) {
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");

        Objeto dato = new Objeto();
        ServicioComprobantesPAPOB servicioCPAP = new ServicioComprobantesPAPOB(contexto);
        ComprobantePAPOB comprobante = null;
        try {
            comprobante = servicioCPAP.find(Long.valueOf(idOperacion)).get();
        }catch (Exception e ){
            System.out.println("error en buscar comprobante - ver tabla: " + e.getMessage());
        }

        if (empty(comprobante) || comprobante == null) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo en base de datos.");
        }
        List<String> columnas = new ArrayList<>();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        Blob archivo=null;
        try {
            AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.PAGO_PROVEEDORES, reemplazarTXT(comprobante.nombreArchivo));
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());

        }catch (Exception e ){
            return respuesta("ERROR", "descripcion", "No se encontró el archivo en azure.");
        }
    try {
        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        List<ArchivoComprobantesDTO.HeaderPage> listHeadersPagina = ArchivoComprobantesDTO.getHeaderPages(new String(archivo.getBinaryStream().readAllBytes(), Charset.forName("Cp1252")));
        int numLinea = 1;
        for (ArchivoComprobantesDTO.HeaderPage item : listHeadersPagina) {
            Objeto resp = OBValidacionesComprobantesPAP.validarHeaderPagina(item, numLinea);
            if (!resp.get("estado").equals("0")) {
                return contexto.response(200, resp);
            }
            numLinea++;
        }
        List<ArchivoComprobantesDTO.HeaderPage> listaPaginada = getPage(listHeadersPagina, numeroPagina, registrosPorPagina);
        for (ArchivoComprobantesDTO.HeaderPage item : listaPaginada) {
            Objeto detail = new Objeto();
            detail.set("referencia", item.referenciaPago.trim());
            detail.set("retencion", EnumRetencionTipo.fromCodigo(Integer.valueOf(item.tipoCertificado)).getNombre());
            respuesta.add("registros", detail);
        }

        dato.set("cantidadRegistros", listHeadersPagina.size());
        dato.set("nombreArchivo", comprobante.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("paginasTotales", (int) Math.ceil(Integer.parseInt(dato.get("cantidadRegistros").toString()) / registrosPorPagina));
        respuesta.add("informacionArchivo", dato);
        columnas.add("Número de orden de pago");
        columnas.add("Tipo de retención");
        respuesta.add("columnas", columnas);


        return respuesta("datos", respuesta);
    }catch (Exception e ){
        return respuesta("ERROR", "descripcion", "ERROR EN EL ARMADO DEL ARCHIVO");
    }

    }

    public static Object descargaArchivosEnviados(ContextoOB contexto) {
        int idLote = contexto.parametros.integer("idLote");
        ArchivoEnviado archivo = ApiEmpresas.descargaArchivosEnviadosPagoProveedores(contexto,idLote).tryGet();
        byte[] decodedBytes = Base64.getDecoder().decode(archivo.archivoEnviado.archivo);
        try {
            ByteArrayOutputStream fos = new ByteArrayOutputStream();
            ZipOutputStream zipOut = new ZipOutputStream(fos);
            ZipEntry zipEntry = new ZipEntry(archivo.archivoEnviado.IDArchEnviado+".txt");
            zipOut.putNextEntry(zipEntry);
            zipOut.write(decodedBytes, 0, decodedBytes.length);
            zipOut.closeEntry();
            zipOut.finish();
            contexto.response.type("application/zip");
            contexto.response.header("Access-Control-Expose-Headers", "Content-Disposition");
            contexto.response.header("Content-Disposition", "attachment; filename="+archivo.archivoEnviado.IDArchEnviado+ ".zip");
            Archivo file = new Archivo(archivo.archivoEnviado.IDArchEnviado + ".zip", fos.toByteArray());
            return file;

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Object detallesPAP(ContextoOB contexto) {
        Integer idOperacion = contexto.parametros.integer("idOperacion");
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioControlDualAutorizanteOB servicioControlDualAutorizanteOB = new ServicioControlDualAutorizanteOB(contexto);
                
        PagoAProveedoresOB pago = servicioPagoAProveedoresOB.find(idOperacion).get();
        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy/MM/dd");
        Objeto dato = new Objeto();
        dato.set("archivo", pago.nombreArchivo);
        dato.set("cantidadRegistros", pago.cantidadRegistros);
        dato.set("creadoPor", pago.usuario.nombre + " " + pago.usuario.apellido);
        dato.set("fechaCreacion", pago.fechaCreacion.format(formatter));
        dato.set("monto", pago.monto);
        Objeto estado = new Objeto();
        estado.set("id", pago.estado.id);
        estado.set("descripcionCorta", pago.estado.descripcion.equals("EN BANDEJA")?pago.estadoBandeja.descripcion:pago.estado.descripcion);
        dato.set("estado", estado);
        dato.set("cuentaOrigen", pago.cuentaOrigen);
        dato.set("autorizante","");
        dato.set("firmas","");
        dato.set("autorizantes","");
        try{
            ControlDualAutorizanteOB autorizante = servicioControlDualAutorizanteOB.findAutorizantePorEmpresa(contexto.sesion().usuarioOB, contexto.sesion().empresaOB).tryGet();
            dato.set("autorizante", empty(autorizante) ? false : true);
            BandejaOB bandeja = servicioBandeja.find(idOperacion).get();
            dato.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
            dato.set("autorizantes", OBControlDual.obtenerDatosAutorizantes(contexto, bandeja));
            verificarYActualizarEstado(contexto,servicioPagoAProveedoresOB,new ServicioEstadosPagoAProveedoresOB(contexto),pago,dato);
        }catch (Exception e){
            LogOB.evento(contexto,"DetallesPAP",e.toString());
        }
        return respuesta("datos", dato);
    }

    private static void verificarYActualizarEstado(ContextoOB contexto, ServicioPagoAProveedoresOB servicioPagoAProveedoresOB, ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedoresOB, PagoAProveedoresOB pago, Objeto dato) {
        if (pago.estadoBandeja.id == EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()) {
            LotesPAP archivo = ApiEmpresas.archivosRecibidosOP(contexto, Integer.parseInt(pago.nroAdherente), pago.convenio, pago.subconvenio).get();

            if (!archivo.archivosRecibidos.archivoRecibido.isEmpty()) {
                Optional<LotesPAP.ArchivoRecibido> archivoRecibido = archivo.archivosRecibidos.archivoRecibido.stream().filter(
                        a -> a.nombOrigArch.equals(reemplazarTXT(pago.nombreArchivo))).findFirst();
                if (archivoRecibido.isPresent()) {
                    int estadoNuevo = archivoRecibido.get().estado;

                    if (estadoNuevo != (EnumEstadosCorePAPOB.PENDIENTE.getCodigo())) {

                        pago.estado = switch (estadoNuevo) {
                            case 2, 3 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.ENVIADO_BANCO.getCodigo()).get();
                            case 4, 5, 14 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();
                            case 6 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.PROCESADO.getCodigo()).get();
                            default ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()).get();
                        };
                        pago.nroLote = String.valueOf(archivo.archivosRecibidos.archivoRecibido.get(0).idLote);
                        servicioPagoAProveedoresOB.update(pago);
                    }
                }
            }
        }
        dato.set("estadoOperacion", pago.estado.descripcion);
    }

    
    
    public static Object actualizarEstado(ContextoOB contexto) {
        ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedoresOB = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        List<PagoAProveedoresOB> pagos = new ArrayList<>();
       try {
           pagos = servicioPagoAProveedoresOB.buscarPorEstadosYEmpresa(null, servicioEstadoBandeja.find(EnumEstadoBandejaOB.FIRMADO_COMPLETO.getCodigo()).get(), contexto.sesion().empresaOB).get();
       } catch (Exception e){

       }
        int convenioInicial = 0;
        int subConvenioInicial = 0;
        String adherenteInicial = "0";
        if (!pagos.isEmpty()) {
            convenioInicial = pagos.get(0).convenio;
            subConvenioInicial = pagos.get(0).subconvenio;
            adherenteInicial = pagos.get(0).nroAdherente;
            pagos.stream().sorted(Comparator.comparingInt(p -> p.convenio)).collect(Collectors.toList());
        }
        int contLlamadas = 0;
        LotesPAP archivo = null;
        for (PagoAProveedoresOB pago : pagos) {
            if ((pago.convenio != convenioInicial || pago.subconvenio != subConvenioInicial || !pago.nroAdherente.equals(adherenteInicial)) || contLlamadas == 0) {
                archivo = ApiEmpresas.archivosRecibidosOP(contexto, Integer.parseInt(pago.nroAdherente), pago.convenio, pago.subconvenio).tryGet();
                convenioInicial = pago.convenio;
                subConvenioInicial = pago.subconvenio;
                adherenteInicial = pago.nroAdherente;
                contLlamadas++;
            }
            if (archivo.archivosRecibidos!=null && !archivo.archivosRecibidos.archivoRecibido.isEmpty()) {
                Optional<LotesPAP.ArchivoRecibido> archivoRecibido = archivo.archivosRecibidos.archivoRecibido.stream().filter(
                                a -> a.nombOrigArch.equals(reemplazarTXT(pago.nombreArchivo)))
                        .findFirst();
                if (archivoRecibido.isPresent()) {
                    int estadoNuevo = archivoRecibido.get().estado;

                    if (estadoNuevo != (EnumEstadosCorePAPOB.PENDIENTE.getCodigo())) {

                        pago.estado = switch (estadoNuevo) {
                            case 2, 3 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.ENVIADO_BANCO.getCodigo()).get();
                            case 4, 5, 14 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();
                            case 6 ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.PROCESADO.getCodigo()).get();
                            default ->
                                    servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()).get();
                        };
                        pago.nroLote = String.valueOf(archivo.archivosRecibidos.archivoRecibido.get(0).idLote);
                        servicioPagoAProveedoresOB.update(pago);
                    }
                }
            }
        }
        return respuesta("0");
    }

    public static Object historialPagoProveedores(ContextoOB contexto) {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
        Integer convenio = contexto.parametros.integer("convenio", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion", null);
        String estadoFiltro = contexto.parametros.string("estado", null);
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedoresOB = new ServicioEstadosPagoAProveedoresOB(contexto);
        Objeto respuesta = new Objeto();
        EstadosPagosAProveedoresOB estadoAFiltrar = null;

        if (contexto.sesion().empresaOB.cuit.toString().equals("30624957942")&&contexto.sesion().usuarioOB.numeroDocumento.toString().equals("40854690")&&convenio==null){
            convenio = 1102;
        }

        String estadoFiltroEnum = null;
        if (estadoFiltro != null) {
            estadoFiltroEnum = estadoFiltro.replaceAll(" ", "_");
            if (!EnumUtils.isValidEnum(EnumEstadoPagosAProveedoresOB.class, estadoFiltroEnum)) {
                if (estadoFiltroEnum.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.name()) || estadoFiltro.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.name())) {
                    estadoAFiltrar = servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.EN_BANDEJA.getCodigo()).get();
                }
            } else {
                estadoAFiltrar = servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.valueOf(estadoFiltroEnum).getCodigo()).get();
            }
        }
        List<PagoAProveedoresOB> movimientos = servicioPagoAProveedoresOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, null, null, estadoAFiltrar,previsualizacion).get();

        if (estadoFiltro != null && estadoFiltroEnum.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.name())) {
            movimientos = movimientos.stream().filter(m -> m.estadoBandeja.id.equals(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo())).toList();
        }

        if (estadoFiltro != null && estadoFiltroEnum.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.name())) {
            movimientos = movimientos.stream().filter(m -> m.estadoBandeja.id.equals(EnumEstadoBandejaOB.PARCIALMENTE_FIRMADA.getCodigo())).toList();
        }

        for (PagoAProveedoresOB m : movimientos) {
            Objeto datos = new Objeto();
            Objeto estado = new Objeto();

            datos.set("idBandeja", m.id);
            datos.set("convenio", m.convenio);
            datos.set("descripcion", m.nombreArchivo);
            datos.set("estadoOperacion", m.estado.descripcion.equals("EN BANDEJA")?"PENDIENTE":m.estado.descripcion);

            estado.set("id", m.estadoBandeja.id);
            estado.set("descripcionCorta", m.estadoBandeja.descripcion);
            datos.set("estado", estado);

            datos.set("monto", m.monto);
            datos.set("fechaCreacion", m.fechaCreacion.toLocalDate().toString());

            respuesta.add(datos);

            if (previsualizacion) {
                if (respuesta.toList().size() == 5) {
                    return respuesta("datos", respuesta);
                }
            }
        }
        return respuesta("datos", respuesta);
    }

    public static Object verTablaPB(ContextoOB contexto) throws SQLException, IOException {
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");
        Objeto dato = new Objeto();
        ServicioPagoBeneficiariosOB servicioPB = new ServicioPagoBeneficiariosOB(contexto);
        PagoBeneficiariosOB pago = servicioPB.find(idOperacion).get();

        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        List<String> columnas = new ArrayList<>();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
        Blob archivo;

        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.PAGO_PROVEEDORES, pago.nombreArchivo);
            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }
        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        List<ArchivoBeneficiarioDTO.Body> listDetalleItem = ArchivoBeneficiarioDTO.getBody(archivo);
        List<ArchivoBeneficiarioDTO.Body> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
        for (ArchivoBeneficiarioDTO.Body item : listaPaginada) {
            Objeto detail = new Objeto();
            detail.set("identificacion", item.registroId);
            detail.set("nombreBeneficiario", item.nombreBeneficiario);
            detail.set("nroDocumento", item.documento);
            detail.set("email", item.email);
            detail.set("cbu", item.cbu);

            respuesta.add("registros", detail);
        }

        dato.set("nombreArchivo", pago.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", pago.cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) pago.cantidadRegistros / registrosPorPagina));
        respuesta.add("informacionArchivo", dato);

        ServicioBeneficiariosConfigOB servicioBeneficiariosConfigOB = new ServicioBeneficiariosConfigOB(contexto);
        columnas.addAll(servicioBeneficiariosConfigOB.findAll().get().stream().parallel()
                .filter(columna -> columna.visible.equals(true))
                .sorted(Comparator.comparing(PagoBeneficiariosConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());
        respuesta.add("columnas", columnas);
        return respuesta("datos", respuesta);
    }

    public static Object verTablaPAP(ContextoOB contexto) throws SQLException, IOException {
        int idOperacion = contexto.parametros.integer("idOperacion");
        int numeroPagina = contexto.parametros.integer("numeroPagina");
        Objeto dato = new Objeto();
        ServicioPagoAProveedoresOB servicioPAP = new ServicioPagoAProveedoresOB(contexto);
        PagoAProveedoresOB pago = servicioPAP.find(idOperacion).get();

        if (empty(pago) || pago == null) {
            return respuesta("DATOS_INVALIDOS");
        }
        List<String> columnas = new ArrayList<>();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        String containerName = contexto.config.string("ob_azure_blob_st_container");

        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto, connectionString, containerName);
//        Blob archivo;
        byte[] archivoBytes;

        try {
            BlobClient blobClient = az.findBlobByName(contexto, EnumTipoProductoOB.PAGO_PROVEEDORES, reemplazarTXT(pago.nombreArchivo));
//            archivo = new SerialBlob(blobClient.downloadContent().toBytes());
            archivoBytes = blobClient.downloadContent().toBytes();
            
            
        } catch (Exception e) {
            return respuesta("ERROR", "descripcion", "No se encontró el archivo con ese id de bandeja.");
        }
        Integer registrosPorPagina = 60;
        Objeto respuesta = new Objeto();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        List<ArchivoPapDTO.Body> listDetalleItem = ArchivoPapDTO.getBody(archivoBytes, getEncoding(archivoBytes));
        String medioPago = null;
        List<ArchivoPapDTO.Body> listaPaginada = getPage(listDetalleItem, numeroPagina, registrosPorPagina);
        for (ArchivoPapDTO.Body item : listaPaginada) {
            Objeto detail = new Objeto();
            detail.set("fechaEjecucion", LocalDate.parse(item.fechaEjecucion, formatter).toString().replace('-', '/'));
            detail.set("importe", Double.parseDouble(eliminarCerosIzquierda(item.importe)) / 100.00);
            detail.set("identificacion", item.referencia);
            detail.set("razonSocial", item.nombreBeneficiario);
            detail.set("nroDocumento", item.documento);
            medioPago = getMedioPago(item);
            detail.set("medioPago", medioPago);
            detail.set("email", item.email);
            detail.set("CBU", item.cbuCredito);
            detail.set("cruzarCheque", item.cruzarCheque);
            detail.set("clausulaNo", item.clausula);
            if (item.fechaVencimiento.isBlank()) {
                detail.set("fechaDiferimiento", item.fechaVencimiento);
            } else {
                detail.set("fechaDiferimiento", LocalDate.parse(item.fechaVencimiento, formatter).toString().replace('-', '/'));
            }

            detail.set("tipoTransaccion", mapearTipoOperatoria(item.tipoOperatoria));
            detail.set("nroCheque", item.nroCheque);
            //detail.set("estado", "pendiente"); //Revisar
            respuesta.add("registros", detail);
        }

        dato.set("nombreArchivo", pago.nombreArchivo);
        dato.set("numeroPagina", numeroPagina);
        dato.set("cantidadRegistros", pago.cantidadRegistros);
        dato.set("paginasTotales", Math.ceil((double) pago.cantidadRegistros / registrosPorPagina));
        respuesta.add("informacionArchivo", dato);


        ServicioPagoAProveedoresConfigOB servicioPagoAProveedoresConfigOB = new ServicioPagoAProveedoresConfigOB(contexto);
        columnas.addAll(servicioPagoAProveedoresConfigOB.findAll().get().stream().parallel()
                .filter(columna -> columna.visible.equals(true))
                .sorted(Comparator.comparing(PagoAProveedoresConfigOB::getPosicion))
                .map(columna -> columna.nombreColumna).toList());
        respuesta.add("columnas", columnas);
        return respuesta("datos", respuesta);
    }

    @NotNull
    private static String getMedioPago(ArchivoPapDTO.Body item) {
        String medioPago;
        if (item.medioPago.equals("002") || item.medioPago.equals("003")) {
            medioPago = "Cheque";
        } else if (item.medioPago.equals("009")) {
            medioPago = "Transferencia";
        } else {
            medioPago = "eCheq";
        }
        return medioPago;
    }

    private static String mapearTipoOperatoria(String tipoOperacion) {
        String response = "";
        switch (tipoOperacion) {
            case "P": {
                response = "Pago a proveedores";
                break;
            }
            case "G": {
                response = "Pago sin gravámenes";
                break;
            }
            case "J": {
                response = "Transferencias judiciales";
                break;
            }
            case "O": {
                response = "Transferencias propias";
                break;
            }
            case "S": {
                response = "Transferencias de sueldo";
                break;
            }
        }
        return response;

    }

    public static Object historialOrdenesPago(ContextoOB contexto) throws Exception {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd");
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd");
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        Integer convenio = contexto.parametros.integer("convenio");
        String adherente = contexto.parametros.string("adherente");
        String estadoOrden = contexto.parametros.string("estado", null);
        Boolean previsualizacion = contexto.parametros.bool("previsualizacion");
        Objeto respuesta = new Objeto();
        int cantRegistros = /*previsualizacion?5:*/800;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedoresOB = new ServicioEstadosPagoAProveedoresOB(contexto);
        boolean entroEnIf = false;
        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));

        SubConveniosOB respuestaConvenios = ApiEmpresas.subconvenios(contexto).get();
        List<SubConveniosOB.SubConvenio> listaSubconvenios = respuestaConvenios.subconvenios.subconvenio;

        if (previsualizacion){
            OrdenPagoOB respuestaOrdenes = null;
                //caso procesadas
                entroEnIf = true;
            ArrayList<OrdenPagoOB.OrdenPago> ordenes = new ArrayList<>();
            Integer codigoEstado = null;
            if (estadoOrden != null) codigoEstado = EnumEstadosCorePAPOB.getCodigoPorTexto(estadoOrden);
                for(SubConveniosOB.SubConvenio subconv: listaSubconvenios) {
                    for (int i = 1; i < 7; i++) {
                            int numeroPagina = 1;
                            boolean finalizoBusqueda = false;
                            do {
                                respuestaOrdenes = ApiEmpresas.ordenesPagos(contexto, subconv.nroConv, subconv.nroSubConv, String.valueOf(subconv.nroAdh), codigoEstado, fechaDesde, fechaHasta, i, String.valueOf(numeroPagina), cantRegistros).get();
                                if (respuestaOrdenes.ordenesPagos != null) {
                                    ordenes.addAll(respuestaOrdenes.ordenesPagos.subconvenio);
                                    if (respuestaOrdenes.ordenesPagos.subconvenio.size() >= cantRegistros && previsualizacion) {
                                        finalizoBusqueda = true;
                                    }
                                }
                                numeroPagina++;
                                if (respuestaOrdenes.ordenesPagos == null) {
                                    finalizoBusqueda = true;
                                } else if (respuestaOrdenes.ordenesPagos.subconvenio.size() < cantRegistros) {
                                    finalizoBusqueda = true;
                                }
                            } while (!finalizoBusqueda);

                    }
                }

                    List<OrdenPagoOB.OrdenPago> ordenesOrdenadas = ordenes.stream()
                            .sorted()
                            .limit(5)
                            .collect(Collectors.toList());


                    for (OrdenPagoOB.OrdenPago ordenPago : ordenesOrdenadas) {

                        //obtengo id de operacion en bandeja
                        try {
                            // PagoAProveedoresOB pago = servicioPagoAProveedoresOB.buscarPorNroLoteYEmpresa(contexto.sesion().empresaOB, String.valueOf(ordenPago.idLote)).get();
                            Objeto datos = new Objeto();
                            datos.set("idOperacion", null);
                            datos.set("convenio", ordenPago.nroConv);
                            datos.set("subconvenio", ordenPago.nroSubConv);
                            datos.set("adherente", ordenPago.nroAdh);

                            for (EnumEstadosCorePAPOB e : EnumEstadosCorePAPOB.values()) {
                                if (ordenPago.codEstOrden == e.getCodigo()) {
                                    datos.set("estado", e.name().replace("_", " "));
                                    break;
                                }
                            }

                            datos.set("monto", ordenPago.impOrden);
                            datos.set("fechaEjecucion", ordenPago.fecEjecOrden.replace("-", "/"));
                            datos.set("moneda", ordenPago.monPago);
                            datos.set("medioPago", ordenPago.descCatMedioEjec);
                            datos.set("cuit", ordenPago.nroCuilCuitBeneficiario);
                            datos.set("nombreBeneficiario", ordenPago.nomBeneficiario);
                            datos.set("nroOrden", ordenPago.nroOrden);
                            datos.set("linea", null);

                            respuesta.add(datos);

                        } catch (Exception e) {
                            try {
                                throw new Exception(e);
                            } catch (Exception ex) {
                                throw new RuntimeException(ex);
                            }
                        }
                    }

                    if (respuesta.objetos().size()<5){
                        SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
                        List<SubConveniosOB.SubConvenio> listaSubconveniosFiltrados = respuestaConvenio.subconvenios.subconvenio.stream().filter(conv->(conv.nroConv==convenio&&conv.nroSubConv!=subconvenio)||(conv.nroConv!=convenio&&conv.nroSubConv==subconvenio)||conv.nroConv!=convenio||conv.nroSubConv!=subconvenio).distinct().toList();
                        int j = 0;
                        while (j<listaSubconveniosFiltrados.size()-1&&respuesta.objetos().size()<5){
                            int convenioSeleccionado = listaSubconveniosFiltrados.get(j).nroConv;
                            int subconvenioSeleccionado = listaSubconveniosFiltrados.get(j).nroSubConv;
                            int adherenteSeleccionado = listaSubconveniosFiltrados.get(j).nroAdh;
                            for (int i = 1;i< 7;i++){
                                if (i!=3){
                                    int numeroPagina = 1;
                                    boolean finalizoBusqueda = false;
                                    do{
                                        respuestaOrdenes = ApiEmpresas.ordenesPagos(contexto, convenioSeleccionado, subconvenioSeleccionado, String.valueOf(adherenteSeleccionado), codigoEstado, fechaDesde, fechaHasta,i, String.valueOf(numeroPagina),cantRegistros).get();
                                        if (respuestaOrdenes.ordenesPagos!=null) {
                                            ordenes.addAll(respuestaOrdenes.ordenesPagos.subconvenio);
                                            if (respuestaOrdenes.ordenesPagos.subconvenio.size()>=cantRegistros && previsualizacion){
                                                finalizoBusqueda = true;
                                            }
                                        }
                                        numeroPagina++;
                                        if (respuestaOrdenes.ordenesPagos==null){
                                            finalizoBusqueda = true;
                                        }else if (respuestaOrdenes.ordenesPagos.subconvenio.size()<cantRegistros){
                                            finalizoBusqueda = true;
                                        }
                                    } while (!finalizoBusqueda);
                                }

                            }
                            ordenesOrdenadas = ordenes.stream()
                                    .sorted()
                                    .collect(Collectors.toList());
                            for (OrdenPagoOB.OrdenPago ordenPago : ordenesOrdenadas) {

                                //obtengo id de operacion en bandeja
                                try {
                                    // PagoAProveedoresOB pago = servicioPagoAProveedoresOB.buscarPorNroLoteYEmpresa(contexto.sesion().empresaOB, String.valueOf(ordenPago.idLote)).get();
                                    Objeto datos = new Objeto();
                                    datos.set("idOperacion", null);
                                    datos.set("convenio", ordenPago.nroConv);
                                    datos.set("subconvenio", ordenPago.nroSubConv);
                                    datos.set("adherente", ordenPago.nroAdh);

                                    for (EnumEstadosCorePAPOB e : EnumEstadosCorePAPOB.values()) {
                                        if (ordenPago.codEstOrden == e.getCodigo()) {
                                            datos.set("estado", e.name().replace("_", " "));
                                            break;
                                        }
                                    }

                                    datos.set("monto", ordenPago.impOrden);
                                    datos.set("fechaEjecucion", ordenPago.fecEjecOrden.replace("-", "/"));
                                    datos.set("moneda", ordenPago.monPago);
                                    datos.set("medioPago", ordenPago.descCatMedioEjec);
                                    datos.set("cuit", ordenPago.nroCuilCuitBeneficiario);
                                    datos.set("nombreBeneficiario", ordenPago.nomBeneficiario);
                                    datos.set("nroOrden", ordenPago.nroOrden);
                                    datos.set("subNroOrden",ordenPago.subNroOrden);
                                    datos.set("admiteStopPayment",ordenPago.admiteStop.equals("S"));
                                    datos.set("linea", null);

                                    respuesta.add(datos);

                                } catch (Exception e) {
                                    try {
                                        throw new Exception(e);
                                    } catch (Exception ex) {
                                        throw new RuntimeException(ex);
                                    }
                                }
                            }
                        }

                    }



            }else{
            //caso procesadas
            entroEnIf = true;
            Integer codigoEstado = null;
            if (estadoOrden != null) codigoEstado = EnumEstadosCorePAPOB.getCodigoPorTexto(estadoOrden);
            OrdenPagoOB respuestaOrdenes = null;
            ArrayList<OrdenPagoOB.OrdenPago> ordenes = new ArrayList<>();
            for (int i = 1;i<7;i++){

                    int numeroPagina = 1;
                    boolean finalizoBusqueda = false;
                    do{
                        respuestaOrdenes = ApiEmpresas.ordenesPagos(contexto, convenio, subconvenio, adherente, codigoEstado, fechaDesde, fechaHasta,i, String.valueOf(numeroPagina),cantRegistros).get();
                        if (respuestaOrdenes.ordenesPagos!=null) {
                            ordenes.addAll(respuestaOrdenes.ordenesPagos.subconvenio);
                            if (respuestaOrdenes.ordenesPagos.subconvenio.size()>=cantRegistros && previsualizacion){
                                finalizoBusqueda = true;
                            }
                        }
                        numeroPagina++;
                        if (respuestaOrdenes.ordenesPagos==null){
                            finalizoBusqueda = true;
                        }else if (respuestaOrdenes.ordenesPagos.subconvenio.size()<cantRegistros){
                            finalizoBusqueda = true;
                        }
                    } while (!finalizoBusqueda);

            }

            if (ordenes.isEmpty()&&!previsualizacion) {
                return respuesta("0", "datos", respuesta);
            }
            List<OrdenPagoOB.OrdenPago> ordenesOrdenadas = ordenes.stream()
                    .sorted()
                    .collect(Collectors.toList());


            for (OrdenPagoOB.OrdenPago ordenPago : ordenesOrdenadas) {

                //obtengo id de operacion en bandeja
                try {
                    // PagoAProveedoresOB pago = servicioPagoAProveedoresOB.buscarPorNroLoteYEmpresa(contexto.sesion().empresaOB, String.valueOf(ordenPago.idLote)).get();
                    Objeto datos = new Objeto();
                    datos.set("idOperacion", null);
                    datos.set("convenio", ordenPago.nroConv);
                    datos.set("subconvenio", ordenPago.nroSubConv);
                    datos.set("adherente", ordenPago.nroAdh);

                    for (EnumEstadosCorePAPOB e : EnumEstadosCorePAPOB.values()) {
                        if (ordenPago.codEstOrden == e.getCodigo()) {
                            datos.set("estado", e.name().replace("_", " "));
                            break;
                        }
                    }

                    datos.set("monto", ordenPago.impOrden);
                    datos.set("fechaEjecucion", ordenPago.fecEjecOrden.replace("-", "/"));
                    datos.set("moneda", ordenPago.monPago);
                    datos.set("medioPago", ordenPago.descCatMedioEjec);
                    datos.set("cuit", ordenPago.nroCuilCuitBeneficiario);
                    datos.set("nombreBeneficiario", ordenPago.nomBeneficiario);
                    datos.set("nroOrden", ordenPago.nroOrden);
                    datos.set("subNroOrden",ordenPago.subNroOrden);
                    datos.set("admiteStopPayment",ordenPago.admiteStop.equals("S"));
                    datos.set("linea", null);

                    respuesta.add(datos);

                } catch (Exception e) {
                    throw new Exception(e);
                }
            }

            if (respuesta.objetos().size()<5){
                SubConveniosOB respuestaConvenio = ApiEmpresas.subconvenios(contexto).get();
                List<SubConveniosOB.SubConvenio> listaSubconveniosFiltrados = respuestaConvenio.subconvenios.subconvenio.stream().filter(conv->(conv.nroConv==convenio&&conv.nroSubConv!=subconvenio)||(conv.nroConv!=convenio&&conv.nroSubConv==subconvenio)||conv.nroConv!=convenio||conv.nroSubConv!=subconvenio).distinct().toList();
                int j = 0;
                while (j<listaSubconveniosFiltrados.size()-1&&respuesta.objetos().size()<5){
                    int convenioSeleccionado = listaSubconveniosFiltrados.get(j).nroConv;
                    int subconvenioSeleccionado = listaSubconveniosFiltrados.get(j).nroSubConv;
                    int adherenteSeleccionado = listaSubconveniosFiltrados.get(j).nroAdh;
                    for (int i = 1;i<6;i++){
                        if (i!=3){
                            int numeroPagina = 1;
                            boolean finalizoBusqueda = false;
                            do{
                                respuestaOrdenes = ApiEmpresas.ordenesPagos(contexto, convenioSeleccionado, subconvenioSeleccionado, String.valueOf(adherenteSeleccionado), codigoEstado, fechaDesde, fechaHasta,i, String.valueOf(numeroPagina),cantRegistros).get();
                                if (respuestaOrdenes.ordenesPagos!=null) {
                                    ordenes.addAll(respuestaOrdenes.ordenesPagos.subconvenio);
                                    if (respuestaOrdenes.ordenesPagos.subconvenio.size()>=cantRegistros && previsualizacion){
                                        finalizoBusqueda = true;
                                    }
                                }
                                numeroPagina++;
                                if (respuestaOrdenes.ordenesPagos==null){
                                    finalizoBusqueda = true;
                                }else if (respuestaOrdenes.ordenesPagos.subconvenio.size()<cantRegistros){
                                    finalizoBusqueda = true;
                                }
                            } while (!finalizoBusqueda);
                        }

                    }
                    ordenesOrdenadas = ordenes.stream()
                            .sorted()
                            .collect(Collectors.toList());
                    for (OrdenPagoOB.OrdenPago ordenPago : ordenesOrdenadas) {

                        //obtengo id de operacion en bandeja
                        try {
                            // PagoAProveedoresOB pago = servicioPagoAProveedoresOB.buscarPorNroLoteYEmpresa(contexto.sesion().empresaOB, String.valueOf(ordenPago.idLote)).get();
                            Objeto datos = new Objeto();
                            datos.set("idOperacion", null);
                            datos.set("convenio", ordenPago.nroConv);
                            datos.set("subconvenio", ordenPago.nroSubConv);
                            datos.set("adherente", ordenPago.nroAdh);

                            for (EnumEstadosCorePAPOB e : EnumEstadosCorePAPOB.values()) {
                                if (ordenPago.codEstOrden == e.getCodigo()) {
                                    datos.set("estado", e.name().replace("_", " "));
                                    break;
                                }
                            }

                            datos.set("monto", ordenPago.impOrden);
                            datos.set("fechaEjecucion", ordenPago.fecEjecOrden.replace("-", "/"));
                            datos.set("moneda", ordenPago.monPago);
                            datos.set("medioPago", ordenPago.descCatMedioEjec);
                            datos.set("cuit", ordenPago.nroCuilCuitBeneficiario);
                            datos.set("nombreBeneficiario", ordenPago.nomBeneficiario);
                            datos.set("nroOrden", ordenPago.nroOrden);
                            datos.set("subNroOrden",ordenPago.subNroOrden);
                            datos.set("admiteStopPayment",ordenPago.admiteStop.equals("S"));
                            datos.set("linea", null);

                            respuesta.add(datos);

                        } catch (Exception e) {
                            throw new Exception(e);
                        }
                    }
                }

            }

        }


        if (previsualizacion && !respuesta.isEmpty()) {
            return respuesta("datos", respuesta.toList().stream().limit(5).collect(Collectors.toList()));
        } else return respuesta("datos", respuesta);
    }

    public static Objeto stopPayment(ContextoOB contexto){
        int nroOrden = contexto.parametros.integer("nroOrden");
        int subNroOrden = contexto.parametros.integer("subNroOrden");

        StopPaymentPapOB stopPaymentPapOB = null;
        try{
            stopPaymentPapOB = ApiEmpresas.stopPayment(contexto,nroOrden,subNroOrden).get();
        }catch (Exception e){
            return respuesta("ERROR","DATOS",e.toString());
        }


        if (!stopPaymentPapOB.respuestaStopPaymentOP.stopAplicado.equals("S")){
            return respuesta("ERROR","DATOS","Error al aplicar Stop Payment");
        } else {
            return respuesta("0","DATOS","Stop Payment aplicado");
        }
    }

    public static Object detalleOrdenPago(ContextoOB contexto) throws SQLException, IOException {
        Integer idOperacion = contexto.parametros.integer("idOperacion", null);
        String nroOrden = contexto.parametros.string("orden", null);
        String linea = contexto.parametros.string("linea", null);
        String fecha = contexto.parametros.string("fecha", null);
        Integer convenio = contexto.parametros.integer("convenio", null);
        Integer subconvenio = contexto.parametros.integer("subconvenio", null);
        Integer adherente = contexto.parametros.integer("adherente", null);

        Objeto respuesta = new Objeto();
        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioBandejaOB servicioBandejaOB = new ServicioBandejaOB(contexto);
        BandejaOB bandeja = null;
        PagoAProveedoresOB pago;
        Fecha fechaDesde = null;

        if (idOperacion != null) {
            bandeja = servicioBandejaOB.find(idOperacion).get();
            pago = servicioPagoAProveedoresOB.find(idOperacion).get();
            fechaDesde = new Fecha(pago.fechaCreacion.toLocalDate().toString(), "yyyy-MM-dd");
            if (pago == null || empty(pago)) {
                return respuesta("estado", "ERROR", "No se encontró la operación");
            }

        } else {
            pago = null;
            if (fecha != null) {
                fechaDesde = new Fecha(fecha.replace('/', '-'), "yyyy-MM-dd");
            }
        }
        Fecha fechaHasta = fechaDesde;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        //caso órdenes fuera del canal ob nuevo
        if (idOperacion == null && nroOrden != null) {
            int i = 1;
            OrdenPagoOB ordenPago = null;
            while (i<7){
                ordenPago = ApiEmpresas.ordenesPagos(contexto, convenio, subconvenio, String.valueOf(adherente), null, fechaDesde, fechaHasta, nroOrden,i).get();
                i++;
                if (ordenPago.ordenesPagos!=null){
                    i=7;
                }
            }
            if (ordenPago.ordenesPagos != null && !ordenPago.ordenesPagos.subconvenio.isEmpty()) {
                OrdenPagoOB.OrdenPago orden = ordenPago.ordenesPagos.subconvenio.stream().filter(s -> s.nroSubConv == subconvenio).findFirst().get();
                respuesta.set("medioPago", orden.descCatMedioEjec);
                if (orden.descMonPago.equals("PESOS")) respuesta.set("moneda", EnumMonedasOB.PESOS.getMoneda());
                respuesta.set("monto", orden.impOrden);
                String estado = EnumEstadosCorePAPOB.fromCodigo(orden.codEstOrden).toString();
                respuesta.set("estadoOperacion", estado);
                if (estado.equals("RECHAZADO")) {
                    ErrorOrdenPagoOB error = ApiEmpresas.errorOrdenPago(contexto, orden.nroOrden, orden.subNroOrden).get();
                    if (error.erroresOP.errorOP.get(0).descError.isBlank()) {
                        respuesta.set("motivoRechazo", error.erroresOP.errorOP.get(0).errorExterno);
                    } else {
                        respuesta.set("motivoRechazo", error.erroresOP.errorOP.get(0).descError);
                    }
                }
                respuesta.set("fechaOperacion", orden.fecEjecOrden);
                respuesta.set("fechaCreacion", orden.fecEjecOrden);
                respuesta.set("nombreBeneficiario", orden.nomBeneficiario);
                if (orden.descCatMedioEjec.equals("TRANSFERENCIAS")) {
                    ServicioBancoOB servicioBanco = new ServicioBancoOB(contexto);
                    if (orden.transferencia!=null){BancoOB banco = servicioBanco.find(Integer.valueOf(orden.transferencia.cbuCtaAcred.substring(0, 3))).get();
                        if (banco == null) {
                            respuesta.set("bancoBeneficiario", null);
                        } else {
                            respuesta.set("bancoBeneficiario", banco.denominacion);
                        }
                    }else{
                        respuesta.set("bancoBeneficiario", null);
                    }

                    respuesta.set("cbuAcreditar", orden.transferencia!=null?orden.transferencia.cbuCtaAcred:null);
                } else if (orden.descCatMedioEjec.contains("CHEQUES")) {
                    respuesta.set("bancoBeneficiario", null);
                    respuesta.set("cbuAcreditar", null);
                    respuesta.set("descNroAdh", orden.descNroAdh);
                    respuesta.set("cuitEmisor", orden.cuitEmp);
                    if (orden.cheque != null) {
                        Objeto cheque = new Objeto();
                        cheque.set("numeroCheque", orden.cheque.get(0).nroCheque);
                        if (orden.cheque.get(0).tipoCheque.equals("N")) {
                            cheque.set("caracter", "No a la orden");
                        } else cheque.set("caracter", "A la orden");

                        cheque.set("fechaEmision", orden.cheque.get(0).fecDispChq);
                        cheque.set("fechaPago", orden.cheque.get(0).fechaVencimiento);
                        cheque.set("codigoSucursal", orden.cheque.get(0).sucDest);
                        cheque.set("tipoCheque", orden.cheque.get(0).tipoCheque);

                        cheque.set("codigoBanco", "044");
                        respuesta.set("cheque", cheque);
                    }

                }
                respuesta.set("cuentaOrigen", orden.formatoCtaADebitar);
                respuesta.set("CBUDebito", orden.cbuCtaADebitar.trim());
                respuesta.set("motivo", orden.motivoPago);
                respuesta.set("tipoOp", orden.descTipoOper);
                respuesta.set("cuitBeneficiario", orden.nroCuilCuitBeneficiario);
                respuesta.set("emailBeneficiario", orden.emailBeneficiario);
                respuesta.set("nroOrden", orden.nroOrden);
                respuesta.set("subNroOrden",orden.subNroOrden);
                respuesta.set("admiteStopPayment",orden.admiteStop.equals("S"));
                respuesta.set("nroOpe", orden.nroOrden);
                respuesta.set("bancoOrigen", EnumBancosOB.BH.getNombre());
                respuesta.set("cuitOrigen", contexto.sesion().empresaOB.cuit);
                respuesta.set("rSocialOrigen", contexto.sesion().empresaOB.razonSocial);
                respuesta.set("creadoPor", null);
                respuesta.set("firmas", null);
                return respuesta("datos", respuesta);
            }
        }
        //caso procesado
        if (pago != null && (pago.estado.id.equals(EnumEstadoPagosAProveedoresOB.ENVIADO_BANCO.getCodigo()) || pago.estado.id.equals(EnumEstadoPagosAProveedoresOB.PENDIENTE_AUTORIZACION.getCodigo()) || pago.estado.id.equals(EnumEstadoPagosAProveedoresOB.PROCESADO.getCodigo()))) {
            int i = 1;
            OrdenPagoOB ordenPago = null;
            while (i<7){
                ordenPago = ApiEmpresas.ordenesPagos(contexto, convenio, subconvenio, String.valueOf(adherente), null, fechaDesde, fechaHasta, nroOrden,i).get();
                i++;
                if (ordenPago.ordenesPagos!=null){
                    i=7;
                }
            }

            OrdenPagoOB.OrdenPago orden = ordenPago.ordenesPagos.subconvenio.stream().filter(o -> o.nroOrden == Integer.parseInt(nroOrden)).findFirst().get();
            respuesta.set("medioPago", orden.descCatMedioEjec.replace("_", " "));

            if (orden.descMonPago.equals("PESOS")) respuesta.set("moneda", EnumMonedasOB.PESOS.getMoneda());
            respuesta.set("monto", orden.impOrden);
            respuesta.set("estadoOperacion", EnumEstadosCorePAPOB.fromCodigo(orden.codEstOrden).toString());
            respuesta.set("fechaOperacion", orden.fecEjecOrden);
            respuesta.set("nombreBeneficiario", orden.nomBeneficiario);
            if (orden.descCatMedioEjec.equals("TRANSFERENCIAS")) {
                ServicioBancoOB servicioBanco = new ServicioBancoOB(contexto);
                BancoOB banco = servicioBanco.find(Integer.valueOf(orden.cbuCtaADebitar.substring(0, 3))).get();
                if (banco == null) {
                    respuesta.set("bancoBeneficiario", null);
                } else {
                    respuesta.set("bancoBeneficiario", banco.denominacion);
                }
                respuesta.set("cbuAcreditar", orden.cbuCtaADebitar);
            } else if (orden.descCatMedioEjec.contains("CHEQUES")) {
                respuesta.set("bancoBeneficiario", null);
                respuesta.set("cbuAcreditar", null);
                if (orden.cheque != null) {
                    Objeto cheque = new Objeto();
                    cheque.set("numeroCheque", orden.cheque.get(0).nroCheque);
                    if (orden.cheque.get(0).tipoCheque.equals("N")) {
                        cheque.set("caracter", "No a la orden");
                    } else cheque.set("caracter", "A la orden");

                    cheque.set("fechaEmision", orden.cheque.get(0).fecDispChq);
                    cheque.set("fechaPago", orden.cheque.get(0).fechaVencimiento);
                    cheque.set("codigoSucursal", orden.cheque.get(0).sucDest);
                    cheque.set("codigoBanco", "044");
                    respuesta.set("cheque", cheque);
                }

            }
            respuesta.set("cuentaOrigen", orden.formatoCtaADebitar);
            respuesta.set("CBUDebito", orden.cbuCtaADebitar.trim());
            respuesta.set("bancoOrigen", EnumBancosOB.BH.getNombre());
        }

        //caso pendiente de proceso
        if (pago != null && (pago.estado.id.equals(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()) || pago.estado.id.equals(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()))) {
            byte[] archivoBytes = pago.archivo.getBinaryStream().readAllBytes(); 
            String encoding;

            ArchivoPapDTO.Body detalleItem = ArchivoPapDTO.getBody(archivoBytes, getEncoding(archivoBytes)).get(Integer.parseInt(linea) - 1);
            for (EnumMediosPagoPAPOB mediosPago : EnumMediosPagoPAPOB.values()) {
                if (mediosPago.getCodigo().equals(detalleItem.medioPago)) {
                    respuesta.set("medioPago", mediosPago.name().replace("_", " "));
                    break;
                }
            }

            if (detalleItem.moneda.equals("ARS")) respuesta.set("moneda", EnumMonedasOB.PESOS.getMoneda());
            respuesta.set("monto", Double.parseDouble(eliminarCerosIzquierda(detalleItem.importe)) / 100.00);
            respuesta.set("estadoOperacion", EnumEstadoPagosAProveedoresOB.PENDIENTE.name());
            respuesta.set("fechaOperacion", detalleItem.fechaEjecucion.substring(6, 8) + "/" + detalleItem.fechaEjecucion.substring(4, 6) + "/" + detalleItem.fechaEjecucion.substring(0, 4));

            respuesta.set("nombreBeneficiario", detalleItem.nombreBeneficiario);
            if (detalleItem.medioPago.equals("009")) {
                ServicioBancoOB servicioBanco = new ServicioBancoOB(contexto);
                BancoOB banco = servicioBanco.find(Integer.valueOf(detalleItem.cbuCredito.substring(0, 3))).get();
                if (banco == null) {
                    respuesta.set("bancoBeneficiario", null);
                } else {
                    respuesta.set("bancoBeneficiario", banco.denominacion);
                }
                respuesta.set("cbuAcreditar", detalleItem.cbuCredito);
            } else {
                respuesta.set("nroCuilCuitBeneficiario", detalleItem.documento);
                respuesta.set("bancoBeneficiario", null);
                respuesta.set("cbuAcreditar", null);
                Objeto cheque = new Objeto();
                cheque.set("numerocheque", null);
                cheque.set("caracter", null);
                cheque.set("fechaEmision", null);
                cheque.set("fechaPago", null);
                cheque.set("codigoSucursal", null);
                cheque.set("codigoBanco", "044");
            }
            String contenidoArchivo = blobToString(pago.archivo);
            ArchivoPapDTO.Header cabecera = ArchivoPapDTO.getHeader(contenidoArchivo.trim());
            if (detalleItem.CBUDebito != null && !detalleItem.CBUDebito.equals("")) {
                respuesta.set("CBUDebito", detalleItem.CBUDebito);
                respuesta.set("cuentaOrigen", detalleItem.CBUDebito.substring(8, 21));
            } else {


                if (!cabecera.cbu.trim().isEmpty()) {
                    respuesta.set("cuentaOrigen", cabecera.cbu.substring(8, 21));
                    respuesta.set("CBUDebito", cabecera.cbu);
                } else {
                    String cbu = ApiEmpresas.subconvenios(contexto).get().subconvenios.subconvenio.stream().filter(s -> s.nroAdh == Integer.parseInt(pago.nroAdherente)).findFirst().get().cbuCuentaPagos;
                    respuesta.set("CBUDebito", cbu);
                    respuesta.set("cuentaOrigen", cbu.substring(9, 21));
                }
            }
            respuesta.set("motivo", null);
            respuesta.set("bancoOrigen", EnumBancosOB.BH.getNombre());
            respuesta.set("tipoOp", null);
            respuesta.set("cuitBeneficiario", detalleItem.documento);
            respuesta.set("emailBeneficiario", detalleItem.email);
            respuesta.set("nroOpe", null);
            respuesta.set("cuitOrigen", contexto.sesion().empresaOB.cuit);
            respuesta.set("rSocialOrigen", contexto.sesion().empresaOB.razonSocial);
            respuesta.set("creadoPor", null);
            respuesta.set("firmas", null);
        }

        //compartidos
        if (pago != null) {
            respuesta.set("creadoPor", pago.usuario.nombreCompleto());
            respuesta.set("fechaCreacion", pago.fechaCreacion.format(formatter));
            respuesta.set("firmas", OBFirmas.obtenerDatosFirmantes(contexto, bandeja));
            respuesta.set("bancoOrigen", EnumBancosOB.BH.getNombre());
        }

        return respuesta("datos", respuesta);
    }

    public static Object consultaEstadosOrdenes(ContextoOB contexto) {
        Objeto datos = new Objeto();
        for (EnumEstadosCorePAPOB estado : EnumEstadosCorePAPOB.values()) {
            Objeto est = new Objeto();
            est.set("id", estado.getCodigo());
            est.set("descripcion", estado.name().replace("_", " "));
            datos.add(est);
        }
        return respuesta("datos", datos);
    }
    public static Object consultabeneficiarios(ContextoOB contexto) {
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        Integer convenio = contexto.parametros.integer("convenio");
        ArrayList<Objeto> beneficiarios = new ArrayList<>();
        BeneficiariosOB respuestaBeneficiarios = ApiEmpresas.beneficiarios(contexto, convenio, subconvenio).get();
        if(respuestaBeneficiarios.beneficiarios!=null){
             SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");


        ArrayList<BeneficiariosOB.Beneficiario> listaBeneficiarios = respuestaBeneficiarios.beneficiarios.beneficiario;
            beneficiarios.addAll(listaBeneficiarios.stream().map(b ->{
                Objeto data = new Objeto();
                String fecha = formatter.format(b.fecAlta);
                data.set("nombre",b.nomBeneficiario);
                data.set("tipoDocumento",b.tipoDoc);
                data.set("cuit",b.nroCuilCuit);
                data.set("fecAlta", fecha);
                data.set("nroConv",b.nroConv);
                data.set("nroSubConv",b.nroSubConv);
                data.set("descTipoDoc",b.descTipoDoc);
                data.set("mail",b.mail);
                data.set("cbu",b.cbuCtaInt);
                data.set("id",b.nroBeneficiarioSist);
                return  data;
            }).toList()
        );
        }

        return respuesta("datos", beneficiarios);
    }

    public static Object descargarOrdenesDePago(ContextoOB contexto) throws Exception {
        Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", Fecha.hoy().restarMeses(6));
        Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", Fecha.hoy());
        Integer subconvenio = contexto.parametros.integer("subconvenio");
        Integer convenio = contexto.parametros.integer("convenio");
        String adherente = contexto.parametros.string("adherente");
        String estadoOrden = contexto.parametros.string("estadoOrden", null);

        ArrayList<OrdenDePagoDTO> ordenesDto = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        ServicioPagoAProveedoresOB servicioPagoAProveedoresOB = new ServicioPagoAProveedoresOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPagoAProveedoresOB = new ServicioEstadosPagoAProveedoresOB(contexto);
        boolean entroEnIf = false;
        Random random = new Random(Integer.valueOf(contexto.sesion().empresaOB.idCobis));
        Integer nroConsulta = random.nextInt();

        if ((!entroEnIf)/*&&(estadoOrden == null || estadoOrden.equals(EnumEstadosOrdenesPagoOB.SIN_RETIRAR.name()) || estadoOrden.equals(EnumEstadosOrdenesPagoOB.PROCESADA.name()) || estadoOrden.equals(EnumEstadosCorePAPOB.RECHAZADO.name()) || estadoOrden.equals(EnumEstadosOrdenesPagoOB.PAGO_ANULADO.name())||estadoOrden.equals(EnumEstadosCorePAPOB.EXITOSA.name()))*/) {
            //caso procesadas
            entroEnIf = true;
            Integer codigoEstado = null;
            if (estadoOrden != null) codigoEstado = EnumEstadosCorePAPOB.getCodigoPorTexto(estadoOrden);
            OrdenPagoOB respuestaOrdenes = null;

            ArrayList<OrdenPagoOB.OrdenPago> ordenes = new ArrayList<>();
            for (int i = 1; i < 7; i++) {
                int j = 1;
                boolean finalizoBusqueda = false;
                do{
                    respuestaOrdenes = ApiEmpresas.ordenesPagos(contexto, convenio, subconvenio, adherente, codigoEstado, fechaDesde, fechaHasta,i, String.valueOf(j),800).get();
                    if (respuestaOrdenes.ordenesPagos!=null) ordenes.addAll(respuestaOrdenes.ordenesPagos.subconvenio);
                    j++;
                    if (respuestaOrdenes.ordenesPagos==null){
                        finalizoBusqueda = true;
                    }else if (respuestaOrdenes.ordenesPagos.subconvenio.size()<800){
                        finalizoBusqueda = true;
                    }
                } while (!finalizoBusqueda);
            }
            Collections.reverse(ordenes);

            String estado = "";
            for (OrdenPagoOB.OrdenPago ordenPago : ordenes) {

                for (EnumEstadosCorePAPOB e : EnumEstadosCorePAPOB.values()) {
                    if (ordenPago.codEstOrden == e.getCodigo()) {
                        estado = e.name().replace("_", " ");
                        break;
                    }
                }
                String nroOrden = String.format("%-9s", ordenPago.nroOrden).replace(' ', '0');


                try {
                    String medioEjec = ordenPago.descCatMedioEjec.toUpperCase(); // Normalizar a mayúsculas para la comparación
                    if (medioEjec.contains("TRANSFERENCIA")) {
                        // Agregar datos de transferencia
                        ordenesDto.add(new OrdenDePagoDTO(ordenPago.fecEjecOrden.replace("-", "/"), ordenPago.nomBeneficiario, ordenPago.nroCuilCuitBeneficiario,
                                ordenPago.descCatMedioEjec, ordenPago.simboloMonPago + ordenPago.impOrden, estado, ordenPago.refCliente, ordenPago.nroOrden, nroOrden,
                                " ", null, " ", " ", " ", " ", " ", " "," "));
                    } else if (medioEjec.contains("CHEQUES") || medioEjec.contains("ECHEQ")) {
                        // Agregar datos de cheques y echeqs
                        ordenesDto.add(new OrdenDePagoDTO(ordenPago.fecEjecOrden.replace("-", "/"), ordenPago.nomBeneficiario, ordenPago.nroCuilCuitBeneficiario,
                                ordenPago.descCatMedioEjec, ordenPago.simboloMonPago + ordenPago.impOrden, estado, ordenPago.refCliente, ordenPago.nroOrden, nroOrden,
                                ordenPago.cheque.get(0).serCheque, ordenPago.cheque.get(0).nroCheque , ordenPago.cheque.get(0).cruzarCheque, ordenPago.cheque.get(0).reqCheqNoALaOrden,
                                ordenPago.cheque.get(0).fechaVencimiento, ordenPago.cheque.get(0).fecVencimientoVig, ordenPago.cheque.get(0).fecEntrega, ordenPago.cheque.get(0).fecPagoRechazo,ordenPago.indicBeneficiario));
                    }
                } catch (Exception e) {
                    throw new Exception(e);
                }
            }
        }

        if ((!entroEnIf) && estadoOrden == null || (estadoOrden != null && estadoOrden.equals(EnumEstadosOrdenesPagoOB.ENVIADA.name()))) {
            //caso firmado completo pero pendiente de proceso
            entroEnIf = true;
            List<PagoAProveedoresOB> papsEnProceso = servicioPagoAProveedoresOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, subconvenio, adherente, servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()).get(),false).get();

            String medioPago = null;
            byte[] archivoBytes; 
                        
            for (PagoAProveedoresOB papEnProceso : papsEnProceso) {
            	archivoBytes = papEnProceso.archivo.getBinaryStream().readAllBytes();
                List<ArchivoPapDTO.Body> listDetalleItem = ArchivoPapDTO.getBody(archivoBytes, getEncoding(archivoBytes));
                for (ArchivoPapDTO.Body item : listDetalleItem) {

                    for (EnumMediosPagoPAPOB mediosPago : EnumMediosPagoPAPOB.values()) {
                        if (mediosPago.getCodigo().equals(item.medioPago)) {
                            medioPago = mediosPago.name();
                            break;
                        }
                    }

                    if (medioPago.equals("TRANSFERENCIA")) {
                        if (!eliminarCerosIzquierda(item.cbuCredito).startsWith("044")) {
                            medioPago = "Transferencia a otro banco";
                        } else medioPago = "Transferencia interna";
                    }

                    ordenesDto.add(new OrdenDePagoDTO(EnumEstadosOrdenesPagoOB.ENVIADA.name().replace("_", " "), LocalDate.parse(item.fechaEjecucion,
                            formatter).toString().replace('-', '/'), item.moneda + item.importe, medioPago, item.documento, item.nombreBeneficiario, null, null,"",
                            " ",null," "," "," "," "," "," "," "));

                }
            }
        }

        //caso rechazadas en firma
        if ((!entroEnIf) && estadoOrden == null || (estadoOrden != null && estadoOrden.equals(EnumEstadosOrdenesPagoOB.RECHAZADA.name()))) {
            entroEnIf = true;
            List<PagoAProveedoresOB> papsRechazados = servicioPagoAProveedoresOB.filtrarMovimientosHistorial(contexto.sesion().empresaOB, fechaDesde, fechaHasta, convenio, subconvenio, adherente, servicioEstadosPagoAProveedoresOB.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get(),false).get();
            String medioPago = null;
            for (PagoAProveedoresOB papRechazado : papsRechazados) {
            	byte[] archivoBytesRechazado = papRechazado.archivo.getBinaryStream().readAllBytes();
            	
                List<ArchivoPapDTO.Body> listDetalleItem = ArchivoPapDTO.getBody(archivoBytesRechazado, getEncoding(archivoBytesRechazado));
                for (ArchivoPapDTO.Body item : listDetalleItem) {

                    for (EnumMediosPagoPAPOB mediosPago : EnumMediosPagoPAPOB.values()) {
                        if (mediosPago.getCodigo().equals(item.medioPago)) {
                            medioPago = mediosPago.name();
                            break;
                        }
                    }

                    if (medioPago.equals("TRANSFERENCIA")) {
                        if (!eliminarCerosIzquierda(item.cbuCredito).startsWith("044")) {
                            medioPago = "Transferencia a otro banco";
                        } else medioPago = "Transferencia interna";
                    }

                    ordenesDto.add(new OrdenDePagoDTO(EnumEstadosOrdenesPagoOB.RECHAZADA.name().replace("_", " "),
                            LocalDate.parse(item.fechaEjecucion, formatter).toString().replace('-', '/'), item.moneda + item.importe, medioPago, item.documento,
                            item.nombreBeneficiario, null, null,
                            " ",null,null," "," "," "," "," ",""," "));

                }
            }
        }

        if (!ordenesDto.isEmpty()) {
            try {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                OutputStreamWriter streamWriter = new OutputStreamWriter(stream, Charset.forName(StandardCharsets.UTF_8.name()));
                CSVWriter writer = new CSVWriter(streamWriter, ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);

                var mappingStrategy = new OBManejoArchivos.CustomColumnPositionStrategy<OrdenDePagoDTO>();
                mappingStrategy.setType(OrdenDePagoDTO.class);

                StatefulBeanToCsv<OrdenDePagoDTO> builder = new StatefulBeanToCsvBuilder<OrdenDePagoDTO>(writer)
                        .withQuotechar(CSVWriter.NO_QUOTE_CHARACTER)
                        .withSeparator(';')
                        .withMappingStrategy(mappingStrategy)
                        .build();

                builder.write(ordenesDto);
                streamWriter.flush();
                writer.close();

                byte[] file = stream.toByteArray();
                contexto.response.header("Content-Disposition", "attachment; filename=ordenes_de_pago-" + Fecha.hoy() + ".csv");
                contexto.response.type("application/csv");

                return file;

            } catch (Exception e) {
                return respuesta("ERROR", "descripcion", "No se pudo descargar el archivo.");
            }

        } else return respuesta("0", "descripcion", "No hay órdenes de pago para descargar.");
    }


}
