/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import Data_Structures.Listas;
import java.util.concurrent.Semaphore;

/**
 *
 * @author andrea
 */
//el disco local del computador: es un componente mas que obedece al reloj
public class Disco implements ClockListener {

    public static final int MIN_BLOQUES = 100;
    public static final int MAX_BLOQUES = 500;

    //primero van los bloques de archivos y despues los del area de intercambio
    private Bloque[] bloques;
    private int bloquesArchivos;
    private int bloquesSwap;

    private int cabezal;
    private DireccionCabezal direccion = DireccionCabezal.ARRIBA;
    //posiciones que recorre el cabezal en un ciclo
    private int velocidad = 5;

    //la unica cola de E/S, en orden de llegada
    private Listas<SolicitudES> cola = new Listas<>();
    private SolicitudES actual;
    private PlanificadorDisco planificador = new DiscoFIFO();

    //cuando el cabezal va a un extremo sin atender a nadie (SCAN y C-SCAN)
    private boolean enViaje = false;
    private int destinoViaje;
    private int saltoViaje;

    //solicitudes de la carga inicial que todavia no han llegado, mientras falten el disco espera
    private int retenidas = 0;

    //semaforo de java para la cola, el mapa de bloques y el cabezal
    private final Semaphore llave = new Semaphore(1);

    private OyenteDisco oyente;
    private LogEventos log;
    private long ciclo = 0;

    //numeros para las metricas
    private long desplazamientoTotal = 0;
    private long sumaEspera = 0;
    private long atendidas = 0;
    private long[] atendidasPorPolitica = new long[PoliticaDisco.values().length];

    public Disco(int bloquesArchivos, int bloquesSwap, int cabezalInicial) {
        if (bloquesArchivos < MIN_BLOQUES || bloquesArchivos > MAX_BLOQUES) {
            throw new IllegalArgumentException("El disco debe tener entre " + MIN_BLOQUES + " y " + MAX_BLOQUES + " bloques");
        }
        if (bloquesSwap < 0 || bloquesSwap > MAX_BLOQUES) {
            throw new IllegalArgumentException("El area de intercambio debe tener entre 0 y " + MAX_BLOQUES + " bloques");
        }
        if (cabezalInicial < 0 || cabezalInicial >= bloquesArchivos) {
            throw new IllegalArgumentException("El cabezal debe empezar entre 0 y " + (bloquesArchivos - 1));
        }
        this.bloquesArchivos = bloquesArchivos;
        this.bloquesSwap = bloquesSwap;
        this.cabezal = cabezalInicial;
        bloques = new Bloque[bloquesArchivos + bloquesSwap];
        for (int i = 0; i < bloques.length; i++) {
            bloques[i] = new Bloque(i, i >= bloquesArchivos);
        }
    }

    public void setOyente(OyenteDisco o) {
        oyente = o;
    }

    public void setLog(LogEventos l) {
        log = l;
    }

    //----- lo que hace el disco en cada ciclo del reloj -----

    @Override
    public void onTick(long cycle) {
        SolicitudES terminada;
        llave.acquireUninterruptibly();
        try {
            ciclo = cycle;
            terminada = trabajar();
        } finally {
            llave.release();
        }
        //el aviso va despues de soltar el semaforo, porque el sistema operativo vuelve a usar el disco
        if (terminada != null && oyente != null) {
            oyente.solicitudAtendida(terminada);
        }
    }

    //un ciclo de trabajo: o mueve el cabezal, o lee/escribe un bloque
    //devuelve la solicitud si se termino de atender en este ciclo
    private SolicitudES trabajar() {
        if (retenidas > 0) {
            return null;
        }
        //si no esta haciendo nada le pregunta a la politica que sigue
        int vueltas = 0;
        while (actual == null && !enViaje && !cola.estaVacia() && vueltas < 3) {
            vueltas++;
            DecisionDisco d = planificador.decidir(cola, cabezal, direccion, limiteBarrido());
            if (d.getSolicitud() != null) {
                actual = d.getSolicitud();
                cola.eliminar(actual);
                actual.setCicloInicio(ciclo);
                sumaEspera += ciclo - actual.getCicloLlegada();
                registrar("El disco atiende la solicitud de " + actual.getProceso().getname() + " en el bloque "
                        + actual.getBloque() + " (" + planificador.getPolitica() + ")");
            } else {
                enViaje = true;
                destinoViaje = d.getExtremo();
                saltoViaje = d.getSaltoA();
                if (cabezal == destinoViaje) {
                    llegarAlExtremo();
                }
            }
        }
        if (enViaje) {
            mover(destinoViaje);
            if (cabezal == destinoViaje) {
                llegarAlExtremo();
            }
            return null;
        }
        if (actual == null) {
            return null;
        }
        int objetivo = actual.getBloqueActual();
        if (cabezal != objetivo) {
            mover(objetivo);
            return null;
        }
        //el cabezal ya esta sobre el bloque: leerlo o escribirlo cuesta este ciclo
        actual.sumarHecho();
        if (!actual.termino()) {
            return null;
        }
        SolicitudES lista = actual;
        actual = null;
        atendidas++;
        atendidasPorPolitica[planificador.getPolitica().ordinal()]++;
        return lista;
    }

    private void llegarAlExtremo() {
        enViaje = false;
        if (saltoViaje >= 0) {
            registrar("El cabezal llega al bloque " + destinoViaje + " y salta al " + saltoViaje
                    + " (" + planificador.getPolitica() + ")");
            //el salto de regreso no cuenta como desplazamiento
            cabezal = saltoViaje;
            direccion = DireccionCabezal.ARRIBA;
        } else {
            registrar("El cabezal llega al bloque " + destinoViaje + " y regresa (" + planificador.getPolitica() + ")");
            direccion = direccion == DireccionCabezal.ARRIBA ? DireccionCabezal.ABAJO : DireccionCabezal.ARRIBA;
        }
    }

    //acerca el cabezal al destino lo que le permite la velocidad
    private void mover(int destino) {
        int falta = Math.abs(destino - cabezal);
        if (falta == 0) {
            return;
        }
        int paso = Math.min(velocidad, falta);
        if (destino > cabezal) {
            cabezal += paso;
            direccion = DireccionCabezal.ARRIBA;
        } else {
            cabezal -= paso;
            direccion = DireccionCabezal.ABAJO;
        }
        desplazamientoTotal += paso;
    }

    //hasta donde sube el cabezal cuando barre: el ultimo bloque de archivos,
    //o el ultimo del area de intercambio si hay alguna solicitud esperando alla
    private int limiteBarrido() {
        int limite = bloquesArchivos - 1;
        if (cabezal > limite) {
            return bloques.length - 1;
        }
        for (int i = 0; i < cola.len(); i++) {
            if (cola.retornar(i).getBloque() > limite) {
                return bloques.length - 1;
            }
        }
        return limite;
    }

    //----- la cola de E/S -----

    public void encolar(SolicitudES s) {
        llave.acquireUninterruptibly();
        try {
            cola.agregar(s);
            if (retenidas > 0) {
                retenidas--;
                if (retenidas == 0) {
                    registrar("El disco empieza a atender: " + cola.len() + " solicitudes en la cola");
                }
            }
        } finally {
            llave.release();
        }
    }

    //el disco no atiende hasta que lleguen n solicitudes, para que la politica las vea todas juntas
    public void retener(int n) {
        llave.acquireUninterruptibly();
        try {
            retenidas = Math.max(0, n);
        } finally {
            llave.release();
        }
    }

    //una de las solicitudes que se esperaban ya no va a llegar
    public void descontarRetencion() {
        llave.acquireUninterruptibly();
        try {
            if (retenidas > 0) {
                retenidas--;
            }
        } finally {
            llave.release();
        }
    }

    public void liberarRetencion() {
        llave.acquireUninterruptibly();
        try {
            if (retenidas > 0) {
                retenidas = 0;
                registrar("El disco empieza a atender: " + cola.len() + " solicitudes en la cola");
            }
        } finally {
            llave.release();
        }
    }

    public void setPlanificador(PlanificadorDisco p) {
        llave.acquireUninterruptibly();
        try {
            planificador = p;
            registrar("El disco cambia a la politica " + p.getPolitica());
        } finally {
            llave.release();
        }
    }

    public PoliticaDisco getPolitica() {
        return planificador.getPolitica();
    }

    public void setVelocidad(int v) {
        if (v < 1 || v > MAX_BLOQUES) {
            throw new IllegalArgumentException("La velocidad del cabezal debe estar entre 1 y " + MAX_BLOQUES);
        }
        velocidad = v;
    }

    public int getVelocidad() {
        return velocidad;
    }

    //----- el mapa de bloques y la asignacion encadenada -----

    //busca bloques libres entre desde y hasta, y los encadena; devuelve null si no alcanzan
    private int[] encadenar(int cantidad, int desde, int hasta, int empezarEn, String ocupante, int color) {
        int[] cadena = new int[cantidad];
        int encontrados = 0;
        int largo = hasta - desde;
        for (int i = 0; i < largo && encontrados < cantidad; i++) {
            int b = desde + (empezarEn - desde + i) % largo;
            if (!bloques[b].estaOcupado()) {
                cadena[encontrados] = b;
                encontrados++;
            }
        }
        if (encontrados < cantidad) {
            return null;
        }
        marcar(cadena, ocupante, color);
        return cadena;
    }

    //cada bloque guarda cual es el siguiente y el ultimo queda en -1
    private void marcar(int[] cadena, String ocupante, int color) {
        for (int i = 0; i < cadena.length; i++) {
            bloques[cadena[i]].ocupar(ocupante, color);
            if (i > 0) {
                bloques[cadena[i - 1]].setSiguiente(cadena[i]);
            }
        }
    }

    //reserva bloques para un archivo nuevo, devuelve la cadena o null si no hay suficientes
    public int[] asignar(int cantidad, String ocupante, int color) {
        llave.acquireUninterruptibly();
        try {
            if (cantidad < 1) {
                return null;
            }
            return encadenar(cantidad, 0, bloquesArchivos, 0, ocupante, color);
        } finally {
            llave.release();
        }
    }

    //igual pero el primer bloque tiene que ser el que dice el archivo (para cargar el json)
    public int[] asignarDesde(int primero, int cantidad, String ocupante, int color) {
        llave.acquireUninterruptibly();
        try {
            if (primero < 0 || primero >= bloquesArchivos) {
                throw new IllegalArgumentException(ocupante + ": el bloque " + primero + " no existe en el disco");
            }
            if (bloques[primero].estaOcupado()) {
                throw new IllegalArgumentException(ocupante + ": el bloque " + primero + " ya lo ocupa "
                        + bloques[primero].getOcupante());
            }
            int[] cadena = encadenar(cantidad, 0, bloquesArchivos, primero, ocupante, color);
            if (cadena == null) {
                throw new IllegalArgumentException(ocupante + ": no hay bloques libres suficientes");
            }
            return cadena;
        } finally {
            llave.release();
        }
    }

    //ocupa exactamente los bloques de la cadena (para cargar un estado guardado)
    public void asignarCadena(int[] cadena, String ocupante, int color) {
        llave.acquireUninterruptibly();
        try {
            for (int i = 0; i < cadena.length; i++) {
                int b = cadena[i];
                if (b < 0 || b >= bloquesArchivos) {
                    throw new IllegalArgumentException(ocupante + ": el bloque " + b + " no existe en el disco");
                }
                if (bloques[b].estaOcupado()) {
                    throw new IllegalArgumentException(ocupante + ": el bloque " + b + " ya lo ocupa "
                            + bloques[b].getOcupante());
                }
                for (int j = 0; j < i; j++) {
                    if (cadena[j] == b) {
                        throw new IllegalArgumentException(ocupante + ": el bloque " + b + " esta repetido");
                    }
                }
            }
            marcar(cadena, ocupante, color);
        } finally {
            llave.release();
        }
    }

    //reserva bloques del area de intercambio para un proceso suspendido
    public int[] asignarSwap(int cantidad, String ocupante) {
        llave.acquireUninterruptibly();
        try {
            if (cantidad < 1 || bloquesSwap == 0) {
                return null;
            }
            return encadenar(cantidad, bloquesArchivos, bloques.length, bloquesArchivos, ocupante, -1);
        } finally {
            llave.release();
        }
    }

    //libera toda la cadena que empieza en ese bloque
    public void liberar(int primero) {
        llave.acquireUninterruptibly();
        try {
            int b = primero;
            int vistos = 0;
            while (b >= 0 && b < bloques.length && vistos < bloques.length) {
                int sig = bloques[b].getSiguiente();
                bloques[b].liberar();
                b = sig;
                vistos++;
            }
        } finally {
            llave.release();
        }
    }

    //los bloques de un archivo siguiendo los enlaces desde el primero
    public int[] cadena(int primero) {
        llave.acquireUninterruptibly();
        try {
            int cuantos = 0;
            int b = primero;
            while (b >= 0 && b < bloques.length && bloques[b].estaOcupado() && cuantos < bloques.length) {
                cuantos++;
                b = bloques[b].getSiguiente();
            }
            int[] c = new int[cuantos];
            b = primero;
            for (int i = 0; i < cuantos; i++) {
                c[i] = b;
                b = bloques[b].getSiguiente();
            }
            return c;
        } finally {
            llave.release();
        }
    }

    //cuando se renombra un archivo sus bloques tienen que mostrar el nombre nuevo
    public void cambiarOcupante(int primero, String ocupante) {
        llave.acquireUninterruptibly();
        try {
            int b = primero;
            int vistos = 0;
            while (b >= 0 && b < bloques.length && vistos < bloques.length) {
                bloques[b].setOcupante(ocupante);
                b = bloques[b].getSiguiente();
                vistos++;
            }
        } finally {
            llave.release();
        }
    }

    public int getLibres() {
        return contarLibres(0, bloquesArchivos);
    }

    public int getLibresSwap() {
        return contarLibres(bloquesArchivos, bloques.length);
    }

    private int contarLibres(int desde, int hasta) {
        llave.acquireUninterruptibly();
        try {
            int libres = 0;
            for (int i = desde; i < hasta; i++) {
                if (!bloques[i].estaOcupado()) {
                    libres++;
                }
            }
            return libres;
        } finally {
            llave.release();
        }
    }

    //----- para que la ventana pueda mirar el disco sin tocarlo -----

    public Bloque[] copiarBloques() {
        llave.acquireUninterruptibly();
        try {
            Bloque[] copia = new Bloque[bloques.length];
            for (int i = 0; i < bloques.length; i++) {
                copia[i] = bloques[i].copia();
            }
            return copia;
        } finally {
            llave.release();
        }
    }

    public SolicitudES[] copiarCola() {
        llave.acquireUninterruptibly();
        try {
            SolicitudES[] copia = new SolicitudES[cola.len()];
            for (int i = 0; i < copia.length; i++) {
                copia[i] = cola.retornar(i);
            }
            return copia;
        } finally {
            llave.release();
        }
    }

    public SolicitudES getActual() {
        return actual;
    }

    public int getCabezal() {
        return cabezal;
    }

    public DireccionCabezal getDireccion() {
        return direccion;
    }

    public int getBloquesArchivos() {
        return bloquesArchivos;
    }

    public int getBloquesSwap() {
        return bloquesSwap;
    }

    public int getRetenidas() {
        return retenidas;
    }

    public boolean estaOcioso() {
        return actual == null && !enViaje && cola.estaVacia();
    }

    public long getDesplazamientoTotal() {
        return desplazamientoTotal;
    }

    public long getAtendidas() {
        return atendidas;
    }

    public long getAtendidas(PoliticaDisco p) {
        return atendidasPorPolitica[p.ordinal()];
    }

    //promedio de ciclos que espero una solicitud en la cola antes de que el disco la tomara
    public double getEsperaPromedio() {
        llave.acquireUninterruptibly();
        try {
            long empezadas = atendidas + (actual != null ? 1 : 0);
            if (empezadas == 0) {
                return 0;
            }
            return (double) sumaEspera / empezadas;
        } finally {
            llave.release();
        }
    }

    private void registrar(String mensaje) {
        if (log != null) {
            log.registrar((int) ciclo, mensaje);
        }
    }
}
