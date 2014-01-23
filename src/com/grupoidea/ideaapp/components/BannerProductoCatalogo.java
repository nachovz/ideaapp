package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
import com.grupoidea.ideaapp.activities.DetalleProductoActivity;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Producto;

import java.util.ArrayList;

/** Adaptador que permite crear el listado de Views de productos utilizando un ArrayList de Productos*/
public class BannerProductoCatalogo extends ParentBannerProducto {
    protected String TAG = this.getClass().getSimpleName();
    private BannerProductoCarrito adapterCarrito;
    private ListView listCarrito;
    private Carrito carrito;
    private Catalogo catalogo;
	protected Producto producto;
	private Context mContext;

    public BannerProductoCatalogo(Context context, Catalogo catalogo){
        super(context);
        this.mContext = context;
        this.catalogo = catalogo;
    }

	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param catalogo Arreglo de productos. */
    public BannerProductoCatalogo(Context context, Catalogo catalogo, ListView listCarrito) {
		this(context, catalogo);
		this.setListCarrito(listCarrito);
        this.adapterCarrito = (BannerProductoCarrito) listCarrito.getAdapter();
        carrito = adapterCarrito.getCarrito();
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
		producto = (Producto) getItem(position);

		if (convertView == null ) convertView = ((LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.banner_producto_catalogo_layout, null);
        assert convertView != null;

            if(producto != null && producto.getIsInCatalogo()) {
                if(producto.getCodigo() != null) {
                    TextView codigoTextView = (TextView) convertView.findViewById(R.id.banner_producto_titulo_text_view);
                    codigoTextView.setText(producto.getCodigo());
                }

                if(producto.getMarca() != null) {
                    TextView marcaTextView = (TextView) convertView.findViewById(R.id.banner_producto_marca_textView);
                    marcaTextView.setText(producto.getMarca());
                }

                if(producto.getStringPrecioComercial() != null) {
                    TextView precioTextView = (TextView) convertView.findViewById(R.id.banner_producto_precio_text_view);
                    precioTextView.setText(producto.getPrecioComercialSinIvaConIvaString());
                }

                //Boton de agregar a carrito
                ImageView botonCarritoImageView = (ImageView) convertView.findViewById(R.id.banner_producto_add_carrito_image_view);
                int carritoResId = producto.getIsInCarrito()? R.drawable.client_boton_carrito_selected: R.drawable.client_boton_carrito;
                botonCarritoImageView.setImageBitmap(BitmapFactory.decodeResource(mContext.getResources(), carritoResId));

                botonCarritoImageView = (ImageView) convertView.findViewById(R.id.banner_producto_add_carrito_image_view);
                botonCarritoImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final int index;
                        Producto producto = catalogo.getProductosCatalogo().get(position);

                        if(producto.getExistencia()+producto.getExcedente() > 0 && !producto.getIsInCarrito()){
                            showCarrito();
                            producto.setIsInCarrito(true);
                            producto.addCantidad();
                            notifyDataSetChanged();

                            carrito.addProducto(producto);
                            index = carrito.findProductoIndexByCodigo(producto.getCodigo());
                            carrito.recalcularMontos();
                            setTotalCarrito(carrito.calcularTotalString());
                            adapterCarrito.notifyDataSetChanged();

                            getListCarrito().post(new Runnable() {
                                public void run() {
                                    getListCarrito().smoothScrollToPosition(index);
                                }
                            });

                        }else if(producto.getIsInCarrito()){
                            Toast.makeText(mContext, mContext.getString(R.string.producto_already_in_carrito), Toast.LENGTH_SHORT).show();
                        }else{
                            Toast.makeText(mContext, mContext.getString(R.string.producto_not_available), Toast.LENGTH_SHORT).show();
                        }
                    }
                });

                ImageView productoImageView = (ImageView) convertView.findViewById(R.id.banner_producto_image_view);
                if(producto.getImagen() == null) productoImageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prod_background));
                else productoImageView.setImageBitmap(producto.getImagen());

                View productTitleTextView = convertView.findViewById(R.id.banner_producto_titulo_marca_linearLayout);
                productTitleTextView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Bundle extras = new Bundle();
                        extras.putInt("position", position);
                        menuActivity.dispatchActivity(DetalleProductoActivity.class, extras, false);
                    }
                });

                ImageView flechaMenuImageView = (ImageView) convertView.findViewById(R.id.banner_producto_menu_image_view);
                flechaMenuImageView.setTag(producto);
                flechaMenuImageView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        showPopup(view);
                    }
                });

                TextView descuentoManualTextView = (TextView) convertView.findViewById(R.id.banner_producto_menu_item_descuento_manual);
                descuentoManualTextView.setTag(producto);
                descuentoManualTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ((CatalogoActivity) menuActivity).setValorDescuentoManual((Producto) view.getTag());
                        Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", Toast.LENGTH_LONG).show();
                    }
                });

            }else{
                convertView.setVisibility(View.GONE);
            }
		return convertView;
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
            for(int i=0, size=menu_prod.size(); i<size; i++) descProdSubmenu.add(menu_prod.get(i));
        }else popup.getMenu().getItem(1).setEnabled(false);


        //Descuento Categoria
        SubMenu descCatSubmenu = popup.getMenu().getItem(2).getSubMenu();
        if(descCatSubmenu != null && producto.getCategoria()!=null && producto.getCategoria().hasDescuentos()){
            //Agregar Descuentos por Categoria al menu
            descCatSubmenu.clear();
            ArrayList<String> menu_prod = producto.getCategoria().getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) descCatSubmenu.add(menu_prod.get(i));
        }else popup.getMenu().getItem(2).setEnabled(false);

        //Descuento Grupo Categoria
        SubMenu descGrupoSubmenu = popup.getMenu().getItem(3).getSubMenu();
        if(descGrupoSubmenu != null && producto.getGrupoCategorias()!= null && producto.getGrupoCategorias().hasDescuentos()){
            //Agregar Descuentos por Grupo de Categoria al menu
            descGrupoSubmenu.clear();
            ArrayList<String> menu_prod = producto.getGrupoCategorias().getDescuentosString();
            for(int i=0, size=menu_prod.size(); i<size; i++) descGrupoSubmenu.add(menu_prod.get(i));
        }else popup.getMenu().getItem(3).setEnabled(false);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.add_desc_popup_menu:
                        ((CatalogoActivity) menuActivity).setValorDescuentoManual((Producto) v.getTag());
                        Toast.makeText(mContext, "Si desea eliminar el descuento manual pongalo de nuevo en 0", Toast.LENGTH_LONG).show();
                        break;
                    default:
                        break;
                }
                return true;
            }
        });
        popup.show();
    }

    /** Listado de elementos del carrito*/
    public ListView getListCarrito() {
        return listCarrito;
    }

    public void setListCarrito(ListView listCarrito) {
        this.listCarrito = listCarrito;
    }
}
