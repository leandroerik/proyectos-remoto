package ar.com.hipotecario.backend.base;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.Type;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Pattern;

import javax.imageio.ImageIO;

import org.apache.commons.codec.binary.Base64;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.TypeAdapter;
import com.google.gson.TypeAdapterFactory;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * @author Gabriel Suarez
 */
public class Util {

    /* ========== ATRIBUTOS ESTATICOS ========== */
    private static Gson gson = gson(true);
    private static SecureRandom secureRandom = new SecureRandom();

    /* ========== RANDOM ========== */
    public static Integer random(Integer minimo, Integer maximo) {
        return secureRandom.nextInt(maximo - minimo) + minimo;
    }

    @SafeVarargs
    public static <T> T random(T... valores) {
        Integer minimo = 0;
        Integer maximo = valores.length - 1;
        Integer random = random(minimo, maximo);
        return valores[random];
    }

    /* ========== STRING ========== */
    public static String format(String texto, Object... parametros) {
        return String.format(texto, parametros);
    }

    public static StringBuilder append(StringBuilder stringBuilder, String texto, Object... parametros) {
        return stringBuilder.append(String.format(texto, parametros));
    }

    /* ========== DATE ========== */
    public static String date(Date date, String formato) {
        try {
            return new SimpleDateFormat(formato).format(date);
        } catch (Exception e) {
            return "";
        }
    }

    /* ========== INTEGER ========== */
    public static Integer integer(String str) {
        Integer valor = 0;
        if (empty(str))
            return valor;

        try {
            valor = Integer.parseInt(str);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return valor;
    }

    public static Integer documento(String str) {
        String regex = "^([0-9]{8})$";

        if (empty(str))
            return 0;

        if (!Pattern.compile(regex).matcher(str).matches())
            str = str.replaceAll("[^0-9]", "");

        return Integer.parseInt(str);
    }

    /* ========== FUTUROS ========== */
    public static <T> Futuro<T> futuro(Callable<T> funcion) {
        return new Futuro<>(funcion);
    }

    public static <K, V> Futuros<K, V> futuros(Iterable<K> collection, Function<K, V> funcion) {
        return new Futuros<>(collection, funcion);
    }

    /* ========== COLLECTIONS ========== */
    @SafeVarargs
    public static <T extends Object> List<T> list(T... valores) {
        List<T> list = new ArrayList<>();
        for (int i = 0; i < valores.length; ++i) {
            list.add(valores[i]);
        }
        return list;
    }

    @SafeVarargs
    public static <T extends Object> Set<T> set(T... valores) {
        Set<T> set = new LinkedHashSet<>();
        for (int i = 0; i < valores.length; ++i) {
            set.add(valores[i]);
        }
        return set;
    }

    @SafeVarargs
    public static <T> T firstNonNull(T... valores) {
        for (int i = 0; i < valores.length; ++i) {
            if (valores[i] != null) {
                return valores[i];
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T> T firstNonEmpty(T... valores) {
        for (int i = 0; i < valores.length; ++i) {
            if (valores[i] != null && !valores[i].toString().isEmpty()) {
                if (!(valores[i] instanceof Fecha) || !((Fecha) valores[i]).isNull()) {
                    return valores[i];
                }
            }
        }
        return null;
    }

    @SafeVarargs
    public static <T> T lastNonNull(T valor, T... valores) {
        T respuesta = valor;
        for (int i = 0; i < valores.length; ++i) {
            if (valores[i] != null) {
                respuesta = valores[i];
            }
        }
        return respuesta;
    }

    @SafeVarargs
    public static <T> T lastNonNull(T... valores) {
        T respuesta = null;
        for (int i = 0; i < valores.length; ++i) {
            if (valores[i] != null) {
                respuesta = valores[i];
            }
        }
        return respuesta;
    }

    /* ========== EMPTY ========== */
    public static Boolean empty(Object... objetos) {
        Boolean empty = false;
        for (Object objeto : objetos) {
            empty |= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty()) || (objeto instanceof Fecha && ((Fecha) objeto).isNull());
        }
        return empty;
    }

    public static Boolean anyEmpty(Object... objetos) {
        Boolean anyEmpty = false;
        for (Object objeto : objetos) {
            anyEmpty |= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty());
        }
        return anyEmpty;
    }

    public static Boolean allEmpty(Object... objetos) {
        Boolean allEmpty = true;
        for (Object objeto : objetos) {
            allEmpty &= (objeto == null) || (objeto instanceof String && ((String) objeto).isEmpty());
        }
        return allEmpty;
    }

    /* ========== JSON ========== */
    public static Gson gson(Boolean formatear) {
        GsonBuilder gsonBuilder = gsonBuilder();
        if (formatear) {
            gsonBuilder.setPrettyPrinting();
        }
        gsonBuilder.serializeNulls();
        return gsonBuilder.create();
    }

    public static GsonBuilder gsonBuilder() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapterFactory(new TypeAdapterFactory() {
            @SuppressWarnings("unchecked")
            public <T> TypeAdapter<T> create(Gson gson, TypeToken<T> type) {
                if (type.getRawType() == Fecha.class) {
                    return (TypeAdapter<T>) new FechaAdapter();
                }
                return null;
            }
        });
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer());
        gsonBuilder.registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer());
        return gsonBuilder;
    }

    public static String toJson(Object object) {
        return gson.toJson(object);
    }

    public static <T> T fromJson(String json, Class<T> clase) {
        return gson.fromJson(json, clase);
    }

    public static <T> T clonar(Object objeto, Class<T> clase) {
        Gson gson = new Gson();
        String json = gson.toJson(objeto);
        T t = gson.fromJson(json, clase);
        return t;
    }

    static class LocalDateTimeDeserializer implements JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return LocalDateTime.parse(json.getAsString());
        }
    }

    static class LocalDateTimeSerializer implements JsonSerializer<LocalDateTime> {
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        }
    }

    /* ========== EXCEPTION ========== */
    public static Throwable getCause(Throwable t) {
        while (t.getCause() != null) {
            t = t.getCause();
        }
        return t;
    }
	public static String ddMMyyyy(String fecha, String formato) {
		if (fecha == null || fecha.isEmpty()) return "";
		Objeto obj = new Objeto();
		obj.set("fechaAux", fecha);
		obj.set("fecha", obj.date("fechaAux", formato, "dd/MM/yyyy"));
		return obj.string("fecha");
	}

    public static StackTraceElement stackTraceElement(Throwable t) {
        t = getCause(t);
        StackTraceElement[] ste = t.getStackTrace();
        StackTraceElement st = null;
        for (Integer i = 0; i < ste.length; ++i) {
            if (ste[i].getClassName().startsWith("ar.com.hipotecario")) {
                st = ste[i];
                break;
            }
        }
        return st;
    }

    /* ========== GSON ADAPTER ========== */
    private static class FechaAdapter extends TypeAdapter<Fecha> {
        public void write(JsonWriter out, Fecha value) throws IOException {
            out.value(value != null ? value.toString() : null);
        }

        public Fecha read(JsonReader in) throws IOException {
            if (in.peek() != JsonToken.NULL) {
                String valor = in.nextString();
                Fecha fecha = Fecha.nunca();
                String formato = formatoFecha(valor);
                fecha = formato.equals("xxxx-xx-xx") ? new Fecha(valor, "yyyy-MM-dd") : fecha;
                fecha = formato.startsWith("xxxx-xx-xxTxx:xx:xx") ? new Fecha(valor, "yyyy-MM-dd'T'HH:mm:ss") : fecha;
                fecha = formato.startsWith("xxxx-xx-xx xx:xx:xx") ? new Fecha(valor, "yyyy-MM-dd HH:mm:ss") : fecha;
                return fecha;
            }
            in.nextNull();
            return Fecha.nunca();
        }
    }

    public static String formatoFecha(String fecha) {
        Integer len = fecha.length();
        char[] chars = new char[len];
        Set<Character> set = set('0', '1', '2', '3', '4', '5', '6', '7', '8', '9');
        for (Integer i = 0; i < len; ++i) {
            Character c = fecha.charAt(i);
            chars[i] = set.contains(c) ? 'x' : c;
        }
        String formato = String.valueOf(chars);
        return formato;
    }

    public static String substring(String texto, Integer cantidad) {
        if (texto == null || texto.isEmpty()) {
            return "";
        } else {
            return texto.substring(0, Math.min(cantidad, texto.length()));
        }
    }

    public static String mergeImagesBase64(String image1Base64, String image2Base64) {

        try {

            // Decodificar las imágenes base64
            byte[] image1Bytes = Base64.decodeBase64(image1Base64);
            byte[] image2Bytes = Base64.decodeBase64(image2Base64);

            // Convertir los bytes en BufferedImage
            ByteArrayInputStream image1Stream = new ByteArrayInputStream(image1Bytes);
            BufferedImage image1 = ImageIO.read(image1Stream);

            ByteArrayInputStream image2Stream = new ByteArrayInputStream(image2Bytes);
            BufferedImage image2 = ImageIO.read(image2Stream);

            // Obtener las dimensiones de las imágenes
            int width1 = image1.getWidth();
            int height1 = image1.getHeight();
            int width2 = image2.getWidth();
            int height2 = image2.getHeight();

            // Crear una nueva imagen con las dimensiones adecuadas para combinar las dos
            // imágenes
            int newWidth = width1 + width2;
            int newHeight = Math.max(height1, height2);
            BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_RGB);

            // Combinar las imágenes en la nueva imagen
            newImage.createGraphics().drawImage(image1, 0, 0, null);
            newImage.createGraphics().drawImage(image2, width1, 0, null);

            // Convertir la nueva imagen a base64
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(newImage, "png", outputStream);
            byte[] combinedImageBytes = outputStream.toByteArray();
            String combinedImageBase64 = Base64.encodeBase64String(combinedImageBytes);

            return combinedImageBase64;

        } catch (Exception e) {

        }

        return null;
    }

    public static void delay(long tiempoMilis) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        java.util.concurrent.CompletableFuture<Void> future = new CompletableFuture<>();
        try {
            scheduler.schedule(() -> {
                future.complete(null);
            }, tiempoMilis, TimeUnit.MILLISECONDS);
            future.get();
            scheduler.shutdown();
        } catch (Exception e) {
            scheduler.shutdown();
        } finally {
            scheduler.shutdown();
        }
    }
    public static String idProcesoLibreria() {
        Integer random = secureRandom.nextInt(Integer.MAX_VALUE - 1) + 1;
        return random.toString();
    }
}
