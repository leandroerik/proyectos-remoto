package ar.com.hipotecario.canal.officebanking;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.sql.SqlHB_BE;

public class LogCrmOB extends Modulo {

    public static void evento(ContextoOB contexto, int empresa,int usuario,String operacion,String usuario_crm) {
        try {
                SqlHB_BE.logCrm(contexto, empresa, usuario, operacion, usuario_crm).get();

        } catch (Exception ex) {
            System.out.println("error de base de datos: " + ex.getMessage());
        }
    }

}
