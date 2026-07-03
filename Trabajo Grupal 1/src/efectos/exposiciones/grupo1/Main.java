package efectos.exposiciones.grupo1;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.awt.event.*;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main extends JFrame {

    private static final Color C_FONDO = new Color(15, 17, 26);
    private static final Color C_PANEL = new Color(22, 25, 38);
    private static final Color C_CARD = new Color(30, 34, 52);
    private static final Color C_BORDE = new Color(45, 50, 80);
    private static final Color C_ACENTO = new Color(99, 179, 237);
    private static final Color C_ACENTO2 = new Color(154, 117, 234);
    private static final Color C_EXITO = new Color(72, 187, 120);
    private static final Color C_PELIGRO = new Color(229, 62, 62);
    private static final Color C_TEXTO = new Color(226, 232, 240);
    private static final Color C_TEXTO_DIM = new Color(100, 110, 140);
    private static final Color C_SELECCION = new Color(45, 80, 130);

    private final ScenePanel scenePanel = new ScenePanel();
    private final JLabel lblEstado = new JLabel("Listo");
    private final JLabel lblDetalle = new JLabel("Grupo 1: Rasterización, Z-Buffer y Cubos 3D");
    private final JCheckBox chkAnimacion = new JCheckBox("Animación automática", true);
    private final JCheckBox chkZBuffer = new JCheckBox("Z-Buffer virtual", true);
    private final JSlider sliderVelocidad = new JSlider(1, 20, 8);
    private final JButton btnReiniciar = new JButton("Reiniciar escena");
    private final JButton btnPausar = new JButton("Pausar");
    private final Timer timer;

    private Point puntoArrastre;
    private boolean animando = true;

    public Main() {
        super("Grupo 1 — Rasterización, Z-Buffer y Cubos 3D");
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(1280, 780);
        setMinimumSize(new Dimension(1000, 640));
        setLocationRelativeTo(null);
        setUndecorated(true);
        getContentPane().setBackground(C_FONDO);
        construirUI();

        timer = new Timer(16, e -> actualizarAnimacion());
        timer.start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().setVisible(true));
    }

    private void construirUI() {
        setLayout(new BorderLayout(0, 0));
        add(crearBarraSuperior(), BorderLayout.NORTH);
        add(crearCuerpo(), BorderLayout.CENTER);
        add(crearBarraEstado(), BorderLayout.SOUTH);
        actualizarTextoEstado("Listo para presentar la escena.");
    }

    private JPanel crearBarraSuperior() {
        JPanel barra = new JPanel(new BorderLayout(0, 0));
        barra.setBackground(C_PANEL);
        barra.add(crearBarraTitulo(), BorderLayout.NORTH);
        barra.add(crearHeader(), BorderLayout.CENTER);
        return barra;
    }

    private JPanel crearBarraTitulo() {
        JPanel filaTitulo = new JPanel(new BorderLayout());
        filaTitulo.setBackground(C_ACENTO.darker().darker());
        filaTitulo.setBorder(new EmptyBorder(6, 12, 6, 8));

        JLabel lblTitulo = new JLabel("Grupo 1 — Exposición Swing");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(Color.WHITE);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
        controles.setBackground(C_ACENTO.darker().darker());

        JButton btnMin = crearBotonVentana("min");
        JButton btnClose = crearBotonVentana("close");
        btnMin.addActionListener(e -> setState(ICONIFIED));
        btnClose.addActionListener(e -> dispose());
        controles.add(btnMin);
        controles.add(btnClose);

        filaTitulo.add(lblTitulo, BorderLayout.WEST);
        filaTitulo.add(controles, BorderLayout.EAST);

        filaTitulo.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                puntoArrastre = e.getPoint();
            }
        });
        filaTitulo.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
                Point loc = getLocation();
                setLocation(loc.x + e.getX() - puntoArrastre.x,
                        loc.y + e.getY() - puntoArrastre.y);
            }
        });

        return filaTitulo;
    }

    private JPanel crearHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(C_PANEL);
        header.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 1, 0, C_BORDE),
                new EmptyBorder(10, 18, 10, 18)));

        JLabel lblTitulo = new JLabel("● Grupo 1");
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTitulo.setForeground(C_ACENTO);

        JLabel lblSub = new JLabel("  Rasterización, Z-Buffer y Cubos 3D en Swing");
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblSub.setForeground(C_TEXTO_DIM);

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izq.setOpaque(false);
        izq.add(lblTitulo);
        izq.add(lblSub);

        JPanel derecha = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        derecha.setOpaque(false);

        JButton btnReiniciarHeader = crearBoton("Reiniciar", C_ACENTO);
        JButton btnCerrar = crearBoton("Cerrar", C_PELIGRO);
        btnReiniciarHeader.addActionListener(e -> reiniciarEscena());
        btnCerrar.addActionListener(e -> dispose());
        derecha.add(btnReiniciarHeader);
        derecha.add(btnCerrar);

        header.add(izq, BorderLayout.WEST);
        header.add(derecha, BorderLayout.EAST);
        return header;
    }

    private JPanel crearCuerpo() {
        JPanel cuerpo = new JPanel(new BorderLayout(0, 0));
        cuerpo.setBackground(C_FONDO);
        cuerpo.add(crearPanelIzquierdo(), BorderLayout.WEST);
        cuerpo.add(crearPanelEscena(), BorderLayout.CENTER);
        return cuerpo;
    }

    private JPanel crearPanelIzquierdo() {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setPreferredSize(new Dimension(280, 0));
        panel.setBackground(C_PANEL);
        panel.setBorder(BorderFactory.createCompoundBorder(
                new MatteBorder(0, 0, 0, 1, C_BORDE),
                new EmptyBorder(10, 10, 10, 10)));

        panel.add(crearTarjetaDescripcion(), BorderLayout.NORTH);
        panel.add(crearTarjetaControles(), BorderLayout.CENTER);
        panel.add(crearTarjetaLeyenda(), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearTarjetaDescripcion() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titulo = new JLabel("Exposición Grupo 1");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(C_ACENTO2);

        JLabel texto = new JLabel(
                "La escena reproduce los dos cubos originales, ahora dibujados con Java2D y el mismo encuadre visual.");
        texto.setAlignmentX(Component.LEFT_ALIGNMENT);
        texto.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        texto.setForeground(C_TEXTO_DIM);
        texto.setBorder(new EmptyBorder(6, 0, 0, 0));

        card.add(titulo);
        card.add(texto);
        return card;
    }

    private JPanel crearTarjetaControles() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titulo = new JLabel("Controles");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(C_ACENTO);

        chkAnimacion.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkAnimacion.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkAnimacion.setForeground(C_TEXTO);
        chkAnimacion.setBackground(C_CARD);
        chkAnimacion.setBorder(new EmptyBorder(6, 0, 0, 0));
        chkAnimacion.addActionListener(e -> {
            animando = chkAnimacion.isSelected();
            btnPausar.setText(animando ? "Pausar" : "Reanudar");
            actualizarTextoEstado(animando ? "Animación activada." : "Animación pausada.");
        });

        chkZBuffer.setAlignmentX(Component.LEFT_ALIGNMENT);
        chkZBuffer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkZBuffer.setForeground(C_TEXTO);
        chkZBuffer.setBackground(C_CARD);
        chkZBuffer.setSelected(true);
        chkZBuffer.setBorder(new EmptyBorder(6, 0, 0, 0));
        chkZBuffer.addActionListener(e -> {
            scenePanel.setZBufferEnabled(chkZBuffer.isSelected());
            actualizarTextoEstado(chkZBuffer.isSelected()
                    ? "Z-Buffer virtual activo."
                    : "Z-Buffer virtual desactivado.");
        });

        JLabel lblVelocidad = labelParam("Velocidad de rotación");
        lblVelocidad.setBorder(new EmptyBorder(10, 0, 0, 0));

        sliderVelocidad.setBackground(C_CARD);
        sliderVelocidad.setForeground(C_TEXTO_DIM);
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setPaintLabels(true);
        sliderVelocidad.setMajorTickSpacing(5);
        sliderVelocidad.setMinorTickSpacing(1);
        sliderVelocidad.setFont(new Font("Segoe UI", Font.PLAIN, 10));

        btnPausar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnReiniciar.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPausar.addActionListener(e -> {
            animando = !animando;
            chkAnimacion.setSelected(animando);
            btnPausar.setText(animando ? "Pausar" : "Reanudar");
            actualizarTextoEstado(animando ? "Animación reanudada." : "Animación detenida.");
        });
        btnReiniciar.addActionListener(e -> reiniciarEscena());

        card.add(titulo);
        card.add(chkAnimacion);
        card.add(chkZBuffer);
        card.add(lblVelocidad);
        card.add(sliderVelocidad);
        card.add(Box.createVerticalStrut(12));
        card.add(btnPausar);
        card.add(Box.createVerticalStrut(6));
        card.add(btnReiniciar);
        return card;
    }

    private JPanel crearTarjetaLeyenda() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(C_CARD);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(C_BORDE),
                new EmptyBorder(10, 10, 10, 10)));

        JLabel titulo = new JLabel("Lectura visual");
        titulo.setAlignmentX(Component.LEFT_ALIGNMENT);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(C_ACENTO2);

        JLabel l1 = labelParam("• Cubo rojo: ligeramente más cercano");
        JLabel l2 = labelParam("• Cubo azul: ligeramente más lejano");
        JLabel l3 = labelParam("• La escena conserva la inclinación y el giro del original");
        JLabel l4 = labelParam("• El Z-Buffer se simula con orden por profundidad");

        card.add(titulo);
        card.add(Box.createVerticalStrut(6));
        card.add(l1);
        card.add(l2);
        card.add(l3);
        card.add(l4);
        return card;
    }

    private JPanel crearPanelEscena() {
        JPanel contenedor = new JPanel(new BorderLayout(0, 0));
        contenedor.setBackground(C_FONDO);
        contenedor.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel cabecera = new JPanel(new BorderLayout());
        cabecera.setOpaque(false);
        JLabel titulo = new JLabel("Escena 3D - Rasterización / Z-Buffer");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titulo.setForeground(C_ACENTO);
        JLabel indicador = new JLabel("Rueda/controles: Ajustar la atmósfera visual");
        indicador.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        indicador.setForeground(C_TEXTO_DIM);
        cabecera.add(titulo, BorderLayout.WEST);
        cabecera.add(indicador, BorderLayout.EAST);

        contenedor.add(cabecera, BorderLayout.NORTH);
        contenedor.add(scenePanel, BorderLayout.CENTER);
        return contenedor;
    }

    private JPanel crearBarraEstado() {
        JPanel barra = new JPanel(new BorderLayout());
        barra.setBackground(C_PANEL);
        barra.setBorder(new MatteBorder(1, 0, 0, 0, C_BORDE));

        JPanel izq = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        izq.setOpaque(false);
        JLabel indicador = new JLabel("●");
        indicador.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        indicador.setForeground(C_EXITO);
        lblEstado.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblEstado.setForeground(C_TEXTO);
        izq.add(indicador);
        izq.add(lblEstado);

        JPanel der = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 4));
        der.setOpaque(false);
        lblDetalle.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        lblDetalle.setForeground(C_TEXTO_DIM);
        der.add(lblDetalle);

        barra.add(izq, BorderLayout.WEST);
        barra.add(der, BorderLayout.EAST);
        return barra;
    }

    private void actualizarAnimacion() {
        scenePanel.setZBufferEnabled(chkZBuffer.isSelected());
        if (animando) {
            double step = sliderVelocidad.getValue() / 200.0;
            scenePanel.advance(step);
            lblDetalle.setText(String.format("Angulo: %.2f rad | Z-Buffer: %s",
                    scenePanel.getAngulo(),
                    scenePanel.isZBufferEnabled() ? "ON" : "OFF"));
        }
    }

    private void reiniciarEscena() {
        scenePanel.reset();
        sliderVelocidad.setValue(8);
        chkAnimacion.setSelected(true);
        chkZBuffer.setSelected(true);
        animando = true;
        btnPausar.setText("Pausar");
        scenePanel.setZBufferEnabled(true);
        actualizarTextoEstado("Escena reiniciada.");
    }

    private void actualizarTextoEstado(String texto) {
        lblEstado.setText(texto);
    }

    private JLabel labelParam(String texto) {
        JLabel lbl = new JLabel(texto);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lbl.setForeground(C_TEXTO_DIM);
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        return lbl;
    }

    private JButton crearBoton(String texto, Color color) {
        JButton btn = new JButton(texto) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setForeground(Color.WHITE);
        btn.setBackground(color.darker());
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(color);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(color.darker());
                btn.repaint();
            }
        });
        return btn;
    }

    private JButton crearBotonVentana(String tipo) {
        Color base = C_ACENTO.darker().darker();
        Color hover = tipo.equals("close") ? C_PELIGRO : new Color(80, 130, 180);
        JButton btn = new JButton() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setStroke(new BasicStroke(1.8f));
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 6, 6);
                g2.setColor(Color.WHITE);
                int cx = getWidth() / 2;
                int cy = getHeight() / 2;
                switch (tipo) {
                    case "min" -> g2.drawLine(cx - 5, cy + 2, cx + 5, cy + 2);
                    case "close" -> {
                        g2.drawLine(cx - 5, cy - 4, cx + 5, cy + 4);
                        g2.drawLine(cx + 5, cy - 4, cx - 5, cy + 4);
                    }
                }
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        };
        btn.setBackground(base);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setPreferredSize(new Dimension(36, 26));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(hover);
                btn.repaint();
            }

            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(base);
                btn.repaint();
            }
        });
        return btn;
    }

    private static final class ScenePanel extends JPanel {

        private static final int[][] FACES = {
                { 0, 1, 2, 3 },
                { 4, 5, 6, 7 },
                { 0, 1, 5, 4 },
                { 2, 3, 7, 6 },
                { 1, 2, 6, 5 },
                { 0, 3, 7, 4 }
        };

        private static final double[] SHADE = { 1.0, 0.6, 0.4, 0.9, 0.75, 0.55 };

        private final BufferedImage backdrop = crearFondo(1600, 1000);
        private double angulo;
        private boolean zBufferEnabled = true;

        ScenePanel() {
            setBackground(C_FONDO);
            setBorder(BorderFactory.createLineBorder(C_BORDE));
        }

        double getAngulo() {
            return angulo;
        }

        boolean isZBufferEnabled() {
            return zBufferEnabled;
        }

        void setZBufferEnabled(boolean enabled) {
            this.zBufferEnabled = enabled;
        }

        void advance(double step) {
            angulo += step;
            repaint();
        }

        void reset() {
            angulo = 0.0;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            g2.drawImage(backdrop, 0, 0, getWidth(), getHeight(), null);

            List<FaceRender> caras = new ArrayList<>();
            caras.addAll(construirCubo(new ImageProcessor.Point3D(-0.8, 0.0, 0.3), 2.0,
                    new Color(255, 0, 0), angulo, angulo));
            caras.addAll(construirCubo(new ImageProcessor.Point3D(0.5, 0.0, -0.3), 2.0,
                    new Color(0, 0, 255), angulo, angulo));

            if (zBufferEnabled) {
                caras.sort(Comparator.comparingDouble(FaceRender::depth));
            }

            for (FaceRender cara : caras) {
                g2.setColor(cara.fill());
                g2.fillPolygon(cara.polygon());
                g2.setColor(cara.border());
                g2.drawPolygon(cara.polygon());
            }

            g2.dispose();
        }

        private List<FaceRender> construirCubo(ImageProcessor.Point3D centro, double tamanio, Color base,
                double rotacionX, double rotacionY) {
            ImageProcessor.Point3D[] vertices = crearVertices(tamanio);
            ImageProcessor.Point3D[] transformados = new ImageProcessor.Point3D[vertices.length];
            Point2D.Double[] proyectados = new Point2D.Double[vertices.length];

            for (int i = 0; i < vertices.length; i++) {
                ImageProcessor.Point3D mundo = ImageProcessor.translate(vertices[i], centro.x(), centro.y(),
                        centro.z());
                mundo = ImageProcessor.rotateY(mundo, Math.toRadians(35) + rotacionY);
                mundo = ImageProcessor.rotateX(mundo, Math.toRadians(30) + rotacionX);
                transformados[i] = mundo;
                proyectados[i] = ImageProcessor.project(mundo, getWidth(), getHeight(), 360, 5.0);
            }

            List<FaceRender> caras = new ArrayList<>();
            for (int i = 0; i < FACES.length; i++) {
                int[] face = FACES[i];
                Polygon poly = new Polygon();
                double depth = 0.0;
                for (int idx : face) {
                    Point2D.Double p = proyectados[idx];
                    poly.addPoint((int) Math.round(p.x), (int) Math.round(p.y));
                    depth += transformados[idx].z();
                }
                depth /= face.length;
                Color fill = ImageProcessor.shade(base, SHADE[i]);
                Color border = ImageProcessor.shade(base.darker(), 0.65);
                caras.add(new FaceRender(poly, fill, border, depth));
            }
            return caras;
        }

        private ImageProcessor.Point3D[] crearVertices(double tamanio) {
            double s = tamanio / 2.0;
            return new ImageProcessor.Point3D[] {
                    new ImageProcessor.Point3D(-s, -s, s),
                    new ImageProcessor.Point3D(s, -s, s),
                    new ImageProcessor.Point3D(s, s, s),
                    new ImageProcessor.Point3D(-s, s, s),
                    new ImageProcessor.Point3D(-s, -s, -s),
                    new ImageProcessor.Point3D(s, -s, -s),
                    new ImageProcessor.Point3D(s, s, -s),
                    new ImageProcessor.Point3D(-s, s, -s)
            };
        }

        private BufferedImage crearFondo(int width, int height) {
            BufferedImage background = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
            for (int y = 0; y < height; y++) {
                double t = (double) y / Math.max(1, height - 1);
                int red = clamp((int) (18 + 18 * t));
                int green = clamp((int) (20 + 22 * t));
                int blue = clamp((int) (30 + 30 * t));
                int rowColor = (255 << 24) | (red << 16) | (green << 8) | blue;
                for (int x = 0; x < width; x++) {
                    background.setRGB(x, y, rowColor);
                }
            }
            return background;
        }

        private int clamp(int value) {
            return Math.max(0, Math.min(255, value));
        }
    }

    private record FaceRender(Polygon polygon, Color fill, Color border, double depth) {
    }
}
