import java.util.LinkedList;
import javax.swing.JFrame;

@SuppressWarnings("serial")
public class Main extends JFrame implements Listener {
    LinkedList<Step> steps = new LinkedList<Step>();

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        steps.add(new LoadImageStep(this));
        steps.add(new BackgroundSubtractionStep(this));
        steps.add(new AlphaCleanupStep(this));
        steps.add(new BlobDetectionStep(this));
        steps.add(new SmallBlobDeletionStep(this));
        steps.add(new CurvatureSolverStep(this));

        steps.add(0, null);
        update(null, null);

        setVisible(true);
    }

    @Override
    public void update(Object source, Object data) {
        if (steps.peek() != null) {
            remove(steps.peek());
        }
        steps.remove();
        if (steps.size() > 0) {
            getContentPane().add(steps.peek());
            steps.peek().begin(data);
            pack();
        }
    }
}
