package ar.com.hipotecario.backend.servicio.api.debin;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumTipoProductoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioAccionesOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoProductoFirmaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.ConceptoDebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.debin.DebinProgramadoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.mobile.negocio.EnumMoneda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ListarDebinProgramado extends ApiObjeto {

    public static class Result extends ApiObjeto {
            public Listado listado;
            public Respuesta respuesta;
            public List<Recurrencia> recurrencia;

    }
public Result result = new Result();
    public static class Listado extends ApiObjeto {
        public int paginas_totales;
    }

    public static class Respuesta extends ApiObjeto {
        public String descripcion;
        public String codigo;
    }

    public static class Recurrencia extends ApiObjeto {
        public String id;
        public String estado;
        public Vendedor vendedor;
        public Comprador comprador;
        public Debin debin;
        public String autorizado;
        public String fecha_creacion;
        public String cuit_creacion;

        public LocalDateTime getFechaCreacion() {
            return LocalDateTime.parse(fecha_creacion, DateTimeFormatter.ISO_DATE_TIME);
        }
    }


    public static class Vendedor extends ApiObjeto {
        public String cuit;
    }

    public static class Comprador extends ApiObjeto {
        public String cuit;
        public String cbu;
    }

    public static class Debin extends ApiObjeto {
        public String moneda;
        public BigDecimal importe;
        public String concepto;
        public String detalle;
        public String prestacion;
        public String referencia;
        public int limite_cuotas;
    }

    public static DebinProgramadoOB mapToDebinProgramadoOB(ListarDebinProgramado.Recurrencia recurrencia, ContextoOB contexto) {
        DebinProgramadoOB debin = new DebinProgramadoOB();
        ServicioTipoProductoFirmaOB servicioTipoProductoFirmaOB = new ServicioTipoProductoFirmaOB(contexto);
        if (recurrencia != null ) {
            debin.idDebin = recurrencia.id;
            debin.tipoProductoFirma = servicioTipoProductoFirmaOB.findByCodigo(EnumTipoProductoOB.DEBIN_PROGRAMADO.getCodigo()).get();
            debin.fechaCreacion = recurrencia.fecha_creacion;
            debin.cuitCreacion = recurrencia.cuit_creacion;
            debin.vencimiento = recurrencia.getFechaCreacion().plusDays(3).format(DateTimeFormatter.ISO_DATE_TIME);
            debin.compradorCuit = recurrencia.comprador.cuit;
            debin.compradorCbu = recurrencia.comprador.cbu;
            debin.cuentaOrigen = recurrencia.comprador.cbu;
            debin.vendedorCuit = recurrencia.vendedor.cuit;
            debin.autorizado = recurrencia.autorizado;
            debin.debinReferencia = recurrencia.debin.referencia;
            debin.debinPrestacion =recurrencia.debin.prestacion;
            debin.debinDetalle = recurrencia.debin.detalle;
            debin.debinLimiteCuotas = recurrencia.debin.limite_cuotas;
            debin.debinImporte = recurrencia.debin.importe;
            debin.estado = recurrencia.estado;
            debin.empresa = contexto.sesion().empresaOB;
            debin.fechaUltActulizacion = LocalDateTime.now();
            // Set the related entities
            if(recurrencia.debin.moneda.equals("032")){
                MonedaOB moneda = new MonedaOB();
                    moneda.id = 80;
                moneda.descripcion = "ARS";
                moneda.simbolo = "$";
                debin.moneda = moneda;
                debin.debinMoneda =  recurrencia.debin.moneda;
            }
            String conceptoNombre = switch (recurrencia.debin.concepto) {
                case "ALQ" -> "ALQUILERES";
                case "CUO" -> "CUOTAS";
                case "EXP" -> "EXPENSAS";
                case "FAC" -> "FACTURAS";
                case "HON" -> "HONORARIOS";
                case "PRE" -> "PRESTAMOS";
                case "SEG" -> "SEGUROS";
                case "HAB" -> "HABERES";
                default -> "VARIOS";
            };
            debin.debinConcepto = conceptoNombre;
            debin.emp_codigo = contexto.sesion().empresaOB;
            debin.usuario = contexto.sesion().usuarioOB;
            debin.monto = recurrencia.debin.importe;

        }

        return debin;
    }

    public static ListarDebinProgramado post(Contexto contexto, String idTributarioComprador, String banco, String fechaDesde, String fechaHasta, String idTributarioVendedor) {
        ApiRequest request = new ApiRequest("ListarDebinProgramado", "debin", "POST", "/v1/debinprogramado/compradorRecurrenciaLista", contexto);
        request.body("listado.tamano", 300);
        request.body("listado.pagina", 1);

        request.body("fechaDesde", fechaDesde);
        request.body("fechaHasta", fechaHasta);

        if (idTributarioComprador != null) {
            request.body("comprador.cliente.idTributario", idTributarioComprador);
            request.body("comprador.cliente.cuenta.banco", banco);
        }
        if (idTributarioVendedor != null) {
            request.body("vendedor.cliente.idTributario", idTributarioVendedor);
            request.body("vendedor.cliente.cuenta.banco", banco);
        }

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(ListarDebinProgramado.class);
    }
    public static ListarDebinProgramado buscarDebinRecurrente(Contexto contexto, String idTributarioComprador,String banco, String fechaDesde, String fechaHasta, String idTributarioVendedor,String bancoVendedor,String estado) {
        ApiRequest request = new ApiRequest("ListarDebinProgramado", "debin", "POST", "/v1/debinprogramado/compradorRecurrenciaLista", contexto);

        request.body("listado.tamano", 300);
        request.body("listado.pagina", 1);

        request.body("fechaDesde", fechaDesde);
        request.body("fechaHasta", fechaHasta);
        if(estado != null) {
            request.body("estado.codigo", estado);        }

        if (idTributarioComprador != null) {
            request.body("comprador.cliente.idTributario", idTributarioComprador);
            request.body("comprador.cliente.cuenta.banco", banco);
        }
        if( idTributarioVendedor != null) {
            request.body("vendedor.cliente.idTributario", idTributarioVendedor);
            request.body("vendedor.cliente.cuenta.banco", bancoVendedor);
        }

        ApiResponse response = request.ejecutar();
        ApiException.throwIf(!response.http(200, 201), request, response);
        return response.crear(ListarDebinProgramado.class);
    }
}