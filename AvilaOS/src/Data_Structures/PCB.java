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
    private int ID;
    protected String name;
    protected int pc;
    protected EstadoProceso state = EstadoProceso.Nuevo;
    protected String type;
    protected float mem;
    protected int deadline;
    protected float time;
        //el numero menor es mas importante
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
}
