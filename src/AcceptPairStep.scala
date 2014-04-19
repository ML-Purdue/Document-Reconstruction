import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import javax.swing.JFrame;
import java.util.ArrayList;

class AcceptPairStep(listener: Listener) extends Step(listener) {

  val (rows, cols) = (0, 5);
  var frame: JFrame = new JFrame();
  var triplets: ArrayList[((Piece, Piece), BufferedImage, Double)] = new ArrayList[((Piece, Piece), BufferedImage, Double)](); ;

  override def begin(input: Object) = {
    frame.setLayout(new GridLayout(rows, cols));
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
  }

  def notifyTriplets(pairs: ArrayList[((Piece, Piece), BufferedImage, Double)]) =
    {
      triplets.clear();
      triplets.addAll(pairs);
      for (i <- 0 until triplets.size()) {
        frame.add(new AcceptPairView(triplets.get(i)._2));
      }
    }
}
