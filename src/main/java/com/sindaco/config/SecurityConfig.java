package com.sindaco.config;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/login", "/", "/index.html", "/app.js", "/estilo.css",
                        "/favicon.ico", "/error").permitAll()

                // CU01 · matrículas: alumno, recepcionista y administrador
                .requestMatchers("/api/matriculas/**")
                    .hasAnyRole("ALUMNO", "RECEPCIONISTA", "ADMINISTRADOR")
                // CU02 · pagos y comprobantes: solo administrador
                .requestMatchers("/api/pagos/**")
                    .hasAnyRole("ADMINISTRADOR")
                // CU03 · clases: administrador e instructor
                .requestMatchers("/api/clases/**")
                    .hasAnyRole("ADMINISTRADOR", "INSTRUCTOR")
                // CU04 · instructores y vehículos
                .requestMatchers("/api/instructores/**", "/api/vehiculos/**")
                    .hasAnyRole("ADMINISTRADOR", "INSTRUCTOR", "ENCARGADO_FLOTAS")
                // CU05 · asistencia y evaluación
                .requestMatchers("/api/asistencias/**")
                    .hasAnyRole("ADMINISTRADOR", "INSTRUCTOR")
                // CU06 · reporte de ingresos
                .requestMatchers("/api/reportes/**")
                    .hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                // CU07 · mantenimiento de vehículos
                .requestMatchers("/api/mantenimientos/**")
                    .hasAnyRole("ADMINISTRADOR", "INSTRUCTOR", "ENCARGADO_FLOTAS")
                // CU08 · paquetes y promociones. El alumno puede consultarlos para matricularse.
                .requestMatchers(HttpMethod.GET, "/api/paquetes/**", "/api/promociones/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "PROPIETARIO", "ALUMNO")
                .requestMatchers("/api/paquetes/**", "/api/promociones/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "PROPIETARIO")
                // CU09 · ausencias y reprogramaciones
                .requestMatchers("/api/reprogramaciones/**")
                    .hasAnyRole("ADMINISTRADOR", "ALUMNO")
                // CU10 · historial
                .requestMatchers("/api/historial/**")
                    .hasAnyRole("ADMINISTRADOR", "ALUMNO")

                // Apoyo a los formularios: lectura de alumnos para roles que operan matrículas o clases.
                .requestMatchers(HttpMethod.GET, "/api/alumnos/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "INSTRUCTOR", "ALUMNO")
                .requestMatchers("/api/alumnos/**")
                    .hasAnyRole("ADMINISTRADOR", "RECEPCIONISTA", "INSTRUCTOR")
                .requestMatchers("/api/usuarios/**")
                    .hasAnyRole("ADMINISTRADOR", "PROPIETARIO")
                .requestMatchers("/api/notificaciones/**", "/api/auth/me").authenticated()
                .requestMatchers("/api/**").denyAll()
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
