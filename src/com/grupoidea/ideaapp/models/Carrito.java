package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

public class Carrito {
	private ArrayList<Producto> productos;
	
	/** Constructor por defecto, permite instanciar el listado de productos.*/
	public Carrito() {
		productos = new ArrayList<Producto>();
	}
	
	public ArrayList<Producto> getProductos() {
		return productos;
	}
	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
	}
	/** Permite agregar un producto al listado de productos del carrito
	 *  @parama producto Objeto que contiene la definicion del producto ha ser agregado al carrito.*/
	public void addProducto(Producto producto) {
		this.productos.add(producto);
	}
	/** Permite remover un producto especifico del listado de productos del carrito.
	 *  @param indice Entero con el indice del producto que se desea eliminar.*/
	public void removeProducto(int indice) {
		this.productos.remove(indice);
	}
}
