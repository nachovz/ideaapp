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
import com.grupoidea.ideaapp.models.Pedido;
import com.grupoidea.ideaapp.models.Producto;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

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
	/** ArrayList que contiene los productos que se mostraran en el grid del catalogo*/
	private static ArrayList<Producto> catalogoProductos;
    /** Objeto que representa al catalogo.*/
    public static Catalogo catalogo;
	/** Objeto que representa al carrito de compras del catalogo.*/
	private Carrito carrito;
    private ListView listCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del carrito y el layout de cada producto*/
	protected static BannerProductoCarrito adapterCarrito;
	/** Adapter utilizado como puente entre el ArrayList de productos del catalogo y el layout de cada producto*/
	public static BannerProductoCatalogo adapterCatalogo;
    public String modificarPedidoId, modificarPedidoNum;
    /** Minimo y maximo valor para descuentos manueales*/
    public final static Double MIN_DESC_MAN = 0.0, MAX_DESC_MAN = 100.0;
    protected ArrayList<GrupoCategorias> gruposCategorias;
    protected ArrayList<Categoria> categorias;
//    protected String categoriaActual="Todas", marcaActual="Todas";
    protected static ArrayList<String> marcas;
    protected static MarcasAdapter marcasAdapter;
    protected static CategoriasAdapter categoriasAdapter;
    public ListView categoriasFiltro;
    public ListView marcasFiltro;
    protected static  Context mContext;
    protected ProgressBar descuentosProgressBar;
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

        //mostrar nombre de usuario
        setMenuTittle(ParseUser.getCurrentUser().getUsername());

        //Dialogo de carga carrito
        carritoProgressDialog = new ProgressDialog(this);
        carritoProgressDialog.setTitle("Cargando...");
        carritoProgressDialog.setMessage("Cargando Carrito, por favor espere...");
        carritoProgressDialog.setIndeterminate(true);
        carritoProgressDialog.setCancelable(false);

        //Obtener datos de pedido (en caso de que se esté modificando o clonando uno
        if(app.pedido != null){
            modificarPedidoId = app.pedido.getObjectId();
            modificarPedidoNum = app.pedido.getNumPedido();
            clienteNombre = app.clienteActual.getNombre();
        }

		setParentLayoutVisibility(View.GONE);
		setContentView(R.layout.activity_catalogo_layout);

        //Poblar Spinner de Clientes e inflar
        clienteSpinner.setAdapter(adapter);
        clienteSpinner.setEnabled(true);
        clienteSpinner.setVisibility(View.VISIBLE);
        clienteSpinner.setSelection(0);
        clienteSelected = 0;
        clienteSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                clienteSelected = i;
                Log.d(TAG, "Cliente seleccionado: " + i);
                updatePreciosComerciales();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        marcas = new ArrayList<String>();
        categoriasAdapter = new CategoriasAdapter(mContext);
        marcasAdapter = new MarcasAdapter(mContext, categoriasAdapter);
        categoriasAdapter.setMarcasAdapter(marcasAdapter);
//        categoriaActual = getString(R.string.todas);
//        marcaActual = getString(R.string.todas);
	}

    @Override
    public void onBackPressed(){
        Log.d(TAG, "accionando onBackPressed");
        //Correr Thread para cancelar queries en proceso
        new Thread(new Runnable() {
            public void run(){
                for(ParseQuery query: queries){
                    if(null != query) query.cancel();
                }
            }
        }).start();

//        this.dispatchActivity(DashboardActivity.class, null, true);
        this.finish();
    }

    @Override
    protected Request getRequestAction() {
        getDescuentosFromParse();
        Request req = new Request(Request.PARSE_REQUEST);
        ParseQuery query = new ParseQuery("Producto");
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
        ArrayList<Producto> productos = new ArrayList<Producto>();
        gruposCategorias = new ArrayList<GrupoCategorias>();
        categorias = new ArrayList<Categoria>();
        descuentosProgressBar = (ProgressBar) findViewById(R.id.descuentosProgressBar);
        descuentosProgressBar.setVisibility(View.VISIBLE);
        descuentosProgressBar.setMax(productosParse.size()*3);
        descuentosProgressBar.setProgress(1);
        Toast.makeText(mContext, "Cargando Descuentos, por favor espere.", Toast.LENGTH_SHORT).show();

        GrupoIdea app = (GrupoIdea) getApplication();
        app.productos = productos;
        app.productosParse = productosParse;

        Producto producto;
        RelativeLayout menuRight, menuLeft;
        listCarrito = null;

        //Cargar productos desde Parse
        for (int i=0, size=productosParse.size(); i<size; i++) {
            ParseObject parseObject = productosParse.get(i);
            producto = retrieveProducto(parseObject, i, size);
            productos.add(producto);
        }

        //Inicializar menu izquierdo (Filtro de Marcas y Categorias)
        menuLeft = (RelativeLayout) getMenuLeft();
        if(menuLeft != null){
            initMenuLeft();
        }

        //Inicializar menu derecho (Carrito)
        carrito = new Carrito(categorias, gruposCategorias);
        adapterCarrito = new BannerProductoCarrito(this, carrito);
        menuRight = (RelativeLayout) getMenuRight();
        if(menuRight != null) {
            listCarrito = initMenuRight(menuRight);
        }

        if(modificarPedidoId == null && getIntent().getIntExtra("status", 0)!= Pedido.ESTADO_RECHAZADO){
            //Pedido Nuevo
            //Pedido no está rechazado ni tiene identificador de pedido a ser modificado/clonado
            Log.d(TAG, "Pedido Nuevo "+getIntent().getIntExtra("status", 0));

        }else if(getIntent().getIntExtra("status", 0)== Pedido.ESTADO_RECHAZADO || getIntent().getIntExtra("status", 0)== Pedido.ESTADO_VERIFICANDO){
            //Editar pedido rechazado
            editarPedido(productos);

        }else{
            //Clonar Pedido
            /* Pedido no está "Rechazado" ni "En Espera" */
            clonarPedido(productos);
        }

        catalogo = new Catalogo(this, productos);
        marcasAdapter.setCatalogo(catalogo);

        if(listCarrito != null) {
            initCatalogoLayout();
        }
    }

    /*---
    * ---------------
    * FUNCIONES Y PROCEDURES PARA OBTENER PRODUCTOS
    * ---------------
    * ---*/

    /**
     * Instanciar nuevo producto en base a un ParseObject
     * @param productoParse {@link com.parse.ParseObject} del producto, obtenido desde Parse
     * @return Instancia de {@link Producto}
     */
	private Producto retrieveProducto(final ParseObject productoParse, final int pos, final int size){
        final boolean[] prodDone = {false}, catDone={false}, groupDone = {false};
		String codigo = productoParse.getString("codigo");
		String nombre = productoParse.getString("nombre");
		double precio = productoParse.getDouble("costo");
		final String objectId = productoParse.getObjectId();
		final Producto producto = new Producto(objectId, nombre, codigo, precio);
        producto.setProductoParse(productoParse);

        //Obtener Descripcion
        producto.setDescripcion(productoParse.getString("descripcion"));

        //Obtener Imagen
        if(null!= productoParse.getString("picture") && !productoParse.getString("picture").isEmpty()){
            retrieveImage(producto, productoParse);
        }else{
            producto.setImagen(BitmapFactory.decodeResource(getResources(), R.drawable.prod_background));
        }

        //Obtener existencia
        ParseQuery queryExistencia = new ParseQuery("Metas");
        queries.add(queryExistencia);
        queryExistencia.whereEqualTo("asesor", ParseUser.getCurrentUser());
        queryExistencia.whereEqualTo("producto", productoParse);
        queryExistencia.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                if (e == null){
                    int existencia = parseObject.getInt("meta") - parseObject.getInt("pedido")-parseObject.getInt("facturado");
                    if(existencia >0){ producto.setExistencia(existencia);
                    }else{ producto.setExistencia(0);}
                }
                else Log.d(TAG, "No existe registro del producto "+objectId+" en la tabla Metas");
            }
        });

        //asignar IVA
        producto.setIva(productoParse.getParseObject("iva").getDouble("porcentaje"));
        //Asignar Excedente
        producto.setExcedente(productoParse.getInt("excedente"));
        //Asignar marca
        producto.setMarca(productoParse.getString("marca"));
        //agregar marca a filtro de catalogo
        Marca marca = marcasAdapter.addMarca(productoParse.getString("marca"));

        /*-------------- PRODUCTOS -------------- */
        //Obtener descuentos por producto
        SparseArray<Double> tablaDescuentosProducto = new SparseArray<Double>();
        ArrayList<ParseObject> descuentos = findDescuentosByRelatedObjectId(productoParse.getObjectId());
        if(descuentos!= null){
            for (ParseObject descuento : descuentos) {
                tablaDescuentosProducto.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
            }
        }else{
            Log.d(TAG, "El Producto " + productoParse.getString("codigo") + " no posee descuentos");
        }

        //Encender flags y actualizar progressBar
        prodDone[0] =true;
        descuentosProgressBar.incrementProgressBy(1);

        //Verificar y Notificar Carga Completa de Productos
        if(pos == size -1 && prodDone[0] && catDone[0] && groupDone[0]){
            descuentosProgressBar.setVisibility(View.GONE);
            Log.d(TAG, "Finalizada carga de productos");
            Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_SHORT).show();
            refresh.setClickable(true);
        }

        producto.setTablaDescuentos(tablaDescuentosProducto);
        /* -------------- FIN PRODUCTOS -------------- */

        /* -------------- CATEGORIA -------------- */
        //Obtener categoria
        ParseObject categoriaParse = productoParse.getParseObject("categoria");

        //Obtener nombre y descuentos de categoria
        if(null != categoriaParse){
            //Revisar si categoria ya existe y agregarla al producto
            Categoria categoria = findCategoriaByName(categoriaParse.getString("nombre"));

            //Si no existe crear una nueva y agregarla a categorias y al producto
            if( categoria == null){
                categoria = new Categoria(categoriaParse.getString("nombre"));
            }

            //Agregar Categoria a Marca
            if(!marca.findCategoria(categoria.getNombre()))
                marca.addCategoria(categoria);
            //Agregar Categoria a Todas
            if(!marcasAdapter.todas.findCategoria(categoria.getNombre()))
                marcasAdapter.todas.addCategoria(categoria);
                marcasAdapter.notifyDataSetChanged();

            //Obtener descuentos por categoria
            final SparseArray<Double> tablaDescuentosCategoria= new SparseArray<Double>();
            descuentos = findDescuentosByRelatedObjectId(categoriaParse.getObjectId());
            if (descuentos != null) {
                for (ParseObject descuento : descuentos) {
                    tablaDescuentosCategoria.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));
                }
            }else{
                Log.d(TAG, "La Categoria " + categoria.getNombre() + " no posee descuentos");
            }

            //Encender flags y actualizar progressBar
            catDone[0]=true;
            descuentosProgressBar.incrementProgressBy(1);

            //Verificar y Notificar Carga Completa de Productos
            if(pos == size -1 && prodDone[0] && catDone[0] && groupDone[0]){
                descuentosProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "Finalizada carga de productos");
                Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_LONG).show();
                refresh.setClickable(true);
            }

            //Establecer descuentos en Categoria
            categoria.setTablaDescuentos(tablaDescuentosCategoria);
            //Agregar Categoria a Producto
            producto.setCategoria(categoria);

        }else{
            //Encender flags y actualizar progressBar
            catDone[0]=true;
            descuentosProgressBar.incrementProgressBy(1);
        }
        /* -------------- FIN CATEGORIA -------------- */

        /* -------------- GRUPO CATEGORIAS -------------- */
        //Obtener nombre y descuentos de grupo de categorias
        //Obtener categoria
        ParseObject grupoParse = productoParse.getParseObject("grupo_categorias");
        if(null != productoParse.getParseObject("grupo_categorias") && null != productoParse.getParseObject("grupo_categorias").getJSONArray("relacionadas")){
            //Revisar si grupo categorias ya existe y agregarlo al producto
            GrupoCategorias grupo = findGrupoCategoriasByName(grupoParse.getString("nombre"));
            //Si no existe crear uno nuevo y agregarlo a gruposCategorias y al producto
            if( grupo == null){
                grupo = new GrupoCategorias(grupoParse.getString("nombre"), grupoParse.getJSONArray("relacionadas"));
                gruposCategorias.add(grupo);
            }

            //obtener descuentos por grupo de categoria
            final SparseArray<Double> tablaDescuentosGrupo = new SparseArray<Double>();

            descuentos = findDescuentosByRelatedObjectId(grupoParse.getObjectId());
            if (descuentos != null) {
                for (int i = 0, sizeDesc = descuentos.size(); i<sizeDesc; i++) {
                    ParseObject descuento = descuentos.get(i);
                    tablaDescuentosGrupo.append(descuento.getInt("cantidad"), descuento.getDouble("porcentaje"));

                    //Verificar y Notificar Carga Completa de Productos
                    if(i == sizeDesc-1 && pos == size -1){
                        Log.d(TAG, "Finalizada carga de productos");
                        Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_LONG).show();
                        refresh.setClickable(true);
                        marcasAdapter.notifyDataSetChanged();
                        categoriasAdapter.notifyDataSetChanged();
                    }
                }
            }else{
                Log.d(TAG, "El Grupo " + grupo.getNombre() + " no posee descuentos");
            }

            //Encender flags y actualizar progressBar
            groupDone[0]=true;
            descuentosProgressBar.incrementProgressBy(1);

            //Verificar y Notificar Carga Completa de Productos
            if(pos == size -1 && prodDone[0] && catDone[0] && groupDone[0]){
                descuentosProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "Finalizada carga de productos");
                Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_LONG).show();
                refresh.setClickable(true);
                marcasAdapter.notifyDataSetChanged();
                categoriasAdapter.notifyDataSetChanged();
            }

            //Verificar y Notificar Carga Completa de Productos
            if(pos == size -1 && prodDone[0] && catDone[0] && groupDone[0]){
                descuentosProgressBar.setVisibility(View.GONE);
            }else{
//                Log.d(TAG,"pasando por producto "+producto.getCodigo());
            }

            //Establecer descuentos de Grupo
            grupo.setTablaDescuentos(tablaDescuentosGrupo);
            //Agregar Grupo a Producto
            producto.setGrupoCategorias(grupo);

        }else{
            //Encender flags y actualizar progressBar
            groupDone[0]=true;
            descuentosProgressBar.incrementProgressBy(1);

            //Verificar y Notificar Carga Completa de Productos
            if(pos == size -1 && prodDone[0] && catDone[0] && groupDone[0]){
                descuentosProgressBar.setVisibility(View.GONE);
                Log.d(TAG, "Finalizada carga de productos");
                Toast.makeText(mContext, "Finalizada carga de descuentos", Toast.LENGTH_LONG).show();
                refresh.setClickable(true);
                marcasAdapter.notifyDataSetChanged();
                categoriasAdapter.notifyDataSetChanged();
            }else{
                Log.d(TAG,"pasando por producto "+producto.getCodigo());
            }
        }
        /* -------------- FIN GRUPO CATEGORIAS -------------- */

        return producto;
	}

    /**
     * Metodo invocado para obtener las imágenes de los productos desde el servidor de IDEA
     * @param producto {@link com.grupoidea.ideaapp.models.Producto} Instancia local del producto
     * @param productoParse {@link ParseObject} del producto obtenido desde Parse
     */
    public void retrieveImage(Producto producto, ParseObject productoParse){
        //Obtener imagen
        if(productoParse.getString("picture")!= null && !productoParse.getString("picture").isEmpty()){
            ImageDownloadTask imgDownloader = new ImageDownloadTask(producto);
            imgDownloader.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,productoParse.getString("picture"));
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
                    Log.d(TAG, "Buscando imagen en SD "+ file2.getAbsolutePath());
                    bitmap = BitmapFactory.decodeFile(file2.getAbsolutePath());

                    if(bitmap == null){
                        //Descarga imagen del server de IDEA
                        Log.d(TAG, "Imagen no existe localmente. Descargando del servidor");
                        InputStream in = new java.net.URL(params[0]).openStream();
                        bitmap = BitmapFactory.decodeStream(in);

                        //Guardar imagen en SD
                        if(isExternalStorageWritable()){
                            appDir = mContext.getExternalFilesDir("img/");
                            File fileSave = new File(appDir,fileName);
                            if(!fileSave.exists())
                                if(fileSave.createNewFile()){
                                    Log.d(TAG, "Guardando imagen en "+ fileSave.getAbsolutePath());
                                    FileOutputStream out = new FileOutputStream(fileSave);
                                    Bitmap bitmapSave = Bitmap.createBitmap(bitmap);
                                    bitmapSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
                                    out.flush();
                                    out.close();
                                }
                        }else{
                            Log.d(TAG, "No se pudo salvar la imagen "+fileName);
                            Toast.makeText(mContext, "No se pudo salvar la imagen "+fileName, Toast.LENGTH_SHORT).show();
                        }

                        return bitmap;
                    }else{
                        //Imagen está en SD
                        return bitmap;
                    }
                }else{
                    Log.d(TAG, "Tarjeta SD no disponible ");
                    Toast.makeText(mContext, "Tarjeta SD no disponible ", Toast.LENGTH_SHORT).show();
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
                    Log.d(TAG,"Imagen de producto "+prod.getCodigo()+" obtenida");
                    prod.setImagen(bitmap);
                    if(adapterCatalogo != null){
                        adapterCatalogo.notifyDataSetChanged();
                    }
                }
            }
        }
    }

    /*---
    * ---------------
    * FIN FUNCIONES Y PROCEDURES PARA OBTENER PRODUCTOS
    * ---------------
    * ---*/

    /*---
    * ---------------
    * FUNCIONES PARA GENERAR PEDIDOS
    * ---------------
    * ---*/

    /**
     * Metodo llamado al haber seleccionado un pedido para editar desde el Dashboard
     * @param productos <code>ArrayList</code> de <code>Producto</code> que contiene los productos del pedido
     */
    private void editarPedido(ArrayList<Producto> productos) {
        Log.d(TAG, "Modificar Pedido " + modificarPedidoId);
        carritoProgressDialog.show();
        //TODO Ver si se puede optimizar esto
        clienteSpinner.setVisibility(View.INVISIBLE);
        final ArrayList<Producto> prodsModPedido=productos;
        //Pedido Id
        final ParseQuery queryPedido = new ParseQuery("Pedido");
        queryPedido.whereEqualTo("objectId", modificarPedidoId);
        queryPedido.include("asesor");
        queryPedido.include("cliente");
        queryPedido.getFirstInBackground(new GetCallback() {
            @Override
            public void done(ParseObject parseObject, ParseException e) {
                app.pedido.setParseObject(parseObject);
                //Productos en pedido
                final ParseQuery productosEnPedido = new ParseQuery("PedidoHasProductos");
                productosEnPedido.whereEqualTo("pedido", parseObject);
                productosEnPedido.include("producto");
                productosEnPedido.findInBackground(new FindCallback() {
                    @Override
                    public void done(List<ParseObject> pedidoConProductoObjects, ParseException e) {
                        //Recorrer los productos relacionados al pedido
                        for (ParseObject pedidoConProductoObj : pedidoConProductoObjects) {
                            //Agregar los relacionados al pedido en el carrito
                            ParseObject prodAdd = pedidoConProductoObj.getParseObject("producto");
                            for (Producto aProdsModPedido : prodsModPedido) {
                                if (prodAdd.get("codigo").equals(aProdsModPedido.getCodigo())) {
                                    aProdsModPedido.setCantidad(pedidoConProductoObj.getInt("cantidad") + pedidoConProductoObj.getInt("excedente"));
                                    Log.d(TAG, "Meta en rechazo para prod: " + String.valueOf(pedidoConProductoObj.getInt("cantidad") + aProdsModPedido.getExistencia()));
                                    //Agregar cantidad a existencia temporalmente
                                    aProdsModPedido.setExistencia(pedidoConProductoObj.getInt("cantidad") + aProdsModPedido.getExistencia());
                                    aProdsModPedido.setIsInCarrito(true);
                                    adapterCarrito.notifyDataSetChanged();
                                    Log.d(TAG, "Agregando " + pedidoConProductoObj.getInt("cantidad") + " productos " + prodAdd.get("codigo") + "(" + prodAdd.getObjectId() + ") a pedido");
                                    adapterCarrito.getCarrito().addProducto(aProdsModPedido);
                                    adapterCarrito.notifyDataSetChanged();
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

    /**
     * Metodo llamado al haber seleccionado crear un nuevo pedido a partir de uno existente desde el Dashboard
     * @param productos <code>ArrayList</code> de <code>Producto</code> que contiene los productos del pedido
     */
    private void clonarPedido(ArrayList<Producto> productos) {
        Log.d(TAG, "Clonar Pedido " + modificarPedidoId);
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
                            for (Producto aProdsModPedido : prodsModPedido) {
                                if (prodAdd.get("codigo").equals(aProdsModPedido.getCodigo())) {
                                    aProdsModPedido.setCantidad(pedidoConProductoObj.getInt("cantidad") + pedidoConProductoObj.getInt("excedente"));
                                    aProdsModPedido.setIsInCarrito(true);
                                    adapterCarrito.notifyDataSetChanged();
//                                        Log.d(TAG, "Agregando "+pedidoConProductoObj.getInt("cantidad")+" productos "+prodAdd.get("codigo")+"("+prodAdd.getObjectId()+") a pedido");
                                    adapterCarrito.getCarrito().addProducto(aProdsModPedido);
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

    /*---
    * ---------------
    * FUNCIONES PARA GENERAR PEDIDOS
    * ---------------
    * ---*/

    /*---
    * ---------------
    * LAYOUT
    * ---------------
    * ---*/

    /**
     * Metodo que inicializa el layout del catalogo de productos
     */
    private void initCatalogoLayout(){
        adapterCatalogo = new BannerProductoCatalogo(this, catalogo, listCarrito);
        marcasAdapter.setAdapterCatalogo(adapterCatalogo);
        /* Elemento que permite mostrar Views en forma de grid.*/
        GridView grid = (GridView) this.findViewById(R.id.catalogo_grid);

        if(grid != null) {
            grid.setAdapter(adapterCatalogo);
            grid.setOnTouchListener(new OnTouchListener() {
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
                                if (!isMenuRightShowed() && !isMenuLeftShowed()) {
                                    showRightMenu();
                                } else if (isMenuLeftShowed()) {
                                    hideMenuLeft();
                                }
                            } else {
                                if (!isMenuRightShowed() && !isMenuLeftShowed()) {
                                    showLeftMenu();
                                } else if (isMenuRightShowed()) {
                                    hideMenuRight();
                                }
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
    private void initMenuLeft() {
        //Filtro Categorias
        categoriasFiltro = (ListView) findViewById(R.id.categorias_filtro_listView);
        categoriasFiltro.setAdapter(categoriasAdapter);
        categoriasAdapter.setCategoriasListView(categoriasFiltro);
//        categoriasFiltro.setSelection(0);
//        categoriasAdapter.notifyDataSetInvalidated();
        categoriasAdapter.notifyDataSetChanged();

        //Filtro Marcas
        marcasFiltro = (ListView) findViewById(R.id.marcas_filtro_listView);
        marcasFiltro.setAdapter(marcasAdapter);
        marcasAdapter.setMarcasListView(marcasFiltro);
//        marcasFiltro.setSelection(0);
//        marcasAdapter.notifyDataSetInvalidated();
        marcasAdapter.notifyDataSetChanged();
    }

    /**
     * Metodo que inicializa el layout del menú izquierdo, o carrito
     * @param menuRight {@link android.widget.RelativeLayout} del Menu Derecho
     */
    private ListView initMenuRight(RelativeLayout menuRight){
        listCarrito = (ListView) menuRight.findViewById(R.id.carrito_list_view);
        RelativeLayout relativeLayout = (RelativeLayout) menuRight.findViewById(R.id.carrito_total_layout);
        if(relativeLayout != null) {
            //Agregar onClick MarcasListener para cuando se haga tap sobre el total del carrito
            relativeLayout.setOnClickListener(new OnClickListener(){
                @Override
                public void onClick(View arg0) {
                    generarFactura();
                }
            });
        }
        if(listCarrito != null) {
            listCarrito.setAdapter(adapterCarrito);
            listCarrito.setSelection(listCarrito.getAdapter().getCount() - 1);
        }

        return listCarrito;
    }

    /**
     * Procedimiento que actualiza los precios comerciales de los productos del catalogo con respecto al cliente seleccionado
     */
    public static void updatePreciosComerciales(){
        Double descCliente = app.clientes.get(clienteSelected).getDescuento()/100.0;
        Double precio;
        for(Producto prod: catalogo.getProductosCatalogo()){
            precio = prod.getPrecio();
            prod.setPrecioComercial(precio - (precio * descCliente));
        }
        adapterCarrito.notifyDataSetChanged();
        adapterCatalogo.notifyDataSetChanged();
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
                if(textView != null) {
                    textView.setText(total);
                }
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
                        Log.d(TAG, valor.toString());
                        if(valor == 0.0){
                            producto.setDescuentoManual(0.0);
                            adapterCarrito.notifyDataSetChanged();
                            adapterCarrito.getCarrito().recalcularMontos();
                            setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                            Toast.makeText(oThis, "Descuento manual eliminado", Toast.LENGTH_LONG).show();
                        }else{
                            producto.setDescuentoManual(valor);
                            adapterCarrito.notifyDataSetChanged();
                            adapterCarrito.getCarrito().recalcularMontos();
                            setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
                            Toast.makeText(oThis, "Porcentaje de descuento manual asignado", Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Toast.makeText(oThis, "Porcentaje de descuento manual no valido", Toast.LENGTH_LONG).show();
                        Log.d(TAG, "porcentaje no valido");
                    }
                } else {
                    producto.setDescuentoManual(0.0);
                    adapterCarrito.notifyDataSetChanged();
                    adapterCarrito.getCarrito().recalcularMontos();
                    setTotalCarrito(adapterCarrito.getCarrito().calcularTotalString());
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

//    /**
//     * Maneja el comportamiento de los taps en el filtro de Marcas y Categorias
//     * @param selected TextView seleccionado
//     */
//    public void onClickFiltroTextView(TextView selected){
//        LinearLayout parent = (LinearLayout) selected.getParent();
//        if (parent != null) {
//            TextView tv = (TextView)parent.getChildAt(0);
//            if (tv != null) {
//                if (getString(R.string.categorias).equals(tv.getText())){
//                    setCategoriaActual(selected);
//                    catalogo.filter(marcaActual, categoriaActual);
//                    adapterCatalogo.notifyDataSetChanged();
//                }else if(getString(R.string.marcas).equals(tv.getText())){
//                    setMarcaActual(selected);
//                    catalogo.filter(marcaActual, categoriaActual);
//                    adapterCatalogo.notifyDataSetChanged();
//                }
//            }
//            if(!selected.equals(parent.getChildAt(1))){
//                //actualizar estilo
//                selected.setBackgroundResource(R.drawable.pastilla_item_selected_filtro);
//                selected.setTextColor(Color.parseColor("#3A70B9"));
//            }
//        }
//    }

    /*---
    * ---------------
    * FIN LAYOUT
    * ---------------
    * ---*/

     /*---
    * ---------------
    * CATEGORIAS
    * ---------------
    * ---*/

//    /**
//     * Agrega la categoria cat a la lista de categorias en el menu lateral de filtros
//     * @param categoria Categoria
//     */
//    public void addCategoriaTextView(Categoria categoria){
//        String cat = categoria.getNombre();
//        if((cat != null) && !isInCategorias(cat)){
//            categorias.add(categoria);
//            TextView tv = new TextView(mContext);
//            tv.setText(cat);
//            tv.setTextColor(Color.parseColor("#FFFFFF"));
//            tv.setTextSize(22);
//            tv.setGravity(Gravity.CENTER);
//            tv.setPadding(10, 10, 10, 10);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 5, 0, 5);
//            tv.setLayoutParams(layoutParams);
//            tv.setBackgroundResource(R.drawable.pastilla_items_filtro);
//            tv.setTypeface(null, Typeface.BOLD_ITALIC);
//            tv.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //poner todos los views en default
//                    TextView item;
//                    for(int i =2, size= categoriasFiltro.getChildCount(); i <size; i++){
//                        item = (TextView) categoriasFiltro.getChildAt(i);
//                        if (item != null) {
//                            item.setBackgroundResource(R.drawable.pastilla_items_filtro);
//                            item.setTextColor(Color.parseColor("#FFFFFF"));
//                        }
//                    }
//                    //aplicar item seleccionado de filtro
//                    onClickFiltroTextView((TextView) v);
//                }
//            });
//            categoriasFiltro = (LinearLayout) findViewById(R.id.categorias_filtro_layout);
//            categoriasFiltro.addView(tv);
//        }
//    }

    /**
     * Revisa si el string cat está contenido dentro de las categorias almacenadas en el servidor
     * @param categoria categoria
     * @return Exito en la busqueda
     */
    public Boolean isInCategorias(String categoria){
        for (Categoria categoria1 : categorias) {
            if (categoria.equalsIgnoreCase(categoria1.getNombre())) return true;
        }
        return false;
    }

//    /**
//     * Establecer categoria seleccionada
//     * @param selected categoria seleccionada
//     */
//    public void setCategoriaActual(TextView selected){
//        if(selected.getText()!= null){
//            categoriaActual = selected.getText().toString();
//            Log.d(TAG, "categoria seleccionada: "+ categoriaActual);
//        }
//    }

    /**
     * Busca una Categoria por nombre, devuelve <code>null</code> de no existir
     * @param name nombre de categoria dentro del ParseObject de Producto
     * @return <code>Categoria</code> o <code>null</code> de no existir categoria asociada
     */
    protected Categoria findCategoriaByName(String name) {
        Categoria categoriaActual, categoriaFinal = null;
        for (Categoria categoria : categorias) {
            categoriaActual = categoria;
            if (categoriaActual != null) {
                if (name.equalsIgnoreCase(categoriaActual.getNombre())) {
                    categoriaFinal = categoriaActual;
                    break;
                }
            }
        }
        return categoriaFinal;
    }

    /*---
    * ---------------
    * FIN CATEGORIAS
    * ---------------
    * ---*/

    /*---
    * ---------------
    * MARCAS
    * ---------------
    * ---*/

//    /**
//     * Agregar marca al menu lateral de filtros
//     * @param mar marca a agregar
//     */
//    protected void addMarcaTextView(String mar){
//        if((mar != null) && !isInMarcas(mar)){
//            marcas.add(mar);
//            TextView tv = new TextView(mContext);
//            tv.setText(mar);
//            tv.setTextColor(Color.parseColor("#FFFFFF"));
//            tv.setTextSize(22);
//            tv.setGravity(Gravity.CENTER);
//            tv.setPadding(10, 10, 10, 10);
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//            layoutParams.setMargins(0, 5, 0, 5);
//            tv.setLayoutParams(layoutParams);
//            tv.setBackgroundResource(R.drawable.pastilla_items_filtro);
//            tv.setTypeface(null, Typeface.BOLD_ITALIC);
//            tv.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    //poner todos los views en default
//                    TextView item;
//                    for(int i =2, size= marcasFiltro.getChildCount(); i <size; i++){
//                        item = (TextView) marcasFiltro.getChildAt(i);
//                        if (item != null) {
//                            item.setBackgroundResource(R.drawable.pastilla_items_filtro);
//                            item.setTextColor(Color.parseColor("#FFFFFF"));
//                        }
//                    }
//                    //aplicar item seleccionado de filtro
//                    onClickFiltroTextView((TextView) v);
//                }
//            });
//            marcasFiltro = (LinearLayout) findViewById(R.id.marcas_filtro_layout);
//            marcasFiltro.addView(tv);
//        }
//    }

    /**
     * Revisa si el string cat está contenido dentro de las marcas almacenadas en el servidor
     * @param marcaCheck <code>String</code> a buscar
     * @return Resultado de la busqueda
     */
    public Boolean isInMarcas(String marcaCheck){
        for (String marca : marcas) {
            if (marca.equals(marcaCheck))
                return true;
        }
        return false;
    }

//    /**
//     * Establece la marca actual
//     * @param selected marca seleccionada
//     */
//    protected void setMarcaActual(TextView selected){
//        if(selected.getText()!= null){
//            marcaActual = selected.getText().toString();
//        }
//    }

    /*---
    * ---------------
    * FIN MARCAS
    * ---------------
    * ---*/

    /*---
    * ---------------
    * GRUPOS DE CATEGORIAS
    * ---------------
    * ---*/

    /**
     * Busca un GrupoCategorias por nombre, devuelve <code>null</code> de no existir
     * @param name nombre de grupo de categorias dentro del <code>ParseObject</code> de Producto
     * @return <code>GrupoCategorias</code> o <code>null</code> de no existir grupo de categorias asociado
     */
    public GrupoCategorias findGrupoCategoriasByName(String name) {
        for (GrupoCategorias gruposCategoria : gruposCategorias) {
            if (gruposCategoria != null) {
                if (name.equalsIgnoreCase(gruposCategoria.getNombre())) {
                    return gruposCategoria;
                }
            }
        }
        return null;
    }

    /*---
    * ---------------
    * FIN GRUPOS DE CATEGORIAS
    * ---------------
    * ---*/

    /*---
    * ---------------
    * DESCUENTOS
    * ---------------
    * ---*/

    /**
     * Procedimiento que obtiene todos los descuentos de Parse y los almacena en <code>descuentosParse</code>
     */
    private void getDescuentosFromParse(){
        ParseQuery query = new ParseQuery("Descuento");
        if(null == queries) queries = new ArrayList<ParseQuery>();
        queries.add(query);
        query.setCachePolicy(ParseQuery.CachePolicy.CACHE_ELSE_NETWORK);
        query.findInBackground(new FindCallback() {
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
                if(descuento.getString("perteneceAObjectId").equals(relatedObjectId)){
                    descuentos.add(descuento);
                }
            }
            if(descuentos.size() != 0) return descuentos;
        }
        return null;
    }

    /*---
    * ---------------
    * FIN DESCUENTOS
    * ---------------
    * ---*/


 }
