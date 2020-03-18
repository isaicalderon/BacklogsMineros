package com.matco.backlogs.bean;

import java.io.File;
import java.io.FileFilter;
import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.StreamedContent;
import com.matco.amce3.dto.BacklogsMinerosDto;
import com.matco.amce3.facade.BacklogsMinerosBitacoraEstatusFacade;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.amce3.facade.ImagenesBacklogsMinerosFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.RevisionKardexFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.ImagenesBacklogsMineros;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.RevisionKardexEntity;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.ejes.entity.Marca;
import com.matco.servicio.entity.Tecnico;

/***
 * Administración de datos que influyen en el proceso de cambio de estatus de
 * backlogs
 * 
 * @author N soluciones de software
 *
 */
@ManagedBean(name = "cambioEstatusBacklogsMineros")
@ViewScoped
public class CambioEstatusBacklogsMinerosBean extends GenericBacklogBean implements Serializable {
	private static final long serialVersionUID = -6107398132778226716L;
	private static final Logger log = Logger.getLogger(CambioEstatusBacklogsMinerosBean.class);
	private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	private boolean success = true;
	private boolean renderGaleria = true;
	private boolean btnRevision = false;
	private boolean piezasSurtidas = false;
	private boolean fechaEjecutarProgramada = false;
	private boolean porAutorizar = false;
	private Date fechaCreacion;
	private Date fechaEjecutar; // programado para ejecutar
	private Date fechaEjecucion; // cuando se ejecuto
	private Date lastFechaLlegada = null;
	private Double horasHombre;
	private Double horasMaquina;
	private Double totalRefacciones;
	private String numeroCotizacion;
	private String autorizadoPor;
	private String area;
	private String folio;
	private String trabajoRealizado;
	private String numeroReserva;
	private String comentario;
	private String aprobadoPor;
	private String supervisorACargo;
	private String mostrarInformacion = "";
	private String mostrarGaleria = "display:none;";
	private String error;
	private String usuario;
	private String detalleRevision;
	private String urlReporte;
	private Sintomas sintomasObject;
	private BacklogsStaticsVarBean seleccionBean;
	private LoginBean loginBean = this.obtenerBean("loginBean");
	private BacklogsMineros backlogMineroSeleccionado;
	private EstatusBacklogsMineros estatusBacklogsMineros;
	private RevisionKardexEntity revisionBacklog;
	private Tecnico tecnicoEjecutor = new Tecnico();
	private LadoComponente ladoComponente = new LadoComponente();
	private List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();
	private List<String> listaImagenes = new ArrayList<>();
	private List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<>();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();

	@PostConstruct
	public void init() {
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		
		backlogMineroSeleccionado = seleccionBean.getBacklogsMinerosSeleccionado();

		if (backlogMineroSeleccionado == null) {
			redireccionar("menuBacklogs");
			return;
		}

		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		
		int idBacklog = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
		int almacen = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen().intValue();
		
		urlReporte = "http://" + serverUrlServices + "/BacklogsMinerosReportes/servlet/BacklogsMineroServlet?PIDBACKLOGMINERO="
				+ idBacklog + "&PIDALMACEN=" + almacen;
		
		sintomasObject = backlogMineroSeleccionado.getSintoma();
		if (sintomasObject.getIdCodigoSintoma() == null) {
			sintomasObject.setDescripcionSintoma("");
			sintomasObject.setIdCodigoSintoma("");
		}

		folio = backlogMineroSeleccionado.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
		fechaCreacion = backlogMineroSeleccionado.getFechaHoraCreacion();
		estatusBacklogsMineros = backlogMineroSeleccionado.getIdEstatusBacklogsMineros();
		fechaEjecutar = backlogMineroSeleccionado.getFechaProgTrabajo();
		if (fechaEjecutar != null) {
			fechaEjecutarProgramada = true;
		}

		backlogsMinerosDetalleRefaList = listarBacklogsMinerosDetalleRefa();
		generarSubtotalRefacciones(backlogsMinerosDetalleRefaList);

		if (backlogsMinerosDetalleRefaList.isEmpty()) {
			porAutorizar = true;
		}

		riesgosTabajosList = obtenerRiesgosTrabajo();

		// buscar lado del componente
		String codigoLDBL = backlogMineroSeleccionado.getLadoComponenteOb().getCodigoLDC();
		String descripcionLDBL = backlogMineroSeleccionado.getLadoComponenteOb().getDescripcion();
		if (codigoLDBL != null) {
			LadoComponenteFacade ladoComponenteFacade = new LadoComponenteFacade(RUTA_PROPERTIES_AMCE3);
			try {
				ladoComponenteList = ladoComponenteFacade.obtenerTodosLDC();
				for (LadoComponente ld : ladoComponenteList) {
					if (ld.getCodigoLDC().equals(codigoLDBL)) {
						ladoComponente.setCodigoLDC(codigoLDBL);
						ladoComponente.setDescripcion(ld.getDescripcion());
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			ladoComponente.setCodigoLDC("");
			ladoComponente.setDescripcion(descripcionLDBL);
		}
		
		totalRefacciones = calcularTotalRefacciones();
		revisionBacklog = obtenerRevisionBacklogActual();

		if (revisionBacklog.getIdBacklogMinero() > 0) {
			if (revisionBacklog.getEstatusRevision() == 1 || revisionBacklog.getEstatusRevision() == 3) {
				btnRevision = true;
				agregarMensajeWarn("Actualmente se encuentra una revisión para este Backlog");
				PrimeFaces.current().ajax().update("growl");
			}
		}

		piezasSurtidas = true;
		for (BacklogsMinerosDetalleRefa forRefa : backlogMineroSeleccionado.getListaRefacciones()) {
			if (!forRefa.isSurtido()) {
				piezasSurtidas = false;
				break;
			}
		}
	}

	/**
	 * Verifica que el ROL tenga permiso para acceder a la vista
	 * 
	 * @param permiso
	 */
	public void verificarPermiso(String permiso) {
		boolean warn = false;

		switch (permiso) {
		case "POR-AUTORIZAR":
			warn = tienePermiso("ESTATUS B1-C");
			break;
		}

		if (warn == false) {
			agregarMensaje(summary, "No tienes permisos suficientes para ingresar al estatus.", "warn", true);
			PrimeFaces.current().ajax().update("growl");
			redireccionar("menuBacklogs");
		}
	}

	/**
	 * Esta funcion sirve para pasar del estatus CREADO a POR AUTORIZAR, siempre y
	 * cuando el Backlog no cuente con ninguna refacción
	 */
	public String porAutorizarSinRefacciones() {
		if (isPorAutorizar() == true) {
			numeroCotizacion = "0";
			mandarParaCotizacionCompletada();
		}
		if (isSuccess() == true) {
			return "backlogsLista";
		}
		return "";
	}

	/**
	 * Se obtiene el url del reporte de autorizacion Y redirecciona
	 */
	public String obtenerReporte() {

		PrimeFaces.current().executeScript("abrirTab();");

		return urlReporte;

		/*
		 * FacesContext contex = FacesContext.getCurrentInstance(); try {
		 * contex.getExternalContext().redirect(urlReporte); contex.responseComplete();
		 * } catch (IOException e) { log.error(e); }
		 */
	}

	/**
	 * Funcion para la tabla de refacciones del estatus ESPERA DE REFACCIONES
	 * Analiza la celda editada y agrega la fecha estimada de entrega de la pieza y
	 * el surtido de la pieza
	 * 
	 * @param Celda del listado de refacciones
	 */
	public void onRowEditER(RowEditEvent event) {
		BacklogsMinerosDetalleRefa refaccion = ((BacklogsMinerosDetalleRefa) event.getObject());
		BacklogsMinerosDetalleRefaFacade refaccionFacade = new BacklogsMinerosDetalleRefaFacade(RUTA_PROPERTIES_AMCE3);

		Date fechaLlegadaEstimada = refaccion.getFechaLlegadaEstimada();

		if (fechaLlegadaEstimada != null) {
			try {
				if (fechaLlegadaEstimada != lastFechaLlegada) {
					refaccionFacade.modificarFechaLlegadaEstimada(refaccion);
					this.agregarMensajeInfo(summary, "Se ha agregado la fecha de llegada estimada al número parte: "
							+ refaccion.getNumeroParte());
					lastFechaLlegada = fechaLlegadaEstimada;
				}
			} catch (Exception e) {
				String error = "No se pudo modificar la fecha llegada estimada!";
				this.agregarMensajeError(error);
				log.error(e);
			}
		}

		Date fechaLlegadaReal = null;
		if (refaccion.isSurtido()) {
			fechaLlegadaReal = new Date();
		}

		try {
			refaccion.setFechaLlegadaReal(fechaLlegadaReal);
			refaccionFacade.modificarSurtirRefaccion(refaccion);
			if (refaccion.isSurtido()) {
				this.agregarMensajeInfo(summary, "Se surtió la refacción correctamente!");
			}
		} catch (Exception e) {
			String error = "No se pudo surtir la refacción!";
			this.agregarMensajeError(error);
			log.error(e);
		}

		// VERIFICAR LAS PIEZAS SURTIDAS
		piezasSurtidas = true;
		for (BacklogsMinerosDetalleRefa forRefa : backlogMineroSeleccionado.getListaRefacciones()) {
			if (!forRefa.isSurtido()) {
				piezasSurtidas = false;
				break;
			}
		}
	}

	/**
	 * Envia los datalles del backlog, si el backlog ya tiene revision pasara a 1 si
	 * el backlog existe verifica el estatus y edita la revision
	 */
	public String enviarDetalles() {
		RevisionKardexFacade facade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
		RevisionKardexEntity revision = new RevisionKardexEntity();
		try {
			int idBacklogMinero = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
			int idAlmacen = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
			String usuarioCreador = backlogMineroSeleccionado.getCreadoPor();
			revision.setComentariosSolicitante(detalleRevision.toUpperCase());
			revision.setUsuarioSolicitante(usuario);
			revision.setEstatusRevision(1);
			revision.setIdBacklogMinero(idBacklogMinero);
			revision.setIdAlmacen(idAlmacen);
			revision.setUsuarioCreador(usuarioCreador);
			if (revisionBacklog.getIdBacklogMinero() == 0) {
				facade.agregarRevisionKardex(revision);
			} else if (revisionBacklog.getEstatusRevision() == 0 || revisionBacklog.getEstatusRevision() == 2) {
				revision.setIdRevisionKardex(revisionBacklog.getIdRevisionKardex());
				revision.setComentariosCreador("");
				revision.setFechaCierre(null);
				facade.modificarRevisionKardex(revision);
			}
			agregarMensajeInfo(summary, "Se ha enviado la revisión correctamente");
			detalleRevision = "";
			btnRevision = true;
			return "backlogsLista";

			// revisionBacklog = obtenerRevisionBacklogActual();
		} catch (Exception e) {
			String error = "No se pudo agregar la revision";
			this.agregarMensajeError(error);
			log.error(e);
		}
		return "";
	}

	/**
	 * 
	 * @return
	 */
	public RevisionKardexEntity obtenerRevisionBacklogActual() {
		RevisionKardexFacade facade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
		RevisionKardexEntity revision = new RevisionKardexEntity();
		try {
			
			int idBacklogMinero = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
			int idAlmacen = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
			revision.setIdBacklogMinero(idBacklogMinero);
			revision.setIdAlmacen(idAlmacen);
			revisionBacklog = facade.obtenerRevisionByIDBL(revision);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return revisionBacklog;
	}

	/**
	 * Obtiene todos los riesgos de trabajo
	 * 
	 * @return List<RiesgosTrabajo>
	 */
	public List<RiesgosTrabajo> obtenerRiesgosTrabajo() {
		SintomasYRiesgosFacade riesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
		RiesgosTrabajo riesgo = new RiesgosTrabajo();
		int idBacklog = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
		int idAlmacen = (int) backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
		riesgo.setIdBacklog(idBacklog);
		riesgo.setIdAlmacen(idAlmacen);
		try {
			riesgosTabajosList = riesgosFacade.getRiesgosDeunBacklog(riesgo);
		} catch (Exception e2) {
			String error = "No se pudieron obtener los riesgos";
			this.agregarMensajeError(error);
			log.error(e2);
		}
		return riesgosTabajosList;
	}

	/**
	 * Obtiene el subtotal de todas las refacciones y las guarda en una variable
	 * llamada total
	 * 
	 * @return double
	 */
	public Double calcularTotalRefacciones() {
		double total = 0;
		for (BacklogsMinerosDetalleRefa refaccionList : backlogsMinerosDetalleRefaList) {
			total += refaccionList.getSubTotal();
		}
		return total;
	}

	public StreamedContent obtenerExcelReporte(String reporte) {
		GenerarExcel generarExcel = new GenerarExcel(backlogMineroSeleccionado);
		StreamedContent pdfExport = null;
		boolean existe = true;
		try {
			// verificar el numero parte matco
			for (BacklogsMinerosDetalleRefa refacciones : backlogMineroSeleccionado.getListaRefacciones()) {
				String numParteMatco = refacciones.getNumeroParte();
				existe = analizarNumeroPMatco(numParteMatco);
				refacciones.setExisteNumeroParteMatco(existe);
				if (existe == false) {
					agregarMensajeWarn(summary, "No se encontró el Número parte MATCO: " + numParteMatco);
				}
			}

			pdfExport = generarExcel.generarExcelReporte(reporte);
			String sumario = "Generando Reporte.";
			agregarMensajeInfo(sumario);
			eliminarTemporales();

		} catch (Exception e) {
			String error = "No se pudo generar el excel.";
			this.agregarMensajeError(error);
			log.error(e);
		}

		return pdfExport;
	}

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
			System.out.println("Hubo un problema al tratar de borrar archivos temporales.");
		}

	}

	/***
	 * Obtiene el estatus del backlog seleccionado para que aparezca en la barra de
	 * navegación, ya que los estatus M y A usan la misma vista
	 * 
	 * @return
	 */
	public String obtenerNombreEstatus() {
		if (estatusBacklogsMineros == null) {
			return "";
		}
		String estatusBacklog = estatusBacklogsMineros.getIdEstatusBacklogMineroNoCode();
		// capitalizar
		estatusBacklog = estatusBacklog.substring(0, 1).toUpperCase() + estatusBacklog.substring(1).toLowerCase();
		estatusBacklog = "Estatus " + estatusBacklog;
		return estatusBacklog;
	}

	/***
	 * Verifica si se debe mostrar el botón de mandar backlog a monitoreo, esto es,
	 * siempre y cuando el backlog no este ya en monitoreo y se tenga permiso para
	 * mandar a monitoreo
	 * 
	 * @return Si esta en monitoreo regresa false, caso contrario regresa true
	 */
	public boolean mostrarBotonMonitoreando() {
		if (estatusBacklogsMineros == null) {
			return false;
		}
		String estatus = estatusBacklogsMineros.getDescripcionEstatusBacklog();
		boolean permiso = tienePermiso("ESTATUS A-M");
		boolean resultado = !estatus.equals("MONITOREAR") && permiso;
		return resultado;
	}

	/***
	 * Si se esta viendo un backlog en estatus M y no se tiene permiso para sacarlo
	 * de ese estatus ocultar el boton o si esta en A y no se tiene permiso para
	 * pasarlo a B2
	 * 
	 * @return
	 */
	public boolean mostrarBotonMandarCotizar() {
		if (estatusBacklogsMineros == null) {
			return false;
		}
		String estatus = estatusBacklogsMineros.getDescripcionEstatusBacklog();
		boolean permiso = tienePermiso("ESTATUS A-M");
		boolean mostrar = true;
		if (!permiso && estatus.equals("MONITOREAR")) {
			mostrar = false;
		} else {
			permiso = tienePermiso("ESTATUS A-B2");
			if (estatus.equals("CREADO") && !permiso) {
				mostrar = false;
			}
		}
		return mostrar;
	}

	/***
	 * Verifica si se debe mostrar el comentario (C2)
	 * 
	 * @return true si el estado es No ejecutado, false en caso contrario
	 */
	public boolean mostrarComentario() {
		if (estatusBacklogsMineros == null) {
			return false;
		}
		String estatus = estatusBacklogsMineros.getDescripcionEstatusBacklog();
		boolean resultado = estatus.equals("NO EJECUTADO");
		return resultado;
	}

	/***
	 * Obtiene el color de la celda Estatus BL
	 * 
	 * @return el estilo para que la celda tenga el color de background
	 *         correspondiente
	 */
	public String obtenerColorCelda() {
		if (estatusBacklogsMineros == null) {
			return "";
		}
		String estatusBacklog = estatusBacklogsMineros.getIdEstatusBacklogMineroNoCode();
		if (estatusBacklog.equals("CREADO")) {// A
			return "background-color:#a80000;color:white";
		} else if (estatusBacklog.equals("POR COTIZAR")) {// B2
			return "background-color:#ffc000;color:white";
		} else if (estatusBacklog.equals("POR AUTORIZAR")) {// B1
			return "background-color:#1f4e78;color:white";
		} else if (estatusBacklog.equals("POR EJECUTAR")) {// C
			return "background-color:#c05710;color:white";
		} else if (estatusBacklog.equals("EJECUTADO")) {// D
			return "background-color:#548235;color:white";
		} else if (estatusBacklog.equals("MONITOREAR")) {// M
			return "background-color:#ee8800;color:white";
		} else if (estatusBacklog.equals("NO AUTORIZADO")) {// B3
			return "background-color:#ff4747;color:white";
		} else if (estatusBacklog.equals("NO EJECUTADO")) {// C2
			return "background-color:#c05710;color:white";
		} else if (estatusBacklog.equals("ESPERA DE REFACCIONES")) {// espera ref
			return "background-color:#1096a4;color:white";
		} else if (estatusBacklog.equals("POSPUESTO")) {// pospuesto
			return "background-color:#f19759;color:white";
		} else if (estatusBacklog.equals("CANCELADO")) {// cancelado
			return "background-color:#a6a6a6;color:white";
		}
		return "";
	}

	/**
	 * Esta función cambia del estatus ESPERA DE REFACCIONEs al estatus POR EJECUTAR
	 */
	public String porEjecutar() {
		BacklogsMinerosBitacoraEstatusFacade bitacoraFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);

		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		if (piezasSurtidas == true) {
			try {
				bitacoraFacade.cambioEstatusBacklogMineroERcambioC(backlogMineroDto);
				agregarMensajeInfo(summary, "Se ha cambiado el estado de ESPERA DE REFACCIONES a POR EJECUTAR");
				return "backlogsLista";
			} catch (Exception e) {
				error = "No se pudo cambiar de estatus ESPERA DE REFACCIONES a POR EJECUTAR";
				log.error(error, e);
				agregarMensajeError(summary, error);
				setSuccess(false);
			}
		}
		return "";
	}
	
	
	public void analizarCambioEstado() {
		
		
		
	}

	public void mandarParaCotizacion() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		RevisionKardexFacade revisionFacade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);

		BacklogsMinerosDto backlogMineroDto = asignarCampos();
		if (obtenerSucursalFiltro() == 13) {
			if (numeroReserva.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Orden de trabajo' está vacio y se requiere para continuar.");
				return;
			}
		}

		try {
			// cerramos la revision
			RevisionKardexEntity revision = revisionBacklog;
			if (revision.getEstatusRevision() == 0 || revision.getEstatusRevision() == 2) {
				revision.setFechaCierre(new Date());

				revisionFacade.modificarRevisionKardex(revision);

				backlogMineroDto.setUsuarioEstatusFinal(usuario);
				if (obtenerSucursalFiltro() == 13) {
					BacklogsMinerosKey blmKey;
					blmKey = backlogsMinerosFacade.obtenerNumeroReserva(numeroReserva);

					if (blmKey.getIdBacklogMinero() != null) {
						if (blmKey.getIdAlmacen().getAlmacen() == 13) {
							agregarMensaje(summary,
									"Actualmente existe un Backlog registrado con el mismo número de reserva.", "warn",
									false);
							return;
						}
					}

					backlogMineroDto.setNumeroReserva(numeroReserva.toUpperCase());
				}
				backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroAcambioB2(backlogMineroDto);
				String msj = "Se cambió el Folio "
						+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado() + " de estatus "
						+ backlogMineroDto.getIdEstatusBacklogMinero() + " a estatus POR COTIZAR.";
				agregarMensaje(summary, msj, "info", true);

				redireccionar("menuBacklogs");

			} else {
				agregarMensaje(summary,
						"No se puede enviar a cotizar porque actualmente existe una revision para este Backlog",
						"alert", true);
			}

		} catch (Exception e) {
			error = "No se pudo cambiar de estatus " + backlogMineroDto.getIdEstatusBacklogMinero() + " a POR COTIZAR";
			log.error(error, e);
			agregarMensaje(summary, error, "error", true);
		}

	}

	public String revisarCotizacion() {
		RevisionKardexFacade facade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
		RevisionKardexEntity revision = new RevisionKardexEntity();
		try {
			int idBacklogMinero = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
			int idAlmacen = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
			String usuarioCreador = backlogMineroSeleccionado.getCreadoPor();
			revision.setComentariosSolicitante(detalleRevision.toUpperCase());
			revision.setUsuarioSolicitante(usuario);
			revision.setEstatusRevision(3);
			revision.setIdBacklogMinero(idBacklogMinero);
			revision.setIdAlmacen(idAlmacen);
			revision.setUsuarioCreador(usuarioCreador);

			if (revisionBacklog.getIdBacklogMinero() == 0 || revisionBacklog.getEstatusRevision() == 2) {

				facade.agregarRevisionKardex(revision);
			} else if (revisionBacklog.getEstatusRevision() == 4) {
				revision.setComentariosCreador("");
				revision.setFechaCierre(null);
				facade.modificarRevisionKardex(revision);
			}

			agregarMensajeInfo(summary, "Se ha enviado la revisión correctamente");
			detalleRevision = "";
			btnRevision = true;
			return "backlogsLista";

			// revisionBacklog = obtenerRevisionBacklogActual();
		} catch (Exception e) {
			String error = "No se pudo agregar la revision";
			this.agregarMensajeError(error);
			log.error(e);
		}
		return "";
	}

	public void mandarParaMonitoreo() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		try {

			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroAcambioM(backlogMineroDto);
			this.getFlash().setKeepMessages(true);
			this.agregarMensajeInfo(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus CREADO a estatus MONITOREAR.");
			setSuccess(true);
		} catch (Exception e) {
			error = "No se pudo cambiar de estatus CREADO a MONITOREAR";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	public void mandarParaCotizacionCompletada() {

		if (numeroCotizacion.equals("")) {
			agregarMensajeWarn(summary, "Número de cotización está vacio y se requiere para continuar.");
			return;
		}

		if (!verificarPartesRequeridas()) {

			BacklogsMinerosDto backlogMineroDto = asignarCampos();
			RevisionKardexEntity revision = revisionBacklog;

			if (revision.getEstatusRevision() == 0 || revision.getEstatusRevision() == 2
					|| revision.getEstatusRevision() == 4) {

				revision.setFechaCierre(new Date());

				try {
					revisionFacade.modificarRevisionKardex(revision);
					backlogMineroDto.setUsuarioEstatusFinal(usuario);
					backlogMineroDto.setFechaCotizacion(new Date());
					backlogMineroDto.setNumeroCotizacion(numeroCotizacion.toString());
					backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB2cambioB1(backlogMineroDto);

					this.getFlash().setKeepMessages(true);

					if (porAutorizar == true) {
						this.agregarMensajeInfo(summary,
								"Se cambió el Folio "
										+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
										+ " de estatus CREADO a estatus POR AUTORIZAR.");
					} else {
						this.agregarMensajeInfo(summary,
								"Se cambió el Folio "
										+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
										+ " de estatus POR COTIZAR a estatus POR AUTORIZAR.");
					}
					asignarDetalleRefa(backlogMineroSeleccionado);
					
					redireccionar("menuBacklogs");

				} catch (Exception e) {
					error = "No se pudo cambiar de estatus POR COTIZAR a POR AUTORIZAR";
					log.error(error, e);
					agregarMensajeError(summary, error);
				}
			} else {
				agregarMensajeWarn(summary,
						"No se puede enviar a cotizar porque actualmente existe una revision para este Backlog");
			}
		}

	}

	private boolean verificarPartesRequeridas() {
		if (!backlogsMinerosDetalleRefaList.isEmpty()) {
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				boolean campoFaltante = ((backlogsMinerosDetalleRefa.getCantidad() == null)
						|| backlogsMinerosDetalleRefa.getDescripcionParte() == null
						|| backlogsMinerosDetalleRefa.getDescripcionParte().equals("")
						|| backlogsMinerosDetalleRefa.getNumeroParte() == null
						|| backlogsMinerosDetalleRefa.getNumeroParte().equals("")) ? true : false;
				backlogsMinerosDetalleRefa.setCamposFaltantes(campoFaltante);
			}

			boolean falta = false;
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				falta = backlogsMinerosDetalleRefa.isCamposFaltantes();
				if (falta == true) {
					agregarMensajeWarn(summary,
							"Verifique que todas las partes requeridas contengan los campos requeridos.");
					break;
				}
			}
			return falta;
		}
		return false;
	}

	public void asignarDetalleRefa(BacklogsMineros backlog) {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
		try {
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				Marca idMarca = backlog.getIdMarca();
				backlogsMinerosDetalleRefa.setIdMarca(idMarca);
				backlogsMinerosDetalleRefa.setPrecio(backlogsMinerosDetalleRefa.getPrecio());// No importa ahora mismo.
				backlogsMinerosDetalleRefa.setTotal(backlogsMinerosDetalleRefa.getTotal());
				String numPart = backlogsMinerosDetalleRefa.getNumeroParte().toUpperCase();
				while (numPart.length() < 7) {
					numPart = "0" + numPart;
				}
				backlogsMinerosDetalleRefa.setNumeroParte(numPart);
				String descripcionParte = backlogsMinerosDetalleRefa.getDescripcionParte().toUpperCase();
				backlogsMinerosDetalleRefa.setDescripcionParte(descripcionParte);
				String observaciones = backlogsMinerosDetalleRefa.getObservaciones();
				if (observaciones != null) {
					observaciones = observaciones.toUpperCase();
				}
				backlogsMinerosDetalleRefa.setObservaciones(observaciones);
				backlogsMinerosDetalleRefaFacade.editarBacklogMinerosDetalleRefa(backlogsMinerosDetalleRefa);

			}
			setSuccess(true);
		} catch (Exception e) {
			error = "No se pudo guardar el Detalle Refacciones del "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	public void completarAutorizacion() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);

		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		if (obtenerSucursalFiltro() == 6) {
			if (numeroReserva.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Número de reserva' está vacio y se requiere para continuar.");
				return;
			}
		}

		if (area.equals("") || autorizadoPor.equals("")) {
			if (area.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Área' está vacio y se requiere para continuar.");
			}
			if (autorizadoPor.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Autorizó' está vacio y se requiere para continuar.");
			}
		} else {
			try {
				backlogMineroDto.setUsuarioEstatusFinal(usuario);
				backlogMineroDto.setArea(area.toUpperCase());
				backlogMineroDto.setFechaProgTrabajo(null);
				backlogMineroDto.setAutorizadoPor(autorizadoPor.toUpperCase());

				if (obtenerSucursalFiltro() == 6) {
					BacklogsMinerosKey blmKey;
					blmKey = backlogsMinerosFacade.obtenerNumeroReserva(numeroReserva);

					if (blmKey.getIdBacklogMinero() != null) {

						if (blmKey.getIdAlmacen().getAlmacen() == 6) {
							agregarMensaje(summary,
									"Actualmente existe un Backlog registrado con el mismo número de reserva.", "warn",
									false);
						}
						return;
					}

					backlogMineroDto.setNumeroReserva(numeroReserva.toUpperCase());
				}

				String estado = "";

				if (backlogMineroSeleccionado.getListaRefacciones().isEmpty()) {
					estado = "POR EJECUTAR";
					backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioC(backlogMineroDto);
				} else {
					estado = "ESPERA DE REFACCIONES";
					backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioER(backlogMineroDto);
				}

				this.getFlash().setKeepMessages(true);

				this.agregarMensajeInfo(summary,
						"Se cambió el Folio "
								+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
								+ " de estatus POR AUTORIZAR a " + estado + ".");

				redireccionar("menuBacklogs");

			} catch (Exception e) {
				error = "No se pudo cambiar de estatus POR AUTORIZAR a ESPERA DE REFACCIONES";
				log.error(error, e);
				agregarMensajeError(summary, error);
			}
		}
	}

	public void denegarAutorizacion() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		try {
			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB1cambioA1(backlogMineroDto);
			this.getFlash().setKeepMessages(true);
			this.agregarMensajeInfo(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus POR AUTORIZAR a estatus NO AUTORIZADO.");
			setSuccess(true);
		} catch (Exception e) {
			error = "No se pudo cambiar de estatus POR AUTORIZAR a NO AUTORIZADO";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	/*
	 * public void cotizacionIncompleta() { BacklogsMinerosBitacoraEstatusFacade
	 * backlogsMinerosBitacoraEstatusFacade = new
	 * BacklogsMinerosBitacoraEstatusFacade( RUTA_PROPERTIES_AMCE3);
	 * BacklogsMinerosDto backlogMineroDto = asignarCampos();
	 * 
	 * try { backlogMineroDto.setUsuarioEstatusFinal(usuario);
	 * backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroB2cambioA2(
	 * backlogMineroDto); this.getFlash().setKeepMessages(true);
	 * this.agregarMensajeInfo(summary, "Se cambió el Folio " +
	 * backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
	 * + " de estatus B2 a estatus A2."); setSuccess(true); } catch (Exception e) {
	 * error = "No se pudo cambiar de estatus B2 a A2"; log.error(error, e);
	 * agregarMensajeError(summary, error); setSuccess(false); } }
	 */
	public void noEjecutar() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		try {
			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogMineroDto.setComentarioBacklogMinero(comentario);
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroCcambioA2(backlogMineroDto);
			this.getFlash().setKeepMessages(true);
			this.agregarMensajeInfo(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus POR EJECUTAR a estatus NO EJECUTADO.");
			setSuccess(true);
		} catch (Exception e) {
			error = "No se pudo cambiar de estatus POR EJECUTAR a NO EJECUTADO";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	public void estatusPospuesto() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogMineroDto = asignarCampos();

		try {
			backlogMineroDto.setUsuarioEstatusFinal(usuario);
			backlogMineroDto.setComentarioBacklogMinero(comentario);
			backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroCCambioP(backlogMineroDto);
			this.getFlash().setKeepMessages(true);
			this.agregarMensajeInfo(summary,
					"Se cambió el Folio " + backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus POR EJECUTAR a estatus POSPUESTO.");
			setSuccess(true);
		} catch (Exception e) {
			error = "No se pudo cambiar de estatus POR EJECUTAR a POSPUESTO";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	/***
	 * obtiene el comentario del backlog seleccionado para mostrar por qué no se
	 * ejecutó
	 * 
	 * @return comentario del backlog
	 */
	public String obtenerComentario() {
		if (backlogMineroSeleccionado == null) {
			return "";
		}
		comentario = backlogMineroSeleccionado.getComentarioBacklogMinero();
		return comentario;
	}

	public boolean verificarErrorCampos() {
		boolean errorGeneral = false;
		if (trabajoRealizado.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Trabajo realizado' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}
		if (horasHombre == null) {
			agregarMensajeWarn(summary, "El campo 'Horas hombre' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}
		if (horasMaquina == null) {
			agregarMensajeWarn(summary, "El campo 'Horas máquina' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}
		if (aprobadoPor.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Aprobado por' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (tecnicoEjecutor == null) {
			agregarMensajeWarn(summary, "El campo 'Técnico Ejecutor' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		/*
		 * if (supervisorACargo.equals("")) { agregarMensajeWarn(summary,
		 * "El campo 'Supervisor a cargo' está vacio y se requiere para continuar.");
		 * errorGeneral = true; }
		 */

		if (fechaEjecutar == null) {
			agregarMensajeWarn(summary,
					"El campo 'Fecha para ejecutar Backlog' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}
		setSuccess(!errorGeneral);

		return errorGeneral;
	}

	/**
	 * Esta función solo cambia la fecha para ejecutar el Backlog Primero se
	 * programa la fecha y se reenvia al listado de backlogs
	 */
	public String programarParaEjecutar() {
		BacklogsMinerosFacade backlosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosKey backlogKey = backlogMineroSeleccionado.getBacklogsMinerosKey();

		try {
			backlosFacade.modificarBacklogFechaProgramar(backlogKey, fechaEjecutar);

			this.agregarMensajeInfo(summary,
					"El Backlog Minero "
							+ backlogMineroSeleccionado.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
							+ " de estatus POR EJECUTAR a estatus EJECUTADO.");
			return "backlogsLista";
		} catch (Exception e) {
			error = "No se pudo agregar la fecha de programación del Backlog";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
		return "";
	}

	/**
	 * Esta funcion es para cuando el backlog ya se ejecuto y solo se verificara y
	 * llenara lo que se requiere Se usa cuando el backlog ya tiene una fecha para
	 * ejecutar el backlog
	 *
	 */
	public void mandarParaEjecutar() {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogMineroDto = asignarCampos();
		try {
			if (verificarErrorCampos() == false) {
				backlogMineroDto.setUsuarioEstatusFinal(usuario);
				backlogMineroDto.setTrabajoRealizado(trabajoRealizado.toUpperCase());
				backlogMineroDto.setHorasHombreReales(horasHombre);
				backlogMineroDto.setHorasMaquinaReales(horasMaquina);
				backlogMineroDto.setFechaHoraCerrado(new Date());

				backlogMineroDto.setTecnicoEjecutor(tecnicoEjecutor);

				backlogMineroDto.setFechaEjecutado(fechaEjecucion);
				backlogMineroDto.setSupervisorACargo("");
				aprobadoPor = aprobadoPor.toUpperCase();
				backlogMineroDto.setAprobadoPor(aprobadoPor);
				backlogsMinerosBitacoraEstatusFacade.cambioEstatusBacklogMineroCcambioD(backlogMineroDto);
				this.getFlash().setKeepMessages(true);
				this.agregarMensajeInfo(summary,
						"Se cambió el Folio "
								+ backlogMineroDto.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
								+ " de estatus POR EJECUTAR a estatus EJECUTADO.");
				// setSuccess(true);
			}
		} catch (Exception e) {
			error = "No se pudo cambiar de estatus POR EJECUTAR a EJECUTADO";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSuccess(false);
		}
	}

	/**
	 * Cuando se envie a por ejecutar se eliminará la fecha para ejecutar backlog y
	 * se cambiara el estatus
	 */
	public String porEjecutarUnCancelado() {
		BacklogsMinerosBitacoraEstatusFacade bitacoraFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosDto backlogDto = new BacklogsMinerosDto();
		backlogDto.setBacklogsMinerosKey(backlogMineroSeleccionado.getBacklogsMinerosKey());
		backlogDto.setUsuarioEstatusFinal(usuario);
		try {
			bitacoraFacade.cambioEstatusBacklogMineroC2CambioC(backlogDto);
			agregarMensajeInfoKeep(summary, "Se cambio correctamente de NO EJECUTADO a POR EJECUTAR");
			return "backlogsLista";
		} catch (Exception e) {
			log.error(e);
			agregarMensajeError(summary, "No se pudo cambiar de NO EJECUTADO a POR EJECUTAR");
		}
		return "";
	}

	/***
	 * Obtiene una lista con las rutas las imagenes de un backlog p:galleria solo
	 * funciona con imagenes que estan en la carpeta resources del war por lo que en
	 * este metodo lo primero que hacemos es copiar las imagenes del backlog a esta
	 * carpeta para desde ahí mandarlas llamar
	 * 
	 * @return lista de rutas de imagenes de backlogs
	 * @throws Exception
	 */
	public void listarImagenes() {

		if (listaImagenes.size() == 0) {
			List<String> listaRutas = new ArrayList<>();
			ImagenesBacklogsMinerosFacade imagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(
					RUTA_PROPERTIES_AMCE3);

			List<ImagenesBacklogsMineros> listaImagenes;
			try {
				listaImagenes = imagenesBacklogsMinerosFacade
						.obtenerImagenesBacklogsMinerosPorID(backlogMineroSeleccionado.getBacklogsMinerosKey());

				for (ImagenesBacklogsMineros imagen : listaImagenes) {
					String ruta = imagen.getImagenesBacklogsMinerosKey().getImagenBacklog();
					String rutaCompleta = "http://" + getServerUrlServices()
							+ getExternalContext().getInitParameter("servletImagenes") + ruta.replaceAll("/", "%2F");

					listaRutas.add(rutaCompleta);
				}

				this.listaImagenes = listaRutas;

				if (listaRutas.size() == 0) {
					renderGaleria = false;
					agregarMensajeWarn("Backlogs Mineros", "No se encontraron imagenes de este backlog");
				}

			} catch (Exception e) {
				agregarMensajeError(summary, "No se pudieron obtener las imágenes");
				log.error("No se pudieron obtener las imágenes", e);
			}
		}
		mostrarGaleria = mostrarGaleria.equals("") ? "display:none;" : "";

	}

	/***
	 * En las pantallas de estatus al dar clic en ocultar información se cambia el
	 * style a display none
	 */
	public void mostrarOcultarInformacion() {
		if (mostrarInformacion == null) {
			mostrarInformacion = "display:none";
			return;
		}
		mostrarInformacion = mostrarInformacion.equals("") ? "display:none" : "";
	}

	public String redireccionarPagina() {
		String accion = isSuccess() == true ? "backlogsLista" : "";
		return accion;
	}

	public void editarCeldaDetalleRefa(CellEditEvent event) {
		BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa;
		int indiceFila = event.getRowIndex();
		backlogsMinerosDetalleRefa = backlogsMinerosDetalleRefaList.get(indiceFila);
		Double precio = backlogsMinerosDetalleRefa.getPrecio();
		Short cantidad = backlogsMinerosDetalleRefa.getCantidad();
		Double total = precio * cantidad;
		backlogsMinerosDetalleRefa.setTotal(total);
		backlogsMinerosDetalleRefaList.remove(indiceFila);
		backlogsMinerosDetalleRefaList.add(indiceFila, backlogsMinerosDetalleRefa);
	}

	public BacklogsMinerosDto asignarCampos() {
		BacklogsMinerosDto backlogMineroDto = new BacklogsMinerosDto();
		BacklogsMineros backlog = backlogMineroSeleccionado;

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
		backlogMineroDto.setIdEstatusBacklogsMineros(backlog.getIdEstatusBacklogsMineros());

		return backlogMineroDto;
	}

	/**
	 * Obtiene todas las refacciones de un backlog
	 * 
	 * @return lista de refacciones
	 */
	private List<BacklogsMinerosDetalleRefa> listarBacklogsMinerosDetalleRefa() {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
		try {
			BacklogsMinerosKey idBacklog = backlogMineroSeleccionado.getBacklogsMinerosKey();
			backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaFacade
					.obtenerBacklogMineroDetalleRefaPorId(idBacklog);
			Comparator<BacklogsMinerosDetalleRefa> comp = (BacklogsMinerosDetalleRefa a,
					BacklogsMinerosDetalleRefa b) -> {
				Integer consecutivoA = a.getBacklogsMinerosDetalleRefaKey().getConsecutivo();
				Integer consecutivoB = b.getBacklogsMinerosDetalleRefaKey().getConsecutivo();
				return consecutivoA.compareTo(consecutivoB);
			};
			Collections.sort(backlogsMinerosDetalleRefaList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar Backlogs Mineros Detalle Refacciones del Folio "
					+ backlogMineroSeleccionado.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return backlogsMinerosDetalleRefaList;
	}

	/**
	 * Se ejecuta este metodo cuando se edita una celda de la tabla refacciones Aqui
	 * se calcula el subtotal de la celda y el total de toda la tabla
	 * 
	 * @param event
	 */
	public void onRowEdit(RowEditEvent event) {
		try {
			BacklogsMinerosDetalleRefa refaccion = ((BacklogsMinerosDetalleRefa) event.getObject());
			int cantidadRefaccion = refaccion.getCantidad();
			double precioRefaccion = refaccion.getPrecio();

			if (cantidadRefaccion > 0 && precioRefaccion > 0) {
				refaccion.setSubTotal(cantidadRefaccion * precioRefaccion);
				totalRefacciones = calcularTotalRefacciones();

			} else {
				refaccion.setPrecio(0.0);
				refaccion.setSubTotal(0.0);
				totalRefacciones = calcularTotalRefacciones();
			}

			// verificar el numero parte matco
			String numParteMatco = refaccion.getNumeroParte();

			boolean existe = analizarNumeroPMatco(numParteMatco);
			refaccion.setExisteNumeroParteMatco(existe);

			if (existe == false) {
				agregarMensajeWarn(summary, "El número parte MATCO agregado no existe!");
			}

			// agregarMensajeInfo(summary, "Se edito una celda: ");
		} catch (Exception e) {
			agregarMensajeWarn(summary, "No se pudo calcular la refacción");
		}
	}

	/**
	 * Obtiene el total de las refacciones y lo formateoa
	 * 
	 * @return total refacciones formateado
	 */
	public String getTotalFormateado() {
		String totalFormateado = "";
		if (this.totalRefacciones != null) {
			totalFormateado = numberFormat.format(this.totalRefacciones);
		}
		return totalFormateado;
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
		// if (this.obtenerSucursalFiltro() == 13) {
		split[2] = split[2].replace("_", "");
		if (split[2].length() < 7) {
			diff = 6 - split[2].length();
			for (int i = 0; i < diff; i++) {
				split[2] = "0" + split[2];
			}
			numeroCotizacion = split[0] + "-" + split[1] + "-" + split[2];
		}
		// }
		/*
		 * else if (this.obtenerSucursalFiltro() == 6) { // para cananea split[1] =
		 * split[1].replace("_",""); if(split[1].length() < 11) { diff = 10 -
		 * split[1].length(); for(int i = 0; i < diff; i++) { split[1] = "0" + split[1];
		 * } numeroCotizacion = split[0]+"-"+split[1]; } }
		 */
	}

	/**
	 * Obtiene la mascara del campo numero de cotizacion
	 * 
	 * @return
	 */
	public String getMascaraCotizacion() {
		if (this.obtenerSucursalFiltro() == 13) {
			return "QT-13-999999";
		}
		return "QT-06-999999";
	}

	public String getSupervisorACargo() {
		return supervisorACargo;
	}

	public void setSupervisorACargo(String supervisorACargo) {
		this.supervisorACargo = supervisorACargo;
	}

	public Double getTotalRefacciones() {
		return totalRefacciones;
	}

	public void setTotalRefacciones(Double totalRefacciones) {
		this.totalRefacciones = totalRefacciones;
	}

	public BacklogsMineros getBacklogMineroSeleccionado() {
		return backlogMineroSeleccionado;
	}

	public void setBacklogMineroSeleccionado(BacklogsMineros backlogMineroSeleccionado) {
		this.backlogMineroSeleccionado = backlogMineroSeleccionado;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public Date getFechaCreacion() {
		return fechaCreacion;
	}

	public void setFechaCreacion(Date fechaCreacion) {
		this.fechaCreacion = fechaCreacion;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getNumeroCotizacion() {
		return numeroCotizacion;
	}

	public void setNumeroCotizacion(String numeroCotizacion) {
		this.numeroCotizacion = numeroCotizacion;
	}

	public String getAutorizadoPor() {
		return autorizadoPor;
	}

	public void setAutorizadoPor(String autorizadoPor) {
		this.autorizadoPor = autorizadoPor;
	}

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public Date getFechaEjecutar() {
		return fechaEjecutar;
	}

	public void setFechaEjecutar(Date fechaEjecutar) {
		this.fechaEjecutar = fechaEjecutar;
	}

	public List<BacklogsMinerosDetalleRefa> getBacklogsMinerosDetalleRefaList() {
		return backlogsMinerosDetalleRefaList;
	}

	public void setBacklogsMinerosDetalleRefaList(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		this.backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaList;
	}

	public Double getHorasHombre() {
		return horasHombre;
	}

	public void setHorasHombre(Double horasHombre) {
		this.horasHombre = horasHombre;
	}

	public Double getHorasMaquina() {
		return horasMaquina;
	}

	public void setHorasMaquina(Double horasMaquina) {
		this.horasMaquina = horasMaquina;
	}

	public String getTrabajoRealizado() {
		return trabajoRealizado;
	}

	public void setTrabajoRealizado(String trabajoRealizado) {
		this.trabajoRealizado = trabajoRealizado;
	}

	public Tecnico getTecnicoEjecutor() {
		return tecnicoEjecutor;
	}

	public void setTecnicoEjecutor(Tecnico tecnicoEjecutor) {
		this.tecnicoEjecutor = tecnicoEjecutor;
	}

	public String getNumeroReserva() {
		return numeroReserva;
	}

	public void setNumeroReserva(String numeroReserva) {
		this.numeroReserva = numeroReserva;
	}

	public String getComentario() {
		return comentario;
	}

	public void setComentario(String comentario) {
		this.comentario = comentario;
	}

	public List<String> getListaImagenes() {
		return listaImagenes;
	}

	public void setListaImagenes(List<String> listaImagenes) {
		this.listaImagenes = listaImagenes;
	}

	public String getMostrarGaleria() {
		return mostrarGaleria;
	}

	public void setMostrarGaleria(String mostrarGaleria) {
		this.mostrarGaleria = mostrarGaleria;
	}

	public boolean getRenderGaleria() {
		return renderGaleria;
	}

	public void setMostrarBotonVerImagenes(boolean mostrarBotonVerImagenes) {
		this.renderGaleria = mostrarBotonVerImagenes;
	}

	public String getAprobadoPor() {
		return aprobadoPor;
	}

	public void setAprobadoPor(String aprobadoPor) {
		this.aprobadoPor = aprobadoPor;
	}

	public String getMostrarInformacion() {
		return mostrarInformacion;
	}

	public void setMostrarInformacion(String mostrarInformacion) {
		this.mostrarInformacion = mostrarInformacion;
	}

	public List<RiesgosTrabajo> getRiesgosTabajosList() {
		return riesgosTabajosList;
	}

	public void setRiesgosTabajosList(List<RiesgosTrabajo> riesgosTabajosList) {
		this.riesgosTabajosList = riesgosTabajosList;
	}

	public LadoComponente getLadoComponente() {
		return ladoComponente;
	}

	public void setLadoComponente(LadoComponente ladoComponente) {
		this.ladoComponente = ladoComponente;
	}

	public List<LadoComponente> getLadoComponenteList() {
		return ladoComponenteList;
	}

	public void setLadoComponenteList(List<LadoComponente> ladoComponenteList) {
		this.ladoComponenteList = ladoComponenteList;
	}

	public Sintomas getSintomasObject() {
		return sintomasObject;
	}

	public void setSintomasObject(Sintomas sintomasObject) {
		this.sintomasObject = sintomasObject;
	}

	public String getDetalleRevision() {
		return detalleRevision;
	}

	public void setDetalleRevision(String detalleRevision) {
		this.detalleRevision = detalleRevision;
	}

	public boolean isBtnRevision() {
		return btnRevision;
	}

	public void setBtnRevision(boolean btnRevision) {
		this.btnRevision = btnRevision;
	}

	public RevisionKardexEntity getRevisionBacklog() {
		return revisionBacklog;
	}

	public void setRevisionBacklog(RevisionKardexEntity revisionBacklog) {
		this.revisionBacklog = revisionBacklog;
	}

	/**
	 * Funcion modificada para obtener el diferente de la pieza surtidada para
	 * mostrar el boton en esperaderefacciones.xhtml
	 * 
	 * @return
	 */
	public boolean isPiezasSurtidas() {
		return !piezasSurtidas;
	}

	public void setPiezasSurtidas(boolean piezasSurtidas) {
		this.piezasSurtidas = piezasSurtidas;
	}

	public Date getFechaEjecucion() {
		return fechaEjecucion;
	}

	public void setFechaEjecucion(Date fechaEjecucion) {
		this.fechaEjecucion = fechaEjecucion;
	}

	public boolean isFechaEjecutarProgramada() {
		return fechaEjecutarProgramada;
	}

	public void setFechaEjecutarProgramada(boolean fechaEjecutarProgramada) {
		this.fechaEjecutarProgramada = fechaEjecutarProgramada;
	}

	public String getUrlReporte() {
		return urlReporte;
	}

	public void setUrlReporte(String urlReporte) {
		this.urlReporte = urlReporte;
	}

	public boolean isPorAutorizar() {
		return porAutorizar;
	}

	public void setPorAutorizar(boolean porAutorizar) {
		this.porAutorizar = porAutorizar;
	}
}
