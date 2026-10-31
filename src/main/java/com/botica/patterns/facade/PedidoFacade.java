package com.botica.patterns.facade;

import com.botica.dao.DetallePedidoDAO;
import com.botica.dao.PedidoDAO;
import com.botica.dao.ProductoDAO;
import com.botica.dao.factory.DAOFactory;
import com.botica.model.DetallePedido;
import com.botica.model.Pedido;
import com.botica.model.Producto;
import com.botica.patterns.PedidoSujeto;
import com.botica.service.PedidoService;
import com.botica.service.ReclamoService;
import com.botica.util.ConexionBD;
import com.botica.util.Constantes;
import com.botica.util.ReglaNegocioException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Patrón Facade (Estructural): interfaz simple para las operaciones que combinan varios servicios y DAO.
 * - realizarPedido: pedido + detalle + descuento de stock en UNA transacción, y aviso a los observadores.
 * - avanzarPedido y registrarReclamo: delegan en los servicios correspondientes.
 */
public class PedidoFacade {
    private final PedidoDAO pedidoDAO;
    private final ProductoDAO productoDAO;
    private final DetallePedidoDAO detallePedidoDAO;
    private final PedidoSujeto notificador;
    private final PedidoService pedidoService;
    private final ReclamoService reclamoService;

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
                if (producto.getStock() < item.getCantidad()) {
                    throw new ReglaNegocioException("Stock insuficiente para " + producto.getNombre());
                }

                DetallePedido detalle = new DetallePedido();
                detalle.setIdPedido(nuevo.getId());
                detalle.setIdProducto(producto.getId());
                detalle.setCantidad(item.getCantidad());
                detalle.setSubtotal(producto.getPrecio() * item.getCantidad());
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

    public Pedido avanzarPedido(int idPedido) {
        return pedidoService.avanzarEstado(idPedido);
    }

    public void registrarReclamo(int idCliente, int idPedido, String descripcion) {
        reclamoService.crear(idCliente, idPedido, descripcion);
    }
}
