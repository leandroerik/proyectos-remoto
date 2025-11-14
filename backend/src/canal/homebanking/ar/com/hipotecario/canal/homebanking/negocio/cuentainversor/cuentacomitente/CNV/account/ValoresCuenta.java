package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValoresCuenta {
    public String idAlternativo; //alternateId
    public String referenciaFacturacion; //billingReference
    public CaCuentaDistribucion caCuentaDistribucion; //caDistributionAccount
    public String comentario; //comments
    public Integer id; //id
    public Boolean esNotificacionISOAccionCorporativa; //isCorporateActionISONotification
    public Boolean esCuentaEspejoCsdEmisorVinculadoPorDefecto; //isDefaultLinkedIssuerCsdMirrorAccount
    public Emisor emisor; //emisor
    public ValoresCuentaExtension valoresCuentaExtension; //securitiesAccountExtension
    public String tipoCuentaComitente; //securitiesAccountType // Cuentas de único inversor: BENEFICIAL_OWNER_ACCOUNT Cuentas con más de un inversor: CO_OWNER_ACCOUNT
    public ValoresCuenta() {
    }
    public ValoresCuenta(String idAlternativo, String referenciaFacturacion, CaCuentaDistribucion caCuentaDistribucion, String comentario, Integer id, Boolean esNotificacionISOAccionCorporativa, Boolean esCuentaEspejoCsdEmisorVinculadoPorDefecto, Emisor emisor, ValoresCuentaExtension valoresCuentaExtension, String tipoCuentaComitente) {
        this.idAlternativo = idAlternativo;
        this.referenciaFacturacion = referenciaFacturacion;
        this.caCuentaDistribucion = caCuentaDistribucion;
        this.comentario = comentario;
        this.id = id;
        this.esNotificacionISOAccionCorporativa = esNotificacionISOAccionCorporativa;
        this.esCuentaEspejoCsdEmisorVinculadoPorDefecto = esCuentaEspejoCsdEmisorVinculadoPorDefecto;
        this.emisor = emisor;
        this.valoresCuentaExtension = valoresCuentaExtension;
        this.tipoCuentaComitente = tipoCuentaComitente;
    }

    public String getIdAlternativo() {
        return idAlternativo;
    }
    public void setIdAlternativo(String idAlternativo) {
        this.idAlternativo = idAlternativo;
    }
    public String getReferenciaFacturacion() {
        return referenciaFacturacion;
    }
    public void setReferenciaFacturacion(String referenciaFacturacion) {
        this.referenciaFacturacion = referenciaFacturacion;
    }
    public CaCuentaDistribucion getCaCuentaDistribucion() {
        return caCuentaDistribucion;
    }
    public void setCaCuentaDistribucion(CaCuentaDistribucion caCuentaDistribucion) {
        this.caCuentaDistribucion = caCuentaDistribucion;
    }
    public String getComentario() {
        return comentario;
    }
    public void setComentario(String comentario) {
        this.comentario = comentario;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public Boolean getEsNotificacionISOAccionCorporativa() {
        return esNotificacionISOAccionCorporativa;
    }
    public void setEsNotificacionISOAccionCorporativa(Boolean esNotificacionISOAccionCorporativa) {
        this.esNotificacionISOAccionCorporativa = esNotificacionISOAccionCorporativa;
    }
    public Boolean getEsCuentaEspejoCsdEmisorVinculadoPorDefecto() {
        return esCuentaEspejoCsdEmisorVinculadoPorDefecto;
    }
    public void setEsCuentaEspejoCsdEmisorVinculadoPorDefecto(Boolean esCuentaEspejoCsdEmisorVinculadoPorDefecto) {
        this.esCuentaEspejoCsdEmisorVinculadoPorDefecto = esCuentaEspejoCsdEmisorVinculadoPorDefecto;
    }
    public Emisor getEmisor() {
        return emisor;
    }
    public void setEmisor(Emisor emisor) {
        this.emisor = emisor;
    }
    public ValoresCuentaExtension getValoresCuentaExtension() {
        return valoresCuentaExtension;
    }
    public void setValoresCuentaExtension(ValoresCuentaExtension valoresCuentaExtension) {
        this.valoresCuentaExtension = valoresCuentaExtension;}
    public String getTipoCuentaComitente() {
        return tipoCuentaComitente;
    }
    public void setTipoCuentaComitente(String tipoCuentaComitente) {
        this.tipoCuentaComitente = tipoCuentaComitente;}
}
