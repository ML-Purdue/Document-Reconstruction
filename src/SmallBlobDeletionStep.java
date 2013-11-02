import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class SmallBlobDeletionStep extends Step {
    public static final double SMALL_BLOB_PERCENTAGE = .005;

    public SmallBlobDeletionStep(Listener listener) {
        super(listener);
    }

    public void begin(Object input) {
        ArrayList<Piece> blobs = (ArrayList<Piece>) input;
        // Get the total "rough" area of the image
        int totalBlobArea = getTotalBlobArea(blobs);
        for (int i = 0; i < blobs.size(); i++) {
            BufferedImage image = blobs.get(i).image;
            // If the rough area is less than the percentage of total area, remove the blob
            if (image.getWidth() * image.getHeight() < SMALL_BLOB_PERCENTAGE * totalBlobArea) {
                blobs.remove(i);
                // To prevent skipping a blob after removal
                i--;
            }
        }
        System.out.println("Total Blobs after removing: " + blobs.size());
        listener.update(this, blobs);
    }

    private int getTotalBlobArea(ArrayList<Piece> blobs) {
        int area = 0;
        for (Piece piece : blobs) {
            BufferedImage image = piece.image;
            area += image.getHeight() * image.getWidth();
        }
        return area;
    }
}
