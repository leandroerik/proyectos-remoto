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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBuhobank.BBTarjetaFinalizarBuhobank;

@SuppressWarnings("serial")
public class BBTarjetasFinalizarBuhobank extends SqlObjetos<BBTarjetaFinalizarBuhobank> {

	// tipos
	public static String TIPO_TITULAR = "titular";
	public static String TIPO_ITEM = "item";

	// subtipo
	public static String SUBTIPO_INVERSOR = "inversor";
	public static String SUBTIPO_TARJETAFISICA = "tarjetaFisica";
	
	public static String tituloFueraDeHorarioInversor = "Tu Cuenta Inversor está en proceso";
	public static String textoFueraDeHorarioInversor = "Finalizá el alta en la sección Inversiones dentro de Home Banking o la App BH cualquier día hábil de 8 a 19hs.";
	
	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaFinalizarBuhobank extends SqlObjeto {
		public Integer numero_paquete;
		public String descripcion;
		public Boolean virtual;

		public Integer id;
		public Integer id_tarjeta;
		public String tipo;
		public String subtipo;
		public String titulo;
		public String texto;
		public String logo;
	}

	public static BBTarjetasFinalizarBuhobank buscarIdTarjeta(BBTarjetasFinalizarBuhobank tarjetasFinalizar, Integer id) {

		if (tarjetasFinalizar == null || Utils.isEmpty(id)) {
			return null;
		}

		BBTarjetasFinalizarBuhobank tarjetasFinalizarId = new BBTarjetasFinalizarBuhobank();

		for (BBTarjetaFinalizarBuhobank tarjetaFinalizar : tarjetasFinalizar) {

			if (id.equals(tarjetaFinalizar.id_tarjeta)) {
				tarjetasFinalizarId.add(tarjetaFinalizar);
			}
		}

		return tarjetasFinalizarId;
	}

	public static BBTarjetaFinalizarBuhobank buscarId(BBTarjetasFinalizarBuhobank tarjetasFinalizar, Integer id) {

		if (tarjetasFinalizar == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaFinalizarBuhobank tarjetaFinalizar : tarjetasFinalizar) {

			if (id.equals(tarjetaFinalizar.id)) {
				return tarjetaFinalizar;
			}
		}

		return null;
	}

	private static BBTarjetasFinalizarBuhobank buscarTipo(BBTarjetasFinalizarBuhobank tarjetasFinalizarBuhobank, String tipo, String subtipo) {

		if (tarjetasFinalizarBuhobank == null || Utils.isEmpty(tipo)) {
			return null;
		}

		BBTarjetasFinalizarBuhobank tarjetaFinalizarTipo = new BBTarjetasFinalizarBuhobank();

		for (BBTarjetaFinalizarBuhobank tarjetaFinalizar : tarjetasFinalizarBuhobank) {

			if (tipo.equals(tarjetaFinalizar.tipo)) {

				if (!Utils.isEmpty(subtipo)) {

					if (subtipo.equals(tarjetaFinalizar.subtipo)) {
						tarjetaFinalizarTipo.add(tarjetaFinalizar);
					}
				} else {
					tarjetaFinalizarTipo.add(tarjetaFinalizar);
				}
			}
		}

		return tarjetaFinalizarTipo;

	}
	
	public static List<Objeto> obtenerItems(BBTarjetasFinalizarBuhobank tarjetaFinalizarBuhobank, Boolean cuentaInversor, Boolean tieneTDVirtual, Boolean tieneTDFisica, Integer numeroPaquete, Boolean estaEnHorarioInversor) {

		BBTarjetasFinalizarBuhobank tarjetasFinalizarItems = buscarTipo(tarjetaFinalizarBuhobank, TIPO_ITEM, null);
		if (tarjetasFinalizarItems == null) {
			return null;
		}
		
		if(tieneTDVirtual && !tieneTDFisica) {
			tarjetasFinalizarItems = filtrarPorTipo(tarjetasFinalizarItems, SUBTIPO_TARJETAFISICA);
		}

		if (!cuentaInversor) {
			tarjetasFinalizarItems = filtrarPorTipo(tarjetasFinalizarItems, SUBTIPO_INVERSOR);
		} else {
			tarjetasFinalizarItems = filtrarNoInversor(tarjetasFinalizarItems);
		}
		
		if(!estaEnHorarioInversor) {
			for(BBTarjetaFinalizarBuhobank item: tarjetasFinalizarItems) {
				if(item.subtipo.equals(SUBTIPO_INVERSOR)) {
					item.titulo = tituloFueraDeHorarioInversor;
					item.texto = textoFueraDeHorarioInversor;
				}
			}
		}
		
		return crearItems(tarjetasFinalizarItems);
	}

	private static BBTarjetasFinalizarBuhobank filtrarPorTipo(BBTarjetasFinalizarBuhobank tarjetasFinalizarItems, String tipo) {

		BBTarjetasFinalizarBuhobank tarjetasFinalizarFiltradas = new BBTarjetasFinalizarBuhobank();

		for (BBTarjetaFinalizarBuhobank tarjetaFinalizar : tarjetasFinalizarItems) {
			if (!tarjetaFinalizar.subtipo.contains(tipo)) {
				tarjetasFinalizarFiltradas.add(tarjetaFinalizar);
			}
		}
		return tarjetasFinalizarFiltradas;
	}
	
	private static BBTarjetasFinalizarBuhobank filtrarNoInversor(BBTarjetasFinalizarBuhobank tarjetasFinalizarItems) {

		BBTarjetasFinalizarBuhobank tarjetasFinalizarFiltradas = new BBTarjetasFinalizarBuhobank();

		for (BBTarjetaFinalizarBuhobank tarjetaFinalizar : tarjetasFinalizarItems) {
			if (!tarjetaFinalizar.subtipo.contains(SUBTIPO_INVERSOR)) {
				String subtipo = tarjetaFinalizar.subtipo;
				if (!Utils.isEmpty(subtipo)) {
					BBTarjetasFinalizarBuhobank items = buscarTipo(tarjetasFinalizarItems, TIPO_ITEM, subtipo + "_" + SUBTIPO_INVERSOR);
					if (items == null || items.size() == 0) {
						tarjetasFinalizarFiltradas.add(tarjetaFinalizar);
					}
				}
			} else {
				tarjetasFinalizarFiltradas.add(tarjetaFinalizar);
			}
		}
		return tarjetasFinalizarFiltradas;
	}

	private static List<Objeto> crearItems(BBTarjetasFinalizarBuhobank tarjetasFinalizarItems) {

		List<Objeto> items = new ArrayList<Objeto>();

		for (BBTarjetaFinalizarBuhobank tarjetasFinalizar : tarjetasFinalizarItems) {
			
			Objeto item = crearItem(tarjetasFinalizar.titulo, tarjetasFinalizar.texto, tarjetasFinalizar.logo);
			if (item != null) {
				items.add(item);
			}
		}

		return items;
	}

	public static Objeto crearItem(String titulo, String texto, String logo) {

		Objeto item = new Objeto();

		item.set("titulo", titulo);
		item.set("titulo_html", htmlNegrita(titulo));
		item.set("descripcion", texto);
		item.set("logo", logo);

		return item;
	}

	public static String htmlNegrita(String texto) {
		return "<b>" + texto + "</b>";
	}

	public static Objeto obtenerTextBox(BBContenidosDinamicoBuhobank contenidoClaveObj) {

		if (contenidoClaveObj == null || contenidoClaveObj.size() == 0) {
			return null;
		}

		return crearItem(contenidoClaveObj.first().titulo, contenidoClaveObj.first().texto, contenidoClaveObj.first().imagen);
	}

	public static String obtenerTextoBoton(BBContenidosDinamicoBuhobank contenidoClaveObj) {

		if (contenidoClaveObj == null || contenidoClaveObj.size() == 0) {
			return null;
		}

		return contenidoClaveObj.first().texto_legales;
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaFinalizarBuhobank tarjetaFinalizar, int cantidad) {

		Object[] parametros = new Object[cantidad];

		parametros[0] = !Util.empty(tarjetaFinalizar.id_tarjeta) ? tarjetaFinalizar.id_tarjeta : null;
		parametros[1] = !Util.empty(tarjetaFinalizar.tipo) ? tarjetaFinalizar.tipo : null;
		parametros[2] = !Util.empty(tarjetaFinalizar.subtipo) ? tarjetaFinalizar.subtipo : null;
		parametros[3] = !Util.empty(tarjetaFinalizar.titulo) ? tarjetaFinalizar.titulo : null;
		parametros[4] = !Util.empty(tarjetaFinalizar.texto) ? tarjetaFinalizar.texto : null;
		parametros[5] = !Util.empty(tarjetaFinalizar.logo) ? tarjetaFinalizar.logo : null;

		return parametros;
	}

	/* ========== SERVICIO ========== */
	public static BBTarjetasFinalizarBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.descripcion, t2.virtual, t3.* ";
		sql += "FROM bb_paquetes AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN bb_tarjetas AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "INNER JOIN bb_tarjetas_finalizar AS t3 WITH (NOLOCK) ";
		sql += "ON t2.id = t3.id_tarjeta ";
		sql += "ORDER BY t3.id_tarjeta ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBTarjetasFinalizarBuhobank.class, BBTarjetaFinalizarBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBTarjetaFinalizarBuhobank nuevaTarjetaFinalizar) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas_finalizar] ";
		sql += "([id_tarjeta], [tipo], [subtipo], [titulo], [texto], [logo], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjetaFinalizar, 6);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaFinalizarBuhobank tarjetaFinalizar) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas_finalizar] SET ";
		sql += "id_tarjeta = ? ,";
		sql += "tipo = ? ,";
		sql += "subtipo = ? ,";
		sql += "titulo = ? ,";
		sql += "texto = ? ,";
		sql += "logo = ? ,";
		sql += "fecha_ultima_modificacion = GETDATE() ";
		sql += "WHERE id = ? ";

		Object[] parametros = obtenerParametros(contexto, tarjetaFinalizar, 7);
		parametros[6] = tarjetaFinalizar.id;

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean delete(Contexto contexto, Integer id) {

		String sql = "";
		sql += "DELETE [dbo].[bb_tarjetas_finalizar] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idTarjetaBase, Integer idTarjeta) {

		String sql = "";
		sql += "INSERT [dbo].[bb_tarjetas_finalizar] (id_tarjeta, tipo, subtipo, titulo, texto, logo, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_tarjeta, tipo, subtipo, titulo, texto, logo, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_tarjetas_finalizar] WITH (NOLOCK) ";
		sql += "WHERE id_tarjeta = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idTarjeta, idTarjetaBase) > 0;
	}

}
