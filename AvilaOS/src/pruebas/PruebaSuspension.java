/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.PoliticaDisco;
import avilaos.SistemaOperativo;

/**
 *
 * @author diego
 */
//prueba por consola de la suspension: poca RAM y mas procesos de los que caben
//se corre con cada politica de disco para ver como cambia el resultado
public class PruebaSuspension {

    public static void main(String[] args) throws Exception {
        String carpeta = args.length > 0 ? args[0] : "datos";
        PoliticaDisco[] politicas = PoliticaDisco.values();
        String[] resumen = new String[politicas.length];
        for (int i = 0; i < politicas.length; i++) {
            resumen[i] = correr(carpeta + "/sistema_archivos.json", carpeta + "/procesos_suspension.json", politicas[i]);
        }
        System.out.println();
        System.out.println("===== Resumen de la suspension =====");
        for (int i = 0; i < resumen.length; i++) {
            System.out.println(resumen[i]);
        }
    }

    private static String correr(String rutaArchivos, String rutaProcesos, PoliticaDisco politica) throws Exception {
        ConfigArchivos ca = ArchivosJSON.cargarArchivos(rutaArchivos);
        ConfigProcesos cp = ArchivosJSON.cargarProcesos(rutaProcesos);
        cp.globalConfig.initialDiskPolicy = politica.name();
        System.out.println();
        System.out.println("===== " + rutaProcesos + " con RAM de " + cp.computer.ramCapacity + " MB y disco "
                + politica + " =====");
        SistemaOperativo so = new SistemaOperativo(ca, cp);
        //los ciclos se dan a mano, igual que en PruebaDisco
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
        System.out.println("--- decisiones del mediano plazo, en orden ---");
        for (int i = 0; i < lineas.length; i++) {
            String l = lineas[i];
            if (l.contains("Se suspende") || l.contains("Se reactiva") || l.contains("bloqueado/suspendido")
                    || l.contains("listo/suspendido") || l.contains("vuelve a la RAM") || l.contains("intercambio")
                    || l.contains("estando suspendido")) {
                System.out.println(l);
            }
        }
        int libres = so.getDisco().getLibresSwap();
        return politica + ": suspensiones " + so.getMetricas().getSuspensiones() + ", reactivaciones "
                + so.getMetricas().getReactivaciones() + " | desplazamiento " + so.getDisco().getDesplazamientoTotal()
                + " | espera prom. " + String.format("%.1f", so.getDisco().getEsperaPromedio())
                + " | ciclos " + ciclo + " | terminados " + so.getComputador().getTerminados().len() + "/" + total
                + " | swap libre al final " + libres + "/" + so.getDisco().getBloquesSwap();
    }
}
