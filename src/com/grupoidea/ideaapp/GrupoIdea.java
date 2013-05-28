package com.grupoidea.ideaapp;

import android.app.Application;

import com.parse.Parse;

/** Clase que permite inicializar y almacenar valores globales de la aplicacion*/
public class GrupoIdea extends Application {
	/** Cadena de texto que contiene el identificador unico de la aplicacion en Parse*/
	public static final String PARSE_APP_ID = "UIoWxIGXSIzMSB7osoxUHkK72yOrHHLzqNwA9O2B";//"74hxdwCXYYP2jtbHuX4mVVbX1HjpkOSAqmdMM4pp";
	/** Cadena de texto que contiene el identificador unico del cliente en Parse*/
	public static final String PARSE_CLIENT_KEY = "fCR8ESnjzQnNCHxaMZTVzvfaZn7ffbCZqDpw7Tax";//"Yn7uSk9casb9IXshqdSKIuIajoMMOGh6GuXyPYGU";
	
	@Override
	public void onCreate() {
		super.onCreate();
		Parse.initialize(this, PARSE_APP_ID, PARSE_CLIENT_KEY);
	}
}