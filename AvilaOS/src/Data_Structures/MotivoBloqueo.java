/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author vyckh
 */
//por que esta bloqueado un proceso
public enum MotivoBloqueo {
    NINGUNO(""),
    ES_DISCO("E/S de disco"),
    //el mediano plazo lo eligio y el disco lo esta copiando al area de intercambio
    SUSPENSION("pasando al area de intercambio");

    private final String etiqueta;

    MotivoBloqueo(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
