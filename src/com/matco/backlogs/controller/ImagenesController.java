package com.matco.backlogs.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.PhaseId;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;
import com.matco.backlogs.bean.GenericBean;

@ManagedBean
@RequestScoped
public class ImagenesController extends GenericBean implements Serializable {
	private static final long serialVersionUID = 3809603771077807078L;
	@SuppressWarnings("unused")
	private StreamedContent content;
	private UploadedFile file;

	public StreamedContent getContent() throws FileNotFoundException {
		FacesContext context = FacesContext.getCurrentInstance();
		
		if (context.getCurrentPhaseId() == PhaseId.RENDER_RESPONSE) {
			return new DefaultStreamedContent();
		} else {
			String imageId = getExternalContext().getRequestParameterMap().get("filename");
	        File file = new File(System.getProperty("jboss.server.temp.dir") + File.separator+ imageId);
	        FileInputStream s = new FileInputStream(file);
			return new DefaultStreamedContent(s);
		}
	}

	public void setContent(StreamedContent content) {
		this.content = content;
	}

	public UploadedFile getFile() {
		return file;
	}

	public void setFile(UploadedFile file) {
		this.file = file;
	}
}