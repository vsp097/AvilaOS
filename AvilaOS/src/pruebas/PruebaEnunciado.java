/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package pruebas;

import avilaos.ArchivosJSON;
import avilaos.ConfigArchivos;
import avilaos.ConfigProcesos;
import avilaos.ModoTransferencia;
import avilaos.PoliticaDisco;
import avilaos.SistemaOperativo;
import avilaos.SolicitudES;

/**
 *
 * @author vyckh
 */
//corre los dos JSON tal cual vienen en el enunciado (archivos de varios bloques, mbr.bin en el bloque 0)
//y compara el orden en que el disco atiende con las tablas de los casos P1 a P4
public class PruebaEnunciado {

    private static final String[] ESPERADO = {
        "95 180 34 119 11 123 62 64",
        "62 64 34 11 95 119 123 180",
        "62 64 95 119 123 180 34 11",
        "62 64 95 119 123 180 11 34"
    };
    private static final PoliticaDisco[] POLITICAS = {
        PoliticaDisco.FIFO, PoliticaDisco.SSTF, PoliticaDisco.SCAN, PoliticaDisco.C_SCAN
    };

    public static void main(String[] args) throws Exception {
        String carpeta = args.length > 0 ? args[0] : "datos";
        int fallas = 0;
        for (ModoTransferencia modo : ModoTransferencia.values()) {
            System.out.println("===== Transferencia " + modo.enJson() + " (" + modo + ") =====");
            for (int i = 0; i < POLITICAS.length; i++) {
                String orden = correr(carpeta, POLITICAS[i], modo);
                boolean igual = orden.equals(ESPERADO[i]);
                if (modo == ModoTransferencia.FIJO && !igual) {
                    fallas++;
                }
                System.out.println(POLITICAS[i] + ": " + orden.replace(" ", " -> ")
                        + (modo == ModoTransferencia.FIJO ? (igual ? "  [ok, igual al enunciado]" : "  [DISTINTO al enunciado]") : ""));
            }
        }
        System.out.println();
        System.out.println("Con FIXED (el modo por defecto) los casos P1 a P4 dan distinto al enunciado: " + fallas);
        System.out.println("Con CHAIN el cabezal recorre los bloques de cada archivo, por eso SSTF cambia despues de leer core.dll (64 a 94).");
    }

    //arma el sistema con los dos JSON del enunciado, cambia la politica y el modo, y anota el bloque de cada solicitud que empieza
    private static String correr(String carpeta, PoliticaDisco politica, ModoTransferencia modo) throws Exception {
        ConfigArchivos ca = ArchivosJSON.cargarArchivos(carpeta + "/enunciado_sistema_archivos.json");
        ca.diskConfig.transferMode = modo.enJson();
        ConfigProcesos cp = ArchivosJSON.cargarProcesos(carpeta + "/enunciado_procesos.json");
        cp.globalConfig.initialDiskPolicy = politica.toString();
        SistemaOperativo so = new SistemaOperativo(ca, cp);
        StringBuilder orden = new StringBuilder();
        SolicitudES anterior = null;
        //los ciclos se dan a mano para que el resultado no dependa de la velocidad de la computadora
        for (int c = 1; c <= 3000; c++) {
            so.getComputador().onTick(c);
            so.getDisco().onTick(c);
            SolicitudES s = so.getDisco().getActual();
            if (s != null && s != anterior) {
                orden.append(orden.length() == 0 ? "" : " ").append(s.getBloque());
                anterior = s;
            }
        }
        return orden.toString();
    }
}
