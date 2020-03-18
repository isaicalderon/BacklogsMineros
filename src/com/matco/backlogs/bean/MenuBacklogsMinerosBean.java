package com.matco.backlogs.bean;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.primefaces.PrimeFaces;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.TransferEvent;
import org.primefaces.model.DualListModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.push.annotation.OnClose;
import com.matco.amce3.dto.BacklogsMinerosDto;
import com.matco.amce3.facade.BacklogsMinerosBitacoraEstatusFacade;
import com.matco.amce3.facade.RevisionKardexFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosBitacoraEstatus;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.RevisionKardexEntity;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;

@ManagedBean(name = "menuBacklogsMinerosBean")
@ViewScoped
public class MenuBacklogsMinerosBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = -5524385157413013411L;

	private boolean checkedBacklogs = false;
	private boolean allCheckBacklogs = false;

	private int contBlCreador = 0;
	private int contBlSolicitante = 0;
	private int sucursal;

	private String numeroCotizacion;
	private String numeroCotizacionSelect;

	private String numeroMatco = "";

	private String comentarioCancelado;

	private String[] estatusSeleccionados;

	private DualListModel<EstatusBacklogsMineros> estadosPickList;

	private GenericBacklogBean genericBacklogBean;

	private List<String> filtroEstados = new ArrayList<>();

	private List<BacklogsMineros> backlogsMinerosList = new ArrayList<>();
	private List<BacklogsMineros> backlogsMinerosListFiltrada;
	private List<BacklogsMineros> backlogsMinerosListFiltradaTemp;

	private List<EstatusBacklogsMineros> estatusBacklogsMinerosList;
	private List<EstatusBacklogsMineros> estatusBacklogsMinerosListTarget; // auxiliar
	private List<CodigosSMCS> codigosSMCSList;
	private List<CodigosSistemas> codigosSistemasList;

	private List<BacklogsMineros> backlogsSeleccionadosTrabajo = new ArrayList<>();
	private List<BacklogsMineros> backlogsSeleccionados;
	private List<BacklogsMineros> backlogsAutorizados;
	private List<BacklogsMineros> backlogsNoAutorizados;
	private List<BacklogsMineros> backlogsCotizados;
	private List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();
	private List<RevisionKardexEntity> revisionCreadorBacklogsList = new ArrayList<>();
	private List<RevisionKardexEntity> revisionSolicitanteBacklogsList = new ArrayList<>();
	private List<BacklogsMinerosBitacoraEstatus> bitacoraList = new ArrayList<>();

	@PostConstruct
	public void init() {
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		this.seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		this.genericBacklogBean = this.obtenerBean("genericBacklogBean");

		seleccionBean.setBacklogEstandarSelected(null);
		seleccionBean.setBacklogsMinerosSeleccionado(null);

		estatusBacklogsMinerosList = seleccionBean.getEstatusBacklogsMinerosList();

		if (estatusBacklogsMinerosList == null) {
			redireccionar("menuBacklogs");
			return;
		}

		int estSize = estatusBacklogsMinerosList.size();
		estatusBacklogsMinerosListTarget = new ArrayList<>();

		// verificamos si se buscaron backlogs
		if (!seleccionBean.getBacklogsFiltradosList().isEmpty()) {
			backlogsMinerosListFiltrada = seleccionBean.getBacklogsFiltradosList();
			for (BacklogsMineros backlog : backlogsMinerosListFiltrada) {
				backlog.setCheckSeleccionado(false);
			}
		}

		// verificamos los estados agregados
		if (!seleccionBean.getEstadosFiltradosList().isEmpty()) {
			for (EstatusBacklogsMineros estados : seleccionBean.getEstadosFiltradosList()) {
				estatusBacklogsMinerosListTarget.add(estados);
				estSize = estatusBacklogsMinerosList.size();

				String estadoKey = estados.getIdEstatusBacklogMinero();

				for (int i = 0; i < estSize; i++) {
					String etmpKey = estatusBacklogsMinerosList.get(i).getIdEstatusBacklogMinero();
					if (estadoKey.equals(etmpKey)) {
						estatusBacklogsMinerosList.remove(i);
						break;
					}
				}
			}
		}

		estadosPickList = new DualListModel<EstatusBacklogsMineros>(estatusBacklogsMinerosList,
				estatusBacklogsMinerosListTarget);

		backlogsSeleccionados = new ArrayList<>();
		backlogsNoAutorizados = new ArrayList<>();
		backlogsAutorizados = new ArrayList<>();
		backlogsCotizados = new ArrayList<>();

		obtenerCantidadBLRevisados();
		
		if (backlogsMinerosListFiltrada != null) {
			if (!backlogsMinerosListFiltrada.isEmpty()) {
				obtenerBacklogs();
			}
		}
		
		mostrarMensajesGrid();
	}

	@OnClose
	public void onClose() {
		System.out.println("Saliendo de blm");
		backlogsMinerosListFiltrada = new ArrayList<>(backlogsMinerosListFiltradaTemp);
		seleccionBean.setBacklogsFiltradosList(backlogsMinerosListFiltrada);
	}

	/**
	 * Filtra la tabla de backlogs por un número de parte MATCO
	 */
	public void filtrarPorNPM() {

		if (numeroMatco.equals("")) {
			try {
				backlogsMinerosListFiltrada = new ArrayList<>(backlogsMinerosListFiltradaTemp);
				seleccionBean.setBacklogsFiltradosList(backlogsMinerosListFiltrada);
				PrimeFaces.current().executeScript("PF('tablaBacklogsMineros').filter();");
			} catch (Exception e) {
				agregarMensaje(summary, "No hay datos para buscar", "warn", false);
			}
			return;
		}
		
		numeroMatco = rellenarNumeroParteMatco(numeroMatco);

		backlogsMinerosListFiltrada.clear();
		for (BacklogsMineros blm : backlogsMinerosListFiltradaTemp) {
			for (BacklogsMinerosDetalleRefa ref : blm.getListaRefacciones()) {
				if (ref.getNumeroParte().equals(numeroMatco)) {
					backlogsMinerosListFiltrada.add(blm);
					break;
				}
			}
		}

		PrimeFaces.current().executeScript("PF('tablaBacklogsMineros').filter();");
	}
	
	public void displayRowIndex() {
	    FacesContext context = FacesContext.getCurrentInstance();
	    Map<String, String> map = context.getExternalContext().getRequestParameterMap();
	    String pIndex = (String) map.get("index");
	    int index = Integer.parseInt(pIndex);
	    
	    System.out.println(index);
	}

	/**
	 * Obtiene los backlogs segun los filtros de la vista
	 * 
	 * @deprecation DATE
	 */
	@SuppressWarnings("deprecation")
	public void obtenerBacklogs() {
		try {
			// filtroEstados = new ArrayList<>();
			//List<BacklogsMineros> backlogsMinerosListTemp = new ArrayList<>();
			backlogsMinerosListFiltrada = new ArrayList<>();

			Date fechaBL1 = seleccionBean.getFechaBL1();
			Date fechaBL2 = seleccionBean.getFechaBL2();

			// obtenemos los backlogs con las fechas correspondientes
			fechaBL2.setDate(fechaBL2.getDate() + 1);
			int diff = fechaBL1.compareTo(fechaBL2);

			seleccionBean.setFechaBL1(fechaBL1);
			seleccionBean.setFechaBL2(fechaBL2);

			if (diff != 1) {

				String numSerie = seleccionBean.getNumeroSerie();
				String modelo = seleccionBean.getModelo();
				sucursal = seleccionBean.getSucursalFiltro().intValue();
				
				// obtenemos los los estados seleccionados
				if (estadosPickList.getTarget().size() == 0) {
					agregarMensaje(summary, "No hay estados seleccionados", "warn", true);
					return;
				}
				
				String estatusKey = "";
				
				for (EstatusBacklogsMineros estatus : estadosPickList.getTarget()) {
					estatusKey += estatus.getIdEstatusBacklogMinero()+",";
				}
				
				int diffLent = 10 - estadosPickList.getTarget().size();
				for(int i = 0; i < diffLent; i++) {
					estatusKey+="x,";
				}
				
				backlogsMinerosListFiltrada = backlogsMinerosFacade.obtenerBacklogsEntreFechasFiltros(fechaBL1,
						fechaBL2, numSerie, modelo, sucursal, estatusKey);
				
				if (backlogsMinerosListFiltrada.isEmpty()) {
					agregarMensaje(summary, "No se encontraron Backlogs.", "warn", true);
					seleccionBean.setBacklogsFiltradosList(backlogsMinerosListFiltrada);
					PrimeFaces.current().executeScript("PF('tablaBacklogsMineros').filter()");
					return;
				}

				seleccionBean.setBacklogsFiltradosList(backlogsMinerosListFiltrada);

				// guardamos la busqueda en otra variable para una segunda busqueda
				backlogsMinerosListFiltradaTemp = new ArrayList<>(backlogsMinerosListFiltrada);

				PrimeFaces.current().executeScript("PF('tablaBacklogsMineros').filter()");

			} else {
				agregarMensaje(summary, "La fecha inicial no puede ser mayor a la fecha final", "warn", false);
				return;
			}

		} catch (Exception e) {
			error = "No se pudo obtener los Backlogs";
			agregarMensaje(summary, error, "3", false);
			log.error(error, e);
		}
	}

	public void obtenerRefaccionesBacklogSeleccionado() {

		if (seleccionBean.getBacklogsMinerosSeleccionado() != null) {

			try {
				
				if (seleccionBean.getBacklogsMinerosSeleccionado().getListaRefacciones().isEmpty()) {
					BacklogsMinerosKey blmKey = seleccionBean.getBacklogsMinerosSeleccionado().getBacklogsMinerosKey();
					List<BacklogsMinerosDetalleRefa> lista = blmRefaFacade.obtenerBacklogMineroDetalleRefaPorId(blmKey);
					lista = generarSubtotalRefacciones(lista);
					seleccionBean.getBacklogsMinerosSeleccionado().setListaRefacciones(lista);
				}
				
				PrimeFaces.current().ajax().update(
						":formListaBacklogs:partesRequeridas :formListaBacklogs:totalTablaRefaccionesSeleccion");

			} catch (Exception e) {
				log.error("No pudo obtener las refacciones ", e);
				agregarMensaje(summary, "No pudo obtener las refacciones", "error", false);
			}
		}
	}

	public void obtenerRefaccionesAllBacklogs() {
		try {
			for (BacklogsMineros backlog : backlogsMinerosListFiltrada) {
				if (backlog.getListaRefacciones().isEmpty()) {
					BacklogsMinerosKey blmKey = backlog.getBacklogsMinerosKey();
					List<BacklogsMinerosDetalleRefa> lista = blmRefaFacade.obtenerBacklogMineroDetalleRefaPorId(blmKey);
					backlog.setListaRefacciones(lista);
				}
			}
		} catch (Exception e) {
			log.error("No pudo obtener las refacciones ", e);
			agregarMensaje(summary, "No pudo obtener las refacciones", "error", false);
		}

	}

	/**
	 * Obtiene todas las revisiones de un usuario inspector o supervisor
	 */
	public void obtenerCantidadBLRevisados() {
		RevisionKardexFacade facade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
		RevisionKardexEntity revision = new RevisionKardexEntity();
		List<RevisionKardexEntity> revisionesTmp;
		try {
			// para el usuario creador
			revision.setUsuarioCreador(this.usuario);
			revision.setEstatusRevision(1);
			revisionCreadorBacklogsList = facade.obtenerRevisionesUsuarioCreador(revision);
			revision.setEstatusRevision(3);
			revisionesTmp = facade.obtenerRevisionesUsuarioCreador(revision);
			for (RevisionKardexEntity rev : revisionesTmp) {
				revisionCreadorBacklogsList.add(rev);
			}
			revisionesTmp.clear();

			// Para el usuario solicitante
			revision.setUsuarioSolicitante(this.usuario);
			revision.setEstatusRevision(2);
			revisionesTmp = facade.obtenerRevisionesUsuarioSolicitante(revision);
			for (RevisionKardexEntity rev : revisionesTmp) {
				if (rev.getFechaCierre() == null) {
					revisionSolicitanteBacklogsList.add(rev);
				}
			}
			revision.setEstatusRevision(4);
			revisionesTmp = facade.obtenerRevisionesUsuarioSolicitante(revision);
			for (RevisionKardexEntity rev : revisionesTmp) {
				if (rev.getFechaCierre() == null) {
					revisionSolicitanteBacklogsList.add(rev);
				}
			}

			contBlCreador = revisionCreadorBacklogsList.size();
			contBlSolicitante = revisionSolicitanteBacklogsList.size();

		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
	}

	/**
	 * Se utiliza para la transeferencia de estados
	 * 
	 * @param event
	 */
	public void onTransfer(TransferEvent event) {
		for (Object item : event.getItems()) {
			// builder.append(((Theme) item).getName()).append("<br />");
			if (item instanceof EstatusBacklogsMineros) {
				EstatusBacklogsMineros estatus = (EstatusBacklogsMineros) item;
				String estatusKey = estatus.getIdEstatusBacklogMinero();
				if (event.isAdd()) {
					filtroEstados.add(estatusKey);
					seleccionBean.getEstadosFiltradosList().add(estatus);
				} else {
					filtroEstados.remove(estatusKey);
					seleccionBean.getEstadosFiltradosList().remove(estatus);
				}
			}
		}
	}

	/***
	 * Actualiza la lista de backlogs seleccionados en la lista de backglos, se
	 * ejecuta después de seleccionar o deseleccionar un backlog
	 * 
	 * @param backlog backlog cuya casilla se seleccionó/deseleccionó
	 */
	public void actualizarSeleccion(BacklogsMineros backlog) {
		
		if (backlogsSeleccionados.contains(backlog)) {
			backlog.setCheckSeleccionado(false);
			backlogsSeleccionados.remove(backlog);
		} else {
			backlog.setCheckSeleccionado(true);
			backlogsSeleccionados.add(backlog);
		}

		if (backlogsSeleccionados.size() > 0) {
			checkedBacklogs = true;
		} else {
			checkedBacklogs = false;
		}
	}

	/***
	 * Actualiza la lista de backlogs seleccionados en la lista de backglos, se
	 * ejecuta después de seleccionar o deseleccionar un backlog
	 * 
	 * @param backlog backlog cuya casilla se seleccionó/deseleccionó
	 */
	public void seleccionarTodosCheckBox() {
		for (BacklogsMineros backlog : backlogsMinerosList) {
			if (backlogsSeleccionados.contains(backlog)) {
				backlog.setCheckSeleccionado(false);
				backlogsSeleccionados.remove(backlog);
			} else {
				backlog.setCheckSeleccionado(true);
				backlogsSeleccionados.add(backlog);
			}
		}

		if (backlogsSeleccionados.size() > 0) {
			checkedBacklogs = true;
		} else {
			checkedBacklogs = false;
		}

	}

	/*** OTRAS FUNCIONES ***/

	/***
	 * Al abrir el dialogo para pasar multiples backlogs a X, verifica que los BL
	 * seleccionados sean los correctos para pasar al siguiente estado, solo guarda
	 * los BL que son aptos para pasar al siguiente estado y los guarda en la lista
	 * backlogsSeleccionadosTrabajo.
	 */
	public void contextMenu_verificarPasarCotizar() {
		String mensaje = "";
		for (BacklogsMineros backlog : backlogsSeleccionados) {
			String estatus = backlog.getIdEstatusBacklogsMineros().getDescripcionEstatusBacklog();
			if (estatus.equals("CREADO") || estatus.equals("MONITOREAR")) {
				// backlogsSeleccionadosTrabajo.add(backlog);
				backlogsSeleccionadosTrabajo.add(backlog);
			}
		}

		if (backlogsSeleccionadosTrabajo.isEmpty()) {
			mensaje = "No hay backlogs con estatus CREADO o MONITOREAR seleccionados.";
			agregarMensajeWarn(summary, mensaje);
			return;
		}
		PrimeFaces.current().executeScript("PF('dialogoAtoB2').show();");
		backlogsSeleccionados.clear();
	}

	/***
	 * Cambia al estado B2 los backlogs seleccionados en la lista de backlogs
	 */
	public void cambioAtoB2Multiple() {

		if (backlogsSeleccionadosTrabajo.isEmpty()) {
			String mensaje = "No hay BL seleccionados.";
			agregarMensajeErrorKeep(summary, mensaje);
			return;
		}

		try {

			BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
					RUTA_PROPERTIES_AMCE3);

			for (BacklogsMineros backlog : backlogsSeleccionadosTrabajo) {
				BacklogsMineros backlogsCopi = backlog;
				BacklogsMinerosDto backlogMineroDto = new BacklogsMinerosDto();
				backlogMineroDto.asignarCamposByBacklog(backlog);

				backlogMineroDto.setUsuarioEstatusFinal(usuario);

				RevisionKardexEntity revisionBacklog = obtenerRevisionBacklogActual(backlog);
				if (revisionBacklog.getIdBacklogMinero() > 0) {
					if (revisionBacklog.getEstatusRevision() == 1 || revisionBacklog.getEstatusRevision() == 3) {
						agregarMensaje(summary,
								"No se pudo actualizar el Backlog "
										+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
										+ ". Actualmente se encuentra con una revisión.",
								"warn", true);
						PrimeFaces.current().ajax().update("growl");
						break;
					}
				}

				revisionBacklog.setFechaCierre(new Date());
				revisionFacade.modificarRevisionKardex(revisionBacklog);

				backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroAcambioB2(backlogMineroDto);

				String message = "Se cambió el Folio "
						+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado() + " de estatus "
						+ "CREADO a estatus POR COTIZAR.";

				agregarMensaje(summary, message, "info", true);

				backlog.setCheckSeleccionado(false);
				backlogsSeleccionados.remove(backlogsCopi);

			}

			while (!backlogsSeleccionadosTrabajo.isEmpty()) {
				backlogsSeleccionadosTrabajo.remove(0);
			}

			// limpiarSeleccion();
			redireccionar("menuBacklogs");

		} catch (Exception e) {
			error = "No se pudo cambiar de estatus CREADO a POR COTIZAR";
			log.error(error, e);
			agregarMensajeErrorKeep(summary, error);
		}
	}

	/***
	 * Al abrir el dialogo para pasar multiples backlogs a Cotizar, verifica que los
	 * BL seleccionados sean los correctos para pasar al siguiente estado, solo
	 * guarda los BL que son aptos para pasar al siguiente estado y los guarda en la
	 * lista backlogsSeleccionadosTrabajo.
	 */
	public void contextMenu_verificarSeleccionadosCotizar() {
		String mensaje = "";
		// por si no esta vacio
		if (backlogsSeleccionadosTrabajo != null) {
			if (!backlogsSeleccionadosTrabajo.isEmpty()) {
				backlogsSeleccionadosTrabajo.clear();
			}
		}

		for (BacklogsMineros backlog : backlogsSeleccionados) {
			String estatus = backlog.getIdEstatusBacklogsMineros().getDescripcionEstatusBacklog();
			if (estatus.equals("COTIZAR")) {
				backlogsSeleccionadosTrabajo.add(backlog);
			}
		}

		if (backlogsSeleccionadosTrabajo.isEmpty()) {
			mensaje = "No hay backlogs con estatus POR COTIZAR seleccionados.";
			agregarMensajeWarn(summary, mensaje);
			return;
		}

		PrimeFaces.current().executeScript("PF('dialogoB2toB1').show();");
	}

	/***
	 * Al abrir el dialogo para pasar multiples backlogs a Autorizar, verifica que
	 * los BL seleccionados sean los correctos para pasar al siguiente estado, solo
	 * guarda los BL que son aptos para pasar al siguiente estado y los guarda en la
	 * lista backlogsSeleccionadosTrabajo.
	 */
	public void contextMenu_verificarSeleccionadosAutorizar() {
		String mensaje = "";
		// por si no esta vacio
		if (backlogsSeleccionadosTrabajo != null) {
			if (!backlogsSeleccionadosTrabajo.isEmpty()) {
				backlogsSeleccionadosTrabajo.clear();
			}
		}

		for (BacklogsMineros backlog : backlogsSeleccionados) {
			String estatus = backlog.getIdEstatusBacklogsMineros().getDescripcionEstatusBacklog();
			if (estatus.equals("AUTORIZAR")) {
				backlogsSeleccionadosTrabajo.add(backlog);
			}
		}

		if (backlogsSeleccionadosTrabajo.isEmpty()) {
			mensaje = "No hay backlogs con estatus Aurotizar seleccionados.";
			agregarMensajeWarn(summary, mensaje);
			return;
		}
		PrimeFaces.current().executeScript("PF('dialogoB1toER').show();");
	}

	/**
	 * 
	 */
	public void contextMenu_verificarAutorizadosReporte() {
		String mensaje = "";
		String estatusTemp = "";
		if (backlogsSeleccionados.isEmpty()) {
			mensaje = "No hay backlogs seleccionados.";
			agregarMensajeWarn(summary, mensaje);
			return;
		} else {
			List<BacklogsMineros> listaNueva = new ArrayList<BacklogsMineros>();
			for (BacklogsMineros bl : backlogsSeleccionados) {
				estatusTemp = bl.getIdEstatusBacklogsMineros().getIdEstatusBacklogMineroNoCode();
				if (estatusTemp.equals("POR AUTORIZAR")) {
					listaNueva.add(bl);
				}
			}
			backlogsSeleccionados = listaNueva;
		}
		PrimeFaces.current().executeScript("PF('dialogob1').show();");
	}

	/***
	 * sirve para deshabilitar la fila de un backlog ya autorizado en el dialogo de
	 * autorizar multiples backlogs
	 * 
	 * @param backlog backlog de la tabla
	 * @return true si ya fue autorizado, false si aun no
	 */
	public boolean deshabilitarFila(BacklogsMineros backlog) {
		boolean deshabilitar;
		deshabilitar = backlogsAutorizados.contains(backlog) || backlogsNoAutorizados.contains(backlog);
		return deshabilitar;
	}

	/***
	 * sirve para ocultar los campos de la fila de un backlog que fue no autorizado
	 * en el dialogo de autorizar multiples backlogs tambien se usa con el boton
	 * autorizar ya que son las mismas condiciones
	 * 
	 * @param backlog backlog de la tabla
	 * @return true si debe mostrar los campos, false si el backlog fue no
	 *         autorizado
	 */
	public boolean renderFila(BacklogsMineros backlog) {
		boolean render = backlogsNoAutorizados.contains(backlog);
		render = !render;
		return render;
	}

	/***
	 * borra los elementos de la lista de backlogs seleccionados
	 */
	public void limpiarSeleccion() {
		for (BacklogsMineros backlog : backlogsMinerosListFiltrada) {
			backlog.setCheckSeleccionado(false);
		}

		backlogsSeleccionadosTrabajo.clear();
		backlogsSeleccionados.clear();
		allCheckBacklogs = false;

		PrimeFaces.current().executeScript("PF('dialogoAtoB2').hide();");
		PrimeFaces.current().executeScript("PF('dialogoB2toB1').hide();");
		PrimeFaces.current().executeScript("PF('dialogoB1toER').hide();");
		PrimeFaces.current().executeScript("PF('dialogob1').hide();");
		// agregarMensaje(summary, "Es necesario actualizar la tabla para ver reflejados
		// los cambios.", "warn", true);

		obtenerBacklogs();
		obtenerCantidadBLRevisados();
	}

	/**
	 * Formatea el campo Numero cotizacion
	 */
	public void formatearCotizacion() {
		int diff = 0;
		// QT-0000000000
		// QT-13-000000
		String[] split = numeroCotizacion.split("-");
		// codigo para cananea y caborca
		split[2] = split[2].replace("_", "");
		if (split[2].length() < 7) {
			diff = 6 - split[2].length();
			for (int i = 0; i < diff; i++) {
				split[2] = "0" + split[2];
			}
			numeroCotizacion = split[0] + "-" + split[1] + "-" + split[2];
			numeroCotizacionSelect = numeroCotizacion;
		}
	}

	/**
	 * Se ejecuta este metodo cuando se edita una celda de la tabla refacciones Aqui
	 * se calcula el subtotal de la celda y el total de toda la tabla
	 * 
	 * @param event
	 */
	public void onRowEdit_DialogPasarCotizar(RowEditEvent event) {
		try {
			BacklogsMinerosDetalleRefa refaccion = ((BacklogsMinerosDetalleRefa) event.getObject());
			int cantidadRefaccion = refaccion.getCantidad();
			double precioRefaccion = refaccion.getPrecio();

			if (cantidadRefaccion > 0 && precioRefaccion > 0) {
				refaccion.setSubTotal(cantidadRefaccion * precioRefaccion);
			} else {
				refaccion.setPrecio(0.0);
				refaccion.setSubTotal(0.0);
			}

			// verificar el numero parte matco
			String numParteMatco = refaccion.getNumeroParte();

			boolean existe = analizarNumeroPMatco(numParteMatco);
			refaccion.setExisteNumeroParteMatco(existe);

			if (existe == false) {
				agregarMensajeWarn(summary, "El número parte MATCO agregado no existe!");
			}

			PrimeFaces.current().executeScript("PF('dialogoB2toB1').show();");

		} catch (Exception e) {
			agregarMensajeWarn(summary, "No se pudo calcular la refacción");
		}
	}

	public void dialogoB2toB1_cotizarBacklog(BacklogsMineros backlog) {

		BacklogsMinerosDto backlogMineroDto = new BacklogsMinerosDto();
		backlogMineroDto.asignarCamposByBacklog(backlog);

		// String numeroCotizacion = backlog.getNumeroCotizacion();

		if (numeroCotizacionSelect == null || numeroCotizacionSelect.equals("")) {
			agregarMensajeWarn(summary, "Número de cotización está vacio y se requiere para continuar.");
			return;
		}

		try {
			// asignarDetalleRefaBacklogMinero(backlog);
			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogMineroDto.setFechaCotizacion(new Date());
			backlogMineroDto.setNumeroCotizacion(numeroCotizacionSelect.toString());

			RevisionKardexEntity revisionBacklog = obtenerRevisionBacklogActual(backlog);
			if (revisionBacklog.getIdBacklogMinero() > 0) {
				if (revisionBacklog.getEstatusRevision() == 1 || revisionBacklog.getEstatusRevision() == 3) {
					agregarMensajeWarn("No se pudo actualizar el Backlog "
							+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ ". Actualmente se encuentra con una revisión.");
					PrimeFaces.current().ajax().update("growl");
					return;
				}
			}

			revisionBacklog.setFechaCierre(new Date());
			revisionFacade.modificarRevisionKardex(revisionBacklog);
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB2cambioB1(backlogMineroDto);

			this.agregarMensajeInfoKeep(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus POR COTIZAR a estatus POR AUTORIZAR.");

			backlog.setCheckSeleccionado(false);
			// removemos el backlog de la lista
			int len = backlogsSeleccionadosTrabajo.size();

			for (int i = 0; i < len; i++) {
				int idBLTabV = backlogsSeleccionadosTrabajo.get(i).getBacklogsMinerosKey().getIdBacklogMinero();
				int idBLSend = backlog.getBacklogsMinerosKey().getIdBacklogMinero();
				if (idBLTabV == idBLSend) {
					backlogsSeleccionadosTrabajo.remove(i);
					break;
				}
			}

			if (backlogsSeleccionadosTrabajo.isEmpty()) {
				PrimeFaces.current().executeScript("PF('dialogoB2toB1').hide();");
				redireccionar("menuBacklogs");
				// limpiarSeleccion();
				// obtenerBacklogs();
			}

			numeroCotizacionSelect = "";

		} catch (Exception e) {
			error = "No se pudo cambiar de estatus POR COTIZAR a POR AUTORIZAR";
			log.error(error, e);
			agregarMensajeErrorKeep(summary, error);
		}

	}

	/***
	 * Cambia el estado de POR AUTORIZAR a estatus ESPERA DE REFACCIONES en un
	 * backlog de la tabla de autorizacion multiple
	 * 
	 * @param backlog backlog a a autorizar
	 */
	public void dialogoB1toER_autorizar(BacklogsMineros backlog) {

		BacklogsMineros backlogCopi = backlog;
		String area = backlog.getArea();
		String autorizadoPor = backlog.getAutorizadoPor();
		String numeroReserva = backlog.getNumeroReserva();
		BacklogsMinerosDto backlogMineroDto = asignarCampos(backlog);
		boolean success = true;
		String estado = "";

		if (obtenerSucursalFiltro() == 6) {
			if (numeroReserva.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Número de reserva' está vacio y se requiere para continuar.");
				success = false;
			}
		}
		if (area.equals("") || autorizadoPor.equals("")) {
			if (area.equals("")) {
				agregarMensajeWarn(summary, "El campo 'área' está vacio y se requiere para continuar.");
			}
			if (autorizadoPor.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Autorizó' está vacio y se requiere para continuar.");
			}
			success = false;
		} else if (success) {
			try {
				backlogMineroDto.setUsuarioEstatusFinal(usuario);
				backlogMineroDto.setArea(area.toUpperCase());
				backlogMineroDto.setFechaProgTrabajo(null);
				backlogMineroDto.setAutorizadoPor(autorizadoPor.toUpperCase());

				if (obtenerSucursalFiltro() == 6) {
					backlogMineroDto.setNumeroReserva(numeroReserva.toUpperCase());
				}

				if (backlog.getListaRefacciones().isEmpty()) {
					estado = "POR EJECUTAR";
					backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioC(backlogMineroDto);
				} else {
					estado = "ESPERA DE REFACCIONES";
					backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioER(backlogMineroDto);
				}

				// backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioER(backlogMineroDto);

				this.agregarMensajeInfo(summary,
						"Se cambió el Folio "
								+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
								+ " de estatus POR AUTORIZAR a " + estado + ".");

				backlogsAutorizados.add(backlog);
				backlogCopi.setCheckSeleccionado(false);
				backlogsSeleccionados.remove(backlogCopi);
				backlogsSeleccionadosTrabajo.remove(backlogCopi);

				obtenerBacklogs();
			} catch (Exception e) {
				error = "No se pudo cambiar de estatus POR AUTORIZAR a " + estado;
				log.error(error, e);
				agregarMensajeErrorKeep(summary, error);
			}
		}

		if (backlogsSeleccionadosTrabajo.isEmpty()) {
			limpiarSeleccion();
		}

	}

	/**
	 * 
	 */
	public String cancelarBacklog() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);

		BacklogsMineros backlogEntity = seleccionBean.getBacklogsMinerosSeleccionado();
		BacklogsMinerosDto backlogMineroDto = new BacklogsMinerosDto();
		backlogMineroDto.setBacklogsMinerosKey(backlogEntity.getBacklogsMinerosKey());
		backlogMineroDto.setIdEstatusBacklogsMineros(backlogEntity.getIdEstatusBacklogsMineros());
		String estatusTmp = backlogMineroDto.getIdEstatusBacklogsMineros().getIdEstatusBacklogMineroNoCode();
		try {
			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogMineroDto.setComentarioBacklogMinero(comentarioCancelado.toUpperCase());
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroCancelar(backlogMineroDto);
			this.getFlash().setKeepMessages(true);
			this.agregarMensajeInfo(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus " + estatusTmp + " a CANCELADO.");

			limpiarSeleccion();

		} catch (Exception e) {
			error = "No se pudo cambiar al estatus CANCELADO";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return "";
	}

	/***
	 * crea un backlogMineroDto a partir de un backlog
	 * 
	 * @param backlog que se va a cambir de estado
	 * @return backlogDto
	 */
	public BacklogsMinerosDto asignarCampos(BacklogsMineros backlog) {
		BacklogsMinerosDto backlogMineroDto = new BacklogsMinerosDto();
		backlogMineroDto.setAprobadoPor(backlog.getAprobadoPor());
		backlogMineroDto.setArea(backlog.getArea());
		backlogMineroDto.setNumeroReserva(backlog.getNumeroReserva());
		backlogMineroDto.setAutorizadoPor(backlog.getAutorizadoPor());
		backlogMineroDto.setBacklogsMinerosKey(backlog.getBacklogsMinerosKey());
		backlogMineroDto.setComentarioBacklogMinero(backlog.getComentarioBacklogMinero());
		backlogMineroDto.setCotizado(backlog.isCotizado());
		backlogMineroDto.setCotizadoPor(backlog.getCotizadoPor());
		backlogMineroDto.setCreadoPor(backlog.getCreadoPor());
		backlogMineroDto.setFechaCotizacion(backlog.getFechaCotizacion());
		backlogMineroDto.setFechaHoraCerrado(backlog.getFechaHoraCerrado());
		backlogMineroDto.setFechaHoraCreacion(backlog.getFechaHoraCreacion());
		backlogMineroDto.setFechaProgTrabajo(backlog.getFechaProgTrabajo());
		backlogMineroDto.setHorasHombreEstimadas(backlog.getHorasHombreEstimadas());
		backlogMineroDto.setHorasHombreReales(backlog.getHorasHombreReales());
		backlogMineroDto.setHorasMaquinaEstimadas(backlog.getHorasMaquinaEstimadas());
		backlogMineroDto.setHorasMaquinaReales(backlog.getHorasMaquinaReales());
		backlogMineroDto.setHorometro(backlog.getHorometro());
		backlogMineroDto.setIdCargoTrabajo(backlog.getIdCargoTrabajo());
		backlogMineroDto.setIdCodigoSistema(backlog.getIdCodigoSistema());
		backlogMineroDto.setIdCodigoSMCS(backlog.getIdCodigoSMCS());
		backlogMineroDto.setIdMarca(backlog.getIdMarca());
		backlogMineroDto.setNumeroReserva(backlog.getNumeroReserva());
		backlogMineroDto.setTrabajoRealizado(backlog.getTrabajoRealizado());
		backlogMineroDto.setIdEstatusBacklogMinero(backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero());
		return backlogMineroDto;
	}

	/***
	 * Obtiene el excel de reporte de servicio de los backlogs seleccionados
	 * 
	 * @return
	 */
	public StreamedContent obtenerExcelReporte(String reporte) {
		GenerarExcel generarExcel = new GenerarExcel(backlogsSeleccionados);
		StreamedContent pdfExport = null;
		boolean existe = true;
		if (backlogsSeleccionados.size() > 0) {
			try {
				// verificar el numero parte matco
				for (BacklogsMineros bl : backlogsSeleccionados) {
					for (BacklogsMinerosDetalleRefa refacciones : bl.getListaRefacciones()) {
						String numParteMatco = refacciones.getNumeroParte();
						existe = analizarNumeroPMatco(numParteMatco);
						refacciones.setExisteNumeroParteMatco(existe);
						if (existe == false) {
							agregarMensajeWarn(summary, "No se encontró el Número parte MATCO: " + numParteMatco
									+ " para el Backlog: " + bl.getBacklogsMinerosKey().getBacklogsMineroFormateado());
						}
					}
				}

				pdfExport = generarExcel.generarExcelReporte(reporte);
				String sumario = "Generando Reporte.";
				agregarMensajeInfo(sumario);
				eliminarTemporales();

			} catch (Exception e) {
				String error = "No se pudo generar el excel.";
				this.agregarMensajeErrorKeep(error);
				log.error(e);
			}
		} else {
			String error = "No se pudo generar el excel.";
			this.agregarMensajeErrorKeep(error);
		}
		return pdfExport;
	}

	/***
	 * 
	 */
	public static void eliminarTemporales() {
		ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
		String rutaBin = externalContext.getInitParameter("rutaArchivosTemporales");
		System.out.println(rutaBin);
		try {
			File[] archivos = new File(rutaBin).listFiles(new FileFilter() {
				public boolean accept(File archivo) {
					if (archivo.isFile()) {
						String nombreArchivo = archivo.getName();
						boolean temporal = nombreArchivo.startsWith("Solicitud")
								|| nombreArchivo.startsWith("Servicio ");
						return temporal;
					}
					return false;
				}
			});
			for (File archivo : archivos) {
				if (!archivo.delete())
					archivo.deleteOnExit();
			}
		} catch (Exception e) {
			log.error(e);
		}

	}

	public void seleccionNumEconomico() {
		String numEcon = seleccionBean.getNumeroEconomico();
		String numSerie = seleccionBean.getNumeroSerie();
		String modelo = seleccionBean.getModelo();

		if (numEcon == null || numEcon.equals("")) {
			numSerie = null;
			return;
		}
		numSerie = this.obtenerMaquinariaPorNumEconomico(seleccionBean.getMaquinariaDtoList(), numEcon);
		modelo = obtenerModeloPorSerie(seleccionBean.getMaquinariaDtoList(), numSerie);

		seleccionBean.setNumeroSerie(numSerie);
		seleccionBean.setModelo(modelo);

		buscarCliente(seleccionBean.getMaquinariaDtoList(), numSerie);
	}

	/**
	 * Este metodo activa los campos para la captura del BL
	 */
	public void seleccionNumSerie() {
		String numSerie = seleccionBean.getNumeroSerie();
		String numEcon = seleccionBean.getNumeroEconomico();
		String modelo = seleccionBean.getModelo();

		if (numSerie == null || numSerie.equals("")) {
			numEcon = null;
			return;
		}

		numEcon = obtenerNumEconomicoPorSerie(seleccionBean.getMaquinariaDtoList(), numSerie);
		modelo = obtenerModeloPorSerie(seleccionBean.getMaquinariaDtoList(), numSerie);

		seleccionBean.setNumeroEconomico(numEcon);
		seleccionBean.setModelo(modelo);

		buscarCliente(seleccionBean.getMaquinariaDtoList(), numSerie);
	}

	public void seleccionModelo() {
		String numEcon = seleccionBean.getNumeroEconomico();
		String numSerie = seleccionBean.getNumeroSerie();
		String modelo = seleccionBean.getModelo();

		numSerie = genericBacklogBean.obtenerSeriePorModelo(seleccionBean.getMaquinariaDtoList(), modelo);
		numEcon = genericBacklogBean.obtenerNumEconomicoPorSerie(seleccionBean.getMaquinariaDtoList(), numSerie);

		seleccionBean.setNumeroSerie(numSerie);
		seleccionBean.setNumeroEconomico(numEcon);
	}

	public void abrirDialogoCancelar() {
		if (seleccionBean.getBacklogsMinerosSeleccionado() != null) {
			BacklogsMineros backlogtmp = seleccionBean.getBacklogsMinerosSeleccionado();
			String idEstatus = backlogtmp.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero();
			switch (idEstatus) {
			case "D":
			case "CA":
				agregarMensajeWarn(summary, "No se puede cancelar este backlog");
				break;
			default:
				PrimeFaces.current().executeScript("PF('dialogoCancelar').show();");
			}
		} else {
			agregarMensajeWarn(summary, "No se ha seleccionado un Backlog");
		}
	}

	public void obtenerHistorialBitacora() {
		try {
			bitacoraList = backlogsMinerosBitacoraEstatusFacade
					.obtenerHistorialBacklogs(seleccionBean.getSucursalFiltro());
		} catch (Exception e) {
			log.error("No se pudo obtener la bitacora", e);
		}
	}

	/**
	 * Regresa cual sera el estilo del menuItem
	 */
	public String obtenerEstiloARevisar() {
		if (contBlSolicitante > 0) {
			return "revisarBL1";
		}
		return "revisarBL0";
	}

	public String obtenerEstiloPorRevisar() {
		if (contBlCreador > 0) {
			return "revisarBL1";
		}
		return "revisarBL0";
	}

	/**
	 * Cambia el filtro de la tabla con una sucursal
	 */
	public void cambiarFiltro() {
		seleccionBean.setSucursalFiltro((short) sucursal);
	}

	/*** GETTERS AND SETTERS ***/
	public List<BacklogsMineros> getBacklogsMinerosListFiltrada() {
		return backlogsMinerosListFiltrada;
	}

	public void setBacklogsMinerosListFiltrada(List<BacklogsMineros> backlogsMinerosListFiltrada) {
		this.backlogsMinerosListFiltrada = backlogsMinerosListFiltrada;
	}

	public String[] getEstatusSeleccionados() {
		return estatusSeleccionados;
	}

	public void setEstatusSeleccionados(String[] estatusSeleccionados) {
		this.estatusSeleccionados = estatusSeleccionados;
	}

	public List<BacklogsMineros> getBacklogsMinerosList() {
		return backlogsMinerosList;
	}

	public void setBacklogsMinerosList(List<BacklogsMineros> backlogsMinerosList) {
		this.backlogsMinerosList = backlogsMinerosList;
	}

	public List<EstatusBacklogsMineros> getEstatusBacklogsMinerosList() {
		return estatusBacklogsMinerosList;
	}

	public void setEstatusBacklogsMinerosList(List<EstatusBacklogsMineros> estatusBacklogsMinerosList) {
		this.estatusBacklogsMinerosList = estatusBacklogsMinerosList;
	}

	public List<CodigosSMCS> getCodigosSMCSList() {
		return codigosSMCSList;
	}

	public void setCodigosSMCSList(List<CodigosSMCS> codigosSMCSList) {
		this.codigosSMCSList = codigosSMCSList;
	}

	public List<CodigosSistemas> getCodigosSistemasList() {
		return codigosSistemasList;
	}

	public void setCodigosSistemasList(List<CodigosSistemas> codigosSistemasList) {
		this.codigosSistemasList = codigosSistemasList;
	}

	public List<BacklogsMineros> getBacklogsSeleccionados() {
		return backlogsSeleccionados;
	}

	public void setBacklogsSeleccionados(List<BacklogsMineros> backlogsSeleccionados) {
		this.backlogsSeleccionados = backlogsSeleccionados;
	}

	public List<BacklogsMineros> getBacklogsAutorizados() {
		return backlogsAutorizados;
	}

	public void setBacklogsAutorizados(List<BacklogsMineros> backlogsAutorizados) {
		this.backlogsAutorizados = backlogsAutorizados;
	}

	public List<BacklogsMineros> getBacklogsNoAutorizados() {
		return backlogsNoAutorizados;
	}

	public void setBacklogsNoAutorizados(List<BacklogsMineros> backlogsNoAutorizados) {
		this.backlogsNoAutorizados = backlogsNoAutorizados;
	}

	public List<BacklogsMineros> getBacklogsCotizados() {
		return backlogsCotizados;
	}

	public void setBacklogsCotizados(List<BacklogsMineros> backlogsCotizados) {
		this.backlogsCotizados = backlogsCotizados;
	}

	public List<BacklogsMinerosDetalleRefa> getBacklogsMinerosDetalleRefaList() {
		return backlogsMinerosDetalleRefaList;
	}

	public void setBacklogsMinerosDetalleRefaList(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		this.backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaList;
	}

	public List<RevisionKardexEntity> getRevisionCreadorBacklogsList() {
		return revisionCreadorBacklogsList;
	}

	public void setRevisionCreadorBacklogsList(List<RevisionKardexEntity> revisionCreadorBacklogsList) {
		this.revisionCreadorBacklogsList = revisionCreadorBacklogsList;
	}

	public List<RevisionKardexEntity> getRevisionSolicitanteBacklogsList() {
		return revisionSolicitanteBacklogsList;
	}

	public void setRevisionSolicitanteBacklogsList(List<RevisionKardexEntity> revisionSolicitanteBacklogsList) {
		this.revisionSolicitanteBacklogsList = revisionSolicitanteBacklogsList;
	}

	public DualListModel<EstatusBacklogsMineros> getEstadosPickList() {
		return estadosPickList;
	}

	public void setEstadosPickList(DualListModel<EstatusBacklogsMineros> estadosPickList) {
		this.estadosPickList = estadosPickList;
	}

	public List<BacklogsMineros> getBacklogsSeleccionadosTrabajo() {
		return backlogsSeleccionadosTrabajo;
	}

	public void setBacklogsSeleccionadosTrabajo(List<BacklogsMineros> backlogsSeleccionadosTrabajo) {
		this.backlogsSeleccionadosTrabajo = backlogsSeleccionadosTrabajo;
	}

	public String getNumeroCotizacion() {
		return numeroCotizacion;
	}

	public void setNumeroCotizacion(String numeroCotizacion) {
		this.numeroCotizacion = numeroCotizacion;
	}

	public boolean isAllCheckBacklogs() {
		return allCheckBacklogs;
	}

	public void setAllCheckBacklogs(boolean allCheckBacklogs) {
		this.allCheckBacklogs = allCheckBacklogs;
	}

	public boolean isCheckedBacklogs() {
		return checkedBacklogs;
	}

	public void setCheckedBacklogs(boolean checkedBacklogs) {
		this.checkedBacklogs = checkedBacklogs;
	}

	public int getContBlCreador() {
		return contBlCreador;
	}

	public void setContBlCreador(int contBlCreador) {
		this.contBlCreador = contBlCreador;
	}

	public int getContBlSolicitante() {
		return contBlSolicitante;
	}

	public void setContBlSolicitante(int contBlSolicitante) {
		this.contBlSolicitante = contBlSolicitante;
	}

	public List<BacklogsMinerosBitacoraEstatus> getBitacoraList() {
		return bitacoraList;
	}

	public void setBitacoraList(List<BacklogsMinerosBitacoraEstatus> bitacoraList) {
		this.bitacoraList = bitacoraList;
	}

	public int getSucursal() {
		return sucursal;
	}

	public void setSucursal(int sucursal) {
		this.sucursal = sucursal;
	}

	public String getComentarioCancelado() {
		return comentarioCancelado;
	}

	public void setComentarioCancelado(String comentarioCancelado) {
		this.comentarioCancelado = comentarioCancelado;
	}

	public String getNumeroMatco() {
		return numeroMatco;
	}

	public void setNumeroMatco(String numeroMatco) {
		this.numeroMatco = numeroMatco;
	}

}
