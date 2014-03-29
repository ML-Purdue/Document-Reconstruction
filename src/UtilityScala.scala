import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.geom.Point2D
import scala.collection.JavaConversions._

object UtilityScala {
  def showAlpha(image: BufferedImage): BufferedImage =
    {
      val newImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
      for (iy <- 0 until image.getHeight(); ix <- 0 until image.getWidth()) {
        val alpha = new Color(image.getRGB(ix, iy), true).getAlpha();
        newImage.setRGB(ix, iy, new Color(alpha, alpha, alpha).getRGB());
      }
      return newImage;
    }

  def lerp(a: Double, b: Double, t: Double): Double =
    {
      return a + (b - a) * t;
    }

  def unlerp(a: Double, b: Double, v: Double): Double =
    {
      return (v - a) / (b - a);
    }

  /*def listPoint2DToVector2D(l: List<Point2D.Double>): List<Vector2D> =
  {
    val lp = new ArrayList<Vector2D>();
    for (Point2D.Double p : l) {
        lp.add(new Vector2D(p));
    }
    return lp;
  }*/

  def curveError(left: List[Double], right: List[Double]): Double = {
    assume(left.length == right.length)
    if (left.length == 0) {
      return Double.PositiveInfinity
    }

    val integrate = (l: List[Double]) => l.scanLeft(0.0)(_ + _)
    val difference = (l: List[Double], r: List[Double]) => (l, r).zipped map (_ - _)
    val x = difference(integrate(left), integrate(right))

    (10 + x.map(Math.abs _).sum) / left.length
  }

  def computeConfigurationError(leftPiece: Piece, rightPiece: Piece, n: Int, m: Int, l: Int): Double = {
    val pieceToSmoothCurve = (p: Piece, amount: Int) => Utility.smooth(Utility.getCurvature(Utility.awesomePerimeter(Utility.getLargestBlob(p.image, 128))), amount).toList.map(_.toDouble)
    val (leftCurve, rightCurve) = (pieceToSmoothCurve(leftPiece, 5).toList, pieceToSmoothCurve(rightPiece, 5));

    return UtilityScala.curveError((leftCurve ++ leftCurve).drop(n).take(l), (rightCurve ++ rightCurve).drop(m).take(l).reverse.map(_ * -1))
  }
}
