package ar.com.hipotecario.canal.officebanking.cron;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron;
import ar.com.hipotecario.canal.officebanking.AzureBlobStorageManager;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioArchivoRendicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ParametroOB;
import ar.com.hipotecario.canal.officebanking.util.StringUtil;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class CronOBCargaInicialArchivos extends Cron.CronJob {

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBCargaInicialArchivos.class.getSimpleName().toUpperCase();
    @Override
    public void run() {
        boolean ejecutar = contexto.config.bool("ob_cron_cargar_convenios_inicial",true);
        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }
        ServicioParametroOB servicioParametroOB = new ServicioParametroOB(contexto);
        ParametroOB correrCron = servicioParametroOB.find("reca.correr.cron").get();
        LogOB.evento(contexto,"valor de 'reca.correr.cron'", correrCron.valor);
        System.out.println("correrCron.valor = " + correrCron.valor);
        if (correrCron.valor.equals("1")){
            AzureBlobStorageManager azureBlobStorageManager = new AzureBlobStorageManager(contexto);
            HashMap<String, HashSet<String>> fechasConArchivoDD = azureBlobStorageManager.obtenerConveniosConArchivoInicial(contexto, contexto.config.string("ob_ruta_consulta_rendiciones_dd"));
            LogOB.evento(contexto,"CRON Inicial rendiciones", "Se buscan en azure rendiciones DD");
            HashMap<String, HashSet<String>> fechasConArchivoCI = azureBlobStorageManager.obtenerConveniosConArchivoInicial(contexto, contexto.config.string("ob_ruta_consulta_rendiciones_ci"));
            LogOB.evento(contexto,"CRON Inicial rendiciones", "Se buscan en azure rendiciones CI");
            ServicioArchivoRendicionesOB servicioArchivoRendicionesOB = new ServicioArchivoRendicionesOB(contexto);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            fechasConArchivoDD.forEach((fecha,convenios)->{
                convenios.forEach(convenio->servicioArchivoRendicionesOB.crear(StringUtil.eliminarCerosIzquierda(convenio),LocalDate.parse(fecha,formatter), EnumTipoProductoOB.DEBITO_DIRECTO));
            });
            LogOB.evento(contexto,"CRON Inicial rendiciones", "Finaliza persistencia rendiciones DD");
            fechasConArchivoCI.forEach((fecha,convenios)->{
                convenios.forEach(convenio->servicioArchivoRendicionesOB.crear(StringUtil.eliminarCerosIzquierda(convenio),LocalDate.parse(fecha,formatter), EnumTipoProductoOB.COBRANZA_INTEGRAL));
            });
            LogOB.evento(contexto,"CRON Inicial rendiciones", "Finaliza persistencia rendiciones CI");
            correrCron.valor = "0";
            servicioParametroOB.save(correrCron);
            LogOB.evento(contexto,"Se actualiza 'reca.correr.cron'", correrCron.valor);
        }

    }
}
