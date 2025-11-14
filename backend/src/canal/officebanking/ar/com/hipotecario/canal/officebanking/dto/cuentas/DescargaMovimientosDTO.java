package ar.com.hipotecario.canal.officebanking.dto.cuentas;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class DescargaMovimientosDTO {

    @CsvBindByName(column = "FECHA",required = true)
    @CsvBindByPosition(position = 0)
    public String fecha;

    @CsvBindByName(column = "DESCRIPCION",required = true)
    @CsvBindByPosition(position = 1)
    public String descripcion;

    @CsvBindByName(column = "SUCURSAL",required = true)
    @CsvBindByPosition(position = 2)
    public String sucursal;

    @CsvBindByName(column = "REFERENCIA",required = true)
    @CsvBindByPosition(position = 3)
    public String referencia;

    @CsvBindByName(column = "DEBITO EN $",required = true)
    @CsvBindByPosition(position = 4)
    public String debito;

    @CsvBindByName(column = "CREDITO EN $",required = true)
    @CsvBindByPosition(position = 5)
    public String credito;

    @CsvBindByName(column = "SALDO EN $",required = true)
    @CsvBindByPosition(position = 6)
    public String saldo;

    public DescargaMovimientosDTO(String fecha, String descripcion, String sucursal, String referencia, String debito, String credito, String saldo) {
        this.fecha = fecha;
        this.descripcion = descripcion;
        this.sucursal = sucursal;
        this.referencia = referencia;
        this.debito = debito;
        this.credito = credito;
        this.saldo = saldo;
    }
}
