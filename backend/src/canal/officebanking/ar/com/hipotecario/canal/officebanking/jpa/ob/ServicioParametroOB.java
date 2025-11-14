package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.ParametroOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.ParametroOBRepositorio;
import com.github.jknack.handlebars.internal.Param;

public class ServicioParametroOB extends ServicioOB {

	private ParametroOBRepositorio repo;

	public ServicioParametroOB(ContextoOB contexto) {
		super(contexto);
		repo = new ParametroOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<ParametroOB> find(String clave) {
		return futuro(() -> repo.find(clave));
	}

	public Futuro<List<String>> split(String clave) {
		ParametroOB parametro = repo.find(clave);
		String[] valores = parametro.valor.split(",");
		return futuro(() -> Arrays.asList(valores));
	}

	public Futuro<ParametroOB> save(ParametroOB parametro){
		return futuro(()->repo.update(parametro));
	}

}