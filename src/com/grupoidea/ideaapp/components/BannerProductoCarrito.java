package com.grupoidea.ideaapp.components;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
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
        carrito.recalcularDescuentosGrupoCategoria(producto);
		
		if (convertView == null) {  
			inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			view = inflater.inflate(R.layout.banner_producto_carrito_layout, null);
		} else {
			view = convertView;
		}
        mContext= view.getContext();
			
		if(producto != null) {
            //cantidad de productos de esta clase
			editText = (TextView) view.findViewById(R.id.banner_carrito_cantidad);
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
                    carrito.recalcularDescuentosGrupoCategoria(producto);

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
                    carrito.recalcularDescuentosGrupoCategoria(producto);
					carritoAdapter.notifyDataSetChanged();
					//Calcula el total del carrito
					setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
				}
			});

            //Cantidad cambiada con teclado
            TextView cantProd = (TextView) view.findViewById(R.id.banner_carrito_cantidad);

            //numberPicker Dialog
            final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            inflater = (LayoutInflater) menuActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View dialog = inflater.inflate(R.layout.cantidad_picker, null);
            final NumberPicker np = (NumberPicker) dialog.findViewById(R.id.numberPicker);
            np.setValue(producto.getCantidad());
            np.setMinValue(1);
            np.setMaxValue(producto.getExcedente()+producto.getExistencia());
            np.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            builder.setView(dialog);
            final LayoutInflater finalInflater = inflater;
            builder.setMessage(R.string.set_cantidad)
                    .setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogi, int id) {
                            // Send the positive button event back to the host activity
                            producto.setCantidad(np.getValue());
                            carrito.recalcularDescuentosGrupoCategoria(producto);
                            carritoAdapter.notifyDataSetChanged();
                            //Calcula el total del carrito
                            setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());

                        }
                    })
                    .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialogi, int id) {
                            // Send the negative button event back to the host activity
                        }
                    });

            final AlertDialog.Builder tempBuilder = builder;
            cantProd.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //display dialog
                    tempBuilder.create().show();
                }
            });

//            TextWatcher tw = new TextWatcher() {
//                @Override
//                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//                @Override
//                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
//
//                @Override
//                public void afterTextChanged(Editable editable) {
//                    if(!cantProd.getText().toString().isEmpty()){
//                        int cant = Integer.valueOf(cantProd.getText().toString());
//                        int max = producto.getExcedente()+producto.getExistencia();
//                        if(cant > max){
//                            producto.setCantidad(max);
//                        }else{
//                            producto.setCantidad(cant);
//                        }
//                        carrito.recalcularDescuentosGrupoCategoria(producto);
//                        carritoAdapter.notifyDataSetChanged();
//                        //Calcula el total del carrito
//                        setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
//                    }else{
//                        producto.setCantidad(1);
//                        carrito.recalcularDescuentosGrupoCategoria(producto);
//                        carritoAdapter.notifyDataSetChanged();
//                        //Calcula el total del carrito
//                        setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
//                    }
////                    cantProd.requestFocus();
//                }
//            };
//
//            cantProd.addTextChangedListener(tw);

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

                    carrito.recalcularDescuentosGrupoCategoria(producto);
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

            //imagen del producto
			if(producto.getImagen() != null) {
				imageView = (ImageView) view.findViewById(R.id.banner_carrito_image_view);
				imageView.setImageBitmap(producto.getImagen());
			}

            //descuento aplicado
            RelativeLayout rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
            rlDesc.setVisibility(View.INVISIBLE);
            if(producto.getDescuentoAplicado() !=0.0){
                rlDesc = (RelativeLayout) view.findViewById(R.id.banner_carrito_descuento_layout);
                rlDesc.setVisibility(View.VISIBLE);
                TextView porcDescTextView = (TextView) view.findViewById(R.id.descuento_textView);
                porcDescTextView.setText(producto.getDescuentoAplicadoPorcString());
            }

            //Descuento manual
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
