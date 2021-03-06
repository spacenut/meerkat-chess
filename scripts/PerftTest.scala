import com.alanjz.meerkat.moves.Move.{PawnPromoteCapture, PawnPromote}
import com.alanjz.meerkat.moves.{Attacker, Move, PseudoLegalMover}
import com.alanjz.meerkat.position.mutable.MaskNode
import com.alanjz.meerkat.util.numerics.BitMask
import com.alanjz.meerkat.util.position.mutable.FENMaskNodeBuilder

/**
 * Created by alan on 12/15/14.
 */
object PerftTest extends App {
  def divide(node : MaskNode, depth : Int) : Unit = {
    var nodeCount = 0
    var moveCount = 0
    if(depth == 0) return
    val moves = new PseudoLegalMover(node).getMoves
    for (move <- moves) {
      node.make(move)
      val a = perft(node, depth-1)
      node.unmake()
      move match {
        case PawnPromote(o, t, p) => println(s"$o$t${p.toChar.toUpper} $a")
        case PawnPromoteCapture(o, t, c, p) => println(s"$o$t${p.toChar.toUpper} $a")
        case m: Move => println(s"${m.origin}${m.target} $a")
      }
      nodeCount += a
      if(a > 0) moveCount += 1
    }
    println(s"Nodes: $nodeCount")
    println(s"Moves: $moveCount")
  }

  def perft(node : MaskNode, depth : Int) : Int = {
    var num = 0
    if(depth == 0) {
      if(node.isTerminal) return 0
      else return 1
    }
    else if(node.isTerminal) {
      return 0
    }
    val moves = new PseudoLegalMover(node).getMoves
    for (move <- moves) {
      node.make(move)
      num += perft(node, depth-1)
      node.unmake()
    }
    num
  }

  //FENMaskNodeBuilder.parse("r4r1k/p1pNqpb1/bn2pnp1/3P4/1p2P3/2N2Q1p/PPPBBPPP/R4K1R w - - 0 1")
  //FENMaskNodeBuilder.parse("r3k2r/p2pqpb1/bn1ppnp1/1B2N3/1p2P3/2N2Q1p/PPPB1PPP/R3K2R b KQkq - 0 1")
  val node = FENMaskNodeBuilder.parse("rnbqkb1r/pp1p1ppp/2p5/4P3/2B5/8/PPP1NnPP/RNBQK2R w KQkq - 0 6")
  // MaskNode.initialPosition

  var moves = new PseudoLegalMover(node).getMoves
  /*node.make(moves.find(_.toString=="Nc6").get)
  moves = new PseudoLegalMover(node).getMoves
  node.make(moves.find(_.toString=="hxg2").get)
  moves = new PseudoLegalMover(node).getMoves
  node.make(moves.find(_.toString=="Nb8").get)
  moves = new PseudoLegalMover(node).getMoves*/

  println(node)
  println(moves.mkString(" "))

  val start = System.nanoTime()
  divide(node,4)
  println(s"${(System.nanoTime() - start) / 1e9}s")
}
