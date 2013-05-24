package com.grupoidea.ideaapp.components;

import java.util.ArrayList;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DetalleProductoActivity;
import com.grupoidea.ideaapp.models.Producto;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends ParentBannerProducto {
	/** Listado de elementos del carrito*/
	private ListView listCarrito;
	/** Arreglo de productos.*/
	private ArrayList<Producto> productos;
	/** ViewGroup que permite mostrar el menu del producto.*/
	private LinearLayout menu;
	protected Producto producto;
	private BannerProductoCatalogo adapter;
	private Context mContext;
	
	protected AsyncTask<Object, Object, Object> tarea;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param productos Arreglo de productos. */
	public BannerProductoCatalogo(Context context, ArrayList<Producto> productos, ListView listCarrito) {
		super(context);
		this.productos = productos;
		this.listCarrito = listCarrito;
		this.adapter = this;
		this.mContext = context;
		menu = null;
	}
	
	/** Permite obtener la posicion de un producto en el ArrayList de productos perteneciente al catalogo**/
	public void removeProductoFlagCarrito(Producto producto) {
		Producto prod;
		if(producto != null) {
			for (int i = 0; i < productos.size(); i++) {
				prod = productos.get(i);
				if(producto.getId() == prod.getId()) {
					prod.setIsInCarrito(false);
				}
			}
		}
	}
	
	/** Permite obtener la posicion de un producto en el ArrayList de productos perteneciente al catalogo**/
	public void addProductoFlagCarrito(Producto producto) {
		Producto prod;
		if(producto != null) {
			for (int i = 0; i < productos.size(); i++) {
				prod = productos.get(i);
				if(producto.getId() == prod.getId()) {
					prod.setIsInCarrito(true);
				}
			}
		}
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
		RelativeLayout relativeLayout;
		LayoutInflater inflater;
//		final Producto producto;
		
//		final ListView listView = (ListView) parent;
		
		producto = (Producto) getItem(position);
		
		if (convertView == null) {  
			inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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
			
			if(producto.getIsInCarrito()) {
				//textView = (TextView) view.findViewById(R.id.banner_producto_titulo_text_view);
				//textView.setText("AGREGADO!");
				imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito_selected));
			}else{
				imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito));
			}
			
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_producto_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}
			
			//Crear comportamiento de click al articulo = despachar al activity de detalle de producto.
			imageView = (ImageView) view.findViewById(R.id.banner_producto_tittle_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Producto producto;
					producto = productos.get(position);
					
					Bundle extras = new Bundle();
					extras.putString("nombre",producto.getNombre());
					extras.putDouble("precio", producto.getPrecio());
					extras.putParcelable("bitmap", producto.getImagen());
					menuActivity.dispatchActivity(DetalleProductoActivity.class, extras, false);
				}
			});
			
//			relativeLayout = (RelativeLayout) view.findViewById(R.id.banner_producto_box);
//			relativeLayout.setOnClickListener(new OnClickListener() {
//				@Override
//				public void onClick(View v) {
//					Producto producto;
//					producto = productos.get(position);
//					
//					Bundle extras = new Bundle();
//					extras.putString("nombre",producto.getNombre());
//					extras.putDouble("precio", producto.getPrecio());
//					extras.putParcelable("bitmap", producto.getImagen());
//					menuActivity.dispatchActivity(DetalleProductoActivity.class, extras, false);
//				}
//			});
			
			imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					final int index;
					Producto producto;
					BannerProductoCarrito adapterCarrito;
					
					//Muestra el carrito de compras.
					showCarrito();
					//Agrega el producto clickeado al carrito de compras.
					producto = productos.get(position);
					
					addProductoFlagCarrito(producto);
					adapter.notifyDataSetChanged();
					adapterCarrito = (BannerProductoCarrito) listCarrito.getAdapter();
					adapterCarrito.getCarrito().addProducto(producto);
					//Realiza el scroll al elemento agregado o incrementado.
					index = adapterCarrito.getCarrito().findProductoIndex(producto.getId());
					adapterCarrito.notifyDataSetChanged();
		        	
//					tarea = new AsyncTask<Object, Object, Object>() {
//						RelativeLayout relativeLayout;
//						@Override
//						protected void onPreExecute() {
//							relativeLayout = (RelativeLayout) listCarrito.getChildAt(index);
//							if(relativeLayout != null) {
//								relativeLayout.setBackgroundColor(0xFF00FF00);
//							}
//						}
//						@Override
//						protected Object doInBackground(Object... params) {
//							try {
//								Thread.sleep(3000);
//							} catch (InterruptedException e) {
//								e.printStackTrace();
//							}
//							return null;
//						}
//						@Override
//						protected void onPostExecute(Object result) {
//							if(relativeLayout != null) {
//								relativeLayout.setBackgroundColor(0x00000000);
//							}
//						}
//					};
					
					listCarrito.post(new Runnable() {
				        public void run() {
				        	listCarrito.smoothScrollToPosition(index);
//				        	tarea.execute();
				        }
				    });
					
					
					
					
					//Calcula el total del carrito
					setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
				}
			});
			
			menu = (LinearLayout) view.findViewById(R.id.banner_producto_menu_layout);
			if(producto.getIsMenuOpen()) {
				menu.setVisibility(LinearLayout.VISIBLE);
			} else {
				menu.setVisibility(LinearLayout.GONE);
			}
			
			imageView = (ImageView) view.findViewById(R.id.banner_producto_menu_image_view);
			imageView.setOnClickListener(new View.OnClickListener() {
				LayoutParams layoutParams;
				@Override
				public void onClick(View view) {
					Producto producto;
					producto = productos.get(position);
					
					if(producto.getIsMenuOpen()) {
						//El menu esta abierto... cerrarlo
						if(menu != null) {
							producto.setIsMenuOpen(false);
							notifyDataSetChanged();
						}
						layoutParams = new LayoutParams(40, 40);
						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
						view.setLayoutParams(layoutParams);
					} else {
						//El menu esta cerrado... abrirlo
						if(menu != null) {
							producto.setIsMenuOpen(true);
							notifyDataSetChanged();
						}
						layoutParams = new LayoutParams(180, 40);
						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
						view.setLayoutParams(layoutParams);
						
						/*if (producto.getCountDescuentos()>0){
                            TextView descuentoText = new TextView(mContext);
                            for (int i = 0; i < producto.getCountDescuentos(); i++) {
                                descuentoText.setText(producto.getStringDescuento(i));
                                menu.addView(descuentoText);
                            }
                        }*/
					}
				}
			});
		}
		return view;
	}
}
