package com.sindaco.comun.exception;

import java.time.LocalDateTime;
import java.util.List;

/** Formato único de error que recibe el frontend. */
public record ApiError(int estado, String mensaje, List<String> detalles, LocalDateTime fecha) {
}
