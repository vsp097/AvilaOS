/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Computador;
import Data_Structures.Listas;

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

            if (Clocklist != null) {
                for (int i = 0; i < Clocklist.len(); i++) {
                    Computador c = (Computador) Clocklist.retornar(i);
                    c.onTick(cycle);
                }
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
