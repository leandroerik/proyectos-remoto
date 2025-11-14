package ar.com.hipotecario.canal.officebanking;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Optional;

import org.json.JSONObject;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.libreriasecurity.application.service.OfficeBankingUseCaseService;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.AdditionalData;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.BodyForCsm;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.BodyForLecturaCsmIdAuth;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.BodyForVerificacionDNI;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.BodyForMigrationMay;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.Context;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.DataBaseCredentials;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.DescriptionError;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.GenericResponse;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.MosaicCredentials;
import ar.com.hipotecario.canal.libreriasecurity.domain.models.ObMayoRequest;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ClaveUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;

public class OBTransmit extends ModuloOB {
	
	static Context contextLib = new Context();
	static BodyForMigrationMay bodyForMigration = new BodyForMigrationMay();
	static ObMayoRequest request = new ObMayoRequest();
	static OfficeBankingUseCaseService obcase = new OfficeBankingUseCaseService();
	static BodyForCsm bodyForScm = new BodyForCsm();
	private static Config config = new Config();
    
	public static void inicializar(ContextoOB contexto, UsuarioOB usuarioOB) {
		try {
			MosaicCredentials mosaicCredential = new MosaicCredentials();
        	DataBaseCredentials dataBase = new DataBaseCredentials();  
        	InetAddress localHost = null;
        	try {
				localHost = InetAddress.getLocalHost();
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		
			Map<String, String> properties = Config.properties();
			mosaicCredential.setKeyApplicationId(Config.desencriptarOB(properties.get("client_id_transmit")));
			mosaicCredential.setKeySecret(Config.desencriptarOB(properties.get("secret_id_transmit")));
                      
        	dataBase.setBdUrl(Config.desencriptarOB(properties.get("libreria_transmit_bd")));
        	dataBase.setBdUser(Config.desencriptarOB(properties.get("libreria_transmit_bd_user")));
        	dataBase.setBdPassword(Config.desencriptarOB(properties.get("libreria_transmit_bd_pass")));
                
        	contextLib.setChannel("ob");
        
        	contextLib.setIp(localHost != null ? localHost.getHostAddress() : "1.0.0.0");
        	contextLib.setAuditURi(config.string("backend_api_auditor"));
        	contextLib.setUser(usuarioOB.numeroDocumento.toString());
        	contextLib.setProcessId("66b2270c-a2a5-4651-bdc2-3bea49d6b0e3");//AVERIGUAR
        	contextLib.setSession(usuarioOB.cuil.toString());
        	contextLib.setSubChannel("web");
        
        	request.setMosaicCredentials(mosaicCredential);
        	request.setDataBaseCredentials(dataBase);
        	request.setContext(contextLib);
		}catch (Exception ex) {
			LogOB.evento(contexto, "INICIALIZAR_LIBRERIA_TRANSMIT", ex.getMessage());
		}
	}
	
	public static Objeto migrarUsuario(ContextoOB contexto, UsuarioOB usuarioOB, String usuario, String clave){
		try{
			inicializar(contexto, usuarioOB);
		
			LogOB.evento(contexto, "PRE_INICIO_MIGRAR_USUARIO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
			
			ServicioClaveUsuarioOB servicioClaveUsuario = new ServicioClaveUsuarioOB(contexto);
			ClaveUsuarioOB cu = servicioClaveUsuario.findByUsuario(usuarioOB).get().get(0);
				
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
			String fechaFormateada = cu.fechaCreacion.plusDays(45).format(formatter);		
				
			//MIGRACION DE USUARIO
			if(usuarioOB.migrado == 0) {
				LogOB.evento(contexto, "INICIO_MIGRAR_USUARIO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);	
				contextLib.setJourney(config.string("journeyID"));
				bodyForMigration.setDni(usuarioOB.numeroDocumento.toString());
				bodyForMigration.setEmail(usuarioOB.email);
				bodyForMigration.setPhone("+549" + usuarioOB.telefonoMovil);        	
				bodyForMigration.setPassword(clave);
				bodyForMigration.setUserName(usuario);
				bodyForMigration.setPasswordModificationDate(fechaFormateada);
				bodyForMigration.setUsernameModificationDate(fechaFormateada); //TRAER DATOS FECHA DE USUARIO        	
				request.setBodyRequesForBuild(bodyForMigration);
				Optional<GenericResponse<?>> response = obcase.userMigration(request);
				if(response.get().isCodeErrorEqualCero()) {
					ServicioUsuarioOB servicio = new ServicioUsuarioOB(contexto);  
					LogOB.evento(contexto, "UPDATE_USUARIO_MIGRADO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
					LogOB.evento(contexto, "UPDATE_USUARIO_MIGRADO_DETALLE1", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ""+response.get().isCodeErrorEqualCero());
					try {
						LogOB.evento(contexto, "UPDATE_USUARIO_MIGRADO_DETALLE2", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ""+response.get().getAddicionalData());
					} catch (Exception ex){}
					
					Boolean varificacionDNI = OBTransmit.verificacionExistenciaDNI(contexto, usuarioOB, usuarioOB.numeroDocumento.toString());
					
					if(varificacionDNI) {
						usuarioOB.migrado = 1;
						servicio.update(usuarioOB);
					}
					return respuesta("0");
				}else {
					LogOB.evento(contexto, "ERROR_MIGRACION_TRANSMIT ", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), response.get().getAddicionalData().toString());
					return respuesta("ERROR_MIGRACION_TRANSMIT");
				}
			}
			LogOB.evento(contexto, "FIN_SIN_MIGRAR_USUARIO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), null);
			return respuesta("0");
		}catch(Exception ex) {
			LogOB.evento(contexto, "MIGRAR_USUARIO", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ex.getMessage());
			return respuesta("MIGRAR_USUARIO");	
		}
    }
	
	public static Objeto requestLibreriaRiesgoFraudes(ContextoOB contexto, UsuarioOB usuarioOB, String usuario, String clave, String responseTransmit, Objeto datos){
		try {
			inicializar(contexto, usuarioOB);

			if(!empty(responseTransmit)) {
				//RECUPERA LOS DATOS PARA GENERAR EL csm_idAuth
				JSONObject jsonObject = new JSONObject(responseTransmit);
				JSONObject jsonData = jsonObject.getJSONObject("jsonData");
				String csm_id = jsonData.getString("csm_id");
				String checksum = jsonData.getString("checksum");
				datos.set("csm_id", csm_id);
				datos.set("checksum", checksum);	
			}
			return respuesta("0");
		}catch(Exception ex) {
			LogOB.evento(contexto, "requestLibreriaRiesgoFraudes", ex.getMessage());
			return respuesta("requestLibreriaRiesgoFraudes");	
		}
    }
	
	public static Boolean lecturaCsmIdAuth(ContextoOB contexto) {
		SesionOB sesion = contexto.sesion();
		
		Map<String, String> properties = Config.properties();
		contextLib.setJourney(properties.get("journeyID_lectura_csmIdAuth"));		
		
        BodyForLecturaCsmIdAuth bodyForLectura = BodyForLecturaCsmIdAuth.builder()
                .csmIdAuth(sesion.transmit.csmIdAuth)
                .checksum(sesion.transmit.checksum)
                .dni(sesion.usuarioOB.numeroDocumento.toString())
                .build();

        request.setBodyRequesForBuild(bodyForLectura);
        
        try {
        	Optional<GenericResponse<?>> response = obcase.lecturaCsmIdAuth(request);
        
        	if(response.get().isCodeErrorEqualCero()) {
        		return true;	
        	}else if(response.get().getErrorMessage().equals("CSM de backend ha vencido")) {
        		return false;
        	}else if(response.get().getErrorMessage().equals("CSM de backend ya ha sido usado")) {
        		return false;
        	}else if(response.get().getErrorMessage().equals("error en el journey")) {
        		return false;
        	}
        }catch(Exception ex) {
        	LogOB.evento(contexto, "Error_lectura_csmIdAuth");
        	return false;
        }
        return false;	
	}

	public static Boolean verificacionExistenciaDNI(ContextoOB contexto, UsuarioOB usuarioOB, String numeroDocumento) {
		
		Map<String, String> properties = Config.properties();
		contextLib.setJourney(properties.get("journeyID_verificacionExistenciaDNI"));		
		
		BodyForVerificacionDNI bodyForLectura = BodyForVerificacionDNI.builder()
                .dni(numeroDocumento)
                .build();

        request.setBodyRequesForBuild(bodyForLectura);
        
        try {
        	Optional<GenericResponse<?>> response = obcase.verificacionExistenciaDNI(request);
			try {
				LogOB.evento(contexto, "RESPONSE_VERIFICAEXISTENCIADNI", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ""+response.get().getAddicionalData());
				LogOB.evento(contexto, "RESPONSE_VERIFICAEXISTENCIADNI_RESULT", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ""+response.get().getResult());
			} catch (Exception ex){}
 
        	String result = response.get().getResult();
        	if("success".equals(result)) {
        		return true;
        	} else if("error".equals(result)) {
        		return false;
        	}
        }catch(Exception ex) {
			LogOB.evento(contexto, "EXCEPTION_VERIFICAEXISTENCIADNI", usuarioOB.cuil.toString(), usuarioOB.numeroDocumento.toString(), ex.getMessage());
        	return false;
        }
        return false;	
	}

	public static Object generarCsmIdAuth(ContextoOB contexto) {
		String csm_id = contexto.parametros.string("csm_id", null);
		String checksum = contexto.parametros.string("checksum", null);
		Objeto datos = new Objeto();
		String csmIdAuth = null;
		SesionOB sesion = contexto.sesion();
		
		LogOB.evento(contexto, "INICIO_generarCsmIdAuth");
		
		if(csm_id != "" && checksum != "" && csm_id != null && checksum != null) {
			System.out.println("INICIAR_generarCsmIdAuth");
			contextLib.setJourney(config.string("journeyID_verifica_csm"));
			bodyForScm.setChecksum(checksum);
			bodyForScm.setTokenCsm(csm_id);
			request.setBodyRequesForBuild(bodyForScm);
			Optional<GenericResponse<?>> response = obcase.validTokenCsm(request);     
			csmIdAuth = ((AdditionalData) response.get().getAddicionalData()).getCsmIdAuth();
			datos.set("csmIdAuth", csmIdAuth);
		}
		
		sesion.transmit.checksum = checksum;
		sesion.transmit.csmIdAuth = csmIdAuth;
		sesion.save();
				
		LogOB.evento(contexto, "FIN_generarCsmIdAuth");
    	return respuesta("datos", datos);
	}
}
