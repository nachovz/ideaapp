package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowPedido;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DashboardActivity extends ParentMenuActivity {
	/** Elemento que contiene la sesion del usuario actual*/ 
	private ParseUser user;
	/** ViewGroup que contiene las filas con informacion de los clientes*/
	//private LinearLayout clienteList;
	/** ViewGroup que contiene las filas con informacion de los pedidos */
	private LinearLayout pedidosList;
    private List<ParseObject> pedidos;
    private Spinner pedidosSpinner;
    private ArrayAdapter<String> pedidosSpinnerAdapter;
    private double iva;

    /** Lista de metas del usuario logueado */
    private ArrayList<Meta> metas;

    private TableLayout tl;
    private TableRow tr;

    /** Lista de Marcas disponibles para el Spinner */
    private HashSet<String> marcas;

    /** Adapter para Spinner de marcas */
    ArrayAdapter<String> marcasAdapter;
    /** Spinner de marcas*/
    Spinner marcasSpinner;

    /** Context */
    Context mContext;
	
	public DashboardActivity() {
		super(true, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        metas = new ArrayList<Meta>();
        marcas = new HashSet<String>();
		mContext = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);

        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

        //Contador de pedidos
        final TextView pedidosCounter;
        pedidosCounter = (TextView) findViewById(R.id.counterPedidos);

        //Llenar Spinner
        final ArrayList<String> estados = new ArrayList<String>();
        estados.add("VERIFICANDO");
        estados.add("APROBADO");
        estados.add("RECHAZADO");
        estados.add("ANULADO");
        estados.add("TODOS");

        pedidosSpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, estados);
        pedidosSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pedidosSpinner = (Spinner)findViewById(R.id.pedidos_spinner);
        pedidosSpinner.setAdapter(pedidosSpinnerAdapter);

        //Obtener IVA para los RowPedido
        getIVAFromParse();

        //Carga de Pedidos
		pedidosList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
        ParseQuery queryPedidos = new ParseQuery("Pedido");
        queryPedidos.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        queryPedidos.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryPedidos.include("cliente");
        queryPedidos.orderByAscending("estado");
        queryPedidos.addDescendingOrder("createdAt");
        queryPedidos.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    pedidos = parseObjects;
                    //setear contador de pedidos
                    pedidosCounter.setText(String.valueOf(pedidos.size()));
                    pedidosCounter.setVisibility(View.VISIBLE);

                    pedidosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            Log.d("DEBUG","Estado seleccionado en Spinner "+estados.get(position));
                            RowPedido row;
//                            pedidosList.removeAllViews();
                            pedidosList.removeViews(1, pedidosList.getChildCount()-1);
                            for (ParseObject parseObject : pedidos) {
                                if(parseObject.getInt("estado") == position || position == 4){
                                    ParseObject cliente = parseObject.getParseObject("cliente");
                                    row = new RowPedido(mContext, cliente.getString("nombre"), parseObject.getInt("estado"), parseObject.getString("num_pedido"), parseObject.getCreatedAt(), parseObject.getUpdatedAt());
                                    row.idPedido = parseObject.getObjectId();
                                    row.numPedido = parseObject.getString("num_pedido");
                                    if(position == Pedido.ESTADO_RECHAZADO || parseObject.getInt("estado") == Pedido.ESTADO_RECHAZADO){
                                        row.observacionesRechazoPedido = parseObject.getString("comentario_cambio_status");
                                    }
                                    pedidosList.addView(row);
                                }
                            }
                            ProgressBar loading = (ProgressBar) pedidosList.findViewById(R.id.pedidos_progressBar);
                            loading.setVisibility(View.GONE);
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });

                    //poner Rechazado como seleccionado
                    pedidosSpinner.setSelection(2);

                    Log.d("DEBUG", "Carga de pedidos completa");
                } else {
                    Log.d("DEBUG", e.toString());
                }
            }
        });

        //Carga de metas
        ParseQuery query = new ParseQuery("Metas");
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.whereEqualTo("asesor", ParseUser.getCurrentUser());
        query.include("producto");
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    Meta meta;
                    Producto producto;
                    int facturadoMetas = 0, totalMetas = 0, pedidoMetas = 0;

                    for (ParseObject parseObject: parseObjects){
                        meta = new Meta();
                        meta.setCantMeta(parseObject.getInt("meta"));
                        meta.setCantFacturado(parseObject.getInt("facturado"));
                        meta.setCantPedido(parseObject.getInt("pedido"));
                        meta.setValorBs(parseObject.getDouble("meta_bs"));
                        String producto1 = parseObject.getParseObject("producto").getObjectId();
                        String codigo = parseObject.getParseObject("producto").getString("codigo");

                        producto = new Producto(producto1, null, codigo, 0.0);
                        String marca = parseObject.getParseObject("producto").getString("marca");
                        producto.setMarca(marca);
                        marcas.add(marca);

                        meta.setProducto(producto);
                        metas.add(meta);

                        //Acumular metas y cosas
                        facturadoMetas += meta.getCantFacturado();
                        pedidoMetas +=meta.getCantPedido();
                        totalMetas += meta.getCantMeta();
                    }

                    TextView metasTextView=(TextView)findViewById(R.id.metas_actual_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Actual: " + facturadoMetas);

                    metasTextView=(TextView)findViewById(R.id.metas_total_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Meta: " + totalMetas);

                    metasTextView=(TextView)findViewById(R.id.metas_restante_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Resta: " + (totalMetas-facturadoMetas));

                    ProgressBar metasProgress = (ProgressBar) findViewById(R.id.metas_gauge_progressbar);
                    metasProgress.setMax(totalMetas);
                    metasProgress.setProgress(facturadoMetas);

                    //Spinner de marcas con metas
                    final ArrayList<String> lista = new ArrayList<String>(marcas);

                    marcasAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, lista);

                    marcasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    marcasSpinner = (Spinner)findViewById(R.id.metas_spinner);

                    marcasSpinner.setAdapter(marcasAdapter);
                    marcasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            TextView tcod, tmeta, tpedido, tfacturado, tbs;
                            Boolean darkBackground = true;

                            tl = (TableLayout) findViewById(R.id.meta_list_elements);

                            //limpiar TableLayout de metas
                            if(tl.getChildCount()>2){
                                tl.removeViews(2,tl.getChildCount()-1);
                            }

                            for (Meta meta:metas){
                                if (meta.getProducto().getMarca().equalsIgnoreCase(lista.get(position))){
                                    //Crear TableRow nuevo
                                    TableRow.LayoutParams params;
                                    tr = new TableRow(mContext);
                                    tr.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                    if(!darkBackground){
                                        tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                    }else{
                                        tr.setBackgroundColor(Color.parseColor("#D9D9D9"));
                                    }

                                    //Crear y agregar TextView de Codigo a TableRow
                                    tcod = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                    tcod.setLayoutParams(params);
                                    tcod.setTextColor(Color.parseColor("#262626"));
                                    tcod.setPadding(18, 18, 18, 18);
                                    tcod.setText(meta.getProducto().getCodigo());
                                    tcod.setTypeface(null, Typeface.BOLD);
                                    tr.addView(tcod);

                                    //Crear y agregar TextView de Meta a TableRow
                                    tmeta = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    tmeta.setLayoutParams(params);
                                    tmeta.setTextColor(Color.parseColor("#262626"));
                                    tmeta.setPadding(18, 18, 18, 18);
                                    tmeta.setText(String.valueOf(meta.getCantMeta()));
                                    tmeta.setGravity(Gravity.CENTER);
                                    tr.addView(tmeta);

                                    //Crear y agregar TextView de Pedidos a TableRow
                                    tpedido = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    tpedido.setLayoutParams(params);
                                    tpedido.setTextColor(Color.parseColor("#262626"));
                                    tpedido.setPadding(18, 18, 18, 18);
                                    tpedido.setText(String.valueOf(meta.getCantPedido()));
                                    tpedido.setGravity(Gravity.CENTER);
                                    tr.addView(tpedido);

                                    //Crear y agregar TextView de Facturado a TableRow
                                    tfacturado = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    tfacturado.setLayoutParams(params);
                                    tfacturado.setTextColor(Color.parseColor("#262626"));
                                    tfacturado.setPadding(18, 18, 18, 18);
                                    tfacturado.setText(String.valueOf(meta.getCantFacturado()));
                                    tfacturado.setGravity(Gravity.CENTER);
                                    tr.addView(tfacturado);

                                    //Crear y agregar TextView de Bolivares a TableRow
                                    tbs = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                    tbs.setLayoutParams(params);
                                    tbs.setTextColor(Color.parseColor("#262626"));
                                    tbs.setPadding(18, 18, 18, 18);
                                    tbs.setText(String.valueOf(meta.getValorBs()));
                                    tbs.setGravity(Gravity.CENTER);
                                    tr.addView(tbs);

                                    //Añadir a TableLayout de metas
                                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    darkBackground=!darkBackground;
                                }
                            }

                            ProgressBar loading = (ProgressBar) findViewById(R.id.metas_progressBar);
                            loading.setVisibility(View.GONE);

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Log.d("DEBUG","Metas cargadas");
                }else{
                    Log.d("DEBUG",e.toString());
                }
            }
        });

	}
	 
	private Cliente retrieveCliente(ParseObject producto){
		
		String nombre = producto.getString("nombre");		
		Cliente client = new Cliente(nombre);
		
		return client;
	}
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
        return null;
	}
	
	public void createNewPedido(View view){
		Bundle bundle;
		bundle = new Bundle();
		this.dispatchActivity(CatalogoActivity.class, bundle, false);

	}

    private class ObtenerMetasTask extends AsyncTask<ParseQuery, Void, TableRow[]> {
        @Override
        protected TableRow[] doInBackground(ParseQuery... queries) {
                try {
                    ParseQuery query = queries[0];
                    query.findInBackground(new FindCallback() {
                        @Override
                        public void done(List<ParseObject> parseObjects, ParseException e) {
                            if(e == null){
                                Meta meta;
                                Producto producto;
                                int facturadoMetas = 0, totalMetas = 0, pedidoMetas = 0;

                                for (ParseObject parseObject: parseObjects){
                                    meta = new Meta();
                                    meta.setCantMeta(parseObject.getInt("meta"));
                                    meta.setCantFacturado(parseObject.getInt("facturado"));
                                    meta.setCantPedido(parseObject.getInt("pedido"));
                                    meta.setValorBs(parseObject.getDouble("meta_bs"));
                                    String producto1 = parseObject.getParseObject("producto").getObjectId();
                                    String codigo = parseObject.getParseObject("producto").getString("codigo");

                                    producto = new Producto(producto1, null, codigo, 0.0);
                                    String marca = parseObject.getParseObject("producto").getString("marca");
                                    producto.setMarca(marca);
                                    marcas.add(marca);

                                    meta.setProducto(producto);
                                    metas.add(meta);

                                    //Acumular metas y cosas
                                    facturadoMetas += meta.getCantFacturado();
                                    pedidoMetas +=meta.getCantPedido();
                                    totalMetas += meta.getCantMeta();
                                }

                                TextView metasTextView=(TextView)findViewById(R.id.metas_actual_textView);
                                metasTextView.setTextSize(18);
                                metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                                metasTextView.setTextColor(Color.WHITE);
                                metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                metasTextView.setText("Actual: " + facturadoMetas);

                                metasTextView=(TextView)findViewById(R.id.metas_total_textView);
                                metasTextView.setTextSize(18);
                                metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                                metasTextView.setTextColor(Color.WHITE);
                                metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                metasTextView.setText("Meta: " + totalMetas);

                                metasTextView=(TextView)findViewById(R.id.metas_restante_textView);
                                metasTextView.setTextSize(18);
                                metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                                metasTextView.setTextColor(Color.WHITE);
                                metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                metasTextView.setText("Resta: " + (totalMetas-facturadoMetas));

                                ProgressBar metasProgress = (ProgressBar) findViewById(R.id.metas_gauge_progressbar);
                                metasProgress.setMax(totalMetas);
                                metasProgress.setProgress(facturadoMetas);

                                //Spinner de marcas con metas
                                final ArrayList<String> lista = new ArrayList<String>(marcas);

                                marcasAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, lista);

                                marcasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                                marcasSpinner = (Spinner)findViewById(R.id.metas_spinner);

                                marcasSpinner.setAdapter(marcasAdapter);
                                marcasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        TextView tcod, tmeta, tpedido, tfacturado, tbs;
                                        Boolean darkBackground = true;

                                        tl = (TableLayout) findViewById(R.id.meta_list_elements);

                                        //limpiar TableLayout de metas
                                        if(tl.getChildCount()>2){
                                            tl.removeViews(2,tl.getChildCount()-1);
                                        }

                                        for (Meta meta:metas){
                                            if (meta.getProducto().getMarca().equalsIgnoreCase(lista.get(position))){
                                                //Crear TableRow nuevo
                                                TableRow.LayoutParams params;
                                                tr = new TableRow(mContext);
                                                tr.setLayoutParams( new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                                                if(!darkBackground){
                                                    tr.setBackgroundColor(Color.parseColor("#FFFFFF"));
                                                }else{
                                                    tr.setBackgroundColor(Color.parseColor("#D9D9D9"));
                                                }

                                                //Crear y agregar TextView de Codigo a TableRow
                                                tcod = new TextView(mContext);
                                                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                                tcod.setLayoutParams(params);
                                                tcod.setTextColor(Color.parseColor("#262626"));
                                                tcod.setPadding(18, 18, 18, 18);
                                                tcod.setText(meta.getProducto().getCodigo());
                                                tcod.setTypeface(null, Typeface.BOLD);
                                                tr.addView(tcod);

                                                //Crear y agregar TextView de Meta a TableRow
                                                tmeta = new TextView(mContext);
                                                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                                tmeta.setLayoutParams(params);
                                                tmeta.setTextColor(Color.parseColor("#262626"));
                                                tmeta.setPadding(18, 18, 18, 18);
                                                tmeta.setText(String.valueOf(meta.getCantMeta()));
                                                tmeta.setGravity(Gravity.CENTER);
                                                tr.addView(tmeta);

                                                //Crear y agregar TextView de Pedidos a TableRow
                                                tpedido = new TextView(mContext);
                                                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                                tpedido.setLayoutParams(params);
                                                tpedido.setTextColor(Color.parseColor("#262626"));
                                                tpedido.setPadding(18, 18, 18, 18);
                                                tpedido.setText(String.valueOf(meta.getCantPedido()));
                                                tpedido.setGravity(Gravity.CENTER);
                                                tr.addView(tpedido);

                                                //Crear y agregar TextView de Facturado a TableRow
                                                tfacturado = new TextView(mContext);
                                                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                                tfacturado.setLayoutParams(params);
                                                tfacturado.setTextColor(Color.parseColor("#262626"));
                                                tfacturado.setPadding(18, 18, 18, 18);
                                                tfacturado.setText(String.valueOf(meta.getCantFacturado()));
                                                tfacturado.setGravity(Gravity.CENTER);
                                                tr.addView(tfacturado);

                                                //Crear y agregar TextView de Bolivares a TableRow
                                                tbs = new TextView(mContext);
                                                params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                                tbs.setLayoutParams(params);
                                                tbs.setTextColor(Color.parseColor("#262626"));
                                                tbs.setPadding(18, 18, 18, 18);
                                                tbs.setText(String.valueOf(meta.getValorBs()));
                                                tbs.setGravity(Gravity.CENTER);
                                                tr.addView(tbs);

                                                //Añadir a TableLayout de metas
                                                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                                darkBackground=!darkBackground;
                                            }
                                        }

                                        ProgressBar loading = (ProgressBar) findViewById(R.id.metas_progressBar);
                                        loading.setVisibility(View.GONE);

                                    }
                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                });

                                Log.d("DEBUG","Metas cargadas");
                            }else{
                                Log.d("DEBUG",e.toString());
                            }
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(TableRow[] rows) {
        }
    }

    private void getIVAFromParse(){
        //Obtener IVA desde Parse
        ParseQuery queryIva = new ParseQuery("Impuestos");
        queryIva.whereEqualTo("nombre","IVA");
        queryIva.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseImp, ParseException e) {
                iva = parseImp.getDouble("porcentaje")/100.0;
            }
        });
    }

    public double getIva(){
        return iva;
    }
}
