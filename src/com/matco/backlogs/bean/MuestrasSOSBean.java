package com.matco.backlogs.bean;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;

import org.apache.log4j.Logger;

import com.matco.amce2.dto.GetMueSosDto;
import com.matco.amce2.entity.SosAnormalidades;
import com.matco.amce2.facade.MuestrasFacade;
import com.matco.amce2.facade.SosAnormalidadesFacade;

@ManagedBean(name = "muestrasSOSBean")
@ViewScoped
public class MuestrasSOSBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = -3737304142415346658L;
	private static final Logger log = Logger.getLogger(MuestrasSOSBean.class);
	private BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
	private List<GetMueSosDto> muestrasSOSList = new ArrayList<GetMueSosDto>(0);
	private List<String> descripcionesAnormalidades = new ArrayList<>();

	private String error;
	private String summary = "Muestras SOS";

	@PostConstruct
	public void init() {
		seleccionBean.getMuestrasSeleccionadas().clear();
		Short sucursal = obtenerSucursalCorrespondiente();
		muestrasSOSList = listarMuestrasSOS(sucursal.intValue());
		descripcionesAnormalidades = anormalidadesList();
	}


	private List<GetMueSosDto> listarMuestrasSOS(Integer idLugarTrabajo) {
		MuestrasFacade muestrasFacade = new MuestrasFacade(RUTA_PROPERTIES_AMCE3);
		try {
			muestrasSOSList = muestrasFacade.obtenerMuestras(idLugarTrabajo);
		} catch (Exception e) {
			error = "No se pudieron obtener las Muetras SOS";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		if (muestrasSOSList.size() > 0) {
			for (GetMueSosDto muestraDto : muestrasSOSList) {
				if (muestraDto.getTipoInterpretacion().equals("COOLANT")) {
					String descripcionMuestra = "ANOMALÍA EN REFRIGERANTE";
					muestraDto.setDescripcionMuestra(descripcionMuestra);
				} else {
					String anormalidad = muestraDto.getAnormalidad();
					String descripcionMuestra = obtenerDescripcionMuestra(anormalidad);
					muestraDto.setDescripcionMuestra(descripcionMuestra);
				}
			}
		}
		return muestrasSOSList;
	}

	private String obtenerDescripcionMuestra(String anormalidad) {
		String descripcion = null;
		if (anormalidad == null) {
			descripcion = "";
		} else {
			anormalidad = anormalidad.trim();
			List<String> anormalidades = anormalidadesList();
			for (String sosAnormalidad : anormalidades) {
				sosAnormalidad = sosAnormalidad.trim();
				if (anormalidad.equals(sosAnormalidad)) {
					descripcion = sosAnormalidad;
					break;
				}
			}
		}
		return descripcion;
	}

	public List<String> anormalidadesList() {
		SosAnormalidadesFacade anormalidadesFacade = new SosAnormalidadesFacade(RUTA_PROPERTIES_AMCE3);
		descripcionesAnormalidades = new ArrayList<String>(0);
		try {
			List<SosAnormalidades> lista = anormalidadesFacade.obtenerSosAnormalidades();
			for (SosAnormalidades sosAnormalidades : lista) {
				String descripcionAnormalidad = sosAnormalidades.getDescripcion();
				descripcionesAnormalidades.add(descripcionAnormalidad);
			}
		} catch (Exception e) {
			error = "No se pudo obtener la lista de anormalidades";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return descripcionesAnormalidades;
	}

	public boolean filterByDate(Object value, Object filter, Locale locale) {
		DateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MMM/dd");
		String filterText = (filter == null) ? null : filter.toString().trim();
		if (filterText == null || filterText.isEmpty()) {
			return true;
		}
		if (value == null) {
			return false;
		}

		Date fechaGrid = (Date) value;
		Date fechaSeleccionada;

		fechaSeleccionada = (Date) filter;

		String seleccionada = simpleDateFormat.format(fechaSeleccionada);
		String grid = simpleDateFormat.format(fechaGrid);

		return seleccionada.equals(grid);
	}

	/**
	 * Obtiene el clientes formateado
	 * 
	 * @param cliente     Número de clientes a formatear
	 * @param razonSocial Razón Social del cliente
	 * @return Número de cliente y Razón social formateada
	 */
	public String obtenerClienteFormateado(Integer cliente, String razonSocial) {
		DecimalFormat decimalFormat = new DecimalFormat("000000");
		String clienteFormateado;
		if (razonSocial == null || razonSocial.length() == 0) {
			clienteFormateado = null;
		} else if (cliente == null || cliente == 0) {
			clienteFormateado = razonSocial;
		} else {
			clienteFormateado = decimalFormat.format(cliente) + " - " + razonSocial;
		}
		return clienteFormateado;
	}

	/**
	 * Filtra los días para el datatable de muestras de SOS
	 * 
	 * @param value  Valor a procesar
	 * @param filter Filtro a aplicar
	 * @param locale Indica el dato de localización
	 * @return Verdadero si el valor es mayor o igual al filtro
	 */
	public boolean filtroDias(Object value, Object filter, Locale locale) {
		boolean resultado = false;
		String filtro = (String) filter;
		String valor = String.valueOf(value);
		if (filtro == null || filtro.isEmpty()) {
			resultado = true;
		} else if (valor == null) {
			resultado = false;
		} else {
			try {
				Integer valorEntero = Integer.parseInt(valor);
				Integer filtroEntero = Integer.parseInt(filtro);
				resultado = (valorEntero >= filtroEntero);
			} catch (Exception e) {
				resultado = false;
			}
		}
		return resultado;
	}

	/**
	 * Obtiene el color de la celda en base a los días de sin atención
	 * 
	 * @param diasSinAtencion Número de días sin atención de una muestras de SOS
	 * @return Código CSS para cambiar el fondo de color en base a los días sin
	 *         atención
	 */
	public String colorCelda(int diasSinAtencion) {
		String colorCSS = "";
		if (diasSinAtencion >= 1 && diasSinAtencion < 7) {
			colorCSS = "background-color:#77DD77";
		} else if (diasSinAtencion >= 7 && diasSinAtencion < 14) {
			colorCSS = "background-color:#FDFD96";
		} else if (diasSinAtencion >= 14 && diasSinAtencion < 21) {
			colorCSS = "background-color:#FFBF00";
		} else if (diasSinAtencion >= 21) {
			colorCSS = "background-color:#FF6961";
		}
		return colorCSS;
	}

	public List<GetMueSosDto> getMuestrasSOSList() {
		return muestrasSOSList;
	}

	public void setMuestrasSOSList(List<GetMueSosDto> muestrasSOSList) {
		this.muestrasSOSList = muestrasSOSList;
	}

	public List<String> getDescripcionesAnormalidades() {
		return descripcionesAnormalidades;
	}

	public void setDescripcionesAnormalidades(List<String> descripcionesAnormalidades) {
		this.descripcionesAnormalidades = descripcionesAnormalidades;
	}

}
