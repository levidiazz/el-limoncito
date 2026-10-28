package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import mx.ellimoncito.modelo.MovimientoEnvase;
import mx.ellimoncito.modelo.TipoEnvase;
import mx.ellimoncito.repositorio.MovimientoEnvaseRepositorio;
import mx.ellimoncito.repositorio.TipoEnvaseRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EnvaseServicio {

    private final TipoEnvaseRepositorio tipoEnvaseRepositorio;
    private final MovimientoEnvaseRepositorio movimientoEnvaseRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;

    public EnvaseServicio(TipoEnvaseRepositorio tipoEnvaseRepositorio,
                          MovimientoEnvaseRepositorio movimientoEnvaseRepositorio,
                          UsuarioActualServicio usuarioActualServicio) {
        this.tipoEnvaseRepositorio = tipoEnvaseRepositorio;
        this.movimientoEnvaseRepositorio = movimientoEnvaseRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
    }

    public List<TipoEnvase> listar() {
        return tipoEnvaseRepositorio.findAllByOrderByNombreAsc();
    }

    public List<TipoEnvase> listarActivos() {
        return tipoEnvaseRepositorio.findByActivoTrueOrderByNombreAsc();
    }

    public List<MovimientoEnvase> ultimosMovimientos() {
        return movimientoEnvaseRepositorio.findTop20ByOrderByFechaHoraDesc();
    }

    public TipoEnvase buscar(Integer id) {
        return tipoEnvaseRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("El tipo de envase no existe."));
    }

    @Transactional
    public TipoEnvase registrar(TipoEnvase tipo) {
        String nombre = tipo.getNombre().trim();
        if (tipoEnvaseRepositorio.existsByNombreIgnoreCase(nombre)) {
            throw new ReglaNegocioException("Ya existe un tipo de envase con ese nombre.");
        }
        TipoEnvase nuevo = new TipoEnvase();
        nuevo.setNombre(nombre);
        nuevo.setValorDeposito(tipo.getValorDeposito());
        nuevo.setVaciosEnBodega(0);
        nuevo.setActivo(true);
        return tipoEnvaseRepositorio.save(nuevo);
    }

    @Transactional
    public TipoEnvase modificar(Integer id, TipoEnvase datos) {
        TipoEnvase tipo = buscar(id);
        String nombre = datos.getNombre().trim();
        if (tipoEnvaseRepositorio.existsByNombreIgnoreCaseAndIdNot(nombre, id)) {
            throw new ReglaNegocioException("Ya existe un tipo de envase con ese nombre.");
        }
        tipo.setNombre(nombre);
        tipo.setValorDeposito(datos.getValorDeposito());
        return tipoEnvaseRepositorio.save(tipo);
    }

    @Transactional
    public String darDeBajaOEliminar(Integer id) {
        TipoEnvase tipo = buscar(id);
        if (movimientoEnvaseRepositorio.existsByTipoEnvaseId(id) || tipo.getVaciosEnBodega() > 0) {
            tipo.setActivo(false);
            tipoEnvaseRepositorio.save(tipo);
            return "El tipo de envase tiene movimientos y se desactivó.";
        }
        tipoEnvaseRepositorio.delete(tipo);
        return "El tipo de envase se eliminó.";
    }

    @Transactional
    public void reactivar(Integer id) {
        TipoEnvase tipo = buscar(id);
        tipo.setActivo(true);
        tipoEnvaseRepositorio.save(tipo);
    }

    @Transactional
    public void registrarEntregaAProveedor(Integer idTipo, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        TipoEnvase tipo = buscar(idTipo);
        if (!tipo.getActivo()) {
            throw new ReglaNegocioException("El tipo de envase está desactivado.");
        }
        if (cantidad > tipo.getVaciosEnBodega()) {
            throw new ReglaNegocioException("Solo hay " + tipo.getVaciosEnBodega() + " envases vacíos de ese tipo en bodega.");
        }
        tipo.setVaciosEnBodega(tipo.getVaciosEnBodega() - cantidad);
        tipoEnvaseRepositorio.save(tipo);

        MovimientoEnvase movimiento = new MovimientoEnvase();
        movimiento.setTipoEnvase(tipo);
        movimiento.setUsuario(usuarioActualServicio.obtener());
        movimiento.setTipo(MovimientoEnvase.ENTREGA_PROVEEDOR);
        movimiento.setCantidad(cantidad);
        movimiento.setMonto(BigDecimal.ZERO);
        movimiento.setFechaHora(LocalDateTime.now());
        movimientoEnvaseRepositorio.save(movimiento);
    }

    @Transactional
    public void registrarVaciosRecibidos(Integer idTipo, Integer cantidad) {
        if (cantidad == null || cantidad <= 0) {
            throw new ReglaNegocioException("La cantidad debe ser mayor que cero.");
        }
        TipoEnvase tipo = buscar(idTipo);
        if (!tipo.getActivo()) {
            throw new ReglaNegocioException("El tipo de envase está desactivado.");
        }
        tipo.setVaciosEnBodega(tipo.getVaciosEnBodega() + cantidad);
        tipoEnvaseRepositorio.save(tipo);

        MovimientoEnvase movimiento = new MovimientoEnvase();
        movimiento.setTipoEnvase(tipo);
        movimiento.setUsuario(usuarioActualServicio.obtener());
        movimiento.setTipo(MovimientoEnvase.DEVOLUCION_CLIENTE);
        movimiento.setCantidad(cantidad);
        movimiento.setMonto(tipo.getValorDeposito().multiply(BigDecimal.valueOf(cantidad)));
        movimiento.setFechaHora(LocalDateTime.now());
        movimientoEnvaseRepositorio.save(movimiento);
    }
}
