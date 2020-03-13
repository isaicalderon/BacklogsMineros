package com.matco.backlogs.bean;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import com.matco.amce3.facade.CartasServicioFacade;
import com.matco.backlogs.entity.CartasServicio;
import com.matco.ejes.entity.Marca;
import com.matco.ejes.entity.Modelo;

@ManagedBean(name = "administradorCartasServicioBean")
@ViewScoped
public class AdministradorCartasServicioBean extends GenericBacklogBean implements Serializable {
	private static final long serialVersionUID = 388423039343668162L;
	private static final Logger log = Logger.getLogger(AdministradorCartasServicioBean.class);
	private String rutaArchivosTemporal = getExternalContext().getInitParameter("rutaCartasServicio");
	private static final int DEFAULT_BUFFER_SIZE = 10240; // 10KB.
	private List<CartasServicio> cartasServicioList;
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private Modelo idModelo;
	private Marca idMarca;
	private InputStream file;
	private String cartaServicio;

	private String ruta;
	private String descripcion;
	private String serie;
	private String rutaCompleta;
	private String accion;
	private boolean habilitaModelo = false;
	private boolean habilitaNumSerie = false;
	private boolean habilitaMarca = false;
	private boolean metioModelo = false;
	private boolean metioSerie = false;
	private boolean consultaExitosa = true;
	private boolean seGuarda = false;

	private String error;
	private String summary = "Cartas Servicio";

	@PostConstruct
	public void init() {

	}

	public void cartaSeleccionada() {
		consultaExitosa = false;
	}

	public void consultarCarta() throws IOException {
		HttpServletResponse response = (HttpServletResponse) getExternalContext().getResponse();

		File file = new File(cartaServicio);
		BufferedInputStream input = null;
		BufferedOutputStream output = null;

		try {

			input = new BufferedInputStream(new FileInputStream(file), DEFAULT_BUFFER_SIZE);

			response.reset();
			response.setContentType("application/pdf");
			response.setContentLength((int) file.length());
			response.setHeader("Content-disposition", "inline; filename=\"" + file.getName() + "\"");
			output = new BufferedOutputStream(response.getOutputStream(), DEFAULT_BUFFER_SIZE);

			byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
			int length;
			while ((length = input.read(buffer)) > 0) {
				output.write(buffer, 0, length);
			}

			output.flush();
		} finally {
			close(output);
			close(input);
		}

		facesContext.responseComplete();

	}

	// Helpers (can be refactored to public utility class)
	// ----------------------------------------

	private static void close(Closeable resource) {
		if (resource != null) {
			try {
				resource.close();
			} catch (IOException e) {
				// Do your thing with the exception. Print it, log it or mail it. It may be
				// useful to
				// know that this will generally only be thrown when the client aborted the
				// download.
				e.printStackTrace();
			}
		}
	}

	public void upload(FileUploadEvent event) {
		try {
			if (event.getFile() == null) {
				error = "El archivo no es válido";
				agregarMensajeWarn(summary, error);
			} else {
				String fileName = event.getFile().getFileName();
				String ext = fileName.substring(fileName.lastIndexOf('.'), fileName.length());
				if (ext.equals(".pdf")) {
					setFile(event.getFile().getInputstream());
					setRuta(fileName);
					setRutaCompleta(rutaArchivosTemporal + fileName);
				} else {
					agregarMensajeError(summary, "El archivo debe de ser PDF.");
					setFile(null);
					setRuta("");
					setRutaCompleta("");
				}
			}
			event.getFile().getInputstream().close();
		} catch (Exception e) {
		}
	}

	public boolean copiarArchivo(String destination, String fileName, InputStream in) throws Exception {
		boolean copiaCorrecta = true;
		OutputStream out = null;
		try {
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

	public void verCartas() {
		boolean metioModelo = isMetioModelo();
		boolean metioSerie = isMetioSerie();
		cartasServicioList = new ArrayList<>();
		if (metioModelo == false && metioSerie == false) {
			agregarMensajeWarn(summary, "Se debe especificar el modelo o serie.");
			setAccion("");
		} else {
			consultaExitosa = true;
			CartasServicioFacade cartasServicioFacade = new CartasServicioFacade(RUTA_PROPERTIES_AMCE3);
			List<CartasServicio> result = new ArrayList<>();
			if (metioModelo == true) {
				String modelo = getIdModelo().getModeloKey().getModelo();
				try {
					result = cartasServicioFacade.obtenerCartasServicioPorModelo(modelo);
					Comparator<CartasServicio> comp = (CartasServicio a, CartasServicio b) -> {
						Date fechaA = a.getFechaRegistroCartaServicio() != null ? a.getFechaRegistroCartaServicio()
								: new Date();
						Date fechaB = b.getFechaRegistroCartaServicio() != null ? b.getFechaRegistroCartaServicio()
								: new Date();
						return fechaB.compareTo(fechaA);
					};
					Collections.sort(result, comp);
					for (CartasServicio cartaa : result) {
						if (cartaa != null) {
							if (cartaa.getArchivoCartaServicio() != null && cartaa.getSerieAplica() != null
									&& cartaa.getIdCartaServicio() != null && cartaa.getIdMarca() != null
									&& cartaa.getModeloAplica() != null) {
								File file = new File(cartaa.getArchivoCartaServicio());
								if (file.exists()) {
									cartasServicioList.add(cartaa);
								}
							}
						}
					}
				} catch (Exception e) {
					error = "No se pudieron listar las cartas servicio por modelo";
					log.error(error, e);
					agregarMensajeError(summary, error);
				}
				if (getCartasServicioList().isEmpty()) {
					agregarMensajeWarn(summary, "No existen cartas servicio para este modelo.");
				} else {
					PrimeFaces.current().executeScript("PF('dialogoMostrarCartas').show();");
				}
			} else {
				try {
					result = cartasServicioFacade.obtenerCartasServicioPorSerie(serie);
					Comparator<CartasServicio> comp = (CartasServicio a, CartasServicio b) -> {
						Date fechaA = a.getFechaRegistroCartaServicio() != null ? a.getFechaRegistroCartaServicio()
								: new Date();
						Date fechaB = b.getFechaRegistroCartaServicio() != null ? b.getFechaRegistroCartaServicio()
								: new Date();
						return fechaB.compareTo(fechaA);
					};
					Collections.sort(result, comp);
					for (CartasServicio cartaa : result) {
						if (cartaa != null) {
							if (cartaa.getArchivoCartaServicio() != null && cartaa.getSerieAplica() != null
									&& cartaa.getIdCartaServicio() != null && cartaa.getIdMarca() != null
									&& cartaa.getModeloAplica() != null) {
								cartasServicioList.add(cartaa);
							}
						}
					}
				} catch (Exception e) {
					error = "No se pudieron listar las cartas servicio por número de serie.";
					log.error(error, e);
					agregarMensajeError(summary, error);
				}

				if (getCartasServicioList().isEmpty()) {
					agregarMensajeWarn(summary, "No existen cartas servicio para este número de serie.");
				} else {
					PrimeFaces.current().executeScript("PF('dialogoMostrarCartas').show();");
				}
			}

		}
	}

	public void subirCartaServicio() {
		CartasServicioFacade cartasServicioFacade = new CartasServicioFacade(RUTA_PROPERTIES_AMCE3);
		CartasServicio nuevo = new CartasServicio();
		seGuarda = false;
		if (serie == null || idModelo == null || idMarca == null || ruta == null) {
			if(serie == null) {
				agregarMensajeWarn(summary, "El campo 'Número de serie' está vacío y se requiere para continuar");
			}
			if(idModelo == null) {
				agregarMensajeWarn(summary, "El campo 'Número de serie' está vacío y se requiere para continuar");
			}
			if(idMarca == null) {
				agregarMensajeWarn(summary, "El campo 'Número de serie' está vacío y se requiere para continuar");
			}
			if(ruta == null) {
				agregarMensajeWarn(summary, "No se ha subido ningún archivo y se requiere para continuar");
			}
		} else {
			nuevo.setArchivoCartaServicio(rutaCompleta);
			nuevo.setDescripcionCartasServicio(descripcion);
			nuevo.setSerieAplica(serie);
			nuevo.setModeloAplica(idModelo);
			nuevo.setIdMarca(idMarca);
			try {
				cartasServicioFacade.guardarCartasServicio(nuevo);
				if (copiarArchivo(getRutaArchivosTemporal(), getRuta(), getFile()) == true) {
					agregarMensajeInfo(summary, "Se ha agregado la carta servicio " + getRuta());
					PrimeFaces.current().executeScript("PF('dialogoSubirCarta').hide();");
					seleccionBean.setIdMarca(null);
				} else {
					error = "No ha podido cargar en el disco la carta servicio: " + getRuta();
					agregarMensajeError(summary, error);
				}
				limpiarCampos();
			} catch (Exception e) {
				error = "No ha podido agregar la carta servicio: " + getRuta();
				log.error(error, e);
				agregarMensajeError(summary, error);
				limpiarCampos();
			}
		}

	}

	public void limpiarCampos() {
		setRuta("");
		setDescripcion("");
		setSerie("");
		setIdMarca(null);
		setIdModelo(null);
		setHabilitaModelo(false);
		setHabilitaNumSerie(false);
		setHabilitaMarca(false);
		seleccionBean.setIdMarca(null);
	}

	public void deshabilitarControlesBusquedaModeloConsultarCarta() {
		setHabilitaModelo(true);
		setHabilitaNumSerie(true);
		setMetioModelo(true);
	}

	public void habilitarControlesBusquedaModeloConsultarCarta() {
		setHabilitaModelo(false);
		setHabilitaNumSerie(false);
		setMetioModelo(false);
		setIdModelo(null);
	}

	public void deshabilitarControlesBusquedaNumeroSerieConsultarCarta() {
		setHabilitaNumSerie(true);
		setHabilitaModelo(true);
		setMetioSerie(true);
	}

	public void habilitarControlesBusquedaNumeroSerieConsultarCarta() {
		setHabilitaNumSerie(false);
		setHabilitaModelo(false);
		setMetioSerie(false);
		setSerie("");
	}

	public void deshabilitarControlesBusquedaModelo() {
		setHabilitaModelo(true);
	}

	public void habilitarControlesBusquedaModelo() {
		setHabilitaModelo(false);
		setIdModelo(null);
	}

	public void deshabilitarControlesBusquedaNumeroSerie() {
		setHabilitaNumSerie(true);
	}

	public void habilitarControlesBusquedaNumeroSerie() {
		setHabilitaNumSerie(false);
		setSerie("");
	}

	public void deshabilitarControlesBusquedaMarca() {
		setHabilitaMarca(true);
		seleccionBean.setIdMarca(idMarca);
	}

	public void habilitarControlesBusquedaMarca() {
		setHabilitaMarca(false);
		setIdMarca(null);
		seleccionBean.setIdMarca(null);
	}

	public List<CartasServicio> getCartasServicioList() {
		return cartasServicioList;
	}

	public void setCartasServicioList(List<CartasServicio> cartasServicioList) {
		this.cartasServicioList = cartasServicioList;
	}

	public Modelo getIdModelo() {
		return idModelo;
	}

	public void setIdModelo(Modelo idModelo) {
		this.idModelo = idModelo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public boolean isHabilitaModelo() {
		return habilitaModelo;
	}

	public void setHabilitaModelo(boolean habilitaModelo) {
		this.habilitaModelo = habilitaModelo;
	}

	public boolean isHabilitaNumSerie() {
		return habilitaNumSerie;
	}

	public void setHabilitaNumSerie(boolean habilitaNumSerie) {
		this.habilitaNumSerie = habilitaNumSerie;
	}

	public String getRutaCompleta() {
		return rutaCompleta;
	}

	public void setRutaCompleta(String rutaCompleta) {
		this.rutaCompleta = rutaCompleta;
	}

	public Marca getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(Marca idMarca) {
		this.idMarca = idMarca;
	}

	public String getRutaArchivosTemporal() {
		return rutaArchivosTemporal;
	}

	public void setRutaArchivosTemporal(String rutaArchivosTemporal) {
		this.rutaArchivosTemporal = rutaArchivosTemporal;
	}

	public InputStream getFile() {
		return file;
	}

	public void setFile(InputStream file) {
		this.file = file;
	}

	public boolean isHabilitaMarca() {
		return habilitaMarca;
	}

	public void setHabilitaMarca(boolean habilitaMarca) {
		this.habilitaMarca = habilitaMarca;
	}

	public String getAccion() {
		return accion;
	}

	public void setAccion(String accion) {
		this.accion = accion;
	}

	public boolean isMetioModelo() {
		return metioModelo;
	}

	public void setMetioModelo(boolean metioModelo) {
		this.metioModelo = metioModelo;
	}

	public boolean isMetioSerie() {
		return metioSerie;
	}

	public void setMetioSerie(boolean metioSerie) {
		this.metioSerie = metioSerie;
	}

	public String getCartaServicio() {
		return cartaServicio;
	}

	public void setCartaServicio(String cartaServicio) {
		this.cartaServicio = cartaServicio;
	}

	public boolean isConsultaExitosa() {
		return consultaExitosa;
	}

	public void setConsultaExitosa(boolean consultaExitosa) {
		this.consultaExitosa = consultaExitosa;
	}

	public boolean isSeGuarda() {
		return seGuarda;
	}

	public void setSeGuarda(boolean seGuarda) {
		this.seGuarda = seGuarda;
	}

}
