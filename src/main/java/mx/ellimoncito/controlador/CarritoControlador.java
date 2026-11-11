package mx.ellimoncito.controlador;

import java.util.List;
import mx.ellimoncito.servicio.CarritoServicio;
import mx.ellimoncito.servicio.ClienteServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/ventas")
public class CarritoControlador {

    private static final String CAJA = "redirect:/ventas/nueva";

    private final CarritoServicio carritoServicio;
    private final ClienteServicio clienteServicio;

    public CarritoControlador(CarritoServicio carritoServicio, ClienteServicio clienteServicio) {
        this.carritoServicio = carritoServicio;
        this.clienteServicio = clienteServicio;
    }

    @GetMapping("/nueva")
    public String caja(@RequestParam(defaultValue = "") String producto,
                       @RequestParam(defaultValue = "") String cliente, Model modelo) {
        modelo.addAttribute("carrito", carritoServicio.vista());
        modelo.addAttribute("textoProducto", producto);
        modelo.addAttribute("textoCliente", cliente);
        modelo.addAttribute("productos", carritoServicio.buscarProductos(producto));
        modelo.addAttribute("clientes", cliente.isBlank() ? List.of() : clienteServicio.buscarParaCaja(cliente));
        return "ventas/caja";
    }

    @PostMapping("/carrito/agregar")
    public String agregar(@RequestParam(required = false) String codigo,
                          @RequestParam(required = false) Integer idProducto,
                          @RequestParam(defaultValue = "1") Integer cantidad, RedirectAttributes mensajes) {
        try {
            if (idProducto != null) {
                carritoServicio.agregarProducto(idProducto, cantidad);
            } else {
                carritoServicio.agregarPorCodigo(codigo, cantidad);
            }
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return CAJA;
    }

    @PostMapping("/carrito/cantidad")
    public String cantidad(@RequestParam Integer idProducto, @RequestParam Integer cantidad,
                           RedirectAttributes mensajes) {
        try {
            carritoServicio.cambiarCantidad(idProducto, cantidad);
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return CAJA;
    }

    @PostMapping("/carrito/quitar")
    public String quitar(@RequestParam Integer idProducto) {
        carritoServicio.quitar(idProducto);
        return CAJA;
    }

    @PostMapping("/carrito/vaciar")
    public String vaciar() {
        carritoServicio.vaciar();
        return CAJA;
    }

    @PostMapping("/carrito/cliente")
    public String elegirCliente(@RequestParam Integer idCliente, RedirectAttributes mensajes) {
        try {
            carritoServicio.elegirCliente(idCliente);
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return CAJA;
    }

    @PostMapping("/carrito/cliente/quitar")
    public String quitarCliente() {
        carritoServicio.quitarCliente();
        return CAJA;
    }
}
