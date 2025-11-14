package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentacomitente.CNV.investor;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Cliente;
import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.base.Objeto;
import ar.com.hipotecario.canal.homebanking.conector.Api;
import ar.com.hipotecario.canal.homebanking.conector.ApiResponse;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.Domicilio;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.servicio.ProductosService;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;
import ar.com.hipotecario.canal.homebanking.servicio.RestPersona;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigInteger;
import java.text.SimpleDateFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InversorCNV {
    private String tipoActivacion; //activationtype ACTIVE, DEACTIVATED, SUSPENDED, INACTIVE
    private List<DetalleDireccion> detallesDireccion = new ArrayList<>(); //addressdetails
    private String codigoAdministrador; //administratorcode
    private String fechaNacimiento;//birthDate
    private String nacionalidad;//cuidadania citizenship
    private List<DetalleContato> destallesContacto; //contactdetails
    private String paisResidencia; //countryofresidence
    private Ext ext;
    private String nombre; //firstname
    private BigInteger codigoInversor; //investorcode
    private Boolean esParteInteresada; //isinterestedparty
    private Boolean esInversorParticipante; //isparticipantinvestor
    private String apellido; //longname
    private String paisRegistro; //registrationcountry
    private List<IdentificadorInteresado> identificadorInteresado; //stakeholderidentifiers
    private String tipoInversor; //stakeholdertype
    private String paisDomicilioFiscal; //taxdomicile
    private BigInteger numIdentificador; //taxpayeridentifier

    public InversorCNV() {
    }

    public InversorCNV(String tipoActivacion, List<DetalleDireccion> detallesDireccion, String codigoAdministrador, String fechaNacimiento, String nacionalidad, List<DetalleContato> destallesContacto, String paisResidencia, Ext ext, String nombre, BigInteger codigoinversor, Boolean esParteInteresada, Boolean esInversorParticipante, String apellido, String paisRegistro, List<IdentificadorInteresado> identificadorInteresado, String tipoInversor, String paisDomicilioFiscal, BigInteger numIdentificador) {
        this.tipoActivacion = tipoActivacion;
        this.detallesDireccion = detallesDireccion;
        this.codigoAdministrador = codigoAdministrador;
        this.fechaNacimiento = fechaNacimiento;
        this.nacionalidad = nacionalidad;
        this.destallesContacto = destallesContacto;
        this.paisResidencia = paisResidencia;
        this.ext = ext;
        this.nombre = nombre;
        this.codigoInversor = codigoinversor;
        this.esParteInteresada = esParteInteresada;
        this.esInversorParticipante = esInversorParticipante;
        this.apellido = apellido;
        this.paisRegistro = paisRegistro;
        this.identificadorInteresado = identificadorInteresado;
        this.tipoInversor = tipoInversor;
        this.paisDomicilioFiscal = paisDomicilioFiscal;
        this.numIdentificador = numIdentificador;
    }

    public static List<InversorCNV> getInversorCNVCoTitulares(ContextoHB contexto, Cuenta cuentaPesos) {
        List<InversorCNV> inversorCNVList = new ArrayList<>();
//        Cliente clienteCotitular;
        ApiResponse responseProducto = ProductosService.integrantesProducto(contexto, cuentaPesos.numero());
        if (responseProducto.hayError()) {
            return null;
        }
        //Todo: estos cotitulares armar busqueda por CUIT y filtrar los que no fueron dados de alta
        List<Cliente> clientesCotitularesProducto = responseProducto.objetos()
                .stream()
                .filter(p -> p.objeto("rol").string("descripcion").equalsIgnoreCase("COTITULAR"))
                .map(c -> {
                    try {
                        Futuro<Cliente> clienteCotitularFuturo = ApiPersonas.cliente(contexto, c.string("idCliente"));
                        return clienteCotitularFuturo.get();
                    }catch (Exception e){
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();

        if(clientesCotitularesProducto.isEmpty()){
            return null;
        }

        for(Cliente cliente : clientesCotitularesProducto){
            try{
                inversorCNVList.add(getCotitularesByCliente(contexto, cliente));
                if(inversorCNVList==null){
                    return null;
                }
            }catch (Exception e){
                return null;
            }
        }

        if(inversorCNVList == null || inversorCNVList.isEmpty()){
            return null;
        }


//        inversorCNVList = getInversorCNVCoTitulares();

        return inversorCNVList;
    }

    private static InversorCNV getCotitularesByCliente(ContextoHB contextoHB ,Cliente cliente){
        Api.eliminarCache(contextoHB, "Email", contextoHB.idCobis());
        InversorCNV inversorCNV = new InversorCNV();
        ApiResponse responseMail = RestPersona.emails(contextoHB,cliente.cuit);

        if(responseMail.hayError()){
            return null;
        }

        String email = responseMail
                .objetos()
                .stream()
                .filter(m->m.string("idTipoMail").equalsIgnoreCase("EMP"))
                .findFirst()
                .map(m->m.string("direccion"))
                .orElse("notiene@notienemail.com.ar");

        //todo: No se elimina el cache!
//        Api.eliminarCache(contextoHB, "Domicilio", contextoHB.idCobis());
//        Objeto responseDomicilioCliente = RestPersona.domicilios(contextoHB, cliente.cuit);
//        if(responseDomicilioCliente == null){
//            return null;
//        }
//        Objeto domicilio = responseDomicilioCliente
//                .objetos()
//                .stream()
//                .filter(d->d.string("idTipoDomicilio").equalsIgnoreCase("DP"))
//                .findFirst()
//                .orElse(null);

        inversorCNV
                .setApellido(cliente.apellidos)
                .setCodigoAdministrador("") //dato fijo 139 homo 1270 prod
                .setCodigoInversor(new BigInteger(cliente.cuit))
                .setDetallesContacto(
                        Objeto.listOf(new DetalleContato(
                                cliente.nombreCompleto()
                                , email))
                )
                .setDetallesDireccion(
                        Objeto.listOf(new DetalleDireccion(
                                ""
                                , 0 //codigoPostal
                                , ""
                                , ""
                                , "" // RestCatalogo.nombrePais(contexto, persona.idPaisResidencia()) "AR"
                                , "MAIN")
                        )
                )
                .setEsInversorParticipante(true)
                .setEsParteInteresada(false)
                .setExt(
                        new Ext(
                                cliente.idTipoIDTributario,
                                "CONF",
                                "NON_TAX_REGISTERED"
                        )
                )
                .setFechaNacimiento(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(cliente.fechaNacimiento))
                .setIdentificadorInteresado(
                        Objeto.listOf(
                                new IdentificadorInteresado(
                                        cliente.idTipoDocumento, //DNI, OTHERS, CVSAOTHER
                                        Integer.valueOf(cliente.numeroDocumento),
                                        "ARGENTINA",
                                        "" //persona.idPaisResidencia() return 80
                                )
                        )
                )
                .setNacionalidad(RestCatalogo.nombrePais(contextoHB, Integer.valueOf(cliente.idNacionalidad)))//todo: eliminar cache primero
                .setNombre(cliente.nombres)
                .setNumIdentificador(new BigInteger(cliente.cuit))
                .setPaisDomicilioFiscal("ARGENTINA") //todo: NO LO ENVIA EL FRONT
                .setPaisRegistro("ARGENTINA") //todo: ver esto
                .setPaisResidencia(RestCatalogo.nombrePais(contextoHB, 80)) //persona.idPaisResidencia()
                .setTipoActivacion("ACTIVE") // Fijo
                .setTipoInversor("Individual");

        return inversorCNV;
    }

    public String getTipoActivacion() {
        return tipoActivacion;
    }

    public InversorCNV setTipoActivacion(String tipoActivacion) {
        this.tipoActivacion = tipoActivacion;
        return this;
    }

    public List<DetalleDireccion> getDetallesDireccion() {
        return detallesDireccion;
    }

    public InversorCNV setDetallesDireccion(List<DetalleDireccion> detallesDireccion) {
        this.detallesDireccion = detallesDireccion;
        return this;
    }

    public String getCodigoAdministrador() {
        return codigoAdministrador;
    }

    public InversorCNV setCodigoAdministrador(String codigoAdministrador) {
        this.codigoAdministrador = codigoAdministrador;
        return this;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public InversorCNV setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
        return this;
    }

    public String getNacionalidad() {
        return nacionalidad;
    }

    public InversorCNV setNacionalidad(String nacionalidad) {
        this.nacionalidad = nacionalidad;
        return this;
    }

    public List<DetalleContato> getDetallesContacto() {
        return destallesContacto;
    }

    public InversorCNV setDetallesContacto(List<DetalleContato> destallesContacto) {
        this.destallesContacto = destallesContacto;
        return this;
    }

    public String getPaisResidencia() {
        return paisResidencia;
    }

    public InversorCNV setPaisResidencia(String paisResidencia) {
        this.paisResidencia = paisResidencia;
        return this;
    }

    public Ext getExt() {
        return ext;
    }

    public InversorCNV setExt(Ext ext) {
        this.ext = ext;
        return this;
    }

    public String getNombre() {
        return nombre;
    }

    public InversorCNV setNombre(String nombre) {
        this.nombre = nombre;
        return this;
    }

    public BigInteger getCodigoInversor() {
        return codigoInversor;
    }

    public InversorCNV setCodigoInversor(BigInteger codigoinversor) {
        this.codigoInversor = codigoinversor;
        return this;
    }

    public Boolean getEsParteInteresada() {
        return esParteInteresada;
    }

    public InversorCNV setEsParteInteresada(Boolean esParteInteresada) {
        this.esParteInteresada = esParteInteresada;
        return this;
    }

    public Boolean getEsInversorParticipante() {
        return esInversorParticipante;
    }

    public InversorCNV setEsInversorParticipante(Boolean esInversorParticipante) {
        this.esInversorParticipante = esInversorParticipante;
        return this;
    }

    public String getApellido() {
        return apellido;
    }

    public InversorCNV setApellido(String apellido) {
        this.apellido = apellido;
        return this;
    }

    public String getPaisRegistro() {
        return paisRegistro;
    }

    public InversorCNV setPaisRegistro(String paisRegistro) {
        this.paisRegistro = paisRegistro;
        return this;
    }

    public List<IdentificadorInteresado> getIdentificadorInteresado() {
        return identificadorInteresado;
    }

    public InversorCNV setIdentificadorInteresado(List<IdentificadorInteresado> identificadorInteresado) {
        this.identificadorInteresado = identificadorInteresado;
        return this;
    }

    public String getTipoInversor() {
        return tipoInversor;
    }

    public InversorCNV setTipoInversor(String tipoInversor) {
        this.tipoInversor = tipoInversor;
        return this;
    }

    public String getPaisDomicilioFiscal() {
        return paisDomicilioFiscal;
    }

    public InversorCNV setPaisDomicilioFiscal(String paisDomicilioFiscal) {
        this.paisDomicilioFiscal = paisDomicilioFiscal;
        return this;
    }

    public BigInteger getNumIdentificador() {
        return numIdentificador;
    }

    public InversorCNV setNumIdentificador(BigInteger numIdentificador) {
        this.numIdentificador = numIdentificador;
        return this;
    }

    public String toJson() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            return "";
        }
    }

    public static InversorCNV getInversorCNVTitular(ContextoHB contextoHB , Persona persona, Domicilio domicilio) {
        String calleDomicilio = contextoHB.parametros.string("calleDomicilio", null);
        String alturaDomicilio = contextoHB.parametros.string("alturaDomicilio", null);
        String mail = contextoHB.parametros.string("email");
        Integer codigoPostal = contextoHB.parametros.integer("codigoPostal", null);
        Integer nacionalidad = contextoHB.parametros.integer("nacionalidad", contextoHB.persona().idPaisNacimiento());

        InversorCNV inversorCNV = new InversorCNV();
        try{
            inversorCNV
                    .setApellido(persona.apellidos())
                    .setCodigoAdministrador("")     //dato fijo 139 homo 1270 prod
                    .setCodigoInversor(new BigInteger(persona.cuit())) //api-inversiones completa este campo.
                    .setDetallesContacto(
                            Objeto.listOf(new DetalleContato(
                                    persona.nombreCompleto()
                                    , mail))
                    )
                    .setDetallesDireccion(
                            Objeto.listOf(new DetalleDireccion(
                                    calleDomicilio + " " + alturaDomicilio
                                    , codigoPostal
                                    , domicilio.localidad()
                                    , domicilio.string("idProvincia", "")
                                    , RestCatalogo.nombrePais(contextoHB, persona.idPaisResidencia()) // RestCatalogo.nombrePais(contexto, persona.idPaisResidencia()) "AR"
                                    , "MAIN")
                            )
                    )
                    .setEsInversorParticipante(true)
                    .setEsParteInteresada(false)
                    .setExt(
                            new Ext(
                                    persona.tipoTributario(),
                                    "CONF",
                                    "NON_TAX_REGISTERED"//
                            )
                    )
                    .setFechaNacimiento(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSZ").format(persona.fechaNacimiento()))
                    .setIdentificadorInteresado(
                            Objeto.listOf(
                                    new IdentificadorInteresado(
                                            persona.idTipoDocumentoString(), //DNI, OTHERS, CVSAOTHER
                                            Integer.valueOf(persona.numeroDocumento()),
                                            "ARGENTINA",
                                            RestCatalogo.nombrePais(contextoHB, persona.idPaisResidencia())
                                    )
                            )
                    )
                    .setNacionalidad(RestCatalogo.nombrePais(contextoHB, nacionalidad))
                    .setNombre(persona.nombres())
                    .setNumIdentificador(new BigInteger(persona.cuit()))
                    .setPaisDomicilioFiscal("ARGENTINA") //todo: NO LO ENVIA EL FRONT
                    .setPaisRegistro("ARGENTINA") //todo: ver esto solo persona juridica
                    .setPaisResidencia(RestCatalogo.nombrePais(contextoHB, persona.idPaisResidencia()))
                    .setTipoActivacion("ACTIVE") // Fijo
                    .setTipoInversor("Individual");
        }catch (Exception e){
            return null;
        }

        return inversorCNV;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
