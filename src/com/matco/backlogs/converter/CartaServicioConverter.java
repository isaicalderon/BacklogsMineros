package com.matco.backlogs.converter;

import java.util.List;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.matco.amce3.facade.CartasServicioFacade;
import com.matco.backlogs.bean.GenericBacklogBean;
import com.matco.backlogs.entity.CartasServicio;

@FacesConverter("cartaServicioConverter")
public class CartaServicioConverter extends GenericBacklogBean implements Converter {

	private static final long serialVersionUID = 7692574743805055639L;

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		CartasServicioFacade marcaFacade = new CartasServicioFacade(RUTA_PROPERTIES_AMCE3);
		CartasServicio carta = new CartasServicio();
		String[] separacion2 = value.split("-");
		String[] separacion = separacion2[0].split("_");
		if (value != null && value.trim().length() > 0) {
			try {
				List<CartasServicio> cartasServicio = marcaFacade.obtenerCartasServicioPorModelo(separacion[0]);
				for (CartasServicio serviceLetter : cartasServicio) {
					if (serviceLetter != null) {
						String idMarca = serviceLetter.getIdMarca().getMarca();
						String modelo = serviceLetter.getModeloAplica().getModeloKey().getModelo();
						String serie = serviceLetter.getSerieAplica();
						if (!idMarca.trim().equals("") && !modelo.trim().equals("") && !serie.trim().equals("")) {
							if (idMarca.equals(separacion[2].trim()) && modelo.equals(separacion[0].trim())
									&& serie.equals(separacion[1].trim())) {
								carta = serviceLetter;
								return carta;
							}
						}
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
			CartasServicio serviceLetter = ((CartasServicio) arg2);
			if (serviceLetter != null) {
				String idMarca = serviceLetter.getIdMarca().getMarca();
				String modelo = serviceLetter.getModeloAplica().getModeloKey().getModelo();
				String serie = serviceLetter.getSerieAplica();
				String result = modelo + "_" + serie + "_" + idMarca + " - "
						+ serviceLetter.getDescripcionCartasServicio();
				return result;
			}
		}
		return "";
	}

}