package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import org.json.JSONArray;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestionPedidosActivity extends ParentMenuActivity {
    protected String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected JSONArray productosJSON;
    protected double subtotal, desc, flete, misc, imp, total;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");
    protected String denom, direccion, observaciones;
    protected ParseUser vendedor;
    protected Cliente cliente;
    /** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
    protected ArrayList<Producto> productos;
    protected String numPedido, idPedido;
    protected boolean editar = false;
    protected GrupoIdea app;
    protected ArrayList<ParseObject> pedidoHasProductoToSave;
    protected ArrayList<ParseObject> metaToSave;

    public GestionPedidosActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mContext=this;
        setContentView(R.layout.activity_gestion_pedidos_layout);

        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

        clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        app = (GrupoIdea) getApplication();
        productos = app.pedido.getProductos();
        idPedido = app.pedido.getObjectId();
        numPedido = app.pedido.getNumPedido();
        vendedor = ParseUser.getCurrentUser();
        cliente = app.clienteActual;
        denom = productos.get(0).getDenominacion();

        TextView text;
        //Fecha
            text = (TextView) findViewById(R.id.fecha_edit);
            Time now = new Time(); now.setToNow();
            text.setText(now.monthDay + "/" + now.month + "/" + now.year);
        //ID Cliente
            text = (TextView) findViewById(R.id.id_cliente_edit);
            text.setText(cliente.getCodigo());
        //Nombre Cliente
            text = (TextView) findViewById(R.id.nombre_cliente_edit);
            text.setText(cliente.getNombre());
        //# Orden Compra
            if(numPedido==null){
                editar = false;
                Random rand = new Random();
                numPedido = String.valueOf(56000 + rand.nextInt(10000));
                Log.d(TAG, "numPedido: " + numPedido);
            }else{
                editar = true;
            }
            text = (TextView) findViewById(R.id.numero_orden_compra_edit);
            text.setText("#"+numPedido);

        //Subtotal
            subtotal = getSubtotal();
            text = (TextView) findViewById(R.id.subtotal_edit);
            text.setText(df.format(subtotal)+" "+denom);

        //Impuesto
            imp=subtotal*(app.iva/100.0);
            text = (TextView) findViewById(R.id.impuesto_edit);
            text.setText(df.format(imp)+" "+denom);

        //Total
            total=subtotal+imp;
            text = (TextView) findViewById(R.id.total_edit);
            text.setText(df.format(total)+" "+denom);

        //llenar TableRow con productos
        TableLayout tl = (TableLayout)findViewById(R.id.listado_productos_pedido_table);
        TableRow tr;
        TableRow.LayoutParams params;
        String descProd;
        TextView nombreTextView, cantidadTextView, precioTextView, precioComercialTextView, descuentoTextView, precioFinalTextView;
        Boolean darkBackground = true;

        for (Producto prod : productos) {
            tr = new TableRow(this);
            tr.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));

            //Nombre
            nombreTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("3"));
            nombreTextView.setLayoutParams(params);
            nombreTextView.setTextColor(Color.parseColor("#262626"));
            nombreTextView.setPadding(18, 18, 18, 18);
            nombreTextView.setText(prod.getCodigo());
            tr.addView(nombreTextView);
            if (darkBackground) nombreTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else nombreTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Cantidad
            cantidadTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("2"));
            cantidadTextView.setLayoutParams(params);
            cantidadTextView.setTextColor(Color.parseColor("#262626"));
            cantidadTextView.setPadding(18, 18, 18, 18);
            cantidadTextView.setText(String.valueOf(prod.getCantidad()));
            tr.addView(cantidadTextView);
            if (darkBackground)
                cantidadTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else cantidadTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Precio Lista
            precioTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("3"));
            precioTextView.setLayoutParams(params);
            precioTextView.setTextColor(Color.parseColor("#262626"));
            precioTextView.setPadding(18, 18, 18, 18);
            precioTextView.setText(prod.getStringPrecio());
            tr.addView(precioTextView);
            if (darkBackground) precioTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else precioTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Precio con Desc Comercial
            precioComercialTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("3"));
            precioComercialTextView.setLayoutParams(params);
            precioComercialTextView.setTextColor(Color.parseColor("#262626"));
            precioComercialTextView.setPadding(18, 18, 18, 18);
            precioComercialTextView.setText(prod.getStringPrecioComercial());
            tr.addView(precioComercialTextView);
            if (darkBackground)
                precioComercialTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else precioComercialTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Descuento por Volumen
            descuentoTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("2"));
            descuentoTextView.setLayoutParams(params);
            descuentoTextView.setTextColor(Color.parseColor("#262626"));
            descuentoTextView.setPadding(18, 18, 18, 18);
            descProd = df.format(prod.getPrecioComercial() * prod.getDescuentoAplicado());
            descuentoTextView.setText(descProd + " (" + prod.getDescuentoAplicadoPorcString() + ")");
            tr.addView(descuentoTextView);
            if (darkBackground)
                descuentoTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else descuentoTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Precio Final
            precioFinalTextView = new TextView(this);
            params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("4"));
            precioFinalTextView.setLayoutParams(params);
            precioFinalTextView.setTextColor(Color.parseColor("#262626"));
            precioFinalTextView.setPadding(18, 18, 18, 18);
            precioFinalTextView.setText(prod.getStringPrecioComercialTotal());
            tr.addView(precioFinalTextView);
            if (darkBackground)
                precioFinalTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
            else precioFinalTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

            //Añadir a TableLayout de productos
            tl.addView(tr, tl.getChildCount(), new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            darkBackground = !darkBackground;
        }
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
		return null;
	}

    public void commitPedido(View view) {
        Log.d(TAG, "Finalizar Pedido Button clicked");
        Button finalizarPedidoButton = (Button) findViewById(R.id.finalizarPedidoButton);
        finalizarPedidoButton.setEnabled(false);

        Toast.makeText(mContext, R.string.subiendoPedido, Toast.LENGTH_SHORT).show();

        //Almacenar Direccion
        EditText direccionET = (EditText) findViewById(R.id.direccion_envio_edit);
        direccion = ""+String.valueOf(direccionET.getText());

        //Almacenar Observaciones
        EditText obs = (EditText) findViewById(R.id.obs_editText);
        observaciones = ""+String.valueOf(obs.getText());

        //Obtener Cliente de Parse desde Application
        ParseObject clienteParse = app.clienteActual.getClienteParse();
        //Crear Pedido
        final ParseObject pedidoParse;

        // Si es un pedido nuevo
        if(!editar){
            //Instanciar nuevo ParseObject de Pedido
            Log.d(TAG, "Creando nuevo Pedido para Parse");
            pedidoParse = new ParseObject("Pedido");
            pedidoParse.put("asesor", ParseUser.getCurrentUser());
            pedidoParse.put("cliente", clienteParse);
            pedidoParse.put("direccion", direccion);
            pedidoParse.put("estado", 0);
            pedidoParse.put("num_pedido", numPedido);
            pedidoParse.put("comentario", observaciones);

            pedidoParse.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if(e == null){

                        for (final Producto prod : productos) {
                            //Buscar ParseObject de Producto
                            final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                            pedidoHasProductoToSave = new ArrayList<ParseObject>();
                            metaToSave = new ArrayList<ParseObject>();
                            //Buscar por producto y asesor
                            ParseObject metaParse = findMeta(ParseUser.getCurrentUser(), prod.getCodigo());
                            int dispMeta = metaParse.getInt("meta") - (metaParse.getInt("pedido") + metaParse.getInt("facturado"));
                            int cant = prod.getCantidad();
                            int cantExced = cant-dispMeta;

                            //Guardar cantidades
                            if(cant>dispMeta){
                                //Si la cantidad es mayor a lo disponible en Meta se toma lo faltante del excedente del producto
                                pedidoHasProductos.put("cantidad", dispMeta);
                                pedidoHasProductos.put("excedente", cantExced);
                                metaParse.put("pedido", metaParse.getInt("pedido")+dispMeta);
                                prod.getProductoParse().put("excedente", prod.getProductoParse().getInt("excedente") - cantExced);
                            }else{
                                //Si no, no se coloca nada en exedente
                                pedidoHasProductos.put("cantidad", cant);
                                pedidoHasProductos.put("excedente", 0);
                                metaParse.put("pedido", metaParse.getInt("pedido")+cant);
                            }

                            //Guardar descuentos y montos
                            pedidoHasProductos.put("descuento", prod.getDescuentoAplicado());
                            pedidoHasProductos.put("precio_unitario", round(prod.getPrecioComercial()));
                            pedidoHasProductos.put("monto", round(prod.getPrecioComercialTotal()));

                            //Agregar ParseObjects de Pedido y Producto a pedidohasProductos
                            pedidoHasProductos.put("pedido", pedidoParse);
                            pedidoHasProductos.put("producto", prod.getProductoParse());

                            //Indicar Descuento Manual
                            if (prod.getDescuentoManual() != 0.0) {
                                pedidoHasProductos.put("manual", true);
                            } else {
                                pedidoHasProductos.put("manual", false);
                            }

                            //Agregar pedidoHasProducto y Meta a sus ArrayList
                            pedidoHasProductoToSave.add(pedidoHasProductos);
                            metaToSave.add(metaParse);

                            //Guardar pedidoHasProductos
                            pedidoHasProductos.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        Log.d(TAG, "Error guardando Pedido: " + pedidoParse.getObjectId() + " y Producto: " + prod.getProductoParse().getObjectId() + " agregados a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                    }else{
                                        Log.d(TAG, "Pedido: " + pedidoParse.getObjectId() + " y Producto: " + prod.getProductoParse().getObjectId() + " agregados a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                    }
                                }
                            });

                            //Guardar Meta
                            metaParse.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e!=null){
                                        e.printStackTrace();
                                        Log.d(TAG, "Error Guardando meta");
                                    }else{
                                        Toast.makeText(mContext, R.string.pedidoCompletado, Toast.LENGTH_LONG).show();
                                        //Ubicacion Original
//                                        dispatchActivity(DashboardActivity.class, null, true);
                                    }
                                }
                            });
                        }
                    }else{
                        e.printStackTrace();
                        Log.d(TAG, "Error guardando Pedido");
                    }
                }
            });

        /*
        Si es un pedido que se esta editando
         */
        }else{
            //Si es un pedido rechazado que se está modificando
            Log.d(TAG, "Editando pedido "+idPedido);
            pedidoParse = app.pedido.getParseObject();

            /*
            Obtener productos asociados con el pedido para eliminarlos y colocar los productos nuevos
            */
            //Query para obtener Productos relacionados al Pedido
            Log.d(TAG, "Recuperando productos previamente existentes en pedido");
            ParseQuery query = new ParseQuery("PedidoHasProductos");
            query.include("producto");
            query.include("pedido");
            query.whereEqualTo("pedido", pedidoParse);
            query.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> productosPedidoHasProductos, ParseException e) {
                    if (e == null) {
                        //obtener productos del pedido
                        for (final ParseObject productoPedidoHasProductos : productosPedidoHasProductos) {
                            //buscar producto en metas
                            final ParseObject meta = findMeta(ParseUser.getCurrentUser(), productoPedidoHasProductos.getParseObject("producto").getString("codigo"));

                            //restaurarle la cantidad a campo pedido en metas
                            Log.d(TAG, "Restaurando metas para producto " + meta.getParseObject("producto").getObjectId());
                            int pedido = meta.getInt("pedido") - productoPedidoHasProductos.getInt("cantidad");
                            Log.d(TAG, "pedido meta: " + meta.get("pedido").toString() + " - producto cantidad: " + productoPedidoHasProductos.get("cantidad").toString() + " = " + pedido);
                            meta.put("pedido", pedido);
                            meta.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null)
                                        Log.d(TAG, "Meta Actualizada");
                                    else
                                        Log.d(TAG, "No se pudo actualizar meta " + e.getCause() + " " + e.getMessage());
                                }
                            });

                            //restaurar excedentes de existir
                            if (productoPedidoHasProductos.getInt("excedente") > 0) {
                                Log.d(TAG, "Restaurando excedentes");
                                ParseObject productoEx = productoPedidoHasProductos.getParseObject("producto");
                                int excedente = productoEx.getInt("excedente") + productoPedidoHasProductos.getInt("excedente");
                                productoEx.put("excedente", excedente);
                                productoEx.saveInBackground();
                            }

                            //borrar el producto del pedido
                            Log.d(TAG, "Solicitando borrar producto");
                            productoPedidoHasProductos.deleteEventually(new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) Log.d(TAG, "No se pudo borrar producto " + e.getCause() + " " + e.getMessage());
                                }
                            });
                        }
                    }else{
                        Log.d(TAG, "No se encontraron productos relacionados al pedido. Cause: " + e.getCause() + " Msg: " + e.getMessage());
                    }
                }
            });
            //Fin de Restaurar metas

            //Setear datos de pedido
            pedidoParse.put("asesor", ParseUser.getCurrentUser());
            pedidoParse.put("cliente", clienteParse);
            pedidoParse.put("direccion", direccion);
            pedidoParse.put("estado", 0);
            pedidoParse.put("num_pedido", numPedido);
            pedidoParse.put("comentario", observaciones);

            //Obtener Productos
            for (final Producto prod : productos) {
               final ParseObject productoParse = prod.getProductoParse();
                Log.d(TAG, "Agregando a PedidoHasProducto");
                //Agregar a PedidoHasProducto
                final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                ParseQuery queryMetas = new ParseQuery("Metas");
                queryMetas.whereEqualTo("asesor", ParseUser.getCurrentUser());
                queryMetas.whereEqualTo("producto", productoParse);
                queryMetas.getFirstInBackground(new GetCallback() {
                    @Override
                    public void done(final ParseObject metaParse, ParseException e) {
                    if(e==null){
                        //Almacenar meta disponible para producto
                        int dispMeta=metaParse.getInt("meta") - (metaParse.getInt("pedido") + metaParse.getInt("facturado"));
                        int cant=prod.getCantidad();
                        int cantExced=cant-dispMeta;
                        if(cant>dispMeta){
                            pedidoHasProductos.put("cantidad", dispMeta);
                            pedidoHasProductos.put("excedente", cantExced);
                            metaParse.put("pedido", metaParse.getInt("pedido")+dispMeta);
                            productoParse.put("excedente", productoParse.getInt("excedente")-cantExced);
                        }else{
                            pedidoHasProductos.put("cantidad", cant);
                            pedidoHasProductos.put("excedente", 0);
                            metaParse.put("pedido", metaParse.getInt("pedido")+cant);
                        }
                        pedidoHasProductos.put("descuento", round(prod.getDescuentoAplicado() * 100.0));
                        pedidoHasProductos.put("precio_unitario", round(prod.getPrecioComercial()));
                        pedidoHasProductos.put("monto", round(prod.getPrecioComercialTotal()));
                        pedidoHasProductos.put("pedido", pedidoParse);
                        pedidoHasProductos.put("producto", productoParse);
                        if (prod.getDescuentoManual() != 0.0) {
                            pedidoHasProductos.put("manual", true);
                        } else {
                            pedidoHasProductos.put("manual", false);
                        }
                        pedidoHasProductos.saveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                            if (e != null) e.printStackTrace();
                            Log.d(TAG, "Producto: " + productoParse.getObjectId() + " agregado a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                            metaParse.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e == null){
                                        Log.d(TAG, "Meta actualizada: "+metaParse.get("pedido"));
                                        Toast.makeText(mContext, R.string.pedidoCompletado, Toast.LENGTH_LONG).show();
                                        //Ubicacion Original
//                                        dispatchActivity(DashboardActivity.class, null, true);
                                    } else {
                                        Log.d(TAG, e.getCause() + e.getMessage());
                                    }
                                }
                            });
                            }
                        });
                    }
                    }
                });
            }
        }

        dispatchActivity(DashboardActivity.class, null, false);
    }

    /**
     * Funcion que devuelve el Subtotal del pedido sin impuestos
     * @return Subtotal del pedido
     */
    public Double getSubtotal(){
        Double sub=0.0;
        for(Producto prod:productos){
            sub += prod.getPrecioComercialTotal();
        }
        return sub;
    }


    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }

    /**
     * Funcion que busca la meta que coincida con el producto y usuario indicado en las metas almacenadas en <code>Application</code>
     * @param usuario Usuario al que pertenece la Meta
     * @param codigoProducto codigo del producto de la meta
     * @return el <code>ParseObject</code> de la Meta, <code>null</code> otherwise
     */
    public ParseObject findMeta(ParseUser usuario, String codigoProducto){
        Log.d(TAG, "Buscando: "+codigoProducto+" asesor:"+usuario.getUsername());
        for (ParseObject meta:app.metasParse){
            if(meta.getParseUser("asesor").getUsername().equals(usuario.getUsername()) && meta.getParseObject("producto").getString("codigo").equals(codigoProducto)){
                Log.d(TAG, "Buscando: Coincidencia ncontrada para "+codigoProducto);
                return meta;
            }
        }
        Log.d(TAG, "Buscando: No se pudo encontrar una meta para el producto "+codigoProducto);
        return null;
    }
}
