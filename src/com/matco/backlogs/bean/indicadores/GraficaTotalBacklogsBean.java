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
import javax.faces.bean.ViewScoped;
import org.primefaces.model.chart.ChartSeries;
import org.primefaces.model.chart.LegendPlacement;
import org.primefaces.model.chart.LineChartModel;
import org.apache.log4j.Logger;
import org.primefaces.model.chart.Axis;
import org.primefaces.model.chart.AxisType;
import org.primefaces.model.chart.CategoryAxis;

/***
 * Visualiar gráfica de total de backlogs
 * 
 * @author N soluciones de software
 *
 */
@ManagedBean(name = "graficaTotalBacklogsBean")
@ViewScoped
public class GraficaTotalBacklogsBean extends GraficInterfaz implements Serializable {

	private static final long serialVersionUID = -1033967305497210728L;
	private static final Logger log = Logger.getLogger(GraficaTotalBacklogsBean.class);
	private DatosGraficas datosGraficas;
	private LineChartModel lineChartModel = new LineChartModel();
	private GenericGraficBean genericGraficBean = this.obtenerBean("genericGraficBean");
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");

	/***
	 * Obtiene los valores de la base de datos
	 */
	@PostConstruct
	public void init() {
		setIntervalo("100");
		if (seleccionBean.getAuxiliarGrafica() == 1 || seleccionBean.getAuxiliarGrafica() == 0) {
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
		int day = cal.getActualMaximum(GregorianCalendar.DAY_OF_MONTH);
		fecha2.setDate(day);
		
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
		datosGraficas.setTotalPendientes(datos.get(0));
		datosGraficas.setTotalGenerados(datos.get(1));
		datosGraficas.setTotalEjecutados(datos.get(2));
	}
	
	@Override
	public LineChartModel initModels() {

		boolean dialogoGrafica = seleccionBean.isDialogGrafica();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				datosGraficas = graficasFacade.obtenerTotalBackLogs(fecha1, fecha2, serie, modelo, almacen);
			} else {
				obtenerValores();
			}

		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		LineChartModel model = new LineChartModel();

		ChartSeries blPendientes = new ChartSeries();
		blPendientes.setLabel("PENDIENTES");
		crearColumnas(blPendientes, fecha1, fecha2, datosGraficas.getTotalPendientes());

		ChartSeries blGenerados = new ChartSeries();
		blGenerados.setLabel("GENERADOS");
		crearColumnas(blGenerados, fecha1, fecha2, datosGraficas.getTotalGenerados());

		ChartSeries blEjecutados = new ChartSeries();
		blEjecutados.setLabel("EJECUTADOS");
		crearColumnas(blEjecutados, fecha1, fecha2, datosGraficas.getTotalEjecutados());

		model.addSeries(blPendientes);
		model.addSeries(blEjecutados);
		model.addSeries(blGenerados);

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
		try {
			String valores = "[";
			for (Integer val : datosGraficas.getTotalPendientes()) {
				if (valores.equals("[")) {
					valores = valores + "" + val;
				} else {
					valores = valores + "," + val;
				}
			}

			valores = valores + "],[";
			for (int i = 0; i < datosGraficas.getTotalGenerados().size(); i++) {
				if (i == 0) {
					valores = valores + "" + datosGraficas.getTotalGenerados().get(i);
				} else {
					valores = valores + "," + datosGraficas.getTotalGenerados().get(i);
				}
			}

			valores = valores + "],[";
			for (int i = 0; i < datosGraficas.getTotalEjecutados().size(); i++) {
				if (i == 0) {
					valores = valores + "" + datosGraficas.getTotalEjecutados().get(i);
				} else {
					valores = valores + "," + datosGraficas.getTotalEjecutados().get(i);
				}
			}
			
			valores = valores + "]";
			genericGraficBean.setValores(valores);
			
		} catch (Exception e) {
			log.error("[100] Fallo la gráfica:", e);
		}
	}

	@SuppressWarnings("deprecation")
	private void createLineModels() {
		lineChartModel = initModels();
		lineChartModel.setTitle("BACKLOG GENERADOS / EJECUTADOS / PENDIENTES DE EJECUTAR");
		lineChartModel.setSeriesColors("D7D713,75923C,17375D");
		lineChartModel.setLegendPosition("s");
		lineChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		lineChartModel.setShowPointLabels(true);
		lineChartModel.getAxes().put(AxisType.X, new CategoryAxis("Meses - Años"));
		if ((fecha2.getYear() - fecha1.getYear()) >= 1) {
			lineChartModel.getAxis(AxisType.X).setTickAngle(-40);
		}
		Axis yAxis = lineChartModel.getAxis(AxisType.Y);
		yAxis.setLabel("NÚMERO DE BACKLOGS");
		yAxis.setMax(getMayor());
		yAxis.setTickInterval("5");
		
	}

	/***
	 * Compara los valores de los 3 estados en todos los meses
	 * 
	 * @return el numero mayor
	 */
	public int getMayor() {
		int mayor = Integer.MIN_VALUE;
		for (int valor : datosGraficas.getTotalGenerados()) {
			if (valor > mayor)
				mayor = valor;
		}
		for (int valor : datosGraficas.getTotalEjecutados()) {
			if (valor > mayor)
				mayor = valor;
		}
		for (int valor : datosGraficas.getTotalPendientes()) {
			if (valor > mayor)
				mayor = valor;
		}

		mayor += 5;
		while ((mayor % 5) != 0) {
			mayor++;
		}

		return mayor;
	}

	public LineChartModel getLineChartModel() {
		return lineChartModel;
	}

	public void setLineChartModel(LineChartModel lineChartModel) {
		this.lineChartModel = lineChartModel;
	}

}
