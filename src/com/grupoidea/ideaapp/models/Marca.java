package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

public class Marca {
	/** Cadena de texto que contiene el nombre de la marca*/
	private String nombre;
	/** ArrayList que contiene las metas para esta marca*/
	private ArrayList<Meta> metas;
	/** ArrayList que contiene los productos de esta marca*/
	private ArrayList<Producto> productos;
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
	public ArrayList<Producto> getProductos() {
		return productos;
	}
	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
	}


}
