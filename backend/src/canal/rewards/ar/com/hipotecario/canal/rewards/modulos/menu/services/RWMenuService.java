package ar.com.hipotecario.canal.rewards.modulos.menu.services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;

public class RWMenuService {

    public static Objeto getMenu(ContextoRewards contexto) {

        try {
            String filePath = "tas/rewards/menu_json/" + contexto.sesion().rol + ".json";
            InputStream inputStream = RWMenuService.class.getClassLoader().getResourceAsStream(filePath);

            if (inputStream == null) {
                Objeto errorResponse = new Objeto();
                errorResponse.set("status", 1);
                errorResponse.set("msg", "Error al cargar menu: Archivo no encontrado " + filePath);
                return errorResponse;
            }

            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);
            return Objeto.fromMap(jsonMap);

        } catch (IOException e) {
            Objeto errorResponse = new Objeto();
            errorResponse.set("status", 1);
            errorResponse.set("msg", "Error al cargar menu: " + e.getMessage()); // Incluye el mensaje de error para
                                                                                 // depuración
            return errorResponse;
        } catch (Exception e) {
            Objeto errorResponse = new Objeto();
            errorResponse.set("status", 9);
            errorResponse.set("msg", "Error al cargar menu: " + e.getMessage());
            return errorResponse;
        }

    }
}
