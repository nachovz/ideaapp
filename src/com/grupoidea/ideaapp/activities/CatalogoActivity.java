package com.grupoidea.ideaapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.*;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.*;
import com.parse.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CatalogoActivity extends ParentMenuActivity {
	/** Elemento que permite mostrar Views en forma de grid.*/
	private GridView grid;
	/** Cadena de texto que contiene el nombre del cliente*/
	private String clienteNombre;
	/** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
	private static ArrayList<Producto> catalogoProductos;
    /** Objeto que representa al catalogo.*/
    private static Catalogo catalogo;
	/** Objeto que representa al carrito de compras del catalogo.*/
	private Carrito carrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del carrito y el layout de cada producto*/
	public static BannerProductoCarrito adapterCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	public static BannerProductoCatalogo adapterCatalogo;
    public String modificarPedidoId, modificarPedidoNum;
    /** Minimo y maximo valor para descuentos manueales*/
    public final static Double MIN_DESC_MAN = 0.0, MAX_DESC_MAN = 100.0;
    public String lastprodName;
    public String categoriaActual="Todas", marcaActual="Todas";
    public static ArrayList<String> marcas, categorias;
    public static ArrayAdapter marcasAdapter, categoriasAdapter;
    public LinearLayout categoriasFiltro, marcasFiltro;
    protected static  Context mContext;
	
	public CatalogoActivity() {
		super(true, true, true, true); //hasCache (segundo param) :true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
        mContext = getBaseContext();
        super.instanceContext = mContext;

        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

        //Dialogo de carga catalogo
        catalogoProgressDialog = new ProgressDialog(this);
        catalogoProgressDialog.setTitle("Cargando...");
        catalogoProgressDialog.setMessage("Cargando Catalogo, por favor espere...");
        catalogoProgressDialog.setIndeterminate(true);
        catalogoProgressDialog.setCancelable(false);
        catalogoProgressDialog.show();

        //Dialogo de carga carrito
        carritoProgressDialog = new ProgressDialog(this);
        carritoProgressDialog.setTitle("Cargando...");
        carritoProgressDialog.setMessage("Cargando Carrito, por favor espere...");
        carritoProgressDialog.setIndeterminate(true);
        carritoProgressDialog.setCancelable(false);

        modificarPedidoId= getIntent().getExtras().getString("idPedido");
        modificarPedidoNum= getIntent().getExtras().getString("numPedido");
        clienteNombre= getIntent().getExtras().getString("clienteNombre");

		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.catalogo_layout);

        //Poblar Spinner de Clientes e inflar
        adapter = getClientesFromParse();
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        clienteSpinner.setAdapter(adapter);
        clienteSpinner.setEnabled(true);
        clienteSpinner.setVisibility(View.VISIBLE);
        clienteSpinner.setSelection(0);
        clienteSelected=0;
        clienteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clienteSelected = i;
                Log.d("DEBUG", "Cliente seleccionado: " + i);
                updatePreciosComerciales();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        categorias = new ArrayList<String>();
        marcas = new ArrayList<String>();
        categoriaActual = getString(R.string.todas);
        marcaActual = getString(R.string.todas);
//        Log.d("DEBUG", "marca actual: "+ marcaActual+" categoria actual: "+categoriaActual);
	}

    /**
     * Instanciar nuevo producto en base a un ParseObject
     * @param producto producto obtenido de Parse
     * @return Instancia de Clase Producto
     */
	private Producto retrieveProducto(final ParseObject producto){
		String codigo = producto.getString("codigo");
		String nombre = producto.getString("nombre");
		double precio = producto.getDouble("costo");
		final String objectId = producto.getObjectId();
		final Producto prod = new Producto(objectId,codigo, nombre, precio);

        if(producto.getString("picture")!= null && !producto.getString("picture").isEmpty()){
            ImageDownloadTask imgDownloader = new ImageDownloadTask(prod);
            imgDownloader.execute(producto.getString("picture"));
        }

        //asignar IVA
        prod.setIva(producto.getParseObject("iva").getDouble("porcentaje"));
        prod.setExcedente(producto.getInt("excedente"));

        //Obtener categoria
        ParseObject categoria = producto.getParseObject("categoria");
		prod.setMarca(producto.getString("marca"));
        addMarca(producto.getString("marca"));
		prod.setCategoria(categoria.getString("nombre"));
        addCategoria(categoria.getString("nombre"));
        prod.setIdCategoria(categoria.getObjectId());

        //Obtener descuentos
        final SparseArray<Double> tablaDescuentos= new SparseArray<Double>();
        ParseQuery descuentosQuery = categoria.getRelation("descuentos").getQuery();
        descuentosQuery.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> descuentos, ParseException e) {
                if (e == null && descuentos != null) {
                    for (ParseObject descuento : descuentos) {
                        tablaDescuentos.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));

//                        //Quitar el dialogo con el ultimo descuento del ultimo producto
//                        if(descuento.equals(descuentos.get(descuentos.size()-1)) && lastprodName.equalsIgnoreCase(prod.getNombre())){
//                            Log.d("DEBUG", "Finalizada carga de productos");
//                            catalogoProgressDialog.dismiss();
//                        }
                    }
                }
            }
        });
        prod.setTablaDescuentos(tablaDescuentos);

        if(null != producto.getParseObject("grupo_categorias") && null != producto.getParseObject("grupo_categorias").getJSONArray("relacionadas")){
            //almacenar grupo categoria
            GrupoCategoria grupo = new GrupoCategoria();
            grupo.setRelacionadasJSONArray(producto.getParseObject("grupo_categorias").getJSONArray("relacionadas"));

            //obtener descuentos por grupo de categoria
            final SparseArray<Double> tablaDescuentosGrupo = new SparseArray<Double>();
            descuentosQuery = producto.getParseObject("grupo_categorias").getRelation("descuentos").getQuery();
            descuentosQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> descuentos, ParseException e) {
                    if (e == null && descuentos != null) {
                        for (ParseObject descuento : descuentos) {
                            tablaDescuentosGrupo.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
                        }
                    }
                }
            });
            grupo.setTablaDescuentos(tablaDescuentosGrupo);
            prod.setGrupoCategoria(grupo);
        }

        //Obtener existencia
        ParseQuery queryExistencia = new ParseQuery("Metas");
        queryExistencia.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryExistencia.whereEqualTo("producto", producto);
        queryExistencia.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null){
                    int exist = parseObject.getInt("meta")-parseObject.getInt("pedido")-parseObject.getInt("facturado");
                    if(exist >0){
                        prod.setExistencia(exist);
                    }else{
                        prod.setExistencia(0);
                    }
                    //Quitar el dialogo con el ultimo descuento del ultimo producto
                    if(lastprodName.equalsIgnoreCase(prod.getNombre())){
                        Log.d("DEBUG", "Finalizada carga de productos");
                        catalogoProgressDialog.dismiss();
                    }
                }

                else Log.d("DEBUG", "No existe registro del producto "+objectId+" en la tabla Metas");
            }
        });

        return prod;
	}
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		@SuppressWarnings("unchecked")
		List<ParseObject> productosParse = (List<ParseObject>) response.getResponse();
		ArrayList<Producto> productos = new ArrayList<Producto>();
		Producto producto;
		RelativeLayout menuRight, menuLeft;
		RelativeLayout relativeLayout;
		ListView listCarrito = null;

        lastprodName=null;
        //cargar productos desde Parse
		for (ParseObject parseObject : productosParse) {
			producto = retrieveProducto(parseObject);
			productos.add(producto);
            if(parseObject.equals(productosParse.get(productosParse.size()-1))){
                lastprodName=producto.getNombre();
            }
		}


        menuLeft = (RelativeLayout) getMenuLeft();
        if(menuLeft != null){
            //onCLick en Categorias
            categoriasFiltro = (LinearLayout) findViewById(R.id.categorias_filtro_layout);
            View child = categoriasFiltro.getChildAt(1);
            if(child!= null){
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //poner todos los views en default
                        TextView item;
                        for(int i =2, size= categoriasFiltro.getChildCount(); i <size; i++){
                            item = (TextView) categoriasFiltro.getChildAt(i);
                            if (item != null) {
                                item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                                item.setTextColor(Color.parseColor("#FFFFFF"));
                            }
                        }
                        //aplicar item seleccionado de filtro
                        onClickTextView((TextView) v);
                    }
                });
            }

            //onClick en Marcas
            marcasFiltro = (LinearLayout) findViewById(R.id.marcas_filtro_layout);
            child = marcasFiltro.getChildAt(1);
            if(child!= null){
                child.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //poner todos los views en default
                        TextView item;
                        for(int i =2, size= marcasFiltro.getChildCount(); i <size; i++){
                            item = (TextView) marcasFiltro.getChildAt(i);
                            if (item != null) {
                                item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                                item.setTextColor(Color.parseColor("#FFFFFF"));
                            }

                        }
                        //aplicar item seleccionado de filtro
                        onClickTextView((TextView) v);
                    }
                });
            }

        }

		carrito = new Carrito();
        adapterCarrito = new BannerProductoCarrito(this, carrito);
        menuRight = (RelativeLayout) getMenuRight();
        if(menuRight != null) {
            listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
            relativeLayout = (RelativeLayout) menuRight.findViewById(R.id.carrito_total_layout);
            if(relativeLayout != null) {
                //Agregar onClick Listener para cuando se haga tap sobre el total del carrito
                relativeLayout.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View arg0) {
                        //lanzar GestionPedidosActivity
                        String productos = productsToJSONString();
                        if(productos != ""){
                            Bundle bundle = new Bundle();
                            bundle.putString("Productos", productos);
                            Cliente clienteM;
                            if(modificarPedidoId != null){
                                //Obtener indice de cliente
//                                Log.d("DEBUG", ""+clientes.size());
                                for(int j=0, size= clientes.size(); j<size; j++){
                                    if (clientes.get(j).getNombre().equalsIgnoreCase(clienteNombre)){
                                        clienteSpinner.setSelection(j);
                                        clienteSelected = j;
                                        clienteSpinner.setEnabled(false);
                                        Log.d("DEBUG", "cliente mod pedido: "+clienteNombre+" "+j);
                                    }
                                }
                                clienteM = clientes.get(clienteSelected);
                            }else{
                                clienteM = clientes.get(clienteSpinner.getSelectedItemPosition());
                            }

                            bundle.putString("Cliente", clienteM.getNombre());
                            bundle.putString("ClienteId", clienteM.getId());
                            bundle.putDouble("Descuento", clienteM.getDescuento());
                            bundle.putString("parseId", clienteM.getParseId());
                            bundle.putString("idPedido", modificarPedidoId);
                            bundle.putString("numPedido", modificarPedidoNum);
                            dispatchActivity(GestionPedidosActivity.class, bundle, false);
                        }else{
                            Toast.makeText(getApplicationContext(), getString(R.string.warning_agregar_elementos_carrito), Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            if(listCarrito != null) {
                listCarrito.setAdapter(adapterCarrito);
                listCarrito.setSelection(listCarrito.getAdapter().getCount()-1);
            }
        }

        if(modificarPedidoId == null && getIntent().getExtras().getInt("status")!= Pedido.ESTADO_RECHAZADO){
            //Pedido nuevo
            Log.d("DEBUG", "Pedido Nuevo "+getIntent().getExtras().getInt("status"));
        }else if(getIntent().getExtras().getInt("status")== Pedido.ESTADO_RECHAZADO || getIntent().getExtras().getInt("status")== Pedido.ESTADO_VERIFICANDO){
            //Editar pedido rechazado
            Log.d("DEBUG", "Modificar Pedido "+modificarPedidoId);
            carritoProgressDialog.show();
            clienteSpinner.setVisibility(View.INVISIBLE);
            final ArrayList<Producto> prodsModPedido=productos;
            //Pedido Id
            final ParseQuery queryPedido = new ParseQuery("Pedido");
            queryPedido.whereEqualTo("objectId", modificarPedidoId);
            queryPedido.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    //Productos en pedido
                    final ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
                    productosEnPedido.whereEqualTo("pedido", parseObject);
                    productosEnPedido.include("producto");
                    productosEnPedido.findInBackground(new FindCallback() {
                        @Override
                        public void done(List<ParseObject> pedidoConProductoObjects, ParseException e) {
                            //Me muevo en los productos relacionados al pedido
                            for (ParseObject pedidoConProductoObj : pedidoConProductoObjects) {
                                //Agrego los relacionados al pedido en el carrito
                                ParseObject prodAdd = pedidoConProductoObj.getParseObject("producto");
                                for (int i = 0, size = prodsModPedido.size(); i < size; i++) {
                                    if (prodAdd.get("codigo").equals(prodsModPedido.get(i).getNombre())) {
                                        prodsModPedido.get(i).setCantidad(pedidoConProductoObj.getInt("cantidad")+pedidoConProductoObj.getInt("excedente"));
                                        Log.d("DEBUG", "Meta en rechazo para prod: " + String.valueOf(pedidoConProductoObj.getInt("cantidad") + prodsModPedido.get(i).getExistencia()));
                                        prodsModPedido.get(i).setExistencia(pedidoConProductoObj.getInt("cantidad") + prodsModPedido.get(i).getExistencia());
                                        prodsModPedido.get(i).setIsInCarrito(true);
                                        adapterCarrito.notifyDataSetChanged();
//                                        Log.d("DEBUG", "Agregando "+pedidoConProductoObj.getInt("cantidad")+" productos "+prodAdd.get("codigo")+"("+prodAdd.getObjectId()+") a pedido");
                                        adapterCarrito.getCarrito().addProducto(prodsModPedido.get(i));
                                        adapterCarrito.setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                                    }
                                }
                            }
                            carritoProgressDialog.dismiss();
                        }
                    });
                }
            });
            adapterCarrito.notifyDataSetChanged();
            adapterCarrito.setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
            listCarrito.smoothScrollToPosition(0);
            adapterCarrito.showCarrito();

        }else{
            Log.d("DEBUG", "Clonar Pedido "+modificarPedidoId);
            final ArrayList<Producto> prodsModPedido=productos;
            carritoProgressDialog.show();
            //Pedido Id
            final ParseQuery queryPedido = new ParseQuery("Pedido");
            queryPedido.whereEqualTo("objectId", modificarPedidoId);
            queryPedido.getFirstInBackground(new GetCallback() {
                @Override
                public void done(ParseObject parseObject, ParseException e) {
                    //Productos en pedido
                    final ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
                    productosEnPedido.whereEqualTo("pedido", parseObject);
                    productosEnPedido.include("producto");
                    productosEnPedido.findInBackground(new FindCallback() {
                        @Override
                        public void done(List<ParseObject> pedidoConProductoObjects, ParseException e) {
                            //Me muevo en los productos relacionados al pedido
                            for (ParseObject pedidoConProductoObj : pedidoConProductoObjects) {
                                //Agrego los relacionados al pedido en el carrito
                                ParseObject prodAdd = pedidoConProductoObj.getParseObject("producto");
                                for (int i = 0, size = prodsModPedido.size(); i < size; i++) {
                                    if (prodAdd.get("codigo").equals(prodsModPedido.get(i).getNombre())) {
                                        prodsModPedido.get(i).setCantidad(pedidoConProductoObj.getInt("cantidad")+pedidoConProductoObj.getInt("excedente"));
                                        prodsModPedido.get(i).setIsInCarrito(true);
                                        adapterCarrito.notifyDataSetChanged();
//                                        Log.d("DEBUG", "Agregando "+pedidoConProductoObj.getInt("cantidad")+" productos "+prodAdd.get("codigo")+"("+prodAdd.getObjectId()+") a pedido");
                                        adapterCarrito.getCarrito().addProducto(prodsModPedido.get(i));
                                        adapterCarrito.setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                                    }
                                }
                            }
                            carritoProgressDialog.dismiss();
                        }
                    });
                }
            });

            adapterCarrito.notifyDataSetChanged();
            adapterCarrito.setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
            listCarrito.smoothScrollToPosition(0);
            adapterCarrito.showCarrito();
        }

//		catalogoProductos = productos;
        catalogo = new Catalogo(this, productos);
		
		if(listCarrito != null) {
//            adapterCatalogo = new BannerProductoCatalogo(this, catalogoProductos, listCarrito);
            adapterCatalogo = new BannerProductoCatalogo(this, catalogo, listCarrito);
			grid = (GridView) this.findViewById(R.id.catalogo_grid);
			grid.setOnTouchListener(new OnTouchListener() {
				private int xDown;
				private int xUp;
				private int xDiff;
				private int yDiff;
				private int yDown;
				private int yUp;
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if(event.getActionMasked() == MotionEvent.ACTION_DOWN) {
						xDown = (int)event.getX();
						yDown = (int)event.getY();
					}
					if(event.getActionMasked() == MotionEvent.ACTION_UP) {
						xUp =(int)event.getX();
						yUp = (int)event.getY();
						
						xDiff = xDown - xUp;
						yDiff = yDown - yUp;
						if(Math.abs(yDiff) < 200 && Math.abs(xDiff) > 200) {
							if(xDiff > 0) {
//								Log.d("RIGHT MENU", String.valueOf(xDiff) + "#" + String.valueOf(yDiff));
								if(!isMenuRightShowed() && !isMenuLeftShowed()) {
									showRightMenu();
								} else if(isMenuLeftShowed()) {
									hideMenuLeft();
								} 
							} else {
//								Log.d("LEFT MENU", String.valueOf(xDiff) + "#" + String.valueOf(yDiff));
								if(!isMenuRightShowed() && !isMenuLeftShowed()) {
									showLeftMenu();
								} else if(isMenuRightShowed()) {
									hideMenuRight();
								}
							}
						}
					}
					return false;
				}
			});
			if(grid != null) {
				grid.setAdapter(adapterCatalogo);
			}
		}
	}

	protected String productsToJSONString() {
		String productos = "";
		JSONArray productosJSONArray = new JSONArray();
		JSONObject productoJSONObj;
        ArrayList<Producto> prodsCarrito = carrito.getProductos();

        try {
            for (int i = 0, count = prodsCarrito.size(); i < count; i++) {
                productoJSONObj = prodsCarrito.get(i).toJSON();
//                Log.d("DEBUG", "productoJSONObj: "+ productoJSONObj.toString(1));
                productosJSONArray.put(productoJSONObj);
            }
            productos = productosJSONArray.toString();
//            Log.d("DEBUG", "Result productosJSONtoString: "+productos);
            return productos;
        } catch (JSONException e) {
            e.printStackTrace();
            Log.d("DEBUG", e.getCause().toString() + e.getMessage());
        }
        return  null;
	}

    public static void updatePreciosComerciales(){
        Double descCliente = clientes.get(clienteSelected).getDescuento()/100.0;
        Double precio = 0.0;
        for(Producto prod: catalogo.getProductosCatalogo()){
            precio = prod.getPrecio();
            prod.setPrecioComercial(precio - (precio * descCliente));
        }
        adapterCarrito.notifyDataSetChanged();
        adapterCatalogo.notifyDataSetChanged();
    }

    public void updatePreciosComerciales(int i){
        Double descCliente = clientes.get(clienteSelected).getDescuento()/100.0;
        Producto prod = catalogo.getProductosCatalogo().get(i);
        Double precio = prod.getPrecio();
        prod.setPrecioComercial(precio -(precio*descCliente));
    }

    /** Proceso que establece el valor del descuento manual para el producto seleccionado*/
    public void setValorDescuentoManual(final Producto producto){
        final Context oThis = this;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);    //seteandolo para numeros solamente
        input.setMaxLines(1);
        input.setHint(R.string.descuento_manual);
        builder.setView(input);
        TextView title = new TextView(this);
        title.setText(R.string.titulo_descuento_manual);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(20);
        builder.setCustomTitle(title);
        builder.setPositiveButton(R.string.aceptar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Valor de Descuento ingresado
                if ((input.getText().toString() != null) && !input.getText().toString().isEmpty()) {
                    Double valor = Double.parseDouble(input.getText().toString());
                    if (valor >= MIN_DESC_MAN && valor <= MAX_DESC_MAN) {
                        Log.d("DEBUG", valor.toString());
                        if(valor == 0.0){
                            producto.setDescuentoManual(0);
                            adapterCatalogo.notifyDataSetChanged();
                            adapterCarrito.notifyDataSetChanged();
                            Toast.makeText(oThis, "Descuento manual eliminado", 3000).show();
                        }else{
                            producto.setDescuentoManual(valor);
                            adapterCatalogo.notifyDataSetChanged();
                            adapterCarrito.notifyDataSetChanged();
                            Toast.makeText(oThis, "Porcentaje de descuento manual asignado", 3000).show();
                        }

                    } else {
                        Toast.makeText(oThis, "Porcentaje de descuento manual no valido", 3000).show();
                        Log.d("DEBUG", "porcentaje no valido");
                    }
                } else {
                    producto.setDescuentoManual(0);
                    adapterCatalogo.notifyDataSetChanged();
                    adapterCarrito.notifyDataSetChanged();
                    Toast.makeText(oThis, "Descuento manual eliminado", 3000).show();
                }
            }
        })
                .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // Dialogo cancelado
                        Toast.makeText(oThis, "Descuento manual no establecido", 3000).show();
                    }
                });
        builder.create();
        builder.show();
    }

	@Override
	protected Request getRequestAction() {
		Request req = new Request(Request.PARSE_REQUEST);
		ParseQuery query = new ParseQuery("Producto");
        query.include("categoria");
        query.include("iva");
        query.include("grupo_categorias");
        query.orderByAscending("marca");
		req.setRequest(query);
		
		return req;
	}

    public void onClickTextView(TextView selected){
        LinearLayout parent = (LinearLayout) selected.getParent();
        if (((TextView)parent.getChildAt(0)).getText().equals(getString(R.string.categorias))){
            setCategoriaActual(selected);
            catalogo.filter(marcaActual, categoriaActual);
            adapterCatalogo.notifyDataSetChanged();
        }else if(((TextView)parent.getChildAt(0)).getText().equals(getString(R.string.marcas))){
            setMarcaActual(selected);
            catalogo.filter(marcaActual, categoriaActual);
            adapterCatalogo.notifyDataSetChanged();
        }
        if(!selected.equals(parent.getChildAt(1))){
            //actualizar estilo
            selected.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
            selected.setTextColor(Color.parseColor("#3A70B9"));
        }
    }

    /*------------- CATEGORIAS -----------------*/

    /**
     * Agrega la categoria cat a la lista de categorias en el menu lateral de filtros
     * @param cat categoria
     */
    public void addCategoria(String cat){
        if((cat != null) && !isInCategorias(cat)){
            categorias.add(cat);
            TextView tv = new TextView(mContext);
            tv.setText(cat);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(22);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 5, 0, 5);
            tv.setLayoutParams(layoutParams);
            tv.setBackgroundResource(R.drawable.pastilla_items_filtro);
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //poner todos los views en default
                    TextView item;
                    for(int i =2, size= categoriasFiltro.getChildCount(); i <size; i++){
                        item = (TextView) categoriasFiltro.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                            item.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                    //aplicar item seleccionado de filtro
                    onClickTextView((TextView) v);
                }
            });
            categoriasFiltro = (LinearLayout) findViewById(R.id.categorias_filtro_layout);
            categoriasFiltro.addView(tv);
        }
    }

    /**
     * Revisa si el string cat está contenido dentro de las categorias almacenadas en el servidor
     * @param cat categoria
     * @return Exito en la busqueda
     */
    public Boolean isInCategorias(String cat){
        for(int i = 0, size = categorias.size(); i<size; i++){
            if (cat.equals(categorias.get(i)))
                return true;
        }
        return false;
    }

    /**
     * Establecer categoria seleccionada
     * @param selected categoria seleccionada
     */
    public void setCategoriaActual(TextView selected){
        categoriaActual = selected.getText().toString();
        Log.d("DEBUG", "categoria seleccionada: "+ categoriaActual);
    }

    /*------------- MARCAS -----------------*/

    /**
     * Agregar marca al menu lateral de filtros
     * @param mar marca a agregar
     */
    public void addMarca(String mar){
        if((mar != null) && !isInMarcas(mar)){
            marcas.add(mar);
            TextView tv = new TextView(mContext);
            tv.setText(mar);
            tv.setTextColor(Color.parseColor("#FFFFFF"));
            tv.setTextSize(22);
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(10, 10, 10, 10);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 5, 0, 5);
            tv.setLayoutParams(layoutParams);
            tv.setBackgroundResource(R.drawable.pastilla_items_filtro);
            tv.setTypeface(null, Typeface.BOLD_ITALIC);
            tv.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    //poner todos los views en default
                    TextView item;
                    for(int i =2, size= marcasFiltro.getChildCount(); i <size; i++){
                        item = (TextView) marcasFiltro.getChildAt(i);
                        if (item != null) {
                            item.setBackgroundResource(R.drawable.pastilla_items_filtro);
                            item.setTextColor(Color.parseColor("#FFFFFF"));
                        }
                    }
                    //aplicar item seleccionado de filtro
                    onClickTextView((TextView) v);
                }
            });
            marcasFiltro = (LinearLayout) findViewById(R.id.marcas_filtro_layout);
            marcasFiltro.addView(tv);
        }
    }

    /**
     * Revisa si el string cat está contenido dentro de las marcas almacenadas en el servidor
     * @param cat
     * @return Exito en la busqueda
     */
    public Boolean isInMarcas(String cat){
        for(int i = 0, size = marcas.size(); i<size; i++){
            if (cat.equals(marcas.get(i)))
                return true;
        }
        return false;
    }

    /**
     * Establece la marca actual
     * @param selected marca seleccionada
     */
    public void setMarcaActual(TextView selected){
        marcaActual = selected.getText().toString();
//        Log.d("DEBUG", "marca seleccionada: "+ marcaActual);
    }

    /**
     * Clase AsyncTask que se encarga de descargar las imagenes de los productos del catalogo
     */
    public class ImageDownloadTask extends AsyncTask<String, Void, Bitmap> {
        private final WeakReference<Producto> productoWeakReference;

        public ImageDownloadTask(Producto producto) {
            productoWeakReference = new WeakReference<Producto>(producto);
        }

        @Override
        protected Bitmap doInBackground(String... params) {
            try {
                InputStream in = new java.net.URL(params[0]).openStream();
                Bitmap bitmap = BitmapFactory.decodeStream(in);
                return bitmap;
            } catch (Exception e) {
//                Log.e("ImageDownload Exception: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            if (productoWeakReference != null) {
                Producto prod = productoWeakReference.get();
                if (prod != null) {
//                    Log.d("DEBUG","Imagen de producto "+prod.getCodigo()+" obtenida");
                    prod.setImagen(bitmap);
                    if(adapterCatalogo != null){
                        adapterCatalogo.notifyDataSetChanged();
                    }
                }
            }
        }
    }
}
