package com.matco.backlogs.controller;

import java.io.IOException;
import java.util.List;

import javax.faces.FactoryFinder;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.context.FacesContextFactory;
import javax.faces.lifecycle.Lifecycle;
import javax.faces.lifecycle.LifecycleFactory;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.matco.controlaccesos.service.Exception_Exception;
import com.matco.controlaccesos.service.Usuario;
import com.matco.backlogs.bean.LoginBean;

/**
 * Filtro que se encarga de administrar la solicitud de recursos. Para acceder a
 * un recurso es necesario tener credenciales válidas con el permiso requerido.
 * 
 * @author JRojas
 *
 */
public class FiltroSeguridad implements Filter {

	private static final Logger log = Logger.getLogger(FiltroSeguridad.class);
	private ServletContext servletContext;
	private HttpServletRequest req;
	private HttpServletResponse res;

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		this.req = (HttpServletRequest) request;
		this.res = (HttpServletResponse) response;
		HttpSession sesion = req.getSession();
		Usuario usuario = obtenerUsuarioEnSesion(sesion);
		String rol = null;
		boolean esRolValido = false;

		if (usuario != null) {
			rol = usuario.getRol().getIdRolUsuario();
			esRolValido = esRolValido(rol, sesion);
		}
	
		if (esRolValido) {
			chain.doFilter(request, response);
		} else {
			if (rol != null) {
				String mensajeError = "El usuario: '" + usuario.getUsuario() + "' tiene asignado el rol: '" + rol
						+ "', el rol no pertenece al sistema";
				log.error(mensajeError);
			}

			if (isAjax(req)) {
				servletContext.getRequestDispatcher("/faces/login/login.xhtml").forward(req, res);
			} else {
				res.sendRedirect(req.getContextPath());
			}
			sesion.invalidate();
		}
	}

	/**
	 * Valida si es un rol permitido
	 * 
	 * @param rolUsuario Rol de usuario
	 * @param sesion     Sesión activa
	 * @return Indica si es válido o no un usuario
	 */
	private boolean esRolValido(String rolUsuario, HttpSession sesion) {
		boolean esRolValido = false;

		if (rolUsuario == null) {
			return esRolValido;
		}

		LoginBean loginBean = (LoginBean) obtenerVariableSesion("loginBean", sesion);

		if (loginBean != null) {
			String sistema = loginBean.getSistema();
			String modulo = loginBean.getModulo();
			List<String> rolesPermitidos = null;
			try {
				rolesPermitidos = loginBean.getControlAccesosPort().obtenerRolesPermitidosPorSistemaModulo(sistema,
						modulo);
			} catch (Exception_Exception e) {
				log.error(e);
			}
			// TODO: Cambiar esta parte - Se pone comentario al siguiente for para su
			// funcionamiento fuera de la empresa

			for (String rol : rolesPermitidos) {
				if (rolUsuario.equals(rol)) {
					esRolValido = true;
					break;
				}
			}

			// Se establece a true la siguiente variable para su funcionamiento fuera de la
			// empresa
			// TODO esto se cambia.
			// esRolValido = true;
		}

		return esRolValido;
	}

	/**
	 * Obtiene el usuario que se encuentra actualmente en sesión.
	 * 
	 * @param sesion {@link HttpSession} Sesión de donde se obtienen los datos
	 * @return {@link Usuario} Usuario en la sesión actual
	 */
	private Usuario obtenerUsuarioEnSesion(HttpSession sesion) {
		Usuario usuario = obtenerAccesos(sesion);
		if (usuario == null) {
			return null;
		}

		return usuario;
	}

	/**
	 * Obtiene el conjunto de accesos que el usuario actual tiene en el sistema.
	 * 
	 * @param sesion {@link HttpSession} Sesión de donde se obtienen los datos
	 * @return {@link Usuario} Datos del usuario
	 */
	private Usuario obtenerAccesos(HttpSession sesion) {
		LoginBean loginBean = (LoginBean) obtenerVariableSesion("loginBean", sesion);
		if (loginBean == null) {
			return null;
		}

		Usuario usuario = loginBean.getUsuario();
		return usuario;
	}

	/**
	 * Recupera un ManagedBean de cualquier Scope por medio de una Expression
	 * Language. En caso de no haber una instancia inicializada, crea una nueva. Si
	 * no se encuentra un ManagedBean registrado con el nombre proporcionado el
	 * resultado es null.
	 * 
	 * @param beanName Nombre con el que está registrado el ManagedBean
	 * @return Instancia del ManagedBean recuperada o creada
	 */

	@SuppressWarnings("unchecked")
	protected <T> T obtenerBean(String beanName) {
		FacesContext context = getFacesContext();
		String expression = "#{" + beanName + "}";
		return (T) context.getApplication().evaluateExpressionGet(context, expression, Object.class);
	}

	/**
	 * Crea un FacesContext generado con una configuración por defecto
	 * 
	 * @return Una instancia de un FacesContext
	 */
	private FacesContext getFacesContext() {
		FacesContext facesContext = FacesContext.getCurrentInstance();

		if (facesContext == null) {

			FacesContextFactory contextFactory = (FacesContextFactory) FactoryFinder
					.getFactory(FactoryFinder.FACES_CONTEXT_FACTORY);
			LifecycleFactory lifecycleFactory = (LifecycleFactory) FactoryFinder
					.getFactory(FactoryFinder.LIFECYCLE_FACTORY);
			Lifecycle lifecycle = lifecycleFactory.getLifecycle(LifecycleFactory.DEFAULT_LIFECYCLE);

			facesContext = contextFactory.getFacesContext(req.getSession().getServletContext(), req, res, lifecycle);

			// Set using our inner class
			InnerFacesContext.setFacesContextAsCurrentInstance(facesContext);

			// set a new viewRoot, otherwise context.getViewRoot returns null
			UIViewRoot view = facesContext.getApplication().getViewHandler().createView(facesContext, "");
			facesContext.setViewRoot(view);
		}

		return facesContext;
	}

	/**
	 * Obtiene una variable de la sesión actual.
	 * 
	 * @param key    {@link String} Nombre de la variable que se busca en sesión
	 * @param sesion {@link HttpSession} Sesión de donde se obtienen los datos
	 * @return {@link Object} Variable de sesión
	 */
	private Object obtenerVariableSesion(String key, HttpSession sesion) {
		if (sesion == null) {
			return null;
		}
		Object value = sesion.getAttribute(key);
		return value;
	}

	/**
	 * You need an inner class to be able to call FacesContext.setCurrentInstance
	 * since it's a protected method
	 * 
	 * @author MAHurtado
	 *
	 */
	private abstract static class InnerFacesContext extends FacesContext {
		protected static void setFacesContextAsCurrentInstance(FacesContext facesContext) {
			FacesContext.setCurrentInstance(facesContext);
		}
	}

	@Override
	public void init(FilterConfig configuracion) throws ServletException {
		servletContext = configuracion.getServletContext();
	}

	/**
	 * Determina si una petición es de tipo AJAX.
	 * 
	 * @param request {@link HttpServletRequest} Petición de donde se obtienen los
	 *                datos
	 * @return <b>true</b> si la petición se considera AJAX y <b>false</b> en caso
	 *         contrario
	 */
	private boolean isAjax(HttpServletRequest request) {
		return "XMLHttpRequest".equals(request.getHeader("X-Requested-With"));
	}
}
