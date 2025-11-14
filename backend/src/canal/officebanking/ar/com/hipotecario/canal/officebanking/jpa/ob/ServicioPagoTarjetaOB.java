package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.enums.pagoTarjeta.EnumEstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EstadoEcheqOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.EstadoPagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.tarjetaCredito.PagoTarjetaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PagoTarjetaOBRepositorio;

public class ServicioPagoTarjetaOB extends ServicioOB {
    private static PagoTarjetaOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
    private static ServicioEstadoPagoTarjetaOB servicioEstadoPagoTarjetaOB = new ServicioEstadoPagoTarjetaOB(contexto);
    private static ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
    private static ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);

    public ServicioPagoTarjetaOB(ContextoOB contexto) {
        super(contexto);
        repo = new PagoTarjetaOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<PagoTarjetaOB> crear(String cuenta, BigDecimal monto, String cuentaTarjeta, String tipoCuenta, 
    									String tipoTarjeta, UsuarioOB usuario, EmpresaOB empresa) {
    	PagoTarjetaOB pagoTarjetaOB = new PagoTarjetaOB();
    	pagoTarjetaOB.cuentaTarjeta = cuentaTarjeta;
    	pagoTarjetaOB.tipoCuenta = tipoCuenta;
    	pagoTarjetaOB.tipoTarjeta = tipoTarjeta;
    	pagoTarjetaOB.usuario = usuario;
    	pagoTarjetaOB.monto = monto;
    	pagoTarjetaOB.fechaUltActulizacion = LocalDateTime.now();
    	pagoTarjetaOB.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.PAGO_TARJETA.getCodigo()).get();
    	pagoTarjetaOB.empresa = empresa;
    	pagoTarjetaOB.estado = servicioEstadoPagoTarjetaOB.find(EnumEstadoPagoTarjetaOB.EN_BANDEJA.getCodigo()).get();
    	pagoTarjetaOB.estadoBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
    	
        return futuro(() -> repo.create(pagoTarjetaOB));
    }

    public Futuro<PagoTarjetaOB> find(int id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<PagoTarjetaOB> update(PagoTarjetaOB pago) {
        pago.fechaUltActulizacion = LocalDateTime.now();
        return futuro(() -> repo.update(pago));
    }
    
    public Futuro<List<PagoTarjetaOB>> buscarPorEstado(EstadoEcheqOB estado){
        return futuro(()->repo.buscarPorEstado(estado));
    }
    
    public Futuro<PagoTarjetaOB> enviarPagoTarjeta(ContextoOB contexto, Integer idMoneda, BigDecimal monto, EstadoPagoTarjetaOB estadoEnBandeja, TipoProductoFirmaOB tipoProductoFirmaOB, EstadoBandejaOB estadoInicialBandeja){
    	SesionOB sesion = contexto.sesion();

		EmpresaOB empresaOB = sesion.empresaOB;

		LocalDateTime ahora = LocalDateTime.now();
		
		ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
		MonedaOB moneda = servicioMoneda.find(idMoneda).get();
		
		PagoTarjetaOB pagoTarjetaOB = new PagoTarjetaOB();
		
		pagoTarjetaOB.cuentaTarjeta = contexto.parametros.string("cuentaTarjeta");
		pagoTarjetaOB.cuentaOrigen = contexto.parametros.string("cuenta");
		pagoTarjetaOB.tipoCuenta = contexto.parametros.string("tipoCuenta");
		pagoTarjetaOB.tipoTarjeta = contexto.parametros.string("tipoTarjeta");
		pagoTarjetaOB.moneda = moneda;
		pagoTarjetaOB.monto = monto;
		pagoTarjetaOB.estado = estadoEnBandeja;
		pagoTarjetaOB.estadoBandeja = estadoInicialBandeja;
		pagoTarjetaOB.tipoProductoFirma = tipoProductoFirmaOB;
		pagoTarjetaOB.fechaUltActulizacion = ahora;
		pagoTarjetaOB.usuario = sesion.usuarioOB;
		if (empresaOB != null) {
			pagoTarjetaOB.empresa = empresaOB;			
		}
		
		return futuro(() -> repo.create(pagoTarjetaOB));
		
    }

}
