# BoticaOnline

Java 17 · Servlets/JSP (Jakarta) · JDBC · PostgreSQL · Maven (WAR)

## Puesta en marcha
1. Ejecutar `script_basedatos.sql` en PostgreSQL (usuarios demo, contraseña `Botica123`:
   cliente@, vendedor@, almacen@, soporte@, admin@ + `botica.com`).
2. (Opcional) Variables de entorno: `BOTICA_DB_URL`, `BOTICA_DB_USER`, `BOTICA_DB_PASSWORD`
   (por defecto: localhost:5432/botica_db, postgres/admin).
3. `mvn clean package` y desplegar el WAR en Tomcat 10+.

## Arquitectura
view (JSP) -> controller -> service -> dao (JDBC) -> PostgreSQL

Patrones: Singleton (ConexionBD), Factory (DAOFactory), Facade (PedidoFacade),
Observer (PedidoSujeto/Notificador*), State (EstadoPedido*),
Command (patterns/command: deshacer cambios de estado de reclamos),
Memento (PedidoMemento, patterns/memento: revertir el avance de un pedido), GRASP Controlador y Experto.
Diagrama de clases: `docs/diagrama-clases.mmd` (pegar en https://mermaid.live).
