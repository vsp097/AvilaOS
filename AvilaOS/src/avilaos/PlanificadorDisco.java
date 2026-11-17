/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;

/**
 *
 * @author andrea
 */
//toda politica de disco implementa esto, asi el disco no sabe cual esta usando
public interface PlanificadorDisco {

    PoliticaDisco getPolitica();

    //la cola viene en orden de llegada y nunca vacia, la politica no la modifica
    //ultimoBloque es hasta donde llega el cabezal cuando barre hacia arriba
    DecisionDisco decidir(Listas<SolicitudES> cola, int cabezal, DireccionCabezal direccion, int ultimoBloque);
}
