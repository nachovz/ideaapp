package com.grupoidea.ideaapp.components;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.ParentMenuActivity;
import com.grupoidea.ideaapp.models.Producto;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends BaseAdapter {
	/** Contexto actual de la aplicacion*/
	private Context context;
	/** Objeto que contiene el adaptador del carrito*/
	private BannerProductoCarrito adapterCarrito;
	/** Arreglo de productos.*/
	private ArrayList<Producto> productos;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param productos Arreglo de productos. */
	public BannerProductoCatalogo(Context context, ArrayList<Producto> productos, BannerProductoCarrito adapterCarrito) {
		this.context = context;
		this.productos = productos;
		this.adapterCarrito = adapterCarrito;
	}
	
	@Override
	public int getCount() {
		return productos.size();
	}

	@Override
	public Object getItem(int position) {
		return productos.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		View view = null;
		TextView textView;
		ImageView imageView;
		LayoutInflater inflater;
		Producto producto;
		
		producto = (Producto) getItem(position);
		
		if (convertView == null) {  
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_catalogo_layout, null);
		} else {
        	view = convertView;
        }
			
		if(producto != null) {
			if(producto.getNombre() != null) {
				textView = (TextView) view.findViewById(R.id.banner_producto_titulo_text_view);
				textView.setText(producto.getNombre());
			}
			
			if(producto.getStringPrecio() != null) {
				textView = (TextView) view.findViewById(R.id.banner_producto_precio_text_view);
				textView.setText(producto.getStringPrecio());
			}
			
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_producto_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}
			
			imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					((ParentMenuActivity) context).showRightMenu();
					adapterCarrito.getCarrito().addProducto(productos.get(position));
					adapterCarrito.notifyDataSetChanged();
				}
			});
		}
		return view;
	}
}
