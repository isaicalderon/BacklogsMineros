package com.matco.backlogs.bean.indicadores;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;

import com.matco.backlogs.bean.BacklogsStaticsVarBean;
import com.matco.backlogs.entity.DatosGraficas;
import com.matco.backlogs.entity.IndicadoresEntity;

/***
 * Visualizar gráfica de porcentaje de estatus de backlogs por mes
 * 
 * @author N soluciones de software
 *
 */
@ManagedBean(name = "graficaPorcentajeEstatusBean")
@ViewScoped
public class GraficaPorcentajeEstatusBean extends GraficInterfaz implements Serializable {

	private static final long serialVersionUID = 6652832029185765284L;
	private static final Logger log = Logger.getLogger(GraficaPorcentajeEstatusBean.class);
	private DatosGraficas datosGraficas;
	private BarChartModel barChartModel;
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

	@PostConstruct
	public void init() {
		setIntervalo("10");
		if (seleccionBean.getAuxiliarGrafica() == 5 || seleccionBean.getAuxiliarGrafica() == 0) {
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

		List<List<Double>> datos = new ArrayList<>();
		List<Double> temp = new ArrayList<>();

		for (int i = 0; i < split.length; i++) {
			if (split[i].length() > 1) {
				if (split[i].charAt(0) == '[') {
					String val = split[i].replace("[", "");
					temp.add(Double.parseDouble(val));
				} else if (split[i].charAt(split[i].length() - 1) == ']') {
					String val = split[i].replace("]", "");
					temp.add(Double.parseDouble(val));
					datos.add(new ArrayList<Double>(temp));
					temp.clear();
				} else {
					temp.add(Double.valueOf(split[i]));
				}
			} else {
				temp.add(Double.valueOf(split[i]));
			}
		}

		datosGraficas = new DatosGraficas();
		datosGraficas.setPorcentajeGenerados(datos.get(0));
		datosGraficas.setPorcentajePendientes(datos.get(1));
		datosGraficas.setPorcentajeEjecutados(datos.get(2));
	}

	@Override
	public BarChartModel initModels() {

		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				datosGraficas = graficasFacade.obtenerPorcentajeEstatusBackLogsPorMes(fecha1, fecha2, serie, modelo,
						almacen);
			} else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		BarChartModel modelo = new BarChartModel();

		ChartSeries backlogsCreados = new ChartSeries();
		ChartSeries backlogsPendientes = new ChartSeries();
		ChartSeries backlogsEjecutados = new ChartSeries();

		backlogsCreados.setLabel("CREADOS");
		crearColumnasDouble(backlogsCreados, fecha1, fecha2, datosGraficas.getPorcentajeGenerados());

		backlogsPendientes.setLabel("PENDIENTES");
		crearColumnasDouble(backlogsPendientes, fecha1, fecha2, datosGraficas.getPorcentajePendientes());

		backlogsEjecutados.setLabel("EJECUTADOS");
		crearColumnasDouble(backlogsEjecutados, fecha1, fecha2, datosGraficas.getPorcentajeEjecutados());

		modelo.addSeries(backlogsCreados);
		modelo.addSeries(backlogsPendientes);
		modelo.addSeries(backlogsEjecutados);

		if (dialogoGrafica == false) {
			crearValoresGrafica();
		}

		return modelo;
	}

	/**
	 * Crea en forma de string los valores de la grafica para subirse como
	 * comentario
	 */
	public void crearValoresGrafica() {
		try {
			String valores = "[";
			for (Double val : datosGraficas.getPorcentajeGenerados()) {
				if (valores.equals("[")) {
					valores = valores + "" + val;
				} else {
					valores = valores + "," + val;
				}
			}

			valores = valores + "],[";
			for (int i = 0; i < datosGraficas.getPorcentajePendientes().size(); i++) {
				if (i == 0) {
					valores = valores + "" + datosGraficas.getPorcentajePendientes().get(i);
				} else {
					valores = valores + "," + datosGraficas.getPorcentajePendientes().get(i);
				}
			}

			valores = valores + "],[";
			for (int i = 0; i < datosGraficas.getPorcentajeEjecutados().size(); i++) {
				if (i == 0) {
					valores = valores + "" + datosGraficas.getPorcentajeEjecutados().get(i);
				} else {
					valores = valores + "," + datosGraficas.getPorcentajeEjecutados().get(i);
				}
			}
			valores = valores + "]";
			genericGraficBean.setValores(valores);
		} catch (Exception e) {
			log.error("[101] Fallo la gráfica:", e);
		}
	}

	@SuppressWarnings("deprecation")
	public void createModels() {
		barChartModel = initModels();
		barChartModel.setSeriesColors("C00000,17375D,75923C");
		barChartModel.setLegendPosition("s");
		barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		barChartModel.setTitle("PORCENTAJE GENERADOS / EJECUTADOS / PENDIENTES DE EJECUTAR");
		barChartModel.setAnimate(true);
		barChartModel.setShowPointLabels(true);

		if ((fecha2.getYear() - fecha1.getYear()) >= 1) {
			barChartModel.getAxis(AxisType.X).setTickAngle(-40);
		}

		Axis yAxis = barChartModel.getAxis(AxisType.Y);
		yAxis.setLabel("% BACKLOGS - ROJO VERDE AZUL");
		yAxis.setMin(0);
		yAxis.setMax(100);
		yAxis.setTickInterval(getIntervalo());
	}

	public BarChartModel getBarChartModel() {
		return barChartModel;
	}

	public void setBarChartModel(BarChartModel barChartModel) {
		this.barChartModel = barChartModel;
	}

}
