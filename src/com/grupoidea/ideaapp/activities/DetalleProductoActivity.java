package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;

public class DetalleProductoActivity extends ParentMenuActivity {

	public DetalleProductoActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detalle_producto_layout);
		
		RelativeLayout parentInflater;
		View inflateView;
		
		inflateView = getLayoutInflater().inflate(R.layout.banner_producto_catalogo_layout, null);
		if(inflateView != null) {
			parentInflater = (RelativeLayout) findViewById(R.id.detalle_producto_image_zone);
			if(parentInflater != null) {
				parentInflater.addView(inflateView);
			}
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
