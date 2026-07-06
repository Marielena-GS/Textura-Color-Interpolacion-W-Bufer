package efectos.exposiciones.grupo9;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Blending {
    public static void main(String[] args) {

        try {

            File entradaUno = new File(
                    "src/main/java/com/edu/ec/imagenes/paisaje.png");

            File entradaDos = new File(
                    "src/main/java/com/edu/ec/imagenes/universo.png");

            File salidaArchivo = new File(
                    "src/main/java/com/edu/ec/imagenes/blending.png");

            BufferedImage fondo = ImageIO.read(entradaUno);
            BufferedImage frente = ImageIO.read(entradaDos);

            int ancho = Math.min(
                    fondo.getWidth(),
                    frente.getWidth());

            int alto = Math.min(
                    fondo.getHeight(),
                    frente.getHeight());

            BufferedImage salida = new BufferedImage(
                    ancho,
                    alto,
                    BufferedImage.TYPE_INT_ARGB);

            float alpha = 0.5f;

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {

                    Color c1 = new Color(
                            fondo.getRGB(x, y), true);

                    Color c2 = new Color(
                            frente.getRGB(x, y), true);

                    int r = (int) (alpha * c2.getRed()
                            + (1 - alpha) * c1.getRed());

                    int g = (int) (alpha * c2.getGreen()
                            + (1 - alpha) * c1.getGreen());

                    int b = (int) (alpha * c2.getBlue()
                            + (1 - alpha) * c1.getBlue());

                    Color mezcla = new Color(r, g, b);

                    salida.setRGB(x, y, mezcla.getRGB());
                }
            }

            ImageIO.write(
                    salida,
                    "png",
                    salidaArchivo);

            System.out.println(
                    "Blending aplicado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
