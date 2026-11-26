package com.botica.patterns;

import com.botica.service.AuditoriaService;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Observador concreto: deja en la bitácora de auditoría cada compra, pago, avance, reversión, cancelación y reembolso. */
public class NotificadorAuditoria implements NotificadorPedido {
    private static final Pattern ID_PEDIDO = Pattern.compile("#(\\d+)");
    private final AuditoriaService auditoria;

    public NotificadorAuditoria() {
        this(new AuditoriaService());
    }

    public NotificadorAuditoria(AuditoriaService auditoria) {
        this.auditoria = auditoria;
    }

    @Override
    public void notificar(String mensaje) {
        auditoria.registrar(accionDe(mensaje), "PEDIDO", idPedidoDe(mensaje), mensaje);
    }

    // shortcut: deduce la acción del texto del aviso; si cambian los mensajes de PedidoService/PagoService/PedidoFacade, actualizar aquí
    static String accionDe(String mensaje) {
        if (mensaje.startsWith("Pago aprobado")) return "PAGO";
        if (mensaje.contains("fue registrado")) return "COMPRA";
        if (mensaje.contains("cambió a")) return "AVANCE";
        if (mensaje.contains("volvió a")) return "REVERSION";
        if (mensaje.contains("fue reembolsado")) return "REEMBOLSO";
        if (mensaje.contains("fue cancelado")) return "CANCELACION";
        return "PEDIDO";
    }

    static Integer idPedidoDe(String mensaje) {
        Matcher m = ID_PEDIDO.matcher(mensaje);
        return m.find() ? Integer.valueOf(m.group(1)) : null;
    }
}
