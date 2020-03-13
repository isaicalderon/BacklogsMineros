package com.matco.backlogs.bean;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.servlet.http.HttpServletRequest;
import org.apache.log4j.Logger;
import com.matco.controlaccesos.service.ControlAccesosEndpoint;
import com.matco.controlaccesos.service.ControlAccesosService;
import com.matco.controlaccesos.service.Exception_Exception;
import com.matco.controlaccesos.service.Usuario;

/**
 * En esta clase se gestiona la autenticación y permisos del usuario
 *
 */
@ManagedBean(name = "loginBean")
@SessionScoped
public class LoginBean extends GenericBean implements Serializable {
	private static final long serialVersionUID = -7468708218722517917L;
	private static final Logger log = Logger.getLogger(LoginBean.class);
	private static final DecimalFormat numeroDosDigitosFormat = new DecimalFormat("00");

	private String imgLogin;
	private String leyendaUsuario;
	private String leyendaLogOut;
	private String username;
	private String password;
	private String rol;
	private String sistema;
	private String modulo;
	private Usuario usuario;
	private ControlAccesosEndpoint controlAccesosPort;
	private List<String> rolesPermitidos;
	private String nombreUsuario;
	private String rolOracion;
	
	Permisos permisos = new Permisos();
	
	@PostConstruct
	public void init() {
		String urlServidor = getExternalContext().getInitParameter("controlaccesos_wshost");
		String puertoServidor = getExternalContext().getInitParameter("controlaccesos_wsport");
		String servidor = "http://" + urlServidor + ":" + puertoServidor
				+ "/controlaccesosservices/controlaccesos?wsdl";
		URL url = null;
		
		try {
			url = new URL(servidor);
		} catch (MalformedURLException e) {
			String mensajeError = "URL mal formada para acceder al servicio de control de accesos";
			log.error(mensajeError, e);
		}

		ControlAccesosService controlAccesos = new ControlAccesosService(url);
		controlAccesosPort = controlAccesos.getControlAccesosServicePort();

		sistema = getExternalContext().getInitParameter("sistema");
		modulo = getExternalContext().getInitParameter("modulo");

		BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		rolesPermitidos = new ArrayList<>();
		obtenerRolesPermitidos();
		seleccionBean.setRoles(rolesPermitidos);
	}

	/**
	 * Valida los datos del usuario y si tiene acceso al sistema regresa un
	 * <b>outcome</b> para redireccionar a la página principal. Si los datos
	 * ingresados no corresponden a un usuario válido, se vuelve a solicitar el
	 * usuario y contraseña.
	 * 
	 * @return {@link String} <b>outcome</b> para una regla de navegación
	 */
	public String login() {
		if (tienePermisoUsuario()) {
			nombreUsuario = obtenerNombreUsuarioActual();
			this.leyendaUsuario = nombreUsuario + " (" + rol + "), Bienvenido al Sistema ";
			this.leyendaLogOut = "[Cerrar sesión]";
			String mensaje = "El usuario '" + getUsername() + "', con el rol "+rol+", ha iniciado sesión";
			log.info(mensaje);
			return "menuBacklogs";
		} else {
			return "login";
		}
	}

	/**
	 * Termina la sesión del usuario y regresa un <b>outcome</b> para redireccionar
	 * a la página de login.
	 * 
	 * @return {@link String} <b>outcome</b> para una regla de navegación
	 */
	public String logout() {
		String mensaje = "El usuario '" + getUsername() + "' ha cerrado sesión";
		log.info(mensaje);
		getRequest().getSession().invalidate();
		return "logout";
	}

	/**
	 * Determina si el usuario que intenta iniciar sesión cuenta con permisos
	 * suficientes para ingresar al sistema.
	 * 
	 * @return <b>true</b> si el usuario tiene acceso al sistema y <b>false</b> en
	 *         caso contrario
	 */
	private boolean tienePermisoUsuario() {

		boolean existeUsuario = false;
		try {
			existeUsuario = controlAccesosPort.esUsuarioContrasenaValido(username, password);

			if (existeUsuario) {

				// Set request context property.
				java.util.Map<String, Object> requestContext = ((javax.xml.ws.BindingProvider) controlAccesosPort)
						.getRequestContext();
				requestContext.put("com.sun.xml.internal.ws.request.timeout", new Long(600000));

				usuario = controlAccesosPort.obtenerDatosUsuario(username, password, sistema, modulo);
				rol = usuario.getRol().getIdRolUsuario();
				// TODO: CAMBIAR ESTA PARTE - Esta parte es para probar fuera de la empresa
				// rol = "SUPERVISOR_BACKLOGS_CAN";  

				boolean permitido = (rol != null);
				if (!permitido) {
					agregarMensajeError("No tiene permisos para ingresar");
				} else {
					int longitudRol = rol.length();
					String letraInicial = rol.substring(0, 1);
					String textoDepuesLetraInicial = rol.substring(1, longitudRol);
					setRolOracion(letraInicial.toUpperCase() + textoDepuesLetraInicial.toLowerCase());
				}
				 //return true;
			return permitido;
			} else {
				agregarMensajeError("Usuario o contraseña no válidos");
			}
		} catch (Exception_Exception e) {
			String mensajeError = e.getMessage();
			getFlash().put("mensajeError", mensajeError);
		}
		 //return true;
		return existeUsuario;
	}

	/**
	 * Return the environment-specific object instance for the current request.
	 * 
	 * @return {@link HttpServletRequest} Solicitud actual
	 */
	private HttpServletRequest getRequest() {
		HttpServletRequest request = (HttpServletRequest) getExternalContext().getRequest();
		return request;
	}

	/**
	 * Obtiene los roles que tienen acceso al sistema.
	 * 
	 * @return Roles permitidos
	 */
	private List<String> obtenerRolesPermitidos() {

		List<String> roles = obtenerRolesSistemaModulo();
		return roles;
	}

	private List<String> obtenerRolesSistemaModulo() {

		List<String> rolesPermitidos = new ArrayList<>();

		try {
			rolesPermitidos = controlAccesosPort.obtenerRolesPermitidosPorSistemaModulo(sistema, modulo);
		} catch (Exception_Exception e) {
			String mensajeError = e.getMessage();
			getFlash().put("mensajeError", mensajeError);
		}

		return rolesPermitidos;
	}

	/**
	 * Obtiene de forma aleatoria el nombre de una IMAGEN para mostrarla como fondo
	 * de pantalla en la página de login. Las imágenes tienen un nombre conformado
	 * por la palabra <b>fondo</b>, un número del <b>01</b> al <b>12</b> y la
	 * extensión <b>jpg</b>. Ejemplo:
	 * 
	 * <pre>
	 * fondo01.jpg, fondo06.jpg, fondo12.jpg
	 * </pre>
	 * 
	 * @return {@link String} nombre de la IMAGEN que se mostrará como fondo
	 */
	public String getImgLogin() {
		Random aleatorio = new Random();
		int numero = aleatorio.nextInt(12) + 1;
		this.imgLogin = "fondo" + numeroDosDigitosFormat.format(numero) + ".jpg";
		return imgLogin;
	}

	/***
	 * conocer si un usuario tiene un permiso
	 * 
	 * @param permiso Permiso solicititado
	 * @return
	 */
	public boolean tienePermiso(String permiso) {
		boolean permitido = false;
		//rol = "ADMINISTRADOR";
		
		if (rol == null) {
			redireccionar("login");
			return false;
		}
		
		if (permisos == null) {
			permisos = new Permisos();
		}
		List<String> permisosList = permisos.obtenerPermisos(rol);
		permitido = permisosList.contains(permiso);
		return permitido;
	}
	
	public void setImgLogin(String imgLogin) {
		this.imgLogin = imgLogin;
	}

	public String getLeyendaUsuario() {
		return leyendaUsuario;
	}

	public void setLeyendaUsuario(String leyendaUsuario) {
		this.leyendaUsuario = leyendaUsuario;
	}

	public String getLeyendaLogOut() {
		return leyendaLogOut;
	}

	public void setLeyendaLogOut(String leyendaLogOut) {
		this.leyendaLogOut = leyendaLogOut;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public List<String> getRolesPermitidos() {
		return rolesPermitidos;
	}

	public void setRolesPermitidos(List<String> rolesPermitidos) {
		this.rolesPermitidos = rolesPermitidos;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public ControlAccesosEndpoint getControlAccesosPort() {
		return controlAccesosPort;
	}

	public void setControlAccesosPort(ControlAccesosEndpoint controlAccesosPort) {
		this.controlAccesosPort = controlAccesosPort;
	}

	public String getNombreUsuario() {
		return nombreUsuario;
	}

	public void setNombreUsuario(String nombreUsuario) {
		this.nombreUsuario = nombreUsuario;
	}

	public String getRolOracion() {
		return rolOracion;
	}

	public void setRolOracion(String rolOracion) {
		this.rolOracion = rolOracion;
	}

}