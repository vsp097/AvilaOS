/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Enum.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author vyckh
 */
//los siete estados del modelo con suspension
public enum EstadoProceso {
    Nuevo("Nuevo"),
    Listo("Listo"),
    Ejecucion("Ejecucion"),
    Bloqueado("Bloqueado"),
    ListoSuspendido("Listo/Suspendido"),
    BloqueadoSuspendido("Bloqueado/Suspendido"),
    Terminado("Terminado");

    private final String etiqueta;

    EstadoProceso(String etiqueta) {
        this.etiqueta = etiqueta;
    }

    public String getEtiqueta() {
        return etiqueta;
    }
}
