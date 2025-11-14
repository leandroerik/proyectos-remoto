package ar.com.hipotecario.backend.servicio.api.prisma;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.prisma.Reclamos.Reclamo;
import ar.com.hipotecario.backend.servicio.api.prisma.Reclamos.ReclamosBody.DatosGenerales;
import ar.com.hipotecario.backend.servicio.api.prisma.Reclamos.ReclamosBody.Item;

public class Reclamos extends ApiObjetos<Reclamo> {

	/* ========== ATRIBUTOS ========== */
	public static class EstadosReclamos {
		public String idEstado;
		public String descEstado;
		public String reclamos;
	}

	public static class Clientes {
		public String idCobis;
		public String tdoc;
		public String ndoc;
		public String nombre;
		public String apellido;
		public String telefono;
		public String email;
		public String domPostal;
	}

	public static class Canales {
		public String idCanal;
		public String descCanal;
		public String enLinea;
		public String fechaHasta;
	}

	public static class Items {
		public String idItem;
		public String idReclamo;
		public String idTema;
		public String descripcionTema;
		public String idTipoIncidente;
		public String idGrupo;
		public String idUsuario;
		public String idGrupoRespuesta;
		public String idUsuarioRespuesta;
		public String idMedio;
		public String idRespuesta;
		public Fecha fechaRecepcion;
		public Fecha fechaEstado;
		public Fecha fechaUltimaModificacion;
		public Fecha fechaEstimadaResolucion;
		public String numeroCarpetin;
		public String origen;
		public String idSucursal;
		public String idProducto;
		public String numeroProducto;
		public String codigoProducto;
		public String respuesta;
		public String notaResolucion;
		public String notaDatos;
		public String idEstado;
		public String cierreMasivo;
		public String estaCancelado;
		public String accion;
		public String idTipoResultado;
		public String documentos;
		public String ordenes;
		public String datos;
		public String idAnalistaSupervisor;
		public String idSucursalProducto;
		public String idUsuarioLogueado;
		public String sectorDescripcion;
		public String idLider;
		public String idGerente;
		public String idSupervisor;
		public String sectorDerivado;
		public String prioridad;
		public String linkSAC;
		public String notifica;
		public Boolean respuestaModificada;
		public String autorizacion;
		public String grupoDescripcion;
		public String estadoItemDescripcion;
		public Integer plazo;
		public String idGrupoPadreTema;
		public String numeroUPC;
		public Integer nroReintentos;
	}

	public static class Reclamo extends ApiObjeto {
		public String idReclamo;
		public EstadosReclamos estadosReclamos;
		public Clientes clientes;
		public Canales canales;
		public String idCanal;
		public Fecha fechaApertura;
		public Fecha fechaEstado;
		public List<Items> items;
		public boolean conflictivo;
		public Fecha fechaFin;
		public String idProcesoWfk;
	}

	public static class ResponsePost extends ApiObjeto {
		public String resultado;
		public String idReclamo;
	}

	public static class ResponsePatch extends ApiObjeto {
		public String idProcesoWfk;
		public String idReclamo;
		public Items items;
	}

	public static class ReclamosBody extends ApiObjeto {

		/* ========== ATRIBUTOS ========== */
		public static class Actividades {
			public String descripcion;
			public String descripcionGrupo;
			public String descripcionMotivoContacto;
			public String descripcionTipoActividad;
			public Fecha fechaActividad;
			public String id;
			public String idGrupo;
			public String idUsuario;
		};

		public static class DatosGenerales {
			public String apellido;
			public String codigoCliente;
			public String correo;
			public Fecha fechaApertura;
			public String nombre;
			public String numeroDocumento;
			public String telefonoContacto;
			public String tipoDocumento;
		};

		public static class Ordenes {
			public String descripcionEstado;
			public String descripcionSector;
			public String fechaOrden;
			public String idGrupo;
			public String idOrden;
			public String idOrdenEstado;
			public String idSector;
			public String idUsuario;
			public String observacion;
			public String referencia;
		}

		public static class Item {
			public String idItem;
			public String idTema;
			public String idTipoIncidente;
			public Fecha fechaRecepcion;
			public Fecha fechaEstado;
			public String idProducto;
			public String codigoProducto;
			public String numeroProducto;
			public String idEstado;
			public String accion;
			public String idGrupo;
			public String idSucursal;
			public String idUsuario;
			public String idUsuarioLogueado;
			public String notaDatos;
		};

		public List<Actividades> actividades;
		public Boolean conflictivo;
		public DatosGenerales datosGenerales;
		public String idCanal;
		public String idProcesoWfk;
		public String idReclamo;
		public List<Item> items;
	}

	public static class ReclamosParams extends ApiObjeto {
		public String idReclamo;
		public String idEstadoItem;
		public String idEstadoReclamo;
		public Fecha fechaAperturaDesde;
		public Fecha fechaAperturaHasta;
		public Fecha fechaEstadoDesde;
		public Fecha fechaEstadoHasta;
		public String nombre;
		public String apellido;
		public String numeroDocumento;
		public String idTema;
		public String idUsuario;
		public String idGrupo;
		public String idMedio;
		public Boolean filtroItemsSinUsuarioAsignado;
		public Integer ultimoIdRetornado;
		public String tipoConsulta;
		public Boolean vigente;
		public String referencia;
		public Boolean enTermino;
		public List<String> canalesId;
		public List<String> sucursalesId;
		public List<String> productosId;
		public List<String> usuariosId;
		public List<String> estadosItemId;
		public List<String> gruposId;
		public List<String> temasId;
		public String datosItem;
		public Boolean ultimosReclamos;
		public String maxResults;
	}

	/* ========== SERVICIOS ========== */
	// API-Prisma_ConsultaReclamosContainer
	public static Boolean validParamsReclamoContainer(String param) {
		List<String> paramsValid = Arrays.asList("idReclamo", "idEstadoItem", "idEstadoReclamo", "fechaAperturaDesde", "fechaAperturaHasta", "fechaEstadoDesde", "nombre", "apellido", "numeroDocumento", "idUsuario", "idGrupo", "filtroItemsSinUsuarioAsignado", "ultimoIdRetornado", "tipoConsulta", "vigente", "referencia", "enTermino", "canalesId", "sucursalesId", "productosId", "usuariosId", "estadoItemId", "gruposId", "datosItem", "ultimosReclamos", "maxResults");

		return paramsValid.contains(param);
	}

	public static Reclamos getReclamosContainer(Contexto contexto, String idCobis) {
		return getReclamosContainer(contexto, idCobis, null);
	}

	public static Reclamos getReclamosContainer(Contexto contexto, String idCobis, ReclamosParams params) {
		ApiRequest request = new ApiRequest("ComprobantesReclamosContainer", "prisma", "GET", "/v1/reclamosContainer", contexto);
		request.query("idCobis", idCobis);

		if (params != null) {
			for (String clave : params.objeto().keys()) {
				if (params.objeto().get(clave) != null) {
					request.query(clave, params.objeto().get(clave));
				}
			}
		}

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Reclamos.class);
	}

	// API-Prisma_ConsultaReclamosSAC
	public static Reclamos getReclamosSac(Contexto contexto, String idCobis) {
		return getReclamosSac(contexto, idCobis, null);
	}

	public static Reclamos getReclamosSac(Contexto contexto, String idCobis, String maxResults) {
		ApiRequest request = new ApiRequest("ComprobantesReclamosSac", "prisma", "GET", "/v1/reclamosSAC", contexto);
		request.query("idCobis", idCobis);

		if (maxResults != null)
			request.query("maxResults", maxResults);

		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(Reclamos.class);
	}

	// API-Prisma_AltaReclamo
	public static ResponsePost post(Contexto contexto, ReclamosBody reclamo) {
		ApiRequest request = new ApiRequest("AltaReclamoPrisma", "prisma", "POST", "/v1/reclamo", contexto);
		request.body(reclamo.objeto());
		request.cache = true;

		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200, 204), request, response);
		return response.crear(ResponsePost.class);
	}

	/* ========== TEST ========== */
	public static void main(String[] args) {
		Contexto contexto = contexto("HB", "homologacion");
		String method = "getReclamosContainer";

		if (method.equals("getReclamosContainer")) {
			ReclamosParams params = new ReclamosParams();
			params.idReclamo = "549137";
			params.idTema = "1275";
			params.fechaAperturaDesde = new Fecha("2006-05-01", "yyyy-MM-dd");

			Reclamos datos = getReclamosContainer(contexto, "166069", params);
			imprimirResultado(contexto, datos);
		}

		if (method.equals("getReclamosSac")) {
			Reclamos datos = getReclamosSac(contexto, "5943010", "50");
			imprimirResultado(contexto, datos);
		}

		if (method.equals("post")) {
			ReclamosBody reclamo = new ReclamosBody();
			DatosGenerales datosGenerales = new DatosGenerales();
			datosGenerales.nombre = "Diegol";
			datosGenerales.apellido = "Perez";
			datosGenerales.codigoCliente = "36481";
			datosGenerales.tipoDocumento = "1";
			datosGenerales.numeroDocumento = "28414895";
			datosGenerales.fechaApertura = new Fecha("2021-04-07", "yyyy-MM-dd");

			reclamo.datosGenerales = datosGenerales;
			reclamo.idCanal = "10";
			reclamo.conflictivo = false;

			Item item = new Item();
			item.idItem = "1";
			item.idTema = "1342";
			item.idTipoIncidente = "P";
			item.fechaRecepcion = new Fecha("2021-04-07", "yyyy-MM-dd");
			item.fechaEstado = item.fechaRecepcion;
			item.idProducto = "CLI";
			item.codigoProducto = "C5C9E59E-A2F1-C4D0-8BA5-78AC6D100000";
			item.numeroProducto = "C5C9E59E-A2F1-C4D0-8BA5-78AC6D100000";
			item.idEstado = "PER";
			item.accion = "PENDIENTE_RESOLUCION";
			item.idGrupo = "52";
			item.idSucursal = "";
			item.idUsuario = "HB";
			item.idUsuarioLogueado = "HB";
			item.notaDatos = "Solicitud de Documentación Digital";

			List<ReclamosBody.Item> items = new ArrayList<ReclamosBody.Item>();
			items.add(item);
			reclamo.items = items;

			ResponsePost datos = post(contexto, reclamo);
			imprimirResultado(contexto, datos);
		}
	}
}
