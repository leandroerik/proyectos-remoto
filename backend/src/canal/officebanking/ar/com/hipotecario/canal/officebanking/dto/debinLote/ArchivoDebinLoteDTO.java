package ar.com.hipotecario.canal.officebanking.dto.debinLote;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoDebinLoteDTO {
    public static Header header = new Header();
    public static List<Body> body = new ArrayList<>();
    public static class Header {
        public String filler;
        public String nroConvenio;
        public String nroServicio;
        public String nroEmpresa;
        public String fechaGeneracion;
        public String importeTotal;
        public String moneda;
        public String tipoMov;
        public String secuencial;
        public String infoMonetaria;
        public String sinUso;
        public String fillerFinal;
    }

    public static class Body {
        public String filler;
        public String nroConvenio;
        public String nroServicio;
        public String codBanco;
        public String codSucCuenta;
        public String tipoCuenta;
        public String cuentaBanc;
        public String moneda;
        public String cuit;
        public String nombreApellido;
        public String importe;
        public String vigencia;
        public String concepto;
        public String idDebito;
        public String fillerFinal;
    }
    public static Header getHeader(String contenidoArchivo) {
        Header header = new Header();
        header.filler = contenidoArchivo.substring(0,1);
        header.nroConvenio = contenidoArchivo.substring(1,6);
        header.nroServicio = contenidoArchivo.substring(6,16);
        header.nroEmpresa = contenidoArchivo.substring(16,21);
        header.fechaGeneracion = contenidoArchivo.substring(21,29);
        header.importeTotal = contenidoArchivo.substring(29,47);
        header.moneda = contenidoArchivo.substring(47,50);
        header.tipoMov = contenidoArchivo.substring(50,52);
        header.secuencial = contenidoArchivo.substring(52,55);
        header.infoMonetaria = contenidoArchivo.substring(55,150);
        header.sinUso = contenidoArchivo.substring(150,219);
        header.fillerFinal = contenidoArchivo.substring(219,220);
        return header;
    }

    public static List<Body> getBody(Blob archivo) {
        try (InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))){
            String line;
            body = new ArrayList<>();
            br.readLine();
            line = br.readLine();
            while (line!=null){
                String nextLine = br.readLine();
                Body item = new Body();
                item.filler = line.substring(0,1);
                item.nroConvenio = line.substring(1,6);
                item.nroServicio = line.substring(6,16);
                item.codBanco = line.substring(16,19);
                item.codSucCuenta = line.substring(19,23);
                item.tipoCuenta = line.substring(23,24);
                item.cuentaBanc = line.substring(24,39);
                item.moneda = line.substring(39,42);
                item.cuit = line.substring(42,53);
                item.nombreApellido = line.substring(53,103);
                item.importe = line.substring(103,116);
                item.vigencia = line.substring(116,118);
                item.concepto = line.substring(118,121);
                item.idDebito = line.substring(121,136);
                item.fillerFinal = line.substring(136,220);
                body.add(item);

                line = nextLine;
            }

        }catch (Exception e){
            e.printStackTrace();
        }
        return body;
    }


}
