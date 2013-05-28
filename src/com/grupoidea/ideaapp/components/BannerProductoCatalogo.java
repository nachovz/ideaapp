package com.grupoidea.ideaapp.components;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Debug;
import android.util.Log;
import android.view.*;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;

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
	
	/** Permite apagar la bandera isInCarrito de un producto en el carrito**/
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
	
	/** Permite encender la bandera isInCarrito de un producto en el carrito**/
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
				/** Mostrar el boton del carrito como seleccionado si el producto se encuentra dentro del mismo*/
				imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito_selected));
			}else{
                /** No mostrar el boton de carrito seleccionado*/
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

            /**Crear Listener para cuando el producto es agregado al carrito, agregar el producto al carrito y mostrar carrito*/
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

            /** Menu de producto*/
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
					}

                    /** Listar descuentos en el menu de producto*/
//					if (producto.getCountDescuentos()>0){
                        TextView descProd = new TextView(mContext);
                        descProd.setHeight(40);
                        descProd.setWidth(LayoutParams.MATCH_PARENT);
                        descProd.setTextColor(Color.parseColor("#646464"));
                        descProd.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC);
                        descProd.setGravity(Gravity.CENTER);
                        descProd.setBackgroundResource(R.drawable.menu_producto_selector);
                        descProd.setText("Desc1");
                        menu.addView(descProd, 1);
                        /*for (int i = 0; i < producto.getCountDescuentos(); i++) {
                            descProd.setText(producto.getStringDescuento(i));
                            menu.addView(descProd);
                        }*/
//                  }

				}
			});

            TextView prodMenuText = (TextView) view.findViewById(R.id.banner_producto_menu_item_descuento_manual);
            prodMenuText.setOnClickListener(new View.OnClickListener() {
                LayoutParams layoutParams;
                @Override
                public void onClick(View view) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
                    builder.setTitle(R.string.titulo_descuento_manual);

                    LayoutInflater inflater = LayoutInflater.from(mContext);
                    builder.setView(inflater.inflate(R.layout.producto_descuento_manual_popup, null));

                    final EditText descuentoManual = (EditText) view.findViewById(R.id.valor_descuento_manual);

                    builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // Click en Aceptar
                            try{
                                double valorDescuento = Double.parseDouble(descuentoManual.getText().toString());
                                if(valorDescuento < (Double) 100.0 && valorDescuento > (Double) 0.0){
    //                                producto.setDescuento(valorDescuento);
                                    Toast.makeText(mContext, descuentoManual.getText().toString(), Toast.LENGTH_SHORT).show();
                                    Toast.makeText(mContext, "Descuento manual establecido en "+ descuentoManual.getText().toString(), 3000).show();
                                }else{
                                    Toast.makeText(mContext, "Valor del descuento no esta en un rango valido", 3000).show();
                                }
                            }catch(Exception e){
                                Log.d("idea", e.toString());

                            }
                        }
                    });
                    builder.setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            //Click en Cancelar
                            Toast.makeText(mContext, "Ajuste manual de descuento cancelado por el usuario", 3000);
                            dialog.cancel();
                        }
                    });

                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            });

		}
		return view;
	}
}
