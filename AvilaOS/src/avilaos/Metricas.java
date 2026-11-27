package avilaos;

import java.util.concurrent.Semaphore;

/**
 *
 * @author andrea
 */
public class Metricas {

    //cuantas muestras guardo para las graficas de la cpu, cuando se llena se pierden las mas viejas
    private static final int MAX_MUESTRAS = 500;
    //cada cuantos ciclos saco un punto de esas graficas
    private static final int CICLOS_POR_MUESTRA = 5;
    //para la grafica del cabezal se guarda un punto por ciclo
    private static final int MAX_MUESTRAS_DISCO = 5000;

    //semaforo para que el reloj y la ventana no usen los numeros al mismo tiempo
    private final Semaphore mutex = new Semaphore(1);

    //----- metricas del proyecto 1, ahora de un solo computador -----
    private long ciclosTotales = 0;
    private long ciclosOcupada = 0;
    private long completados = 0;
    private long sumaRespuesta = 0;
    private long cantidadRespuesta = 0;
    private long deadlineCumplidos = 0;
    private long deadlineTotales = 0;

    //para la equidad: suma de x, suma de x al cuadrado y cuantos procesos
    private double sumaX = 0;
    private double sumaX2 = 0;
    private long cantidadX = 0;

    //para la grafica de utilizacion: lo que va de la ventana de ciclos actual
    private int cicloEnVentana = 0;
    private int ocupadoEnVentana = 0;

    //historiales de las graficas: utilizacion, throughput, tasa de exito y tiempo de espera
    public static final int UTILIZACION = 0;
    public static final int THROUGHPUT = 1;
    public static final int EXITO = 2;
    public static final int ESPERA = 3;
    private double[][] historial = new double[4][MAX_MUESTRAS];
    private int inicioHistorial = 0;
    private int cantidadHistorial = 0;

    //----- suspension -----
    private long suspensiones = 0;
    private long reactivaciones = 0;

    //----- disco: desplazamiento acumulado del cabezal en cada ciclo y la politica que estaba activa -----
    private long[] cicloDisco = new long[MAX_MUESTRAS_DISCO];
    private long[] desplazamientoDisco = new long[MAX_MUESTRAS_DISCO];
    private PoliticaDisco[] politicaDisco = new PoliticaDisco[MAX_MUESTRAS_DISCO];
    private int inicioDisco = 0;
    private int cantidadDisco = 0;

    private double dividir(double a, double b) {
        if (b == 0) {
            return 0;
        }
        return a / b;
    }

    //----- avisos que le hace el sistema a las metricas -----

    //se llama una vez por ciclo, cpuOcupada es true si habia un proceso en la cpu
    public void registrarCiclo(boolean cpuOcupada) {
        mutex.acquireUninterruptibly();
        try {
            ciclosTotales++;
            cicloEnVentana++;
            if (cpuOcupada) {
                ciclosOcupada++;
                ocupadoEnVentana++;
            }
            //cuando se completa una ventana guardo un punto para las graficas
            if (cicloEnVentana == CICLOS_POR_MUESTRA) {
                guardarMuestra(ocupadoEnVentana * 100.0 / CICLOS_POR_MUESTRA);
                cicloEnVentana = 0;
                ocupadoEnVentana = 0;
            }
        } finally {
            mutex.release();
        }
    }

    //cuando un proceso entra por primera vez a la cpu: ciclos que espero desde que llego
    public void registrarRespuesta(long ciclosRespuesta) {
        mutex.acquireUninterruptibly();
        try {
            sumaRespuesta += ciclosRespuesta;
            cantidadRespuesta++;
        } finally {
            mutex.release();
        }
    }

    //cuando un proceso termina normal: ciclos que uso la cpu y ciclos que estuvo en el sistema
    public void registrarTermino(long ciclosCPU, long ciclosEnSistema) {
        mutex.acquireUninterruptibly();
        try {
            completados++;
            if (ciclosEnSistema > 0) {
                //x es la fraccion del tiempo que el proceso estuvo realmente en la cpu
                double x = (double) ciclosCPU / ciclosEnSistema;
                sumaX += x;
                sumaX2 += x * x;
                cantidadX++;
            }
        } finally {
            mutex.release();
        }
    }

    //se llama cuando termina un proceso que tenia deadline, cumplio es false si se le acabo
    public void registrarDeadline(boolean cumplio) {
        mutex.acquireUninterruptibly();
        try {
            deadlineTotales++;
            if (cumplio) {
                deadlineCumplidos++;
            }
        } finally {
            mutex.release();
        }
    }

    //cuando el disco termina de escribir un proceso en el area de intercambio
    public void registrarSuspension() {
        mutex.acquireUninterruptibly();
        try {
            suspensiones++;
        } finally {
            mutex.release();
        }
    }

    //cuando el disco termina de traer un proceso de vuelta a la RAM
    public void registrarReactivacion() {
        mutex.acquireUninterruptibly();
        try {
            reactivaciones++;
        } finally {
            mutex.release();
        }
    }

    //una muestra por ciclo: cuanto lleva movido el cabezal y con que politica
    public void registrarDisco(long ciclo, long desplazamiento, PoliticaDisco politica) {
        mutex.acquireUninterruptibly();
        try {
            int pos = (inicioDisco + cantidadDisco) % MAX_MUESTRAS_DISCO;
            if (cantidadDisco < MAX_MUESTRAS_DISCO) {
                cantidadDisco++;
            } else {
                inicioDisco = (inicioDisco + 1) % MAX_MUESTRAS_DISCO;
            }
            cicloDisco[pos] = ciclo;
            desplazamientoDisco[pos] = desplazamiento;
            politicaDisco[pos] = politica;
        } finally {
            mutex.release();
        }
    }

    //----- resultados del computador -----

    public double getUtilizacion() {
        mutex.acquireUninterruptibly();
        try {
            return dividir(ciclosOcupada * 100.0, ciclosTotales);
        } finally {
            mutex.release();
        }
    }

    //procesos terminados por ciclo
    public double getThroughput() {
        mutex.acquireUninterruptibly();
        try {
            return throughput();
        } finally {
            mutex.release();
        }
    }

    public double getRespuestaPromedio() {
        mutex.acquireUninterruptibly();
        try {
            return respuesta();
        } finally {
            mutex.release();
        }
    }

    //porcentaje de procesos con deadline que lo cumplieron
    public double getCumplimientoDeadline() {
        mutex.acquireUninterruptibly();
        try {
            return exito();
        } finally {
            mutex.release();
        }
    }

    //indice de jain: 1 es lo mas justo, devuelve 0 si todavia no termino ningun proceso
    public double getEquidad() {
        mutex.acquireUninterruptibly();
        try {
            if (cantidadX == 0 || sumaX2 == 0) {
                return 0;
            }
            return (sumaX * sumaX) / (cantidadX * sumaX2);
        } finally {
            mutex.release();
        }
    }

    public long getCompletados() {
        mutex.acquireUninterruptibly();
        try {
            return completados;
        } finally {
            mutex.release();
        }
    }

    public long getSuspensiones() {
        mutex.acquireUninterruptibly();
        try {
            return suspensiones;
        } finally {
            mutex.release();
        }
    }

    public long getReactivaciones() {
        mutex.acquireUninterruptibly();
        try {
            return reactivaciones;
        } finally {
            mutex.release();
        }
    }

    //sin el semaforo, para usarlos desde adentro cuando ya esta tomado
    private double throughput() {
        return dividir(completados, ciclosTotales);
    }

    private double respuesta() {
        return dividir(sumaRespuesta, cantidadRespuesta);
    }

    private double exito() {
        return dividir(deadlineCumplidos * 100.0, deadlineTotales);
    }

    //----- datos para las graficas del computador -----

    //se llama con el mutex ya tomado
    private void guardarMuestra(double utilizacion) {
        int pos = (inicioHistorial + cantidadHistorial) % MAX_MUESTRAS;
        if (cantidadHistorial < MAX_MUESTRAS) {
            cantidadHistorial++;
        } else {
            inicioHistorial = (inicioHistorial + 1) % MAX_MUESTRAS;
        }
        historial[UTILIZACION][pos] = utilizacion;
        historial[THROUGHPUT][pos] = throughput();
        historial[EXITO][pos] = exito();
        historial[ESPERA][pos] = respuesta();
    }

    public int getCantidadMuestras() {
        mutex.acquireUninterruptibly();
        try {
            return cantidadHistorial;
        } finally {
            mutex.release();
        }
    }

    //cual es UTILIZACION, THROUGHPUT, EXITO o ESPERA; i es el punto (0 es el mas viejo)
    public double getMuestra(int cual, int i) {
        if (cual < 0 || cual >= historial.length) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            if (i < 0 || i >= cantidadHistorial) {
                return 0;
            }
            return historial[cual][(inicioHistorial + i) % MAX_MUESTRAS];
        } finally {
            mutex.release();
        }
    }

    public int getCiclosPorMuestra() {
        return CICLOS_POR_MUESTRA;
    }

    //----- datos para la grafica del cabezal -----

    public int getCantidadMuestrasDisco() {
        mutex.acquireUninterruptibly();
        try {
            return cantidadDisco;
        } finally {
            mutex.release();
        }
    }

    //copia de las muestras del disco para dibujarlas sin tener el semaforo tomado
    //ciclos, desplazamientos y politicas tienen que tener el largo de getCantidadMuestrasDisco()
    //devuelve cuantas copio
    public int copiarMuestrasDisco(long[] ciclos, long[] desplazamientos, PoliticaDisco[] politicas) {
        mutex.acquireUninterruptibly();
        try {
            int n = Math.min(cantidadDisco, Math.min(ciclos.length, Math.min(desplazamientos.length, politicas.length)));
            for (int i = 0; i < n; i++) {
                int pos = (inicioDisco + i) % MAX_MUESTRAS_DISCO;
                ciclos[i] = cicloDisco[pos];
                desplazamientos[i] = desplazamientoDisco[pos];
                politicas[i] = politicaDisco[pos];
            }
            return n;
        } finally {
            mutex.release();
        }
    }
}
