package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

public class Vendedor {
	/** Cadena de texto que contiene el nombre del vendedor*/
	private String nombre;
	private ArrayList<Meta> metas;
	/** ArrayList de clientes disponibles para este vendedor*/
	private ArrayList<Cliente> clientes;
    private String parseId;
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public ArrayList<Meta> getMetas() {
		return metas;
	}
	public void setMetas(ArrayList<Meta> metas) {
		this.metas = metas;
	}
	public ArrayList<Cliente> getClientes() {
		return clientes;
	}
	public void setClientes(ArrayList<Cliente> clientes) {
		this.clientes = clientes;
	}

    public String getParseId() {
        return parseId;
    }

    public void setParseId(String parseId) {
        this.parseId = parseId;
    }
}
