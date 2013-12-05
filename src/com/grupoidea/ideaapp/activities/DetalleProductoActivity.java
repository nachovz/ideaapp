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

public class DetalleProductoActivity extends ParentMenuActivity {
	/** Cadena de texto que contiene el codigo del producto a detallar*/
	private String codigo;
	/** Double que contiene el precio producto a detallar*/
	private String precio;
	/** Imagen del producto a detallar*/
	private Bitmap bitmap;

    private String descripcion, excedente, meta, categoria, grupo, marca;
	
	public DetalleProductoActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		RelativeLayout parentInflater;
		TextView textView;
		ImageView imageView;
		
		super.onCreate(savedInstanceState);

		setContentView(R.layout.detalle_producto_layout);
        codigo = getIntent().getExtras().getString("codigo");
        marca = getIntent().getExtras().getString("marca");
		precio = getIntent().getExtras().getString("precio");
        bitmap = getIntent().getExtras().getParcelable("bitmap");
        categoria = getIntent().getExtras().getString("categoria");
        grupo = getIntent().getExtras().getString("grupo");
        meta = getIntent().getExtras().getString("meta");
        excedente = getIntent().getExtras().getString("excedente");
        descripcion = getIntent().getExtras().getString("descripcion");


        parentInflater = (RelativeLayout) findViewById(R.id.detalle_producto_image_zone);
        if(parentInflater != null) {
            //Codigo
            textView = (TextView) findViewById(R.id.nombre_producto_textView);
            textView.setText(codigo);

            //Marca
            textView = (TextView) findViewById(R.id.marca_producto_textView);
            textView.setText(marca);

            //Precio
            textView = (TextView) findViewById(R.id.precio_producto_textView);
            textView.setText(precio);

            //Imagen
            imageView = (ImageView) findViewById(R.id.producto_imageView);
            if(null != bitmap){
                imageView.setImageBitmap(bitmap);
            }else{
                imageView.setImageResource(R.drawable.prod_background);
            }


            //Categoria
            textView = (TextView) findViewById(R.id.categoria_textView);
            textView.setText(categoria);

            //Grupo Categorias
            textView = (TextView) findViewById(R.id.grupo_categorias_textView);
            textView.setText(grupo);

            //Meta
            textView = (TextView) findViewById(R.id.meta_textView);
            textView.setText(meta);

            //Excdente
            textView = (TextView) findViewById(R.id.excedente_textView);
            textView.setText(excedente);

            //Descripcion
            textView = (TextView) findViewById(R.id.descripcion_textView);
            textView.setText(descripcion);

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
