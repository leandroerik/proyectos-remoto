package ar.com.hipotecario.canal.buhobank.scoring;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "productosOfrecidos")
public class ProductosOfrecidos {

	@XmlElement(required = true)
	public ProductoOfrecido productoOfrecido;

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "productoOfrecido")
	public static class ProductoOfrecido {

		@XmlElement(required = true)
		public CuentasOfrecidas cuentasOfrecidas;
		@XmlElement(required = true)
		public AcuerdosOfrecidos acuerdosOfrecidos;
		@XmlElement(required = true)
		public TarjetasOfrecidas tarjetasOfrecidas;
		@XmlElement(required = true)
		public PrestamosOfrecidos prestamosOfrecidos;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "cuentasOfrecidas")
	public static class CuentasOfrecidas {

		@XmlElement(required = true)
		public List<CuentaOfrecida> cuentaOfrecida;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "cuentaOfrecida")
		public static class CuentaOfrecida {

			@XmlElement(required = true)
			public BigInteger nro;
			@XmlElement(required = true)
			public String subProducto;
			@XmlElement(required = true)
			public String negocio;
			@XmlElement(required = true)
			public AcuerdoOfrecido acuerdoOfrecido;
			@XmlElement(required = true)
			public DocumentacionCuenta documentacionCuenta;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			@XmlElement(name = "dummy_02", required = true)
			public String dummy02;
			@XmlElement(name = "dummy_03", required = true)
			public String dummy03;
			public Boolean flagModificacion;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "acuerdoOfrecido")
	public static class AcuerdoOfrecido {

		@XmlElement(required = true)
		public BigInteger nro;
		@XmlElement(required = true)
		public String subProducto;
		@XmlElement(required = true)
		public String negocio;
		public Integer plazo;
		@XmlElement(name = "dummy_01", required = true)
		public String dummy01;
		public BigDecimal monto;
		public BigDecimal montoMax;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "documentacionCuenta")
	public static class DocumentacionCuenta {

		@XmlElement(required = true)
		public List<Documentacion> documentacion;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "acuerdosOfrecidos")
	public static class AcuerdosOfrecidos {

		@XmlElement(required = true)
		public List<AcuerdoOfrecido> acuerdoOfrecido;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "tarjetasOfrecidas")
	public static class TarjetasOfrecidas {

		@XmlElement(required = true)
		public List<TarjetaOfrecida> tarjetaOfrecida;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "tarjetaOfrecida")
		public static class TarjetaOfrecida {

			@XmlElement(required = true)
			public BigInteger nro;
			@XmlElement(required = true)
			public String subProducto;
			@XmlElement(required = true)
			public String negocio;
			@XmlElement(required = true)
			public String destinoFondos;
			@XmlElement(required = true)
			public String tipoBien;
			@XmlElement(required = true)
			public String marca;
			@XmlElement(required = true)
			public String producto;
			@XmlElement(required = true)
			public String modeloLiquidacion;
			@XmlElement(required = true)
			public String cartera;
			@XmlElement(required = true)
			public String grupoAfinidad;
			@XmlElement(required = true)
			public DocumentacionTarjeta documentacionTarjeta;
			@XmlElement(required = true)
			public String codigoDistribucion;
			@XmlElement(required = true)
			public CodigosDistribucion codigosDistribucion;
			@XmlElement(required = true)
			public String variablesArbol;
			@XmlElement(required = true)
			public String valoresArbol;
			@XmlElement(required = true)
			public String tipoLimite;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			@XmlElement(name = "dummy_02", required = true)
			public String dummy02;
			@XmlElement(name = "dummy_03", required = true)
			public String dummy03;
			@XmlAnyElement(lax = true)
			public Object any;
			public BigDecimal limiteCompra;
			public BigDecimal limiteCompraMax;
			public BigDecimal limitePrimeraCompra;
			public Boolean flagModificacion;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "documentacionTarjeta")
	public static class DocumentacionTarjeta {

		@XmlElement(required = true)
		public List<Documentacion> documentacion;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "codigosDistribucion")
	public static class CodigosDistribucion {

		@XmlElement(required = true)
		public List<CodigoDistribucion> codigoDistribucion;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "codigoDistribucion")
		public static class CodigoDistribucion {

			@XmlElement(required = true)
			public String codigoDistribucion;
			public Boolean codigoDefault;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "prestamosOfrecidos")
	public static class PrestamosOfrecidos {

		@XmlElement(required = true)
		public List<PrestamoOfrecido> prestamoOfrecido;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "prestamoOfrecido")
		public static class PrestamoOfrecido {

			@XmlElement(required = true)
			public BigInteger nro;
			@XmlElement(required = true)
			public String subProducto;
			@XmlElement(required = true)
			public String negocio;
			@XmlElement(required = true)
			public String destinoFondos;
			@XmlElement(required = true)
			public String destinoFondosPrestamo;
			@XmlElement(required = true)
			public String tipoBien;
			@XmlElement(required = true)
			public String destinoBien;
			@XmlElement(required = true)
			public String destinoBienPrestamo;
			@XmlElement(required = true)
			public String productoPrestamo;
			@XmlElement(required = true)
			public BigInteger plazo;
			@XmlElement(required = true)
			public BigInteger plazoObjetivo;
			@XmlElement(required = true)
			public Desembolsos desembolsos;
			@XmlElement(required = true)
			public DocumentacionPrestamo documentacionPrestamo;
			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			@XmlElement(name = "dummy_02", required = true)
			public String dummy02;
			@XmlElement(name = "dummy_03", required = true)
			public String dummy03;
			@XmlElement(required = true)
			public Tenencias tenencias;
			@XmlElement(required = true)
			public String tipoDDJJSalud;
			@XmlElement(required = true)
			public Preventa preventa;
			@XmlElement(required = true)
			public String idPreventa;
			public BigDecimal montoAnticipoProyectoObra;
			public BigDecimal montoFinanciacionTerreno;
			public BigDecimal montoAcumuladoPreventa;
			public BigDecimal valorInstrumento;
			public BigDecimal monto;
			public Integer antiguedadBien;
			public BigDecimal cuota;
			public BigDecimal tasa;
			public BigDecimal rci;
			public BigDecimal ltv;
			public BigDecimal montoMax;
			public Integer plazoMax;
			public BigDecimal cuotaMax;
			public BigDecimal rciMax;
			public BigDecimal ltvMaxPolitica;
			public BigDecimal ltvMax;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "desembolsos")
	public static class Desembolsos {

		@XmlElement(required = true)
		public List<DesembolsoPrestamo> desembolsoPrestamo;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "desembolsoPrestamo")
		public static class DesembolsoPrestamo {

			@XmlElement(name = "dummy_01", required = true)
			public String dummy01;
			public BigDecimal montoDesembolso;
			public BigDecimal porAvanceObra;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "documentacionPrestamo")
	public static class DocumentacionPrestamo {

		@XmlElement(required = true)
		public List<Documentacion> documentacion;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "tenencias")
	public static class Tenencias {

		@XmlElement(required = true)
		public List<TenenciaCedulas> tenenciaCedulas;

		@XmlAccessorType(XmlAccessType.FIELD)
		@XmlRootElement(name = "tenenciaCedulas")
		public static class TenenciaCedulas {

			@XmlElement(required = true)
			public String cuentaComitente;
			@XmlElement(required = true)
			public String serie;
			public BigDecimal disponibleNominal;
			public BigDecimal cotizacionAplicada;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "preventa")
	public static class Preventa {

		public BigDecimal cuotaPreventa;
		public BigDecimal montoPreventa;
		public BigDecimal montoAnticipo;
	}
}
