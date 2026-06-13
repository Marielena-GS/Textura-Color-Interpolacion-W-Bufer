package rasterizador;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

	/*
	Práctica: Rasterizador de un cartel 3D con textura, color e interpolacion de profundidad
	El programa debe permitir:

	Mostrar solo la textura aplicada al cartel.
	Mostrar solo el color interpolado entre los vértices.
	Comparar interpolación lineal vs interpolación corregida con 1/W.
	Mostrar dos carteles superpuestos para comprobar el W-Buffering.
	Mostrar la vista completa con textura, color, profundidad y buffer.
	
	Lo que deben cumplir
	Usar coordenadas UV.
	Interpolar colores por vértice.
	Aplicar corrección de perspectiva con 1/W.
	Usar un buffer de profundidad para que el objeto de adelante tape al de atrás.
	Cambiar de modo con las teclas 1, 2, 3, 4 y 5.
	*/

public class EjercicioPropuesto extends JPanel {

    private static final int WIDTH = 800;
    private static final int HEIGHT = 600;

    private static final int MODO_TEXTURA = 1;
    private static final int MODO_COLOR = 2;
    private static final int MODO_PROFUNDIDAD = 3;
    private static final int MODO_WBUFFER = 4;
    private static final int MODO_COMPLETO = 5;

    private int modoActual = MODO_COMPLETO;

    private final BufferedImage canvas;
    private final BufferedImage textura;
    private final float[][] wBuffer;

    static class Vertex {
        float x, y;
        float w;
        float r, g, b;
        float u, v;

        Vertex(float x, float y, float w,
               float r, float g, float b,
               float u, float v) {
            this.x = x;
            this.y = y;
            this.w = w;
            this.r = r;
            this.g = g;
            this.b = b;
            this.u = u;
            this.v = v;
        }
    }

    public EjercicioPropuesto() {
        canvas = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        textura = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        wBuffer = new float[WIDTH][HEIGHT];

        crearTextura();
        instalarControlesTeclado();
        renderizar();
    }

    private void setModo(int modo) {
        modoActual = modo;
        renderizar();
        repaint();
    }

    private void crearTextura() {
        for (int y = 0; y < textura.getHeight(); y++) {
            for (int x = 0; x < textura.getWidth(); x++) {
                boolean cuadro = ((x / 32) + (y / 32)) % 2 == 0;
                textura.setRGB(x, y, cuadro ? Color.WHITE.getRGB() : Color.BLACK.getRGB());
            }
        }
    }

    private void limpiarWBuffer() {
        for (int i = 0; i < WIDTH; i++) {
            Arrays.fill(wBuffer[i], Float.NEGATIVE_INFINITY);
        }
    }

    private float edge(float ax, float ay, float bx, float by, float cx, float cy) {
        return (cx - ax) * (by - ay) - (cy - ay) * (bx - ax);
    }

    private boolean dentroTriangulo(float w0, float w1, float w2, float area) {
        if (area > 0) {
            return w0 >= 0 && w1 >= 0 && w2 >= 0;
        }
        return w0 <= 0 && w1 <= 0 && w2 <= 0;
    }

    private void dibujarTitulo(Graphics2D g, String texto) {
        g.setColor(Color.WHITE);
        g.drawString(texto, 20, 20);
        g.drawString("Teclas: 1 Textura | 2 Color | 3 Profundidad | 4 W-Buffer | 5 Completo", 20, 40);
    }

    private void renderizar() {
        limpiarWBuffer();

        Graphics2D g = canvas.createGraphics();
        g.setColor(Color.DARK_GRAY);
        g.fillRect(0, 0, WIDTH, HEIGHT);

        switch (modoActual) {
            case MODO_TEXTURA -> renderTextura(g);
            case MODO_COLOR -> renderColor(g);
            case MODO_PROFUNDIDAD -> renderProfundidad(g);
            case MODO_WBUFFER -> renderWBuffer(g);
            default -> renderCompleto(g);
        }

        g.dispose();
    }

    private void renderTextura(Graphics2D g) {
        dibujarTitulo(g, "1) TEXTURA");

        // Cartel rectangular = 2 triángulos
        Vertex a = new Vertex(140, 120, 1.0f, 1, 1, 1, 0, 0);
        Vertex b = new Vertex(620, 150, 0.8f, 1, 1, 1, 1, 0);
        Vertex c = new Vertex(600, 470, 0.6f, 1, 1, 1, 1, 1);
        Vertex d = new Vertex(160, 440, 0.9f, 1, 1, 1, 0, 1);

        rasterizarTriangulo(a, b, c, true, false, true, false);
        rasterizarTriangulo(a, c, d, true, false, true, false);
    }

    private void renderColor(Graphics2D g) {
        dibujarTitulo(g, "2) COLOR INTERPOLADO");

        Vertex a = new Vertex(140, 120, 1.0f, 1, 0, 0, 0, 0);
        Vertex b = new Vertex(620, 150, 0.8f, 0, 1, 0, 1, 0);
        Vertex c = new Vertex(600, 470, 0.6f, 0, 0, 1, 1, 1);
        Vertex d = new Vertex(160, 440, 0.9f, 1, 1, 0, 0, 1);

        rasterizarTriangulo(a, b, c, false, true, true, false);
        rasterizarTriangulo(a, c, d, false, true, true, false);
    }

    private void renderProfundidad(Graphics2D g) {
        dibujarTitulo(g, "3) INTERPOLACION EN PROFUNDIDAD");

        g.setColor(Color.WHITE);
        g.drawString("Izquierda: sin correccion | Derecha: con correccion 1/W", 20, 60);

        // Izquierda: sin corrección de perspectiva
        Vertex a1 = new Vertex(70, 120, 1.0f, 1, 1, 1, 0, 0);
        Vertex b1 = new Vertex(320, 150, 0.5f, 1, 1, 1, 1, 0);
        Vertex c1 = new Vertex(300, 460, 0.2f, 1, 1, 1, 1, 1);
        Vertex d1 = new Vertex(80, 430, 0.8f, 1, 1, 1, 0, 1);

        rasterizarTriangulo(a1, b1, c1, true, false, false, false);
        rasterizarTriangulo(a1, c1, d1, true, false, false, false);

        // Derecha: con corrección de perspectiva
        Vertex a2 = new Vertex(430, 120, 1.0f, 1, 1, 1, 0, 0);
        Vertex b2 = new Vertex(720, 150, 0.5f, 1, 1, 1, 1, 0);
        Vertex c2 = new Vertex(700, 460, 0.2f, 1, 1, 1, 1, 1);
        Vertex d2 = new Vertex(450, 430, 0.8f, 1, 1, 1, 0, 1);

        rasterizarTriangulo(a2, b2, c2, true, false, true, false);
        rasterizarTriangulo(a2, c2, d2, true, false, true, false);
    }

    private void renderWBuffer(Graphics2D g) {
        dibujarTitulo(g, "4) W-BUFFERING");

        g.setColor(Color.WHITE);
        g.drawString("Izquierda: sin W-Buffer | Derecha: con W-Buffer", 20, 60);

        // Izquierda: sin buffer
        Vertex a1 = new Vertex(100, 120, 1.0f, 1, 0, 0, 0, 0);
        Vertex b1 = new Vertex(300, 160, 1.0f, 0, 1, 0, 1, 0);
        Vertex c1 = new Vertex(270, 470, 1.0f, 0, 0, 1, 1, 1);
        Vertex d1 = new Vertex(120, 430, 1.0f, 1, 1, 0, 0, 1);

        Vertex a2 = new Vertex(160, 170, 0.3f, 1, 1, 0, 0, 0);
        Vertex b2 = new Vertex(360, 210, 0.3f, 1, 0, 1, 1, 0);
        Vertex c2 = new Vertex(330, 500, 0.3f, 0, 1, 1, 1, 1);
        Vertex d2 = new Vertex(180, 460, 0.3f, 1, 0.5f, 0, 0, 1);

        rasterizarTriangulo(a1, b1, c1, true, true, true, false);
        rasterizarTriangulo(a1, c1, d1, true, true, true, false);
        rasterizarTriangulo(a2, b2, c2, true, true, true, false);
        rasterizarTriangulo(a2, c2, d2, true, true, true, false);

        // Derecha: con W-Buffer
        Vertex a3 = new Vertex(450, 120, 1.0f, 1, 0, 0, 0, 0);
        Vertex b3 = new Vertex(650, 160, 1.0f, 0, 1, 0, 1, 0);
        Vertex c3 = new Vertex(620, 470, 1.0f, 0, 0, 1, 1, 1);
        Vertex d3 = new Vertex(470, 430, 1.0f, 1, 1, 0, 0, 1);

        Vertex a4 = new Vertex(510, 170, 0.3f, 1, 1, 0, 0, 0);
        Vertex b4 = new Vertex(710, 210, 0.3f, 1, 0, 1, 1, 0);
        Vertex c4 = new Vertex(680, 500, 0.3f, 0, 1, 1, 1, 1);
        Vertex d4 = new Vertex(530, 460, 0.3f, 1, 0.5f, 0, 0, 1);

        rasterizarTriangulo(a3, b3, c3, true, true, true, true);
        rasterizarTriangulo(a3, c3, d3, true, true, true, true);
        rasterizarTriangulo(a4, b4, c4, true, true, true, true);
        rasterizarTriangulo(a4, c4, d4, true, true, true, true);
    }

    private void renderCompleto(Graphics2D g) {
        dibujarTitulo(g, "5) COMPLETO");

        Vertex a = new Vertex(140, 120, 1.0f, 1, 0, 0, 0, 0);
        Vertex b = new Vertex(620, 150, 0.8f, 0, 1, 0, 1, 0);
        Vertex c = new Vertex(600, 470, 0.6f, 0, 0, 1, 1, 1);
        Vertex d = new Vertex(160, 440, 0.9f, 1, 1, 0, 0, 1);

        rasterizarTriangulo(a, b, c, true, true, true, true);
        rasterizarTriangulo(a, c, d, true, true, true, true);
    }

    private void rasterizarTriangulo(Vertex v0, Vertex v1, Vertex v2,
                                     boolean usarTextura,
                                     boolean usarColor,
                                     boolean perspectivaCorrecta,
                                     boolean usarWBuffer) {

        int minX = (int) Math.max(0, Math.min(v0.x, Math.min(v1.x, v2.x)));
        int maxX = (int) Math.min(WIDTH - 1, Math.max(v0.x, Math.max(v1.x, v2.x)));
        int minY = (int) Math.max(0, Math.min(v0.y, Math.min(v1.y, v2.y)));
        int maxY = (int) Math.min(HEIGHT - 1, Math.max(v0.y, Math.max(v1.y, v2.y)));

        float area = edge(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
        if (area == 0) return;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {

                float w0 = edge(v1.x, v1.y, v2.x, v2.y, x, y);
                float w1 = edge(v2.x, v2.y, v0.x, v0.y, x, y);
                float w2 = edge(v0.x, v0.y, v1.x, v1.y, x, y);

                if (!dentroTriangulo(w0, w1, w2, area)) {
                    continue;
                }

                w0 /= area;
                w1 /= area;
                w2 /= area;

                float invW = (w0 * (1f / v0.w)) + (w1 * (1f / v1.w)) + (w2 * (1f / v2.w));

                if (usarWBuffer) {
                    if (invW <= wBuffer[x][y]) {
                        continue;
                    }
                    wBuffer[x][y] = invW;
                }

                float r, g, b;
                float u, v;

                if (perspectivaCorrecta) {
                    r = (w0 * v0.r / v0.w + w1 * v1.r / v1.w + w2 * v2.r / v2.w) / invW;
                    g = (w0 * v0.g / v0.w + w1 * v1.g / v1.w + w2 * v2.g / v2.w) / invW;
                    b = (w0 * v0.b / v0.w + w1 * v1.b / v1.w + w2 * v2.b / v2.w) / invW;

                    u = (w0 * v0.u / v0.w + w1 * v1.u / v1.w + w2 * v2.u / v2.w) / invW;
                    v = (w0 * v0.v / v0.w + w1 * v1.v / v1.w + w2 * v2.v / v2.w) / invW;
                } else {
                    r = w0 * v0.r + w1 * v1.r + w2 * v2.r;
                    g = w0 * v0.g + w1 * v1.g + w2 * v2.g;
                    b = w0 * v0.b + w1 * v1.b + w2 * v2.b;

                    u = w0 * v0.u + w1 * v1.u + w2 * v2.u;
                    v = w0 * v0.v + w1 * v1.v + w2 * v2.v;
                }

                Color colorFinal;

                if (usarTextura && usarColor) {
                    int tx = Math.max(0, Math.min(textura.getWidth() - 1, (int) (u * (textura.getWidth() - 1))));
                    int ty = Math.max(0, Math.min(textura.getHeight() - 1, (int) (v * (textura.getHeight() - 1))));
                    Color texColor = new Color(textura.getRGB(tx, ty));
                    colorFinal = mezclarColor(texColor, r, g, b);
                } else if (usarTextura) {
                    int tx = Math.max(0, Math.min(textura.getWidth() - 1, (int) (u * (textura.getWidth() - 1))));
                    int ty = Math.max(0, Math.min(textura.getHeight() - 1, (int) (v * (textura.getHeight() - 1))));
                    colorFinal = new Color(textura.getRGB(tx, ty));
                } else if (usarColor) {
                    colorFinal = new Color(
                            Math.min(255, (int) (r * 255)),
                            Math.min(255, (int) (g * 255)),
                            Math.min(255, (int) (b * 255))
                    );
                } else {
                    colorFinal = Color.WHITE;
                }

                canvas.setRGB(x, y, colorFinal.getRGB());
            }
        }
    }

    private Color mezclarColor(Color tex, float r, float g, float b) {
        int fr = Math.min(255, (int) (tex.getRed() * r));
        int fg = Math.min(255, (int) (tex.getGreen() * g));
        int fb = Math.min(255, (int) (tex.getBlue() * b));
        return new Color(fr, fg, fb);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    private void instalarControlesTeclado() {
        setFocusable(true);
        requestFocusInWindow();

        InputMap inputMap = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0), "modo1");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0), "modo2");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0), "modo3");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0), "modo4");
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_5, 0), "modo5");

        actionMap.put("modo1", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModo(MODO_TEXTURA);
            }
        });
        actionMap.put("modo2", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModo(MODO_COLOR);
            }
        });
        actionMap.put("modo3", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModo(MODO_PROFUNDIDAD);
            }
        });
        actionMap.put("modo4", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModo(MODO_WBUFFER);
            }
        });
        actionMap.put("modo5", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setModo(MODO_COMPLETO);
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Practica de Rasterizador - Compañeros");
            EjercicioPropuesto panel = new EjercicioPropuesto();

            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLayout(new BorderLayout());
            frame.add(panel, BorderLayout.CENTER);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);

            panel.requestFocusInWindow();
        });
    }
}