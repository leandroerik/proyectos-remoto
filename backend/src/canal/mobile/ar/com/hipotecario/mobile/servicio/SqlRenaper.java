package ar.com.hipotecario.mobile.servicio;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.conector.ApiResponseMB;
import ar.com.hipotecario.mobile.conector.SqlMB;
import ar.com.hipotecario.mobile.conector.SqlRequestMB;
import ar.com.hipotecario.mobile.conector.SqlResponseMB;

public class SqlRenaper {

	public static SqlResponseMB consultaRenaperOnboarding(String cuit) {
		String base = ConfigMB.string("sql_esales_base");
		SqlRequestMB sqlRequest = SqlMB.request("SelectPersonaRenaper", "esales");
		sqlRequest.sql += "SELECT *";
		sqlRequest.sql += "FROM [" + base + "].[dbo].[Persona_Renaper] ";
		sqlRequest.sql += "WHERE cuil = ?";
		sqlRequest.parametros.add(cuit);

		return SqlMB.response(sqlRequest);
	}

	public static SqlResponseMB consultaTablaInterna(String idDispositivo, String cuit) {
		SqlResponseMB response;
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and id_dispositivo = ? and id>= (" + "select top 1 id From [Mobile].[dbo].[Renaper] where cuil = ? order by id desc) order by id desc";
		sqlRequest.parametros.add(cuit);
		sqlRequest.parametros.add(idDispositivo);
		sqlRequest.parametros.add(cuit);
		response = SqlMB.response(sqlRequest);

		if (response.registros.isEmpty()) {
			// primera vez trae el registro de onboarding
			sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and id_dispositivo = ? order by id desc";
			sqlRequest.parametros.add(cuit);
			sqlRequest.parametros.add(idDispositivo);
			response = SqlMB.response(sqlRequest);
		}
		return response;
	}

	// Este metodo sirve para obtener el registro de renaper en tabla internar
	// cuando
	// no tengo datos validados.
	public static SqlResponseMB consultaTablaInternaDatosExistentes(String idDispositivo, String cuit) {
		SqlResponseMB response;
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and id_dispositivo = ? and id >= ( " + "select top 1 id From [Mobile].[dbo].[Renaper] where cuil = ? order by id desc) order by id desc";
		sqlRequest.parametros.add(cuit);
		sqlRequest.parametros.add(idDispositivo);
		sqlRequest.parametros.add(cuit);
		response = SqlMB.response(sqlRequest);

		return response;
	}

	public static SqlResponseMB consultaTablaInterna(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT * FROM [Mobile].[dbo].[Renaper] where cuil = ?";
		sqlRequest.parametros.add(cuit);
		SqlResponseMB response = SqlMB.response(sqlRequest);

		return response;
	}

	public static SqlResponseMB consultaTablaInternaRenaper(String cuit) {
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? order by id desc";
		sqlRequest.parametros.add(cuit);
		SqlResponseMB response = SqlMB.response(sqlRequest);

		return response;
	}

	public static SqlResponseMB consultaTablaInternaRenaperValidado(String cuit) {
		SqlResponseMB response1;
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and estado in ('validado','finalizado') order by id desc";
		sqlRequest.parametros.add(cuit);
		response1 = SqlMB.response(sqlRequest);
		if (response1.registros.isEmpty()) {
			sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and estado in ('iniciado','pendiente') order by id desc";

			response1 = SqlMB.response(sqlRequest);
		}
		return response1;
	}

	// metodo para obtener el id idpositivo registrado ultimo en la tabla con estado
	// pendiente
	public static SqlResponseMB ultimoIdDispositivoPendiente(String cuit) {
		SqlResponseMB response;
		SqlRequestMB sqlRequest = SqlMB.request("SelectTablaInterna", "mobile");
		sqlRequest.sql = "SELECT top 1 * FROM [Mobile].[dbo].[Renaper] where cuil = ? and estado in ('pendiente') order by id desc";
		sqlRequest.parametros.add(cuit);
		response = SqlMB.response(sqlRequest);
		return response;
	}

	public static Boolean InsertTablaInterna(ApiResponseMB response, String estadoDeValidacion, String idDispositivoSql) {

		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO[Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(response.get("id_tramite_principal", null));
		request.add(response.string("ejemplar", null).toUpperCase());
		request.add(response.get("vencimiento", null));
		request.add(response.get("emision", null));
		request.add(response.string("apellido", null).toUpperCase());
		request.add(response.string("nombres", null).toUpperCase());
		request.add(response.get("fecha_nacimiento", null));
		request.add(response.get("cuil", null));
		request.add(response.string("calle", null).toUpperCase());
		request.add(response.get("numero", null));
		request.add(response.get("piso", null));
		request.add(response.get("departamento", null));
		request.add(response.get("codigo_postal", null));
		request.add(response.string("barrio", null).toUpperCase());
		request.add(response.get("monoblock", null));
		request.add(response.get("ciudad", null));
		request.add(response.string("municipio", null).toUpperCase());
		request.add(response.string("provincia", null).toUpperCase());
		request.add(response.string("pais", null).toUpperCase());
		request.add(idDispositivoSql);
		request.add(estadoDeValidacion);
//        se agrega este estado para diferenciar el origen de datos en la tabla
		request.add("renaper_viviendas");

		SqlResponseMB response1 = SqlMB.response(request);
		if (response1.hayError) {
			return false;
		}
		return true;
	}

	public static Boolean insertTablaInternaPendiente(SqlResponseMB datos, String idDispositivo) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(datos.registros.get(0).get("id_tramite", null));
		request.add(datos.registros.get(0).get("ejemplar", null));
		request.add(datos.registros.get(0).get("vencimiento", null));
		request.add(datos.registros.get(0).get("fecha_emision", null));
		request.add(datos.registros.get(0).get("apellido", null));
		request.add(datos.registros.get(0).get("nombre", null));
		request.add(datos.registros.get(0).get("fecha_nacimiento", null));
		request.add(datos.registros.get(0).get("cuil", null));
		request.add(datos.registros.get(0).get("calle", null));
		request.add(datos.registros.get(0).get("numero", null));
		request.add(datos.registros.get(0).string("piso", null));
		request.add(datos.registros.get(0).get("departamento", null));
		request.add(datos.registros.get(0).get("codigo_postal", null));
		request.add(datos.registros.get(0).string("barrio", null));
		request.add(datos.registros.get(0).string("monoblock", null));
		request.add(datos.registros.get(0).get("ciudad", null));
		request.add(datos.registros.get(0).get("municipio", null));
		request.add(datos.registros.get(0).get("provincia", null));
		request.add(datos.registros.get(0).get("pais", null));
		request.add(idDispositivo);
		request.add("PENDIENTE");
		request.add(("onboarding"));

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}

	public static Boolean insertTablaInterna(SqlResponseMB datos, String idDispositivo) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(datos.registros.get(0).get("id_tramite", null));
		request.add(datos.registros.get(0).get("ejemplar", null));
		request.add(datos.registros.get(0).get("vencimiento", null));
		request.add(datos.registros.get(0).get("fecha_emision", null));
		request.add(datos.registros.get(0).get("apellido", null));
		request.add(datos.registros.get(0).get("nombre", null));
		request.add(datos.registros.get(0).get("fecha_nacimiento", null));
		request.add(datos.registros.get(0).get("cuil", null));
		request.add(datos.registros.get(0).get("calle", null));
		request.add(datos.registros.get(0).get("numero", null));
		request.add(datos.registros.get(0).string("piso", null));
		request.add(datos.registros.get(0).get("departamento", null));
		request.add(datos.registros.get(0).get("codigo_postal", null));
		request.add(datos.registros.get(0).string("barrio", null));
		request.add(datos.registros.get(0).string("monoblock", null));
		request.add(datos.registros.get(0).get("ciudad", null));
		request.add(datos.registros.get(0).get("municipio", null));
		request.add(datos.registros.get(0).get("provincia", null));
		request.add(datos.registros.get(0).get("pais", null));
		request.add(idDispositivo);
		request.add("INICIADO");
		request.add(("onboarding"));

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}

	public static Boolean insertTablaInternaPrimerRegistro(SqlResponseMB datos) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		request.add(datos.registros.get(0).get("id_tramite", null));
		request.add(datos.registros.get(0).get("ejemplar", null));
		request.add(datos.registros.get(0).get("vencimiento", null));
		request.add(datos.registros.get(0).get("fecha_emision", null));
		request.add(datos.registros.get(0).get("apellido", null));
		request.add(datos.registros.get(0).get("nombre", null));
		request.add(datos.registros.get(0).get("fecha_nacimiento", null));
		request.add(datos.registros.get(0).get("cuil", null));
		request.add(datos.registros.get(0).get("calle", null));
		request.add(datos.registros.get(0).get("numero", null));
		request.add(datos.registros.get(0).string("piso", null));
		request.add(datos.registros.get(0).get("departamento", null));
		request.add(datos.registros.get(0).get("codigo_postal", null));
		request.add(datos.registros.get(0).string("barrio"));
		request.add(datos.registros.get(0).string("monoblock"));
		request.add(datos.registros.get(0).get("ciudad", null));
		request.add(datos.registros.get(0).get("municipio", null));
		request.add(datos.registros.get(0).get("provincia", null));
		request.add(datos.registros.get(0).get("pais", null));
		request.add(datos.registros.get(0).get("id_dispositivo", null));
		request.add("INICIADO");
		request.add("onboarding");

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}

	public static Boolean updateTablaInterna(Integer id, String validacion) {

		SqlRequestMB request = SqlMB.request("UpdateRegistro", "mobile");
		request.sql = "UPDATE [Mobile].dbo.[Renaper] SET [estado] = '" + validacion + "' WHERE id = ?";
		request.parametros.add(id);

		SqlResponseMB response1 = SqlMB.response(request);
		if (response1.hayError) {
			return false;
		}
		return true;
	}

	public static Boolean updateTablaInternaIdDispositivo(Integer id, String idDispositivoSql) {

		SqlRequestMB request = SqlMB.request("UpdateRegistro", "mobile");
		request.sql = "UPDATE [Mobile].dbo.[Renaper] SET [id_dispositivo] = '" + idDispositivoSql + "' WHERE id = ?";
		request.parametros.add(id);

		SqlResponseMB response1 = SqlMB.response(request);
		if (response1.hayError) {
			return false;
		}
		return true;
	}

	public static boolean validaDiferenciaDatos(SqlResponseMB renaperResponse, ApiResponseMB response) {

		try {

			if (!response.string("ejemplar").equals(renaperResponse.registros.get(0).get("ejemplar"))) {
				return true;
			}
			if (!response.string("vencimiento").substring(0, 10).equals(renaperResponse.registros.get(0).string("vencimiento").substring(0, 10))) {
				return true;
			}
			if (!response.string("emision").substring(0, 10).equals(renaperResponse.registros.get(0).string("fecha_emision").substring(0, 10))) {
				return true;
			}
			if (!response.string("apellido").equalsIgnoreCase(renaperResponse.registros.get(0).string("apellido"))) {
				return true;
			}
			if (!response.string("nombres").equalsIgnoreCase(renaperResponse.registros.get(0).string("nombre"))) {
				return true;
			}
			if (!response.string("fecha_nacimiento").substring(0, 10).equals(renaperResponse.registros.get(0).string("fecha_nacimiento").substring(0, 10))) {
				return true;
			}

			if (!response.string("calle").equalsIgnoreCase(renaperResponse.registros.get(0).string("calle"))) {
				return true;
			}
			if (!response.string("numero").equals(renaperResponse.registros.get(0).get("numero"))) {
				return true;
			}
			if (!response.string("piso").equals(renaperResponse.registros.get(0).get("piso"))) {
				return true;
			}
			if (!response.string("departamento").equals(renaperResponse.registros.get(0).get("departamento"))) {
				return true;
			}
			if (!response.string("codigo_postal").trim().equalsIgnoreCase(renaperResponse.registros.get(0).string("codigo_postal").trim())) {
				return true;
			}
			if (!response.string("barrio").equalsIgnoreCase(renaperResponse.registros.get(0).string("barrio"))) {
				return true;
			}
			if (!response.string("monoblock").equalsIgnoreCase(renaperResponse.registros.get(0).string("monoblock"))) {
				return true;
			}
			if (!response.string("ciudad").equalsIgnoreCase(renaperResponse.registros.get(0).string("ciudad"))) {
				return true;
			}
			if (!response.string("municipio").equalsIgnoreCase(renaperResponse.registros.get(0).string("municipio"))) {
				return true;
			}
			if (!response.string("provincia").equalsIgnoreCase(renaperResponse.registros.get(0).string("provincia"))) {
				return true;
			}
			if (!response.string("pais").equalsIgnoreCase(renaperResponse.registros.get(0).string("pais"))) {
				return true;
			}
		} catch (Exception e) {
			return true;
		}
		return false;
	}

	public static boolean validaDiferenciaDatosCuil(SqlResponseMB renaperResponse, ApiResponseMB response) {

		try {

			if (!response.string("cuil").equals(renaperResponse.registros.get(0).get("cuil"))) {
				return true;
			}
		} catch (Exception e) {
			return false;
		}
		return false;
	}

	public static boolean InsertTablaInterna(SqlResponseMB renaperResponse, String iniciado, String idDispositivo) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(renaperResponse.registros.get(0).get("id_tramite", null));
		request.add(renaperResponse.registros.get(0).get("ejemplar", null));
		request.add(renaperResponse.registros.get(0).get("vencimiento", null));
		request.add(renaperResponse.registros.get(0).get("fecha_emision", null));
		request.add(renaperResponse.registros.get(0).get("apellido", null));
		request.add(renaperResponse.registros.get(0).get("nombre", null));
		request.add(renaperResponse.registros.get(0).get("fecha_nacimiento", null));
		request.add(renaperResponse.registros.get(0).get("cuil", null));
		request.add(renaperResponse.registros.get(0).get("calle", null));
		request.add(renaperResponse.registros.get(0).get("numero", null));
		request.add(renaperResponse.registros.get(0).string("piso", null));
		request.add(renaperResponse.registros.get(0).get("departamento", null));
		request.add(renaperResponse.registros.get(0).get("codigo_postal", null));
		request.add(renaperResponse.registros.get(0).string("barrio"));
		request.add(renaperResponse.registros.get(0).string("monoblock"));
		request.add(renaperResponse.registros.get(0).get("ciudad", null));
		request.add(renaperResponse.registros.get(0).get("municipio", null));
		request.add(renaperResponse.registros.get(0).get("provincia", null));
		request.add(renaperResponse.registros.get(0).get("pais", null));
		request.add(idDispositivo);
		request.add("INICIADO");
		request.add("onboarding");

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}

	public static boolean InsertarTablaInterna(SqlResponseMB renaperResponse, String estado, String idDispositivo) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(renaperResponse.registros.get(0).get("id_tramite", null));
		request.add(renaperResponse.registros.get(0).get("ejemplar", null));
		request.add(renaperResponse.registros.get(0).get("vencimiento", null));
		request.add(renaperResponse.registros.get(0).get("fecha_emision", null));
		request.add(renaperResponse.registros.get(0).get("apellido", null));
		request.add(renaperResponse.registros.get(0).get("nombre", null));
		request.add(renaperResponse.registros.get(0).get("fecha_nacimiento", null));
		request.add(renaperResponse.registros.get(0).get("cuil", null));
		request.add(renaperResponse.registros.get(0).get("calle", null));
		request.add(renaperResponse.registros.get(0).get("numero", null));
		request.add(renaperResponse.registros.get(0).string("piso", null));
		request.add(renaperResponse.registros.get(0).get("departamento", null));
		request.add(renaperResponse.registros.get(0).get("codigo_postal", null));
		request.add(renaperResponse.registros.get(0).string("barrio", null));
		request.add(renaperResponse.registros.get(0).string("monoblock", null));
		request.add(renaperResponse.registros.get(0).get("ciudad", null));
		request.add(renaperResponse.registros.get(0).get("municipio", null));
		request.add(renaperResponse.registros.get(0).get("provincia", null));
		request.add(renaperResponse.registros.get(0).get("pais", null));
		request.add(idDispositivo);
		request.add(estado);
		request.add("onboarding");

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}

	public static boolean InsertarTablaInternaRenaper(SqlResponseMB renaperResponse, String estado, String idDispositivo) {
		SqlRequestMB request = SqlMB.request("InsertRegistro", "mobile");
		request.sql = "INSERT INTO [Mobile].dbo.[Renaper]([id_tramite],[ejemplar],[vencimiento],[fecha_emision],[apellido],[nombre]," + "[fecha_nacimiento],[cuil],[calle],[numero],[piso],[departamento],[codigo_postal],[barrio]," + "[monoblock],[ciudad],[municipio],[provincia],[pais],[id_dispositivo],[estado],[origen])" + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";

		request.add(renaperResponse.registros.get(0).get("id_tramite", null));
		request.add(renaperResponse.registros.get(0).get("ejemplar", null));
		request.add(renaperResponse.registros.get(0).get("vencimiento", null));
		request.add(renaperResponse.registros.get(0).get("fecha_emision", null));
		request.add(renaperResponse.registros.get(0).get("apellido", null));
		request.add(renaperResponse.registros.get(0).get("nombre", null));
		request.add(renaperResponse.registros.get(0).get("fecha_nacimiento", null));
		request.add(renaperResponse.registros.get(0).get("cuil", null));
		request.add(renaperResponse.registros.get(0).get("calle", null));
		request.add(renaperResponse.registros.get(0).get("numero", null));
		request.add(renaperResponse.registros.get(0).string("piso", null));
		request.add(renaperResponse.registros.get(0).get("departamento", null));
		request.add(renaperResponse.registros.get(0).get("codigo_postal", null));
		request.add(renaperResponse.registros.get(0).string("barrio", null));
		request.add(renaperResponse.registros.get(0).string("monoblock", null));
		request.add(renaperResponse.registros.get(0).get("ciudad", null));
		request.add(renaperResponse.registros.get(0).get("municipio", null));
		request.add(renaperResponse.registros.get(0).get("provincia", null));
		request.add(renaperResponse.registros.get(0).get("pais", null));
		request.add(idDispositivo);
		request.add(estado);
		request.add("onboarding");

		SqlResponseMB response = SqlMB.response(request);
		if (response.hayError) {
			return false;
		}
		return true;
	}
}
