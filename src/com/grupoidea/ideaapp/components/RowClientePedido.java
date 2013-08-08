package com.grupoidea.ideaapp.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.widget.*;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DashboardActivity;
import com.grupoidea.ideaapp.activities.ParentActivity;
import com.grupoidea.ideaapp.models.Pedido;
import com.parse.*;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;

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
    private View productosPedidoView;

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
            fechaPedido.setText(DateFormat.getDateInstance().format(createdAt));
        }else{
            fechaPedido.setText(DateFormat.getDateInstance().format(createdAt)+"   (Editado: "+DateFormat.getDateInstance().format(updatedAt)+")");
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
                    new AlertDialog.Builder(contextDialog).setMessage(observacionesRechazoPedido).setTitle(contextDialog.getString(R.string.obs_rechazo_pedido_alert_title)).show();
                }
            });


        }else if(estado == Pedido.ESTADO_VERIFICANDO){
            //editar pedido
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    arg0.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
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
        }else if(estado == Pedido.ESTADO_APROBADO){
            //editar pedido
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

            //mostrar view pedido aprobado
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Obtener ParseObject de Pedido
                    ParseQuery queryPedido = new ParseQuery("Pedido");
                    queryPedido.getInBackground(idPedido, new GetCallback() {
                        @Override
                        public void done(ParseObject pedidoParse, ParseException e) {
                        //Obtener productos relacionados al pedido
                        ParseQuery queryProductosPedido = new ParseQuery("PedidoHasProductos");
                        queryProductosPedido.whereEqualTo("pedido", pedidoParse);
                        queryProductosPedido.include("producto");
                        queryProductosPedido.findInBackground(new FindCallback() {
                            @Override
                            public void done(List<ParseObject> productosPedido, ParseException e) {
                                if(e==null){
                                double subtotal = 0.0;
                                final AlertDialog.Builder builder = new AlertDialog.Builder(contextDialog);
                                final LayoutInflater inflater = ((DashboardActivity)contextDialog).getLayoutInflater();
                                View progress = null;
                                progress = inflater.inflate(R.layout.productos_pedido_aprobado_progress_layout, null, false);
                                builder.setView(progress);
                                builder.setTitle("Informacion del Pedido");
                                builder.setNegativeButton("Atras", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                    // Canceled.
                                    dialog.cancel();
                                    dialog.dismiss();
                                    }
                                    });
                                final AlertDialog alertProgress = builder.create();
                                alertProgress.show();

                                productosPedidoView = null;
                                productosPedidoView = inflater.inflate(R.layout.productos_pedido_aprobado_layout, null, false);
                                builder.setView(productosPedidoView);
                                final AlertDialog alert = builder.create();
                                TableRow tr; TextView tv; TableRow.LayoutParams params; ParseObject productoPedido;
                                TableLayout tl = (TableLayout) productosPedidoView.findViewById(R.id.productos_pedido_aprobado);
                                tl.removeAllViews();
//                                Log.d("DEBUG", "Productos en Pedido Dialogo: "+productosPedido.size());
                                if(productosPedido.size()>0){
                                    for(int i = 0, size = productosPedido.size(); i<size; i++){
                                        productoPedido = productosPedido.get(i);
                                        tr = new TableRow(productosPedidoView.getContext());
                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.5"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(18,5,18,5);
                                        tv.setText(productoPedido.getParseObject("producto").getString("codigo"));
                                        tr.addView(tv);

                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.1"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(18,5,18,5);
                                        tv.setText(productoPedido.get("cantidad").toString());
                                        tr.addView(tv);

                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.25"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(18,5,18,5);
                                        if(productoPedido.get("precio_unitario")!= null){
                                            tv.setText(productoPedido.get("precio_unitario").toString());
                                        }else{
                                            tv.setText("");
                                        }
                                        tr.addView(tv);

                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.1"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(18,5,0,5);
                                        tv.setText(productoPedido.get("descuento").toString());
                                        tr.addView(tv);

                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.15"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(0,5,18,5);
                                        if(productoPedido.getBoolean("manual")){
                                            tv.setText("M");
                                        }else{
                                            tv.setText("-");
                                        }
                                        tr.addView(tv);
                                        tv = new TextView(productosPedidoView.getContext());
                                        params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.3"));
                                        tv.setLayoutParams(params);
                                        tv.setTextColor(Color.parseColor("#FFFFFF"));
                                        tv.setPadding(18, 5, 18, 5);
                                        tv.setText(productoPedido.get("monto").toString());
                                        tr.addView(tv);
                                        subtotal += productoPedido.getDouble("monto");
                                        tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    }
                                    ParseQuery queryIva = new ParseQuery("Impuestos");
                                    queryIva.whereEqualTo("nombre","IVA");
                                    final double finalSubtotal = subtotal;
                                    final AlertDialog.Builder finalBuilder = builder;
                                    queryIva.getFirstInBackground(new GetCallback() {
                                        @Override
                                        public void done(ParseObject parseImp, ParseException e) {
                                            TextView tv = (TextView) productosPedidoView.findViewById(R.id.subtotal_edit_aprobado);
                                            tv.setText(String.valueOf(finalSubtotal));

                                            tv = (TextView) productosPedidoView.findViewById(R.id.impuesto_edit_aprobado);
                                            Double imp = finalSubtotal * (parseImp.getDouble("porcentaje")/100.0);
                                            tv.setText(String.valueOf(imp));

                                            tv = (TextView) productosPedidoView.findViewById(R.id.total_edit_aprobado);
                                            tv.setText(String.valueOf(finalSubtotal+imp));
                                            alertProgress.dismiss();
                                            alert.show();
                                        }
                                    });
                                }else{
                                    tr = new TableRow(productosPedidoView.getContext());
                                    tv = new TextView(productosPedidoView.getContext());
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("1"));
                                    tv.setLayoutParams(params);
                                    tv.setTextColor(Color.parseColor("#FFFFFF"));
                                    tv.setPadding(18, 0, 18, 0);
                                    tv.setText("Este pedido no tiene productos asociados");
                                    tl.removeAllViews();
                                    tr.addView(tv);
                                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
//                                    builder.show();
                                    alert.show();
                                    tl.removeAllViews();
                                }
                                }

                            }
                        });
                        }
                    });
                }
            });
        }else{
            //estado anulado o error
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

        final Animation animation = new AlphaAnimation(1, (float) 0.6); // Change alpha from fully visible to invisible
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
