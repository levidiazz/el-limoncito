package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import mx.ellimoncito.formulario.UsuarioFormulario;
import mx.ellimoncito.servicio.ReglaNegocioException;
import mx.ellimoncito.servicio.UsuarioServicio;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/usuarios")
public class UsuarioControlador {

    private final UsuarioServicio usuarioServicio;

    public UsuarioControlador(UsuarioServicio usuarioServicio) {
        this.usuarioServicio = usuarioServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String texto, Model modelo) {
        modelo.addAttribute("usuarios", usuarioServicio.listar(texto));
        modelo.addAttribute("texto", texto);
        return "usuarios/lista";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model modelo) {
        modelo.addAttribute("usuarioFormulario", new UsuarioFormulario());
        prepararFormulario(modelo, "Nuevo usuario", "/usuarios", true);
        return "usuarios/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("usuarioFormulario") UsuarioFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                usuarioServicio.registrar(formulario);
                mensajes.addFlashAttribute("exito", "Usuario registrado.");
                return "redirect:/usuarios";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Nuevo usuario", "/usuarios", true);
        return "usuarios/formulario";
    }

    @GetMapping("/{id}/editar")
    public String editar(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("usuarioFormulario", usuarioServicio.formularioDe(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/usuarios";
        }
        prepararFormulario(modelo, "Modificar usuario", "/usuarios/" + id, false);
        return "usuarios/formulario";
    }

    @PostMapping("/{id}")
    public String actualizar(@PathVariable Integer id,
                             @Valid @ModelAttribute("usuarioFormulario") UsuarioFormulario formulario,
                             BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                usuarioServicio.modificar(id, formulario);
                mensajes.addFlashAttribute("exito", "Usuario actualizado.");
                return "redirect:/usuarios";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo, "Modificar usuario", "/usuarios/" + id, false);
        return "usuarios/formulario";
    }

    @PostMapping("/{id}/desbloquear")
    public String desbloquear(@PathVariable Integer id, RedirectAttributes mensajes) {
        return ejecutar(() -> usuarioServicio.desbloquear(id), "Cuenta desbloqueada.", mensajes);
    }

    @PostMapping("/{id}/baja")
    public String baja(@PathVariable Integer id, RedirectAttributes mensajes) {
        return ejecutar(() -> usuarioServicio.darDeBaja(id), "Usuario dado de baja.", mensajes);
    }

    @PostMapping("/{id}/reactivar")
    public String reactivar(@PathVariable Integer id, RedirectAttributes mensajes) {
        return ejecutar(() -> usuarioServicio.reactivar(id), "Usuario reactivado.", mensajes);
    }

    private String ejecutar(Runnable accion, String mensajeExito, RedirectAttributes mensajes) {
        try {
            accion.run();
            mensajes.addFlashAttribute("exito", mensajeExito);
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/usuarios";
    }

    private void prepararFormulario(Model modelo, String titulo, String accion, boolean nuevo) {
        modelo.addAttribute("titulo", titulo);
        modelo.addAttribute("accion", accion);
        modelo.addAttribute("nuevo", nuevo);
        modelo.addAttribute("roles", usuarioServicio.roles());
    }
}
