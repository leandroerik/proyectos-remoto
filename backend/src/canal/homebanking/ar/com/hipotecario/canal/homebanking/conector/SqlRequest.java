package ar.com.hipotecario.canal.homebanking.conector;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SqlRequest {

    /* ========== ATRIBUTOS ========== */
    public String servicio;
    public String servidor;
    public String usuario;
    public String clave;
    public String sql = "";
    public List<Object> parametros = new ArrayList<>();

    /* ========== METODOS ========== */
    public void add(Object valor) {
        parametros.add(valor);
    }


    /**
     * Permite configurar la ejecución de un SP
     *
     * @param storedProcedure nombre de Sp a ejecutar
     * @param parametros      listado de parametros del sp, el mapa debe contener el nombre del parámetro de sql y el valor
     */
    public void configuraStoredProcedure(String storedProcedure, Map<String, Object> parametros) {
        sql = armarExecSp(storedProcedure);
        if (!parametros.isEmpty()) {
            parametros.forEach((key, value) -> {
                sql = sql.concat(key.contains("@") ? key : "@".concat(key)).concat(" = ?, ");
                add(value);
            });
            sql = sql.substring(0, sql.length() - 2);
        }
    }

    /**
     * Permite configurar la ejecución de un SP
     *
     * @param storedProcedure nombre de Sp a ejecutar
     * @param parametros      listado de parametros del sp, deben estar en el exacto orden de como están declarados en el sp
     */
    public void configurarStoredProcedure(String storedProcedure, Object... parametros) {
        sql = armarExecSp(storedProcedure);
        if (parametros != null && !(Arrays.asList(parametros).isEmpty())) {
            Arrays.asList(parametros).forEach(p -> {
                String valor = esString(p) ? "'".concat(p.toString()).concat("'") : p != null ? p.toString() : "NULL";
                sql = sql.concat(valor).concat(", ");
            });
            sql = sql.substring(0, sql.length() - 2);
        }
    }

    /**
     * Permite configurar la ejecución de un SP
     *
     * @param storedProcedure nombre de Sp a ejecutar
     * @param parametros      objeto con parametros del sp, deben estar en el exacto orden de como están declarados en el sp
     */
    public void configurarStoredProcedure(String storedProcedure, Objeto parametros) {
        sql = armarExecSp(storedProcedure);
        if (parametros != null && !parametros.objetos().isEmpty()) {
            parametros.objetos().get(0).claves().forEach(p -> {
                Object a = parametros.objetos().get(0).get(p);
                String valor = esString(a) ? StringUtils.isNotBlank(a.toString()) ? "'".concat(a.toString()).concat("'") : "NULL" : a != null ? a.toString() : "NULL";
                sql = sql.concat(valor).concat(", ");
            });
            sql = sql.substring(0, sql.length() - 2);
        }
    }

    private String armarExecSp(String storedProcedure) {
        return "EXEC ".concat(storedProcedure).concat(" ");
    }

    private boolean esString(Object value) {
        return value instanceof String;
    }
}
