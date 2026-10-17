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

//Buffer donde los productores meten elementos y los consumidores los sacan
public class Buffer {
    private String nombre;
    private int capacidad;
    private int[] elementos;
    private int entrada;
    private int salida;
    private int cantidad;

    private Semaforo vacio;
    private Semaforo lleno;
    private Semaforo mutex;
    private Semaphore llave = new Semaphore(1);

    public Buffer(String nombre, int capacidad) {
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.elementos = new int[capacidad];
        this.entrada = 0;
        this.salida = 0;
        this.cantidad = 0;
        this.vacio = new Semaforo("vacio", capacidad);
        this.lleno = new Semaforo("lleno", 0);
        this.mutex = new Semaforo("mutex", 1);
    }
    
    public void insertar(int elemento) {
        llave.acquireUninterruptibly();
        try {
            elementos[entrada] = elemento;
            entrada = (entrada + 1) % capacidad;
            cantidad = cantidad + 1;
        } finally {
            llave.release();
        }
    }

    public int extraer() {
        llave.acquireUninterruptibly();
        try {
            int elemento = elementos[salida];
            salida = (salida + 1) % capacidad;
            cantidad = cantidad - 1;
            return elemento;
        } finally {
            llave.release();
        }
    }
}
