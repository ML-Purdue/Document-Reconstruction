import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class ShoelaceSolverStep extends Step implements Runnable {

    BufferedImage sandbox;
    BufferedImage display;
    List<Piece> layout;
    double bestError;

    public ShoelaceSolverStep(Listener listener) {
        super(listener);
    }

    @Override
    public void begin(Object input) {
        layout = (List<Piece>) input;
        sandbox = makeSandbox(layout);
        display = sandbox;
        bestError = Double.POSITIVE_INFINITY;
        setPreferredSize(new Dimension(sandbox.getWidth(), sandbox.getHeight()));

        new Thread(this).start();
    }

    private void solve() {
        // blob detect at 50%
        // construct lists of edges
        Utility.drawLayout(layout, sandbox);
        repaint();
        List<List<Double>> curvatures = new ArrayList<List<Double>>();
        List<List<Point2D.Double>> perimeters = new ArrayList<List<Point2D.Double>>();
        List<List<Color>> colors = new ArrayList<List<Color>>();
        for (int i = 0; i < layout.size(); i++) {
            System.out.printf("Computing perimeter, curvature, and colors %d/%d\n", i + 1, layout.size());
            boolean[][] blob = Utility.getLargestBlob(layout.get(i).image, 128);
            List<Point2D.Double> perimeter = Utility.awesomePerimeter(blob);
            perimeters.add(perimeter);
            List<Double> rawCurvature = Utility.getCurvature(perimeter);
            List<Double> curvature = Utility.smooth(rawCurvature, 5);
            curvatures.add(curvature);
            List<Color> edgeColors = Utility.getEdgeColors(layout.get(i).image, perimeter);
            colors.add(edgeColors);
            int h = 10;
            BufferedImage image = new BufferedImage(edgeColors.size(), h, BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < edgeColors.size(); x++) {
                Color c = edgeColors.get(x);
                c = new Color(c.getRed(), c.getGreen(), c.getBlue(), 255); // set alpha to 1
                for (int y = 0; y < h; y++) {
                    image.setRGB(x, y, c.getRGB());
                }
            }
            Utility.show(image);
        }

        // TODO
        // pick arbitrary piece as the island
        // while the island does not contain all pieces:
        // - find the best match between the island and any other piece using graph comparison of curvatures
        // - combine the island with the best match by moving and rotating it into place
        // - draw the best match on the island

        Piece island = layout.remove(0);
        island.position.x = sandbox.getWidth() / 2;
        island.position.y = sandbox.getHeight() / 2;
        List<Point2D.Double> islandPerimeter = perimeters.remove(0);
        List<Double> islandCurvature = curvatures.remove(0);
        List<Color> islandColor = colors.remove(0);

        while (!layout.isEmpty()) {
            Utility.MatchInfo bestMatch = null;
            int bestIndex = 0;
            double bestError = Double.POSITIVE_INFINITY;
            for (int i = 0; i < layout.size(); i++) {
                System.out.printf("Computing best match %d/%d\n", i + 1, layout.size());
                Utility.MatchInfo match = Utility.matchShoelaceAndColors(island, layout.get(i), islandPerimeter, perimeters.get(i), islandColor, colors.get(i));
                if (match.error < bestError) {
                    bestError = match.error;
                    bestMatch = match;
                    bestIndex = i;
                }
            }
            BufferedImage image = new BufferedImage(layout.get(bestIndex).image.getWidth(), layout.get(bestIndex).image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int k = 0; k < bestMatch.length; k++) {
                image.setRGB((int) perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + k, perimeters.get(bestIndex).size())).x, (int) perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + k, perimeters.get(bestIndex).size())).y, Color.RED.getRGB());
            }
            //Utility.show(image);
            image = new BufferedImage(island.image.getWidth(), island.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int k = 0; k < bestMatch.length; k++) {
                image.setRGB((int) islandPerimeter.get((bestMatch.indexA + k) % islandPerimeter.size()).x, (int) islandPerimeter.get((bestMatch.indexA + k) % islandPerimeter.size()).y, Color.RED.getRGB());
            }
            //Utility.show(image);

            Utility.DistanceAngle distAngle = Utility.calculateDistanceAngle(islandPerimeter.get(bestMatch.indexA),
                    islandPerimeter.get((bestMatch.indexA + bestMatch.length) % islandPerimeter.size()),
                    perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB, perimeters.get(bestIndex).size())),
                    perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + bestMatch.length, perimeters.get(bestIndex).size())),
                    island,
                    layout.get(bestIndex));
            double angle = distAngle.angle;
            Vector2D bestPosition = new Vector2D(layout.get(bestIndex).position);
            Vector2D delta = distAngle.delta;
            layout.get(bestIndex).position = bestPosition.subtract(delta);
            layout.get(bestIndex).rotation += angle;
            BufferedImage temp = new BufferedImage(sandbox.getWidth(), sandbox.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Utility.drawPiece(island, temp);
            Utility.drawPiece(layout.get(bestIndex), temp);

            int maxX = (int) Math.max(island.image.getWidth() / 2 + island.position.x, layout.get(bestIndex).image.getWidth() / 2 + layout.get(bestIndex).position.x);
            int maxY = (int) Math.max(island.image.getHeight() / 2 + island.position.y, layout.get(bestIndex).image.getHeight() / 2 + layout.get(bestIndex).position.y);
            BufferedImage tempIsland = new BufferedImage(sandbox.getWidth(), sandbox.getHeight(), BufferedImage.TYPE_INT_ARGB);
            island.position.x = sandbox.getWidth() / 2;
            island.position.y = sandbox.getHeight() / 2;
            Utility.drawPiece(island, tempIsland);
            Utility.drawPiece(layout.get(bestIndex), tempIsland);

            layout.remove(bestIndex);
            perimeters.remove(bestIndex);
            curvatures.remove(bestIndex);
            colors.remove(bestIndex);

            //            islandPerimeter = Utility.perimeter(Utility.getLargestBlob(island.image, 128));
            //            islandCurvature = Utility.smooth(Utility.getCurvature(islandPerimeter), 5);

            Utility.drawChecker(sandbox.getGraphics(), sandbox.getWidth(), sandbox.getHeight(), 10, Color.LIGHT_GRAY, Color.DARK_GRAY);
            //Utility.show(island.image);
            Utility.drawPiece(island, sandbox);
            //Utility.show(temp);
            repaint();
            System.out.println();

            // set bestPiece.rotation and position
            // draw bestPiece on match
            // recalculate island blob, perimeter, curvature
            // draw layout on checker background in sandbox

            //Merge the best fit
            island.image = tempIsland;
            island.position.x = sandbox.getWidth() / 2;
            island.position.y = sandbox.getHeight() / 2;
            boolean[][] blob = Utility.getLargestBlob(island.image, 128);
            List<Point2D.Double> perimeter = Utility.awesomePerimeter(blob);
            islandPerimeter = perimeter;
            List<Double> rawCurvature = Utility.getCurvature(perimeter);
            List<Double> curvature = Utility.smooth(rawCurvature, 10);
            islandCurvature = curvature;
            islandColor = Utility.getEdgeColors(island.image, perimeter);
            repaint();
        }
        Utility.drawPiece(island, sandbox);
        Utility.show(island.image);
        repaint();
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
