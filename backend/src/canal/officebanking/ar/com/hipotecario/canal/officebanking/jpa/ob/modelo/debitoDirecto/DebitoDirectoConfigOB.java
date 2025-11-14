package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Debito_Directo_Config")

public class DebitoDirectoConfigOB {

	    @Id
	    @Column(name = "nombre_columna", nullable = false)
	    public String nombreColumna;
	    @Column(name = "visible", nullable = false)
	    public Boolean visible;

	    @Column(name = "longitud")
	    public Integer longitud;

	    @Column(name = "posicion", unique = true)
	    public Integer posicion;

	    public Integer getPosicion() {
	        return posicion;
	    }
	
}
