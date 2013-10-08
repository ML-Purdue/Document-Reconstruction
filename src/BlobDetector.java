import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BlobDetector {
    private BufferedImage baseImage;
    private int[][] blobs;
    public static final int BLOB_THRESHOLD = 50;

    public BlobDetector(BufferedImage bi) {
        baseImage = bi;
        blobs = new int[baseImage.getWidth()][baseImage.getHeight()];
    }

    public void detectBlobs() {
        int blobNum = 0;
        // The = 1, < size -1
        // Are for not having to do inbounds checks for each neighbor
        for (int x = 1; x < baseImage.getWidth() - 1; x++) {
            for (int y = 1; y < baseImage.getHeight() - 1; y++) {
                if (blobs[x][y] != 0) {
                    continue;
                }
                if (getAlphaValue(baseImage.getRGB(x, y)) <= BLOB_THRESHOLD) {
                    continue;
                }
                // Check neighbors
                // If neighbor is not transparent, set to highest neighbor number
                // If neighbors == transparent, set own blob color
                int highestNeighbor = 0;
                for (int i = x - 1; i < x + 1; i++) {
                    for (int j = y - 1; j < y + 1; j++) {
                        if (blobs[i][j] > highestNeighbor) {
                            highestNeighbor = blobs[i][j];
                        }
                    }
                }
                if (highestNeighbor > 0) {
                    blobs[x][y] = highestNeighbor;
                } else {
                    blobNum++;
                    blobs[x][y] = blobNum;
                }

                /*
                 * //Recursive (stack overflow) if (blobs[x][y] != 0) continue; if (getAlphaValue(baseImage.getRGB(x, y)) <= BLOB_THRESHOLD) continue; System.out.println(x + " " + y); fillBlob(x, y, blobNum); blobNum++;
                 */
            }
        }
        System.out.println("Creating outImage");
        BufferedImage outImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < baseImage.getWidth(); x++) {
            for (int y = 0; y < baseImage.getHeight(); y++) {
                if (blobs[x][y] == 0) {
                    outImage.setRGB(x, y, 0xFFFFFFFF);
                } else {
                    outImage.setRGB(x, y, 0);
                }
            }
        }
        System.out.println("Saving");
        try {
            ImageIO.write(outImage, "png", new File("outImage.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void fillBlob(int x, int y, int num) {
        if (x < 0 || x >= baseImage.getWidth()) {
            return;
        }
        if (y < 0 || y >= baseImage.getHeight()) {
            return;
        }
        if (getAlphaValue(baseImage.getRGB(x, y)) <= BLOB_THRESHOLD) {
            return;
        }
        if (blobs[x][y] != 0) {
            return;
        }
        System.out.println("b " + x + " " + y);
        blobs[x][y] = num;
        fillBlob(x - 1, y - 1, num);
        fillBlob(x - 1, y, num);
        fillBlob(x - 1, y + 1, num);
        fillBlob(x, y - 1, num);
        fillBlob(x, y + 1, num);
        fillBlob(x + 1, y - 1, num);
        fillBlob(x + 1, y, num);
        fillBlob(x + 1, y + 1, num);
    }

    public int getAlphaValue(int pixel) {
        return (pixel >> 24) & 0xFF;
    }

    public static void main(String[] args) {
        BufferedImage thresholded = null;
        try {
            thresholded = ImageIO.read(new File("thresholded2.png"));
            BlobDetector blobber = new BlobDetector(thresholded);
            blobber.detectBlobs();
        } catch (Exception e) {
            System.out.println(e);
            e.printStackTrace();
        }
    }
}
