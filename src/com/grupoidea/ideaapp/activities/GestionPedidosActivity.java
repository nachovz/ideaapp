package com.grupoidea.ideaapp.activities;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.format.Time;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TableRow;
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

            //llenar tablerow con productos
            TableLayout tl = (TableLayout)findViewById(R.id.listado_productos_pedido_table);
            TableRow tr =new TableRow(this);
            TextView t1= new TextView(this),t2 = new TextView(this),t3 = new TextView(this),t4 = new TextView(this);
            Boolean darkBackground = false;
            for(int i=0, size=productos.size(); i<size; i++){
                Producto prod = productos.get(i);
                tr =new TableRow(this);
                t1= new TextView(this);
                t1.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t1.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t1.setTextColor(Color.parseColor("#808080"));
                t1.setPadding(18,18,18,18);
                t1.setText(prod.getNombre());
                if(darkBackground) t1.setBackgroundColor(Color.parseColor("#D9D9D9"));

                t2 = new TextView(this);
                t2.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t2.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t2.setTextColor(Color.parseColor("#808080"));
                t2.setPadding(18,18,18,18);
                t2.setText(prod.getPrecio()+denom);
                if(darkBackground) t2.setBackgroundColor(Color.parseColor("#D9D9D9"));

                t3 = new TextView(this);
                t3.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t3.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t3.setTextColor(Color.parseColor("#808080"));
                t3.setPadding(18,18,18,18);
                t3.setText(String.valueOf(prod.getCantidad()));
                if(darkBackground) t3.setBackgroundColor(Color.parseColor("#D9D9D9"));

                t4 = new TextView(this);
                t4.setHeight(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t4.setWidth(RelativeLayout.LayoutParams.WRAP_CONTENT);
                t4.setTextColor(Color.parseColor("#808080"));
                t4.setPadding(18, 18, 18, 18);
                t4.setText(prod.getStringPrecioTotal()+denom);
                if(darkBackground) t4.setBackgroundColor(Color.parseColor("#D9D9D9"));

                //Añadir a TableLayout de productos
                tr.addView(t1, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                tr.addView(t2, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                tr.addView(t3, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                tr.addView(t4, new TableRow.LayoutParams(TableRow.LayoutParams.MATCH_PARENT, TableRow.LayoutParams.WRAP_CONTENT));
                tl.addView(tr, new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
                darkBackground=!darkBackground;
            }



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
