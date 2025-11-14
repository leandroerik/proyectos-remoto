package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.account;

import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;


public class CuentaInversorCNV {
    private GrupoCuenta grupoCuentas; //accountgroup
    private Integer identificadorCuenta; //accountidentifier
    private String tipoIdentificadorCuenta; //accountidentifiertype
    private String nombreCuenta; //accountname
    private String tipoActivacion; //activationtype
    private String paisEmision; // countryofissuance
    private String fechaCaducidad; //expirydate
    private List<CoPropietario> coPropietarios; //CoOwner
    private Boolean esAccionCorporativa; //iscorporateaction
    private String entidadEmisora; //issuingentity
    private String identificadorOperador; //operatoridentifier
    private String tipoOperador; // operatortype
    private String fechaRegistro; // registrationdate
    private List<ValoresCuenta> valoresCuentas; // securitiesaccount significa valores cuentas?

    public CuentaInversorCNV() {
    }

    public CuentaInversorCNV(GrupoCuenta grupoCuentas, Integer identificadorCuenta, String tipoIdentificadorCuenta, String nombreCuenta, String tipoActivacion, String paisEmision, String fechaCaducidad, Holder holder, List<CoPropietario> coPropietarios, boolean esAccionCorporativa, String entidadEmisora, String identificadorOperador, String tipoOperador, String fechaRegistro, List<ValoresCuenta> valoresCuentas) {
        this.grupoCuentas = grupoCuentas;
        this.identificadorCuenta = identificadorCuenta;
        this.tipoIdentificadorCuenta = tipoIdentificadorCuenta;
        this.nombreCuenta = nombreCuenta;
        this.tipoActivacion = tipoActivacion;
        this.paisEmision = paisEmision;
        this.fechaCaducidad = fechaCaducidad;
        this.coPropietarios = coPropietarios;
        this.esAccionCorporativa = esAccionCorporativa;
        this.entidadEmisora = entidadEmisora;
        this.identificadorOperador = identificadorOperador;
        this.tipoOperador = tipoOperador;
        this.fechaRegistro = fechaRegistro;
        this.valoresCuentas = valoresCuentas;
    }

    public GrupoCuenta getGrupoCuentas() {
        return grupoCuentas;
    }
    public CuentaInversorCNV setGrupoCuentas(GrupoCuenta grupoCuentas) {
        this.grupoCuentas = grupoCuentas;
        return this;
    }
    public Integer getIdentificadorCuenta() {
        return identificadorCuenta;
    }

    public CuentaInversorCNV setIdentificadorCuenta(Integer identificadorCuenta) {
        this.identificadorCuenta = identificadorCuenta;
        return this;
    }
    public String getTipoIdentificadorCuenta() {
        return tipoIdentificadorCuenta;
    }

    public CuentaInversorCNV setTipoIdentificadorCuenta(String tipoIdentificadorCuenta) {
        this.tipoIdentificadorCuenta = tipoIdentificadorCuenta;
        return this;
    }
    public String getNombreCuenta() {
        return nombreCuenta;
    }
    public CuentaInversorCNV setNombreCuenta(String nombreCuenta) {
        this.nombreCuenta = nombreCuenta;
        return this;
    }
    public String getTipoActivacion() {
        return tipoActivacion;
    }
    public CuentaInversorCNV setTipoActivacion(String tipoActivacion) {
        this.tipoActivacion = tipoActivacion;
        return this;
    }
    public String getPaisEmision() {
        return paisEmision;
    }
    public CuentaInversorCNV setPaisEmision(String paisEmision) {
        this.paisEmision = paisEmision;
        return this;
    }
    public String getFechaCaducidad() {
        return fechaCaducidad;
    }
    public CuentaInversorCNV setFechaCaducidad(String fechaCaducidad) {
        this.fechaCaducidad = fechaCaducidad;
        return this;
    }
    public List<CoPropietario> getCoPropietarios() {
        return coPropietarios;
    }
    public CuentaInversorCNV setCoPropietarios(List<CoPropietario> coPropietarios) {
        this.coPropietarios = coPropietarios;
        return this;
    }
    public Boolean getEsAccionCorporativa() {
        return esAccionCorporativa;
    }
    public CuentaInversorCNV setEsAccionCorporativa(Boolean esAccionCorporativa) {
        this.esAccionCorporativa = esAccionCorporativa;
        return this;
    }
    public String getEntidadEmisora() {
        return entidadEmisora;
    }
    public CuentaInversorCNV setEntidadEmisora(String entidadEmisora) {
        this.entidadEmisora = entidadEmisora;
        return this;
    }
    public String getIdentificadorOperador() {
        return identificadorOperador;
    }
    public CuentaInversorCNV setIdentificadorOperador(String identificadorOperador) {
        this.identificadorOperador = identificadorOperador;
        return this;
    }
    public String getTipoOperador() {
        return tipoOperador;
    }
    public CuentaInversorCNV setTipoOperador(String tipoOperador) {
        this.tipoOperador = tipoOperador;
        return this;
    }
    public String getFechaRegistro() {
        return fechaRegistro;
    }
    public CuentaInversorCNV setFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
        return this;
    }
    public List<ValoresCuenta> getValoresCuentas() {
        return valoresCuentas;
    }
    public CuentaInversorCNV setValoresCuentas(List<ValoresCuenta> valoresCuentas) {
        this.valoresCuentas = valoresCuentas;
        return this;
    }

    public static CuentaInversorCNV getValue(Persona persona){
        CuentaInversorCNV cuentaInversorCNV = new CuentaInversorCNV();
        cuentaInversorCNV
                .setGrupoCuentas(
                        new GrupoCuenta(
                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date()), //Fecha de desactivación del grupo de cuentas. Recomiendan fecha del momento
                                "Allow"
                        )
                )
                .setIdentificadorCuenta(1)//Numero de cuentaComitente en Unitrade - Este valor se pisa en api-inversiones con el nro de cuenta Comitente
                .setTipoIdentificadorCuenta("PROPRIETARY")
                .setNombreCuenta(persona.nombreCompleto())
                .setTipoActivacion("ACTIVE")
                .setPaisEmision("ARGENTINA")
                .setFechaCaducidad(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date())) //Fecha de desactivación del grupo de cuentas. Recomiendan fecha del momento
                .setCoPropietarios(
                        Objeto.listOf(
                                new CoPropietario(
                                        new Propietario(
                                                persona.numeroDocumento(),
                                                persona.idTipoDocumentoString()),
                                        true

                                )//add coPropietario
                        )
                )
                .setEsAccionCorporativa(false)//Indica si la cuenta es para acciones corporativas. Para los tipos de cuentas que estamos trabajando, dejarlo en false.
                .setEntidadEmisora("ARGENTINA")//todo validar País de la Entidad emisora.
                .setIdentificadorOperador("") //Numero de depositante 139 - 1270
                .setTipoOperador("PCODE") //solo permite este valor
                .setFechaRegistro(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date()))
                .setValoresCuentas(
                        Objeto.listOf(
                                new ValoresCuenta(
                                        "",//No obligatorio
                                        "",//No obligatorio
                                        new CaCuentaDistribucion(
                                                "PROPRIETARY", //Allowed Value: PROPRIETARY
                                                "" //No requerido - identificador - Se guarda el mismo valor qe nroCuetaComitente (Lo hace api-inversiones)
                                        ),
                                        "",//No requerido - Comentarios adicionales.
                                        0,//No hay documentacion
                                        false, //No requerido, por defecto false
                                        false, //No requerido, por defecto false
                                        new Emisor(
                                                "ARGENTINA",//No requerido País de emisión.
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date()), // requerido - Fecha de vencimiento.
                                                "", //No requerido - Identificador único.
                                                "ARGENTINA", //PaisEntidodadEmisora
                                                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(new Date()), //fecha de registro
                                                "PCODE" //No requerido - Tipo de identificador. Allowed value PCODE
                                        ),
                                        new ValoresCuentaExtension(
                                                false, //Se coloca en false por defecto
                                                false, //Se coloca en false por defecto
                                                false //Se coloca en false por defecto

                                        ),
                                        "CO_OWNER_ACCOUNT"
                                )
                        )
                )
        ;
        return cuentaInversorCNV;
    }





    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }
}
