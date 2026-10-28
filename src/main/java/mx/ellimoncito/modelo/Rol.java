package mx.ellimoncito.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "rol")
public class Rol {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_rol")
    private Integer id;

    @Column(nullable = false, unique = true, length = 30)
    private String nombre;

    @Column(length = 150)
    private String descripcion;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getNombreLegible() {
        return switch (nombre) {
            case "ADMINISTRADOR" -> "Administrador";
            case "GERENTE_GENERAL" -> "Gerente General";
            case "VENTAS" -> "Ventas";
            case "ALMACEN" -> "Almacén";
            default -> nombre;
        };
    }
}
