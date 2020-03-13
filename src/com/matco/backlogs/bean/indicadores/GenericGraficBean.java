package com.matco.backlogs.bean.indicadores;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Picture;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFFont;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.chart.ChartSeries;
import com.matco.amce3.facade.DatosGraficasFacade;
import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.backlogs.bean.LoginBean;
import com.matco.backlogs.bean.BacklogsStaticsVarBean;
import com.matco.backlogs.entity.IndicadoresEntity;

@ManagedBean(name = "genericGraficBean")
@ViewScoped
public class GenericGraficBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = -7006591998055167705L;
	private static final Logger log = Logger.getLogger(GenericGraficBean.class);
	private static final String[] MESES;
	private static final String[] titulosGraficas;

	private boolean renderBtnExport = false;

	private String ANIOS[];
	public int almacen = obtenerSucursalFiltro();

	public String detalle;
	public String maquinaria; // serie
	public String numEconomico;
	public String modelo;
	private String comentarioGrafica;
	private String valores;

	public Date fecha1;
	public Date fecha2;
	private String mesSeleccionado1;
	private String anioSeleccionadoString1;
	private String mesSeleccionado2;
	private String anioSeleccionadoString2;

	private String base64Tmp = "";

	private String anioActual = calcularAnioActual();
	private String intervalo;
	private int actualYear = Integer.parseInt(anioActual);
	private int anioSeleccionado = actualYear;
	private int lastYears[] = { actualYear, actualYear - 1, actualYear - 2, actualYear - 3 };

	private GenericBacklogBean genericBacklogBean = this.obtenerBean("genericBacklogBean");
	private LoginBean loginBean = this.obtenerBean("loginBean");
	public BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

	private IndicadoresEntity comentarioSeleccionado;

	public DatosGraficasFacade graficasFacade = new DatosGraficasFacade(RUTA_PROPERTIES_AMCE3);

	List<IndicadoresEntity> lista = new ArrayList<>();
	List<IndicadoresEntity> listaExport = new ArrayList<>();

	// variable de prueba
	private StreamedContent chart;

	public static SimpleDateFormat dateFormat = new SimpleDateFormat("MM/yyyy");

	static {
		titulosGraficas = new String[9];
		titulosGraficas[0] = "BACKLOG GENERADOS / EJECUTADOS / PENDIENTES A EJECUTAR";
		titulosGraficas[1] = "ESTATUS DE BACKLOGS";
		titulosGraficas[2] = "BACKLOG > 30 DÍAS";
		titulosGraficas[3] = "BACKLOG PENDIENTES";
		titulosGraficas[4] = "PORCENTAJE GENERADOS / EJECUTADOS / PENDIENTES A EJECUTAR";
		titulosGraficas[5] = "TIEMPO ESTIMADO DE REPARACIÓN";
		titulosGraficas[6] = "LABOR ESTIMADO DE REPARACIÓN";
		titulosGraficas[7] = "BACKLOG GENERADOS POR ÁREA";
		titulosGraficas[8] = "BACKLOG GENERADOS POR SMCS";

		MESES = new String[12];
		MESES[0] = "Enero";
		MESES[1] = "Febrero";
		MESES[2] = "Marzo";
		MESES[3] = "Abril";
		MESES[4] = "Mayo";
		MESES[5] = "Junio";
		MESES[6] = "Julio";
		MESES[7] = "Agosto";
		MESES[8] = "Septiembre";
		MESES[9] = "Octubre";
		MESES[10] = "Noviembre";
		MESES[11] = "Diciembre";
	}

	@SuppressWarnings("deprecation")
	@PostConstruct
	public void init() {

		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";

		fecha1 = restarMesesFecha(3);
		fecha2 = new Date();

		mesSeleccionado1 = MESES[fecha1.getMonth()];
		mesSeleccionado2 = MESES[fecha2.getMonth()];

		anioSeleccionadoString1 = "" + (fecha1.getYear() + 1900);
		anioSeleccionadoString2 = "" + (fecha2.getYear() + 1900);

		int year = 2013;
		ANIOS = new String[10];
		for (int i = 0; i < 10; i++) {
			ANIOS[i] = "" + (year++);
		}

		base64Tmp = "base64Tmp";
	}

	/**
	 * 
	 */
	public void setValuesToNull() {
		seleccionBean.setDialogGrafica(false);
		seleccionBean.setAuxiliarGrafica(0);
		seleccionBean.setComentarioSeleccionado(null);
	}

	/**
	 * Agrega un comentario de una grafica
	 * 
	 * @param idGrafica
	 */
	public void agregarComentario(int idGrafica) {
		// initModel
		String bean = obtenerGraficBean(idGrafica);
		GraficInterfaz grafica = obtenerBean(bean);
		grafica.initModels();

		IndicadoresEntity indicador = new IndicadoresEntity(idGrafica, almacen, fecha1, fecha2, null, valores,
				maquinaria, numEconomico, modelo, comentarioGrafica, usuario);

		try {
			graficasFacade.agregarComentarioIndicador(indicador);
			agregarMensaje(summary, "Se agregó correctamente el comentario.", "info", true);
			comentarioGrafica = "";
		} catch (Exception e) {
			String mensaje = "No se pudo guardar el comentario";
			log.error(mensaje, e);
			agregarMensaje(summary, mensaje, "error", false);
		}
	}

	public int obtenerMesInt(String mes) {
		int mesInt = 0;

		for (int i = 0; i < MESES.length; i++) {
			if (mes.equals(MESES[i])) {
				mesInt = i;
				break;
			}
		}

		return mesInt;
	}

	/**
	 * Obtiene los comentarios de las gráficas
	 * 
	 * @return
	 */
	public List<IndicadoresEntity> obtenerComentariosGrafica() {
		try {
			if (lista.isEmpty()) {
				lista = graficasFacade.obtenerComentarioIndicadores("", almacen);
			}
		} catch (Exception e) {
			String mensaje = "No se pudieron obtener los comentarios";
			agregarMensaje(summary, mensaje, "error", false);
			log.error(mensaje, e);
		}
		return lista;
	}

	/***
	 * Asigna datos enteros a una gráfica
	 * 
	 * @param chartSeries  gráfica a agregar
	 * @param listaValores lista con los datos
	 */
	public void asignarValoresInt(ChartSeries chartSeries, List<Integer> listaValores) {
		for (int i = 0; i < listaValores.size(); i++) {
			chartSeries.set(MESES[i], listaValores.get(i));
		}
	}

	/**
	 * Calcula la nueva manera en la que se mostraran las columnas
	 * 
	 * @param fecha1
	 * @param fecha2
	 */
	/*
	 * @SuppressWarnings("deprecation") public void crearColumnas(ChartSeries
	 * chartSeries, Date fecha1, Date fecha2, List<Integer> valores) { int contador
	 * = 0;
	 * 
	 * if (fecha1.getYear() == fecha2.getYear()) { int mes1 = fecha1.getMonth(); int
	 * mes2 = fecha2.getMonth();
	 * 
	 * for (int i = mes1; i <= mes2; i++) { chartSeries.set(MESES[i] + " - " +
	 * (fecha1.getYear() + 1900), valores.isEmpty() ? 0 : valores.get(contador));
	 * contador++; } } else { int yearsDiff = fecha2.getYear() - fecha1.getYear();
	 * int mes1 = fecha1.getMonth(); int mes2 = fecha2.getMonth(); int cont = 0;
	 * boolean flag = false; contador = 0; do { if (mes1 > mes2) { mes2 = 11; } else
	 * if (mes2 == 11) { if (cont > 0) { mes1 = 0; mes2 = 11; } if (cont ==
	 * yearsDiff && cont != 0) { mes1 = 0; mes2 = fecha2.getMonth();
	 * 
	 * } } for (int i = mes1; i <= mes2; i++) { chartSeries.set(MESES[i] + " - " +
	 * (fecha1.getYear() + 1900 + cont), valores.isEmpty() ? 0 :
	 * valores.get(contador)); contador++; } if (yearsDiff != cont) { cont++; } else
	 * { flag = true; } } while (flag == false); } }
	 */
	@SuppressWarnings("deprecation")
	public void crearColumnas(ChartSeries chartSeries, Date fecha1, Date fecha2, List<Integer> valores) {
		int contador = 0;

		if (fecha1.getYear() == fecha2.getYear()) {
			int mes1 = fecha1.getMonth();
			int mes2 = fecha2.getMonth();

			for (int i = mes1; i <= mes2; i++) {
				chartSeries.set(MESES[i] + " - " + (fecha1.getYear() + 1900),
						valores.isEmpty() ? 0 : valores.get(contador));
				contador++;
			}
		} else {

			int yearDate1 = fecha1.getYear();
			int yearDate2 = fecha2.getYear();
			int mes1 = fecha1.getMonth();
			int mes2 = fecha2.getMonth();
			int cont = 0;

			int yeardiff = yearDate2 - yearDate1;
			int mesesTotal = (yeardiff * 12 + (mes2 - mes1)) + 1;

			for (int i = 0; i < mesesTotal; i++) {

				chartSeries.set(MESES[mes1++] + " - " + (fecha1.getYear() + 1900 + cont),
						valores.isEmpty() ? 0 : valores.get(i));

				if (mes1 == 12) {
					mes1 = 0;
					cont++;
				}
			}
		}
	}

	/**
	 * Calcula la nueva manera en la que se mostraran las columnas
	 * 
	 * @param fecha1
	 * @param fecha2
	 */
	@SuppressWarnings("deprecation")
	public void crearColumnasDouble(ChartSeries chartSeries, Date fecha1, Date fecha2, List<Double> valores) {
		int contador = 0;

		if (fecha1.getYear() == fecha2.getYear()) {
			int mes1 = fecha1.getMonth();
			int mes2 = fecha2.getMonth();

			for (int i = mes1; i <= mes2; i++) {
				chartSeries.set(MESES[i] + " - " + (fecha1.getYear() + 1900),
						valores.isEmpty() ? 0 : valores.get(contador));
				contador++;
			}
		} else {

			int yearDate1 = fecha1.getYear();
			int yearDate2 = fecha2.getYear();
			int mes1 = fecha1.getMonth();
			int mes2 = fecha2.getMonth();
			int cont = 0;

			int yeardiff = yearDate2 - yearDate1;
			int mesesTotal = (yeardiff * 12 + (mes2 - mes1)) + 1;

			for (int i = 0; i < mesesTotal; i++) {

				chartSeries.set(MESES[mes1++] + " - " + (fecha1.getYear() + 1900 + cont),
						valores.isEmpty() ? 0 : valores.get(i));

				if (mes1 == 12) {
					mes1 = 0;
					cont++;
				}
			}
		}
	}

	/**
	 * Asigna datos de tipo double a una gráfica
	 * 
	 * @param chartSeries  gráfica a agregar
	 * @param listaValores lista con los datos
	 */
	public void asignarValoresDouble(ChartSeries chartSeries, List<Double> listaValores) {
		for (int i = 0; i < listaValores.size(); i++) {
			chartSeries.set(MESES[i], listaValores.get(i));
		}
	}

	public void seleccionNumEconomico() {
		if (numEconomico == null || numEconomico.equals("")) {
			return;
		}
		maquinaria = genericBacklogBean.obtenerMaquinariaPorNumEconomico(seleccionBean.getMaquinariaDtoList(),
				numEconomico);
		modelo = genericBacklogBean.obtenerModeloPorSerie(seleccionBean.getMaquinariaDtoList(), maquinaria);
	}

	/**
	 * Este metodo activa los campos para la captura del BL
	 */
	public void seleccionNumSerie() {
		if (maquinaria == null || maquinaria.equals("")) {
			return;
		}
		numEconomico = genericBacklogBean.obtenerNumEconomicoPorSerie(seleccionBean.getMaquinariaDtoList(), maquinaria);
		modelo = genericBacklogBean.obtenerModeloPorSerie(seleccionBean.getMaquinariaDtoList(), maquinaria);
	}

	public void seleccionModelo() {
		//maquinaria = genericBacklogBean.obtenerSeriePorModelo(seleccionBean.getMaquinariaDtoList(), modelo);
		//numEconomico = genericBacklogBean.obtenerNumEconomicoPorSerie(seleccionBean.getMaquinariaDtoList(), maquinaria);
		maquinaria = null;
		numEconomico = null;
	}

	/**
	 * Asigna el url de la grafica que se mostrara
	 * 
	 * @param int - grafica
	 */
	public void seleccionarGrafica(int grafica) {
		seleccionBean.setAuxiliarGrafica(grafica);
		switch (grafica) {
		case 1:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaAdminBL.xhtml");
			break;
		case 2:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaEstatusBL.xhtml");
			break;
		case 3:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaMayor30Dias.xhtml");
			break;
		case 4:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaPendientesBL.xhtml");
			break;
		case 5:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaPorcentajeBL.xhtml");
			break;
		case 6:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaHorasHyM.xhtml");
			break;
		case 7:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaTiempoEstimadoBL.xhtml");
			break;
		case 8:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaGeneradosAreaBL.xhtml");
			break;
		case 9:
			seleccionBean.setDialogGrafica(false);
			seleccionBean.setGraficaSeleccionada("graficas/graficaGeneradosSMCS.xhtml");
			break;
		default:
			setValuesToNull();
		}

	}

	/**
	 * Decide cual gráfica se mostrará
	 * 
	 * @param idGrafica
	 * @return
	 */
	public boolean mostrarGrafica(int idGrafica) {
		IndicadoresEntity comentario = comentarioSeleccionado;

		if (comentario != null) {
			int id1 = comentario.getIdGrafica();
			if (id1 == idGrafica) {
				seleccionBean.setDialogGrafica(true);
				seleccionBean.setAuxiliarGrafica(idGrafica);
				seleccionBean.setComentarioSeleccionado(comentario);
				// obtenemos el bean y ejecutamos la funcion buscar
				String bean = obtenerGraficBean(idGrafica);
				GraficInterfaz grafica = obtenerBean(bean);
				grafica.buscar();
				return true;
			}
		}
		return false;
	}

	public String obtenerGraficBean(int idGrafica) {
		String grafica = "";
		switch (idGrafica) {
		case 1:
			grafica = "graficaTotalBacklogsBean";
			break;
		case 2:
			grafica = "graficaEstatusBLBean";
			break;
		case 3:
			grafica = "graficaMayor30Bean";
			break;
		case 4:
			grafica = "graficaBacklogsPendientesBean";
			break;
		case 5:
			grafica = "graficaPorcentajeEstatusBean";
			break;
		case 6:
			grafica = "graficaHorasBean";
			break;
		case 7:
			grafica = "graficaLaborEstimadoBean";
			break;
		case 8:
			grafica = "graficaBLGeneradoAreaBean";
			break;
		case 9:
			grafica = "graficaBLGeneradoSMCS";
			break;
		}

		return grafica;
	}

	/**
	 * Prepara el base64 para tomar la imagen de la gráfica
	 * 
	 * public void exportarGrafica() { Map<String, String> params =
	 * FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
	 * String prueba = params.get("base64Img"); base64Tmp = prueba;
	 * PrimeFaces.current().ajax().update("dtExport");
	 * PrimeFaces.current().executeScript("PF('verGrafica').hide();"); }
	 */

	public void crearImagen() {
		listaExport.clear();
		IndicadoresEntity indicador = comentarioSeleccionado;
		listaExport.add(indicador);
		int idGrafica = indicador.getIdGrafica();
		mostrarGrafica(idGrafica);
		PrimeFaces.current().ajax().update("formVerGrafica");
		PrimeFaces.current().ajax().update("dtExport");

		renderBtnExport = true;
	}

	public void guardarBase64() {
		Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
		base64Tmp = params.get("base64Img");

		PrimeFaces.current().executeScript("document.getElementById('formExport:link2').click();");
	}

	/**
	 * Tomara el excel que se esta por exportar y agregará la imagen
	 * 
	 * @param document
	 */
	public void postProcessXLS(Object document) {
		XSSFWorkbook workbook = (XSSFWorkbook) document;
		XSSFSheet sheet = workbook.getSheetAt(0);
		Sheet hoja = sheet;
		// Agregamos el Header Imagen
		XSSFCellStyle cellStyle = workbook.createCellStyle();
		Row row = hoja.createRow(3);
		Cell cell = row.createCell(0);
		cell.setCellValue("Gráfica");
		XSSFFont font = workbook.createFont();
		font.setFontName("Arial");
		cellStyle.setFont(font);
		cellStyle.setAlignment(HorizontalAlignment.CENTER);
		cell.setCellStyle(cellStyle);

		String[] splitBase = base64Tmp.split(",");

		if (splitBase.length < 2) {
			agregarMensaje(summary, "No pudo obtener la imagen de la gráfica", "warn", false);
			return;
		}

		byte[] imageTmp = Base64.getDecoder().decode(splitBase[1]);
		// InputStream imagen = new ByteArrayInputStream(imageTmp);
		try {
			setimagen(workbook, hoja, "Img", imageTmp);
			// workbook.close();
			// imageTmp = null;
		} catch (Exception e) {
			String mensaje = "No se pudo guardar la imagen en Excel.";
			agregarMensaje(summary, mensaje, "error", true);
			log.error(mensaje, e);
		}

	}

	/**
	 * Función para insertar una imagen en Excel
	 * 
	 * @param workbook
	 * @param sheet
	 * @param name
	 * @param bytes
	 */
	public void setimagen(XSSFWorkbook workbook, Sheet sheet, String name, byte[] bytes) throws Exception {
		// Font whiteFont = workbook.createFont();
		// whiteFont.setColor(IndexedColors.ORANGE.index);
		// whiteFont.setFontHeightInPoints((short) 20.00);
		// whiteFont.setBold(true);

		// XSSFCellStyle cellheader = workbook.createCellStyle();
		// cellheader.setAlignment(HorizontalAlignment.CENTER);
		// cellheader.setWrapText(true);
		// cellheader.setFont(whiteFont);

		// NOMBRE DEL REPORTE
		// Row row = sheet.createRow((short) 4);
		// row.setHeightInPoints((float) 69.00);
		// Cell cell = row.createCell((short) 4);
		// sheet.addMergedRegion(CellRangeAddress.valueOf("$E$2:$G$2"));
		// cell.setCellValue(new XSSFRichTextString(name));
		// cell.setCellStyle(cellheader);
		// Get the contents of an InputStream as a byte[].
		// byte[] bytes = IOUtils.toByteArray(file);

		// Adds a picture to the workbook
		int pictureIdx = workbook.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		// close the input stream
		// Returns an object that handles instantiating concrete classes
		CreationHelper helper = workbook.getCreationHelper();
		// Creates the top-level drawing patriarch.
		@SuppressWarnings("rawtypes")
		Drawing drawing = sheet.createDrawingPatriarch();
		// Create an anchor that is attached to the worksheet
		ClientAnchor anchor = helper.createClientAnchor();
		// set top-left corner for the image
		anchor.setDx1(0);
		anchor.setDy1(0);
		anchor.setDx2(1023);
		anchor.setDy2(0);
		anchor.setCol1(0);
		anchor.setRow1(4);
		anchor.setCol2(0);
		anchor.setRow2(4);
		// Creates a picture
		Picture pict = drawing.createPicture(anchor, pictureIdx);
		// Reset the image to the original size
		pict.resize();
		sheet.createFreezePane(0, 3, 0, 3);

	}

	/**
	 * Limpia las opciones
	 * 
	 */
	public void limpiarFiltros() {
		modelo = null;
		maquinaria = null;
		numEconomico = null;
	}

	/* GETTERS Y SETTERS */
	public String getIntervalo() {
		return intervalo;
	}

	public void setIntervalo(String intervalo) {
		this.intervalo = intervalo;
	}

	public String getAnioActual() {
		return anioActual;
	}

	public void setAnioActual(String anioActual) {
		this.anioActual = anioActual;
	}

	public int getAnioSeleccionado() {
		return anioSeleccionado;
	}

	public void setAnioSeleccionado(int anioSeleccionado) {
		this.anioSeleccionado = anioSeleccionado;
	}

	public int getActualYear() {
		return actualYear;
	}

	public void setActualYear(int actualYear) {
		this.actualYear = actualYear;
	}

	public int[] getLastYears() {
		return lastYears;
	}

	public void setLastYears(int[] lastYears) {
		this.lastYears = lastYears;
	}

	public String getMaquinaria() {
		return maquinaria;
	}

	public void setMaquinaria(String maquinaria) {
		this.maquinaria = maquinaria;
	}

	public String getNumEconomico() {
		return numEconomico;
	}

	public void setNumEconomico(String numEconomico) {
		this.numEconomico = numEconomico;
	}

	public Date getFecha1() {
		return fecha1;
	}

	public void setFecha1(Date fecha1) {
		this.fecha1 = fecha1;
	}

	public Date getFecha2() {
		return fecha2;
	}

	public void setFecha2(Date fecha2) {
		this.fecha2 = fecha2;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public static String[] getMeses() {
		return MESES;
	}

	public String getMes(int pos) {
		return MESES[pos];
	}

	public String getMesSeleccionado1() {
		return mesSeleccionado1;
	}

	public void setMesSeleccionado1(String mesSeleccionado1) {
		this.mesSeleccionado1 = mesSeleccionado1;
	}

	public String getAnioSeleccionadoString1() {
		return anioSeleccionadoString1;
	}

	public void setAnioSeleccionadoString1(String anioSeleccionadoString1) {
		this.anioSeleccionadoString1 = anioSeleccionadoString1;
	}

	public String getMesSeleccionado2() {
		return mesSeleccionado2;
	}

	public void setMesSeleccionado2(String mesSeleccionado2) {
		this.mesSeleccionado2 = mesSeleccionado2;
	}

	public String getAnioSeleccionadoString2() {
		return anioSeleccionadoString2;
	}

	public void setAnioSeleccionadoString2(String anioSeleccionadoString2) {
		this.anioSeleccionadoString2 = anioSeleccionadoString2;
	}

	public String[] getANIOS() {
		return ANIOS;
	}

	public void setANIOS(String[] aNIOS) {
		ANIOS = aNIOS;
	}

	public String getComentarioGrafica() {
		return comentarioGrafica;
	}

	public void setComentarioGrafica(String comentarioGrafica) {
		this.comentarioGrafica = comentarioGrafica;
	}

	public String getValores() {
		return valores;
	}

	public void setValores(String valores) {
		this.valores = valores;
	}

	public IndicadoresEntity getComentarioSeleccionado() {
		return comentarioSeleccionado;
	}

	public void setComentarioSeleccionado(IndicadoresEntity comentarioSeleccionado) {
		this.comentarioSeleccionado = comentarioSeleccionado;
	}

	public StreamedContent getChart() {
		return chart;
	}

	public void setChart(StreamedContent chart) {
		this.chart = chart;
	}

	public List<IndicadoresEntity> getListaExport() {
		return listaExport;
	}

	public void setListaExport(List<IndicadoresEntity> listaExport) {
		this.listaExport = listaExport;
	}

	public String getBase64Tmp() {
		return base64Tmp;
	}

	public void setBase64Tmp(String base64Tmp) {
		this.base64Tmp = base64Tmp;
	}

	public String[] getTitulosgraficas() {
		return titulosGraficas;
	}

	public boolean isRenderBtnExport() {
		return renderBtnExport;
	}

	public void setRenderBtnExport(boolean renderBtnExport) {
		this.renderBtnExport = renderBtnExport;
	}

	/* FIN */
}
