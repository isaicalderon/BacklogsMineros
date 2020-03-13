package com.matco.backlogs.bean.indicadores;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;

import com.matco.backlogs.bean.BacklogsStaticsVarBean;
import com.matco.backlogs.entity.IndicadoresEntity;

/***
 * Visualizar grafica de Backlogs Generados por Area
 * 
 * @author N soluciones software
 * @developer ialeman
 */

@ManagedBean(name = "graficaBLGeneradoAreaBean")
public class GraficaBLGeneradosAreaBean extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = 1359261564932551198L;
	private static final Logger log = Logger.getLogger(GraficaBLGeneradosAreaBean.class);
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private BarChartModel barChartModel;
	private int[] inspeccionesArray;
	private String sumario;
	private String detalle;

	@PostConstruct
	public void init() {
		setIntervalo("5");
		if (seleccionBean.getAuxiliarGrafica() == 8 || seleccionBean.getAuxiliarGrafica() == 0) {
			buscar();
		}
	}

	/**
	 * Busca y Ordena la grafica segun las fechas
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void buscar() {

		fecha1 = genericGraficBean.getFecha1();
		fecha1.setDate(1);
		fecha1.setMonth(obtenerMesInt(genericGraficBean.getMesSeleccionado1()));
		fecha1.setYear(Integer.parseInt(genericGraficBean.getAnioSeleccionadoString1()) - 1900);

		fecha2 = genericGraficBean.getFecha2();
		fecha2.setMonth(obtenerMesInt(genericGraficBean.getMesSeleccionado2()));
		fecha2.setYear(Integer.parseInt(genericGraficBean.getAnioSeleccionadoString2()) - 1900);
		Calendar cal = GregorianCalendar.getInstance();
		cal.set(fecha2.getYear()+1900, fecha2.getMonth(), 1);
		fecha2.setDate(cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH));

		int fechaResult = fecha1.compareTo(fecha2);

		if (fechaResult != 1) {
			createModels();
		} else {
			agregarMensaje(summary, "La fecha inicial no puede ser mayor a la fecha final", "warn", false);
		}
	}

	/**
	 * Obtiene y remplaza los valores de la grafica comentada
	 */
	public void obtenerValores() {

		IndicadoresEntity comentario = seleccionBean.getComentarioSeleccionado();
		String valores = comentario.getValores();
		fecha1 = comentario.getFechaInicial();
		fecha2 = comentario.getFechaFinal();

		String[] split = valores.split(",");

		List<List<Integer>> datos = new ArrayList<>();
		List<Integer> temp = new ArrayList<>();

		for (int i = 0; i < split.length; i++) {
			if (split[i].length() > 1) {
				if (split[i].charAt(0) == '[') {
					String val = split[i].replace("[", "");
					temp.add(Integer.parseInt(val));
				} else if (split[i].charAt(split[i].length() - 1) == ']') {
					String val = split[i].replace("]", "");
					temp.add(Integer.parseInt(val));
					datos.add(new ArrayList<Integer>(temp));
					temp.clear();
				} else {
					temp.add(Integer.valueOf(split[i]));
				}
			} else {
				temp.add(Integer.valueOf(split[i]));
			}
		}

		// datosGraficas = new DatosGraficas();
		inspeccionesArray = new int[9];
		List<Integer> dataTemp = datos.get(0);
		for (int i = 0; i < inspeccionesArray.length; i++) {
			inspeccionesArray[i] = dataTemp.get(i);
		}
	}
	
	@Override
	public BarChartModel initModels() {

		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				inspeccionesArray = graficasFacade.obtenerToTalBackLogsGeneradosPorArea(fecha1, fecha2, serie, modelo,
						almacen);
			} else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(sumario, detalle);
		}

		BarChartModel modelo = new BarChartModel();
		ChartSeries inspecciones = new ChartSeries();

		inspecciones.setLabel("INSPECCIONES");
		inspecciones.set("PM", inspeccionesArray[0]);
		inspecciones.set("SOS Fluidos", inspeccionesArray[1]);
		inspecciones.set("VIMS", inspeccionesArray[2]);
		inspecciones.set("Diaria", inspeccionesArray[3]);
		inspecciones.set("Pre-PM", inspeccionesArray[4]);
		inspecciones.set("Pre-PCR", inspeccionesArray[5]);
		inspecciones.set("Operador", inspeccionesArray[6]);
		inspecciones.set("Inspección PM", inspeccionesArray[7]);
		inspecciones.set("Tecnicas", inspeccionesArray[8]);

		modelo.addSeries(inspecciones);

		if (dialogoGrafica == false) {
			crearValores();
		}

		return modelo;
	}

	public void crearValores() {
		String valores = "[";
		for (int i = 0; i < inspeccionesArray.length; i++) {
			if (i == 0) {
				valores = valores + "" + inspeccionesArray[i];
			} else {
				valores = valores + "," + inspeccionesArray[i];
			}
		}
		valores = valores + "]";
		genericGraficBean.setValores(valores);
	}

	@SuppressWarnings("deprecation")
	public void createModels() {

		barChartModel = initModels();
		
		String fechas = getMes(fecha1.getMonth()) + "/" + (fecha1.getYear() + 1900) + " al " + getMes(fecha2.getMonth())
				+ "/" + (fecha2.getYear() + 1900);

		barChartModel.setSeriesColors("ffc000");
		barChartModel.setLegendPosition("s");
		barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		barChartModel.setTitle("BACKLOGS GENERADOS POR ÁREA");
		barChartModel.setAnimate(true);
		barChartModel.setShowPointLabels(true);
		barChartModel.getAxes().put(AxisType.X, new CategoryAxis(fechas));
		// if ((fecha2.getYear() - fecha1.getYear()) >= 1) {
		// barChartModel.getAxis(AxisType.X).setTickAngle(-40);
		// }
		Axis yAxis = barChartModel.getAxis(AxisType.Y);
		yAxis.setMin(0);
		// yAxis.setMax(getMaximo());
		yAxis.setMax(getMaximo());
		yAxis.setTickInterval("5");
	}

	/*
	 * public int getMaximo() { int intervalo = Integer.parseInt(getIntervalo());
	 * int sumatoria = datosGraficas.getCantidadEtRoja() +
	 * datosGraficas.getCantidadEtAzul()+datosGraficas.getCantidadEtVerde()
	 * +datosGraficas.getCantidadEtAmarilla(); if (sumatoria < 35) { sumatoria = 35;
	 * } sumatoria= (sumatoria/intervalo+2)*intervalo; return sumatoria; }
	 */

	public int getMaximo() {
		int max = 0;
		for (Integer val : inspeccionesArray) {
			max = Math.max(max, val);
		}

		max = max + 5;
		while ((max % 5) != 0) {
			max++;
		}

		return max;
	}

	public BarChartModel getBarChartModel() {
		return barChartModel;
	}

	public void setBarChartModel(BarChartModel barChartModel) {
		this.barChartModel = barChartModel;
	}

}
