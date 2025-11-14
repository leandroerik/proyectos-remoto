package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.auditoria.modelos.reversas.*;

import java.util.ArrayList;
import java.util.List;

public class TASAuditoriaAdminContingencia extends Objeto {

    // Reversas
    private List<TASDatosDepositoReversa> depositoReversasAReintentar = new ArrayList();
    private List<TASDatosPagoTarjetaReversa> pagoTarjetaReversasAReintentar = new ArrayList();
    private List<TASDatosPagoPrestamoReversa> pagoPrestamoReversasAReintentar = new ArrayList();

    // Operaciones
    private List<TASDatosPagoPrestamo> pagosPrestamoAReintentar = new ArrayList();

    private List<TASDatosRegistroOperacion> registroOperacionAReintentar = new ArrayList();

    public TASAuditoriaAdminContingencia() {
    }

    public List<TASDatosDepositoReversa> getDepositoReversasAReintentar() {
        return depositoReversasAReintentar;
    }

    public void setDepositoReversasAReintentar(List<TASDatosDepositoReversa> depositoReversasAReintentar) {
        this.depositoReversasAReintentar = depositoReversasAReintentar;
    }

    public List<TASDatosPagoTarjetaReversa> getPagoTarjetaReversasAReintentar() {
        return pagoTarjetaReversasAReintentar;
    }

    public void setPagoTarjetaReversasAReintentar(List<TASDatosPagoTarjetaReversa> pagoTarjetaReversasAReintentar) {
        this.pagoTarjetaReversasAReintentar = pagoTarjetaReversasAReintentar;
    }

    public List<TASDatosPagoPrestamoReversa> getPagoPrestamoReversasAReintentar() {
        return pagoPrestamoReversasAReintentar;
    }

    public void setPagoPrestamoReversasAReintentar(List<TASDatosPagoPrestamoReversa> pagoPrestamoReversasAReintentar) {
        this.pagoPrestamoReversasAReintentar = pagoPrestamoReversasAReintentar;
    }

    public List<TASDatosPagoPrestamo> getPagosPrestamoAReintentar() {
        return pagosPrestamoAReintentar;
    }

    public void setPagosPrestamoAReintentar(List<TASDatosPagoPrestamo> pagosPrestamoAReintentar) {
        this.pagosPrestamoAReintentar = pagosPrestamoAReintentar;
    }

    public List<TASDatosRegistroOperacion> getRegistroOperacionAReintentar() {
        return registroOperacionAReintentar;
    }

    public void setRegistroOperacionAReintentar(List<TASDatosRegistroOperacion> registroOperacionAReintentar) {
        this.registroOperacionAReintentar = registroOperacionAReintentar;
    }
}
