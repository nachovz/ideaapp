package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowClienteMarca;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.parse.ParseUser;

public class DashboardActivity extends ParentMenuActivity {
	/** Elemento que contiene la sesion del usuario actual*/ 
	private ParseUser user;
	/** ViewGroup que contiene las filas con informacion de los clientes*/
	private LinearLayout clienteList;
	
	public DashboardActivity() {
		super(false, false); //TODO Cambiar valores a autoLoad:true y useCache:true!
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		RowClienteMarca row;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.dashboard_layout);
		
		clienteList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
		
		//Creacion de Rows dummy!
		row = new RowClienteMarca(this, "Centro Comercial Lider");
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
		clienteList.addView(row);
		
		user = ParseUser.getCurrentUser();
		if(user != null) {
			setMenuTittle(user.getUsername());
		}
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		// TODO Mostrar el response de la consulta en los elementos del activity.

	}

	@Override
	protected Request getRequest() {
		// TODO Crear consulta a la data del Dashboard.
		return null;
	}

}
