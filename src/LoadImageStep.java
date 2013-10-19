import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

@SuppressWarnings("serial")
public class LoadImageStep extends Step {
    public LoadImageStep(Listener listener) {
        super(listener);
    }

    public void begin(Object input) {
        JFileChooser fileChooser = new JFileChooser(System.getProperty("user.dir"));
        fileChooser.showOpenDialog(this);
        Object output = null;
        try {
            output = Utility.addAlphaChannel(ImageIO.read(fileChooser.getSelectedFile()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        listener.update(this, output);
    }
}
