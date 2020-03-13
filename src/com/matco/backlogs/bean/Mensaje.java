package com.matco.backlogs.bean;

import java.io.Serializable;

public class Mensaje implements Serializable{

	private static final long serialVersionUID = 6627765211446087081L;
	private String summary;
	private String detail;
	
	public String getSummary() {
		return summary;
	}
	public void setSummary(String summary) {
		this.summary = summary;
	}
	public String getDetail() {
		return detail;
	}
	public void setDetail(String detail) {
		this.detail = detail;
	}

}
