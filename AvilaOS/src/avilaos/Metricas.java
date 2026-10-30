package avilaos;

import java.util.concurrent.Semaphore;

/**
 *
 * @author andrea
 */
public class Metricas {

    public static final int MAX_COMPUTADORES = 10;
    public static final int MAX_BUFFERS = 20;
    //cuantas muestras guardo para la grafica, cuando se llena se pierden las mas viejas
    private static final int MAX_MUESTRAS = 500;
    //cada cuantos ciclos saco un punto de la grafica
    private static final int CICLOS_POR_MUESTRA = 5;

    //semaforo para que el reloj y la ventana no usen los numeros al mismo tiempo
    private final Semaphore mutex = new Semaphore(1);

    //datos por computador (el computador 1 es el indice 0)
    private long[] ciclosTotales = new long[MAX_COMPUTADORES];
    private long[] ciclosOcupada = new long[MAX_COMPUTADORES];
    private long[] completados = new long[MAX_COMPUTADORES];
    private long[] sumaRespuesta = new long[MAX_COMPUTADORES];
    private long[] cantidadRespuesta = new long[MAX_COMPUTADORES];
    private long[] deadlineCumplidos = new long[MAX_COMPUTADORES];
    private long[] deadlineTotales = new long[MAX_COMPUTADORES];

    //para la equidad: suma de x, suma de x al cuadrado y cuantos procesos
    private double[] sumaX = new double[MAX_COMPUTADORES];
    private double[] sumaX2 = new double[MAX_COMPUTADORES];
    private long[] cantidadX = new long[MAX_COMPUTADORES];

    //para la grafica: utilizacion de cada ventana de ciclos
    private int[] cicloEnVentana = new int[MAX_COMPUTADORES];
    private int[] ocupadoEnVentana = new int[MAX_COMPUTADORES];
    private double[][] historial = new double[MAX_COMPUTADORES][MAX_MUESTRAS];
    private int[] inicioHistorial = new int[MAX_COMPUTADORES];
    private int[] cantidadHistorial = new int[MAX_COMPUTADORES];

    //para las otras graficas: throughput, tasa de exito y tiempo de espera de todo el sistema
    public static final int THROUGHPUT = 0;
    public static final int EXITO = 1;
    public static final int ESPERA = 2;
    private double[][] historialSistema = new double[3][MAX_MUESTRAS];
    private int inicioSistema = 0;
    private int cantidadSistema = 0;

    //datos por buffer
    private long[] producidos = new long[MAX_BUFFERS];
    private long[] consumidos = new long[MAX_BUFFERS];
    private long[] sumaBloqueo = new long[MAX_BUFFERS];
    private long[] cantidadBloqueo = new long[MAX_BUFFERS];

    private boolean computadorValido(int c) {
        return c >= 0 && c < MAX_COMPUTADORES;
    }

    private boolean bufferValido(int b) {
        return b >= 0 && b < MAX_BUFFERS;
    }

    private double dividir(double a, double b) {
        if (b == 0) {
            return 0;
        }
        return a / b;
    }

    //----- avisos que le hace el sistema a las metricas -----

    //se llama una vez por ciclo en cada computador, cpuOcupada es true si habia un proceso en la cpu
    public void registrarCiclo(int c, boolean cpuOcupada) {
        if (!computadorValido(c)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            ciclosTotales[c]++;
            cicloEnVentana[c]++;
            if (cpuOcupada) {
                ciclosOcupada[c]++;
                ocupadoEnVentana[c]++;
            }
            //cuando se completa una ventana guardo un punto para la grafica
            if (cicloEnVentana[c] == CICLOS_POR_MUESTRA) {
                double u = ocupadoEnVentana[c] * 100.0 / CICLOS_POR_MUESTRA;
                int pos = (inicioHistorial[c] + cantidadHistorial[c]) % MAX_MUESTRAS;
                if (cantidadHistorial[c] < MAX_MUESTRAS) {
                    cantidadHistorial[c]++;
                } else {
                    inicioHistorial[c] = (inicioHistorial[c] + 1) % MAX_MUESTRAS;
                }
                historial[c][pos] = u;
                cicloEnVentana[c] = 0;
                ocupadoEnVentana[c] = 0;
                //el computador 1 siempre existe, asi que con el saco tambien el punto del sistema
                if (c == 0) {
                    guardarMuestraSistema();
                }
            }
        } finally {
            mutex.release();
        }
    }

    //cuando un proceso entra por primera vez a la cpu: ciclos que espero desde que llego
    public void registrarRespuesta(int c, long ciclosRespuesta) {
        if (!computadorValido(c)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            sumaRespuesta[c] += ciclosRespuesta;
            cantidadRespuesta[c]++;
        } finally {
            mutex.release();
        }
    }

    //cuando un proceso termina normal: ciclos que uso la cpu y ciclos que estuvo en el sistema
    public void registrarTermino(int c, long ciclosCPU, long ciclosEnSistema) {
        if (!computadorValido(c)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            completados[c]++;
            if (ciclosEnSistema > 0) {
                //x es la fraccion del tiempo que el proceso estuvo realmente en la cpu
                double x = (double) ciclosCPU / ciclosEnSistema;
                sumaX[c] += x;
                sumaX2[c] += x * x;
                cantidadX[c]++;
            }
        } finally {
            mutex.release();
        }
    }

    //se llama cuando termina un proceso que tenia deadline (mayor que 0), cumplio es false si se le acabo
    public void registrarDeadline(int c, boolean cumplio) {
        if (!computadorValido(c)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            deadlineTotales[c]++;
            if (cumplio) {
                deadlineCumplidos[c]++;
            }
        } finally {
            mutex.release();
        }
    }

    public void registrarProducido(int b) {
        if (!bufferValido(b)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            producidos[b]++;
        } finally {
            mutex.release();
        }
    }

    public void registrarConsumido(int b) {
        if (!bufferValido(b)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            consumidos[b]++;
        } finally {
            mutex.release();
        }
    }

    //cuando un proceso se despierta de un semaforo del buffer: cuantos ciclos estuvo bloqueado
    public void registrarBloqueo(int b, long ciclosBloqueado) {
        if (!bufferValido(b)) {
            return;
        }
        mutex.acquireUninterruptibly();
        try {
            sumaBloqueo[b] += ciclosBloqueado;
            cantidadBloqueo[b]++;
        } finally {
            mutex.release();
        }
    }

    //----- resultados por computador -----

    public double getUtilizacion(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return dividir(ciclosOcupada[c] * 100.0, ciclosTotales[c]);
        } finally {
            mutex.release();
        }
    }

    //procesos terminados por ciclo
    public double getThroughput(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return dividir(completados[c], ciclosTotales[c]);
        } finally {
            mutex.release();
        }
    }

    public double getRespuestaPromedio(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return dividir(sumaRespuesta[c], cantidadRespuesta[c]);
        } finally {
            mutex.release();
        }
    }

    //porcentaje de procesos con deadline que lo cumplieron
    public double getCumplimientoDeadline(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return dividir(deadlineCumplidos[c] * 100.0, deadlineTotales[c]);
        } finally {
            mutex.release();
        }
    }

    //indice de jain: 1 es lo mas justo, devuelve 0 si todavia no termino ningun proceso
    public double getEquidad(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return jain(sumaX[c], sumaX2[c], cantidadX[c]);
        } finally {
            mutex.release();
        }
    }

    public long getCompletados(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return completados[c];
        } finally {
            mutex.release();
        }
    }

    private double jain(double suma, double sumaCuadrados, long n) {
        if (n == 0 || sumaCuadrados == 0) {
            return 0;
        }
        return (suma * suma) / (n * sumaCuadrados);
    }

    //----- resultados de todo el sistema -----

    public double getUtilizacionSistema() {
        mutex.acquireUninterruptibly();
        try {
            long ocupada = 0;
            long total = 0;
            for (int i = 0; i < MAX_COMPUTADORES; i++) {
                ocupada += ciclosOcupada[i];
                total += ciclosTotales[i];
            }
            return dividir(ocupada * 100.0, total);
        } finally {
            mutex.release();
        }
    }

    //como todos los computadores avanzan juntos, uso los ciclos del que mas lleva
    public double getThroughputSistema() {
        mutex.acquireUninterruptibly();
        try {
            return throughputSistema();
        } finally {
            mutex.release();
        }
    }

    //sin el semaforo, para usarlo desde adentro cuando ya esta tomado
    private double throughputSistema() {
        long terminados = 0;
        long ciclos = 0;
        for (int i = 0; i < MAX_COMPUTADORES; i++) {
            terminados += completados[i];
            if (ciclosTotales[i] > ciclos) {
                ciclos = ciclosTotales[i];
            }
        }
        return dividir(terminados, ciclos);
    }

    public double getRespuestaPromedioSistema() {
        mutex.acquireUninterruptibly();
        try {
            return respuestaSistema();
        } finally {
            mutex.release();
        }
    }

    //sin el semaforo, para usarlo desde adentro cuando ya esta tomado
    private double respuestaSistema() {
        long suma = 0;
        long cantidad = 0;
        for (int i = 0; i < MAX_COMPUTADORES; i++) {
            suma += sumaRespuesta[i];
            cantidad += cantidadRespuesta[i];
        }
        return dividir(suma, cantidad);
    }

    public double getCumplimientoDeadlineSistema() {
        mutex.acquireUninterruptibly();
        try {
            return exitoSistema();
        } finally {
            mutex.release();
        }
    }

    //sin el semaforo, para usarlo desde adentro cuando ya esta tomado
    private double exitoSistema() {
        long cumplidos = 0;
        long total = 0;
        for (int i = 0; i < MAX_COMPUTADORES; i++) {
            cumplidos += deadlineCumplidos[i];
            total += deadlineTotales[i];
        }
        return dividir(cumplidos * 100.0, total);
    }

    public double getEquidadSistema() {
        mutex.acquireUninterruptibly();
        try {
            double suma = 0;
            double sumaCuadrados = 0;
            long n = 0;
            for (int i = 0; i < MAX_COMPUTADORES; i++) {
                suma += sumaX[i];
                sumaCuadrados += sumaX2[i];
                n += cantidadX[i];
            }
            return jain(suma, sumaCuadrados, n);
        } finally {
            mutex.release();
        }
    }

    //----- resultados por buffer -----

    public long getProducidos(int b) {
        if (!bufferValido(b)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return producidos[b];
        } finally {
            mutex.release();
        }
    }

    public long getConsumidos(int b) {
        if (!bufferValido(b)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return consumidos[b];
        } finally {
            mutex.release();
        }
    }

    //ciclos promedio que los procesos pasan bloqueados en los semaforos de ese buffer
    public double getBloqueoPromedio(int b) {
        if (!bufferValido(b)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return dividir(sumaBloqueo[b], cantidadBloqueo[b]);
        } finally {
            mutex.release();
        }
    }

    //igual pero juntando todos los buffers
    public double getBloqueoPromedioSistema() {
        mutex.acquireUninterruptibly();
        try {
            long suma = 0;
            long cantidad = 0;
            for (int i = 0; i < MAX_BUFFERS; i++) {
                suma += sumaBloqueo[i];
                cantidad += cantidadBloqueo[i];
            }
            return dividir(suma, cantidad);
        } finally {
            mutex.release();
        }
    }

    //----- datos para la grafica -----

    //cuantos puntos hay guardados de ese computador
    public int getCantidadMuestras(int c) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            return cantidadHistorial[c];
        } finally {
            mutex.release();
        }
    }

    //utilizacion del punto i (0 es el mas viejo), cada punto resume CICLOS_POR_MUESTRA ciclos
    public double getMuestra(int c, int i) {
        if (!computadorValido(c)) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            if (i < 0 || i >= cantidadHistorial[c]) {
                return 0;
            }
            return historial[c][(inicioHistorial[c] + i) % MAX_MUESTRAS];
        } finally {
            mutex.release();
        }
    }

    //guarda como van el throughput, la tasa de exito y la espera en este momento
    //se llama con el mutex ya tomado
    private void guardarMuestraSistema() {
        int pos = (inicioSistema + cantidadSistema) % MAX_MUESTRAS;
        if (cantidadSistema < MAX_MUESTRAS) {
            cantidadSistema++;
        } else {
            inicioSistema = (inicioSistema + 1) % MAX_MUESTRAS;
        }
        historialSistema[THROUGHPUT][pos] = throughputSistema();
        historialSistema[EXITO][pos] = exitoSistema();
        historialSistema[ESPERA][pos] = respuestaSistema();
    }

    public int getCantidadMuestrasSistema() {
        mutex.acquireUninterruptibly();
        try {
            return cantidadSistema;
        } finally {
            mutex.release();
        }
    }

    //cual es THROUGHPUT, EXITO o ESPERA; i es el punto (0 es el mas viejo)
    public double getMuestraSistema(int cual, int i) {
        if (cual < 0 || cual > 2) {
            return 0;
        }
        mutex.acquireUninterruptibly();
        try {
            if (i < 0 || i >= cantidadSistema) {
                return 0;
            }
            return historialSistema[cual][(inicioSistema + i) % MAX_MUESTRAS];
        } finally {
            mutex.release();
        }
    }

    public int getCiclosPorMuestra() {
        return CICLOS_POR_MUESTRA;
    }
}