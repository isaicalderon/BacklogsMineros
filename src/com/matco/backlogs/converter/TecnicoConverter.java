package com.matco.backlogs.converter;

import java.text.DecimalFormat;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.matco.servicio.entity.Tecnico;
import com.matco.servicio.facade.TecnicoFacade;

@FacesConverter("tecnicoConverter")
public class TecnicoConverter implements Converter {
	FacesContext facesContext = FacesContext.getCurrentInstance();
	private String ruta_properties_servicio = facesContext.getExternalContext().getInitParameter("admintx_servicio");

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		TecnicoFacade tecnicoFacade = new TecnicoFacade(ruta_properties_servicio);
		Tecnico tecnico = new Tecnico();
		String[] separacion = value.split(" - ");
		if (value != null && value.trim().length() > 0) {
			try {
				Integer claveNomina = Integer.valueOf(separacion[0]);
				tecnico = tecnicoFacade.obtenerTecnicoPorId(claveNomina);
				return tecnico;
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			Tecnico tecnicoResultado = ((Tecnico) object);
			if (tecnicoResultado.getClaveNomina() != null) {
				Integer idTecnico = tecnicoResultado.getClaveNomina();
				DecimalFormat formatoClaveNomina = new DecimalFormat("000000");
				String nombreTecnico = tecnicoResultado.getNombre();
				String result = nombreTecnico == null ? formatoClaveNomina.format(idTecnico)
						: formatoClaveNomina.format(idTecnico) + " - " + nombreTecnico;
				return result;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

}
