package es.florida.biblioteca.config;

import java.time.Clock;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** Reloj unico de la aplicacion: usa la zona horaria del sistema (Europe/Madrid en la instancia). */
@Configuration
public class RelojConfig {

    @Bean
    public Clock reloj() {
        return Clock.systemDefaultZone();
    }
}
