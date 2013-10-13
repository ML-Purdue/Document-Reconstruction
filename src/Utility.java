import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public class Utility {
    public static BufferedImage addAlphaChannel(BufferedImage originalImage) {
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D newImageGraphics = newImage.createGraphics();
        newImageGraphics.drawImage(originalImage, 0, 0, null);
        newImageGraphics.dispose();
        return newImage;
    }
}
