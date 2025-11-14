package ar.com.hipotecario.canal.officebanking.jpa.dto.pap;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ArchivoPAPItem {

    private String registroID;
    private Double importe;
    private Integer totalRegistros;

    public String getRegistroID() {
        return registroID;
    }

    public void setRegistroID(String registroID) {
        this.registroID = registroID;
    }

    public Double getImporte() {
        return importe;
    }

    public void setImporte(Double importe) {
        this.importe = importe;
    }

    public Integer getTotalRegistros() {
        return totalRegistros;
    }

    public void setTotalRegistros(Integer totalRegistros) {
        this.totalRegistros = totalRegistros;
    }

//    public static ArchivoPAPItem readPAPItemTotalesFromFile(Blob archivo) {
    public static ArchivoPAPItem readPAPItemTotalesFromFile(byte[] archivoBytes, String encoding) {
        ArchivoPAPItem item = new ArchivoPAPItem();
        int contadorPO = 0;

//        try (InputStream inputStream = archivo.getBinaryStream();
		  try (BufferedReader br = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(archivoBytes), encoding))) {
            String line;

            //salto la primer linea que es el header
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.startsWith("PO")) {
                    contadorPO++;
                }
                if (line.startsWith("FT")) {
                    item = new ArchivoPAPItem();

                    String registroId = line.substring(0, 2).trim();
                    String importe = line.substring(3, 27).trim();
                    String totalRegistro = String.valueOf(contadorPO);

                    item.setRegistroID(registroId);
                    item.setImporte(Double.parseDouble(importe));
                    item.setTotalRegistros(Integer.valueOf(totalRegistro));
                    return item;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return item;
    }
}
