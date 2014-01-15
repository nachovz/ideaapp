package com.grupoidea.ideaapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Producto;

public class DetalleProductoActivity extends ParentMenuActivity {
    protected String TAG = this.getClass().getSimpleName();
	/** Cadena de texto que contiene el codigo del producto a detallar*/
	private String codigo;
	/** Double que contiene el precio producto a detallar*/
	private String precio;
	/** Imagen del producto a detallar*/
	private Bitmap bitmap;

    private GrupoIdea app;

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

		setContentView(R.layout.activity_detalle_producto_layout);
        app = (GrupoIdea) getApplication();
        int position = getIntent().getIntExtra("position", 0);
        Producto prod = app.productos.get(position);

//        Log.d(TAG,"-----------------------------------------------------");
//        Log.d(TAG, "-- Detalle de Producto --");
        //Codigo
        codigo = prod.getCodigo();
//        Log.d(TAG,"Codigo: "+codigo);

        //Marca
        marca = prod.getMarca();
//        Log.d(TAG,"Marca: "+marca);

        //Precio
        precio = prod.getPrecioComercialSinIvaConIvaString();
//        Log.d(TAG,"Precio: "+precio);

        //Imagen
        bitmap = prod.getImagen();

        //Categoria
        categoria = (null != prod.getCategoria().getNombre())? prod.getCategoria().getNombre() : "-";
//        Log.d(TAG,"Categoria: "+categoria);

        //Grupo
        grupo = (null != prod.getNombreGrupoCategorias())? prod.getNombreGrupoCategorias() : "-";
//        Log.d(TAG,"Grupo: "+grupo);

        //Meta
        meta = String.valueOf(prod.getExistencia());
//        Log.d(TAG,"Meta: "+meta);

        //Excedente
        excedente = String.valueOf(prod.getExcedente());
//        Log.d(TAG,"Excedente: "+excedente);

        //Descripcion
        descripcion = (null != prod.getDescripcion())? prod.getDescripcion() : "-";
//        Log.d(TAG,"Descripcion: "+descripcion);

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
            imageView.setImageBitmap(bitmap);

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
