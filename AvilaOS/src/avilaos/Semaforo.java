/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;
import java.util.concurrent.Semaphore;

/**
 *
 * @author vyckh
 */


public class Semaforo {
    //contador que si es negativo indica cuantos procesos estan esperando
    private int valor;
    //los bloqueados en cola
    private Cola<PCB> bloqueados;
    //para el nombre del semaforo, y luego mostrarlo en el log
    private String nombre;

    //se crea un semaforo nuevo para llevar la cuenta de los bloqueados
    public Semaforo(String nombre, int valorInicial) {
        this.nombre = nombre;
        this.valor = valorInicial;
        this.bloqueados = new Cola<>();
    }

    //cuando un proceso pide pasar se resta 1; si queda negativo, se bloquea y va a la cola
    public boolean semaWait(PCB proceso) {
        llave.acquireUninterruptibly();
        try {
        valor = valor-1;
        if (valor<0) {
            bloqueados.Encolar(proceso);
            return true;
        }
        return false;
        } finally {
            llave.release();
        }
    }

    //avisa que el proceso termino, sumandole 1; si queda en 0 o menos, pasa al primero de la cola
    public PCB semaSignal() {
        llave.acquireUninterruptibly();
        try {
            valor = valor + 1;
            if (valor <= 0) {
                return bloqueados.Desencolar();
            }
            return null;
        } finally {
            llave.release();
        }
    }
    

    //para devolver el valor
    public int getValor() {
        llave.acquireUninterruptibly();
        try {
            return valor;
        } finally {
            llave.release();
        }
    }
    
    //nombres de los procesos que estan esperando en este semaforo
    public String esperar() {
        llave.acquireUninterruptibly();
        try {
            String texto = "";
            Cola<PCB> aux = new Cola<>();
            PCB p = bloqueados.Desencolar();
            while (p != null) {
                if (!texto.equals("")) {
                    texto = texto + ", ";
                }
                texto = texto + p.getname();
                aux.Encolar(p);
                p = bloqueados.Desencolar();
            }
            //los devuelvo a la cola en el mismo orden
            p = aux.Desencolar();
            while (p != null) {
                bloqueados.Encolar(p);
                p = aux.Desencolar();
            }
            if (texto.equals("")) {
                return "nadie";
            }
            return texto;
        } finally {
            llave.release();
        }
    }
    //para devolver el nombre
    public String getNombre() {
        return nombre;
    }
    
    private Semaphore llave = new Semaphore(1);
    
}