package ar.com.hipotecario.canal.officebanking;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.notificaciones.ApiNotificaciones;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioTipoBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoCuentaOB;
import ar.com.hipotecario.canal.officebanking.enums.EnumMigracionTransmit;

public class OBBeneficiarios extends ModuloOB {

	public static Object beneficiario(ContextoOB contexto) {
		Integer idBeneficiario = contexto.parametros.integer("idBeneficiario");

		SesionOB sesion = contexto.sesion();

		ServicioBeneficiarioOB servicio = new ServicioBeneficiarioOB(contexto);
		BeneficiarioOB beneficiarioOB = servicio.beneficiario(idBeneficiario).tryGet();
		if (beneficiarioOB == null) {
			return respuesta("DATOS_INVALIDOS");
		}

		if (beneficiarioOB.empresa.emp_codigo.intValue() != sesion.empresaOB.emp_codigo.intValue()) {
			return respuesta("DATOS_INVALIDOS");
		}

		Objeto beneficiario = new Objeto();
		beneficiario.set("id", beneficiarioOB.id);
		beneficiario.set("nombre", beneficiarioOB.nombre);
		beneficiario.set("cbu", beneficiarioOB.cbu);
		beneficiario.set("alias", beneficiarioOB.alias);
		beneficiario.set("cuit", beneficiarioOB.cuit);
		beneficiario.set("bancoDestino", beneficiarioOB.bancoDestino);
		beneficiario.set("referencia", beneficiarioOB.referencia);
		beneficiario.set("email", beneficiarioOB.email);
		Boolean esCVU = OBTransferencias.esCVU(beneficiarioOB.cbu);
		beneficiario.set("esCVU", esCVU);

		Objeto monedas = new Objeto();
		if (esCVU) beneficiarioOB.monedas = beneficiarioOB.monedas.stream().filter(monedaOB -> monedaOB.codigoCobis.equals("ARS")).toList();
		for (MonedaOB moneda : beneficiarioOB.monedas) {
			Objeto mon = new Objeto();
			mon.set("codigo", moneda.codigoCobis);
			mon.set("simbolo", moneda.simbolo);
			mon.set("descripcion", moneda.descripcion);
			monedas.add(mon);
		}


		beneficiario.set("monedas", monedas);
		TipoCuentaOB tipoCuentaBeneficiario = beneficiarioOB.tipoCuenta.toString().equals("CTE") ? TipoCuentaOB.CC : TipoCuentaOB.CA;
		Objeto tcb = new Objeto();
		tcb.set("id", tipoCuentaBeneficiario.ordinal());
		tcb.set("descripcionCorta", tipoCuentaBeneficiario.name());
		tcb.set("descripcionLarga", tipoCuentaBeneficiario.getDescripcionLarga());
		beneficiario.set("tipoCuenta", tcb);
		beneficiario.set("tipoBeneficiario", beneficiarioOB.tipo.descripcion);

		return respuesta("datos", beneficiario);
	}

	public static Object beneficiarios(ContextoOB contexto) {
		Integer idTipoBeneficiario = contexto.parametros.integer("tipoBeneficiario", null);
		Integer idOperacionInicial = contexto.parametros.integer("idOperacionInicial", null);
		Integer limite = contexto.parametros.integer("limite", null);

		SesionOB sesion = contexto.sesion();

		if (empty(sesion.empresaOB)) {
			return respuesta("EMPRESA_INVALIDA");
		}

		ServicioBeneficiarioOB servicio = new ServicioBeneficiarioOB(contexto);
		List<BeneficiarioOB> beneficiarios = null;

		if (!empty(idTipoBeneficiario)) {
			ServicioTipoBeneficiarioOB servicioTB = new ServicioTipoBeneficiarioOB(contexto);
			TipoBeneficiarioOB tipoBeneficiario = servicioTB.find(idTipoBeneficiario).tryGet();
			if (empty(tipoBeneficiario)) {
				return respuesta("TIPO_BENEFICIARIO_INVALIDO");
			}
			beneficiarios = servicio.findByTipoBeneficiario(sesion.empresaOB, tipoBeneficiario).tryGet();
		}

		beneficiarios = servicio.findByEmpresa(sesion.empresaOB).tryGet();
		if (beneficiarios.size() < 1) {
			return respuesta("SIN_BENEFICIARIOS");
		}

		if (!empty(idOperacionInicial)) {
			beneficiarios = beneficiarios.stream().filter(t -> t.id > idOperacionInicial).collect(Collectors.toList());
		}

		Objeto datos = new Objeto();
		for (BeneficiarioOB b : beneficiarios) {
			Objeto bnf = new Objeto();
			bnf.set("id", b.id);
			bnf.set("nombre", b.nombre.trim());
			bnf.set("bancoDestino", b.bancoDestino);
			bnf.set("tipoBeneficiario", b.tipo.descripcion);
			bnf.set("esCVU",OBTransferencias.esCVU(b.cbu));
			bnf.set("referencia", b.referencia!=null?b.referencia:"");
			datos.add(bnf);
			if (limite != null && datos.toList().size() == limite) {
				break;
			}

		}
		return respuesta("datos", datos);
	}

	public static Object alta(ContextoOB contexto) {
		String cbu = contexto.parametros.string("cbu", null);
		String alias = contexto.parametros.string("alias", null);
		String email = contexto.parametros.string("email", null);
		String referencia = contexto.parametros.string("referencia", null);
		String fechaCreacion= Fecha.ahora().toString();
		SesionOB sesion = contexto.sesion();

		if (!aliasValido(alias) && !cbuValido(cbu)) {
			return respuesta("DATOS_INCORRECTOS");
		}

		ServicioBeneficiarioOB servicio = new ServicioBeneficiarioOB(contexto);

		BeneficiarioOB beneficiario = servicio.findByCBU(sesion.empresaOB, cbu).tryGet();
		if (!empty(beneficiario)) {
			return respuesta("BENEFICIARIO_EXISTENTE");
		}

		InfoCuentaDTO infoCuenta = servicio.infoCBUAlias(contexto, cbu != null ? cbu : alias);
		if (empty(infoCuenta)) {
			return respuesta("CBU_ALIAS_INVALIDO");
		}
		infoCuenta.monedas = infoCuenta.monedas.stream()
				.collect(Collectors.toMap(m->m.id,m->m,(m1,m2)->m1)).values().stream().toList();
		
		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}else {
			Boolean response = OBTransmit.lecturaCsmIdAuth(contexto);
				
			if (!response) {
				LogOB.evento(contexto, "firmar", "STOP_DEBIT_FACTOR_NO_VALIDADO_TRANSMIT");
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

		servicio.altaBeneficiario(contexto, infoCuenta, email, referencia);

		String usuario = sesion.usuarioOB.nombre+""+sesion.usuarioOB.apellido;
		String banco = ApiCatalogo.bancos(contexto, infoCuenta.cuenta.nroBco).tryGet().Descripcion.toString();
		ApiNotificaciones.envioAvisoBenfiario(contexto, infoCuenta, banco!=null?banco:"", fechaCreacion, sesion.usuarioOB.email, usuario).tryGet();

		return respuesta("0");
	}

	public static Object baja(ContextoOB contexto) {
		String cbu = contexto.parametros.string("cbu");

		if (empty(cbu) || !cbuValido(cbu)) {
			return respuesta("DATOS_INCORRECTOS");
		}

		SesionOB sesion = contexto.sesion();

		if(sesion.usuarioOB.migrado != EnumMigracionTransmit.MIGRADO.getCodigo()) {
			if (sesion.token.fechaValidacion.isNull()) {
				return respuesta("FACTOR_NO_VALIDADO");
			}
		}
		
		sesion.token.fechaValidacion = Fecha.nunca();
		sesion.save();

		ServicioBeneficiarioOB servicio = new ServicioBeneficiarioOB(contexto);
		BeneficiarioOB beneficiario = servicio.findByCBU(sesion.empresaOB, cbu).tryGet();
		if (empty(beneficiario)) {
			return respuesta("BENEFICIARIO_INEXISTENTE");
		}

		servicio.bajaBeneficiario(contexto, beneficiario);

		return respuesta("0");
	}

	public static Object editar(ContextoOB contexto) {
		String cbu = contexto.parametros.string("cbu");
		String email = contexto.parametros.string("email", null);
		String referencia = contexto.parametros.string("referencia", null);

		if (empty(cbu) || !cbuValido(cbu)) {
			return respuesta("DATOS_INCORRECTOS");
		}

		SesionOB sesion = contexto.sesion();

		ServicioBeneficiarioOB servicio = new ServicioBeneficiarioOB(contexto);
		BeneficiarioOB beneficiario = servicio.findByCBU(sesion.empresaOB, cbu).tryGet();
		if (empty(beneficiario)) {
			return respuesta("BENEFICIARIO_INEXISTENTE");
		}

		servicio.editarBeneficiario(contexto, beneficiario, email, referencia);

		return respuesta("0");
	}

	public static Object validarCbuBeneficiario(ContextoOB contexto) {
		String cbuAlias = contexto.parametros.string("cbu");
		SesionOB sesion = contexto.sesion();

		if (!cbuValido(cbuAlias) && !aliasValido(cbuAlias)) {
			return respuesta("CBU_ALIAS_FORMATO_INVALIDO");
		}

		ServicioBeneficiarioOB servicioBeneficiario = new ServicioBeneficiarioOB(contexto);
		InfoCuentaDTO info = servicioBeneficiario.infoCBUAlias(contexto, cbuAlias);

		if (empty(info)) {
			return respuesta("CBU_ALIAS_INVALIDO");
		}

		CuentaCoelsa cuenta = info.cuenta;
		if (!cuenta.ctaActiva) {
			return respuesta("CUENTA_INACTIVA");
		}


		BeneficiarioOB beneficiario = servicioBeneficiario.findByCBU(sesion.empresaOB, info.cuenta.cbu).tryGet();
		if (!empty(beneficiario)) {
			return respuesta("BENEFICIARIO_EXISTENTE");
		}

		return respuesta("0");
	}


	public static boolean esNuevoBeneficiario(ContextoOB contexto, EmpresaOB empresa, String cbu){
		BeneficiarioOB beneficiario = new ServicioBeneficiarioOB(contexto).findByCBU(empresa,cbu).tryGet();
		return beneficiario==null||beneficiario.fechaCreacion.plusDays(1).isAfter(LocalDateTime.now());
	}
	public static boolean esUnipersonal(String cuit){
		return cuit.startsWith("20") || cuit.startsWith("23") || cuit.startsWith("27");
	}


}