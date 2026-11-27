/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//lo que llena el usuario en el formulario para crear un proceso
//es solo un paquete de datos, por eso los campos son publicos
public class DatosProceso {
    public String nombre;
    public float memoria;
    public int instrucciones;
    public int prioridad = 1;
    public int deadline;
    //null si el proceso no hace E/S
    public TipoOperacionES operacion;
    public String ruta;
    //solo para CREAR
    public boolean esDirectorio;
    public int tamano;
    public Permiso permiso = Permiso.PUBLICO;
    public String dueno;
    //solo para ACTUALIZAR, vacio si no se cambia el nombre
    public String nuevoNombre = "";
}
