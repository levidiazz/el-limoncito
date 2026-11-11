package mx.ellimoncito.servicio;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class CarritoSesion implements Serializable {

    private Integer idCliente;
    private final Map<Integer, Integer> lineas = new LinkedHashMap<>();

    public Integer getIdCliente() {
        return idCliente;
    }

    public void setIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public Map<Integer, Integer> getLineas() {
        return lineas;
    }

    public void vaciar() {
        lineas.clear();
        idCliente = null;
    }
}
