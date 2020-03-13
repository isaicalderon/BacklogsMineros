package com.matco.backlogs.bean;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.util.Base64;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.imageio.ImageIO;
import org.apache.log4j.Logger;
import org.imgscalr.Scalr;
import org.primefaces.PrimeFaces;
import org.primefaces.model.DefaultStreamedContent;
import com.nsoluciones.catinspect.facade.*;
import com.nsoluciones.catinspect.entity.CatInspectEntity;
import com.nsoluciones.catinspect.entity.CatInspectQuestionsEntity;
import com.matco.backlogs.catInspecciones.ClienteCatInspeccion;
import com.matco.interfacescatinspect.clientes.GetContentCliente;
import com.matco.interfacescatinspect.entity.Report;
import com.matco.interfacescatinspect.entity.RetrieveReportRequest;
import com.matco.interfacescatinspect.entity.RetrieveReportResponse;
import com.matco.interfacescatinspect.utils.dto.GetContentDto;
import com.nsoluciones.catinspect.entity.CatInspectAttachmentsEntity;

/**
 * Class Bean para obtener y administrar CatInspect Mineros
 * 
 * @author N Solucione de Software
 *
 */
@ManagedBean(name = "catInspectMinerosBean")
@ViewScoped
public class CatInspectMinerosBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = 2850444434469177715L;
	private static final Logger log = Logger.getLogger(CatInspectMinerosBean.class);
	private final String PDF_TYPE = "pdforderedbyform";
	//private BacklogsMineros backlogEntity;
	private CatInspectFacade catInspectFacade = new CatInspectFacade(RUTA_PROPERTIES_AMCE3);
	private CatInspectEntity catInspectEntitySeleccionada;
	private CatInspectQuestionsEntity questionEntitySeleccionada;
	private DefaultStreamedContent streamedContent;
	private BacklogsStaticsVarBean seleccionBean;
	private List<CatInspectEntity> inspeccionesList;

	@PostConstruct
	public void init() {
		this.seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		seleccionBean.setBacklogEstandarSelected(null);
		seleccionBean.setInspeccionSeleccionada(null);
		cargarInspecciones();
		//formatearListaInspeccion();
		
	}

	public String hacerInspeccion() {
		if (questionEntitySeleccionada != null) {
			
			if (questionEntitySeleccionada.getImagenes().isEmpty()) {
				obtenerImagenQuestionRedirect(questionEntitySeleccionada);
			}
			
			this.seleccionBean.setInspeccionSeleccionada(questionEntitySeleccionada);
			return "registroInspeccion";
		}
		return " ";
	}

	/**
	 * Carga todas las inspecciones
	 */
	public void cargarInspecciones() {
		try {
			inspeccionesList = catInspectFacade.obtenerTodasInspecciones();
			for (CatInspectEntity catInspectEntity : inspeccionesList) {
				List<CatInspectQuestionsEntity> questInspeccionesList = catInspectFacade
						.obtenerTodasPreguntasByIdInsp(catInspectEntity);
				catInspectEntity.setCantidad(questInspeccionesList.size());
				catInspectEntity.setQuestInspeccionesList(questInspeccionesList);
			}
			
		} catch (Exception e) {
			String error = "No se pudo obtener las inspecciones";
			this.agregarMensajeError(error);
			log.error(e);
		}
	}
	
	/**
	 * Con esta funcion le daremos formato a la lista
	 * - Se borraran de la lista cuyo numero de alertas sea 0
	 * - Solo se mostraran las inspecciones con sucursal 6 0 13
	 */
	public void formatearListaInspeccion() {
		int lenList = inspeccionesList.size();
		CatInspectEntity catEntityTmp;
		for(int i = 0; i < lenList; i++) {
			catEntityTmp = inspeccionesList.get(i);
			if (catEntityTmp.getSucursal() != 6 || catEntityTmp.getSucursal() != 13) {
				inspeccionesList.remove(i);
			}else {
				if (catEntityTmp.getCantidad() == 0) {
					inspeccionesList.remove(i);
				}
			}
		}
	}

	public Date restarMesesFecha() {
		int meses = -3; // conts
		Date fechaActual = new Date();
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fechaActual);
		calendar.add(Calendar.MONTH, meses);
		return calendar.getTime();
	}

//    /**
//     * Separa las inspecciones segun su folio y las agrega a un unico arreglo de
//     * inspecciones
//     */
//    public void separarInspecciones() {
//	int numeroFolioActual = 0;
//	for (CatInspectEntity entity : inspeccionesList) {
//	    int numeroFolio = entity.getFolio();
//	    if (numeroFolio != numeroFolioActual) {
//		numeroFolioActual = numeroFolio;
//		uniqueInspeccionesList.add(entity);
//	    }
//	}
//    }
//
//    /**
//     * Agrega las preguntas ligadas a un Folio de inspección
//     */
//    public void agregarPreguntas() {
//	for (CatInspectEntity entityA : uniqueInspeccionesList) {
//	    for (CatInspectEntity entityB : inspeccionesList) {
//		if (entityA.getFolio() == entityB.getFolio()) {
//		    entityA.setCantidad(entityA.getCantidad() + 1);
//		    CatInspectQuestionsEntity questionsEntity = new CatInspectQuestionsEntity(entityB);
//		    entityA.getQuestInspeccionesList().add(questionsEntity);
//		}
//	    }
//	}
//    }

	/*
	 * public void obtenerImagenes() { List<CatInspectAttachmentsEntity>
	 * attachmentsList; try { for (CatInspectEntity inspectCat :
	 * uniqueInspeccionesList) { for (CatInspectQuestionsEntity questionsCat :
	 * inspectCat.getQuestInspeccionesList()) { attachmentsList =
	 * catInspectFacade.obtenerImagenesByFolio(questionsCat);
	 * questionsCat.setImagenes(attachmentsList); } } } catch (Exception e) {
	 * agregarMensajeError(summary,
	 * "No se pudo obtener las imagenes de algunas inspecciones"); log.error(e); }
	 * // obtener las inspecciones respecto a la sucursal }
	 */
	/**
	 * Obtiene las imagenes de una pregunta cuando se da clic al boton
	 * 
	 * @return realmente no regresa nada
	 */
	public String obtenerImagenQuestion(CatInspectQuestionsEntity questionsCat, int folio) {
		List<CatInspectAttachmentsEntity> attachmentsList;
		PrimeFaces pf = PrimeFaces.current();
		String imgBase64Scaled = null;
		questionsCat.setFolio(folio);
		try {
			attachmentsList = catInspectFacade.obtenerImagenesByFolio(questionsCat);
			if (attachmentsList.size() > 0) {
				// obtenemos a base64
				for (CatInspectAttachmentsEntity img : attachmentsList) {
					img = obtenerBase64(img);
					imgBase64Scaled = scaledImageBase64(img.getImgBase64(), 450, 250, img.getTypeOfMedia());
					img.setImgBase64Scaled(imgBase64Scaled);
				}
				questionsCat.setImagenes(attachmentsList);
				pf.executeScript("PF('dialogoImagenes').show()");
			} else {
				agregarMensajeWarn(summary, "Esta inspección no cuenta con imagenes");
			}
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo obtener las imagenes de la inspección");
			log.error(e);
		}
		return " ";
	}
	
	public void obtenerImagenQuestionRedirect(CatInspectQuestionsEntity questionsCat) {
		List<CatInspectAttachmentsEntity> attachmentsList;
		String imgBase64Scaled = null;
		try {
			attachmentsList = catInspectFacade.obtenerImagenesByFolio(questionsCat);
			if (attachmentsList.size() > 0) {
				// obtenemos a base64
				for (CatInspectAttachmentsEntity img : attachmentsList) {
					img = obtenerBase64(img);
					imgBase64Scaled = scaledImageBase64(img.getImgBase64(), 450, 250, img.getTypeOfMedia());
					img.setImgBase64Scaled(imgBase64Scaled);
				}
				questionsCat.setImagenes(attachmentsList);
			} else {
				//agregarMensajeWarn(summary, "Esta inspección no cuenta con imagenes");
			}
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo obtener las imagenes de la inspección");
			log.error(e);
		}
	}

	public CatInspectAttachmentsEntity obtenerBase64(CatInspectAttachmentsEntity img) {
		try {
			GetContentCliente getContentCliente = new GetContentCliente();
			// String contentId = "3f7ae792-40ec-4f3c-aff1-d365cf8faec7";
			GetContentDto[] obtenerContenido;
			obtenerContenido = getContentCliente.obtenerContenidoInspeccion(img.getIdentifierKey());
			String mimeType = obtenerContenido[0].getMimeType(); // 'image/jpeg'
			String content = obtenerContenido[0].getContent(); // base64
			// String img = "data:"+mimeType+";base64, "+content;
			img.setImgBase64(content);
			img.setTypeOfMedia(mimeType);
			return img;
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo convertir la imagen a base 64");
			log.error(e);
		}
		return img;
	}

	/**
	 * Prepara el PDF de la inspeccion para ser descargado
	 * @return
	 */
	public String prepararPDF() {
		//CatInspectQuestionsEntity question = questionEntitySeleccionada;
		CatInspectEntity catInspect = catInspectEntitySeleccionada;
		
		ClienteCatInspeccion clienteCatInspeccion = new ClienteCatInspeccion(RUTA_PROPERTIES_CATINSPECT);
		RetrieveReportRequest reporteInspeccion = new RetrieveReportRequest();
		reporteInspeccion.setInspectionNumber(catInspect.getFolio() + "");
		reporteInspeccion.setReportName(PDF_TYPE);
		RetrieveReportResponse contenidoPDF = clienteCatInspeccion.obtenerPDF(reporteInspeccion);
		if (contenidoPDF != null) {
			cargarPDF(contenidoPDF);
		}else {
			agregarMensajeWarn(summary, "No se encontró el PDF");
		}
		return "";
	}
	
	private void cargarPDF(RetrieveReportResponse contenidoPDF) {
		Report report = contenidoPDF.getReport();
		String contenido = report.getContent();
		if (contenidoPDF == null || contenido == null || contenido.isEmpty() || contenido.trim().length() == 0) {
			String mensaje = "No se ha podido obtener el pdf.";
			agregarMensajeError(mensaje);
			streamedContent = new DefaultStreamedContent();
			return;
		}
		
		//ReportIdentity reportIdentity = contenidoPDF.getReportIdentity();
		//String reportName = reportIdentity.getReportName();
		String reportName = contenidoPDF.getReportIdentity().getContentID();
		InputStream inputStream = null;
		byte[] arr = Base64.getDecoder().decode(contenido);
		
		inputStream = new ByteArrayInputStream(arr);
		InputStream targetStream = null;
		try {
			targetStream = checkForUtf8BOMAndDiscardIfAny(inputStream);
		} catch (IOException e) {
			log.error(e);
			streamedContent = new DefaultStreamedContent();
			return;
		}
		streamedContent = new DefaultStreamedContent(targetStream, "application/pdf", reportName + ".pdf");
		
		//Files.write(path, lines, cs, options)
		
		
		//seleccionBean.setLecturaPDF(false);
		// targetStream.close();
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
	
	public void scaledImage(String rutaOrigen, String rutaDestino, int width, int height, String formato) {
		BufferedImage originalImage;
		try {
			originalImage = ImageIO.read(new File(rutaOrigen));
			BufferedImage scaledImg = Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC,
					width, height, Scalr.OP_ANTIALIAS);
			ImageIO.write(scaledImg, formato, new File(rutaDestino + "." + formato));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String scaledImageBase64(String base64Origen, int width, int height, String mimeType) {
		BufferedImage originalImage;
		InputStream imageConverter = null;
		String base64Destino = null;

		try {
			byte[] imageTmp = Base64.getDecoder().decode(base64Origen);
			imageConverter = new ByteArrayInputStream(imageTmp);
			originalImage = ImageIO.read(imageConverter);
			// mimeType = FilenameUtils.getExtension(mimeType);
			String[] mimeTypeArray = mimeType.split("/"); // image/jpeg
			mimeType = mimeTypeArray[1];
			BufferedImage scaledImg = Scalr.resize(originalImage, Scalr.Method.ULTRA_QUALITY, Scalr.Mode.AUTOMATIC,
					width, height, Scalr.OP_ANTIALIAS);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			ImageIO.write(scaledImg, mimeType, baos);
			baos.flush();
			byte[] imageInByte = baos.toByteArray();
			base64Destino = Base64.getEncoder().encodeToString(imageInByte);
			return base64Destino;
		} catch (IOException e) {
			e.printStackTrace();
			return base64Origen;
		}
	}

	/***
	 * Obtiene el StyleClass para la columna.
	 * 
	 * -cuadroAlertaColorAmarillo, asigna el color amarillo a la columna
	 * -cuadroAlertaColorRojo, asigna el color rojo a la columna
	 * 
	 * @param prioridad Prioridad de la alerta.
	 * @return color
	 */
	public String color(int prioridad) {
		String color = "";
		switch (prioridad) {
		case 2:
			color = "cuadroAlertaColorAmarillo";
			break;
		case 1:
			color = "cuadroAlertaColorRojo";
			break;
		}
		return color;
	}
	
	/**
	 * Formatea un cliente para el filtro de la columna
	 * @param idCliente
	 * @param razonSocial
	 * @return
	 */
	public String getClienteFormateado(int idCliente, String razonSocial) {
		String clienteFormateado = "";
		if (idCliente == 0) {
			return "";
		}
		
		clienteFormateado = idCliente+" - "+razonSocial;
		return clienteFormateado;
	}

	public List<CatInspectEntity> getInspeccionesList() {
		return inspeccionesList;
	}

	public void setInspeccionesList(List<CatInspectEntity> inspeccionesList) {
		this.inspeccionesList = inspeccionesList;
	}

	public CatInspectEntity getCatInspectEntitySeleccionada() {
		return catInspectEntitySeleccionada;
	}

	public void setCatInspectEntitySeleccionada(CatInspectEntity catInspectEntitySeleccionada) {
		this.catInspectEntitySeleccionada = catInspectEntitySeleccionada;
	}

	public CatInspectQuestionsEntity getQuestionEntitySeleccionada() {
		return questionEntitySeleccionada;
	}

	public void setQuestionEntitySeleccionada(CatInspectQuestionsEntity questionEntitySeleccionada) {
		this.questionEntitySeleccionada = questionEntitySeleccionada;
	}

	public DefaultStreamedContent getStreamedContent() {
		return streamedContent;
	}

	public void setStreamedContent(DefaultStreamedContent streamedContent) {
		this.streamedContent = streamedContent;
	}
	
}
