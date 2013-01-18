package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

public class Cliente {
	/** Cadena de texto que contiene el nombre del cliente.*/
	private String nombre;
	/** Listado de marcas previamente adquiridas por un cliente*/
	private ArrayList<Marca> marcas;
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public ArrayList<Marca> getMarcas() {
		return marcas;
	}
	public void setMarcas(ArrayList<Marca> marcas) {
		this.marcas = marcas;
	}
}
