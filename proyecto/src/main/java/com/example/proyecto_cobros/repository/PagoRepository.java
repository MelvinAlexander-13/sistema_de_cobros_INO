package com.example.proyecto_cobros.repository;

import com.example.proyecto_cobros.entity.Pago;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface PagoRepository extends JpaRepository<Pago, Integer> {

    @Query("SELECT p FROM Pago p WHERE CAST(p.nie AS string) LIKE CONCAT('%', :nie, '%')")
    List<Pago> findByNieContaining(@Param("nie") String nie);

    Optional<Pago> findByNieAndMes(Integer nie, String mes);

    long countByEstado(String estado);

    @Query("SELECT COALESCE(SUM(p.monto), 0) FROM Pago p WHERE p.estado = :estado")
    BigDecimal sumMontoByEstado(@Param("estado") String estado);

}