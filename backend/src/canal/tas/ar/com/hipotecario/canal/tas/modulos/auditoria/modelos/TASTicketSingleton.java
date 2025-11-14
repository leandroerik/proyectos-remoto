package ar.com.hipotecario.canal.tas.modulos.auditoria.modelos;

import java.util.HashMap;
import java.util.List;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASTicketSingleton extends ApiObjeto {

	private static TASTicketSingleton instance = null;

	static private HashMap<String, List> fragmentos;

	// private List fragmentos=null;

	protected TASTicketSingleton() {
		// Exists only to defeat instantiation.
	}

	public static TASTicketSingleton getInstance() {
		if (instance == null) {
			instance = new TASTicketSingleton();
			fragmentos = new HashMap();
		}
		return instance;
	}

	public HashMap<String, List> getFragmentos() {
		return fragmentos;
	}

	public void setFragmentos(HashMap<String, List> fragmentos) {
		this.fragmentos = fragmentos;
	}


}
