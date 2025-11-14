package ar.com.hipotecario.canal.homebanking.ventas;

import java.math.BigDecimal;
import java.util.Date;

public class ModificacionAcuerdo {

	/* ========== ATRIBUTOS ========== */
	public String AcuerdoDescripcion;
	public String Tipo;
	protected Date FechaAutorizacion;
	protected Date FechaVencimiento;
	protected String Autorizante;
	protected String TipoVencimiento;
	protected BigDecimal Tasa;
	protected String Garantia;
	protected BigDecimal MontoActual;
	protected BigDecimal MontoSolicitado;
	protected BigDecimal MontoOfrecido;
	protected BigDecimal MontoAceptado;
	protected String TipoDescubierto;
	protected BigDecimal ValorAutorizado;
}
