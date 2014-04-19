import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import javax.swing.JButton;
import javax.swing.JPanel;

public class AcceptPairView extends JPanel implements ActionListener {
    int index;
    AcceptPairStep asdf;

    public AcceptPairView(BufferedImage image, int index, AcceptPairStep asdf)
    {
        super();
        setLayout(new GridLayout(2, 1));
        add(new ImagePanel(image));
        JButton butt = new JButton();
        butt.addActionListener(this);
        butt.setText("Accept!");
        add(butt);
        this.index = index;
        this.asdf = asdf;
    }

    class ImagePanel extends JPanel {
        BufferedImage bufferedImage;

        public ImagePanel(BufferedImage image) {
            this.setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
            bufferedImage = image;
        }

        public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.drawImage(bufferedImage, null, 50, 50);
        }
    }

    public void actionPerformed(ActionEvent arg0) {
        asdf.accepted(index);
    }
}
