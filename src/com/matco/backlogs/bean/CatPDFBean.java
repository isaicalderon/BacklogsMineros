package com.matco.backlogs.bean;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Base64;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

import com.matco.amce3.dto.InspeccionDto;
import com.matco.backlogs.catInspecciones.ClienteCatInspeccion;
import com.matco.backlogs.dto.InspeccionCatDto;
import com.matco.interfacescatinspect.entity.Report;
import com.matco.interfacescatinspect.entity.ReportIdentity;
import com.matco.interfacescatinspect.entity.RetrieveReportRequest;
import com.matco.interfacescatinspect.entity.RetrieveReportResponse;

@ManagedBean(name = "catPDFBean")
@RequestScoped
public class CatPDFBean extends GenericInspectBean implements Serializable {
	
	private static final long serialVersionUID = 1818288225038067984L;
	private static final Logger log = Logger.getLogger(CatPDFBean.class);
	private BacklogsStaticsVarBean seleccionBean;

	private ClienteCatInspeccion clienteCatInspeccion;
	private String mensaje;
	private StreamedContent streamedContent;
	private InspeccionDto inspeccionDto;

	@PostConstruct
	public void init() {
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		if (seleccionBean.getInspeccionesSeleccionadas().isEmpty()) {
			return;
		}
		Integer inspeccionEnProceso = seleccionBean.getInspeccionEnProceso();
		InspeccionCatDto inspeccionCatDto = seleccionBean.getInspeccionesSeleccionadas().get(inspeccionEnProceso);
		InspeccionDto inspeccionDto = inspeccionCatDto.getInspeccionDto();
		setInspeccionDto(inspeccionDto);
	}

	private void cargarInspeccionActual() {
		if (seleccionBean.getInspeccionesSeleccionadas().isEmpty()) {
			return;
		}
		//if (clienteCatInspeccion != null) {
			clienteCatInspeccion = new ClienteCatInspeccion(RUTA_PROPERTIES_CATINSPECT);
			RetrieveReportRequest reporteInspeccion = new RetrieveReportRequest();
			reporteInspeccion.setInspectionNumber(getInspeccionDto().getFolio());
			reporteInspeccion.setReportName(PDF_TYPE);
			RetrieveReportResponse contenidoPDF = clienteCatInspeccion.obtenerPDF(reporteInspeccion);
			cargarPDF(contenidoPDF);
		//}
	}

	private void cargarPDF(RetrieveReportResponse contenidoPDF) {
		Report report = contenidoPDF.getReport();
		String contenido = report.getContent();
		if (contenidoPDF == null || contenido == null || contenido.isEmpty() || contenido.trim().length() == 0) {
			mensaje = "No se ha podido obtener el pdf.";
			agregarMensajeError(mensaje);
			streamedContent = new DefaultStreamedContent();
			return;
		}
		ReportIdentity reportIdentity = contenidoPDF.getReportIdentity();
		String reportName = reportIdentity.getReportName();
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
		seleccionBean.setLecturaPDF(false);
		// targetStream.close();
	}

	public StreamedContent getStreamedContent() {
		cargarInspeccionActual();
		return streamedContent;
	}

	public void setStreamedContent(StreamedContent streamedContent) {
		this.streamedContent = streamedContent;
	}

	public InspeccionDto getInspeccionDto() {
		return inspeccionDto;
	}

	public void setInspeccionDto(InspeccionDto inspeccionDto) {
		this.inspeccionDto = inspeccionDto;
	}

}
