package ar.com.hipotecario.backend.util;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import ar.com.hipotecario.backend.Config;
import ar.com.hipotecario.backend.Contexto;
import ar.com.hipotecario.backend.base.Objeto;
import ar.com.hipotecario.canal.rewards.modulos.login.models.LoginLdapResponse;

public class LoginLDAP {

	private static Config config = new Config();

	public static Boolean loginLDAP(String usuario, String clave) {
		Hashtable<String, String> env = new Hashtable<String, String>();
		env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		env.put(Context.PROVIDER_URL, config.string("ldap_url"));
		env.put(Context.SECURITY_AUTHENTICATION, "simple");
		env.put(Context.SECURITY_PRINCIPAL, "CASA_CENTRAL\\" + usuario);
		env.put(Context.SECURITY_CREDENTIALS, clave);
		try {
			new InitialDirContext(env);
			return true;
		} catch (NamingException e) {
			System.out.println(e);
			return false;
		}
	}

	// public static Objeto loginLdpaGeneral(Contexto contexto, String usuario,
	// String contraseña) {
	// // veri si esta implementacion tiene cosas especificas de buhobank
	// return loginLdpaBB(contexto, usuario, contraseña);
	// }

	public static LoginLdapResponse loginLdpaRW(Contexto contexto, String usuario, String contraseña) {
		String dominio = config.string("ldap_dominio");
		String searchBase = config.string("ldap_seach_base");

		try {
			// Crea el contexto LDAP con las credenciales de autenticación
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, dominio);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, usuario + "@BH.COM.AR");
			env.put(Context.SECURITY_CREDENTIALS, contraseña);
			// Objeto respuesta = new Objeto();

			// Realiza la autenticación y establece la conexión LDAP
			DirContext ctx = new InitialDirContext(env);

			// Realiza la búsqueda del usuario
			String filtro = "(&(objectCategory=person)(objectClass=user)(samaccountname=" + usuario + "))";
			String[] atributos = { "displayname", "mail", "samaccountname", "memberOf", "distinguishedName", "OU" };
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(atributos);

			NamingEnumeration<SearchResult> results = ctx.search(searchBase, filtro, controls);

			LoginLdapResponse respuestaLdap = new LoginLdapResponse();
			while (results.hasMore()) {
				SearchResult result = results.next();

				// Obtiene los atributos del usuario
				Attributes attrs = result.getAttributes();

				if (autenticarUsuario(attrs)) {
					respuestaLdap.setAutenticado(true);
					// respuesta.set("autenticado", true);
				} else {
					respuestaLdap.setAutenticado(false);
					// respuesta.set("autenticado", false);
					ctx.close();
					return respuestaLdap;
				}

				Attribute displayNameAttr = attrs.get("displayname");
				Attribute samAccountNameAttr = attrs.get("samaccountname");
				Attribute atributosDeMiembros = attrs.get("memberof");

				// Obtiene los valores de los atributos del usuario
				String displayName = displayNameAttr != null ? displayNameAttr.get().toString() : "";
				String samAccountName = samAccountNameAttr != null ? samAccountNameAttr.get().toString() : "";

				respuestaLdap.setNombre(displayName);
				// respuesta.set("nombre", displayName);
				// respuesta.set("usuario", samAccountName);
				respuestaLdap.setUsuario(samAccountName);
				List<String> roles = obtenerRoles(atributosDeMiembros);

				// String permisos = obtenerPermisosRmkBB(contexto, roles);
				// respuesta.set("roles", roles);
				respuestaLdap.setRoles(roles);
				break;
			}
			// Cierra el contexto LDAP
			ctx.close();
			// return respuesta;
			return respuestaLdap;
		} catch (NamingException e) {
			// Objeto respuesta = new Objeto();
			LoginLdapResponse respuestaLdapError = new LoginLdapResponse();
			e.printStackTrace();
			// respuesta.set("autenticado", false);
			respuestaLdapError.setAutenticado(false);
			return respuestaLdapError;
			// return respuesta;
		}

	}

	public static Objeto loginLdpaBB(Contexto contexto, String usuario, String contraseña) {
		String dominio = config.string("ldap_dominio");
		String searchBase = config.string("ldap_seach_base");

		try {
			// Crea el contexto LDAP con las credenciales de autenticación
			Hashtable<String, String> env = new Hashtable<>();
			env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
			env.put(Context.PROVIDER_URL, dominio);
			env.put(Context.SECURITY_AUTHENTICATION, "simple");
			env.put(Context.SECURITY_PRINCIPAL, usuario + "@BH.COM.AR");
			env.put(Context.SECURITY_CREDENTIALS, contraseña);
			Objeto respuesta = new Objeto();

			// Realiza la autenticación y establece la conexión LDAP
			DirContext ctx = new InitialDirContext(env);

			// Realiza la búsqueda del usuario
			String filtro = "(&(objectCategory=person)(objectClass=user)(samaccountname=" + usuario + "))";
			String[] atributos = { "displayname", "mail", "samaccountname", "memberOf", "distinguishedName", "OU" };
			SearchControls controls = new SearchControls();
			controls.setSearchScope(SearchControls.SUBTREE_SCOPE);
			controls.setReturningAttributes(atributos);

			NamingEnumeration<SearchResult> results = ctx.search(searchBase, filtro, controls);

			while (results.hasMore()) {
				SearchResult result = results.next();

				// Obtiene los atributos del usuario
				Attributes attrs = result.getAttributes();

				if (autenticarUsuario(attrs)) {
					respuesta.set("autenticado", true);
				} else {
					respuesta.set("autenticado", false);
					ctx.close();
					return respuesta;
				}

				Attribute displayNameAttr = attrs.get("displayname");
				Attribute samAccountNameAttr = attrs.get("samaccountname");
				Attribute atributosDeMiembros = attrs.get("memberof");

				// Obtiene los valores de los atributos del usuario
				String displayName = displayNameAttr != null ? displayNameAttr.get().toString() : "";
				String samAccountName = samAccountNameAttr != null ? samAccountNameAttr.get().toString() : "";

				respuesta.set("nombre", displayName);
				respuesta.set("usuario", samAccountName);

				List<String> roles = obtenerRoles(atributosDeMiembros);

				String permisos = obtenerPermisosRmkBB(contexto, roles);
				respuesta.set("permisos", permisos);

				break;
			}
			// Cierra el contexto LDAP
			ctx.close();
			return respuesta;
		} catch (NamingException e) {
			Objeto respuesta = new Objeto();
			e.printStackTrace();
			respuesta.set("autenticado", false);
			return respuesta;
		}

	}

	public static Boolean autenticarUsuario(Attributes atributos) {

		if (atributos == null || atributos.size() == 0) {
			return false;
		}
		return true;
	}

	public static List<String> obtenerRoles(Attribute atributosDeMiembros) {

		NamingEnumeration<?> valores;
		try {
			valores = atributosDeMiembros.getAll();
			List<String> roles = new ArrayList<>();

			while (valores.hasMore()) {
				Object valor = valores.next();

				if (valor instanceof String) {
					String strValor = (String) valor;
					int comaIndex = strValor.indexOf(",");
					// Obtener unicamente el rol
					if (strValor.startsWith("CN=")) {
						String rol = strValor.substring(3, comaIndex);
						roles.add(rol);
					}
				}
			}

			return roles;
		} catch (NamingException e) {
			e.printStackTrace();
			return null;
		}

	}

	public static String obtenerPermisosRmkBB(Contexto contexto, List<String> roles) {
		for (String rol : roles) {
			if (rol.contains("H_BB_DASH_")) {
				return rol;
			}
		}
		return null;
	}

}
