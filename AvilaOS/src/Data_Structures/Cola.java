/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author diego
 */
public class Cola<T> {
    protected Nodo<T> pFirst;
    protected Nodo<T> pFinal;
    protected int in;
    
    public Cola(){
        in = -1;
    }
    
    public void Encolar(T dato){
        in ++;
        Nodo<T> aux = new Nodo(dato);
        if (pFinal == null){ 
        pFinal = pFirst = aux;
        }
        else{
            pFinal.pNext = aux;
            pFinal = aux;
        }
    }
    
    public T Desencolar(){
        
        if(pFirst == null){
            return null;
        }
        else{
            in--;
            Nodo<T> aux = pFirst;
            if(pFinal == pFirst){
                aux = aux.pNext;
            }
            aux = aux.pNext;
            return aux.data;
        }
    }
}
