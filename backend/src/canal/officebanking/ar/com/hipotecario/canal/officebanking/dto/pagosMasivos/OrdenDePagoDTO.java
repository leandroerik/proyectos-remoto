package ar.com.hipotecario.canal.officebanking.dto.pagosMasivos;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class OrdenDePagoDTO {
    @CsvBindByName(column = "Fecha", required = true)
    @CsvBindByPosition(position = 0)
    public String fecha;

    @CsvBindByName(column = "Nombre beneficiario", required = true)
    @CsvBindByPosition(position = 1)
    public String nombreBeneficiario;

    @CsvBindByName(column = "Cuit beneficiario", required = true)
    @CsvBindByPosition(position = 2)
    public String cuitBeneficiario;

    @CsvBindByName(column = "Medio de pago", required = true)
    @CsvBindByPosition(position = 3)
    public String tipoDePago;

    @CsvBindByName(column = "Monto", required = true)
    @CsvBindByPosition(position = 4)
    public String monto;

    @CsvBindByName(column = "Estado", required = true)
    @CsvBindByPosition(position = 5)
    public String estado;

    @CsvBindByName(column = "Id Pago", required = true)
    @CsvBindByPosition(position = 6)
    public String idPago;

    @CsvBindByName(column = "Numero de orden", required = true)
    @CsvBindByPosition(position = 7)
    public Integer numeroOrden;

    @CsvBindByName(column = "Nro. Comprobante", required = false)
    @CsvBindByPosition(position = 8)
    public String nroComprobante;

    @CsvBindByName(column = "Serie de cheque", required = false)
    @CsvBindByPosition(position = 9)
    public String chequeSerie;

    @CsvBindByName(column = "Numero de cheque", required = false)
    @CsvBindByPosition(position = 10)
    public Integer chequeNumero;

    @CsvBindByName(column = "Cheque cruzado", required = false)
    @CsvBindByPosition(position = 11)
    public String chequeCruzado;

    @CsvBindByName(column = "Cheque no a la orden", required = false)
    @CsvBindByPosition(position = 12)
    public String chequeNoALaOrden;

    @CsvBindByName(column = "Fecha vencimiento cheque", required = false)
    @CsvBindByPosition(position = 13)
    public String chequeFechaVencimiento;

    @CsvBindByName(column = "Fecha diferido cheque", required = false)
    @CsvBindByPosition(position = 14)
    public String chequeFechaDiferido;

    @CsvBindByName(column = "Fecha entrega cheque", required = false)
    @CsvBindByPosition(position = 15)
    public String chequeFechaEntrega;

    @CsvBindByName(column = "Fecha rechazo cheque", required = false)
    @CsvBindByPosition(position = 16)
    public String chequeFechaRechazo;
    @CsvBindByName(column = "Orden pago", required = false)
    @CsvBindByPosition(position = 17)
    public String ordenDePago;

    public OrdenDePagoDTO(String fecha, String nombreBeneficiario, String cuitBeneficiario, String tipoDePago, String monto, String estado, String idPago, Integer numeroOrden, String nroComprobante, String chequeSerie, Integer chequeNumero, String chequeCruzado, String chequeNoALaOrden, String chequeFechaVencimiento, String chequeFechaDiferido, String chequeFechaEntrega, String chequeFechaRechazo,String ordenPago) {
        this.fecha = fecha;
        this.nombreBeneficiario = nombreBeneficiario;
        this.cuitBeneficiario = cuitBeneficiario;
        this.tipoDePago = tipoDePago;
        this.monto = monto;
        this.estado = estado;
        this.idPago = idPago;
        this.numeroOrden = numeroOrden;
        this.nroComprobante = nroComprobante;
        this.chequeSerie = chequeSerie;
        this.chequeNumero = chequeNumero;
        this.chequeCruzado = chequeCruzado;
        this.chequeNoALaOrden = chequeNoALaOrden;
        this.chequeFechaVencimiento = chequeFechaVencimiento;
        this.chequeFechaDiferido = chequeFechaDiferido;
        this.chequeFechaEntrega = chequeFechaEntrega;
        this.chequeFechaRechazo = chequeFechaRechazo;
        this.ordenDePago = ordenPago;
    }
}

