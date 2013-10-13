import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;

public class Main extends JFrame implements ActionListener {
    JFileChooser fileChooser;
    JButton loadImageButton;
    ImagePanel imagePanel;

    public static void main(String[] args) {
        new Main();
    }

    public Main() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        loadImageButton = new JButton("Load image...");
        imagePanel = new ImagePanel();

        getContentPane().add(loadImageButton, BorderLayout.NORTH);
        getContentPane().add(imagePanel, BorderLayout.CENTER);
        pack();

        loadImageButton.addActionListener(this);
        fileChooser = new JFileChooser(System.getProperty("user.dir"));

        setVisible(true);
    }

    public void actionPerformed(ActionEvent arg0) {
        if (arg0.getSource() == loadImageButton) {
            fileChooser.showOpenDialog(Main.this);
            try {
                imagePanel.updateImage(ImageIO.read(fileChooser.getSelectedFile()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
