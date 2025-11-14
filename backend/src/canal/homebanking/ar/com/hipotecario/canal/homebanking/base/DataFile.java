package ar.com.hipotecario.canal.homebanking.base;

public class DataFile {

	/* ========== ATTRIBUTES ========== */
	public String name;
	public byte[] bytes;

	/* ========== INSTANCE ========== */
	public DataFile(String name, byte[] bytes) {
		this.name = name;
		this.bytes = bytes;
	}
}
