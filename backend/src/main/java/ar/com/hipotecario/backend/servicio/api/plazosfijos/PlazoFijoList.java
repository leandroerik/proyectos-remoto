package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class PlazoFijoList extends ApiObjetos<PlazoFijoList.PlazoFijo> {
	
	/* ========== ATRIBUTOS ========== */
	public class PlazoFijo extends ApiObjeto {
	    public String rol;
	    public String descMoneda;
	    public boolean muestraPaquete;
	    public String tipoProducto;
	    public String numeroProducto;
	    public String idProducto;
	    public String estado;
	    public String descEstado;
	    public Date fechaAlta;
	    public String descTipoTitularidad;
	    public String tipoOperacion;
	    public boolean adicionales;
	    public String idMoneda;
	    public Date fechaVencimiento;
	    public String importe;
	    public String estadoPlazoFijo;
	    
	    public String totalIntereses;
	    public String descSucursal;
	    
		public Date getFechaVencimiento() {
			return fechaVencimiento;
		}
		public void setFechaVencimiento(Date fechaVencimiento) {
			this.fechaVencimiento = fechaVencimiento;
		}

		public Date getFechaAlta() {
			return fechaAlta;
		}
		public void setFechaAlta(Date fechaAlta) {
			this.fechaAlta = fechaAlta;
		}

		public static class RespuestaOk extends ApiObjeto {
			public Boolean ok;
		}
	}

    /* ========== CONSTRUCTOR ========== */
    public PlazoFijoList() {
        super(); // Llama al constructor de ApiObjetos para inicializar la lista interna
    }

    /* ========== MÉTODOS PERSONALIZADOS ========== */

    public List<PlazoFijo> getPlazosFijos() {
        List<PlazoFijo> plazos = new ArrayList<>();
        forEach(plazo -> plazos.add(plazo));
        return plazos;
    }

    public void setPlazosFijos(List<PlazoFijo> plazosFijos) {
        List<PlazoFijo> listaInterna = list();
        listaInterna.clear();
        plazosFijos.forEach(listaInterna::add);
    }

	/* ========== SERVICIOS ========== */	
	public static PlazoFijoList get(Contexto contexto, String idcliente) {
		// Primer llamado a la API
		ApiRequest request1 = new ApiRequest("API-PlazoFijo_ConsultaListadoPlazoFijo", "plazosfijos", "GET", "/v1/plazosfijos", contexto);
		
		request1.query("idcliente", idcliente);
		
		ApiResponse response1 = request1.ejecutar();
		
		if (response1.codigoHttp == 204) {
			response1.body = "NO TIENE PLAZOS FIJOS";
		}
		else {
			ApiException.throwIf(!response1.http(200), request1, response1);	
		}
		
		PlazoFijoList plazoFijoList1 = response1.crear(PlazoFijoList.class);
        
		// Segundo llamado a la API con los nuevos parámetros
		Date fecha = new Date();
        SimpleDateFormat formatoFecha = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String fechaFormateada = formatoFecha.format(fecha);
        
		ApiRequest request2 = new ApiRequest("API-PlazoFijo_ConsultaListadoPlazoFijo", "plazosfijos", "GET", "/v1/plazosfijos", contexto);
		request2.query("fecha", fechaFormateada);
	    request2.query("idcliente", idcliente);
	    request2.query("inversiones", true);

	    ApiResponse response2 = request2.ejecutar();

	    if (response2.codigoHttp == 204) {
	        response2.body = "NO TIENE PLAZOS FIJOS";
	    } else {
	        ApiException.throwIf(!response2.http(200), request2, response2);    
	    }

	    PlazoFijoList plazoFijoList2 = response2.crear(PlazoFijoList.class);
	    //PlazoFijoList plazo = new PlazoFijoList();
//	    plazo.add(new PlazoFijo());
		//System.out.println("tiene"+plazoFijoList2.size());
	    // Combinar los resultados de ambas listas
	    PlazoFijoList combinedPlazoFijoList = combinePlazoFijoLists(plazoFijoList1, plazoFijoList2);
	    return filtrarPlazosVigentes(combinedPlazoFijoList);
//		return plazoFijoList;
	}	
	
	private static PlazoFijoList combinePlazoFijoLists(PlazoFijoList list1, PlazoFijoList list2) {
        List<PlazoFijo> combinedPlazos = new ArrayList<>(list1.getPlazosFijos());
        combinedPlazos.addAll(list2.getPlazosFijos());
        list1.setPlazosFijos(combinedPlazos);
        return list1;
    }
	
	private static PlazoFijoList filtrarPlazosVigentes(PlazoFijoList plazoFijoList) {
        List<PlazoFijo> vigentes = new ArrayList<>();
        LocalDate today = LocalDate.now();
	
        for (PlazoFijo plazoFijo : plazoFijoList.getPlazosFijos()) {
            LocalDate fechaVencimiento = plazoFijo.getFechaVencimiento().toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            if (!fechaVencimiento.isBefore(today)) {
                vigentes.add(plazoFijo);
            }
        }
        
        plazoFijoList.setPlazosFijos(vigentes);
        return plazoFijoList;
    }

}

