package com.sindaco.academico.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sindaco.academico.dto.IngresoMensual;
import com.sindaco.academico.dto.IngresoPorMetodo;
import com.sindaco.academico.dto.ReporteIngresosResponse;
import com.sindaco.comun.RangoFechas;
import com.sindaco.comun.exception.ReglaNegocioException;
import com.sindaco.comun.notificacion.service.NotificacionService;
import com.sindaco.matricula.service.PagoService;

/** Implementación del reporte de ingresos (CU06). */
@Service
@Transactional(readOnly = true)
public class ReporteServiceImpl implements ReporteService {

    private final PagoService pagoService;
    private final NotificacionService notificacionService;

    public ReporteServiceImpl(PagoService pagoService, NotificacionService notificacionService) {
        this.pagoService = pagoService;
        this.notificacionService = notificacionService;
    }

    @Override
    public ReporteIngresosResponse ingresos(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = RangoFechas.inicio(desde);
        LocalDate fin = RangoFechas.fin(hasta);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        var pagos = pagoService.listarPorFecha(inicio, fin);
        if (pagos == null || pagos.isEmpty()) {
            return new ReporteIngresosResponse(
                    dosDecimales(BigDecimal.ZERO), 0L, dosDecimales(BigDecimal.ZERO), List.of(), List.of());
        }

        BigDecimal total = pagos.stream()
                .map(p -> valor(p.monto()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long totalPagos = pagos.size();
        BigDecimal promedio = dosDecimales(total.divide(BigDecimal.valueOf(totalPagos), 2, RoundingMode.HALF_UP));

        var agrupadoPorMetodo = pagos.stream().collect(Collectors.groupingBy(
                p -> texto(p.metodo()), TreeMap::new, Collectors.toList()));
        List<IngresoPorMetodo> porMetodo = agrupadoPorMetodo.entrySet().stream()
                .map(entry -> new IngresoPorMetodo(
                        entry.getKey(),
                        dosDecimales(entry.getValue().stream()
                                .map(p -> valor(p.monto()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add)),
                        entry.getValue().size()))
                .toList();

        var agrupadoPorMes = pagos.stream()
                .filter(p -> p.fecha() != null)
                .collect(Collectors.groupingBy(
                        p -> YearMonth.from(p.fecha()).toString(), TreeMap::new, Collectors.toList()));
        List<IngresoMensual> porMes = agrupadoPorMes.entrySet().stream()
                .map(entry -> new IngresoMensual(
                        entry.getKey(),
                        dosDecimales(entry.getValue().stream()
                                .map(p -> valor(p.monto()))
                                .reduce(BigDecimal.ZERO, BigDecimal::add))))
                .toList();

        return new ReporteIngresosResponse(
                dosDecimales(total), totalPagos, promedio, porMetodo, porMes);
    }

    @Override
    @Transactional
    public String exportarCsv(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = RangoFechas.inicio(desde);
        LocalDate fin = RangoFechas.fin(hasta);
        if (inicio.isAfter(fin)) {
            throw new ReglaNegocioException("La fecha desde no puede ser posterior a la fecha hasta");
        }

        var pagos = pagoService.listarPorFecha(inicio, fin);
        StringBuilder csv = new StringBuilder();
        csv.append("fecha;comprobante;matricula_id;alumno;metodo;tipo;monto\n");
        if (pagos != null) {
            for (var pago : pagos) {
                csv.append(escapar(valor(pago.fecha()))).append(';')
                        .append(escapar(valor(pago.comprobante()))).append(';')
                        .append(escapar(valor(pago.matriculaId()))).append(';')
                        .append(escapar(valor(pago.alumnoNombre()))).append(';')
                        .append(escapar(valor(pago.metodo()))).append(';')
                        .append(escapar(valor(pago.tipo()))).append(';')
                        .append(dosDecimales(valorMonto(pago.monto())).toPlainString())
                        .append('\n');
            }
        }

        notificacionService.registrar("MTC", "Reporte de ingresos enviado al MTC",
                "Se generó el reporte de ingresos del " + inicio + " al " + fin
                        + " con " + (pagos == null ? 0 : pagos.size()) + " pagos.",
                "MTC", null);
        return csv.toString();
    }

    private String valor(Object valor) {
        return valor == null ? "" : valor.toString();
    }

    private BigDecimal valorMonto(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    /** Escapa comillas, separadores y saltos de línea para que el CSV se abra correctamente. */
    private String escapar(String valor) {
        String resultado = valor.replace("\"", "\"\"");
        return resultado.contains(";") || resultado.contains("\"") || resultado.contains("\n")
                ? "\"" + resultado + "\""
                : resultado;
    }

    private BigDecimal valor(BigDecimal monto) {
        return monto == null ? BigDecimal.ZERO : monto;
    }

    private BigDecimal dosDecimales(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }

    private String texto(Object valor) {
        return valor == null || valor.toString().isBlank() ? "SIN ESPECIFICAR" : valor.toString();
    }
}
