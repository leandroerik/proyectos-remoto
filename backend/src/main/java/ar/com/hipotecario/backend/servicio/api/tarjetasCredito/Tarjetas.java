package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tarjetas extends ApiObjeto {
	
	public Boolean esPagableUS;
	public String altaPuntoVenta;
	public Long idPaquete;
	public Boolean muestraPaquete;
	public Integer sucursal;
	public String descSucursal;
	public String estado;
	public String descEstado;
	public String fechaAlta;
	public String tipoTitularidad;
	public String descTipoTitularidad;
	public String numero;
	public String cuenta;
	public String tipoTarjeta;
	public String descTipoTarjeta;
	public String fechaVencActual;
	public String cierreActual;
	public double debitosEnCursoPesos;
	public double debitosEnCursoDolares;
	public String formaPago;
	public String denominacionTarjeta;
	public String modeloLiquidacion;
	public String descModeloLiquidacion;

	public static List<Tarjetas> getTarjetas(ContextoOB contexto, String idCobis, Boolean adicionales) {
		ApiRequest request = new ApiRequest("ListadoTarjetas", "tarjetascredito", "GET", "/v1/tarjetascredito", contexto);
		
		request.query("adicionales", adicionales);
		request.query("cancelados", false);
		request.query("idcliente", idCobis);
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		
		if(response.http(204)) {
			throw new RuntimeException("SIN_CUENTAS");
		}
		
		try {
            ObjectMapper objectMapper = new ObjectMapper();
            List<Tarjetas> tarjetas = objectMapper.readValue(response.body, new TypeReference<List<Tarjetas>>() {});
            if(tarjetas.size() > 0) {
            	TarjetasCredito.obtenerDatosClienteSO(contexto, tarjetas.get(0).cuenta);
            }
            return tarjetas;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error deserializando la respuesta a una lista de Tarjetas", e);
        }
	}
}
