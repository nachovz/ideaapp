package com.grupoidea.ideapp.models;

import android.graphics.Bitmap;
/** Clase que contiene la representación de un producto*/
public class Producto {
	/** Cadena de texto que contiene el nombre del producto.*/
	private String nombre;
	/** Cadena de texto que contiene el precio del producto, este valor debe contenter
	 *  al final la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".*/
	private String precio;
	/** Mapa de bits con la representación visual del producto.*/
	private Bitmap imagen;
	
	/** Construye un producto con nombre y precio sin imagen del producto (imagen = null)
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Cadena de texto que contiene el precio del producto, este valor debe contenter
	 *  al final la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts" */
	public Producto(String nombre, String precio) {
		this(nombre, precio, null);
	}
	
	/** Constructor por defecto, construye un producto con nombre, precio e imagen
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Cadena de texto que contiene el precio del producto, este valor debe contenter
	 *  al final la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts"
	 *  @param imagen  Mapa de bits con la representación visual del producto.*/
	public Producto(String nombre, String precio, Bitmap imagen) {
		this.nombre = nombre;
		this.precio = precio;
		this.imagen = imagen;
	}
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public String getPrecio() {
		return precio;
	}
	public void setPrecio(String precio) {
		this.precio = precio;
	}
	public Bitmap getImagen() {
		return imagen;
	}
	public void setImagen(Bitmap imagen) {
		this.imagen = imagen;
	}
}
