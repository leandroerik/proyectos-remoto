package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTextosOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TextoOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;


public class OBTextos extends ModuloOB{
	
	public static Object obtenerTextos(ContextoOB contexto) {
		String idFront = contexto.parametros.string("idFront");
		
		Objeto respuesta = new Objeto();
		SesionOB sesion = contexto.sesion();

		ServicioTextosOB servicioTextos = new ServicioTextosOB(contexto);
		TextoOB textos = servicioTextos.find(idFront).tryGet();
		
		if(textos==null) {
			return respuesta("ERROR", "descripcion", "No existe el IdFront");
		}
		
		Objeto datos = new Objeto();
		datos.set("idFront", textos.idFront);
		datos.set("titulo", StringUtil.reemplazarCaracteresCodificacion(textos.titulo));
		datos.set("subtitulo", StringUtil.reemplazarCaracteresCodificacion(textos.subtitulo));
		datos.set("descripcion", StringUtil.reemplazarCaracteresCodificacion(textos.descripcion));
		datos.set("TipoProductoFirma",textos.tipoProductoFirma.codProdFirma);
						
		respuesta.add(datos);

		return respuesta("datos", respuesta);
	}

}
