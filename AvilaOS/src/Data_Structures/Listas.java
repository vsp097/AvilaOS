/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author vyckh
 */
//lista enlazada simple, la usamos en vez de las colecciones de java
public class Listas<T> {
    protected Nodo<T> pFirst;
    protected Nodo<T> pLast;
    //cantidad de elementos
    protected int in;

    public Listas() {
        in = 0;
    }

    public Listas(Nodo<T> aux) {
        pFirst = pLast = aux;
        in = 1;
    }

    public void insertar(Nodo<T> aux) {
        aux.pNext = null;
        if (pFirst == null) {
            pFirst = pLast = aux;
        } else {
            pLast.pNext = aux;
            pLast = aux;
        }
        in++;
    }

    //lo mismo que insertar pero recibe el dato directo
    public void agregar(T dato) {
        insertar(new Nodo<T>(dato));
    }

    public T retornar(int x) {
        if (x < 0 || x >= in) {
            return null;
        }
        Nodo<T> aux = pFirst;
        for (int i = 0; i < x; i++) {
            aux = aux.pNext;
        }
        return aux.data;
    }

    public int len() {
        return in;
    }

    public boolean estaVacia() {
        return in == 0;
    }

    public boolean contiene(T dato) {
        Nodo<T> aux = pFirst;
        while (aux != null) {
            if (aux.data == dato || (dato != null && dato.equals(aux.data))) {
                return true;
            }
            aux = aux.pNext;
        }
        return false;
    }

    //quita la primera aparicion del dato, devuelve false si no estaba
    public boolean eliminar(T dato) {
        Nodo<T> anterior = null;
        Nodo<T> aux = pFirst;
        while (aux != null) {
            if (aux.data == dato || (dato != null && dato.equals(aux.data))) {
                if (anterior == null) {
                    pFirst = aux.pNext;
                } else {
                    anterior.pNext = aux.pNext;
                }
                if (aux == pLast) {
                    pLast = anterior;
                }
                in--;
                return true;
            }
            anterior = aux;
            aux = aux.pNext;
        }
        return false;
    }

    public void vaciar() {
        pFirst = pLast = null;
        in = 0;
    }
}
