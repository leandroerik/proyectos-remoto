package ar.com.hipotecario.canal.rewards.middleware.models.negocio;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class RWCliente extends ApiObjeto {
    private String actividadAFIP;
    private Boolean adoptoDisposicionesSO;
    private Integer anioRodado;
    private String apellidoConyuge;
    private String apellidos;
    private String apellidosMadre;
    private String apellidosPadre;
    private Boolean cajaAhorroMenores;
    private String calificacionCrediticia;
    private String canalModificacion;
    private Integer cantidadApoderados;
    private Integer cantidadChequeras;
    private Integer cantidadEmpleados;
    private Integer cantidadHijos;
    private String cantidadNupcias;
    private String cargoPEP;
    private String categoriaCliente;
    private String categoriaMonotributo;
    private String ciudadNacimiento;
    private String cuentaUsuarioFacebook;
    private String cuentaUsuarioTwitter;
    private Long cuit;
    private Integer eTagPreguntasCSC;
    private Integer edad;
    private Boolean esPEP;
    private Boolean esPerflInversor;
    private Boolean esPersonaFisica;
    private Boolean esPersonaJuridica;
    private Boolean esReferido;
    private Boolean esSO;
    private Boolean esUnidoDeHecho;
    private Integer etag;
    private Integer etagActividades;
    private Integer etagCliente;
    private Integer etagDomicilios;
    private Integer etagGastosPatrimoniales;
    private Integer etagMails;
    private Integer etagPerfilesPatrimoniales;
    private Integer etagPrestamosPatrimoniales;
    private Integer etagReferencias;
    private Integer etagRelaciones;
    private Integer etagTarjetasPatrimoniales;
    private Integer etagTelefonos;
    private LocalDateTime fechaActualizacionEstadoCivil;
    private LocalDateTime fechaConstitucionPersonaJuridica;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaDeclaracionPEP;
    private LocalDateTime fechaFinalResidencia1;
    private LocalDateTime fechaFinalResidencia2;
    private LocalDateTime fechaFirmaFormularioFatca;
    private LocalDateTime fechaIncripcionSO;
    private LocalDateTime fechaInicialResidencia1;
    private LocalDateTime fechaInicialResidencia2;
    private LocalDateTime fechaInicioResidenciaPais;
    private Integer fechaInicioResidenciaVivienda;
    private LocalDateTime fechaModificacion;
    private LocalDateTime fechaNacimiento;
    private LocalDateTime fechaNacimientoHijoMayor;
    private LocalDateTime fechaPerfilCliente;
    private LocalDateTime fechaPerfilPatrimonial;
    private LocalDateTime fechaPresentacionDDJJSO;
    private LocalDateTime fechaRecategorizacionMonotributo;
    private LocalDateTime fechaVencimientoFormularioFatca;
    private LocalDateTime fechaVencimientoMandatos;
    private LocalDateTime fechaVencimientoPEP;
    private Integer grupoEconomico;
    private Long id;
    private String idAlcanceDebitosCreditos;
    private Integer idCategoriaFatca;
    private Integer idCliente;
    private Integer idConvenio;
    private String idEstadoCivil;
    private String idGanancias;
    private String idImpuestoDebitosCreditos;
    private String idImpuestoSellos;
    private String idIva;
    private String idMarcaRodado;
    private String idModeloRodado;
    private Integer idNacionalidad;
    private String idNivelEstudios;
    private String idObraSocial;
    private String idPaisNacimiento;
    private String idPaisResidencia;
    private Integer idPaisResidencia1;
    private String idPaisResidencia2;
    private String idResidencia;
    private String idSector;
    private Integer idSegundaNacionalidad;
    private String idSexo;
    private String idSituacionImpositiva;
    private String idSituacionLaboral;
    private String idSituacionVivienda;
    private String idSubtipoEstadoCivil;
    private String idSucursalAsignada;
    private Integer idTerceraNacionalidad;
    private String idTipoBanca;
    private String idTipoCliente;
    private String idTipoDocumento;
    private String idTipoIDTributario;
    private String idTipoSO;
    private String idVersionDocumento;
    private BigDecimal importePerfilPatrimonial;
    private Boolean indicioFatca;
    private Boolean informadoPadronPEP;
    private BigDecimal montoAlquiler;
    private String nombreConyuge;
    private String nombres;
    private String nombresMadre;
    private String nombresPadre;
    private Long numeroContribuyente1;
    private Long numeroContribuyente2;
    private String numeroDocumento;
    private String numeroIdentificacionFatca;
    private String otroCargoPEP;
    private BigDecimal perfilPatrimonial;
    private Boolean presentoConstanciaSO;
    private Boolean presentoFormularioFatca;
    private String razonSocial;
    private Integer representanteLegal;
    private String tipoCompania;
    private String tipoConvenio;

    public String getActividadAFIP() {
        return actividadAFIP;
    }

    public void setActividadAFIP(String actividadAFIP) {
        this.actividadAFIP = actividadAFIP;
    }

    public Boolean getAdoptoDisposicionesSO() {
        return adoptoDisposicionesSO;
    }

    public void setAdoptoDisposicionesSO(Boolean adoptoDisposicionesSO) {
        this.adoptoDisposicionesSO = adoptoDisposicionesSO;
    }

    public Integer getAnioRodado() {
        return anioRodado;
    }

    public void setAnioRodado(Integer anioRodado) {
        this.anioRodado = anioRodado;
    }

    public String getApellidoConyuge() {
        return apellidoConyuge;
    }

    public void setApellidoConyuge(String apellidoConyuge) {
        this.apellidoConyuge = apellidoConyuge;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getApellidosMadre() {
        return apellidosMadre;
    }

    public void setApellidosMadre(String apellidosMadre) {
        this.apellidosMadre = apellidosMadre;
    }

    public String getApellidosPadre() {
        return apellidosPadre;
    }

    public void setApellidosPadre(String apellidosPadre) {
        this.apellidosPadre = apellidosPadre;
    }

    public Boolean getCajaAhorroMenores() {
        return cajaAhorroMenores;
    }

    public void setCajaAhorroMenores(Boolean cajaAhorroMenores) {
        this.cajaAhorroMenores = cajaAhorroMenores;
    }

    public String getCalificacionCrediticia() {
        return calificacionCrediticia;
    }

    public void setCalificacionCrediticia(String calificacionCrediticia) {
        this.calificacionCrediticia = calificacionCrediticia;
    }

    public String getCanalModificacion() {
        return canalModificacion;
    }

    public void setCanalModificacion(String canalModificacion) {
        this.canalModificacion = canalModificacion;
    }

    public Integer getCantidadApoderados() {
        return cantidadApoderados;
    }

    public void setCantidadApoderados(Integer cantidadApoderados) {
        this.cantidadApoderados = cantidadApoderados;
    }

    public Integer getCantidadChequeras() {
        return cantidadChequeras;
    }

    public void setCantidadChequeras(Integer cantidadChequeras) {
        this.cantidadChequeras = cantidadChequeras;
    }

    public Integer getCantidadEmpleados() {
        return cantidadEmpleados;
    }

    public void setCantidadEmpleados(Integer cantidadEmpleados) {
        this.cantidadEmpleados = cantidadEmpleados;
    }

    public Integer getCantidadHijos() {
        return cantidadHijos;
    }

    public void setCantidadHijos(Integer cantidadHijos) {
        this.cantidadHijos = cantidadHijos;
    }

    public String getCantidadNupcias() {
        return cantidadNupcias;
    }

    public void setCantidadNupcias(String cantidadNupcias) {
        this.cantidadNupcias = cantidadNupcias;
    }

    public String getCargoPEP() {
        return cargoPEP;
    }

    public void setCargoPEP(String cargoPEP) {
        this.cargoPEP = cargoPEP;
    }

    public String getCategoriaCliente() {
        return categoriaCliente;
    }

    public void setCategoriaCliente(String categoriaCliente) {
        this.categoriaCliente = categoriaCliente;
    }

    public String getCategoriaMonotributo() {
        return categoriaMonotributo;
    }

    public void setCategoriaMonotributo(String categoriaMonotributo) {
        this.categoriaMonotributo = categoriaMonotributo;
    }

    public String getCiudadNacimiento() {
        return ciudadNacimiento;
    }

    public void setCiudadNacimiento(String ciudadNacimiento) {
        this.ciudadNacimiento = ciudadNacimiento;
    }

    public String getCuentaUsuarioFacebook() {
        return cuentaUsuarioFacebook;
    }

    public void setCuentaUsuarioFacebook(String cuentaUsuarioFacebook) {
        this.cuentaUsuarioFacebook = cuentaUsuarioFacebook;
    }

    public String getCuentaUsuarioTwitter() {
        return cuentaUsuarioTwitter;
    }

    public void setCuentaUsuarioTwitter(String cuentaUsuarioTwitter) {
        this.cuentaUsuarioTwitter = cuentaUsuarioTwitter;
    }

    public Long getCuit() {
        return cuit;
    }

    public void setCuit(Long cuit) {
        this.cuit = cuit;
    }

    public Integer geteTagPreguntasCSC() {
        return eTagPreguntasCSC;
    }

    public void seteTagPreguntasCSC(Integer eTagPreguntasCSC) {
        this.eTagPreguntasCSC = eTagPreguntasCSC;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public Boolean getEsPEP() {
        return esPEP;
    }

    public void setEsPEP(Boolean esPEP) {
        this.esPEP = esPEP;
    }

    public Boolean getEsPerflInversor() {
        return esPerflInversor;
    }

    public void setEsPerflInversor(Boolean esPerflInversor) {
        this.esPerflInversor = esPerflInversor;
    }

    public Boolean getEsPersonaFisica() {
        return esPersonaFisica;
    }

    public void setEsPersonaFisica(Boolean esPersonaFisica) {
        this.esPersonaFisica = esPersonaFisica;
    }

    public Boolean getEsPersonaJuridica() {
        return esPersonaJuridica;
    }

    public void setEsPersonaJuridica(Boolean esPersonaJuridica) {
        this.esPersonaJuridica = esPersonaJuridica;
    }

    public Boolean getEsReferido() {
        return esReferido;
    }

    public void setEsReferido(Boolean esReferido) {
        this.esReferido = esReferido;
    }

    public Boolean getEsSO() {
        return esSO;
    }

    public void setEsSO(Boolean esSO) {
        this.esSO = esSO;
    }

    public Boolean getEsUnidoDeHecho() {
        return esUnidoDeHecho;
    }

    public void setEsUnidoDeHecho(Boolean esUnidoDeHecho) {
        this.esUnidoDeHecho = esUnidoDeHecho;
    }

    public Integer getEtag() {
        return etag;
    }

    public void setEtag(Integer etag) {
        this.etag = etag;
    }

    public Integer getEtagActividades() {
        return etagActividades;
    }

    public void setEtagActividades(Integer etagActividades) {
        this.etagActividades = etagActividades;
    }

    public Integer getEtagCliente() {
        return etagCliente;
    }

    public void setEtagCliente(Integer etagCliente) {
        this.etagCliente = etagCliente;
    }

    public Integer getEtagDomicilios() {
        return etagDomicilios;
    }

    public void setEtagDomicilios(Integer etagDomicilios) {
        this.etagDomicilios = etagDomicilios;
    }

    public Integer getEtagGastosPatrimoniales() {
        return etagGastosPatrimoniales;
    }

    public void setEtagGastosPatrimoniales(Integer etagGastosPatrimoniales) {
        this.etagGastosPatrimoniales = etagGastosPatrimoniales;
    }

    public Integer getEtagMails() {
        return etagMails;
    }

    public void setEtagMails(Integer etagMails) {
        this.etagMails = etagMails;
    }

    public Integer getEtagPerfilesPatrimoniales() {
        return etagPerfilesPatrimoniales;
    }

    public void setEtagPerfilesPatrimoniales(Integer etagPerfilesPatrimoniales) {
        this.etagPerfilesPatrimoniales = etagPerfilesPatrimoniales;
    }

    public Integer getEtagPrestamosPatrimoniales() {
        return etagPrestamosPatrimoniales;
    }

    public void setEtagPrestamosPatrimoniales(Integer etagPrestamosPatrimoniales) {
        this.etagPrestamosPatrimoniales = etagPrestamosPatrimoniales;
    }

    public Integer getEtagReferencias() {
        return etagReferencias;
    }

    public void setEtagReferencias(Integer etagReferencias) {
        this.etagReferencias = etagReferencias;
    }

    public Integer getEtagRelaciones() {
        return etagRelaciones;
    }

    public void setEtagRelaciones(Integer etagRelaciones) {
        this.etagRelaciones = etagRelaciones;
    }

    public Integer getEtagTarjetasPatrimoniales() {
        return etagTarjetasPatrimoniales;
    }

    public void setEtagTarjetasPatrimoniales(Integer etagTarjetasPatrimoniales) {
        this.etagTarjetasPatrimoniales = etagTarjetasPatrimoniales;
    }

    public Integer getEtagTelefonos() {
        return etagTelefonos;
    }

    public void setEtagTelefonos(Integer etagTelefonos) {
        this.etagTelefonos = etagTelefonos;
    }

    public LocalDateTime getFechaActualizacionEstadoCivil() {
        return fechaActualizacionEstadoCivil;
    }

    public void setFechaActualizacionEstadoCivil(LocalDateTime fechaActualizacionEstadoCivil) {
        this.fechaActualizacionEstadoCivil = fechaActualizacionEstadoCivil;
    }

    public LocalDateTime getFechaConstitucionPersonaJuridica() {
        return fechaConstitucionPersonaJuridica;
    }

    public void setFechaConstitucionPersonaJuridica(LocalDateTime fechaConstitucionPersonaJuridica) {
        this.fechaConstitucionPersonaJuridica = fechaConstitucionPersonaJuridica;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public LocalDateTime getFechaDeclaracionPEP() {
        return fechaDeclaracionPEP;
    }

    public void setFechaDeclaracionPEP(LocalDateTime fechaDeclaracionPEP) {
        this.fechaDeclaracionPEP = fechaDeclaracionPEP;
    }

    public LocalDateTime getFechaFinalResidencia1() {
        return fechaFinalResidencia1;
    }

    public void setFechaFinalResidencia1(LocalDateTime fechaFinalResidencia1) {
        this.fechaFinalResidencia1 = fechaFinalResidencia1;
    }

    public LocalDateTime getFechaFinalResidencia2() {
        return fechaFinalResidencia2;
    }

    public void setFechaFinalResidencia2(LocalDateTime fechaFinalResidencia2) {
        this.fechaFinalResidencia2 = fechaFinalResidencia2;
    }

    public LocalDateTime getFechaFirmaFormularioFatca() {
        return fechaFirmaFormularioFatca;
    }

    public void setFechaFirmaFormularioFatca(LocalDateTime fechaFirmaFormularioFatca) {
        this.fechaFirmaFormularioFatca = fechaFirmaFormularioFatca;
    }

    public LocalDateTime getFechaIncripcionSO() {
        return fechaIncripcionSO;
    }

    public void setFechaIncripcionSO(LocalDateTime fechaIncripcionSO) {
        this.fechaIncripcionSO = fechaIncripcionSO;
    }

    public LocalDateTime getFechaInicialResidencia1() {
        return fechaInicialResidencia1;
    }

    public void setFechaInicialResidencia1(LocalDateTime fechaInicialResidencia1) {
        this.fechaInicialResidencia1 = fechaInicialResidencia1;
    }

    public LocalDateTime getFechaInicialResidencia2() {
        return fechaInicialResidencia2;
    }

    public void setFechaInicialResidencia2(LocalDateTime fechaInicialResidencia2) {
        this.fechaInicialResidencia2 = fechaInicialResidencia2;
    }

    public LocalDateTime getFechaInicioResidenciaPais() {
        return fechaInicioResidenciaPais;
    }

    public void setFechaInicioResidenciaPais(LocalDateTime fechaInicioResidenciaPais) {
        this.fechaInicioResidenciaPais = fechaInicioResidenciaPais;
    }

    public Integer getFechaInicioResidenciaVivienda() {
        return fechaInicioResidenciaVivienda;
    }

    public void setFechaInicioResidenciaVivienda(Integer fechaInicioResidenciaVivienda) {
        this.fechaInicioResidenciaVivienda = fechaInicioResidenciaVivienda;
    }

    public LocalDateTime getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(LocalDateTime fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
    }

    public LocalDateTime getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(LocalDateTime fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public LocalDateTime getFechaNacimientoHijoMayor() {
        return fechaNacimientoHijoMayor;
    }

    public void setFechaNacimientoHijoMayor(LocalDateTime fechaNacimientoHijoMayor) {
        this.fechaNacimientoHijoMayor = fechaNacimientoHijoMayor;
    }

    public LocalDateTime getFechaPerfilCliente() {
        return fechaPerfilCliente;
    }

    public void setFechaPerfilCliente(LocalDateTime fechaPerfilCliente) {
        this.fechaPerfilCliente = fechaPerfilCliente;
    }

    public LocalDateTime getFechaPerfilPatrimonial() {
        return fechaPerfilPatrimonial;
    }

    public void setFechaPerfilPatrimonial(LocalDateTime fechaPerfilPatrimonial) {
        this.fechaPerfilPatrimonial = fechaPerfilPatrimonial;
    }

    public LocalDateTime getFechaPresentacionDDJJSO() {
        return fechaPresentacionDDJJSO;
    }

    public void setFechaPresentacionDDJJSO(LocalDateTime fechaPresentacionDDJJSO) {
        this.fechaPresentacionDDJJSO = fechaPresentacionDDJJSO;
    }

    public LocalDateTime getFechaRecategorizacionMonotributo() {
        return fechaRecategorizacionMonotributo;
    }

    public void setFechaRecategorizacionMonotributo(LocalDateTime fechaRecategorizacionMonotributo) {
        this.fechaRecategorizacionMonotributo = fechaRecategorizacionMonotributo;
    }

    public LocalDateTime getFechaVencimientoFormularioFatca() {
        return fechaVencimientoFormularioFatca;
    }

    public void setFechaVencimientoFormularioFatca(LocalDateTime fechaVencimientoFormularioFatca) {
        this.fechaVencimientoFormularioFatca = fechaVencimientoFormularioFatca;
    }

    public LocalDateTime getFechaVencimientoMandatos() {
        return fechaVencimientoMandatos;
    }

    public void setFechaVencimientoMandatos(LocalDateTime fechaVencimientoMandatos) {
        this.fechaVencimientoMandatos = fechaVencimientoMandatos;
    }

    public LocalDateTime getFechaVencimientoPEP() {
        return fechaVencimientoPEP;
    }

    public void setFechaVencimientoPEP(LocalDateTime fechaVencimientoPEP) {
        this.fechaVencimientoPEP = fechaVencimientoPEP;
    }

    public Integer getGrupoEconomico() {
        return grupoEconomico;
    }

    public void setGrupoEconomico(Integer grupoEconomico) {
        this.grupoEconomico = grupoEconomico;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getIdAlcanceDebitosCreditos() {
        return idAlcanceDebitosCreditos;
    }

    public void setIdAlcanceDebitosCreditos(String idAlcanceDebitosCreditos) {
        this.idAlcanceDebitosCreditos = idAlcanceDebitosCreditos;
    }

    public Integer getIdCategoriaFatca() {
        return idCategoriaFatca;
    }

    public void setIdCategoriaFatca(Integer idCategoriaFatca) {
        this.idCategoriaFatca = idCategoriaFatca;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Integer getIdConvenio() {
        return idConvenio;
    }

    public void setIdConvenio(Integer idConvenio) {
        this.idConvenio = idConvenio;
    }

    public String getIdEstadoCivil() {
        return idEstadoCivil;
    }

    public void setIdEstadoCivil(String idEstadoCivil) {
        this.idEstadoCivil = idEstadoCivil;
    }

    public String getIdGanancias() {
        return idGanancias;
    }

    public void setIdGanancias(String idGanancias) {
        this.idGanancias = idGanancias;
    }

    public String getIdImpuestoDebitosCreditos() {
        return idImpuestoDebitosCreditos;
    }

    public void setIdImpuestoDebitosCreditos(String idImpuestoDebitosCreditos) {
        this.idImpuestoDebitosCreditos = idImpuestoDebitosCreditos;
    }

    public String getIdImpuestoSellos() {
        return idImpuestoSellos;
    }

    public void setIdImpuestoSellos(String idImpuestoSellos) {
        this.idImpuestoSellos = idImpuestoSellos;
    }

    public String getIdIva() {
        return idIva;
    }

    public void setIdIva(String idIva) {
        this.idIva = idIva;
    }

    public String getIdMarcaRodado() {
        return idMarcaRodado;
    }

    public void setIdMarcaRodado(String idMarcaRodado) {
        this.idMarcaRodado = idMarcaRodado;
    }

    public String getIdModeloRodado() {
        return idModeloRodado;
    }

    public void setIdModeloRodado(String idModeloRodado) {
        this.idModeloRodado = idModeloRodado;
    }

    public Integer getIdNacionalidad() {
        return idNacionalidad;
    }

    public void setIdNacionalidad(Integer idNacionalidad) {
        this.idNacionalidad = idNacionalidad;
    }

    public String getIdNivelEstudios() {
        return idNivelEstudios;
    }

    public void setIdNivelEstudios(String idNivelEstudios) {
        this.idNivelEstudios = idNivelEstudios;
    }

    public String getIdObraSocial() {
        return idObraSocial;
    }

    public void setIdObraSocial(String idObraSocial) {
        this.idObraSocial = idObraSocial;
    }

    public String getIdPaisNacimiento() {
        return idPaisNacimiento;
    }

    public void setIdPaisNacimiento(String idPaisNacimiento) {
        this.idPaisNacimiento = idPaisNacimiento;
    }

    public String getIdPaisResidencia() {
        return idPaisResidencia;
    }

    public void setIdPaisResidencia(String idPaisResidencia) {
        this.idPaisResidencia = idPaisResidencia;
    }

    public Integer getIdPaisResidencia1() {
        return idPaisResidencia1;
    }

    public void setIdPaisResidencia1(Integer idPaisResidencia1) {
        this.idPaisResidencia1 = idPaisResidencia1;
    }

    public String getIdPaisResidencia2() {
        return idPaisResidencia2;
    }

    public void setIdPaisResidencia2(String idPaisResidencia2) {
        this.idPaisResidencia2 = idPaisResidencia2;
    }

    public String getIdResidencia() {
        return idResidencia;
    }

    public void setIdResidencia(String idResidencia) {
        this.idResidencia = idResidencia;
    }

    public String getIdSector() {
        return idSector;
    }

    public void setIdSector(String idSector) {
        this.idSector = idSector;
    }

    public Integer getIdSegundaNacionalidad() {
        return idSegundaNacionalidad;
    }

    public void setIdSegundaNacionalidad(Integer idSegundaNacionalidad) {
        this.idSegundaNacionalidad = idSegundaNacionalidad;
    }

    public String getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(String idSexo) {
        this.idSexo = idSexo;
    }

    public String getIdSituacionImpositiva() {
        return idSituacionImpositiva;
    }

    public void setIdSituacionImpositiva(String idSituacionImpositiva) {
        this.idSituacionImpositiva = idSituacionImpositiva;
    }

    public String getIdSituacionLaboral() {
        return idSituacionLaboral;
    }

    public void setIdSituacionLaboral(String idSituacionLaboral) {
        this.idSituacionLaboral = idSituacionLaboral;
    }

    public String getIdSituacionVivienda() {
        return idSituacionVivienda;
    }

    public void setIdSituacionVivienda(String idSituacionVivienda) {
        this.idSituacionVivienda = idSituacionVivienda;
    }

    public String getIdSubtipoEstadoCivil() {
        return idSubtipoEstadoCivil;
    }

    public void setIdSubtipoEstadoCivil(String idSubtipoEstadoCivil) {
        this.idSubtipoEstadoCivil = idSubtipoEstadoCivil;
    }

    public String getIdSucursalAsignada() {
        return idSucursalAsignada;
    }

    public void setIdSucursalAsignada(String idSucursalAsignada) {
        this.idSucursalAsignada = idSucursalAsignada;
    }

    public Integer getIdTerceraNacionalidad() {
        return idTerceraNacionalidad;
    }

    public void setIdTerceraNacionalidad(Integer idTerceraNacionalidad) {
        this.idTerceraNacionalidad = idTerceraNacionalidad;
    }

    public String getIdTipoBanca() {
        return idTipoBanca;
    }

    public void setIdTipoBanca(String idTipoBanca) {
        this.idTipoBanca = idTipoBanca;
    }

    public String getIdTipoCliente() {
        return idTipoCliente;
    }

    public void setIdTipoCliente(String idTipoCliente) {
        this.idTipoCliente = idTipoCliente;
    }

    public String getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(String idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getIdTipoIDTributario() {
        return idTipoIDTributario;
    }

    public void setIdTipoIDTributario(String idTipoIDTributario) {
        this.idTipoIDTributario = idTipoIDTributario;
    }

    public String getIdTipoSO() {
        return idTipoSO;
    }

    public void setIdTipoSO(String idTipoSO) {
        this.idTipoSO = idTipoSO;
    }

    public String getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(String idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }

    public BigDecimal getImportePerfilPatrimonial() {
        return importePerfilPatrimonial;
    }

    public void setImportePerfilPatrimonial(BigDecimal importePerfilPatrimonial) {
        this.importePerfilPatrimonial = importePerfilPatrimonial;
    }

    public Boolean getIndicioFatca() {
        return indicioFatca;
    }

    public void setIndicioFatca(Boolean indicioFatca) {
        this.indicioFatca = indicioFatca;
    }

    public Boolean getInformadoPadronPEP() {
        return informadoPadronPEP;
    }

    public void setInformadoPadronPEP(Boolean informadoPadronPEP) {
        this.informadoPadronPEP = informadoPadronPEP;
    }

    public BigDecimal getMontoAlquiler() {
        return montoAlquiler;
    }

    public void setMontoAlquiler(BigDecimal montoAlquiler) {
        this.montoAlquiler = montoAlquiler;
    }

    public String getNombreConyuge() {
        return nombreConyuge;
    }

    public void setNombreConyuge(String nombreConyuge) {
        this.nombreConyuge = nombreConyuge;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
    }

    public String getNombresMadre() {
        return nombresMadre;
    }

    public void setNombresMadre(String nombresMadre) {
        this.nombresMadre = nombresMadre;
    }

    public String getNombresPadre() {
        return nombresPadre;
    }

    public void setNombresPadre(String nombresPadre) {
        this.nombresPadre = nombresPadre;
    }

    public Long getNumeroContribuyente1() {
        return numeroContribuyente1;
    }

    public void setNumeroContribuyente1(Long numeroContribuyente1) {
        this.numeroContribuyente1 = numeroContribuyente1;
    }

    public Long getNumeroContribuyente2() {
        return numeroContribuyente2;
    }

    public void setNumeroContribuyente2(Long numeroContribuyente2) {
        this.numeroContribuyente2 = numeroContribuyente2;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getNumeroIdentificacionFatca() {
        return numeroIdentificacionFatca;
    }

    public void setNumeroIdentificacionFatca(String numeroIdentificacionFatca) {
        this.numeroIdentificacionFatca = numeroIdentificacionFatca;
    }

    public String getOtroCargoPEP() {
        return otroCargoPEP;
    }

    public void setOtroCargoPEP(String otroCargoPEP) {
        this.otroCargoPEP = otroCargoPEP;
    }

    public BigDecimal getPerfilPatrimonial() {
        return perfilPatrimonial;
    }

    public void setPerfilPatrimonial(BigDecimal perfilPatrimonial) {
        this.perfilPatrimonial = perfilPatrimonial;
    }

    public Boolean getPresentoConstanciaSO() {
        return presentoConstanciaSO;
    }

    public void setPresentoConstanciaSO(Boolean presentoConstanciaSO) {
        this.presentoConstanciaSO = presentoConstanciaSO;
    }

    public Boolean getPresentoFormularioFatca() {
        return presentoFormularioFatca;
    }

    public void setPresentoFormularioFatca(Boolean presentoFormularioFatca) {
        this.presentoFormularioFatca = presentoFormularioFatca;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public Integer getRepresentanteLegal() {
        return representanteLegal;
    }

    public void setRepresentanteLegal(Integer representanteLegal) {
        this.representanteLegal = representanteLegal;
    }

    public String getTipoCompania() {
        return tipoCompania;
    }

    public void setTipoCompania(String tipoCompania) {
        this.tipoCompania = tipoCompania;
    }

    public String getTipoConvenio() {
        return tipoConvenio;
    }

    public void setTipoConvenio(String tipoConvenio) {
        this.tipoConvenio = tipoConvenio;
    }

}
