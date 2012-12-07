package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.grupoidea.ideaapp.R;

public abstract class ParentMenuActivity extends ParentActivity {
	
	public ParentMenuActivity(boolean autoLoad, boolean hasCache) {
		super(autoLoad, hasCache);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.setContentView(R.layout.parent_menu_layout);
	}

	@Override
	public void setContentView(int layoutResId) {
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(layoutResId, null);
		parentInflater = (RelativeLayout) findViewById(R.id.parent_menu_inflate_layout);
		parentInflater.addView(inflateView);
	}
	
}
