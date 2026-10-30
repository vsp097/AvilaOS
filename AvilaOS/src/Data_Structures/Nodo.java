/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author diego
 */
public class Nodo<T> {
    protected Nodo<T> pNext;
    protected T data;
    public Nodo(T dato){
        data = dato;
    }
    
    T Dato(){
        return data;
    }
}
