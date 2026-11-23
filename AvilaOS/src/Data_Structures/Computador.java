/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;
import avilaos.ClockListener;
import avilaos.FCFS;
import avilaos.Planificador;
import avilaos.LogEventos;
import avilaos.Metricas;
import avilaos.ServicioES;
import avilaos.ServicioIntercambio;
import avilaos.PlanificadorMedianoPlazo;
import avilaos.MedianoPlazoAntiguedad;
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
    //las dos colas de procesos suspendidos (estan en el area de intercambio del disco)
    protected Cola<PCB> ListosSuspendidos = new Cola<>();
    protected Cola<PCB> BloqueadosSuspendidos = new Cola<>();
    protected PCB enEjecucion;
    protected ModoCPU modo = ModoCPU.Usuario;
    protected String nombre = "Computador";
    protected LogEventos log;
    protected int ciclosUsados = 0;
    protected long ciclo = 0;
    //el sistema operativo, que es quien arma las solicitudes para el disco
    protected ServicioES servicioES;

    public void setServicioES(ServicioES s) {
        servicioES = s;
    }

    //----- mediano plazo -----
    //el sistema operativo, que arma las solicitudes SUSPENDER y REACTIVAR
    protected ServicioIntercambio intercambio;
    protected PlanificadorMedianoPlazo medianoPlazo = new MedianoPlazoAntiguedad();
    //RAM que se va a liberar cuando el disco termine las suspensiones que ya se pidieron
    protected float ramEnCamino = 0;
    //true cuando un proceso termino y solto su RAM, asi el mediano plazo revisa a los listos/suspendidos
    protected boolean seLiberoMemoria = false;
    //para no repetir en el log el mismo aviso en cada ciclo
    private String ultimoAviso = "";

    public void setServicioIntercambio(ServicioIntercambio s) {
        intercambio = s;
    }

    public void setMedianoPlazo(PlanificadorMedianoPlazo m) {
        medianoPlazo = m;
    }

        //las metricas del sistema (una sola para todos) y el numero de este computador (0 es el primero)
    protected Metricas metricas;
    protected int indice = 0;

    public void setMetricas(Metricas m, int i) {
        metricas = m;
        indice = i;
    }

    public Computador(){
        CPU = null;
        RAM = 0;
        CPUt = null;
        RAMt = 0;   
        datatype = null;
    }
    
    public long getCiclo() {
        return ciclo;
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
        //solo el primero que no cabe le pide espacio al mediano plazo, los demas esperan su turno
        boolean pidioEspacio = false;
        PCB p = Nuevos.Desencolar();
        while (p != null) {
            if (p.getmem() <= RAM) {
                useRam(p.getmem());
                p.setstate(EstadoProceso.Listo);
                Listos.Encolar(p);
                registrar("Proceso " + p.getname() + " es admitido y pasa a listos");
            } else {
                if (!pidioEspacio) {
                    pidioEspacio = true;
                    hacerEspacio(p);
                }
                siguen.Encolar(p);
            }
            p = Nuevos.Desencolar();
        }
        moverTodos(siguen, Nuevos);
    }

    //el planificador a mediano plazo suspende bloqueados hasta que el nuevo quepa
    //la RAM se libera cuando el disco termina de escribirlos en el area de intercambio
    private void hacerEspacio(PCB nuevo) {
        if (intercambio == null) {
            return;
        }
        float falta = nuevo.getmem() - RAM - ramEnCamino;
        if (falta <= 0) {
            //ya hay suspensiones en camino que le van a dejar el espacio
            return;
        }
        Cola<PCB> candidatos = candidatosASuspender();
        float posible = 0;
        for (int i = 0; i < candidatos.len(); i++) {
            posible += candidatos.ver(i).getmem();
        }
        if (posible < falta) {
            //no se suspende a nadie si igual no va a caber
            avisarUnaVez("No hay procesos bloqueados que se puedan suspender para admitir a " + nuevo.getname()
                    + " (necesita " + falta + " MB mas)");
            return;
        }
        while (falta > 0) {
            PCB v = medianoPlazo.elegirParaSuspender(candidatos);
            if (v == null) {
                return;
            }
            candidatos.eliminar(v);
            String error = intercambio.suspender(v, ciclo);
            if (error != null) {
                avisarUnaVez("No se puede suspender a " + v.getname() + " para admitir a " + nuevo.getname() + ": " + error);
                return;
            }
            v.setMotivo(MotivoBloqueo.SUSPENSION);
            v.setEnIntercambio(true);
            ramEnCamino += v.getmem();
            falta -= v.getmem();
            registrar("Se suspende al proceso " + v.getname() + " para admitir a " + nuevo.getname()
                    + " (" + v.getBloquesSwap() + " bloques al area de intercambio)");
        }
    }

    //los bloqueados cuya solicitud todavia espera en la cola del disco
    //el que el disco esta atendiendo no se toca, asi un proceso nunca tiene dos solicitudes en el disco
    private Cola<PCB> candidatosASuspender() {
        Cola<PCB> candidatos = new Cola<>();
        for (int i = 0; i < Bloqueados.len(); i++) {
            PCB p = Bloqueados.ver(i);
            if (p.getMotivo() == MotivoBloqueo.ES_DISCO && intercambio.puedeSuspender(p)) {
                candidatos.Encolar(p);
            }
        }
        return candidatos;
    }

    //cuando se libera RAM (un proceso termino), los listos/suspendidos van antes que los nuevos:
    //ya fueron admitidos una vez y si no se les da prioridad se pueden quedar afuera para siempre
    //sin RAM liberada solo vuelven si no hay nuevos esperando, asi no le quitan el espacio al que provoco la suspension
    private void revisarReactivacion() {
        boolean puede = seLiberoMemoria || Nuevos.estaVacia();
        seLiberoMemoria = false;
        if (intercambio == null || !puede || ListosSuspendidos.estaVacia()) {
            return;
        }
        while (true) {
            Cola<PCB> caben = new Cola<>();
            for (int i = 0; i < ListosSuspendidos.len(); i++) {
                PCB p = ListosSuspendidos.ver(i);
                if (!p.getEnIntercambio() && p.getmem() <= RAM) {
                    caben.Encolar(p);
                }
            }
            PCB p = medianoPlazo.elegirParaReactivar(caben);
            if (p == null) {
                return;
            }
            //la RAM se aparta de una vez para que ningun nuevo la tome mientras el disco lee el area de intercambio
            useRam(p.getmem());
            p.setEnIntercambio(true);
            intercambio.reactivar(p, ciclo);
            registrar("Se reactiva al proceso " + p.getname() + ": hay " + (RAM + p.getmem()) + " MB libres");
        }
    }

    //el disco termino de escribir al proceso en el area de intercambio: suelta su RAM
    public void suspensionLista(PCB p) {
        tomar();
        try {
            if (!Bloqueados.eliminar(p)) {
                return;
            }
            RAM += p.getmem();
            ramEnCamino = Math.max(0, ramEnCamino - p.getmem());
            p.setEnIntercambio(false);
            p.setMotivo(MotivoBloqueo.ES_DISCO);
            p.setstate(EstadoProceso.BloqueadoSuspendido);
            BloqueadosSuspendidos.Encolar(p);
            if (metricas != null) {
                metricas.registrarSuspension();
            }
            registrar("Proceso " + p.getname() + " queda bloqueado/suspendido en los bloques " + p.getPrimerSwap()
                    + " a " + (p.getPrimerSwap() + p.getBloquesSwap() - 1) + " y libera " + p.getmem() + " MB");
        } finally {
            soltar();
        }
    }

    //el disco termino de leer al proceso del area de intercambio: ya tiene su RAM y pasa a listos
    public void reactivacionLista(PCB p) {
        tomar();
        try {
            if (!ListosSuspendidos.eliminar(p)) {
                return;
            }
            p.setEnIntercambio(false);
            p.setPrimerSwap(-1);
            p.setBloquesSwap(0);
            if (metricas != null) {
                metricas.registrarReactivacion();
            }
            registrar("Proceso " + p.getname() + " vuelve a la RAM y pasa a listos");
            if (p.getTuvoDeadline() && p.getdeadline() == 0) {
                terminar(p, "se le acabo el deadline");
            } else {
                p.setstate(EstadoProceso.Listo);
                Listos.Encolar(p);
            }
        } finally {
            soltar();
        }
    }

    //termina un proceso que esta en el area de intercambio: suelta sus bloques y no tiene RAM que devolver
    private void terminarSuspendido(PCB p, String motivo) {
        if (intercambio != null) {
            intercambio.liberarSwap(p);
        }
        p.setstate(EstadoProceso.Terminado);
        Terminados.Encolar(p);
        registrar("Proceso " + p.getname() + " termina estando suspendido: " + motivo);
        anotarTermino(p);
    }

    private void avisarUnaVez(String mensaje) {
        if (!mensaje.equals(ultimoAviso)) {
            ultimoAviso = mensaje;
            registrar(mensaje);
        }
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

    public Cola<PCB> getListosSuspendidos() {
        return ListosSuspendidos;
    }

    public Cola<PCB> getBloqueadosSuspendidos() {
        return BloqueadosSuspendidos;
    }

    //copia de todas las colas para que la ventana las muestre, una por cada estado
    //va en el mismo orden que el enum EstadoProceso
    public PCB[][] copiarColas() {
        tomar();
        try {
            PCB[][] copia = new PCB[EstadoProceso.values().length][];
            copia[EstadoProceso.Nuevo.ordinal()] = copiar(Nuevos);
            copia[EstadoProceso.Listo.ordinal()] = copiar(Listos);
            copia[EstadoProceso.Ejecucion.ordinal()] = enEjecucion == null ? new PCB[0] : new PCB[]{enEjecucion};
            copia[EstadoProceso.Bloqueado.ordinal()] = copiar(Bloqueados);
            copia[EstadoProceso.ListoSuspendido.ordinal()] = copiar(ListosSuspendidos);
            copia[EstadoProceso.BloqueadoSuspendido.ordinal()] = copiar(BloqueadosSuspendidos);
            copia[EstadoProceso.Terminado.ordinal()] = copiar(Terminados);
            return copia;
        } finally {
            soltar();
        }
    }

    private PCB[] copiar(Cola<PCB> cola) {
        PCB[] arreglo = new PCB[cola.len()];
        for (int i = 0; i < arreglo.length; i++) {
            arreglo[i] = cola.ver(i);
        }
        return arreglo;
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
        boolean ocioso;
        tomar();
        try {
            ciclo = cycle;
            revisarReactivacion();
            admitirInterno();
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
            ocioso = enEjecucion == null && Listos.estaVacia();
        } finally {
            soltar();
        }
        //se avisa ya sin el candado, para no tener dos semaforos tomados a la vez
        if (ocioso && servicioES != null) {
            servicioES.cpuOciosa();
        }
    }
 
    //el disco termino la solicitud de este proceso: sale de bloqueados y vuelve a listos
    public void despertar(PCB p) {
        tomar();
        try {
            //si estaba suspendido su evento ya ocurrio: pasa a listo/suspendido, sigue en el area de intercambio
            if (BloqueadosSuspendidos.eliminar(p)) {
                p.setMotivo(MotivoBloqueo.NINGUNO);
                registrar("Proceso " + p.getname() + " termina su E/S (" + p.textoOperacion() + ") estando suspendido");
                if (p.getTuvoDeadline() && p.getdeadline() == 0) {
                    terminarSuspendido(p, "se le acabo el deadline");
                } else if (p.getime() <= 0) {
                    terminarSuspendido(p, "termino sus instruciones");
                } else {
                    p.setstate(EstadoProceso.ListoSuspendido);
                    ListosSuspendidos.Encolar(p);
                    registrar("Proceso " + p.getname() + " pasa a listo/suspendido");
                }
                return;
            }
            if (!Bloqueados.eliminar(p)) {
                return;
            }
            p.setMotivo(MotivoBloqueo.NINGUNO);
            registrar("Proceso " + p.getname() + " termina su E/S (" + p.textoOperacion() + ")");
            if (p.getTuvoDeadline() && p.getdeadline() == 0) {
                terminar(p, "se le acabo el deadline");
            } else if (p.getime() <= 0) {
                terminar(p, "termino sus instruciones");
            } else {
                p.setstate(EstadoProceso.Listo);
                Listos.Encolar(p);
            }
        } finally {
            soltar();
        }
    }

    //la solicitud no se pudo completar (por ejemplo el archivo ya no existia): el proceso termina con error
    public void fallar(PCB p, String motivo) {
        tomar();
        try {
            if (BloqueadosSuspendidos.eliminar(p)) {
                p.setMotivo(MotivoBloqueo.NINGUNO);
                terminarSuspendido(p, "fallo su E/S: " + motivo);
                return;
            }
            if (!Bloqueados.eliminar(p)) {
                return;
            }
            p.setMotivo(MotivoBloqueo.NINGUNO);
            terminar(p, "fallo su E/S: " + motivo);
        } finally {
            soltar();
        }
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
        //lo primero que hace un proceso de E/S es pedirle su operacion al disco
        //esa instruccion se ejecuta en modo sistema operativo
        if (p.gettype() == TipoProceso.ES && !p.getSolicitudHecha()) {
            modo = ModoCPU.SO;
            p.setSolicitudHecha(true);
            p.settime(p.getime() - 1);
            String error = servicioES == null ? "no hay disco" : servicioES.solicitar(p, ciclo);
            if (error != null) {
                terminar(p, "fallo su E/S: " + error);
            } else {
                p.setMotivo(MotivoBloqueo.ES_DISCO);
                p.setstate(EstadoProceso.Bloqueado);
                p.setCicloBloqueo(ciclo);
                Bloqueados.Encolar(p);
                registrar("Proceso " + p.getname() + " pide " + p.textoOperacion() + " y se bloquea por E/S");
            }
            enEjecucion = null;
            return;
        }
        modo = ModoCPU.Usuario;
        p.settime(p.getime() - 1);
        if (p.getime() <= 0) {
            terminar(p, "termino sus instruciones");
            enEjecucion = null;
        }
    }

       //el proceso pasa a terminados y libera su ram
    private void terminar(PCB p, String motivo) {
        p.setstate(EstadoProceso.Terminado);
        Terminados.Encolar(p);
        RAM += p.getmem();
        seLiberoMemoria = true;
        registrar("Proceso " + p.getname() + " termina: " + motivo);
        anotarTermino(p);
    }

    //si tenia deadline y llego a 0, se le acabo el tiempo y no cuenta como completado
    private void anotarTermino(PCB p) {
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

        //a los bloqueados tambien les corre el deadline, pero no se sacan a mitad de su E/S:
        //si se les acaba, terminan cuando el disco los despierta
        for (int i = 0; i < Bloqueados.len(); i++) {
            bajarDeadline(Bloqueados.ver(i));
        }
        for (int i = 0; i < BloqueadosSuspendidos.len(); i++) {
            bajarDeadline(BloqueadosSuspendidos.ver(i));
        }
        //un listo/suspendido que se queda sin deadline termina ahi mismo y suelta el area de intercambio,
        //salvo que ya este volviendo a la RAM: ese se revisa cuando llegue a listos
        siguen = new Cola<>();
        p = ListosSuspendidos.Desencolar();
        while (p != null) {
            if (bajarDeadline(p) && !p.getEnIntercambio()) {
                terminarSuspendido(p, "se le acabo el deadline");
            } else {
                siguen.Encolar(p);
            }
            p = ListosSuspendidos.Desencolar();
        }
        moverTodos(siguen, ListosSuspendidos);
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
 