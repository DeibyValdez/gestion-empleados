package edu.umg.programacion2.proyecto.dao;

import edu.umg.programacion2.proyecto.modelo.Empleado;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class EmpleadoDAO {

    private static final String COLUMNAS =
            "id, nombre, departamento, salario, fecha_contratacion, anios_experiencia, activo";

    private static final String SQL_INSERTAR =
            "INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, anios_experiencia, activo) "
            + "VALUES (?, ?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM empleados ORDER BY id";
    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM empleados WHERE id = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE empleados SET nombre = ?, departamento = ?, salario = ?, "
            + "fecha_contratacion = ?, anios_experiencia = ?, activo = ? WHERE id = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM empleados WHERE id = ?";

    public Empleado crear(Empleado empleado) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            asignarDatos(ps, empleado);
            ps.executeUpdate();

            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    empleado.setId(claves.getInt(1));
                }
            }
        }
        return empleado;
    }

    public List<Empleado> listarTodos() throws SQLException {
        List<Empleado> lista = new ArrayList<>();
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_LISTAR);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                lista.add(armarEmpleado(rs));
            }
        }
        return lista;
    }

    public Optional<Empleado> buscarPorId(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_BUSCAR)) {

            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(armarEmpleado(rs));
                }
            }
        }
        return Optional.empty();
    }

    public boolean actualizar(Empleado empleado) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {

            asignarDatos(ps, empleado);
            ps.setInt(7, empleado.getId());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    private void asignarDatos(PreparedStatement ps, Empleado e) throws SQLException {
        ps.setString(1, e.getNombre());
        ps.setString(2, e.getDepartamento());
        ps.setBigDecimal(3, e.getSalario());
        ps.setDate(4, Date.valueOf(e.getFechaContratacion()));
        ps.setInt(5, e.getAniosExperiencia());
        ps.setBoolean(6, e.isActivo());
    }

    private Empleado armarEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("departamento"),
                rs.getBigDecimal("salario"),
                rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getInt("anios_experiencia"),
                rs.getBoolean("activo"));
    }
}