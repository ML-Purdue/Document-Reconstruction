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
        image = Utility.deepCopy(piece.image);
    }

    public Piece() {
    }

    public boolean equals(Object o) {
        if (!(o instanceof Piece)) {
            return false;
        }
        Piece other = (Piece) o;
        if (!other.position.equals(position)) {
            return false;
        }
        if (other.rotation != rotation) {
            return false;
        }
        return true;
    }

    public String toString() {
        return String.format("[Piece: position %s rotation %s", position, rotation);
    }
}
