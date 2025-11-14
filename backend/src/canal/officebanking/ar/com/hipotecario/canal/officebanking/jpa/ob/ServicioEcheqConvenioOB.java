package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.eCheq.EcheqConvenioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ECheqConvenioOBRepositorio;

public class ServicioEcheqConvenioOB extends ServicioOB {
    private static ECheqConvenioOBRepositorio repo;
    private static ContextoOB contexto = new ContextoOB("OB", Config.ambiente(), "1");

    public ServicioEcheqConvenioOB(ContextoOB contexto) {
        super(contexto);
        repo = new ECheqConvenioOBRepositorio();
        repo.setEntityManager(this.getEntityManager());
    }

    public Futuro<EcheqConvenioOB> crear(Integer idObEcheq, String convenio) {
    	EcheqConvenioOB echeq = new EcheqConvenioOB();
        echeq.idObEcheq = idObEcheq;
        echeq.convenio = convenio;

        return futuro(() -> {
            try {
                return repo.create(echeq);
            } catch (Exception e) {
                throw new RuntimeException("Error al intentar guardar en la tabla OB_Echeq_Convenio.", e);
            }
        });
    }
	
    public static Futuro<EcheqConvenioOB> find(int id) {
        return futuro(() -> repo.find(id));
    }

    public Futuro<EcheqConvenioOB> update(EcheqConvenioOB echeqConvenioOB) {
        return futuro(() -> repo.update(echeqConvenioOB));
    }

    public Futuro<EcheqConvenioOB> findById(int idCheque){
        return futuro(()-> repo.find(idCheque));
    }
    
    public Futuro<Void> delete(EcheqConvenioOB echeqConvenioOB) {
        return futuro(() -> {
            repo.delete(echeqConvenioOB);
            return null;
        });
    }
    
//    public Futuro<EcheqConvenioOB> findByField(String idCheque){
//        return futuro(()-> repo.findByField(idCheque, "idObEcheq"));
//    }
    
//	public static void main(String[] args) {
//		try {
//			repo = new ECheqConvenioOBRepositorio();
//			ContextoOB contexto = new ContextoOB(null, null, null);
//			SesionOB sesion = new SesionOB();
//			EcheqConvenioOB echeq = new EcheqConvenioOB();
//			echeq.idObEcheq = "1";
//			echeq.convenio = "333";
//			repo.create(echeq);
//			
//		} catch (Exception e) {
//			e.printStackTrace();
//			}
//	}

}
