package ar.com.hipotecario.canal.homebanking.ventas;

import java.math.BigDecimal;
import java.util.List;

public class SolicitudAumentoLimite {

	/* ========== ATRIBUTOS ========== */
	public String Id;
	public String tipoProducto;
	public Object TipoOperacion;
	public Integer IdProductoFrontEnd;
	public String cuenta;
	public BigDecimal MontoOfrecido;
	public BigDecimal MontoAceptado;
	public ModificacionAcuerdo Acuerdo;

	public List<Integrante> Integrantes;
	public Object Advertencias;
	public String moneda;
	public BigDecimal monto;
	public Boolean RechazadoMotor;
	public Object IdPaqueteProductos;

}
