package ar.com.hipotecario.backend.servicio.api.cuentas;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.conector.api.ApiObjetos;
import ar.com.hipotecario.backend.conector.api.ApiRequest;
import ar.com.hipotecario.backend.conector.api.ApiResponse;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentasAcuerdos.CuentasAcuerdo;

public class CuentasAcuerdos extends ApiObjetos<CuentasAcuerdo> {

    public static class ListaCuentasAcuerdo extends ApiObjeto {
        public ArrayList<CuentasAcuerdo> list1 = new ArrayList<>();

        public ArrayList<CuentasAcuerdo> list2 = new ArrayList<>();

        public ArrayList<CuentasAcuerdo> getList1(){
            return list1;
        }

        public ArrayList<CuentasAcuerdo> getList2(){
            return list2;
        }
    }

    public static class CuentasAcuerdoDeuda extends ApiObjeto {
        public String numeroProducto;
        public String fechaVencimiento;

        public String getNumeroProducto(){
            return numeroProducto;
        }

        public String getFechaVencimiento(){
            return fechaVencimiento;
        }
    }

        public static class CuentasAcuerdo extends ApiObjeto {
            public Integer producto;
            public String descProducto;
            public String canal;
            public String subcanal;
            public String destino;
            public String descDestino;
            public  String fechaContrato;
            public String nCtaCorriente;

            public Integer sucursal;
            public BigDecimal limiteReduccionCred;
            public BigDecimal porcentajeReduccion;
            public BigDecimal monto;
            public BigDecimal plazo;
            public BigDecimal tasaInteres;
            public String tipoTasa;
            public String desTasa;
            public String indicadorTasa;
            public String garantia;
            public Double tem;
            public Double tea;
            public Integer idOficialAlta;
            public String nombreOficial;
    }

    /* ========== SERVICIOS ========== */
    // API-Cuentas_AcuerdosXCuenta
    static List<Objeto> get(Contexto contexto, String idCuenta) {
        ApiRequest request = new ApiRequest("Cuentas_AcuerdosXCuenta", "cuentas", "GET", "/v1/acuerdos/cuenta", contexto);
        request.query("cuenta", idCuenta);
        request.cache = false;

        ApiResponse response = request.ejecutar();

        if(!response.http(200, 204)){
            return new ArrayList();
        }
        CuentasAcuerdos.ListaCuentasAcuerdo listadoDeAcuerdos = response.crear(CuentasAcuerdos.ListaCuentasAcuerdo.class);
        List<Objeto> acuerdos = listadoDeAcuerdos.getList1().stream().map(ac -> {
            Objeto acuerdo = new Objeto();
            acuerdo.set("fechaContrato",ac.fechaContrato);
            acuerdo.set("tasaInteres",ac.tasaInteres);
            acuerdo.set("monto",ac.monto);
            acuerdo.set("tem",ac.tem);
            acuerdo.set("tea",ac.tea);
           return acuerdo;
        }).collect(Collectors.toList());

        return acuerdos;
    }
}

