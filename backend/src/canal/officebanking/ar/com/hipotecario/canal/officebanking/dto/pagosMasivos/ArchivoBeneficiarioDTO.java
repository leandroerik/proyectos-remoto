package ar.com.hipotecario.canal.officebanking.dto.pagosMasivos;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoBeneficiarioDTO {

    public static Header header = new Header();
    public static ArrayList<Body> body = new ArrayList<>();
    public static Trailer trailer = new Trailer();

    public static class Header {
        public String registroId;
        public String idArchivo;
        public String secuencial;
        public String idContenido;
        public String convenio;
        public String subconvenio;
//        public String loteCompleto;
        public String TipoOperatoria;
        public String fechaEjecucion;
        public String cbu;
        public String moneda;

    }

    public static class Trailer {
        public String registroId;
        public String importeTotal;
        public String catidadRegistros;

    }

    public static class Body {
        public String registroId = "";
        public String cuit = "";
        public String cbu = "";
        public String tipoDocumento = "";
        public String nombreBeneficiario = "";
        public String documento = "";
        public String email = "";
    }


    public static Header getHeader(String contenidoArchivo) {
        Header header = new Header();
        try {
            header.registroId = contenidoArchivo.substring(0, 2);
            header.idArchivo = contenidoArchivo.substring(2, 22);
            header.secuencial = contenidoArchivo.substring(22, 29);
            header.idContenido = contenidoArchivo.substring(29, 40);
            header.convenio = contenidoArchivo.substring(40, 47);
            header.subconvenio = contenidoArchivo.substring(47, 54);
            header.TipoOperatoria = contenidoArchivo.substring(55, 56);
            header.fechaEjecucion = contenidoArchivo.substring(56, 64);
            header.cbu = contenidoArchivo.substring(64, 86);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return header;
    }

    public static Trailer getTrailer() {
        return trailer;
    }

    public static List<Body> getBody(Blob archivo) {
        try (InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            body = new ArrayList<>();

            // salto la primer linea que es el header
            br.readLine();
            line = br.readLine();

            while (line != null) {
                String nextLine = br.readLine();
                if (nextLine == null) {
                    Trailer item = new Trailer();
                    item.registroId = line.substring(0, 2).trim();
                    item.importeTotal = line.substring(2, 27).trim();
                    item.catidadRegistros = line.substring(27, 37).trim();
                    trailer = item;
                } else {
                    Body item = new Body();
                    if (line.length()>1601){
                        throw new Exception("El largo de las lineas no puede exceder los 1601 caracteres.");
                    }

                    item.registroId = line.substring(2, 27).trim();
                    item.nombreBeneficiario = line.substring(30, 90);
                    item.cuit = line.substring(93, 104).trim();
                    item.email = line.substring(428, 528).trim().trim();
                    item.cbu = line.substring(878, 900).trim().trim();

                    body.add(item);
                }
                line = nextLine;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }
}
