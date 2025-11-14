package ar.com.hipotecario.canal.buhobank;

import java.util.ArrayList;
import java.util.List;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.backend.base.Lista;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.backend.exception.ApiException;
import ar.com.hipotecario.backend.servicio.api.cuentas.ApiCuentas;
import ar.com.hipotecario.backend.servicio.api.cuentas.TarjetasDebitoAsociadasCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.cuentas.TarjetasDebitoAsociadasCajaAhorro.TarjetaDebitoAsociadasCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Moneda;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Paquete;
import ar.com.hipotecario.backend.servicio.api.paquetes.Paquetes.Producto;
import ar.com.hipotecario.backend.servicio.api.personas.ApiPersonas;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos;
import ar.com.hipotecario.backend.servicio.api.personas.Telefonos.Telefono;
import ar.com.hipotecario.backend.servicio.api.productos.ApiProductos;
import ar.com.hipotecario.backend.servicio.api.productos.Productos;
import ar.com.hipotecario.backend.servicio.api.ventas.ApiVentas;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.Integrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Integrantes.NuevoIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.MailAvisos;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion.BuhoBank;
import ar.com.hipotecario.backend.servicio.api.ventas.Resolucion.NuevaResolucion;
import ar.com.hipotecario.backend.servicio.api.ventas.RolIntegrantes.RolIntegrante;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud;
import ar.com.hipotecario.backend.servicio.api.ventas.Solicitud.NuevaSolicitud;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudCajaAhorro.NuevaSolicitudCajaAhorro;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.CuentaLegal;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.DomicilioResumen;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.ProductosNuevos;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.ResolucionPaquete;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudPaquete.ProductoExistente;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaCredito;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaDebito;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaDebito.CuentasOperativas;
import ar.com.hipotecario.backend.servicio.api.ventas.SolicitudTarjetaDebito.NuevaSolicitudTarjetaDebito;
import ar.com.hipotecario.backend.servicio.sql.SqlBuhoBank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank;
import ar.com.hipotecario.backend.servicio.sql.buhobank.BBPaquetesBuhobank.BBPaqueteBuhobank;
import ar.com.hipotecario.backend.servicio.sql.campniaswf.DatosTarjetaCredito;

public class BBVentas extends Modulo {

	public static Solicitud crearSolicitud(ContextoBB contexto) {
		NuevaSolicitud nuevaSolicitud = new NuevaSolicitud();
		nuevaSolicitud.TipoOperacion = contexto.prendidoOfertaMotor() ? SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD : SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		nuevaSolicitud.Oficina = GeneralBB.OFICINA_PAQ;
		nuevaSolicitud.CanalOriginacion1 = contexto.canalOriginacion1();
		nuevaSolicitud.CanalOriginacion2 = contexto.canalOriginacion2();
		nuevaSolicitud.CanalOriginacion3 = contexto.canalOriginacion3();
		nuevaSolicitud.CanalVenta1 = contexto.canalVenta1();
		nuevaSolicitud.CanalVenta2 = contexto.canalVenta2();
		nuevaSolicitud.CanalVenta3 = contexto.canalVenta3();
		nuevaSolicitud.CanalVenta4 = contexto.canalVenta4();

		try {
			return ApiVentas.crearSolicitud(contexto, nuevaSolicitud).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.SOLICITUD_NO_CREADA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.SOLICITUD_NO_CREADA, e.getMessage());
			return null;
		}
	}

	public static Solicitud obtenerSolicitud(ContextoBB contexto, String idSolicitud) {
		if (empty(idSolicitud)) {
			return null;
		}

		return ApiVentas.solicitud(contexto, idSolicitud).tryGet();
	}

	public static Integrante agregarIntegrante(ContextoBB contexto, String idSolicitud, String cuil, String evento) {
		NuevoIntegrante nuevoIntegrante = new NuevoIntegrante();
		nuevoIntegrante.TipoOperacion = contexto.prendidoOfertaMotor() ? SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD : SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		nuevoIntegrante.NumeroTributario = cuil;

		try {
			return ApiVentas.crearIntegrante(contexto, idSolicitud, nuevoIntegrante).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, evento, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, evento, e.getMessage());
			return null;
		}
	}

	public static SolicitudTarjetaCredito tarjetaCreditoFinal(ContextoBB contexto, List<RolIntegrante> integrantes, DomicilioResumen domicilioResumen) {
		SesionBB sesion = contexto.sesion();

		MailAvisos mailAvisos = new MailAvisos();
		mailAvisos.Tipo = GeneralBB.TIPO_MAIL_AVISOS_TC;
		mailAvisos.Direccion = sesion.mail;

		SolicitudTarjetaCredito tarjetaCredito = new SolicitudTarjetaCredito();
		tarjetaCredito.Integrantes = integrantes;
		tarjetaCredito.Domicilio = domicilioResumen;
		tarjetaCredito.MailAvisos = mailAvisos;

		DatosTarjetaCredito datosTC = sesion.obtenerDatosTC();
		if (datosTC == null) {
			return null;
		}

		tarjetaCredito.Producto = datosTC.producto();
		tarjetaCredito.Afinidad = datosTC.afinidad();
		tarjetaCredito.CarteraGrupo = datosTC.carteraGrupo();
		tarjetaCredito.ModeloLiquidacion = datosTC.modeloLiquidacion();
		tarjetaCredito.Distribucion = datosTC.distribucion();
		tarjetaCredito.Caracteristica = !empty(sesion.subProducto) ? sesion.subProducto : GeneralBB.SUB_BUHO_PUNTOS;
		tarjetaCredito.Embozado = sesion.embozado();
		tarjetaCredito.Limite = sesion.limite;
		tarjetaCredito.Telefono = asignarTipoTelefono(contexto);
		tarjetaCredito.FormaPago = GeneralBB.FORMA_PAGO_TC;
		tarjetaCredito.TipoCuenta = GeneralBB.TIPO_CUENTA_TC;
		tarjetaCredito.NumeroCuenta = GeneralBB.NUMERO_CUENTA_TC;
		tarjetaCredito.EmpresaAseguradora = GeneralBB.EMPRESA_ASEGURADORA_TC;
		tarjetaCredito.SucursalCuenta = GeneralBB.SUCURSAL_CUENTA_TC;
		tarjetaCredito.AvisosViaMail = GeneralBB.AVISOS_VIA_MAIL_TC;
		tarjetaCredito.AvisosCorreoTradicional = GeneralBB.AVISOS_CORREO_TC;
		tarjetaCredito.esVirtual = sesion.esTcVirtual() && !sesion.esTdFisica();
		tarjetaCredito.altaOnline = sesion.esTcVirtual();

		return tarjetaCredito;
	}

	public static String asignarTipoTelefono(ContextoBB contexto) {

		String tipoTelefono = GeneralBB.TELEFONO_TC;

		SesionBB sesion = contexto.sesion();

		Telefonos telefonos = ApiPersonas.telefonos(contexto, sesion.cuil, false).tryGet();
		if (telefonos == null) {
			telefonos = new Telefonos();
		}

		Telefono telefonoCelular = telefonos.celular();
		Telefono telefonoParticular = telefonos.particular();

		if (Telefonos.esBatch(telefonoCelular)) {
			if (telefonoParticular != null && !Telefonos.esBatch(telefonoParticular)) {
				LogBB.evento(contexto, GeneralBB.ALTA_CON_TELEFONO_PARTICULAR);
				return Telefono.PARTICULAR;
			}
		}

		return tipoTelefono;
	}

	public static SolicitudTarjetaDebito tarjetaDebitoFinal(ContextoBB contexto, Producto producto, List<RolIntegrante> integrantes, DomicilioResumen domicilioResumen) {
		List<CuentasOperativas> cuentasOperativas = new ArrayList<>();
		CuentasOperativas cuentaOperativa = new CuentasOperativas();
		cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
		cuentaOperativa.Cuenta = GeneralBB.CUENTA_CUENTA_OP_TD;
		cuentaOperativa.Moneda = Moneda.PESOS;
		cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
		cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
		cuentasOperativas.add(cuentaOperativa);

		SolicitudTarjetaDebito tarjetaDebito = new SolicitudTarjetaDebito();
		tarjetaDebito.Integrantes = integrantes;
		tarjetaDebito.Domicilio = domicilioResumen;
		tarjetaDebito.TarjetaDebitoCuentasOperativas = cuentasOperativas;
		tarjetaDebito.Tipo = GeneralBB.TIPO_TD;
		tarjetaDebito.Grupo = GeneralBB.GRUPO_TD;
		tarjetaDebito.TipoCuentaComision = GeneralBB.TIPO_CUENTA_COMISION_TD;
		tarjetaDebito.NumeroCtaComision = GeneralBB.NUMERO_CTA_COMISION_TD;

		SesionBB sesion = contexto.sesion();
		tarjetaDebito.EsVirtual = sesion.esTdVirtual();
		tarjetaDebito.VisualizaVirtual = tarjetaDebito.EsVirtual ? "S" : "N";
		tarjetaDebito.RequiereEmbozado = sesion.esTdFisica() ? "S": "N";

		return tarjetaDebito;
	}

	public static SolicitudCajaAhorro cajaAhorroFinal(Producto producto, List<RolIntegrante> integrantes, DomicilioResumen domicilioResumen, CuentaLegal cuentaLegal) {
		SolicitudCajaAhorro cajaAhorro = new SolicitudCajaAhorro();
		cajaAhorro.Integrantes = integrantes;
		cajaAhorro.DomicilioResumen = domicilioResumen;
		cajaAhorro.CuentaLegales = cuentaLegal;
		cajaAhorro.Moneda = Moneda.PESOS;
		cajaAhorro.Categoria = GeneralBB.CATEGORIA_CA_ARS;
		cajaAhorro.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_ARS;
		cajaAhorro.Oficial = GeneralBB.OFICIAL_CA_ARS;
		cajaAhorro.Oficina = GeneralBB.OFICINA_CA_ARS;
		cajaAhorro.Origen = GeneralBB.ORIGEN_CA_ARS;
		cajaAhorro.UsoFirma = GeneralBB.USO_FIRMA_CA_ARS;
		cajaAhorro.Ciclo = GeneralBB.CICLO_CA_ARS;
		cajaAhorro.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_ARS;
		cajaAhorro.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_ARS;

		return cajaAhorro;
	}

	public static SolicitudCajaAhorro cajaAhorroDolaresFinal(Producto producto, List<RolIntegrante> integrantes, DomicilioResumen domicilioResumen, CuentaLegal cuentaLegal) {
		SolicitudCajaAhorro cajaAhorroDolar = new SolicitudCajaAhorro();
		cajaAhorroDolar.Integrantes = integrantes;
		cajaAhorroDolar.DomicilioResumen = domicilioResumen;
		cajaAhorroDolar.CuentaLegales = cuentaLegal;
		cajaAhorroDolar.Moneda = Moneda.DOLARES;
		cajaAhorroDolar.Categoria = GeneralBB.CATEGORIA_CA_USD;
		cajaAhorroDolar.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_USD;
		cajaAhorroDolar.Oficial = GeneralBB.OFICIAL_CA_USD;
		cajaAhorroDolar.Oficina = GeneralBB.OFICINA_CA_USD;
		cajaAhorroDolar.Origen = GeneralBB.ORIGEN_CA_USD;
		cajaAhorroDolar.UsoFirma = GeneralBB.USO_FIRMA_CA_USD;
		cajaAhorroDolar.Ciclo = GeneralBB.CICLO_CA_USD;
		cajaAhorroDolar.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_USD;
		cajaAhorroDolar.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_USD;

		return cajaAhorroDolar;
	}

	public static SolicitudPaquete crearPaqueteOptimizado(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String codigoPaquete = sesion.codigoPaqueteMotor;
		String idCobis = sesion.idCobis;
		String letraTC = !empty(sesion.letraTC) && !"0".equals(sesion.letraTC) ? sesion.letraTC : null;

		if (empty(cuil)) {
			LogBB.error(contexto, ErroresBB.PAQUETE_VACIO);
			return null;
		}

		Paquete paqueteApi = BBPaquetes.obtenerPaquetePorCodigo(contexto, codigoPaquete);
		if (paqueteApi == null && !empty(letraTC)) {
			paqueteApi = BBPaquetes.obtenerPaquetePorLetra(contexto, letraTC);

			if (paqueteApi != null) {
				sesion.codigoPaqueteMotor = paqueteApi.codigo;
				sesion.save();
				codigoPaquete = paqueteApi.codigo;
			}
		}

		if (paqueteApi == null) {
			LogBB.error(contexto, ErroresBB.PAQUETE_VACIO);
			return null;
		}

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		CuentaLegal cuentaLegal = new CuentaLegal();
		cuentaLegal.Uso = GeneralBB.USO_CUENTA_LEGAL;
		cuentaLegal.RealizaTransferencias = GeneralBB.REALIZA_TRANSF_CUENTA_LEGAL;

		SolicitudPaquete.Paquete paquete = new SolicitudPaquete.Paquete();

		ProductosNuevos productosNuevos = new ProductosNuevos();
		for (Producto producto : paqueteApi.productos) {
			if (producto.esTarjetaCredito()) {
				productosNuevos.TarjetaCredito = tarjetaCreditoFinal(contexto, integrantes, domicilioResumen);
			}

			if (producto.esTarjetaDebito()) {
				productosNuevos.TarjetaDebito = tarjetaDebitoFinal(contexto, producto, integrantes, domicilioResumen);
			}

			if (producto.esCajaAhorro()) {
				productosNuevos.CajaAhorro = cajaAhorroFinal(producto, integrantes, domicilioResumen, cuentaLegal);
				if (producto.cuentaCobro) {
					paquete.ProductoBancarioCobro = producto.codigo;
					paquete.ProductoCobisCobro = producto.id.toString();
				}
			}

			if (producto.esCajaAhorroDolar()) {
				productosNuevos.CajaAhorroDolares = cajaAhorroDolaresFinal(producto, integrantes, domicilioResumen, cuentaLegal);
				if (producto.cuentaCobro) {
					paquete.ProductoBancarioCobro = producto.codigo;
					paquete.ProductoCobisCobro = producto.id.toString();
				}
			}
		}

		if (sesion.cobisPositivo()) {
			Productos productos = ApiProductos.productos(contexto, idCobis, false).tryGet();
			if(productos != null && productos.size() > 0) {
				String nroTarjetaDebito = null;
				String nroCaPesos = null;

				for (Productos.Producto producto : productos){
					if(!empty(nroCaPesos) && !empty(nroTarjetaDebito)){
						break;
					}

					if (producto.cajaPesosActiva() && producto.categoria.equals("D")) {
						nroCaPesos = producto.numero;

						TarjetasDebitoAsociadasCajaAhorro tarjetaDebito = ApiCuentas.tarjetasDebitoAsociadasCajaAhorro(contexto, producto.numero).tryGet();
						for (TarjetaDebitoAsociadasCajaAhorro tarjeta : tarjetaDebito){
							if("A".equals(tarjeta.estado)){
								nroTarjetaDebito = tarjeta.numeroTarjeta;
								break;
							}
						}
					}
				}

				Lista<ProductoExistente> productosExistentes = new Lista<>();
				List<CuentasOperativas> cuentasOperativas = new ArrayList<>();

				String mensaje = "";

				if (nroTarjetaDebito != null) {
					ProductoExistente tarjetaDebito = new ProductoExistente();
					tarjetaDebito.idProducto = GeneralBB.CODIGO_PRODUCTO_TD;
					tarjetaDebito.NumeroProducto = nroTarjetaDebito;
					tarjetaDebito.Integrantes = integrantes;
					productosExistentes.add(tarjetaDebito);

					ProductoExistente cajaAhorro = new ProductoExistente();
					cajaAhorro.idProducto = GeneralBB.CODIGO_PRODUCTO_CAJA_ARS;
					cajaAhorro.NumeroProducto = nroCaPesos;
					cajaAhorro.Integrantes = integrantes;
					productosExistentes.add(cajaAhorro);

					productosNuevos.TarjetaDebito = null;
					productosNuevos.CajaAhorro = null;
					productosNuevos.TarjetaCredito.NumeroCuenta = nroCaPesos;

					CuentasOperativas cuentaOperativa = new CuentasOperativas();
					cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
					cuentaOperativa.Cuenta = nroCaPesos;
					cuentaOperativa.Moneda = Moneda.PESOS;
					cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
					cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
					cuentasOperativas.add(cuentaOperativa);

					mensaje += "TD|";
				}
				else if(nroCaPesos != null) {

					ProductoExistente cajaAhorro = new ProductoExistente();
					cajaAhorro.idProducto = GeneralBB.CODIGO_PRODUCTO_CAJA_ARS;
					cajaAhorro.NumeroProducto = nroCaPesos;
					cajaAhorro.Integrantes = integrantes;
					productosExistentes.add(cajaAhorro);

					productosNuevos.CajaAhorro = null;
					productosNuevos.TarjetaDebito.NumeroCtaComision = nroCaPesos;
					productosNuevos.TarjetaCredito.NumeroCuenta = nroCaPesos;

					CuentasOperativas cuentaOperativa = new CuentasOperativas();
					cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
					cuentaOperativa.Cuenta = nroCaPesos;
					cuentaOperativa.Moneda = Moneda.PESOS;
					cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
					cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
					cuentasOperativas.add(cuentaOperativa);

					mensaje += "AHO_PESOS|";
				}

				String nroCaUsd = null;

				for (Productos.Producto producto : productos){
					if (producto.esCajaAhorroUsd() && producto.tipoProductoActivo(Productos.TIPO_CAJA_AHORRO)) {
						nroCaUsd = producto.numero;
						break;
					}
				}

				if(nroCaUsd != null) {

					ProductoExistente cajaAhorro = new ProductoExistente();
					cajaAhorro.idProducto = GeneralBB.CODIGO_PRODUCTO_CAJA_USD;
					cajaAhorro.NumeroProducto = nroCaUsd;
					cajaAhorro.Integrantes = integrantes;
					productosExistentes.add(cajaAhorro);

					productosNuevos.CajaAhorroDolares = null;

					CuentasOperativas cuentaOperativa = new CuentasOperativas();
					cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
					cuentaOperativa.Cuenta = nroCaUsd;
					cuentaOperativa.Moneda = Moneda.DOLARES;
					cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
					cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
					cuentasOperativas.add(cuentaOperativa);

					mensaje += "AHO_USD";
				}

				if (productosNuevos.TarjetaDebito != null) {
					productosNuevos.TarjetaDebito.TarjetaDebitoCuentasOperativas = cuentasOperativas;
				}

				paquete.ProductosExistentes = productosExistentes;
				LogBB.evento(contexto, "USUARIO_PAQUETIZADO", mensaje);
			}
		}

		BBPaquetesBuhobank paquetesBuhobank = SqlBuhoBank.obtenerPaquetes(contexto, sesion.getFlujo()).tryGet();
		BBPaqueteBuhobank paqueteBuhobank = BBPaquetesBuhobank.buscarPaquete(paquetesBuhobank, letraTC, sesion.numeroPaquete());
		if (paqueteBuhobank != null) {

			productosNuevos.TarjetaCredito.Afinidad = paqueteBuhobank.afinidad;
			productosNuevos.TarjetaCredito.ModeloLiquidacion = paqueteBuhobank.modelo_liquidacion;
			productosNuevos.TarjetaCredito.Distribucion = paqueteBuhobank.codigo_distribucion;
			paquete.Ciclo = !empty(paqueteBuhobank.ciclo) ? paqueteBuhobank.ciclo : paqueteApi.ciclo;
		} else {
			productosNuevos.TarjetaCredito.Afinidad = null;
			productosNuevos.TarjetaCredito.ModeloLiquidacion = null;
			productosNuevos.TarjetaCredito.Distribucion = null;
			paquete.Ciclo = paqueteApi.ciclo;
		}

		paquete.TipoPaquete = codigoPaquete;
		paquete.Integrantes = integrantes;
		paquete.DomicilioResumen = domicilioResumen;
		paquete.ProductosNuevos = productosNuevos;
		paquete.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_PAQ;
		paquete.OrigenCaptacion = GeneralBB.ORIGEN_CAPTACION_PAQ;
		paquete.Oficina = GeneralBB.OFICINA_PAQ;
		paquete.Oficial = GeneralBB.OFICIAL_PAQ;
		paquete.UsoFirma = GeneralBB.USO_FIRMA_PAQ;
		paquete.ProductoCobisCobro = GeneralBB.PRODUCTO_COBIS_COBRO_PAQ;
		paquete.ProductoBancarioCobro = GeneralBB.PRODUCTO_BANCARIO_COBRO_PAQ;
		paquete.ResumenMagnetico = GeneralBB.RESUMEN_MAGNETICO_PAQ;

		paquete.EleccionDistribucion = SolicitudPaquete.CLIENTE_ELECCION_DISTRIBUCION;
		if (sesion.aSucursal()) {
			paquete.EleccionDistribucion = SolicitudPaquete.BANCO_ELECCION_DISTRIBUCION;
			paquete.DestinoDistribucion = sesion.idSucursal.toString();
		}
		if (sesion.aSucursalAndreani()) {
			paquete.EleccionDistribucion = SolicitudPaquete.ANDREANI_ELECCION_DISTRIBUCION;
			paquete.DestinoDistribucion = sesion.idSucursal.toString();
		}

		ResolucionPaquete resolucionPaquete = new ResolucionPaquete();
		resolucionPaquete.MotivoExcepcion = "";
		resolucionPaquete.FlagSolicitaExcepcion = false;

		SolicitudPaquete nuevoPaquete = new SolicitudPaquete();
		nuevoPaquete.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		nuevoPaquete.EjecutaMotor = GeneralBB.EJECUTAR_MOTOR;
		nuevoPaquete.Resolucion = resolucionPaquete;
		nuevoPaquete.Paquete = paquete;

		try {
			return ApiVentas.crearPaquete(contexto, idSolicitud, nuevoPaquete).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.PAQUETE_VACIO, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.PAQUETE_VACIO, e.getMessage());
			return null;
		}
	}



	public static Resolucion resolucionesPut(ContextoBB contexto, String idSolicitud, String tipoOperacion) {
		NuevaResolucion nuevaResolucion = new NuevaResolucion();
		nuevaResolucion.TipoOperacion = tipoOperacion;

		if (contexto.prendidoOfertaMotor()) {
			SesionBB sesion = contexto.sesion();
			String idSituacionLaboral = sesion.idSituacionLaboral;

			BuhoBank buhoBank = new BuhoBank();
			buhoBank.ingresoNeto = GeneralBB.INGRESO_NETO_BB_RESOLUCION;
			buhoBank.situacionLaboral = idSituacionLaboral;

			nuevaResolucion.BuhoBank = buhoBank;
			nuevaResolucion.TipoInvocacion = GeneralBB.TIPO_INVOCACION_RESOLUCION;
			nuevaResolucion.NroInstancia = GeneralBB.NRO_INSTANCIA_RESOLUCION;
			nuevaResolucion.CodigoMotivoExcepcion = idSolicitud;
			nuevaResolucion.MotivoExcepcion = GeneralBB.MOTIVO_EXCEPCION_RESOLUCION;
			nuevaResolucion.FlagExcepcion = GeneralBB.EXCEPCION_RESOLUCION;
			nuevaResolucion.SolicitaMontoRefuerzo = GeneralBB.SOLICITA_MONTO_REFUERZO;
			nuevaResolucion.FlagSolicitaAprobacionCentralizada = GeneralBB.SOLICITA_APROBACION_CENTRALIZADA;
			nuevaResolucion.FlagSolicitaValidarIdentidad = GeneralBB.SOLICITA_VALIDAR_IDENTIDAD;
			nuevaResolucion.FlagSolicitaAprobacionEstandard = GeneralBB.SOLICITA_APROBACION_ESTANDAR;
			nuevaResolucion.FlagSolicitaExcepcion = GeneralBB.SOLICITA_EXCEPCION;
		}

		try {
			return ApiVentas.resolucionesPut(contexto, idSolicitud, nuevaResolucion).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.RESOLUCION_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.RESOLUCION_VACIA, e.getMessage());
			return null;
		}
	}

	public static Resolucion resolucionesPutStand(ContextoBB contexto, String idSolicitud, String tipoOperacion) {
		NuevaResolucion nuevaResolucion = new NuevaResolucion();
		nuevaResolucion.TipoOperacion = tipoOperacion;

		if (contexto.prendidoOfertaMotor()) {
			SesionBB sesion = contexto.sesion();
			String idSituacionLaboral = sesion.idSituacionLaboral;

			BuhoBank buhoBank = new BuhoBank();
			buhoBank.ingresoNeto = GeneralBB.INGRESO_NETO_BB_RESOLUCION;
			buhoBank.situacionLaboral = idSituacionLaboral;

			nuevaResolucion.BuhoBank = buhoBank;
			nuevaResolucion.TipoInvocacion = GeneralBB.TIPO_INVOCACION_RESOLUCION;
			nuevaResolucion.NroInstancia = GeneralBB.NRO_INSTANCIA_RESOLUCION;
			nuevaResolucion.FlagExcepcion = GeneralBB.EXCEPCION_RESOLUCION;
			nuevaResolucion.FlagSolicitaAprobacionCentralizada = GeneralBB.SOLICITA_APROBACION_CENTRALIZADA;
			nuevaResolucion.FlagSolicitaValidarIdentidad = GeneralBB.SOLICITA_VALIDAR_IDENTIDAD;
			nuevaResolucion.FlagSolicitaComprobarIngresos = GeneralBB.SOLICITA_COMPROBAR_INGRESOS;
			nuevaResolucion.FlagSolicitaAprobacionEstandard = GeneralBB.SOLICITA_APROBACION_ESTANDAR_STAND;
			nuevaResolucion.FlagSolicitaExcepcion = GeneralBB.SOLICITA_EXCEPCION;
			nuevaResolucion.FlagSolicitaEvaluarMercadoAbierto = GeneralBB.SOLICITA_EVALUAR_MERCADO_ABIERTO;
			nuevaResolucion.EsPlanSueldo = GeneralBB.ES_PLAN_SUELDO;
		}

		try {
			return ApiVentas.resolucionesPut(contexto, idSolicitud, nuevaResolucion).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.RESOLUCION_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.RESOLUCION_VACIA, e.getMessage());
			return null;
		}
	}

	public static List<Producto> obtenerProductosStandalone() {
		Moneda monedaPesos = new Moneda();
		monedaPesos.id = "80";
		monedaPesos.descripcion = "PESOS";

		Producto producto1 = new Producto();
		producto1.id = 4;
		producto1.categoria = "CAJA DE AHORRO";
		producto1.codigo = "3";
		producto1.descripcion = "CAJA DE AHORROS";
		producto1.tipo = "AHO";
		producto1.moneda = monedaPesos;
		producto1.cuentaCobro = true;
		producto1.condicionante = "N";
		producto1.categoriaDefault = "EV";
		producto1.opcional = "N";

		Producto producto2 = new Producto();
		producto2.id = 16;
		producto2.categoria = "TARJETA DE DEBITO";
		producto2.codigo = "6";
		producto2.descripcion = "TARJETA DE DEBITO";
		producto2.tipo = "ATM";
		producto2.moneda = monedaPesos;
		producto2.cuentaCobro = false;
		producto2.condicionante = "N";
		producto2.categoriaDefault = "NC|3";
		producto2.opcional = "N";

		List<Producto> productos = new ArrayList<Producto>();
		productos.add(producto1);
		productos.add(producto2);

		return productos;
	}

	public static void guardarRechazoMotor(ContextoBB contexto, Resolucion resolucion, Boolean enSesion) {
		SesionBB sesion = contexto.sesion();

		if (enSesion) {
			sesion.resolucionMotorDeScoring = resolucion == null ? Resolucion.RECHAZAR : resolucion.ResolucionId;
			sesion.saveSesion();
		}
	}

	public static Solicitud finalizarSolicitud(ContextoBB contexto, String idSolicitud) {

		try {
			return ApiVentas.finalizarSolicitud(contexto, idSolicitud).get();
		} catch (ApiException e) {

			if (isInformado(e.response.body.toString())) {
				LogBB.evento(contexto, "ES_INFORMADO");
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);

			} else if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.ERROR_FINALIZAR_SOLICITUD, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isInformado(e.getMessage())) {
				LogBB.evento(contexto, "ES_INFORMADO");
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			} else if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.ERROR_FINALIZAR_SOLICITUD, e.getMessage());
			return null;
		}
	}

	public static SolicitudCajaAhorro solicitarCajaAhorroStandalone(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		NuevaSolicitudCajaAhorro nuevaCajaAhorro = new NuevaSolicitudCajaAhorro();
		nuevaCajaAhorro.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD;
		nuevaCajaAhorro.Moneda = Moneda.PESOS;
		nuevaCajaAhorro.Integrantes = integrantes;

		if (sesion.esDeLaOferta(GeneralBB.BUHO_CGU)) {
			nuevaCajaAhorro.Categoria = GeneralBB.CATEGORIA_CGU;
			nuevaCajaAhorro.Subtipo = GeneralBB.SUBTIPO_CGU;
		} else {
			nuevaCajaAhorro.Categoria = GeneralBB.CATEGORIA_CA_ARS;
		}

		try {
			return ApiVentas.solicitudCajaAhorroStand(contexto, idSolicitud, nuevaCajaAhorro).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.getMessage());
			return null;
		}
	}
	
	public static SolicitudCajaAhorro solicitarCajaAhorroDolaresStandalone(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		NuevaSolicitudCajaAhorro nuevaCajaAhorro = new NuevaSolicitudCajaAhorro();
		nuevaCajaAhorro.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD;
		nuevaCajaAhorro.Moneda = Moneda.DOLARES;
		nuevaCajaAhorro.Integrantes = integrantes;
		nuevaCajaAhorro.Categoria = GeneralBB.CATEGORIA_CA_USD_BINICIA;
		
		try {
			return ApiVentas.solicitudCajaAhorroStand(contexto, idSolicitud, nuevaCajaAhorro).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.getMessage());
			return null;
		}
	}

	public static SolicitudCajaAhorro actualizarCajaAhorro(ContextoBB contexto, String idSolicitud, SolicitudCajaAhorro sca) {
		if (sca == null)
			return null;

		if (empty(sca.Id))
			return null;

		String idCajaAhorro = sca.Id;

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		CuentaLegal cuentaLegal = new CuentaLegal();
		cuentaLegal.Uso = GeneralBB.USO_CUENTA_LEGAL;
		cuentaLegal.RealizaTransferencias = GeneralBB.REALIZA_TRANSF_CUENTA_LEGAL;

		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		sca.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_ARS;
		sca.DomicilioResumen = domicilioResumen;
		sca.CuentaLegales = cuentaLegal;
		sca.Oficial = GeneralBB.OFICIAL_CA_ARS_STAND;
		sca.Oficina = GeneralBB.OFICINA_CA_ARS_STAND;
		sca.Origen = GeneralBB.ORIGEN_CA_ARS_STAND;
		sca.UsoFirma = GeneralBB.USO_FIRMA_CA_ARS;
		sca.Ciclo = GeneralBB.CICLO_CA_ARS_STAND;
		sca.ResumenMagnetico = GeneralBB.RESUMEN_MAGNETICO_CA_ARS_STAND;
		sca.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_ARS_STAND;
		sca.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_ARS_STAND;
		if (GeneralBB.CATEGORIA_CGU.equals(sca.Categoria))
			sca.Subtipo = GeneralBB.SUBTIPO_CGU;

		try {
			return ApiVentas.actualizarCajaAhorro(contexto, idSolicitud, idCajaAhorro, sca).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_NO_ACTUALIZADA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_NO_ACTUALIZADA, e.getMessage());
			return null;
		}
	}

	public static SolicitudCajaAhorro cajaAhorroV2(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		CuentaLegal cuentaLegal = new CuentaLegal();
		cuentaLegal.Uso = GeneralBB.USO_CUENTA_LEGAL;
		cuentaLegal.RealizaTransferencias = GeneralBB.REALIZA_TRANSF_CUENTA_LEGAL;

		SolicitudCajaAhorro sca = new SolicitudCajaAhorro();
		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD;
		sca.Moneda = Moneda.PESOS;
		sca.Integrantes = integrantes;

		if (sesion.esDeLaOferta(GeneralBB.BUHO_CGU)) {
			sca.Categoria = GeneralBB.CATEGORIA_CGU;
			sca.Subtipo = GeneralBB.SUBTIPO_CGU;
		} else {
			sca.Categoria = GeneralBB.CATEGORIA_CA_ARS;
		}

		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		sca.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_ARS;
		sca.DomicilioResumen = domicilioResumen;
		sca.CuentaLegales = cuentaLegal;
		sca.Oficial = GeneralBB.OFICIAL_CA_ARS_STAND;
		sca.Oficina = GeneralBB.OFICINA_CA_ARS_STAND;
		sca.Origen = GeneralBB.ORIGEN_CA_ARS_STAND;
		sca.UsoFirma = GeneralBB.USO_FIRMA_CA_ARS;
		sca.Ciclo = GeneralBB.CICLO_CA_ARS_STAND;
		sca.ResumenMagnetico = GeneralBB.RESUMEN_MAGNETICO_CA_ARS_STAND;
		sca.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_ARS_STAND;
		sca.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_ARS_STAND;
		if (GeneralBB.CATEGORIA_CGU.equals(sca.Categoria))
			sca.Subtipo = GeneralBB.SUBTIPO_CGU;

		try {
			return ApiVentas.solicitudCajaAhorroStandV2(contexto, idSolicitud, sca).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.getMessage());
			return null;
		}
	}

	public static SolicitudCajaAhorro actualizarCajaAhorroUsd(ContextoBB contexto, String idSolicitud, SolicitudCajaAhorro sca) {
		if (sca == null)
			return null;

		if (empty(sca.Id))
			return null;

		String idCajaAhorro = sca.Id;

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		CuentaLegal cuentaLegal = new CuentaLegal();
		cuentaLegal.Uso = GeneralBB.USO_CUENTA_LEGAL;
		cuentaLegal.RealizaTransferencias = GeneralBB.REALIZA_TRANSF_CUENTA_LEGAL;

		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		sca.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_USD;
		sca.DomicilioResumen = domicilioResumen;
		sca.CuentaLegales = cuentaLegal;
		sca.Oficial = GeneralBB.OFICIAL_CA_USD;
		sca.Oficina = GeneralBB.OFICINA_CA_USD;
		sca.Origen = GeneralBB.ORIGEN_CA_USD;
		sca.UsoFirma = GeneralBB.USO_FIRMA_CA_ARS;
		sca.Ciclo = GeneralBB.CICLO_CA_USD;
		sca.ResumenMagnetico = GeneralBB.RESUMEN_MAGNETICO_CA_ARS_STAND;
		sca.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_USD;
		sca.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_USD;
		if (sca.Categoria.equals(GeneralBB.CATEGORIA_CGU))
			sca.Subtipo = GeneralBB.SUBTIPO_CGU;

		try {
			return ApiVentas.actualizarCajaAhorro(contexto, idSolicitud, idCajaAhorro, sca).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_NO_ACTUALIZADA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_NO_ACTUALIZADA, e.getMessage());
			return null;
		}
	}

	public static SolicitudCajaAhorro cajaAhorroUsdV2(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		CuentaLegal cuentaLegal = new CuentaLegal();
		cuentaLegal.Uso = GeneralBB.USO_CUENTA_LEGAL;
		cuentaLegal.RealizaTransferencias = GeneralBB.REALIZA_TRANSF_CUENTA_LEGAL;

		SolicitudCajaAhorro sca = new SolicitudCajaAhorro();
		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD;
		sca.Moneda = Moneda.DOLARES;
		sca.Integrantes = integrantes;
		sca.Categoria = GeneralBB.CATEGORIA_CA_USD_BINICIA;
		sca.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		sca.ProductoBancario = GeneralBB.PRODUCTO_BANCARIO_CA_USD;
		sca.DomicilioResumen = domicilioResumen;
		sca.CuentaLegales = cuentaLegal;
		sca.Oficial = GeneralBB.OFICIAL_CA_USD;
		sca.Oficina = GeneralBB.OFICINA_CA_USD;
		sca.Origen = GeneralBB.ORIGEN_CA_USD;
		sca.UsoFirma = GeneralBB.USO_FIRMA_CA_ARS;
		sca.Ciclo = GeneralBB.CICLO_CA_USD;
		sca.ResumenMagnetico = GeneralBB.RESUMEN_MAGNETICO_CA_ARS_STAND;
		sca.TransfiereAcredHab = GeneralBB.TRANSFIERE_ACRED_HAB_CA_USD;
		sca.CobroPrimerMantenimiento = GeneralBB.COBRO_PRIMER_MANT_CA_USD;
		if (sca.Categoria.equals(GeneralBB.CATEGORIA_CGU))
			sca.Subtipo = GeneralBB.SUBTIPO_CGU;

		try {
			return ApiVentas.solicitudCajaAhorroStandV2(contexto, idSolicitud, sca).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.CAJA_AHORRO_VACIA, e.getMessage());
			return null;
		}
	}

	public static SolicitudTarjetaDebito solicitarTarjetaDebitoStandalone(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		NuevaSolicitudTarjetaDebito nuevaTarjetaDebito = new NuevaSolicitudTarjetaDebito();
		nuevaTarjetaDebito.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_OPORTUNIDAD;
		nuevaTarjetaDebito.Integrantes = integrantes;
		nuevaTarjetaDebito.EsVirtual = sesion.esTdVirtual();
		nuevaTarjetaDebito.VisualizaVirtual = nuevaTarjetaDebito.EsVirtual ? "S" : "N";
		nuevaTarjetaDebito.RequiereEmbozado = sesion.esTdFisica() ? "S": "N";

		try {
			return ApiVentas.solicitudTarjetaDebitoStand(contexto, idSolicitud, nuevaTarjetaDebito).get();
		} catch (ApiException e) {

			if (isTimeOut(e.response.body.toString())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_VACIA, e.response.body.toString());
			return null;
		} catch (Exception e) {

			if (isTimeOut(e.getMessage())) {
				contexto.sesion().actualizarEstado(EstadosBB.ERROR_FUERA_DE_SERVICIO);
			}

			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_VACIA, e.getMessage());
			return null;
		}
	}

	public static SolicitudTarjetaDebito actualizarTarjetaDebito(ContextoBB contexto, String idSolicitud, SolicitudTarjetaDebito tarjetaDebito) {
		if (tarjetaDebito == null)
			return null;

		if (empty(tarjetaDebito.Id))
			return null;

		SesionBB sesion = contexto.sesion();

		String idTarjetaDebito = tarjetaDebito.Id;

		List<CuentasOperativas> cuentasOperativas = new ArrayList<>();
		CuentasOperativas cuentaOperativa = new CuentasOperativas();
		cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
		cuentaOperativa.Cuenta = GeneralBB.CUENTA_CUENTA_OP_TD;
		cuentaOperativa.Moneda = Moneda.PESOS;
		cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
		cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
		cuentasOperativas.add(cuentaOperativa);
		
		if(sesion.buhoInversorAceptada()) {
			CuentasOperativas cuentaOperativaDolares = new CuentasOperativas();
			cuentaOperativaDolares.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
			cuentaOperativaDolares.Cuenta = GeneralBB.CUENTA_CUENTA_OP_TD;
			cuentaOperativaDolares.Moneda = Moneda.DOLARES;
			cuentaOperativaDolares.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
			cuentaOperativaDolares.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
			cuentasOperativas.add(cuentaOperativaDolares);
		}

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		tarjetaDebito.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		tarjetaDebito.TarjetaDebitoCuentasOperativas = cuentasOperativas;
		tarjetaDebito.Tipo = GeneralBB.TIPO_TD;
		tarjetaDebito.Grupo = GeneralBB.GRUPO_TD;
		tarjetaDebito.TipoCuentaComision = GeneralBB.TIPO_CUENTA_COMISION_TD;
		tarjetaDebito.NumeroCtaComision = GeneralBB.NUMERO_CTA_COMISION_TD;
		tarjetaDebito.Domicilio = domicilioResumen;
		tarjetaDebito.EsVirtual = sesion.esTdVirtual();
		tarjetaDebito.VisualizaVirtual = tarjetaDebito.EsVirtual ? "S" : "N";
		tarjetaDebito.RequiereEmbozado = sesion.esTdFisica() ? "S": "N";

		try {
			return ApiVentas.actualizarTarjetaDebito(contexto, idSolicitud, idTarjetaDebito, tarjetaDebito).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_NO_ACTUALIZADA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_NO_ACTUALIZADA, e.getMessage());
			return null;
		}
	}

	public static SolicitudTarjetaDebito tarjetaDebitoV2(ContextoBB contexto, String idSolicitud) {
		SesionBB sesion = contexto.sesion();
		String cuil = sesion.cuil;
		String idCobis = sesion.idCobis;

		RolIntegrante rolIntegrante = new RolIntegrante();
		rolIntegrante.Rol = GeneralBB.ROL_INTEGRANTE_TITULAR;
		rolIntegrante.NumeroDocumentoTributario = cuil;
		rolIntegrante.IdCobis = sesion.cobisPositivo() ? idCobis : null;

		List<RolIntegrante> integrantes = new ArrayList<RolIntegrante>();
		integrantes.add(rolIntegrante);

		List<CuentasOperativas> cuentasOperativas = new ArrayList<>();
		CuentasOperativas cuentaOperativa = new CuentasOperativas();
		cuentaOperativa.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
		cuentaOperativa.Cuenta = GeneralBB.CUENTA_CUENTA_OP_TD;
		cuentaOperativa.Moneda = Moneda.PESOS;
		cuentaOperativa.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
		cuentaOperativa.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
		cuentasOperativas.add(cuentaOperativa);

		if(sesion.buhoInversorAceptada()) {
			CuentasOperativas cuentaOperativaDolares = new CuentasOperativas();
			cuentaOperativaDolares.Producto = GeneralBB.PRODUCTO_CUENTA_OP_TD;
			cuentaOperativaDolares.Cuenta = GeneralBB.CUENTA_CUENTA_OP_TD;
			cuentaOperativaDolares.Moneda = Moneda.DOLARES;
			cuentaOperativaDolares.Principal = GeneralBB.PRINCIPAL_CUENTA_OP_TD;
			cuentaOperativaDolares.Firma = GeneralBB.FIRMA_CUENTA_OP_TD;
			cuentasOperativas.add(cuentaOperativaDolares);
		}

		DomicilioResumen domicilioResumen = new DomicilioResumen();
		domicilioResumen.Tipo = GeneralBB.TIPO_DOMICILIO_RESUMEN;

		SolicitudTarjetaDebito tarjetaDebito = new SolicitudTarjetaDebito();
		tarjetaDebito.Integrantes = integrantes;
		tarjetaDebito.TipoOperacion = SolicitudPaquete.TIPO_OPERACION_SOLICITUD;
		tarjetaDebito.TarjetaDebitoCuentasOperativas = cuentasOperativas;
		tarjetaDebito.Tipo = GeneralBB.TIPO_TD;
		tarjetaDebito.Grupo = GeneralBB.GRUPO_TD;
		tarjetaDebito.TipoCuentaComision = GeneralBB.TIPO_CUENTA_COMISION_TD;
		tarjetaDebito.NumeroCtaComision = GeneralBB.NUMERO_CTA_COMISION_TD;
		tarjetaDebito.Domicilio = domicilioResumen;
		tarjetaDebito.EsVirtual = sesion.esTdVirtual();
		tarjetaDebito.VisualizaVirtual = tarjetaDebito.EsVirtual ? "S" : "N";
		tarjetaDebito.RequiereEmbozado = sesion.esTdFisica() ? "S": "N";

		try {
			return ApiVentas.solicitudTarjetaDebitoStandV2(contexto, idSolicitud, tarjetaDebito).get();
		} catch (ApiException e) {
			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_NO_ACTUALIZADA, e.response.body.toString());
			return null;
		} catch (Exception e) {
			LogBB.error(contexto, ErroresBB.TARJETA_DEBITO_NO_ACTUALIZADA, e.getMessage());
			return null;
		}
	}

}
