package com.grupoidea.ideaapp.models;

import com.parse.ParseObject;

import java.util.ArrayList;

public class Cliente {
	/** Cadena de texto que contiene el nombre del cliente.*/
	private String nombre;
    private String codigo;
    private double descuento;
    private String parseId;
    private ParseObject clienteParse;
	/** Listado de marcas previamente adquiridas por un cliente*/
	private ArrayList<Marca> marcas;
	
	public Cliente(String nombre) {
		this.nombre = nombre;
	}

    public Cliente(ParseObject parseObject){
        this(parseObject.getString("nombre"));
        clienteParse = parseObject;
        parseId = parseObject.getObjectId();
        codigo = parseObject.getString("codigo");
        descuento = parseObject.getDouble("descuentoComercial");
    }
	
	public String getNombre() {
		return nombre;
	}
	public void setNombre(String nombre) {
		this.nombre = nombre;
	}
	public ArrayList<Marca> getMarcas() {
		return marcas;
	}
	public void setMarcas(ArrayList<Marca> marcas) {
		this.marcas = marcas;
	}

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public double getDescuento() {
        return descuento;
    }

    public void setDescuento(double descuento) {
        this.descuento = descuento;
    }

    public String getParseId() {
        return parseId;
    }

    public void setParseId(String parseId) {
        this.parseId = parseId;
    }

    public ParseObject getClienteParse() {
        return clienteParse;
    }

    public void setClienteParse(ParseObject clienteParse) {
        this.clienteParse = clienteParse;
    }

    @Override
    public String toString(){
        return nombre;
    }
}
