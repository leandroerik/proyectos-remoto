package ar.com.hipotecario.mobile.conector;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.mobile.lib.Objeto;

public class SqlResponseMB {

	/* ========== ATRIBUTOS ========== */
	public Boolean hayError = false;
	public List<Objeto> registros = new ArrayList<>();
	public Objeto registro;
    public Objeto getRegistro() {
        return registro;
    }
    
    public void setRegistro(Objeto registro) {
        this.registro = registro;
    }
}
