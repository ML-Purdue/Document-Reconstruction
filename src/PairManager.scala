import java.util._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class PairManager(var pieces: ListBuffer[Piece]) extends Observable() with Runnable {
  var acceptPending = false
  var accepted: (Piece, Piece, Double) = null
  val pairs = (for (i <- 0 until pieces.size; j <- i + 1 until pieces.size) yield (new Piece(pieces.get(i)), new Piece(pieces.get(j)), Double.PositiveInfinity)).to[ListBuffer]
  pairs.sortBy(_._3).reverse

  def acceptPair(piece: (Piece, Piece, Double)) = {
    acceptPending = true
  }

  def doAccept(pair: (Piece, Piece, Double)) = {
    println("HI")
    val (l, r, e) = pair
    pieces = UtilityScala.merge(l, r) +=: pieces.filter(p => !p.equals(l) && !p.equals(r))
  }

  override def run() = {
    while (true) {
      def f {
        for (i <- 0 until pairs.length) {
          if (acceptPending) {
            doAccept(accepted)
            acceptPending = false
            return
          }
          val (l, r, e) = pairs.get(i)
          val (lP, rP, eP) = UtilityScala.randomConfiguration(l, r)
          if (eP < e) {
            pairs.update(i, (lP, rP, eP))
            notifyObservers(pairs)
          }
        }
      }

      f
    }
  }
}