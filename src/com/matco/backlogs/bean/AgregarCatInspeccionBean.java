package com.matco.backlogs.bean;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Base64;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.event.ActionEvent;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.matco.amce3.dto.MaquinariaDto;
import com.matco.amce3.facade.BacklogsMinerosBitacoraEstatusFacade;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.amce3.facade.CargosTrabajoFacade;
import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.amce3.facade.ImagenesBacklogsMinerosFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.LugaresOrigenBacklogsMinerosFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.dto.ImagenBlDto;
import com.matco.backlogs.entity.BacklogMineroEstandar;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosBitacoraEstatus;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.ImagenesBacklogsMineros;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.LugaresOrigenBacklogsMineros;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;
import com.matco.backlogs.entity.key.BacklogsMinerosBitacoraEstatusKey;
import com.matco.backlogs.entity.key.BacklogsMinerosDetalleRefaKey;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.backlogs.entity.key.ImagenesBacklogsMinerosKey;
import com.matco.ejes.entity.Almacen;
import com.matco.ejes.entity.Marca;
import com.nsoluciones.catinspect.entity.CatInspeccionQuestEntity;
import com.nsoluciones.catinspect.entity.CatInspectAttachmentsEntity;
import com.nsoluciones.catinspect.entity.CatInspectQuestionsEntity;
import com.nsoluciones.catinspect.facade.CatInspectFacade;

/**
 * Class Bean para agregar una inspeccion
 * 
 * @author N Soluciones de Software
 *
 */
@ManagedBean(name = "agregarCatInspeccionBean")
@ViewScoped
public class AgregarCatInspeccionBean extends GenericBacklogBean implements Serializable {
	
	private static final long serialVersionUID = 7372756196144614761L;
	private static final Logger log = Logger.getLogger(AgregarCatInspeccionBean.class);
	private static final DecimalFormat formato = new DecimalFormat("00");
	private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	private final String rutaArchivosTemporal = getExternalContext().getInitParameter("rutaImagenesBacklogsMineros");
	private boolean habilitaCodigoTrabajo = false;
	
	private Double totalRefacciones;
	private String rutaImagenesTemporales;
	private String error;
	private String usuario;
	private BacklogsMineros backlogEntity;
	private BacklogMineroEstandar backlogEstandarEntity;
	private CatInspectQuestionsEntity questionEntity;
	private BacklogsStaticsVarBean seleccionBean;
	private LoginBean loginBean;
	private List<MaquinariaDto> maquinariaList;
	private List<String> tipoArchivo = new ArrayList<>();
	private List<ImagenBlDto> listaImagenesDto = new ArrayList<>();
	private List<CargosTrabajo> cargosTrabajosList = new ArrayList<>();
	private List<OrigenesBacklogsMineros> origenesBacklogsMinerosList = new ArrayList<>();
	private List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList = new ArrayList<>();
	private List<CodigosSistemas> codigosSistemasList = new ArrayList<>();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();
	private List<Sintomas> sintomasList = new ArrayList<>();
	private List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<>();
	private List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList = new ArrayList<>();
	private List<BacklogsMinerosDetalleRefa> partesSeleccionadas = new ArrayList<>();
	private BacklogsMinerosFacade backlogsMinerosFacade;
	private BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade;
	private BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade;
	private CatInspectFacade catInspectFacade;
	
	@SuppressWarnings("unused")
	private boolean hayImagenes = true;
	@SuppressWarnings("unused")
	private String totalFormateado;
	
	
	@PostConstruct
	public void init() {
		this.seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		loginBean = this.obtenerBean("loginBean");
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		backlogEntity = new BacklogsMineros();
		cargarDatosBacklog();
		
		if (seleccionBean.getBacklogEstandarSelected() != null) {
			
			backlogEstandarEntity = seleccionBean.getBacklogEstandarSelected();
			backlogEstandarEntity.cargarVariables();
			try {
				backlogEntity.asignarBacklogEstandar(backlogEstandarEntity);
				generarSubtotalRefacciones(backlogEntity.getListaRefacciones());
				totalRefacciones = calcularTotalRefacciones();
				
				questionEntity = seleccionBean.getInspeccionSeleccionada();
				llenarObjetos(questionEntity, backlogEntity);
				
				if(backlogEntity.getCodigoTrabajoDescripcion() != null) {
					habilitaCodigoTrabajo = true;
				}
				
			} catch (Exception e) {
				log.error("No se pudo cargar el Backlog Estándar", e);
				agregarMensajeError(summary, "No se pudo cargar el Backlog Estándar");
			}
		}else {
			questionEntity = seleccionBean.getInspeccionSeleccionada();
			llenarObjetos(questionEntity, backlogEntity);
		}
	}
	
	

	/**
	 * Se ejecutara cuando se salga de la pagina
	 */
	@PreDestroy
	public void destroy() {
		//System.out.println("Saliendo de CatInspect");
		// hacemos null el Backlog Estándar para no crear conflictos
		seleccionBean.setBacklogEstandarSelected(null);
	}

	/**
	 * 
	 */
	public void cargarDatosBacklog() {
		origenesBacklogsMinerosList = listarOrigenesBacklogsMineros();
		prioridadesBacklogsMinerosList = listarPrioridadesBacklogsMineros();
		codigosSistemasList = listarSistemasBacklogsMineros();
		ladoComponenteList = listarLadoComponentes();
		sintomasList = listarSintomas();
		riesgosTabajosList = listarRiesgosTrabajos();
		lugaresOrigenBacklogsMinerosList = listarLugaresOrigenesBacklogsMineros();
		cargosTrabajosList = listarCargosTrabajoBacklogsMineros();
		rutaImagenesTemporales = System.getProperty("jboss.server.temp.dir") + File.separator;
		backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(RUTA_PROPERTIES_AMCE3);
		backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(RUTA_PROPERTIES_AMCE3);
		catInspectFacade = new CatInspectFacade(RUTA_PROPERTIES_AMCE3);
	}

	/**
	 * Se ejecuta por unica vez cuando se envia la inspeccion
	 * 
	 * @param questionEntity
	 * @param backlogEntity
	 */
	public void llenarObjetos(CatInspectQuestionsEntity questionEntity, BacklogsMineros backlogEntity) {
		backlogEntity.setIdInspeccion(questionEntity.getFolio());
		backlogEntity.setNumeroSerie(questionEntity.getSerie());
		backlogEntity.setNumeroEconomico(questionEntity.getNumeroEconomico());
		
		if(questionEntity.getPreguntaCat() != null || !questionEntity.getPreguntaCat().equals("")) {
			backlogEntity.setAccionEquipo(questionEntity.getPreguntaCat());
		}
		
		if	(questionEntity.getComments() != null || !questionEntity.getComments().equals("")) {
			//backlogEntity.setComentarioBacklogMinero(questionEntity.getComments());
			backlogEntity.setSintomasEquipo(questionEntity.getComments());
		}
		
		
		backlogEntity.setHorometro(questionEntity.getHorometro());
		backlogEntity.getOrigenesBacklogsMineros().setIdOrigenBacklogMinero((short) 10);
		String codigoComponente = questionEntity.getComponentCode();
		
		if(codigoComponente != null) {
			String splitCode[] = codigoComponente.split(" ");
			int codigoSistema = Integer.parseInt(splitCode[0]);
			backlogEntity.getIdCodigoSistema().setCodigoSistema(codigoSistema);
		}
		
		Almacen idAlmacen = new Almacen();
		int idBacklog = 0;
		idAlmacen.setAlmacen(obtenerSucursalFiltro());
		BacklogsMinerosKey backlogKey = new BacklogsMinerosKey(idBacklog, idAlmacen);
		backlogEntity.setBacklogsMinerosKey(backlogKey);
		/**/
		maquinariaList = listarMaquinariaDtoBacklogsMineros((short) questionEntity.getSucursal());
		MaquinariaDto maquinaria = new MaquinariaDto();
		String numeroDeSerie = questionEntity.getSerie();
		for (MaquinariaDto maquinariaDto : maquinariaList) {
			String numeroSerie = maquinariaDto.getSerie();
			if (numeroSerie.equals(numeroDeSerie)) {
				maquinaria = maquinariaDto;
				break;
			}
		}
		backlogEntity.setModeloEquipo(maquinaria.getIdModelo());
		backlogEntity.setIdMarca(maquinaria.getIdMarca());
		// llenado de imagenes
		cargarImagenesBase64(questionEntity);
	}

	/***
	 * Guarda un BL
	 * 
	 * @return
	 */
	public String agregarInspeccion() {
		if (verificarCamposBacklog() == true || verificarPartesRequeridas() == true) {
			return "";
		}
		String mensaje = "";
		BacklogsMineros backlog = backlogEntity;
		int dato;
		try {
			EstatusBacklogsMineros idEstatusBacklogsMineros = new EstatusBacklogsMineros();
			String estatusBacklogMinero = "A"; // ESTATUS CREADO

			idEstatusBacklogsMineros.setIdEstatusBacklogMinero(estatusBacklogMinero);
			backlog.setIdEstatusBacklogsMineros(idEstatusBacklogsMineros);
			backlog.setCreadoPor(usuario);

			cambiarCodigoTrabajo();
			
			CodigosSMCS idCodigoSMCS = new CodigosSMCS();
			Short idCodigo = backlogEntity.getIdCodigoTrabajoSeleccionado();
			idCodigoSMCS.setIdCodigoSMCS(idCodigo);
			backlog.setIdCodigoSMCS(idCodigoSMCS);
			
			dato = backlogsMinerosFacade.guardarBacklogMineroInsp(backlog);

			// Se guarda la question donde se genero el backlog
			CatInspeccionQuestEntity inspeccionCreada = new CatInspeccionQuestEntity();
			inspeccionCreada.setIdBacklog(dato);
			inspeccionCreada.setInspeccionNumber(questionEntity.getFolio());
			inspeccionCreada.setQuestionId(questionEntity.getIdPreguntaCat());
			catInspectFacade.agregarInspeccionQuest(inspeccionCreada);
		} catch (Exception e) {
			mensaje = "No se pudo guardar el BL.";
			agregarMensajeError(mensaje);
			return "";
		}
		backlog.getBacklogsMinerosKey().setIdBacklogMinero(dato);
		mensaje = "El Backlog Minero " + backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
				+ " se ha agregado correctamente.";
		this.agregarMensajeInfo(summary, mensaje);

		BacklogsMinerosKey bKey = backlog.getBacklogsMinerosKey();
		bKey.setIdBacklogMinero(dato);
		backlog.setBacklogsMinerosKey(bKey);

		try {
			asignarBitacoraEstatusBacklogMinero(backlog, usuario);
		} catch (Exception e) {
			String error = "No se pudo guardar la Bitacora Estatus del "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
			return "";
		}

		asignarRiesgosDeBacklogs(backlog);

		if (!backlog.getListaRefacciones().isEmpty()) {
			try {
				asignarDetalleRefaBacklogMinero(backlog, backlog.getListaRefacciones());
			} catch (Exception e) {
				String error = "No se pudo guardar el Detalle Refacciones del "
						+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
				log.error(error, e);
				agregarMensajeError(summary, error);
				return "";
			}
		}

		if (!listaImagenesDto.isEmpty()) {
			asignarImagenesBacklogMinero(backlog);
		}
		/*
		 * if (seleccionBean.getAlerta() == seleccionBean.getAlertasTotal()) {
		 * seleccionBean.setIncrementarInspeccionEnProceso();
		 * seleccionBean.setLecturaPDF(true); seleccionBean.setAlerta(1); } else {
		 * seleccionBean.setIncrementarAlerta(); }
		 */
		return "catInspect";
	}

	/**
	 * Verifica los campos del backlog antes de ser registrado
	 * 
	 * @return boolean
	 */
	public boolean verificarCamposBacklog() {
		boolean errorCampos = false;

		if (backlogEntity.getSintomasEquipo().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Sintomas' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getSolicitadoPor().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Requerido por' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getHorometro() == null) {
			agregarMensajeWarn(summary, "El campo 'Horometro' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getLadoComponenteOb().getCodigoLDC() == null) {
			agregarMensajeWarn(summary, "El campo 'Lado del componente' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getOrigenesBacklogsMineros().getIdOrigenBacklogMinero() == null) {
			agregarMensajeWarn(summary, "El campo 'Origen del BL' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getIdPrioridadBacklog().getIdPrioridadBacklogMinero() == null) {
			agregarMensajeWarn(summary, "El campo 'Prioridad' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getTipoTrabajo() == null) {
			agregarMensajeWarn(summary, "El campo 'Tipo de trabajo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getIdCodigoSistema().getCodigoSistema() == null) {
			agregarMensajeWarn(summary, "El campo 'Sistema' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getIdCargoTrabajo().getIdCargoTrabajo() == null) {
			agregarMensajeWarn(summary, "El campo 'Cargo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getCodigoTrabajoDescripcion() == null) {
			agregarMensajeWarn(summary, "El campo 'Código de trabajo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getSintoma().getIdCodigoSintoma() == null) {
			agregarMensajeWarn(summary, "El campo 'Código de sintomas' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (backlogEntity.getIdCodigoRiesgoList() == null || backlogEntity.getIdCodigoRiesgoList().length == 0) {
			agregarMensajeWarn(summary,
					"El campo 'Riesgo trabajo' está vacio y se requiere al menos un riesgo para continuar.");
			errorCampos = true;
		}
		return errorCampos;
	}

	/**
	 * Verifica los campos de la tabla Partes Requeridas antes de ser registrado
	 * 
	 * @return boolean
	 */
	private boolean verificarPartesRequeridas() {
		boolean incompleto = false;
		if (!backlogEntity.getListaRefacciones().isEmpty()) {
			for (BacklogsMinerosDetalleRefa refaccion : backlogEntity.getListaRefacciones()) {
				if (refaccion.getCantidad() == null || refaccion.getCantidad() <= 0) {
					agregarMensajeWarn(summary, "La cantidad de una refacción no puede quedar vacía");
					incompleto = true;
				}
				
				if (refaccion.getNumeroParte() == null || refaccion.getNumeroParte().equals("")) {
					agregarMensajeWarn(summary, "El Número Parte de una refacción no puede quedar vacío");
					incompleto = true;
				}
				
				if (refaccion.getDescripcionParte() == null || refaccion.getDescripcionParte().equals("")) {
					agregarMensajeWarn(summary, "La Descripción de una refacción no puede quedar vacía");
					incompleto = true;
				}
				
				boolean existe = analizarNumeroPMatco(refaccion.getNumeroParte());
				if (existe == false){
					agregarMensajeWarn(summary, "No se encontró el número parte MATCO: "+refaccion.getNumeroParte());
					//incompleto = true;
				}

			}
			return incompleto;
		}
		return false;
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
				agregarMensajeWarn(summary, "No se encontró el Número de parte MATCO "
						+ numParteMatco+" con el Cliente: "
						+seleccionBean.getIdClienteMatco());
			}
			
		} catch (Exception e) {
			agregarMensajeWarn(summary, "No se pudo calcular la refacción");
		}
	}

	/**
	 * Abre el dialgo BL Estandars
	 */
	public void activarDialogoBLEST() {
		PrimeFaces.current().executeScript("PF('dialogBlEstandar').show();");
		seleccionBean.setRedireccionDialogBLEST("registroInspeccion.xhtml");
	}
	
	/**
	 * Reinicia los campos a la inspeccion anterior
	 */
	public void limpiarCamposCI() {
		backlogEntity = new BacklogsMineros();
		questionEntity = seleccionBean.getInspeccionSeleccionada();
		llenarObjetos(questionEntity, backlogEntity);
	}

	/**
	 * Obtiene el subtotal de todas las refacciones y las guarda en una variable
	 * llamada total
	 * 
	 * @return
	 */
	public Double calcularTotalRefacciones() {
		double total = 0;
		for (BacklogsMinerosDetalleRefa refaccionList : backlogEntity.getListaRefacciones()) {
			if (refaccionList != null) {
				total += refaccionList.getSubTotal();
			}
		}
		return total;
	}
	
	/**
	 * Genera subtotal de refaccionesList
	 */
	public void generarSubtotalRefacciones(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		for (BacklogsMinerosDetalleRefa refaccion : backlogsMinerosDetalleRefaList) {
			refaccion.setSubTotal(refaccion.getCantidad() * refaccion.getPrecio());
		}
	}

	/***
	 * acutaliza la lista de partes selecciondas para borrar en editar backlog
	 * 
	 * @param seleccion parte seleccionada
	 */
	public void actualizarSeleccion(BacklogsMinerosDetalleRefa seleccion) {
		if (partesSeleccionadas.contains(seleccion)) {
			partesSeleccionadas.remove(seleccion);
		} else {
			partesSeleccionadas.add(seleccion);
		}
	}

	public void habilitarCodigoTrabajo() {
		backlogEntity.getIdCodigoSMCS().setCodigoSMCS(null);
		backlogEntity.getIdCodigoSMCS().setDescripcionSMCS(null);
		backlogEntity.setTrabajoRealizar(null);
		backlogEntity.setCodigoTrabajoDescripcion("");
		habilitaCodigoTrabajo = false;
	}

	public void agregarFilaPartesRequeridas(ActionEvent even) {
		BacklogsMinerosDetalleRefa backlogsMinerosDetalle = new BacklogsMinerosDetalleRefa((short) 1, "", "", "", 0.0);
		backlogEntity.getListaRefacciones().add(backlogsMinerosDetalle);

	}

	/***
	 * En editar backlog borra las partes seleccionadas
	 */
	public void borrarFilasPartes(ActionEvent even) {
		try {
			for (BacklogsMinerosDetalleRefa refa : partesSeleccionadas) {
				backlogEntity.getListaRefacciones().remove(refa);
			}
			partesSeleccionadas.clear();
			totalRefacciones = calcularTotalRefacciones();
		} catch (Exception e) {
			agregarMensajeWarn(summary, "No se ha seleccionado una refacción");
			// log.error(error, e); se concidera mas como una alerta
		}
	}

	public void subirListado(FileUploadEvent event) throws IOException {
		UploadedFile archivo = event.getFile();
		String fileContent = archivo.getContentType();
		String fileName = archivo.getFileName();
		InputStream inputStream = archivo.getInputstream();
		String extension = FilenameUtils.getExtension(fileName);
		log.info("typeContentFile: " + fileContent);
		try {
			if (fileContent.equals("application/vnd.ms-excel") || extension.equals("xls")) { // Para archivos .xls
				// HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
				Workbook workbook = WorkbookFactory.create(inputStream);
				Sheet sheet = workbook.getSheetAt(0);

				Integer rows = sheet.getPhysicalNumberOfRows();
				Integer cols = 5;
				cargarListadoPartesRequeridas(sheet, rows, cols);
			} else {
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
				XSSFSheet sheet = workbook.getSheetAt(0);

				Integer rows = sheet.getPhysicalNumberOfRows();
				Integer cols = 5;
				cargarListadoPartesRequeridas(sheet, rows, cols);
				workbook.close();
			}
			inputStream.close();
			totalRefacciones = calcularTotalRefacciones();
		} catch (OldExcelFormatException ex) {
			agregarMensajeError(summary, "El archivo Excel ingresado es muy viejo, no se pudo cargar. "
					+ "Por favor intente subir una versión más nueva");
			log.error("No se pudo cargar el excel: " + ex);
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo cargar el Excel");
			log.error("No se pudo cargar el excel: " + e);
		}
	}

	/**
	 * 
	 * @param workbook
	 */
	public void cargarListadoPartesRequeridas(Sheet sheet, int rows, int cols) {
		Row row;
		Cell cell;
		for (int r = 0; r < rows; r++) {
			row = sheet.getRow(r);
			BacklogsMinerosDetalleRefa bmdr = new BacklogsMinerosDetalleRefa();
			if (row != null) {
				for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);
					if (cell != null) {
						switch (c) {
						case 0:
							// es un entero de 3 digitos
							if (cell.getCellType() == CellType.NUMERIC) {
								short cantidad = (short) cell.getNumericCellValue();
								bmdr.setCantidad(cantidad);
							} else {
								agregarMensajeWarn(summary, "El campo Cantidad debe ser un numero entero, se ingreso: "
										+ cell.getStringCellValue());
								bmdr.setCantidad((short) 1);
							}
							break;
						case 1:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setNumeroParte(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setNumeroParte(cell.getStringCellValue());
							}
							break;
						case 2:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setDescripcionParte(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setDescripcionParte(cell.getStringCellValue());
							}
							break;
						case 3:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setObservaciones(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setObservaciones(cell.getStringCellValue());
							}
							break;
						case 4:
							if (cell.getCellType() == CellType.NUMERIC) {
								if (cell.getNumericCellValue() >= 0) {
									bmdr.setPrecio(cell.getNumericCellValue());
									bmdr.setSubTotal(bmdr.getCantidad() * bmdr.getPrecio());
								} else {
									agregarMensajeWarn(summary,
											"El campo Precio Unitario no puede ser un numero negativo: "
													+ cell.getNumericCellValue());
									bmdr.setPrecio(0.0);
									bmdr.setSubTotal(0.0);
								}
							} else {
								agregarMensajeWarn(summary,
										"El campo Precio Unitario debe ser un numero real, se ingreso: "
												+ cell.getStringCellValue());
								bmdr.setPrecio(0.0);
								bmdr.setSubTotal(0.0);
							}
							break;
						}
					} else {
						switch (c) {
						case 0:
							bmdr.setCantidad((short) 0);
							break;
						case 1:
							bmdr.setNumeroParte("");
							break;
						case 2:
							bmdr.setDescripcionParte("");
							break;
						case 3:
							bmdr.setObservaciones("");
							break;
						case 4:
							bmdr.setPrecio(0.0);
						}
					}
				}
				backlogEntity.getListaRefacciones().add(bmdr);
			}
		}
	}

	public void borrarImagenes() {
		String mensaje = "";
		if (listaImagenesDto.isEmpty()) {
			mensaje = "No hay imagenes cargadas.";
			agregarMensajeError(mensaje);
			return;
		}

		listaImagenesDto.clear();
		hayImagenes = listaImagenesDto.isEmpty();
		mensaje = "Las imagenes se borraron correctamente.";
		agregarMensajeWarn(mensaje);

	}

	public void upload(FileUploadEvent event) {
		UploadedFile uploadedFile = event.getFile();
		if (uploadedFile == null) {
			error = "El archivo no es válido";
			agregarMensajeWarn(summary, error);
			return;
		}

		StreamedContent streamedContent = null;
		InputStream inputStream = null;

		String nombre = event.getFile().getFileName();
		String mimeType = event.getFile().getContentType();
		String rutaImagen = rutaImagenesTemporales + nombre;
		rutaImagen = construirURL(rutaImagen);
		String rutaCompleta = "http://" + getServerUrlServices()
				+ getExternalContext().getInitParameter("servletImagenes") + rutaImagen.replaceAll("/", "%2F");

		streamedContent = null;
		try {
			inputStream = new BufferedInputStream(event.getFile().getInputstream());
			streamedContent = new DefaultStreamedContent(inputStream, mimeType, nombre);
			File file = new File(rutaImagen);
			file.createNewFile();
			crearImagenesTemporales(file, inputStream);
		} catch (IOException e) {
			log.error(e);
		}
		ImagenBlDto imagen = new ImagenBlDto();
		imagen.setNombre(nombre);
		imagen.setRutaImagen(rutaImagen);
		imagen.setRutaImagenTemp(rutaCompleta);
		imagen.setStreamedContent(streamedContent);
		imagen.setMimeType(mimeType);
		imagen.setExiste(false);
		listaImagenesDto.add(imagen);
		hayImagenes = !listaImagenesDto.isEmpty();
		tipoArchivo.add(nombre);
	}

	public void cargarImagenesBase64(CatInspectQuestionsEntity questionEntity) {
		List<CatInspectAttachmentsEntity> imagenesList = questionEntity.getImagenes();
		ImagenBlDto imagen;
		String data = "data:";
		String base64 = ";base64,";
		String ruta = "";
		for (CatInspectAttachmentsEntity img : imagenesList) {
			imagen = new ImagenBlDto();
			ruta = data + "" + img.getTypeOfMedia() + "" + base64 + "" + img.getImgBase64();
			imagen.setRutaImagen(ruta);
			imagen.setRutaImagenTemp(ruta);
			imagen.setRutaBase64(img.getImgBase64());
			imagen.setBase64(true);
			imagen.setMimeType(img.getTypeOfMedia());
			listaImagenesDto.add(imagen);
		}
		hayImagenes = true;
	}

	public void crearImagenesTemporales(File file, InputStream contenido) throws IOException {
		FileUtils.copyInputStreamToFile(contenido, file);
	}

	/***
	 * Actualiza las variables correspondientes a las horas hombre y estimadas
	 * después de seleccionar un código de trabajo
	 */
	public void asignarHoras() {
		CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		CodigosSMCS codigo;
		try {
			codigo = codigosSMCSFacade
					.obtenerCodigosSMCSPorID(backlogEntity.getIdCodigoTrabajoSeleccionado().toString());
			Double horasHombre = codigo.getHoraHombreCodigoSMCS();
			backlogEntity.setHorasHombreEstimadas(horasHombre);
			Double horasMaquina = (horasHombre) / 2;
			backlogEntity.setHorasMaquinaEstimadas(horasMaquina);
		} catch (Exception e) {
			error = "No se pudo obtener el Código SMCS por ID al asignar las horas";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
	}

	/***
	 * Al seleccionar un codigo de trabajo, actualiza las variables correspondientes
	 * al código de trabajo, descripción e id
	 * 
	 * @throws Exception
	 */
	public void cambiarCodigoTrabajo() throws Exception {
		backlogEntity.getCodigoTrabajoDescripcion().trim();
		String codigoTrabajoFormateado = backlogEntity.getCodigoTrabajoDescripcion().split("-")[0].trim();
		backlogEntity.setCodigoTrabajo(Integer.parseInt(codigoTrabajoFormateado));
		// backlogEstandar.setTipoTrabajo(backlogEstandar.getCodigoTrabajoDescripcion().split("-")[1].trim());
		backlogEntity.setTrabajoRealizar(backlogEntity.getCodigoTrabajoDescripcion().split("-")[1].trim());
		CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		List<CodigosSMCS> listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		for (CodigosSMCS codigoTrabajoComparado : listaCodigosTrabajo) {
			int codigo = codigoTrabajoComparado.getCodigoSMCS();
			String descripcion = codigoTrabajoComparado.getDescripcionSMCS();
			if (backlogEntity.getCodigoTrabajo() == codigo && descripcion.equals(backlogEntity.getTrabajoRealizar())) {
				backlogEntity.setIdCodigoTrabajoSeleccionado(codigoTrabajoComparado.getIdCodigoSMCS());
				habilitaCodigoTrabajo = true;
				break;
			}
		}
	}

	private List<CargosTrabajo> listarCargosTrabajoBacklogsMineros() {
		CargosTrabajoFacade cargosTrabajoFacade = new CargosTrabajoFacade(RUTA_PROPERTIES_AMCE3);
		try {
			cargosTrabajosList = cargosTrabajoFacade.obtenerTodosCargosTrabajo();
			Comparator<CargosTrabajo> comp = (CargosTrabajo a, CargosTrabajo b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(cargosTrabajosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Cargos Trabajo";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return cargosTrabajosList;
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

	private List<LadoComponente> listarLadoComponentes() {
		LadoComponenteFacade ladoComponenteFacade = new LadoComponenteFacade(RUTA_PROPERTIES_AMCE3);
		try {
			ladoComponenteList = ladoComponenteFacade.obtenerTodosLDC();
		} catch (Exception e) {
			error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return ladoComponenteList;
	}

	private List<Sintomas> listarSintomas() {
		SintomasYRiesgosFacade sintomasYriesgos = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
		try {
			sintomasList = sintomasYriesgos.getAllSintomas();
		} catch (Exception e) {
			error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return sintomasList;
	}

	private List<RiesgosTrabajo> listarRiesgosTrabajos() {
		SintomasYRiesgosFacade sintomasYriesgos = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
		try {
			riesgosTabajosList = sintomasYriesgos.getAllRiesgos();
		} catch (Exception e) {
			error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return riesgosTabajosList;
	}

	private List<LugaresOrigenBacklogsMineros> listarLugaresOrigenesBacklogsMineros() {
		LugaresOrigenBacklogsMinerosFacade lugaresOrigenBacklogsMinerosFacade = new LugaresOrigenBacklogsMinerosFacade(
				RUTA_PROPERTIES_AMCE3);
		try {
			lugaresOrigenBacklogsMinerosList = lugaresOrigenBacklogsMinerosFacade
					.obtenerTodosLugaresOrigenBacklogsMineros();
			Comparator<LugaresOrigenBacklogsMineros> comp = (LugaresOrigenBacklogsMineros a,
					LugaresOrigenBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(lugaresOrigenBacklogsMinerosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Lugares Origen Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return lugaresOrigenBacklogsMinerosList;
	}

	public List<MaquinariaDto> listarMaquinariaDtoBacklogsMineros(Short idSucursal) {
		List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();
		try {
			List<MaquinariaDto> maquinariaDtoLista = new ArrayList<>();
			if (idSucursal == 6) {
				short sucursal = 6;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 9;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 14;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
			} else if (idSucursal == 13 || idSucursal == 2 || idSucursal == 1) {
				short sucursal = 13;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 2;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 1;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
			} else {
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(idSucursal));
			}

			for (MaquinariaDto maquinaria : maquinariaDtoLista) {
				String numeroSerie = maquinaria.getSerie() == null ? "" : maquinaria.getSerie();
				String numeroSerieDos = numeroSerie.equals("") ? "" : numeroSerie;
				if (!numeroSerieDos.equals("")) {
					maquinariaDtoList.add(maquinaria);
				}
			}
			Comparator<MaquinariaDto> comp = (MaquinariaDto a, MaquinariaDto b) -> {
				String serieA = a.getSerie() != null ? a.getSerie() : "";
				String serieB = b.getSerie() != null ? b.getSerie() : "";
				return serieA.compareTo(serieB);
			};
			Collections.sort(maquinariaDtoList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return maquinariaDtoList;
	}

	public void asignarBitacoraEstatusBacklogMinero(BacklogsMineros backlog, String usuario) throws Exception {
		BacklogsMinerosBitacoraEstatus backlogsMinerosBitacoraEstatus = new BacklogsMinerosBitacoraEstatus();
		BacklogsMinerosBitacoraEstatusKey backlogsMinerosBitacoraEstatusKey = new BacklogsMinerosBitacoraEstatusKey(
				backlog, new Date());
		backlogsMinerosBitacoraEstatus.setBacklogsMinerosBitacoraEstatusKey(backlogsMinerosBitacoraEstatusKey);
		String idEstatusBacklogMinero = backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero();
		backlogsMinerosBitacoraEstatus.setIdEstatusBacklogMinero(idEstatusBacklogMinero);
		backlogsMinerosBitacoraEstatus.setUsuarioEstatusInicio(usuario);
		backlogsMinerosBitacoraEstatusFacade.guardarBacklogMineroBitacoraEstatus(backlogsMinerosBitacoraEstatus);
	}

	public void asignarDetalleRefaBacklogMinero(BacklogsMineros backlog,
			List<BacklogsMinerosDetalleRefa> listaRefacciones) throws Exception {

		for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : listaRefacciones) {
			BacklogsMinerosKey idBacklogMinero = backlog.getBacklogsMinerosKey();
			BacklogsMinerosDetalleRefaKey backlogsMinerosDetalleRefaKey = new BacklogsMinerosDetalleRefaKey(
					idBacklogMinero);
			backlogsMinerosDetalleRefa.setBacklogsMinerosDetalleRefaKey(backlogsMinerosDetalleRefaKey);
			Marca idMarca = backlog.getIdMarca();
			backlogsMinerosDetalleRefa.setIdMarca(idMarca);
			backlogsMinerosDetalleRefa.setPrecio(backlogsMinerosDetalleRefa.getPrecio());
			backlogsMinerosDetalleRefa.setTotal(backlogsMinerosDetalleRefa.getTotal());
			String numPart = backlogsMinerosDetalleRefa.getNumeroParte().equals("") ? ""
					: backlogsMinerosDetalleRefa.getNumeroParte().toUpperCase();

			backlogsMinerosDetalleRefa.setNumeroParte(numPart);
			String descripcionParte = backlogsMinerosDetalleRefa.getDescripcionMayuscula().equals("") ? null
					: backlogsMinerosDetalleRefa.getDescripcionMayuscula();
			backlogsMinerosDetalleRefa.setDescripcionParte(descripcionParte);
			String observaciones = backlogsMinerosDetalleRefa.getObservacionesMayuscula().equals("") ? null
					: backlogsMinerosDetalleRefa.getObservacionesMayuscula();
			backlogsMinerosDetalleRefa.setObservaciones(observaciones);
			backlogsMinerosDetalleRefaFacade.guardarBacklogMinerosDetalleRefa(backlogsMinerosDetalleRefa);
		}

	}

	public void asignarImagenesBacklogMinero(BacklogsMineros backlog) {
		ImagenesBacklogsMinerosFacade backlogsMinerosFacade = new ImagenesBacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		ImagenesBacklogsMineros imagenesBacklogsMineros = new ImagenesBacklogsMineros();
		try {
			String rutaBacklog = backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			rutaBacklog = rutaBacklog.replaceAll("-", "");
			String rutaFinal = rutaArchivosTemporal + rutaBacklog + "/";
			int contadorNombre = 1;

			for (ImagenBlDto imagenBlDto : listaImagenesDto) {
				// Esto para que guarde el tipo del archivo con el nombre especifico, se
				// utiliza
				// el arreglo debido a que se necesita qué tipo de archivo es.
				if (imagenBlDto.isExiste() == false) {
					String nombreArchivo = "";
					String rutaImagenTemporal = imagenBlDto.getRutaImagen();
					if (imagenBlDto.isBase64()) {
						String mimeType = imagenBlDto.getMimeType();
						String[] split = mimeType.split("/");
						nombreArchivo = rutaBacklog + "-" + formato.format(contadorNombre) + "." + split[1];
					} else {
						nombreArchivo = rutaBacklog + "-" + formato.format(contadorNombre);
					}
					imagenesBacklogsMineros.setEstatusImagen(true);
					BacklogsMineros idBacklogMinero = backlog;
					ImagenesBacklogsMinerosKey imagenesBacklogsMinerosKey = new ImagenesBacklogsMinerosKey(
							rutaFinal + nombreArchivo, idBacklogMinero);
					imagenesBacklogsMineros.setImagenesBacklogsMinerosKey(imagenesBacklogsMinerosKey);
					backlogsMinerosFacade.guardarImagenesBacklogsMineros(imagenesBacklogsMineros);
					InputStream imagen = null;
					if (imagenBlDto.isBase64()) {
						byte[] imageTmp = Base64.getDecoder().decode(imagenBlDto.getRutaBase64());
						imagen = new ByteArrayInputStream(imageTmp);
						
					} else {
						imagen = new BufferedInputStream(new FileInputStream(rutaImagenTemporal));
					}
					copiarArchivo(rutaFinal, nombreArchivo, imagen);

					contadorNombre++;

				}
			}

			hayImagenes = listaImagenesDto.isEmpty(); // 1
			if (!listaImagenesDto.isEmpty()) {
				for (ImagenBlDto imagenBlDto : listaImagenesDto) {
					if (imagenBlDto.isExiste() == false) {
						String nombreArchivoImagen = imagenBlDto.getRutaImagen();
						File fileImagen = new File(nombreArchivoImagen);
						borraArchivosDeImagenes(fileImagen);
					}
				}
			}
		} catch (Exception e) {
			String error = "No se pudieron guardar las imagenes del backlog minero "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}

	}

	public void borraArchivosDeImagenes(File file) {
		if (file.exists()) {
			if (!file.delete()) {
				log.error("La operación de borrado de archivo ha fallado");
				log.error(file.getAbsolutePath());
			}
		}
	}

	public boolean copiarArchivo(String destination, String fileName, InputStream in) throws Exception {
		boolean copiaCorrecta = true;
		OutputStream out = null;
		File theDir = new File(destination);

		// Crea el directorio
		if (!theDir.exists()) {
			try {
				theDir.mkdir();
			} catch (SecurityException se) {
				String error = "No se ha podido guardar el archivo " + fileName + " en la ruta " + destination;
				log.error(error, se);
				agregarMensajeError(error);
			}
		}
		// -------------------------------------------

		try {
			// File file = new File(destination+fileName);
			// file.createNewFile();
			// crearImagenesTemporales(file, in);

			out = new FileOutputStream(destination + fileName);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {
			copiaCorrecta = false;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
				try {
					out.close();
				} catch (IOException ex) {
					copiaCorrecta = false;
				}
			}
		}
		return copiaCorrecta;
	}

	/**
	 * Guarda los riesgos de un backlog
	 * 
	 * @param backlog
	 */
	public void asignarRiesgosDeBacklogs(BacklogsMineros backlog) {
		SintomasYRiesgosFacade sintomasYRiesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
		int idBacklog = backlog.getBacklogsMinerosKey().getIdBacklogMinero();
		RiesgosTrabajo riesgo;
		try {
			for (String idRiesgo : backlog.getIdCodigoRiesgoList()) {
				riesgo = new RiesgosTrabajo(idRiesgo, "", idBacklog, (int) this.obtenerSucursalFiltro());
				sintomasYRiesgosFacade.guardarRiesgosDeBacklog(riesgo);
			}
		} catch (Exception e) {
			String error = "No se pudo guardar los riesgos del backlog";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
	}

	public BacklogsMineros getBacklogEntity() {
		return backlogEntity;
	}

	public void setBacklogEntity(BacklogsMineros backlogEntity) {
		this.backlogEntity = backlogEntity;
	}

	public CatInspectQuestionsEntity getQuestionEntity() {
		return questionEntity;
	}

	public void setQuestionEntity(CatInspectQuestionsEntity questionEntity) {
		this.questionEntity = questionEntity;
	}

	public List<OrigenesBacklogsMineros> getOrigenesBacklogsMinerosList() {
		return origenesBacklogsMinerosList;
	}

	public void setOrigenesBacklogsMinerosList(List<OrigenesBacklogsMineros> origenesBacklogsMinerosList) {
		this.origenesBacklogsMinerosList = origenesBacklogsMinerosList;
	}

	public List<PrioridadesBacklogsMineros> getPrioridadesBacklogsMinerosList() {
		return prioridadesBacklogsMinerosList;
	}

	public void setPrioridadesBacklogsMinerosList(List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList) {
		this.prioridadesBacklogsMinerosList = prioridadesBacklogsMinerosList;
	}

	public List<CodigosSistemas> getCodigosSistemasList() {
		return codigosSistemasList;
	}

	public void setCodigosSistemasList(List<CodigosSistemas> codigosSistemasList) {
		this.codigosSistemasList = codigosSistemasList;
	}

	public List<LadoComponente> getLadoComponenteList() {
		return ladoComponenteList;
	}

	public void setLadoComponenteList(List<LadoComponente> ladoComponenteList) {
		this.ladoComponenteList = ladoComponenteList;
	}

	public List<Sintomas> getSintomasList() {
		return sintomasList;
	}

	public void setSintomasList(List<Sintomas> sintomasList) {
		this.sintomasList = sintomasList;
	}

	public List<RiesgosTrabajo> getRiesgosTabajosList() {
		return riesgosTabajosList;
	}

	public void setRiesgosTabajosList(List<RiesgosTrabajo> riesgosTabajosList) {
		this.riesgosTabajosList = riesgosTabajosList;
	}

	public List<LugaresOrigenBacklogsMineros> getLugaresOrigenBacklogsMinerosList() {
		return lugaresOrigenBacklogsMinerosList;
	}

	public void setLugaresOrigenBacklogsMinerosList(
			List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList) {
		this.lugaresOrigenBacklogsMinerosList = lugaresOrigenBacklogsMinerosList;
	}

	public boolean isHabilitaCodigoTrabajo() {
		return habilitaCodigoTrabajo;
	}

	public void setHabilitaCodigoTrabajo(boolean habilitaCodigoTrabajo) {
		this.habilitaCodigoTrabajo = habilitaCodigoTrabajo;
	}

	public List<CargosTrabajo> getCargosTrabajosList() {
		return cargosTrabajosList;
	}

	public void setCargosTrabajosList(List<CargosTrabajo> cargosTrabajosList) {
		this.cargosTrabajosList = cargosTrabajosList;
	}

	/**
	 * Formatea el total de las refacciones
	 * 
	 * @return total formateado
	 */
	public String getTotalFormateado() {
		String totalFormateado = "";
		if (this.totalRefacciones != null) {
			totalFormateado = numberFormat.format(this.totalRefacciones);
		}
		return totalFormateado;
	}

	public void setTotalFormateado(String totalFormateado) {
		this.totalFormateado = totalFormateado;
	}

	public Double getTotalRefacciones() {
		return totalRefacciones;
	}

	public void setTotalRefacciones(Double totalRefacciones) {
		this.totalRefacciones = totalRefacciones;
	}

	public List<ImagenBlDto> getListaImagenesDto() {
		return listaImagenesDto;
	}

	public void setListaImagenesDto(List<ImagenBlDto> listaImagenesDto) {
		this.listaImagenesDto = listaImagenesDto;
	}

	public List<BacklogsMinerosDetalleRefa> getPartesSeleccionadas() {
		return partesSeleccionadas;
	}

	public void setPartesSeleccionadas(List<BacklogsMinerosDetalleRefa> partesSeleccionadas) {
		this.partesSeleccionadas = partesSeleccionadas;
	}

}
