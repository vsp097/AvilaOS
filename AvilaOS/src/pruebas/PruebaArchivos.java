/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import avilaos.ArchivosJSON;
import avilaos.Bloque;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.DatosProceso;
import avilaos.InfoNodo;
import avilaos.Permiso;
import avilaos.SistemaOperativo;
import avilaos.TipoOperacionES;
import java.io.File;

/**
 *
 * @author diego
 */
//prueba por consola del sistema de archivos: crear archivos de varios bloques, renombrar, eliminar,
//guardar el estado en json, volver a cargarlo y comprobar que quedo igual
public class PruebaArchivos {

    private static int fallas = 0;

    public static void main(String[] args) throws Exception {
        String carpeta = args.length > 0 ? args[0] : "datos";
        ConfigArchivos ca = ArchivosJSON.cargarArchivos(carpeta + "/sistema_archivos_grande.json");
        ConfigProcesos cp = ArchivosJSON.cargarProcesos(carpeta + "/procesos_permisos.json");
        SistemaOperativo so = new SistemaOperativo(ca, cp);
        int libresAntes = so.getDisco().getLibres();
        System.out.println("Bloques libres al empezar: " + libresAntes);

        //1. crear dos archivos de varios bloques
        pedir(so, TipoOperacionES.CREAR, "/usuarios/user1/informe.pdf", 7, "");
        pedir(so, TipoOperacionES.CREAR, "/usuarios/user1/borrador.txt", 5, "");
        correr(so);
        InfoNodo informe = buscar(so, "/usuarios/user1/informe.pdf");
        comprobar(informe != null && informe.bloques == 7, "informe.pdf existe con 7 bloques");
        comprobar(so.getDisco().getLibres() == libresAntes - 12, "se ocuparon 12 bloques");
        if (informe != null) {
            int[] cadena = so.getDisco().cadena(informe.primerBloque);
            comprobar(cadena.length == 7, "la cadena de informe.pdf tiene 7 enlaces: " + texto(cadena));
            Bloque[] bloques = so.getDisco().copiarBloques();
            comprobar(bloques[cadena[cadena.length - 1]].getSiguiente() == -1, "el ultimo bloque de la cadena apunta a -1");
        }

        //2. renombrar uno
        pedir(so, TipoOperacionES.ACTUALIZAR, "/usuarios/user1/borrador.txt", 0, "final.txt");
        correr(so);
        InfoNodo fin = buscar(so, "/usuarios/user1/final.txt");
        comprobar(buscar(so, "/usuarios/user1/borrador.txt") == null && fin != null, "borrador.txt ahora se llama final.txt");
        if (fin != null) {
            Bloque[] bloques = so.getDisco().copiarBloques();
            comprobar(bloques[fin.primerBloque].getOcupante().equals("/usuarios/user1/final.txt"),
                    "los bloques muestran el nombre nuevo");
        }

        //3. eliminar el otro y un directorio con todo adentro
        pedir(so, TipoOperacionES.ELIMINAR, "/usuarios/user1/informe.pdf", 0, "");
        pedir(so, TipoOperacionES.ELIMINAR, "/usuarios/user2", 0, "");
        correr(so);
        comprobar(buscar(so, "/usuarios/user1/informe.pdf") == null, "informe.pdf ya no existe");
        comprobar(buscar(so, "/usuarios/user2/fotos.zip") == null, "al borrar /usuarios/user2 se borro fotos.zip");
        //se liberan los 7 de informe.pdf y los 9 + 2 de lo que habia en /usuarios/user2
        comprobar(so.getDisco().getLibres() == libresAntes - 12 + 7 + 11, "los bloques se liberaron");

        //4. guardar, volver a cargar y comparar
        File archivo = File.createTempFile("avilaos_prueba", ".json");
        archivo.deleteOnExit();
        so.guardarArchivos(archivo.getPath());
        SistemaOperativo otro = new SistemaOperativo(ArchivosJSON.cargarArchivos(archivo.getPath()),
                ArchivosJSON.cargarProcesos(carpeta + "/procesos_permisos.json"));
        comprobar(mismoArbol(so.copiarArbol(), otro.copiarArbol()), "el arbol cargado es igual al guardado");
        comprobar(mismosBloques(so.getDisco().copiarBloques(), otro.getDisco().copiarBloques()),
                "el mapa de bloques cargado es igual al guardado (mismas cadenas)");

        System.out.println();
        System.out.println("===== Resumen del sistema de archivos =====");
        System.out.println(fallas == 0 ? "Todo quedo igual: 0 fallas" : "Hubo " + fallas + " fallas");
    }

    private static void pedir(SistemaOperativo so, TipoOperacionES op, String ruta, int tamano, String nuevoNombre) {
        DatosProceso d = new DatosProceso();
        d.nombre = op + "_" + ruta.substring(ruta.lastIndexOf('/') + 1);
        d.memoria = 8;
        d.instrucciones = 1;
        d.operacion = op;
        d.ruta = ruta;
        d.tamano = tamano;
        d.permiso = Permiso.PRIVADO;
        d.dueno = "user1";
        d.nuevoNombre = nuevoNombre;
        so.crearProceso(d);
    }

    //da ciclos hasta que no quede nada por hacer
    private static long ciclo = 0;

    private static void correr(SistemaOperativo so) {
        int vueltas = 0;
        do {
            ciclo++;
            vueltas++;
            so.getComputador().onTick(ciclo);
            so.getDisco().onTick(ciclo);
        } while (vueltas < 100000 && (!so.getDisco().estaOcioso() || !so.getComputador().getNuevos().estaVacia()
                || !so.getComputador().getListos().estaVacia() || !so.getComputador().getBloqueados().estaVacia()
                || so.getComputador().getEnEjecucion() != null));
    }

    private static InfoNodo buscar(SistemaOperativo so, String ruta) {
        InfoNodo[] nodos = so.copiarArbol();
        for (int i = 0; i < nodos.length; i++) {
            if (nodos[i].ruta.equals(ruta)) {
                return nodos[i];
            }
        }
        return null;
    }

    private static boolean mismoArbol(InfoNodo[] a, InfoNodo[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (!a[i].ruta.equals(b[i].ruta) || a[i].tipo != b[i].tipo || !a[i].dueno.equals(b[i].dueno)
                    || a[i].permiso != b[i].permiso || a[i].bloques != b[i].bloques
                    || a[i].primerBloque != b[i].primerBloque || !a[i].creador.equals(b[i].creador)) {
                System.out.println("   distinto: " + a[i].ruta + " / " + b[i].ruta);
                return false;
            }
        }
        return true;
    }

    //solo se comparan los bloques de archivos, el area de intercambio empieza vacia
    private static boolean mismosBloques(Bloque[] a, Bloque[] b) {
        for (int i = 0; i < Math.min(a.length, b.length); i++) {
            if (a[i].esSwap()) {
                break;
            }
            if (a[i].estaOcupado() != b[i].estaOcupado() || a[i].getSiguiente() != b[i].getSiguiente()
                    || !a[i].getOcupante().equals(b[i].getOcupante())) {
                System.out.println("   distinto en el bloque " + i);
                return false;
            }
        }
        return true;
    }

    private static void comprobar(boolean condicion, String que) {
        System.out.println((condicion ? "[ok] " : "[FALLA] ") + que);
        if (!condicion) {
            fallas++;
        }
    }

    private static String texto(int[] c) {
        String t = "";
        for (int i = 0; i < c.length; i++) {
            t += (i == 0 ? "" : " -> ") + c[i];
        }
        return t;
    }
}
