/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Computador;
import Data_Structures.Listas;
import Data_Structures.Nodo;
import java.util.concurrent.Semaphore;

/**
 *
 * @author diego
 */
public class GlobalClock extends Thread{
    private volatile long cycleDuration = 1000;
    private volatile long cycle = 0;
    private volatile boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    
    private Listas Clocklist;
    //semaforo de java para que la ventana no agregue un computador mientras el reloj recorre la lista
    private final Semaphore llaveLista = new Semaphore(1);
    private volatile ClockListener listener;
    //vycky aqui crea el semaforo
    
   public void setClockListener(ClockListener l) {
        this.listener = l;
    }

    public void setClocklist(Listas l) {   // si no tenías forma de asignarla
        this.Clocklist = l;
    }

    @Override
    public void run() {
        while (running) {
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
            if (!running) break;

            cycle++;

            llaveLista.acquireUninterruptibly();
            try {
                if (Clocklist != null) {
                    for (int i = 0; i < Clocklist.len(); i++) {
                        Computador c = (Computador) Clocklist.retornar(i);
                        c.onTick(cycle);
                    }
                }
            } finally {
                llaveLista.release();
            }

            // Avisar a la interfaz
            ClockListener l = listener;
            if (l != null) {
                l.onTick(cycle);
            }

            try {
                Thread.sleep(cycleDuration);
            } catch (InterruptedException e) {
                return;
            }
        }
    }
    
    //agrega un computador mas al reloj, por ejemplo cuando la configuracion pide 3 o mas
    public void agregarComputador(Computador c) {
        llaveLista.acquireUninterruptibly();
        try {
            Clocklist.insertar(new Nodo<Computador>(c));
        } finally {
            llaveLista.release();
        }
    }

    public void pausar() {
        paused = true;
    }

    public void reanudar() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();   
        }
    }

    public void detener() {
        running = false;
        reanudar();               
    }

    //ciclo actual, para el log de la ventana
    public long getCiclo() {
        return cycle;
    }

    public boolean isPaused() {
        return paused;
    }

    public void setcduration(long ms) {
        cycleDuration = ms;
    }
    
}
