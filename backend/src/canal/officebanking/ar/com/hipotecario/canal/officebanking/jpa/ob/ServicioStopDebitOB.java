package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.PagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.StopDebitOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.BandejaOBRepositorio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class ServicioStopDebitOB extends ServicioOB {
    private BandejaOBRepositorio repo;

    public ServicioStopDebitOB(ContextoOB contexto) {
        super(contexto);
        repo = new BandejaOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<BandejaOB> enviarStopDebitBandeja(ContextoOB contexto, String cuenta, BigDecimal monto, int idMoneda, UsuarioOB usuario, EmpresaOB empresa, TipoProductoFirmaOB tipoProductoFirmaOB) {
        BandejaOB stopDebitOB = new BandejaOB();

        LocalDateTime ahora = LocalDateTime.now();

        ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
        MonedaOB moneda = servicioMoneda.find(idMoneda).get();

        stopDebitOB.cuentaOrigen = cuenta;
        stopDebitOB.fechaUltActulizacion = LocalDateTime.now();
        stopDebitOB.monto = monto;

        stopDebitOB.empresa = empresa;
        ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        EstadoBandejaOB estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        stopDebitOB.estadoBandeja = estadoBandeja;

        stopDebitOB.moneda = moneda;

        stopDebitOB.tipoProductoFirma = tipoProductoFirmaOB;


        return futuro(() -> repo.create((BandejaOB) stopDebitOB));
    }

    public Futuro<List<BandejaOB>> findByNumeroCuenta(String cuenta) {
        return futuro(() -> repo.findByField("cuentaOrigen",cuenta));
    }

   public Futuro<BandejaOB> find(int id) {
       return futuro(() -> repo.find(id));
   }
}