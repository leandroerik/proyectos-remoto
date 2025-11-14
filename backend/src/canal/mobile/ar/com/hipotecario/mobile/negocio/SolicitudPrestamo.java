package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import ar.com.hipotecario.mobile.ConfigMB;
import ar.com.hipotecario.mobile.lib.Objeto;

public class SolicitudPrestamo {

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String tipoProducto;
	public Integer IdProductoFrontEnd;
	public Object TipoOperacion;
	public String Amortizacion;
	public String TipoTasa;
	public Object TipoBien;
	public Integer DestinoBien;
	public String DescripcionDestinoFondos;
	public Object SubtipoBien;
	public String SubProducto;
	public Object AntiguedadBien;
	public Object ValorBien;
	public BigDecimal MontoSolicitado;
	public Integer PlazoSolicitado;
	public Object CuotaSolicitada;
	public String Mercado;
	public BigDecimal MontoAprobado;
	public String FormaCobroTipo;
	public String FormaCobroCuenta;
	public Domicilio Domicilio = new Domicilio();
	public Integer Plazo;
	public Object TipoPlazo;
	public BigDecimal Tasa;
	public BigDecimal CFT;
	public Boolean FechaCobroFija;
	public Integer DiaCobro;
	public Integer EmpresaAseguradora;
	public Object MontoAfectacion;
	public Object ConvenioRecaudacion;
	public Object FechaCertificado;
	public Object Bonificacion;
	public MailAvisos MailAvisos;
	public Object DdjjSalud;
	public Desembolsos Desembolsos = new Desembolsos();
	public Object ListaDesembolsos;
	public List<Integrante> Integrantes;
	public Object Advertencias;
	public String productoBancario;
	public String producto;
	public String submercado;
	public String destinoVivienda;
	public Integer ubicacion;
	public Object diasVencido;
	public Object periodoGraciaCapital;
	public Object fechaImpresion;
	public Boolean capitalizarRubro;
	public BigDecimal montoDesembolsado;
	public List<Object> rubros = null;
	public List<Object> seguros = null;
	public Object AvisosCorreoTradicional;
	public Boolean AvisosViaMail;
	public Object unidadFuncional;
	public Object valorTerreno;
	public BigDecimal valorReposicion;
	public Object tipoBonificacion;
	public Object anticipo;
	public BigDecimal importeCuota;
	public String moneda;
	public String MonedaDestino;
	public Integer oficial;
	public String destino;
	public String formaCobro;
	public String oficina;
	public String cuenta;
	public String TipoDDJJSalud;
	public Boolean RechazadoMotor;
	public Object DerivacionOnline;
	public Object datosMicrocreditos;
	public Object IdPaqueteProductos;
	public Object MontoSubsidio;
	public Object MontoSubsidioCalculado;
	public Object MontoAprobadoUVA;
	public Object CotizacionUVA;
	public Object FechaCotizacionUVA;
	public String DetalleTrabajo;
	public BigDecimal MontoMateriales;
	public BigDecimal MontoManoObra;
	public BigDecimal MontoArtefactos;
	public String DestinoBienVivienda;

	public class Domicilio {
		public String Id;
		public String Tipo;
		public Integer SecuencialCobis;
	}

	public class MailAvisos {
		public String Id;
		public String Tipo;
		public String Direccion;
		public Integer SecuencialCobis;
	}

	public static class Desembolsos {
		public String Id;
		public Integer NroDesembolso;
		public BigDecimal Capital;
		public Object FechaLiquidacion;
		public List<FormasDesembolso> FormasDesembolso = new ArrayList<>();
	}

	public static class FormasDesembolso {
		public String Id;
		public String Forma;
		public String Referencia;
		public String Beneficiario;
		public Integer NroDesembolso;
		public BigDecimal Valor;
		public Boolean EsDesembolsoBatch;
		public Object Advertencias;
	}

	/* ========== METODOS ========== */
	/*
	 * public Boolean esProcrearRefaccion() { Set<String>
	 * subProductosProcrearRefaccion = Config.esDesarrollo() ? Objeto.setOf("36",
	 * "37", "38") : Objeto.setOf("23", "24", "25"); Boolean esProcrearRefaccion =
	 * subProductosProcrearRefaccion.contains(SubProducto); return
	 * esProcrearRefaccion; }
	 */
	public Boolean esProcrearRefaccion() {
		Set<String> subProductosProcrearRefaccion = ConfigMB.esDesarrollo() ? Objeto.setOf("41", "42", "38") : Objeto.setOf("28", "29", "25");
		Boolean esProcrearRefaccion = subProductosProcrearRefaccion.contains(SubProducto);
		return esProcrearRefaccion;
	}

	public Boolean esPrestamoComplementario() {
		String subProductoPrestamoComplementario = ConfigMB.esDesarrollo() ? "43" : "30";
		return subProductoPrestamoComplementario.equals(SubProducto);
	}

}
