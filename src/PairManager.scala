import java.util._
import scala.collection.mutable.ListBuffer
import scala.collection.JavaConversions._
import scala.collection.JavaConverters._

class PairManager(var pieces: ListBuffer[Piece]) extends Observable() with Runnable {
  var acceptPending = false
  var accepted: (Piece, Piece, Double) = null
  var pairs = (for (i <- 0 until pieces.size; j <- i + 1 until pieces.size) yield (new Piece(pieces.get(i)), new Piece(pieces.get(j)), Double.PositiveInfinity)).to[ListBuffer]

  def acceptPair(piece: (Piece, Piece, Double)) = {
    accepted = piece
    acceptPending = true
  }

  def doAccept(pair: (Piece, Piece, Double)) = {
    val (l, r, e) = pair
    val merged = UtilityScala.merge(l, r)

    printf("pieces before %d\n", pieces.size)
    for (p <- pieces) {
      println(p)
    }
    println("l " + l)
    println("r " + r)
    pieces = pieces - l
    pieces = pieces - r
    //pieces = merged +=: (pieces.filter(p => !p.equals(l) && !p.equals(r)))
    printf("pieces after %d\n", pieces.size)

    //    val (l, r, e) = pair
    //    val merged = UtilityScala.merge(l, r)
    //    printf("pieces before %d\n", pieces.size)
    //    pieces = merged +=: pieces.filter(p => !p.equals(l) && !p.equals(r))
    //    printf("pieces after %d\n", pieces.size)
    //    val hit = (p: (Piece, Piece, Double)) => if (p._1.equals(l) || p._1.equals(r) || p._2.equals(l) || p._2.equals(r)) { true } else { false }
    //    printf("pairs before %d\n", pairs.size)
    //    pairs = pairs.filterNot(hit)
    //    printf("pairs after filter %d\n", pairs.size)
    //    for (p <- pieces) {
    //      pairs = (p, merged, Double.PositiveInfinity) +=: pairs
    //    }
    //    printf("pairs after adds %d\n", pairs.size)
    //    pairs = pairs.sortBy(_._3).reverse
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
            setChanged()
            notifyObservers(pairs)
          }
        }
      }

      f
    }
  }
}