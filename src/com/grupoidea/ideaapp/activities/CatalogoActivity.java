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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Categoria;
import com.grupoidea.ideaapp.models.Cliente;
import com.grupoidea.ideaapp.models.GrupoCategorias;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
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
    public ArrayList<GrupoCategorias> gruposCategorias;
    public ArrayList<Categoria> categorias;
    public String categoriaActual="Todas", marcaActual="Todas";
    public static ArrayList<String> marcas;
    public static ArrayAdapter marcasAdapter, categoriasAdapter;
    public LinearLayout categoriasFiltro, marcasFiltro;
    protected static  Context mContext;
    protected ProgressBar descuentosProgressBar;
	public CatalogoActivity() {
		super(true, false, true, true); //hasCache (segundo param) :true!
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
        catalogoProgressDialog.setCancelable(true);
//        catalogoProgressDialog.show();

        //Dialogo de carga carrito
        carritoProgressDialog = new ProgressDialog(this);
        carritoProgressDialog.setTitle("Cargando...");
        carritoProgressDialog.setMessage("Cargando Carrito, por favor espere...");
        carritoProgressDialog.setIndeterminate(true);
        carritoProgressDialog.setCancelable(false);

        //ProgressBar Descuentos
//        descuentosProgressBar = (ProgressBar) findViewById(R.id.descuentosProgressBar);

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

        marcas = new ArrayList<String>();
        categoriaActual = getString(R.string.todas);
        marcaActual = getString(R.string.todas);
	}

    /**
     * Instanciar nuevo producto en base a un ParseObject
     * @param productoParse producto obtenido de Parse
     * @return Instancia de Clase Producto
     */
	private Producto retrieveProducto(final ParseObject productoParse){
		String codigo = productoParse.getString("codigo");
		String nombre = productoParse.getString("nombre");
		double precio = productoParse.getDouble("costo");
		final String objectId = productoParse.getObjectId();
		final Producto producto = new Producto(objectId, nombre, codigo, precio);

        ParseQuery descuentosQuery;

        //Obtener Imagen
        retrieveImage(producto, productoParse);

        //Obtener existencia
        ParseQuery queryExistencia = new ParseQuery("Metas");
        queryExistencia.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryExistencia.whereEqualTo("producto", productoParse);
        queryExistencia.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null){
                    int exist = parseObject.getInt("meta")-parseObject.getInt("pedido")-parseObject.getInt("facturado");
                    if(exist >0){
                        producto.setExistencia(exist);
                    }else{
                        producto.setExistencia(0);
                    }
                }

                else Log.d("DEBUG", "No existe registro del producto "+objectId+" en la tabla Metas");
            }
        });

        //asignar IVA
        producto.setIva(productoParse.getParseObject("iva").getDouble("porcentaje"));
        producto.setExcedente(productoParse.getInt("excedente"));

        //asignar marca
        producto.setMarca(productoParse.getString("marca"));
        //agregar marca a filtro de catalogo
        addMarcaTextView(productoParse.getString("marca"));

        //Descuentos Producto
        //Obtener descuentos por categoria
        final SparseArray<Double> tablaDescuentosProducto= new SparseArray<Double>();
        descuentosQuery = productoParse.getRelation("descuentos").getQuery();
        descuentosQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        descuentosQuery.findInBackground(new FindCallback() {
            @Override
            public void done(List<ParseObject> descuentos, ParseException e) {
                if (e == null && descuentos != null) {
                    for (ParseObject descuento : descuentos) {
                        tablaDescuentosProducto.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
                    }
                }
                descuentosProgressBar.incrementProgressBy(1);
            }

        });
        producto.setTablaDescuentos(tablaDescuentosProducto);

        /**
         * -------------- CATEGORIA --------------
         */
        //Obtener categoria
        ParseObject categoriaParse = productoParse.getParseObject("categoria");

        //Obtener nombre y descuentos de categoria
        if(null != categoriaParse){
            //Revisar si categoria ya existe y agregarla al producto
            Categoria categoria= findCategoriaByName(categoriaParse.getString("nombre"));
            //Si no existe crear una nueva y agregarla a categorias y al producto
            if( categoria == null){
                categoria = new Categoria(categoriaParse.getString("nombre"));
            }
            addCategoriaTextView(categoria);

            //Obtener descuentos por categoria
            final SparseArray<Double> tablaDescuentosCategoria= new SparseArray<Double>();
            descuentosQuery = categoriaParse.getRelation("descuentos").getQuery();
            descuentosQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            descuentosQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> descuentos, ParseException e) {
                    if (e == null && descuentos != null) {
                        for (ParseObject descuento : descuentos) {
                            tablaDescuentosCategoria.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
                        }
                        descuentosProgressBar.incrementProgressBy(1);
                    }
                }
            });
            categoria.setTablaDescuentos(tablaDescuentosCategoria);
            producto.setCategoria(categoria);
        }

        /**
         * -------------- FIN CATEGORIA --------------
         */

        /**
         * -------------- GRUPO CATEGORIAS --------------
         */
        //Obtener nombre y descuentos de grupo de categorias
        if(null != productoParse.getParseObject("grupo_categorias") && null != productoParse.getParseObject("grupo_categorias").getJSONArray("relacionadas")){
            //Revisar si grupo categorias ya existe y agregarlo al producto
            GrupoCategorias grupo = findGrupoCategoriasByName(productoParse.getParseObject("grupo_categorias").getString("nombre"));
            //Si no existe crear uno nuevo y agregarlo a gruposCategorias y al producto
            if( grupo == null){
                grupo = new GrupoCategorias(productoParse.getParseObject("grupo_categorias").getString("nombre"), productoParse.getParseObject("grupo_categorias").getJSONArray("relacionadas"));
                gruposCategorias.add(grupo);
            }

            //obtener descuentos por grupo de categoria
            final SparseArray<Double> tablaDescuentosGrupo = new SparseArray<Double>();
            descuentosQuery = productoParse.getParseObject("grupo_categorias").getRelation("descuentos").getQuery();
            descuentosQuery.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
            descuentosQuery.findInBackground(new FindCallback() {
                @Override
                public void done(List<ParseObject> descuentos, ParseException e) {
                    if (e == null && descuentos != null) {
                        for (ParseObject descuento : descuentos) {
                            tablaDescuentosGrupo.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));

                            //Quitar el dialogo con el ultimo descuento del ultimo producto
                            if(descuento.equals(descuentos.get(descuentos.size()-1)) && lastprodName!=null && lastprodName.equalsIgnoreCase(producto.getCodigo())){
                                Log.d("DEBUG", "Finalizada carga de productos");
                                Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_LONG).show();
                                refresh.setClickable(true);
                                catalogoProgressDialog.dismiss();
                            }
                        }
                    }
                    descuentosProgressBar.incrementProgressBy(1);
                    if(producto.getCodigo().equalsIgnoreCase(lastprodName)){
                        descuentosProgressBar.setVisibility(View.GONE);
                    }
                }
            });
            grupo.setTablaDescuentos(tablaDescuentosGrupo);
            producto.setGrupoCategorias(grupo);
        }
        /**
         * -------------- FIN GRUPO CATEGORIAS --------------
         */
//        descuentosProgressBar.incrementProgressBy(1);
        return producto;
	}

    public void retrieveImage(Producto prod, ParseObject producto){
        //Obtener imagen
        if(producto.getString("picture")!= null && !producto.getString("picture").isEmpty()){
            ImageDownloadTask imgDownloader = new ImageDownloadTask(prod);
            imgDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,producto.getString("picture"));
        }

    }

//    public Producto retrieveProductoAsync(ParseObject productoParse, Producto producto){
//        String codigo = productoParse.getString("codigo");
//        String nombre = productoParse.getString("nombre");
//        double precio = productoParse.getDouble("costo");
//        final String objectId = productoParse.getObjectId();
//        producto = new Producto(objectId,codigo, nombre, precio);
//        ProductRetrieverTask retriever = new ProductRetrieverTask(producto);
//        retriever.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, productoParse);
//        return producto;
//    }
	 
	@Override
	protected void manageResponse(Response response, boolean isLiveData) {
		@SuppressWarnings("unchecked")
		List<ParseObject> productosParse = (List<ParseObject>) response.getResponse();
        ArrayList<Producto> productos = new ArrayList<Producto>();
        gruposCategorias = new ArrayList<GrupoCategorias>();
        categorias = new ArrayList<Categoria>();
        descuentosProgressBar = (ProgressBar) findViewById(R.id.descuentosProgressBar);
        descuentosProgressBar.setVisibility(View.VISIBLE);
        descuentosProgressBar.setMax(productosParse.size()*3);
        descuentosProgressBar.setProgress(1);
        Toast.makeText(mContext, "Cargando Descuentos, por favor espere.", Toast.LENGTH_LONG).show();

		Producto producto = null;
		RelativeLayout menuRight, menuLeft;
		RelativeLayout relativeLayout;
		ListView listCarrito = null;

        lastprodName=null;
        //cargar productos desde Parse
		for (ParseObject parseObject : productosParse) {
            producto = retrieveProducto(parseObject);
//            producto = retrieveProductoAsync(parseObject, producto);

			productos.add(producto);
            if(parseObject.equals(productosParse.get(productosParse.size()-1))){
                lastprodName=producto.getCodigo();
            }
		}
        catalogoProgressDialog.dismiss();


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

		carrito = new Carrito(categorias, gruposCategorias);
        adapterCarrito = new BannerProductoCarrito(this, carrito);
        menuRight = (RelativeLayout) getMenuRight();
        if(menuRight != null) {
            listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
            relativeLayout = (RelativeLayout) menuRight.findViewById(R.id.carrito_total_layout);
            if(relativeLayout != null) {
                //TODO Working here
                //Agregar onClick Listener para cuando se haga tap sobre el total del carrito
                relativeLayout.setOnClickListener(new OnClickListener(){
                    @Override
                    public void onClick(View arg0) {
                        //lanzar GestionPedidosActivity
                        String productos = productsToJSONString();
                        if(!productos.equalsIgnoreCase("")){
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
                                    if (prodAdd.get("codigo").equals(prodsModPedido.get(i).getCodigo())) {
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
                                    if (prodAdd.get("codigo").equals(prodsModPedido.get(i).getCodigo())) {
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
		String productos;
		JSONArray productosJSONArray = new JSONArray();
		JSONObject productoJSONObj;
        ArrayList<Producto> prodsCarrito = carrito.getProductos();

        try {
            for (int i = 0, count = prodsCarrito.size(); i < count; i++) {
                productoJSONObj = prodsCarrito.get(i).toJSON();
                Log.d("DEBUG","prod: "+prodsCarrito.get(i).getCodigo()+"cant: "+prodsCarrito.get(i).getCantidad());
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
                if (null != input.getText().toString() && !input.getText().toString().isEmpty()) {
                    Double valor = Double.parseDouble(input.getText().toString());
                    if (valor >= MIN_DESC_MAN && valor <= MAX_DESC_MAN) {
                        Log.d("DEBUG", valor.toString());
                        if(valor == 0.0){
                            producto.setDescuentoManual(0.0);
                            adapterCarrito.notifyDataSetChanged();
                            adapterCarrito.getCarrito().recalcularMontos();
                            setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                            Toast.makeText(oThis, "Descuento manual eliminado", 3000).show();
                        }else{
                            producto.setDescuentoManual(valor);
                            adapterCarrito.notifyDataSetChanged();
                            adapterCarrito.getCarrito().recalcularMontos();
                            setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                            Toast.makeText(oThis, "Porcentaje de descuento manual asignado", 3000).show();
                        }
                    } else {
                        Toast.makeText(oThis, "Porcentaje de descuento manual no valido", 3000).show();
                        Log.d("DEBUG", "porcentaje no valido");
                    }
                } else {
                    producto.setDescuentoManual(0.0);
                    adapterCarrito.notifyDataSetChanged();
                    adapterCarrito.getCarrito().recalcularMontos();
                    setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
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
     * @param categoria Categoria
     */
    public void addCategoriaTextView(Categoria categoria){
        String cat = categoria.getNombre();
        if((cat != null) && !isInCategorias(cat)){
            categorias.add(categoria);
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
     * @param categoria categoria
     * @return Exito en la busqueda
     */
    public Boolean isInCategorias(String categoria){
        for(int i = 0, size = categorias.size(); i<size; i++){
            if (categoria.equalsIgnoreCase(categorias.get(i).getNombre()))  return true;
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
    public void addMarcaTextView(String mar){
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
     * @param marca
     * @return Exito en la busqueda
     */
    public Boolean isInMarcas(String marca){
        for(int i = 0, size = marcas.size(); i<size; i++){
            if (marca.equals(marcas.get(i)))
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
    }

    /**
     * Busca una Categoria por nombre, devuelve null de no existir
     * @param name nombre de categoria dentro del objeto producto de Parse
     * @return <code>Categoria</code> o <code>null</code> de no existir categoria asociada
     */
    public Categoria findCategoriaByName(String name) {
        Categoria categoriaActual, categoriaFinal = null;
        for (int i = 0; i < categorias.size(); i++) {
            categoriaActual = categorias.get(i);
            if (categoriaActual != null) {
                if (name.equalsIgnoreCase(categoriaActual.getNombre())) {
                    categoriaFinal = categoriaActual;
                    break;
                }
            }
        }
        return categoriaFinal;
    }

    /**
     * Busca un GrupoCategorias por nombre, devuelve null de no existir
     * @param name nombre de grupo de categorias dentro del objeto producto de Parse
     * @return <code>GrupoCategorias</code> o <code>null</code> de no existir grupo de categorias asociado
     */
    public GrupoCategorias findGrupoCategoriasByName(String name) {
        GrupoCategorias grupoCategoriasActual, grupoCategoriasFinal = null;
        for (int i = 0; i < gruposCategorias.size(); i++) {
            grupoCategoriasActual = gruposCategorias.get(i);
            if (grupoCategoriasActual != null) {
                if (name.equalsIgnoreCase(grupoCategoriasActual.getNombre())) {
                    grupoCategoriasFinal = grupoCategoriasActual;
                    break;
                }
            }
        }
        return grupoCategoriasFinal;
    }

//    /**
//     * Clase AsyncTask para instanciar productos desde Parse
//     */
//    public class ProductRetrieverTask extends AsyncTask<ParseObject,Void,Producto>{
//        private final WeakReference<Producto> productoWeakReference;
//
//        public ProductRetrieverTask(Producto producto) {
//            productoWeakReference = new WeakReference<Producto>(producto);
//        }
//
//        @Override
//        protected Producto doInBackground(ParseObject... parseObjects) {
//            try {
//                return null;
//            }catch (Exception e){
//                e.printStackTrace();
//                return null;
//            }
//        }
//
//        @Override
//        protected void onPostExecute(Producto producto){
//            catalogoProductos.add(producto);
//            adapterCatalogo.notifyDataSetChanged();
//        }

//    }

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
                Bitmap bitmap; File appDir, file2;

                //Extraer nombre de archivo de URL
                String temp[] = params[0].split("/");
                String fileName = temp[temp.length-1];

                if(isExternalStorageReadable()){
                    appDir = mContext.getExternalFilesDir("img/");
                    file2 = new File(appDir, fileName);
                    Log.d("DEBUG", "Buscando imagen en SD "+ file2.getAbsolutePath());
                    bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());

                    if(bitmap == null){
                        //Descarga imagen del server de IDEA
                        Log.d("DEBUG", "Imagen no existe localmente. Descargando del servidor");
                        InputStream in = new java.net.URL(params[0]).openStream();
                        bitmap = BitmapFactory.decodeStream(in);

                        //Guardar imagen en SD
                        if(isExternalStorageWritable()){
                            appDir = mContext.getExternalFilesDir("img/");
                            File fileSave = new File(appDir,fileName);
                            if(!fileSave.exists()) fileSave.createNewFile();
                            Log.d("DEBUG", "Guardando imagen en "+ fileSave.getAbsolutePath());
                            FileOutputStream out = new FileOutputStream(fileSave);
                            Bitmap bitmapSave = Bitmap.createBitmap(bitmap);
                            bitmapSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
                            out.flush();
                            out.close();
                        }else{
                            Log.d("DEBUG", "No se pudo salvar la imagen "+fileName);
                            Toast.makeText(mContext, "No se pudo salvar la imagen "+fileName, 1000).show();
                        }

                        return bitmap;
                    }else{
                        //Imagen está en SD
                        return bitmap;
                    }
                }else{
                    Log.d("DEBUG", "Tarjeta SD no disponible ");
                    Toast.makeText(mContext, "Tarjeta SD no disponible ", 1000).show();
                }
            } catch (Exception e) {
                Log.e("ImageDownload Exception: ", e.getMessage());
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
                    Log.d("DEBUG","Imagen de producto "+prod.getCodigo()+" obtenida");
                    prod.setImagen(bitmap);
                    if(adapterCatalogo != null){
                        adapterCatalogo.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    public void setTotalCarrito(String total) {
        TextView textView;
        RelativeLayout layout;

        layout = (RelativeLayout) this.getRightMenuLayout();
        if(layout != null) {
            layout = (RelativeLayout) layout.findViewById(R.id.carrito_total_layout);
            if(layout != null) {
                textView = (TextView) layout.findViewById(R.id.carrito_total_precio_text_view);
                if(textView != null) {
                    textView.setText(total);
                }
            }
        }
    }

}
