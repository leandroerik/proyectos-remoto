package ar.com.hipotecario.canal.buhobank;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.ServerSentEvents;
import ar.com.hipotecario.backend.base.Encriptador;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.MODO.ApiModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.CuentaModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.TarjetaModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.FingerprintModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.GeolocalizationModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.PaymentMethodModo;
import ar.com.hipotecario.backend.servicio.api.MODO.Usuarios.CreditCardModo;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasBB.CuentaBB;
import ar.com.hipotecario.backend.servicio.api.mobile.ApiMobile;
import ar.com.hipotecario.backend.servicio.api.mobile.SoftToken;
import ar.com.hipotecario.backend.servicio.api.seguridad.ApiSeguridad;
import ar.com.hipotecario.backend.servicio.api.seguridad.UsuarioISVA;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.ApiTarjetaDebito;
import ar.com.hipotecario.backend.servicio.api.tarjetaDebito.TarjetasDebitos;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.ApiTarjetaCredito;
import ar.com.hipotecario.backend.servicio.api.tarjetasCredito.DetalleTarjetasCredito;
import ar.com.hipotecario.backend.servicio.api.tarjetascredito.ApiTarjetasCreditoBB;
import ar.com.hipotecario.backend.servicio.api.tarjetascredito.TarjetasCreditoBB;
import ar.com.hipotecario.backend.servicio.api.tarjetascredito.TarjetasCreditoBB.TarjetaCreditoBB;
import ar.com.hipotecario.backend.servicio.api.tarjetasdebito.ApiTarjetasDebitoBB;
import ar.com.hipotecario.backend.servicio.api.tarjetasdebito.TarjetasDebitoBB;
import ar.com.hipotecario.backend.servicio.api.tarjetasdebito.TarjetasDebitoBB.TarjetaDebitoBB;
import ar.com.hipotecario.backend.servicio.sql.SqlEsales;
import ar.com.hipotecario.backend.servicio.sql.SqlMobile;
import ar.com.hipotecario.backend.servicio.sql.esales.BBPersonasAlta;
import ar.com.hipotecario.backend.servicio.sql.esales.PersonasRenaper.PersonaRenaper;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales;
import ar.com.hipotecario.backend.servicio.sql.esales.SesionesEsales.SesionEsales;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class BBAltaProducFinal extends Modulo {

	public static Object altaOtrosProductos(ContextoBB contexto, ServerSentEvents<ContextoBB> sse) {
		SesionBB sesion = contexto.sesion();
		if (!EstadosBB.FINALIZAR_OK.equals(sesion.estado)) return null;

		String idDispositivo = contexto.parametros.string("id", null);
		String deviceModel = contexto.parametros.string("device_model", null);
		String osVersion = contexto.parametros.string("os_version", null);
		String hardwareId = contexto.parametros.string("hardware_id", null);
		String latitud = contexto.parametros.string("latitude", "-18.011775864280644");
		String longitud = contexto.parametros.string("longitude", "-70.24436930849632");
		String direccionIp = contexto.parametros.string("direccionIp", null);

		if(empty(latitud) || empty(latitud)){
			latitud = "-18.011775864280644";
			longitud = "-70.24436930849632";
		}

		String alias = contexto.parametros.string("alias", null);
		if(sesion.esFlujoTcv()){
			try {
				if(!empty(idDispositivo, alias)){
					String idCobis = sesion.getCobis(contexto);
					if(!idCobis.startsWith("-")){
						SqlMobile.guardarRegistroDispositivo(contexto, idCobis, idDispositivo, alias);
					}
				}
			} catch (Exception e) { }

			try {
				altaEnModo(contexto, deviceModel, osVersion, hardwareId, latitud, longitud, direccionIp);
			} catch (Exception e) {}
		}

		BBDocumentacion.guardarFormulario(contexto);
		BBDocumentacion.guardarLegajoImagenesVU(contexto);

		actualizarPersonaRenaper(contexto);
		return altaSoftToken(contexto, idDispositivo);
	}

	private static void altaEnModo(ContextoBB contexto, String deviceModel, String osVersion, String hardwareId, String latitud, String longitud, String direccionIp) {
		SesionBB sesion = contexto.sesion();
		String telefono = sesion.codArea + sesion.celular;
		String cobis = sesion.getCobis(contexto);

		//cambiar a postSimplificado
		Usuarios responsePost = ApiModo.postSimplificado(contexto, cobis, sesion.numeroDocumento, sesion.nombre, sesion.apellido, sesion.genero, telefono, sesion.mail).tryGet();
		if(responsePost == null) {
			LogBB.evento(contexto, "ERROR_ALTA_MODO", "crear usuario");
			return;
		}

		String accessToken = responsePost.access_token;

		try{
			ApiModo.insertToken(contexto, cobis, accessToken, responsePost.refresh_token, responsePost.expires_in, responsePost.token_type, telefono);
		}
		catch (Exception e){}

		List<CuentaModo> cuentaSModo =  getCuentasModo(contexto);
		if(cuentaSModo == null){
			LogBB.evento(contexto, "ERROR_ALTA_MODO",  "obtener cuentas");
			return;
		}

		boolean responseCuentas = ApiModo.postCuentas(contexto, cobis, sesion.numeroDocumento, accessToken, cuentaSModo).tryGet();
		if(!responseCuentas){
			LogBB.evento(contexto, "ERROR_ALTA_MODO", "alta cuentas");
			return;
		}

		try{
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ");
			String timeStamp = ZonedDateTime.now().format(formatter);

			FingerprintModo fingerprint = new FingerprintModo();
			fingerprint.language = "es";
			fingerprint.emulador = "false";
			fingerprint.time_stamp = timeStamp;
			fingerprint.os_name = contexto.sesion().esAndroid() ? "ANDROID" : "iOS";
			fingerprint.device_model = deviceModel;
			fingerprint.os_version = osVersion;
			fingerprint.hardware_id = hardwareId;
			fingerprint.ip = direccionIp;

			GeolocalizationModo geolocalization = new GeolocalizationModo();
			geolocalization.latitude = latitud;
			geolocalization.longitude = longitud;
			fingerprint.geolocalizationModo = geolocalization;

			altaModoTd(contexto, accessToken, fingerprint);
			altaModoTc(contexto, accessToken, fingerprint);
		}
		catch(Exception e){
			LogBB.evento(contexto, "ERROR_ALTA_MODO", "alta tarjetas");
		}
	}

	public static List<CuentaModo> getCuentasModo(ContextoBB contexto){
		try{
			List<CuentaModo> cuentasModo = new ArrayList<>();
			CuentasBB cuentas = ApiCuentasBB.get(contexto, contexto.sesion().getCobis(contexto)).tryGet();
			for (CuentaBB caja : cuentas.getCuentas()) {
				cuentasModo.add(new CuentaModo(caja.cbu));
			}
			return cuentasModo;
		}
		catch(Exception e){
			return null;
		}
	}

	public static void altaModoTd(ContextoBB contexto, String accessToken, FingerprintModo fingerprintTd){
		try{
			TarjetasDebitoBB tarjetasDebito = ApiTarjetasDebitoBB.get(contexto, contexto.sesion().getCobis(contexto)).tryGet();
			for (TarjetaDebitoBB td : tarjetasDebito) {
				CreditCardModo creditCardTd = new CreditCardModo();
				creditCardTd.first_name = contexto.sesion().nombre;
				creditCardTd.last_name = contexto.sesion().apellido;
				creditCardTd.number = td.numeroProducto;

				TarjetasDebitos tarjetaDetalle = ApiTarjetaDebito.tarjeta(contexto, td.numeroProducto).tryGet();
				creditCardTd.month = tarjetaDetalle.fechaExpiracion.mes().toString();
				creditCardTd.year = tarjetaDetalle.fechaExpiracion.año().toString();
				PaymentMethodModo paymentMethodTd = new PaymentMethodModo();
				paymentMethodTd.credit_card = creditCardTd;

				TarjetaModo tarjetaDebito = new TarjetaModo();
				tarjetaDebito.fingerprint = fingerprintTd;
				tarjetaDebito.payment_method = paymentMethodTd;
				ApiModo.postTarjeta(contexto, contexto.sesion().getCobis(contexto), contexto.sesion().numeroDocumento, accessToken, tarjetaDebito).tryGet();
			}

		} catch(Exception e){ }
	}

	public static void altaModoTc(ContextoBB contexto, String accessToken, FingerprintModo fingerprintTd){
		try{
			TarjetasCreditoBB tarjetasCredito = ApiTarjetasCreditoBB.get(contexto, contexto.sesion().getCobis(contexto)).tryGet();
			for (TarjetaCreditoBB tc : tarjetasCredito) {
				CreditCardModo creditCardTd = new CreditCardModo();
				creditCardTd.first_name = contexto.sesion().nombre;
				creditCardTd.last_name = contexto.sesion().apellido;
				creditCardTd.number = tc.numero;

				DetalleTarjetasCredito tarjetaDetalle = ApiTarjetaCredito.tarjeta(contexto, tc.numero).tryGet();
				if(tarjetaDetalle != null && !tarjetaDetalle.isEmpty()){
					creditCardTd.month = tarjetaDetalle.get(0).vigenciaHasta.mes().toString();
					creditCardTd.year = tarjetaDetalle.get(0).vigenciaHasta.año().toString();
					PaymentMethodModo paymentMethodTd = new PaymentMethodModo();
					paymentMethodTd.credit_card = creditCardTd;

					TarjetaModo tarjetacredito = new TarjetaModo();
					tarjetacredito.fingerprint = fingerprintTd;
					tarjetacredito.payment_method = paymentMethodTd;
					ApiModo.postTarjeta(contexto, contexto.sesion().getCobis(contexto), contexto.sesion().numeroDocumento, accessToken, tarjetacredito).tryGet();
				}
			}
		} catch(Exception e){ }
	}

	public static void actualizarPersonaRenaper(ContextoBB contexto) {
		try {
			PersonaRenaper persona = SqlEsales.get(contexto, contexto.sesion().cuil).get();
			persona.estado = EstadosBB.FINALIZAR_OK;
			SqlEsales.update(contexto, persona).get();
		} catch (Exception e) {
			System.out.println("No se pudo actualizar PersonaRenaper pero continúa el flujo");
		}
	}

	public static Objeto generarIDCliente(ContextoBB contexto) {
		SesionBB sesion = contexto.sesion();
		String idCobis = sesion.getCobis(contexto);
		if(idCobis.startsWith("-")){
			return respuesta("SIN_PSEUDO_SESION");
		}

		return respuesta("ID", Encriptador.sha256(idCobis));
	}

	public static Objeto altaSoftToken(ContextoBB contexto, String idDispositivo) {
		SesionBB sesion = contexto.sesion();
		String idCobis = sesion.getCobis(contexto);
		if(idCobis.startsWith("-")){
			LogBB.error(contexto, EstadosBB.ETAPA_CREAR_STOKEN, "sin cobis positivo");
			return respuesta(ErroresBB.ERROR_PERSONA);
		}

		if (empty(idDispositivo)) {
			idDispositivo = Encriptador.sha256(idCobis) + (empty(sesion.idDispositivo) ? "" : sesion.idDispositivo);
		}
		
		sesion.idDispositivo = idDispositivo;
		sesion.saveSesionbb2();	

		UsuarioISVA usuarioISVA = ApiSeguridad.usuarioISVA(contexto, idCobis, "ClientesBH").get();
		if (usuarioISVA == null || empty(usuarioISVA.idISVA)) {
			usuarioISVA = ApiSeguridad.postUsuarioISVA(contexto, idCobis, "").tryGet();
		}

		SoftToken sToken = ApiMobile.crear(contexto, true, idCobis, idDispositivo).tryGet();
		if (sToken == null || !sToken.estado.equals("0")) {
			LogBB.error(contexto, EstadosBB.ETAPA_CREAR_STOKEN, ErroresBB.ERROR_API + "idDispositivoEnc: " + idDispositivo);
			return respuesta(ErroresBB.ERROR_API);
		}

		SesionEsales sesionEsales = SesionesEsales.getPorToken(contexto, sesion.token);
		if (empty(sesionEsales)) {
			LogBB.error(contexto, EstadosBB.ETAPA_CREAR_STOKEN, ErroresBB.SIN_REGISTROS_DE_SESION);
			return respuesta(ErroresBB.SIN_REGISTROS_DE_SESION);
		}

		sToken.cuil = sesion.cuil;
		sToken.id_dispositivo = idDispositivo;

		try {
			BBPersonasAlta.crearPersonasAlta(contexto, sesionEsales.id, sesion, sToken);
		} catch (Exception e) {
			LogBB.error(contexto, EstadosBB.ETAPA_GUARDAR_STOKEN, ErroresBB.ERROR_GUARDAR_ST);
		}
		
		LogBB.evento(contexto, EstadosBB.ETAPA_CREAR_STOKEN, EstadosBB.CREAR_ST_OK);

		Objeto respuesta = respuesta();
		respuesta.set("algoritmo", sToken.algoritmo);
		respuesta.set("digitos", sToken.digitos);
		respuesta.set("periodo", sToken.periodo);
		respuesta.set("claveSecreta", sToken.claveSecreta);
		respuesta.set("idDispositivo", idDispositivo);
		return respuesta;
	}

}
