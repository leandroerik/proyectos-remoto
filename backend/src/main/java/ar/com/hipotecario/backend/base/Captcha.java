package ar.com.hipotecario.backend.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;

import javax.imageio.ImageIO;

import cn.apiclub.captcha.Captcha.Builder;
import cn.apiclub.captcha.backgrounds.GradiatedBackgroundProducer;
import cn.apiclub.captcha.gimpy.RippleGimpyRenderer;
import cn.apiclub.captcha.text.producer.DefaultTextProducer;

public class Captcha {

	/* ========== ATRIBUTOS ========== */
	public String texto;
	public byte[] imagen;

	/* ========== METODOS ========== */
	public static Captcha generar(Integer length, Integer width, Integer height) {
		Builder builder = new Builder(width, height);
		builder.addText(new DefaultTextProducer(length, "abcdefghkmnprwxy2345678".toCharArray()));
		builder.addBackground(new GradiatedBackgroundProducer());
		builder.addBorder();
		builder.addNoise();
		builder.gimp(new RippleGimpyRenderer());
		cn.apiclub.captcha.Captcha builded = builder.build();

		byte[] imagen;
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			ImageIO.write(builded.getImage(), "jpg", baos);
			baos.flush();
			imagen = baos.toByteArray();
			baos.close();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		Captcha captcha = new Captcha();
		captcha.texto = builded.getAnswer();
		captcha.imagen = imagen;
		return captcha;
	}

	public Archivo archivo() {
		return new Archivo("captcha.jpg", imagen);
	}

	public String base64() {
		String base64 = Base64.getEncoder().encodeToString(imagen);
		return base64;
	}
}
