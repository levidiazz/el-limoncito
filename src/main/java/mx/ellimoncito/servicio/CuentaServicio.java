package mx.ellimoncito.servicio;

import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.UsuarioRepositorio;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CuentaServicio {

    private static final int LONGITUD_MINIMA = 8;

    private final UsuarioActualServicio usuarioActualServicio;
    private final UsuarioRepositorio usuarioRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final BitacoraServicio bitacoraServicio;

    public CuentaServicio(UsuarioActualServicio usuarioActualServicio, UsuarioRepositorio usuarioRepositorio,
                          PasswordEncoder passwordEncoder, BitacoraServicio bitacoraServicio) {
        this.usuarioActualServicio = usuarioActualServicio;
        this.usuarioRepositorio = usuarioRepositorio;
        this.passwordEncoder = passwordEncoder;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional
    public void cambiarContrasena(String actual, String nueva, String confirmacion) {
        Usuario usuario = usuarioActualServicio.obtener();
        if (actual == null || !passwordEncoder.matches(actual, usuario.getContrasenaHash())) {
            throw new ReglaNegocioException("La contraseña actual no es correcta.");
        }
        if (nueva == null || nueva.length() < LONGITUD_MINIMA) {
            throw new ReglaNegocioException("La nueva contraseña debe tener al menos " + LONGITUD_MINIMA + " caracteres.");
        }
        if (!nueva.equals(confirmacion)) {
            throw new ReglaNegocioException("La confirmación no coincide con la nueva contraseña.");
        }
        if (passwordEncoder.matches(nueva, usuario.getContrasenaHash())) {
            throw new ReglaNegocioException("La nueva contraseña debe ser distinta de la actual.");
        }
        usuario.setContrasenaHash(passwordEncoder.encode(nueva));
        usuarioRepositorio.save(usuario);
        bitacoraServicio.registrar(usuario, "CAMBIO_CONTRASENA", "El usuario cambió su contraseña.");
    }
}
