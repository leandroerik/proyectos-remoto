package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumAccionesEcheqOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumEstadoEcheqChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqDescuentoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.HistorialEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ECheqDescuentoOBRepositorio;

public class ServicioEcheqDescuentoOB extends ServicioOB {
    private static ECheqDescuentoOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioEstadoEcheqOB servicioEstadoEcheqOB = new ServicioEstadoEcheqOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
    private static ServicioMonedaOB servicioMonedaOB = new ServicioMonedaOB(contexto);
    private static ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);

    public ServicioEcheqDescuentoOB(ContextoOB contexto) {
        super(contexto);
        repo = new ECheqDescuentoOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EcheqDescuentoOB> crear(String cuentaOrigen, BigDecimal monto, String numeroDocumento, Integer tipoDocumento, String estadoCodigo, String solicitudNumero, UsuarioOB usuario, EmpresaOB empresa) {
        EcheqDescuentoOB echeqDescuento = new EcheqDescuentoOB();
        echeqDescuento.numeroDocumento = numeroDocumento;
        echeqDescuento.tipoDocumento = tipoDocumento;
        echeqDescuento.estadoCodigo = estadoCodigo;
        echeqDescuento.solicitudNumero = solicitudNumero;
        echeqDescuento.usuario = usuario;
        echeqDescuento.cuentaOrigen = cuentaOrigen;
        echeqDescuento.monto = monto;
        echeqDescuento.moneda = servicioMonedaOB.find(80).get();
        echeqDescuento.fechaUltActulizacion = LocalDateTime.now();
        echeqDescuento.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.ECHEQ_DESCUENTO.getCodigo()).get();
        echeqDescuento.empresa = empresa;
        echeqDescuento.estado = servicioEstadoEcheqOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        echeqDescuento.estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        echeqDescuento.accion = EnumAccionesEcheqOB.EMISION;

        return futuro(() -> repo.create(echeqDescuento));
    }

    public Futuro<EcheqDescuentoOB> find(int id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<EcheqDescuentoOB> update(EcheqDescuentoOB echeq) {
        echeq.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(echeq));
    }
    
    public Futuro<List<EcheqDescuentoOB>> buscarPorEstado(EstadoEcheqOB estado){
        return futuro(()->repo.buscarPorEstado(estado));
    }

}
