package com.grupoidea.ideaapp.models;

import android.util.Log;
import android.util.SparseArray;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;

public class GrupoCategorias {
    private String nombre;
    private ArrayList<String> relacionadas;
    private SparseArray<Double> tablaDescuentos;
    private int cantItemsCarrito, prevCantDesc, nextCantDesc;
    private double descActual;

    public GrupoCategorias(){
        relacionadas = new ArrayList<String>();
        tablaDescuentos = new SparseArray<Double>();
        nextCantDesc = 0;
        prevCantDesc = 1000;
        descActual = 0.0;
        cantItemsCarrito = 1;
    }

    /**
     * Constructor con nombre para el grupo
     * @param nombre String
     */
    public GrupoCategorias(String nombre){
        this();
        this.nombre = nombre;
    }

    public GrupoCategorias(String nombre, JSONArray relacionadas){
        this(nombre);
        setRelacionadasJSONArray(relacionadas);
    }

    /**
     * Getter de categorias relacionadas
     * @return <code>ArrayList<String></code> de <code>String</code> con nombres
     */
    public ArrayList<String> getRelacionadas() {
        return relacionadas;
    }

    /**
     * Setter de <code>ArralysList</code> de categorias relacionadas
     * @param relacionadas
     */
    public void setRelacionadas(ArrayList<String> relacionadas) {
        this.relacionadas = relacionadas;
    }

    /**
     * Setter de <code>JSONArray</code> de categorias relacionadas
     * @param jsonArray
     */
    public void setRelacionadasJSONArray(JSONArray jsonArray) {
        try{
            ArrayList<String> list = new ArrayList<String>();
            if (jsonArray != null) {
                int len = jsonArray.length();
                for (int i=0;i<len;i++){
                    list.add(jsonArray.get(i).toString());
                }
                this.relacionadas = list;
            }else{
                this.relacionadas = null;
            }
        }catch(JSONException e){
            this.relacionadas = null;
        }
    }

    /** Devuelve un <code>SparseArray</code> de <code>Doubles</code> con el conjunto de descuentos (cantidad,porcentaje) del grupo */
    public SparseArray<Double> getTablaDescuentos() {
        return tablaDescuentos;
    }

    /**
     * Establece el <code>SparseArray</code> con el conjunto de descuentos (cantidad,porcentaje) del grupo
     * @param tablaDescuentos <code>SparseArray</code> de descuentos
     */
    public void setTablaDescuentos(SparseArray<Double> tablaDescuentos) {
        this.tablaDescuentos = tablaDescuentos;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCantItemsCarrito(int cant){
        if (cant >= 0) {
            cantItemsCarrito = cant;
//            Log.d("DEBUG", "prev: " + prevCantDesc + " act: " + cantItemsCarrito + " prox: " + nextCantDesc);
//            if (cant > nextCantDesc || cant < prevCantDesc) {
//                Log.d("DEBUG", "Recalculando descuento categoria");
//                calcularDescuento();
//            }
            if(cant == 0){
                descActual = 0.0;
            }
        }
    }

    public int getCantItemsCarrito(){
        return cantItemsCarrito;
    }

    public void addCantidad(){
        setCantItemsCarrito(cantItemsCarrito+1);
    }

    public void substractCantidad(){
        setCantItemsCarrito(cantItemsCarrito-1);
    }

    public void addCantidad(int cant) {
        setCantItemsCarrito(cantItemsCarrito + cant);
    }

    public void substractCantidad(int cant) {
        setCantItemsCarrito(cantItemsCarrito - cant);
    }

    /**
     * Devuelve el descuento especificado como un String en formato ">cant : desc%"
     * @param index posicion del descuento
     * @return <code>String</code> de descuento
     */
    public String getStringDescuentoAt(int index){
        return ">"+(tablaDescuentos.keyAt(index)-1)+" : "+tablaDescuentos.valueAt(index)+"%";
    }

    /**
     * Devuelve todos los descuentos como un <code>ArrayList</code> de String en formato ">cant : desc%"
     * @return <code>ArrayList</code> de descuentos
     */
    public ArrayList<String> getDescuentosString(){
        ArrayList<String> descuentos = new ArrayList<String>();
        for (int i=0, size = tablaDescuentos.size(); i<size; i++){
            descuentos.add(getStringDescuentoAt(i));
        }
        return descuentos;
    }

    public boolean hasDescuentos(){
        if(tablaDescuentos.size()>0){
            return true;
        }else{
            return false;
        }
    }

    /**
     * Devuelve el descuento aplicado a la cantidad especificada en el parámetro
     * @param cant Cantidad de productos de la categoria
     * @return Descuento aplicado
     */
    public double calcularDescuentoAplicado(int cant){
        int key;
        for( int i = tablaDescuentos.size()-1; i>=0; i--){
            key = tablaDescuentos.keyAt(i);
            if( key != 0 && key<= cant){
                return tablaDescuentos.valueAt(i);
            }
        }
        return 0.0;
    }

    public void calcularDescuento() {
        int key;
        if(cantItemsCarrito >= nextCantDesc || cantItemsCarrito < prevCantDesc){
            descActual = 0.0;
            for (int i = tablaDescuentos.size() - 1; i >= 0; i--) {
                key = tablaDescuentos.keyAt(i);
                if (key != 0 && key <= cantItemsCarrito) {
                    //guardar cantidad previa y siguiente
                    if((i-1) > -1) prevCantDesc = tablaDescuentos.keyAt(i - 1);
                    if((i+1) < tablaDescuentos.size()) nextCantDesc = tablaDescuentos.keyAt(i + 1);
                    //establecer descuento para cantidad actual
                    descActual = tablaDescuentos.valueAt(i);
                    Log.d("DEBUG", "key: "+String.valueOf(tablaDescuentos.keyAt(i)));
                    break;
                }
            }
        }
    }

    public double getDescActual() {
        return descActual;
    }
}
