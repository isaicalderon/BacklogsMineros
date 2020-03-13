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
 * Visualizar gráfica Backlogs Generados por SMCS
 * 
 * @author N soluciones de software
 * @developer ialeman
 */
@ManagedBean(name = "graficaBLGeneradoSMCS")
public class GraficaBacklogsGeneradosSMCS extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = 501472859315595273L;
	private static final Logger log = Logger.getLogger(GraficaBacklogsGeneradosSMCS.class);
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private BarChartModel barChartModel = new BarChartModel();
	private String detalle;
	private ArrayList<Integer> arraySmcs;

	@PostConstruct
	public void init() {
		setIntervalo("5");
		if (seleccionBean.getAuxiliarGrafica() == 9 || seleccionBean.getAuxiliarGrafica() == 0) {
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
		arraySmcs = new ArrayList<>();
		List<Integer> dataTemp = datos.get(0);

		arraySmcs = (ArrayList<Integer>) dataTemp;

	}
	
	@Override
	public BarChartModel initModels() {
		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				arraySmcs = graficasFacade.obtenerTotalBackLogsGeneradosPorSMCS(fecha1, fecha2, serie, modelo, almacen);
			} else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		BarChartModel modelo = new BarChartModel();
		ChartSeries smcs = new ChartSeries();

		smcs.setLabel("SMCS");
		smcs.set("Motor", arraySmcs.get(0));
		smcs.set("E. Maq.", arraySmcs.get(1));
		smcs.set("E. Mot.", arraySmcs.get(2));
		smcs.set("TC", arraySmcs.get(3));
		smcs.set("XSMN", arraySmcs.get(4));
		smcs.set("Dif.", arraySmcs.get(5));
		smcs.set("Mando F.", arraySmcs.get(6));
		smcs.set("Freno", arraySmcs.get(7));
		smcs.set("Dir.", arraySmcs.get(8));
		smcs.set("Hco.", arraySmcs.get(9));
		smcs.set("S. Aire", arraySmcs.get(10));
		smcs.set("Maq.", arraySmcs.get(11));
		smcs.set("Chasis", arraySmcs.get(12));
		smcs.set("Susp.", arraySmcs.get(13));
		smcs.set("A/C", arraySmcs.get(14));
		smcs.set("Acc. y Abu.", arraySmcs.get(15));
		smcs.set("Otros", arraySmcs.get(16));

		modelo.addSeries(smcs);

		if (dialogoGrafica == false) {
			crearValoresGrafica();
		}

		return modelo;
	}

	public void crearValoresGrafica() {
		String valores = "[";
		for (int i = 0; i < arraySmcs.size(); i++) {
			if (i == 0) {
				valores = valores + "" + arraySmcs.get(i);
			} else {
				valores = valores + "," + arraySmcs.get(i);
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
		barChartModel.setTitle("BACKLOGS GENERADOS POR SMCS");
		barChartModel.setAnimate(true);
		barChartModel.setShowPointLabels(true);
		barChartModel.getAxes().put(AxisType.X, new CategoryAxis(fechas));
		Axis yAxis = barChartModel.getAxis(AxisType.Y);
		yAxis.setMin(0);
		yAxis.setMax(getMaximo());
		yAxis.setTickInterval("5");
	}

	/**
	 * Calcula el maximo de los values y le suma 5
	 * 
	 * @return
	 */
	public int getMaximo() {

		int max = 0;

		for (Integer value : arraySmcs) {
			max = Math.max(max, value);
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
