package ar.com.hipotecario.canal.officebanking;

import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.TextoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.bandejaOperaciones.TipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.comex.CategoriaComexOB;

public class DataProvider {
	
	public static List<CategoriaComexOB> categoriaComexListMock() {
		 TipoProductoFirmaOB tipoProducto = new TipoProductoFirmaOB(1, true, "Comercio Exterior", 101, false);
		 TextoOB textoOB = new TextoOB();

		 CategoriaComexOB categoria1 = new CategoriaComexOB(1, "Categoria 1", textoOB);
		 CategoriaComexOB categoria2 = new CategoriaComexOB(2, "Categoria 2", textoOB);

		 return Arrays.asList(categoria1, categoria2);
	 }
}
