package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoAProveedores;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(schema = "dbo", name = "OB_Orden_Pago_FechaEjecucion")
public class OrdenPagoFechaEjecucionOB{

	@Id
	@Column(name = "idOperacion", nullable = false)
    public Integer idOperacion;
		
	@Column(name = "nombre_archivo")
    public String nombreArchivo;
	
	@Column(name = "fecha_ejecucion", nullable = false)
    public LocalDate fechaEjecucion;
}
