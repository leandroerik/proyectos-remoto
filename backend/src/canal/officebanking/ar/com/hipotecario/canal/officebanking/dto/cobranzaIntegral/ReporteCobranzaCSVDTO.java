package ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class ReporteCobranzaCSVDTO {
    @CsvBindByName(column = "Número de Cuenta", required = true)
    @CsvBindByPosition(position = 0)
    public String numeroDeCuenta;

    @CsvBindByName(column = "Forma de pago", required = true)
    @CsvBindByPosition(position = 1)
    public String formaPago;

    @CsvBindByName(column = "Moneda", required = true)
    @CsvBindByPosition(position = 2)
    public String moneda;

    @CsvBindByName(column = "Importe", required = true)
    @CsvBindByPosition(position = 3)
    public Number importe;

    @CsvBindByName(column = "CUIT emisor cheque", required = true)
    @CsvBindByPosition(position = 4)
    public String cuitEmisorCheque;

    @CsvBindByName(column = "Tipo de cheque", required = true)
    @CsvBindByPosition(position = 5)
    public String tipoCheque;

    @CsvBindByName(column = "Número de cheque", required = true)
    @CsvBindByPosition(position = 6)
    public String nroCheque;

    @CsvBindByName(column = "Estado", required = true)
    @CsvBindByPosition(position = 7)
    public String estado;

    @CsvBindByName(column = "Fecha Emisión", required = true)
    @CsvBindByPosition(position = 8)
    public String fechaEmision;

    @CsvBindByName(column = "Fecha de pago", required = true)
    @CsvBindByPosition(position = 9)
    public String fechaPago;

    @CsvBindByName(column = "Fecha de depósito", required = true)
    @CsvBindByPosition(position = 10)
    public String fechaDeposito;

    @CsvBindByName(column = "Sucursal", required = true)
    @CsvBindByPosition(position = 11)
    public String sucursal;

    @CsvBindByName(column = "Id. Cliente", required = true)
    @CsvBindByPosition(position = 12)
    public String idCliente;

    @CsvBindByName(column = "Nro. Comprobante", required = true)
    @CsvBindByPosition(position = 13)
    public String nroComprobante;

    @CsvBindByName(column = "Nombre Cliente", required = true)
    @CsvBindByPosition(position = 14)
    public String nombreCliente;

    @CsvBindByName(column = "Cuit Cliente", required = true)
    @CsvBindByPosition(position = 15)
    public String cuitCliente;

/*    @CsvBindByName(column = "Número  Operación", required = true)
    @CsvBindByPosition(position = 16)
    public Integer nroOperacion;

    @CsvBindByName(column = "Concepto", required = true)
    @CsvBindByPosition(position = 17)
    public String concepto;

 */

    @CsvBindByName(column = "Número de convenio", required = true)
    @CsvBindByPosition(position = 16)
    public String nroConvenio;

    public ReporteCobranzaCSVDTO(String numeroDeCuenta, String formaPago, String moneda, Number importe, String cuitEmisorCheque, String tipoCheque, String nroCheque, String estado, String fechaEmision, String fechaPago, String fechaDeposito, String sucursal, String idCliente, String nroComprobante, String nombreCliente, String cuitCliente, String nroConvenio) {//, Integer nroOperacion, String concepto) {
        this.numeroDeCuenta = numeroDeCuenta;
        this.formaPago = formaPago;
        this.moneda = moneda;
        this.importe = importe;
        this.cuitEmisorCheque = cuitEmisorCheque;
        this.tipoCheque = tipoCheque;
        this.nroCheque = nroCheque;
        this.estado = estado;
        this.fechaEmision = fechaEmision;
        this.fechaPago = fechaPago;
        this.fechaDeposito = fechaDeposito;
        this.sucursal = sucursal;
        this.idCliente = idCliente;
        this.nroComprobante = nroComprobante;
        this.nombreCliente = nombreCliente;
        this.cuitCliente = cuitCliente;
        //this.nroOperacion = nroOperacion;
        //this.concepto = concepto;
        this.nroConvenio = nroConvenio;
    }
}
