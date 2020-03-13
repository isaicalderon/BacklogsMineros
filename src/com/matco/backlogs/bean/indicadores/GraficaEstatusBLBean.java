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
import com.matco.backlogs.entity.DatosGraficas;
import com.matco.backlogs.entity.IndicadoresEntity;

/***
 * Visualizar gráfica de Estatus de Backlogs
 * 
 * @author N soluciones de software
 * @developer ialeman
 */

@ManagedBean(name = "graficaEstatusBLBean")
public class GraficaEstatusBLBean extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = -8079497876020000907L;
	private static final Logger log = Logger.getLogger(GraficaEstatusBLBean.class);
	private DatosGraficas datosGraficas;
	private BarChartModel barChartModel = new BarChartModel();
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

	@PostConstruct
	public void init() {
		setIntervalo("5");
		if (seleccionBean.getAuxiliarGrafica() == 2 || seleccionBean.getAuxiliarGrafica() == 0) {
			buscar();
		}
	}

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
			createLineModels();
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

		datosGraficas = new DatosGraficas();
		List<Integer> dataTemp = datos.get(0);

		datosGraficas.setCantidadEtRoja(dataTemp.get(0));
		datosGraficas.setCantidadEtAzul(dataTemp.get(1));
		datosGraficas.setCantidadEtVerde(dataTemp.get(2));
		datosGraficas.setCantidadEtAmarilla(dataTemp.get(3));

	}
	
	@Override
	public BarChartModel initModels() {

		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				datosGraficas = graficasFacade.obtenerTotalBackLogsEstatusPendientes(fecha1, fecha2, serie, modelo,
						almacen);
			} else {
				obtenerValores();
			}

		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		BarChartModel model = new BarChartModel();

		ChartSeries cRojo = new ChartSeries();
		ChartSeries cAzul = new ChartSeries();
		ChartSeries cVerde = new ChartSeries();
		ChartSeries cAmarillo = new ChartSeries();

		cRojo.setLabel("ETAPA ROJA");
		cRojo.set("", datosGraficas.getCantidadEtRoja());

		cAzul.setLabel("ETAPA AZUL");
		cAzul.set("", datosGraficas.getCantidadEtAzul());

		cVerde.setLabel("ETAPA VERDE");
		cVerde.set("", datosGraficas.getCantidadEtVerde());

		cAmarillo.setLabel("ETAPA AMARILLA");
		cAmarillo.set("", datosGraficas.getCantidadEtAmarilla());

		model.addSeries(cRojo);
		model.addSeries(cAzul);
		model.addSeries(cVerde);
		model.addSeries(cAmarillo);

		if (dialogoGrafica == false) {
			crearValoresGrafica();
		}

		return model;
	}

	/**
	 * Crea en forma de string los valores de la grafica para subirse como
	 * comentario
	 */
	public void crearValoresGrafica() {
		String valores = "[";
		valores = valores + datosGraficas.getCantidadEtRoja() + "," + datosGraficas.getCantidadEtAzul() + ","
				+ datosGraficas.getCantidadEtVerde() + "," + datosGraficas.getCantidadEtAmarilla() + "]";

		genericGraficBean.setValores(valores);
	}

	@SuppressWarnings("deprecation")
	public void createLineModels() {

		barChartModel = initModels();
		
		String fechas = getMes(fecha1.getMonth()) + "/" + (fecha1.getYear() + 1900) + " al " + getMes(fecha2.getMonth())
				+ "/" + (fecha2.getYear() + 1900);

		barChartModel.setSeriesColors("ff0000,0070c0,00b050,ffc000");
		// barChartModel.setStacked(true);
		barChartModel.setLegendPosition("s");
		barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		barChartModel.setTitle("ESTATUS DE BACKLOGS");
		barChartModel.setAnimate(true);
		barChartModel.setShowPointLabels(true);
		barChartModel.getAxes().put(AxisType.X, new CategoryAxis(fechas));
		Axis yAxis = barChartModel.getAxis(AxisType.Y);
		yAxis.setMin(0);
		yAxis.setMax(getMaximo());
		yAxis.setTickInterval("5");
		
	}

	public int getMaximo() {

		int max = Math.max(datosGraficas.getCantidadEtRoja(), datosGraficas.getCantidadEtAzul());
		max = Math.max(max, datosGraficas.getCantidadEtVerde());
		max = Math.max(max, datosGraficas.getCantidadEtAmarilla());

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
