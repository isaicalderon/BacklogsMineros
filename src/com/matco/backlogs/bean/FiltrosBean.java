package com.matco.backlogs.bean;

import java.util.List;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import com.matco.backlogs.entity.BacklogsMineros;

@ManagedBean(name = "filtrosBean")
@ViewScoped
public class FiltrosBean {

	private List<BacklogsMineros> backlogsMinerosListFiltrado;
	private List<BacklogsMineros> backlogsMinerosListFiltradoTemp;

	public List<BacklogsMineros> getBacklogsMinerosListFiltrado() {
		return backlogsMinerosListFiltrado;
	}

	public void setBacklogsMinerosListFiltrado(List<BacklogsMineros> backlogsMinerosListFiltrado) {
		this.backlogsMinerosListFiltrado = backlogsMinerosListFiltrado;
	}

	public List<BacklogsMineros> getBacklogsMinerosListFiltradoTemp() {
		return backlogsMinerosListFiltradoTemp;
	}

	public void setBacklogsMinerosListFiltradoTemp(List<BacklogsMineros> backlogsMinerosListFiltradoTemp) {
		this.backlogsMinerosListFiltradoTemp = backlogsMinerosListFiltradoTemp;
	}
}
