package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Producto;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCarrito extends BaseAdapter{
	/** Contexto actual de la aplicacion*/
	private Context context;
	/** Objeto que contiene la logica del carrito de productos*/
	private Carrito carrito;
	/** Objeto que contiene un producto de manera temporal*/
	private Producto producto;
	/** Objeto que contiene el adaptador actual*/
	private BannerProductoCarrito carritoAdapter;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param productos Arreglo de productos. */
	public BannerProductoCarrito(Context context, Carrito carrito) {
		this.context = context;
		this.carrito = carrito;
		carritoAdapter = this;
	}

	@Override
	public int getCount() {
		return carrito.getProductos().size();
	}

	@Override
	public Object getItem(int position) {
		return carrito.getProductos().get(position);
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
		EditText editText;
		
		producto = (Producto) getItem(position);
		
		if (convertView == null) {  
			inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_carrito_layout, null);
		} else {
			view = convertView;
		}
			
		if(producto != null) {
			editText = (EditText) view.findViewById(R.id.banner_carrito_cantidad);
			if(editText != null) {
				editText.setText(String.valueOf(producto.getCantidad()));
			}

			textView = (TextView) view.findViewById(R.id.banner_carrito_total_text_view);
			if(textView != null) {
				textView.setText(producto.getStringPrecioTotal());
			}
			
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_mas_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					producto = (Producto) getItem(position);
					producto.addCantidad();
					carritoAdapter.notifyDataSetChanged();
				}
			});
			
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_menos_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					producto = (Producto) getItem(position);
					producto.substractCantidad();
					carritoAdapter.notifyDataSetChanged();
				}
			});
			
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_eliminar_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					carrito.removeProducto(position);
					carritoAdapter.notifyDataSetChanged();
				}
			});
			
			if(producto.getNombre() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_titulo_text_view);
				textView.setText(producto.getNombre());
			}
			
			if(producto.getStringPrecio() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_precio_text_view);
				textView.setText(producto.getStringPrecio());
			}
			
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}
		}
		return view;
	}
	
	public Carrito getCarrito() {
		return carrito;
	}

}
