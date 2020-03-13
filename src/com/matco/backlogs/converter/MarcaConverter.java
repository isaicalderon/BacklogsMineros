package com.matco.backlogs.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.ejes.entity.Marca;
import com.matco.ejes.facade.MarcaFacade;

@FacesConverter("marcaConverter")
public class MarcaConverter extends GenericBacklogBean implements Converter {
	
	private static final long serialVersionUID = -6711624781028685790L;
	private String ruta_properties = getExternalContext().getInitParameter("admintx_matco");

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		MarcaFacade marcaFacade = new MarcaFacade(ruta_properties);
		Marca marca = new Marca();
		String[] separacion = value.split(" - ");
		if (value != null && value.trim().length() > 0) {
			try {
				marca = marcaFacade.obtenerMarcaPorId(separacion[0]);
				if (marca.getMarca() != null) {
					if (!marca.getMarca().equals("")) {
						return marca;
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
			String result = ((Marca)arg2).getMarcaDescripcionGrid();
			return result;
		}
		return "";
	}

}