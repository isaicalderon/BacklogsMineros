package com.matco.backlogs.converter;

import java.util.ArrayList;
import java.util.List;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import com.matco.amce3.dto.MaquinariaDto;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.backlogs.bean.GenericBacklogBean;

@FacesConverter("maquinariaConverter")
public class MaquinariaConverter extends GenericBacklogBean implements Converter {

	private static final long serialVersionUID = 1771121031068724150L;
	private String ruta_properties = getExternalContext().getInitParameter("admintx_amce3");

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		BacklogsMinerosFacade modeloFacade = new BacklogsMinerosFacade(ruta_properties);
		MaquinariaDto maquinaria = new MaquinariaDto();
		if (value != null && value.trim().length() > 0) {
			try {
				Short idSucursal = obtenerSucursalFiltro();
				List<MaquinariaDto> model = new ArrayList<>();
				if(idSucursal==6) {
					short sucursal = 6;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
					sucursal = 9;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
					sucursal = 14;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
				}else if(idSucursal==13||idSucursal==2||idSucursal==1) {
					short sucursal = 1;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
					sucursal = 13;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
					sucursal = 2;
					model.addAll(modeloFacade.obtenerMaquinaria(sucursal));
					
					
				}
				for (MaquinariaDto maquinariaDto : model) {
					if(maquinariaDto.getSerie().equals(value)) {
						maquinaria = maquinariaDto;
						return maquinaria;
					}
				}
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

}