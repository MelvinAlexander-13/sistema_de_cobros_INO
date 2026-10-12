package com.example.proyecto_cobros.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "pagos")
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id_pago;

    @Column(nullable = false)
    private Integer nie;

    @Column(nullable = false, length = 100)
    private String estudiante;

    @Column(nullable = false, length = 20)
    private String mes;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal monto;

    @Column(nullable = false)
    private LocalDate fecha_pago;

    @Column(nullable = false, length = 20)
    private String estado;

    @Column(name = "id_usuario")
    private Integer id_usuario;

    @Column(name = "id_mensualidad")
    private Integer id_mensualidad;

    @Column(name = "metodo_pago", length = 25)
    private String metodo_pago;

    @Column(name = "activo")
    private Boolean activo = true;

    @Column(name = "numero_comprobante", length = 30)
    private String numeroComprobante;

    @Column(name = "correo_enviado", nullable = false)
    private Boolean correoEnviado = false;

    public Integer getId_pago() { return id_pago; }
    public void setId_pago(Integer id_pago) { this.id_pago = id_pago; }

    public Integer getNie() { return nie; }
    public void setNie(Integer nie) { this.nie = nie; }

    public String getEstudiante() { return estudiante; }
    public void setEstudiante(String estudiante) { this.estudiante = estudiante; }

    public String getMes() { return mes; }
    public void setMes(String mes) { this.mes = mes; }

    public BigDecimal getMonto() { return monto; }
    public void setMonto(BigDecimal monto) { this.monto = monto; }

    public LocalDate getFecha_pago() { return fecha_pago; }
    public void setFecha_pago(LocalDate fecha_pago) { this.fecha_pago = fecha_pago; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }

    public Integer getId_usuario() { return id_usuario; }
    public void setId_usuario(Integer id_usuario) { this.id_usuario = id_usuario; }

    public Integer getId_mensualidad() { return id_mensualidad; }
    public void setId_mensualidad(Integer id_mensualidad) { this.id_mensualidad = id_mensualidad; }

    public String getMetodo_pago() { return metodo_pago; }
    public void setMetodo_pago(String metodo_pago) { this.metodo_pago = metodo_pago; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public String getNumeroComprobante() { return numeroComprobante; }
    public void setNumeroComprobante(String numeroComprobante) { this.numeroComprobante = numeroComprobante; }

    public Boolean getCorreoEnviado() { return correoEnviado; }
    public void setCorreoEnviado(Boolean correoEnviado) { this.correoEnviado = correoEnviado; }
}