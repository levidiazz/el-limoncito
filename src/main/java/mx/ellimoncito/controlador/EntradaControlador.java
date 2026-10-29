package mx.ellimoncito.controlador;

import jakarta.validation.Valid;
import java.time.LocalDate;
import mx.ellimoncito.formulario.EntradaFormulario;
import mx.ellimoncito.formulario.LoteFormulario;
import mx.ellimoncito.modelo.EntradaMercancia;
import mx.ellimoncito.servicio.EntradaServicio;
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
@RequestMapping("/entradas")
public class EntradaControlador {

    private final EntradaServicio entradaServicio;

    public EntradaControlador(EntradaServicio entradaServicio) {
        this.entradaServicio = entradaServicio;
    }

    @GetMapping
    public String lista(@RequestParam(defaultValue = "0") Integer idProveedor,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
                        Model modelo) {
        modelo.addAttribute("entradas", entradaServicio.listar(idProveedor, desde, hasta));
        modelo.addAttribute("proveedores", entradaServicio.proveedores());
        modelo.addAttribute("idProveedor", idProveedor);
        modelo.addAttribute("desde", desde);
        modelo.addAttribute("hasta", hasta);
        return "entradas/lista";
    }

    @GetMapping("/nueva")
    public String nueva(Model modelo) {
        modelo.addAttribute("entradaFormulario", new EntradaFormulario());
        modelo.addAttribute("proveedores", entradaServicio.proveedoresActivos());
        return "entradas/formulario";
    }

    @PostMapping
    public String guardar(@Valid @ModelAttribute("entradaFormulario") EntradaFormulario formulario,
                          BindingResult resultado, Model modelo) {
        if (!resultado.hasErrors()) {
            try {
                EntradaMercancia entrada = entradaServicio.registrar(formulario);
                return "redirect:/entradas/" + entrada.getId();
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("proveedores", entradaServicio.proveedoresActivos());
        return "entradas/formulario";
    }

    @GetMapping("/{id}")
    public String detalle(@PathVariable Integer id, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("entrada", entradaServicio.buscar(id));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/entradas";
        }
        modelo.addAttribute("lotes", entradaServicio.lotesDe(id));
        modelo.addAttribute("productos", entradaServicio.productosActivos());
        if (!modelo.containsAttribute("loteFormulario")) {
            modelo.addAttribute("loteFormulario", new LoteFormulario());
        }
        return "entradas/detalle";
    }

    @PostMapping("/{id}/lotes")
    public String agregarLote(@PathVariable Integer id,
                              @Valid @ModelAttribute("loteFormulario") LoteFormulario formulario,
                              BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        if (!resultado.hasErrors()) {
            try {
                entradaServicio.agregarLote(id, formulario);
                mensajes.addFlashAttribute("exito", "Producto agregado a la entrada.");
                return "redirect:/entradas/" + id;
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("entrada", entradaServicio.buscar(id));
        modelo.addAttribute("lotes", entradaServicio.lotesDe(id));
        modelo.addAttribute("productos", entradaServicio.productosActivos());
        return "entradas/detalle";
    }

    @GetMapping("/lotes/{idLote}/editar")
    public String editarLote(@PathVariable Integer idLote, Model modelo, RedirectAttributes mensajes) {
        try {
            modelo.addAttribute("loteFormulario", entradaServicio.formularioDeLote(idLote));
            modelo.addAttribute("lote", entradaServicio.buscarLote(idLote));
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/entradas";
        }
        modelo.addAttribute("productos", entradaServicio.productosActivos());
        return "entradas/lote";
    }

    @PostMapping("/lotes/{idLote}")
    public String corregirLote(@PathVariable Integer idLote,
                               @Valid @ModelAttribute("loteFormulario") LoteFormulario formulario,
                               BindingResult resultado, Model modelo, RedirectAttributes mensajes) {
        Integer idEntrada = entradaServicio.buscarLote(idLote).getEntrada().getId();
        if (!resultado.hasErrors()) {
            try {
                entradaServicio.corregirLote(idLote, formulario);
                mensajes.addFlashAttribute("exito", "Lote corregido.");
                return "redirect:/entradas/" + idEntrada;
            } catch (ReglaNegocioException e) {
                modelo.addAttribute("error", e.getMessage());
            }
        }
        modelo.addAttribute("lote", entradaServicio.buscarLote(idLote));
        modelo.addAttribute("productos", entradaServicio.productosActivos());
        return "entradas/lote";
    }

    @PostMapping("/lotes/{idLote}/quitar")
    public String quitarLote(@PathVariable Integer idLote, RedirectAttributes mensajes) {
        Integer idEntrada = entradaServicio.buscarLote(idLote).getEntrada().getId();
        try {
            entradaServicio.eliminarLote(idLote);
            mensajes.addFlashAttribute("exito", "Lote quitado de la entrada.");
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/entradas/" + idEntrada;
    }

    @PostMapping("/{id}/cancelar")
    public String cancelar(@PathVariable Integer id, RedirectAttributes mensajes) {
        try {
            entradaServicio.cancelar(id);
            mensajes.addFlashAttribute("exito", "Entrada cancelada.");
            return "redirect:/entradas";
        } catch (ReglaNegocioException e) {
            mensajes.addFlashAttribute("error", e.getMessage());
            return "redirect:/entradas/" + id;
        }
    }
}
