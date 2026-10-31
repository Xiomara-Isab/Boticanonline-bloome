package com.botica.dao.factory;

import com.botica.dao.DetallePedidoDAO;
import com.botica.dao.DetallePedidoDAOImpl;
import com.botica.dao.PedidoDAO;
import com.botica.dao.PedidoDAOImpl;
import com.botica.dao.ProductoDAO;
import com.botica.dao.ProductoDAOImpl;
import com.botica.dao.ReclamoDAO;
import com.botica.dao.ReclamoDAOImpl;
import com.botica.dao.UsuarioDAO;
import com.botica.dao.UsuarioDAOImpl;

public class DAOFactory {

    public static ProductoDAO getProductoDAO() {
        return new ProductoDAOImpl();
    }

    public static UsuarioDAO getUsuarioDAO() {
        return new UsuarioDAOImpl();
    }

    public static PedidoDAO getPedidoDAO() {
        return new PedidoDAOImpl();
    }

    public static DetallePedidoDAO getDetallePedidoDAO() {
        return new DetallePedidoDAOImpl();
    }

    public static ReclamoDAO getReclamoDAO() {
        return new ReclamoDAOImpl();
    }
}
