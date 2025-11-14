package ar.com.hipotecario.canal.homebanking.servicio;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiRequest;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.conector.Sql;
import ar.com.hipotecario.canal.homebanking.conector.SqlRequest;
import ar.com.hipotecario.canal.homebanking.conector.SqlResponse;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;

public class RestSeguridad {

	/* ========== SERVICIOS ========== */
	public static ApiResponse usuario(ContextoHB contexto, Boolean crearUsuario) {
		ApiResponse response = usuario(contexto);
		if (crearUsuario && (response.codigo == 204 || "USER_NOT_EXIST".equals(response.string("codigo")) || "404".equals(response.string("codigo")))) {
			ApiResponse responseCrearUsuario = crearUsuario(contexto, contexto.persona().nombreCompleto());
			response = responseCrearUsuario.hayError() ? responseCrearUsuario : usuario(contexto);
		}
		return response;
	}

	public static ApiResponse usuario(ContextoHB contexto) {
		ApiRequest request = Api.request("UsuarioIDG", "seguridad", "GET", "/v1/usuario", contexto);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.permitirSinLogin = true;
		ApiResponse response = Api.response(request, contexto.idCobis());
		if (response.codigo == 204 || "USER_NOT_EXIST".equals(response.string("codigo")) || "404".equals(response.string("codigo"))) {
			response.codigo = 204;
		}
		return response;
	}

	public static ApiResponse crearUsuario(ContextoHB contexto, String nombreCompleto) {
		ApiRequest request = Api.request("CrearUsuarioIDG", "seguridad", "POST", "/v1/usuario", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idcliente", contexto.idCobis());
		request.body("nombreCompleto", nombreCompleto);
		request.body("comentarios", "");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse validarUsuario(ContextoHB contexto, String usuario, String fingerprint) {
		ApiRequest request = Api.request("ValidarUsuarioIDG", "seguridad", "GET", "/v1/clave", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", usuario);
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse validarClave(ContextoHB contexto, String clave, String fingerprint) {
		ApiRequest request = Api.request("ValidarClaveIDG", "seguridad", "GET", "/v1/clave", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", clave);
		request.query("nombreClave", "numerica");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse validarClavev2(ContextoHB contexto, String clave, String fingerprint) {
		ApiRequest request = Api.request("ValidarClaveCanalIDG", "seguridad", "GET", "/v1/clave/canal", contexto);
		request.header("x-fingerprint", fingerprint);
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		request.query("clave", clave);
		request.query("nombreClave", "numerica");
		request.permitirSinLogin = true;
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse cambiarUsuario(ContextoHB contexto, String usuario, boolean permitirSinLogin) {
		// SeguridadPostCambiarUsuario
		ApiRequest request = Api.request("CambiarUsuario", "seguridad", "PUT", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", contexto.idCobis());
		request.body("parametros").set("clave", usuario);
		request.permitirSinLogin = permitirSinLogin;

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse cambiarClave(ContextoHB contexto, String clave, boolean permitirSinLogin) {
		ApiRequest request = Api.request("CambiarClave", "seguridad", "PUT", "/v1/clave", contexto);
		request.body("grupo", "ClientesBH");
		request.body("idUsuario", contexto.idCobis());
		request.body("nombreClave", "numerica");
		request.body("parametros").set("clave", clave);
		request.permitirSinLogin = permitirSinLogin;

		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse consultaPreguntasPorCliente(ContextoHB contexto, Integer cantidad) {
		ApiRequest request = Api.request("ConsultaPreguntasPorCliente", "seguridad", "GET", "/v1/preguntas/desafio", contexto);
		request.query("cantidad", cantidad.toString());
		request.query("grupo", "ClientesBH");
		request.query("idcliente", contexto.idCobis());
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse historialActividades(ContextoHB contexto, String actividad) {
		ApiRequest request = Api.request("SeguridadHistorialActividades", "seguridad", "GET", "/v1/clientes/{idCliente}/auditorias", contexto);
		request.path("idCliente", contexto.idCobis());
		request.query("actividad", actividad);
		return Api.response(request, contexto.idCobis());
	}

	public static ApiResponse usuarioGirePost(ContextoHB contexto) throws Exception {
		ApiRequest request = Api.request("SeguridadGireUsuarioPost", "seguridad", "POST", "/v1/usuariogire", contexto);

		Objeto usuario = new Objeto();
		usuario.set("perfilrd", "A");
		usuario.set("nombre", contexto.persona().nombreCompleto());
		usuario.set("officeBankingId", contexto.persona().cuitFormateado());
		usuario.set("rolMobile", "S");
		request.body.add("usuarios", usuario);

		Objeto empresa = new Objeto();
		empresa.set("cuit", contexto.persona().cuitFormateado());
		empresa.set("razonSocial", contexto.persona().nombreCompleto());
		empresa.set("horarioCorte", "16:00:00");
		empresa.set("limiteCheques", "0");
		empresa.add("tiposProcesos", new Objeto().set("tipo", "1"));
		// empresa.add("tiposProcesos", new Objeto().set("tipo","2"));

		Integer contador = 0;
		for (Cuenta cuenta : contexto.cuentas()) {
			if (cuenta.esDolares()) {
				continue;
			}
			// cuentaUsuario
			Objeto cuentaUsuario = new Objeto();
			cuentaUsuario.set("numero", cuenta.numero());
			cuentaUsuario.set("officeBankingId", contexto.persona().cuitFormateado());
			request.body.add("cuentaUsuarios", cuentaUsuario);

			// cuentaEmpresa
			Objeto cuentaEmpresa = new Objeto();
			cuentaEmpresa.set("numero", cuenta.numero());
			empresa.add("cuentas", cuentaEmpresa);

			// empresa
			// contador ++;
			Objeto limiteCuenta = new Objeto();

			limiteCuenta.set("cuentaBase", "0");
			limiteCuenta.set("cuentaNumero", cuenta.numero());
			limiteCuenta.set("importeMaximo", "0");
			// limiteCuenta.set("lid", contador);
			limiteCuenta.set("limiteCheques", "0");
			limiteCuenta.set("limiteDias", "0");
			limiteCuenta.set("procTipo", "1");
			limiteCuenta.set("sucursalCuenta", cuenta.sucursal());

			boolean sucursalAgregada = false;
			for (Objeto localizacionAux : empresa.objetos("localizaciones")) {
				if (cuenta.sucursal().equals(localizacionAux.string("sucradicacion"))) {
					limiteCuenta.set("lid", localizacionAux.string("lid"));
					sucursalAgregada = true;
				}
			}

			empresa.add("limiteCuentas", limiteCuenta);

			if (!sucursalAgregada) {
				contador++;
				limiteCuenta.set("lid", contador);
				Objeto localizacion = new Objeto();
				localizacion.set("importeMaximo", "0");
				localizacion.set("lid", contador);
				localizacion.set("limiteDias", "0");
				ApiResponse responseSucursal = RestCatalogo.sucursalPorCodigo(contexto, cuenta.sucursal());
				if (responseSucursal.hayError()) {
					throw new Exception("ERROR_SUCURSAL");
				}
				String descripcionSucursal = "";
				if (responseSucursal.objetos() != null && !responseSucursal.objetos().isEmpty()) {
					descripcionSucursal = responseSucursal.objetos().get(0).string("DesSucursal");
				}
				localizacion.set("nombre", descripcionSucursal);
				localizacion.set("sucradicacion", cuenta.sucursal());
				empresa.add("localizaciones", localizacion);
			}
		}
		empresa.set("responsabilidadEndoso", "N");
		request.body("empresa", empresa);

		String sucursalLocalizacionUsuario = "1";
		Objeto localizacionesUsuarios = new Objeto();

		// De las sucursales de las cuentas del usuario, tomo la que es igual a la que
		// tiene configura en Api Persona.
		// Si no hay ninguna dejo el "1"
		if (!"".equals(contexto.persona().sucursal())) {
			for (Objeto localizacionAux : empresa.objetos("localizaciones")) {
				if (contexto.persona().sucursal().equals(localizacionAux.string("sucradicacion"))) {
					sucursalLocalizacionUsuario = localizacionAux.string("lid");
				}
			}
		}

		localizacionesUsuarios.add(new Objeto().set("lid", sucursalLocalizacionUsuario).set("officeBankingId", contexto.persona().cuitFormateado()));
		request.body("localizacionesUsuarios", localizacionesUsuarios);

		return Api.response(request);
	}

	public static SqlResponse usuarioGireSql(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("SelectGireUsuario", "homebanking");
		sqlRequest.sql = "select * from [homebanking].[dbo].[gire_usuario] where id_cobis =  ?";
		sqlRequest.parametros.add(contexto.idCobis());
		return Sql.response(sqlRequest);
	}

	public static SqlResponse insertarGireUsuarioSql(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("InsertGireUsuario", "homebanking");
		sqlRequest.sql = "insert into [homebanking].[dbo].[gire_usuario](id_cobis, fecha_alta, fecha_modificacion) values (?, GETDATE(), GETDATE()) ";
		sqlRequest.parametros.add(contexto.idCobis());
		return Sql.response(sqlRequest);
	}

	public static SqlResponse updateGireUsuarioSql(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("UpdateGireUsuario", "homebanking");
		sqlRequest.sql = "update [homebanking].[dbo].[gire_usuario] set fecha_modificacion = GETDATE() where id_cobis = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		return Sql.response(sqlRequest);
	}

	public static SqlResponse updateCuentasGireUsuarioSql(ContextoHB contexto) {
		SqlRequest sqlRequest = Sql.request("DeleteGireUsuarioCuentas", "homebanking");
		sqlRequest.sql = "delete from [homebanking].[dbo].[gire_usuario_cuentas] where id_cobis = ?";
		sqlRequest.parametros.add(contexto.idCobis());
		SqlResponse sqlResponse = Sql.response(sqlRequest);
		if (sqlResponse.hayError) {
			return sqlResponse;
		}

		for (Cuenta cuenta : contexto.cuentas()) {
			if (cuenta.esDolares()) {
				continue;
			}
			sqlRequest = Sql.request("InsertGireUsuarioCuenta", "homebanking");
			sqlRequest.sql = "insert into [homebanking].[dbo].[gire_usuario_cuentas](id_cobis, id_producto, numero) values (?, ?, ?)";
			sqlRequest.parametros.add(contexto.idCobis());
			sqlRequest.parametros.add(cuenta.id());
			sqlRequest.parametros.add(cuenta.numero());
			sqlResponse = Sql.response(sqlRequest);
			if (sqlResponse.hayError) {
				return sqlResponse;
			}
		}

		return sqlResponse;
	}
}
