package com.matco.backlogs.bean;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;
import com.matco.amce3.dto.MaquinariaDto;
import com.matco.amce3.facade.BacklogsMinerosBitacoraEstatusFacade;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.amce3.facade.EstatusBacklogsMinerosFacade;
import com.matco.amce3.facade.NumeroParteClientesFacade;
import com.matco.amce3.facade.OrigenesBacklogsMinerosFacade;
import com.matco.amce3.facade.PrioridadesBacklogsMinerosFacade;
import com.matco.amce3.facade.RevisionKardexFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.NumeroParteClientes;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RevisionKardexEntity;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.ejes.entity.Almacen;
import com.matco.ejes.entity.Modelo;
import com.matco.ejes.facade.ModeloFacade;

@ManagedBean(name = "genericBacklogBean")
@ViewScoped
public class GenericBacklogBean extends GenericBean implements Serializable {

	private static final long serialVersionUID = -5997701249116445267L;
	public static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	public static final Logger log = Logger.getLogger(GenericBacklogBean.class);

	public boolean renderTooltip = true;
	public BacklogsStaticsVarBean seleccionBean;
	public LoginBean loginBean = this.obtenerBean("loginBean");
	private RevisionKardexEntity revisionSeleccionada;

	BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
	BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
			RUTA_PROPERTIES_AMCE3);
	BacklogsMinerosDetalleRefaFacade blmRefaFacade = new BacklogsMinerosDetalleRefaFacade(RUTA_PROPERTIES_AMCE3);
	CodigosSMCSFacade codigosSMCSFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);
	CodigosSistemasFacade codigosSistemasFacade = new CodigosSistemasFacade(RUTA_PROPERTIES_AMCE3);
	EstatusBacklogsMinerosFacade estatusBacklogsFacade = new EstatusBacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
	OrigenesBacklogsMinerosFacade origenesBacklogsMinerosFacade = new OrigenesBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);
	PrioridadesBacklogsMinerosFacade prioridadesBacklogsMinerosFacade = new PrioridadesBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);
	ModeloFacade modeloFacade = new ModeloFacade(RUTA_PROPERTIES_MATCO);
	RevisionKardexFacade revisionFacade = new RevisionKardexFacade(RUTA_PROPERTIES_AMCE3);

	@PostConstruct
	public void init() {
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		
		/*
		LoginBean loginBean = obtenerBean("loginBean");
		String rol = loginBean.getRol();
		
		if (rol == null) {
			//redireccionar("login");
			redireccionarVista("../login/login.xhtml");
			return;
		}
		 */
		
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		estatusBacklogsFacade = new EstatusBacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		revisionSeleccionada = new RevisionKardexEntity();
		seleccionBean.setSucursalFiltro(obtenerSucursalCorrespondiente());
		obtenerMaquinaria(seleccionBean.getSucursalFiltro());
		obtenerModelosMenu();
		obtenerEstadosBLM();
	}

	public List<EstatusBacklogsMineros> obtenerEstadosBLM() {
		List<EstatusBacklogsMineros> estados = seleccionBean.getEstatusBacklogsMinerosList();

		if (estados.isEmpty()) {
			estados = listarEstatusBacklogsMineros();
			seleccionBean.setEstatusBacklogsMinerosList(estados);
		}

		return estados;
	}

	public List<MaquinariaDto> obtenerMaquinaria(int sucursal) {
		List<MaquinariaDto> maquinariaDtoList = seleccionBean.getMaquinariaDtoList();
		List<String> numeroEconomicoList = seleccionBean.getNumeroEconomicoList();

		if (maquinariaDtoList.isEmpty()) {
			maquinariaDtoList = listarMaquinariaDtoBacklogsMinerosSucursal(sucursal);
			seleccionBean.setMaquinariaDtoList(maquinariaDtoList);
		}

		if (numeroEconomicoList.isEmpty()) {
			numeroEconomicoList = createNumEconomicoList(maquinariaDtoList);
			seleccionBean.setNumeroEconomicoList(numeroEconomicoList);
		}

		return maquinariaDtoList;
	}

	public List<MaquinariaDto> obtenerMaquinariaNueva(int sucursal) {
		List<MaquinariaDto> maquinariaDtoList = listarMaquinariaDtoBacklogsMinerosSucursal(sucursal);
		List<String> numeroEconomicoList = createNumEconomicoList(maquinariaDtoList);
		seleccionBean.setMaquinariaDtoList(maquinariaDtoList);
		seleccionBean.setNumeroEconomicoList(numeroEconomicoList);

		return maquinariaDtoList;
	}

	public List<Modelo> obtenerModelosMenu() {
		List<Modelo> modeloList = seleccionBean.getModeloList();

		if (modeloList.isEmpty()) {
			modeloList = obtenerModelos();
			seleccionBean.setModeloList(modeloList);
		}

		return modeloList;
	}

	/**
	 * Setea el Id Cliente en donde se agregue un número serie
	 * 
	 * @param idCliente
	 */
	public void buscarCliente(List<MaquinariaDto> maquinaria, String numSerie) {
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		//List<MaquinariaDto> maquinaria = seleccionBean.getMaquinariaDtoList();
		for (MaquinariaDto maquinariaDto : maquinaria) {
			if (maquinariaDto.getSerie().equals(numSerie)) {
				seleccionBean.setIdClienteMatco(maquinariaDto.getIdCliente().getCliente());
				break;
			}
		}
	}

	public String obtenerNombreGrafica(int idGrafica) {
		String name = "";
		switch (idGrafica) {
		case 1:
			name = "BACKLOG GENERADOS / EJECUTADOS / PENDIENTES DE EJECUTAR";
			break;
		case 2:
			name = "ESTATUS DE BACKLOGS";
			break;
		case 3:
			name = "BACKLOGS > 30 DIAS";
			break;
		case 4:
			name = "BACKLOGS PENDIENTES";
			break;
		case 5:
			name = "PORCENTAJE GENERADOS / EJECUTADOS / PENDIENTES DE EJECUTAR";
			break;
		case 6:
			name = "ADMINISTRACIÓN DE BACKLOGS - TIEMPO ESTIMADO DE REPARACIÓN";
			break;
		case 7:
			name = "ADMINISTRACIÓN DE BACKLOGS - LABOR ESTIMADO DE REPARACIÓN";
			break;
		case 8:
			name = "BACKLOGS GENERADOS POR ÁREA";
			break;
		case 9:
			name = "BACKLOGS GENERADOS POR SMCS";
			break;
		}

		return name;
	}

	/**
	 * Buscamos primero el ID del backlog a revisar para pasarlo a la variable
	 * seleccionBean y cambiamos la pantalla a editar
	 */
	public String editarBacklogPorRevision() {
		BacklogsMinerosFacade facade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		BacklogsMineros backlog;
		try {
			int idBacklogMinero = revisionSeleccionada.getIdBacklogMinero();
			int idAlmacen = revisionSeleccionada.getIdAlmacen();
			Almacen almacen = new Almacen();
			almacen.setAlmacen((short) idAlmacen);
			BacklogsMinerosKey backlogKey = new BacklogsMinerosKey(idBacklogMinero, almacen);

			backlog = facade.obtenerBacklogMineroPorId(backlogKey);
			seleccionBean.setBacklogsMinerosSeleccionado(backlog);
			seleccionBean.setRevisionBacklog(true);
			seleccionBean.setRevisionSeleccionada(revisionSeleccionada);
			return "editarbacklog";
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return "";
	}

	/**
	 * Envia la revision
	 * 
	 * @return
	 */
	public String revisarBacklogSupervisor() {
		BacklogsMinerosFacade facade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		BacklogsMineros backlog;
		String returnUrl = "";
		try {
			int idBacklogMinero = revisionSeleccionada.getIdBacklogMinero();
			int idAlmacen = revisionSeleccionada.getIdAlmacen();
			Almacen almacen = new Almacen();
			almacen.setAlmacen((short) idAlmacen);
			BacklogsMinerosKey backlogKey = new BacklogsMinerosKey(idBacklogMinero, almacen);

			backlog = facade.obtenerBacklogMineroPorId(backlogKey);
			seleccionBean.setBacklogsMinerosSeleccionado(backlog);
			seleccionBean.setRevisionBacklog(true);
			seleccionBean.setRevisionSeleccionada(this.revisionSeleccionada);
			switch (backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero()) {
			case "A":
			case "M":
				returnUrl = "estatusCreado";
				break;
			case "B2":
				returnUrl = "estatusB2";
				break;
			}
			return returnUrl;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e);
		}
		return "";
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
			if (maquina.getSerie().equals(serie)) {
				return maquina.getNumeroEconomicoCliente();
			}
		}
		return "No definido";
	}

	/**
	 * Obtiene un modelo por un número de serie dado
	 * 
	 * @param maquinaria
	 * @param serie
	 * @return
	 */
	public String obtenerModeloPorSerie(List<MaquinariaDto> maquinaria, String serie) {
		for (MaquinariaDto maquinas : maquinaria) {
			if (maquinas.getSerie().equals(serie)) {
				return maquinas.getIdModelo().getModeloKey().getModelo();
			}
		}
		return "";
	}

	/**
	 * Obtiene un numero serie dado un modelo
	 * 
	 * @param maquinaria
	 * @param modelo
	 * @return
	 */
	public String obtenerSeriePorModelo(List<MaquinariaDto> maquinaria, String modelo) {
		for (MaquinariaDto maquinas : maquinaria) {
			if (maquinas.getIdModelo().getModeloKey().getModelo().equals(modelo)) {
				return maquinas.getSerie();
			}
		}
		return "";
	}

	/***
	 * comprueba que los campos obligatorios de los numero parte tengan contenido
	 * 
	 * @param backlog backlog al que corresponden las partes
	 * @return true si faltan partes, fale si todo esta bien
	 */
	public boolean verificarPartesRequeridas(BacklogsMineros backlog) {
		List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = backlog.getListaRefacciones();
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
					agregarMensajeWarnKeep(summary,
							"Verifique que todas las partes requeridas contengan los campos requeridos.");
					break;
				}
			}
			return falta;
		}
		return false;
	}

	/***
	 * Al dar doble clic en una fila de la tabla de backlogs ver la pantalla del
	 * estado en que se encuentra
	 * 
	 * @throws IOException
	 */
	public void seleccionarBacklog() throws IOException {
		BacklogsMineros backlog = seleccionBean.getBacklogsMinerosSeleccionado();
		String estatusBacklog = backlog.getIdEstatusBacklogsMineros().getDescripcionEstatusBacklog();
		String url = obtenerURLRedireccionamiento(estatusBacklog);
		if (!url.equals("")) {

			FacesContext.getCurrentInstance().getExternalContext().redirect(url);

		} else {
			agregarMensajeWarn(summary,
					"El Folio " + seleccionBean.getBacklogsMinerosSeleccionado().getBacklogsMinerosKey()
							.getBacklogMineroAlmacenFormateado() + " no tiene un estatus al cual cambiar.");
			seleccionBean.setBacklogsMinerosSeleccionado(null);
		}
	}

	/***
	 * Verifica si debe mostrar el checkbox en la columna de carga rápida de la
	 * lista de backlogs.
	 * 
	 * @param estatusBacklog estatus del backlog
	 * @return "" si si debe aparecer el checkbox, display:none si no debe aparecer.
	 */
	public String mostrarCheckBox(String estatusBacklog) {
		String mostrar;
		boolean b1 = estatusBacklog.equals("AUTORIZAR");
		boolean a = estatusBacklog.equals("CREADO") && tienePermiso("ESTATUS A-B2") && obtenerSucursalFiltro() == 6;
		boolean m = estatusBacklog.equals("MONITOREAR") && tienePermiso("ESTATUS A-M") && obtenerSucursalFiltro() == 6;
		boolean b2 = estatusBacklog.equals("COTIZAR") && tienePermiso("ESTATUS B2-B1");
		boolean b3 = estatusBacklog.equals("NO AUTORIZADO") && tienePermiso("ESTATUS B2-B1");
		boolean estatus = b1 || a || m || b2 || b3;
		if (estatus) {
			mostrar = "";
		} else {
			mostrar = "display:none;";
		}
		return mostrar;
	}

	/**
	 * Verifica el Backlog antes de redireccionar a Editar
	 * 
	 * @return editarbacklog - String
	 */
	public String redireccionarEditar() {
		if (seleccionBean.getBacklogsMinerosSeleccionado() != null) {
			BacklogsMineros backlog = seleccionBean.getBacklogsMinerosSeleccionado();
			String estatusBacklog = backlog.getIdEstatusBacklogsMineros().getDescripcionEstatusBacklog();
			if (estatusBacklog.equals("EJECUTADO") || estatusBacklog.equals("CANCELADO")) {
				agregarMensajeWarnKeep(summary, "El Folio " + seleccionBean.getBacklogsMinerosSeleccionado()
						.getBacklogsMinerosKey().getBacklogMineroAlmacenFormateado() + " no puede editarse.");
				seleccionBean.setBacklogsMinerosSeleccionado(null);
				return "";
			}

			BacklogsMineros backlogMineroSeleccionado = seleccionBean.getBacklogsMinerosSeleccionado();
			RevisionKardexEntity revisionBacklog = obtenerRevisionBacklogActual(backlogMineroSeleccionado);

			if (revisionBacklog.getEstatusRevision() != 1) {
				if (revisionBacklog.getEstatusRevision() != 3) {
					seleccionBean.setRevisionBacklog(false);
					// seleccionBean.setRevisionSeleccionada(revisionBacklog);
				}
			}

			return "editarbacklog";
		} else {
			agregarMensajeWarnKeep(summary, "No hay un backlog seleccionado");
		}
		return "";
	}

	/**
	 * 
	 * @return
	 */
	public RevisionKardexEntity obtenerRevisionBacklogActual(BacklogsMineros backlog) {

		RevisionKardexEntity revision = new RevisionKardexEntity();

		try {
			int idBacklogMinero = backlog.getBacklogsMinerosKey().getIdBacklogMinero();
			int idAlmacen = backlog.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
			revision.setIdBacklogMinero(idBacklogMinero);
			revision.setIdAlmacen(idAlmacen);
			revision = revisionFacade.obtenerRevisionByIDBL(revision);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return revision;
	}

	/***
	 * Obtiene el url al que se direccionará al darle doble clic a un backlog en la
	 * lista de backlogs segun su estado
	 * 
	 * @param estatusBacklog estado actual del backlog seleccionado
	 * @return url correspondiente al estado del backlog
	 */
	public String obtenerURLRedireccionamiento(String estatusBacklog) {
		String returnUrl = "";
		switch (estatusBacklog) {
		case "CREADO":
		case "MONITOREAR":
			returnUrl = "../backlogs/estatusBacklogs.xhtml";
			break;
		case "COTIZAR":
		case "NO AUTORIZADO":
			returnUrl = "../backlogs/estatusB2Backlogs.xhtml";
			break;
		case "AUTORIZAR":
			returnUrl = "../backlogs/estatusB1Backlogs.xhtml";
			break;
		case "EJECUTAR":
			returnUrl = "../backlogs/estatusCBacklogs.xhtml";
			break;
		case "NO EJECUTADO":
			returnUrl = "../backlogs/estatusNoCBacklogs.xhtml";
			break;
		case "EJECUTADO":
			returnUrl = "../backlogs/informacionbacklog.xhtml";
			break;
		case "ESPERA DE REFACCIONES":
			returnUrl = "../backlogs/estatusEsperaDeRefacciones.xhtml";
			break;
		case "POSPUESTO":
			returnUrl = "../backlogs/estatusPospuesto.xhtml";
			break;
		case "CANCELADO":
			returnUrl = "../backlogs/estatusCancelado.xhtml";
			break;
		default:
			returnUrl = "";
		}
		return returnUrl;
	}

	/***
	 * Obtiene el color de fondo y de fuente de la columna de estatus en la lista de
	 * backlogs. El color de fuente es blanco excepto en el estado "cotizar" que es
	 * negro porque el blanco no se ve en el amarillo
	 * 
	 * @param estatusBacklog estatus del backlog
	 * @return style css (color de fondo y fuente)
	 */
	public String colorCelda(String estatusBacklog) {
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

	/***
	 * Obtiene los dias que lleva abierto un backlog que no se haya ejecutado aún
	 * 
	 * @param backlog backlog abierto
	 * @return número de dias que han pasado desde la creación del backlog, si ya se
	 *         ejecutó regresa null
	 * @throws ParseException
	 */
	public Integer obtenerDiasActivo(BacklogsMineros backlog) throws ParseException {
		if (backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero().equals("D")) {
			darFormatoFecha(backlog.getFechaHoraCerrado());
			return null;
		}
		Integer diasActivo;
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		Date fechaHoraCreacion = backlog.getFechaHoraCreacion();
		Date fechaActual = new Date();
		String fechaCreacion = sdf.format(fechaHoraCreacion);
		String fechaHoy = sdf.format(fechaActual);
		fechaHoraCreacion = sdf.parse(fechaCreacion);
		fechaActual = sdf.parse(fechaHoy);
		long diferencia = fechaActual.getTime() - fechaHoraCreacion.getTime();
		diasActivo = (int) TimeUnit.DAYS.convert(diferencia, TimeUnit.MILLISECONDS);
		return diasActivo;
	}

	/***
	 * Da Formato dd-MM-yyyy a una fecha (en este caso se usa en la fecha de
	 * creación que se muestra en la lista de backlogs)
	 * 
	 * @param fecha fecha a formatear
	 * @return fecha con el formato dd-MM-yyyy
	 * @throws ParseException
	 */
	public Date darFormatoFecha(Date fecha) throws ParseException {
		if (fecha == null) {
			return null;
		}
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
		String fechaHora = sdf.format(fecha);
		fecha = sdf.parse(fechaHora);
		return fecha;
	}

	/***
	 * Recibe una opción del submenu del filtro de sucursal y regresa si debe
	 * habilitarse o no
	 * 
	 * @param sucursal sucursal del submenu del filtro de sucursal
	 * @return
	 */
	public boolean deshabilitarOpcionFiltroSucursal(String sucursal) {
		Short sucursalActual = obtenerSucursalCorrespondiente();
		// lo recibe como string porque el attributo itemDisabled solo asi lo permite
		// por lo que hay que pasarlo a short
		Short s = sucursal.equals("todos") ? null : Short.valueOf(sucursal);
		// si sucursalActual es null debe estar habilitado
		// si no sucursalActual debe ser igual al parametro
		// System.out.println("sucursal "+s);
		boolean deshabilitar = !(sucursalActual == null || sucursalActual == s);
		return deshabilitar;
	}

	public boolean filterByDate(Object value, Object filter, Locale locale) {

		String filterText = (filter == null) ? null : filter.toString().trim();
		if (filterText == null || filterText.isEmpty()) {
			return true;
		}
		if (value == null) {
			return false;
		}

		DateFormat df2 = new SimpleDateFormat("yyyy/MMM/dd");

		Calendar c = Calendar.getInstance();
		Date fechaGrid = (Date) value;
		c.setTime(fechaGrid);
		fechaGrid = c.getTime();
		Date fechaSeleccionada;

		fechaSeleccionada = (Date) filter;

		String seleccionada = df2.format(fechaSeleccionada);
		String grid = df2.format(fechaGrid);

		return seleccionada.equals(grid);
	}

	/**
	 * Obtiene las refacciones de un backlog
	 * 
	 * @param backlog
	 * @return Cadena con todas las refacciones
	 */
	public String obtenerPartes(BacklogsMineros backlog) {
		String partes = "";
		// asignarPartes(backlog);
		List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = backlog.getListaRefacciones();
		int i = 0;
		int listSize = backlogsMinerosDetalleRefaList.size();
		
		for (BacklogsMinerosDetalleRefa refa : backlogsMinerosDetalleRefaList) {
			String numeroParte = refa.getNumeroParte();
			int cantidad = refa.getCantidad();
			String descripcion = refa.getDescripcionMayuscula();

			if ((i + 1) == listSize) {
				partes += "[" + cantidad + "] - " + numeroParte + " - " + descripcion + ".";
			} else {
				partes += "[" + cantidad + "] - " + numeroParte + " - " + descripcion + ", ";
			}

			i++;
		}
		
		
		return partes;
	}

	/**
	 * Formatea el total de las refacciones de un total recibido
	 * 
	 * @return total formateado
	 */
	public String getTotalFormateadoDouble(Double total) {
		String totalFormateado = "";
		if (total != null) {
			totalFormateado = numberFormat.format(total);
		}
		return totalFormateado;
	}

	public String getTotalRefaccionBLSeleccionado() {
		if (seleccionBean.getBacklogsMinerosSeleccionado() != null) {
			List<BacklogsMinerosDetalleRefa> refacciones = seleccionBean.getBacklogsMinerosSeleccionado()
					.getListaRefacciones();
			Double total = 0.0;

			for (BacklogsMinerosDetalleRefa refTemp : refacciones) {
				total += refTemp.getSubTotal();
			}
			
			String totalRecibido = "";
			totalRecibido = getTotalFormateadoDouble(total);
			return totalRecibido;

		}
		return "";
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
	 * Calcula y regresa el total de una Lista de Refacciones
	 * 
	 * @param refacciones
	 * @return
	 */
	public String generarTotalRefacciones(List<BacklogsMinerosDetalleRefa> refacciones) {
		Double total = 0.0;
		for (BacklogsMinerosDetalleRefa refTemp : refacciones) {
			total += refTemp.getSubTotal();
		}
		String totalRecibido = "";
		totalRecibido = getTotalFormateadoDouble(total);
		totalRecibido = totalRecibido.replace("$", ""); // quitamos el signo

		return totalRecibido;
	}

	/***
	 * Cambia el valor de render tooltip, si esta en true cuando se genera el excel
	 * de la lista de backlogs salen las letras
	 */
	public void invertirRenderTooltip() {
		if (renderTooltip) {
			renderTooltip = !renderTooltip;
		}
	}

	public List<BacklogsMineros> listarBacklogsMineros() {
		List<BacklogsMineros> backlogsMinerosList = null;
		try {
			// backlogsMinerosList = backlogsMinerosFacade.obtenerTodosBacklogsMineros();
			backlogsMinerosList = backlogsMinerosFacade.obtenerTodosBacklogsMinerosConRefacciones();
			Comparator<BacklogsMineros> comp = (BacklogsMineros a, BacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(backlogsMinerosList, comp);

		} catch (Exception e) {
			error = "No se pudieron listar los Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError("Backlogs Mineros", error);
		}
		return backlogsMinerosList;
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

	/** FUNCIONES PARA OBTENER DATOS **/

	/**
	 * Verifica si el numero parte matco ingresado existe en la base de datos
	 * 
	 * @param numeroParteMatco - String
	 * @return true, si encuentra los resultados
	 */
	public boolean analizarNumeroPMatco(String numeroParteMatco) {
		NumeroParteClientesFacade npcFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		List<NumeroParteClientes> npcEntityList = new ArrayList<>();
		NumeroParteClientes npcEntity = new NumeroParteClientes();

		try {
			npcEntityList = npcFacade.obtenerNumeroParteClientesPorNumParteMatcoYCliente(numeroParteMatco,
					seleccionBean.getIdClienteMatco());
			if (npcEntityList.isEmpty()) {
				// Buscar en número de parte anterior

				npcEntity = npcFacade.obtenerNumeroParteClientePorNumParteMatcoAnterior(numeroParteMatco,
						seleccionBean.getIdClienteMatco());

				if (npcEntity == null) {
					return false;
				}

				String mensaje = "No se encontro le Número parte " + numeroParteMatco
						+ ", Pero se encontro como Número de parte Matco anterior.";

				agregarMensaje(summary, mensaje, "info", false);

			} else {
				// verificar si tiene un numero de parte cliente valido
				String npc = npcEntityList.get(0).getNumeroParteCliente();
				String npcM = npcEntityList.get(0).getNumeroParteClientesKey().getNumeroParteMatco();
				if (npc == null || npc.equals("")) {
					agregarMensajeWarn(summary, "El Número parte MATCO " + "'" + npcM
							+ "' agregado no cuenta con un Número de parte Cliente");
				}
			}

			return true;

		} catch (Exception e) {
			String error = "No se pudo verificar el número parte Matco";
			log.error(error);
			agregarMensajeError(summary, error);
		}

		return false;
	}

	public List<EstatusBacklogsMineros> listarEstatusBacklogsMineros() {
		List<EstatusBacklogsMineros> estatusBacklogsMinerosList = null;
		try {
			estatusBacklogsMinerosList = estatusBacklogsFacade.obtenerTodosEstatusBacklogsMineros();
			Comparator<EstatusBacklogsMineros> comp = (EstatusBacklogsMineros a, EstatusBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(estatusBacklogsMinerosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Estatus Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return estatusBacklogsMinerosList;
	}

	public List<CodigosSMCS> listarCodigosSMCS() {
		List<CodigosSMCS> codigosSMCSList = null;
		try {
			codigosSMCSList = codigosSMCSFacade.obtenerTodosCodigosSMCS();
			Comparator<CodigosSMCS> comp = (CodigosSMCS a, CodigosSMCS b) -> {
				Integer codigoA = a.getCodigoSMCS();
				Integer codigoB = b.getCodigoSMCS();
				return codigoA.compareTo(codigoB);
			};
			Collections.sort(codigosSMCSList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Códigos SMCS";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return codigosSMCSList;
	}

	public List<CodigosSistemas> listarCodigosSistemas() {
		List<CodigosSistemas> codigosSistemasList = null;
		try {
			codigosSistemasList = codigosSistemasFacade.obtenerTodosCodigosSistemas();
			Comparator<CodigosSistemas> comp = (CodigosSistemas a, CodigosSistemas b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(codigosSistemasList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Códigos Sistemas";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return codigosSistemasList;
	}

	public List<OrigenesBacklogsMineros> listarOrigenesBacklogsMineros() {
		List<OrigenesBacklogsMineros> origenesBacklogsMinerosList = null;
		try {
			origenesBacklogsMinerosList = origenesBacklogsMinerosFacade.obtenerTodosOrigenesBacklogsMineros();
			Comparator<OrigenesBacklogsMineros> comp = (OrigenesBacklogsMineros a, OrigenesBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(origenesBacklogsMinerosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los Origenes Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return origenesBacklogsMinerosList;
	}

	public List<PrioridadesBacklogsMineros> listarPrioridadesBacklogsMineros() {
		List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList = null;
		try {
			prioridadesBacklogsMinerosList = prioridadesBacklogsMinerosFacade.obtenerTodosPrioridadesBacklogsMineros();
			Comparator<PrioridadesBacklogsMineros> comp = (PrioridadesBacklogsMineros a,
					PrioridadesBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(prioridadesBacklogsMinerosList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return prioridadesBacklogsMinerosList;
	}

	public List<MaquinariaDto> listarMaquinariaDtoBacklogsMineros() {
		List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();

		try {
			Short idSucursal = obtenerSucursalFiltro();
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

		// setMaquinariaDtoList(maquinariaDtoList);
		// setNumeroEconomicoList(createNumEconomicoList(maquinariaDtoList));

		return maquinariaDtoList;

	}

	public List<MaquinariaDto> listarMaquinariaDtoBacklogsMinerosSucursal(int pSucursal) {
		List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();

		try {
			Short idSucursal = (short) pSucursal;
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

		// setMaquinariaDtoList(maquinariaDtoList);
		// setNumeroEconomicoList(createNumEconomicoList(maquinariaDtoList));

		return maquinariaDtoList;

	}

	public List<Modelo> obtenerModelos() {
		List<Modelo> modelo = new ArrayList<>();
		try {
			modelo = modeloFacade.obtenerTodosModelos();
		} catch (Exception e) {
			error = "No se pudo listar los modelos";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}

		return modelo;
	}

	public boolean isRenderTooltip() {
		return renderTooltip;
	}

	public void setRenderTooltip(boolean renderTooltip) {
		this.renderTooltip = renderTooltip;
	}

	public RevisionKardexEntity getRevisionSeleccionada() {
		return revisionSeleccionada;
	}

	public void setRevisionSeleccionada(RevisionKardexEntity revisionSeleccionada) {
		this.revisionSeleccionada = revisionSeleccionada;
	}
}
