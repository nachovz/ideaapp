package com.grupoidea.ideaapp.models;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.SparseArray;

import com.parse.ParseRelation;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

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
	private SparseArray<Double> tablaDescuentos;
	
	/** Variables para uso del cat�logo (instancia) */

    /**Entero que almacena la existencia del producto en el servidor*/
    private int existencia;
	/** Entero que almacena la cantidad de productos deseados*/
	private int cantidad;
	/** Double que representa el descuentoManual aplicado al producto (manualmente) */
	private double descuentoManual;
    protected static DecimalFormat df = new DecimalFormat("#.##");

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
        this.descuentoManual = 0.0;
		this.isInCarrito = false;
		this.tablaDescuentos = new SparseArray<Double>();
	}
	public String getDenominacion() {
		return denominacion;
	}
	public void setDenominacion(String denominacion) {
		this.denominacion = denominacion;
	}

    public int getExistencia() {
        return existencia;
    }
    public void setExistencia(int existencia) {
        this.existencia = existencia;
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

    /** Funcion que devuelve el precio del producto (tomando en cuenta si un descuentoManual manual fue aplicado)
     * @return Precio del producto
     */
    public double getPrecio(){
        if(descuentoManual == 0){
            return precio;
        }else{
            return precio * descuentoManual /100.0;
        }
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
	
	public double getDescuentoManual() {
		return descuentoManual;
	}

	public void setDescuentoManual(double descuentoManual) {
		this.descuentoManual = descuentoManual;
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
		double precioTotal, desc= 1.0 - getDescuentoAplicado();
        precioTotal = cantidad * precio * desc;
        Log.d("DEBUG", "getPrecioTotal= "+String.valueOf(cantidad)+" * "+String.valueOf(precio)+" * "+String.valueOf(desc)+" = "+String.valueOf(precioTotal));
		return precioTotal;
	}
	/** Permite construir el string del precio total concatenandole al precio la denominacion*/
	public String getStringPrecioTotal() {
		return precioDenominacionToString(getPrecioTotal());
	}
	/** Permite construir el string del precio unitario concatenandole al precio la denominacion*/
	public String getStringPrecio() {
		return precioDenominacionToString(getPrecio());
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
        strValue = df.format(precio);

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
		productoJSON.put("descuentoManual", getDescuentoManual());
		
		return productoJSON;
	}

    public static Producto setFromJSON(JSONObject json){
        try{
            String id = json.getString("id");
            String nombre = json.getString("nombre");
            Double precio = json.getDouble("precio");
            Producto producto = new Producto(id, nombre, precio, "Bs.");
            producto.setCodigo(json.getString("codigo"));
            producto.setCantidad(json.getInt("cantidad"));
            producto.setDescuentoManual(json.getDouble("descuentoManual"));

            return producto;
        }catch(JSONException e){
            return null;
        }
    }
	
	public void setCantidadDescuento(int c, double d){
		tablaDescuentos.append(c, d);
	}
	
	public double getDescuentoAplicado(){
        if(descuentoManual ==0){
            int key;
            for( int i = tablaDescuentos.size()-1; i>=0; i--){
                key = tablaDescuentos.keyAt(i);
                if( key <= cantidad){
                    return tablaDescuentos.valueAt(i)/100.0;
                }
            }
        }else{
            return descuentoManual /100.0;
        }
        return 0.0;
	}
	
	public int getCountDescuentos(){
        return tablaDescuentos.size();
	}
	
	public String getStringDescuentoAt(int index){
		String texto = ">"+tablaDescuentos.keyAt(index)+" : "+tablaDescuentos.valueAt(index)+"%";
		return texto;
	}

    public ArrayList<String> getDescuentosString(){
        ArrayList<String> descuentos = new ArrayList<String>();
        for (int i=0, size = tablaDescuentos.size(); i<size; i++){
            descuentos.add(getStringDescuentoAt(i));
        }
        return descuentos;
    }

    public void setTablaDescuentos(SparseArray<Double> tablaDescuentos){
        this.tablaDescuentos=tablaDescuentos;
    }

	public void setDescuentosFromParse() {
        tablaDescuentos.clear();
//        final ParseQuery query = new ParseQuery("Marca");
//        query.findInBackground(new FindCallback() {
//            public void done(List<ParseObject> marcas, ParseException e) {
//                if(e == null && marcas != null){
//                    Log.d("DEBUG", "Marcas obtenidas: "+String.valueOf(marcas.size()));
//                    for (ParseObject marcaObj : marcas) {
//                        ParseQuery descuentosQuery = marcaObj.getRelation("descuentos").getQuery();
//                        descuentosQuery.findInBackground(new FindCallback() {
//                            public void done(List<ParseObject> descuentos, ParseException e) {
//                                if(e == null && descuentos != null){
//                                    Log.d("DEBUG", "Descuentos recuperados para este producto: "+descuentos.size());
//                                    int pos=0;
//                                    for(ParseObject descuentoManual: descuentos){
//                                        tablaDescuentos.append(descuentoManual.getInt("cantidad"),descuentoManual.getDouble("porcentaje"));
//                                        pos = tablaDescuentos.size()-1;
//                                        Log.d("DEBUG","cant:"+tablaDescuentos.keyAt(pos)+" %:"+tablaDescuentos.valueAt(pos));
//                                    }
//                                }else{
//                                    Log.d("DEBUG", "No se recuperaron descuentos para este producto");
//                                }
//                            }
//                            });
//                    }
//                }else{
//                    Log.d("DEBUG", "result: null");
//                }
//            }
//        });
    }

	public ParseRelation getDescuentosQuery() {
		return descuentosQuery;
	}

	public void setDescuentosQuery(ParseRelation descuentosQuery) {
		this.descuentosQuery = descuentosQuery;
	}
}
