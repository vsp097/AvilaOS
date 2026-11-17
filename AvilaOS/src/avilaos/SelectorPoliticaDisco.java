/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
public class SelectorPoliticaDisco {

    //para agregar una politica nueva solo se toca el enum y este metodo, el disco queda igual
    public static PlanificadorDisco crear(PoliticaDisco politica) {
        switch (politica) {
            case FIFO:
                return new DiscoFIFO();
            case SSTF:
                return new DiscoSSTF();
            case SCAN:
                return new DiscoSCAN();
            case C_SCAN:
                return new DiscoCSCAN();
            default:
                throw new IllegalArgumentException("Politica de disco todavia no hecha: " + politica);
        }
    }
}
