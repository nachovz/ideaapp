package com.grupoidea.ideaapp.components;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.models.Categoria;

import java.util.ArrayList;

/**
 * Created by fernando on 1/3/14.
 */
public class CategoriasAdapter extends BaseAdapter {
    protected String TAG = this.getClass().getSimpleName();
    ArrayList<Categoria> categorias;
    private ListView categoriasListView;
    Categoria todas;
    private MarcasAdapter marcasAdapter;
    public Categoria categoriaActual;
    protected Context mContext;

    private CategoriasAdapter(){
        categorias = new ArrayList<Categoria>();
    }

    public CategoriasAdapter(Context mContextParam){
        this();
        mContext = mContextParam;
        todas = new Categoria(mContext.getString(R.string.todas));
        categorias.add(todas);
        categoriaActual = todas;
    }

    @Override
    public int getCount() {
        return categorias.size();
    }

    @Override
    public Object getItem(int i) {
        return categorias.get(i);
    }

    @Override
    public long getItemId(int i) {
        return categorias.get(i).hashCode();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {
        //Inflar el View si no existe
        if(convertView == null)
            convertView = ((LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.row_filtro_menu_layout, null);

        assert  convertView != null;
        TextView tv = (TextView) convertView.findViewById(R.id.filtro_menu_item_textView);
        tv.setText(categorias.get(i).toString());
        tv.setTag(categorias.get(i));
        tv.setOnClickListener(new CategoriasListener());

        if(categoriaActual.equals(todas) && categorias.get(i).equals(todas)){
            //aplicar estilo de item seleccionado
            tv.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
            tv.setTextColor(Color.parseColor("#3A70B9"));
        }else if(!categorias.get(i).equals(todas)){
            tv.setBackgroundResource(R.drawable.pastilla_items_filtro);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
        }



        return convertView;
    }

    public class CategoriasListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Categoria categoria = (Categoria)v.getTag();
            categoriaActual = categoria;
            //Asignar las categorias de la marca al adapter de categorias
            Log.d(TAG, "getTag() Categoria: " + categoria.toString());

            TextView item; ListView categoriasFiltro = (ListView) v.getParent();
            assert categoriasFiltro != null;
            //poner todos los views en default
            for(int i = 0, size = categoriasFiltro.getChildCount(); i <size; i++){
                item = (TextView) categoriasFiltro.getChildAt(i);
                if (item != null) {
                    item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                    item.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }

            //aplicar filtro de item seleccionado
            getMarcasAdapter().getCatalogo().filter(getMarcasAdapter().getMarcaActual().toString(), categoriaActual.toString());
            getMarcasAdapter().getAdapterCatalogo().notifyDataSetChanged();

            //aplicar estilo de item seleccionado
            v.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
            ((TextView)v).setTextColor(Color.parseColor("#3A70B9"));
        }
    }

    public MarcasAdapter getMarcasAdapter() {
        return marcasAdapter;
    }

    public void setMarcasAdapter(MarcasAdapter marcasAdapter) {
        this.marcasAdapter = marcasAdapter;
    }

    public ListView getCategoriasListView() {
        return categoriasListView;
    }

    public void setCategoriasListView(ListView categoriasListView) {
        this.categoriasListView = categoriasListView;
    }

    public void setCategorias(ArrayList<Categoria> categoriasParam) {
        categorias.clear();
        categorias.add(todas);
        categorias.addAll(categoriasParam);
        categoriaActual = todas;
        Log.d(TAG,"setCategorias Resultado: "+categorias.toString());
        this.notifyDataSetChanged();
    }

    /**
     * Devuelve la categoria seleccionada actualmente
     * @return categoria actual
     */
    public Categoria getCategoriaActual(){
        if(null == categoriaActual) return todas;
        return categoriaActual;
    }
}
