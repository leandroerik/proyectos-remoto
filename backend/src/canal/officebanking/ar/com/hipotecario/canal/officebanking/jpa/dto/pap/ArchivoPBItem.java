package ar.com.hipotecario.canal.officebanking.jpa.dto.pap;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Blob;

public class ArchivoPBItem {

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

    public static ArchivoPBItem readPAPItemTotalesFromFile(Blob archivo) {
        ArchivoPBItem item = new ArchivoPBItem();
        int contadorPO = 0;

        try (InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;

            //salto la primer linea que es el header
            br.readLine();

            while ((line = br.readLine()) != null) {
                if (line.startsWith("PB")) {
                    contadorPO++;
                }
                if (line.startsWith("PB")) {
                    item = new ArchivoPBItem();

                    String registroId = line.substring(0, 2).trim();
                    String totalRegistro = String.valueOf(contadorPO);

                    item.setRegistroID(registroId);
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
