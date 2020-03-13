package com.matco.backlogs.bean;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
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

import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.amce3.facade.OtrosTrabajosFacade;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.OtrosTrabajos;
import com.matco.servicio.entity.Tecnico;

@ManagedBean(name = "administradorOtrosTrabajosBean")
@ViewScoped
public class AdministradorOtrosTrabajosBean extends GenericBacklogBean implements Serializable {
	private static final long serialVersionUID = 388423039343668162L;
	private static final Logger log = Logger.getLogger(AdministradorOtrosTrabajosBean.class);
	private List<OtrosTrabajos> otrosTrabajosList;
	private OtrosTrabajos otrosTrabajosSeleccionado;
	private Tecnico tecnicoResponsable = new Tecnico();
	// listaCodigoSistemaa
	private List<CodigosSistemas> codigosSistemasList = new ArrayList<>();
	private LoginBean loginBean;
	private String descripcion;
	private String numSerie;
	private String nombreTecnico;
	private String error;
	private String summary = "Otros Trabajos";
	private String usuario;
	// codigoSistemaUnico
	private String codigoSistema = "";
	private boolean habilitaModificar = true;
	private boolean habilitarTecnico = false;
	private boolean habilitarSerie = false;

	public AdministradorOtrosTrabajosBean() {
		this.mostrarMensajesGrid();
	}

	@PostConstruct
	public void init() {
		setOtrosTrabajosList(listarOtrosTrabajos());
		this.loginBean = this.obtenerBean("loginBean");
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		codigosSistemasList = listarSistemasBacklogsMineros();
		numSerie = "";
		descripcion = "";
	}

	private List<CodigosSistemas> listarSistemasBacklogsMineros() {
		CodigosSistemasFacade codigosSistemasFacade = new CodigosSistemasFacade(RUTA_PROPERTIES_AMCE3);
		try {
			codigosSistemasList = codigosSistemasFacade.obtenerTodosCodigosSistemas();
			Comparator<CodigosSistemas> comp = (CodigosSistemas a, CodigosSistemas b) -> {
				Integer codigoA = a.getCodigoSistema();
				Integer codigoB = b.getCodigoSistema();
				return codigoA.compareTo(codigoB);
			};
			Collections.sort(codigosSistemasList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Sistemas";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return codigosSistemasList;
	}

	private List<OtrosTrabajos> listarOtrosTrabajos() {
		OtrosTrabajosFacade otrosTrabajosFacade = new OtrosTrabajosFacade(RUTA_PROPERTIES_AMCE3);
		try {
			otrosTrabajosList = otrosTrabajosFacade.obtenerTodosOtrosTrabajos();
			Comparator<OtrosTrabajos> comp = (OtrosTrabajos a, OtrosTrabajos b) -> {
				Date fechaA = a.getFechaCreacion() != null ? a.getFechaCreacion() : new Date();
				Date fechaB = b.getFechaCreacion() != null ? b.getFechaCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(otrosTrabajosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los " + summary;
			log.error(error, e);
			agregarMensajeError(summary, error);
			return null;
		}
		return otrosTrabajosList;
	}
	
	/**
	 * Obtiene la clave y nombre del tecnico y lo formatea
	 * @param claveNomina
	 * @param nombreTecnico
	 * @return clave - nombre tecnico
	 */
	public String getTecnicoFormateado(Integer claveNomina, String nombreTecnico) {
		DecimalFormat decimalFormat = new DecimalFormat("000000");
		String tecnicoFormateado = null;
		if (claveNomina != null && claveNomina != 0 && nombreTecnico != null && nombreTecnico.length() != 0) {
			tecnicoFormateado = decimalFormat.format(claveNomina) + " - " + nombreTecnico;
		} else if (nombreTecnico != null && nombreTecnico.length() != 0) {
			tecnicoFormateado = nombreTecnico;
		}
		return tecnicoFormateado;
	}

	public String getCodigoSistema(OtrosTrabajos tmp) {
		String codigoFormateado = tmp.getCodigoSistema() + " " + tmp.getDescripcionCodigoSistema();
		return codigoFormateado;
	}

	public void agregarOtrosTrabajos(ActionEvent actionEvent) {

		if (numSerie == null || tecnicoResponsable == null || descripcion.equals("") || codigoSistema == null) {

			if (numSerie == null) {
				agregarMensajeWarn(summary, "El campo 'Serie del equipo' está vacío y se requiere para continuar");
			}
			if (tecnicoResponsable == null) {
				agregarMensajeWarn(summary, "El campo 'Responsable' está vacío y se requiere para continuar");
			}

			if (descripcion.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
			}

			if (codigoSistema == null || codigoSistema.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Sistema' está vacío y se requiere para continuar");
			}

		} else {
			OtrosTrabajos nuevo = new OtrosTrabajos();
			OtrosTrabajosFacade otrosTrabajosFacade = new OtrosTrabajosFacade(RUTA_PROPERTIES_AMCE3);
			nuevo.setNumeroSerie(numSerie);
			nuevo.setDescripcionOtrosTrabajos(descripcion.toUpperCase());
			tecnicoResponsable.setNombre(nombreTecnico);
			nuevo.setTecnicoResponsable(tecnicoResponsable);
			nuevo.setCreadoPor(usuario);
			nuevo.setFechaInicioOtroTrabajo(new Date());
			nuevo.setComentariosOtroTrabajo("");
			nuevo.setCodigoSistema(codigoSistema);
			try {
				otrosTrabajosFacade.guardarOtrosTrabajos(nuevo);
				agregarMensajeInfo(summary, "Se ha agregado el otro trabajo: " + nuevo.getDescripcionOtrosTrabajos());
				PrimeFaces.current().executeScript("PF('altaOtroTrabajo').hide();");
			} catch (Exception e) {
				error = "No ha podido agregar el otro trabajo: " + nuevo.getDescripcionOtrosTrabajos();
				log.error(error, e);
				agregarMensajeError(summary, error);
			}
			reload();
		}
	}

	public void modificarOtrosTrabajos(ActionEvent actionEvent) {
		if (numSerie == null || tecnicoResponsable == null || codigoSistema == null) {
			if (numSerie == null) {
				agregarMensajeWarn(summary, "El campo 'Serie del equipo' está vacío y se requiere para continuar");
			}
			if (tecnicoResponsable == null) {
				agregarMensajeWarn(summary, "El campo 'Responsable' está vacío y se requiere para continuar");
			}
			if (descripcion.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
			}
			if (codigoSistema == null || codigoSistema.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Sistema' está vacío y se requiere para continuar");
			}

		} else {
			String otrosTrabajoSeleccionado = otrosTrabajosSeleccionado.getDescripcionOtrosTrabajos();

			OtrosTrabajos nuevo = new OtrosTrabajos();
			OtrosTrabajosFacade otrosTrabajosFacade = new OtrosTrabajosFacade(RUTA_PROPERTIES_AMCE3);
			Integer idOtroTrabajo = otrosTrabajosSeleccionado.getIdOtroTrabajo();
			nuevo.setIdOtroTrabajo(idOtroTrabajo);
			nuevo.setDescripcionOtrosTrabajos(descripcion);
			nuevo.setModificadoPor(usuario);
			tecnicoResponsable.setNombre(nombreTecnico);
			nuevo.setTecnicoResponsable(tecnicoResponsable);
			nuevo.setNumeroSerie(otrosTrabajosSeleccionado.getNumeroSerie());
			nuevo.setCodigoSistema(codigoSistema);

			try {
				otrosTrabajosFacade.editarOtrosTrabajos(nuevo);
				String detail = "Se ha modificado el otro trabajo: " + otrosTrabajoSeleccionado + " a "
						+ nuevo.getDescripcionOtrosTrabajos();
				PrimeFaces.current().executeScript("PF('modificarOtroTrabajo').hide();");
				agregarMensajeInfo(summary, detail);
			} catch (Exception e) {
				error = "No ha podido modificar el otro trabajo: " + otrosTrabajoSeleccionado;
				log.error(error, e);
				agregarMensajeError(summary, error);
			}
			reload();
		}
	}

	public void deshabilitarControlesBusquedaTecnico(SelectEvent event) throws Exception {
		setHabilitarTecnico(true);
	}

	public void habilitarControlesBusquedaTecnico(ActionEvent event) {
		setHabilitarTecnico(false);
		setTecnicoResponsable(null);
	}

	public void deshabilitarControlesBusquedaSerie(SelectEvent event) throws Exception {
		setHabilitarSerie(true);
	}

	public void habilitarControlesBusquedaSerie(ActionEvent event) {
		setHabilitarSerie(false);
		setNumSerie(null);
	}

	public void onRowSelect(SelectEvent event) {
		setHabilitaModificar(false);
		setHabilitarSerie(true);
		setHabilitarTecnico(true);
		OtrosTrabajos aux = (OtrosTrabajos) event.getObject();
		setOtrosTrabajosSeleccionado(aux);
		String descripcionOtroTrabajo = aux.getDescripcionOtrosTrabajos();
		String numeroSerie = aux.getNumeroSerie();
		Tecnico tecnico = aux.getTecnicoResponsable();
		setDescripcion(descripcionOtroTrabajo);
		setTecnicoResponsable(tecnico);
		setNumSerie(numeroSerie);
		setCodigoSistema(aux.getCodigoSistema());
	}

	public void reload() {
		setOtrosTrabajosSeleccionado(null);
		setHabilitaModificar(true);
		setHabilitarSerie(false);
		setHabilitarTecnico(false);
		getOtrosTrabajosList().clear();
		setDescripcion("");
		setTecnicoResponsable(null);
		setNumSerie("");
		setCodigoSistema("");
		setOtrosTrabajosList(listarOtrosTrabajos());
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('tablaOtrosTrabajos').clearFilters()");
	}

	public void limpiaCampos() {
		setHabilitarSerie(false);
		setHabilitarTecnico(false);
		setDescripcion("");
		setTecnicoResponsable(null);
		setNumSerie("");
		setCodigoSistema("");
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

	public List<OtrosTrabajos> getOtrosTrabajosList() {
		return otrosTrabajosList;
	}

	public void setOtrosTrabajosList(List<OtrosTrabajos> otrosTrabajosList) {
		this.otrosTrabajosList = otrosTrabajosList;
	}

	public String getNumSerie() {
		return numSerie;
	}

	public void setNumSerie(String numSerie) {
		this.numSerie = numSerie;
	}

	public OtrosTrabajos getOtrosTrabajosSeleccionado() {
		return otrosTrabajosSeleccionado;
	}

	public void setOtrosTrabajosSeleccionado(OtrosTrabajos otrosTrabajosSeleccionado) {
		this.otrosTrabajosSeleccionado = otrosTrabajosSeleccionado;
	}

	public Tecnico getTecnicoResponsable() {
		return tecnicoResponsable;
	}

	public void setTecnicoResponsable(Tecnico tecnicoResponsable) {
		this.tecnicoResponsable = tecnicoResponsable;
	}

	public boolean isHabilitarTecnico() {
		return habilitarTecnico;
	}

	public void setHabilitarTecnico(boolean habilitarTecnico) {
		this.habilitarTecnico = habilitarTecnico;
	}

	public boolean isHabilitarSerie() {
		return habilitarSerie;
	}

	public void setHabilitarSerie(boolean habilitarSerie) {
		this.habilitarSerie = habilitarSerie;
	}

	public String getNombreTecnico() {
		return nombreTecnico;
	}

	public void setNombreTecnico(String nombreTecnico) {
		this.nombreTecnico = nombreTecnico;
	}

	public String getCodigoSistema() {
		return codigoSistema;
	}

	public void setCodigoSistema(String codigoSistema) {
		this.codigoSistema = codigoSistema;
	}

	public List<CodigosSistemas> getCodigosSistemasList() {
		return codigosSistemasList;
	}

	public void setCodigosSistemasList(List<CodigosSistemas> codigosSistemasList) {
		this.codigosSistemasList = codigosSistemasList;
	}

}
