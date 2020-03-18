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
import org.primefaces.model.chart.CategoryAxis;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;

import com.matco.backlogs.bean.BacklogsStaticsVarBean;
import com.matco.backlogs.entity.DatosGraficas;
import com.matco.backlogs.entity.IndicadoresEntity;

/***
 * Visualizar grafica de sumatoria de horas reales y estimadas por mes
 * 
 * @author N soluciones software
 *
 */
@ManagedBean(name = "graficaLaborEstimadoBean")
public class GraficaLaborEstimadoBean extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = -2296350223299578082L;
	private static final Logger log = Logger.getLogger(GraficaLaborEstimadoBean.class);
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private DatosGraficas datosGraficas;
	private LineChartModel lineChartModel;
	private String sumario;
	private String detalle;

	/***
	 * obtienes los valores de la base de datos
	 */
	@PostConstruct
	public void init() {
		setIntervalo("100");
		if (seleccionBean.getAuxiliarGrafica() == 7 || seleccionBean.getAuxiliarGrafica() == 0) {
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
		datosGraficas.setHorasHomEstimadas(datos.get(0));
		datosGraficas.setHorasHomReales(datos.get(1));
	}
	
	@Override
	public LineChartModel initModels() {

		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				datosGraficas = graficasFacade.obtenerTotalBackLogsHorasHombreHorasReales(fecha1, fecha2, serie, modelo,
						almacen);
			} else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(sumario, detalle);
		}

		LineChartModel modelo = new LineChartModel();

		ChartSeries horasEstimadas = new ChartSeries();
		ChartSeries horasReales = new ChartSeries();

		horasEstimadas.setLabel("HORAS HOOMBRE ESTIMADAS");
		crearColumnasDouble(horasEstimadas, fecha1, fecha2, datosGraficas.getHorasHomEstimadas());

		horasReales.setLabel("HORAS HOMBRE REALES");
		crearColumnasDouble(horasReales, fecha1, fecha2, datosGraficas.getHorasHomReales());

		modelo.addSeries(horasEstimadas);
		modelo.addSeries(horasReales);
		
		if (dialogoGrafica == false) {
			crearValoresGrafica();
		}

		return modelo;
	}

	public void crearValoresGrafica() {
		String valores = "[";
		for (Double val : datosGraficas.getHorasHomEstimadas()) {
			if (valores.equals("[")) {
				valores = valores + "" + val;
			} else {
				valores = valores + "," + val;
			}
		}

		valores = valores + "],[";
		for (int i = 0; i < datosGraficas.getHorasHomReales().size(); i++) {
			if (i == 0) {
				valores = valores + "" + datosGraficas.getHorasHomReales().get(i);
			} else {
				valores = valores + "," + datosGraficas.getHorasHomReales().get(i);
			}
		}

		valores = valores + "]";
		genericGraficBean.setValores(valores);
	}

	@SuppressWarnings("deprecation")
	public void createModels() {
		lineChartModel = initModels();
		lineChartModel.setSeriesColors("75923C,173760");
		lineChartModel.setLegendPosition("s");
		lineChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		lineChartModel.setTitle("ADMINISTRACIÓN DE BACKLOGS - LABOR ESTIMADO DE REPARACIÓN ");
		lineChartModel.setAnimate(true);
		lineChartModel.setShowPointLabels(true);
		CategoryAxis categoryAxis = new CategoryAxis("");
		lineChartModel.getAxes().put(AxisType.X, categoryAxis);

		if ((fecha2.getYear() - fecha1.getYear()) >= 1) {
			lineChartModel.getAxis(AxisType.X).setTickAngle(-40);
		}

		Axis yAxis = lineChartModel.getAxis(AxisType.Y);
		yAxis.setLabel("Horas");
		//yAxis.setMax(getMaximo());
		yAxis.setTickInterval("5");
	}

	public int getMaximo() {

		double max = 0.0;

		for (double valor : datosGraficas.getHorasHomEstimadas()) {
			max = Math.max(max, valor);
		}

		for (double valor : datosGraficas.getHorasHomReales()) {
			max = Math.max(max, valor);
		}

		max += 10;
		while ((max % 10) != 0) {
			max++;
		}

		return (int) max;
	}

	public LineChartModel getLineChartModel() {
		return lineChartModel;
	}

	public void setLineChartModel(LineChartModel lineChartModel) {
		this.lineChartModel = lineChartModel;
	}

}
