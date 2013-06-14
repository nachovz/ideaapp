package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.*;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Producto;

import com.parse.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestionPedidosActivity extends ParentMenuActivity {
    protected Context mContext;
    protected JSONArray productosJSON;
    protected double subtotal, desc, flete, misc, imp, total;
    protected DecimalFormat df = new DecimalFormat("#.##");
    protected String denom, direccion;
    protected ParseUser vendedor;
    protected Cliente cliente;
    /** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
    protected ArrayList<Producto> productos;
    protected String numPedido;

    public GestionPedidosActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestion_pedidos_layout);
        mContext=this;
        clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        denom = new String();
        productos = new ArrayList<Producto>();
        vendedor = ParseUser.getCurrentUser();
        try {
            Intent intent = getIntent();
            String jsonStr = intent.getStringExtra("Productos");
            cliente = new Cliente(intent.getStringExtra("Cliente"));
            cliente.setId(intent.getStringExtra("ClienteId"));
            cliente.setDescuento(intent.getDoubleExtra("Descuento", 0.0));
            cliente.setParseId(intent.getStringExtra("parseId"));
            productosJSON = new JSONArray(jsonStr);
            llenarProductosfromJSON(productosJSON);
            denom = productos.get(0).getDenominacion();
            TextView text;
            //Fecha
                text = (TextView) findViewById(R.id.fecha_edit);
                Time now = new Time(); now.setToNow();
                text.setText(now.monthDay + "/" + now.month + "/" + now.year);

            //ID Cliente
                text = (TextView) findViewById(R.id.id_cliente_edit);
                text.setText(cliente.getId());
            //Nombre Cliente
                text = (TextView) findViewById(R.id.nombre_cliente_edit);
                text.setText(cliente.getNombre());
            //# Orden Compra
                //TODO convertir a funcion
                text = (TextView) findViewById(R.id.numero_orden_compra_edit);
                text.setText("#0019587");

            //Subtotal
                subtotal = getSubtotal();
                text = (TextView) findViewById(R.id.subtotal_edit);
                text.setText(df.format(subtotal)+denom);

            //Descuento Comercial
                desc=cliente.getDescuento()/100.0;
                Log.d("DEBUG", desc+"");
                desc = subtotal *desc;
                text = (TextView) findViewById(R.id.desc_comercial_edit);
                text.setText(df.format(desc)+denom);
            //Flete
                //TODO convertir a funcion
                flete = 0.0;
                text = (TextView) findViewById(R.id.flete_edit);
                text.setText(df.format(flete)+denom);
            //Miscelaneos
                //TODO convertir a funcion
                misc = 0.0;
                text = (TextView) findViewById(R.id.misc_edit);
                text.setText(df.format(misc)+denom);
            //Impuesto
                //TODO convertir a funcion
                imp=0.0;
                text = (TextView) findViewById(R.id.impuesto_edit);
                text.setText(df.format(imp)+denom);

            //Total
                total=subtotal-desc; //TODO + impuestos flete y eso
                text = (TextView) findViewById(R.id.total_edit);
                text.setText(df.format(total)+denom);

            //llenar tablerow con productos
            TableLayout tl = (TableLayout)findViewById(R.id.listado_productos_pedido_table);
            TableRow tr;
            TextView t1,t2,t3,t4;
            LayoutInflater inflater = this.getLayoutInflater();
            Boolean darkBackground = true;
            TableRow.LayoutParams params;

            for(int i=0, size=productos.size(); i<size; i++){
                Producto prod = productos.get(i);
                tr = new TableRow(this);
                tr.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                t1= new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.35"));
                t1.setLayoutParams(params);
                t1.setTextColor(Color.parseColor("#262626"));
                t1.setPadding(18,18,18,18);
                t1.setText(prod.getNombre());
                tr.addView(t1);
                if(darkBackground) t1.setBackgroundColor(Color.parseColor("#FFFFFF"));

                t2 = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.15"));
                t2.setLayoutParams(params);
                t2.setTextColor(Color.parseColor("#262626"));
                t2.setPadding(18,18,18,18);
                t2.setText(String.valueOf(prod.getCantidad()));
                tr.addView(t2);
                if(darkBackground) t2.setBackgroundColor(Color.parseColor("#FFFFFF"));

                t3 = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.25"));
                t3.setLayoutParams(params);
                t3.setTextColor(Color.parseColor("#262626"));
                t3.setPadding(18, 18, 18, 18);
                t3.setText(prod.getStringPrecio());
                tr.addView(t3);
                if(darkBackground) t3.setBackgroundColor(Color.parseColor("#FFFFFF"));

                t4 = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.25"));
                t4.setLayoutParams(params);
                t4.setTextColor(Color.parseColor("#262626"));
                t4.setPadding(18, 18, 18, 18);
                t4.setText(prod.getStringPrecioTotal());
                tr.addView(t4);
                if(darkBackground) t4.setBackgroundColor(Color.parseColor("#FFFFFF"));

                //Añadir a TableLayout de productos
                tl.addView(tr, tl.getChildCount() - 1, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                darkBackground=!darkBackground;
            }

        } catch (JSONException e) {
            Log.d("DEBUG", "onCreate: "+e.getMessage());
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
        Log.d("DEBUG", "Button clicked");
        EditText direccionET = (EditText) findViewById(R.id.direccion_envio_edit);
        direccion = ""+String.valueOf(direccionET.getText());

        //Cliente
        Log.d("DEBUG", "Pidiendo Cliente");
        final ParseQuery clienteParseQuery = new ParseQuery("Cliente");
        clienteParseQuery.whereEqualTo("nombre", cliente.getNombre());
        clienteParseQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject clienteParse, ParseException e) {
                Random rand = new Random();
                numPedido = String.valueOf(56000 + rand.nextInt(10000));
                Log.d("DEBUG", "numPedido: " + numPedido);

                //Pedido
                Log.d("DEBUG", "Pidiendo Pedido");
                final ParseObject pedidoParse = new ParseObject("Pedido");
                pedidoParse.put("asesor", ParseUser.getCurrentUser());
                pedidoParse.put("cliente", clienteParse);
                pedidoParse.put("direccion", direccion);
                pedidoParse.put("estado", 0);
                pedidoParse.put("num_pedido", numPedido);
                pedidoParse.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        for (final Producto prod : productos) {
                            //Producto
                            Log.d("DEBUG", "Pidiendo Producto");
                            ParseQuery productoParseQuery = new ParseQuery("Producto");
                            productoParseQuery.whereEqualTo("codigo", prod.getNombre()); //TODO FIX Getters y Setters
                            productoParseQuery.getFirstInBackground(new GetCallback() {
                                @Override
                                public void done(final ParseObject productoParse, ParseException e) {
                                    if (e != null) Log.d("DEBUG", e.getMessage());
                                    Log.d("DEBUG", "Agregando a PedidoHasProducto");
                                    final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                                    pedidoHasProductos.put("cantidad", prod.getCantidad());
                                    pedidoHasProductos.put("descuento", prod.getDescuento());
                                    pedidoHasProductos.put("pedido", pedidoParse);
                                    pedidoHasProductos.put("producto", productoParse);
                                    pedidoHasProductos.saveInBackground(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) e.printStackTrace();
                                            Log.d("DEBUG", "Pedido: " + pedidoParse.getObjectId() + " y Producto: " + productoParse.getObjectId() + " agregados a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                            Toast.makeText(mContext,R.string.pedidoCompletado,3000).show();
                                            dispatchActivity(DashboardActivity.class, null, true);
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    public Double getSubtotal(){
        Double sub=0.0;
        for(Producto prod:productos){
            sub += prod.getPrecioTotal();
        }
        return sub;
    }

    public void llenarProductosfromJSON(JSONArray json){
        try {
            Producto prod;
            JSONObject jsonO;
            for(int i=0, size=json.length();i<size;i++){
                jsonO = json.getJSONObject(i);
                prod = Producto.setFromJSON(jsonO);
                productos.add(prod);
            }
        } catch (JSONException e) {
            Log.d("DEBUG", "llenarProductosfromJSON: "+e.getMessage());
        }
    }
}
