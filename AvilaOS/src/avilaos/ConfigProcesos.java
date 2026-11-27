/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//el archivo json de procesos y configuracion
//las clases de adentro son solo datos, por eso sus campos son publicos (asi gson los lee directo)
public class ConfigProcesos {

    public static class GlobalConfig {
        public int cycleDurationMs;
        public int swapSizeBlocks;
        public String initialDiskPolicy;
    }

    public static class Computer {
        public int ramCapacity;
        public String policy;
        public int quantum;
    }

    public static class Proceso {
        public String name;
        public float memory;
        public int deadline;
        public int instructions;
        public String targetPath;
        public String operation;
        public int fileSizeBlocks;
        //opcional, si no viene se usa 1
        public Integer priority;
    }

    public GlobalConfig globalConfig;
    public Computer computer;
    public Proceso[] processes;

    //la politica de cpu puede venir con el nombre del enum o con los nombres cortos de siempre
    public static PoliticaPlanificacion leerPolitica(String texto) {
        if (texto == null) {
            throw new IllegalArgumentException("Falta la politica del computador");
        }
        String t = texto.trim().toUpperCase().replace(" ", "_").replace("-", "_");
        switch (t) {
            case "FCFS":
            case "FIFO":
                return PoliticaPlanificacion.FCFS;
            case "RR":
            case "ROUND_ROBIN":
            case "ROUNDROBIN":
                return PoliticaPlanificacion.ROUND_ROBIN;
            case "EDF":
                return PoliticaPlanificacion.EDF;
            case "PRIORIDADES":
            case "PRIORIDAD":
            case "PRIORITY":
                return PoliticaPlanificacion.PRIORIDADES;
            default:
                throw new IllegalArgumentException("La politica '" + texto + "' del computador no es valida");
        }
    }

    public void validar() {
        if (globalConfig == null) {
            throw new IllegalArgumentException("Falta la seccion globalConfig");
        }
        if (computer == null) {
            throw new IllegalArgumentException("Falta la seccion computer");
        }
        rango(globalConfig.cycleDurationMs, "cycleDurationMs", 1, 60000);
        rango(globalConfig.swapSizeBlocks, "swapSizeBlocks", 0, Disco.MAX_BLOQUES);
        PoliticaDisco.desdeTexto(globalConfig.initialDiskPolicy);
        rango(computer.ramCapacity, "ramCapacity", 1, 1000000);
        PoliticaPlanificacion politica = leerPolitica(computer.policy);
        //el quantum solo importa con round robin, en las demas puede venir en 0
        if (politica == PoliticaPlanificacion.ROUND_ROBIN) {
            rango(computer.quantum, "quantum", 1, 1000);
        } else {
            rango(computer.quantum, "quantum", 0, 1000);
        }
        if (processes == null) {
            processes = new Proceso[0];
        }
        for (int i = 0; i < processes.length; i++) {
            Proceso p = processes[i];
            String donde = "Proceso " + (i + 1);
            if (p == null) {
                throw new IllegalArgumentException(donde + " esta vacio");
            }
            try {
                p.name = Validaciones.leerTexto(p.name, "name", 30);
                donde = "Proceso " + p.name;
                if (Float.isNaN(p.memory) || p.memory < 1 || p.memory > 1000000) {
                    throw new IllegalArgumentException("memory debe estar entre 1 y 1000000");
                }
                if (p.memory > computer.ramCapacity) {
                    throw new IllegalArgumentException("pide " + p.memory + " de memoria y la RAM es de " + computer.ramCapacity);
                }
                rango(p.deadline, "deadline", 0, 100000);
                rango(p.instructions, "instructions", 1, 100000);
                if (p.priority != null) {
                    rango(p.priority, "priority", 1, 10);
                }
                TipoOperacionES op = TipoOperacionES.desdeTexto(p.operation);
                if (op != null) {
                    p.targetPath = SistemaArchivos.limpiarRuta(p.targetPath);
                    if (op == TipoOperacionES.CREAR) {
                        rango(p.fileSizeBlocks, "fileSizeBlocks", 1, Disco.MAX_BLOQUES);
                    }
                }
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(donde + ": " + ex.getMessage());
            }
        }
    }

    private void rango(int valor, String campo, int min, int max) {
        if (valor < min || valor > max) {
            throw new IllegalArgumentException(campo + " debe estar entre " + min + " y " + max);
        }
    }
}
