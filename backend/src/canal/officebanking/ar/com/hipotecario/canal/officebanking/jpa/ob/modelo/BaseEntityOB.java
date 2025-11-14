package ar.com.hipotecario.canal.officebanking.jpa.ob.modelo;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public class BaseEntityOB {

	@Column(name = "fecha_creacion", nullable = false)
	public LocalDateTime fechaCreacion;

}