package ar.com.hipotecario.canal.officebanking.jpa.ob;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.servicio.api.catalogo.ApiCatalogo;
import ar.com.hipotecario.backend.servicio.api.catalogo.Bancos.Banco;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaCoelsa;
import ar.com.hipotecario.backend.servicio.api.cuentas.CuentaLink;
import ar.com.hipotecario.backend.servicio.api.cuentas.EstadoCuenta;
import ar.com.hipotecario.backend.servicio.sql.intercambioate.BancosInterbanking;
import ar.com.hipotecario.canal.officebanking.ContextoOB;
import ar.com.hipotecario.canal.officebanking.ModuloOB;
import ar.com.hipotecario.canal.officebanking.SesionOB;
import ar.com.hipotecario.canal.officebanking.jpa.dto.InfoCuentaDTO;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.BeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.MonedaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoBeneficiarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.transferencia.TipoCuentaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.repositorio.BeneficiarioOBRepositorio;

public class ServicioBeneficiarioOB extends ServicioOB {

	private BeneficiarioOBRepositorio repo;

	public ServicioBeneficiarioOB(ContextoOB contexto) {
		super(contexto);
		repo = new BeneficiarioOBRepositorio();
		repo.setEntityManager(this.getEntityManager());
	}

	public Futuro<List<BeneficiarioOB>> beneficiarios() {
		return futuro(() -> repo.findAll());
	}

	public Futuro<BeneficiarioOB> beneficiario(Integer id) {
		return futuro(() -> repo.find(id));
	}

	public Futuro<List<BeneficiarioOB>> findByEmpresa(EmpresaOB empresaOB) {
		return futuro(() -> repo.findByField("empresa", empresaOB));
	}

	public Futuro<BeneficiarioOB> findByCBU(EmpresaOB empresa, String cbu) {
		return futuro(() -> repo.cbu(empresa, cbu));
	}

	public Futuro<List<BeneficiarioOB>> findByTipoBeneficiario(EmpresaOB empresa, TipoBeneficiarioOB tipo) {
		return futuro(() -> repo.beneficiario(empresa, tipo));
	}

	public Futuro<BeneficiarioOB> altaBeneficiario(ContextoOB contexto, InfoCuentaDTO info, String email, String referencia) {
		SesionOB sesion = contexto.sesion();

		CuentaCoelsa cuenta = info.cuenta;
		
		Long cuit = Long.parseLong(cuenta.cuit);
		TipoCuentaOB tipoCuenta = cuenta.tipoCuenta.equals("CC") ? TipoCuentaOB.CC : TipoCuentaOB.CA;

		Integer tipoBeneficiario = cuit.equals(sesion.empresaOB.cuit) ? 1 : 2;
		ServicioTipoBeneficiarioOB servicio = new ServicioTipoBeneficiarioOB(contexto);
		TipoBeneficiarioOB tipoBeneficiarioOB = servicio.find(tipoBeneficiario).get();

		BeneficiarioOB beneficiario = new BeneficiarioOB();

		for (MonedaOB moneda : info.monedas) {
			beneficiario.monedas.add(moneda);
		}

		beneficiario.cbu = cuenta.cbu;
		beneficiario.alias = cuenta != null ? cuenta.nuevoAlias : null;
		beneficiario.cuit = cuit;
		
		beneficiario.nombre = !cuenta.nombreTitular.isBlank() ? cuenta.nombreTitular : (cuenta != null ? cuenta.nuevoAlias : cuenta.cbu);
		beneficiario.email = email;
		beneficiario.referencia = referencia;

		Banco datosBanco = ApiCatalogo.bancos(contexto, cuenta.nroBco).get();
		beneficiario.bancoDestino = datosBanco.Descripcion; // TODO.: TRADUCIR CODIGO A NOMBRE BANCO
		beneficiario.tipoCuenta = tipoCuenta;
		beneficiario.tipo = tipoBeneficiarioOB;
		beneficiario.empresa = sesion.empresaOB;
		beneficiario.ultimaModificacion = null;

		return futuro(() -> repo.create(beneficiario));
	}

	public Futuro<BeneficiarioOB> editarBeneficiario(ContextoOB contexto, BeneficiarioOB beneficiario, String email, String referencia) {

		beneficiario.email = email == null ? beneficiario.email : email;
		beneficiario.referencia = referencia == null ? beneficiario.referencia : referencia;
		beneficiario.ultimaModificacion = LocalDateTime.now();

		return futuro(() -> repo.update(beneficiario));
	}

	public void bajaBeneficiario(ContextoOB contexto, BeneficiarioOB beneficiario) {
		repo.delete(beneficiario);
	}

	public InfoCuentaDTO infoCBUAlias(ContextoOB contexto, String cbuAlias) {
		String cuitCuenta = null;
		return infoCBUAlias(contexto, cbuAlias, cuitCuenta);
	}

	public InfoCuentaDTO infoCBUAlias(ContextoOB contexto, String cbuAlias, String cuitCuenta) {
		String cuit = cuitCuenta;
		String cbu = ModuloOB.cbuValido(cbuAlias) ? cbuAlias : null;
		String alias = ModuloOB.aliasValido(cbuAlias) ? cbuAlias : null;
		String cuenta = null;
		CuentaLink linkpesos=null;
		CuentaLink linkdolares=null;
		CuentaCoelsa coelsa = null;
		List<MonedaOB> monedas = new ArrayList<MonedaOB>();
		ServicioMonedaOB servicioMoneda = new ServicioMonedaOB(contexto);
		if(alias != null){
			coelsa = ApiCuentas.cuentaCoelsa(contexto, cbu, alias).tryGet();
			if (empty(coelsa) || !coelsa.ctaActiva) {
				// TODO Falta en el Front campo Cuit, para consultar estadoCuenta el CUIT es
				// obligatorio.
				if (!empty(cuit)) {
					coelsa = new CuentaCoelsa();
					coelsa.cbu = cbu;
					coelsa.cuit = cuit;
				} else {
					return null;
				}
			}else{
				cbu = coelsa.cbu;
			}
		}
		if(cbu != null){
			if(alias==null){
				coelsa = ApiCuentas.cuentaCoelsa(contexto, cbu, alias).tryGet();
			}
			if(empty(coelsa)){

					linkpesos = ApiCuentas.cuentaLink(contexto,cbu,"80").tryGet();
					if(!empty(linkpesos)){
						MonedaOB moneda = servicioMoneda.find(Integer.valueOf(linkpesos.moneda)).tryGet();
						monedas.add(moneda);
						cuenta = linkpesos.cuenta;
					}
					linkdolares = ApiCuentas.cuentaLink(contexto,cbu,"2").tryGet();
					if(!empty(linkdolares)){
						MonedaOB moneda = servicioMoneda.find(Integer.valueOf(linkdolares.moneda)).tryGet();
						if(!monedas.stream().map(m -> m.id ).collect(Collectors.toSet()).contains(moneda.id)){
							monedas.add(moneda);
						}
						cuenta = linkdolares.cuenta;
					}

			}else{
				linkpesos = ApiCuentas.cuentaLink(contexto,cbu,"80").tryGet();
				if(!empty(linkpesos)){
					MonedaOB moneda = servicioMoneda.find(Integer.valueOf(linkpesos.moneda)).tryGet();
					monedas.add(moneda);
					cuenta = linkpesos.cuenta;
				}
				linkdolares = ApiCuentas.cuentaLink(contexto,cbu,"2").tryGet();
				if(!empty(linkdolares)){
					MonedaOB moneda = servicioMoneda.find(Integer.valueOf(linkdolares.moneda)).tryGet();
					monedas.add(moneda);
					cuenta = linkdolares.cuenta;
				}				
			}

		}
		if(empty(coelsa) && empty(linkpesos) && empty((linkdolares))) {
			return null;
		}
		EstadoCuenta estadoCuenta =null;
		InfoCuentaDTO info = new InfoCuentaDTO();
		if(!empty(coelsa)) {
			estadoCuenta = ApiCuentas.estadoCuenta(contexto, coelsa.cbu, coelsa.cuit).tryGet();
			// RUCC VALIDO INVALIDO / CAMARA

			if (empty(estadoCuenta) || estadoCuenta.codigoHttp() != 200) {
				if (cbu.startsWith("000")||BancosInterbanking.get(contexto, "OB", cbu.substring(0, 3)) != null) {
					Banco datosBanco = ApiCatalogo.bancos(contexto, coelsa.nroBco).get();
					coelsa.nroBco = datosBanco != null ? datosBanco.codigo : "";
					coelsa.ctaActiva = coelsa.ctaActiva;
					coelsa.tipoCuenta = coelsa.tipoCuenta.equals("AHO") ? "CA" : "CC";
				}else {
					return null;
				}
			} else {
				coelsa.nroBco = estadoCuenta.CodigoBancoBCRA;
				coelsa.ctaActiva = estadoCuenta.CodigoRespuesta.contains("CBU_OK");
				coelsa.tipoCuenta = estadoCuenta.TipoDeCuenta;
			}

			if(estadoCuenta != null) {
				MonedaOB moneda = servicioMoneda.findBySimbol(estadoCuenta.Moneda).get();
				if (empty(moneda)) {
					return null;
				}

				if(!monedas.stream().map(m -> m.id ).collect(Collectors.toSet()).contains(moneda.id)){
					monedas.add(moneda);
				}
			}

			info.cuenta = coelsa;
			info.numero = estadoCuenta != null ? estadoCuenta.Numero : cuenta;
			info.monedas = monedas;

		}
		
		if(!empty(linkpesos) && empty(coelsa)){
			info.cuenta = info.linktocoelsa(linkpesos);
			info.numero = linkpesos.cuenta;
			info.monedas = monedas;
		} else if (!empty(linkdolares) && empty(coelsa)) {
			info.cuenta = info.linktocoelsa(linkdolares);
			info.numero = linkdolares.cuenta;
			info.monedas = monedas;
		} else if(!empty(coelsa) && empty(linkpesos) && empty(linkdolares) && info.monedas.size() == 0) {
			List<MonedaOB> lstMonedas = servicioMoneda.findAll().get();
			if (empty(lstMonedas)) {
				return null;
			}
			
			for(MonedaOB m : lstMonedas) {
				monedas.add(m);
			}
		}

		return info;
	}

}