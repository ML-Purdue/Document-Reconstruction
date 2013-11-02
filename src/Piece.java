import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Piece extends BufferedImage {
    Point2D.Double position;
    double rotation;

    public Piece(int width, int height, int imageType) {
        super(width, height, imageType);

        position = new Point2D.Double(0, 0);
        rotation = 0;
    }
}
