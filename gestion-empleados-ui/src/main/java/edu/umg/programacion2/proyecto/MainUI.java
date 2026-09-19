package edu.umg.programacion2.proyecto;

import edu.umg.programacion2.proyecto.ui.VentanaPrincipal;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;


 // Lanza la ventana principal.
public class MainUI {

    private static final Logger LOG = Logger.getLogger(MainUI.class.getName());

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ex) {
            LOG.log(Level.WARNING, "No se pudo aplicar el estilo del sistema.", ex);
        }

        SwingUtilities.invokeLater(() -> new VentanaPrincipal().mostrar());
    }
}