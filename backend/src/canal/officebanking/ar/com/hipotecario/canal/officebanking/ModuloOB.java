package ar.com.hipotecario.canal.officebanking;

import java.util.List;
import java.util.Optional;

import ar.com.hipotecario.backend.Modulo;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioEmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.ServicioUsuariosEmpresasActivoOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.EmpresaUsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuarioOB;
import ar.com.hipotecario.canal.officebanking.jpa.ob.modelo.UsuariosEmpresasActivoOB;

public class ModuloOB extends Modulo {

	/* ========== EXPRESIONES REGULARES ========== */
	public static Boolean usuarioClaveValidos(String usuario, String clave) {
		return usuarioValido(usuario) && claveValida(clave);
	}

	public static Boolean usuarioValido(String usuario) {
		String patron = "(?=.*^[a-zA-Z])(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
		return usuario.matches(patron);
	}

	public static Boolean claveValida(String clave) {
		String patron = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)[a-zA-Z\\d]{8,}$";
		return clave.matches(patron);
	}

	public static Boolean emailValido(String email) {
		return email != null && email.length() >= 4 && email.contains("@");
	}

	public static Boolean celularValido(String celular) {
		return celular != null && celular.length() >= 8;
	}

	/* ========== ENMASCARADO ========== */
	public static String emailEnmascarado(String email) {
		if (email != null) {
			return email.substring(0, 2) + "***" + email.substring(email.indexOf("@"));
		}
		return "";
	}

	public static String celularEnmascarado(String celular) {
		if (celular != null && celular.length() > 5) {
			return celular.substring(0, 2) + "***" + celular.substring(celular.length() - 2);
		}
		return "";
	}

	public static boolean cbuValido(String cbu) {
		try {
			String patron = "^[0-9]{22}$";
			return cbu.matches(patron);
		} catch (Exception e) {
			return false;
		}
	}

	public static boolean aliasValido(String alias) {
		try {
			String patron = "^[a-zA-Z0-9.]{6,20}$";
			return alias.matches(patron);
		} catch (Exception e) {
			return false;
		}
	}

	public static UsuarioOB usuario(ContextoOB contexto, Long numeroDocuemento) {
		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		return servicioUsuario.findByNumeroDocumento(numeroDocuemento).tryGet();
	}

	public static UsuarioOB usuario(ContextoOB contexto, Integer codigo) {
		ServicioUsuarioOB servicioUsuario = new ServicioUsuarioOB(contexto);
		return servicioUsuario.find(codigo).tryGet();
	}

	public static EmpresaUsuarioOB empresasUsuario(ContextoOB contexto, EmpresaOB empresa, UsuarioOB usuarioOB) {
		ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
		return servicioEmpresaUsuario.findByUsuarioEmpresa(usuarioOB, empresa).tryGet();
	}

	public static EmpresaUsuarioOB empresasUsuario(ContextoOB contexto, EmpresaOB empresa, UsuarioOB usuarioOB, Integer rolId) {
		ServicioEmpresaUsuarioOB servicioEmpresaUsuario = new ServicioEmpresaUsuarioOB(contexto);
		EmpresaUsuarioOB empresaUsuario = servicioEmpresaUsuario.findByUsuarioEmpresa(usuarioOB, empresa).tryGet();
		if (!empty(empresaUsuario) && empresaUsuario.rol.rol_codigo.intValue() == rolId.intValue()) {
			return empresaUsuario;
		}
		return null;
	}
	
	public static UsuariosEmpresasActivoOB empresasUsuarioActivo(ContextoOB contexto, UsuarioOB usuarioOB, EmpresaOB empresa) {
		ServicioUsuariosEmpresasActivoOB servicio = new ServicioUsuariosEmpresasActivoOB(contexto);
		UsuariosEmpresasActivoOB empresaUsuarioActivo = servicio.findByUsuarioEmpresaActivo(usuarioOB, empresa).tryGet();
		if (!empty(empresaUsuarioActivo)) {
			return empresaUsuarioActivo;
		}
		return null;
	}

	public static UsuarioOB operadorInicial(ContextoOB contexto) {
		ServicioEmpresaUsuarioOB servicio = new ServicioEmpresaUsuarioOB(contexto);
		List<EmpresaUsuarioOB> empresaUsuarioOB = servicio.findByEmpresa(contexto.sesion().empresaOB).tryGet();
		Optional<EmpresaUsuarioOB> operadorInicial = empresaUsuarioOB.stream().filter(e -> e.rol.rol_codigo.intValue() == 1).findFirst();
		if (operadorInicial.isPresent()) {
			return operadorInicial.get().usuario;
		}
		return null;
	}
	
	public static String ultimos4digitos(String numero) {
		String ultimos4digitos = "";
		if (numero != null && numero.length() >= 4) {
			ultimos4digitos = numero.substring(numero.length() - 4, numero.length());
		}
		return ultimos4digitos;
	}
}