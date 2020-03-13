package com.matco.backlogs.converter;

import javax.faces.convert.Converter;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

import com.matco.ejes.entity.Cliente;
import com.matco.ejes.facade.ClienteFacade;

@FacesConverter("clienteConverter")
public class ClienteConverter implements Converter {

	public Object getAsObject(FacesContext arg0, UIComponent arg1, String value) {
		ClienteFacade clienteFacade = new ClienteFacade("admintx_matco.properties");
		Cliente cliente = new Cliente();
		String[] separacion = value.split(" - ");
		
		if (value != null && value.trim().length() > 0) {
			try {
				Integer idCliente = Integer.parseInt(separacion[0]);
				Cliente clienteList = clienteFacade.obtenerClientePorId(idCliente);
					if (clienteList.getCliente().compareTo(idCliente) == 0) {
						cliente = clienteList;
					}
				return cliente;
			} catch (Exception e) {

			}
		}
		return null;
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		Cliente clienteResultado = ((Cliente) object);
		String idCliente = clienteResultado.getCodigoFormateado();
		String razonSocial = clienteResultado.getRazonSocial();
		String result = razonSocial == null ? idCliente : idCliente + " - " + razonSocial;
		return result;
	}
}
