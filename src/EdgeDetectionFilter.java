import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

public class EdgeDetectionFilter {
    public static BufferedImage getEdgeDetectionFilter(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = dest.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        for (int x = 0; x < dest.getWidth(); x++) {
            for (int y = 0; y < dest.getHeight(); y++) {
                double total = 0;
                int count = 0;
                Color currentColor = new Color(src.getRGB(x, y), true);
                for (int i = x - 1; i <= x + 1; i++) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i < 0 || i >= dest.getWidth() || j < 0 || j >= dest.getHeight() || (i == x && j == y)) {
                            continue;
                        } else {
                            count++;
                            Color temp = new Color(src.getRGB(i, j), true);
                            total += (Math.abs(currentColor.getRed() - temp.getRed()) + Math.abs(currentColor.getBlue() - temp.getBlue()) + Math.abs(currentColor.getGreen() - temp.getGreen()) + Math.abs(currentColor.getAlpha() - temp.getAlpha())) / 4.0;
                        }
                    }
                }
                dest.setRGB(x, y, getScale((int) (total / count)));
            }
        }

        return dest;
    }

    private static int getScale(int avg) {
        return new Color(avg, avg, avg).getRGB();
    }
}
