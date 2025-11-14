package ar.com.hipotecario.mobile.test;

import ar.com.hipotecario.mobile.lib.Encriptador;

public abstract class Desencriptador {

	public static void main(String[] args) throws InterruptedException {
		String valor = "ENC(RBPrWMjcD472b63WCA4caCTWLSbIJXBCqShFpLvJYkqnTLb/4Yd6cT6T4Gn4J/w7af+2HTdyq+OZkACAJLDsEl0X6ssJ4rWDDBzrlvqRs9xw21YALtJJzQ==)";
		if (valor != null && valor.startsWith("ENC(") && valor.endsWith(")")) {
			valor = Encriptador.desencriptarPBE(valor);
			System.out.println(valor);
		}

		String encriptar = "jdbc:jtds:sqlserver://desamssql19:1145/ST_Vhist;cachemetadata=true;prepareSQL=3";
		String encriptado = Encriptador.encriptarPBE(encriptar);
		System.out.println(encriptado);
	}
}
