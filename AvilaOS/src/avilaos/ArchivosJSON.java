/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package avilaos;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author andrea
 */
//lee y escribe los dos archivos json del simulador
public class ArchivosJSON {

    //el pretty printing es para que el archivo quede ordenado y se pueda leer
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static ConfigArchivos cargarArchivos(String ruta) throws IOException {
        ConfigArchivos config = leer(ruta, ConfigArchivos.class);
        config.validar();
        return config;
    }

    public static ConfigProcesos cargarProcesos(String ruta) throws IOException {
        ConfigProcesos config = leer(ruta, ConfigProcesos.class);
        config.validar();
        return config;
    }

    public static void guardarArchivos(ConfigArchivos config, String ruta) throws IOException {
        try (Writer w = new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8)) {
            GSON.toJson(config, w);
        }
    }

    private static <T> T leer(String ruta, Class<T> clase) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8)) {
            T config = GSON.fromJson(r, clase);
            if (config == null) {
                throw new IllegalArgumentException("El archivo esta vacio");
            }
            return config;
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("El archivo no tiene un formato JSON valido");
        }
    }
}
