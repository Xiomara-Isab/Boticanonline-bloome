package com.botica.dao;

import com.botica.model.Reclamo;
import java.util.List;

public interface ReclamoDAO {
    void crear(Reclamo r);
    Reclamo buscarPorId(int id);
    List<Reclamo> listarTodos();
    List<Reclamo> listarPorCliente(int idCliente);
    void actualizarEstado(int id, String estado);
    /** Guarda la respuesta de Soporte y toma el reclamo; si estaba ABIERTO pasa a EN_PROCESO. */
    void responder(int id, String respuesta, int idSoporte);
}
