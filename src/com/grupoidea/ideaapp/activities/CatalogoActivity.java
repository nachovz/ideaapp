package com.grupoidea.ideaapp.activities;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.grupoidea.ideaapp.GrupoIdea;
import com.grupoidea.ideaapp.R;
import com.grupoidea.ideaapp.components.BannerProductoCarrito;
import com.grupoidea.ideaapp.components.BannerProductoCatalogo;
import com.grupoidea.ideaapp.components.CategoriasAdapter;
import com.grupoidea.ideaapp.components.MarcasAdapter;
import com.grupoidea.ideaapp.io.Request;
import com.grupoidea.ideaapp.io.Response;
import com.grupoidea.ideaapp.models.Carrito;
import com.grupoidea.ideaapp.models.Catalogo;
import com.grupoidea.ideaapp.models.Categoria;
import com.grupoidea.ideaapp.models.GrupoCategorias;
import com.grupoidea.ideaapp.models.Marca;
import com.grupoidea.ideaapp.models.Meta;
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class CatalogoActivity extends ParentMenuActivity {
    protected String TAG = this.getClass().getSimpleName();
    /** Cadena de texto que contiene el nombre del cliente*/
	private String clienteNombre;
	/** ArrayList que contiene los productos que se mostraran en el gridCatalogo del catalogo*/
	private static ArrayList<Producto> catalogoProductos;
    /** Objeto que representa al catalogo.*/
    public static Catalogo catalogo;
	/** Objeto que representa al carrito de compras del catalogo.*/
	private Carrito carrito;
    private ListView carritoListView;
	/** Adapter utilizado como puente entre el ArrayList de productos del carrito y el layout de cada producto*/
	protected static BannerProductoCarrito carritoAdapter;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	public static BannerProductoCatalogo catalogoAdapter;
    public String modificarPedidoId, modificarPedidoNum;
    /** Minimo y maximo valor para descuentos manueales*/
    public final static Double MIN_DESC_MAN = 0.0, MAX_DESC_MAN = 100.0;
    protected ArrayList<GrupoCategorias> gruposCategorias;
    protected ArrayList<Categoria> categorias;
    protected static ArrayList<String> marcas;
    protected static MarcasAdapter marcasAdapter;
    protected static CategoriasAdapter categoriasAdapter;
    public ListView categoriasFiltro;
    public ListView marcasFiltro;
    public GridView gridCatalogo;
    private RelativeLayout carritoLayout, filtroLayout;
    protected static  Context mContext;
    protected ProgressBar catalogoProgressBar;
    public List<ParseObject> descuentosParse;
    protected static GrupoIdea app;

	public CatalogoActivity() {
		super(true, false, true, true); //hasCache (segundo param) :true!
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        mContext = getBaseContext();
        super.instanceContext = mContext;
        app = (GrupoIdea) getApplication();
        app.clientes = getClientesFromParse();
        app.productos = new ArrayList<Producto>();
        catalogo = new Catalogo(mContext);
        gruposCategorias = new ArrayList<GrupoCategorias>();
        categorias = new ArrayList<Categoria>();

        //Obtener datos de pedido (en caso de que se esté modificando o clonando uno)
        if(app.pedido != null){
            modificarPedidoId = app.pedido.getObjectId();
            modificarPedidoNum = app.pedido.getNumPedido();
            clienteNombre = app.clienteActual.getNombre();
        }

		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.activity_catalogo_layout);

        clienteSpinner.setAdapter(adapter);
        clienteSpinner.setEnabled(true);
        clienteSpinner.setVisibility(View.VISIBLE);
        clienteSpinner.setSelection(0);
        clienteSelected = 0;

        filtroLayout = (RelativeLayout) getMenuLeft();
        categoriasFiltro = (ListView) findViewById(R.id.categorias_filtro_listView);
        marcasFiltro = (ListView) findViewById(R.id.marcas_filtro_listView);

        carritoLayout = (RelativeLayout) getMenuRight();

        marcas = new ArrayList<String>();
        categoriasAdapter = new CategoriasAdapter(mContext);
        marcasAdapter = new MarcasAdapter(mContext, categoriasAdapter);
        categoriasAdapter.setMarcasAdapter(marcasAdapter);
        gridCatalogo = (GridView) this.findViewById(R.id.catalogo_grid);
//        gridCatalogo.setVisibility(View.GONE);

        catalogoProgressBar = (ProgressBar) findViewById(R.id.catalogo_progressBar);
        catalogoProgressBar.setVisibility(View.VISIBLE);
//        catalogoProgressBar.setVisibility(View.GONE);
	}

    @Override
    public void onBackPressed(){
        super.onBackPressed();
//        Log.d(TAG, "accionando onBackPressed");
        //Correr Thread para cancelar queries en proceso
//        new Thread(new Runnable() {
//            public void run(){
//                for(ParseQuery query: queries){
//                    if(null != query) query.cancel();
//                }
//            }
//        }).start();
        this.dispatchActivity(DashboardActivity.class, null, false);
//        this.finish();
    }

    @Override
    protected Request getRequestAction() {
        getDescuentosFromParse();
        Request req = new Request(Request.PARSE_REQUEST);
        ParseQuery query = new ParseQuery("Producto");
        query.setLimit(QUERY_LIMIT);
        query.include("categoria");
        query.setCachePolicy(getParseCachePolicy());
        query.include("iva");
        query.include("grupo_categorias");
        query.orderByAscending("marca");
        req.setRequest(query);

        return req;
    }

    @Override
    protected void manageResponse(Response response, boolean isLiveData) {

        @SuppressWarnings("unchecked")
        List<ParseObject> productosParse = (List<ParseObject>) response.getResponse();
        app.productosParse = productosParse;
        catalogo.setProductos(app.productos);

        if(filtroLayout != null) initFiltroLayout();

        carrito = new Carrito(categorias, gruposCategorias);
        carritoAdapter = new BannerProductoCarrito(this, carrito);
        if(carritoLayout != null) carritoListView = initCarritoLayout();

//        catalogo.setProductos(app.productos);
        marcasAdapter.setCatalogo(catalogo);

        if(carritoListView != null) initCatalogoLayout();

        clienteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clienteSelected = i;
                updatePreciosComerciales();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        int statusPedido = getIntent().getIntExtra("status", Pedido.ESTADO_NUEVO);
        switch(statusPedido){
            case Pedido.ESTADO_RECHAZADO:
            case Pedido.ESTADO_VERIFICANDO:
                //Dialogo de carga carrito
                initCarritoProgressDialog();
                editarPedido();
                break;
            case Pedido.ESTADO_APROBADO:
                initCarritoProgressDialog();
                clonarPedido();
                break;
            default:
                break;
        }

        for (int i=0, size=productosParse.size(); i<size; i++) {
            ParseObject parseObject = productosParse.get(i);
            new ProductoDownloadTask(parseObject, i, size).execute();
        }
    }

    /**
     * Instanciar nuevo producto en base a un ParseObject
     * @param productoParse {@link com.parse.ParseObject} del producto, obtenido desde Parse
     * @return Instancia de {@link Producto}
     */
	private Producto retrieveProducto(final ParseObject productoParse, final int pos, final int size){
        ArrayList<ParseObject> descuentos;
        Meta meta;
        Marca marca;
        String codigo = productoParse.getString("codigo");
		String nombre = productoParse.getString("nombre");
		double precio = productoParse.getDouble("costo");
		final String objectId = productoParse.getObjectId();
		final Producto producto = new Producto(objectId, nombre, codigo, precio);
        producto.setProductoParse(productoParse);
        producto.setIva(app.iva);
        producto.setExcedente(productoParse.getInt("excedente"));
        producto.setDescripcion(productoParse.getString("descripcion"));

        //Obtener Imagen
        if(null!= productoParse.getString("picture") && !productoParse.getString("picture").isEmpty()){
            retrieveImage(producto, productoParse);
        }else{
            producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.prod_background));
            producto.setImagenID(R.drawable.prod_background);
            producto.setImagenURL(productoParse.getString("picture"));
        }

        //Obtener existencia
        meta = app.findMetaByProductCode(producto.getCodigo());
        if(meta != null) producto.setExistencia(meta.getExistencia());
        else producto.setExistencia(0);

        producto.setMarca(productoParse.getString("marca"));
        marca = marcasAdapter.addMarca(productoParse.getString("marca"));

        //Obtener descuentos por producto
        SparseArray<Double> tablaDescuentosProducto = new SparseArray<Double>();
        descuentos = findDescuentosByRelatedObjectId(productoParse.getObjectId());
        if(descuentos!= null){
            for (ParseObject descuento : descuentos) tablaDescuentosProducto.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
        }
        producto.setTablaDescuentos(tablaDescuentosProducto);


        //Obtener Categoria
        ParseObject categoriaParse = productoParse.getParseObject("categoria");
        if(null != categoriaParse){
            Categoria categoria = findCategoriaByName(categoriaParse.getString("nombre"));
            if( categoria == null) categoria = new Categoria(categoriaParse.getString("nombre"));
            if(!marca.findCategoria(categoria.getNombre())) marca.addCategoria(categoria);
            //Agregar Categoria a Todas
            if(!marcasAdapter.todas.findCategoria(categoria.getNombre())){
                marcasAdapter.todas.addCategoria(categoria);
            }

            final SparseArray<Double> tablaDescuentosCategoria= new SparseArray<Double>();
            descuentos = findDescuentosByRelatedObjectId(categoriaParse.getObjectId());
            if (descuentos != null) {
                for (ParseObject descuento : descuentos) tablaDescuentosCategoria.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
            }
            categoria.setTablaDescuentos(tablaDescuentosCategoria);
            producto.setCategoria(categoria);
        }

        //Obtener Grupo de Categorias
        ParseObject grupoParse = productoParse.getParseObject("grupo_categorias");
        if(null != productoParse.getParseObject("grupo_categorias") && null != productoParse.getParseObject("grupo_categorias").getJSONArray("relacionadas")){
            GrupoCategorias grupo = findGrupoCategoriasByName(grupoParse.getString("nombre"));
            if( grupo == null){
                grupo = new GrupoCategorias(grupoParse.getString("nombre"), grupoParse.getJSONArray("relacionadas"));
                gruposCategorias.add(grupo);
            }

            final SparseArray<Double> tablaDescuentosGrupo = new SparseArray<Double>();
            descuentos = findDescuentosByRelatedObjectId(grupoParse.getObjectId());
            if (descuentos != null) {
                for (ParseObject descuento : descuentos) tablaDescuentosGrupo.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
            }

            grupo.setTablaDescuentos(tablaDescuentosGrupo);
            producto.setGrupoCategorias(grupo);
        }

//        //Verificar y Notificar Carga Completa de Productos
//        if(pos == size -1) onProductosCargados();

        return producto;
	}

    private void onProductosCargados() {
        marcasAdapter.notifyDataSetChanged();
        categoriasAdapter.notifyDataSetChanged();
        catalogoAdapter.notifyDataSetChanged();
        catalogoProgressBar.setVisibility(View.GONE);
        gridCatalogo.setVisibility(View.VISIBLE);
        menuFilterIcon.setEnabled(true);
        Toast.makeText(mContext, getString(R.string.productos_cargados), Toast.LENGTH_SHORT).show();
        refresh.setClickable(true);
    }

    /**
     * Metodo invocado para obtener las imágenes de los productos desde el servidor de IDEA
     * @param producto {@link com.grupoidea.ideaapp.models.Producto} Instancia local del producto
     * @param productoParse {@link ParseObject} del producto obtenido desde Parse
     */
    public void retrieveImage(Producto producto, ParseObject productoParse){
        if(productoParse.getString("picture")!= null && !productoParse.getString("picture").isEmpty()){
            ImageDownloadTask imgDownloader = new ImageDownloadTask(producto);
            imgDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,productoParse.getString("picture"));
        }
    }

    public class ProductoDownloadTask extends AsyncTask<Void, Integer, Producto>{

        public WeakReference<ParseObject> productoParseWeak;
        public int pos, size;

        ProductoDownloadTask(ParseObject productoParse, int pos, int size){
            this.productoParseWeak = new WeakReference<ParseObject>(productoParse);
            this.pos = pos;
            this.size = size;
        }


        @Override
        protected Producto doInBackground(Void... voids) {
            return retrieveProducto(productoParseWeak.get(), pos, size);
        }


        @Override
        protected void onPostExecute(Producto producto){
            app.productos.add(producto);
            catalogo.setProductos(app.productos);
            catalogoAdapter.notifyDataSetChanged();
            Log.d(TAG, "Notificando producto "+producto.getCodigo()+" cargado");
            Log.d(TAG,"app.productos: "+app.productos.size()+" adapter: "+catalogoAdapter.getCount());

            //Verificar y Notificar Carga Completa de Productos
            if(pos == size -1) onProductosCargados();

        }
    }

    /**
     * AsyncTask que se encarga de descargar las imagenes de los productos del catalogo
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
//                    Log.d(TAG, "Buscando imagen en SD "+ file2.getAbsolutePath());
                    bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());

                    if(bitmap == null){
                        //Descarga imagen del server de IDEA
//                        Log.d(TAG, "Imagen no existe localmente. Descargando del servidor");
                        InputStream in = new java.net.URL(params[0]).openStream();

                        BitmapFactory.Options opt = new BitmapFactory.Options();
                        opt.inPurgeable = true;
                        opt.inInputShareable = true;
                        opt.inPreferredConfig = Bitmap.Config.RGB_565;

                        bitmap = BitmapFactory.decodeStream(in, null, opt);
                        in.close();

                        //Guardar imagen en SD
                        if(isExternalStorageWritable()){
                            appDir = mContext.getExternalFilesDir("img/");
                            File fileSave = new File(appDir,fileName);
                            if(!fileSave.exists())
                                if(fileSave.createNewFile()){
//                                    Log.d(TAG, "Guardando imagen en "+ fileSave.getAbsolutePath());
                                    FileOutputStream out = new FileOutputStream(fileSave);
                                    Bitmap bitmapSave = Bitmap.createBitmap(bitmap);
                                    bitmapSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                    out.flush();
                                    out.close();
                                }
                        }else{
//                            Log.d(TAG, "No se pudo salvar la imagen "+fileName);
                            Toast.makeText(mContext, "No se pudo salvar la imagen "+fileName, Toast.LENGTH_SHORT).show();
                        }

                        return bitmap;
                    }else{
                        //Imagen está en SD
                        return bitmap;
                    }
                }else{
//                    Log.d(TAG, "Tarjeta SD no disponible ");
                    Toast.makeText(mContext, getString(R.string.sd_card_unavailable), Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
//                Log.e("ImageDownload Exception: ", e.getMessage());
            }
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) bitmap = null;

            Producto prod = productoWeakReference.get();
            if (null != prod) {
                prod.setImagen(bitmap);
                if(catalogoAdapter != null) catalogoAdapter.notifyDataSetChanged();
            }
        }
    }

    /**
     * Metodo llamado al haber seleccionado un pedido para isNuevoPedido desde el Dashboard
     */
    private void editarPedido() {
        Log.d(TAG, "Modificar Pedido " + modificarPedidoId);
        carritoProgressDialog.show();
        clienteSpinner.setVisibility(View.INVISIBLE);
        ParseObject parseObject = findPedidoById(modificarPedidoId);
        app.pedido.setParseObject(parseObject);

        //Productos en pedido
        ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
        productosEnPedido.setCachePolicy(getParseCachePolicy());
        productosEnPedido.setLimit(QUERY_LIMIT);
        productosEnPedido.whereEqualTo("pedido", parseObject);
        productosEnPedido.include("producto");
        productosEnPedido.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> productosPedidoHasProductos, ParseException e) {
                for (ParseObject productoPedidoHasProductos : productosPedidoHasProductos) {
                    //Agregar los relacionados al pedido en el carrito
                    ParseObject productoPedidoParse = productoPedidoHasProductos.getParseObject("producto");
                    for (Producto productoPedidoLocal : app.productos) {
                        if (productoPedidoParse.get("codigo").equals(productoPedidoLocal.getCodigo())) {
                            productoPedidoLocal.setCantidad(productoPedidoHasProductos.getInt("cantidad") + productoPedidoHasProductos.getInt("excedente"));
//                                    Log.d(TAG, "Meta en rechazo para prod: " + String.valueOf(productoPedidoHasProductos.getInt("cantidad") + productoPedidoLocal.getExistencia()));
                            //Agregar cantidad a existencia temporalmente
                            productoPedidoLocal.setExistencia(productoPedidoHasProductos.getInt("cantidad") + productoPedidoLocal.getExistencia());
                            productoPedidoLocal.setIsInCarrito(true);
//                                    Log.d(TAG, "Agregando " + productoPedidoHasProductos.getInt("cantidad") + " productos " + productoPedidoParse.get("codigo") + "(" + productoPedidoParse.getObjectId() + ") a pedido");
                            carritoAdapter.getCarrito().addProducto(productoPedidoLocal);
                        }
                    }
                }
                carritoAdapter.setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
                carritoAdapter.notifyDataSetChanged();
                carritoProgressDialog.dismiss();
                carritoListView.smoothScrollToPosition(0);
                carritoAdapter.showCarrito();
            }
        });
    }

    /**
     * Metodo llamado al haber seleccionado crear un nuevo pedido a partir de uno existente desde el Dashboard
     */
    private void clonarPedido() {
        Log.d(TAG, "Clonar Pedido " + modificarPedidoId);
        carritoProgressDialog.show();
        ParseObject parseObject = findPedidoById(modificarPedidoId);
        app.pedido.setParseObject(parseObject);
        //Productos en pedido
        final ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
        productosEnPedido.setCachePolicy(getParseCachePolicy());
        productosEnPedido.setLimit(QUERY_LIMIT);
        productosEnPedido.whereEqualTo("pedido", parseObject);
        productosEnPedido.include("producto");
        productosEnPedido.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> pedidoConProductoObjects, ParseException e) {
                for (ParseObject pedidoConProductoObj : pedidoConProductoObjects) {
                    //Agrego los relacionados al pedido en el carrito
                    ParseObject prodAdd = pedidoConProductoObj.getParseObject("producto");
                    for (Producto aProdsModPedido : app.productos) {
                        if (prodAdd.get("codigo").equals(aProdsModPedido.getCodigo())) {
                            aProdsModPedido.setCantidad(pedidoConProductoObj.getInt("cantidad") + pedidoConProductoObj.getInt("excedente"));
                            aProdsModPedido.setIsInCarrito(true);
//                                        Log.d(TAG, "Agregando "+pedidoConProductoObj.getInt("cantidad")+" productos "+prodAdd.get("codigo")+"("+prodAdd.getObjectId()+") a pedido");
                            carritoAdapter.getCarrito().addProducto(aProdsModPedido);
                        }
                    }
                }
                carritoAdapter.setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
                carritoAdapter.notifyDataSetChanged();
                carritoProgressDialog.dismiss();
                carritoListView.smoothScrollToPosition(0);
                carritoAdapter.showCarrito();
            }
        });
    }

    /**
     * Accion que es llamada al hacer tap sobre el total del Carrito
     */
    protected void generarFactura() {
        GrupoIdea app = (GrupoIdea) getApplication();
        if(app.pedido == null)  app.pedido = new Pedido();

        app.pedido.setProductos(carrito.getProductos());

        if(carrito.getProductos().size()>0){
            Bundle bundle = new Bundle();
            if(modificarPedidoId != null){
                //Obtener indice de cliente
                for(int j=0, size= clientes.size(); j<size; j++){
                    if (clientes.get(j).getNombre().equalsIgnoreCase(clienteNombre)){
                        clienteSpinner.setSelection(j);
                        clienteSelected = j;
                        clienteSpinner.setEnabled(false);
                        app.clienteActual = clientes.get(j);
                    }
                }
                Log.d(TAG, "id de Pedido a modificar: " + modificarPedidoId + " #" + modificarPedidoNum + " del Cliente: " + app.clienteActual.getNombre());
            }else{
                app.clienteActual = clientes.get(clienteSpinner.getSelectedItemPosition());
                Log.d(TAG, "Nuevo pedido de Cliente: " + app.clienteActual.getNombre());
            }

            dispatchActivity(GestionPedidosActivity.class, bundle, false);

        }else{
            Toast.makeText(getBaseContext(), getString(R.string.warning_agregar_elementos_carrito), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Metodo que inicializa el layout del catalogo de productos
     */
    private void initCatalogoLayout(){
        catalogoAdapter = new BannerProductoCatalogo(this, catalogo, carritoListView);
        marcasAdapter.setAdapterCatalogo(catalogoAdapter);

        if(gridCatalogo != null) {
            gridCatalogo.setAdapter(catalogoAdapter);
            gridCatalogo.setOnTouchListener(new OnTouchListener() {
                private int xDown, xUp, xDiff, yDiff, yDown, yUp;

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return handleGestures(event);
                }

                protected boolean handleGestures(MotionEvent event) {
                    if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                        xDown = (int) event.getX();
                        yDown = (int) event.getY();
                    }

                    if (event.getActionMasked() == MotionEvent.ACTION_UP) {
                        xUp = (int) event.getX();
                        yUp = (int) event.getY();

                        xDiff = xDown - xUp;
                        yDiff = yDown - yUp;
                        if (Math.abs(yDiff) < 200 && Math.abs(xDiff) > 200) {
                            if (xDiff > 0) {
                                if (!isMenuRightShowed() && !isMenuLeftShowed()) showRightMenu();
                                else if (isMenuLeftShowed()) hideMenuLeft();
                            } else {
                                if (!isMenuRightShowed() && !isMenuLeftShowed()) showLeftMenu();
                                else if (isMenuRightShowed()) hideMenuRight();
                            }
                        }
                    }
                    return false;
                }
            });
        }
    }

    /**
     * Metodo que inicializa el layout para el menú izquierdo o filtro de categorias
     */
    private void initFiltroLayout() {
        categoriasFiltro.setAdapter(categoriasAdapter);
        categoriasAdapter.setCategoriasListView(categoriasFiltro);
        categoriasAdapter.notifyDataSetChanged();

        marcasFiltro.setAdapter(marcasAdapter);
        marcasAdapter.setMarcasListView(marcasFiltro);
        marcasAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo que inicializa el layout del menú izquierdo, o carrito
     */
    private ListView initCarritoLayout(){
        carritoListView = (ListView) carritoLayout.findViewById(R.id.carrito_list_view);
        RelativeLayout totalCarritoLayout = (RelativeLayout) carritoLayout.findViewById(R.id.carrito_total_layout);
        if(totalCarritoLayout != null) {
            totalCarritoLayout.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    generarFactura();
                }
            });
        }
        if(carritoListView != null) {
            carritoListView.setAdapter(carritoAdapter);
            carritoListView.setSelection(carritoListView.getAdapter().getCount() - 1);
        }
        return carritoListView;
    }

    private void initCarritoProgressDialog() {
        carritoProgressDialog = new ProgressDialog(this);
        carritoProgressDialog.setTitle("Cargando...");
        carritoProgressDialog.setMessage("Cargando Carrito, por favor espere...");
        carritoProgressDialog.setIndeterminate(true);
        carritoProgressDialog.setCancelable(false);
    }

    /**
     * Procedimiento que actualiza los precios comerciales de los productos del catalogo con respecto al cliente seleccionado
     */
    public void updatePreciosComerciales(){
        Double descCliente = app.clientes.get(clienteSelected).getDescuento()/100.0;
        Double precio;
        if(catalogo == null) Log.d(TAG, "Catalogo null");
        if(catalogo != null && catalogo.getProductos() == null) Log.d(TAG, "Productos Catalogo null");
        for(Producto prod: catalogo.getProductos()){
            precio = prod.getPrecio();
            prod.setPrecioComercial(precio - (precio * descCliente));
        }
        carritoAdapter.notifyDataSetChanged();
        catalogoAdapter.notifyDataSetChanged();
    }

    /**
     * Procedimiento que actualiza el monto total del carrito en el layout
     * @param total String del monto total a colocar en el layout
     */
    protected void setTotalCarrito(String total) {
        TextView textView;
        RelativeLayout layout;
        layout = (RelativeLayout) this.getRightMenuLayout();
        if(layout != null) {
            layout = (RelativeLayout) layout.findViewById(R.id.carrito_total_layout);
            if(layout != null) {
                textView = (TextView) layout.findViewById(R.id.carrito_total_precio_text_view);
                if(textView != null) textView.setText(total);
            }
        }
    }

    /**
     * Proceso que establece el valor del descuento manual para el <code>Producto</code> especificado
     * @param producto <code>Producto</code> sobre el cual se va a aplicar el descuento
     */
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
                if (input.getText() != null && !input.getText().toString().isEmpty()) {
                    Double valor = Double.parseDouble(input.getText().toString());
                    if (valor >= MIN_DESC_MAN && valor <= MAX_DESC_MAN) {
                        if(valor == 0.0){
                            producto.setDescuentoManual(0.0);
                            Toast.makeText(oThis, "Descuento manual eliminado", Toast.LENGTH_LONG).show();
                        }else{
                            producto.setDescuentoManual(valor);
                            Toast.makeText(oThis, "Porcentaje de descuento manual asignado", Toast.LENGTH_LONG).show();
                        }

                        carritoAdapter.notifyDataSetChanged();
                        carritoAdapter.getCarrito().recalcularMontos();
                        setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());

                    } else Toast.makeText(oThis, "Porcentaje de descuento manual no valido", Toast.LENGTH_LONG).show();
                } else {
                    producto.setDescuentoManual(0.0);
                    carritoAdapter.notifyDataSetChanged();
                    carritoAdapter.getCarrito().recalcularMontos();
                    setTotalCarrito(carritoAdapter.getCarrito().calcularTotalString());
                    Toast.makeText(oThis, "Descuento manual eliminado", Toast.LENGTH_LONG).show();
                }
            }
        })
        .setNegativeButton(R.string.cancelar, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // Dialogo cancelado
                Toast.makeText(oThis, "Descuento manual no establecido", Toast.LENGTH_LONG).show();
            }
        });
        builder.create();
        builder.show();
    }

    /**
     * Busca una Categoria por nombre, devuelve <code>null</code> de no existir
     * @param name nombre de categoria dentro del ParseObject de Producto
     * @return <code>Categoria</code> o <code>null</code> de no existir categoria asociada
     */
    protected Categoria findCategoriaByName(String name) {
        for (Categoria categoria : categorias) {
            if ( categoria != null && name.equalsIgnoreCase(categoria.getNombre())) return categoria;
        }
        return null;
    }

    /**
     * Busca un GrupoCategorias por nombre, devuelve <code>null</code> de no existir
     * @param name nombre de grupo de categorias dentro del <code>ParseObject</code> de Producto
     * @return <code>GrupoCategorias</code> o <code>null</code> de no existir grupo de categorias asociado
     */
    public GrupoCategorias findGrupoCategoriasByName(String name) {
        for (GrupoCategorias gruposCategoria : gruposCategorias) {
            if (gruposCategoria != null && name.equalsIgnoreCase(gruposCategoria.getNombre())) return gruposCategoria;
        }
        return null;
    }

    /**
     * Procedimiento que obtiene todos los descuentos de Parse y los almacena en <code>descuentosParse</code>
     */
    private void getDescuentosFromParse(){
        if(null == queries) queries = new ArrayList<ParseQuery>();

        ParseQuery query = new ParseQuery("Descuento");
        queries.add(query);
        query.setLimit(QUERY_LIMIT);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> descuentos, ParseException e) {
                if (e == null) {
                    Log.d(TAG, "Descuentos obtenidos");
                    descuentosParse = descuentos;
                }
            }
        });
    }

    /**
     * Funcion que busca los descuentos pertenecientes al <code>objectId</code> especificado
     * @return Descuentos buscado en forma de {@link java.util.ArrayList} de {@link com.parse.ParseObject}, <code>null</code> otherwise
     */
    public ArrayList<ParseObject> findDescuentosByRelatedObjectId(String relatedObjectId){
        ArrayList<ParseObject> descuentos = new ArrayList<ParseObject>();
        if(descuentosParse != null){
            for(ParseObject descuento:descuentosParse){
                if(descuento.getString("perteneceAObjectId").equals(relatedObjectId)) descuentos.add(descuento);
            }
            if(descuentos.size() != 0) return descuentos;
        }
        return null;
    }

    public ParseObject findPedidoById(String pedidoId){
        for (ParseObject pedido:app.pedidos){
            if(pedido.getObjectId().equals(pedidoId)) return pedido;
        }
        return null;
    }

 }
