package ar.com.hipotecario.canal.officebanking.jpa.dto.haberes;

import ar.com.hipotecario.canal.officebanking.OBRegistroImportado;

public class ArchivoAcreditacionHeader extends OBRegistroImportado {

    private String idArchivo;
    private String codRegistroHeader;
    private String cuitEmpresa;
    private String convenio;
    private String fechaGeneracionArchivo;
    private String horaGeneracionArchivo;
    private String sumatoriaTotalImportes;
    private int cantidad;

    public String getIdArchivo() {
        return idArchivo;
    }

    public void setIdArchivo(String idArchivo) {
        this.idArchivo = idArchivo;
    }

    public String getCodRegistroHeader() {
        return codRegistroHeader;
    }

    public void setCodRegistroHeader(String codRegistroHeader) {
        this.codRegistroHeader = codRegistroHeader;
    }

    public String getCuitEmpresa() {
        return cuitEmpresa;
    }

    public void setCuitEmpresa(String cuitEmpresa) {
        this.cuitEmpresa = cuitEmpresa;
    }

    public String getConvenio() {
        return convenio;
    }

    public void setConvenio(String convenio) {
        this.convenio = convenio;
    }

    public String getFechaGeneracionArchivo() {
        return fechaGeneracionArchivo;
    }

    public void setFechaGeneracionArchivo(String fechaGeneracionArchivo) {
        this.fechaGeneracionArchivo = fechaGeneracionArchivo;
    }

    public String getSumatoriaTotalImportes() {
        return sumatoriaTotalImportes;
    }

    public void setSumatoriaTotalImportes(String sumatoriaTotalImportes) {
        this.sumatoriaTotalImportes = sumatoriaTotalImportes;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }

    public String getHoraGeneracionArchivo() {
        return horaGeneracionArchivo;
    }

    public static ArchivoAcreditacionHeader readAcreditacionHeaderFromFile(String contenidoArchivo) {
        ArchivoAcreditacionHeader header = new ArchivoAcreditacionHeader();

        try {
            String cabecera = contenidoArchivo.substring(0, 38);
            header.convenio = cabecera.substring(0, 5);
            header.fechaGeneracionArchivo = cabecera.substring(5, 13);
            header.horaGeneracionArchivo = cabecera.substring(13, 17);
            header.cantidad = Integer.parseInt(cabecera.substring(17, 24));
            header.sumatoriaTotalImportes = cabecera.substring(24, 38);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return header;
    }
}
