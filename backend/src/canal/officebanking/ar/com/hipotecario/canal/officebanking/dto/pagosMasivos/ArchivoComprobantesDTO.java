package ar.com.hipotecario.canal.officebanking.dto.pagosMasivos;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ArchivoComprobantesDTO {

    public static Header header = new Header();

    public static List<HeaderPage> headerPages = new ArrayList<>();
    public static class Header {
        public String registroId;
        public String idArchivo;
        public String secuencial;
        public String idContenido;
        public String convenio;
        public String subconvenio;

    }
    public static class HeaderPage{
        public String registroId;
        public String referenciaPago;
        public String tipoCertificado;
        public String numeroHoja;
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return header;
    }

    public static List<HeaderPage> getHeaderPages(String contenidoArchivo){
        List<HeaderPage> lista = new ArrayList<>();
        Arrays.stream(contenidoArchivo.split("\n")).filter(linea->linea.startsWith("H1")).forEach(cabecera->{
            HeaderPage headerPage = new HeaderPage();
            headerPage.registroId = cabecera.substring(0,2);
            headerPage.referenciaPago = cabecera.substring(2,27);
            headerPage.tipoCertificado = cabecera.substring(27,30);
            headerPage.numeroHoja = cabecera.substring(30,35);
            lista.add(headerPage);
        });
        return lista;

    }

}
