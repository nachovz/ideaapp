package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.MetasAdapter;
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class DashboardActivity extends ParentMenuActivity {
    String TAG = this.getClass().getSimpleName();
	/** ViewGroup que contiene las filas con informacion de los clientes*/
	/** ViewGroup que contiene las filas con informacion de los pedidos */
	private LinearLayout pedidosList;
    private Spinner pedidosSpinner;
    private ArrayAdapter<String> pedidosSpinnerAdapter;

    /** Lista de metas del usuario logueado */
    private ArrayList<Meta> metas;

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
		setContentView(R.layout.activity_dashboard_layout);

        Log.d(TAG, "internet status: "+GrupoIdea.isNetworkAvailable(mContext));

        //Obtener IVA para los RowPedido
        getIVAFromParse();

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

		clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        clienteSpinner.setEnabled(false);
        clienteSpinner.setVisibility(View.INVISIBLE);

        pedidosSpinnerAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, estados);
        pedidosSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        pedidosSpinner = (Spinner)findViewById(R.id.pedidos_spinner);
        pedidosSpinner.setAdapter(pedidosSpinnerAdapter);

        //Carga de Pedidos
		pedidosList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
        ParseQuery queryPedidos = new ParseQuery("Pedido");
        queryPedidos.setLimit(QUERY_LIMIT);
        queryPedidos.setCachePolicy(getParseCachePolicy());
        queryPedidos.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryPedidos.include("cliente");
        queryPedidos.orderByAscending("estado");
        queryPedidos.addDescendingOrder("createdAt");
        queryPedidos.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    app.pedidos = parseObjects;
                    //setear contador de pedidos
                    pedidosCounter.setText(String.valueOf(app.pedidos.size()));
                    pedidosCounter.setVisibility(View.VISIBLE);

                    pedidosSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            //Mostrar Loading
                            ProgressBar loading = (ProgressBar) pedidosList.findViewById(R.id.pedidos_progressBar);
                            loading.setVisibility(View.VISIBLE);

                            Log.d(TAG,"Estado seleccionado en Spinner "+estados.get(position));
                            RowPedido row;

                            //eliminar todos menos el primero
                            pedidosList.removeViews(1, pedidosList.getChildCount()-1);

                            //Agregarlos al View
                            for (ParseObject parseObject : app.pedidos) {
                                if(parseObject.getInt("estado") == position || position == Pedido.ESTADO_TODOS){
                                    ParseObject cliente = parseObject.getParseObject("cliente");
                                    row = new RowPedido(mContext, parseObject);

                                    if(position == Pedido.ESTADO_RECHAZADO){
                                        if(null != parseObject.getString("comentario_cambio_status") && !parseObject.getString("comentario_cambio_status").isEmpty()){
                                            row.observacionesRechazoPedido = parseObject.getString("comentario_cambio_status");
                                        }else{
                                            row.observacionesRechazoPedido = getString(R.string.empty_obs_rechazo_pedido);
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

                    Log.d(TAG, "Carga de pedidos completa");
                } else {
                    Log.d(TAG, e.toString());
                }
            }
        });

        //Carga de metas
        ParseQuery query = new ParseQuery("Metas");
        query.setLimit(QUERY_LIMIT);
        query.setCachePolicy(getParseCachePolicy());
        query.whereEqualTo("asesor", ParseUser.getCurrentUser());
        query.include("producto");
        query.include("asesor");
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    app.metasParse = parseObjects;
                    Meta meta;
                    Producto producto;
                    int facturadoMetas = 0, totalMetas = 0;

                    //Instanciar Metas
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

                    //Almacenar metas en application
                    app.metas = metas;

                    //Setear Vista de Meta Actual
                    TextView metasTextView=(TextView)findViewById(R.id.metas_actual_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Actual: " + facturadoMetas);

                    //Setear Vista de Meta Total
                    metasTextView=(TextView)findViewById(R.id.metas_total_textView);
                    metasTextView.setTextSize(18);
                    metasTextView.setTypeface(null, Typeface.BOLD_ITALIC);
                    metasTextView.setTextColor(Color.WHITE);
                    metasTextView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                    metasTextView.setText("Meta: " + totalMetas);

                    //Setear Vista de Meta Restante
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

                    MetasAdapter metasAdapter = new MetasAdapter(metas, mContext);
                    ListView metasListView = (ListView) findViewById(R.id.metas_listView);
                    metasAdapter.updateMarca(lista.get(0));
                    metasListView.setAdapter(metasAdapter);
                    marcasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            TextView codigoMetaTextView, metaMetaTextView, pedidoMetaTextView, facturadoMetaTextView, montoMetaTextView;
//                            Boolean darkBackground = true;

                            assert marcasSpinner != null && marcasSpinner.getSelectedItem() != null;
                            MetasAdapter metasAdapter = new MetasAdapter(metas, mContext);
                            ListView metasListView = (ListView) findViewById(R.id.metas_listView);
                            metasListView.setAdapter(metasAdapter);
                            metasAdapter.updateMarca(lista.get(position));
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });


                    Log.d(TAG,"Metas cargadas");
                }else{
                    Log.d(TAG,e.toString());
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

    private void getIVAFromParse(){
        //Obtener IVA desde Parse
        ParseQuery queryIva = new ParseQuery("Impuestos");
        queryIva.setLimit(QUERY_LIMIT);
        queryIva.setCachePolicy(getParseCachePolicy());
        queryIva.whereEqualTo("nombre","IVA");
        queryIva.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseImp, ParseException e) {
                if(e == null){
                    if(parseImp == null){
                        Log.d(TAG, "No existe un valor especificado para el IVA en la BD");
                        app.iva = 12.0;
                    }else{
                        app.iva = parseImp.getDouble("porcentaje")/100.0;
//                        Log.d(TAG, "IVA: " + parseImp.getDouble("porcentaje")+"%");
                    }
                }else{
                    Log.d(TAG, "Error en el Query para obtener el IVA");
                    e.printStackTrace();
                }
            }
        });
    }

    public double getIva(){
        return app.iva;
    }

    @Override
    public void reloadApp() {
        //@TODO Verificar primero si hay internet
        ParseQuery.clearAllCachedResults();
        finish();
        getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(getIntent());
    }
}
