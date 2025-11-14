package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASPlazosFijosPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class TASPlazosFijosV4Mapper {
    /*
    * {
      "numeroProducto": "00008000111073489",
      "idProducto": "78031022",
      "estado": "V",
      "fechaAlta": "2022-01-17",
      "tipoTitularidad": "T",
      "tipoOperacion": "0011",
      "moneda": "80",
      "fechaVencimiento": "2022-02-16",
      "importe": 671178
    },*/
    public static TASPlazosFijosPConsV4 mapear(TASProductosGenericosV4 producto){
        TASPlazosFijosPConsV4 pf = new TASPlazosFijosPConsV4();
        pf.setNumeroProducto(producto.getNumeroProducto());
        pf.setIdProducto(String.valueOf(producto.getDetProducto()));
        pf.setEstado(producto.getEstado());
        pf.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));//parsear de dd/mm/yyyy a yyyy-mm-dd
        pf.setTipoTitularidad(producto.getCodigoTitularidad());
        pf.setTipoOperacion(producto.getDescProducto());
        pf.setMoneda(String.valueOf(producto.getCodMoneda()));
        pf.setFechaVencimiento(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getPfFechaVencimiento()));//parsear de dd/mm/yyyy a yyyy-mm-dd
        pf.setImporte(producto.getImporte());
        return pf;
    }

    /*
    * {
    "rol": "T",
    "descMoneda": "PESOS",
    "muestraPaquete": false,
    "tipoProducto": "PFI",
    "numeroProducto": "00008000111073489",
    "idProducto": "78031022",
    "estado": "V",
    "descEstado": "VIGENTE",
    "fechaAlta": "2022-01-17",
    "descTipoTitularidad": "TITULAR",
    "tipoOperacion": "0011",
    "adicionales": true,
    "idMoneda": "80",
    "fechaVencimiento": "2022-02-16",
    "importe": "671178.0",
    "estadoPlazoFijo": "CAN"
  }
    * */
    public static List<TASPlazosFijosPConsV4> mapeoCompleto(Objeto responseApi){
        List<TASPlazosFijosPConsV4> pfs = new ArrayList<>();
        for(Objeto pfApi : responseApi.objetos()){
            TASPlazosFijosPConsV4 pf = new TASPlazosFijosPConsV4();
            pf.setNumeroProducto(pfApi.string("numeroProducto"));
            pf.setIdProducto(pfApi.string("idProducto"));
            pf.setEstado(pfApi.string("estado"));
            pf.setFechaAlta(pfApi.string("fechaAlta"));//parsear de dd/mm/yyyy a yyyy-mm-dd
            pf.setTipoTitularidad(pfApi.string("rol"));
            pf.setTipoOperacion(pfApi.string("tipoOperacion"));
            pf.setMoneda(pfApi.string("idMoneda"));
            pf.setFechaVencimiento(pfApi.string("fechaVencimiento"));//parsear de dd/mm/yyyy a yyyy-mm-dd
            Double importe = Double.valueOf(pfApi.string("importe"));
            pf.setImporte(BigDecimal.valueOf(importe));
            pfs.add(pf);
        }
        return pfs;
    }
    public static Objeto pfToObjeto(TASPlazosFijosPConsV4 pf){
        Objeto pfObj = new Objeto();
        pfObj.set("numeroProducto", pf.getNumeroProducto());
        pfObj.set("idProducto", pf.getIdProducto());
        pfObj.set("estado", pf.getEstado());
        pfObj.set("fechaAlta", pf.getFechaAlta());
        pfObj.set("tipoTitularidad", pf.getTipoTitularidad());
        pfObj.set("tipoOperacion", pf.getTipoOperacion());
        pfObj.set("moneda", pf.getMoneda());
        pfObj.set("fechaVencimiento", pf.getFechaVencimiento());
        pfObj.set("importe", pf.getImporte());
        return pfObj;
    }
}
