package com.grupoidea.ideaapp.activities;

import java.util.List;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideapp.models.Request;
import com.grupoidea.ideapp.models.Response;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/** Activity que permite al usuario autentificarse en la aplición mediante la data obtenida de Parse*/
public class LoginActivity extends ParentActivity {
	/** View que almacena el nombre de usuario*/
	private TextView userTextView;
	/** View que almacena el la contraseña del usuario*/
	private TextView passwordTextView;
	
	/** Constructor que establece que el Activity no consultara el proveedor de servicios al ser creada
	 *  y no almacenara la consulta en cache.*/
	public LoginActivity() {
		super(false, false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Button ingresar;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		
		userTextView = (TextView) findViewById(R.id.username_edit_text);
		passwordTextView = (TextView) findViewById(R.id.password_edit_text);
		
		ingresar = (Button) findViewById(R.id.ingresar_button);
		ingresar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loadData();
			}
		});
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		List<ParseObject> parseData = null;
		ParseObject parseObject;
		
		if(response.getResponse() instanceof List<?>) {
			parseData = (List<ParseObject>) response.getResponse();
			
			if(parseData != null && parseData.size() > 0) {
				parseObject = parseData.get(0);
				Log.d("Debug", new StringBuffer(parseObject.getString("login")).append(parseObject.getString("password")).toString());
			} else {
				Log.e("Error", "Nombre de usuario y/o contraseña invalidos");
			}
				
		}
	}

	@Override
	protected Request getRequest() {
		ParseQuery query;
		Request request = null;
		String login = null;
		String pass = null;
		
		login = userTextView.getText().toString();
		pass = passwordTextView.getText().toString();
		
		if(login != null && !login.isEmpty() && pass != null && !pass.isEmpty()){
			request = new Request(Request.PARSE_REQUEST);
			query = new ParseQuery("Usuario");
			
			query.whereEqualTo("login", login);
			query.whereEqualTo("password", pass);
			request.setRequest(query);
		} else {
			Log.e("Error", "Nombre de usuario y/o contraseña vacios");
		}
		
		return request;
	}
}
