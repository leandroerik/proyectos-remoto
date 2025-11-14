package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.PlazoFijoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.PlazoFijoOBRepositorio;

public class ServicioPlazoFijoOB extends ServicioOB {

	private static PlazoFijoOBRepositorio repo;

	public ServicioPlazoFijoOB(ContextoOB contexto) {
		super(contexto);
		repo = new PlazoFijoOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<PlazoFijoOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<PlazoFijoOB> update(PlazoFijoOB plazoFijoOB) {
		plazoFijoOB.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repo.update(plazoFijoOB));
	}

	public Futuro<PlazoFijoOB> enviarPlazoFijo(ContextoOB contexto, Integer idMoneda, BigDecimal monto, EstadoCedipOB estadoEnBandeja, TipoProductoFirmaOB tipoProductoFirmaOB, EstadoBandejaOB estadoBandejaOB) {
		SesionOB sesion = contexto.sesion();

		EmpresaOB empresaOB = sesion.empresaOB;

		LocalDateTime ahora = LocalDateTime.now();

		ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
		MonedaOB moneda = servicioMoneda.find(idMoneda).get();
		
		//creacion del CEDIP en Bandeja
		PlazoFijoOB plazoFijo = new PlazoFijoOB();
		
		plazoFijo.canal = 0;
		plazoFijo.usuarioAlta = sesion.usuarioOB.codigo.toString();
		plazoFijo.fecha_accion = ahora;
		plazoFijo.accion = "nuevo";
		plazoFijo.idcliente = Integer.valueOf(sesion.empresaOB.idCobis);
		plazoFijo.monto = monto;
		plazoFijo.moneda = moneda;
		plazoFijo.cuentaOrigen = contexto.parametros.string("cuenta");
		plazoFijo.estado_plazo_fijo = estadoEnBandeja;
		plazoFijo.estado_firma = "Pendiente de Firma";
		plazoFijo.estadoBandeja = estadoBandejaOB;
		plazoFijo.capInteres = contexto.parametros.string("capInteres");
		plazoFijo.periodo = contexto.parametros.integer("periodo");
		plazoFijo.plazo = contexto.parametros.integer("plazo");
		plazoFijo.cuenta = contexto.parametros.string("cuenta");
		plazoFijo.tipoCuenta = contexto.parametros.string("tipoCuenta");
		if (contexto.parametros.string("cuenta").charAt(0) == '4') {
			plazoFijo.tipoCuenta = "AHO";
		} else {
			plazoFijo.tipoCuenta = contexto.parametros.string("tipoCuenta");			
		}
		plazoFijo.renova = contexto.parametros.string("renova");
		plazoFijo.tipoOperacion = contexto.parametros.string("tipoOperacion");
		plazoFijo.tipoProductoFirma = tipoProductoFirmaOB;
		plazoFijo.fechaUltActulizacion = ahora;
		if (empresaOB != null) {
			plazoFijo.empresa = empresaOB;			
		}

		return futuro(() -> repo.create(plazoFijo));
	}

}