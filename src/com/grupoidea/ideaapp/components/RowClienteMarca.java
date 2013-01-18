package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.ParentActivity;

/** Clase que permite crear una fila con el nombre del cliente y un listado de tags que identifican las marcas adquiridas
 *  por este cliente.*/
public class RowClienteMarca extends RelativeLayout{
	private RelativeLayout rowClienteLayout;
	private RelativeLayout fillLayout;
	private RelativeLayout backgroundLayout;
	private LinearLayout marcasLayout;
	
	private TextView clienteNombre;
	private TextView clienteInversion;
	
	private ParentActivity parent;
	private Context context;
	
	/** Clase que permite crear una fila con el nombre del cliente y un listado de tags que identifican las marcas adquiridas
	 *  por este cliente.
	 *  @param context Contexto actual de la app
	 *  @nombreCliente Cadena de texto que contiene el nombre del cliente que se mostrara en el listado*/
	public RowClienteMarca(Context context, String nombreCliente) {
		super(context);
		this.context = context;
		
		View view;
		LayoutInflater inflater;
		
		parent = (ParentActivity) context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.row_cliente_marca_layout, this);
		rowClienteLayout = (RelativeLayout) view;
		
		view = rowClienteLayout.findViewById(R.id.cliente_nombre_text_view);
		clienteNombre = (TextView) view;
		clienteNombre.setText(nombreCliente);
		
		view = rowClienteLayout.findViewById(R.id.marcas_layout);
		marcasLayout = (LinearLayout) view;
		
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle bundle;
				//TODO: Enviar idCliente mediante el bundle para que el CatalogoActivity se encargue de consultar los productos destacados del cliente
				bundle = new Bundle();
				bundle.putString("clienteNombre", clienteNombre.getText().toString());
				parent.dispatchActivity(CatalogoActivity.class, bundle, false);
			}
		});
	}
	
	/** Metodo privado que permite crear un TextView con el nombre de la marca asociada a un cliente.
	 *  @param tagName Cadena de texto con el nombre del tag.*/
	private View createTag(String tagName) {
		LinearLayout.LayoutParams layoutParams;
		TextView textView;
		Drawable drawable;
		
		textView = new TextView(context);
		textView.setText(tagName);
		textView.setTextSize(20);
		textView.setTypeface(null, Typeface.BOLD_ITALIC);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		drawable = getResources().getDrawable(R.drawable.client_img_pastilla);
		//Se utiliza este metodo deprecado por el min-API:13.
		textView.setBackgroundDrawable(drawable);
		layoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		layoutParams.setMargins(7, 0, 0, 0); // llp.setMargins(left, top, right, bottom);
		textView.setLayoutParams(layoutParams);
	    
		return textView;
	}
	
	/** Permite agregar un tag de marca al cliente actual.
	 *  @param nombre Cadena de texto con el nombre de la marca*/
	public void addMarca(String nombre) {
		marcasLayout.addView(createTag(nombre));
	}
}
