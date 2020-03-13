package com.matco.backlogs.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.ejes.entity.Modelo;
import com.matco.ejes.facade.ModeloFacade;

@FacesConverter("modeloNormalConverter")
public class ModeloNoMarcaConverter extends GenericBacklogBean implements Converter {
	
	private static final long serialVersionUID = 420095281350770728L;
	private String ruta_properties = getExternalContext().getInitParameter("admintx_matco");

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		ModeloFacade modeloFacade = new ModeloFacade(ruta_properties);
		Modelo modelo = new Modelo();
		String[] separacion = value.split(" - ");
		if (value != null && value.trim().length() > 0) {
			try {
				List<Modelo> modelList = modeloFacade.obtenerTodosModelos();
				for (Modelo model : modelList) {
					if (model.getModeloKey().getModelo().equals(separacion[0])) {
						modelo = model;
						return modelo;
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		if (arg2 != null) {
			String result = ((Modelo) arg2).getModeloDescripcionGrid();
			return result;
		}
		return "";
	}

}