package parser;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PseudoValidator {

    private static final Pattern TAG_OR_TEXT = Pattern.compile("(<[^>]+>)|([^<]+)");
    private static final Pattern OPEN_TAG = Pattern.compile("^<([a-zA-Z]+)>$");
    private static final Pattern CLOSE_TAG = Pattern.compile("^</([a-zA-Z]+)>$");
    private static final Pattern ID_RE = Pattern.compile("^[A-Za-z][A-Za-z0-9_]*$");
    private static final Pattern NUM_RE = Pattern.compile("^[0-9]+$");
    private static final Pattern ASSIGN_RE = Pattern.compile("^\\s*([A-Za-z][A-Za-z0-9_]*)\\s*=\\s*(.+);\\s*$");

    // 🔹 Acepta comparaciones con <, >, <=, >=, ==, !=
    private static final Pattern CONDITION_SIMPLE = Pattern.compile(
        "\\s*([A-Za-z][A-Za-z0-9_]*|[0-9]+)\\s*(>=|<=|>|<|==|!=)\\s*([A-Za-z][A-Za-z0-9_]*|[0-9]+)\\s*"
    );

    public void parse(String input) {
        Matcher m = TAG_OR_TEXT.matcher(input);
        Deque<String> stack = new ArrayDeque<>();
        Deque<StringBuilder> buffers = new ArrayDeque<>();
        buffers.push(new StringBuilder());

        while (m.find()) {
            String tag = m.group(1);
            String text = m.group(2);

            if (tag != null) {
                Matcher mo = OPEN_TAG.matcher(tag);
                Matcher mc = CLOSE_TAG.matcher(tag);
                if (mo.matches()) {
                    String name = mo.group(1).toLowerCase();
                    stack.push(name);
                    buffers.push(new StringBuilder());
                    if (name.equals("funcion")) {
                        PseudoParserDriver.totalFunciones++;
                    }
                } else if (mc.matches()) {
                    String name = mc.group(1).toLowerCase();
                    if (stack.isEmpty() || !stack.peek().equals(name)) {
                        PseudoParserDriver.errores_sintacticos++;
                        PseudoParserDriver.listaErrores.add("Etiqueta cerrada inesperada </" + name + ">");
                        continue;
                    }
                    String inner = buffers.pop().toString();
                    stack.pop();

                    switch (name) {
                        case "parametros":
                            processParametros(inner);
                            break;
                        case "codigo":
                            processCodigo(inner);
                            break;
                        case "condicion":
                            processCondicion(inner);
                            break;
                        case "if":
                            processIfBlock(inner);
                            break;
                        case "do":
                            processDoBlock(inner);
                            break;
                        default:
                            break;
                    }

                    buffers.peek().append(" ");
                } else {
                    PseudoParserDriver.errores_lexicos++;
                    PseudoParserDriver.listaErrores.add("Etiqueta no reconocida: " + tag);
                }
            } else if (text != null) {
                buffers.peek().append(text);
            }
        }

        while (!stack.isEmpty()) {
            String un = stack.pop();
            PseudoParserDriver.errores_sintacticos++;
            PseudoParserDriver.listaErrores.add("Etiqueta no cerrada: <" + un + ">");
        }
    }

    private void processParametros(String inner) {
        String cleaned = inner.replaceAll("[\\r\\n]", " ").trim();
        if (cleaned.isEmpty()) return;
        String[] parts = cleaned.split(",");
        for (String p : parts) {
            String t = p.trim();
            if (t.isEmpty()) continue;
            if (ID_RE.matcher(t).matches() || NUM_RE.matcher(t).matches()) {
                PseudoParserDriver.parametros_validos++;
            } else {
                PseudoParserDriver.parametros_invalidos++;
                PseudoParserDriver.listaErrores.add("Parámetro inválido: '" + t + "'");
            }
        }
    }

    private void processCodigo(String inner) {
        String[] lines = inner.split("[\\r\\n]+");
        for (String ln : lines) {
            String s = ln.trim();
            if (s.isEmpty()) continue;
            if (s.startsWith("<")) continue;
            Matcher ma = ASSIGN_RE.matcher(s);
            if (ma.matches()) {
                String left = ma.group(1);
                String right = ma.group(2);
                boolean leftOk = ID_RE.matcher(left).matches();
                boolean rightOk = validateExpression(right);
                if (leftOk && rightOk) {
                    PseudoParserDriver.asignaciones_validas++;
                } else {
                    PseudoParserDriver.asignaciones_invalidas++;
                    PseudoParserDriver.listaErrores.add("Asignación inválida: '" + s + "'");
                }
            } else {
                if (s.contains("=")) {
                    PseudoParserDriver.asignaciones_invalidas++;
                    PseudoParserDriver.listaErrores.add("Asignación mal formada: '" + s + "'");
                }
            }
        }
    }

    private boolean validateExpression(String expr) {
        String cleaned = expr.replaceAll("[\\s\\t]+", "");
        if (cleaned.isEmpty()) return false;
        if (!cleaned.matches("[A-Za-z0-9_+\\-*/()]+")) return false;
        if (!cleaned.matches(".*[A-Za-z0-9].*")) return false;
        return true;
    }

    // 🔹 Actualizado: decodifica &lt; &gt; &amp;&amp; antes de validar
    private void processCondicion(String inner) {
        String t = inner.trim();
        if (t.isEmpty()) {
            PseudoParserDriver.condiciones_invalidas++;
            PseudoParserDriver.listaErrores.add("Condición vacía");
            return;
        }

        // Decodificar entidades HTML comunes
        t = t.replaceAll("&amp;&amp;", "&&");
        t = t.replaceAll("&amp;", "&");
        t = t.replaceAll("&lt;", "<");
        t = t.replaceAll("&gt;", ">");
        t = t.trim();

        // Separar condiciones por && o ||
        String[] parts = t.split("\\s*(?:&&|\\|\\|)\\s*");

        for (String part : parts) {
            String p = part.trim();
            if (p.isEmpty()) continue;

            Matcher mc = CONDITION_SIMPLE.matcher(p);
            if (mc.matches()) {
                PseudoParserDriver.condiciones_validas++;
            } else {
                PseudoParserDriver.condiciones_invalidas++;
                PseudoParserDriver.listaErrores.add("Condición inválida: '" + p + "'");
            }
        }
    }

    private void processIfBlock(String inner) {
        boolean hasCond = inner.toLowerCase().contains("<condicion>");
        boolean hasCodigo = inner.toLowerCase().contains("<codigo>");
        if (!hasCond || !hasCodigo) {
            PseudoParserDriver.if_invalidos++;
            PseudoParserDriver.listaErrores.add("<if> inválido: falta <condicion> o <codigo>");
            return;
        }
        PseudoParserDriver.if_validos++;
    }

    private void processDoBlock(String inner) {
        boolean hasCond = inner.toLowerCase().contains("<condicion>");
        boolean hasCodigo = inner.toLowerCase().contains("<codigo>");
        if (!hasCond || !hasCodigo) {
            PseudoParserDriver.do_invalidos++;
            PseudoParserDriver.listaErrores.add("<do> inválido: falta <codigo> o <condicion>");
            return;
        }
        PseudoParserDriver.do_validos++;
    }
}
