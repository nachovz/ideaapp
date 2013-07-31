package com.grupoidea.ideaapp.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
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
	public Context context;
	public String idPedido;
    public String numPedido;
    public String observacionesRechazoPedido;

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
	public RowClientePedido(Context context, String nombreCliente, int estadoParam, String codPedido, Date createdAt, Date updatedAt) {
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

        view = rowClienteLayout.findViewById(R.id.cliente_num_pedido_textview);
        clienteNombre = (TextView) view;
        clienteNombre.setText("   #"+codPedido);

        //Mostrar fechas de creacion y de actualizacion del pedido
        view = rowClienteLayout.findViewById(R.id.cliente_pedido_date_textview);
        fechaPedido = (TextView) view;
        if(createdAt.equals(updatedAt)){
            fechaPedido.setText("Creado: "+DateFormat.getDateInstance().format(createdAt));
        }else{
            fechaPedido.setText("Creado: "+DateFormat.getDateInstance().format(createdAt)+" (Editado: "+DateFormat.getDateInstance().format(updatedAt)+")");
        }
		view = rowClienteLayout.findViewById(R.id.cliente_pedido_status_layout);
		estatusLayout = (FrameLayout) view;
		
		estado = estadoParam;
		this.addEstado(estado);

        //Establecer acciones para taps y long taps segun estado de pedido
        final Context contextDialog = context;
		if (estado == Pedido.ESTADO_RECHAZADO) {
			this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextDialog);
                    builder.setMessage(contextDialog.getString(R.string.rechazado_dialog_message))
                            .setTitle(contextDialog.getString(R.string.rechazado_dialog_title))
                            .setPositiveButton(contextDialog.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Bundle bundle;
                                    bundle = new Bundle();
                                    bundle.putString("clienteNombre", "" + clienteNombre.getText());
                                    bundle.putString("idPedido", idPedido);
                                    bundle.putString("numPedido", numPedido);
                                    bundle.putInt("status", Pedido.ESTADO_RECHAZADO);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle,false);
                                }
                            })
                            .setNegativeButton(contextDialog.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });

            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
//                    Log.d("DEBUG", "obs rechazo: " + observacionesRechazoPedido);
                    new AlertDialog.Builder(contextDialog).setMessage(observacionesRechazoPedido).setTitle(contextDialog.getString(R.string.obs_rechazo_pedido_alert_title)).show();
                }
            });


        }else if(estado == Pedido.ESTADO_VERIFICANDO){
            //editar pedido
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextDialog);
                    builder.setMessage(contextDialog.getString(R.string.verificando_dialog_message))
                            .setTitle(contextDialog.getString(R.string.verificando_dialog_title))
                            .setPositiveButton(contextDialog.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Bundle bundle;
                                    bundle = new Bundle();
                                    bundle.putString("clienteNombre", ""+clienteNombre.getText());
                                    bundle.putString("idPedido", idPedido);
                                    bundle.putString("numPedido", numPedido);
                                    bundle.putInt("status", Pedido.ESTADO_VERIFICANDO);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle,false);
                                }
                            })
                            .setNegativeButton(contextDialog.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });
        }else{
            //estado aprobado estado anulado
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(contextDialog);
                    builder.setMessage(contextDialog.getString(R.string.copiar_dialog_message))
                            .setTitle(contextDialog.getString(R.string.copiar_dialog_title))
                            .setPositiveButton(contextDialog.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Bundle bundle;
                                    bundle = new Bundle();
                                    bundle.putString("idPedido", idPedido);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle,false);
                                }
                            })
                            .setNegativeButton(contextDialog.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
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

        final Animation animation = new AlphaAnimation(1, (float) 0.25); // Change alpha from fully visible to invisible
        animation.setDuration(1000); // duration - half a second
        animation.setInterpolator(new LinearInterpolator()); // do not alter animation rate
        animation.setRepeatCount(Animation.INFINITE); // Repeat animation infinitely
        animation.setRepeatMode(Animation.REVERSE); // Reverse animation at the end so the button will fade back in

        switch (estado) {
            case Pedido.ESTADO_VERIFICANDO:
                textView.setBackgroundResource(R.drawable.pastilla_verificando);
                break;
            case Pedido.ESTADO_APROBADO:
                textView.setBackgroundResource(R.drawable.pastilla_aprobado);
                break;
            case Pedido.ESTADO_RECHAZADO:
                textView.setBackgroundResource(R.drawable.pastilla_rechazado);
                textView.startAnimation(animation);
                break;
            case Pedido.ESTADO_ANULADO:
                textView.setBackgroundResource(R.drawable.pastilla_anulado);
                break;
            default:
                textView.setBackgroundResource(R.drawable.pastilla_error);
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
        case Pedido.ESTADO_ANULADO:
            estadoString = "ANULADO";
            break;
		default:
			estadoString = "ERROR";
			break;
		}
		estatusLayout.addView(createStatusTag(estadoString));
	}
}
