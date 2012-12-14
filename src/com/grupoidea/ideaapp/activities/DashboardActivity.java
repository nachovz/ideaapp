package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.RowCliente;
import com.grupoidea.ideapp.models.Request;
import com.grupoidea.ideapp.models.Response;
import com.parse.ParseUser;

public class DashboardActivity extends ParentMenuActivity {
	/** Elemento que contiene la sesion del usuario actual*/ 
	private ParseUser user;
	/** ViewGroup que contiene las filas con informacion de los clientes*/
	private LinearLayout clienteList;
	
	public DashboardActivity() {
		super(false, false, false); //TODO Cambiar valores a autoLoad:true y useCache:true!
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		RowCliente row;
		
		super.onCreate(savedInstanceState);
		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.dashboard_layout);
		
		clienteList = (LinearLayout) findViewById(R.id.client_list_linear_layout);
		
		//Creacion de Rows dummy!
		row = new RowCliente(this, 40, "Centro Comercial Lider", 40000);
		clienteList.addView(row);
		row = new RowCliente(this, 30, "Restaurant Tamarindo", 30000);
		clienteList.addView(row);
		row = new RowCliente(this, 20, "Central Madeirense", 20000);
		clienteList.addView(row);
		row = new RowCliente(this, 10, "Makro", 10000);
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
