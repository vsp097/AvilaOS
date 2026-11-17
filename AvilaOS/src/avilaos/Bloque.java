/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//un bloque del disco
public class Bloque {

    private int numero;
    private boolean ocupado = false;
    //numero del siguiente bloque del mismo archivo, -1 si es el ultimo
    private int siguiente = -1;
    //ruta del archivo o nombre del proceso suspendido que lo ocupa
    private String ocupante = "";
    //numero de color del archivo, -1 si no tiene
    private int color = -1;
    //true si el bloque es del area de intercambio
    private boolean swap;

    public Bloque(int numero, boolean swap) {
        this.numero = numero;
        this.swap = swap;
    }

    public Bloque copia() {
        Bloque b = new Bloque(numero, swap);
        b.ocupado = ocupado;
        b.siguiente = siguiente;
        b.ocupante = ocupante;
        b.color = color;
        return b;
    }

    public void ocupar(String ocupante, int color) {
        this.ocupado = true;
        this.ocupante = ocupante;
        this.color = color;
        this.siguiente = -1;
    }

    public void liberar() {
        ocupado = false;
        ocupante = "";
        color = -1;
        siguiente = -1;
    }

    public int getNumero() {
        return numero;
    }

    public boolean estaOcupado() {
        return ocupado;
    }

    public int getSiguiente() {
        return siguiente;
    }

    public void setSiguiente(int s) {
        siguiente = s;
    }

    public String getOcupante() {
        return ocupante;
    }

    public void setOcupante(String o) {
        ocupante = o;
    }

    public int getColor() {
        return color;
    }

    public boolean esSwap() {
        return swap;
    }
}
