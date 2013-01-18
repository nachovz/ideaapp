package com.grupoidea.ideaapp.io;

/** Clase que representa las distintas consultas que puede realizar la aplicacion*/
public class Request {
	/** Consulta remota a los servidores de Parse*/
	public final static int PARSE_REQUEST = 0;
	/** Consulta remota a un URL via http*/
	public final static int HTTP_REQUEST = 1;
	/** Entero que representa el tipo de consulta a realizar, valores permitidos en la clase <code>Request</code>*/
	private int requestType;
	/** Objeto que representa la consulta a realizar, si el tipo de consulta es PARSE_QUERY este objeto contendra un <code>ParseRequest</code>.
	 *  Si el tipo de consulta es HTTP_QUERY el objeto contendra un String con el URL de la consulta*/
	private Object request;
	
	/** Creacion del request suministrando el tipo de request*/
	public Request(int requestType) {
		this.requestType = requestType;
	}
	
	public int getRequestType() {
		return requestType;
	}
	public void setRequestType(int requestType) {
		this.requestType = requestType;
	}
	public Object getRequest() {
		return request;
	}
	public void setRequest(Object request) {
		this.request = request;
	}
	
}
