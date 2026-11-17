/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;

/**
 *
 * @author andrea
 */
public class DiscoFIFO implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.FIFO;
    }

    //atiende en el mismo orden en que llegaron
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        return DecisionDisco.atender(cola.retornar(0));
    }
}
