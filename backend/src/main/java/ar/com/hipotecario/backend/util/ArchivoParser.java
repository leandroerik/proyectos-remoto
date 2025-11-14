package ar.com.hipotecario.backend.util;

import ar.com.hipotecario.canal.officebanking.OBErrorMessage;

public class ArchivoParser {

	/**
	 * Loger.
	 */
//	private static final Logger log = LoggerFactory.getLogger(DateUtils.class);

	public static final String DATE_TYPE = "Date";
	public static final String NUMBER_TYPE = "Number";
	public static final String LONG_TYPE = "Long";
	public static final String STRING_TYPE = "String";

	/**
	 * Ultima linea procesada.
	 */
	private String lastLine = "";

	public String getLastLine() {
		return lastLine;
	}

	public void setLastLine(String lastLine) {
		this.lastLine = lastLine;
	}

	/**
	 * Mensajes de Error para ser mostrados.
	 * 
	 * @param message
	 * @param parametros
	 * @return
	 */
	public OBErrorMessage setError(String message, Object[] parametros) {
		OBErrorMessage errorMessage = new OBErrorMessage();
		errorMessage.setBundleMessage(message);
		errorMessage.setParametros(parametros);
		return errorMessage;
	}

	/**
	 * Valida el tamano de la linea de registro a procesar.
	 * 
	 * @param totalTamLinea
	 * @param linea
	 * @param numLinea
	 * @param reg
	 * @return
	 */
	/*
	 * public Boolean validoTamanoLineaRegistro(Integer totalTamLinea, String linea,
	 * int numLinea, RegistroImportado reg ) {
	 * 
	 * if (linea==null){ linea = ""; }
	 * 
	 * if (linea.length() > totalTamLinea) { List<ErrorMessage> errores = new
	 * ArrayList<ErrorMessage>(); errores.add(setError("error.maxlenght", new
	 * Object[] { numLinea, totalTamLinea }));
	 * 
	 * reg.setCorrupted(true); reg.setErrores(errores); reg.setNumLinea(numLinea);
	 * return false; } return true; }
	 */

	/**
	 * Procesa los campos de la linea de texto con la lista de definiciones de los
	 * campos.
	 * 
	 * @param numLinea
	 * @param registro
	 * @param textLine
	 * @param errores
	 * @param campos
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 */
	/*
	 * public void procesarLineaCampos(Integer numLinea, RegistroImportado registro,
	 * char[] textLine, List<ErrorMessage> errores, List<Field> campos) throws
	 * IllegalAccessException, InvocationTargetException { for (int i = 0; i <
	 * campos.size(); i++) {
	 * 
	 * // Si hay un error en la columna de linea de archivo este corta todo el
	 * proceso. if (hasErros(registro,numLinea,errores)){ break; }
	 * 
	 * Field campo = campos.get(i);
	 * 
	 * int count = campo.getSize().intValue(); String token=""; if (textLine.length
	 * >= campo.getFrom() - 1 + count) token= new String(textLine, campo.getFrom() -
	 * 1, count);
	 * 
	 * // Filler if (GenericValidator.isBlankOrNull(token)) { if
	 * (campo.getMandatory().equals(Boolean.TRUE)) {
	 * errores.add(setError("error.dynamic.field.null", new Object[] { numLinea,
	 * campo.getDescription() })); continue; } continue; }
	 * 
	 * // date field if (campo.getType().equals(DATE_TYPE)) {
	 * 
	 * if (campo.getMandatory()) {
	 * 
	 * if (GenericValidator.isBlankOrNull(token)) {
	 * errores.add(setError("error.dynamic.field.null", new Object[] { numLinea,
	 * campo.getDescription() }));
	 * 
	 * continue;
	 * 
	 * }
	 * 
	 * }
	 * 
	 * if (token.equals(campo.getDefaultValue())) { // fecha por defecto
	 * GregorianCalendar gc = new GregorianCalendar(1900, Calendar.JANUARY, 1);
	 * gc.set(Calendar.HOUR_OF_DAY, 0); gc.set(Calendar.MINUTE, 0);
	 * gc.set(Calendar.SECOND, 0); gc.set(Calendar.MILLISECOND, 0);
	 * asignarValorPropiedadDate(registro, campo.getMethodName(), gc.getTime());
	 * continue; }
	 * 
	 * // vacio if (token.trim().equals(campo.getDefaultValue())) {
	 * 
	 * asignarValorPropiedadDate(registro, campo.getMethodName(), null); continue; }
	 * 
	 * if (!GenericValidator.isDate(token, campo.getFormat(), true)) {
	 * errores.add(setError( "error.dynamic.field.date", new Object[] { numLinea,
	 * campo.getDescription(), campo.getFormat() }));
	 * 
	 * continue; }
	 * 
	 * Date date = DateUtils.esFormatoValido(campo.getFormat(), token,
	 * Locale.ENGLISH);
	 * 
	 * if (date == null) { errores.add(setError( "error.dynamic.field.date", new
	 * Object[] { numLinea, campo.getDescription(), campo.getFormat() })); continue;
	 * }
	 * 
	 * if (date.before(DateUtils.FECHA_DEFAULT)) {
	 * errores.add(setError("error.fechas.rango", new Object[] { numLinea,
	 * campo.getDescription() })); continue; }
	 * 
	 * asignarValorPropiedadDate(registro, campo.getMethodName(), date);
	 * 
	 * continue;
	 * 
	 * }
	 * 
	 * // Numerico if (campo.getType().equals(NUMBER_TYPE)) {
	 * 
	 * // no requerido if (!campo.getMandatory()) {
	 * 
	 * // se permiten espacios if (campo.getDefaultValue().equals(token.trim())) {
	 * continue; } }
	 * 
	 * BigDecimal numero = null;
	 * 
	 * try {
	 * 
	 * String digitos = token.substring(0, campo.getScale()); String decimales =
	 * token.substring(campo.getScale(), token.length());
	 * 
	 * StringBuffer sb = new StringBuffer(); sb.append(digitos); sb.append(".");
	 * sb.append(decimales);
	 * 
	 * numero = new BigDecimal(sb.toString());
	 * 
	 * if ((campo.getMinValue().doubleValue() > numero .doubleValue()) ||
	 * (campo.getMaxValue().doubleValue() < numero .doubleValue())) { throw new
	 * NumberFormatException(); }
	 * 
	 * } catch (NumberFormatException e) {
	 * 
	 * errores.add(setError("error.dynamic.field.numeric", new Object[] { numLinea,
	 * campo.getDescription(), campo.getMinValue().toString(),
	 * campo.getMaxValue().toString() })); continue;
	 * 
	 * }
	 * 
	 * asignarValorPropiedadBigDecimal(registro, campo.getMethodName(), numero);
	 * 
	 * continue;
	 * 
	 * }
	 * 
	 * // Numerico if (campo.getType().equals(LONG_TYPE)) {
	 * 
	 * if (campo.getMandatory()) {
	 * 
	 * if (GenericValidator.isBlankOrNull(token)) {
	 * 
	 * errores.add(setError("error.dynamic.field.null", new Object[] { numLinea,
	 * campo.getDescription() }));
	 * 
	 * continue;
	 * 
	 * } }else if (GenericValidator.isBlankOrNull(token)) continue;
	 * 
	 * Long numero = null;
	 * 
	 * try {
	 * 
	 * 
	 * 
	 * numero = new Long(token.trim());
	 * 
	 * // Cuando est declarado el valor por defecto, // y es distinto al numero
	 * lanza exception. if ((campo.getDefaultValue() != null) && (numero.longValue()
	 * != Long.parseLong(campo .getDefaultValue()))) {
	 * errores.add(setError("error.dynamic.field", new Object[] { numLinea,
	 * campo.getDescription(), campo.getDefaultValue() }));
	 * 
	 * continue; }
	 * 
	 * // Si el numero est fuera del rango Min y Max lanza exception.
	 * 
	 * // si el valor minimo esta seteado y el numero es menor if
	 * (campo.getMinValue()!=null && campo.getMinValue().longValue() >
	 * numero.longValue()){ throw new NumberFormatException("El valor del campo " +
	 * campo.getName() + " es inferior al minimo permitido"); }
	 * 
	 * // el el valor maximo esta seteado y el numero es mayor if
	 * (campo.getMaxValue()!=null && campo.getMaxValue().longValue() <
	 * numero.longValue()){ throw new NumberFormatException("El valor del campo " +
	 * campo.getName() + " es superior al maximo permitido"); }
	 * 
	 * } catch (NumberFormatException e) { log.error(e.getMessage(),e);
	 * errores.add(setError("error.dynamic.field.numeric", new Object[] { numLinea,
	 * campo.getDescription(), campo.getMinValue().toString(),
	 * campo.getMaxValue().toString() }));
	 * 
	 * continue;
	 * 
	 * }
	 * 
	 * asignarValorPropiedadLong(registro, campo.getMethodName(), numero);
	 * 
	 * continue;
	 * 
	 * }
	 * 
	 * // Numerico if (campo.getType().equals(STRING_TYPE)) {
	 * 
	 * // constante if (campo.getConstant() != null) {
	 * 
	 * if (!campo.getConstant().equals(token)) {
	 * 
	 * errores.add(setError( "error.dynamic.field", new Object[] { numLinea,
	 * campo.getDescription(), campo.getConstant() }));
	 * 
	 * continue; } }
	 * 
	 * if (campo.getMandatory()) {
	 * 
	 * if (GenericValidator.isBlankOrNull(token)) {
	 * 
	 * errores.add(setError("error.dynamic.field.null", new Object[] { numLinea,
	 * campo.getDescription() }));
	 * 
	 * }
	 * 
	 * } asignarValorPropiedadString(registro, campo.getMethodName(), token.trim());
	 * } } }
	 */
	/**
	 * Si hay un error se marca en el registro.
	 * 
	 * @param registro
	 * @param numLinea
	 * @param errores
	 * @return
	 */
	/*
	 * private Boolean hasErros(RegistroImportado registro, Integer numLinea ,
	 * List<ErrorMessage> errores){ Boolean result = Boolean.FALSE; if (
	 * errores.size() > 0){ registro.setCorrupted(true);
	 * registro.setErrores(errores); registro.setNumLinea(numLinea); result =
	 * Boolean.TRUE; } return result; }
	 * 
	 */
	/**
	 * Asigna el valor a una propiedad de un objeto.
	 * 
	 * @param registro
	 * @param nombrePropiedad
	 * @param valor
	 */
	/*
	 * private void asignarValorPropiedadLong(RegistroImportado registro, String
	 * nombrePropiedad, Long valor) { // Si es una propiedad anidada.
	 * if(nombrePropiedad.contains(".")){ PropertyUtilsBean beanUtilsProperties =
	 * new PropertyUtilsBean(); try { // Guardo el valor en la propiedad anidada.
	 * beanUtilsProperties.setNestedProperty(registro, nombrePropiedad, valor); }
	 * catch (Exception e) { log.
	 * error(" Error al asignar un valor a una propiedad anidada en el parser de archivo : "
	 * + e); } // Caso opuesto }else{ // Solo asigno el valor a la propiedad. try {
	 * BeanUtils.setProperty(registro, nombrePropiedad, valor); } catch (Exception
	 * e) { log.
	 * error(" Error al asignar un valor a una propiedad en el parser de archivo : "
	 * + e); } } }
	 * 
	 * /** Asigna el valor a una propiedad de un objeto.
	 * 
	 * @param registro
	 * 
	 * @param nombrePropiedad
	 * 
	 * @param valor
	 */
	/*
	 * private void asignarValorPropiedadBigDecimal(RegistroImportado registro,
	 * String nombrePropiedad, BigDecimal valor) { // Si es una propiedad anidada.
	 * if(nombrePropiedad.contains(".")){ PropertyUtilsBean beanUtilsProperties =
	 * new PropertyUtilsBean(); try { // Guardo el valor en la propiedad anidada.
	 * beanUtilsProperties.setNestedProperty(registro, nombrePropiedad, valor); }
	 * catch (Exception e) { log.
	 * error(" Error al asignar un valor a una propiedad anidada en el parser de archivo : "
	 * + e); } // Caso opuesto }else{ // Solo asigno el valor a la propiedad. try {
	 * BeanUtils.setProperty(registro, nombrePropiedad, valor); } catch (Exception
	 * e) { log.
	 * error(" Error al asignar un valor a una propiedad en el parser de archivo : "
	 * + e); } } }
	 * 
	 * /** Asigna el valor a una propiedad de un objeto.
	 * 
	 * @param registro
	 * 
	 * @param nombrePropiedad
	 * 
	 * @param valor
	 */
	/*
	 * private void asignarValorPropiedadDate(RegistroImportado registro, String
	 * nombrePropiedad, Date valor) { if(valor != null){ // Si es una propiedad
	 * anidada. if(nombrePropiedad.contains(".")){ PropertyUtilsBean
	 * beanUtilsProperties = new PropertyUtilsBean(); try { // Guardo el valor en la
	 * propiedad anidada. beanUtilsProperties.setNestedProperty(registro,
	 * nombrePropiedad, valor); } catch (Exception e) { log.
	 * error(" Error al asignar un valor a una propiedad anidada en el parser de archivo : "
	 * + e); } // Caso opuesto }else{ // Solo asigno el valor a la propiedad. try {
	 * BeanUtils.setProperty(registro, nombrePropiedad, valor); } catch (Exception
	 * e) { log.
	 * error(" Error al asignar un valor a una propiedad en el parser de archivo : "
	 * + e); } } } }
	 * 
	 * /** Asigna el valor a una propiedad de un objeto.
	 * 
	 * @param registro
	 * 
	 * @param nombrePropiedad
	 * 
	 * @param valor
	 */
	/*
	 * private void asignarValorPropiedadString(RegistroImportado registro, String
	 * nombrePropiedad, String valor){
	 * 
	 * // Si es una propiedad anidada. if(nombrePropiedad.contains(".")){
	 * PropertyUtilsBean beanUtilsProperties = new PropertyUtilsBean(); try { //
	 * Guardo el valor en la propiedad anidada.
	 * beanUtilsProperties.setNestedProperty(registro, nombrePropiedad, valor); }
	 * catch (Exception e) { log.
	 * error(" Error al asignar un valor a una propiedad anidada en el parser de archivo : "
	 * + e); } // Caso opuesto }else{ // Solo asigno el valor a la propiedad. try {
	 * BeanUtils.setProperty(registro, nombrePropiedad, valor); } catch (Exception
	 * e) { log.
	 * error(" Error al asignar un valor a una propiedad en el parser de archivo : "
	 * + e); } } }
	 * 
	 * 
	 * 
	 * 
	 * 
	 * 
	 * /** Exporta un linea de texto registro con la definicion de campos pasada.
	 * 
	 * @param definitionItem
	 * 
	 * @param registro
	 * 
	 * @return Linea de texto con los campos concatenados en base a la definicion
	 * pasada de campos.
	 */
	/*
	 * public String exportLine(DefinitionItem definitionItem, RegistroImportado
	 * registro) { return exportLine(definitionItem, registro, "", false); }
	 * 
	 * /** Genera una linea de regitro de caracteres en base a la definicion pasada.
	 * 
	 * @param definitionItem Definicion de cada campo del registro a generar en una
	 * linea de texto.
	 * 
	 * @param registro Registro con los valores de los campos.
	 * 
	 * @param lineSeparator Separador de campos en la linea de registro a crear.
	 * 
	 * @param truncate Especifica si el valor es blanqueado con un valor default
	 * definido.
	 * 
	 * @return Linea de caracteres que representa un registro.
	 */
	/*
	 * public String exportLine(DefinitionItem definitionItem, RegistroImportado
	 * registro, String lineSeparator, boolean truncate) {
	 * 
	 * List<Field> campos = definitionItem.getFieldList();
	 * 
	 * String linea = null; for (Field campo : campos) { String value = "";
	 * 
	 * if (campo.getConstant() != null) { value = campo.getConstant(); } else {
	 * 
	 * Date fecha = null; try { java.lang.reflect.Field field =
	 * (java.lang.reflect.Field) registro
	 * .getClass().getDeclaredField(campo.getMethodName());
	 * field.setAccessible(true); value =
	 * field.get(registro)!=null?field.get(registro).toString():"";
	 * 
	 * if (field.getType()== Date.class){ fecha = field.get(registro)!=null? (Date)
	 * field.get(registro):null; }
	 * 
	 * } catch (Exception e) {
	 * 
	 * }
	 * 
	 * // parseo segun tipo de dato if (campo.getType().equals(DATE_TYPE)) { // Si
	 * tenemos una fecha. if (!value.equals("")){ value =
	 * DateUtils.getFormatoFecha(campo.getFormat(), fecha); // Si no tenemos fecha.
	 * } else if (value.equals("")){ // Al no haber valor lleno a la izquierda con
	 * la definicion especificada. value = StringUtils.leftPad(value,
	 * campo.getSize(), campo.getCaracterCompletar()); } } else if
	 * (campo.getType().equals(NUMBER_TYPE)) {// Scale="13" // Precision="2" String
	 * pEntera = StringUtils.leftPad( !value.isEmpty()? value.split("\\.")[0] : "",
	 * campo.getScale(), "0"); String pDecimal = StringUtils.rightPad(
	 * !value.isEmpty()? value.split("\\.")[1] : "", campo.getPrecision(), "0");
	 * value = pEntera + pDecimal; } else if (campo.getType().equals(LONG_TYPE)) {
	 * if (campo.getCaracterCompletar() != null) { // lleno a la izquierda value =
	 * StringUtils.leftPad(value, campo.getSize(), campo.getCaracterCompletar()); }
	 * } else if (campo.getType().equals(STRING_TYPE)){ if
	 * (campo.getCaracterCompletar() != null) { // lleno a la derecha value =
	 * StringUtils.rightPad(value, campo.getSize(), campo.getCaracterCompletar()); }
	 * } else { // tipo desconocido log.warn("Mapeo de campo [" + campo.getName() +
	 * "] tipo de dato desconocido : [" + campo.getType() + "]"); if
	 * (campo.getCaracterCompletar() != null) { value = StringUtils.rightPad(value,
	 * campo.getSize(), campo.getCaracterCompletar()); } } }
	 * 
	 * // Agrego valor a la linea ignorando cualquier valor anterior. if (truncate){
	 * value = value.trim(); }
	 * 
	 * linea = (linea==null) ? value : linea + lineSeparator + value; } return
	 * linea; }
	 */

}
