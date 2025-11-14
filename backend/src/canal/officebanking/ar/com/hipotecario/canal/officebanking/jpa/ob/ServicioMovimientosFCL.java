package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.List;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.pagos.pagoHaberes.FondoCeseLaboralOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.MovientosFCLOBRepositorio;

public class ServicioMovimientosFCL extends ServicioOB{
	
    private static MovientosFCLOBRepositorio repo;

    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    public ServicioMovimientosFCL(ContextoOB contexto) {
        super(contexto);
        repo = new MovientosFCLOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }
    
	public Futuro<List<FondoCeseLaboralOB>> buscarPorConvenio(int convenio) {
		return futuro(() -> repo.buscarByConvenio(convenio));
	}
	
	public Futuro<FondoCeseLaboralOB> buscarPorSecuencia(long id) {
        return futuro(() -> repo.find(id));
	}
}
