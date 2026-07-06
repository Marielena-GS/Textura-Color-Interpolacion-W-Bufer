package efectos.exposiciones.grupo9;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Alpha {
    public static void main(String[] args) {

        try {

            File entrada = new File(
                    "src/main/java/com/edu/ec/imagenes/paisaje.png");

            File archivoSalida = new File(
                    "src/main/java/com/edu/ec/imagenes/AlphaTest.png");

            // Leer imagen
            BufferedImage img = ImageIO.read(entrada);

            int ancho = img.getWidth();
            int alto = img.getHeight();

            // Crear imagen de salida
            BufferedImage salida = new BufferedImage(
                    ancho,
                    alto,
                    BufferedImage.TYPE_INT_ARGB);

            // Alpha Test usando brillo promedio
            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {

                    int pixel = img.getRGB(x, y);

                    Color c = new Color(pixel, true);

                    int promedio = (c.getRed()
                            + c.getGreen()
                            + c.getBlue()) / 3;

                    if (promedio > 128) {
                        salida.setRGB(x, y, pixel);
                    } else {
                        salida.setRGB(x, y, 0); // transparente
                    }
                }
            }

            ImageIO.write(salida, "png", archivoSalida);

            System.out.println("Imagen procesada correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
