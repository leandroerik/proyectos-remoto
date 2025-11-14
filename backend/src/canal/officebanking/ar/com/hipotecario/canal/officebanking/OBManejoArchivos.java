package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Servidor;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.homebanking.lib.Archivo;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ParametroOB;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.utils.IOUtils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OBManejoArchivos extends ModuloOB {

    private static Logger log = LoggerFactory.getLogger(Servidor.class);

    public enum TipoOperacionSamba {
        PS_ALTAMASIVA, PS_ACREDITACION, RECAUDACIONES_ABM, PAGO_PROVEEDORES, DEBITO_DIRECTO;
    }

    private static String username = null;
    private static String password = null;
    private static String domain = null;

    public static void subirArchivoAURL(ContextoOB contexto, byte[] archivo, String pathDestino, String nombreArchivo, TipoOperacionSamba op)
            throws Exception {

        String path = "smb:" + pathDestino + "/" + nombreArchivo;
        log.info("Enviando archivo " + path);

        SmbFileOutputStream out = null;

        try {
            NtlmPasswordAuthentication credenciales = obtenerCredenciales(contexto, op);

            SmbFile file = new SmbFile(path, credenciales);
            out = new SmbFileOutputStream(file);
            out.write(archivo);

            log.info("Archivo Enviado Correctamente: " + path);
        } catch (Exception e) {
            throw new Exception(e.getMessage());
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static NtlmPasswordAuthentication obtenerCredenciales(ContextoOB contexto, TipoOperacionSamba op) {
        switch (op) {
            case PS_ACREDITACION:
                username = contexto.config.string("ph_username_acreditaciones");
                domain = contexto.config.string("ph_domain_acreditaciones");
                password = contexto.config.string("ph_password_acreditaciones");
                break;
            case PAGO_PROVEEDORES:
                username = contexto.config.string("ob_username_pap");
                domain = contexto.config.string("ob_domain_pap");
                password = contexto.config.string("ob_password_pap");
                break;
            case DEBITO_DIRECTO:
                username = contexto.config.string("ob_username_debito_directo");
                domain = contexto.config.string("ob_domain_debito_directo");
                password = contexto.config.string("ob_password_debito_directo");
                break;
        }

        return new NtlmPasswordAuthentication(domain, username, password);
    }

    public static Blob inputStreamToBlobUTF8(InputStream inputStream) throws SQLException {
        // Crear un array de bytes para almacenar los datos del InputStream
        byte[] bytes = new byte[1024];
        int bytesRead;
        StringBuilder stringBuilder = new StringBuilder();

        // Leer el InputStream y escribir los bytes en StringBuilder
        try {
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, bytesRead));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convertir los bytes a un array de bytes
        byte[] data = stringBuilder.toString().getBytes();

        // Crear un objeto Blob a partir del array de bytes
        return new SerialBlob(data);
    }

    public static Blob inputStreamToBlobANSI(InputStream inputStream) throws SQLException {
        // Crear un array de bytes para almacenar los datos del InputStream
        byte[] bytes = new byte[1024];
        int bytesRead;
        StringBuilder stringBuilder = new StringBuilder();

        // Leer el InputStream y escribir los bytes en StringBuilder
        try {
            while ((bytesRead = inputStream.read(bytes)) != -1) {
                stringBuilder.append(new String(bytes, 0, bytesRead, Charset.forName("Cp1252")));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Convertir los bytes a un array de bytes
        byte[] data = stringBuilder.toString().getBytes();

        // Crear un objeto Blob a partir del array de bytes
        return new SerialBlob(data);
    }


    public static String blobToString(Blob blob) throws SQLException, IOException {
        // Convertir Blob a String
        if (blob == null) {
            return null;
        }

        try (InputStream inputStream = blob.getBinaryStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {

            StringBuilder stringBuilder = new StringBuilder();
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }

            return stringBuilder.toString();
        }

    }


    public static byte[] descargarTemplate(ContextoOB contexto, Integer producto) {
        ServicioParametroOB servicioParametroOB = new ServicioParametroOB(contexto);

        ParametroOB nombreArchivo = null;
        ParametroOB volumen = null;

        switch (producto) {
            case 1:
                nombreArchivo = servicioParametroOB.find("ph.nombre.archivo.acreditaciones").get();
                volumen = servicioParametroOB.find("ph.ruta.acreditaciones").get();
                break;
            case 14:
                nombreArchivo = servicioParametroOB.find("ph.nombre.archivo.nomina").get();
                volumen = servicioParametroOB.find("ph.ruta.nomina").get();
                break;
            case 2:
                nombreArchivo = servicioParametroOB.find("pp.nombre.archivo.pap").get();
                volumen = servicioParametroOB.find("pp.ruta.pap").get();
                break;
            case 6:
                nombreArchivo = servicioParametroOB.find("dd.nombre.archivo.debitoDirecto").get();
                volumen = servicioParametroOB.find("dd.ruta.debitoDirecto").get();
                break;
            case 17:
                nombreArchivo = servicioParametroOB.find("ci.nombre.archivo.CobranzaIntegral").get();
                volumen = servicioParametroOB.find("ci.ruta.CobranzaIntegral").get();
                break;
            case 18:
                nombreArchivo = servicioParametroOB.find("ci.nombre.archivo.DebinPorLotes").get();
                volumen = servicioParametroOB.find("ci.ruta.DebinPorLotes").get();
                break;
            case 104:
                nombreArchivo = servicioParametroOB.find("ci.nombre.archivo.CobranzaIntegral").get();
                volumen = servicioParametroOB.find("ci.ruta.CobranzaIntegral").get();
                break;
        }

        byte[] file = Archivo.leerBinario(volumen.valor + nombreArchivo.valor);

        // Configuración de la respuesta para la descarga del archivo ZIP
        contexto.response.type("application/zip");
        contexto.response.header("Access-Control-Expose-Headers", "Content-Disposition");
        contexto.response.header("Content-Disposition", nombreArchivo.valor);

        return file;
    }

    protected static <T> List<T> getPage(List<T> sourceList, int page, int pageSize) {
        if (pageSize <= 0 || page <= 0) {
            throw new IllegalArgumentException("invalid page size: " + pageSize);
        }

        int fromIndex = (page - 1) * pageSize;
        if (sourceList == null || sourceList.size() <= fromIndex) {
            return Collections.emptyList();
        }

        // toIndex exclusive
        return sourceList.subList(fromIndex, Math.min(fromIndex + pageSize, sourceList.size()));
    }

    public static class CustomColumnPositionStrategy<T> extends ColumnPositionMappingStrategy<T> {
        @Override
        public String[] generateHeader(T bean) throws CsvRequiredFieldEmptyException {
            super.generateHeader(bean);
            return super.getColumnMapping();
        }
    }

    public static List<Objeto> obtenerListaArchivosPorFecha(ContextoOB contexto, String path, final LocalDate fecha, OBManejoArchivos.TipoOperacionSamba op, String convenio) throws Exception {

        NtlmPasswordAuthentication credenciales = obtenerCredenciales(contexto, op);
        ArrayList<Archivo> archivos;
        String resultado = "OK";

        SmbFile[] fArr;
        byte[] pdfs;
        List<Objeto> listaBytes;
        try {
            SmbFile smbFiles = new SmbFile(path + fecha + "/", credenciales, SmbFile.FILE_SHARE_READ);
            fArr = smbFiles.listFiles();
            List<SmbFile> filtrados = Arrays.stream(fArr).toList().stream().filter(
                            f -> f.getName().contains(convenio))
                    .filter(f -> op == TipoOperacionSamba.DEBITO_DIRECTO ?
                            (f.getName().contains("nd_aplic") ||
                                    f.getName().contains("rdiarias") ||
                                    f.getName().contains("adhdbcar") ||
                                    f.getName().contains("pundeb") ||
                                    f.getName().contains("sda")) :
                            (f.getName().contains("refreshcli_proc") ||
                                    f.getName().contains("refreshcli_err") ||
                                    f.getName().contains("DPPPdet") ||
                                    f.getName().contains("DPPPdat") ||
                                    f.getName().contains("DPPPtot") ||
                                    f.getName().contains("stock_cheques"))
                    ).toList();

            Objeto archivo = null;
            listaBytes = new ArrayList<>();
            for (SmbFile smbFile : filtrados) {
                InputStream input;
                byte[] data = null;
                try {
                    archivo = new Objeto();
                    input = smbFile.getInputStream();
                    data = IOUtils.toByteArray(input);
                    archivo.set("nombre", smbFile.getName());
                    archivo.set("data", data);
                   /*
                   esta es una prueba para descargar el archivo en mi pc. y funciono bien
                   asi se envia
                   FileOutputStream fos = new FileOutputStream("C:\\Users\\c06622\\Desktop\\utiles\\"+smbFile.getName());
                    fos.write(data);*/
                    listaBytes.add(archivo);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

            }


        } catch (Exception e) {
            resultado = e.getMessage();
            throw new Exception(e.getMessage());

        } finally {
            try {
                log.info("Acceso a " + path + " status: " + resultado);
            } catch (Exception e) {
                log.warn("Error en logueo smb" + e.getMessage());
            }
        }

        return listaBytes;
    }

    public enum DozzerIdMappings {
        SMBFILE_TO_ARCHIVO("smbFileToArchivo");

        private String value;

        DozzerIdMappings(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public String toString() {
            return value;
        }
    }

}
