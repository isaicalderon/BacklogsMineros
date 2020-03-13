package com.matco.backlogs.controller;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.PropertyConfigurator;

public class Log4jInit extends HttpServlet {

	private static final long serialVersionUID = -5476139340801620487L;

	public void init() {
		String file = getInitParameter("log4j-init-file");
		if (file != null) {
			PropertyConfigurator.configure(file);
		}
	}
	
	@Override
	public void doGet(HttpServletRequest req, HttpServletResponse res) {
	}
}
