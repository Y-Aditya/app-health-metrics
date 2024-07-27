package com.crc.healthmetrics.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
        jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(new KeycloakRoleConverter());

        http
            .authorizeHttpRequests(authorizeRequests ->
                authorizeRequests
                    .requestMatchers("/api/v1/health-metrics").hasAnyRole("ADMIN","USER")
                    .requestMatchers("/api/v1/metrics/start-capture", "/api/v1/metrics/stop-capture").hasRole("ADMIN")
                    //.requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**").permitAll()
                    // Allow Swagger UI and API docs without authentication
                    .anyRequest().authenticated()
            ).csrf(csrf -> csrf
                    .ignoringRequestMatchers("/api/v1/metrics/start-capture", "/api/v1/metrics/stop-capture")
                    .ignoringRequestMatchers("/swagger-ui.html", "/swagger-ui/**", "/api-docs/**")
            );
        //to act as resoruce server
        http.oauth2ResourceServer(rsc -> rsc.jwt(jwtConfigurer ->
                jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter)));

        return http.build();
    }
}
