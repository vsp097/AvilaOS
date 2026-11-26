/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.DatosProceso;
import avilaos.ModoUsuario;
import avilaos.Permiso;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import avilaos.Validaciones;

/**
 *
 * @author diego
 */
//prueba por consola de los permisos: en modo usuario intenta operaciones sobre archivos propios,
//publicos y ajenos, y dice cuales se aceptan y cuales se rechazan con su motivo
public class PruebaPermisos {

    private static int aceptadas = 0;
    private static int rechazadas = 0;
    private static int errores = 0;
    private static int siguiente = 1;

    public static void main(String[] args) throws Exception {
        String carpeta = args.length > 0 ? args[0] : "datos";
        ConfigArchivos ca = ArchivosJSON.cargarArchivos(carpeta + "/sistema_archivos_grande.json");
        ConfigProcesos cp = ArchivosJSON.cargarProcesos(carpeta + "/procesos_permisos.json");
        SistemaOperativo so = new SistemaOperativo(ca, cp);

        System.out.println();
        System.out.println("===== Modo usuario: user1 =====");
        so.setModoUsuario(ModoUsuario.USUARIO, "user1");
        //{operacion, ruta, que se espera}
        probar(so, TipoOperacionES.LEER, "/usuarios/user1/tesis.docx", false, "", true, "leer un archivo propio y privado");
        probar(so, TipoOperacionES.LEER, "/usuarios/user2/fotos.zip", false, "", true, "leer un archivo ajeno y publico");
        probar(so, TipoOperacionES.LEER, "/sistema/kernel.bin", false, "", true, "leer un archivo del sistema publico");
        probar(so, TipoOperacionES.LEER, "/usuarios/user2/claves.txt", false, "", false, "leer un archivo ajeno y privado");
        probar(so, TipoOperacionES.LEER, "/sistema/log.txt", false, "", false, "leer un archivo del sistema privado");
        probar(so, TipoOperacionES.CREAR, "/usuarios/user1/nuevo.txt", false, "", true, "crear en su propio directorio");
        probar(so, TipoOperacionES.CREAR, "/usuarios/user2/intruso.txt", false, "", false, "crear en un directorio ajeno");
        probar(so, TipoOperacionES.CREAR, "/sistema/virus.exe", false, "", false, "crear en un directorio del sistema");
        probar(so, TipoOperacionES.CREAR, "/usuarios/user1/carpeta", true, "", false, "crear un directorio");
        probar(so, TipoOperacionES.ACTUALIZAR, "/usuarios/user1/musica.mp3", false, "cancion.mp3", true, "renombrar un archivo propio");
        probar(so, TipoOperacionES.ACTUALIZAR, "/usuarios/user2/fotos.zip", false, "mias.zip", false, "renombrar un archivo ajeno publico");
        probar(so, TipoOperacionES.ACTUALIZAR, "/usuarios/user1", false, "yo", false, "renombrar un directorio propio");
        probar(so, TipoOperacionES.ELIMINAR, "/usuarios/user1/tesis.docx", false, "", true, "eliminar un archivo propio");
        probar(so, TipoOperacionES.ELIMINAR, "/usuarios/compartido.csv", false, "", false, "eliminar un archivo publico del admin");
        probar(so, TipoOperacionES.ELIMINAR, "/sistema", false, "", false, "eliminar un directorio del sistema");
        probar(so, TipoOperacionES.LEER, "/usuarios/user1/no_existe.txt", false, "", false, "leer algo que no existe");

        System.out.println();
        System.out.println("===== Modo administrador: lo mismo que se le rechazo al usuario =====");
        so.setModoUsuario(ModoUsuario.ADMINISTRADOR, "admin");
        probar(so, TipoOperacionES.LEER, "/usuarios/user2/claves.txt", false, "", true, "leer un archivo privado de user2");
        probar(so, TipoOperacionES.CREAR, "/usuarios/user1/carpeta", true, "", true, "crear un directorio");
        probar(so, TipoOperacionES.ACTUALIZAR, "/usuarios/user2/fotos.zip", false, "fotos2.zip", true, "renombrar un archivo ajeno");
        probar(so, TipoOperacionES.ELIMINAR, "/usuarios/compartido.csv", false, "", true, "eliminar un archivo");

        System.out.println();
        System.out.println("===== Validaciones de los campos del formulario =====");
        validar("Memoria", "abc", true);
        validar("Memoria", "-5", true);
        validar("Memoria", "16.5", true);
        validar("Instrucciones", "2.5", false);
        validar("Instrucciones", "99999999999", false);
        validar("Instrucciones", "", false);
        validar("Prioridad", "11", false);
        validar("Prioridad", "3", false);
        texto("Nombre", "   ");
        texto("Nombre", "<b>P1</b>");
        texto("Nombre", "Proceso_con_un_nombre_demasiado_largo");
        texto("Nombre", "P1");

        //los procesos aceptados se ejecutan de verdad: la CPU los corre y el disco los atiende
        long ciclo = 0;
        int total = so.getComputador().getNuevos().len() + so.getComputador().getListos().len();
        while (ciclo < 100000) {
            ciclo++;
            so.getComputador().onTick(ciclo);
            so.getDisco().onTick(ciclo);
            if (so.getComputador().getTerminados().len() >= total && so.getDisco().estaOcioso()) {
                break;
            }
        }
        System.out.println();
        System.out.println("===== Resumen de los permisos =====");
        System.out.println("Como se esperaba: " + (aceptadas + rechazadas) + " (" + aceptadas + " aceptadas, " + rechazadas
                + " rechazadas) | distinto de lo esperado: " + errores);
        System.out.println("Procesos ejecutados: " + so.getComputador().getTerminados().len() + " en " + ciclo + " ciclos");
    }

    //intenta crear el proceso de E/S y compara con lo que se esperaba
    private static void probar(SistemaOperativo so, TipoOperacionES op, String ruta, boolean directorio, String nuevoNombre,
            boolean seEspera, String descripcion) {
        DatosProceso d = new DatosProceso();
        d.nombre = "T" + (siguiente++);
        d.memoria = 8;
        d.instrucciones = 2;
        d.operacion = op;
        d.ruta = ruta;
        d.esDirectorio = directorio;
        d.tamano = directorio ? 0 : 3;
        d.permiso = Permiso.PUBLICO;
        d.nuevoNombre = nuevoNombre;
        String resultado;
        boolean acepto;
        try {
            so.crearProceso(d);
            acepto = true;
            resultado = "ACEPTADA";
        } catch (IllegalArgumentException e) {
            acepto = false;
            resultado = "RECHAZADA: " + e.getMessage();
        }
        boolean bien = acepto == seEspera;
        if (!bien) {
            errores++;
        } else if (acepto) {
            aceptadas++;
        } else {
            rechazadas++;
        }
        System.out.println((bien ? "[ok] " : "[!!] ") + op + " " + ruta + " (" + descripcion + ") -> " + resultado);
    }

    private static void validar(String campo, String valor, boolean decimal) {
        try {
            if (decimal) {
                System.out.println("[ok] " + campo + " = '" + valor + "' -> aceptado: "
                        + Validaciones.leerDecimal(valor, campo, 1, 1000000));
            } else {
                int max = campo.equals("Prioridad") ? 10 : 100000;
                System.out.println("[ok] " + campo + " = '" + valor + "' -> aceptado: "
                        + Validaciones.leerEntero(valor, campo, 1, max));
            }
        } catch (IllegalArgumentException e) {
            System.out.println("[ok] " + campo + " = '" + valor + "' -> rechazado: " + e.getMessage());
        }
    }

    private static void texto(String campo, String valor) {
        try {
            System.out.println("[ok] " + campo + " = '" + valor + "' -> aceptado: " + Validaciones.leerTexto(valor, campo, 30));
        } catch (IllegalArgumentException e) {
            System.out.println("[ok] " + campo + " = '" + valor + "' -> rechazado: " + e.getMessage());
        }
    }
}
