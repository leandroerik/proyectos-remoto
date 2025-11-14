package ar.com.hipotecario.canal.rewards.core;

import java.io.InputStream;
import java.lang.String;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.ContextoRewards;
import ar.com.hipotecario.canal.rewards.modulos.menu.services.RWMenuService;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.Iterator;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

public class ValidacionPermisos {

    public enum Permisos {
        SOLICITAR_AJUSTE("solicitar_ajuste"),
        AUTORIZAR_AJUSTE("autorizar_ajuste"),
        MODIFICAR_CAMPANIA("modificar_campania"),
        CORREGIR_RECHAZO("corregir_rechazo"),
        APROBAR_RECHAZO("aprobar_rechazo"),
        DENEGAR_RECHAZO("denegar_rechazo");

        private final String valor;

        Permisos(String valor) {
            this.valor = valor;
        }

        public String getValue() {
            return valor;
        }

    }

    static public boolean validarPermisos(ContextoRewards contexto, Permisos permiso)
    {
        try {
            String filePath = "tas/rewards/menu_json/" + contexto.sesion().rol + ".json";
            InputStream inputStream = ValidacionPermisos.class.getClassLoader().getResourceAsStream(filePath);

            if (inputStream == null) {
                RespuestaRW.error(contexto, "Error al cargar menu: Archivo no encontrado" + filePath);
                return false;
            }

            String jsonContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            ObjectMapper objectMapper = new ObjectMapper();
            @SuppressWarnings("unchecked")
            Map<String, Object> jsonMap = objectMapper.readValue(jsonContent, Map.class);
            Map<String, Boolean> permisosMap = (Map<String, Boolean>) jsonMap.get("permisos");

            if (permisosMap != null && permisosMap.get(permiso.getValue()) != null
                    && permisosMap.get(permiso.getValue())) {
                return true;
            }

            return false;
        } catch (Exception e) {
            RespuestaRW.error(contexto, "Error al deserealizar el json de permisos en ValidacionPermisos", e, "");
            return false;
        }
    }
}