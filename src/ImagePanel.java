import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	BufferedImage image;
	
	public ImagePanel() {
		setPreferredSize(new Dimension(800, 600));
	}
	
	public void paint(Graphics g) {
		g.setColor(Color.BLACK);
		if (image != null) {
			g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		}
	}

	public void loadImage(File selectedFile) {
		try {
			image = ImageIO.read(selectedFile);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		repaint();
	}
}
