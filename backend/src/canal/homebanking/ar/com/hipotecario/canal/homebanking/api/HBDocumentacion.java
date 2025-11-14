package ar.com.hipotecario.canal.homebanking.api;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import ar.com.hipotecario.canal.buhobank.BBAplicacion;
import ar.com.hipotecario.canal.homebanking.negocio.Sucursal;
import org.apache.commons.io.IOUtils;
import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONObject;
import com.itextpdf.text.DocumentException;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.Respuesta;
import ar.com.hipotecario.canal.homebanking.base.ConfigHB;
import ar.com.hipotecario.canal.homebanking.base.DataFile;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.excepcion.UnauthorizedException;
import ar.com.hipotecario.canal.homebanking.lib.Bitwise;
import ar.com.hipotecario.canal.homebanking.lib.Fecha;
import ar.com.hipotecario.canal.homebanking.negocio.EstadoDocumentacion;
import ar.com.hipotecario.canal.homebanking.negocio.TipoNotificacion;
import ar.com.hipotecario.canal.homebanking.servicio.DocumentacionService;
import ar.com.hipotecario.canal.homebanking.servicio.RestArchivo;
import ar.com.hipotecario.canal.homebanking.servicio.RestNotificaciones;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;

public class HBDocumentacion {

	/*
	 * ENUMS Y CONSTANTES
	 */

	/**
	 * Enum que representa el estado de documentación pendiente. El valor es un int,
	 * y los chequeos se hacen mediante operaciones bitwise. Nuevas entradas deben
	 * seguir el formato (1 << 0), (1 << 1), (1 << 2) ... (1 << n)
	 * 
	 * @author C06470
	 *
	 */
	private static enum Documentacion {
		NADA(0), DNI(1 << 0), INGRESOS(1 << 1), PREVENCION_LAVADO(1 << 2), RESPALDATORIA(1 << 3), CONSTANCIA_DOMICILIO(1 << 4);

		public final int value;

		private Documentacion(int value) {
			this.value = value;
		}
	}

	/**
	 * enum que mapea el índice de la columna de documentacion (en una hoja de
	 * campaña de actualización), con un tipo de Documentacion
	 * 
	 * @author C06470
	 *
	 */
	private static enum CeldaDocumentacion {
		CD_PREVENCION(5, Documentacion.PREVENCION_LAVADO), CD_INGRESO(6, Documentacion.INGRESOS), CD_DNI(7, Documentacion.DNI), CD_RESPALDATORIA(8, Documentacion.RESPALDATORIA), CD_DOMICILIO(9, Documentacion.CONSTANCIA_DOMICILIO);

		public final int index;
		public final Documentacion tipoDocumentacion;

		private CeldaDocumentacion(int index, Documentacion tipoDocumentacion) {
			this.index = index;
			this.tipoDocumentacion = tipoDocumentacion;
		}
	}

	private static enum SituacionLaboral {
		AUTONOMO, DESEMPLEADO, JUBILADO, OTRO, DEPENDENCIA_CONTRATADO, DEPENDENCIA_FIJO, SUB_EMPLEADO, SUBSIDIADO
	}

	/*
	 * index fijo de otros valores que necesitamos sacar de las planillas de
	 * actualización
	 */
	public static final int INDEX_NOMBRE = 0;
	public static final int INDEX_COBIS = 1;
	public static final int INDEX_CUIL = 2;

	public static Objeto TIPOS_SO;
	public static Objeto CARGOS_PEP;

	public static Objeto faltantes;

	public static Objeto completas;

	static {
		TIPOS_SO = parsearParCSV("tipos-SO.csv", ";");
		CARGOS_PEP = parsearParCSV("cargos-PEP.csv", ";");
	}

	/*
	 * RECURSOS
	 */

	public static Respuesta requerirEstados(ContextoHB contexto, TipoNotificacion tipoNotificacion) {
		UnauthorizedException.ifNot(contexto.request.headers("Authorization"));

		// Se agrega condicion para mandar Template Segun tipo de Cuenta

		String Cuenta = "";

		if (String.valueOf(tipoNotificacion).equals("CUENTA_DOLARES")) {
			Cuenta = "doppler_documentacion_cuenta_dolares";
		}

		else if (String.valueOf(tipoNotificacion).equals("SOS")) {
			Cuenta = "doppler_SOS";
		} else if (String.valueOf(tipoNotificacion).equals("Inversion")) {
			Cuenta = "doppler_Inversion";
		}

		else {
			Cuenta = "doppler_documentacion";
		}

		DataFile reporte = contexto.archivos.get("reporte");
		byte[] reporteEstados = reporte.bytes;

		if (reporteEstados == null || reporteEstados.length == 0) {
			return Respuesta.parametrosIncorrectos();
		}

		ArrayList<EstadoDocumentacion> estados = new ArrayList<>();
		try {
			estados = excelToEstados(reporteEstados, tipoNotificacion);
		} catch (Exception e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
			return Respuesta.error();
		}

		for (EstadoDocumentacion estado : estados) {
			// solo actualizar clientes que lo requieran
			if (estado.getEstado() != 0) {
				try {
					actualizarEstado(estado.getCuil(), estado.getEstado(), estado.getNombreCompleto(), tipoNotificacion);

					if (true) {

						Objeto parametros = new Objeto();
						Date hoy = new Date();
						parametros.set("Nombre_Apellido", estado.getNombreCompleto());
						parametros.set("Subject", "¡Recorda actualizar tu documentación para seguir operando de forma segura!");
						parametros.set("FECHA", new SimpleDateFormat("dd/MM/yyyy").format(hoy));
						parametros.set("HORA", new SimpleDateFormat("hh:mm").format(hoy));
						parametros.set("CANAL", "Home Banking");

						RestNotificaciones.envioMailSinSesion(contexto, ConfigHB.string(Cuenta), parametros, Long.toString(estado.getCuil()), estado.getIdCobis());

						/*
						 * sin tener sesión, la primera dirección de mail queda guardada en el cache y
						 * se reúsa en las siguientes llamadas, por lo que necesitamos eliminarlo del
						 * cache al final de cada ciclo
						 */
						Api.eliminarCache(contexto, "Email", estado.getIdCobis());
						Api.eliminarCache(contexto, "Email");
						Api.eliminarCache(contexto, "Email", contexto.idCobis());

					}

				} catch (Exception e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
					return Respuesta.error();
				}
			}
		}

		return Respuesta.estado("OK");
	}

	/**
	 * Lista de clientes con documentación al día en formato CUIL,estado
	 * 
	 * @param contexto
	 * @return
	 * 
	 * 
	 */
	public static Object documentacionActualizada(ContextoHB contexto) {
		UnauthorizedException.ifNot(contexto.request.headers("Authorization"));
		String actualizados = "";
		try {
			actualizados = listarActualizados(contexto);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return Respuesta.error();
		}

		return actualizados.getBytes();
	}

	/**
	 * similar documentacionActualizada, pero con queries y diferente formato
	 * 
	 * @param contexto
	 * @return
	 */
	public static Object reporteActualizados(ContextoHB contexto) {
		UnauthorizedException.ifNot(contexto.request.headers("Authorization"));
		Integer proceso = contexto.parametros.integer("proceso");
		String fecha = contexto.parametros.string("fecha");

		if (proceso == null && fecha == null) {
			System.err.println("Error: reporte de documentacion requiere al menos un parámetro.");
			return Respuesta.parametrosIncorrectos();
		} else {
			String reporte = "";
			try {
				reporte = obtenerReportesActualizacion(proceso, fecha);
			} catch (Exception e) {
				System.err.println(e.getMessage());
				e.printStackTrace();
				return Respuesta.error();
			}

			return reporte.getBytes();
		}
	}

	/*
	 * ACTUALIZACION DE PERSONAS
	 */

	private static boolean checkDataFormularios(Objeto datos) {
		boolean esPEP = datos.bool("IPEP_DeclaracionCliente", false);
		String cargoPEP = datos.string("IPEP_CargoFuncionJerarquia", "");

		boolean esSO = datos.bool("IDSO_SujetoObligado", false);
		String tipoSO = datos.string("TSO_Id", "");

		boolean checkPEP = false;
		boolean checkSO = false;

		if (!esPEP && cargoPEP.isEmpty()) {
			checkPEP = true;
		} else if (esPEP && !cargoPEP.isEmpty()) {
			for (Objeto par : CARGOS_PEP.objetos()) {
				if (par.string("codigo").equals(cargoPEP)) {
					checkPEP = true;
					break;
				}
			}
		}

		if (!esSO && tipoSO.isEmpty()) {
			checkSO = true;
		} else if (esSO && !tipoSO.isEmpty()) {
			for (Objeto par : TIPOS_SO.objetos()) {
				if (par.string("codigo").equals(tipoSO)) {
					checkSO = true;
					break;
				}
			}
		}

		return checkPEP && checkSO;
	}

	private static void actualizarDatosPersona(ContextoHB contexto, long cuil, Objeto parche) {
		ApiResponse respuestaActualizacion = RestPersona.actualizarPersona(contexto, parche);

		if (respuestaActualizacion.hayError()) {
			/*
			 * TODO: por ahora no hace fallar todo el proceso, API Personas es caprichosa
			 */
			System.err.println(String.format("Error al actualizar persona con CUIL %d, continuando.", cuil));
		}
	}

	private static Objeto actualizacionPrevencion(Objeto formularios) {
		Objeto actualizacion = new Objeto();
		actualizacion.set("esPEP", formularios.bool("IPEP_DeclaracionCliente"));
		actualizacion.set("cargoPEP", formularios.bool("IPEP_DeclaracionCliente", false) ? formularios.string("IPEP_CargoFuncionJerarquia") : null);

		actualizacion.set("esSO", formularios.bool("IDSO_SujetoObligado"));
		actualizacion.set("idTipoSO", formularios.bool("IDSO_SujetoObligado", false) ? formularios.string("TSO_Id") : "");

		return actualizacion;
	}

	private static Objeto actualizacionPersonaIngresos(ContextoHB contexto, Objeto patrimonio) throws Exception {
		// TODO: dependiendo de la actividad laboral del cliente, añadir diferentes
		// campos
		SituacionLaboral situacionLaboral = idASituacionLaboral(patrimonio.string("idSituacionLaboral"));

		switch (situacionLaboral) {
		case AUTONOMO:
			ApiResponse personaResponse = RestPersona.consultarPersonaEspecifica(contexto, contexto.persona().cuit());
			if (personaResponse.hayError()) {
				throw new Exception("Error al recuperar datos para actualizar persona.");
			}

			String categoriaResponse = personaResponse.string("categoriaMonotributo");
			String categoriaMonotributo = patrimonio.string("categoriaMonotributo");

			if (categoriaMonotributo.equals("")) /* responsable inscripto */ {
				// Solo se actualiza la situacion de monotributo si es diferente a la actual
				if (!categoriaResponse.equals(categoriaMonotributo)) {
					patrimonio.add("fechaRecategorizacionMonotributo", Fecha.fechaYHoraActual());
				}
			} else if (!categoriaResponse.equals(categoriaMonotributo)) /* monotributista */ {
				patrimonio.add("fechaRecategorizacionMonotributo", Fecha.fechaYHoraActual());
			}
			break;
		case DESEMPLEADO:
			break;
		case DEPENDENCIA_CONTRATADO: // fallthrough explícito
		case DEPENDENCIA_FIJO:
			break;
		default:
			break;
		}

		return patrimonio;
	}

	/*
	 * CONSULTA Y ACTUALIZACION DDBB
	 */
	private static EstadoDocumentacion consultaDocumentacion(long cuil) throws Exception {
		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");
		// sqlRequest.sql += "SELECT * FROM Homebanking.dbo.estado_documentacion WHERE
		// cuil_id = ?;";
		sqlRequest.sql += "SELECT cuil_id, estado, nombre_completo, tipo_notificacion FROM Homebanking.dbo.estado_documentacion WHERE cuil_id = ?;";
		sqlRequest.parametros.add(cuil);

		SqlResponse response = Sql.response(sqlRequest);
		if (response.hayError) {
			throw new Exception("Error al recuperar documentacion asociada al cuil " + cuil);
		} else if (response.registros.size() == 0) {
			return null;
		}

		Objeto registro = response.registros.get(0);
		return new EstadoDocumentacion(registro.string("nombre_completo"), registro.integer("estado"), registro.longer("cuil_id"), codigoATipoNotificacion(registro.integer("tipo_notificacion")));
	}

	private static String listarActualizados(ContextoHB contexto) throws Exception {
		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");
		sqlRequest.sql += "SELECT cuil_id, nombre_completo FROM Homebanking.dbo.estado_documentacion WHERE estado = 0;";

		SqlResponse response = Sql.response(sqlRequest);
		if (response.hayError) {
			throw new Exception("Error al recuperar estados de documentación de la base de datos.");
		}

		String listado = "CUIL, nombre completo\n";
		for (Objeto c : response.registros) {
			listado += c.string("cuil_id") + "," + c.string("nombre_completo") + "\n";
		}

		return listado;
	}

	private static int chequearEstado(long cuil) throws Exception {
		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");

		sqlRequest.sql += "SELECT estado FROM Homebanking.dbo.estado_documentacion WHERE cuil_id = ?;";
		sqlRequest.parametros.add(cuil);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			System.err.println("Error al recuperar estado de documentación para cliente con cuil " + cuil + ", devolviendo estado 0.");
			return 0;
		} else if (sqlResponse.registros.isEmpty()) {
			return 0;
		}

		return sqlResponse.registros.get(0).integer("estado");
	}

	private static void actualizarEstado(long cuil, int nuevoEstado, String nombre, TipoNotificacion tipoNotificacion) throws Exception {
		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");

		sqlRequest.sql += "begin tran " + "if not exists (select  cuil_id, estado, nombre_completo, tipo_notificacion from Homebanking.dbo.estado_documentacion where cuil_id = ?) " // 1
				+ "begin " + "insert into Homebanking.dbo.estado_documentacion " + "(cuil_id, estado, nombre_completo, tipo_notificacion) " + "values (?, ?, ?, ?) " // 2, 3, 4, 5
				+ "end " + "else begin " + "UPDATE Homebanking.dbo.estado_documentacion " + "SET estado = ?, tipo_notificacion = ? WHERE cuil_id = ?; " // 6, 7, 8
				+ "end commit;";

		sqlRequest.parametros.add(cuil); // 1
		sqlRequest.parametros.add(cuil); // 2
		sqlRequest.parametros.add(nuevoEstado); // 3
		sqlRequest.parametros.add(nombre); // 4
		sqlRequest.parametros.add(tipoNotificacion.value); // 5
		sqlRequest.parametros.add(nuevoEstado); // 6
		sqlRequest.parametros.add(tipoNotificacion.value); // 7
		sqlRequest.parametros.add(cuil); // 8

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			throw new Exception("Error al actualizar estado de documentación para cliente con cuil " + cuil);
		}
	}

	private static String obtenerReportesActualizacion(Integer proceso, String fecha) throws Exception {
		String query = construirQueryReporteActualizacion(proceso, fecha);
		SqlRequest sqlRequest = Sql.request("ConsultaReporteDocumentacion", "homebanking");

		sqlRequest.sql += "SELECT cuil_id, proceso, fecha FROM homebanking.dbo.reportes_documentacion ";
		sqlRequest.sql += query + ";";
		if (proceso != null) {
			sqlRequest.parametros.add(proceso);
		}

		if (fecha != null && !fecha.isEmpty()) {
			sqlRequest.parametros.add(fecha);
		}

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			throw new Exception("Error al obtener reporte de clientes actualizados.");
		}

		String reporte = "cuil,proceso,fecha";
		for (Objeto r : sqlResponse.registros) {
			reporte += "\n" + r.longer("cuil_id") + "," + r.integer("proceso") + "," + r.string("fecha");
		}

		return reporte;
	}

	private static void guardarReporteActualizacion(long cuil, int proceso, String fecha) throws Exception {
		SqlRequest sqlRequest = Sql.request("ReporteDocumentacion", "homebanking");

		sqlRequest.sql += "INSERT INTO Homebanking.dbo.reportes_documentacion ";
		sqlRequest.sql += "(cuil_id, proceso, fecha) ";
		sqlRequest.sql += "VALUES (?, ?, ?);";

		sqlRequest.parametros.add(cuil);
		sqlRequest.parametros.add(proceso);
		sqlRequest.parametros.add(fecha);

		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			throw new Exception("Error al registrar reporte de actualización de documentación.");
		}
	}

	private static String construirQueryReporteActualizacion(Integer proceso, String fecha) {
		List<String> tokens = new ArrayList<>();

		if (proceso != null) {
			tokens.add(" WHERE proceso = ? ");
		}

		if (fecha != null && !fecha.isEmpty()) {
			tokens.add(" WHERE fecha = ? ");
		}

		return String.join(" AND ", tokens);
	}

	/*
	 * CONVERSIÓN DE ENUMS Y CONSTANTES
	 */

	private static String[] estadosAClases(int estados) {
		ArrayList<String> descripciones = new ArrayList<>();

		Stream.of(Documentacion.values()).filter(d -> d != Documentacion.NADA).forEach(d -> {
			if (Bitwise.banderaActiva(estados, d.value)) {
				try {
					descripciones.add(estadoAClase(d));
				} catch (Exception e) {
					System.err.println(e.getMessage());
				}
			}
		});

		String[] ret = new String[descripciones.size()];
		return descripciones.toArray(ret);
	}

	// TODO: Actualizar AMBOS métodos al agregar una nueva clase documental!
	private static Documentacion claseAEstado(String estado) throws Exception {
		switch (estado) {
		case "":
			return Documentacion.NADA;
		case "PrevencionLavadoH":
			return Documentacion.PREVENCION_LAVADO;
		case "IngresosH":
			return Documentacion.INGRESOS;
		case "DNI":
			return Documentacion.DNI;
		case "DocumentacionRespaldatoriaPrevencionH":
			return Documentacion.RESPALDATORIA;
		case "ComprobanteDomicilio":
			return Documentacion.CONSTANCIA_DOMICILIO;
		default:
			throw new Exception("Clase documental inesperada.");
		}
	}

	private static String estadoAClase(Documentacion doc) throws Exception {
		switch (doc) {
		case CONSTANCIA_DOMICILIO:
			return "ComprobanteDomicilio";
		case DNI:
			return "DNI";
		case INGRESOS:
			return "IngresosH";
		case PREVENCION_LAVADO:
			return "PrevencionLavadoH";
		case RESPALDATORIA:
			return "DocumentacionRespaldatoriaPrevencionH";
		default:
			throw new Exception("Clase documental inesperada");
		}
	}

	private static SituacionLaboral idASituacionLaboral(String id) throws Exception {
		switch (id) {
		case "1":
			return SituacionLaboral.AUTONOMO;
		case "2":
			return SituacionLaboral.DESEMPLEADO;
		case "3":
			return SituacionLaboral.JUBILADO;
		case "4":
			return SituacionLaboral.OTRO;
		case "5":
			return SituacionLaboral.DEPENDENCIA_CONTRATADO;
		case "6":
			return SituacionLaboral.DEPENDENCIA_FIJO;
		case "7":
			return SituacionLaboral.SUB_EMPLEADO;
		case "8":
			return SituacionLaboral.SUBSIDIADO;
		default:
			throw new Exception("ID de situación laboral no válido.");
		}
	}

	private static TipoNotificacion codigoATipoNotificacion(int codigo) throws Exception {
		switch (codigo) {
		case 0:
			return TipoNotificacion.SIMPLE;
		case 1:
			return TipoNotificacion.CUENTA_DOLARES;
		case 2:
			return TipoNotificacion.SOS;
		case 3:
			return TipoNotificacion.Inversion;
		default:
			throw new Exception("ID de tipo de notificacion no válido.");
		}
	}

	/*
	 * PARSEO DE PLANILLAS DE REQUERIMIENTO DE DOCUMENTACION
	 */

	private static ArrayList<EstadoDocumentacion> excelToEstados(byte[] excel, TipoNotificacion tipoNotificacion) throws Exception {
		XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excel));
		XSSFSheet sheet = workbook.getSheetAt(0);

		ArrayList<EstadoDocumentacion> filas = new ArrayList<>();

		for (Row row : sheet) {
			try {
				filas.add(new EstadoDocumentacion(row.getCell(INDEX_NOMBRE).toString(), parsearEstados(row), parsearIdCobis(row.getCell(INDEX_COBIS)), parsearCUIL(row.getCell(INDEX_CUIL)), tipoNotificacion));
			} catch (Exception e) {
				continue;
			}
		}

		workbook.close();

		return filas;
	}

	private static String parsearIdCobis(Cell cell) {
		String idCobis = new DataFormatter().formatCellValue(cell);

		if (idCobis == null) {
			throw new NullPointerException();
		} else if (idCobis.isEmpty()) {
			throw new RuntimeException();
		}

		return idCobis;
	}

	private static long parsearCUIL(Cell id) throws NumberFormatException {
		String cuilStr = new DataFormatter().formatCellValue(id);
		try {
			return Long.parseLong(cuilStr);
		} catch (NumberFormatException e) {
			throw e;
		}
	}

	private static int parsearEstados(Row fila) {
		int estados = 0;

		for (CeldaDocumentacion c : CeldaDocumentacion.values()) {
			Cell cell = fila.getCell(c.index);
			if (cell != null && cell.toString().equals("SI")) {
				estados = Bitwise.activarBandera(estados, c.tipoDocumentacion.value);
			}
		}

		return estados;
	}

	/*
	 * MISCELÁNEOS
	 */

	private static Objeto parsearParCSV(String nombre, String delimitador) {
		InputStream csvIStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(nombre);

		Objeto lines = new Objeto();
		try {
			String rawLines = new String(IOUtils.toByteArray(csvIStream), "UTF-8");
			for (String line : rawLines.split("\n")) {
				String[] tokens = line.split(delimitador, 2);
				Objeto obj = new Objeto();
				obj.set("codigo", tokens[0].trim());
				obj.set("descripcion", tokens[1].trim());
				lines.add(obj);
			}
			csvIStream.close();
		} catch (IOException e) {
			System.err.println("Error al parsear " + nombre + ": " + e.getMessage());
		}

		return lines;
	}

	public static String FechaActualizacion(long Cuil) {

		// Metodo que va a Buscar la Fecha Mas grande que se Actualizo dicho Cuil que se
		// pasa por parametro

		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");
		sqlRequest.sql += "SELECT fecha FROM Homebanking.dbo.reportes_documentacion WHERE  fecha in (Select max(fecha) from Homebanking.dbo.reportes_documentacion where cuil_id = ?);";
		sqlRequest.parametros.add(Cuil);

		SqlResponse response = Sql.response(sqlRequest);
		if (response.hayError) {
			System.out.println("Error al recuperar documentacion asociada al cuil " + Cuil);
		} else if (response.registros.size() == 0) {
			return null;
		}

		return response.registros.toString();
	}

	public static Respuesta estadoDocumentacion(ContextoHB contexto) {
		contexto.sesion.setChallengeOtp(false);

		try {
			long cuil = RestPersona.clientes(contexto).objetos().get(0).longer("cuit");

			Respuesta respuesta = new Respuesta();
			int estado = 0;

			try {
				estado = chequearEstado(cuil);
			} catch (Exception e) {
				e.printStackTrace();
				return Respuesta.error();
			}

			// Te Agrego esto que viene del metodo original porque sino estaba mostrando el
			// popup a todos los usuarios
			if (estado == 0) {
				return Respuesta.estado("DOCUMENTACION_AL_DIA");
			}

			/*
			 * Se Agrega Modificacion Para enviar Las clases Documentales Que Estan
			 * Actualizadas con su ultima Fecha de Actualizacion C06635
			 */

			// Voy a Buscar la ultima Fecha de Actualizacion
			/*
			 * String Fecha = FechaActualizacion(cuil);
			 * 
			 * if (Fecha != null && !Fecha.isEmpty()) { Fecha = Fecha.substring(15, 25); }
			 * else { Fecha = "-"; }
			 */
			// Creo 3 Arrays para la Verificacion de las Clases Documentales que Estan
			// Actualizadas

			ArrayList<String> a = new ArrayList<>();

			String[] arr = new String[5];

			String[] lala = new String[5];

			arr[0] = "DNI";
			arr[1] = "IngresosH";
			arr[2] = "PrevencionLavadoH";
			arr[3] = "DocumentacionRespaldatoriaPrevencionH";
			arr[4] = "ComprobanteDomicilio";

			a.add("DNI");
			a.add("IngresosH");
			a.add("PrevencionLavadoH");
			a.add("DocumentacionRespaldatoriaPrevencionH");
			a.add("ComprobanteDomicilio");

			respuesta.set("estado", "PENDIENTE");

			lala = estadosAClases(estado);

			String[] textoInformado = new String[5];

			for (int aa = 0; aa < lala.length; aa++) {

				textoInformado[aa] = Consulta_textoInformado(cuil, lala[aa]);

			}

			Objeto lines = new Objeto();

			for (int b = 0; b < lala.length; b++) {
				Objeto obj = new Objeto();
				obj.set("Clase", lala[b]);
				obj.set("Texto_Informado", textoInformado[b]);
				lines.add(obj);
			}

			faltantes = lines;

			respuesta.set("faltantes", faltantes);

			lala = estadosAClases(estado);

			for (int z = 0; z < arr.length; z++) {
				for (int b = 0; b < lala.length; b++) {
					if (arr[z].equals(lala[b])) {
						b = 100;
						a.remove(arr[z]);
					}
				}
			}

			Objeto lines2 = new Objeto();

			for (int i = 0; i < a.size(); i++) {
				Objeto obj = new Objeto();
				obj.set("Clase", arr[i]);
				lines2.add(obj);
			}

			completas = lines2;

			respuesta.set("Completas", completas);

			if (Bitwise.banderaActiva(estado, Documentacion.PREVENCION_LAVADO.value)) {
				respuesta.set("cargosPEP", CARGOS_PEP);
				respuesta.set("tiposSO", TIPOS_SO);
			}

			return respuesta;
		} catch (Exception e) {
			return Respuesta.error();
		}
	}

	private static String Consulta_textoInformado(long cuil, String claseDocumental) {

		switch (claseDocumental) {
		case "DNI":
			claseDocumental = "1";
			break;
		case "IngresosH":
			claseDocumental = "2";
			break;
		case "PrevencionLavadoH":
			claseDocumental = "3";
			break;
		case "DocumentacionRespaldatoriaPrevencionH":
			claseDocumental = "4";
			break;
		case "ComprobanteDomicilio":
			claseDocumental = "5";
			break;
		}

		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");
		sqlRequest.sql += "SELECT texto_reclamo FROM Homebanking.dbo.estado_ReclamoDocumentacion WHERE  cuil_id =" + cuil + " and clase_documental = '" + claseDocumental + "' and fecha_reclamo = (Select max(fecha_reclamo) FROM Homebanking.dbo.estado_ReclamoDocumentacion WHERE  cuil_id = " + cuil + " and clase_documental = '" + claseDocumental + "') and informado = 0;";

		SqlResponse response = Sql.response(sqlRequest);
		if (response.hayError) {
			System.out.println("Error al recuperar documentacion asociada al cuil " + cuil);
		} else if (response.registros.size() == 0) {
			return null;
		}

		return response.registros.get(0).get("texto_reclamo").toString();

	}

	public static Respuesta guardarDocumentacion(ContextoHB contexto) {
		Objeto datos = contexto.parametros;
		Objeto datos2 = contexto.parametros;
		long cuil = RestPersona.clientes(contexto).objetos().get(0).longer("cuit");
		boolean tieneBloqueOperaciones = false;

		if (datos == null) {
			return Respuesta.parametrosIncorrectos();
		}

		try {
			int estado = chequearEstado(cuil);

			if (datos.existe("formularios") && datos.objeto("formularios") != null) {
				if (!checkDataFormularios(datos.objeto("formularios"))) {
					System.err.println("ERROR: datos inválidos en formulario PEP/FATCA.");
					return Respuesta.parametrosIncorrectos();
				}
				Objeto formularios = datos.objeto("formularios");

//				ApiRequest req = null;

				// Modificacion: Se envian los datos del formulario a api-Persona

				/*
				 * if (Config.esDesarrollo() || Config.esHomologacion()) {
				 * 
				 * req = Api.request("API-Personas_ModificarParcialmentePersona", "personas",
				 * "PATCH", "/personas/" + cuil, contexto);
				 * 
				 * Boolean PEP = formularios.bool("IPEP_DeclaracionCliente");
				 * 
				 * Boolean IDSO = formularios.bool("IDSO_SujetoObligado");
				 * 
				 * String fecha = formularios.string("dtpFecha");
				 * 
				 * int CFatca = formularios.integer("IDF_CFatca_Id");
				 * 
				 * Objeto BodyPEP = new Objeto();
				 * 
				 * if (PEP) {
				 * 
				 * String IPEP_CargoFuncionJerarquia =
				 * formularios.string("IPEP_CargoFuncionJerarquia");
				 * 
				 * BodyPEP.set("idVersionDocumento", "B");
				 * BodyPEP.set("fechaPresentacionDDJJSO", fecha);
				 * BodyPEP.set("fechaIncripcionSO", fecha); BodyPEP.set("esPEP", PEP);
				 * BodyPEP.set("cargoPEP", IPEP_CargoFuncionJerarquia);
				 * BodyPEP.set("otroCargoPEP", "Prueba"); BodyPEP.set("fechaDeclaracionPEP",
				 * fecha);
				 * 
				 * }
				 * 
				 * if (IDSO) {
				 * 
				 * String TSO = formularios.string("TSO_Id");
				 * 
				 * String presentoConstanciaSO = formularios.string("presentoConstanciaSO");
				 * 
				 * String IDSO_DisposicionPrevencion =
				 * formularios.string("IDSO_DisposicionPrevencion");
				 * 
				 * BodyPEP.set("fechaPresentacionDDJJSO", fecha);
				 * BodyPEP.set("fechaIncripcionSO", fecha); BodyPEP.set("presentoConstanciaSO",
				 * presentoConstanciaSO); BodyPEP.set("esSO", IDSO); BodyPEP.set("idTipoSO",
				 * TSO); BodyPEP.set("adoptoDisposicionesSO", IDSO_DisposicionPrevencion);
				 * BodyPEP.set("fechaDeclaracionPEP", fecha);
				 * 
				 * }
				 * 
				 * String IDF_TIN = formularios.string("IDF_TIN"); String IDF_TIN2 =
				 * formularios.string("IDF_TIN2"); String PAI_Descripcion1 =
				 * formularios.string("PAI_Descripcion1"); String PAI_Descripcion2 =
				 * formularios.string("PAI_Descripcion2");
				 * 
				 * if (CFatca == 2) {
				 * 
				 * BodyPEP.set("idCategoriaFatca", IDF_TIN);
				 * 
				 * }
				 * 
				 * BodyPEP.set("fechaPresentacionDDJJSO", fecha);
				 * BodyPEP.set("fechaIncripcionSO", fecha); BodyPEP.set("numeroContribuyente1",
				 * PAI_Descripcion1); BodyPEP.set("numeroContribuyente2", PAI_Descripcion2);
				 * BodyPEP.set("idPaisResidencia1", IDF_TIN); BodyPEP.set("idPaisResidencia2",
				 * IDF_TIN2); BodyPEP.set("fechaInicialResidencia1", fecha);
				 * BodyPEP.set("fechaFinalResidencia1", fecha);
				 * BodyPEP.set("fechaInicialResidencia2", fecha);
				 * BodyPEP.set("fechaFinalResidencia2", fecha);
				 * BodyPEP.set("idSegundaNacionalidad", null);
				 * BodyPEP.set("idTerceraNacionalidad", null);
				 * 
				 * req.body(BodyPEP);
				 * 
				 * String actividad = formularios.string("idSituacionLaboral");
				 * 
				 * String fecha_actividad = formularios.string("fechaInicioActividad");
				 * 
				 * actividad = "1";
				 * 
				 * fecha_actividad = "2023-03-01";
				 * 
				 * Boolean verifico = patchActividades(contexto, actividad, fecha_actividad);
				 * 
				 * if (!verifico) { System.out.print("Error al actualizar situacion laboral"); }
				 * 
				 * // Modificacion para completar documento compartido Digit-187
				 * 
				 * String Descripcion_PR3 = formularios.string("PR3_Descripcion");
				 * 
				 * String nombre_completo = formularios.string("INT_Nombres") + " " +
				 * formularios.string("INT_Apellido");
				 * 
				 * Boolean verifico_doc = Documento_compartido(nombre_completo, Descripcion_PR3,
				 * contexto);
				 * 
				 * if (!verifico_doc) {
				 * System.out.print("Error al actualizar documento compartido"); }
				 * 
				 * } else {
				 */

				// Modificacion urgente para guardar en formulario en la tabla
				// Estado_Documentacion
				SubirFormulario(formularios, cuil);
				estado = Bitwise.desactivarBandera(estado, Documentacion.PREVENCION_LAVADO.value);
				actualizarDatosPersona(contexto, cuil, actualizacionPrevencion(formularios));

				estado = Bitwise.desactivarBandera(estado, Documentacion.PREVENCION_LAVADO.value);
				actualizarDatosPersona(contexto, cuil, actualizacionPrevencion(formularios));

				// }

				/*
				 * ApiResponse res = Api.response(req, contexto.idCobis());
				 * 
				 * if (res.hayError()) { System.err.
				 * println("Error al subir formulario para API Formularios. No se pudo actualizar documentación de prevención."
				 * ); return Respuesta.error(); } else { estado =
				 * Bitwise.desactivarBandera(estado, Documentacion.PREVENCION_LAVADO.value);
				 * actualizarDatosPersona(contexto, cuil, actualizacionPrevencion(formularios));
				 * }
				 */
			}

			if (datos.existe("documentos") && datos.objeto("documentos") != null) {
				String outputFilePath = getDoc(contexto.idCobis());
				String claseDocumental = "";
				Objeto patrimonio = null;
				PDDocument document = new PDDocument();
				ApiResponse res = null;
				boolean actualizaPatrimonio = false;
				boolean subeArchivo = false;

				for (Objeto doc : datos.objetos("documentos")) {

					patrimonio = doc.objeto("patrimonio", null);
					claseDocumental = doc.string("claseDocumental", null);
					if (claseDocumental == null || claseDocumental.isEmpty()) {
						throw new Exception("Error: clase documental faltante.");
					}

					// chequeamos que el archivo venga vacio SOLO cuando A) la clase documental sea
					// IngresosH y B) idSituacionLaboral sea "2" (SituacionLaboral.DESOCUPADO), en
					// cualquier otra condición, es un error
					subeArchivo = !doc.string("archivo").equals("");
					if (subeArchivo) {
						String nombre = doc.string("nombre", null);
						if (nombre == null || nombre.isEmpty()) {
							throw new Exception("Error: nombre de documento faltante.");
						}

						byte[] decodedBytes = Base64.getDecoder().decode(doc.string("archivo"));
						String extencion = nombre.substring(nombre.length() - 3).toLowerCase();
						if (extencion.equals("png") || extencion.equals("jpg") || extencion.equals("peg") || extencion.equals("jpe")) {
							PDPage page = new PDPage();
							document.addPage(page);
							PDImageXObject image = PDImageXObject.createFromByteArray(document, decodedBytes, "image");
							PDPageContentStream contentStream = new PDPageContentStream(document, page);
							float scale = 0.5f; // Escalar al 50% del tamaño original
							float imageWidth = image.getWidth() * scale;
							float imageHeight = image.getHeight() * scale;
							float startX = (page.getMediaBox().getWidth() - imageWidth) / 2; // Centrar horizontalmente
							float startY = (page.getMediaBox().getHeight() - imageHeight) / 2; // Centrar verticalmente
							contentStream.drawImage(image, startX, startY, imageWidth, imageHeight);
							contentStream.close();
							document.save(outputFilePath);
						}
						else if(isPdfHeaderValid(decodedBytes)) {
							PDDocument tempPdf = PDDocument.load(new ByteArrayInputStream(decodedBytes));
							PDFMergerUtility merger = new PDFMergerUtility();
							merger.appendDocument(document, tempPdf);
							document.save(outputFilePath);
						}
						else {
							borrarDoc(contexto.idCobis());
							return Respuesta.estado("ERROR_FORMATO");
						}

					} else if (claseDocumental.equals("IngresosH")) {
						patrimonio = doc.objeto("patrimonio", null);

						if (patrimonio == null) {
							throw new Exception("Error: datos de patrimonio faltantes.");
						}

						SituacionLaboral s = idASituacionLaboral(patrimonio.string("idSituacionLaboral"));
						if (s != SituacionLaboral.DESEMPLEADO) {

							throw new Exception("Error: archivo vacio.");
						} else if (s == SituacionLaboral.DESEMPLEADO) {
							// se agrega metodo para insertar datos de desempleado en la tabla
							// documentacion_Desempleado
							Inserto_Desempleado(cuil);
						}
					} else if (claseDocumental.equals("DocumentacionRespaldatoriaPrevencionH")) {
						String check = doc.string("ValidoCheck");
						if (check == "false") {
							throw new Exception("Error: archivo vacio.");
						}
					} else {
						throw new Exception("Error: archivo vacio.");
					}
				}

				if(subeArchivo){
					File file = new File(outputFilePath);
					byte[] fileContent = Files.readAllBytes(file.toPath());
					byte[] encodedBytes = Base64.getEncoder().encode(fileContent);
					res = RestArchivo.subirDocumentacion(contexto, "miPDF.pdf", claseDocumental, new String(encodedBytes));
				}

				borrarDoc(contexto.idCobis());

				if (res != null && res.hayError()) {
					return Respuesta.error();
				} else {

					tieneBloqueOperaciones = tieneBloqueOperacionesBool(contexto, claseDocumental);

					if (claseDocumental.equals("IngresosH")) {
						contexto.insertarContador("GUARDO_DOC_INGRESO");
						actualizaPatrimonio = true;
					}
					else if(claseDocumental.equals("DocumentacionRespaldatoriaPrevencionH")){
						contexto.insertarContador("GUARDO_DOC_EXTRAORDINARIA");
					}

					estado = Bitwise.desactivarBandera(estado, claseAEstado(claseDocumental).value);
				}

				if (actualizaPatrimonio) {
					try {
						actualizarDatosPersona(contexto, cuil, actualizacionPersonaIngresos(contexto, patrimonio));
					} catch (Exception e) {
						System.err.println(e.getMessage());
						System.err.println("Error al actualizar datos de ingresos via API Personas. Continuando.");
					}
				}
			}

			EstadoDocumentacion documentacion = consultaDocumentacion(cuil);
			TipoNotificacion tn = TipoNotificacion.SIMPLE;
			if (documentacion != null) {
				tn = documentacion.getTipoNotificacion();
				actualizarEstado(cuil, estado, contexto.persona().nombreCompleto(), documentacion.getTipoNotificacion());
			} else { // el cliente no existe en DB
				actualizarEstado(cuil, estado, contexto.persona().nombreCompleto(), tn);
			}

			if (estado == 0 && documentacion != null) {
				envioEmailSucursal(contexto);
				guardarReporteActualizacion(cuil, documentacion.getTipoNotificacion().value, Fecha.fechaActual());
				if (tn == TipoNotificacion.CUENTA_DOLARES) {
					// TODO: si es cliente con cuenta en dólares, notificar que su cuenta será
					// desbloqueada en breve
				}
			}
		} catch (Exception e) {
			borrarDoc(contexto.idCobis());
			return Respuesta.error();
		}

		return Respuesta.exito("esUPLD", tieneBloqueOperaciones);
	}

	private static boolean tieneBloqueOperacionesBool(ContextoHB ctx, String claseDocumental){
		return (claseDocumental.equals("IngresosH") || claseDocumental.equals("DocumentacionRespaldatoriaPrevencionH"))
				&& "REQUIERE_DOCUMENTACION".equals(HBPersona.tieneBloqueoOperaciones(ctx).string("estado"));
	}

	private static void envioEmailSucursal(ContextoHB contexto){
		try{
			if(HBAplicacion.funcionalidadPrendida(contexto.idCobis(), "prendido_envio_email_sucursal")){
				String email = Sucursal.email(contexto.persona().sucursal());
				String nombreCompleto = contexto.persona().nombres() + " " + contexto.persona().apellidos();

				Objeto parametros = new Objeto();
				parametros.set("ASUNTO", "Documentación para revisar – Alerta SOS");
				parametros.set("NOMBRE_COMPLETO", nombreCompleto.toUpperCase());
				parametros.set("CUIL", contexto.persona().cuit());
				RestNotificaciones.enviarCorreo(contexto, email, ConfigHB.string("doppler_envio_email_sucursal"), parametros);
			}
		}
		catch (Exception e){}
	}

	private static boolean isPdfHeaderValid(byte[] pdfBytes) {
		String header = new String(pdfBytes, 0, Math.min(pdfBytes.length, 10));
		return header.startsWith("%PDF");
	}

	private static String getDoc(String cobis){
		return "documentacion" + cobis + ".pdf";
	}
	private static void borrarDoc(String cobis){
		try {
			File archivo = new File(getDoc(cobis));
			archivo.delete();
		} catch (Exception e) {}
	}

	public static void SubirFormulario(Objeto formu, long cuil) throws Exception {

		int PEP = 0;
		int IDSO = 0;
		int IDSO_PresentoConstancia = 0;
		int IDSO_DisposicionPrevencion = 0;

		String fecha2 = formu.string("dtpFecha");

		if (formu.bool("IPEP_DeclaracionCliente")) {
			PEP = 1;
		}
		if (formu.bool("IDSO_SujetoObligado")) {
			IDSO = 1;
		}
		if (formu.bool("IDSO_PresentoConstancia")) {
			IDSO_PresentoConstancia = 1;
		}
		if (formu.bool("IDSO_DisposicionPrevencion")) {
			IDSO_DisposicionPrevencion = 1;
		}

		SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");

//		sqlRequest.sql += "INSERT INTO Homebanking.dbo.formulario_prevencion VALUES ("+cuil+",'"+OFI_Descripcion+"','"+INT_Apellido+"','"+INT_Nombres+"',"+INT_NumeroTrib+","+PEP+","+IPEP_CargoFuncionJerarquia+","+IDSO+","+TSO_Id+",'"+TSO_Descripcion+"',"+IDSO_PresentoConstancia+","+IDSO_DisposicionPrevencion+",'"+PR3_Descripcion+"','"+SLA_Descripcion+"',' ',"+ACT_FechaInicio+","+IDF_CFatca_Id+","+IDF_TIN+","+IDF_TIN2+",'"+PAI_Descripcion1+"','"+PAI_Descripcion2+"','"+fecha2+"');";

		sqlRequest.sql += "Select * from Homebanking.dbo.formulario_prevencion Where cuil_id = " + cuil;

		SqlResponse sqlResponse = Sql.response(sqlRequest);

		if (sqlResponse.registros.isEmpty()) {

			sqlRequest.sql = "INSERT INTO Homebanking.dbo.formulario_prevencion VALUES (" + cuil + ",'" + formu.string("OFI_Descripcion") + "','" + formu.string("INT_Apellido") + "','" + formu.string("INT_Nombres") + "'," + formu.string("INT_NumeroTrib") + "," + PEP + "," + formu.integer("IPEP_CargoFuncionJerarquia") + "," + IDSO + ",'" + formu.string("TSO_Id") + "','" + formu.string("TSO_Descripcion") + "'," + IDSO_PresentoConstancia + "," + IDSO_DisposicionPrevencion + ",'" + formu.string("PR3_Descripcion") + "','" + formu.string("SLA_Descripcion") + "',' '," + formu.string("ACT_FechaInicio") + "," + formu.string("IDF_CFatca_Id") + "," + formu.integer("IDF_TIN") + "," + formu.integer("IDF_TIN2") + ",'" + formu.string("PAI_Descripcion1") + "','" + formu.string("PAI_Descripcion2") + "','"
					+ fecha2 + "' , '" + fecha2 + "');";

			SqlResponse sqlResponse2 = Sql.response(sqlRequest);
			if (sqlResponse2.hayError) {
				throw new Exception("Error al insertar cliente en formulario prevencion:  " + cuil);
			}

		} else {

			sqlRequest.sql = "Update Homebanking.dbo.formulario_prevencion  set OFI_Descripcion = '" + formu.string("OFI_Descripcion") + "' , INT_Apellido = '" + formu.string("INT_Apellido") + "' , INT_Nombres = '" + formu.string("INT_Nombres") + "' , INT_NumeroTrib = " + formu.string("INT_NumeroTrib") + " , IPEP_DeclaracionCliente = " + formu.integer("IPEP_CargoFuncionJerarquia") + " , IDSO_SujetoObligado = '" + formu.string("TSO_Id") + "' , TSO_Descripcion = '" + formu.string("TSO_Descripcion") + "' , IDSO_PresentoConstancia = " + IDSO_PresentoConstancia + " , IDSO_DisposicionPrevencion = '" + IDSO_DisposicionPrevencion + "' , PR3_Descripcion = '" + formu.string("PR3_Descripcion") + "' , SLA_Descripcion = '" + formu.string("SLA_Descripcion") + "' , ACT_FechaInicio = '"
					+ formu.string("ACT_FechaInicio") + "' , IDF_CFatca_Id = '" + formu.string("IDF_CFatca_Id") + "' , IDF_TIN = " + formu.integer("IDF_TIN") + " , IDF_TIN2 = " + formu.integer("IDF_TIN2") + " , PAI_Descripcion1 = '" + formu.string("PAI_Descripcion1") + "' , PAI_Descripcion2= '" + formu.string("PAI_Descripcion2") + "', dtpFecha = '" + fecha2 + "' , Fecha_ActFormulario = '" + fecha2 + "'  where cuil_id = " + cuil;

			SqlResponse sqlResponse3 = Sql.response(sqlRequest);
			if (sqlResponse3.hayError) {
				throw new Exception("Error al actualizar cliente en tabla formulario_prevencion: " + cuil);
			}
		}

	}

	private static String setExtensionNombreArchivo(String nombreArchivo, String extension) {
		Set<String> formatos = Objeto.setOf(".pdf", ".png", ".jpg", ".peg", ".jpe", ".jpg");
		if (!formatos.contains(nombreArchivo)) {
			String[] ext = extension.split("/");
			extension = ext.length > 1 ? ext[1] : ext[0];
			extension = extension.replace(".", "");
			nombreArchivo = nombreArchivo + "." + extension;
		}
		return nombreArchivo;
	}

	public static Respuesta sucursalesAndreani(ContextoHB contexto) {

		Respuesta respuesta = new Respuesta();

		ApiRequest req;

		req = Api.request("Andreani", "andreani-sucursales", "GET", "v2/sucursales", contexto);

		ApiResponse res = Api.response(req, contexto.idCobis());

		if (res.hayError()) {
			System.err.println("Error al comunicarse con andreani sucursales");
			return Respuesta.error();
		} else {

			respuesta.set("Sucursales", res.json);

		}

		return respuesta;

	}

	public static Respuesta getActividades(ContextoHB contexto) {

		Respuesta respuesta = new Respuesta();

		ApiRequest req;

		req = Api.request("API-Personas_ConsultaActividad", "personas", "GET", "/actividades", contexto);

		ApiResponse res = Api.response(req, contexto.idCobis());

		if (res.hayError()) {
			System.err.println("Error al comunicarse con API-Personas_ConsultaActividad");
			return Respuesta.error();
		} else {

			respuesta.set("Sucursales", res.json);

		}

		return respuesta;

	}

	public static Boolean patchActividades(ContextoHB contexto, String Actividad, String fecha_Actividad) {

		ApiRequest req;

		req = Api.request("API-Personas_ModificarActividad", "personas", "PATCH", "/actividades/" + contexto.idCobis(), contexto);

		Objeto Body = new Objeto();

		Body.set("idSituacionLaboral", Actividad);
		Body.set("fechaInicioActividad", "2023-03-01T17:01:49.507Z");

		req.body(Body);

		ApiResponse res = Api.response(req, contexto.idCobis());

		if (res.hayError()) {
			System.err.println("Error al comunicarse con API-Personas_ModificarActividad");
			return false;
		} else {
			return true;
		}

	}

	@SuppressWarnings("unused")
	public static Boolean Documento_compartido(String nombre, String descripcion, ContextoHB contexto) throws IOException {

		try {

			Date hoy = new Date();

			String fecha = new SimpleDateFormat("dd/MM/yyyy").format(hoy);

			long cuil = RestPersona.clientes(contexto).objetos().get(0).longer("cuit");

			String ruta = "C:\\rutaExcel\\Registros_formularioHB.xlsx";

			FileInputStream file = new FileInputStream(ruta);

			XSSFWorkbook wb = new XSSFWorkbook(file);
			XSSFSheet sheet = wb.getSheetAt(0);

			Iterator<Row> rowIterator = sheet.iterator();

			Row row = null;

			// se recorre cada fila hasta el final
			while (rowIterator.hasNext()) {
				row = rowIterator.next();
				// se obtiene las celdas por fila
				Iterator<Cell> cellIterator = row.cellIterator();
				Cell cell;

				while (cellIterator.hasNext()) {
					cell = cellIterator.next();
					CellType a = cell.getCellType();

				}

			}

			int numero = row.getRowNum() + 1;

			XSSFRow fila1 = sheet.getRow(numero);

			if (fila1 == null) {
				fila1 = sheet.createRow(numero);
			}

			XSSFRow fila2 = sheet.getRow(numero);

			if (fila2 == null) {
				fila2 = sheet.createRow(numero);
			}

			XSSFRow fila3 = sheet.getRow(numero);

			if (fila3 == null) {
				fila3 = sheet.createRow(numero);
			}

			XSSFRow fila4 = sheet.getRow(numero);

			if (fila4 == null) {
				fila4 = sheet.createRow(numero);
			}

			XSSFCell celda1 = fila1.createCell(1);

			celda1 = fila1.createCell(0);

			celda1.setCellValue(cuil);

			XSSFCell celda2 = fila2.createCell(2);

			celda2 = fila2.createCell(1);

			celda2.setCellValue(nombre);

			XSSFCell celda3 = fila3.createCell(3);

			celda3 = fila3.createCell(2);

			celda3.setCellValue(descripcion);

			XSSFCell celda4 = fila4.createCell(4);

			celda4 = fila4.createCell(3);

			celda4.setCellValue(fecha);

			file.close();

			FileOutputStream output = new FileOutputStream(ruta);

			wb.write(output);
			output.close();
			wb.close();

			return true;

		} catch (IOException e) {

			return false;
		}

	}

	// esta version no realiza updates de la persona, se encarga unicamente de subir
	// los arhivos al contenedor
	public static Respuesta guardarDocumentacionV2(ContextoHB contexto) {
		Objeto datos = contexto.parametros;

		if (datos == null) {
			return Respuesta.parametrosIncorrectos();
		}

		try {
			if (datos.existe("documentos") && datos.objeto("documentos") != null) {

				for (Objeto doc : datos.objetos("documentos")) {
					String claseDocumental = doc.string("claseDocumental", null);

					if (claseDocumental == null || claseDocumental.isEmpty()) {
						return Respuesta.estado("SIN_CLASE_DOCUMENTAL");
					}

					boolean subeArchivo = !doc.string("archivo").equals("");

					if (subeArchivo) {
						String nombre = doc.string("nombre", null);

						if (nombre == null || nombre.isEmpty()) {
							return Respuesta.estado("SIN_NOMBRE_ARCHIVO");
						}

						nombre = setExtensionNombreArchivo(nombre, doc.string("extension"));
						ApiResponse res = RestArchivo.subirDocumentacion(contexto, nombre, claseDocumental, doc.string("archivo"));

						if (res.hayError()) {
							return Respuesta.estado("ERROR_SUBIENDO_ARCHIVO");
						}
					} else {
						return Respuesta.estado("ERROR_SIN_ARCHIVO");
					}
				}

			}
		} catch (Exception e) {
			e.printStackTrace();
			return Respuesta.error();
		}
		contexto.sesion.setAdjuntaDocumentacion(true);
		return Respuesta.exito();
	}

	public static Respuesta documentacionXSolicitud(ContextoHB contexto) {
		String idSolicitud = contexto.parametros.string("idSolicitud");
		Respuesta respuesta = new Respuesta();

		if (Objeto.empty(idSolicitud)) {
			Respuesta.parametrosIncorrectos();
		}

		Objeto documentacionRequerida = DocumentacionService.docRequeridoXCanalAmarillo(contexto, idSolicitud);
		if (Objeto.empty(documentacionRequerida)) {
			return Respuesta.estado("ERROR_SIN_DOCUMENTOS_REQUERIDOS");
		}

		return respuesta.set("documentacion", documentacionRequerida);
	}

	public static void Inserto_Desempleado(long cuil) {

		try {

			SqlRequest sqlRequest = Sql.request("ConsultaDocumentacion", "homebanking");

			sqlRequest.sql = "Insert into [Homebanking].[dbo].[documentacion_Desempleado]  (cuil_id,procesado,fecha_de_completitud) values (" + cuil + ",0, '" + Fecha.fechaActual() + "')";

			SqlResponse response = Sql.response(sqlRequest);

			if (response.hayError) {
				System.out.println("Error al insertar cuil: " + cuil + " en la tabla documentacion_Desempleado");
			}
		} catch (Exception e) {
			System.out.println("Error Al Insertar cuil: " + cuil + " en tabla documentacion_Desempleado: " + e);
		}

	}

	public static Respuesta Enviar_Captura(ContextoHB contexto) {

		Objeto datos = contexto.parametros;

		if (datos.existe("documentos") && datos.objeto("documentos") != null) {

			for (Objeto doc : datos.objetos("documentos")) {

				String claseDocumental = doc.string("claseDocumental", null);
				if (claseDocumental == null || claseDocumental.isEmpty()) {
					return Respuesta.error();
				}

				boolean subeArchivo = !doc.string("archivo").equals("");

				if (subeArchivo) {
					String nombre = doc.string("nombre", null);

					if (nombre == null || nombre.isEmpty()) {
						return Respuesta.error();
					}

					try {
						ApiResponse res = null;

						res = RestArchivo.subirDocumentacion(contexto, nombre, claseDocumental, doc.string("archivo"));

						if (res != null && res.hayError()) {
							System.err.println("Error al subir documentacion adicional a FileNet. Código de respuesta: " + res.codigo);
							return Respuesta.error();
						}
					} catch (Exception e) {

						return Respuesta.error();
					}

				} else {

					if (claseDocumental.equals("FormOrigTyC")) {

						String id = doc.string("nombre", null);

						ByteArrayOutputStream buffer = null;
						try {
							InputStream is = ConfigHB.class.getResourceAsStream("/terminosycondiciones/" + id);
							buffer = new ByteArrayOutputStream();
							int nRead;
							byte[] data = new byte[1024];
							while ((nRead = is.read(data, 0, data.length)) != -1) {
								buffer.write(data, 0, nRead);
							}
							buffer.flush();
						} catch (Exception e) {
							throw new RuntimeException(e);
						}

						byte[] archivo = buffer.toByteArray();

						String Archivo_s = "";

						Archivo_s = Base64.getEncoder().encodeToString(archivo);

						try {
							ApiResponse res = null;

							res = RestArchivo.subirDocumentacion(contexto, id, claseDocumental, Archivo_s);

							if (res != null && res.hayError()) {
								System.err.println("Error al subir documentacion adicional a FileNet. Código de respuesta: " + res.codigo);
								return Respuesta.error();
							}

						} catch (Exception e) {

							return Respuesta.error();
						}

					} else {

						Respuesta.error();
						System.out.println("Error Archivo vino vacio");
					}

				}
			}

		}

		return Respuesta.exito();
	}

	public static Boolean Cliente_Nuevo(ContextoHB contexto) {
		try {
			ApiResponse res = null;

			String version = ConfigHB.string("hb_version_posicionconsolidada", "/v3");
			ApiRequest request = Api.request("Productos", "productos", "GET", version + "/posicionconsolidada", contexto);
			request.query("idcliente", contexto.idCobis());
			request.query("cancelados", "false");
			request.query("firmaconjunta", "false");
			request.query("firmantes", "false");
			request.query("adicionales", "true");
            request.query("tipoestado", "vigente");
			request.permitirSinLogin = true;
			request.cacheSesion = true;
			request.cache204 = true;

			res = Api.response(request, contexto.idCobis());

			if (res != null && res.hayError()) {
				System.err.println("Error");
				return false;
			} else {

				JSONObject json = new JSONObject(res.json);

				JSONArray cuentas = (JSONArray) json.get("cuentas");

				String tipo_producto = "";

				String categoria = "";

				String moneda = "";

				String descEstado = "";

				for (int i = 0; i <= 10; i++) {

					JSONObject propiedades = (JSONObject) cuentas.get(i);

					tipo_producto = propiedades.getString("tipoProducto");

					categoria = propiedades.getString("categoria");

					moneda = propiedades.getString("moneda");

					descEstado = propiedades.getString("descEstado");

					if (tipo_producto.equals("AHO") && (categoria.equals("B") || categoria.equals("K") || categoria.equals("FCL")) && moneda.equals("80") && descEstado.equals("VIGENTE")) {
						i = 20;
					}
				}

				if (tipo_producto.equals("AHO") && (categoria.equals("B") || categoria.equals("K") || categoria.equals("FCL")) && moneda.equals("80")) {
					return true;
				} else {
					return false;
				}

			}

		} catch (Exception e) {

			return false;
		}
	}

}
