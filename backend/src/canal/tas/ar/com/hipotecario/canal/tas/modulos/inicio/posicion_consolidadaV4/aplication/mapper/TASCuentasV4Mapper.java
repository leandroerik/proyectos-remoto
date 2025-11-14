package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASCuentasPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.ArrayList;
import java.util.List;

public class TASCuentasV4Mapper {

    /*
    *  {
      "tipoProducto": "AHO",
      "numeroProducto": "400000011982883",
      "idProducto": "52941715",
      "sucursal": 0,
      "fechaAlta": "2007-08-17",
      "tipoTitularidad": "T",
      "moneda": "80",
      "estadoCuenta": "",
      "categoria": ""
    }*/
    public static TASCuentasPConsV4 mapear(TASProductosGenericosV4 producto){
        TASCuentasPConsV4 cuenta = new TASCuentasPConsV4();
        cuenta.setIdPaquete(null);
        cuenta.setTipoProducto(producto.getTipo());
        cuenta.setNumeroProducto(producto.getNumeroProducto());
        cuenta.setIdProducto(String.valueOf(producto.getDetProducto()));
        cuenta.setSucursal(null);
        cuenta.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));//parsear de dd/mm/yyyy a yyyy-mm-dd
        cuenta.setTipoTitularidad(producto.getCodigoTitularidad());
        cuenta.setMoneda(String.valueOf(producto.getCodMoneda()));
        cuenta.setEstadoCuenta(null);
        cuenta.setDisponible(producto.getImporte());
        cuenta.setAcuerdo(null);
        cuenta.setCategoria(null);
        return cuenta;
    }

    public static TASCuentasPConsV4 enriquecerCtas(TASCuentasPConsV4 cta, Objeto ctaApi){
            if(ctaApi.string("numeroProducto").equals(cta.getNumeroProducto())){
                cta.setIdPaquete(!ctaApi.string("idPaquete").isEmpty() ? Integer.valueOf(ctaApi.string("idPaquete")) : null);
                cta.setSucursal(ctaApi.integer("sucursal"));
                cta.setEstadoCuenta(ctaApi.string("estadoCuenta"));
                cta.setAcuerdo(ctaApi.bigDecimal("acuerdo"));
                cta.setCategoria(ctaApi.string("categoria"));
            }
        return cta;
    }

    /*
    * {
    "numeroProducto": "300400000318855",
    "cbu": "0440004230000003188557",
    "descEstado": "VIGENTE",
    "moneda": "80",
    "descMoneda": "PESOS",
    "disponible": 231453.6,
    "tipoTitularidad": "T",
    "descTipoTitularidad": "TITULAR",
    "idPaquete": "32756",
    "categoria": "PBH",
    "sucursal": 4,
    "descSucursal": "COMODORO RIVADAVIA",
    "muestraPaquete": true,
    "tipoProducto": "CTE",
    "idProducto": "52351535",
    "fechaAlta": "2007-05-14",
    "idDomicilio": 3,
    "tipoOperacion": "",
    "adicionales": true,
    "estadoCuenta": "A",
    "acuerdo": 0,
    "esTransaccional": true,
    "adelantoCuentaAsociada": "",
    "estado": "V"
  }
    * */
    public static List<TASCuentasPConsV4> mapeoCompleto(Objeto apíResponse){
        List <TASCuentasPConsV4> cuentas = new ArrayList<>();
        for(Objeto ctaApi : apíResponse.objetos()){
            TASCuentasPConsV4 cuenta = new TASCuentasPConsV4();
            cuenta.setIdPaquete(!ctaApi.string("idPaquete").isEmpty() ? Integer.valueOf(ctaApi.string("idPaquete")) : null);
            cuenta.setTipoProducto(ctaApi.string("tipoProducto"));
            cuenta.setNumeroProducto(ctaApi.string("numeroProducto"));
            cuenta.setIdProducto(ctaApi.string("idProducto"));
            cuenta.setSucursal(ctaApi.integer("sucursal"));
            cuenta.setFechaAlta(ctaApi.string("fechaAlta"));
            cuenta.setTipoTitularidad(ctaApi.string("tipoTitularidad"));
            cuenta.setMoneda(ctaApi.string("moneda"));
            cuenta.setEstadoCuenta(ctaApi.string("estadoCuenta"));
            cuenta.setDisponible(ctaApi.bigDecimal("disponible"));
            cuenta.setAcuerdo(ctaApi.bigDecimal("acuerdo"));
            cuenta.setCategoria(ctaApi.string("categoria"));
            cuentas.add(cuenta);
        }
        return cuentas;
    }

    /*
    * "idPaquete": 104377,
      "tipoProducto": "CTE",
      "numeroProducto": "300000000359371",
      "idProducto": "53465906",
      "sucursal": 0,
      "fechaAlta": "2019-04-25",
      "tipoTitularidad": "T",
      "moneda": "80",
      "estadoCuenta": "",
      "acuerdo": 0,
      "categoria": ""
    * */
    public static Objeto cuentaToObjeto(TASCuentasPConsV4 cta){
        Objeto cuenta = new Objeto();
        if(cta.getIdPaquete() != null){cuenta.set("idPaquete",cta.getIdPaquete());}
        cuenta.set("tipoProducto",cta.getTipoProducto());
        cuenta.set("numeroProducto", cta.getNumeroProducto());
        cuenta.set("idProducto", cta.getIdProducto());
        cuenta.set("sucursal", cta.getSucursal());
        cuenta.set("fechaAlta", cta.getFechaAlta());
        cuenta.set("tipoTitularidad", cta.getTipoTitularidad());
        cuenta.set("moneda", cta.getMoneda());
        cuenta.set("estadoCuenta", cta.getEstadoCuenta());
        if(cta.getDisponible() != null){cuenta.set("disponible", cta.getDisponible());}
        if(cta.getAcuerdo() != null){cuenta.set("acuerdo", cta.getAcuerdo());}
        cuenta.set("categoria", cta.getCategoria());
        return cuenta;
    }
}
