package com.matco.backlogs.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Token {
	String access_token;
	String token_type;
	String expires_in;
	
	public Token(@JsonProperty("access_token") String access_token, @JsonProperty("token_type") String token_type, @JsonProperty("expires_in") String expires_in) {
		this.access_token = access_token;
		this.token_type = token_type;
		this.expires_in = expires_in;
	}

	public String getAccess_token() {
		return access_token;
	}

	public void setAccess_token(String access_token) {
		this.access_token = access_token;
	}

	public String getToken_type() {
		return token_type;
	}

	public void setToken_type(String token_type) {
		this.token_type = token_type;
	}

	public String getExpires_in() {
		return expires_in;
	}

	public void setExpires_in(String expires_in) {
		this.expires_in = expires_in;
	}
}
