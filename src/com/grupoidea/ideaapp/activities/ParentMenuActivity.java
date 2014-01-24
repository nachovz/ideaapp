package com.grupoidea.ideaapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.models.Cliente;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentMenuActivity extends ParentActivity {
    protected String TAG = this.getClass().getSimpleName();
    final int QUERY_LIMIT = 1000;
	private TextView menuTituloTextView; 
	private ImageView logOff;
    protected ImageView refresh;
	protected ImageView menuFilterIcon;
	private ImageView carrito;
    protected ProgressDialog catalogoProgressDialog;
    protected ProgressDialog carritoProgressDialog;
	protected Spinner clienteSpinner;
    public static int clienteSelected;
    protected ArrayAdapter<String> clientesAdapter;
    public static ArrayList<Cliente> clientes;
	private RelativeLayout frontLayout;
	
	private ViewGroup menuRight;
	private ViewGroup menuLeft;
	
	private Boolean menuRightShowed;
	private Boolean menuLeftShowed;
	private Boolean hasMenuRight;
	private Boolean hasMenuLeft;
	
	/** Constructor sobrecargado.
	 * @param  autoLoad Boolean que denota si el Activity debe consultar al proveedor de servicios al inciar.
	 * @param  useCache Boolean que denota si el Activity debe almacenar el resultado de una consulta exitosa, este resultado se mostrara
	 *  		antes de consultar al proveedor de servicios (de esta forma el usuario observara la ultima consulta exitosa).*/
	public ParentMenuActivity(boolean autoLoad, boolean useCache) {
		this(autoLoad, useCache, false, false);
	}
	/** Constructor sobrecargado.
	 * @param  autoLoad Boolean que denota si el Activity debe consultar al proveedor de servicios al inciar.
	 * @param  useCache Boolean que denota si el Activity debe almacenar el resultado de una consulta exitosa, este resultado se mostrara
	 *  		antes de consultar al proveedor de servicios (de esta forma el usuario observara la ultima consulta exitosa).
	 * @param hasMenuRight Boolean que denota si el Activity posee menu lateral dereceho (por defecto se agrega el boton mostrar carrito
	 * 			que permite  despligar/ocultar el menu de carrito)*/
	public ParentMenuActivity(boolean autoLoad, boolean useCache, boolean hasMenuRight) {
		this(autoLoad, useCache, hasMenuRight, false);
	}
	
	/** Constructor por defecto (contiene la implementacion de los constructores sobrecargados)
	 * @param  autoLoad Boolean que denota si el Activity debe consultar al proveedor de servicios al inciar.
	 * @param  useCache Boolean que denota si el Activity debe almacenar el resultado de una consulta exitosa, este resultado se mostrara
	 *  		antes de consultar al proveedor de servicios (de esta forma el usuario observara la ultima consulta exitosa).
	 * @param hasMenuRight Boolean que denota si el Activity posee menu lateral dereceho (por defecto se agrega el boton mostrar carrito
	 * 			que permite  despligar/ocultar el menu de carrito)
	 * @param hasMenuLeft Boolean que denota si el Activity posee menu lateral izquierdo (por defecto se agrega la funcionalidad de
	 * 			mostrar/ocultar el menu de filtro de categorias del catalogo al hacer click en el icono "menu").*/
	public ParentMenuActivity(boolean autoLoad, boolean useCache, boolean hasMenuRight, boolean hasMenuLeft) {
		super(autoLoad, useCache);
		this.hasMenuRight = hasMenuRight;
		this.hasMenuLeft = hasMenuLeft;
		menuRight = null;
		setMenuLeft(null);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
		
		setParentLayoutVisibility(View.GONE);

		menuRightShowed = false;
		menuLeftShowed = false;
        app = (GrupoIdea) getApplication();
		
		menuTituloTextView = (TextView) findViewById(R.id.menu_titulo_text_view);
        refresh = (ImageView) findViewById(R.id.menu_refresh_image_view);
		logOff = (ImageView) findViewById(R.id.menu_logoff_image_view);
		menuFilterIcon = (ImageView) findViewById(R.id.menu_icon_image_view);
        menuFilterIcon.setEnabled(false);
		carrito = (ImageView) findViewById(R.id.menu_carrito_image_view);

		clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        clienteSpinner.setEnabled(false);
        clienteSpinner.setVisibility(View.INVISIBLE);

        frontLayout = (RelativeLayout) findViewById(R.id.parent_menu_front_layout);

		if(hasMenuRight) {
			carrito.setVisibility(View.VISIBLE);
			createRightMenu();
		}
        carrito.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                toggleRightMenu();
            }
        });

		if(hasMenuLeft) {
			createLeftMenu();
			menuFilterIcon.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(menuFilterIcon.isEnabled()) toggleLeftMenu();
                }
            });
		}

        refresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isNetworkAvailable(v.getContext());
            }
        });

        //Crear listener para el boton de log off
		logOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
                logOff(v.getContext());
			}
		});
	}

    protected void logOff(Context mContext) {
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setMessage(mContext.getString(R.string.log_off_confirmation))
                .setTitle(mContext.getString(R.string.cerrar_sesion))
                .setPositiveButton(mContext.getString(R.string.dialog_continuar_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        ParseUser.logOut();
                        dispatchActivity(LoginActivity.class, null, true);
                    }
                })
                .setNegativeButton(mContext.getString(R.string.dialog_cancelar_button), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void reloadApp() {
        if(GrupoIdea.hasInternet){
            ParseQuery.clearAllCachedResults();
            finish();
            startActivity(getIntent());
            if(this instanceof DashboardActivity) getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            refresh.setClickable(false);
        }
    }

    @Override
	public void setContentView(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		if(inflateView != null) {
			parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_inflate_layout);
			if(parentInflater != null) parentInflater.addView(inflateView);
		}
	}
	
	/** Permite obtener el ViewGroup asociado al menu lateral derecho.
	 *  @return ViewGroup que contiene el menu lateral derecho, si este no esta definido retornara null*/
	protected ViewGroup getMenuRight() {
		return menuRight;
	}
	
	/** Metodo que puede ser reimplementado en los hijos, permite asignar un layout al menu lateral derecho. Por defecto
	 *  se asigna el layout del carrito.*/
	protected void createRightMenu() {
		setRightMenuLayout(R.layout.component_carrito_layout);
	}
	
	/** Metodo que puede ser reimplementado en los hijos, permite asignar un layout al menu lateral izquierdo. Por defecto
	 *  se asigna el menu de categorias.*/
	protected void createLeftMenu() {
		setLeftMenuLayout(R.layout.component_filtro_menu_layout);
	}
	
	/** Permite setear el ViewGroup que se utlizara como menu lateral derecho*/
	protected void setRightMenuLayout(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		if(inflateView != null) {
			menuRight = (ViewGroup) inflateView;
			parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_right_layout);
			if(parentInflater != null) {
				parentInflater.addView(inflateView);
			}
		}
	}
	
	/** Permite setear el ViewGroup que se utlizara como menu lateral derecho*/
	protected void setLeftMenuLayout(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		if(inflateView != null) {
			setMenuLeft((ViewGroup) inflateView);
			parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_left_layout);
			if(parentInflater != null) {
				parentInflater.addView(inflateView);
			}
		}
	}
	
	/** Permite obtener el ViewGroup que se utilizara como menu lateral derecho*/
	public ViewGroup getRightMenuLayout() {
		return menuRight;
	}
	
	/** Permite obtener el ViewGroup que se utilizara como menu lateral derecho*/
	public ViewGroup getLeftMenuLayout() {
		return getMenuLeft();
	}
	
	/** Permite mostrar una cadena de texto en el centro del menu.
	 *  @param titulo Cadena de texto que representa informacion relevante al Activity.*/
	protected void setMenuTittle(String titulo) {
		if(menuTituloTextView != null && titulo != null) {
			menuTituloTextView.setText(titulo);
		}
	}
	
	/** Permite mostrar/ocultar el menu que se encuentra a la izquierda del aplicativo.*/
	public void toggleLeftMenu() {
		if(!menuLeftShowed) {
			showLeftMenu();
		} else {
			hideMenuLeft();
		}
	}
	
	/** Permite mostrar el menu que se encuentra a la izquierda del aplicativo.*/
	public void showLeftMenu() {
		if(frontLayout != null) {
			frontLayout.scrollTo(dpToPx(-300), frontLayout.getScrollY());
			menuLeftShowed = true;
		}
	}
	
	/** Permite ocultar el menu que se encuentra a la izquierda del aplicativo.*/
	public void hideMenuLeft() {
		if(frontLayout != null) {
			frontLayout.scrollTo(0, frontLayout.getScrollY());
			menuLeftShowed = false;
		}
	}
	
	/** Permite determinar si el menu que se encuentra a la izquierda del aplicativo es visible actualmente*/
	protected Boolean isMenuLeftShowed() {
		return menuLeftShowed;
	}
	
	/** Permite mostrar/ocultar el menu que se encuentra a la derecha del aplicativo.*/
	public void toggleRightMenu() {
		if(!menuRightShowed) {
			showRightMenu();
		} else {
			hideMenuRight();
		}
	}
	
	/** Permite mostrar el menu que se encuentra a la derecha del aplicativo.*/
	public void showRightMenu() {
		if(frontLayout != null) {
			frontLayout.scrollTo(dpToPx(300), frontLayout.getScrollY());
			menuRightShowed = true;
		}
	}
	
	/** Permite ocultar el menu que se encuentra a la derecha del aplicativo.*/
	public void hideMenuRight() {
		if(frontLayout != null) {
			frontLayout.scrollTo(0, frontLayout.getScrollY());
			menuRightShowed = false;
		}
	}
	
	/** Permite determinar si el menu que se encuentra a la derecha del aplicativo es visible actualmente*/
	protected Boolean isMenuRightShowed() {
		return menuRightShowed;
	}

    /**
     * Obtiene los Clientes desde Parse y los almacena en <code>Application</code>
     * @return <code>ArrayList</code> de clientes obtenidos desde Parse insanciados como <code>Cliente</code>
     */
    public ArrayList<Cliente> getClientesFromParse(){
        clientes = new ArrayList<Cliente>();
        clientesAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_spinner_item);
        ParseQuery query = new ParseQuery("Cliente");
        query.setLimit(QUERY_LIMIT);
        query.setCachePolicy(getParseCachePolicy());
        query.findInBackground(new FindCallback<ParseObject>() {
            public void done(List<ParseObject> listaClientes, ParseException e) {
                Cliente cliente;
                if (e == null) {
                    Log.d(TAG, "Obtenidos " + listaClientes.size() + " clientes");
                    for (ParseObject parseObj:listaClientes){
                        cliente = new Cliente(parseObj.getString("nombre"));
                        cliente.setCodigo(parseObj.getString("codigo"));
                        cliente.setDescuento(parseObj.getDouble("descuentoComercial"));
                        cliente.setParseId(parseObj.getObjectId());
                        cliente.setClienteParse(parseObj);
                        //Almacenar clientes directamente en el clientesAdapter
                        clientes.add(cliente);
                        clientesAdapter.add(cliente.getNombre());
                    }
                } else {
                    Log.d(TAG, "Error: " + e.getMessage());
                }
            }
        });
        return clientes;
    }

    public ViewGroup getMenuLeft() {
        return menuLeft;
    }

    public void setMenuLeft(ViewGroup menuLeft) {
        this.menuLeft = menuLeft;
    }

    public void isNetworkAvailable(Context context){
        GrupoIdea.hasInternet = false;
        boolean tempInfo = false;
        ConnectivityManager connManag = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] info = connManag.getAllNetworkInfo();
        if (info != null)
            for (NetworkInfo anInfo : info)
                if (anInfo.getState() == NetworkInfo.State.CONNECTED){
                    tempInfo = true;
                }

        if(tempInfo){
            InternetCheckTask task = new InternetCheckTask();
            task.execute();
        }else{
            Toast.makeText(getBaseContext(), getString(R.string.no_internet_message_refresh), Toast.LENGTH_LONG).show();
            Log.d("ConnectionResult", "NetworkInfo false");
        }
    }

    public class InternetCheckTask extends AsyncTask<Void, Void, Boolean> {
        Exception bla;

        @Override
        protected Boolean doInBackground(Void... voids) {
            boolean result = false;
            try{
                HttpGet request = new HttpGet("http://www.parse.com");
                HttpParams httpParameters = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
                HttpClient httpClient = new DefaultHttpClient(httpParameters);
                HttpResponse response = httpClient.execute(request);

                int status = response.getStatusLine().getStatusCode();

                if (status == HttpStatus.SC_OK) result = true;

            }catch (Exception e){
                bla = e;
                result = false;
            }

            return result;
        }


        @Override
        protected void onPostExecute(Boolean result){
            GrupoIdea.hasInternet = result;
            Log.d("ConnectionResult", "Resultado de conex: "+result.toString());
            if(bla != null) bla.printStackTrace();
            if(result) reloadApp();
            else Toast.makeText(getBaseContext(), getString(R.string.no_internet_message_refresh), Toast.LENGTH_LONG).show();
        }
    }
}
