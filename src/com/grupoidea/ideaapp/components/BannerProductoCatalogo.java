package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DetalleProductoActivity;
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Producto;

import java.util.ArrayList;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends ParentBannerProducto {
	/** Listado de elementos del carrito*/
	private ListView listCarrito;
	/** Arreglo de productos.*/
	private ArrayList<Producto> productos;
    private Catalogo catalogo;
	/** ViewGroup que permite mostrar el menu del producto.*/
    private LinearLayout menu;
    private PopupMenu popup;
    private ScrollView menuScroll;
	protected Producto producto;
	private BannerProductoCatalogo catalogoAdapter;
	private Context mContext;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param catalogo Arreglo de productos. */
//	public BannerProductoCatalogo(Context context, ArrayList<Producto> productos, ListView listCarrito) {
    public BannerProductoCatalogo(Context context, Catalogo catalogo, ListView listCarrito) {
		super(context);
//		this.productos = productos;
        this.catalogo = catalogo;
		this.listCarrito = listCarrito;
		this.catalogoAdapter = this;
		this.mContext = context;
		menu = null;
	}
	
	/** Permite apagar la bandera isInCarrito de un producto en el carrito**/
	public void removeProductoFlagCarrito(Producto producto) {
		Producto prod;
		if(producto != null) {
			for (int i = 0; i < catalogo.getProductos().size(); i++) {
				prod = catalogo.getProductos().get(i);
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
			for (int i = 0; i < catalogo.getProductos().size(); i++) {
				prod = catalogo.getProductos().get(i);
				if(producto.getId() == prod.getId()) {
					prod.setIsInCarrito(true);
				}
			}
		}
	}
	
	@Override
	public int getCount() {
		return catalogo.getProductosCatalogo().size();
	}

	@Override
	public Object getItem(int position) {
		return catalogo.getProductosCatalogo().get(position);
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

        if(producto != null && producto.getIsInCatalogo()) {
            view.setVisibility(View.VISIBLE);
            //Nombre producto
			if(producto.getNombre() != null) {
				textView = (TextView) view.findViewById(R.id.banner_producto_titulo_text_view);
				textView.setText(producto.getNombre());
			}

            //Marca producto
            if(producto.getMarca() != null) {
                textView = (TextView) view.findViewById(R.id.banner_producto_marca_textView);
                textView.setText(producto.getMarca());
            }

            //Precio producto
			if(producto.getStringPrecioComercial() != null) {
				textView = (TextView) view.findViewById(R.id.banner_producto_precio_text_view);
				textView.setText(producto.getPrecioComercialSinIvaConIvaString());
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

            //Cargar Imagen
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_producto_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}else{
                imageView = (ImageView) view.findViewById(R.id.banner_producto_image_view);
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prod_background));
            }
			
			//Crear comportamiento de click al articulo = despachar al activity de detalle de producto.
			imageView = (ImageView) view.findViewById(R.id.banner_producto_tittle_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Producto producto;
					producto = catalogo.getProductosCatalogo().get(position);
					
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

					//Agrega el producto clickeado al carrito de compras.
					producto = catalogo.getProductosCatalogo().get(position);

                    if(producto.getExistencia()+producto.getExcedente() > 0){
                        //Muestra el carrito de compras.
                        showCarrito();
                        producto.setIsInCarrito(true);
                        catalogoAdapter.notifyDataSetChanged();
                        adapterCarrito = (BannerProductoCarrito) listCarrito.getAdapter();
                        adapterCarrito.getCarrito().addProducto(producto);
                        //Realiza el scroll al elemento agregado o incrementado.
                        index = adapterCarrito.getCarrito().findProductoIndexById(producto.getId());
                        adapterCarrito.notifyDataSetChanged();

                        listCarrito.post(new Runnable() {
                            public void run() {
                                listCarrito.smoothScrollToPosition(index);
    //				        	tarea.execute();
                            }
                        });

                        //Calcula el total del carrito
                        setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                    }else{
                        Toast.makeText(mContext, "No hay disponibilidad del producto", 3000).show();
                    }
				}
			});

            //cargar menu desde layout.xml, actualizar excedentes y agregar descuentos
			menu = (LinearLayout) view.findViewById(R.id.banner_producto_menu_layout);
            menu.setTag(producto);

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

            //Actualizar Existencia
            TextView exced = (TextView) menu.getChildAt(menu.getChildCount()-2);
            exced.setText(menuActivity.getText(R.string.meta_restante)+" "+String.valueOf(producto.getExistencia()));

            exced = (TextView) menu.getChildAt(menu.getChildCount()-1);
            exced.setText(menuActivity.getText(R.string.excedentes)+" "+String.valueOf(producto.getExcedente()));

            // Listar descuentos en el menu de producto
            menu.removeViews(1, menu.getChildCount()-3);
            ArrayList<String> menu_prod = producto.getDescuentosString();
            if (menu_prod.size()>0 && (menu.getChildCount() != menu_prod.size()+3)){
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

            // Imagen (Flecha) de menu emergente de producto
			imageView = (ImageView) view.findViewById(R.id.banner_producto_menu_image_view);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
                    LayoutParams layoutParams;
					Producto producto;
					producto = catalogo.getProductosCatalogo().get(position);

                    showPopup(view);
					
//					if(producto.getIsMenuOpen()) {
//						//El menu esta abierto... cerrarlo
//						if(menu != null) {
//							producto.setIsMenuOpen(false);
//							notifyDataSetChanged();
//						}
//						layoutParams = new LayoutParams(40, 40);
//						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//						view.setLayoutParams(layoutParams);
//					} else {
//						//El menu esta cerrado... abrirlo
//						if(menu != null) {
//                            for (Producto prod:catalogo.getProductosCatalogo()){
//                                prod.setIsMenuOpen(false);
//                            }
//							producto.setIsMenuOpen(true);
//							notifyDataSetChanged();
//						}
//						layoutParams = new LayoutParams(180, 40);
//						layoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
//						view.setLayoutParams(layoutParams);
//					}
				}
			});

            // Boton de descuento manual
            TextView prodMenuText = (TextView) view.findViewById(R.id.banner_producto_menu_item_descuento_manual);
            prodMenuText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    View view2 = (View) view.getParent();
                    ((CatalogoActivity)menuActivity).setValorDescuentoManual((Producto)view2.getTag());
                    Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", 3000).show();
                }
            });

		}else{
            view.setVisibility(View.GONE);
        }
		return view;
	}

    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.layout.banner_producto_catalogo_popup_layout, popup.getMenu());
        final View parentPopUpView = v;

        //Actualizar Existencia
        popup.getMenu().add(mContext.getText(R.string.meta_restante)+" "+String.valueOf(producto.getExistencia()));

        popup.getMenu().add(mContext.getText(R.string.excedentes)+" "+String.valueOf(producto.getExcedente()));

        //Descuento Producto
        popup.getMenu().getItem(1).setEnabled(false);

        //Descuento Categoria
        SubMenu descCatSubmenu = popup.getMenu().getItem(2).getSubMenu();
        if(descCatSubmenu != null){
            descCatSubmenu.clear();
            ArrayList<String> menu_prod = producto.getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) {
                descCatSubmenu.add(menu_prod.get(i));
            }
        }

        //Descuento Grupo Categoria
        SubMenu descGrupoSubmenu = popup.getMenu().getItem(3).getSubMenu();
        if(descGrupoSubmenu != null && producto.getGrupoCategoria()!= null){
            descGrupoSubmenu.clear();
            ArrayList<String> menu_prod = producto.getGrupoCategoria().getDescuentosString();
            Log.d("DEBUG", "size: "+producto.getCantidadDescuentosGroup());
            for(int i=0, size=menu_prod.size(); i<size; i++) {
                descGrupoSubmenu.add(menu_prod.get(i));
            }
        }else{
            popup.getMenu().getItem(3).setEnabled(false);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_desc_popup_menu:
                        //agregar descuento
                        ((CatalogoActivity) menuActivity).setValorDescuentoManual((Producto) parentPopUpView.getTag());
                        Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", 3000).show();
                        break;
                    case R.id.add_desc_cat_popup_menu:
                        Log.d("DEBUG", "Tap en desc cat");
                        break;
                    case R.id.add_desc_grupo_cat_popup_menu:
                        Log.d("DEBUG", "Tap en desc grupo");
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        popup.show();
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

}
