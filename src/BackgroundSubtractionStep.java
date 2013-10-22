import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashSet;
import quickhull3d.Point3d;
import quickhull3d.QuickHull3D;

@SuppressWarnings("serial")
public class BackgroundSubtractionStep extends Step implements MouseMotionListener, KeyListener, MouseListener {
    BufferedImage originalImage;
    BufferedImage processedImage;
    HashSet<Color> foregroundColors = new HashSet<Color>();
    HashSet<Color> backgroundColors = new HashSet<Color>();
    boolean mouseLeftDown = false;
    boolean mouseRightDown = false;

    public BackgroundSubtractionStep(Listener listener) {
        super(listener);

        setFocusable(true);
        addKeyListener(this);
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    @Override
    public void begin(Object input) {
        originalImage = (BufferedImage) input;
        processedImage = Utility.addAlphaChannel(originalImage);
        setPreferredSize(new Dimension(originalImage.getWidth(), originalImage.getHeight()));
        // TODO listener.update(this, output);
    }

    @Override
    public void paint(Graphics g) {
        if (processedImage != null) {
            g.drawImage(processedImage, 0, 0, processedImage.getWidth(), processedImage.getHeight(), null);
        }
    }

    @Override
    public void mouseDragged(MouseEvent arg0) {
        if (mouseLeftDown == true) {
            foregroundColors.add(new Color(originalImage.getRGB(arg0.getX(), arg0.getY())));
        }
        if (mouseRightDown == true) {
            backgroundColors.add(new Color(originalImage.getRGB(arg0.getX(), arg0.getY())));
        }
    }

    @Override
    public void mouseMoved(MouseEvent arg0) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
        switch (e.getKeyCode()) {
        case KeyEvent.VK_F:
            foregroundColors.clear();
            break;
        case KeyEvent.VK_B:
            backgroundColors.clear();
            break;
        case KeyEvent.VK_ENTER:
            subtractBackground();
            break;
        }
    }

    private void subtractBackground() {
        Point3d[] backgroundPoints = new Point3d[backgroundColors.size()];
        ArrayList<Color> backgroundColorsList = new ArrayList<Color>(backgroundColors);
        for (int i = 0; i < backgroundColorsList.size(); i++) {
            Color color = backgroundColorsList.get(i);
            backgroundPoints[i] = Utility.colorToPoint3d(color);
        }
        QuickHull3D backgroundHull = new QuickHull3D(backgroundPoints);
        Point3d[] backgroundVertices = backgroundHull.getVertices();
        int[][] backgroundFaces = backgroundHull.getFaces();

        Point3d[] foregroundPoints = new Point3d[foregroundColors.size()];
        ArrayList<Color> foregroundColorsList = new ArrayList<Color>(foregroundColors);
        for (int i = 0; i < foregroundColorsList.size(); i++) {
            Color color = foregroundColorsList.get(i);
            foregroundPoints[i] = Utility.colorToPoint3d(color);
        }
        QuickHull3D foregroundHull = new QuickHull3D(foregroundPoints);
        Point3d[] foregroundVertices = foregroundHull.getVertices();
        int[][] foregroundFaces = foregroundHull.getFaces();

        Point3d[][] processedImagePoints = new Point3d[processedImage.getWidth()][processedImage.getHeight()];
        for (int y = 0; y < processedImagePoints[0].length; y++) {
            for (int x = 0; x < processedImagePoints.length; x++) {
                Point3d point = Utility.colorToPoint3d(new Color(originalImage.getRGB(x, y)));
                processedImagePoints[x][y] = processColor(point, backgroundHull, foregroundHull);
            }
        }

        for (int y = 0; y < processedImage.getHeight(); y++) {
            for (int x = 0; x < processedImage.getWidth(); x++) {
                processedImage.setRGB(x, y, Utility.Point3dToColor(processedImagePoints[x][y]).getRGB());
            }
        }

        repaint();
    }

    private Point3d processColor(Point3d point, QuickHull3D backgroundHull, QuickHull3D foregroundHull) {
        Point3d[] foregroundVertices = foregroundHull.getVertices();
        double nearestDistance = Double.POSITIVE_INFINITY;
        Point3d nearestPoint = null;

        for (int i = 0; i < foregroundVertices.length; i++) {
            if (point.distance(foregroundVertices[i]) < nearestDistance) {
                nearestDistance = point.distance(foregroundVertices[i]);
                nearestPoint = new Point3d(foregroundVertices[i]);
            }
        }

        return nearestPoint;
    }

    @Override
    public void keyReleased(KeyEvent e) {
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void mouseClicked(MouseEvent arg0) {
    }

    @Override
    public void mouseEntered(MouseEvent arg0) {
    }

    @Override
    public void mouseExited(MouseEvent arg0) {
    }

    @Override
    public void mousePressed(MouseEvent arg0) {
        switch (arg0.getButton()) {
        case MouseEvent.BUTTON1:
            mouseLeftDown = true;
            break;
        case MouseEvent.BUTTON3:
            mouseRightDown = true;
            break;
        }
    }

    @Override
    public void mouseReleased(MouseEvent arg0) {
        switch (arg0.getButton()) {
        case MouseEvent.BUTTON1:
            mouseLeftDown = false;
            break;
        case MouseEvent.BUTTON3:
            mouseRightDown = false;
            break;
        }
    }
}
