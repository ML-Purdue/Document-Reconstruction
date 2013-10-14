import java.awt.Color;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Stack;
import javax.imageio.ImageIO;

public class BlobDetector {
    private BufferedImage baseImage;
    private int[][] blobs;
    public static final int BLOB_THRESHOLD = 0;

    public BlobDetector(BufferedImage bi) {
        baseImage = bi;
        blobs = new int[baseImage.getWidth()][baseImage.getHeight()];
    }

    public void detectBlobs() {
        int blobNum = 0;

        ArrayList<BlobRegion> blobRegions = new ArrayList<BlobRegion>();
        Stack<Point> pixelStack = new Stack<Point>();
        for (int x = 0; x < baseImage.getWidth() - 1; x++) {
            for (int y = 0; y < baseImage.getHeight() - 1; y++) {
                if (blobs[x][y] != 0) {
                    continue;
                }
                if (getAlphaValue(baseImage.getRGB(x, y)) <= BLOB_THRESHOLD) {
                    continue;
                }
                /*
                 * For every pixel not part of a blob Add the pixel to a stack While the stack isn't empty, pop off the pixel, mark it as a blob, add its non-transparent neighbors
                 */
                pixelStack.push(new Point(x, y));

                blobNum++;
                BlobRegion blobRegion = new BlobRegion(blobNum);
                blobRegion.minX = x;
                blobRegion.minY = y;
                blobRegion.maxX = x;
                blobRegion.maxY = y;
                blobRegions.add(blobRegion);
                while (!pixelStack.empty()) {
                    Point currentPixel = pixelStack.pop();
                    // System.out.printf("Popped: %d %d\n", currentPixel.x, currentPixel.y);
                    blobs[currentPixel.x][currentPixel.y] = blobNum;
                    if (currentPixel.x < blobRegion.minX) {
                        blobRegion.minX = currentPixel.x;
                    }
                    if (currentPixel.y < blobRegion.minY) {
                        blobRegion.minY = currentPixel.y;
                    }
                    if (currentPixel.x > blobRegion.maxX) {
                        blobRegion.maxX = currentPixel.x;
                    }
                    if (currentPixel.y > blobRegion.maxY) {
                        blobRegion.maxY = currentPixel.y;
                    }
                    // System.out.println(getAlphaValue(baseImage.getRGB(currentPixel.x, currentPixel.y)));
                    for (int i = currentPixel.x - 1; i <= currentPixel.x + 1; i++) {
                        for (int j = currentPixel.y - 1; j <= currentPixel.y + 1; j++) {
                            // System.out.printf("dsa: %d %d\n", i, j);
                            if (i < 0 || j < 0 || i >= baseImage.getWidth() || j >= baseImage.getHeight()) {
                                continue;
                            }
                            // System.out.println("dsaf");
                            if (blobs[i][j] != 0) {
                                // System.out.println((i == currentPixel.x) + " " + (j == currentPixel.y) + " " + getAlphaValue(baseImage.getRGB(i, j)));
                                continue;
                            }

                            if (getAlphaValue(baseImage.getRGB(i, j)) <= BLOB_THRESHOLD) {
                                continue;
                            }

                            // System.out.printf("Pushing: %d %d\n", i, j);
                            pixelStack.push(new Point(i, j));
                        }
                    }
                    // System.exit(1);
                }

                /*
                 * Iterative Method, creates too many blobs // Check neighbors // If neighbor is not transparent, set to highest neighbor number // If neighbors == transparent, set own blob color int highestNeighbor = 0; for (int i = x - 1; i < x + 1; i++) { for (int j = y - 1; j < y + 1; j++) { if (blobs[i][j] > highestNeighbor) { highestNeighbor = blobs[i][j]; } } } if (highestNeighbor > 0) { blobs[x][y] = highestNeighbor; } else { blobNum++; blobs[x][y] = blobNum; }
                 */

                /*
                 * //Recursive (stack overflow) if (blobs[x][y] != 0) continue; if (getAlphaValue(baseImage.getRGB(x, y)) <= BLOB_THRESHOLD) continue; System.out.println(x + " " + y); fillBlob(x, y, blobNum); blobNum++;
                 */
            }
        }
        System.out.println((blobNum) + " blobs found");
        int[] blobColors = new int[blobNum + 1];
        for (int i = 0; i < blobColors.length; i++) {
            Color randomColor = new Color((int) (Math.random() * 255), (int) (Math.random() * 255), (int) (Math.random() * 255));
            blobColors[i] = randomColor.getRGB();
        }
        System.out.println("Creating outImage");
        BufferedImage outImage = new BufferedImage(baseImage.getWidth(), baseImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        for (int x = 0; x < baseImage.getWidth(); x++) {
            for (int y = 0; y < baseImage.getHeight(); y++) {
                if (blobs[x][y] == 0) {
                    outImage.setRGB(x, y, 0x00FFFFFF);
                } else {
                    outImage.setRGB(x, y, blobColors[blobs[x][y]]);
                }
                /*
                 * if (blobs[x][y] == 1) { System.out.printf("x: %d, y: %d\n", x, y); }
                 */
            }
        }
        System.out.println("Saving Full Image");
        try {
            ImageIO.write(outImage, "png", new File("outImage.png"));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println("Saving Individual Images");

        File directory = new File("blobs");
        if (!directory.exists()) {
            directory.mkdir();
        }
        for (BlobRegion region : blobRegions) {
            BufferedImage regionImage = new BufferedImage(region.maxX - region.minX + 1, region.maxY - region.minY + 1, BufferedImage.TYPE_INT_ARGB);
            for (int x = region.minX; x <= region.maxX; x++) {
                for (int y = region.minY; y <= region.maxY; y++) {
                    if (blobs[x][y] != region.blobNum) {
                        regionImage.setRGB(x - region.minX, y - region.minY, 0x00FFFFFF);
                    } else {
                        regionImage.setRGB(x - region.minX, y - region.minY, baseImage.getRGB(x, y));
                    }
                }
            }
            try {
                ImageIO.write(regionImage, "png", new File("blobs/blob" + region.blobNum + ".png"));
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        System.out.println("Done saving");
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
