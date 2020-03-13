package com.matco.backlogs.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import org.primefaces.PrimeFaces;
import com.matco.amce2.dto.GetMueSosDto;
import com.matco.amce3.dto.MaquinariaDto;
import com.matco.backlogs.dto.InspeccionCatDto;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.EstatusBacklogsMineros;
import com.matco.backlogs.entity.IndicadoresEntity;
import com.matco.backlogs.entity.NumeroParteClientes;
import com.matco.backlogs.entity.OtrosTrabajos;
import com.matco.backlogs.entity.RevisionKardexEntity;
import com.matco.ejes.entity.Marca;
import com.matco.ejes.entity.Modelo;
import com.matco.servicio.entity.Tecnico;
import com.nsoluciones.catinspect.entity.CatInspectQuestionsEntity;
import com.matco.backlogs.entity.BacklogMineroEstandar;

/**
 *
 * @author N Soluciones de Software
 */

@ManagedBean(name = "backlogsStaticsVarBean")
@SessionScoped
public class BacklogsStaticsVarBean extends GenericBean implements Serializable {

	private static final long serialVersionUID = -3890730083875317375L;

	private boolean lecturaPDF = false; // pdf actual de la inspeccion.
	private boolean revisionBacklog = false;
	private boolean dialogGrafica = false;
	private int numeroFolioFiltro;
	private int descripcionFiltro;
	private int prioridadFiltro;
	private int origenFiltro;
	private int auxiliarGrafica = -1;
	
	private int indexTablaBlm;

	// id cliente
	private Integer idClienteMatco = null;

	private Date fechaCreacionFiltro = null;
	private Date fechaCerradoFiltro = null;
	private Integer inspeccionEnProceso = 0; // inspeccion actual
	private Integer alertasTotal = 0; // obtener total de alertas de la inspeccion
	private Integer alerta = 1; // alerta actual de la inspeccion
	private Short sucursalFiltro;
	private String estatusFiltro = null;
	private String folioFiltro = null;
	private String numEconFiltro = null;
	private String serieFiltro = null;
	private String horometroFiltro = null;
	private String diasFiltro = null;
	private String sistemaFiltro = null;
	private String sintomasFiltro = null;
	private String accionFiltro = null;
	private String notasFiltro = null;
	private String partesFiltro = null;
	private String creadoPorFiltro = null;
	private String numeroReserva = null;
	private String redireccionDialogBLEST = "";
	private String graficaSeleccionada = "";
	private String mensajeLoading = "Procesando...";

	// auxiliares para el MenuBackclogs
	private String numeroSerie;
	private String numeroEconomico;
	private String modelo;
	private Date fechaBL1;
	private Date fechaBL2;

	private IndicadoresEntity comentarioSeleccionado = null;
	private CatInspectQuestionsEntity inspeccionSeleccionada;
	private BacklogsMineros backlogsMinerosSeleccionado;
	private BacklogMineroEstandar backlogEstandarSelected;
	private RevisionKardexEntity revisionSeleccionada;
	private Marca idMarca;
	private Tecnico tecnico;
	/***/

	// private SeleccionBean seleccionBean;

	/***/
	private List<EstatusBacklogsMineros> estadosFiltradosList = new ArrayList<>();
	private List<EstatusBacklogsMineros> estatusBacklogsMinerosList = new ArrayList<>();
	private List<BacklogsMineros> backlogsFiltradosList = new ArrayList<>();
	private List<GetMueSosDto> muestrasSeleccionadas = new ArrayList<>();
	private List<InspeccionCatDto> inspeccionesSeleccionadas = new ArrayList<>(); // lista de inspecciones
	private List<CodigosSistemas> codigosSistemasListFiltrado;
	private List<CodigosSMCS> codigosSMCSListFiltrado;
	private List<CargosTrabajo> cargosTrabajoListFiltrado;
	private List<OtrosTrabajos> otrosTrabajosListFiltrado;
	private List<NumeroParteClientes> numeroParteClientesListFiltrado;
	private List<GetMueSosDto> muestrasSOSListFiltrado;
	private List<String> roles;

	/* MAQUINARIA */
	public List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();
	public List<String> numeroEconomicoList = new ArrayList<>();
	public List<Modelo> modeloList = new ArrayList<>();

	private boolean creado = false;

	@PostConstruct
	public void init() {
		if (creado == false) {
			this.setFechaBL1(restarMesesFecha(3));
			this.setFechaBL2(new Date());
			creado = !creado;
		}
	}

	public void limpiarFiltrosTablaBacklogs() {
		estatusFiltro = null;
		folioFiltro = null;
		numeroFolioFiltro = 0;
		numEconFiltro = null;
		serieFiltro = null;
		horometroFiltro = null;
		fechaCreacionFiltro = null;
		fechaCerradoFiltro = null;
		diasFiltro = null;
		sistemaFiltro = null;
		descripcionFiltro = 0;
		sintomasFiltro = null;
		accionFiltro = null;
		prioridadFiltro = 0;
		notasFiltro = null;
		origenFiltro = 0;
		partesFiltro = null;
		creadoPorFiltro = null;
		numeroReserva = null;
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('tablaBacklogsMineros').filter()");
	}

	public String getAccionFiltro() {
		return accionFiltro;
	}

	public void setAccionFiltro(String accionFiltro) {
		this.accionFiltro = accionFiltro;
	}

	public List<InspeccionCatDto> getInspeccionesSeleccionadas() {
		return inspeccionesSeleccionadas;
	}

	public Integer getAlertasTotal() {
		return alertasTotal;
	}

	public void setAlertasTotal(Integer alertasTotal) {
		this.alertasTotal = alertasTotal;
	}

	public Integer getAlerta() {
		return alerta;
	}

	public Short getSucursalFiltro() {
		return sucursalFiltro;
	}

	public void setSucursalFiltro(Short sucursalFiltro) {
		this.sucursalFiltro = sucursalFiltro;
	}

	public void setAlerta(Integer alerta) {
		this.alerta = alerta;
	}

	public boolean isLecturaPDF() {
		return lecturaPDF;
	}

	public void setLecturaPDF(boolean lecturaPDF) {
		this.lecturaPDF = lecturaPDF;
	}

	public void setInspeccionesSeleccionadas(List<InspeccionCatDto> inspeccionesSeleccionadas) {
		this.inspeccionesSeleccionadas = inspeccionesSeleccionadas;
	}

	public void setInspeccionEnProceso(Integer inspeccionEnProceso) {
		this.inspeccionEnProceso = inspeccionEnProceso;
	}

	public void setIncrementarInspeccionEnProceso() {
		this.inspeccionEnProceso++;
	}

	public void setIncrementarAlerta() {
		this.alerta++;
	}

	public void desseleccionarBacklog() {
		backlogsMinerosSeleccionado = null;
		muestrasSeleccionadas.clear();
	}

	public List<GetMueSosDto> getMuestrasSeleccionadas() {
		return muestrasSeleccionadas;
	}

	public void setMuestrasSeleccionadas(List<GetMueSosDto> muestrasSeleccionadas) {
		this.muestrasSeleccionadas = muestrasSeleccionadas;
	}

	public List<String> getRoles() {
		return roles;
	}

	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	public BacklogsMineros getBacklogsMinerosSeleccionado() {
		return backlogsMinerosSeleccionado;
	}

	public void setBacklogsMinerosSeleccionado(BacklogsMineros backlogsMinerosSeleccionado) {
		this.backlogsMinerosSeleccionado = backlogsMinerosSeleccionado;
	}

	public List<CodigosSistemas> getCodigosSistemasListFiltrado() {
		return codigosSistemasListFiltrado;
	}

	public void setCodigosSistemasListFiltrado(List<CodigosSistemas> codigosSistemasListFiltrado) {
		this.codigosSistemasListFiltrado = codigosSistemasListFiltrado;
	}

	public List<CodigosSMCS> getCodigosSMCSListFiltrado() {
		return codigosSMCSListFiltrado;
	}

	public void setCodigosSMCSListFiltrado(List<CodigosSMCS> codigosSMCSListFiltrado) {
		this.codigosSMCSListFiltrado = codigosSMCSListFiltrado;
	}

	public List<OtrosTrabajos> getOtrosTrabajosListFiltrado() {
		return otrosTrabajosListFiltrado;
	}

	public void setOtrosTrabajosListFiltrado(List<OtrosTrabajos> otrosTrabajosListFiltrado) {
		this.otrosTrabajosListFiltrado = otrosTrabajosListFiltrado;
	}

	public int getInspeccionEnProceso() {
		return inspeccionEnProceso;
	}

	public void setInspeccionEnProceso(int inspeccionEnProceso) {
		this.inspeccionEnProceso = inspeccionEnProceso;
	}

	public Marca getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(Marca idMarca) {
		this.idMarca = idMarca;
	}

	public List<CargosTrabajo> getCargosTrabajoListFiltrado() {
		return cargosTrabajoListFiltrado;
	}

	public void setCargosTrabajoListFiltrado(List<CargosTrabajo> cargosTrabajoListFiltrado) {
		this.cargosTrabajoListFiltrado = cargosTrabajoListFiltrado;
	}

	public Tecnico getTecnico() {
		return tecnico;
	}

	public void setTecnico(Tecnico tecnico) {
		this.tecnico = tecnico;
	}

	public List<NumeroParteClientes> getNumeroParteClientesListFiltrado() {
		return numeroParteClientesListFiltrado;
	}

	public void setNumeroParteClientesListFiltrado(List<NumeroParteClientes> numeroParteClientesListFiltrado) {
		this.numeroParteClientesListFiltrado = numeroParteClientesListFiltrado;
	}

	public List<GetMueSosDto> getMuestrasSOSListFiltrado() {
		return muestrasSOSListFiltrado;
	}

	public void setMuestrasSOSListFiltrado(List<GetMueSosDto> muestrasSOSListFiltrado) {
		this.muestrasSOSListFiltrado = muestrasSOSListFiltrado;
	}

	public String getEstatusFiltro() {
		return estatusFiltro;
	}

	public void setEstatusFiltro(String estatusFiltro) {
		this.estatusFiltro = estatusFiltro;
	}

	public String getFolioFiltro() {
		return folioFiltro;
	}

	public void setFolioFiltro(String folioFiltro) {
		this.folioFiltro = folioFiltro;
	}

	public String getNumEconFiltro() {
		return numEconFiltro;
	}

	public void setNumEconFiltro(String numEconFiltro) {
		this.numEconFiltro = numEconFiltro;
	}

	public String getSerieFiltro() {
		return serieFiltro;
	}

	public void setSerieFiltro(String serieFiltro) {
		this.serieFiltro = serieFiltro;
	}

	public Date getFechaFiltro() {
		return fechaCreacionFiltro;
	}

	public void setFechaFiltro(Date fechaFiltro) {
		this.fechaCreacionFiltro = fechaFiltro;
	}

	public String getDiasFiltro() {
		return diasFiltro;
	}

	public void setDiasFiltro(String diasFiltro) {
		this.diasFiltro = diasFiltro;
	}

	public int getDescripcionFiltro() {
		return descripcionFiltro;
	}

	public void setDescripcionFiltro(int descripcionFiltro) {
		this.descripcionFiltro = descripcionFiltro;
	}

	public String getSintomasFiltro() {
		return sintomasFiltro;
	}

	public void setSintomasFiltro(String sintomasFiltro) {
		this.sintomasFiltro = sintomasFiltro;
	}

	public String getNotasFiltro() {
		return notasFiltro;
	}

	public void setNotasFiltro(String notasFiltro) {
		this.notasFiltro = notasFiltro;
	}

	public String getHorometroFiltro() {
		return horometroFiltro;
	}

	public void setHorometroFiltro(String horometroFiltro) {
		this.horometroFiltro = horometroFiltro;
	}

	public String getSistemaFiltro() {
		return sistemaFiltro;
	}

	public void setSistemaFiltro(String sistemaFiltro) {
		this.sistemaFiltro = sistemaFiltro;
	}

	public int getPrioridadFiltro() {
		return prioridadFiltro;
	}

	public void setPrioridadFiltro(int prioridadFiltro) {
		this.prioridadFiltro = prioridadFiltro;
	}

	public int getOrigenFiltro() {
		return origenFiltro;
	}

	public void setOrigenFiltro(int origenFiltro) {
		this.origenFiltro = origenFiltro;
	}

	public Date getFechaCreacionFiltro() {
		return fechaCreacionFiltro;
	}

	public void setFechaCreacionFiltro(Date fechaCreacionFiltro) {
		this.fechaCreacionFiltro = fechaCreacionFiltro;
	}

	public Date getFechaCerradoFiltro() {
		return fechaCerradoFiltro;
	}

	public void setFechaCerradoFiltro(Date fechaCerradoFiltro) {
		this.fechaCerradoFiltro = fechaCerradoFiltro;
	}

	public String getCreadoPorFiltro() {
		return creadoPorFiltro;
	}

	public void setCreadoPorFiltro(String creadoPorFiltro) {
		this.creadoPorFiltro = creadoPorFiltro;
	}

	public int getNumeroFolioFiltro() {
		return numeroFolioFiltro;
	}

	public void setNumeroFolioFiltro(int numeroFolioFiltro) {
		this.numeroFolioFiltro = numeroFolioFiltro;
	}

	public String getPartesFiltro() {
		return partesFiltro;
	}

	public void setPartesFiltro(String partesFiltro) {
		this.partesFiltro = partesFiltro;
	}

	public BacklogMineroEstandar getBacklogEstandarSelected() {
		return backlogEstandarSelected;
	}

	public void setBacklogEstandarSelected(BacklogMineroEstandar backlogEstandarSelected) {
		this.backlogEstandarSelected = backlogEstandarSelected;
	}

	public boolean isRevisionBacklog() {
		return revisionBacklog;
	}

	public void setRevisionBacklog(boolean revisionBacklog) {
		this.revisionBacklog = revisionBacklog;
	}

	public RevisionKardexEntity getRevisionSeleccionada() {
		return revisionSeleccionada;
	}

	public void setRevisionSeleccionada(RevisionKardexEntity revisionSeleccionada) {
		this.revisionSeleccionada = revisionSeleccionada;
	}

	public CatInspectQuestionsEntity getInspeccionSeleccionada() {
		return inspeccionSeleccionada;
	}

	public void setInspeccionSeleccionada(CatInspectQuestionsEntity inspeccionSeleccionada) {
		this.inspeccionSeleccionada = inspeccionSeleccionada;
	}

	public String getRedireccionDialogBLEST() {
		return redireccionDialogBLEST;
	}

	public void setRedireccionDialogBLEST(String redireccionDialogBLEST) {
		this.redireccionDialogBLEST = redireccionDialogBLEST;
	}

	public List<BacklogsMineros> getBacklogsFiltradosList() {
		return backlogsFiltradosList;
	}

	public void setBacklogsFiltradosList(List<BacklogsMineros> backlogsFiltradosList) {
		this.backlogsFiltradosList = backlogsFiltradosList;
	}

	public List<EstatusBacklogsMineros> getEstadosFiltradosList() {
		return estadosFiltradosList;
	}

	public void setEstadosFiltradosList(List<EstatusBacklogsMineros> estadosFiltradosList) {
		this.estadosFiltradosList = estadosFiltradosList;
	}

	public String getGraficaSeleccionada() {
		return graficaSeleccionada;
	}

	public void setGraficaSeleccionada(String graficaSeleccionada) {
		this.graficaSeleccionada = graficaSeleccionada;
	}

	public int getAuxiliarGrafica() {
		return auxiliarGrafica;
	}

	public void setAuxiliarGrafica(int auxiliarGrafica) {
		this.auxiliarGrafica = auxiliarGrafica;
	}

	public boolean isDialogGrafica() {
		return dialogGrafica;
	}

	public void setDialogGrafica(boolean dialogGrafica) {
		this.dialogGrafica = dialogGrafica;
	}

	public IndicadoresEntity getComentarioSeleccionado() {
		return comentarioSeleccionado;
	}

	public void setComentarioSeleccionado(IndicadoresEntity comentarioSeleccionado) {
		this.comentarioSeleccionado = comentarioSeleccionado;
	}

	public List<MaquinariaDto> getMaquinariaDtoList() {
		return maquinariaDtoList;
	}

	public void setMaquinariaDtoList(List<MaquinariaDto> maquinariaDtoList) {
		this.maquinariaDtoList = maquinariaDtoList;
	}

	public List<String> getNumeroEconomicoList() {
		return numeroEconomicoList;
	}

	public void setNumeroEconomicoList(List<String> numeroEconomicoList) {
		this.numeroEconomicoList = numeroEconomicoList;
	}

	public List<Modelo> getModeloList() {
		return modeloList;
	}

	public void setModeloList(List<Modelo> modeloList) {
		this.modeloList = modeloList;
	}

	public String getMensajeLoading() {
		return mensajeLoading;
	}

	public void setMensajeLoading(String mensajeLoading) {
		this.mensajeLoading = mensajeLoading;
	}

	public Integer getIdClienteMatco() {
		return idClienteMatco;
	}

	public void setIdClienteMatco(Integer idClienteMatco) {
		this.idClienteMatco = idClienteMatco;
	}

	public List<EstatusBacklogsMineros> getEstatusBacklogsMinerosList() {
		return estatusBacklogsMinerosList;
	}

	public void setEstatusBacklogsMinerosList(List<EstatusBacklogsMineros> estatusBacklogsMinerosList) {
		this.estatusBacklogsMinerosList = estatusBacklogsMinerosList;
	}

	public String getNumeroReserva() {
		return numeroReserva;
	}

	public void setNumeroReserva(String numeroReserva) {
		this.numeroReserva = numeroReserva;
	}

	public String getNumeroSerie() {
		return numeroSerie;
	}

	public void setNumeroSerie(String numeroSerie) {
		this.numeroSerie = numeroSerie;
	}

	public String getNumeroEconomico() {
		return numeroEconomico;
	}

	public void setNumeroEconomico(String numeroEconomico) {
		this.numeroEconomico = numeroEconomico;
	}

	public String getModelo() {
		return modelo;
	}

	public void setModelo(String modelo) {
		this.modelo = modelo;
	}

	public Date getFechaBL1() {
		return fechaBL1;
	}

	public void setFechaBL1(Date fechaBL1) {
		this.fechaBL1 = fechaBL1;
	}

	public Date getFechaBL2() {
		return fechaBL2;
	}

	public void setFechaBL2(Date fechaBL2) {
		this.fechaBL2 = fechaBL2;
	}

	public int getIndexTablaBlm() {
		return indexTablaBlm;
	}

	public void setIndexTablaBlm(int indexTablaBlm) {
		this.indexTablaBlm = indexTablaBlm;
	}

}
