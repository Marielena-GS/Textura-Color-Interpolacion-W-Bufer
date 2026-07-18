package efectos;

import ui.VentanaPrincipal;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

/**
 * Punto de entrada de ImageGen Studio.
 *
 * Lanza la interfaz en el hilo de eventos de Swing (EDT)
 * siguiendo las buenas practicas de Swing.
 */

public class Main {
    public static void main(String[] args) {
        // Workaround para fallos intermitentes del JFileChooser en Windows.
        System.setProperty("sun.awt.shell.disableFileChooserSpeedFix", "true");
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {

        }

        SwingUtilities.invokeLater(() -> {
            VentanaPrincipal ventana = new VentanaPrincipal();
            ventana.setVisible(true);
        });
    }
}
