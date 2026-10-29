package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import java.time.LocalDate;
import mx.ellimoncito.formulario.MermaFormulario;
import mx.ellimoncito.servicio.MermaServicio;
import mx.ellimoncito.servicio.ReglaNegocioException;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("/mermas")
public class MermaControlador {

    private final MermaServicio mermaServicio;

    public MermaControlador(MermaServicio mermaServicio) {
        this.mermaServicio = mermaServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "") String motivo,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model modelo) {
        modelo.addAttribute("mermas", mermaServicio.listar(motivo, desde, hasta));
        modelo.addAttribute("motivos", MermaServicio.MOTIVOS);
        modelo.addAttribute("motivo", motivo);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        return "mermas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("mermaFormulario", new MermaFormulario());
        prepararFormulario(modelo);
        return "mermas/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("mermaFormulario") MermaFormulario formulario,
                          BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                mermaServicio.registrar(formulario);
                mensajes.addFlashAttribute("exito", "Merma registrada.");
                return "redirect:/mermas";
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        prepararFormulario(modelo);
        return "mermas/formulario";
    }

    @PostMapping("/{id}/corregir")
    public String corregir(@PathVariable Integer id, @RequestParam Integer cantidad, RedirectAttributes mensajes) {
        try {
            mermaServicio.corregir(id, cantidad);
            mensajes.addFlashAttribute("exito", "Merma corregida.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mermas";
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            mermaServicio.cancelar(id);
            mensajes.addFlashAttribute("exito", "Merma cancelada y unidades devueltas al lote.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/mermas";
    }

    private void prepararFormulario(Model modelo) {
        modelo.addAttribute("productos", mermaServicio.productosActivos());
        modelo.addAttribute("motivos", MermaServicio.MOTIVOS);
    }
}
