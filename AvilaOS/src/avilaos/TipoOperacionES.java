/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package avilaos;

/**
 *
 * @author vyckh
 */
//lo que le puede pedir un proceso al disco
public enum TipoOperacionES {
    CREAR,
    LEER,
    ACTUALIZAR,
    ELIMINAR,
    SUSPENDER,
    REACTIVAR;

    //en el json las operaciones vienen en ingles
    public static TipoOperacionES desdeTexto(String texto) {
        if (texto == null) {
            return null;
        }
        String t = texto.trim().toUpperCase();
        switch (t) {
            case "CREATE":
            case "CREAR":
                return CREAR;
            case "READ":
            case "LEER":
                return LEER;
            case "UPDATE":
            case "ACTUALIZAR":
                return ACTUALIZAR;
            case "DELETE":
            case "ELIMINAR":
                return ELIMINAR;
            case "":
            case "NONE":
            case "NINGUNA":
                return null;
            default:
                throw new IllegalArgumentException("La operacion '" + texto + "' no es valida");
        }
    }

    public String enIngles() {
        switch (this) {
            case CREAR:
                return "CREATE";
            case LEER:
                return "READ";
            case ACTUALIZAR:
                return "UPDATE";
            case ELIMINAR:
                return "DELETE";
            default:
                return name();
        }
    }
}
