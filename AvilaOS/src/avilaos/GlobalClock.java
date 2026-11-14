/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;
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
    
    //los componentes que trabajan con el reloj: el computador y el disco
    private Listas<ClockListener> Clocklist = new Listas<>();
    //semaforo de java para que nadie agregue un componente mientras el reloj recorre la lista
    private final Semaphore llaveLista = new Semaphore(1);
    private volatile ClockListener listener;
   public void setClockListener(ClockListener l) {
        this.listener = l;
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
                //cada componente avanza exactamente un ciclo de trabajo
                for (int i = 0; i < Clocklist.len(); i++) {
                    Clocklist.retornar(i).onTick(cycle);
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
    
    //agrega un componente al reloj, se llaman en el mismo orden en que se agregaron
    public void agregar(ClockListener c) {
        llaveLista.acquireUninterruptibly();
        try {
            Clocklist.agregar(c);
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

    public long getcduration() {
        return cycleDuration;
    }

    public void setcduration(long ms) {
        cycleDuration = ms;
    }
    
}
