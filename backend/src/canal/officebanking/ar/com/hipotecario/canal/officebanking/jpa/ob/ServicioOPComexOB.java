package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCambioComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumCondicionComexOB;
import ar.com.hipotecario.canal.officebanking.enums.Comex.EnumTipoPersonaComexOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.CategoriaComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.ConceptoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.EstadoOPComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.OrdenPagoComexOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.OPComexOBRepositorio;


public class ServicioOPComexOB extends ServicioOB {
	
	 private static OPComexOBRepositorio repo;
	    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");
	    private static ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
	    private static ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
	    private static ServicioTipoProductoFirmaOB servicioTipoProductoFirma = new ServicioTipoProductoFirmaOB(contexto);
	   
	    public ServicioOPComexOB(ContextoOB contexto) {
	        super(contexto);
	        repo = new OPComexOBRepositorio();
	        repo.setEntityManager(this.getEntityManager());
	    }

	    public Futuro<OrdenPagoComexOB> find(Integer codigo){
	    	return futuro(() -> repo.find(codigo));
	    }
	    
	    public Futuro<List<OrdenPagoComexOB>> listarCuentas(EmpresaOB empresa) {
	        return futuro(() -> repo.listarCuentas(empresa));
	    }
	    
	    public Futuro <OrdenPagoComexOB> buscarPorTRR(String trr){
	    	return futuro(() -> repo.buscarPorTRR(trr));
	    }

	    public Futuro<OrdenPagoComexOB> update(OrdenPagoComexOB ordenPago){
	    	return futuro(() -> repo.update(ordenPago));
	    }

		public Futuro<OrdenPagoComexOB> crearPorEntidad(OrdenPagoComexOB orden){
			return futuro(()-> repo.create(orden));
		}
		    
	    public Futuro<OrdenPagoComexOB> crear(ContextoOB contexto,
											  String cuentaOrigen,
											  BigDecimal monto,
											  CategoriaComexOB categoria,
											  ConceptoComexOB concepto,
											  String razonSocial,
											  String numeroTRR,
											  Character rectificacion,
											  Boolean bienesYservicio,
											  String nroCuentaCreditoPesos,
											  String nroCuentaCredMonedaExt,
											  String url,
											  MonedaOB moneda,
											  BigDecimal montoMonedaExt,
											  MonedaOB simboloMonedaExt,
											  EstadoOPComexOB estado,
											  LocalDateTime fechaCreacion,
											  LocalDateTime fechaModificacion,
											  EnumCambioComexOB cambio,
											  EnumCondicionComexOB condicion,
											  String cuitCuil,
											  EnumTipoPersonaComexOB persona,
											  boolean relacion
                ) {
					OrdenPagoComexOB op = new OrdenPagoComexOB();
					op.cuentaOrigen=cuentaOrigen;
					op.categoria = categoria; 
					op.concepto = concepto; 
					op.razonSocial = razonSocial;
					op.numeroTRR = numeroTRR;
					op.rectificacion = rectificacion;
					op.bienesYservicio = bienesYservicio;
					op.nroCuentaCreditoPesos = nroCuentaCreditoPesos;
					op.nroCuentaCredMonedaExt = nroCuentaCredMonedaExt;
					op.url = url; 
					op.monto = monto; 
					op.moneda = moneda; 
					op.montoMonedaExt = montoMonedaExt; 
					op.simboloMonedaExt = simboloMonedaExt; 
					op.estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();; 
					op.empresa=contexto.sesion().empresaOB;
					op.tipoProductoFirma= servicioTipoProductoFirma.findByCodigo(EnumTipoProductoOB.COMERCIO_EXTERIOR.getCodigo()).get();
					op.fechaCreacion=fechaCreacion;
					op.fechaModificacion=fechaModificacion;
					op.fechaUltActulizacion= LocalDateTime.now();
					op.usuario = contexto.sesion().usuarioOB; 
					op.estado=estado;
					op.cambio = cambio;
					op.condicion = condicion;
					op.cuitCuil = cuitCuil;
					op.persona = persona;
					op.relacion = relacion;
					return futuro(() -> repo.create(op));
	    }
 
	    public Futuro<List<OrdenPagoComexOB>> filtrarOrdenesPagosHistorial(EmpresaOB empresa, String cuenta, Fecha fechaDesde, Fecha fechaHasta, boolean previsualizacion) {
	        return futuro(() -> repo.filtrarOrdenesPagosHistorial(empresa, cuenta, fechaDesde, fechaHasta, previsualizacion));
	    }
}
