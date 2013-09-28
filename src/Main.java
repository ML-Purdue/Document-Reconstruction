import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JPanel;


public class Main extends JFrame {
	public static void main(String[] args) {
		new Main();
	}
	
	public Main() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		ImagePanel imagePanel = new ImagePanel();
		
		getContentPane().add(imagePanel);
		pack();
		
		setVisible(true);
	}
}
