package com.botica.patterns.proxy;

import com.botica.model.Producto;
import com.botica.model.Usuario;
import com.botica.service.ProductoService;
import com.botica.util.Constantes;
import java.util.logging.Logger;

/**
 * Patrón Proxy (de protección): el guardia del catálogo. Tiene la misma interfaz que el servicio real,
 * revisa el rol del usuario antes de dejar pasar cada acción y registra quién lo intentó.
 */
public class ProductoServiceProxy implements ProductoService {
    private static final Logger REGISTRO = Logger.getLogger(ProductoServiceProxy.class.getName());

    private final ProductoService servicioReal;
    private final Usuario usuario;

    public ProductoServiceProxy(ProductoService servicioReal, Usuario usuario) {
        this.servicioReal = servicioReal;
        this.usuario = usuario;
    }

    @Override
    public void crear(Producto producto) {
        revisar("crear el producto " + producto.getNombre(), Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
        servicioReal.crear(producto);
    }

    @Override
    public void actualizarPrecioYStock(int idProducto, double precio, int stock) {
        revisar("editar el producto #" + idProducto, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
        servicioReal.actualizarPrecioYStock(idProducto, precio, stock);
    }

    @Override
    public void ajustarStock(int idProducto, int stock) {
        revisar("ajustar el stock del producto #" + idProducto, Constantes.ROL_ALMACEN, Constantes.ROL_ADMINISTRADOR);
        servicioReal.ajustarStock(idProducto, stock);
    }

    @Override
    public void eliminar(int idProducto) {
        revisar("eliminar el producto #" + idProducto, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
        servicioReal.eliminar(idProducto);
    }

    @Override
    public void cambiarActivo(int idProducto, boolean activo) {
        revisar((activo ? "activar" : "desactivar") + " el producto #" + idProducto,
                Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
        servicioReal.cambiarActivo(idProducto, activo);
    }

    @Override
    public void aplicarDescuento(int idProducto, int porcentaje) {
        revisar("aplicar un descuento al producto #" + idProducto, Constantes.ROL_VENDEDOR, Constantes.ROL_ADMINISTRADOR);
        servicioReal.aplicarDescuento(idProducto, porcentaje);
    }

    @Override
    public void registrarIngreso(int idProducto, int cantidad) {
        revisar("registrar el ingreso de mercadería del producto #" + idProducto,
                Constantes.ROL_ALMACEN, Constantes.ROL_ADMINISTRADOR);
        servicioReal.registrarIngreso(idProducto, cantidad);
    }

    @Override
    public int retirarVencidos() {
        revisar("retirar los productos vencidos", Constantes.ROL_ALMACEN, Constantes.ROL_ADMINISTRADOR);
        return servicioReal.retirarVencidos();
    }

    /** Deja constancia del intento y, si el rol no está entre los permitidos, corta el paso. */
    private void revisar(String accion, String... rolesPermitidos) {
        String quien = usuario.getCorreo() + " (" + usuario.getRol() + ")";
        for (String rol : rolesPermitidos) {
            if (rol.equals(usuario.getRol())) {
                REGISTRO.info(quien + " intentó " + accion + ": PERMITIDO");
                return;
            }
        }
        REGISTRO.warning(quien + " intentó " + accion + ": DENEGADO");
        throw new SecurityException("No tienes permiso para " + accion);
    }
}
