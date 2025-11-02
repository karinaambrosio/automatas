package parser;

import java.util.ArrayList;
import java.util.List;

public class PseudoParserDriver {
    public static String archivoActual = "";
    public static int errores_sintacticos = 0;
    public static List<String> listaErrores = new ArrayList<>();
    public static int totalFunciones = 0;
    public static int parametros_validos = 0;
    public static int parametros_invalidos = 0;
    public static int asignaciones_validas = 0;
    public static int asignaciones_invalidas = 0;
    public static int if_validos = 0;
    public static int if_invalidos = 0;
    public static int do_validos = 0;
    public static int do_invalidos = 0;
    public static int condiciones_validas = 0;
    public static int condiciones_invalidas = 0;
    public static int errores_lexicos = 0;

    public static void printReport() {
        System.out.println(getReport());
    }

    public static String getReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n--- REPORTE DE VALIDACIÓN ---\n");
        sb.append("Archivo: ").append((archivoActual == null || archivoActual.isEmpty()) ? "(sin nombre)" : archivoActual).append("\n");
        sb.append("Funciones: ").append(totalFunciones).append("\n");
        sb.append("Parámetros válidos: ").append(parametros_validos).append("\n");
        sb.append("Parámetros inválidos: ").append(parametros_invalidos).append("\n");
        sb.append("Asignaciones válidas: ").append(asignaciones_validas).append("\n");
        sb.append("Asignaciones inválidas: ").append(asignaciones_invalidas).append("\n");
        sb.append("If válidos: ").append(if_validos).append("\n");
        sb.append("If inválidos: ").append(if_invalidos).append("\n");
        sb.append("Do válidos: ").append(do_validos).append("\n");
        sb.append("Do inválidos: ").append(do_invalidos).append("\n");
        sb.append("Condiciones válidas: ").append(condiciones_validas).append("\n");
        sb.append("Condiciones inválidas: ").append(condiciones_invalidas).append("\n");
        sb.append("Errores léxicos: ").append(errores_lexicos).append("\n");
        sb.append("Errores sintácticos: ").append(errores_sintacticos).append("\n\n");
        sb.append("Lista de errores (línea aproximada):\n");
        if (listaErrores == null || listaErrores.isEmpty()) {
            sb.append(" - No se encontraron errores 🎉\n");
        } else {
            for (String e : listaErrores) {
                sb.append(" - ").append(e).append("\n");
            }
        }
        sb.append("-----------------------------\n");
        return sb.toString();
    }

    public static void resetAll() {
        archivoActual = "";
        errores_sintacticos = 0;
        listaErrores.clear();
        totalFunciones = 0;
        parametros_validos = 0;
        parametros_invalidos = 0;
        asignaciones_validas = 0;
        asignaciones_invalidas = 0;
        if_validos = 0;
        if_invalidos = 0;
        do_validos = 0;
        do_invalidos = 0;
        condiciones_validas = 0;
        condiciones_invalidas = 0;
        errores_lexicos = 0;
    }
}
