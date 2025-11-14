package ar.com.hipotecario.backend.servicio.api.viviendas;

import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import org.codehaus.plexus.util.StringUtils;

import java.util.Date;

public class ConsultaPersona extends ApiObjeto {
	/* ========== ATRIBUTOS ========== */
	public String codigo;
	public String mensaje;
	public String cuil;
	public String nombres;
	public String apellido;
	public String fecha_nacimiento;
	public String ejemplar;
	public String barrio;
	public String emision;
	public String vencimiento;
	public String id_tramite_principal;
	public String codigo_postal;
	public String pais;
	public String provincia;
	public String ciudad;
	public String municipio;
	public String calle;
	public String numero;
	public String monoblock;
	public String piso;
	public String departamento;
	public String codigo_fallecido;

	public String getApellido() {
		if(Util.empty(apellido)) return "";
		return StringUtils.capitaliseAllWords(apellido.toLowerCase());
	}

	public String getNombres() {
		if(Util.empty(nombres)) return "";
		return StringUtils.capitaliseAllWords(nombres.toLowerCase());
	}

	public String getFechaNacimiento() {
		try {
			return Util.ddMMyyyy(fecha_nacimiento, "yyyy-MM-dd HH:mm:ss");
		}
		catch (Exception e){
			return "";
		}
	}

	public String getCuil() {
		return esCuilValido() ? cuil.trim() : "";
	}

	public boolean esCuilValido() {
		return !Util.empty(cuil) && cuil.trim().length() == 11;
	}

	public boolean estaFallecido() {
		return !Util.empty(codigo_fallecido) && !"0".equals(codigo_fallecido);
	}

	public boolean edadValido(){
		try {
			Fecha fechaNacimiento = new Fecha(fecha_nacimiento, "yyyy-MM-dd HH:mm:ss");
			return fechaNacimiento.edad() >= 18;
		}
		catch (Exception e){
			return true;
		}
	}

	public boolean esUltimoEjemplar(String valor) {
		try {
			return ejemplar.equals(valor);
		}catch (Exception e){}
		return true;
	}
}
