package com.matco.backlogs.bean;

import java.io.IOException;
import java.io.InputStream;
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
import javax.faces.context.FacesContext;
import javax.faces.event.ActionEvent;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.RowEditEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.UploadedFile;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.BacklogsMinerosEstandarFacade;
import com.matco.amce3.facade.CargosTrabajoFacade;
import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.LugaresOrigenBacklogsMinerosFacade;
import com.matco.amce3.facade.RefaccionesBacklogEstandarFacade;
import com.matco.amce3.facade.RiesgosEstandarFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.entity.BacklogMineroEstandar;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.LugaresOrigenBacklogsMineros;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;

/**
 * Crea y administra un backlog estándar
 * 
 * @author N Soluciones de Software
 *
 */
@ManagedBean(name = "backlogEstandarBean")
@ViewScoped
public class BacklogEstandarBean extends GenericBacklogBean implements Serializable {

	private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	private static final long serialVersionUID = -7643890782852616966L;
	private static final Logger log = Logger.getLogger(BacklogEstandarBean.class);
	private boolean habilitaCodigoTrabajo = false;
	private boolean selectedBacklog = false;
	private boolean editarBl = false;
	private Double totalRefacciones;
	private String usuario;
	private String error;
	private String btnGuardarBL = "Guardar BL Estándar";
	private String TIPOS_DE_TRABAJO[] = { "MANO DE OBRA", "SOLDADURA", "PROGRAMADO", "VERIFICAR", "DIAGNOSTICO VIMS",
			"SOS", "OPERACIÓN" };

	private BacklogMineroEstandar backlogEstandar;
	private BacklogMineroEstandar backlogEstandarSeleccionado;
	private BacklogMineroEstandar backlogEstandarRepetido;
	private BacklogsStaticsVarBean seleccionBean;
	private LoginBean loginBean = this.obtenerBean("loginBean");
	private List<OrigenesBacklogsMineros> origenesBacklogsMinerosList = new ArrayList<>();
	private List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList = new ArrayList<>();
	private List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList = new ArrayList<>();
	private List<CodigosSistemas> codigosSistemasList = new ArrayList<>();
	private List<CargosTrabajo> cargosTrabajosList = new ArrayList<>();
	private List<BacklogMineroEstandar> backglosEstandarList = new ArrayList<BacklogMineroEstandar>();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();
	private List<Sintomas> sintomasList = new ArrayList<>();
	private List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<>();

	@PostConstruct
	public void init() {
		backlogEstandar = new BacklogMineroEstandar();
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		if (seleccionBean != null) {
			// Si hay un backlog seleccionado para editar
			if (seleccionBean.getBacklogEstandarSelected() != null) {
				backlogEstandar = seleccionBean.getBacklogEstandarSelected();
				backlogEstandar.cargarVariables();
				backlogEstandar.setListaRefacciones(obtenerPartes(backlogEstandar));
				btnGuardarBL = "Editar BL Estándar";
				
				if(backlogEstandar.getCargoTrabajo().getIdCargoTrabajo() != null) {
					habilitaCodigoTrabajo = true;
				}
				
				editarBl = true;
			}
		}
		
		backglosEstandarList = listarBacklogsEstandar();
		origenesBacklogsMinerosList = listarOrigenesBacklogsMineros();
		prioridadesBacklogsMinerosList = listarPrioridadesBacklogsMineros();
		lugaresOrigenBacklogsMinerosList = listarLugaresOrigenesBacklogsMineros();
		codigosSistemasList = listarSistemasBacklogsMineros();
		cargosTrabajosList = listarCargosTrabajoBacklogsMineros();
		ladoComponenteList = listarLadoComponentes();
		sintomasList = listarSintomas();
		riesgosTabajosList = listarRiesgosTrabajos();
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		seleccionBean.setRedireccionDialogBLEST("../backlogs/registroBacklogs.xhtml");
		
	}

	public boolean verificarCampos() {
		boolean errorGeneral = false;

		if (backlogEstandar.getDescripcion() == null || backlogEstandar.getDescripcion().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Descripción' está vacio y se necesita para continuar ");
			errorGeneral = true;
		}

		if (backlogEstandar.getOrigenesBacklogsMineros() == null) {
			agregarMensajeWarn(summary, "El campo 'Origen del BL' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getIdProriedad() == null) {
			agregarMensajeWarn(summary, "El campo 'Prioridad' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getCodigosSistemas() == null) {
			agregarMensajeWarn(summary, "El campo 'Sistema' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getCodigoTrabajo() == null) {
			agregarMensajeWarn(summary, "El campo 'Código de trabajo' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getHorasMaquinaEstimadas() == null) {
			agregarMensajeWarn(summary, "El campo 'Horas Máquina' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getHorasHombreEstimadas() == null) {
			agregarMensajeWarn(summary, "El campo 'Horas Hombre' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getIdCargosTrabajo() == null) {
			agregarMensajeWarn(summary, "El campo 'Cargo' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}

		if (backlogEstandar.getTipoTrabajo() == null) {
			agregarMensajeWarn(summary, "El campo 'Tipo de trabajo' está vacio y se requiere para continuar.");
			errorGeneral = true;
		}
		return errorGeneral;
	}

	/**
	 * Verifica los campos de la tabla Partes Requeridas antes de ser registrado
	 * 
	 * @return boolean
	 */
	private boolean verificarPartesRequeridas() {
		boolean incompleto = false;
		List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = backlogEstandar.getListaRefacciones();

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

				boolean existe = analizarNumeroPMatco(refaccion.getNumeroParte());
				if (existe == false) {
					agregarMensajeWarn(summary, "No se encontró el número parte MATCO: " + refaccion.getNumeroParte());
					incompleto = true;
				}

			}
			return incompleto;
		}
		return false;
	}

	/**
	 * Redirecciona a Editar el Backlog cuando se encuentre uno repetido
	 * 
	 * @return String - URL Editar
	 */
	public String editarBLRepeatEstandar() {
		if (backlogEstandarRepetido.getFechaHoraCreacion() != null) {
			seleccionBean.setBacklogEstandarSelected(backlogEstandarRepetido);
			return "backlogEstandarRegistro";
		}
		return "";
	}

	/**
	 * Agrega el backlog estándar
	 */
	public String agregarBacklogEstandar() {
		BacklogsMinerosEstandarFacade backlogsEstandarFacade = new BacklogsMinerosEstandarFacade(RUTA_PROPERTIES_AMCE3);

		if (verificarCampos() == false && verificarPartesRequeridas() == false) {
			try {
				this.getFlash().setKeepMessages(true);
				backlogEstandar.setCreadoPor(usuario);
				backlogEstandar.setDescripcion(backlogEstandar.getDescripcion().toUpperCase());

				if (editarBl == false) {
					// Verificar que el backlog que se registrará no este repetido
					backlogEstandarRepetido = backlogsEstandarFacade.obtenerBacklogEstandarRepeat(backlogEstandar);

					if (backlogEstandarRepetido.getFechaHoraCreacion() == null) {
						int idBacklog = backlogsEstandarFacade.guardarBacklogEstandar(backlogEstandar);
						backlogEstandar.setKeyBacklog(idBacklog);
						// riesgosEstandarFacade.guardarRiesgosDeBacklogEstandar(riesgo);

						registrarRiesgosDeBacklogs(idBacklog);

						registrarRefaccionesEstandar(backlogEstandar);

						agregarMensajeInfo(summary,
								"El Backlog Minero Estándar número #" + idBacklog + " se ha agregado correctamente");

						return "backlogEstandar";
					} else {
						PrimeFaces.current().executeScript("PF('dialogBLRepetidoEstandar').show();");
						agregarMensajeWarn("El Backlog que se quiere registrar se encuentra repetido o en proceso.");
					}
				} else {
					BacklogsMinerosDetalleRefaFacade refacionesFacade = new BacklogsMinerosDetalleRefaFacade(
							RUTA_PROPERTIES_AMCE3);
					SintomasYRiesgosFacade riesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);

					int idBacklogEstandar = backlogEstandar.getKeyBacklog();

					RiesgosTrabajo delRiesgo = new RiesgosTrabajo();
					delRiesgo.setIdBacklog(idBacklogEstandar);

					// se eliminan para volver a agregarse
					riesgosFacade.eliminarRiesgosDeBacklogEstandar(delRiesgo);
					refacionesFacade.eliminarBacklogMinerosDetalleRefaEstandar(idBacklogEstandar);
					// se registran de nuevo para evitar editarlos
					registrarRiesgosDeBacklogs(idBacklogEstandar);
					registrarRefaccionesEstandar(backlogEstandar);

					backlogsEstandarFacade.editarBacklogEstandar(backlogEstandar);

					agregarMensajeInfo(summary, "El Backlog Minero Estándar número #" + idBacklogEstandar
							+ " se ha editado correctamente.");

					return "backlogEstandar";
				}

			} catch (Exception e) {
				error = "No se pudo guardar el Backlog Minero Estándar";
				log.error(error, e);
				agregarMensajeError(summary, error);
			}
		}
		return "";
	}

	/**
	 * Guarda los riesgos de un backlog
	 * 
	 * @param backlog
	 */
	public void registrarRiesgosDeBacklogs(int idBacklog) {
		RiesgosTrabajo riesgo;
		RiesgosEstandarFacade riesgosEstandarFacade = new RiesgosEstandarFacade(RUTA_PROPERTIES_AMCE3);
		try {
			for (String idRiesgo : backlogEstandar.getIdCodigoRiesgoList()) {
				riesgo = new RiesgosTrabajo();
				riesgo.setIdBacklog(idBacklog);
				riesgo.setIdCodigoRiesgo(idRiesgo);
				riesgosEstandarFacade.guardarRiesgosDeBacklogEstandar(riesgo);
			}
		} catch (Exception e) {
			error = "No se pudo guardar los riesgos del backlog estándar";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
	}

	/**
	 * Registra las refacciones estandar de un Backlog
	 * 
	 * @param BacklogMineroEstandar - backlogEstandar
	 */
	public void registrarRefaccionesEstandar(BacklogMineroEstandar backlogEstandar) {
		RefaccionesBacklogEstandarFacade refaccionesEstandarFacade = new RefaccionesBacklogEstandarFacade(
				RUTA_PROPERTIES_AMCE3);

		try {
			for (BacklogsMinerosDetalleRefa refacciones : backlogEstandar.getListaRefacciones()) {
				refacciones.setIdBacklogEstandar(backlogEstandar.getKeyBacklog());
				refaccionesEstandarFacade.guardarRefaccionesEstandar(refacciones);
			}
		} catch (Exception e) {
			error = "No se pudieron guardar las refacciones del Backlog Estándar";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
	}

	/**
	 * Obtiene las refacciones del backlog estandar
	 * 
	 * @param BacklogMineroEstandar - backlogEstandar
	 * @return List<BacklogsMinerosDetalleRefa> result
	 */
	public List<BacklogsMinerosDetalleRefa> obtenerPartes(BacklogMineroEstandar backlogEstandar) {
		// BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new
		// BacklogsMinerosDetalleRefaFacade(
		// RUTA_PROPERTIES_AMCE3);
		RefaccionesBacklogEstandarFacade refaccionesFacade = new RefaccionesBacklogEstandarFacade(
				RUTA_PROPERTIES_AMCE3);
		List<BacklogsMinerosDetalleRefa> result = null;
		try {
			int idBacklog = backlogEstandar.getKeyBacklog();
			result = refaccionesFacade.obtenerRefaccionesBacklogEstandar(idBacklog);
			Double subTotal = 0.0;

			for (BacklogsMinerosDetalleRefa refaTemp : result) {
				subTotal = (refaTemp.getCantidad() * refaTemp.getPrecio());
				refaTemp.setSubTotal(subTotal);
			}

			/*
			 * Comparator<BacklogsMinerosDetalleRefa> comp = (BacklogsMinerosDetalleRefa a,
			 * BacklogsMinerosDetalleRefa b) -> { Integer consecutivoA =
			 * a.getBacklogsMinerosDetalleRefaKey().getConsecutivo(); Integer consecutivoB =
			 * b.getBacklogsMinerosDetalleRefaKey().getConsecutivo(); return
			 * consecutivoA.compareTo(consecutivoB); }; Collections.sort(result, comp);
			 */

		} catch (Exception e) {
			error = "No se pudieron listar las refacciones del Backlog Estándar ID: " + backlogEstandar.getKeyBacklog();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		// filasRefacciones = backlogsMinerosDetalleRefaList.size();
		return result;
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
				totalRefacciones = calcularTotalRefacciones(backlogEstandar.getListaRefacciones());
			} else {
				refaccion.setPrecio(0.0);
				refaccion.setSubTotal(0.0);
				totalRefacciones = calcularTotalRefacciones(backlogEstandar.getListaRefacciones());
			}

			// verificar el numero parte matco
			String numParteMatco = refaccion.getNumeroParte();

			boolean existe = analizarNumeroPMatco(numParteMatco);
			refaccion.setExisteNumeroParteMatco(existe);

			if (existe == false) {
				agregarMensajeWarn(summary, "El número parte MATCO agregado no existe!");
			}

		} catch (Exception e) {
			agregarMensajeWarn("No se pudo calcular la refacción");
		}
	}

	/**
	 * Obtiene el subtotal de todas las refacciones y las guarda en una variable
	 * llamada total
	 * 
	 * @return
	 */
	public Double calcularTotalRefacciones(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		double total = 0;
		for (BacklogsMinerosDetalleRefa refaccionList : backlogsMinerosDetalleRefaList) {
			total += refaccionList.getSubTotal();
		}
		return total;
	}

	/***
	 * Al seleccionar un codigo de trabajo, actualiza las variables correspondientes
	 * al código de trabajo, desccripción e id
	 * 
	 * @throws Exception
	 */
	public void cambiarCodigoTrabajo() throws Exception {
		backlogEstandar.getCodigoTrabajoDescripcion().trim();
		String codigoTrabajoFormateado = backlogEstandar.getCodigoTrabajoDescripcion().split("-")[0].trim();
		backlogEstandar.setCodigoTrabajo(Integer.parseInt(codigoTrabajoFormateado));
		// backlogEstandar.setTipoTrabajo(backlogEstandar.getCodigoTrabajoDescripcion().split("-")[1].trim());
		backlogEstandar.setTrabajoRealizar(backlogEstandar.getCodigoTrabajoDescripcion().split("-")[1].trim());
		CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		List<CodigosSMCS> listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		//boolean encontrado = false;
		for (CodigosSMCS codigoTrabajoComparado : listaCodigosTrabajo) {
			int codigo = codigoTrabajoComparado.getCodigoSMCS();
			String descripcion = codigoTrabajoComparado.getDescripcionSMCS();
			if (backlogEstandar.getCodigoTrabajo() == codigo
					&& descripcion.equals(backlogEstandar.getTrabajoRealizar())) {
				backlogEstandar.setIdCodigoTrabajoSeleccionado(codigoTrabajoComparado.getIdCodigoSMCS());
				//encontrado = true;
				habilitaCodigoTrabajo = false;
				break;
			}
		}
		/*
		 * if (encontrado) { asignarHoras(); }
		 */
	}

	public void habilitarCodigoTrabajo(ActionEvent even) {
		System.out.println("habilitando el trabajo");
		backlogEstandar.setCodigoTrabajoDescripcion(null);
		backlogEstandar.setCodigoTrabajo(null);
		backlogEstandar.setTrabajoRealizar(null);
		backlogEstandar.setIdCodigoTrabajoSeleccionado(null);
		habilitaCodigoTrabajo = false;
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

	/***
	 * Actualiza las variables correspondientes a las horas hombre y estimadas
	 * después de seleccionar un código de trabajo
	 */
	public void asignarHoras() {
		CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
		CodigosSMCS codigo;
		try {
			codigo = codigosSMCSFacade
					.obtenerCodigosSMCSPorID(backlogEstandar.getIdCodigoTrabajoSeleccionado().toString());
			Double horasHombre = codigo.getHoraHombreCodigoSMCS();
			backlogEstandar.setHorasHombreEstimadas(horasHombre);
			Double horasMaquina = (horasHombre) / 2;
			backlogEstandar.setHorasMaquinaEstimadas(horasMaquina);
		} catch (Exception e) {
			error = "No se pudo obtener el Código SMCS por ID al asignar las horas";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
	}

	private List<BacklogMineroEstandar> listarBacklogsEstandar() {
		try {
			BacklogsMinerosEstandarFacade blEstandarFacade = new BacklogsMinerosEstandarFacade(RUTA_PROPERTIES_AMCE3);

			RiesgosEstandarFacade riesgosEstandarFacade = new RiesgosEstandarFacade(RUTA_PROPERTIES_AMCE3);

			RiesgosTrabajo riesgoTmp;
			List<RiesgosTrabajo> riesgosResult;
			// String[]

			backglosEstandarList = blEstandarFacade.obtenerTodosBacklogsEstandar();
			Comparator<BacklogMineroEstandar> comp = (BacklogMineroEstandar a, BacklogMineroEstandar b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(backglosEstandarList, comp);
			// listamos los riesgos y las refacciones
			for (BacklogMineroEstandar backlogEstandarTemp : backglosEstandarList) {
				int idBacklog = backlogEstandarTemp.getKeyBacklog();
				riesgoTmp = new RiesgosTrabajo();
				riesgoTmp.setIdBacklog(idBacklog);
				riesgosResult = riesgosEstandarFacade.getRiesgosDeunBacklogEstandar(riesgoTmp);
				backlogEstandarTemp.setRiesgosList(riesgosResult);
				// riesgosResult.clear();

				// listamos las refacciones
				backlogEstandarTemp.setListaRefacciones(obtenerPartes(backlogEstandarTemp));
			}

		} catch (Exception e) {
			error = "No se pudieron listar los Backlogs Estándar";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return backglosEstandarList;
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

	public void subirListado(FileUploadEvent event) throws IOException {
		UploadedFile archivo = event.getFile();
		InputStream inputStream = archivo.getInputstream();

		try {
			XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
			inputStream.close();
			cargarListadoPartesRequeridas(workbook);
			totalRefacciones = calcularTotalRefacciones(backlogEstandar.getListaRefacciones());
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo cargar el excel");
			System.out.println("Error: " + e);
		}
	}

	/**
	 * 
	 * @param workbook
	 */
	public void cargarListadoPartesRequeridas(XSSFWorkbook workbook) {
		workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row;
		XSSFCell cell;

		Integer rows = sheet.getPhysicalNumberOfRows();
		Integer cols = 5;
		// System.out.println("Number of rows: "+rows);
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
							if (cell.getCellType() == CellType.STRING) {
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
								bmdr.setPrecio(cell.getNumericCellValue());
								bmdr.setSubTotal(bmdr.getCantidad() * bmdr.getPrecio());
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
				backlogEstandar.getListaRefacciones().add(bmdr);
			}
		}
	}

	/**
	 * Agrega una nueva fila a la tabla de refacciones
	 */
	public void agregarFila(ActionEvent even) {
		backlogEstandar.getListaRefacciones().add(new BacklogsMinerosDetalleRefa((short) 1, "", "", "", 0.0));
	}

	/**
	 * Borra un elemento de la tabla de refacciones
	 */
	public void borrarFilaPartesRequeridas(ActionEvent even) {
		if (backlogEstandar.getListaRefacciones().size() != 0) {
			int indice = backlogEstandar.getListaRefacciones().size() - 1;
			backlogEstandar.getListaRefacciones().remove(indice);
			totalRefacciones = calcularTotalRefacciones(backlogEstandar.getListaRefacciones());
		}
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

	/**
	 * 
	 */
	public void onRowSelectBLEST(SelectEvent event) {
		selectedBacklog = true;

		// PrimeFaces pf = PrimeFaces.current();
		// pf.executeScript("PF('tablaBacklogsMineros').filter()");
		// pf.ajax().update(":formDialogsBlEstandar:btnUsarBLEST");

		// seleccionBean.setB

	}

	public void usarBacklog() {
		String url = seleccionBean.getRedireccionDialogBLEST();
		backlogEstandarSeleccionado = seleccionBean.getBacklogEstandarSelected();
		try {
			if (backlogEstandarSeleccionado != null) {
				FacesContext.getCurrentInstance().getExternalContext().redirect(url);
			} else {
				String message = "No se ha seleccionado un Backlog Estándar. Por favor seleccione uno.";
				agregarMensaje(summary, message, "2", true);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Double getTotalRefacciones() {
		return totalRefacciones;
	}

	public void setTotalRefacciones(Double totalRefacciones) {
		this.totalRefacciones = totalRefacciones;
	}

	public String[] getTIPOS_DE_TRABAJO() {
		return TIPOS_DE_TRABAJO;
	}

	public void setTIPOS_DE_TRABAJO(String[] tIPOS_DE_TRABAJO) {
		TIPOS_DE_TRABAJO = tIPOS_DE_TRABAJO;
	}

	public boolean isHabilitaCodigoTrabajo() {
		return habilitaCodigoTrabajo;
	}

	public void setHabilitaCodigoTrabajo(boolean habilitaCodigoTrabajo) {
		this.habilitaCodigoTrabajo = habilitaCodigoTrabajo;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public List<OrigenesBacklogsMineros> getOrigenesBacklogsMinerosList() {
		return origenesBacklogsMinerosList;
	}

	public void setOrigenesBacklogsMinerosList(List<OrigenesBacklogsMineros> origenesBacklogsMinerosList) {
		this.origenesBacklogsMinerosList = origenesBacklogsMinerosList;
	}

	public List<CodigosSistemas> getCodigosSistemasList() {
		return codigosSistemasList;
	}

	public void setCodigosSistemasList(List<CodigosSistemas> codigosSistemasList) {
		this.codigosSistemasList = codigosSistemasList;
	}

	public List<CargosTrabajo> getCargosTrabajosList() {
		return cargosTrabajosList;
	}

	public void setCargosTrabajosList(List<CargosTrabajo> cargosTrabajosList) {
		this.cargosTrabajosList = cargosTrabajosList;
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

	public String getSummary() {
		return summary;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public BacklogMineroEstandar getBacklogEstandar() {
		return backlogEstandar;
	}

	public void setBacklogEstandar(BacklogMineroEstandar backlogEstandar) {
		this.backlogEstandar = backlogEstandar;
	}

	public List<BacklogMineroEstandar> getBackglosEstandarList() {
		return backglosEstandarList;
	}

	public void setBackglosEstandarList(List<BacklogMineroEstandar> backglosEstandarList) {
		this.backglosEstandarList = backglosEstandarList;
	}

	public boolean isSelectedBacklog() {
		return selectedBacklog;
	}

	public void setSelectedBacklog(boolean selectedBacklog) {
		this.selectedBacklog = selectedBacklog;
	}

	public BacklogsStaticsVarBean getSeleccionBean() {
		return seleccionBean;
	}

	public void setSeleccionBean(BacklogsStaticsVarBean seleccionBean) {
		this.seleccionBean = seleccionBean;
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

	public BacklogMineroEstandar getBacklogEstandarSeleccionado() {
		return backlogEstandarSeleccionado;
	}

	public void setBacklogEstandarSeleccionado(BacklogMineroEstandar backlogEstandarSeleccionado) {
		this.backlogEstandarSeleccionado = backlogEstandarSeleccionado;
	}

	public BacklogMineroEstandar getBacklogEstandarRepetido() {
		return backlogEstandarRepetido;
	}

	public void setBacklogEstandarRepetido(BacklogMineroEstandar backlogEstandarRepetido) {
		this.backlogEstandarRepetido = backlogEstandarRepetido;
	}

	public boolean isEditarBl() {
		return editarBl;
	}

	public void setEditarBl(boolean editarBl) {
		this.editarBl = editarBl;
	}

	public String getBtnGuardarBL() {
		return btnGuardarBL;
	}

	public void setBtnGuardarBL(String btnGuardarBL) {
		this.btnGuardarBL = btnGuardarBL;
	}

}
