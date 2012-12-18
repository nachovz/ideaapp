package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.parse.ParseUser;

public abstract class ParentMenuActivity extends ParentActivity {
	private TextView menuTituloTextView; 
	private ImageView logOff;
	private ImageView menuIcon;
	private ImageView carrito;
	private RelativeLayout frontLayout;
	
	private ViewGroup menuRight;
	
	private Boolean menuRightShowed;
	private Boolean hasMenuRight;
	
	public ParentMenuActivity(boolean autoLoad, boolean hasCache, boolean hasMenuRight) {
		super(autoLoad, hasCache);
		this.hasMenuRight = hasMenuRight;
		menuRight = null;
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
		
		menuRightShowed = false;
		
		menuTituloTextView = (TextView) findViewById(R.id.menu_titulo_text_view);
		logOff = (ImageView) findViewById(R.id.menu_logoff_image_view);
		menuIcon = (ImageView) findViewById(R.id.menu_icon_image_view);
		carrito = (ImageView) findViewById(R.id.menu_carrito_image_view);
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
		
		menuIcon.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
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
	
	/** Permite setear el ViewGroup que se utlizara como menu lateral derecho*/
	private void setRightMenuLayout(int layoutResId) {
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
	
	/** Permite mostrar una cadena de texto en el centro del menú.
	 *  @param titulo Cadena de texto que representa informacion relevante al Activity.*/
	protected void setMenuTittle(String titulo) {
		if(menuTituloTextView != null && titulo != null) {
			menuTituloTextView.setText(titulo);
		}
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
