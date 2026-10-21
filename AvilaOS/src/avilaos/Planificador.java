/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author vyckh
 */
public interface Planificador {
    PCB elegirSiguiente(Cola<PCB> listos);

    boolean debeSacar(PCB actual, Cola<PCB> listos, int ciclosUsados);
}
