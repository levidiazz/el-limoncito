DO $$
DECLARE
    id_cajero INT;
    id_venta_dia INT;
    dia DATE;
    factor_semana NUMERIC;
    factor_tendencia NUMERIC;
BEGIN
    IF EXISTS (SELECT 1 FROM producto WHERE codigo_barras = 'DEMO-001') THEN
        RETURN;
    END IF;

    PERFORM setseed(0.42);

    SELECT id_usuario INTO id_cajero FROM usuario WHERE usuario = 'cajero';

    INSERT INTO cliente (nombre, telefono, perfil) VALUES
        ('Abarrotes Doña Mary', '9511000001', 'MAYORISTA'),
        ('Cantina El Roble', '9511000002', 'DISTRIBUIDOR'),
        ('Juan Pérez López', '9511000003', 'MINORISTA')
    ON CONFLICT DO NOTHING;

    INSERT INTO producto (id_categoria, id_tipo_envase, codigo_barras, nombre, presentacion,
                          precio_menudeo, precio_mayoreo, cantidad_min_mayoreo, tasa_iva, stock_minimo, retornable)
    VALUES
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Cerveza'),
         (SELECT id_tipo_envase FROM tipo_envase WHERE nombre = 'Caja de cerveza 12 botellas'),
         'DEMO-001', 'Cerveza clara', 'Caja con 12 botellas de 355 ml', 380.00, 350.00, 5, 0.16, 10, TRUE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Cerveza'),
         (SELECT id_tipo_envase FROM tipo_envase WHERE nombre = 'Caja de cerveza 12 botellas'),
         'DEMO-002', 'Cerveza oscura', 'Caja con 12 botellas de 355 ml', 420.00, 390.00, 5, 0.16, 8, TRUE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Refrescos'),
         (SELECT id_tipo_envase FROM tipo_envase WHERE nombre = 'Caja de refresco 24 botellas'),
         'DEMO-003', 'Refresco de cola', 'Caja con 24 botellas de 600 ml', 195.00, 180.00, 5, 0.16, 10, TRUE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Abarrotes'), NULL,
         'DEMO-004', 'Arroz', 'Bolsa de 1 kg', 28.00, 25.00, 10, 0.00, 20, FALSE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Abarrotes'), NULL,
         'DEMO-005', 'Aceite vegetal', 'Botella de 900 ml', 48.00, 44.00, 6, 0.16, 12, FALSE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Botanas'), NULL,
         'DEMO-006', 'Papas fritas', 'Bolsa de 45 g', 18.00, 15.00, 12, 0.16, 24, FALSE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Botanas'), NULL,
         'DEMO-007', 'Galletas surtidas', 'Paquete de 12 piezas', 12.00, 10.00, 20, 0.16, 24, FALSE),
        ((SELECT id_categoria FROM categoria WHERE nombre = 'Lácteos'), NULL,
         'DEMO-008', 'Leche entera', 'Caja de 1 L', 26.00, 24.00, 12, 0.00, 18, FALSE);

    CREATE TEMP TABLE demanda_base (codigo_barras VARCHAR(30), base NUMERIC);
    INSERT INTO demanda_base VALUES
        ('DEMO-001', 6), ('DEMO-002', 3), ('DEMO-003', 7), ('DEMO-004', 14),
        ('DEMO-005', 6), ('DEMO-006', 22), ('DEMO-007', 16), ('DEMO-008', 18);

    FOR dia IN SELECT d::DATE FROM generate_series(DATE '2026-05-01', DATE '2026-11-16', INTERVAL '1 day') AS d LOOP
        factor_semana := CASE EXTRACT(DOW FROM dia)
                             WHEN 5 THEN 1.5
                             WHEN 6 THEN 1.8
                             WHEN 0 THEN 1.3
                             ELSE 1.0
                         END;
        factor_tendencia := 1 + (dia - DATE '2026-05-01') / 700.0;

        INSERT INTO venta (id_usuario, fecha_hora, subtotal, iva, deposito_envases, total, efectivo_recibido, cambio)
        VALUES (id_cajero, dia + TIME '20:00', 0, 0, 0, 0, 0, 0)
        RETURNING id_venta INTO id_venta_dia;

        INSERT INTO detalle_venta (id_venta, id_producto, cantidad, precio_aplicado, tipo_precio, importe)
        SELECT id_venta_dia, p.id_producto, q.cantidad, p.precio_menudeo, 'MENUDEO', q.cantidad * p.precio_menudeo
        FROM producto p
        JOIN demanda_base b ON b.codigo_barras = p.codigo_barras
        CROSS JOIN LATERAL (
            SELECT GREATEST(1, ROUND(b.base * factor_semana * factor_tendencia * (0.8 + random() * 0.4))::INT) AS cantidad
        ) q;

        UPDATE venta v
        SET subtotal = t.subtotal,
            iva = t.iva,
            total = t.subtotal + t.iva,
            efectivo_recibido = t.subtotal + t.iva
        FROM (
            SELECT d.id_venta,
                   SUM(d.importe) AS subtotal,
                   ROUND(SUM(d.importe * p.tasa_iva), 2) AS iva
            FROM detalle_venta d
            JOIN producto p ON p.id_producto = d.id_producto
            WHERE d.id_venta = id_venta_dia
            GROUP BY d.id_venta
        ) t
        WHERE v.id_venta = t.id_venta;
    END LOOP;

    DROP TABLE demanda_base;
END
$$;
