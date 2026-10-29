package mx.ellimoncito.servicio;

import java.util.List;
import mx.ellimoncito.formulario.UsuarioFormulario;
import mx.ellimoncito.modelo.Rol;
import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.RolRepositorio;
import mx.ellimoncito.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UsuarioServicio {

    private static final int LONGITUD_MINIMA = 8;

    private final UsuarioRepositorio usuarioRepositorio;
    private final RolRepositorio rolRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraServicio bitacoraServicio;
    private final UsuarioActualServicio usuarioActualServicio;

    public UsuarioServicio(UsuarioRepositorio usuarioRepositorio, RolRepositorio rolRepositorio,
                           PasswordEncoder passwordEncoder, BitacoraServicio bitacoraServicio,
                           UsuarioActualServicio usuarioActualServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.rolRepositorio = rolRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraServicio = bitacoraServicio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    public List<Usuario> listar(String texto) {
        return usuarioRepositorio.findByNombreCompletoContainingIgnoreCaseOrderByNombreCompletoAsc(
                texto == null ? "" : texto.trim());
    }

    public List<Rol> roles() {
        return rolRepositorio.findAllByOrderByNombreAsc();
    }

    public Usuario buscar(Integer id) {
        return usuarioRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El usuario no existe."));
    }

    public UsuarioFormulario formularioDe(Integer id) {
        Usuario usuario = buscar(id);
        UsuarioFormulario formulario = new UsuarioFormulario();
        formulario.setId(usuario.getId());
        formulario.setNombreCompleto(usuario.getNombreCompleto());
        formulario.setUsuario(usuario.getUsuario());
        formulario.setIdRol(usuario.getRol().getId());
        return formulario;
    }

    @Transactional
    public Usuario registrar(UsuarioFormulario formulario) {
        validarContrasena(formulario.getContrasena());
        String nombreUsuario = formulario.getUsuario().trim();
        if (usuarioRepositorio.existsByUsuarioIgnoreCase(nombreUsuario)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese nombre de usuario.");
        }
        Usuario usuario = new Usuario();
        usuario.setNombreCompleto(formulario.getNombreCompleto().trim());
        usuario.setUsuario(nombreUsuario);
        usuario.setRol(buscarRol(formulario.getIdRol()));
        usuario.setContrasenaHash(passwordEncoder.encode(formulario.getContrasena()));
        usuario.setIntentosFallidos(0);
        usuario.setBloqueado(false);
        usuario.setActivo(true);
        Usuario guardado = usuarioRepositorio.save(usuario);
        bitacoraServicio.registrar("USUARIO_ALTA",
                "Se registró al usuario " + guardado.getUsuario() + " con rol " + guardado.getRol().getNombre() + ".");
        return guardado;
    }

    @Transactional
    public Usuario modificar(Integer id, UsuarioFormulario formulario) {
        Usuario usuario = buscar(id);
        String nombreUsuario = formulario.getUsuario().trim();
        if (usuarioRepositorio.existsByUsuarioIgnoreCaseAndIdNot(nombreUsuario, id)) {
            throw new ReglaNegocioException("Ya existe un usuario con ese nombre de usuario.");
        }
        Rol nuevoRol = buscarRol(formulario.getIdRol());
        Usuario actual = usuarioActualServicio.obtener();
        if (actual.getId().equals(id) && !usuario.getRol().getId().equals(nuevoRol.getId())) {
            throw new ReglaNegocioException("No puedes cambiar tu propio rol.");
        }
        String rolAnterior = usuario.getRol().getNombre();
        usuario.setNombreCompleto(formulario.getNombreCompleto().trim());
        usuario.setUsuario(nombreUsuario);
        usuario.setRol(nuevoRol);
        String contrasena = formulario.getContrasena();
        if (contrasena != null && !contrasena.isBlank()) {
            validarContrasena(contrasena);
            usuario.setContrasenaHash(passwordEncoder.encode(contrasena));
        }
        Usuario guardado = usuarioRepositorio.save(usuario);
        String detalle = "Se modificó al usuario " + guardado.getUsuario() + ".";
        if (!rolAnterior.equals(nuevoRol.getNombre())) {
            detalle = detalle + " Rol: " + rolAnterior + " a " + nuevoRol.getNombre() + ".";
        }
        bitacoraServicio.registrar("USUARIO_MODIFICACION", detalle);
        return guardado;
    }

    @Transactional
    public void desbloquear(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setBloqueado(false);
        usuario.setIntentosFallidos(0);
        usuarioRepositorio.save(usuario);
        bitacoraServicio.registrar("USUARIO_DESBLOQUEO", "Se desbloqueó la cuenta de " + usuario.getUsuario() + ".");
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Usuario usuario = buscar(id);
        if (usuarioActualServicio.obtener().getId().equals(id)) {
            throw new ReglaNegocioException("No puedes dar de baja tu propia cuenta.");
        }
        usuario.setActivo(false);
        usuarioRepositorio.save(usuario);
        bitacoraServicio.registrar("USUARIO_BAJA", "Se dio de baja al usuario " + usuario.getUsuario() + ".");
    }

    @Transactional
    public void reactivar(Integer id) {
        Usuario usuario = buscar(id);
        usuario.setActivo(true);
        usuarioRepositorio.save(usuario);
        bitacoraServicio.registrar("USUARIO_REACTIVACION", "Se reactivó al usuario " + usuario.getUsuario() + ".");
    }

    private Rol buscarRol(Integer idRol) {
        return rolRepositorio.findById(idRol).orElseThrow(() -> new ReglaNegocioException("El rol no existe."));
    }

    private void validarContrasena(String contrasena) {
        if (contrasena == null || contrasena.length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("La contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
    }
}
