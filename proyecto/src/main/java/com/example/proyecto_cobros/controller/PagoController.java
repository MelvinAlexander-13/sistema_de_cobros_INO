package com.example.proyecto_cobros.controller;

import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.entity.Pago;
import com.example.proyecto_cobros.repository.EstudianteRepository;
import com.example.proyecto_cobros.repository.PagoRepository;
import com.example.proyecto_cobros.service.ComprobantePdfService;
import com.example.proyecto_cobros.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoRepository repo;

    @Autowired
    private EstudianteRepository estudianteRepo;

    @Autowired
    private ComprobantePdfService comprobantePdfService;

    @Autowired
    private EmailService emailService;

    @GetMapping
    public String listarPagos(Model model, HttpSession session) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("listaPagos", repo.findAll());
        model.addAttribute("pago", new Pago());
        return "pagos";
    }

    @PostMapping("/guardar")
    public String guardarPago(@ModelAttribute Pago pago,
                               Model model,
                               HttpSession session,
                               RedirectAttributes redirectAttributes) {

        model.addAttribute("usuario", session.getAttribute("usuario"));

        Integer idUsuarioLogueado = (Integer) session.getAttribute("id_usuario");
        if (idUsuarioLogueado == null) {
            idUsuarioLogueado = 1;
        }
        pago.setId_usuario(idUsuarioLogueado);

        if (pago.getActivo() == null) {
            pago.setActivo(true);
        }

        if (pago.getMetodo_pago() == null || pago.getMetodo_pago().isBlank()) {
            pago.setMetodo_pago("efectivo");
        }

        Pago pagoExistente = repo.findByNieAndMes(pago.getNie(), pago.getMes()).orElse(null);

        if (pagoExistente != null) {
            model.addAttribute("error", "⚠️ El estudiante ya tiene pagado el mes de " + pago.getMes());
            model.addAttribute("listaPagos", repo.findAll());
            model.addAttribute("pago", new Pago());
            return "pagos";
        }

        repo.save(pago);

        // GENERAR NUMERO DE COMPROBANTE
        int anioActual = LocalDate.now().getYear();
        String numeroComprobante = "OSC-" + anioActual + "-" + String.format("%06d", pago.getId_pago());
        pago.setNumeroComprobante(numeroComprobante);
        repo.save(pago);

        // BUSCAR ESTUDIANTE PARA PDF Y CORREO
        Estudiante estudiante = estudianteRepo.findByNie(pago.getNie()).orElse(null);

                try {
            byte[] pdfBytes = comprobantePdfService.generarComprobantePdf(pago, estudiante);

            if (estudiante != null && estudiante.getCorreo() != null && !estudiante.getCorreo().isBlank()) {

                emailService.enviarComprobante(
                        estudiante.getCorreo(),
                        pago.getEstudiante(),
                        numeroComprobante,
                        pdfBytes
                );

                pago.setCorreoEnviado(true);
                repo.save(pago);

                redirectAttributes.addFlashAttribute("mensaje",
                        "✅ Pago guardado. Comprobante " + numeroComprobante + " enviado a " + estudiante.getCorreo());
            } else {
                redirectAttributes.addFlashAttribute("mensaje",
                        "✅ Pago guardado (comprobante " + numeroComprobante + "). El estudiante no tiene correo registrado.");
            }

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error",
                    "⚠️ Pago guardado, pero hubo un problema generando/enviando el comprobante: " + e.getMessage());
        }

        return "redirect:/pagos";
    }

    @GetMapping("/buscar")
    public String buscarPago(@RequestParam("nie") String nie, Model model, HttpSession session) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        model.addAttribute("listaPagos", repo.findByNieContaining(nie));
        model.addAttribute("pago", new Pago());
        return "pagos";
    }

    @GetMapping("/buscar-mes")
    public String buscarPorMes(@RequestParam("nie") Integer nie,
                                @RequestParam("mes") String mes,
                                Model model, HttpSession session) {
        model.addAttribute("usuario", session.getAttribute("usuario"));

        Pago pago = repo.findByNieAndMes(nie, mes).orElse(null);

        if (pago != null) {
            model.addAttribute("listaPagos", List.of(pago));
            model.addAttribute("mensaje", "✅ El pago del mes de " + mes + " ya fue realizado.");
        } else {
            model.addAttribute("listaPagos", List.of());
            model.addAttribute("error", "⚠️ El estudiante tiene pendiente el pago de " + mes);
        }

        model.addAttribute("pago", new Pago());
        return "pagos";
    }

    @GetMapping("/editar/{id}")
    public String editarPago(@PathVariable Integer id, Model model, HttpSession session) {
        model.addAttribute("usuario", session.getAttribute("usuario"));
        Pago pago = repo.findById(id).orElse(null);
        model.addAttribute("pago", pago);
        model.addAttribute("listaPagos", repo.findAll());
        return "pagos";
    }

    @PostMapping("/actualizar")
    public String actualizarPago(@ModelAttribute Pago pago, Model model, HttpSession session) {
        model.addAttribute("usuario", session.getAttribute("usuario"));

        Integer idUsuarioLogueado = (Integer) session.getAttribute("id_usuario");
        if (idUsuarioLogueado == null) {
            idUsuarioLogueado = 1;
        }
        pago.setId_usuario(idUsuarioLogueado);

        if (pago.getActivo() == null) {
            pago.setActivo(true);
        }

        Pago existente = repo.findByNieAndMes(pago.getNie(), pago.getMes()).orElse(null);

        if (existente != null && !existente.getId_pago().equals(pago.getId_pago())) {
            model.addAttribute("error", "⚠️ Ya existe un pago para ese NIE y mes.");
            model.addAttribute("listaPagos", repo.findAll());
            model.addAttribute("pago", pago);
            return "pagos";
        }

        repo.save(pago);
        return "redirect:/pagos";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarPago(@PathVariable Integer id) {
        repo.deleteById(id);
        return "redirect:/pagos";
    }
}