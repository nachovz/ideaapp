package com.grupoidea.ideaapp.models;

import com.parse.ParseObject;

import java.util.ArrayList;
import java.util.Date;

public class Pedido {
	/** Estado del pedido */
	public static final int ESTADO_VERIFICANDO = 0;
	public static final int ESTADO_APROBADO = 1;
    public static final int ESTADO_RECHAZADO = 2;
    public static final int ESTADO_ANULADO = 3;
    public static final int ESTADO_TODOS = 4;
    public static final int ESTADO_NUEVO = 5;

	/** Objeto de tipo Cliente asociado al pedido */
	private Cliente cliente;
	/** Direccion de envio del pedido */
	private String direccion;
	/** Fecha de creacion del pedido */
	private Date createdAt;
    private Date updatedAt;
	/** Variable para verificar el estado del pedido */
	private int estado;
	/** Listado de productos que se encuentran en el pedido */
    private ArrayList<Producto> productos;
    private String objectId;
    private String numPedido;
    private ParseObject parseObject;
    private String observaciones;

    public Pedido(ParseObject parse){
        objectId = parse.getObjectId();
        numPedido = parse.getString("num_pedido");
        direccion = parse.getString("direccion");
        createdAt = parse.getCreatedAt();
        updatedAt = parse.getUpdatedAt();
        estado = parse.getInt("estado");

        if(null != parse.getString("comentario_cambio_status") && !parse.getString("comentario_cambio_status").isEmpty()){
            setObservaciones(parse.getString("comentario_cambio_status"));
        }

        parseObject = parse;
    }

    public Pedido(){

    }
	
	public Cliente getCliente() {
		return cliente;
	}
	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}
	public String getDireccion() {
		return direccion;
	}
	public void setDireccion(String direccion) {
		this.direccion = direccion;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public int getEstado() {
		return estado;
	}
	public void setEstado(int estado) {
		this.estado = estado;
	}
	public ArrayList<Producto> getProductos() {
		return productos;
	}
	public void setProductos(ArrayList<Producto> productos) {
		this.productos = productos;
	}

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    public String getNumPedido() {
        return numPedido;
    }

    public void setNumPedido(String numPedido) {
        this.numPedido = numPedido;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }
}
