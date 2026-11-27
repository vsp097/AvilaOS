package avilaos;

/**
 *
 * @author andrea
 */
public class Validaciones {

    //revisa que el texto no este vacio ni muy largo, devuelve el texto sin espacios de los lados
    public static String leerTexto(String texto, String campo, int largoMax) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacio");
        }
        String limpio = texto.trim();
        //los signos < y > no se dejan porque Java los podria leer como formato (html) en las listas
        if (limpio.contains("<") || limpio.contains(">")) {
            throw new IllegalArgumentException("El campo " + campo + " no puede tener los signos < o >");
        }
        if (limpio.length() > largoMax) {
            throw new IllegalArgumentException("El campo " + campo + " no puede tener mas de " + largoMax + " caracteres");
        }
        return limpio;
    }

    //revisa que el texto sea un numero entero y que este entre min y max
    public static int leerEntero(String texto, String campo, int min, int max) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacio");
        }
        int valor;
        try {
            valor = Integer.parseInt(texto.trim());
        } catch (NumberFormatException e) {
            //se dice que fue lo que escribieron para que se entienda el error
            if (texto.trim().matches("-?[0-9]+[.,][0-9]*")) {
                throw new IllegalArgumentException("El campo " + campo + " debe ser un numero entero, sin decimales (escribiste '"
                        + texto.trim() + "')");
            }
            if (texto.trim().matches("-?[0-9]+")) {
                throw new IllegalArgumentException("El campo " + campo + " tiene un numero demasiado grande");
            }
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero entero (escribiste '"
                    + texto.trim() + "')");
        }
        if (valor < min || valor > max) {
            throw new IllegalArgumentException("El campo " + campo + " debe estar entre " + min + " y " + max);
        }
        return valor;
    }

    //igual que el entero pero acepta decimales, por ejemplo para la memoria
    public static float leerDecimal(String texto, String campo, float min, float max) {
        if (texto == null || texto.trim().isEmpty()) {
            throw new IllegalArgumentException("El campo " + campo + " no puede estar vacio");
        }
        float valor;
        try {
            valor = Float.parseFloat(texto.trim());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero valido (escribiste '"
                    + texto.trim() + "'), usa punto para los decimales");
        }
        //parseFloat deja pasar "NaN" e "Infinity", asi que los reviso aparte
        if (Float.isNaN(valor) || Float.isInfinite(valor)) {
            throw new IllegalArgumentException("El campo " + campo + " debe ser un numero valido");
        }
        if (valor < min || valor > max) {
            throw new IllegalArgumentException("El campo " + campo + " debe estar entre " + numero(min) + " y " + numero(max));
        }
        return valor;
    }

    //escribe 1 en vez de 1.0 cuando el numero no tiene decimales
    private static String numero(float f) {
        return f == (long) f ? String.valueOf((long) f) : String.valueOf(f);
    }
}
