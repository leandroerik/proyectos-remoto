package ar.com.hipotecario.canal.buhobank;

public class ErroresBB {

	// POST retomar Sesion por abandonos
	public static String NO_EXISTE_PERSONA = "NO_HUBO_ABANDONO";
	public static String SIN_REGISTROS_DE_SESION = "SIN_REGISTROS_DE_SESION";
	public static String DNI_O_IP_INVALIDOS = "DNI_O_IP_INVALIDOS";
	public static String NO_SE_PUDO_RETOMAR_SESION = "NO_SE_PUDO_RETOMAR_SESION";

	// POST guardarVU
	public static String ETAPA_VU = "VU";
	public static String PERSON_VU_VACIA = "PERSON_VU_VACIA";
	public static String OCR_VU_VACIO = "OCR_VU_VACIO";
	public static String DATA_VU_VACIA = "DATA_VU_VACIA";
	public static String PERSONA_NO_ENCONTRADA = "PERSONA_NO_ENCONTRADA";
	public static String PERSONA_FALLECIDA = "PERSONA_FALLECIDA";
	public static String DOCUMENTO_INVALIDO = "DOCUMENTO_INVALIDO";
	public static String DOCUMENTO_DIFERENTE = "DOCUMENTO_DIFERENTE";
	public static String PERSONA_DIFERENTE = "PERSONA_DIFERENTE";

	// POST validar persona

	public static String ETAPA_VALIDAR_PERSONA = "VALIDAR_PERSONA";
	public static String COD_PERSONA_NO_ENCONTRADA = "12";
	public static String COD_VALIDAR_PERSONA_OK = "0";
	public static String COD_VALIDAR_PERSONA_FIRMADA = "99";
	public static String COD_DATOS_INCORRECTOS = "3";
	public static String ERROR_INTERNO = "ERROR_INTERNO";
	public static String DATOS_INCORRECTOS = "DATOS_INCORRECTOS";
	public static String MAXIMO_REINTENTOS = "MAXIMO_REINTENTOS";
	public static String SERVICIO_DESHABILITADO = "SERVICIO_DESHABILITADO";

	// POST guardar nacionalidad
	public static String ETAPA_GUARDAR_NACIONALIDAD = "GUARDAR_NACIONALIDAD";

	// GET validarDatosPersonales
	public static String ETAPA_VALIDAR_DATOS_PERSONALES = "VALIDAR_DATOS_PERSONALES";
	public static String FECHA_NACIMIENTO_VACIA = "FECHA_NACIMIENTO_VACIA";
	public static String MENOR_DE_EDAD = "MENOR_DE_EDAD";
	public static String DATOS_VACIOS_VU = "DATOS_VACIOS_VU";

	// GET Obtener codigo de área
	public static String ETAPA_CODIGO_AREA = "CODIGO_INVALIDO";

	// GET ofertas
	public static String ETAPA_OFERTAS = "OBTENCION_OFERTAS";
	public static String PAQUETE_NO_ENCONTRADO = "PAQUETE_NO_ENCONTRADO";
	public static String STANDALONE_NO_ENCONTRADO = "STANDALONE_NO_ENCONTRADO";
	// POST elegirOferta
	public static String ETAPA_ELEGIR_OFERTA = "ELEGIR_OFERTA";

	// GET validarFormaEntrega
	public static String ETAPA_VALIDAR_FORMA_ENTREGA = "ETAPA_VALIDAR_FORMA_ENTREGA";
	public static String DOMICILIO_LEGAL_VACIO = "DOMICILIO_LEGAL_VACIO";

	// POST formaEntrega
	public static String ETAPA_FORMA_ENTREGA = "FORMA_ENTREGA";
	public static String TIPO_INVALIDO = "TIPO_INVALIDO";

	// POST guardarPostalAlt
	public static String ETAPA_DOMICILIO_ALTERNATIVO = "DOMICILIO_ALTERNATIVO";
	public static String NUEVO_DOMICILIO_NULO = "NUEVO_DOMICILIO_NULO";
	public static String CODIGO_POSTAl_DOM_POSTAL_INVALIDO = "CODIGO_POSTAl_DOM_POSTAL_INVALIDO";

	// POST guardarDomLegal
	public static String ETAPA_DOMICILIO_LEGAL = "ETAPA_DOMICILIO_LEGAL";
	public static String DOMICILIO_LEGAL_NULO = "DOMICILIO_LEGAL_NULO";
	public static String CODIGO_POSTAl_DOM_LEGAL_INVALIDO = "CODIGO_POSTAl_DOM_LEGAL_INVALIDO";

	// POST guardaradicionales
	public static String ETAPA_ADICIONALES = "DATOS_ADICIONALES";

	// GET finalizar
	public static String ETAPA_FINALIZAR = "FINALIZAR";
	public static String SOLICITUD_NO_CREADA = "SOLICITUD_NO_CREADA";
	public static String SOLICITUD_VACIA = "SOLICITUD_VACIA";
	public static String INTEGRANTE_VACIO = "INTEGRANTE_VACIO";
	public static String INTEGRANTE_COTITULAR_VACIO = "INTEGRANTE_COTITULAR_VACIO";
	public static String CAJA_AHORRO_VACIA = "CAJA_AHORRO_VACIA";
	public static String TARJETA_DEBITO_VACIA = "TARJETA_DEBITO_VACIA";
	public static String RESOLUCION_VACIO_O_RECHAZADA = "RESOLUCION_VACIO_O_RECHAZADA";
	public static String RESOLUCION_VACIA = "RESOLUCION_VACIA";
	public static String CAJA_AHORRO_NO_ACTUALIZADA = "CAJA_AHORRO_NO_ACTUALIZADA";
	public static String TARJETA_DEBITO_NO_ACTUALIZADA = "TARJETA_DEBITO_NO_ACTUALIZADA";
	public static String ERROR_FINALIZAR_SOLICITUD = "ERROR_FINALIZAR_SOLICITUD";
	public static String ERROR_SERVICIO = "ERROR_SERVICIO";
	public static String RESOLUCION_RECHAZADA = "RESOLUCION_RECHAZADA";
	public static String PAQUETE_VACIO = "PAQUETE_VACIO";
	public static String ERROR_PROCESO_BB_INVERSOR = "ERROR_PROCESO_BB_INVERSOR";
	public static String ERROR_USUARIO_STAND_BY = "ERROR_USUARIO_STAND_BY";
	public static String ERROR_PROCESO_BATCH = "ERROR_PROCESO_BATCH";
	public static String ERROR_TRANSMIT = "ERROR_TRANSMIT";

	// guardarPersona
	public static String PERSONA_NO_GUARDADA = "PERSONA_NO_GUARDADA";
	public static String DATOS_INCOMPLETOS = "DATOS_INCOMPLETOS";
	public static String DATOS_INVALIDOS = "DATOS_INVALIDOS";

	// terminarRelaciones
	public static String RELACION_NO_TERMINADA = "RELACION_NO_TERMINADA";

	// guardarConyuge
	public static String CONYUGE_VACIO = "CONYUGE_VACIO";
	public static String CONYUGE_INVALIDO = "CONYUGE_INVALIDO";
	public static String CONYUGE_INVALIDO_POR_NACIONALIDAD = "CONYUGE_INVALIDO_POR_NACIONALIDAD";
	public static String PERSONA_CONYUGE_NO_GUARDADA = "PERSONA_CONYUGE_NO_GUARDADA";

	// guardarRelacionConyuge
	public static String RELACIONES_NO_ENCONTRADAS = "RELACIONES_NO_ENCONTRADAS";

	// guardarDomicilios
	public static String DOM_LEGAL_NO_GUARDADO = "DOM_LEGAL_NO_GUARDADO";
	public static String DOM_POSTAL_NO_GUARDADO = "DOM_POSTAL_NO_GUARDADO";

	// guardarMail
	public static String EMAIL_INVALIDO = "EMAIL_INVALIDO";
	public static String EMAIL_NO_GUARDADO = "EMAIL_NO_GUARDADO";

	// guardarTelefono
	public static String TELEFONO_CELULAR_NO_GUARDADO = "TELEFONO_CELULAR_NO_GUARDADO";
	public static String TELEFONO_PARTICULAR_NO_GUARDADO = "TELEFONO_PARTICULAR_NO_GUARDADO";

	// terminarActividades
	public static String ACTIVIDAD_NO_CREADA = "ACTIVIDAD_NO_CREADA";

	// guardarCobis
	public static String ERROR_YA_CLIENTE = "ERROR_YA_CLIENTE";
	public static String ERROR_PERSONA = "ERROR_PERSONA";
	public static String ERROR_RELACIONES = "ERROR_RELACIONES";
	public static String ERROR_CONYUGE = "ERROR_CONYUGE";
	public static String ERROR_RELACION_CONYUGE = "ERROR_RELACION_CONYUGE";
	public static String ERROR_DOMICILIO = "ERROR_DOMICILIO";
	public static String ERROR_DOMICILIO_CONYUGE = "ERROR_DOMICILIO_CONYUGE";
	public static String ERROR_MAIL = "ERROR_MAIL";
	public static String ERROR_TELEFONO = "ERROR_TELEFONO";
	public static String ERROR_ACTIVIDADES = "ERROR_ACTIVIDADES";

	// Archivos
	public static String ARCHIVO_INVALIDO = "ERROR_ARCHIVO_INVALIDO";
	public static String CUIL_VACIO = "ERROR_CUIL_VACIO";
	public static String TIPO_ARCHIVO_VACIO = "ERROR_TIPO_ARCHIVO_VACIO";
	public static String ARCHIVO_NO_GUARDADO = "ARCHIVO_NO_GUARDADO";
	public static String LEGAJO_NO_GUARDADO = "LEGAJO_NO_GUARDADO";

	// SoftToken
	public static String ERROR_API = "ERROR_API_MOBILE_ST";
	public static String ERROR_GUARDAR_ST = "ERROR_PERSISTIR_ST";

	// Remarketing chatbot
	public static String ERROR_GET_DATOS = "ERROR_GET_DATOS";


	// Contenedor Digital
	public static String SOLICITUD_NO_ACTUALIZADA = "SOLICITUD_NO_ACTUALIZADA";

	// Transmit
	public static String ERROR_DENY = "ERROR_DENY";
	public static String ERROR_CHALLENGE = "ERROR_CHALLENGE";
	public static String SIN_SESSION_TOKEN = "ERROR_SIN_SESSION_TOKEN";
}
