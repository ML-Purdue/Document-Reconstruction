import java.awt.Graphics;
import java.awt.Panel;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

/**
 * Primitive image operations class - translate, rotate, etc
 * 
 * @author mgottein
 * 
 */
public class ImageOperations {
    /**
     * Sample main method to illustrate how it works
     * TODO: add user-specified parameters for image operations
     * 
     * @param args
     */
    public static void main(String[] args) {
        JFileChooser fc = new JFileChooser("Choose image");
        switch (fc.showOpenDialog(null)) {
        case JFileChooser.APPROVE_OPTION:
            JFrame frame = new JFrame("Display image");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            try {
                BufferedImage in = ImageIO.read(fc.getSelectedFile());
                final BufferedImage img = new ImgOpBuilder(in).rotate(Math.PI).translate(-100, -100).filter();
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

    /**
     * Build a image operations - use AffineTransform underneath
     * 
     * @author mgottein
     * 
     */
    public static class ImgOpBuilder {
        private AffineTransform tr;
        private BufferedImage img;

        /**
         * Construct a image operation builder
         * 
         * @param img
         *            image to filter
         */
        public ImgOpBuilder(BufferedImage img) {
            tr = new AffineTransform();
            this.img = img;
        }

        /**
         * Rotate the image
         * 
         * @param theta
         *            radians to rotate the image by
         * @return this image op builder
         */
        public ImgOpBuilder rotate(double theta) {
            tr.rotate(theta, img.getWidth() / 2, img.getHeight() / 2);
            return this;
        }

        /**
         * Translate the image
         * Axes: -x is right, +x is left
         * -y is down, +y is up
         * 
         * @param x
         *            amount to move on the x axis by
         * @param y
         *            amount to move on the y axis by
         * @return this image op builder
         */
        public ImgOpBuilder translate(double x, double y) {
            tr.translate(x, y);
            return this;
        }

        /**
         * Actually filter the image
         * 
         * @return a copy of the image with all operations specified by this builder applied to it
         */
        public BufferedImage filter() {
            return new AffineTransformOp(tr, AffineTransformOp.TYPE_BILINEAR).filter(img, null);
        }

    }
}
