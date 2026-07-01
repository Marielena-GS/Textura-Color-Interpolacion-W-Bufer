package efectos;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

/**
 * Panel personalizado que dibuja una imagen centrada con soporte de zoom.
 * - Zoom con rueda del mouse (Ctrl + scroll o scroll directo)
 * - Placeholder visual cuando no hay imagen
 * - Fondo cuadriculado para imágenes con transparencia (ARGB)
 * - Etiqueta de filtro activo superpuesta
 */
public class ImagePanel extends JPanel {

    private BufferedImage imagen;
    private String        mensajePlaceholder;
    private String        filtroActivo = "";   // etiqueta superpuesta
    private double        zoom         = 1.0;  // factor de zoom actual

    // Colores del fondo cuadriculado (indica transparencia)
    private static final Color CHECKER_CLARO  = new Color(200, 200, 200);
    private static final Color CHECKER_OSCURO = new Color(150, 150, 150);
    private static final int   CHECKER_SIZE   = 10;

    // Colores UI
    private static final Color C_FONDO      = new Color(25,  27,  40);
    private static final Color C_ACENTO     = new Color(99,  179, 237);
    private static final Color C_TEXTO_DIM  = new Color(100, 110, 140);
    private static final Color C_BADGE_BG   = new Color(20,  30,  60,  200);
    private static final Color C_BADGE_BORDE= new Color(99,  179, 237, 120);

    public ImagePanel(String placeholder) {
        this.mensajePlaceholder = placeholder;
        setBackground(C_FONDO);
        setBorder(BorderFactory.createLineBorder(new Color(60, 65, 100), 1));

        // Zoom con rueda del mouse
        addMouseWheelListener(e -> {
            double delta = e.getWheelRotation() * -0.12;
            zoom = Math.max(0.1, Math.min(zoom + delta, 8.0));
            repaint();
        });

        // Doble clic para resetear zoom
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    zoom = 1.0;
                    repaint();
                }
            }
        });

        // Tooltip con instrucciones
        setToolTipText("Scroll: zoom  |  Doble clic: reset zoom");
    }

    /** Actualiza la imagen mostrada, resetea zoom y repinta. */
    public void setImagen(BufferedImage img) {
        this.imagen = img;
        this.zoom   = 1.0;
        repaint();
    }

    /** Actualiza la etiqueta de filtro activo superpuesta sobre la imagen. */
    public void setFiltroActivo(String filtro) {
        this.filtroActivo = (filtro == null) ? "" : filtro;
        repaint();
    }

    /** Devuelve la imagen actual (puede ser null). */
    public BufferedImage getImagen() { return imagen; }

    /** Devuelve el zoom actual. */
    public double getZoom() { return zoom; }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                            RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int panelW = getWidth();
        int panelH = getHeight();

        if (imagen == null) {
            dibujarPlaceholder(g2, panelW, panelH);
            return;
        }

        // Calcular dimensiones base manteniendo relación de aspecto
        int imgW   = imagen.getWidth();
        int imgH   = imagen.getHeight();
        int margen = 8;
        int maxW   = panelW - margen * 2;
        int maxH   = panelH - margen * 2;

        double ratioBase = Math.min((double) maxW / imgW, (double) maxH / imgH);
        double ratio     = ratioBase * zoom;

        int drawW = Math.max(1, (int) (imgW * ratio));
        int drawH = Math.max(1, (int) (imgH * ratio));
        int drawX = (panelW - drawW) / 2;
        int drawY = (panelH - drawH) / 2;

        // Fondo cuadriculado solo si tiene alpha
        if (imagen.getColorModel().hasAlpha()) {
            dibujarTablero(g2, drawX, drawY, drawW, drawH);
        }

        g2.drawImage(imagen, drawX, drawY, drawW, drawH, null);

        // Badge de zoom si es distinto de 1x
        dibujarBadgeZoom(g2, panelW, panelH);

        // Badge de filtro activo
        if (!filtroActivo.isEmpty()) {
            dibujarBadgeFiltro(g2, panelW);
        }
    }

    /** Placeholder visual con ícono y texto. */
    private void dibujarPlaceholder(Graphics2D g2, int w, int h) {
        // Rectángulo central con ícono
        int bw = 72, bh = 72;
        int bx = (w - bw) / 2;
        int by = h / 2 - bh - 10;

        g2.setColor(new Color(35, 38, 58));
        g2.fillRoundRect(bx, by, bw, bh, 18, 18);
        g2.setColor(new Color(55, 62, 95));
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(bx, by, bw, bh, 18, 18);

        // Ícono simple de imagen (montaña + sol)
        g2.setColor(C_TEXTO_DIM);
        // sol
        g2.fillOval(bx + 18, by + 16, 14, 14);
        // montaña
        int[] xp = {bx + 10, bx + 30, bx + 50, bx + 62, bx + 10};
        int[] yp = {by + bh - 14, by + 30, by + 42, by + bh - 14, by + bh - 14};
        g2.fillPolygon(xp, yp, 5);

        // Texto placeholder
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        g2.setColor(C_TEXTO_DIM);
        FontMetrics fm = g2.getFontMetrics();
        int tx = (w - fm.stringWidth(mensajePlaceholder)) / 2;
        g2.drawString(mensajePlaceholder, tx, h / 2 + 30);

        // Hint de scroll
        String hint = "Scroll para hacer zoom · Doble clic para resetear";
        g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
        g2.setColor(new Color(65, 72, 105));
        fm = g2.getFontMetrics();
        g2.drawString(hint, (w - fm.stringWidth(hint)) / 2, h / 2 + 50);
    }

    /** Badge de zoom en esquina inferior derecha. */
    private void dibujarBadgeZoom(Graphics2D g2, int w, int h) {
        String txt = String.format("%.0f%%", zoom * 100);
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(txt) + 14;
        int bh = 18;
        int bx = w - bw - 8;
        int by = h - bh - 8;

        g2.setColor(C_BADGE_BG);
        g2.fillRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(C_BADGE_BORDE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, bw, bh, 8, 8);

        g2.setColor(zoom != 1.0 ? C_ACENTO : C_TEXTO_DIM);
        g2.drawString(txt, bx + 7, by + bh - 5);
    }

    /** Badge con nombre del filtro activo en esquina superior izquierda. */
    private void dibujarBadgeFiltro(Graphics2D g2, int w) {
        String txt = "● " + filtroActivo;
        g2.setFont(new Font("Segoe UI", Font.BOLD, 10));
        FontMetrics fm = g2.getFontMetrics();
        int bw = fm.stringWidth(txt) + 14;
        int bh = 18;
        int bx = 8;
        int by = 8;

        g2.setColor(C_BADGE_BG);
        g2.fillRoundRect(bx, by, bw, bh, 8, 8);
        g2.setColor(C_BADGE_BORDE);
        g2.setStroke(new BasicStroke(1f));
        g2.drawRoundRect(bx, by, bw, bh, 8, 8);

        g2.setColor(C_ACENTO);
        g2.drawString(txt, bx + 7, by + bh - 5);
    }

    /** Dibuja el patrón de tablero de ajedrez para indicar transparencia. */
    private void dibujarTablero(Graphics2D g2, int x, int y, int w, int h) {
        for (int row = 0; row * CHECKER_SIZE < h; row++) {
            for (int col = 0; col * CHECKER_SIZE < w; col++) {
                boolean claro = (row + col) % 2 == 0;
                g2.setColor(claro ? CHECKER_CLARO : CHECKER_OSCURO);
                int cx = x + col * CHECKER_SIZE;
                int cy = y + row * CHECKER_SIZE;
                int cw = Math.min(CHECKER_SIZE, x + w - cx);
                int ch = Math.min(CHECKER_SIZE, y + h - cy);
                if (cw > 0 && ch > 0) g2.fillRect(cx, cy, cw, ch);
            }
        }
    }
}