package com.matco.backlogs.catInspecciones;

import java.io.StringReader;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import com.matco.interfacescatinspect.clientes.TokenCliente;
import com.matco.interfacescatinspect.dto.ConfiguracionDto;
import com.matco.interfacescatinspect.entity.RetrieveReportRequest;
import com.matco.interfacescatinspect.entity.RetrieveReportResponse;
import com.matco.interfacescatinspect.facade.ConfiguracionFacade;

public class ClienteCatInspeccion {
	private static final Logger log = Logger.getLogger(ClienteCatInspeccion.class);
	private String direccionRetrieveReport = "Reports/";
	private String direccion;

	public ClienteCatInspeccion(String archivoConfig) {
		ConfiguracionFacade configuracionFacade = new ConfiguracionFacade(archivoConfig);
		ConfiguracionDto configuracion;

		try {
			configuracion = configuracionFacade.obtenerCredenciales();
		} catch (Exception e) {
			log.error(e);
			return;
		}

		String urlToken = configuracion.getUrlToken();
		direccion = configuracion.getUrlServicios();
		String clientId = configuracion.getIdCliente();
		String password = configuracion.getIdPassword();

		TokenCliente service = new TokenCliente(urlToken, clientId, password);
		service.generarBearerToken();
		log.info("Obteniendo Bearer Token");
	}

	public RetrieveReportResponse obtenerPDF(RetrieveReportRequest reporteInspeccion) {
		RetrieveReportResponse streamPDF = null;
		try {
			ResteasyClient client = new ResteasyClientBuilder().build();
			String url = direccion + direccionRetrieveReport + "RetrieveReport";
			ResteasyWebTarget target = client.target(url);
			/*
			String token = "Bearer " + TokenCliente.tokenGlobal;
			Response response = target.request().header("Authorization", token).get();
			
			@SuppressWarnings("rawtypes")
			Entity entity = Entity.entity(reporteInspeccion, "application/json");
			Response response = target.request().header("Authorization", "Bearer " + TokenCliente.tokenGlobal)
					.post(entity);
			*/
			
			@SuppressWarnings("rawtypes")
			Entity<RetrieveReportRequest> entity = Entity.entity(reporteInspeccion, "application/json");
			Response response = target.request().header("Authorization", "Bearer " + TokenCliente.tokenGlobal)
					.post(entity);
					
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			} else {
				String jsonString = response.readEntity(String.class);
				Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.UPPER_CAMEL_CASE)
						.setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
				JsonReader reader = new JsonReader(new StringReader(jsonString));
				reader.setLenient(true);
				RetrieveReportResponse[] reportes = gson.fromJson(reader, RetrieveReportResponse[].class);

				for (RetrieveReportResponse reporte : reportes) {
					log.info("Obteniendo PDF " + reporte.getReportIdentity().getReportName());
					streamPDF = reporte;
					break;
				}
			}
			response.close();
		} catch (Exception e) {
			String mensajeError = "No se pudo generar el PDF";
			log.error(mensajeError, e);
			return null;
		}
		return streamPDF;
	}
}
