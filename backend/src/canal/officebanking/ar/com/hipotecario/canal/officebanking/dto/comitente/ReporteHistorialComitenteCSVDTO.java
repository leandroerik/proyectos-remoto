package ar.com.hipotecario.canal.officebanking.dto.comitente;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import java.math.BigDecimal;

public class ReporteHistorialComitenteCSVDTO {
    @CsvBindByName(column = "Fecha", required = true)
    @CsvBindByPosition(position = 0)
    public String Fecha;

    @CsvBindByName(column = "Tipo de orden", required = true)
    @CsvBindByPosition(position = 1)
    public String Tipo_De_Orden;

    @CsvBindByName(column = "Especie", required = true)
    @CsvBindByPosition(position = 2)
    public String Especie;

    @CsvBindByName(column = "Cuenta Liquidadora", required = true)
    @CsvBindByPosition(position = 3)
    public String Cuenta_liquidadora;

    @CsvBindByName(column = "%VR", required = true)
    @CsvBindByPosition(position = 4)
    public String VR;

    @CsvBindByName(column = "Cantidad nominal", required = true)
    @CsvBindByPosition(position = 5)
    public String Cantidad_nominal;

    @CsvBindByName(column = "Precio", required = true)
    @CsvBindByPosition(position = 6)
    public BigDecimal Precio;

    @CsvBindByName(column = "Monto $", required = true)
    @CsvBindByPosition(position = 7)
    public String Monto_Pesos;

    @CsvBindByName(column = "Monto USD", required = true)
    @CsvBindByPosition(position = 8)
    public String Monto_Dolares;

    public ReporteHistorialComitenteCSVDTO(String fecha, String tipoDeOrden, String especie, String cuentaLiquidadora, String vr, BigDecimal cantidadNominal, BigDecimal precio, BigDecimal montoPesos, BigDecimal montoDolares){
        this.Fecha=fecha;
        this.Tipo_De_Orden=tipoDeOrden;
        this.Especie=especie;
        this.Cuenta_liquidadora=cuentaLiquidadora;
        this.VR=vr;
        this.Cantidad_nominal=String.valueOf(cantidadNominal);
        this.Precio=precio;
        this.Monto_Pesos=String.valueOf(montoPesos);
        this.Monto_Dolares=String.valueOf(montoDolares);
    }
}
