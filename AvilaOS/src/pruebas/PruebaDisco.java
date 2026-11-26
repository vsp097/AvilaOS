/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import Data_Structures.PCB;
import Data_Structures.TipoProceso;
import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.Disco;
import avilaos.OyenteDisco;
import avilaos.PoliticaDisco;
import avilaos.SelectorPoliticaDisco;
import avilaos.SistemaOperativo;
import avilaos.SolicitudES;
import avilaos.TipoOperacionES;

/**
 *
 * @author diego
 */
//pruebas por consola de las politicas de disco, sin ventana
//primero el ejemplo del anexo con el disco solo, y despues los casos P1 a P4 con el sistema completo
public class PruebaDisco {

    //guarda el orden en que el disco fue atendiendo
    private static String orden = "";

    public static void main(String[] args) throws Exception {
        System.out.println("===== Anexo: cabezal en 50, cola 82, 17, 43, 140, 24, 16, 190 =====");
        int[] cola = {82, 17, 43, 140, 24, 16, 190};
        PoliticaDisco[] politicas = PoliticaDisco.values();
        String[] resumen = new String[politicas.length];
        for (int i = 0; i < politicas.length; i++) {
            resumen[i] = anexo(politicas[i], cola);
        }
        String carpeta = args.length > 0 ? args[0] : "datos";
        String[] archivos = {"procesos_fifo.json", "procesos_sstf.json", "procesos_scan.json", "procesos_cscan.json"};
        String[] casos = new String[archivos.length];
        for (int i = 0; i < archivos.length; i++) {
            casos[i] = caso(carpeta + "/sistema_archivos.json", carpeta + "/" + archivos[i]);
        }
        System.out.println();
        System.out.println("===== Resumen del anexo =====");
        for (int i = 0; i < resumen.length; i++) {
            System.out.println(resumen[i]);
        }
        System.out.println("===== Resumen de los casos P1 a P4 =====");
        for (int i = 0; i < casos.length; i++) {
            System.out.println(casos[i]);
        }
    }

    //el disco solo, con todas las solicitudes ya en la cola
    private static String anexo(PoliticaDisco politica, int[] cola) {
        Disco disco = new Disco(200, 0, 50);
        disco.setVelocidad(1);
        disco.setPlanificador(SelectorPoliticaDisco.crear(politica));
        orden = "";
        disco.setOyente(new OyenteDisco() {
            @Override
            public void solicitudAtendida(SolicitudES s) {
                orden += (orden.isEmpty() ? "" : ", ") + s.getBloque();
            }
        });
        for (int i = 0; i < cola.length; i++) {
            PCB p = new PCB(i + 1, "P" + cola[i], 0, TipoProceso.ES, 1);
            disco.encolar(new SolicitudES(p, TipoOperacionES.LEER, "/prueba", new int[]{cola[i]}, 0));
        }
        long ciclo = 0;
        while (!disco.estaOcioso() && ciclo < 100000) {
            ciclo++;
            disco.onTick(ciclo);
        }
        return politica + ": " + orden + "  | desplazamiento " + disco.getDesplazamientoTotal() + " bloques";
    }

    //el sistema completo: los procesos del json pasan por la CPU, piden su E/S y el disco los atiende
    private static String caso(String rutaArchivos, String rutaProcesos) throws Exception {
        ConfigArchivos ca = ArchivosJSON.cargarArchivos(rutaArchivos);
        ConfigProcesos cp = ArchivosJSON.cargarProcesos(rutaProcesos);
        System.out.println();
        System.out.println("===== " + rutaProcesos + " (" + cp.globalConfig.initialDiskPolicy + ") =====");
        SistemaOperativo so = new SistemaOperativo(ca, cp);
        //aqui el reloj no se arranca: los ciclos se dan a mano para que la prueba no dependa del tiempo
        long ciclo = 0;
        int total = cp.processes.length;
        while (ciclo < 100000) {
            ciclo++;
            so.getComputador().onTick(ciclo);
            so.getDisco().onTick(ciclo);
            if (so.getComputador().getTerminados().len() == total && so.getDisco().estaOcioso()) {
                break;
            }
        }
        //el orden se saca del log, que es lo mismo que ve el usuario
        String[] lineas = so.getLog().getTexto().split("\n");
        String atencion = "";
        for (int i = 0; i < lineas.length; i++) {
            String marca = "en el bloque ";
            int j = lineas[i].indexOf(marca);
            if (lineas[i].contains("El disco atiende") && j >= 0) {
                String resto = lineas[i].substring(j + marca.length());
                atencion += (atencion.isEmpty() ? "" : " -> ") + resto.substring(0, resto.indexOf(' '));
            }
        }
        return cp.globalConfig.initialDiskPolicy + ": " + atencion + "  | desplazamiento "
                + so.getDisco().getDesplazamientoTotal() + " | ciclos " + ciclo
                + " | terminados " + so.getComputador().getTerminados().len() + "/" + total;
    }
}
