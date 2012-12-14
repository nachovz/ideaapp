package com.grupoidea.ideapp.models;

import android.graphics.Bitmap;
/** Clase que contiene la representación de un producto*/
public class Producto {
	/** Cadena de texto que contiene el nombre del producto.*/
	private String nombre;
	/** Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".*/
	private String denominacion;
	/** Numero con el precio unitario del producto.*/
	private double precio;
	/** Mapa de bits con la representación visual del producto.*/
	private Bitmap imagen;
	/** Entero que almacena la cantidad de productos deseados*/
	private int cantidad;
	
	/** Construye un producto con nombre y precio utilizando como denominacion "Bs." sin imagen del producto (imagen = null)
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto */
	public Producto(String nombre, double precio) {
		this(nombre, precio, "Bs.", null);
	}
	
	/** Construye un producto con nombre y precio sin imagen del producto (imagen = null)
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts". */
	public Producto(String nombre, double precio, String denominacion) {
		this(nombre, precio, denominacion, null);
	}
	
	/** Constructor por defecto, construye un producto con nombre, precio e imagen
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".
	 *  @param imagen  Mapa de bits con la representación visual del producto.*/
	public Producto(String nombre, double precio, String denominacion, Bitmap imagen) {
		this.nombre = nombre;
		this.precio = precio;
		this.denominacion = denominacion;
		this.imagen = imagen;
	}
	public String getDenominacion() {
		return denominacion;
	}
	public void setDenominacion(String denominacion) {
		this.denominacion = denominacion;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public double getPrecio() {
		return precio;
	}
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	public Bitmap getImagen() {
		return imagen;
	}
	public void setImagen(Bitmap imagen) {
		this.imagen = imagen;
	}
	/** Permite construir el string del precio concatenandole al precio la denominacion*/
	public String getStringPrecio() {
		StringBuffer stringBuffer;
		String strValue = null;
		
		strValue = Double.toString(precio);
		stringBuffer = new StringBuffer(strValue).append(" ").append(denominacion);
		if(stringBuffer != null) {
			strValue = stringBuffer.toString();
		}
		return strValue;
	}
}
