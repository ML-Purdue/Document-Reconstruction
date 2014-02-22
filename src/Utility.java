import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import javax.swing.JFrame;
import javax.swing.JPanel;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

public class Utility {
    static int nextFrameX = 0;
    static int nextFrameY = 0;
    static int maxRowHeight = 0;

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

    /**
     * Used for the shuffle solver
     * Assumes that all the pixel subvalues (rgba) are the same, hence just using red is ok
     * @param image
     * @return
     */
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

    public static void drawPiece(Piece piece, BufferedImage sandbox) {
        Graphics2D g = (Graphics2D) sandbox.getGraphics();
        g.translate(piece.position.x - piece.image.getWidth() / 2, piece.position.y - piece.image.getHeight() / 2);
        g.rotate(piece.rotation, piece.image.getWidth() / 2, piece.image.getHeight() / 2);

        g.drawImage(piece.image, 0, 0, null);
    }

    public static void drawLayout(List<Piece> layout, BufferedImage sandbox) {
        // Graphics g = sandbox.getGraphics();
        // for (Piece piece : layout) {
        // g.drawImage(new ImageOperations.ImgOpBuilder(piece.image).rotate(piece.rotation).filter(), (int) piece.position.x, (int) piece.position.y, null);
        // }
        Graphics2D g = (Graphics2D) sandbox.getGraphics();
        for (Piece piece : layout) {
            g.rotate(-piece.rotation);
            g.drawImage(piece.image, (int) piece.position.x - piece.image.getWidth() / 2, (int) piece.position.y - piece.image.getHeight() / 2, null);
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

    public static boolean[][] getLargestBlob(BufferedImage baseImage, int blob_threshold) {
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
                 * For every pixel not part of a blob Add the pixel to a stack
                 * While the stack isn't empty, pop off the pixel, mark it as a blob, add its non-transparent neighbors
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
                    for (int i = currentPixel.x - 1; i <= currentPixel.x + 1; i++) {
                        for (int j = currentPixel.y - 1; j <= currentPixel.y + 1; j++) {
                            if (i < 0 || j < 0 || i >= baseImage.getWidth() || j >= baseImage.getHeight()) {
                                continue;
                            }
                            if (blobs[i][j] != 0) {
                                continue;
                            }

                            if (getAlphaValue(baseImage.getRGB(i, j)) <= blob_threshold) {
                                continue;
                            }

                            pixelStack.push(new Point(i, j));
                        }
                    }
                }
            }
        }

        BlobRegion largestBlobRegion = blobRegions.get(0);
        for (BlobRegion blobRegion : blobRegions) {
            int largestArea = (largestBlobRegion.maxX - largestBlobRegion.minX) * (largestBlobRegion.maxY - largestBlobRegion.minY);
            int thisArea = (blobRegion.maxX - blobRegion.minX) * (blobRegion.maxY - blobRegion.minY);
            if (thisArea < largestArea) {
                largestBlobRegion = blobRegion;
            }
        }

        boolean[][] blob = new boolean[baseImage.getWidth()][baseImage.getHeight()];
        for (int x = largestBlobRegion.minX; x <= largestBlobRegion.maxX; x++) {
            for (int y = largestBlobRegion.minY; y <= largestBlobRegion.maxY; y++) {
                blob[x][y] = blobs[x][y] == largestBlobRegion.blobNum;
            }
        }

        return blob;
    }

    public static void show(BufferedImage image) {
        JFrame frame = new JFrame();
        frame.add(new ImagePanel(deepCopy(image)));
        frame.pack();
        frame.setLocation(nextFrameX, nextFrameY);
        maxRowHeight = Math.max(maxRowHeight, frame.getLocation().y + frame.getHeight() + 30);
        nextFrameX += image.getWidth() + 10;
        if (nextFrameX > Toolkit.getDefaultToolkit().getScreenSize().width) {
            nextFrameX = 0;
            nextFrameY += maxRowHeight;
            maxRowHeight = 0;
            if (nextFrameY > Toolkit.getDefaultToolkit().getScreenSize().height) {
                nextFrameY = 0;
            }
        }
        frame.setVisible(true);
    }

    public static class ImagePanel extends JPanel {
        private BufferedImage image;

        public ImagePanel(BufferedImage image) {
            this.image = image;
            setPreferredSize(new Dimension(image.getWidth(), image.getHeight()));
        }

        public void paint(Graphics g) {
            g.drawImage(image, 0, 0, null);
        }
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
                 * For every pixel not part of a blob Add the pixel to a stack
                 * While the stack isn't empty, pop off the pixel, mark it as a blob, add its non-transparent neighbors
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
                    for (int i = currentPixel.x - 1; i <= currentPixel.x + 1; i++) {
                        for (int j = currentPixel.y - 1; j <= currentPixel.y + 1; j++) {
                            if (i < 0 || j < 0 || i >= baseImage.getWidth() || j >= baseImage.getHeight()) {
                                continue;
                            }
                            if (blobs[i][j] != 0) {
                                continue;
                            }

                            if (getAlphaValue(baseImage.getRGB(i, j)) <= blob_threshold) {
                                continue;
                            }

                            pixelStack.push(new Point(i, j));
                        }
                    }
                }
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
            Point2D.Double pos = new Point2D.Double(region.minX + (regionImage.getWidth() / 2.0), region.minY + (regionImage.getHeight() / 2.0));
            Piece p = new Piece(pos, 0, regionImage);
            blobList.add(p);
        }

        return blobList;
    }

    public static int getAlphaValue(int pixel) {
        return (pixel >> 24) & 0xFF;
    }

    public static double[] lineOfBestFit(List<Point2D.Double> points) {
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

    public static List<Point> perimeter(boolean[][] blob) {
        List<Point> l = new ArrayList<Point>();
        outerloop: for (int y = 0; y < blob[0].length; y++) {
            for (int x = 0; x < blob.length; x++) {
                // first element you run into that is part of the blob must be on the perimeter
                if (blob[x][y] == true) {
                    l.add(new Point(x, y));
                    break outerloop;
                }
            }
        }

        Point newP = null;
        Point curP = l.get(0);
        int x = (int) curP.x;
        int y = (int) curP.y;
        int dir = 2; // 0 for up, 1 for up and right, 2 for right, 3 for down and right, 4 for down, 5 for down and left, 6 for left, 7 for up and left
        while (true) {
            if (newP != null) {
                if (newP.getX() == l.get(0).getX() && newP.getY() == l.get(0).getY()) {
                    break;
                }
                l.add(newP);
                curP = newP;
                x = (int) curP.getX();
                y = (int) curP.getY();
                dir = ((dir + 4) % 8) + 1;
                newP = null;
            }
            switch (dir) {
            // for each case, make sure the current position is not on the edge of the image.
            // If it isn't, check the new element in the current direction
            case 0:
                if (y > 0 && isPerimeter(x, y - 1, blob)) {
                    newP = new Point(x, y - 1);
                }
                break;

            case 1:
                if (x < blob.length - 1 && y > 0 && isPerimeter(x + 1, y - 1, blob)) {
                    newP = new Point(x + 1, y - 1);
                }
                break;

            case 2:
                if (x < blob.length - 1 && isPerimeter(x + 1, y, blob)) {
                    newP = new Point(x + 1, y);
                }
                break;

            case 3:
                if (x < blob.length - 1 && y < blob[0].length - 1 && isPerimeter(x + 1, y + 1, blob)) {
                    newP = new Point(x + 1, y + 1);
                }
                break;

            case 4:
                if (y < blob[0].length - 1 && isPerimeter(x, y + 1, blob)) {
                    newP = new Point(x, y + 1);
                }
                break;

            case 5:
                if (x > 0 && y < blob[0].length - 1 && isPerimeter(x - 1, y + 1, blob)) {
                    newP = new Point(x - 1, y + 1);
                }
                break;

            case 6:
                if (x > 0 && isPerimeter(x - 1, y, blob)) {
                    newP = new Point(x - 1, y);
                }
                break;

            case 7:
                if (x > 0 && y > 0 && isPerimeter(x - 1, y - 1, blob)) {
                    newP = new Point(x - 1, y - 1);
                }
                break;

            default:
                break;
            }

            // update the direction if you don't have a new piece to add to the perimeter
            if (newP == null) {
                dir++;
                dir = dir % 8;
            }
        }

        return l;
    }

    public static List<Point2D.Double> awesomePerimeter(boolean[][] blob) {
        boolean[][] edges = new boolean[blob.length + 1][blob[0].length + 1];
        for (int i = 0; i < blob.length; i++) {
            for (int j = 0; j < blob[0].length; j++) {
                if (blob[i][j]) {
                    edges[i][j] = true;
                    edges[i + 1][j] = true;
                    edges[i + 1][j + 1] = true;
                    edges[i][j + 1] = true;
                }
            }
        }
        List<Point> l = perimeter(edges);
        List<Point2D.Double> points = new ArrayList<Point2D.Double>();
        for (Point p : l) {
            Point2D.Double point = new Point2D.Double(p.x - .5, p.y - .5);
            points.add(point);
        }
        return points;
    }

    static boolean isPerimeter(int i, int j, boolean[][] blob) {
        // if the blob has a false value up, down, left, or right from the element, then it is part of the perimeter
        if (i >= blob.length || j >= blob[0].length) {
            return false;
        }
        if (blob[i][j] == true) {
            if (i == 0 || i == blob.length - 1 || j == 0 || j == blob[0].length - 1) {
                return true;
            }
            if (i > 0 && blob[i - 1][j] == false) {
                return true;
            }
            if (i < blob.length - 1 && blob[i + 1][j] == false) {
                return true;
            }
            if (j > 0 && blob[i][j - 1] == false) {
                return true;
            }
            if (j < blob[0].length - 1 && blob[i][j + 1] == false) {
                return true;
            }
        }
        return false;

    }

    public static List<Double> getCurvature(List<Point2D.Double> perimeter) {
        CircularArrayList<Double> curvature = new CircularArrayList<Double>();

        for (int i = 0; i < perimeter.size(); i++) {
            Point2D.Double a = perimeter.get(mod(i - 1, perimeter.size()));
            Point2D.Double p = perimeter.get(i);
            Point2D.Double b = perimeter.get((i + 1) % perimeter.size());
            Point2D.Double ab = new Point2D.Double(b.x - a.x, b.y - a.y);
            Point2D.Double ap = new Point2D.Double(p.x - a.x, p.y - a.y);
            double ab_mag = Math.sqrt(ab.x * ab.x + ab.y * ab.y);
            double ap_mag = Math.sqrt(ap.x * ap.x + ap.y * ap.y);
            if (a.equals(b)) {
                curvature.add(ap_mag);
                continue;
            }
            Point2D.Double ab_hat = new Point2D.Double(ab.x / ab_mag, ab.y / ab_mag);
            Point2D.Double ap_hat = new Point2D.Double(ap.x / ap_mag, ap.y / ap_mag);
            double angle = Math.acos(ab_hat.x * ap_hat.x + ab_hat.y * ap_hat.y);
            double opposite = Math.sin(angle) * ap_mag;
            if (ab_hat.x * ap_hat.y - ab_hat.y * ap_hat.x < 0) {
                opposite = -opposite;
            }

            curvature.add(opposite);
        }

        return curvature;
    }

    public static double getAreaOfPiece(BufferedImage image) {
        double area = 0;
        for (int i = 0; i < image.getWidth(); i++) {
            for (int j = 0; j < image.getHeight(); j++) {
                area += getAlphaValue(image.getRGB(i, j));
            }
        }
        return area;
    }

    public static int mod(int a, int n) {
        return a < 0 ? (a % n + n) % n : a % n;
    }

    public static List<Double> smooth(List<Double> unsmoothList, int tail) {
        List<Double> smoothList = new ArrayList<Double>();
        for (int i = 0; i < unsmoothList.size(); i++) {
            double sum = 0;
            for (int j = -tail; j <= tail; j++) {
                sum += (double) (tail - Math.abs(j)) / (tail * tail) * unsmoothList.get(mod(i + j, unsmoothList.size()));
            }
            smoothList.add(sum);
        }
        return smoothList;
    }

    public static List<Double> integrate(List<Double> list) {
        List<Double> integral = new ArrayList<Double>();
        double value = 0;
        for (Double d : list) {
            value += d;
            integral.add(value);
        }
        return integral;
    }

    public static <T> List<T> shift(List<T> list, int offset) {
        List<T> newList = new ArrayList<T>();
        newList.addAll(list.subList((list.size() + offset) % list.size(), list.size()));
        newList.addAll(list.subList(0, (list.size() + offset) % list.size()));
        return newList;
    }

    public static <T> List<T> reverse(List<T> list) {
        List<T> t = new ArrayList<T>(list);
        list = new ArrayList<T>();
        for (int i = t.size() - 1; i >= 0; i--) {
            list.add(t.get(i));
        }
        return list;
    }

    public static List<Double> negate(List<Double> list) {
        List<Double> t = new ArrayList<Double>(list);
        list = new ArrayList<Double>();
        for (int i = 0; i < t.size(); i++) {
            list.add(-t.get(i));
        }
        return list;
    }

    public static double sum(List<Double> list) {
        double sum = 0;
        for (Double d : list) {
            sum += d;
        }
        return sum;
    }

    public static double maximum(List<Double> list) {
        double value = Double.NEGATIVE_INFINITY;
        for (Double d : list) {
            value = Math.max(value, d);
        }
        return value;
    }

    public static double minimum(List<Double> list) {
        double value = Double.POSITIVE_INFINITY;
        for (Double d : list) {
            value = Math.min(value, d);
        }
        return value;
    }

    public static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    public static double unlerp(double a, double b, double v) {
        return (v - a) / (b - a);
    }

    public static BufferedImage plot(List<Double> values, int height) {
        BufferedImage image = new BufferedImage(values.size(), height, BufferedImage.TYPE_INT_ARGB);
        double max = maximum(values);
        double min = minimum(values);
        Graphics g = image.getGraphics();
        g.setColor(Color.BLACK);
        for (int x = 0; x < values.size() - 1; x++) {
            g.drawLine(x, (int) (lerp(height - 1, 0, unlerp(min, max, values.get(x)))), x + 1, (int) (lerp(height - 1, 0, unlerp(min, max, values.get(x + 1)))));
        }
        g.drawString(String.format("min %f max %f", min, max), 5, 12);
        g.setColor(Color.RED);
        int xAxis = (int) lerp(height - 1, 0, unlerp(min, max, 0));
        g.drawLine(0, xAxis, values.size() - 1, xAxis);
        return image;
    }

    public static MatchInfo matchCurvaturesAndColors(List<Double> curveA, List<Double> curveB, List<Color> colorA, List<Color> colorB) {
        MatchInfo match = new MatchInfo();

        curveB = negate(reverse(curveB));
        colorB = reverse(colorB);

        match.error = Double.POSITIVE_INFINITY;
        int tenPercent = (int) (0.1 * curveA.size());
        for (int a = 0; a < curveA.size(); a++) {
            //Print out % done
            if (a % tenPercent == 0) {
                System.out.println(Math.round((10.0 * a) / curveA.size()) * 10 + "%");
            }
            List<Double> shiftedCurveA = shift(curveA, a);
            List<Color> shiftedColorA = shift(colorA, a);
            for (int b = 0; b < curveB.size(); b++) {
                List<Double> shiftedCurveB = shift(curveB, b);
                List<Color> shiftedColorB = shift(colorB, b);
                int m = Math.min(curveA.size(), curveB.size());
                shiftedCurveA = shiftedCurveA.subList(0, m);
                shiftedCurveB = shiftedCurveB.subList(0, m);
                shiftedColorA = shiftedColorA.subList(0, m);
                shiftedColorB = shiftedColorB.subList(0, m);
                List<Double> aI = integrate(shiftedCurveA);
                List<Double> bI = integrate(shiftedCurveB);
                List<Double> eI = new ArrayList<Double>();
                for (int i = 0; i < m; i++) {
                    double e = 0;
                    e += Math.abs(bI.get(i) - aI.get(i));
                    e += 0.3 * colorToPoint3d(shiftedColorA.get(i)).distance(colorToPoint3d(shiftedColorB.get(i))) / Math.sqrt(3);

                    if (i % 10 == 0) {
                        //                        System.out.println(shiftedColorA.get(i) + " " + shiftedColorB.get(i));
                        //                        System.out.printf("curve error: %.2f\n", Math.pow(Math.abs(bI.get(i) - aI.get(i)), 2) / (Math.sqrt(2) / 2) / (i + 1));
                        //                        System.out.printf("color error: %.2f\n", colorToPoint3d(shiftedColorA.get(i)).distance(colorToPoint3d(shiftedColorB.get(i))) / Math.sqrt(3));
                    }
                    eI.add(e);
                }
                List<Double> cEI = integrate(eI);

                for (int i = 0; i < cEI.size(); i++) {
                    double error = (cEI.get(i) + 4) / (i + 1);
                    if (error < match.error || error == match.error && i > match.length) {
                        match.indexA = a;
                        match.indexB = curveB.size() - b - i;
                        match.length = i;
                        match.error = error;
                    }
                }
            }
        }

        return match;
    }

    public static List<Vector2D> listPoint2DToVector2D(List<Point2D.Double> l) {
        List<Vector2D> lp = new ArrayList<Vector2D>();
        for (Point2D.Double p : l) {
            lp.add(new Vector2D(p));
        }
        return lp;
    }

    public static List<Point2D.Double> listVector2DToPoint2D(List<Vector2D> l) {
        List<Point2D.Double> lp = new ArrayList<Point2D.Double>();
        for (Vector2D p : l) {
            lp.add(p);
        }
        return lp;
    }

    public static class MatchInfo {
        int indexA;
        int indexB;
        int length;
        double error;
    }

    public static int getAvgColor(BufferedImage img, java.awt.geom.Point2D.Double p, int n) {
        int avgA = 0, avgR = 0, avgG = 0, avgB = 0, w = img.getWidth(), h = img.getHeight(), cx = (int) p.x, cy = (int) p.y;
        double weight = 0;
        for (int i = -n / 2; i < n / 2; ++i) {
            for (int j = -n / 2; j < n / 2; ++j) {
                int x = cx + (i - 1), y = cy + (j - 1);
                if (x >= 0 && x < w
                        && y >= 0 && y < h) {
                    Color curColor = new Color(img.getRGB(x, y), true);
                    double aWeight = ((double) curColor.getAlpha()) / 255;
                    weight += aWeight;
                    avgA += curColor.getAlpha();
                    avgR += aWeight * curColor.getRed();
                    avgG += aWeight * curColor.getGreen();
                    avgB += aWeight * curColor.getBlue();
                }
            }
        }
        if (weight != 0) {
            return new Color((int) (avgR / weight), (int) (avgG / weight), (int) (avgB / weight), (int) (avgA / weight)).getRGB();
        }
        else {
            return new Color(0, 0, 0, 0).getRGB();
        }
    }

    public static List<Color> getEdgeColors(BufferedImage image, List<java.awt.geom.Point2D.Double> perimeter) {
        List<Color> colors = new ArrayList<Color>();
        for (Point2D.Double p : perimeter) {
            colors.add(new Color(getAvgColor(image, p, 15)));
        }
        return colors;
    }

    public static double shoeLace(List<java.awt.geom.Point2D.Double> a, List<java.awt.geom.Point2D.Double> b) {
        double total = 0.0;
        if (a.size() != b.size()) {
            return -1.0;
        }
        for (int i = 0; i < a.size(); i++) {
            total += a.get(i).distance(b.get(i));
        }
        return total;
    }

    public static <T> List<T> cyclicSubList(int from, int to, List<T> l) {
        List<T> n = new ArrayList<T>();
        for (int i = from; i != to; i = (i + 1) % l.size()) {
            n.add(l.get(i));
        }
        return n;
    }

    public static class DistanceAngle {
        public Vector2D delta;
        public double angle;
    }

    public static DistanceAngle calculateDistanceAngle(Point2D.Double s1, Point2D.Double e1, Point2D.Double s2, Point2D.Double e2, Piece p1, Piece p2) {
        Vector2D ipA = new Vector2D(s1);
        Vector2D ipB = new Vector2D(e1);
        Vector2D is2 = new Vector2D(p1.image.getWidth() / 2, p1.image.getHeight() / 2);
        Vector2D ip = new Vector2D(p1.position);
        //actual position of ipA (start point) in the sandbox
        Vector2D islandA = ip.subtract(is2).add(ipA);
        //distance between the start and end point
        Vector2D islandAB = ipB.subtract(ipA);

        //This flip is due to the traversal of the lists in opposite directions, maybe
        Vector2D mpB = new Vector2D(s2);
        Vector2D mpA = new Vector2D(e2);
        Vector2D ms2 = new Vector2D(p2.image.getWidth() / 2, p2.image.getHeight() / 2);
        Vector2D mp = new Vector2D(p2.position);
        Vector2D matchA = mp.subtract(ms2).add(mpA);
        Vector2D matchAB = mpB.subtract(mpA);

        double angle = matchAB.angleBetween(islandAB);
        Vector2D position = new Vector2D(p2.position);
        Vector2D delta = matchA.subtract(position).rotate(angle).add(position).subtract(islandA);
        DistanceAngle distAngle = new DistanceAngle();
        distAngle.angle = angle;
        distAngle.delta = delta;

        return distAngle;
    }

    public static List<Vector2D> transform(Vector2D distance, double angle, List<Vector2D> points) {
        List<Vector2D> trans = new ArrayList<Vector2D>();
        java.util.ListIterator<Vector2D> it = points.listIterator();
        Vector2D start = it.next();
        start.add(distance);
        trans.add(start);
        while (it.hasNext()) {
            Vector2D newP = it.next();
            newP.add(distance);
            newP.rotate(angle, start);
            trans.add(newP);
        }
        return trans;
    }
}
