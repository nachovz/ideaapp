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
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");
    protected String denom, direccion, observaciones;
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
                text.setText(df.format(subtotal)+" "+denom);

            //Impuesto
                //TODO convertir a funcion
                imp=subtotal*0.14;
                text = (TextView) findViewById(R.id.impuesto_edit);
                text.setText(df.format(imp)+" "+denom);

            //Total
                total=subtotal+imp;
                text = (TextView) findViewById(R.id.total_edit);
                text.setText(df.format(total)+" "+denom);

            //llenar tablerow con productos
            TableLayout tl = (TableLayout)findViewById(R.id.listado_productos_pedido_table);
            TableRow tr;
            TextView nombreTextView,cantidadTextView,precioTextView, precioComercialTextView,descuentoTextView, precioFinalTextView;
            LayoutInflater inflater = this.getLayoutInflater();
            Boolean darkBackground = true;
            TableRow.LayoutParams params;
            String descProd;

            for(int i=0, size=productos.size(); i<size; i++){
                Producto prod = productos.get(i);
                tr = new TableRow(this);
                tr.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                nombreTextView= new TextView(this);
                //Nombre
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.3"));
                nombreTextView.setLayoutParams(params);
                nombreTextView.setTextColor(Color.parseColor("#262626"));
                nombreTextView.setPadding(18, 18, 18, 18);
                nombreTextView.setText(prod.getNombre());
                tr.addView(nombreTextView);
                if(darkBackground) nombreTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else nombreTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Cantidad
                cantidadTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.1"));
                cantidadTextView.setLayoutParams(params);
                cantidadTextView.setTextColor(Color.parseColor("#262626"));
                cantidadTextView.setPadding(18, 18, 18, 18);
                cantidadTextView.setText(String.valueOf(prod.getCantidad()));
                tr.addView(cantidadTextView);
                if(darkBackground) cantidadTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else cantidadTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Precio Lista
                precioTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.15"));
                precioTextView.setLayoutParams(params);
                precioTextView.setTextColor(Color.parseColor("#262626"));
                precioTextView.setPadding(18, 18, 18, 18);
                precioTextView.setText(prod.getStringPrecio());
                tr.addView(precioTextView);
                if(darkBackground) precioTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else precioTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Precio con Desc Comercial
                precioComercialTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.15"));
                precioComercialTextView.setLayoutParams(params);
                precioComercialTextView.setTextColor(Color.parseColor("#262626"));
                precioComercialTextView.setPadding(18, 18, 18, 18);
                precioComercialTextView.setText(prod.getStringPrecioComercial());
                tr.addView(precioComercialTextView);
                if(darkBackground) precioComercialTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else precioComercialTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Descuento por Volumen
                descuentoTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.1"));
                descuentoTextView.setLayoutParams(params);
                descuentoTextView.setTextColor(Color.parseColor("#262626"));
                descuentoTextView.setPadding(18, 18, 18, 18);
                descProd= df.format(prod.getPrecioComercial() * prod.getDescuentoAplicado());
                descuentoTextView.setText(descProd+" ("+prod.getDescuentoAplicadoPorcString()+")");
                tr.addView(descuentoTextView);
                if(darkBackground) descuentoTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else descuentoTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Precio Final
                precioFinalTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.15"));
                precioFinalTextView.setLayoutParams(params);
                precioFinalTextView.setTextColor(Color.parseColor("#262626"));
                precioFinalTextView.setPadding(18, 18, 18, 18);
                precioFinalTextView.setText(prod.getStringPrecioComercialTotal());
                tr.addView(precioFinalTextView);
                if(darkBackground) precioFinalTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else precioFinalTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Añadir a TableLayout de productos
                tl.addView(tr, tl.getChildCount(), new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
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

        EditText obs = (EditText) findViewById(R.id.obs_editText);
        observaciones = ""+String.valueOf(obs.getText());

        //Obtener Cliente de Parse
        Log.d("DEBUG", "Pidiendo Cliente");
        final ParseQuery clienteParseQuery = new ParseQuery("Cliente");
        clienteParseQuery.whereEqualTo("nombre", cliente.getNombre());
        clienteParseQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject clienteParse, ParseException e) {
                Random rand = new Random();
                numPedido = String.valueOf(56000 + rand.nextInt(10000));
                Log.d("DEBUG", "numPedido: " + numPedido);

                //Crear Pedido
                Log.d("DEBUG", "Pidiendo Pedido");
                final ParseObject pedidoParse = new ParseObject("Pedido");
                pedidoParse.put("asesor", ParseUser.getCurrentUser());
                pedidoParse.put("cliente", clienteParse);
                pedidoParse.put("direccion", direccion);
                pedidoParse.put("estado", 0);
                pedidoParse.put("num_pedido", numPedido);
                pedidoParse.put("comentario", observaciones);
                pedidoParse.saveEventually(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        for (final Producto prod : productos) {
                            //Obtener Productos de Parse
                            Log.d("DEBUG", "Pidiendo Producto");
                            ParseQuery productoParseQuery = new ParseQuery("Producto");
                            productoParseQuery.whereEqualTo("codigo", prod.getNombre()); //TODO FIX Getters y Setters
                            productoParseQuery.getFirstInBackground(new GetCallback() {
                                @Override
                                public void done(final ParseObject productoParse, ParseException e) {
                                    if (e != null) Log.d("DEBUG", e.getMessage());
                                    Log.d("DEBUG", "Agregando a PedidoHasProducto");
                                    //Agregar a PedidoHasProducto
                                    final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                                    pedidoHasProductos.put("cantidad", prod.getCantidad());
                                    Log.d("DEBUG", "descuento aplicado: " + prod.getDescuentoAplicadoString());
                                    pedidoHasProductos.put("descuento", prod.getDescuentoAplicado() * 100.0);
                                    pedidoHasProductos.put("pedido", pedidoParse);
                                    pedidoHasProductos.put("producto", productoParse);
                                    pedidoHasProductos.saveEventually(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) e.printStackTrace();
                                            Log.d("DEBUG", "Pedido: " + pedidoParse.getObjectId() + " y Producto: " + productoParse.getObjectId() + " agregados a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                            Toast.makeText(mContext, R.string.pedidoCompletado, 3000).show();
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

        //Actualizar existencia en UserHasProducto
        ParseQuery queryExistencia = new ParseQuery("UserHasProducto");
        queryExistencia.include("producto");
        queryExistencia.whereEqualTo("usuario", ParseUser.getCurrentUser());
        queryExistencia.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e==null){
                    int existenciaParse;
                    Producto producto;
                    Log.d("DEBUG", "objetos recuperados UserHasProducto: "+parseObjects.size());
                    for(ParseObject parseObject:parseObjects){
                        existenciaParse = parseObject.getInt("cantidad");
                        Log.d("DEBUG", "existenciaparse: "+existenciaParse);
                        producto=getProductoByObjectId(parseObject.getParseObject("producto").getObjectId());
                        if(producto != null){
                            parseObject.put("cantidad", existenciaParse-producto.getCantidad());
                            Log.d("DEBUG", "cantidad actualizada: "+(existenciaParse-producto.getCantidad()));
                            parseObject.saveEventually(new SaveCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if(e!=null)
                                        Log.d("DEBUG", e.getMessage());
                                }
                            });
                        }
                    }
                }else{
                    Log.d("DEBUG", e.getCause()+e.getMessage());
                }
            }
        });

        //Actualizar campo pedido en Metas
    }

    public Producto getProductoByObjectId(String id){
        for(Producto producto:productos){
            Log.d("DEBUG", "id parse: "+id+" ipProd: "+producto.getId());
            if(producto.getId().equals(id))
                Log.d("DEBUG", "Found "+producto.getId());
                return producto;
        }
        return null;
    }

    public Double getSubtotal(){
        Double sub=0.0;
        for(Producto prod:productos){
            sub += prod.getPrecioComercialTotal();
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
