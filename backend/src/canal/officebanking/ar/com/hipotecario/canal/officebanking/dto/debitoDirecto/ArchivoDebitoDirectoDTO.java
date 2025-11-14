package ar.com.hipotecario.canal.officebanking.dto.debitoDirecto;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoDebitoDirectoDTO {
    public static Header header = new Header();
    public static ArrayList<Body> body = new ArrayList<>();

    public static class Header {
        public String filler;
        public String convenio;
        public String servicio;
        public String empresa;
        public String fechaGeneracion;

        public String importeTotal;
        public String monedaConvenio;
        public String tipoMovimiento;
        public String secuencial;
        public String infoMonetaria;
        public String sinUso;
        public String fillerFinal;
    }

    public static class Body {
        public String filler;
        public String convenio;
        public String servicio;
        public String empresa;
        public String codigoBanco;
        public String codigoSucursalCuenta;
        public String tipoCuenta;
        public String cuentaBancaria;
        public String idActualCliente;
        public String idDebito;
        public String funcionMovimiento;
        public String codigoMotivoRechazo;
        public String fechaVencimiento;
        public String monedaConvenio;
        public String importeADebitar;
        public String fechaReintentoTopeDevolucion;
        public String importeDebitado;
        public String nuevaSucursalBanco;
        public String nuevoTipoCuenta;
        public String nuevaCuentaBancaria;
        public String nuevoIdCliente;
        public String datosRetorno;
        public String sinUso;
        public String fillerFinal;
    }

    public static Header getHeader(String contenidoArchivo) {
        Header header = new Header();
        try {
            header.filler = contenidoArchivo.substring(0, 1);
            header.convenio = contenidoArchivo.substring(1, 6);
            header.servicio = contenidoArchivo.substring(6, 16);
            header.empresa = contenidoArchivo.substring(16, 21);
            header.fechaGeneracion = contenidoArchivo.substring(21, 29);
            header.importeTotal = contenidoArchivo.substring(29, 47);
            header.monedaConvenio = contenidoArchivo.substring(47, 50);
            header.tipoMovimiento = contenidoArchivo.substring(50, 52);
            header.secuencial = contenidoArchivo.substring(52, 55);
            header.infoMonetaria = contenidoArchivo.substring(55, 150);
            header.sinUso = contenidoArchivo.substring(150, 219);
            header.fillerFinal = contenidoArchivo.substring(219, 220);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
                item.filler = line.substring(0, 1).trim();
                item.convenio = line.substring(1, 6).trim();
                item.servicio = line.substring(6, 16).trim();
                item.empresa = line.substring(16, 21).trim();
                item.codigoBanco = line.substring(21, 24).trim();
                item.codigoSucursalCuenta = line.substring(24, 28).trim();
                item.tipoCuenta = line.substring(28, 29).trim();
                item.cuentaBancaria = line.substring(29, 44).trim();
                item.idActualCliente = line.substring(44, 66).trim();
                item.idDebito = line.substring(66, 81).trim();
                item.funcionMovimiento = line.substring(81, 83).trim();
                item.codigoMotivoRechazo = line.substring(83, 87).trim();
                item.fechaVencimiento = line.substring(87, 95).trim();
                item.monedaConvenio = line.substring(95, 98).trim();
                item.importeADebitar = line.substring(98, 111).trim();
                item.fechaReintentoTopeDevolucion = line.substring(111, 119).trim();
                item.importeDebitado = line.substring(119, 132).trim();
                item.nuevaSucursalBanco = line.substring(132, 136).trim();
                item.nuevoTipoCuenta = line.substring(136, 137).trim();
                item.nuevaCuentaBancaria = line.substring(137, 152).trim();
                item.nuevoIdCliente = line.substring(152, 174).trim();
                item.datosRetorno = line.substring(174, 214).trim();
                item.sinUso = line.substring(214, 219).trim();
                item.fillerFinal = line.substring(219, 220).trim();
                body.add(item);

                line = nextLine;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return body;
    }
}
