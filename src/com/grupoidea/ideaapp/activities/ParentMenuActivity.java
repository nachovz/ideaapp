package com.grupoidea.ideaapp.activities;

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

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.parse.*;

import java.util.ArrayList;
import java.util.List;

public abstract class ParentMenuActivity extends ParentActivity {
	private TextView menuTituloTextView; 
	private ImageView logOff;
	private ImageView menuIcon;
	private ImageView carrito;
	private Spinner clienteSpinner;
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
		menuLeft = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
		
		setParentLayoutVisibility(View.GONE);
		
		menuRightShowed = false;
		menuLeftShowed = false;
		
		menuTituloTextView = (TextView) findViewById(R.id.menu_titulo_text_view);
		logOff = (ImageView) findViewById(R.id.menu_logoff_image_view);
		menuIcon = (ImageView) findViewById(R.id.menu_icon_image_view);
		carrito = (ImageView) findViewById(R.id.menu_carrito_image_view);
		clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);

        //Hacer query de la tabla Cliente en Parse
        ParseQuery query = new ParseQuery("Cliente");
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
        query.findInBackground(new FindCallback() {
            public void done(List<ParseObject> listaClientes, ParseException e) {
                String cliente;
                if (e == null) {
                    Log.d("clientes", "Obtenidos " + listaClientes.size() + " clientes");
                    for (ParseObject parseObj:listaClientes){
                        cliente = parseObj.getString("nombre");
                        //Almacenar clientes directamente en el adapter
                        adapter.add(cliente);
                    }
                } else {
                    Log.d("clientes", "Error: " + e.getMessage());
                }
            }
        });

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		clienteSpinner.setAdapter(adapter);
		frontLayout = (RelativeLayout) findViewById(R.id.parent_menu_front_layout);
		
		if(hasMenuRight) {
			carrito.setVisibility(View.VISIBLE);
			createRightMenu();
		}
		
		if(hasMenuLeft) {
			createLeftMenu();
			menuIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					toggleLeftMenu();
				}
			});
		} else {
			menuIcon.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					onBackPressed();
				}
			});
		}
		
		carrito.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				toggleRightMenu();
			}
		});
		
		logOff.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				ParseUser.logOut();
				dispatchActivity(LoginActivity.class, null, true);
			}
		});
	}

	@Override
	public void setContentView(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		if(inflateView != null) {
			parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_inflate_layout);
			if(parentInflater != null) {
				parentInflater.addView(inflateView);
			}
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
		setRightMenuLayout(R.layout.carrito_layout);
	}
	
	/** Metodo que puede ser reimplementado en los hijos, permite asignar un layout al menu lateral izquierdo. Por defecto
	 *  se asigna el menu de categorias.*/
	protected void createLeftMenu() {
		setLeftMenuLayout(R.layout.menu_layout);
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
			menuLeft = (ViewGroup) inflateView;
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
		return menuLeft;
	}
	
	/** Permite mostrar una cadena de texto en el centro del menu.
	 *  @param titulo Cadena de texto que representa informacion relevante al Activity.*/
	protected void setMenuTittle(String titulo) {
		if(menuTituloTextView != null && titulo != null) {
			menuTituloTextView.setText(titulo);
			clienteSpinner.setEnabled(false);
			clienteSpinner.setVisibility(View.INVISIBLE);
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
}
