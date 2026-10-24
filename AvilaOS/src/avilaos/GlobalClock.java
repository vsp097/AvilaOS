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
    private boolean paused = false;
    
    private Listas Clocklist;
    //vycky aqui crea el semaforo
    
    @Override
    public void run(){
        while(running){
            if (!paused){
                cycle++;
                for(int i = 0; i < Clocklist.len(); i++){
                    Computador l = (Computador) Clocklist.retornar(i);
                    l.onTick(cycle);
                }
                for(int i = 0; i < Clocklist.len(); i++){
                    
                }
                try{Thread.sleep(cycleDuration);}
            catch (InterruptedException e) { return;}
            }
        }
    }
    
    public void setcduration(long ms){
        cycleDuration = ms;
    }
    
}
