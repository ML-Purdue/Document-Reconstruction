import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import scala.collection.JavaConversions._
import java.awt.event.KeyListener
import java.awt.event.KeyEvent
import javax.swing.JFrame
import java.awt.event.WindowEvent

class DivideAndConquer(listener: Listener) extends Step(listener) with Runnable {
  var pieceLayout: List[Piece] = null

  override def begin(input: Object) = {
    pieceLayout = input.asInstanceOf[java.util.List[Piece]].toList

    new Thread(this).start()
  }

  def run() = {
    var windows = for (p <- pieceLayout) yield Utility.show(p.image)

    while (pieceLayout.length > 1) {
      val pairErrors = for (i <- 0 until pieceLayout.length; j <- i + 1 until pieceLayout.length) yield (pieceLayout(i), pieceLayout(j), UtilityScala.bestMatch(pieceLayout(i), pieceLayout(j)))
      val (bestL, bestR, (merged, error)) = pairErrors.minBy({ case (_, _, (_, e)) => e })
      println(error)
      pieceLayout = merged :: pieceLayout.filter(p => p != bestL && p != bestR)

      //for (w <- windows) w.dispose()
      windows = for (p <- pieceLayout) yield Utility.show(p.image)
    }

    Thread.sleep(5000000)
  }
}