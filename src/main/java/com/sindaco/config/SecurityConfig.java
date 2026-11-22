package com.sindaco.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import tools.jackson.databind.ObjectMapper;
import com.sindaco.comun.exception.ApiError;
import com.sindaco.seguridad.repository.UsuarioRepository;

/** Capa de configuración / seguridad: JWT stateless, CORS y permisos por rol. */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${sindaco.cors.allowed-origins}")
    private String[] allowedOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter,
                                           ObjectMapper objectMapper) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/", "/index.html", "/app.js", "/estilo.css",
                        "/favicon.ico", "/error").permitAll()
                .requestMatchers("/api/reportes/**", "/api/historial/**")
                    .hasAnyRole("PROPIETARIO", "ADMINISTRADOR", "RECEPCIONISTA")
                .requestMatchers("/api/mantenimientos/**", "/api/vehiculos/**")
                    .hasAnyRole("ADMINISTRADOR", "ENCARGADO_FLOTAS", "PROPIETARIO")
                .requestMatchers("/api/asistencias/**")
                    .hasAnyRole("ADMINISTRADOR", "INSTRUCTOR", "PROPIETARIO", "RECEPCIONISTA")
                .requestMatchers("/api/usuarios/**")
                    .hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().permitAll())
            .exceptionHandling(e -> e
                .authenticationEntryPoint((request, response, ex) ->
                    escribirError(response, objectMapper, HttpStatus.UNAUTHORIZED, "Debe iniciar sesión para acceder"))
                .accessDeniedHandler((request, response, ex) ->
                    escribirError(response, objectMapper, HttpStatus.FORBIDDEN, "No tiene permiso para esta operación")))
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins).map(String::trim).toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Expone los usuarios de la BD a Spring Security. Evita el usuario en memoria
     * con contraseña generada que muestra Spring Boot por defecto.
     */
    @Bean
    public org.springframework.security.core.userdetails.UserDetailsService userDetailsService(
            UsuarioRepository usuarioRepository) {
        return username -> usuarioRepository.findByUsername(username)
                .map(usuario -> org.springframework.security.core.userdetails.User
                        .withUsername(usuario.getUsername())
                        .password(usuario.getPassword())
                        .roles(usuario.getRol().name())
                        .disabled(!usuario.isActivo())
                        .build())
                .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException(
                        "Usuario no encontrado: " + username));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    private void escribirError(jakarta.servlet.http.HttpServletResponse response, ObjectMapper mapper,
                               HttpStatus status, String mensaje) throws java.io.IOException {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        mapper.writeValue(response.getWriter(),
                new ApiError(status.value(), mensaje, List.of(), LocalDateTime.now()));
    }
}
