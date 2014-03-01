import java.awt.image.BufferedImage;
import java.awt.Color;

object UtilityScala {
  def showAlpha(image: BufferedImage): BufferedImage = {
    val newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
    for (iy <- 0 until image.getHeight(); ix <- 0 until image.getWidth()) {
      System.out.println("(" + ix + ", " + iy + ")");
      val alpha = new Color(image.getRGB(ix, iy), true).getAlpha();
      newImage.setRGB(ix, iy, new Color(alpha, alpha, alpha).getRGB());
    }
    return newImage;
  }

  def lerp(a: Double, b: Double, t: Double): Double = {
    return a + (b - a) * t;
  }

  def unlerp(a: Double, b: Double, v: Double): Double = {
    return (v - a) / (b - a);
  }
}