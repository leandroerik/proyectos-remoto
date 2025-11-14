package ar.com.hipotecario.canal.officebanking.dto.haberes;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class NominaCSVDTO {
    @CsvBindByName(column = "CUIL/T", required = true)
    @CsvBindByPosition(position = 0)
    private String cuilT;

    @CsvBindByName(column = "Nombre Completo", required = true)
    @CsvBindByPosition(position = 1)
    public String nombreCompleto;

    @CsvBindByName(column = "Cuenta", required = true)
    @CsvBindByPosition(position = 2)
    public String cuenta;

    @CsvBindByName(column = "CBU", required = true)
    @CsvBindByPosition(position = 3)
    public String cbu;

    @CsvBindByName(column = "CTA FCL", required = true)
    @CsvBindByPosition(position = 4)
    public String ctaFcl;

    @CsvBindByName(column = "CBU FCL", required = true)
    @CsvBindByPosition(position = 5)
    public String cbuFcl;

    @CsvBindByName(column = "Estado", required = true)
    @CsvBindByPosition(position = 6)
    public String estado;

    @CsvBindByName(column = "Motivo de Rechazo", required = true)
    @CsvBindByPosition(position = 7)
    public String motivoDeRechazo;

    public NominaCSVDTO(String cuilT, String nombreCompleto, String cuenta, String cbu, String ctaFcl, String cbuFcl, String estado, String motivoDeRechazo) {
        this.cuilT = cuilT;
        this.nombreCompleto = nombreCompleto;
        this.cuenta = cuenta;
        this.cbu = cbu;
        this.ctaFcl = ctaFcl;
        this.cbuFcl = cbuFcl;
        this.estado = estado;
        this.motivoDeRechazo = motivoDeRechazo;
    }
}

