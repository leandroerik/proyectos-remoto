package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASInversionesPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class TASInversionesV4Mapper {

    /*
    * {
      "tipoProducto": "UNI",
      "numeroProducto": "2-000114235",
      "idProducto": "66102950",
      "sucursal": 0,
      "descSucursal": "BUENOS AIRES",
      "descEstado": "VIGENTE",
      "estado": "V",
      "fechaAlta": "2017-09-15",
      "idDomicilio": 5,
      "tipoTitularidad": "T",
      "descTipoTitularidad": "TITULAR",
      "adicionales": false,
      "moneda": 80,
      "descMoneda": "PESOS"
    }*/

    public static TASInversionesPConsV4 mapear(TASProductosGenericosV4 producto){
        TASInversionesPConsV4 inversion = new TASInversionesPConsV4();
        inversion.setTipoProducto(producto.getTipo());
        inversion.setNumeroProducto(String.valueOf(producto.getNumeroProducto()));
        inversion.setIdProducto(String.valueOf(producto.getDetProducto()));
        inversion.setSucursal(null);
        inversion.setDescSucursal(null);
        inversion.setDescEstado(null);
        inversion.setEstado(producto.getEstado());
        inversion.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));//parsear de dd/mm/yyyy a yyyy-mm-dd
        inversion.setIdDomicilio(null);
        inversion.setTipoTitularidad(producto.getCodigoTitularidad());
        inversion.setDescTipoTitularidad(producto.getDescTitularidad());
        inversion.setAdicionales(null);
        inversion.setMoneda(producto.getCodMoneda());
        inversion.setDescMoneda(TASPosicionConsolidadaV4Utils.verificarMoneda(producto.getCodMoneda()));
        return inversion;
    }

    public static TASInversionesPConsV4 enriquecerInversiones(TASInversionesPConsV4 inversion, Objeto invApi){
        if(invApi.string("numeroProducto").equals(inversion.getNumeroProducto())) {
            inversion.setSucursal(invApi.integer("sucursal"));
            inversion.setDescSucursal(invApi.string("descSucursal"));
            inversion.setDescEstado(invApi.string("descEstado"));
            inversion.setIdDomicilio(invApi.integer("idDomicilio"));
            inversion.setAdicionales(invApi.bool("adicionales"));
        }
        return inversion;
    }

    /*
    * {
    "tipoProducto": "UNI",
    "numeroProducto": "2-000114235",
    "idProducto": "66102950",
    "sucursal": 0,
    "descSucursal": "BUENOS AIRES",
    "estado": "V",
    "descEstado": "VIGENTE",
    "fechaAlta": "2017-09-15T03:00:00.000",
    "idDomicilio": 5,
    "tipoTitularidad": "T",
    "descTipoTitularidad": "TITULAR",
    "adicionales": false,
    "moneda": "80",
    "monedaDesc": "PESOS"
  }
    * */
    public static List<TASInversionesPConsV4> mapeoCompleto(Objeto apiResponse){
        List<TASInversionesPConsV4> inversiones = new ArrayList<>();
        for(Objeto invApi : apiResponse.objetos()){
            TASInversionesPConsV4 inversion = new TASInversionesPConsV4();
            inversion.setTipoProducto(invApi.string("tipoProducto"));
            inversion.setNumeroProducto(invApi.string("numeroProducto"));
            inversion.setIdProducto(invApi.string("idProducto"));
            inversion.setSucursal(invApi.integer("sucursal"));
            inversion.setDescSucursal(invApi.string("descSucursal"));
            inversion.setDescEstado(invApi.string("descEstado"));
            inversion.setEstado(invApi.string("estado"));
            inversion.setFechaAlta(parsearFecha(invApi.string("fechaAlta")));//parsear de dd/mm/yyyy a yyyy-mm-dd
            inversion.setIdDomicilio(invApi.integer("idDomicilio"));
            inversion.setTipoTitularidad(invApi.string("tipoTitularidad"));
            inversion.setDescTipoTitularidad(invApi.string("descTipoTitularidad"));
            inversion.setAdicionales(invApi.bool("adicionales"));
            inversion.setMoneda(Integer.valueOf(invApi.string("moneda")));
            inversion.setDescMoneda(invApi.string("monedaDesc"));
            inversiones.add(inversion);
        }
        return inversiones;
    }

    private static String parsearFecha(String fechaApi){
        LocalDateTime fecha = LocalDateTime.parse(fechaApi);
        String fechaFormateada = fecha.toLocalDate().toString();
        return fechaFormateada;
    }
    /*
    "tipoProducto": "UNI",
            "numeroProducto": "2-000114235",
            "idProducto": "66102950",
            "sucursal": 0,
            "descSucursal": "BUENOS AIRES",
            "descEstado": "VIGENTE",
            "estado": "V",
            "fechaAlta": "2017-09-15",
            "idDomicilio": 5,
            "tipoTitularidad": "T",
            "descTipoTitularidad": "TITULAR",
            "adicionales": false,
            "moneda": 80,
            "descMoneda": "PESOS"
            */
    public static Objeto inversionToObjeto(TASInversionesPConsV4 inv){
        Objeto invObj = new Objeto();
        invObj.set("tipoProducto", inv.getTipoProducto());
        invObj.set("numeroProducto", inv.getNumeroProducto());
        invObj.set("idProducto", inv.getIdProducto());
        invObj.set("sucursal", inv.getSucursal());
        invObj.set("descSucursal", inv.getDescSucursal());
        invObj.set("descEstado", inv.getDescEstado());
        invObj.set("estado", inv.getEstado());
        invObj.set("fechaAlta", inv.getFechaAlta());
        invObj.set("idDomicilio", inv.getIdDomicilio());
        invObj.set("tipoTitularidad", inv.getTipoTitularidad());
        invObj.set("descTipoTitularidad", inv.getDescTipoTitularidad());
        invObj.set("adicionales", inv.getAdicionales());
        invObj.set("moneda", inv.getMoneda());
        invObj.set("descMoneda", inv.getDescMoneda());
        return invObj;
    }
}
