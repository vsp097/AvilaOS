/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Data_Structures;

import avilaos.ClockListener;
import avilaos.FCFS;
import avilaos.Planificador;

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
    protected Cola<PCB> Nuevos, Listos, Bloqueados, Terminados = new Cola<PCB>();
    protected PCB enEjecucion;

    public PCB getEnEjecucion() {
        return enEjecucion;
    }
    
    protected Planificador planificador = new FCFS();

    public void setPlanificador(Planificador p) {
        planificador = p;
    }

    public Planificador getPlanificador() {
        return planificador;
    }
    protected String modo;
    //semaforo
    
    public Computador(){
        CPU = null;
        RAM = 0;
        CPUt = null;
        RAMt = 0;   
        datatype = null;
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
    
    public void setmode(String m){
        modo = m;
    }
    
    public String getmode(){
        return modo;
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
        
    }
    
}
