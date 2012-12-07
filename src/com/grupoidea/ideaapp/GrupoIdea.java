package com.grupoidea.ideaapp;

import com.parse.Parse;
import android.app.Application;

/** Clase que permite inicializar y almacenar valores globales de la aplicación*/
public class GrupoIdea extends Application {
	/** Cadena de texto que contiene el identificador unico de la aplicacion en Parse*/
	public static final String PARSE_APP_ID = "74hxdwCXYYP2jtbHuX4mVVbX1HjpkOSAqmdMM4pp";
	/** Cadena de texto que contiene el identificador unico del cliente en Parse*/
	public static final String PARSE_CLIENT_KEY = "Yn7uSk9casb9IXshqdSKIuIajoMMOGh6GuXyPYGU";
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);
	}
	
}