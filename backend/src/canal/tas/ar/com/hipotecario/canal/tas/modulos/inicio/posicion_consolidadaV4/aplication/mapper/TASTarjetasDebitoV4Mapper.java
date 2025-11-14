package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASPrestamosPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASTarjetasDebitoPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.ArrayList;
import java.util.List;

public class TASTarjetasDebitoV4Mapper {

    /*
    {
      "idPaquete": 104377,
      "muestraPaquete": true,
      "tipoProducto": "ATM",
      "numeroProducto": "4998590092888714",
      "idProducto": "69997653",
      "sucursal": 0,
      "descSucursal": "BUENOS AIRES",
      "descEstado": "VIGENTE",
      "estado": "V",
      "fechaAlta": "2019-02-07",
      "idDomicilio": 1,
      "tipoTitularidad": "P",
      "adicionales": false,
      "moneda": "80",
      "descMoneda": "PESOS",
      "limiteExtraccionMonto": 250000,
      "fechaVencimiento": "2022-02-28",
      "activacionTemprana": false,
      "virtual": false
    }
    * */
    public static TASTarjetasDebitoPConsV4 mapear(TASProductosGenericosV4 producto){
        TASTarjetasDebitoPConsV4 td = new TASTarjetasDebitoPConsV4();
        td.setIdPaquete(null);
        td.setMuestraPaquete(Boolean.valueOf(producto.getMuestraPaquete()));
        td.setTipoProducto(producto.getTipo());
        td.setNumeroProducto(producto.getNumeroProducto());
        td.setIdProducto(String.valueOf(producto.getDetProducto()));
        td.setSucursal(null);
        td.setDescSucursal(null);
        td.setDescEstado(null);
        td.setEstado(producto.getEstado());
        td.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));
        td.setIdDomicilio(null);
        td.setTipoTitularidad(producto.getCodigoTitularidad());
        td.setAdicionales(null);
        td.setMoneda(String.valueOf(producto.getCodMoneda()));
        td.setDescMoneda(TASPosicionConsolidadaV4Utils.verificarMoneda(producto.getCodMoneda()));
        td.setLimiteExtraccionMonto(null);
        td.setFechaVencimiento(null);
        td.setActivacionTemprana(null);
        td.setVirtual(null);
        return td;
    }

    public static TASTarjetasDebitoPConsV4 enriquecerTD(TASTarjetasDebitoPConsV4 td, Objeto tdApi){
        if(tdApi.string("numeroProducto").equals(td.getNumeroProducto())){
            td.setIdPaquete(!tdApi.string("idPaquete").isEmpty() ? Integer.valueOf(tdApi.string("idPaquete")) : null);
            td.setSucursal(tdApi.integer("sucursal"));
            td.setDescSucursal(tdApi.string("descSucursal"));
            td.setDescEstado(tdApi.string("descEstado"));
            td.setIdDomicilio(tdApi.integer("idDomicilio"));
            td.setAdicionales(tdApi.bool("adicionales"));
            td.setLimiteExtraccionMonto(tdApi.bigDecimal("limiteExtraccionMonto"));
            td.setFechaVencimiento(tdApi.string("fechaVencimiento"));
            td.setActivacionTemprana(tdApi.bool("activacionTemprana"));
            td.setVirtual(tdApi.bool("virtual"));
        }
        return td;
    }

    /*
    * {
    "idPaquete": "104377",
    "muestraPaquete": true,
    "tipoProducto": "ATM",
    "numeroProducto": "4998590092888714",
    "idProducto": "69997653",
    "sucursal": 0,
    "descSucursal": "BUENOS AIRES",
    "estado": "V",
    "descEstado": "VIGENTE",
    "fechaAlta": "2019-02-07",
    "idDomicilio": 1,
    "tipoTitularidad": "P",
    "adicionales": false,
    "moneda": "80",
    "monedaDesc": "PESOS",
    "numeroTarjeta": "2599232",
    "estadoTarjeta": "B",
    "nroSolicitud": "0",
    "limiteExtraccionMonto": 250000,
    "fechaVencimiento": "2022-02-28",
    "activacionTemprana": false,
    "virtual": false,
    "pausada": "N"
  }
    * */
    public static List<TASTarjetasDebitoPConsV4> mapeoCompleto(Objeto responseApi){
        List<TASTarjetasDebitoPConsV4> tds = new ArrayList<>();
        for(Objeto tdApi : responseApi.objetos()){
            TASTarjetasDebitoPConsV4 td = new TASTarjetasDebitoPConsV4();
            td.setIdPaquete(!tdApi.string("idPaquete").isEmpty() ? Integer.valueOf(tdApi.string("idPaquete")) : null);
            td.setMuestraPaquete(tdApi.bool("muestraPaquete"));
            td.setTipoProducto(tdApi.string("tipoProducto"));
            td.setNumeroProducto(tdApi.string("numeroProducto"));
            td.setIdProducto(tdApi.string("idProducto"));
            td.setSucursal(tdApi.integer("sucursal"));
            td.setDescSucursal(tdApi.string("descSucursal"));
            td.setDescEstado(tdApi.string("descEstado"));
            td.setEstado(tdApi.string("estado"));
            td.setFechaAlta(tdApi.string("fechaAlta"));
            td.setIdDomicilio(tdApi.integer("idDomicilio"));
            td.setTipoTitularidad(tdApi.string("tipoTitularidad"));
            td.setAdicionales(tdApi.bool("adicionales"));
            td.setMoneda(tdApi.string("moneda"));
            td.setDescMoneda(tdApi.string("monedaDesc"));
            td.setLimiteExtraccionMonto(tdApi.bigDecimal("limiteExtraccionMonto"));
            td.setFechaVencimiento(tdApi.string("fechaVencimiento"));
            td.setActivacionTemprana(tdApi.bool("activacionTemprana"));
            td.setVirtual(tdApi.bool("virtual"));
            tds.add(td);
        }
        return tds;
    }

    public static Objeto tdToObjeto(TASTarjetasDebitoPConsV4 td){
        Objeto tdObj = new Objeto();
        if(td.getIdPaquete() != null){tdObj.set("idPaquete",td.getIdPaquete());}
        tdObj.set("muestraPaquete", td.getMuestraPaquete());
        tdObj.set("tipoProducto", td.getTipoProducto());
        tdObj.set("numeroProducto", td.getNumeroProducto());
        tdObj.set("idProducto", td.getIdProducto());
        tdObj.set("sucursal", td.getSucursal());
        tdObj.set("descSucursal", td.getDescSucursal());
        tdObj.set("descEstado", td.getDescEstado());
        tdObj.set("estado", td.getEstado());
        tdObj.set("fechaAlta", td.getFechaAlta());
        tdObj.set("idDomicilio", td.getIdDomicilio());
        tdObj.set("tipoTitularidad", td.getTipoTitularidad());
        tdObj.set("adicionales", td.getAdicionales());
        tdObj.set("moneda", td.getMoneda());
        tdObj.set("descMoneda", td.getDescMoneda());
        tdObj.set("limiteExtraccionMonto", td.getLimiteExtraccionMonto());
        tdObj.set("fechaVencimiento", td.getFechaVencimiento());
        tdObj.set("activacionTemprana", td.getActivacionTemprana());
        tdObj.set("virtual", td.isVirtual());
        return tdObj;
    }
}
