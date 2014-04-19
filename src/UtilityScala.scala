import java.awt.image.BufferedImage
import java.awt.Color
import java.awt.geom.Point2D
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import java.util.Random;
import javax.swing.JFrame

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
      return scala.Double.PositiveInfinity
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

    val (n, m, l) = (random.nextInt(leftCurve.size()), random.nextInt(rightCurve.size()), random.nextInt(Math.min(leftCurve.length, rightCurve.length)))

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

  def crop(b: BufferedImage): BufferedImage = {
    var (minX, minY, maxX, maxY) = (b.getWidth(), b.getHeight(), 0, 0)
    for (x <- 0 until b.getWidth(); y <- 0 until b.getHeight()) {
      if (Utility.getAlphaValue(b.getRGB(x, y)) > 0) {
        minX = Math.min(minX, x)
        minY = Math.min(minY, y)
        maxX = Math.max(maxX, x)
        maxY = Math.max(maxY, y)
      }
    }

    val newImage = new BufferedImage(maxX - minX, maxY - minY, BufferedImage.TYPE_INT_ARGB)
    val g = newImage.getGraphics()
    g.drawImage(b, -minX, -minY, null)

    return newImage
  }

  def merge(l: Piece, r: Piece): Piece = {
    val longest = Math.max(Math.max(l.image.getWidth(), l.image.getHeight()), Math.max(r.image.getWidth(), r.image.getHeight()))
    val (sw, sh) = (4 * longest, 4 * longest)

    val newImage = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB)

    Utility.drawPiece(l, newImage)
    Utility.drawPiece(r, newImage)
    val cropped = UtilityScala.crop(newImage)
    return new Piece(new Point2D.Double(cropped.getWidth() / 2, cropped.getHeight() / 2), 0, cropped)
  }

  def mergeMatch(lOld: Piece, lp: List[Point2D.Double], rOld: Piece, rp: List[Point2D.Double], info: Utility.MatchInfo): Piece = {
    val (l, r) = (new Piece(lOld), new Piece(rOld))
    val longest = Math.max(Math.max(l.image.getWidth(), l.image.getHeight()), Math.max(r.image.getWidth(), r.image.getHeight()))
    val (sw, sh) = (4 * longest, 4 * longest)
    l.position = new Vector2D(sw / 2, sh / 2)

    val distAngle = Utility.calculateDistanceAngle(
      lp.get(info.indexA),
      lp.get((info.indexA + info.length) % lp.size()),
      rp.get(info.indexB),
      rp.get((info.indexB + info.length) % rp.size()),
      l,
      r);

    r.position = new Vector2D(r.position).subtract(distAngle.delta)
    r.rotation = distAngle.angle

    val newImage = new BufferedImage(sw, sh, BufferedImage.TYPE_INT_ARGB)

    //    for (i <- 0 to info.length) {
    //      val iPrime = Utility.mod(info.indexA + i, lp.length)
    //      l.image.setRGB(lp(iPrime).getX().intValue(), lp(iPrime).getY().intValue(), Color.RED.getRGB())
    //    }
    //
    //    for (i <- 0 to info.length) {
    //      val iPrime = Utility.mod(info.indexB - (rp.length - 1 - i), rp.length)
    //      r.image.setRGB(rp(iPrime).getX().intValue(), rp(iPrime).getY().intValue(), Color.GREEN.getRGB())
    //    }
    //Utility.show(l.image)
    //Utility.show(r.image)

    Utility.drawPiece(l, newImage)
    Utility.drawPiece(r, newImage)
    val cropped = UtilityScala.crop(newImage)
    return new Piece(new Point2D.Double(cropped.getWidth() / 2, cropped.getHeight() / 2), 0, cropped)
  }

  def bestMatch(l: Piece, r: Piece): (Piece, Double) = {
    val pieceToPerimeter = (p: Piece) => Utility.awesomePerimeter(Utility.getLargestBlob(p.image, 128)).toList
    val perimeterToSmoothCurve = (p: List[Point2D.Double], amount: Int) => Utility.smooth(Utility.getCurvature(p), amount).toList.map(_.toDouble)

    val (leftPerimeter, rightPerimeter) = (pieceToPerimeter(l), pieceToPerimeter(r))
    val (leftCurve, rightCurve) = (perimeterToSmoothCurve(leftPerimeter, 5).toList, perimeterToSmoothCurve(rightPerimeter, 5).toList);

    val info = Utility.matchCurvatures(leftCurve.map(x => new java.lang.Double(x)), rightCurve.map(x => new java.lang.Double(x)))

    return (mergeMatch(l, leftPerimeter, r, rightPerimeter, info), info.error);
  }

  def doubleToColor(d: Double): Color = {
    if (d.isNaN()) {
      return Color.WHITE;
    }
    val v = 2 / (1 + Math.pow(3, -d)) - 1
    new Vector3D(0.5, 0.5, 0.5) + (new Vector3D(0.5, 0, -0.5) * v) toColor
  }
}