package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.dto;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.*;

import java.util.List;

public class TASPosicionConsolidadaV4DTO extends Objeto {
    public List <TASCuentasPConsV4> cuentas;
    public List <TASCajasSegPConsV4> cajasSeguridad;
    public List <TASInversionesPConsV4> inversiones;
    public List <TASPlazosFijosPConsV4> plazosFijos;
    public List <TASPrestamosPConsV4> prestamos;
    public List <TASProductosPConsV4> productos;
    public List <TASTarjetasCreditoPConsV4> tarjetasCredito;
    public List <TASTarjetasDebitoPConsV4> tarjetasDebito;

    public TASPosicionConsolidadaV4DTO() {
    }

    public List<TASCuentasPConsV4> getCuentas() {
        return cuentas;
    }

    public void setCuentas(List<TASCuentasPConsV4> cuentas) {
        this.cuentas = cuentas;
    }

    public List<TASCajasSegPConsV4> getCajasSeguridad() {
        return cajasSeguridad;
    }

    public void setCajasSeguridad(List<TASCajasSegPConsV4> cajasSeguridad) {
        this.cajasSeguridad = cajasSeguridad;
    }

    public List<TASInversionesPConsV4> getInversiones() {
        return inversiones;
    }

    public void setInversiones(List<TASInversionesPConsV4> inversiones) {
        this.inversiones = inversiones;
    }

    public List<TASPlazosFijosPConsV4> getPlazosFijos() {
        return plazosFijos;
    }

    public void setPlazosFijos(List<TASPlazosFijosPConsV4> plazosFijos) {
        this.plazosFijos = plazosFijos;
    }

    public List<TASPrestamosPConsV4> getPrestamos() {
        return prestamos;
    }

    public void setPrestamos(List<TASPrestamosPConsV4> prestamos) {
        this.prestamos = prestamos;
    }

    public List<TASProductosPConsV4> getProductos() {
        return productos;
    }

    public void setProductos(List<TASProductosPConsV4> productos) {
        this.productos = productos;
    }

    public List<TASTarjetasCreditoPConsV4> getTarjetasCredito() {
        return tarjetasCredito;
    }

    public void setTarjetasCredito(List<TASTarjetasCreditoPConsV4> tarjetasCredito) {
        this.tarjetasCredito = tarjetasCredito;
    }

    public List<TASTarjetasDebitoPConsV4> getTarjetasDebito() {
        return tarjetasDebito;
    }

    public void setTarjetasDebito(List<TASTarjetasDebitoPConsV4> tarjetasDebito) {
        this.tarjetasDebito = tarjetasDebito;
    }
}
