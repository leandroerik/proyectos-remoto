package ar.com.hipotecario.backend.util;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class TasasUtil {

	/* ========== METODOS ========== */
	public static BigDecimal tea(Double tna) {
		return tea(new BigDecimal(tna));
	}

	// TODO EMA: esta formula la saque de internet, convierte una tna en tea,
	// ¿podrás validar que funcione?
	public static BigDecimal tea(BigDecimal tna) {
		try {
			BigDecimal tea = tna.setScale(10, RoundingMode.HALF_EVEN);
			tea = tea.divide(new BigDecimal("100"), RoundingMode.HALF_EVEN);
			tea = tea.divide(new BigDecimal("365"), RoundingMode.HALF_EVEN);
			tea = tea.add(new BigDecimal("1")).pow(365).subtract(new BigDecimal("1")).multiply(new BigDecimal("100"));
			tea = tea.setScale(2, RoundingMode.HALF_EVEN);
			return tea;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
