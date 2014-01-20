package com.grupoidea.ideaapp.models;

public class Meta {
	/** Valor entero o decimal con el valor final de la meta*/
	private int cantMeta;
	private int cantFacturado;
    private int cantPedido;
    private double valorBs;
    private Producto producto;

	public int getCantMeta() {
		return cantMeta;
	}
	public void setCantMeta(int cantMeta) {
		this.cantMeta = cantMeta;
	}
	public int getCantFacturado() {
		return cantFacturado;
	}
	public void setCantFacturado(int cantFacturado) {
		this.cantFacturado = cantFacturado;
	}
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public int getCantPedido() {
        return cantPedido;
    }

    public void setCantPedido(int cantPedido) {
        this.cantPedido = cantPedido;
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
}
