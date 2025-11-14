package ar.com.hipotecario.mobile.negocio.cuentainversor.cuentacomitente.ctaunitrade;

import ar.com.hipotecario.mobile.negocio.Cuenta;
import ar.com.hipotecario.mobile.negocio.Domicilio;
import ar.com.hipotecario.mobile.negocio.Persona;
import ar.com.hipotecario.mobile.negocio.Telefono;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class CuentaComitenteUnitrade {

    private String cobis;
    private String sucursal;
    private String razonSocial;
    private String tipoIdTributario;
    private String cuit;
    private String tipoSujeto;
    private String calificacion;
    private String actividad;
    private String situacionGanancia;
    private String condicionIva;
    private String direccion;
    private String telefono;
    private CuentasLiquidacion cuentasLiquidacion;

    public CuentaComitenteUnitrade() {
    }

    public String getCobis() {
        return cobis;
    }

    public void setCobis(String cobis) {
        this.cobis = cobis;
    }

    public String getSucursal() {
        return sucursal;
    }

    public void setSucursal(String sucursal) {
        this.sucursal = sucursal;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getTipoIdTributario() {
        return tipoIdTributario;
    }

    public void setTipoIdTributario(String tipoIdTributario) {
        this.tipoIdTributario = tipoIdTributario;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public String getTipoSujeto() {
        return tipoSujeto;
    }

    public void setTipoSujeto(String tipoSujeto) {
        this.tipoSujeto = tipoSujeto;
    }

    public String getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(String calificacion) {
        this.calificacion = calificacion;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public String getSituacionGanancia() {
        return situacionGanancia;
    }

    public void setSituacionGanancia(String situacionGanancia) {
        this.situacionGanancia = situacionGanancia;
    }

    public String getCondicionIva() {
        return condicionIva;
    }

    public void setCondicionIva(String condicionIva) {
        this.condicionIva = condicionIva;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public CuentasLiquidacion getCuentasLiquidacion() {
        return cuentasLiquidacion;
    }

    public void setCuentasLiquidacion(CuentasLiquidacion cuentasLiquidacion) {
        this.cuentasLiquidacion = cuentasLiquidacion;
    }

    public static CuentaComitenteUnitrade getValue(Persona persona, Domicilio domicilio, Telefono telefono, Cuenta pesos, Cuenta dolar) {
        String calificacion = persona.string("idTipoCliente", "").equals("PS") ? "PA" : persona.string("idTipoCliente", "");


        CuentaComitenteUnitrade unitrade = new CuentaComitenteUnitrade();
        unitrade.setCobis(persona.string("idCliente", null));
        unitrade.setSucursal(persona.sucursal());
        unitrade.setRazonSocial(persona.nombreCompleto());
        unitrade.setTipoIdTributario(persona.tipoTributario());
        unitrade.setCuit(persona.cuit());
        unitrade.setTipoSujeto(persona.string("idSector", ""));
        unitrade.setCalificacion(calificacion);
        unitrade.setActividad("000");
        unitrade.setSituacionGanancia(persona.string("idGanancias", ""));
        unitrade.setCondicionIva("CONF");
        unitrade.setDireccion(domicilio.string("idCore", ""));

        String telefonoUnitrade = telefono.string("idCore", "") + ", " + telefono.string("idDireccion", "");
        unitrade.setTelefono(telefonoUnitrade);

        CuentasLiquidacion cuentasLiquidacion1 = new CuentasLiquidacion();
        Pesos pesos1 = new Pesos();
        pesos1.setMoneda(pesos.string("moneda", ""));
        pesos1.setSucursal(pesos.sucursal());
        pesos1.setTipoCuenta(pesos.idTipo());
        pesos1.setNumero(pesos.numero());
        cuentasLiquidacion1.setPeso(pesos1);

        if (dolar != null) {
            Dolares dolar1 = new Dolares();
            dolar1.setMoneda(dolar.string("moneda", ""));
            dolar1.setSucursal(dolar.sucursal());
            dolar1.setTipoCuenta(dolar.idTipo());
            dolar1.setNumero(dolar.numero());
            cuentasLiquidacion1.setDolares(dolar1);
        }
        unitrade.setCuentasLiquidacion(cuentasLiquidacion1);
        return unitrade;
    }
    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
