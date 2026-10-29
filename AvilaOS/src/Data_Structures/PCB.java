/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

/**
 *
 * @author diego
 */
public class PCB {
    
        //datos para ejecutar el proceso
    protected int paso = 0;
    protected avilaos.Buffer buffer;
    protected int elementos;
    protected int cadaCuantos = 1;
    protected int contador = 0;
    protected int espera = 0;
    //datos para las metricas
    protected long llegada = 0;
    protected boolean tuvoDeadline = false;
    protected boolean yaEjecuto = false;
    protected int ciclosCPU = 0;
    protected int indiceBuffer = -1;
    protected long cicloBloqueo = 0;
    private int ID;
    protected String name;
    protected int pc;
    protected EstadoProceso state = EstadoProceso.Nuevo;
    protected String type;
    protected float mem;
    protected int deadline;
    protected float time;
    protected int prioridad;
    
    public PCB(int id, String n, int p, String t, float m){
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
    
    public String gettype(){
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
    
    public void avanzarPC() {
        pc++;
    }

    public int getPaso() {
        return paso;
    }

    public void setPaso(int p) {
        paso = p;
    }

    public avilaos.Buffer getBuffer() {
        return buffer;
    }

    public void setBuffer(avilaos.Buffer b) {
        buffer = b;
    }

    public int getElementos() {
        return elementos;
    }

    public void setElementos(int e) {
        elementos = e;
    }

    public int getCadaCuantos() {
        return cadaCuantos;
    }

    public void setCadaCuantos(int c) {
        cadaCuantos = c;
    }

    public int getContador() {
        return contador;
    }

    public void setContador(int c) {
        contador = c;
    }

    public int getEspera() {
        return espera;
    }

    public void setEspera(int e) {
        espera = e;
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

    public int getIndiceBuffer() {
        return indiceBuffer;
    }

    public void setIndiceBuffer(int i) {
        indiceBuffer = i;
    }

    public long getCicloBloqueo() {
        return cicloBloqueo;
    }

    public void setCicloBloqueo(long c) {
        cicloBloqueo = c;
    }
    //si es e/s o red o semaofro
    protected String motivo = "";

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String m) {
        motivo = m;
    }
}
