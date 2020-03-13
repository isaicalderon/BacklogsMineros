package com.matco.backlogs.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.backlogs.dto.InspeccionCatDto;

@FacesConverter("inspeccionConverter")
public class InspeccionConverter extends GenericBacklogBean implements Converter {

	private static final long serialVersionUID = -2090788575679569713L;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null || arg2.trim().isEmpty()) {
			return null;
		}

		List<InspeccionCatDto> lista = seleccionBean.getInspeccionesSeleccionadas();
		InspeccionCatDto nvo1 = new InspeccionCatDto();
		for (InspeccionCatDto inspeccion : lista) {
			if (arg2.equals(inspeccion.getInspeccionDto().getFolio())) {
				nvo1 = inspeccion;
				break;
			}
		}

		return nvo1;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		if (arg2 == null) {
			return "";
		}
		InspeccionCatDto nvo1 = (InspeccionCatDto) arg2;
		return nvo1.getInspeccionDto().getFolio();
	}
}