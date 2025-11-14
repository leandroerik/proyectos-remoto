package ar.com.hipotecario.backend.servicio.api.tarjetasCredito;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DatosPagoTC extends ApiObjeto {
	
 	public boolean esPagableUS;
    public String modoEResumenVisa;
    public String modoEResumenVisaDescrip;
    public String origen;
    public String origenDescripcion;
    public String email;
    public String numero;
    public String cuenta;
    public String tipoTarjeta;
    public String descTipoTarjeta;
    public String fechaVencActual;
    public String cierreActual;
    public String proxVenc;
    public String proxCierre;
    public String vencAnterior;
    public String cierreAnterior;
    public double limiteCompraAnterior;
    public double limiteCompraAcordado;
    public double limiteFinanciacionDisponible;
    public double limiteFinanciacionAcordado;
    public double pagoMinimoActual;
    public double pagoMinimoAnterior;
    public double debitosEnCursoPesos;
    public double debitosEnCursoDolares;
    public int cantAdicionales;
    public String grupo;
    public String grupoDesc;
    public String grupoAfinidad;
    public String descGrupoAfinidad;
    public String formaPago;
    public String descFormaPago;
    public String canal;
    public String descCanal;
    public String subCanal;
    public String descSubCanal;
    public String nroSolicitud;
    public boolean firmaContrato;
    public boolean tarjetaHabilitada;
    public String fechaTarjetaHabilitada;
    public String vigenciaDesde;
    public String vigenciaHasta;
    public String fechaRenovacion;
    public String bancaCuentaTipo;
    public String bancaCuentaCasaCodi;
    public String bancaCuentaNumero;
    public String idCliente;
    public String cuentaEstado;
    public String fechaAltaCuenta;
    public String tarjetaEstado;
    public boolean adheridoResumenElectronico;
    public double tasaFinancicionPesos;
    public String stopDebit;
    public double sumaTotalDolaresMasPesosEnPesos;
    public double cotizacionActualVenta;
    public double cotizacionActualCompra;
    public double totalDolaresEnPesos;
	
	public static List<DatosPagoTC> get(ContextoOB contexto, String numeroTarjeta) {
        ApiRequest request = new ApiRequest("TarjetaCreditoGET", "tarjetascredito", "GET",
                "/v2/tarjetascredito/{idtarjeta}", contexto);
        request.path("idtarjeta", numeroTarjeta);
        request.query("filtro", "1");
        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200), request, response);
        
        try {
	    	ObjectMapper objectMapper = new ObjectMapper();
	        List<DatosPagoTC> pago = objectMapper.readValue(response.body, new TypeReference<List<DatosPagoTC>>() {});
	        return pago;
        }catch (Exception e) {
        	throw new RuntimeException("Error deserializando la respuesta de los datos del pago", e);
        }
        

    }

}
