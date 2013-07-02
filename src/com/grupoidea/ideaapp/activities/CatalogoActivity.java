package com.grupoidea.ideaapp.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.*;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CatalogoActivity extends ParentMenuActivity {
	/** Elemento que permite mostrar Views en forma de grid.*/
	private GridView grid;
	/** Cadena de texto que contiene el nombre del cliente*/
	private String clienteNombre;
	/** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
	private static ArrayList<Producto> catalogoProductos;
	/** Objeto que representa al carrito de compras del catalogo.*/
	private Carrito carrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del carrito y el layout de cada producto*/
	public static BannerProductoCarrito adapterCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	public static BannerProductoCatalogo adapterCatalogo;
    public String modificarPedidoId;
    /** Minimo y maximo valor para descuentos manueales*/
    public final static Double MIN_DESC_MAN = 0.0, MAX_DESC_MAN = 100.0;
	
	public CatalogoActivity() {
		super(true, false, true, true); //TODO: Modificar a autoLoad:true, hasCache:true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		clienteNombre = getIntent().getExtras().getString("clienteNombre");
        Log.d("DEBUG", "clientenombre: "+clienteNombre);
		if(clienteNombre != null) {
			setMenuTittle(clienteNombre);
		}
        modificarPedidoId= getIntent().getExtras().getString("numPedido");
		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.catalogo_layout);
        //Poblar Spinner de Clientes e inflar
        adapter = getClientesFromParse();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clienteSpinner.setAdapter(adapter);
        clienteSpinner.setEnabled(true);
        clienteSpinner.setVisibility(View.VISIBLE);
        clienteSpinner.setSelection(0);
        clienteSelected=0;
        clienteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clienteSelected = i;
                Log.d("DEBUG", "Cliente seleccionado: " + i);
                updatePreciosComerciales();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });
	}
	
	private Producto retrieveProducto(final ParseObject producto){
		
		String codigo = producto.getString("codigo");
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("costo");
		final String objectId = producto.getObjectId();
		
		final Producto prod = new Producto(objectId,codigo, nombre, precio);

        //Obtener existencia
        ParseQuery queryExistencia = new ParseQuery("UserHasProducto");
        queryExistencia.whereEqualTo("usuario", ParseUser.getCurrentUser());
        queryExistencia.whereEqualTo("producto",producto);
        queryExistencia.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null) prod.setExistencia(parseObject.getInt("cantidad"));
                else{
                    e.printStackTrace();
                    Log.d("DEBUG", "No existe registro del producto "+objectId+" en la tabla UserHasProducto");
                }
            }
        });

        ParseObject categoria = producto.getParseObject("categoria");
		prod.setMarca(producto.getString("marca"));
		prod.setCategoria(categoria.getString("nombre"));
        prod.setIdCategoria(categoria.getObjectId());
        final SparseArray<Double> tablaDescuentos= new SparseArray<Double>();

        //Obtener categorias
        ParseQuery descuentosQuery = categoria.getRelation("descuentos").getQuery();
        descuentosQuery.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> descuentos, ParseException e) {
                if (e == null && descuentos != null) {
                    for (ParseObject descuento : descuentos) {
                        tablaDescuentos.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
                    }
                }
            }
        });
        prod.setTablaDescuentos(tablaDescuentos);
        return prod;
	}
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		@SuppressWarnings("unchecked")
		List<ParseObject> productosParse = (List<ParseObject>) response.getResponse();
		ArrayList<Producto> productos = new ArrayList<Producto>();
		Producto producto;
		RelativeLayout menuRight;
		RelativeLayout relativeLayout;
		ListView listCarrito = null;

        //cargar productos desde Parse
		for (ParseObject parseObject : productosParse) {
			producto = retrieveProducto(parseObject);
			productos.add(producto);
		}

		carrito = new Carrito();
        //Si vengo a modificar un pedido
        //TODO llevar el codigo de pedido generado con el random como matrimonio obligado por todas las activities hasta el gestionar pedido
        if(modificarPedidoId == null){
            Log.d("DEBUG", "Pedido Nuevo");
        }else{
            Log.d("DEBUG", "Modificar Pedido "+modificarPedidoId);
            final ArrayList<Producto> productos1=productos;
            //Pedido Id
            final ParseQuery pedido = new ParseQuery("Pedido");
            pedido.whereEqualTo("objectId", modificarPedidoId);
            pedido.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    //Productos en pedido
                    final ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
                    productosEnPedido.include("producto");
                    productosEnPedido.findInBackground(new FindCallback() {
                        @Override
                        public void done(List<ParseObject> productosEnPedidoObj, ParseException e) {
                            //Me muevo en los productos relacionados al pedido
                            for(ParseObject producto: productosEnPedidoObj){
                                //Agrego los relacionados al pedido en el carrito
                                for(int i=0, size=productos1.size(); i<size; i++){
                                    if(productos1.get(i).getNombre().equals(producto.get("codigo"))){
                                       productos1.get(i).setCantidad(producto.getInt("cantidad"));
                                        carrito.addProducto(productos1.get(i));
                                        Log.d("DEBUG", "Agregando productos de pedido a modificar");
                                    }
                                }
                            }
                        }
                    });
                }
            });
        }

		adapterCarrito = new BannerProductoCarrito(this, carrito);
		menuRight = (RelativeLayout) getMenuRight();
		if(menuRight != null) {
			listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
			relativeLayout = (RelativeLayout) menuRight.findViewById(R.id.carrito_total_layout);
			if(relativeLayout != null) {
				relativeLayout.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {

                        //lanzar GestionPedidosActivity
						String productos = productsToJSONString();
						if(productos != ""){
							Bundle bundle = new Bundle();
							
							bundle.putString("Productos", productos);

                            Cliente clienteM = clientes.get(clienteSpinner.getSelectedItemPosition());

                            bundle.putString("Cliente", clienteM.getNombre());
                            bundle.putString("ClienteId", clienteM.getId());
                            bundle.putDouble("Descuento", clienteM.getDescuento());
                            bundle.putString("parseId", clienteM.getParseId());

							
							dispatchActivity(GestionPedidosActivity.class, bundle, false);
						}else{
							Toast.makeText(getApplicationContext(), getString(R.string.warning_agregar_elementos_carrito), Toast.LENGTH_LONG).show();
						}
					}
				});
			}
			if(listCarrito != null) {
				listCarrito.setAdapter(adapterCarrito);
				listCarrito.setSelection(listCarrito.getAdapter().getCount()-1);
			}
		}
		
		catalogoProductos = productos;
		
		if(listCarrito != null) {
			adapterCatalogo = new BannerProductoCatalogo(this, catalogoProductos, listCarrito);
			grid = (GridView) this.findViewById(R.id.catalogo_grid);
			grid.setOnTouchListener(new OnTouchListener() {
				private int xDown;
				private int xUp;
				private int xDiff;
				private int yDiff;
				private int yDown;
				private int yUp;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						xDown = (int)event.getX();
						yDown = (int)event.getY();
					}
					if(event.getActionMasked() == MotionEvent.ACTION_UP) {
						xUp =(int)event.getX();
						yUp = (int)event.getY();
						
						xDiff = xDown - xUp;
						yDiff = yDown - yUp;
						if(Math.abs(yDiff) < 200 && Math.abs(xDiff) > 200) {
							if(xDiff > 0) {
//								Log.d("RIGHT MENU", String.valueOf(xDiff) + "#" + String.valueOf(yDiff));
								if(!isMenuRightShowed() && !isMenuLeftShowed()) {
									showRightMenu();
								} else if(isMenuLeftShowed()) {
									hideMenuLeft();
								} 
							} else {
//								Log.d("LEFT MENU", String.valueOf(xDiff) + "#" + String.valueOf(yDiff));
								if(!isMenuRightShowed() && !isMenuLeftShowed()) {
									showLeftMenu();
								} else if(isMenuRightShowed()) {
									hideMenuRight();
								}
							}
						}
					}
					return false;
				}
			});
			if(grid != null) {
				grid.setAdapter(adapterCatalogo);
			}
		}
		
	}

	protected String productsToJSONString() {
		String productos = "";
		JSONArray productosJSONArray = new JSONArray();
		JSONObject productoJSONObj;
        ArrayList<Producto> prodsCarrito = carrito.getProductos();

        try {
            for (int i = 0, count = prodsCarrito.size(); i < count; i++) {
                productoJSONObj = prodsCarrito.get(i).toJSON();
                Log.d("DEBUG", "productoJSONObj: "+ productoJSONObj.toString(1));
                productosJSONArray.put(productoJSONObj);
            }
            productos = productosJSONArray.toString();
//            Log.d("DEBUG", "Result productosJSONtoString: "+productos);
            return productos;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DEBUG", e.getCause().toString() + e.getMessage());
        }
        return  null;
	}

    public static void updatePreciosComerciales(){
        Double descCliente = clientes.get(clienteSelected).getDescuento()/100.0;
        Double precio = 0.0;
        for(Producto prod: catalogoProductos){
            precio = prod.getPrecio();
            prod.setPrecioComercial(precio - (precio * descCliente));
        }
        adapterCarrito.notifyDataSetChanged();
        adapterCatalogo.notifyDataSetChanged();
    }

    public void updatePreciosComerciales(int i){
        Double descCliente = clientes.get(clienteSelected).getDescuento()/100.0;
        Producto prod = catalogoProductos.get(i);
        Double precio = prod.getPrecio();
        prod.setPrecioComercial(precio -(precio*descCliente));
    }

    /** Proceso que establece el valor del descuento manual para el producto seleccionado*/
    public void setValorDescuentoManual(final Producto producto){
        final Context oThis = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);    //seteandolo para numeros solamente
        input.setMaxLines(1);
        input.setHint(R.string.descuento_manual);
        builder.setView(input);
        TextView title = new TextView(this);
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
                    producto.setDescuentoManual(valor);
                    adapterCatalogo.notifyDataSetChanged();
                    adapterCarrito.notifyDataSetChanged();
                    Toast.makeText(oThis, "Porcentaje de descuento manual asignado", 3000).show();
                } else {
                    Toast.makeText(oThis, "Porcentaje de descuento manual no valido", 3000).show();
                    Log.d("DEBUG", "porcentaje no valido");
                }
            }
        })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dialogo cancelado
                        Toast.makeText(oThis, "Descuento manual no establecido", 3000).show();
                    }
                });
        builder.create();
        builder.show();
    }

	@Override
	protected Request getRequestAction() {
		Request req = new Request(Request.PARSE_REQUEST);
		ParseQuery query = new ParseQuery("Producto");
        query.include("categoria");
		req.setRequest(query);
		
		return req;
	}

}
