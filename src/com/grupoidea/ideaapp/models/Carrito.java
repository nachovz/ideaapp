package com.grupoidea.ideaapp.models;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class Carrito {
	private ArrayList<Producto> productos;
    public DecimalFormat df = new DecimalFormat("###,###,##0.##");
	
	/** Constructor por defecto, permite instanciar el listado de productos.*/
	public Carrito() {
		productos = new ArrayList<Producto>();
	}
	
	public ArrayList<Producto> getProductos() {
		return productos;
	}
	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
	}
	
	/** Permite retornar el valor del total sumarizado de los productos agregados al carrito.*/
	public double calcularTotalValue() {
		double totalValue = 0;
		Producto productoActual = null;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			totalValue += productoActual.getPrecioComercialTotal();
		}
		return totalValue;
	}
	
	/** Permite retornar el valor del total sumarizado de los productos agregados al carrito.*/
	public String calcularTotalString() {
		String total;
		double totalValue;
		
		totalValue = calcularTotalValue(); 
		return ""+df.format(totalValue)+" Bs.";
	}
	
	/** Permite determinar el indice de un producto si se encuentra actualmente en el listado de productos
	 *  del carrito, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Entero con la posicion del producto en el listado de productos del carrito.*/
	public int findProductoIndex(String id) {
		Producto productoActual = null;
		int index = -1;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
				index = i;
			}
		}
		return index;
	}
	
	/** Permite determinar si un un producto se encuentra actualmente en el listado de productos
	 *  del carrito, es necesario suministrar el identificador unico del producto.
	 *  @param id Entero con el identificador unico del producto
	 *  @return Objeto de tipo de Producto con el producto solicitado, si el producto no esta en el 
	 *  		listado de productos del carrito retorna null.*/
	public Producto findProductoById(String id) {
		Producto productoActual = null;
		Producto productoFinal = null;
		
		for(int i=0; i<productos.size(); i++) {
			productoActual = productos.get(i);
			if(productoActual != null && productoActual.getId().equals(id)) {
				productoFinal = productoActual;
				break;
			}
		}
		return productoFinal;
	}
	/** Permite determinar si un producto existe en el carrito para agregarlo al listado 
	 *  de productos del carrito o sumarle la cantidad del existente
	 *  @parama producto Objeto que contiene la definicion del producto ha ser agregado al carrito.*/
	public void addProducto(Producto productoAdd) {
		Producto productoFinded;
		productoFinded = findProductoById(productoAdd.getId());
		if(productoFinded != null) {
			productoFinded.addCantidad();
		} else {
			this.productos.add(productoAdd);
		}
		
	}
	/** Permite remover un producto especifico del listado de productos del carrito.
	 *  @param indice Entero con el indice del producto que se desea eliminar.*/
	public void removeProducto(int indice) {
		this.productos.remove(indice);
	}
	/** Retorna el numero de elementos en el carrito.
	 */
	public int count(){
		return productos.size();
	}

    public void recalcularDescuentosGrupoCategoria(Producto prodPivot){
        ArrayList<String> catsGrupo;
        ArrayList<Producto> prodGroup = new ArrayList<Producto>();
        int cantGrupo=0, size=productos.size();
        String categoriaProd = prodPivot.getCategoria();
//        Log.d("DEBUG", "Calculando descuento para " + prodPivot.getNombre()+" cantidad: "+prodPivot.getCantidad());
//        Log.d("DEBUG","categoria: "+ categoriaProd);
        if(prodPivot.getRelacionadas() != null){
            catsGrupo= prodPivot.getRelacionadas();

            //cantidad incial es la cantidad de productos pivot
            cantGrupo=prodPivot.getCantidad();
//            Log.d("DEBUG","categorias: "+catsGrupo.get(0));

            //desplazarse por los productos buscando productos en categorias relacionadas que estén en el carrito
            for(int i=0; i<size; i++){
                if(productos.get(i).getIsInCarrito() && !(productos.get(i).getId().equals(prodPivot.getId())) && productos.get(i).isInCategoryGroup(catsGrupo)){
                    //agrego los productos que coincidan con sus cantidades
                    cantGrupo+=productos.get(i).getCantidad();
                    prodGroup.add(productos.get(i));
//                    Log.d("DEBUG", "Producto relacionado: " + productos.get(i).getNombre()+" cantidad: "+productos.get(i).getCantidad());
                }
            }
            //agrego mi producto pivot al final
            prodGroup.add(prodPivot);
//            Log.d("DEBUG", "Cantidad total: " + cantGrupo);
            //calcular descuento aplicado y guardar el mayor
            double descAplGroup= prodPivot.getGrupoCategoria().calcularDescuentoAplicado(cantGrupo);
//            Log.d("DEBUG", "desc aplicado= "+descAplGroup);
            //aplicar mayor descuento a todos los productos
            for(Producto prod:prodGroup){
                if(prod.getDescuentoAplicado()<=descAplGroup){
                    prod.setDescuentoAplicado(descAplGroup);
                }else{
                    prod.setDescuentoAplicado(prod.calcularDescuentoAplicado());
                }
            }
        }
    }
}
