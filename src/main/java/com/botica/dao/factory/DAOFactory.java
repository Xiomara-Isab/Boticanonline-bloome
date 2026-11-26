package com.botica.dao.factory;

import com.botica.dao.AuditoriaDAO;
import com.botica.dao.AuditoriaDAOImpl;
import com.botica.dao.CategoriaDAO;
import com.botica.dao.CategoriaDAOImpl;
import com.botica.dao.DetallePedidoDAO;
import com.botica.dao.DetallePedidoDAOImpl;
import com.botica.dao.PagoDAO;
import com.botica.dao.PagoDAOImpl;
import com.botica.dao.PedidoDAO;
import com.botica.dao.PedidoDAOImpl;
import com.botica.dao.ProductoDAO;
import com.botica.dao.ProductoDAOImpl;
import com.botica.dao.ReclamoDAO;
import com.botica.dao.ReclamoDAOImpl;
import com.botica.dao.ReporteDAO;
import com.botica.dao.ReporteDAOImpl;
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

    public static PagoDAO getPagoDAO() {
        return new PagoDAOImpl();
    }

    public static ReclamoDAO getReclamoDAO() {
        return new ReclamoDAOImpl();
    }

    public static AuditoriaDAO getAuditoriaDAO() {
        return new AuditoriaDAOImpl();
    }

    public static CategoriaDAO getCategoriaDAO() {
        return new CategoriaDAOImpl();
    }

    public static ReporteDAO getReporteDAO() {
        return new ReporteDAOImpl();
    }
}
