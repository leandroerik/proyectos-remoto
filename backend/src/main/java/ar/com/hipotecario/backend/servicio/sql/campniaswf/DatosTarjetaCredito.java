package ar.com.hipotecario.backend.servicio.sql.campniaswf;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.servicio.sql.SqlCampaniasWF;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoDistribucion.TarjetaCreditoDistribucion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupo.TarjetaCreditoGrupo;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoGrupoAfinidad.TarjetaCreditoGrupoAfinidad;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoModeloLiquidacion.TarjetaCreditoModeloLiquidacion;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.TarjetasCreditoProducto.TarjetaCreditoProducto;

public class DatosTarjetaCredito {
	public TarjetaCreditoProducto tcProducto;
	public TarjetaCreditoDistribucion tcDistribucion;
	public TarjetaCreditoGrupo tcGrupo;
	public TarjetaCreditoGrupoAfinidad tcGrupoAfinidad;
	public TarjetaCreditoModeloLiquidacion tcModLiq;

	public static DatosTarjetaCredito crear(Contexto contexto, Integer marca, String distribucionDesc, Integer producto, Integer grupo, String afinidadDesc, Integer modLiq) {
		DatosTarjetaCredito datoTC = new DatosTarjetaCredito();

		datoTC.tcProducto = SqlCampaniasWF.tcProducto(contexto, marca, producto).tryGet();
		datoTC.tcDistribucion = SqlCampaniasWF.tcDistribucion(contexto, marca, producto, distribucionDesc).tryGet();
		datoTC.tcGrupo = SqlCampaniasWF.tcGrupo(contexto, marca, producto, grupo).tryGet();
		datoTC.tcGrupoAfinidad = SqlCampaniasWF.tcGrupoAfinidad(contexto, marca, producto, afinidadDesc).tryGet();
		datoTC.tcModLiq = SqlCampaniasWF.tcModeloLiquidacion(contexto, marca, producto, modLiq).tryGet();

		return datoTC;
	}

	public Integer producto() {
		if (tcProducto == null)
			return 0;
		return tcProducto.TCPR_Id;
	}

	public Integer carteraGrupo() {
		if (tcGrupo == null)
			return 0;
		return tcGrupo.TCGR_Id;
	}

	public Integer afinidad() {
		if (tcGrupoAfinidad == null)
			return 0;
		return tcGrupoAfinidad.TCGA_Id;
	}

	public Integer modeloLiquidacion() {
		if (tcModLiq == null)
			return 0;
		return tcModLiq.TCML_Id;
	}

	public Integer distribucion() {
		if (tcDistribucion == null)
			return 0;
		return tcDistribucion.TCDI_Id;
	}
}
