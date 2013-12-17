package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
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

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowPedido;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.text.DecimalFormat;
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
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");

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

        app = (GrupoIdea) getApplication();
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
                            //Mostrar Loading
                            ProgressBar loading = (ProgressBar) pedidosList.findViewById(R.id.pedidos_progressBar);
                            loading.setVisibility(View.VISIBLE);

                            Log.d("DEBUG","Estado seleccionado en Spinner "+estados.get(position));
                            RowPedido row;

                            //eliminar todos menos el primero
                            pedidosList.removeViews(1, pedidosList.getChildCount()-1);

                            //Agregarlos al View
                            for (ParseObject parseObject : pedidos) {
                                if(parseObject.getInt("estado") == position || position == Pedido.ESTADO_TODOS){
                                    ParseObject cliente = parseObject.getParseObject("cliente");
                                    row = new RowPedido(mContext, parseObject.getObjectId(), cliente.getString("nombre"), parseObject.getInt("estado"), parseObject.getString("num_pedido"), parseObject.getCreatedAt(), parseObject.getUpdatedAt());

                                    if(position == Pedido.ESTADO_RECHAZADO){
                                        if(null != parseObject.getString("comentario_cambio_status")){
                                            row.observacionesRechazoPedido = parseObject.getString("comentario_cambio_status");
                                        }else{
                                            row.observacionesRechazoPedido = "";
                                        }
                                    }
                                    pedidosList.addView(row);
                                }
                            }

                            //Ocultar Loading
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
        query.whereEqualTo("asesor", ParseUser.getCurrentUser());
        query.include("producto");
        query.include("asesor");
//        ObtenerMetasTask obtenerMetas =  new ObtenerMetasTask();
//        obtenerMetas.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    app.metasParse = parseObjects;
                    Meta meta;
                    Producto producto;
                    int facturadoMetas = 0, totalMetas = 0;

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
                        totalMetas += meta.getCantMeta();
                    }

                    app.metas = metas;

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
                            TextView codigoMetaTextView, metaMetaTextView, pedidoMetaTextView, facturadoMetaTextView, montoMetaTextView;
                            Boolean darkBackground = true;

                            tl = (TableLayout) findViewById(R.id.meta_list_elements);
                            tl.removeAllViews();
                            tl.setVisibility(View.GONE);

                            //Mostrar Loading
                            ProgressBar loading = (ProgressBar) findViewById(R.id.metas_progressBar);
                            loading.setVisibility(View.VISIBLE);

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
                                    codigoMetaTextView = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                    codigoMetaTextView.setLayoutParams(params);
                                    codigoMetaTextView.setTextColor(Color.parseColor("#262626"));
                                    codigoMetaTextView.setPadding(18, 18, 18, 18);
                                    codigoMetaTextView.setText(meta.getProducto().getCodigo());
                                    codigoMetaTextView.setTypeface(null, Typeface.BOLD);
                                    tr.addView(codigoMetaTextView);

                                    //Crear y agregar TextView de Meta a TableRow
                                    metaMetaTextView = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    metaMetaTextView.setLayoutParams(params);
                                    metaMetaTextView.setTextColor(Color.parseColor("#262626"));
                                    metaMetaTextView.setPadding(18, 18, 18, 18);
                                    metaMetaTextView.setText(String.valueOf(meta.getCantMeta()));
                                    metaMetaTextView.setGravity(Gravity.CENTER);
                                    tr.addView(metaMetaTextView);

                                    //Crear y agregar TextView de Pedidos a TableRow
                                    pedidoMetaTextView = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    pedidoMetaTextView.setLayoutParams(params);
                                    pedidoMetaTextView.setTextColor(Color.parseColor("#262626"));
                                    pedidoMetaTextView.setPadding(18, 18, 18, 18);
                                    pedidoMetaTextView.setText(String.valueOf(meta.getCantPedido()));
                                    pedidoMetaTextView.setGravity(Gravity.CENTER);
                                    tr.addView(pedidoMetaTextView);

                                    //Crear y agregar TextView de Facturado a TableRow
                                    facturadoMetaTextView = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1);
                                    facturadoMetaTextView.setLayoutParams(params);
                                    facturadoMetaTextView.setTextColor(Color.parseColor("#262626"));
                                    facturadoMetaTextView.setPadding(18, 18, 18, 18);
                                    facturadoMetaTextView.setText(String.valueOf(meta.getCantFacturado()));
                                    facturadoMetaTextView.setGravity(Gravity.CENTER);
                                    tr.addView(facturadoMetaTextView);

                                    //Crear y agregar TextView de Bolivares a TableRow
                                    montoMetaTextView = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 2);
                                    montoMetaTextView.setLayoutParams(params);
                                    montoMetaTextView.setTextColor(Color.parseColor("#262626"));
                                    montoMetaTextView.setPadding(18, 18, 18, 18);
                                    montoMetaTextView.setText(df.format(meta.getValorBs()));
                                    montoMetaTextView.setGravity(Gravity.CENTER);
                                    tr.addView(montoMetaTextView);

                                    //Añadir a TableLayout de metas
                                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    darkBackground=!darkBackground;
                                }
                            }

                            loading.setVisibility(View.GONE);
                            tl.setVisibility(View.VISIBLE);

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
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
        return null;
	}
	
	public void createNewPedido(View view){
		Bundle bundle = new Bundle();
        app.pedido = null;
		this.dispatchActivity(CatalogoActivity.class, bundle, false);
	}

    private class ObtenerMetasTask extends AsyncTask<ParseQuery, Void, ArrayList<TableRow>> {
        @Override
        protected ArrayList<TableRow> doInBackground(ParseQuery... queries) {
                try {
                    Meta meta;
                    Producto producto;
                    final ArrayList<TableRow> rows = new ArrayList<TableRow>();
                    int facturadoMetas = 0, totalMetas = 0, pedidoMetas = 0;

                    //Hacer find
                    ParseQuery query = queries[0];
                    List<ParseObject> parseObjects = query.find();

                    //Extraer Metas
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

                    /*
                    Actualizar Gauge de Metas
                     */

                    TextView metasTextView=(TextView) findViewById(R.id.metas_actual_textView);
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

                    metasTextView = (TextView)findViewById(R.id.metas_restante_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Resta: " + (totalMetas-facturadoMetas));

                    ProgressBar metasProgress = (ProgressBar) findViewById(R.id.metas_gauge_progressbar);
                    metasProgress.setMax(totalMetas);
                    metasProgress.setProgress(facturadoMetas);

                    /*
                    Fin Actualizar Gauge de Metas
                     */

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

                                    //Agregar row a arreglo
                                    rows.add(tr);
                                    darkBackground=!darkBackground;
                                }
                            }

                            ProgressBar loading = (ProgressBar) findViewById(R.id.metas_progressBar);
                            loading.setVisibility(View.GONE);

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {}
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<TableRow> rows) {

            tl = (TableLayout) findViewById(R.id.meta_list_elements);
            //limpiar TableLayout de metas
            if(tl.getChildCount()>2){
                tl.removeViews(2,tl.getChildCount()-1);
            }

            for(TableRow tr: rows){
                //Añadir a TableLayout de metas
                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            }

        }
    }

    private void getIVAFromParse(){
        //Obtener IVA desde Parse
        ParseQuery queryIva = new ParseQuery("Impuestos");
        queryIva.whereEqualTo("nombre","IVA");
        queryIva.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseImp, ParseException e) {
                app.iva = parseImp.getDouble("porcentaje")/100.0;
            }
        });
    }

    public double getIva(){
        return app.iva;
    }

    @Override
    public void reloadApp() {
        ParseQuery.clearAllCachedResults();
        finish();
        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(getIntent());
    }
}
