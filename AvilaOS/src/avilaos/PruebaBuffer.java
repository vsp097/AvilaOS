/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;

/**
 *
 * @author vyckh
 */
public class PruebaBuffer {
        public static void main(String[] args) {
        LogEventos log = new LogEventos();
        Buffer b1 = new Buffer("B1", 2);
        PCB productor = new PCB(1, "P1-H1", 0, "Listo", "Productor", 64);
        PCB consumidor = new PCB(2, "P2-H1", 0, "Listo", "Consumidor", 64);
        int ciclo = 0;

        for (int elemento = 1; elemento <= 3; elemento++) {
            for (int paso = 0; paso < 5; paso++) {
                ciclo++;
                boolean seBloqueo = b1.pasitoProductor(paso, productor, elemento);
                log.registrar(ciclo, "P1-H1 paso " + paso + estado(b1));
                if (seBloqueo) {
                    log.registrar(ciclo, "P1-H1 se bloquea en 'vacio': el buffer esta lleno");
                    break;
                }
            }
        }

        for (int paso = 0; paso < 5; paso++) {
            ciclo++;
            b1.pasitoConsumidor(paso, consumidor);
            log.registrar(ciclo, "P2-H1 paso " + paso + estado(b1));
        }
        log.registrar(ciclo, "P2-H1 libero un espacio y desperto a P1-H1");

        for (int paso = 1; paso < 5; paso++) {
            ciclo++;
            b1.pasitoProductor(paso, productor, 3);
            log.registrar(ciclo, "P1-H1 paso " + paso + estado(b1));
        }

        log.registrar(ciclo, "Producidos: " + b1.getProducidos() + " | Consumidos: " + b1.getConsumidos());
    }

    private static String estado(Buffer b) {
        return " | cantidad=" + b.getCantidad()
                + " vacio=" + b.getVacio().getValor()
                + " lleno=" + b.getLleno().getValor()
                + " mutex=" + b.getMutex().getValor();
    }
}
