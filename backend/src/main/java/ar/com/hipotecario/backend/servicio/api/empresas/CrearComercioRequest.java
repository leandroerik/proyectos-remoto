package ar.com.hipotecario.backend.servicio.api.empresas;

import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class CrearComercioRequest extends ApiObjeto {
	
	private String cuit;
	private String email;
	private String nombreFantasia;
	private String razonSocial;
	private String segmento;
	private String codigoActividadAFIP;
	private Boolean esExceptuadoIVA;
	private List<ComercioCondicionFiscal> condicionesFiscales;
	private Boolean esPersonaJuridica;
	private String sexo;
	private ComercioDomicilio domicilio;

	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		this.cuit = cuit;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNombreFantasia() {
		return nombreFantasia;
	}

	public void setNombreFantasia(String nombreFantasia) {
		this.nombreFantasia = nombreFantasia;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getSegmento() {
		return segmento;
	}

	public void setSegmento(String segmento) {
		this.segmento = segmento;
	}

	public String getCodigoActividadAFIP() {
		return codigoActividadAFIP;
	}

	public void setCodigoActividadAFIP(String codigoActividadAFIP) {
		this.codigoActividadAFIP = codigoActividadAFIP;
	}

	public Boolean getEsExceptuadoIVA() {
		return esExceptuadoIVA;
	}

	public void setEsExceptuadoIVA(Boolean esExceptuadoIVA) {
		this.esExceptuadoIVA = esExceptuadoIVA;
	}

	public List<ComercioCondicionFiscal> getCondicionesFiscales() {
		return condicionesFiscales;
	}

	public void setCondicionesFiscales(List<ComercioCondicionFiscal> condicionesFiscales) {
		this.condicionesFiscales = condicionesFiscales;
	}

	public Boolean getEsPersonaJuridica() {
		return esPersonaJuridica;
	}

	public void setEsPersonaJuridica(Boolean esPersonaJuridica) {
		this.esPersonaJuridica = esPersonaJuridica;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public ComercioDomicilio getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(ComercioDomicilio domicilio) {
		this.domicilio = domicilio;
	}

	public static class ComercioCondicionFiscal{
		private String jurisdiccion;
		private String tipo;
		private Boolean valor;
		private String fechaFinVigencia;

		public String getJurisdiccion() {
			return jurisdiccion;
		}

		public void setJurisdiccion(String jurisdiccion) {
			this.jurisdiccion = jurisdiccion;
		}

		public String getTipo() {
			return tipo;
		}

		public void setTipo(String tipo) {
			this.tipo = tipo;
		}

		public Boolean getValor() {
			return valor;
		}

		public void setValor(Boolean valor) {
			this.valor = valor;
		}

		public String getFechaFinVigencia() {
			return fechaFinVigencia;
		}

		public void setFechaFinVigencia(String fechaFinVigencia) {
			this.fechaFinVigencia = fechaFinVigencia;
		}
	}
	
	public static class ComercioDomicilio{
		private String codigoPostal;
		private String localidad;
		private String calle;
		private String altura;
		private Integer codigoProvincia;
		private String datosAdicionales;
		private Integer latitud;
		private Integer longitud;

		public String getCodigoPostal() {
			return codigoPostal;
		}

		public void setCodigoPostal(String codigoPostal) {
			this.codigoPostal = codigoPostal;
		}

		public String getLocalidad() {
			return localidad;
		}

		public void setLocalidad(String localidad) {
			this.localidad = localidad;
		}

		public String getCalle() {
			return calle;
		}

		public void setCalle(String calle) {
			this.calle = calle;
		}

		public String getAltura() {
			return altura;
		}

		public void setAltura(String altura) {
			this.altura = altura;
		}

		public Integer getCodigoProvincia() {
			return codigoProvincia;
		}

		public void setCodigoProvincia(Integer codigoProvincia) {
			this.codigoProvincia = codigoProvincia;
		}

		public String getDatosAdicionales() {
			return datosAdicionales;
		}

		public void setDatosAdicionales(String datosAdicionales) {
			this.datosAdicionales = datosAdicionales;
		}

		public Integer getLatitud() {
			return latitud;
		}

		public void setLatitud(Integer latitud) {
			this.latitud = latitud;
		}

		public Integer getLongitud() {
			return longitud;
		}

		public void setLongitud(Integer longitud) {
			this.longitud = longitud;
		}

	}

}