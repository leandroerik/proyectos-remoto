package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

import java.util.List;

public class Cuenta extends ApiObjeto {
    public String alias;
    public String alta;
    public String banco;
    public String categoria;
    public String cbu;
    public String codigo;
    public Boolean contratoTransferencia;
    //public Object cuenta;
    public Boolean cuentaCobro;
    public Integer cuentaSolidaria;
    public String descripcion;
    public Detalle detalle;
    public String endpointId;
    public Boolean esTitular;
    public Estado estado;
    public EstadoComprador estadoComprador;
    public String fechaAlta;
    public String fechaBaja;
    public Integer id;
    public Moneda moneda;
    public String numero;
    public Integer oficial;
    public String paquete;
    public Sucursal sucursal;
    public TarjetaDebito tarjetadebito;
    public String terminal;
    public String tipo;
    public List<String> titulares;
    public String visualizar;

    public Cuenta(String alias, String alta, String banco, String categoria, String cbu, String codigo, Boolean contratoTransferencia, Boolean cuentaCobro, Integer cuentaSolidaria, String descripcion, Detalle detalle, String endpointId, Boolean esTitular, Estado estado, EstadoComprador estadoComprador, String fechaAlta, String fechaBaja, Integer id, Moneda moneda, String numero, Integer oficial, String paquete, Sucursal sucursal, TarjetaDebito tarjetadebito, String terminal, String tipo, List<String> titulares, String visualizar) {
        this.alias = alias;
        this.alta = alta;
        this.banco = banco;
        this.categoria = categoria;
        this.cbu = cbu;
        this.codigo = codigo;
        this.contratoTransferencia = contratoTransferencia;
        this.cuentaCobro = cuentaCobro;
        this.cuentaSolidaria = cuentaSolidaria;
        this.descripcion = descripcion;
        this.detalle = detalle;
        this.endpointId = endpointId;
        this.esTitular = esTitular;
        this.estado = estado;
        this.estadoComprador = estadoComprador;
        this.fechaAlta = fechaAlta;
        this.fechaBaja = fechaBaja;
        this.id = id;
        this.moneda = moneda;
        this.numero = numero;
        this.oficial = oficial;
        this.paquete = paquete;
        this.sucursal = sucursal;
        this.tarjetadebito = tarjetadebito;
        this.terminal = terminal;
        this.tipo = tipo;
        this.titulares = titulares;
        this.visualizar = visualizar;
    }

    public Cuenta(String banco) {
        this.banco = banco;
    }
    
    public Cuenta(String cbu, String numero, String tipo, Moneda moneda, Sucursal sucursal) {
        this.cbu = cbu;
        this.numero = numero;
        this.tipo = tipo;
        this.sucursal = sucursal;
        this.moneda = moneda;
    }

    public Cuenta() {
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getAlta() {
        return alta;
    }

    public void setAlta(String alta) {
        this.alta = alta;
    }

    public String getBanco() {
        return banco;
    }

    public void setBanco(String banco) {
        this.banco = banco;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public String getCbu() {
        return cbu;
    }

    public void setCbu(String cbu) {
        this.cbu = cbu;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public Boolean getContratoTransferencia() {
        return contratoTransferencia;
    }

    public void setContratoTransferencia(Boolean contratoTransferencia) {
        this.contratoTransferencia = contratoTransferencia;
    }

    public Boolean getCuentaCobro() {
        return cuentaCobro;
    }

    public void setCuentaCobro(Boolean cuentaCobro) {
        this.cuentaCobro = cuentaCobro;
    }

    public Integer getCuentaSolidaria() {
        return cuentaSolidaria;
    }

    public void setCuentaSolidaria(Integer cuentaSolidaria) {
        this.cuentaSolidaria = cuentaSolidaria;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Detalle getDetalle() {
        return detalle;
    }

    public void setDetalle(Detalle detalle) {
        this.detalle = detalle;
    }

    public String getEndpointId() {
        return endpointId;
    }

    public void setEndpointId(String endpointId) {
        this.endpointId = endpointId;
    }

    public Boolean getEsTitular() {
        return esTitular;
    }

    public void setEsTitular(Boolean esTitular) {
        this.esTitular = esTitular;
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public EstadoComprador getEstadoComprador() {
        return estadoComprador;
    }

    public void setEstadoComprador(EstadoComprador estadoComprador) {
        this.estadoComprador = estadoComprador;
    }

    public String getFechaAlta() {
        return fechaAlta;
    }

    public void setFechaAlta(String fechaAlta) {
        this.fechaAlta = fechaAlta;
    }

    public String getFechaBaja() {
        return fechaBaja;
    }

    public void setFechaBaja(String fechaBaja) {
        this.fechaBaja = fechaBaja;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Moneda getMoneda() {
        return moneda;
    }

    public void setMoneda(Moneda moneda) {
        this.moneda = moneda;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public Integer getOficial() {
        return oficial;
    }

    public void setOficial(Integer oficial) {
        this.oficial = oficial;
    }

    public String getPaquete() {
        return paquete;
    }

    public void setPaquete(String paquete) {
        this.paquete = paquete;
    }

    public Sucursal getSucursal() {
        return sucursal;
    }

    public void setSucursal(Sucursal sucursal) {
        this.sucursal = sucursal;
    }

    public TarjetaDebito getTarjetadebito() {
        return tarjetadebito;
    }

    public void setTarjetadebito(TarjetaDebito tarjetadebito) {
        this.tarjetadebito = tarjetadebito;
    }

    public String getTerminal() {
        return terminal;
    }

    public void setTerminal(String terminal) {
        this.terminal = terminal;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public List<String> getTitulares() {
        return titulares;
    }

    public void setTitulares(List<String> titulares) {
        this.titulares = titulares;
    }

    public String getVisualizar() {
        return visualizar;
    }

    public void setVisualizar(String visualizar) {
        this.visualizar = visualizar;
    }
}
