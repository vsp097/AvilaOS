package avilaos;

/**
 *
 * @author andrea
 */
public class Configuracion {

    //estas clases de adentro son solo datos, por eso sus campos son publicos
    //(asi gson los puede leer y escribir directo)

    //un computador: tamano de su ram y la politica con la que arranca
    public static class ConfigComputador {
        public int ram;
        public String politica;
        public int quantum;

        public ConfigComputador() {
        }

        public ConfigComputador(int ram, String politica, int quantum) {
            this.ram = ram;
            this.politica = politica;
            this.quantum = quantum;
        }
    }

    //un buffer: su capacidad y en que computador vive (1, 2, 3...)
    public static class ConfigBuffer {
        public int capacidad;
        public int computadorAnfitrion;

        public ConfigBuffer() {
        }

        public ConfigBuffer(int capacidad, int computadorAnfitrion) {
            this.capacidad = capacidad;
            this.computadorAnfitrion = computadorAnfitrion;
        }
    }

    //un proceso con todos sus datos, los mismos que pide el formulario
    public static class ConfigProceso {
        public String nombre;
        public int instrucciones;
        public float memoria;
        public int prioridad;
        public int deadline;
        public String tipo;
        public int computador;
        //posicion del buffer, -1 si no usa
        public int buffer;
        public int ciclos;
        public int cadaCuantosCiclos;
        public int elementos;
    }

    //duracion de un ciclo en milisegundos
    public int duracionCicloMs;
    //ciclos extra que cuesta usar un buffer que esta en otro computador
    public int latenciaRed;
    public ConfigComputador[] computadores;
    public ConfigBuffer[] buffers;
    public ConfigProceso[] procesos;

    //configuracion con la que arranca el simulador si no se carga ningun archivo
    public static Configuracion porDefecto() {
        Configuracion c = new Configuracion();
        c.duracionCicloMs = 1000;
        c.latenciaRed = 2;
        c.computadores = new ConfigComputador[]{
            new ConfigComputador(1024, "FCFS", 4),
            new ConfigComputador(1024, "FCFS", 4)
        };
        c.buffers = new ConfigBuffer[0];
        c.procesos = new ConfigProceso[0];
        return c;
    }

    //revisa que todo este en rango, si algo esta mal lanza un error con el mensaje
    public void validar() {
        rango(duracionCicloMs, "La duracion del ciclo", 1, 60000);
        rango(latenciaRed, "La latencia de red", 0, 1000);

        if (computadores == null || computadores.length < 2 || computadores.length > Metricas.MAX_COMPUTADORES) {
            throw new IllegalArgumentException("Tiene que haber entre 2 y " + Metricas.MAX_COMPUTADORES + " computadores");
        }
        for (int i = 0; i < computadores.length; i++) {
            ConfigComputador c = computadores[i];
            String nombre = "Computador " + (i + 1);
            if (c == null) {
                throw new IllegalArgumentException(nombre + " esta vacio");
            }
            rango(c.ram, "La RAM del " + nombre, 1, 1000000);
            rango(c.quantum, "El quantum del " + nombre, 1, 1000);
            try {
                PoliticaPlanificacion.valueOf(c.politica);
            } catch (IllegalArgumentException | NullPointerException e) {
                throw new IllegalArgumentException("La politica del " + nombre + " no es valida");
            }
        }

        //si falta el arreglo de buffers o procesos lo dejo vacio
        if (buffers == null) {
            buffers = new ConfigBuffer[0];
        }
        if (procesos == null) {
            procesos = new ConfigProceso[0];
        }

        if (buffers.length > Metricas.MAX_BUFFERS) {
            throw new IllegalArgumentException("No puede haber mas de " + Metricas.MAX_BUFFERS + " buffers");
        }
        for (int i = 0; i < buffers.length; i++) {
            ConfigBuffer b = buffers[i];
            String nombre = "Buffer " + (i + 1);
            if (b == null) {
                throw new IllegalArgumentException(nombre + " esta vacio");
            }
            rango(b.capacidad, "La capacidad del " + nombre, 1, 1000);
            rango(b.computadorAnfitrion, "El computador anfitrion del " + nombre, 1, computadores.length);
        }

        for (int i = 0; i < procesos.length; i++) {
            ConfigProceso p = procesos[i];
            String nombre = "Proceso " + (i + 1);
            if (p == null) {
                throw new IllegalArgumentException(nombre + " esta vacio");
            }
            if (p.nombre == null || p.nombre.trim().isEmpty()) {
                throw new IllegalArgumentException(nombre + ": el nombre no puede estar vacio");
            }
            rango(p.instrucciones, nombre + ": las instrucciones", 1, 100000);
            rango(p.prioridad, nombre + ": la prioridad", 1, 10);
            rango(p.deadline, nombre + ": el deadline", 0, 100000);
            rango(p.computador, nombre + ": el computador", 1, computadores.length);
            if (p.memoria < 1 || p.memoria > 100000) {
                throw new IllegalArgumentException(nombre + ": la memoria debe estar entre 1 y 100000");
            }
            boolean prodCons = "Productor".equals(p.tipo) || "Consumidor".equals(p.tipo);
            boolean cpuIo = "CPU Bound".equals(p.tipo) || "I/O Bound".equals(p.tipo);
            if (!prodCons && !cpuIo) {
                throw new IllegalArgumentException(nombre + ": el tipo no es valido");
            }
            if (prodCons) {
                rango(p.buffer, nombre + ": el buffer", 0, buffers.length - 1);
                rango(p.cadaCuantosCiclos, nombre + ": cada cuantos ciclos", 1, 1000);
                rango(p.elementos, nombre + ": los elementos", 1, 100000);
            } else {
                rango(p.ciclos, nombre + ": los ciclos", 1, 100000);
            }
        }
    }

    private void rango(int valor, String campo, int min, int max) {
        if (valor < min || valor > max) {
            throw new IllegalArgumentException(campo + " debe estar entre " + min + " y " + max);
        }
    }
}