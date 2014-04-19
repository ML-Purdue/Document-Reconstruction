import java.awt.GridLayout
import java.awt.image.BufferedImage
import java.util._
import java.util.ArrayList
import javax.swing.JPanel

import scala.collection.JavaConversions._
import scala.collection.JavaConverters._
import scala.collection.mutable.ListBuffer

import javax.swing.JFrame

class AcceptPairStep(listener: Listener) extends Step(listener) with Observer {

  val (rows, cols) = (0, 10);
  var frame: JFrame = new JFrame();
  var panel: JPanel = new JPanel();
  var triplets: List[(Piece, Piece, Double)] = null;
  var pm: PairManager = null;
  override def begin(input: Object) = {
    panel.setLayout(new GridLayout(rows, cols));
    frame.setContentPane(panel);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    pm = new PairManager(input.asInstanceOf[ArrayList[Piece]].to[ListBuffer]);
    pm.addObserver(this);
    new Thread(pm).start();
  }

  def update(o: Observable, arg: Object) = {
    triplets = arg.asInstanceOf[ListBuffer[(Piece, Piece, Double)]];
    panel.removeAll();
    println(triplets.size());
    for (i <- 0 until triplets.size()) {
      panel.add(new AcceptPairView(UtilityScala.merge(triplets.get(i)._1, triplets.get(i)._2).image, i, this));
    }
    frame.revalidate();
    frame.repaint();
  }
  def accepted(index: Integer) = {
    pm.acceptPair(triplets.get(index)._1, triplets.get(index)._2, triplets.get(index)._3);
  }
}