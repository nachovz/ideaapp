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
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DashboardActivity;
import com.grupoidea.ideaapp.activities.ParentActivity;
import com.grupoidea.ideaapp.activities.ParentMenuActivity;
import com.grupoidea.ideaapp.models.Pedido;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

/**
 * Clase que permite crear una fila con el nombre del cliente con el estado y la
 * fecha del pedido que se realizo
 * 
 * @author ignaciocordoba
 * 
 */
public class RowPedido extends RelativeLayout {
    private RelativeLayout rowPedidoLayout;
	private RelativeLayout fillLayout;
	private RelativeLayout backgroundLayout;
	private FrameLayout rowStatusLayout;
    private View productosPedidoView;

	private TextView rowClienteTextView, rowCodPedidoTextView, rowFechaPedidoTextView;

	private ParentActivity parent;
	public final Context context;
	public String idPedido;
    public String numPedido;
    public String observacionesRechazoPedido;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");

    /*Estado del pedido*/
	private int estado;
    private boolean previewExists;

	/**
	 * Constructor de clase RowPedido. Permite crear una fila con el
	 * nombre del cliente (Razon Social) con la que se genero el pedido, la
	 * fecha de creacion del pedido y el estado actual.
	 * 
	 * @param contextParam
	 *            Contexto actual de la app
	 * @param nombreCliente
	 *            Cadena de texto con el nombre (Razon Social) del cliente
     * @param estadoParam
     *            estado del pedido
     * @param numPedidoParam
     *            Codigo del Pedido, se muestra en la vista con el formato "#numPedidoParam"
     * @param createdAt
     *            Fecha en que fue creado el pedido
     * @param updatedAt
     *            Fecha en que fue actualizado el pedido
	 */
	public RowPedido(Context contextParam, String idPedidoParam, String nombreCliente, int estadoParam, final String numPedidoParam, Date createdAt, Date updatedAt) {
		super(contextParam);

        this.context = contextParam;
		LayoutInflater inflater;
        previewExists = false;
        numPedido = numPedidoParam;
        idPedido = idPedidoParam;

		parent = (ParentActivity) context;
		inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		rowPedidoLayout = (RelativeLayout) inflater.inflate(R.layout.row_pedido_layout, this);
        rowPedidoLayout.setBackgroundResource(R.drawable.selector_row_pedido);

		rowClienteTextView = (TextView) rowPedidoLayout.findViewById(R.id.cliente_nombre_pedido_textview);
		rowClienteTextView.setText(nombreCliente);

        rowCodPedidoTextView = (TextView) rowPedidoLayout.findViewById(R.id.cliente_num_pedido_textview);
        rowCodPedidoTextView.setText("   #" + numPedidoParam);

        //Mostrar fechas de creacion y de actualizacion del pedido
        rowFechaPedidoTextView = (TextView) rowPedidoLayout.findViewById(R.id.cliente_pedido_date_textview);
        if(createdAt.equals(updatedAt)){
            rowFechaPedidoTextView.setText(DateFormat.getDateInstance().format(createdAt));
        }else{
            rowFechaPedidoTextView.setText(DateFormat.getDateInstance().format(createdAt) + "   (Editado: " + DateFormat.getDateInstance().format(updatedAt) + ")");
        }

        //Status de Pedido
		rowStatusLayout = (FrameLayout) rowPedidoLayout.findViewById(R.id.cliente_pedido_status_layout);
		estado = estadoParam;
		this.setEstado(estado);


        /**
         *
         * Establecer acciones para taps y long taps segun estado de pedido
         *
         */

        /**
         * Estado RECHAZADO
         */
		if (estado == Pedido.ESTADO_RECHAZADO) {
            /**
             * Long Click
             * Editar Pedido Rechazado
             */
			this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.rechazado_dialog_message))
                            .setTitle(context.getString(R.string.rechazado_dialog_title))
                            .setPositiveButton(context.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Relanzar pedido como "nuevo" pedido
                                    Bundle bundle = new Bundle();
                                    GrupoIdea app = (GrupoIdea) parent.getApplication();
                                    app.clienteActual = ((ParentMenuActivity)parent).findClienteByName(rowClienteTextView.getText().toString());
                                    app.pedido = new Pedido();
                                    app.pedido.setObjectId(idPedido);
                                    app.pedido.setNumPedido(numPedido);
                                    app.pedido.setEstado(Pedido.ESTADO_RECHAZADO);
//                                    bundle.putString("cliente", "" + rowClienteTextView.getText().toString());
//                                    bundle.putString("idPedido", idPedido);
//                                    bundle.putString("numPedido", numPedido);
//                                    bundle.putInt("status", Pedido.ESTADO_RECHAZADO);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle, false);
                                }
                            })
                            .setNegativeButton(context.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    //Do Nothing
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });

            /**
             * Click
             * Mostrar Obsrvaciones de Rechazo
             */
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    v.setEnabled(false);
                    new AlertDialog.Builder(context).setMessage(observacionesRechazoPedido).setTitle(context.getString(R.string.obs_rechazo_pedido_alert_title)).show();
                    v.setEnabled(true);
                }
            });

        /**
         * Estado VERIFICANDO
         */
        }else if(estado == Pedido.ESTADO_VERIFICANDO){

            /**
             * Long Click
             * Editar Pedido
             */
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    arg0.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.verificando_dialog_message))
                            .setTitle(context.getString(R.string.verificando_dialog_title))
                            .setPositiveButton(context.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Bundle bundle;
                                    bundle = new Bundle();
                                    GrupoIdea app = (GrupoIdea) parent.getApplication();
                                    app.clienteActual = ((ParentMenuActivity)parent).findClienteByName(rowClienteTextView.getText().toString());
                                    app.pedido = new Pedido();
                                    app.pedido.setObjectId(idPedido);
                                    app.pedido.setNumPedido(numPedido);
                                    app.pedido.setEstado(Pedido.ESTADO_VERIFICANDO);
//                                    bundle.putString("cliente", "" + rowClienteTextView.getText());
//                                    bundle.putString("idPedido", idPedido);
//                                    bundle.putString("numPedido", numPedido);
//                                    bundle.putInt("status", Pedido.ESTADO_VERIFICANDO);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle, false);
                                }
                            })
                            .setNegativeButton(context.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });
        /**
         * Estado APROBADO
         */
        }else if(estado == Pedido.ESTADO_APROBADO){
            /**
             * Long Click
             * Clonar Pedido
             */
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.copiar_dialog_message))
                            .setTitle(context.getString(R.string.copiar_dialog_title))
                            .setPositiveButton(context.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                    Bundle bundle;
                                    bundle = new Bundle();
                                    GrupoIdea app = (GrupoIdea) parent.getApplication();
                                    app.pedido = new Pedido();
                                    app.pedido.setObjectId(idPedido);
//                                    bundle.putString("idPedido", idPedido);
                                    parent.dispatchActivity(CatalogoActivity.class, bundle, false);
                                }
                            })
                            .setNegativeButton(context.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {
                                }
                            });
                    AlertDialog dialog = builder.create();
                    dialog.show();

                    return true;
                }
            });

            /**
             * Click
             * Mostrar preview de pedido aprobado
             */
            this.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                    //Crear dialogo de carga
                    final AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    final LayoutInflater inflater = ((DashboardActivity)context).getLayoutInflater();
                    View progress = null;
                    progress = inflater.inflate(R.layout.loading_layout, null, false);
                    builder.setView(progress);
                    builder.setTitle("Informacion del Pedido");
                    builder.setNegativeButton("Atras", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            // Canceled.
                            dialog.cancel();
                            dialog.dismiss();
                        }
                    });

                    //Mostrar dialogo de carga
                    final AlertDialog alertProgress = builder.create();
                    alertProgress.show();

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
                                    //Crear dialogo de preview
                                    productosPedidoView = null;
                                    productosPedidoView = inflater.inflate(R.layout.pedido_aprobado_preview_layout, null, false);
                                    builder.setView(productosPedidoView);
                                    final AlertDialog alert = builder.create();
                                    setPreview(productosPedido, numPedidoParam);
                                    alertProgress.dismiss();
                                    alert.show();
                                }

                            }
                        });
                        }
                    });
                }
            });
        }else{

            /**
             * Pedido Anulado o Error
             */
            this.setOnLongClickListener(new OnLongClickListener() {
                @Override
                public boolean onLongClick(View arg0) {
                    //Levantar dialogo de confirmacion
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setMessage(context.getString(R.string.copiar_dialog_message))
                        .setTitle(context.getString(R.string.copiar_dialog_title))
                        .setPositiveButton(context.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                Bundle bundle;
                                bundle = new Bundle();
                                GrupoIdea app = (GrupoIdea) parent.getApplication();
                                app.pedido = new Pedido();
                                app.pedido.setObjectId(idPedido);
//                                bundle.putString("idPedido", idPedido);
                                parent.dispatchActivity(CatalogoActivity.class, bundle, false);
                            }
                        })
                        .setNegativeButton(context.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
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
	 * @param status Cadena de texto con el estado actual*/
	private View createStatusTag(String status) {
		TextView textView = new TextView(context);
		textView.setText(status);
		textView.setTextSize(18);
		textView.setTypeface(null, Typeface.BOLD_ITALIC);
		textView.setTextColor(Color.WHITE);
		textView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

        final Animation animation = new AlphaAnimation(1, (float) 0.6);
        animation.setDuration(1000);
        animation.setInterpolator(new LinearInterpolator());
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.REVERSE);

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
	 * Permite colocar la etiqueta del estado para el pedido actual. Esta funcion sobreescribe el estado anterior.
	 * @param estado Valor del Estado en <code>Pedido.ESTADO_*</code>
	 */
	public void setEstado(int estado) {
		String estadoString;
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
		rowStatusLayout.addView(createStatusTag(estadoString));
	}

    /**
     * Metodo que setea el preview del pedido
     * @param productosPedido productos que componen el pedido
     * @param codPedido codigo del pedido
     */
    protected void setPreview(List<ParseObject> productosPedido, String codPedido){
        TextView tv; TableRow tr; TableRow.LayoutParams params; double subtotal = 0.0;

        final TableLayout tl = (TableLayout) productosPedidoView.findViewById(R.id.productos_pedido_aprobado);
        tl.removeAllViews();

        tv = (TextView)productosPedidoView.findViewById(R.id.pedido_aprobado_preview_pedido_number_textView);
        tv.setText("Pedido #"+codPedido);

        if(productosPedido.size() >0){
            for(ParseObject productoPedido : productosPedido){
                tr = new TableRow(context);
                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(18,5,18,5);
                tv.setText(productoPedido.getParseObject("producto").getString("codigo"));
                tr.addView(tv);

                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(18,5,18,5);
                tv.setText(productoPedido.get("cantidad").toString());
                tr.addView(tv);

                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(18,5,18,5);
                if(productoPedido.get("precio_unitario")!= null){
                    tv.setText(df.format(productoPedido.get("precio_unitario")));
                }else{
                    tv.setText("");
                }
                tr.addView(tv);

                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(18,5,0,5);
                tv.setText(productoPedido.get("descuento").toString());
                tr.addView(tv);

                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(0,5,18,5);
                if(productoPedido.getBoolean("manual")){
                    tv.setText("M");
                }else{
                    tv.setText("-");
                }
                tr.addView(tv);

                tv = new TextView(context);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 3);
                tv.setLayoutParams(params);
                tv.setTextColor(Color.parseColor("#FFFFFF"));
                tv.setPadding(18, 5, 18, 5);
                tv.setText(df.format(productoPedido.get("monto")));
                tr.addView(tv);

                subtotal += productoPedido.getDouble("monto");

                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }


            //Setear subtotal sin IVA
            tv = (TextView) productosPedidoView.findViewById(R.id.subtotal_edit_aprobado);
            tv.setText(df.format(subtotal));

            //Setear Impuesto
            tv = (TextView) productosPedidoView.findViewById(R.id.impuesto_edit_aprobado);
            Double imp = subtotal * (((DashboardActivity)parent).getIva());
            tv.setText(df.format(imp));

            //Setear Total
            tv = (TextView) productosPedidoView.findViewById(R.id.total_edit_aprobado);
            tv.setText(df.format(subtotal+imp));

        }else{
            //Pedido sin productos asociados
            tr = new TableRow(context);
            tv = new TextView(context);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("1"));
            tv.setLayoutParams(params);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setPadding(18, 0, 18, 0);
            tv.setText("Este pedido no tiene productos asociados");
            tl.removeAllViews();
            tr.addView(tv);
            tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
        }

        previewExists = true;
    }
}
