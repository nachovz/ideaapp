package com.grupoidea.ideaapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Producto;

public class DetalleProductoActivity extends ParentMenuActivity {
	/** Cadena de texto que contiene el nombre del producto a detallar*/
	private String nombre;
	/** Double que contiene el precio producto a detallar*/
	private Double precio;
	/** Imagen del producto a detallar*/
	private Bitmap bitmap;
	
	public DetalleProductoActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		RelativeLayout parentInflater;
		View inflateView;
		TextView textView;
		ImageView imageView;
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detalle_producto_layout);
		nombre = getIntent().getExtras().getString("nombre");
		precio = getIntent().getExtras().getDouble("precio");
		bitmap = getIntent().getExtras().getParcelable("bitmap");
		
		inflateView = getLayoutInflater().inflate(R.layout.banner_producto_catalogo_layout, null);
		if(inflateView != null) {
			parentInflater = (RelativeLayout) findViewById(R.id.detalle_producto_image_zone);
			if(parentInflater != null) {
				parentInflater.addView(inflateView);
				
				textView = (TextView) inflateView.findViewById(R.id.banner_producto_titulo_text_view);
				textView.setText(nombre);
				
				textView = (TextView) inflateView.findViewById(R.id.banner_producto_precio_text_view);
				textView.setText(Producto.precioToString(precio));
				
				imageView = (ImageView) inflateView.findViewById(R.id.banner_producto_image_view);
				imageView.setImageBitmap(bitmap);
				//No mostrar los botones de "agregar al carrito" y "menu producto"
				imageView = (ImageView) inflateView.findViewById(R.id.banner_producto_add_carrito_image_view);
				imageView.setVisibility(View.INVISIBLE);
				imageView = (ImageView) inflateView.findViewById(R.id.banner_producto_menu_image_view);
				imageView.setVisibility(View.INVISIBLE);
			}
		}
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
		return null;
	}

}
