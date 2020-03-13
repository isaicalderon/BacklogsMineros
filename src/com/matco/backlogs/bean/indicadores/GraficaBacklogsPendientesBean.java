package com.matco.backlogs.bean.indicadores;

import com.matco.backlogs.bean.BacklogsStaticsVarBean;
import com.matco.backlogs.entity.DatosGraficas;
import com.matco.backlogs.entity.IndicadoresEntity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import org.primefaces.model.chart.BarChartModel;
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;

/***
 * Visualizar total de backlogs en estado B2, B1, C
 * 
 * @author N soluciones de software
 *
 */
@ManagedBean(name = "graficaBacklogsPendientesBean")
public class GraficaBacklogsPendientesBean extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = 3656320807676997115L;
	private static final Logger log = Logger.getLogger(GraficaBacklogsPendientesBean.class);
	private DatosGraficas datosGraficas;
	private BarChartModel barChartModel = new BarChartModel();
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

	private int cantidadB1;
	private int cantidadB2;
	private int cantidadC;

	/***
	 * Obtiene los valores de la base de datos
	 */
	@PostConstruct
	public void init() {
		setIntervalo("100");
		if (seleccionBean.getAuxiliarGrafica() == 4 || seleccionBean.getAuxiliarGrafica() == 0) {
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

		// List<List<Integer>> datos = new ArrayList<>();
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

		cantidadB1 = dataTemp.get(0);
		cantidadB2 = dataTemp.get(1);
		cantidadC = dataTemp.get(2);
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
				cantidadB1 = datosGraficas.getCantidadB1();
				cantidadB2 = datosGraficas.getCantidadB2();
				cantidadC = datosGraficas.getCantidadC();
			} else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		BarChartModel modelo = new BarChartModel();
		ChartSeries b1 = new ChartSeries();

		b1.setLabel("AUTORIZAR");
		b1.set(" ", cantidadB1);

		ChartSeries b2 = new ChartSeries();
		b2.setLabel("COTIZAR");
		b2.set(" ", cantidadB2);

		ChartSeries c = new ChartSeries();
		c.setLabel("EJECUTAR");
		c.set(" ", cantidadC);

		if (dialogoGrafica == false) {
			String valores = "[" + cantidadB1 + "," + cantidadB2 + "," + cantidadC + "]";
			genericGraficBean.setValores(valores);
		}
		
		modelo.addSeries(b1);
		modelo.addSeries(b2);
		modelo.addSeries(c);

		return modelo;
	}

	/***
	 * Crea la gráfica de backlogs pendientes
	 */
	@SuppressWarnings("deprecation")
	public void createModels() {
		barChartModel = initModels();
		
		String fechas = getMes(fecha1.getMonth()) + "/" + (fecha1.getYear() + 1900) + " al " + getMes(fecha2.getMonth())
				+ "/" + (fecha2.getYear() + 1900);

		barChartModel.setSeriesColors("17375D,D7D713,E46D0A");
		
		//barChartModel.setStacked(true);
		barChartModel.setLegendPosition("s");
		barChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		barChartModel.setTitle("BACKLOGS PENDIENTES");
		barChartModel.setAnimate(true);
		barChartModel.setShowPointLabels(true);
		barChartModel.getAxes().put(AxisType.X, new CategoryAxis(fechas));
		Axis yAxis = barChartModel.getAxis(AxisType.Y);
		yAxis.setMin(0);
		yAxis.setMax(getMaximo());
		yAxis.setTickInterval("5");
	}

	/***
	 * Compara los totales en los 3 estados (B1,B2,C) para saber cual es el mayor
	 * 
	 * @return el total mayor de los 3 estados, si es menor a 35 regresa 35
	 *         (default)
	 */
	public int getMaximo() {

		int max = cantidadB2 + cantidadB1 + cantidadC;

		max = max + 5;
		while ((max % 10) != 0) {
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
