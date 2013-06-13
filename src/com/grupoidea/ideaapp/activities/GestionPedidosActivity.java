package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Producto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
        clienteSpinner = (Spinner) findViewById(R.id.menu_cliente_select_spinner);
        denom = new String();
        productos = new ArrayList<Producto>();
        try {
            Intent intent = getIntent();
            String jsonStr = intent.getStringExtra("Productos");
            productosJSON = new JSONArray(jsonStr);
            llenarProductosfromJSON(productosJSON);
            denom = productos.get(0).getDenominacion();
            TextView text;
            //Fecha
                text = (TextView) findViewById(R.id.fecha_edit);
                Time now = new Time(); now.setToNow();
                text.setText(now.monthDay + "/" + now.month + "/" + now.year);

            Log.d("DEBUG", String.valueOf(clienteSelected));
            //ID Cliente
                text = (TextView) findViewById(R.id.id_cliente_edit);
                text.setText(clientes.get(clienteSelected).getId());
            //Nombre Cliente
                text = (TextView) findViewById(R.id.nombre_cliente_edit);
                text.setText(clientes.get(clienteSelected).getNombre());
            //# Orden Compra
                //TODO convertir a funcion
                text = (TextView) findViewById(R.id.nombre_cliente_edit);
                text.setText("#0019587");
            //Subtotal

            //Descuento Comercial
                desc=clientes.get(clienteSelected).getDescuento();
                text = (TextView) findViewById(R.id.desc_comercial_edit);
                text.setText(desc+denom);
            //Flete
                //TODO convertir a funcion
                flete = 0.0;
                text = (TextView) findViewById(R.id.flete_edit);
                text.setText(flete+denom);
            //Miscelaneos
                //TODO convertir a funcion
                misc = 0.0;
                text = (TextView) findViewById(R.id.misc_edit);
                text.setText(misc+denom);
            //Impuesto
                //TODO convertir a funcion
                imp=0.0;
                text = (TextView) findViewById(R.id.impuesto_edit);
                text.setText(imp+denom);

            //Total
                //subtotal + impuestos -desc comercial y bla bla


        } catch (JSONException e) {
            Log.d("DEBUG", "onCreate: "+e.getMessage());
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
        //TODO String envioAddress = //en el onclick
    }

    public void llenarProductosfromJSON(JSONArray json){
        try {
            Producto prod;
            JSONObject jsonO;
            for(int i=0, size=json.length();i<size;i++){
                jsonO = json.getJSONObject(i);
                prod = Producto.setFromJSON(jsonO);
                productos.add(prod);
            }
        } catch (JSONException e) {
            Log.d("DEBUG", "llenarProductosfromJSON: "+e.getMessage());
        }
    }

}
