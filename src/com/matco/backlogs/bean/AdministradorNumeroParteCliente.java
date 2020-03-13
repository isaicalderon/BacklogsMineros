package com.matco.backlogs.bean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.sql.SQLIntegrityConstraintViolationException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.hssf.OldExcelFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.primefaces.PrimeFaces;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.UploadedFile;

import com.matco.amce3.facade.NumeroParteClientesFacade;
import com.matco.backlogs.entity.NumeroParteClientes;
import com.matco.backlogs.entity.key.NumeroParteClientesKey;
import com.matco.ejes.entity.Cliente;
import com.matco.ejes.entity.Marca;
import com.matco.ejes.facade.ClienteFacade;
import com.matco.ejes.facade.MarcaFacade;

@ManagedBean(name = "administradorNumeroParteCliente")
@ViewScoped
public class AdministradorNumeroParteCliente extends GenericBacklogBean implements Serializable {

	private static final long serialVersionUID = -9009028771765899420L;
	private static final Logger log = Logger.getLogger(AdministradorNumeroParteCliente.class);
	private LoginBean loginBean;
	private List<NumeroParteClientes> numeroParteClientesList;
	private List<NumeroParteClientes> numeroParteClientesExcelList = new ArrayList<>();
	private NumeroParteClientes numeroParteClientesSeleccionado;
	private String numeroParteMatco;
	private String numeroParteMatcoAnterior;
	private Cliente idCliente;
	private Marca idMarca;
	private String numeroParteCliente;
	private String usuario;
	private boolean habilitarModificar = true;
	private boolean habilitarMarca = false;
	private boolean habilitarCliente = false;
	private String error;
	private PrimeFaces pf = PrimeFaces.current();
	NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
	ClienteFacade clienteFacade = new ClienteFacade(RUTA_PROPERTIES_MATCO);
	MarcaFacade marcaFacade = new MarcaFacade(RUTA_PROPERTIES_MATCO);

	@PostConstruct
	public void init() {
		numeroParteClientesList = listarNumeroParteCliente();
		this.loginBean = this.obtenerBean("loginBean");
		usuario = this.loginBean.getUsuario() != null ? this.loginBean.getUsuario().getUsuario() : "DESARROLLO";
	}

	public void agregarNumeroParteCliente() {
		if (idMarca == null) {
			agregarMensajeWarn(summary, "El campo 'Marca' está vacío y se requiere para continuar");
		}
		if (idCliente == null) {
			agregarMensajeWarn(summary, "El campo 'Cliente' está vacío y se requiere para continuar");
		}
		if (numeroParteCliente == null || numeroParteCliente.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Cliente' está vacío y se requiere para continuar");
		}

		if (numeroParteMatco == null || numeroParteMatco.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Matco' está vacío y se requiere para continuar");
		}

		if (idMarca == null || idCliente == null || numeroParteCliente == null || numeroParteMatco == null
				|| numeroParteCliente.trim().equals("") || numeroParteMatco.trim().equals("")) {
			return;
		}

		NumeroParteClientes numeroParteClientes = new NumeroParteClientes();

		NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);

		numeroParteClientes.setNumeroParteCliente(numeroParteCliente.toUpperCase());

		numeroParteMatco = rellenarNumeroParteMatco(numeroParteMatco.toUpperCase());

		NumeroParteClientesKey numeroParteClientesKey = new NumeroParteClientesKey(numeroParteMatco, idMarca,
				idCliente);

		numeroParteClientes.setNumeroParteClientesKey(numeroParteClientesKey);
		numeroParteClientes.setCreadoPor(usuario);
		try {
			numeroParteClientesFacade.guardarNumeroParteClientes(numeroParteClientes);
			agregarMensajeInfo(summary,
					"Se ha agregado el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente());
			pf.executeScript("PF('altaNPC').hide();");

		} catch (SQLIntegrityConstraintViolationException ex) {
			error = "El Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente()
					+ " ya se encuentra registrado en el sistema";
			log.warn(error, ex);
			agregarMensajeWarn(summary, error);
			return;
		} catch (Exception e) {
			error = "No se pudo agregar el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		limpiarCampos();
	}

	/**
	 * Agrega un numero parte cliente proviniente de un Excel
	 * 
	 * @param numeroParteClientes
	 */
	public void agregarNumeroParteCliente(NumeroParteClientes numeroParteClientes) {
		try {
			numeroParteClientes.setCreadoPor(usuario);
			numeroParteClientesFacade.guardarNumeroParteClientes(numeroParteClientes);
			agregarMensajeInfo(summary,
					"Se ha agregado el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente());
			pf.executeScript("PF('altaNPC').hide();");

		} catch (SQLIntegrityConstraintViolationException ex) {
			error = "El Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente()
					+ " ya se encuentra registrado en el sistema";
			log.warn(error, ex);
			agregarMensajeWarn(summary, error);
			return;
		} catch (Exception e) {
			error = "No se pudo agregar el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente();
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		limpiarCampos();
	}

	/***
	 * Edita el número parte cliente, de un registro donde coincida el numero parte
	 * matco, marca y cliente, con el registro seleccionado
	 */
	public void editarNumeroParteCliente() {
		if (idMarca == null) {
			agregarMensajeWarn(summary, "El campo 'Marca' está vacío y se requiere para continuar");
		}
		if (idCliente == null) {
			agregarMensajeWarn(summary, "El campo 'Cliente' está vacío y se requiere para continuar");
		}
		if (numeroParteCliente == null || numeroParteCliente.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Cliente' está vacío y se requiere para continuar");
		}

		if (numeroParteMatco == null || numeroParteMatco.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Matco' está vacío y se requiere para continuar");
		}

		if (idMarca == null || idCliente == null || numeroParteCliente == null || numeroParteMatco == null
				|| numeroParteCliente.trim().equals("") || numeroParteMatco.trim().equals("")) {
			return;
		}

		NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		NumeroParteClientes numeroParteClientes = new NumeroParteClientes();
		numeroParteClientes.setNumeroParteCliente(numeroParteCliente.toUpperCase());
		NumeroParteClientesKey numeroParteClientesKey = new NumeroParteClientesKey(numeroParteMatco, idMarca,
				idCliente);
		numeroParteClientes.setNumeroParteClientesKey(numeroParteClientesKey);
		numeroParteClientes.setModificadoPor(usuario);
		// numeroParteClientes.setFechaHoraModificacion(fechaHoraModificacion);

		try {
			numeroParteClientesFacade.editarNumeroParteClientes(numeroParteClientes);
			agregarMensajeInfo(summary,
					"Se ha modificado el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente());
			pf.executeScript("PF('editarNPC').hide();");
		} catch (Exception e) {
			error = "No se ha podido modificar el Número Parte Cliente: " + numeroParteClientes.getNumeroParteCliente();
			log.error(error, e);
			agregarMensajeError(summary, error);
			return;
		}
		limpiarCampos();
	}

	/***
	 * Edita el número parte matco, de un registro donde coincida el numero parte
	 * cliente, marca y cliente, con el registro seleccionado
	 */
	public void editarNumeroParteMatco() {
		if (idMarca == null) {
			agregarMensajeWarn(summary, "El campo 'Marca' está vacío y se requiere para continuar");
		}
		if (idCliente == null) {
			agregarMensajeWarn(summary, "El campo 'Cliente' está vacío y se requiere para continuar");
		}
		if (numeroParteCliente == null || numeroParteCliente.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Cliente' está vacío y se requiere para continuar");
		}

		if (numeroParteMatco == null || numeroParteMatco.trim().equals("")) {
			agregarMensajeWarn(summary, "El campo 'Número Parte Matco' está vacío y se requiere para continuar");
		}

		if (idMarca == null || idCliente == null || numeroParteCliente == null || numeroParteMatco == null
				|| numeroParteCliente.trim().equals("") || numeroParteMatco.trim().equals("")) {
			return;
		}

		NumeroParteClientes numeroParteClientes = new NumeroParteClientes();
		NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		numeroParteClientes.setNumeroParteCliente(numeroParteCliente);
		numeroParteMatco = rellenarNumeroParteMatco(numeroParteMatco.toUpperCase());
		NumeroParteClientesKey numeroParteClientesKey = new NumeroParteClientesKey(numeroParteMatco, idMarca,
				idCliente);
		numeroParteClientesKey.setNumeroParteMatcoAnterior(numeroParteMatcoAnterior.toUpperCase());
		numeroParteClientes.setNumeroParteClientesKey(numeroParteClientesKey);
		numeroParteClientes.setModificadoPor(usuario);
		try {
			numeroParteClientesFacade.editarNumeroParteMatco(numeroParteClientes);
			agregarMensajeInfo(summary,
					"Se ha modificado el Número Parte Matco de " + numeroParteMatcoAnterior + " a " + numeroParteMatco);

			pf.executeScript("PF('editarNumeroParteMatco').hide();");

		} catch (Exception e) {
			error = "No se ha podido modificar el Número Parte Matco: " + numeroParteMatcoAnterior;
			log.error(error, e);
			agregarMensajeError(summary, error);
			return;
		}
		// if(vengoDelExcel == false) {
		limpiarCampos();
		// }else {
		// numeroParteClientesList = listarNumeroParteCliente();
		// }
	}

	/**
	 * Edita el número parte matco proviniente del cargado de Excel
	 * 
	 * @param numeroParteCliente
	 */
	public void editarNumeroParteMatco(NumeroParteClientes numeroParteClienteExcel) {

		try {
			NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
			numeroParteClienteExcel.setModificadoPor(usuario);
			numeroParteClientesFacade.editarNumeroParteMatco(numeroParteClienteExcel);
			agregarMensajeInfo(summary,
					"Se ha modificado el Número Parte Matco de "
							+ numeroParteClienteExcel.getNumeroParteClientesKey().getNumeroParteMatcoAnterior() + " a "
							+ numeroParteClienteExcel.getNumeroParteClientesKey().getNumeroParteMatco());

			// pf.executeScript("PF('editarNumeroParteMatco').hide();");

		} catch (Exception e) {
			error = "No se ha podido modificar el Número Parte Matco: " + numeroParteMatcoAnterior;
			log.error(error, e);
			agregarMensajeError(summary, error);
			return;
		}
	}

	/***
	 * Rellena el numero parte matco con ceros hasta que tenga 7 caracteres
	 * 
	 * @param numeroParteMatco numero parte a rellenar
	 * @return numero parte matco formateado
	 */
	public String rellenarNumeroParteMatco(String numeroParteMatco) {
		while (numeroParteMatco.length() < 7) {
			numeroParteMatco = "0" + numeroParteMatco;
		}
		return numeroParteMatco;
	}

	public void seleccionarNPC() {
		habilitarModificar = false;
		idCliente = numeroParteClientesSeleccionado.getNumeroParteClientesKey().getIdCliente();
		idMarca = numeroParteClientesSeleccionado.getNumeroParteClientesKey().getIdMarca();
		numeroParteCliente = numeroParteClientesSeleccionado.getNumeroParteCliente();
		numeroParteMatco = numeroParteClientesSeleccionado.getNumeroParteClientesKey().getNumeroParteMatco();
		// String npmTmp = numeroParteClientesSeleccionado.getNumeroParteClientesKey()
		// .getNumeroParteMatcoAnterior();

		// if (npmTmp == null) {
		numeroParteMatcoAnterior = numeroParteMatco;
		// }else {
		// numeroParteMatcoAnterior = npmTmp;
		// }
		habilitarCliente = true;
		habilitarMarca = true;
	}


	public void limpiarCampos() {
		idCliente = null;
		idMarca = null;
		numeroParteCliente = null;
		numeroParteMatco = null;
		numeroParteMatcoAnterior = null;
		numeroParteClientesList = listarNumeroParteCliente();
		habilitarModificar = true;
		habilitarCliente = false;
		habilitarMarca = false;
		numeroParteClientesSeleccionado = null;
		numeroParteClientesExcelList = null;
		PrimeFaces pf = PrimeFaces.current();
		pf.executeScript("PF('tablaNPC').clearFilters();");
	}

	/**
	 * Carga un listado de Excel utilizando una funcion generica para leer los
	 * datos. Abre un dialogo donde muestra y gestiona los datos del Excel
	 * 
	 * @param FileUploadEvent
	 * @throws IOException
	 */
	public void cargarExcel2(FileUploadEvent event) throws IOException {
		PrimeFaces pf = PrimeFaces.current();

		UploadedFile archivo = event.getFile();
		String fileContent = archivo.getContentType();
		String fileName = archivo.getFileName();
		InputStream inputStream = archivo.getInputstream();
		String extension = FilenameUtils.getExtension(fileName);
		log.info("typeContentFile: " + fileContent);
		numeroParteClientesExcelList = new ArrayList<>();
		try {
			if (fileContent.equals("application/vnd.ms-excel") || extension.equals("xls")) { // Para archivos .xls
				// HSSFWorkbook workbook = new HSSFWorkbook(inputStream);
				Workbook workbook = WorkbookFactory.create(inputStream);
				Sheet sheet = workbook.getSheetAt(0);

				Integer rows = sheet.getPhysicalNumberOfRows();
				Integer cols = 4;
				cargarListadoNumeroParteCliente(sheet, rows, cols);
			} else {
				XSSFWorkbook workbook = new XSSFWorkbook(inputStream);
				workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
				XSSFSheet sheet = workbook.getSheetAt(0);

				Integer rows = sheet.getPhysicalNumberOfRows();
				Integer cols = 4;
				cargarListadoNumeroParteCliente(sheet, rows, cols);
				workbook.close();
			}
			// System.out.println("Termino de leer la lista");
			verificarColisionEnListas();
			pf.executeScript("PF('cargarExcel').show();");
			inputStream.close();
		} catch (OldExcelFormatException ex) {
			agregarMensajeError(summary, "El archivo Excel ingresado es muy viejo, no se pudo cargar. "
					+ "Por favor intente subir una versión más nueva");
			log.error("No se pudo cargar el excel: " + ex);
		} catch (Exception e) {
			agregarMensajeError(summary, "No se pudo cargar el Excel");
			log.error("No se pudo cargar el excel: " + e);
		}
	}

	/**
	 * Funcion generica para leero datos de un Excel. Permite formatos xls y xlsx.
	 * Crea un listado llamado numeroParteClientesExcelList
	 * 
	 * @param sheet
	 * @param #rows
	 * @param cols
	 * @throws Exception
	 */
	public void cargarListadoNumeroParteCliente(Sheet sheet, int rows, int cols) throws Exception {
		Row row;
		Cell cell;
		for (int r = 0; r < rows; r++) {
			row = sheet.getRow(r);
			if (row != null) {
				for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);
					if (cell != null) {
						switch (c) {
						case 0:
							if (cell.getCellType() == CellType.NUMERIC) {
								numeroParteMatco = ((int) cell.getNumericCellValue()) + "";
							} else {
								numeroParteMatco = cell.getStringCellValue();
							}
							break;
						case 1:
							if (cell.getCellType() == CellType.NUMERIC) {
								numeroParteCliente = ((int) cell.getNumericCellValue()) + "";
							} else {
								numeroParteCliente = cell.getStringCellValue();
							}
							break;
						case 2:// Marca string
							String marca;
							try {
								int idm = (int) cell.getNumericCellValue();
								marca = String.valueOf(idm);
							} catch (Exception e) {
								marca = cell.getStringCellValue();
							}
							while (marca.length() < 4) {
								marca = "0" + marca;
							}
							idMarca = marcaFacade.obtenerMarcaPorId(marca);
							break;
						case 3:// Cliente int
							int cliente;
							try {
								cliente = (int) cell.getNumericCellValue();
							} catch (Exception e) {
								cliente = Integer.parseInt(cell.getStringCellValue());
							}
							idCliente = clienteFacade.obtenerClientePorId(cliente);
							break;
						}
					}
				}

				NumeroParteClientes npc = new NumeroParteClientes();
				npc.setNumeroParteCliente(numeroParteCliente);

				numeroParteMatco = rellenarNumeroParteMatco(numeroParteMatco.toUpperCase());
				NumeroParteClientesKey numeroParteClientesKey = new NumeroParteClientesKey(numeroParteMatco, idMarca,
						idCliente);
				String numeroParteMatcoActual = obtenerNumeroParteMatcoActual(numeroParteCliente);

				if (numeroParteMatcoActual == null || numeroParteMatcoActual.equals("")) {
					npc.setCreadoPor(usuario);
				} else {
					npc.setModificadoPor(usuario);
				}
				numeroParteClientesKey.setNumeroParteMatcoAnterior(numeroParteMatcoActual);
				npc.setNumeroParteClientesKey(numeroParteClientesKey);
				npc.setNumeroParteMatcoActual(numeroParteMatcoActual);

				numeroParteClientesExcelList.add(npc);
			}
		}
	}

	/**
	 * Verifica si el numero de parte matco ya existe en la base de datos
	 * verificando en la lista que obtiene del sistema
	 * 
	 * @param numeroParteCliente
	 * @return
	 */
	public boolean verificarSiExisteParteCliente(String numeroParteCliente) {
		String numeroParteClienteLista;

		for (NumeroParteClientes npc : numeroParteClientesList) {
			numeroParteClienteLista = npc.getNumeroParteCliente();
			if (numeroParteClienteLista != null) {
				if (numeroParteClienteLista.equals(numeroParteCliente)) {
					numeroParteMatcoAnterior = npc.getNumeroParteClientesKey().getNumeroParteMatco();
					npc.getNumeroParteClientesKey().setNumeroParteMatcoAnterior(numeroParteMatcoAnterior);
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Verifica las listas numeroParteClientesList y numeroParteClientesExcelList
	 * para identificar errores de Cliente y Marca
	 * 
	 * ** Se cambio esta funcion para comparar idcliente y idmarca cuando sean
	 * iguales se editara el numero de parte MATCO cuando sean diferentes se
	 * agregara como nuevo
	 */
	public void verificarColisionEnListas() {
		numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		NumeroParteClientes npcSearch;
		try {
			for (NumeroParteClientes npcExcel : numeroParteClientesExcelList) {
				npcSearch = numeroParteClientesFacade
						.obtenerNumeroParteClienteByNumParteCliente(npcExcel.getNumeroParteCliente());
				if (npcSearch != null) {

					int idClienteDB = npcSearch.getNumeroParteClientesKey().getIdCliente().getCliente();
					String idMarcaDB = npcSearch.getNumeroParteClientesKey().getIdMarca().getMarca();
					int idClienteEx = npcExcel.getNumeroParteClientesKey().getIdCliente().getCliente();
					String idMarcaEx = npcSearch.getNumeroParteClientesKey().getIdMarca().getMarca();

					if (idClienteEx == idClienteDB) {
						if (idMarcaEx == idMarcaDB) {
							npcExcel.setEditarPorColision(true);
						} else {
							npcExcel.setEditarPorColision(false);
						}
					} else {
						npcExcel.setEditarPorColision(false);
					}
				} else {
					npcExcel.setEditarPorColision(false);
				}
			}

		} catch (Exception e) {
			String error = "No se pudo verificar los Número de Parte Cliente";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}

	}

	/**
	 * Verifica y obtiene el numero parte MATCO dado un numero parte cliente
	 * 
	 * @param numeroParteCliente
	 * @return numeroParteMatco
	 */
	public String obtenerNumeroParteMatcoActual(String numeroParteCliente) {
		String numeroParteClienteLista;
		for (NumeroParteClientes npc : numeroParteClientesList) {
			numeroParteClienteLista = npc.getNumeroParteCliente();
			if (numeroParteClienteLista != null) {
				if (numeroParteClienteLista.equals(numeroParteCliente)) {
					numeroParteMatcoAnterior = npc.getNumeroParteClientesKey().getNumeroParteMatco();
					npc.getNumeroParteClientesKey().setNumeroParteMatcoAnterior(numeroParteMatcoAnterior);
					// npc.setEditarPorColision(true);
					return npc.getNumeroParteClientesKey().getNumeroParteMatco();
				}
			}
		}
		return "";
	}

	/**
	 * Actualiza la lista de Numero de parte cliente respecto al excel, Si encuentra
	 * un numero parte Cliente igual se substituyen los numero de parte MATCO
	 */
	public void subirCambiosExcel() {

		NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		// numeroParteClientesFacade.subirCambiosNumeroParteMatcoListaExcel(numeroParteClientesExcelList);

		for (NumeroParteClientes npc : numeroParteClientesExcelList) {
			try {
				if (npc.isEditarPorColision()) {
					numeroParteClientesFacade.editarNumeroParteMatco(npc);
				} else {
					numeroParteClientesFacade.guardarNumeroParteClientes(npc);
				}
			} catch (Exception e) {
				error = "No se pudo guardar el número de parte cliente: " + npc.getNumeroParteCliente();
				log.error(error, e);
				log.warn("Se intentará editar el número de parte cliente");
				try {
					numeroParteClientesFacade.editarNumeroParteMatco(npc);
				} catch (Exception e1) {
					error = "Ocurrió un error inesperado con el número parte cliente: " + npc.getNumeroParteCliente();
					log.error(error, e1);
					agregarMensaje(summary, error, "warn", true);
				}
				continue;
			}
		}

		agregarMensajeInfo(summary, "Se ha subido la lista correctamente");

		limpiarListaExcel();
	}

	/**
	 * Limpia la lista de excel y cierra el formulario
	 */
	public void limpiarListaExcel() {
		// numeroParteClientesExcelList.clear();
		numeroParteClientesList = listarNumeroParteCliente();
		pf.executeScript("PF('cargarExcel').hide();");
		pf.executeScript("PF('tablaNPC').clearFilters();");

	}

	public void deshabilitarControlesBusquedaMarca() {
		setHabilitarMarca(true);
		// actualizarNPC();
	}

	public void habilitarControlesBusquedaMarca() {
		setHabilitarMarca(false);
		setIdMarca(null);
		// actualizarNPC();
	}

	public void deshabilitarControlesBusquedaCliente() {
		setHabilitarCliente(true);
		// actualizarNPC();
	}

	public void habilitarControlesBusquedaCliente() {
		setHabilitarCliente(false);
		setIdCliente(null);
		// actualizarNPC();
	}

	private List<NumeroParteClientes> listarNumeroParteCliente() {
		NumeroParteClientesFacade numeroParteClientesFacade = new NumeroParteClientesFacade(RUTA_PROPERTIES_AMCE3);
		try {
			numeroParteClientesList = numeroParteClientesFacade.obtenerTodosNumeroParteCliente();
			Comparator<NumeroParteClientes> comp = (NumeroParteClientes a, NumeroParteClientes b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(numeroParteClientesList, comp);
		} catch (Exception e) {
			error = "No se pudieron listar los " + summary;
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return numeroParteClientesList;
	}

	public NumeroParteClientes getNumeroParteClientesSeleccionado() {
		return numeroParteClientesSeleccionado;
	}

	public void setNumeroParteClientesSeleccionado(NumeroParteClientes numeroParteClientesSeleccionado) {
		this.numeroParteClientesSeleccionado = numeroParteClientesSeleccionado;
	}

	public String getNumeroParteMatco() {
		return numeroParteMatco;
	}

	public void setNumeroParteMatco(String numeroParteMatco) {
		this.numeroParteMatco = numeroParteMatco;
	}

	public Cliente getIdCliente() {
		return idCliente;
	}

	public void setIdCliente(Cliente idCliente) {
		this.idCliente = idCliente;
	}

	public Marca getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(Marca idMarca) {
		this.idMarca = idMarca;
	}

	public String getNumeroParteCliente() {
		return numeroParteCliente;
	}

	public void setNumeroParteCliente(String numeroParteCliente) {
		this.numeroParteCliente = numeroParteCliente;
	}

	public boolean isHabilitarModificar() {
		return habilitarModificar;
	}

	public void setHabilitarModificar(boolean habilitarModificar) {
		this.habilitarModificar = habilitarModificar;
	}

	public boolean isHabilitarMarca() {
		return habilitarMarca;
	}

	public void setHabilitarMarca(boolean habilitarMarca) {
		this.habilitarMarca = habilitarMarca;
	}

	public boolean isHabilitarCliente() {
		return habilitarCliente;
	}

	public void setHabilitarCliente(boolean habilitarCliente) {
		this.habilitarCliente = habilitarCliente;
	}

	public List<NumeroParteClientes> getNumeroParteClientesList() {
		return numeroParteClientesList;
	}

	public void setNumeroParteClientesList(List<NumeroParteClientes> numeroParteClientesList) {
		this.numeroParteClientesList = numeroParteClientesList;
	}

	public String getNumeroParteMatcoAnterior() {
		return numeroParteMatcoAnterior;
	}

	public void setNumeroParteMatcoAnterior(String numeroParteMatcoAnterior) {
		this.numeroParteMatcoAnterior = numeroParteMatcoAnterior;
	}

	public List<NumeroParteClientes> getNumeroParteClientesExcelList() {
		return numeroParteClientesExcelList;
	}

	public void setNumeroParteClientesExcelList(List<NumeroParteClientes> numeroParteClientesExcelList) {
		this.numeroParteClientesExcelList = numeroParteClientesExcelList;
	}

}
