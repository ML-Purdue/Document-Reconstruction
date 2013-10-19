import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

@SuppressWarnings("serial")
public class BackgroundSubtractionStep extends Step {
    BufferedImage image;

    public BackgroundSubtractionStep(Listener listener) {
        super(listener);
    }

    @Override
    public void begin(Object input) {
        image = (BufferedImage) input;
        setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        // TODO listener.update(this, output);
    }

    @Override
    public void paint(Graphics g) {
        if (image != null) {
            g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
        }
    }
}
