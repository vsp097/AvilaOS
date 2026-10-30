/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;
import Data_Structures.EstadoProceso;
import Data_Structures.PCB;
import java.util.concurrent.Semaphore;
import Data_Structures.Computador;
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
    private Computador computador;

    
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
                despertar(mutex.semaSignal());
                return false;
            case 4:
                despertar(lleno.semaSignal());
                return false;        }
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
                despertar(mutex.semaSignal());
                return false;
            case 4:
                despertar(vacio.semaSignal());
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
    //pone el buffer en un computador y le quita ram
    public boolean setComputador(Computador c) {
        if (c.getRam() < capacidad) {
            return false;
        }
        computador = c;
        c.useRam(capacidad);
        return true;
    }

    public Computador getComputador() {
        return computador;
    }

    //true si el proceso esta en otro computador
    public boolean estaLejos(Computador c) {
        return computador != null && computador != c;
    }
    //si un productor o consumidor termina a mitad de los 5 pasos (por deadline)
    //hay que devolver los semaforos que tenia, si no los demas se quedan bloqueados para siempre
    //el paso guardado es el siguiente que le tocaba hacer
    public void soltarSemaforos(PCB proceso) {
        int paso = proceso.getPaso();
        if (paso == 0) {
            return;
        }
        //en el paso 2 o 3 todavia tenia el mutex
        if (paso == 2 || paso == 3) {
            despertar(mutex.semaSignal());
        }
        boolean esProductor = proceso.gettype().equals("Productor");
        //si ya metio o saco el elemento (paso 3 o 4) se hace el signal que le faltaba
        //si no, se devuelve el wait que hizo en el paso 0
        if (esProductor && paso >= 3) {
            despertar(lleno.semaSignal());
        } else if (esProductor) {
            despertar(vacio.semaSignal());
        } else if (paso >= 3) {
            despertar(vacio.semaSignal());
        } else {
            despertar(lleno.semaSignal());
        }
    }

    //si el semaforo desperto a alguien lo marca como listo para que su computador lo pase a la cola de listos
    private void despertar(PCB proceso) {
        if (proceso != null) {
            proceso.setstate(EstadoProceso.Listo);
        }
    }
}