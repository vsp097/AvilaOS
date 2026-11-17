/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;

/**
 *
 * @author andrea
 */
//un directorio o un archivo del arbol del sistema de archivos
public class NodoArchivo {

    private String nombre;
    private TipoNodo tipo;
    private String dueno;
    private Permiso permiso;
    //solo para archivos: donde empieza su cadena de bloques y cuantos tiene
    private int primerBloque = -1;
    private int bloques = 0;
    //nombre del proceso que lo creo
    private String creador;
    //numero de color con el que se pinta en el disco y en la tabla
    private int color = -1;
    private NodoArchivo padre;
    private Listas<NodoArchivo> hijos = new Listas<>();

    public NodoArchivo(String nombre, TipoNodo tipo, String dueno, Permiso permiso, String creador) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.dueno = dueno;
        this.permiso = permiso;
        this.creador = creador;
    }

    public void agregarHijo(NodoArchivo hijo) {
        hijo.padre = this;
        hijos.agregar(hijo);
    }

    public void quitarHijo(NodoArchivo hijo) {
        hijos.eliminar(hijo);
        hijo.padre = null;
    }

    public NodoArchivo buscarHijo(String nombre) {
        for (int i = 0; i < hijos.len(); i++) {
            if (hijos.retornar(i).nombre.equals(nombre)) {
                return hijos.retornar(i);
            }
        }
        return null;
    }

    //la ruta completa se arma subiendo por los padres hasta la raiz
    public String getRuta() {
        if (padre == null) {
            return "/";
        }
        String base = padre.getRuta();
        return base.equals("/") ? "/" + nombre : base + "/" + nombre;
    }

    public boolean esDirectorio() {
        return tipo == TipoNodo.DIRECTORIO;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String n) {
        nombre = n;
    }

    public TipoNodo getTipo() {
        return tipo;
    }

    public String getDueno() {
        return dueno;
    }

    public Permiso getPermiso() {
        return permiso;
    }

    public int getPrimerBloque() {
        return primerBloque;
    }

    public int getBloques() {
        return bloques;
    }

    public void setBloques(int primerBloque, int bloques) {
        this.primerBloque = primerBloque;
        this.bloques = bloques;
    }

    public String getCreador() {
        return creador;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int c) {
        color = c;
    }

    public NodoArchivo getPadre() {
        return padre;
    }

    public Listas<NodoArchivo> getHijos() {
        return hijos;
    }
}
