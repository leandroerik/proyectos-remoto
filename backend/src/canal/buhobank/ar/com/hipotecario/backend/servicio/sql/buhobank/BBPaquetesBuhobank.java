package ar.com.hipotecario.backend.servicio.sql.buhobank;

import java.util.ArrayList;
import java.util.List;

import com.github.jknack.handlebars.Handlebars.Utils;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.conector.sql.Sql;
import ar.com.hipotecario.backend.conector.sql.SqlObjeto;
import ar.com.hipotecario.backend.conector.sql.SqlObjetos;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.canal.buhobank.ContextoBB;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

@SuppressWarnings("serial")
public class BBPaquetesBuhobank extends SqlObjetos<BBPaqueteBuhobank> {

	public static String CGU = "CGU";

	/* ========== ATRIBUTOS ========== */
	public static class BBPaqueteBuhobank extends SqlObjeto {
		public Integer id;
		public Integer id_plantilla_flujo;
		public Integer numero_paquete;
		public String letra_tc;
		public String nombre;
		public Integer paq_base;
		public Boolean es_standalone;
		public Boolean es_emprendedor;
		public Boolean td_virtual;
		public Boolean tc_virtual;
		public Boolean envio_sucursal;
		public Boolean cuenta_inversor;
		public Boolean td_visualiza_virtual;
		public Integer afinidad;
		public Integer modelo_liquidacion;
		public Integer codigo_distribucion;
		public String ciclo;
		public String caracteristica;

		public String getCaracteristica() {
			return !"N".equals(caracteristica) ? caracteristica : GeneralBB.SUB_BUHO_PUNTOS;
		}

		public Boolean checkInversor() {
			return Utils.isEmpty(cuenta_inversor) ? true : false;
		}

		public Boolean checkTdVisualizaVirtual(ContextoBB contexto) {
			return contexto.sesion().esTdVirtual() && !Utils.isEmpty(td_visualiza_virtual) && td_visualiza_virtual;
		}
		
		public Boolean checkTdVisualizaVirtualCgu() {
			return !Utils.isEmpty(td_visualiza_virtual) && td_visualiza_virtual;
		}

		public Boolean checkCaracteristica() {
			return Utils.isEmpty(caracteristica);
		}

		public Boolean cuentaInversor() {
			return !Utils.isEmpty(cuenta_inversor) && cuenta_inversor;
		}
	}

	public static BBPaquetesBuhobank buscarNumero(BBPaquetesBuhobank paquetes, Integer numero) {

		if (paquetes == null || Util.empty(numero)) {
			return new BBPaquetesBuhobank();
		}

		BBPaquetesBuhobank paquetesFiltrados = new BBPaquetesBuhobank();

		for (BBPaqueteBuhobank paquete : paquetes) {

			if (numero.equals(paquete.numero_paquete)) {
				paquetesFiltrados.add(paquete);
			}
		}

		return paquetesFiltrados;
	}

	public static BBPaqueteBuhobank buscarPaquete(BBPaquetesBuhobank paquetes, String letraTc, Integer numeroPaquete) {

		BBPaquetesBuhobank paquetesNumero = buscarNumero(paquetes, numeroPaquete);
		if (paquetesNumero == null || paquetesNumero.size() == 0) {
			return null;
		}

		if (paquetesNumero.size() == 1) {
			return paquetesNumero.first();
		}

		for (BBPaqueteBuhobank paquete : paquetesNumero) {

			if (!Util.empty(letraTc)) {

				if (letraTc.equals(paquete.letra_tc)) {
					return paquete;
				}
			} else if (Util.empty(paquete.letra_tc)) {
				return paquete;
			}
		}

		return null;
	}

	public static BBPaqueteBuhobank buscarStandalone(BBPaquetesBuhobank paquetes) {

		if (paquetes == null) {
			return null;
		}

		for (BBPaqueteBuhobank paquete : paquetes) {

			if (paquete.es_standalone && !paquete.nombre.contains(CGU)) {
				return paquete;
			}
		}

		return null;
	}

	public static BBPaquetesBuhobank buscarPaquetes(BBPaquetesBuhobank paquetesBuhobank, Boolean filtrarEmprendedor) {

		if (paquetesBuhobank == null) {
			return null;
		}

		BBPaquetesBuhobank paquetes = new BBPaquetesBuhobank();

		for (BBPaqueteBuhobank paquete : paquetesBuhobank) {

			if (!paquete.es_standalone) {

				if (filtrarEmprendedor) {

					if (!paquete.es_emprendedor) {
						paquetes.add(paquete);
					}
				} else {
					paquetes.add(paquete);
				}
			}
		}

		return paquetes;
	}

	public static BBPaquetesBuhobank buscarPaquetesEmprendedor(BBPaquetesBuhobank paquetesBuhobank) {

		if (paquetesBuhobank == null) {
			return null;
		}

		BBPaquetesBuhobank paquetes = new BBPaquetesBuhobank();

		for (BBPaqueteBuhobank paquete : paquetesBuhobank) {

			if (!paquete.es_standalone && paquete.es_emprendedor) {
				paquetes.add(paquete);
			}
		}

		return paquetes;
	}

	public static BBPaquetesBuhobank buscarIdPlantilla(BBPaquetesBuhobank paquetes, Integer idPlantilla) {

		if (paquetes == null || Utils.isEmpty(idPlantilla)) {
			return null;
		}

		BBPaquetesBuhobank paquetesById = new BBPaquetesBuhobank();

		for (BBPaqueteBuhobank paquete : paquetes) {

			if (idPlantilla.equals(paquete.id_plantilla_flujo)) {
				paquetesById.add(paquete);
			}
		}

		return paquetesById;
	}

	public static BBPaqueteBuhobank buscarId(BBPaquetesBuhobank paquetes, Integer id) {

		if (paquetes == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBPaqueteBuhobank paquete : paquetes) {

			if (id.equals(paquete.id)) {
				return paquete;
			}
		}

		return null;
	}

	public static List<String> obtenerNumeroPaquetes(BBPaquetesBuhobank paquetes) {

		if (paquetes == null) {
			return null;
		}

		List<String> numerosPaquetes = new ArrayList<String>();

		for (BBPaqueteBuhobank paquete : paquetes) {
			numerosPaquetes.add(paquete.numero_paquete.toString());
		}

		return numerosPaquetes;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBPaqueteBuhobank paquete, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(paquete.id_plantilla_flujo) ? paquete.id_plantilla_flujo : null;
		parametros[1] = !Util.empty(paquete.numero_paquete) ? paquete.numero_paquete : null;
		parametros[2] = !Util.empty(paquete.letra_tc) ? paquete.letra_tc : null;
		parametros[3] = !Util.empty(paquete.nombre) ? paquete.nombre : null;
		parametros[4] = !Util.empty(paquete.paq_base) ? paquete.paq_base : null;
		parametros[5] = !Util.empty(paquete.es_standalone) ? paquete.es_standalone : null;
		parametros[6] = !Util.empty(paquete.es_emprendedor) ? paquete.es_emprendedor : null;
		parametros[7] = !Util.empty(paquete.td_virtual) ? paquete.td_virtual : null;
		parametros[8] = !Util.empty(paquete.tc_virtual) ? paquete.tc_virtual : null;
		parametros[9] = !Util.empty(paquete.envio_sucursal) ? paquete.envio_sucursal : null;
		parametros[10] = !Util.empty(paquete.afinidad) ? paquete.afinidad : null;
		parametros[11] = !Util.empty(paquete.modelo_liquidacion) ? paquete.modelo_liquidacion : null;
		parametros[12] = !Util.empty(paquete.codigo_distribucion) ? paquete.codigo_distribucion : null;
		parametros[13] = !Util.empty(paquete.ciclo) ? paquete.ciclo : null;
		parametros[14] = !Util.empty(paquete.cuenta_inversor) ? paquete.cuenta_inversor : null;
		parametros[15] = !Util.empty(paquete.caracteristica) ? paquete.caracteristica : null;
		parametros[16] = !Util.empty(paquete.td_visualiza_virtual) ? paquete.td_visualiza_virtual : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBPaquetesBuhobank getByFlujo(Contexto contexto, String flujo) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_paquetes] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class);
	}

	public static BBPaqueteBuhobank getByPaqBase(Contexto contexto, Integer paqBase) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_paquetes] WITH (NOLOCK) ";
		sql += "WHERE paq_base = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, paqBase);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class).first();
	}

	public static BBPaqueteBuhobank getByNumero(Contexto contexto, Integer numeroPaquete) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_paquetes] WITH (NOLOCK) ";
		sql += "WHERE numero_paquete = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, numeroPaquete);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class).first();
	}
	
	public static BBPaqueteBuhobank getByNumero(Contexto contexto, Integer numeroPaquete, String flujo) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_paquetes] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";
		sql += "AND t2.numero_paquete = ? ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo, numeroPaquete);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class).first();
	}

	public static BBPaqueteBuhobank getByLetra(Contexto contexto, String flujo, String letraTc, Boolean esEmprendedor) {

		String sql = "";
		sql += "SELECT t2.* ";
		sql += "FROM [dbo].[bb_plantillas_flujo] AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN [dbo].[bb_paquetes] AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_plantilla_flujo ";
		sql += "WHERE t1.plantilla = ? ";
		sql += "AND t2.letra_tc = ? ";
		sql += "AND t2.es_emprendedor = ? ";
		sql += "AND t2.paq_base IS NULL ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, flujo, letraTc, esEmprendedor);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class).first();
	}

	public static BBPaquetesBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT * ";
		sql += "FROM [dbo].[bb_paquetes] WITH (NOLOCK) ";
		sql += "ORDER BY id_plantilla_flujo ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class);
	}

	public static void clonarTotal(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		BBPaquetesBuhobank paquetesBuhobank = get(contexto);
		BBPaquetesBuhobank paquetesBase = buscarIdPlantilla(paquetesBuhobank, idPlantillaBase);
		if (paquetesBase == null || paquetesBase.size() == 0) {
			return;
		}

		for (BBPaqueteBuhobank paqueteBase : paquetesBase) {

			BBPaqueteBuhobank nuevoPaquete = clonarPaquete(contexto, paqueteBase.id, idPlantilla);
			if (!Utils.isEmpty(nuevoPaquete)) {
				BBTarjetasBuhobank.clonarTotal(contexto, paqueteBase.id, nuevoPaquete.id);
				BBPaquetesSubproductoBuhobank.clonarTotal(contexto, paqueteBase.id, nuevoPaquete.id);
				BBContenidosDinamicoPaqueteBuhobank.clonarTotal(contexto, paqueteBase.id, nuevoPaquete.id);
			}
		}
	}

	public static Boolean clonar(Contexto contexto, Integer idPlantillaBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_paquetes] (id_plantilla_flujo, numero_paquete, letra_tc, nombre, paq_base, es_standalone, es_emprendedor, td_virtual, tc_virtual, envio_sucursal, afinidad, modelo_liquidacion, codigo_distribucion, ciclo, cuenta_inversor, caracteristica, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_plantilla_flujo, numero_paquete, letra_tc, nombre, paq_base, es_standalone, es_emprendedor, td_virtual, tc_virtual, envio_sucursal, afinidad, modelo_liquidacion, codigo_distribucion, ciclo, cuenta_inversor, caracteristica, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_paquetes] WITH (NOLOCK) ";
		sql += "WHERE id_plantilla_flujo = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPlantillaBase) > 0;
	}

	public static BBPaqueteBuhobank clonarPaquete(Contexto contexto, Integer idPaqueteBase, Integer idPlantilla) {

		String sql = "";
		sql += "INSERT [dbo].[bb_paquetes] (id_plantilla_flujo, numero_paquete, letra_tc, nombre, paq_base, es_standalone, es_emprendedor, td_virtual, tc_virtual, envio_sucursal, afinidad, modelo_liquidacion, codigo_distribucion, ciclo, cuenta_inversor, caracteristica, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_plantilla_flujo, numero_paquete, letra_tc, nombre, paq_base, es_standalone, es_emprendedor, td_virtual, tc_virtual, envio_sucursal, afinidad, modelo_liquidacion, codigo_distribucion, ciclo, cuenta_inversor, caracteristica, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_paquetes] WITH (NOLOCK) ";
		sql += "WHERE id = ? ;";
		sql += "SELECT SCOPE_IDENTITY() AS id";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql, idPlantilla, idPaqueteBase);
		return map(datos, BBPaquetesBuhobank.class, BBPaqueteBuhobank.class).first();
	}

	public static Boolean post(Contexto contexto, BBPaqueteBuhobank nuevoPaquete) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_paquetes] ";
		sql += "(id_plantilla_flujo, numero_paquete, letra_tc, nombre, paq_base, es_standalone, es_emprendedor, td_virtual, tc_virtual, envio_sucursal, afinidad, modelo_liquidacion, codigo_distribucion, ciclo, cuenta_inversor, caracteristica, ";
		sql += "fecha_ultima_modificacion) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevoPaquete, 16);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBPaqueteBuhobank paquete) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_paquetes] SET ";
		sql += "id_plantilla_flujo = ? ,";
		sql += "numero_paquete = ? ,";
		sql += "letra_tc = ? ,";
		sql += "nombre = ? ,";
		sql += "paq_base = ? ,";
		sql += "es_standalone = ? ,";
		sql += "es_emprendedor = ? ,";
		sql += "td_virtual = ? ,";
		sql += "tc_virtual = ? ,";
		sql += "envio_sucursal = ? ,";
		sql += "afinidad = ? ,";
		sql += "modelo_liquidacion = ? ,";
		sql += "codigo_distribucion = ? ,";
		sql += "ciclo = ? ,";
		sql += "cuenta_inversor = ? ,";
		sql += "caracteristica = ? ,";
		sql += "td_visualiza_virtual = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, paquete, 18);
		parametros[17] = paquete.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_paquetes] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

}
