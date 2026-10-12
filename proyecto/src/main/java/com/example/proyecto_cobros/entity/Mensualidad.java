package com.example.proyecto_cobros.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "mensualidades")
public class Mensualidad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_mensualidad")
    private Integer id_mensualidad;

    @Column(name = "id_estudiante")
    private Integer id_estudiante;

    @Column(name = "anio")
    private Integer anio;

    @Column(name = "mes")
    private Integer mes;

    @Column(name = "monto", precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(name = "pagado")
    private Boolean pagado;

    public Integer getId_mensualidad() { return id_mensualidad; }
    public void setId_mensualidad(Integer id_mensualidad) { this.id_mensualidad = id_mensualidad; }

    public Integer getId_estudiante() { return id_estudiante; }
    public void setId_estudiante(Integer id_estudiante) { this.id_estudiante = id_estudiante; }

    public Integer getAnio() { return anio; }
    public void setAnio(Integer anio) { this.anio = anio; }

    public Integer getMes() { return mes; }
    public void setMes(Integer mes) { this.mes = mes; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public Boolean getPagado() { return pagado; }
    public void setPagado(Boolean pagado) { this.pagado = pagado; }
}