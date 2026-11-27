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
public class DiscoCSCAN implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.C_SCAN;
    }

    //solo atiende subiendo: al llegar al ultimo bloque salta al 0 y vuelve a subir
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        SolicitudES mejor = null;
        for (int i = 0; i < cola.len(); i++) {
            SolicitudES s = cola.retornar(i);
            int b = s.getBloque();
            if (b < cabezal) {
                continue;
            }
            if (mejor == null || b < mejor.getBloque()) {
                mejor = s;
            }
        }
        if (mejor != null) {
            return DecisionDisco.atender(mejor);
        }
        return DecisionDisco.irAlExtremoYSaltar(ultimoBloque, 0);
    }
}
