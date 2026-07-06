package efectos.exposiciones.grupo9;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Depth {
    public static void main(String[] args) {
        try {

            File entradaUno = new File(
                    "src/main/java/com/edu/ec/imagenes/paisaje.png");

            File entradaDos = new File(
                    "src/main/java/com/edu/ec/imagenes/universo.png");

            File salidaArchivo = new File(
                    "src/main/java/com/edu/ec/imagenes/depthTest.png");

            BufferedImage img1 = ImageIO.read(entradaUno);
            BufferedImage img2 = ImageIO.read(entradaDos);

            int ancho = Math.min(img1.getWidth(), img2.getWidth());
            int alto = Math.min(img1.getHeight(), img2.getHeight());

            BufferedImage salida = new BufferedImage(
                    ancho,
                    alto,
                    BufferedImage.TYPE_INT_ARGB);

            // Profundidades simuladas
            int x = 0;
            float zProfundidadUno = 10.0f;
            float zProfundidadDos = 5.0f;

            for (int y = 0; y < alto; y++) {
                for (x = 0; x < ancho; x++) {

                    if (zProfundidadDos < zProfundidadUno) {
                        salida.setRGB(x, y, img2.getRGB(x, y));
                    } else {
                        salida.setRGB(x, y, img1.getRGB(x, y));
                    }
                }
            }

            ImageIO.write(salida, "png", salidaArchivo);

            System.out.println("Depth Test aplicado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
