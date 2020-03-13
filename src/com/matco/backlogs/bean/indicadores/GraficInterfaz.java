package com.matco.backlogs.bean.indicadores;

import java.io.Serializable;

import org.primefaces.model.chart.CartesianChartModel;

/**
 * 
 * @author N Soluciones de Software
 *
 */
public abstract class GraficInterfaz extends GenericGraficBean implements Serializable {
	
	private static final long serialVersionUID = 5704073071823287161L;

	public abstract void buscar();
	
	public abstract CartesianChartModel initModels();
	
}
