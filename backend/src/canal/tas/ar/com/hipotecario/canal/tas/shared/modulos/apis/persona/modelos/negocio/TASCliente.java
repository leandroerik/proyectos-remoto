package ar.com.hipotecario.canal.tas.shared.modulos.apis.persona.modelos.negocio;
import java.math.BigDecimal;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class TASCliente extends ApiObjeto {

    private String 		idTipoDocumento;
    private String 		numeroDocumento;
    private String 		idVersionDocumento;
    private String 		idSexo;
    private String 		idTipoIDTributario;
    private Long 		id;
    private Long 		cuit;
    private Integer 	idCliente;
    private String 		apellidos;
    private String 		nombres;
    private Boolean 	esPersonaFisica;
    private Boolean 	esPersonaJuridica;
    private Boolean 	esReferido;
    private String 		fechaActualizacionEstadoCivil;
    private String 		idSubtipoEstadoCivil;
    private String 		idObraSocial;
    private String 		idSituacionVivienda;
    private String 		fechaNacimiento;
    private String 		idEstadoCivil;
    private Boolean 	esUnidoDeHecho;
    private String 		cantidadNupcias;
    private Integer 	cantidadHijos;
    private String 		idNivelEstudios;
    private Integer 	fechaInicioResidenciaVivienda;
    private Float 		montoAlquiler;
    private Integer 	idPaisNacimiento;
    private String 		idPaisResidencia;
    private String 		fechaInicioResidenciaPais;
    private String 		ciudadNacimiento;
    private String 		apellidosPadre;
    private String 		nombresPadre;
    private String 		apellidosMadre;
    private String 		nombresMadre;
    private String 		idGanancias;
    private String 		idImpuestoDebitosCreditos;
    private String 		idImpuestoSellos;
    private String 		idAlcanceDebitosCreditos;
    private Boolean 	esPEP;
    private String 		cargoPEP;
    private String 		otroCargoPEP;
    private String 		fechaDeclaracionPEP;
    private String 		fechaVencimientoPEP;
    private Boolean 	informadoPadronPEP;
    private Boolean 	esSO;
    private String 		idTipoSO;
    private Boolean 	adoptoDisposicionesSO;
    private Boolean 	presentoConstanciaSO;
    private String 		fechaPresentacionDDJJSO;
    private String 		fechaIncripcionSO;
    private BigDecimal 	perfilPatrimonial;
    private String 		fechaPerfilCliente;
    private Integer 	idNacionalidad;
    private Integer 	idSegundaNacionalidad;
    private Integer 	idTerceraNacionalidad;
    private Integer 	idPaisResidencia1;
    private String 		fechaInicialResidencia1;
    private String 		fechaFinalResidencia1;
    private Long 		numeroContribuyente1;
    private String 		idPaisResidencia2;
    private String 		fechaInicialResidencia2;
    private String 		fechaFinalResidencia2;
    private Long 		numeroContribuyente2;
    private Integer 	idCategoriaFatca;
    private Boolean 	presentoFormularioFatca;
    private String 		fechaVencimientoFormularioFatca;
    private String 		razonSocial;
    private String 		actividadAFIP;
    private String 		fechaConstitucionPersonaJuridica;
    private Integer 	cantidadEmpleados;
    private String 		calificacionCrediticia;
    private Integer 	representanteLegal;
    private Integer 	cantidadApoderados;
    private String 		fechaVencimientoMandatos;
    private Integer 	grupoEconomico;
    private Integer 	cantidadChequeras;
    private String 		idSituacionImpositiva;
    private String 		categoriaCliente;
    private String 		tipoSocietario;
    private Integer 	eTag;
    private String 		canalModificacion;
    private String 		fechaCreacion;
    private String 		usuarioModificacion;
    private String 		fechaModificacion;
    private String 		cuentaUsuarioFacebook;
    private String 		cuentaUsuarioTwitter;
    private String 		fechaNacimientoHijoMayor;
    private String 		idMarcaRodado;
    private String 		idModeloRodado;
    private Integer 	anioRodado;
    private Integer 	eTagGastosPatrimoniales;
    private Integer 	eTagPerfilesPatrimoniales;
    private Integer 	eTagPrestamosPatrimoniales;
    private Integer 	eTagTarjetasPatrimoniales;
    private Integer 	eTagActividades;
    private Integer 	eTagReferencias;
    private Integer 	eTagRelaciones;
    private Integer 	eTagDomicilios;
    private Integer 	eTagTelefonos;
    private Integer 	eTagMails;
    private Integer 	eTagCliente;
    private String 		valorLealtadCliente;
    private Boolean 	esPerflInversor;
    private String 		idSucursalAsignada;
    private Integer 	idConvenio;
    private String 		tipoConvenio;
    private String 		idTipoCliente;
    private String 		idTipoBanca;
    private BigDecimal 	valorRentabilidadCa;
    private BigDecimal 	valorRentabilidadCc;
    private BigDecimal  valorRentabilidadEmerix;
    private BigDecimal 	valorRentabilidadGenesys;
    private BigDecimal 	valorRentabilidadIvr;
    private BigDecimal 	valorRentabilidadMesaCambio;
    private BigDecimal 	valorRentabilidadPf;
    private BigDecimal 	valorRentabilidadPh;
    private BigDecimal 	valorRentabilidadPp;
    private BigDecimal 	valorRentabilidadTc;
    private String 		numeroIdentificacionFatca;
    private String fechaFirmaFormularioFatca;
    private String 		tipoFormularioFatca;
    private String 		idSituacionLaboral;
    private Boolean 	indicioFatca;
    private Integer 	eTagPreguntasCSC;
    private String 		categoriaMonotributo;
    private String 		fechaRecategorizacionMonotributo;
    private String 		fechaPerfilPatrimonial;
    private BigDecimal 	importePerfilPatrimonial;
    private String 		tipoCompania;
    private Boolean 	cajaAhorroMenores;
    private Integer		edad;
    private String 		apellidoConyuge;
    private String 		nombreConyuge;

    public String getIdTipoDocumento() {
        return idTipoDocumento;
    }

    public void setIdTipoDocumento(String idTipoDocumento) {
        this.idTipoDocumento = idTipoDocumento;
    }

    public String getNumeroDocumento() {
        return numeroDocumento;
    }

    public void setNumeroDocumento(String numeroDocumento) {
        this.numeroDocumento = numeroDocumento;
    }

    public String getIdVersionDocumento() {
        return idVersionDocumento;
    }

    public void setIdVersionDocumento(String idVersionDocumento) {
        this.idVersionDocumento = idVersionDocumento;
    }

    public String getIdSexo() {
        return idSexo;
    }

    public void setIdSexo(String idSexo) {
        this.idSexo = idSexo;
    }

    public String getIdTipoIDTributario() {
        return idTipoIDTributario;
    }

    public void setIdTipoIDTributario(String idTipoIDTributario) {
        this.idTipoIDTributario = idTipoIDTributario;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCuit() {
        return cuit;
    }

    public void setCuit(Long cuit) {
        this.cuit = cuit;
    }

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getNombres() {
        return nombres;
    }

    public void setNombres(String nombres) {
        this.nombres = nombres;
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

    public String getFechaActualizacionEstadoCivil() {
        return fechaActualizacionEstadoCivil;
    }

    public void setFechaActualizacionEstadoCivil(String fechaActualizacionEstadoCivil) {
        this.fechaActualizacionEstadoCivil = fechaActualizacionEstadoCivil;
    }

    public String getIdSubtipoEstadoCivil() {
        return idSubtipoEstadoCivil;
    }

    public void setIdSubtipoEstadoCivil(String idSubtipoEstadoCivil) {
        this.idSubtipoEstadoCivil = idSubtipoEstadoCivil;
    }

    public String getIdObraSocial() {
        return idObraSocial;
    }

    public void setIdObraSocial(String idObraSocial) {
        this.idObraSocial = idObraSocial;
    }

    public String getIdSituacionVivienda() {
        return idSituacionVivienda;
    }

    public void setIdSituacionVivienda(String idSituacionVivienda) {
        this.idSituacionVivienda = idSituacionVivienda;
    }

    public String getFechaNacimiento() {
        return fechaNacimiento;
    }

    public void setFechaNacimiento(String fechaNacimiento) {
        this.fechaNacimiento = fechaNacimiento;
    }

    public String getIdEstadoCivil() {
        return idEstadoCivil;
    }

    public void setIdEstadoCivil(String idEstadoCivil) {
        this.idEstadoCivil = idEstadoCivil;
    }

    public Boolean getEsUnidoDeHecho() {
        return esUnidoDeHecho;
    }

    public void setEsUnidoDeHecho(Boolean esUnidoDeHecho) {
        this.esUnidoDeHecho = esUnidoDeHecho;
    }

    public String getCantidadNupcias() {
        return cantidadNupcias;
    }

    public void setCantidadNupcias(String cantidadNupcias) {
        this.cantidadNupcias = cantidadNupcias;
    }

    public Integer getCantidadHijos() {
        return cantidadHijos;
    }

    public void setCantidadHijos(Integer cantidadHijos) {
        this.cantidadHijos = cantidadHijos;
    }

    public String getIdNivelEstudios() {
        return idNivelEstudios;
    }

    public void setIdNivelEstudios(String idNivelEstudios) {
        this.idNivelEstudios = idNivelEstudios;
    }

    public Integer getFechaInicioResidenciaVivienda() {
        return fechaInicioResidenciaVivienda;
    }

    public void setFechaInicioResidenciaVivienda(Integer fechaInicioResidenciaVivienda) {
        this.fechaInicioResidenciaVivienda = fechaInicioResidenciaVivienda;
    }

    public Float getMontoAlquiler() {
        return montoAlquiler;
    }

    public void setMontoAlquiler(Float montoAlquiler) {
        this.montoAlquiler = montoAlquiler;
    }

    public Integer getIdPaisNacimiento() {
        return idPaisNacimiento;
    }

    public void setIdPaisNacimiento(Integer idPaisNacimiento) {
        this.idPaisNacimiento = idPaisNacimiento;
    }

    public String getIdPaisResidencia() {
        return idPaisResidencia;
    }

    public void setIdPaisResidencia(String idPaisResidencia) {
        this.idPaisResidencia = idPaisResidencia;
    }

    public String getFechaInicioResidenciaPais() {
        return fechaInicioResidenciaPais;
    }

    public void setFechaInicioResidenciaPais(String fechaInicioResidenciaPais) {
        this.fechaInicioResidenciaPais = fechaInicioResidenciaPais;
    }

    public String getCiudadNacimiento() {
        return ciudadNacimiento;
    }

    public void setCiudadNacimiento(String ciudadNacimiento) {
        this.ciudadNacimiento = ciudadNacimiento;
    }

    public String getApellidosPadre() {
        return apellidosPadre;
    }

    public void setApellidosPadre(String apellidosPadre) {
        this.apellidosPadre = apellidosPadre;
    }

    public String getNombresPadre() {
        return nombresPadre;
    }

    public void setNombresPadre(String nombresPadre) {
        this.nombresPadre = nombresPadre;
    }

    public String getApellidosMadre() {
        return apellidosMadre;
    }

    public void setApellidosMadre(String apellidosMadre) {
        this.apellidosMadre = apellidosMadre;
    }

    public String getNombresMadre() {
        return nombresMadre;
    }

    public void setNombresMadre(String nombresMadre) {
        this.nombresMadre = nombresMadre;
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

    public String getIdAlcanceDebitosCreditos() {
        return idAlcanceDebitosCreditos;
    }

    public void setIdAlcanceDebitosCreditos(String idAlcanceDebitosCreditos) {
        this.idAlcanceDebitosCreditos = idAlcanceDebitosCreditos;
    }

    public Boolean getEsPEP() {
        return esPEP;
    }

    public void setEsPEP(Boolean esPEP) {
        this.esPEP = esPEP;
    }

    public String getCargoPEP() {
        return cargoPEP;
    }

    public void setCargoPEP(String cargoPEP) {
        this.cargoPEP = cargoPEP;
    }

    public String getOtroCargoPEP() {
        return otroCargoPEP;
    }

    public void setOtroCargoPEP(String otroCargoPEP) {
        this.otroCargoPEP = otroCargoPEP;
    }

    public String getFechaDeclaracionPEP() {
        return fechaDeclaracionPEP;
    }

    public void setFechaDeclaracionPEP(String fechaDeclaracionPEP) {
        this.fechaDeclaracionPEP = fechaDeclaracionPEP;
    }

    public String getFechaVencimientoPEP() {
        return fechaVencimientoPEP;
    }

    public void setFechaVencimientoPEP(String fechaVencimientoPEP) {
        this.fechaVencimientoPEP = fechaVencimientoPEP;
    }

    public Boolean getInformadoPadronPEP() {
        return informadoPadronPEP;
    }

    public void setInformadoPadronPEP(Boolean informadoPadronPEP) {
        this.informadoPadronPEP = informadoPadronPEP;
    }

    public Boolean getEsSO() {
        return esSO;
    }

    public void setEsSO(Boolean esSO) {
        this.esSO = esSO;
    }

    public String getIdTipoSO() {
        return idTipoSO;
    }

    public void setIdTipoSO(String idTipoSO) {
        this.idTipoSO = idTipoSO;
    }

    public Boolean getAdoptoDisposicionesSO() {
        return adoptoDisposicionesSO;
    }

    public void setAdoptoDisposicionesSO(Boolean adoptoDisposicionesSO) {
        this.adoptoDisposicionesSO = adoptoDisposicionesSO;
    }

    public Boolean getPresentoConstanciaSO() {
        return presentoConstanciaSO;
    }

    public void setPresentoConstanciaSO(Boolean presentoConstanciaSO) {
        this.presentoConstanciaSO = presentoConstanciaSO;
    }

    public String getFechaPresentacionDDJJSO() {
        return fechaPresentacionDDJJSO;
    }

    public void setFechaPresentacionDDJJSO(String fechaPresentacionDDJJSO) {
        this.fechaPresentacionDDJJSO = fechaPresentacionDDJJSO;
    }

    public String getFechaIncripcionSO() {
        return fechaIncripcionSO;
    }

    public void setFechaIncripcionSO(String fechaIncripcionSO) {
        this.fechaIncripcionSO = fechaIncripcionSO;
    }

    public BigDecimal getPerfilPatrimonial() {
        return perfilPatrimonial;
    }

    public void setPerfilPatrimonial(BigDecimal perfilPatrimonial) {
        this.perfilPatrimonial = perfilPatrimonial;
    }

    public String getFechaPerfilCliente() {
        return fechaPerfilCliente;
    }

    public void setFechaPerfilCliente(String fechaPerfilCliente) {
        this.fechaPerfilCliente = fechaPerfilCliente;
    }

    public Integer getIdNacionalidad() {
        return idNacionalidad;
    }

    public void setIdNacionalidad(Integer idNacionalidad) {
        this.idNacionalidad = idNacionalidad;
    }

    public Integer getIdSegundaNacionalidad() {
        return idSegundaNacionalidad;
    }

    public void setIdSegundaNacionalidad(Integer idSegundaNacionalidad) {
        this.idSegundaNacionalidad = idSegundaNacionalidad;
    }

    public Integer getIdTerceraNacionalidad() {
        return idTerceraNacionalidad;
    }

    public void setIdTerceraNacionalidad(Integer idTerceraNacionalidad) {
        this.idTerceraNacionalidad = idTerceraNacionalidad;
    }

    public Integer getIdPaisResidencia1() {
        return idPaisResidencia1;
    }

    public void setIdPaisResidencia1(Integer idPaisResidencia1) {
        this.idPaisResidencia1 = idPaisResidencia1;
    }

    public String getFechaInicialResidencia1() {
        return fechaInicialResidencia1;
    }

    public void setFechaInicialResidencia1(String fechaInicialResidencia1) {
        this.fechaInicialResidencia1 = fechaInicialResidencia1;
    }

    public String getFechaFinalResidencia1() {
        return fechaFinalResidencia1;
    }

    public void setFechaFinalResidencia1(String fechaFinalResidencia1) {
        this.fechaFinalResidencia1 = fechaFinalResidencia1;
    }

    public Long getNumeroContribuyente1() {
        return numeroContribuyente1;
    }

    public void setNumeroContribuyente1(Long numeroContribuyente1) {
        this.numeroContribuyente1 = numeroContribuyente1;
    }

    public String getIdPaisResidencia2() {
        return idPaisResidencia2;
    }

    public void setIdPaisResidencia2(String idPaisResidencia2) {
        this.idPaisResidencia2 = idPaisResidencia2;
    }

    public String getFechaInicialResidencia2() {
        return fechaInicialResidencia2;
    }

    public void setFechaInicialResidencia2(String fechaInicialResidencia2) {
        this.fechaInicialResidencia2 = fechaInicialResidencia2;
    }

    public String getFechaFinalResidencia2() {
        return fechaFinalResidencia2;
    }

    public void setFechaFinalResidencia2(String fechaFinalResidencia2) {
        this.fechaFinalResidencia2 = fechaFinalResidencia2;
    }

    public Long getNumeroContribuyente2() {
        return numeroContribuyente2;
    }

    public void setNumeroContribuyente2(Long numeroContribuyente2) {
        this.numeroContribuyente2 = numeroContribuyente2;
    }

    public Integer getIdCategoriaFatca() {
        return idCategoriaFatca;
    }

    public void setIdCategoriaFatca(Integer idCategoriaFatca) {
        this.idCategoriaFatca = idCategoriaFatca;
    }

    public Boolean getPresentoFormularioFatca() {
        return presentoFormularioFatca;
    }

    public void setPresentoFormularioFatca(Boolean presentoFormularioFatca) {
        this.presentoFormularioFatca = presentoFormularioFatca;
    }

    public String getFechaVencimientoFormularioFatca() {
        return fechaVencimientoFormularioFatca;
    }

    public void setFechaVencimientoFormularioFatca(String fechaVencimientoFormularioFatca) {
        this.fechaVencimientoFormularioFatca = fechaVencimientoFormularioFatca;
    }

    public String getRazonSocial() {
        return razonSocial;
    }

    public void setRazonSocial(String razonSocial) {
        this.razonSocial = razonSocial;
    }

    public String getActividadAFIP() {
        return actividadAFIP;
    }

    public void setActividadAFIP(String actividadAFIP) {
        this.actividadAFIP = actividadAFIP;
    }

    public String getFechaConstitucionPersonaJuridica() {
        return fechaConstitucionPersonaJuridica;
    }

    public void setFechaConstitucionPersonaJuridica(String fechaConstitucionPersonaJuridica) {
        this.fechaConstitucionPersonaJuridica = fechaConstitucionPersonaJuridica;
    }

    public Integer getCantidadEmpleados() {
        return cantidadEmpleados;
    }

    public void setCantidadEmpleados(Integer cantidadEmpleados) {
        this.cantidadEmpleados = cantidadEmpleados;
    }

    public String getCalificacionCrediticia() {
        return calificacionCrediticia;
    }

    public void setCalificacionCrediticia(String calificacionCrediticia) {
        this.calificacionCrediticia = calificacionCrediticia;
    }

    public Integer getRepresentanteLegal() {
        return representanteLegal;
    }

    public void setRepresentanteLegal(Integer representanteLegal) {
        this.representanteLegal = representanteLegal;
    }

    public Integer getCantidadApoderados() {
        return cantidadApoderados;
    }

    public void setCantidadApoderados(Integer cantidadApoderados) {
        this.cantidadApoderados = cantidadApoderados;
    }

    public String getFechaVencimientoMandatos() {
        return fechaVencimientoMandatos;
    }

    public void setFechaVencimientoMandatos(String fechaVencimientoMandatos) {
        this.fechaVencimientoMandatos = fechaVencimientoMandatos;
    }

    public Integer getGrupoEconomico() {
        return grupoEconomico;
    }

    public void setGrupoEconomico(Integer grupoEconomico) {
        this.grupoEconomico = grupoEconomico;
    }

    public Integer getCantidadChequeras() {
        return cantidadChequeras;
    }

    public void setCantidadChequeras(Integer cantidadChequeras) {
        this.cantidadChequeras = cantidadChequeras;
    }

    public String getIdSituacionImpositiva() {
        return idSituacionImpositiva;
    }

    public void setIdSituacionImpositiva(String idSituacionImpositiva) {
        this.idSituacionImpositiva = idSituacionImpositiva;
    }

    public String getCategoriaCliente() {
        return categoriaCliente;
    }

    public void setCategoriaCliente(String categoriaCliente) {
        this.categoriaCliente = categoriaCliente;
    }

    public String getTipoSocietario() {
        return tipoSocietario;
    }

    public void setTipoSocietario(String tipoSocietario) {
        this.tipoSocietario = tipoSocietario;
    }

    public Integer geteTag() {
        return eTag;
    }

    public void seteTag(Integer eTag) {
        this.eTag = eTag;
    }

    public String getCanalModificacion() {
        return canalModificacion;
    }

    public void setCanalModificacion(String canalModificacion) {
        this.canalModificacion = canalModificacion;
    }

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public String getUsuarioModificacion() {
        return usuarioModificacion;
    }

    public void setUsuarioModificacion(String usuarioModificacion) {
        this.usuarioModificacion = usuarioModificacion;
    }

    public String getFechaModificacion() {
        return fechaModificacion;
    }

    public void setFechaModificacion(String fechaModificacion) {
        this.fechaModificacion = fechaModificacion;
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

    public String getFechaNacimientoHijoMayor() {
        return fechaNacimientoHijoMayor;
    }

    public void setFechaNacimientoHijoMayor(String fechaNacimientoHijoMayor) {
        this.fechaNacimientoHijoMayor = fechaNacimientoHijoMayor;
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

    public Integer getAnioRodado() {
        return anioRodado;
    }

    public void setAnioRodado(Integer anioRodado) {
        this.anioRodado = anioRodado;
    }

    public Integer geteTagGastosPatrimoniales() {
        return eTagGastosPatrimoniales;
    }

    public void seteTagGastosPatrimoniales(Integer eTagGastosPatrimoniales) {
        this.eTagGastosPatrimoniales = eTagGastosPatrimoniales;
    }

    public Integer geteTagPerfilesPatrimoniales() {
        return eTagPerfilesPatrimoniales;
    }

    public void seteTagPerfilesPatrimoniales(Integer eTagPerfilesPatrimoniales) {
        this.eTagPerfilesPatrimoniales = eTagPerfilesPatrimoniales;
    }

    public Integer geteTagPrestamosPatrimoniales() {
        return eTagPrestamosPatrimoniales;
    }

    public void seteTagPrestamosPatrimoniales(Integer eTagPrestamosPatrimoniales) {
        this.eTagPrestamosPatrimoniales = eTagPrestamosPatrimoniales;
    }

    public Integer geteTagTarjetasPatrimoniales() {
        return eTagTarjetasPatrimoniales;
    }

    public void seteTagTarjetasPatrimoniales(Integer eTagTarjetasPatrimoniales) {
        this.eTagTarjetasPatrimoniales = eTagTarjetasPatrimoniales;
    }

    public Integer geteTagActividades() {
        return eTagActividades;
    }

    public void seteTagActividades(Integer eTagActividades) {
        this.eTagActividades = eTagActividades;
    }

    public Integer geteTagReferencias() {
        return eTagReferencias;
    }

    public void seteTagReferencias(Integer eTagReferencias) {
        this.eTagReferencias = eTagReferencias;
    }

    public Integer geteTagRelaciones() {
        return eTagRelaciones;
    }

    public void seteTagRelaciones(Integer eTagRelaciones) {
        this.eTagRelaciones = eTagRelaciones;
    }

    public Integer geteTagDomicilios() {
        return eTagDomicilios;
    }

    public void seteTagDomicilios(Integer eTagDomicilios) {
        this.eTagDomicilios = eTagDomicilios;
    }

    public Integer geteTagTelefonos() {
        return eTagTelefonos;
    }

    public void seteTagTelefonos(Integer eTagTelefonos) {
        this.eTagTelefonos = eTagTelefonos;
    }

    public Integer geteTagMails() {
        return eTagMails;
    }

    public void seteTagMails(Integer eTagMails) {
        this.eTagMails = eTagMails;
    }

    public Integer geteTagCliente() {
        return eTagCliente;
    }

    public void seteTagCliente(Integer eTagCliente) {
        this.eTagCliente = eTagCliente;
    }

    public String getValorLealtadCliente() {
        return valorLealtadCliente;
    }

    public void setValorLealtadCliente(String valorLealtadCliente) {
        this.valorLealtadCliente = valorLealtadCliente;
    }

    public Boolean getEsPerflInversor() {
        return esPerflInversor;
    }

    public void setEsPerflInversor(Boolean esPerflInversor) {
        this.esPerflInversor = esPerflInversor;
    }

    public String getIdSucursalAsignada() {
        return idSucursalAsignada;
    }

    public void setIdSucursalAsignada(String idSucursalAsignada) {
        this.idSucursalAsignada = idSucursalAsignada;
    }

    public Integer getIdConvenio() {
        return idConvenio;
    }

    public void setIdConvenio(Integer idConvenio) {
        this.idConvenio = idConvenio;
    }

    public String getTipoConvenio() {
        return tipoConvenio;
    }

    public void setTipoConvenio(String tipoConvenio) {
        this.tipoConvenio = tipoConvenio;
    }

    public String getIdTipoCliente() {
        return idTipoCliente;
    }

    public void setIdTipoCliente(String idTipoCliente) {
        this.idTipoCliente = idTipoCliente;
    }

    public String getIdTipoBanca() {
        return idTipoBanca;
    }

    public void setIdTipoBanca(String idTipoBanca) {
        this.idTipoBanca = idTipoBanca;
    }

    public BigDecimal getValorRentabilidadCa() {
        return valorRentabilidadCa;
    }

    public void setValorRentabilidadCa(BigDecimal valorRentabilidadCa) {
        this.valorRentabilidadCa = valorRentabilidadCa;
    }

    public BigDecimal getValorRentabilidadCc() {
        return valorRentabilidadCc;
    }

    public void setValorRentabilidadCc(BigDecimal valorRentabilidadCc) {
        this.valorRentabilidadCc = valorRentabilidadCc;
    }

    public BigDecimal getValorRentabilidadEmerix() {
        return valorRentabilidadEmerix;
    }

    public void setValorRentabilidadEmerix(BigDecimal valorRentabilidadEmerix) {
        this.valorRentabilidadEmerix = valorRentabilidadEmerix;
    }

    public BigDecimal getValorRentabilidadGenesys() {
        return valorRentabilidadGenesys;
    }

    public void setValorRentabilidadGenesys(BigDecimal valorRentabilidadGenesys) {
        this.valorRentabilidadGenesys = valorRentabilidadGenesys;
    }

    public BigDecimal getValorRentabilidadIvr() {
        return valorRentabilidadIvr;
    }

    public void setValorRentabilidadIvr(BigDecimal valorRentabilidadIvr) {
        this.valorRentabilidadIvr = valorRentabilidadIvr;
    }

    public BigDecimal getValorRentabilidadMesaCambio() {
        return valorRentabilidadMesaCambio;
    }

    public void setValorRentabilidadMesaCambio(BigDecimal valorRentabilidadMesaCambio) {
        this.valorRentabilidadMesaCambio = valorRentabilidadMesaCambio;
    }

    public BigDecimal getValorRentabilidadPf() {
        return valorRentabilidadPf;
    }

    public void setValorRentabilidadPf(BigDecimal valorRentabilidadPf) {
        this.valorRentabilidadPf = valorRentabilidadPf;
    }

    public BigDecimal getValorRentabilidadPh() {
        return valorRentabilidadPh;
    }

    public void setValorRentabilidadPh(BigDecimal valorRentabilidadPh) {
        this.valorRentabilidadPh = valorRentabilidadPh;
    }

    public BigDecimal getValorRentabilidadPp() {
        return valorRentabilidadPp;
    }

    public void setValorRentabilidadPp(BigDecimal valorRentabilidadPp) {
        this.valorRentabilidadPp = valorRentabilidadPp;
    }

    public BigDecimal getValorRentabilidadTc() {
        return valorRentabilidadTc;
    }

    public void setValorRentabilidadTc(BigDecimal valorRentabilidadTc) {
        this.valorRentabilidadTc = valorRentabilidadTc;
    }

    public String getNumeroIdentificacionFatca() {
        return numeroIdentificacionFatca;
    }

    public void setNumeroIdentificacionFatca(String numeroIdentificacionFatca) {
        this.numeroIdentificacionFatca = numeroIdentificacionFatca;
    }

    public String getFechaFirmaFormularioFatca() {
        return fechaFirmaFormularioFatca;
    }

    public void setFechaFirmaFormularioFatca(String fechaFirmaFormularioFatca) {
        this.fechaFirmaFormularioFatca = fechaFirmaFormularioFatca;
    }

    public String getTipoFormularioFatca() {
        return tipoFormularioFatca;
    }

    public void setTipoFormularioFatca(String tipoFormularioFatca) {
        this.tipoFormularioFatca = tipoFormularioFatca;
    }

    public String getIdSituacionLaboral() {
        return idSituacionLaboral;
    }

    public void setIdSituacionLaboral(String idSituacionLaboral) {
        this.idSituacionLaboral = idSituacionLaboral;
    }

    public Boolean getIndicioFatca() {
        return indicioFatca;
    }

    public void setIndicioFatca(Boolean indicioFatca) {
        this.indicioFatca = indicioFatca;
    }

    public Integer geteTagPreguntasCSC() {
        return eTagPreguntasCSC;
    }

    public void seteTagPreguntasCSC(Integer eTagPreguntasCSC) {
        this.eTagPreguntasCSC = eTagPreguntasCSC;
    }

    public String getCategoriaMonotributo() {
        return categoriaMonotributo;
    }

    public void setCategoriaMonotributo(String categoriaMonotributo) {
        this.categoriaMonotributo = categoriaMonotributo;
    }

    public String getFechaRecategorizacionMonotributo() {
        return fechaRecategorizacionMonotributo;
    }

    public void setFechaRecategorizacionMonotributo(String fechaRecategorizacionMonotributo) {
        this.fechaRecategorizacionMonotributo = fechaRecategorizacionMonotributo;
    }

    public String getFechaPerfilPatrimonial() {
        return fechaPerfilPatrimonial;
    }

    public void setFechaPerfilPatrimonial(String fechaPerfilPatrimonial) {
        this.fechaPerfilPatrimonial = fechaPerfilPatrimonial;
    }

    public BigDecimal getImportePerfilPatrimonial() {
        return importePerfilPatrimonial;
    }

    public void setImportePerfilPatrimonial(BigDecimal importePerfilPatrimonial) {
        this.importePerfilPatrimonial = importePerfilPatrimonial;
    }

    public String getTipoCompania() {
        return tipoCompania;
    }

    public void setTipoCompania(String tipoCompania) {
        this.tipoCompania = tipoCompania;
    }

    public Boolean getCajaAhorroMenores() {
        return cajaAhorroMenores;
    }

    public void setCajaAhorroMenores(Boolean cajaAhorroMenores) {
        this.cajaAhorroMenores = cajaAhorroMenores;
    }

    public Integer getEdad() {
        return edad;
    }

    public void setEdad(Integer edad) {
        this.edad = edad;
    }

    public String getApellidoConyuge() {
        return apellidoConyuge;
    }

    public void setApellidoConyuge(String apellidoConyuge) {
        this.apellidoConyuge = apellidoConyuge;
    }

    public String getNombreConyuge() {
        return nombreConyuge;
    }

    public void setNombreConyuge(String nombreConyuge) {
        this.nombreConyuge = nombreConyuge;
    }
}
