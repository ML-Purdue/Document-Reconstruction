import java.awt.Color;
import java.awt.Graphics;
import java.awt.Panel;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class EdgeDetectionFilter {
    public static void main(String[] args) {
        JFileChooser fc = new JFileChooser("Choose image");
        switch (fc.showOpenDialog(null)) {
        case JFileChooser.APPROVE_OPTION:
            JFrame frame = new JFrame("Display image");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                BufferedImage in = ImageIO.read(fc.getSelectedFile());
                final BufferedImage img = getEdgeDetectionFilter(in);
                // final BufferedImage img = detectEdges(in);
                frame.getContentPane().add(new Panel() {
                    @Override
                    public void paint(Graphics g) {
                        g.drawImage(img, 0, 0, img.getWidth(), img.getHeight(), null);
                    }
                });
                frame.setSize(1000, 1000);
                frame.setVisible(true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            break;
        }
    }

    public static BufferedImage getEdgeDetectionFilter(BufferedImage src) {
        BufferedImage dest = new BufferedImage(src.getWidth(), src.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics g = dest.getGraphics();
        g.drawImage(src, 0, 0, null);
        g.dispose();
        for (int x = 0; x < dest.getWidth(); x++) {
            for (int y = 0; y < dest.getHeight(); y++) {
                double total = 0;
                int count = 0;
                Color currentColor = new Color(src.getRGB(x, y));
                for (int i = x - 1; i <= x + 1; i++) {
                    for (int j = y - 1; j <= y + 1; j++) {
                        if (i < 0 || i >= dest.getWidth() || j < 0 || j >= dest.getHeight() || (i == x && j == y)) {
                            continue;
                        } else {
                            count++;
                            Color temp = new Color(src.getRGB(i, j));
                            total += (Math.abs(currentColor.getRed() - temp.getRed()) + Math.abs(currentColor.getBlue() - temp.getBlue()) + Math.abs(currentColor.getGreen() - temp.getGreen())) / 3.0;
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
