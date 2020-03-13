package com.matco.backlogs.bean;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFClientAnchor;
import org.apache.poi.hssf.usermodel.HSSFPalette;
import org.apache.poi.hssf.usermodel.HSSFPatriarch;
import org.apache.poi.hssf.usermodel.HSSFPicture;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.util.IOUtils;
import org.imgscalr.Scalr;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.ImagenesBacklogsMinerosFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.NumeroParteClientesFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.ImagenesBacklogsMineros;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.NumeroParteClientes;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.ejes.entity.Marca;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;

/***
 * Metodos para la generacion del archivo excel de solicitud de cotizacion
 * 
 * @author N soluciones
 *
 */
public class GenerarExcel extends GenericBacklogBean implements Serializable {
	
	private static final long serialVersionUID = 6607576468339716478L;
	private final String ALMACEN = "027c";
	private final String CENTRO = "o001";
	private final String ENCABEZADOS[] = { "#BL", "DESCRIPCION DE MATERIAL", "NO SAP", "NUM. PARTE CAT", "CANTIDAD", "",
			"", "", "ALMACEN", "CENTRO", "OPERACION" };

	private final int OPERACION = 30;
	@SuppressWarnings("unused")
	private double total = 0;
	private BacklogsMineros backlogMineroSeleccionado;
	private BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
			RUTA_PROPERTIES_AMCE3);

	private NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
	private ImagenesBacklogsMinerosFacade imagenesBacklogsMinerosFacade;
	BacklogsMinerosKey backlogsMinerosKey;

	private List<BacklogsMineros> listaBacklogs;
	HSSFWorkbook libroExcel = new HSSFWorkbook();
	private Marca marca;
	@SuppressWarnings("unused")
	private String serie;

	private LadoComponente ladoComponente = new LadoComponente();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();
	
	private BacklogsStaticsVarBean seleccionBean = obtenerBean("backlogsStaticsVarBean");

	public GenerarExcel(BacklogsMineros backlogMineroSeleccionado) {
		listaBacklogs = new ArrayList<BacklogsMineros>();
		listaBacklogs.add(backlogMineroSeleccionado);
		this.backlogMineroSeleccionado = backlogMineroSeleccionado;
		backlogsMinerosKey = backlogMineroSeleccionado.getBacklogsMinerosKey();
	}

	public GenerarExcel(List<BacklogsMineros> listaBacklogs) {
		this.listaBacklogs = listaBacklogs;
	}

	/***
	 * Obtiene los datos de las partes correspondientes al backlog seleccionado
	 * 
	 * @return matriz con los datos de la tabla
	 * @throws Exception
	 */
	public Object[][] obtenerDatosPartes(List<BacklogsMineros> listaBacklogs) throws Exception {
		List<BacklogsMinerosDetalleRefa> listaDetalles = new ArrayList<>();
		for (BacklogsMineros backlogMinero : listaBacklogs) {
			backlogMineroSeleccionado = backlogMinero;
			backlogsMinerosKey = backlogMinero.getBacklogsMinerosKey();
			List<BacklogsMinerosDetalleRefa> listaDetallesBacklog = backlogsMinerosDetalleRefaFacade
					.obtenerBacklogMineroDetalleRefaPorId(backlogsMinerosKey);
			if (listaDetallesBacklog != null) {
				listaDetalles.addAll(listaDetallesBacklog);
			}
		}
		int numeroFilas = listaDetalles.size();
		Object[][] datosPartes = new Object[numeroFilas][11];
		total = 0;
		marca = backlogMineroSeleccionado.getIdMarca();
		String idMarca = marca.getMarca();
		serie = backlogMineroSeleccionado.getNumeroSerie();

		for (int renglonTabla = 0; renglonTabla < numeroFilas; renglonTabla++) {
			BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa = listaDetalles.get(renglonTabla);
			String numeroParteCat = backlogsMinerosDetalleRefa.getNumeroParte();

			NumeroParteClientes npcEntityList = new NumeroParteClientes();

			npcEntityList = numeroParteClientesFacade.obtenerNumeroParteClientePorNumParteMatcoByMarcaCliente(
					numeroParteCat, idMarca, seleccionBean.getIdClienteMatco().intValue());
			String npc = "";
			if (npcEntityList != null) {
				npc = npcEntityList.getNumeroParteCliente();
			}else {
				npc = "";
			}
			
			/*
			 * if (numeroParteClientes != null) {// ENCONTRO EL NUMERO DE PARTE CLIENTE CON
			 * ESE NUMERO CAT numeroParteCliente =
			 * numeroParteClientes.getNumeroParteCliente(); } else {// NO ENCONTRO NPC CON
			 * ESE NUMERO CAT numeroParteClientes = numeroParteClientesFacade
			 * .obtenerNumeroParteClientePorNumParteMatcoAnteriorYSerie(numeroParteCat,
			 * idMarca, serie);
			 * 
			 * if (numeroParteClientes != null) {// PERO SI LO ENCONTRO CON EL NUMERO CAT
			 * ANTERIOR numeroParteCliente = numeroParteClientes.getNumeroParteCliente();
			 * numeroParteCat =
			 * numeroParteClientes.getNumeroParteClientesKey().getNumeroParteMatco(); } }
			 */

			int numeroBacklog = backlogsMinerosDetalleRefa.getBacklogsMinerosDetalleRefaKey().getBacklogsMinerosKey()
					.getIdBacklogMinero();

			String sintoma = "";
			boolean encontrado = false;
			for (BacklogsMineros backlogMinero : listaBacklogs) {
				if (!encontrado) {
					if (backlogMinero.getBacklogsMinerosKey().getIdBacklogMinero() == numeroBacklog) {
						encontrado = true;
						sintoma = backlogMinero.getSintomasEquipo();
					}
				}
			}

			String numeroSintoma = numeroBacklog + " - " + sintoma;

			datosPartes[renglonTabla][0] = numeroSintoma;// #BL
			datosPartes[renglonTabla][1] = backlogsMinerosDetalleRefa.getDescripcionMayuscula();// descipcion
			datosPartes[renglonTabla][2] = npc;// No. SAP
			datosPartes[renglonTabla][3] = numeroParteCat;// No. parte
			datosPartes[renglonTabla][4] = backlogsMinerosDetalleRefa.getCantidad();// cantidad
			datosPartes[renglonTabla][5] = "";
			datosPartes[renglonTabla][6] = "";
			datosPartes[renglonTabla][7] = "";
			datosPartes[renglonTabla][8] = ALMACEN; // ALMACEN
			datosPartes[renglonTabla][9] = CENTRO;// CENTRO
			datosPartes[renglonTabla][10] = OPERACION;// OPERACION
			total += backlogsMinerosDetalleRefa.getTotal();
		}
		return datosPartes;
	}

	public void obtenerHojaReporteServicio(List<BacklogsMineros> listaBacklogs) throws Exception {
		// COLORES
		HSSFPalette palette = libroExcel.getCustomPalette();
		HSSFColor myColor = palette.findSimilarColor(255, 192, 0);
		short amarilloMatco = myColor.getIndex();// amarillo
		myColor = palette.findSimilarColor(213, 0, 0);
		short rojoBuenavista = myColor.getIndex(); // rojo

		// FUENTES
		Font negritasBlanco = libroExcel.createFont();
		negritasBlanco.setFontName("Calibri");
		negritasBlanco.setFontHeightInPoints((short) 14);
		negritasBlanco.setBold(true);
		negritasBlanco.setColor(IndexedColors.WHITE.getIndex());
		Font negrita12 = libroExcel.createFont();
		negrita12.setFontName("Calibri");
		negrita12.setBold(true);
		negrita12.setFontHeightInPoints((short) 12);
		Font negrita11 = libroExcel.createFont();
		negrita11.setFontName("Calibri");
		negrita11.setBold(true);
		negrita11.setFontHeightInPoints((short) 11);
		Font negrita10 = libroExcel.createFont();
		negrita10.setFontName("Calibri");
		negrita10.setBold(true);
		negrita10.setFontHeightInPoints((short) 10);
		Font negritasRojo = libroExcel.createFont();
		negritasRojo.setFontName("Calibri");
		negritasRojo.setFontHeightInPoints((short) 12);
		negritasRojo.setBold(true);
		negritasRojo.setColor(rojoBuenavista);

		// ESTILOS
		// estilo para encabezado de la tabla
		CellStyle encabezado = libroExcel.createCellStyle();
		encabezado.setBorderBottom(BorderStyle.THICK);
		encabezado.setBorderTop(BorderStyle.THICK);
		encabezado.setBorderLeft(BorderStyle.THICK);
		encabezado.setBorderRight(BorderStyle.THICK);
		encabezado.setBottomBorderColor(amarilloMatco);
		encabezado.setTopBorderColor(amarilloMatco);
		encabezado.setLeftBorderColor(amarilloMatco);
		encabezado.setRightBorderColor(amarilloMatco);
		encabezado.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		encabezado.setFillForegroundColor(IndexedColors.BLACK.getIndex());
		encabezado.setFont(negritasBlanco);
		encabezado.setAlignment(HorizontalAlignment.CENTER);
		// estilo tabla
		CellStyle tabla = libroExcel.createCellStyle();
		tabla.setBorderBottom(BorderStyle.THIN);
		tabla.setBorderTop(BorderStyle.THIN);
		tabla.setBorderLeft(BorderStyle.THICK);
		tabla.setBorderRight(BorderStyle.THICK);
		tabla.setBottomBorderColor(amarilloMatco);
		tabla.setTopBorderColor(amarilloMatco);
		tabla.setLeftBorderColor(amarilloMatco);
		tabla.setRightBorderColor(amarilloMatco);
		tabla.setAlignment(HorizontalAlignment.CENTER);
		CellStyle neg12 = libroExcel.createCellStyle();
		neg12.setFont(negrita12);
		neg12.setAlignment(HorizontalAlignment.CENTER);
		CellStyle neg11 = libroExcel.createCellStyle();
		neg11.setFont(negrita11);
		neg11.setAlignment(HorizontalAlignment.CENTER);
		CellStyle neg10 = libroExcel.createCellStyle();
		neg10.setFont(negrita10);
		neg10.setAlignment(HorizontalAlignment.CENTER);
		CellStyle negRojo = libroExcel.createCellStyle();
		negRojo.setFont(negritasRojo);
		negRojo.setAlignment(HorizontalAlignment.CENTER);

		final String rutaLogoBuenavista = getExternalContext().getInitParameter("rutaLogoBuenavista");
		HSSFSheet hojaExcel = libroExcel.createSheet();

		HSSFRow fila = hojaExcel.createRow(0);// empresa
		HSSFCell celda = fila.createCell(3);
		celda.setCellStyle(neg12);
		celda.setCellValue("BUENAVISTA DEL COBRE S.A DE C.V.");
		fila = hojaExcel.createRow(1);
		celda = fila.createCell(3);
		celda.setCellStyle(neg11);
		celda.setCellValue("REPORTE DE SERVICIO");
		fila = hojaExcel.createRow(2);// ing t planeacion de mantenimiento mina
		celda = fila.createCell(3);
		celda.setCellStyle(neg10);
		celda.setCellValue("INGENIERÍA Y PLANEACIÓN DE MANTENIMIENTO MINA");
		fila = hojaExcel.createRow(3);
		celda = fila.createCell(3);
		celda.setCellStyle(neg10);
		Date hoy = new Date();
		SimpleDateFormat simppleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fecha = simppleDateFormat.format(hoy);
		String dia = fecha.split("/")[0];
		String mes = fecha.split("/")[1];
		int m = Integer.parseInt(mes) - 1;
		String meses[] = { "ENERO", "FEBRERO", "MARZO", "ABRIL", "MAYO", "JUNIO", "JULIO", "AGOSTO", "SEPTIEMBRE",
				"OCTUBRE", "NOVIEMBRE", "DICIEMBRE" };
		mes = meses[m];
		String anio = fecha.split("/")[2];
		fecha = dia + " DE " + mes + " DE " + anio;
		celda.setCellValue(fecha);
		fila = hojaExcel.createRow(4); // numero economico y numero de serie
		celda = fila.createCell(3);
		celda.setCellStyle(negRojo);
		String serie = "";
		for (BacklogsMineros backlog : listaBacklogs) {
			String serieBacklog = backlog.getNumeroSerie();
			if (backlog.getNumeroEconomico() != null) {
				serieBacklog = backlog.getNumeroEconomico() + " - " + serieBacklog;
			}
			serie = serieBacklog;
		}
		celda.setCellValue(serie);
		fila = hojaExcel.createRow(5);
		celda = fila.createCell(1);
		celda.setCellStyle(encabezado);
		celda.setCellValue("BL");
		celda = fila.createCell(2);
		celda.setCellStyle(encabezado);
		celda.setCellValue("#");
		celda = fila.createCell(3);
		celda.setCellStyle(encabezado);
		celda.setCellValue("Actividad");
		int consecutivo = 1;
		for (BacklogsMineros backlog : listaBacklogs) {
			fila = hojaExcel.createRow(5 + consecutivo);
			BacklogsMinerosKey backlogsMinerosKey = backlog.getBacklogsMinerosKey();
			int id = backlogsMinerosKey.getIdBacklogMinero();
			celda = fila.createCell(1);
			celda.setCellStyle(tabla);
			celda.setCellValue(id);
			celda = fila.createCell(2);
			celda.setCellStyle(tabla);
			celda.setCellValue(consecutivo);
			celda = fila.createCell(3);
			celda.setCellStyle(tabla);
			String actividad = backlog.getSintomasEquipo();

			String codigoLDBL = backlog.getLadoComponenteOb().getCodigoLDC();
			String descripcionLDBL = backlog.getLadoComponenteOb().getDescripcion();
			if (codigoLDBL != null) {
				LadoComponenteFacade ladoComponenteFacade = new LadoComponenteFacade(RUTA_PROPERTIES_AMCE3);
				try {
					ladoComponenteList = ladoComponenteFacade.obtenerTodosLDC();
					for (LadoComponente ld : ladoComponenteList) {
						if (ld.getCodigoLDC().equals(codigoLDBL)) {
							ladoComponente.setCodigoLDC(codigoLDBL);
							ladoComponente.setDescripcion(ld.getDescripcion());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			} else {
				ladoComponente.setCodigoLDC("");
				ladoComponente.setDescripcion(descripcionLDBL);
			}
			/*
			 * if (backlog.getLadoComponente() != null) { actividad += ". LADO " +
			 * backlog.getLadoComponente() + " "; }
			 */

			actividad += ". " + ladoComponente.getDescripcion();

			for (BacklogsMinerosDetalleRefa refaccion : backlog.getListaRefacciones()) {
				String numeroParte = refaccion.getNumeroParte();
				int cantidad = refaccion.getCantidad();
				actividad += " " + cantidad + "--" + numeroParte;
			}

			/*
			 * List<BacklogsMinerosDetalleRefa> listaDetalles =
			 * backlogsMinerosDetalleRefaFacade
			 * .obtenerBacklogMineroDetalleRefaPorId(backlogsMinerosKey); for
			 * (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : listaDetalles) {
			 * String numeroParte = backlogsMinerosDetalleRefa.getNumeroParte(); int
			 * cantidad = backlogsMinerosDetalleRefa.getCantidad(); actividad += cantidad +
			 * "-" + numeroParte + " "; }
			 */
			celda.setCellValue(actividad);
			consecutivo++;
		}
		FileInputStream stream = new FileInputStream(rutaLogoBuenavista);
		byte[] bytes = IOUtils.toByteArray(stream);
		final HSSFPatriarch drawing = hojaExcel.createDrawingPatriarch();
		final ClientAnchor anchor = new HSSFClientAnchor();
		int pictureIndex;
		pictureIndex = libroExcel.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
		anchor.setCol1(0);
		anchor.setCol2(1);
		anchor.setRow1(0);
		final HSSFPicture pict = drawing.createPicture(anchor, pictureIndex);
		pict.resize();
		hojaExcel.autoSizeColumn(3);
	}

	/***
	 * Obtiene la hoja de excel correspondiente al backlog actual
	 * 
	 * @return hoja de excel del backlog actual
	 * @throws Exception
	 */
	public void obtenerHojaSolicitud(List<BacklogsMineros> listaBacklogs, boolean imagenes) throws Exception {
		HSSFSheet hojaExcel = libroExcel.createSheet();
		Object[][] datosPartes = obtenerDatosPartes(listaBacklogs);
		// lista con las celdas en la que se va a dibujar un renglon
		LinkedList<HSSFCell> renglones = new LinkedList<>();
		// estilos
		// estilo para las celdas del encabezado de la tabla
		CellStyle headerStyle = libroExcel.createCellStyle();
		headerStyle.setWrapText(true);
		Font font = libroExcel.createFont();
		font.setBold(true);
		headerStyle.setFont(font);
		// letra en negritas
		CellStyle negritas = libroExcel.createCellStyle();
		negritas.setFont(font);
		// estilo para las filas impares de la tabla
		HSSFPalette palette = libroExcel.getCustomPalette();
		HSSFColor myColor = palette.findSimilarColor(255, 255, 0);// amarillo
		short palIndex = myColor.getIndex();
		CellStyle style1 = libroExcel.createCellStyle();
		style1.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		style1.setFillForegroundColor(palIndex);
		style1.setBorderBottom(BorderStyle.THIN);
		style1.setBorderTop(BorderStyle.THIN);
		style1.setBorderLeft(BorderStyle.THIN);
		style1.setBorderRight(BorderStyle.THIN);
		// estilo para celdas con fuente numero 14
		CellStyle style14 = libroExcel.createCellStyle();
		Font font14 = libroExcel.createFont();
		font14.setFontName("Calibri");
		font14.setFontHeightInPoints((short) 14);
		style14.setFont(font14);
		// estilo para filas pares de la tabla
		CellStyle style2 = libroExcel.createCellStyle();
		style2.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		HSSFColor myColor2 = palette.findSimilarColor(255, 192, 0);// naranja
		short palIndex2 = myColor2.getIndex();
		style2.setFillForegroundColor(palIndex2);
		style2.setBorderBottom(BorderStyle.THIN);
		style2.setBorderTop(BorderStyle.THIN);
		style2.setBorderLeft(BorderStyle.THIN);
		style2.setBorderRight(BorderStyle.THIN);
		// estilo para celdas con contorno
		CellStyle stylebox = libroExcel.createCellStyle();
		stylebox.setBorderBottom(BorderStyle.THIN);
		stylebox.setBorderTop(BorderStyle.THIN);
		stylebox.setBorderLeft(BorderStyle.THIN);
		stylebox.setBorderRight(BorderStyle.THIN);
		// estilo para celdas con fondo café
		CellStyle styleBrown = libroExcel.createCellStyle();
		styleBrown.setFillPattern(FillPatternType.SOLID_FOREGROUND);
		styleBrown.setFillForegroundColor(IndexedColors.BROWN.getIndex());

		// LLENADO DE FILAS Y COLUMNAS DEL EXCEL
		HSSFRow fila = hojaExcel.createRow(0);
		HSSFCell celda = fila.createCell(0);
		List<String> trabajos = new ArrayList<>();
		String trabajoRealizar = "";
		for (BacklogsMineros backlog : listaBacklogs) {
			String trabajoBacklog = backlog.getTrabajoRealizar();
			if (trabajos.contains(trabajoBacklog)) {

			} else {
				trabajos.add(trabajoBacklog);
				if (trabajos.size() > 1) {
					trabajoBacklog = ", " + trabajoBacklog;
				}
				trabajoRealizar += trabajoBacklog;
			}
		}
		// String trabajoRealizar = backlogMineroSeleccionado.getTrabajoRealizar();
		String numeroEconomico = backlogMineroSeleccionado.getNumeroEconomico();
		String renglon1;
		if (numeroEconomico == null) {
			renglon1 = trabajoRealizar;
		} else {
			String renglonConNumeroEconomico = numeroEconomico + " " + trabajoRealizar;
			renglon1 = renglonConNumeroEconomico;
		}
		celda.setCellValue(renglon1);
		celda = fila.createCell(10);
		celda.setCellStyle(style14);
		Date hoy = new Date();
		SimpleDateFormat simppleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
		String fecha = simppleDateFormat.format(hoy);
		celda.setCellValue(fecha);

		fila = hojaExcel.createRow(1);
		for (int columna = 0; columna < 11; columna++) {
			celda = fila.createCell(columna);
			celda.setCellStyle(styleBrown);
		}
		// Asignar encabezados
		fila = hojaExcel.createRow(2);
		for (int i = 0; i < ENCABEZADOS.length; ++i) {
			String header = ENCABEZADOS[i];
			celda = fila.createCell(i);
			celda.setCellStyle(headerStyle);
			celda.setCellValue(header);
		}

		// combinar celdas
		int filaCelda = 3;
		for (BacklogsMineros backlog : listaBacklogs) {
			backlogsMinerosKey = backlog.getBacklogsMinerosKey();
			List<BacklogsMinerosDetalleRefa> listaDetallesBacklog = backlogsMinerosDetalleRefaFacade
					.obtenerBacklogMineroDetalleRefaPorId(backlogsMinerosKey);
			int npartes = listaDetallesBacklog.size();
			if (npartes > 1) {
				hojaExcel.addMergedRegion(new CellRangeAddress(filaCelda, filaCelda + npartes - 1, 0, 0));
			}
			filaCelda += npartes;
		}
		// asignar valores de tabla
		CellStyle estilo = style1;
		for (int i = 0; i < datosPartes.length; ++i) {
			fila = hojaExcel.createRow(i + 3);
			for (int j = 0; j < datosPartes[i].length; j++) {
				celda = fila.createCell(j);
				celda.setCellStyle(estilo);
				String datoString = datosPartes[i][j].toString();
				celda.setCellValue(datoString);
				if (j == 4 || j == 10) {// #BL, CANTIDAD, OPERACION
					int datoNumerico = Integer.parseInt(datoString);
					celda.setCellValue(datoNumerico);
				} else {
					celda.setCellValue(datoString);
				}
			}
			if (estilo == style1) {
				estilo = style2;
			} else {
				estilo = style1;
			}
		}
		// agregar imagenes
		// las imagenes se deben agregar al final porque si no al ajustar el ancho de
		// las columnas se desacomoda
		// por lo pronto dejamos el espacio en blanco que se necesitara
		int filaImagenesInicio = 3 + datosPartes.length;
		if (imagenes) {
			imagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);

			for (BacklogsMineros backlog : listaBacklogs) {
				List<ImagenesBacklogsMineros> imagenesBacklog = imagenesBacklogsMinerosFacade
						.obtenerImagenesBacklogsMinerosPorID(backlog.getBacklogsMinerosKey());
				if (imagenesBacklog.size() > 0) {
					fila = hojaExcel.createRow(filaImagenesInicio);
					celda = fila.createCell(0);
					// celda.setCellStyle(style14);
					celda.setCellValue("Imágenes backlog: " + backlog.getBacklogsMinerosKey().getIdBacklogMinero());
					filaImagenesInicio += 8;
				}
			}
		}
		int filaImagenesFin = filaImagenesInicio;
		// agregar campos
		fila = hojaExcel.createRow(filaImagenesFin + 1);
		celda = fila.createCell(2);
		celda.setCellStyle(style14);
		celda.setCellValue("N° RESERVA: ");
		renglones.add(fila.createCell(3));
		celda = fila.createCell(9);
		celda.setCellStyle(style14);
		celda.setCellValue("MONTO: ");
		celda = fila.createCell(10);
		// celda.setCellValue("$" + total);
		renglones.add(celda);
		fila = hojaExcel.createRow(filaImagenesFin + 5);
		renglones.add(fila.createCell(5));
		renglones.add(fila.createCell(6));
		fila = hojaExcel.createRow(filaImagenesFin + 6);
		celda = fila.createCell(5);
		celda.setCellValue("SOLICITANTE");
		fila = hojaExcel.createRow(filaImagenesFin + 14);
		renglones.add(fila.createCell(2));
		renglones.add(fila.createCell(3));
		renglones.add(fila.createCell(9));
		renglones.add(fila.createCell(10));
		fila = hojaExcel.createRow(filaImagenesFin + 15);
		celda = fila.createCell(2);
		celda.setCellStyle(negritas);
		celda.setCellValue("SUP: OMIMSA");
		celda = fila.createCell(9);
		celda.setCellValue("SUPERINTENDENTE");
		celda.setCellStyle(negritas);
		// agregar renglones
		CellStyle styleLineas = libroExcel.createCellStyle();
		styleLineas.setBorderBottom(BorderStyle.THIN);
		for (HSSFCell celdaRenglon : renglones) {
			celdaRenglon.setCellStyle(styleLineas);
		}
		short columnasAutoajustar[] = { 1, 2, 3, 4, 8, 10 };
		for (short columnaAjustar : columnasAutoajustar) {
			hojaExcel.autoSizeColumn((short) columnaAjustar);
		}
		hojaExcel.setColumnWidth(0, 4500);
		hojaExcel.setColumnWidth(9, 2800);

		filaImagenesInicio = 4 + datosPartes.length;
		filaImagenesFin = filaImagenesInicio;

		if (imagenes) {
			for (BacklogsMineros backlog : listaBacklogs) {
				List<ImagenesBacklogsMineros> imagenesBacklog = imagenesBacklogsMinerosFacade
						.obtenerImagenesBacklogsMinerosPorID(backlog.getBacklogsMinerosKey());

				int columnaImagenInicio = 0;
				int columnaImagenFin = 2;
				if (imagenesBacklog.size() > 0) {
					filaImagenesFin += 7;
					for (ImagenesBacklogsMineros imagen : imagenesBacklog) {
						String ruta = imagen.getImagenesBacklogsMinerosKey().getImagenBacklog();
						String extension = FilenameUtils.getExtension(ruta);
						String rutaEscala = scaledImage(ruta, 151, 113, extension);
						FileInputStream stream = new FileInputStream(rutaEscala);
						byte[] bytes = IOUtils.toByteArray(stream);
						final HSSFPatriarch drawing = hojaExcel.createDrawingPatriarch();
						final ClientAnchor anchor = new HSSFClientAnchor();
						int pictureIndex;
						if (extension.equals("png")) {
							pictureIndex = libroExcel.addPicture(bytes, Workbook.PICTURE_TYPE_PNG);
						} else {
							pictureIndex = libroExcel.addPicture(bytes, Workbook.PICTURE_TYPE_JPEG);
						}
						anchor.setCol1(columnaImagenInicio);
						anchor.setCol2(columnaImagenFin);
						anchor.setRow1(filaImagenesInicio);
						anchor.setRow2(filaImagenesFin);
						final HSSFPicture picture = drawing.createPicture(anchor, pictureIndex);
						picture.resize();
						columnaImagenInicio += 2;
						columnaImagenFin += 2;
					}
					filaImagenesInicio += 8;
				}

			}
		}
	}

	/***
	 * genera excel de reporte de servicio
	 * 
	 * @param reporte "Servicio" para reporte de servicio, "Solicitud" para
	 *                solicitud de autorización (carga rápida)
	 * @return
	 * @throws Exception
	 */
	public StreamedContent generarExcelReporte(String reporte) throws Exception {
		int numeroHoja = 0;
		String excelFileName = reporte;
		List<String> numerosDeSerie = new ArrayList<String>();
		for (BacklogsMineros backlogMinero : listaBacklogs) {
			String serie = backlogMinero.getNumeroSerie();
			if (numerosDeSerie.contains(serie)) {

			} else {
				numerosDeSerie.add(serie);
				excelFileName += " " + serie;
			}
		}
		for (String serie : numerosDeSerie) {
			List<BacklogsMineros> backlogsSerie = new ArrayList<BacklogsMineros>();
			for (BacklogsMineros backlogMinero : listaBacklogs) {
				String serieBackog = backlogMinero.getNumeroSerie();
				if (serie.equals(serieBackog)) {
					backlogsSerie.add(backlogMinero);
				}
			}
			switch (reporte) {
			case "Servicio":
				obtenerHojaReporteServicio(backlogsSerie);
				break;
			case "Solicitud":// con imagenes
				obtenerHojaSolicitud(backlogsSerie, true);
				break;
			case "solicitud":// sin imagenes
				obtenerHojaSolicitud(backlogsSerie, false);
				break;
			}

			backlogsSerie.clear();
			libroExcel.setSheetName(numeroHoja, serie);
			numeroHoja++;
		}
		FileOutputStream fos = new FileOutputStream(excelFileName);
		libroExcel.write(fos);
		fos.flush();
		libroExcel.close();
		fos.close();
		InputStream stream = new BufferedInputStream(new FileInputStream(excelFileName));
		StreamedContent exportFile = new DefaultStreamedContent(stream,
				"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", excelFileName + ".xls");

		return exportFile;
	}

	/***
	 * Cambia el tamaño de una imagen y lo guarda en un nuevo archivo
	 * 
	 * @param rutaOrigen ruta de la imagen original
	 * @param width      ancho deseado
	 * @param height     alto deseado
	 * @param formato    extension del nuevo archivo
	 * @return ruta destino
	 */
	private String scaledImage(String rutaOrigen, int width, int height, String formato) {
		BufferedImage originalImage;
		String rutaDestino = rutaOrigen;
		try {
			originalImage = ImageIO.read(new File(rutaOrigen));
			BufferedImage scaledImg = Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT,
					width, height, Scalr.OP_ANTIALIAS);
			rutaDestino = rutaOrigen.replace(".", " escala.");
			// System.out.println(rutaDestino);
			ImageIO.write(scaledImg, formato, new File(rutaDestino));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return rutaDestino;
	}

}