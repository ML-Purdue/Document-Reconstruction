import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.HashMap;

public class BackgroundRemover {
	BufferedImage image;
	Color[][] colorArray;
	HashMap<Integer, Integer> colorCounts = new HashMap<Integer, Integer>();

	public BackgroundRemover(BufferedImage image) {
		this.image = image;
	}

	public void calculate() {
		calculateColorArray();
		countColors();
	}

	private void calculateColorArray() {
		colorArray = new Color[image.getHeight()][image.getWidth()];
		for (int y = 0; y < image.getHeight(); y++)
			for (int x = 0; x < image.getWidth(); x++)
				colorArray[y][x] = new Color(image.getRGB(x, y), true);
	}

	private void countColors() {
		for (int y = 0; y < colorArray.length; y++)
			for (int x = 0; x < colorArray[y].length; x++) {
				int rgb = colorArray[y][x].getRGB();
				if (!colorCounts.containsKey(rgb))
					colorCounts.put(rgb, 1);
				else
					colorCounts.put(rgb, colorCounts.get(rgb) + 1);
			}
	}
	public Color[][] getColorArray(){
		return colorArray;
	}
	public HashMap<Integer,Integer> getColorCounts(){
		return colorCounts;
	}
}
