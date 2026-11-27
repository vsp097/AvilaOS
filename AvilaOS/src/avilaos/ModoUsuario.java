/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
public enum ModoUsuario {
    ADMINISTRADOR("Administrador"),
    USUARIO("Usuario");

    private final String etiqueta;

    ModoUsuario(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    @Override
    public String toString() {
        return etiqueta;
    }
}
