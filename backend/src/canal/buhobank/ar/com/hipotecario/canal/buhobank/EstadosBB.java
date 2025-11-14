package ar.com.hipotecario.canal.buhobank;

public class EstadosBB {

	// proceso stand by
	public static String CONTROLAR_USUARIO = "CONTROLAR_USUARIO";
	public static String CONTROL_ERROR = "CONTROL_ERROR";

	// Batch
	public static String BATCH_CORRIENDO = "BATCH_CORRIENDO";
	public static String ERROR_FUERA_DE_SERVICIO = "ERROR_FUERA_DE_SERVICIO";
	public static String PREVIO_BATCH_CORRIENDO = "PREVIO_BATCH_CORRIENDO";

	// POST sesion
	public static String SESION_CREADA = "SESION_CREADA";

	// POST retormarSesion
	public static String SESION_RETOMADA = "SESION_RETOMADA";
	public static String RETOMA_SESION = "RETOMA_SESION";

	// POST guardarVU
	public static String VU_PERSON_OK = "VU_PERSON_OK";

	// POST validar persona
	public static String VALIDAR_PERSONA_OK = "VALIDAR_PERSONA_OK";

	// POST validar persona
	public static String GUARDAR_NACIONALIDAD_OK = "GUARDAR_NACIONALIDAD_OK";

	// POST guardarVUCompleto
	public static String VU_TOTAL_OK = "VU_TOTAL_OK";

	// GET validarDatosPersonales
	public static String VALIDAR_DATOS_PERSONALES_OK = "VALIDAR_DATOS_PERSONALES_OK";
	public static String BB_VALIDAR_DATOS_PERSONALES_OK = "BB_VALIDAR_DATOS_PERSONALES_OK";

	// POST noConfirmarDatos
	public static String NO_CONFIRMA_DATOS = "NO_CONFIRMA_DATOS";

	// GET ofertas
	public static String OFERTAS = "OFERTAS_OBTENIDAS";
	public static String RECHAZO_MOTOR = "RECHAZO_MOTOR";
	public static String RECHAZO_PREVIO_MOTOR = "RECHAZO_PREVIO_MOTOR";

	// POST elegirOferta
	public static String ELEGIR_OFERTA_OK = "ELEGIR_OFERTA_OK";

	// POST formaEntrega
	public static String FORMA_ENTREGA_OK = "FORMA_ENTREGA_OK";

	// POST guardarPostalAlt
	public static String GUARDAR_DOMICILIO_ALTERNATIVO_OK = "GUARDAR_DOMICILIO_ALTERNATIVO_OK";

	// POST guardarDomLegal
	public static String COMPLETAR_DOMICILIO_LEGAL_OK = "COMPLETAR_DOMICILIO_LEGAL_OK";

	// POST guardarAdicionales
	public static String GUARDAR_ADICIONALES_OK = "GUARDAR_ADICIONALES_OK";

	// POST guardarConyuge
	public static String GUARDAR_CONYUGE_OK = "GUARDAR_CONYUGE_OK";

	// GET finalizar
	public static String BB_FINALIZAR_OK = "BB_FINALIZAR_OK";
	public static String FINALIZAR_OK = "FINALIZAR_OK";
	public static String ERROR_FINALIZAR = "ERROR_FINALIZAR";
	public static String BB_ES_INFORMADO = "BB_ES_INFORMADO";

	// SoftToken
	public static String ETAPA_CREAR_STOKEN = "ETAPA_CREAR_STOKEN";
	public static String CREAR_ST_OK = "CREAR_STOKEN_OK";
	public static String ETAPA_GUARDAR_STOKEN = "ETAPA_GUARDAR_STOKEN";

	// Teradata
	public static String CONSULTAR_TERADATA = "CONSULTAR_TERADATA";
	public static String NO_EXISTE_EN_TERADATA = "NO_EXISTE_EN_TERADATA";
	public static String EXISTE_EMPRENDEDOR = "EXISTE_EMPRENDEDOR";

	// Contenedor digital
	public static String ETAPA_POST_ENVIO_A_CONTENEDOR = "ETAPA_ENVIO_A_CONTENEDOR";
}
