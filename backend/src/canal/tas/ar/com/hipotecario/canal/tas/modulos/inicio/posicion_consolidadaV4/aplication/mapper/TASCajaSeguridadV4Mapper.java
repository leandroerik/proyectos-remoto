package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASCajasSegPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.ArrayList;
import java.util.List;

public class TASCajaSeguridadV4Mapper {

    /*
    {
      "muestraPaquete": false,
      "tipoProducto": "CSG",
      "numeroProducto": "5396",
      "idProducto": "72003810",
      "sucursal": 0,
      "descSucursal": "BUENOS AIRES",
      "descEstado": "VIGENTE",
      "estado": "V",
      "fechaAlta": "2019-09-05",
      "idDomicilio": 5,
      "tipoTitularidad": "T",
      "descTipoTitularidad": "TITULAR",
      "tipoOperacion": " ",
      "adicionales": true,
      "moneda": "80",
      "descMoneda": "PESOS",
      "estadoCajaSeguridad": "V",
      "fechaVencimiento": "2024-09-04"
    }
    */
    public static TASCajasSegPConsV4 mapear(TASProductosGenericosV4 producto){
        TASCajasSegPConsV4 cajasSeg = new TASCajasSegPConsV4();
        cajasSeg.setMuestraPaquete(producto.getMuestraPaquete().equals("true") ? true : false);
        cajasSeg.setTipoProducto(producto.getTipo());
        cajasSeg.setNumeroProducto(producto.getNumeroProducto());
        cajasSeg.setIdProducto(String.valueOf(producto.getDetProducto()));
        cajasSeg.setSucursal(null);
        cajasSeg.setDescSucursal(null);
        cajasSeg.setDescEstado(null);
        cajasSeg.setEstado(producto.getEstado());
        cajasSeg.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));//parsear de dd/mm/yyyy a yyyy-mm-dd
        cajasSeg.setIdDomicilio(null);
        cajasSeg.setTipoTitularidad(producto.getCodigoTitularidad());
        cajasSeg.setDescTipoTitularidad(producto.getDescTitularidad());
        cajasSeg.setTipoOperacion(null);
        cajasSeg.setAdicionales(null);
        cajasSeg.setMoneda(String.valueOf(producto.getCodMoneda()));
        cajasSeg.setDescMoneda(TASPosicionConsolidadaV4Utils.verificarMoneda(producto.getCodMoneda()));
        cajasSeg.setEstadoCajaSeguridad(null);
        cajasSeg.setFechaVencimiento(null);
        return cajasSeg;
    }
    
    public static TASCajasSegPConsV4 enriquecerCajas(TASCajasSegPConsV4 caja, Objeto cajaApi){
        if(cajaApi.string("numeroProducto").equals(caja.getNumeroProducto())){
            caja.setSucursal(cajaApi.integer("sucursal"));
            caja.setDescSucursal(cajaApi.string("descSucursal"));
            caja.setDescEstado(cajaApi.string("descEstado"));
            caja.setIdDomicilio(cajaApi.integer("idDomicilio"));
            caja.setTipoOperacion(cajaApi.string("tipoOperacion"));
            caja.setAdicionales(cajaApi.bool("adicionales"));
            caja.setEstadoCajaSeguridad(cajaApi.string("estadoCajaSeguridad"));
            caja.setFechaVencimiento(cajaApi.string("fechaVencimiento"));
        }
        return caja;
    }

    /*
    *  {
    "muestraPaquete": false,
    "tipoProducto": "CSG",
    "numeroProducto": "5396",
    "idProducto": "72003810",
    "sucursal": 0,
    "descSucursal": "BUENOS AIRES",
    "estado": "V",
    "descEstado": "VIGENTE",
    "fechaAlta": "2019-09-05",
    "idDomicilio": 5,
    "tipoTitularidad": "T",
    "descTipoTitularidad": "TITULAR",
    "tipoOperacion": " ",
    "adicionales": true,
    "moneda": "80",
    "descMoneda": "PESOS",
    "estadoCajaSeguridad": "V",
    "fechaVencimiento": "2024-09-04"
  }
    * */
    public static List<TASCajasSegPConsV4> mapeoCompleto(Objeto apiResponse){
        List<TASCajasSegPConsV4> cajasSeg = new ArrayList<>();
        for(Objeto cajaApi : apiResponse.objetos()){
            TASCajasSegPConsV4 cajaSeg = new TASCajasSegPConsV4();
            cajaSeg.setMuestraPaquete(cajaApi.bool("muestraPaquete"));
            cajaSeg.setTipoProducto(cajaApi.string("tipoProducto"));
            cajaSeg.setNumeroProducto(cajaApi.string("numeroProducto"));
            cajaSeg.setIdProducto(cajaApi.string("idProducto"));
            cajaSeg.setSucursal(cajaApi.integer("sucursal"));
            cajaSeg.setDescSucursal(cajaApi.string("descSucursal"));
            cajaSeg.setDescEstado(cajaApi.string("descEstado"));
            cajaSeg.setEstado(cajaApi.string("estado"));
            cajaSeg.setFechaAlta(cajaApi.string("fechaAlta"));//parsear de dd/mm/yyyy a yyyy-mm-dd
            cajaSeg.setIdDomicilio(cajaApi.integer("idDomicilio"));
            cajaSeg.setTipoTitularidad(cajaApi.string("tipoTitularidad"));
            cajaSeg.setDescTipoTitularidad(cajaApi.string("descTipoTitularidad"));
            cajaSeg.setTipoOperacion(cajaApi.string("tipoOperacion"));
            cajaSeg.setAdicionales(cajaApi.bool("adicionales"));
            cajaSeg.setMoneda(cajaApi.string("moneda"));
            cajaSeg.setDescMoneda(cajaApi.string("descMoneda"));
            cajaSeg.setEstadoCajaSeguridad(cajaApi.string("estadoCajaSeguridad"));
            cajaSeg.setFechaVencimiento(cajaApi.string("fechaVencimiento"));
            cajasSeg.add(cajaSeg);
        }
        return cajasSeg;
    }
/*
* "muestraPaquete": false,
      "tipoProducto": "CSG",
      "numeroProducto": "5396",
      "idProducto": "72003810",
      "sucursal": 0,
      "descSucursal": "BUENOS AIRES",
      "descEstado": "VIGENTE",
      "estado": "V",
      "fechaAlta": "2019-09-05",
      "idDomicilio": 5,
      "tipoTitularidad": "T",
      "descTipoTitularidad": "TITULAR",
      "tipoOperacion": " ",
      "adicionales": true,
      "moneda": "80",
      "descMoneda": "PESOS",
      "estadoCajaSeguridad": "V",
      "fechaVencimiento": "2024-09-04"
* */
    public static Objeto cajaToObjeto(TASCajasSegPConsV4 caja){
        Objeto cajaObj = new Objeto();
        cajaObj.set("muestraPaquete", caja.isMuestraPaquete());
        cajaObj.set("tipoProducto", caja.getTipoProducto());
        cajaObj.set("numeroProducto", caja.getNumeroProducto());
        cajaObj.set("idProducto", caja.getIdProducto());
        cajaObj.set("sucursal", caja.getSucursal());
        cajaObj.set("descSucursal", caja.getDescSucursal());
        cajaObj.set("descEstado", caja.getDescEstado());
        cajaObj.set("estado", caja.getEstado());
        cajaObj.set("fechaAlta", caja.getFechaAlta());
        cajaObj.set("idDomicilio", caja.getIdDomicilio());
        cajaObj.set("tipoTitularidad", caja.getTipoTitularidad());
        cajaObj.set("descTipoTitularidad", caja.getDescTipoTitularidad());
        cajaObj.set("tipoOperacion", caja.getTipoOperacion());
        cajaObj.set("adicionales", caja.getAdicionales());
        cajaObj.set("moneda", caja.getMoneda());
        cajaObj.set("descMoneda", caja.getDescMoneda());
        cajaObj.set("estadoCajaSeguridad", caja.getEstadoCajaSeguridad());
        cajaObj.set("fechaVencimiento", caja.getFechaVencimiento());
        return cajaObj;
    }
}
