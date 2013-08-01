package com.grupoidea.ideaapp.models;

import android.util.SparseArray;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

/**
 * Created by geeks on 08/07/13.
 */
public class GrupoCategoria {
    private String nombre;
    private ArrayList<String> relacionadas;
    private SparseArray<Double> tablaDescuentos;

    public GrupoCategoria(){
        relacionadas = new ArrayList<String>();
        tablaDescuentos = new SparseArray<Double>();
    }

    public ArrayList<String> getRelacionadas() {
        return relacionadas;
    }

    public void setRelacionadas(ArrayList<String> relacionadas) {
        this.relacionadas = relacionadas;
    }

    public void setRelacionadasJSONArray(JSONArray jsonArray) {
        try{
            ArrayList<String> list = new ArrayList<String>();
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    list.add(jsonArray.get(i).toString());
//                    Log.d("DEBUG", "json: "+jsonArray.get(i).toString());
                }
                this.relacionadas = list;
            }else{
                this.relacionadas = null;
            }
        }catch(JSONException e){
            this.relacionadas = null;
        }
    }

    /** Conjunto de descuentos: cant(cantidad)->porc(porcentaje) para el producto */
    public SparseArray<Double> getTablaDescuentos() {
        return tablaDescuentos;
    }

    public void setTablaDescuentos(SparseArray<Double> tablaDescuentos) {
        this.tablaDescuentos = tablaDescuentos;
    }

    public double calcularDescuentoAplicado(int cant){
            int key;
//        Log.d("DEBUG", "tabla size:"+ tablaDescuentos.size());
            for( int i = tablaDescuentos.size()-1; i>=0; i--){
                key = tablaDescuentos.keyAt(i);
//                Log.d("DEBUG", "descuentos: cant: "+key+" desc:"+tablaDescuentos.valueAt(i)/100.0);
                if( key != 0 && key<= cant){
                    return tablaDescuentos.valueAt(i)/100.0;
                }
            }
        return 0.0;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
}
