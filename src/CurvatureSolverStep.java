import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class CurvatureSolverStep extends Step implements Runnable {
    BufferedImage sandbox;
    BufferedImage display;
    List<Piece> layout;
    double bestError;

    public CurvatureSolverStep(Listener listener) {
        super(listener);
    }

    @Override
    public void begin(Object input) {
        layout = (List<Piece>) input;
        sandbox = makeSandbox(layout);
        bestError = Double.POSITIVE_INFINITY;
        setPreferredSize(new Dimension(sandbox.getWidth(), sandbox.getHeight()));

        new Thread(this).start();
    }

    private void solve() {
        // blob detect at 50%
        // construct lists of edges
        List<List<Double>> curvatures = new ArrayList<List<Double>>();
        List<List<Point>> perimeters = new ArrayList<List<Point>>();
        for (int i = 0; i < layout.size(); i++) {
            boolean[][] blob = Utility.getLargestBlob(layout.get(i).image, 128);
            List<Point> perimeter = Utility.perimeter(blob);
            perimeters.add(perimeter);
            List<Double> rawCurvature = Utility.getCurvature(perimeter);
            List<Double> curvature = Utility.smooth(rawCurvature, 5);
            curvatures.add(curvature);

            Utility.show(layout.get(i).image);
            BufferedImage image = new BufferedImage(layout.get(i).image.getWidth(), layout.get(i).image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            double mag = Double.NEGATIVE_INFINITY;
            for (int j = 0; j < curvature.size(); j++) {
                mag = Math.max(mag, Math.abs(curvature.get(j)));
            }
            for (int j = 0; j < perimeter.size(); j++) {
                int value = Math.abs((int) (255 / mag * curvature.get(j)));
                if (curvature.get(j) > 0) {
                    image.setRGB(perimeter.get(j).x, perimeter.get(j).y, new Color(value, 0, 0).getRGB());
                } else {
                    image.setRGB(perimeter.get(j).x, perimeter.get(j).y, new Color(0, value, 0).getRGB());
                }
            }
            Utility.show(image);
        }

        for (int i = 0; i < layout.size() - 1; i++) {
            for (int j = i + 1; j < layout.size(); j++) {
                Utility.CurvatureMatch match = Utility.matchCurvatures(curvatures.get(i), curvatures.get(j));
                BufferedImage image = new BufferedImage(layout.get(i).image.getWidth(), layout.get(i).image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int k = 0; k < match.length; k++) {
                    image.setRGB(perimeters.get(i).get((match.indexA + k) % perimeters.get(i).size()).x, perimeters.get(i).get((match.indexA + k) % perimeters.get(i).size()).y, Color.RED.getRGB());
                }
                Utility.show(image);
                BufferedImage image2 = new BufferedImage(layout.get(j).image.getWidth(), layout.get(j).image.getHeight(), BufferedImage.TYPE_INT_ARGB);
                for (int k = 0; k < match.length; k++) {
                    image2.setRGB(perimeters.get(j).get((match.indexB + k) % perimeters.get(j).size()).x, perimeters.get(j).get((match.indexB + k) % perimeters.get(j).size()).y, Color.GREEN.getRGB());
                }
                Utility.show(image2);
            }
        }

        // TODO
        // pick arbitrary piece as the island
        // while the island does not contain all pieces:
        // - find the best match between the island and any other piece using graph comparison of curvatures
        // - combine the island with the best match by moving and rotating it into place
        // - draw the best match on the island
    }

    @Override
    public void paint(Graphics g) {
        if (display != null) {
            g.drawImage(display, 0, 0, display.getWidth(), display.getHeight(), null);
        }
    }

    private BufferedImage makeSandbox(List<Piece> pieces) {
        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        for (Piece piece : pieces) {
            minX = (int) Math.min(minX, piece.position.x - piece.image.getWidth() / 2.0);
            minY = (int) Math.min(minY, piece.position.y - piece.image.getHeight() / 2.0);
            maxX = (int) Math.max(maxX, piece.position.x + piece.image.getWidth() / 2.0);
            maxY = (int) Math.max(maxY, piece.position.y + piece.image.getHeight() / 2.0);
        }

        int width = 2 * (maxX - minX);
        int height = 2 * (maxY - minY);

        return new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    @Override
    public void run() {
        solve();
    }
}
