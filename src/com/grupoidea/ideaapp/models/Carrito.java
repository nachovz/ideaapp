package com.grupoidea.ideaapp.models;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Carrito {
	private ArrayList<Producto> productos;
    private ArrayList<GrupoCategorias> gruposCategorias;
    private ArrayList<Categoria> categorias;
    public DecimalFormat df = new DecimalFormat("###,###,##0.##");
	
	/** Constructor por defecto, permite instanciar el listado de productos.*/
	public Carrito() {
        productos = new ArrayList<Producto>();
        categorias = new ArrayList<Categoria>();
        gruposCategorias = new ArrayList<GrupoCategorias>();
	}

    /**
     * Constructor que recibe grupos de categorias existentes
     * @param grupos
     */
    public Carrito(ArrayList<Categoria> categorias, ArrayList<GrupoCategorias> grupos) {
        productos = new ArrayList<Producto>();
        this.categorias = categorias;
        gruposCategorias = grupos;
    }
	
	public ArrayList<Producto> getProductos() {
        for(Producto prod:productos){
            if(prod.getCantidad() <1) prod.setCantidad(1);
        }
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
			totalValue += productoActual.getPrecioComercialTotalConIva();
		}
		return totalValue;
	}
	
	/** Permite retornar el valor del total sumarizado de los productos agregados al carrito.*/
	public String calcularTotalString() {
		String total;
		double totalValue;
		
		totalValue = calcularTotalValue(); 
		return ""+df.format(totalValue)+" Bs.";
	}
	
	/** Permite determinar el indice de un producto si se encuentra actualmente en el listado de productos
	 *  del carrito, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Entero con la posicion del producto en el listado de productos del carrito.*/
	public int findProductoIndexById(String id) {
		Producto productoActual = null;
		int index = -1;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
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
	public Producto findProductoById(String id) {
		Producto productoActual = null;
		Producto productoFinal = null;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
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

	/** Retorna el numero de elementos en el carrito.
	 */
	public int getCount(){
		return productos.size();
	}

    public void recalcularMontos(){
        GrupoCategorias grupo;
        Categoria categoria;
        Producto producto;
        //recorrer grupos categorias y actualizar descuentos
        for(int g = 0, size = gruposCategorias.size(); g < size; g++){
            gruposCategorias.get(g).calcularDescuento();
        }

        //recorrer categorias y actualizar descuentos
        for(int c = 0, size = categorias.size(); c < size; c++){
            categorias.get(c).calcularDescuento();
        }

        //recorrer productos en carrito ,actualizar y aplicar descuentos
        for(int p = 0, size = productos.size(); p < size; p++){
            productos.get(p).calcularDescuento();
            productos.get(p).calcularDescuentoAplicado();
        }
    }
}
