package com.grupoidea.ideaapp.activities;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowClienteMarca;
import com.grupoidea.ideaapp.components.RowClientePedido;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

public class DashboardActivity extends ParentMenuActivity {
	/** Elemento que contiene la sesion del usuario actual*/ 
	private ParseUser user;
	/** ViewGroup que contiene las filas con informacion de los clientes*/
	//private LinearLayout clienteList;
	/** ViewGroup que contiene las filas con informaci—n de los pedidos */
	private LinearLayout pedidosList;
	
	public DashboardActivity() {
		super(true, false); //TODO Cambiar valores a autoLoad:true y useCache:true!
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		//RowClienteMarca row;
		RowClientePedido row;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
		
		//clienteList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
		pedidosList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
		
		//Creacion de Rows dummy!
		/*row = new RowClienteMarca(this, "Centro Comercial Lider");
		row.addMarca("AirOn");
		row.addMarca("Airston");
		row.addMarca("Berloni");
		row.addMarca("DeLonghi");
		clienteList.addView(row);
		row = new RowClienteMarca(this, "Restaurant Tamarindo");
		row.addMarca("Samsung");
		row.addMarca("Thermos");
		row.addMarca("AirOn");
		clienteList.addView(row);
		row = new RowClienteMarca(this, "Central Madeirense");
		row.addMarca("Samsung");
		clienteList.addView(row);
		row = new RowClienteMarca(this, "Makro");
		row.addMarca("Samsung");
		row.addMarca("AirOn");
		row.addMarca("Airston");
		row.addMarca("Thermos");
		row.addMarca("Berloni");
		row.addMarca("DeLonghi");
		clienteList.addView(row);*/
		row = new RowClientePedido(this, "Centro Comercial Lider", Pedido.ESTADO_VERIFICANDO);
		pedidosList.addView(row);
		row = new RowClientePedido(this, "Restaurant Tamarindo", Pedido.ESTADO_APROBADO);
		pedidosList.addView(row);
		row = new RowClientePedido(this, "Makro", Pedido.ESTADO_RECHAZADO);
		pedidosList.addView(row);
		
		user = ParseUser.getCurrentUser();
		if(user != null) {
			setMenuTittle(user.getUsername());
		}
	}
	 
	private Cliente retreiveCliente(ParseObject producto){
		
		String nombre = producto.getString("nombre");		
		Cliente client = new Cliente(nombre);
		
		return client;
	}
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		// TODO Mostrar el response de la consulta en los elementos del activity.
		List<ParseObject> clientesParse = (List<ParseObject>) response.getResponse();
		ArrayList<Cliente> clientes = new ArrayList<Cliente>();
		Cliente cliente;
		
		for (ParseObject parseObject : clientesParse) {	
			cliente = retreiveCliente(parseObject);
			clientes.add(cliente);
		}
		
	}

	@Override
	protected Request getRequestAction() {
		// TODO Crear consulta a la data del Dashboard.
		
		Request req = new Request(Request.PARSE_REQUEST);
		
		ParseQuery query = new ParseQuery("Cliente");
		
		req.setRequest(query);
		
		return req;
	}
	
	public void createNewPedido(View view){
		Bundle bundle;
		//TODO: Enviar idCliente mediante el bundle para que el CatalogoActivity se encargue de consultar los productos destacados del cliente
		bundle = new Bundle();
		//bundle.putString("clienteNombre", clienteNombre.getText().toString());
		this.dispatchActivity(CatalogoActivity.class, bundle, false);
	}

}
