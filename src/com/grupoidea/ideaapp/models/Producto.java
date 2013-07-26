package com.grupoidea.ideaapp.models;

import android.graphics.Bitmap;
import android.util.SparseArray;
import com.parse.ParseObject;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.ArrayList;

/** Clase que contiene la representaci�n de un producto*/
public class Producto {
	/** Long con el UID de serializacion*/
	private static final long serialVersionUID = 7522501988094486252L;

    private ParseObject productoParse;
    /** Entero con el identificador unico del producto*/
	private String id;
	/** Cadena de texto que contiene el nombre del producto.*/
	private String nombre;
	/** Cadena de texto que contiene la denominacion de la moneda y/o unidad de medida. ej: "Bs", "$", "pts".*/
	private static String denominacion;
	/** Numero con el precio unitario del producto.*/
	private double precio;
    /** Precio del producto con descuento comercial del cliente*/
    private double precioComercial;
	/** Mapa de bits con la representaci�n visual del producto.*/
	private Bitmap imagen;
	/** String que representa el c�digo del producto para IDEA */
	private String codigo;
	/** Identificador de categoria para obtener los descuentos desde parse */
	private String idCategoria;
	/** Nombre de la marca del producto */
	private String marca;
	/** Clase dentro de la marca del producto */
	private String categoria;

    private GrupoCategoria grupoCategoria;

	/** Conjunto de descuentos: cant(cantidad)->porc(porcentaje) para el producto */
	private SparseArray<Double> tablaDescuentos;
	
	/** Variables para uso del cat�logo (instancia) */

    /**Entero que almacena la existencia del producto en el servidor*/
    private int existencia;
	/** Entero que almacena la cantidad de productos deseados*/
	private int cantidad;

    private int excedente;

    private int cantidadDescuentosGroup;
	/** Double que representa el descuentoManual aplicado al producto (manualmente)*/
	private double descuentoManual;
    /** Double que representa el descuento aplicado al producto segun cantidad y categoria*/
    private double descuentoAplicado;
    private double iva;

    /** Boolean que permite determinar si el menu del producto es visible al usuario*/
	private Boolean isMenuOpen;
	/** Boolean que permite determinar si el producto esta en el carrito*/
	private Boolean isInCarrito;
    public static DecimalFormat df = new DecimalFormat("###,###,##0.##");

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
        this.precioComercial = precio;
		this.denominacion = denominacion;
		this.imagen = imagen;
		this.codigo = codigo;
		this.cantidad = 1;
        this.descuentoManual = 0.0;
        this.descuentoAplicado = 0.0;
        this.iva = 0.0;
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
		this.descuentoManual = descuentoManual/100.0;
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

	public String getIdCategoria() {
		return idCategoria;
	}

	public void setIdCategoria(String idCategoria) {
		this.idCategoria = idCategoria;
	}

	public String getMarca() {
		return marca;
	}

	public void setMarca(String marca) {
		this.marca = marca;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
		//setDescuentosFromParse();
	}

    /** Permite calcular el precio comercial de los productos del mismo tipo.*/
    public double getPrecioComercialTotal() {
        double precioTotal, desc= 1.0 - getDescuentoAplicado();
        precioTotal = cantidad * precioComercial * desc;
//        Log.d("DEBUG", "getPrecioTotal= "+String.valueOf(cantidad)+" * "+String.valueOf(precioComercial)+" * "+String.valueOf(desc)+" = "+String.valueOf(precioTotal));
        return precioTotal;
    }
    /** Permite construir el string del precio comercial total concatenandole al precio la denominacion*/
    public String getStringPrecioComercialTotal() {
        return precioDenominacionToString(getPrecioComercialTotal());
    }

    /** Permite construir el string del precio unitario concatenandole al precio la denominacion*/
    public String getStringPrecioComercial() {
        return precioDenominacionToString(getPrecioComercial());
    }

    public String getStringPrecioDescuento() {
        return precioDenominacionToString(getPrecioComercial()-(getPrecioComercial()*getDescuentoAplicado()));
    }

	/** Permite construir el string del precio unitario concatenandole al precio la denominacion*/
	public String getStringPrecio() {
		return precioDenominacionToString(getPrecio());
	}
	/** Permite agregar un item a la cantidad de productos del mismo tipo*/
	public void addCantidad() {
		if(cantidad < existencia+excedente){
            this.cantidad++;
            setDescuentoAplicado(calcularDescuentoAplicado());
        }
	}
	
	/** Permite disminuir un item a la cantidad de productos del mismo tipo*/
	public void substractCantidad() {
		if(cantidad-- == 0) {
			this.cantidad = 0;
		}
        setDescuentoAplicado(calcularDescuentoAplicado());
	}

	/** Permite construir el string de algun precio suministrado concatenandole al precio la denominacion*/
	public static String precioDenominacionToString(double precio) {
        return ""+df.format(precio)+" "+denominacion;
	}
	/** Convierte el objeto producto en JSONObject 
	 * @throws JSONException */
	public JSONObject toJSON() throws JSONException{
		JSONObject productoJSON = new JSONObject();
		
		productoJSON.put("nombre", getNombre());
		productoJSON.put("id", getId());
		productoJSON.put("codigo", getCodigo());
        productoJSON.put("precio", getPrecio());
        productoJSON.put("precioComercial", getPrecioComercial());
		productoJSON.put("cantidad", getCantidad());
		productoJSON.put("totalComercial", getPrecioComercialTotal());
        productoJSON.put("descuentoManual", getDescuentoManual());
        productoJSON.put("descuentoAplicado", getDescuentoAplicado());
        productoJSON.put("IVA", getIva());
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
            producto.setPrecioComercial(json.getDouble("precioComercial"));
            producto.setDescuentoManual(json.getDouble("descuentoManual"));
            producto.setDescuentoAplicado(json.getDouble("descuentoAplicado"));
            producto.setIva(json.getDouble("IVA")*100.0);

            return producto;
        }catch(JSONException e){
            return null;
        }
    }
	
	public void setCantidadDescuento(int c, double d){
		tablaDescuentos.append(c, d);
	}
	
	public double calcularDescuentoAplicado(){
        if(descuentoManual == 0.0){
            int key;
            for( int i = tablaDescuentos.size()-1; i>=0; i--){
                key = tablaDescuentos.keyAt(i);
                if( key != 0 && key<= cantidad){
                    return tablaDescuentos.valueAt(i)/100.0;
                }
            }
        }else{
            return descuentoManual /100.0;
        }
        return 0.0;
	}

    public String getDescuentoAplicadoString(){
        return df.format(getDescuentoAplicado() * 100.0);
    }
	
	public int getCountDescuentos(){
        return tablaDescuentos.size();
	}
	
	public String getStringDescuentoAt(int index){
		String texto = ">"+(tablaDescuentos.keyAt(index)-1)+" : "+tablaDescuentos.valueAt(index)+"%";
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

    public ParseObject getProductoParse() {
        return productoParse;
    }

    public void setProductoParse(ParseObject productoParse) {
        this.productoParse = productoParse;
    }

    /** Double que representa el descuento aplicado al producto por cantidad**/
    public double getDescuentoAplicado() {
        if(descuentoManual == 0.0){
            return descuentoAplicado;
        }else{
            return descuentoManual;
        }
    }

    public String getDescuentoAplicadoPorcString(){
        return ""+df.format(getDescuentoAplicado() * 100.0)+"%";
    }

    public void setDescuentoAplicado(double descuentoAplicado) {
        this.descuentoAplicado = descuentoAplicado;
    }

    public double getPrecioComercial() {
        return precioComercial;
    }

    public void setPrecioComercial(double precioComercial) {
        this.precioComercial = precioComercial;
    }

    public double getIva() {
        return iva;
    }

    public void setIva(double iva) {
        this.iva = iva/100.0;
    }

    public String getPrecioComercialConIva(){
        return precioDenominacionToString(getPrecioComercial() + (getPrecioComercial()*getIva()));
    }

    public String getPrecioComercialSinIvaConIvaString(){
        return getStringPrecioComercial()+" / "+getPrecioComercialConIva();
    }

    public String getPrecioDescuentoConIva(){
        double precio = getPrecioComercial();
        precio = precio - (precio*getDescuentoAplicado());
        return precioDenominacionToString(precio + (precio*getIva()));
    }

    public String getPrecioCarritoSinIvaConIvaString(){
        return getStringPrecioDescuento()+" / "+getPrecioDescuentoConIva();
    }

    public int getCantidadDescuentosGroup() {
        return cantidadDescuentosGroup;
    }

    public void setCantidadDescuentosGroup(int cantidadDescuentosGroup) {
        this.cantidadDescuentosGroup = cantidadDescuentosGroup;
    }

    public boolean isInCategoryGroup(ArrayList<String> grupoCategorias){
        for(int i=0, size=grupoCategorias.size(); i<size; i++){
            if(grupoCategorias.get(i).equalsIgnoreCase(this.getCategoria())){
                return true;
            }
        }
        return false;
    }

    public int getExcedente() {
        return excedente;
    }

    public void setExcedente(int excedente) {
        this.excedente = excedente;
    }

    public ArrayList<String> getRelacionadas(){
        if(null != getGrupoCategoria())
            return getGrupoCategoria().getRelacionadas();
        else
            return  null;
    }

    public void setRelacionadas(JSONArray related){
        getGrupoCategoria().setRelacionadasJSONArray(related);
    }

    public void setRelacionadas(ArrayList<String> related){
        getGrupoCategoria().setRelacionadas(related);
    }

    public GrupoCategoria getGrupoCategoria() {
        if(null!= grupoCategoria)
            return grupoCategoria;
        else
            return null;
    }

    public void setGrupoCategoria(GrupoCategoria grupoCategoria) {
        this.grupoCategoria = grupoCategoria;
    }
}
