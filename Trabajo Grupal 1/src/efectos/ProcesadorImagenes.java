package efectos;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.util.Random;

/**
 * Motor central de procesamiento de imagenes.
 *
 * Contiene todos los efectos del grupo unificados en metodos estaticos. Cada
 * metodo recibe una BufferedImage de entrada y devuelve una NUEVA imagen con el
 * efecto aplicado — nunca modifica la imagen original.
 */
public class ProcesadorImagenes {

	private static final Random random = new Random();

	private static void validar(BufferedImage img) {
		if (img == null) {
			throw new IllegalArgumentException("Se necesita cargar una imagen primero.");
		}
	}

	private static int clamp(int valor) {
		return Math.max(0, Math.min(255, valor));
	}

	/**
	 * Genera una imagen con pixeles de colores completamente aleatorios.
	 *
	 * @param ancho ancho en pixeles de la imagen a generar
	 * @param alto  alto en pixeles de la imagen a generar
	 * @return nueva BufferedImage con pixeles aleatorios
	 */
	public static BufferedImage imagenAleatoria(int ancho, int alto) {
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int r = random.nextInt(256);
				int g = random.nextInt(256);
				int b = random.nextInt(256);
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Copia pixel a pixel la imagen de entrada.
	 *
	 * @param img imagen de entrada
	 * @return copia exacta de la imagen
	 */
	public static BufferedImage copiarImagen(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				resultado.setRGB(x, y, img.getRGB(x, y));
			}
		}
		return resultado;
	}

	/**
	 * Aplica convolucion manual 3x3 con el kernel. El kernel debe tener exactamente
	 * 9 elementos.
	 *
	 * @param img    imagen de entrada
	 * @param kernel arreglo de 9 floats (fila por fila)
	 * @return imagen con convolucion aplicada
	 */
	public static BufferedImage convolucionManual(BufferedImage img, float[][] kernel) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 1; y < alto - 1; y++) {
			for (int x = 1; x < ancho - 1; x++) {
				float sumaR = 0, sumaG = 0, sumaB = 0;

				for (int i = -1; i < 2; i++) {
					for (int j = -1; j < 2; j++) {
						int pixel = img.getRGB(x + j, y + i);
						int r = (pixel >> 16) & 0xFF;
						int g = (pixel >> 8) & 0xFF;
						int b = (pixel >> 0) & 0xFF;
						sumaR += r * kernel[i + 1][j + 1];
						sumaG += g * kernel[i + 1][j + 1];
						sumaB += b * kernel[i + 1][j + 1];
					}
				}

				int r = clamp((int) sumaR);
				int g = clamp((int) sumaG);
				int b = clamp((int) sumaB);
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Aplica convolucion usando ConvolveOp de Java. Mas eficiente que la manual
	 * para imagenes grandes.
	 *
	 * @param img    imagen de entrada
	 * @param kernel arreglo plano de floats (ej. Kernels.kEnfoque)
	 * @return imagen con convolucion aplicada
	 */
	public static BufferedImage convolucionOp(BufferedImage img, float[] kernel) {
		validar(img);
		int tamanio = (int) Math.sqrt(kernel.length);
		Kernel k = new Kernel(tamanio, tamanio, kernel);
		ConvolveOp op = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, null);

		// ConvolveOp requiere TYPE_INT_RGB
		BufferedImage rgbImg = toRGB(img);
		return op.filter(rgbImg, null);
	}

	/**
	 * Genera 10 imagenes aplicando convolucion con intensidad creciente (efecto
	 * amanecer). Retorna un arreglo con las 10 imagenes.
	 *
	 * @param img imagen de entrada
	 * @return arreglo de 10 BufferedImage (indices 0..9)
	 */
	public static BufferedImage[] convolucionAmanecer(BufferedImage img) {
		validar(img);
		BufferedImage[] imagenes = new BufferedImage[10];
		float[][] matrizAmanecer = { { 0, 0, 0 }, { 0, 1, 0 }, { 0, 0, 0 } };

		for (int i = 0; i < 10; i++) {
			float intensidad = i / 9.0f;
			int ancho = img.getWidth();
			int alto = img.getHeight();
			BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

			for (int y = 1; y < alto - 1; y++) {
				for (int x = 1; x < ancho - 1; x++) {
					float sumaR = 0, sumaG = 0, sumaB = 0;
					for (int dy = -1; dy < 2; dy++) {
						for (int dx = -1; dx < 2; dx++) {
							int pixel = img.getRGB(x + dx, y + dy);
							int r = (pixel >> 16) & 0xFF;
							int g = (pixel >> 8) & 0xFF;
							int b = (pixel >> 0) & 0xFF;
							sumaR += r * matrizAmanecer[dy + 1][dx + 1];
							sumaG += g * matrizAmanecer[dy + 1][dx + 1];
							sumaB += b * matrizAmanecer[dy + 1][dx + 1];
						}
					}
					int r = clamp((int) (sumaR * intensidad));
					int g = clamp((int) (sumaG * intensidad));
					int b = clamp((int) (sumaB * intensidad));
					resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
				}
			}
			imagenes[i] = resultado;
		}
		return imagenes;
	}

	/**
	 * Blanco y negro: cada canal se convierte a 0 o 255 segun luminancia.
	 */
	public static BufferedImage blancoNegro(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				int gris = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
				int bw = (gris >= 128) ? 255 : 0;

				resultado.setRGB(x, y, (a << 24) | (bw << 16) | (bw << 8) | bw);
			}
		}
		return resultado;
	}

	/**
	 * Escala de grises con N niveles. N=2 → solo blanco/negro, N=255 → grises
	 * continuos.
	 *
	 * @param img imagen de entrada
	 * @param N   numero de niveles de gris (minimo 2)
	 */
	public static BufferedImage escalaGrises(BufferedImage img, int N) {
		validar(img);
		if (N < 2)
			N = 2;
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		float paso = 255f / (N - 1);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				int gris = (int) (0.2126 * r + 0.7152 * g + 0.0722 * b);
				int nivel = Math.round(gris / paso);
				gris = clamp(Math.round(nivel * paso));

				resultado.setRGB(x, y, (a << 24) | (gris << 16) | (gris << 8) | gris);
			}
		}
		return resultado;
	}

	/**
	 * Escala de grises usando el canal V (Value) del espacio HSV
	 * 
	 */
	public static BufferedImage escalaGrisesHSV(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				float[] hsv = Color.RGBtoHSB(r, g, b, null);
				// saturation = 0 para grises
				int pixelAux = Color.HSBtoRGB(hsv[0], 0, hsv[2]);
				resultado.setRGB(x, y, pixelAux);
			}
		}
		return resultado;
	}

	/**
	 * Efecto Retro 1: reduce colores a N niveles por canal RGB
	 *
	 * @param img imagen de entrada
	 * @param N   numero de niveles por canal (2, 4, 8, 64, 128, 255)
	 */
	public static BufferedImage efectorRetro1(BufferedImage img, int N) {
		validar(img);
		if (N < 2)
			N = 2;
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		float paso = 255f / (N - 1);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				r = clamp(Math.round(Math.round(r / paso) * paso));
				g = clamp(Math.round(Math.round(g / paso) * paso));
				b = clamp(Math.round(Math.round(b / paso) * paso));

				resultado.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Efecto Retro 2: reduce colores a N niveles en solo 2 canales
	 *
	 * @param img  imagen de entrada
	 * @param N    numero de niveles (2, 4, 8, 64, 128, 255)
	 * @param modo canal a usar: "RG", "RB" o "GB"
	 */
	public static BufferedImage efectorRetro2(BufferedImage img, int N, String modo) {
		validar(img);
		if (N < 2)
			N = 2;
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);
		float paso = 255f / (N - 1);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				if (modo.equalsIgnoreCase("RG")) {
					r = clamp(Math.round(Math.round(r / paso) * paso));
					g = clamp(Math.round(Math.round(g / paso) * paso));
					b = 0;
				} else if (modo.equalsIgnoreCase("RB")) {
					r = clamp(Math.round(Math.round(r / paso) * paso));
					g = 0;
					b = clamp(Math.round(Math.round(b / paso) * paso));
				} else { // GB
					r = 0;
					g = clamp(Math.round(Math.round(g / paso) * paso));
					b = clamp(Math.round(Math.round(b / paso) * paso));
				}
				resultado.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Filtro negativo: invierte todos los canales RGB
	 */
	public static BufferedImage filtroNegativo(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;
				resultado.setRGB(x, y, ((255 - r) << 16) | ((255 - g) << 8) | (255 - b));
			}
		}
		return resultado;
	}

	/**
	 * Genera una imagen con el histograma RGB de la imagen de entrada.
	 */
	public static BufferedImage generarHistograma(BufferedImage img) {
		validar(img);

		final int widthHistograma = 800;
		final int heightHistograma = 600;
		int[] histogramaRed = new int[256];
		int[] histogramaGreen = new int[256];
		int[] histogramaBlue = new int[256];

		int ancho = img.getWidth();
		int alto = img.getHeight();

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int red = (pixel >> 16) & 0xFF;
				int green = (pixel >> 8) & 0xFF;
				int blue = pixel & 0xFF;

				histogramaRed[red]++;
				histogramaGreen[green]++;
				histogramaBlue[blue]++;
			}
		}

		BufferedImage histograma = new BufferedImage(widthHistograma, heightHistograma, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = histograma.createGraphics();
		graphics.setColor(Color.BLACK);
		graphics.fillRect(0, 0, widthHistograma, heightHistograma);
		graphics.setStroke(new java.awt.BasicStroke(2));

		int maximoGeneral = Math.max(maximoValorHistograma(histogramaRed),
				Math.max(maximoValorHistograma(histogramaGreen), maximoValorHistograma(histogramaBlue)));
		float escalaX = widthHistograma / 256.0f;
		float escalaY = maximoGeneral == 0 ? 0 : heightHistograma * 1.0f / maximoGeneral;

		dibujarHistograma(graphics, histogramaRed, Color.RED, escalaX, escalaY, heightHistograma);
		dibujarHistograma(graphics, histogramaGreen, Color.GREEN, escalaX, escalaY, heightHistograma);
		dibujarHistograma(graphics, histogramaBlue, Color.BLUE, escalaX, escalaY, heightHistograma);

		graphics.dispose();
		return histograma;
	}

	/**
	 * Combina dos imagenes usando distintos modos de blending.
	 * La segunda imagen se escala al tamanio de la primera.
	 */
	public static BufferedImage blending(BufferedImage img1, BufferedImage img2, String modo, float alpha) {
		validar(img1);
		validar(img2);

		int ancho = img1.getWidth();
		int alto = img1.getHeight();
		BufferedImage img2Escalada = escalarImagen(img2, ancho, alto);
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		float a = Math.max(0f, Math.min(1f, alpha));

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel1 = img1.getRGB(x, y);
				int pixel2 = img2Escalada.getRGB(x, y);

				int r1 = (pixel1 >> 16) & 0xFF;
				int g1 = (pixel1 >> 8) & 0xFF;
				int b1 = pixel1 & 0xFF;

				int r2 = (pixel2 >> 16) & 0xFF;
				int g2 = (pixel2 >> 8) & 0xFF;
				int b2 = pixel2 & 0xFF;

				int r, g, b;
				if ("Sumativa".equalsIgnoreCase(modo)) {
					r = clamp(r1 + r2);
					g = clamp(g1 + g2);
					b = clamp(b1 + b2);
				} else if ("Multiplicativa".equalsIgnoreCase(modo)) {
					r = (r1 * r2) / 255;
					g = (g1 * g2) / 255;
					b = (b1 * b2) / 255;
				} else {
					r = clamp((int) ((1 - a) * r1 + a * r2));
					g = clamp((int) ((1 - a) * g1 + a * g2));
					b = clamp((int) ((1 - a) * b1 + a * b2));
				}

				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Modifica saturacion y brillo en espacio HSV.
	 *
	 * @param img              imagen de entrada
	 * @param factorSaturacion factor para saturacion (>1 satura mas)
	 * @param factorBrillo     factor para brillo/value (>1 aclara)
	 */
	public static BufferedImage filtrosHSV(BufferedImage img, float factorSaturacion, float factorBrillo) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				float[] hsv = Color.RGBtoHSB(r, g, b, null);
				hsv[1] = Math.min(1f, hsv[1] * factorSaturacion);
				hsv[2] = Math.min(1f, hsv[2] * factorBrillo);

				resultado.setRGB(x, y, Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]));
			}
		}
		return resultado;
	}

	/**
	 * Saturacion HSV avanzada: modifica RGB antes de convertir a HSV.
	 *
	 * @param img     imagen de entrada
	 * @param factorS factor de saturacion (ej. 0.4f)
	 * @param factorB factor de brillo (ej. 2.4f)
	 */
	public static BufferedImage saturacionHSV(BufferedImage img, float factorS, float factorB) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				// Reducir intensidad como en tu codigo original
				r = (int) (r * 0.2);
				g = (int) (g * 0.5);
				b = (int) (b * 0.2);

				float[] hsv = Color.RGBtoHSB(r, g, b, null);
				hsv[1] = Math.min(1f, hsv[1] * factorS);
				hsv[2] = Math.min(1f, hsv[2] * factorB);

				int pixelNuevo = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
				resultado.setRGB(x, y, pixelNuevo);
			}
		}
		return resultado;
	}

	/**
	 * Brillo por canal: suma un valor fijo a cada canal RGB
	 *
	 * @param img    imagen de entrada
	 * @param brillo valor a sumar a cada canal (puede ser negativo para oscurecer)
	 */
	public static BufferedImage brilloPorCanal(BufferedImage img, int brillo) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int r = clamp(((pixel >> 16) & 0xFF) + brillo);
				int g = clamp(((pixel >> 8) & 0xFF) + brillo);
				int b = clamp(((pixel >> 0) & 0xFF) + brillo);
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Canal Alpha: modifica transparencia por factor
	 *
	 * @param img                 imagen de entrada
	 * @param factorTransparencia factor para multiplicar el alpha (ej. 1.5f)
	 */
	public static BufferedImage canalAlpha(BufferedImage img, float factorTransparencia) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int alpha = clamp((int) (((pixel >> 24) & 0xFF) * factorTransparencia));
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;
				resultado.setRGB(x, y, (alpha << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Desvanecimiento circular: centro opaco, bordes transparentes
	 */
	public static BufferedImage desvanecimientoCircular(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		double centroX = (ancho - 1) / 2.0;
		double centroY = (alto - 1) / 2.0;
		double maxDistancia = Math.sqrt(centroX * centroX + centroY * centroY);
		if (maxDistancia == 0)
			maxDistancia = 1;

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				int a = (pixel >> 24) & 0xFF;
				int r = (pixel >> 16) & 0xFF;
				int g = (pixel >> 8) & 0xFF;
				int b = (pixel >> 0) & 0xFF;

				double distancia = Math.sqrt((x - centroX) * (x - centroX) + (y - centroY) * (y - centroY));
				a = clamp((int) (255 * (1.0 - distancia / maxDistancia)));
				resultado.setRGB(x, y, (a << 24) | (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Vidrio esmerilado: toma un vecino aleatorio dentro de un radio para
	 * simular difusion tipo cristal.
	 */
	public static BufferedImage vidrioEsmerilado(BufferedImage img) {
		return vidrioEsmerilado(img, 3);
	}

	public static BufferedImage vidrioEsmerilado(BufferedImage img, int radio) {
		validar(img);
		int r = Math.max(1, radio);
		int w = img.getWidth(), h = img.getHeight();
		BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		java.util.concurrent.ThreadLocalRandom rnd = java.util.concurrent.ThreadLocalRandom.current();

		for (int y = 0; y < h; y++) {
			for (int x = 0; x < w; x++) {
				int nx = Math.max(0, Math.min(w - 1, x + rnd.nextInt(-r, r + 1)));
				int ny = Math.max(0, Math.min(h - 1, y + rnd.nextInt(-r, r + 1)));
				out.setRGB(x, y, img.getRGB(nx, ny));
			}
		}
		return out;
	}


	/**
	 * Genera un degradado horizontal (izquierda a derecha).
	 *
	 * @param ancho  ancho de la imagen
	 * @param alto   alto de la imagen
	 * @param color1 color inicial (izquierda)
	 * @param color2 color final (derecha)
	 */
	public static BufferedImage degradadoHorizontal(int ancho, int alto, Color color1, Color color2) {
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < ancho; x++) {
			float t = (ancho > 1) ? (float) x / (ancho - 1) : 0f;
			int r = clamp((int) (color1.getRed() * (1 - t) + color2.getRed() * t));
			int g = clamp((int) (color1.getGreen() * (1 - t) + color2.getGreen() * t));
			int b = clamp((int) (color1.getBlue() * (1 - t) + color2.getBlue() * t));
			for (int y = 0; y < alto; y++) {
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Genera un degradado vertical (arriba hacia abajo).
	 */
	public static BufferedImage degradadoVertical(int ancho, int alto, Color color1, Color color2) {
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		for (int y = 0; y < alto; y++) {
			float t = (alto > 1) ? (float) y / (alto - 1) : 0f;
			int r = clamp((int) (color1.getRed() * (1 - t) + color2.getRed() * t));
			int g = clamp((int) (color1.getGreen() * (1 - t) + color2.getGreen() * t));
			int b = clamp((int) (color1.getBlue() * (1 - t) + color2.getBlue() * t));
			for (int x = 0; x < ancho; x++) {
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Genera un degradado radial (desde el centro hacia los bordes).
	 */
	public static BufferedImage degradadoRadial(int ancho, int alto, Color colorCentro, Color colorBorde) {
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		double cx = (ancho - 1) / 2.0;
		double cy = (alto - 1) / 2.0;
		double maxD = Math.sqrt(cx * cx + cy * cy);
		if (maxD == 0)
			maxD = 1;

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				double dist = Math.sqrt((x - cx) * (x - cx) + (y - cy) * (y - cy));
				float t = (float) Math.min(1.0, dist / maxD);
				int r = clamp((int) (colorCentro.getRed() * (1 - t) + colorBorde.getRed() * t));
				int g = clamp((int) (colorCentro.getGreen() * (1 - t) + colorBorde.getGreen() * t));
				int b = clamp((int) (colorCentro.getBlue() * (1 - t) + colorBorde.getBlue() * t));
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Gradiente radial superpuesto sobre la imagen. Mezcla la imagen con un
	 * gradiente radial al 50%.
	 */
	public static BufferedImage gradienteRadial(BufferedImage img) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage gradiente = degradadoRadial(ancho, alto, new Color(255, 200, 50), new Color(30, 0, 80));
		BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int p1 = img.getRGB(x, y);
				int p2 = gradiente.getRGB(x, y);
				int r = (((p1 >> 16) & 0xFF) + ((p2 >> 16) & 0xFF)) / 2;
				int g = (((p1 >> 8) & 0xFF) + ((p2 >> 8) & 0xFF)) / 2;
				int b = (((p1 >> 0) & 0xFF) + ((p2 >> 0) & 0xFF)) / 2;
				resultado.setRGB(x, y, (r << 16) | (g << 8) | b);
			}
		}
		return resultado;
	}

	/**
	 * Recorte de bits: extrae solo los bits altos de cada canal. Simula reduccion
	 * de profundidad de color.
	 *
	 * @param img           imagen de entrada
	 * @param bitsARecortar mascara de bits (ej. 0b1111 para 4 bits)
	 */
	public static BufferedImage recorteBits(BufferedImage img, int mascara, boolean escalar) {
	    validar(img);

	    int ancho = img.getWidth();
	    int alto = img.getHeight();
	    BufferedImage resultado = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

	    // 15 = 4 bits, 3 = 2 bits, 1 = 1 bit
	    int niveles = mascara + 1;     // 16, 4, 2
	    int paso = 256 / niveles;      // 16, 64, 128
	    int maxSinEscalar = 256 - paso; // 240, 192, 128

	    for (int y = 0; y < alto; y++) {
	        for (int x = 0; x < ancho; x++) {
	            int pixel = img.getRGB(x, y);
	            Color color = new Color(pixel, true);

	            int a = color.getAlpha();

	            int r = (color.getRed()   / paso) * paso;
	            int g = (color.getGreen() / paso) * paso;
	            int b = (color.getBlue()  / paso) * paso;

	            if (escalar && maxSinEscalar > 0) {
	                r = (r * 255) / maxSinEscalar;
	                g = (g * 255) / maxSinEscalar;
	                b = (b * 255) / maxSinEscalar;
	            }

	            resultado.setRGB(
	                x, y,
	                (a << 24) | (clamp(r) << 16) | (clamp(g) << 8) | clamp(b)
	            );
	        }
	    }

	    return resultado;
	}

	/**
	 * Convierte una BufferedImage a TYPE_INT_RGB (necesario para ConvolveOp).
	 */
	private static BufferedImage toRGB(BufferedImage img) {
		if (img.getType() == BufferedImage.TYPE_INT_RGB)
			return img;
		BufferedImage rgb = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
		rgb.getGraphics().drawImage(img, 0, 0, null);
		return rgb;
	}

	private static BufferedImage escalarImagen(BufferedImage img, int ancho, int alto) {
		BufferedImage escalada = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D graphics = escalada.createGraphics();
		graphics.drawImage(img, 0, 0, ancho, alto, null);
		graphics.dispose();
		return escalada;
	}

	private static void dibujarHistograma(Graphics2D graphics, int[] histograma, Color color,
			float escalaX, float escalaY, int heightHistograma) {
		graphics.setColor(color);
		for (int i = 1; i < histograma.length; i++) {
			int x1 = (int) (escalaX * (i - 1));
			int y1 = heightHistograma - (int) (escalaY * histograma[i - 1]);
			int x2 = (int) (escalaX * i);
			int y2 = heightHistograma - (int) (escalaY * histograma[i]);
			graphics.drawLine(x1, y1, x2, y2);
		}
	}

	private static int maximoValorHistograma(int[] histograma) {
		int maxValor = 0;
		for (int valor : histograma) {
			if (valor > maxValor) {
				maxValor = valor;
			}
		}
		return maxValor;
	}
	
	//nuevo metodo
	public static boolean tieneTransparenciaReal(BufferedImage img) {
	    if (img == null || !img.getColorModel().hasAlpha()) return false;

	    for (int y = 0; y < img.getHeight(); y++) {
	        for (int x = 0; x < img.getWidth(); x++) {
	            int alpha = (img.getRGB(x, y) >> 24) & 0xFF;
	            if (alpha < 255) {
	                return true;
	            }
	        }
	    }
	    return false;
	}

	// ─────────────────────────────────────────────────────────────────────────
	// EXPOSICIÓN GRUPO 9
	// ─────────────────────────────────────────────────────────────────────────

	public static BufferedImage alphaTestGrupo9(BufferedImage img, int umbral) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel = img.getRGB(x, y);
				Color c = new Color(pixel, true);
				int promedio = (c.getRed() + c.getGreen() + c.getBlue()) / 3;

				if (promedio > umbral) {
					salida.setRGB(x, y, pixel);
				} else {
					salida.setRGB(x, y, 0); // transparente
				}
			}
		}
		return salida;
	}

	public static BufferedImage depthTestGrupo9(BufferedImage img1, BufferedImage img2, float z1, float z2) {
		validar(img1);
		validar(img2);
		int ancho = Math.min(img1.getWidth(), img2.getWidth());
		int alto = Math.min(img1.getHeight(), img2.getHeight());
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				if (z2 < z1) {
					salida.setRGB(x, y, img2.getRGB(x, y));
				} else {
					salida.setRGB(x, y, img1.getRGB(x, y));
				}
			}
		}
		return salida;
	}

	public static BufferedImage logicOpGrupo9(BufferedImage img1, BufferedImage img2, String operacion) {
		validar(img1);
		validar(img2);
		int ancho = Math.min(img1.getWidth(), img2.getWidth());
		int alto = Math.min(img1.getHeight(), img2.getHeight());
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				int pixel1 = img1.getRGB(x, y);
				int pixel2 = img2.getRGB(x, y);
				int resultado;
				
				if ("AND".equalsIgnoreCase(operacion)) {
					resultado = pixel1 & pixel2;
				} else if ("OR".equalsIgnoreCase(operacion)) {
					resultado = pixel1 | pixel2;
				} else { // XOR
					resultado = pixel1 ^ pixel2;
				}

				salida.setRGB(x, y, resultado);
			}
		}
		return salida;
	}

	public static BufferedImage stencilTestGrupo9(BufferedImage img, int radio) {
		validar(img);
		int ancho = img.getWidth();
		int alto = img.getHeight();
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		int centroX = ancho / 2;
		int centroY = alto / 2;

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
		return salida;
	}

	public static BufferedImage blendingGrupo9(BufferedImage fondo, BufferedImage frente, float alpha) {
		validar(fondo);
		validar(frente);
		int ancho = Math.min(fondo.getWidth(), frente.getWidth());
		int alto = Math.min(fondo.getHeight(), frente.getHeight());
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_ARGB);

		for (int y = 0; y < alto; y++) {
			for (int x = 0; x < ancho; x++) {
				Color c1 = new Color(fondo.getRGB(x, y), true);
				Color c2 = new Color(frente.getRGB(x, y), true);

				int r = (int) (alpha * c2.getRed() + (1 - alpha) * c1.getRed());
				int g = (int) (alpha * c2.getGreen() + (1 - alpha) * c1.getGreen());
				int b = (int) (alpha * c2.getBlue() + (1 - alpha) * c1.getBlue());
				
				Color mezcla = new Color(r, g, b);
				salida.setRGB(x, y, mezcla.getRGB());
			}
		}
		return salida;
	}

	// =========================================================================
	// GRUPO 2: RASTERIZACIÓN DE CARTELES 3D (Z-BUFFER Y W-BUFFER)
	// =========================================================================

	public static class VertexG2 {
		float x, y, w, r, g, b, u, v;

		public VertexG2(float x, float y, float w, float r, float g, float b, float u, float v) {
			this.x = x; this.y = y; this.w = w;
			this.r = r; this.g = g; this.b = b;
			this.u = u; this.v = v;
		}
	}

	private static float edgeG2(float ax, float ay, float bx, float by, float cx, float cy) {
		return (cx - ax) * (by - ay) - (cy - ay) * (bx - ax);
	}

	private static boolean dentroTrianguloG2(float w0, float w1, float w2, float area) {
		if (area > 0) return w0 >= 0 && w1 >= 0 && w2 >= 0;
		return w0 <= 0 && w1 <= 0 && w2 <= 0;
	}

	private static int mezclarColorG2(int texRGB, float r, float g, float b) {
		int tr = (texRGB >> 16) & 0xFF;
		int tg = (texRGB >> 8) & 0xFF;
		int tb = texRGB & 0xFF;
		int fr = Math.min(255, (int) (tr * r));
		int fg = Math.min(255, (int) (tg * g));
		int fb = Math.min(255, (int) (tb * b));
		return (255 << 24) | (fr << 16) | (fg << 8) | fb;
	}

	private static void rasterizarTrianguloG2(int[] canvasPix, int width, int height, 
											  int[] texPix, int tWidth, int tHeight, float[][] wBuffer,
											  VertexG2 v0, VertexG2 v1, VertexG2 v2,
											  boolean usarTextura, boolean usarColor,
											  boolean perspectivaCorrecta, boolean usarWBuffer) {
		int minX = (int) Math.max(0, Math.min(v0.x, Math.min(v1.x, v2.x)));
		int maxX = (int) Math.min(width - 1, Math.max(v0.x, Math.max(v1.x, v2.x)));
		int minY = (int) Math.max(0, Math.min(v0.y, Math.min(v1.y, v2.y)));
		int maxY = (int) Math.min(height - 1, Math.max(v0.y, Math.max(v1.y, v2.y)));

		float area = edgeG2(v0.x, v0.y, v1.x, v1.y, v2.x, v2.y);
		if (area == 0) return;

		for (int y = minY; y <= maxY; y++) {
			for (int x = minX; x <= maxX; x++) {
				float w0 = edgeG2(v1.x, v1.y, v2.x, v2.y, x, y);
				float w1 = edgeG2(v2.x, v2.y, v0.x, v0.y, x, y);
				float w2 = edgeG2(v0.x, v0.y, v1.x, v1.y, x, y);

				if (!dentroTrianguloG2(w0, w1, w2, area)) continue;

				w0 /= area;
				w1 /= area;
				w2 /= area;

				float invW = (w0 * (1f / v0.w)) + (w1 * (1f / v1.w)) + (w2 * (1f / v2.w));

				if (usarWBuffer) {
					if (invW <= wBuffer[x][y]) continue;
					wBuffer[x][y] = invW;
				}

				float r, g, b, u, v;

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

				int finalRGB;

				if (usarTextura && usarColor) {
					int tx = Math.max(0, Math.min(tWidth - 1, (int) (u * (tWidth - 1))));
					int ty = Math.max(0, Math.min(tHeight - 1, (int) (v * (tHeight - 1))));
					int texColor = texPix[tx + ty * tWidth];
					finalRGB = mezclarColorG2(texColor, r, g, b);
				} else if (usarTextura) {
					int tx = Math.max(0, Math.min(tWidth - 1, (int) (u * (tWidth - 1))));
					int ty = Math.max(0, Math.min(tHeight - 1, (int) (v * (tHeight - 1))));
					finalRGB = texPix[tx + ty * tWidth];
				} else if (usarColor) {
					int fr = Math.min(255, (int) (r * 255));
					int fg = Math.min(255, (int) (g * 255));
					int fb = Math.min(255, (int) (b * 255));
					finalRGB = (255 << 24) | (fr << 16) | (fg << 8) | fb;
				} else {
					finalRGB = 0xFFFFFFFF;
				}

				canvasPix[x + y * width] = finalRGB;
			}
		}
	}

	public static BufferedImage generarRasterizadoGrupo2(int modo, int width, int height, BufferedImage customTex) {
		BufferedImage canvas = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		BufferedImage textura;
		
		if (customTex != null) {
			textura = customTex;
		} else {
			textura = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
			int[] texPix = ((java.awt.image.DataBufferInt) textura.getRaster().getDataBuffer()).getData();
			for (int y = 0; y < textura.getHeight(); y++) {
				for (int x = 0; x < textura.getWidth(); x++) {
					boolean cuadro = ((x / 32) + (y / 32)) % 2 == 0;
					texPix[x + y * 256] = cuadro ? 0xFFFFFFFF : 0xFF000000;
				}
			}
		}
		
		int texWidth = textura.getWidth();
		int texHeight = textura.getHeight();

		float[][] wBuffer = new float[width][height];

		int[] canvasPix = ((java.awt.image.DataBufferInt) canvas.getRaster().getDataBuffer()).getData();
		int[] texPix;
		if (textura.getType() == BufferedImage.TYPE_INT_RGB || textura.getType() == BufferedImage.TYPE_INT_ARGB) {
			texPix = ((java.awt.image.DataBufferInt) textura.getRaster().getDataBuffer()).getData();
		} else {
			texPix = textura.getRGB(0, 0, texWidth, texHeight, null, 0, texWidth);
		}

		for (int i = 0; i < width; i++) {
			java.util.Arrays.fill(wBuffer[i], Float.NEGATIVE_INFINITY);
		}

		Graphics2D g = canvas.createGraphics();
		g.setColor(Color.DARK_GRAY);
		g.fillRect(0, 0, width, height);
		g.setColor(Color.WHITE);
		g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 14));

		switch (modo) {
			case 1 -> { // Textura
				g.drawString("TEXTURA", 20, 30);
				VertexG2 a = new VertexG2(140, 120, 1.0f, 1, 1, 1, 0, 0);
				VertexG2 b = new VertexG2(620, 150, 0.8f, 1, 1, 1, 1, 0);
				VertexG2 c = new VertexG2(600, 470, 0.6f, 1, 1, 1, 1, 1);
				VertexG2 d = new VertexG2(160, 440, 0.9f, 1, 1, 1, 0, 1);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, b, c, true, false, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, c, d, true, false, true, false);
			}
			case 2 -> { // Color
				g.drawString("COLOR INTERPOLADO", 20, 30);
				VertexG2 a = new VertexG2(140, 120, 1.0f, 1, 0, 0, 0, 0);
				VertexG2 b = new VertexG2(620, 150, 0.8f, 0, 1, 0, 1, 0);
				VertexG2 c = new VertexG2(600, 470, 0.6f, 0, 0, 1, 1, 1);
				VertexG2 d = new VertexG2(160, 440, 0.9f, 1, 1, 0, 0, 1);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, b, c, false, true, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, c, d, false, true, true, false);
			}
			case 3 -> { // Profundidad
				g.drawString("INTERPOLACION EN PROFUNDIDAD", 20, 30);
				g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
				g.drawString("Izquierda: sin correccion | Derecha: con correccion 1/W", 20, 50);

				VertexG2 a1 = new VertexG2(70, 120, 1.0f, 1, 1, 1, 0, 0);
				VertexG2 b1 = new VertexG2(320, 150, 0.5f, 1, 1, 1, 1, 0);
				VertexG2 c1 = new VertexG2(300, 460, 0.2f, 1, 1, 1, 1, 1);
				VertexG2 d1 = new VertexG2(80, 430, 0.8f, 1, 1, 1, 0, 1);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a1, b1, c1, true, false, false, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a1, c1, d1, true, false, false, false);

				VertexG2 a2 = new VertexG2(430, 120, 1.0f, 1, 1, 1, 0, 0);
				VertexG2 b2 = new VertexG2(720, 150, 0.5f, 1, 1, 1, 1, 0);
				VertexG2 c2 = new VertexG2(700, 460, 0.2f, 1, 1, 1, 1, 1);
				VertexG2 d2 = new VertexG2(450, 430, 0.8f, 1, 1, 1, 0, 1);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a2, b2, c2, true, false, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a2, c2, d2, true, false, true, false);
			}
			case 4 -> { // W-Buffering
				g.drawString("W-BUFFERING", 20, 30);
				g.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 12));
				g.drawString("Izquierda: sin W-Buffer | Derecha: con W-Buffer", 20, 50);

				VertexG2 a1 = new VertexG2(100, 120, 1.0f, 1, 0, 0, 0, 0);
				VertexG2 b1 = new VertexG2(300, 160, 1.0f, 0, 1, 0, 1, 0);
				VertexG2 c1 = new VertexG2(270, 470, 1.0f, 0, 0, 1, 1, 1);
				VertexG2 d1 = new VertexG2(120, 430, 1.0f, 1, 1, 0, 0, 1);

				VertexG2 a2 = new VertexG2(160, 170, 0.3f, 1, 1, 0, 0, 0);
				VertexG2 b2 = new VertexG2(360, 210, 0.3f, 1, 0, 1, 1, 0);
				VertexG2 c2 = new VertexG2(330, 500, 0.3f, 0, 1, 1, 1, 1);
				VertexG2 d2 = new VertexG2(180, 460, 0.3f, 1, 0.5f, 0, 0, 1);

				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a1, b1, c1, true, true, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a1, c1, d1, true, true, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a2, b2, c2, true, true, true, false);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a2, c2, d2, true, true, true, false);

				VertexG2 a3 = new VertexG2(450, 120, 1.0f, 1, 0, 0, 0, 0);
				VertexG2 b3 = new VertexG2(650, 160, 1.0f, 0, 1, 0, 1, 0);
				VertexG2 c3 = new VertexG2(620, 470, 1.0f, 0, 0, 1, 1, 1);
				VertexG2 d3 = new VertexG2(470, 430, 1.0f, 1, 1, 0, 0, 1);

				VertexG2 a4 = new VertexG2(510, 170, 0.3f, 1, 1, 0, 0, 0);
				VertexG2 b4 = new VertexG2(710, 210, 0.3f, 1, 0, 1, 1, 0);
				VertexG2 c4 = new VertexG2(680, 500, 0.3f, 0, 1, 1, 1, 1);
				VertexG2 d4 = new VertexG2(530, 460, 0.3f, 1, 0.5f, 0, 0, 1);

				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a3, b3, c3, true, true, true, true);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a3, c3, d3, true, true, true, true);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a4, b4, c4, true, true, true, true);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a4, c4, d4, true, true, true, true);
			}
			case 5 -> { // Completo
				g.drawString("COMPLETO", 20, 30);
				VertexG2 a = new VertexG2(140, 120, 1.0f, 1, 0, 0, 0, 0);
				VertexG2 b = new VertexG2(620, 150, 0.8f, 0, 1, 0, 1, 0);
				VertexG2 c = new VertexG2(600, 470, 0.6f, 0, 0, 1, 1, 1);
				VertexG2 d = new VertexG2(160, 440, 0.9f, 1, 1, 0, 0, 1);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, b, c, true, true, true, true);
				rasterizarTrianguloG2(canvasPix, width, height, texPix, texWidth, texHeight, wBuffer, a, c, d, true, true, true, true);
			}
		}
		g.dispose();
		return canvas;
	}

	// =========================================================================
	// GRUPO 8: FRAGMENTOS (STENCIL, BLENDING, XOR)
	// =========================================================================

	public static BufferedImage stencilGrupo8(BufferedImage imagen, int diametroParam) {
		validar(imagen);
		int ancho = imagen.getWidth();
		int alto = imagen.getHeight();

		BufferedImage mascara = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		Graphics2D g = mascara.createGraphics();
		g.setColor(Color.BLACK);
		g.fillRect(0, 0, ancho, alto);
		g.setColor(Color.WHITE);
		int diametro = diametroParam;
		if (diametro <= 0) diametro = Math.min(ancho, alto) / 2;
		g.fillOval((ancho - diametro) / 2, (alto - diametro) / 2, diametro, diametro);
		g.dispose();

		int[] pImagen = imagen.getRGB(0, 0, ancho, alto, null, 0, ancho);
		int[] pMascara = ((java.awt.image.DataBufferInt) mascara.getRaster().getDataBuffer()).getData();
		
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		int[] pSalida = ((java.awt.image.DataBufferInt) salida.getRaster().getDataBuffer()).getData();

		int length = ancho * alto;
		for (int i = 0; i < length; i++) {
			int gris = (pMascara[i] >> 16) & 0xFF; // Es blanco o negro, r=g=b
			if (gris > 128) {
				pSalida[i] = pImagen[i];
			} else {
				pSalida[i] = 0xFF000000; // Negro
			}
		}
		return salida;
	}

	public static BufferedImage blendingGrupo8(BufferedImage fondo, BufferedImage superior, float alpha) {
		validar(fondo);
		validar(superior);
		int ancho = Math.min(fondo.getWidth(), superior.getWidth());
		int alto = Math.min(fondo.getHeight(), superior.getHeight());
		int[] pFondo = fondo.getRGB(0, 0, ancho, alto, null, 0, ancho);
		int[] pSuperior = superior.getRGB(0, 0, ancho, alto, null, 0, ancho);
		
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		int[] pSalida = ((java.awt.image.DataBufferInt) salida.getRaster().getDataBuffer()).getData();

		int length = ancho * alto;
		for (int i = 0; i < length; i++) {
			int p1 = pFondo[i];
			int p2 = pSuperior[i];

			int r1 = (p1 >> 16) & 0xFF;
			int g1 = (p1 >> 8) & 0xFF;
			int b1 = p1 & 0xFF;

			int r2 = (p2 >> 16) & 0xFF;
			int g2 = (p2 >> 8) & 0xFF;
			int b2 = p2 & 0xFF;

			int r = (int) (r2 * alpha + r1 * (1 - alpha));
			int g = (int) (g2 * alpha + g1 * (1 - alpha));
			int b = (int) (b2 * alpha + b1 * (1 - alpha));

			pSalida[i] = (r << 16) | (g << 8) | b;
		}
		return salida;
	}

	public static BufferedImage xorGrupo8(BufferedImage img1, BufferedImage img2) {
		validar(img1);
		validar(img2);
		int ancho = Math.min(img1.getWidth(), img2.getWidth());
		int alto = Math.min(img1.getHeight(), img2.getHeight());
		int[] pImg1 = img1.getRGB(0, 0, ancho, alto, null, 0, ancho);
		int[] pImg2 = img2.getRGB(0, 0, ancho, alto, null, 0, ancho);
		
		BufferedImage salida = new BufferedImage(ancho, alto, BufferedImage.TYPE_INT_RGB);
		int[] pSalida = ((java.awt.image.DataBufferInt) salida.getRaster().getDataBuffer()).getData();

		int length = ancho * alto;
		for (int i = 0; i < length; i++) {
			int p1 = pImg1[i];
			int p2 = pImg2[i];

			int r1 = (p1 >> 16) & 0xFF;
			int g1 = (p1 >> 8) & 0xFF;
			int b1 = p1 & 0xFF;

			int r2 = (p2 >> 16) & 0xFF;
			int g2 = (p2 >> 8) & 0xFF;
			int b2 = p2 & 0xFF;

			int r = r1 ^ r2;
			int g = g1 ^ g2;
			int b = b1 ^ b2;

			pSalida[i] = (r << 16) | (g << 8) | b;
		}
		return salida;
	}
}
