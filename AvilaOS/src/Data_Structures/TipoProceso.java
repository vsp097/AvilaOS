/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author vyckh
 */
public enum TipoProceso {
    CPU_BOUND("CPU Bound"),
    ES("E/S de archivos");

    private final String etiqueta;

    TipoProceso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
