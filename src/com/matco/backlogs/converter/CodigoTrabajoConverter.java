package com.matco.backlogs.converter;

import java.util.ArrayList;
import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import org.apache.log4j.Logger;

import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.backlogs.bean.GenericInspectBean;
import com.matco.backlogs.entity.CodigosSMCS;

@FacesConverter("codigoTrabajoConverter")
public class CodigoTrabajoConverter extends GenericInspectBean implements Converter {
	
	private static final long serialVersionUID = 3346240773690596313L;
	private static final Logger log = Logger.getLogger(CodigoTrabajoConverter.class);
	private CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		if (arg2 == null || arg2.trim().isEmpty()) {
			return null;
		}
		String codigoTrabajoDescripcion = arg2.trim();
		String[] codigoTrabajoSplit = codigoTrabajoDescripcion.split("-");
		String codigoTrabajoObtenido = removerCeros(codigoTrabajoSplit[0]).trim();
		String trabajoRealizar = codigoTrabajoDescripcion.split("-")[1].trim();
		int codigoTrabajo = Integer.parseInt(codigoTrabajoObtenido);
		CodigosSMCS resultado = null;
		List<CodigosSMCS> listaCodigosTrabajo = new ArrayList<>();
		try {
			listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		} catch (Exception e) {
			log.error(e);
		}

		for (CodigosSMCS codigoTrabajoComparado : listaCodigosTrabajo) {
			int codigo = codigoTrabajoComparado.getCodigoSMCS();
			String descripcion = codigoTrabajoComparado.getDescripcionSMCS();
			if (codigoTrabajo == codigo && descripcion.equals(trabajoRealizar)) {
				resultado = codigoTrabajoComparado;
				break;
			}
		}

		return resultado;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		if (arg2 == null || arg2.toString().trim().equals("")) {
			return "";
		}
		// CodigosSMCS resultado = (CodigosSMCS) arg2;
		return arg2.toString();
	}

	private String removerCeros(String s) {
		s = s.replaceAll("^[0]+", "");
		if (s.equals("")) {
			return "0";
		}
		return s;
	}
}
