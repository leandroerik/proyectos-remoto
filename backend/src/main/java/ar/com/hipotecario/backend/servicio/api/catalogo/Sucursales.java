package ar.com.hipotecario.backend.servicio.api.catalogo;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.catalogo.Sucursales.Sucursal;

public class Sucursales extends ApiObjetos<Sucursal> {

	/* ========== ATRIBUTOS ========== */
	public static class Sucursal extends ApiObjeto {
		public String CodSucursal;
		public String DesSucursal;
		public String Domicilio;
		public String HorarioAtencion;
		public String desTipoSucursal;
		public String NomProvincia;
		public Integer codTipoSucursal;
		public Integer codProvincia;
		public BigDecimal Latitud;
		public BigDecimal Longitud;
		public Boolean AudioNoVidentes;
	}

	public static class SucursalGeo extends ApiObjeto {
		public String id;
		public String desSucursal;
		public String domicilio;
		public String provincia;
		public BigDecimal distancia;
	}

	/* ========== SERVICIOS ========== */
	static Sucursales get(Contexto contexto) {
		return get(contexto, "", "", "");
	}

	// API-Catalogo_ConsultaSucursales
	static Sucursales get(Contexto contexto, String provincia, String sucursal, String tipoSucursal) {
		ApiRequest request = new ApiRequest("sucursales", "catalogo", "GET", "/v1/sucursales", contexto);
		if(!provincia.isEmpty()) {
			request.query("codProvincia", provincia);
		}
		if(sucursal != null && !sucursal.isEmpty()) {
			request.query("codSucursal", sucursal);
		}
		if(!tipoSucursal.isEmpty()) {
			request.query("codTipoSucursal", tipoSucursal);
		}
		//request.query("codSucursal", sucursal);
		//request.query("codTipoSucursal", tipoSucursal);
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200) && !response.contains("no arrojó resultados"), request, response);
		return response.crear(Sucursales.class);
	}

	public List<SucursalGeo> getSucursalesPorGeo(String latitud, String longitud, BigDecimal radio) {

		try {
			List<SucursalGeo> sucursalesGeo = new ArrayList<SucursalGeo>();

			for (Sucursal sucursal : this) {

				BigDecimal distancia = calcularDistancia(sucursal.Latitud, sucursal.Longitud, new BigDecimal(latitud), new BigDecimal(longitud));

				if (radio != null && distancia.compareTo(radio) > 0) {
					continue;
				}

				SucursalGeo sucursalGeo = new SucursalGeo();
				sucursalGeo.id = sucursal.CodSucursal;
				sucursalGeo.desSucursal = sucursal.DesSucursal;
				sucursalGeo.domicilio = sucursal.Domicilio;
				sucursalGeo.provincia = sucursal.NomProvincia;
				sucursalGeo.distancia = distancia;
				sucursalesGeo.add(sucursalGeo);
			}

			Collections.sort(sucursalesGeo, Comparator.comparing(sucursal -> sucursal.distancia));

			return sucursalesGeo;
		} catch (Exception e) {

		}

		return new ArrayList<SucursalGeo>();
	}

	public static BigDecimal calcularDistancia(BigDecimal lat1, BigDecimal lon1, BigDecimal lat2, BigDecimal lon2) {
		// Radio de la Tierra en kilómetros
		BigDecimal radioTierra = new BigDecimal("6371.0");

		// Convertir latitud y longitud de grados a radianes
		BigDecimal lat1Rad = lat1.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), 10, RoundingMode.HALF_UP);
		BigDecimal lon1Rad = lon1.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), 10, RoundingMode.HALF_UP);
		BigDecimal lat2Rad = lat2.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), 10, RoundingMode.HALF_UP);
		BigDecimal lon2Rad = lon2.multiply(BigDecimal.valueOf(Math.PI)).divide(BigDecimal.valueOf(180), 10, RoundingMode.HALF_UP);

		// Diferencia de latitud y longitud
		BigDecimal dlat = lat2Rad.subtract(lat1Rad);
		BigDecimal dlon = lon2Rad.subtract(lon1Rad);

		// Calcular la distancia utilizando la fórmula de Haversine
		BigDecimal a = new BigDecimal(Math.pow(Math.sin(dlat.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP).doubleValue()), 2) + Math.cos(lat1Rad.doubleValue()) * Math.cos(lat2Rad.doubleValue()) * Math.pow(Math.sin(dlon.divide(new BigDecimal("2"), 10, RoundingMode.HALF_UP).doubleValue()), 2));
		BigDecimal c = new BigDecimal("2").multiply(new BigDecimal(Math.atan2(Math.sqrt(a.doubleValue()), Math.sqrt(1 - a.doubleValue()))));

		BigDecimal distancia = radioTierra.multiply(c);

		return distancia;
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		Sucursales datos = get(contexto);
		imprimirResultado(contexto, datos);
	}

}
