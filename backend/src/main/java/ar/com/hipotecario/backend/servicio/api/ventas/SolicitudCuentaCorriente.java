package ar.com.hipotecario.backend.servicio.api.ventas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.*;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.canal.homebanking.ventas.SolicitudCuentaCorriente.Acuerdo;
import java.util.ArrayList;
import java.util.List;

public class SolicitudCuentaCorriente {

    private String tipoProducto;
    private Integer IdProductoFrontEnd;
    private String TipoOperacion;
    private String ProductoBancario;
    private String Categoria;
    private DomicilioResumen DomicilioResumen;
    private String Oficial;
    private Boolean CobroPrimerMantenimiento;
    private String Moneda;
    private String Origen;
    private String UsoFirma;
    private String Ciclo;
    private Boolean ResumenMagnetico;
    private Integer EmpresaAseguradora;
    private Acuerdo Acuerdo;
    private List<RolIntegrante> Integrantes;
    private CuentaLegal CuentaLegales;
    private String Advertencias;
    private String oficina;
    private String canalVenta;
    private Boolean cuentaCobro;
    private Boolean cuentaGastosPropia;
    private Boolean depositoChequesGranel;
    private String depositoInicial;
    private Boolean documentacionTributaria;
    private DomicilioChequeras domicilioChequeras;
    private String Ganancias;
    private String GanExencion;
    private String GanVencimientoExencion;
    private String Iva;
    private String IvaExencion;
    private String IvaReduccion;
    private String IvaVencimientoExencion;
    private String IvaVencimientoReduccion;
    private String nombreCuenta;
    private String numeroCuentaGastos;
    private Integer oficialVenta;
    private String tipoCuentaGastos;
    private String tipoPromedio;
    private Boolean RechazadoMotor;
    private String IdPaqueteProductos;
    private Integer Id;

    public SolicitudCuentaCorriente(String tipoProducto, Integer idProductoFrontEnd, String tipoOperacion, String productoBancario, String categoria, DomicilioResumen domicilioResumen, String oficial, Boolean cobroPrimerMantenimiento, String moneda, String origen, String usoFirma, String ciclo, Boolean resumenMagnetico, Integer empresaAseguradora, Acuerdo acuerdo, List<RolIntegrante> integrantes, CuentaLegal cuentaLegales, String advertencias, String oficina, String canalVenta, Boolean cuentaCobro, Boolean cuentaGastosPropia, Boolean depositoChequesGranel, String depositoInicial, Boolean documentacionTributaria, DomicilioChequeras domicilioChequeras, String ganancias, String ganExencion, String ganVencimientoExencion, String iva, String ivaExencion, String ivaReduccion, String ivaVencimientoExencion, String ivaVencimientoReduccion, String nombreCuenta, String numeroCuentaGastos, Integer oficialVenta, String tipoCuentaGastos, String tipoPromedio, Boolean rechazadoMotor, String idPaqueteProductos, Integer id) {
        this.tipoProducto = tipoProducto;
        IdProductoFrontEnd = idProductoFrontEnd;
        TipoOperacion = tipoOperacion;
        ProductoBancario = productoBancario;
        Categoria = categoria;
        DomicilioResumen = domicilioResumen;
        Oficial = oficial;
        CobroPrimerMantenimiento = cobroPrimerMantenimiento;
        Moneda = moneda;
        Origen = origen;
        UsoFirma = usoFirma;
        Ciclo = ciclo;
        ResumenMagnetico = resumenMagnetico;
        EmpresaAseguradora = empresaAseguradora;
        Acuerdo = acuerdo;
        Integrantes = integrantes;
        CuentaLegales = cuentaLegales;
        Advertencias = advertencias;
        this.oficina = oficina;
        this.canalVenta = canalVenta;
        this.cuentaCobro = cuentaCobro;
        this.cuentaGastosPropia = cuentaGastosPropia;
        this.depositoChequesGranel = depositoChequesGranel;
        this.depositoInicial = depositoInicial;
        this.documentacionTributaria = documentacionTributaria;
        this.domicilioChequeras = domicilioChequeras;
        Ganancias = ganancias;
        GanExencion = ganExencion;
        GanVencimientoExencion = ganVencimientoExencion;
        Iva = iva;
        IvaExencion = ivaExencion;
        IvaReduccion = ivaReduccion;
        IvaVencimientoExencion = ivaVencimientoExencion;
        IvaVencimientoReduccion = ivaVencimientoReduccion;
        this.nombreCuenta = nombreCuenta;
        this.numeroCuentaGastos = numeroCuentaGastos;
        this.oficialVenta = oficialVenta;
        this.tipoCuentaGastos = tipoCuentaGastos;
        this.tipoPromedio = tipoPromedio;
        RechazadoMotor = rechazadoMotor;
        IdPaqueteProductos = idPaqueteProductos;
        Id = id;
    }

    public String getTipoProducto() {
        return tipoProducto;
    }

    public void setTipoProducto(String tipoProducto) {
        this.tipoProducto = tipoProducto;
    }

    public Integer getIdProductoFrontEnd() {
        return IdProductoFrontEnd;
    }

    public void setIdProductoFrontEnd(Integer idProductoFrontEnd) {
        IdProductoFrontEnd = idProductoFrontEnd;
    }

    public String getTipoOperacion() {
        return TipoOperacion;
    }

    public void setTipoOperacion(String tipoOperacion) {
        TipoOperacion = tipoOperacion;
    }

    public String getProductoBancario() {
        return ProductoBancario;
    }

    public void setProductoBancario(String productoBancario) {
        ProductoBancario = productoBancario;
    }

    public String getCategoria() {
        return Categoria;
    }

    public void setCategoria(String categoria) {
        Categoria = categoria;
    }

    public DomicilioResumen getDomicilioResumen() {
        return DomicilioResumen;
    }

    public void setDomicilioResumen(DomicilioResumen domicilioResumen) {
        DomicilioResumen = domicilioResumen;
    }

    public String getOficial() {
        return Oficial;
    }

    public void setOficial(String oficial) {
        Oficial = oficial;
    }

    public Boolean getCobroPrimerMantenimiento() {
        return CobroPrimerMantenimiento;
    }

    public void setCobroPrimerMantenimiento(Boolean cobroPrimerMantenimiento) {
        CobroPrimerMantenimiento = cobroPrimerMantenimiento;
    }

    public String getMoneda() {
        return Moneda;
    }

    public void setMoneda(String moneda) {
        Moneda = moneda;
    }

    public String getOrigen() {
        return Origen;
    }

    public void setOrigen(String origen) {
        Origen = origen;
    }

    public String getUsoFirma() {
        return UsoFirma;
    }

    public void setUsoFirma(String usoFirma) {
        UsoFirma = usoFirma;
    }

    public String getCiclo() {
        return Ciclo;
    }

    public void setCiclo(String ciclo) {
        Ciclo = ciclo;
    }

    public Boolean getResumenMagnetico() {
        return ResumenMagnetico;
    }

    public void setResumenMagnetico(Boolean resumenMagnetico) {
        ResumenMagnetico = resumenMagnetico;
    }

    public Integer getEmpresaAseguradora() {
        return EmpresaAseguradora;
    }

    public void setEmpresaAseguradora(Integer empresaAseguradora) {
        EmpresaAseguradora = empresaAseguradora;
    }

    public Acuerdo getAcuerdo() {
        return Acuerdo;
    }

    public void setAcuerdo(Acuerdo acuerdo) {
        Acuerdo = acuerdo;
    }

    public List<RolIntegrante> getIntegrantes() {
        return Integrantes;
    }

    public void setIntegrantes(List<RolIntegrante> integrantes) {
        Integrantes = integrantes;
    }

    public CuentaLegal getCuentaLegales() {
        return CuentaLegales;
    }

    public void setCuentaLegales(CuentaLegal cuentaLegales) {
        CuentaLegales = cuentaLegales;
    }

    public String getAdvertencias() {
        return Advertencias;
    }

    public void setAdvertencias(String advertencias) {
        Advertencias = advertencias;
    }

    public String getOficina() {
        return oficina;
    }

    public void setOficina(String oficina) {
        this.oficina = oficina;
    }

    public String getCanalVenta() {
        return canalVenta;
    }

    public void setCanalVenta(String canalVenta) {
        this.canalVenta = canalVenta;
    }

    public Boolean getCuentaCobro() {
        return cuentaCobro;
    }

    public void setCuentaCobro(Boolean cuentaCobro) {
        this.cuentaCobro = cuentaCobro;
    }

    public Boolean getCuentaGastosPropia() {
        return cuentaGastosPropia;
    }

    public void setCuentaGastosPropia(Boolean cuentaGastosPropia) {
        this.cuentaGastosPropia = cuentaGastosPropia;
    }

    public Boolean getDepositoChequesGranel() {
        return depositoChequesGranel;
    }

    public void setDepositoChequesGranel(Boolean depositoChequesGranel) {
        this.depositoChequesGranel = depositoChequesGranel;
    }

    public String getDepositoInicial() {
        return depositoInicial;
    }

    public void setDepositoInicial(String depositoInicial) {
        this.depositoInicial = depositoInicial;
    }

    public Boolean getDocumentacionTributaria() {
        return documentacionTributaria;
    }

    public void setDocumentacionTributaria(Boolean documentacionTributaria) {
        this.documentacionTributaria = documentacionTributaria;
    }

    public DomicilioChequeras getDomicilioChequeras() {
        return domicilioChequeras;
    }

    public void setDomicilioChequeras(DomicilioChequeras domicilioChequeras) {
        this.domicilioChequeras = domicilioChequeras;
    }

    public String getGanancias() {
        return Ganancias;
    }

    public void setGanancias(String ganancias) {
        Ganancias = ganancias;
    }

    public String getGanExencion() {
        return GanExencion;
    }

    public void setGanExencion(String ganExencion) {
        GanExencion = ganExencion;
    }

    public String getGanVencimientoExencion() {
        return GanVencimientoExencion;
    }

    public void setGanVencimientoExencion(String ganVencimientoExencion) {
        GanVencimientoExencion = ganVencimientoExencion;
    }

    public String getIva() {
        return Iva;
    }

    public void setIva(String iva) {
        Iva = iva;
    }

    public String getIvaExencion() {
        return IvaExencion;
    }

    public void setIvaExencion(String ivaExencion) {
        IvaExencion = ivaExencion;
    }

    public String getIvaReduccion() {
        return IvaReduccion;
    }

    public void setIvaReduccion(String ivaReduccion) {
        IvaReduccion = ivaReduccion;
    }

    public String getIvaVencimientoExencion() {
        return IvaVencimientoExencion;
    }

    public void setIvaVencimientoExencion(String ivaVencimientoExencion) {
        IvaVencimientoExencion = ivaVencimientoExencion;
    }

    public String getIvaVencimientoReduccion() {
        return IvaVencimientoReduccion;
    }

    public void setIvaVencimientoReduccion(String ivaVencimientoReduccion) {
        IvaVencimientoReduccion = ivaVencimientoReduccion;
    }

    public String getNombreCuenta() {
        return nombreCuenta;
    }

    public void setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
    }

    public String getNumeroCuentaGastos() {
        return numeroCuentaGastos;
    }

    public void setNumeroCuentaGastos(String numeroCuentaGastos) {
        this.numeroCuentaGastos = numeroCuentaGastos;
    }

    public Integer getOficialVenta() {
        return oficialVenta;
    }

    public void setOficialVenta(Integer oficialVenta) {
        this.oficialVenta = oficialVenta;
    }

    public String getTipoCuentaGastos() {
        return tipoCuentaGastos;
    }

    public void setTipoCuentaGastos(String tipoCuentaGastos) {
        this.tipoCuentaGastos = tipoCuentaGastos;
    }

    public String getTipoPromedio() {
        return tipoPromedio;
    }

    public void setTipoPromedio(String tipoPromedio) {
        this.tipoPromedio = tipoPromedio;
    }

    public Boolean getRechazadoMotor() {
        return RechazadoMotor;
    }

    public void setRechazadoMotor(Boolean rechazadoMotor) {
        RechazadoMotor = rechazadoMotor;
    }

    public String getIdPaqueteProductos() {
        return IdPaqueteProductos;
    }

    public void setIdPaqueteProductos(String idPaqueteProductos) {
        IdPaqueteProductos = idPaqueteProductos;
    }

    public Integer getId() {
        return Id;
    }

    public void setId(Integer id) {
        Id = id;
    }


    public class DomicilioChequeras {
        private String Tipo;
        private Integer SecuencialCobis;
        private Integer Id;

        public DomicilioChequeras(String tipo, Integer secuencialCobis, Integer id) {
            Tipo = tipo;
            SecuencialCobis = secuencialCobis;
            Id = id;
        }

        public String getTipo() {
            return Tipo;
        }

        public void setTipo(String tipo) {
            Tipo = tipo;
        }

        public Integer getSecuencialCobis() {
            return SecuencialCobis;
        }

        public void setSecuencialCobis(Integer secuencialCobis) {
            SecuencialCobis = secuencialCobis;
        }

        public Integer getId() {
            return Id;
        }

        public void setId(Integer id) {
            Id = id;
        }
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

}
