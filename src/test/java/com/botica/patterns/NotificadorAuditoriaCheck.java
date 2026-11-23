package com.botica.patterns;

import com.botica.dao.AuditoriaDAO;
import com.botica.model.Auditoria;
import com.botica.model.Usuario;
import com.botica.service.AuditoriaService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Comprobación sin base de datos del observador de auditoría y del CSV.
 * Ejecutar con: java -ea -cp "target/classes:target/test-classes" com.botica.patterns.NotificadorAuditoriaCheck
 */
public class NotificadorAuditoriaCheck {
    public static void main(String[] args) {
        List<Auditoria> guardados = new ArrayList<>();
        AuditoriaDAO dao = new AuditoriaDAO() {
            @Override public void registrar(Auditoria a) { guardados.add(a); }
            @Override public List<Auditoria> listar(LocalDate d, LocalDate h, String u, String a, int l) { return guardados; }
        };
        Usuario almacen = new Usuario("Luisa", "almacen@botica.com", "x", "Almacen");
        almacen.setId(3);
        AuditoriaService.fijarContexto(almacen, "10.0.0.7");

        NotificadorAuditoria n = new NotificadorAuditoria(new AuditoriaService(dao));
        // Mensajes tal como los emiten PedidoFacade, PagoService y PedidoService
        String[][] casos = {
                {"Tu pedido #12 fue registrado por S/ 33.40", "COMPRA"},
                {"Pago aprobado del pedido #12 por S/ 33.40 (Yape)", "PAGO"},
                {"Tu pedido #12 cambió a EN_DESPACHO", "AVANCE"},
                {"Tu pedido #12 volvió a PAGADO", "REVERSION"},
                {"Tu pedido #12 fue reembolsado", "REEMBOLSO"},
                {"Tu pedido #12 fue cancelado", "CANCELACION"},
        };
        for (String[] c : casos) n.notificar(c[0]);
        for (int i = 0; i < casos.length; i++) {
            Auditoria a = guardados.get(i);
            assert casos[i][1].equals(a.getAccion()) : casos[i][0] + " -> " + a.getAccion();
            assert a.getIdEntidad() == 12 && "PEDIDO".equals(a.getEntidad());
            assert a.getIdUsuario() == 3 && "Almacen".equals(a.getRol()) && "10.0.0.7".equals(a.getIp());
        }
        AuditoriaService.limpiarContexto();

        Auditoria malicioso = new Auditoria(null, null, "LOGIN_FALLIDO", "USUARIO", null, "=HYPERLINK(\"x\")", "1.2.3.4");
        malicioso.setFecha(LocalDateTime.of(2026, 1, 2, 3, 4));
        String csv = AuditoriaService.csv(List.of(malicioso));
        assert csv.contains("\"'=HYPERLINK(\"\"x\"\")\"") : "neutraliza fórmulas y escapa comillas: " + csv;
        assert csv.contains("\"02/01/2026 03:04\"");
        System.out.println("NotificadorAuditoriaCheck OK");
    }
}
