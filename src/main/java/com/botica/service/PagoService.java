package com.botica.service;

import com.botica.dao.PagoDAO;
import com.botica.dao.PedidoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.Pago;
import com.botica.model.Pedido;
import com.botica.patterns.EstadoPedidoFactory;
import com.botica.patterns.PedidoSujeto;
import com.botica.patterns.pago.MetodoPago;
import com.botica.patterns.pago.MetodoPagoFactory;
import com.botica.util.ConexionBD;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import java.util.Locale;

/**
 * Regla de negocio del pago: el cobro lo hace la estrategia elegida (Strategy), el pedido avanza con
 * el patrón State (PENDIENTE -> PAGADO) y el pago + el cambio de estado se guardan en UNA transacción.
 */
public class PagoService {
    private final PedidoDAO pedidoDAO;
    private final PagoDAO pagoDAO;
    private final PedidoSujeto notificador;

    public PagoService() {
        this(DAOFactory.getPedidoDAO(), DAOFactory.getPagoDAO(), PedidoSujeto.crearPorDefecto());
    }

    public PagoService(PedidoDAO pedidoDAO, PagoDAO pagoDAO, PedidoSujeto notificador) {
        this.pedidoDAO = pedidoDAO;
        this.pagoDAO = pagoDAO;
        this.notificador = notificador;
    }

    public Pago pagar(int idCliente, int idPedido, String codigoMetodo, String dato) {
        Pedido previo = exigirPendiente(idCliente, idPedido);
        MetodoPago metodo = MetodoPagoFactory.desde(codigoMetodo);
        String referencia = metodo.procesar(previo.getTotal(), dato);
        Pago pago = new Pago(idPedido, metodo.codigo(), previo.getTotal(), referencia);

        ConexionBD.getInstancia().ejecutarTransaccion(conn -> {
            Pedido actual = exigirPendiente(idCliente, idPedido); // por si cambió mientras se procesaba el cobro
            pagoDAO.crear(pago, conn);
            EstadoPedidoFactory.desde(actual.getEstado()).avanzar(actual);
            pedidoDAO.actualizar(actual, conn);
            return actual;
        });

        notificador.notificar("Pago aprobado del pedido #" + idPedido + " por S/ "
                + String.format(Locale.US, "%.2f", pago.getMonto()) + " (" + metodo.etiqueta() + ")");
        return pago;
    }

    private Pedido exigirPendiente(int idCliente, int idPedido) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null || pedido.getIdCliente() != idCliente) {
            throw new ReglaNegocioException("Pedido no válido");
        }
        if (!Constantes.PENDIENTE.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("El pedido #" + idPedido + " ya no está pendiente de pago");
        }
        return pedido;
    }
}
