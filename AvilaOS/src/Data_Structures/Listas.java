/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author diego
 */
public class Listas<T> {
    protected Nodo<T> pFirst;
    protected Nodo<T> pLast;
    protected int in;
    
    public Listas(Nodo aux){
        pFirst = pLast = aux;
        in=0;
    }
    
    public void insertar(Nodo aux){
        pLast.pNext = aux;
        pLast = aux;
        in++;
    }
    
    public T retornar(int x){
        if (x == 0){
            return (T) pFirst.data;
        }
        Nodo aux = pFirst;
        for(int i = 0; i< in; i++){
            aux = aux.pNext;
        }
        return (T) aux.data;
    }
    
    public int len(){
        return in;
    }
}
