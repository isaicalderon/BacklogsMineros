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
import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.backlogs.entity.CodigosSMCS;

@ManagedBean(name = "administradorCodigosSMCSBean")
@ViewScoped
public class AdministradorCodigosSMCSBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = -7027637111345193382L;
	private static final Logger log = Logger.getLogger(AdministradorCodigosSMCSBean.class);
	private List<CodigosSMCS> codigosSMCSList;
	private CodigosSMCS codigosSMCSSeleccionado;
	private LoginBean loginBean;
	private Integer codigo;
	private Double horasHombre;
	private String descripcion;
	private String error;
	private String summary = "Códigos SMCS";
	private String usuario;
	private boolean habilitaModificar = true;

	@PostConstruct
	public void init() {
		setCodigosSMCSList(listarCodigosSMCS());
		this.loginBean = this.obtenerBean("loginBean");
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
	}

	public void agregarCodigosSMCS(ActionEvent actionEvent) {
		if (codigo == null || horasHombre == null || descripcion.equals("")) {
			if (codigo == null) {
				agregarMensajeWarn(summary, "El campo 'Código' está vacío y se requiere para continuar");
			}
			if (descripcion.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
			}
			if (horasHombre == null) {
				agregarMensajeWarn(summary, "El campo 'Horas de trabajo' está vacío y se requiere para continuar");
			}
			return;
		}

		CodigosSMCS nuevo = new CodigosSMCS();
		CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		nuevo.setCodigoSMCS(codigo);
		nuevo.setDescripcionSMCS(descripcion.toUpperCase());
		nuevo.setHoraHombreCodigoSMCS(horasHombre);
		nuevo.setCreadoPor(usuario);
		try {
			codigosSMCSFacade.guardarCodigosSMCS(nuevo);
			agregarMensajeInfo(summary, "Se ha agregado el código: " + nuevo.getCodigosSMCSGrid());
			PrimeFaces.current().executeScript("PF('altaCodigo').hide();");
		} catch (Exception e) {
			error = "No ha podido agregar el código: " + nuevo.getCodigosSMCSFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		limpiarCampos();
	}

	public void modificarCodigosSMCS(ActionEvent actionEvent) {

		if (codigo == null || horasHombre == null || descripcion.equals("")) {
			if (codigo == null) {
				agregarMensajeWarn(summary, "El campo 'Código' está vacío y se requiere para continuar");
			}
			if (descripcion.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Descripción' está vacío y se requiere para continuar");
			}
			if (horasHombre == null) {
				agregarMensajeWarn(summary, "El campo 'Horas de trabajo' está vacío y se requiere para continuar");
			}
			return;
		}
		String codigoSeleccionado = codigosSMCSSeleccionado.getCodigosSMCSGrid();

		CodigosSMCS nuevo = new CodigosSMCS();
		CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		Short idCodigoSeleccionado = codigosSMCSSeleccionado.getIdCodigoSMCS();
		nuevo.setIdCodigoSMCS(idCodigoSeleccionado);
		nuevo.setCodigoSMCS(codigo);
		nuevo.setDescripcionSMCS(descripcion.toUpperCase());
		nuevo.setHoraHombreCodigoSMCS(horasHombre);
		nuevo.setModificadoPor(usuario);

		try {
			codigosSMCSFacade.editarCodigosSMCS(nuevo);
			// Por si el código cambiá la descripción.
			String detailCambio = "Se ha modificado el código: " + codigosSMCSSeleccionado.getCodigosSMCSGrid() + " a "
					+ nuevo.getCodigosSMCSGrid();

			// Si el código no ha cambiado la descripción pero lo demás sí.
			String detailNormal = "Se ha modificado el código: " + nuevo.getCodigosSMCSGrid();
			String detail = codigoSeleccionado.equals(nuevo.getCodigosSMCSGrid()) ? detailNormal : detailCambio;

			agregarMensajeInfo(summary, detail);
			PrimeFaces.current().executeScript("PF('modificarCodigo').hide();");
		} catch (Exception e) {
			error = "No ha podido modificar el código: " + codigoSeleccionado;
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		limpiarCampos();
	}

	public void seleccionarCodigoSMCS(SelectEvent event) {
		setHabilitaModificar(false);
		CodigosSMCS aux = (CodigosSMCS) event.getObject();
		setCodigosSMCSSeleccionado(aux);
		String descripcionCodigoSMCS = aux.getDescripcionSMCS();
		Integer codigoSMCS = aux.getCodigoSMCS();
		Double horaHombre = aux.getHoraHombreCodigoSMCS();
		setCodigo(codigoSMCS);
		setDescripcion(descripcionCodigoSMCS);
		setHorasHombre(horaHombre);
	}

	public void limpiarCampos() {
		setHabilitaModificar(true);
		getCodigosSMCSList().clear();
		setCodigo(null);
		setDescripcion("");
		setHorasHombre(null);
		setCodigosSMCSList(listarCodigosSMCS());
		setCodigosSMCSSeleccionado(null);
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('tablaCodigos').clearFilters()");
	}

	public void limpiaCampos() {
		setCodigo(null);
		setDescripcion("");
		setHorasHombre(null);
	}

	public CodigosSMCS getCodigosSMCSSeleccionado() {
		return codigosSMCSSeleccionado;
	}

	public void setCodigosSMCSSeleccionado(CodigosSMCS codigosSMCSSeleccionado) {
		this.codigosSMCSSeleccionado = codigosSMCSSeleccionado;
	}

	public List<CodigosSMCS> getCodigosSMCSList() {
		return codigosSMCSList;
	}

	public void setCodigosSMCSList(List<CodigosSMCS> codigosSMCSList) {
		this.codigosSMCSList = codigosSMCSList;
	}

	public Double getHorasHombre() {
		return horasHombre;
	}

	public void setHorasHombre(Double horasHombre) {
		this.horasHombre = horasHombre;
	}

	public Integer getCodigo() {
		return codigo;
	}

	public void setCodigo(Integer codigo) {
		this.codigo = codigo;
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

}
