package ar.com.hipotecario.canal.officebanking.dto.logs;

import com.opencsv.bean.CsvBindByName;
import com.opencsv.bean.CsvBindByPosition;

public class ReporteLogsCrmDto {
    @CsvBindByName(column = "operacion", required = true)
    @CsvBindByPosition(position = 0)
    public String operacion;

    @CsvBindByName(column = "Empresa", required = true)
    @CsvBindByPosition(position = 1)
    public String cuitEmpresa;

    @CsvBindByName(column = "Usuario", required = true)
    @CsvBindByPosition(position = 2)
    public String cuitUsuario;

    @CsvBindByName(column = "usuario_crm", required = true)
    @CsvBindByPosition(position = 3)
    public String usuarioCrm;

    @CsvBindByName(column = "fecha", required = true)
    @CsvBindByPosition(position = 4)
    public String fecha;


    public ReporteLogsCrmDto(String operacion, String fecha, String cuitEmpresa, String cuitUsuario,String usuarioCrm) {
        this.fecha = fecha;
        this.cuitEmpresa = cuitEmpresa;
        this.cuitUsuario = cuitUsuario;
        this.usuarioCrm = usuarioCrm;
        this.operacion = operacion;

    }






}
