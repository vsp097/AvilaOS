/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
//como se mueve el cabezal mientras lee o escribe los bloques de una solicitud
//FIJO: el cabezal va al primer bloque y ahi transfiere todos los bloques (1 ciclo por bloque), como en los casos P1 a P4 del enunciado
//CADENA: el cabezal sigue la cadena del archivo y pasa por cada uno de sus bloques
public enum ModoTransferencia {
    FIJO("FIXED", "en el primer bloque"),
    CADENA("CHAIN", "siguiendo la cadena");

    private final String enJson;
    private final String etiqueta;

    ModoTransferencia(String enJson, String etiqueta) {
        this.enJson = enJson;
        this.etiqueta = etiqueta;
    }

    public String enJson() {
        return enJson;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    //si no viene en el json se usa FIJO, que es lo que dan los casos de prueba del enunciado
    public static ModoTransferencia desdeTexto(String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return FIJO;
        }
        String t = texto.trim().toUpperCase();
        for (ModoTransferencia m : values()) {
            if (m.enJson.equals(t) || m.name().equals(t)) {
                return m;
            }
        }
        throw new IllegalArgumentException("transferMode debe ser FIXED o CHAIN, no '" + texto.trim() + "'");
    }
}
