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
        List<List<Point>> perimeters = new ArrayList<List<Point>>();
        for (int i = 0; i < layout.size(); i++) {
            System.out.printf("Computing perimeter and curvature %d/%d\n", i + 1, layout.size());
            boolean[][] blob = Utility.getLargestBlob(layout.get(i).image, 128);
            List<Point> perimeter = Utility.perimeter(blob);
            perimeters.add(perimeter);
            List<Double> rawCurvature = Utility.getCurvature(perimeter);
            List<Double> curvature = Utility.smooth(rawCurvature, 10);
            curvatures.add(curvature);
        }

        // TODO
        // pick arbitrary piece as the island
        // while the island does not contain all pieces:
        // - find the best match between the island and any other piece using graph comparison of curvatures
        // - combine the island with the best match by moving and rotating it into place
        // - draw the best match on the island

        Piece island = layout.get(0);
        List<Point> islandPerimeter = perimeters.get(0);
        List<Double> islandCurvature = curvatures.get(0);
        layout.remove(0);
        perimeters.remove(0);
        curvatures.remove(0);

        while (!layout.isEmpty()) {
            Utility.CurvatureMatch bestMatch = null;
            int bestIndex = 0;
            double bestError = Double.POSITIVE_INFINITY;
            for (int i = 0; i < layout.size(); i++) {
                System.out.printf("Computing best match %d/%d\n", i + 1, layout.size());
                Utility.CurvatureMatch match = Utility.matchCurvatures(islandCurvature, curvatures.get(i));
                if (match.error < bestError) {
                    bestError = match.error;
                    bestMatch = match;
                    bestIndex = i;
                }
            }
            BufferedImage image = new BufferedImage(layout.get(bestIndex).image.getWidth(), layout.get(bestIndex).image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int k = 0; k < bestMatch.length; k++) {
                image.setRGB(perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + k, perimeters.get(bestIndex).size())).x, perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + k, perimeters.get(bestIndex).size())).y, Color.RED.getRGB());
            }
            //Utility.show(image);
            image = new BufferedImage(island.image.getWidth(), island.image.getHeight(), BufferedImage.TYPE_INT_ARGB);
            for (int k = 0; k < bestMatch.length; k++) {
                image.setRGB(islandPerimeter.get((bestMatch.indexA + k) % islandPerimeter.size()).x, islandPerimeter.get((bestMatch.indexA + k) % islandPerimeter.size()).y, Color.RED.getRGB());
            }
            //Utility.show(image);
            Vector2D ipA = new Vector2D(islandPerimeter.get(bestMatch.indexA));
            Vector2D ipB = new Vector2D(islandPerimeter.get((bestMatch.indexA + bestMatch.length) % islandPerimeter.size()));
            Vector2D is2 = new Vector2D(island.image.getWidth() / 2, island.image.getHeight() / 2);
            Vector2D ip = new Vector2D(island.position);
            Vector2D islandA = ip.subtract(is2).add(ipA);
            Vector2D islandAB = ipB.subtract(ipA);

            Vector2D mpB = new Vector2D(perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB, perimeters.get(bestIndex).size())));
            Vector2D mpA = new Vector2D(perimeters.get(bestIndex).get(Utility.mod(bestMatch.indexB + bestMatch.length, perimeters.get(bestIndex).size())));
            Vector2D ms2 = new Vector2D(layout.get(bestIndex).image.getWidth() / 2, layout.get(bestIndex).image.getHeight() / 2);
            Vector2D mp = new Vector2D(layout.get(bestIndex).position);
            Vector2D matchA = mp.subtract(ms2).add(mpA);
            Vector2D matchAB = mpB.subtract(mpA);

            double angle = matchAB.angleBetween(islandAB);
            Vector2D bestPosition = new Vector2D(layout.get(bestIndex).position);
            Vector2D delta = matchA.subtract(bestPosition).rotate(angle).add(bestPosition).subtract(islandA);
            layout.get(bestIndex).position = bestPosition.subtract(delta);
            layout.get(bestIndex).rotation += angle;
            BufferedImage temp = new BufferedImage(sandbox.getWidth(), sandbox.getHeight(), BufferedImage.TYPE_INT_ARGB);
            Utility.drawPiece(island, temp);
            Utility.drawPiece(layout.get(bestIndex), temp);

            int maxX = (int) Math.max(island.image.getWidth() / 2 + island.position.x, layout.get(bestIndex).image.getWidth() / 2 + layout.get(bestIndex).position.x);
            int maxY = (int) Math.max(island.image.getHeight() / 2 + island.position.y, layout.get(bestIndex).image.getHeight() / 2 + layout.get(bestIndex).position.y);
            BufferedImage tempIsland = new BufferedImage(maxX, maxY, BufferedImage.TYPE_INT_ARGB);
            Utility.drawPiece(island, tempIsland);
            Utility.drawPiece(layout.get(bestIndex), tempIsland);

            layout.remove(bestIndex);
            perimeters.remove(bestIndex);
            curvatures.remove(bestIndex);

            islandPerimeter = Utility.perimeter(Utility.getLargestBlob(island.image, 128));
            islandCurvature = Utility.smooth(Utility.getCurvature(islandPerimeter), 5);

            Utility.drawChecker(sandbox.getGraphics(), sandbox.getWidth(), sandbox.getHeight(), 10, Color.LIGHT_GRAY, Color.DARK_GRAY);
            //Utility.show(island.image);
            Utility.drawPiece(island, sandbox);
            Vector2D matchedA = matchA.rotate(angle, bestPosition).subtract(delta);
            Vector2D matchedAB = matchAB.rotate(angle);
            Graphics g = temp.getGraphics();
            g.setColor(Color.WHITE);
            g.drawLine((int) islandA.x, (int) islandA.y, (int) matchA.x, (int) matchA.y);
            g.setColor(Color.GREEN);
            g.drawLine((int) islandA.x, (int) islandA.y, (int) islandA.x + (int) islandAB.x, (int) islandA.y + (int) islandAB.y);
            g.setColor(Color.RED);
            g.drawLine((int) matchA.x, (int) matchA.y, (int) matchA.x + (int) matchAB.x, (int) matchA.y + (int) matchAB.y);
            g.setColor(new Color(0, 0, 255, 128));
            g.drawLine((int) matchedA.x, (int) matchedA.y, (int) matchedA.x + (int) matchedAB.x, (int) matchedA.y + (int) matchedAB.y);
            g.drawRect((int) (island.position.x - island.image.getWidth() / 2), (int) (island.position.y - island.image.getHeight() / 2), island.image.getWidth(), island.image.getHeight());
            //Utility.show(temp);
            repaint();
            System.out.println();

            // set bestPiece.rotation and position
            // draw bestPiece on match
            // recalculate island blob, perimeter, curvature
            // draw layout on checker background in sandbox

            //Merge the best fit
            island.image = tempIsland;
            island.position.x = tempIsland.getWidth() / 2;
            island.position.y = tempIsland.getHeight() / 2;
            boolean[][] blob = Utility.getLargestBlob(island.image, 128);
            List<Point> perimeter = Utility.perimeter(blob);
            islandPerimeter = perimeter;
            List<Double> rawCurvature = Utility.getCurvature(perimeter);
            List<Double> curvature = Utility.smooth(rawCurvature, 10);
            islandCurvature = curvature;
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
