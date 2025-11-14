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
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBTarjetasFinalizarBatchBuhobank.BBTarjetaFinalizarBatchBuhobank;
import ar.com.hipotecario.canal.buhobank.GeneralBB;

@SuppressWarnings("serial")
public class BBTarjetasFinalizarBatchBuhobank extends SqlObjetos<BBTarjetaFinalizarBatchBuhobank> {

	// tipos
	public static String TIPO_TITULAR = "titular";
	public static String TIPO_ITEM = "item";

	/* ========== ATRIBUTOS ========== */
	public static class BBTarjetaFinalizarBatchBuhobank extends SqlObjeto {

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

	public static BBTarjetasFinalizarBatchBuhobank buscarIdTarjeta(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizar, Integer id) {

		if (tarjetasFinalizar == null || Utils.isEmpty(id)) {
			return null;
		}

		BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarId = new BBTarjetasFinalizarBatchBuhobank();

		for (BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar : tarjetasFinalizar) {

			if (id.equals(tarjetaFinalizar.id_tarjeta)) {
				tarjetasFinalizarId.add(tarjetaFinalizar);
			}
		}

		return tarjetasFinalizarId;
	}

	public static BBTarjetaFinalizarBatchBuhobank buscarId(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizar, Integer id) {

		if (tarjetasFinalizar == null || Utils.isEmpty(id)) {
			return null;
		}

		for (BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar : tarjetasFinalizar) {

			if (id.equals(tarjetaFinalizar.id)) {
				return tarjetaFinalizar;
			}
		}

		return null;
	}

	private static BBTarjetasFinalizarBatchBuhobank buscarTipo(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarBuhobank, String tipo, String subtipo) {

		if (tarjetasFinalizarBuhobank == null || Utils.isEmpty(tipo)) {
			return null;
		}

		BBTarjetasFinalizarBatchBuhobank tarjetaFinalizarTipo = new BBTarjetasFinalizarBatchBuhobank();

		for (BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar : tarjetasFinalizarBuhobank) {

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

		if (contenidoClaveObj != null && contenidoClaveObj.size() > 0) {
			return crearItem(contenidoClaveObj.first().titulo, contenidoClaveObj.first().texto, contenidoClaveObj.first().imagen);
		}

		return null;
	}

	public static String obtenerTitular(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizar) {

		BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarTipo = buscarTipo(tarjetasFinalizar, TIPO_TITULAR, null);
		if (tarjetasFinalizarTipo != null && tarjetasFinalizarTipo.size() > 0) {
			return tarjetasFinalizarTipo.first().titulo;
		}

		return null;
	}

	public static List<Objeto> obtenerItems(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizar) {

		BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarItems = buscarTipo(tarjetasFinalizar, TIPO_ITEM, null);
		if (tarjetasFinalizarItems == null) {
			return null;
		}

		return crearItems(tarjetasFinalizarItems);
	}

	private static List<Objeto> crearItems(BBTarjetasFinalizarBatchBuhobank tarjetasFinalizarItems) {

		List<Objeto> items = new ArrayList<Objeto>();

		for (BBTarjetaFinalizarBatchBuhobank tarjetasFinalizar : tarjetasFinalizarItems) {

			Objeto item = crearItem(tarjetasFinalizar.titulo, tarjetasFinalizar.texto, tarjetasFinalizar.logo);
			if (item != null) {
				items.add(item);
			}
		}

		return items;
	}

	public static Objeto obtenerContenidoDefault(String flujo, Boolean esVirtual, Boolean esBatch, Boolean tieneClaveActiva, Boolean esStandalone, Boolean tieneTdFisica, Boolean aceptarInversion, Boolean estaEnHorarioInversor) {

		List<Objeto> items = new ArrayList<Objeto>();
		
		String titularPrincipal = "";
		String titular = "¡Te damos la bienvenida al Banco del Hogar!";
		
		if(esBatch) {
			titularPrincipal = "¡Felicitaciones!";
			titular = "¡Bienvenido/a al Banco del Hogar!";
			
			if(esVirtual) {
				String tituloVirtual = "Tu tarjeta virtual está en proceso";
				String descripcionVirtual = "En 24/48 hs. estará disponible para que puedas comenzar a utilizarla.";
				String logoVirtual = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png";
				items.add(crearItem(tituloVirtual, descripcionVirtual, logoVirtual));
			}

			if(!esVirtual || tieneTdFisica || !esStandalone) {
				String tituloTarjeta = "Estamos preparando el envío de tus tarjetas";
				String descripcionTarjeta = "Cuando esté disponible, la recibirás en el domicilio que elegiste.";
				String logoTarjeta = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png";
				items.add(crearItem(tituloTarjeta, descripcionTarjeta, logoTarjeta));
			}
			
		}
		else {
			
			String tituloCaja = "Ya contás con una caja de ahorro";
			String descripcionCaja = esStandalone ? "Podés operar en pesos todos los días las 24 hs." : "Podés operar en pesos y dólares las 24 hs.";
			String logoCaja = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/icono_caja.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-ahorro.png";
			items.add(crearItem(tituloCaja, descripcionCaja, logoCaja));
			
			if(esVirtual) {
				String tituloVirtual = "Tu Tarjeta de Débito Virtual está lista";
				String descripcionVirtual = "Recordá tener dinero en tu cuenta para empezar a usarla.";
				String logoVirtual = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/credit-card-20-regular.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png";
				items.add(crearItem(tituloVirtual, descripcionVirtual, logoVirtual));
			}

			if(!esVirtual || tieneTdFisica || !esStandalone) {
				String tituloTarjeta = "Estamos preparando el envío de tus tarjetas";
				String descripcionTarjeta = "Llegará en un máximo de 20 días hábiles al domicilio que elegiste.";
				String logoTarjeta = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/delivery-20-regular.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-tarjeta.png";
				items.add(crearItem(tituloTarjeta, descripcionTarjeta, logoTarjeta));
			}
			
			if(aceptarInversion) {
				String tituloInversor = estaEnHorarioInversor ? "Tu Cuenta Inversor está en proceso" : BBTarjetasFinalizarBuhobank.tituloFueraDeHorarioInversor;
				String descripcionInversor = estaEnHorarioInversor ? "Te avisaremos cuando esté disponible para que puedas empezar a invertir." : BBTarjetasFinalizarBuhobank.textoFueraDeHorarioInversor;
				String logoInversor = GeneralBB.FLUJO_ONBOARDING.equals(flujo) ? "https://www.hipotecario.com.ar/media/buhobank/time-20-regular.png" : "https://www.hipotecario.com.ar/media/buhobank/icono-inversiones.png";
				items.add(crearItem(tituloInversor, descripcionInversor, logoInversor));
			}
			
		}
		
		String tituloTextBox = "Encontramos que ya tenés una cuenta en el banco. Inicio sesión para poder operar.";
		String tituloTextBox2 = "Si no recordás tu usuario o clave Búho Fácil, podés recuperarla desde el inicio de sesión.";

		if(!tieneClaveActiva) {
			tituloTextBox = "A continuacin vas a poder crear tu usuario y clave Búho Fácil para ingresar a los canales digitales del banco.";
			tituloTextBox2 = "";
		}
		
		if(esBatch) {
			tituloTextBox = "En breve te vamos a notificar para que puedas crear tu usuario y contraseña del banco.";
			tituloTextBox2 = "";
		}
		
		Objeto contenido = new Objeto();
		contenido.set("esBatch", esBatch);
		contenido.set("logo_principal", getLogoPrincipalDefault(flujo));
		contenido.set("titular_principal", titularPrincipal);
		contenido.set("titular", titular);
		contenido.set("titular_html", BBTarjetasFinalizarBuhobank.htmlNegrita(titular));
		contenido.set("items", items);
		contenido.set("textBox", crearItem(tituloTextBox, tituloTextBox2, null));
		
		String textBoton = tieneClaveActiva ? "INICIAR SESION" : "CREAR USUARIO Y CONTRASEA";
		String urlBoton = "";
		
		if(esBatch) {
			textBoton = "VER PROMOCIONES";
			urlBoton = "https://buhobank.com";
		}
		
		contenido.set("textoBoton", textBoton);
		contenido.set("urlBoton", urlBoton);
		contenido.set("esDefault", true);

		return contenido;
	}
	
	private static String getLogoPrincipalDefault(String flujo) {
		
		if(GeneralBB.FLUJO_LIBERTAD.equals(flujo)) {
			return "https://www.hipotecario.com.ar/media/buhobank/ico-mano-ok.png";
		}
		
		if(GeneralBB.FLUJO_INVERSIONES.equals(flujo)) {
			return "https://www.hipotecario.com.ar/media/buhobank/ico-mano-ok.png";
		}
		
		return "https://www.hipotecario.com.ar/media/buhobank/success_image.png";
	}

	private static Object[] obtenerParametros(Contexto contexto, BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar, int cantidad) {

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
	public static BBTarjetasFinalizarBatchBuhobank get(Contexto contexto) {

		String sql = "";
		sql += "SELECT t1.numero_paquete, t2.descripcion, t2.virtual, t3.* ";
		sql += "FROM bb_paquetes AS t1 WITH (NOLOCK) ";
		sql += "INNER JOIN bb_tarjetas AS t2 WITH (NOLOCK) ";
		sql += "ON t1.id = t2.id_paquete ";
		sql += "INNER JOIN bb_tarjetas_finalizar_batch AS t3 WITH (NOLOCK) ";
		sql += "ON t2.id = t3.id_tarjeta ";
		sql += "ORDER BY t3.id_tarjeta ASC ";

		Objeto datos = Sql.select(contexto, SqlBuhoBank.SQL, sql);
		return map(datos, BBTarjetasFinalizarBatchBuhobank.class, BBTarjetaFinalizarBatchBuhobank.class);
	}

	public static Boolean post(Contexto contexto, BBTarjetaFinalizarBatchBuhobank nuevaTarjetaFinalizar) {

		String sql = "";
		sql += "INSERT INTO [dbo].[bb_tarjetas_finalizar_batch] ";
		sql += "([id_tarjeta], [tipo], [subtipo], [titulo], [texto], [logo], ";
		sql += "[fecha_ultima_modificacion]) ";
		sql += "VALUES (?, ?, ?, ?, ?, ?, GETDATE())";

		Object[] parametros = obtenerParametros(contexto, nuevaTarjetaFinalizar, 6);

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, parametros) > 0;
	}

	public static Boolean put(Contexto contexto, BBTarjetaFinalizarBatchBuhobank tarjetaFinalizar) {

		String sql = "";
		sql += "UPDATE [dbo].[bb_tarjetas_finalizar_batch] SET ";
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
		sql += "DELETE [dbo].[bb_tarjetas_finalizar_batch] ";
		sql += "WHERE id = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, id) > 0;
	}

	public static Boolean clonar(Contexto contexto, Integer idTarjetaBase, Integer idTarjeta) {

		String sql = "";
		sql += "INSERT [dbo].[bb_tarjetas_finalizar_batch] (id_tarjeta, tipo, subtipo, titulo, texto, logo, fecha_ultima_modificacion) ";
		sql += "SELECT ? AS id_tarjeta, tipo, subtipo, titulo, texto, logo, GETDATE() AS fecha_ultima_modificacion ";
		sql += "FROM [dbo].[bb_tarjetas_finalizar_batch] WITH (NOLOCK) ";
		sql += "WHERE id_tarjeta = ? ";

		return Sql.update(contexto, SqlBuhoBank.SQL, sql, idTarjeta, idTarjetaBase) > 0;
	}

}
