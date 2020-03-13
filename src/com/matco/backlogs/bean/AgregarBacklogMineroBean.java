package com.matco.backlogs.bean;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.event.ActionEvent;
import javax.faces.bean.ViewScoped;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import com.matco.amce2.dto.GetMueSosDto;
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
import com.matco.amce3.facade.RevisionKardexFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.dto.ImagenBlDto;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosBitacoraEstatus;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.BacklogMineroEstandar;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.ImagenesBacklogsMineros;
import com.matco.backlogs.entity.LugaresOrigenBacklogsMineros;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RevisionKardexEntity;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.key.BacklogsMinerosBitacoraEstatusKey;
import com.matco.backlogs.entity.key.BacklogsMinerosDetalleRefaKey;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.backlogs.entity.key.ImagenesBacklogsMinerosKey;
import com.matco.ejes.entity.Almacen;
import com.matco.ejes.entity.Marca;

@ManagedBean(name = "agregarBacklogsMinerosBean")
@ViewScoped
public class AgregarBacklogMineroBean extends GenericBacklogBean implements Serializable {
	private static final DecimalFormat formato = new DecimalFormat("00");
	private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	private static final long serialVersionUID = -1272206086100944040L;
	private static final Logger log = Logger.getLogger(AgregarBacklogMineroBean.class);
	private final String rutaArchivosTemporal = getExternalContext().getInitParameter("rutaImagenesBacklogsMineros");
	private BacklogsMinerosDetalleRefa backlogsMinerosDetalle;
	private BacklogsMineros backlogMineroSeleccionado;
	private BacklogsMineros backlogRepetido;
	private BacklogsMinerosKey backlogMineroKey;
	private BacklogMineroEstandar backlogEstandar;
	private List<OrigenesBacklogsMineros> origenesBacklogsMinerosList = new ArrayList<>();
	private List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList = new ArrayList<>();
	private List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList = new ArrayList<>();
	private List<CodigosSistemas> codigosSistemasList = new ArrayList<>();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();
	private List<Sintomas> sintomasList = new ArrayList<>();
	private List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<>();
	private String[] idCodigoRiesgoList;
	private List<CargosTrabajo> cargosTrabajosList = new ArrayList<>();
	private List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();
	private List<String> numeroEconomicoList = new ArrayList<>();
	private List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();
	private List<String> tipoArchivo = new ArrayList<>();
	private List<BacklogsMinerosDetalleRefa> partesSeleccionadas = new ArrayList<>();
	private List<ImagenesBacklogsMineros> imagenesBacklog;// se usa en editar
	private List<String> listaImagenes;// se usa en editar
	private List<GetMueSosDto> muestrasSeleccionadas;
	private List<ImagenBlDto> listaImagenesDto = new ArrayList<>();
	private List<ImagenBlDto> imagenesSeleccionadasList = new ArrayList<>();
	private LoginBean loginBean = this.obtenerBean("loginBean");
	private LadoComponente ladoComponente = new LadoComponente();
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private RevisionKardexEntity revisionSeleccionada;

	private boolean habilitaNumSerie = true;
	private boolean seGuarda = true;
	private boolean hayImagenes = true;
	private boolean habilitaCodigoTrabajo;
	private boolean conservarImagenes;
	private boolean ladoComponenteAnterior = false;
	private Double totalRefacciones;
	private Double horasHombre;
	private Double horasMaquina;
	private Integer horometro;
	private Integer codigosSistemas;
	private Integer codigoTrabajo;
	private Short cargosTrabajo;
	private Short idCodigoTrabajoSeleccionado;
	private Short prioridadesBacklogsMineros;
	private Short lugaresOrigenBacklogsMineros;
	private Short origenesBacklogsMineros;
	private String respuestaRevision;
	private String folioMuestra;
	private String folio;
	private String maquinaria;
	private String numEconomico;
	private String accion; // sintoma
	private String accionEquipo;
	private String ladoComponenteAnteriorString;
	private String idLadoCompontente;
	private String idCodigoSintoma;
	private String notasSobreBL;
	private String requeridoPor;
	private String otrosMaterialesEquipo;
	private String trabajoRealizar;
	private String tipoTrabajo;
	private String cargarImagenes = "PF('dialogoImagenes').show();";
	private String error;
	private String usuario;
	private String codigoTrabajoDescripcion; // código de trabajo - Descripción de trabajo seleccionado
	private String rutaImagenesTemporales;
	private String TIPOS_DE_TRABAJO[] = { "MANO DE OBRA", "SOLDADURA", "PROGRAMADO", "VERIFICAR", "DIAGNOSTICO VIMS",
			"SOS", "OPERACIÓN" };

	BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
	
	@PostConstruct
	public void init() {
		this.seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

		revisionSeleccionada = seleccionBean.getRevisionSeleccionada();

		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";

		rutaImagenesTemporales = System.getProperty("jboss.server.temp.dir") + File.separator;

		// idSucursal = obtenerSucursal();
		//backlogMineroSeleccionado = seleccionBean.getBacklogsMinerosSeleccionado();
		maquinariaDtoList = listarMaquinariaDtoBacklogsMineros();
		numeroEconomicoList = createNumEconomicoList(maquinariaDtoList);
		origenesBacklogsMinerosList = listarOrigenesBacklogsMineros();
		lugaresOrigenBacklogsMinerosList = listarLugaresOrigenesBacklogsMineros();
		prioridadesBacklogsMinerosList = listarPrioridadesBacklogsMineros();
		codigosSistemasList = listarSistemasBacklogsMineros();
		ladoComponenteList = listarLadoComponentes();
		sintomasList = listarSintomas();
		cargosTrabajosList = listarCargosTrabajoBacklogsMineros();
		riesgosTabajosList = listarRiesgosTrabajos();
		
		habilitaCodigoTrabajo = true;

		if (seleccionBean != null) {

			// si se esta creando desde muestras
			muestrasSeleccionadas = seleccionBean.getMuestrasSeleccionadas();
			if (!muestrasSeleccionadas.isEmpty()) {
				muestraBacklog();
			} else if (seleccionBean.getBacklogsMinerosSeleccionado() != null) { // por si estan editando un backlog
				backlogMineroSeleccionado = seleccionBean.getBacklogsMinerosSeleccionado();
				backlogMineroKey = backlogMineroSeleccionado.getBacklogsMinerosKey();
				folio = backlogMineroKey.getBacklogMineroAlmacenFormateado();
				horometro = backlogMineroSeleccionado.getHorometro();
				maquinaria = backlogMineroSeleccionado.getNumeroSerie();
				numEconomico = backlogMineroSeleccionado.getNumeroEconomico(); // se agrego el numero economico
				origenesBacklogsMineros = backlogMineroSeleccionado.getOrigenesBacklogsMineros()
						.getIdOrigenBacklogMinero();
				lugaresOrigenBacklogsMineros = backlogMineroSeleccionado.getLugaresOrigenesBacklogsMineros()
						.getIdLugarOrigenBacklogMinero();
				prioridadesBacklogsMineros = backlogMineroSeleccionado.getIdPrioridadBacklog()
						.getIdPrioridadBacklogMinero();
				codigosSistemas = backlogMineroSeleccionado.getIdCodigoSistema().getCodigoSistema();

				ladoComponente = backlogMineroSeleccionado.getLadoComponenteOb();
				idLadoCompontente = ladoComponente.getCodigoLDC();

				if (idLadoCompontente == null) {
					ladoComponenteAnterior = true;
					ladoComponenteAnteriorString = ladoComponente.getDescripcion();
				}

				idCodigoSintoma = backlogMineroSeleccionado.getSintoma().getIdCodigoSintoma();

				codigoTrabajoDescripcion = backlogMineroSeleccionado.getIdCodigoSMCS().getCodigoSMCS().toString();
				idCodigoTrabajoSeleccionado = backlogMineroSeleccionado.getIdCodigoSMCS().getIdCodigoSMCS();

				trabajoRealizar = backlogMineroSeleccionado.getIdCodigoSMCS().getDescripcionSMCS();
				codigoTrabajoDescripcion = codigoTrabajoDescripcion + " - " + trabajoRealizar;

				accion = backlogMineroSeleccionado.getSintomasEquipo();
				accionEquipo = backlogMineroSeleccionado.getAccionEquipo();
				horasMaquina = backlogMineroSeleccionado.getHorasMaquinaEstimadas();
				horasHombre = backlogMineroSeleccionado.getHorasHombreEstimadas();
				cargosTrabajo = backlogMineroSeleccionado.getIdCargoTrabajo().getIdCargoTrabajo();
				notasSobreBL = backlogMineroSeleccionado.getComentarioBacklogMinero();
				requeridoPor = backlogMineroSeleccionado.getSolicitadoPor();
				tipoTrabajo = backlogMineroSeleccionado.getTipoTrabajo();
				otrosMaterialesEquipo = backlogMineroSeleccionado.getOtrosMateriales();

				// idCodigoSintoma =
				// backlogMineroSeleccionado.getSintoma().getSintomaFormateado();

				backlogsMinerosDetalleRefaList = obtenerPartes(backlogMineroKey);
				generarSubtotalRefacciones(backlogsMinerosDetalleRefaList);
				totalRefacciones = calcularTotalRefacciones();

				lugaresOrigenBacklogsMinerosList = listarLugaresOrigenesBacklogsMineros();
				codigosSistemasList = listarSistemasBacklogsMineros();
				cargosTrabajosList = listarCargosTrabajoBacklogsMineros();

				SintomasYRiesgosFacade riesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
				RiesgosTrabajo riesgo = new RiesgosTrabajo();
				riesgo.setIdBacklog(backlogMineroKey.getIdBacklogMinero());
				riesgo.setIdAlmacen((int) obtenerSucursalFiltro());
				try {
					List<RiesgosTrabajo> riesgosBL = riesgosFacade.getRiesgosDeunBacklog(riesgo);
					idCodigoRiesgoList = new String[riesgosBL.size()];
					for (int i = 0; i < riesgosBL.size(); i++) {
						idCodigoRiesgoList[i] = riesgosBL.get(i).getIdCodigoRiesgo();
					}

				} catch (Exception e2) {
					e2.printStackTrace();
				}

				ImagenesBacklogsMinerosFacade imagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(
						RUTA_PROPERTIES_AMCE3);

				habilitaNumSerie = false;
				try {
					listarImagenes();
				} catch (Exception e1) {
					System.out.println("No se pudieron listar imágenes");
				}

				try {
					imagenesBacklog = imagenesBacklogsMinerosFacade
							.obtenerImagenesBacklogsMinerosPorID(backlogMineroKey);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// seleccionBean.setBacklogsMinerosSeleccionado(null);
			} else if (seleccionBean.getBacklogEstandarSelected() != null) {
				backlogEstandar = seleccionBean.getBacklogEstandarSelected();
				backlogEstandar.cargarVariables();
				try {
					asignarCamposBacklogEstandar();
				} catch (Exception e) {
					log.error("No se pudo cargar el Backlog Estándar", e);
					agregarMensajeError(summary, "No se pudo cargar el Backlog Estándar");
				}
				habilitaNumSerie = false;
			}
		}
	}

	/**
	 * Envia los datalles del backlog
	 */
	public void agregarRevisionKardex(int idBacklogMinero, int idAlmacen) {
		RevisionKardexFacade facade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
		RevisionKardexEntity revision = new RevisionKardexEntity();
		try {

			revision.setComentariosSolicitante("");
			revision.setUsuarioSolicitante("");
			revision.setEstatusRevision(0);
			revision.setIdBacklogMinero(idBacklogMinero);
			revision.setIdAlmacen(idAlmacen);
			revision.setUsuarioCreador(usuario);

			facade.agregarRevisionKardex(revision);

			// agregarMensajeInfo(summary, "Se ha enviado la revisión correctamente");
			// revisionBacklog = obtenerRevisionBacklogActual();

		} catch (Exception e) {
			String error = "No se pudo agregar la revision";
			this.agregarMensajeError(error);
			log.error(e);
		}
	}

	/**
	 * Obtiene los datos del Backlog Estandar y los convierte a Backlog normal
	 * @throws Exception 
	 */
	public void asignarCamposBacklogEstandar() throws Exception {
		if (backlogEstandar != null) {
			origenesBacklogsMineros = backlogEstandar.getOrigenesBacklogsMineros();
			lugaresOrigenBacklogsMineros = backlogEstandar.getLugaresOrigenesBacklogsMineros();
			prioridadesBacklogsMineros = backlogEstandar.getIdProriedad();
			codigosSistemas = backlogEstandar.getCodigosSistemas();
			idLadoCompontente = backlogEstandar.getLadoComponente().getCodigoLDC();
			idCodigoSintoma = backlogEstandar.getSintoma().getIdCodigoSintoma();
			accion = backlogEstandar.getSintomasEquipo();
			
			codigoTrabajoDescripcion = backlogEstandar.getCodigoTrabajoDescripcion();
			idCodigoTrabajoSeleccionado = backlogEstandar.getCodigoTrabajo().shortValue();
			cambiarCodigoTrabajo();
			
			accionEquipo = backlogEstandar.getAccionEquipo();
			horasMaquina = backlogEstandar.getHorasMaquinaEstimadas();
			horasHombre = backlogEstandar.getHorasHombreEstimadas();
			cargosTrabajo = backlogEstandar.getCargoTrabajo().getIdCargoTrabajo();
			notasSobreBL = backlogEstandar.getNotasBacklog();
			idCodigoRiesgoList = backlogEstandar.getIdCodigoRiesgoList();

			tipoTrabajo = backlogEstandar.getTipoTrabajo();
			otrosMaterialesEquipo = backlogEstandar.getOtrosMateriales();
			
			backlogsMinerosDetalleRefaList = backlogEstandar.getListaRefacciones();
			generarSubtotalRefacciones(backlogsMinerosDetalleRefaList);
			totalRefacciones = calcularTotalRefacciones();
		}
	}

	public void muestraBacklog() {
		GetMueSosDto muestraSeleccionada = muestrasSeleccionadas.remove(0);
		maquinaria = muestraSeleccionada.getNumeroSerie();
		habilitarControlesCampos();
		origenesBacklogsMineros = 2;
		tipoTrabajo = "SOS";
		accion = muestraSeleccionada.getDescripcionMuestra();
		// ladoComponente = muestraSeleccionada.getDescripcionCompartimento();

		folioMuestra = muestraSeleccionada.getNumeroMuestraMatco();
	}

	/*
	 * 1. Verificar si la lista de las imágenes está vacía. 2. Borra los archivos
	 * temporales que se encuentran dentro del servidor.
	 */
	public void cerrarDialogoImagenes() {
		hayImagenes = listaImagenesDto.isEmpty();// 1
	}

	private boolean borraArchivosDeImagenes(File file) {
		boolean operacion = true;
		if (file.exists()) {
			if (!file.delete()) {
				log.error("La operación de borrado de archivo ha fallado");
				log.error(file.getAbsolutePath());
				operacion = false;
			}
		}
		return operacion;
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

	/**
	 * Borra las imagenes que esten seleccionadas de la base de datos
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void borrarImagenesDB(ActionEvent event) {
		// String mensaje = "";
		ImagenesBacklogsMinerosFacade backlogsMinerosFacade = new ImagenesBacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		ImagenesBacklogsMineros imagenesBacklogsMineros;
		try {
			for (ImagenBlDto imagen : imagenesSeleccionadasList) {
				System.out.println("imagen: " + imagen.getNombre());
				imagenesBacklogsMineros = new ImagenesBacklogsMineros();
				imagenesBacklogsMineros.setRutaImagenReal(imagen.getRutaImagenReal());
				imagenesBacklogsMineros.setIdAlmacen(obtenerSucursalFiltro());
				if (conservarImagenes == false) {
					// borramos las imagenes del disco duro tambien
					File imgtmp = new File(imagen.getRutaImagenReal());
					imgtmp.delete();
				}
				backlogsMinerosFacade.eliminarImagen(imagenesBacklogsMineros);
				agregarMensajeInfo(summary, "Se borro correctamente la imagen: " + imagen.getNombre());
			}
			listaImagenesDto.clear();
			listarImagenes();
			limpiarSeleccionImg();
			PrimeFaces.current().executeScript("PF('dialogBorrarImg').hide();");
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo borrar la imagen del disco");
			log.error("No se pudo borrar la imagen del disco", e);
		}

	}

	/**
	 * Abre o cierra el dialogo de selección de Backlogs Estándar
	 * Activa una variable para indicar la redireccion
	 */
	public void activarDialogoBLEST() {
		PrimeFaces.current().executeScript("PF('dialogBlEstandar').show();");
		seleccionBean.setRedireccionDialogBLEST("registroBacklogs.xhtml");
	}
	
	/**
	 * Deselecciona las imagenes que se seleccionaron en la tabla para borrar
	 * imagenes
	 */
	public void limpiarSeleccionImg() {
		imagenesSeleccionadasList.clear();
	}

	public void showDialogBorrarImg() {
		PrimeFaces.current().executeScript("PF('dialogBorrarImg').show();");
		
	}

	/*
	 * Se ejecuta al seleccionar el botón de borrar cuando las imágenes ya han sido
	 * cargadas.
	 */
	public void borrarImagenes(boolean keep) {
		String mensaje = "";

		if (keep) {
			if (listaImagenesDto.isEmpty()) {
				mensaje = "No hay imagenes cargadas.";
				agregarMensajeError(summary, mensaje);
				return;
			}

			listaImagenesDto.clear();
			hayImagenes = listaImagenesDto.isEmpty();
			mensaje = "Las imagenes se borraron correctamente.";
			agregarMensajeWarn(summary, mensaje);
		} else {
			listaImagenesDto.clear();
			hayImagenes = listaImagenesDto.isEmpty();
		}
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
		hayImagenes = listaImagenesDto.isEmpty();
		tipoArchivo.add(nombre);
	}

	public static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
		PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
		byte[] bom = new byte[3];
		if (pushbackInputStream.read(bom) != -1) {
			if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
				pushbackInputStream.unread(bom);
			}
		}
		return pushbackInputStream;
	}

	/**
	 * Si es posible debido a que esta abierto un flujo de entrada lo cierra
	 * 
	 * @param inputStream Flujo a cerrar
	 */
	protected void cierraInputStream(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	/**
	 * Si es posible debido a que esta abierto un flujo de entrada lo cierra
	 * 
	 * @param inputStream Flujo a cerrar
	 */
	protected void cierraByteArrayOutputStream(ByteArrayOutputStream byteArrayOutPutStream) {
		try {
			if (byteArrayOutPutStream != null) {
				byteArrayOutPutStream.close();
			}
		} catch (IOException e) {
			log.error(e);
		}
	}

	public void crearImagenesTemporales(File file, InputStream contenido) throws IOException {
		FileUtils.copyInputStreamToFile(contenido, file);
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
				backlogsMinerosDetalleRefaList.add(bmdr);
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
				error = "No se ha podido guardar el archivo " + fileName + " en la ruta " + destination;
				log.error(error, se);
				agregarMensajeError(summary, error);
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

	/***
	 * Actualiza las variables correspondientes a las horas hombre y estimadas
	 * después de seleccionar un código de trabajo
	 */
	public void asignarHoras() {
		CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		CodigosSMCS codigo;
		try {
			codigo = codigosSMCSFacade.obtenerCodigosSMCSPorID(idCodigoTrabajoSeleccionado.toString());
			Double horasHombre = codigo.getHoraHombreCodigoSMCS();
			setHorasHombre(horasHombre);
			Double horasMaquina = (horasHombre) / 2;
			setHorasMaquina(horasMaquina);
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
		codigoTrabajoDescripcion = codigoTrabajoDescripcion.trim();
		String codigoTrabajoFormateado = codigoTrabajoDescripcion.split("-")[0].trim();
		codigoTrabajo = Integer.parseInt(codigoTrabajoFormateado);
		trabajoRealizar = codigoTrabajoDescripcion.split("-")[1].trim();
		CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		List<CodigosSMCS> listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		//boolean encontrado = false;
		for (CodigosSMCS codigoTrabajoComparado : listaCodigosTrabajo) {
			int codigo = codigoTrabajoComparado.getCodigoSMCS();
			String descripcion = codigoTrabajoComparado.getDescripcionSMCS();
			if (codigoTrabajo == codigo && descripcion.equals(trabajoRealizar)) {
				idCodigoTrabajoSeleccionado = codigoTrabajoComparado.getIdCodigoSMCS();
				//encontrado = true;
				habilitaCodigoTrabajo = false;
				break;
			}
		}
		/*
		 * if (encontrado) { asignarHoras(); }
		 */
	}

	public void habilitarCodigoTrabajo() {
		codigoTrabajoDescripcion = null; // clave - trabajo
		codigoTrabajo = null; // clave
		trabajoRealizar = null; // trabajo
		idCodigoTrabajoSeleccionado = null; // id fila en bdd
		habilitaCodigoTrabajo = true;
	}

	public void agregarFilaPartesRequeridas(ActionEvent even) {
		backlogsMinerosDetalle = new BacklogsMinerosDetalleRefa((short) 1, "", "", "", 0.0);
		backlogsMinerosDetalleRefaList.add(backlogsMinerosDetalle);
	}

	/**
	 * Busca el numero serie donde coincida con un Num. Economico
	 * 
	 * @param maquinariaDtoList
	 * @param numEconomico
	 * @return maquina || null
	 */
	public String obtenerMaquinariaPorNumEconomico(List<MaquinariaDto> maquinariaDtoList, String numEconomico) {
		for (MaquinariaDto maquina : maquinariaDtoList) {
			if (maquina.getNumeroEconomicoCliente().equals(numEconomico))
				return maquina.getSerie();
		}
		return null;
	}

	/**
	 * Busca el numero economci donde coincida con un numero de serie
	 * 
	 * @param maquinariaDtoList
	 * @param serie
	 * @return maquina.getNumeroEconomicoCliente
	 */
	public String obtenerNumEconomicoPorSerie(List<MaquinariaDto> maquinariaDtoList, String serie) {
		for (MaquinariaDto maquina : maquinariaDtoList) {
			if (maquina.getSerie().equals(serie))
				return maquina.getNumeroEconomicoCliente();
		}
		return "No definido";
	}

	public void seleccionNumEconomico() {
		if (numEconomico == null || numEconomico.equals("")) {
			prepararSiguienteBacklog();
			return;
		}
		maquinaria = obtenerMaquinariaPorNumEconomico(maquinariaDtoList, numEconomico);
		habilitarControlesCampos();
		buscarCliente(maquinaria);
	}

	/**
	 * Este metodo activa los campos para la captura del BL
	 */
	public void seleccionNumSerie() {
		if (maquinaria == null || maquinaria.equals("")) {
			prepararSiguienteBacklog();
			return;
		}
		numEconomico = obtenerNumEconomicoPorSerie(maquinariaDtoList, maquinaria);
		
		habilitarControlesCampos();
		
		buscarCliente(maquinaria);
		
	}

	public void habilitarControlesCampos() {
		habilitaNumSerie = false;
		origenesBacklogsMinerosList = listarOrigenesBacklogsMineros();
		lugaresOrigenBacklogsMinerosList = listarLugaresOrigenesBacklogsMineros();
		codigosSistemasList = listarSistemasBacklogsMineros();

		// System.out.println("NE: "+numEconomico);
		// System.out.println("SERIE "+maquinaria);
		// codigosSMCSList = listarCodigosSMCSBacklogsMineros();
		// codigosSMCSSeleccionadoList = listarDescripcionCodigosSMCSBacklogsMineros();
		cargosTrabajosList = listarCargosTrabajoBacklogsMineros();
		prioridadesBacklogsMinerosList = listarPrioridadesBacklogsMineros();
		//habilitaCodigoTrabajo = true;
	}

	public void deshabilitarControlesCampos() {
		habilitaNumSerie = true;
		origenesBacklogsMinerosList = new ArrayList<>();
		lugaresOrigenBacklogsMinerosList = new ArrayList<>();
		codigosSistemasList = new ArrayList<>();
		// codigosSMCSList = new ArrayList<>();
		// codigosSMCSSeleccionadoList = new ArrayList<>();
		habilitaCodigoTrabajo = false;
		trabajoRealizar = "";
		codigoTrabajo = null;
		idCodigoTrabajoSeleccionado = null;
		codigoTrabajoDescripcion = "";
		cargosTrabajosList = new ArrayList<>();
		prioridadesBacklogsMinerosList = new ArrayList<>();
		backlogsMinerosDetalleRefaList = new ArrayList<>();
		setHorasHombre(null);
		setHorasMaquina(null);
	}

	/***
	 * Asigna los atributos a un backlog minero para agregar o editar
	 * 
	 * @return backlog con los atributos ingresados
	 * @throws Exception
	 */
	public BacklogsMineros asignarCamposBacklogMinero() throws Exception {
		BacklogsMineros backlog = new BacklogsMineros();
		Almacen idAlmacen = new Almacen();
		ladoComponente = new LadoComponente();
		int idBacklog;
		if (backlogMineroSeleccionado == null) {// si es para agregar
			idAlmacen.setAlmacen(obtenerSucursalFiltro());
			idBacklog = 0;
		} else {// si es para editar
			idAlmacen.setAlmacen(backlogMineroKey.getIdAlmacen().getAlmacen());
			idBacklog = backlogMineroKey.getIdBacklogMinero();
		}

		BacklogsMinerosKey bKey = new BacklogsMinerosKey(idBacklog, idAlmacen);
		backlog.setBacklogsMinerosKey(bKey);

		MaquinariaDto maquinaria = new MaquinariaDto();
		for (MaquinariaDto maquinariaDto : maquinariaDtoList) {
			String cadena = maquinariaDto.getSerie();
			if (cadena.equals(this.maquinaria)) {
				maquinaria = maquinariaDto;
				break;
			}
		}
		backlog.setNumeroSerie(maquinaria.getSerie());
		backlog.setModeloEquipo(maquinaria.getIdModelo());
		backlog.setIdMarca(maquinaria.getIdMarca());
		backlog.setNumeroEconomico(maquinaria.getNumeroEconomicoCliente());

		OrigenesBacklogsMineros origenesBacklogsMineros = new OrigenesBacklogsMineros();
		Short idOrigenBacklogMinero = getOrigenesBacklogsMineros();
		origenesBacklogsMineros.setIdOrigenBacklogMinero(idOrigenBacklogMinero);
		backlog.setOrigenesBacklogsMineros(origenesBacklogsMineros);

		LugaresOrigenBacklogsMineros lugaresOrigenBacklogsMineros = new LugaresOrigenBacklogsMineros();
		Short idLugarOrigenBacklogMinero = getLugaresOrigenBacklogsMineros();
		lugaresOrigenBacklogsMineros.setIdLugarOrigenBacklogMinero(idLugarOrigenBacklogMinero);
		backlog.setLugaresOrigenesBacklogsMineros(lugaresOrigenBacklogsMineros);

		Integer horometro = getHorometro();
		backlog.setHorometro(horometro);

		CodigosSistemas idCodigoSistema = new CodigosSistemas();
		Integer codigoSistema = getCodigosSistemas();
		idCodigoSistema.setCodigoSistema(codigoSistema);
		backlog.setIdCodigoSistema(idCodigoSistema);

		CodigosSMCS idCodigoSMCS = new CodigosSMCS();
		Short idCodigo = getIDCodigoSMCSDescripcion();// Este es de la descripción, que es el que importa.
		idCodigoSMCS.setIdCodigoSMCS(idCodigo);
		backlog.setIdCodigoSMCS(idCodigoSMCS);

		String sintomasEquipo = getAccion().toUpperCase();
		backlog.setSintomasEquipo(sintomasEquipo);

		PrioridadesBacklogsMineros idPrioridadBacklog = new PrioridadesBacklogsMineros();
		Short idPrioridadBacklogMinero = getPrioridadesBacklogsMineros();
		idPrioridadBacklog.setIdPrioridadBacklogMinero(idPrioridadBacklogMinero);
		backlog.setIdPrioridadBacklog(idPrioridadBacklog);

		Double horasHombreEstimadas = getHorasHombre();
		backlog.setHorasHombreEstimadas(horasHombreEstimadas);

		Double horasMaquinaEstimadas = getHorasMaquina();
		backlog.setHorasMaquinaEstimadas(horasMaquinaEstimadas);

		String comentarioBacklogMinero = getNotasSobreBL().toUpperCase();
		backlog.setComentarioBacklogMinero(comentarioBacklogMinero);

		String solicitadoPor = getRequeridoPor().toUpperCase();
		backlog.setSolicitadoPor(solicitadoPor);

		backlog.setTrabajoRealizar(trabajoRealizar);

		CargosTrabajo idCargoTrabajo = new CargosTrabajo();
		Short cargoTrabajo = getCargosTrabajo();
		idCargoTrabajo.setIdCargoTrabajo(cargoTrabajo);
		backlog.setIdCargoTrabajo(idCargoTrabajo);

		backlog.setAccionEquipo(getAccionEquipo().toUpperCase());

		Sintomas sintoma = new Sintomas();
		sintoma.setIdCodigoSintoma(this.idCodigoSintoma);
		backlog.setSintoma(sintoma);

		if (backlogMineroSeleccionado == null) {// crear
			EstatusBacklogsMineros idEstatusBacklogsMineros = new EstatusBacklogsMineros();
			String estatusBacklogMinero = "A"; // ESTATUS CREADO
			idEstatusBacklogsMineros.setIdEstatusBacklogMinero(estatusBacklogMinero);
			backlog.setIdEstatusBacklogsMineros(idEstatusBacklogsMineros);
		}

		// ladoComponenteString = ladoComponenteString.trim();
		// String[] splitLD = ladoComponenteString.split("-");

		// String codigoLD = splitLD[0];
		// String descripcionLD = splitLD[1];

		if (idLadoCompontente != null) {
			ladoComponente.setCodigoLDC(idLadoCompontente);
		}

		// ladoComponente.setDescripcion(descripcionLD.toUpperCase());

		backlog.setLadoComponenteOb(ladoComponente);

		backlog.setTipoTrabajo(tipoTrabajo);
		otrosMaterialesEquipo = otrosMaterialesEquipo.toUpperCase();
		backlog.setOtrosMateriales(otrosMaterialesEquipo);
		if (backlogMineroSeleccionado == null) {// agregar
			backlog.setCreadoPor(usuario);
		} else {// editar
			backlog.setModificadoPor(usuario);
		}
		return backlog;
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

	public void borrarFilaPartesRequeridas() {
		if (backlogsMinerosDetalleRefaList.size() != 0) {
			int indice = backlogsMinerosDetalleRefaList.size() - 1;
			backlogsMinerosDetalleRefaList.remove(indice);
			totalRefacciones = calcularTotalRefacciones();
		}
	}

	/***
	 * En editar backlog borra las partes seleccionadas
	 */
	public void borrarFilasPartes(ActionEvent even) {
		try {
			for (BacklogsMinerosDetalleRefa refa : partesSeleccionadas) {
				backlogsMinerosDetalleRefaList.remove(refa);
			}
			partesSeleccionadas.clear();
			totalRefacciones = calcularTotalRefacciones();
		} catch (Exception e) {
			agregarMensajeWarn(summary, "No se ha seleccionado una refacción");
			// log.error(error, e); se concidera mas como una alerta
		}
	}

	/***
	 * edita la informacion de la tabla backlogs mineros
	 * 
	 * @throws Exception
	 */
	public void editarBacklogMinero() throws Exception {
		
		if (seleccionBean.isRevisionBacklog()) {
			if (respuestaRevision == null || respuestaRevision.equals("")) {
				agregarMensajeWarn(summary, "El campo 'Respuesta de revisión' está vacío y se requiere para continuar.");
				return;
			}
		}

		if (verificarCamposBacklog() != true && verificarPartesRequeridas() != true) {
			BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
			BacklogsMineros instancia = asignarCamposBacklogMinero();
			
			editarRefacciones(instancia);

			asignarDetalleRefaBacklogMinero(instancia);

			SintomasYRiesgosFacade riesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
			RiesgosTrabajo riesgoTmp = new RiesgosTrabajo();
			riesgoTmp.setIdBacklog(instancia.getBacklogsMinerosKey().getIdBacklogMinero());
			riesgoTmp.setIdAlmacen((int) obtenerSucursalFiltro());

			riesgosFacade.eliminarRiesgosDeBacklog(riesgoTmp);

			if (!riesgosTabajosList.isEmpty()) {
				asignarRiesgosDeBacklogs(instancia);
			}

			if (!listaImagenesDto.isEmpty()) {
				asignarImagenesBacklogMinero(instancia);
			}

			if (seleccionBean.isRevisionBacklog()) {
				try {
					RevisionKardexFacade revisionFacade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);
					RevisionKardexEntity revisionEntity = seleccionBean.getRevisionSeleccionada();
					revisionEntity.setComentariosCreador(respuestaRevision.toUpperCase());
					if (revisionEntity.getEstatusRevision() == 1) {
						revisionEntity.setEstatusRevision(2);
					}
					if (revisionEntity.getEstatusRevision() == 3) {
						revisionEntity.setEstatusRevision(4);
					}
					revisionEntity.setUsuarioCreador(usuario);
					revisionFacade.modificarRevisionKardex(revisionEntity);
				} catch (Exception e) {
					error = "No se pudo modificar la revisión";
					log.error(error, e);
					agregarMensajeError(summary, error);
				}
			}

			try {
				backlogsMinerosFacade.modificarBacklogMinero(instancia);
				this.agregarMensajeInfoKeep(summary, "El Backlog Minero se ha editado correctamente.");
				seGuarda = true;
				
				seleccionBean.setRevisionBacklog(false);
				seleccionBean.setRevisionSeleccionada(null);
				
				redireccionar("menuBacklogs");
			} catch (Exception e) {
				error = "No se pudo editar el Backlog Minero";
				log.error(error, e);
				agregarMensajeError(summary, error);
			}
		} else {
			verificarPartesRequeridas();
		}
	}

	/***
	 * editar refacciones
	 * 
	 * @throws Exception
	 */
	public void editarRefacciones(BacklogsMineros backlog) throws Exception {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
		BacklogsMinerosKey idBacklog = backlog.getBacklogsMinerosKey();
		BacklogsMinerosDetalleRefaKey key = new BacklogsMinerosDetalleRefaKey(idBacklog);
		BacklogsMinerosDetalleRefa refaccionAuxiliar = new BacklogsMinerosDetalleRefa();
		refaccionAuxiliar.setBacklogsMinerosDetalleRefaKey(key);
		backlogsMinerosDetalleRefaFacade.eliminarBacklogMinerosDetalleRefa(refaccionAuxiliar);
	}

	/**
	 * Verifica los campos del backlog antes de ser registrado
	 * 
	 * @return boolean
	 */
	public boolean verificarCamposBacklog() {
		boolean errorCampos = false;

		if (maquinaria == null) {
			agregarMensajeWarn(summary, "El campo 'Número de serie' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (accion.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Sintomas' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (requeridoPor.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Requerido por' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (horometro == null) {
			agregarMensajeWarn(summary, "El campo 'Horometro' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (idLadoCompontente == null || idLadoCompontente.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Lado del componente' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (origenesBacklogsMineros == null) {
			agregarMensajeWarn(summary, "El campo 'Origen del BL' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (prioridadesBacklogsMineros == null) {
			agregarMensajeWarn(summary, "El campo 'Prioridad' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (tipoTrabajo == null) {
			agregarMensajeWarn(summary, "El campo 'Tipo de trabajo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (codigosSistemas == null) {
			agregarMensajeWarn(summary, "El campo 'Sistema' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (cargosTrabajo == null) {
			agregarMensajeWarn(summary, "El campo 'Cargo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (idCodigoTrabajoSeleccionado == null) {
			agregarMensajeWarn(summary, "El campo 'Código de trabajo' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (idCodigoSintoma == null || idCodigoSintoma.equals("")) {
			agregarMensajeWarn(summary, "El campo 'Código de sintomas' está vacio y se requiere para continuar.");
			errorCampos = true;
		}

		if (idCodigoRiesgoList == null || idCodigoRiesgoList.length == 0) {
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
		if (!backlogsMinerosDetalleRefaList.isEmpty()) {
			for (BacklogsMinerosDetalleRefa refaccion : backlogsMinerosDetalleRefaList) {

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
				
				if (seleccionBean.getIdClienteMatco() == null) {
					buscarCliente(maquinaria);
				}

				boolean existe = analizarNumeroPMatco(refaccion.getNumeroParte());
				if (existe == false) {
					agregarMensajeWarn(summary, "No se encontró el Número de parte MATCO "
							+ refaccion.getNumeroParte()+" con el Cliente: "
							+seleccionBean.getIdClienteMatco());
					//incompleto = true;
				}
			}
			return incompleto;
		}
		return false;
	}

	/**
	 * Redirecciona a Editar el Backlog cuando se encuentre uno repetido
	 * @return String - URL Editar
	 */
	public String editarBLRepeat() {
		if (backlogRepetido.getBacklogsMinerosKey() != null) {
			seleccionBean.setBacklogsMinerosSeleccionado(backlogRepetido);
			return "editarbacklog";
		}
		return "";
	}
	
	/**
	 * Agrega un Backlog
	 * 
	 * @param event
	 */
	public void agregarBacklogMinero(ActionEvent event) {
		BacklogsMineros backlog;
		
		if (verificarCamposBacklog() == false && verificarPartesRequeridas() == false) { 
			try {
				this.getFlash().setKeepMessages(true);
				backlog = asignarCamposBacklogMinero();

				// Verificar que el backlog que se registrará no este repetido
				backlogRepetido = backlogsMinerosFacade.obtenerBacklogRepetido(backlog);
				
				if (backlogRepetido.getBacklogsMinerosKey() != null) {
					
					String estadoBL = backlogRepetido.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero();
					
					if (estadoBL.equals("CA") || estadoBL.equals("D")) {
						guardarBL(backlog);
					}else {
						// si el backlog esta repetido
						agregarMensajeWarn("El Backlog que se quiere registrar se encuentra repetido o en proceso.");
						PrimeFaces.current().executeScript("PF('dialogBLRepetido').show();");
						PrimeFaces.current().ajax().update("formBlRepetidos");
					}
					
				} else {
					guardarBL(backlog);
				}

			} catch (Exception e) {
				error = "No se pudo guardar el Backlog Minero";
				log.error(error, e);
				agregarMensajeError(summary, error);
				setSeGuarda(false);
			}
		} else {
			// se hace un verificado por si la primera funcion fue true
			verificarPartesRequeridas();
		}
	}
	
	/**
	 * Auxiliar de la funcion agregarBacklogMinero
	 * @param backlog
	 * @throws Exception
	 */
	public void guardarBL(BacklogsMineros backlog) throws Exception{
		
		int dato = backlogsMinerosFacade.guardarBacklogMinero(backlog);
		BacklogsMinerosKey bKey = backlog.getBacklogsMinerosKey();
		bKey.setIdBacklogMinero(dato);
		backlog.setBacklogsMinerosKey(bKey);

		this.agregarMensajeInfo(summary,
				"El Backlog Minero " + backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado()
						+ " se ha agregado correctamente.");

		asignarBitacoraEstatusBacklogMinero(backlog);

		if (!riesgosTabajosList.isEmpty()) {
			asignarRiesgosDeBacklogs(backlog);
		}

		if (!backlogsMinerosDetalleRefaList.isEmpty()) {
			asignarDetalleRefaBacklogMinero(backlog);
		}

		if (!listaImagenesDto.isEmpty()) {
			asignarImagenesBacklogMinero(backlog);
		}
		int idAlmacen = (int) obtenerSucursalFiltro();

		agregarRevisionKardex(dato, idAlmacen);

		setSeGuarda(true);

		prepararSiguienteBacklog();

		if (!muestrasSeleccionadas.isEmpty()) {
			muestraBacklog();
		} else {
			folioMuestra = null;
		}
	}

	/***
	 * limpia los campos necesarios para registrar un segundo backlog
	 */
	public void prepararSiguienteBacklog() {
		
		borrarImagenes(false);
		
		while (backlogsMinerosDetalleRefaList.size() > 0) {
			borrarFilaPartesRequeridas();
		}
		
		// maquinaria = null;
		origenesBacklogsMineros = null;
		prioridadesBacklogsMineros = null;
		horometro = null;
		lugaresOrigenBacklogsMineros = null;
		codigosSistemas = null;
		ladoComponente = null;
		habilitarCodigoTrabajo();
		accion = "";
		horasMaquina = null;
		horasHombre = null;
		cargosTrabajo = null;
		notasSobreBL = "";
		requeridoPor = "";
		tipoTrabajo = "";
		otrosMaterialesEquipo = "";
		accionEquipo = "";
		// habilitaNumSerie = true;
		totalRefacciones = 0.0;
		idLadoCompontente = null;
		idCodigoSintoma = null;
		idCodigoRiesgoList = null;
		// numEconomico = "";
	}
	
	public void limpiarCampos() {
		
		borrarImagenes(false);
		
		while (backlogsMinerosDetalleRefaList.size() > 0) {
			borrarFilaPartesRequeridas();
		}
		
		origenesBacklogsMineros = null;
		prioridadesBacklogsMineros = null;
		horometro = null;
		lugaresOrigenBacklogsMineros = null;
		codigosSistemas = null;
		ladoComponente = null;
		habilitarCodigoTrabajo();
		accion = "";
		horasMaquina = null;
		horasHombre = null;
		cargosTrabajo = null;
		notasSobreBL = "";
		requeridoPor = "";
		tipoTrabajo = "";
		otrosMaterialesEquipo = "";
		accionEquipo = "";
		// habilitaNumSerie = true;
		totalRefacciones = 0.0;
		idLadoCompontente = null;
		idCodigoSintoma = null;
		idCodigoRiesgoList = null;
		// numEconomico = "";
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
					String rutaImagenTemporal = imagenBlDto.getRutaImagen();
					String nombreArchivo = rutaBacklog + "-" + formato.format(contadorNombre)
							+ tipoArchivo.get(contadorNombre - 1);// -1 porque empieza desde 1.
					imagenesBacklogsMineros.setEstatusImagen(true);
					BacklogsMineros idBacklogMinero = backlog;
					ImagenesBacklogsMinerosKey imagenesBacklogsMinerosKey = new ImagenesBacklogsMinerosKey(
							rutaFinal + nombreArchivo, idBacklogMinero);
					imagenesBacklogsMineros.setImagenesBacklogsMinerosKey(imagenesBacklogsMinerosKey);
					backlogsMinerosFacade.guardarImagenesBacklogsMineros(imagenesBacklogsMineros);
					InputStream imagen = new BufferedInputStream(new FileInputStream(rutaImagenTemporal));
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
			error = "No se pudieron guardar las imagenes del backlog minero "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSeGuarda(false);
		}

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
			for (String idRiesgo : idCodigoRiesgoList) {
				riesgo = new RiesgosTrabajo(idRiesgo, "", idBacklog, (int) this.obtenerSucursalFiltro());
				sintomasYRiesgosFacade.guardarRiesgosDeBacklog(riesgo);
			}
		} catch (Exception e) {
			error = "No se pudo guardar los riesgos del backlog";
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSeGuarda(false);
		}
	}

	public void asignarDetalleRefaBacklogMinero(BacklogsMineros backlog) {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
		try {

			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				BacklogsMinerosKey idBacklogMinero = backlog.getBacklogsMinerosKey();
				
				BacklogsMinerosDetalleRefaKey backlogsMinerosDetalleRefaKey = new BacklogsMinerosDetalleRefaKey(
						idBacklogMinero);
				
				backlogsMinerosDetalleRefa.setBacklogsMinerosDetalleRefaKey(backlogsMinerosDetalleRefaKey);
				Marca idMarca = backlog.getIdMarca();
				backlogsMinerosDetalleRefa.setIdMarca(idMarca);
				backlogsMinerosDetalleRefa.setPrecio(backlogsMinerosDetalleRefa.getPrecio()); // Modificado
				backlogsMinerosDetalleRefa.setTotal(backlogsMinerosDetalleRefa.getTotal()); // Modificado
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
		} catch (Exception e) {
			error = "No se pudo guardar el Detalle Refacciones del "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSeGuarda(false);
		}
	}

	public void asignarBitacoraEstatusBacklogMinero(BacklogsMineros backlog) {
		BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
				RUTA_PROPERTIES_AMCE3);
		try {
			BacklogsMinerosBitacoraEstatus backlogsMinerosBitacoraEstatus = new BacklogsMinerosBitacoraEstatus();
			BacklogsMinerosBitacoraEstatusKey backlogsMinerosBitacoraEstatusKey = new BacklogsMinerosBitacoraEstatusKey(
					backlog, new Date());
			backlogsMinerosBitacoraEstatus.setBacklogsMinerosBitacoraEstatusKey(backlogsMinerosBitacoraEstatusKey);
			String idEstatusBacklogMinero = backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero();
			backlogsMinerosBitacoraEstatus.setIdEstatusBacklogMinero(idEstatusBacklogMinero);
			backlogsMinerosBitacoraEstatus.setUsuarioEstatusInicio(usuario);

			backlogsMinerosBitacoraEstatusFacade.guardarBacklogMineroBitacoraEstatus(backlogsMinerosBitacoraEstatus);
		} catch (Exception e) {
			error = "No se pudo guardar la Bitacora Estatus del "
					+ backlog.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado();
			log.error(error, e);
			agregarMensajeError(summary, error);
			setSeGuarda(false);
		}
	}

	// Si todo funciona, regresa a la pantalla de lista de backlogs.
	public String redireccionarPagina() {
		boolean seGuarda = isSeGuarda();
		String accion = (seGuarda == true) ? "backlogsLista" : "";
		return accion;
	}

	public List<MaquinariaDto> listarMaquinariaDtoBacklogsMineros() {
		Short idSucursal;
		if (backlogMineroSeleccionado != null) {
			idSucursal = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
		} else {
			idSucursal = obtenerSucursalFiltro();
		}
		BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
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
			error = "No se pudo listar la Maquinaria";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		setMaquinariaDtoList(maquinariaDtoList);
		setNumeroEconomicoList(createNumEconomicoList(maquinariaDtoList));
		return maquinariaDtoList;
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

	public List<BacklogsMinerosDetalleRefa> obtenerPartes(BacklogsMinerosKey backlogMineroKey) {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
		try {

			backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaFacade
					.obtenerBacklogMineroDetalleRefaPorId(backlogMineroKey);
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
		// filasRefacciones = backlogsMinerosDetalleRefaList.size();
		return backlogsMinerosDetalleRefaList;
	}

	/***
	 * asigna las rutas con el servlet de las imagenes para mostrarlas en la
	 * galleria
	 * 
	 * @throws Exception
	 */
	public void listarImagenes() throws Exception {

//		List<String> listaRutas = new ArrayList<>();
		ImagenesBacklogsMinerosFacade imagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(
				RUTA_PROPERTIES_AMCE3);

		List<ImagenesBacklogsMineros> listaImagenes = imagenesBacklogsMinerosFacade
				.obtenerImagenesBacklogsMinerosPorID(backlogMineroSeleccionado.getBacklogsMinerosKey());
		for (ImagenesBacklogsMineros imagen : listaImagenes) {
			ImagenBlDto img = new ImagenBlDto();

			String ruta = imagen.getImagenesBacklogsMinerosKey().getImagenBacklog();
			String rutaCompleta = "http://" + getServerUrlServices()
					+ getExternalContext().getInitParameter("servletImagenes") + ruta.replaceAll("/", "%2F");

			img.setRutaImagen(rutaCompleta);
			img.setExiste(true);
			img.setRutaImagenReal(imagen.getImagenesBacklogsMinerosKey().getImagenBacklog());
			String name = FilenameUtils.getName(img.getRutaImagenReal());
			img.setNombre(name);
			// img.setStreamedContent(streamedContent);

			// listaRutas.add(rutaCompleta);
			listaImagenesDto.add(img);
		}

		// this.listaImagenes = listaRutas;

	}

	/**
	 * Crea una lista de String con Numero Economico para evitar nulls
	 * 
	 * @param maquinariaDtoList
	 * @return List<String>
	 */
	public List<String> createNumEconomicoList(List<MaquinariaDto> maquinariaDtoList) {
		List<String> tmp = new ArrayList<String>();
		for (MaquinariaDto maquina : maquinariaDtoList) {
			if (!maquina.getNumeroEconomicoCliente().equals("")) {
				tmp.add(maquina.getNumeroEconomicoCliente());
			}
		}
		return tmp;
	}

	/**
	 * Genera subtotal de refaccionesList
	 */
	public void generarSubtotalRefacciones(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		for (BacklogsMinerosDetalleRefa refaccion : backlogsMinerosDetalleRefaList) {
			refaccion.setSubTotal(refaccion.getCantidad() * refaccion.getPrecio());
		}
	}

	/**
	 * Se ejecuta este metodo cuando se edita una celda de la tabla refacciones Aqui
	 * se calcula el subtotal de la celda y el total de toda la tabla
	 * 
	 * Verifica el numero parte matco de la refacción
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
			
			if (seleccionBean.getIdClienteMatco() == null) {
				buscarCliente(maquinaria);
			}

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
	 * Obtiene el subtotal de todas las refacciones y las guarda en una variable
	 * llamada total
	 * 
	 * @return
	 */
	public Double calcularTotalRefacciones() {
		double total = 0;
		for (BacklogsMinerosDetalleRefa refaccionList : backlogsMinerosDetalleRefaList) {
			if (refaccionList != null) {
				total += refaccionList.getSubTotal();
			}
		}
		return total;
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
	
	/***
	 * Obtiene el color de la celda Estatus BL
	 * 
	 * @return el estilo para que la celda tenga el color de background
	 *         correspondiente
	 */
	public String obtenerColorCelda() {
		if (backlogRepetido == null || backlogRepetido.getBacklogsMinerosKey() == null) {
			return "";
		}
		
		String estatusBacklog = backlogRepetido.getIdEstatusBacklogsMineros().getIdEstatusBacklogMineroNoCode();
		
		if(estatusBacklog == null) {
			return "";
		}
		
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
	 * 
	 */
	public void limpiarBLRepetido() {
		backlogRepetido = null;
	}
	
	/** GETTERS Y SETTERS **/
	public Double getTotalRefacciones() {
		return totalRefacciones;
	}

	public void setTotalRefacciones(Double totalRefacciones) {
		this.totalRefacciones = totalRefacciones;
	}

	public List<String> getNumeroEconomicoList() {
		return numeroEconomicoList;
	}

	public void setNumeroEconomicoList(List<String> numeroEconomicoList) {
		this.numeroEconomicoList = numeroEconomicoList;
	}

	public String getNumEconomico() {
		return numEconomico;
	}

	public void setNumEconomico(String numEconomico) {
		this.numEconomico = numEconomico;
	}

	public boolean isHabilitaNumSerie() {
		return habilitaNumSerie;
	}

	public void setHabilitaNumSerie(boolean habilitaNumSerie) {
		this.habilitaNumSerie = habilitaNumSerie;
	}

	public Integer getHorometro() {
		return horometro;
	}

	public void setHorometro(Integer horometro) {
		this.horometro = horometro;
	}

	public List<OrigenesBacklogsMineros> getOrigenesBacklogsMinerosList() {
		return origenesBacklogsMinerosList;
	}

	public void setOrigenesBacklogsMinerosList(List<OrigenesBacklogsMineros> origenesBacklogsMinerosList) {
		this.origenesBacklogsMinerosList = origenesBacklogsMinerosList;
	}

	public List<LugaresOrigenBacklogsMineros> getLugaresOrigenBacklogsMinerosList() {
		return lugaresOrigenBacklogsMinerosList;
	}

	public void setLugaresOrigenBacklogsMinerosList(
			List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList) {
		this.lugaresOrigenBacklogsMinerosList = lugaresOrigenBacklogsMinerosList;
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

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public String getAccionEquipo() {
		return accionEquipo;
	}

	public void setAccionEquipo(String accionEquipo) {
		this.accionEquipo = accionEquipo;
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

	public List<CargosTrabajo> getCargosTrabajosList() {
		return cargosTrabajosList;
	}

	public void setCargosTrabajosList(List<CargosTrabajo> cargosTrabajosList) {
		this.cargosTrabajosList = cargosTrabajosList;
	}

	public List<BacklogsMinerosDetalleRefa> getBacklogsMinerosDetalleRefaList() {
		return backlogsMinerosDetalleRefaList;
	}

	public void setBacklogsMinerosDetalleRefaList(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		this.backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaList;
	}

	public String getNotasSobreBL() {
		return notasSobreBL;
	}

	public void setNotasSobreBL(String notasSobreBL) {
		this.notasSobreBL = notasSobreBL;
	}

	public String getRequeridoPor() {
		return requeridoPor;
	}

	public void setRequeridoPor(String requeridoPor) {
		this.requeridoPor = requeridoPor;
	}

	public String getOtrosMaterialesEquipo() {
		return otrosMaterialesEquipo;
	}

	public void setOtrosMaterialesEquipo(String otrosMaterialesEquipo) {
		this.otrosMaterialesEquipo = otrosMaterialesEquipo;
	}

	public List<MaquinariaDto> getMaquinariaDtoList() {
		return maquinariaDtoList;
	}

	public void setMaquinariaDtoList(List<MaquinariaDto> maquinariaDtoList) {
		this.maquinariaDtoList = maquinariaDtoList;
	}

	public Integer getCodigoSMCS() {
		return codigoTrabajo;
	}

	public void setCodigoSMCS(Integer codigoSMCS) {
		this.codigoTrabajo = codigoSMCS;
	}

	public Short getCargosTrabajo() {
		return cargosTrabajo;
	}

	public void setCargosTrabajo(Short cargosTrabajo) {
		this.cargosTrabajo = cargosTrabajo;
	}

	public void setCodigosSistemas(Integer codigosSistemas) {
		this.codigosSistemas = codigosSistemas;
	}

	public Integer getCodigosSistemas() {
		return codigosSistemas;
	}

	public Short getIDCodigoSMCSDescripcion() {
		return idCodigoTrabajoSeleccionado;
	}

	public void setIDCodigoSMCSDescripcion(Short descripcionCodigosSMCS) {
		this.idCodigoTrabajoSeleccionado = descripcionCodigosSMCS;
	}

	public Short getPrioridadesBacklogsMineros() {
		return prioridadesBacklogsMineros;
	}

	public void setPrioridadesBacklogsMineros(Short prioridadesBacklogsMineros) {
		this.prioridadesBacklogsMineros = prioridadesBacklogsMineros;
	}

	public Short getLugaresOrigenBacklogsMineros() {
		return lugaresOrigenBacklogsMineros;
	}

	public void setLugaresOrigenBacklogsMineros(Short lugaresOrigenBacklogsMineros) {
		this.lugaresOrigenBacklogsMineros = lugaresOrigenBacklogsMineros;
	}

	public Short getOrigenesBacklogsMineros() {
		return origenesBacklogsMineros;
	}

	public void setOrigenesBacklogsMineros(Short origenesBacklogsMineros) {
		this.origenesBacklogsMineros = origenesBacklogsMineros;
	}

	public boolean isSeGuarda() {
		return seGuarda;
	}

	public void setSeGuarda(boolean seGuarda) {
		this.seGuarda = seGuarda;
	}

	public String getMaquinaria() {
		return maquinaria;
	}

	public void setMaquinaria(String maquinaria) {
		this.maquinaria = maquinaria;
	}

	public BacklogsMinerosDetalleRefa getBacklogsMinerosDetalle() {
		return backlogsMinerosDetalle;
	}

	public void setBacklogsMinerosDetalle(BacklogsMinerosDetalleRefa backlogsMinerosDetalle) {
		this.backlogsMinerosDetalle = backlogsMinerosDetalle;
	}

	public boolean isHayImagenes() {
		return hayImagenes;
	}

	public void setHayImagenes(boolean hayImagenes) {
		this.hayImagenes = hayImagenes;
	}

	public String getRutaArchivosTemporal() {
		return rutaArchivosTemporal;
	}

	public List<String> getTipoArchivo() {
		return tipoArchivo;
	}

	public void setTipoArchivo(List<String> tipoArchivo) {
		this.tipoArchivo = tipoArchivo;
	}

	public String getCargarImagenes() {
		return cargarImagenes;
	}

	public void setCargarImagenes(String cargarImagenes) {
		this.cargarImagenes = cargarImagenes;
	}

	public String getTrabajoRealizar() {
		return trabajoRealizar;
	}

	public void setTrabajoRealizar(String trabajoRealizar) {
		this.trabajoRealizar = trabajoRealizar;
	}

	public LadoComponente getLadoComponente() {
		return ladoComponente;
	}

	public void setLadoComponente(LadoComponente ladoComponente) {
		this.ladoComponente = ladoComponente;
	}

	public String getCodigoTrabajoDescripcion() {
		return codigoTrabajoDescripcion;
	}

	public void setCodigoTrabajoDescripcion(String codigoTrabajoDescripcion) {
		this.codigoTrabajoDescripcion = codigoTrabajoDescripcion;
	}

	public String getTipoTrabajo() {
		return tipoTrabajo;
	}

	public void setTipoTrabajo(String tipoTrabajo) {
		this.tipoTrabajo = tipoTrabajo;
	}

	public String[] getTIPOS_DE_TRABAJO() {
		return TIPOS_DE_TRABAJO;
	}

	public List<BacklogsMinerosDetalleRefa> getPartesSeleccionadas() {
		return partesSeleccionadas;
	}

	public void setPartesSeleccionadas(List<BacklogsMinerosDetalleRefa> partesSeleccionadas) {
		this.partesSeleccionadas = partesSeleccionadas;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public boolean isHabilitaCodigoTrabajo() {
		return habilitaCodigoTrabajo;
	}

	public void setHabilitaCodigoTrabajo(boolean habilitaCodigoTrabajo) {
		this.habilitaCodigoTrabajo = habilitaCodigoTrabajo;
	}

	public List<ImagenesBacklogsMineros> getImagenesBacklog() {
		return imagenesBacklog;
	}

	public void setImagenesBacklog(List<ImagenesBacklogsMineros> imagenesBacklog) {
		this.imagenesBacklog = imagenesBacklog;
	}

	public List<String> getListaImagenes() {
		return listaImagenes;
	}

	public void setListaImagenes(List<String> listaImagenes) {
		this.listaImagenes = listaImagenes;
	}

	public String getFolioMuestra() {
		return folioMuestra;
	}

	public void setFolioMuestra(String folioMuestra) {
		this.folioMuestra = folioMuestra;
	}

	public boolean obtenerMostrarFolio() {
		return folioMuestra != null ? false : true;
	}

	public List<ImagenBlDto> getListaImagenesDto() {
		return listaImagenesDto;
	}

	public void setListaImagenesDto(List<ImagenBlDto> listaImagenesDto) {
		this.listaImagenesDto = listaImagenesDto;
	}

	public List<LadoComponente> getLadoComponenteList() {
		return ladoComponenteList;
	}

	public void setLadoComponenteList(List<LadoComponente> ladoComponenteList) {
		this.ladoComponenteList = ladoComponenteList;
	}

	public List<ImagenBlDto> getImagenesSeleccionadasList() {
		return imagenesSeleccionadasList;
	}

	public void setImagenesSeleccionadasList(List<ImagenBlDto> imagenesSeleccionadasList) {
		this.imagenesSeleccionadasList = imagenesSeleccionadasList;
	}

	public boolean isConservarImagenes() {
		return conservarImagenes;
	}

	public void setConservarImagenes(boolean conservarImagenes) {
		this.conservarImagenes = conservarImagenes;
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

	public String getIdCodigoSintoma() {
		return idCodigoSintoma;
	}

	public void setIdCodigoSintoma(String idCodigoSintoma) {
		this.idCodigoSintoma = idCodigoSintoma;
	}

	public String[] getIdCodigoRiesgoList() {
		return idCodigoRiesgoList;
	}

	public void setIdCodigoRiesgoList(String[] idCodigoRiesgoList) {
		this.idCodigoRiesgoList = idCodigoRiesgoList;
	}

	public Integer getCountRiesgos() {
		Integer countRiesgos = idCodigoRiesgoList.length;
		return countRiesgos;
	}

	public String getIdLadoCompontente() {
		return idLadoCompontente;
	}

	public void setIdLadoCompontente(String idLadoCompontente) {
		this.idLadoCompontente = idLadoCompontente;
	}

	public boolean isLadoComponenteAnterior() {
		return ladoComponenteAnterior;
	}

	public void setLadoComponenteAnterior(boolean ladoComponenteAnterior) {
		this.ladoComponenteAnterior = ladoComponenteAnterior;
	}

	public String getLadoComponenteAnteriorString() {
		return ladoComponenteAnteriorString;
	}

	public void setLadoComponenteAnteriorString(String ladoComponenteAnteriorString) {
		this.ladoComponenteAnteriorString = ladoComponenteAnteriorString;
	}

	public String getRespuestaRevision() {
		return respuestaRevision;
	}

	public void setRespuestaRevision(String respuestaRevision) {
		this.respuestaRevision = respuestaRevision;
	}

	public RevisionKardexEntity getRevisionSeleccionada() {
		return revisionSeleccionada;
	}

	public void setRevisionSeleccionada(RevisionKardexEntity revisionSeleccionada) {
		this.revisionSeleccionada = revisionSeleccionada;
	}

	public BacklogsMineros getBacklogRepetido() {
		return backlogRepetido;
	}

	public void setBacklogRepetido(BacklogsMineros backlogRepetido) {
		this.backlogRepetido = backlogRepetido;
	}
	
}
