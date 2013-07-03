package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.ParentActivity;
import com.grupoidea.ideaapp.models.Pedido;

import java.text.DateFormat;
import java.util.Date;

/**
 * Clase que permite crear una fila con el nombre del cliente con el estado y la
 * fecha del pedido que se realizo
 * 
 * @author ignaciocordoba
 * 
 */
public class RowClientePedido extends RelativeLayout {
	private RelativeLayout rowClienteLayout;
	private RelativeLayout fillLayout;
	private RelativeLayout backgroundLayout;
	private FrameLayout estatusLayout;

	private TextView clienteNombre;
	private TextView fechaPedido;

	private ParentActivity parent;
	private Context context;
	
	private int estado;

	/**
	 * Constructor de clase RowClientePedido. Permite crear una fila con el
	 * nombre del cliente (Razon Social) con la que se genero el pedido, la
	 * fecha de creacion del pedido y el estado actual.
	 * 
	 * @param context
	 *            Contexto actual de la app
	 * @param nombreCliente
	 *            Cadena de texto con el nombre (Razon Social) del cliente
	 */
	public RowClientePedido(Context context, String nombreCliente, int estadoParam, Date creationDate) {
		super(context);
		this.context = context;
		
		View view;
		LayoutInflater inflater;
		
		parent = (ParentActivity) context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		view = inflater.inflate(R.layout.row_cliente_pedido_layout, this);
		rowClienteLayout = (RelativeLayout) view;
		
		view = rowClienteLayout.findViewById(R.id.cliente_nombre_pedido_textview);
		clienteNombre = (TextView) view;
		clienteNombre.setText(nombreCliente);

        view = rowClienteLayout.findViewById(R.id.cliente_pedido_date_textview);
        fechaPedido = (TextView) view;
        fechaPedido.setText(DateFormat.getDateInstance().format(creationDate));
		
		view = rowClienteLayout.findViewById(R.id.cliente_pedido_status_layout);
		estatusLayout = (FrameLayout) view;
		
		estado = estadoParam;
		this.addEstado(estado);
		
		if (estado == Pedido.ESTADO_RECHAZADO) {
			this.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					Bundle bundle;
					// TODO: Enviar idCliente mediante el bundle para que el
					// CatalogoActivity se encargue de consultar los productos
					// destacados del cliente
					bundle = new Bundle();
					bundle.putString("clienteNombre", clienteNombre.getText()
							.toString());
					parent.dispatchActivity(CatalogoActivity.class, bundle,
							false);
				}
			});
		}
	}
	
	/**
	 * Metodo privado que permite crear un TextView con el estado actual del pedido.
	 * @param status Cadena de texto con el estado actual.*/
	private View createStatusTag(String status) {
		TextView textView;
		
		textView = new TextView(context);
		textView.setText(status);
		textView.setTextSize(18);
		textView.setTypeface(null, Typeface.BOLD_ITALIC);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        switch (estado) {
            case Pedido.ESTADO_VERIFICANDO:
                textView.setBackgroundResource(R.drawable.pastillaverificando);
                break;
            case Pedido.ESTADO_APROBADO:
                textView.setBackgroundResource(R.drawable.pastillaaprobado);
                break;
            case Pedido.ESTADO_RECHAZADO:
                textView.setBackgroundResource(R.drawable.pastillarechazado);
                break;
            default:
                textView.setBackgroundResource(R.drawable.pastillaerror);
                break;
        }
	    
		return textView;
	}
	/**
	 * Permite colocar la etiqueta del estado para el pedido actual. Esta funcion sobre-escribe el estado anterior.
	 * @param estadoParam Valor del Estado en <code>Pedido.ESTADO_*</code>
	 */
	public void addEstado(int estadoParam) {
		String estadoString = "";
		
		switch (estado) {
		case Pedido.ESTADO_VERIFICANDO:
			estadoString = "VERIFICANDO";
			break;
		case Pedido.ESTADO_APROBADO:
			estadoString = "APROBADO";
			break;
		case Pedido.ESTADO_RECHAZADO:
			estadoString = "RECHAZADO";
			break;
		default:
			estadoString = "ERROR";
			break;
		}
		estatusLayout.addView(createStatusTag(estadoString));
	}
}
