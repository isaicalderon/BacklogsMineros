package com.matco.backlogs.bean;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.io.Serializable;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row.MissingCellPolicy;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import com.matco.amce3.dto.MaquinariaDto;
import com.matco.amce3.facade.BacklogsMinerosBitacoraEstatusFacade;
import com.matco.amce3.facade.BacklogsMinerosDetalleRefaFacade;
import com.matco.amce3.facade.BacklogsMinerosFacade;
import com.matco.amce3.facade.CargosTrabajoFacade;
import com.matco.amce3.facade.CodigosSistemasFacade;
import com.matco.amce3.facade.ImagenesBacklogsMinerosFacade;
import com.matco.amce3.facade.LadoComponenteFacade;
import com.matco.amce3.facade.LugaresOrigenBacklogsMinerosFacade;
import com.matco.amce3.facade.OrigenesBacklogsMinerosFacade;
import com.matco.amce3.facade.PrioridadesBacklogsMinerosFacade;
import com.matco.amce3.facade.SintomasYRiesgosFacade;
import com.matco.backlogs.entity.BacklogsMineros;
import com.matco.backlogs.entity.BacklogsMinerosBitacoraEstatus;
import com.matco.backlogs.entity.BacklogsMinerosDetalleRefa;
import com.matco.backlogs.entity.CargosTrabajo;
import com.matco.backlogs.entity.CodigosSMCS;
import com.matco.backlogs.entity.CodigosSistemas;
import com.matco.backlogs.entity.LadoComponente;
import com.matco.backlogs.entity.LugaresOrigenBacklogsMineros;
import com.matco.backlogs.entity.OrigenesBacklogsMineros;
import com.matco.backlogs.entity.PrioridadesBacklogsMineros;
import com.matco.backlogs.entity.RiesgosTrabajo;
import com.matco.backlogs.entity.Sintomas;
import com.matco.backlogs.entity.key.BacklogsMinerosBitacoraEstatusKey;
import com.matco.backlogs.entity.key.BacklogsMinerosDetalleRefaKey;
import com.matco.backlogs.entity.key.BacklogsMinerosKey;
import com.matco.ejes.entity.Marca;

public class GenericInspectBean extends GenericBacklogBean implements Serializable {
	
	private static final long serialVersionUID = 1841871607535681930L;
	private static final Logger log = Logger.getLogger(GenericInspectBean.class);
	
	protected final String RUTA_PROPERTIES_AMCE3 = getExternalContext().getInitParameter("admintx_amce3");
	protected final String RUTA_PROPERTIES_SERVICIO = getExternalContext().getInitParameter("admintx_servicio");
	protected final String RUTA_PROPERTIES_MATCO = getExternalContext().getInitParameter("admintx_matco");
	protected final String RUTA_PROPERTIES_CATINSPECT = getExternalContext()
			.getInitParameter("admintx_interfacescatinspect");

	protected final String rutaArchivosTemporal = getExternalContext().getInitParameter("rutaImagenesBacklogsMineros");
	protected static final DecimalFormat formato = new DecimalFormat("00");
	protected static final DecimalFormat formatoBL = new DecimalFormat("00000000");

	protected BacklogsMinerosFacade backlogsMinerosFacade = new BacklogsMinerosFacade(RUTA_PROPERTIES_AMCE3);
	protected OrigenesBacklogsMinerosFacade origenesBacklogsMinerosFacade = new OrigenesBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);
	protected LugaresOrigenBacklogsMinerosFacade lugaresOrigenBacklogsMinerosFacade = new LugaresOrigenBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);
	protected PrioridadesBacklogsMinerosFacade prioridadesBacklogsMinerosFacade = new PrioridadesBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);
	protected CodigosSistemasFacade codigosSistemasFacade = new CodigosSistemasFacade(RUTA_PROPERTIES_AMCE3);
	protected CargosTrabajoFacade cargosTrabajoFacade = new CargosTrabajoFacade(RUTA_PROPERTIES_AMCE3);
	protected BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
			RUTA_PROPERTIES_AMCE3);
	protected BacklogsMinerosBitacoraEstatusFacade backlogsMinerosBitacoraEstatusFacade = new BacklogsMinerosBitacoraEstatusFacade(
			RUTA_PROPERTIES_AMCE3);
	protected ImagenesBacklogsMinerosFacade ImagenesBacklogsMinerosFacade = new ImagenesBacklogsMinerosFacade(
			RUTA_PROPERTIES_AMCE3);

	protected LadoComponenteFacade ladoComponenteFacade = new LadoComponenteFacade(RUTA_PROPERTIES_AMCE3);
	protected SintomasYRiesgosFacade sintomasYriesgos = new SintomasYRiesgosFacade(RUTA_PROPERTIES_AMCE3);
	protected final String PDF_TYPE = "pdforderedbyform";

	public List<LadoComponente> listarLadoComponentes() {
		List<LadoComponente> ladoComponenteList = new ArrayList<LadoComponente>();
		try {
			ladoComponenteList = ladoComponenteFacade.obtenerTodosLDC();
		} catch (Exception e) {
			String error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return ladoComponenteList;
	}

	public List<Sintomas> listarSintomas() {
		List<Sintomas> sintomasList = new ArrayList<Sintomas>();
		try {
			sintomasList = sintomasYriesgos.getAllSintomas();
		} catch (Exception e) {
			String error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return sintomasList;
	}

	public List<RiesgosTrabajo> listarRiesgosTrabajos() {
		List<RiesgosTrabajo> riesgosTabajosList = new ArrayList<RiesgosTrabajo>();
		try {
			riesgosTabajosList = sintomasYriesgos.getAllRiesgos();
		} catch (Exception e) {
			String error = "No se pudieron listar las Prioridades Backlogs Mineros";
			log.error(error, e);
			agregarMensajeError(summary, error);
		}
		return riesgosTabajosList;
	}

	public List<MaquinariaDto> listarMaquinariaDtoBacklogsMineros(Short idSucursal) {
		List<MaquinariaDto> maquinariaDtoList = new ArrayList<>();
		try {
			List<MaquinariaDto> maquinariaDtoLista = new ArrayList<>();
			if (idSucursal == 6) {
				short sucursal = 6;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 9;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 14;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
			} else if (idSucursal == 13 || idSucursal == 2 || idSucursal == 1) {
				short sucursal = 13;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 2;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));
				sucursal = 1;
				maquinariaDtoLista.addAll(backlogsMinerosFacade.obtenerMaquinaria(sucursal));

			}
			for (MaquinariaDto maquinaria : maquinariaDtoLista) {
				String numeroSerie = maquinaria.getSerie() == null ? "" : maquinaria.getSerie();
				String numeroSerieDos = numeroSerie.equals("") ? "" : numeroSerie;
				if (!numeroSerieDos.equals("")) {
					maquinariaDtoList.add(maquinaria);
				}
			}
			Comparator<MaquinariaDto> comp = (MaquinariaDto a, MaquinariaDto b) -> {
				String serieA = a.getSerie() != null ? a.getSerie() : "";
				String serieB = b.getSerie() != null ? b.getSerie() : "";
				return serieA.compareTo(serieB);
			};
			Collections.sort(maquinariaDtoList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return maquinariaDtoList;
	}

	public List<OrigenesBacklogsMineros> listarOrigenesBacklogsMineros() {
		List<OrigenesBacklogsMineros> origenesBacklogsMinerosList = new ArrayList<>();
		List<OrigenesBacklogsMineros> resultado = new ArrayList<>();

		try {
			origenesBacklogsMinerosList = origenesBacklogsMinerosFacade.obtenerTodosOrigenesBacklogsMineros();
			Comparator<OrigenesBacklogsMineros> comp = (OrigenesBacklogsMineros a, OrigenesBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(origenesBacklogsMinerosList, comp);

			resultado.add(origenesBacklogsMinerosList.get(origenesBacklogsMinerosList.size() - 1));
		} catch (Exception e) {
			log.error(e);
		}
		return resultado;
	}

	public List<LugaresOrigenBacklogsMineros> listarLugaresOrigenesBacklogsMineros() {
		List<LugaresOrigenBacklogsMineros> lugaresOrigenBacklogsMinerosList = new ArrayList<>();

		try {
			lugaresOrigenBacklogsMinerosList = lugaresOrigenBacklogsMinerosFacade
					.obtenerTodosLugaresOrigenBacklogsMineros();
			Comparator<LugaresOrigenBacklogsMineros> comp = (LugaresOrigenBacklogsMineros a,
					LugaresOrigenBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(lugaresOrigenBacklogsMinerosList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return lugaresOrigenBacklogsMinerosList;
	}

	public List<PrioridadesBacklogsMineros> listarPrioridadesBacklogsMineros() {
		List<PrioridadesBacklogsMineros> prioridadesBacklogsMinerosList = new ArrayList<>();
		PrioridadesBacklogsMinerosFacade prioridadesBacklogsMinerosFacade = new PrioridadesBacklogsMinerosFacade(
				RUTA_PROPERTIES_AMCE3);
		try {
			prioridadesBacklogsMinerosList = prioridadesBacklogsMinerosFacade.obtenerTodosPrioridadesBacklogsMineros();
			Comparator<PrioridadesBacklogsMineros> comp = (PrioridadesBacklogsMineros a,
					PrioridadesBacklogsMineros b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(prioridadesBacklogsMinerosList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return prioridadesBacklogsMinerosList;
	}

	public List<CodigosSistemas> listarSistemasBacklogsMineros() {
		List<CodigosSistemas> codigosSistemasList = new ArrayList<>();
		try {
			codigosSistemasList = codigosSistemasFacade.obtenerTodosCodigosSistemas();
			Comparator<CodigosSistemas> comp = (CodigosSistemas a, CodigosSistemas b) -> {
				Integer codigoA = a.getCodigoSistema();
				Integer codigoB = b.getCodigoSistema();
				return codigoA.compareTo(codigoB);
			};
			Collections.sort(codigosSistemasList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return codigosSistemasList;
	}

	public List<CargosTrabajo> listarCargosTrabajoBacklogsMineros() {
		List<CargosTrabajo> cargosTrabajosList = new ArrayList<>();
		try {
			cargosTrabajosList = cargosTrabajoFacade.obtenerTodosCargosTrabajo();
			Comparator<CargosTrabajo> comp = (CargosTrabajo a, CargosTrabajo b) -> {
				Date fechaA = a.getFechaHoraCreacion() != null ? a.getFechaHoraCreacion() : new Date();
				Date fechaB = b.getFechaHoraCreacion() != null ? b.getFechaHoraCreacion() : new Date();
				return fechaB.compareTo(fechaA);
			};
			Collections.sort(cargosTrabajosList, comp);
		} catch (Exception e) {
			log.error(e);
		}
		return cargosTrabajosList;
	}

	public List<BacklogsMinerosDetalleRefa> obtenerPartes(BacklogsMinerosKey backlogMineroKey) {
		List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();

		try {

			backlogsMinerosDetalleRefaList = backlogsMinerosDetalleRefaFacade
					.obtenerBacklogMineroDetalleRefaPorId(backlogMineroKey);
			Comparator<BacklogsMinerosDetalleRefa> comp = (BacklogsMinerosDetalleRefa a,
					BacklogsMinerosDetalleRefa b) -> {
				Integer consecutivoA = a.getBacklogsMinerosDetalleRefaKey().getConsecutivo();
				Integer consecutivoB = b.getBacklogsMinerosDetalleRefaKey().getConsecutivo();
				return consecutivoA.compareTo(consecutivoB);
			};
			Collections.sort(backlogsMinerosDetalleRefaList, comp);
		} catch (Exception e) {

			log.error(e);
		}
		// filasRefacciones = backlogsMinerosDetalleRefaList.size();
		return backlogsMinerosDetalleRefaList;
	}

	protected void cierraInputStream(InputStream inputStream) {
		try {
			if (inputStream != null) {
				inputStream.close();
			}
		} catch (IOException e) {
		}
	}

	public static InputStream checkForUtf8BOMAndDiscardIfAny(InputStream inputStream) throws IOException {
		PushbackInputStream pushbackInputStream = new PushbackInputStream(new BufferedInputStream(inputStream), 3);
		byte[] bom = new byte[3];
		if (pushbackInputStream.read(bom) != -1) {
			if (!(bom[0] == (byte) 0xEF && bom[1] == (byte) 0xBB && bom[2] == (byte) 0xBF)) {
				pushbackInputStream.unread(bom);
			}
		}
		return pushbackInputStream;
	}

	public List<BacklogsMinerosDetalleRefa> cargarListadoPartesRequeridas(XSSFWorkbook workbook) {
		List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new ArrayList<>();
		workbook.setMissingCellPolicy(MissingCellPolicy.RETURN_BLANK_AS_NULL);
		XSSFSheet sheet = workbook.getSheetAt(0);
		XSSFRow row;
		XSSFCell cell;

		Integer rows = sheet.getPhysicalNumberOfRows();
		Integer cols = 5;

		for (int r = 0; r < rows; r++) {
			row = sheet.getRow(r);
			BacklogsMinerosDetalleRefa bmdr = new BacklogsMinerosDetalleRefa();
			if (row != null) {
				for (int c = 0; c < cols; c++) {
					cell = row.getCell((short) c);
					if (cell != null) {
						switch (c) {
						case 0:
							// es un entero de 3 digitos
							if (cell.getCellType() == CellType.STRING) {
								short cantidad = (short) cell.getNumericCellValue();
								bmdr.setCantidad(cantidad);
							} else {
								agregarMensajeWarn(summary, "El campo Cantidad debe ser un numero entero, se ingreso: "
										+ cell.getStringCellValue());
								bmdr.setCantidad((short) 1);
							}
							break;
						case 1:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setNumeroParte(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setNumeroParte(cell.getStringCellValue());
							}
							break;
						case 2:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setDescripcionParte(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setDescripcionParte(cell.getStringCellValue());
							}
							break;
						case 3:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setObservaciones(((int) cell.getNumericCellValue()) + "");
							} else if (cell.getCellType() == CellType.STRING) {
								bmdr.setObservaciones(cell.getStringCellValue());
							}
							break;
						case 4:
							if (cell.getCellType() == CellType.NUMERIC) {
								bmdr.setPrecio(cell.getNumericCellValue());
								bmdr.setSubTotal(bmdr.getCantidad() * bmdr.getPrecio());
							} else {
								agregarMensajeWarn(summary,
										"El campo Precio Unitario debe ser un numero real, se ingreso: "
												+ cell.getStringCellValue());
								bmdr.setPrecio(0.0);
								bmdr.setSubTotal(0.0);
							}
							break;
						}
					} else {
						switch (c) {
						case 0:
							bmdr.setCantidad((short) 0);
							break;
						case 1:
							bmdr.setNumeroParte("");
							break;
						case 2:
							bmdr.setDescripcionParte("");
							break;
						case 3:
							bmdr.setObservaciones("");
							break;
						case 4:
							bmdr.setPrecio(0.0);
						}
					}
				}
				backlogsMinerosDetalleRefaList.add(bmdr);
			}
		}
		return backlogsMinerosDetalleRefaList;
	}

	/*
	 * @SuppressWarnings("deprecation") public List<BacklogsMinerosDetalleRefa>
	 * cargarListadoPartesRequeridasLegacy(HSSFWorkbook workbook) {
	 * List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList = new
	 * ArrayList<>();
	 * 
	 * HSSFSheet sheet = workbook.getSheetAt(0); HSSFRow row; HSSFCell cell;
	 * 
	 * Integer rows = sheet.getPhysicalNumberOfRows(); Integer cols = 4;
	 * 
	 * for (int r = 1; r < rows; r++) { row = sheet.getRow(r);
	 * BacklogsMinerosDetalleRefa bmdr = new BacklogsMinerosDetalleRefa(); if (row
	 * != null) { for (int c = 0; c < cols; c++) { cell = row.getCell((short) c);
	 * int cellType = cell.getCellType(); if (cell != null) { switch (c) { case 0:
	 * if (HSSFCell.CELL_TYPE_NUMERIC == cellType) { String numeroParte =
	 * cell.getStringCellValue(); while (numeroParte.length() < 7) { numeroParte =
	 * "0" + numeroParte; } bmdr.setNumeroParte(numeroParte); } break; case 1:
	 * String descripcionParte = cell.toString();
	 * bmdr.setDescripcionParte(descripcionParte); break; case 2: if
	 * (cell.toString() != null) { if (HSSFCell.CELL_TYPE_NUMERIC == cellType) {
	 * Short cantidad = Short.parseShort(cell.getStringCellValue());
	 * //bmdr.setCantidad(cantidad); } } break; case 3: String observaciones =
	 * cell.toString(); bmdr.setObservaciones(observaciones); break; } } } if
	 * (bmdr.getCantidad() != null && !bmdr.getNumeroParte().equals("") &&
	 * !bmdr.getDescripcionParte().equals("")) {
	 * backlogsMinerosDetalleRefaList.add(bmdr); } } } return
	 * backlogsMinerosDetalleRefaList; }
	 */
	public boolean verificarPartesRequeridas(List<BacklogsMinerosDetalleRefa> backlogsMinerosDetalleRefaList) {
		boolean partesRequeridas = false;
		if (!backlogsMinerosDetalleRefaList.isEmpty()) {
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				boolean campoFaltante = ((backlogsMinerosDetalleRefa.getCantidad() == null)
						|| backlogsMinerosDetalleRefa.getDescripcionParte() == null
						|| backlogsMinerosDetalleRefa.getDescripcionParte().equals("")
						|| backlogsMinerosDetalleRefa.getNumeroParte() == null
						|| backlogsMinerosDetalleRefa.getNumeroParte().equals("")) ? true : false;
				backlogsMinerosDetalleRefa.setCamposFaltantes(campoFaltante);
			}

			boolean falta = false;
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : backlogsMinerosDetalleRefaList) {
				falta = backlogsMinerosDetalleRefa.isCamposFaltantes();
				if (falta == true) {
					agregarMensajeWarnKeep(
							"Verifique que todas las partes requeridas contengan los campos requeridos.");
					partesRequeridas = true;
					break;
				}
			}
		}
		return partesRequeridas;
	}

	public void borraArchivosDeImagenes(File file) {
		if (file.exists()) {
			if (!file.delete()) {
				log.error("La operación de borrado de archivo ha fallado");
				log.error(file.getAbsolutePath());
			}
		}
	}

	public boolean validarCamposRequeridos(List<BacklogsMinerosDetalleRefa> listaRefacciones, String sintomas,
			String solicitadoPor, Integer horometro, String ladoComponente, Short idOrigenBacklogMinero,
			Short idLugarOrigenBacklogMinero, Short idPrioridadBacklogMinero, String tipoTrabajo,
			Integer idCodigoSistema, Short idCargoTrabajo, CodigosSMCS codigoTrabajo) {
		boolean resultado = true;
		if (verificarPartesRequeridas(listaRefacciones)) {
			resultado = false;
		}
		if (sintomas == null || sintomas.trim().equals("")) {
			agregarMensajeWarnKeep("El campo 'Sintomas' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (solicitadoPor == null || solicitadoPor.trim().equals("")) {
			agregarMensajeWarnKeep("El campo 'Requerido por' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (horometro == null) {
			agregarMensajeWarn("El campo 'Horometro' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (ladoComponente == null || ladoComponente.trim().equals("")) {
			agregarMensajeWarnKeep("El campo 'Lado del componente' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (idOrigenBacklogMinero == null) {
			agregarMensajeWarnKeep("El campo 'Origen del BL' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (idLugarOrigenBacklogMinero == null) {
			agregarMensajeWarnKeep("El campo 'Lugar Origen del BL' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (idPrioridadBacklogMinero == null) {
			agregarMensajeWarnKeep("El campo 'Prioridad' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (tipoTrabajo == null || tipoTrabajo.trim().equals("")) {
			agregarMensajeWarnKeep("El campo 'Tipo de trabajo' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (idCodigoSistema == null) {
			agregarMensajeWarnKeep("El campo 'Sistema' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (idCargoTrabajo == null) {
			agregarMensajeWarnKeep("El campo 'Cargo' está vacio y se requiere para continuar.");
			resultado = false;
		}
		if (codigoTrabajo == null) {
			agregarMensajeWarnKeep("El campo 'Código de trabajo' está vacio y se requiere para continuar.");
			resultado = false;
		}
		return resultado;
	}

	public void asignarBitacoraEstatusBacklogMinero(BacklogsMineros backlog, String usuario) throws Exception {
		BacklogsMinerosBitacoraEstatus backlogsMinerosBitacoraEstatus = new BacklogsMinerosBitacoraEstatus();
		BacklogsMinerosBitacoraEstatusKey backlogsMinerosBitacoraEstatusKey = new BacklogsMinerosBitacoraEstatusKey(
				backlog, new Date());
		backlogsMinerosBitacoraEstatus.setBacklogsMinerosBitacoraEstatusKey(backlogsMinerosBitacoraEstatusKey);
		String idEstatusBacklogMinero = backlog.getIdEstatusBacklogsMineros().getIdEstatusBacklogMinero();
		backlogsMinerosBitacoraEstatus.setIdEstatusBacklogMinero(idEstatusBacklogMinero);
		backlogsMinerosBitacoraEstatus.setUsuarioEstatusInicio(usuario);
		backlogsMinerosBitacoraEstatusFacade.guardarBacklogMineroBitacoraEstatus(backlogsMinerosBitacoraEstatus);

	}

	public void asignarDetalleRefaBacklogMinero(BacklogsMineros backlog, List<BacklogsMinerosDetalleRefa> listaRefacciones ) throws Exception {
		BacklogsMinerosDetalleRefaFacade backlogsMinerosDetalleRefaFacade = new BacklogsMinerosDetalleRefaFacade(
				RUTA_PROPERTIES_AMCE3);
			for (BacklogsMinerosDetalleRefa backlogsMinerosDetalleRefa : listaRefacciones ) {
				BacklogsMinerosKey idBacklogMinero = backlog.getBacklogsMinerosKey();
				BacklogsMinerosDetalleRefaKey backlogsMinerosDetalleRefaKey = new BacklogsMinerosDetalleRefaKey(
						idBacklogMinero);
				backlogsMinerosDetalleRefa.setBacklogsMinerosDetalleRefaKey(backlogsMinerosDetalleRefaKey);
				Marca idMarca = backlog.getIdMarca();
				backlogsMinerosDetalleRefa.setIdMarca(idMarca);
				backlogsMinerosDetalleRefa.setPrecio(backlogsMinerosDetalleRefa.getPrecio());
				backlogsMinerosDetalleRefa.setTotal(backlogsMinerosDetalleRefa.getTotal());
				String numPart = backlogsMinerosDetalleRefa.getNumeroParte().equals("") ? ""
						: backlogsMinerosDetalleRefa.getNumeroParte().toUpperCase();

				backlogsMinerosDetalleRefa.setNumeroParte(numPart);
				String descripcionParte = backlogsMinerosDetalleRefa.getDescripcionMayuscula().equals("") ? null
						: backlogsMinerosDetalleRefa.getDescripcionMayuscula();
				backlogsMinerosDetalleRefa.setDescripcionParte(descripcionParte);
				String observaciones = backlogsMinerosDetalleRefa.getObservacionesMayuscula().equals("") ? null
						: backlogsMinerosDetalleRefa.getObservacionesMayuscula();
				backlogsMinerosDetalleRefa.setObservaciones(observaciones);
				backlogsMinerosDetalleRefaFacade.guardarBacklogMinerosDetalleRefa(backlogsMinerosDetalleRefa);
			}
	
	}

	public boolean copiarArchivo(String destination, String fileName, InputStream in) throws Exception {
		boolean copiaCorrecta = true;
		OutputStream out = null;
		File theDir = new File(destination);

		// Crea el directorio
		if (!theDir.exists()) {
			try {
				theDir.mkdir();
			} catch (SecurityException se) {
				String error = "No se ha podido guardar el archivo " + fileName + " en la ruta " + destination;
				log.error(error, se);
				agregarMensajeError(error);
			}
		}
		// -------------------------------------------

		try {
			// File file = new File(destination+fileName);
			// file.createNewFile();
			// crearImagenesTemporales(file, in);

			out = new FileOutputStream(destination + fileName);
			int read = 0;
			byte[] bytes = new byte[1024];
			while ((read = in.read(bytes)) != -1) {
				out.write(bytes, 0, read);
			}
		} catch (Exception e) {
			copiaCorrecta = false;
		} finally {
			if (in != null) {
				in.close();
			}
			if (out != null) {
				out.flush();
				out.close();
				try {
					out.close();
				} catch (IOException ex) {
					copiaCorrecta = false;
				}
			}
		}
		return copiaCorrecta;
	}

	public void crearImagenesTemporales(File file, InputStream contenido) throws IOException {
		FileUtils.copyInputStreamToFile(contenido, file);
	}

}
