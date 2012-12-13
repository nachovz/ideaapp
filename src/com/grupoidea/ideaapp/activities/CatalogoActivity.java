package com.grupoidea.ideaapp.activities;

import java.util.ArrayList;

import android.os.Bundle;
import android.view.View;
import android.widget.GridView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProducto;
import com.grupoidea.ideapp.models.Producto;
import com.grupoidea.ideapp.models.Request;
import com.grupoidea.ideapp.models.Response;

public class CatalogoActivity extends ParentMenuActivity {
	/** Elemento que permite mostrar Views en forma de grid.*/
	private GridView grid;
	/** Cadena de texto que contiene el nombre del cliente*/
	private String clienteNombre;
	
	public CatalogoActivity() {
		super(false, false); //TODO: Modificar a autoLoad:true, hasCache:true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		BannerProducto adapter;
		ArrayList<Producto> productos;
		Producto producto;
		
		super.onCreate(savedInstanceState);
		
		clienteNombre = getIntent().getExtras().getString("clienteNombre");
		if(clienteNombre != null) {
			setMenuTittle(clienteNombre);
		}
		
		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.catalogo_layout);
		
		productos = new ArrayList<Producto>();
		
		producto = new Producto("Producto1", "322 Bs.");
		productos.add(producto);
		producto = new Producto("Producto2", "321 Bs.");
		productos.add(producto);
		producto = new Producto("Producto3", "123 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		producto = new Producto("Producto4", "242 Bs.");
		productos.add(producto);
		
		adapter = new BannerProducto(this, productos);
		
		grid = (GridView) this.findViewById(R.id.catalogo_grid);
		grid.setAdapter(adapter);
	}
	
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {

	}

	@Override
	protected Request getRequest() {
		return null;
	}

}
