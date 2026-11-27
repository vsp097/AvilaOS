/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;

/**
 *
 * @author diego
 */
public class DiscoCLOOK implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.C_LOOK;
    }

    //como C-SCAN, pero sin llegar al ultimo bloque: despues de la ultima solicitud salta a la mas baja
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        SolicitudES adelante = null;
        int masBaja = -1;
        for (int i = 0; i < cola.len(); i++) {
            SolicitudES s = cola.retornar(i);
            int b = s.getBloque();
            if (masBaja < 0 || b < masBaja) {
                masBaja = b;
            }
            if (b >= cabezal && (adelante == null || b < adelante.getBloque())) {
                adelante = s;
            }
        }
        if (adelante != null) {
            return DecisionDisco.atender(adelante);
        }
        //no queda nada hacia arriba: salta desde donde esta hasta la solicitud mas baja, y el salto no cuenta
        return DecisionDisco.irAlExtremoYSaltar(cabezal, masBaja);
    }
}
