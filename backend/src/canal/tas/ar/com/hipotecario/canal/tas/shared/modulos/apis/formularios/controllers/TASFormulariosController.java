package ar.com.hipotecario.canal.tas.shared.modulos.apis.formularios.controllers;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.codec.binary.Base64;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.ContextoTAS;
import ar.com.hipotecario.canal.tas.LogTAS;
import ar.com.hipotecario.canal.tas.RespuestaTAS;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.formularios.servicios.TASRestFormularios;
import ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio.TASClientePersona;
import ar.com.hipotecario.canal.tas.shared.utils.models.strings.TASMensajesString;

public class TASFormulariosController {

  public static Objeto getBuscarTextos(ContextoTAS contexto){
        try {
            String idCliente = contexto.parametros.string("idCliente");
            TASClientePersona clienteSesion = contexto.sesion().getClienteTAS();
            // ? comentar las proximas 2 lineas para no validar el cliente en sesion
            if (clienteSesion == null)
                return RespuestaTAS.sinParametros(contexto, "Sin Sesion");
            if (!idCliente.contentEquals(clienteSesion.getIdCliente()))
                return RespuestaTAS.parametrosIncorrectos(contexto, "Cliente equivocado");
            String nroSolicitud = contexto.parametros.string("nroSolicitud", "-1");
            String idMoneda = contexto.parametros.string("idMoneda", "80");
            List<String> pdf = new ArrayList<String>();
            if(!nroSolicitud.contentEquals("-1")){
                
                Objeto params = new Objeto();
                params.set("solicitudId", nroSolicitud);
                params.set("canal", "TAS");
                params.set("grupoCodigo", "CASOLIC");

                Objeto responsePDF = TASRestFormularios.getFormularioPDF(contexto, params);
                if(responsePDF.string("estado").contentEquals("ERROR")){
                  LogTAS.error(contexto, (Exception) responsePDF.get("error"));
                } else{
                String texto = responsePDF.objeto("respuesta").string("Data");            
                pdf= separar(convertPDFtoTxt(texto));
              }
            } 

            String[] valores_comisiones = !idMoneda.equals("80") ? contexto.config.string("tas_valores_comisiones_dolares").split(",") : contexto.config.string("tas_valores_comisiones_pesos").split(",");
            List<String> comisiones_cargos_tasas_nn = separar(valores_comisiones, TASMensajesString.TAS_COMISIONES_CARGOS_TASAS_NN_0.getTipoMensaje(), 
            TASMensajesString.TAS_COMISIONES_CARGOS_TASAS_NN_1.getTipoMensaje(), TASMensajesString.TAS_COMISIONES_CARGOS_TASAS_NN_2.getTipoMensaje(),
            TASMensajesString.TAS_COMISIONES_CARGOS_TASAS_NN_3.getTipoMensaje(), TASMensajesString.TAS_COMISIONES_CARGOS_TASAS_NN_4.getTipoMensaje());
            
            Objeto response = new Objeto();
            response.set("estado", "OK");
            response.set("texto", pdf);
            response.set("comisiones", comisiones_cargos_tasas_nn);
            return response;
        } catch (Exception e) {
            return RespuestaTAS.error(contexto, "TASApiPersona - getBuscarTextos()", e);
        }
    }

    /**
     * String extractedText = null;
		byte[] textEncoded = Base64.decodeBase64(base64Text.getBytes("UTF-8"));
		PDDocument document = PDDocument.load(textEncoded);

		PDFTextStripper pdfStripper = new PDFTextStripper();
		pdfStripper.setStartPage(1);
		pdfStripper.setEndPage(document.getNumberOfPages());
		extractedText = pdfStripper.getText(document);

		String allText = "";

		// split by detecting newline
		String[] lines = extractedText.split("\r\n|\r|\n");

		for (String temp : lines) {
			if (temp.contains("\r\n")) {
				temp = temp.replace("\r\n", "");
			} else if (temp.contains(" \r\n")) {
				temp = temp.replace(" \r\n", "");
			}

			temp = filterFooter(temp);
			if (temp.length() > 4 && Character.isDigit(temp.charAt(0)) && (Character.isDigit(temp.charAt(1)) || temp.charAt(1) == '.'))

				temp = "<br>" + temp;

			allText = allText + temp;

		}

		return allText;
     */

     /*
      * String base64 = response.string("Data");
        byte[] archivo = Base64.getDecoder().decode(base64);
        contexto.responseHeader("Content-Type", response.string("propiedades.MimeType", "application/pdf") + "; name=archivo.pdf");
        return archivo;
      */
    private static String convertPDFtoTxt(String base64Text){
      try{
        String extractedText = null;
		    byte[] textEncoded = Base64.decodeBase64(base64Text.getBytes("UTF-8"));
		    PDDocument document = PDDocument.load(textEncoded);

        PDFTextStripper pdfStripper = new PDFTextStripper();
		    pdfStripper.setStartPage(1);
		    pdfStripper.setEndPage(document.getNumberOfPages());
		    extractedText = pdfStripper.getText(document);

        String allText = "";

		    // split by detecting newline
		    String[] lines = extractedText.split("\r\n|\r|\n");
        for (String temp : lines) {
          if (temp.contains("\r\n")) {
            temp = temp.replace("\r\n", "");
          } else if (temp.contains(" \r\n")) {
            temp = temp.replace(" \r\n", "");
          }
          temp = filterFooter(temp);
			    if (temp.length() > 4 && Character.isDigit(temp.charAt(0)) && (Character.isDigit(temp.charAt(1)) || temp.charAt(1) == '.'))
				  temp = "<br>" + temp;
			    allText = allText + temp;
        }

        return allText;
      }catch(Exception e){
        return null;
      }
      
    }

    private static String filterFooter(String line) {
      String retornoFiltrado = "";
  
      if (line.contains("Pagina") && line.contains("Solicitud Nro:")) {
        retornoFiltrado = "<br>" + line + "<br>";
      } else {
        retornoFiltrado = line;  
      }  
      return retornoFiltrado;
    }

    private static List<String> separar(String text) {
      List<String> cadenas = new ArrayList<String>();
      int length = text.length();
      for (int i = 0; i < length; i += 500){
        if ( i+500 < text.length()){
          cadenas.add(text.substring(i, i+500));
        }else {
          cadenas.add(text.substring(i,text.length()));
        } 
      }
      return cadenas;
    }
    private static List<String> separar(String text0, String text1, String text2, String text3, String text4){        
			String text = text0 + text1 + text2 + text3 + text4;	
      List<String> cadenas = new ArrayList<String>();
				for (int i=0; i<text.length();i+=500){
					if (i+500<text.length()) cadenas.add(text.substring(i, i+500));
					else cadenas.add(text.substring(i,text.length()));
				}
				return cadenas;
		}

    private static List<String> separar(String [] comisiones ,String text0, String text1, String text2, String text3, String text4){        
			String text = text0 + text1 + text2 + text3 + text4;
      for (int i=0; i<comisiones.length; i++) {
        text = text.replace("placeholder"+(i+10), comisiones[i]);
      }
      List<String> cadenas = new ArrayList<String>();
				for (int i=0; i<text.length();i+=500){
					if (i+500<text.length()) cadenas.add(text.substring(i, i+500));
					else cadenas.add(text.substring(i,text.length()));
				}
				return cadenas;
		}
    
  
}
