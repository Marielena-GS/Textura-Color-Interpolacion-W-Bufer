package efectos.exposiciones.grupo8;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.imageio.ImageIO;

public class ProcesoFragmentos {
    static final int MASCARA = 0xFF;

    public static void main(String[] args) {

        try {

            // Cargar imágenes

            BufferedImage imagen1 = ImageIO.read(
                    new File("src/Imagenes/MascMundial.jpg"));

            BufferedImage imagen2 = ImageIO.read(
                    new File("src/Imagenes/perro.jpg"));

            // Crear máscara circular

            BufferedImage stencil = crearStencil(
                    imagen1.getWidth(),
                    imagen1.getHeight());

            // PARTE 1

            BufferedImage resultadoStencil = aplicarStencil(imagen1, stencil);

            ImageIO.write(resultadoStencil,
                    "jpg",
                    new File("src/Resultados/Stencil.jpg"));

            System.out.println("Stencil.jpg generado");

            // Redimensionar imagen2

            BufferedImage imagen2Redimensionada = new BufferedImage(
                    resultadoStencil.getWidth(),
                    resultadoStencil.getHeight(),
                    BufferedImage.TYPE_INT_RGB);

            Graphics2D g = imagen2Redimensionada.createGraphics();

            g.drawImage(imagen2,
                    0,
                    0,
                    resultadoStencil.getWidth(),
                    resultadoStencil.getHeight(),
                    null);

            g.dispose();

            // PARTE 2

            BufferedImage blending = aplicarBlending(
                    resultadoStencil,
                    imagen2Redimensionada,
                    0.6f);

            ImageIO.write(blending,
                    "jpg",
                    new File("src/Resultados/Blending.jpg"));

            System.out.println("Blending.jpg generado");

            // PARTE 3

            BufferedImage resultadoFinal = aplicarXOR(
                    blending,
                    imagen2Redimensionada);

            ImageIO.write(resultadoFinal,
                    "jpg",
                    new File("src/Resultados/ResultadoFinal.jpg"));

            System.out.println("ResultadoFinal.jpg generado");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    // CREA LA MÁSCARA

    public static BufferedImage crearStencil(int ancho, int alto) {

        BufferedImage img = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_RGB);

        Graphics2D g = img.createGraphics();

        g.setColor(Color.BLACK);

        g.fillRect(0, 0, ancho, alto);

        g.setColor(Color.WHITE);

        int diametro = Math.min(ancho, alto) - 100;

        g.fillOval(
                (ancho - diametro) / 2,
                (alto - diametro) / 2,
                diametro,
                diametro);

        g.dispose();

        return img;

    }

    // STENCIL TEST

    public static BufferedImage aplicarStencil(
            BufferedImage imagen,
            BufferedImage mascara) {

        int ancho = imagen.getWidth();

        int alto = imagen.getHeight();

        BufferedImage salida = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {

            for (int x = 0; x < ancho; x++) {

                int pixelMascara = mascara.getRGB(x, y);

                int r = (pixelMascara >> 16) & MASCARA;
                int g = (pixelMascara >> 8) & MASCARA;
                int b = pixelMascara & MASCARA;

                int gris = (r + g + b) / 3;

                if (gris > 128) {

                    salida.setRGB(x, y,
                            imagen.getRGB(x, y));

                } else {

                    salida.setRGB(x, y, 0xFF000000);

                }

            }

        }

        return salida;

    }

    // BLENDING

    public static BufferedImage aplicarBlending(
            BufferedImage fondo,
            BufferedImage superior,
            float alpha) {

        int ancho = fondo.getWidth();

        int alto = fondo.getHeight();

        BufferedImage salida = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {

            for (int x = 0; x < ancho; x++) {

                int p1 = fondo.getRGB(x, y);

                int p2 = superior.getRGB(x, y);

                int r1 = (p1 >> 16) & MASCARA;
                int g1 = (p1 >> 8) & MASCARA;
                int b1 = p1 & MASCARA;

                int r2 = (p2 >> 16) & MASCARA;
                int g2 = (p2 >> 8) & MASCARA;
                int b2 = p2 & MASCARA;

                int r = (int) (r2 * alpha + r1 * (1 - alpha));
                int g = (int) (g2 * alpha + g1 * (1 - alpha));
                int b = (int) (b2 * alpha + b1 * (1 - alpha));

                int pixel = (r << 16) | (g << 8) | b;

                salida.setRGB(x, y, pixel);

            }

        }

        return salida;

    }

    // XOR

    public static BufferedImage aplicarXOR(
            BufferedImage img1,
            BufferedImage img2) {

        int ancho = img1.getWidth();

        int alto = img1.getHeight();

        BufferedImage salida = new BufferedImage(
                ancho,
                alto,
                BufferedImage.TYPE_INT_RGB);

        for (int y = 0; y < alto; y++) {

            for (int x = 0; x < ancho; x++) {

                int p1 = img1.getRGB(x, y);

                int p2 = img2.getRGB(x, y);

                int r1 = (p1 >> 16) & MASCARA;
                int g1 = (p1 >> 8) & MASCARA;
                int b1 = p1 & MASCARA;

                int r2 = (p2 >> 16) & MASCARA;
                int g2 = (p2 >> 8) & MASCARA;
                int b2 = p2 & MASCARA;

                int r = r1 ^ r2;
                int g = g1 ^ g2;
                int b = b1 ^ b2;

                int pixel = (r << 16) | (g << 8) | b;

                salida.setRGB(x, y, pixel);

            }

        }

        return salida;

    }
}
