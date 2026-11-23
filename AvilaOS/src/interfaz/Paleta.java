/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package interfaz;

import java.awt.Color;

/**
 *
 * @author andrea
 */
//los colores con los que se pintan los archivos en el disco y en la tabla
public class Paleta {

    public static final Color LIBRE = new Color(236, 238, 241);
    public static final Color SWAP_LIBRE = new Color(214, 226, 240);
    public static final Color SWAP_OCUPADO = new Color(88, 118, 160);
    public static final Color CABEZAL = new Color(214, 40, 40);

    private static final Color[] COLORES = {
        new Color(244, 162, 97), new Color(42, 157, 143), new Color(233, 196, 106), new Color(131, 197, 190),
        new Color(231, 111, 81), new Color(144, 190, 109), new Color(180, 142, 214), new Color(249, 132, 174),
        new Color(87, 167, 221), new Color(214, 186, 150), new Color(119, 191, 163), new Color(240, 128, 128),
        new Color(160, 196, 255), new Color(205, 180, 219), new Color(202, 210, 100), new Color(255, 183, 3)
    };

    //cuando hay mas archivos que colores se repiten desde el principio
    public static Color de(int numero) {
        if (numero < 0) {
            return LIBRE;
        }
        return COLORES[numero % COLORES.length];
    }
}
