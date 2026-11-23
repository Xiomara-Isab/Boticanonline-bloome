package com.botica.dao;

import com.botica.model.Auditoria;
import java.time.LocalDate;
import java.util.List;

public interface AuditoriaDAO {
    void registrar(Auditoria a);

    /** Filtros opcionales (null o vacío = sin filtro); más recientes primero. */
    List<Auditoria> listar(LocalDate desde, LocalDate hasta, String usuario, String accion, int limite);
}
