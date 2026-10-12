package com.example.proyecto_cobros.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.proyecto_cobros.entity.Usuario;
import com.example.proyecto_cobros.entity.Estudiante;
import com.example.proyecto_cobros.repository.UsuarioRepository;
import com.example.proyecto_cobros.repository.PagoRepository;
import com.example.proyecto_cobros.repository.EstudianteRepository;
import com.example.proyecto_cobros.security.HashUtil;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/usuarios")
public class UsuarioController {

    @Autowired
    private UsuarioRepository repo;

    @Autowired
    private PagoRepository pagoRepo;

    @Autowired
    private EstudianteRepository estudianteRepo;

    @GetMapping
    public String mostrarLogin(HttpSession session) {

        if (session.getAttribute("usuario") != null) {
            return "redirect:/usuarios/dashboard";
        }

        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {

        if (username == null
                || password == null
                || username.trim().isEmpty()
                || password.trim().isEmpty()) {

            model.addAttribute("error", "Debe ingresar usuario y contraseña");
            return "login";
        }

        String passwordHash = HashUtil.sha256(password);

        Optional<Usuario> usuarioOptional =
                repo.findByUsernameAndPasswordHash(username.trim(), passwordHash);

        if (usuarioOptional.isEmpty()) {
            model.addAttribute("error", "Usuario o contraseña incorrectos");
            return "login";
        }

        Usuario usuario = usuarioOptional.get();

        System.out.println("ROL LOGIN: " + usuario.getRol());

        if (usuario.getActivo() != null && !usuario.getActivo()) {
            model.addAttribute("error", "Usuario desactivado");
            return "login";
        }

        session.setAttribute("usuario", usuario);
        session.setAttribute("rol", usuario.getRol());
        session.setAttribute("id_usuario", usuario.getId());

        return "redirect:/usuarios/dashboard";
    }

    @GetMapping("/dashboard")
    public String dashboard(HttpSession session, Model model) {

        Usuario usuario = (Usuario) session.getAttribute("usuario");

        if (usuario == null) {
            return "redirect:/usuarios";
        }

        model.addAttribute("usuario", usuario);

        long totalPagos = pagoRepo.count();
        long pagosPendientes = pagoRepo.countByEstado("Pendiente");
        BigDecimal totalRecaudado = pagoRepo.sumMontoByEstado("Pagado");

        model.addAttribute("totalPagos", totalPagos);
        model.addAttribute("pagosPendientes", pagosPendientes);
        model.addAttribute("totalRecaudado", totalRecaudado != null ? totalRecaudado : BigDecimal.ZERO);

        List<Estudiante> todosLosEstudiantes = estudianteRepo.findAll();
        long totalEstudiantes = todosLosEstudiantes.size();

        long estudiantesActivos = todosLosEstudiantes.stream()
                .filter(e -> Boolean.TRUE.equals(e.getActivo()))
                .count();

        model.addAttribute("totalEstudiantes", totalEstudiantes);
        model.addAttribute("estudiantesActivos", estudiantesActivos);

        return "dashboard";
    }

    @GetMapping("/panel-admin")
    public String panelAdmin(HttpSession session, Model model) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().trim().toLowerCase().equals("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        List<Usuario> usuarios = repo.findAllByOrderByUsernameAsc();

        model.addAttribute("usuarios", usuarios);
        model.addAttribute("usuarioObj", new Usuario());

        return "admin_usuarios";
    }

    @PostMapping("/crear")
    public String crearUsuario(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String confirmarPassword,
            @RequestParam String rol,
            HttpSession session,
            Model model
    ) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().trim().toLowerCase().equals("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        if (username == null
                || password == null
                || confirmarPassword == null
                || rol == null
                || username.trim().isEmpty()
                || password.trim().isEmpty()
                || confirmarPassword.trim().isEmpty()
                || rol.trim().isEmpty()) {

            model.addAttribute("error", "Todos los campos son obligatorios");
            model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        if (password.trim().length() < 8) {
            model.addAttribute("error", "La contraseña debe tener mínimo 8 caracteres");
            model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        if (!password.equals(confirmarPassword)) {
            model.addAttribute("error", "Las contraseñas no coinciden");
            model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        Optional<Usuario> existe = repo.findByUsername(username.trim());

        if (existe.isPresent()) {
            model.addAttribute("error", "El usuario ya existe");
            model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
            model.addAttribute("usuarioObj", new Usuario());
            return "admin_usuarios";
        }

        Usuario usuario = new Usuario();
        usuario.setUsername(username.trim());
        usuario.setPasswordHash(HashUtil.sha256(password));
        usuario.setRol(rol.trim().toLowerCase());
        usuario.setActivo(true);
        usuario.setFechaCreacion(LocalDateTime.now());

        repo.save(usuario);

        return "redirect:/usuarios/panel-admin?success=created";
    }

    @GetMapping("/editar/{id}")
    public String editarUsuario(@PathVariable Integer id, HttpSession session, Model model) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().trim().toLowerCase().equals("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        Optional<Usuario> usuarioOptional = repo.findById(id);

        if (usuarioOptional.isEmpty()) {
            return "redirect:/usuarios/panel-admin";
        }

        model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
        model.addAttribute("usuarioObj", usuarioOptional.get());

        return "admin_usuarios";
    }

    @PostMapping("/actualizar")
    public String actualizarUsuario(
            @RequestParam Integer id,
            @RequestParam String username,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String confirmarPassword,
            @RequestParam String rol,
            @RequestParam Boolean activo,
            HttpSession session,
            Model model
    ) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().trim().toLowerCase().equals("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        Optional<Usuario> usuarioOptional = repo.findById(id);

        if (usuarioOptional.isEmpty()) {
            return "redirect:/usuarios/panel-admin";
        }

        Usuario usuario = usuarioOptional.get();

        usuario.setUsername(username.trim());
        usuario.setRol(rol.trim().toLowerCase());
        usuario.setActivo(activo);

        if (password != null && !password.trim().isEmpty()) {

            if (password.trim().length() < 8) {
                model.addAttribute("error", "La contraseña debe tener mínimo 8 caracteres");
                model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
                model.addAttribute("usuarioObj", usuario);
                return "admin_usuarios";
            }

            if (!password.equals(confirmarPassword)) {
                model.addAttribute("error", "Las contraseñas no coinciden");
                model.addAttribute("usuarios", repo.findAllByOrderByUsernameAsc());
                model.addAttribute("usuarioObj", usuario);
                return "admin_usuarios";
            }

            usuario.setPasswordHash(HashUtil.sha256(password));
        }

        repo.save(usuario);

        return "redirect:/usuarios/panel-admin?success=updated";
    }

    @GetMapping("/eliminar/{id}")
    public String eliminarUsuario(@PathVariable Integer id, HttpSession session) {

        Usuario usuarioSesion = (Usuario) session.getAttribute("usuario");

        if (usuarioSesion == null) {
            return "redirect:/usuarios";
        }

        if (usuarioSesion.getRol() == null
                || !usuarioSesion.getRol().trim().toLowerCase().equals("admin")) {
            return "redirect:/usuarios/dashboard";
        }

        Optional<Usuario> usuarioOptional = repo.findById(id);

        if (usuarioOptional.isPresent()) {
            repo.deleteById(id);
        }

        return "redirect:/usuarios/panel-admin?success=deleted";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/usuarios";
    }
}