package ar.com.hipotecario.mobile.servicio;

public class RestPrisma {

	/* ========== SERVICIOS ========== */
//	@Deprecated
//	public static ApiResponse generarReclamo(Contexto contexto, String idCanal, String idTemaReclamo, String idProducto, 
//												String codigoProducto, String numeroProducto, boolean esPaquete,
//												String idGrupo, String idSucursal) {
//		ApiRequest request = Api.request("AltaReclamoPrisma", "prisma", "POST", "/v1/reclamo", contexto);
//		Date hoy = new Date();
//		idCanal = (idCanal == null || "".equals(idCanal))? "1" : idCanal;
//		
//		Objeto datosGenerales = new Objeto();
//		datosGenerales.set("codigoCliente", contexto.idCobis());
//		datosGenerales.set("fechaApertura", new SimpleDateFormat("yyyy-MM-dd").format(new Date()));
//		datosGenerales.set("tipoDocumento", contexto.persona().idTipoDocumento());
//		datosGenerales.set("numeroDocumento", contexto.persona().numeroDocumento());
//		datosGenerales.set("apellido", (contexto.persona().apellido().length()>30)?contexto.persona().apellido().substring(0, 30):contexto.persona().apellido());
//		datosGenerales.set("nombre", (contexto.persona().nombre().length()>30)?contexto.persona().nombre().substring(0, 30):contexto.persona().nombre());
//		request.body("datosGenerales", datosGenerales);
//		request.body("idCanal", idCanal);
//		request.body("conflictivo", false);
//		
//		Objeto item = new Objeto();
//		item.set("idItem", "1");
//		item.set("idTema", idTemaReclamo);
//		item.set("idTipoIncidente", "P");
//		item.set("fechaRecepcion", new SimpleDateFormat("yyyy-MM-dd").format(hoy));
//		item.set("fechaEstado", new SimpleDateFormat("yyyy-MM-dd").format(hoy));
//		item.set("idProducto", idProducto);
//		item.set("codigoProducto", codigoProducto);
//		item.set("numeroProducto", numeroProducto);
//		item.set("idEstado", "PER");
//		item.set("accion", "PENDIENTE_RESOLUCION");
//		if (esPaquete) {
//			item.set("idProducto", "PBH");
//		}
//		item.set("idGrupo", idGrupo);
//		item.set("idSucursal", idSucursal);
//		item.set("idUsuario", "HB");
//		item.set("idUsuarioLogueado", "HB");
//		item.set("notaDatos", "Solicitud de Baja de Producto enviada a traves de Home Banking");
//		request.body.set("items", new Objeto().add(item));
//		
//		ApiResponse response = Api.response(request, contexto.idCobis());
//		
//		return response;
//	}

}
