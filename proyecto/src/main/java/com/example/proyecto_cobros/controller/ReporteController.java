package com.example.proyecto_cobros.controller;

import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.entity.Mensualidad;
import com.example.proyecto_cobros.entity.Pago;
import com.example.proyecto_cobros.repository.EstudianteRepository;
import com.example.proyecto_cobros.repository.MensualidadRepository;
import com.example.proyecto_cobros.repository.PagoRepository;
import com.example.proyecto_cobros.service.ReportePdfService;
import com.example.proyecto_cobros.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/reportes")
public class ReporteController {

    private static final String[] MESES = {
        "Febrero", "Marzo", "Abril", "Mayo", "Junio",
        "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre"
    };

    // Mapa nombre de mes -> numero (2 al 11), coincide con la tabla mensualidades
    private static final Map<String, Integer> MES_NUMERO = new HashMap<>();
    static {
        MES_NUMERO.put("Febrero", 2);
        MES_NUMERO.put("Marzo", 3);
        MES_NUMERO.put("Abril", 4);
        MES_NUMERO.put("Mayo", 5);
        MES_NUMERO.put("Junio", 6);
        MES_NUMERO.put("Julio", 7);
        MES_NUMERO.put("Agosto", 8);
        MES_NUMERO.put("Septiembre", 9);
        MES_NUMERO.put("Octubre", 10);
        MES_NUMERO.put("Noviembre", 11);
    }

    @Autowired
    private EstudianteRepository estudianteRepo;

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private MensualidadRepository mensualidadRepo;

    @Autowired
    private ReportePdfService reportePdfService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String vista(Model model) {
        model.addAttribute("anioActual", LocalDate.now().getYear());
        return "reportes";
    }

    @GetMapping("/buscar")
    @ResponseBody
    public Map<String, Object> buscarReporte(@RequestParam("q") String q,
                                              @RequestParam("anio") Integer anio) {

        Map<String, Object> resultado = new HashMap<>();
        String texto = q.trim();

        Estudiante estudiante = null;

        if (texto.matches("\\d+")) {
            estudiante = estudianteRepo.findByNie(Integer.parseInt(texto)).orElse(null);
        } else {
            List<Estudiante> lista = estudianteRepo.findAll().stream()
                    .filter(e ->
                            (e.getNombres() != null && e.getNombres().toLowerCase().contains(texto.toLowerCase()))
                            || (e.getApellidos() != null && e.getApellidos().toLowerCase().contains(texto.toLowerCase()))
                            || (e.getNombreCompleto() != null && e.getNombreCompleto().toLowerCase().contains(texto.toLowerCase()))
                    )
                    .toList();

            if (lista.size() == 1) {
                estudiante = lista.get(0);
            } else if (lista.size() > 1) {
                resultado.put("tipo", "multiple");
                List<Map<String, Object>> opciones = new ArrayList<>();
                for (Estudiante e : lista) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nie", e.getNie());
                    item.put("nombreCompleto", e.getNombreCompleto());
                    opciones.add(item);
                }
                resultado.put("opciones", opciones);
                return resultado;
            }
        }

        if (estudiante == null) {
            resultado.put("tipo", "vacio");
            return resultado;
        }

        final Estudiante estudianteFinal = estudiante;

        resultado.put("tipo", "encontrado");
        resultado.put("nie", estudianteFinal.getNie());
        resultado.put("nombreCompleto", estudianteFinal.getNombreCompleto());
        resultado.put("seccion", estudianteFinal.getSeccion());
        resultado.put("grado", estudianteFinal.getGrado());
        resultado.put("correo", estudianteFinal.getCorreo());
        resultado.put("activo", estudianteFinal.getActivo());
        resultado.put("anio", anio);

        List<Pago> pagosDelAnio = pagoRepo.findAll().stream()
                .filter(p -> p.getNie().equals(estudianteFinal.getNie()))
                .filter(p -> p.getFecha_pago() != null && p.getFecha_pago().getYear() == anio)
                .toList();

        List<Mensualidad> mensualidadesDelAnio =
                mensualidadRepo.findByIdEstudianteAndAnio(estudianteFinal.getId_estudiante(), anio);

        List<Map<String, Object>> meses = new ArrayList<>();
        BigDecimal totalPagado = BigDecimal.ZERO;
        BigDecimal totalAdeudado = BigDecimal.ZERO;
        int mesesPagados = 0;
        int mesesPendientes = 0;

        for (String nombreMes : MESES) {

            Pago pagoDelMes = pagosDelAnio.stream()
                    .filter(p -> p.getMes().equalsIgnoreCase(nombreMes))
                    .findFirst()
                    .orElse(null);

            Map<String, Object> item = new HashMap<>();
            item.put("mes", nombreMes);

            if (pagoDelMes != null) {
                item.put("pagado", true);
                item.put("monto", pagoDelMes.getMonto());
                item.put("fecha", pagoDelMes.getFecha_pago().toString());
                item.put("metodoPago", pagoDelMes.getMetodo_pago());
                item.put("numeroComprobante", pagoDelMes.getNumeroComprobante());
                item.put("correoEnviado", Boolean.TRUE.equals(pagoDelMes.getCorreoEnviado()));
                totalPagado = totalPagado.add(pagoDelMes.getMonto());
                mesesPagados++;
            } else {
                item.put("pagado", false);
                mesesPendientes++;

                Integer numeroMes = MES_NUMERO.get(nombreMes);
                Mensualidad mensualidad = mensualidadesDelAnio.stream()
                        .filter(m -> m.getMes() != null && m.getMes().equals(numeroMes))
                        .findFirst()
                        .orElse(null);

                if (mensualidad != null && mensualidad.getMonto() != null) {
                    item.put("montoAdeudado", mensualidad.getMonto());
                    totalAdeudado = totalAdeudado.add(mensualidad.getMonto());
                } else {
                    item.put("montoAdeudado", null);
                }
            }

            meses.add(item);
        }

        resultado.put("meses", meses);
        resultado.put("totalPagado", totalPagado);
        resultado.put("totalAdeudado", totalAdeudado);
        resultado.put("mesesPagados", mesesPagados);
        resultado.put("mesesPendientes", mesesPendientes);

        return resultado;
    }

    @PostMapping("/enviar-correo")
    @ResponseBody
    public Map<String, Object> enviarReportePorCorreo(@RequestParam("nie") Integer nie,
                                                        @RequestParam("anio") Integer anio) {

        Map<String, Object> resultado = new HashMap<>();

        Estudiante estudiante = estudianteRepo.findByNie(nie).orElse(null);

        if (estudiante == null) {
            resultado.put("exito", false);
            resultado.put("mensaje", "Estudiante no encontrado.");
            return resultado;
        }

        if (estudiante.getCorreo() == null || estudiante.getCorreo().isBlank()) {
            resultado.put("exito", false);
            resultado.put("mensaje", "El estudiante no tiene correo registrado.");
            return resultado;
        }

        try {
            List<Pago> pagosDelAnio = pagoRepo.findAll().stream()
                    .filter(p -> p.getNie().equals(nie))
                    .filter(p -> p.getFecha_pago() != null && p.getFecha_pago().getYear() == anio)
                    .toList();

            List<Mensualidad> mensualidadesDelAnio =
                    mensualidadRepo.findByIdEstudianteAndAnio(estudiante.getId_estudiante(), anio);

            byte[] pdfBytes = reportePdfService.generarReportePdf(estudiante, anio, pagosDelAnio, mensualidadesDelAnio);

            emailService.enviarReporte(estudiante.getCorreo(), estudiante.getNombreCompleto(), anio, pdfBytes);

            resultado.put("exito", true);
            resultado.put("mensaje", "✅ Reporte enviado a " + estudiante.getCorreo());

        } catch (Exception e) {
            resultado.put("exito", false);
            resultado.put("mensaje", "⚠️ Error enviando el reporte: " + e.getMessage());
        }

        return resultado;
    }

    @GetMapping("/porcentaje-mensual")
    @ResponseBody
    public List<Map<String, Object>> porcentajeMensual(
            @RequestParam(value = "anio", required = false) Integer anio) {

        int anioConsulta = (anio != null) ? anio : LocalDate.now().getYear();

        long totalEstudiantes = estudianteRepo.count();

        List<Pago> pagosDelAnio = pagoRepo.findAll().stream()
                .filter(p -> "Pagado".equalsIgnoreCase(p.getEstado()))
                .filter(p -> p.getFecha_pago() != null && p.getFecha_pago().getYear() == anioConsulta)
                .toList();

        List<Map<String, Object>> resultado = new ArrayList<>();

        for (String nombreMes : MESES) {

            long estudiantesQuePagaron = pagosDelAnio.stream()
                    .filter(p -> p.getMes().equalsIgnoreCase(nombreMes))
                    .map(Pago::getNie)
                    .distinct()
                    .count();

            double porcentaje = totalEstudiantes == 0 ? 0
                    : (estudiantesQuePagaron * 100.0) / totalEstudiantes;

            Map<String, Object> item = new HashMap<>();
            item.put("mes", nombreMes);
            item.put("porcentaje", Math.round(porcentaje * 10.0) / 10.0);
            resultado.add(item);
        }

        return resultado;
    }
}