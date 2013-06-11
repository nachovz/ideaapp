package com.grupoidea.ideaapp.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import android.widget.RelativeLayout.LayoutParams;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.DetalleProductoActivity;
import com.grupoidea.ideaapp.models.Producto;

import java.util.ArrayList;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends ParentBannerProducto {
	/** Listado de elementos del carrito*/
	private ListView listCarrito;
	/** Arreglo de productos.*/
	private static ArrayList<Producto> productos;
	/** ViewGroup que permite mostrar el menu del producto.*/
	private LinearLayout menu;
	protected Producto producto;
    protected Double descMan;
	private BannerProductoCatalogo adapter;
	private Context mContext;

    public final static Double MIN_DESC_MAN = 0.0, MAX_DESC_MAN = 100.0;
	
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
		
		producto = (Producto) getItem(position);

        //View previo no existe
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
				/* Mostrar el boton del carrito como seleccionado si el producto se encuentra dentro del mismo*/
				imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito_selected));
			}else{
                /* No mostrar el boton de carrito seleccionado*/
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

            /*Crear Listener para cuando el producto es agregado al carrito, agregar el producto al carrito y mostrar carrito*/
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

            //cargar menu desde layout.xml, actualizar excedentes y agregar descuentos
            menu = new LinearLayout(menuActivity);
			menu = (LinearLayout) view.findViewById(R.id.banner_producto_menu_layout);
            //TODO fix: Listener para cuando se pierda el foco
            menu.setOnFocusChangeListener(new View.OnFocusChangeListener(){
                LayoutParams layoutParams;
                @Override
                public void onFocusChange(View v, boolean hasFocus){
                    if(producto.getIsMenuOpen() && !hasFocus){
                        //El menu esta abierto... cerrarlo
                        if(menu != null) {
                            producto.setIsMenuOpen(false);
                            menu.setVisibility(LinearLayout.VISIBLE);
                            notifyDataSetChanged();
                        }
                        layoutParams = new LayoutParams(40, 40);
                        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
                        v.setLayoutParams(layoutParams);
                    }
                }
            });

            TextView exced = (TextView) menu.getChildAt(menu.getChildCount()-1);
            if (exced == null) {
                throw new AssertionError();
            }
            exced.setText(menuActivity.getText(R.string.excedentes)+String.valueOf(producto.getExistencia()));

            // Listar descuentos en el menu de producto
            ArrayList<String> menu_prod = producto.getDescuentosString();
            if (menu_prod.size()>0 && (menu.getChildCount() != menu_prod.size()+2)){
                for(int i=0, size=menu_prod.size(); i<size; i++) {
                    TextView descProd = createProductoMenuTextView(menuActivity, menu_prod.get(i));
                    menu.addView(descProd, menu.getChildCount()-2);
                }
            }

			if(producto.getIsMenuOpen()) {
				menu.setVisibility(LinearLayout.VISIBLE);
			} else {
				menu.setVisibility(LinearLayout.GONE);
			}

            // Imagen de menu emergente de producto
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
				}
			});

            // Boton de descuento manual
            TextView prodMenuText = (TextView) view.findViewById(R.id.banner_producto_menu_item_descuento_manual);
            prodMenuText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    setValorDescuentoManual(producto);
                }
            });

		}
		return view;
	}

    /**
     * Funcion que retorna un TextView configurado para el menu de producto, con el contexto y texto especificado
     * @param mContext Contexto del menu
     * @param text Texto a mostrar por el TextView
     * @return TextView configurado
     */
    public TextView createProductoMenuTextView(Context mContext, String text){
        TextView descProd = new TextView(mContext);
        descProd.setHeight(40);
        descProd.setWidth(LayoutParams.MATCH_PARENT);
        descProd.setTextColor(Color.parseColor("#646464"));
        descProd.setTypeface(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC);
        descProd.setGravity(Gravity.CENTER);
        descProd.setBackgroundResource(R.drawable.menu_producto_selector);
        descProd.setText(text);

        return descProd;
    }

    /** Proceso que establece el valor del descuento manual para el producto seleccionado*/
    public void setValorDescuentoManual(final Producto producto){
        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        final EditText input = new EditText(mContext);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);    //seteandolo para numeros solamente
        input.setMaxLines(1);
        input.setHint(R.string.descuento_manual);
        builder.setView(input);
        TextView title = new TextView(mContext);
        title.setText(R.string.titulo_descuento_manual);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Valor de Descuento ingresado
                Double valor = Double.parseDouble(input.getText().toString());
                if (valor >= MIN_DESC_MAN && valor <= MAX_DESC_MAN) {
                    Log.d("DEBUG", valor.toString());
                    producto.setDescuento(valor);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(mContext, "Porcentaje de descuento manual asignado", 3000).show();
                } else {
                    Toast.makeText(mContext, "Porcentaje de descuento manual no valido", 3000).show();
                    Log.d("DEBUG", "porcentaje no valido");
                }
            }
        })
        .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dialogo cancelado
                Toast.makeText(mContext, "Descuento manual no establecido", 3000).show();
            }
        });
        builder.create();
        builder.show();
    }

}
