import argparse
import os
import sys
from datetime import datetime

import numpy as np
import pandas as pd
from sklearn.ensemble import GradientBoostingRegressor, RandomForestRegressor
from sklearn.linear_model import LinearRegression

DIAS_DE_PRUEBA = 28
SEMILLA = 42

CONSULTA_VENTAS = """
    SELECT v.fecha_hora::date AS fecha, d.id_producto, SUM(d.cantidad) AS cantidad
    FROM detalle_venta d
    JOIN venta v ON v.id_venta = d.id_venta
    GROUP BY v.fecha_hora::date, d.id_producto
    ORDER BY fecha, d.id_producto
"""


def conectar():
    import psycopg2

    return psycopg2.connect(
        host=os.getenv("DB_HOST", "localhost"),
        port=os.getenv("DB_PUERTO", "5432"),
        dbname=os.getenv("DB_NOMBRE", "el_limoncito"),
        user=os.getenv("DB_USUARIO", "postgres"),
        password=os.getenv("DB_CONTRASENA", "postgres"),
    )


def cargar_ventas(ruta_csv):
    if ruta_csv:
        ventas = pd.read_csv(ruta_csv, parse_dates=["fecha"])
    else:
        with conectar() as conexion, conexion.cursor() as cursor:
            cursor.execute(CONSULTA_VENTAS)
            ventas = pd.DataFrame(cursor.fetchall(), columns=["fecha", "id_producto", "cantidad"])
        ventas["fecha"] = pd.to_datetime(ventas["fecha"])
    ventas["cantidad"] = ventas["cantidad"].astype(float)
    return ventas


def completar_calendario(ventas):
    dias = pd.date_range(ventas["fecha"].min(), ventas["fecha"].max(), freq="D")
    indice = pd.MultiIndex.from_product([dias, sorted(ventas["id_producto"].unique())],
                                        names=["fecha", "id_producto"])
    completo = (ventas.set_index(["fecha", "id_producto"])["cantidad"]
                .reindex(indice, fill_value=0.0)
                .reset_index())
    return completo.sort_values(["id_producto", "fecha"]).reset_index(drop=True)


def construir_variables(serie):
    grupo = serie.groupby("id_producto")["cantidad"]
    datos = serie.copy()
    datos["rezago_1"] = grupo.shift(1)
    datos["rezago_7"] = grupo.shift(7)
    datos["rezago_14"] = grupo.shift(14)
    datos["media_7"] = grupo.transform(lambda s: s.shift(1).rolling(7).mean())
    datos["media_28"] = grupo.transform(lambda s: s.shift(1).rolling(28).mean())
    datos["tendencia"] = (datos["fecha"] - datos["fecha"].min()).dt.days
    for dia in range(7):
        datos[f"dia_{dia}"] = (datos["fecha"].dt.dayofweek == dia).astype(int)
    datos = pd.concat([datos, pd.get_dummies(datos["id_producto"], prefix="producto", dtype=int)], axis=1)
    return datos.dropna().reset_index(drop=True)


def columnas_de_entrada(datos):
    base = ["rezago_1", "rezago_7", "rezago_14", "media_7", "media_28", "tendencia"]
    dias = [f"dia_{d}" for d in range(7)]
    productos = [c for c in datos.columns if c.startswith("producto_")]
    return base + dias + productos


def error_absoluto_medio(real, estimado):
    return float(np.mean(np.abs(np.asarray(real) - np.asarray(estimado))))


def evaluar(datos):
    corte = datos["fecha"].max() - pd.Timedelta(days=DIAS_DE_PRUEBA - 1)
    entrenamiento = datos[datos["fecha"] < corte]
    prueba = datos[datos["fecha"] >= corte]
    columnas = columnas_de_entrada(datos)
    real = prueba["cantidad"]

    resultados = {
        "Naive estacional (mismo día de la semana pasada)": error_absoluto_medio(real, prueba["rezago_7"]),
        "Promedio móvil de 7 días": error_absoluto_medio(real, prueba["media_7"]),
    }

    modelos = {
        "Regresión lineal": LinearRegression(),
        "Random Forest": RandomForestRegressor(n_estimators=200, min_samples_leaf=2, random_state=SEMILLA, n_jobs=-1),
        "Gradient Boosting": GradientBoostingRegressor(n_estimators=300, learning_rate=0.05, max_depth=3,
                                                       random_state=SEMILLA),
    }
    for nombre, modelo in modelos.items():
        modelo.fit(entrenamiento[columnas], entrenamiento["cantidad"])
        resultados[nombre] = error_absoluto_medio(real, modelo.predict(prueba[columnas]))
    return resultados


def mostrar(resultados):
    ancho = max(len(nombre) for nombre in resultados)
    print(f"\nError absoluto medio (MAE) en los últimos {DIAS_DE_PRUEBA} días, en unidades por producto y día\n")
    for nombre, mae in sorted(resultados.items(), key=lambda par: par[1]):
        print(f"  {nombre.ljust(ancho)}  {mae:8.3f}")


def registrar_en_base(algoritmo, mae, desde, hasta):
    with conectar() as conexion:
        with conexion.cursor() as cursor:
            cursor.execute("UPDATE modelo_prediccion SET activo = FALSE WHERE activo = TRUE")
            cursor.execute(
                """
                INSERT INTO modelo_prediccion (algoritmo, version, fecha_entrenamiento, datos_desde, datos_hasta,
                                               error_mae, activo)
                VALUES (%s, %s, %s, %s, %s, %s, TRUE)
                """,
                (algoritmo[:50], datetime.now().strftime("%Y.%m.%d"), datetime.now(), desde, hasta, round(mae, 4)),
            )


def principal():
    analizador = argparse.ArgumentParser(description="Compara modelos de predicción de ventas y elige el de menor MAE.")
    analizador.add_argument("--csv", help="Archivo con las columnas fecha, id_producto y cantidad. Si se omite se lee la base de datos.")
    analizador.add_argument("--registrar", action="store_true", help="Guarda el modelo ganador en la tabla modelo_prediccion.")
    argumentos = analizador.parse_args()

    ventas = cargar_ventas(argumentos.csv)
    if ventas.empty:
        print("No hay ventas para entrenar. Carga los datos históricos primero.")
        return 1
    datos = construir_variables(completar_calendario(ventas))
    if datos["fecha"].nunique() <= DIAS_DE_PRUEBA + 30:
        print("Se necesitan más días de historia para evaluar los modelos.")
        return 1

    resultados = evaluar(datos)
    mostrar(resultados)
    ganador, mae = min(resultados.items(), key=lambda par: par[1])
    print(f"\nModelo elegido: {ganador} (MAE {mae:.3f})")

    if argumentos.registrar:
        registrar_en_base(ganador, mae, ventas["fecha"].min().date(), ventas["fecha"].max().date())
        print("Modelo registrado como activo en modelo_prediccion.")
    return 0


if __name__ == "__main__":
    sys.exit(principal())
