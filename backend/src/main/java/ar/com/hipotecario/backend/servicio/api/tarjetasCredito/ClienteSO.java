package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ClienteSO extends ApiObjeto {
	 	public String tarjeNume;
	    public int renoMarca;
	    public String ddn;
	    public String email;
	    public String cuit;
	    public int datosRetenMarca;
	    public String tarjeProdProdTipo;
	    public String cuentaEstaDescrip;
	    public String pagoFormaCuenta;
	    public int renoDesisMarca;
	    public String ce_EmpreCodi;
	    public String extCalle;
	    public int pagoFormaDB;
	    public String tarjeVenci;
	    public String docuCodi;
	    public String ceduCodi;
	    public String loca;
	    public int miniPagoPlazo;
	    public String compraLimi;
	    public int digi;
	    public int boleMarca;
	    public String afiGrupo;
	    public int entiNume;
	    public int tasaGrupo;
	    public long entiAltaFecha;
	    public int modoEResumen;
	    public String piso;
	    public int grupoCodi;
	    public int sucurCodi;
	    public String pagoFormaTipo;
	    public String depto;
	    public double crediLimi;
	    public long modiUltiFecha;
	    public String calleNume;
	    public String agrupaCodi;
	    public String ceduNume;
	    public String cuenta;
	    public String docuNume;
	    public String tarjeTipo;
    
    public static List<ClienteSO> get(ContextoOB contexto, String cuenta) {
    	ApiRequest requestCodEmpresa = new ApiRequest("ListadoTarjetas", "tarjetascredito", "GET", "/v1/tarjetascredito/obtenerIdPrisma", contexto);
    	List<ClienteSO> codEmpList = null;
		String codEmpre = null;
		
		try {
			ObjectMapper objectMapper = new ObjectMapper();
	    	requestCodEmpresa.query("cuenta", cuenta);  
			ApiResponse responseCodEmpresa = requestCodEmpresa.ejecutar();
			ApiException.throwIf(!responseCodEmpresa.http(200, 204), requestCodEmpresa, responseCodEmpresa);
			codEmpList = objectMapper.readValue(responseCodEmpresa.body, new TypeReference<List<ClienteSO>>() {});      	

			SesionOB sesion = contexto.sesion();
			sesion.idPrisma = codEmpList.get(0).ce_EmpreCodi;
			sesion.save();
			return codEmpList;
		} catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializando la respuesta a una lista de datos de cliente en SmartOpen", e);
        }
    }
}
