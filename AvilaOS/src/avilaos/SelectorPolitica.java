/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
public class SelectorPolitica {
    //recibe la politivs que el usuario escoge en el menu y devuelve esa política lista para usar
        public static Planificador crear(PoliticaPlanificacion politica, int quantum) {
        switch (politica) {
            case FCFS:
                return new FCFS();
            case ROUND_ROBIN:
                return new Roundrobin(quantum);
            default:
                throw new IllegalArgumentException("Politica todavia no hecha: " + politica);
        }
    }
}
