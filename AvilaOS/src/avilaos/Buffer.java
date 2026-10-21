/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.PCB;
import java.util.concurrent.Semaphore;

/**
 *
 * @author vyckh
 */

// Buffer donde los productores meten elementos y los consumidores los sacan 
public class Buffer {
    private String nombre;
    private int capacidad;
    private int[] elementos;//donde se guardan los elementos
    private int entrada;
    private int salida;
    private int cantidad;
    private int producidos;
    private int consumidos;

    private Semaforo vacio;
    private Semaforo lleno;
    private Semaforo mutex;
    private Semaphore llave = new Semaphore(1);

    
    public Buffer(String nombre, int capacidad) {
        if (capacidad < 1) {
          throw new IllegalArgumentException("La capacidad del buffer debe ser mayor que 0");
        }
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.elementos = new int[capacidad];
        this.entrada = 0;
        this.salida = 0;
        this.cantidad = 0;
        this.producidos = 0;
        this.consumidos = 0;
        this.vacio = new Semaforo("vacio", capacidad);
        this.lleno = new Semaforo("lleno", 0);
        this.mutex = new Semaforo("mutex", 1);
    }
    //para metewr elementos enel buffer
    public void insertar(int elemento) {
        llave.acquireUninterruptibly();
        try {
            elementos[entrada] = elemento;
            entrada = (entrada + 1) % capacidad;
            cantidad = cantidad + 1;
            producidos = producidos + 1;
        } finally {
            llave.release();
        }
    }
    
    //para sacar elementos del buffer
    public int extraer() {
        llave.acquireUninterruptibly();
        try {
            int elemento = elementos[salida];
            salida = (salida + 1) % capacidad;
            cantidad = cantidad - 1;
            consumidos = consumidos + 1;
            return elemento;
        } finally {
            llave.release();
        }
    }
    
    //para hacer pasos del productor por ciclo
    public boolean pasitoProductor(int paso, PCB proceso, int elemento) {
        switch (paso) {
            case 0:
                return vacio.semaWait(proceso);
            case 1:
                return mutex.semaWait(proceso);
            case 2:
                insertar(elemento);
                return false;
            case 3:
                mutex.semaSignal();
                return false;
            case 4:
                lleno.semaSignal();
                return false;
        }
        return false;
    }
    
    //para hacer pasos del consumidor por ciclo
    public boolean pasitoConsumidor(int paso, PCB proceso) {
        switch (paso) {
            case 0:
                return lleno.semaWait(proceso);
            case 1:
                return mutex.semaWait(proceso);
            case 2:
                extraer();
                return false;
            case 3:
                mutex.semaSignal();
                return false;
            case 4:
                vacio.semaSignal();
                return false;
        }
        return false;
    }

    
    //getter para tener los datos ymostrarlos en la ventana
    public String getNombre() {
        return nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public int getCantidad() {
        llave.acquireUninterruptibly();
        try {
            return cantidad;
        } finally {
            llave.release();
        }
    }

    public int getProducidos() {
        llave.acquireUninterruptibly();
        try {
            return producidos;
        } finally {
            llave.release();
        }
    }

    public int getConsumidos() {
        llave.acquireUninterruptibly();
        try {
            return consumidos;
        } finally {
            llave.release();
        }
    }

    public Semaforo getVacio() {
        return vacio;
    }

    public Semaforo getLleno() {
        return lleno;
    }

    public Semaforo getMutex() {
        return mutex;
    }
    
        public int getElemento(int posicion) {
        llave.acquireUninterruptibly();
        try {
            return elementos[posicion];
        } finally {
            llave.release();
        }
    }

    public boolean estaOcupada(int posicion) {
        llave.acquireUninterruptibly();
        try {
            return (posicion - salida + capacidad) % capacidad < cantidad;
        } finally {
            llave.release();
        }
    }
}