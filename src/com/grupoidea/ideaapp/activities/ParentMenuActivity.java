package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;

public abstract class ParentMenuActivity extends ParentActivity {
	private TextView menuTituloTextView; 
	
	public ParentMenuActivity(boolean autoLoad, boolean hasCache) {
		super(autoLoad, hasCache);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
		
		menuTituloTextView = (TextView) findViewById(R.id.menu_titulo_text_view);
	}

	@Override
	public void setContentView(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_inflate_layout);
		parentInflater.addView(inflateView);
	}
	
	/** Permite mostrar una cadena de texto en el centro del menú.
	 *  @param titulo Cadena de texto que representa informacion relevante al Activity.*/
	protected void setMenuTittle(String titulo) {
		if(menuTituloTextView != null && titulo != null) {
			menuTituloTextView.setText(titulo);
		}
	}
}
