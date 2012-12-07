package com.grupoidea.ideapp.models;

/** Clase que representa la response obtenida de manera local o remota */
public class Response {
	/** Objeto que contendra la response retornada por el origen de datos */
	private Object response;

	public Object getResponse() {
		return response;
	}

	public void setResponse(Object response) {
		this.response = response;
	}
}
