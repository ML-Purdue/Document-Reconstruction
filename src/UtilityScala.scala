import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.geom.Point2D
import scala.collection.JavaConversions._
import java.util.Random;

object UtilityScala {
  val random = new Random()

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

  def randomConfiguration(leftPiece: Piece, rightPiece: Piece): (Piece, Piece, Double) = {
    val pieceToPerimeter = (p: Piece) => Utility.awesomePerimeter(Utility.getLargestBlob(p.image, 128)).toList
    val perimeterToSmoothCurve = (p: List[Point2D.Double], amount: Int) => Utility.smooth(Utility.getCurvature(p), amount).toList.map(_.toDouble)

    val (leftPerimeter, rightPerimeter) = (pieceToPerimeter(leftPiece), pieceToPerimeter(rightPiece))
    val (leftCurve, rightCurve) = (perimeterToSmoothCurve(leftPerimeter, 5).toList, perimeterToSmoothCurve(rightPerimeter, 5).toList);

    val (n, m, l) = (random.nextInt(leftCurve.size()), random.nextInt(rightCurve.size()), Math.min(leftCurve.length, rightCurve.length))

    val distAngle = Utility.calculateDistanceAngle(
      leftPerimeter.get(n),
      leftPerimeter.get((n + l) % leftPerimeter.size()),
      rightPerimeter.get(m),
      rightPerimeter.get((m + l) % rightPerimeter.size()),
      leftPiece,
      rightPiece);
    val p = new Piece(new Vector2D(rightPiece.position).subtract(distAngle.delta), distAngle.angle, rightPiece.image)
    val e = UtilityScala.curveError((leftCurve ++ leftCurve).drop(n).take(l), (rightCurve ++ rightCurve).drop(m).take(l).reverse.map(_ * -1))

    return (leftPiece, p, e)
  }
}
