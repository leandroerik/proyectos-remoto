package ar.com.hipotecario.canal.officebanking;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.enums.EnumAccionesOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.enums.pagosMasivos.EnumEstadoPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioControlDualAutorizanteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEstadosPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioPagoAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.AccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.BandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.EstadoBandejaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.controlDual.ControlDualAutorizanteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.EstadosPagosAProveedoresOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores.PagoAProveedoresOB;

public class OBControlDual extends ModuloOB {
	
	protected static Object obtenerDatosAutorizantes(ContextoOB contexto, BandejaOB bandeja) {
        ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
      
        Objeto respuesta = new Objeto();
        
        List<BandejaAccionesOB> bandejaAccionesOB = servicioBandejaAcciones.buscarPorIdBandejaYAccion(bandeja, EnumAccionesOB.APROBAR.getCodigo()).get();
        if (bandejaAccionesOB!=null && bandejaAccionesOB.size()>0){
        	Objeto datos = new Objeto();

        	BandejaAccionesOB firma = bandejaAccionesOB.get(0);
        		
        	UsuarioOB usuario = servicioUsuario.findByCuil(firma.empresaUsuario.usuario.cuil).get();
        	datos.set("nombreAutorizante", usuario.nombre + " " + usuario.apellido);
        	
        	datos.set("fechaAprobacion", firma.fechaCreacion.toLocalDate().toString() + " " + firma.fechaCreacion.toLocalTime().withSecond(0).withNano(0).toString());
        	
            respuesta.add("datos", datos);
        }
        
        return respuesta;
    }

	
	public static Object aprobar(ContextoOB contexto) {
        Objeto objetoIds = contexto.parametros.objeto("idsOperaciones");
        List<Object> idsOperaciones = objetoIds.toList();
        List<Object> datosOperaciones = new ArrayList<>();
        
        ServicioPagoAProveedoresOB servicioPAP = new ServicioPagoAProveedoresOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPAP = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionAprobar = servicioAcciones.find(EnumAccionesOB.APROBAR.getCodigo()).get();
        
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);
        
        EstadoBandejaOB estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.PENDIENTE_FIRMA.getCodigo()).get();
		EstadosPagosAProveedoresOB estadoPago = servicioEstadosPAP.find(EnumEstadoPagosAProveedoresOB.PENDIENTE.getCodigo()).get();
        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_AUTORIZACION.getCodigo()).get();

        Objeto datos = new Objeto();
        try {
        	for (Object id : idsOperaciones) {
        		BandejaOB bandeja = servicioBandeja.find(Integer.parseInt(id.toString())).get();
        		PagoAProveedoresOB pago = servicioPAP.find(bandeja.id).get();
        	
        		servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionAprobar, estadoInicialBandeja, estadoBandeja);
        		
        		pago.estado = estadoPago;
        		pago.estadoBandeja = estadoBandeja;
        		servicioPAP.update(pago);
        		
        		datos.set("idOperacion", bandeja.id);
        		datos.set("estadoBandeja", estadoBandeja.descripcion);
        		datos.set("estadoPago", estadoBandeja.descripcion);
        		     
                datos.set("autorizantes", OBControlDual.obtenerDatosAutorizantes(contexto, bandeja)); 
        		
        		datosOperaciones.add(respuesta("datos", datos));
        	}	
        }catch (Exception ex){
        	LogOB.evento(contexto, "APROBAR", "EMPRESA:" + contexto.sesion().empresaOB.cuit);	
        	LogOB.evento(contexto, "APROBAR", "ERROR:" + ex.getMessage());
        }

        return respuesta("datosOperaciones", datosOperaciones);
    }

	public static Object rechazar(ContextoOB contexto) {
        Objeto objetoIds = contexto.parametros.objeto("idsOperaciones");
        List<Object> idsOperaciones = objetoIds.toList();
        List<Object> datosOperaciones = new ArrayList<>();
                
        ServicioPagoAProveedoresOB servicioPAP = new ServicioPagoAProveedoresOB(contexto);
        ServicioBandejaOB servicioBandeja = new ServicioBandejaOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandeja = new ServicioEstadoBandejaOB(contexto);
        ServicioEstadosPagoAProveedoresOB servicioEstadosPAP = new ServicioEstadosPagoAProveedoresOB(contexto);
        ServicioBandejaAccionesOB servicioBandejaAcciones = new ServicioBandejaAccionesOB(contexto);
        ServicioEstadoBandejaOB servicioEstadoBandejaOB = new ServicioEstadoBandejaOB(contexto);
        
        ServicioAccionesOB servicioAcciones = new ServicioAccionesOB(contexto);
        AccionesOB accionAprobar = servicioAcciones.find(EnumAccionesOB.APROBAR.getCodigo()).get();
        
        EmpresaUsuarioOB empresaUsuario = empresasUsuario(contexto, contexto.sesion().empresaOB, contexto.sesion().usuarioOB);
        
        EstadoBandejaOB estadoBandeja = servicioEstadoBandeja.find(EnumEstadoBandejaOB.RECHAZADO_AUTORIZACION.getCodigo()).get();
        EstadosPagosAProveedoresOB estadoPago = servicioEstadosPAP.find(EnumEstadoPagosAProveedoresOB.RECHAZADO.getCodigo()).get();
        EstadoBandejaOB estadoInicialBandeja = servicioEstadoBandejaOB.find(EnumEstadoBandejaOB.PENDIENTE_AUTORIZACION.getCodigo()).get();
	
        Objeto datos = new Objeto();
        try {
        	for (Object id : idsOperaciones) {
        		BandejaOB bandeja = servicioBandeja.find(Integer.parseInt(id.toString())).get();
        		PagoAProveedoresOB pago = servicioPAP.find(bandeja.id).get();
        	
        		servicioBandejaAcciones.crear(bandeja, empresaUsuario, accionAprobar, estadoInicialBandeja, estadoBandeja);
        		        		
        		pago.estado = estadoPago;
        		pago.estadoBandeja = estadoBandeja;
        		servicioPAP.update(pago);
        		
        		datos.set("idOperacion", bandeja.id);
        		datos.set("estadoBandeja", estadoBandeja.descripcion);
        		datos.set("estadoPago", estadoBandeja.descripcion);
        		
        		datos.set("autorizantes", OBControlDual.obtenerDatosAutorizantes(contexto, bandeja));
        		
        		datosOperaciones.add(respuesta("datos", datos));
        	}	
        }catch (Exception ex){
        	LogOB.evento(contexto, "APROBAR", "EMPRESA:" + contexto.sesion().empresaOB.cuit);	
        	LogOB.evento(contexto, "APROBAR", "ERROR:" + ex.getMessage());
        }

        return respuesta("datosOperaciones", datosOperaciones);
    }
}
