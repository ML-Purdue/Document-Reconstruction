import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public class Piece {
    Point2D.Double position;
    double rotation;
    BufferedImage image;

    public Piece(Point2D.Double pos, double rot, BufferedImage i) {
        position = pos;
        rotation = rot;
        image = i;
    }

    public Piece(Piece piece) {
        position = new Point2D.Double(piece.position.x, piece.position.y);
        rotation = piece.rotation;
        image = piece.image;
    }
}
