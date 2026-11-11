package mx.ellimoncito.modelo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.List;

@Entity
@Table(name = "cliente")
public class Cliente {

    public static final String MINORISTA = "MINORISTA";
    public static final String MAYORISTA = "MAYORISTA";
    public static final String DISTRIBUIDOR = "DISTRIBUIDOR";
    public static final List<String> PERFILES = List.of(MINORISTA, MAYORISTA, DISTRIBUIDOR);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Integer id;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Column(length = 10)
    private String telefono;

    @Column(nullable = false, length = 15)
    private String perfil = MINORISTA;

    @Column(nullable = false)
    private Boolean activo = true;

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

    public String getTelefono() {
        return telefono;
    }

    public void setTelefono(String telefono) {
        this.telefono = telefono;
    }

    public String getPerfil() {
        return perfil;
    }

    public void setPerfil(String perfil) {
        this.perfil = perfil;
    }

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }

    public String getPerfilLegible() {
        return switch (perfil) {
            case MINORISTA -> "Minorista";
            case MAYORISTA -> "Mayorista";
            case DISTRIBUIDOR -> "Distribuidor";
            default -> perfil;
        };
    }

    public boolean isPrecioMayoreo() {
        return MAYORISTA.equals(perfil) || DISTRIBUIDOR.equals(perfil);
    }
}
