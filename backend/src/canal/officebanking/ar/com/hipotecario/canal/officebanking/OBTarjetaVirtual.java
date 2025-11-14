package ar.com.hipotecario.canal.officebanking;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.link.ApiLink;
import ar.com.hipotecario.backend.servicio.api.link.LinkAdhesiones;
import ar.com.hipotecario.backend.servicio.api.link.TarjetaVirtual;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTarjetaVirtualOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TarjetaVirtualOB;

public class OBTarjetaVirtual extends ModuloOB {

	private static final String FINAL_TARJETA_VIRTUAL = "0000";
	
	public static Object obtenerTarjetasVirtuales(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		Integer idFijo=7786;
		Integer empFija=2101;


		ServicioTarjetaVirtualOB servicioTarjetaVirtualOB = new ServicioTarjetaVirtualOB(contexto);

		List<TarjetaVirtualOB> tarjetas;

		if(contexto.sesion().empresaOB.emp_codigo!=2101){
			tarjetas = servicioTarjetaVirtualOB.buscarPorEmpresa(sesion.empresaOB).get();
		}else{
			tarjetas = servicioTarjetaVirtualOB.buscarPorEmpresaAndId(sesion.empresaOB, idFijo).get();
		}


		if(tarjetas.size() == 0) {
			nuevaTarjetaVirtual(contexto, sesion, servicioTarjetaVirtualOB);
			tarjetas = servicioTarjetaVirtualOB.buscarPorEmpresa(sesion.empresaOB).get();
		}
		
		sesion.tarjetasVirtuales = tarjetas;
		sesion.save();
		 
		List<Objeto> datos = new ArrayList<Objeto>();
		for(TarjetaVirtualOB tv : tarjetas) {
			Objeto o = new Objeto();
			o.set("tarjetas", tv.nroTarjeta);
			datos.add(o);
		}
		
		return respuesta("tarjetasVirtuales", datos);
	}
	
	private static TarjetaVirtual nuevaTarjetaVirtual(ContextoOB contexto, SesionOB sesion, ServicioTarjetaVirtualOB servicioTarjetaVirtualOB) {
		TarjetaVirtual tarjetaVirtualApi = new TarjetaVirtual(); 
		TarjetaVirtualOB tarjeta = new TarjetaVirtualOB();
		String tvIndex = null;
			
		String lastIndex = servicioTarjetaVirtualOB.getIndex().get();
					
		if(!empty(lastIndex)) {
			String nuevoIndex = Integer.toString(Integer.parseInt(lastIndex) + 1);
			tvIndex = String.format("%05d", Integer.parseInt(nuevoIndex));
		}else {
			tvIndex = "00001";
		}

		String secuencial = tvIndex + FINAL_TARJETA_VIRTUAL;
		tarjetaVirtualApi = TarjetaVirtual.post(contexto, secuencial);
		
		tarjeta.empresa = sesion.empresaOB;
		tarjeta.tvIndex = tvIndex;		
		tarjeta.nroTarjeta = tarjetaVirtualApi.nroTarjeta;
		servicioTarjetaVirtualOB.create(tarjeta).get();			
				 
		return tarjetaVirtualApi;
	}

	public static String obtenerTarjetaVirtual(ContextoOB contexto, List<TarjetaVirtualOB> tarjetas) {
		SesionOB sesion = contexto.sesion();
		ServicioTarjetaVirtualOB servicioTarjetaVirtualOB = new ServicioTarjetaVirtualOB(contexto);
		ServicioParametroOB servicioParametros = new ServicioParametroOB(contexto);
		String nroTarjeta = null;
		Boolean sinTarjetasParaAdherir = false;
		int cant_max_adhesiones = Integer.parseInt(servicioParametros.find("cant_max_adhesiones").get().valor);
		
		for(TarjetaVirtualOB tv : tarjetas) {
			LinkAdhesiones adhesiones = ApiLink.getAdhesiones(contexto, tv.nroTarjeta).tryGet();
			if (empty(adhesiones) || adhesiones.size() < cant_max_adhesiones) {
				return tv.nroTarjeta;
			}else {
				sinTarjetasParaAdherir = true;
			}
		 }
		
		if(sinTarjetasParaAdherir == true) {
			nroTarjeta = nuevaTarjetaVirtual(contexto, contexto.sesion(), servicioTarjetaVirtualOB).nroTarjeta;
			tarjetas = servicioTarjetaVirtualOB.buscarPorEmpresa(contexto.sesion().empresaOB).get();
			sesion.tarjetasVirtuales = tarjetas;
			sesion.save();
		}
		
		return nroTarjeta;
	}
}
