package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.*;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowClientePedido;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
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
		super(true, false); //TODO Cambiar valores a autoLoad:true y useCache:true!
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
        metas = new ArrayList<Meta>();
        marcas = new HashSet<String>();
		mContext = this;

		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
		
		//Carga de Pedidos
		pedidosList = (LinearLayout) findViewById(R.id.client_list_linear_layout);

        ParseQuery queryPedidos = new ParseQuery("Pedido");
        queryPedidos.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryPedidos.include("cliente");
        queryPedidos.orderByDescending("createdAt");
        queryPedidos.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    RowClientePedido row;
                    for (ParseObject parseObject : parseObjects) {
                        ParseObject cliente = parseObject.getParseObject("cliente");
                        row = new RowClientePedido(mContext, cliente.getString("nombre"), parseObject.getInt("estado"), parseObject.getCreatedAt());
                        pedidosList.addView(row);
                    }
                    Log.d("DEBUG", "Carga de pedidos completa");
                } else {
                    Log.d("PARSE", e.toString());
                }
            }
        });
		
		user = ParseUser.getCurrentUser();
		if(user != null) {
			setMenuTittle(user.getUsername());
		}

        //Carga de metas
        ParseQuery query = new ParseQuery("Metas");
        query.whereEqualTo("asesor", ParseUser.getCurrentUser());
        query.include("producto");
        query.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if(e == null){
                    Meta meta;
                    Producto producto;

                    for (ParseObject parseObject: parseObjects){
                        meta = new Meta();
                        meta.setValorFinal(parseObject.getDouble("meta"));
                        meta.setValorActual(parseObject.getDouble("pedido"));
                        String producto1 = parseObject.getParseObject("producto").getObjectId();
                        String codigo = parseObject.getParseObject("producto").getString("codigo");

                        producto = new Producto(producto1, null, codigo, 0.0);
                        String marca = parseObject.getParseObject("producto").getString("marca");
                        producto.setMarca(marca);
                        marcas.add(marca);

                        meta.setProducto(producto);
                        metas.add(meta);
                    }

                    //Spinner de marcas con metas
                    final ArrayList<String> lista = new ArrayList<String>(marcas);

                    marcasAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, lista);

                    marcasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    marcasSpinner = (Spinner)findViewById(R.id.metas_spinner);

                    marcasSpinner.setAdapter(marcasAdapter);
                    marcasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                            LayoutInflater inflador;
                            TextView tcod, tmeta, tpedido;
//                            LinearLayout scrollView = (LinearLayout)((Activity) mContext).findViewById(R.id.metas_scroll_view);
//                            scrollView.removeAllViews();
//                            inflador = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                            Boolean darkBackground = true;
                            tl = (TableLayout) findViewById(R.id.meta_list_elements);
                            //limpiar TableLayout de metas
                            if(tl.getChildCount()>1){
                                tl.removeViews(1,tl.getChildCount()-1);
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
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
                                    tcod.setLayoutParams(params);
                                    tcod.setTextColor(Color.parseColor("#262626"));
                                    tcod.setPadding(18, 18, 18, 18);
                                    tcod.setText(meta.getProducto().getCodigo());
                                    tr.addView(tcod);

                                    //Crear y agregar TextView de Meta a TableRow
                                    tmeta = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.2"));
                                    tmeta.setLayoutParams(params);
                                    tmeta.setTextColor(Color.parseColor("#262626"));
                                    tmeta.setPadding(18, 18, 18, 18);
                                    tmeta.setText(String.valueOf(meta.getValorFinal()));
                                    tmeta.setGravity(Gravity.CENTER);
                                    tr.addView(tmeta);

                                    //Crear y agregar TextView de Pedidos a TableRow
                                    tpedido = new TextView(mContext);
                                    params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, Float.parseFloat("0.25"));
                                    tpedido.setLayoutParams(params);
                                    tpedido.setTextColor(Color.parseColor("#262626"));
                                    tpedido.setPadding(18, 18, 18, 18);
                                    tpedido.setText(String.valueOf(meta.getValorActual()));
                                    tpedido.setGravity(Gravity.CENTER);
                                    tr.addView(tpedido);

                                    //Añadir a TableLayout de metas
                                    tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                                    darkBackground=!darkBackground;

                                }
                            }

                        }
                        @Override
                        public void onNothingSelected(AdapterView<?> parent) {

                        }
                    });

                    Log.d("DEBUG","Metas cargadas");
                }else{
                    Log.d("PARSE",e.toString());
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
		//TODO: Enviar idCliente mediante el bundle para que el CatalogoActivity se encargue de consultar los productos destacados del cliente
		bundle = new Bundle();
		//bundle.putString("clienteNombre", clienteNombre.getText().toString());
		this.dispatchActivity(CatalogoActivity.class, bundle, false);
	}

}
