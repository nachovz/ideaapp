package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

public class Marca {
	/** Cadena de texto que contiene el nombre de la marca*/
	private String nombre;
	/** ArrayList que contiene las metas para esta marca*/
	private ArrayList<Meta> metas;
	/** ArrayList que contiene los productos de esta marca*/
	private ArrayList<Producto> productos;
    private ArrayList<Categoria> categorias;

    private Marca(){
        metas = new ArrayList<Meta>();
        productos = new ArrayList<Producto>();
        categorias = new ArrayList<Categoria>();
    }

    public Marca(String nombreParam){
        this();
        nombre = nombreParam;
    }

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

    @Override
    public String toString(){
        return nombre;
    }

    /** ArrayList que contiene las categorias de esta marca */
    public ArrayList<Categoria> getCategorias() {
        return categorias;
    }

    public void setCategorias(ArrayList<Categoria> categorias) {
        this.categorias = categorias;
    }

    /**
     * Funcion que verifica la existencia de una <code>Categoria</code> dentro de la <code>Marca</code>
     * @param categoriaNombre nombre de la <code>Categoria</code> buscada
     * @return resultado de la busqueda, <code>true</code> de existir, <code>false</code> en caso contrario
     */
    public boolean findCategoria(String categoriaNombre){
        for(Categoria categoria: categorias){
            if(categoria.getNombre().equalsIgnoreCase(categoriaNombre)){
                return true;
            }
        }
        return false;
    }

    public boolean addCategoria(Categoria categoriaParam){
        return categorias.add(categoriaParam);
    }
}
