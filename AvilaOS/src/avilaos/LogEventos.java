/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;


import java.util.concurrent.Semaphore;

/**
 *
 * @author vyckh
 */
public class LogEventos {
    
    private String texto = "";
    private String ultimo = "";
    private Semaphore llave = new Semaphore(1);
    
    // para registrar las decisiones importantes del sistema
    public void registrar(int ciclo, String mensaje) {
       String linea = String.format("[%04d] ", ciclo) + mensaje;
       llave.acquireUninterruptibly();
        try {
            texto = texto + linea + "\n";
            ultimo = linea;
        } finally {
            llave.release();
        }
        System.out.println(linea);
    }
    
    public String getTexto() {
        llave.acquireUninterruptibly();
        try {
            return texto;
        } finally {
            llave.release();
            }
    }

    public String getUltimo() {
        llave.acquireUninterruptibly();
        try {
            return ultimo;
        } finally {
            llave.release();
        }
    }
    
    
}
