package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASInversionesPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASPrestamosPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.ArrayList;
import java.util.List;

public class TASPrestamosV4Mapper {

    /*
    *{
            "muestraPaquete": false,
            "tipoProducto": "CCA",
            "numeroProducto": "0040059029",
            "idProducto": "52153258",
            "sucursal": 99,
            "descSucursal": "CASA CENTRAL",
            "descEstado": "VIGENTE",
            "estado": "V",
            "fechaAlta": "2007-03-12",
            "idDomicilio": "3",
            "tipoTitularidad": "D",
            "descTipoTitularidad": "DEUDOR PRINCIPAL DE UN PRESTAMO",
            "adicionales": false,
            "moneda": "80",
            "descMoneda": "PESOS",
            "hipotecarioNSP": " ",
            "codigoProducto": "HIPO$",
            "montoAprobado": 300000.0,
            "fechaProxVencimiento": "2022-08-16",
            "formaPago": "NDMNCA",
            "esPrecodeu": "N",
            "esProCrear": "N",
            "cantCuotasMora": 6,
            "montoCuotaActual": 3092.94,
            "plazoOriginal": 240
        }
    * */
    public static TASPrestamosPConsV4 mapear(TASProductosGenericosV4 producto){
        TASPrestamosPConsV4 prestamo = new TASPrestamosPConsV4();
        prestamo.setMuestraPaquete(Boolean.valueOf(producto.getMuestraPaquete()));
        prestamo.setTipoProducto(producto.getTipo());
        prestamo.setNumeroProducto(producto.getNumeroProducto());
        prestamo.setIdProducto(String.valueOf(producto.getDetProducto()));
        prestamo.setSucursal(null);
        prestamo.setDescSucursal(null);
        prestamo.setDescEstado(null);
        prestamo.setEstado(producto.getEstado());
        prestamo.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));
        prestamo.setIdDomicilio(null);
        prestamo.setTipoTitularidad(producto.getCodigoTitularidad());
        prestamo.setDescTipoTitularidad(producto.getDescTitularidad());
        prestamo.setAdicionales(null);
        prestamo.setMoneda(String.valueOf(producto.getCodMoneda()));
        prestamo.setDescMoneda(TASPosicionConsolidadaV4Utils.verificarMoneda(producto.getCodMoneda()));
        prestamo.setHipotecarioNSP(null);
        prestamo.setCodigoProducto(null);
        prestamo.setMontoAprobado(null);
        prestamo.setFechaProxVencimiento(null);
        prestamo.setFormaPago(null);
        prestamo.setEsPrecodeu(null);
        prestamo.setEsProCrear(null);
        prestamo.setCantCuotasMora(null);
        prestamo.setMontoCuotaActual(null);
        prestamo.setPlazoOriginal(null);
        return prestamo;
    }

    public static TASPrestamosPConsV4 enriquecerPrestamo(TASPrestamosPConsV4 prestamo, Objeto prestamoApi){
        if(prestamoApi.string("numeroProducto").equals(prestamo.getNumeroProducto())) {
            prestamo.setSucursal(prestamoApi.integer("sucursal"));
            prestamo.setDescSucursal(prestamoApi.string("descSucursal"));
            prestamo.setDescEstado(prestamoApi.string("descEstado"));
            prestamo.setIdDomicilio(String.valueOf(prestamoApi.integer("idDomicilio")));
            prestamo.setAdicionales(prestamoApi.bool("adicionales"));
            prestamo.setHipotecarioNSP(prestamoApi.string("hipotecarioNSP"));
            prestamo.setCodigoProducto(prestamoApi.string("codProducto"));
            prestamo.setMontoAprobado(prestamoApi.bigDecimal("montoAprobado"));
            prestamo.setFechaProxVencimiento(prestamoApi.string("fechaProximoVenc"));
            prestamo.setFormaPago(prestamoApi.string("formaPago"));
            prestamo.setEsPrecodeu(prestamoApi.string("esPrecodeu"));
            prestamo.setEsProCrear(prestamoApi.string("esProcrear"));
            prestamo.setCantCuotasMora(prestamoApi.integer("cantCuotasMora"));
            prestamo.setMontoCuotaActual(prestamoApi.bigDecimal("montoCuotaActual"));
            prestamo.setPlazoOriginal(prestamoApi.integer("plazoOriginal"));
        }
        return prestamo;
    }

    /*
    * {
    "muestraPaquete": false,
    "tipoProducto": "CCA",
    "numeroProducto": "0040059029",
    "idProducto": "52153258",
    "sucursal": 99,
    "descSucursal": "CASA CENTRAL",
    "estado": "V",
    "descEstado": "VIGENTE",
    "fechaAlta": "2007-03-12",
    "idDomicilio": 3,
    "tipoTitularidad": "D",
    "descTipoTitularidad": "DEUDOR PRINCIPAL DE UN PRESTAMO",
    "adicionales": false,
    "moneda": "80",
    "descMoneda": "PESOS",
    "codProducto": "HIPO$",
    "hipotecarioNSP": "",
    "montoAprobado": 300000,
    "fechaProximoVenc": "2022-08-16",
    "formaPago": "NDMNCA",
    "plazoOriginal": 240,
    "esPrecodeu": "N",
    "esProcrear": "N",
    "categoria": "HIPOTECARIO",
    "cantCuotasMora": 6,
    "montoCuotaActual": 3092.94
  }
    * */
    public static List<TASPrestamosPConsV4> mapeoCompleto(Objeto apiResponse){
        List<TASPrestamosPConsV4> prestamos = new ArrayList<>();
        for(Objeto prestamoApi : apiResponse.objetos()){
            TASPrestamosPConsV4 prestamo = new TASPrestamosPConsV4();
            prestamo.setMuestraPaquete(prestamoApi.bool("muestraPaquete"));
            prestamo.setTipoProducto(prestamoApi.string("tipoProducto"));
            prestamo.setNumeroProducto(prestamoApi.string("numeroProducto"));
            prestamo.setIdProducto(prestamoApi.string("idProducto"));
            prestamo.setSucursal(prestamoApi.integer("sucursal"));
            prestamo.setDescSucursal(prestamoApi.string("descSucursal"));
            prestamo.setDescEstado(prestamoApi.string("descEstado"));
            prestamo.setEstado(prestamoApi.string("estado"));
            prestamo.setFechaAlta(prestamoApi.string("fechaAlta"));
            prestamo.setIdDomicilio(String.valueOf(prestamoApi.integer("idDomicilio")));
            prestamo.setTipoTitularidad(prestamoApi.string("tipoTitularidad"));
            prestamo.setDescTipoTitularidad(prestamoApi.string("descTipoTitularidad"));
            prestamo.setAdicionales(prestamoApi.bool("adicionales"));
            prestamo.setMoneda(prestamoApi.string("moneda"));
            prestamo.setDescMoneda(prestamoApi.string("descMoneda"));
            prestamo.setHipotecarioNSP(prestamoApi.string("hipotecarioNSP"));
            prestamo.setCodigoProducto(prestamoApi.string("codProducto"));
            prestamo.setMontoAprobado(prestamoApi.bigDecimal("montoAprobado"));
            prestamo.setFechaProxVencimiento(prestamoApi.string("fechaProximoVenc"));
            prestamo.setFormaPago(prestamoApi.string("formaPago"));
            prestamo.setEsPrecodeu(prestamoApi.string("esPrecodeu"));
            prestamo.setEsProCrear(prestamoApi.string("esProcrear"));
            prestamo.setCantCuotasMora(prestamoApi.integer("cantCuotasMora"));
            prestamo.setMontoCuotaActual(prestamoApi.bigDecimal("montoCuotaActual"));
            prestamo.setPlazoOriginal(prestamoApi.integer("plazoOriginal"));
            prestamos.add(prestamo);
        }
        return prestamos;
    }

    public static Objeto prestamoToObjeto(TASPrestamosPConsV4 prestamo){
        Objeto prestamoObj = new Objeto();
        prestamoObj.set("muestraPaquete", prestamo.isMuestraPaquete());
        prestamoObj.set("tipoProducto", prestamo.getTipoProducto());
        prestamoObj.set("numeroProducto", prestamo.getNumeroProducto());
        prestamoObj.set("idProducto", prestamo.getIdProducto());
        prestamoObj.set("sucursal", prestamo.getSucursal());
        prestamoObj.set("descSucursal", prestamo.getDescSucursal());
        prestamoObj.set("descEstado", prestamo.getDescEstado());
        prestamoObj.set("estado", prestamo.getEstado());
        prestamoObj.set("fechaAlta", prestamo.getFechaAlta());
        prestamoObj.set("idDomicilio", prestamo.getIdDomicilio());
        prestamoObj.set("tipoTitularidad", prestamo.getTipoTitularidad());
        prestamoObj.set("descTipoTitularidad", prestamo.getDescTipoTitularidad());
        prestamoObj.set("adicionales", prestamo.getAdicionales());
        prestamoObj.set("moneda", prestamo.getMoneda());
        prestamoObj.set("descMoneda", prestamo.getDescMoneda());
        prestamoObj.set("hipotecarioNSP", prestamo.getHipotecarioNSP());
        prestamoObj.set("codigoProducto", prestamo.getCodigoProducto());
        prestamoObj.set("montoAprobado", prestamo.getMontoAprobado());
        prestamoObj.set("fechaProxVencimiento", prestamo.getFechaProxVencimiento());
        prestamoObj.set("formaPago", prestamo.getFormaPago());
        prestamoObj.set("esPrecodeu", prestamo.getEsPrecodeu());
        prestamoObj.set("esProCrear", prestamo.getEsProCrear());
        prestamoObj.set("cantCuotasMora", prestamo.getCantCuotasMora());
        prestamoObj.set("montoCuotaActual", prestamo.getMontoCuotaActual());
        prestamoObj.set("plazoOriginal", prestamo.getPlazoOriginal());
        return prestamoObj;
    }
}
