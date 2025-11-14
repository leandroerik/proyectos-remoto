package ar.com.hipotecario.canal.officebanking.dto.logs;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class ReporteLogsDto {
    @CsvBindByName(column = "Fecha", required = true)
    @CsvBindByPosition(position = 0)
    public String fecha;

    @CsvBindByName(column = "Empresa", required = true)
    @CsvBindByPosition(position = 1)
    public String cuitEmpresa;

    @CsvBindByName(column = "Usuario", required = true)
    @CsvBindByPosition(position = 2)
    public String cuitUsuario;

    @CsvBindByName(column = "Endpoint", required = true)
    @CsvBindByPosition(position = 3)
    public String enpoint;

    @CsvBindByName(column = "evento", required = true)
    @CsvBindByPosition(position = 4)
    public String evento;

    @CsvBindByName(column = "Error", required = true)
    @CsvBindByPosition(position = 5)
    public String error;

    @CsvBindByName(column = "IdProceso", required = true)
    @CsvBindByPosition(position = 6)
    public Integer id;

    @CsvBindByName(column = "datos", required = true)
    @CsvBindByPosition(position = 7)
    public String datos;



    @CsvBindByName(column = "IP", required = true)
    @CsvBindByPosition(position = 8)
    public String ip;



    public ReporteLogsDto(String fecha, String cuitEmpresa, String cuitUsuario, String enpoint, String evento, String error,String datos, Integer id, String ip) {
        this.fecha = fecha;
        this.cuitEmpresa = cuitEmpresa;
        this.cuitUsuario = cuitUsuario;
        this.enpoint = enpoint;
        this.evento = evento;
        this.error = error;
        this.id = id;
        this.ip = ip;
        this.datos=datos;
    }






}
