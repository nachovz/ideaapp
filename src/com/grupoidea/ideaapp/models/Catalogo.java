package com.grupoidea.ideaapp.models;

import android.content.Context;

import com.grupoidea.ideaapp.R;

import java.util.ArrayList;

public class Catalogo {
	private ArrayList<Producto> productos, productosCatalogo;
    private Context mContext;

    public Catalogo(Context context){
        mContext = context;
        productosCatalogo = new ArrayList<Producto>();
    }

	/** Constructor por defecto, permite instanciar el listado de productos.*/
	public Catalogo(Context context, ArrayList<Producto> prods) {
		this(context);
        productos = prods;
        productosCatalogo.clear();
        productosCatalogo.addAll(prods);
	}
	
	public ArrayList<Producto> getProductosCatalogo() {
		return productosCatalogo;
	}

    public ArrayList<Producto> getProductos() {
        return productos;
    }

	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
        productosCatalogo.clear();
        productosCatalogo.addAll(productos);
	}
	
	/** Permite determinar el indice de un producto si se encuentra actualmente en el listado de productos
	 *  del catalogo, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Entero con la posicion del producto en el listado de productos del catalogo.*/
	public int findProductoIndexById(String id) {
		Producto productoActual = null;
		int index = -1;
		
		for(int i=0; i<productosCatalogo.size(); i++) {
			productoActual = productosCatalogo.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
				index = i;
			}
		}
		return index;
	}

    public ArrayList<Producto> filter(String marcaActual, String categoriaActual){
//        Log.d("DEBUG","Catalogo.filter("+marcaActual+", "+categoriaActual+")");
        productosCatalogo.clear();
        for (Producto producto : productos) {
            if (marcaActual.equals(mContext.getString(R.string.todas))) {
                if (categoriaActual.equals(mContext.getString(R.string.todas))) {
                    //No hay filtros puestos
                    producto.setIsInCatalogo(true);
                    productosCatalogo.add(producto);
                } else if (producto.getNombreCategoria().equals(categoriaActual)) {
                    //la categoria coincide y no hay filtros de marcas
                    producto.setIsInCatalogo(true);
                    productosCatalogo.add(producto);
                } else {
                    //No hay filtro de marcas, no son la misma categoria
                    producto.setIsInCatalogo(false);
                }
            } else if (producto.getMarca().equals(marcaActual)) {
                //la marca coincide, verificar contra la categoria
                if (categoriaActual.equals(mContext.getString(R.string.todas))) {
                    //No hay filtro de categoria puesto y la marca coincide
                    producto.setIsInCatalogo(true);
                    productosCatalogo.add(producto);
                } else if (producto.getNombreCategoria().equals(categoriaActual)) {
                    //la categoria y la marca coinciden
                    producto.setIsInCatalogo(true);
                    productosCatalogo.add(producto);
                } else {
                    //La marca coincide pero no es la misma categoria
                    producto.setIsInCatalogo(false);
                }
            } else {
                //hay filtro de marca puesto, no coinciden
                producto.setIsInCatalogo(false);
            }
        }
        return productosCatalogo;
    }
}
