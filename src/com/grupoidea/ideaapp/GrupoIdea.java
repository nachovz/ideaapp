package com.grupoidea.ideaapp;

import android.app.Application;

import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.Parse;
import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.List;

/** Clase que permite inicializar y almacenar valores globales de la aplicacion*/
public class GrupoIdea extends Application {
	/** Cadena de texto que contiene el identificador unico de la aplicacion en Parse*/
	public static final String PARSE_APP_ID = "UIoWxIGXSIzMSB7osoxUHkK72yOrHHLzqNwA9O2B";//"74hxdwCXYYP2jtbHuX4mVVbX1HjpkOSAqmdMM4pp";
	/** Cadena de texto que contiene el identificador unico del cliente en Parse*/
	public static final String PARSE_CLIENT_KEY = "fCR8ESnjzQnNCHxaMZTVzvfaZn7ffbCZqDpw7Tax";//"Yn7uSk9casb9IXshqdSKIuIajoMMOGh6GuXyPYGU";

    public ArrayList<Producto> productos;
    public List<ParseObject> productosParse;

    public ArrayList<Meta> metas;
    public List<ParseObject> metasParse;

    public Pedido pedido;
    public List<ParseObject> pedidos;
    public Cliente clienteActual;
    public double iva;
    public static boolean hasInternet = false;

    public ArrayList<Cliente> clientes;

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);
	}

    /**
     * Funcion que busca la meta que coincida con el producto y usuario indicado en las metas almacenadas en <code>Application</code>
     *
     * @param codigoProducto codigo del producto de la meta
     * @return el <code>ParseObject</code> de la Meta, <code>null</code> otherwise
     */
    public Meta findMetaByProductCode(String codigoProducto){
        for (Meta meta:metas){
            if(meta.getCodigo().equals(codigoProducto)){
                return meta;
            }
        }
        return null;
    }
}