package ar.com.hipotecario.backend.servicio.api.plazosfijos;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.util.FechaNullSaferDeserializer;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;

public class SimulacionCedip extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */
	public String tipo;
	@JsonDeserialize(using = FechaNullSaferDeserializer.class)
	public Fecha fechaActivacion;
	public Boolean garantiaDeLosDepositos;

	public BigDecimal montoTotal;
	public BigDecimal interesEstimado;
	public BigDecimal totalInteresEstimado;
	public BigDecimal impuestosAPagar;
	public String fechaPagoInteres;
	public String diaDePago;
	public String numeroDePagos;
	public String fechaVencimiento;
	public String fechaActual;
	public String producto;
	public String moneda;
	public BigDecimal capital;
	public BigDecimal tasa;
	public Integer plazo;
	public String cuentaADebitar;
	public String gtiaDeDepositos;
	public BigDecimal montoUVA;
	public BigDecimal cotizacionUVA;
	@JsonDeserialize(using = FechaNullSaferDeserializer.class)
	public Fecha fechaCotizacionUVA;
	public String cancelacionAnticipada;
	public BigDecimal tasaCancelacionAnt;
	public BigDecimal tnaCancelacionAnt;
	public BigDecimal teaCancelacionAnt;
	@JsonDeserialize(using = FechaNullSaferDeserializer.class)
	public Fecha fechaDesdeCancelacionAnt;
	@JsonDeserialize(using = FechaNullSaferDeserializer.class)
	public Fecha fechaHastaCancelacionAnt;
	public BigDecimal tasaCancelacionLeliq120;
	public BigDecimal tnaCancelacionLeliq120;
	public BigDecimal teaCancelacionLeliq120;
	@JsonDeserialize(using = FechaNullSaferDeserializer.class)
	public Fecha fechaCancelacionLeliq120;
	public BigDecimal sellos;
	
	private static final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	/* ========== SERVICIOS ========== */
	public static List<SimulacionCedip> get(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion, String cuenta, Integer idCliente, String capInteres,String moneda) {
		return getSimulacion(contexto, monto, plazo, tipoOperacion, cuenta, idCliente, capInteres, moneda);
	}

	// API-Simuladores
	//GET v1/plazoFijos
	public static List<SimulacionCedip> getSimulacion(Contexto contexto, BigDecimal monto, Integer plazo, String tipoOperacion, String cuenta, Integer idCliente, String capInteres,String moneda) {
	ApiRequest request = new ApiRequest("AltaPlazoFijoSimulacion", "simuladores", "GET", "/v1/plazoFijos", contexto);
		request.query("tipoOperacion", tipoOperacion);
		request.query("moneda", moneda);
		request.query("monto", monto);
		request.query("plazo", plazo);
		request.query("cuenta", cuenta);
		request.query("idcliente", idCliente);
		request.query("capInteres", capInteres);
	
		request.cache = false;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
        
        return procesarRespuesta(response.body);
	}
	
	private static List<SimulacionCedip> procesarRespuesta(String responseBody) {
		try {
            return objectMapper.readValue(responseBody, new TypeReference<List<SimulacionCedip>>() {});
        } catch (IOException e) {
            e.printStackTrace();
            // Manejar la excepción apropiadamente
            return Collections.emptyList();
        }
    }
}
