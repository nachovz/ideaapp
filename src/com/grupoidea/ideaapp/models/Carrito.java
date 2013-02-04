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
	
	/** Permite retornar el valor del total sumarizado de los productos agregados al carrito.*/
	public double calcularTotalValue() {
		double totalValue = 0;
		Producto productoActual = null;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			totalValue += productoActual.getPrecioTotal();
		}
		return totalValue;
	}
	
	/** Permite retornar el valor del total sumarizado de los productos agregados al carrito.*/
	public String calcularTotalString() {
		String total;
		double totalValue;
		
		totalValue = calcularTotalValue(); 
		total = new StringBuffer(String.valueOf(totalValue)).append(" Bs.").toString();
		return total;
	}
	
	/** Permite determinar el indice de un producto si se encuentra actualmente en el listado de productos
	 *  del carrito, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Entero con la posicion del producto en el listado de productos del carrito.*/
	public int findProductoIndex(int id) {
		Producto productoActual = null;
		int index = -1;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId() == id) {
				index = i;
			}
		}
		return index;
	}
	
	/** Permite determinar si un un producto se encuentra actualmente en el listado de productos
	 *  del carrito, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Objeto de tipo de Producto con el producto solicitado, si el producto no esta en el 
	 *  		listado de productos del carrito retorna null.*/
	public Producto findProductoById(int id) {
		Producto productoActual = null;
		Producto productoFinal = null;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId() == id) {
				productoFinal = productoActual;
				break;
			}
		}
		return productoFinal;
	}
	/** Permite determinar si un producto existe en el carrito para agregarlo al listado 
	 *  de productos del carrito o sumarle la cantidad del existente
	 *  @parama producto Objeto que contiene la definicion del producto ha ser agregado al carrito.*/
	public void addProducto(Producto productoAdd) {
		Producto productoFinded;
		productoFinded = findProductoById(productoAdd.getId());
		if(productoFinded != null) {
			productoFinded.addCantidad();
		} else {
			this.productos.add(productoAdd);
		}
		
	}
	/** Permite remover un producto especifico del listado de productos del carrito.
	 *  @param indice Entero con el indice del producto que se desea eliminar.*/
	public void removeProducto(int indice) {
		this.productos.remove(indice);
	}
}
