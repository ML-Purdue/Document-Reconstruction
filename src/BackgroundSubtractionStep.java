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
            backgroundPoints[i] = new Point3d(color.getRed() / 255.0, color.getGreen() / 255.0, color.getBlue() / 255.0);
        }
        QuickHull3D hull = new QuickHull3D(backgroundPoints);
        Point3d[] vertices = hull.getVertices();
        int[][] faces = hull.getFaces();
        repaint();
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
