package com.matco.backlogs.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.faces.bean.ManagedBean;
import org.primefaces.context.RequestContext;
import com.matco.amce3.dto.MaquinariaDto;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.amce3.facade.CodigosSMCSFacade;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.ejes.entity.Cliente;
import com.matco.ejes.entity.Marca;
import com.matco.ejes.entity.Modelo;
import com.matco.ejes.facade.ClienteFacade;
import com.matco.ejes.facade.MarcaFacade;
import com.matco.ejes.facade.ModeloFacade;
import com.matco.servicio.entity.Tecnico;
import com.matco.servicio.facade.TecnicoFacade;


/**
 * Define los métodos comunes en varias vistas para autocompletar los datos
 * requeridos por el componente p:autocomplete
 * 
 * @author N Soluciones de Software
 */

@ManagedBean
public class AutoCompleteBean extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = 3372246169222995724L;

	public List<String> completeTecnico(String query) throws Exception {
		TecnicoFacade tecnicoFacade = new TecnicoFacade(RUTA_PROPERTIES_SERVICIO);
		
		List<Tecnico> tecnicoList = tecnicoFacade.obtenerTodosTecnicos();
		
		String buscadorTecnico = query.toLowerCase();
		
		List<String> listaAutocompleteTecnico = new ArrayList<>();
		
		for (Tecnico tecnicoAuxiliar : tecnicoList) {

			// Para evaluar todos los encuentros con el nombre del tecnico que
			// se intenta
			// encontrar
			String nombreTecnico = tecnicoAuxiliar.getNombre();

			// Evitar sobrecargar el if con otro má©todo para el string.
			String nombreTecnicoMinuscula = nombreTecnico == null ? "" : nombreTecnico.toLowerCase();

			String idTecnico = tecnicoAuxiliar.getClaveNominaFormateada();

			if (nombreTecnicoMinuscula.contains(buscadorTecnico) || idTecnico.contains(buscadorTecnico)) {
				String resultado = nombreTecnico == null ? idTecnico : idTecnico + " - " + nombreTecnico;
				listaAutocompleteTecnico.add(resultado);
			}
		}
		return listaAutocompleteTecnico;
	}

	public List<String> completeNumeroSerie(String query) throws Exception {
		Short idSucursal = 6;
		BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
		List<MaquinariaDto> maquinariaList = backlogsMinerosFacade.obtenerMaquinariaAutocomplete(idSucursal, query);
		String buscadorNumeroSerie = query.toLowerCase();
		List<String> listaAutocompleteModelo = new ArrayList<>();
		for (MaquinariaDto maquinariaAuxiliar : maquinariaList) {

			// Para evaluar todos los encuentros con el nombre del número de serie que
			// se intenta
			// encontrar
			String numeroSerie = maquinariaAuxiliar.getSerie();

			// Evitar sobrecargar el if con otro má©todo para el string.
			String numeroSerieMinuscula = numeroSerie == null ? "" : numeroSerie.toLowerCase();

			if (numeroSerieMinuscula.contains(buscadorNumeroSerie)) {
				String resultado = numeroSerie;
				listaAutocompleteModelo.add(resultado);
			}
		}
		return listaAutocompleteModelo;
	}

	public List<String> completeModelo(String query) throws Exception {
		ModeloFacade modeloFacade = new ModeloFacade(RUTA_PROPERTIES_MATCO);
		List<Modelo> modeloList = modeloFacade.obtenerTodosModelos();
		String buscadorModelo = query.toLowerCase();
		List<String> listaAutocompleteModelo = new ArrayList<>();
		for (Modelo modeloAuxiliar : modeloList) {

			// Para evaluar todos los encuentros con la descripcion del modelo que
			// se intenta
			// encontrar
			String descripcionModelo = modeloAuxiliar.getDescripcionModelo();

			// Evitar sobrecargar el if con otro má©todo para el string.
			String descripcionModeloMinuscula = descripcionModelo == null ? "" : descripcionModelo.toLowerCase();

			String idModelo = modeloAuxiliar.getModeloKey().getModelo();
			String idModeloMinuscula = idModelo == null ? "" : idModelo.toLowerCase();

			if (descripcionModeloMinuscula.contains(buscadorModelo) || idModeloMinuscula.contains(buscadorModelo)) {
				String resultado = descripcionModelo == null ? idModelo : idModelo + " - " + descripcionModelo;
				listaAutocompleteModelo.add(resultado);
			}
		}
		return listaAutocompleteModelo;
	}

	@SuppressWarnings("deprecation")
	public List<String> completeSubirCartaModelo(String query) throws Exception {
		ModeloFacade modeloFacade = new ModeloFacade(RUTA_PROPERTIES_MATCO);
		BacklogsStaticsVarBean seleccionBean = this.obtenerBean("backlogsStaticsVarBean");
		Marca marca = seleccionBean.getIdMarca();
		if (marca != null) {
			List<Modelo> modeloList = modeloFacade.obtenerModelosPorMarca(marca, query);
			String buscadorModelo = query.toLowerCase();
			List<String> listaAutocompleteModelo = new ArrayList<>();
			for (Modelo modeloAuxiliar : modeloList) {

				// Para evaluar todos los encuentros con la descripcion del modelo que
				// se intenta
				// encontrar
				String descripcionModelo = modeloAuxiliar.getDescripcionModelo();

				// Evitar sobrecargar el if con otro má©todo para el string.
				String descripcionModeloMinuscula = descripcionModelo == null ? "" : descripcionModelo.toLowerCase();

				String idModelo = modeloAuxiliar.getModeloKey().getModelo();
				String idModeloMinuscula = idModelo == null ? "" : idModelo.toLowerCase();

				if (descripcionModeloMinuscula.contains(buscadorModelo) || idModeloMinuscula.contains(buscadorModelo)) {
					String resultado = descripcionModelo == null ? idModelo : idModelo + " - " + descripcionModelo;
					listaAutocompleteModelo.add(resultado);
				}
			}
			return listaAutocompleteModelo;
		} else {
			agregarMensajeWarn("Cartas Servicio", "Se necesita una Marca para poder ingresar un Modelo.");
			RequestContext.getCurrentInstance().update("growl");
			//PrimeFaces.current().ajax().update("growl");
			
			return new ArrayList<>();
		}
	}

	/***
	 * Obtiene la lista de elementos posibles del autocomplete de código de trabajo al registrar un backlog
	 * @return lista de código de trabajo - trabajo a relizar que coincidan con lo escrito
	 * @throws Exception 
	 */
	public List<String> completeCodigoTrabajo(String query) throws Exception{
		List<String> listaAutoCompleteCodigoTrabajo = new ArrayList<>();
		if(query.trim().equals("")) {
			return listaAutoCompleteCodigoTrabajo;
		}
		query=query.toUpperCase();
		CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3); 
		List<CodigosSMCS> listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		for(CodigosSMCS codigoTrabajo:listaCodigosTrabajo) {
			String codigo = codigoTrabajo.getCodigosSMCSFormateado();
			String descripcion = codigoTrabajo.getDescripcionSMCS();
			String codigoDescripcion = codigo + " - "+descripcion;
			if(codigoDescripcion.contains(query)) {
				listaAutoCompleteCodigoTrabajo.add(codigoDescripcion);
			}
		}
		return listaAutoCompleteCodigoTrabajo;
	}
	
	/***
	 * Obtiene la lista de elementos posibles del autocomplete de código de trabajo al registrar un backlog
	 * @return lista de código de trabajo - trabajo a relizar que coincidan con lo escrito
	 * @throws Exception 
	 */
	public List<CodigosSMCS> completeCodigoTrabajoObj(String query) throws Exception{
		List<CodigosSMCS> listaAutoCompleteCodigoTrabajo = new ArrayList<>();
		if(query.trim().equals("")) {
			return listaAutoCompleteCodigoTrabajo;
		}
		query=query.toUpperCase();
		CodigosSMCSFacade codigosTrabajoFacade = new CodigosSMCSFacade(RUTA_PROPERTIES_AMCE3); 
		List<CodigosSMCS> listaCodigosTrabajo = codigosTrabajoFacade.obtenerTodosCodigosSMCS();
		for(CodigosSMCS codigoTrabajo:listaCodigosTrabajo) {
			String codigo = codigoTrabajo.getCodigosSMCSFormateado();
			String descripcion = codigoTrabajo.getDescripcionSMCS();
			String codigoDescripcion = codigo + " - "+descripcion;
			if(codigoDescripcion.contains(query)) {
				listaAutoCompleteCodigoTrabajo.add(codigoTrabajo);
			}
		}
		return listaAutoCompleteCodigoTrabajo;
	}
	
	
	public List<String> completeMarca(String query) throws Exception {
		MarcaFacade marcaFacade = new MarcaFacade(RUTA_PROPERTIES_MATCO);
		List<Marca> marcaList = marcaFacade.obtenerMarcasPorIdODescripcion(query);
		String buscadorMarca = query.toLowerCase();
		List<String> listaAutocompleteMarca = new ArrayList<>();
		for (Marca marcaAuxiliar : marcaList) {

			// Para evaluar todos los encuentros con la descripcion de la marca que
			// se intenta
			// encontrar
			String descripcionMarca = marcaAuxiliar.getDescripcionMarca();

			// Evitar sobrecargar el if con otro má©todo para el string.
			String descripcionMarcaMinuscula = descripcionMarca == null ? "" : descripcionMarca.toLowerCase();

			String idMarca = marcaAuxiliar.getMarca();
			String idMarcaMinuscula = idMarca == null ? "" : idMarca.toLowerCase();

			if (descripcionMarcaMinuscula.contains(buscadorMarca) || idMarcaMinuscula.contains(buscadorMarca)) {
				String resultado = descripcionMarca == null ? idMarca : idMarca + " - " + descripcionMarca;
				listaAutocompleteMarca.add(resultado);
			}
		}
		return listaAutocompleteMarca;
	}

	public List<String> completeCliente(String query) throws Exception {
		ClienteFacade clienteFacade = new ClienteFacade(RUTA_PROPERTIES_MATCO);
		String buscadorCliente = query.toLowerCase();
		List<Cliente> clienteList = clienteFacade.obtenerTodosClientes(query);
		List<String> listaAutocompleteCliente = new ArrayList<>();
		for (Cliente clienteAuxiliar : clienteList) {
			// Para evaluar todos los encuentros con la razón social que se intenta
			// encontrar
			String razonSocialCliente = clienteAuxiliar.getRazonSocial();
			// Evita sobrecargar el if con otro método para el string.
			String razonSocialClienteMinuscula = razonSocialCliente == null ? "" : razonSocialCliente.toLowerCase();
			String idClienteFormateado = clienteAuxiliar.getCodigoFormateado();
			if (razonSocialClienteMinuscula.contains(buscadorCliente)
					|| idClienteFormateado.contains(buscadorCliente)) {
				String resultado = razonSocialCliente == null ? idClienteFormateado
						: idClienteFormateado + " - " + razonSocialCliente;
				listaAutocompleteCliente.add(resultado);
			}
		}
		return listaAutocompleteCliente;
	}
	
	

}
