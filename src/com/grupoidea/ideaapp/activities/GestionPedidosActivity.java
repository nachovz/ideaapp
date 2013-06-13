package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.ScrollView;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Producto;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GestionPedidosActivity extends ParentMenuActivity {
    protected Context mContext;
    protected JSONArray productosJSON;
    protected double subtotal, desc, flete, misc, imp, total;
    protected String denom;
    /** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
    private ArrayList<Producto> productos;
	public GestionPedidosActivity() {
		super(false, false);
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.gestion_pedidos_layout);
        mContext=this;
        denom = new String();
        try {
            Intent intent = getIntent();
            String json = intent.getStringExtra("Productos");
            productosJSON = new JSONArray(json);
            llenarProductosfromJSON(productosJSON);
            denom = productos.get(0).getDenominacion();
            ScrollView view = new ScrollView(this);

//          Fecha
            TextView text = (TextView) findViewById(R.id.fecha_edit);
            Time now = new Time(); now.setToNow();
            Log.d("DEBUG", now.monthDay+"/"+now.month+"/"+now.year);

            text.setText(now.monthDay+"/"+now.month+"/"+now.year);
        } catch (JSONException e) {
            e.printStackTrace();
        }
	}

	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
	}

	@Override
	protected Request getRequestAction() {
		return null;
	}

    public void commitPedido(View view) {
        Log.d("DEBUG", "Button clicked");
        String id = mContext.getString(view.getId());
        Log.d("DEBUG", "id: "+id);
    }

    public void llenarProductosfromJSON(JSONArray json){
        try {
            for(int i=0, size=json.length();i<size;i++){
                    productos.add((Producto) json.get(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

}
