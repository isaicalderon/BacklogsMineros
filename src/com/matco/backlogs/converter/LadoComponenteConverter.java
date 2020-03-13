package com.matco.backlogs.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.backlogs.entity.LadoComponente;

@FacesConverter("ladoComponenteConverter")
public class LadoComponenteConverter extends GenericBacklogBean implements Converter {
	
	private static final long serialVersionUID = 2123390729598610447L;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null || arg2.trim().isEmpty()) {
			return null;
		}
		/*
		LadoComponenteFacade ldFacade = new LadoComponenteFacade(RUTA_PROPERTIES_AMCE3);
		*/
		String ladoComponenteFormateado = arg2.trim();
		String[] splitLD = ladoComponenteFormateado.split("-");
		
		String idCodigoLD = splitLD[0];
		String descripcionLD = splitLD[1];
		
		LadoComponente ladoComponente = new LadoComponente();
		ladoComponente.setCodigoLDC(idCodigoLD);
		ladoComponente.setDescripcion(descripcionLD);
		
		return ladoComponente;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		if (arg2 == null || arg2.toString().trim().equals("")) {
			return "";
		}
		return arg2.toString();
	}

	
}
