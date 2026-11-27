/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author vyckh
 */
public class Cola<T> {

    protected Nodo<T> pFirst;
    protected Nodo<T> pFinal;
    //cantidad de elementos
    protected int in;

    public Cola() {
        in = 0;
    }

    public void Encolar(T dato) {
        in++;
        Nodo<T> aux = new Nodo<>(dato);
        if (pFinal == null) {
            pFinal = pFirst = aux;
        } else {
            pFinal.pNext = aux;
            pFinal = aux;
        }
    }

    public T Desencolar() {
        if (pFirst == null) {
            return null;
        } else {
            in--;
            Nodo<T> aux = pFirst;
            pFirst = aux.pNext;
            if (pFirst == null) {
                pFinal = null;
            }
            return aux.data;
        }
    }

    public int len() {
        return in;
    }

    public boolean estaVacia() {
        return in == 0;
    }

    //para mirar un elemento sin sacarlo, el 0 es el primero de la cola
    public T ver(int x) {
        if (x < 0 || x >= in) {
            return null;
        }
        Nodo<T> aux = pFirst;
        for (int i = 0; i < x; i++) {
            aux = aux.pNext;
        }
        return aux.data;
    }

    //saca un elemento de cualquier parte de la cola sin cambiar el orden de los demas
    public boolean eliminar(T dato) {
        Nodo<T> anterior = null;
        Nodo<T> aux = pFirst;
        while (aux != null) {
            if (aux.data == dato) {
                if (anterior == null) {
                    pFirst = aux.pNext;
                } else {
                    anterior.pNext = aux.pNext;
                }
                if (aux == pFinal) {
                    pFinal = anterior;
                }
                in--;
                return true;
            }
            anterior = aux;
            aux = aux.pNext;
        }
        return false;
    }
}
