package com.grupoidea.ideaapp.activities;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Producto;

public class DetalleProductoActivity extends ParentMenuActivity {
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

		setContentView(R.layout.detalle_producto_layout);
        app = (GrupoIdea) getApplication();
        int position = getIntent().getIntExtra("position", 0);
        Producto prod = app.productos.get(position);

        Log.d("DEBUG","-----------------------------------------------------");
        Log.d("DEBUG", "-- Detalle de Producto --");
        //Codigo
        codigo = prod.getCodigo();
        Log.d("DEBUG","Codigo: "+codigo);

        //Marca
        marca = prod.getMarca();
        Log.d("DEBUG","Marca: "+marca);

        //Precio
        precio = prod.getPrecioComercialSinIvaConIvaString();
        Log.d("DEBUG","Precio: "+precio);

        //Imagen
        bitmap = prod.getImagen();

        //Categoria
        categoria = (null != prod.getCategoria().getNombre())? prod.getCategoria().getNombre() : "-";
        Log.d("DEBUG","Categoria: "+categoria);

        //Grupo
        grupo = (null != prod.getNombreGrupoCategorias())? prod.getNombreGrupoCategorias() : "-";
        Log.d("DEBUG","Grupo: "+grupo);

        //Meta
        meta = String.valueOf(prod.getExistencia());
        Log.d("DEBUG","Meta: "+meta);

        //Excedente
        excedente = String.valueOf(prod.getExcedente());
        Log.d("DEBUG","Excedente: "+excedente);

        //Descripcion
        descripcion = (null != prod.getDescripcion())? prod.getDescripcion() : "-";
        Log.d("DEBUG","Descripcion: "+descripcion);

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
