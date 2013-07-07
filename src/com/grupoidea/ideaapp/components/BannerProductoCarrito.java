package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.ParentMenuActivity;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Producto;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCarrito extends ParentBannerProducto{
	/** Objeto que contiene la logica del carrito de productos*/
	private Carrito carrito;
	/** Objeto que contiene un producto de manera temporal*/
	private Producto producto;
	/** Objeto que contiene el adaptador actual*/
	private BannerProductoCarrito carritoAdapter;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param carrito Arreglo de productos. */
	public BannerProductoCarrito(Context context, Carrito carrito) {
		super(context);
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
        //TODO recalcular descuentos
		
		if (convertView == null) {  
			inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_carrito_layout, null);
		} else {
			view = convertView;
		}
			
		if(producto != null) {
            //cantidad de productos de esta clase
			editText = (EditText) view.findViewById(R.id.banner_carrito_cantidad);
			if(editText != null) {
				editText.setText(String.valueOf(producto.getCantidad()));
			}

            //monto total de productos de esta clase
			textView = (TextView) view.findViewById(R.id.banner_carrito_total_text_view);
			if(textView != null) {
				textView.setText(producto.getStringPrecioComercialTotal());
			}

            //Aumentar cantidad
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_mas_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					TextView textView;
					RelativeLayout layout;
					ParentMenuActivity menuActivity;
					producto = (Producto) getItem(position);
					producto.addCantidad();
                    //TODO recalcular descuentos

					carritoAdapter.notifyDataSetChanged();
					//Calcula el total del carrito
					setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
				}
			});

            //Disminuir cantidad
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_menos_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					producto = (Producto) getItem(position);
					producto.substractCantidad();
                    //TODO recalcular descuentos
					carritoAdapter.notifyDataSetChanged();
					//Calcula el total del carrito
					setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
				}
			});

            //Eliminar producto del carrito
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_eliminar_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					CatalogoActivity catalogoActivity;
					Producto producto;
					
					producto = (Producto) getItem(position);
                    producto.setCantidad(1);
					catalogoActivity = (CatalogoActivity) menuActivity;
					catalogoActivity.adapterCatalogo.removeProductoFlagCarrito(producto);
					catalogoActivity.adapterCatalogo.notifyDataSetChanged();
					
					carrito.removeProducto(position);
					carritoAdapter.notifyDataSetChanged();
					//Calcula el total del carrito
					setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
				}
			});

            //Nombre producto
			if(producto.getNombre() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_titulo_text_view);
				textView.setText(producto.getNombre());
			}

            //Precio producto
			if(producto.getStringPrecioComercial() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_precio_text_view);
				textView.setText(producto.getPrecioComercialSinIvaConIvaString());
			}

            //imagen del producto
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}

            //descuento aplicado
            if(producto.getDescuentoAplicado() !=0.0){
                RelativeLayout rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
                rlDesc.setVisibility(View.VISIBLE);
                TextView porcDescTextView = (TextView) view.findViewById(R.id.descuento_textView);
                porcDescTextView.setText(producto.getDescuentoAplicadoPorcString());
            }else{
                RelativeLayout rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
                rlDesc.setVisibility(View.INVISIBLE);
            }
		}
		return view;
	}
	
	public Carrito getCarrito() {
		return carrito;
	}

}
