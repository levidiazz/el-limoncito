INSERT INTO rol (nombre, descripcion) VALUES
    ('ADMINISTRADOR', 'Equipo de desarrollo: usuarios, roles y respaldos'),
    ('GERENTE_GENERAL', 'Dueño del negocio: catálogos, reportes y predicción'),
    ('VENTAS', 'Cajero del punto de venta'),
    ('ALMACEN', 'Encargado de almacén: entradas, mermas y envases');

INSERT INTO usuario (id_rol, nombre_completo, usuario, contrasena_hash) VALUES
    ((SELECT id_rol FROM rol WHERE nombre = 'ADMINISTRADOR'), 'Equipo de desarrollo', 'admin', '$2a$10$Y32ZCZjqfMB/7QbnWNvB2u3gkhNrpQYKY43TWqEqccpgT5YDQKoly'),
    ((SELECT id_rol FROM rol WHERE nombre = 'GERENTE_GENERAL'), 'Gerente General', 'gerente', '$2a$10$Y32ZCZjqfMB/7QbnWNvB2u3gkhNrpQYKY43TWqEqccpgT5YDQKoly'),
    ((SELECT id_rol FROM rol WHERE nombre = 'VENTAS'), 'Cajero', 'cajero', '$2a$10$Y32ZCZjqfMB/7QbnWNvB2u3gkhNrpQYKY43TWqEqccpgT5YDQKoly'),
    ((SELECT id_rol FROM rol WHERE nombre = 'ALMACEN'), 'Encargado de almacén', 'almacen', '$2a$10$Y32ZCZjqfMB/7QbnWNvB2u3gkhNrpQYKY43TWqEqccpgT5YDQKoly');

INSERT INTO categoria (nombre) VALUES
    ('Cerveza'),
    ('Refrescos'),
    ('Abarrotes'),
    ('Botanas'),
    ('Lácteos');

INSERT INTO tipo_envase (nombre, valor_deposito, vacios_en_bodega) VALUES
    ('Caja de cerveza 12 botellas', 60.00, 0),
    ('Caja de cerveza 20 botellas', 100.00, 0),
    ('Caja de refresco 24 botellas', 72.00, 0);
