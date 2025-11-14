package ar.com.hipotecario.canal.officebanking.enums;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public enum EnumPerfilInversorOB {
	CONSERVADOR(1), MODERADO(2), ARRIESGADO(3), OPERA_BAJO_SU_PROPIO_RIESGO(4), NO_REQUIERE_PERFIL(5);

	private Integer codigo;

	private static final Map<Integer, EnumPerfilInversorOB> lookup = new HashMap<Integer, EnumPerfilInversorOB>();
	static {
		for (EnumPerfilInversorOB s : EnumSet.allOf(EnumPerfilInversorOB.class))
			lookup.put(s.getCodigo(), s);
	}

	EnumPerfilInversorOB(Integer codigo) {
		this.codigo = codigo;
	}

	public Integer getCodigo() {
		return codigo;
	}

	public static EnumPerfilInversorOB get(int code) {
		return lookup.get(code);
	}

}