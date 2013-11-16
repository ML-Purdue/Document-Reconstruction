import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

public class Utility {
    public static BufferedImage deepCopy(BufferedImage image) {
        ColorModel colorModel = image.getColorModel();
        boolean isAlphaPremultiplied = colorModel.isAlphaPremultiplied();
        WritableRaster raster = image.copyData(null);
        return new BufferedImage(colorModel, raster, isAlphaPremultiplied, null);
    }

    public static BufferedImage addAlphaChannel(BufferedImage originalImage) {
        BufferedImage newImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D newImageGraphics = newImage.createGraphics();
        newImageGraphics.drawImage(originalImage, 0, 0, null);
        newImageGraphics.dispose();
        return newImage;
    }

    public static Point3d colorToPoint3d(Color color) {
        return new Point3d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
    }

    public static Color Point3dToColor(Point3d point) {
        return new Color((int) (255 * point.x), (int) (255 * point.y), (int) (255 * point.z));
    }

    public static boolean pointInHull(Point3d point, QuickHull3D hull) {
        Point3d[] vertices = hull.getVertices();
        int[][] faces = hull.getFaces();
        Triangle[] triangles = new Triangle[faces.length];
        for (int i = 0; i < faces.length; i++) {
            triangles[i] = new Triangle(vertices[faces[i][0]], vertices[faces[i][1]], vertices[faces[i][2]]);
        }

        for (Triangle triangle : triangles) {
            if (triangle.side(point) == Triangle.Side.FRONT) {
                return false;
            }
        }

        return true;
    }

    public static void drawChecker(Graphics graphics, int width, int height, int size, Color colorLeft, Color colorRight) {
        for (int iy = 0; iy * size <= height; iy++) {
            for (int ix = 0; ix * size <= width; ix++) {
                Color color = (iy + ix) % 2 == 0 ? colorLeft : colorRight;
                graphics.setColor(color);
                graphics.fillRect(ix * size, iy * size, size, size);
            }
        }
    }

    public static BufferedImage showAlpha(BufferedImage image) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int iy = 0; iy < image.getHeight(); iy++) {
            for (int ix = 0; ix < image.getWidth(); ix++) {
                int alpha = new Color(image.getRGB(ix, iy), true).getAlpha();
                newImage.setRGB(ix, iy, new Color(alpha, alpha, alpha).getRGB());
            }
        }

        return newImage;
    }

    public static double clamp(double minimum, double value, double maximum) {
        return Math.min(Math.max(value, minimum), maximum);
    }

    public static BufferedImage contrastAlpha(BufferedImage image, double contrast) {
        BufferedImage newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);

        for (int iy = 0; iy < image.getHeight(); iy++) {
            for (int ix = 0; ix < image.getWidth(); ix++) {
                Color rgb = new Color(image.getRGB(ix, iy), true);
                int r = rgb.getRed();
                int g = rgb.getGreen();
                int b = rgb.getBlue();
                double alpha = new Color(image.getRGB(ix, iy), true).getAlpha() / 255.0;
                double newAlpha = clamp(0, (alpha - 0.5) * (1 + contrast) + 0.5, 1);
                Color newRGB = new Color(r, g, b, (int) (255 * newAlpha));
                newImage.setRGB(ix, iy, newRGB.getRGB());
            }
        }

        return newImage;
    }

    public static int getSumOfPixels(BufferedImage image) {
        int sum = 0;
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                int rgb = image.getRGB(x, y);
                int red = (rgb >> 16) & 0xFF;
                sum += red;
            }
        }
        return sum;
    }

    public static void drawLayout(List<Piece> layout, BufferedImage sandbox) {
        // Graphics g = sandbox.getGraphics();
        // for (Piece piece : layout) {
        // g.drawImage(new ImageOperations.ImgOpBuilder(piece.image).rotate(piece.rotation).filter(), (int) piece.position.x, (int) piece.position.y, null);
        // }
        Graphics2D g = (Graphics2D) sandbox.getGraphics();
        for (Piece piece : layout) {
            g.rotate(-piece.rotation);
            g.drawImage(piece.image, (int) piece.position.x, (int) piece.position.y, null);
            g.rotate(piece.rotation);
        }
    }

    /**
     * Returns the distance from point (x, y) to line y=mx + b
     * 
     * @param x
     * @param y
     * @param m
     * @param b
     * @return
     */
    public static double distancePointToLine(double x, double y, double m, double b) {
        // Formula from http://en.wikipedia.org/wiki/Distance_from_a_point_to_a_line
        double part1 = Math.pow((x + m * y - m * b) / (m * m + 1) - x, 2);
        double part2 = Math.pow((m * (x + m * y - m * b) / (m * m + 1)) + b - y, 2);
        double result = Math.sqrt(part1 + part2);
        return result;
    }

    public static List<Piece> detectBlobs(BufferedImage baseImage, int blob_threshold) {
        int blobNum = 0;

        int[][] blobs = new int[baseImage.getWidth()][baseImage.getHeight()];

        ArrayList<BlobRegion> blobRegions = new ArrayList<BlobRegion>();
        Stack<Point> pixelStack = new Stack<Point>();
        for (int x = 0; x < baseImage.getWidth() - 1; x++) {
            for (int y = 0; y < baseImage.getHeight() - 1; y++) {
                if (blobs[x][y] != 0) {
                    continue;
                }
                if (getAlphaValue(baseImage.getRGB(x, y)) <= blob_threshold) {
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

                            if (getAlphaValue(baseImage.getRGB(i, j)) <= blob_threshold) {
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
        ArrayList<Piece> blobList = new ArrayList<Piece>();
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
            Point2D.Double pos = new Point2D.Double(region.minX + (regionImage.getWidth() / 2.0), region.minY + (regionImage.getWidth() / 2.0));
            Piece p = new Piece(pos, 0, regionImage);
            blobList.add(p);
        }

        return blobList;
    }

    public static int getAlphaValue(int pixel) {
        return (pixel >> 24) & 0xFF;
    }

    public double[] lineOfBestFit(List<Point2D.Double> points) {
        // a double array of 2 elements is returned, where the first element is the slope, and the second is the y-intercept
        double sumX = 0.0;
        double sumY = 0.0;
        double sumProd = 0.0;
        double sumSquareX = 0.0;
        double slopeTop = 0.0;
        double slopeBottom = 0.0;
        int n = points.size();
        for (int i = 0; i < n; i++) {
            sumX += points.get(i).getX();
            sumY += points.get(i).getY();
            sumSquareX += points.get(i).getX() * points.get(i).getX();
            sumProd += points.get(i).getX() * points.get(i).getY();
        }
        slopeTop = (n * sumProd) - (sumX * sumY);
        slopeBottom = (n * sumSquareX) - (sumX * sumX);
        double slope = slopeTop / slopeBottom;
        double[] array = { slope, sumY - (slope * sumX) };
        return array;

    }
}
