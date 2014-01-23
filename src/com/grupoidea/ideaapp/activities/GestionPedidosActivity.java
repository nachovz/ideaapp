package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.ProductosPedidoAdapter;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestionPedidosActivity extends ParentMenuActivity {
    protected String TAG = this.getClass().getSimpleName();
    protected Context mContext;
    protected double subtotal, desc, flete, misc, imp, total;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");
    protected String denom, direccion, observaciones;
    protected ParseUser vendedor;
    protected Cliente cliente;
    /** ArrayList que contiene los productos que se mostraran en el gridCatalogo del catalogo*/
    protected ArrayList<Producto> productos;
    protected String numPedido, idPedido;
    protected boolean isNuevoPedido = true;
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
        clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        app = (GrupoIdea) getApplication();
        productos = app.pedido.getProductos();
        idPedido = app.pedido.getObjectId();
        numPedido = app.pedido.getNumPedido();
        vendedor = ParseUser.getCurrentUser();
        cliente = app.clienteActual;
        denom = productos.get(0).getDenominacion();

        if(numPedido == null){
            //@TODO Reemplazar por codigo generador de numeros de pedido
            Random rand = new Random();
            numPedido = String.valueOf(56000 + rand.nextInt(10000));
        }else{
            isNuevoPedido = false;
        }

        Log.d(TAG, "isNuevoPedido: "+String.valueOf(isNuevoPedido)+" numPedido: " + numPedido + " productos: "+productos.size());

        TextView text;

        //-------------- Setear Header --------------
        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

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

        //-------------- Fin Setear Header --------------

        ProductosPedidoAdapter adapter = new ProductosPedidoAdapter(mContext, productos);
        ListView productosListView = (ListView) findViewById(R.id.productos_pedido_listView);
        productosListView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
		return null;
	}

    public void commitPedido(View view) {
        Button finalizarPedidoButton = (Button) findViewById(R.id.finalizarPedidoButton);
        finalizarPedidoButton.setEnabled(false);
        final ParseObject pedidoParse;

        //Toast de mensaje de upload
        int idToastMessage;
        if(GrupoIdea.hasInternet) idToastMessage = R.string.subiendoPedido;
        else idToastMessage = R.string.no_internet_subiendoPedido;
        Toast.makeText(mContext, idToastMessage, Toast.LENGTH_SHORT).show();

        EditText direccionET = (EditText) findViewById(R.id.direccion_envio_edit);
        direccion = ""+String.valueOf(direccionET.getText());

        EditText obs = (EditText) findViewById(R.id.obs_editText);
        observaciones = ""+String.valueOf(obs.getText());

        ParseObject clienteParse = app.clienteActual.getClienteParse();

        if(isNuevoPedido){
            pedidoParse = new ParseObject("Pedido");
        }else{
            pedidoParse = app.pedido.getParseObject();
            restaurarProductosPedido(pedidoParse);
        }

        //Setear datos de pedido
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
                    ParseObject pedidoHasProductos;
                    ParseObject metaParse;

                    for (Producto prod : productos) {
                        pedidoHasProductoToSave = new ArrayList<ParseObject>();
                        metaToSave = new ArrayList<ParseObject>();
                        pedidoHasProductos = new ParseObject("PedidoHasProductos");
                        //Buscar Meta por producto y asesor
                        Meta meta = app.findMetaByProductCode(prod.getCodigo());
                        metaParse = meta != null? meta.getParseObject() : null;
                        if(metaParse != null){
                            int dispMeta = metaParse.getInt("meta") - (metaParse.getInt("pedido") + metaParse.getInt("facturado"));
                            int cant = prod.getCantidad();
                            int cantExced = cant-dispMeta;

                            //Guardar cantidades
                            if(cant>dispMeta){
                                //Si la cantidad es mayor a lo disponible en Meta se toma lo faltante del excedente del producto
                                pedidoHasProductos.put("cantidad", dispMeta);
                                pedidoHasProductos.put("excedente", cantExced);
                                metaParse.put("pedido", metaParse.getInt("pedido") + dispMeta);
                                prod.getProductoParse().put("excedente", prod.getProductoParse().getInt("excedente") - cantExced);
                            }else{
                                //Si no exedente 0
                                pedidoHasProductos.put("cantidad", cant);
                                pedidoHasProductos.put("excedente", 0);
                                metaParse.put("pedido", metaParse.getInt("pedido") + cant);
                            }
                        }else{
                            Log.d(TAG, "Meta para producto "+prod.getCodigo()+" null, sete");
                            pedidoHasProductos.put("excedente", prod.getCantidad());
                            prod.getProductoParse().put("excedente", prod.getProductoParse().getInt("excedente") - prod.getCantidad());
                        }

                        //Guardar descuentos y montos
                        pedidoHasProductos.put("descuento", prod.getDescuentoAplicado());
                        pedidoHasProductos.put("precio_unitario", round(prod.getPrecioComercial()));
                        pedidoHasProductos.put("monto", round(prod.getPrecioComercialTotal()));

                        //Agregar ParseObjects de Pedido y Producto a pedidohasProductos
                        pedidoHasProductos.put("pedido", pedidoParse);
                        pedidoHasProductos.put("producto", prod.getProductoParse());

                        //Indicar Descuento Manual
                        if (prod.getDescuentoManual() != 0.0) pedidoHasProductos.put("manual", true);
                        else pedidoHasProductos.put("manual", false);

                        //Agregar pedidoHasProducto y Meta a sus ArrayList
                        pedidoHasProductoToSave.add(pedidoHasProductos);
                        metaToSave.add(metaParse);

                        //Guardar pedidoHasProductos
                        pedidoHasProductos.saveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                if (e != null) e.printStackTrace();
                            }
                        });

                        //Guardar Meta
                        if(metaParse != null){
                            metaParse.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e != null) {
                                        e.printStackTrace();
                                        Log.d(TAG, "Error Guardando meta");
                                    } else Toast.makeText(mContext, R.string.pedidoCompletado, Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }else{
                    e.printStackTrace();
                    Log.d(TAG, "Error guardando Pedido");
                }
            }
        });

        dispatchActivity(DashboardActivity.class, null, false);
    }

    protected void restaurarProductosPedido(ParseObject pedidoParse) {
        ParseQuery query = new ParseQuery("PedidoHasProductos");
        query.include("producto");
        query.include("pedido");
        query.whereEqualTo("pedido", pedidoParse);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> productosPedidoHasProductos, ParseException e) {
                if (e == null) {
                    //obtener productos del pedido
                    for (ParseObject productoPedidoHasProductos : productosPedidoHasProductos) {
                        //buscar producto en metas
                        Meta meta = app.findMetaByProductCode(productoPedidoHasProductos.getParseObject("producto").getString("codigo"));
                        final ParseObject metaParse = meta != null? meta.getParseObject() : null;
                        if(metaParse != null){
                            //restaurarle la cantidad a campo pedido en metas
                            Log.d(TAG, "Restaurando metas para producto " + metaParse.getParseObject("producto").getObjectId());
                            int pedido = metaParse.getInt("pedido") - productoPedidoHasProductos.getInt("cantidad");
                            Log.d(TAG, "pedido meta: " + metaParse.get("pedido").toString() + " - producto cantidad: " + productoPedidoHasProductos.get("cantidad").toString() + " = " + pedido);
                            metaParse.put("pedido", pedido);
                            metaParse.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null)
                                        Log.d(TAG, "Meta Actualizada");
                                    else
                                        Log.d(TAG, "No se pudo actualizar meta " + e.getCause() + " " + e.getMessage());
                                }
                            });
                        }

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
    }

    /**
     * Funcion que devuelve el Subtotal del pedido sin impuestos
     * @return Subtotal del pedido
     */
    public Double getSubtotal(){
        Double sub=0.0;
        for(Producto prod:productos) sub += prod.getPrecioComercialTotal();
        return sub;
    }


    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();
        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
