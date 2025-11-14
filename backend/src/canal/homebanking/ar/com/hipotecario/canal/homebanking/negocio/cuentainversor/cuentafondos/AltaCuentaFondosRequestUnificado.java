package ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.cuentafondos;

import ar.com.hipotecario.canal.homebanking.ContextoHB;
import ar.com.hipotecario.canal.homebanking.negocio.Cuenta;
import ar.com.hipotecario.canal.homebanking.negocio.Domicilio;
import ar.com.hipotecario.canal.homebanking.negocio.Persona;
import ar.com.hipotecario.canal.homebanking.negocio.Telefono;
import ar.com.hipotecario.canal.homebanking.negocio.cuentainversor.*;
import ar.com.hipotecario.canal.homebanking.servicio.RestCatalogo;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class AltaCuentaFondosRequestUnificado {
    private String telefono;
    private String idPersonaFondo;
    private String cuotaPartista;
    private String actividadPrincipal;
    private String categoria;
    private DomicilioCuentaInversor domicilio;
    private String email;
    private Boolean esFisico;
    private String fechaIngreso;
    private Boolean imprimeResumenCuenta;
    private String nombre;
    private String numCuotapartista;
    private Origen origen;
    private String perfil;
    private Boolean poseeInstrPagoPerm;
    private Radicacion radicacion;
    private Boolean requiereFirmaConjunta;
    private String segmentoInversion;
    private String tipoCuotapartista;
    private String tipoInversor;
    private String tipoPerfilRiesgo;
    private List<CuotasBancarias> cuotasBancarias;
    private DatosUIF datosUIF;
    private String representantes;
    private String tarjetasCredito;
    private String idPersonaCondomino;
    private String idPersona;
    private String apellido;
    private String cuit;
    private Boolean esPEP;
    private Boolean esmasculino;
    private String fechaNacimiento;
    private String numDoc;
    private String paisNacimiento;
    private String paisNacionalidad;
    private String patrimonio;
    private String idBanco;
    private String promedioMensual;
    private String tipoContribuyente;
    private String tipoDoc;
    private String tipoEstadoCivil;

    public AltaCuentaFondosRequestUnificado() {
    }

    public AltaCuentaFondosRequestUnificado(String telefono, String idPersonaFondo, String cuotaPartista, String actividadPrincipal, String categoria, DomicilioCuentaInversor domicilio, String email, Boolean esFisico, String fechaIngreso, Boolean imprimeResumenCuenta, String nombre, String numCuotapartista, Origen origen, String perfil, Boolean poseeInstrPagoPerm, Radicacion radicacion, Boolean requiereFirmaConjunta, String segmentoInversion, String tipoCuotapartista, String tipoInversor, String tipoPerfilRiesgo, List<CuotasBancarias> cuotasBancarias, DatosUIF datosUIF, String representantes, String tarjetasCredito, String idPersonaCondomino, String idPersona, String apellido, String cuit, Boolean esPEP, Boolean esmasculino, String fechaNacimiento, String numDoc, String paisNacimiento, String paisNacionalidad, String patrimonio, String idBanco, String promedioMensual, String tipoContribuyente, String tipoDoc, String tipoEstadoCivil) {
        this.telefono = telefono;
        this.idPersonaFondo = idPersonaFondo;
        this.cuotaPartista = cuotaPartista;
        this.actividadPrincipal = actividadPrincipal;
        this.categoria = categoria;
        this.domicilio = domicilio;
        this.email = email;
        this.esFisico = esFisico;
        this.fechaIngreso = fechaIngreso;
        this.imprimeResumenCuenta = imprimeResumenCuenta;
        this.nombre = nombre;
        this.numCuotapartista = numCuotapartista;
        this.origen = origen;
        this.perfil = perfil;
        this.poseeInstrPagoPerm = poseeInstrPagoPerm;
        this.radicacion = radicacion;
        this.requiereFirmaConjunta = requiereFirmaConjunta;
        this.segmentoInversion = segmentoInversion;
        this.tipoCuotapartista = tipoCuotapartista;
        this.tipoInversor = tipoInversor;
        this.tipoPerfilRiesgo = tipoPerfilRiesgo;
        this.cuotasBancarias = cuotasBancarias;
        this.datosUIF = datosUIF;
        this.representantes = representantes;
        this.tarjetasCredito = tarjetasCredito;
        this.idPersonaCondomino = idPersonaCondomino;
        this.idPersona = idPersona;
        this.apellido = apellido;
        this.cuit = cuit;
        this.esPEP = esPEP;
        this.esmasculino = esmasculino;
        this.fechaNacimiento = fechaNacimiento;
        this.numDoc = numDoc;
        this.paisNacimiento = paisNacimiento;
        this.paisNacionalidad = paisNacionalidad;
        this.patrimonio = patrimonio;
        this.idBanco = idBanco;
        this.promedioMensual = promedioMensual;
        this.tipoContribuyente = tipoContribuyente;
        this.tipoDoc = tipoDoc;
        this.tipoEstadoCivil = tipoEstadoCivil;
    }

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getIdPersonaFondo() {
        return idPersonaFondo;
    }

    public void setIdPersonaFondo(String idPersonaFondo) {
        this.idPersonaFondo = idPersonaFondo;
    }

    public String getCuotaPartista() {
        return cuotaPartista;
    }

    public void setCuotaPartista(String cuotaPartista) {
        this.cuotaPartista = cuotaPartista;
    }

    public String getActividadPrincipal() {
        return actividadPrincipal;
    }

    public void setActividadPrincipal(String actividadPrincipal) {
        this.actividadPrincipal = actividadPrincipal;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public DomicilioCuentaInversor getDomicilio() {
        return domicilio;
    }

    public void setDomicilio(DomicilioCuentaInversor domicilio) {
        this.domicilio = domicilio;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Boolean getEsFisico() {
        return esFisico;
    }

    public void setEsFisico(Boolean esFisico) {
        this.esFisico = esFisico;
    }

    public String getFechaIngreso() {
        return fechaIngreso;
    }

    public void setFechaIngreso(String fechaIngreso) {
        this.fechaIngreso = fechaIngreso;
    }

    public Boolean getImprimeResumenCuenta() {
        return imprimeResumenCuenta;
    }

    public void setImprimeResumenCuenta(Boolean imprimeResumenCuenta) {
        this.imprimeResumenCuenta = imprimeResumenCuenta;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getNumCuotapartista() {
        return numCuotapartista;
    }

    public void setNumCuotapartista(String numCuotapartista) {
        this.numCuotapartista = numCuotapartista;
    }

    public Origen getOrigen() {
        return origen;
    }

    public void setOrigen(Origen origen) {
        this.origen = origen;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Boolean getPoseeInstrPagoPerm() {
        return poseeInstrPagoPerm;
    }

    public void setPoseeInstrPagoPerm(Boolean poseeInstrPagoPerm) {
        this.poseeInstrPagoPerm = poseeInstrPagoPerm;
    }

    public Radicacion getRadicacion() {
        return radicacion;
    }

    public void setRadicacion(Radicacion radicacion) {
        this.radicacion = radicacion;
    }

    public Boolean getRequiereFirmaConjunta() {
        return requiereFirmaConjunta;
    }

    public void setRequiereFirmaConjunta(Boolean requiereFirmaConjunta) {
        this.requiereFirmaConjunta = requiereFirmaConjunta;
    }

    public String getSegmentoInversion() {
        return segmentoInversion;
    }

    public void setSegmentoInversion(String segmentoInversion) {
        this.segmentoInversion = segmentoInversion;
    }

    public String getTipoCuotapartista() {
        return tipoCuotapartista;
    }

    public void setTipoCuotapartista(String tipoCuotapartista) {
        this.tipoCuotapartista = tipoCuotapartista;
    }

    public String getTipoInversor() {
        return tipoInversor;
    }

    public void setTipoInversor(String tipoInversor) {
        this.tipoInversor = tipoInversor;
    }

    public String getTipoPerfilRiesgo() {
        return tipoPerfilRiesgo;
    }

    public void setTipoPerfilRiesgo(String tipoPerfilRiesgo) {
        this.tipoPerfilRiesgo = tipoPerfilRiesgo;
    }

    public List<CuotasBancarias> getCuotasBancarias() {
        return cuotasBancarias;
    }

    public void setCuotasBancarias(List<CuotasBancarias> cuotasBancarias) {
        this.cuotasBancarias = cuotasBancarias;
    }

    public DatosUIF getDatosUIF() {
        return datosUIF;
    }

    public void setDatosUIF(DatosUIF datosUIF) {
        this.datosUIF = datosUIF;
    }

    public String getRepresentantes() {
        return representantes;
    }

    public void setRepresentantes(String representantes) {
        this.representantes = representantes;
    }

    public String getTarjetasCredito() {
        return tarjetasCredito;
    }

    public void setTarjetasCredito(String tarjetasCredito) {
        this.tarjetasCredito = tarjetasCredito;
    }

    public String getIdPersonaCondomino() {
        return idPersonaCondomino;
    }

    public void setIdPersonaCondomino(String idPersonaCondomino) {
        this.idPersonaCondomino = idPersonaCondomino;
    }

    public String getIdPersona() {
        return idPersona;
    }

    public void setIdPersona(String idPersona) {
        this.idPersona = idPersona;
    }

    public String getApellido() {
        return apellido;
    }

    public void setApellido(String apellido) {
        this.apellido = apellido;
    }

    public String getCuit() {
        return cuit;
    }

    public void setCuit(String cuit) {
        this.cuit = cuit;
    }

    public Boolean getEsPEP() {
        return esPEP;
    }

    public void setEsPEP(Boolean esPEP) {
        this.esPEP = esPEP;
    }

    public Boolean getEsmasculino() {
        return esmasculino;
    }

    public void setEsmasculino(Boolean esmasculino) {
        this.esmasculino = esmasculino;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getNumDoc() {
        return numDoc;
    }

    public void setNumDoc(String numDoc) {
        this.numDoc = numDoc;
    }

    public String getPaisNacimiento() {
        return paisNacimiento;
    }

    public void setPaisNacimiento(String paisNacimiento) {
        this.paisNacimiento = paisNacimiento;
    }

    public String getPaisNacionalidad() {
        return paisNacionalidad;
    }

    public void setPaisNacionalidad(String paisNacionalidad) {
        this.paisNacionalidad = paisNacionalidad;
    }

    public String getPatrimonio() {
        return patrimonio;
    }

    public void setPatrimonio(String patrimonio) {
        this.patrimonio = patrimonio;
    }

    public String getIdBanco() {
        return idBanco;
    }

    public void setIdBanco(String idBanco) {
        this.idBanco = idBanco;
    }

    public String getPromedioMensual() {
        return promedioMensual;
    }

    public void setPromedioMensual(String promedioMensual) {
        this.promedioMensual = promedioMensual;
    }

    public String getTipoContribuyente() {
        return tipoContribuyente;
    }

    public void setTipoContribuyente(String tipoContribuyente) {
        this.tipoContribuyente = tipoContribuyente;
    }

    public String getTipoDoc() {
        return tipoDoc;
    }

    public void setTipoDoc(String tipoDoc) {
        this.tipoDoc = tipoDoc;
    }

    public String getTipoEstadoCivil() {
        return tipoEstadoCivil;
    }

    public void setTipoEstadoCivil(String tipoEstadoCivil) {
        this.tipoEstadoCivil = tipoEstadoCivil;
    }

    public static AltaCuentaFondosRequestUnificado getValue(ContextoHB contexto, Telefono telefono, Domicilio domicilio, Persona persona, Cuenta cuentaPesos, Cuenta cuentaDolar) {
        Integer idPaisNacimiento = contexto.parametros.integer("idPaisNacimiento", contexto.persona().idPaisNacimiento());
        String alturaDomicilio = contexto.parametros.string("alturaDomicilio", null);
        String calleDomicilio = contexto.parametros.string("calleDomicilio", null);
        String codigoPostal = contexto.parametros.string("codigoPostal", null);
        String email = contexto.parametros.string("email", null);
        String codigoAreaCelular = contexto.parametros.string("codigoArea", null);
        String caracteristicaCelular = contexto.parametros.string("caracteristicaCelular", null);
        String numeroCelular = contexto.parametros.string("numeroCelular", null);
        Boolean esPEP = contexto.parametros.bool("esPersonaExpuestaPoliticamente", contexto.persona().esPersonaExpuestaPoliticamente());

        //Forzamos el 54 porque todos son de Argentina.
        String vfnet = "(54)" + "(" + codigoAreaCelular + ")" + caracteristicaCelular + "-" + numeroCelular;

        AltaCuentaFondosRequestUnificado cf = new AltaCuentaFondosRequestUnificado();
        cf.setTelefono(vfnet);
//        cf.setIdPersonaFondo("idPersonaFondo"); Lo cargamos despues por afuera
        cf.setCuotaPartista(generarIDUnico());
        cf.setActividadPrincipal("");
        cf.setCategoria("1");

        DomicilioCuentaInversor d = new DomicilioCuentaInversor();
        d.setAlturaCalle(alturaDomicilio);
        d.setCalle(calleDomicilio);
        d.setCp(codigoPostal);
        d.setLocalidad(domicilio.localidad());
        d.setPais(RestCatalogo.nombrePais(contexto, persona.idPaisResidencia()));
        d.setProvincia(domicilio.string("idProvincia", ""));
        cf.setDomicilio(d);

        cf.setEmail(email.toLowerCase());
        cf.setEsFisico(persona.esPersonaFisica());
        //cf.setFechaIngreso();//todo ver estoLocalDate.now().toString()
        cf.setImprimeResumenCuenta(true);
        cf.setNombre(fixCaracteresEspeciales(persona.nombres()));
        cf.setNumCuotapartista("1");

        Origen origen1 = new Origen();
        origen1.setAgenteColocador("8");
        origen1.setSucursal(persona.sucursal());
        cf.setOrigen(origen1);

        cf.setPerfil(contexto.idCobis());
        cf.setPoseeInstrPagoPerm(true);

        Radicacion radicacion1 = new Radicacion();
        radicacion1.setAgenteColocador("8");
        radicacion1.setSucursal(persona.sucursal());
        radicacion1.setCanalVivienda("");
        radicacion1.setOficinaCuenta("");
        cf.setRadicacion(radicacion1);

        cf.setRequiereFirmaConjunta(false);
        cf.setSegmentoInversion(persona.esPersonaJuridica() ? "3" : "1");
        cf.setTipoCuotapartista("");
        cf.setTipoInversor("");
        cf.setTipoPerfilRiesgo("");

        List<CuotasBancarias> cuotasBancarias = new ArrayList<>();
        CuotasBancarias cuentaBancaria = new CuotasBancarias();
        cuentaBancaria.setAlias(cuentaPesos.alias());
        cuentaBancaria.setBanco(new Banco("00044"));
        cuentaBancaria.setCbu(cuentaPesos.cbu());
        cuentaBancaria.setCuitTitular(persona.cuit());
        cuentaBancaria.setDescripcion(cuentaPesos.descripcionCorta());
        cuentaBancaria.setFechaApertura(cuentaPesos.fechaAlta("yyyy-MM-dd"));
        cuentaBancaria.setIban("");
        cuentaBancaria.setIdCuentaBancaria(generarIDUnico());
        cuentaBancaria.setMoneda(new Moneda(cuentaPesos.idMoneda()));
        cuentaBancaria.setNumeroCuenta(cuentaPesos.numero());
        cuentaBancaria.setNumeroSucursal(cuentaPesos.sucursal());
        cuentaBancaria.setTipoCuentaBancaria(new TipoCuentaBancaria(cuentaPesos.idTipo()));
        cuotasBancarias.add(cuentaBancaria);

        if (cuentaDolar != null) {
            cuentaBancaria = new CuotasBancarias();
            cuentaBancaria.setAlias(cuentaDolar.alias());
            cuentaBancaria.setBanco(new Banco("00044"));
            cuentaBancaria.setCbu(cuentaDolar.cbu());
            cuentaBancaria.setCuitTitular(persona.cuit());
            cuentaBancaria.setDescripcion(cuentaDolar.descripcionCorta());
            cuentaBancaria.setFechaApertura(cuentaDolar.fechaAlta("yyyy-MM-dd"));
            cuentaBancaria.setIban("");
            cuentaBancaria.setIdCuentaBancaria(generarIDUnico());
            cuentaBancaria.setMoneda(new Moneda(cuentaDolar.idMoneda()));
            cuentaBancaria.setNumeroCuenta(cuentaDolar.numero());
            cuentaBancaria.setNumeroSucursal(cuentaDolar.sucursal());
            cuentaBancaria.setTipoCuentaBancaria(new TipoCuentaBancaria(cuentaDolar.idTipo()));
            cuotasBancarias.add(cuentaBancaria);
        }
        cf.setCuotasBancarias(cuotasBancarias);

        DatosUIF datosUIF = new DatosUIF();
        datosUIF.setMonedaImporteEstimado("");
        cf.setDatosUIF(datosUIF);

        cf.setRepresentantes("");
        cf.setTarjetasCredito("");
        cf.setIdPersona(contexto.idCobis());
        cf.setIdPersonaCondomino(cf.getIdPersona());
        cf.setApellido(fixCaracteresEspeciales(persona.apellidos()));
        cf.setCuit(persona.cuit());
        cf.setEsPEP(esPEP);
        cf.setEsmasculino(persona.idSexo().equalsIgnoreCase("M"));
        cf.setFechaNacimiento(new SimpleDateFormat("dd/MM/yyyy").format(contexto.parametros.date("fechaNacimiento", "dd/MM/yyyy", contexto.persona().fechaNacimiento())));
        cf.setNumDoc(persona.numeroDocumento());
        cf.setPaisNacimiento(RestCatalogo.nombrePais(contexto, idPaisNacimiento));
        cf.setPaisNacionalidad(RestCatalogo.nombrePais(contexto, persona.idNacionalidad()));
        cf.setPatrimonio("0.00");
        cf.setPromedioMensual("0.00");
        cf.setTipoContribuyente("");
        cf.setTipoDoc(persona.idTipoDocumentoString());
        cf.setTipoEstadoCivil(persona.idEstadoCivil());

        return cf;
    }

    public static String fixCaracteresEspeciales(String valor) {
        String REGEX = "[^a-zA-Z]";
        String valueReturn;
        valueReturn = valor.replaceAll("Ñ", "N").replaceAll("ñ", "n");
        valueReturn = valueReturn.replaceAll(REGEX, " ");
        return valueReturn;
    }

    public static String generarIDUnico() {
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            throw new RuntimeException();
        }
        LocalDateTime ahora = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");// todo: 17caracteres ver esto
        return ahora.format(formatter).substring(2);
    }
}
