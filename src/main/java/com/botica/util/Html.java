package com.botica.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Utilidades de presentación para las vistas JSP (escape HTML, formatos, iconos). */
public final class Html {
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Html() {
    }

    public static String esc(Object o) {
        if (o == null) return "";
        String s = o.toString();
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (char c : s.toCharArray()) {
            switch (c) {
                case '&' -> sb.append("&amp;");
                case '<' -> sb.append("&lt;");
                case '>' -> sb.append("&gt;");
                case '"' -> sb.append("&quot;");
                case '\'' -> sb.append("&#39;");
                default -> sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String money(double v) {
        return String.format(Locale.US, "S/ %.2f", v);
    }

    public static String fecha(LocalDateTime f) {
        return f == null ? "-" : f.format(FMT);
    }

    public static String icono(String cat) {
        String c = cat == null ? "" : cat.toLowerCase();
        if (c.contains("analg")) return "bi-capsule";
        if (c.contains("vitam")) return "bi-brightness-high";
        if (c.contains("cuidado")) return "bi-droplet-half";
        if (c.contains("primeros")) return "bi-bandaid";
        if (c.contains("beb")) return "bi-balloon-heart";
        if (c.contains("derm")) return "bi-stars";
        return "bi-prescription2";
    }

    public static String color(String cat) {
        String c = cat == null ? "" : cat.toLowerCase();
        if (c.contains("analg")) return "#e76f51";
        if (c.contains("vitam")) return "#f4a261";
        if (c.contains("cuidado")) return "#2a9d8f";
        if (c.contains("primeros")) return "#e63946";
        if (c.contains("beb")) return "#6c8ebf";
        if (c.contains("derm")) return "#9b72cf";
        return "#0e8f6e";
    }

    /** Clase de color de Bootstrap según el estado de pedido o reclamo. */
    public static String badge(String estado) {
        if (estado == null) return "secondary";
        return switch (estado) {
            case "ESCALADO" -> "danger";
            case "PENDIENTE", "ABIERTO" -> "warning text-dark";
            case "PAGADO" -> "info text-dark";
            case "EN_DESPACHO", "ENVIADO", "EN_PROCESO" -> "primary";
            case "ENTREGADO", "RESUELTO" -> "success";
            default -> "secondary";
        };
    }

    public static String estadoLegible(String estado) {
        return estado == null ? "" : estado.replace('_', ' ');
    }
}
