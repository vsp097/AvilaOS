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
public class DiscoSSTF implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.SSTF;
    }

    //atiende la que esta mas cerca del cabezal, si empatan gana la que llego primero
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        SolicitudES mejor = null;
        int menor = 0;
        for (int i = 0; i < cola.len(); i++) {
            SolicitudES s = cola.retornar(i);
            int distancia = Math.abs(s.getBloque() - cabezal);
            if (mejor == null || distancia < menor) {
                mejor = s;
                menor = distancia;
            }
        }
        return DecisionDisco.atender(mejor);
    }
}
