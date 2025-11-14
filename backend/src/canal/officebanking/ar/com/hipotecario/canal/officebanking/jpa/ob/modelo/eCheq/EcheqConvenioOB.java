package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq;

import jakarta.persistence.*;


@Entity
@Table(schema = "dbo", name = "OB_Echeq_Convenio")

public class EcheqConvenioOB {

	@Id
    @Column(name = "id_ob_echeq",nullable = false)
    public Integer idObEcheq;
    
    @Column(name = "convenio")
    public String convenio;

	public String getConvenio() {
		return convenio;
	}

	public void setConvenio(String convenio) {
		this.convenio = convenio;
	}
    
    
}
