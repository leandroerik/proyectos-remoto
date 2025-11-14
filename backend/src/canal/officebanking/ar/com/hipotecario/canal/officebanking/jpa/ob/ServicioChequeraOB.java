package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.echeq.EnumEstadoEcheqChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.ChequeraOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ChequeraOBRepositorio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ServicioChequeraOB extends ServicioOB {
    private static ChequeraOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioEstadoChequeraOB servicioEstadoChequeraOB = new ServicioEstadoChequeraOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);

    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);

    public ServicioChequeraOB(ContextoOB contexto) {
        super(contexto);
        repo = new ChequeraOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<ChequeraOB> crear(ContextoOB contexto, String tipoChequera, String cuentaOrigen, TipoProductoFirmaOB producto, UsuarioOB usuario) {
        ChequeraOB chequera = new ChequeraOB();

        chequera.fechaCreacion = LocalDate.now();
        chequera.tipoChequera = tipoChequera;
        chequera.estado = servicioEstadoChequeraOB.find(EnumEstadoEcheqChequeraOB.EN_BANDEJA.getCodigo()).get();
        chequera.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
        chequera.tipoProductoFirma = producto;
        chequera.empresa = contexto.sesion().empresaOB;
        chequera.cuentaOrigen = cuentaOrigen;
        chequera.monto = BigDecimal.valueOf(1);
        chequera.moneda = servicioMoneda.find(80).get();
        chequera.fechaUltActulizacion = LocalDateTime.now();
        chequera.usuario =usuario;

        return futuro(() -> repo.create(chequera));
    }

    public Futuro<ChequeraOB> find(Integer id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<ChequeraOB> update(ChequeraOB chequera) {
        chequera.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(chequera));
    }
}
