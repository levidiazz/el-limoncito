# Selección del modelo de predicción

Compara cinco modelos de predicción de ventas diarias por producto y elige el de menor error absoluto medio (MAE) sobre los últimos 28 días de historia.

Modelos: naive estacional, promedio móvil de 7 días, regresión lineal, Random Forest y Gradient Boosting.

## Preparar el entorno

```
cd ia
python3 -m venv .venv
source .venv/bin/activate
pip install -r requirements.txt
```

## Cargar los datos históricos de prueba

Arranca el sistema con el perfil `demo` para que Flyway cargue ocho productos, tres clientes y 200 días de ventas simuladas:

```
mvn spring-boot:run -Dspring-boot.run.profiles=demo
```

## Ejecutar

```
python seleccion_modelo.py
python seleccion_modelo.py --registrar
```

Con `--registrar` el modelo ganador queda activo en la tabla `modelo_prediccion`. Si se omite, solo se muestra la comparación.

Las variables de conexión son `DB_HOST`, `DB_PUERTO`, `DB_NOMBRE`, `DB_USUARIO` y `DB_CONTRASENA`; por omisión usan los mismos valores que la aplicación. También se puede pasar `--csv archivo.csv` con las columnas `fecha`, `id_producto` y `cantidad`.
