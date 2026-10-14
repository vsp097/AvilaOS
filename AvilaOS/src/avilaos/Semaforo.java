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

//pendiente poner comentarios
public class Semaforo {
    private int valor;
    private Cola<PCB> bloqueados;

    public Semaforo(int valorInicial) {
        this.valor = valorInicial;
        this.bloqueados = new Cola<>();
    }

    public boolean semaWait(PCB proceso) {
        valor = valor-1;
        if (valor<0) {
            bloqueados.Encolar(proceso);
            return true;
        }
        return false;
    }

    public boolean semaSignal() {
        valor = valor+1;
        if (valor<=0) {
            bloqueados.Desencolar();
            return true;
        }
        return false;
    }

}
