package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mx.ellimoncito.modelo.Cliente;
import mx.ellimoncito.modelo.Producto;
import mx.ellimoncito.repositorio.ClienteRepositorio;
import mx.ellimoncito.repositorio.LotePepsRepositorio;
import mx.ellimoncito.repositorio.ProductoRepositorio;
import mx.ellimoncito.repositorio.ProductoVentaRepositorio;
import org.springframework.stereotype.Service;

@Service
public class CarritoServicio {

    public static final String MENUDEO = "MENUDEO";
    public static final String MAYOREO = "MAYOREO";

    private static final int LIMITE_BUSQUEDA = 10;

    public record LineaCarrito(Producto producto, int cantidad, String tipoPrecio, BigDecimal precio,
                               BigDecimal importe, BigDecimal iva, BigDecimal deposito) {

        public boolean esMayoreo() {
            return MAYOREO.equals(tipoPrecio);
        }
    }

    public record CarritoVista(Cliente cliente, List<LineaCarrito> lineas, BigDecimal subtotal, BigDecimal iva,
                               BigDecimal deposito, BigDecimal total) {

        public boolean vacio() {
            return lineas.isEmpty();
        }

        public int unidades() {
            return lineas.stream().mapToInt(LineaCarrito::cantidad).sum();
        }

        public boolean requiereCliente() {
            return deposito.signum() > 0 && cliente == null;
        }
    }

    public record ResultadoProducto(Producto producto, long existencia) {
    }

    private final CarritoSesion carrito;
    private final ProductoRepositorio productoRepositorio;
    private final ProductoVentaRepositorio productoVentaRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final LotePepsRepositorio lotePepsRepositorio;

    public CarritoServicio(CarritoSesion carrito, ProductoRepositorio productoRepositorio,
                           ProductoVentaRepositorio productoVentaRepositorio, ClienteRepositorio clienteRepositorio,
                           LotePepsRepositorio lotePepsRepositorio) {
        this.carrito = carrito;
        this.productoRepositorio = productoRepositorio;
        this.productoVentaRepositorio = productoVentaRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.lotePepsRepositorio = lotePepsRepositorio;
    }

    public CarritoVista vista() {
        Cliente cliente = null;
        if (carrito.getIdCliente() != null) {
            cliente = clienteRepositorio.findById(carrito.getIdCliente()).filter(Cliente::getActivo).orElse(null);
            if (cliente == null) {
                carrito.setIdCliente(null);
            }
        }
        List<LineaCarrito> lineas = new ArrayList<>();
        Iterator<Map.Entry<Integer, Integer>> entradas = carrito.getLineas().entrySet().iterator();
        while (entradas.hasNext()) {
            Map.Entry<Integer, Integer> entrada = entradas.next();
            Producto producto = productoRepositorio.findById(entrada.getKey()).filter(Producto::getActivo).orElse(null);
            if (producto == null) {
                entradas.remove();
            } else {
                lineas.add(construirLinea(producto, entrada.getValue(), cliente));
            }
        }
        BigDecimal subtotal = sumar(lineas.stream().map(LineaCarrito::importe).toList());
        BigDecimal iva = sumar(lineas.stream().map(LineaCarrito::iva).toList());
        BigDecimal deposito = sumar(lineas.stream().map(LineaCarrito::deposito).toList());
        return new CarritoVista(cliente, lineas, subtotal, iva, deposito, subtotal.add(iva).add(deposito));
    }

    public List<ResultadoProducto> buscarProductos(String texto) {
        if (texto == null || texto.isBlank()) {
            return List.of();
        }
        return productoVentaRepositorio.buscarActivos(texto.trim()).stream()
                .limit(LIMITE_BUSQUEDA)
                .map(p -> new ResultadoProducto(p, lotePepsRepositorio.existenciaDe(p.getId())))
                .toList();
    }

    public void agregarPorCodigo(String codigo, Integer cantidad) {
        if (codigo == null || codigo.isBlank()) {
            throw new ReglaNegocioException("Escribe o escanea el código del producto.");
        }
        Producto producto = productoVentaRepositorio.buscarPorCodigo(codigo.trim())
                .orElseThrow(() -> new ReglaNegocioException("No existe un producto activo con el código " + codigo.trim() + "."));
        sumar(producto, cantidad);
    }

    public void agregarProducto(Integer idProducto, Integer cantidad) {
        Producto producto = productoRepositorio.findById(idProducto)
                .filter(Producto::getActivo)
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe o está dado de baja."));
        sumar(producto, cantidad);
    }

    public void cambiarCantidad(Integer idProducto, Integer cantidad) {
        if (!carrito.getLineas().containsKey(idProducto)) {
            throw new ReglaNegocioException("El producto no está en el carrito.");
        }
        if (cantidad == null || cantidad <= 0) {
            carrito.getLineas().remove(idProducto);
            return;
        }
        Producto producto = productoRepositorio.findById(idProducto)
                .orElseThrow(() -> new ReglaNegocioException("El producto no existe."));
        validarExistencia(producto, cantidad);
        carrito.getLineas().put(idProducto, cantidad);
    }

    public void quitar(Integer idProducto) {
        carrito.getLineas().remove(idProducto);
    }

    public void vaciar() {
        carrito.vaciar();
    }

    public void elegirCliente(Integer idCliente) {
        Cliente cliente = clienteRepositorio.findById(idCliente)
                .orElseThrow(() -> new ReglaNegocioException("El cliente no existe."));
        if (!cliente.getActivo()) {
            throw new ReglaNegocioException("El cliente está dado de baja.");
        }
        carrito.setIdCliente(cliente.getId());
    }

    public void quitarCliente() {
        carrito.setIdCliente(null);
    }

    private void sumar(Producto producto, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        int nueva = carrito.getLineas().getOrDefault(producto.getId(), 0) + cantidad;
        validarExistencia(producto, nueva);
        carrito.getLineas().put(producto.getId(), nueva);
    }

    private void validarExistencia(Producto producto, int cantidad) {
        long existencia = lotePepsRepositorio.existenciaDe(producto.getId());
        if (cantidad > existencia) {
            throw new ReglaNegocioException("Solo hay " + existencia + " unidades de " + producto.getNombre() + ".");
        }
    }

    private LineaCarrito construirLinea(Producto producto, int cantidad, Cliente cliente) {
        String tipoPrecio = tipoPrecio(producto, cantidad, cliente);
        BigDecimal precio = MAYOREO.equals(tipoPrecio) ? producto.getPrecioMayoreo() : producto.getPrecioMenudeo();
        BigDecimal importe = redondear(precio.multiply(BigDecimal.valueOf(cantidad)));
        BigDecimal iva = redondear(importe.multiply(producto.getTasaIva()));
        BigDecimal deposito = BigDecimal.ZERO.setScale(2);
        if (Boolean.TRUE.equals(producto.getRetornable()) && producto.getTipoEnvase() != null) {
            deposito = redondear(producto.getTipoEnvase().getValorDeposito().multiply(BigDecimal.valueOf(cantidad)));
        }
        return new LineaCarrito(producto, cantidad, tipoPrecio, precio, importe, iva, deposito);
    }

    private String tipoPrecio(Producto producto, int cantidad, Cliente cliente) {
        if (cliente != null && cliente.isPrecioMayoreo()) {
            return MAYOREO;
        }
        return cantidad >= producto.getCantidadMinMayoreo() ? MAYOREO : MENUDEO;
    }

    private BigDecimal redondear(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal sumar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO.setScale(2), BigDecimal::add);
    }
}
