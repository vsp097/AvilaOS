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
public class DiscoSCAN implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.SCAN;
    }

    //el ascensor: atiende lo que consigue en su direccion, llega al extremo y se devuelve
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        boolean subiendo = direccion == DireccionCabezal.ARRIBA;
        SolicitudES mejor = null;
        for (int i = 0; i < cola.len(); i++) {
            SolicitudES s = cola.retornar(i);
            int b = s.getBloque();
            //solo cuentan las que estan del lado hacia donde va el cabezal
            if (subiendo && b < cabezal) {
                continue;
            }
            if (!subiendo && b > cabezal) {
                continue;
            }
            if (mejor == null || Math.abs(b - cabezal) < Math.abs(mejor.getBloque() - cabezal)) {
                mejor = s;
            }
        }
        if (mejor != null) {
            return DecisionDisco.atender(mejor);
        }
        //no queda nada en esta direccion: sigue hasta el extremo y ahi cambia de direccion
        return DecisionDisco.irAlExtremo(subiendo ? ultimoBloque : 0);
    }
}
