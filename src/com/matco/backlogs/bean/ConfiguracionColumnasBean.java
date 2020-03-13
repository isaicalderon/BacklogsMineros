package com.matco.backlogs.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/***
 * Administra las columnas que se van a mostrar en la lista de backlogs
 * 
 * @author N soluciones software
 *
 */
@ManagedBean(name = "configuracionColumnasBean")
@SessionScoped
public class ConfiguracionColumnasBean extends GenericBacklogBean implements Serializable {
	
	private static final long serialVersionUID = -4012170713138469546L;
	private List<String> columnasSeleccionadas = new ArrayList<>(); // opciones marcadas en el menú "ocultar columnas"
	private List<String> columnas; // todas las opciones del menú "ocultar columnas"
	private String mostrarSeleccionar = "";
	private String mostrarEstatus = "";
	private String mostrarFolio = "";
	private String mostrarNumEcon = "";
	private String mostrarSerie = "";
	private String mostrarHorometro = "";
	private String mostrarFechaCreacion = "";
	private String mostrarFechaCierre = "";
	private String mostrarDiasActivo = "";
	private String mostrarSistema = "";
	private String mostrarDescripcion = "";
	private String mostrarSintomas = "";
	private String mostrarAccion = "";
	private String mostrarNotas = "";
	private String mostrarPrioridad = "";
	private String mostrarOrigen = "";
	private String mostrarPartes = "";
	private String mostrarCreadoPor = "";
	private boolean mostrarBotonCargaRapida = true;
	//private String mostrarSucursal = "";
	//private String mostrarCancelado = "";
	//private String mostrarPospuesto = "";
	//private String mostrarEsperaRefacciones = "";

	
	/***
	 * Inicializ el menú "ocultar columnas" con una opción por cada columna de la
	 * tabla de lista de backlogs
	 */
	@PostConstruct
	public void init() {
		columnas = new ArrayList<String>();
		columnas.add("Seleccionar BL");
		columnas.add("Estatus BL");
		columnas.add("Folio BL");
		/*
		if (obtenerSucursalFiltro() == null) {
			columnas.add("Sucursal");
		}
		*/
		columnas.add("Núm. Econ.");
		columnas.add("Serie");
		columnas.add("Horómetro");
		columnas.add("Fecha Creación");
		columnas.add("Fecha Cierre");
		columnas.add("Dias activo");
		columnas.add("Sistema");
		columnas.add("Descripción");
		columnas.add("Sintomas");
		columnas.add("Acción");
		columnas.add("Prioridad");
		columnas.add("Notas");
		columnas.add("Origen");
		columnas.add("Refacciones");
		columnas.add("Creado por");
		columnas.add("Cancelado");
		columnas.add("Pospuestor");
		columnas.add("Espera de Refacciones");

	}

	/***
	 * Asigna a las variables de correspondientes a cada columna el atributo style
	 * dependiendo de las casillas seleccionadas
	 */
	public void actualizarColumnas() {
		mostrarSeleccionar = actualizarEstilo("Seleccionar BL");
		mostrarBotonCargaRapida = mostrarSeleccionar.equals("");
		mostrarEstatus = actualizarEstilo("Estatus BL");
		mostrarFolio = actualizarEstilo("Folio BL");
		//mostrarSucursal = obtenerSucursalFiltro() == null ? actualizarEstilo("Sucursal") : "display:none";
		mostrarNumEcon = actualizarEstilo("Núm. Econ.");
		mostrarSerie = actualizarEstilo("Serie");
		mostrarHorometro = actualizarEstilo("Horómetro");
		mostrarFechaCreacion = actualizarEstilo("Fecha Creación");
		mostrarFechaCierre = actualizarEstilo("Fecha Cierre");
		mostrarDiasActivo = actualizarEstilo("Dias activo");
		mostrarSistema = actualizarEstilo("Sistema");
		mostrarDescripcion = actualizarEstilo("Descripción");
		mostrarSintomas = actualizarEstilo("Sintomas");
		mostrarAccion = actualizarEstilo("Acción");
		mostrarPrioridad = actualizarEstilo("Prioridad");
		mostrarNotas = actualizarEstilo("Notas");
		mostrarOrigen = actualizarEstilo("Origen");
		mostrarPartes = actualizarEstilo("Refacciones");
		mostrarCreadoPor = actualizarEstilo("Creado por");

		//mostrarCancelado = actualizarEstilo("Cancelado");
		//mostrarPospuesto = actualizarEstilo("Pospuesto");
		//mostrarEsperaRefacciones = actualizarEstilo("Espera de Refacciones");
		
	}

	/***
	 * Verifica si una columna esta seleccionada o no
	 * 
	 * @param columna casilla a comprobar si esta marcada
	 * @return "display:none" si la columna esta marcada como oculta y "" si esta
	 *         visible
	 */
	public String actualizarEstilo(String columna) {
		String estilo = "";
		if (columnasSeleccionadas.contains(columna)) {
			estilo = "display:none;";
		}
		return estilo;
	}

	public String getMostrarAccion() {
		return mostrarAccion;
	}

	public void setMostrarAccion(String mostrarAccion) {
		this.mostrarAccion = mostrarAccion;
	}

	public String getMostrarEstatus() {
		return mostrarEstatus;
	}

	public void setMostrarEstatus(String mostrarEstatus) {
		this.mostrarEstatus = mostrarEstatus;
	}

	public List<String> getColumnas() {
		return columnas;
	}

	public void setColumnas(List<String> columnas) {
		this.columnas = columnas;
	}

	public List<String> getColumnasSeleccionadas() {
		return columnasSeleccionadas;
	}

	public void setColumnasSeleccionadas(List<String> columnasSeleccionadas) {
		this.columnasSeleccionadas = columnasSeleccionadas;
	}

	public String getMostrarFolio() {
		return mostrarFolio;
	}

	public void setMostrarFolio(String mostrarFolio) {
		this.mostrarFolio = mostrarFolio;
	}

	public String getMostrarSerie() {
		return mostrarSerie;
	}

	public void setMostrarSerie(String mostrarSerie) {
		this.mostrarSerie = mostrarSerie;
	}

	public String getMostrarHorometro() {
		return mostrarHorometro;
	}

	public void setMostrarHorometro(String mostrarHorometro) {
		this.mostrarHorometro = mostrarHorometro;
	}

	public String getMostrarDiasActivo() {
		return mostrarDiasActivo;
	}

	public void setMostrarDiasActivo(String mostrarDiasActivo) {
		this.mostrarDiasActivo = mostrarDiasActivo;
	}

	public String getMostrarSistema() {
		return mostrarSistema;
	}

	public void setMostrarSistema(String mostrarSistema) {
		this.mostrarSistema = mostrarSistema;
	}

	public String getMostrarDescripcion() {
		return mostrarDescripcion;
	}

	public void setMostrarDescripcion(String mostrarDescripcion) {
		this.mostrarDescripcion = mostrarDescripcion;
	}

	public String getMostrarSintomas() {
		return mostrarSintomas;
	}

	public void setMostrarSintomas(String mostrarSintomas) {
		this.mostrarSintomas = mostrarSintomas;
	}

	public String getMostrarNotas() {
		return mostrarNotas;
	}

	public void setMostrarNotas(String mostrarNotas) {
		this.mostrarNotas = mostrarNotas;
	}

	public String getMostrarNumEcon() {
		return mostrarNumEcon;
	}

	public void setMostrarNumEcon(String mostrarNumEcon) {
		this.mostrarNumEcon = mostrarNumEcon;
	}

	public String getMostrarPrioridad() {
		return mostrarPrioridad;
	}

	public void setMostrarPrioridad(String mostrarPrioridad) {
		this.mostrarPrioridad = mostrarPrioridad;
	}

	public String getMostrarSeleccionar() {
		return mostrarSeleccionar;
	}

	public void setMostrarSeleccionar(String mostrarSeleccionar) {
		this.mostrarSeleccionar = mostrarSeleccionar;
	}

	public boolean isMostrarBotonCargaRapida() {
		return mostrarBotonCargaRapida;
	}

	public void setMostrarBotonCargaRapida(boolean mostrarBotonCargaRapida) {
		this.mostrarBotonCargaRapida = mostrarBotonCargaRapida;
	}

	public String getMostrarOrigen() {
		return mostrarOrigen;
	}

	public void setMostrarOrigen(String mostrarOrigen) {
		this.mostrarOrigen = mostrarOrigen;
	}

	public String getMostrarFechaCreacion() {
		return mostrarFechaCreacion;
	}

	public void setMostrarFechaCreacion(String mostrarFechaCreacion) {
		this.mostrarFechaCreacion = mostrarFechaCreacion;
	}

	public String getMostrarFechaCierre() {
		return mostrarFechaCierre;
	}

	public void setMostrarFechaCierre(String mostrarFechaCierre) {
		this.mostrarFechaCierre = mostrarFechaCierre;
	}

	public String getMostrarCreadoPor() {
		return mostrarCreadoPor;
	}

	public void setMostrarCreadoPor(String mostrarCreadoPor) {
		this.mostrarCreadoPor = mostrarCreadoPor;
	}
	
	/*
	public String getMostrarSucursal() {
		return mostrarSucursal;
	}

	public void setMostrarSucursal(String mostrarSucursal) {
		this.mostrarSucursal = mostrarSucursal;
	}
*/	
	public String getMostrarPartes() {
		return mostrarPartes;
	}

	public void setMostrarPartes(String mostrarPartes) {
		this.mostrarPartes = mostrarPartes;
	}


}
