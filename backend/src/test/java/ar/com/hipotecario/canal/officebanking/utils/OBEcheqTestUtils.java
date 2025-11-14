package ar.com.hipotecario.canal.officebanking.utils;

import ar.com.hipotecario.backend.servicio.api.cheques.ListadoChequesOB;

import java.util.ArrayList;

 public class OBEcheqTestUtils {

    public static ListadoChequesOB getListadoChequesOBCargado1Cheque(){
        ListadoChequesOB listado = new ListadoChequesOB();
        listado.result = listado.new result();
        ListadoChequesOB.cheques cheque = listado.new cheques();
        cheque.cuenta_emisora = listado.new cuenta_emisora();

        cheque.cheque_id = "cheque123";
        cheque.numero_chequera = "numero123";
        cheque.monto = 1000.00;
        cheque.cheque_modo = "0";
        cheque.cheque_caracter = "a la orden";
        cheque.cheque_motivo_pago = "Pago de servicios";
        cheque.cheque_concepto = "Concepto de prueba";
        cheque.fecha_pago = "2023-10-28T12:00:00";
        cheque.cuenta_emisora.emisor_cuit = "2";
        cheque.cheque_tipo = "TipoCheque";
        listado.result.cheques = new ArrayList<>();
        listado.result.cheques.add(cheque);
        return listado;
    }
}
