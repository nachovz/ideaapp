package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideapp.models.Request;
import com.grupoidea.ideapp.models.Response;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

/** Activity que permite al usuario autentificarse en la aplición mediante la data obtenida de Parse*/
public class LoginActivity extends ParentActivity {
	private LoginActivity loginActivity;
	/** View que almacena el nombre de usuario*/
	private TextView userTextView;
	/** View que almacena el la contraseña del usuario*/
	private TextView passwordTextView;
	/** Elemnto grafico que permite mostrarle al usuario que hay una consulta en curso*/
	private ProgressBar loading;
	
	public LoginActivity() {
		super(false, false);
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		Button ingresar;
		ParseUser currentUser;
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.login_layout);
		
		loginActivity = this;
		currentUser = ParseUser.getCurrentUser();
		
		if(currentUser != null) {
			Log.d("DEBUG", "Usuario almacenado, despachando al Dashboard");
			dispatchActivity(DashboardActivity.class, null, true);
		} else {
			Log.d("DEBUG", "Usuario no almacenado.");
		}
		
		userTextView = (TextView) findViewById(R.id.username_edit_text);
		passwordTextView = (TextView) findViewById(R.id.password_edit_text);
		loading = (ProgressBar) findViewById(R.id.login_loading_progress_bar);
		ingresar = (Button) findViewById(R.id.ingresar_button);
		ingresar.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				loading.setVisibility(View.VISIBLE);
				loginFromParse();
			}
		});
	}
	
	/** Metodo privado que permite autentificar las credeciales del usuario utilizando Parse*/
	private void loginFromParse() {
		String login = null;
		String pass = null;
		
		login = userTextView.getText().toString();
		pass = passwordTextView.getText().toString();
		
		if(login != null && !login.isEmpty() && pass != null && !pass.isEmpty()){
			ParseUser.logInInBackground(login, pass, new LogInCallback() {
				public void done(ParseUser user, ParseException e) {
				    if (user != null) {
				    	Log.d("DEBUG", "Usuario autentificado correctamente");
				    	loginActivity.dispatchActivity(DashboardActivity.class, null, true);
				    } else {
				    	Log.e("EXCEPTION", "Error al autentificar el usuario: "+e.getMessage());
				    	Toast.makeText(getApplicationContext(), getString(R.string.fallo_login), Toast.LENGTH_LONG).show();
				    }
				    loading.setVisibility(View.GONE);
				}
			});
		} else {
			Log.e("ERROR", "Nombre de usuario y/o contraseña vacios");
			Toast.makeText(getApplicationContext(), getString(R.string.vacio_login), Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequest() {
		return null;
	}
}
