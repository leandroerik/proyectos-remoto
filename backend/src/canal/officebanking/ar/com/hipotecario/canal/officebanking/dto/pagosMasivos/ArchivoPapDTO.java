package ar.com.hipotecario.canal.officebanking.dto.pagosMasivos;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoPapDTO {

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
        public String loteCompleto;
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
        public String referencia = "";
        public String tipoOperatoria = "";
        public String motivo = "";
        public String fechaEjecucion = "";
        public String medioPago = ""; // Los medios disponibles, son: ‘002’ = Pago con cheque Normal ‘003’ = Pago con cheque Diferido ‘009’ = Transferencia. ‘012’ = Pago con eCheq Normal ‘013’ = Pago con eCheq Diferido
        public String importe = "";
        public String moneda = "";
        public String comprobante = "";
        public String CBUDebito = "";
        public String nroBeneficiario = "";
        public String nombreBeneficiario = "";
        public String tipoDocumento = "";
        public String documento = "";
        public String medioComunicacion = "";
        public String mensaje = "";
        public String email = "";
        public String tipoCuentaCredito = "";
        public String monedaCuentaCredito = "";
        public String cbuCredito = "";
        public String nombreCuentaAcreditar = "";
        public String canalEntrega = "";
        public String sucursalEnviar = "";
        public String cruzarCheque = "";
        public String incluirFirma = "";
        public String clausula = "";
        public String fechaVencimiento = "";
        public String dniAutorizado = "";
        public String nombreApellidoAutorizado = "";
        public String dniAutorizado2 = "";
        public String nombreApellidoAutorizado2 = "";
        public String dniAutorizado3 = "";
        public String nombreApellidoAutorizado3 = "";
        public String requerirReciboOficial = "";
        public String nroCheque= "";
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
            header.loteCompleto = contenidoArchivo.substring(54, 55);
            header.TipoOperatoria = contenidoArchivo.substring(55, 56);
            header.fechaEjecucion = contenidoArchivo.substring(56, 64);
            header.cbu = contenidoArchivo.substring(64, 86);
            header.moneda = contenidoArchivo.substring(86, 89);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return header;
    }

    public static Trailer getTrailer() {
        return trailer;
    }

    public static List<Body> getBody(byte[] archivoBytes, String encoding) {
        try (//InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(archivoBytes), encoding))) {
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
                    if (line.length()>1602){
                        throw new Exception("El largo de las lineas no puede exceder los 1601 caracteres.");
                    }
                    item.registroId = line.substring(0, 2).trim();
                    item.referencia = line.substring(2, 27).trim();
                    item.tipoOperatoria = line.substring(27, 28).trim();
                    item.motivo = line.substring(28, 284).trim();
                    item.fechaEjecucion = line.substring(284, 292).trim();
                    item.medioPago = line.substring(292, 295).trim();
                    item.importe = line.substring(295, 310).trim();
                    item.moneda = line.substring(310, 313).trim();
                    item.comprobante = line.substring(313, 316).trim();
                    item.CBUDebito = line.substring(316, 338).trim();
                    item.nroBeneficiario = line.substring(338, 363).trim();
                    item.nombreBeneficiario = line.substring(363, 423).trim();
                    item.tipoDocumento = line.substring(423, 426).trim();
                    item.documento = line.substring(426, 437).trim();
                    item.medioComunicacion = line.substring(437, 440).trim();
                    item.mensaje = line.substring(440, 560).trim();
                    item.email = line.substring(560, 660).trim().trim();
                    item.cbuCredito = line.substring(665, 687).trim();
                    if(!item.nroBeneficiario.isBlank()){
                        try{
                            item.tipoCuentaCredito = line.substring(660, 662).trim();
                            item.monedaCuentaCredito = line.substring(662, 665).trim();
                            item.nombreCuentaAcreditar = line.substring(687, 747).trim();
                            item.canalEntrega = line.substring(747, 750).trim();
                            item.sucursalEnviar = line.substring(750, 758).trim();
                            item.incluirFirma = line.substring(758, 759).trim();
                            item.cruzarCheque = line.substring(759, 760).trim();
                            item.clausula = line.substring(760, 761).trim();
                            item.fechaVencimiento = line.substring(761, 769).trim();
                            item.dniAutorizado = line.substring(769, 794).trim();
                            item.nombreApellidoAutorizado = line.substring(794, 854).trim();
                            item.dniAutorizado2 = line.substring(857, 882).trim();
                            item.nombreApellidoAutorizado2 = line.substring(882, 942).trim();
                            item.dniAutorizado3 = line.substring(945, 970).trim();
                            item.nombreApellidoAutorizado3 = line.substring(970, 1030).trim();
                            item.requerirReciboOficial = line.substring(1033, 1034).trim();
                            item.nroCheque = line.substring(1549, 1556).trim();
                        }catch (Exception ignored){}
                    } else {
                        try{
                            item.tipoCuentaCredito = line.substring(660, 662).trim();
                            item.monedaCuentaCredito = line.substring(662, 665).trim();
                            item.nombreCuentaAcreditar = line.substring(687, 747).trim();
                            item.canalEntrega = line.substring(747, 750).trim();
                            item.sucursalEnviar = line.substring(750, 758).trim();
                            item.incluirFirma = line.substring(758, 759).trim();
                            item.cruzarCheque = line.substring(759, 760).trim();
                            item.clausula = line.substring(760, 761).trim();
                            item.fechaVencimiento = line.substring(761, 769).trim();
                            item.dniAutorizado = line.substring(769, 794).trim();
                            item.nombreApellidoAutorizado = line.substring(794, 854).trim();
                            item.dniAutorizado2 = line.substring(857, 882).trim();
                            item.nombreApellidoAutorizado2 = line.substring(882, 942).trim();
                            item.dniAutorizado3 = line.substring(945, 970).trim();
                            item.nombreApellidoAutorizado3 = line.substring(970, 1030).trim();
                            item.requerirReciboOficial = line.substring(1033, 1034).trim();
                            item.nroCheque = line.substring(1549, 1556).trim();
                        }catch (Exception ignored){

                        }

                    }


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
