package ar.com.hipotecario.backend.servicio.sql;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.CiudadesWF.CiudadWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.ProvinciasWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SolicitudesWorkflow;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SucursalesWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.SucursalesWF.SucursalWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoDistribucion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoDistribucion.TarjetaCreditoDistribucion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupo;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupo.TarjetaCreditoGrupo;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupoAfinidad;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupoAfinidad.TarjetaCreditoGrupoAfinidad;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoModeloLiquidacion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoModeloLiquidacion.TarjetaCreditoModeloLiquidacion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoProducto;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoProducto.TarjetaCreditoProducto;

public class SqlCampaniasWF extends Sql {

	public static String SQL = "campaniaswf";

	/* ========== SERVICIOS ========== */
	public static Futuro<CiudadesWF> ciudades(Contexto contexto, String codigoPostal) {
		return futuro(() -> CiudadesWF.get(contexto, codigoPostal));
	}

	public static Futuro<CiudadWF> ciudad(Contexto contexto, String id) {
		return futuro(() -> CiudadesWF.getPorId(contexto, id));
	}

	public static Futuro<ProvinciasWF> provincias(Contexto contexto, String codigoPostal) {
		return futuro(() -> ProvinciasWF.get(contexto, codigoPostal));
	}

	public static Futuro<SucursalWF> sucursal(Contexto contexto, String codigoPostal) {
		return futuro(() -> SucursalesWF.get(contexto, codigoPostal));
	}

	public static Futuro<TarjetaCreditoDistribucion> tcDistribucion(Contexto contexto, Integer marca, Integer producto, String distribucionDesc) {
		return futuro(() -> TarjetasCreditoDistribucion.get(contexto, marca, producto, distribucionDesc));
	}

	public static Futuro<TarjetaCreditoGrupo> tcGrupo(Contexto contexto, Integer marca, Integer producto, Integer grupo) {
		return futuro(() -> TarjetasCreditoGrupo.get(contexto, marca, producto, grupo));
	}

	public static Futuro<TarjetaCreditoGrupoAfinidad> tcGrupoAfinidad(Contexto contexto, Integer marca, Integer producto, String afinidadDesc) {
		return futuro(() -> TarjetasCreditoGrupoAfinidad.get(contexto, marca, producto, afinidadDesc));
	}

	public static Futuro<TarjetaCreditoModeloLiquidacion> tcModeloLiquidacion(Contexto contexto, Integer marca, Integer producto, Integer modLiq) {
		return futuro(() -> TarjetasCreditoModeloLiquidacion.get(contexto, marca, producto, modLiq));
	}

	public static Futuro<TarjetaCreditoProducto> tcProducto(Contexto contexto, Integer marca, Integer producto) {
		return futuro(() -> TarjetasCreditoProducto.get(contexto, marca, producto));
	}

}
