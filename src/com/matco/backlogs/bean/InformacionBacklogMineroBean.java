package com.matco.backlogs.bean;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.log4j.Logger;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.ImagenesBacklogsMinerosFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.ImagenesBacklogsMineros;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.LugaresOrigenBacklogsMineros;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;

/***
 * Mostrar información del backlog seleccionado
 * 
 * @author N soluciones software
 *
 */
@ManagedBean(name = "informacionBacklogsMinerosBean")
@ViewScoped
public class InformacionBacklogMineroBean extends GenericBacklogBean implements Serializable {
	
	private static final long serialVersionUID = -995540175787221186L;
	private static final Logger log = Logger.getLogger(InformacionBacklogMineroBean.class);
	private static final NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
	private List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();
	List<String> listaImagenes = new ArrayList<>();
	private LoginBean loginBean = this.obtenerBean("loginBean");
	private BacklogsStaticsVarBean seleccionBean;
	private BacklogsMineros backlogMineroSeleccionado;
	BacklogsMinerosKey backlogMineroKey;
	private boolean renderGaleria = true;
	private boolean success = true;
	private int horometro;
	private Double horasMaquina;
	private Double horasHombre;
	private Double totalRefacciones;
	private String folio;
	private String serie;
	private String origen;
	private String sistema;
	private String generacion;
	private String tipoTrabajo;
	private String prioridad;
	private String codigoTrabajo;
	private String sintomas;
	private String cargo;
	private String notas;
	private String requerido;
	private String aprobado;
	private String otrosMateriales;
	private String numeroReserva;
	private String error;
	private String mostrarGaleria = "display:none;";
	String usuario;
	private Sintomas sintomasObject;
	private LadoComponente ladoComponente = new LadoComponente();
	private List<LadoComponente> ladoComponenteList = new ArrayList<>();
	private List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<>();
	
	@PostConstruct
	public void init() {
		seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		if (seleccionBean != null) {
			// Por si se meten directamente sin seleccionar a un backlog minero en el grid
			backlogMineroSeleccionado = seleccionBean.getBacklogsMinerosSeleccionado() != null
					? seleccionBean.getBacklogsMinerosSeleccionado()
					: null;
			usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
		}

		if (backlogMineroSeleccionado == null) {
			return;
		}

		backlogMineroKey = backlogMineroSeleccionado.getBacklogsMinerosKey();
		folio = backlogMineroKey.getBacklogMineroAlmacenFormateado();
		horometro = backlogMineroSeleccionado.getHorometro();
		serie = backlogMineroSeleccionado.getNumeroSerie();
		origen = obtenerOrigen();
		generacion = obtenerGeneracion();
		prioridad = obtenerPrioridad();
		sistema = obtenerSistema();
		
		codigoTrabajo = obtenerCodigoTrabajo();
		
		sintomasObject = backlogMineroSeleccionado.getSintoma();
		
		if (sintomasObject.getIdCodigoSintoma() == null) {
			sintomasObject.setDescripcionSintoma("");
			sintomasObject.setIdCodigoSintoma("");
		}
		
		sintomas = backlogMineroSeleccionado.getSintomasEquipo();
		horasMaquina = backlogMineroSeleccionado.getHorasMaquinaEstimadas();
		horasHombre = backlogMineroSeleccionado.getHorasHombreEstimadas();
		cargo = obtenerCargo();
		notas = backlogMineroSeleccionado.getComentarioBacklogMinero();
		requerido = backlogMineroSeleccionado.getSolicitadoPor();
		aprobado = backlogMineroSeleccionado.getAprobadoPor();
		tipoTrabajo = backlogMineroSeleccionado.getTipoTrabajo();
		otrosMateriales = backlogMineroSeleccionado.getOtrosMateriales();
		numeroReserva = backlogMineroSeleccionado.getNumeroReserva();
		
		riesgosTabajosList = obtenerRiesgosTrabajo();
		
		// buscar lado del componente
		String codigoLDBL = backlogMineroSeleccionado.getLadoComponenteOb().getCodigoLDC();
		String descripcionLDBL = backlogMineroSeleccionado.getLadoComponenteOb().getDescripcion();
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
		}else {
			ladoComponente.setCodigoLDC("");
			ladoComponente.setDescripcion(descripcionLDBL);
		}
		backlogsMinerosDetalleRefaList = obtenerPartes();
		generarSubtotalRefacciones(backlogsMinerosDetalleRefaList);
		totalRefacciones = calcularTotalRefacciones();

		// Se limpia el backlog minero seleccionado de la sesión.
		// seleccionBean.setBacklogsMinerosSeleccionado(null);
	}
	
	public List<RiesgosTrabajo> obtenerRiesgosTrabajo() {
		SintomasYRiesgosFacade riesgosFacade = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
		RiesgosTrabajo riesgo = new RiesgosTrabajo();
		int idBacklog = backlogMineroSeleccionado.getBacklogsMinerosKey().getIdBacklogMinero();
		int idAlmacen = (int) backlogMineroSeleccionado.getBacklogsMinerosKey().getIdAlmacen().getAlmacen();
		riesgo.setIdBacklog(idBacklog);
		riesgo.setIdAlmacen(idAlmacen);
		try {
			riesgosTabajosList = riesgosFacade.getRiesgosDeunBacklog(riesgo);
		} catch (Exception e2) {
			String error = "No se pudieron obtener los riesgos";
			this.agregarMensajeError(error);
			log.error(e2);
		}
		return riesgosTabajosList;
	}

	public String obtenerSistema() {
		CodigosSistemas idCodigoSistema = backlogMineroSeleccionado.getIdCodigoSistema();
		String codigoSistema = idCodigoSistema.getCodigosSistemasFormateado();
		String descripcion = idCodigoSistema.getDescripcionCodigoSistema();
		String sistema = codigoSistema + " - " + descripcion;
		return sistema;
	}

	public String obtenerOrigen() {
		OrigenesBacklogsMineros origenes = backlogMineroSeleccionado.getOrigenesBacklogsMineros();
		String origen = origenes.getDescripcionOrigen();
		return origen;
	}

	public String obtenerGeneracion() {
		LugaresOrigenBacklogsMineros generacion = backlogMineroSeleccionado.getLugaresOrigenesBacklogsMineros();
		String lugar = generacion.getDescripcionLugarOrigen();
		return lugar;
	}

	public String obtenerPrioridad() {
		PrioridadesBacklogsMineros prioridades = backlogMineroSeleccionado.getIdPrioridadBacklog();
		String prioridad = prioridades.getDescripcionPrioridadBacklog();
		return prioridad;
	}

	public String obtenerCargo() {
		CargosTrabajo cargos = backlogMineroSeleccionado.getIdCargoTrabajo();
		String cargo = cargos.getDescripcionCargoTrabajo();
		return cargo;
	}

	public String obtenerCodigoTrabajo() {
		CodigosSMCS codigoTrabajo = backlogMineroSeleccionado.getIdCodigoSMCS();
		String codigo = codigoTrabajo.getCodigosSMCSFormateado();
		String descripcion = codigoTrabajo.getDescripcionSMCS();
		String codigoTrabajoFormato = codigo + " - " + descripcion;
		return codigoTrabajoFormato;
	}

	public List<BacklogsMinerosDetalleRefa> obtenerPartes() {
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
		return backlogsMinerosDetalleRefaList;
	}

	/***
	 * asigna las rutas con el servlet de las imagenes para mostrarlas en la
	 * galleria
	 * 
	 * @throws Exception
	 */
	public void listarImagenes() throws Exception {

		if (listaImagenes.size() == 0) {
			List<String> listaRutas = new ArrayList<>();
			ImagenesBacklogsMinerosFacade imagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(
					RUTA_PROPERTIES_AMCE3);

			List<ImagenesBacklogsMineros> listaImagenes = imagenesBacklogsMinerosFacade
					.obtenerImagenesBacklogsMinerosPorID(backlogMineroSeleccionado.getBacklogsMinerosKey());

			for (ImagenesBacklogsMineros imagen : listaImagenes) {
				String ruta = imagen.getImagenesBacklogsMinerosKey().getImagenBacklog();
				String rutaCompleta = "http://" + getServerUrlServices()
						+ getExternalContext().getInitParameter("servletImagenes") + ruta.replaceAll("/", "%2F");

				listaRutas.add(rutaCompleta);
			}
			this.listaImagenes = listaRutas;
			if (listaRutas.size() == 0) {
				renderGaleria = false;
				agregarMensajeWarn("Backlogs Mineros", "No se encontraron imagenes de este backlog");
			}
		}
		mostrarGaleria = mostrarGaleria.equals("") ? "display:none;" : "";

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
	 * Obtiene el subtotal de todas las refacciones y las guarda en una variable
	 * llamada total
	 * 
	 * @return double
	 */
	public Double calcularTotalRefacciones() {
		double total = 0;
		for (BacklogsMinerosDetalleRefa refaccionList : backlogsMinerosDetalleRefaList) {
			total += refaccionList.getSubTotal();
		}
		return total;
	}

	/**
	 * Obtiene el total de las refacciones y lo formateoa
	 * 
	 * @return total refacciones formateado
	 */
	public String getTotalFormateado() {
		String totalFormateado = "";
		if (this.totalRefacciones != null) {
			totalFormateado = numberFormat.format(this.totalRefacciones);
		}
		return totalFormateado;
	}

	public Double getTotalRefacciones() {
		return totalRefacciones;
	}

	public void setTotalRefacciones(Double totalRefacciones) {
		this.totalRefacciones = totalRefacciones;
	}

	public int getHorometro() {
		return horometro;
	}

	public String getPrioridad() {
		return prioridad;
	}

	public void setPrioridad(String prioridad) {
		this.prioridad = prioridad;
	}

	public List<BacklogsMinerosDetalleRefa> getBacklogsMinerosDetalleRefaList() {
		return backlogsMinerosDetalleRefaList;
	}

	public void setBacklogsMinerosDetalleRefaList(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		this.backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaList;
	}

	public LoginBean getLoginBean() {
		return loginBean;
	}

	public void setLoginBean(LoginBean loginBean) {
		this.loginBean = loginBean;
	}

	public BacklogsStaticsVarBean getSeleccionBean() {
		return seleccionBean;
	}

	public void setSeleccionBean(BacklogsStaticsVarBean seleccionBean) {
		this.seleccionBean = seleccionBean;
	}

	public BacklogsMineros getBacklogMineroSeleccionado() {
		return backlogMineroSeleccionado;
	}

	public void setBacklogMineroSeleccionado(BacklogsMineros backlogMineroSeleccionado) {
		this.backlogMineroSeleccionado = backlogMineroSeleccionado;
	}

	public String getNumeroReserva() {
		return numeroReserva;
	}

	public void setNumeroReserva(String numeroReserva) {
		this.numeroReserva = numeroReserva;
	}

	public String getSerie() {
		return serie;
	}

	public void setSerie(String serie) {
		this.serie = serie;
	}

	public String getOrigen() {
		return origen;
	}

	public void setOrigen(String origen) {
		this.origen = origen;
	}

	public String getSistema() {
		return sistema;
	}

	public void setSistema(String sistema) {
		this.sistema = sistema;
	}

	public String getGeneracion() {
		return generacion;
	}

	public void setGeneracion(String generacion) {
		this.generacion = generacion;
	}

	public LadoComponente getLadoComponente() {
		return ladoComponente;
	}

	public void setLadoComponente(LadoComponente ladoComponente) {
		this.ladoComponente = ladoComponente;
	}

	public String getTipoTrabajo() {
		return tipoTrabajo;
	}

	public void setTipoTrabajo(String tipoTrabajo) {
		this.tipoTrabajo = tipoTrabajo;
	}

	public String getCodigoTrabajo() {
		return codigoTrabajo;
	}

	public void setCodigoTrabajo(String codigoTrabajo) {
		this.codigoTrabajo = codigoTrabajo;
	}

	public String getSintomas() {
		return sintomas;
	}

	public void setSintomas(String sintomas) {
		this.sintomas = sintomas;
	}

	public Double getHorasMaquina() {
		return horasMaquina;
	}

	public void setHorasMaquina(Double horasMaquina) {
		this.horasMaquina = horasMaquina;
	}

	public Double getHorasHombre() {
		return horasHombre;
	}

	public void setHorasHombre(Double horasHombre) {
		this.horasHombre = horasHombre;
	}

	public String getCargo() {
		return cargo;
	}

	public void setCargo(String cargo) {
		this.cargo = cargo;
	}

	public String getNotas() {
		return notas;
	}

	public void setNotas(String notas) {
		this.notas = notas;
	}

	public String getRequerido() {
		return requerido;
	}

	public void setRequerido(String requerido) {
		this.requerido = requerido;
	}

	public String getAprobado() {
		return aprobado;
	}

	public void setAprobado(String aprobado) {
		this.aprobado = aprobado;
	}

	public String getOtrosMateriales() {
		return otrosMateriales;
	}

	public void setOtrosMateriales(String otrosMateriales) {
		this.otrosMateriales = otrosMateriales;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getError() {
		return error;
	}

	public void setError(String error) {
		this.error = error;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public String getUsuario() {
		return usuario;
	}

	public void setUsuario(String usuario) {
		this.usuario = usuario;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public static Logger getLog() {
		return log;
	}

	public void setHorometro(int horometro) {
		this.horometro = horometro;
	}

	public String getMostrarGaleria() {
		return mostrarGaleria;
	}

	public void setMostrarGaleria(String mostrarGaleria) {
		this.mostrarGaleria = mostrarGaleria;
	}

	public boolean isRenderGaleria() {
		return renderGaleria;
	}

	public void setRenderGaleria(boolean renderGaleria) {
		this.renderGaleria = renderGaleria;
	}

	public List<String> getListaImagenes() {
		return listaImagenes;
	}

	public void setListaImagenes(List<String> listaImagenes) {
		this.listaImagenes = listaImagenes;
	}

	public String getFolio() {
		return folio;
	}

	public void setFolio(String folio) {
		this.folio = folio;
	}

	public List<LadoComponente> getLadoComponenteList() {
		return ladoComponenteList;
	}

	public void setLadoComponenteList(List<LadoComponente> ladoComponenteList) {
		this.ladoComponenteList = ladoComponenteList;
	}

	public List<RiesgosTrabajo> getRiesgosTabajosList() {
		return riesgosTabajosList;
	}

	public void setRiesgosTabajosList(List<RiesgosTrabajo> riesgosTabajosList) {
		this.riesgosTabajosList = riesgosTabajosList;
	}

	public Sintomas getSintomasObject() {
		return sintomasObject;
	}

	public void setSintomasObject(Sintomas sintomasObject) {
		this.sintomasObject = sintomasObject;
	}
	
	

}
