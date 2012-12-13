package com.grupoidea.ideaapp.activities;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideapp.models.Request;
import com.grupoidea.ideapp.models.Response;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

/** Esta clase se encarga de determinar si las Activities hijas deben consultar al proveedor de servicios
 *  al ser creada y si debe almacenar un cache con la data consultada. Para consultar data se debe implementar
 *  el metodo abstracto <code>getRequest</code> retornando un <code>Request</code> valido. Para ejecutar la consulta los
 *  hijos pueden utilizar el metodo <code>loadData()</code> el cual se encargara de procesar el Request y ejecutar en background la consulta retornando
 *  un <code>Response</code> al metodo abstracto <code>manageReponse(Response response, isLiveData)</code> el cual debe se implementar
 *  en los hijos.*/
public abstract class ParentActivity extends Activity {
	/** Boolean que denota si el Activity debe consultar al proveedor de servicios al inciar.*/
	private boolean autoLoad;
	/**Boolean que denota si el Activity debe almacenar el resultado de una consulta exitosa.*/
	private boolean useCache;
	/** Objecto que contiene la data consultada desde el proveedor de servicios.*/
	private Response response;
	/** View que permite observar un mensaje mientras existe un proceso de consulta en background.*/
	protected TextView loadingTextView;
	/** Layout que contiene informacion sobre el estatus del request actual.*/
	private RelativeLayout parentAvailableLayout;
	
	/** Constructor que define los atributos de carga automatica y almacenamiento en cache (si es requerido por las clases hijas).  
	 *  @param  autoLoad Boolean que denota si el Activity debe consultar al proveedor de servicios al inciar.
	 *  @param  useCache Boolean que denota si el Activity debe almacenar el resultado de una consulta exitosa, este resultado se mostrara
	 *  		antes de consultar al proveedor de servicios (de esta forma el usuario observara la ultima consulta exitosa).*/
	public ParentActivity(boolean autoLoad, boolean useCache) {
		this.autoLoad = autoLoad;
		this.useCache = useCache;
	}
	
	/** Permite establecer un valor de visibilidad a el layout disponible del ParentLayout*/
	public void setParentLayoutVisibility(int visibiliy) {
		parentAvailableLayout.setVisibility(visibiliy);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_layout);
		
		loadingTextView = (TextView) findViewById(R.id.loading_text_view);
		parentAvailableLayout  = (RelativeLayout) findViewById(R.id.parent_available_layout);
		if(autoLoad) {
			loadData();
		}
	}
	
	@Override
	public void setContentView(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		parentInflater = (RelativeLayout) findViewById(R.id.parent_inflate_layout);
		parentInflater.addView(inflateView);
	}
	
	/** Se encarga de consultar datos del proveedor de servicio, utilizando <code>getRquest()</code>
	 *  se determina el tipo de request y se ejecuta la consulta, llamando al metodo <code>drawData(Response response, boolean isLiveData)</code> con el <code>Response</code> resultante.
	 *  Este metodo puede ser llamado por los hijos en cualquier momento que deseen iniciar el proceso descrito.*/
	protected void loadData() {
		Request request;
		ParseQuery query;
		
		if(useCache) {
			loadFromCache();
		}
		
		request = getRequest();
		if(request != null) {
			loadingTextView.setText(getString(R.string.cargando));
			if(request.getRequestType() == Request.PARSE_REQUEST) {
				query = (ParseQuery) request.getRequest();
				loadFromParse(query);
			} else if(request.getRequestType() == Request.HTTP_REQUEST) {
				//TODO: Implementar clase HttpRequest que permita hacer consultas http.
			}
		} else {
			Log.d("LOAD_DATA","Este activity no posee query");
		}
		
	}
	
	/** Metodo para ser implementado en los hijos que permitira utilizar la respuesta consultada utlizando el query de <code>getRequest()</code>.
	 *  @param response: Objeto que contiene el resultado de la consulta obtenida.
	 *  @param isLiveData: Boolean que determina si la respuesta proviene del cache o del proveedor de servicios.*/
	protected abstract void manageResponse(Response response, boolean isLiveData);
	
	/** Metodo para ser implementado en los hijos que permitira al Activity actual hacer una consulta a un proveedor de servicios, definidos por un <code>Request</code>*/
	protected abstract Request getRequest();
	
	/** Se encarga de obtener el cache de datos perteneciente al Activity actual y retornarlo a <code>drawData(Response response, boolean isLiveData)</code>*/
	private void loadFromCache() {
		Response response = null;
		//TODO: Implementar carga desde el cache (Implementar y utilizar PersistentStores)
		manageResponse(response, false);
	}
	
	/** Se encarga de hacer persistente un response exitoso
	 *  @response Objeto que contiene la respoesta que se desea almacenar de forma persistente en el dispositivo*/
	private void setCache(Object response) {
		//TODO: Implementar almacenamiento de objetos en el cache (Implementar y utilizar PersistentStores)
	}
	
	/** Se encarga de ejecutar la consulta de parse en background utilizando <code>findInBackground</code>.
	 *  @param query Objeto de tipo ParseQuery que contiene la consulta que se desea realizar (para mas info revisar documentación de Parse)*/
	private void loadFromParse(ParseQuery query) {
		query.findInBackground(new FindCallback(){
			@Override
			public void done(List<ParseObject> parseData, ParseException e) {
				if (e == null) {
					if(parseData != null) {
						response = new Response();
						response.setResponse(parseData);
						manageResponse(response, true);
						
						if(useCache) {
							setCache(response);
						}
						
						loadingTextView.setText(getString(R.string.end_cargando));
					}
		        } else { 
		            Log.e("Exception", "Parse Exception: " + e.getMessage());
		        }
			}
		});
	}
	
	/** Permite despachar a un Activity nuevo.
	 *  @param activityClass Contiene la clase del Activity que se quiere mostrar
	 *  @param disposeCurrentActivity Boolean que determina si luego de iniciar esta nueva actividad se debe terminar la anterior*/
	public void dispatchActivity(Class activityClass, Bundle bundle, boolean disposeCurrentActivity) {
		Intent intent = new Intent(this, activityClass);
		if(bundle != null) {
			intent.putExtras(bundle);
		}
		this.startActivity(intent);
        
		if(disposeCurrentActivity) {
        	this.finish();
        }
	}
}
