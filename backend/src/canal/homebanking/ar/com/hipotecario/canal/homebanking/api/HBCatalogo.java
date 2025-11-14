package ar.com.hipotecario.canal.homebanking.api;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;

public class HBCatalogo {

	public static Respuesta nacionalidades(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		ApiResponse datos = RestCatalogo.paises(contexto);
		if (datos.hayError()) {
			return Respuesta.error();
		}
		datos.ordenar("descripcion");
		for (Objeto dato : datos.objetos()) {
			if (dato.string("id").equals("80")) {
				respuesta.add("paises", new Objeto().set("id", dato.integer("id")).set("descripcion", dato.string("nacionalidad")));
			}
		}
		for (Objeto dato : datos.objetos()) {
			if (!dato.string("id").equals("80")) {
				respuesta.add("paises", new Objeto().set("id", dato.integer("id")).set("descripcion", dato.string("nacionalidad")));
			}
		}
		return respuesta;
	}

	public static Respuesta provinciasPorCodigoPostal(ContextoHB contexto) {
		String codigoPostal = contexto.parametros.string("codigoPostal");

		Respuesta respuesta = new Respuesta();
		Map<Integer, String> provincias = "".equals(codigoPostal) ? RestCatalogo.mapaProvincias(contexto) : RestCatalogo.mapaProvinciasPorCodigoPostal(contexto, codigoPostal);
		for (Integer id : provincias.keySet()) {
			respuesta.add("provincias", new Objeto().set("id", id).set("descripcion", provincias.get(id)));
		}
		return respuesta;
	}

	public static Respuesta localidadesPorCodigoPostal(ContextoHB contexto) {
		Integer idProvincia = contexto.parametros.integer("idProvincia");
		String codigoPostal = contexto.parametros.string("codigoPostal");

		if (Objeto.anyEmpty(idProvincia)) {
			return Respuesta.parametrosIncorrectos();
		}

		Respuesta respuesta = new Respuesta();
		Map<Integer, String> localidades = codigoPostal.equals("") ? RestCatalogo.mapaLocalidades(contexto, idProvincia) : RestCatalogo.mapaLocalidadesPorCodigoPostal(contexto, idProvincia, codigoPostal);
		for (Integer id : localidades.keySet()) {
			respuesta.add("localidades", new Objeto().set("id", id).set("descripcion", localidades.get(id)));
		}

		return respuesta;
	}

	public static Respuesta filtraSituaciones(ApiResponse datos) {
		Respuesta situaciones = new Respuesta();

		for (Objeto dato : datos.objetos()) {
			if (dato.integer("Id") == 6) {
				situaciones.add("situaciones", new Objeto().set("id", dato.integer("Id")).set("descripcion", "Monotributista"));
				situaciones.add("situaciones", new Objeto().set("id", 66).set("descripcion", "Responsable Inscripto"));

			} else if (dato.integer("Id") != 99 && dato.integer("Id") != 2) {
				String descripcion = dato.string("Descripcion");
				if (dato.integer("Id") == 1) {
					descripcion = "Relación de dependencia fijo";
				}
				situaciones.add("situaciones", new Objeto().set("id", dato.integer("Id")).set("descripcion", descripcion));
			}

		}
		return situaciones;
	}

	private static Respuesta situacionesLaboralesXfuncionalidad(ContextoHB contexto, ApiResponse datos) {
		String funcionalidad = contexto.parametros.string("funcionalidad");
		Respuesta respuesta = new Respuesta();

		datos.ordenar("descripcion");
		if (funcionalidad.equalsIgnoreCase("canal-amarillo")) {
			return filtraSituaciones(datos);
		}

		for (Objeto dato : datos.objetos()) {
			respuesta.add("situaciones", new Objeto().set("id", dato.integer("Id")).set("descripcion", dato.string("Descripcion")));
		}
		return respuesta;
	}

	public static Respuesta situacionesLaborales(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		ApiResponse datos = RestCatalogo.situacionesLaborales(contexto);
		if (datos.hayError()) {
			return Respuesta.error();
		}

		respuesta = situacionesLaboralesXfuncionalidad(contexto, datos);
		return respuesta;
	}

	public static Respuesta situacionesVivienda(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		respuesta.add("situaciones", new Objeto().set("id", "01").set("descripcion", "PROPIA"));
		respuesta.add("situaciones", new Objeto().set("id", "02").set("descripcion", "ALQUILADA"));
		respuesta.add("situaciones", new Objeto().set("id", "03").set("descripcion", "FAMILIAR"));
		respuesta.add("situaciones", new Objeto().set("id", "04").set("descripcion", "PRESTADA"));
		respuesta.add("situaciones", new Objeto().set("id", "05").set("descripcion", "DE LA EMPRESA"));
		return respuesta;
	}

	public static Respuesta nivelesEstudio(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		ApiResponse datos = RestCatalogo.nivelesEstudio(contexto);
		if (datos.hayError()) {
			return Respuesta.error();
		}
		datos.ordenar("descripcion");
		for (Objeto dato : datos.objetos()) {
			respuesta.add("nivelesEstudio", new Objeto().set("id", dato.integer("Id")).set("descripcion", dato.string("Descripcion")));
		}
		return respuesta;
	}

	public static Respuesta estadosCiviles(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		ApiResponse datos = RestCatalogo.estadosCiviles(contexto);
		if (datos.hayError()) {
			return Respuesta.error();
		}
		datos.ordenar("descripcion");
		for (Objeto dato : datos.objetos()) {
			respuesta.add("estadosCiviles", new Objeto().set("id", dato.string("id")).set("descripcion", dato.string("descripcion")));
		}
		return respuesta;
	}

	public static Map<Integer, String> mapaProfesiones() {
		Map<Integer, String> mapa = new LinkedHashMap<>();
		mapa.put(30701, "ABOGADO");
		mapa.put(10100, "ACTOR/DOBLE DE FILM");
		mapa.put(30305, "ACTUARIO");
		mapa.put(31703, "ADM. PROPIEDADES");
		mapa.put(31602, "AGENTE VIAJES");
		mapa.put(22200, "AGRICULTOR");
		mapa.put(31005, "AGRIMENSOR");
		mapa.put(20500, "ALBAÑIL");
		mapa.put(31403, "ANALISTA AMBIENTAL");
		mapa.put(30409, "ANALISTA DE SISTEMAS");
		mapa.put(31401, "ANALISTA ECOLÓGICO");
		mapa.put(31112, "ANESTESISTA");
		mapa.put(30903, "ANTROPÓLOGO");
		mapa.put(30201, "ARQUITECTO");
		mapa.put(10800, "ARTESANO");
		mapa.put(31202, "ASISTENTE DENTAL");
		mapa.put(30511, "ASISTENTE SOCIAL");
		mapa.put(11100, "AUTOMOVILISTA");
		mapa.put(10700, "BAILARÍN");
		mapa.put(30802, "BIOQUÍMICO");
		mapa.put(20003, "BOMBERO");
		mapa.put(11200, "BOXEADOR");
		mapa.put(12100, "BUZO");
		mapa.put(20004, "CAFETERO");
		mapa.put(20005, "CAJERO");
		mapa.put(11000, "CANTANTE");
		mapa.put(11500, "CARDENAL");
		mapa.put(20006, "CARNICERO");
		mapa.put(20800, "CARPINTERO");
		mapa.put(20007, "CERRAJERO");
		mapa.put(22500, "CHAPISTA");
		mapa.put(21100, "CHOFER");
		mapa.put(21800, "COCINERO");
		mapa.put(23400, "COMERCIANTE");
		mapa.put(31302, "CONDUCTOR");
		mapa.put(30303, "CONTADOR PÚBLICO");
		mapa.put(21300, "COSMETÓLOGA-ESTETICISTA");
		mapa.put(31304, "CRÍTICO");
		mapa.put(30203, "DECORADOR / PAISAJISTA");
		mapa.put(23200, "DECORADOR SIN TÍTULO");
		mapa.put(30311, "DESPACHANTE DE ADUANAS");
		mapa.put(10400, "DIBUJANTE");
		mapa.put(30202, "DISEÑADOR");
		mapa.put(30102, "DISEÑADOR PAISAJISTA");
		mapa.put(20200, "ELECTRICISTA");
		mapa.put(30703, "ESCRIBANO");
		mapa.put(10500, "ESCRITOR");
		mapa.put(10600, "ESCULTOR");
		mapa.put(30801, "FARMACÉUTICO");
		mapa.put(31102, "FONOAUDIÓLOGO");
		mapa.put(31306, "FOTÓGRAFO / CAMAROGRAFO");
		mapa.put(22700, "FUMIGADOR");
		mapa.put(11300, "FUTBOLISTA");
		mapa.put(20400, "GASISTA");
		mapa.put(31701, "GESTOR");
		mapa.put(31402, "GUARDAPARQUE");
		mapa.put(31601, "GUÍA TURÍSTICA");
		mapa.put(20008, "HELADERO");
		mapa.put(22000, "HERRERO");
		mapa.put(31001, "ING. CIVIL");
		mapa.put(31003, "ING. ELECTRICISTA - ELECTRÓNICO -MECÁNICO");
		mapa.put(30103, "ING. FORESTAL");
		mapa.put(31002, "ING. INDUSTRIAL");
		mapa.put(30101, "INGENIERO AGRÓNOMO");
		mapa.put(31503, "INSTRUCTOR-ENTRENADOR");
		mapa.put(20900, "JARDINERO");
		mapa.put(12200, "JOCKEY DE CABALLO DE CARRERAS");
		mapa.put(31103, "KINESIÓLOGO");
		mapa.put(31109, "LABORAT.");
		mapa.put(30106, "LIC. ADM.- TECNOLOGÍA AGRARIA");
		mapa.put(30301, "LIC. ADMINISTRACIÓN");
		mapa.put(31004, "LIC. ANÁLISIS DE SISTEMAS");
		mapa.put(30405, "LIC. ASTRONOMÍA-GEOFÍSICA-GEOLOGÍA");
		mapa.put(30905, "LIC. BIBLIOT.-MUSEOLOGÍA");
		mapa.put(30401, "LIC. BIOLOGÍA");
		mapa.put(30406, "LIC. BIOTECNOLOGÍA");
		mapa.put(30104, "LIC. COMERC. AGROPEC.");
		mapa.put(30306, "LIC. COMERCIO INTERN.");
		mapa.put(30402, "LIC. COMPUTACIÓN");
		mapa.put(31308, "LIC. COMUNICACIÓN SOCIAL");
		mapa.put(30507, "LIC. CS. EDUCACIÓN");
		mapa.put(30501, "LIC. CS. POLÍTICAS");
		mapa.put(30307, "LIC. DIRECCIÓN DE EMPRESAS");
		mapa.put(30302, "LIC. ECONOMÍA");
		mapa.put(31106, "LIC. ENFERMERÍA");
		mapa.put(30902, "LIC. FILOSOFÍA");
		mapa.put(30403, "LIC. FÍSICA-MATEMÁTICA-QUÍMICA");
		mapa.put(31605, "LIC. GASTRONOMÍA");
		mapa.put(30904, "LIC. GEOGRAFÍA-HISTORIA-ARTES");
		mapa.put(30408, "LIC. INFORMÁTICA");
		mapa.put(30407, "LIC. INVESTIG. OPERATIVA");
		mapa.put(30901, "LIC. LETRAS");
		mapa.put(30105, "LIC. MECÁNICA AGRÍCOLA");
		mapa.put(30404, "LIC. METEOROLOGÍA");
		mapa.put(31104, "LIC. NUTRICIÓN");
		mapa.put(31105, "LIC. OBSTETRICIA");
		mapa.put(30308, "LIC. ORG. EMPRESAS");
		mapa.put(30309, "LIC. ORG. SEGUROS");
		mapa.put(30505, "LIC. PSICOLOGÍA");
		mapa.put(30310, "LIC. PUBLICIDAD / MARKETING");
		mapa.put(30502, "LIC. REL. DEL TRABAJO");
		mapa.put(30510, "LIC. REL. INDUSTRIALES");
		mapa.put(30509, "LIC. REL. INTERNACIONALES");
		mapa.put(30508, "LIC. REL. PÚBLICAS");
		mapa.put(30304, "LIC. SISTEMAS");
		mapa.put(30503, "LIC. SOCIOLOGÍA");
		mapa.put(30504, "LIC. TRABAJO SOCIAL");
		mapa.put(31303, "LOCUTOR");
		mapa.put(30204, "M.MAYOR DE OBRAS");
		mapa.put(31501, "MAESTRO");
		mapa.put(21200, "MANICURA-PODÓLOGO");
		mapa.put(30705, "MARTILLERO");
		mapa.put(20100, "MECÁNICO");
		mapa.put(31300, "MEDIOS DE COMUNICACIÓN");
		mapa.put(11800, "MILITAR / POLICIA / SEGURIDAD /VIGILANCIA");
		mapa.put(23500, "MONTADOR INSTALACIONES + DE 250 V");
		mapa.put(21600, "MOZO");
		mapa.put(20009, "MUCAMA");
		mapa.put(31101, "MÉDICO");
		mapa.put(10200, "MÚSICO-COMPOSITOR");
		mapa.put(40001, "NO TIENE");
		mapa.put(11600, "OBISPO");
		mapa.put(31201, "ODONTÓLOGO");
		mapa.put(20600, "OPERARIO");
		mapa.put(30803, "OPTICO");
		mapa.put(10900, "ORFEBRE-EBANISTA");
		mapa.put(31006, "OTROS");
		mapa.put(11900, "OTROS");
		mapa.put(21900, "PANADERO");
		mapa.put(21400, "PELUQUERA-O");
		mapa.put(23300, "PENADO");
		mapa.put(31301, "PERIODISTA");
		mapa.put(30706, "PERITO CALÍGRAFO");
		mapa.put(20002, "PERITO MERCANTIL");
		mapa.put(22400, "PESCADOR");
		mapa.put(21000, "PINTOR");
		mapa.put(10300, "PINTOR");
		mapa.put(23000, "PIZZERO");
		mapa.put(20300, "PLOMERO");
		mapa.put(20010, "PORTERO");
		mapa.put(20011, "PRECEPTOR");
		mapa.put(30704, "PROCURADOR");
		mapa.put(31502, "PROFESOR");
		mapa.put(20001, "PROMOTOR");
		mapa.put(30506, "PSICOPEDAGOGÍA");
		mapa.put(22800, "REMISERO /TAXISTA");
		mapa.put(12000, "RESTAURADOR");
		mapa.put(11700, "SACERDOTE");
		mapa.put(21500, "SASTRE-MODISTA-COSTURERA-BORDADORA");
		mapa.put(20012, "SODERO");
		mapa.put(22300, "TAMBERO");
		mapa.put(22100, "TAPICERO");
		mapa.put(31702, "TASADOR-MARTILLERO");
		mapa.put(31603, "TEC. ADM. HOTELERA");
		mapa.put(31107, "TEC. ANÁLISIS CLÍNICOS");
		mapa.put(31111, "TEC. HEMOTERAPIA");
		mapa.put(31108, "TEC. INSTRUMENT.");
		mapa.put(31110, "TEC. RADIÓLOGO");
		mapa.put(31604, "TEC. SERV. GASTRONOMICOS");
		mapa.put(11400, "TENISTA");
		mapa.put(23100, "TORNERO");
		mapa.put(30702, "TRADUCTOR PÚBLICO-INTÉRPRETE");
		mapa.put(22900, "TRANSPORTISTA / CAMIONERO / FLETERO");
		mapa.put(23600, "TRIPULACION NO ADMINIST DE BUQUES");
		mapa.put(31307, "TÉCNICO EDICIÓN-COMPAGINADOR");
		mapa.put(31305, "TÉCNICO RADIO-TV");
		mapa.put(30601, "VETERINARIO");
		mapa.put(31113, "VISITADOR MÉDICO");
		mapa.put(22600, "YESERO");
		mapa.put(21700, "ZAPATERO");
		return mapa;
	}

	public static Respuesta sucursales(ContextoHB contexto) {
		Integer idProvincia = contexto.parametros.integer("idProvincia");

		Respuesta respuesta = new Respuesta();
		ApiResponse datos = RestCatalogo.sucursales(contexto);
		if (datos.hayError()) {
			return Respuesta.error();
		}
		for (Objeto dato : datos.objetos()) {
			if (idProvincia == null || idProvincia.equals(dato.integer("codProvincia"))) {
				Objeto sucursal = new Objeto();
				sucursal.set("codigoSucursal", dato.string("CodSucursal"));
				sucursal.set("descripcionSucursal", dato.string("DesSucursal"));
				sucursal.set("codigoProvincia", dato.string("codProvincia"));
				sucursal.set("nombreProvincia", dato.string("NomProvincia"));
				sucursal.set("domicilio", dato.string("Domicilio"));
				sucursal.set("latitud", dato.string("Latitud"));
				sucursal.set("longitud", dato.string("Longitud"));
				respuesta.add("sucursales", sucursal);
			}
		}
		return respuesta;
	}

	public static Respuesta formaPagoTC(ContextoHB contexto) {
		try {
			Respuesta respuesta = new Respuesta();
			ApiResponse formas = RestCatalogo.formaPagoTC(contexto);
			if (formas.hayError()) {
				return Respuesta.error();
			}
			respuesta.set("formasPago", formas);
			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	public static String idCategoriaMonotributo(ContextoHB contexto, String letra) {
		Map<String, Object> categorias = RestCatalogo.categoriaMonotributo(contexto);
		return categorias.get(letra).toString();
	}

	public static String montoMonotributo(ContextoHB contexto, String letra) {
		Map<String, Object> categorias = RestCatalogo.categoriaMonotributoXMonto(contexto);
		return categorias.get(letra).toString();
	}

	public static Respuesta rubros(ContextoHB contexto) {
		Respuesta respuesta = new Respuesta();
		List<String> rubros = new ArrayList<String>();
		rubros.add("Combustible");
		rubros.add("Construcción");
		rubros.add("Librería");
		rubros.add("Supermercados");
		rubros.add("Delivery");
		rubros.add("Carnicerías");
		rubros.add("Pescaderías");
		rubros.add("Panaderías");
		rubros.add("Casas de Pastas");
		rubros.add("Farmacias y perfumerías");
		rubros.add("Hogar y Deco");
		rubros.add("Veterinarias");
		rubros.add("Modo");
		rubros.add("Otros");
		respuesta.set("rubros", rubros);
		return respuesta;
	}

}
