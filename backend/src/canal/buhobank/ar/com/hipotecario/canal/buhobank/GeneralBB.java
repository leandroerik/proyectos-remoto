package ar.com.hipotecario.canal.buhobank;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes;

public class GeneralBB {

	public static String FLUJO_ONBOARDING = "FLUJO_ONBOARDING";
	public static String FLUJO_LIBERTAD = "FLUJO_LIBERTAD";
	public  static  String FLUJO_TCV = "FLUJO_TCV";
	public static String FLUJO_DEFAULT = FLUJO_TCV;
	public static String FLUJO_INVERSIONES = "FLUJO_INVERSIONES";

	public static String LOGO_FLUJO_ONBOARDING = "https://www.hipotecario.com.ar/media/buhobank/logo-hipotecario.png";
	public static String LOGO_FLUJO_LIBERTAD = "https://www.hipotecario.com.ar/media/buhobank/logos/libertad.png";
	public static String LOGO_FLUJO_INVERSIONES = "https://www.hipotecario.com.ar/media/buhobank/logo-hipotecario.png";

	public static String LOGO_SEC_FLUJO_ONBOARDING = "";
	public static String LOGO_SEC_FLUJO_LIBERTAD = "https://www.hipotecario.com.ar/media/buhobank/logo-hipotecario.png";
	public static String LOGO_SEC_FLUJO_INVERSIONES = "https://www.hipotecario.com.ar/media/buhobank/logo-hipotecario.png";

	public static String COLOR_FLUJO_ONBOARDING = "#F37320";
	public static String COLOR_FLUJO_LIBERTAD = "#E3001B";
	public static String COLOR_FLUJO_INVERSIONES = "#BBA675";

	public static String COLOR_SEC_FLUJO_ONBOARDING = "";
	public static String COLOR_SEC_FLUJO_LIBERTAD = "#303030";
	public static String COLOR_SEC_FLUJO_INVERSIONES = "#F2F1F1";

	public static String CANAL_DEFAULT = "BB";
	public static String VISUALIZA_S = "S";
	public static String VISUALIZA_N = "N";

	public static String GUARDAR_CONTENEDOR_ERROR = "GUARDAR_CONTENEDOR_ERROR";
	public static String REEMPLAZAR_CUIL = "REEMPLAZAR_CUIL";

	public static String ENVIO_MAIL_ALTA_OK = "ENVIO_MAIL_ALTA_OK";
	public static String ENVIO_MAIL_ALTA_ERROR = "ENVIO_MAIL_ALTA_ERROR";

	// alta automatica
	public static String ERROR_RETOMAR_SESION = "ERROR_RETOMAR_SESION";
	public static String ENVIO_MAIL_AVISO_BATCH = "ENVIO_MAIL_AVISO_BATCH";
	public static String ENVIO_MAIL_OK_ALTA_BATCH = "ENVIO_MAIL_OK_ALTA_BATCH";

	public static String ENVIO_MAIL_ERROR_ALTA_BATCH = "ENVIO_MAIL_ERROR_ALTA_BATCH";

	// alertas push
	public static String OK_ENVIO_ALERTA_PUSH = "OK_ENVIO_ALERTA_PUSH";
	public static String ERROR_ENVIO_ALERTA_PUSH = "ERROR_ENVIO_ALERTA_PUSH";

	// proceso stand by
	public static String SESION_STAND_BY_SIN_SELFIES = "SESION_STAND_BY_SIN_SELFIES";
	public static String SESION_STAND_BY_CREADA = "SESION_STAND_BY_CREADA";
	public static String SESION_STAND_BY_VU_OK = "SESION_STAND_BY_VU_OK";
	public static String SESION_STAND_BY_CONTROLAR = "SESION_STAND_BY_CONTROLAR";
	public static String SESION_STAND_BY_CONTROL_OK = "SESION_STAND_BY_CONTROL_OK";
	public static String SESION_STAND_BY_CONTROL_ERROR = "SESION_STAND_BY_CONTROL_ERROR";
	public static String SESION_STAND_BY_BORRADA = "SESION_STAND_BY_BORRADA";

	public static String ERROR_EJEMPLAR_DIFERENTE = "ERROR_EJEMPLAR_DIFERENTE";
	public static String ERROR_FECHA_VENCIMIENTO = "ERROR_FECHA_VENCIMIENTO";
	public static String ERROR_GET_INFORMATION_VU = "ERROR_GET_INFORMATION_VU";
	public static String ERROR_OFERTA_ELEGIDA_VACIA = "ERROR_OFERTA_ELEGIDA_VACIA";

	// telemarketing manual
	public static String TELEMARKETING_MANUAL = "TELEMARKETING_MANUAL";
	public static String TELEMARKETING_MANUAL_POST = "TELEMARKETING_MANUAL_POST";

	public static String ALTA_CON_TELEFONO_PARTICULAR = "ALTA_CON_TELEFONO_PARTICULAR";

	// estados buho inversor
	public static String CUENTA_INVERSOR_ACEPTADA = "1";
	public static String CUENTA_INVERSOR_NO_ACEPTADA = "0";
	public static String SOLICITUD_INVERSOR_ENVIADA = "SOLICITUD_ENVIADA";
	public static String FUERA_DE_HORARIO = "FUERA_DE_HORARIO";

	// fecha
	public static String FORMATO_FECHA_DMY = "dd/MM/yyyy";
	public static String FORMATO_FECHA = "yyyy-MM-dd";
	public static String FORMATO_FECHA_COMPLETA = "yyyy-MM-dd HH:mm:ss";

	// Validacion de app
	public static String VERSION_PLATAFORMA_0_0_1 = "LKnWRBG8r6TINxs2u7dG9AxS8ZUFVlyXUT/8O9VOHY0b+5/jpOcytqeZ92BkeBTGBrDVpCHsbyU=";
	public static String VERSION_PLATAFORMA_0_0_2 = "ci38r5dsCLDKzMBYoCK2sL+foB1FdnSiziBk8gBuFyKuZ5fJvhgIM3L3BxlEtFc2NQ7y7UnXwpg2R4RpcINToA=";
	public static String VERSION_PLATAFORMA_0_0_3 = "2NQ7y7UnXwpg2R4RpcINToA=ci38r5dsCLDKzMBYoCK2sL+foB1FdnSiziBk8gBuFyKuZ5fJvhgIM3L3BxlEtFc";
	public static String NUMERO_VERSION_PLATAFORMA_0_0_1 = "0.0.1";
	public static String NUMERO_VERSION_PLATAFORMA_0_0_2 = "0.0.2";
	public static String NUMERO_VERSION_PLATAFORMA_0_0_3 = "0.0.3";

	public static String CODIGO_ADMIN = "JPocytqeZ92BkeBTGBrDVpCHsbyA=/LEnWRBG8r6TINxs2u7dG9AxS8ZUFVlyXUT/8O9VOHY0b+5";

	public static Integer DIAS_ATRAS_ALTA_BB_INVERSOR_PAQUETES = 20;

	// Horario de procesoS cron
	public static String CRON_ALTA_BATCH_HORA_INICIO = "05";
	public static String CRON_INVERSOR_HORA_INICIO = "08";
	public static String CRON_ALERTA_PUSH_HORA_INICIO = "08";
	public static String CRON_ALERTA_PUSH_HORA_FIN = "22";
	public static Integer MINUTO_PROCESO_ALERTA_PUSH = 30;

	public static String PROCESO_TELEMARKETING = "PROCESO_TELEMARKETING";
	public static String PROCESO_BUHO_INVERSOR = "PROCESO_BUHO_INVERSOR";

	// Días en los cuales se consideran registros para usuarios que abandonaron el
	// flujo
	public static Integer DIAS_RETOMAR_SESION = 2;
	public static Integer DIAS_RETOMAR_CONTACTO = 1;

	// Ultimos dias en los cuales se consideran registros para alta automatica
	public static Integer DIAS_ALTA_AUTOMATICA = 30;

	// Minutos de anticipacion para ver si esta corriendo proceso batch
	public static Integer MINUTOS_ANTICIPACION_BATCH = 10;

	// Intervalo en segundos de proceso automatico de alta para pruebas
	public static Integer INTERVALO_SEG_TEST_CRON = 1200;

	public static String COD_ALERTA_PRIMER_FLUJO_VU = "FVUPRIMER";
	public static String COD_ALERTA_POST_OFERTA_INICIA = "POSTOFERTAINICIA";
	public static String COD_ALERTA_POST_OFERTA_CREDITICIA = "POSTOFERTACREDITICIA";
	public static String COD_ALERTA_PRIMER_FLUJO_VU_MAIL = "FVUPRIMERMAIL";
	public static String COD_ALERTA_POST_OFERTA_INICIA_MAIL = "POSTOFERTAINICIAMAIL";
	public static String COD_ALERTA_POST_OFERTA_CREDITICIA_MAIL = "POSTOFERTACREDITICIAMAIL";

	public static String ESTADO_ENVIO_ALERTA_OK = "ENVIO_OK";
	public static String ESTADO_ENVIO_ALERTA_ERROR = "ENVIO_ERROR";
	public static String ESTADO_ENVIO_ALERTA_DESHABILITADO = "ENVIO_DESHABILITADO";
	public static String TIPO_ALERTA_PUSH = "TIPO_ALERTA_PUSH";
	public static String TIPO_ALERTA_MAIL = "TIPO_ALERTA_MAIL";

	public static String PLATAFORMA_ANDROID = "ANDROID";
	public static String PLATAFORMA_IOS = "IOS";
	public static String URL_STORE_DEFAULT = "https://www.buhobank.com/qr";
	public static String URL_STORE_ANDROID = "https://play.google.com/store/apps/details?id=com.hipotecario.mobile&hl=es";
	public static String URL_STORE_IOS = "https://apps.apple.com/ar/app/banca-m%C3%B3vil-banco-hipotecario/id469262970";

	// Flags del canal
	public static String CANAL_CODIGO = "BB";
	public static Boolean PRENDIDO_OFERTA_MOTOR = true;
	public static Boolean PRENDIDO_EXPIRACION_SESION = true;
	public static Boolean PRENDIDO_MAXIMO_REINTENTOS_POR_SESION = false;
	public static Boolean PRENDIDO_VALIDAR_FALLECIMIENTO = false;
	public static Boolean PRENDIDO_LOGS_BASE = true;

	// Horario Operativo
	public static String IDENTIF_BATCH_CORE = "BATCH_CORE";

	// Sesion
	public static Integer MAXIMO_REINTENTOS = 3;
	public static Integer SESION_EXPIRACION = 1;
	public static Integer OTP_EXPIRACION = 2;
	public static String CODIGO_CELULAR_ARG = "9";

	// Documentacion
	public static String CUIT = "11";
	public static String CUIL = "08";

	public static String TIPO_DOC_NACIONAL = "01";
	public static String TIPO_DOC_EXTRANJERO = "134";

	// Datos basicos
	public static String DEFAULT_EJEMPLAR = "A";
	public static String DEFAULT_PAIS_NACIMIENTO_DESC = "ARGENTINA";
	public static String DEFAULT_NACIONALIDAD_DESC = "ARGENTINO";
	public static String DEFAULT_PAIS_NACIMIENTO = "80";
	public static String DEFAULT_NACIONALIDAD = "80";

	// Emprendedor
	public static List<String> CATEGORIAS = Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
	public static String RESPONSABLE_INSCRIPTO = "NI";

	// Situacion laboral
	public static String CUIT_SITUACION_IMPOSITIVA = "MONO";
	public static String CUIT_IVA = "MONO";
	public static String CUIT_GANANCIAS = "MONO";
	public static String CATEGORIA_MONO = "B";
	public static String TIPO_SIT_LABORAL_M = "MONOTRIBUTISTA";
	public static String TIPO_SIT_LABORAL_A = "AUTONOMO";

	public static String CUIL_SITUACION_IMPOSITIVA = "CONF";
	public static String CUIL_IVA = "CONF";
	public static String CUIL_GANANCIAS = "NORE";
	public static String SITUACION_IMP_AUTONOMOS = "INSC";
	public static String IVA_AUTONOMOS = "INSC";
	public static String DGI_AUTONOMOS = "RINP";

	public static String SITUACION_LABORAL_REL_DEPENDENCIA = "1";
	public static String SITUACION_LABORAL_IND_O_MONO = "6";
	public static String SITUACION_LABORAL_JUBILADO = "11";

	// Estado civil
	public static String CASADA = "C";
	public static String SOLTERA = "S";

	public static String UNION_CIVIL_COMUNIDAD = "Y";
	public static String UNION_CIVIL_SEP_BIENES = "Z";
	public static String SUBESTADO_CIVIL_SOLTERO = "X";

	public static String NUPCIAS_PRIMERAS = "P";
	public static String NUPCIAS_SEGUNDAS = "S";
	public static String NUPCIAS_TERCERAS = "T";
	public static String NUPCIAS_CUARTAS = "C";

	// Subestado civil
	public static String SUB_ESTADO_CIVIL_ID = "Y";
	public static String SUB_ESTADO_CIVIL_DESCRIPCION = "COMUNIDAD GANANCIAS";

	// Forma de entrega
	public static String DEFAULT_SUCURSAL_ASIGNADA = "0";
	public static String ENTREGA_DOMICILIO = "D";
	public static String ENTREGA_SUCURSAL = "S";
	public static String ENTREGA_OTRO_DOMICILIO = "DP";
	public static String ENTREGA_DOM_ALTERNATIVO = "AL";
	public static String ENTREGA_DOM_ANDREANI = "A";
	public static String DOMICILIO_LEGAL = "L";
	public static String DOMICILIO_POSTAL = "P";
	public static String ERROR_GET_INFORMACION_SUCURSALES = "ERROR GET INFORMACION SUCURSALES";


	// SubProducto
	public static String SUB_BUHO_PUNTOS = Paquetes.SUB_CARACT_BUHO_PUNTOS;
	public static String SUB_AEROLINEAS = Paquetes.SUB_CARACT_AEROLINEAS;

	// Standalone
	public static Integer BUHO_INICIA = 0;
	public static Integer CS_FACIL_PACK = 34;
	public static Integer BUHO_CGU = 1;
	public static Integer BUHO_INICIA_HML = 2;
	public static Integer BUHO_INICIA_INVERSOR = 4;
	public static String SUBTIPO_CGU = "1"; // (subtipo para CGU)

	// Paquetes
	public static Integer PAQUETE_BUHO_PACK = 40;
	public static Integer PAQUETE_GOLD = 41;
	public static Integer PAQUETE_PLATINUM = 42;
	public static Integer PAQUETE_BLACK = 43;
	public static Integer PAQUETE_EMPRENDEDOR = 47;
	public static Integer PAQUETE_EMPRENDEDOR_BLACK = 53;

	public static Integer PAQUETE_BUHO_PACK_VIRTUAL = 73;
	public static Integer PAQUETE_GOLD_VIRTUAL = 74;
	public static Integer PAQUETE_PLATINUM_VIRTUAL = 75;
	public static Integer PAQUETE_BLACK_VIRTUAL = 76;
	public static Integer PAQUETE_EMPRENDEDOR_VIRTUAL = 81;
	public static Integer PAQUETE_EMPRENDEDOR_BLACK_VIRTUAL = 82;

	public static Integer CS_FACIL_SUELDO = 34;
	public static Integer CS_PAQUETE_BUHO_PACK = 35;
	public static Integer CS_PAQUETE_GOLD = 36;
	public static Integer CS_PAQUETE_PLATINUM = 37;
	public static Integer CS_PAQUETE_BLACK = 38;

	public static Integer HOMO_NRO_PAQUETE_HML_INTERNACIONAL = 69;
	public static Integer HOMO_NRO_PAQUETE_HML_GOLD = 73;
	public static Integer HOMO_NRO_PAQUETE_HML_PLATINUM = 74;
	public static Integer HOMO_NRO_PAQUETE_HML_SIGNATURE = 75;

	public static Integer PROD_NRO_PAQUETE_HML_INTERNACIONAL = 68;
	public static Integer PROD_NRO_PAQUETE_HML_GOLD = 69;
	public static Integer PROD_NRO_PAQUETE_HML_PLATINUM = 70;
	public static Integer PROD_NRO_PAQUETE_HML_SIGNATURE = 71;

	public static String LETRA_PAQUETE_BUHO_PACK = "I";
	public static String LETRA_PAQUETE_GOLD = "P";
	public static String LETRA_PAQUETE_PLATINUM = "L";
	public static String LETRA_PAQUETE_BLACK = "S";

	public static Integer LIMITE_COMPRA_PAQUETE_BUHO_PACK = 32000;
	public static Integer LIMITE_COMPRA_PAQUETE_GOLD = 96000;
	public static Integer LIMITE_COMPRA_PAQUETE_PLATINUM = 256000;
	public static Integer LIMITE_COMPRA_PAQUETE_BLACK = 384000;

	public static Integer HOMO_PAQUETE_HML_INTERNACIONAL_ID_AFINIDAD = 1394;
	public static Integer HOMO_PAQUETE_HML_GOLD_ID_AFINIDAD = 1395;
	public static Integer HOMO_PAQUETE_HML_SIGNATURE_ID_AFINIDAD = 1398;
	public static Integer HOMO_PAQUETE_HML_PLATINUM_ID_AFINIDAD = 1399;

	public static Integer HOMO_PAQUETE_HML_INTERNACIONAL_COD_DISTRIBUCION = 267;
	public static Integer HOMO_PAQUETE_HML_GOLD_COD_DISTRIBUCION = 269;
	public static Integer HOMO_PAQUETE_HML_SIGNATURE_COD_DISTRIBUCION = 271;
	public static Integer HOMO_PAQUETE_HML_PLATINUM_COD_DISTRIBUCION = 266;

	public static Integer HOMO_PAQUETE_HML_INTERNACIONAL_MOD_LIQUIDACION = 261;
	public static Integer HOMO_PAQUETE_HML_GOLD_MOD_LIQUIDACION = 262;
	public static Integer HOMO_PAQUETE_HML_SIGNATURE_MOD_LIQUIDACION = 263;
	public static Integer HOMO_PAQUETE_HML_PLATINUM_MOD_LIQUIDACION = 259;

	public static Integer PROD_PAQUETE_HML_INTERNACIONAL_ID_AFINIDAD = 1381;
	public static Integer PROD_PAQUETE_HML_GOLD_ID_AFINIDAD = 1382;
	public static Integer PROD_PAQUETE_HML_PLATINUM_ID_AFINIDAD = 1386;
	public static Integer PROD_PAQUETE_HML_SIGNATURE_ID_AFINIDAD = 1385;

	public static Integer PROD_PAQUETE_HML_INTERNACIONAL_COD_DISTRIBUCION = 266;
	public static Integer PROD_PAQUETE_HML_GOLD_COD_DISTRIBUCION = 268;
	public static Integer PROD_PAQUETE_HML_PLATINUM_COD_DISTRIBUCION = 272;
	public static Integer PROD_PAQUETE_HML_SIGNATURE_COD_DISTRIBUCION = 270;

	public static Integer PROD_PAQUETE_HML_INTERNACIONAL_MOD_LIQUIDACION = 261;
	public static Integer PROD_PAQUETE_HML_GOLD_MOD_LIQUIDACION = 262;
	public static Integer PROD_PAQUETE_HML_PLATINUM_MOD_LIQUIDACION = 259;
	public static Integer PROD_PAQUETE_HML_SIGNATURE_MOD_LIQUIDACION = 263;

	// Motor
	public static BigInteger PRIMERA_LLAMADA = new BigInteger("1");

	public static String PUNTO_VENTA = "1";
	public static BigInteger OFICIAL_VENTA = new BigInteger("1");
	public static String CANAL_TRAMITE = "12";
	public static String SUB_CANAL_TRAMITE = "1";
	public static String OFICIAL_TRAMITE = "ES0000";
	public static String MOTOR_TIPO_INVOCACION = "2";
	public static String TIPO_PERSONA_PARTICULAR = "PA";
	public static Integer NRO_SOLICITANTE = 0;
	public static String ROL_SOLICITANTE_TITULAR = "T";
	public static Integer INGRESO_NETO_INT = 286711;
	public static BigDecimal INGRESO_NETO = new BigDecimal("286711.0");

	// Solicitante
	public static BigDecimal MONTO_ALQUILER = new BigDecimal("0.0");
	public static Boolean UNIDO_HECHO_INICIAL = false;

	// Tarjeta solicitada
	public static BigInteger TIPO_TARJETA_SOLICITADA = new BigInteger("2");
	public static BigInteger NRO_TARJETA_SOLICITADA = new BigInteger("0");
	public static BigDecimal MONTO_ALTA_TARJETA_SOLICITADA = new BigDecimal("0.0");
	public static BigDecimal MONTO_SOLICITADO_TARJETA_SOLICITADA = new BigDecimal("0.0");

	// Cuenta solicitada
	public static BigInteger TIPO_CUENTA_SOLICITADA = new BigInteger("4");
	public static BigInteger NRO_CUENTA_SOLICITADA = new BigInteger("0");
	public static BigDecimal MONTO_ALTA_CUENTA_SOLICITADA = new BigDecimal("0.0");

	// Flags motor
	public static Boolean SOLICITA_APROBACION_ESTANDAR_STAND = true;
	public static Boolean SOLICITA_APROBACION_ESTANDAR = false;
	public static Boolean SOLICITA_PRIMERA_COMPRA = false;
	public static Boolean SOLICITA_VALIDAR_IDENTIDAD = false;
	public static Boolean SOLICITA_EVALUAR_MERCADO_ABIERTO = false;
	public static Boolean SOLICITA_COMPROBAR_INGRESOS = false;
	public static Boolean SOLICITA_APROBACION_CENTRALIZADA = false;
	public static Boolean SOLICITA_EXCEPCION_CHEQUEO_FINAL = false;
	public static Boolean ES_SIMULACION = false;
	public static Boolean RUTA_CON_PACTADO = false;
	public static String ES_PLAN_SUELDO = "false";
	public static Boolean ES_PAQUETE = false;
	public static Boolean ES_SUJETO_OBLIGADO = false;

	// Alta solicitud paquete
	public static Boolean EJECUTAR_MOTOR = false;

	// Alta Paquete
	public static String TIPO_DOMICILIO_RESUMEN = "DP";
	public static String ROL_INTEGRANTE_TITULAR = "T";
	public static String USO_CUENTA_LEGAL = "PER";
	public static Boolean REALIZA_TRANSF_CUENTA_LEGAL = false;
	public static String PRODUCTO_BANCARIO_PAQ = "80";
	public static String ORIGEN_CAPTACION_PAQ = "10";
	public static String OFICINA_PAQ = "0";
	public static String OFICIAL_PAQ = "1";
	public static String USO_FIRMA_PAQ = "U";
	public static String PRODUCTO_COBIS_COBRO_PAQ = "4";
	public static String PRODUCTO_BANCARIO_COBRO_PAQ = "3";
	public static Boolean RESUMEN_MAGNETICO_PAQ = true;

	// Alta TC
	public static String TIPO_MAIL_AVISOS_TC = "EMP";
	public static String TELEFONO_TC = "E";
	public static Integer FORMA_PAGO_TC = 6;
	public static String TIPO_CUENTA_TC = "4";
	public static String NUMERO_CUENTA_TC = "0";
	public static String EMPRESA_ASEGURADORA_TC = "40";
	public static String SUCURSAL_CUENTA_TC = "0";
	public static Boolean AVISOS_VIA_MAIL_TC = true;
	public static Boolean AVISOS_CORREO_TC = false;

	// Alta TD
	public static String PRODUCTO_CUENTA_OP_TD = "4";
	public static String CUENTA_CUENTA_OP_TD = "0";
	public static Boolean PRINCIPAL_CUENTA_OP_TD = true;
	public static String FIRMA_CUENTA_OP_TD = "U";
	public static String TIPO_TD = "NC";
	public static String GRUPO_TD = "3";
	public static String TIPO_CUENTA_COMISION_TD = "4";
	public static String NUMERO_CTA_COMISION_TD = "0";

	// Alta CGU
	public static String CATEGORIA_CGU = "CGU";

	// Alta CA ARS
	public static String CATEGORIA_CA_ARS = "D";
	public static String PRODUCTO_BANCARIO_CA_ARS = "3";
	public static String OFICIAL_CA_ARS = "1";
	public static String OFICINA_CA_ARS = "0";
	public static String ORIGEN_CA_ARS = "30";
	public static String USO_FIRMA_CA_ARS = "U";
	public static String CICLO_CA_ARS = "6";
	public static Boolean TRANSFIERE_ACRED_HAB_CA_ARS = false;
	public static Boolean COBRO_PRIMER_MANT_CA_ARS = false;

	// Alta CA ARS Stand
	public static String OFICIAL_CA_ARS_STAND = "1";
	public static String OFICINA_CA_ARS_STAND = "0";
	public static String ORIGEN_CA_ARS_STAND = "30";
	public static String CICLO_CA_ARS_STAND = "6";
	public static Boolean RESUMEN_MAGNETICO_CA_ARS_STAND = false;
	public static Boolean TRANSFIERE_ACRED_HAB_CA_ARS_STAND = false;
	public static Boolean COBRO_PRIMER_MANT_CA_ARS_STAND = false;

	// Alta CA USD
	public static String CATEGORIA_CA_USD = "D";
	public static String CATEGORIA_CA_USD_BINICIA = "SC";
	public static String PRODUCTO_BANCARIO_CA_USD = "3";
	public static String OFICIAL_CA_USD = "1";
	public static String OFICINA_CA_USD = "0";
	public static String ORIGEN_CA_USD = "30";
	public static String USO_FIRMA_CA_USD = "U";
	public static String CICLO_CA_USD = "6";
	public static Boolean TRANSFIERE_ACRED_HAB_CA_USD = false;
	public static Boolean COBRO_PRIMER_MANT_CA_USD = false;

	// Tarjeta ofrecida
	public static Integer MARCA_TC_DUENIOS = 2;

	// Resoluciones PUT
	public static BigDecimal INGRESO_NETO_BB_RESOLUCION = new BigDecimal("286711.0");
	public static String TIPO_INVOCACION_RESOLUCION = "2";
	public static Integer NRO_INSTANCIA_RESOLUCION = 2;
	public static String MOTIVO_EXCEPCION_RESOLUCION = "Verificar resolucion SCORING";

	// Flags Resoluciones PUT
	public static Boolean EXCEPCION_RESOLUCION = true;
	public static Boolean SOLICITA_MONTO_REFUERZO = false;
	public static Boolean SOLICITA_EXCEPCION = false;

	// Felicitaciones
	public static String URL_GENERAR_CLAVE_USUARIO = "https://hb.hipotecario.com.ar/hb/#/adhesion/adhesion";

	// Documentos
	public static String CLASE_DOCUMENTAL_INGRESOS_H = "IngresosH";
	public static String CLASE_DOCUMENTAL_DNI = "DNI";
	public static String CLASE_DOCUMENTAL_FE_VIDA = "FeDeVidaCliente";
	public static String CLASE_DOCUMENTAL_LAVADO_DINERO = "PrevencionLavadoH";
	public static String ORIGEN_ALTA = "BUHOBANK";
	public static String TIPO_PERSONA = "F";
	public static String MIMETYPE = "application/";
	public static String GRUPO_CODIGO = "ONBOARDING";

	// Clave redis
	public static String REDIS_BB_FLUJOS = "REDIS_BB_FLUJOS";
	public static String REDIS_BB_VISTAS = "REDIS_BB_VISTAS";
	public static Integer BB_REDIS_EXPIRACION = 86400; // 1 dia

	public static String BB_REDIS_PAQUETE = "BB_REDIS_PAQUETE";
	public static Integer BB_REDIS_PAQUETE_EXPIRACION = 1728000; // 20 dias

	public static Integer DIAS_LIMPIAR_USUARIO_QA = 150;

	public static String NYP = "NYP";
	public static String SUCURSALES_ANDREANI = "SUCURSALES_ANDREANI";


	public static String CUENTA_SUELDO_INVALIDA = "CUENTA_SUELDO_INVALIDA";

    public static String CODIGO_PRODUCTO_TD = "11";
    public static String CODIGO_PRODUCTO_CAJA_ARS = "8";
    public static String CODIGO_PRODUCTO_CAJA_USD = "9";

}
