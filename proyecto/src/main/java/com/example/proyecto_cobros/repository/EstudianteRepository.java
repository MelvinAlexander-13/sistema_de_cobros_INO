package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Estudiante;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EstudianteRepository extends JpaRepository<Estudiante, Integer> {

    List<Estudiante> findByNombresContainingIgnoreCaseOrApellidosContainingIgnoreCase(
            String nombres,
            String apellidos
    );

    List<Estudiante> findByActivo(Boolean activo);

    Optional<Estudiante> findByNie(Integer nie);
}