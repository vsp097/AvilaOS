/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

/**
 *
 * @author andrea
 */
//el archivo json del sistema de archivos
//las clases de adentro son solo datos, por eso sus campos son publicos (asi gson los lee y escribe directo)
public class ConfigArchivos {

    public static class DiskConfig {
        public int totalBlocks;
        public int initialHead;
        //posiciones que recorre el cabezal por ciclo, es opcional
        public Integer headSpeed;
        //FIXED (el cabezal se queda en el primer bloque) o CHAIN (recorre la cadena), es opcional
        public String transferMode;
    }

    public static class Entrada {
        public String path;
        public String type;
        public Integer startBlock;
        public Integer blocks;
        public String owner;
        public String permissions;
        //la cadena completa de bloques; se escribe al guardar para poder cargar el disco igual que estaba
        public int[] chain;
        //nombre del proceso que creo el archivo, tambien se escribe al guardar
        public String createdBy;
    }

    public DiskConfig diskConfig;
    public Entrada[] fileSystem;

    //revisa lo que se puede revisar sin tener el disco armado
    public void validar() {
        if (diskConfig == null) {
            throw new IllegalArgumentException("Falta la seccion diskConfig");
        }
        if (diskConfig.totalBlocks < Disco.MIN_BLOQUES || diskConfig.totalBlocks > Disco.MAX_BLOQUES) {
            throw new IllegalArgumentException("totalBlocks debe estar entre " + Disco.MIN_BLOQUES + " y " + Disco.MAX_BLOQUES);
        }
        if (diskConfig.initialHead < 0 || diskConfig.initialHead >= diskConfig.totalBlocks) {
            throw new IllegalArgumentException("initialHead debe estar entre 0 y " + (diskConfig.totalBlocks - 1));
        }
        if (diskConfig.headSpeed != null && (diskConfig.headSpeed < 1 || diskConfig.headSpeed > Disco.MAX_BLOQUES)) {
            throw new IllegalArgumentException("headSpeed debe estar entre 1 y " + Disco.MAX_BLOQUES);
        }
        ModoTransferencia.desdeTexto(diskConfig.transferMode);
        if (fileSystem == null) {
            fileSystem = new Entrada[0];
        }
        for (int i = 0; i < fileSystem.length; i++) {
            Entrada e = fileSystem[i];
            String donde = "Elemento " + (i + 1) + " del sistema de archivos";
            if (e == null) {
                throw new IllegalArgumentException(donde + " esta vacio");
            }
            try {
                e.path = SistemaArchivos.limpiarRuta(e.path);
                if (e.path.equals("/")) {
                    throw new IllegalArgumentException("la raiz no se declara");
                }
                TipoNodo tipo = TipoNodo.desdeTexto(e.type);
                Permiso.desdeTexto(e.permissions);
                if (e.owner == null || e.owner.trim().isEmpty()) {
                    throw new IllegalArgumentException("falta el dueno (owner)");
                }
                e.owner = e.owner.trim();
                if (tipo == TipoNodo.ARCHIVO) {
                    if (e.chain != null && e.chain.length > 0) {
                        e.startBlock = e.chain[0];
                        e.blocks = e.chain.length;
                    }
                    if (e.startBlock == null || e.blocks == null) {
                        throw new IllegalArgumentException("a un archivo le faltan startBlock o blocks");
                    }
                    if (e.blocks < 1 || e.blocks > diskConfig.totalBlocks) {
                        throw new IllegalArgumentException("blocks debe estar entre 1 y " + diskConfig.totalBlocks);
                    }
                }
            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(donde + ": " + ex.getMessage());
            }
        }
    }
}
