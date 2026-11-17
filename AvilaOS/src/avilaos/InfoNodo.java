/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//copia de los datos de un archivo o directorio para que la ventana los muestre
//la ventana nunca toca los nodos de verdad
public class InfoNodo {
    public final String ruta;
    public final String nombre;
    public final TipoNodo tipo;
    public final String dueno;
    public final Permiso permiso;
    public final int primerBloque;
    public final int bloques;
    public final String creador;
    public final int color;
    //que tan adentro del arbol esta: los hijos de la raiz tienen nivel 1
    public final int nivel;

    public InfoNodo(NodoArchivo n, int nivel) {
        ruta = n.getRuta();
        nombre = n.getNombre();
        tipo = n.getTipo();
        dueno = n.getDueno();
        permiso = n.getPermiso();
        primerBloque = n.getPrimerBloque();
        bloques = n.getBloques();
        creador = n.getCreador();
        color = n.getColor();
        this.nivel = nivel;
    }

    public boolean esDirectorio() {
        return tipo == TipoNodo.DIRECTORIO;
    }

    //asi sale escrito en el arbol
    @Override
    public String toString() {
        return nombre;
    }
}
