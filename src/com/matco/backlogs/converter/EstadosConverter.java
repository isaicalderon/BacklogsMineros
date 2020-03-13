package com.matco.backlogs.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.primefaces.component.picklist.PickList;
import org.primefaces.model.DualListModel;

import com.matco.backlogs.entity.EstatusBacklogsMineros;

@FacesConverter("EstadosConverter")
public class EstadosConverter implements Converter{

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {

		Object returnData = null;
		
		if (arg1 instanceof PickList) {
			Object dualList = ((PickList) arg1).getValue();
			
			@SuppressWarnings("rawtypes")
			DualListModel dl = (DualListModel) dualList;
			for (Object ob : dl.getSource()) {
				String id = "" +((EstatusBacklogsMineros) ob).getIdEstatusBacklogMinero();
				if (arg2.equals(id)) {
					returnData = ob;
					break;
				}
			}
			
			if (returnData == null) {
				for (Object ob : dl.getTarget()) {
	                String id = "" + ((EstatusBacklogsMineros) ob).getIdEstatusBacklogMinero();
	                if (arg2.equals(id)) {
	                	returnData = ob;
	                    break;
	                }
	            }
			}
		}
		return returnData;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		String str = "";
	    if (arg2 instanceof EstatusBacklogsMineros) {
	        str = "" + ((EstatusBacklogsMineros) arg2).getIdEstatusBacklogMinero();
	    }
	    return str;
	}

}
