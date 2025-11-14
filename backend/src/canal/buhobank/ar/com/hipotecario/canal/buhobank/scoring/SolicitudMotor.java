package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigInteger;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "header", "body" })
@XmlRootElement(name = "request")
public class SolicitudMotor {
	@XmlElement(required = true)
	public Header header = new Header();
	@XmlElement(required = true)
	public Body body = new Body();

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Body {
		@XmlElement(required = true)
		public Solicitud solicitud = new Solicitud();
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class Header {
		@XmlElement(required = true)
		public String handle;
		@XmlElement(required = true)
		public String requestSource;
		@XmlElement(required = true)
		public String startTime;
		@XmlElement(required = true)
		public String component;
		@XmlElement(required = true)
		public String action;
		@XmlElement(required = true)
		public String dominio;
		@XmlElement(required = true)
		public String user;
		@XmlElement(required = true)
		public String password;
		@XmlElement(required = true)
		public String perfil;
		public String userCOBIS;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "solicitud")
	public static class Solicitud {

		public long idSolicitud;
		@XmlElement(required = true)
		public BigInteger nroInstancia;
		@XmlElement(required = true)
		public String canalVenta;
		@XmlElement(required = true)
		public String subCanalVenta;
		@XmlElement(required = true)
		public String puntoVenta;
		@XmlElement(required = true)
		public BigInteger oficialVenta;
		@XmlElement(required = true)
		public String nroDocVendedor;
		@XmlElement(required = true)
		public String canalTramite;
		@XmlElement(required = true)
		public String subCanalTramite;
		@XmlElement(required = true)
		public String oficialTramite;
		@XmlElement(required = true)
		public String observaciones;
		@XmlElement(required = true)
		public String codigoExcepcionTotal;
		@XmlElement(required = true)
		public String fechaFinalizacionDex;
		@XmlElement(required = true)
		public String tipoInvocacion;
		@XmlElement(required = true)
		public Solicitantes solicitantes;
		@XmlElement(required = true)
		public ProductosSolicitados productosSolicitados;
		@XmlElement(required = true)
		public SolicitudEtapas solicitudEtapas;
		@XmlElement(required = true)
		public InformacionProcrear informacionProcrear;
		public String tipoConsulta;
		public String fuenteConsulta;
		public Boolean flagSimulacion;
		public Boolean flagRutaConPactado;
		public Boolean flagSolicitaAprobacionEstandar;
		public Boolean flagSolicitaComprobarIngresos;
		public Boolean flagSolicitaAprobacionCentralizada;
		public Boolean flagSolicitaExcepcionChequeoFinal;
	}

}