package efectos.exposiciones.grupo9;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Stencil {
    public static void main(String[] args) {

        try {

            File entrada = new File(
                    "src/main/java/com/edu/ec/imagenes/paisaje.png");

            File salidaArchivo = new File(
                    "src/main/java/com/edu/ec/imagenes/paisajeStencil.png");

            BufferedImage img = ImageIO.read(entrada);

            int ancho = img.getWidth();
            int alto = img.getHeight();

            BufferedImage salida = new BufferedImage(
                    ancho,
                    alto,
                    BufferedImage.TYPE_INT_ARGB);

            int centroX = ancho / 2;
            int centroY = alto / 2;

            int radio = Math.min(ancho, alto) / 3;

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {

                    int dx = x - centroX;
                    int dy = y - centroY;

                    if (dx * dx + dy * dy <= radio * radio) {
                        salida.setRGB(x, y, img.getRGB(x, y));
                    } else {
                        salida.setRGB(x, y, 0);
                    }
                }
            }

            ImageIO.write(salida, "png", salidaArchivo);

            System.out.println("Stencil Test aplicado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
