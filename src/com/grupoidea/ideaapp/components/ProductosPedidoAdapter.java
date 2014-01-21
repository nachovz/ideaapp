package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.models.Producto;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by fernando on 1/20/14.
 */
public class ProductosPedidoAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<Producto> productos;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");

    private ProductosPedidoAdapter(){

    }

    private ProductosPedidoAdapter(Context mContext){
        this();
        this.mContext = mContext;
    }

    public ProductosPedidoAdapter(Context mContext, ArrayList<Producto> productos){
        this(mContext);
        this.productos = productos;
    }

    @Override
    public int getCount() {
        return productos.size();
    }

    @Override
    public Object getItem(int i) {
        return productos.get(i);
    }

    @Override
    public long getItemId(int i) {
        return productos.get(i).hashCode();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        if(convertView == null)
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_producto_pedido_layout, null);

        assert  convertView != null;
        Producto prod = productos.get(i);

        //Set Background
        if(i%2 == 0) convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        else convertView.setBackgroundColor(Color.parseColor("#E4E4E4"));

        //Codigo
        TextView tv = (TextView) convertView.findViewById(R.id.codigo_lista_producto_pedido_textView);
        tv.setText(prod.getCodigo());

        //Cant
        tv = (TextView) convertView.findViewById(R.id.cantidad_lista_producto_pedido_textView);
        tv.setText(String.valueOf(prod.getCantidad()));

        //Precio Lista
        tv = (TextView) convertView.findViewById(R.id.precio_lista_producto_pedido_textView);
        tv.setText(prod.getStringPrecio());

        //Precio Comercial
        tv = (TextView) convertView.findViewById(R.id.precio_comercial_lista_producto_pedido_textView);
        tv.setText(prod.getStringPrecioComercial());

        //Descuento Comercial
        tv = (TextView) convertView.findViewById(R.id.descuento_volumen_lista_producto_pedido_textView);
        tv.setText(df.format(prod.getPrecioComercial() * prod.getDescuentoAplicado()) + " (" + prod.getDescuentoAplicadoPorcString() + ")");

        //Precio Final
        tv = (TextView) convertView.findViewById(R.id.precio_final_lista_producto_pedido_textView);
        tv.setText(prod.getStringPrecioComercialTotal());

        return convertView;
    }
}
