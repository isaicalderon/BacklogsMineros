package com.matco.backlogs.bean;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Obtiene la lista de permisos para no estar moviendo el LoginBean en cada
 * ocacion
 * 
 * @author N Soluciones de Software
 *
 */
public class Permisos {
	List<String> permisos;
	List<String> administradorBacklogsSucursal;
	List<String> jefeTallerSucursal;
	List<String> encargadoBacklogsSucursal;
	List<String> planeadorBacklogsSucursal;
	List<String> auxiliarBacklogsSucursal;
	List<String> inspectorBacklogsSucursal;
	List<String> supervisorBacklogsSucursal;
	List<String> monitoreoBacklogsSucursal;
	List<String> administrador;
	List<String> jefeTaller;
	List<String> encargadoBacklogs;
	List<String> planeador;
	List<String> auxiliar;
	List<String> inspector;
	List<String> supervisor;
	List<String> monitoreo;

	/**
	 * Crea los arreglos con los permisos de cada ROL
	 */
	public Permisos() {
		permisos = new ArrayList<>();

		jefeTallerSucursal = Arrays.asList(
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"REVISAR-BL",
				"ESTATUS A-M",
				"CANCELAR-BL",
				"ESTATUS A-B2",
				"ESTATUS B2-B1",
				"ESTATUS B1-B3",
				"ESTATUS B1-ER",
				"ESTATUS ER-C",
				"ESTATUS B1-C",
				"ESTATUS C-P",
				"ESTATUS C-D",
				"VER-CATINSPECT",
				"VER-SOS",
				"INDICADORES",
				"CATALOGOS"
		);
			
		encargadoBacklogsSucursal = Arrays.asList(
				"CATALOGOS",
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"REVISAR-BL",
				"ESTATUS A-M",
				"CANCELAR-BL",
				"ESTATUS A-B2",
				"ESTATUS B2-B1",
				"ESTATUS B1-B3",
				"ESTATUS B1-ER",
				"ESTATUS ER-C",
				"ESTATUS B1-C",
				"ESTATUS C-P",
				"ESTATUS C-D",
				"INDICADORES",
				"VER-CATINSPECT",
				"VER-SOS"
		);
		
		planeadorBacklogsSucursal = Arrays.asList(
				"CATALOGOS",
				"CANCELAR-BL",
				"ESTATUS C-P",
				"VER-CATINSPECT",
				"VER-SOS"
		);
		
		auxiliarBacklogsSucursal = Arrays.asList(
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"ESTATUS A-M",
				"VER-CATINSPECT",
				"VER-SOS",
				"CATALOGOS"
		);
		
		inspectorBacklogsSucursal = Arrays.asList(
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"CATINSPECT",
				"SOS",
				"CATALOGOS"
		);
		
		supervisorBacklogsSucursal = Arrays.asList(
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"REVISAR-BL",
				"ESTATUS A-M",
				"CANCELAR-BL",
				"ESTATUS A-B2",
				"ESTATUS B2-B1",
				"ESTATUS B1-B3",
				"ESTATUS B1-ER",
				"ESTATUS ER-C",
				"ESTATUS B1-C",
				"ESTATUS C-P",
				"ESTATUS C-D",
				"CATINSPECT",
				"SOS",
				"INDICADORES",
				"CATALOGOS"
		);
		
		monitoreoBacklogsSucursal = Arrays.asList(
				"CATALOGOS",
				"CANCELAR-BL",
				"ESTATUS A-B2"
		);
		
		administradorBacklogsSucursal = Arrays.asList(
				"VER-REGISTROBACKLOGS",
				"VER-REVISAR-BL",
				"VER-ESTATUS-M",
				"CANCELAR-BL",
				"VER-ESTATUS-B2",
				"VER-ESTATUS-B1",
				"ESTATUS B2-B1",
				"ESTATUS B1-A",
				"ESTATUS B1-C",
				"ESTATUS C-D",
				"SUCURSAL 6-13", 
				"REVISAR-BL",
				"CATALOGOS",
				"EDIT-CATALOGOS",
				"INDICADORES",
				"COMENTARIOS-IND",
				"HISTORIAL_BL"
		);
		
		administrador = Arrays.asList(
				"CAMBIAR-SUC",
				"REGISTROBACKLOGS",
				"BL-ESTANDAR",
				"REVISAR-BL",
				"ESTATUS A-M",
				"CANCELAR-BL",
				"ESTATUS A-B2",
				"ESTATUS B2-B1",
				"ESTATUS B1-B3",
				"ESTATUS B1-ER",
				"ESTATUS ER-C",
				"ESTATUS B1-C",
				"ESTATUS C-P",
				"ESTATUS C-D",
				"INDICADORES",
				"CATINSPECT",
				"SOS",
				"CATALOGOS",
				"EDIT-CATALOGOS",
				"COMENTARIOS-IND",
				"HISTORIAL_BL"
		);
	}

	public List<String> obtenerPermisos(String rol) {
		switch (rol) {
		case "ADMINISTRADOR_BL_CAN":
		case "ADMINISTRADOR_BL_CAB":
			permisos = administradorBacklogsSucursal;
			break;
		case "JEFE_TALLER_BL_CAN":
		case "JEFE_TALLER_BL_CAB":
			permisos = jefeTallerSucursal;
			break;
		case "ENCARGADO_BL_CAN":
		case "ENCARGADO_BL_CAB":
			permisos = encargadoBacklogsSucursal;
			break;
		case "PLANEADOR_BL_CAN":
		case "PLANEADOR_BL_CAB":
			permisos = planeadorBacklogsSucursal;
			break;
		case "AUXILIAR_BL_CAN":
		case "AUXILIAR_BL_CAB":
			permisos = auxiliarBacklogsSucursal;
			break;
		case "INSPECTOR_BL_CAN":
		case "INSPECTOR_BL_CAB":
			permisos = inspectorBacklogsSucursal;
			break;
		case "SUPERVISOR_BL_CAN":
		case "SUPERVISOR_BL_CAB":
			permisos = supervisorBacklogsSucursal;
			break;
		case "MONITOREO_BL_CAN":
		case "MONITOREO_BL_CAB":
			permisos = monitoreoBacklogsSucursal;
			break;
		case "ADMINISTRADOR":
			permisos = administrador;
			break;
		}
		return permisos;
	}

}
