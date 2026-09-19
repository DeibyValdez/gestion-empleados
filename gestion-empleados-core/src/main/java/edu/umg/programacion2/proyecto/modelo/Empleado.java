package edu.umg.programacion2.proyecto.modelo;

import java.math.BigDecimal;
import java.time.LocalDate;

public class Empleado {

    private int id;
    private String nombre;
    private String departamento;
    private BigDecimal salario;
    private LocalDate fechaContratacion;
    private int aniosExperiencia;
    private boolean activo;

    public Empleado() {
    }

    public Empleado(String nombre, String departamento, BigDecimal salario,
                    LocalDate fechaContratacion, int aniosExperiencia, boolean activo) {
        this.nombre = nombre;
        this.departamento = departamento;
        this.salario = salario;
        this.fechaContratacion = fechaContratacion;
        this.aniosExperiencia = aniosExperiencia;
        this.activo = activo;
    }

    public Empleado(int id, String nombre, String departamento, BigDecimal salario,
                    LocalDate fechaContratacion, int aniosExperiencia, boolean activo) {
        this(nombre, departamento, salario, fechaContratacion, aniosExperiencia, activo);
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getDepartamento() {
        return departamento;
    }

    public void setDepartamento(String departamento) {
        this.departamento = departamento;
    }

    public BigDecimal getSalario() {
        return salario;
    }

    public void setSalario(BigDecimal salario) {
        this.salario = salario;
    }

    public LocalDate getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDate fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public int getAniosExperiencia() {
        return aniosExperiencia;
    }

    public void setAniosExperiencia(int aniosExperiencia) {
        this.aniosExperiencia = aniosExperiencia;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return "Empleado{id=" + id + ", nombre='" + nombre + "', departamento='" + departamento
                + "', salario=" + salario + ", fechaContratacion=" + fechaContratacion
                + ", aniosExperiencia=" + aniosExperiencia
                + ", activo=" + activo + "}";
    }
}