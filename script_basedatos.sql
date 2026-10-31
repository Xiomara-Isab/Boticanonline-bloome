CREATE DATABASE botica_db;

CREATE TABLE usuario (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(100) NOT NULL,
  correo VARCHAR(100) UNIQUE NOT NULL,
  contrasena VARCHAR(255) NOT NULL,
  rol VARCHAR(30) NOT NULL
);

CREATE TABLE producto (
  id SERIAL PRIMARY KEY,
  nombre VARCHAR(150) NOT NULL,
  descripcion TEXT,
  precio NUMERIC(10,2) NOT NULL,
  stock INT NOT NULL DEFAULT 0,
  categoria VARCHAR(50),
  fecha_vencimiento DATE,
  id_vendedor INT REFERENCES usuario(id)
);

CREATE TABLE pedido (
  id SERIAL PRIMARY KEY,
  id_cliente INT REFERENCES usuario(id),
  fecha_pedido TIMESTAMP DEFAULT NOW(),
  estado VARCHAR(30) NOT NULL DEFAULT 'PENDIENTE',
  total NUMERIC(10,2) NOT NULL DEFAULT 0
);

CREATE TABLE detalle_pedido (
  id SERIAL PRIMARY KEY,
  id_pedido INT REFERENCES pedido(id),
  id_producto INT REFERENCES producto(id),
  cantidad INT NOT NULL,
  subtotal NUMERIC(10,2) NOT NULL
);

CREATE TABLE reclamo (
  id SERIAL PRIMARY KEY,
  id_pedido INT REFERENCES pedido(id),
  descripcion TEXT,
  estado VARCHAR(30) DEFAULT 'ABIERTO',
  fecha_creacion TIMESTAMP DEFAULT NOW()
);



INSERT INTO usuario (nombre, correo, contrasena, rol) VALUES
  ('Ana Cliente', 'cliente@botica.com', 'fi+M78iwFem6cz5z+nb42w==:PPoVdos7KrePYGNicDtybbghz+4kq1t4OXZhQuOOyck=', 'Cliente'),
  ('Carlos Vendedor', 'vendedor@botica.com', 'm4ldbJHahzrDqzDgFJkM+w==:8/f9Ukgal3t1BtQ1d//kZOVz6K8ivI7WWd3vvaTTHf8=', 'Vendedor'),
  ('Luisa Almacén', 'almacen@botica.com', 'XTh2QlNrDDQqNDbqX2puqg==:4pEYjvSIJkfvYftYZ6kG0QN2cXJTZAPOnvJEk5fT28o=', 'Almacen'),
  ('Sofía Soporte', 'soporte@botica.com', 'oEgBTWtLUeW9M6z+beMnqQ==:02HinVABUyVQfLSR5153APvlH9rbHzxFSb+OAxWxXCE=', 'Soporte'),
  ('Admin Botica', 'admin@botica.com', '9/Q4bsrCkZxlofNlzHhj1A==:DAaeBsyJ6Pm8QYH94Q+mexsS8N6VMY9Fq6D1HW26Yrw=', 'Administrador');

INSERT INTO producto (nombre, descripcion, precio, stock, categoria, fecha_vencimiento, id_vendedor) VALUES
  ('Paracetamol 500 mg x 20', 'Alivio de dolor leve a moderado y fiebre.', 8.50, 120, 'Analgésicos', CURRENT_DATE + 540, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Ibuprofeno 400 mg x 10', 'Antiinflamatorio para dolor muscular y de cabeza.', 12.90, 8, 'Analgésicos', CURRENT_DATE + 400, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Vitamina C 1000 mg x 30', 'Refuerza las defensas del organismo.', 24.00, 60, 'Vitaminas', CURRENT_DATE + 60, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Multivitamínico Adultos x 60', 'Complemento diario de vitaminas y minerales.', 39.90, 35, 'Vitaminas', CURRENT_DATE + 700, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Alcohol medicinal 250 ml', 'Antiséptico de uso externo.', 5.50, 90, 'Primeros auxilios', CURRENT_DATE + 900, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Gasas estériles x 10', 'Para curación de heridas.', 6.80, 5, 'Primeros auxilios', NULL, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Curitas surtidas x 50', 'Protección rápida para pequeñas heridas.', 7.90, 75, 'Primeros auxilios', NULL, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Shampoo anticaspa 400 ml', 'Control de caspa y cuidado del cuero cabelludo.', 22.50, 40, 'Cuidado personal', CURRENT_DATE + 800, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Protector solar FPS 50', 'Protección UVA/UVB para rostro y cuerpo.', 45.00, 25, 'Dermocosmética', CURRENT_DATE + 600, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Crema hidratante facial', 'Hidratación profunda para todo tipo de piel.', 36.50, 18, 'Dermocosmética', CURRENT_DATE + 75, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Pañales etapa 3 x 40', 'Máxima absorción para bebés de 6 a 10 kg.', 52.90, 30, 'Bebés', NULL, (SELECT id FROM usuario WHERE correo='vendedor@botica.com')),
  ('Crema para coceduras 100 g', 'Protege la piel sensible del bebé.', 14.20, 0, 'Bebés', CURRENT_DATE + 365, (SELECT id FROM usuario WHERE correo='vendedor@botica.com'));

-- Pedidos de ejemplo del cliente demo
INSERT INTO pedido (id_cliente, fecha_pedido, estado, total) VALUES
  ((SELECT id FROM usuario WHERE correo='cliente@botica.com'), NOW() - INTERVAL '6 days', 'ENTREGADO', 33.40),
  ((SELECT id FROM usuario WHERE correo='cliente@botica.com'), NOW() - INTERVAL '3 days', 'ENVIADO', 48.00),
  ((SELECT id FROM usuario WHERE correo='cliente@botica.com'), NOW() - INTERVAL '1 day', 'PAGADO', 45.00),
  ((SELECT id FROM usuario WHERE correo='cliente@botica.com'), NOW() - INTERVAL '2 hours', 'PENDIENTE', 12.90);

INSERT INTO detalle_pedido (id_pedido, id_producto, cantidad, subtotal) VALUES
  ((SELECT MIN(id) FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Paracetamol%'), 2, 17.00),
  ((SELECT MIN(id) FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Alcohol%'), 1, 5.50),
  ((SELECT MIN(id) FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Gasas%'), 1, 6.80),
  ((SELECT MIN(id) FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Curitas%'), 1, 7.90),
  ((SELECT MIN(id) + 1 FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Vitamina C%'), 2, 48.00),
  ((SELECT MIN(id) + 2 FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Protector solar%'), 1, 45.00),
  ((SELECT MIN(id) + 3 FROM pedido), (SELECT id FROM producto WHERE nombre LIKE 'Ibuprofeno%'), 1, 12.90);

INSERT INTO reclamo (id_pedido, descripcion, estado) VALUES
  ((SELECT MIN(id) + 1 FROM pedido), 'Mi pedido sigue en camino y aún no llega, ¿pueden confirmar la fecha de entrega?', 'ABIERTO'),
  ((SELECT MIN(id) FROM pedido), 'Una de las cajas llegó con el empaque golpeado.', 'RESUELTO');
