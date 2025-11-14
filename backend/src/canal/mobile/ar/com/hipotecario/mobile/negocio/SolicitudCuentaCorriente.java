package ar.com.hipotecario.mobile.negocio;

import java.math.BigDecimal;
import java.util.List;

public class SolicitudCuentaCorriente {

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String tipoProducto;
	public String IdProductoFrontEnd;
	public String TipoOperacion;
	public String Moneda;
	public String NombreCuenta;
	public Boolean DepositoCheques;
	public Boolean DepositoInicial;
	public Object MontoDepositoInicial;
	public String TipoPromedio;
	public String TipoCapitalizacion;
	public Boolean DepositoChequesGranel;
	public Boolean CuentaGastosPropia;
	public String TipoCuentaGastos;
	public Object NumeroCuentaGastos;
	public String Iva;
	public Boolean IvaExencion;
	public Boolean IvaVencimientoExencion;
	public Boolean IvaReduccion;
	public Boolean IvaVencimientoReduccion;
	public String Ganancias;
	public Object GanExencion;
	public Object GanVencimientoExencion;
	public Boolean DocumentacionTributaria;
	public Object NumeroOperacion;
	public Object Subtipo;
	public Object ProductoBancario;
	public String Categoria;
	public TipoDomicilio DomicilioResumen;
	public String Oficina;
	public String Oficial;
	public Boolean CobroPrimerMantenimiento;
	public String Origen;
	public String UsoFirma;
	public String Ciclo;
	public Boolean ResumenMagnetico;
	public Boolean TransfiereAcredHab;
	public List<Integrante> Integrantes;
	public CuentaLegal CuentaLegales;
	public Object Advertencias;
	public Boolean RechazadoMotor;
	public String EmpresaAseguradora;
	public Acuerdo Acuerdo;

	public static class TipoDomicilio {
		public String Id;
		public String Tipo;
		public Integer SecuencialCobis;
	}

	public static class CuentaLegal {
		public String Id;
		public String Uso;
		public String Transacciones;
		public Integer TransaccionesCantidad;
		public BigDecimal TransaccionesVolumen;
		public Boolean RealizaTransferencias;
	}

	public static class Acuerdo {
		public BigDecimal ValorAutorizado;
		public String Plazo;
		public String TipoDescubierto;
		public String TipoLiquidacion;

	}

}
