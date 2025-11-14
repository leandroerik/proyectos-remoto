package ar.com.hipotecario.backend.servicio.api.empresas;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.codec.digest.DigestUtils;

import com.azure.storage.blob.BlobClient;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.AzureBlobStorageManager;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import static ar.com.hipotecario.canal.officebanking.util.StringUtil.agregarCerosAIzquierda;

public class ValidaintegridadArchivoOB extends ApiObjeto {

	/* ========== ATRIBUTOS ========== */

	/* ========== SERVICIOS ========== */
	// API-Empresas_DatosEmpresa
	public static ValidaintegridadArchivoOB get(ContextoOB contexto, String nombreArchivo, String convenio, String subconvenio) {

		ApiRequest request = new ApiRequest("API-Empresas_ValidaintegridadArchivo", "empresas", "GET", "/v1/sat/validaIntegridadArchivo", contexto);
		request.header("Content-Type","application/json");
		SesionOB sesion = contexto.sesion();
		SimpleDateFormat timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		String dateString = timestamp.format(new Date());
		request.query("codigoEntidad", "044");						
		request.query("fechaHoraConsulta",	dateString);					
		request.query("frontEnd", "2");						
		request.query("frontEndID", "OB");							
		request.query("id",	sesion.usuarioOB.idCobis+"-"+sesion.usuarioOB.codigo);
		request.query("idUsrLog", sesion.usuarioOB.idCobis);
		request.query("tipoID",		"1");				
		
		request.query("canalRecep","OB");		
		request.query("nomArch", nombreArchivo);
//		StringUtils.leftPad((filtroDto.getNroSubconvenio()).toString(), 7, "0");
		request.query("subconvenio",agregarCerosAIzquierda(convenio,7)+"-"+agregarCerosAIzquierda(subconvenio,7));						
		request.query("tipoVal",		"Hash");				
		request.query("valHash", getValHash(contexto,nombreArchivo,(nombreArchivo.toLowerCase().startsWith("pb"))||nombreArchivo.toLowerCase().startsWith("pc")));
					
		ApiResponse response = request.ejecutar();
		ApiException.throwIf(!response.http(200), request, response);
		return response.crear(ValidaintegridadArchivoOB.class, response.objeto("Detallelote"));
	}

    public static String getValHash(ContextoOB contexto, String archivoNombre,boolean esBeneficiarios) {
        String valHash = null;
        
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto);
		String rutaArchivo ="";
		if(esBeneficiarios){
			 rutaArchivo = contexto.config.string("pap_ruta_master_files","pagos-masivos/master-file-init/") + archivoNombre;
		}else{
			 rutaArchivo = contexto.config.string("pap_ruta_en_bandeja","pagos-masivos/en-bandeja/") + archivoNombre;
		}

        BlobClient blobClient = az.ObtenerblobClientFile(contexto, rutaArchivo);
        try {
        	String archivo = blobClient.downloadContent().toString();
			valHash = DigestUtils.md5Hex(archivo);
		} catch (Exception e) {
			System.out.println(e.toString());
			e.printStackTrace();
		}
       
    	return valHash;
    }
    

	/* ========== TEST ========== */
	public static void main(String[] args) throws InterruptedException {
		ContextoOB contexto = new ContextoOB("OB", "desarrollo","123");
		String datos = getValHash( contexto, "PO_192_1_182_240607_7_AUTOGEN.txt",false);
		System.out.println(datos);
//		imprimirResultado(contexto, datos);
	}
}
