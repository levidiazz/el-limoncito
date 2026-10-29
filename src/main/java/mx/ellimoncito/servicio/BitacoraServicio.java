package mx.ellimoncito.servicio;

import java.time.LocalDateTime;
import mx.ellimoncito.modelo.Bitacora;
import mx.ellimoncito.modelo.Usuario;
import mx.ellimoncito.repositorio.BitacoraRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BitacoraServicio {

    private final BitacoraRepositorio bitacoraRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;

    public BitacoraServicio(BitacoraRepositorio bitacoraRepositorio, UsuarioActualServicio usuarioActualServicio) {
        this.bitacoraRepositorio = bitacoraRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    @Transactional
    public void registrar(Usuario usuario, String accion, String detalle) {
        Bitacora registro = new Bitacora();
        registro.setUsuario(usuario);
        registro.setAccion(accion);
        registro.setDetalle(detalle);
        registro.setFechaHora(LocalDateTime.now());
        bitacoraRepositorio.save(registro);
    }

    @Transactional
    public void registrar(String accion, String detalle) {
        registrar(usuarioActualServicio.obtener(), accion, detalle);
    }
}
