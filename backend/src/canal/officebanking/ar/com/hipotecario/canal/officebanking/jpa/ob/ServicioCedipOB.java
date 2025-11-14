package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.CedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.inversiones.EstadoCedipOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CedipAccionesOBRepositorio;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.CedipOBRepositorio;

public class ServicioCedipOB extends ServicioOB {

	private static CedipOBRepositorio repo;
	private static CedipAccionesOBRepositorio repoAcc;

	public ServicioCedipOB(ContextoOB contexto) {
		super(contexto);
		repo = new CedipOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
		repoAcc = new CedipAccionesOBRepositorio();
		repoAcc.setEntityManager(this.getEntityManager());
	}

	public Futuro<CedipOB> find(Integer codigo) {
		return futuro(() -> repo.find(codigo));
	}

	public Futuro<CedipOB> update(CedipOB cedipOB) {
		cedipOB.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repo.update(cedipOB));
	}
	
	public Futuro<CedipAccionesOB> update(CedipAccionesOB cedipOB) {
		cedipOB.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repoAcc.update(cedipOB));
	}
	
	public Futuro<CedipAccionesOB> findAcc(Integer codigo) {
		return futuro(() -> repoAcc.find(codigo));
	}

	public Futuro<CedipAccionesOB> updateAcc(CedipAccionesOB cedipAccionesOB) {
		cedipAccionesOB.fechaUltActulizacion = LocalDateTime.now();
		return futuro(() -> repoAcc.update(cedipAccionesOB));
	}

	public Futuro<CedipOB> enviarCedip(ContextoOB contexto, String accion, Integer idMoneda, BigDecimal monto, EstadoCedipOB estadoEnBandeja, TipoProductoFirmaOB tipoProductoFirmaOB, EstadoBandejaOB estadoBandejaOB) {
		SesionOB sesion = contexto.sesion();

		EmpresaOB empresaOB = sesion.empresaOB;

		LocalDateTime ahora = LocalDateTime.now();

		ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
		MonedaOB moneda = servicioMoneda.find(idMoneda).get();
		
		//creacion del CEDIP en Bandeja
		CedipOB cedip = new CedipOB();
		
		cedip.accion = "cedipNuevo";
		cedip.canal = 0;
		cedip.usuarioAlta = sesion.usuarioOB.codigo.toString();
		cedip.fecha_accion = ahora;
		
		cedip.cedip = true;
		cedip.idcliente = 1125598;
		cedip.monto = monto;
		cedip.montoCedip = monto;
		cedip.moneda = moneda;
		cedip.monedaCedip = moneda.id;
		cedip.cuentaOrigen = contexto.parametros.string("cuenta");
		cedip.estado_cedip = estadoEnBandeja;
		cedip.estado_firma = "Pendiente de Firma";
		cedip.estadoBandeja = estadoBandejaOB;
		cedip.capInteres = contexto.parametros.string("capInteres");
		cedip.cedipCBUAcred = contexto.parametros.string("cedipCBUAcred");
		cedip.idPlanAhorro = contexto.parametros.integer("idPlanAhorro");
		cedip.nroOperacion = contexto.parametros.integer("nroOperacion");
		cedip.periodo = contexto.parametros.integer("periodo");
		cedip.plazo = contexto.parametros.integer("plazo");
		cedip.cedipTipoAcred = contexto.parametros.string("cedipTipoAcred");
		cedip.cuenta = contexto.parametros.string("cuenta");
		cedip.renova = contexto.parametros.string("renova");
		cedip.reverso = contexto.parametros.string("reverso");
		cedip.tipoCuenta = contexto.parametros.string("tipoCuenta");
		cedip.tipoOperacion = contexto.parametros.string("tipoOperacion");
		cedip.tipoProductoFirma = tipoProductoFirmaOB;
		cedip.fechaUltActulizacion = ahora;
		if (empresaOB != null) {
			cedip.empresa = empresaOB;			
		}

		return futuro(() -> repo.create(cedip));
	}
	
	public Futuro<CedipAccionesOB> cedipAcciones(ContextoOB contexto, String accion, String cuenta, Integer idMoneda, Object transmisiones, Object firmantes, Object montos, EstadoCedipOB estadoEnBandeja, TipoProductoFirmaOB tipoProductoFirmaOB, EstadoBandejaOB estadoBandejaOB) {
		SesionOB sesion = contexto.sesion();
		EmpresaOB empresaOB = sesion.empresaOB;
		LocalDateTime ahora = LocalDateTime.now();

		ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
		MonedaOB moneda = servicioMoneda.find(idMoneda).get();
		
	    Objeto objetoFirmante = (Objeto) firmantes;
		String documentoFirmante = null;
	    String tipoDocumentoFirmante = null;
	    List<Objeto> listaFirmante = objetoFirmante.objetos();
	    if (!listaFirmante.isEmpty()) {
	        Objeto firmante = listaFirmante.get(0);	     
	        documentoFirmante = firmante.string("documentoFirmante");
	        tipoDocumentoFirmante = firmante.string("tipoDocumentoFirmante");
	    }
		
		CedipAccionesOB cedip = new CedipAccionesOB();
		
		cedip.accion = accion;
		cedip.canal = 0;
		cedip.usuarioAlta = sesion.usuarioOB.codigo.toString();
		cedip.fecha_accion = ahora;
		cedip.cedip = true;
		String idCliente = empresaOB.idCobis;
		int idClienteInt = Integer.parseInt(idCliente);
		cedip.idcliente = idClienteInt;
		cedip.cuentaOrigen = "0";
		cedip.monto = BigDecimal.ZERO;
		//montoCedip modificarlo en BD ya que no deberia ser obligatorio
		cedip.montoCedip = BigDecimal.ZERO;
		
		cedip.cedipId = contexto.parametros.string("cedipId");
		cedip.cedipCBUAcred = contexto.parametros.existe("cbuAcreditar") ? cedip.cedipCBUAcred = contexto.parametros.string("cbuAcreditar") : null;
		cedip.cedipTipoAcred = contexto.parametros.existe("cedipTipoAcred") ? cedip.cedipTipoAcred = contexto.parametros.string("cedipTipoAcred") : null;
		cedip.codigoBanco = contexto.parametros.existe("codigoBanco") ? contexto.parametros.string("codigoBanco") : null; 
		cedip.ejecutorDocumento = contexto.parametros.existe("ejecutorDocumento") ? contexto.parametros.string("ejecutorDocumento") : null;
		cedip.ejecutorTipoDocumento = contexto.parametros.existe("ejecutorTipoDocumento") ? contexto.parametros.string("ejecutorTipoDocumento") : null;

		cedip.fraccionId = contexto.parametros.existe("fraccionId") ? contexto.parametros.integer("fraccionId") : null;
		cedip.fraccionado = contexto.parametros.existe("fraccionado") ? contexto.parametros.bool("fraccionado") : false;
		cedip.tenedorDocumento = contexto.parametros.existe("tenedorDocumento") ? contexto.parametros.string("tenedorDocumento") : null;
		cedip.tenedorTipoDocumento = contexto.parametros.existe("tenedorTipoDocumento") ? contexto.parametros.string("tenedorTipoDocumento"): null;
		
		cedip.cbuAcreditar = contexto.parametros.existe("cbuAcreditar") ? contexto.parametros.string("cbuAcreditar"): null;
		cedip.tipoAcreditacion = contexto.parametros.existe("tipoAcreditacion") ? contexto.parametros.string("tipoAcreditacion"): null;
		cedip.fechaVencimiento = contexto.parametros.existe("fechaVencimiento") ? contexto.parametros.string("fechaVencimiento"): null;
		
		
		//TRANSMISION
		if (transmisiones != null) {
			Objeto objetoTransmisiones = (Objeto) transmisiones;		
			String beneficiarioDocumento = null;
			String beneficiarioNombre = null;
			String beneficiarioTipoDocumento = null;
			BigDecimal monto = null;
			String tipoTransmision = null;	    
			if (objetoTransmisiones.isList()) {
			    List<Objeto> listaTransmisiones = objetoTransmisiones.objetos();
			    if (!listaTransmisiones.isEmpty()) {
			        Objeto transmision = listaTransmisiones.get(0); // Tomamos el primer elemento de la lista
	
			        beneficiarioDocumento = transmision.string("beneficiarioDocumento");
			        beneficiarioNombre = transmision.string("beneficiarioNombre");
			        beneficiarioTipoDocumento = transmision.string("beneficiarioTipoDocumento");
			        monto = transmision.bigDecimal("monto");
			        tipoTransmision = transmision.string("tipoTransmision");
			        
			        cedip.monto = monto;
					cedip.montoCedip = monto;
					cedip.beneficiarioDocumento = beneficiarioDocumento;
			        cedip.beneficiarioNombre = beneficiarioNombre;
			        cedip.beneficiarioTipoDocumento = beneficiarioTipoDocumento;
			        cedip.tipoTransmision = tipoTransmision;
			    }
			}
    	}
        //FIRMANTE
        cedip.documentoFirmante = documentoFirmante;
        cedip.tipoDocumentoFirmante = tipoDocumentoFirmante;
        
        if (montos != null) {
	        Objeto objetoMontos = (Objeto) montos;
			BigDecimal montoACobrar = null;
			BigDecimal montoDepositado = null;
			BigDecimal montoIntereses = null;
			BigDecimal montoRetencion = null;
		    List<Objeto> listaMontos = objetoMontos.objetos();
		    if (!listaMontos.isEmpty()) {
		        Objeto monto = listaMontos.get(0);	     
		        montoACobrar = monto.bigDecimal("montoACobrar");
		        montoDepositado = monto.bigDecimal("montoDepositado");
		        montoIntereses = monto.bigDecimal("montoIntereses");
		        montoRetencion = monto.bigDecimal("montoRetencion");
		    }
		    cedip.montoACobrar = montoACobrar;
		    cedip.montoDepositado = montoDepositado;
		    cedip.montoIntereses = montoIntereses;
		    cedip.montoRetencion = montoRetencion;
        }
		
        if (cedip.accion.equals("depositar")) {   	
    		cedip.monto = contexto.parametros.existe("montoACobrar") ? contexto.parametros.bigDecimal("montoACobrar") : BigDecimal.ZERO;
    	}
     
    	if (cedip.accion.equals("modificar") || cedip.accion.equals("anular") ) {   	
    		cedip.monto = contexto.parametros.existe("montoDepositado") ? contexto.parametros.bigDecimal("montoDepositado") : BigDecimal.ZERO;
    	}
    	
		cedip.moneda = moneda;
		cedip.monedaCedip = moneda.id;
//		estadoEnBandeja = servicioEstadoCedipOB.find(EnumEstadoCedipOB.EN_BANDEJA.getCodigo()).get().id;
		cedip.estadoBandeja = estadoBandejaOB;
		cedip.estado = estadoEnBandeja;
		cedip.estado_firma = "Pendiente de Firma";
		cedip.tipoProductoFirma = tipoProductoFirmaOB;
		cedip.cuenta = cuenta;
		cedip.cuentaOrigen = cuenta;
		cedip.fechaUltActulizacion = ahora;
		if (empresaOB != null) {
			cedip.empresa = empresaOB;			
		}
		return futuro(() -> repoAcc.create(cedip));
	}

}