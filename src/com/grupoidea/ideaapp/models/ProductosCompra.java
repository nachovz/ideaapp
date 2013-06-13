package com.grupoidea.ideaapp.models;

import java.util.ArrayList;

/**
 * Created by geeks on 12/06/13.
 */
public class ProductosCompra {
    private static ProductosCompra ourInstance = new ProductosCompra();
    private ArrayList<Producto> productos;

    public static ProductosCompra getInstance() {
        return ourInstance;
    }

    private ProductosCompra() {
    }

    public ArrayList<Producto> getProductos() {
        return productos;
    }

    public void setProductos(ArrayList<Producto> productos) {
        this.productos = productos;
    }
}
