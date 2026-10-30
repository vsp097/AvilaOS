/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;
import avilaos.Buffer;
import avilaos.ClockListener;
import avilaos.FCFS;
import avilaos.Planificador;
import avilaos.LogEventos;
import avilaos.Metricas;
import java.util.concurrent.Semaphore;

/**
 *
 * @author diego
 */
public class Computador implements ClockListener{
    protected String CPUt;
    protected float RAMt;
    protected String CPU;
    protected float RAM;
    protected String datatype;
    protected Cola<PCB> Nuevos = new Cola<>();
    protected Cola<PCB> Listos = new Cola<>();
    protected Cola<PCB> Bloqueados = new Cola<>();
    protected Cola<PCB> Terminados = new Cola<>();
    protected PCB enEjecucion;
    protected ModoCPU modo = ModoCPU.Usuario;
    protected String nombre = "Computador";
    protected LogEventos log;
    protected int ciclosUsados = 0;
    protected long ciclo = 0;
    //cada cuanto pide e/s y cuanto dura
    protected int cadaES = 3;
    protected int duracionES = 2;

        //las metricas del sistema (una sola para todos) y el numero de este computador (0 es el primero)
    protected Metricas metricas;
    protected int indice = 0;

    public void setMetricas(Metricas m, int i) {
        metricas = m;
        indice = i;
    }

    //ciclos que tarda la red
    protected int retrasoRed = 2;

    public void setRetrasoRed(int r) {
        retrasoRed = r;

    }
    
    
    public Computador(){
        CPU = null;
        RAM = 0;
        CPUt = null;
        RAMt = 0;   
        datatype = null;
    }
    
    public PCB getEnEjecucion() {
        return enEjecucion;
    }

    //semaforo de java para la exclusion mutua entre el hilo del reloj y el de la ventana
    private final Semaphore llave = new Semaphore(1);

    //la ventana toma el candado antes de recorrer las colas y lo suelta al terminar
    public void tomar() {
        llave.acquireUninterruptibly();
    }

    public void soltar() {
        llave.release();
    }
    
    public void admitir() {
        tomar();
        try {
            admitirInterno();
        } finally {
            soltar();
        }
    }

    //el cuerpo sin candado, para poder llamarlo desde onTick que ya lo tiene tomado
    private void admitirInterno() {
        Cola<PCB> siguen = new Cola<>();
        PCB p = Nuevos.Desencolar();
        while (p != null) {
            if (p.getmem() <= RAM) {
                useRam(p.getmem());
                p.setstate(EstadoProceso.Listo);
                Listos.Encolar(p);
                registrar("Proceso " + p.getname() + " es admitido y pasa a listos");
            } else {
                siguen.Encolar(p);
            }
            p = Nuevos.Desencolar();
        }
        moverTodos(siguen, Nuevos);
    }

    
    protected Planificador planificador = new FCFS();

    public void setPlanificador(Planificador p) {
        planificador = p;
    }

    public Planificador getPlanificador() {
        return planificador;
    }
    
    public boolean isnull(){
        if(CPU == null){
            return true;
        }
        return false;
    }
    
    public void setRAMt(float t){
        RAM=RAMt = t;
    }
    
    public void settype(String t){
        datatype = t;
    }
    
    public void setCPUt(String t){
        CPU=CPUt =t;
        
    }
    
    public void setmode(ModoCPU m){
        modo = m;
    }

    public ModoCPU getmode(){
        return modo;
    }
    
        public void setNombre(String n) {
        nombre = n;
    }

    public String getNombre() {
        return nombre;
    }

    public void setLog(LogEventos l) {
        log = l;
    }

    public Cola<PCB> getNuevos() {
        return Nuevos;
    }

    public Cola<PCB> getListos() {
        return Listos;
    }

    public Cola<PCB> getBloqueados() {
        return Bloqueados;
    }

    public Cola<PCB> getTerminados() {
        return Terminados;
    }

    //el PC que se muestra en la ventana es el del proceso que esta en la CPU
    public int getPC() {
        if (enEjecucion == null) {
            return 0;
        }
        return enEjecucion.getpc();
    }

    //un proceso nuevo entra a la cola de nuevos y espera a que haya RAM
    public void agregarProceso(PCB p) {
        tomar();
        try {
            p.setstate(EstadoProceso.Nuevo);
            p.setComputador(nombre);
            p.setLlegada(ciclo);
            p.setTuvoDeadline(p.getdeadline() > 0);
            Nuevos.Encolar(p);
            registrar("Proceso " + p.getname() + " llega a la cola de nuevos");
        } finally {
            soltar();
        }
    }
    
    public String getCPU(){
        return CPU;
    }
    
    public float getRam(){
        return RAM;
    }
    
    public float getRamt(){
        return RAMt;
    }
    
    public String getCPUt(){
        return CPUt;
    }
    
    public String getType(){
        return datatype;
    }
    
    public void useRam(float n){
        RAM-= n;
    }

       //lo que hace el computador en cada ciclo del reloj
    @Override
    public void onTick(long cycle) {
        tomar();
        try {
            ciclo = cycle;
            admitirInterno();
            revisarBloqueados();
            if (enEjecucion != null && planificador.debeSacar(enEjecucion, Listos, ciclosUsados)) {
                expropiese();
            }
            if (enEjecucion == null) {
                elegir();
            }
            if (enEjecucion == null) {
                modo = ModoCPU.SO;
                if (metricas != null) {
                    metricas.registrarCiclo(indice, false);
                }
            } else {
                if (metricas != null) {
                    metricas.registrarCiclo(indice, true);
                }
                ejecutar();
            }
            //el deadline se baja al final del ciclo, asi el proceso si aprovecha el ultimo ciclo que le queda
            revisarDeadlines();
        } finally {
            soltar();
        }
    }
 
    //revisa los bloqueados o sea los que despertaron por un semaforo o terminaron su e/s
    private void revisarBloqueados() {
        Cola<PCB> siguen = new Cola<>();
        PCB p = Bloqueados.Desencolar();
        while (p != null) {
            if (p.getEspera() > 0) {
                p.setEspera(p.getEspera() - 1);
                if (p.getEspera() == 0) {
                    p.setstate(EstadoProceso.Listo);
                    registrar("Proceso " + p.getname() + " termina su E/S");
                }
            }
            if (p.getstate() == EstadoProceso.Listo) {
                //si lo que esperaba era un semaforo del buffer, se anota el tiempo
                if (metricas != null && p.getMotivo().startsWith("semaforo")) {
                    metricas.registrarBloqueo(p.getIndiceBuffer(), ciclo - p.getCicloBloqueo());
                }
                Listos.Encolar(p);
            } else {
                siguen.Encolar(p);
            }
            p = Bloqueados.Desencolar();
        }
        moverTodos(siguen, Bloqueados);
    }
 
    //cambio de proces para el que estaba en la CPU vuelve a listos 
    private void expropiese() {
        modo = ModoCPU.SO;
        enEjecucion.setstate(EstadoProceso.Listo);
        Listos.Encolar(enEjecucion);
        registrar("Proceso " + enEjecucion.getname() + " sale de la CPU y vuelve a listos (PC = " + enEjecucion.getpc() + ")");
        enEjecucion = null;
    }
 
    //el planificador escoge quien entra a la CPU
    private void elegir() {
        modo = ModoCPU.SO;
        PCB p = planificador.elegirSiguiente(Listos);
        if (p != null) {
            p.setstate(EstadoProceso.Ejecucion);
            enEjecucion = p;
            ciclosUsados = 0;
                        if (!p.getYaEjecuto()) {
                p.setYaEjecuto(true);
                if (metricas != null) {
                    metricas.registrarRespuesta(indice, ciclo - p.getLlegada());
                }
            }
            registrar(nombre + " selecciona el proceso " + p.getname());
        }
    }
 
    //ejecuta una instruccion del proceso que esta en la CPU
    private void ejecutar() {
        PCB p = enEjecucion;
        ciclosUsados++;
                p.sumarCicloCPU();
        p.avanzarPC();
        String tipo = p.gettype();
        if (tipo.equals("Productor") || tipo.equals("Consumidor")) {
            ejecutarBuffer(p);
            return;
        }
        modo = ModoCPU.Usuario;
        p.settime(p.getime() - 1);
        if (p.getime() <= 0) {
            terminar(p, "termino sus instruciones");
            enEjecucion = null;
            return;
        }
        if (tipo.equals("I/O Bound") && p.getpc() % cadaES == 0) {
            p.setEspera(duracionES);
            p.setMotivo("E/S");
            p.setstate(EstadoProceso.Bloqueado);
            Bloqueados.Encolar(p);
            registrar("Proceso " + p.getname() + " pide E/S y se bloquea");
            enEjecucion = null;
        }
    }
 
    //un paso del productor o consumidor
    private void ejecutarBuffer(PCB p) {
        Buffer b = p.getBuffer();
        //espera sus ciclos antes de producir o modo usuario
        if (p.getPaso() == 0 && p.getContador() < p.getCadaCuantos() - 1) {
            modo = ModoCPU.Usuario;
            p.setContador(p.getContador() + 1);
            return;
        }
        //los pasos del buffer son en modo SO
        modo = ModoCPU.SO;
        int paso = p.getPaso();
        boolean seBloqueo;
        if (p.gettype().equals("Productor")) {
            seBloqueo = b.pasitoProductor(paso, p, p.getId());
        } else {
            seBloqueo = b.pasitoConsumidor(paso, p);
        }
        p.setPaso(paso + 1);
        //en el paso 2 es cuando de verdad se mete o se saca un elemento del buffer
        if (paso == 2 && metricas != null) {
            if (p.gettype().equals("Productor")) {
                metricas.registrarProducido(p.getIndiceBuffer());
            } else {
                metricas.registrarConsumido(p.getIndiceBuffer());
            }
        }
        //si ya hizo los 5 paso termino un elemento
        if (p.getPaso() == 5) {
            p.setPaso(0);
            p.setContador(0);
            p.setElementos(p.getElementos() - 1);
            if (p.getElementos() <= 0) {
                terminar(p, "termino sus elementos");
                enEjecucion = null;
                return;
            }
        }
                //si el buffer esta en otro computador espera la red
        if (paso == 2 && b.estaLejos(this) && retrasoRed > 0) {
            p.setEspera(retrasoRed);
            p.setMotivo("red (buffer " + b.getNombre() + ")");
            p.setstate(EstadoProceso.Bloqueado);
            Bloqueados.Encolar(p);
            registrar("Proceso " + p.getname() + " accede al buffer " + b.getNombre() + " de forma remota");
            enEjecucion = null;
            return;
        }
        if (seBloqueo) {
            p.setstate(EstadoProceso.Bloqueado);
            //se guarda el ciclo para saber despues cuanto estuvo esperando
            p.setCicloBloqueo(ciclo);
            Bloqueados.Encolar(p);
            String sem = "mutex";
            if (paso == 0 && p.gettype().equals("Productor")) {
                sem = "vacio";
            } else if (paso == 0) {
                sem = "lleno";
            }
            p.setMotivo("semaforo " + sem + " (buffer " + b.getNombre() + ")");
            registrar("Proceso " + p.getname() + " se bloquea en el semaforo '" + sem + "' del buffer " + b.getNombre());
            enEjecucion = null;
        }
    }
 
       //el proceso pasa a terminados y libera su ram
    private void terminar(PCB p, String motivo) {
        p.setstate(EstadoProceso.Terminado);
        Terminados.Encolar(p);
        RAM += p.getmem();
        registrar("Proceso " + p.getname() + " termina: " + motivo);
        //si era productor o consumidor y se quedo a mitad de un elemento, suelta sus semaforos
        if (p.getBuffer() != null && p.getPaso() != 0) {
            p.getBuffer().soltarSemaforos(p);
            p.setPaso(0);
            registrar("Proceso " + p.getname() + " suelta los semaforos del buffer " + p.getBuffer().getNombre());
        }
        //si tenia deadline y llego a 0, se le acabo el tiempo y no cuenta como completado
        boolean vencio = p.getTuvoDeadline() && p.getdeadline() == 0;
        if (metricas != null) {
            if (!vencio) {
                metricas.registrarTermino(indice, p.getCiclosCPU(), ciclo - p.getLlegada());
            }
            if (p.getTuvoDeadline()) {
                metricas.registrarDeadline(indice, !vencio);
            }
        }
    }
    
    
        //baja el deadline de los procesos que tienen uno y termina los que llegan a 0
    private void revisarDeadlines() {
        if (enEjecucion != null && bajarDeadline(enEjecucion)) {
            terminar(enEjecucion, "se le acabo el deadline");
            enEjecucion = null;
        }
        Cola<PCB> siguen = new Cola<>();
        PCB p = Listos.Desencolar();
        while (p != null) {
            if (bajarDeadline(p)) {
                terminar(p, "se le acabo el deadline");
            } else {
                siguen.Encolar(p);
            }
            p = Listos.Desencolar();
        }
        moverTodos(siguen, Listos);

        //de los bloqueados solo reviso los que estan en E/S
        siguen = new Cola<>();
        p = Bloqueados.Desencolar();
        while (p != null) {
            if (p.getEspera() > 0 && bajarDeadline(p)) {
                terminar(p, "se le acabo el deadline");
            } else {
                siguen.Encolar(p);
            }
            p = Bloqueados.Desencolar();
        }
        moverTodos(siguen, Bloqueados);
    }

    //le baja 1 al deadline y dice si llego a 0, si no tenia deadline (0) no hace nada
    private boolean bajarDeadline(PCB p) {
        if (p.getdeadline() <= 0) {
            return false;
        }
        p.setdeadline(p.getdeadline() - 1);
        return p.getdeadline() == 0;
    }
 
    //pasa todos los procesos de una cola a otra sin cambiar el orden
    private void moverTodos(Cola<PCB> desde, Cola<PCB> hacia) {
        PCB p = desde.Desencolar();
        while (p != null) {
            hacia.Encolar(p);
            p = desde.Desencolar();
        }
    }
 
    private void registrar(String mensaje) {
        if (log != null) {
            log.registrar((int) ciclo, mensaje);
        }
    }
}
 