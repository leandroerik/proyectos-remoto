package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.debin.EnumEstadoDebinRecibidasOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.ConceptoDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.DebinOBRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ServicioDebinOB extends ServicioOB {

    private static DebinOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
    ServicioEstadoDebinRecibidasOB servicioEstadoDebinRecibidasOB = new ServicioEstadoDebinRecibidasOB(contexto);

    public ServicioDebinOB(ContextoOB contexto) {
        super(contexto);
        repo = new DebinOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<DebinOB> update(DebinOB debinOB) {
        return futuro(() -> repo.update(debinOB));
    }

    public Futuro<DebinOB> enviarSolicitud(ContextoOB contexto, String vencimiento, MonedaOB moneda, BigDecimal monto, ConceptoDebinOB concepto,
                                           String referencia, DebinOB debin, String fechaCreacion, InfoCuentaDTO infoCuentaComprador, String cuentaCredito, String cuitVendedor) {
        SesionOB sesion = contexto.sesion();
        EmpresaOB empresaOB = sesion.empresaOB;

        // Debito
        DebinOB solicitud = new DebinOB();

        solicitud.cbuComprador = debin.cbuComprador;
        solicitud.cuentaComprador = debin.cuentaComprador;
        solicitud.idTributarioComprador = debin.idTributarioComprador;

        solicitud.nombreComprador = debin.nombreComprador;
        solicitud.tipoCuentaComprador = debin.tipoCuentaComprador;

        solicitud.sucursalDescVendedor = debin.sucursalDescVendedor;
        solicitud.sucursalIdVendedor = debin.sucursalIdVendedor;
        solicitud.cuentaVendedor = cuentaCredito;
        solicitud.cuitVendedor = cuitVendedor;

        solicitud.emp_codigo = empresaOB;
        solicitud.concepto = concepto;
        solicitud.fechaCreacion = fechaCreacion;
        solicitud.usuario = sesion.usuarioOB;
        solicitud.estadoRecibida = servicioEstadoDebinRecibidasOB.find(EnumEstadoDebinRecibidasOB.ACEPTAR_O_RECHAZAR_DEBIN.getCodigo()).get();
        solicitud.vencimiento = vencimiento;
        solicitud.referenciaSolicitud = referencia;
        solicitud.idDebin = debin.idDebin;

        solicitud.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.DEBIN.getCodigo()).get();
        solicitud.empresa = empresaOB;
        solicitud.fechaUltActulizacion = LocalDateTime.now();
        if (infoCuentaComprador.numero != null) {
            solicitud.cuentaOrigen = infoCuentaComprador.numero;
        } else solicitud.cuentaOrigen = debin.cuentaOrigen;
        solicitud.monto = monto;
        solicitud.moneda = moneda;

        solicitud.estadoEnviada = debin.estadoEnviada;
        solicitud.idDebin = String.valueOf(debin.idDebin);

        return futuro(() -> repo.create(solicitud));
    }

    public Futuro<DebinOB> find(Integer codigo) {
        return futuro(() -> repo.find(codigo));
    }

    public Futuro<DebinOB> findByIdDebin(String idDebin) {
        return futuro(() -> repo.findByIdDebin(idDebin));
    }

}
