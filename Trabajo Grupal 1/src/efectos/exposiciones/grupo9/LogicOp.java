package efectos.exposiciones.grupo9;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class LogicOp {
    public static void main(String[] args) {

        try {

            File entradaUno = new File(
                    "src/main/java/com/edu/ec/imagenes/paisaje.png");

            File entradaDos = new File(
                    "src/main/java/com/edu/ec/imagenes/universo.png");

            File salidaArchivo = new File(
                    "src/main/java/com/edu/ec/imagenes/logicOp.png");

            BufferedImage img1 = ImageIO.read(entradaUno);
            BufferedImage img2 = ImageIO.read(entradaDos);

            int ancho = Math.min(
                    img1.getWidth(),
                    img2.getWidth());

            int alto = Math.min(
                    img1.getHeight(),
                    img2.getHeight());

            BufferedImage salida = new BufferedImage(
                    ancho,
                    alto,
                    BufferedImage.TYPE_INT_ARGB);

            for (int y = 0; y < alto; y++) {
                for (int x = 0; x < ancho; x++) {

                    int pixel1 = img1.getRGB(x, y);
                    int pixel2 = img2.getRGB(x, y);

                    // Operaciones:
                    // int resultado = pixel1 | pixel2;
                    int resultado = pixel1 ^ pixel2; // normal que te de en oscuro
                    // int resultado = pixel1 & pixel2;

                    salida.setRGB(x, y, resultado);
                }
            }

            ImageIO.write(salida, "png", salidaArchivo);

            System.out.println(" aplicado correctamente.");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
