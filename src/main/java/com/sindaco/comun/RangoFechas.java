package com.sindaco.comun;

import java.time.LocalDate;

/**
 * Rangos de fecha seguros para consultas en PostgreSQL.
 * LocalDate.MIN/MAX están fuera del rango que PostgreSQL puede representar
 * (año -999999999), por lo que se usan límites prácticos para "sin filtro".
 */
public final class RangoFechas {

    public static final LocalDate INICIO = LocalDate.of(1900, 1, 1);
    public static final LocalDate FIN = LocalDate.of(2100, 12, 31);

    private RangoFechas() {
    }

    /** Devuelve {@code INICIO} si la fecha es nula o está antes del límite inferior. */
    public static LocalDate inicio(LocalDate desde) {
        return desde == null || desde.isBefore(INICIO) ? INICIO : desde;
    }

    /** Devuelve {@code FIN} si la fecha es nula o está después del límite superior. */
    public static LocalDate fin(LocalDate hasta) {
        return hasta == null || hasta.isAfter(FIN) ? FIN : hasta;
    }
}
