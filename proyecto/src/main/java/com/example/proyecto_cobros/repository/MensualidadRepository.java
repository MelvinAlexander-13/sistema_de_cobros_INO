package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Mensualidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MensualidadRepository extends JpaRepository<Mensualidad, Integer> {

    @Query("SELECT m FROM Mensualidad m WHERE m.id_estudiante = :idEstudiante AND m.anio = :anio")
    List<Mensualidad> findByIdEstudianteAndAnio(@Param("idEstudiante") Integer idEstudiante,
                                                @Param("anio") Integer anio);
}