package com.botica.patterns.facade;

import com.botica.dao.DetallePedidoDAO;
import com.botica.dao.PagoDAO;
import com.botica.dao.PedidoDAO;
import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.DetallePedido;
import com.botica.model.Pago;
import com.botica.model.Pedido;
import com.botica.model.Producto;
import com.botica.patterns.PedidoSujeto;
import com.botica.service.PagoService;
import com.botica.service.PedidoService;
import com.botica.service.ReclamoService;
import com.botica.util.ConexionBD;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Locale;

/**
 * Patrón Facade (Estructural): interfaz simple para las operaciones que combinan varios servicios y DAO.
 * - realizarPedido: pedido + detalle + descuento de stock en UNA transacción, y aviso a los observadores.
 * - avanzarPedido, revertirPedido y registrarReclamo: delegan en los servicios correspondientes.
 */
public class PedidoFacade {
    private final PedidoDAO pedidoDAO;
    private final ProductoDAO productoDAO;
    private final DetallePedidoDAO detallePedidoDAO;
    private final PedidoSujeto notificador;
    private final PedidoService pedidoService;
    private final ReclamoService reclamoService;
    private final PagoService pagoService = new PagoService();
    private final PagoDAO pagoDAO = DAOFactory.getPagoDAO();

    public PedidoFacade() {
        this(DAOFactory.getPedidoDAO(), DAOFactory.getProductoDAO(), DAOFactory.getDetallePedidoDAO(),
                PedidoSujeto.crearPorDefecto(), new PedidoService(), new ReclamoService());
    }

    public PedidoFacade(PedidoDAO pedidoDAO, ProductoDAO productoDAO, DetallePedidoDAO detallePedidoDAO,
                        PedidoSujeto notificador, PedidoService pedidoService, ReclamoService reclamoService) {
        this.pedidoDAO = pedidoDAO;
        this.productoDAO = productoDAO;
        this.detallePedidoDAO = detallePedidoDAO;
        this.notificador = notificador;
        this.pedidoService = pedidoService;
        this.reclamoService = reclamoService;
    }

    public Pedido realizarPedido(int idCliente, List<ItemCarrito> items) {
        if (items == null || items.isEmpty()) {
            throw new ReglaNegocioException("El carrito está vacío");
        }
        for (ItemCarrito item : items) {
            if (item.getCantidad() <= 0) {
                throw new ReglaNegocioException("La cantidad debe ser mayor que cero");
            }
        }

        Pedido pedido = ConexionBD.getInstancia().ejecutarTransaccion(conn -> {
            Pedido nuevo = new Pedido(idCliente, Constantes.PENDIENTE, 0);
            pedidoDAO.crear(nuevo, conn);

            List<DetallePedido> detalles = new ArrayList<>();
            for (ItemCarrito item : items) {
                Producto producto = productoDAO.buscarPorIdParaActualizar(item.getIdProducto(), conn);
                if (producto == null) {
                    throw new ReglaNegocioException("Producto no encontrado: " + item.getIdProducto());
                }
                if (!producto.isActivo()) {
                    throw new ReglaNegocioException("El producto " + producto.getNombre() + " ya no está disponible");
                }
                if (producto.getStock() < item.getCantidad()) {
                    throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre());
                }

                DetallePedido detalle = new DetallePedido();
                detalle.setIdPedido(nuevo.getId());
                detalle.setIdProducto(producto.getId());
                detalle.setCantidad(item.getCantidad());
                detalle.setSubtotal(producto.getPrecioFinal() * item.getCantidad());
                detallePedidoDAO.crear(detalle, conn);
                detalles.add(detalle);

                productoDAO.descontarStock(producto.getId(), item.getCantidad(), conn);
            }

            nuevo.calcularTotal(detalles);
            pedidoDAO.actualizar(nuevo, conn);
            return nuevo;
        });

        notificador.notificar("Tu pedido #" + pedido.getId() + " fue registrado por S/ "
                + String.format(Locale.US, "%.2f", pedido.getTotal()));
        return pedido;
    }

    /** El cliente paga un pedido pendiente con el método elegido (pago simulado). */
    public Pago pagarPedido(int idCliente, int idPedido, String metodo, String dato) {
        return pagoService.pagar(idCliente, idPedido, metodo, dato);
    }

    /** El cliente cancela un pedido que aún no pagó: el pedido pasa a CANCELADO y el stock vuelve al inventario. */
    public Pedido cancelarPedido(int idCliente, int idPedido) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null || pedido.getIdCliente() != idCliente) {
            throw new ReglaNegocioException("Pedido no válido");
        }
        if (!Constantes.PENDIENTE.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("Solo se pueden cancelar pedidos pendientes de pago");
        }
        List<DetallePedido> detalles = detallePedidoDAO.listarPorPedido(idPedido);
        ConexionBD.getInstancia().ejecutarTransaccion(conn -> {
            for (DetallePedido detalle : detalles) {
                productoDAO.sumarStock(detalle.getIdProducto(), detalle.getCantidad(), conn);
            }
            pedido.setEstado(Constantes.CANCELADO);
            pedidoDAO.actualizar(pedido, conn);
            return pedido;
        });
        notificador.notificar("Tu pedido #" + idPedido + " fue cancelado");
        return pedido;
    }

    /**
     * Soporte reembolsa un pedido ya pagado que todavía no salió del almacén (PAGADO o EN_DESPACHO):
     * el pedido pasa a CANCELADO, el stock vuelve al inventario y se registra un pago negativo (REEMBOLSO).
     */
    public Pago reembolsarPedido(int idPedido) {
        Pedido previo = exigirReembolsable(idPedido);
        List<DetallePedido> detalles = detallePedidoDAO.listarPorPedido(idPedido);
        Pago reembolso = new Pago(idPedido, Constantes.PAGO_REEMBOLSO, -previo.getTotal(),
                "RF-" + ThreadLocalRandom.current().nextInt(10_000_000, 100_000_000));
        ConexionBD.getInstancia().ejecutarTransaccion(conn -> {
            Pedido actual = exigirReembolsable(idPedido); // por si cambió mientras tanto
            for (DetallePedido detalle : detalles) {
                productoDAO.sumarStock(detalle.getIdProducto(), detalle.getCantidad(), conn);
            }
            actual.setEstado(Constantes.CANCELADO);
            pedidoDAO.actualizar(actual, conn);
            pagoDAO.crear(reembolso, conn);
            return actual;
        });
        notificador.notificar("Tu pedido #" + idPedido + " fue reembolsado");
        return reembolso;
    }

    private Pedido exigirReembolsable(int idPedido) {
        Pedido pedido = pedidoDAO.buscarPorId(idPedido);
        if (pedido == null) {
            throw new ReglaNegocioException("El pedido no existe");
        }
        if (!Constantes.PAGADO.equals(pedido.getEstado()) && !Constantes.EN_DESPACHO.equals(pedido.getEstado())) {
            throw new ReglaNegocioException("Solo se puede reembolsar un pedido pagado que aún no fue enviado");
        }
        return pedido;
    }

    public Pedido avanzarPedido(int idPedido) {
        return pedidoService.avanzarEstado(idPedido);
    }

    public Pedido revertirPedido(int idPedido) {
        return pedidoService.revertirEstado(idPedido);
    }

    public void registrarReclamo(int idCliente, int idPedido, String descripcion) {
        reclamoService.crear(idCliente, idPedido, descripcion);
    }
}
