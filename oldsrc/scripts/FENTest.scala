import com.alanjz.meerkat.util.position.immutable.FENParser

object FENTest extends App {
  val node = FENParser.parse("r3k2r/p1ppqpb1/bn2pnp1/3PN3/1p2P3/2N2Q1p/PPPBBPPP/R3K2R w KQkq - 0 1")
  println(node.verboseString)
}
