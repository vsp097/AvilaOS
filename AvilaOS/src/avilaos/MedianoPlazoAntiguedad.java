/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Cola;
import Data_Structures.PCB;

/**
 *
 * @author andrea
 */
//suspende al bloqueado que lleva mas tiempo bloqueado; si empatan, al de menor prioridad (numero mas alto)
//por que: el que lleva mas tiempo bloqueado es el que mas tiempo lleva ocupando RAM sin usarla,
//y la prioridad solo desempata para no sacar a uno importante cuando da igual cual sacar
//para reactivar va el que llego primero a listo/suspendido, asi ninguno se queda afuera para siempre
public class MedianoPlazoAntiguedad implements PlanificadorMedianoPlazo {

    @Override
    public PCB elegirParaSuspender(Cola<PCB> bloqueados) {
        PCB elegido = null;
        for (int i = 0; i < bloqueados.len(); i++) {
            PCB p = bloqueados.ver(i);
            if (elegido == null || p.getCicloBloqueo() < elegido.getCicloBloqueo()
                    || (p.getCicloBloqueo() == elegido.getCicloBloqueo() && p.getPrioridad() > elegido.getPrioridad())) {
                elegido = p;
            }
        }
        return elegido;
    }

    @Override
    public PCB elegirParaReactivar(Cola<PCB> listosSuspendidos) {
        return listosSuspendidos.ver(0);
    }
}
