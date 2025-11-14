package ar.com.hipotecario.canal.officebanking.dto.cobranzaIntegral;

import ar.com.hipotecario.backend.servicio.api.recaudaciones.ReporteCobranzasOB;

import java.util.ArrayList;

public class ReporteCobranzaConvenioDTO {
    public Integer convenio;
    public ArrayList<ReporteCobranzasOB.ReporteCobranza> reporte;

    public ReporteCobranzaConvenioDTO(Integer convenio, ArrayList<ReporteCobranzasOB.ReporteCobranza> reporte) {
        this.convenio = convenio;
        this.reporte = reporte;
    }

    public Integer getConvenio() {
        return convenio;
    }

    public void setConvenio(Integer convenio) {
        this.convenio = convenio;
    }
}
