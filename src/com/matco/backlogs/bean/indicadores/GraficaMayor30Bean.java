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
 * Visualizar gráfica de backlogs atrasados mas de 30 dias
 * 
 * @author N soluciones de software
 *
 */
@ManagedBean(name = "graficaMayor30Bean")
public class GraficaMayor30Bean extends GraficInterfaz implements Serializable {
	private static final long serialVersionUID = -8671962472613852285L;
	private static final Logger log = Logger.getLogger(GraficaMayor30Bean.class);
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
		if (seleccionBean.getAuxiliarGrafica() == 3 || seleccionBean.getAuxiliarGrafica() == 0) {
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
		datosGraficas.setBacklogsMayor30(dataTemp);

	}

	@Override
	public LineChartModel initModels() {
		boolean dialogoGrafica = seleccionBean.isDialogGrafica();
		LineChartModel model = new LineChartModel();

		try {
			if (dialogoGrafica == false) {
				String serie = genericGraficBean.getMaquinaria();
				String modelo = genericGraficBean.getModelo();
				datosGraficas = graficasFacade.obtenerTotalBackLogsAtrasados(fecha1, fecha2, serie, modelo, almacen);
			}else {
				obtenerValores();
			}
		} catch (Exception e) {
			detalle = "No se pudo obtener información para las gráficas ";
			log.error(detalle, e);
			agregarMensajeError(summary, detalle);
		}

		ChartSeries blMay30 = new ChartSeries();
		blMay30.setLabel("BL > 30");
		crearColumnas(blMay30, fecha1, fecha2, datosGraficas.getBacklogsMayor30());

		model.addSeries(blMay30);
		
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
		for (Integer val : datosGraficas.getBacklogsMayor30()) {
			if (valores.equals("[")) {
				valores = valores + "" + val;
			} else {
				valores = valores + "," + val;
			}
		}

		valores = valores + "]";
		genericGraficBean.setValores(valores);
	}

	@SuppressWarnings("deprecation")
	public void createLineModels() {
		lineChartModel = initModels();
		lineChartModel.setTitle("BACKLOGS > 30 DIAS");
		lineChartModel.setLegendPosition("s");
		lineChartModel.setLegendPlacement(LegendPlacement.OUTSIDEGRID);
		lineChartModel.setShowPointLabels(true);
		lineChartModel.getAxes().put(AxisType.X, new CategoryAxis("Meses-Año"));

		if ((fecha2.getYear() - fecha1.getYear()) >= 1) {
			lineChartModel.getAxis(AxisType.X).setTickAngle(-40);
		}

		Axis yAxis = lineChartModel.getAxis(AxisType.Y);
		yAxis.setLabel("BACKLOGS > 30 DIAS (%)");
		//yAxis.setMax(getMaximo());
		yAxis.setTickInterval("10");
	}

	public int getMaximo() {
		int intervalo = Integer.parseInt(getIntervalo());
		int mayor = Integer.MIN_VALUE;
		for (int valor : datosGraficas.getBacklogsMayor30()) {
			if (valor > mayor)
				mayor = valor;
		}
		int maximo;
		if (mayor < 100) {
			maximo = 100;
		} else {
			maximo = (int) mayor;
			maximo = maximo / intervalo + 2;
			maximo *= intervalo;
		}
		return maximo;
	}

	public LineChartModel getLineChartModel() {
		return lineChartModel;
	}

	public void setLineChartModel(LineChartModel lineChartModel) {
		this.lineChartModel = lineChartModel;
	}

}
