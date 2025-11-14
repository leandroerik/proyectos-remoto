package ar.com.hipotecario.canal.tas.modulos.cuentas.cuenta.modelos;
import java.util.Date;

import ar.com.hipotecario.backend.base.Objeto;

public class TASSeguimientoPaquetes extends Objeto{
  public static final String INICIADA = "INICIADA";
   public static final String EN_CURSO = "EN CURSO";
   public static final String FINALIZADA = "FINALIZADA";
   public static final String CANCELADA = "CANCELADA";
   public static final String RECHAZADA = "RECHAZADA";
		
	private String estado;
	private Date fechaInicio;

  
  public TASSeguimientoPaquetes() {
  }
  
  public String getEstado() {
    return estado;
  }
  public void setEstado(String estado) {
    this.estado = estado;
  }
  public Date getFechaInicio() {
    return fechaInicio;
  }
  public void setFechaInicio(Date fechaInicio) {
    this.fechaInicio = fechaInicio;
  }

  
  
}