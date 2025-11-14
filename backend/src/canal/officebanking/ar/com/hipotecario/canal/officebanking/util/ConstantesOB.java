package ar.com.hipotecario.canal.officebanking.util;

public class ConstantesOB {
	// **************** Factoring ********************//
	public static final Integer FACTORING_PARAMETROS_PRODUCTO_TIPO = 110;
	public static final Integer FACTORING_AUTORIZACION_PARAMETRO_MODULO = 802;
	public static final Integer FACTORING_AUTORIZACION_PARAMETRO_TRANSACCION = 110;
	public static final String FACTORING_AUTORIZACION_PARAMETRO_USUARIO = "OFB";
	public static final Integer FACTORING_CLIENTE_TIPO_DOCUMENTO = 11;
	public static final Integer FACTORING_PAGINACION_CANTIDAD_PAGINAS = 1;
	public static final Integer FACTORING_PAGINACION_NUMERO_PAGINA = 1;
	public static final String FACTORING_PARAMETROS_SUCURSAL = "000";
	public static final String FACTORING_DESCUENTO_ESTADO_ELIMINAR = "ELI";
	public static final String FACTORING_DESCUENTO_ESTADO_PREAUTORIZAR = "PAU";
	public static final String FACTORING_DESCUENTO_ESTADO_AUTORIZAR = "AUT";
	public static final String FACTORING_DESCUENTO_ESTADO_RECHAZAR = "REC";
	public static final String COMEX_MAIL_PARA = "COMERCIOEXTERIOR@hipotecario.com.ar";
	public static final String COMEX_MAIL_CC = "INFO.COMEX@hipotecario.com.ar";
	public static final String COMEX_MAIL_NOMBRE_PARA = "COMEX";
	public static final String COMEX_MAIL_MENSAJE = "Aviso nuevo OP de Comex";
	public static final String COMEX_MAIL_ASUNTO = "Office Banking - Notificación Comex";

	public static String generarHtmlComex(String empresa, String trr, String importe, String divisa, String concepto, String fecha) {
		String html = "<h2>Aviso | Nueva Orden de Pago.</h2>"
				+ "<table>"
				+ "<tr><td>Cliente: </td><td><b>" + empresa + "</b></td></tr>"
				+ "<tr><td>TRR: </td><td><b>" + trr + "</b></td></tr>"
				+ "<tr><td>Importe: </td><td><b>" + importe + "</b></td></tr>"
				+ "<tr><td>Divisa: </td><td><b>" + divisa + "</b></td></tr>"
				+ "<tr><td>Concepto: </td><td><b>" + concepto + "</b></td></tr>"
				+ "<tr><td>Fecha: </td><td><b>" + fecha + "</b></td></tr>"
				+ "</table>";
		return html;
	}

}
