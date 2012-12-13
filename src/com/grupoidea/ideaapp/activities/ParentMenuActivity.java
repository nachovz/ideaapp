package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.parse.ParseUser;

public abstract class ParentMenuActivity extends ParentActivity {
	private TextView menuTituloTextView; 
	private ImageView logOff;
	private ImageView menuIcon;
	
	public ParentMenuActivity(boolean autoLoad, boolean hasCache) {
		super(autoLoad, hasCache);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
		
		menuTituloTextView = (TextView) findViewById(R.id.menu_titulo_text_view);
		logOff = (ImageView) findViewById(R.id.menu_logoff_image_view);
		menuIcon = (ImageView) findViewById(R.id.menu_icon_image_view);
		
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
