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
    protected String TAG = this.getClass().getSimpleName();
	/** Objeto que contiene la logica del carrito de productos*/
	private Carrito carrito;
	/** Objeto que contiene un producto de manera temporal*/
	private Producto producto;
    private Context mContext;

	/** Constructor por default, permite crear el listado de Views de productos utilizando un ArrayList de Productos
	 *  @param context Contexto actual de la aplicacion.
	 *  @param carrito Arreglo de productos. */
	public BannerProductoCarrito(Context context, Carrito carrito) {
		super(context);
		this.carrito = carrito;
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
		View view; TextView textView; ImageView imageView;
		
		if (convertView == null) {
			view = ((LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.banner_producto_carrito_layout, null);
            producto = (Producto) getItem(position);

            assert view != null && producto != null;
            mContext= view.getContext();
            NumberPicker np = (NumberPicker) view.findViewById(R.id.numberPicker);
            np.setMinValue(1);
            int max = producto.getExcedente()+producto.getExistencia();
            np.setMaxValue(max);
            np.setValue(producto.getCantidad());
            np.setWrapSelectorWheel(false);

            //Listener para cuando se cambia el valor mediante +/-
            CustomOnValueChangeListener onValueChangeListener = new CustomOnValueChangeListener(np, producto);
            np.setOnValueChangedListener(onValueChangeListener);

            //Monto total de productos de este tipo
            textView = (TextView) view.findViewById(R.id.banner_carrito_total_text_view);
            if(textView != null) textView.setText(producto.getPrecioComercialTotalConIvaString());

            //Eliminar producto del carrito
            imageView = (ImageView) view.findViewById(R.id.banner_carrito_eliminar_image_view);
            imageView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Producto producto = (Producto) getItem(position);
                    producto.setCantidad(0);
                    producto.setIsInCarrito(false);
                    CatalogoActivity.catalogoAdapter.notifyDataSetChanged();

                    carrito.removeProducto(position);
                    carrito.recalcularMontos();
                    setTotalCarrito(getCarrito().calcularTotalString());
                    notifyDataSetChanged();
                    if(carrito.getCount() == 0) hideCarrito();
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

            imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
            if(producto.getImagen() == null){
                imageView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.prod_background));
            }else{
                imageView.setImageBitmap(producto.getImagen());
            }

            //Etiqueta de Descuento Aplicado
            RelativeLayout rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
            rlDesc.setVisibility(View.INVISIBLE);
            if(producto.calcularDescuentoAplicado() > 0.0){
                Log.d(TAG, "Descuento aplicado : " + producto.calcularDescuentoAplicado());
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
        } else {
            view = convertView;
        }

        return view;
	}

    /**
     * Procedimiento que actualiza la cantidad del <code>Producto</code> siendo modificado en el carrito
     * @param producto <code>Producto</code> siendo modificado
     * @param cant cantidad a setear en el <code>Producto</code>
     */
    protected void updateCantidadCarrito(Producto producto, int cant){
        producto.setCantidad(cant);
        carrito.recalcularMontos();
        notifyDataSetChanged();
        setTotalCarrito(getCarrito().calcularTotalString());
    }

	public Carrito getCarrito() {
		return carrito;
	}

    private class CustomOnValueChangeListener implements NumberPicker.OnValueChangeListener{
        NumberPicker np;
        Producto producto;

        CustomOnValueChangeListener(NumberPicker np, Producto producto){
            this.np = np;
            this.producto = producto;
        }

        @Override
        public void onValueChange(NumberPicker numberPicker, int prev, int act) {
            if(numberPicker.getValue() > 1){
                updateCantidadCarrito(producto, act);
            }
        }
    }
}
