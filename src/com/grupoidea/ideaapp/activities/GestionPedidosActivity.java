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
import com.grupoidea.ideaapp.models.Cliente;
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
    Cliente cliente;
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
            cliente = new Cliente(intent.getStringExtra("Cliente"));
            cliente.setId(intent.getStringExtra("ClienteId"));
            cliente.setDescuento(intent.getDoubleExtra("Descuento", 0.0));
            productosJSON = new JSONArray(jsonStr);
            llenarProductosfromJSON(productosJSON);
            denom = productos.get(0).getDenominacion();
            TextView text;
            //Fecha
                text = (TextView) findViewById(R.id.fecha_edit);
                Time now = new Time(); now.setToNow();
                text.setText(now.monthDay + "/" + now.month + "/" + now.year);

            //ID Cliente
                text = (TextView) findViewById(R.id.id_cliente_edit);
                text.setText(cliente.getId());
            //Nombre Cliente
                text = (TextView) findViewById(R.id.nombre_cliente_edit);
                text.setText(cliente.getNombre());
            //# Orden Compra
                //TODO convertir a funcion
                text = (TextView) findViewById(R.id.numero_orden_compra_edit);
                text.setText("#0019587");

            //Subtotal
                subtotal = getSubtotal();
                text = (TextView) findViewById(R.id.subtotal_edit);
                text.setText(subtotal+denom);

            //Descuento Comercial
                desc=cliente.getDescuento()/100.0;
                Log.d("DEBUG", desc+"");
                desc = subtotal *desc;
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
                total=subtotal-desc; //TODO + impuestos flete y eso
                text = (TextView) findViewById(R.id.total_edit);
                text.setText(total+denom);



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

    public Double getSubtotal(){
        Double sub=0.0;
        for(Producto prod:productos){
            sub += prod.getPrecioTotal();
        }

        return sub;
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
