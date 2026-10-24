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
public class ConfiguracionJSON {

    //el pretty printing es para que el archivo quede ordenado y se pueda leer
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    //valida la configuracion y la escribe en un archivo json
    public static void guardar(Configuracion config, String ruta) throws IOException {
        config.validar();
        try (Writer w = new OutputStreamWriter(new FileOutputStream(ruta), StandardCharsets.UTF_8)) {
            GSON.toJson(config, w);
        }
    }

    //lee un archivo json y revisa que todo este en rango antes de devolverlo
    public static Configuracion cargar(String ruta) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(ruta), StandardCharsets.UTF_8)) {
            Configuracion config = GSON.fromJson(r, Configuracion.class);
            if (config == null) {
                throw new IllegalArgumentException("El archivo esta vacio");
            }
            config.validar();
            return config;
        } catch (JsonParseException e) {
            throw new IllegalArgumentException("El archivo no tiene un formato JSON valido");
        }
    }
}