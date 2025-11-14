package ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoCobranzaIntegralDTO {
    public static Header header = new Header();
    public static List<Body> body = new ArrayList<>();

    public static class Header {
        public String codigoRegistro;
        public String gcr;
        public String fechaProceso;
        public String horaProceso;
        public String filler;
    }

    public static class Body {
        public String codigoRegistro;
        public String codigoServicio;
        public String numeroDepositante;
        public String numeroComprobante;
        public String tipoDocumento;
        public String numeroDocumento;
        public String fechaDesde;
        public String fechaHasta;
        public String importe1;
        public String fechaVencimiento1;
        public String importe2;
        public String fechaVencimiento2;
        public String importe3;
        public String fechaVencimiento3;
        public String nombreDepositante;
        public String claveInterna;
        public String division;
        public String descripcionDivision;
        public String moneda;
        public String cuitEmpresa;
        public String leyenda1;
        public String leyenda2;
        public String leyenda3;
        public String leyenda4;
        public String leyenda5;
        public String leyenda6;
        public String leyenda7;
        public String filler1;
        public String celularCliente;
        public String mailCliente;
        public String filler2;
    }

    public static class Trailer {
        public String codigoRegistro;
        public String cantRegistros;
        public String filler;
    }

    public static Header getHeader(String contenidoArchivo) {
        Header header = new Header();
        header.codigoRegistro = contenidoArchivo.substring(0, 1);
        header.gcr = contenidoArchivo.substring(1, 11);
        header.fechaProceso = contenidoArchivo.substring(11, 19);
        header.horaProceso = contenidoArchivo.substring(19, 25);
        header.filler = contenidoArchivo.substring(25, 41);

        return header;
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
                Body item = new Body();
                item.codigoRegistro = line.substring(0, 1).trim();
                item.codigoServicio = line.substring(1, 7);
                item.numeroDepositante = line.substring(7, 23);
                item.numeroComprobante = line.substring(23, 41);
                item.tipoDocumento = line.substring(41, 43);
                item.numeroDocumento = line.substring(45, 56);
                item.fechaDesde = line.substring(56, 64);
                item.fechaHasta = line.substring(64, 72);
                item.importe1 = line.substring(72, 87);
                item.fechaVencimiento1 = line.substring(87, 95);
                item.importe2 = line.substring(95, 110);
                item.fechaVencimiento2 = line.substring(110, 118);
                item.importe3 = line.substring(118, 133);
                item.fechaVencimiento3 = line.substring(133, 141);
                item.nombreDepositante = line.substring(141, 171);
                item.claveInterna = line.substring(171, 201);
                item.division = line.substring(201, 207);
                item.descripcionDivision = line.substring(207, 237);
                item.moneda = line.substring(237, 239);
                item.cuitEmpresa = line.substring(239, 250);
                item.leyenda1 = line.substring(250, 288);
                item.leyenda2 = line.substring(288, 326);
                item.leyenda3 = line.substring(326, 364);
                item.leyenda4 = line.substring(364, 402);
                item.leyenda5 = line.substring(402, 440);
                item.leyenda6 = line.substring(440, 478);
                item.leyenda7 = line.substring(478, 516);
                item.filler1 = line.substring(516, 692);
                item.celularCliente = line.substring(692, 732);
                item.mailCliente = line.substring(732, 772);
                item.filler2 = line.substring(772, 775);
                body.add(item);

                line = nextLine;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return body;
    }
}


