-- V2: las categorías pasan de texto suelto en producto a una tabla propia.
-- Ejecutar conectado a botica_db, después de script_basedatos.sql:
--   psql -U postgres -h localhost -d botica_db -f db/migraciones/V2_categoria.sql
-- Se puede ejecutar más de una vez.

CREATE TABLE IF NOT EXISTS categoria (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(50) UNIQUE NOT NULL,
  activa BOOLEAN NOT NULL DEFAULT TRUE
);

-- Un texto vacío no es una categoría: esos productos quedan sin categoría.
UPDATE producto SET categoria = NULL WHERE TRIM(categoria) = '';
UPDATE producto SET categoria = TRIM(categoria) WHERE categoria <> TRIM(categoria);

INSERT INTO categoria (nombre)
SELECT DISTINCT categoria FROM producto WHERE categoria IS NOT NULL
ON CONFLICT (nombre) DO NOTHING;

-- producto.categoria sigue guardando el nombre, así los DAO de producto no cambian.
-- ON UPDATE CASCADE: al renombrar una categoría, sus productos se actualizan solos.
-- Sin ON DELETE: la base no deja borrar una categoría que todavía tiene productos.
ALTER TABLE producto DROP CONSTRAINT IF EXISTS fk_producto_categoria;
ALTER TABLE producto ADD CONSTRAINT fk_producto_categoria
  FOREIGN KEY (categoria) REFERENCES categoria(nombre) ON UPDATE CASCADE;
