package com.grupoidea.ideaapp.models;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.grupoidea.ideaapp.R;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseRelation;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;
import android.widget.LinearLayout;

/** Clase que contiene la representaci�n de un producto*/
public class Producto {
	/** Long con el UID de serializacion*/
	private static final long serialVersionUID = 7522501988094486252L;
	/** Entero con el identificador unico del producto*/
	private String id;
	/** Cadena de texto que contiene el nombre del producto.*/
	private String nombre;
	/** Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".*/
	private static String denominacion;
	/** Numero con el precio unitario del producto.*/
	private double precio;
	/** Mapa de bits con la representaci�n visual del producto.*/
	private Bitmap imagen;
	/** String que representa el c�digo del producto para IDEA */
	private String codigo;
	/** Identificador de marca para obtener los descuentos desde parse */
	private String idMarca;
	/** Nombre de la marca del producto */
	private String nombreMarca;
	/** Clase dentro de la marca del producto */
	private String claseMarca;

    /** Objeto tipo ParseRelation para obtener los descuentos del producto */
	private ParseRelation descuentosQuery;
	/** Conjunto de descuentos: cant(cantidad)->porc(porcentaje) para el producto */
//	private ArrayList<Integer> cant;
//	private ArrayList<Double> porc;
	private SparseArray<Double> tablaDescuentos;
	
	/** Variables para uso del cat�logo (instancia) */
	
	/** Entero que almacena la cantidad de productos deseados*/
	private int cantidad;
	/** Double que representa el descuento aplicado al producto (manualmente) */
	private double descuento;

    /** Boolean que permite determinar si el menu del producto es visible al usuario*/
	private Boolean isMenuOpen;
	/** Boolean que permite determinar si el producto esta en el carrito*/
	private Boolean isInCarrito;

	/** Construye un producto con nombre y precio utilizando como denominacion "Bs." sin imagen del producto (imagen = null)
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto */
	public Producto(String id, String nombre, String codigo, double precio) {
		this(id, nombre, precio, "Bs.", null, codigo);
		isMenuOpen = false;
	}
	
	/** Construye un producto con nombre y precio sin imagen del producto (imagen = null)
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts". */
	public Producto(String id, String nombre, double precio, String denominacion) {
		this(id, nombre, precio, denominacion, null, null);
	}
	
	/** Constructor por defecto, construye un producto con nombre, precio e imagen
	 *  @param id Entero que contiene el identificador unico del producto
	 *  @param nombre Cadena de texto que contiene el nombre del producto
	 *  @param precio Numero con el precio unitario del producto
	 *  @param denominacion Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".
	 *  @param imagen  Mapa de bits con la representaci�n visual del producto.*/
	public Producto(String id, String nombre, double precio, String denominacion, Bitmap imagen, String codigo) {
		this.id = id;
		this.nombre = nombre;
		this.precio = precio;
		this.denominacion = denominacion;
		this.imagen = imagen;
		this.codigo = codigo;
		this.cantidad = 1;
		this.isInCarrito = false;
		this.tablaDescuentos = new SparseArray<Double>();
	}
	public String getDenominacion() {
		return denominacion;
	}
	public void setDenominacion(String denominacion) {
		this.denominacion = denominacion;
	}
	public int getCantidad() {
		return cantidad;
	}
	public void setCantidad(int cantidad) {
		this.cantidad = cantidad;
	}
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public double getPrecio() {
		return precio;
	}
	public void setPrecio(double precio) {
		this.precio = precio;
	}
	public Bitmap getImagen() {
		return imagen;
	}
	public void setImagen(Bitmap imagen) {
		this.imagen = imagen;
	}
	
	public Boolean getIsMenuOpen() {
		return isMenuOpen;
	}

	public void setIsMenuOpen(Boolean isMenuOpen) {
		this.isMenuOpen = isMenuOpen;
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public double getDescuento() {
		return descuento;
	}

	public void setDescuento(double descuento) {
		this.descuento = descuento;
	}
	
	public String getCodigo() {
		return codigo;
	}

	public void setCodigo(String codigo) {
		this.codigo = codigo;
	}

	public Boolean getIsInCarrito() {
		return isInCarrito;
	}

	public void setIsInCarrito(Boolean isInCarrito) {
		this.isInCarrito = isInCarrito;
	}

	public String getIdMarca() {
		return idMarca;
	}

	public void setIdMarca(String idMarca) {
		this.idMarca = idMarca;
	}

	public String getNombreMarca() {
		return nombreMarca;
	}

	public void setNombreMarca(String nombreMarca) {
		this.nombreMarca = nombreMarca;
	}

	public String getClaseMarca() {
		return claseMarca;
	}

	public void setClaseMarca(String claseMarca) {
		this.claseMarca = claseMarca;
		//setDescuentosFromParse();
	}

	/** Permite calcular el precio de los productos del mismo tipo.*/
	public double getPrecioTotal() {
		double precioTotal;
		precioTotal = precio * cantidad;
		return precioTotal;
	}
	/** Permite construir el string del precio total concatenandole al precio la denominacion*/
	public String getStringPrecioTotal() {
		return precioDenominacionToString(getPrecioTotal());
	}
	/** Permite construir el string del precio unitario concatenandole al precio la denominacion*/
	public String getStringPrecio() {
		return precioDenominacionToString(this.precio);
	}
	/** Permite agregar un item a la cantidad de productos del mismo tipo*/
	public void addCantidad() {
		this.cantidad++;
	}
	
	/** Permite disminuir un item a la cantidad de productos del mismo tipo*/
	public void substractCantidad() {
		if(cantidad-- == 0) {
			this.cantidad = 0;
		}
	}
	/** Permite construir el string de algun precio suministrado concatenandole al precio la denominacion*/
	public static String precioDenominacionToString(double precio) {
		StringBuffer stringBuffer;
		String strValue = null;
		
		strValue = Double.toString(precio);
		stringBuffer = new StringBuffer(strValue).append(" ").append(denominacion);
		if(stringBuffer != null) {
			strValue = stringBuffer.toString();
		}
		return strValue;
	}
	/** Convierte el objeto producto en JSONObject 
	 * @throws JSONException */
	public JSONObject toJSON() throws JSONException{
		JSONObject productoJSON = new JSONObject();
		
		productoJSON.put("nombre", getNombre());
		productoJSON.put("id", getId());
		productoJSON.put("codigo", getCodigo());
		productoJSON.put("precio", getPrecio());
		productoJSON.put("cantidad", getCantidad());
		productoJSON.put("total", getPrecioTotal());
		productoJSON.put("descuento", getDescuento());
		
		return productoJSON;
	}
	
	public void setCantidadDescuento(int c, double d){
		tablaDescuentos.append(c, d);
//		cant.add(c);
//		porc.add(d);
	}
	
	public double getDescuentoCantidad(){

        int size = tablaDescuentos.size(), key;

        for( int i = 0; i<size; i++){
            key= tablaDescuentos.keyAt(i);
            if( key >= cantidad){
                return tablaDescuentos.valueAt(key);
            }
        }

//		for (Integer cantidades : cant) {
//			if (cantidad >= cantidades) {
//				descuento = porc.get(cant.indexOf(cantidades));
//			}
//		}
		
		return 0.0;
	}
	
	public int getCountDescuentos(){
        return tablaDescuentos.size();
//		return cant.size();
	}
	
	public String getStringDescuento(int index){
		String texto = "";
        texto = ">"+tablaDescuentos.keyAt(index)+" : "+tablaDescuentos.valueAt(index)+"%";
//		texto = ">"+cant.get(index)+" : "+porc.get(index)+"%";
		return texto;
	}
	
	public void setDescuentosFromParse(){

        ParseQuery query = new ParseQuery("Marca");
        query.include("descuentos");
        query.whereEqualTo("nombre", nombreMarca);
        query.whereEqualTo("clase",claseMarca);
		query.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> arg0, ParseException e) {
                Log.d("setDescuentosFromParse", String.valueOf(arg0.size()));
                if(e == null && arg0 != null){
                    for (ParseObject descuentos : arg0) {
                        tablaDescuentos.append(descuentos.getInt("cantidad"),descuentos.getDouble("descuento"));
                    }
                }
            }
        });
    }

//		//ParseRelation desctos = marca.getRelation("descuentos");
//		getDescuentosQuery().getQuery().findInBackground(new FindCallback() {
//			@Override
//			public void done(List<ParseObject> arg0, ParseException arg1) {
//				if(arg1 == null && arg0 != null){
//					for (ParseObject descuentos : arg0) {
//						//prod.setCantidadDescuento(parseObject.getInt("cantidad"), parseObject.getDouble("descuento"));
//                        tablaDescuentos.append(descuentos.getInt("cantidad"),descuentos.getDouble("descuento"));
////                        cant.add(descuentos.getInt("cantidad"));
////						porc.add(descuentos.getDouble("descuento"));
//					}
//				}
//			}
//		});
//	}

	public ParseRelation getDescuentosQuery() {
		return descuentosQuery;
	}

	public void setDescuentosQuery(ParseRelation descuentosQuery) {
		this.descuentosQuery = descuentosQuery;
	}

    public ArrayList<String> getDescuentosString(){
        ArrayList<String> descuentos = new ArrayList<String>();
        int size = descuentos.size();
        for (int i=0; i<size; i++){
            descuentos.add(getStringDescuento(i));
        }
        Log.d("getDescuentosString Result", descuentos.toString());
        return descuentos;
    }
}
