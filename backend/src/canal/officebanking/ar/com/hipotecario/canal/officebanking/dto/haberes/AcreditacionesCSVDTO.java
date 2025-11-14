package ar.com.hipotecario.canal.officebanking.dto.haberes;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.math.BigDecimal;

public class AcreditacionesCSVDTO {
    @CsvBindByName(column = "Número Cuenta", required = true)
    @CsvBindByPosition(position = 0)
    public String numeroCuenta;

    @CsvBindByName(column = "CUIL", required = true)
    @CsvBindByPosition(position = 1)
    public Long cuil;

    @CsvBindByName(column = "Nombre completo", required = true)
    @CsvBindByPosition(position = 2)
    public String nombreCompleto;

    @CsvBindByName(column = "Estado", required = true)
    @CsvBindByPosition(position = 3)
    public String estado;

    @CsvBindByName(column = "Importe", required = true)
    @CsvBindByPosition(position = 4)
    public BigDecimal importe;

    @CsvBindByName(column = "Descripción", required = true)
    @CsvBindByPosition(position = 5)
    public String descripcion;

    @CsvBindByName(column = "Causal", required = true)
    @CsvBindByPosition(position = 6)
    public String causal;

    public AcreditacionesCSVDTO(String numeroCuenta, Long cuil, String nombreCompleto, String estado, BigDecimal importe, String descripcion, String causal) {
        this.numeroCuenta = numeroCuenta;
        this.cuil = cuil;
        this.nombreCompleto = nombreCompleto;
        this.estado = estado;
        this.importe = importe;
        this.descripcion = descripcion;
        this.causal = causal;

    }
}
