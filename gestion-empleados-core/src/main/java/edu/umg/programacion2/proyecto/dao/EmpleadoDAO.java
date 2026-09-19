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

/**
 * Aquí en DAO de empleados vive todo el acceso a la base de datos.
 * Todo el SQL usa PreparedStatement y los recursos se cierran con
 * try-with-resources. Las SQLException no se atrapan aquí: llegan a la UI,
 * que decide cómo avisar al usuario.
 */
public class EmpleadoDAO {

    private static final String COLUMNAS =
            "id, nombre, departamento, salario, fecha_contratacion, activo";

    private static final String SQL_INSERTAR =
            "INSERT INTO empleados (nombre, departamento, salario, fecha_contratacion, activo) "
            + "VALUES (?, ?, ?, ?, ?)";
    private static final String SQL_LISTAR =
            "SELECT " + COLUMNAS + " FROM empleados ORDER BY id";
    private static final String SQL_BUSCAR =
            "SELECT " + COLUMNAS + " FROM empleados WHERE id = ?";
    private static final String SQL_ACTUALIZAR =
            "UPDATE empleados SET nombre = ?, departamento = ?, salario = ?, "
            + "fecha_contratacion = ?, activo = ? WHERE id = ?";
    private static final String SQL_ELIMINAR =
            "DELETE FROM empleados WHERE id = ?";

    // Inserta el empleado y devuelve el mismo objeto con el id generado.
    public Empleado crear(Empleado empleado) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_INSERTAR, Statement.RETURN_GENERATED_KEYS)) {

            asignarDatos(ps, empleado);
            ps.executeUpdate();

            // La base de datos genera el id (AUTO_INCREMENT); lo leemos de vuelta
            try (ResultSet claves = ps.getGeneratedKeys()) {
                if (claves.next()) {
                    empleado.setId(claves.getInt(1));
                }
            }
        }
        return empleado;
    }

    // Devuelve todos los empleados (activos e inactivos).
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

    // Busca un empleado por id; Optional vacío si no existe.
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

    // Actualiza todos los campos. Devuelve true si se modificó una fila.
    public boolean actualizar(Empleado empleado) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ACTUALIZAR)) {

            asignarDatos(ps, empleado);
            ps.setInt(6, empleado.getId());
            return ps.executeUpdate() > 0;
        }
    }

    // Borrado físico de la fila. Devuelve true si se eliminó una fila.
    public boolean eliminar(int id) throws SQLException {
        try (Connection con = ConexionBD.obtenerConexion();
             PreparedStatement ps = con.prepareStatement(SQL_ELIMINAR)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        }
    }

    // Parámetros 1 a 5, en el mismo orden para INSERT y UPDATE
    private void asignarDatos(PreparedStatement ps, Empleado e) throws SQLException {
        ps.setString(1, e.getNombre());
        ps.setString(2, e.getDepartamento());
        ps.setBigDecimal(3, e.getSalario());
        ps.setDate(4, Date.valueOf(e.getFechaContratacion()));
        ps.setBoolean(5, e.isActivo());
    }

    // Convierte la fila actual del ResultSet en un objeto Empleado
    private Empleado armarEmpleado(ResultSet rs) throws SQLException {
        return new Empleado(
                rs.getInt("id"),
                rs.getString("nombre"),
                rs.getString("departamento"),
                rs.getBigDecimal("salario"),
                rs.getDate("fecha_contratacion").toLocalDate(),
                rs.getBoolean("activo"));
    }
}