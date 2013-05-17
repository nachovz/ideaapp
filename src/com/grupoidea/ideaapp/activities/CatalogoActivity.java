package com.grupoidea.ideaapp.activities;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
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
import com.parse.ParseRelation;

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
		
//		producto = new Producto(1, "nacho 40'", 13550);
//		carrito.addProducto(producto);
		
		
//		catalogoProductos = new ArrayList<Producto>();
//		producto = new Producto(1, "TV Samsung 3D 40'", 13550);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.tv));
//		catalogoProductos.add(producto);
//		producto = new Producto(2, "Microondas Samsung", 1200);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.microondas));
//		catalogoProductos.add(producto);
//		producto = new Producto(3, "Aire Acondicionado Split 12KBTU", 9340);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.aire));
//		catalogoProductos.add(producto);
//		producto = new Producto(4, "Equipo Sonido QKMJ12", 2490);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.audio));
//		catalogoProductos.add(producto);
//		producto = new Producto(5, "TV SmartTV Samung 60'", 23000);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.tv));
//		catalogoProductos.add(producto);
//		producto = new Producto(6, "Vinera Samnsug", 943);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.vinera));
//		catalogoProductos.add(producto);
//		producto = new Producto(7, "Sonido Home Samsung", 5200);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.sonido));
//		catalogoProductos.add(producto);
//		producto = new Producto(8, "Parrillera Grill Delonghi", 6599);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.parrillera));
//		catalogoProductos.add(producto);
//		producto = new Producto(9, "Thermos AVT123", 239);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.thermos));
//		catalogoProductos.add(producto);
//		producto = new Producto(10, "Cafetera Delonghi", 12000);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.cafetera));
//		catalogoProductos.add(producto);
//		producto = new Producto(11, "Tostadora Casera", 960);
//		producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.tostadoras));
//		catalogoProductos.add(producto);
		
		
	}
	
	private Producto retreiveProducto(ParseObject producto){
		
		String codigo = producto.getString("codigo");
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("costo");
		String objectId = producto.getObjectId();
		
		Producto prod = new Producto(objectId,codigo, nombre, precio);

		ParseObject marca = producto.getParseObject("marca");
		prod.setIdMarca(marca.getObjectId());
		prod.setNombreMarca(marca.getString("nombre"));
		prod.setClaseMarca(marca.getString("clase"));
	
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
		
		for (ParseObject parseObject : productosParse) {	
			producto = retreiveProducto(parseObject);
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
							Toast.makeText(getApplicationContext(), "Debe agregar elementos en el carrito.", Toast.LENGTH_LONG).show();
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
		
		for (int i = 0; i < carrito.count(); i++) {
			
		}
		
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
