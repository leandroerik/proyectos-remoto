package ar.com.hipotecario.canal.officebanking.cron;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Cron;
import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.canal.officebanking.AzureBlobStorageManager;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.LogOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioArchivoRendicionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioCobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioDebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.CobranzaIntegralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.cobranzaIntegral.DebinLoteOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debitoDirecto.DebitoDirectoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ServicioDebinLoteOB;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

public class CronOBCargarConveniosConRendiciones extends Cron.CronJob{

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    private static final String CRON = CronOBCargarConveniosConRendiciones.class.getSimpleName().toUpperCase();

    @Override
    public void run() {
        System.out.println("TEST-CRON");
        boolean ejecutar = contexto.config.bool("ob_cron_ejecutar_cargar_convenios_rendiciones",true);
        if (!ejecutar) {
            LogOB.evento(contexto, "CRON_DESHABILITADO_EJEC_CONFIG", "", CRON);
            return;
        }
        System.out.println("TEST-CRON EJECUTANDO OK");
        LogOB.evento(contexto, "INICIO", LocalDateTime.now().toString(), CRON);

        ServicioDebitoDirectoOB servicioDebitoDirectoOB = new ServicioDebitoDirectoOB(contexto);
        ServicioCobranzaIntegralOB servicioCobranzaIntegralOB = new ServicioCobranzaIntegralOB(contexto);
        ServicioDebinLoteOB servicioDebinLoteOB = new ServicioDebinLoteOB(contexto);

        Fecha diaHabilAnterior = ApiCatalogo.diaBancario(contexto, Fecha.hoy()).get().diaHabilAnterior;
        LocalDate dateDiaHabilAnterior = LocalDate.of(diaHabilAnterior.año(),diaHabilAnterior.mes(), diaHabilAnterior.dia());
        List<DebitoDirectoOB> debitosCargadosEnDiaHabilAnterior = servicioDebitoDirectoOB.buscarPorFechaCreacion(dateDiaHabilAnterior).get();
        List<CobranzaIntegralOB> cobranzaCargadaEnDiaHabilAnterior = servicioCobranzaIntegralOB.buscarPorFechaCreacion(dateDiaHabilAnterior).get();
        List<DebinLoteOB> debinLoteCargadaEnDiaHabilAnterior = servicioDebinLoteOB.buscarPorFechaCreacion(dateDiaHabilAnterior).get();
        LogOB.evento(contexto,"traerDebitosCargadosEnDiaHabilAnterior", String.valueOf(debitosCargadosEnDiaHabilAnterior.size()));
        LogOB.evento(contexto,"traerCobranzaCargadosEnDiaHabilAnterior", String.valueOf(cobranzaCargadaEnDiaHabilAnterior.size()));
        LogOB.evento(contexto,"traerDebinLoteCargadosEnDiaHabilAnterior", String.valueOf(debinLoteCargadaEnDiaHabilAnterior.size()));
        HashMap<String, String> convenioGCRDebito = new HashMap<>();
        HashMap<String, String> convenioGCRCobranza = new HashMap<>();
        debitosCargadosEnDiaHabilAnterior.forEach(debito->convenioGCRDebito.put(debito.convenio.toString(),debito.gcr));
        cobranzaCargadaEnDiaHabilAnterior.forEach(cobranza->convenioGCRCobranza.put( cobranza.convenio.toString(),cobranza.gcr));
        debinLoteCargadaEnDiaHabilAnterior.forEach(debinLote->convenioGCRCobranza.put(debinLote.convenio.toString(),debinLote.gcr==null?"NOTIENE":debinLote.gcr));
        String dia = diaHabilAnterior.dia().toString().length()==1?"0"+diaHabilAnterior.dia():diaHabilAnterior.dia().toString();
        String mes = diaHabilAnterior.mes().toString().length()==1?"0"+diaHabilAnterior.mes():diaHabilAnterior.mes().toString();
        AzureBlobStorageManager az = new AzureBlobStorageManager(contexto);
        if (convenioGCRDebito.size()>0){
            System.out.println("TEST-CRON HAY DEBITO");
            List<String> conveniosConArchivo = az.obtenerConveniosConArchivoEnFecha(contexto,convenioGCRDebito,diaHabilAnterior.año()+"-"+mes+"-"+dia,contexto.config.string("ob_ruta_consulta_rendiciones_dd"));
            LogOB.evento(contexto,"Convenios de DD encontrados en azure", String.valueOf(conveniosConArchivo.size()));
            if (conveniosConArchivo.size()>0){
                ServicioArchivoRendicionesOB servicioArchivoRendicionesOB = new ServicioArchivoRendicionesOB(contexto);
                conveniosConArchivo.forEach(convenio->servicioArchivoRendicionesOB.crear(convenio,dateDiaHabilAnterior, EnumTipoProductoOB.DEBITO_DIRECTO));
            }
        }
        if (convenioGCRCobranza.size()>0){
            System.out.println("TEST-CRON HAY CI");
            List<String> conveniosConArchivo = az.obtenerConveniosConArchivoEnFecha(contexto,convenioGCRCobranza,diaHabilAnterior.año()+"-"+diaHabilAnterior.mes()+"-"+dia,contexto.config.string("ob_ruta_consulta_rendiciones_ci"));
            LogOB.evento(contexto,"Convenios de CI encontrados en azure", String.valueOf(conveniosConArchivo.size()));
            if (conveniosConArchivo.size()>0){
                ServicioArchivoRendicionesOB servicioArchivoRendicionesOB = new ServicioArchivoRendicionesOB(contexto);
                conveniosConArchivo.forEach(convenio->servicioArchivoRendicionesOB.crear(convenio,dateDiaHabilAnterior, EnumTipoProductoOB.COBRANZA_INTEGRAL));
            }
        }




    }
}
