package com.matco.backlogs.bean;

import java.util.Calendar;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

/**
 * Obtiene el año del sistema para mostrarlo en el template footer
 * @author N Soluciones de Software
 * @developer ialeman
 *
 */
@ManagedBean(name = "footerBean")
@ViewScoped
public class FooterBean {
	String actualYear = "";
	
	@PostConstruct
	public void init() {
		Calendar cal = Calendar.getInstance();
		actualYear = String.valueOf(cal.get(Calendar.YEAR));
	}

	public String getActualYear() {
		return actualYear;
	}

	public void setActualYear(String actualYear) {
		this.actualYear = actualYear;
	}
}
