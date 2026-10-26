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
    
        public synchronized void admitir() {
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
    
    public int getPC() {
        if (enEjecucion == null) {
            return 0;
        }
        return enEjecucion.getpc();
    }

    public void agregarProceso(PCB p) {
        p.setstate(EstadoProceso.Nuevo);
        Nuevos.Encolar(p);
        registrar("Proceso " + p.getname() + " llega a la cola de nuevos");
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

      @Override
    public void onTick(long cycle) {
        ciclo = cycle;
        admitir();
        revisarBloqueados();
        if (enEjecucion != null && planificador.debeSacar(enEjecucion, Listos, ciclosUsados)) {
            expropiese();
        }
        if (enEjecucion == null) {
            elegir();
        }
        if (enEjecucion == null) {
            modo = ModoCPU.SO;
            return;
        }
        ejecutar();
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
            registrar(nombre + " selecciona el proceso " + p.getname());
        }
    }
 
    //ejecuta una instruccion del proceso que esta en la CPU
    private void ejecutar() {
        PCB p = enEjecucion;
        ciclosUsados++;
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
        if (seBloqueo) {
            p.setstate(EstadoProceso.Bloqueado);
            Bloqueados.Encolar(p);
            registrar("Proceso " + p.getname() + " se bloquea en el buffer " + b.getNombre());
            enEjecucion = null;
        }
    }
 
    //el proceso pasa a terminados y libera su ram
    private void terminar(PCB p, String motivo) {
        p.setstate(EstadoProceso.Terminado);
        Terminados.Encolar(p);
        RAM += p.getmem();
        registrar("Proceso " + p.getname() + " termina: " + motivo);
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
 