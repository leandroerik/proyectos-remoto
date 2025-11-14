package ar.com.hipotecario.mobile.test;

import java.io.BufferedReader;
import java.io.FileReader;

public abstract class ExtractorCBU {

	private static String origen = "C:\\Users\\C05302\\Desktop\\agendatransferencias_20200205180942.txt";

	public static void main(String[] args) throws Exception {
		String linea;
		try (BufferedReader buffer = new BufferedReader(new FileReader(origen))) {
			while ((linea = buffer.readLine()) != null) {
				if (linea.length() > 34) {
					System.out.println(linea.substring(12, 34));
				}
			}
		}
	}
}
