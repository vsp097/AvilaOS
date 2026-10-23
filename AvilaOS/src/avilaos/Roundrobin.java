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
public class Roundrobin implements Planificador {
    
    private int quantum;

    public Roundrobin(int quantum) {
        setQuantum(quantum);
    }

    public void setQuantum(int quantum) {
        if (quantum < 1) {
            throw new IllegalArgumentException("El quantum debe ser mayor que 0");
        }
        this.quantum = quantum;
    }

    public int getQuantum() {
        return quantum;
    }

    public PCB elegirSiguiente(Cola<PCB> listos) {
        return listos.Desencolar();
    }

    public boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados) {
        return ciclosUsados >= quantum;
    }
}
