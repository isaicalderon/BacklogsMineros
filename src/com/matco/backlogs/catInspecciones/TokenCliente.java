package com.matco.backlogs.catInspecciones;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;

import com.matco.backlogs.dto.Token;


public class TokenCliente {
	
	private String parametroConfiguracion;
	public  static String tokenGlobal;
	public  String clientId;
	public  String password;
	private static final Logger log = Logger.getLogger(TokenCliente.class);
	
	public TokenCliente(String parametroConfiguracion, String clientId, String password) {
		this.parametroConfiguracion = parametroConfiguracion;
		this.clientId = clientId;
		this.password = password;
	}

	/** Obtiene el bearer token de la seguridad de los servicios
	 */
	public void generarBearerToken() {
		try {
			MultivaluedMap<String, Object> cuerpo = new MultivaluedHashMap<String, Object>();
			cuerpo.add("grant_type", "client_credentials");
			cuerpo.add("scope", "manage:all");
			ResteasyClient client = new ResteasyClientBuilder().build();
			ResteasyWebTarget target = client.target(parametroConfiguracion).queryParams(cuerpo);
			target.register(new BasicAuthentication(clientId, password));
			Response response = target.request().post(null);
			if (response.getStatus() != 200) {
				throw new RuntimeException("Failed : HTTP error code : " + response.getStatus());
			} 
			else {
				Token token = response.readEntity(Token.class);
				tokenGlobal = (token == null) ? "" : token.getAccess_token();
			}
			response.close();
		} 
		catch (Exception e) {
			String mensajeError = "No se pudo generar el Bearer Token";
			log.error(mensajeError, e);
		}
	}
}
