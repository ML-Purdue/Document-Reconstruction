import java.awt.BorderLayout;
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

        steps.add(null);
        steps.add(new LoadImageStep(this));
        steps.add(new BackgroundSubtractionStep(this));

        setVisible(true);

        update(null, null);
    }

    @Override
    public void update(Object source, Object data) {
        if (steps.peek() != null) {
            remove(steps.peek());
        }
        steps.remove();
        if (steps.size() > 0) {
            setLayout(new BorderLayout());
            getContentPane().add(steps.peek());
            steps.peek().begin(data);
            pack();
        }
    }
}
