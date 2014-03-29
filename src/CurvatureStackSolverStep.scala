import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.geom.Point2D
import java.awt.image.BufferedImage
import scala.collection.JavaConversions._
import javax.swing.JLabel
import javax.swing.ImageIcon
import java.awt.event.KeyListener
import java.awt.event.KeyEvent
import java.io.File
import java.io.PrintWriter
import java.util.Random;
import scala.collection.mutable.ListBuffer

class CurvatureStackSolverStep(listener: Listener) extends Step(listener) with Runnable {
  var sandbox: BufferedImage = null
  var display: BufferedImage = null
  var pieceLayout: java.util.List[Piece] = null
  var stacks: Array[Array[(Piece, List[Piece], Double)]] = null
  var random: Random = new Random()

  override def begin(input: Object) = {
    pieceLayout = input.asInstanceOf[java.util.List[Piece]].toList
    sandbox = new BufferedImage(600, 600, BufferedImage.TYPE_INT_ARGB)
    var picLabel = new JLabel(new ImageIcon(sandbox))
    //add(picLabel)
    display = sandbox
    setPreferredSize(new Dimension(sandbox.getWidth(), sandbox.getHeight()))

    val n = pieceLayout.length
    stacks = Array.ofDim[(Piece, List[Piece], Double)](n, n)
    for (i <- 0 until stacks.length) {
      stacks(i)(0) = ((pieceLayout.get(i), pieceLayout.filterNot(e => e == pieceLayout.get(i)).toList, 0))
      for (j <- 1 until stacks(i).length) {
        stacks(i)(j) = makeNextLevel(stacks(i)(j - 1))
      }
    }

    new Thread(this).start()
  }

  def makeNextLevel(item: (Piece, List[Piece], Double)): (Piece, List[Piece], Double) = {
    val p = item._2.get(random.nextInt(item._2.size()))
    val island = new Piece(item._1)
    island.position = new Point2D.Double(sandbox.getWidth() / 2, sandbox.getHeight() / 2)
    var (left, right, e) = UtilityScala.randomConfiguration(island, p)

    var temp = new BufferedImage(sandbox.getWidth(), sandbox.getHeight(), BufferedImage.TYPE_INT_ARGB)
    Utility.drawPiece(left, temp)
    Utility.drawPiece(right, temp)

    var colorDifference = ((Utility.getSumOfPixels(UtilityScala.showAlpha(p.image)) + Utility.getSumOfPixels(UtilityScala.showAlpha(island.image))) - Utility.getSumOfPixels(UtilityScala.showAlpha(temp))).toDouble
    colorDifference /= 255.0
    colorDifference *= 0.01 // weight
    e += colorDifference

    return (new Piece(new Point2D.Double(sandbox.getWidth() / 2, sandbox.getHeight() / 2), 0, temp), item._2.filterNot(elm => elm == p), e)
  }

  override def paint(g: Graphics) = {
    g.clearRect(0, 0, getWidth(), getHeight())
    g.drawImage(sandbox, 0, 0, null)
  }

  def run() = {
    while (true) {
      //Thread.sleep(500)
      for (s <- stacks) {
        printf("%.2f ", s(1)._3)
      }
      println()
      val i = random.nextInt(stacks.length)
      val j = random.nextInt(stacks(i).length - 1)
      val (p, l, e) = makeNextLevel(stacks(i)(j))
      if (e < stacks(i)(j + 1)._3) {
        //printf("SUCC i %d, j %d, e %.4f, old_e %.4f\n", i, j, e, stacks(i)(j + 1)._3)
        stacks(i)(j + 1) = (p, l, e)
        for (jj <- (j + 2) until stacks(i).length) {
          stacks(i)(jj) = makeNextLevel(stacks(i)(jj - 1))
        }
        //val s = stacks.minBy((s: Array[(Piece, List[Piece], Double)]) => s.map(_._3).sum)
        sandbox = new BufferedImage(sandbox.getWidth(), sandbox.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Utility.drawPiece(stacks(3).last._1, sandbox)
        //println(s.last._3)
      } else {
        //printf("FAIL i %d, j %d, e %.4f, old_e %.4f\n", i, j, e, stacks(i)(j + 1)._3)
      }
      repaint();
    }
  }
}