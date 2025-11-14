package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.ContextoMB;
import ar.com.hipotecario.mobile.api.MBAplicacion;
import ar.com.hipotecario.mobile.api.MBPersona;
import ar.com.hipotecario.mobile.api.MBSeguridad;
import ar.com.hipotecario.mobile.api.MBTransferencia;
import ar.com.hipotecario.mobile.lib.Objeto;

public class OtpDatavalidTest {

	public static void main(String[] args) {
		try {
			//boolean test1 = validaContactoTest();
			//boolean test2 = modificaEmailTest();
			boolean test3 = validarDomicilioTest();
			//validarPersonaErrorTest();

			//System.out.println("PRUEBA 1: validaContactoTest" + (test1 ? "OK" : "ERROR"));
			//System.out.println("PRUEBA 2: modificaEmailTest " + (test2 ? "OK" : "ERROR"));
			System.out.println("PRUEBA 3: validarDomicilioTest " + (test3 ? "OK" : "ERROR"));

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//valida otp de un dato de contacto que no quiere modificarlo
	private static boolean validaContactoTest() {
		try {
			String idCobis = "803";
			String email = "pepehomo@hipotecario.com";
			String telefono = "1123114488";

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			Objeto resDatavalid = MBPersona.dataValidOtp(contexto);
			if(!"0".equals(resDatavalid.string("estado"))
					&& !"VENCIDO".equals(resDatavalid.string("estado"))){
				System.out.println("1. (get-datavalid) ERROR RESPUESTA");
				return false;
			}

			if("0".equals(resDatavalid.string("estado"))){
				System.out.println("1. (get-datavalid) NO TIENE DATOS VENCIDOS");
				return true;
			}

			if(resDatavalid.bool("emailVencido", false)){
				System.out.println("1. (get-datavalid) EMAIL VENCIDO");
			}

			if(resDatavalid.bool("telefonoVencido", false)){
				System.out.println("1. (get-datavalid) TELEFONO VENCIDO");
			}

			if(resDatavalid.bool("emailVencido", false)){
				contexto.parametros.set("idCanal", "DATAVALID_OTP");
				contexto.parametros.set("email", email);
				Objeto resPedirOtpEmail = MBSeguridad.pedirOTP(contexto);
				contexto.parametros.set("idCanal", null);
				contexto.parametros.set("email", null);

				if(!"0".equals(resPedirOtpEmail.string("estado"))){
					System.out.println("2. (pedir otp email) ERROR PEDIR OTP EMAIL, estado: " + resPedirOtpEmail.string("estado"));
					return false;
				}

				if(!email.equals(contexto.sesion().getEmailOtpDatavalid())){
					System.out.println("2. (pedir otp email) ERROR EMAIL OTP NO GUARDADO");
					return false;
				}

				if(!"email".equals(contexto.sesion().validadorPedido())){
					System.out.println("2. (pedir otp email) ERROR VALIDADOR EMAIL NO GUARDADO");
					return false;
				}

				if(resPedirOtpEmail.string("otp").isEmpty()){
					System.out.println("2. (pedir otp email) NO SE DEVOLVIO EL OTP EMAIL");
					return false;
				}

				contexto.parametros.set("otp", "000000");
				Objeto resValidarOtpEmail = MBSeguridad.validarOTP(contexto);
				contexto.parametros.set("otp", null);

				if(!"0".equals(resValidarOtpEmail.string("estado"))){
					System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL, estado: " + resValidarOtpEmail.string("estado"));
					return false;
				}

				if(!contexto.sesion().getEmailOtpDatavalid().equals("VALIDADO_" + email)){
					System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL NO GUARDADO");
					return false;
				}

				if(contexto.sesion().validaSegundoFactorOtp()){
					System.out.println("3. (validar otp email) ERROR SEGUNDO FACTOR ESTA ACTIVO");
					return false;
				}
			}

			if(resDatavalid.bool("telefonoVencido", false)){
				contexto.parametros.set("idCanal", "DATAVALID_OTP");
				contexto.parametros.set("telefono", telefono);
				Objeto resPedirOtptelefono = MBSeguridad.pedirOTP(contexto);
				contexto.parametros.set("idCanal", null);
				contexto.parametros.set("telefono", null);

				if(!"0".equals(resPedirOtptelefono.string("estado"))){
					System.out.println("2. (pedir otp telefono) ERROR PEDIR OTP TELEFONO, estado: " + resPedirOtptelefono.string("estado"));
					return false;
				}

				if(!telefono.equals(contexto.sesion().getTelefonoOtpDatavalid())){
					System.out.println("2. (pedir otp telefono) ERROR TELEFONO OTP NO GUARDADO: " + contexto.sesion().getTelefonoOtpDatavalid());
					return false;
				}

				if(!"sms".equals(contexto.sesion().validadorPedido())){
					System.out.println("2. (pedir otp telefono) ERROR VALIDADOR TELEFONO NO GUARDADO");
					return false;
				}

				if(resPedirOtptelefono.string("otp").isEmpty()){
					System.out.println("2. (pedir otp telefono) NO SE DEVOLVIO EL OTP TELEFONO");
					return false;
				}

				contexto.parametros.set("otp", "000000");
				Objeto resValidarOtpTelefono = MBSeguridad.validarOTP(contexto);
				contexto.parametros.set("otp", null);

				if(!"0".equals(resValidarOtpTelefono.string("estado"))){
					System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO, estado: " + resValidarOtpTelefono.string("estado"));
					return false;
				}

				if(!contexto.sesion().getTelefonoOtpDatavalid().equals("VALIDADO_" + telefono)){
					System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO NO GUARDADO");
					return false;
				}

				if(contexto.sesion().validaSegundoFactorOtp()){
					System.out.println("3. (validar otp telefono) ERROR SEGUNDO FACTOR ESTA ACTIVO");
					return false;
				}
			}

			contexto.parametros.set("validarDatos", "true");
			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("validarDatos", null);

			if(!"0".equals(resActualizarPersona.string("estado"))){
				System.out.println("**ERROR ACTUALIZAR PERSONA: " + resActualizarPersona.string("estado"));
				return false;
			}

			if(!contexto.persona().email().equals(email)){
				System.out.println("**ERROR NO SE ACTUALIZO EMAIL");
				return false;
			}

			if(!contexto.persona().celular().equals(telefono)){
				System.out.println("**ERROR NO SE ACTUALIZO TELEFONO: " + contexto.persona().celular());
				return false;
			}

			Objeto resDatavalidResultado = MBPersona.dataValidOtp(contexto);
			if(!"0".equals(resDatavalidResultado.string("estado"))
					&& !"VENCIDO".equals(resDatavalidResultado.string("estado"))){
				System.out.println("1. (get-datavalid) ERROR RESPUESTA");
				return false;
			}

			if(resDatavalidResultado.bool("emailVencido", false)){
				System.out.println("EMAIL VENCIDO");
				return false;
			}

			if(resDatavalidResultado.bool("telefonoVencido", false)){
				System.out.println("TELEFONO VENCIDO");
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static boolean modificarContactoTest() {
		try {
			String idCobis = "9040571";
			String email = "nuevocorreo@hipotecario.com";
			String codigoArea = "011";
			String caracteristica = "2311";
			String numero = "3388";
			String telefono = codigoArea + caracteristica + numero;
			String telefonoAux = telefono.startsWith("0") ? telefono.substring(1) : telefono;
			boolean modificoEmail = false;
			boolean modificoTelefono = false;

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");
			Objeto resDatavalid = MBPersona.dataValidOtp(contexto);
			if(!"0".equals(resDatavalid.string("estado"))
					&& !"VENCIDO".equals(resDatavalid.string("estado"))){
				System.out.println("1. (get-datavalid) ERROR RESPUESTA");
				return false;
			}

			if("0".equals(resDatavalid.string("estado"))){
				System.out.println("1. (get-datavalid) NO TIENE DATOS VENCIDOS");
				return true;
			}

			if(resDatavalid.bool("emailVencido", false)){
				System.out.println("1. (get-datavalid) EMAIL VENCIDO");
			}

			if(resDatavalid.bool("telefonoVencido", false)){
				System.out.println("1. (get-datavalid) TELEFONO VENCIDO");
			}

			if(resDatavalid.bool("emailVencido", false)){
				contexto.parametros.set("idCanal", "DATAVALID_OTP");
				contexto.parametros.set("email", email);
				Objeto resPedirOtpEmail = MBSeguridad.pedirOTP(contexto);
				contexto.parametros.set("idCanal", null);
				contexto.parametros.set("email", null);

				if(!"0".equals(resPedirOtpEmail.string("estado"))){
					System.out.println("2. (pedir otp email) ERROR PEDIR OTP EMAIL, estado: " + resPedirOtpEmail.string("estado"));
					return false;
				}

				if(!email.equals(contexto.sesion().getEmailOtpDatavalid())){
					System.out.println("2. (pedir otp email) ERROR EMAIL OTP NO GUARDADO");
					return false;
				}

				if(!"email".equals(contexto.sesion().validadorPedido())){
					System.out.println("2. (pedir otp email) ERROR VALIDADOR EMAIL NO GUARDADO");
					return false;
				}

				if(resPedirOtpEmail.string("otp").isEmpty()){
					System.out.println("2. (pedir otp email) NO SE DEVOLVIO EL OTP EMAIL");
					return false;
				}

				contexto.parametros.set("otp", "000000");
				Objeto resValidarOtpEmail = MBSeguridad.validarOTP(contexto);
				contexto.parametros.set("otp", null);

				if(!"0".equals(resValidarOtpEmail.string("estado"))){
					System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL, estado: " + resValidarOtpEmail.string("estado"));
					return false;
				}

				if(!contexto.sesion().getEmailOtpDatavalid().equals("VALIDADO_" + email)){
					System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL NO GUARDADO");
					return false;
				}

				if(contexto.sesion().validaSegundoFactorOtp()){
					System.out.println("3. (validar otp email) ERROR SEGUNDO FACTOR ESTA ACTIVO");
					return false;
				}

				modificoEmail = true;
			}

			if(resDatavalid.bool("telefonoVencido", false)){
				contexto.parametros.set("idCanal", "DATAVALID_OTP");
				contexto.parametros.set("telefono", telefono);
				Objeto resPedirOtptelefono = MBSeguridad.pedirOTP(contexto);
				contexto.parametros.set("idCanal", null);
				contexto.parametros.set("telefono", null);

				if(!"0".equals(resPedirOtptelefono.string("estado"))){
					System.out.println("2. (pedir otp telefono) ERROR PEDIR OTP TELEFONO, estado: " + resPedirOtptelefono.string("estado"));
					return false;
				}

				if(!telefonoAux.equals(contexto.sesion().getTelefonoOtpDatavalid())){
					System.out.println("2. (pedir otp telefono) ERROR TELEFONO OTP NO GUARDADO: " + contexto.sesion().getTelefonoOtpDatavalid());
					return false;
				}

				if(!"sms".equals(contexto.sesion().validadorPedido())){
					System.out.println("2. (pedir otp telefono) ERROR VALIDADOR TELEFONO NO GUARDADO");
					return false;
				}

				if(resPedirOtptelefono.string("otp").isEmpty()){
					System.out.println("2. (pedir otp telefono) NO SE DEVOLVIO EL OTP TELEFONO");
					return false;
				}

				contexto.parametros.set("otp", "000000");
				Objeto resValidarOtpTelefono = MBSeguridad.validarOTP(contexto);
				contexto.parametros.set("otp", null);

				if(!"0".equals(resValidarOtpTelefono.string("estado"))){
					System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO, estado: " + resValidarOtpTelefono.string("estado"));
					return false;
				}

				if(!contexto.sesion().getTelefonoOtpDatavalid().equals("VALIDADO_" + telefonoAux)){
					System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO NO GUARDADO");
					return false;
				}

				if(contexto.sesion().validaSegundoFactorOtp()){
					System.out.println("3. (validar otp telefono) ERROR SEGUNDO FACTOR ESTA ACTIVO");
					return false;
				}

				modificoTelefono = true;
			}

			if(modificoEmail){
				contexto.parametros.set("actualizarEmail", "true");
				contexto.parametros.set("email", email);
			}
			if(modificoTelefono){
				contexto.parametros.set("actualizarCelular", "true");
				contexto.parametros.set("celular.codigoArea", codigoArea);
				contexto.parametros.set("celular.caracteristica", caracteristica);
				contexto.parametros.set("celular.numero", numero);
			}

			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("actualizarEmail", null);
			contexto.parametros.set("email", null);
			contexto.parametros.set("actualizarCelular", null);
			contexto.parametros.set("celular.codigoArea", null);
			contexto.parametros.set("celular.caracteristica", null);
			contexto.parametros.set("celular.numero", null);

			if(!"0".equals(resActualizarPersona.string("estado"))){
				System.out.println("**ERROR ACTUALIZAR PERSONA: " + resActualizarPersona.string("estado"));
				return false;
			}

			if(!contexto.persona().email().equals(email)){
				System.out.println("**ERROR NO SE ACTUALIZO EMAIL");
				return false;
			}

			if(!contexto.persona().celular().contains(telefonoAux)){
				System.out.println("**ERROR NO SE ACTUALIZO TELEFONO: " + contexto.persona().celular());
				return false;
			}

			Objeto resDatavalidResultado = MBPersona.dataValidOtp(contexto);
			if(!"0".equals(resDatavalidResultado.string("estado"))
					&& !"VENCIDO".equals(resDatavalid.string("estado"))){
				System.out.println("1. (get-datavalid) ERROR RESPUESTA");
				return false;
			}

			if(resDatavalidResultado.bool("emailVencido", false)){
				System.out.println("EMAIL VENCIDO");
				return false;
			}

			if(resDatavalidResultado.bool("telefonoVencido", false)){
				System.out.println("TELEFONO VENCIDO");
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static boolean modificarTelefonoTest() {
		try {
			String idCobis = "803";
			String codigoArea = "011";
			String caracteristica = "2311";
			String numero = "4488";
			String telefono = codigoArea + caracteristica + numero;
			String telefonoAux = telefono.startsWith("0") ? telefono.substring(1) : telefono;

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");

			contexto.parametros.set("idCanal", "DATAVALID_OTP");
			contexto.parametros.set("telefono", telefono);
			Objeto resPedirOtptelefono = MBSeguridad.pedirOTP(contexto);
			contexto.parametros.set("idCanal", null);
			contexto.parametros.set("telefono", null);

			if(!"0".equals(resPedirOtptelefono.string("estado"))){
				System.out.println("2. (pedir otp telefono) ERROR PEDIR OTP TELEFONO, estado: " + resPedirOtptelefono.string("estado"));
				return false;
			}

			if(!telefonoAux.equals(contexto.sesion().getTelefonoOtpDatavalid())){
				System.out.println("2. (pedir otp telefono) ERROR TELEFONO OTP NO GUARDADO: " + contexto.sesion().getTelefonoOtpDatavalid());
				return false;
			}

			if(!"sms".equals(contexto.sesion().validadorPedido())){
				System.out.println("2. (pedir otp telefono) ERROR VALIDADOR TELEFONO NO GUARDADO");
				return false;
			}

			if(resPedirOtptelefono.string("otp").isEmpty()){
				System.out.println("2. (pedir otp telefono) NO SE DEVOLVIO EL OTP TELEFONO");
				return false;
			}

			contexto.parametros.set("otp", "000000");
			Objeto resValidarOtpTelefono = MBSeguridad.validarOTP(contexto);
			contexto.parametros.set("otp", null);

			if(!"0".equals(resValidarOtpTelefono.string("estado"))){
				System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO, estado: " + resValidarOtpTelefono.string("estado"));
				return false;
			}

			if(!contexto.sesion().getTelefonoOtpDatavalid().equals("VALIDADO_" + telefonoAux)){
				System.out.println("3. (validar otp telefono) ERROR VALIDAR OTP TELEFONO NO GUARDADO");
				return false;
			}

			if(contexto.sesion().validaSegundoFactorOtp()){
				System.out.println("3. (validar otp telefono) ERROR SEGUNDO FACTOR ESTA ACTIVO");
				return false;
			}

			contexto.parametros.set("actualizarCelular", "true");
			contexto.parametros.set("celular.codigoArea", codigoArea);
			contexto.parametros.set("celular.caracteristica", caracteristica);
			contexto.parametros.set("celular.numero", numero);
			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("actualizarCelular", null);
			contexto.parametros.set("celular.codigoArea", null);
			contexto.parametros.set("celular.caracteristica", null);
			contexto.parametros.set("celular.numero", null);

			if(!"0".equals(resActualizarPersona.string("estado"))){
				System.out.println("**ERROR ACTUALIZAR PERSONA: " + resActualizarPersona.string("estado"));
				return false;
			}

			if(!contexto.persona().celular().contains(telefonoAux)){
				System.out.println("**ERROR NO SE ACTUALIZO TELEFONO: " + contexto.persona().celular());
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	private static boolean modificaEmailTest() { //sin email vencido
		try {
			String idCobis = "803";
			//String email = "pepehomologacionit4@hipotecario.com";
			String email = "pepehomo@hipotecario.com";

			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");

			contexto.parametros.set("idCanal", "DATAVALID_OTP");
			contexto.parametros.set("email", email);
			Objeto resPedirOtpEmail = MBSeguridad.pedirOTP(contexto);
			contexto.parametros.set("idCanal", null);
			contexto.parametros.set("email", null);

			if(!"0".equals(resPedirOtpEmail.string("estado"))){
				System.out.println("2. (pedir otp email) ERROR PEDIR OTP EMAIL, estado: " + resPedirOtpEmail.string("estado"));
				return false;
			}

			if(!email.equals(contexto.sesion().getEmailOtpDatavalid())){
				System.out.println("2. (pedir otp email) ERROR EMAIL OTP NO GUARDADO");
				return false;
			}

			if(!"email".equals(contexto.sesion().validadorPedido())){
				System.out.println("2. (pedir otp email) ERROR VALIDADOR EMAIL NO GUARDADO");
				return false;
			}

			if(resPedirOtpEmail.string("otp").isEmpty()){
				System.out.println("2. (pedir otp email) NO SE DEVOLVIO EL OTP EMAIL");
				return false;
			}

			contexto.parametros.set("otp", "000000");
			Objeto resValidarOtpEmail = MBSeguridad.validarOTP(contexto);
			contexto.parametros.set("otp", null);

			if(!"0".equals(resValidarOtpEmail.string("estado"))){
				System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL, estado: " + resValidarOtpEmail.string("estado"));
				return false;
			}

			if(!contexto.sesion().getEmailOtpDatavalid().equals("VALIDADO_" + email)){
				System.out.println("3. (validar otp email) ERROR VALIDAR OTP EMAIL NO GUARDADO");
				return false;
			}

			if(contexto.sesion().validaSegundoFactorOtp()){
				System.out.println("3. (validar otp email) ERROR SEGUNDO FACTOR ESTA ACTIVO");
				return false;
			}

			contexto.parametros.set("actualizarEmail", "true");
			contexto.parametros.set("email", email);
			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("actualizarEmail", null);
			contexto.parametros.set("email", null);

			if(!"0".equals(resActualizarPersona.string("estado"))){
				System.out.println("**ERROR ACTUALIZAR PERSONA: " + resActualizarPersona.string("estado"));
				return false;
			}

			if(!contexto.persona().email().equals(email)){
				System.out.println("**ERROR NO SE ACTUALIZO EMAIL");
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

	//si tiene el email o telefono vencido no puede validar los datos personales debe validar otp del contacto vencido
	private static void validarPersonaErrorTest() {
		try{

			String idCobis = "803";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");

			contexto.parametros.set("validarDatos", "true");
			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("validarDatos", null);

			if("REQUIERE_SEGUNDO_FACTOR".equals(resActualizarPersona.string("estado"))){
				System.out.println("**SE VALIDA QUE TENGA VALIDACION POR OTP -OK");
			}
			else{
				System.out.println("**ERROR NO SE VALIDA QUE TENGA OTP");
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean validarDomicilioTest() { //tiene que validar domicilio y tiene datos contacto vigente
		try{

			String idCobis = "803";
			ContextoMB contexto = new ContextoMB(idCobis, "1", "127.0.0.1");

			contexto.parametros.set("validarDatos", "true");
			Objeto resActualizarPersona = MBPersona.actualizarDatosPersonales(contexto);
			contexto.parametros.set("validarDatos", null);

			if("REQUIERE_SEGUNDO_FACTOR".equals(resActualizarPersona.string("estado"))){
				System.out.println("**PIDE SEGUNDO FACTOR");
				return false;
			}

			return true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
