package com.example.proyecto_cobros.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "estudiantes")
public class Estudiante {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estudiante")
    private Integer id_estudiante;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(length = 50)
    private String grado;

    @Column(length = 20)
    private String seccion;

    @Column(name = "id_especialidad")
    private Integer id_especialidad;

    @Column(nullable = false)
    private Boolean activo = true;

    @Column(nullable = false)
    private Integer nie;

    @Column(length = 150)
    private String correo;

    public Integer getId_estudiante() { return id_estudiante; }
    public void setId_estudiante(Integer id_estudiante) { this.id_estudiante = id_estudiante; }

    public String getNombres() { return nombres; }
    public void setNombres(String nombres) { this.nombres = nombres; }

    public String getApellidos() { return apellidos; }
    public void setApellidos(String apellidos) { this.apellidos = apellidos; }

    public String getGrado() { return grado; }
    public void setGrado(String grado) { this.grado = grado; }

    public String getSeccion() { return seccion; }
    public void setSeccion(String seccion) { this.seccion = seccion; }

    public Integer getId_especialidad() { return id_especialidad; }
    public void setId_especialidad(Integer id_especialidad) { this.id_especialidad = id_especialidad; }

    public Boolean getActivo() { return activo; }
    public void setActivo(Boolean activo) { this.activo = activo; }

    public Integer getNie() { return nie; }
    public void setNie(Integer nie) { this.nie = nie; }

    public String getCorreo() { return correo; }
    public void setCorreo(String correo) { this.correo = correo; }

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }
}