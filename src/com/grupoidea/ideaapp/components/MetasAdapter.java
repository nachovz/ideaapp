package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.models.Meta;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by fernando on 1/17/14.
 */
public class MetasAdapter extends BaseAdapter {
    Context mContext;
    ArrayList<Meta> metas;
    ArrayList<Meta> metaMarca;
    String marca;
    protected DecimalFormat df = new DecimalFormat("###,###,##0.##");
    private String TAG = this.getClass().getSimpleName();

    public MetasAdapter(ArrayList<Meta> metas, Context mContext){
        this.metas = metas;
        this.mContext = mContext;
        metaMarca = new ArrayList<Meta>();
    }

    @Override
    public int getCount() {
        return metaMarca.size();
    }

    @Override
    public Object getItem(int i) {
        return metaMarca.get(i);
    }

    @Override
    public long getItemId(int i) {
        return metaMarca.get(i).hashCode();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        if(convertView == null)
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_metas_layout, null);

        assert  convertView != null;

        //Set Background
        if(i%2 == 0) convertView.setBackgroundColor(Color.parseColor("#FFFFFF"));
        else convertView.setBackgroundColor(Color.parseColor("#D9D9D9"));

        //Codigo
        TextView codigoTextView = (TextView) convertView.findViewById(R.id.meta_codigo_textView);
        codigoTextView.setText(metaMarca.get(i).getProducto().getCodigo());

        //Meta
        TextView metaTextView = (TextView) convertView.findViewById(R.id.meta_meta_textView);
        metaTextView.setText(String.valueOf(metaMarca.get(i).getCantMeta()));

        //Pedido
        TextView pedidoTextView = (TextView) convertView.findViewById(R.id.meta_pedido_textView);
        pedidoTextView.setText(String.valueOf(metaMarca.get(i).getCantPedido()));

        //Facturado
        TextView facturadoTextView = (TextView) convertView.findViewById(R.id.meta_facturado_textView);
        facturadoTextView.setText(String.valueOf(metaMarca.get(i).getCantFacturado()));

        //Valor
        TextView valorTextView = (TextView) convertView.findViewById(R.id.meta_valor_textView);
        valorTextView.setText(df.format(metaMarca.get(i).getValorBs()));

        return convertView;
    }

    public void updateMarca(String marca){
        this.marca = marca;
        metaMarca.clear();
        for(Meta meta: metas){
            if(meta.getMarca().equals(marca)){
                metaMarca.add(meta);
            }
        }
//        Log.d(TAG, "updateMarca: marca: "+marca+" count: "+metaMarca.size());
        this.notifyDataSetChanged();
    }
}
