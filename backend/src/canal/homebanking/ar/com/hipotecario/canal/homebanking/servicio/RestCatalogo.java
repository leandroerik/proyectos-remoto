package ar.com.hipotecario.canal.homebanking.servicio;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;

public class RestCatalogo {

	/* ========== SERVICIOS ========== */
	public static ApiResponse paises(ContextoHB contexto) {
		ApiRequest request = Api.request("Paises", "catalogo", "GET", "/v1/paises", contexto);
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse provincias(ContextoHB contexto) {
		ApiRequest request = Api.request("Provincias", "catalogo", "GET", "/v1/paises/{idPais}/provincias", contexto);
		request.path("idPais", "80");
		request.cacheSesion = true;
		return Api.response(request, "80");
	}

	public static ApiResponse localidades(ContextoHB contexto, String idProvincia) {
		ApiRequest request = Api.request("Localidades", "catalogo", "GET", "/v1/paises/{idPais}/provincias/{idProvincia}/ciudades", contexto);
		request.path("idPais", "80");
		request.path("idProvincia", idProvincia);
		request.cacheSesion = true;
		return Api.response(request, "80", idProvincia);
	}

	public static ApiResponse estadosCiviles(ContextoHB contexto) {
		ApiRequest request = Api.request("EstadosCiviles", "catalogo", "GET", "/v1/estadoCiviles", contexto);
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse situacionesLaborales(ContextoHB contexto) {
		ApiRequest request = Api.request("SituacionLaboral", "ventas_windows", "GET", "/parametria/SituacionLaboral", contexto);
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse nivelesEstudio(ContextoHB contexto) {
		ApiRequest request = Api.request("nivelesEstudios", "ventas_windows", "GET", "/parametria/NivelEstudios", contexto);
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse sucursales(ContextoHB contexto) {
		ApiRequest request = Api.request("sucursales", "catalogo", "GET", "/v1/sucursales", contexto);
		request.query("codTipoSucursal", "1");
		request.cacheSesion = true;
		return Api.response(request);
	}

	public static ApiResponse sucursalPorCodigo(ContextoHB contexto, String codigo) {
		ApiRequest request = Api.request("sucursalesPorCodigo", "catalogo", "GET", "/v1/sucursales", contexto);
		request.query("codSucursal", codigo);
		return Api.response(request);
	}

	public static ApiResponse calendarioFechaActual(ContextoHB contexto) {
		Calendar hoy = Calendar.getInstance();
		hoy.set(Calendar.HOUR_OF_DAY, 0);
		hoy.set(Calendar.MINUTE, 0);
		hoy.set(Calendar.SECOND, 0);
		hoy.set(Calendar.MILLISECOND, 0);

		String fecha = new SimpleDateFormat("yyyy-MM-dd").format(hoy.getTime());

		ApiRequest request = Api.request("CatalogoCalendario", "catalogo", "GET", "/v1/calendario/{fecha}", contexto);
		request.path("fecha", fecha);
		request.cacheSesion = true;

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse calendarioFecha(ContextoHB contexto, String fecha) {
		// La fecha que se pasa por parametro tiene que ser yyyy-MM-dd
		ApiRequest request = Api.request("CatalogoCalendario", "catalogo", "GET", "/v1/calendario/{fecha}", contexto);
		request.path("fecha", fecha);
		request.cacheSesion = true;
		return Api.response(request, contexto.idCobis(), fecha);
	}

	public static ApiResponse consultaRubrosDebitosAutomaticos(ContextoHB contexto, Integer paginaActual) {
		ApiRequest request = Api.request("CatalogoRubrosDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/RUBROS", contexto);
		request.query("cantidadRegistros", "30");
		request.query("numeroPagina", paginaActual.toString());

		return Api.response(request);
	}

	public static ApiResponse consultaEmpresasDebitosAutomaticos(ContextoHB contexto, String codigoRubro, Integer paginaActual) {
		ApiRequest request = Api.request("CatalogoEmpresasDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/EMPRESAS", contexto);
		request.query("cantidadRegistros", "30");
		request.query("codigoRubro", codigoRubro);
		request.query("numeroPagina", paginaActual.toString());
		request.cacheSesion = true;

		return Api.response(request, contexto.idCobis(), codigoRubro, paginaActual.toString());
	}

	public static ApiResponse consultaServiciosDebitosAutomaticos(ContextoHB contexto, String codigoRubro, String cuitEmpresa, Integer paginaActual) {
		ApiRequest request = Api.request("CatalogoServiciosDebitosAutomaticos", "catalogo", "GET", "/v1/consulta/SERVICIOS", contexto);
		request.query("cantidadRegistros", "100");
		request.query("codigoRubro", codigoRubro);
		request.query("cuit", cuitEmpresa);
		request.query("numeroPagina", paginaActual.toString());

		return Api.response(request);
	}

	public static ApiResponse consultaCodigosPostales(ContextoHB contexto, String codigoPostal) {
		ApiRequest request = Api.request("CatalogoCodigosPostales", "catalogo", "GET", "/v1/cp", contexto);
		request.query("cp", codigoPostal);
		request.query("partidoCiudad", "");
		request.query("provincia", "");
		return Api.response(request);
	}

	/* ========== UTIL ========== */
	public static Map<Integer, String> mapaPaises(ContextoHB contexto) {
		Map<Integer, String> mapa = new TreeMap<>();
		ApiResponse response = paises(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				mapa.put(item.integer("id"), item.string("descripcion"));
			}
		}
		return mapa;
	}

	public static Map<Integer, String> mapaNacionalidades(ContextoHB contexto) {
		Map<Integer, String> mapa = new TreeMap<>();
		ApiResponse response = paises(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				mapa.put(item.integer("id"), item.string("nacionalidad"));
			}
		}
		return mapa;
	}

	public static Map<Integer, String> mapaProvincias(ContextoHB contexto) {
		Map<Integer, String> mapa = new TreeMap<>();
		ApiResponse response = provincias(contexto);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if (!"NO DEFINIDA".equals(item.string("descripcion"))) {
					mapa.put(item.integer("id"), item.string("descripcion"));
				}
			}
		}
		return mapa;
	}

	public static Map<Integer, String> mapaProvinciasPorCodigoPostal(ContextoHB contexto, String codigoPostal) {
		Map<Integer, String> mapa = new TreeMap<>();
		ApiResponse response = consultaCodigosPostales(contexto, codigoPostal);
		if (!response.hayError()) {
			for (Objeto item : response.objetos()) {
				if (!"NO DEFINIDA".equals(item.string("descripcion")) && 0 == item.integer("statuscode")) {
					mapa.put(item.integer("idProvincia"), item.string("provincia"));
				}
			}
		}
		return mapa;
	}

	public static String nombrePais(ContextoHB contexto, Integer idPais) {
		String nombrePais = mapaPaises(contexto).get(idPais);
		return nombrePais != null ? nombrePais : "";
	}

	public static String nombreNacionalidad(ContextoHB contexto, Integer idPais) {
		String nombreNacionalidad = mapaNacionalidades(contexto).get(idPais);
		return nombreNacionalidad != null ? nombreNacionalidad : "";
	}

	public static String nombreProvincia(ContextoHB contexto, Integer idProvincia) {
		String nombreProvincia = mapaProvincias(contexto).get(idProvincia != null ? idProvincia : 0);
		return nombreProvincia != null ? nombreProvincia : "";
	}

	public static Map<Integer, String> mapaLocalidades(ContextoHB contexto, Integer idProvincia) {
		Map<Integer, String> mapa = new LinkedHashMap<>();
		if (idProvincia != null) {
			ApiResponse response = localidades(contexto, idProvincia.toString());
			if (!response.hayError()) {
				response.ordenar("descripcion");
				for (Objeto item : response.objetos()) {
					mapa.put(item.integer("id"), item.string("descripcion"));
				}
			}
		}
		return mapa;
	}

	public static Map<Integer, String> mapaLocalidadesPorCodigoPostal(ContextoHB contexto, Integer idProvincia, String codigoPostal) {
		Map<Integer, String> mapa = new LinkedHashMap<>();
		if (idProvincia != null) {
			ApiResponse response = consultaCodigosPostales(contexto, codigoPostal);
			if (!response.hayError()) {
				response.ordenar("partidoCiudad");
				for (Objeto item : response.objetos()) {
					if (item.integer("idProvincia").equals(idProvincia) && 0 == item.integer("statuscode")) {
						mapa.put(item.integer("idCiudad"), item.string("partidoCiudad"));
					}
				}
			}
		}
		return mapa;
	}

	public static String nombreLocalidad(ContextoHB contexto, Integer idProvincia, Integer idLocalidad) {
		String nombreLocalidad = mapaLocalidades(contexto, idProvincia).get(idLocalidad);
		return nombreLocalidad != null ? nombreLocalidad : "";
	}

	// EMM: no sé si está bien que esto venga acá, pero me pareció un catalogo. Lo
	// puse fijo para no llamar a hbs, aunque deberíamos chequera si en pro son los
	// mismos datos
	public static Map<Integer, String> mapaPaquetes() {
		Map<Integer, String> mapa = new LinkedHashMap<>();
		mapa.put(34, "Fácil Pack");
		mapa.put(35, "Buho Pack");
		mapa.put(36, "Gold Pack");
		mapa.put(37, "Platinum Pack");
		mapa.put(38, "Black Pack");
		mapa.put(39, "Fácil Pack");
		mapa.put(40, "Buho Pack");
		mapa.put(41, "Gold Pack");
		mapa.put(42, "Platinum Pack");
		mapa.put(43, "Black Pack");
		mapa.put(44, "Pack");
		mapa.put(45, "Pack");
		mapa.put(46, "Pack");
		mapa.put(47, "Emprendedor Pack");
		mapa.put(48, "BH Pack");
		mapa.put(49, "Paquete Signature Masivo Internacional");
		mapa.put(53, "Emprendedor Black Pack");

		return mapa;
	}

	public static SqlResponse paquetesBeneficios(ContextoHB contexto, String codigoPaquete, String letra) {
		SqlRequest sqlRequest = Sql.request("SelectPaquetesBeneficios", "hbs");
		sqlRequest.sql = "select t1.numero_paquete, t2.desc_beneficio, t2.desc_beneficio_html from [Esales].[dbo].[paquete] t1 inner join [Esales].[dbo].[paquete_beneficio] t2 on t1.id = t2.paquete_id";
		
		if (codigoPaquete != null && !Objeto.empty(letra)) {
			sqlRequest.sql += " where t1.numero_paquete = " + codigoPaquete;
			sqlRequest.sql += " and t1.letra_tc = '" + letra + "'";
		}
		else if (codigoPaquete != null) {
			sqlRequest.sql += " where t1.numero_paquete = " + codigoPaquete;
		}

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		return sqlResponse;
	}

	private static Map<String, String> bancoMapa(){
		Map<String, String> mapa = new HashMap<>();
		mapa.put("005", "The Royal Bank of Scotland N.V.");
		mapa.put("007", "Banco Galicia");
		mapa.put("011", "Banco de la Nación Argentina");
		mapa.put("014", "Banco de la Provincia de Buenos Aires");
		mapa.put("015", "ICBC");
		mapa.put("016", "Citibank N.A.");
		mapa.put("017", "BBVA Banco Francés");
		mapa.put("018", "The Bank of Tokyo-Mitsubishi UFJ, LTD.");
		mapa.put("020", "Banco de la Provincia de Córdoba");
		mapa.put("027", "Banco Supervielle");
		mapa.put("029", "Banco de la Ciudad de Buenos Aires");
		mapa.put("030", "Central de la República Argentina");
		mapa.put("034", "Banco Patagonia");
		mapa.put("044", "Banco Hipotecario");
		mapa.put("045", "Banco de San Juan");
		mapa.put("046", "Banco do Brasil");
		mapa.put("060", "Banco de Tucumán");
		mapa.put("065", "Banco Municipal de Rosario");
		mapa.put("025", "Banco Santander");
		mapa.put("072", "Banco Santander");
		mapa.put("083", "Banco del Chubut");
		mapa.put("086", "Banco de Santa Cruz");
		mapa.put("093", "Banco de la Pampa Sociedad de Economía Mixta");
		mapa.put("094", "Banco de Corrientes");
		mapa.put("097", "Banco Provincia del Neuquén");
		mapa.put("143", "Brubank");
		mapa.put("150", "HSBC Bank Argentina");
		mapa.put("165", "JP Morgan Chase Bank NA");
		mapa.put("191", "Banco Credicoop Cooperativo Limitado");
		mapa.put("198", "Banco de Valores");
		mapa.put("247", "Banco Roela");
		mapa.put("254", "Banco Mariva");
		mapa.put("259", "Banco Itaú Argentina");
		mapa.put("262", "Bank of America National Association");
		mapa.put("266", "BNP Paribas");
		mapa.put("268", "Banco Provincia de Tierra del Fuego");
		mapa.put("269", "Banco de la República Oriental del Uruguay");
		mapa.put("277", "Banco Sáenz");
		mapa.put("281", "Banco Meridian");
		mapa.put("285", "Banco Macro");
		mapa.put("295", "American Express Bank LTD.");
		mapa.put("299", "Banco Comafi");
		mapa.put("300", "Banco de Inversión y Comercio Exterior");
		mapa.put("301", "Banco Piano");
		mapa.put("305", "Banco Julio");
		mapa.put("309", "Nuevo Banco de la Rioja");
		mapa.put("310", "Banco del Sol");
		mapa.put("311", "Nuevo Banco del Chaco");
		mapa.put("312", "MBA Lazard Banco de Inversiones");
		mapa.put("315", "Banco de Formosa");
		mapa.put("319", "Banco CMF");
		mapa.put("321", "Banco de Santiago del Estero");
		mapa.put("322", "Banco Industrial");
		mapa.put("325", "Deutsche Bank");
		mapa.put("330", "Nuevo Banco de Santa Fe");
		mapa.put("331", "Banco Cetelem Argentina");
		mapa.put("332", "Banco de Servicios Financieros");
		mapa.put("336", "Banco Bradesco Argentina");
		mapa.put("338", "Banco de Servicios y Transacciones");
		mapa.put("339", "RCI Banque");
		mapa.put("340", "BACS Banco de Crédito y Securitización");
		mapa.put("341", "Más Ventas");
		mapa.put("384", "Ualá");
		mapa.put("386", "Nuevo Banco de Entre Ríos");
		mapa.put("389", "Banco Columbia");
		mapa.put("405", "Ford Credit Compañía Financiera");
		mapa.put("406", "Metrópolis Compañía Financiera");
		mapa.put("408", "Compañía Financiera Argentina");
		mapa.put("413", "Montemar Compañía Financiera");
		mapa.put("415", "Multifinanzas Compañía Financiera");
		mapa.put("428", "Caja de Crédito Coop. La Capital del Plata LTDA.");
		mapa.put("431", "Banco Coinag");
		mapa.put("432", "Banco de Comercio");
		mapa.put("434", "Caja de Crédito Cuenca Coop. LTDA.");
		mapa.put("437", "Volkswagen Credit Compañía Financiera");
		mapa.put("438", "Cordial Compañía Financiera");
		mapa.put("440", "Fiat Crédito Compañía Financiera");
		mapa.put("441", "GPAT Compañía Financiera");
		mapa.put("442", "Mercedes-Benz Compañía Financiera Argentina");
		mapa.put("443", "Rombo Compañía Financiera");
		mapa.put("444", "John Deere Credit Compañía Financiera");
		mapa.put("445", "PSA Finance Argentina Compañía Financiera");
		mapa.put("446", "Toyota Compañía Financiera de Argentina");
		mapa.put("448", "Finandino Compañía Financiera");
		mapa.put("992", "Provincanje");
		mapa.put("059", "Banco Entre Rios");
		mapa.put("156", "Bica");
		mapa.put("426", "Bica");
		
		//Billeteras 
		mapa.put("500","Ank");
		mapa.put("501","ULTRA");
		mapa.put("502","Bitso");
		mapa.put("503","BKR");
		mapa.put("504","CuentaDigital.com");
		mapa.put("505","TELEPAGOS S.A.");
		mapa.put("506","GARPA S.A. (DolarApp)");
		mapa.put("507","GOAT S.A");
		mapa.put("508","INVOITION");
		mapa.put("509","Mercado Pago");
		mapa.put("510","Nubi");
		mapa.put("511","Paymovil");
		mapa.put("512","Pluspagos");
		mapa.put("513","Prex");
		mapa.put("514","Propago");
		mapa.put("515","Resimple");
		mapa.put("516","Sysworld Digital S.A.");
		mapa.put("517","Satoshitango");
		mapa.put("518","TAP Billetera Virtual");
		mapa.put("520","PagosOnline");
		mapa.put("521","PAGOS360");
		mapa.put("522","Personal Pay");
		mapa.put("523","YPF");
		mapa.put("524","AstroPay");
		mapa.put("525","BAMBA");
		mapa.put("526","COIN Cobro Inmediato");
		mapa.put("527","Belo");
		mapa.put("453","Naranja X");
		mapa.put("528","Yacaré");
		mapa.put("529","Yoy");
		
		return mapa;
	}
	
	public static String banco(String codigo) {
		String descripcion = bancoMapa().get(codigo);
		descripcion = descripcion != null ? descripcion : "";
		return descripcion;
	}
	
	public static String nroBancoInterno(String descripcion) {
	    for (Map.Entry<String, String> entry : bancoMapa().entrySet()) {
	        if (entry.getValue().equals(descripcion)) {
	            return entry.getKey(); 
	        }
	    }
	    return "";
	}


	public static String bancoFiltrado(String codigo) {
		return banco(formatearCodigo(codigo))
				.replace(" S.A.", "")
				.replace("LTD.", "")
				.replace("LTDA.", "");
	}

	public static String bancoLogo(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		
		mapa.put("027", "https://www.hipotecario.com.ar/media/logo-bancos/logo-supervielle.svg");
		mapa.put("025", "https://www.hipotecario.com.ar/media/logo-bancos/logo-santander.svg");
		mapa.put("072", "https://www.hipotecario.com.ar/media/logo-bancos/logo-santander.svg");
		mapa.put("453", "https://www.hipotecario.com.ar/media/logo-bancos/logo-naranja-x.svg");
		mapa.put("281", "https://www.hipotecario.com.ar/media/logo-bancos/logo-meridian.svg");
		mapa.put("509", "https://www.hipotecario.com.ar/media/logo-bancos/logo-mercado-pago.svg");
		mapa.put("285", "https://www.hipotecario.com.ar/media/logo-bancos/logo-macro.svg");
		mapa.put("438", "https://www.hipotecario.com.ar/media/logo-bancos/logo-iudu.svg");
		mapa.put("015", "https://www.hipotecario.com.ar/media/logo-bancos/logo-icbc.svg");
		mapa.put("044", "https://www.hipotecario.com.ar/media/logo-bancos/logo-hipotecario.svg");
		mapa.put("389", "https://www.hipotecario.com.ar/media/logo-bancos/logo-columbia.svg");
		mapa.put("431", "https://www.hipotecario.com.ar/media/logo-bancos/logo-coinag.svg");
		mapa.put("319", "https://www.hipotecario.com.ar/media/logo-bancos/logo-cmf.svg");
		mapa.put("191", "https://www.hipotecario.com.ar/media/logo-bancos/logo-credicoop.svg");
		mapa.put("071", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-santa-fe.svg");
		mapa.put("086", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-santa-cruz.svg");
		mapa.put("045", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-san-juan.svg");
		mapa.put("277", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-saenz.svg");
		mapa.put("247", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-roela.svg");
		mapa.put("014", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-provincia.svg");
		mapa.put("301", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-piano.svg");
		mapa.put("034", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-patagonia.svg");
		mapa.put("097", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-neuquen.svg");
		mapa.put("011", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-nacion.svg");
		mapa.put("065", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-municipal.svg");
		mapa.put("254", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-mariva.svg");
		mapa.put("259", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-itau.svg");
		mapa.put("007", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-galicia.svg");
		mapa.put("059", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-entre-rios.svg");
		mapa.put("448", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-dino.svg");
		mapa.put("310", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-del-sol.svg");
		mapa.put("311", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-del-chaco.svg");
		mapa.put("094", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-corrientes.svg");
		mapa.put("299", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-comafi.svg");
		mapa.put("029", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-ciudad.svg");
		mapa.put("083", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-chubut.svg");
		mapa.put("295", "https://www.hipotecario.com.ar/media/logo-bancos/logo-american-express.svg");
		mapa.put("143", "https://www.hipotecario.com.ar/media/logo-bancos/logo-brubank.svg");
		mapa.put("156", "https://www.hipotecario.com.ar/media/logo-bancos/logo-bica.svg");
		mapa.put("150", "https://www.hipotecario.com.ar/media/logo-bancos/logo-hscb.svg");
		mapa.put("017", "https://www.hipotecario.com.ar/media/logo-bancos/logo-bbva.svg");
		mapa.put("386", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-entre-rios.svg");
		mapa.put("322", "https://www.hipotecario.com.ar/media/logo-bancos/logo-banco-bind.svg");
		
		//Billeteras Virtuales
		mapa.put("384", "https://www.hipotecario.com.ar/media/logo-bancos/logo-uala.svg");
		mapa.put("524", "https://www.hipotecario.com.ar/media/logo-bancos/logo_astropay.svg");
		mapa.put("525", "https://www.hipotecario.com.ar/media/logo-bancos/logo_bamba.svg");
		mapa.put("502", "https://www.hipotecario.com.ar/media/logo-bancos/logo_bitso.svg");
		mapa.put("503", "https://www.hipotecario.com.ar/media/logo-bancos/logo_bkr.svg");
		mapa.put("526", "https://www.hipotecario.com.ar/media/logo-bancos/logo_coincobroinmediato.svg");
		mapa.put("506", "https://www.hipotecario.com.ar/media/logo-bancos/logo_dolarapp.svg");
		mapa.put("507", "https://www.hipotecario.com.ar/media/logo-bancos/logo_goat.svg");
		mapa.put("510", "https://www.hipotecario.com.ar/media/logo-bancos/logo_nubi.svg");
		mapa.put("521", "https://www.hipotecario.com.ar/media/logo-bancos/logo_pagos360.svg");
		mapa.put("511", "https://www.hipotecario.com.ar/media/logo-bancos/logo_paymovil.svg");
		mapa.put("512", "https://www.hipotecario.com.ar/media/logo-bancos/logo_pluspagos.svg");
		mapa.put("513", "https://www.hipotecario.com.ar/media/logo-bancos/logo_prex.svg");
		mapa.put("514", "https://www.hipotecario.com.ar/media/logo-bancos/logo_propago.svg");
		mapa.put("515", "https://www.hipotecario.com.ar/media/logo-bancos/logo_resimple.svg");
		mapa.put("517", "https://www.hipotecario.com.ar/media/logo-bancos/logo_satoshitango.svg");
		mapa.put("516", "https://www.hipotecario.com.ar/media/logo-bancos/logo_sysworlddigitalsa.svg");
		mapa.put("518", "https://www.hipotecario.com.ar/media/logo-bancos/logo-tap.svg");
		mapa.put("527", "https://www.hipotecario.com.ar/media/logo-bancos/logo-belo.svg");
		mapa.put("522", "https://www.hipotecario.com.ar/media/logo-bancos/logo-personal-pay.svg");
		mapa.put("523", "https://www.hipotecario.com.ar/media/logo-bancos/logo_ypf.svg");
		mapa.put("529", "https://www.hipotecario.com.ar/media/logo-bancos/logo-yoy.svg");
		
		String descripcion = mapa.get(formatearCodigo(codigo));
		descripcion = descripcion != null ? descripcion
				: "https://www.hipotecario.com.ar/media/logo-bancos/logo-default.svg";
		return descripcion;
	}

	public static String formatearCodigo(String codigo) {
		if(codigo == null || codigo.isEmpty()){
			return null;
		}

		if(codigo.length() > 3){
			return codigo.substring(codigo.length() - 3);
		}
		else if(codigo.length() == 3){
			return codigo;
		}
		else if(codigo.length() == 2){
			return "0" + codigo;
		}
		else {
			return "00" + codigo;
		}
	}


	public static String nombreMes(Integer numeroMes) {
		Map<Integer, String> mapa = new HashMap<>();
		mapa.put(1, "Enero");
		mapa.put(2, "Febrero");
		mapa.put(3, "Marzo");
		mapa.put(4, "Abril");
		mapa.put(5, "Mayo");
		mapa.put(6, "Junio");
		mapa.put(7, "Julio");
		mapa.put(8, "Agosto");
		mapa.put(9, "Septiembre");
		mapa.put(10, "Octubre");
		mapa.put(11, "Noviembre");
		mapa.put(12, "Diciembre");
		return mapa.get(numeroMes);
	}

	public static ApiResponse formaPagoTC(ContextoHB contexto) {
		ApiRequest request = Api.request("FormaPago", "catalogo", "GET", "/v1/formapago", contexto);
		request.query("marca", "2");
		request.cacheSesion = false;
		return Api.response(request);
	}

	public static Map<String, Object> categoriaMonotributo(ContextoHB contexto) {
//		SqlRequest sqlRequest = Sql.request("SelectCategoriaMonotributo", "Catalogo");
//		sqlRequest.sql = "SELECT [CMT_Id] as id, [CMT_Descripcion] as descripcion, [CMT_Estado] as estado FROM [Catalogos].[dbo].[CategoriaMonotributo]";
//		SqlResponse sqlResponse = Sql.response(sqlRequest);
//		return sqlResponse;
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("A", "01");
		mapa.put("B", "02");
		mapa.put("C", "03");
		mapa.put("D", "04");
		mapa.put("E", "05");
		mapa.put("F", "06");
		mapa.put("G", "07");
		mapa.put("H", "08");
		mapa.put("I", "09");
		mapa.put("J", "10");
		mapa.put("K", "11");
		return mapa;
	}

	public static Map<String, Object> categoriaMonotributoXMonto(ContextoHB contexto) {
//		SqlRequest sqlRequest = Sql.request("SelectCategoriaMonotributo", "CampaniasWF");
//		sqlRequest.sql = "SELECT * FROM [CampaniasWF].[dbo].[CategoriaMonotributoIngresos]";
//		SqlResponse sqlResponse = Sql.response(sqlRequest);
//		return sqlResponse;
		Map<String, Object> mapa = new HashMap<>();
		mapa.put("A", "31080.11");
		mapa.put("B", "46200.16");
		mapa.put("C", "64680.22");
		mapa.put("D", "89040.30");
		mapa.put("E", "117600.40");
		mapa.put("F", "147000.50");
		mapa.put("G", "176400.60");
		mapa.put("H", "218400.74");
		mapa.put("I", "244440.83");
		mapa.put("J", "280140.96");
		mapa.put("K", "311001.06");
		return mapa;
	}

	public static String bancoCVU(String codigo) {
		Map<String, String> mapa = new HashMap<>();
		mapa.put("0000047","Ank");
		mapa.put("0000067","ULTRA");
		mapa.put("0000025","Bitso");
		mapa.put("0000039","BKR");
		mapa.put("0000043","CuentaDigital.com");
		mapa.put("0000045","TELEPAGOS S.A.");
		mapa.put("0000069","GARPA S.A. (DolarApp)");
		mapa.put("0000114","GOAT S.A");
		mapa.put("0000011","INVOITION");
		mapa.put("0000003","Mercado Pago");
		mapa.put("0000029","Nubi");
		mapa.put("0000027","Paymovil");
		mapa.put("0000006","Pluspagos (Billetera Santa Fe)");
		mapa.put("0000013","Prex");
		mapa.put("0000074","Propago");
		mapa.put("0000034","Resimple");
		mapa.put("0000066","Sysworld Digital S.A.");
		mapa.put("0000022","Satoshitango");
		mapa.put("0000040","TAP Billetera Virtual");
		mapa.put("0000007","Ualá");
		mapa.put("0000127","PagosOnline");
		mapa.put("0000079","PAGOS360");
		mapa.put("0000076","Personal Pay");
		mapa.put("0000122","YPF");
		mapa.put("0000177","AstroPay");
		mapa.put("0000184","AstroPay");
		mapa.put("0000120","BAMBA");
		mapa.put("0000123","COIN Cobro Inmediato");
		mapa.put("0000139","Belo");
		mapa.put("0150532","Yoy");
		String descripcion = mapa.get(codigo);
		descripcion = descripcion != null ? descripcion : "";
		return descripcion;
	}

}
