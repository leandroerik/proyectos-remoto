package ar.com.hipotecario.canal.officebanking;

import java.io.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.recaudaciones.ConveniosRecaOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import com.azure.storage.blob.models.BlobProperties;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.canal.officebanking.OBManejoArchivos.TipoOperacionSamba;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;

public class AzureBlobStorageManager extends ModuloOB {

    private static final String CLASE = AzureBlobStorageManager.class.getSimpleName().toUpperCase();

    public AzureBlobStorageManager(ContextoOB contexto, String connectionString, String containerName) {
        connectionString = contexto.config.string("ob_azure_blob_st_url");
        containerName = contexto.config.string("ob_azure_blob_st_container");
    }
    
    public AzureBlobStorageManager(ContextoOB contexto) {
    	new AzureBlobStorageManager(contexto, contexto.config.string("ob_azure_blob_st_url"),contexto.config.string("ob_azure_blob_st_container"));
    }
    public static BlobServiceClient GetBlobServiceClient(ContextoOB contexto) {
        return Config.esOpenShift() ?
                GetBlobServiceClientServer(contexto) : GetBlobServiceClientDev(contexto);
    }

    public static BlobServiceClient GetBlobServiceClientServer(ContextoOB contexto) {
        //cargo las credenciales y valido
        ClientCertificateCredentialBuilder credenciales = null;
        try {
            credenciales = ObtenerCredencialesAz(contexto);
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - GetBlobServiceClientServer credenciales", String.valueOf(e.toString()), CLASE);
            return null;
        }

        try {
            //obtengo conexion con credenciales - BlobServiceClient
            return GetBlobServiceClient(contexto, credenciales);
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - GetBlobServiceClientServer serviceClient", String.valueOf(e.toString()), CLASE);
            return null;
        }

    }

    private static BlobServiceClient GetBlobServiceClient(ContextoOB contexto, ClientCertificateCredentialBuilder credenciales) {

        String connectionString = contexto.config.string("ob_azure_blob_st_url", null);
        try {
            return new BlobServiceClientBuilder().
                    endpoint(connectionString).
                    credential(credenciales.build()).
                    buildClient();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - GetBlobServiceClient", String.valueOf(e.toString()), CLASE);
            return null;
        }
    }

    private static ClientCertificateCredentialBuilder ObtenerCredencialesAz(ContextoOB contexto) {

        try {
            String pfx = contexto.config.string("ob_azure_st_cert", null);
            String pfxPass = contexto.config.string("ob_azure_st_cert_pass", null);
            String tenantId = contexto.config.string("ob_azure_st_tenant_id", null);
            String clientId = contexto.config.string("ob_azure_st_client_id", null);

            byte[] pfxUnbase64 = java.util.Base64.getDecoder().decode(pfx);
            InputStream is = new ByteArrayInputStream(pfxUnbase64);


            return (new ClientCertificateCredentialBuilder().
                    clientId(clientId).
                    pfxCertificate(is).
                    clientCertificatePassword(pfxPass).
                    tenantId(tenantId));
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - ClientCertificateCredentialBuilder", String.valueOf(e.toString()), CLASE);
            return null;
        }

    }

    public static BlobServiceClient GetBlobServiceClientDev(ContextoOB contexto) {
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        try {
            return
                    new BlobServiceClientBuilder()
                            .endpoint(connectionString)
                            .credential(credential)
                            .buildClient();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - GetBlobServiceClientDev", String.valueOf(e.toString()), CLASE);
            return null;
        }

    }

    public void uploadBlobFromService(ContextoOB contexto, String blobName, InputStream content, long contentLength) {
        uploadBlob(contexto, blobName, content, contentLength);
    }

    private void uploadBlob(ContextoOB contexto, String blobName, InputStream content, long contentLength) {

        BlobClient blobClient = ObtenerblobClientFile(contexto, blobName);
        try {
            //subir archivo
            blobClient.upload(content, contentLength);
//        blobClient.getProperties();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - GetBlobServiceClientServer uploadBlob", String.valueOf(e.toString()), CLASE);
        }
    }

    public BlobClient ObtenerblobClientFile(ContextoOB contexto, String blobName) {

        try {
            BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
            String containerName = contexto.config.string("ob_azure_blob_st_container");
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
            BlobClient blobClient = containerClient.getBlobClient(blobName);
            return blobClient;
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - ObtenerblobClientFile", String.valueOf(e.toString()), CLASE);
            return null;
        }


    }

    //mueve el blob de una carpeta a otra
    public void moveBlob(ContextoOB contexto, String sourceBlobName, String destinationBlobName) {

        BlobClient sourceBlobClient = ObtenerblobClientFile(contexto, sourceBlobName);
        BlobClient destinationBlobClient = ObtenerblobClientFile(contexto, destinationBlobName);

        try {
            destinationBlobClient.copyFromUrl(sourceBlobClient.getBlobUrl());
            sourceBlobClient.delete();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - moveBlob", String.valueOf(e.toString()), CLASE);
            LogOB.evento(contexto, "AZURE BLOB STORAGE - moveBlob", "origen: " + sourceBlobName + " / destino" + destinationBlobName, CLASE);
        }

    }

    public static void moveBlobBetweenContainers(ContextoOB contexto, String blobName, String originalContainer, String destinationContainer){
        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
        String connectionString = contexto.config.string("ob_azure_blob_st_url");
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);



        BlobContainerClient sourceContainerClient = blobServiceClient.getBlobContainerClient(originalContainer);
        BlobContainerClient destinationContainerClient = blobServiceClient.getBlobContainerClient(destinationContainer);
        BlobClient sourceBlobClient = sourceContainerClient.getBlobClient(blobName);

        BlobClient destinationBlobClient = destinationContainerClient.getBlobClient(blobName.replace(contexto.config.string("cx_ruta_en_bandeja"),""));
        destinationBlobClient.beginCopy(sourceBlobClient.getBlobUrl(), null);
        sourceBlobClient.delete();
    }

    public void copyBlob(ContextoOB contexto, String sourceBlobName, String destinationBlobName) {

        BlobClient sourceBlobClient = ObtenerblobClientFile(contexto, sourceBlobName);
        BlobClient destinationBlobClient = ObtenerblobClientFile(contexto, destinationBlobName);
        try {
            destinationBlobClient.copyFromUrl(sourceBlobClient.getBlobUrl());
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - copyBlob", String.valueOf(e.toString()), CLASE);
            LogOB.evento(contexto, "AZURE BLOB STORAGE - copyBlob", "origen: " + sourceBlobName + " / destino" + destinationBlobName, CLASE);
        }

    }

    public byte[] listBlobs(ContextoOB contexto, TipoOperacionSamba productoOB, String path, List<String> fechas, ConveniosRecaOB.ConvenioReca convenio) throws Exception {

        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        String op = productoOB.toString();
        byte[] zip = zipFilteredBlobs(containerClient, path, fechas, op, convenio);

       return zip;

        }



    public  byte[] obtenerListaArchivosPorFecha(ContextoOB contexto, String path, List<String> fechas, OBManejoArchivos.TipoOperacionSamba productoOB, ConveniosRecaOB.ConvenioReca convenio)
    				throws Exception {
    	return listBlobs(contexto, productoOB, path, fechas, convenio);
    }

    public byte[] downloadBlob(ContextoOB contexto, String blobName) {

        BlobClient blobClient = ObtenerblobClientFile(contexto, blobName);

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            blobClient.downloadStream(outputStream);
            return outputStream.toByteArray();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - downloadBlob", String.valueOf(e.toString()), CLASE);
            LogOB.evento(contexto, "AZURE BLOB STORAGE - downloadBlob", "ObtenerblobClientFile:downloadStream " + blobName, CLASE);
            return null;
        }


    }

    public String getBlobContentType(ContextoOB contexto, String blobName) {

        BlobClient blobClient = ObtenerblobClientFile(contexto, blobName);

        BlobProperties properties = blobClient.getProperties();

        return properties.getContentType();

    }


    public void uploadArchivoToAzure(ContextoOB contexto, byte[] archivoBytes, String pathDestino, String nombreArchivo, String idBandeja) {
        BlobClient blobClient = ObtenerblobClientFile(contexto, pathDestino + nombreArchivo);

        try {
            ByteArrayInputStream dataStream = new ByteArrayInputStream(archivoBytes);

            //etiqueta metadata idBandeja
            Map<String, String> etiquetas = new HashMap<>();
            etiquetas.put("idBandeja", idBandeja);

            blobClient.upload(dataStream, archivoBytes.length, false); // El último parámetro indica si se sobrescribe el archivo existente
            blobClient.setMetadata(etiquetas);
            dataStream.close();
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE BLOB STORAGE - uploadArchivoToAzure", String.valueOf(e.toString()), CLASE);
            LogOB.evento(contexto, "AZURE BLOB STORAGE - uploadArchivoToAzure", "origen: " + pathDestino + nombreArchivo, CLASE);

        }
    }

    public void addBlobMetadata(ContextoOB contexto, BlobClient blobClient, String idBandeja) {
        Map<String, String> mapIdBandeja = new HashMap<String, String>();
        mapIdBandeja.put("idBandeja", idBandeja);

        try {
            blobClient.setMetadata(mapIdBandeja);
            System.out.printf("Set metadata completed %n");
        } catch (UnsupportedOperationException error) {
            System.out.printf("Failure while setting metadata %n");
        }
    }

    public BlobClient findBlobByEtiquetaInFolder(ContextoOB contexto, String producto, String idBandeja) throws Exception {
        BlobClient blobClient;
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
        BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("officebanking");

        switch (producto) {
            case "PLAN_SUELDO" -> {
                PagedIterable<BlobItem> items;
                try {
                    items = blobContainerClient.listBlobsByHierarchy(contexto.config.string("ph_ruta_master_files"));
                } catch (Exception e) {
                    LogOB.evento(contexto, "Listar items en Blob Client", String.valueOf(e.toString()), CLASE);
                    throw new Exception("No se pudo obtener el listado de blobs");
                }
                for (BlobItem blobItem : items) {
                    try {
                        blobClient = blobContainerClient.getBlobClient(blobItem.getName());
                        // Verifica si el blob tiene la etiqueta especificada
                        if (blobClient.getProperties().getMetadata() != null && blobClient.getProperties().getMetadata().containsKey("idBandeja") && blobClient.getProperties().getMetadata().get("idBandeja").equals(idBandeja)) {
                            return blobClient;
                        }
                    } catch (Exception e) {
                        LogOB.evento(contexto, "Buscar archivo por metadata idBandeja", String.valueOf(e.toString()), CLASE);
                        throw new Exception("No se pudo obtener el blob especificado por metadata idBandeja");
                    }
                }
            }
        }
        return null;
    }

    public BlobClient findBlobByName(ContextoOB contexto, EnumTipoProductoOB producto, String nombreArchivo) {
        BlobClient blobClient = null;
        String pathDestino;
        switch (producto) {
            case PLAN_SUELDO -> pathDestino = contexto.config.string("ph_ruta_master_files");
            case PAGO_PROVEEDORES -> pathDestino = contexto.config.string("pap_ruta_master_files");
            case COBRANZA_INTEGRAL -> pathDestino = contexto.config.string("ci_ruta_master_files");
            case DEBITO_DIRECTO -> pathDestino = contexto.config.string("dd_ruta_master_files");
            case BENEFICIARIOS -> pathDestino = contexto.config.string("beneficiarios_ruta_master_files");
            case COMERCIO_EXTERIOR -> pathDestino = contexto.config.string("cx_ruta_master_files");
            default -> throw new RuntimeException("Producto incorrecto");
        }
        blobClient = ObtenerblobClientFile(contexto,pathDestino+nombreArchivo);
        return blobClient;
    }

    public List<String> obtenerConveniosConArchivoEnFecha(ContextoOB contexto,HashMap<String,String> convenioGCR,String fecha,String ruta){
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        String searchPath = ruta+fecha+"/";
        List<String> conveniosConArchivo = new ArrayList<>();
        PagedIterable<BlobItem> blobItems = containerClient.listBlobsByHierarchy(searchPath);
        for (BlobItem blob : blobItems) {
            if (convenioGCR.isEmpty()) return conveniosConArchivo;
            for (String convenio : convenioGCR.keySet()) {
                if (blob.getName().contains(convenio)||blob.getName().contains(convenioGCR.get(convenio))){
                    conveniosConArchivo.add(convenio);
                    convenioGCR.remove(convenio);
                    break;
                }
            }
        }


        return conveniosConArchivo;
    }

    public HashMap<String,HashSet<String>> obtenerConveniosConArchivoInicial(ContextoOB contexto,String ruta){
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
        String containerName = contexto.config.string("ob_azure_blob_st_container");
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        PagedIterable<BlobItem> carpetasListado = containerClient.listBlobsByHierarchy(ruta);
        HashMap<String,HashSet<String>> fechaConvenio = new HashMap<>();
        for (BlobItem carpeta : carpetasListado) {
            String nombreCarpeta = carpeta.getName();
            String fecha = nombreCarpeta.substring(nombreCarpeta.indexOf("2"),nombreCarpeta.length()-1);
            HashSet<String> convenios = new HashSet<>();
            for (BlobItem item : containerClient.listBlobsByHierarchy(nombreCarpeta)) {
                String nombreArchivo = item.getName();
                if (nombreArchivo.contains("conv")){
                    int index = nombreArchivo.indexOf("conv") + "conv".length();
                    String convenio = nombreArchivo.substring(index,index+5);
                    if (convenio.matches("\\d+")) {
                        convenios.add(convenio);
                    }

                }
                if (nombreArchivo.contains("sda")){
                    int index = nombreArchivo.indexOf("sda") + "sda".length();
                    String convenio = nombreArchivo.substring(index,index+5);
                    if (convenio.matches("\\d+")) {
                        convenios.add(convenio);
                    }
                }
            }
            fechaConvenio.put(fecha,convenios);
        }
        return fechaConvenio;
    }

    private static void downloadBlobToStream(BlobContainerClient containerClient, String blobName, ByteArrayOutputStream byteArrayOutputStream) throws IOException {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.download(byteArrayOutputStream);

        System.out.println("Archivo descargado: " + blobName.replaceAll("^(?:[^/]+/[^/]+/)(.*)", "$1"));
    }

    
    public  byte[] zipFilteredBlobs(BlobContainerClient containerClient, String pathListados, List<String> fechas, String op, ConveniosRecaOB.ConvenioReca convenio) throws IOException, InterruptedException {
        List<BlobItem> filteredBlobs = new ArrayList<>();
        for (String fecha : fechas){
            String searchPath = pathListados + fecha+"/";

            PagedIterable<BlobItem> blobItems = containerClient.listBlobsByHierarchy(searchPath);


            if (op.equals("DEBITO_DIRECTO")) {
                filteredBlobs.addAll(blobItems
                        .stream()
                        .parallel()
                        .filter(f ->
                                f.getName().contains("conv".concat(StringUtil.agregarCerosAIzquierda(convenio.convenio.toString(),5)))||
                                        f.getName().contains(convenio.grupoRecaudacion)||
                                        f.getName().contains("sda".concat(StringUtil.agregarCerosAIzquierda(convenio.convenio.toString(),5))))
                        .filter(f ->
                                f.getName().contains("nd_aplic") ||
                                        f.getName().contains("rdiarias") ||
                                        f.getName().contains("adhdbcar") ||
                                        f.getName().contains("pundeb") ||
                                        f.getName().contains("sda")||
                                        f.getName().contains("re_lis_rev")
                        )
                        .collect(Collectors.toList()));

            } else {
                filteredBlobs.addAll(blobItems
                        .stream()
                        .parallel()
                        .filter(f ->
                                f.getName().contains(StringUtil.agregarCerosAIzquierda(convenio.convenio.toString(),5))||
                                        f.getName().contains(convenio.grupoRecaudacion)||
                                        f.getName().contains("sda".concat(StringUtil.agregarCerosAIzquierda(convenio.convenio.toString(),5))))
                        .filter(f ->
                                f.getName().contains("refreshcli_proc") ||
                                        f.getName().contains("refreshcli_err") ||
                                        f.getName().contains("rdiarias") ||
                                        f.getName().contains("DPPPdet") ||
                                        f.getName().contains("DPPPdat") ||
                                        f.getName().contains("DPPPtot") ||
                                        f.getName().contains("stock_cheques")
                        )
                        .collect(Collectors.toList()));
            }
        }


        ExecutorService executor = Executors.newFixedThreadPool(10);
        ByteArrayOutputStream zipByteArrayOutputStream = new ByteArrayOutputStream();
        List<Callable<ByteArrayOutputStream>> downloadTasks = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(filteredBlobs.size());
        try (ZipOutputStream zipOut = new ZipOutputStream(zipByteArrayOutputStream)) {

            for (BlobItem blobItem : filteredBlobs) {
                downloadTasks.add(() -> {
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    try {
                        downloadBlobToStream(containerClient, blobItem.getName(), byteArrayOutputStream);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        latch.countDown();
                    }
                    return byteArrayOutputStream;
                });
            }

            List<Future<ByteArrayOutputStream>> results = executor.invokeAll(downloadTasks);

            latch.await();
            if (filteredBlobs.isEmpty()) throw new RuntimeException("NO SE ENCONTRARON ARCHIVOS");
            for (int i = 0; i < filteredBlobs.size(); i++) {
                Future<ByteArrayOutputStream> future = results.get(i);
                ByteArrayOutputStream byteArrayOutputStream = future.get();
                ZipEntry zipEntry = new ZipEntry(filteredBlobs.get(i).getName().replaceAll("^(?:[^/]+/[^/]+/)(.*)", "$1"));
                zipOut.putNextEntry(zipEntry);
                zipOut.write(byteArrayOutputStream.toByteArray());
                zipOut.closeEntry();
                System.out.println("Archivo añadido al ZIP: " + filteredBlobs.get(i).getName().replaceAll("^(?:[^/]+/[^/]+/)(.*)", "$1"));
            }
            zipOut.finish();


        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } finally {
            executor.shutdown();
        }
        return zipByteArrayOutputStream.toByteArray();
//        try {
//            try (
//                    ByteArrayOutputStream fos = new ByteArrayOutputStream();
//                    ZipOutputStream zipOut = new ZipOutputStream(fos)) {
//                boolean seZipeoArchivo = false;
//                for (BlobItem blobItem : filteredBlobs) {
//
//                    String blobName = blobItem.getName();
//                    BlobClient blobClient = containerClient.getBlobClient(blobName);
//                    InputStream blobInputStream = blobClient.openInputStream();
//                    ZipEntry zipEntry = new ZipEntry(blobName.substring(blobName.lastIndexOf("/") + 1));
//                    zipOut.putNextEntry(zipEntry);
//
//                    byte[] bytes = new byte[1024];
//                    int length;
//                    while ((length = blobInputStream.read(bytes)) >= 0) {
//                        zipOut.write(bytes, 0, length);
//                    }
//
//                    blobInputStream.close();
//                    zipOut.closeEntry();
//                    seZipeoArchivo = true;
//                }
//                zipOut.finish();
//                return seZipeoArchivo?fos.toByteArray():null;
//            }
//        }
//            catch (Exception e){
//            return null;
//            }

    		
        }


    public BlobClient findBlobRecaProcesado(ContextoOB contexto, String nombreArchivo, EnumTipoProductoOB producto ) {
        BlobClient blobClient = null;
        String pathDestino = null;
        switch (producto) {
            case COBRANZA_INTEGRAL -> pathDestino = contexto.config.string("ci_ruta_procesado");
            case DEBITO_DIRECTO -> pathDestino = contexto.config.string("dd_ruta_procesado");
            default -> throw new RuntimeException("Producto incorrecto");
        }
        blobClient = ObtenerblobClientFile(contexto,pathDestino+nombreArchivo);
        return blobClient;
    }

    public void deleteBlob(ContextoOB contexto, String sourceBlobName) {
        try {
            BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
            String containerName = contexto.config.string("ob_azure_blob_st_container");
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            for (BlobItem blobItem : containerClient.listBlobsByHierarchy(sourceBlobName)) {
                BlobClient blobClient = containerClient.getBlobClient(blobItem.getName());
                blobClient.delete();
                LogOB.evento(contexto, "AZURE Eliminar", "Eliminado blob: " + blobItem.getName(), CLASE);
            }
        } catch (Exception e) {
            LogOB.evento(contexto, "AZURE - Eliminar", String.valueOf(e.toString()), CLASE);
            LogOB.evento(contexto, "AZURE - Eliminar", "Error eliminando la carpeta: " + sourceBlobName, CLASE);
        }

    }
    public void deleteSingleBlob(ContextoOB contexto, String blobPath){
        try{
            BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
            String containerName = contexto.config.string("ob_azure_blob_st_container");
            BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

            BlobClient blobClient = containerClient.getBlobClient(blobPath);
            boolean blobExists = blobClient.exists();
            if (blobExists) {
                blobClient.delete();
            }
        } catch (Exception e) {
            System.out.println("Ocurrió un error al eliminar el blob: " + e.getMessage());
        }


    }

    public static void subirArchivoContenedor(ContextoOB contexto, String pathDestino, Objeto orden, byte[] pdfBytes) throws IOException {
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);

        BlobContainerClient destinationContainerClient = blobServiceClient.getBlobContainerClient(contexto.config.string("ob_azure_blob_comex_st_container"));
        BlobClient blobClient = destinationContainerClient.getBlobClient(pathDestino+"detalleOP"+ orden.get("datos.nroTRR")+".pdf");



        ByteArrayInputStream dataStream = new ByteArrayInputStream(pdfBytes);
        blobClient.upload(dataStream, pdfBytes.length,false);
        dataStream.close();
    }

    public static void renameBlob(ContextoOB contexto, String containerName, String nombreViejo, String nombreNuevo){
        BlobServiceClient blobServiceClient = GetBlobServiceClient(contexto);
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);

        BlobClient sourceBlob = containerClient.getBlobClient(nombreViejo);
        BlobClient destinationBlob = containerClient.getBlobClient(nombreNuevo);

        destinationBlob.beginCopy(sourceBlob.getBlobUrl(),null);
        sourceBlob.delete();
    }



}

