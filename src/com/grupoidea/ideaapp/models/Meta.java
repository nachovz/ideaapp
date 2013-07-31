package com.grupoidea.ideaapp.models;

public class Meta {
	/** Valor entero o decimal con el valor final de la meta*/
	private double valorFinal;
	private double valorActual;
	private double valorRestante;
    private double valorEspera;
    private double valorBs;
    private Producto producto;

	public double getValorFinal() {
		return valorFinal;
	}
	public void setValorFinal(double valorFinal) {
		this.valorFinal = valorFinal;
	}
	public double getValorActual() {
		return valorActual;
	}
	public void setValorActual(double valorActual) {
		this.valorActual = valorActual;
	}
	public double getValorRestante() {
		return valorRestante;
	}
	public void setValorRestante(double valorRestante) {
		this.valorRestante = valorRestante;
	}
    public Producto getProducto() {
        return producto;
    }
    public void setProducto(Producto producto) {
        this.producto = producto;
    }

    public double getValorEspera() {
        return valorEspera;
    }

    public void setValorEspera(double valorEspera) {
        this.valorEspera = valorEspera;
    }

    public double getValorBs() {
        return valorBs;
    }

    public void setValorBs(double valorBs) {
        this.valorBs = valorBs;
    }
}
