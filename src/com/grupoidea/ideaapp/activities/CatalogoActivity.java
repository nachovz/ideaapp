package com.grupoidea.ideaapp.activities;

import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
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
	private ArrayList<Producto> catalogoProductos;
	/** Objeto que representa al carrito de compras del catalogo.*/
	private Carrito carrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del carrito y el layout de cada producto*/
	public BannerProductoCarrito adapterCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	public BannerProductoCatalogo adapterCatalogo;
	
	public CatalogoActivity() {
		super(true, false, true, true); //TODO: Modificar a autoLoad:true, hasCache:true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		
		clienteNombre = getIntent().getExtras().getString("clienteNombre");
		if(clienteNombre != null) {
			setMenuTittle(clienteNombre);
		}

		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.catalogo_layout);
	}
	
	private Producto retrieveProducto(ParseObject producto){
		
		String codigo = producto.getString("codigo");
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("costo");
		String objectId = producto.getObjectId();
		
		Producto prod = new Producto(objectId,codigo, nombre, precio);
        prod.setExistencia(producto.getInt("existencia"));

		ParseObject marca = producto.getParseObject("marca");
		prod.setIdMarca(marca.getObjectId());
		prod.setNombreMarca(marca.getString("nombre"));
		prod.setClaseMarca(marca.getString("clase"));
        final SparseArray<Double> tablaDescuentos= new SparseArray<Double>();
        Log.d("CatalogoActivity", "-----------------------------------------------------------");
        Log.d("CatalogoActivity", prod.getNombre());

        //Obtener marcas
        final ParseQuery query = new ParseQuery("Marca");
        query.whereEqualTo("nombre", prod.getNombreMarca());
        query.whereEqualTo("clase", prod.getClaseMarca());
        query.findInBackground(new FindCallback() {
            public void done(List<ParseObject> marcas, ParseException e) {
                if(e == null && marcas != null){
                    Log.d("CatalogoActivity", "Marcas obtenidas: "+String.valueOf(marcas.size()));

                    //Obtener descuentos para marcas
                    for (ParseObject marcaObj : marcas) {
                        ParseQuery descuentosQuery = marcaObj.getRelation("descuentos").getQuery();
                        descuentosQuery.findInBackground(new FindCallback() {
                            public void done(List<ParseObject> descuentos, ParseException e) {
                                if(e == null && descuentos != null){
                                    Log.d("CatalogoActivity", "Descuentos recuperados para este producto: "+descuentos.size());
                                    for(ParseObject descuento: descuentos){
                                        tablaDescuentos.append(descuento.getInt("cantidad"),descuento.getDouble("porcentaje"));
                                    }
                                    for(int i=0, size=tablaDescuentos.size(); i<size; i++){
                                        Log.d("CatalogoActivity","cant: " + tablaDescuentos.keyAt(i) + ", porc: " + tablaDescuentos.valueAt(i).toString());
                                    }
                                }else{
                                    Log.d("CatalogoActivity", "No se recuperaron descuentos para este producto");
                                }
                            }
                        });
                    }
                }else{
                    Log.d("CatalogoActivity", "result: null");
                }
            }
        });
        //Almacenar tabla de descuentos en el Producto
        prod.setTablaDescuentos(tablaDescuentos);
        
        return prod;
	}
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		// TODO Mostrar el response de la consulta en los elementos del activity.
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
		adapterCarrito = new BannerProductoCarrito(this, carrito);
		menuRight = (RelativeLayout) getMenuRight();
		if(menuRight != null) {
			listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
			relativeLayout = (RelativeLayout) menuRight.findViewById(R.id.carrito_total_layout);
			if(relativeLayout != null) {
				relativeLayout.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View arg0) {
						
						String productos = productsToJSON();
						
						if(productos != ""){
							Bundle bundle = new Bundle();
							
							bundle.putString("Productos", productos);
							
							dispatchActivity(GestionPedidosActivity.class, bundle, false);
						}else{
							Toast.makeText(getApplicationContext(), "Debe agregar elementos en el carrito", Toast.LENGTH_LONG).show();
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

	protected String productsToJSON() {
		String productos = "";
		JSONArray productosArray = new JSONArray();
		JSONObject producto = new JSONObject();
        ArrayList<Producto> prodsCarrito = carrito.getProductos();

        try {
            for (int i = 0, count = carrito.count(); i < count; i++) {
                producto = prodsCarrito.get(i).toJSON();
                productosArray.put(producto);
            }
            productos = productosArray.toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.d("DEBUG", "productosJSONtoString: "+productos);
        return productos;
	}

	@Override
	protected Request getRequestAction() {
		// TODO Crear consulta a la data del Dashboard.
		
		Request req = new Request(Request.PARSE_REQUEST);
		ParseQuery query = new ParseQuery("Producto");
		query.include("marca");
		
		req.setRequest(query);
		
		return req;
	}

}
