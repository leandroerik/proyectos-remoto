package ar.com.hipotecario.canal.officebanking.jpa.ob;

import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.TextosOBRepositorio;
import ar.com.hipotecario.backend.base.Futuro;
import java.util.List;


public class ServicioTextosOB extends ServicioOB{

		private TextosOBRepositorio repo;
		
		public ServicioTextosOB(ContextoOB contexto) {
			super(contexto);
			repo = new TextosOBRepositorio();
			repo.setEntityManager(this.getEntityManager());
		}
		
		public Futuro<TextoOB> find(String idFront){
			return futuro(() -> repo.findByFieldUnique("idFront", idFront));
		}
}
