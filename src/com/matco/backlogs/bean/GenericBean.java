package com.matco.backlogs.bean;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.faces.application.ConfigurableNavigationHandler;
import javax.faces.application.FacesMessage;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;

public abstract class GenericBean {
	protected static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	protected static final String ROL_CORPORATIVO = "CORPORATIVO";
	private static final Logger log = Logger.getLogger(GenericBean.class);
	public static final Integer IDCLIENTE = 60001;

	protected final String RUTA_PROPERTIES_AMCE3 = getExternalContext().getInitParameter("admintx_amce3");
	protected final String RUTA_PROPERTIES_SERVICIO = getExternalContext().getInitParameter("admintx_servicio");
	protected final String RUTA_PROPERTIES_MATCO = getExternalContext().getInitParameter("admintx_matco");
	protected final String RUTA_PROPERTIES_CATINSPECT = getExternalContext()
			.getInitParameter("admintx_interfacescatinspect");
	protected final String serverUrlServices = getExternalContext().getInitParameter("server_url_services");

	protected ExternalContext externalContext;
	protected FacesContext facesContext;

	@SuppressWarnings("unused")
	private Map<String, Object> requestMap;
	private Map<String, Object> sessionMap;
	private Flash flash;
	public String summary = "ADMINISTRACIÓN DE BACKLOGS";
	public String error;
	public String usuario;

	/**
	 * Resta el numero de meses a la fecha actual
	 * 
	 * @param int - Meses por restar
	 * @return Date - Fecha actual menos el numero de meses
	 */
	public Date restarMesesFecha(int meses) {
		meses = Math.abs(meses) * -1;
		Date fechaActual = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActual);
		calendar.add(Calendar.MONTH, meses);
		Date fechaFinal = calendar.getTime();
		return fechaFinal;
	}

	/**
	 * Regresa el año actual
	 * 
	 * @return
	 */
	public String calcularAnioActual() {
		Calendar cal = Calendar.getInstance();
		return String.valueOf(cal.get(Calendar.YEAR));
	}

	/***
	 * Sustituye las \\ por / en una URL
	 * 
	 * @param rutaImagen
	 * @return
	 */
	protected String construirURL(String rutaImagen) {
		char[] arreglo = rutaImagen.toCharArray();
		String url = "";
		for (char letra : arreglo) {
			if (letra == 92) {
				url += "/";
			} else {
				url += letra;
			}
		}
		return url;
	}

	/**
	 * Calcula y regresa el ROL logueado dependiendo a una cadena
	 * 
	 * @param LoginBean - loginBean
	 * @return CAB o CAN
	 */
	public String getLoginRol(LoginBean loginBean) {
		String returnRol = "";
		String rol = loginBean.getRol();
		String[] splitRol = rol.split("_");
		int lenRol = splitRol.length;
		if (lenRol > 1) {
			returnRol = splitRol[lenRol - 1];
		}
		return returnRol;
	}

	/**
	 * Obtiene el nombre completo del usuario que actualmente inicio sesion. <br>
	 * Se obtiene a partir de la instancia de LoginBean, siempre existe una
	 * instancia de LoginBean porque pasa por el filtro de seguridad asegurandose
	 * que exista una sesión valida
	 * 
	 * @return username
	 */
	protected String obtenerNombreUsuarioActual() {
		LoginBean loginBean = obtenerBean("loginBean");
		String nombreUsuario = loginBean.getUsuario().getNombreCompleto();
		return nombreUsuario;
	}

	/***
	 * Regresa la sucursal seleccionada en el filtro
	 * 
	 * @return sucursal seleccionada con el filtro
	 */
	public Short obtenerSucursalFiltro() {
		BacklogsStaticsVarBean seleccionBean = obtenerBean("backlogsStaticsVarBean");
		return seleccionBean.getSucursalFiltro();
	}

	/***
	 * obtiene la sucursal que le corresponde segun su rol
	 * 
	 * @return si el termina en CAN 6, si termina en CAB 13, si no null
	 */
	public Short obtenerSucursalCorrespondiente() {
		LoginBean loginBean = obtenerBean("loginBean");
		String rol = loginBean.getRol();
		Short sucursalActual = null;
		try {
			if (!rol.equals("ADMINISTRADOR")) {
				String suc = rol.substring(rol.length() - 3, rol.length());
				if (suc.equals("CAN")) {
					sucursalActual = 6;
				} else if (suc.equals("CAB")) {
					sucursalActual = 13;
				}
			} else {
				
				Short tmp = obtenerSucursalFiltro();
				if (tmp == null) {
					sucursalActual = 6;
				}else {
					sucursalActual = tmp;
				}
			}
		} catch (Exception e) {
			log.error("No pudo obtener el rol: ", e);
		}
		return sucursalActual;
	}

	public boolean tienePermiso(String permiso) {
		LoginBean loginBean = obtenerBean("loginBean");
		boolean permitido = loginBean.tienePermiso(permiso);
		return permitido;
	}

	public String obtenerFolioFormateado(String folio) {
		if (folio.length() == 10) {
			String documento = folio.substring(0, 2);
			String idAlmacen = folio.substring(2, 4);
			String consecutivo = folio.substring(4);
			String folioFormateado = documento + "-" + idAlmacen + "-" + consecutivo;
			return folioFormateado;
		}
		return folio;
	}

	public String getInitParameter(String parametro) {
		String initParameter = getFacesContext().getExternalContext().getInitParameter(parametro);
		return initParameter;
	}

	public String getRutaArchivos() {
		String archivo = getInitParameter("ruta_archivos");
		return archivo;
	}

	/**
	 * Pasa los mensajes que estan en Flash a la cola de mensajes a desplegar
	 */
	protected boolean mostrarMensajesGrid() {
		String mensajeInfo = (String) getFlash().get("mensajeInfo");

		if (mensajeInfo == null) {
			String mensajeError = (String) getFlash().get("mensajeError");
			if (mensajeError == null) {
				Mensaje mensajeWarn = (Mensaje) getFlash().get("mensajeWarnDetail");
				if (mensajeWarn == null) {
					Mensaje mensajeInfoDetail = (Mensaje) getFlash().get("mensajeInfoDetail");
					if (mensajeInfoDetail != null) {
						String summary = mensajeInfoDetail.getSummary();
						String detail = mensajeInfoDetail.getDetail();
						agregarMensajeInfo(summary, detail);
						return true;
					}
				} else {
					String summary = mensajeWarn.getSummary();
					String detail = mensajeWarn.getDetail();
					agregarMensajeWarn(summary, detail);
				}
			} else {
				agregarMensajeError(mensajeError);
			}
		} else {
			agregarMensajeInfo(mensajeInfo);
			return true;
		}

		return false;
	}

	/**
	 * Verifica la sintaxis de la direccion de correo
	 *
	 * @param correo email
	 * @return El resultado de la validacion
	 */
	protected boolean validarCorreo(String correo) {
		String expresionCorreoValido = "^[A-Za-z][a-zA-Z0-9|\\_|\\-|\\.]*@[a-zA-Z]+\\.[a-zA-Z]+(\\.[a-zA-Z]+)*$";
		Pattern p = Pattern.compile(expresionCorreoValido);
		Matcher m = p.matcher(correo);
		boolean resultado = m.find();
		return resultado;
	}

	/**
	 * Modifica la bandera "pasovalidacion" para que permita cerrar un dialogo que
	 * pase todas las validaciones. <br>
	 * Agrega en el contexto de la petición la variable <b>"pasovalidacion"</b> con
	 * el valor <b>true</b> <br/>
	 * <br/>
	 * Ejemplo de como leer desde JavaScript:
	 *
	 * <pre>
	 *	function handleDialogo(xhr, status, args) {
	 *	    if (args.pasovalidacion) {
	 *		    agregarComponente.hide();
	 *	    }
	 *	}
	 * </pre>
	 * </p>
	 */
	@SuppressWarnings("deprecation")
	protected void permitirCerrarDialogo() {
		RequestContext.getCurrentInstance().addCallbackParam("pasovalidacion", true);
	}

	/**
	 * Obtiene el username del usuario que actualmente inicio sesion. <br>
	 * Se obtiene a partir de la instancia de LoginBean, siempre existe una
	 * instancia de LoginBean porque pasa por el filtro de seguridad asegurandose
	 * que exista una sesion valida
	 *
	 * @return username
	 */
//	protected String obtenerUsernameActual() {
//		LoginBean loginBean = obtenerBean("loginBean");
//		String username = loginBean.getUsuario().getUsuario();
//		return username;
//	}

	/**
	 * Obtiene el nombre completo del usuario que actualmente inicio sesion. <br>
	 * Se obtiene a partir de la instancia de LoginBean, siempre existe una
	 * instancia de LoginBean porque pasa por el filtro de seguridad asegurandose
	 * que exista una sesion valida
	 *
	 * @return username
	 */
//	protected String obtenerNombreUsuarioActual() {
//		LoginBean loginBean = obtenerBean("loginBean");
//		String nombreUsuario = loginBean.getUsuario().getNombreCompleto();
//		return nombreUsuario;
//	}
//
//	protected String obtenerRolUsuario() {
//		LoginBean loginBean = obtenerBean("loginBean");
//		String rolUsuario = loginBean.getRol();
//		return rolUsuario;
//	}

	/**
	 * Determina si la sesion actual tiene acceso de solo lectura en un campo del
	 * formulario de una entidad
	 *
	 * @param objeto Entidad a la que hace referencia;
	 * @param tarea  Tarea que se quiere realizar sobre la entidad; Ejemplo:
	 *               "Agregar", "Modificar"
	 * @param campo  Campo de la tarea que se quiere realizar; Ejemplo: "PIN"
	 * @return true en caso de que tenga acceso y false en caso contrario
	 */
	/*
	 * protected boolean tieneAccesoLectura(String objeto, String tarea, String
	 * campo) { LoginBean loginBean = obtenerBean("loginBean"); AccesoUsuario
	 * accesoUsuario = loginBean.getAccesoUsuario(); String acceso =
	 * accesoUsuario.obtenerAccesoCampo(objeto, tarea, campo); boolean resultado =
	 * (acceso != null && acceso.equalsIgnoreCase("L")); return resultado; }
	 */

	/**
	 * Determina si la sesion actual tiene acceso a una tarea de una entidad
	 * 
	 * @param objeto Entidad a la que hace referencia;
	 * @param tarea  Tarea que se quiere realizar sobre la entidad; Ejemplo:
	 *               "Agregar", "Modificar"
	 * @return true en caso de que tenga acceso y false en caso contrario
	 */
//	protected boolean tieneAccesoTarea(String objeto, String tarea) {
//		LoginBean loginBean = obtenerBean("loginBean");
//		String rol = loginBean.getRol();
//		String sistema = loginBean.getSistema();
//		String modulo = loginBean.getModulo();
//		boolean resultado = false;
//		List<AccesoGeneral> lista;
//		List<AccesoGeneral> lista2; 
//		try {
//			resultado = loginBean.getControlAccesosPort().tieneAcceso(rol, sistema, modulo, objeto, tarea);
//			lista = loginBean.getControlAccesosPort().obtenerListaAccesoGrupoObjeto(rol, sistema, modulo, objeto);
//			lista2 = loginBean.getControlAccesosPort().obtenerListaAccesosTareaGrupoObjeto(rol, sistema, modulo, objeto, tarea);
//		} catch (Exception e) {
//			String mensajeError = e.getMessage();
//			getFlash().put("mensajeError", mensajeError);
//		}
//		return resultado;
//	}

	/**
	 * Agrega un mensaje sin detalle de tipo informativo a la cola de mensajes.
	 *
	 * @param String  - summary (titulo)
	 * @param String  - Mensaje informativo
	 * @param String  - Tipo del mensaje info, warn o error (1, 2 o 3)
	 * @param boolean - keep message
	 */
	protected void agregarMensaje(String summary, String detalle, String tipo, boolean keep) {
		FacesMessage message = new FacesMessage();
		switch (tipo) {
		case "info":
		case "1":
			message.setSeverity(FacesMessage.SEVERITY_INFO);
			break;
		case "warn":
		case "2":
			message.setSeverity(FacesMessage.SEVERITY_WARN);
			break;
		case "error":
		case "3":
			message.setSeverity(FacesMessage.SEVERITY_ERROR);
			break;

		}
		message.setSummary(summary);
		message.setDetail(detalle);
		getFacesContext().addMessage(null, message);

		if (keep) {
			getFlash().setKeepMessages(true);
		}
	}

	/**
	 * Agrega un mensaje sin detalle de tipo informativo a la cola de mensajes.
	 *
	 * @param mensaje Mensaje informativo
	 */
	protected void agregarMensajeInfo(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
		this.getFlash().setKeepMessages(true);

	}

	/**
	 * Agrega un mensaje sin detalle de tipo informativo a la cola de mensajes.
	 *
	 * @param mensaje Mensaje informativo
	 */
	protected void agregarMensajeInfoKeep(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje tipo informativo con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeInfo(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje tipo informativo con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeInfoKeep(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_INFO);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega un mensaje sin detalle de error a la cola de mensajes.
	 *
	 * @param mensaje Mensaje de error
	 */
	protected void agregarMensajeError(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
	}

	/**
	 * Agrega un mensaje sin detalle de error a la cola de mensajes.
	 *
	 * @param mensaje Mensaje de error
	 */
	protected void agregarMensajeErrorKeep(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje de error con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeErrorKeep(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje de error con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeError(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_ERROR);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
	}

	/**
	 * Agrega un mensaje sin detalle de advertencia a la cola de mensajes.
	 *
	 * @param mensaje Mensaje de advertencia
	 */
	protected void agregarMensajeWarn(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_WARN);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
	}

	/**
	 * Agrega un mensaje sin detalle de advertencia a la cola de mensajes.
	 *
	 * @param mensaje Mensaje de advertencia
	 */
	protected void agregarMensajeWarnKeep(String mensaje) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_WARN);
		message.setSummary(mensaje);
		message.setDetail("");
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje tipo advertencia con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeWarnKeep(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_WARN);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
		getFlash().setKeepMessages(true);
	}

	/**
	 * Agrega a la cola de mensajes, un mensaje tipo advertencia con un detalle o
	 * explicacion detallada.
	 *
	 * @param summary Mensaje general
	 * @param detail  Explicación detallada del mensaje
	 */
	protected void agregarMensajeWarn(String summary, String detail) {
		FacesMessage message = new FacesMessage();
		message.setSeverity(FacesMessage.SEVERITY_WARN);
		message.setSummary(summary);
		message.setDetail(detail);
		getFacesContext().addMessage(null, message);
	}

	/**
	 * Obtiene la memoria Flash actual. <br>
	 * The Flash concept is taken from Ruby on Rails and provides a way to pass
	 * temporary objects between the user views generated by the faces lifecycle. As
	 * in Rails, anything one places in the flash will be exposed to the next view
	 * encountered by the same user session and then cleared out. It is important to
	 * note that -next view- may have the same view id as the previous view.
	 *
	 * @return Return the threadsafe {@link Flash} for this application
	 */
	protected Flash getFlash() {
		this.flash = getFacesContext().getExternalContext().getFlash();
		return this.flash;
	}

	/**
	 * Redirecciona utilizando la configuracion de faces-config.xml a partir de un
	 * outcome
	 *
	 * @param outcome Texto a resolver
	 */
	protected void redireccionar(String outcome) {
		ConfigurableNavigationHandler handler = (ConfigurableNavigationHandler) getFacesContext().getApplication()
				.getNavigationHandler();
		handler.performNavigation(outcome);
	}

	protected void redireccionarVista(String outcome) {
		FacesContext contex = FacesContext.getCurrentInstance();
		try {
			contex.getExternalContext().redirect(outcome);
		} catch (IOException e) {

		}
	}

	/**
	 * Obtiene la instancia de un bean de Sesión.
	 *
	 * @param beanName {@link String} nombre del bean de sesión que se desea obtener
	 * @return La instancia del bean de sesión
	 */
	@SuppressWarnings("unchecked")
	protected <T> T obtenerBean(String beanName) {
		Object bean = getSessionMap().get(beanName);

		if (bean == null) {
			bean = obtenerBeanEL(beanName);
		}
		return (T) bean;
	}

	/**
	 * Recupera un ManagedBean de cualquier Scope por medio de una Expression
	 * Language. En caso de que no exista una instancia inicializada, crea una
	 * nueva. Si no se encuentra un ManagedBean registrado con el nombre
	 * proporcionado el resultado es Null.
	 *
	 * @param beanName Nombre con el que está registrado el ManagedBean
	 * @return Instancia del ManagedBean recuperada o creada
	 */
	@SuppressWarnings({ "unchecked", "el-syntax" })
	private static <T> T obtenerBeanEL(String beanName) {
		FacesContext context = FacesContext.getCurrentInstance();
		String expresion = "#{" + beanName + "}";
		return (T) context.getApplication().evaluateExpressionGet(context, expresion, Object.class);
	}

	protected FacesContext getFacesContext() {
		this.facesContext = FacesContext.getCurrentInstance();
		return this.facesContext;
	}

	protected ExternalContext getExternalContext() {
		externalContext = getFacesContext().getExternalContext();
		return externalContext;
	}

	protected void setExternalContext(ExternalContext externalContext) {
		this.externalContext = externalContext;
	}

	protected Map<String, Object> getRequestMap() {
		this.requestMap = externalContext.getRequestMap();
		return this.requestMap;
	}

	protected void setRequestMap(Map<String, Object> requestMap) {
		this.requestMap = requestMap;
	}

	protected Map<String, Object> getSessionMap() {
		this.sessionMap = externalContext.getSessionMap();
		return this.sessionMap;
	}

	protected void setSessionMap(Map<String, Object> sessionMap) {
		this.sessionMap = sessionMap;
	}

	public String getServerUrlServices() {
		//setServerUrlServices(serverUrlServices);
		return serverUrlServices;
	}


}