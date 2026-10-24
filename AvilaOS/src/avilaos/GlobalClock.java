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
    private long cycleDuration = 0; 
    private long cycle = 0;
    private boolean running = true;
    private volatile boolean paused = false;
    private final Object pauseLock = new Object();
    
    private Listas Clocklist;
    //vycky aqui crea el semaforo
    
    @Override
    public void run(){
        while(running){
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
            for (int i = 0; i < Clocklist.len(); i++) {
                Computador c = (Computador) Clocklist.retornar(i);
                c.onTick(cycle);
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

    public boolean isPaused() {
        return paused;
    }

    public void setcduration(long ms) {
        cycleDuration = ms;
    }
    
}
