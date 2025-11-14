package ar.com.hipotecario.canal.officebanking.dto.prisma;

import com.fasterxml.jackson.annotation.JsonProperty;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.servicio.api.productos.CuentasOB.CuentaOB;

public class TransaccionesRequest {
	
	private CardInfo card;
	private ClientPrisma client;
	private String company_id;

	 public Objeto toObjeto() {
        Objeto objeto = new Objeto();
        
        objeto.set("card", this.card);
        objeto.set("client", this.client);
        objeto.set("company_id", this.company_id);
        
        return objeto;
	 }

	    public CardInfo getCard() {
	        return card;
	    }

	    public void setCard(CardInfo card) {
	        this.card = card;
	    }

	    public ClientPrisma getClient() {
	        return client;
	    }

	    public void setClient(ClientPrisma client) {
	        this.client = client;
	    }

	    public String getCompanyId() {
	        return company_id;
	    }

	    public void setCompanyId(String company_id) {
	        this.company_id = company_id;
	    }


	    public static class CardInfo {

	        private String bank_code;
	        private String bin;
	        private String last_four_digits;


	        public String getBankCode() {
	            return bank_code;
	        }

	        public void setBankCode(String bank_code) {
	            this.bank_code = bank_code;
	        }

	        public String getBin() {
	            return bin;
	        }

	        public void setBin(String bin) {
	            this.bin = bin;
	        }

	        public String getLastFourDigits() {
	            return last_four_digits;
	        }

	        public void setLastFourDigits(String last_four_digits) {
	            this.last_four_digits = last_four_digits;
	        }
	    }


	    public static class ClientPrisma {

	        private String document_type;
	        private String document_number;
	        private String gender;

	        public String getDocumentType() {
	            return document_type;
	        }

	        public void setDocumentType(String document_type) {
	            this.document_type = document_type;
	        }

	        public String getDocumentNumber() {
	            return document_number;
	        }

	        public void setDocumentNumber(String document_number) {
	            this.document_number = document_number;
	        }

	        public String getGender() {
	            return gender;
	        }

	        public void setGender(String gender) {
	            this.gender = gender;
	        }


	    }
	
}
