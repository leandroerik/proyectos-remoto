package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASTarjetasCreditoPConsV4;

import java.util.ArrayList;
import java.util.List;

public class TASTarjetasCreditoV4Mapper {

    /*
    V3
    *   {
      "idPaquete": 104377,
      "muestraPaquete": true,
      "sucursal": 99,
      "descSucursal": "CASA MATRIZ",
      "descEstado": "TARJ. NORMAL",
      "estado": "20",
      "fechaAlta": "2017-02-24",
      "tipoTitularidad": "T",
      "descTipoTitularidad": "TITULAR",
      "tipoTarjeta": "S",
      "descTipoTarjeta": "Visa Signature",
      "numero": "4420140000079630",
      "cuenta": "0284122540",
      "formaPago": "05",
      "denominacionTarjeta": "BODETTO/DARIO GUSTA",
      "modeloLiquidacion": "142",
      "descModeloLiquidacion": "Modelo 142 - Empleados Signature AA",
      "altaPuntoVenta": "O"
    }*/
    public static List<TASTarjetasCreditoPConsV4> mapearTC(Objeto response){
        List <TASTarjetasCreditoPConsV4> tcList = new ArrayList<>();
        for(Objeto p : response.objetos()){
            TASTarjetasCreditoPConsV4 tc = new TASTarjetasCreditoPConsV4();
            tc.setIdPaquete(p.integer("idPaquete") != null ? p.integer("idPaquete") : null);
            tc.setMuestraPaquete(p.bool("muestraPaquete"));
            tc.setSucursal(p.integer("sucursal"));
            tc.setDescSucursal(p.string("descSucursal"));
            tc.setDescEstado(p.string("descEstado"));
            tc.setEstado(p.string("estado"));
            tc.setFechaAlta(p.string("fechaAlta"));
            tc.setTipoTitularidad(p.string("tipoTitularidad"));
            tc.setDescTipoTitularidad(p.string("descTipoTitularidad"));
            tc.setTipoTarjeta(p.string("tipoTarjeta"));
            tc.setDescTipoTarjeta(p.string("descTipoTarjeta"));
            tc.setNumero(p.string("numero"));
            tc.setCuenta(p.string("cuenta"));
            tc.setFormaPago(p.string("formaPago"));
            tc.setDenominacionTarjeta(p.string("denominacionTarjeta"));
            tc.setModeloLiquidacion(p.string("modeloLiquidacion"));
            tc.setDescModeloLiquidacion(p.string("descModeloLiquidacion"));
            tc.setAltaPuntoVenta(p.string("altaPuntoVenta"));
            tcList.add(tc);
        }
        return tcList;
    }

    public static Objeto tcToObjeto(TASTarjetasCreditoPConsV4 tc){
        Objeto tcObj = new Objeto();
        tcObj.set("idPaquete", tc.getIdPaquete());
        tcObj.set("muestraPaquete", tc.isMuestraPaquete());
        tcObj.set("sucursal", tc.getSucursal());
        tcObj.set("descSucursal", tc.getDescSucursal());
        tcObj.set("descEstado", tc.getDescEstado());
        tcObj.set("estado", tc.getEstado());
        tcObj.set("fechaAlta", tc.getFechaAlta());
        tcObj.set("tipoTitularidad", tc.getTipoTitularidad());
        tcObj.set("descTipoTitularidad", tc.getDescTipoTitularidad());
        tcObj.set("tipoTarjeta", tc.getTipoTarjeta());
        tcObj.set("descTipoTarjeta", tc.getDescTipoTarjeta());
        tcObj.set("numero", tc.getNumero());
        tcObj.set("cuenta", tc.getCuenta());
        tcObj.set("formaPago", tc.getFormaPago());
        tcObj.set("denominacionTarjeta", tc.getDenominacionTarjeta());
        tcObj.set("modeloLiquidacion", tc.getModeloLiquidacion());
        tcObj.set("descModeloLiquidacion", tc.getDescModeloLiquidacion());
        tcObj.set("altaPuntoVenta", tc.getAltaPuntoVenta());
        return tcObj;
    }
}
