package com.matco.backlogs.dto;

import org.primefaces.model.StreamedContent;

public class ImagenBlDto {
	private String nombre;
	private StreamedContent streamedContent;
	private String rutaImagen;
	private String rutaImagenTemp;
	private String mimeType;
	private String rutaImagenReal;
	private String rutaBase64;
	private boolean existe;
	private boolean base64 = false;
	
	public String getRutaEditar() {
		if (rutaImagenTemp == null) {
			return rutaImagen;
		}
		return rutaImagenTemp;
	}
	public String getRutaImagenReal() {
		return rutaImagenReal;
	}
	public void setRutaImagenReal(String rutaImagenReal) {
		this.rutaImagenReal = rutaImagenReal;
	}
	public boolean isExiste() {
		return existe;
	}
	public void setExiste(boolean existe) {
		this.existe = existe;
	}

	public String getRutaImagenTemp() {
		return rutaImagenTemp;
	}
	public void setRutaImagenTemp(String rutaImagenTemp) {
		this.rutaImagenTemp = rutaImagenTemp;
	}
	public String getMimeType() {
		return mimeType;
	}
	public void setMimeType(String mimeType) {
		this.mimeType = mimeType;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public StreamedContent getStreamedContent() {
		return streamedContent;
	}
	public void setStreamedContent(StreamedContent streamedContent) {
		this.streamedContent = streamedContent;
	}
	public String getRutaImagen() {
		return rutaImagen;
	}
	public void setRutaImagen(String rutaImagen) {
		this.rutaImagen = rutaImagen;
	}
	public String getRutaBase64() {
		return rutaBase64;
	}
	public void setRutaBase64(String rutaBase64) {
		this.rutaBase64 = rutaBase64;
	}
	public boolean isBase64() {
		return base64;
	}
	public void setBase64(boolean base64) {
		this.base64 = base64;
	}
	
}
