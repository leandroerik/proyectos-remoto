package ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.aplication.mapper;

import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosGenericosV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.domain.model.TASProductosPConsV4;
import ar.com.hipotecario.canal.tas.modulos.inicio.posicion_consolidadaV4.infrastructure.utils.TASPosicionConsolidadaV4Utils;

import java.util.ArrayList;
import java.util.List;

public class TASProductosV4Mapper {
    /*
    * {
      "descPaquete": " ",
      "descTipoTitularidad": "TITULAR",
      "estado": "V",
      "fechaAlta": "2021-12-21",
      "muestraPaquete": false,
      "numero": "TO-19773",
      "tipo": "RJA",
      "tipoTitularidad": "T",
      "cuentaAsociada": ""
    },
    {
      "codigoPaquete": "48",
      "descPaquete": "BH PACK",
      "descTipoTitularidad": "TITULAR",
      "estado": "A",
      "fechaAlta": "2019-04-25",
      "muestraPaquete": true,
      "numero": "104377",
      "tipo": "PAQ",
      "tipoTitularidad": "T",
      "cuentaAsociada": ""
    }
    * */
    public static TASProductosPConsV4 mapear(TASProductosGenericosV4 producto){
        TASProductosPConsV4 prod = new TASProductosPConsV4();
        prod.setCodigoPaquete(producto.getCodigoPaquete() != null ? producto.getCodigoPaquete() : null);
        prod.setDescPaquete(producto.getDescripcionPaquete());
        prod.setDescTipoTitularidad(producto.getDescTitularidad());
        prod.setEstado(producto.getEstado());
        prod.setFechaAlta(TASPosicionConsolidadaV4Utils.formatearFechaApiV4(producto.getFechaAlta()));
        prod.setMuestraPaquete(Boolean.valueOf(producto.getMuestraPaquete()));
        prod.setNumero(producto.getNumeroProducto());
        prod.setTipo(producto.getTipo());
        prod.setTipoTitularidad(producto.getCodigoTitularidad());
        prod.setCuentaAsociada(producto.getCuentaAsociada());
        return prod;
    }

    /*
    * "id": 80,
    "codigo": "48",
    "descripcion": "BH PACK",
    "fechaAlta": "2019-04-25",
    "sucursal": {
      "codigo": 0
    },
    "tipo": null,
    "estado": {
      "codigo": "A"
    },
    "oficial": 2062,
    "nroPaquete": 104377,
    "categoriaDefault": null,
    "tipoCanalVenta": "12",
    "descCanalVenta": "SUCURSAL",
    "cuentaDeCobro": "",
    "origen": "10",
    "codigoBaja": "",
    "ciclo": "4",
    "direccion": "5",
    "tipoDireccion": "D",
    "resumenMagnetico": "S",
    "titularidad": "T",
    "pagoTarjeta": null,
    "programaRecompensa": "Puntos",
    * */
    public static List<TASProductosPConsV4> mapeoCompleto(Objeto responseApi){
        List<TASProductosPConsV4> paquetes = new ArrayList<>();
        for(Objeto prodApi : responseApi.objetos()){
            TASProductosPConsV4 prod = new TASProductosPConsV4();
            prod.setCodigoPaquete(prodApi.string("codigo"));
            prod.setDescPaquete(prodApi.string("descripcion"));
            prod.setDescTipoTitularidad(prodApi.string("titularidad").equals("T")
                    ? "TITULAR" : prodApi.string("titularidad"));
            prod.setEstado(prodApi.objeto("estado").string("codigo"));
            prod.setFechaAlta(prodApi.string("fechaAlta"));
            prod.setMuestraPaquete(null);
            prod.setNumero(prodApi.string("nroPaquete"));
            prod.setTipo("PAQ");
            prod.setTipoTitularidad(prodApi.string("titularidad"));
            prod.setCuentaAsociada(prodApi.string("cuentaDeCobro"));
            paquetes.add(prod);
        }
        return paquetes;
    }

    public static Objeto paqueteToObjeto(TASProductosPConsV4 paq){
        Objeto paqObj = new Objeto();
        paqObj.set("codigoPaquete", paq.getCodigoPaquete());
        paqObj.set("descPaquete", paq.getDescPaquete());
        paqObj.set("descTipoTitularidad", paq.getDescTipoTitularidad());
        paqObj.set("estado", paq.getEstado());
        paqObj.set("fechaAlta", paq.getFechaAlta());
        paqObj.set("muestraPaquete", paq.getMuestraPaquete());
        paqObj.set("numero", paq.getNumero());
        paqObj.set("tipo", paq.getTipo());
        paqObj.set("tipoTitularidad", paq.getTipoTitularidad());
        paqObj.set("cuentaAsociada", paq.getCuentaAsociada());
        return paqObj;
    }
}
