/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;
import java.util.concurrent.Semaphore;

/**
 *
 * @author andrea
 */
//el arbol de directorios y archivos, es parte del sistema operativo
//quien lo use tiene que tomar() antes y soltar() despues, igual que el computador
public class SistemaArchivos {

    public static final int LARGO_NOMBRE = 30;

    private NodoArchivo raiz = new NodoArchivo("/", TipoNodo.DIRECTORIO, "admin", Permiso.PUBLICO, "sistema");
    private final Semaphore llave = new Semaphore(1);
    //sube cada vez que cambia el arbol, asi la ventana sabe cuando volver a dibujarlo
    private int version = 0;
    private int siguienteColor = 0;

    public void tomar() {
        llave.acquireUninterruptibly();
    }

    public void soltar() {
        llave.release();
    }

    public NodoArchivo getRaiz() {
        return raiz;
    }

    public int getVersion() {
        return version;
    }

    public int nuevoColor() {
        return siguienteColor++;
    }

    //----- rutas -----

    //revisa que la ruta este bien escrita y la devuelve limpia; lanza error con el motivo si no
    public static String limpiarRuta(String ruta) {
        if (ruta == null || ruta.trim().isEmpty()) {
            throw new IllegalArgumentException("La ruta no puede estar vacia");
        }
        String r = ruta.trim();
        if (!r.startsWith("/")) {
            throw new IllegalArgumentException("La ruta debe empezar con /");
        }
        while (r.length() > 1 && r.endsWith("/")) {
            r = r.substring(0, r.length() - 1);
        }
        if (r.equals("/")) {
            return r;
        }
        String[] partes = r.substring(1).split("/", -1);
        for (int i = 0; i < partes.length; i++) {
            revisarNombre(partes[i]);
        }
        return r;
    }

    public static void revisarNombre(String nombre) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new IllegalArgumentException("El nombre no puede estar vacio");
        }
        if (nombre.length() > LARGO_NOMBRE) {
            throw new IllegalArgumentException("El nombre '" + nombre + "' pasa de " + LARGO_NOMBRE + " caracteres");
        }
        if (nombre.contains("/") || nombre.contains("<") || nombre.contains(">") || !nombre.equals(nombre.trim())) {
            throw new IllegalArgumentException("El nombre '" + nombre + "' tiene caracteres que no se permiten");
        }
    }

    //la ruta del directorio que contiene a esta ruta
    public static String rutaPadre(String ruta) {
        int i = ruta.lastIndexOf('/');
        return i <= 0 ? "/" : ruta.substring(0, i);
    }

    public static String nombreDe(String ruta) {
        return ruta.substring(ruta.lastIndexOf('/') + 1);
    }

    public static String unir(String directorio, String nombre) {
        return directorio.equals("/") ? "/" + nombre : directorio + "/" + nombre;
    }

    //baja por el arbol siguiendo la ruta, devuelve null si no existe
    public NodoArchivo buscar(String ruta) {
        if (ruta == null || !ruta.startsWith("/")) {
            return null;
        }
        if (ruta.equals("/")) {
            return raiz;
        }
        String[] partes = ruta.substring(1).split("/", -1);
        NodoArchivo actual = raiz;
        for (int i = 0; i < partes.length && actual != null; i++) {
            actual = actual.buscarHijo(partes[i]);
        }
        return actual;
    }

    //----- cambios en el arbol -----

    public NodoArchivo crear(String ruta, TipoNodo tipo, String dueno, Permiso permiso, String creador) {
        NodoArchivo padre = buscar(rutaPadre(ruta));
        if (padre == null || !padre.esDirectorio()) {
            throw new IllegalArgumentException("No existe el directorio " + rutaPadre(ruta));
        }
        if (padre.buscarHijo(nombreDe(ruta)) != null) {
            throw new IllegalArgumentException("Ya existe " + ruta);
        }
        NodoArchivo nodo = new NodoArchivo(nombreDe(ruta), tipo, dueno, permiso, creador);
        padre.agregarHijo(nodo);
        version++;
        return nodo;
    }

    public void quitar(NodoArchivo nodo) {
        if (nodo.getPadre() != null) {
            nodo.getPadre().quitarHijo(nodo);
            version++;
        }
    }

    public void renombrar(NodoArchivo nodo, String nuevoNombre) {
        nodo.setNombre(nuevoNombre);
        version++;
    }

    //todos los archivos que hay debajo de un nodo (el mismo si es un archivo), en el orden del arbol
    public Listas<NodoArchivo> archivosDe(NodoArchivo nodo) {
        Listas<NodoArchivo> lista = new Listas<>();
        juntarArchivos(nodo, lista);
        return lista;
    }

    private void juntarArchivos(NodoArchivo nodo, Listas<NodoArchivo> lista) {
        if (!nodo.esDirectorio()) {
            lista.agregar(nodo);
            return;
        }
        for (int i = 0; i < nodo.getHijos().len(); i++) {
            juntarArchivos(nodo.getHijos().retornar(i), lista);
        }
    }

    //todos los nodos menos la raiz, primero cada directorio y despues lo que tiene adentro
    public Listas<NodoArchivo> todos() {
        Listas<NodoArchivo> lista = new Listas<>();
        juntarTodos(raiz, lista);
        return lista;
    }

    private void juntarTodos(NodoArchivo nodo, Listas<NodoArchivo> lista) {
        if (nodo != raiz) {
            lista.agregar(nodo);
        }
        for (int i = 0; i < nodo.getHijos().len(); i++) {
            juntarTodos(nodo.getHijos().retornar(i), lista);
        }
    }

    //----- permisos -----

    //dice si ese usuario puede hacer la operacion; devuelve null si puede o la explicacion si no
    //nodo es el archivo o directorio sobre el que trabaja (para CREAR es el directorio donde va a quedar)
    public static String revisarPermiso(TipoOperacionES op, NodoArchivo nodo, boolean creaDirectorio,
            ModoUsuario modo, String usuario) {
        if (modo == ModoUsuario.ADMINISTRADOR) {
            return null;
        }
        boolean propio = nodo.getDueno().equals(usuario);
        switch (op) {
            case CREAR:
                if (creaDirectorio) {
                    return "en modo usuario no se pueden crear directorios, solo el administrador puede";
                }
                if (!propio) {
                    return "el usuario " + usuario + " solo puede crear archivos dentro de sus propios directorios y "
                            + nodo.getRuta() + " es de " + nodo.getDueno();
                }
                return null;
            case LEER:
                if (!propio && nodo.getPermiso() == Permiso.PRIVADO) {
                    return "el usuario " + usuario + " no puede leer " + nodo.getRuta() + " porque es privado y su dueno es "
                            + nodo.getDueno();
                }
                return null;
            case ACTUALIZAR:
            case ELIMINAR:
                if (nodo.esDirectorio()) {
                    return "en modo usuario no se pueden modificar ni eliminar directorios, solo el administrador puede";
                }
                if (!propio) {
                    return "el usuario " + usuario + " no puede modificar " + nodo.getRuta() + " porque es de "
                            + nodo.getDueno();
                }
                return null;
            default:
                return "esa operacion es solo del sistema operativo";
        }
    }
}
