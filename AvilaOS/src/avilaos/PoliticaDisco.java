/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
public enum PoliticaDisco {
    FIFO("FIFO"),
    SSTF("SSTF"),
    SCAN("SCAN"),
    C_SCAN("C-SCAN");

    private final String etiqueta;

    PoliticaDisco(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    //acepta C-SCAN, CSCAN y C_SCAN porque en los archivos lo escriben de varias formas
    public static PoliticaDisco desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Falta la politica de disco");
        }
        String t = texto.trim().toUpperCase().replace("-", "").replace("_", "");
        switch (t) {
            case "FIFO":
            case "FCFS":
                return FIFO;
            case "SSTF":
                return SSTF;
            case "SCAN":
                return SCAN;
            case "CSCAN":
                return C_SCAN;
            default:
                throw new IllegalArgumentException("La politica de disco '" + texto + "' no es valida");
        }
    }
}
