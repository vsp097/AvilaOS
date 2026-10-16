/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;
import Data_Structures.Cola;
import Data_Structures.PCB;
/**
 *
 * @author vyckh
 */


public class Semaforo {
    //contador que si es negativo indica cuantos procesos estan esperando
    private int valor;
    //los bloqueados en cola 
    private Cola<PCB> bloqueados;

    //se crea un semaforo nuevo para llevar la cuenta de los bloqueados
    public Semaforo(int valorInicial) {
        this.valor = valorInicial;
        this.bloqueados = new Cola<>();
    }
    //cuando un proceso pide pasar,y que esta en el proceso, se resta 1 para aclarar que hay un valor menos en el buffer
    public boolean semaWait(PCB proceso) {
        valor = valor-1;
        if (valor<0) {
            bloqueados.Encolar(proceso);
            return true;
        }
        return false;
    }
    //avisa que el proceso termino, sumandole 1,si queda en 0 o menos, pasa asi al primero de la cola
    public boolean semaSignal() {
        valor = valor+1;
        if (valor<=0) {
            bloqueados.Desencolar();
            return true;
        }
        return false;
    }

}
