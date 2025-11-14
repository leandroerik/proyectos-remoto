package ar.com.hipotecario.mobile.test;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import ar.com.hipotecario.mobile.lib.Archivo;
import ar.com.hipotecario.mobile.lib.Objeto;

public abstract class CuentaCoelsa {

	public static void main(String[] args) throws Exception {
		File folder = new File("C:\\homebanking-dummy\\homologacion\\CuentaCoelsa");
		for (File file : folder.listFiles()) {
			if (file.isFile()) {
				String path = file.getAbsolutePath();
				String json = Archivo.leer(path);
				Objeto objeto = Objeto.fromJson(json);

				StringBuilder sb = new StringBuilder();
				sb.append(objeto.string("cbu")).append(" ");
				sb.append(objeto.string("cuit")).append(" ");
				sb.append(objeto.objetos("cotitulares").size()).append(" ");
				sb.append(objeto.string("nuevoAlias")).append("");
				if (objeto.string("cuit").startsWith("30")) {
					System.out.println(sb.toString());
				}
			}
		}
	}

	public static List<String> lista() {
		String contenido = Archivo.leer("C:\\Users\\C05302\\Desktop\\cbu_otros_bancos.txt");
		List<String> lista = Arrays.asList(contenido.split("\\n"));
		return lista;
	}
}
