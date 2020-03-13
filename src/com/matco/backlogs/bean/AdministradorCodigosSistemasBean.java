package com.matco.backlogs.bean;

import java.io.Serializable;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.controlaccesos.service.Usuario;

/**
 * Administrador del cat�logo de c�digos de sistemas
 * 
 * @author N Soluciones de Software
 *
 */
@ManagedBean(name = "administradorCodigosSistemasBean")
@ViewScoped
public class AdministradorCodigosSistemasBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = 388423039343668162L;
	private static final Logger log = Logger.getLogger(AdministradorCodigosSistemasBean.class);

	private boolean habilitaModificar = true;

	private Integer codigo;
	private String descripcion;
	private String sistema;
	private String detalle;
	private String usuario;

	private List<CodigosSistemas> codigosSistemasList;
	private CodigosSistemas codigosSistemasSeleccionado;
	private LoginBean loginBean;
	PrimeFaces primeFaces = PrimeFaces.current();
	CodigosSistemasFacade codigosSistemasFacade = new CodigosSistemasFacade(RUTA_PROPERTIES_AMCE3);

	@PostConstruct
	public void init() {
		setCodigosSistemasList(listarCodigosSistemas());
		this.loginBean = this.obtenerBean("loginBean");
		Usuario usuario = loginBean.getUsuario();
		this.usuario = (usuario == null) ? "DESARROLLO" : usuario.getUsuario();
	}

	/**
	 * Agrega un c�digo de sistemas
	 * 
	 * @param actionEvent Objeto del evento de acci�n
	 */
	public void agregarCodigosSistemas(ActionEvent actionEvent) {
		if (descripcion.equals("") || codigo == null) {
			if (descripcion.equals("")) {
				detalle = "El campo 'Descripci�n' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}

			if (codigo == null) {
				detalle = "El campo 'C�digo' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}

			if (sistema == null) {
				detalle = "EL campo 'Sistema' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}
			return;
		}

		CodigosSistemas codigosSistemas = new CodigosSistemas();
		CodigosSistemasFacade codigosSistemasFacade = new CodigosSistemasFacade(RUTA_PROPERTIES_AMCE3);
		codigosSistemas.setCodigoSistema(codigo);
		codigosSistemas.setDescripcionCodigoSistema(descripcion.toUpperCase());
		codigosSistemas.setCreadoPor(usuario);
		codigosSistemas.setSistema(sistema.toUpperCase());
		
		try {
			codigosSistemasFacade.guardarCodigosSistemas(codigosSistemas);
			detalle = "Se ha agregado el c�digo sistema: " + codigosSistemas.getCodigosSistemasGrid();
			agregarMensaje(summary, detalle, "info", false);

			primeFaces.executeScript("PF('altaSistema').hide();");

		} catch (Exception e) {
			detalle = "No ha podido agregar el c�digo sistema: " + codigosSistemas.getCodigosSistemasGrid();
			log.error(detalle, e);
			agregarMensaje(summary, detalle, "error", false);
		}

		limpiarCampos();
	}

	/**
	 * Modifica un c�digo de sistemas
	 * 
	 * @param actionEvent Objeto del evento acci�n
	 */
	public void modificarCodigosSistemas(ActionEvent actionEvent) {
		if (descripcion.equals("") || codigo == null) {
			if (descripcion.equals("")) {
				detalle = "El campo 'Descripci�n' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}

			if (codigo == null) {
				detalle = "El campo 'C�digo' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}

			if (sistema == null) {
				detalle = "EL campo 'Sistema' est� vac�o y se requiere para continuar";
				agregarMensaje(summary, detalle, "warn", false);
			}
			return;
		}

		CodigosSistemas codigosSistemas = new CodigosSistemas();
		String codigoSistemaSeleccionado = codigosSistemasSeleccionado.getDescripcionCodigoSistema();
		Integer codigoSistema = codigosSistemasSeleccionado.getCodigoSistema();
		codigosSistemas.setCodigoSistema(codigoSistema);
		codigosSistemas.setDescripcionCodigoSistema(descripcion.toUpperCase());
		codigosSistemas.setModificadoPor(usuario);
		codigosSistemas.setSistema(sistema.toUpperCase());

		try {

			codigosSistemasFacade.editarCodigosSistemas(codigosSistemas);
			detalle = "Se ha modificado el c�digo sistema: "+ codigosSistemasSeleccionado.getCodigosSistemasGrid();

			agregarMensaje(summary, detalle, "info", false);
			primeFaces.executeScript("PF('modificarSistema').hide();");

		} catch (Exception e) {
			detalle = "No ha podido modificar el c�digo sistema: " + codigoSistemaSeleccionado;
			log.error(detalle, e);
			agregarMensaje(summary, detalle, "error", false);
		}

		limpiarCampos();
	}

	/**
	 * Evento de selecci�n de fila
	 * 
	 * @param event Objeto del evento de selecci�n
	 */
	public void onRowSelect(SelectEvent event) {
		setHabilitaModificar(false);
		CodigosSistemas codigosSistemas = (CodigosSistemas) event.getObject();
		setCodigosSistemasSeleccionado(codigosSistemas);
		Integer codigoSistema = codigosSistemas.getCodigoSistema();
		String descripcionCodigosSistemas = codigosSistemas.getDescripcionCodigoSistema();
		setDescripcion(descripcionCodigosSistemas);
		setCodigo(codigoSistema);
		setSistema(codigosSistemas.getSistema());
	}

	/**
	 * Limpia los campos de la vista
	 */
	public void limpiarCampos() {
		setHabilitaModificar(true);
		getCodigosSistemasList().clear();
		setDescripcion("");
		setCodigo(null);
		setCodigosSistemasSeleccionado(null);
		setSistema("");
		setCodigosSistemasList(listarCodigosSistemas());
		primeFaces.executeScript("PF('tablaSistemas').clearFilters()");
	}

	public void limpiarCamposDialogo() {
		setDescripcion("");
		setCodigo(null);
		setSistema("");
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public boolean isHabilitaModificar() {
		return habilitaModificar;
	}

	public void setHabilitaModificar(boolean habilitaModificar) {
		this.habilitaModificar = habilitaModificar;
	}

	public List<CodigosSistemas> getCodigosSistemasList() {
		return codigosSistemasList;
	}

	public void setCodigosSistemasList(List<CodigosSistemas> codigosSistemasList) {
		this.codigosSistemasList = codigosSistemasList;
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
	}

	public CodigosSistemas getCodigosSistemasSeleccionado() {
		return codigosSistemasSeleccionado;
	}

	public void setCodigosSistemasSeleccionado(CodigosSistemas codigosSistemasSeleccionado) {
		this.codigosSistemasSeleccionado = codigosSistemasSeleccionado;
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
	}
}
