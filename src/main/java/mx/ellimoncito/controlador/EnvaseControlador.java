package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.modelo.TipoEnvase;
import mx.ellimoncito.servicio.EnvaseServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/envases")
public class EnvaseControlador {

    private final EnvaseServicio envaseServicio;

    public EnvaseControlador(EnvaseServicio envaseServicio) {
        this.envaseServicio = envaseServicio;
    }

    @GetMapping
    public String lista(Model modelo) {
        modelo.addAttribute("tipos", envaseServicio.listar());
        modelo.addAttribute("movimientos", envaseServicio.ultimosMovimientos());
        return "envases/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("tipoEnvase", new TipoEnvase());
        modelo.addAttribute("titulo", "Nuevo tipo de envase");
        modelo.addAttribute("accion", "/envases");
        return "envases/formulario";
    }

    @PostMapping
    public String guardar(@Valid TipoEnvase tipoEnvase, BindingResult resultado, Model modelo,
                          RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                envaseServicio.registrar(tipoEnvase);
                mensajes.addFlashAttribute("exito", "Tipo de envase registrado.");
                return "redirect:/envases";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("titulo", "Nuevo tipo de envase");
        modelo.addAttribute("accion", "/envases");
        return "envases/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("tipoEnvase", envaseServicio.buscar(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/envases";
        }
        modelo.addAttribute("titulo", "Modificar tipo de envase");
        modelo.addAttribute("accion", "/envases/" + id);
        return "envases/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id, @Valid TipoEnvase tipoEnvase, BindingResult resultado,
                             Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                envaseServicio.modificar(id, tipoEnvase);
                mensajes.addFlashAttribute("exito", "Tipo de envase actualizado.");
                return "redirect:/envases";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("titulo", "Modificar tipo de envase");
        modelo.addAttribute("accion", "/envases/" + id);
        return "envases/formulario";
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            mensajes.addFlashAttribute("exito", envaseServicio.darDeBajaOEliminar(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/envases";
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            envaseServicio.reactivar(id);
            mensajes.addFlashAttribute("exito", "Tipo de envase reactivado.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/envases";
    }

    @GetMapping("/movimiento")
    public String movimiento(Model modelo) {
        modelo.addAttribute("tipos", envaseServicio.listarActivos());
        return "envases/movimiento";
    }

    @PostMapping("/entrega")
    public String entrega(@RequestParam Integer idTipo, @RequestParam Integer cantidad, RedirectAttributes mensajes) {
        try {
            envaseServicio.registrarEntregaAProveedor(idTipo, cantidad);
            mensajes.addFlashAttribute("exito", "Entrega al proveedor registrada.");
            return "redirect:/envases";
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/envases/movimiento";
        }
    }

    @PostMapping("/recepcion")
    public String recepcion(@RequestParam Integer idTipo, @RequestParam Integer cantidad, RedirectAttributes mensajes) {
        try {
            envaseServicio.registrarVaciosRecibidos(idTipo, cantidad);
            mensajes.addFlashAttribute("exito", "Envases vacíos recibidos registrados.");
            return "redirect:/envases";
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/envases/movimiento";
        }
    }
}
