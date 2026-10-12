package com.example.proyecto_cobros.controller;

import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.repository.EstudianteRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/estudiantes")
public class EstudianteController {

    @Autowired
    private EstudianteRepository repo;

    // =========================
    // LISTAR (activos primero, alfabético)
    // =========================
    @GetMapping
    public String listar(Model model) {

        List<Estudiante> lista = repo.findAll().stream()
                .sorted((a, b) -> {
                    boolean activoA = Boolean.TRUE.equals(a.getActivo());
                    boolean activoB = Boolean.TRUE.equals(b.getActivo());
                    if (activoA == activoB) {
                        return a.getNombres().compareToIgnoreCase(b.getNombres());
                    }
                    return activoA ? -1 : 1;
                })
                .toList();

        model.addAttribute("listaEstudiantes", lista);
        model.addAttribute("estudiante", new Estudiante());
        return "estudiantes";
    }

    @GetMapping("/nuevo")
    public String nuevo(Model model) {
        model.addAttribute("estudiante", new Estudiante());
        return "formulario_estudiante";
    }

    // =========================
    // GUARDAR (con validación de NIE duplicado)
    // =========================
    @PostMapping("/guardar")
    public String guardar(@ModelAttribute Estudiante estudiante,
                           Model model,
                           RedirectAttributes redirectAttributes) {

        if (estudiante.getActivo() == null) {
            estudiante.setActivo(true);
        }

        Optional<Estudiante> existente = repo.findByNie(estudiante.getNie());
        if (existente.isPresent()) {
            model.addAttribute("error", "⚠️ Ya existe un estudiante registrado con el NIE " + estudiante.getNie() + ".");
            model.addAttribute("estudiante", estudiante);
            return "formulario_estudiante";
        }

        try {
            repo.save(estudiante);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Estudiante agregado correctamente.");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "⚠️ Ya existe un estudiante registrado con ese NIE.");
            model.addAttribute("estudiante", estudiante);
            return "formulario_estudiante";
        }

        return "redirect:/estudiantes";
    }

    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Integer id, Model model) {
        Estudiante estudiante = repo.findById(id).orElse(null);
        if (estudiante == null) {
            return "redirect:/estudiantes";
        }
        model.addAttribute("estudiante", estudiante);
        return "formulario_estudiante";
    }

    // =========================
    // ACTUALIZAR (con validación de NIE duplicado)
    // =========================
    @PostMapping("/actualizar")
    public String actualizar(@ModelAttribute Estudiante estudiante,
                              Model model,
                              RedirectAttributes redirectAttributes) {

        Optional<Estudiante> existente = repo.findByNie(estudiante.getNie());
        if (existente.isPresent() && !existente.get().getId_estudiante().equals(estudiante.getId_estudiante())) {
            model.addAttribute("error", "⚠️ Ya existe otro estudiante registrado con el NIE " + estudiante.getNie() + ".");
            model.addAttribute("estudiante", estudiante);
            return "formulario_estudiante";
        }

        try {
            repo.save(estudiante);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Estudiante actualizado correctamente.");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "⚠️ Ya existe otro estudiante registrado con ese NIE.");
            model.addAttribute("estudiante", estudiante);
            return "formulario_estudiante";
        }

        return "redirect:/estudiantes";
    }

    // =========================
    // BUSCAR (por nombre, apellido, o nombre completo — activos primero)
    // =========================
    @GetMapping("/buscar")
    public String buscar(@RequestParam("texto") String texto, Model model) {

        List<Estudiante> todos = repo.findAll();
        String textoBusqueda = texto.trim().toLowerCase();

        List<Estudiante> estudiantes = todos.stream()
                .filter(e ->
                        (e.getNombres() != null && e.getNombres().toLowerCase().contains(textoBusqueda))
                        || (e.getApellidos() != null && e.getApellidos().toLowerCase().contains(textoBusqueda))
                        || (e.getNombreCompleto() != null && e.getNombreCompleto().toLowerCase().contains(textoBusqueda))
                )
                .toList();

        List<Estudiante> ordenados = estudiantes.stream()
                .sorted((a, b) -> {
                    boolean activoA = Boolean.TRUE.equals(a.getActivo());
                    boolean activoB = Boolean.TRUE.equals(b.getActivo());
                    if (activoA == activoB) {
                        return a.getNombres().compareToIgnoreCase(b.getNombres());
                    }
                    return activoA ? -1 : 1;
                })
                .toList();

        model.addAttribute("listaEstudiantes", ordenados);
        model.addAttribute("estudiante", new Estudiante());
        model.addAttribute("busqueda", texto);
        return "estudiantes";
    }

    @GetMapping("/estado/{id}")
    public String cambiarEstado(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        Estudiante estudiante = repo.findById(id).orElse(null);
        if (estudiante != null) {
            estudiante.setActivo(!Boolean.TRUE.equals(estudiante.getActivo()));
            repo.save(estudiante);

            String estadoTexto = Boolean.TRUE.equals(estudiante.getActivo()) ? "activado" : "desactivado";
            redirectAttributes.addFlashAttribute("mensaje", "✅ Estudiante " + estadoTexto + " correctamente.");
        }
        return "redirect:/estudiantes";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminar(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        try {
            repo.deleteById(id);
            redirectAttributes.addFlashAttribute("mensaje", "✅ Estudiante eliminado correctamente.");
        } catch (DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error",
                    "⚠️ No se puede eliminar este estudiante porque ya tiene mensualidades o pagos registrados. Usa 'Desactivar' en su lugar.");
        }
        return "redirect:/estudiantes";
    }

    @GetMapping("/buscar-nie")
    @ResponseBody
    public Map<String, Object> buscarPorNie(@RequestParam("nie") String nie) {
        Map<String, Object> resultado = new HashMap<>();
        try {
            Integer nieInt = Integer.parseInt(nie.trim());
            Estudiante estudiante = repo.findByNie(nieInt).orElse(null);
            if (estudiante != null) {
                resultado.put("encontrado", true);
                resultado.put("nombreCompleto", estudiante.getNombreCompleto());
                resultado.put("seccion", estudiante.getSeccion());
                resultado.put("correo", estudiante.getCorreo());
            } else {
                resultado.put("encontrado", false);
            }
        } catch (NumberFormatException e) {
            resultado.put("encontrado", false);
        }
        return resultado;
    }

    // =========================
    // BUSCAR GENERAL POR NIE O NOMBRE (JSON) - para el buscador de Pagos
    // =========================
    @GetMapping("/buscar-general")
    @ResponseBody
    public Map<String, Object> buscarGeneral(@RequestParam("q") String q) {
        Map<String, Object> resultado = new HashMap<>();
        String texto = q.trim();

        if (texto.matches("\\d+")) {

            Integer nieInt = Integer.parseInt(texto);
            Estudiante est = repo.findByNie(nieInt).orElse(null);

            if (est != null) {
                resultado.put("tipo", "unico");
                resultado.put("nie", est.getNie());
                resultado.put("nombreCompleto", est.getNombreCompleto());
                resultado.put("seccion", est.getSeccion());
                resultado.put("correo", est.getCorreo());
            } else {
                resultado.put("tipo", "vacio");
            }

        } else {

            String textoBusqueda = texto.toLowerCase();

            List<Estudiante> lista = repo.findAll().stream()
                    .filter(e ->
                            (e.getNombres() != null && e.getNombres().toLowerCase().contains(textoBusqueda))
                            || (e.getApellidos() != null && e.getApellidos().toLowerCase().contains(textoBusqueda))
                            || (e.getNombreCompleto() != null && e.getNombreCompleto().toLowerCase().contains(textoBusqueda))
                    )
                    .toList();

            if (lista.isEmpty()) {
                resultado.put("tipo", "vacio");
            } else if (lista.size() == 1) {
                Estudiante est = lista.get(0);
                resultado.put("tipo", "unico");
                resultado.put("nie", est.getNie());
                resultado.put("nombreCompleto", est.getNombreCompleto());
                resultado.put("seccion", est.getSeccion());
                resultado.put("correo", est.getCorreo());
            } else {
                resultado.put("tipo", "lista");
                List<Map<String, Object>> resultados = new ArrayList<>();
                for (Estudiante est : lista) {
                    Map<String, Object> item = new HashMap<>();
                    item.put("nie", est.getNie());
                    item.put("nombreCompleto", est.getNombreCompleto());
                    item.put("seccion", est.getSeccion());
                    resultados.add(item);
                }
                resultado.put("resultados", resultados);
            }
        }

        return resultado;
    }
}