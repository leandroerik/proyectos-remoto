package ar.com.hipotecario.backend.servicio.api.empresas;

import java.text.SimpleDateFormat;
import java.util.Date;

import ar.com.hipotecario.backend.conector.api.ApiObjeto;

public class AltaConsultaCapitaRequest extends ApiObjeto {
	public Integer alcance;
	public String apellido;
	public String archivo;
	public String bancaAux;
	public String base;
	public String calle;
	public Integer cantidadFilas;
	public String caracteristica;
	public String ciudadNac;
	public Integer cliente;
	public Integer codConv;
	public Integer codConvHasta;
	public String codigo;
	public String codigoHasta;
	public String codigoPostal;
	public Integer convenio;
	public String correctos;
	public String cuenta;
	public String cuitEmpresa;
	public String ddi;
	public String ddn;
	public String depto;
	public String desc;
	public String descConv;
	public String dirElec;
	public String documento;
	public String eCivilDefault;
	public Integer errMigracion;
	public Integer errValExt;
	public Integer errValInt;
	public Integer errValidacion;
	public String esfcl;
	public String estCivil;
	public String estado;
	public String estadoConv;
	public String fechaDesde; // example: 2019-02-19T16:32:22.436Z
	public String fechaHasta; // example: 2019-02-19T16:32:22.436Z
	public String fechaIngreso;
	public String fechaNac;
	public Integer formatoFecha;
	public Integer funcionarioHasta;
	public Integer grupoHasta;
	public String idPersonal;
	public String idTributaria;
	public String ivaAux;
	public Double ivaExen;
	public Double ivaReduc;
	public String localidad;
	public Integer lote;
	public Integer loteHasta;
	public Integer modulo;
	public Integer moduloHasta;
	public Integer monedaHasta;
	public Integer nacionalidad;
	public String nombre;
	public String nroTelefono;
	public Integer numero;
	public String observaciones;
	public Integer oficialHasta;
	public String oficina;
	public Integer opcion;
	public String operacion;
	public Integer pais;
	public String parametro;
	public String parametroHasta;
	public String pep;
	public String piso;
	public String prefijoTelefono;
	public String profesion;
	public Integer provincia;
	public String quienLlama;
	public String rSocial;
	public Integer regMigrados;
	public Integer regTotal;
	public String residenciaAux;
	public String sectorAux;
	public Integer secuencial;
	public Integer secuencialHasta;
	public String seleccionados;
	public Integer set;
	public Integer setHasta;
	public String sexo;
	public String situacionLaboral;
	public Integer solicitud;
	public String sp;
	public String spHasta;
	public Integer sueldo;
	public String tidPersonal;
	public String tidTributaria;
	public String tipoDirElec;
	public String tipoPersona;
	public String tipoTarjeta;
	public String tipoTarjetaHasta;
	public String tipoTelefono;
	public String tipoTitularAux;
	public String ttelefono;
	public String type;
	public String validacion;
	public String valor;

	public Integer getAlcance() {
		return alcance;
	}

	public void setAlcance(Integer alcance) {
		this.alcance = alcance;
	}

	public String getApellido() {
		return apellido;
	}

	public void setApellido(String apellido) {
		this.apellido = apellido;
	}

	public String getArchivo() {
		return archivo;
	}

	public void setArchivo(String archivo) {
		this.archivo = archivo;
	}

	public String getBancaAux() {
		return bancaAux;
	}

	public void setBancaAux(String bancaAux) {
		this.bancaAux = bancaAux;
	}

	public String getBase() {
		return base;
	}

	public void setBase(String base) {
		this.base = base;
	}

	public String getCalle() {
		return calle;
	}

	public void setCalle(String calle) {
		this.calle = calle;
	}

	public Integer getCantidadFilas() {
		return cantidadFilas;
	}

	public void setCantidadFilas(Integer cantidadFilas) {
		this.cantidadFilas = cantidadFilas;
	}

	public String getCaracteristica() {
		return caracteristica;
	}

	public void setCaracteristica(String caracteristica) {
		this.caracteristica = caracteristica;
	}

	public String getCiudadNac() {
		return ciudadNac;
	}

	public void setCiudadNac(String ciudadNac) {
		this.ciudadNac = ciudadNac;
	}

	public Integer getCliente() {
		return cliente;
	}

	public void setCliente(Integer cliente) {
		this.cliente = cliente;
	}

	public Integer getCodConv() {
		return codConv;
	}

	public void setCodConv(Integer codConv) {
		this.codConv = codConv;
	}

	public Integer getCodConvHasta() {
		return codConvHasta;
	}

	public void setCodConvHasta(Integer codConvHasta) {
		this.codConvHasta = codConvHasta;
	}

	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public String getCodigoHasta() {
		return codigoHasta;
	}

	public void setCodigoHasta(String codigoHasta) {
		this.codigoHasta = codigoHasta;
	}

	public String getCodigoPostal() {
		return codigoPostal;
	}

	public void setCodigoPostal(String codigoPostal) {
		this.codigoPostal = codigoPostal;
	}

	public Integer getConvenio() {
		return convenio;
	}

	public void setConvenio(Integer convenio) {
		this.convenio = convenio;
	}

	public String getCorrectos() {
		return correctos;
	}

	public void setCorrectos(String correctos) {
		this.correctos = correctos;
	}

	public String getCuenta() {
		return cuenta;
	}

	public void setCuenta(String cuenta) {
		this.cuenta = cuenta;
	}

	public String getCuitEmpresa() {
		return cuitEmpresa;
	}

	public void setCuitEmpresa(String cuitEmpresa) {
		this.cuitEmpresa = cuitEmpresa;
	}

	public String getDdi() {
		return ddi;
	}

	public void setDdi(String ddi) {
		this.ddi = ddi;
	}

	public String getDdn() {
		return ddn;
	}

	public void setDdn(String ddn) {
		this.ddn = ddn;
	}

	public String getDepto() {
		return depto;
	}

	public void setDepto(String depto) {
		this.depto = depto;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getDescConv() {
		return descConv;
	}

	public void setDescConv(String descConv) {
		this.descConv = descConv;
	}

	public String getDirElec() {
		return dirElec;
	}

	public void setDirElec(String dirElec) {
		this.dirElec = dirElec;
	}

	public String getDocumento() {
		return documento;
	}

	public void setDocumento(String documento) {
		this.documento = documento;
	}

	public String geteCivilDefault() {
		return eCivilDefault;
	}

	public void seteCivilDefault(String eCivilDefault) {
		this.eCivilDefault = eCivilDefault;
	}

	public Integer getErrMigracion() {
		return errMigracion;
	}

	public void setErrMigracion(Integer errMigracion) {
		this.errMigracion = errMigracion;
	}

	public Integer getErrValExt() {
		return errValExt;
	}

	public void setErrValExt(Integer errValExt) {
		this.errValExt = errValExt;
	}

	public Integer getErrValInt() {
		return errValInt;
	}

	public void setErrValInt(Integer errValInt) {
		this.errValInt = errValInt;
	}

	public Integer getErrValidacion() {
		return errValidacion;
	}

	public void setErrValidacion(Integer errValidacion) {
		this.errValidacion = errValidacion;
	}

	public String getEsfcl() {
		return esfcl;
	}

	public void setEsfcl(String esfcl) {
		this.esfcl = esfcl;
	}

	public String getEstCivil() {
		return estCivil;
	}

	public void setEstCivil(String estCivil) {
		this.estCivil = estCivil;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	public String getEstadoConv() {
		return estadoConv;
	}

	public void setEstadoConv(String estadoConv) {
		this.estadoConv = estadoConv;
	}

	public String getFechaDesde() {
		return fechaDesde;
	}

	public void setFechaDesde(String fechaDesde) {
		this.fechaDesde = fechaDesde;
	}

	public String getFechaHasta() {
		return fechaHasta;
	}

	public void setFechaHasta(String fechaHasta) {
		this.fechaHasta = fechaHasta;
	}

	public String getFechaIngreso() {
		return fechaIngreso;
	}

	public void setFechaIngreso(String fechaIngreso) {
		try {
			Date fechaParsed = new SimpleDateFormat("dd/MM/yyyy").parse(fechaIngreso);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			this.fechaIngreso = sdf.format(fechaParsed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * public void setFechaIngreso(String fechaIngreso) { this.fechaIngreso =
	 * fechaIngreso; }
	 */

	/*
	 * public String getFechaNac() { return fechaNac; }
	 * 
	 * public void setFechaNac(String fechaNac) { this.fechaNac = fechaNac; }
	 */
	private Date fechaNacimiento;

	public void setFechaNac(String fechaNac) {
		try {
			Date fechaParsed = new SimpleDateFormat("dd/MM/yyyy").parse(fechaNac);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			this.fechaNac = sdf.format(fechaParsed);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String getFechaNacimiento() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		return sdf.format(fechaNacimiento);
	}

	public Integer getFormatoFecha() {
		return formatoFecha;
	}

	public void setFormatoFecha(Integer formatoFecha) {
		this.formatoFecha = formatoFecha;
	}

	public Integer getFuncionarioHasta() {
		return funcionarioHasta;
	}

	public void setFuncionarioHasta(Integer funcionarioHasta) {
		this.funcionarioHasta = funcionarioHasta;
	}

	public Integer getGrupoHasta() {
		return grupoHasta;
	}

	public void setGrupoHasta(Integer grupoHasta) {
		this.grupoHasta = grupoHasta;
	}

	public String getIdPersonal() {
		return idPersonal;
	}

	public void setIdPersonal(String idPersonal) {
		this.idPersonal = idPersonal;
	}

	public String getIdTributaria() {
		return idTributaria;
	}

	public void setIdTributaria(String idTributaria) {
		this.idTributaria = idTributaria;
	}

	public String getIvaAux() {
		return ivaAux;
	}

	public void setIvaAux(String ivaAux) {
		this.ivaAux = ivaAux;
	}

	public Double getIvaExen() {
		return ivaExen;
	}

	public void setIvaExen(Double ivaExen) {
		this.ivaExen = ivaExen;
	}

	public Double getIvaReduc() {
		return ivaReduc;
	}

	public void setIvaReduc(Double ivaReduc) {
		this.ivaReduc = ivaReduc;
	}

	public String getLocalidad() {
		return localidad;
	}

	public void setLocalidad(String localidad) {
		this.localidad = localidad;
	}

	public Integer getLote() {
		return lote;
	}

	public void setLote(Integer lote) {
		this.lote = lote;
	}

	public Integer getLoteHasta() {
		return loteHasta;
	}

	public void setLoteHasta(Integer loteHasta) {
		this.loteHasta = loteHasta;
	}

	public Integer getModulo() {
		return modulo;
	}

	public void setModulo(Integer modulo) {
		this.modulo = modulo;
	}

	public Integer getModuloHasta() {
		return moduloHasta;
	}

	public void setModuloHasta(Integer moduloHasta) {
		this.moduloHasta = moduloHasta;
	}

	public Integer getMonedaHasta() {
		return monedaHasta;
	}

	public void setMonedaHasta(Integer monedaHasta) {
		this.monedaHasta = monedaHasta;
	}

	public int getNacionalidad() {
		return nacionalidad;
	}

	public void setNacionalidad(int nacionalidad) {
		this.nacionalidad = nacionalidad;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getNroTelefono() {
		return nroTelefono;
	}

	public void setNroTelefono(String nroTelefono) {
		this.nroTelefono = nroTelefono;
	}

	public Integer getNumero() {
		return numero;
	}

	public void setNumero(Integer numero) {
		this.numero = numero;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public void setObservaciones(String observaciones) {
		this.observaciones = observaciones;
	}

	public Integer getOficialHasta() {
		return oficialHasta;
	}

	public void setOficialHasta(Integer oficialHasta) {
		this.oficialHasta = oficialHasta;
	}

	public String getOficina() {
		return oficina;
	}

	public void setOficina(String oficina) {
		this.oficina = oficina;
	}

	public Integer getOpcion() {
		return opcion;
	}

	public void setOpcion(Integer opcion) {
		this.opcion = opcion;
	}

	public String getOperacion() {
		return operacion;
	}

	public void setOperacion(String operacion) {
		this.operacion = operacion;
	}

	public Integer getPais() {
		return pais;
	}

	public void setPais(Integer pais) {
		this.pais = pais;
	}

	public String getParametro() {
		return parametro;
	}

	public void setParametro(String parametro) {
		this.parametro = parametro;
	}

	public String getParametroHasta() {
		return parametroHasta;
	}

	public void setParametroHasta(String parametroHasta) {
		this.parametroHasta = parametroHasta;
	}

	public String getPep() {
		return pep;
	}

	public void setPep(String pep) {
		this.pep = pep;
	}

	public String getPiso() {
		return piso;
	}

	public void setPiso(String piso) {
		this.piso = piso;
	}

	public String getPrefijoTelefono() {
		return prefijoTelefono;
	}

	public void setPrefijoTelefono(String prefijoTelefono) {
		this.prefijoTelefono = prefijoTelefono;
	}

	public String getProfesion() {
		return profesion;
	}

	public void setProfesion(String profesion) {
		this.profesion = profesion;
	}

	public Integer getProvincia() {
		return provincia;
	}

	public void setProvincia(Integer provincia) {
		this.provincia = provincia;
	}

	public String getQuienLlama() {
		return quienLlama;
	}

	public void setQuienLlama(String quienLlama) {
		this.quienLlama = quienLlama;
	}

	public String getrSocial() {
		return rSocial;
	}

	public void setrSocial(String rSocial) {
		this.rSocial = rSocial;
	}

	public Integer getRegMigrados() {
		return regMigrados;
	}

	public void setRegMigrados(Integer regMigrados) {
		this.regMigrados = regMigrados;
	}

	public Integer getRegTotal() {
		return regTotal;
	}

	public void setRegTotal(Integer regTotal) {
		this.regTotal = regTotal;
	}

	public String getResidenciaAux() {
		return residenciaAux;
	}

	public void setResidenciaAux(String residenciaAux) {
		this.residenciaAux = residenciaAux;
	}

	public String getSectorAux() {
		return sectorAux;
	}

	public void setSectorAux(String sectorAux) {
		this.sectorAux = sectorAux;
	}

	public Integer getSecuencial() {
		return secuencial;
	}

	public void setSecuencial(Integer secuencial) {
		this.secuencial = secuencial;
	}

	public Integer getSecuencialHasta() {
		return secuencialHasta;
	}

	public void setSecuencialHasta(Integer secuencialHasta) {
		this.secuencialHasta = secuencialHasta;
	}

	public String getSeleccionados() {
		return seleccionados;
	}

	public void setSeleccionados(String seleccionados) {
		this.seleccionados = seleccionados;
	}

	public Integer getSet() {
		return set;
	}

	public void setSet(Integer set) {
		this.set = set;
	}

	public Integer getSetHasta() {
		return setHasta;
	}

	public void setSetHasta(Integer setHasta) {
		this.setHasta = setHasta;
	}

	public String getSexo() {
		return sexo;
	}

	public void setSexo(String sexo) {
		this.sexo = sexo;
	}

	public String getSituacionLaboral() {
		return situacionLaboral;
	}

	public void setSituacionLaboral(String situacionLaboral) {
		this.situacionLaboral = situacionLaboral;
	}

	public Integer getSolicitud() {
		return solicitud;
	}

	public void setSolicitud(Integer solicitud) {
		this.solicitud = solicitud;
	}

	public String getSp() {
		return sp;
	}

	public void setSp(String sp) {
		this.sp = sp;
	}

	public String getSpHasta() {
		return spHasta;
	}

	public void setSpHasta(String spHasta) {
		this.spHasta = spHasta;
	}

	public Integer getSueldo() {
		return sueldo;
	}

	public void setSueldo(Integer sueldo) {
		this.sueldo = sueldo;
	}

	public String getTidPersonal() {
		return tidPersonal;
	}

	public void setTidPersonal(String tidPersonal) {
		this.tidPersonal = tidPersonal;
	}

	public String getTidTributaria() {
		return tidTributaria;
	}

	public void setTidTributaria(String tidTributaria) {
		this.tidTributaria = tidTributaria;
	}

	public String getTipoDirElec() {
		return tipoDirElec;
	}

	public void setTipoDirElec(String tipoDirElec) {
		this.tipoDirElec = tipoDirElec;
	}

	public String getTipoPersona() {
		return tipoPersona;
	}

	public void setTipoPersona(String tipoPersona) {
		this.tipoPersona = tipoPersona;
	}

	public String getTipoTarjeta() {
		return tipoTarjeta;
	}

	public void setTipoTarjeta(String tipoTarjeta) {
		this.tipoTarjeta = tipoTarjeta;
	}

	public String getTipoTarjetaHasta() {
		return tipoTarjetaHasta;
	}

	public void setTipoTarjetaHasta(String tipoTarjetaHasta) {
		this.tipoTarjetaHasta = tipoTarjetaHasta;
	}

	public String getTipoTelefono() {
		return tipoTelefono;
	}

	public void setTipoTelefono(String tipoTelefono) {
		this.tipoTelefono = tipoTelefono;
	}

	public String getTipoTitularAux() {
		return tipoTitularAux;
	}

	public void setTipoTitularAux(String tipoTitularAux) {
		this.tipoTitularAux = tipoTitularAux;
	}

	public String getTtelefono() {
		return ttelefono;
	}

	public void setTtelefono(String ttelefono) {
		this.ttelefono = ttelefono;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getValidacion() {
		return validacion;
	}

	public void setValidacion(String validacion) {
		this.validacion = validacion;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}
}
