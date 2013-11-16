import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class BlobDetectionStep extends Step {
    private BufferedImage baseImage;
    // private BufferedImage processedImage;
    // private BufferedImage displayImage;
    private int[][] blobs;
    public static final int BLOB_THRESHOLD = 0;
    BufferedImage processedImage;

    public BlobDetectionStep(Listener listener) {
        super(listener);

        setFocusable(true);

    }

    public void begin(Object input) {
        baseImage = (BufferedImage) input;
        baseImage = Utility.addAlphaChannel(baseImage);
        setPreferredSize(new Dimension(baseImage.getWidth(), baseImage.getHeight()));
        blobs = new int[baseImage.getWidth()][baseImage.getHeight()];
        ArrayList<Piece> output = (ArrayList<Piece>) Utility.detectBlobs(baseImage, 0);
        System.out.println("finished");
        System.out.println("Total Blobs Found: " + output.size());
        listener.update(this, output);
    }

    @Override
    public void paint(Graphics g) {
        Utility.drawChecker(g, getWidth(), getHeight(), 10, Color.LIGHT_GRAY, Color.DARK_GRAY);
        if (processedImage != null) {
            g.drawImage(processedImage, 0, 0, processedImage.getWidth(), processedImage.getHeight(), null);
        }
    }
}
