package com.grupoidea.ideaapp.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Producto;

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
	private BannerProductoCarrito adapterCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	private BannerProductoCatalogo adapterCatalogo;
	
	public CatalogoActivity() {
		super(false, false, true); //TODO: Modificar a autoLoad:true, hasCache:true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Producto producto;
		RelativeLayout menuRight;
		ListView listCarrito;
		
		super.onCreate(savedInstanceState);
		
		clienteNombre = getIntent().getExtras().getString("clienteNombre");
		if(clienteNombre != null) {
			setMenuTittle(clienteNombre);
		}
		
		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.catalogo_layout);
		
		carrito = new Carrito();
		adapterCarrito = new BannerProductoCarrito(this, carrito);
		menuRight = (RelativeLayout) getMenuRight();
		if(menuRight != null) {
			listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
			if(listCarrito != null) {
				listCarrito.setAdapter(adapterCarrito);
			}
		}
		
		catalogoProductos = new ArrayList<Producto>();
		producto = new Producto("Producto1", 322);
		catalogoProductos.add(producto);
		producto = new Producto("Producto2", 321.734);
		catalogoProductos.add(producto);
		producto = new Producto("Producto3", 123);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		producto = new Producto("Producto4", 242);
		catalogoProductos.add(producto);
		
		adapterCatalogo = new BannerProductoCatalogo(this, catalogoProductos, adapterCarrito);
		grid = (GridView) this.findViewById(R.id.catalogo_grid);
		if(grid != null) {
			grid.setAdapter(adapterCatalogo);
		}
	}
	
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {

	}

	@Override
	protected Request getRequest() {
		return null;
	}
}
