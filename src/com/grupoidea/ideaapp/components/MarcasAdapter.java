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
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Marca;

import java.util.ArrayList;

/**
 * Created by fernando on 1/3/14.
 */
public class MarcasAdapter extends BaseAdapter {
    protected String TAG = this.getClass().getSimpleName();
    protected ArrayList<Marca> marcas;
    private ListView marcasListView;
    public Marca todas;
    protected CategoriasAdapter categoriasAdapter;
    protected Context mContext;
    public Marca marcaActual;
    private Catalogo catalogo;
    private BannerProductoCatalogo adapterCatalogo;

    public MarcasAdapter(){
        marcas = new ArrayList<Marca>();
    }

    public MarcasAdapter(Context mContextParam){
        this();
        mContext = mContextParam;
        //Agregar item de "TODAS"
        todas = new Marca(mContext.getString(R.string.todas));
        marcas.add(todas);
        marcaActual = todas;
    }

    public MarcasAdapter(Context mContextParam, CategoriasAdapter adapter){
        this(mContextParam);
        categoriasAdapter = adapter;
    }

    @Override
    public int getCount() {
        return marcas.size();
    }

    @Override
    public Object getItem(int i) {
        return marcas.get(i);
    }

    @Override
    public long getItemId(int i) {
        return marcas.get(i).hashCode();
    }

    @Override
    public View getView(int i, View convertView, ViewGroup viewGroup) {

        //Inflar el View si no existe
        if(convertView == null)
            convertView = ((LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.filtro_menu_item_layout, null);

        assert convertView != null;
        TextView tv = (TextView) convertView.findViewById(R.id.filtro_menu_item_textView);
        if(null == tv.getTag() || !tv.getTag().equals(marcas.get(i))){
            tv.setTag(marcas.get(i));
            tv.setText(marcas.get(i).toString());
            tv.setOnClickListener(new MarcasListener());
        }

        if(marcaActual.equals(todas) && marcas.get(i).equals(todas)){
            //aplicar estilo de item seleccionado
            tv.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
            tv.setTextColor(Color.parseColor("#3A70B9"));
        }

        return convertView;
    }

    public ListView getMarcasListView() {
        return marcasListView;
    }

    public void setMarcasListView(ListView marcasListView) {
        this.marcasListView = marcasListView;
    }

    public class MarcasListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            Marca marca = (Marca)v.getTag();
            marcaActual = marca;
            //Asignar las categorias de la marca al adapter de categorias
            Log.d(TAG, "getTag() Marca: " + marca.toString());
            categoriasAdapter.setCategorias(marca.getCategorias());

            TextView item; ListView marcasFiltro = (ListView) v.getParent();
            assert marcasFiltro != null;
            //poner todos los views en default
            for(int i = 0, size = marcasFiltro.getChildCount(); i <size; i++){
                item = (TextView) marcasFiltro.getChildAt(i);
                if (item != null) {
                    item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                    item.setTextColor(Color.parseColor("#FFFFFF"));
                }
            }

            //aplicar filtro de item seleccionado
            getCatalogo().filter(getMarcaActual().toString(), categoriasAdapter.getCategoriaActual().toString());
            getAdapterCatalogo().notifyDataSetChanged();

            //aplicar estilo de item seleccionado
            v.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
            ((TextView)v).setTextColor(Color.parseColor("#3A70B9"));
        }
    }

    /**
     * Funcion que intenta agregar un objeto <code>Marca</code> a <code>marcas</code>
     * de no existir la misma la crea, en cualquier caso devuelve el objeto <code>Marca</code>
     * correspondiente al <code>nombreMarca</code> especificado
     * @param nombreMarca nombre de la <code>Marca</code> a agregar
     * @return Objeto <code>Marca</code>
     */
    public Marca addMarca(String nombreMarca){
        for(Marca marca : marcas){
            //Si la Marca existe devolverla
            if(nombreMarca.equalsIgnoreCase(marca.toString())){
                return marca;
            }
        }
        //Si la Marca no existe
        Marca newMarca = new Marca(nombreMarca);
        marcas.add(newMarca);
        return newMarca;
    }

    /**
     * Devuelve la marca seleccionada actualmente
     * @return Instancia de la <code>Marca</code> actual
     */
    public Marca getMarcaActual(){
        if(null == marcaActual) return todas;
        return marcaActual;
    }

    public Catalogo getCatalogo() {
        return catalogo;
    }

    public void setCatalogo(Catalogo catalogo) {
        this.catalogo = catalogo;
    }

    public BannerProductoCatalogo getAdapterCatalogo() {
        return adapterCatalogo;
    }

    public void setAdapterCatalogo(BannerProductoCatalogo adapterCatalogo) {
        this.adapterCatalogo = adapterCatalogo;
    }
}
