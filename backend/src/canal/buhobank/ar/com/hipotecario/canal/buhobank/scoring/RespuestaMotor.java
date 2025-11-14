package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import ar.com.hipotecario.backend.base.Util;
import ar.com.hipotecario.canal.buhobank.GeneralBB;
import ar.com.hipotecario.canal.buhobank.scoring.Integrantes.CampanaVigente;
import ar.com.hipotecario.canal.buhobank.scoring.Integrantes.Integrante;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosOfrecidos.ProductoOfrecido;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosOfrecidos.TarjetasOfrecidas;
import ar.com.hipotecario.canal.buhobank.scoring.ProductosOfrecidos.TarjetasOfrecidas.TarjetaOfrecida;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = { "header", "body" })
@XmlRootElement(name = "response")
public class RespuestaMotor {
	@XmlElement(required = true)
	public Object header;
	@XmlElement(required = true)
	public BodyResponse body;

	@XmlAccessorType(XmlAccessType.FIELD)
	public static class BodyResponse {
		@XmlElement(required = true)
		public SetVariablesMotor setVariablesMotor = new SetVariablesMotor();
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "setVariablesMotor")
	public static class SetVariablesMotor {

		@XmlElement(required = true)
		public String idSolicitud;
		@XmlElement(required = true)
		public String fechaSolicitud;
		public Integer nroInstancia;
		@XmlElement(required = true)
		public VarIntermedias varIntermedias;
		@XmlElement(required = true)
		public Etapas etapas;
		@XmlElement(required = true)
		public Integrantes integrantes;
		@XmlElement(required = true)
		public ProductosOfrecidos productosOfrecidos;
		@XmlElement(required = true)
		public Resolucion resolucion;
		@XmlElement(required = true)
		public DocumentacionSolicitud documentacionSolicitud;
		@XmlElement(required = true)
		public DatosInternos datosInternos;
		@XmlElement(required = true)
		public IndicadoresCalidadResumen indicadoresCalidadResumen;
		@XmlElement(required = true)
		public String digitalizaDocumentacion;
		@XmlElement(required = true)
		public String codigoDistribucionAdicionalesTc;
		@XmlElement(required = true)
		public InformacionProcrearSalida informacionProcrearSalida;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		@XmlElement(name = "dummy_02", required = true)
		public String dummy02;
		@XmlElement(name = "dummy_03", required = true)
		public String dummy03;
		@XmlElement(name = "dummy_04", required = true)
		public String dummy04;
		@XmlElement(name = "dummy_05", required = true)
		public String dummy05;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "documentacionSolicitud")
	public static class DocumentacionSolicitud {

		@XmlElement(required = true)
		public List<Documentacion> documentacion;
	}

	/* ========== METODOS ========== */
	public Boolean variablesMotorNulas() {
		return Util.empty(body) || Util.empty(body.setVariablesMotor);
	}

	public Integer ingresoNeto() {
		if (variablesMotorNulas())
			return GeneralBB.INGRESO_NETO_INT;
		if (Util.empty(body.setVariablesMotor.varIntermedias))
			return GeneralBB.INGRESO_NETO_INT;
		if (Util.empty(body.setVariablesMotor.varIntermedias.ingresoComputado))
			return GeneralBB.INGRESO_NETO_INT;
		return body.setVariablesMotor.varIntermedias.ingresoComputado.intValue();
	}

	public String resolucion() {
		if (variablesMotorNulas())
			return General.RECHAZAR;
		if (Util.empty(body.setVariablesMotor.resolucion))
			return General.RECHAZAR;
		return body.setVariablesMotor.resolucion.resolucionCodigo;
	}

	public String modoAprobacion() {
		if (variablesMotorNulas())
			return null;
		if (Util.empty(body.setVariablesMotor.resolucion))
			return null;
		return body.setVariablesMotor.resolucion.modoAprobacion;
	}

	public Boolean aprobado() {
		String resolucionId = resolucion();
		if (Util.empty(resolucionId))
			return false;
		return resolucionId.equals(General.APROBAR_VERDE);
	}

	public Boolean sinIntegrantes() {
		if (variablesMotorNulas())
			return true;
		if (Util.empty(body.setVariablesMotor.integrantes))
			return true;
		Integrantes integrantes = body.setVariablesMotor.integrantes;
		if (Util.empty(integrantes.integrante) || integrantes.integrante.size() == 0)
			return true;

		return false;
	}

	public Integrante integranteTitular() {
		if (sinIntegrantes())
			return null;
		Integrantes integrantes = body.setVariablesMotor.integrantes;
		return integrantes.integrante.get(0);
	}

	public CampanaVigente campanaVigente() {
		Integrante integrante = integranteTitular();
		if (Util.empty(integrante))
			return null;

		return integrante.campañaVigente;
	}

	public String codigoPaquete() {
		CampanaVigente campana = campanaVigente();
		if (Util.empty(campana))
			return "";
		if (Util.empty(campana.PQ_CodPaquete))
			return "";

		return campana.PQ_CodPaquete;
	}

	public Boolean sinProductosOfrecidos() {
		if (variablesMotorNulas())
			return true;
		if (Util.empty(body.setVariablesMotor.productosOfrecidos))
			return true;
		ProductosOfrecidos productos = body.setVariablesMotor.productosOfrecidos;
		if (Util.empty(productos.productoOfrecido))
			return true;

		ProductoOfrecido producto = productos.productoOfrecido;

		Boolean tieneProductos = false;
		tieneProductos |= !Util.empty(producto.acuerdosOfrecidos);
		tieneProductos |= !Util.empty(producto.cuentasOfrecidas);
		tieneProductos |= !Util.empty(producto.prestamosOfrecidos);
		tieneProductos |= !Util.empty(producto.tarjetasOfrecidas);

		return !tieneProductos;
	}

	public TarjetaOfrecida tarjetaOfrecida() {
		if (sinProductosOfrecidos())
			return null;
		ProductosOfrecidos productos = body.setVariablesMotor.productosOfrecidos;
		ProductoOfrecido producto = productos.productoOfrecido;
		if (Util.empty(producto.tarjetasOfrecidas))
			return null;
		TarjetasOfrecidas tarjetas = producto.tarjetasOfrecidas;
		if (Util.empty(tarjetas.tarjetaOfrecida) || tarjetas.tarjetaOfrecida.size() == 0)
			return null;
		return tarjetas.tarjetaOfrecida.get(0);
	}

	public BigDecimal limiteCompra() {
		BigDecimal defaultLimite = new BigDecimal("0.0");
		if (sinProductosOfrecidos())
			return defaultLimite;
		TarjetaOfrecida tarjeta = tarjetaOfrecida();
		if (Util.empty(tarjeta))
			return defaultLimite;
		if (Util.empty(tarjeta.limiteCompra))
			return defaultLimite;

		return tarjeta.limiteCompra;
	}

	public String letraTC() {
		TarjetaOfrecida tarjeta = tarjetaOfrecida();
		if (Util.empty(tarjeta))
			return "";
		if (Util.empty(tarjeta.producto))
			return "";

		String letra = "";
		switch (tarjeta.producto) {
		case "2":
			letra = General.PAQ_LETRA_BUHO_PACK_WHITE;
			break;
		case "3":
			letra = General.PAQ_LETRA_GOLD;
			break;
		case "543":
			letra = General.PAQ_LETRA_PLAT;
			break;
		case "541":
			letra = General.PAQ_LETRA_BLACK;
			break;
		default:
			letra = "";
			break;
		}

		return letra;
	}
}
