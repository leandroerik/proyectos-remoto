package ar.com.hipotecario.canal.homebanking.negocio.seguro;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;


public class Producto {
    @JsonProperty("ramoIdBHSeg")
    private String ramoIdBHSeg;

    @JsonProperty("productores")
    private List<Object> productores;

    @JsonProperty("productoIdBHSeg")
    private String productoIdBHSeg;

    @JsonProperty("descripcionRamo")
    private String descripcionRamo;

    @JsonProperty("enlatados")
    private List<Enlatado> enlatados;

    @JsonProperty("addOns")
    private List<Object> addOns;

    @JsonProperty("descripcionProducto")
    private String descripcionProducto;

    @JsonProperty("idProductoBase")
    private String idProductoBase;

    @JsonProperty("edadMax")
    private int edadMax;

    @JsonProperty("edadMin")
    private int edadMin;

    public String getRamoIdBHSeg() {
        return ramoIdBHSeg;
    }

    public void setRamoIdBHSeg(String ramoIdBHSeg) {
        this.ramoIdBHSeg = ramoIdBHSeg;
    }

    public List<Object> getProductores() {
        return productores;
    }

    public void setProductores(List<Object> productores) {
        this.productores = productores;
    }

    public String getProductoIdBHSeg() {
        return productoIdBHSeg;
    }

    public void setProductoIdBHSeg(String productoIdBHSeg) {
        this.productoIdBHSeg = productoIdBHSeg;
    }

    public String getDescripcionRamo() {
        return descripcionRamo;
    }

    public void setDescripcionRamo(String descripcionRamo) {
        this.descripcionRamo = descripcionRamo;
    }

    public List<Enlatado> getEnlatados( ) {
        return enlatados;
    }

    public void setEnlatados( List<Enlatado> enlatados ) {
        this.enlatados = enlatados;
    }

    public List<Object> getAddOns() {
        return addOns;
    }

    public void setAddOns( List<Object> addOns ) {
        this.addOns = addOns;
    }

    public String getDescripcionProducto() {
        return descripcionProducto;
    }

    public void setDescripcionProducto(String descripcionProducto) {
        this.descripcionProducto = descripcionProducto;
    }

    public String getIdProductoBase() {
        return idProductoBase;
    }

    public void setIdProductoBase(String idProductoBase) {
        this.idProductoBase = idProductoBase;
    }

    public int getEdadMax() {
        return edadMax;
    }

    public void setEdadMax(int edadMax) {
        this.edadMax = edadMax;
    }

    public int getEdadMin() {
        return edadMin;
    }

    public void setEdadMin(int edadMin) {
        this.edadMin = edadMin;
    }
}
