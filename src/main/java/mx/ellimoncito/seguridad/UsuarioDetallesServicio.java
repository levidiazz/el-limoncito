package mx.ellimoncito.seguridad;

import java.util.List;
import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.UsuarioRepositorio;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UsuarioDetallesServicio implements UserDetailsService {

    private final UsuarioRepositorio usuarioRepositorio;

    public UsuarioDetallesServicio(UsuarioRepositorio usuarioRepositorio) {
        this.usuarioRepositorio = usuarioRepositorio;
    }

    @Override
    public UserDetails loadUserByUsername(String nombreUsuario) throws UsernameNotFoundException {
        Usuario usuario = usuarioRepositorio.findByUsuario(nombreUsuario)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado."));
        return org.springframework.security.core.userdetails.User.withUsername(usuario.getUsuario())
                .password(usuario.getContrasenaHash())
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + usuario.getRol().getNombre())))
                .accountLocked(usuario.getBloqueado())
                .disabled(!usuario.getActivo())
                .build();
    }
}
