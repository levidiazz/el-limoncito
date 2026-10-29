package mx.ellimoncito.seguridad;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SeguridadConfig {

    @Bean
    public SecurityFilterChain cadenaDeFiltros(HttpSecurity http, AccesoCorrectoHandler accesoCorrecto,
                                               AccesoFallidoHandler accesoFallido) throws Exception {
        http
                .authorizeHttpRequests(reglas -> reglas
                        .requestMatchers("/login", "/css/**", "/js/**", "/error").permitAll()
                        .requestMatchers("/usuarios/**").hasRole("ADMINISTRADOR")
                        .requestMatchers("/categorias/**", "/productos/**", "/proveedores/**").hasRole("GERENTE_GENERAL")
                        .requestMatchers("/entradas/**", "/mermas/**", "/envases/**").hasRole("ALMACEN")
                        .requestMatchers("/existencias/**").hasAnyRole("GERENTE_GENERAL", "VENTAS", "ALMACEN")
                        .anyRequest().authenticated())
                .formLogin(formulario -> formulario
                        .loginPage("/login")
                        .successHandler(accesoCorrecto)
                        .failureHandler(accesoFallido)
                        .permitAll())
                .logout(salida -> salida
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?salida")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
                .httpBasic(basic -> basic.disable());
        return http.build();
    }
}
