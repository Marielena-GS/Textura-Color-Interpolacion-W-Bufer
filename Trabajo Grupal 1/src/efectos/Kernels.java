package efectos;

/**
 * Clase que contiene todos los kernels (matrices de convolucion) disponibles.
 * Cada kernel es un arreglo de floats de 9 elementos (matriz 3x3).
 */

public class Kernels {

    private Kernels() {}

    public static final float[] kNormal = {
        0f, 0f, 0f,
        0f, 1f, 0f,
        0f, 0f, 0f
    };

    public static final float[] kEnfoque = {
        0f, -1f,  0f,
       -1f,  5f, -1f,
        0f, -1f,  0f
    };

    public static final float[] kDesenfoque = {
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9,
        1f/9, 1f/9, 1f/9
    };

    public static final float[] kDesenfoque9 = {
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,
        1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81,1f/81
    };

    public static final float[] kBordes = {
       -0.5f, -0.5f, -0.5f,
       -0.5f,  4f,   -0.5f,
       -0.5f, -0.5f, -0.5f
    };

    public static final float[] kBordes8 = {
       -1f, -1f, -1f,
       -1f,  8f, -1f,
       -1f, -1f, -1f
    };

    public static final float[] kAclaracion = {
        0.1f, 0.1f, 0.1f,
        0.1f, 1.0f, 0.1f,
        0.1f, 0.1f, 0.1f
    };

    public static final float[] kOscurecer = {
        0.01f, 0.01f, 0.01f,
        0.01f,  0.5f, 0.01f,
        0.01f, 0.01f, 0.01f
    };

    /**
     * Devuelve el nombre legible de cada kernel para mostrar en la UI.
     *
     * @param kernel arreglo de floats del kernel
     * @return nombre descriptivo
     */
    
    public static String getNombre(float[] kernel) {
        if (kernel == kNormal)      return "Normal (sin cambio)";
        if (kernel == kEnfoque)     return "Enfoque (Sharpen)";
        if (kernel == kDesenfoque)  return "Desenfoque 3x3";
        if (kernel == kDesenfoque9) return "Desenfoque 9x9";
        if (kernel == kBordes)      return "Bordes (4 vecinos)";
        if (kernel == kBordes8)     return "Bordes (8 vecinos)";
        if (kernel == kAclaracion)  return "Aclaracion";
        if (kernel == kOscurecer)   return "Oscurecer";
        return "Kernel personalizado";
    }
}