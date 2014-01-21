package com.grupoidea.ideaapp;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;

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

    public ArrayList<Cliente> clientes;

	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);
	}

    public static boolean isNetworkAvailable(Context context){
        return ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo() != null;
    }
}