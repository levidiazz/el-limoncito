package mx.ellimoncito.servicio;

import java.util.List;
import mx.ellimoncito.formulario.ProveedorFormulario;
import mx.ellimoncito.modelo.Proveedor;
import mx.ellimoncito.repositorio.ProveedorRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProveedorServicio {

    private final ProveedorRepositorio proveedorRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ProveedorServicio(ProveedorRepositorio proveedorRepositorio, BitacoraServicio bitacoraServicio) {
        this.proveedorRepositorio = proveedorRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<Proveedor> listar(String texto) {
        return proveedorRepositorio.findByNombreContainingIgnoreCaseOrderByNombreAsc(
                texto == null ? "" : texto.trim());
    }

    public List<Proveedor> activos() {
        return proveedorRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public Proveedor buscar(Integer id) {
        return proveedorRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El proveedor no existe."));
    }

    public ProveedorFormulario formularioDe(Integer id) {
        Proveedor proveedor = buscar(id);
        ProveedorFormulario formulario = new ProveedorFormulario();
        formulario.setNombre(proveedor.getNombre());
        formulario.setContacto(proveedor.getContacto());
        formulario.setTelefono(proveedor.getTelefono());
        return formulario;
    }

    @Transactional
    public Proveedor registrar(ProveedorFormulario formulario) {
        Proveedor proveedor = new Proveedor();
        aplicar(proveedor, formulario);
        proveedor.setActivo(true);
        Proveedor guardado = proveedorRepositorio.save(proveedor);
        bitacoraServicio.registrar("PROVEEDOR_ALTA", "Se registró al proveedor " + guardado.getNombre() + ".");
        return guardado;
    }

    @Transactional
    public Proveedor modificar(Integer id, ProveedorFormulario formulario) {
        Proveedor proveedor = buscar(id);
        aplicar(proveedor, formulario);
        Proveedor guardado = proveedorRepositorio.save(proveedor);
        bitacoraServicio.registrar("PROVEEDOR_MODIFICACION", "Se modificó al proveedor " + guardado.getNombre() + ".");
        return guardado;
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Proveedor proveedor = buscar(id);
        proveedor.setActivo(false);
        proveedorRepositorio.save(proveedor);
        bitacoraServicio.registrar("PROVEEDOR_BAJA", "Se dio de baja al proveedor " + proveedor.getNombre() + ".");
    }

    @Transactional
    public void reactivar(Integer id) {
        Proveedor proveedor = buscar(id);
        proveedor.setActivo(true);
        proveedorRepositorio.save(proveedor);
        bitacoraServicio.registrar("PROVEEDOR_REACTIVACION", "Se reactivó al proveedor " + proveedor.getNombre() + ".");
    }

    private void aplicar(Proveedor proveedor, ProveedorFormulario formulario) {
        proveedor.setNombre(formulario.getNombre().trim());
        proveedor.setContacto(vacioANulo(formulario.getContacto()));
        proveedor.setTelefono(vacioANulo(formulario.getTelefono()));
    }

    private String vacioANulo(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
