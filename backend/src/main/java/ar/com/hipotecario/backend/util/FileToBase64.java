package ar.com.hipotecario.backend.util;

import ar.com.hipotecario.backend.base.Archivo;
import ar.com.hipotecario.backend.base.Base;

public class FileToBase64 extends Base {

	public static void main(String[] args) {
		String carpeta = "C:/Users/C05302/Desktop/Ordenar/fotos2";
		System.out.println(new Archivo(carpeta + "/" + "1-frente.jpg").comprimirImagen(30).base64());
//		System.out.println(new Archivo(carpeta + "/" + "2-dorso.jpg").comprimirImagen(30).base64());
//		System.out.println(new Archivo(carpeta + "/" + "3-neutra.jpg").comprimirImagen(30).base64());
//		System.out.println(new Archivo(carpeta + "/" + "4-ojos-cerrados.jpg").comprimirImagen(30).base64());
//		System.out.println(new Archivo(carpeta + "/" + "5-sonriente.jpg").comprimirImagen(30).base64());
	}
}
