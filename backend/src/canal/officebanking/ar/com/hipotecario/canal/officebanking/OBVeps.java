package ar.com.hipotecario.canal.officebanking;

import java.math.BigDecimal;

import ar.com.hipotecario.backend.base.Fecha;
import ar.com.hipotecario.backend.base.Futuro;
import ar.com.hipotecario.backend.conector.api.ApiObjeto;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.ApiLinkPagosVep;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.Contribuyentes;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.VepsPagados;
import ar.com.hipotecario.backend.servicio.api.linkPagosVep.VepsPendientes;
import ar.com.hipotecario.backend.servicio.api.productos.Cuentas.Cuenta;

public class OBVeps extends ModuloOB {

	public static Object solicitudPagoVep(ContextoOB contexto) {
//		String idTributarioCliente = contexto.parametros.string("cliente.idTributario", null);
//		String idTributarioEmpresa = contexto.parametros.string("empresa.idTributario", null);
//		String idTributarioContribuyente = contexto.parametros.string("contribuyente.idTributario", null);
//		BigDecimal importe = contexto.parametros.bigDecimal("importe", null);
//		String token = contexto.parametros.string("token", null);
//		String numeroCuenta = contexto.parametros.string("cuenta.numero", null);
//		String tipoProducto = contexto.parametros.string("cuenta.tipo", null);
//		String moneda = contexto.parametros.string("cuenta.moneda.id", null);
//		String numeroTarjeta = contexto.parametros.string("tarjetaDebito.numero", null);
//		String numeroVep = contexto.parametros.string("numeroVep", null);

//		SesionOB sesion = contexto.sesion();
//		Objeto datos = new Objeto();

		return null;

	}

	public static Object vepsPendiente(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string(":idtributariocliente");
		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente", null);
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		Fecha fechaHasta = contexto.parametros.fecha("fechaHasta", "yyyy-MM-dd", null);
		String idTributarioEmpresa = contexto.parametros.string("idTributarioEmpresa", null);
		String idTributarioOriginante = contexto.parametros.string("idTributarioOriginante", null);
		String maxCantidad = contexto.parametros.string("maxCantidad", null);
		String numeroTarjeta = contexto.parametros.string("numerotarjeta");
		String numeroVep = contexto.parametros.string("numeroVep", null);
		String tipoConsultaLink = contexto.parametros.string("tipoConsultaLink");
		String pagina = contexto.parametros.string("pagina", null);

		Futuro<VepsPendientes> ecs = ApiLinkPagosVep.vepPendientes(contexto, fechaDesde, fechaHasta, idTributarioCliente, idTributarioContribuyente, idTributarioEmpresa, idTributarioOriginante, maxCantidad, numeroTarjeta, numeroVep, pagina, tipoConsultaLink);
		VepsPendientes ecs1 = ecs.get();
		return respuesta("datos", ecs1);

	}

	public static Object consultaContribuyente(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string(":idtributariocliente");
		String idTributarioEmpresa = contexto.parametros.string("idTributarioEmpresa", null);
		String maxCantidad = contexto.parametros.string("maxCantidad", null);
		String numeroTarjeta = contexto.parametros.string("numerotarjeta");
		String pagina = contexto.parametros.string("pagina", null);

		Futuro<Contribuyentes> ecs = ApiLinkPagosVep.contribuyentes(contexto, idTributarioCliente, idTributarioEmpresa, maxCantidad, numeroTarjeta, pagina);
		Contribuyentes ecs1 = ecs.get();

		return respuesta("datos", ecs1);

	}

	public static Object altaContribuyente(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string("cliente.idTributario");
		String idTributarioEmpresa = contexto.parametros.string("empresa.idTributario");
		String idTributarioContribuyente = contexto.parametros.string("contribuyente.idTributario");
		String numeroTarjeta = contexto.parametros.string("tarjetaDebito.numero");
		String referencia = contexto.parametros.string("referencia");

		Futuro<Boolean> ecs = ApiLinkPagosVep.agregarContribuyente(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, idTributarioContribuyente, referencia);
		Boolean ecs1 = ecs.get();

		return respuesta("datos", ecs1);

	}

	public static Object bajaContribuyente(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string("idtributariocliente");
		String idTributarioEmpresa = contexto.parametros.string("idTributarioEmpresa");
		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente");
		String numeroTarjeta = contexto.parametros.string("numerotarjeta");

		Futuro<Boolean> ecs = ApiLinkPagosVep.eliminarContribuyente(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, idTributarioContribuyente);

		return respuesta("datos", ecs.get());

	}

	public static Object vepsPagados(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string(":idtributariocliente");
		String idTributarioContribuyente = contexto.parametros.string("idTributarioContribuyente", null);
		Fecha fechaDesde = contexto.parametros.fecha("fechaDesde", "yyyy-MM-dd", null);
		String idTributarioEmpresa = contexto.parametros.string("idTributarioEmpresa", null);
		String idTributarioOriginante = contexto.parametros.string("idTributarioOriginante", null);
		String maxCantidad = contexto.parametros.string("maxCantidad", null);
		String numeroTarjeta = contexto.parametros.string("numerotarjeta");
		String numeroVep = contexto.parametros.string("numeroVep", null);
		String tipoConsultaLink = contexto.parametros.string("tipoConsultaLink");

		Futuro<VepsPagados> ecs = ApiLinkPagosVep.vepsPagados(contexto, fechaDesde, idTributarioCliente, idTributarioContribuyente, idTributarioOriginante, idTributarioEmpresa, maxCantidad, numeroTarjeta, numeroVep, tipoConsultaLink);
		VepsPagados ecs1 = ecs.get();
		return respuesta("datos", ecs1);

	}

	public static Object eliminarVep(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string("idtributariocliente");
		String numeroTarjeta = contexto.parametros.string("numerotarjeta");
		String numeroVep = contexto.parametros.string("numeroVep", null);
		ApiObjeto ecs = ApiLinkPagosVep.eliminarVep(contexto, idTributarioCliente, numeroTarjeta, numeroVep).get();
		return respuesta("datos", ecs);

	}

	public static Object pagarVep(ContextoOB contexto) {

		String idTributarioCliente = contexto.parametros.string("cliente.idTributario", null);
		String idTributarioEmpresa = contexto.parametros.string("empresa.idTributario", null);
		String idTributarioContribuyente = contexto.parametros.string("contribuyente.idTributario", null);
		BigDecimal importe = contexto.parametros.bigDecimal("importe", null);
		String token = contexto.parametros.string("token", null);
		String numeroCuenta = contexto.parametros.string("cuenta.numero", null);
		String tipoProducto = contexto.parametros.string("cuenta.tipo", null);
		String moneda = contexto.parametros.string("cuenta.moneda.id", null);
		String numeroTarjeta = contexto.parametros.string("tarjetaDebito.numero", null);
		String numeroVep = contexto.parametros.string("numeroVep", null);

		ApiObjeto ecs = ApiLinkPagosVep.pagarVep(contexto, idTributarioCliente, idTributarioEmpresa, numeroTarjeta, numeroVep, idTributarioContribuyente, importe, token, convertir(numeroCuenta, tipoProducto, moneda)).get();
		return respuesta("datos", ecs);

	}

	public static Object tokenAFIP(ContextoOB contexto) {
		String idTributarioCliente = contexto.parametros.string("cliente.idTributario", null);
		String idTributarioEmpresa = contexto.parametros.string("empresa.idTributario", null);
		String numerotarjetaDebito = contexto.parametros.string("tarjetaDebito.numero", null);

		ApiObjeto ecs = ApiLinkPagosVep.tokenAfip(contexto, idTributarioCliente, idTributarioEmpresa, numerotarjetaDebito).get();
		return respuesta("datos", ecs);

	}

	private static Cuenta convertir(String numeroProducto, String tipoProducto, String moneda) {
		Cuenta cuenta = new Cuenta();

		cuenta.numeroProducto = numeroProducto;
		cuenta.tipoProducto = tipoProducto;
		cuenta.moneda = moneda;
		return cuenta;
	}
}