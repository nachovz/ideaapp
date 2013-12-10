package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.activities.CatalogoActivity;
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
    private Context mContext;

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
		LayoutInflater inflater = null;
		TextView editText;
		
		producto = (Producto) getItem(position);
//        carrito.recalcularDescuentosGrupoCategoria(producto);
//        carrito.recalcularMontos();
		
		if (convertView == null) {  
			inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_carrito_layout, null);
		} else {
			view = convertView;
		}
        mContext= view.getContext();
			
		if(producto != null) {
            view.setTag(producto);
            NumberPicker np = (NumberPicker) view.findViewById(R.id.numberPicker);
            np.setMinValue(1);
            np.setMaxValue(producto.getExcedente()+producto.getExistencia());

            Log.d("DEBUG", "Producto :"+producto.getCodigo()+" Cantidad: "+producto.getCantidad());
            np.setValue(producto.getCantidad());
            np.setTag(producto);

            np.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
                @Override
                public void onValueChange(NumberPicker numberPicker, int prev, int act) {
                    Producto productoListener = (Producto)numberPicker.getTag();
                    productoListener.setCantidad(act);
                    carrito.recalcularMontos();
                    carritoAdapter.notifyDataSetChanged();
                    if(productoListener.hasDescuentos()){
                        Log.d("DEBUG", productoListener.getCodigo()+" Descuento producto : cant : "+productoListener.getCantidad()+" % : "+productoListener.getDescuentoAplicado());
                    }
                    if(productoListener.getCategoria()!= null){
                        Log.d("DEBUG", productoListener.getCodigo()+ "Descuento categoria : cant : "+productoListener.getCategoria().getCantItemsCarrito()+" % : "+productoListener.getCategoria().getDescActual());
                    }
                    if(productoListener.getGrupoCategorias() != null){
                        Log.d("DEBUG", productoListener.getCodigo()+" Descuento grupo cant : "+productoListener.getGrupoCategorias().getCantItemsCarrito()+"  % : "+productoListener.getGrupoCategorias().getDescActual());
                    }
                    //Calcula el total del carrito
                    setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
                }
            });

            //Monto total de productos de este tipo
			textView = (TextView) view.findViewById(R.id.banner_carrito_total_text_view);
			if(textView != null) {
				textView.setText(producto.getPrecioComercialTotalConIvaString());
			}

            //Eliminar producto del carrito
			imageView = (ImageView) view.findViewById(R.id.banner_carrito_eliminar_image_view);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
                Producto producto;
                producto = (Producto) getItem(position);
                producto.setCantidad(0);
                CatalogoActivity.adapterCatalogo.removeProductoFlagCarrito(producto);
                CatalogoActivity.adapterCatalogo.notifyDataSetChanged();

                carrito.removeProducto(position);
                carrito.recalcularMontos();
                carritoAdapter.notifyDataSetChanged();
                //Calcula el total del carrito
                setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
                if(carrito.getCount() == 0){
                    hideCarrito();
                }
				}
			});

            //Nombre producto
			if(producto.getCodigo() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_titulo_text_view);
				textView.setText(producto.getCodigo());
			}

            //Marca producto
            if(producto.getMarca() != null) {
                textView = (TextView) view.findViewById(R.id.banner_carrito_marca_textView);
                textView.setText(producto.getMarca());
            }

            //Precio producto
			if(producto.getStringPrecioComercial() != null) {
				textView = (TextView) view.findViewById(R.id.banner_carrito_precio_text_view);
				textView.setText(producto.getPrecioCarritoSinIvaConIvaString());
			}

            //Imagen del producto
			if(producto.getImagen() != null) {
                //Tiene imagen
                imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
                imageView.setImageBitmap(producto.getImagen());
            }else{
                //No tiene imagen, colocar imagen por default
                imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prod_background));
            }

            //Etiqueta de Descuento Aplicado
            RelativeLayout rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
            rlDesc.setVisibility(View.INVISIBLE);
//            if(producto.getDescuentoAplicado() != 0.0){
            if(producto.calcularDescuentoAplicado() > 0.0){
                Log.d("DEBUG", "Descuento aplicado : " + producto.calcularDescuentoAplicado());
                rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
                rlDesc.setVisibility(View.VISIBLE);
                TextView porcDescTextView = (TextView) view.findViewById(R.id.descuento_textView);
                porcDescTextView.setText(producto.getDescuentoAplicadoPorcString());
            }

            //Etiqueta de Descuento Manual
            rlDesc = (RelativeLayout) view.findViewById(R.id.descuento_manual_indicator);
            rlDesc.setVisibility(View.INVISIBLE);
            if(producto.getDescuentoManual() != 0){
                rlDesc = (RelativeLayout) view.findViewById(R.id.descuento_manual_indicator);
                rlDesc.setVisibility(View.VISIBLE);
            }
		}
		return view;
	}
	
	public Carrito getCarrito() {
		return carrito;
	}

}
