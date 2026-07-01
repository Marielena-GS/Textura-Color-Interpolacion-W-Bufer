package rasterizador;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * ═══════════════════════════════════════════════════════════════════
 *  TALLER II – Gráficos por Computadora
 *  Rasterizador interactivo:
 *
 *    1 -> Texturas
 *    2 -> Color
 *    3 -> Interpolación en profundidad
 *    4 -> W-Buffering
 *    5 -> Todo junto
 * ═══════════════════════════════════════════════════════════════════
 */
public class Presentacion extends JPanel {

    private static final int WIDTH  = 800;
    private static final int HEIGHT = 600;

    private static final int MODO_TEXTURA     = 1;
    private static final int MODO_COLOR       = 2;
    private static final int MODO_PROFUNDIDAD = 3;
    private static final int MODO_WBUFFER     = 4;
    private static final int MODO_COMPLETO    = 5;

    private int modoActual = MODO_TEXTURA;

    private final BufferedImage canvas;
    private final BufferedImage textura;

    // W-Buffer
    // Convención unificada: almacena invW (= 1/w del fragmento).
    //   invW mayor  →  fragmento MÁS CERCANO a la cámara.
    //   Se inicializa en -∞ para que cualquier fragmento real gane
    //   la primera comparación.
  
    private final float[][] wBuffer;

    private void limpiarWBuffer() {
        for (float[] fila : wBuffer)
            Arrays.fill(fila, Float.NEGATIVE_INFINITY);  // -∞ = "vacío"
    }

    /**
     * Devuelve true y actualiza el buffer si invW es MAYOR que el
     * valor almacenado (es decir, el fragmento es más cercano).
     */
    private boolean wBufferTest(int x, int y, float invW) {
        if (invW > wBuffer[y][x]) {   // mayor invW = más cercano
            wBuffer[y][x] = invW;
            return true;              // dibujar
        }
        return false;                 // descartar
    }

    // Vértice

    static class Vertice {
        float x, y;
        float w;          // distancia a la cámara (w > 0; mayor = más lejos)
        float u, v;       // coordenadas de textura [0, 1]
        float r, g, b;    // color [0, 1]

        Vertice(float x, float y, float w,
                float u, float v,
                float r, float g, float b) {
            this.x = x; this.y = y; this.w = w;
            this.u = u; this.v = v;
            this.r = r; this.g = g; this.b = b;
        }
    }

    // Constructor

    public Presentacion() {
        canvas  = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        wBuffer = new float[HEIGHT][WIDTH];   // [fila][columna]
        textura = cargarOGenerarTextura();

        instalarControlesTeclado();
        renderizar();
    }

    // Carga de textura

    private BufferedImage cargarOGenerarTextura() {
        try {
            var recurso = Presentacion.class.getResource("textura.png");
            if (recurso != null) {
                BufferedImage img = ImageIO.read(recurso);
                if (img != null) return img;
            }
        } catch (Exception ignored) {}
        return crearTexturaTablero(256, 256);
    }

    // Controles de teclado

    private void instalarControlesTeclado() {
        setFocusable(true);
        InputMap  im = getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        int[] modos = {MODO_TEXTURA, MODO_COLOR, MODO_PROFUNDIDAD,
                       MODO_WBUFFER, MODO_COMPLETO};
        int[] teclas = {KeyEvent.VK_1, KeyEvent.VK_2, KeyEvent.VK_3,
                        KeyEvent.VK_4, KeyEvent.VK_5};
        String[] keys = {"modo1","modo2","modo3","modo4","modo5"};

        for (int i = 0; i < 5; i++) {
            final int modo = modos[i];
            im.put(KeyStroke.getKeyStroke(teclas[i], 0), keys[i]);
            am.put(keys[i], new AbstractAction() {
                @Override public void actionPerformed(ActionEvent e) {
                    modoActual = modo;
                    renderizar();
                    repaint();
                }
            });
        }
    }

    // Utilidades

    private void limpiarCanvas() {
        Graphics2D g = canvas.createGraphics();
        g.setColor(new Color(30, 30, 40));
        g.fillRect(0, 0, WIDTH, HEIGHT);
        g.dispose();
    }

    private float lerp(float a, float b, float t) {
        return a * (1f - t) + b * t;
    }

    private float edge(float ax, float ay, float bx, float by, float cx, float cy) {
        return (cx - ax) * (by - ay) - (cy - ay) * (bx - ax);
    }

    private float interpolarConPerspectiva(float a0, float a1,
                                           float w0, float w1, float t) {
        float denom = (1f / w0) * (1f - t) + (1f / w1) * t;
        float num   = (a0 / w0) * (1f - t) + (a1 / w1) * t;
        return num / denom;
    }

    private float interpolarAtributo(float a0, float a1,
                                     float w0, float w1, float t,
                                     boolean perspectiva) {
        return perspectiva
                ? interpolarConPerspectiva(a0, a1, w0, w1, t)
                : lerp(a0, a1, t);
    }

    private int clamp255(float v) {
        return Math.max(0, Math.min(255, (int) v));
    }

    private int armarColor(int a, int r, int g, int b) {
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static int extraerR(int argb) { return (argb >> 16) & 0xFF; }
    private static int extraerG(int argb) { return (argb >>  8) & 0xFF; }
    private static int extraerB(int argb) { return  argb        & 0xFF; }

    private int muestrearTextura(float u, float v) {
        u = Math.max(0f, Math.min(1f, u));
        v = Math.max(0f, Math.min(1f, v));
        int px = Math.max(0, Math.min(textura.getWidth()  - 1, (int)(u * (textura.getWidth()  - 1))));
        int py = Math.max(0, Math.min(textura.getHeight() - 1, (int)(v * (textura.getHeight() - 1))));
        return textura.getRGB(px, py);
    }

    private Color mezclarTexturaYColor(int texel, float r, float g, float b) {
        return new Color(
                clamp255(extraerR(texel) * r),
                clamp255(extraerG(texel) * g),
                clamp255(extraerB(texel) * b));
    }

    private void dibujarCabecera(Graphics2D g, String titulo, String subtitulo) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString(titulo, 20, 28);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString(subtitulo, 20, 50);
        g.drawString("1 Texturas  |  2 Color  |  3 Depth Interpolation  |  4 W-Buffering  |  5 Todo junto",
                20, HEIGHT - 18);
    }

    // Render principal

    private void renderizar() {
        limpiarCanvas();
        limpiarWBuffer();

        Graphics2D g = canvas.createGraphics();
        switch (modoActual) {
            case MODO_TEXTURA     -> renderModoTextura(g);
            case MODO_COLOR       -> renderModoColor(g);
            case MODO_PROFUNDIDAD -> renderModoProfundidad(g);
            case MODO_WBUFFER     -> renderModoWBuffer(g);
            case MODO_COMPLETO    -> renderModoCompleto(g);
        }
        g.dispose();
    }

    // MODO 1: TEXTURAS

    private void renderModoTextura(Graphics2D g) {
        dibujarCabecera(g,
                "1) TEXTURAS",
                "Se aplica la imagen sobre el triángulo usando coordenadas UV.");
        g.dispose();

        Vertice a = new Vertice(160, 120, 1.5f, 0f,   0f, 1f, 1f, 1f);
        Vertice b = new Vertice(640, 160, 0.7f, 1f,   0f, 1f, 1f, 1f);
        Vertice c = new Vertice(400, 500, 0.3f, 0.5f, 1f, 1f, 1f, 1f);

        rellenarTriangulo(a, b, c,true, false, true, false);
    }

    // MODO 2: COLOR

    private void renderModoColor(Graphics2D g) {
        dibujarCabecera(g,
                "2) COLOR",
                "Cada vértice tiene un color distinto y se interpola en la superficie.");
        g.dispose();

        Vertice a = new Vertice(160, 120, 1.5f, 0f,   0f, 1f, 0f, 0f); // rojo
        Vertice b = new Vertice(640, 160, 0.7f, 1f,   0f, 0f, 1f, 0f); // verde
        Vertice c = new Vertice(400, 500, 0.3f, 0.5f, 1f, 0f, 0f, 1f); // azul

        rellenarTriangulo(a, b, c, false, true, true, false);
    }

    // MODO 3: INTERPOLACIÓN EN PROFUNDIDAD
    //   Izquierda: interpolación lineal  (sin corrección)
    //   Derecha:   corrección con 1/W   (perspectiva correcta)

    private void renderModoProfundidad(Graphics2D g) {
        dibujarCabecera(g,
                "3) INTERPOLACIÓN EN PROFUNDIDAD",
                "Izquierda: interpolación lineal   |   Derecha: corrección con 1/W");

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(180, 180, 180));
        g.drawLine(WIDTH / 2, 70, WIDTH / 2, HEIGHT - 35);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString("Sin corrección", 80, 90);
        g.drawString("Con corrección", 520, 90);
        g.dispose();

        // Misma forma, mismos w — solo cambia el flag de perspectiva
        Vertice a1 = new Vertice( 70, 130, 1.8f, 0f,    0f, 1f, 1f, 1f);
        Vertice b1 = new Vertice(320, 170, 0.6f, 1f,    0f, 1f, 1f, 1f);  // w muy diferente al de a
        Vertice c1 = new Vertice(200, 520, 0.2f, 0.15f, 1f, 1f, 1f, 1f);

        Vertice a2 = new Vertice(470, 130, 1.8f, 0f,    0f, 1f, 1f, 1f);
        Vertice b2 = new Vertice(720, 170, 0.6f, 1f,    0f, 1f, 1f, 1f);
        Vertice c2 = new Vertice(600, 520, 0.2f, 0.15f, 1f, 1f, 1f, 1f);

        rellenarTriangulo(a1, b1, c1, true, false, false, false); // sin corrección
        rellenarTriangulo(a2, b2, c2, true, false, true,  false); // con corrección
    }

    // MODO 4: W-BUFFERING
    //   Izquierda: SIN buffer  → el orden de dibujado manda.
    //              Se pinta primero el FONDO y luego el FRENTE,
    //              pero el frente tapa al fondo correctamente
    //              solo porque se dibujó después.  Sin buffer,
    //              si invirtiéramos el orden el resultado sería
    //              incorrecto.  Esto demuestra la fragilidad del
    //              enfoque sin buffer.
    //
    //   Derecha:   CON W-Buffer → el orden no importa, el
    //              fragmento más cercano siempre gana.

    private void renderModoWBuffer(Graphics2D g) {
        dibujarCabecera(g,
                "4) W-BUFFERING",
                "Izquierda: sin buffer (orden de dibujado manda)   |   Derecha: con W-Buffer");

        g.setStroke(new BasicStroke(2f));
        g.setColor(new Color(180, 180, 180));
        g.drawLine(WIDTH / 2, 70, WIDTH / 2, HEIGHT - 35);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 13));
        g.drawString("Sin W-Buffer", 95, 90);
        g.drawString("Con W-Buffer", 535, 90);
        g.dispose();

        // ── Lado izquierdo: SIN W-Buffer ─────────────────────────
        //    FONDO primero → FRENTE después.
        //    El frente (w=2) tapa al fondo (w=5) solo porque se
        //    dibuja en segundo lugar.  Si invirtieras este orden,
        //    el fondo taparía al frente y el resultado sería WRONG.
        //    Eso demuestra que sin buffer el orden de llamadas manda.

        Vertice fondo1 = new Vertice( 70, 120, 5f, 0f,   0f, 0.2f, 0.4f, 1f);   // azul (lejano)
        Vertice fondo2 = new Vertice(300, 160, 5f, 1f,   0f, 0.2f, 0.4f, 1f);
        Vertice fondo3 = new Vertice(200, 500, 5f, 0.5f, 1f, 0.2f, 0.4f, 1f);

        Vertice frente1 = new Vertice(110, 140, 2f, 0f,   0f, 1f, 0.8f, 0.1f);  // naranja (cercano)
        Vertice frente2 = new Vertice(340, 180, 2f, 1f,   0f, 1f, 0.8f, 0.1f);
        Vertice frente3 = new Vertice(250, 520, 2f, 0.5f, 1f, 1f, 0.8f, 0.1f);

        // Fondo primero, frente después → sin buffer, el último pintado gana
        rellenarTriangulo(fondo1,  fondo2,  fondo3,  false, true, true, false);
        rellenarTriangulo(frente1, frente2, frente3, false, true, true, false);

        // ── Lado derecho: CON W-Buffer ────────────────────────────
        //    Mismos triángulos pero ahora pintamos el FRENTE primero
        //    y el FONDO después.  El W-Buffer garantiza que el fondo
        //    NO sobreescribe los píxeles ya ganados por el frente.
        Vertice fondo1D = new Vertice(470, 120, 5f, 0f,   0f, 0.2f, 0.4f, 1f);
        Vertice fondo2D = new Vertice(700, 160, 5f, 1f,   0f, 0.2f, 0.4f, 1f);
        Vertice fondo3D = new Vertice(600, 500, 5f, 0.5f, 1f, 0.2f, 0.4f, 1f);

        Vertice frente1D = new Vertice(510, 140, 2f, 0f,   0f, 1f, 0.8f, 0.1f);
        Vertice frente2D = new Vertice(740, 180, 2f, 1f,   0f, 1f, 0.8f, 0.1f);
        Vertice frente3D = new Vertice(650, 520, 2f, 0.5f, 1f, 1f, 0.8f, 0.1f);

        // Frente primero, fondo después → con buffer, el W-Buffer protege al frente
        rellenarTriangulo(frente1D, frente2D, frente3D, false, true, true, true);
        rellenarTriangulo(fondo1D,  fondo2D,  fondo3D,  false, true, true, true);
    }

    // MODO 5: TODO JUNTO

    private void renderModoCompleto(Graphics2D g) {
        dibujarCabecera(g,
                "5) TODO JUNTO",
                "Textura + color + interpolación en profundidad + W-Buffer.");
        g.dispose();

        Vertice fondoA = new Vertice(150, 120, 5f, 0f,   0f, 1f,  0.2f, 1f);
        Vertice fondoB = new Vertice(620, 160, 5f, 1f,   0f, 0f,  1f,   0.2f);
        Vertice fondoC = new Vertice(500, 520, 5f, 0.5f, 1f, 0f,  1f,   1f);

        Vertice frenteA = new Vertice(220, 180, 2f, 0f,   0f, 1f, 1f,  0.2f);
        Vertice frenteB = new Vertice(700, 220, 2f, 1f,   0f, 0f, 0.2f, 1f);
        Vertice frenteC = new Vertice(580, 550, 2f, 0.5f, 1f, 0f, 1f,   1f);

        rellenarTriangulo(fondoA,  fondoB,  fondoC,  true, true, true, true);
        rellenarTriangulo(frenteA, frenteB, frenteC, true, true, true, true);
    }

    // Rasterizador de triángulo por scanline

    private void rellenarTriangulo(Vertice a, Vertice b, Vertice c,
                                   boolean usarTextura,
                                   boolean usarColor,
                                   boolean corregirPerspectiva,
                                   boolean usarWBuffer) {

        float area = edge(a.x, a.y, b.x, b.y, c.x, c.y);
        if (area == 0) return;

        // Copiar atributos para poder reordenar por Y
        int   x0 = (int)a.x, y0 = (int)a.y; float u0=a.u,v0=a.v,w0=a.w,r0=a.r,g0=a.g,b0=a.b;
        int   x1 = (int)b.x, y1 = (int)b.y; float u1=b.u,v1=b.v,w1=b.w,r1=b.r,g1=b.g,b1=b.b;
        int   x2 = (int)c.x, y2 = (int)c.y; float u2=c.u,v2=c.v,w2=c.w,r2=c.r,g2=c.g,b2=c.b;

        // Ordenar vértices por Y (burbuja de 3)
        if (y0 > y1) {
            int tx=x0;x0=x1;x1=tx; int ty=y0;y0=y1;y1=ty;
            float tu=u0;u0=u1;u1=tu; float tv=v0;v0=v1;v1=tv;
            float tw=w0;w0=w1;w1=tw; float tr=r0;r0=r1;r1=tr;
            float tg=g0;g0=g1;g1=tg; float tb=b0;b0=b1;b1=tb;
        }
        if (y0 > y2) {
            int tx=x0;x0=x2;x2=tx; int ty=y0;y0=y2;y2=ty;
            float tu=u0;u0=u2;u2=tu; float tv=v0;v0=v2;v2=tv;
            float tw=w0;w0=w2;w2=tw; float tr=r0;r0=r2;r2=tr;
            float tg=g0;g0=g2;g2=tg; float tb=b0;b0=b2;b2=tb;
        }
        if (y1 > y2) {
            int tx=x1;x1=x2;x2=tx; int ty=y1;y1=y2;y2=ty;
            float tu=u1;u1=u2;u2=tu; float tv=v1;v1=v2;v2=tv;
            float tw=w1;w1=w2;w2=tw; float tr=r1;r1=r2;r2=tr;
            float tg=g1;g1=g2;g2=tg; float tb=b1;b1=b2;b2=tb;
        }

        for (int scanY = y0; scanY <= y2; scanY++) {

            // Borde largo v0→v2
            float tA = (y2 == y0) ? 0f : (float)(scanY - y0) / (y2 - y0);
            float xA = lerp(x0, x2, tA);
            float uA = interpolarAtributo(u0,u2,w0,w2,tA,corregirPerspectiva);
            float vA = interpolarAtributo(v0,v2,w0,w2,tA,corregirPerspectiva);
            float rA = interpolarAtributo(r0,r2,w0,w2,tA,corregirPerspectiva);
            float gA = interpolarAtributo(g0,g2,w0,w2,tA,corregirPerspectiva);
            float bA = interpolarAtributo(b0,b2,w0,w2,tA,corregirPerspectiva);
            float wA = interpolarAtributo(w0,w2,w0,w2,tA,corregirPerspectiva);

            // Borde corto v0→v1 ó v1→v2
            float tB, xB, uB, vB, rB, gB, bB, wB;
            if (scanY <= y1) {
                tB = (y1 == y0) ? 0f : (float)(scanY - y0) / (y1 - y0);
                xB = lerp(x0, x1, tB);
                uB = interpolarAtributo(u0,u1,w0,w1,tB,corregirPerspectiva);
                vB = interpolarAtributo(v0,v1,w0,w1,tB,corregirPerspectiva);
                rB = interpolarAtributo(r0,r1,w0,w1,tB,corregirPerspectiva);
                gB = interpolarAtributo(g0,g1,w0,w1,tB,corregirPerspectiva);
                bB = interpolarAtributo(b0,b1,w0,w1,tB,corregirPerspectiva);
                wB = interpolarAtributo(w0,w1,w0,w1,tB,corregirPerspectiva);
            } else {
                tB = (y2 == y1) ? 0f : (float)(scanY - y1) / (y2 - y1);
                xB = lerp(x1, x2, tB);
                uB = interpolarAtributo(u1,u2,w1,w2,tB,corregirPerspectiva);
                vB = interpolarAtributo(v1,v2,w1,w2,tB,corregirPerspectiva);
                rB = interpolarAtributo(r1,r2,w1,w2,tB,corregirPerspectiva);
                gB = interpolarAtributo(g1,g2,w1,w2,tB,corregirPerspectiva);
                bB = interpolarAtributo(b1,b2,w1,w2,tB,corregirPerspectiva);
                wB = interpolarAtributo(w1,w2,w1,w2,tB,corregirPerspectiva);
            }

            int xIni = (int) Math.min(xA, xB);
            int xFin = (int) Math.max(xA, xB);

            for (int scanX = xIni; scanX <= xFin; scanX++) {
                if (scanX < 0 || scanX >= WIDTH || scanY < 0 || scanY >= HEIGHT)
                    continue;

                float t2 = (xFin == xIni) ? 0f : (float)(scanX - xIni) / (xFin - xIni);

                float u   = interpolarAtributo(uA,uB,wA,wB,t2,corregirPerspectiva);
                float v   = interpolarAtributo(vA,vB,wA,wB,t2,corregirPerspectiva);
                float r   = interpolarAtributo(rA,rB,wA,wB,t2,corregirPerspectiva);
                float gC  = interpolarAtributo(gA,gB,wA,wB,t2,corregirPerspectiva);
                float bl  = interpolarAtributo(bA,bB,wA,wB,t2,corregirPerspectiva);
                float wF  = interpolarAtributo(wA,wB,wA,wB,t2,corregirPerspectiva);

                // W-BUFFER TEST
                //    invW = 1/w: mayor = más cercano a la cámara.
                //    Se inicializó en -∞, así que cualquier fragmento
                //    real gana la primera vez.
                float invW = 1f / wF;
                if (usarWBuffer && !wBufferTest(scanX, scanY, invW))
                    continue;   // hay algo más cercano → descartar

                // COLOR FINAL
                int colorFinal;
                if (usarTextura && usarColor) {
                    colorFinal = mezclarTexturaYColor(muestrearTextura(u, v), r, gC, bl).getRGB();
                } else if (usarTextura) {
                    colorFinal = muestrearTextura(u, v);
                } else if (usarColor) {
                    colorFinal = armarColor(255, clamp255(r*255f), clamp255(gC*255f), clamp255(bl*255f));
                } else {
                    colorFinal = Color.WHITE.getRGB();
                }

                canvas.setRGB(scanX, scanY, colorFinal);
            }
        }
    }

    // Textura tablero de ajedrez

    private BufferedImage crearTexturaTablero(int ancho, int alto) {
        BufferedImage img = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < alto; y++)
            for (int x = 0; x < ancho; x++) {
                boolean clara = ((x / 32) + (y / 32)) % 2 == 0;
                img.setRGB(x, y, clara ? new Color(240,240,240).getRGB()
                                       : new Color(35, 35, 35).getRGB());
            }
        return img;
    }

    // Pintado

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(canvas, 0, 0, null);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    // Main

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Rasterizador interactivo");
            Presentacion panel = new Presentacion();
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
