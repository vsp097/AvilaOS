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
public class DiscoLOOK implements PlanificadorDisco {

    @Override
    public PoliticaDisco getPolitica() {
        return PoliticaDisco.LOOK;
    }

    //como SCAN, pero no llega hasta el extremo: cuando no queda nada en su direccion se devuelve ahi mismo
    @Override
    public DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque) {
        boolean subiendo = direccion == DireccionCabezal.ARRIBA;
        SolicitudES adelante = null;
        SolicitudES atras = null;
        for (int i = 0; i < cola.len(); i++) {
            SolicitudES s = cola.retornar(i);
            int b = s.getBloque();
            boolean vaAdelante = subiendo ? b >= cabezal : b <= cabezal;
            if (vaAdelante) {
                if (adelante == null || Math.abs(b - cabezal) < Math.abs(adelante.getBloque() - cabezal)) {
                    adelante = s;
                }
            } else {
                //del otro lado tambien se guarda la mas cercana, por si hay que devolverse
                if (atras == null || Math.abs(b - cabezal) < Math.abs(atras.getBloque() - cabezal)) {
                    atras = s;
                }
            }
        }
        //el disco cambia la direccion solo cuando el cabezal se mueve hacia el otro lado
        return DecisionDisco.atender(adelante != null ? adelante : atras);
    }
}
