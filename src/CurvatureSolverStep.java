import java.awt.Dimension;
import java.awt.Graphics;
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
        for (int i = 0; i < layout.size(); i++) {
            curvatures.add(Utility.smooth(Utility.getCurvature(Utility.perimeter(Utility.getLargestBlob(layout.get(i).image, 128))), 5));
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
