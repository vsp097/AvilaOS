/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;
// para registrar las decisiones importantes del sistema
/**
 *
 * @author vyckh
 */
public class LogEventos {
    public void registrar (int ciclo, String mensaje){
        LogEventos log = new LogEventos();
        System.out.println("[Ciclo " + ciclo + "] " + mensaje);
    }
}
