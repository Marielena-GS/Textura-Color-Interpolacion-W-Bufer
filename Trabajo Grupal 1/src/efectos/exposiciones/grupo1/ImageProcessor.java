package efectos.exposiciones.grupo1;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public final class ImageProcessor {

        private ImageProcessor() {
        }

        public record Point3D(double x, double y, double z) {
        }

        public static Point3D translate(Point3D point, double dx, double dy, double dz) {
                return new Point3D(point.x() + dx, point.y() + dy, point.z() + dz);
        }

        public static Point3D rotateX(Point3D point, double angle) {
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double y = point.y() * cos - point.z() * sin;
                double z = point.y() * sin + point.z() * cos;
                return new Point3D(point.x(), y, z);
        }

        public static Point3D rotateY(Point3D point, double angle) {
                double cos = Math.cos(angle);
                double sin = Math.sin(angle);
                double x = point.x() * cos + point.z() * sin;
                double z = -point.x() * sin + point.z() * cos;
                return new Point3D(x, point.y(), z);
        }

        public static Point2D.Double project(Point3D point, int width, int height, double focalLength,
                        double cameraDistance) {
                double base = Math.min(width, height) / 2.0;
                double scale = (focalLength / 360.0) * base / (cameraDistance - point.z());
                double x = width / 2.0 + point.x() * scale;
                double y = height / 2.0 - point.y() * scale;
                return new Point2D.Double(x, y);
        }

        public static Color shade(Color color, double factor) {
                int red = clamp((int) Math.round(color.getRed() * factor));
                int green = clamp((int) Math.round(color.getGreen() * factor));
                int blue = clamp((int) Math.round(color.getBlue() * factor));
                return new Color(red, green, blue);
        }



        private static int clamp(int value) {
                return Math.max(0, Math.min(255, value));
        }
}
