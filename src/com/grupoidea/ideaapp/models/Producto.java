package com.grupoidea.ideaapp.models;

import android.graphics.Bitmap;
/** Clase que contiene la representación de un producto*/
public class Producto {
	/** Long con el UID de serializacion*/
	private static final long serialVersionUID = 7522501988094486252L;
	/** Entero con el identificador unico del producto*/
	private int id;
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
	/** Boolean que permite determinar si el menu del producto es visible al usuario*/
	private Boolean isMenuOpen;
	
	/** Construye un producto con nombre y precio utilizando como denominacion "Bs." sin imagen del producto (imagen = null)
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto */
	public Producto(int id, String nombre, double precio) {
		this(id, nombre, precio, "Bs.", null);
		isMenuOpen = false;
	}
	
	/** Construye un producto con nombre y precio sin imagen del producto (imagen = null)
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts". */
	public Producto(int id, String nombre, double precio, String denominacion) {
		this(id, nombre, precio, denominacion, null);
	}
	
	/** Constructor por defecto, construye un producto con nombre, precio e imagen
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".
	 *  @param imagen  Mapa de bits con la representación visual del producto.*/
	public Producto(int id, String nombre, double precio, String denominacion, Bitmap imagen) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.denominacion = denominacion;
		this.imagen = imagen;
		this.cantidad = 1;
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
	
	public Boolean getIsMenuOpen() {
		return isMenuOpen;
	}

	public void setIsMenuOpen(Boolean isMenuOpen) {
		this.isMenuOpen = isMenuOpen;
	}
	
	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	/** Permite calcular el precio de los productos del mismo tipo.*/
	public double getPrecioTotal() {
		double precioTotal;
		precioTotal = precio * cantidad;
		return precioTotal;
	}
	/** Permite construir el string del precio total concatenandole al precio la denominacion*/
	public String getStringPrecioTotal() {
		return precioToString(getPrecioTotal());
	}
	/** Permite construir el string del precio unitario concatenandole al precio la denominacion*/
	public String getStringPrecio() {
		return precioToString(this.precio);
	}
	/** Permite agregar un item a la cantidad de productos del mismo tipo*/
	public void addCantidad() {
		this.cantidad++;
	}
	
	/** Permite disminuir un item a la cantidad de productos del mismo tipo*/
	public void substractCantidad() {
		if(cantidad-- == 0) {
			this.cantidad = 0;
		}
	}
	/** Permite construir el string de algun precio suministrado concatenandole al precio la denominacion*/
	private String precioToString(double precio) {
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
