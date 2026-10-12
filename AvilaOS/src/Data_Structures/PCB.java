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
    protected String pc;
    protected String state;
    protected String type;
    protected String mem;
    protected String deadline;
    protected String time;
    
    public PCB(int id, String n, String p, String s, String t, String m){
    ID = id;
    name =n;
    pc=p;
    state=s;
    type=t;
    mem=m;
    deadline= null;
    time=null;
    }
    
    int getId(){
        return ID;
    }
    
    String getname(){
        return name;
    }
    
    String getpc(){
        return pc;
    }

    String getstate(){
        return state;
    }

    String gettype(){
        return type;
    }

    String getmem(){
        return mem;
    }
    
    String getdeadline(){
        return deadline;
    }
    
    String getime(){
        return time;
    }
    
    void settime(String t){
        time =t;
    }
    
    void setdeadline(String d){
        deadline = d;
    }
}
