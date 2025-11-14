package ar.com.hipotecario.canal.tas.shared.utils.models.strings;

public enum TASMensajesString {

    ERROR("ERROR"),
    ESTADO("ESTADO"),
    SIN_RESULTADOS("SIN_RESULTADOS"),
    SIN_PARAMETROS("SIN_PARAMETROS"),
    MULTIPLES_RESULTADOS("MULTIPLES_RESULTADOS"),
    PARAMETROS_INCORRECTOS("PARAMETROS_INCORRECTOS"),
    DESTINO_CUENTA("C"),
    DESTINO_PRESTAMO("P"),
    DESTINO_TARJETA("T"),
    ERROR_SOLICITUD_EN_CURSO("Ya tenés una solicitud de baja en curso."),
    MOTIVO_SOLICITUD_EN_CURSO(" tiene una Solicitud en Curso para su cuenta por lo que no puede realizar la Baja de la Caja de Ahorro por TAS."),
    ERROR_OFICIAL_DE_NEGOCIOS("Para solicitar la baja de la Caja de Ahorro seleccionada acercate a un Oficial de Negocios."),
    MOTIVO_VALIDACION_CUENTA_COMITENTE(" tiene Cuenta Comitente asociada para su cuenta por lo que no puede realizar la Baja de la Caja de Ahorro por TAS."),
    MOTIVO_VALIDACION_VARIOS_TITULARES(" tiene varios Titulares para su cuenta por lo que no puede realizar la Baja de la Caja de Ahorro por TAS."),
    MOTIVO_VALIDACION_BLOQUEO_CUENTA(" tiene un Bloqueo Asociado para su cuenta por lo que no puede realizar la Baja de la Caja de Ahorro por TAS."),
    CAJA_AHORRO_FONDOS_INSUFICIENTES("La caja de ahorros tiene fondos insuficientes."),
    CUENTA_CORRIENTE_FONDOS_INSUFICIENTES("La cuenta corriente tiene fondos insuficientes."),

    TARJETAS_ERROR_VACIO("En este momento no podemos procesar su solicitud.<br>Por favor, intente más tarde.<br>Muchas gracias."),
    TARJETAS_ERROR_SOLICITUD_YA_GENERADA("Ya tenés generada una solicitud en curso para cambiar la forma de pago. Ante cualquier duda comunicate al 0810-222-7777.<br>Muchas gracias."),
    TARJETAS_CAMBIO_PAGO_ERROR_PAGO_TOTAL("El pago total de tu Tarjeta de Crédito está adherido al débito automático de tu cuenta xxx-xxxx-xxxx-"),
    TARJETAS_CAMBIO_PAGO_ERROR_PAGO_MINIMO("El pago minimo de tu Tarjeta de Crédito está adherido al débito automático de tu cuenta xxx-xxxx-xxxx-"),
    TARJETAS_ERROR_FONDOS_INS_CA("La caja de ahorros tiene fondos insuficientes."),
    TARJETAS_ERROR_FONDOS_INS_CC("La cuenta corriente tiene fondos insuficientes."),
    TARJETAS_DEBITO_HAB_PIL_PIN_ERROR("Estimado Cliente,<br><br>" + "En este momento no es posible operar en esta Terminal de Autoservicio." + " Por favor, reintente en unos minutos.<br><br>" + "Disculpe las molestias ocasionadas.<br><br>Muchas gracias."),
    PRESTAMOS_FONDOS_INSUFICIENTES("La cuenta no posee monto mínimo necesario para poder debitar."),
    PLAZO_FIJO_PESOS_LEYENDA("Para depósitos a plazo fijo superiores a $1.000.000, declaro bajo juramento que no soy beneficiario de los \"Créditos a Tasa 24%\" otorgados conforme lo dispuesto por la Comunicación BCRA A 6937 y complementarias (capital de trabajo, pago de sueldos, financiación prestadores de salud) y punto 3 de la Comunicación BCRA A 7006 (clientes no informados en la Central de Deudores) ni \"Créditos a Tasa Subsidiada para Empresas\" previstos en el Decreto Nº 332/2020 y Comunicación BCRA A 7082 punto 1, modificatorias y complementarias. En caso de ser beneficiario de los mencionados créditos, manifiesto conocer y aceptar que no corresponderá aplicar la tasa de interés mínima prevista en el punto 1.11.1 de las normas sobre \"Depósitos e inversiones a plazo\"."),
    PLAZO_FIJO_CLIENTE_SIN_PERFIL_PATRIMONIAL("Para continuar con la gestión, necesitamos actualizar tus datos y trazar tu perfil transaccional. Por favor, acercate a un Oficial de Negocios. <br><br>Muchas gracias."),
    PLAZO_FIJO_CUENTA_SIN_SALDO("La cuenta no posee saldo disponible"),
    PLAZO_FIJO_NO_EXISTE_OFICIAL("No es posible realizar la operación. Por favor acercate a un Oficial de Negocios.<br><br>Muchas gracias."),
    COMPRA_VENTA_USD_EXCEDE_CUPO("Estás intentando comprar más de USD 99.999.<br> Según la Com. \"A\" 8085, para avanzar con la compra podés: <br>-Ingresar un monto menor.<br>-Autorizar la operación en una sucursal (La operación demora 48hs. hábiles, una vez hecho esto podrás continuar con la compra.)"),
    COMPRA_VENTA_USD_INHABILITADA ("No tenés habilitada esta operación por Comunicación A 7105/6 B.C.R.A"),
    COMPRA_VENTA_USD_FERIADO ("El horario para realizar esta operación es de 6 a 21 hs. en días hábiles."),
    CIERRE_LOTE_ERROR("Fallo la invocacion al servicio de cierre Tasi"),
    CIERRE_LOTE_SUCCESS("Invocacion a servicio de cierre Tas Inteligente realizado correctamente"),
    
    TAS_COMISIONES_CARGOS_TASAS_NN_0("<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\" style=\"font-size:19px\"><b>COMISIONES</b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisi\u00F3n mensual por Mantenimiento de Cuenta</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder10</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Tasa de Inter\u00E9s-Saldos mayores a $100.- /USD 100</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder11</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisi\u00F3n por certificaci\u00F3n de firmas</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder12</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisiones mensuales saldos inmovilizados</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder13</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisi\u00F3n por Rechazo de cheque por otros bancos</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder14</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisi\u00F3n por C\u00E1mara Federal Uniforme <br>(Cheque remesa)</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder15</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-8\">Comisi\u00F3n por acreditaci\u00F3n DEBIN</div> \r\n" + //
                "<div class=\"col-sm-4\">placeholder16</div> \r\n" + //
                "</div> \r\n" + //
                "<br> "),
    TAS_COMISIONES_CARGOS_TASAS_NN_1("\r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\" style=\"font-size:19px\"><b>TRANSFERENCIAS POR CANALES ELECTR\u00D3NICOS </b></div> \r\n" + //
                "</div> \r\n" + //
                "<br> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\"><b>TOPE POR CUENTA Y TARJETA</b></div> \r\n" + //
                "<div class=\"col-sm-3\"><b>TOPE DIARIO (POR CUENTA Y TARJETA)</b> </div> \r\n" + //
                "<div class=\"col-sm-3\"><b>TOPE MENSUAL (POR CUENTA Y TARJETA)</b></div> \r\n" + //
                "<div class=\"col-sm-3\"><b>COSTO</b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Entre cuentas de Banco Hipotecario</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope </div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder17</div> \r\n" + //
                "</div> \r\n" + //
                "<br> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\"><b>HOME BANKING </b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Transferencias otras cuentas BH</div> \r\n" + //
                "<div class=\"col-sm-3\">Hasta $ 350.000.- / USD 12.500. </div> \r\n" + //
                "<div class=\"col-sm-3\">S/tope en pesos / USD 25.000</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder18</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Transferencias diferidas</div> \r\n" + //
                "<div class=\"col-sm-3\">Hasta $ 350.000.- / USD 12.500. </div> \r\n" + //
                "<div class=\"col-sm-3\">S/tope en pesos / USD 25.000</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder19</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Transferencias inmediatas</div> \r\n" + //
                "<div class=\"col-sm-3\">Hasta $ 350.000.- / USD 12.500. </div> \r\n" + //
                "<div class=\"col-sm-3\">S/tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder20</div> \r\n" + //
                "</div> \r\n" + //
                "<br> "),
    TAS_COMISIONES_CARGOS_TASAS_NN_2("<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\"><b>CAJEROS AUTOM\u00C1TICOS </b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Transferencias inmediatas (*)</div> \r\n" + //
                "<div class=\"col-sm-3\">Hasta $ 125.000.- / USD 5000</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder21</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Transferencias Diferidas</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder22</div> \r\n" + //
                "</div> \r\n" + //
                "<br> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\"><b>TRANSFERENCIAS POR SUCURSAL</b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">Entre cuentas de Banco Hipotecario</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder23</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">A otros bancos (v\u00EDa CBU)</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder24</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-3\">A otros bancos (v\u00EDa MEP)</div> \r\n" + //
                "<div class=\"col-sm-3\">Sin tope</div> \r\n" + //
                "<div class=\"col-sm-3\">\t$ 0.00</div> \r\n" + //
                "<div class=\"col-sm-3\">placeholder25</div> \r\n" + //
                "</div> \r\n" + //
                "<br> "),
    TAS_COMISIONES_CARGOS_TASAS_NN_3("\r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\" style=\"font-size:19px\"><b>TARJETA DE D\u00C9BITO</b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">Comisi\u00F3n por emisi\u00F3n de tarjeta de d\u00E9bito</div> \r\n" + //
                "<div class=\"col-sm-6\">placeholder26</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">Cargo por reposici\u00F3n por robo y/o hurto y/o p\u00E9rdida.</div> \r\n" + //
                "<div class=\"col-sm-6\">placeholder27</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">Cargo por reposici\u00F3n por deterioro y/o desmagnetizaci\u00F3n</div> \r\n" + //
                "<div class=\"col-sm-6\">Hasta una (1) por au00F1o sin cargo / a partir de la 2da reposici\u00F3n placeholder28</div> \r\n" + //
                "</div> \r\n" + //
                "<br> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-12\" style=\"font-size:19px\"><b>CANTIDAD DE TRANSACCIONES LIBRES POR CAJEROS AUTOM\u00C1TICOS</b></div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">ATM propios de BH</div> \r\n" + //
                "<div class=\"col-sm-6\">Ilimitadas</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">ATM Red Link de otros bancos</div> \r\n" + //
                "<div class=\"col-sm-6\">0</div> \r\n" + //
                "</div> \r\n" + //
                "<div class=\"row\"> \r\n" + //
                "<div class=\"col-sm-6\">ATM Red Banelco</div> \r\n" + //
                "<div class=\"col-sm-6\">0</div> \r\n" + //
                "</div> \r\n" + //
                "<br> "),
                TAS_COMISIONES_CARGOS_TASAS_NN_4("<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-12\" style=\"font-size:19px\"><b>COMISI\u00D3N POR TRANSACCIONES EN CAJEROS AUTOM\u00C1TICOS</b></div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-6\">Transacciones excedentes por Red Link</div> \r\n" + //
                                        "<div class=\"col-sm-6\">placeholder29</div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-6\">Transacciones excedentes por Red Banelco</div> \r\n" + //
                                        "<div class=\"col-sm-6\">placeholder30</div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-6\">Transacciones en Redes del exterior en cajeros de Uruguay</div> \r\n" + //
                                        "<div class=\"col-sm-6\">placeholder31</div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-6\">Transacciones en Redes del exterior Plus, Cirrus e Ita\u00FA</div> \r\n" + //
                                        "<div class=\"col-sm-6\">placeholder32</div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<br> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-12\">(*)El l\u00EDmite diario es 3 veces el l\u00EDmite de extracci\u00F3n con un tope de $50.000 por Tarjeta de d\u00E9bito. No hay tope mensual.</div> \r\n" + //
                                        "</div> \r\n" + //
                                        "<div class=\"row\"> \r\n" + //
                                        "<div class=\"col-sm-12\">(**) Se debitar\u00E1 la comisi\u00F3n seg\u00FAn su equivalente en pesos.</div> \r\n" + //
                                        "</div> ");



    
    private final String tipoMensaje;

    TASMensajesString(String tipoMensaje) {
        this.tipoMensaje = tipoMensaje;
    }

    public String getTipoMensaje() {
        return tipoMensaje;
    }

    public static String error() {
        return TASMensajesString.ERROR.toString();
    }

    public static String sinResultados() {
        return TASMensajesString.SIN_RESULTADOS.toString();
    }

    public static String sinParametros() {
        return TASMensajesString.SIN_PARAMETROS.toString();
    }

    public static String parametrosIncorrectos() {
        return TASMensajesString.PARAMETROS_INCORRECTOS.toString();
    }

    public static String multipleResultados() {
        return TASMensajesString.MULTIPLES_RESULTADOS.tipoMensaje.toString();
    }

    public static String estado() {
        return TASMensajesString.ESTADO.toString();
    }

    public static String errorSolicitudEnCurso(){return TASMensajesString.ERROR_SOLICITUD_EN_CURSO.toString();}

    public static String errorOficialDeNegocio(){return TASMensajesString.ERROR_OFICIAL_DE_NEGOCIOS.toString();}

}
