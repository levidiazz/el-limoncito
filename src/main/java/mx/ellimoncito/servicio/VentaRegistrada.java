package mx.ellimoncito.servicio;

import mx.ellimoncito.modelo.Venta;
import mx.ellimoncito.servicio.CarritoServicio.CarritoVista;

public record VentaRegistrada(Venta venta, CarritoVista carrito) {
}
