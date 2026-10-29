package mx.ellimoncito.servicio;

import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.UsuarioRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacionServicio {

    public static final int MAXIMO_INTENTOS = 3;

    private final UsuarioRepositorio usuarioRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public AutenticacionServicio(UsuarioRepositorio usuarioRepositorio, BitacoraServicio bitacoraServicio) {
        this.usuarioRepositorio = usuarioRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    @Transactional
    public void registrarAccesoCorrecto(String nombreUsuario) {
        usuarioRepositorio.findByUsuario(nombreUsuario).ifPresent(usuario -> {
            usuario.setIntentosFallidos(0);
            usuarioRepositorio.save(usuario);
            bitacoraServicio.registrar(usuario, "INICIO_SESION", "Inicio de sesión correcto.");
        });
    }

    @Transactional
    public boolean registrarIntentoFallido(String nombreUsuario) {
        return usuarioRepositorio.findByUsuario(nombreUsuario).map(usuario -> {
            if (!usuario.getActivo() || usuario.getBloqueado()) {
                return usuario.getBloqueado();
            }
            int intentos = usuario.getIntentosFallidos() + 1;
            usuario.setIntentosFallidos(intentos);
            boolean bloquear = intentos >= MAXIMO_INTENTOS;
            if (bloquear) {
                usuario.setBloqueado(true);
            }
            usuarioRepositorio.save(usuario);
            if (bloquear) {
                bitacoraServicio.registrar(usuario, "CUENTA_BLOQUEADA",
                        "La cuenta se bloqueó por " + MAXIMO_INTENTOS + " intentos fallidos seguidos.");
            } else {
                bitacoraServicio.registrar(usuario, "INTENTO_FALLIDO", "Intento fallido número " + intentos + ".");
            }
            return bloquear;
        }).orElse(false);
    }
}
