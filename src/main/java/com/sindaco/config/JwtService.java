package com.sindaco.config;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import com.sindaco.seguridad.entity.Usuario;

/**
 * Servicio de infraestructura: crea y valida tokens JWT (HMAC-SHA256) sin dependencias externas.
 * El secret y el tiempo de expiración se configuran en application.properties.
 */
@Service
public class JwtService {

    private static final String ALGORITMO = "HmacSHA256";
    private final byte[] secret;
    private final long expiracionMinutos;
    private final ObjectMapper objectMapper;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expiration-minutes}") long expiracionMinutos,
                      ObjectMapper objectMapper) {
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expiracionMinutos = expiracionMinutos;
        this.objectMapper = objectMapper;
    }

    public String generarToken(Usuario usuario) {
        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");

        long ahora = System.currentTimeMillis();
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", usuario.getUsername());
        claims.put("rol", usuario.getRol().name());
        claims.put("iat", ahora / 1000);
        claims.put("exp", ahora / 1000 + expiracionMinutos * 60);

        String contenido = codificar(header) + "." + codificar(claims);
        return contenido + "." + codificar(firmar(contenido));
    }

    public boolean esValido(String token) {
        try {
            String[] partes = token.split("\\.");
            if (partes.length != 3) {
                return false;
            }
            byte[] esperada = firmar(partes[0] + "." + partes[1]);
            byte[] recibida = Base64.getUrlDecoder().decode(partes[2]);
            if (!java.security.MessageDigest.isEqual(esperada, recibida)) {
                return false;
            }
            Map<String, Object> claims = leerClaims(partes[1]);
            Object exp = claims.get("exp");
            return exp instanceof Number numero && numero.longValue() > System.currentTimeMillis() / 1000;
        } catch (Exception ex) {
            return false;
        }
    }

    public String extraerUsername(String token) {
        Object valor = leerClaims(partes(token)[1]).get("sub");
        return valor == null ? null : valor.toString();
    }

    public String extraerRol(String token) {
        Object valor = leerClaims(partes(token)[1]).get("rol");
        return valor == null ? null : valor.toString();
    }

    public LocalDateTime extraerExpiracion(String token) {
        Object valor = leerClaims(partes(token)[1]).get("exp");
        if (!(valor instanceof Number numero)) {
            return LocalDateTime.now();
        }
        return LocalDateTime.ofEpochSecond(numero.longValue(), 0, ZoneOffset.UTC);
    }

    private String[] partes(String token) {
        String[] partes = token.split("\\.");
        if (partes.length != 3) {
            throw new IllegalArgumentException("Token JWT mal formado");
        }
        return partes;
    }

    private Map<String, Object> leerClaims(String payload) {
        try {
            byte[] json = Base64.getUrlDecoder().decode(payload);
            return objectMapper.readValue(json, Map.class);
        } catch (JacksonException ex) {
            throw new IllegalArgumentException("No se pudo leer el token JWT", ex);
        }
    }

    private byte[] firmar(String contenido) {
        try {
            Mac mac = Mac.getInstance(ALGORITMO);
            mac.init(new SecretKeySpec(secret, ALGORITMO));
            return mac.doFinal(contenido.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new IllegalStateException("No se pudo firmar el token JWT", ex);
        }
    }

    private String codificar(Object valor) {
        try {
            byte[] json = objectMapper.writeValueAsBytes(valor);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(json);
        } catch (JacksonException ex) {
            throw new IllegalStateException("No se pudo serializar el token JWT", ex);
        }
    }

    private String codificar(byte[] valor) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(valor);
    }
}
