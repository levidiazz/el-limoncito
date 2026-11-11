package mx.ellimoncito.servicio;

import java.util.List;
import mx.ellimoncito.formulario.ClienteFormulario;
import mx.ellimoncito.modelo.Cliente;
import mx.ellimoncito.repositorio.ClienteRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClienteServicio {

    private static final int LIMITE_CAJA = 8;

    private final ClienteRepositorio clienteRepositorio;
    private final BitacoraServicio bitacoraServicio;

    public ClienteServicio(ClienteRepositorio clienteRepositorio, BitacoraServicio bitacoraServicio) {
        this.clienteRepositorio = clienteRepositorio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<Cliente> listar(String texto) {
        return clienteRepositorio.buscar(normalizar(texto));
    }

    public List<Cliente> buscarParaCaja(String texto) {
        List<Cliente> encontrados = clienteRepositorio.buscarActivos(normalizar(texto));
        return encontrados.size() > LIMITE_CAJA ? encontrados.subList(0, LIMITE_CAJA) : encontrados;
    }

    public Cliente buscar(Integer id) {
        return clienteRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El cliente no existe."));
    }

    public ClienteFormulario formularioDe(Integer id) {
        Cliente cliente = buscar(id);
        ClienteFormulario formulario = new ClienteFormulario();
        formulario.setNombre(cliente.getNombre());
        formulario.setTelefono(cliente.getTelefono());
        formulario.setPerfil(cliente.getPerfil());
        return formulario;
    }

    @Transactional
    public Cliente registrar(ClienteFormulario formulario) {
        String telefono = telefonoDe(formulario);
        if (telefono != null && clienteRepositorio.existsByTelefono(telefono)) {
            throw new ReglaNegocioException("Ya existe un cliente con ese teléfono.");
        }
        Cliente cliente = new Cliente();
        cliente.setNombre(formulario.getNombre().trim());
        cliente.setTelefono(telefono);
        cliente.setPerfil(formulario.getPerfil());
        Cliente guardado = clienteRepositorio.save(cliente);
        bitacoraServicio.registrar("CLIENTE_ALTA",
                "Se registró al cliente " + guardado.getNombre() + " con perfil " + guardado.getPerfilLegible() + ".");
        return guardado;
    }

    @Transactional
    public Cliente modificar(Integer id, ClienteFormulario formulario) {
        Cliente cliente = buscar(id);
        String telefono = telefonoDe(formulario);
        if (telefono != null && clienteRepositorio.existsByTelefonoAndIdNot(telefono, id)) {
            throw new ReglaNegocioException("Ya existe otro cliente con ese teléfono.");
        }
        String perfilAnterior = cliente.getPerfilLegible();
        boolean cambioPerfil = !cliente.getPerfil().equals(formulario.getPerfil());
        cliente.setNombre(formulario.getNombre().trim());
        cliente.setTelefono(telefono);
        cliente.setPerfil(formulario.getPerfil());
        Cliente guardado = clienteRepositorio.save(cliente);
        if (cambioPerfil) {
            bitacoraServicio.registrar("CLIENTE_PERFIL",
                    "El perfil de " + guardado.getNombre() + " cambió de " + perfilAnterior + " a "
                            + guardado.getPerfilLegible() + ".");
        } else {
            bitacoraServicio.registrar("CLIENTE_MODIFICACION", "Se modificó al cliente " + guardado.getNombre() + ".");
        }
        return guardado;
    }

    @Transactional
    public void darDeBaja(Integer id) {
        Cliente cliente = buscar(id);
        cliente.setActivo(false);
        clienteRepositorio.save(cliente);
        bitacoraServicio.registrar("CLIENTE_BAJA", "Se dio de baja al cliente " + cliente.getNombre() + ".");
    }

    @Transactional
    public void reactivar(Integer id) {
        Cliente cliente = buscar(id);
        cliente.setActivo(true);
        clienteRepositorio.save(cliente);
        bitacoraServicio.registrar("CLIENTE_REACTIVACION", "Se reactivó al cliente " + cliente.getNombre() + ".");
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim();
    }

    private String telefonoDe(ClienteFormulario formulario) {
        String telefono = formulario.getTelefono() == null ? "" : formulario.getTelefono().trim();
        return telefono.isEmpty() ? null : telefono;
    }
}
