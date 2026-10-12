package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository
        extends JpaRepository<Usuario, Integer> {

    List<Usuario> findAllByOrderByUsernameAsc();

    Optional<Usuario> findByUsername(String username);

    Optional<Usuario> findByUsernameAndPasswordHash(
            String username,
            String passwordHash
    );
}