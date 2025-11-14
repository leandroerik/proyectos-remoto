package ar.com.hipotecario.canal.homebanking.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public abstract class Serializador {

	public static byte[] bytes(Object objeto) {
		try {
			ByteArrayOutputStream bos = new ByteArrayOutputStream();
			ObjectOutputStream oos = new ObjectOutputStream(bos);
			oos.writeObject(objeto);
			oos.flush();
			byte[] data = bos.toByteArray();
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static Object object(byte[] bytes) {
		try {
			ByteArrayInputStream in = new ByteArrayInputStream(bytes);
			ObjectInputStream is = new ObjectInputStream(in);
			Object objeto = is.readObject();
			return objeto;
		} catch (IOException | ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
