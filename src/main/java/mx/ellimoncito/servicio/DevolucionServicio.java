package mx.ellimoncito.servicio;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mx.ellimoncito.formulario.DevolucionFormulario;
import mx.ellimoncito.modelo.Cliente;
import mx.ellimoncito.modelo.MovimientoEnvase;
import mx.ellimoncito.modelo.TipoEnvase;
import mx.ellimoncito.repositorio.ClienteRepositorio;
import mx.ellimoncito.repositorio.DevolucionRepositorio;
import mx.ellimoncito.repositorio.TipoEnvaseRepositorio;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class DevolucionServicio {

    private static final List<String> TIPOS_CON_CLIENTE =
            List.of(MovimientoEnvase.COBRO_DEPOSITO, MovimientoEnvase.DEVOLUCION_CLIENTE);

    public record EnvasePendiente(TipoEnvase tipoEnvase, long cobrados, long devueltos) {

        public long pendientes() {
            return cobrados - devueltos;
        }

        public BigDecimal depositoPendiente() {
            return tipoEnvase.getValorDeposito().multiply(BigDecimal.valueOf(pendientes()));
        }
    }

    public record PendientesCliente(Cliente cliente, List<EnvasePendiente> envases) {

        public long totalPendientes() {
            return envases.stream().mapToLong(EnvasePendiente::pendientes).sum();
        }

        public BigDecimal depositoPendiente() {
            return envases.stream().map(EnvasePendiente::depositoPendiente).reduce(BigDecimal.ZERO, BigDecimal::add);
        }
    }

    public record DevolucionDelDia(MovimientoEnvase movimiento, String cliente) {
    }

    private final DevolucionRepositorio devolucionRepositorio;
    private final ClienteRepositorio clienteRepositorio;
    private final TipoEnvaseRepositorio tipoEnvaseRepositorio;
    private final UsuarioActualServicio usuarioActualServicio;
    private final BitacoraServicio bitacoraServicio;

    public DevolucionServicio(DevolucionRepositorio devolucionRepositorio, ClienteRepositorio clienteRepositorio,
                              TipoEnvaseRepositorio tipoEnvaseRepositorio,
                              UsuarioActualServicio usuarioActualServicio, BitacoraServicio bitacoraServicio) {
        this.devolucionRepositorio = devolucionRepositorio;
        this.clienteRepositorio = clienteRepositorio;
        this.tipoEnvaseRepositorio = tipoEnvaseRepositorio;
        this.usuarioActualServicio = usuarioActualServicio;
        this.bitacoraServicio = bitacoraServicio;
    }

    public List<DevolucionDelDia> devolucionesDelDia() {
        LocalDateTime inicio = LocalDate.now().atStartOfDay();
        List<MovimientoEnvase> movimientos = devolucionRepositorio
                .findByTipoAndIdClienteNotNullAndFechaHoraGreaterThanEqualOrderByFechaHoraDesc(
                        MovimientoEnvase.DEVOLUCION_CLIENTE, inicio);
        Map<Integer, String> nombres = nombresDeClientes(movimientos.stream().map(MovimientoEnvase::getIdCliente).toList());
        return movimientos.stream()
                .map(m -> new DevolucionDelDia(m, nombres.getOrDefault(m.getIdCliente(), "—")))
                .toList();
    }

    public BigDecimal totalReintegradoHoy(List<DevolucionDelDia> devoluciones) {
        return devoluciones.stream().map(d -> d.movimiento().getMonto()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public List<PendientesCliente> clientesConPendientes() {
        List<MovimientoEnvase> movimientos = devolucionRepositorio.findByIdClienteNotNullAndTipoIn(TIPOS_CON_CLIENTE);
        Map<Integer, List<MovimientoEnvase>> porCliente = new LinkedHashMap<>();
        for (MovimientoEnvase movimiento : movimientos) {
            porCliente.computeIfAbsent(movimiento.getIdCliente(), k -> new ArrayList<>()).add(movimiento);
        }
        Map<Integer, Cliente> clientes = new LinkedHashMap<>();
        clienteRepositorio.findAllById(porCliente.keySet()).forEach(c -> clientes.put(c.getId(), c));
        List<PendientesCliente> resultado = new ArrayList<>();
        for (Map.Entry<Integer, List<MovimientoEnvase>> entrada : porCliente.entrySet()) {
            List<EnvasePendiente> envases = pendientesDe(entrada.getValue());
            Cliente cliente = clientes.get(entrada.getKey());
            if (!envases.isEmpty() && cliente != null) {
                resultado.add(new PendientesCliente(cliente, envases));
            }
        }
        resultado.sort(Comparator.comparing(p -> p.cliente().getNombre(), String.CASE_INSENSITIVE_ORDER));
        return resultado;
    }

    public List<EnvasePendiente> pendientesDelCliente(Integer idCliente) {
        return pendientesDe(devolucionRepositorio.findByIdClienteAndTipoIn(idCliente, TIPOS_CON_CLIENTE));
    }

    public Cliente cliente(Integer idCliente) {
        return clienteRepositorio.findById(idCliente)
                .orElseThrow(() -> new ReglaNegocioException("El cliente no existe."));
    }

    @Transactional
    public void registrar(DevolucionFormulario formulario) {
        Cliente cliente = cliente(formulario.getIdCliente());
        TipoEnvase tipo = tipoEnvaseRepositorio.findById(formulario.getIdTipoEnvase())
                .orElseThrow(() -> new ReglaNegocioException("El tipo de envase no existe."));
        long pendientes = pendientesDelCliente(cliente.getId()).stream()
                .filter(p -> p.tipoEnvase().getId().equals(tipo.getId()))
                .mapToLong(EnvasePendiente::pendientes)
                .sum();
        if (pendientes <= 0) {
            throw new ReglaNegocioException(cliente.getNombre() + " no tiene envases pendientes de ese tipo.");
        }
        if (formulario.getCantidad() > pendientes) {
            throw new ReglaNegocioException("El cliente solo tiene " + pendientes + " envases pendientes de ese tipo.");
        }
        BigDecimal monto = tipo.getValorDeposito().multiply(BigDecimal.valueOf(formulario.getCantidad()));
        tipo.setVaciosEnBodega(tipo.getVaciosEnBodega() + formulario.getCantidad());
        tipoEnvaseRepositorio.save(tipo);

        MovimientoEnvase movimiento = new MovimientoEnvase();
        movimiento.setTipoEnvase(tipo);
        movimiento.setUsuario(usuarioActualServicio.obtener());
        movimiento.setIdCliente(cliente.getId());
        movimiento.setTipo(MovimientoEnvase.DEVOLUCION_CLIENTE);
        movimiento.setCantidad(formulario.getCantidad());
        movimiento.setMonto(monto);
        movimiento.setFechaHora(LocalDateTime.now());
        devolucionRepositorio.save(movimiento);
        bitacoraServicio.registrar("DEVOLUCION_ALTA",
                cliente.getNombre() + " devolvió " + formulario.getCantidad() + " de " + tipo.getNombre()
                        + ". Depósito reintegrado: $" + monto + ".");
    }

    @Transactional
    public void cancelar(Integer id) {
        MovimientoEnvase movimiento = devolucionRepositorio.findById(id)
                .orElseThrow(() -> new ReglaNegocioException("La devolución no existe."));
        if (!MovimientoEnvase.DEVOLUCION_CLIENTE.equals(movimiento.getTipo()) || movimiento.getIdCliente() == null) {
            throw new ReglaNegocioException("El movimiento no es una devolución de cliente.");
        }
        if (movimiento.getFechaHora().toLocalDate().isBefore(LocalDate.now())) {
            throw new ReglaNegocioException("Solo se pueden cancelar devoluciones del día.");
        }
        TipoEnvase tipo = tipoEnvaseRepositorio.findById(movimiento.getTipoEnvase().getId())
                .orElseThrow(() -> new ReglaNegocioException("El tipo de envase no existe."));
        if (tipo.getVaciosEnBodega() < movimiento.getCantidad()) {
            throw new ReglaNegocioException("No se puede cancelar: esos envases ya salieron de la bodega.");
        }
        tipo.setVaciosEnBodega(tipo.getVaciosEnBodega() - movimiento.getCantidad());
        tipoEnvaseRepositorio.save(tipo);
        String cliente = nombresDeClientes(List.of(movimiento.getIdCliente())).getOrDefault(movimiento.getIdCliente(), "cliente");
        devolucionRepositorio.delete(movimiento);
        bitacoraServicio.registrar("DEVOLUCION_CANCELACION",
                "Se canceló la devolución de " + movimiento.getCantidad() + " de " + tipo.getNombre() + " de "
                        + cliente + ". El depósito y los envases se revirtieron.");
    }

    private List<EnvasePendiente> pendientesDe(List<MovimientoEnvase> movimientos) {
        Map<Integer, TipoEnvase> tipos = new LinkedHashMap<>();
        Map<Integer, Long> cobrados = new LinkedHashMap<>();
        Map<Integer, Long> devueltos = new LinkedHashMap<>();
        for (MovimientoEnvase movimiento : movimientos) {
            Integer idTipo = movimiento.getTipoEnvase().getId();
            tipos.putIfAbsent(idTipo, movimiento.getTipoEnvase());
            Map<Integer, Long> destino = MovimientoEnvase.COBRO_DEPOSITO.equals(movimiento.getTipo()) ? cobrados : devueltos;
            destino.merge(idTipo, (long) movimiento.getCantidad(), Long::sum);
        }
        List<EnvasePendiente> resultado = new ArrayList<>();
        for (Map.Entry<Integer, TipoEnvase> entrada : tipos.entrySet()) {
            EnvasePendiente envase = new EnvasePendiente(entrada.getValue(),
                    cobrados.getOrDefault(entrada.getKey(), 0L), devueltos.getOrDefault(entrada.getKey(), 0L));
            if (envase.pendientes() > 0) {
                resultado.add(envase);
            }
        }
        resultado.sort(Comparator.comparing(e -> e.tipoEnvase().getNombre()));
        return resultado;
    }

    private Map<Integer, String> nombresDeClientes(List<Integer> ids) {
        Map<Integer, String> nombres = new LinkedHashMap<>();
        clienteRepositorio.findAllById(ids).forEach(c -> nombres.put(c.getId(), c.getNombre()));
        return nombres;
    }
}
