/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
public enum TipoNodo {
    DIRECTORIO,
    ARCHIVO;

    public static TipoNodo desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Falta el tipo (DIRECTORY o FILE)");
        }
        String t = texto.trim().toUpperCase();
        if (t.equals("DIRECTORY") || t.equals("DIRECTORIO")) {
            return DIRECTORIO;
        }
        if (t.equals("FILE") || t.equals("ARCHIVO")) {
            return ARCHIVO;
        }
        throw new IllegalArgumentException("El tipo '" + texto + "' no es valido");
    }

    public String enIngles() {
        return this == DIRECTORIO ? "DIRECTORY" : "FILE";
    }
}
