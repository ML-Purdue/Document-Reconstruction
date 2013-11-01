import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

public class Utility {
    public static BufferedImage deepCopy(BufferedImage image) {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage addAlphaChannel(BufferedImage originalImage) {
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D newImageGraphics = newImage.createGraphics();
        newImageGraphics.drawImage(originalImage, 0, 0, null);
        newImageGraphics.dispose();
        return newImage;
    }

    public static Point3d colorToPoint3d(Color color) {
        return new Point3d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
    }

    public static Color Point3dToColor(Point3d point) {
        return new Color((int) (255 * point.x), (int) (255 * point.y), (int) (255 * point.z));
    }

    public static boolean pointInHull(Point3d point, QuickHull3D hull) {
        Point3d[] vertices = hull.getVertices();
        int[][] faces = hull.getFaces();
        Triangle[] triangles = new Triangle[faces.length];
        for (int i = 0; i < faces.length; i++) {
            triangles[i] = new Triangle(vertices[faces[i][0]], vertices[faces[i][1]], vertices[faces[i][2]]);
        }

        for (Triangle triangle : triangles) {
            if (triangle.side(point) == Triangle.Side.FRONT) {
                return false;
            }
        }

        return true;
    }

    public static void drawChecker(Graphics graphics, int width, int height, int size, Color colorLeft, Color colorRight) {
        for (int iy = 0; iy * size <= height; iy++) {
            for (int ix = 0; ix * size <= width; ix++) {
                Color color = (iy + ix) % 2 == 0 ? colorLeft : colorRight;
                graphics.setColor(color);
                graphics.fillRect(ix * size, iy * size, size, size);
            }
        }
    }

    public static BufferedImage showAlpha(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int iy = 0; iy < image.getHeight(); iy++) {
            for (int ix = 0; ix < image.getWidth(); ix++) {
                int alpha = new Color(image.getRGB(ix, iy), true).getAlpha();
                newImage.setRGB(ix, iy, new Color(alpha, alpha, alpha).getRGB());
            }
        }

        return newImage;
    }

    public static double clamp(double minimum, double value, double maximum) {
        return Math.min(Math.max(value, minimum), maximum);
    }

    public static BufferedImage contrastAlpha(BufferedImage image, double contrast) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int iy = 0; iy < image.getHeight(); iy++) {
            for (int ix = 0; ix < image.getWidth(); ix++) {
                Color rgb = new Color(image.getRGB(ix, iy), true);
                int r = rgb.getRed();
                int g = rgb.getGreen();
                int b = rgb.getBlue();
                double alpha = new Color(image.getRGB(ix, iy), true).getAlpha() / 255.0;
                double newAlpha = clamp(0, (alpha - 0.5) * (1 + contrast) + 0.5, 1);
                Color newRGB = new Color(r, g, b, (int) (255 * newAlpha));
                newImage.setRGB(ix, iy, newRGB.getRGB());
            }
        }

        return newImage;
    }
}
