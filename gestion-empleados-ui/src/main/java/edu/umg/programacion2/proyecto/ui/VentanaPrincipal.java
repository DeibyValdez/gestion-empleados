package edu.umg.programacion2.proyecto.ui;

import edu.umg.programacion2.proyecto.dao.EmpleadoDAO;
import edu.umg.programacion2.proyecto.modelo.Empleado;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.math.BigDecimal;
// Solo se importa SQLException porque el contrato del DAO la declara.
// La UI no usa Connection, PreparedStatement ni ResultSet.
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Ventana principal: tabla con el listado, formulario y botones del CRUD.
 * Solo llama al DAO del módulo CORE; no contiene SQL.
 */
public class VentanaPrincipal extends JFrame {

    private static final Logger LOG = Logger.getLogger(VentanaPrincipal.class.getName());
    private static final String[] COLUMNAS =
            {"ID", "Nombre", "Departamento", "Salario", "Contratación", "Activo"};

    // Longitudes máximas: deben coincidir con las columnas de schema.sql
    private static final int MAX_NOMBRE = 100;
    private static final int MAX_DEPARTAMENTO = 50;

    private final EmpleadoDAO dao = new EmpleadoDAO();

    // Empleados mostrados en la tabla (misma posición que las filas)
    private List<Empleado> empleados = new ArrayList<>();
    // Id del empleado seleccionado; null cuando se está registrando uno nuevo
    private Integer idSeleccionado = null;

    private final DefaultTableModel modeloTabla = new DefaultTableModel(COLUMNAS, 0) {
        @Override
        public boolean isCellEditable(int fila, int columna) {
            return false;
        }
    };
    private final JTable tabla = new JTable(modeloTabla);

    private final JLabel lblId = new JLabel("(nuevo)");
    private final JTextField txtNombre = new JTextField(25);
    private final JTextField txtDepartamento = new JTextField(25);
    private final JTextField txtSalario = new JTextField(10);
    private final JTextField txtFecha = new JTextField(10);
    private final JCheckBox chkActivo = new JCheckBox("Activo", true);

    public VentanaPrincipal() {
        super("Gestión de empleados");
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel raiz = new JPanel(new BorderLayout(10, 10));
        raiz.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        raiz.add(crearPanelTabla(), BorderLayout.CENTER);
        raiz.add(crearPanelInferior(), BorderLayout.SOUTH);
        setContentPane(raiz);

        limpiarFormulario();
        pack();
        setLocationRelativeTo(null);
    }

    /** Muestra la ventana y carga el listado inicial. */
    public void mostrar() {
        setVisible(true);
        refrescarTabla();
    }

    // ---------------------------------------------------------------
    // Armado de la pantalla
    // ---------------------------------------------------------------

    private JScrollPane crearPanelTabla() {
        tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabla.getSelectionModel().addListSelectionListener(ev -> {
            if (!ev.getValueIsAdjusting()) {
                cargarSeleccionEnFormulario();
            }
        });

        // Anchos de columna (ID angosto, Nombre más ancho)
        int[] anchos = {50, 230, 150, 90, 100, 70};
        for (int i = 0; i < anchos.length; i++) {
            tabla.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        }

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setPreferredSize(new Dimension(760, 260));
        return scroll;
    }

    private JPanel crearPanelInferior() {
        JPanel formulario = new JPanel(new GridBagLayout());
        formulario.setBorder(BorderFactory.createTitledBorder("Datos del empleado"));
        agregarFila(formulario, 0, "ID:", lblId);
        agregarFila(formulario, 1, "Nombre completo:", txtNombre);
        agregarFila(formulario, 2, "Departamento:", txtDepartamento);
        agregarFila(formulario, 3, "Salario mensual (Q):", txtSalario);
        agregarFila(formulario, 4, "Fecha de contratación (AAAA-MM-DD):", txtFecha);
        agregarFila(formulario, 5, "", chkActivo);

        JButton btnRegistrar = new JButton("Registrar nuevo");
        JButton btnActualizar = new JButton("Actualizar");
        JButton btnEliminar = new JButton("Eliminar");
        JButton btnLimpiar = new JButton("Limpiar");
        btnRegistrar.addActionListener(ev -> registrar());
        btnActualizar.addActionListener(ev -> actualizar());
        btnEliminar.addActionListener(ev -> eliminar());
        btnLimpiar.addActionListener(ev -> limpiarFormulario());

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        botones.add(btnRegistrar);
        botones.add(btnActualizar);
        botones.add(btnEliminar);
        botones.add(btnLimpiar);

        JPanel inferior = new JPanel(new BorderLayout(0, 5));
        inferior.add(formulario, BorderLayout.CENTER);
        inferior.add(botones, BorderLayout.SOUTH);
        return inferior;
    }

    private void agregarFila(JPanel panel, int fila, String etiqueta, Component campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(3, 5, 3, 5);
        c.gridy = fila;

        c.gridx = 0;
        c.anchor = GridBagConstraints.EAST;
        panel.add(new JLabel(etiqueta), c);

        c.gridx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.weightx = 1;
        panel.add(campo, c);
    }

    // ---------------------------------------------------------------
    // Acciones del CRUD
    // ---------------------------------------------------------------

    private void registrar() {
        Empleado nuevo = leerFormulario();
        if (nuevo == null) {
            return; // ya se mostró el aviso de validación
        }
        try {
            dao.crear(nuevo);
            limpiarFormulario();
            refrescarTabla();
            informar("Empleado registrado.");
        } catch (SQLException ex) {
            mostrarError("No se pudo registrar el empleado.", ex);
        }
    }

    private void actualizar() {
        if (idSeleccionado == null) {
            avisar("Selecciona un empleado de la tabla para actualizarlo.");
            return;
        }
        Empleado datos = leerFormulario();
        if (datos == null) {
            return;
        }
        datos.setId(idSeleccionado);
        try {
            if (dao.actualizar(datos)) {
                limpiarFormulario();
                refrescarTabla();
                informar("Empleado actualizado.");
            } else {
                avisar("No se encontró el empleado. Puede que ya haya sido eliminado.");
                refrescarTabla();
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo actualizar el empleado.", ex);
        }
    }

    private void eliminar() {
        int fila = tabla.getSelectedRow();
        if (idSeleccionado == null || fila < 0 || fila >= empleados.size()) {
            avisar("Selecciona un empleado de la tabla para eliminarlo.");
            return;
        }
        int id = idSeleccionado;
        String nombre = empleados.get(fila).getNombre();

        // Confirmación antes del borrado físico
        int opcion = JOptionPane.showConfirmDialog(this,
                "¿Eliminar a " + nombre + "?\nSe borrará el registro y no se puede deshacer.",
                "Confirmar eliminación", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (opcion != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            if (dao.eliminar(id)) {
                limpiarFormulario();
                refrescarTabla();
                informar("Empleado eliminado.");
            } else {
                avisar("No se encontró el empleado. Puede que ya haya sido eliminado.");
                refrescarTabla();
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo eliminar el empleado.", ex);
        }
    }

    private void refrescarTabla() {
        try {
            empleados = dao.listarTodos();
            modeloTabla.setRowCount(0);
            for (Empleado e : empleados) {
                modeloTabla.addRow(new Object[]{
                        e.getId(),
                        e.getNombre(),
                        e.getDepartamento(),
                        String.format(Locale.US, "Q%.2f", e.getSalario()),
                        e.getFechaContratacion(),
                        e.isActivo() ? "Activo" : "Inactivo"
                });
            }
        } catch (SQLException ex) {
            mostrarError("No se pudo cargar el listado de empleados.", ex);
        }
    }

    // ---------------------------------------------------------------
    // Formulario y validación
    // ---------------------------------------------------------------

    /**
     * Valida los campos antes de llamar al DAO.
     * Devuelve el empleado armado, o null si algún dato no es válido.
     */
    private Empleado leerFormulario() {
        String nombre = txtNombre.getText().trim();
        if (nombre.isEmpty()) {
            avisar("El nombre no puede quedar vacío.");
            txtNombre.requestFocus();
            return null;
        }
        if (nombre.length() > MAX_NOMBRE) {
            avisar("El nombre admite como máximo " + MAX_NOMBRE + " caracteres.");
            txtNombre.requestFocus();
            return null;
        }

        String departamento = txtDepartamento.getText().trim();
        if (departamento.isEmpty()) {
            avisar("El departamento no puede quedar vacío.");
            txtDepartamento.requestFocus();
            return null;
        }
        if (departamento.length() > MAX_DEPARTAMENTO) {
            avisar("El departamento admite como máximo " + MAX_DEPARTAMENTO + " caracteres.");
            txtDepartamento.requestFocus();
            return null;
        }

        BigDecimal salario;
        try {
            salario = new BigDecimal(txtSalario.getText().trim());
        } catch (NumberFormatException ex) {
            avisar("El salario debe ser un número, por ejemplo 8500.00");
            txtSalario.requestFocus();
            return null;
        }
        if (salario.signum() <= 0) {
            avisar("El salario debe ser mayor a cero.");
            txtSalario.requestFocus();
            return null;
        }
        if (salario.stripTrailingZeros().scale() > 2) {
            avisar("El salario admite como máximo 2 decimales (centavos).");
            txtSalario.requestFocus();
            return null;
        }

        LocalDate fecha;
        try {
            fecha = LocalDate.parse(txtFecha.getText().trim());
        } catch (DateTimeParseException ex) {
            avisar("La fecha debe tener el formato AAAA-MM-DD, por ejemplo 2024-03-15");
            txtFecha.requestFocus();
            return null;
        }
        if (fecha.isAfter(LocalDate.now())) {
            avisar("La fecha de contratación no puede ser futura.");
            txtFecha.requestFocus();
            return null;
        }

        return new Empleado(nombre, departamento, salario, fecha, chkActivo.isSelected());
    }

    private void cargarSeleccionEnFormulario() {
        int fila = tabla.getSelectedRow();
        if (fila < 0 || fila >= empleados.size()) {
            idSeleccionado = null;
            lblId.setText("(nuevo)");
            return;
        }
        Empleado e = empleados.get(fila);
        idSeleccionado = e.getId();
        lblId.setText(String.valueOf(e.getId()));
        txtNombre.setText(e.getNombre());
        txtDepartamento.setText(e.getDepartamento());
        txtSalario.setText(e.getSalario().toPlainString());
        txtFecha.setText(String.valueOf(e.getFechaContratacion()));
        chkActivo.setSelected(e.isActivo());
    }

    private void limpiarFormulario() {
        tabla.clearSelection();
        idSeleccionado = null;
        lblId.setText("(nuevo)");
        txtNombre.setText("");
        txtDepartamento.setText("");
        txtSalario.setText("");
        txtFecha.setText(LocalDate.now().toString());
        chkActivo.setSelected(true);
    }

    // ---------------------------------------------------------------
    // Mensajes al usuario
    // ---------------------------------------------------------------

    private void avisar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Revisa los datos", JOptionPane.WARNING_MESSAGE);
    }

    private void informar(String mensaje) {
        JOptionPane.showMessageDialog(this, mensaje, "Listo", JOptionPane.INFORMATION_MESSAGE);
    }

    private void mostrarError(String mensaje, SQLException ex) {
        LOG.log(Level.SEVERE, mensaje, ex);
        JOptionPane.showMessageDialog(this,
                mensaje + "\nDetalle: " + ex.getMessage(),
                "Error de base de datos", JOptionPane.ERROR_MESSAGE);
    }
}