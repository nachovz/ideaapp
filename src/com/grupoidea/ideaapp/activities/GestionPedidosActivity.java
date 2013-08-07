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

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GestionPedidosActivity extends ParentMenuActivity {
    protected Context mContext;
    protected JSONArray productosJSON;
    protected double subtotal, desc, flete, misc, imp, total;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");
    protected DecimalFormat dfParse = new DecimalFormat("#.##");
    protected String denom, direccion, observaciones;
    protected ParseUser vendedor;
    protected Cliente cliente;
    /** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
    protected ArrayList<Producto> productos;
    protected String numPedido, idPedido;
    protected boolean editar = false;

    public GestionPedidosActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestion_pedidos_layout);
        mContext=this;

        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

        clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        denom = new String();
        productos = new ArrayList<Producto>();
        vendedor = ParseUser.getCurrentUser();
        try {
            Intent intent = getIntent();
            idPedido = intent.getStringExtra("idPedido");
            numPedido = intent.getStringExtra("numPedido");
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
                if(numPedido==null){
                    editar = false;
                    Random rand = new Random();
                    numPedido = String.valueOf(56000 + rand.nextInt(10000));
                    Log.d("DEBUG", "numPedido: " + numPedido);
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
                imp=subtotal*productos.get(0).getIva();
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
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.25"));
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
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
                precioTextView.setLayoutParams(params);
                precioTextView.setTextColor(Color.parseColor("#262626"));
                precioTextView.setPadding(18, 18, 18, 18);
                precioTextView.setText(prod.getStringPrecio());
                tr.addView(precioTextView);
                if(darkBackground) precioTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else precioTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Precio con Desc Comercial
                precioComercialTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
                precioComercialTextView.setLayoutParams(params);
                precioComercialTextView.setTextColor(Color.parseColor("#262626"));
                precioComercialTextView.setPadding(18, 18, 18, 18);
                precioComercialTextView.setText(prod.getStringPrecioComercial());
                tr.addView(precioComercialTextView);
                if(darkBackground) precioComercialTextView.setBackgroundColor(Color.parseColor("#FFFFFF"));
                else precioComercialTextView.setBackgroundColor(Color.parseColor("#E4E4E4"));

                //Descuento por Volumen
                descuentoTextView = new TextView(this);
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
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
                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
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
        Button finalizarPedidoButton = (Button) findViewById(R.id.finalizarPedidoButton);
        finalizarPedidoButton.setEnabled(false);
        Toast.makeText(mContext, R.string.subiendoPedido, 3000).show();
        EditText direccionET = (EditText) findViewById(R.id.direccion_envio_edit);
        direccion = ""+String.valueOf(direccionET.getText());

        EditText obs = (EditText) findViewById(R.id.obs_editText);
        observaciones = ""+String.valueOf(obs.getText());

        //Obtener Cliente de Parse
        Log.d("DEBUG", "Pidiendo Cliente");
        final ParseQuery clienteParseQuery = new ParseQuery("Cliente");
        Log.d("DEBUG", "Cliente: "+ cliente.getNombre());
        clienteParseQuery.whereEqualTo("nombre", cliente.getNombre());
        clienteParseQuery.getFirstInBackground(new GetCallback() {
            @Override
            public void done(final ParseObject clienteParse, ParseException e) {
                if(e==null){
                    Log.d("DEBUG", "entrando en Done de Cliente");
                    //Crear Pedido
                    final ParseObject[] pedidoParse = new ParseObject[1];

                    // Si es un pedido nuevo
                    if(!editar){
                        //Si es un pedido nuevo
                        pedidoParse[0] = new ParseObject("Pedido");
                        Log.d("DEBUG", "Pidiendo Pedido");

                        pedidoParse[0].put("asesor", ParseUser.getCurrentUser());
                        pedidoParse[0].put("cliente", clienteParse);
                        pedidoParse[0].put("direccion", direccion);
                        pedidoParse[0].put("estado", 0);
                        pedidoParse[0].put("num_pedido", numPedido);
                        pedidoParse[0].put("comentario", observaciones);
                        pedidoParse[0].saveEventually(new SaveCallback() {
                            @Override
                            public void done(ParseException e) {
                                for (final Producto prod : productos) {
                                    //Obtener Productos de Parse
                                    Log.d("DEBUG", "Obteniendo Producto de Parse");
                                    ParseQuery productoParseQuery = new ParseQuery("Producto");
                                    productoParseQuery.whereEqualTo("codigo", prod.getNombre());
                                    productoParseQuery.getFirstInBackground(new GetCallback() {
                                        @Override
                                        public void done(final ParseObject productoParse, ParseException e) {
                                            if (e != null) Log.d("DEBUG", e.getMessage());
                                            Log.d("DEBUG", "Agregando a PedidoHasProducto");
                                            //Agregar a PedidoHasProducto
                                            final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                                            ParseQuery queryMetas = new ParseQuery("Metas");
                                            queryMetas.whereEqualTo("asesor", ParseUser.getCurrentUser());
                                            queryMetas.whereEqualTo("producto", productoParse);
                                            queryMetas.getFirstInBackground(new GetCallback() {
                                                @Override
                                                public void done(final ParseObject metaParse, ParseException e) {
                                                    if(e==null){
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
                                                        pedidoHasProductos.put("pedido", pedidoParse[0]);
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
                                                                Log.d("DEBUG", "Pedido: " + pedidoParse[0].getObjectId() + " y Producto: " + productoParse.getObjectId() + " agregados a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                                                metaParse.saveEventually(new SaveCallback() {
                                                                    @Override
                                                                    public void done(ParseException e) {
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
                                }
                            }
                        });

                    //Si es un pedido que se esta editando
                    }else{
                        //Si es un pedido rechazado que se está modificando
                        Log.d("DEBUG", "Editando pedido "+idPedido);
                        //Obtener el ParseObject con el objectId
                        ParseQuery query = new ParseQuery("Pedido");
                        query.getInBackground(idPedido, new GetCallback() {
                            @Override
                            public void done(final ParseObject pedidoParseObject, ParseException e) {
                                Log.d("DEBUG", "Recuperando Pedido " + pedidoParseObject.getObjectId());
                                if (e == null) {
                                    pedidoParse[0] = pedidoParseObject;
                                    //Eliminar productos asociados con el pedido
                                    Log.d("DEBUG", "Recuperando productos antiguos de pedido");
                                    ParseQuery query = new ParseQuery("PedidoHasProductos");
                                    query.include("producto");
                                    query.include("pedido");
                                    query.whereEqualTo("pedido", pedidoParseObject);
                                    query.findInBackground(new FindCallback() {
                                        @Override
                                        public void done(List<ParseObject> productosPedido, ParseException e) {
                                            if (e == null) {
                                                ParseQuery query;
                                                //obtener productos del pedido
                                                for (int i = 0, size = productosPedido.size(); i < size; i++) {
                                                     final ParseObject productoPedido = productosPedido.get(i);
                                                    //buscar producto en metas
                                                    query = new ParseQuery("Metas");
                                                    query.include("producto");
                                                    query.include("asesor");
                                                    query.whereEqualTo("asesor", ParseUser.getCurrentUser());
                                                    query.whereEqualTo("producto", productoPedido.getParseObject("producto"));
                                                    query.getFirstInBackground(new GetCallback() {
                                                        @Override
                                                        public void done(final ParseObject meta, ParseException e) {
                                                            if (e == null) {
                                                                //restaurarle la cantidad a campo pedido en metas
                                                                Log.d("DEBUG", "Restaurando metas para producto " + meta.getParseObject("producto").getObjectId());
                                                                //Obtener el parseObject de PedidoHasProductos ya que se perdio en el loop por la asincronia
                                                                ParseQuery query = new ParseQuery("PedidoHasProductos");
                                                                query.include("producto");
                                                                query.include("pedido");
                                                                query.whereEqualTo("producto", meta.getParseObject("producto"));
                                                                query.whereEqualTo("pedido", pedidoParseObject);
                                                                query.getFirstInBackground(new GetCallback() {
                                                                    @Override
                                                                    public void done(ParseObject productoPedido, ParseException e) {
                                                                        int pedido = meta.getInt("pedido") - productoPedido.getInt("cantidad");
                                                                        Log.d("DEBUG", "pedido meta: "+meta.get("pedido").toString()+" - producto cantidad: "+productoPedido.get("cantidad").toString()+" = "+pedido);
                                                                        meta.put("pedido", pedido);
                                                                        meta.saveEventually(new SaveCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if(e==null)
                                                                                    Log.d("DEBUG", "Meta Actualizada");
                                                                                else
                                                                                    Log.d("DEBUG", "No se pudo actualizar meta " + e.getCause() + " " + e.getMessage());
                                                                            }
                                                                        });

                                                                        //restaurar excedentes de existir
                                                                        if (productoPedido.getInt("excedente") > 0) {
                                                                            Log.d("DEBUG", "Restaurando excedentes");
                                                                            ParseObject productoEx = productoPedido.getParseObject("producto");
                                                                            int excedente = productoEx.getInt("excedente") + productoPedido.getInt("excedente");
                                                                            productoEx.put("excedente", excedente);
                                                                            productoEx.saveInBackground();
                                                                        }

                                                                        //borrar el producto del pedido
                                                                        Log.d("DEBUG", "Solicitando borrar producto");
                                                                        productoPedido.deleteEventually(new DeleteCallback() {
                                                                            @Override
                                                                            public void done(ParseException e) {
                                                                                if (e != null)
                                                                                    Log.d("DEBUG", "No se pudo borrar producto " + e.getCause() + " " + e.getMessage());
                                                                            }
                                                                        });
                                                                    }
                                                                });

                                                            } else {
                                                                Log.d("DEBUG", "No se encontraron metas para el producto. Cause: " + e.getCause() + " Msg: " + e.getMessage());
                                                            }
                                                        }
                                                    });

                                                }
                                            } else {
                                                Log.d("DEBUG", e.getCause() + e.getMessage());
                                            }
                                        }
                                    });

                                    pedidoParse[0].put("asesor", ParseUser.getCurrentUser());
                                    pedidoParse[0].put("cliente", clienteParse);
                                    pedidoParse[0].put("direccion", direccion);
                                    pedidoParse[0].put("estado", 0);
                                    pedidoParse[0].put("num_pedido", numPedido);
                                    pedidoParse[0].put("comentario", observaciones);
                                    pedidoParse[0].saveEventually(new SaveCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            for (final Producto prod : productos) {
                                                //Obtener Productos de Parse
                                                Log.d("DEBUG", "Obteniendo Producto de Parse");
                                                ParseQuery productoParseQuery = new ParseQuery("Producto");
                                                productoParseQuery.whereEqualTo("codigo", prod.getNombre());
                                                productoParseQuery.getFirstInBackground(new GetCallback() {
                                                    @Override
                                                    public void done(final ParseObject productoParse, ParseException e) {
                                                        if (e != null) Log.d("DEBUG", e.getMessage());
                                                        Log.d("DEBUG", "Agregando a PedidoHasProducto");
                                                        //Agregar a PedidoHasProducto
                                                        final ParseObject pedidoHasProductos = new ParseObject("PedidoHasProductos");
                                                        ParseQuery queryMetas = new ParseQuery("Metas");
                                                        queryMetas.whereEqualTo("asesor", ParseUser.getCurrentUser());
                                                        queryMetas.whereEqualTo("producto", productoParse);
                                                        queryMetas.getFirstInBackground(new GetCallback() {
                                                            @Override
                                                            public void done(final ParseObject metaParse, ParseException e) {
                                                                if(e==null){
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
                                                                    pedidoHasProductos.put("pedido", pedidoParse[0]);
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
                                                                            Log.d("DEBUG", "Producto: " + productoParse.getObjectId() + " agregado a PedidoHasProductos:" + pedidoHasProductos.getObjectId());
                                                                            metaParse.saveEventually(new SaveCallback() {
                                                                                @Override
                                                                                public void done(ParseException e) {
                                                                                    if(e == null){
                                                                                        Log.d("DEBUG", "Meta actualizada: "+metaParse.get("pedido"));
                                                                                        Toast.makeText(mContext, R.string.pedidoCompletado, 3000).show();
                                                                                        dispatchActivity(DashboardActivity.class, null, true);
                                                                                    } else {
                                                                                        Log.d("DEBUG", e.getCause() + e.getMessage());
                                                                                    }
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
                                        }
                                    });
                                } else {
                                    Log.d("DEBUG", e.getCause() + e.getMessage());
                                }
                            }
                        });
                    }
                }else{
                    Log.d("DEBUG", e.getCause() + e.getMessage());
                }
            }
        });

        //Actualizar existencia en Metas
//        ParseQuery queryMetas = new ParseQuery("Metas");
//        queryMetas.include("producto");
//        queryMetas.whereEqualTo("asesor", ParseUser.getCurrentUser());
//        queryMetas.findInBackground(new FindCallback() {
//            @Override
//            public void done(List<ParseObject> parseObjects, ParseException e) {
//                if (e == null) {
//                    int existenciaParse;
//                    Producto producto;
//                    Log.d("DEBUG", "objetos recuperados Metas: " + parseObjects.size());
//                    for (ParseObject parseObject : parseObjects) {
//                        existenciaParse = parseObject.getInt("pedido");
//                        Log.d("DEBUG", "existenciaParse: " + existenciaParse);
//                        producto = getProductoByObjectId(parseObject.getParseObject("producto").getObjectId());
//                        if (producto != null) {
//                            parseObject.put("pedido", existenciaParse + producto.getCantidad());
//                            Log.d("DEBUG", "cantidad actualizada: " + (existenciaParse + producto.getCantidad()));
//                            parseObject.saveEventually(new SaveCallback() {
//                                @Override
//                                public void done(ParseException e) {
//                                    if (e != null)
//                                        Log.d("DEBUG", e.getMessage());
//                                }
//                            });
//                        }
//                    }
//                } else {
//                    Log.d("DEBUG", e.getCause() + e.getMessage());
//                }
//            }
//        });
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

    public static double round(double value) {
        int places = 2;
        if (places < 0) throw new IllegalArgumentException();

        BigDecimal bd = new BigDecimal(value);
        bd = bd.setScale(places, BigDecimal.ROUND_HALF_UP);
        return bd.doubleValue();
    }
}
