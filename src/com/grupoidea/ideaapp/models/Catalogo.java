package com.grupoidea.ideaapp.models;

import android.content.Context;
import com.grupoidea.ideaapp.R;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Catalogo {
	private ArrayList<Producto> productos, productosCatalogo;
    private Context mContext;
    public DecimalFormat df = new DecimalFormat("###,###,##0.##");

	/** Constructor por defecto, permite instanciar el listado de productos.*/
	public Catalogo(Context context, ArrayList<Producto> prods) {
		mContext = context;
		//almacenar productos originales
        productos = prods;
        //crear ArrayList para los productos que se van a mostrar
        productosCatalogo = new ArrayList<Producto>(productos);
	}
	
	public ArrayList<Producto> getProductosCatalogo() {
		return productosCatalogo;
	}

    public ArrayList<Producto> getProductos() {
        return productos;
    }

	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
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
	
	/** Permite determinar si un producto se encuentra actualmente en el listado de productos
	 *  del catalogo, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Objeto de tipo de Producto con el producto solicitado, si el producto no esta en el 
	 *  		listado de productos del catalogo retorna null.*/
	public Producto findProductoById(String id) {
		Producto productoActual = null;
		Producto productoFinal = null;
		
		for(int i=0; i<productosCatalogo.size(); i++) {
			productoActual = productosCatalogo.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
				productoFinal = productoActual;
				break;
			}
		}
		return productoFinal;
	}

    public ArrayList<Producto> filter(String marcaActual, String categoriaActual){
        productosCatalogo = new ArrayList<Producto>();
        Producto prod = null;
        for(int i=0; i<productos.size(); i++) {
            prod = productos.get(i);
            if (marcaActual.equals(mContext.getString(R.string.todas))) {
                if(categoriaActual.equals(mContext.getString(R.string.todas))){
                    //No hay filtros puestos
                    prod.setIsInCatalogo(true);
                    productosCatalogo.add(prod);
                }else if(prod.getCategoria().equals(categoriaActual)){
                    //la categoria coincide y no hay filtros de marcas
                    prod.setIsInCatalogo(true);
                    productosCatalogo.add(prod);
                }else{
                    //No hay filtro de marcas, no son la misma categoria
                    prod.setIsInCatalogo(false);
                }
            }else if(prod.getMarca().equals(marcaActual)){
                //la marca coincide, verificar contra la categoria
                if(categoriaActual.equals(mContext.getString(R.string.todas))){
                    //No hay filtro de categoria puesto y la marca coincide
                    prod.setIsInCatalogo(true);
                    productosCatalogo.add(prod);
                }else if(prod.getCategoria().equals(categoriaActual)){
                    //la categoria y la marca coinciden
                    prod.setIsInCatalogo(true);
                    productosCatalogo.add(prod);
                }else{
                    //La marca coincide pero no es la misma categoria
                    prod.setIsInCatalogo(false);
                }
            }else{
                //hay filtro de marca puesto, no coinciden
                prod.setIsInCatalogo(false);
            }
        }
        return productosCatalogo;
    }
}
