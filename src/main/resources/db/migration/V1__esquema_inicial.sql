CREATE TABLE rol (
    id_rol      SERIAL PRIMARY KEY,
    nombre      VARCHAR(30)  NOT NULL UNIQUE,
    descripcion VARCHAR(150)
);

CREATE TABLE usuario (
    id_usuario        SERIAL PRIMARY KEY,
    id_rol            INT          NOT NULL REFERENCES rol (id_rol),
    nombre_completo   VARCHAR(100) NOT NULL,
    usuario           VARCHAR(30)  NOT NULL UNIQUE,
    contrasena_hash   VARCHAR(255) NOT NULL,
    intentos_fallidos SMALLINT     NOT NULL DEFAULT 0 CHECK (intentos_fallidos >= 0),
    bloqueado         BOOLEAN      NOT NULL DEFAULT FALSE,
    activo            BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE bitacora (
    id_bitacora SERIAL PRIMARY KEY,
    id_usuario  INT         NOT NULL REFERENCES usuario (id_usuario),
    accion      VARCHAR(50) NOT NULL,
    detalle     TEXT,
    fecha_hora  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE categoria (
    id_categoria SERIAL PRIMARY KEY,
    nombre       VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE tipo_envase (
    id_tipo_envase   SERIAL PRIMARY KEY,
    nombre           VARCHAR(50)   NOT NULL,
    valor_deposito   NUMERIC(10,2) NOT NULL CHECK (valor_deposito >= 0),
    vacios_en_bodega INT           NOT NULL DEFAULT 0 CHECK (vacios_en_bodega >= 0)
);

CREATE TABLE producto (
    id_producto          SERIAL PRIMARY KEY,
    id_categoria         INT           NOT NULL REFERENCES categoria (id_categoria),
    id_tipo_envase       INT           REFERENCES tipo_envase (id_tipo_envase),
    codigo_barras        VARCHAR(30)   NOT NULL UNIQUE,
    nombre               VARCHAR(100)  NOT NULL,
    presentacion         VARCHAR(50)   NOT NULL,
    precio_menudeo       NUMERIC(10,2) NOT NULL CHECK (precio_menudeo >= 0),
    precio_mayoreo       NUMERIC(10,2) NOT NULL CHECK (precio_mayoreo >= 0),
    cantidad_min_mayoreo INT           NOT NULL CHECK (cantidad_min_mayoreo >= 1),
    tasa_iva             NUMERIC(4,2)  NOT NULL DEFAULT 0.16 CHECK (tasa_iva >= 0),
    stock_minimo         INT           NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    retornable           BOOLEAN       NOT NULL DEFAULT FALSE,
    activo               BOOLEAN       NOT NULL DEFAULT TRUE
);

CREATE TABLE proveedor (
    id_proveedor SERIAL PRIMARY KEY,
    nombre       VARCHAR(100) NOT NULL,
    telefono     VARCHAR(10)
);

CREATE TABLE cliente (
    id_cliente SERIAL PRIMARY KEY,
    nombre     VARCHAR(100) NOT NULL,
    telefono   VARCHAR(10),
    perfil     VARCHAR(15)  NOT NULL CHECK (perfil IN ('MINORISTA', 'MAYORISTA', 'DISTRIBUIDOR')),
    activo     BOOLEAN      NOT NULL DEFAULT TRUE
);

CREATE TABLE venta (
    id_venta          SERIAL PRIMARY KEY,
    id_cliente        INT           REFERENCES cliente (id_cliente),
    id_usuario        INT           NOT NULL REFERENCES usuario (id_usuario),
    fecha_hora        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    subtotal          NUMERIC(12,2) NOT NULL,
    iva               NUMERIC(12,2) NOT NULL,
    deposito_envases  NUMERIC(10,2) NOT NULL DEFAULT 0,
    total             NUMERIC(12,2) NOT NULL,
    efectivo_recibido NUMERIC(12,2) NOT NULL,
    cambio            NUMERIC(12,2) NOT NULL
);

CREATE TABLE detalle_venta (
    id_detalle      SERIAL PRIMARY KEY,
    id_venta        INT           NOT NULL REFERENCES venta (id_venta),
    id_producto     INT           NOT NULL REFERENCES producto (id_producto),
    cantidad        INT           NOT NULL CHECK (cantidad > 0),
    precio_aplicado NUMERIC(10,2) NOT NULL,
    tipo_precio     VARCHAR(10)   NOT NULL CHECK (tipo_precio IN ('MENUDEO', 'MAYOREO')),
    importe         NUMERIC(12,2) NOT NULL
);

CREATE TABLE comprobante (
    id_comprobante SERIAL PRIMARY KEY,
    id_venta       INT         NOT NULL UNIQUE REFERENCES venta (id_venta),
    folio          VARCHAR(20) NOT NULL UNIQUE,
    fecha_emision  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE entrada_mercancia (
    id_entrada    SERIAL PRIMARY KEY,
    id_proveedor  INT           NOT NULL REFERENCES proveedor (id_proveedor),
    id_usuario    INT           NOT NULL REFERENCES usuario (id_usuario),
    folio_nota    VARCHAR(30)   NOT NULL,
    fecha_entrada TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    total         NUMERIC(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE lote (
    id_lote             SERIAL PRIMARY KEY,
    id_entrada          INT           NOT NULL REFERENCES entrada_mercancia (id_entrada),
    id_producto         INT           NOT NULL REFERENCES producto (id_producto),
    cantidad_inicial    INT           NOT NULL CHECK (cantidad_inicial > 0),
    cantidad_disponible INT           NOT NULL CHECK (cantidad_disponible >= 0 AND cantidad_disponible <= cantidad_inicial),
    costo_unitario      NUMERIC(10,2) NOT NULL CHECK (costo_unitario >= 0),
    fecha_entrada       TIMESTAMP     NOT NULL,
    fecha_caducidad     DATE
);

CREATE TABLE movimiento_inventario (
    id_movimiento SERIAL PRIMARY KEY,
    id_lote       INT          NOT NULL REFERENCES lote (id_lote),
    id_usuario    INT          NOT NULL REFERENCES usuario (id_usuario),
    id_venta      INT          REFERENCES venta (id_venta),
    tipo          VARCHAR(10)  NOT NULL CHECK (tipo IN ('ENTRADA', 'VENTA', 'MERMA', 'AJUSTE')),
    cantidad      INT          NOT NULL CHECK (cantidad > 0),
    motivo        VARCHAR(150),
    fecha_hora    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE movimiento_envase (
    id_mov_envase  SERIAL PRIMARY KEY,
    id_tipo_envase INT           NOT NULL REFERENCES tipo_envase (id_tipo_envase),
    id_usuario     INT           NOT NULL REFERENCES usuario (id_usuario),
    id_venta       INT           REFERENCES venta (id_venta),
    id_cliente     INT           REFERENCES cliente (id_cliente),
    tipo           VARCHAR(20)   NOT NULL CHECK (tipo IN ('ENTREGA_PROVEEDOR', 'COBRO_DEPOSITO', 'DEVOLUCION_CLIENTE')),
    cantidad       INT           NOT NULL CHECK (cantidad > 0),
    monto          NUMERIC(10,2) NOT NULL DEFAULT 0,
    fecha_hora     TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE modelo_prediccion (
    id_modelo           SERIAL PRIMARY KEY,
    algoritmo           VARCHAR(50)   NOT NULL,
    version             VARCHAR(20)   NOT NULL,
    fecha_entrenamiento TIMESTAMP     NOT NULL,
    datos_desde         DATE          NOT NULL,
    datos_hasta         DATE          NOT NULL,
    error_mae           NUMERIC(10,4),
    activo              BOOLEAN       NOT NULL DEFAULT FALSE
);

CREATE TABLE prediccion (
    id_prediccion     SERIAL PRIMARY KEY,
    id_modelo         INT           NOT NULL REFERENCES modelo_prediccion (id_modelo),
    id_producto       INT           NOT NULL REFERENCES producto (id_producto),
    fecha_objetivo    DATE          NOT NULL,
    cantidad_estimada NUMERIC(10,2) NOT NULL,
    limite_inferior   NUMERIC(10,2),
    limite_superior   NUMERIC(10,2),
    fecha_generacion  TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE sugerencia_pedido (
    id_sugerencia     SERIAL PRIMARY KEY,
    id_prediccion     INT         NOT NULL UNIQUE REFERENCES prediccion (id_prediccion),
    id_proveedor      INT         NOT NULL REFERENCES proveedor (id_proveedor),
    cantidad_sugerida INT         NOT NULL CHECK (cantidad_sugerida >= 0),
    estado            VARCHAR(15) NOT NULL DEFAULT 'PENDIENTE' CHECK (estado IN ('PENDIENTE', 'ACEPTADA', 'DESCARTADA')),
    fecha_generacion  TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bitacora_fecha ON bitacora (fecha_hora);
CREATE INDEX idx_producto_nombre ON producto (nombre);
CREATE INDEX idx_lote_producto ON lote (id_producto, fecha_caducidad, fecha_entrada);
CREATE INDEX idx_movimiento_lote ON movimiento_inventario (id_lote);
CREATE INDEX idx_movimiento_tipo_fecha ON movimiento_inventario (tipo, fecha_hora);
CREATE INDEX idx_venta_fecha ON venta (fecha_hora);

CREATE VIEW v_existencia_producto AS
SELECT p.id_producto,
       p.nombre,
       COALESCE(SUM(l.cantidad_disponible), 0) AS existencia,
       p.stock_minimo
FROM producto p
LEFT JOIN lote l ON l.id_producto = p.id_producto
WHERE p.activo
GROUP BY p.id_producto, p.nombre, p.stock_minimo;
