package ar.com.hipotecario.mobile.negocio.cuentainversor;

import java.util.List;

public class CuentaInversor {

	private String idPersonaFondo;
	private String cobis;
	private String sucursal;
	private String razonSocial;
	private String tipoIdTributario;
	private String cuit;
	private String tipoSujeto;
	private String calificacion;
	private String actividad;
	private String situacionGanancia;
	private String condicionIva;
	private String direccion;
	private String telefonoVFNet;
	private String telefonoUnitrade;
	private String email;
	private String nombre;
	private String apellido;
	private String tipoPersona;
	private String idPersona;
	private DomicilioCuentaInversor domicilio;
	private String fechaNacimiento;
	private String fechaIngreso;
	private String paisNacimiento;
	private String paisNacionalidad;
	private CuentasLiquidacion cuentasLiquidacion;
	private Origen origen;
	private Radicacion radicacion;
	private List<CuotasBancarias> cuotasBancarias;
	private DatosUIF datosUIF;
	private Boolean esFisico;
	private Boolean esmasculino;
	private Boolean esPEP;
	private Boolean imprimeResumenCuenta;
	private Boolean poseeInstrPagoPerm;
	private Boolean requiereFirmaConjunta;
	private String tipoDoc;
	private String numDoc;
	private String cuotaPartista;
	private String actividadPrincipal;
	private String categoria;
	private String numCuotapartista;
	private String segmentoInversion;
	private String tipoCuotapartista;
	private String tipoInversor;
	private String tipoPerfilRiesgo;
	private String representantes;
	private String tarjetasCredito;
	private String idPersonaCondomino;
	private String perfil;
	private String patrimonio;
	private String idBanco;
	private String promedioMensual;
	private String tipoContribuyente;
	private String tipoEstadoCivil;
	private String identTributario;
	private String numIdentificador;
	private String lugarRegistracion;
	private String provExpedicionCI;
	private String residenteExterior;
	private String PaisResidenciaFiscal;
	private String identificacionFiscal;

	public CuentaInversor() {
	}

	public CuentaInversor(String cobis, String sucursal, String razonSocial, String tipoIdTributario, String cuit, String tipoSujeto, String calificacion, String actividad, String situacionGanancia, String condicionIva, String direccion, String telefonoVFNet, String telefonoUnitrade, String email, String nombre, String apellido, String tipoPersona, String idPersona, DomicilioCuentaInversor domicilio, String fechaNacimiento, String fechaIngreso, String paisNacimiento, String paisNacionalidad, CuentasLiquidacion cuentasLiquidacion, Origen origen, Radicacion radicacion, List<CuotasBancarias> cuotasBancarias, DatosUIF datosUIF, Boolean esFisico, Boolean esmasculino, Boolean esPEP, Boolean imprimeResumenCuenta, Boolean poseeInstrPagoPerm, Boolean requiereFirmaConjunta, String tipoDoc,
			String numDoc, String cuotaPartista, String actividadPrincipal, String categoria, String numCuotapartista, String segmentoInversion, String tipoCuotapartista, String tipoInversor, String tipoPerfilRiesgo, String representantes, String tarjetasCredito, String idPersonaCondomino, String perfil, String patrimonio, String idBanco, String promedioMensual, String tipoContribuyente, String tipoEstadoCivil, String identTributario, String numIdentificador, String lugarRegistracion, String provExpedicionCI, String residenteExterior, String paisResidenciaFiscal, String identificacionFiscal) {
		this.cobis = cobis;
		this.sucursal = sucursal;
		this.razonSocial = razonSocial;
		this.tipoIdTributario = tipoIdTributario;
		this.cuit = cuit;
		this.tipoSujeto = tipoSujeto;
		this.calificacion = calificacion;
		this.actividad = actividad;
		this.situacionGanancia = situacionGanancia;
		this.condicionIva = condicionIva;
		this.direccion = direccion;
		this.telefonoVFNet = telefonoVFNet;
		this.telefonoUnitrade = telefonoUnitrade;
		this.email = email;
		this.nombre = nombre;
		this.apellido = apellido;
		this.tipoPersona = tipoPersona;
		this.idPersona = idPersona;
		this.domicilio = domicilio;
		this.fechaNacimiento = fechaNacimiento;
		this.fechaIngreso = fechaIngreso;
		this.paisNacimiento = paisNacimiento;
		this.paisNacionalidad = paisNacionalidad;
		this.cuentasLiquidacion = cuentasLiquidacion;
		this.origen = origen;
		this.radicacion = radicacion;
		this.cuotasBancarias = cuotasBancarias;
		this.datosUIF = datosUIF;
		this.esFisico = esFisico;
		this.esmasculino = esmasculino;
		this.esPEP = esPEP;
		this.imprimeResumenCuenta = imprimeResumenCuenta;
		this.poseeInstrPagoPerm = poseeInstrPagoPerm;
		this.requiereFirmaConjunta = requiereFirmaConjunta;
		this.tipoDoc = tipoDoc;
		this.numDoc = numDoc;
		this.cuotaPartista = cuotaPartista;
		this.actividadPrincipal = actividadPrincipal;
		this.categoria = categoria;
		this.numCuotapartista = numCuotapartista;
		this.segmentoInversion = segmentoInversion;
		this.tipoCuotapartista = tipoCuotapartista;
		this.tipoInversor = tipoInversor;
		this.tipoPerfilRiesgo = tipoPerfilRiesgo;
		this.representantes = representantes;
		this.tarjetasCredito = tarjetasCredito;
		this.idPersonaCondomino = idPersonaCondomino;
		this.perfil = perfil;
		this.patrimonio = patrimonio;
		this.idBanco = idBanco;
		this.promedioMensual = promedioMensual;
		this.tipoContribuyente = tipoContribuyente;
		this.tipoEstadoCivil = tipoEstadoCivil;
		this.identTributario = identTributario;
		this.numIdentificador = numIdentificador;
		this.lugarRegistracion = lugarRegistracion;
		this.provExpedicionCI = provExpedicionCI;
		this.residenteExterior = residenteExterior;
		PaisResidenciaFiscal = paisResidenciaFiscal;
		this.identificacionFiscal = identificacionFiscal;
	}

	public String getIdPersonaFondo() {
		return idPersonaFondo;
	}

	public void setIdPersonaFondo(String idPersonaFondo) {
		this.idPersonaFondo = idPersonaFondo;
	}

	public String getTelefonoUnitrade() {
		return telefonoUnitrade;
	}

	public void setTelefonoUnitrade(String telefonoUnitrade) {
		this.telefonoUnitrade = telefonoUnitrade;
	}

	public String getCobis() {
		return cobis;
	}

	public void setCobis(String cobis) {
		this.cobis = cobis;
	}

	public String getSucursal() {
		return sucursal;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public String getRazonSocial() {
		return razonSocial;
	}

	public void setRazonSocial(String razonSocial) {
		this.razonSocial = razonSocial;
	}

	public String getTipoIdTributario() {
		return tipoIdTributario;
	}

	public void setTipoIdTributario(String tipoIdTributario) {
		this.tipoIdTributario = tipoIdTributario;
	}

	public String getCuit() {
		return cuit;
	}

	public void setCuit(String cuit) {
		this.cuit = cuit;
	}

	public String getTipoSujeto() {
		return tipoSujeto;
	}

	public void setTipoSujeto(String tipoSujeto) {
		this.tipoSujeto = tipoSujeto;
	}

	public String getCalificacion() {
		return calificacion;
	}

	public void setCalificacion(String calificacion) {
		this.calificacion = calificacion;
	}

	public String getActividad() {
		return actividad;
	}

	public void setActividad(String actividad) {
		this.actividad = actividad;
	}

	public String getSituacionGanancia() {
		return situacionGanancia;
	}

	public void setSituacionGanancia(String situacionGanancia) {
		this.situacionGanancia = situacionGanancia;
	}

	public String getCondicionIva() {
		return condicionIva;
	}

	public void setCondicionIva(String condicionIva) {
		this.condicionIva = condicionIva;
	}

	public String getDireccion() {
		return direccion;
	}

	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}

	public String getTelefonoVFNet() {
		return telefonoVFNet;
	}

	public void setTelefonoVFNet(String telefonoVFNet) {
		this.telefonoVFNet = telefonoVFNet;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getTipoPersona() {
		return tipoPersona;
	}

	public void setTipoPersona(String tipoPersona) {
		this.tipoPersona = tipoPersona;
	}

	public String getIdPersona() {
		return idPersona;
	}

	public void setIdPersona(String idPersona) {
		this.idPersona = idPersona;
	}

	public DomicilioCuentaInversor getDomicilio() {
		return domicilio;
	}

	public void setDomicilio(DomicilioCuentaInversor domicilio) {
		this.domicilio = domicilio;
	}

	public String getFechaNacimiento() {
		return fechaNacimiento;
	}

	public void setFechaNacimiento(String fechaNacimiento) {
		this.fechaNacimiento = fechaNacimiento;
	}

	public String getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(String fechaIngreso) {
		this.fechaIngreso = fechaIngreso;
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

	public CuentasLiquidacion getCuentasLiquidacion() {
		return cuentasLiquidacion;
	}

	public void setCuentasLiquidacion(CuentasLiquidacion cuentasLiquidacion) {
		this.cuentasLiquidacion = cuentasLiquidacion;
	}

	public Origen getOrigen() {
		return origen;
	}

	public void setOrigen(Origen origen) {
		this.origen = origen;
	}

	public Radicacion getRadicacion() {
		return radicacion;
	}

	public void setRadicacion(Radicacion radicacion) {
		this.radicacion = radicacion;
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

	public Boolean getEsFisico() {
		return esFisico;
	}

	public void setEsFisico(Boolean esFisico) {
		this.esFisico = esFisico;
	}

	public Boolean getEsmasculino() {
		return esmasculino;
	}

	public void setEsmasculino(Boolean esmasculino) {
		this.esmasculino = esmasculino;
	}

	public Boolean getEsPEP() {
		return esPEP;
	}

	public void setEsPEP(Boolean esPEP) {
		this.esPEP = esPEP;
	}

	public Boolean getImprimeResumenCuenta() {
		return imprimeResumenCuenta;
	}

	public void setImprimeResumenCuenta(Boolean imprimeResumenCuenta) {
		this.imprimeResumenCuenta = imprimeResumenCuenta;
	}

	public Boolean getPoseeInstrPagoPerm() {
		return poseeInstrPagoPerm;
	}

	public void setPoseeInstrPagoPerm(Boolean poseeInstrPagoPerm) {
		this.poseeInstrPagoPerm = poseeInstrPagoPerm;
	}

	public Boolean getRequiereFirmaConjunta() {
		return requiereFirmaConjunta;
	}

	public void setRequiereFirmaConjunta(Boolean requiereFirmaConjunta) {
		this.requiereFirmaConjunta = requiereFirmaConjunta;
	}

	public String getTipoDoc() {
		return tipoDoc;
	}

	public void setTipoDoc(String tipoDoc) {
		this.tipoDoc = tipoDoc;
	}

	public String getNumDoc() {
		return numDoc;
	}

	public void setNumDoc(String numDoc) {
		this.numDoc = numDoc;
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

	public String getNumCuotapartista() {
		return numCuotapartista;
	}

	public void setNumCuotapartista(String numCuotapartista) {
		this.numCuotapartista = numCuotapartista;
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

	public String getPerfil() {
		return perfil;
	}

	public void setPerfil(String perfil) {
		this.perfil = perfil;
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

	public String getTipoEstadoCivil() {
		return tipoEstadoCivil;
	}

	public void setTipoEstadoCivil(String tipoEstadoCivil) {
		this.tipoEstadoCivil = tipoEstadoCivil;
	}

	public String getIdentTributario() {
		return identTributario;
	}

	public void setIdentTributario(String identTributario) {
		this.identTributario = identTributario;
	}

	public String getNumIdentificador() {
		return numIdentificador;
	}

	public void setNumIdentificador(String numIdentificador) {
		this.numIdentificador = numIdentificador;
	}

	public String getLugarRegistracion() {
		return lugarRegistracion;
	}

	public void setLugarRegistracion(String lugarRegistracion) {
		this.lugarRegistracion = lugarRegistracion;
	}

	public String getProvExpedicionCI() {
		return provExpedicionCI;
	}

	public void setProvExpedicionCI(String provExpedicionCI) {
		this.provExpedicionCI = provExpedicionCI;
	}

	public String getResidenteExterior() {
		return residenteExterior;
	}

	public void setResidenteExterior(String residenteExterior) {
		this.residenteExterior = residenteExterior;
	}

	public String getPaisResidenciaFiscal() {
		return PaisResidenciaFiscal;
	}

	public void setPaisResidenciaFiscal(String paisResidenciaFiscal) {
		PaisResidenciaFiscal = paisResidenciaFiscal;
	}

	public String getIdentificacionFiscal() {
		return identificacionFiscal;
	}

	public void setIdentificacionFiscal(String identificacionFiscal) {
		this.identificacionFiscal = identificacionFiscal;
	}
}
