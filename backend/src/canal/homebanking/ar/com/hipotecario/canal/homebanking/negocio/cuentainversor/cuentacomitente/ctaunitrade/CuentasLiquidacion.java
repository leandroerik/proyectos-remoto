package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.ctaunitrade;

import com.fasterxml.jackson.annotation.JsonInclude;

public class CuentasLiquidacion {
    private Pesos peso;
    private Dolares dolares;

    public CuentasLiquidacion() {
    }
    public Pesos getPeso() {
        return peso;
    }

    public void setPeso(Pesos peso) {
        this.peso = peso;
    }

    public Dolares getDolares() {
        return dolares;
    }

    public void setDolares(Dolares dolares) {
        this.dolares = dolares;
    }
}
