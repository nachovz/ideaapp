package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DetalleProductoActivity;
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Producto;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends ParentBannerProducto {
    protected String TAG = this.getClass().getSimpleName();
	/** Listado de elementos del carrito*/
	private ListView listCarrito;
	/** Arreglo de productos.*/
	private ArrayList<Producto> productos;
    private Catalogo catalogo;
	/** ViewGroup que permite mostrar el menu del producto.*/
    private LinearLayout menu;
	protected Producto producto;
	private BannerProductoCatalogo catalogoAdapter;
	private Context mContext;
	
	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param catalogo Arreglo de productos. */
    public BannerProductoCatalogo(Context context, Catalogo catalogo, ListView listCarrito) {
		super(context);
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
		View view;
		TextView textView;
		ImageView imageView;
		LayoutInflater inflater;

		producto = (Producto) getItem(position);

        //View previo no existe
		if (convertView == null) {  
			inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_catalogo_layout, null);
		} else {
        	view = convertView;
        }

        if(producto != null && producto.getIsInCatalogo() && view!=null) {
            view.setVisibility(View.VISIBLE);
            //Nombre producto
			if(producto.getCodigo() != null) {
				textView = (TextView) view.findViewById(R.id.banner_producto_titulo_text_view);
				textView.setText(producto.getCodigo());
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

            //Boton de agregar a carrito
            imageView = (ImageView) view.findViewById(R.id.banner_producto_add_carrito_image_view);
			if(producto.getIsInCarrito()) {
				/* Mostrar el boton del carrito como seleccionado si el producto se encuentra dentro del mismo*/
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito_selected));
			}else{
                /* No mostrar el boton de carrito seleccionado*/
				imageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.client_boton_carrito));
			}

            //MarcasListener de agregar al carrito y mostrar carrito
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
                        producto.addCantidad();
                        catalogoAdapter.notifyDataSetChanged();
                        adapterCarrito = (BannerProductoCarrito) listCarrito.getAdapter();
                        adapterCarrito.getCarrito().addProducto(producto);
                        //Realiza el scroll al elemento agregado o incrementado.
                        index = adapterCarrito.getCarrito().findProductoIndexById(producto.getId());
                        //Recalcular montos y actualizar vista
                        adapterCarrito.getCarrito().recalcularMontos();
                        adapterCarrito.notifyDataSetChanged();

                        listCarrito.post(new Runnable() {
                            public void run() {
                                listCarrito.smoothScrollToPosition(index);
                            }
                        });

                        //Calcula el total del carrito
                        setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                    }else{
                        Toast.makeText(mContext, "No hay disponibilidad del producto", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            //Cargar Imagen
            imageView = (ImageView) view.findViewById(R.id.banner_producto_image_view);

            if(producto.getImagenURL() == null || producto.getImagenURL().isEmpty()){
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prod_background));
            }else{
                loadBitmap(producto, imageView);
            }

			//Crear comportamiento de click al articulo = despachar al activity de detalle de producto.
			View title = view.findViewById(R.id.banner_producto_titulo_marca_linearLayout);
			view.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
                //Colocar posicion en el Bundle
                Bundle extras = new Bundle();
                extras.putInt("position", position);
                menuActivity.dispatchActivity(DetalleProductoActivity.class, extras, false);
				}
			});

            // Imagen (Flecha) de menu emergente de producto
			imageView = (ImageView) view.findViewById(R.id.banner_producto_menu_image_view);
            imageView.setTag(producto);
			imageView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
                    showPopup(view);
				}
			});

            // Boton de descuento manual
            TextView prodMenuText = (TextView) view.findViewById(R.id.banner_producto_menu_item_descuento_manual);
            prodMenuText.setTag(producto);
            prodMenuText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ((CatalogoActivity)menuActivity).setValorDescuentoManual((Producto)view.getTag());
                    Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", Toast.LENGTH_LONG).show();
                }
            });

		}else{
            view.setVisibility(View.GONE);
        }
		return view;
	}

    /**
     * Procedimiento que maneja los taps sobre el boton de menu en el Banner de Producto del Catalogo
     * @param v vista sobre la cual se hizo tap
     */
    public void showPopup(final View v) {
        PopupMenu popup = new PopupMenu(mContext, v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.layout.banner_producto_catalogo_popup_layout, popup.getMenu());
        Producto producto = (Producto)v.getTag();

        //Actualizar Existencia (Excedentes y Metas)
        popup.getMenu().add(mContext.getText(R.string.meta_restante_label)+" "+String.valueOf(producto.getExistencia()));
        popup.getMenu().add(mContext.getText(R.string.excedentes_label)+" "+String.valueOf(producto.getExcedente()));

        //Descuento Producto
        SubMenu descProdSubmenu = popup.getMenu().getItem(1).getSubMenu();
        if(descProdSubmenu != null && producto.hasDescuentos()){
            //Agregar Descuentos por Producto al menu
            descProdSubmenu.clear();
            ArrayList<String> menu_prod = producto.getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) {
                descProdSubmenu.add(menu_prod.get(i));
            }
        }else{
            //Desactivar Item
            popup.getMenu().getItem(1).setEnabled(false);
        }


        //Descuento Categoria
        SubMenu descCatSubmenu = popup.getMenu().getItem(2).getSubMenu();
        if(descCatSubmenu != null && producto.getCategoria()!=null && producto.getCategoria().hasDescuentos()){
            //Agregar Descuentos por Categoria al menu
            descCatSubmenu.clear();
            ArrayList<String> menu_prod = producto.getCategoria().getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) {
                descCatSubmenu.add(menu_prod.get(i));
            }
        }else{
            //Desactivar Item
            popup.getMenu().getItem(2).setEnabled(false);
        }

        //Descuento Grupo Categoria
        SubMenu descGrupoSubmenu = popup.getMenu().getItem(3).getSubMenu();
        if(descGrupoSubmenu != null && producto.getGrupoCategorias()!= null && producto.getGrupoCategorias().hasDescuentos()){
            //Agregar Descuentos por Grupo de Categoria al menu
            descGrupoSubmenu.clear();
            ArrayList<String> menu_prod = producto.getGrupoCategorias().getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) {
                descGrupoSubmenu.add(menu_prod.get(i));
            }
        }else{
            //Desactivar Item
            popup.getMenu().getItem(3).setEnabled(false);
        }

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_desc_popup_menu:
                        //agregar descuento
                        ((CatalogoActivity) menuActivity).setValorDescuentoManual((Producto) v.getTag());
                        Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", 3000).show();
                        break;
                    case R.id.add_desc_cat_popup_menu:
//                        Log.d(TAG, "Tap en desc cat");
                        break;
                    case R.id.add_desc_grupo_cat_popup_menu:
//                        Log.d(TAG, "Tap en desc grupo");
                        break;
                    default:
                        break;
                }

                return true;
            }
        });

        popup.show();
    }

    public void loadBitmap(Producto producto, ImageView imageView) {
        String imageURL = producto.getImagenURL();
        if (cancelPotentialWork(imageURL, imageView)) {
            final BitmapWorkerTask task = new BitmapWorkerTask(mContext, imageView);
            final AsyncDrawable asyncDrawable = new AsyncDrawable(mContext.getResources(), producto.imagen, task);
            imageView.setImageDrawable(asyncDrawable);
//            task.execute(imageURL);
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,imageURL);
        }
    }

    static class AsyncDrawable extends BitmapDrawable {
        private final WeakReference<BitmapWorkerTask> bitmapWorkerTaskReference;

        public AsyncDrawable(Resources res, Bitmap bitmap, BitmapWorkerTask bitmapWorkerTask) {
            super(res, bitmap);
            bitmapWorkerTaskReference = new WeakReference<BitmapWorkerTask>(bitmapWorkerTask);
        }

        public BitmapWorkerTask getBitmapWorkerTask() {
            return bitmapWorkerTaskReference.get();
        }
    }

    public static boolean cancelPotentialWork(String data, ImageView imageView) {
        final BitmapWorkerTask bitmapWorkerTask = getBitmapWorkerTask(imageView);

        if (bitmapWorkerTask != null) {
            final String bitmapData = bitmapWorkerTask.data;
            if(bitmapData != null && data != null){
                if (!bitmapData.equals(data)) {
                    // Cancel previous task
                    bitmapWorkerTask.cancel(true);
                } else {
                    // The same work is already in progress
                    return false;
                }
            }
        }
        // No task associated with the ImageView, or an existing task was cancelled
        return true;
    }

    private static BitmapWorkerTask getBitmapWorkerTask(ImageView imageView) {
        if (imageView != null) {
            final Drawable drawable = imageView.getDrawable();
            if (drawable instanceof AsyncDrawable) {
                final AsyncDrawable asyncDrawable = (AsyncDrawable) drawable;
                return asyncDrawable.getBitmapWorkerTask();
            }
        }
        return null;
    }

}
