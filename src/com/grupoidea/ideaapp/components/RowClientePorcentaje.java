package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.ParentActivity;

/** Clase que permite crear una fila con el nombre y monto invertido por el cliente, esta fila permite mostrar un porcentaje de inversion del cliente
 *  por medio de un fill blanco en el fondo del mismo.*/
public class RowClientePorcentaje extends RelativeLayout {
	private RelativeLayout rowClienteLayout;
	private RelativeLayout fillLayout;
	private RelativeLayout backgroundLayout;
	private LinearLayout porcentajeLayout;
	
	private TextView clienteNombre;
	private TextView clienteInversion;
	
	private ParentActivity parent;
	
	/** Permite crear una fila con el nombre y monto invertido por el cliente, esta fila permite mostrar un porcentaje de inversion del cliente
	 *  por medio de un fill blanco en el fondo del mismo.
	 *  @param context Objeto que representa el contexto en el cual se esta instanciando el Row
	 *  @param porcentaje Entero que contiene el porcentaje de inversion del cliente con respecto al total de ventas del vendedor,
	 *  para mostrar esta informacion el row se llenara de color blanco hasta el punto que represente el porcentaje con respecto
	 *  al ancho de la pantalla del dispositivo (100%).
	 *  @param nombreCliente Cadena de texto que contiene el nombre del cliente.
	 *  @param inversion Numero que contiene el dinero invertido por el cliente para el vendedor actual.
	 *  @deprecated*/
	public RowClientePorcentaje(Context context, int porcentaje, String nombreCliente, float inversion) {
		super(context);
		float fillWidth;
		float backgroundWidth;
		
		View view;
		LayoutInflater inflater;
		LinearLayout.LayoutParams params;
		
		parent = (ParentActivity) context;
		
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.row_cliente_porcentaje_layout, this);
		rowClienteLayout = (RelativeLayout) view;
		
		view = rowClienteLayout.findViewById(R.id.cliente_nombre_text_view);
		clienteNombre = (TextView) view;
		clienteNombre.setText(nombreCliente);
		
		view = rowClienteLayout.findViewById(R.id.cliente_inversion_text_view);
		clienteInversion = (TextView) view;
		clienteInversion.setText(new StringBuffer(Float.toString(inversion)).append(" Bs.").toString());
		
		view = rowClienteLayout.findViewById(R.id.porcentaje_layout);
		porcentajeLayout = (LinearLayout) view;
		
		porcentaje = porcentaje > 100 ? 100 : porcentaje;
		porcentaje = porcentaje < 0 ? 0 : porcentaje;
		
		fillWidth = (float) (100-porcentaje)/100;
		fillLayout = new RelativeLayout(context);
		fillLayout.setBackgroundColor(0xffffffff);
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, fillWidth);
		porcentajeLayout.addView(fillLayout, params);
		
		backgroundWidth = (float) porcentaje/100;
		backgroundLayout = new RelativeLayout(context);
		backgroundLayout.setBackgroundColor(0xfff0821e);
		params = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, backgroundWidth);
		porcentajeLayout.addView(backgroundLayout, params);
		
		this.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Bundle bundle;bundle = new Bundle();
				bundle.putString("clienteNombre", clienteNombre.getText().toString());
				parent.dispatchActivity(CatalogoActivity.class, bundle, false);
			}
		});
	}
}
