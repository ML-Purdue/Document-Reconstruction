import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;

public class AcceptPairView extends JPanel {
    public AcceptPairView(BufferedImage image)
    {
        super();
        setLayout(new GridLayout(2, 1));
        add(new ImagePanel(image));
        add(new JButton());
    }

    class ImagePanel extends JPanel {
        BufferedImage bufferedImage;

        public ImagePanel(BufferedImage image) {
            bufferedImage = image;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(bufferedImage, null, 50, 50);
        }
    }
}
