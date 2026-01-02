package com.playko.zoologico.configuration;


import com.playko.zoologico.configuration.security.AuditorAwareImpl;
import com.playko.zoologico.entity.Usuario;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaAuditingConfig {

    @Bean
    public AuditorAware<Usuario> auditorProvider() {
        return new AuditorAwareImpl();
    }
}