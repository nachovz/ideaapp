package com.grupoidea.ideaapp.models;

import com.parse.ParseObject;

public class Meta {
	/** Valor entero o decimal con el valor final de la meta*/
	private int cantMeta;
	private int cantFacturado;
    private int cantPedido;
    private double valorBs;
    private Producto producto;
    private ParseObject parseObject;
    private int existencia;


    public Meta(ParseObject parseObject){
        this.setParseObject(parseObject);
        cantMeta = parseObject.getInt("meta");
        cantFacturado = parseObject.getInt("facturado");
        cantPedido = parseObject.getInt("pedido");
        valorBs = parseObject.getDouble("meta_bs");
        updateExistencia();
    }

	public int getCantMeta() {
		return cantMeta;
	}

	public void setCantMeta(int cantMeta) {
		this.cantMeta = cantMeta;
        updateExistencia();
	}

	public int getCantFacturado() {
		return cantFacturado;
	}

	public void setCantFacturado(int cantFacturado) {
		this.cantFacturado = cantFacturado;
        updateExistencia();
	}

    public Producto getProducto() {
        return producto;
    }

    public void setProducto(Producto producto) {
        this.producto = producto;
        updateExistencia();
    }

    public int getCantPedido() {
        return cantPedido;
    }

    public void setCantPedido(int cantPedido) {
        this.cantPedido = cantPedido;
        updateExistencia();
    }

    public double getValorBs() {
        return valorBs;
    }

    public void setValorBs(double valorBs) {
        this.valorBs = valorBs;
    }

    public String getMarca(){
        return producto.getMarca();
    }

    public String getCodigo(){
        return producto.getCodigo();
    }

    public int getExistencia(){
        return existencia;
    }

    public void updateExistencia(){
        int temp = cantMeta - cantPedido - cantFacturado;
        existencia = temp <0? 0 : temp;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

    public void setParseObject(ParseObject parseObject) {
        this.parseObject = parseObject;
    }
}
