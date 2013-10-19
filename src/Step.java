import javax.swing.JPanel;

@SuppressWarnings("serial")
public abstract class Step extends JPanel {
    Listener listener;

    public Step(Listener listener) {
        this.listener = listener;
    }

    public abstract void begin(Object input);
}
