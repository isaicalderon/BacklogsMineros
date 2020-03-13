package com.matco.backlogs.bean;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.SelectEvent;

import com.matco.amce3.facade.CargosTrabajoFacade;
import com.matco.backlogs.entity.CargosTrabajo;

/**
 * Administrador de cargos de trabajo
 * 
 * @author N Soluciones de Software
 *
 */
@ManagedBean(name = "administradorCargosTrabajoBean")
@ViewScoped
public class AdministradorCargosTrabajoBean extends GenericBacklogBean implements Serializable {
	private static final long serialVersionUID = 388423039343668162L;
	private static final Logger log = Logger.getLogger(AdministradorCargosTrabajoBean.class);

	private boolean habilitaModificar = true;

	private List<CargosTrabajo> cargosTrabajoList;
	private CargosTrabajo cargosTrabajoSeleccionado;
	private LoginBean loginBean;
	private String descripcion;
	private String error;
	private String summary = "Cargos Trabajo";
	private String usuario;

	@PostConstruct
	public void init() {
		setCargosTrabajoList(listarCargosTrabajo());
		this.loginBean = this.obtenerBean("loginBean");
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
	}

	/**
	 * Lista los cargos de trabajo
	 * 
	 * @return Lista de cargos de trabajo
	 */
	public List<CargosTrabajo> listarCargosTrabajo() {
		CargosTrabajoFacade cargosTrabajoFacade = new CargosTrabajoFacade(RUTA_PROPERTIES_AMCE3);
		Date fechaActual = new Date();
		Comparator<CargosTrabajo> cargosTrabajoComparator = null;
		try {
			cargosTrabajoList = cargosTrabajoFacade.obtenerTodosCargosTrabajo();
			cargosTrabajoComparator = (CargosTrabajo cargosTrabajoA, CargosTrabajo cargosTrabajoB) -> {
				Date fechaCargoTrabajo = cargosTrabajoA.getFechaHoraCreacion();
				Date fechaA = fechaCargoTrabajo != null ? fechaCargoTrabajo : fechaActual;
				Date fechaCargoTrabajoB = cargosTrabajoB.getFechaHoraCreacion();
				Date fechaB = fechaCargoTrabajoB != null ? fechaCargoTrabajoB : fechaActual;
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(cargosTrabajoList, cargosTrabajoComparator);
		} catch (Exception e) {
			error = "No se pudieron listar los " + summary;
			agregarMensajeError(summary, error);
			log.error(error, e);
		}
		return cargosTrabajoList;
	}

	/**
	 * Agrega un cargo de trabajo
	 * 
	 * @param actionEvent Objeto de evento de acción
	 */
	public void agregarCargosTrabajo(ActionEvent actionEvent) {
		if (descripcion.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
		}
		CargosTrabajo cargoTrabajoNuevo = new CargosTrabajo();
		CargosTrabajoFacade cargosTrabajoFacade = new CargosTrabajoFacade(RUTA_PROPERTIES_AMCE3);
		String descripcionCargoTrabajoMayusculas = descripcion.toUpperCase();
		cargoTrabajoNuevo.setDescripcionCargoTrabajo(descripcionCargoTrabajoMayusculas);
		cargoTrabajoNuevo.setCreadoPor(usuario);
		try {
			cargosTrabajoFacade.guardarCargosTrabajo(cargoTrabajoNuevo);
			agregarMensajeInfo(summary, "Se ha agregado el cargo trabajo: " + descripcionCargoTrabajoMayusculas);
			PrimeFaces.current().executeScript("PF('altaCargo').hide();");
		} catch (Exception e) {
			error = "No ha podido agregar el cargo trabajo: " + descripcionCargoTrabajoMayusculas;
			agregarMensajeError(summary, error);
			log.error(error, e);
		}
		limpiarCampos();
	}

	/**
	 * Modifica un cargo de trabajo
	 * 
	 * @param actionEvent Objeto de evento de acción
	 */
	public void modificarCargosTrabajo(ActionEvent actionEvent) {
		if (descripcion.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
		}
		String cargoTrabajoSeleccionado = cargosTrabajoSeleccionado.getDescripcionCargoTrabajo();
		CargosTrabajo cargoTrabajoNuevo = new CargosTrabajo();
		CargosTrabajoFacade cargosTrabajoFacade = new CargosTrabajoFacade(RUTA_PROPERTIES_AMCE3);
		Short idCargoTrabajo = cargosTrabajoSeleccionado.getIdCargoTrabajo();
		cargoTrabajoNuevo.setIdCargoTrabajo(idCargoTrabajo);
		cargoTrabajoNuevo.setDescripcionCargoTrabajo(descripcion.toUpperCase());
		cargoTrabajoNuevo.setModificadoPor(usuario);
		try {
			cargosTrabajoFacade.cambioCargosTrabajo(cargoTrabajoNuevo);
			String detail = "Se ha modificado el cargo trabajo: " + cargoTrabajoSeleccionado + " a "
					+ cargoTrabajoNuevo.getDescripcionCargoTrabajo();
			agregarMensajeInfo(summary, detail);
			PrimeFaces.current().executeScript("PF('modificarCargo').hide();");
		} catch (Exception e) {
			error = "No ha podido modificar el cargo trabajo: " + cargoTrabajoSeleccionado;
			agregarMensajeError(summary, error);
			log.error(error, e);
		}
		limpiarCampos();
	}

	/**
	 * Método de evento onRowSelect
	 * 
	 * @param event Objeto de evento de selección
	 */
	public void onRowSelect(SelectEvent event) {
		setHabilitaModificar(false);
		CargosTrabajo cargoTrabajoAux = (CargosTrabajo) event.getObject();
		setCargosTrabajoSeleccionado(cargoTrabajoAux);
		String descripcionCargoTrabajo = cargoTrabajoAux.getDescripcionCargoTrabajo();
		setDescripcion(descripcionCargoTrabajo);
	}

	/**
	 * Limpia los campos utilizados en clase de cargos de trabajo
	 */
	public void limpiarCampos() {
		setHabilitaModificar(true);
		getCargosTrabajoList().clear();
		setDescripcion("");
		setCargosTrabajoList(listarCargosTrabajo());
		setCargosTrabajoSeleccionado(null);
		PrimeFaces primeFaces = PrimeFaces.current();
		primeFaces.executeScript("PF('tablaCargos').clearFilters()");
	}

	/**
	 * Limpia los campos utilizados en el dialogo agregar y editar
	 */
	public void limpiarCamposDialogo() {
		setDescripcion("");
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

	public List<CargosTrabajo> getCargosTrabajoList() {
		return cargosTrabajoList;
	}

	public void setCargosTrabajoList(List<CargosTrabajo> cargosTrabajoList) {
		this.cargosTrabajoList = cargosTrabajoList;
	}

	public CargosTrabajo getCargosTrabajoSeleccionado() {
		return cargosTrabajoSeleccionado;
	}

	public void setCargosTrabajoSeleccionado(CargosTrabajo cargosTrabajoSeleccionado) {
		this.cargosTrabajoSeleccionado = cargosTrabajoSeleccionado;
	}

}
