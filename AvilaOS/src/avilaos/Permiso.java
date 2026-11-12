/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
public enum Permiso {
    PUBLICO("publico"),
    PRIVADO("privado");

    private final String etiqueta;

    Permiso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }

    public static Permiso desdeTexto(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Falta el permiso (public o private)");
        }
        String t = texto.trim().toLowerCase();
        if (t.equals("public") || t.equals("publico")) {
            return PUBLICO;
        }
        if (t.equals("private") || t.equals("privado")) {
            return PRIVADO;
        }
        throw new IllegalArgumentException("El permiso '" + texto + "' no es valido");
    }

    public String enIngles() {
        return this == PUBLICO ? "public" : "private";
    }
}
