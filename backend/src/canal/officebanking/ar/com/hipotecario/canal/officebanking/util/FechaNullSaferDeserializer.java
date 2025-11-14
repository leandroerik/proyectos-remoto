package ar.com.hipotecario.canal.officebanking.util;

    // FechaNullSafeDeserializer.java
    import com.fasterxml.jackson.core.JsonParser;
    import com.fasterxml.jackson.databind.DeserializationContext;
    import com.fasterxml.jackson.databind.JsonDeserializer;
    import java.io.IOException;
    import ar.com.hipotecario.backend.base.Fecha;

    public class FechaNullSaferDeserializer extends JsonDeserializer<Fecha> {
        @Override
        public Fecha deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String value = p.getValueAsString();
            if (value == null || value.trim().isEmpty()) {
                return null;
            }
            return new Fecha(value, "yyyy-MM-dd'T'HH:mm:ss");
        }
    }