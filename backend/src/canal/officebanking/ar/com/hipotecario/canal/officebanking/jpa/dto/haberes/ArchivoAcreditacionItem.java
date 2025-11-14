package ar.com.hipotecario.canal.officebanking.jpa.dto.haberes;

import ar.com.hipotecario.canal.officebanking.OBRegistroImportado;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Blob;
import java.util.ArrayList;
import java.util.List;

public class ArchivoAcreditacionItem extends OBRegistroImportado {

    private String fechaAcreditacion;
    private BigDecimal sueldo;
    private String numeroCuenta;
    private String nombreCuenta;
    private Long cuil;
    private String multiCausal;

    public String getFechaAcreditacion() {
        return fechaAcreditacion;
    }

    public void setFechaAcreditacion(String fechaAcreditacion) {
        this.fechaAcreditacion = fechaAcreditacion;
    }

    public BigDecimal getSueldo() {
        return sueldo;
    }

    public void setSueldo(BigDecimal sueldo) {
        this.sueldo = sueldo;
    }

    public String getNumeroCuenta() {
        return numeroCuenta;
    }

    public void setNumeroCuenta(String numeroCuenta) {
        this.numeroCuenta = numeroCuenta;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public Long getCuil() {
        return cuil;
    }

    public void setCuil(Long cuil) {
        this.cuil = cuil;
    }

    public String getMultiCausal() {
        return multiCausal;
    }

    public void setMultiCausal(String multiCausal) {
        this.multiCausal = multiCausal;
    }

    public static List<ArchivoAcreditacionItem> readAcreditacionItemFromFileEnt(Blob archivo) {
        List<ArchivoAcreditacionItem> acreditacionItems = new ArrayList<>();

        try (InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            ArchivoAcreditacionItem item;

            // salto la primer linea que es el header
            br.readLine();

            while ((line = br.readLine()) != null) {
                item = new ArchivoAcreditacionItem();
                String fechaAcreditacion = line.substring(0, 8).trim();
                String sueldo = line.substring(8, 17).trim();
                String num_cuenta = line.substring(17, 39).trim();
                String nom_cuenta_CBU = line.substring(39, 61).trim();
                String cuil = line.substring(61, 72).trim();
                String multicausal = line.substring(72, 75);


                item.setFechaAcreditacion(fechaAcreditacion);
                item.setSueldo(new BigDecimal(sueldo));
                item.setCuil(Long.parseLong(cuil));
                item.setNumeroCuenta(num_cuenta);
                item.setNombreCuenta(nom_cuenta_CBU);
                item.setMultiCausal(multicausal);
                acreditacionItems.add(item);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return acreditacionItems;
    }

    public static List<ArchivoAcreditacionItem> readAcreditacionItemFromFileEnx(Blob archivo) {
        List<ArchivoAcreditacionItem> acreditacionItems = new ArrayList<>();

        try (InputStream inputStream = archivo.getBinaryStream();
             BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
            String line;
            ArchivoAcreditacionItem item;

            // salto la primer linea que es el header
            br.readLine();

            while ((line = br.readLine()) != null) {
                item = new ArchivoAcreditacionItem();

                String fechaAcreditacion = line.substring(0, 8).trim();
                String sueldo = line.substring(8, 21).trim();
                String num_cuenta = line.substring(21, 43).trim();
                String nom_cuenta_CBU = line.substring(43, 65).trim();
                String cuil = line.substring(65, 76).trim();
                String multicausal = line.substring(76, 79);

                item.setFechaAcreditacion(fechaAcreditacion);
                item.setSueldo(new BigDecimal(sueldo));
                item.setCuil(Long.parseLong(cuil));
                item.setNumeroCuenta(num_cuenta);
                item.setNombreCuenta(nom_cuenta_CBU);
                item.setMultiCausal(multicausal);
                acreditacionItems.add(item);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return acreditacionItems;
    }
}