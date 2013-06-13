package com.grupoidea.ideaapp.activities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowClientePedido;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Pedido;
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
    //private List<ParseObject> metas;
    private ArrayList<Meta> metas;

    /** Lista de Marcas disponibles para el Spinner */
    private HashSet<String> marcas;

    /** Marcas Spinner */
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
		
		//clienteList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
		pedidosList = (LinearLayout) findViewById(R.id.client_list_linear_layout);

        ParseQuery queryPedidos = new ParseQuery("Pedido");
        queryPedidos.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryPedidos.include("cliente");
        queryPedidos.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> parseObjects, ParseException e) {
                if (e == null) {
                    RowClientePedido row;
                    for (ParseObject parseObject : parseObjects) {
                        ParseObject cliente = parseObject.getParseObject("cliente");
                        row = new RowClientePedido(mContext, cliente.getString("nombre"), cliente.getInt("estado"));
                        pedidosList.addView(row);
                    }
                    Log.d("DEBUG", "Carga de pedidos completa");
                } else {
                    Log.d("PARSE", e.toString());
                }
            }
        });

		/*row = new RowClientePedido(this, "Centro Comercial Lider", Pedido.ESTADO_VERIFICANDO);
		pedidosList.addView(row);
		row = new RowClientePedido(this, "Restaurant Tamarindo", Pedido.ESTADO_APROBADO);
		pedidosList.addView(row);
		row = new RowClientePedido(this, "Makro", Pedido.ESTADO_RECHAZADO);
		pedidosList.addView(row);*/
		
		user = ParseUser.getCurrentUser();
		if(user != null) {
			setMenuTittle(user.getUsername());
		}

        ParseQuery query = new ParseQuery("Metas");
        query.whereEqualTo("asesor", ParseUser.getCurrentUser());
        query.include("producto.marca");
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
                        String marca = parseObject.getParseObject("producto").getParseObject("marca").getString("nombre");
                        producto.setNombreMarca(marca);
                        marcas.add(marca);

                        meta.setProducto(producto);
                        metas.add(meta);
                    }

                    final ArrayList<String> lista = new ArrayList<String>(marcas);

                    marcasAdapter = new ArrayAdapter<String>(mContext, android.R.layout.simple_spinner_item, lista);

                    marcasAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                    marcasSpinner = (Spinner)findViewById(R.id.metas_spinner);

                    marcasSpinner.setAdapter(marcasAdapter);
                    marcasSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                            View row;
                            LayoutInflater inflador;
                            TextView textView;
                            LinearLayout scrollView = (LinearLayout)((Activity) mContext).findViewById(R.id.meta_list_elements);
                            scrollView.removeAllViews();

                            for (Meta meta:metas){
                                if (meta.getProducto().getNombreMarca().equalsIgnoreCase(lista.get(position))){
                                    inflador = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                    row = (View)inflador.inflate(R.layout.row_meta_producto_layout, null);
                                    textView = (TextView)row.findViewById(R.id.meta_producto_codigo_text_view);
                                    textView.setText(meta.getProducto().getCodigo());

                                    textView = (TextView)row.findViewById(R.id.meta_numero_text_view);
                                    textView.setText(String.valueOf(meta.getValorFinal()));

                                    textView = (TextView)row.findViewById(R.id.meta_actual_text_view);
                                    textView.setText(String.valueOf(meta.getValorActual()));

                                    scrollView.addView(row);

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
		// TODO Mostrar el response de la consulta en los elementos del activity.
		/*List<ParseObject> clientesParse = (List<ParseObject>) response.getResponse();
		ArrayList<Cliente> clientes = new ArrayList<Cliente>();
		Cliente cliente;

		for (ParseObject parseObject : clientesParse) {
			cliente = retrieveCliente(parseObject);
			clientes.add(cliente);
		}*/
        /*int a = 5;
        ParseObject categoria = (ParseObject)((ArrayList)response.getResponse()).get(0);

        ParseObject categoriaMarca = new ParseObject("CategoriaMarca");
        categoriaMarca.put("categoria", categoria);
        categoriaMarca.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                Log.d("PARSE",e.toString());
            }
        });*/
	}

	@Override
	protected Request getRequestAction() {
		// TODO Crear consulta a la data del Dashboard.
//
//		Request req = new Request(Request.PARSE_REQUEST);
//
//		ParseQuery query = new ParseQuery("Cliente");
//
//		req.setRequest(query);
//
//		return req;
        /*Request req = new Request(Request.PARSE_REQUEST);
        ParseQuery query = new ParseQuery("Categoria");
        req.setRequest(query);
*/
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
