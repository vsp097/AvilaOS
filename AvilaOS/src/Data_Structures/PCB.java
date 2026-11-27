/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

import avilaos.ModoUsuario;
import avilaos.Permiso;
import avilaos.TipoOperacionES;

/**
 *
 * @author diego
 */
public class PCB {
    
    //datos del proceso de E/S: que operacion pide y sobre que archivo o directorio
    protected TipoOperacionES operacion;
    protected String ruta = "";
    protected boolean esDirectorio = false;
    protected int tamanoArchivo = 0;
    protected String nuevoNombre = "";
    protected Permiso permisoArchivo = Permiso.PUBLICO;
    protected String duenoArchivo = "";
    //quien lanzo el proceso y en que modo estaba
    protected String usuario = "admin";
    protected ModoUsuario modoUsuario = ModoUsuario.ADMINISTRADOR;
    //true cuando ya le mando su solicitud al disco
    protected boolean solicitudHecha = false;
    //true si viene del archivo de procesos y el disco lo esta esperando
    protected boolean deCargaInicial = false;
    //donde quedo en el area de intercambio cuando esta suspendido, -1 si esta en la RAM
    protected int primerSwap = -1;
    protected int bloquesSwap = 0;
    //true mientras tiene una solicitud SUSPENDER o REACTIVAR en el disco
    protected boolean enIntercambio = false;
    //datos para las metricas
    protected long llegada = 0;
    protected boolean tuvoDeadline = false;
    protected boolean yaEjecuto = false;
    protected int ciclosCPU = 0;
    protected long cicloBloqueo = 0;
    private int ID;
    protected String name;
    protected int pc;
    protected EstadoProceso state = EstadoProceso.Nuevo;
    protected TipoProceso type;
    protected float mem;
    protected int deadline;
    protected float time;
    protected int prioridad;
    //nombre del computador donde corre el proceso, no cambia porque no hay migracion
    protected String computador = "";
    
    public PCB(int id, String n, int p, TipoProceso t, float m){
    ID = id;
    name =n;
    pc=p;
    type=t;
    mem=m;
    deadline= 0;
    time=0;
    }
    
    public int getId(){
        return ID;
    }
    
    public String getname(){
        return name;
    }
    
    public int getpc(){
        return pc;
    }

    public EstadoProceso getstate(){
        return state;
    }
    
    public TipoProceso gettype(){
        return type;
    }

    public float getmem(){
        return mem;
    }
    
    public int getdeadline(){
        return deadline;
    }
    
    public float getime(){
        return time;
    }
    
    public void setstate(EstadoProceso x){
        state = x;
    }
    
    public void settime(float t){
        time =t;
    }
    
    public void setdeadline(int d){
        deadline = d;
    }
        public int getPrioridad(){
        return prioridad;
    }

    public void setPrioridad(int p){
        prioridad = p;
    }
    
    public String getComputador() {
        return computador;
    }

    public void setComputador(String c) {
        computador = c;
    }

    public void avanzarPC() {
        pc++;
    }

    public long getLlegada() {
        return llegada;
    }

    public void setLlegada(long l) {
        llegada = l;
    }

    public boolean getTuvoDeadline() {
        return tuvoDeadline;
    }

    public void setTuvoDeadline(boolean t) {
        tuvoDeadline = t;
    }

    public boolean getYaEjecuto() {
        return yaEjecuto;
    }

    public void setYaEjecuto(boolean y) {
        yaEjecuto = y;
    }

    public int getCiclosCPU() {
        return ciclosCPU;
    }

    public void sumarCicloCPU() {
        ciclosCPU++;
    }

    public long getCicloBloqueo() {
        return cicloBloqueo;
    }

    public void setCicloBloqueo(long c) {
        cicloBloqueo = c;
    }
    //por que esta bloqueado
    protected MotivoBloqueo motivo = MotivoBloqueo.NINGUNO;

    public MotivoBloqueo getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoBloqueo m) {
        motivo = m;
    }

    public TipoOperacionES getOperacion() {
        return operacion;
    }

    public void setOperacion(TipoOperacionES o) {
        operacion = o;
    }

    public String getRuta() {
        return ruta;
    }

    public void setRuta(String r) {
        ruta = r;
    }

    public boolean getEsDirectorio() {
        return esDirectorio;
    }

    public void setEsDirectorio(boolean d) {
        esDirectorio = d;
    }

    public int getTamanoArchivo() {
        return tamanoArchivo;
    }

    public void setTamanoArchivo(int t) {
        tamanoArchivo = t;
    }

    public String getNuevoNombre() {
        return nuevoNombre;
    }

    public void setNuevoNombre(String n) {
        nuevoNombre = n;
    }

    public Permiso getPermisoArchivo() {
        return permisoArchivo;
    }

    public void setPermisoArchivo(Permiso p) {
        permisoArchivo = p;
    }

    public String getDuenoArchivo() {
        return duenoArchivo;
    }

    public void setDuenoArchivo(String d) {
        duenoArchivo = d;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String u) {
        usuario = u;
    }

    public ModoUsuario getModoUsuario() {
        return modoUsuario;
    }

    public void setModoUsuario(ModoUsuario m) {
        modoUsuario = m;
    }

    public boolean getSolicitudHecha() {
        return solicitudHecha;
    }

    public void setSolicitudHecha(boolean s) {
        solicitudHecha = s;
    }

    public boolean getDeCargaInicial() {
        return deCargaInicial;
    }

    public void setDeCargaInicial(boolean d) {
        deCargaInicial = d;
    }

    //lo que se ve en las colas y en el log: la operacion con su ruta
    public String textoOperacion() {
        if (operacion == null) {
            return "";
        }
        return operacion + " " + ruta;
    }

    public int getPrimerSwap() {
        return primerSwap;
    }

    public void setPrimerSwap(int b) {
        primerSwap = b;
    }

    public int getBloquesSwap() {
        return bloquesSwap;
    }

    public void setBloquesSwap(int b) {
        bloquesSwap = b;
    }

    public boolean getEnIntercambio() {
        return enIntercambio;
    }

    public void setEnIntercambio(boolean e) {
        enIntercambio = e;
    }
}
